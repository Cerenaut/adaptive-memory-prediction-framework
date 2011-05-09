/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Supports 3 limited number systems: 1, R and W.
 * W = 2 * pi.
 *
 * Throughout, suffixes:
 * 1 means interval   0 <= n <= 1   ie unit values.
 * R means interval -pi <= n <= pi
 * W means interval   0 <= n <= W
 *
 * @author dave
 */
public class Maths {

    // Notation:
    // R = radians, expected range -pi : pi
    // W = 2 * PI, expected range 0 : W
    // R_x = reciprocal of x
    // 1 == unit == some value value n such that 0 <= n <= 1
    // x2y means convert from x TO y.
    //___________________________________________________________________________
    public static double SQRT2 = Math.sqrt( 2.0 ); // ~= 1.4142135623;
    public static double W     = Math.PI * 2.0;    // ~= 6.28
    public static double R_PI  = 1.0 / Math.PI; // reciprocal of pi, allows fast / pi
    public static double R_W   = 1.0 / W; // reciprocal of pi, allows fast / pi

    protected static double radians2degrees = 180.0 / Math.PI;
    protected static double degrees2radians = Math.PI / 180.0;


    // Converting radians to degrees (and vice-versa)
    //___________________________________________________________________________
    public static double random() {
        return RandomSingleton.random(); // force all randoms thru the same optionally seeded generator
    }

    public static double degrees( double radians ) {
        double degrees = radians2degrees * radians;
        return degrees;
    }

    public static double radians( double degrees ) {
        double radians = degrees2radians * degrees;
        return radians;
    }


    // Conversion between 2 radians systems:
    //___________________________________________________________________________
    public static double R2W( double r ) {
        if( r < 0.0 ) {
            r = Maths.W + r; // e.g. when r = -3.14+6.28=~3.14 (correct)
        }
        return r;
    }

    public static double W2R( double r ) {
        if( r > Math.PI ) {
            r = r - Maths.W; // e.g. 6.2 -6.28=-0.8 (correct)
        }                    // 3.2 -6.28 = -3.08 (correct)
        return r;
    }


    // ASSUME AN ANTI-CLOCKWISE SPAN defined by limit1 and limit2.
    //___________________________________________________________________________
    public static boolean subtendsR( double from, double to, double radians ) {
        return subtendsW( R2W( from ), R2W( to ), R2W( radians ) );
    }

    public static boolean subtendsW( double from, double to, double radians ) {
        // assumes angles are in range 0:W=2pi
        if( from <= to ) {
            return( ( radians >= from ) && ( radians <= to ) );
        }
        // else: from > to ie segment crosses 0/w
        return( ( radians >= from ) || ( radians <= to ) );
    }

    public static boolean subtends1( double from, double to, double radians ) {
       return subtendsW( from, to, radians );
    }

    public static boolean overlapping1( double a1, double a2, double b1, double b2 ) {
        if( subtends1( a1, a2, b1 ) ) return true;
        if( subtends1( a1, a2, b2 ) ) return true;
        if( subtends1( b1, b2, a1 ) ) return true;
        if( subtends1( b1, b2, a2 ) ) return true;
        return false;
    }

    public static double[] minSector1( double a1, double a2, double b1, double b2 ) {
        double c1 = 0.0;
        double c2 = 0.0;

        if( Maths.overlapping1( a1, a2, b1, b2 ) ) {
            // extend to find max limits:
            if( a2 >= a1 ) { // range doesn't cross zero
                if( b2 >= b1 ) { // BOTH range don't cross zero
                    c1 = Math.min( a1, b1 );
                    c2 = Math.max( a2, b2 );
                }
                else { // b range crosses zero, value in interval 0:1
                    c1 = b1;
                    c2 = Math.max( a2, b2 );
                }
            }
            else { // a range crosses zero, value in interval 0:1
                if( b2 >= b1 ) { // b range doesn't cross zero
                    c1 = a1;
                    c2 = Math.max( a2, b2 );
                }
                else { // BOTH ranges cross zero
                    c1 = Math.min( a1, b1 );
                    c2 = Math.max( a2, b2 );
                }
            }
        }
        else { // Not overlapping.
            // try going either way; there are 2 gaps.
            double diffA = Maths.diff1( a2, b1 );
            double diffB = Maths.diff1( b2, a1 );

            if( diffA < diffB ) {
                c1 = a1;
                c2 = b2;
            }
            else {
                c1 = b1;
                c2 = a2;
            }
        }

        double[] sector = new double[ 2 ];
        sector[ 0 ] = c1;
        sector[ 1 ] = c2;
        return sector;
    }


