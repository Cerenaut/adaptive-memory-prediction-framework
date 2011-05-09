/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.volumes;

//import deprecated.volumes.Transform;
//import deprecated.volumes.Operation;
//import deprecated.volumes.Aggregation;
import ai.general.util.Maths;
import ai.general.util.RandomSingleton;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * An N-dimensional matrix. Dimensions are fixed once created. Can be thought of
 * as a large volume of voxels... depending on your preferred terminology.
 * Since many values space is limiting factor hence float not double.
 *
 * TODO Class is getting a bit bloated with semi-useful functions..
 *
 * @author dave
 */
public class Volume { //implements Paintable {

    public static final boolean CHECK_VOLUMES = false;
    
    public Dimensions _d = null;
    public float[] _model = null;//new float[ DimensionSet.volume() ];

    public Volume( Dimensions d ) {
        _d = d;
        create( 0.0f );
    }

    public Volume( Volume v ) {
        _d = v._d;
        _model = Arrays.copyOf( v._model, v._model.length );
    }

    public boolean copy( Volume v ) {

        if( volume() != v.volume() ) {
            return false;
        }

        _model = Arrays.copyOf( v._model, v._model.length );

        return true;
    }

    public boolean copyRange( Volume v, int offsetThis, int offsetThat, int range ) {

        int volumeThis = this.volume();
        int volumeThat =    v.volume();

        if(    ( offsetThis == 0 )
            && ( offsetThat == 0 )
            && ( volumeThis == volumeThat ) ) {
            return copy( v ); // more efficient?
        }

        int offset1 = offsetThis;
        int offset2 = offsetThat;

        int limit = offset2 + range;

        while( offset2 < limit ) {

            _model[ offset1 ] = v._model[ offset2 ];

            ++offset1;
            ++offset2;
        }

        return true;
    }

    public boolean mulRange( Volume v, int offsetThis, int offsetThat, int range ) {

        int offset1 = offsetThis;
        int offset2 = offsetThat;

        int limit = offset2 + range;

        while( offset2 < limit ) {

            _model[ offset1 ] *= v._model[ offset2 ];

            ++offset1;
            ++offset2;
        }

        return true;
    }

    public boolean lerpRange( Volume v, int offsetThis, int offsetThat, int range, float coefficientThis, float coefficientThat ) {

        int offset1 = offsetThis;
        int offset2 = offsetThat;

        int limit = offset2 + range;

        while( offset2 < limit ) {

            float valueThis =   _model[ offset1 ];
            float valueThat = v._model[ offset2 ];

            float lerp = ( valueThis * coefficientThis )
                       + ( valueThat * coefficientThat );

            _model[ offset1 ] = lerp;
            
            ++offset1;
            ++offset2;
        }

        return true;
    }
    
    public boolean write( String filename ) throws IOException {

        if( _model == null ) {
            return false;
        }

        FileOutputStream fos = new FileOutputStream( filename );
        DataOutputStream dos = new DataOutputStream( fos );

        for( int i = 0; i < _model.length; ++i ) {
            dos.writeFloat( _model[ i ] );
        }
        
        return false;
    }

    public boolean read( String filename ) throws IOException {

        if( _model == null ) {
            create( 0.0f );
        }

        FileInputStream fis = new FileInputStream( filename );
        DataInputStream dis = new DataInputStream( fis );

        for( int i = 0; i < _model.length; ++i ) {
            _model[ i ] = dis.readFloat();
        }

        return false;
    }

    public void delete() {
        _model = null;
    }

    public void create( float value ) {
        if( _model == null ) {
            _model = new float[ _d.volume() ];
        }
        
        set( value );
    }
    
    public boolean valid() {
        if( _model == null ) {
            return false;
        }
        return true;
    }

    public int volume() {
        return _d.volume();
    }

    public int volumeExcluding( String invariantDimension ) {
        int volume = volume();
        int size = _d.size( invariantDimension );

        if( size == 0 ) {
            return volume;
        }

        volume /= size;
        
        return volume;
    }

