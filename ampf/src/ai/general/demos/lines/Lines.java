/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.demos.lines;

import ai.general.mpf.NeocorticalUnit;
import ai.general.mpf.mm.SequenceGraph;
import ai.general.mpf.mm.InputSequence;
import ai.general.nn.Schedule;
import ai.general.volumes.Volume;
import ai.general.volumes.VolumeMap;
import ai.general.util.Iterative;
import ai.general.util.Maths;
import ai.general.util.RandomSingleton;

/**
 * Temporal pooling problem from Miller et al, 2006
 * Soln formed by ~5000 iters
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class Lines implements Iterative {

    public SequenceGraph _sg;
    public Volume _vi;
    public double _noiseScale = 0.1; // small amount of noise

    public VolumeMap _vm = new VolumeMap();
    public Schedule _ss;
    public Schedule _st;
    public NeocorticalUnit _nu;

    public Lines() {

        int schedule1 =  500;
        int schedule2 = 5000;
        int sInputs = 9;
        int sSize = 5;
        int tSize = 2; // 5x5=25 units, about as small as possible before it breaks down

        double tAlpha = 0.08;
        boolean variableOrder = false;
        _ss = Schedule.Create( schedule1 );
        _st = Schedule.Create( schedule2 );

        _nu = new NeocorticalUnit( _vm, "nu", _ss, _st, sInputs, sSize, tSize, tAlpha, variableOrder );
        _vi = new Volume( _nu._dif );

        createSequences();
    }

    protected void createSequences() {

        _sg = new SequenceGraph();

        int length1 = 1;
        int length2 = 3;
        int length3 = 3;

        InputSequence is1 = new InputSequence( _nu._dif, length1 ); // blank
        InputSequence is2 = new InputSequence( _nu._dif, length2 ); // vertical line moving
        InputSequence is3 = new InputSequence( _nu._dif, length3 ); // horz line moving

        _sg.addSequence( is1, 0.8 ); // mostly blanks
        _sg.addSequence( is2, 0.1 ); // equal chance of either
        _sg.addSequence( is3, 0.1 ); // type of moving sequence

        Volume v1_0 = is1.get( 0 );
               v1_0.set( 0.0f ); // all blank

        //            v2_0      v2_1     v2_2
        // 0 1 2      * - -     - * -    - - *
        // 3 4 5      * - -     - * -    - - *
        // 6 7 8      * - -     - * -    - - *
        Volume v2_0 = is2.get( 0 );
               v2_0.set( 0.0f ); 
               v2_0._model[ 0 ] = 1.0f;
               v2_0._model[ 3 ] = 1.0f;
               v2_0._model[ 6 ] = 1.0f;

        Volume v2_1 = is2.get( 1 );
               v2_1.set( 0.0f );
               v2_1._model[ 1 ] = 1.0f;
               v2_1._model[ 4 ] = 1.0f;
               v2_1._model[ 7 ] = 1.0f;

        Volume v2_2 = is2.get( 2 );
               v2_2.set( 0.0f );
               v2_2._model[ 2 ] = 1.0f;
               v2_2._model[ 5 ] = 1.0f;
               v2_2._model[ 8 ] = 1.0f;


        //            v3_0      v3_1     v3_2
        // 0 1 2      * * *     - - -    - - -
        // 3 4 5      - - -     * * *    - - -
        // 6 7 8      - - -     - - -    * * *
        Volume v3_0 = is3.get( 0 );
               v3_0.set( 0.0f );
               v3_0._model[ 0 ] = 1.0f;
               v3_0._model[ 1 ] = 1.0f;
               v3_0._model[ 2 ] = 1.0f;

        Volume v3_1 = is3.get( 1 );
               v3_1.set( 0.0f );
               v3_1._model[ 3 ] = 1.0f;
               v3_1._model[ 4 ] = 1.0f;
               v3_1._model[ 5 ] = 1.0f;

        Volume v3_2 = is3.get( 2 );
               v3_2.set( 0.0f );
               v3_2._model[ 6 ] = 1.0f;
               v3_2._model[ 7 ] = 1.0f;
               v3_2._model[ 8 ] = 1.0f;
    }

    @Override public void pre() {}
    @Override public void post() {}
    @Override public void step() {

        _ss.call();
        _st.call();

        System.out.println( "t="+_ss._t );
        
        updateInput();

        _nu._vif.copy( _vi );
        _nu.ff();
    }

    public void updateInput() {
        Volume v = _sg.next();

        _vi.copy( v );

        // add noise:
        int volume = _vi.volume();
        int offset = 0;

        while( offset < volume ) {
            
            double r = RandomSingleton.random() - 0.5;
                   r *= _noiseScale;

            double value = _vi._model[ offset ];
                   value += r;
                   value = Maths.clamp1( value );

            _vi._model[ offset ] = (float)value;

            ++offset;
        }
    }

}
