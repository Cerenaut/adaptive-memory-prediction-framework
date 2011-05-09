/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.demos.rps;

import ai.general.mpf.mm.InputSequence;
import ai.general.volumes.Dimensions;
import ai.general.volumes.Volume;
import ai.general.util.Maths;
import ai.general.util.RandomSingleton;

/**
 * Rock/Paper/Scissors
 *
 * @author gideon
 */
public class Gestures {

    public Strategy _s;
    public InputSequence _rps;
    public Volume _vi;
    public double _noiseScale = 0.1; // small amount of noise

    Gestures( Strategy s )
    {
        _s = s;
        
        createSequences();
        
        _vi = new Volume( _rps.get( 0 )._d );
    }

    public Volume get( int move ) {
        Volume v = _rps.get( move );

        _vi.copy( v );

        // add noise:
        int volume = _vi.volume();
        int offset = 0;

        while( offset < volume ) {

            double r = RandomSingleton.random() - 0.5;
                   r *= _noiseScale;

            double value = _vi._model[ offset ];
                   value += r;
                   value = Maths.clamp1( value );

            _vi._model[ offset ] = (float)value;

            ++offset;
        }

        return _vi;
    }
    
    protected void createSequences() {

        int length = 3;
        Dimensions d = new Dimensions( 1 );
        d.configure( 0, 4*4, Dimensions.TYPE_CARTESIAN, "x" );

        _rps = new InputSequence( d, length );

        rock    ( _rps.get( Moves.ROCK     ) );
        paper   ( _rps.get( Moves.PAPER    ) );
        scissors( _rps.get( Moves.SCISSORS ) );
    }

// ROCK
//-**-  0  1  2  3
//****  4  5  6  7
//****  8  9 10 11
//-**- 12 13 14 15

// PAPER
//----  0  1  2  3
//----  4  5  6  7
//----  8  9 10 11
//---- 12 13 14 15

// SCISSORS
//---*  0  1  2  3
//*-*-  4  5  6  7
//-**-  8  9 10 11
//*--* 12 13 14 15

    public static float FG = 0.0f;
    public static float BG = 1.0f;

    public static void rock( Volume v ) {
        v.set( FG );
        v._model[ 0 ] = BG;
        v._model[ 3 ] = BG;
        v._model[ 12 ] = BG;
        v._model[ 15 ] = BG;
    }

    public static void paper( Volume v ) {
        v.set( BG );
    }

// SCISSORS
//---*  0  1  2  3
//*-*-  4  5  6  7
//-**-  8  9 10 11
//*--* 12 13 14 15
    public static void scissors( Volume v ) {
        v.set( BG );
        v._model[ 3 ] = FG;
        v._model[ 4 ] = FG;
        v._model[ 6 ] = FG;
        v._model[ 9 ] = FG;
        v._model[ 10 ] = FG;
        v._model[ 12 ] = FG;
        v._model[ 15 ] = FG;
    }

}
