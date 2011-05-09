/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.volumes;

import ai.general.util.Maths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Way to access a voxel in a set of dimensions.
 * Assumes when comparing with other coordinates that they have the same
 * dimensions, otherwise random results and array out of bounds exceptions will
 * occur! Checking they're accessing the same set of dimensions is your
 * responsibility!
 * 
 * @author dave
 */
public class Coordinate implements Cloneable {

    public Dimensions _d = null;
    public int[] _indices = null; //new int [ Constants.VOLUME_DIMENSIONS ];

    public Coordinate( Dimensions d ) {
        _d = d;
        _indices = new int[ _d.quantity() ];
        setMin();
    }

    public Coordinate( Coordinate c ) {
        _d = c._d;
        _indices = Arrays.copyOf( c._indices, c._indices.length );
    }

    public @Override Coordinate clone() {
        Coordinate c = null;

        try {
            c = (Coordinate)super.clone(); // will clone array.
        }
        catch( CloneNotSupportedException cnse ) {
            return null;
        }

        return c;
    }

    @Override public boolean equals( Object o ) {
        if( this == o ) {
            return true;
        }

        if( !( o instanceof Coordinate ) ) {
            return false;
        }

        Coordinate c = (Coordinate)o; // allow throw of exception otherwise

        // TODO: Check same dimensions???

        return Arrays.equals( _indices, c._indices );
    }

    @Override public int hashCode() {
        return Arrays.hashCode( _indices );
    }

    @Override public String toString() {
        String s = new String();
        for( int d = 0; d < _indices.length; ++d ) {
            if( d > 0 ) s = s + ", ";
            s = s + _d.label( d ) + "=" + _indices[ d ];
        }
        return s;
    }

    public boolean equivalent( Coordinate c ) {
        return Arrays.equals( _indices, c._indices );
    }

    public void set( HashMap< String, Integer > dimensionValues ) {

        Set< Entry< String, Integer > > s = dimensionValues.entrySet();

        for( Entry< String, Integer > e : s ) {
            String label = (String)e.getKey();
            int n = (Integer)e.getValue().intValue();

            int index = _d.index( label );

            if( index < _indices.length ) {
                set( index, n );
            }
        }
    }

    public void set( String d, int n ) {
        int index = _d.index( d );
        if( index < _indices.length ) {
            set( index, n );
        }
    }

    public void set( int d, int n ) {
        _indices[ d ] = n;
    }
    
    public void set( int n ) {
        Arrays.fill( _indices, n );
    }

    public void setMax() {
        int d = 0;
        int dimensions = _d.quantity();

        while( d < dimensions ) {
            _indices[ d ] = _d.size( d ) -1;

            ++d;
        }
    }

    public void setMin() {
        int d = 0;
        int dimensions = _d.quantity();

        while( d < dimensions ) {
            _indices[ d ] = 0;

            ++d;
        }
    }

    public void setMin( String d ) {
        setMin( _d.index( d ) );
    }

    public void setMin( int d ) {
        _indices[ d ] = 0;
    }

    public void setMax( String d ) {
        int index = _d.index( d );
        if( index < _indices.length ) {
            setMax( index );
        }
    }

    public void setMax( int d ) {
        _indices[ d ] = _d.size( d ) -1;
    }

    public void translate( Coordinate translations ) {
        int dimensions = _indices.length;
        int d = 0;

        while( d < dimensions ) {
            
            int i     =              _indices[ d ];
            int delta = translations._indices[ d ];

            i += delta;
            
            if( _d.type( d ) == Dimensions.TYPE_CIRCULAR ) {
                int size = _d.size( d );
                while( i < 0 ) {
                    i += size;
                }
                while( i >= size ) { // using while it works for any delta
                    i -= size;
                }
            }

            _indices[ d ] = i;
            ++d;
        }
    }

    public double euclidean( Coordinate c ) {
        int sumSqDiff = sumSqDistance( c );
        return Math.sqrt( (double)sumSqDiff );
    }

