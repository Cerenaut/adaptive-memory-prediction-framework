/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.demos.rps;

import ai.general.util.ui.DemoUI;

/**
 *
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class DemoRockPaperScissors {

    public static void run( String title ) {

        boolean variableOrder = false;//true;
        Strategy s = null;
        
//        if( variableOrder ) { Not available in this demo apologies!
//            s = new DualStrategy();
//        }
//        else {
        s = new FixedStrategy();
//        }
//        s = new PersistentStrategy();

        Game g = new Game( s, variableOrder  );
        RPSPainter rpsp = new RPSPainter( g );

        DemoUI d = new DemoUI( title );

        d.iterate( g ); // update the simulation
        d.addDemoKeyListener();
        d.addPanel( "Adaptive Neocortical Unit", rpsp, true );
        d.createFrame( 1 );
        d._hesitation = 50;
        d.addDemoMenuBar();
        d.start();
    }

}
