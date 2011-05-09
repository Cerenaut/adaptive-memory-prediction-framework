/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.demos.rgb;

import ai.general.util.ui.Painting;
import ai.general.nn.Schedule;
import ai.general.nn.unsupervised.SOM2D2;
import ai.general.volumes.VolumeMap;
import ai.general.util.AbstractPair;
import ai.general.util.Iterative;
import ai.general.util.ui.Paintable;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author dave
 */
public class RGBSOM implements Paintable, Iterative {

    public VolumeMap _vm = new VolumeMap();
    public Schedule _s;
    public SOM2D2 _som;
    public int _w = 0;
    public int _h = 0;

    public RGBStochasticProcess _process = new RGBStochasticProcess();
    
    public RGBSOM() {

        int inputs = 3;
        int size = 10;
        int schedule = 1000;//2500;

        _s = Schedule.Create( schedule );
        _w = 640;
        _h = 480;

        _som = new SOM2D2( _vm, "rgb.som", _s, inputs, size );
        _som._learnOnline = false;
        _som._bias = false;
        _som._p.set( "som-sigma-initial", 10.0 );
        _som._p.set( "som-sigma-final", 0.1 );//0.5 0.4 0.3 );
        _som._p.set( "som-roulette-power", 12.0 ); // default

        configureProcess();
    }

    protected void configureProcess() {
        // uniform random unit value
        _process._clampWrap = true;
        _process._clamp = false;
        _process._normalNoise = false;
        _process.setRGBUniform();
        _process.setBias( 0.0f );
        _process.setNoise( 1.0f, 1.0f, 1.0f ); // reduce gamut
    }

    @Override public void pre() {}
    @Override public void post() {}
    @Override public void step() {

        _s.call();

        // make a random RGB:
        float[] rgb = _process.next();

        _som._vif._model[ 0 ] = rgb[ 0 ];
        _som._vif._model[ 1 ] = rgb[ 1 ];
        _som._vif._model[ 2 ] = rgb[ 2 ];

        _som.ff();
    }

    @Override public AbstractPair< Integer, Integer > size() {

        AbstractPair< Integer, Integer > ap = new AbstractPair( _w, _h );

        return ap;
    }

    @Override public void paint( Graphics2D g2d ) {

//      INPUT colour...
//
//        WEIGHTS MATRIX
//        I ------------->
//      J
//      |
//      |
//        ACTIVATION MATRIX
//        I ------------->
//      J
//      |
//      |
        int sizeI  = _som._vw._d.size( "som.i" );
        int sizeJ  = _som._vw._d.size( "som.j" );
        int sizeW  = _som._vw._d.size( "som.w" );
        int sizeIJ = sizeI * sizeJ;
        int sizeJW = sizeJ * sizeW;

        int wUnits = sizeI;
        int hUnits = sizeJ * 2 +1;
        int unitW = _w / wUnits;
        int unitH = _h / hUnits;

        int r = (int)( _som._vif._model[ 0 ] * 255.0f );
        int g = (int)( _som._vif._model[ 1 ] * 255.0f );
        int b = (int)( _som._vif._model[ 2 ] * 255.0f );

        g2d.setColor( new Color( r,g,b ) );
        g2d.fillRect( 0, 0, _w, unitH );
        
        for( int i = 0; i < sizeI; ++i ) {
            for( int j = 0; j < sizeJ; ++j ) {

                int offsetW = sizeJW * i
                            + sizeW  * j
                            +          0; // w===x

                r = (int)( _som._vw._model[ offsetW +0 ] * 255.0f );
                g = (int)( _som._vw._model[ offsetW +1 ] * 255.0f );
                b = (int)( _som._vw._model[ offsetW +2 ] * 255.0f );

                g2d.setColor( new Color( r,g,b ) );

                int x = i     * unitW;
                int y = (j+1) * unitH;
                g2d.fillRect( x, y, unitW-1, unitH-1 );

                int offsetO = sizeJ * i + j;

                int a = (int)( _som._vof._model[ offsetO ] * 255.0f );

                g2d.setColor( new Color( a,a,a ) );

                x = i           * unitW;
                y = (j+1+sizeJ) * unitH;
                g2d.fillRect( x, y, unitW-1, unitH-1 );
                
            }
        }

        Painting.paintSOM2dRGB( g2d, 0,0,10,10,2, _som );
    }
}
