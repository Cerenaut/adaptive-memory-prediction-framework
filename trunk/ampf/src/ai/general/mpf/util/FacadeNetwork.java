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
 * Facade meaning fake-front; this is a way of presenting data computed else-
 * -where to a hierarchy without stressing about the other interfaces..
 * 
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class FacadeNetwork extends BidirectionalNetwork {

    public FacadeNetwork( VolumeMap vm, String name, Schedule s, Dimensions dif, Dimensions dof ) {
        super( vm, name, s );
        _dif = dif;
        _dof = dof;
        _vif = new Volume( _dif );
        _vib = new Volume( _dif );
        _vof = new Volume( _dof );
        _vob = new Volume( _dof );
    }

    public FacadeNetwork( VolumeMap vm, String name, Schedule s, Dimensions dof ) {
        super( vm, name, s );
        _dof = dof;
        _dif = dof;
        _vof = new Volume( _dof );
        _vob = new Volume( _dof );
        _vif = _vof;
        _vib = _vob;
    }

    @Override public void ff() {}
    @Override public void fb() {}

}
