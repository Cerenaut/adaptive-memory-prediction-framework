/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.demos.rgb;

import ai.general.util.ui.DemoUI;

/**
 *
 * @author dave
 */
public class DemoRGBSOM {

    public static void run( String title, String desc, int hesitation ) {

        RGBSOM somrgb = new RGBSOM();

        DemoUI d = new DemoUI( title );

        d.iterate( somrgb ); // update the simulation
        d.addPanel( "RGB->SOM", somrgb, false );
        d.addDemoKeyListener();
        d.createFrame( 1 );
        d._hesitation = hesitation;
        d.addDemoMenuBar();
        d.start();
    }

}
