/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.util.ui;

import ai.general.volumes.VolumeMap;
import ai.general.util.AbstractPair;
import ai.general.util.Iterative;
import ai.general.util.IterativeThread;
import ai.general.util.RandomSingleton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 * Wrapper class to assemble an application with GUI easily.
 * 
 * @author dave
 */
public class DemoUI implements ActionListener, KeyListener {

    public String _title = "SOM-based Adaptive Memory Prediction Framework Demos";
    public String _filePrefix = "ai.general.";
    public KeyListener _kl;
    public long _seed = 28934739;
    public int _hesitation = 80;
    public IterativeThread _it = new IterativeThread();
    public ArrayList< AbstractPair< String, JPanel > > _panels = new ArrayList< AbstractPair< String, JPanel > >();
    public Frame _f;
    public JMenuBar _menuBar;
    public VolumeMap _vm;

    public DemoUI( String title ) {
        _title = title;
    }

    public void start() {
        _it._hesitation = _hesitation;//80;
        _it.run();
    }

    public void stop() {
        _it.stopWait();
    }

    public void pause() {
        _it.suspendWait();
    }

    public void resume() {
        _it.resume();
    }

    public void faster() {
        _it._hesitation /= 2;
    }

    public void slower() {
        _it._hesitation *= 2;
    }

    public void step() {
        _it.singleStep();
    }

    public void addFileMenuBar() {

        ArrayList< String > menuItemLabels = new ArrayList< String >();

        menuItemLabels.add( "Open" );
        menuItemLabels.add( "Save" );

        addMenuBar( "File", menuItemLabels, this );
    }

    public void addDemoMenuBar() {

        ArrayList< String > menuItemLabels = new ArrayList< String >();

        menuItemLabels.add( "Pause" );
        menuItemLabels.add( "Resume" );
        menuItemLabels.add( "Step" );
        menuItemLabels.add( "Faster" );
        menuItemLabels.add( "Slower" );

        addMenuBar( "Demo", menuItemLabels, this );
    }

    public void addMenuBar( String menuLabel, AbstractCollection< String > menuItemLabels, ActionListener al ) {

        if( _menuBar == null ) {
            _menuBar = new JMenuBar();
            _f.setJMenuBar( _menuBar );
        }

        JMenu menu = new JMenu( menuLabel );
        _menuBar.add( menu );

        for( String menuItemLabel : menuItemLabels ) {
            JMenuItem menuItem = new JMenuItem( menuItemLabel );

            menuItem.setActionCommand( menuItemLabel );
            menuItem.addActionListener( al );

            menu.add( menuItem );
        }
    }

    @Override public void actionPerformed(ActionEvent e) {
             if( e.getActionCommand().equals( "Open" ) ) {
            open();
        } 
        else if( e.getActionCommand().equals( "Save" ) ) {
            save();
        }
        else if( e.getActionCommand().equals( "Pause" ) ) {
            pause();
        }
        else if( e.getActionCommand().equals( "Resume" ) ) {
            resume();
        }
        else if( e.getActionCommand().equals( "Faster" ) ) {
            faster();
        }
        else if( e.getActionCommand().equals( "Slower" ) ) {
            slower();
        }
        else if( e.getActionCommand().equals( "Step" ) ) {
            step();
        }
        else {
            System.err.println( "ERROR: Action not understood." );
        }
    }

    public void open() {
        if( _vm == null ) return;

        _it.suspendWait();

        try {
            _vm.read( _filePrefix );
        }
        catch( IOException ioe ) {
            System.err.println( "ERROR: Couldn't read from volume file-set with prefix "+_filePrefix );
        }

        _it.resume();
    }

    public void save() {
        if( _vm == null ) return;

        _it.suspendWait();

        try {
            _vm.write( _filePrefix );
        }
        catch( IOException ioe ) {
            System.err.println( "ERROR: Couldn't write to volume file-set with prefix "+_filePrefix );
        }

        _it.resume();
    }

    public void addPanel( String label, Paintable p, boolean addKeyListener ) {
        KeyListener kl = null;
        if( addKeyListener ) kl = _kl;
        IterativelyPaintablePanel ipp = new IterativelyPaintablePanel( kl );
        ipp._paintable = p;
        AbstractPair< String, JPanel > ap = new AbstractPair< String, JPanel >( label, ipp );
        _panels.add( ap ); // add to gui
        _it.iterate( ipp ); // paint every time
    }

    public void addPanel( String label, JPanel p, boolean addKeyListener ) {
        KeyListener kl = null;
        if( addKeyListener ) kl = _kl;

        AbstractPair< String, JPanel > ap = new AbstractPair< String, JPanel >( label, p );
        _panels.add( ap ); // add to gui
//        _it.iterate( p ); // paint every time
    }

    public void iterate( Iterative i ) {
        _it.iterate( i );
    }

    public void createFrame( int columns ) {
        if( _panels.size() == 0 ) {
            System.err.println( "There aren't any panels to show on the GUI." );
            System.exit( -1 );
        }
        _f = new Frame( _title, _panels, columns, _kl );
    }

    public void seed() {
        RandomSingleton.seed( _seed );
    }

    public void addDemoKeyListener() {
        _kl = this;
    }

    // key listener

    @Override public void keyPressed ( KeyEvent ke )
    {
        if (ke.getKeyChar() == 'f') {
            faster();
        }
        else if (ke.getKeyChar() == 'l') {
            slower();
        }
        else if (ke.getKeyChar() == 'p') {
            pause();
        }
        else if (ke.getKeyChar() == 'r') {
            resume();
        }        
        else if (ke.getKeyChar() == 's') {
            step();
        }
    }

    @Override public void keyReleased( KeyEvent ke ) {}
    @Override public void keyTyped( KeyEvent ek ) {}
}
