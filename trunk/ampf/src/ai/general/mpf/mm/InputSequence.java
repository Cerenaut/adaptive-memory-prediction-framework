/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.mm;

import ai.general.volumes.Dimensions;
import ai.general.volumes.Volume;
import java.util.ArrayList;

/**
 * An ordered list of input matrices to form a predictable sequence.
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class InputSequence {

    public ArrayList< Volume > _inputs = new ArrayList< Volume >();
    public int _index = 0;
    
    public InputSequence( Dimensions d, int length ) {
        for( int i = 0; i < length; ++i ) {
            Volume v = new Volume( d );
            _inputs.add( v );
        }
    }

    public int length() {
        return _inputs.size();
    }

    public Volume next() {
        Volume v = get( _index );

        _index = next( _index );

        return v;
    }

    public int next( int index ) {

        // 0 1
        ++index;

        int length = _inputs.size();

        if( index > length ) {
            index = 0;
        }

        return index;
    }

    public Volume get( int index ) {
        
        if( index >= _inputs.size() ) {
            return null;
        }

        return _inputs.get( index );
    }

}
