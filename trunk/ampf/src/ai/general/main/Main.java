/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.main;

import ai.general.demos.rgb.DemoRGBSOM1MM;
import ai.general.demos.rps.DemoRockPaperScissors;
import ai.general.demos.lines.DemoLines;
import ai.general.demos.words.DemoWords;
import ai.general.util.RandomSingleton;

/**
 * Main function for running demos.
 * @author dave
 */
public class Main {

    public static void main( String[] args ) {

        if(    ( args.length < 1 )
            || ( args.length > 2 ) ) {
            System.err.println( "ERROR: One or two command line arguments are required." );
            help();
            System.exit( -1 );
        }

        int demo = 0;
        int max =  4;

        try {
            demo = Integer.parseInt( args[ 0 ] );
            if( demo < 1   ) throw new NumberFormatException();
            if( demo > max ) throw new NumberFormatException();
        }
        catch( NumberFormatException nfe ) {
            System.err.println( "ERROR: The 1st command line argument must be an integer between 1 and "+max+"." );
            help();
            System.exit( -1 );
        }

        if( args.length > 1 ) {
            String s = args[ 1 ];
            if( !s.equalsIgnoreCase( "R" ) ) {
                System.err.println( "ERROR: The 2nd command line argument must be 'R' or nothing." );
                help();
                System.exit( -1 );
            }
        }
        else { // if no 2nd argument, use fixed seed:
            RandomSingleton.seed( 1234567 );
        }


        // Hardcode the demo for debug convenience:
//        demo = 2;//10;//9;//8;//10;//7;

        switch( demo ) {
            case 1: DemoRGBSOM1MM.run( "Adaptive SOM MPF: RGB->SOM->1st Order Markov Model", 300 ); // fast, predictive
            case 2: DemoLines.run( "Adaptive SOM MPF: Moving Pattern Recognition Demo: SOM->RSOM pair", 100 );
//            case 3: DemoWords.run( "Adaptive SOM MPF: Words/VOMM", 300, false );
            case 3: DemoWords.run( "Adaptive SOM MPF: Words/VOMM", 300, true );
            case 4: DemoRockPaperScissors.run( "Adaptive SOM MPF: Rocks,Paper,Scissors" );
        }

        // EXPECTED OUTPUT ERROR:
        // 3 words-1st: 0.26+- 0.01 10,000 iters ie predicts 74%.
        // 3 words-variable: 0.21+-0.01 10,000 iters 5% improvement 78-79%
        // 4 rps - err<=0.06
    }

    private static void help() {
        String s = "Adaptive SOM-based Memory Prediction Framework Demos        \n"
                 + "------------------------------------------------------------\n"
                 + "Written by David Rawlinson, betw. Mar. '10 & Feb '11        \n"
                 + " with assistance from Gideon Kowadlo                        \n"
                 + "\n"
                 + "USAGE:\n"
                 + "------------------------------------------------------------\n"
                 + "java ampf.jar N [R]\n"
                 + "\n"
                 + "... where N is an integer between 1 and 6 and R is an\n"
                 + "optional parameter. If R is given, the random number \n"
                 + "generator will be seeded with the time, whereas by default\n"
                 + "a fixed seed is used. If fixed, you get exactly the same\n"
                 + "result every time the program is run.\n"
                 + "\n"
                 + "These demo programs and associated source code support our\n"
                 + "paper titled:\n"
                 + " 'Generating Adaptive Behaviour \n"
                 + "  within a Memory-Prediction Framework'.\n"
                 + "\n";
        System.err.println( s );
    }
}