    // diff"Radians"
    // given 2 numbers in the range -pi : pi, returns the difference as a positive
    // quantity in the range 0 <= n <= pi.
    //___________________________________________________________________________
    public static double diffR( double first, double second )
    {
        double diff = 0.0;

        if( first > second )
        {
          diff = first - second;
        }
        else
        {
          diff = second - first;
        }

        if( diff > Math.PI )
        {
          diff = Maths.W - diff;
        }

        return diff;
    };

    public static double diff1( double first, double second )
    {
        double diff = 0.0;

        if( first > second )
        {
          diff = first - second;
        }
        else
        {
          diff = second - first;
        }

        if( diff > 0.5 )
        {
          diff = 1 - diff;
        }

        return diff;
    };


    // mean and variance fns
    //___________________________________________________________________________
    public static double mean( double sum, double samples ) {
        return sum / samples;
    }

    // mean and variance fns: standard variance (sample would normalize by n-1
    // at the end). This function is not very accurate if the numbers are large
    // especially if the variance is comparatively small. If you subtract the
    // first value from all samples it will be more accurate.
    //___________________________________________________________________________
    public static double variance( double sum, double sumOfSq, double samples ) {
        if( samples <= 1.0 ) return 0.0;
        double normalizer = 1.0 / samples;
        double sumSq = sum * sum;
        double variance = sumOfSq - ( normalizer * sumSq );
               variance *= normalizer;
        return variance;
    }

    // circular number system add fn, result in range -pi:pi
    //___________________________________________________________________________
    public static double wrapW( double r ) {
        // if the value is positive we can just mod.
        r %= Maths.W; // wrap to range 0..W, if higher
        return r;
    }

    public static double wrapR( double r ) {

        // if we don't know how large or small the value is, we need this loop! :(
        while( r < -Math.PI ) {
            r += Maths.W;
        }
        while( r > Math.PI ) {
            r -= Maths.W;
        }

        return r;
    }

    public static double addR( double r, double r2 ) {
        return wrapR( r + r2 );
    }

    public static double subR( double r, double r2 ) {
        return wrapR( r - r2 );
    }


    // circular number system difference fn.
    //___________________________________________________________________________
    public static double diffCircular(
        double first,
        double second,
        double max )
    {
        double mid = max * 0.5;
        double diff = 0.0;

        if( first > second )
        {
          diff = first - second;
        }
        else
        {
          diff = second - first;
        }

        if( diff > mid )
        {
          diff = max - diff;
        }

        return diff;
    };


    // (circular number system), range overlap. Given 2 angles theta 1 and 2,
    // and 2 subtended bisectors, what is the occlusion of one angle by the other
    //___________________________________________________________________________
    public static double occlusion(
        double theta1,
        double subtendedBisector1,
        double theta2,
        double subtendedBisector2 )
    {
        double r1 = diffR( theta1, theta2 );
        double r2 = subtendedBisector1 + subtendedBisector2;
        double occlusion = r2 - r1;

        if( occlusion < 0.0 ) occlusion = 0.0; // zero means non-occluding.

        return occlusion;
    }

    // the angle in radians subtended by a circle of radius centred at a point
    // distance units away, divided by 2. Multiply by 2 to get the angle
    // subtended by the entire circle.
    //___________________________________________________________________________
    public static double subtendedBisector(
        double distance,
        double radius )
    {
        // radius = opposite
        // distance = adjacent
        // tan( theta ) = opposite / adjacent
        // theta = atan( opposite / adjacent )
        return Math.atan( radius / distance );
    }

    // Log-Sigmoid Function:
    //  y = f(x) = 1 ./ ( 1+ ( e.^(-x) ) )
    //  Output: 0..1 for input range -infinity <= x <= infinity
    //          0..1 for input range       ~-5 <= x <= ~5, in effect saturates
    //                                                     within this range
    //___________________________________________________________________________
    public static double logSigmoid( double x ) {
        double denominator = 1.0 + Math.pow( Math.E, -x );
        double y = 1.0 / denominator;
        return y;
    }


    // LogSigmoid but input range 0..1 is scaled to -5:5. This is kinda dodgy
    // but very practical :) It is so well conditioned it almost always works.
    //___________________________________________________________________________
    public static double logSigmoid1( double x ) {
        x *= 10.0;
        x -=  5.0;
        return logSigmoid( x );
    }


