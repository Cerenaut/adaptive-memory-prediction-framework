/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf.mm;

import ai.general.nn.Schedule;
import ai.general.volumes.Coordinate;
import ai.general.volumes.Dimensions;
import ai.general.volumes.Volume;
import ai.general.volumes.VolumeMap;
import ai.general.util.Maths;

/**
 * Produces a variable-order predictor by first-order learning, by tricking the
 * modelling into producing multiple first-order representations of states.
 * 
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class VariableOrderMM extends FirstOrderMM {

    public Volume _vif0; // unadulterated copy of vif
    public Volume _vof0; // unadulterated copy of vof
    public Volume _vli; // unpredicted bias
    public Volume _vui; // unpredicted bias

    public Volume _less;
    public Volume _more;
    public Volume _lost;
    public Volume _promotion;

    public VariableOrderMM( VolumeMap vm, String name, Schedule s, Dimensions input ) {
        super( vm, name, s, input );
        _vli = new Volume( input );
        _vui = new Volume( input );
        _vif0 = new Volume( input );
        _vof0 = new Volume( input );

        _less = new Volume( input );
        _more = new Volume( input );
        _lost = new Volume( input );
        _promotion = new Volume( input );

        _p.set( "inhibition-sigma", 1.7 );
        _p.set( "inhibition-delta-sigma", 0.5 );
    }

    @Override public int order() {
        return ORDER_N;
    }

    @Override public void ff() {
//        1. modify input with DoG.
//        2. normal/superclass
//        3. modify output with unpredicted inhibition
//        4. renormalize output
        preprocess();
        super.ff();
    }

    @Override public void predict() {
        super.predict();
        postprocess();
    }

    public void postprocess() {
        _vof0.copy( _vof );
        inhibitUnpredicted( _vif, _vui ); // give it the inhibited winner-take-all input
        _vof.mul( _vui ); // some are inhibited
        _vof.scaleVolume( 1.0f );
        
        // new - old = mass gained
        // old - new = mass lost
        _lost.subLessThan( _vof0, _vof, 0.0f, 0.0f ); // this is the distribution of lost mass.

        double lostMass = _lost.sum();
//System.out.println( "lost mass="+lostMass );

        locallyPromote( _lost, _promotion );

        _promotion.scaleVolume( (float)lostMass ); // this is the increase weighted by how much it deserves it

        _vof.add( _promotion );
        _vof.scaleVolume( 1.0f ); // this is the increase weighted by how much it deserves it
    }
    
    public void preprocess() {
        _vif0.copy( _vif );
        locallyInhibit( _vif, _vli );

        // mask those reduced/increased
        // old - new = decreased. eg 5-2 = 3
        // new - old = increased  eg
        _vif.mul( _vli );
        _vif.scaleVolume( 1.0f );

        _less.subLessThan( _vif0, _vif, 0.0f, 0.0f );
        _more.subLessThan( _vif, _vif0, 0.0f, 0.0f ); // pos. increase

        double massLost   = _less.sum();
        double massGained = _more.sum();
        double netMass = massGained - massLost;

        //how to distribute the extra?
        //mask the ones that decreased to zero
        // _more is 0 where it decreased otherwise the increase.
        // hence the extra should be distributed proportionally, ie most extra around the centre of the fn
        // If I add the extra mass and renormalize then I've ensured the mode has the same mass.
//System.out.println( "net mass="+netMass );
        _more.scaleVolume( (float)netMass ); // this is the increase weighted by how much it deserves it
        _vif.add( _more );
        _vif.scaleVolume( 1.0f ); // this is the increase weighted by how much it deserves it
    }
    
//Dave 2 Gids:
//Shopping List - comment this out to make it compile.
//1. Sigma,dSigma tuning. I don't want it to be a param. How can we relate it to SOM sigma?
//        Currently, sigma=0.25 SOM sigma and dSigma is constant. I think dSigma should
//        be related to sigma?
//2. SOM bias weight coeff. In NeocorticalUnit line 163 I add a constant value to dilute
//        the effect of the FB pass to the SOM. This param is quite sensitive...
//3. Idea - instead of only inhibiting the unpredicted model, I should inhibit it AND
//        promote its neighbours, under the assumption they're similar. This could be
//        achieved by applying a Laplacian of Gaussian function around anything inhibited
//        similar to the DoG inhibition.. in fact the sigmas might be related.
//4. Normalization / correctness of unprediction. There are a number of notes in the
//        code below RE inhibitUnpredicted() function because I'm not sure it's
//        correct yet. Should I normalize the unprediction influence by scaling by
//        the strongest weight inbound to the current model? But the inbound weights
//        are not normalized, only the outbound ones!? Should I use the nonlinear
//        Sigmoid weight influence (it seems better from a handful of runs)? Should
//        I add some noise to the inhibition for more robust solution given it's a
//        gradient descent search for the set of models to use to represent an unknown
//        number of sequences...

//Gids 2 Dave:
//1. Sigma,dSigma tuning. I don't want it to be a param. How can we relate it to SOM sigma?
//        Currently, sigma=0.25 SOM sigma and dSigma is constant. I think dSigma should
//        be related to sigma?
//
//            -> dsigma defines the width of the inhibitory region (95% of the mass within 2*stddev), i.e. what is the radius about which we want to inhibit
//            It should be 3 x sigma   (accentuate one model with sigma, and inhibit around it with radius of 1-2 models )
//
//2. SOM bias weight coeff. In NeocorticalUnit line 163 I add a constant value to dilute
//        the effect of the FB pass to the SOM. This param is quite sensitive...
//
//            -> is there a metric that can be measured as this parameter is modified, that must be maximised/minimised
//            such as sparseness of the 1mm transition weights (should be minimised to increase the number of states being used)
//              ---> then could do some online adaption
//
//3. Idea - instead of only inhibiting the unpredicted model, I should inhibit it AND
//        promote its neighbours, under the assumption they're similar. This could be
//        achieved by applying a Laplacian of Gaussian function around anything inhibited
//        similar to the DoG inhibition.. in fact the sigmas might be related.
//
//            -> i understood it to already maximally bias models that have an existing edge (i.e. multiply by 1.0)
//            there may be many models that don't have an edge, so you don't really want to bias toward all of their neighbours
//
//4. Normalization / correctness of unprediction. There are a number of notes in the
//        code below RE inhibitUnpredicted() function because I'm not sure it's
//        correct yet. Should I normalize the unprediction influence by scaling by
//        the strongest weight inbound to the current model? But the inbound weights
//        are not normalized, only the outbound ones!? Should I use the nonlinear
//        Sigmoid weight influence (it seems better from a handful of runs)? Should
//        I add some noise to the inhibition for more robust solution given it's a
//        gradient descent search for the set of models to use to represent an unknown
//        number of sequences...

//Dave 2 Gids
//VOMM not perfect but I could play with Sigma, Im happy enough.
//    2 problems:
//        a) RSOM is a POS. Unless its totally orthogonal it confuses sequences due
//        to the low influence of historical values. But it's good that it handles
//        sequences of unknown length? Does it?
//        b) It consistently predicts blanks for some reason - perhaps due to the
//        low-activation weights .. this is not a problem when the actual input
//        comes along, but it is a problem for the accuracy of prediction alone!
//        Often the blanks are the strongest prediction!!? I really dont understand this.

//    @Override public void normalizeWeights() {
//        _vw.scaleVolume( 1.0f );
//    }

    protected void locallyInhibit( Volume activation, Volume inhibition ) {

        double  sigma = _p.get( "inhibition-sigma" );
        double dSigma = _p.get( "inhibition-delta-sigma" );

//        System.out.println( "inh.sigma="+sigma+" dSigma="+dSigma );
        
        double sigmaWide   = sigma + dSigma;
        double sigmaNarrow = sigma;// - dSigma;

        Coordinate cMax = activation.maxAt();
        double max = activation.get( cMax );

        Coordinate c = activation.start();
        do {
            // subtract wide (+d) from narrow (-d) gaussians
            double distance = c.euclidean( cMax );

// DAVE TO GIDS: Should this be here? (or more exact form(discretized))
//            if( distance < 1.0 ) continue;

            double DoG = Maths.gaussian( distance, sigmaNarrow )
                       - Maths.gaussian( distance, sigmaWide );

            DoG *= max; // so no effect if zero

            inhibition.set( c, (float)DoG );
        }
        while( c.next() );

        inhibition.scaleRange( 0.0f, 1.0f );
    }

    protected void locallyPromote( Volume lostMass, Volume promotion ) {

        double  sigma = _p.get( "inhibition-sigma" );
        double dSigma = _p.get( "inhibition-delta-sigma" );

//        System.out.println( "inh.sigma="+sigma+" dSigma="+dSigma );
        double sigmaWide   = sigma + dSigma;
        double sigmaNarrow = sigma;// - dSigma;
        double verySmall = 0.0001;

        Coordinate c1 = lostMass.start();
        do {
            double weight = lostMass.get( c1 );

            if( weight < verySmall ) continue;
            
            Coordinate c2 = promotion.start();
            do {
                double distance = c2.euclidean( c1 );

                if( distance < 1.0 ) continue;

                // subtract narrow (-d) gaussians from wide (+d)
                double dog = Maths.gaussian( distance, sigmaWide )
                           - Maths.gaussian( distance, sigmaNarrow );

                dog *= weight; // so no effect if zero

                promotion.add( c2, (float)dog );
            }
            while( c2.next() );
        }
        while( c1.next() );
    }

    // inhibit at t+1, things that are highly active @ time t and ???
    // ??? how to detect the MIS-predictions?
    public void inhibitUnpredicted(
        Volume activation,
        Volume inhibition ) {
        // if models that precede models in v were NOT found, inhibit v
        float max = activation.max();
        float reciprocalMax = 1.0f / max;

        if( max <= 0.0f ) {
            inhibition.set( 1.0f );
//System.out.println( "nothing to inhibit" );
            return;
        }

        int sizeW = _dw.size( "w.i" );

        for( int w2 = 0; w2 < sizeW; ++w2 ) {

            float sum = 0.0f;
// why does the normalization below produce shitty results (locked into fixed cycle)
//            float sumWeights = 0.0f;
//            float wMax = 0.0f;
//
//            for( int w1 = 0; w1 < sizeW; ++w1 ) {
//                int offsetW = w1 * sizeW + w2; // P( transition i=w1,j=w2 | i=w1 )
//
//                float w = _vw._model[ offsetW ];
//
//                if( w > wMax ) wMax = w; // max weight inbound to w2 - is this normalized?
//            }
//
//            float reciprocalWMax = 1.0f / wMax;

            for( int w1 = 0; w1 < sizeW; ++w1 ) {
                int offsetW = w1 * sizeW + w2; // P( transition i=w1,j=w2 | i=w1 )

                float a = activation._model[ w1 ]; // before
// ?? not sure about this feature below:
                      a *= reciprocalMax; // so if A is max, no inhibition
                float w = _vw._model[ offsetW ];
//?scale by highest weight inbound?
//                      w *= reciprocalWMax;
// ?? the line below - is it better? Maybe...
                      w = (float)Maths.logSigmoid1( w );
                float hw = (1.0f-a) * w;
                      hw = 1.0f - hw;

                sum += hw; // assoc w1->w2
//                sumWeights += w;
                // w1 inhibited @ t+1 when there IS a relationship w2->w1, and
                // w2 is NOT active. In all other cases, w1 is NOT inhibited.
                // The intent is to force other models to form other relationships.
                // w1 = 1   1-w1 = 0    w=0.8     0.8*(1.0-1.0)=0.0    Strong rel, expected
                // w1 = 0   1-w1 = 1    w=0.8     0.8*(1.0-0.0)=0.8    String rel, NOT expected
                // w1 = 1   1-w1 = 0    w=0.0     0.0*(1.0-1.0)=0.0    No rel, expected
                // w1 = 0   1-w1 = 1    w=0.0     0.0*(1.0-1.0)=0.0    No rel, NOT expected
                // Then invert, cos we want to mul * 1 to keep it, * 0 to filter it.
            }
            
            inhibition._model[ w2 ] = sum;// + 0.02f; // not entirely removed
        }

        inhibition.scaleRange( 0.0f, 1.0f );
    }
    
}
