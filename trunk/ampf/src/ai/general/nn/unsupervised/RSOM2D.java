/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.nn.unsupervised;

import ai.general.nn.Schedule;
import ai.general.volumes.Coordinate;
import ai.general.volumes.Volume;
import ai.general.volumes.VolumeMap;

/**
 * DEPRECATED EXPERIMENTAL IMPLEMENTATION - DO NOT USE
 * Temporal clustering.
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class RSOM2D extends SOM2D {

    public Volume _vd; // d see Miller 2006 RSOM/HQSOM

    public RSOM2D( VolumeMap vm, String name, Schedule s, int inputs, int size, boolean ffBias ) {
        super( vm, name, s, inputs, size, ffBias );

        _vd = new Volume( _dw ); // 3d
        _vd.set( 1.0f );

        _p.set( "rsom-alpha", 0.5 ); // 1.0 = ordinary SOM
    }

    @Override public Coordinate distance() {

        int sizeX  = _vif._d.size( "i.0" );
        int sizeI  = _vw._d.size( "som.i" );
        int sizeJ  = _vw._d.size( "som.j" );
        int sizeW  = _vw._d.size( "som.w" );
        int sizeIJ = sizeI * sizeJ;
        int sizeJW = sizeJ * sizeW;

        int iMin = 0;
        int jMin = 0;
        double minDiff = Float.MAX_VALUE;
        double alpha = _p.get( "rsom-alpha" );
        double beta  = 1.0 - alpha;

        for( int i = 0; i < sizeI; ++i ) {
            for( int j = 0; j < sizeJ; ++j ) {

                double sumSqDiff = 1.0f; // min is 1

                for( int x = 0; x < sizeX; ++x ) {

                    float valueX = _vif._model[ x ];

                    int offsetW = sizeJW * i
                                + sizeW  * j
                                +          x; // w===x
                    double valueW  = _vw._model[ offsetW ];
                    double valueD0 = _vd._model[ offsetW ];
                    double valueD1 = valueX - valueW;

                    double diff = beta  * valueD0   // d0 is old value
                                + alpha * valueD1;  // d1 = new value
                    // therefore alpha is the weight of the new value

                    sumSqDiff += ( diff * diff ); // sq

                    _vd._model[ offsetW ] = (float)diff;
                }

                int offsetE = sizeJ * i + j;

                double error = sumSqDiff;// * reciprocalSizeX;

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
    }

    @Override public void learn( int iMin, int jMin ) {

        // f is only relevant for selecting the input.
        int sizeX  = _vif._d.size( "i.0" );
        int sizeI  = _vw._d.size( "som.i" );
        int sizeJ  = _vw._d.size( "som.j" );
        int sizeW  = _vw._d.size( "som.w" );
        int sizeJW = sizeJ * sizeW;

//        // setup the neighbourhood function denominator:
//        double denominator = _sigma_t * _sigma_t;
//
//        if( _learnOnline ) {
//            int offsetE = sizeJ * iMin + jMin;
//            double error = _ve._model[ offsetE ]; // See Miller (2006)
//                   error /= (double)sizeX;
//            denominator *= error;
//        }
//        else {
//            denominator *= 2.0;
//        }

        for( int i = 0; i < sizeI; ++i ) {
            for( int j = 0; j < sizeJ; ++j ) {

//                int di = i - iMin;
//                int dj = j - jMin;
//
//                // Euclidean Distance
//                int dSq = di * di + dj * dj;
//
//                double numerator = -( (double)dSq );
//                double exponent = ( numerator / denominator ); // k=0:N-1, lambda = neighbourhood size
//                double gaussian = Math.exp( exponent );
                int offset = sizeJ * i + j;
                double gaussian = _vl._model[ offset ];

                // this is within the neighbourhood:
                for( int x = 0; x < sizeX; ++x ) {

                    float valueX = _vif._model[ x ];

                    int offsetW = sizeJW * i
                                + sizeW  * j
                                +          x; // w===x

                    double valueW = _vw._model[ offsetW ];
                    double valueD = _vd._model[ offsetW ];

                    //  SOM: w' = w + a * (x-w)
                    // RSOM: w' = w + a * d
                    double diff = valueD;//valueX - valueW; // gives sign and weight
                    double deltaW = _epsilon_t * gaussian * diff;

                    valueW += deltaW;
if( valueW < 0.0f ) valueW = 0.0f;

                    _vw._model[ offsetW ] = (float)valueW;
                }
            }
        }
    }

}
