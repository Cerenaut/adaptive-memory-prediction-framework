/*
 * For Joe Holmberg
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.nn;

import ai.general.volumes.Dimensions;
import ai.general.volumes.Volume;
import ai.general.volumes.VolumeMap;
import ai.general.util.Parameters;

/**
 * A neural network that can be run in 1 direction only.
 *
 * @author dave
 */
public abstract class FeedForwardNetwork {

    public VolumeMap _vm;
    public String _name;
    public Schedule _s;
    public Parameters _p;

    public Dimensions _dif; // input forwards
    public Dimensions _dof; // output forwards
    public Volume _vif; // matrix input forwards
    public Volume _vof; // matrix output forwards

    public FeedForwardNetwork( VolumeMap vm, String name, Schedule s ) {
        _vm = vm;
        _name = name;
        _s = s;
        _p = new Parameters();
    }

    public abstract void ff();// { // process the network, forwards

    public String volumeName( String suffix ) {
        return new String( _name + "." + suffix );
    }

    public void configureInputForward( Dimensions d ) {
        _dif = d;
        _vif = new Volume( _dif );
        _vm.put( volumeName( Dimensions.DIMENSION_INPUT ), _vif );
    }

    public void configureOutputForward( Dimensions d ) {
        _dof = d;
        _vof = new Volume( _dof );
        _vm.put( volumeName( Dimensions.DIMENSION_OUTPUT ), _vof );
    }

    public void configureInputForward1D( int inputs ) {

        int[] sizes = new int[ 1 ];
              sizes[ 0 ] = inputs;

        Dimensions d = Dimensions.Create( Dimensions.DIMENSION_INPUT, sizes );

        configureInputForward( d );
    }

    public void configureInputForward2D( int inputs1, int inputs2 ) {

        int[] sizes = new int[ 2 ];
              sizes[ 0 ] = inputs1;
              sizes[ 1 ] = inputs2;

        Dimensions d = Dimensions.Create( Dimensions.DIMENSION_INPUT, sizes );

        configureInputForward( d );
    }

    public void configureOutputForward1D( int outputs ) {

        int[] sizes = new int[ 1 ];
              sizes[ 0 ] = outputs;
              
        Dimensions d = Dimensions.Create( Dimensions.DIMENSION_OUTPUT, sizes );

        configureOutputForward( d );
    }

    public void configureOutputForward2D( int outputs1, int outputs2 ) {

        int[] sizes = new int[ 2 ];
              sizes[ 0 ] = outputs1;
              sizes[ 1 ] = outputs2;

        Dimensions d = Dimensions.Create( Dimensions.DIMENSION_OUTPUT, sizes );

        configureOutputForward( d );
    }

}
