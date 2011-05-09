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
public class FixedStrategy implements Strategy {

    public int _next = Moves.ROCK;

    public FixedStrategy() {

    }
    
    public int move() {
        int move = _next;

        if( move == Moves.ROCK ) {
            _next = Moves.PAPER;
        }
        else if( move == Moves.PAPER ) {
            _next = Moves.SCISSORS;
        }
        else { // SCISSORS
            _next = Moves.ROCK;
        }

        return move;
    }
    
    public void outcome( int yourMove, int otherMove, int outcome ) {
        // Doesn't care
    }

}
