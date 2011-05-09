/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.util;

import ai.general.mpf.Bidirectional;
import ai.general.mpf.BidirectionalNetwork;
import java.util.ArrayList;

/**
 * Like a bi-directional relay, except this one forks rather than concatenates.
 * Isn't this just a special case that's functionally the same???
 * 
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class BidirectionalFork implements Bidirectional {

    public BidirectionalNetwork _concreteNetwork;
    public ArrayList< BidirectionalNetwork > _abstractNetworks = new ArrayList< BidirectionalNetwork >();

    public boolean _fbLerp = false;
    public float _fbWeightAccumulated = 0.5f;
    
    public BidirectionalFork() {
        // Nothing.
    }

    @Override public void ff() {

        for( BidirectionalNetwork a : _abstractNetworks ) {
         
            BidirectionalNetwork c = _concreteNetwork;

            int volume = c._dof.volume();
            a._vif.copyRange( c._vof, 0, 0, volume ); // nothing offset.
        }
    }

    @Override public void fb() {

        boolean first = true;
        
        for( BidirectionalNetwork a : _abstractNetworks ) {

            BidirectionalNetwork c = _concreteNetwork;

            int volume = c._dof.volume();

            if( first ) {
                // TODO should be mul by some constant...?
                c._vob.copyRange( a._vib, 0, 0, volume ); // this not offset, that is.
            }
            else {
                if( _fbLerp ) {
                    float weightThis = _fbWeightAccumulated; // TODO make it work for > 2 !!
                    float weightThat = 1.0f - weightThis;
                    c._vob. lerpRange( a._vib, 0, 0, volume, weightThis, weightThat ); // this not offset, that is.
                }
                else {
                    c._vob. mulRange( a._vib, 0, 0, volume ); // this not offset, that is.
                    c._vob.scaleVolume( 1.0f );
                }
            }

            first = false;
        }
    }
}
