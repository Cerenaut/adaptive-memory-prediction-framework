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
import ai.general.util.Maths;

/**
 * DEPRECATED EXPERIMENTAL IMPLEMENTATION - DO NOT USE
 * SOM allows reduction of arbitrary dimensions to fixed dimensions.
 * TODO consider 3d som? circular dims?
 * @author dave
 */
public class SOM2D extends BidirectionalNetwork {

//    connection should use hebbian rule to connect to ones that fire at same time.
//    need to measure correlation of the inputs.
//            so basically we specify the volumes manually.
//            (not ideal)
//            then the values it selects are those that correlate
//            ACROSS the set of connections.
//            hmm..how to do this?
//            or all manual?if manual I can name them
//    relations between frequencies very important.. not too expensive

    public Dimensions _dw = null; // weights
    public Volume _vw = null; // matrix weights: i,j,w (weights)
    public Volume _ve = null; // matrix errors: i,j (error)
    public Volume _va = null; // matrix update: i,j (activation)
    public Volume _vl = null; // matrix update: i,j (likelihood learning)
    public Volume _vb = null; // matrix bias: i,j (bias/prior)
    public Volume _vb2 = null; // matrix bias: i,j (bias/prior)

    public Coordinate _cSelectedFF;
    public Coordinate _cSelectedFB;
    public Coordinate _cSelectedFF1;
    public Coordinate _cSelectedFF2;

//    public Volume _va = null; // matrix activation: i,j
//    public int[] _inputTypes = null;

// http://www.neuroinformatik.ruhr-uni-bochum.de/ini/VDM/research/gsn/JavaPaper/node22.html#SECTION00710000000000000000
    public double _sigma_t   = 0.0;
    public double _epsilon_t = 0.0;

    public boolean _learn = true;
    public boolean _learnOnline = true;
//    public boolean _biased = true;
//    public boolean _activateAll = true;
//    public boolean _activateAll = true;
//    public int _neighbourhoodSq = 0; // squared threshold, for fast evaluation
//    public float _sensitivity = Constants.SOM_SENSITIVITY_T0; // aka learning rate
//    public float _reciprocal2SigmaSquared = 0.0f;

    public SOM2D( VolumeMap vm, String name, Schedule s, int inputs, int size, boolean bias ) {

        super( vm, name, s );
        
        configureInputForward1D( inputs );
        configureOutputForward2D( size, size );
        configureWeights( inputs, size );
        configureFeedback();

//        _inputTypes = new int[ inputs ];
//        Arrays.fill( _inputTypes, Dimensions.TYPE_CARTESIAN );

//        _p.set( "som-sigma", 1.7 );
//        _p.set( "som-sensitivity-t0", 0.1 );
//        _p.set( "som-neighbourhood-t0", 0.5 );

        _p.set( "som-sigma-initial", 3.0 );
        _p.set( "som-sigma-final", 0.1 );
        _p.set( "som-sigma-activation", 0.1 );
        _p.set( "som-epsilon-initial", 0.5 );
        _p.set( "som-epsilon-final", 0.005 );
        _p.set( "som-bias-mass", 0.02 );

//        if( bias ) {
        _vb = new Volume( _dof );
        _vb.uniform();
//        }
//        sensitize( 0, Constants.SOM_LEARNING_LIMIT );
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

        // ACTIVATION MATRIX
//        _da = new Dimensions( 3 );
//        _da.configure(
//            0,
//            Constants.CAPACITY_FREQUENCIES,
//            Dimensions.TYPE_CARTESIAN,
//            Dimensions.DIMENSION_FREQUENCY );
//        _da.configure(
//            1,
//            size,
//            Dimensions.TYPE_CARTESIAN,
//            "som.i" );
//        _da.configure(
//            2,
//            size,
//            Dimensions.TYPE_CARTESIAN,
//            "som.j" );

//        _vx = new Volume( _dx );
        _vw = new Volume( _dw );
//        _vw.create( 0.0f );
        _vw.randomize();
        _vm.put( volumeName( Dimensions.DIMENSION_WEIGHTS ), _vw );
//        _va = new Volume( _da );
        _ve = new Volume( _dof ); // 2d, error
        _vl = new Volume( _dof );
        _va = new Volume( _dof ); // 2d, activation
        _vb2 = new Volume( _dof ); // 2d, activation
    }

//    public int type( int input ) {
//        return _inputTypes[ input ];
//    }
//
//    public void setType( int input, int type ) {
//        _inputTypes[ input ] = type;
//    }
//schedule the sensitivity of the som,hmm,ng.w,adaptive
//try to automate the reward t from the state of the adaptive bias
//should be able to solve the hard problem
//
//    public void sensitize( int t, int T ) {
//
//        if( t >= T ) {
//            t = T-1;
//        }
    public void sensitize( double timeRatio ) {

//if( timeRatio > 1.0 ) {
//    int g = 0;
//}
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
//        super.update(); // gets the input
_vif.check();
        sensitize( _s.elapsed() );
//        _vof.set( 0.0f );
//        _cSelectedFF = null;
if( _vb != null ) _vb.check();
_vw.check();
        classify();

_vof.check();
//        _vof.copy( _va );
    }

