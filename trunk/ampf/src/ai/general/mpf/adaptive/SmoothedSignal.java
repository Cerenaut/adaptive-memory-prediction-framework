/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.adaptive;

import ai.general.util.MovingWindow;

/**
 *
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class SmoothedSignal implements AdaptiveSignal {

    public AdaptiveSignal _as;
    MovingWindow _rewards;
    MovingWindow _means;

    public boolean _useDeltaMean = false;
    
    public SmoothedSignal( AdaptiveSignal as, int period ) {
        _as = as;
        _rewards = new MovingWindow( period );
        _means   = new MovingWindow( period );
    }
    
    public void update() {
        double r = _as.reward();
        _rewards.update( (float)r );
        double mean = _rewards.mean();
        _means.update( (float)mean ); // new mean added
    }

    @Override public double reward() {
        float value2 = _means.newest();
        float value1 = _means.oldest();

        if( _useDeltaMean ) {
            double reward = value2 - value1;
            return reward;
        }

        double reward = value2;
        return reward;
    }

    @Override public boolean negative() {
        return( _as.negative() ) || ( _useDeltaMean );
    }

}
