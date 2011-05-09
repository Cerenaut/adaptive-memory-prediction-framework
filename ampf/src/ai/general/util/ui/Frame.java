/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.util.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import ai.general.util.AbstractPair;
import javax.swing.JLabel;

/**
 * Frame for the UI.
 * @author dave
 */
public class Frame extends JFrame {

    public Frame( String title, String subTitle, JPanel panel, KeyListener kl ) {
        super( title );
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        setSize( toolkit.getScreenSize() );
        setDefaultCloseOperation( EXIT_ON_CLOSE );

        if( kl != null ) addKeyListener( kl );

        ArrayList< AbstractPair< String, JPanel > > panels = new ArrayList< AbstractPair< String, JPanel > >();

        panels.add( new AbstractPair< String, JPanel >( subTitle, panel ) );

        createContentPane( panels, 1, kl );
    }
    
    public Frame( String title, ArrayList< AbstractPair< String, JPanel > > panels, int columns, KeyListener kl ) {
        super( title );
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        setSize( toolkit.getScreenSize() );
        setDefaultCloseOperation( EXIT_ON_CLOSE );

        if( kl != null ) addKeyListener( kl );

        createContentPane( panels, columns, kl );
    }

    protected void createContentPane( ArrayList< AbstractPair< String, JPanel > > panels, int columns, KeyListener kl ) {

        Toolkit toolkit = Toolkit.getDefaultToolkit();

        // Make a grid for the main view:
        int nbrPanels = panels.size();
        int gridW = columns;
        int gridH = nbrPanels / gridW;
        if( (nbrPanels % gridW) > 0 ) ++gridH; // will have some blanks on last row.

//System.out.println( "gridW= "+gridW+ " gridH ="+gridH );
        Dimension d = toolkit.getScreenSize();
        d.width  /= gridW; // ie width of each element
        d.height /= gridH; // ie width of each element

        GridLayout gridLayout = new GridLayout( gridH, gridW );
        JPanel gridPanel = new JPanel();
               gridPanel.setLayout( gridLayout );

        for( AbstractPair< String, JPanel > p : panels ) {

            String label = p._first;
            JPanel panel = p._second;

            GridLayout contentLayout = new ElasticGridLayout( 2, 1 ); // h=2, w=1

            JPanel contentPanel = new JPanel();
                   contentPanel.setLayout( contentLayout );

            JLabel l = new JLabel( label );
            JScrollPane sp = new JScrollPane( panel );
                        sp.setPreferredSize( d );

            contentPanel.add( l );
            contentPanel.add( sp );
               gridPanel.add( contentPanel );
        }

        setContentPane( gridPanel );

        pack();
        setVisible( true );
    }

    public static void setNativeLookAndFeel() {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        } catch( Exception e ) {
            System.out.println( "Error setting native LAF: " + e );
        }
    }

}
