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

/**
 * Kohonen Self-Organising Map (SOM). 
 * SOM classifies N input dims. to coordinates within M output dims (in this case, 2).
 * We use the SOM for spatial pooling as part of MPF.
 * @author dave
 */
public class SOM2D2 extends BidirectionalNetwork {

    public Dimensions _dw = null; // weights
    public Volume _vw = null; // matrix weights: i,j,w (weights)
    public Volume _vsse  = null; // matrix errors: i,j (error)
    public Volume _vbf = null; // matrix bias: i,j (bias/prior)
    public Volume _val = null; // matrix activation for learning
    public Volume _vaf = null; // matrix update: i,j (activation)
    public Volume _vrb = null; // roulette FB

    public Coordinate _cSelectedFF;
    public Coordinate _cSelectedFFa;
    public Coordinate _cSelectedFFb;
    public Coordinate _cSelectedFB;

// http://www.neuroinformatik.ruhr-uni-bochum.de/ini/VDM/research/gsn/JavaPaper/node22.html#SECTION00710000000000000000
    public double _sigma_t   = 0.0;
    public double _epsilon_t = 0.0;

    public boolean _bias = true; // you can switch all learning off
    public boolean _learn = true; // you can switch all learning off
    public boolean _learnOnline = true; // learning is online or fixed schedule

    public SOM2D2( VolumeMap vm, String name, Schedule s, int inputs, int size ) {

        super( vm, name, s );
        
        configureInputForward1D( inputs );
        configureOutputForward2D( size, size );
        configureWeights( inputs, size );
        configureFeedback();
        
        _vob.uniform(); // has no effect

        _p.set( "som-sigma-initial", 3.0 );
        _p.set( "som-sigma-final", 0.1 );
        _p.set( "som-sigma-activation", 0.1 );
        _p.set( "som-epsilon-initial", 0.5 );
        _p.set( "som-epsilon-final", 0.005 );
        _p.set( "som-roulette-power", 12.0 );
    }

    public void configureWeights( int inputs, int size ) {
            
        // WEIGHTS MATRIX
        _dw = new Dimensions( 3 );
        _dw.configure(
            0,
            size,
            Dimensions.TYPE_CARTESIAN,
            "som.i" );
        _dw.configure(
            1,
            size,
            Dimensions.TYPE_CARTESIAN,
            "som.j" );
        _dw.configure(
            2,
            inputs,
            Dimensions.TYPE_CARTESIAN,
            "som.w" );

        _vw = new Volume( _dw );
        _vw.randomize();
        _vm.put( volumeName( Dimensions.DIMENSION_WEIGHTS ), _vw );

        _vsse  = new Volume( _dof ); // 2d, error
        _vbf  = new Volume( _dof ); // 2d, bias-forwards
        _vbf.uniform(); // ie default has no effect
        _val  = new Volume( _dof ); // 2d, act. for Learning
        _vaf  = new Volume( _dof ); // 2d, act. Forwards
        _vrb  = new Volume( _dof ); // 2d, roulette FB selection vol
    }

    public void sensitize( double timeRatio ) {

        double sigma_i = _p.get( "som-sigma-initial" );
        double sigma_f = _p.get( "som-sigma-final" );
        double epsilon_i = _p.get( "som-epsilon-initial" );
        double epsilon_f = _p.get( "som-epsilon-final" );

        // Lambda is neighbourhood:
        // lambda(t) = lambda_i * ( ( lambda_f / lambda_i ) ^ ( t/t_max ) )
        _sigma_t = (float)( sigma_i * ( Math.pow( ( sigma_f / sigma_i ), timeRatio ) ) );

        // Epsilon is sensitivity:
        //    eps(t) =    eps_i * ( (    eps_f /    eps_i ) ^ ( t/t_max ) )
        _epsilon_t = (float)( epsilon_i * ( Math.pow( ( epsilon_f / epsilon_i ), timeRatio ) ) );
    }

    @Override public void ff() {

        _vbf.copy( _vob );
_vbf.check();
        _vbf.sqrt();
_vbf.check();
        _vbf.scaleVolume( 1.0f );
_vbf.check();
        
        sensitize( _s.elapsed() );
_vif.check();
_vbf.check();
_vw.check();
        _cSelectedFF = classify();
_vof.check();
        if( _learn ) {
            activateLearning( _cSelectedFF, _vsse, _val ); // learn towards this biased max., but diffs are unbiased
            learn( _val );
        }

        _vof.sq();
        _vof.scaleVolume( 1.0f );
    }