    public Coordinate start() {
        return new Coordinate( _d );
    }

    public Coordinate end() {
        Coordinate c = new Coordinate( _d );
                   c.setMax();
        return c;
    }

//    public Volume transform( Transform t ) {
//
//        Volume v = new Volume( _d );
//
//        if( !valid() ) {
//            return v;
//        }
//
//        v.create( 0.0f ); // default everything to 0
//
//        Coordinate c1 = start();
////        Coordinate c2 = start();
//
//        int offset = 0;
//
//        do { // for-each( value in volume )
//            float value = _model[ offset ]; // don't want linearization again and validity checks
//
//            ++offset;
//
////            Coordinate c2 = new Coordinate( c1 );
//
//            Coordinate c2 = t.apply( c1 );//, c2 );
//
//            v.set( c2, value ); // checks for validity
//        } while( c1.next() ); // for-each( value in volume )
//
//        return v;
//    }
    
    public Volume translate( Coordinate translations ) {

        Volume v = new Volume( _d );

        if( !valid() ) {
            return v;
        }

        v.create( 0.0f ); // default everything to 0

        Coordinate c1 = start();
//        Coordinate c2 = start();

        int offset = 0;

        do { // for-each( value in volume )
            float value = _model[ offset ]; // don't want linearization again and validity checks

            ++offset;

            Coordinate c2 = new Coordinate( c1 );
            c2.translate( translations );

            v.set( c2, value ); // checks for validity
        } while( c1.next() ); // for-each( value in volume )

        return v;
    }

    public double absDiff( Volume v ) {

        // check the vols are valid (ie have a model)
        // and are same size.
        if( volume() !=v.volume() ) {
            return Double.MAX_VALUE;
        }

        double absDiff = 0.0;

        int offset = 0;

        while( offset < _model.length ) {

            float value1 =   _model[ offset ];
            float value2 = v._model[ offset ];

            ++offset;

            absDiff += Math.abs( value2 - value1 );
        }

        return absDiff;
    }

    public float similarity( Volume v ) {

        // check the vols are valid (ie have a model)
        if( !  valid() ) {
            return 0.0f;
        }
        
        if( !v.valid() ) {
            return 0.0f;
        }
        
        // mean difference of nonzero values
        float absDiff = 0.0f;
        float massThis = 0.0f;
        float massThat = 0.0f;

        int offset = 0;

        while( offset < _model.length ) {

            float value1 =   _model[ offset ];
            float value2 = v._model[ offset ];

            ++offset;

            if(    ( value1 != 0.0f )
                || ( value2 != 0.0f ) ) {

                massThis += value1;
                massThat += value2;

                absDiff += Math.abs( value2 - value1 );
            }
        }

        // now have sum of abs diff of value and nbr values
        float massSum = massThis + massThat;

        if( massSum <= 0.0f ) {
            return 0.0f; // no similarity
        }

        float similarity = absDiff / massSum; // so 0 to 1.

        similarity = 1.0f - similarity; // TODO make it work when total @ 1 voxel > 1
        
        return similarity;
    }

    public void setRange( Coordinate cMin, Coordinate cMax, float value ) { // fill the volume defined by these 2 ordinates,
        // TODO: test Coordinate.volumeBounded( cMin, cMax ) as ratio of volume()?
        setLargeRange( cMin, cMax, value );
//        setSmallRange( cMin, cMax, value );
    }

    public void setLargeRange( Coordinate cMin, Coordinate cMax, float value ) { // fill the volume defined by these 2 ordinates,

        Coordinate c = start();//new Coordinate( _d );

        int n = 0;

        do { // for-each( value in volume )
            // quicker just to go through every value in the volume, and filter.
            int offset = n;

            ++n;
            
            if( c.anyLessThan( cMin ) ) {
                continue;
            }

            if( c.anyGreaterThan( cMax ) ) {
                continue;
            }

            _model[ offset ] = value;
        } while( c.next() ); // for-each( value in volume )
    }

