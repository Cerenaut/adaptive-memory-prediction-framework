/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.demos.rgb;

import ai.general.util.ui.Painting;
import ai.general.mpf.mm.FirstOrderMM;
import ai.general.volumes.Coordinate;
import ai.general.volumes.Volume;
import ai.general.util.MovingWindow;
import ai.general.util.Maths;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author dave
 */
public class RGBSOM1MM extends RGBSOM {

    public FirstOrderMM _1mm;

    public float[] _rgbff;
    public float[] _rgbfb;
    public float[] _rgbpp; // rgb of previous prediction
    public Coordinate _cpp; // previous FB coordinate
    public Volume _vpp; // previous prediction

    public MovingWindow _es = new MovingWindow();
    
    public RGBSOM1MM() {// int w, int h, int size, int errorPeriod ) {
        super();// w,h, size );

        _1mm = new FirstOrderMM( _vm, "rgb.hmm", _s, _som._dof );
        _1mm._predictSameState = false;
        _vpp = new Volume( _1mm._dof );
        
        int errorPeriod = 1000;
        
        _es.resize( errorPeriod );

        _rgbff = new float[ 3 ];
        _rgbff[ 0 ] = (float)Maths.random();
        _rgbff[ 1 ] = (float)Maths.random();
        _rgbff[ 2 ] = (float)Maths.random();

        _rgbfb = new float[ 3 ];
        _rgbfb[ 0 ] = _rgbff[ 0 ];
        _rgbfb[ 1 ] = _rgbff[ 1 ];
        _rgbfb[ 2 ] = _rgbff[ 2 ];

        _rgbpp = new float[ 3 ];
        _rgbpp[ 0 ] = _rgbff[ 0 ];
        _rgbpp[ 1 ] = _rgbff[ 1 ];
        _rgbpp[ 2 ] = _rgbff[ 2 ];
    }

    @Override protected void configureProcess() {
        // normal stochastic unit value, biased and noise scaled
        _process._clampWrap = false;
        _process._wrap = true; // was: false
        _process._circular = false; // was: false
        _process._clamp = false;
        _process._normalNoise = false;
        _process.setRGBUniform();
        _process.setBias( 0.2f, 0.5f, -0.1f ); // R either way, G increasing, B decreasing (a little)
        _process.setNoise( 0.1f );
    }

    protected void updateError() {
        // compare to prediction:
        double er = Math.abs( _rgbff[ 0 ] - _rgbfb[ 0 ] );
        double eg = Math.abs( _rgbff[ 1 ] - _rgbfb[ 1 ] );
        double eb = Math.abs( _rgbff[ 2 ] - _rgbfb[ 2 ] );
        double error = ( er + eg + eb ) / 3.0;

        _es.update( (float)error );

        System.out.print( _es.summary() );

        double minError = minError( _rgbff ); // the best it could've done, given quantization

        System.out.println( " min.err="+minError );
    }

    @Override public void step() {

        // Update Schedule & Stochastic Process
        // ---------------------------------------------------------------------
        _s.call();
        
        _rgbff = _process.next(); // make a stochastic RGB:

        updateError(); // compare actual RGB to predicted FB RGB

        
        // FF PASS:
        // Classify this input with SOM, produce a PDF of match output.
        // ---------------------------------------------------------------------
        _som._vif._model[ 0 ] = _rgbff[ 0 ];
        _som._vif._model[ 1 ] = _rgbff[ 1 ];
        _som._vif._model[ 2 ] = _rgbff[ 2 ];
        _som.ff();


        // REFLECTION:
        // Predict next value:
        // ---------------------------------------------------------------------
        _vpp.copy( _1mm._vof ); // copy previous prediction
        if( _som._cSelectedFB != null ) {
            _cpp = new Coordinate( _som._cSelectedFB );
        }
        _rgbpp[ 0 ] = _rgbfb[ 0 ];
        _rgbpp[ 1 ] = _rgbfb[ 1 ];
        _rgbpp[ 2 ] = _rgbfb[ 2 ];

        _1mm._vif.set( 0.0f );
        _1mm._vif.set( _som._cSelectedFF, 1.0f ); // make input to predictor orthogonal
        _1mm.ff(); // make new prediction


        // FB PASS:
        // Now reverse the predictions through and work out the predicted input.
        // ---------------------------------------------------------------------
        _som._vob.copy( _1mm._vof );// output replaced
        _som.fb(); // overwrites SOM input, roulette


        // EXTRACT PREDICTED RGB:
        // ---------------------------------------------------------------------
        _rgbfb[ 0 ] = _som._vib._model[ 0 ];
        _rgbfb[ 1 ] = _som._vib._model[ 1 ];
        _rgbfb[ 2 ] = _som._vib._model[ 2 ];
    }

