/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.util;

import ai.general.mpf.BidirectionalNetwork;
import ai.general.nn.Schedule;
import ai.general.volumes.Dimensions;
import ai.general.volumes.Volume;
import ai.general.volumes.VolumeMap;

/**
 * Make the FF data become the FB data.
 * 
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class ReflectiveNetwork extends BidirectionalNetwork {

    public ReflectiveNetwork( VolumeMap vm, String name, Schedule s, Dimensions di ) {
        super( vm, name, s );
        _dif = di;
        _vif = new Volume( _dif );
        _vib = _vif;
    }

    @Override public void ff() {}
    @Override public void fb() {}
}