    public void setSmallRange( Coordinate cMin, Coordinate cMax, float value ) { // fill the volume defined by these 2 ordinates,

        Coordinate c = new Coordinate( cMin );//start();//new Coordinate( _d );

        boolean finished = false;
//        int n = 0;
        while( !finished ) {

            if( c.equals( cMax ) ) {
                finished = true;
            }

            set( c, value );

            c.next();
        }
    }

//    public float getRange(
//        Coordinate cMin,
//        Coordinate cMax,
//        Aggregation a ) { // fill the volume defined by these 2 ordinates,
//
//        Coordinate c = start();//new Coordinate( _d );
//
//        int n = 0;
//
//        float x0 = a._x0;
//
//        do { // for-each( value in volume )
//            // quicker just to go through every value in the volume, and filter.
//            int offset = n;
//
//            ++n;
//
//            if( c.anyLessThan( cMin ) ) {
//                continue;
//            }
//
//            if( c.anyGreaterThan( cMax ) ) {
//                continue;
//            }
//
//            float x1 = _model[ offset ];
//
//            x0 = a.accumulate( x0, x1 );
//
//        } while( c.next() ); // for-each( value in volume )
//
//        return x0;
//    }

    public float sumRange(
        Coordinate cMin,
        Coordinate cMax ) { // fill the volume defined by these 2 ordinates,

        Coordinate c = start();//new Coordinate( _d );

        int n = 0;

        float xSum = 0.0f;

        do { // for-each( value in volume )
            // quicker just to go through every value in the volume, and filter.
            int offset = n;

            ++n;

// TODO : Make this fast for small ranges. Grossly inefficient as-is.
            if( c.anyLessThan( cMin ) ) {
                continue;
            }

            if( c.anyGreaterThan( cMax ) ) {
                continue;
            }

            float x = _model[ offset ];

            xSum += x;

        } while( c.next() ); // for-each( value in volume )

        return xSum;
    }

    // v1-v2
    // iff v1 < v2, x = v2 - v1
    // else x = 0
    // ( v1 -v2 ) : diff, 0
    public void subLessThan( Volume v1, Volume v2, float threshold, float less ) {
        int offset = 0;

        while( offset < _model.length ) {

            float value1 = v1._model[ offset ];
            float value2 = v2._model[ offset ];

            float value = value1 - value2;

            if( value < threshold ) {
                value = less;
            }

            _model[ offset ] = value;

            ++offset;
        }
    }

    public void mask( Volume v, Volume mask, float maskValue, float maskedValue ) {
        int offset = 0;

        while( offset < _model.length ) {

            float value1 = v._model[ offset ];
            float value2 = mask._model[ offset ];

            float value = value1;

            if( value2 == maskValue ) {
                value = maskedValue;
            }

            _model[ offset ] = value;

            ++offset;
        }
    }

    public void lessThan( Volume v, float less, float more ) {
        int offset = 0;

        while( offset < _model.length ) {

            float valueThis =   _model[ offset ];
            float valueThat = v._model[ offset ];

            if( valueThis < valueThat ) {
                valueThis = less;
            }
            else {
                valueThis = more;
            }

            _model[ offset ] = valueThis;

            ++offset;
        }
    }

    public void lessThan( Volume v1, Volume v2, float less, float more ) {
        int offset = 0;

        while( offset < _model.length ) {

            float valueThis = v1._model[ offset ];
            float valueThat = v2._model[ offset ];

            if( valueThis < valueThat ) {
                valueThis = less;
            }
            else {
                valueThis = more;
            }

            _model[ offset ] = valueThis;

            ++offset;
        }
    }

    public void lessThanOrEqual( Volume v, float less, float more ) {
        int offset = 0;

        while( offset < _model.length ) {

            float valueThis =   _model[ offset ];
            float valueThat = v._model[ offset ];

            if( valueThis <= valueThat ) {
                valueThis = less;
            }
            else {
                valueThis = more;
            }

            _model[ offset ] = valueThis;

            ++offset;
        }
    }

