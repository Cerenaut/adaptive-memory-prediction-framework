/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.util.ui;

import ai.general.util.AbstractPair;
import ai.general.util.Iterative;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyListener;
import javax.swing.JPanel;

/**
 * Easy way to knock up various demo / debug GUIs. Most of this stuff is not
 * interactive, just a bunch of displays to repaint().
 * 
 * @author dave
 */
public class IterativelyPaintablePanel extends JPanel implements Iterative {

    public Paintable _paintable = null;
    
    public IterativelyPaintablePanel( KeyListener kl ) {
        super();

        if( kl != null ) {
            setFocusable( true );
            addKeyListener( kl );
        }
    }

    @Override public void pre() {}
    @Override public void step() {
        if( _paintable != null ) {
            AbstractPair< Integer, Integer > ap = _paintable.size();

            if( ap != null ) {
                setPreferredSize( new Dimension( ap._first, ap._second ) );
            }
        }
        repaint();
    }
    
    @Override public void post() {}

    public @Override void paintComponent( Graphics g ) {

        super.paintComponent( g );

        if( _paintable == null ) {
            return;
        }

        // Cast Graphics to Graphics2D
        Graphics2D g2d = (Graphics2D)g;

        _paintable.paint( g2d );
    }

}
