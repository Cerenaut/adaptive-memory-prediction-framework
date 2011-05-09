/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.util;

import ai.general.mpf.Bidirectional;
import ai.general.mpf.BidirectionalNetwork;
import java.util.ArrayList;

/**
 * A pyramid of networks, this class handles data copying/concatenation etc
 * between them, also merging PDFs in the case where there are >1 "higher"
 * networks connected to the lower level.
 *
 * This class effectively constructs a graph that looks like this:
 *   A      B
 *    \    /
 *     this
 *    /    \
 *   C      D
 *
 * This class doesn't call ff() or fb() on A/B/C/D because there may be other
 * inputs to A and B not yet copied e.g. from other relays, e.g. for unit B:
 *   A       B      G
 *    \    /  \    /
 *     this    that
 *    /    \   /   \
 *   C      D E     F
 *
 * The units should be called in a particular order, including this relay, e.g.
 * for FF traversal:
 *  C D E F this that A B G
 * This order should be reversed for FB traversal.
 *
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class BidirectionalRelay implements Bidirectional {

    public ArrayList< BidirectionalNetwork > _concreteNetworks = new ArrayList< BidirectionalNetwork >();
    public ArrayList< BidirectionalNetwork > _abstractNetworks = new ArrayList< BidirectionalNetwork >();

    public boolean _fbLerp = false;
    public float _fbWeightAccumulated = 0.5f;
    
    public BidirectionalRelay() {
        // Nothing.
    }

    @Override public void ff() {

        for( BidirectionalNetwork a : _abstractNetworks ) {
         
            int offset = 0;

            for( BidirectionalNetwork c : _concreteNetworks ) {

                int volume = c._dof.volume();

                a._vif.copyRange( c._vof, offset, 0, volume ); // this is offset, that is not

                offset += volume;
            }
        }
    }

    @Override public void fb() {

        boolean first = true;
        
        for( BidirectionalNetwork a : _abstractNetworks ) {

            int offset = 0;

            for( BidirectionalNetwork c : _concreteNetworks ) {

                int volume = c._dof.volume();

                if( first ) {
                    // TODO should be mul by some constant...
                    c._vob.copyRange( a._vib, 0, offset, volume ); // this not offset, that is.
                }
                else {
                    if( _fbLerp ) {
                        float weightThis = _fbWeightAccumulated; // TODO make it work for > 2 !!
                        float weightThat = 1.0f - weightThis;
                        c._vob. lerpRange( a._vib, 0, offset, volume, weightThis, weightThat ); // this not offset, that is.
                    }
                    else {
                        c._vob. mulRange( a._vib, 0, offset, volume ); // this not offset, that is.
                        c._vob.scaleVolume( 1.0f );
                    }
                }

                offset += volume;
            }

            first = false;
        }
    }
}
