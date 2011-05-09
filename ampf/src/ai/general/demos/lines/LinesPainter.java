/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.demos.lines;

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
public class LinesPainter extends Painting implements Paintable {//, KeyListener {

    public int _w;
    public int _h;

    public Lines _srs;

    public LinesPainter( Lines somrsom ) {
        _h = 640;
        _w = _h;
        _srs = somrsom;
    }

    @Override public AbstractPair< Integer, Integer > size() {
        AbstractPair< Integer, Integer > ap = new AbstractPair( _w, _h );
        return ap;
    }

    @Override public void paint( Graphics2D g2d ) {
        Painting.paintBackground( g2d, this, Color.WHITE );

        int sizeI  = _srs._nu._sp._vw._d.size( "som.i" );
        int g = 2; // gap
        int c = _h / 5;
        int m = c / sizeI; // model size
        double chance2 = 2.0 * ( 1.0 / _srs._nu._sp._dof.volume() );

        g2d.setColor( Color.RED );
        g2d.drawString( "FF input", g( c,g,1 ),g( c,g,2 )-m );
        g2d.drawString( "SOM models", g( c,g,2 ),g( c,g,2 )-m );
        g2d.drawString( "SOM FF output", g( c,g,3 ),g( c,g,2 )-m );
        g2d.drawString( "Sequence Graph", g( c,g,4 ),g( c,g,2 )-m );
        g2d.drawString( "RSOM models", g( c,g,2 ),g( c,g,5 )+m );
        g2d.drawString( "RSOM FF output", g( c,g,4 ),g( c,g,5 )+m );

        paint2d1(     g2d, g( c,g,2 )-g-m,g( c,g,2 ), m, m, _srs._vi );
        paintGraph(   g2d, g( c,g,4 ),g( c,g,2 ), m,m, g, _srs._sg );
        paintSOM2d1(  g2d, g( c,g,2 ),g( c,g,2 ), m,m, g, _srs._nu._sp );
        paint2d1(     g2d, g( c,g,3 ),g( c,g,2 ), c,c, _srs._nu._sp._vof );
        paint2d1(     g2d, g( c,g,3 ),g( c,g,3 ), c*2,c*2, _srs._nu._tp._vof );
        paintRSOM2d1( g2d, g( c,g,1 ),g( c,g,3 ), m, m, g, _srs._nu._sp, _srs._nu._tp, false, chance2, true );
    }
}
