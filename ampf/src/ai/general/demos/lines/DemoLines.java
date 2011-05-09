/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.demos.lines;

import ai.general.util.ui.DemoUI;

/**
 *
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class DemoLines {

    public static void run( String title, int hesitation ) {

        Lines somrsom = new Lines();

        LinesPainter srsp = new LinesPainter( somrsom );

        DemoUI d = new DemoUI( title );

        d.iterate( somrsom ); // update the simulation
        d.addPanel( "Spatial Unit", srsp, true );
        d.addDemoKeyListener();
        d.createFrame( 1 );
        d._hesitation = hesitation;
        d.addDemoMenuBar();
        d.start();
    }

}
