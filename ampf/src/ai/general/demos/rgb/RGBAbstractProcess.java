/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.demos.rgb;

import ai.general.util.Maths;

/**
 *
 * @author David Rawlinson
 * @copyright David Rawlinson
 */
public abstract class RGBAbstractProcess {

    protected float[] _rgb      = new float[ 3 ];
    public    float[] _rgbBias1 = new float[ 3 ];
    public    float[] _rgbBias2 = new float[ 3 ];
    public    float[] _rgbNoise = new float[ 3 ];

    public boolean _normalNoise = false;
    public boolean _clamp = false;
    public boolean _clampWrap = false;
    public boolean _wrap = false;
    public boolean _circular = false;

    public RGBAbstractProcess() {
        setRGBValue  ( 0.0f );
        setNoise( 0.0f );
        setBias ( 0.0f );
    }

    public abstract float[] next();

    public void clamp1() {
        _rgb[ 0 ] = (float)Maths.clamp1( _rgb[ 0 ] );
        _rgb[ 1 ] = (float)Maths.clamp1( _rgb[ 1 ] );
        _rgb[ 2 ] = (float)Maths.clamp1( _rgb[ 2 ] );
    }

    public void clampWrap1() {
        float r = _rgb[ 0 ];
        float g = _rgb[ 1 ];
        float b = _rgb[ 2 ];

        // wrap to 0 at 1.
        while( r > 1.0f ) r -= 1.0f;
        while( g > 1.0f ) g -= 1.0f;
        while( b > 1.0f ) b -= 1.0f;

        // clamp on the lower bound
        if( r < 0.0f ) r = 0.0f;
        if( g < 0.0f ) g = 0.0f;
        if( b < 0.0f ) b = 0.0f;

        _rgb[ 0 ] = r;
        _rgb[ 1 ] = g;
        _rgb[ 2 ] = b;

//        _rgb[ 0 ] = (float)Maths.wrap1( _rgb[ 0 ] );
//        _rgb[ 1 ] = (float)Maths.wrap1( _rgb[ 1 ] );
//        _rgb[ 2 ] = (float)Maths.wrap1( _rgb[ 2 ] );
    }

    public void wrap1() {
        float r = _rgb[ 0 ];
        float g = _rgb[ 1 ];
        float b = _rgb[ 2 ];

        // wrap to 0 at 1.
        while( r > 1.0f ) r -= 1.0f;
        while( g > 1.0f ) g -= 1.0f;
        while( b > 1.0f ) b -= 1.0f;

        // wrap to 1 at 0
        while( r < 0.0f ) r += 1.0f;
        while( g < 0.0f ) g += 1.0f;
        while( b < 0.0f ) b += 1.0f;

        _rgb[ 0 ] = r;
        _rgb[ 1 ] = g;
        _rgb[ 2 ] = b;

//        _rgb[ 0 ] = (float)Maths.wrap1( _rgb[ 0 ] );
//        _rgb[ 1 ] = (float)Maths.wrap1( _rgb[ 1 ] );
//        _rgb[ 2 ] = (float)Maths.wrap1( _rgb[ 2 ] );
    }

    public void circular1() {
        _rgb[ 0 ] = (float)Maths.circular1( _rgb[ 0 ] );
        _rgb[ 1 ] = (float)Maths.circular1( _rgb[ 1 ] );
        _rgb[ 2 ] = (float)Maths.circular1( _rgb[ 2 ] );
    }

    public void setRGBUniform() {
        _rgb[ 0 ] = (float)Maths.random();
        _rgb[ 1 ] = (float)Maths.random();
        _rgb[ 2 ] = (float)Maths.random();
    }

    public void setRGBNormal() {
        _rgb[ 0 ] = (float)Maths.randomNormal();
        _rgb[ 1 ] = (float)Maths.randomNormal();
        _rgb[ 2 ] = (float)Maths.randomNormal();
    }

    public void setRGBValue( float value ) {
        _rgb[ 0 ] = value;
        _rgb[ 1 ] = value;
        _rgb[ 2 ] = value;
    }

    public void setRGBValue( float r, float g, float b ) {
        _rgb[ 0 ] = r;
        _rgb[ 1 ] = g;
        _rgb[ 2 ] = b;
    }

    public void setBias( float bias ) {
        _rgbBias1[ 0 ] = bias;
        _rgbBias1[ 1 ] = bias;
        _rgbBias1[ 2 ] = bias;
    }

    public void setBias( float r, float g, float b ) {
        _rgbBias1[ 0 ] = r;
        _rgbBias1[ 1 ] = g;
        _rgbBias1[ 2 ] = b;
    }

    public void setNoise( float noise ) {
        _rgbNoise[ 0 ] = noise;
        _rgbNoise[ 1 ] = noise;
        _rgbNoise[ 2 ] = noise;
    }

    public void setNoise( float r, float g, float b ) {
        _rgbNoise[ 0 ] = r;
        _rgbNoise[ 1 ] = g;
        _rgbNoise[ 2 ] = b;
    }

}