    public void lessThanOrEqual( Volume v1, Volume v2, float less, float more ) {
        int offset = 0;

        while( offset < _model.length ) {

            float valueThis = v1._model[ offset ];
            float valueThat = v2._model[ offset ];

            if( valueThis <= valueThat ) {
                valueThis = less;
            }
            else {
                valueThis = more;
            }

            _model[ offset ] = valueThis;

            ++offset;
        }
    }

    public void mul( float value ) {
        int offset = 0;

        while( offset < _model.length ) {

            _model[ offset ] *= value;

            ++offset;
        }
    }

    public void add( float value ) {
        int offset = 0;

        while( offset < _model.length ) {

            _model[ offset ] += value;

            ++offset;
        }
    }

    public void sub( float value ) {
        int offset = 0;

        while( offset < _model.length ) {

            _model[ offset ] -= value;

            ++offset;
        }
    }

    public void sqrt() {
        int offset = 0;

        while( offset < _model.length ) {

            float value = _model[ offset ];

            value = (float)Math.sqrt( value );

            _model[ offset ] = value;

            ++offset;
        }
    }

    public void sq() {
        int offset = 0;

        while( offset < _model.length ) {

            float value = _model[ offset ];

            value *= value;

            _model[ offset ] = value;

            ++offset;
        }
    }

    public void logmx() {
        int offset = 0;

        while( offset < _model.length ) {

            double value = _model[ offset ];

            value = -Math.log( value );

            _model[ offset ] = (float)value;

            ++offset;
        }
    }
    
    public void expmx() {
        int offset = 0;

        while( offset < _model.length ) {

            double value = _model[ offset ];

            value = Math.exp( -value );

            _model[ offset ] = (float)value;

            ++offset;
        }
    }

    public void exp() {
        int offset = 0;

        while( offset < _model.length ) {

            double value = _model[ offset ];

            value = Math.exp( value );
            
            _model[ offset ] = (float)value;

            ++offset;
        }
    }

    public void log() {
        int offset = 0;

        while( offset < _model.length ) {

            double value = _model[ offset ];

            value = Math.log( value );

            _model[ offset ] = (float)value;

            ++offset;
        }
    }

    public void pow( double power ) {
        int offset = 0;

        while( offset < _model.length ) {

            double value = _model[ offset ];

            value = Math.pow( value, power );

            _model[ offset ] = (float)value;

            ++offset;
        }
    }

    public void set( float value ) {
        Arrays.fill( _model, value );
    }

    public float get( Coordinate o ) {
        if( !o.valid() ) {
            return 0.0f;
        }

        int offset = o.offset();
        return _model[ offset ];
    }

    public void set( Coordinate o, float value ) {

        if( !o.valid() ) {
            return;
        }

        int offset = o.offset();
        _model[ offset ] = value;
    }

    public void add( Coordinate o, float value ) {

        if( !o.valid() ) {
            return;
        }

        int offset = o.offset();
        _model[ offset ] += value;
    }

    public void activate( Coordinate o ) {
        activate( o, 1.0f );
    }

    public void activate( Coordinate o, float value ) {

        if( !o.valid() ) {
            return;
        }

        int offset = o.offset();

        activate( offset, value );
    }

    public void activate( int offset ) {
        activate( offset, 1.0f );
    }

    public void activate( int offset, float value ) {
        value += _model[ offset ];

        value = Math.max( value, 1.0f ); // squash to 1

        _model[ offset ] = value;
    }

    public void addSaturate1( Coordinate o, float value ) {

        if( !o.valid() ) {
            return;
        }

        int offset = o.offset();

        _model[ offset ] = (float)Maths.saturate1( _model[ offset ], value );
    }

    public void addClamp1( Coordinate o, float value ) {

        if( !o.valid() ) {
            return;
        }

        int offset = o.offset();

        float x = _model[ offset ] + value;

        _model[ offset ] = (float)Maths.clamp1( x );
    }

