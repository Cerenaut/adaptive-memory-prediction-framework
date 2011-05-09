/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.demos.words;


import ai.general.util.ui.DemoUI;

/**
 *
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class DemoWords {

    public static void run( String title, int hesitation, boolean variableOrder ) {

        Words w = new Words( variableOrder );

        WordsPainter vp = new WordsPainter( w );

        DemoUI d = new DemoUI( title );

        d.iterate( w ); // update the simulation
        d.addDemoKeyListener();
        d.addPanel( "Words", vp, true );
        d.createFrame( 1 );
        d._hesitation = hesitation;
        d.addDemoMenuBar();
        d.start();
    }


}
