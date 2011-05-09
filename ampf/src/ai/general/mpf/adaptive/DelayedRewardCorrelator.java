/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.adaptive;

import ai.general.mpf.util.FFQueue;
import ai.general.nn.Schedule;
import ai.general.volumes.Dimensions;
import ai.general.volumes.VolumeMap;

/**
 *
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class DelayedRewardCorrelator extends RewardCorrelator {

    public FFQueue _dff;

    public DelayedRewardCorrelator(
        VolumeMap vm,
        String name,
        Schedule s,
        Dimensions di,
        AdaptiveSignal as,
        int delay ) {
        super( vm, name, s, di, as );

        _dff = new FFQueue( vm, name, s, di, delay-1 );
    }

    @Override public void ff() {
        updateFFQueue();
        updateCorrelation( _dff._vof, 1.0f ); // correlate oldest in queue (most delayed) 
        updatePMF();
    }

    @Override public void fb() {
        super.fb();
        _dff.fb(); // updates the Q
    }

    protected void updateFFQueue() {
        _dff._vif.copy( _vif ); // copy latest to dff
        _dff.ff(); // nothing here.. why bother
    }
}
