/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.mpf;

import ai.general.mpf.mm.FirstOrderMM;
import ai.general.mpf.mm.MarkovModel;
import ai.general.mpf.mm.VariableOrderMM;
import ai.general.nn.Schedule;
import ai.general.nn.unsupervised.RSOM2D2;
import ai.general.nn.unsupervised.SOM2D2;
import ai.general.nn.unsupervised.WSOM2D;
import ai.general.volumes.Coordinate;
import ai.general.volumes.Volume;
import ai.general.volumes.VolumeMap;
import ai.general.util.Parameters;
import ai.general.util.Maths;
import ai.general.util.RandomSingleton;

/**
 * The basic building block for a SOM-MPF hierarchy. The unit basically consists
 * of spatial and temporal poolers, which are SOMs and RSOMs respectively. It
 * also has a predictor withint the unit.
 *
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class NeocorticalUnit extends BidirectionalNetwork {

    public SOM2D2      _sp; // spatial pooler/classifier
    public SOM2D2      _tp; // temporal pooler/classifier
    public MarkovModel _mm; // spatial predictor

    protected double _fbNoiseMagnitude = 0.0; // configured via sensitize()
    public Volume _fbn; // feedback sequence
    public boolean _strictlyOrthogonal = false;
    public Volume _sof; // spatial pooler orthogonal forwards
    public Volume _sob; // spatial pooler orthogonal backwards
    
    public NeocorticalUnit( // WSOM or no temporal pooler
        VolumeMap vm,
        String name,
        Schedule ss,
        Schedule st,
        int sInputs,
        int sSize,
        int tSize,
        int tWindow,
        boolean variableOrder ) {
        this( vm, name, ss, st, sInputs, sSize, tSize, tWindow, 0.0, false, false, variableOrder, false );
    }

    public NeocorticalUnit( // RSOM
        VolumeMap vm,
        String name,
        Schedule ss,
        Schedule st,
        int sInputs,
        int sSize,
        int tSize,
        double tAlpha,
        boolean variableOrder ) {
        this( vm, name, ss, st, sInputs, sSize, tSize, 0, tAlpha, false, false, variableOrder, true );
    }

    public NeocorticalUnit(
        VolumeMap vm,
        String name,
        Schedule ss,
        Schedule st,
        int sInputs,
        int sSize,
        int tSize,
        int tWindow,
        double tAlpha,
        boolean learnOnline,
        boolean predictSameState,
        boolean variableOrder,
        boolean recurrentSOM ) {

        super( vm, name, ss );

        int tInputs = sSize * sSize;

        createSOM( learnOnline, ss, sInputs, sSize );
        createMM( predictSameState, variableOrder );

        _dif = _sp._dif;
        _vif = _sp._vif;
        _vib = new Volume( _dif );//_vif._d ); // don't need to store in VM cos it's transient
        _fbn = new Volume( _sp._dof );
        _sof = new Volume( _sp._dof );
        _sob = new Volume( _sp._dof );
        _dof = _sp._dof;

        if( recurrentSOM ) {
            createRSOM( learnOnline, st, tInputs, tSize, tAlpha );
            _dof = _tp._dof;
        }
        else if( tWindow > 1 ) { // don't bother to do a double-classification of instantaneous stuff
            createWSOM( learnOnline, st, tInputs, tSize, tWindow );
            _dof = _tp._dof;
        }
        else {
            _dof = _sp._dof;
        }

        _vof = new Volume( _dof ); // orthogonal version of sp or tp vof
        _vob = new Volume( _dof ); //
        _vob.uniform(); // in case it's not connected
        
        double fbHigherMassDefault = 1.0;
        double fbUniformMassDefault = 3.5;
        double fbNoiseMagnitudeDefault = 0.05;
        double fbIterationsDefault = 1;
        double orthogonalizePowerDefault = 12.0;

        _p.set( "fb-higher-mass",             fbHigherMassDefault );
        _p.set( "fb-uniform-mass",            fbUniformMassDefault );
        _p.set( "fb-noise-magnitude-initial", fbNoiseMagnitudeDefault );
        _p.set( "fb-noise-magnitude-final",   fbNoiseMagnitudeDefault );
        _p.set( "fb-iterations",              fbIterationsDefault );
        _p.set( "orthogonalize-power",        orthogonalizePowerDefault );
    }

    public void configure(
        Parameters punit,
        Parameters psom,
        Parameters prsom,
        Parameters pmm ) {

        this._p.copy( punit );
        _sp ._p.copy( psom );
        _mm ._p.copy( pmm );

        if( _tp != null ) {
            _tp._p.copy( prsom );
        }
    }

    protected void createSOM(
        boolean learnOnline,
        Schedule s,
        int inputs,
        int size ) {
        _sp = new SOM2D2( _vm, _name+".som", s, inputs, size );
        _sp._learnOnline = learnOnline;
    }

    protected void createWSOM(
        boolean learnOnline,
        Schedule s,
        int inputs,
        int size,
        int window ) {
        _tp = new WSOM2D( _vm, _name+".wsom", s, inputs, size, window );
        _tp._learnOnline = learnOnline;
    }

    protected void createRSOM(
        boolean learnOnline,
        Schedule s,
        int inputs,
        int size,
        double alpha ) {
        _tp = new RSOM2D2( _vm, _name+".wsom", s, inputs, size );
        _tp._learnOnline = learnOnline;
        _tp._p.set( "rsom-alpha", alpha ); // 1.0 = ordinary SOM
    }

    protected void createMM( boolean predictSameState, boolean variableOrder ) {
        if( variableOrder ) {
            _mm = new VariableOrderMM( _vm, _name+".mm", _s, _sp._dof );
        }
        else {
            _mm = new    FirstOrderMM( _vm, _name+".mm", _s, _sp._dof );
        }
        _mm._predictSameState = predictSameState;
    }

    @Override public void ff() {

        sensitize( _s.elapsed() );
        
        _sp.ff();                       // update su classification

        if( _mm.order() == MarkovModel.ORDER_1 ) {
            orthogonalize( _sp._vof, _mm._vif );      // predict next state - should I inhibit before prediction?
        }
        else {
            // mm does its own orthogonalizing
            _mm._vif.copy( _sp._vof );      // predict next state - should I inhibit before prediction?
        }

        _mm.ff();

        if( _strictlyOrthogonal ) {
            orthogonalize( _sof, _sp._cSelectedFF );
        }
        else {
            orthogonalize( _sp._vof, _sof );
        }

        if( _tp != null ) {
            _tp._vif.copy( _sof );
            _tp.ff();
            _vof.copy( _tp._vof );
        }
        else {
            _vof.copy( _sof );
        }
    }

    protected void orthogonalize( Volume orthogonal, Coordinate c ) {
        orthogonal.set( 0.0f );
        orthogonal.set( c, 1.0f );
    }

    protected void orthogonalize( Volume parallel, Volume orthogonal ) {
        double power = _p.get( "orthogonalize-power" );
        orthogonal.copy( parallel );
        orthogonal.pow( power );
        orthogonal.scaleVolume( 1.0f );
    }

    @Override public void fb() {
        // normalize the unit's VOB and copy to relevant pooler VOB
        _vob.scaleVolume( 1.0f ); // it may not be normalized, see note below

        if( _tp != null ) {
            _tp._vob.copy( _vob );
        }
        else {
            _sp._vob.copy( _vob );
        }

        // do multiple passes if required:
        int iterations = (int)_p.get( "fb-iterations" );

        _vib.set( 0.0f );

        for( int i = 0; i < iterations; ++i ) {
            fbOnce();

            _vib.add( _sp._vib );
        }

        if( iterations > 1 ) {
            float scaling = 1.0f / (float)iterations;
            _vib.mul( scaling );
        }
// no no no should be scaled as vob, if necessary, cos vib may be final output ie not a PMF
//        _vib.scaleVolume( 1.0f );
    }

    protected void fbOnce() {
        double fbUniformMass = _p.get( "fb-uniform-mass" );
        double fbHigherMass = _p.get( "fb-higher-mass" );

//        _vob.scaleVolume( 1.0f ); redundant
        if( _tp != null ) {
            _tp.fb(); // is wrapper for this._vob, generate a random value from RSOM

            _fbn.copy( _tp._vib );

            addNoise( _fbn, _fbNoiseMagnitude );

            _sp._vob.copy( _fbn ); // very orthogonal!
        }
        // Now always have message in _sp._vob

        if( fbHigherMass > 0.0 ) { // when there's no higher node, no point listening to _tp or message from above..
            _sp._vob.scaleVolume( (float)fbHigherMass ); // as is scaleRange(d) on FF
        }
        else {
            _sp._vob.uniform();
        }

        // scale vol of mm?
        _sp._vob.mul( _mm._vof ); // combine with prediction
        _sp._vob.add( (float)( fbUniformMass / _sp._vob.volume() ) ); // ~3.5?
        _sp._vob.scaleVolume( 1.0f );
        _sp.fb();

        // add noise and normalize the SP vib:
        addNoise( _sp._vib, _fbNoiseMagnitude );
    }

    protected void addNoise( Volume v, double magnitude ) {

        int volume = v._model.length;

        int offset = 0;

        while( offset < volume ) {

            double value = v._model[ offset ];
            double random = ( RandomSingleton.random() -0.5 ) * 2.0; // ie [-0.5:0.5], scaled, becomes -1:1
            double noise = random * magnitude; // so zero if confident
            double noisy = value + noise;
            noisy = Maths.clamp1( noisy );

            float r = (float)noise;

            if(    Float.isInfinite( r )
                || Float.isNaN( r ) ) {
                r = 0.5f;
            }

            v._model[ offset ] = (float)noisy;

            ++offset;
        }
    }

    public void sensitize( double timeRatio ) {
        double fbNoiseMagnitudeInitial = _p.get( "fb-noise-magnitude-initial" );
        double fbNoiseMagnitudeFinal = _p.get( "fb-noise-magnitude-final" );
        double fbNoiseMagnitudeRange = fbNoiseMagnitudeFinal - fbNoiseMagnitudeInitial;
        _fbNoiseMagnitude = fbNoiseMagnitudeRange * timeRatio + fbNoiseMagnitudeInitial;
//        System.out.println( "n="+_fbNoiseMagnitude );
    }

}
