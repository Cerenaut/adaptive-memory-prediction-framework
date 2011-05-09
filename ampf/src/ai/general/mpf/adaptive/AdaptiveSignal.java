/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.adaptive;

/**
 * Interface for an adaptive signal, ie something that objectively measures
 * internal reward or satisfaction from agent state.
 * @author dave
 */
public interface AdaptiveSignal {

    public void update();
    public double reward();
    public boolean negative();

}