    public void mulAddSaturate1( float r, Volume v ) {

        int offset = 0;

        while( offset < _model.length ) {
            double x =   _model[ offset ] * r
                     + v._model[ offset ];

            _model[ offset ] = (float)Maths.saturate1( x );
            ++offset;
        }
    }

    public void mulAddClamp1( float r, Volume v ) {

        int offset = 0;

        while( offset < _model.length ) {
            double x =   _model[ offset ] * r
                     + v._model[ offset ];

            _model[ offset ] = (float)Maths.clamp1( x );
            ++offset;
        }
    }

    public void addSaturate1( Volume v ) {

        int offset = 0;

        while( offset < _model.length ) {
            double x =   _model[ offset ]
                     + v._model[ offset ];

            _model[ offset ] = (float)Maths.saturate1( x );
            ++offset;
        }
    }

    public void addClamp1( Volume v ) {

        int offset = 0;

        while( offset < _model.length ) {
            double x =   _model[ offset ]
                     + v._model[ offset ];

            _model[ offset ] = (float)Math.max( 1.0, x );
//            _model[ offset ] = (float)Maths.clamp1( x );
            ++offset;
        }
    }

    public void max( Volume v ) {

        int offset = 0;

        while( offset < _model.length ) {
            float r1 =   _model[ offset ];
            float r2 = v._model[ offset ];
   
            _model[ offset ] = Math.max( r1, r2 );
            ++offset;
        }
    }

    public void add( Volume v ) {

        int offset = 0;

        while( offset < _model.length ) {
            _model[ offset ] += v._model[ offset ];
            ++offset;
        }
    }

    public void sub( Volume v ) {

        int offset = 0;

        while( offset < _model.length ) {
            _model[ offset ] -= v._model[ offset ];
            ++offset;
        }
    }

    public void mul( Volume v ) {

        int offset = 0;

        while( offset < _model.length ) {
            _model[ offset ] *= v._model[ offset ];
            ++offset;
        }
    }

    public double sumSqDiff( Volume that ) {

        double sumSqDiff = 0.0;

        int offset = 0;

        while( offset < _model.length ) {

            float valueThis =      _model[ offset ];
            float valueThat = that._model[ offset ];

            double diff = valueThis - valueThat;

            sumSqDiff += ( diff * diff );

            ++offset;
        }

        return sumSqDiff;
    }

    public double sumAbsDiff( Volume that ) {

        double sumAbsDiff = 0.0;

        int offset = 0;

        while( offset < _model.length ) {

            float valueThis =      _model[ offset ];
            float valueThat = that._model[ offset ];

            double diff = Math.abs( valueThis - valueThat );

            sumAbsDiff += diff;

            ++offset;
        }

        return sumAbsDiff;
    }

    public double normalizedAbsDiff( Volume that ) {
        double sumAbsDiff = sumAbsDiff( that );
        double volume = (double)_model.length;
        double normalizedAbsDiff = sumAbsDiff / volume;
        return normalizedAbsDiff;
    }

    public void lerp( Volume that, float weightThis, float weightThat ) {

        int offset = 0;

        while( offset < _model.length ) {

            float valueThis =      _model[ offset ];
            float valueThat = that._model[ offset ];

            float lerp = ( weightThis * valueThis )
                       + ( weightThat * valueThat );

            _model[ offset ] = lerp;

            ++offset;
        }
    }

    public float sum() {

        float sum = 0.0f;

        int offset = 0;
        
        while( offset < _model.length ) {
            sum += _model[ offset ];
            ++offset;
        }

        return sum;
    }

    public double entropy() {

        double sum = 0.0;

        int offset = 0;

        while( offset < _model.length ) {

            double value = _model[ offset ];
            ++offset;

            double r = value * Math.log( value );

            sum += r;
        }

        return -sum;
    }

