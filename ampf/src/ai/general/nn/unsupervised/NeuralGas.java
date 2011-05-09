/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.nn.unsupervised;

import ai.general.mpf.BidirectionalNetwork;
import ai.general.nn.Schedule;
import ai.general.volumes.Coordinate;
import ai.general.volumes.Dimensions;
import ai.general.volumes.Volume;
import ai.general.volumes.VolumeMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/**
 * AKA Neural Gas.
 * No neighbourhood relation but will optimally fill the space.
 * Thanks to :
 * http://www.neuroinformatik.ruhr-uni-bochum.de/ini/VDM/research/gsn/JavaPaper/node16.html
 *
 * dw_i = eps(t) * h_lambda( k_i(input, models) ) * (input_i-w_i)
 *
 * k_i(input, models) = k, the order of this model
 *
 * dw_i = eps(t) * h_lambda( k ) * (input-w_i)
 *
 * h_lambda( k ) = exp( -k/lambda(t) )
 *
 *    eps(t) =    eps_i * ( (    eps_f /    eps_i )^ ( t/t_max ) )
 * lambda(t) = lambda_i * ( ( lambda_f / lambda_i )^ ( t/t_max ) )
 *
 * initial lambda_i and eps_i chosen
 * final   lambda_f and eps_f chosen
 *
 *
 * @author dave
 */
public class NeuralGas extends BidirectionalNetwork {

    public Dimensions _dw = null; // weights
    public Volume _vw = null; // matrix weights
    public Volume _vb = null; // matrix bias

    public Coordinate _cSelectedFF;
    public Coordinate _cSelectedFB;

    // Terminology from Martinez et al (1993)
    public double _epsilon_i = 0.5; // initial value
    public double _epsilon_f = 0.005; // final value
    public double _lambda_i = 10.0; // initial value
    public double _lambda_f = 0.01; // final value

    public double _neighbourhood = 0.0; // squared threshold, for fast evaluation
    public double _sensitivity = 0.0;
    public double _sensitivityMin = 0.001;

    protected TreeMap _tm = null;//new TreeMap();
    
    public NeuralGas( VolumeMap vm, String name, Schedule s, int inputs, int size ) {

        super( vm, name, s );
        
        configureInputForward1D( inputs );
        configureOutputForward1D( size );
        configureWeights( inputs, size );
        configureFeedback();
    }

    public void configureWeights( int inputs, int size ) {
            
        // WEIGHTS MATRIX
        _dw = new Dimensions( 2 );
        _dw.configure(
            0,
            size,
            Dimensions.TYPE_CARTESIAN,
            "m" );
        _dw.configure(
            1,
            inputs,
            Dimensions.TYPE_CARTESIAN,
            "w" );

        _vw = new Volume( _dw );
        _vw.randomize();
        _vm.put( volumeName( Dimensions.DIMENSION_WEIGHTS ), _vw );
    }

    public void sensitize( double timeRatio ) {

        // Epsilon is sensitivity:
        //    eps(t) =    eps_i * ( (    eps_f /    eps_i ) ^ ( t/t_max ) )
        _sensitivity = (float)( _epsilon_i * ( Math.pow( ( _epsilon_f / _epsilon_i ), timeRatio ) ) );

        // Lambda is neighbourhood:
        // lambda(t) = lambda_i * ( ( lambda_f / lambda_i ) ^ ( t/t_max ) )
        _neighbourhood = (float)( _lambda_i * ( Math.pow( ( _lambda_f / _lambda_i ), timeRatio ) ) );
    }

    @Override public void ff() {
        sensitize( _s.elapsed() );
        _vof.set( 0.0f );
        activate( 1.0f );
    }

    @Override public void fb() {
        invertRoulette();
    }

    public void activate( float value ) {
        int m = classify();

        _vof.activate( m, value );

        learn();
    }

    public void invertRoulette() {

        //use roulette selection for invert?
        //cos they're weighted the weight determines it's classification probability
        //works in multi-modal case.
        //gives a sane distinct prediction in multi-modal case.
        //avoids clustering
        _cSelectedFB = _vob.roulette();  // select a voxel proportional to its value as a weight, over all values

        int m = _cSelectedFB._indices[ 0 ];

        int sizeW   = _dw.size( "w" );
        int sizeM   = _dw.size( "m" );

        for( int w = 0; w < sizeW; ++w ) {
            float x = _vw ._model[ m * sizeW + w ];

            _vib._model[ w ] = x;
        }
    }

