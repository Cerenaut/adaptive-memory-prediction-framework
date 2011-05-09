/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.demos.rps;

/**
 *
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public interface Strategy {

    public int move();
    public void outcome( int yourMove, int otherMove, int outcome );

}