    public double variance() {

        // sum sq deviations from mean
        double sum = 0.0;
        double sumSq = 0.0;

        int offset = 0;

        while( offset < _model.length ) {
            double value = _model[ offset ];
            ++offset;
            sum += value;
            sumSq += (value * value);
        }

        double samples = (double)_model.length;
        double variance = Maths.variance( sum, sumSq, samples );
        return variance;
    }

    public double mean() {
        double sum = sum();
        int volume = _model.length;
        double qty = (double)volume;

        if( qty <= 0.0 ) {
            return 0.0;
        }

        double mean = sum / qty;
        return mean;
    }

    public float min() {

        float min = Float.MAX_VALUE;

        int offset = 0;

        while( offset < _model.length ) {
            float x = _model[ offset ];

            if( x < min ) {
                min = x;
            }

            ++offset;
        }

        return min;
    }

    public float max() {

        float max = 0.0f;

        int offset = 0;

        while( offset < _model.length ) {
            float x = _model[ offset ];

            if( x > max ) {
                max = x;
            }

            ++offset;
        }

        return max;
    }

    public Coordinate maxAt() {

        float max = 0.0f;

        Coordinate c = start();
        Coordinate cMax = c;

        do { // for-each( value in volume )
            float value = get( c );

            if( value > max ) {
                max = value;
                cMax = new Coordinate( c );
            }
        } while( c.next() ); // for-each( value in volume )

        return cMax;
    }

    public Coordinate roulette() {  // select a voxel proportional to its value as a weight, over all values

        // first sum the values, and handle the all-zero case as totally random:
        Coordinate c = start();

        double sum = sum();

        if( sum <= 0.0 ) {
            c.randomize();
            return c;
        }

        // OK so now do a roulette selection:
        double random = RandomSingleton.random() * sum;
        double accumulated = 0.0;
        
        do { // for-each( value in volume )
            double value = get( c );

            accumulated += value;

            if( accumulated >= random ) {
                return c;
            }
        } while( c.next() ); // for-each( value in volume )

        return end(); // shouldn't happen!
    }

    public void randomize() {

        int offset = 0;

        while( offset < _model.length ) {
            _model[ offset ] = (float)RandomSingleton.random();
            ++offset;
        }
    }

    public boolean check() {

        if( !CHECK_VOLUMES ) {
            return true;
        }
        
        boolean ok = true;

        int offset = 0;

        while( offset < _model.length ) {
            float value = _model[ offset ];

            if( value < 0.0f ) {
                System.out.println( "ERROR: Volume value is negative");
                ok = false;
            }
            if( Float.isNaN( value ) ) {
                System.out.println( "ERROR: Volume value is NaN");
                ok = false;
            }
            if( Float.isInfinite( value ) ) {
                System.out.println( "ERROR: Volume value is infinite");
                ok = false;
            }

            ++offset;
        }

        return ok;
    }

    public float sumSubVolume( int dimensionsExcluded, Coordinate included ) {

        // First we assume the user has specified the indices in all excluded
        // dimensions using the param "included".
        // e.g. in 5-d if dimsExcluded = 3, included = 12300
        // Then we compute a second coordinate which is 1 position beyond the
        // included range (ie the first excluded coordinate).
        // excluded should be 12400
        Coordinate excluded = new Coordinate( included );
        Coordinate offset   = new Coordinate( _d );
                   offset._indices[ dimensionsExcluded -1 ] = 1;

        excluded.add( offset );

        // Compute the offsets that these coordinates define, and apply within
        // this range:
        int offset1 = included.offset();
        int offset2 = excluded.offset();

        float sum = 0.0f;

        while( offset1 < offset2 ) {
            sum += _model[ offset1 ];
            ++offset1;
        }

        return sum;
    }

    public void mulSubVolume( int dimensionsExcluded, Coordinate included, float value ) {

        Coordinate excluded = new Coordinate( included );
        Coordinate offset   = new Coordinate( _d );
                   offset._indices[ dimensionsExcluded -1 ] = 1;

        excluded.add( offset );

        // Compute the offsets that these coordinates define, and apply within
        // this range:
        int offset1 = included.offset();
        int offset2 = excluded.offset();

        while( offset1 < offset2 ) {
            _model[ offset1 ] *= value;
            ++offset1;
        }
    }

