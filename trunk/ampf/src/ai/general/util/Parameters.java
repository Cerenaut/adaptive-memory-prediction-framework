/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.util;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Object for holding parameters for the programs.. keyed by String
 * 
 * @author dave
 */
public class Parameters implements Cloneable {

    HashMap< String, Double > _parameters = new HashMap< String, Double >();

    public Parameters() {
        // Nothing.
    }

    public void copy( Parameters p ) {
        Set< Entry< String, Double > > s = p._parameters.entrySet();

        for( Entry< String, Double > e : s ) {

            String key = (String)e.getKey();
            double value = ((Double)e.getValue()).doubleValue();

            if( _parameters.get( key ) != null ) {
                _parameters.put( key, value );
            }
        }
    }

    public void set( String key, double value ) {
        _parameters.put( key, value );
    }

    public void set( String key, Double value ) {
        _parameters.put( key, value );
    }

    public double get( String key ) {
        Double d = _parameters.get( key );

        if( d == null ) return 0.0;

        return d.doubleValue();
    }

    @Override public Parameters clone() {

        Parameters p = new Parameters();

        Set< Entry< String, Double > > s = _parameters.entrySet();

        for( Entry< String, Double > e : s ) {

            String key = (String)e.getKey();
            double value = ((Double)e.getValue()).doubleValue();

            p._parameters.put( key, value );
        }

        return p;
    }
}
