/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.mm;

import ai.general.volumes.Dimensions;
import ai.general.volumes.Volume;

/**
 * Generates a random input matrix instead of something from a sequence, but
 * pretends to be a sequence..
 * 
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class RandomInputSequence extends InputSequence {

    public RandomInputSequence( Dimensions d ) {
        super( d, 1 );
    }

    @Override public Volume next() {
        Volume v = super.next();

        if( v != null ) {
            v.randomize();
        }
        
        return v;
    }
}
