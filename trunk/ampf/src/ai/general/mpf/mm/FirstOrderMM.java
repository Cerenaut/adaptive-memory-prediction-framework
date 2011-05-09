/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.mm;

import ai.general.nn.Schedule;
import ai.general.volumes.Coordinate;
import ai.general.volumes.Dimensions;
import ai.general.volumes.VolumeMap;

/**
 * Predict next PMF given current PMF and previous PMF.
 * 
 * Does it work for temporal clustering?? Yes.
 * Can I get around the 1st order Markov problem with temporal clustering and the hierarchy?? Yes.
 *        But limited by tree structure of hierarchy... less flexible
 * 
 * Similar to a first-order Markov chain?
 * A mix of (Hidden) Markov and Hebbian learning
 * Should I use a Markov model?
 * Types:
 *                        Fully observable        Partially observable
 *   System is autonomous Markov chain 	          hidden Markov model
 *   System is controlled Markov decision process partially observable Markov decision process
 *
 * Predict next state from current state ONLY.
 * Learn state transitions using Hebbian rule.
 * Ignore transitions to same state.??
 * @author dave
 */
public class FirstOrderMM extends MarkovModel {

    public FirstOrderMM( VolumeMap vm, String name, Schedule s, Dimensions input ) {
        super( vm, name, s, input );
    }

    public void associate() {

        Coordinate ci = _vif.start();

        int sizeW = _dw.size( "w.i" );

        // iterate over all inputs x all inputs:
        // we want to strengthen weights where
        // a change from active (1) to inactive (0) in some weight i
        // is associated with an increase in another weight j -
        // a change in j from (0) to (1).
        // We detect this by adding weights and then doing a final normalization
        // This normalization step means we don't have to do a Lerp.
        // And means we are approximating a probability.
        // The directionality we're looking for is achieved by the 2 difference
        // calcs, which are in opposite dirs. We clamp negative results to zero.
//System.out.println( "learningrate="+_learningRate );
        for( int w1 = 0; w1 < sizeW; ++w1 ) {

            // x(t-1)-x(t) ie activation declining
            float i1 = _vif._model[ w1 ];
            float h1 = _vh ._model[ w1 ];
            float d1 = h1 - i1;//d1 = oldValueI - newValueI;

            if( d1 < 0.0f ) d1 = 0.0f;

            for( int w2 = 0; w2 < sizeW; ++w2 ) {

                if( !_predictSameState ) {
                    if( w2 == w1 ) {
                        continue; // don't predict transitions to same state!!!
                        // Want to skip over time where nothing happens.
                    }
                }

                // x(x)-x(t-1) ie activation increasing
                float i2 = _vif._model[ w2 ];
                float h2 = _vh ._model[ w2 ];
                float d2 = i2 - h2; //d2 = newValueJ - oldValueJ;
                
                if( d2 < 0.0f ) d2 = 0.0f;

                //need to decrement? No, just increment others and normalize.
                float increment = _learningRate * d1 * d2;// * 100.0f;

                // w1 --> w2
                int offsetW = w1 * sizeW + w2; // P( transition i=w1,j=w2 | i=w1 )

                _vw._model[ offsetW ] += increment;
            }
        }

        normalizeWeights();

        _vh.copy( _vif );
    }

    public void predict() {

        int sizeW = _dw.size( "w.i" );

        // iterate over all inputs x all inputs:
        // we want to strengthen weights where
        // a change from active (1) to inactive (0) in some weight i
        // is associated with an increase in another weight j -
        // a change in j from (0) to (1).
        // We detect this by adding weights and then doing a final normalization
        // This normalization step means we don't have to do a Lerp.
        // The directionality we're looking for is achieved by the 2 difference
        // calcs, which are in opposite dirs. We clamp negative results to zero.
        for( int w2 = 0; w2 < sizeW; ++w2 ) { // accumulate prob. next state is w2

            float output = 0.0f;

            for( int w1 = 0; w1 < sizeW; ++w1 ) { // contribution from each w1

                // need to check this ordering carefully.
                int offsetW = w1 * sizeW + w2; // assoc of w1<-w2

                float i = _vif._model[ w1 ]; // activation of this input
                float w = _vw._model[ offsetW ]; // mul by assoc. to this output

                // PDF (PMF) of our state is given by _vif
                // P( X_j(t) ) = P( X_j(t+1) | X_i(t-1) ) * P( X_i(t-1) ) * normalizing constant
                output += ( w * i );
            }
            _vof._model[ w2 ] = output;//(float)Maths.clamp1( output );
        }

        _vof.scaleVolume( 1.0f );
    }

//    public void conditional( Volume vcp ) {
//
//        int sizeW = _dw.size( "w.i" );
//
//        for( int w2 = 0; w2 < sizeW; ++w2 ) { // accumulate prob. next state is w2
//            for( int w1 = 0; w1 < sizeW; ++w1 ) { // contribution from each w1
//
//                int offsetW = w1 * sizeW + w2; // assoc of w1<-w2
//
//                float i = _vif._model[ w1 ]; // activation of this input
//                float w = _vw._model[ offsetW ]; // mul by assoc. to this output
//                float t = ( w * i ); // prob. of w2 given w1
//
//                vcp._model[ offsetW ] = t; // predicted probability of transition from w2:w1
//            }
//        }
//    }
    
}