    public void scaleSubVolume( int dimensionsExcluded, Coordinate included, float total ) {

        // formula:
        // x = x / sum
        // as reciprocal:
        // x = x * (1/sum)
        float sum = sumSubVolume( dimensionsExcluded, included );

        if( sum <= 0.0f ) {
            return;
        }

        float reciprocal = total / sum;

        mulSubVolume( dimensionsExcluded, included, reciprocal );
    }

    public void uniform() {
        // make it a uniform PDF ie total 1.
        int volume = _model.length;

        float value = (float)( 1.0 / (double)volume );

        set( value );
    }

    public void bias( Volume v, float biasedMass ) {

        // assume vpdf has a sum (mass) of 1
        // total final mass will be 1
        int volume = _model.length;

        float uniformMass = 1.0f - biasedMass;
        float uniformValue = (float)( 1.0 / (double)volume );
              uniformValue *= uniformMass;
              
        int offset = 0;

        while( offset < _model.length ) {
            float   biasedValue = v._model[ offset ] * biasedMass; // everything shrunk
            float combinedValue = uniformValue + biasedValue;
            _model[ offset ] = combinedValue;
            ++offset;
        }
    }

    public void bias( float biasedMass ) {

        // assume vpdf has a sum (mass) of 1
        // total final mass will be 1
        int volume = _model.length;

        float uniformMass = 1.0f - biasedMass;
        float uniformValue = (float)( 1.0 / (double)volume );
              uniformValue *= uniformMass;

        int offset = 0;

        while( offset < _model.length ) {
            float   biasedValue = _model[ offset ] * biasedMass; // everything shrunk
            float combinedValue = uniformValue + biasedValue;
            _model[ offset ] = combinedValue;
            ++offset;
        }
    }

    public void scaleVolume( float total ) {

        // formula:
        // x = x / sum
        // as reciprocal:
        // x = x * (1/sum)
        float sum = sum();

        scaleVolume( total, sum );
    }

    public void scaleVolume( float total, float sum ) {

        if( sum <= 0.0f ) {
            return;
        }

        float reciprocal = total / sum;

        if( Float.isInfinite( reciprocal ) ) { // cos sum is small
            return;
        }

        check();

        mul( reciprocal );

        check();
    }

    public void scaleRange( float limit ) {

        // formula:
        // x = x / max
        // as reciprocal:
        // x = x * (1/max)
        float max = max();

        if( max <= 0.0f ) {
            return;
        }

        // scaling = 1 / max
        // if max < 1, scaling > 1
        // x = x * scaling
        float scaling = limit / max;

        mul( scaling );
    }

    public void scaleRange( float min, float max ) {

        // formula:
        // oldrange = oldmax - oldmin
        // newRange = max-min
        // scaling = newRange / oldRange

        // x = (x - min) * scaling;
        float xMin = Float.MAX_VALUE;
        float xMax = -Float.MAX_VALUE;//0.0f;/Float.MIN_VALUE;

        int offset = 0;

        while( offset < _model.length ) {
            float x = _model[ offset ];

            if( x < xMin ) xMin = x;
            if( x > xMax ) xMax = x;

            ++offset;
        }

        float oldRange = xMax - xMin;
        float newRange =  max -  min;

//System.out.println( "Old Min/Max="+xMin+","+xMax+" New Min/Max="+min+","+max);

        if( oldRange == 0.0f ) {
            set( min );
            return;
        }

        float scaling = newRange / oldRange;
//System.out.println( "Old Range="+oldRange+" New Range"+newRange+" Scaling="+scaling);

        offset = 0;

        while( offset < _model.length ) {
            float x = _model[ offset ];

            x -= xMin;
            x *= scaling;

            _model[ offset ] = x;
            
            ++offset;
        }
    }

}
