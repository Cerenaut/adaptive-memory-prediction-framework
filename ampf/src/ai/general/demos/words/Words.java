/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.demos.words;

import ai.general.mpf.NeocorticalUnit;
import ai.general.nn.Schedule;
import ai.general.volumes.Volume;
import ai.general.volumes.VolumeMap;
import ai.general.util.Iterative;
import ai.general.util.MovingWindow;
import ai.general.util.Parameters;

/**
 *
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class Words implements Iterative {

    public WordGraph _w = new WordGraph();
    public VolumeMap _vm = new VolumeMap();
    public Schedule _s1;
    public Schedule _s2;
    public NeocorticalUnit _nu;
    public MovingWindow _error = new MovingWindow();

    public Words( boolean variableOrder ) {

        _error.resize( 1000 );//500 );
        
        _s1 = Schedule.Create( 1000 );
        _s2 = Schedule.Create( 2000 );

        // RSOM Alpha chart
        // Alpha is the weight of the new value.
        // This chart shows a value decreasing in weight over time:
        // * = reached 10% of original weight, ie significant for these iters.
        //       Sig 1   2    3     4     5     6     7     8     9     10
        // ---------------------------------------------------------------------
        // a=0.9  2  0.9 0.09*0.009 0.0
        // a=0.8  3  0.8 0.16 0.032*0.006 0.001 0.0
        // a=0.7  3  0.7 0.21 0.063*0.018 0.005 0.001 0.0
        // a=0.6  4  0.6 0.24 0.09  0.03 *0.015 0.006 0.002 0.0
        // a=0.5  4  0.5 0.25 0.125 0.062*0.031 0.015 0.007 0.003 0.001 0.0
        // a=0.4  5  0.4 0.24 0.14  0.08  0.05 *0.03  0.018 0.011 0.006 0.004
        // a=0.3  7  0.3 0.21 0.14  0.1   0.07  0.05  0.03*
        // a=0.2 10  0.2 0.16 0.12  0.10  0.08  0.06  0.05  0.04  0.03  0.02*
        // a=0.1 16  0.1 0.09 0.08  0.07  0.06  0.05  0.05  0.047 0.04  0.03 0.03 0.03 0.02 0.02 0.02 0.02 0.01
        // ---------------------------------------------------------------------
        int sInputs = _w.next().volume();
        int sSize = 6;
        int tSize = 3;
        int tWindow = 3;

        _nu = new NeocorticalUnit( _vm, "nu", _s1, _s2, sInputs, sSize, tSize, tWindow, variableOrder );
        _nu._strictlyOrthogonal = true;
        
        Parameters punit = new Parameters();
        punit.set( "fb-noise-magnitude-initial", 0.0 );
        punit.set( "fb-noise-magnitude-final", 0.0 );
        punit.set( "fb-higher-mass", 0.0 );  // was: 0.0 +1.0
        if( variableOrder ) {
            punit.set( "fb-uniform-mass", 0.05 );//2.5 ); // was: 3.5 -1.0 as added 1.0 prior to mm mul
        }
        else {
            punit.set( "fb-uniform-mass", 2.5 );//2.5 ); // was: 3.5 -1.0 as added 1.0 prior to mm mul
        }
        punit.set( "fb-iterations", 1.0 );

        Parameters psom = new Parameters();
        psom.set( "som-sigma-activation", 1.7 );
        psom.set( "som-sigma-initial", 18.0 );
        psom.set( "som-sigma-final", 0.1 );//0.5 0.4 0.3 );
        psom.set( "som-roulette-power", 12.0 ); // default

        Parameters prsom = new Parameters();
        prsom.set( "som-sigma-activation", 1.7 ); // iff online
        prsom.set( "som-sigma-initial", 12.0 );
        prsom.set( "som-sigma-final", 0.1 );
        prsom.set( "som-roulette-power", 12.0 ); // default

        Parameters pmm = new Parameters();
        pmm.set( "final-learning-rate", 0.05 );

//        if( variableOrder ) { ignored if not!
        pmm.set( "inhibition-sigma", 0.425 ); // 1.7 * 0.25 = 0.425
        pmm.set( "inhibition-delta-sigma", 0.5 ); // 1.5 0.5
//        }
        
        _nu.configure( punit, psom, prsom, pmm );
    }
    
    @Override public void pre() {}
    @Override public void post() {}
    @Override public void step() {

        _s1.call();
        _s2.call();

//        System.out.println( "T="+_s1._t+" elapsed="+_s1.elapsed() );
//        System.out.println( "T="+_s2._t+" elapsed="+_s2.elapsed() );

        Volume v = _w.next();

        // compare the FB prediction to the input next time:
        double error = _nu._vib.absDiff( v );
        double volume = _nu._vib.volume();
        error /= volume;

        _error.update( (float)error );
//        System.out.println( "Error(m)="+_error.mean()+" Error(1)="+error ); // @ 1400 mean=0.23 vomm
        System.out.println( "T="+_s1._t+" Error(n)="+_error.mean()+" Error(1)="+error );

        _nu._vif.copy( v );
        _nu.ff();
        _nu._vob.uniform();        // reflect at top or uniform?
        _nu.fb();

    }

}
