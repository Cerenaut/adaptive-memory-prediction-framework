/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.mm;

import ai.general.volumes.Volume;
import ai.general.util.RandomSingleton;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Markov graph, fixed transition probabilities between sequences.
 * Once chosen a sequence plays in its entirety.
 * 
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class SequenceGraph {

    public HashMap< InputSequence, Double > _sequenceProbabilities = new HashMap< InputSequence, Double >();
    public InputSequence _is; // current
    public InputSequence _is0; // root

    public SequenceGraph() {
        
    }

    public void addSequence( InputSequence is, double p ) {
        _sequenceProbabilities.put( is, p );
    }

    public Volume next() {

        Volume v = null;

        while( v == null ) {

            if( _is == null ) {
                _is = nextSequence();
            }
            
            // try to advance current sequence:
            v = _is.next();

            if( v == null ) {
                if( _is != _is0 ) {
                    _is = _is0; // force a root sequence after any other
                }
                else {
                    _is = null; // clear sequence if it's finished
                }
            }
        }

        return v;
    }

    protected InputSequence nextSequence() {

        // roulette selection
        double sum = 0.0;

        Set< Entry< InputSequence, Double > > es = _sequenceProbabilities.entrySet();

        Iterator i = es.iterator();

        while( i.hasNext() ) {

            Object o = i.next();

            Entry< InputSequence, Double > e = (Entry< InputSequence, Double >)o;

            sum += e.getValue();
        }

        double random = RandomSingleton.random() * sum;

        // now go through again:
        sum = 0.0;

        i = es.iterator();

        while( i.hasNext() ) {

            Object o = i.next();

            Entry< InputSequence, Double > e = (Entry< InputSequence, Double >)o;

            sum += e.getValue();

            if( sum >= random ) {
                InputSequence is = e.getKey();

                return is;
            }
        }

        return null;
    }
}
