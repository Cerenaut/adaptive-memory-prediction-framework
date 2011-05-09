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
public class Moves {

    public static final int ROCK     = 0;
    public static final int PAPER    = 1;
    public static final int SCISSORS = 2;

    public static final int WIN  = 0;
    public static final int LOSE = 1;
    public static final int DRAW = 2;

    public static int outcome( int move1, int move2 ) {

        if( move1 == move2 ) {
            return DRAW;
        }

        if( move1 == ROCK ) {
            if( move2 == PAPER ) {
                return LOSE;
            }
            else { // must be SCISSORS
                return WIN;
            }
        }
        else if( move1 == PAPER ) {
            if( move2 == ROCK ) {
                return WIN;
            }
            else { // must be SCISSORS
                return LOSE;
            }
        }
        else { // if( move1 == SCISSORS ) {
            if( move2 == ROCK ) {
                return LOSE;
            }
            else { // must be PAPER
                return WIN;
            }
        }
    }
}