    @Override public void fb() {
_vob.check();
//        invertMean();
        invertRoulette();
//        invertMode();
_vib.check();
    }

    public void invertRoulette() {
        
//use roulette selection for invert?
//cos they're weighted the weight determines it's classification probability
//works in multi-modal case.
//gives a sane distinct prediction in multi-modal case.
//avoids clustering
//
        _cSelectedFB = _vob.roulette();  // select a voxel proportional to its value as a weight, over all values
//        _cSelectedFB = _vob.start();  // select a voxel proportional to its value as a weight, over all values
//        _cSelectedFB.randomize();
//        _cSelectedFB = _vob.maxAt();// select a voxel proportional to its value as a weight, over all values

        int i = _cSelectedFB._indices[ 0 ];
        int j = _cSelectedFB._indices[ 1 ];

        int sizeW   = _dw.size( "som.w" );
        int sizeI   = _dw.size( "som.i" );
        int sizeJ   = _dw.size( "som.j" );
        int sizeJW  = sizeJ * sizeW;

        for( int w = 0; w < sizeW; ++w ) {
            float x = _vw ._model[ ( i * sizeJW ) + ( j * sizeW ) +w ];
//////////////////////////
if( x < 0.0f ) x = 0.0f;
//////////////////////////
            _vib._model[ w ] = x;
        }
    }

    public int[][] _fbModeMask;
    public Volume _fbMode;
    public double _fbModeSum = 0.0;
    