    @Override public void fb() {
_vob.check();
        double power = _p.get( "som-roulette-power" );
        _vrb.copy( _vob );
        _vrb.pow( power );
        _vrb.scaleVolume( 1.0f );
//        _vbf.copy( _vob );
        _cSelectedFB = _vrb.roulette();  // select a voxel proportional to its value as a weight, over all values
//        _cSelectedFB = _vob.start();  // select a voxel proportional to its value as a weight, over all values
//        _cSelectedFB.randomize();
//        _cSelectedFB = _vob.maxAt();// select a voxel proportional to its value as a weight, over all values
        invert( _cSelectedFB );
_vib.check();
    }

    public Coordinate classify() {

        _cSelectedFFa = sumSqError( _vif, _vsse );
        _cSelectedFFa = activateForward( _vsse, _vaf ); // sharpens the output of SSE, makes it more orthogonal

        _vof.copy( _vaf );
        _vof.scaleVolume( 1.0f );

        if( _bias ) {
            _vof.mul( _vbf );
            _vof.scaleVolume( 1.0f );
        }

        _cSelectedFFb = _vof.maxAt();

        return _cSelectedFFb;
    }
    
    public Coordinate activateForwardGaussian( Volume sumSqError, Volume activation ) {

        int sizeI  = _vw._d.size( "som.i" );
        int sizeJ  = _vw._d.size( "som.j" );

        int iMax = 0;
        int jMax = 0;

        double sum = 0.0;
        double max = 0.0;
//        double reciprocalSizeX = 1.0 / (double)sizeX;
        double sigma = _sigma_t;
        double denominator = 2.0 * sigma * sigma;

        for( int i = 0; i < sizeI; ++i ) {
            for( int j = 0; j < sizeJ; ++j ) {

                int offset = sizeJ * i + j;

                double error = sumSqError._model[ offset ]; // See Miller (2006), sum Sq Diff

//                (-x)/450 or -(x/450)
//error *= reciprocalSizeX;
//_vof._model[ offset ] = (float)Math.sqrt( error );
                double numerator = -( error );// * reciprocalSizeX );
                double exponent = ( numerator / denominator ); // k=0:N-1, lambda = neighbourhood size
                double gaussian = Math.exp( exponent );

                activation._model[ offset ] = (float)gaussian;

                sum += gaussian;

                if( gaussian >= max ) {
                    max = gaussian;
                    iMax = i;
                    jMax = j;
                }
            }
        }

        activation.scaleVolume( 1.0f, (float)sum ); // normalize
        
        Coordinate c = activation.start();

        c.set( "o.0", iMax );
        c.set( "o.1", jMax );

        return c;
    }

    public Coordinate activateForward( Volume sumSqError, Volume activation ) {

        int sizeI  = _vw._d.size( "som.i" );
        int sizeJ  = _vw._d.size( "som.j" );
//        int sizeW  = _vw._d.size( "som.w" );

        int iMax = 0;
        int jMax = 0;

        double maxError = sumSqError.max();
        double reciprocalMaxError = 1.0 / maxError;
        double max = 0.0;
        double sum = 0.0;

        for( int i = 0; i < sizeI; ++i ) {
            for( int j = 0; j < sizeJ; ++j ) {

                int offset = sizeJ * i + j;

                double         error = sumSqError._model[ offset ];
                double relativeError = error * reciprocalMaxError;
                double p = 1.0 - relativeError;

                activation._model[ offset ] = (float)p;

                sum += p;

                if( p >= max ) {
                    max = p;
                    iMax = i;
                    jMax = j;
                }
            }
        }

        activation.scaleVolume( 1.0f, (float)sum ); // normalize

        Coordinate c = activation.start();

        c.set( "o.0", iMax );
        c.set( "o.1", jMax );

        return c;
    }

