/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.demos.rps;

import ai.general.demos.rgb.*;
import ai.general.mpf.NeocorticalUnit;
import ai.general.mpf.adaptive.AdaptiveSignal;
import ai.general.mpf.util.BidirectionalRelay;
import ai.general.nn.Schedule;
import ai.general.mpf.util.Traversal;
//import ai.strong.mpf.adaptive.DerivativeRewardCorrelator;
import ai.general.mpf.adaptive.DerivativeSignal;
import ai.general.mpf.adaptive.WindowRewardCorrelator;
import ai.general.mpf.util.DiscreteActuator;
import ai.general.mpf.util.FacadeNetwork;
import ai.general.volumes.Volume;
import ai.general.volumes.VolumeMap;
import ai.general.util.Iterative;
import ai.general.util.MovingWindow;
import ai.general.util.Maths;
import ai.general.util.Parameters;

/**
 * Rocks Paper Scissors Game
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class Game implements Iterative, AdaptiveSignal {

    public boolean _adaptive = true;//false;//true;//true;
                // turn adaptive back on without it err=0.5
//ok why isn't it perfect? why isn't the uniform mass related to something? I suspect this is why it can't get more accurate.
    Volume _gestureObserved;
    Volume _gestureExpected0;
    Volume _gestureExpected1;
    int _moveThis = Moves.ROCK;
    int _moveThat = Moves.ROCK;

    public Strategy _s;
    public Gestures _g;
//    public int _move = Moves.ROCK;
    
    public MovingWindow _error = new MovingWindow();
    public RGBStochasticProcess _process = new RGBStochasticProcess();
    public DerivativeSignal _ds;
    public VolumeMap _vm = new VolumeMap();
    public Schedule _s1;
    public Schedule _s2;
    public Schedule _s3;
    public Schedule _s4;

    public FacadeNetwork _fn;
    public DiscreteActuator _da;
    public BidirectionalRelay _br1;
    public NeocorticalUnit _nu1;
    public BidirectionalRelay _br2;
    public WindowRewardCorrelator _rc;
    public BidirectionalRelay _br3;
    public NeocorticalUnit _nu2;
    public Traversal _t; // [fn+da]-br1-nu1-br2-rc-br3-nu3


    public Game( Strategy s, boolean variableOrder ) {

        super();

        int schedule1 = 1000;
        int schedule2 = 1500;
        int schedule3 = 2000;
        int schedule4 = 2500;
        int errorPeriod = 1000;

        _error.resize( errorPeriod );
        _s1 = Schedule.Create( schedule1 );
        _s2 = Schedule.Create( schedule2 );
        _s3 = Schedule.Create( schedule3 );
        _s4 = Schedule.Create( schedule4 );

        _s = s;//new FixedStrategy();
        _g = new Gestures( _s );


        // Neocortical Unit:
        _fn = new FacadeNetwork( _vm, "fn", _s1, _g._vi._d );
        _da = new DiscreteActuator( _vm, "da", _s1, 3 );

        _gestureObserved  = new Volume( _g._vi._d );
        _gestureExpected0 = new Volume( _g._vi._d );
        _gestureExpected1 = new Volume( _g._vi._d );

        int senses    = _fn._dof.volume();
        int actuators = _da._dof.volume();

        int sInputs1 = senses + actuators;
        int spSize1 = 6;//15;
//if( variableOrder ) {
//    spSize1 = 8;
//}
        int tpSize1 = 0;//6;
        int tWindow1 = 1;//3;//2;
//        boolean variableOrder = false;//true;//false;
        int sInputs2 = spSize1 * spSize1;
        int spSize2 = 6;//15;
        int tpSize2 = 0;
        int tWindow2 = 1;//2;
        
        _nu1 = new NeocorticalUnit( _vm, "nu1", _s1, _s2, sInputs1, spSize1, tpSize1, tWindow1, variableOrder );
        _nu2 = new NeocorticalUnit( _vm, "nu2", _s3, _s4, sInputs2, spSize2, tpSize2, tWindow2, false );

        Parameters punit = new Parameters();
        punit.set( "fb-noise-magnitude-initial", 0.9 );
        punit.set( "fb-noise-magnitude-final", 0.01 ); // was: 0.0
        punit.set( "fb-higher-mass", 1.0 );//0.5 );
        punit.set( "fb-uniform-mass", 0.5 );//0.1 );//0.8 );//1.5 ); // vomm=3.5 0.8 seemed better than 0.1?
        punit.set( "fb-iterations", 1.0 );
        punit.set( "orthogonalize-power", 6.0 );//12.0 );
//fb stuck cos not combined with reality
        Parameters psom = new Parameters();
        psom.set( "som-sigma-initial", 18.0 );
        psom.set( "som-sigma-final", 0.1 );//0.5 0.4 0.3 );
        psom.set( "som-roulette-power", 14.0 ); // default

        Parameters prsom = new Parameters();
        prsom.set( "som-sigma-activation", 1.7 ); // iff online
        prsom.set( "som-sigma-initial", 12.0 );
        prsom.set( "som-sigma-final", 0.2 );
        prsom.set( "som-roulette-power", 12.0 ); // default

// PARAM MOD LIST from best config
// fb-uniform-mass 1 0.8:= 0.1
// pmm.finallearningrate 0.05 := 0.01
        Parameters pmm = new Parameters();
        pmm.set( "initial-learning-rate", 0.95 );
        pmm.set(   "final-learning-rate", 0.05 );//0.05 );
        pmm.set(          "weight-decay", 0.95 );//0.999 );//0.95 );
//        pmm.set(   "final-learning-rate", 0.01 );//0.05 );
//        pmm.set(          "weight-decay", 0.999 );//0.95 );

//        pmm.set( "inhibition-sigma", 0.425 ); // 1.7 * 0.25 = 0.425
//        pmm.set( "inhibition-delta-sigma", 0.5 ); // 1.5 0.5

//if( variableOrder ) {
//    punit.set( "fb-noise-magnitude-final", 0.01 ); // was: 0.0
//    punit.set( "fb-higher-mass", 2.0 );//0.5 );
//    punit.set( "fb-uniform-mass", 0.5 );//0.1 );//0.8 );//1.5 ); // vomm=3.5 0.8 seemed better than 0.1?
//    punit.set( "orthogonalize-power", 6.0 );//12.0 );
//
//    psom.set( "som-sigma-final", 0.1 );//0.5 0.4 0.3 );
//    psom.set( "som-roulette-power", 14.0 ); // default
//}
        _nu1.configure( punit, psom, prsom, pmm );

        punit.set( "fb-higher-mass", 0.0 ); // not connected!
        punit.set( "fb-uniform-mass", 0.1 );//0.6 );//1.0 ); still ok @ 0.1

        _nu2.configure( punit, psom, prsom, pmm );

        // Adaptive Signal
        _ds = new DerivativeSignal( this, 1.0 );

        // Reward Correlator (thalamus)
        int window = 1;
        int delay = 0; // for RPS, no delay
        double rcAdaptiveLearningRate = 0.05;
        
        _rc = new WindowRewardCorrelator( _vm, "rc", _s2, _nu1._dof, _ds, window, delay );
        _rc._p.set( "adaptive-learning-rate", rcAdaptiveLearningRate );


        // Link the hierarchy together
        _br1 = new BidirectionalRelay();
        _br1._concreteNetworks.add( _fn );
        _br1._concreteNetworks.add( _da );
        _br1._abstractNetworks.add( _nu1 );

        _br2 = new BidirectionalRelay();
        _br2._concreteNetworks.add( _nu1 );
        _br2._abstractNetworks.add( _rc );

        _br3 = new BidirectionalRelay();
        _br3._concreteNetworks.add( _rc );
        _br3._abstractNetworks.add( _nu2 );

        _t = new Traversal();
        _t._sequence.add( _fn );
        _t._sequence.add( _da );
        _t._sequence.add( _br1 );
        _t._sequence.add( _nu1 );

        if( _adaptive ) {
            _t._sequence.add( _br2 );
            _t._sequence.add( _rc );
            _t._sequence.add( _br3 );
            _t._sequence.add( _nu2 );
        }
    }

    public double move2unit( int move ) {
        double unit  = (double)move / (3.0-1.0); // 0,0.5,1
               unit = Maths.clamp1( unit ); // just in case it isn't
        return unit;
    }

    public double unit2move( double unit ) {
        unit = Maths.clamp1( unit ); // just in case it isn't
        unit *= (3.0-1.0); // 0:=0 0.5:=1 1:=2
        int move = (int)unit;
        return move;
    }

    protected void updateSignals( int outcome ) {
        // update measurement of world
        _outcome = outcome;
            update(); // added: update this reward value ie now, momentary
        _ds.update(); // update derivative reward from this reward value
        _error.update( (float)( 1.0 - reward() ) );

        System.out.println( "Error(n)="+_error.mean()+" Error(1)="+(1.0-reward()) );
    }


    ////////////////////////////////////////////////////////////////////////////
    // Adaptive Signal BEGIN
    ////////////////////////////////////////////////////////////////////////////
    public int _outcome = Moves.DRAW;
    public double _reward = 0.0;
    
    @Override public void update() {
        if( _outcome == Moves.DRAW ) {
            _reward = 0.5;
        }
        else if( _outcome == Moves.LOSE ) {
            _reward = 0.0;
        }
        else if( _outcome == Moves.WIN ) {
            _reward = 1.0;
        }
    }

    @Override public double reward() {
        return _reward;
    }

    @Override public boolean negative() {
        return false;
    }
    ////////////////////////////////////////////////////////////////////////////
    // Adaptive Signal END
    ////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////
    // Iterative BEGIN
    ////////////////////////////////////////////////////////////////////////////
    @Override public void pre() {}
    @Override public void post() {}
    @Override public void step() {

        _moveThis = _da.actuation(); // I generate a move WHILE...
        _moveThat = _s.move(); // he generates a move

        int outcomeThis = Moves.outcome( _moveThis, _moveThat );
        int outcomeThat = Moves.outcome( _moveThat, _moveThis );

        _s.outcome( _moveThat, _moveThis, outcomeThat );

        updateSchedules();
        updateSignals( outcomeThis );

        // I can't know his CURRENT move? Yes I can, as long as I use my already generated move
        _gestureObserved.copy( _g.get( _moveThat ) );
        _fn._vof.copy( _gestureObserved ); // store the opponent's move.
        // My move is already stored in _da.
        _t.ff();
        _t.fb();
        _gestureExpected0.copy( _gestureExpected1 ); // I predict his move AND...
        _gestureExpected1.copy( _fn._vib ); // I predict his move AND...
    }
    ////////////////////////////////////////////////////////////////////////////
    // Iterative END
    ////////////////////////////////////////////////////////////////////////////

    protected void updateSchedules() {
        _s1.call();
        _s2.call();
        _s3.call();
        _s4.call();
        System.out.println( "s1.elapsed="+_s1.elapsed()+" "+_s2.elapsed()+" "+_s3.elapsed()+" "+_s4.elapsed() );
    }
}