    public int sumSqDistance( Coordinate c ) {
        int dimensions = _indices.length;
        int d = 0;
        int sumSq = 0;
        
        while( d < dimensions ) {

            int i1 =   _indices[ d ];
            int i2 = c._indices[ d ];
            int type = _d.type( d );
            int diff = i1 - i2;
            int diffSq = 0;

            if( type == Dimensions.TYPE_CARTESIAN ) {
                diffSq = diff * diff;
            }
            else if( type == Dimensions.TYPE_CIRCULAR ) {
                int size = _d.size( d );
                int half = size >> 1;

//0 1 2 3 4 5 size=6
//^       ^   half=3
// 4-0=4    4 > 3 so  diff = 6-4=2

                if( diff > half ) {
                    diff = size - diff;
                }
                diffSq = diff * diff;
            }

            sumSq += diffSq;
            ++d;
        }

        return sumSq;
    }

    public boolean next() {

        // watch out - never returns first [0,0,0]!
        // dimension 0 moves most slowly...
        // dimension n-1 moves fastest
        int dimensions = _d.quantity();
        int d = dimensions -1;

        while( d >= 0 ) { // from (quantity-1) to 0
            if( _indices[ d ] < (_d.size( d )-1) ) { // if not finished @ this dim
                ++_indices[ d ]; // increment this one

                // zero all subsequent quantity:
                ++d; //int d2 = d + 1;
                while( d < dimensions ) { // so at last dim, nothing else incremented.
                    _indices[ d ] = 0;
                    ++d; //int d2 = d + 1;
                }
                return true; // valid coordinate found
            }

            // we've reached the end of a dimension:
            --d;
        }

        return false; // finished.
    }
/*    public boolean next() {

//        x  y  z
//        0  0  0
//        1  0  0
//        2  0  0
//        0  1  0 - reset dim to
//        1  1  0
//        2  1  0
//        0  2  0
//        1  2  0
//        2  2  0
//        0  0  1
//        1  0  1

        // dimension 0 moves most slowly...
        int d = 0;
        int quantity = Dimensions.quantity();

        while( d < quantity ) {
            if( _indices[ d ] < Dimensions.size( d ) ) {
                ++_indices[ d ];
                return true; // move within the current dimension
            }
            // end of dimension d.
            _indices[ d ] = 0; // reset
            ++d;
        }

        return false;
    }*/

    public boolean valid() {
        int d = 0;
        int dimensions = _d.quantity();

        while( d < dimensions ) {
            int n = _indices[ d ];
            if(    ( n < 0 )
                || ( n >= _d.size( d ) ) ) {
                return false;
            }
            ++d;
        }

        return true; 
    }

    public int offset() {

       // 3d        o = z * (width * height) + y * width + x
       // 2d        o =                        y * width + x
       // 1d        o =                                    x
       int offset = 0;//o._indices[ 0 ];
       int cumulative = 1;
       int dimensions = _d.quantity();
       int d = dimensions -1;

       while( d >= 0 ) {

           offset += ( _indices[ d ] * cumulative );

           cumulative *= _d.size( d );

           --d;
       }

       return offset;
   }
/*    public int offset() {

        // 3d        o = z * (width * height) + y * width + x
        // 2d        o =                        y * width + x
        // 1d        o =                                    x
        int offset = 0;//o._indices[ 0 ];
        int cumulative = 1;
        int dimension = 0;
        int quantity = Dimensions.quantity();

        while( dimension < quantity ) {

            offset += ( _indices[ dimension ] * cumulative );

            cumulative *= Dimensions.size( dimension );

            ++dimension;
        }

        return offset;
    }*/

    public boolean anyLessThan( Coordinate o ) {
        int dimension = 0;
        int dimensions = _d.quantity();

        while( dimension < dimensions ) {

            int nThis =   _indices[ dimension ];
            int nThat = o._indices[ dimension ];

            ++dimension;

            if( nThis < nThat ) {
                return true;
            }
        }

        return false;
    }

