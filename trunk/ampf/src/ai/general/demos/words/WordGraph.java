/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.demos.words;

import ai.general.mpf.mm.InputSequence;
import ai.general.mpf.mm.RandomInputSequence;
import ai.general.mpf.mm.SequenceGraph;
import ai.general.volumes.Dimensions;
import ai.general.volumes.Volume;
import ai.general.util.Maths;
import ai.general.util.RandomSingleton;

/**
 * a,b,d,m
 * dad
 * bab
 * mad
 * dam
 *
 * @author gideon
 */
public class WordGraph {

    public SequenceGraph _sg;
    public Volume _vi;
    public double _noiseScale = 0.1; // small amount of noise

    WordGraph()
    {
        createSequences();
        
        _vi = new Volume( _sg.next()._d );
    }

    public Volume next() {
        Volume v = _sg.next();

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

        // * dad
        // * bab
        // * mad
        // * dam
        _sg = new SequenceGraph();

        int length = 3;
        Dimensions d = new Dimensions( 1 );
        d.configure( 0, 25, Dimensions.TYPE_CARTESIAN, "x" );

        InputSequence random = new RandomInputSequence( d );
        InputSequence ___ = new InputSequence( d, 1 );
        InputSequence dad = new InputSequence( d, length );
        InputSequence bab = new InputSequence( d, length );
        InputSequence mad = new InputSequence( d, length );
        InputSequence dam = new InputSequence( d, length );

        int words = 4;
        double pRandom = 0.4;//0.4;//0.3; // was: 0.0. 0.9 didn't give good results, all noise..
        double pWord = ( 1.0 - pRandom ) / (double)words;
//        double pGap = 0.0;//0.3; // was: 0.0
//        double pWord = ( 1.0 - pGap ) / (double)words;
//        _sg._is0 = ___;
//        _sg._is0 = random;
//        _sg.addSequence( ___, pGap ); // mostly blanks
        _sg.addSequence( random, pRandom ); // equal chance of either
        _sg.addSequence( dad, pWord ); // equal chance of either
        _sg.addSequence( bab, pWord ); // equal chance of either
        _sg.addSequence( mad, pWord ); // equal chance of either
        _sg.addSequence( dam, pWord ); // equal chance of either

//        _( ___.get( 0 ) );
//        _( ___.get( 1 ) );
//        _( ___.get( 2 ) );

        d( dad.get( 0 ) );
        a( dad.get( 1 ) );
        d( dad.get( 2 ) );

        b( bab.get( 0 ) );
        a( bab.get( 1 ) );
        b( bab.get( 2 ) );

        m( mad.get( 0 ) );
        a( mad.get( 1 ) );
        d( mad.get( 2 ) );

        d( dam.get( 0 ) );
        a( dam.get( 1 ) );
        m( dam.get( 2 ) );
    }

//a,b,d,m
//-----   -----   ----*   *---- 0  1  2  3  4
//-----   -----   ----*   *---- 5  6  7  8  9
//**-*-   -***-   -****   ****- 10 11 12 13 14
//*-*-*   *---*   *---*   *---* 15 16 17 18 19
//*-*-*   -****   -****   ****- 20 21 22 23 24
    public static float FG = 0.0f;
    public static float BG = 1.0f;

    public static void _( Volume v ) {
        v.set( BG );
    }

    public static void m( Volume v ) {
       v.set( BG );
       v._model[ 10 ] = FG;
       v._model[ 11 ] = FG;
       v._model[ 13 ] = FG;

       v._model[ 15 ] = FG;
       v._model[ 17 ] = FG;
       v._model[ 19 ] = FG;

       v._model[ 20 ] = FG;
       v._model[ 22 ] = FG;
       v._model[ 24 ] = FG;
    }
    
    public static void b( Volume v ) {
       a( v );
       v._model[  0 ] = FG;
       v._model[  5 ] = FG;
       v._model[ 10 ] = FG;
       v._model[ 20 ] = FG;

       v._model[ 24 ] = BG;
    }

    public static void d( Volume v ) {
       a( v );
       v._model[  4 ] = FG;
       v._model[  9 ] = FG;
       v._model[ 14 ] = FG;
    }

    public static void a( Volume v ) {
       v.set( BG );
       v._model[ 11 ] = FG;
       v._model[ 12 ] = FG;
       v._model[ 13 ] = FG;
        
       v._model[ 15 ] = FG;
       v._model[ 19 ] = FG;

       v._model[ 21 ] = FG;
       v._model[ 22 ] = FG;
       v._model[ 23 ] = FG;
       v._model[ 24 ] = FG;
    }
}
