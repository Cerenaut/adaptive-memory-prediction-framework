/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.volumes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Container for volumes. They're stored by name, so they can be identified and
 * retrieved. This also forms a single point so that all registered volumes can
 * be saved. This allows entire systems to be serialized and reloaded.
 *
 * @author dave
 */
public class VolumeMap {

    public HashMap< String, Volume > _vm = new HashMap< String, Volume >();

    public VolumeMap() {
        // Nothing
    }

    public void put( String s, Volume v ) {
        _vm.put( s, v );
    }

    public Volume get( String s ) {
        return _vm.get( s );
    }

    public String filename( String volumeName, String prefix ) {
        String filename = prefix + "." + volumeName + ".vol";
        return filename;
    }

    public void write( String filenamePrefix ) throws IOException {

        Set< String > keys = _vm.keySet();
        Iterator< String > i = keys.iterator();

        while( i.hasNext() ) {

            String s = i.next();

            Volume v = _vm.get( s );

            String volumeFilename = filename( s, filenamePrefix );

System.out.println( "saving "+volumeFilename );
            v.write( volumeFilename );
        }
    }

    public void read( String filenamePrefix ) throws IOException {

        Set< String > keys = _vm.keySet();
        Iterator< String > i = keys.iterator();

        while( i.hasNext() ) {

            String s = i.next();

            Volume v = _vm.get( s );

            String volumeFilename = filename( s, filenamePrefix );

System.out.println( "loading "+volumeFilename );
            v.read( volumeFilename );
        }
    }
}