    public Coordinate sumSqError( Volume input, Volume sumSqError ) {

        int sizeX  = input._d.size( "i.0" );
        int sizeI  = _vw._d.size( "som.i" );
        int sizeJ  = _vw._d.size( "som.j" );
        int sizeW  = _vw._d.size( "som.w" );
        int sizeJW = sizeJ * sizeW;

        int iMin = 0;
        int jMin = 0;

        double minDiff = Float.MAX_VALUE;

        for( int i = 0; i < sizeI; ++i ) {
            for( int j = 0; j < sizeJ; ++j ) {

                float sumSqDiff = 1.0f; // min is 1

                for( int x = 0; x < sizeX; ++x ) {

                    float valueX = input._model[ x ];

                    int offsetW = sizeJW * i
                                + sizeW  * j
                                +          x; // w===x
                    float valueW = _vw._model[ offsetW ];

                    float diff = valueX - valueW;

                    sumSqDiff += ( diff * diff ); // sq
                }

                // bias classification:
                int offsetE = sizeJ * i + j;

                double error = sumSqDiff;

                sumSqError._model[ offsetE ] = (float)error; // for online learning

                if( sumSqDiff < minDiff ) {
                    minDiff = sumSqDiff;
                    iMin = i;
                    jMin = j;
                }
            } // model ij
        } // models i*

        Coordinate c = _vof.start();

        c.set( "o.0", iMin );
        c.set( "o.1", jMin );

        return c;
    }
  
    public void invert( Coordinate c ) {

        int i = _cSelectedFB._indices[ 0 ];
        int j = _cSelectedFB._indices[ 1 ];

        int sizeW   = _dw.size( "som.w" );
        int sizeI   = _dw.size( "som.i" );
        int sizeJ   = _dw.size( "som.j" );
        int sizeJW  = sizeJ * sizeW;

        for( int w = 0; w < sizeW; ++w ) {
            float x = _vw ._model[ ( i * sizeJW ) + ( j * sizeW ) +w ];

            if( x < 0.0f ) x = 0.0f; // sometimes, especially early on, can become slightly negative

            _vib._model[ w ] = x;
        }
    }

    public void activateLearning( Coordinate c, Volume sumSqError, Volume activation ) {

        int sizeX  = _vif._d.size( "i.0" );
        int sizeI  = _vw._d.size( "som.i" );
        int sizeJ  = _vw._d.size( "som.j" );

        int iMin = c._indices[ c._d.index( "o.0" ) ];
        int jMin = c._indices[ c._d.index( "o.1" ) ];
        int offset = sizeJ * iMin + jMin;

        double sumSqDiff = sumSqError._model[ offset ]; // See Miller (2006), sum Sq Diff
        double error = sumSqDiff / (double)sizeX;
        double denominator = 0.0;

        if( _learnOnline ) {
            double sigma = _p.get( "som-sigma-activation" );
            denominator = error * sigma * sigma;
        }
        else {
            double sigma = _sigma_t;
            denominator = 2.0 * sigma * sigma;
        }

        for( int i = 0; i < sizeI; ++i ) {
            for( int j = 0; j < sizeJ; ++j ) {

                int di = i - iMin;
                int dj = j - jMin;
                int dSq = di * di + dj * dj;

                double numerator = -( (double)dSq );
                double exponent = ( numerator / denominator ); // k=0:N-1, lambda = neighbourhood size
                double gaussian = Math.exp( exponent );

                offset = sizeJ * i + j;

                activation._model[ offset ] = (float)gaussian;
            }
        }
    }

    public void learn( Volume activation ) {

        int sizeX  = _vif._d.size( "i.0" );
        int sizeI  = _vw._d.size( "som.i" );
        int sizeJ  = _vw._d.size( "som.j" );
        int sizeW  = _vw._d.size( "som.w" );
        int sizeJW = sizeJ * sizeW;

        for( int i = 0; i < sizeI; ++i ) {
            for( int j = 0; j < sizeJ; ++j ) {

                int offset = sizeJ * i + j;
                double gaussian = activation._model[ offset ];
                       
                // this is within the neighbourhood:
                for( int x = 0; x < sizeX; ++x ) {

                    float valueX = _vif._model[ x ];

                    int offsetW = sizeJW * i
                                + sizeW  * j
                                +          x; // w===x

                    double valueW = _vw._model[ offsetW ];

                    // w' = w + a * (x-w)
                    double diff = valueX - valueW; // gives sign and weight
                    double deltaW = _epsilon_t * gaussian * diff;

                    valueW += deltaW;
                    if( valueW < 0.0f ) valueW = 0.0f;
                    
                    _vw._model[ offsetW ] = (float)valueW;
                }
            }
        }
    }
}
