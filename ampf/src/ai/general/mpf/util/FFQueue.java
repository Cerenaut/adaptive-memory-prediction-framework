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
import java.util.ArrayList;

/**
 * I/O dimensions are the same.
 * FF data is delayed for "delay" iterations.
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class FFQueue extends BidirectionalNetwork {

    public ArrayList< Volume > _queue = new ArrayList< Volume >();
    protected int _i = 0;
    protected int _o = 0;
    
    public FFQueue( VolumeMap vm, String name, Schedule s, Dimensions d, int delay ) {
        super( vm, name, s );

        _dif = d;
        _dof = d; // same!

        _vib = new Volume( d );
        _vob = _vib; // copies straight through, no delay

        int ffVolumes = delay +1;

        for( int i = 0; i < ffVolumes; ++i ) {
            Volume v = new Volume( _dif );
                   v.uniform();
            _queue.add( v );
        }

        _i = 0;
        _o = next( _i ); // next to be overwritten
        _vif = _queue.get( _i );
        _vof = _queue.get( _o );
    }

//    0 1 2      0 1 2
//    ----------------
//    A          i o
//    A B          i o
//    A B C      o   i
//    D B C      i o
//    D E C        i o
//order of calls are:
//relay1.ff copies latest into _vif
//this.ff does nothing.
//relay2.ff copies _vof somewhere downstream

    @Override public void ff() {
        // Nothing; _vof is available, _vif has been stored.
    }

    @Override public void fb() {
        // Nothing for vob/vib; writing into _vob writes into _vib.
        updateQueue();
    }

    public int next( int index ) {
        ++index;
        index %= _queue.size();
        return index;
    }

    public int prev( int index ) {
        --index;
        if( index < 0 ) {
            index = _queue.size() -1;
        }
        return index;
    }
    
    public int newestIndex() {
        return _i;
    }

    public int oldestIndex() {
        return _o;
    }

    protected void updateQueue() {
        // Advance the indices:
        _i = next( _i );
        _o = next( _o ); // next to be overwritten
        _vif = _queue.get( _i );
        _vof = _queue.get( _o );
//System.out.println( "i="+_i+" o="+_o );
//System.out.println( "@i="+_vif+" @o="+_vof );
    }
}
