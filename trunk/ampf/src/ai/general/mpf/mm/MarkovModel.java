/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.mm;

import ai.general.nn.FeedForwardNetwork;
import ai.general.nn.Schedule;
import ai.general.volumes.Dimensions;
import ai.general.volumes.Volume;
import ai.general.volumes.VolumeMap;

/**
 * Base class for Markov Models (predictors)
 * 
 * @author gideon
 * @copyright gideon
 */
public abstract class MarkovModel extends FeedForwardNetwork {

    public boolean _predictSameState = true; // parameter

    public Dimensions _dw = null; // weights
    public Volume _vh; // history of input size f x i (same as input)
    public Volume _vw; // weights size i x o

    public float _learningRate = 0.0f; // set via sensitize()

    public MarkovModel( VolumeMap vm, String name, Schedule s, Dimensions input ) {

        super( vm, name, s );

        configureInputForward ( input );
        configureOutputForward( input ); // same!

        int weights = input.volume();

        configureHistory( input   );
        configureWeights( weights );

        _p.set( "initial-learning-rate", 0.98 );
        _p.set(   "final-learning-rate", 0.02 );
        _p.set(          "weight-decay", 0.95 );

        sensitize( _s.elapsed() );
    }

    public static final int ORDER_1 = 0;
    public static final int ORDER_N = 1;

    public int order() {
        return ORDER_1;
    }
    
    public void configureHistory( Dimensions d ) {
        _vh = new Volume( d );
        _vm.put( volumeName( "h" ), _vh );
    }

    public void configureWeights( int weights ) {

        // WEIGHTS MATRIX
        _dw = new Dimensions( 2 );
        _dw.configure(
            0,
            weights,
            Dimensions.TYPE_CARTESIAN,
            "w.i" );
        _dw.configure(
            1,
            weights,
            Dimensions.TYPE_CARTESIAN,
            "w.j" );

        _vw = new Volume( _dw );
        _vm.put( volumeName( Dimensions.DIMENSION_WEIGHTS ), _vw );

        float weight = 1.0f / (float)( weights -1 ); // ie sum should be 1, all equally likely.

        _vw.set( weight );
    }

    public void sensitize( double timeRatio ) {
        // linear:
        float initialLearningRate = (float)_p.get( "initial-learning-rate" );
        float   finalLearningRate = (float)_p.get(   "final-learning-rate" );

        float range = initialLearningRate - finalLearningRate;

        float elapsed = range * (float)timeRatio;
              elapsed = 1.0f - elapsed; // so initially nearer 1, then declining

        _learningRate = finalLearningRate + elapsed;
    }

    public void normalizeWeights() {
        // normalize the associations of each input.
        // ie the sum of w1-wN = 1
        int sizeW = _dw.size( "w.i" );

        for( int w1 = 0; w1 < sizeW; ++w1 ) {

            float sum = 0.0f;

            for( int w2 = 0; w2 < sizeW; ++w2 ) {
                int offsetW = w1 * sizeW + w2;

                sum += _vw._model[ offsetW ]; // assoc w1->w2
            }

            if( sum <= 0.0f ) { // avoid divide by zero
                continue;
            }

            float reciprocal = 1.0f / sum;

            for( int w2 = 0; w2 < sizeW; ++w2 ) {
                int offsetW = w1 * sizeW + w2;

                _vw._model[ offsetW ] *= reciprocal;
            }
        }
    }

    public void decay() {
        float weightDecay = (float)_p.get( "weight-decay" );
        _vw.mul( weightDecay );
        normalizeWeights();
    }

    @Override public void ff() {
_vif.check();
        sensitize( _s.elapsed() );
        decay();
        associate();
_vw.check();
        predict();
_vof.check();
    }

    public abstract void associate();
    public abstract void predict();

}