    public void invertMode() {
//        _fbModeMask = findMode( _vob );
//        _fbMode = findMode( _vob );
        findMode( _vob );
        _fbModeSum = invertMode( _fbModeMask );
    }
    
//    public int[][] findMode( Volume v ) {
    public Volume findMode( Volume v ) {
        Coordinate c = v.roulette();

        return findMode( c, v );
    }

//    public int[][] findMode( Coordinate c, Volume v ) {
    public Volume findMode( Coordinate c, Volume v ) {

        // Create a matrix of classifications (1)==mode, (0)== not mode
        int indexX = 1;
        int indexY = 0;

        // Set the specified coordinate to "mode" and everything else to 0
        int x = 0;
        int y = 0;
        int w = c._d.size( indexX );
        int h = c._d.size( indexY );

//        int[][] mask = new int[ w ][ h ];
        if( _fbModeMask == null ) {
            _fbModeMask = new int[ w ][ h ];
        }
        int[][] mask = _fbModeMask;
        
        for( y = 0; y < h; ++y ) {
            for( x = 0; x < w; ++x ) {
                mask[ x ][ y ] = 0;
            }
        }
//        Arrays.fill( mask, 0 );

        x = c._indices[ indexX ];
        y = c._indices[ indexY ];
        mask[ x ][ y ] = 1;


        // Create a coordinate for manipulation in the loop and a threshold:
        Coordinate c2 = v.start();

        int volume = v.volume();
        
        float t = 1.0f / (float)volume; // uniform level
        t *= 3.0;//1.9;

        // Start looping until no more additions to mode:
        boolean changing = true;

        while( changing ) {

            changing = false;

            for( y = 0; y < h; ++y ) {
                for( x = 0; x < w; ++x ) {

                    int status = mask[ x ][ y ];

                    if( status == 0 ) continue; // not classed as mode.

                    // look at the neighbours of this mask element:
                    for( int y2 = -1; y2 < 2; ++y2 ) {
                        for( int x2 = -1; x2 < 2; ++x2 ) {
                            if(    ( x2 == 0 )
                                && ( y2 == 0 ) ) {
                                continue; // self
                            }

                            int yy = y + y2;
                            int xx = x + x2;

                            if( yy < 0 ) continue;
                            if( xx < 0 ) continue;
                            if( yy >= h ) continue;
                            if( xx >= w ) continue; // out of bounds

                            int status2 = mask[ xx ][ yy ];

                            if( status2 == 1 ) continue; // ALREADY classed as mode.

                            c2.set( indexX, xx );
                            c2.set( indexY, yy );

                            float r = v.get( c2 );

                            if( r > t ) {
                                mask[ xx ][ yy ] = 1;
                                changing = true;
                            }
                        }
                    }
                } // x
            } // y
        } // changing

        // now convert to real volume PDF:
//        Volume m = new Volume( v._d );
        if( _fbMode == null ) _fbMode = new Volume( v._d );
        Volume m = _fbMode;
        m.set( 0.0f );

        Coordinate c3 = m.start();

        for( y = 0; y < h; ++y ) {
            for( x = 0; x < w; ++x ) {

                int status = mask[ x ][ y ];

                if( status == 0 ) continue; // not classed as mode.
                
                c3.set( indexX, x );
                c3.set( indexY, y );

                float value = v.get( c3 );

                m.set( c3, value );
            }
        }

//        m.scaleVolume( 1.0f );
        m.scaleRange( 1.0f );
        
        return m;       
//        return mask;
    }

    public double invertMode( int[][] mask ) {

        _vib.set( 0.0f );

        // ok so take what's in the output.
        // each input vector is a model of the input.
        // The model is weighted by the output associated with that model.
        // Each input value is the mean of the output-weighted values from all outputs.

//        for-each( input ) {
//            sum = 0
//            for-each( model ) {
//                sum += model[ input ] * output[ model ]
//            }
//        }
        int sizeW   = _dw.size( "som.w" );
        int sizeI   = _dw.size( "som.i" );
        int sizeJ   = _dw.size( "som.j" );
        int sizeIJ  = sizeI * sizeJ;
        int sizeJW  = sizeJ * sizeW;

        double sum = 0.0;
        
        for( int w = 0; w < sizeW; ++w ) {

            float  oSum = 0.0f;
            float xoSum = 0.0f;

            for( int i = 0; i < sizeI; ++i ) { // over all i and j @ this f:
                for( int j = 0; j < sizeJ; ++j ) { // over all i and j @ this f:

                    int n = mask[ i ][ j ];
                    if( n == 0 ) continue;

                    float o = _vob._model[                  ( i * sizeJ ) +j ];
                    float x = _vw ._model[ ( i * sizeJW ) + ( j * sizeW ) +w ];

                    // not correct if this weight represents a circular value?
                    // Hard to fix
                    xoSum += ( x * o );
                     oSum += o;
                }
            }

            float weightedMean = 0.0f;

            if( oSum > 0.0f ) {
                weightedMean = xoSum / oSum;
            }

            _vib._model[ w ] = weightedMean;

            sum += oSum;
        }

        return sum;
    }
    
