/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.util;

import ai.general.volumes.Dimensions;
import ai.general.volumes.Volume;

/**
 * Computes stats over a moving window, accurately.
 * 
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class MovingWindow {

    public Dimensions _d;
    public Volume _v;
    public int _offset = 0;
    public int _valid = 0;
    public long _step = 0;
    
    public MovingWindow() {
        resize( 100 );
    }
    
    public MovingWindow( int period ) {
        resize( period );
    }

    public void resize( int period ) {
        _d = new Dimensions( 1 );
        _d.configure( 0, period, Dimensions.TYPE_CARTESIAN, "e" );
        _v = new Volume( _d );
        _v.set( 0.0f );
        _offset = 0;
        _valid = 0;
    }

    public float get( int index ) {
        return _v._model[ index ];
    }
    
    public int newestIndex() {
        int offset = prevIndex( _offset );
        return offset;
    }

    public float newest() {
        int offset = newestIndex();
        float value = _v._model[ offset ]; // last update value
        return value;
    }

    public int oldestIndex() {
        return _offset;
    }
    
    public float oldest() {
        float value = _v._model[ _offset ]; // about to be overwritten
        return value;
    }

    public void reset() {
        resize( _v.volume() );
    }

    public int prevIndex( int index ) {
        int previous = index -1;
        if( previous < 0 ) {
            previous = _v._model.length -1;
        }
        return previous;
    }

    public int nextIndex( int index ) {
        int volume = _v.volume();
        ++index;
        index %= volume;
        return index;
    }

    public void update( float value ) {

        // update into _offset, then advance _offset
        int volume = _v.volume();

        _v._model[ _offset ] = value;

//        ++_offset;
//        _offset %= volume;
        _offset = nextIndex( _offset );

        if( _valid < volume ) {
            ++_valid;
        }

        ++_step;
    }

    public String summary() {
        double mean = mean();
        String s = "step=" + _step + ", err=" + mean;
        return s;
    }

    public double min() {
        return _v.min();
    }

    public double max() {
        return _v.max();
    }

    public double variance() {
        return _v.variance();
    }
    
    public double mean() {
        double sum = _v.sum();

        int volume = _v.volume();

        double qty = (double)volume;

        if( _valid < volume ) {
            qty = (double)_valid;
        }

        if( qty <= 0.0 ) {
            return 0.0;
        }

        double mean = sum / qty;
        return mean;
    }
}