    public boolean anyGreaterThan( Coordinate o ) {
        int dimension = 0;
        int dimensions = _d.quantity();

        while( dimension < dimensions ) {

            int nThis =   _indices[ dimension ];
            int nThat = o._indices[ dimension ];

            ++dimension;

            if( nThis > nThat ) {
                return true;
            }
        }

        return false;
    }

    public void add( Coordinate c ) {
        int dimension = 0;
        int dimensions = _d.quantity();

        while( dimension < dimensions ) {

            _indices[ dimension ] += c._indices[ dimension ];

            ++dimension;
        }
    }

    public void mul( Coordinate c ) {
        int dimension = 0;
        int dimensions = _d.quantity();

        while( dimension < dimensions ) {

            _indices[ dimension ] *= c._indices[ dimension ];

            ++dimension;
        }
    }

    public void add( double value ) {
        int dimension = 0;
        int dimensions = _d.quantity();

        while( dimension < dimensions ) {

            _indices[ dimension ] += value;

            ++dimension;
        }
    }

    public void mul( double value ) {
        int dimension = 0;
        int dimensions = _d.quantity();

        while( dimension < dimensions ) {

            _indices[ dimension ] *= value;

            ++dimension;
        }
    }

    public static int volume( Coordinate c1, Coordinate c2 ) {

        int dimension = 0;
        int volume = 1;
        
        while( dimension < c1._indices.length ) {

            int n1 = c1._indices[ dimension ];
            int n2 = c2._indices[ dimension ];

            int range = Math.abs( n2 - n1 ) +1;

            volume *= range;

            ++dimension;
        }

        return volume;
    }

    public static Coordinate lowerBound( Coordinate c1, Coordinate c2 ) {
        Coordinate c3 = new Coordinate( c1._d );

        int dimensions = c1._d.quantity();
        int d = dimensions -1;

        while( d >= 0 ) { // from (quantity-1) to 0

            int n1 = c1._indices[ d ];
            int n2 = c2._indices[ d ];
            int min = Math.min( n1, n2 );
//            int max = Math.max( n1, n2 ) +1;

            c3._indices[ d ] = min;

            --d;
        }

        return c3;
    }

    public boolean nextBounded( Coordinate c1, Coordinate c2 ) {

        // dimension 0 moves most slowly...
        // dimension n-1 moves fastest
        int dimensions = c1._d.quantity();
        int d = dimensions -1;

        while( d >= 0 ) { // from (quantity-1) to 0

            int n1 = c1._indices[ d ];
            int n2 = c2._indices[ d ];
            int max = Math.max( n1, n2 ) +1;

            if( _indices[ d ] < max ) { // if not finished @ this dim
                ++_indices[ d ]; // increment this one

                // zero all subsequent quantity:
                ++d; // start with d+1

                while( d < dimensions ) { // so at last dim, nothing else incremented.

                    n1 = c1._indices[ d ];
                    n2 = c2._indices[ d ];
                    int min = Math.min( n1, n2 );

                    _indices[ d ] = min;
                    ++d; //int d2 = d + 1;
                }
                return true; // valid coordinate found
            }

            // we've reached the end of a dimension:
            --d;
        }

        return false; // finished.
    }

    public static int volumeBounded( Coordinate c1, Coordinate c2 ) { // size of the volume defined by these 2 ordinates,

        // dimension 0 moves most slowly...
        int size = 1;
        int d = 0;
        int dimensions = c1._d.quantity();

        while( d < dimensions ) {
            int n1 = c1._indices[ d ];
            int n2 = c2._indices[ d ];

            int range = Math.abs( n2 - n1 ) +1;

            size *= range;

            ++d;
        }

        return size;
    }

    public void randomize() { // size of the volume defined by these 2 ordinates,

        // dimension 0 moves most slowly...
        int d = 0;
        int dimensions = _d.quantity();

        while( d < dimensions ) {

            int size = _d.size( d );

            _indices[ d ] = Maths.randomInt( size );
            
            ++d;
        }
    }

}