    public void invertMean() {

        _vib.set( 0.0f );

        // ok so take what's in the output.
        // each input vector is a model of the input.
        // The model is weighted by the output associated with that model.
        // Each input value is the mean of the output-weighted values from all outputs.

//        for-each( input ) {
//            sum = 0
//            for-each( model ) {
//                sum += model[ input ] * output[ model ]
//            }
//        }
        int sizeW   = _dw.size( "som.w" );
        int sizeI   = _dw.size( "som.i" );
        int sizeJ   = _dw.size( "som.j" );
        int sizeIJ  = sizeI * sizeJ;
        int sizeJW  = sizeJ * sizeW;

        for( int w = 0; w < sizeW; ++w ) {

            float  oSum = 0.0f;
            float xoSum = 0.0f;

            for( int i = 0; i < sizeI; ++i ) { // over all i and j @ this f:
                for( int j = 0; j < sizeJ; ++j ) { // over all i and j @ this f:
                    float o = _vob._model[                  ( i * sizeJ ) +j ];
                    float x = _vw ._model[ ( i * sizeJW ) + ( j * sizeW ) +w ];

                    // not correct if this weight represents a circular value?
                    // Hard to fix
                    xoSum += ( x * o );
                     oSum += o;
                }
            }

            float weightedMean = 0.0f;

            if( oSum > 0.0f ) {
                weightedMean = xoSum / oSum;
            }

            _vib._model[ w ] = weightedMean;
        }
    }

