/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.adaptive;

/**
 *
 * @author davidjr
 */
public class DerivativeSignal implements AdaptiveSignal {

    public double _r1 = 0.0;
    public double _r0 = 0.0;
    public double _dr = 0.0;
    public double _dMax = 0.0;
    
    AdaptiveSignal _as;

    public DerivativeSignal( AdaptiveSignal as, double dMax ) {
        _as = as;
        _dMax = dMax;
    }

    @Override public void update() {
        _r0 = _r1; // swap
        _r1 = _as.reward(); // 1 when good stuff is happening, 0 when bad
        _dr = _r1 - _r0;

        // scale it.. to max significance:
             if( _dr >   _dMax  ) _dr = (float)(  _dMax );
        else if( _dr < (-_dMax) ) _dr = (float)( -_dMax );

        _dr /= _dMax; // -0.05 / 0.1 = -0.5, or 0.05/0.1 = 0.5 OK
    }

    @Override public double reward() {
        return _dr;
    }

    @Override public boolean negative() {
        return true; // always true, as can decrease!
    }
}
