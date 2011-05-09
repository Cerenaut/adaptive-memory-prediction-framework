/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.volumes;

import ai.general.util.Maths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A set of dimensions to describe a hyperspace.
 * 
 * Once created is basically immutable.
 *
 * @author dave
 */
public class Dimensions {

    public int[] _dimensions = null;
    public int[] _types = null;
    public HashMap< String, Integer > _labels = new HashMap< String, Integer >();

    public static final int TYPE_CARTESIAN = 0; // normal dimensions with integer linear scale
    public static final int TYPE_CIRCULAR  = 1; // wraps around at some value.

    // typical naming:
    public static final String DIMENSION_INPUT     = "i";
    public static final String DIMENSION_OUTPUT    = "o";
    public static final String DIMENSION_WEIGHTS   = "w";

    public static final String DIMENSION_STATE     = "s"; // agent state
    public static final String DIMENSION_ACTION    = "a"; // agent state

    public static final String DIMENSION_TIME      = "t";
    public static final String DIMENSION_FREQUENCY = "f";
    public static final String DIMENSION_BEHAVIOUR = "beh";
    public static final String DIMENSION_DISTANCE  = "d";
    public static final String DIMENSION_BEARING   = "b";

    public static float Radians2Index( float radians, int size ) { // -pi:pi to index
        radians *= Maths.R_PI; // reciprocal of pi, so now -1:1
        radians *= 0.5f;       // -0.5 : 0.5
        radians += 0.5f;       // 0 : 1
        radians *= (float)size;
        return radians;
    }

    public static float Index2Radians( int index, int size ) {
//        if( index >= size ) {
        index %= size;
//        }
        float radians = (float)index;
              radians /= size; // 0..1
              radians -= 0.5f; // -0.5:0.5
              radians *= 2.0f; // -1:1
              radians *= (float)Math.PI;
        return radians;
    }
    
    public Dimensions( int dimensions ) {
        _dimensions = new int[ dimensions ];
        _types = new int[ dimensions ];
        Arrays.fill( _types, TYPE_CARTESIAN );
    }

    public Dimensions( Dimensions d ) {
        int dimensions = d.quantity();
        _dimensions = Arrays.copyOf( d._dimensions, dimensions );
        _types = Arrays.copyOf( d._types, dimensions );

        Set< Entry< String, Integer > > es = d._labels.entrySet();

        Iterator i = es.iterator();

        while( i.hasNext() ) {

            Entry< String, Integer > e = (Entry< String, Integer >)( i.next() );

            _labels.put( e.getKey(), e.getValue() );
        }
    }

    public String description() {

        StringBuilder result = new StringBuilder();

        int d = 0;

        while( d < _dimensions.length ) {

            String s = label( d );

            ++d;

            result.append( s );

            if( d < _dimensions.length ) {
                result.append( "," );
            }
        }

        return result.toString();
    }

    public void configure( int d, int size, int type, String label ) {
        resize( d, size, type );
        label( d, label );
    }

    public void resize( int d, int size, int type ) {
        _dimensions[ d ] = size;
        _types[ d ] = type;
    }

    public void label( int d, String label ) {
        _labels.put( label, d );
    }

    public String label( int d ) {

        Set< Entry< String, Integer > > s = _labels.entrySet();

        for( Entry< String, Integer > e : s ) {
            String label = (String)e.getKey();
            int n = (Integer)e.getValue().intValue();

            if( n == d ) {
                return label;
            }
        }

        return null;
    }

    public int index( String label ) {
        Integer i = _labels.get( label );

        if( i == null ) {
            return _dimensions.length;
        }

        return i.intValue();
    }

    public int quantity() {
        return _dimensions.length;
    }

    public int size( String s ) {
        Integer i = _labels.get( s );

        if( i == null ) {
            return 0;
        }

        return _dimensions[ i ];
    }

    public int size( int dimension ) {
        return _dimensions[ dimension ];
    }

    public int type( int dimension ) {
        return _types[ dimension ];
    }

    public int volume() {

        int volume = 1;

        int i = 0;

        while( i < _dimensions.length ) {

            volume *= _dimensions[ i ];

            ++i;
        }

//System.out.println( "vol="+volume);
        return volume;
    }

    public static Dimensions Create( String name, int[] dimensionSizes ) {

        int dimensions = dimensionSizes.length;// +1;

        Dimensions d = new Dimensions( dimensions );

        for( int n = 0; n < dimensions; ++n ) {
            d.configure(
                n,
                dimensionSizes[ n ],
                Dimensions.TYPE_CARTESIAN,
                name+"."+n );
        }

        return d;
    }

}