    // Should be like normal add (ie linear) but with saturation approaching 1.
    // This is roughly what you get with the (atan(x)/(pi/2)) function.
    //___________________________________________________________________________
    public static double saturate1( double x0, double x1 ) {

        double x = x0 + x1;

        if( x <  0.0 ) return 0.0;
        if( x >= 1.0 ) return 1.0; // max value

        // x is now 0:1
        // xsin = sin( (pi/2)*x ) ie
        // x==1   := pi/2 == sin(pi/2) == 1
        // x==0   := 0    == sin( 0  ) == 0
        return xsin( x );
    }

    public static double saturate1( double x ) {

        if( x <  0.0 ) return 0.0;
        if( x >= 1.0 ) return 1.0; // max value

        // x is now 0:1
        // xsin = sin( (pi/2)*x ) ie
        // x==1   := pi/2 == sin(pi/2) == 1
        // x==0   := 0    == sin( 0  ) == 0
        return xsin( x );
    }

    // Return an approx to sin(pi/2 * x) where -1 <= x <= 1.
    // In that range it has a max absolute error of 5e-9
    // according to Hastings, Approximations For Digital Computers.
    // from: Computer Approximations by Hart
    // http://stackoverflow.com/questions/523531/fast-transcendent-trigonometric-functions-for-java
    //___________________________________________________________________________
    static double xsin( double x ) {
        double x2 = x * x;
        return(  (  (  (   .00015148419
                         * x2
                         - .00467376557
                       )
                       * x2
                       + .07968967928
                    )
                    * x2
                    - .64596371106
                 )
                 * x2
                 + 1.57079631847
              )
              * x;
    }

    public static double wrap1( double x ) {

        while( x > 1.0 ) {
            x -= 1.0;
        }

        while( x < 0.0 ) {
            x += 1.0;
        }

        return x;
    }

    public static double circular1( double x ) {

        // 1.1 := 0.9
        // 2.3 := 0.7
        // -0.1 := 0.1
        // -1.2 := 0.2
        if( x >= 1.0 ) {
            while( x > 1.0 ) {
                x -= 1.0;
            }
            // 1.1:= 0.1
            // 2.3:=0.3
            x = 1.0 - x;
        }
        else if( x < 0.0 ) { // x < 0
            while( x < -1.0 ) {
                x += 1.0;
            }
            // -0.1 := -0.1
            // -1.2 := -0.2
            x = 0.0 -x;
        }

        return x;
    }

    // Clamps the value to unit range, interval 0..1
    //___________________________________________________________________________
    public static double clamp1( double x ) {
        if( x > 1.0 ) {
            x = 1.0;
        }
        else if( x < 0.0 ) {
            x = 0.0;
        }

        return x;
    }

    public static double clamp1m1( double x ) {
        if( x > 1.0 ) {
            x = 1.0;
        }
        else if( x < -1.0 ) {
            x = -1.0;
        }

        return x;
    }

    // Clamps the value to radians range, interval 0..1. DOES NOT WRAP.
    //___________________________________________________________________________
    public static double clampR( double t ) {
        if( t > Math.PI ) {
            t = Math.PI;
        }
        else if( t < -Math.PI ) {
            t = -Math.PI;
        }

        return t;
    }

    // Gaussian Function:
    //  y = f(x) = a * b
    //    a = 1 / sqrt( 2 * pi * sigma )
    //    b = e^( -(    xSq
    //               --------
    //               2sigmaSq ) )
    //___________________________________________________________________________
    public static double gaussian( double x, double sigma ) {
        double r1 = 1.0 / ( Math.sqrt( 2.0 * Math.PI * sigma ) );
        double r2 = 2.0 * sigma * sigma;
        double r3 = -( (x*x) / r2 );
        double r4 = Math.pow( Math.E, r3 ); // exp( r3 )
        double rG = r1 * r4;
        return rG;
    }

    // Fast pow (a^b) function approximation thanks to:
    // http://martin.ankerl.com/2007/10/04/optimized-pow-approximation-for-java-and-c-c/
    //___________________________________________________________________________
    public static double pow( final double a, final double b ) {
        final int x = (int) (Double.doubleToLongBits( a ) >> 32 );
        final int y = (int) (b * (x - 1072632447) + 1072632447);
        return Double.longBitsToDouble( ((long) y) << 32 );
    }


