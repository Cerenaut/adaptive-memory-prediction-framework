/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.demos.words;

import ai.general.util.ui.Painting;
import ai.general.mpf.mm.InputSequence;
import ai.general.mpf.mm.MarkovModel;
import ai.general.mpf.mm.VariableOrderMM;
import ai.general.util.AbstractPair;
import ai.general.util.ui.Paintable;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author David Rawlinson
 * @copyright David Rawlinson
 */
public class WordsPainter extends Painting implements Paintable {//, KeyListener {

    public int _w;
    public int _h;

    public Words _words;

    public WordsPainter( Words w ) {
        _w = 1280;
        _h = 1024;
        _words = w;
    }

    @Override public AbstractPair< Integer, Integer > size() {
        AbstractPair< Integer, Integer > ap = new AbstractPair( _w, _h );
        return ap;
    }

    @Override public void paint( Graphics2D g2d ) {

        paintBackground( g2d, this, Color.WHITE );

        int sizeI  = _words._nu._sp._vw._d.size( "som.i" );
        int cols = 7;
        int g = 4; // gap
        int c = ( _w - ((cols+1)*g) ) / cols;// >> 1; // cell
        int m = c / sizeI; // model size

        int mv = _words._nu._vib.volume();
        int mw = (int)( Math.sqrt( mv ) );
        int mh = mv / mw;

        boolean paintLabels = false;//true;

        paintGraph( g2d,
            Painting.g( c, g, 1 ),
            Painting.g( c, g, 1 ),
            m,m, g, _words._w._sg );

        paint2d1( g2d,
            Painting.g( c, g, 2 ),
            Painting.g( c, g, 1 ),
            m, m, _words._w._vi._model, 0, mw, mh, null );
        paint2d1( g2d,
            Painting.g( c, g, 2 ),
            Painting.g( c, g, 1 ) +g +m,
            m, m, _words._nu._vib._model, 0, mw, mh, null );

        if( paintLabels ) {
            g2d.setColor( Color.RED );
            g2d.drawString( "Words", Painting.g( c, g, 1 )+(2*m), Painting.g( c, g, 1 )+(3*m) );
            g2d.drawString( "FF input", Painting.g( c, g, 1 )+(4*m), Painting.g( c, g, 1 )+(1*m) );
            g2d.drawString( "FB output", Painting.g( c, g, 1 )+(4*m), Painting.g( c, g, 1 )+(2*m) );
        }
        
        if( _words._w._sg._is != null ) {
            paintSequence( g2d,
                Painting.g( c, g, 2 ) +g +m +m,
                Painting.g( c, g, 1 ),
                m,m, g, _words._w._sg._is );
        }
        //draw the TU selected sequence-model for comparison
        if( _words._w._sg._is != null ) {
            InputSequence is = mostSignificant( _words._nu, 3 );
            paintSequence( g2d,
                Painting.g( c, g, 2 ) +g +m +m,
                Painting.g( c, g, 1 ) +g +m,
                m,m, g, is );
        }

        paintSOM2d1( g2d,
            Painting.g( c, g, 1 ),
            Painting.g( c, g, 3 ),
            m, m, g, _words._nu._sp );
        Painting.paint2d1( g2d,
            Painting.g( c, g, 1 ),
            Painting.g( c, g, 2 ),
            c, c, _words._nu._sp._vaf,
            true );//,
        Painting.paint2d1( g2d,
            Painting.g( c, g, 2 ),
            Painting.g( c, g, 2 ),
            c, c, _words._nu._sp._vof,
            true );//,
        Painting.paint2d1( g2d,
            Painting.g( c, g, 2 ),
            Painting.g( c, g, 3 ),
            c, c, _words._nu._sp._vob,
            true );//,
        Painting.paint2d1( g2d,
            Painting.g( c, g, 1 ),
            Painting.g( c, g, 4 ),
            c, c, _words._nu._sp._vbf,
            true );//,

        if( paintLabels ) {
            g2d.setColor( Color.RED );
            g2d.drawString( "SOM models",    Painting.g( c, g, 1 )+(1*m), Painting.g( c, g, 3 )+(1*m) );
            g2d.drawString( "SOM activation", Painting.g( c, g, 1 )+(1*m), Painting.g( c, g, 2 )+(1*m) );
            g2d.drawString( "SOM FF output",  Painting.g( c, g, 2 )+(1*m), Painting.g( c, g, 2 )+(1*m) );
            g2d.drawString( "SOM FB  input",  Painting.g( c, g, 2 )+(1*m), Painting.g( c, g, 3 )+(1*m) );
            g2d.drawString( "SOM FF bias",    Painting.g( c, g, 1 )+(1*m), Painting.g( c, g, 4 )+(1*m) );
        }

        paintCoordinate2d( g2d,
            Painting.g( c, g, 2 )-1,
            Painting.g( c, g, 2 )-1,
            c,c, _words._nu._sp._cSelectedFFa, Color.ORANGE );
        paintCoordinate2d( g2d,
            Painting.g( c, g, 2 )+1,
            Painting.g( c, g, 2 )+1,
            c,c, _words._nu._sp._cSelectedFFb, Color.GREEN );
        paintCoordinate2d( g2d,
            Painting.g( c, g, 2 )-1,
            Painting.g( c, g, 3 )-1,
            c,c, _words._nu._sp._cSelectedFB,  Color.RED );

        Painting.paint2d1( g2d,
            Painting.g( c, g, 3 ),
            Painting.g( c, g, 1 ),
            c, c, _words._nu._tp._vif,
            true );//,
        Painting.paint2d1( g2d,
            Painting.g( c, g, 4 ),
            Painting.g( c, g, 1 ),
            c, c, _words._nu._tp._vof,
            true );//,
        Painting.paint2d1( g2d,
            Painting.g( c, g, 5 ),
            Painting.g( c, g, 1 ),
            c, c, _words._nu._tp._vib,
            true );//,

        if( paintLabels ) {
            g2d.setColor( Color.RED );
            g2d.drawString( "RSOM FF input",  Painting.g( c, g, 3 )+(1*m), Painting.g( c, g, 1 )+(1*m) );
            g2d.drawString( "RSOM FF output", Painting.g( c, g, 4 )+(1*m), Painting.g( c, g, 1 )+(1*m) );
            g2d.drawString( "RSOM FB output", Painting.g( c, g, 5 )+(1*m), Painting.g( c, g, 1 )+(1*m) );
        }

        paintRSOM2d1( g2d, 
            Painting.g( c, g, 3 ),
            Painting.g( c, g, 2 ),
            m,m, g, _words._nu._sp, _words._nu._tp, true, 0.0, false );

        if( paintLabels ) {
            g2d.setColor( Color.RED );
            g2d.drawString( "RSOM models",  Painting.g( c, g, 3 )+(1*m), Painting.g( c, g, 2 )+(1*m) );
        }

        Painting.paint2d1( g2d,
            Painting.g( c, g, 6 ),
            Painting.g( c, g, 1 ),
            c, c, _words._nu._mm._vif,
            true );//,
        Painting.paint2d1( g2d,
            Painting.g( c, g, 7 ),
            Painting.g( c, g, 1 ),
            c, c, _words._nu._mm._vof,
            true );//,

        if( paintLabels ) {
            g2d.setColor( Color.RED );
            g2d.drawString( "MM predictor FF input",  Painting.g( c, g, 6 )+(1*m), Painting.g( c, g, 1 )+(1*m) );
            g2d.drawString( "MM predictor FF output", Painting.g( c, g, 7 )+(1*m), Painting.g( c, g, 1 )+(1*m) );
        }

        if( _words._nu._mm.order() == MarkovModel.ORDER_N ) {

            VariableOrderMM vomm = (VariableOrderMM)_words._nu._mm;

            Painting.paint2d1( g2d,
                Painting.g( c, g, 6 ),
                Painting.g( c, g, 2 ),
                c, c, vomm._vli,
                true );//,
            Painting.paint2d1( g2d,
                Painting.g( c, g, 6 ),
                Painting.g( c, g, 3 ),
                c, c, vomm._vif0,
                true );//,

            if( paintLabels ) {
                g2d.setColor( Color.RED );
                g2d.drawString( "Local inhibition",  Painting.g( c, g, 6 )+(1*m), Painting.g( c, g, 2 )+(1*m) );
                g2d.drawString( "MM FF input (original)", Painting.g( c, g, 6 )+(1*m), Painting.g( c, g, 3 )+(1*m) );
            }

            Painting.paint2d1( g2d,
                Painting.g( c, g, 7 ),
                Painting.g( c, g, 2 ),
                c, c, vomm._vui,
                true );//,
            Painting.paint2d1( g2d,
                Painting.g( c, g, 7 ),
                Painting.g( c, g, 3 ),
                c, c, vomm._promotion,
                true );//,
            Painting.paint2d1( g2d,
                Painting.g( c, g, 7 ),
                Painting.g( c, g, 4 ),
                c, c, vomm._vof0,
                true );//,

            if( paintLabels ) {
                g2d.setColor( Color.RED );
                g2d.drawString( "Unpredicted inhibition",  Painting.g( c, g, 7 )+(1*m), Painting.g( c, g, 2 )+(1*m) );
                g2d.drawString( "VOMM local promotion", Painting.g( c, g, 7 )+(1*m), Painting.g( c, g, 3 )+(1*m) );
                g2d.drawString( "MM FF output (original)", Painting.g( c, g, 7 )+(1*m), Painting.g( c, g, 4 )+(1*m) );
            }
        }
    }
}