    public void classify() {

//        if( _vb != null ) {
        double biasMass = _p.get( "som-bias-mass" );

        if( biasMass > 0.0 ) {
//            _vb.bias( _vob, (float)biasMass ); // bias with previous predicted class.
        }
_vb2.copy( _vb );
//        }

        _cSelectedFF = distance();
        _cSelectedFF1 = _cSelectedFF;
System.out.println( "normal: "+_cSelectedFF1.toString() );
//int offset0 = _cSelectedFF.offset();
//        activateLearning( _cSelectedFF );
//        activateForward ( _cSelectedFF, false );
boolean exp = false;
boolean log = false;
        activateForward ( null, exp );
        _vof.exp();
//_vof.add( 0.5f / _vof.volume() );
_vof.scaleVolume( 1.0f );
_va.copy( _vof );
//must bias before sharpening
//        if( biasMass > 0.0 ) {
//            bias( _vb, log );
//        }
//        else {
//            _vof.scaleVolume( 1.0f ); // to make it a PDF
//        }
////////////////////
// NEW fixes the smoothness problem and the RSOM but stops the formation of the models
//_vof.sq();
//_vof.scaleVolume( 1.0f ); // to make it a PDF
////////////////////
//        else {
//_vof.check();
//        _vof.exp();
//_vof.check();
_vof.check();
//        }
        _cSelectedFF = _vof.maxAt(); // a biased maxima
        _cSelectedFF2 = _cSelectedFF;
System.out.println( "biased: "+_cSelectedFF2.toString() );
//int offset1 = _cSelectedFF.offset();

//if( offset0 != offset1 ) {
//    int g = 0;
//}
        activateLearning( _cSelectedFF ); // learn towards this biased max.

//        if( _vb != null ) {
//            _cSelectedFF = activateBiased();
//        }
//        else {
//            _cSelectedFF = activate();
//        }

        if( _learn ) {
            learn( _cSelectedFF );
        }
    }

//    public AbstractPair< Integer, Integer > classify() {
    public Coordinate distance() {

//        int sizeF  = _vif._d.size( Dimensions.DIMENSION_FREQUENCY );
        int sizeX  = _vif._d.size( "i.0" );
        int sizeI  = _vw._d.size( "som.i" );
        int sizeJ  = _vw._d.size( "som.j" );
        int sizeW  = _vw._d.size( "som.w" );
        int sizeIJ = sizeI * sizeJ;
        int sizeJW = sizeJ * sizeW;

        int iMin = 0;
        int jMin = 0;
        double minDiff = Float.MAX_VALUE;
//
//        double fbWeight = _p.get( "fb-weight" );
//        double reciprocalSizeX = 1.0 / (double)sizeX;

        for( int i = 0; i < sizeI; ++i ) {
            for( int j = 0; j < sizeJ; ++j ) {

                float sumSqDiff = 1.0f; // min is 1

                for( int x = 0; x < sizeX; ++x ) {

                    float valueX = _vif._model[ x ];

                    int offsetW = sizeJW * i
                                + sizeW  * j
                                +          x; // w===x
                    float valueW = _vw._model[ offsetW ];

                    float diff = valueX - valueW;
//                    diff *= 1000;
//TOOD: Handle circular dims ?!
//                    if( _inputTypes[ x ] == Dimensions.TYPE_CIRCULAR ) {
//                        diff = (float)Maths.diff1( valueX, valueW );
//                    }

                    sumSqDiff += ( diff * diff ); // sq
                }

                // bias classification:
                int offsetE = sizeJ * i + j;
// DO BIAS LATER
//                int offsetB = offsetE;
//
//                double bias = 1.0 - ( _vbf._model[ offsetB ] * fbWeight );//_weightFB );
//
//                sumSqDiff *= bias; // reduced slightly if predicted

                double error = sumSqDiff;// * reciprocalSizeX;
//                       error = Math.sqrt( error );

                _ve._model[ offsetE ] = (float)error; // for online learning

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
//        _errorFF = Math.sqrt( diffMin ); // make it just abs diff
//        _errorFF /= (double)sizeX; // make it 1 if max diff
//        double ffError = Math.sqrt( diffMin ) / (double)sizeX;
//        _p.set( "ff-error", ffError );
//
//        Coordinate c = _vw.start();
//
//        c.set( "som.i", iMin );
//        c.set( "som.j", jMin );
//
//        return c;
//        AbstractPair< Integer, Integer > ap = new AbstractPair< Integer, Integer >();
//
//        ap._first  = iMin;
//        ap._second = jMin;
//
//        return ap;
    }
  
/*    public void activateOne( int iMin, int jMin, float weight ) {

        int sizeI  = _vw._d.size( "som.i" );
        int sizeJ  = _vw._d.size( "som.j" );
//        int sizeIJ = sizeI * sizeJ;

        int offsetO = sizeJ * iMin + jMin;

        // TODO ADD EFFECT OF BIAS WHEN DETERMINING WINNER

        _vof._model[ offsetO ] += weight;
        _vof.scaleVolume( 1.0f ); // make it a PDF
    }

    public void activateAll( int iMin, int jMin, float weight ) {

        int sizeI  = _vw._d.size( "som.i" );
        int sizeJ  = _vw._d.size( "som.j" );

        double sigma = _p.get( "som-sigma-activation" );
//        double sigma = _sigma_t;
        double denominator = 2.0 * sigma * sigma; // schedule used for this sigma too

        for( int i = 0; i < sizeI; ++i ) {
            for( int j = 0; j < sizeJ; ++j ) {
                int offsetE = sizeJ * i + j;
                int offsetO = offsetE;

                double error = _ve._model[ offsetE ]; // See Miller (2006), sum Sq Diff
                double numerator = -error;
                double exponent = ( numerator / denominator ); // k=0:N-1, lambda = neighbourhood size
                double gaussian = Math.exp( exponent );

                _vof._model[ offsetO ] = (float)gaussian;
            }
        }

        _vof.scaleVolume( 1.0f ); // make it a PDF
    }*/
/*
 * bayes recursion approach
from monografia - y_i(t) = exp() which tries to linearize the sum sq diffs.
This is P( x | Z(t) )   ?

A = P( state is x | new class. ) = P = P( state is x | prediction ) *
P( state is x | class. diff ) = Q

-----------------------------------------------------------------------------
                                               P( state is x | class.
diff ) = Q
A = P * Q
     --------
       sum( P * Q )

one predicted, 2 matching:
P1=1
P2=0
Q1=1
Q2=1

A1 = 1*1 / 1*1+0*1=1/1 = 1  (predicted and matched)
A2 = 0*1 / 1*1+0*1=0/1 = 0  (NOT predicted but matched)

two predicted, 2 matching:
P1=0.5
P2=0.5
Q1=0.9
Q2=0.8

A1 = (0.5*0.9) / (0.5*0.9)+(0.5*0.8)=1/0.85 = 0.52
A2 = (0.5*0.8) / (0.5*0.9)+(0.5*0.8)=0/0.85 = 0 47
*/

    public void bias( Volume vb, boolean log ) {

        // modify the classification result based on a prior and using the Gaussian activation as a likelihood function
        int sizeI  = _vw._d.size( "som.i" );
        int sizeJ  = _vw._d.size( "som.j" );
        
//        double fiddle = ( 1.0 / (double)vb.volume() ) * 0.0001; // much smaller than uniform value
//        double sum = 0.0;
        double max = Double.NEGATIVE_INFINITY;

        for( int i = 0; i < sizeI; ++i ) {
            for( int j = 0; j < sizeJ; ++j ) {
                int offset = sizeJ * i + j;

                double likelihood = _vof._model[ offset ]; // See Miller (2006), sum Sq Diff
                double prior = vb._model[ offset ]; // See Miller (2006), sum Sq Diff
//prior = 1.0;
double posterior = 0.0;
if( log ) {
    prior = Maths.log( prior );
    posterior = likelihood + prior;
}
else {
    posterior = likelihood * prior;
}
//                double posterior = likelihood * prior;
//                double posterior = likelihood + prior;
//                posterior += fiddle;
//                sum += posterior;

////////////////////////////////////////////////////////////////////////////////
float r = (float)posterior;
if(    Float.isNaN( r )
    || Float.isInfinite( r ) ) {
//    posterior = fiddle;
}
if( posterior > max ) max = posterior;
////////////////////////////////////////////////////////////////////////////////

                _vof._model[ offset ] = (float)posterior;
            }
        }

if( log ) {
        // so I added them in log space
        _vof.sub( (float)max );
}
else {
        _vof.scaleVolume( 1.0f );
}
//        _vof.exp();
//        double sum2 = _vof.sum();
//        double reciprocal = 1.0 / sum2;
//        _vof.mul( (float)reciprocal );
//System.out.println( "sum2="+sum2+" max="+max+" recip="+reciprocal);

//        // normalize:
//        double reciprocal = 1.0 / sum;
//System.out.println( "fiddle="+fiddle+" max="+max+" recip="+reciprocal);
//_vof.check();
//        _vof.mul( (float)reciprocal );
//_vof.check();
    }

    public void activateForward( Coordinate c, boolean raise ) {

        int sizeX  = _vif._d.size( "i.0" );
        int sizeI  = _vw._d.size( "som.i" );
        int sizeJ  = _vw._d.size( "som.j" );

//        double reciprocalSizeX = 1.0 / (double)sizeX;
        double sigma = _sigma_t;
        double denominator = 2.0 * sigma * sigma;

        for( int i = 0; i < sizeI; ++i ) {
            for( int j = 0; j < sizeJ; ++j ) {

                int offset = sizeJ * i + j;

                double error = _ve._model[ offset ]; // See Miller (2006), sum Sq Diff

//                (-x)/450 or -(x/450)
//error *= reciprocalSizeX;
//_vof._model[ offset ] = (float)Math.sqrt( error );
                double numerator = -( error );// * reciprocalSizeX );
                double exponent = ( numerator / denominator ); // k=0:N-1, lambda = neighbourhood size
                double gaussian = Math.exp( exponent );
if( !raise ) gaussian = exponent;
                _vof._model[ offset ] = (float)gaussian;
            }
        }
    }

// when there is a prediction failure, we get some numerical problems because
// the likelihood * prior ~= 0 everywhere. None of the modes overlap.
// We could solve this by 
    public Coordinate activateBiased() {

        int iMin = 0;
        int jMin = 0;
        double errorMin = Double.MAX_VALUE;

        int sizeI  = _vw._d.size( "som.i" );
        int sizeJ  = _vw._d.size( "som.j" );

//        double sigma = _p.get( "som-sigma-activation" );
        double sigma = _sigma_t;
        double denominator = 2.0 * sigma;// * sigma; // schedule used for this sigma too

        double fiddle = ( 1.0 / (double)_vb.volume() ) * 0.0001; // much smaller than uniform value
        double sum = 0.0;
        double max = 0.0;
//        double logMax = Double.NEGATIVE_INFINITY;
_vb.uniform();
        // bayes recursion
        // 1st pass: multiply prior by likelihood
        for( int i = 0; i < sizeI; ++i ) {
            for( int j = 0; j < sizeJ; ++j ) {
                int offset = sizeJ * i + j;

                double error = _ve._model[ offset ]; // See Miller (2006), sum Sq Diff
                double numerator = -error;
                double exponent = ( numerator / denominator ); // k=0:N-1, lambda = neighbourhood size
                double gaussian = Math.exp( exponent );

                double bias = _vb._model[ offset ]; // See Miller (2006), sum Sq Diff
//if( Float.isNaN( (float)bias ) ) {
//    int g= 0;
//}
//if( Float.isInfinite( (float)bias ) ) {
//    int g= 0;
//}
//bias = 1.0;
//
//0.05 +1 = 1.05
//0.01 +1 = 1.01
//        = 2.06
                bias = 1.0f;

                double activation = bias * gaussian;
                activation += fiddle;
                float converted = (float)activation;

                if(    Float.isNaN( converted )
                    || Float.isInfinite( converted ) ) {
                    converted = 0.0f;
                }

                _vof._model[ offset ] = converted;

                sum += activation;
if( activation > max ) max = activation;
//                if( bias < 0.0 )
//                    bias = 0.0;
                
//                double logLikelihood = Maths.log( gaussian );
//                double logPrior      = Maths.log( bias );
//                double logSum        = logPrior + logLikelihood;
//
//                float converted = (float)logSum;
//                if(    Float.isNaN( converted )
//                    || Float.isInfinite( converted )
//   /*                 || (converted < 0.0f )*/ ) {
//                    converted = 0.0f;
//                }
//
//                _vof._model[ offset ] = converted;//(float)logSum;
//
//                if( logSum > logMax ) {
//                    logMax = logSum;
//                }

                if( error <= errorMin ) {
                    errorMin = error;
                    iMin = i;
                    jMin = j;
                }
            }
        }

//        sum = 0.0;
//double max = 0.0;
//        int offset = 0;
//
//        while( offset < _vof._model.length ) {
//
//            double r = _vof._model[ offset ];
//
//            double z = r - logMax; // now guarantee at least one value is 1.
//            double y = Maths.exp( z ); // convert back
//
//            float qqq = (float)y;
//if( qqq < 0.0f ) {
//    int g = 0;
//}
//            _vof._model[ offset ] = (float)qqq;
//
//if( y > max ) max = y;
//            sum += y;
//
//            ++offset;
//        }

        double reciprocal = 1.0 / sum;
System.out.println( "fiddle="+fiddle+" max="+max+" recip="+reciprocal);
_vof.check();
        _vof.mul( (float)reciprocal );
_vof.check();
//System.out.println( "logMax="+logMax+" max="+max+" recip="+reciprocal);
//        // 2nd pass: normalize
//        if( sum <= 0.0 ) {
//            return _vof.start(); // no more work req'd, all elements same.
//        }
//
//        double reciprocal = 1.0 / sum;
//
//_vof.check();
//        if(    Float.isInfinite( (float)reciprocal )
//            || Float.isNaN     ( (float)reciprocal ) ) {
////            int g= 0;
//            reciprocal = 10000000.0;
//            _vof.mul( (float)reciprocal );
//        }
//        else {
//            _vof.mul( (float)reciprocal );
//        }
//_vof.check();


        // remember where the best model was:
        Coordinate c = _vof.start();

        c.set( "o.0", iMin );
        c.set( "o.1", jMin );

        return c;
    }

    public void learn( Coordinate cMin ) {
        int iMin = cMin._indices[ 0 ];
        int jMin = cMin._indices[ 1 ];

        learn( iMin, jMin );
    }

    public void activateLearning( Coordinate c ) {

        int sizeX  = _vif._d.size( "i.0" );
        int sizeI  = _vw._d.size( "som.i" );
        int sizeJ  = _vw._d.size( "som.j" );

        int iMin = c._indices[ c._d.index( "o.0" ) ];
        int jMin = c._indices[ c._d.index( "o.1" ) ];

        int offset = sizeJ * iMin + jMin;

        double sumSqDiff = _ve._model[ offset ]; // See Miller (2006), sum Sq Diff
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

                _vl._model[ offset ] = (float)gaussian;
            }
        }
    }

