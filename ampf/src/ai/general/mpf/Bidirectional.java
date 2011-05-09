/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf;

/**
 * Something that can be run in FF (Feed-Forward) and FB (Feed-Back) passes.
 * @author dave
 */
public interface Bidirectional {

    public void ff();
    public void fb();
    
}