    protected double minError( float[] rgb ) {
        int sizeI  = _som._vw._d.size( "som.i" );
        int sizeJ  = _som._vw._d.size( "som.j" );
        int sizeW  = _som._vw._d.size( "som.w" );
        int sizeIJ = sizeI * sizeJ;
        int sizeJW = sizeJ * sizeW;

        double minError = Double.MAX_VALUE;
        double r1 = rgb[ 0 ];
        double g1 = rgb[ 1 ];
        double b1 = rgb[ 2 ];

        for( int i = 0; i < sizeI; ++i ) {
            for( int j = 0; j < sizeJ; ++j ) {

                // WEIGHTS MATRIX:
                int offsetW = sizeJW * i
                            + sizeW  * j
                            +          0; // w===x

                double r2 = _som._vw._model[ offsetW +0 ];
                double g2 = _som._vw._model[ offsetW +1 ];
                double b2 = _som._vw._model[ offsetW +2 ];

                double er = Math.abs( r1 - r2 );
                double eg = Math.abs( g1 - g2 );
                double eb = Math.abs( b1 - b2 );
                double error = ( er + eg + eb ) / 3.0;

                if( error < minError ) {
                    minError = error;
                }
            }
        }

        return minError;
    }
    
    @Override public void paint( Graphics2D g2d ) {

        Painting.paintBackground( g2d, this, Color.WHITE );

        int sizeI  = _som._vw._d.size( "som.i" );
        int g = 12; // gap
        int c = ( _w - (3*g) ) >> 1; // cell
        int m = c / sizeI; // model size

        boolean paintLabels = true;
        
        Painting.paintSOM2dRGB(
            g2d,
            Painting.g( c, g, 1 ) + Painting.offsetPositionError( sizeI, c ),
            Painting.g( c, g, 1 ) + Painting.offsetPositionError( sizeI, c ),
            m,
            m,
            0,
            _som );

        g2d.setColor( Color.RED );
        if( paintLabels )
            g2d.drawString( "SOM weights", Painting.g( c, g, 1 )+m, Painting.g( c, g, 1 )+m );

        Painting.paint2d1(
            g2d,
            Painting.g( c, g, 1 ),
            Painting.g( c, g, 2 ),
            c,
            c,
            _som._vof,
            true );

        g2d.setColor( Color.RED );
        if( paintLabels )
            g2d.drawString( "SOM classification (t)", Painting.g( c, g, 1 )+m, Painting.g( c, g, 2 )+m );

        Painting.paint2d1(
            g2d,
            Painting.g( c, g, 2 ),
            Painting.g( c, g, 2 ),
            c,
            c,
            _vpp,
            true );//,

        g2d.setColor( Color.RED );
        if( paintLabels )
            g2d.drawString( "1MM prediction (t-1)", Painting.g( c, g, 2 )+m, Painting.g( c, g, 2 )+m );

        Painting.paintCoordinate2d(
            g2d,
            Painting.g( c, g, 1 ),
            Painting.g( c, g, 2 ),
            c,
            c,
            _som._cSelectedFFa,
            Color.YELLOW );

        Painting.paintCoordinate2d(
            g2d,
            Painting.g( c, g, 1 )+1,
            Painting.g( c, g, 2 )+1,
            c,
            c,
            _som._cSelectedFFb,
            Color.GREEN );

        Painting.paintCoordinate2d(
            g2d,
            Painting.g( c, g, 2 ),
            Painting.g( c, g, 2 ),
            c,
            c,
//            _som._cSelectedFB,
            _cpp,
            Color.RED );

        Painting.paintRGB1(
            g2d,
            Painting.g( c, g, 2 ),
            Painting.g( c, g, 1 ),
            c,
            (c-g) >> 1,
            _rgbff,
            0 );

        g2d.setColor( Color.RED );
        if( paintLabels )
            g2d.drawString( "FF input", Painting.g( c, g, 2 )+m, Painting.g( c, g, 1 )+m );

        Painting.paintRGB1(
            g2d,
            Painting.g( c, g, 2 ),
            Painting.g( c, g, 1 )+g+((c-g)>>1),
            c,
            (c-g) >> 1,
//            _rgbfb,
            _rgbpp,
            0 );

        g2d.setColor( Color.RED );
        if( paintLabels )
            g2d.drawString( "FB output", Painting.g( c, g, 2 )+m, Painting.g( c, g, 1 )+m+g+((c-g)>>1) );
    }

}
