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
 * Recurrent-SOM (part of Hierarchical Quilted SOM (HQSOM), where it forms SOM-
 * RSOM pairs). From the paper by Miller et al. (2006)
 *
 * For our purposes, this performs temporal clustering in the MPF.
 *
 * The input classification is based on a slowly-changing input, where we
 * compute an exponentially weighted moving average input vector.
 *
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class RSOM2D2 extends SOM2D2 {

    public Volume _vd; // d see Miller 2006 RSOM/HQSOM
    public Volume _vsse2; // non-recursive SumSqError

    public RSOM2D2( VolumeMap vm, String name, Schedule s, int inputs, int size ) {
        super( vm, name, s, inputs, size );

        _vd = new Volume( _dw ); // 3d
        _vd.set( 1.0f );

        _vsse2 = new Volume( _dof ); // 2d, error

        _p.set( "rsom-alpha", 0.5 ); // 1.0 = ordinary SOM
    }

    @Override public Coordinate classify() {

        _cSelectedFFa = recursiveSumSqError( _vif, _vsse ); // for learning
        _cSelectedFFa =          sumSqError( _vif, _vsse2 ); // for FF
        _cSelectedFFa = activateForward( _vsse2, _vaf ); // sharpens the output of SSE, makes it more orthogonal

        _vof.copy( _vaf );
        _vof.mul( _vbf );
        _vof.scaleVolume( 1.0f );

        _cSelectedFFb = _vof.maxAt();

        return _cSelectedFFb;
    }

    public Coordinate recursiveSumSqError( Volume input, Volume sumSqError ) {

        int sizeX  = _vif._d.size( "i.0" );
        int sizeI  = _vw._d.size( "som.i" );
        int sizeJ  = _vw._d.size( "som.j" );
        int sizeW  = _vw._d.size( "som.w" );
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

                    float valueX = input._model[ x ];

                    int offsetW = sizeJW * i
                                + sizeW  * j
                                +          x; // w===x
                    double valueW  = _vw._model[ offsetW ];
                    double valueD0 = _vd._model[ offsetW ];
                    double valueD1 = valueX - valueW;
//equivalent to lerping the input?
//    want to get rid of exp weighted
                    double diff = beta  * valueD0   // d0 is old value
                                + alpha * valueD1;  // d1 = new value
                    // therefore alpha is the weight of the new value

                    sumSqDiff += ( diff * diff ); // sq

                    _vd._model[ offsetW ] = (float)diff;
                }

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

    @Override public void learn( Volume activation ) {

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

//                    float valueX = _vif._model[ x ];

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
