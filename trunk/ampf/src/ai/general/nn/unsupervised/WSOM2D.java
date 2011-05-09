/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.nn.unsupervised;

import ai.general.mpf.util.FFQueue;
import ai.general.nn.Schedule;
import ai.general.volumes.Volume;
import ai.general.volumes.VolumeMap;

/**
 * Temporal clustering. "Window" based recurrent SOM.
 * Simulate "OR" logic fn by taking max in each vol in queue.
 * Pros: Nice orthogonal function, easy to design hierarchies cos I know how
 * much temporal compression occurs at each level.
 * Cons: I have to design hierarchies carefully! :/
 *
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class WSOM2D extends SOM2D2 {

    public FFQueue _ffq;
    public Volume _vd; // d see Miller 2006 RSOM/HQSOM
    public Volume _vsse2; // non-recursive SumSqError

    public WSOM2D( VolumeMap vm, String name, Schedule s, int inputs, int size, int window ) {
        super( vm, name, s, inputs, size );

        _vd = new Volume( _dw ); // 3d
        _vd.set( 1.0f );

        _vsse2 = new Volume( _dof ); // 2d, error

        _ffq = new FFQueue( vm, name, s, _dif, window-1 );

        _bias = false;
    }

    @Override public void ff() {
        ffMax();
//        ffLinear();
    }

    public void ffMax() {
        _ffq._vif.copy( _vif ); // copy latest to dff
        _ffq.ff(); // nothing here.. why bother

        _vif.set( 0.0f );

        for( Volume v : _ffq._queue ) {
            _vif.max( v );
        }

        _vif.scaleRange( 0.0f, 1.0f );

        super.ff();

        _ffq.fb(); // update Q
    }

    public void ffLinear() {

        _ffq._vif.copy( _vif ); // copy latest to dff
        _ffq.ff(); // nothing here.. why bother

        _vif.set( 0.0f );

        int o = _ffq.oldestIndex();
        int n = _ffq.newestIndex();
        int length = _ffq._queue.size() +1;// -1;
        int position = 0;
        double reduction = 1.0 / length; // e.g. 1/3 = 0.333-, 1,0.666-,0.333,0
        double weight = 1.0;

        Volume temp = new Volume( _vif._d );

        while( true ) { //n != o ) {

            Volume v = _ffq._queue.get( n );

            temp.copy( v );
            temp.mul( (float)weight );

//System.out.println( "Temp index "+n+" @ position "+position+" of "+length+ " weight="+weight );
            ++position;

            _vif.add( temp );

            if( n == o ) {
                break;
            }

            weight -= reduction;

            n = _ffq.prev( n );
        }

        _vif.scaleRange( 0.0f, 1.0f );

        super.ff();

        _ffq.fb(); // update Q
    }
    
}
