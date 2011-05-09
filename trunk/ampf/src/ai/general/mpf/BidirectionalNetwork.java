/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf;

import ai.general.nn.FeedForwardNetwork;
import ai.general.nn.Schedule;
import ai.general.volumes.Volume;
import ai.general.volumes.VolumeMap;

/**
 * Base class for implementation of the Bidirectional interface using volumes.
 * Dimensions are same in FF/FB directions but are typically different on either
 * side of the network.
 * @author dave
 */
public abstract class BidirectionalNetwork extends FeedForwardNetwork implements Bidirectional {

    // vol dims same as forwards 
    public Volume _vib; // matrix input backwards ( making it an output )
    public Volume _vob; // matrix output backwards ( making it an input )

    public BidirectionalNetwork( VolumeMap vm, String name, Schedule s ) {
        super( vm, name, s );
    }

    public void configureFeedback() {
        _vib = new Volume( _dif );
        _vob = new Volume( _dof );
    }

}