    public void invertMean() {

        _vib.set( 0.0f );

        // ok so take what's in the output.
        // each input vector is a model of the input.
        // The model is weighted by the output associated with that model.
        // Each input value is the mean of the output-weighted values from all outputs.

        int sizeW   = _dw.size( "w" );
        int sizeM   = _dw.size( "m" );

        for( int w = 0; w < sizeW; ++w ) { // for-each weight

            float  oSum = 0.0f;
            float xoSum = 0.0f;

            for( int m = 0; m < sizeM; ++m ) { // over all vectors:
                float o = _vob._model[ m ];
                float x = _vw._model[ m * sizeW + w ];

                xoSum += ( x * o );
                 oSum += o;
            }

            float weightedMean = 0.0f;

            if( oSum > 0.0f ) {
                weightedMean = xoSum / oSum;
            }

            // weightedMean is the mean activity for som weight w weighted by all of the outputs' activations
            _vib._model[ w ] = weightedMean;
        }
    }

    public int classify() {

        _tm = new TreeMap();
        
        int sizeX  = _vif._d.size( "i.0" );
        int sizeM  = _vw._d.size( "m" );
//        int sizeW  = _vw._d.size( "w" ); // == sizeX

        int vMin = 0;
        double diffMin = Double.MAX_VALUE;

        double fbWeight = _p.get( "fb-weight" );

        for( int m = 0; m < sizeM; ++m ) {

            double sumSqDiff = 1.0f; // important!

            for( int x = 0; x < sizeX; ++x ) {

                float valueX = _vif._model[ x ];

                int offsetW = sizeX * m + x; // w===x

                float valueW = _vw._model[ offsetW ];

                float diff = valueX - valueW;

//TOOD: Handle circular dims ?!
//                if( _inputTypes[ x ] == Dimensions.TYPE_CIRCULAR ) {
//                    diff = (float)Maths.diff1( valueX, valueW );
//                }

                sumSqDiff += Math.abs( diff );//diff * diff; // sq
            }

            double bias = 1.0 - ( _vb._model[ m ] * fbWeight );//_weightFB );

            sumSqDiff *= bias; // reduced slightly if predicted
            

            // store the diff :
            double scaled = sumSqDiff * sizeM * sizeM;

            ArrayList< Integer > list = (ArrayList< Integer >)_tm.get( sumSqDiff );

            if( list == null ) {
                list = new ArrayList< Integer >();
                _tm.put( new Double( sumSqDiff ), list );
                list.add( m );
            }
            else {
                list.add( m );
            }

            if( sumSqDiff < diffMin ) {
                diffMin = sumSqDiff;
                vMin = m;
            }
        }

        double ffError = Math.sqrt( diffMin ) / (double)sizeX;
        _p.set( "ff-error", ffError );

        return vMin;
    }

    public void learn() {

        if( _tm == null ) {
            System.err.println( "ERROR: Vector score mapping is null, can't learn." );
            return;
        }
        
        //order all by distance from input
        //ordering affects learning
        //w = w + eps * e^-? * x-w
        //http://en.wikipedia.org/wiki/Neural_gas
        //what structure?
        //associate by index. So build a structure which is sorted by value
        // http://www.neuroinformatik.ruhr-uni-bochum.de/ini/VDM/research/gsn/JavaPaper/node16.html
        int sizeX  = _vif._d.size( "i.0" );
        int sizeM  = _vw._d.size( "m" );
        int sizeW  = _vw._d.size( "w" ); // w==x

        Set< Integer > ks = _tm.keySet();

        int k = 0; // sequence, terminology from Martinez paper

        Iterator i = ks.iterator();

        while( i.hasNext() ) {

            Double d = (Double)i.next();

            ArrayList< Integer > list = (ArrayList< Integer >)_tm.get( d ); // elements with same n

            int size = list.size();

            for( int l = 0; l < size; ++l ) {

                int m = list.get( l );

                // dw_i = eps(t) * h_lambda( k ) * (input-w_i)
                // h_lambda( k ) = exp( -k/lambda(t) )
                // lambda(t) = _neighbourhood
                double exponent = -( k / _neighbourhood ); // k=0:N-1, lambda = neighbourhood size
                double exponential = Math.exp( exponent );

                // dw_i = eps(t) * h_lambda( k ) * (input-w_i)
                // eps(t) = _sensitivity
                // dw_i = t1t2 * (input-w_i)
                double t1t2 = _sensitivity * exponential;

                ++k; // can inc now cos don't use it again til next iter

                // eventually the exponent is so small we might as well not bother updating further nodes:
                // all subsequent vectors have smaller terms, so ignore them all.
                if( Math.abs( exponential ) < _sensitivityMin ) {
                    _tm = null; // don't update any more models
                    return;
                }

                for( int x = 0; x < sizeX; ++x ) {

                    float valueX = _vif._model[ x ];

                    int offsetW = sizeX * m + x; // w===x

                    float valueW = _vw._model[ offsetW ];

                    float diff = valueX - valueW;
     
                    // dw_i = eps(t) * h_lambda( k ) * (input-w_i)
                    double dw = t1t2 * diff;
//System.out.println( "dw="+dw );
                    _vw._model[ offsetW ] += dw;
                }
            }
        }

        _tm = null;
    }
}
