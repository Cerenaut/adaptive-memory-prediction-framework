/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.util;

import ai.general.mpf.BidirectionalNetwork;
import ai.general.nn.Schedule;
import ai.general.volumes.Coordinate;
import ai.general.volumes.VolumeMap;
import ai.general.util.Maths;

/**
 * We want continuous variables, but computer control systems are typically
 * discrete. Get around this problem by making the hierarchy manipulate the
 * PROBABILITY of each discrete alternative.
 * 
 * @author davidjr
 */
public class DiscreteActuator extends BidirectionalNetwork {

    protected int _discreteStates = 0;
    protected int _selectedState = 0;

    public DiscreteActuator( VolumeMap vm, String name, Schedule s, int discreteStates ) {
        super( vm, name, s );

        _discreteStates = discreteStates;

        configureInputForward1D( _discreteStates );
        configureOutputForward( _dif );
        configureFeedback();

        _vif.randomize();
        _vof.randomize();
        _vib.randomize();
        _vob.randomize();
    }

    public void setActuation( int n ) {
        _selectedState = n;
    }

    @Override public void ff() {
        _vif.set( 0.0f );
        _vif._model[ _selectedState ] = 1.0f;
        _vof.copy( _vif );
    }

    @Override public void fb() {
        _vib.copy( _vob ); // each value may be up to unit scale
        _vib.scaleVolume( 1.0f ); // now sum = 1
        Coordinate c = _vib.roulette(); // select proportional to each value
        _selectedState = c.offset();
    }

    public int actuation() {
        return _selectedState;
    }

    public int unit2state( double unit ) {
// e.g.
//        2 := 1        1   * (3) = 2
//                      0.8 * (3) = 2
//                      0.6 *  3  = 1
//        1 := 0.5      0.5 * (3) = 1
//                      0.4 *  3  = 1
//                      0.2 *  3  = 0
//        0 := 0        0   * (3) = 0
        unit = Maths.clamp1( unit );
        // 1 := 2 0.3*3=0.1
//        shouldnt it be 0,0.5,1 rather than 0,0.3,0.6?
        int commands = ( _discreteStates );
        double scaled = unit * (double)commands;
        int command = (int)scaled; // rounds down
        if( command == commands ) command -= 1;
        return command;
    }

    public double state2unit( int command ) {
// e.g. 
//        2 := 1        2/(3-1=2)=1
//        1 := 0.5      1/(3-1=2)=0.5
//        0 := 0        0/(3-1=2)=0
        double unit = (float)command / (float)( _discreteStates -1 );
        unit = Maths.clamp1( unit );
        return unit;
    }    
}
