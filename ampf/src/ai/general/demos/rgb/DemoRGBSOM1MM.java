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
public class DemoRGBSOM1MM {

    public static void run( String title, int hesitation ) {

        RGBSOM1MM rgbsom1mm = new RGBSOM1MM();// w, h, size, errorPeriod );

        DemoUI d = new DemoUI( title );

        d.iterate( rgbsom1mm ); // update the simulation
        d.addPanel( "SOM / 1MM state:", rgbsom1mm, false );
        d.addDemoKeyListener();
        d.createFrame( 1 );
        d._hesitation = hesitation;
        d.addDemoMenuBar();
        d.start();
    }

}
