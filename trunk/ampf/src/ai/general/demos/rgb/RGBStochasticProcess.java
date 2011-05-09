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
public class RGBStochasticProcess extends RGBAbstractProcess {

    public RGBStochasticProcess() {
        super();
    }

    public float[] next() {

        double r = 0.0;
        double g = 0.0;
        double b = 0.0;

        // random noise:
        if( _normalNoise ) {
            r = Maths.randomNormal() -0.5;
            g = Maths.randomNormal() -0.5;
            b = Maths.randomNormal() -0.5;
        }
        else { // assume we want it uniform then, could just set noise scale to 0 if we want that.
            r = Maths.random() - 0.5;
            g = Maths.random() - 0.5;
            b = Maths.random() - 0.5;
        }

        // scale noise:
        double rScale = _rgbNoise[ 0 ];
        double gScale = _rgbNoise[ 1 ];
        double bScale = _rgbNoise[ 2 ];

        r *= rScale; // scale the random value
        g *= gScale;
        b *= bScale;

        // bias noise:
        double rBias1 = _rgbBias1[ 0 ];
        double gBias1 = _rgbBias1[ 1 ];
        double bBias1 = _rgbBias1[ 2 ];

        r += rBias1; // bias the random value
        g += gBias1;
        b += bBias1;

        double rBias2 = _rgbBias2[ 0 ];
        double gBias2 = _rgbBias2[ 1 ];
        double bBias2 = _rgbBias2[ 2 ];

        r += rBias2; // scale the random value
        g += gBias2;
        b += bBias2;

        _rgb[ 0 ] += (float)r;
        _rgb[ 1 ] += (float)g;
        _rgb[ 2 ] += (float)b;

        if( _clamp ) {
            clamp1();
        }
        else if( _clampWrap ) {
            clampWrap1();
        }
        else if( _wrap ) {
            wrap1();
        }
        else if( _circular ) {
            circular1();
        }
        return _rgb;
    }

}
