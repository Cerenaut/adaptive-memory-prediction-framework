/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.util;

import java.util.ArrayList;

/**
 * Use a thread to iterate some Iteratives.
 *
 * @author dave
 */
public class IterativeThread implements Runnable {

    public Callback _pre;
    public Callback _step;
    public Callback _post;
    
    protected ArrayList< Iterative > _iteratives = new ArrayList< Iterative >();
    protected boolean _continue = false;
    protected boolean _iterating = false;
    protected boolean _suspended = false;
    protected boolean _singleStep = false;
    public int _hesitation = 50; // 50ms per iter

    public IterativeThread() {
    }

    public IterativeThread( Iterative i ) {
        _iteratives.add( i );
    }

    public boolean iterate( Iterative i ) {
        if( _iterating ) {
            return false;
        }

        _iteratives.add( i );

        return true;
    }

    public synchronized void stop() {
        _continue = false; // TODO add
        _suspended = false;
    }

    public void stopWait() {
        stop();

        try {
            while( iterating() ) Thread.sleep( _hesitation );
        }
        catch( InterruptedException ie ) {
            System.err.print( ie );
        }
    }

    public synchronized boolean iterate() {
        boolean b = _continue;
        return b;
    }

    public synchronized boolean iterating() {
        boolean b = _iterating;
        return b;
    }

    public synchronized void suspend() {
        _suspended = true;
    }

    public void singleStep() {
        _singleStep = true;
        resume();
    }
    
    public void suspendWait() {
        suspend();

        try {
            while( !suspended() ) Thread.sleep( _hesitation );
        }
        catch( InterruptedException ie ) {
            System.err.print( ie );
        }
    }
    
    public synchronized void resume() {
        _suspended = false;
    }

    public synchronized boolean suspended() {
        boolean b = _suspended;
        return b;
    }

    @Override public void run() {

        _iterating = true;
        _continue = true;

        pre();

        while( iterate() ) {

            if( !suspended() ) {
                step();
                if( _singleStep ) {
                    _singleStep = false;
                    suspend();
                }
            }

            hesitate();
        }

        post();

        _iterating = false;
    }

    public synchronized void pre() {
        for( Iterative i : _iteratives ) {
            i.pre();
        }
        if( _pre != null ) _pre.call();
    }

    public synchronized void step() {
        for( Iterative i : _iteratives ) {
            i.step();
        }
        if( _step != null ) _step.call();
    }

    public synchronized void post() {
        for( Iterative i : _iteratives ) {
            i.post();
        }
        if( _post != null ) _post.call();
    }
    
    public void hesitate() {
        try {
            Thread.sleep( _hesitation );
        }
        catch( InterruptedException ie ) {
            System.err.print( ie );
        }
    } 
}
