/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.util;

import java.util.Random;

/**
 * Globally accessible object for ensuring all randoms come from the same seed.
 * ie to make program deterministic and results reproducible.
 *
 * @author dave
 */
public class RandomSingleton {

    protected static Random _instance = null;

    public static void seed( long seed ) {
        if( _instance != null ) {
            System.err.println( "It's too late to seed the random number generator!" );
            System.exit( -1 );
        }

        _instance = new Random( seed );
    }

    public static double random() {
        if( _instance == null ) {
            _instance = new Random(); // seeded with time
        }
        
        return _instance.nextDouble();
    }
}
