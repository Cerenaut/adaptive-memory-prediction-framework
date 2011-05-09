/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.nn;

import ai.general.util.Callback;

/**
 * A cooling or learning schedule which allows long-term parameters to be set
 * based on elapsed time.
 * 
 * @author dave
 */
public class Schedule implements Callback {

    public int _t = 0;
    public int _T = 25000;

    public static Schedule Create( int T ) {
        return new Schedule( T );
    }
    
    public Schedule( int T ) {
        _T = T;
    }

    public void reset() {
        _t = 0;
    }
    
    @Override public void call() {
//        if( _t < _T ) {
            ++_t;
//        }
//        System.out.println( "s="+_t );
    }

    public double elapsed() {
        double t = Math.min( (double)_t, (double)_T );
        double r = t / (double)_T;
        return r;
    }
}
