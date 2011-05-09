/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.demos.rps;

import ai.general.util.ui.Painting;
import ai.general.util.AbstractPair;
import ai.general.util.ui.Paintable;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author David Rawlinson
 * @copyright David Rawlinson
 */
public class RPSPainter extends Painting implements Paintable {//, KeyListener {

    public int _w;
    public int _h;
    public Game _g;

    public RPSPainter( Game g ) {
        _w = 1280;
        _h = 640;
        _g = g;
    }

    @Override public AbstractPair< Integer, Integer > size() {
        AbstractPair< Integer, Integer > ap = new AbstractPair( _w, _h );
        return ap;
    }

    @Override public void paint( Graphics2D g2d ) {

        paintBackground( g2d, this, Color.WHITE );
        
        int sizeI  = _g._nu1._sp._vw._d.size( "som.i" );
        int cols = 9;
        int g = 4; // gap
        int c = ( _w - ((cols+1)*g) ) / cols;// >> 1; // cell
        int m = c / sizeI; // model size

        boolean paintLabels = false;//true;

        g2d.setColor( new Color( 208,255,00 ) );
        g2d.fillRect( g( c, g, 2 ), 0, g( c, g, 6 )-g( c, g, 3 ), _h );
        g2d.fillRect( g( c, g, 7 ), 0, g( c, g, 9 )-g( c, g, 6 ), _h );
        g2d.setColor( Color.BLUE );
        g2d.drawString( "World and ", g( c, g, 1 )+m, m );
        g2d.drawString( "Sensor-Motor", g( c, g, 1 )+m, m+m );
        g2d.drawString( "Interface", g( c, g, 1 )+m, m+m+m );
        g2d.drawString( "Neocortical Unit #1", g( c, g, 2 )+m, m );
        g2d.drawString( "Reward Correlator", g( c, g, 5 )+m, m );
        g2d.drawString( "Neocortical Unit #2", g( c, g, 7 )+m, m );

        // World / Interface
        Color outcome = null;

        if( _g._outcome == Moves.WIN ) {
            outcome = Color.GREEN;
        }
        else if( _g._outcome == Moves.DRAW ) {
            outcome = Color.YELLOW;
        }
        else { // lose
            outcome = Color.RED;
        }

        paintBox( g2d, g,(m*5)-(2*g), c, m, outcome, null, Color.BLACK );
        g2d.setColor( Color.BLACK );
        g2d.drawString( "Outcome:", g,(m*4)+g );
        g2d.drawString( "Err="+String.format( "%.5g%n", _g._error.mean() ), g,(m*7) );
        g2d.drawString( "Gestures/moves:", g,(m*8) );

        paintSequence( g2d, (m*1),(m*8)+g, m,m, g, _g._g._rps );
        paintBox( g2d, (m*1)+(0*g),(m*9)+g, m,m, Color.RED, null, null );
        paintBox( g2d, (m*2)+(1*g),(m*9)+g, m,m, Color.GREEN, null, null );
        paintBox( g2d, (m*3)+(2*g),(m*9)+g, m,m, Color.BLUE, null, null );

        int patternVolume = _g._g._rps.get( Moves.ROCK ).volume();
        int patternSize = (int)Math.sqrt( patternVolume );

        paint2d1( g2d, (m*6)-(g*2),(m*11)+g, m,m, _g._g._rps.get( _g._moveThis )._model, 0, patternSize,patternSize, null );
        paint2d1( g2d, (m*6)-(g*2),(m*12)+g, m,m, _g._g._rps.get( _g._moveThat )._model, 0, patternSize,patternSize, null );
        paint2d1( g2d, (m*6)-(g*2),(m*13)+g, m,m, _g._gestureObserved._model, 0, patternSize,patternSize, null );
        paint2d1( g2d, (m*6)-(g*2),(m*14)+g, m,m, _g._gestureExpected0._model, 0, patternSize,patternSize, null );

        g2d.drawString( "Hierarchy's Move", g,(m*12) );
        g2d.drawString( "Opponent's Move",  g,(m*13) );
        g2d.drawString( "Gesture Observed", g,(m*14) );
        g2d.drawString( "Gesture Expected", g,(m*15) );

        paint2d1( g2d, (m*4),(m*16)+g, m,m, _g._fn._vof );
        paint2d1( g2d, (m*4),(m*17)+g, m,m, _g._fn._vob );
        g2d.drawString( "Vision FF", g,(m*17) );
        g2d.drawString( "Vision FB", g,(m*18) );

        g2d.drawString( "Motor I.FF", g,(m*20) );
        paint2d1( g2d, (m*5),(m*19)+g, m,m, _g._da._vif );
        paintRGB1( g2d, (m*4),(m*19)+g, m,m, _g._da._vif._model, 0 );
        g2d.drawString( "Motor O.FF", g,(m*21) );
        paint2d1( g2d, (m*5),(m*20)+g, m,m, _g._da._vof );
        paintRGB1( g2d, (m*4),(m*20)+g, m,m, _g._da._vof._model, 0 );
        g2d.drawString( "Motor I.FB", g,(m*22) );
        paint2d1( g2d, (m*5),(m*21)+g, m,m, _g._da._vob );
        paintRGB1( g2d, (m*4),(m*21)+g, m,m, _g._da._vob._model, 0 );
        g2d.drawString( "Motor O.FB", g,(m*23) );
        paint2d1( g2d, (m*5),(m*22)+g, m,m, _g._da._vib );
        paintRGB1( g2d, (m*4),(m*22)+g, m,m, _g._da._vib._model, 0 );

        // NU1
        paintSOM2d1( g2d,
            Painting.g( c, g, 2 ),
            Painting.g( c, g, 1 )+(m*1),
            m, m, g, _g._nu1._sp );
        paintSOM2dRGB( g2d,
            Painting.g( c, g, 2 ),
            Painting.g( c, g, 2 )+(m*1),
            m, m, g, _g._nu1._sp, _g._fn._dof.volume() );

        paintCoordinate2d( g2d,
            Painting.g( c, g, 2 ),
            Painting.g( c, g, 1 )+(m*1),
            c-g, c-g, _g._nu1._sp._cSelectedFFa, Color.YELLOW );
        paintCoordinate2d( g2d,
            Painting.g( c, g, 2 )-1,
            Painting.g( c, g, 1 )+(m*1)-1,
            c-g, c-g, _g._nu1._sp._cSelectedFFb, Color.GREEN );
        paintCoordinate2d( g2d,
            Painting.g( c, g, 2 )-2,
            Painting.g( c, g, 1 )+(m*1)-2,
            c-g, c-g, _g._nu1._sp._cSelectedFB, Color.RED );

        if( paintLabels ) {
            g2d.setColor( Color.RED );
            g2d.drawString( "SOM models (V)", Painting.g( c, g, 2 )+(1*m), Painting.g( c, g, 1 )+(2*m) );
            g2d.drawString( "SOM models (M)", Painting.g( c, g, 2 )+(1*m), Painting.g( c, g, 2 )+(2*m) );
        }

        paintCoordinate2d( g2d,
            Painting.g( c, g, 2 ),
            Painting.g( c, g, 2 )+(m*1),
            c-g, c-g, _g._nu1._sp._cSelectedFFa, Color.YELLOW );
        paintCoordinate2d( g2d,
            Painting.g( c, g, 2 )-1,
            Painting.g( c, g, 2 )+(m*1)-1,
            c-g, c-g, _g._nu1._sp._cSelectedFFb, Color.GREEN );
        paintCoordinate2d( g2d,
            Painting.g( c, g, 2 )-2,
            Painting.g( c, g, 2 )+(m*1)-2,
            c-g, c-g, _g._nu1._sp._cSelectedFB, Color.RED );

        paint2d1( g2d,
            Painting.g( c, g, 2 ),
            Painting.g( c, g, 4 )+(m*1),
            c, c, _g._nu1._sp._vrb,
            true );//,

        if( paintLabels ) {
            g2d.setColor( Color.RED );
            g2d.drawString( "FB Roulette", Painting.g( c, g, 2 )+(1*m), Painting.g( c, g, 4 )+(2*m) );
        }

        paint2d1( g2d,
            Painting.g( c, g, 3 ),
            Painting.g( c, g, 1 )+(m*1),
            c, c, _g._nu1._sp._vaf,
            true );//,
        paint2d1( g2d,
            Painting.g( c, g, 3 ),
            Painting.g( c, g, 2 )+(m*1),
            c, c, _g._nu1._sp._vbf,
            true );//,
        paint2d1( g2d,
            Painting.g( c, g, 3 ),
            Painting.g( c, g, 3 )+(m*1),
            c, c, _g._nu1._fbn,
            true );//,

        if( paintLabels ) {
            g2d.setColor( Color.RED );
            g2d.drawString( "FF Activation",   Painting.g( c, g, 3 )+(1*m), Painting.g( c, g, 1 )+(2*m) );
            g2d.drawString( "FF Bias",         Painting.g( c, g, 3 )+(1*m), Painting.g( c, g, 2 )+(2*m) );
            g2d.drawString( "FB (normalized)", Painting.g( c, g, 3 )+(1*m), Painting.g( c, g, 3 )+(2*m) );
        }

        paint2d1( g2d,
            Painting.g( c, g, 4 ),
            Painting.g( c, g, 1 )+(m*1),
            c, c, _g._nu1._sp._vof,
            true );//,
        paint2d1( g2d,
            Painting.g( c, g, 4 ),
            Painting.g( c, g, 2 )+(m*1),
            c, c, _g._nu1._mm._vif,
            true );//,
        paint2d1( g2d,
            Painting.g( c, g, 4 ),
            Painting.g( c, g, 3 )+(m*1),
            c, c, _g._nu1._mm._vof,
            true );//,
        paint2d1( g2d,
            Painting.g( c, g, 4 ),
            Painting.g( c, g, 4 )+(m*1),
            c, c, _g._nu1._sp._vob,
            true );//,

        if( paintLabels ) {
            g2d.setColor( Color.RED );
            g2d.drawString( "FF Output",     Painting.g( c, g, 4 )+(1*m), Painting.g( c, g, 1 )+(2*m) );
            g2d.drawString( "MM Input",      Painting.g( c, g, 4 )+(1*m), Painting.g( c, g, 2 )+(2*m) );
            g2d.drawString( "MM Prediction", Painting.g( c, g, 4 )+(1*m), Painting.g( c, g, 3 )+(2*m) );
            g2d.drawString( "FB Input",      Painting.g( c, g, 4 )+(1*m), Painting.g( c, g, 4 )+(2*m) );
        }

        paintCoordinate2d( g2d,
            Painting.g( c, g, 3 ),
            Painting.g( c, g, 1 )+(m*1),
            c-g, c-g, _g._nu1._sp._cSelectedFFa, Color.YELLOW );
        paintCoordinate2d( g2d,
            Painting.g( c, g, 3 )-1,
            Painting.g( c, g, 2 )+(m*1)-1,
            c-g, c-g, _g._nu1._sp._cSelectedFFb, Color.GREEN );
        paintCoordinate2d( g2d,
            Painting.g( c, g, 2 )-2,
            Painting.g( c, g, 4 )+(m*1)-2,
            c-g, c-g, _g._nu1._sp._cSelectedFB, Color.RED );


        // RC
        paint2d1( g2d,
            Painting.g( c, g, 5 ),
            Painting.g( c, g, 1 )+(m*1),
            c, c, _g._rc._vif,
            true );//,
        paint2d1( g2d,
            Painting.g( c, g, 6 ),
            Painting.g( c, g, 1 )+(m*1),
            c, c, _g._rc._vof,
            true );//,
        paint2d1( g2d,
            Painting.g( c, g, 5 ),
            Painting.g( c, g, 4 )+(m*1),
            c, c, _g._rc._vib,
            true );//,
        paint2d1( g2d,
            Painting.g( c, g, 6 ),
            Painting.g( c, g, 4 )+(m*1),
            c, c, _g._rc._vob,
            true );//,
        paint2dC( g2d,
            Painting.g( c, g, 5 ) + (c>>1),
            Painting.g( c, g, 2 )+(m*1),
            c, c, _g._rc._vrc, null );//,
        paint2d1( g2d,
            Painting.g( c, g, 5 ) + (c>>1),
            Painting.g( c, g, 3 )+(m*1),
            c, c, _g._rc._vrb,
            true );//,

        if( paintLabels ) {
            g2d.setColor( Color.RED );
            g2d.drawString( "FF Input",           Painting.g( c, g, 5 )+(1*m), Painting.g( c, g, 1 )+(2*m) );
            g2d.drawString( "FF Output",          Painting.g( c, g, 6 )+(1*m), Painting.g( c, g, 1 )+(2*m) );
            g2d.drawString( "Reward Correlation", Painting.g( c, g, 5 )+g + (c>>1), Painting.g( c, g, 2 )+(2*m) );
            g2d.drawString( "Adaptive Bias",      Painting.g( c, g, 5 )+g + (c>>1), Painting.g( c, g, 3 )+(2*m) );
            g2d.drawString( "FB Input",           Painting.g( c, g, 6 )+(1*m), Painting.g( c, g, 4 )+(2*m) );
            g2d.drawString( "FB Output",          Painting.g( c, g, 5 )+(1*m), Painting.g( c, g, 4 )+(2*m) );
        }

        // NU2
        paintSOM2d1( g2d,
            Painting.g( c, g, 7 ),
            Painting.g( c, g, 1 )+(m*1),
            m, m, 2, _g._nu2._sp );

        if( paintLabels ) {
            g2d.setColor( Color.RED );
            g2d.drawString( "SOM models", Painting.g( c, g, 7 )+(1*m), Painting.g( c, g, 1 )+(2*m) );
        }

        paintCoordinate2d( g2d,
            Painting.g( c, g, 7 ),
            Painting.g( c, g, 1 )+(m*1),
            c-g, c-g, _g._nu2._sp._cSelectedFFa, Color.YELLOW );
        paintCoordinate2d( g2d,
            Painting.g( c, g, 7 )-1,
            Painting.g( c, g, 1 )+(m*1)-1,
            c-g, c-g, _g._nu2._sp._cSelectedFFb, Color.GREEN );
        paintCoordinate2d( g2d,
            Painting.g( c, g, 7 )-2,
            Painting.g( c, g, 1 )+(m*1)-2,
            c-g, c-g, _g._nu2._sp._cSelectedFB, Color.RED );

        paint2d1( g2d,
            Painting.g( c, g, 7 ),
            Painting.g( c, g, 4 )+(m*1),
            c, c, _g._nu2._sp._vrb,
            true );//,

        if( paintLabels ) {
            g2d.setColor( Color.RED );
            g2d.drawString( "FB Roulette", Painting.g( c, g, 7 )+(1*m), Painting.g( c, g, 4 )+(2*m) );
        }

        paint2d1( g2d,
            Painting.g( c, g, 8 ),
            Painting.g( c, g, 1 )+(m*1),
            c, c, _g._nu2._sp._vaf,
            true );//,
        paint2d1( g2d,
            Painting.g( c, g, 8 ),
            Painting.g( c, g, 2 )+(m*1),
            c, c, _g._nu2._sp._vbf,
            true );//,
        paint2d1( g2d,
            Painting.g( c, g, 8 ),
            Painting.g( c, g, 3 )+(m*1),
            c, c, _g._nu2._fbn,
            true );//,

        if( paintLabels ) {
            g2d.setColor( Color.RED );
            g2d.drawString( "FF Activation",   Painting.g( c, g, 8 )+(1*m), Painting.g( c, g, 1 )+(2*m) );
            g2d.drawString( "FF Bias",         Painting.g( c, g, 8 )+(1*m), Painting.g( c, g, 2 )+(2*m) );
            g2d.drawString( "FB (normalized)", Painting.g( c, g, 8 )+(1*m), Painting.g( c, g, 3 )+(2*m) );
        }

        paint2d1( g2d,
            Painting.g( c, g, 9 ),
            Painting.g( c, g, 1 )+(m*1),
            c, c, _g._nu2._sp._vof,
            true );//,
        paint2d1( g2d,
            Painting.g( c, g, 9 ),
            Painting.g( c, g, 2 )+(m*1),
            c, c, _g._nu2._mm._vif,
            true );//,
        paint2d1( g2d,
            Painting.g( c, g, 9 ),
            Painting.g( c, g, 3 )+(m*1),
            c, c, _g._nu2._mm._vof,
            true );//,
        paint2d1( g2d,
            Painting.g( c, g, 9 ),
            Painting.g( c, g, 4 )+(m*1),
            c, c, _g._nu2._sp._vob,
            true );//,

        if( paintLabels ) {
            g2d.setColor( Color.RED );
            g2d.drawString( "FF Output",     Painting.g( c, g, 9 )+(1*m), Painting.g( c, g, 1 )+(2*m) );
            g2d.drawString( "MM Input",      Painting.g( c, g, 9 )+(1*m), Painting.g( c, g, 2 )+(2*m) );
            g2d.drawString( "MM Prediction", Painting.g( c, g, 9 )+(1*m), Painting.g( c, g, 3 )+(2*m) );
            g2d.drawString( "FB Input",      Painting.g( c, g, 9 )+(1*m), Painting.g( c, g, 4 )+(2*m) );
        }
        
        paintCoordinate2d( g2d,
            Painting.g( c, g, 8 ),
            Painting.g( c, g, 1 )+(m*1),
            c-g, c-g, _g._nu2._sp._cSelectedFFa, Color.YELLOW );
        paintCoordinate2d( g2d,
            Painting.g( c, g, 8 )-1,
            Painting.g( c, g, 2 )+(m*1)-1,
            c-g, c-g, _g._nu2._sp._cSelectedFFb, Color.GREEN );
        paintCoordinate2d( g2d,
            Painting.g( c, g, 7 )-2,
            Painting.g( c, g, 4 )+(m*1)-2,
            c-g, c-g, _g._nu2._sp._cSelectedFB, Color.RED );
    }
}