    public void learn( int iMin, int jMin ) {

        // f is only relevant for selecting the input.
        int sizeX  = _vif._d.size( "i.0" );
        int sizeI  = _vw._d.size( "som.i" );
        int sizeJ  = _vw._d.size( "som.j" );
        int sizeW  = _vw._d.size( "som.w" );
        int sizeJW = sizeJ * sizeW;

        // setup the neighbourhood function denominator:
//        double denominator = _sigma_t * _sigma_t;
//
//        if( _learnOnline ) {
////        double reciprocalSizeX = 1.0 / (double)sizeX;
//            int offsetE = sizeJ * iMin + jMin;
//            double error = _ve._model[ offsetE ]; // See Miller (2006)
//                   error /= (double)sizeX;
//            denominator *= error;
//        }
//        else {
//            denominator *= 2.0;
//        }

//        for( int f = 0; f < sizeF; ++f ) {
        for( int i = 0; i < sizeI; ++i ) {
            for( int j = 0; j < sizeJ; ++j ) {

//                int di = i - iMin;
//                int dj = j - jMin;
//////////////////////////////////////////////////////////////////////////////////
//// Manhattan Distance
////                int d = Math.abs( di ) + Math.abs( dj );
////                double numerator = ( (double)d * (double)d );
////                double numerator = ( (double)d * (double)d );// * 0.5;
//////////////////////////////////////////////////////////////////////////////////
//// Euclidean Distance
//                int dSq = di * di + dj * dj;
////                double d = Math.sqrt( (double)dSq );
////                double numerator = ( d );
////                double numerator = ( (double)dSq );
//////////////////////////////////////////////////////////////////////////////////
//
//// TODO: Find limit to reintroduce this test for performance.
////                if( distanceSq >= _neighbourhoodSq ) {
////                    continue; // don't update.
////                }
//
////                double numerator = ( (double)d * (double)d );
//                double numerator = -( (double)dSq );
////                double gaussian = Maths.exp( numerator / denominator );
//
////                double exponent = -( numerator / denominator ); // k=0:N-1, lambda = neighbourhood size
//                double exponent = ( numerator / denominator ); // k=0:N-1, lambda = neighbourhood size
//                double gaussian = Math.exp( exponent );

//                if( gaussian < 0.0001 ) {
//                    continue;
//                }
//                       gaussian = Math.max( 0.00001, gaussian );
                int offset = sizeJ * i + j;
                double gaussian = _vl._model[ offset ];

                       
                // this is within the neighbourhood:
                for( int x = 0; x < sizeX; ++x ) {

                    float valueX = _vif._model[ x ];

                    int offsetW = sizeJW * i
                                + sizeW  * j
                                +          x; // w===x

                    double valueW = _vw._model[ offsetW ];
//if(    Double.isInfinite( valueW )
//    || Double.isNaN( valueW ) ) {
//    int g = 0;
//}

                    // w' = w + a * (x-w)
                    double diff = valueX - valueW; // gives sign and weight
                    double deltaW = _epsilon_t * gaussian * diff;
//if(    Double.isNaN( _epsilon_t )
//    || Double.isNaN( diff )
//    || Double.isNaN( gaussian ) ) {
//    int g = 0;
//}
//if(    Double.isInfinite( _epsilon_t )
//    || Double.isInfinite( diff )
//    || Double.isInfinite( gaussian ) ) {
//    int g = 0;
//}
// in the too hard basket
//                    if( _inputTypes[ x ] == Dimensions.TYPE_CIRCULAR ) {
//                        diff = (float)Maths.diff1( valueX, valueW );
//                    }
//
//                    diff *= _sensitivity;
//                    diff *= influence( distanceSq );
//if(    Double.isNaN( valueW )
//    || Double.isNaN( deltaW ) ) {
//    int g = 0;
//}
//if(    Double.isInfinite( valueW )
//    || Double.isInfinite( deltaW ) ) {
//    int g = 0;
//}

                    valueW += deltaW;

if( valueW < 0.0f ) valueW = 0.0f;
                    
//if(    Double.isNaN( valueW )
//    || Double.isNaN( deltaW ) ) {
//    int g = 0;
//}
//if(    Double.isInfinite( valueW )
//    || Double.isInfinite( deltaW ) ) {
//    int g = 0;
//}

                    _vw._model[ offsetW ] = (float)valueW;
//float f = (float)valueW;
//if(    Float.isInfinite( f )
//    || Float.isNaN( f ) ) {
//    int g = 0;
//}

                }
            }
        }
    }
}
