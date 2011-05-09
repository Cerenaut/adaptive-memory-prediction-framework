/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.util;

import ai.general.mpf.Bidirectional;
import java.util.ArrayList;

/**
 * A traversal of the hierarchy. The reverse order is suitable for the fb pass.
 *
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class Traversal implements Bidirectional {

    public ArrayList< Bidirectional > _sequence = new ArrayList< Bidirectional >();

    @Override public void ff() {
//int n = 0;
        for( Bidirectional bd : _sequence ) {
            bd.ff();
//            ++n;
//System.out.println( "ff "+n );
        }
    }

    @Override public void fb() {
        int index = _sequence.size() -1;

        while( index >= 0 ) {

            Bidirectional bd = _sequence.get( index );
            bd.fb();
            
            --index;
        }
    }

}
