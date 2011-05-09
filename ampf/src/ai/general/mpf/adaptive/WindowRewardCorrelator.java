/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.adaptive;

import ai.general.nn.Schedule;
import ai.general.volumes.Dimensions;
import ai.general.volumes.Volume;
import ai.general.volumes.VolumeMap;
import ai.general.util.Maths;

/**
 * Apply reward function over a time window. Kinda like integrated reward over
 * time..
 * 
 * @author davidjr
 */
public class WindowRewardCorrelator extends DelayedRewardCorrelator {

    public int _delay = 0;

    public WindowRewardCorrelator(
        VolumeMap vm,
        String name,
        Schedule s,
        Dimensions di,
        AdaptiveSignal as,
        int window,
        int delay ) {
        super( vm, name, s, di, as, window+delay ); // because 1 is not used
        _delay = delay;
    }

    @Override public void ff() {

        updateFFQueue();

        int o = _dff.oldestIndex();
        int n = _dff.newestIndex();
        int length = _dff._queue.size();// -1;
        int position = 0;
        
        while( true ) { //n != o ) {

            if( position >= _delay ) {
                int relativePosition = position -_delay;
                double distance = distance( relativePosition, length-_delay );
                double weight = updateWeight( distance );
System.out.println( "    correlating index "+n+" @ position "+position+" of "+length+ " having dist="+distance+" weight="+weight );

                Volume v = _dff._queue.get( n );
                updateCorrelation( v, weight );
            }
            else {
System.out.println( "NOT correlating index "+n+" @ position "+position+" of "+length );
            }

            ++position;

            if( n == o ) {
                break;
            }
            
            n = _dff.prev( n );
        }
        
        updatePMF();
    }

//    OLDEST          NEWEST
//    0 1 2 3 4 5 6 7 8
//             M
// S=1           M
// S=-1      M

    public double distance( int position, int length ) {

        // position is 0 when at the newest one (that an be evaluated)
        // position is length-1 when oldest.
        double d = (double)position / (double)length;
        return d;
    }

    public double updateWeight( double distance ) {
        return updateWeightSigmoid( distance );
//        // position starts with OLDEST in the queue.
//        double radius = (double)length * 0.5;
//        double mean = radius + (double)_meanShift;
//
//        double distance = Math.abs( (double)position - mean ); // distance from mean
//               distance /= radius;
//               distance = Maths.clamp1( distance );
//
//        double weight = Maths.logSigmoid1( 1.0 - distance );
//        return weight;
    }

    public double updateWeightSigmoid( double distance ) {
        // position starts with OLDEST in the queue.
        double weight = Maths.logSigmoid1( 1.0 - distance );
        return weight;
    }

}
