/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.adaptive;

import ai.general.mpf.BidirectionalNetwork;
import ai.general.nn.Schedule;
import ai.general.volumes.Dimensions;
import ai.general.volumes.Volume;
import ai.general.volumes.VolumeMap;
import ai.general.util.Maths;

/**
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class RewardCorrelator extends BidirectionalNetwork {

    public AdaptiveSignal _as;
    public Volume _vrc; // reward correlation (with states)
    public Volume _vrb; // reward bias, the effect of correlation

    // probably get rid of these, they're never changed:
    public boolean _asNegative = true; // allow this
    public boolean _fbNormalized = false; // shouldn't be normalized..
    
    public RewardCorrelator(
        VolumeMap vm,
        String name,
        Schedule s,
        Dimensions di,
        AdaptiveSignal as  ) {

        super( vm, name, s );

        _as = as;
        _dif = new Dimensions( di );
        _vif = new Volume( _dif );
        _vrc = new Volume( _dif );
        _vrb = new Volume( _dif );

        _dof = _dif;
        _vof = _vif; // avoid need for copy
        _vob = new Volume( _dof );
        _vib = new Volume( _dif );
        
        _vrc.set( 0.0f );
        _vrb.uniform();

        _p.set( "adaptive-learning-rate", 0.1 );
    }

    protected void updateCorrelation( Volume vif, double weight ) {

        // derivative of reward can be + or -.
        // if node is active (1) * 0.05 = 0.05 and reward is terrible (-1)
        // 0.05 * -1 = -0.05
        // lerp:
        // -0.05 + 0 = -0.05
        // (-1*0.05) + (-1*0.95) = -0.05+-0.95 = -1
        // so correlation range is between -1 <= c <= 1
        double reward = _as.reward();
        reward = Maths.clamp1m1( reward );
        float newValue = (float)reward;//_dr;
        float w = (float)weight;
        float learningRate = (float)_p.get( "adaptive-learning-rate" );
//System.out.println( "dr="+reward+"lr="+learningRate+" act="+vif.max() );
//vif.check();

        int volume = _vrc.volume();

        for( int n = 0; n < volume; ++n ) {

            float activation =  vif._model[ n ]; // if 1 it's made more likely
            float oldValue   = _vrc._model[ n ]; // if 1 it's made more likely

            // only want to change when it's active
            // if not active or learning rate approaches zero, no change.
            float a = w * activation * learningRate;
            float b = 1.0f - a; // fraction unchanged

            float lerp = ( a * newValue )
                       + ( b * oldValue ); // correlation between reward signal and activity in this voxel

            _vrc._model[ n ] = lerp;
        }
    }

    protected void updatePMF() {
        // I want a non-normalized bias. ie not a PMF
        // Reason for this, is that I don't want one choice to restrict another.
        // When one becomes stronger, others are lost in the noise due to normalization.
        int volume = _vrb.volume();
        int offset = 0;

        double SCALING = 0.1;//0.2; // TODO MAKE PARAM!!!
        double uniform = 1.0 / volume;

        while( offset < volume ) {
            double c = _vrc._model[ offset ]; // -1:1

            // want to make it nonlinear so any suggestion of negativity/positivity gets tested.
            c += 1.0; // 0:2
            c *= 0.5; // 0:1
            double b = Maths.logSigmoid1( c ); // nonlinear,centred on 0.5 ie 0.5 = no correlation

            b -= 0.5; // -0.5:0.5
            b *= SCALING; // -?:? suitable for + not mul
            b += uniform; // so value is a small value centred around 1
            b = Maths.clamp1( b );
            
            _vrb._model[ offset ] = (float)b;

            ++offset;
        }
    }

    protected void updatePMFv1() {

        // The bias, althrough updated regularly (ie every ff()) is not predicated
        // on the current state. It is an absolute bias. Later in the fb of the
        // lower MPF nodes it's combined with a PDF representing possible future
        // states and in this way irrelevant options are ignored, regardless how
        // good or bad they may be.

        // ok so we got a correlation that's independent for each element.
        // we want a distribution with a total sum of 1 (ie a PDF) mildly biased
        // by the correlation.
        // if we scale the correlations to a sum of 1.
        // then multiply that by whatever mass we want the bias to have e.g. 0.1
        // then we have a remaining mass of 1-0.1=0.9 that is distributed evenly
        // so the value of each element in the pdf should be

// scale negative correlations from 0 to uniform value.
// scale positive correlations from uniform to 1
//-1 := 0 -- maybe 0 <= n <= uniform?
// 0 := 1.0 (ie unchanged) -- would this be uniform?
// 1 := 2.0 (or what? more?)

        int volume = _vrb.volume();
        int offset = 0;

        double uniform = 1.0 / volume;
        double range = 1.0 - uniform;
        double sum = 0.0;

        while( offset < volume ) {
            double c = _vrc._model[ offset ]; // scale to
            double p = 0.0;

            // piecewise linear scale around uniform value
            if( _asNegative ) {
                if( c < 0.0 ) {
                    // u = 0.002
                    // u + (c*u) (c is neg)
                    // 0.002 + (0.002*-0.1)=0.0018
                    // 0.002 + (0.002*-0.5)=0.0010
                    // 0.002 + (0.002*-0.9)=0.0002
                    // 0.002 + (0.002*-1.0)=0.0000
                    p = uniform + (uniform * c);
                }
                else {
                    p = ( range * c ) + uniform;
                }
            }
            else { // signal is only positive, allow low pos corr. to produce 0 prob.
                p = c;
            }

            if( p < 0.0 ) {
                p = 0.0;
            }

            sum += p;

            _vrb._model[ offset ] = (float)p;

            ++offset;
        }

        if( _fbNormalized ) {
            if( sum <= 0.0 ) {
                _vrb.uniform();
            }
            else {
                float reciprocal = (float)( 1.0 / sum );
                _vrb.mul( reciprocal );
            }
        }

        _vrb.check();
    }

    @Override public void ff() {
        updateCorrelation( _vif, 1.0 );
        updatePMF();
        // _vof is OK already, implicit copy
    }

    @Override public void fb() {
_vrb.check();
_vob.check();
        _vob.scaleVolume( 1.0f ); // it may not be normalized, see note below
        _vib.copy( _vob );
        _vib.mul( _vrb ); // bias it
        _vib.scaleVolume( 1.0f ); // it may not be normalized, see note below
_vib.check();
    }

}
