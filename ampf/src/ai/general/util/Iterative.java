/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.util;

/**
 * Something that gets done many times, with init/finish callbacks
 * 
 * @author dave
 */
public interface Iterative {

    public void pre(); // called once before iterating (step)
    public void step(); // Called many times (once per iteration)
    public void post(); // called once AFTER final step
    
}
