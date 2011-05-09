/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.adaptive;

import ai.general.mpf.BidirectionalNetwork;
import ai.general.mpf.mm.FirstOrderMM;
import ai.general.nn.Schedule;
import ai.general.volumes.Dimensions;
import ai.general.volumes.Volume;
import ai.general.volumes.VolumeMap;

/**
 * WARNING highly rough and experimental code to investigate doing everything on
 * one pass through the reward correlator - ie don't need FF and FB passes to
 * relay via the correlator. This for the event that biologically, only goes
 * thru Thalamus in one direction.
 *
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class RelayRewardCorrelator extends BidirectionalNetwork {

    public FirstOrderMM _1mm;
    public WindowRewardCorrelator _rc;

    public boolean _correlateTransitions = false;
Volume _vcp;
Volume _vt;
Volume _vb;

    public RelayRewardCorrelator(
        VolumeMap vm,
        String name,
        Schedule s,
        Dimensions d,
        AdaptiveSignal as,
        int window,
        int delay ) {
        super( vm, name, s );
        _vif = new Volume( d );
        _vof = _vif; // pass through
        _vib = new Volume( d );
        _vob = new Volume( d );
        _vob.uniform(); // in case nothing attached

        int volume = _vof.volume() * _vof.volume();
        Dimensions dw = new Dimensions( 1 );
        dw.configure( 0, volume, Dimensions.TYPE_CARTESIAN, "w" );
        _vt = new Volume( dw );
        _vb = new Volume( d );

        _1mm = new FirstOrderMM( vm, name+".mm", s, d );

if( _correlateTransitions ) {
        _rc = new WindowRewardCorrelator( vm, name+".rc", s, dw, as, window, delay );
}
else {
        _rc = new WindowRewardCorrelator( vm, name+".rc", s, d, as, window, delay );
}
//        _rc = new WindowRewardCorrelator( vm, name+".rc", s, _1mm._dw, as, window, delay );
//        _rc = new WindowRewardCorrelator( vm, name+".rc", s, d, as, window, delay );
    }

    @Override public void ff() {
_vif.check();
        _1mm._vif.copy( _vif );
        _1mm.ff(); // learn to predict

        _rc.ff(); // now eval what I did previously
        _rc.fb();
    }

    @Override public void fb() {
//
//if ff state == x
//and fb state == y
// correlate ( ff=x & fb=y ) for-each x,y against reward ie what I sensed, against what I did
//
//try iterating more per turn
//        needs an integrating actuator
//try correlating ff state with fb state transitions against reward?
//    also modify so that fb has no influence when no good ideas?
// state-transition correlation:
//        _1mm.conditional( _vib, _vcp );
//        _rc._vif.copy( _vcp );
//        _rc.ff(); // don't eval _vib yet - wait 1 iter
//        _rc.fb(); // evaluating the impact of what I did before
//reformulate fb from rc. - should be able to add as many vols as I like without diluting
// ADAPTIVE BIAS:
//        _1mm._vb = _rc._vib; //DISABLED

//        next - combine multiples of the same system; how to combine output?
//            - higher res, smaller scale
//            - many iters per game iter (integrating output)
//            - form sequences over time
//            - add RC for sequences
//        _1mm._vw.mul( _rc._vib );
//        _1mm.normalize();
// state-correlation:
//        _rc._vif.copy( _vib ); // this is the final PMF
//        _rc.ff(); // don't eval _vib yet - wait 1 iter
//        _rc.fb(); // evaluating the impact of what I did before

//        _1mm._vif.copy( _vob );
//        _1mm.conditional( _vcp ); // ie what am I

//        Volume v1 = _1mm._vof; // prediction here
//        Volume v2 = _vob; // prediction from above
//        Volume v3 = _vb;//_rc._vib; // bias
if( _vob != null ) _vob.check();
_1mm._vof.check();
_rc._vib.check();
_vb.check();
_vt.check();
if( _correlateTransitions ) {
        updateConditional( _vif, _rc._vib, _vb );
}

// new
        _vib.copy( _1mm._vof );
        if( _vob != null ) _vib.mul( _vob );
if( _correlateTransitions ) {
        _vib.mul ( _vb );
}
else {
        _vib.mul( _rc._vib );
}
        _vib.scaleVolume( 1.0f ); // must be PMF

        // learn whether this was a good FF/FB pairing:
if( _correlateTransitions ) {
        updateTransition( _vif, _vib, _vt );
}

if( _correlateTransitions ) {
        _rc._vif.copy( _vt ); // this is the final PMF
}
else {
        _rc._vif.copy( _vib ); // this is the final PMF
}
//        _rc.ff(); // don't eval _vib yet - wait 1 iter
//        _rc.fb();

// old
//        _vib.copy( v3 );
//        _vib.mul ( v2 );
//        _vib.mul ( v1 );
//        _vib.scaleVolume( 1.0f ); // must be PMF

//        _rc._vif.copy( _vib ); // this is the final PMF
//        _rc.ff(); // don't eval _vib yet - wait 1 iter
//        _rc.fb();
    }

    protected void updateTransition( Volume vff, Volume vfb, Volume v12 ) {

        double sum = 0.0;

        int volume = vff.volume();

        for( int nff = 0; nff < volume; ++nff ) {
            float rff  = vff._model[ nff ];

            for( int nfb = 0; nfb < volume; ++nfb ) {
                float rfb = vfb._model[ nfb ];

                float value = rff * rfb;

                v12._model[ nff * volume + nfb ] = value;

                sum += value;
            }
        }

        if( sum <= 0.0 ) {
            return;
        }

        float reciprocal = (float)( 1.0 / sum );

        v12.mul( reciprocal );
    }

    protected void updateConditional( Volume vff, Volume v12, Volume vfb ) {

        // if 1 == 1 and 1-->2
        double sum = 0.0;

        int volume = vff.volume();

        for( int nfb = 0; nfb < volume; ++nfb ) {
            double fbSum = 0.0;

            for( int nff = 0; nff < volume; ++nff ) {
                float rff = vff._model[ nff ];

                float value = v12._model[ nff * volume + nfb ];

                value *= rff;

                fbSum += value;
            }

            vfb._model[ nfb ] = (float)fbSum;

            sum += fbSum;
        }
        
        if( sum <= 0.0 ) {
            return;
        }

        float reciprocal = (float)( 1.0 / sum );

        vfb.mul( reciprocal );
    }
}
