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
            RandomSingleton.seed( 1234 );
        }


        // Hardcode the demo for debug convenience:
//        demo = 2;//10;//9;//8;//10;//7; other demos removed

        String prefix = "Adaptive SOM MPF: ";

        switch( demo ) {
            case 1: DemoRGBSOM1MM.run( prefix+"RGB->SOM->1st Order Markov Model", 300 ); break;
            case 2: DemoLines.run( prefix+"Moving Line Recognition Demo: SOM->RSOM pair", 100 ); break;
            case 3: DemoWords.run( prefix+"Word Recognition (VoMM)", 300, false ); break;
            case 4: DemoRockPaperScissors.run( prefix+"Rocks,Paper,Scissors Game" ); break;
            default: help();
        }

        // EXPECTED OUTPUTS:
        // ---------------------------------------------------------------------
        // #1 rgb:       @ T=2950, err=0.15499847412109374
        // #2 lines:     @ T=11926, described models exist
        // #3 words-1mm: @ T=9912, Error(m)=0.26287353515625      73.72% correct
        // #3 words-vmm: @ T=10308, Error(m)=0.19597596740722656  80.40% correct
        // $4 rps:       @ T=61809, Error(n)=0.0175
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
                 + "... where N is an integer between 1 and 4 and R is an\n"
                 + "optional parameter. If R is given, the random number \n"
                 + "generator will be seeded with the time, whereas by default\n"
                 + "a fixed seed is used. If fixed, you get exactly the same\n"
                 + "result every time the program is run.\n"
                 + "\n"
                 + "These demo programs and associated source code support our\n"
                 + "paper titled:\n"
                 + " 'Generating Adaptive Behaviour \n"
                 + "  within a Memory-Prediction Framework'.\n"
                 + "\n"
                 + "While the demos are running, the following keyboard commands\n"
                 + "make it easier to track what is happening:\n"
                 + "  'f' Makes the demos iterate faster\n"
                 + "  'l' Makes the demos iterate slower\n"
                 + "  'p' Pauses the demos until\n"
                 + "  'r' Resumes the demos;\n"
                 + "  's' Runs one iteration (step) and then pauses.\n";

        System.err.println( s );
    }
}
