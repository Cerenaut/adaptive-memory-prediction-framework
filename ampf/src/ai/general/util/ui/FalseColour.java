/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.util.ui;

import java.awt.Color;

/**
 * Ways to represent a value in a single dimension in RGB, to aid visualisation.
 * Linear mappings..
 * @author dave
 */
public class FalseColour {

    public static Color UnitGrey2Grey( float grey ) { // assumes grey in interval 0..1

        grey *= 255.0f;
        int n = (int)grey;

        return new Color( n,n,n );
    }

    // Greyscale --> 2 Band as RGB
    //
    // Input:  Min------------------------------------------------------------Max
    //
    // Band 1: Min----------------------------------band1Max
    // Band 2:                   band2Min-------------------------------------Max
    //
    // RGB R:  255---------------------------------------000
    // RGB G:
    // RGB B:                    000------------------------------------------255
    //
    //___________________________________________________________________________
    public static Color UnitGrey2RB( float grey ) { // assumes grey in interval 0..1

        float band1Max = 0.6666f;
        float band2Min = 1.0f - band1Max;

        int g = 0;

        return Grey2RB( grey, 0.0f, 1.0f, band1Max, band2Min, g );
    }

    public static Color UnitGrey2RBA( float grey, int alpha ) { // assumes grey in interval 0..1

        float band1Max = 0.6666f;
        float band2Min = 1.0f - band1Max;

        int g = 0;

        return Grey2RBA( grey, 0.0f, 1.0f, band1Max, band2Min, g, alpha );
    }

    public static Color Grey2RB(
        float grey,
        float greyMin,
        float greyMax,
        float band1Max, // defines overlap
        float band2Min,
        int g ) {

        int r = 0;
        int b = 0;

        float rangeR = band1Max - greyMin; // in original units
        float rangeB = greyMax - band2Min; // in original units

        // Clamp grey to the specified range of interest.
        if( grey > greyMax ) {
            grey = greyMax;
        }

        if( grey < greyMin ) {
            grey = greyMin;
        }

        if( grey <= band1Max )
        {
            float ratioR = ( grey - greyMin ) / rangeR;
                  ratioR *= 255.0f; // scale from 0..1 to 0..255
            r = 255 - (int)ratioR;
        }

        if( grey >= band2Min ) {
            float ratioB = ( grey - band2Min ) / rangeB; //
                  ratioB *= 255.0f;
            b = (int)ratioB;
        }

//        if( r > 255 ) r = 255;
//        if( b > 255 ) b = 255;
//        System.out.println( "r="+r+" g="+g+" b="+b );
        Color c = new Color( r, g, b );
        
        return c;
    }

    public static Color Grey2RBA(
        float grey,
        float greyMin,
        float greyMax,
        float band1Max, // defines overlap
        float band2Min,
        int g,
        int a ) {

        int r = 0;
        int b = 0;

        float rangeR = band1Max - greyMin; // in original units
        float rangeB = greyMax - band2Min; // in original units

        // Clamp grey to the specified range of interest.
        if( grey > greyMax ) {
            grey = greyMax;
        }

        if( grey < greyMin ) {
            grey = greyMin;
        }

        if( grey <= band1Max )
        {
            float ratioR = ( grey - greyMin ) / rangeR;
                  ratioR *= 255.0f; // scale from 0..1 to 0..255
            r = 255 - (int)ratioR;
        }

        if( grey >= band2Min ) {
            float ratioB = ( grey - band2Min ) / rangeB; //
                  ratioB *= 255.0f;
            b = (int)ratioB;
        }

//        if( r > 255 ) r = 255;
//        if( b > 255 ) b = 255;
//        System.out.println( "r="+r+" g="+g+" b="+b );
        Color c = new Color( r, g, b, a );

        return c;
    }

    public static Color UnitGrey2RGB( float grey ) { // assumes grey in interval 0..1

        float band1Min = 0.0f;
        float band1Max = 0.5f;
        float band2Min = 0.25f;
        float band2Max = 0.75f;
        float band3Min = 0.5f;
        float band3Max = 1.0f;

//        return Grey2RGB( grey, band1Min, band1Max, band2Min, band2Max, band3Min, band3Max );
        return Grey2ARGB( 255, grey, band1Min, band1Max, band2Min, band2Max, band3Min, band3Max );
    }

    public static Color UnitGrey2ARGB( int alpha, float grey ) { // assumes grey in interval 0..1

        float band1Min = 0.0f;
        float band1Max = 0.5f;
        float band2Min = 0.25f;
        float band2Max = 0.75f;
        float band3Min = 0.5f;
        float band3Max = 1.0f;

        return Grey2ARGB( alpha, grey, band1Min, band1Max, band2Min, band2Max, band3Min, band3Max );
    }

    // Mono --> 3 Band as RGB
    //
    // Output: Min------------------------------------------------------------Max
    //
    // Band 1: Min---------------------------band1Max
    // Band 2:            band2Min---------------------------band2Max
    // Band 3:                          band3Min------------------------------Max
    //
    // RGB R:  255--------------------------------000
    // RGB G:             000----------------255------------------000
    // RGB B:                           000-----------------------------------255
    //
    //___________________________________________________________________________
    public static Color Grey2ARGB(
        int alpha,
        float grey,
        float band1Min,
        float band1Max, // defines overlap
        float band2Min,
        float band2Max,
        float band3Min,
        float band3Max ) {

        int r = 0;
        int g = 0;
        int b = 0;

        float rangeR = band1Max - band1Min; // in original units
        float rangeG = band2Max - band2Min;
        float rangeB = band3Max - band3Min; // in original units

        // Clamp grey to the specified range of interest.
        if( grey > band3Max ) {
            grey = band3Max;
        }

        if( grey < band1Min ) {
            grey = band1Min;
        }

        if( grey <= band1Max ) {
            float ratioR = ( grey - band1Min ) / rangeR;
                  ratioR *= 255.0f; // scale from 0..1 to 0..255
            r = 255 - (int)ratioR;
        }

        if(    ( grey >= band2Min )
            && ( grey <= band2Max ) ) {
            float ratioG = ( grey - band2Min ) / rangeG;
                  ratioG *= 255.0f; // scale from 0..1 to 0..255
            g = 255 - (int)ratioG;
        }

        if( grey >= band3Min ) {
            float ratioB = ( grey - band3Min ) / rangeB; //
                  ratioB *= 255.0f;
            b = (int)ratioB;
        }

//        if( r > 255 ) r = 255;
//        if( b > 255 ) b = 255;
//        System.out.println( "r="+r+" g="+g+" b="+b );
        Color c = new Color( b, g, r, alpha );

        return c;
    }
}