    // Fast exp (e^x) function approximation thanks to:
    // http://martin.ankerl.com/2007/02/11/optimized-exponential-functions-for-java/
    //___________________________________________________________________________
    public static double exp( double x ) {
        final long tmp = (long) (1512775 * x + 1072632447);
        return Double.longBitsToDouble( tmp << 32 );
    }

    // Fast natural log function approximation thanks to:
    // http://martin.ankerl.com/2007/02/11/optimized-exponential-functions-for-java/
    //___________________________________________________________________________
    public static double log( double x ) {
        return 6 * (x - 1) / ( x + 1 + 4 * ( Math.sqrt(x) ) );
    }

    // Should return a value 0.0 <= n < 1.0, taken from a normal distribution
    // approximated by averaging n samples.
    // Generates Irwin Hall distribution, aka uniform sum distribution.
    // This is an approximation of a normal distribution. 12 is *usually enough*
    //___________________________________________________________________________
    public static double randomNormal() {

        int n = 12;
        double r = 0.0;

        for( int i = 1; i < n; ++i ) // unroll please compiler?
        {
            r += Maths.random();
        }

        // truncate the interval from 0..N to 0..1
        // Mean was: n-(n/2), now: 0.5
        r /= (double)(n -1);

        return r;
    }

    
    // Random value between 0 and n-1.
    //___________________________________________________________________________
    public static int randomInt( int n ) {
        double r = ((double)(n)) * Maths.random();
        return (int)r;
    }

    // Random value in radians.
    //___________________________________________________________________________
    public static double randomR() {
        double r = ( Maths.random() * Maths.W ) - Math.PI; // -pi : pi
        return r;
    }

    public static double randomW() {
        double r = ( Maths.random() * Maths.W ); // -pi : pi
        return r;
    }

    public static double lerp1( double x, double x1, double alpha ) {
        double beta = 1.0f - alpha;

        x = alpha * x + beta * x1;

        return x;
    }

    public static double meanCircular( ArrayList< Double > values, double maxValue ) {

        // 1) Handle small lists of values:
        //_________________________________________________________________________
        int size = values.size();

        if( size < 2 ) {
            if( size == 0 ) {
                return 0.0;
            }
            // else: size is 1.

            return values.get( 0 ).doubleValue(); // return the only value we have.
        }


        // 2) Sort the values, or at least references to them!
        //_________________________________________________________________________
        ArrayList< Double > sorted = new ArrayList< Double >( values ); // different ordering, same Double objects.
        
        Collections.sort( sorted );


        // 3) Find the greatest empty span. First prepare for a loop thru all
        // elements.
        //_________________________________________________________________________
        double first = 0.0;
        double min  = 0.0;
        double max  = 0.0;
        double span = 0.0;

        double maxSpanMin = 0.0;
        double maxSpanMax = 0.0;
        double maxSpan    = 0.0;

        Iterator< Double > i = sorted.iterator();

        min = i.next(); // at least 1

        first = min;


        // 4) Loop thru all elements.
        //_________________________________________________________________________
        while( i.hasNext() ) {

            max = i.next();

            span = max - min; // set will have ordered them.

            if( span >= maxSpan )
            {
                maxSpanMin = min;
                maxSpanMax = max;
                maxSpan    = span;
            }

            min = max;
        }


        // 5) There is one final span, spanning zero/max:
        //_________________________________________________________________________
        // can't occur now
//        if( maxSpan == 0.0 ) {
//            return first;
//        }

        max = first;

        span = max + (maxValue - min);

        if( span >= maxSpan )
        {
            maxSpanMin = min;
            maxSpanMax = max;
            maxSpan    = span;
        }


        // 6) The other values may now be assessed by moving them so that they do
        //    not span 0/max. this is achieved by adding the difference between
        //    maxSpanMax and maxValue to all values. The largest empty span sits
        //    just before maxValue.
        //_________________________________________________________________________
        double offset = maxValue - maxSpanMax;
        double value = 0.0;
        double sum   = 0.0;

        i = sorted.iterator();

        while( i.hasNext() ) {

            value = i.next();
            value += offset;

            if( value >= maxValue )
            {
                value -= maxValue; // wrap around
            }

            sum += value;
        }

        sum /= (double)size;
        sum -= offset;

        if( sum < 0.0 )
        {
          sum += maxValue;
        }

        return sum;
    }

}
