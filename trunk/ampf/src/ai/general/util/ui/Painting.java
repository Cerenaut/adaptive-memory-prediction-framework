/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.util.ui;

import ai.general.mpf.NeocorticalUnit;
import ai.general.mpf.mm.InputSequence;
import ai.general.mpf.mm.SequenceGraph;
import ai.general.nn.unsupervised.SOM2D2;
import ai.general.volumes.Coordinate;
import ai.general.volumes.Dimensions;
import ai.general.volumes.Volume;
import ai.general.util.AbstractPair;
import ai.general.util.Maths;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Utility functions for painting the structures for the demos.
 * 
 * @author David Rawlinson
 * @copyright David Rawlinson 
 */
public class Painting {

    public static int g( int cellSize, int gapSize, int column ) {
        int x = gapSize * column + ( cellSize * (column-1) );
        return x;
    }

//    public static int gy( int cellSize, int gapSize, int row ) {
//        int y = gapSize * row + ( cellSize * (row-1) );
//        return y;
//    }

    public static void paintRSOM2d1(
        Graphics2D g2d,
        int xRSOM,
        int yRSOM,
        int wModel,
        int hModel,
        int gap,
        SOM2D2 som,
        SOM2D2 rsom,
        boolean scaleRange,
        double significanceThreshold,
        boolean threshold ) {

        int sizeJ = som._vw._d.size( "som.j" );
        int sizeI = som._vw._d.size( "som.i" );
        int sizeW = som._vw._d.size( "som.w" );

        int sizeJR = rsom._vw._d.size( "som.j" );
        int sizeIR = rsom._vw._d.size( "som.i" );
        int sizeWR = rsom._vw._d.size( "som.w" );

        int wSOM = wModel * sizeI;
        int hSOM = hModel * sizeJ;

//        wModel = ((wModel * sizeIR) - (gap * (sizeIR+1))) / sizeIR;
//        hModel = ((hModel * sizeJR) - (gap * (sizeJR+1))) / sizeJR;

//        wModel = ((wModel * sizeIR) - (gap * (sizeIR+1))) / sizeIR;
//        hModel = ((hModel * sizeJR) - (gap * (sizeJR+1))) / sizeJR;
        wModel -= ((gap * (sizeIR+1)) / sizeI);
        hModel -= ((gap * (sizeJR+1)) / sizeJ);

//        int wModelR = wSOM;// * sizeIR;
//        int hModelR = hSOM;// * sizeJR;
//
//        wModelR = ((wModelR * sizeIR) - (gap * (sizeIR+1))) / sizeIR;
//        hModelR = ((hModelR * sizeJR) - (gap * (sizeJR+1))) / sizeJR;

        int wRSOM = wSOM * sizeIR + (gap * (sizeIR+1));
        int hRSOM = hSOM * sizeJR + (gap * (sizeJR+1));

        g2d.setColor( Color.LIGHT_GRAY );
        g2d.fillRect( xRSOM, yRSOM, wRSOM, hRSOM );

        int offset = 0;
        int x = xRSOM + gap;
        int y = yRSOM + gap;

        for( int j = 0; j < sizeJR; ++j ) {

            x = xRSOM + gap;

            for( int i = 0; i < sizeIR; ++i ) {

//                paintWeightedSOM( g2d, wModel, hModel, x,y, gap, som, rsom._vw._model, offset );
                paintWeightedSOM2d1( g2d, x,y, wModel, hModel, gap, som, rsom._vw._model, offset, scaleRange, significanceThreshold, threshold );

                offset += sizeWR;

                x += wSOM;
                x += gap;//( 2 * gap );
            }

            y += hSOM;
            y += gap;//( 2 * gap );
        }

    }

    public static void paintWeightedSOM2d1(
        Graphics2D g2d,
        int xSOM,
        int ySOM,
        int wModel,
        int hModel,
        int gap,
        SOM2D2 som,
        float[] weights,
        int weightsOffset,
        boolean scaleRange,
        double significanceThreshold,
        boolean threshold ) {

        int sizeJ = som._vw._d.size( "som.j" );
        int sizeI = som._vw._d.size( "som.i" );
        int sizeW = som._vw._d.size( "som.w" );

        //need to normalize the RSOM weights
        int sizeV = sizeI * sizeJ;

        Dimensions d = new Dimensions( 1 );
                   d.configure( 0, sizeV, Dimensions.TYPE_CARTESIAN, "?" );
        Volume v = new Volume( d );
        
        for( int n = 0; n < sizeV; ++n ) {
            v._model[ n ] = weights[ weightsOffset +n ];
        }

        if( scaleRange ) {
            v.scaleRange( 0.0f, 1.0f );
//v.scaleVolume( 1.0f );
        }

        int wSOM = wModel * sizeI;
        int hSOM = hModel * sizeJ;

        wModel = ((wModel * sizeI) - (gap * (sizeI+1))) / sizeI;
        hModel = ((hModel * sizeJ) - (gap * (sizeJ+1))) / sizeJ;
//
//        int wSOM = wModel * sizeI
//              + gap * sizeI
//              + gap * (sizeI+1);
//        int hSOM = hModel * sizeJ
//              + gap * sizeJ
//              + gap * (sizeJ+1);

        g2d.setColor( Color.DARK_GRAY );
        g2d.fillRect( xSOM, ySOM, wSOM, hSOM );

        int offset = 0;
        int x = xSOM;// + gap;
        int y = ySOM;// + gap;
        int mw = (int)( Math.sqrt( sizeW ) );
        int mh = sizeW / mw;

        for( int j = 0; j < sizeJ; ++j ) {

            x = xSOM + gap;

            for( int i = 0; i < sizeI; ++i ) {

                paint2d1( g2d, x,y, wModel, hModel, som._vw._model, offset, mw, mh, null );

                double value = v._model[ sizeI * j +i ];
                       value = Maths.clamp1( value );

                int a = (int)( value * 255.0 );

                if( threshold ) {
                    a = 0;
                    if( value > significanceThreshold ) {
                        a = 255;
                    }
                }

                g2d.setColor( new Color( 0,a,0 ) );
                g2d.drawRect( x,y, wModel,hModel );

                ++weightsOffset;

                offset += sizeW;

                x += wModel;
                x += ( 2 * gap );
            }

            y += hModel;
            y += ( 2 * gap );
        }
    }

    public static void paintSOM2d1(
        Graphics2D g2d,
        int xSOM,
        int ySOM,
        int wModel,
        int hModel,
        int gap,
        SOM2D2 som ) {

        int sizeJ = som._vw._d.size( "som.j" );
        int sizeI = som._vw._d.size( "som.i" );
        int sizeW = som._vw._d.size( "som.w" );

        int wSOM = wModel * sizeI;
        int hSOM = hModel * sizeJ;

        wModel = ((wModel * sizeI) - (gap * (sizeI+1))) / sizeI;
        hModel = ((hModel * sizeJ) - (gap * (sizeJ+1))) / sizeJ;

        g2d.setColor( Color.DARK_GRAY );
        g2d.fillRect( xSOM, ySOM, wSOM, hSOM );

        int offset = 0;
        int xModel = xSOM + gap;
        int yModel = ySOM + gap;
        int mw = (int)( Math.sqrt( sizeW ) );
        int mh = sizeW / mw;

        for( int j = 0; j < sizeJ; ++j ) {

            xModel = xSOM + gap;

            for( int i = 0; i < sizeI; ++i ) {

//                paintPattern( g2d, wModel, hModel, xModel,yModel, som._vw._model, offset, sizeW );
                paint2d1( g2d, xModel,yModel, wModel,hModel, som._vw._model, offset, mw, mh, null );

                offset += sizeW;

                xModel += wModel;
                xModel += gap;//( 2 * gap );
            }

            yModel += hModel;
            yModel += gap;//( 2 * gap );
        }
    }

    public static void paintSOM2dRGB(
        Graphics2D g2d,
        int xSOM,
        int ySOM,
        int wModel,
        int hModel,
        int gap,
        SOM2D2 som,
        Color labelColour,
        String label ) {

        paintSOM2dRGB( g2d, xSOM,ySOM, wModel,hModel, gap, som );

        g2d.setColor( labelColour );
        g2d.drawString( label, xSOM, ySOM -gap );
    }

    public static int offsetPositionError(
//        int position,
        int matrixSize,
        int idealSize ) {
        int unitSize = idealSize / matrixSize;
        int roundingError = idealSize - ( matrixSize * unitSize );

//        int offsetPosition = position + (roundingErrorX >>1);
//        return offsetPosition;
        return( roundingError >> 1 );
    }

    public static void paintSOM2dRGB(
        Graphics2D g2d,
        int xSOM,
        int ySOM,
        int wModel,
        int hModel,
        int gap,
        SOM2D2 som ) {
        paintSOM2dRGB( g2d, xSOM, ySOM, wModel, hModel, gap, som, 0 );
    }

    public static void paintSOM2dRGB(
        Graphics2D g2d,
        int xSOM,
        int ySOM,
        int wModel,
        int hModel,
        int gap,
        SOM2D2 som,
        int offset ) {

        int sizeJ = som._vw._d.size( "som.j" );
        int sizeI = som._vw._d.size( "som.i" );
        int sizeW = som._vw._d.size( "som.w" );

        int wSOM = wModel * sizeI;
        int hSOM = hModel * sizeJ;

        wModel = ((wModel * sizeI) - (gap * (sizeI+1))) / sizeI;
        hModel = ((hModel * sizeJ) - (gap * (sizeJ+1))) / sizeJ;

        g2d.setColor( Color.DARK_GRAY );
        g2d.fillRect( xSOM, ySOM, wSOM, hSOM );

//        int offset = 0;
        int xModel = xSOM + gap;
        int yModel = ySOM + gap;
        int mw = (int)( Math.sqrt( sizeW ) );
        int mh = sizeW / mw;

        for( int j = 0; j < sizeJ; ++j ) {

            xModel = xSOM + gap;

            for( int i = 0; i < sizeI; ++i ) {

//                paintPattern( g2d, wModel, hModel, xModel,yModel, som._vw._model, offset, sizeW );
                int r = (int)( som._vw._model[ offset +0 ] * 255.0f );
                int g = (int)( som._vw._model[ offset +1 ] * 255.0f );
                int b = (int)( som._vw._model[ offset +2 ] * 255.0f );

                g2d.setColor( new Color( r,g,b ) );
                g2d.fillRect( xModel, yModel, wModel, hModel );
                g2d.setColor( Color.DARK_GRAY );
                g2d.drawRect( xModel, yModel, wModel, hModel );

                offset += sizeW;

                xModel += wModel;
                xModel += gap;
            }

            yModel += hModel;
            yModel += gap;
        }
    }

    public static void paintGraph(
        Graphics2D g2d,
        int x,
        int y,
        int w,
        int h,
        int gap,
        SequenceGraph sg ) {

        Set< Entry< InputSequence, Double > > es = sg._sequenceProbabilities.entrySet();

        int sequences = es.size();
        int maxLength = 0;

        Iterator i = es.iterator();

        while( i.hasNext() ) {

            Object o = i.next();

            Entry< InputSequence, Double > e = (Entry< InputSequence, Double >)o;

            int length = e.getKey().length();

            if( length > maxLength ) {
                maxLength = length;
            }
        }

        int wg = w * maxLength
               + gap * (maxLength+1);
        int hg = h * sequences
               + gap * sequences
               + gap * (sequences+1);

        g2d.setColor( Color.DARK_GRAY );
        g2d.fillRect( x, y, wg, hg );

        x = x + gap;
        y = y + gap;

        i = es.iterator();

        while( i.hasNext() ) {

            Object o = i.next();

            Entry< InputSequence, Double > e = (Entry< InputSequence, Double >)o;

            paintSequence( g2d, x,y, w,h, gap, e.getKey() );

            y += ( h + ( 2 * gap ) );
        }
    }

    public static void paintSequence(
        Graphics2D g2d,
        int x,
        int y,
        int w,
        int h,
        int gap,
        InputSequence is ) {

        int length = is.length();

        for( int s = 0; s < length; ++s ) {
            Volume v = is.get( s );

//            paintPattern( g2d, xModel,ySOM, wModel, hModel, v._model, 0, v.volume() );
            paint2d1( g2d, x,y, w, h, v, null );

            x += ( w + gap );
        }
    }

    public static void paintCoordinate2d(
        Graphics2D g2d,
        int x,
        int y,
        int w,
        int h,
//        int gap,
        Coordinate c,
        Color colour ) {
//v.randomize();
        if( c == null ) return;

        int volume = c._d.volume();
        int mw = (int)( Math.sqrt( volume ) );
        int mh = volume / mw;

//        wSOM = wSOM * mw
//          + gap * mw
//          + gap * (mw+1);
//        hSOM = hSOM * mh
//          + gap * mh
//          + gap * (mh+1);

        int vw = w / mw;
        int vh = h / mh;

        int roundingErrorX = w - ( mw * vw );
        int roundingErrorY = h - ( mh * vh );

        x += (roundingErrorX >>1);
        y += (roundingErrorY >>1); // centre the voxels

        int indexX = 1;
        int indexY = 0;
        int cx = c._indices[ indexX ];
        int cy = c._indices[ indexY ];

        g2d.setColor( colour );

        int vx = cx * vw + x;
        int vy = cy * vh + y;

        g2d.drawRect( vx+1, vy+1, vw-2, vh-2 );
    }

    public static void paint2d1(
        Graphics2D g2d,
        int x,
        int y,
        int w,
        int h,
        Volume v,
        boolean scaleRange ) {

        Volume t = v;

        if( scaleRange ) {
            t = new Volume( v );
            t.scaleRange( 0.0f, 1.0f );
        }

        paint2d1( g2d, x,y, w,h, t, null );
    }

    public static void paint2d1(
        Graphics2D g2d,
        int x,
        int y,
        int w,
        int h,
        Volume v, 
        boolean scaleRange,
        Color labelColour,
        String label ) {
        Volume t = v;

        if( scaleRange ) {
            t = new Volume( v );
            t.scaleRange( 0.0f, 1.0f );
        }

        paint2d1( g2d, x,y, w,h, t, null );

        g2d.setColor( labelColour );
        g2d.drawString( label, x, y );
    }

    public static void paint2d1(
        Graphics2D g2d,
        int x,
        int y,
        int w,
        int h,
        Volume v ) {
        paint2d1( g2d, x, y, w, h, v, null );
    }

    public static void paint2d1(
        Graphics2D g2d,
        int x,
        int y,
        int w,
        int h,
        Volume v,
        String label ) {
//v.randomize();
        int volume = v.volume();
        int mw = (int)( Math.sqrt( volume ) );
        int mh = volume / mw;
        paint2d1( g2d, x, y, w, h, v._model, 0, mw, mh, label );
    }

    public static void paint2d1(
        Graphics2D g2d,
        int x,
        int y,
        int w,
        int h,
        float[] matrix,
        int offset,
        int mw,
        int mh,
        String label ) {

        int vw = w / mw;
        int vh = h / mh;

        int roundingErrorX = w - ( mw * vw );
        int roundingErrorY = h - ( mh * vh );

        x += (roundingErrorX >>1);
        y += (roundingErrorY >>1); // centre the voxels

//        int offset = 0;

        for( int my = 0; my < mh; ++my ) {
            for( int mx = 0; mx < mw; ++mx ) {

                // COLOUR
                double value = matrix[ offset ];

                ++offset;

                if( value > 1.0 ) {
                    value = 1.0;
                }

                if( value < 0.0 ) {
                    value = 0.0;
                }

                int a = (int)( value * 255.0 );
                g2d.setColor( new Color( a,a,a ) );

                int xv = mx * vw + x;
                int yv = my * vh + y;

                g2d.fillRect( xv, yv, vw, vh );
                g2d.setColor( Color.DARK_GRAY );
                g2d.drawRect( xv, yv, vw, vh );
            }
        }

        if( label != null ) {
            g2d.setColor( Color.BLACK );
            g2d.drawString( label, x +5, y +h -5 );
            g2d.setColor( Color.WHITE );
            g2d.drawString( label, x +6, y +h -4 );
        }
    }

    public static void paint2dC(
        Graphics2D g2d,
        int x,
        int y,
        int w,
        int h,
        Volume v,
        String label ) {
//v.randomize();
        int volume = v.volume();
        int mw = (int)( Math.sqrt( volume ) );
        int mh = volume / mw;
        paint2dC( g2d, x, y, w, h, v._model, mw, mh, label );
    }
    
    public static void paint2dC(
        Graphics2D g2d,
        int x,
        int y,
        int w,
        int h,
        float[] matrix,
        int mw,
        int mh,
        String label ) {

        int vw = w / mw;
        int vh = h / mh;

        int roundingErrorX = w - ( mw * vw );
        int roundingErrorY = h - ( mh * vh );

        x += (roundingErrorX >>1);
        y += (roundingErrorY >>1); // centre the voxels

        int offset = 0;

        for( int my = 0; my < mh; ++my ) {
            for( int mx = 0; mx < mw; ++mx ) {

                // COLOUR
                double value = matrix[ offset ];

                ++offset;

                value += 1.0; // 0:2
                value *= 0.5; // 0:1

                if( value > 1.0 ) {
                    value = 1.0;
                }

                if( value < 0.0 ) {
                    value = 0.0;
                }

                int a = (int)( value * 255.0 );
                g2d.setColor( new Color( a,a,a ) );

                int xv = mx * vw + x;
                int yv = my * vh + y;

                g2d.fillRect( xv, yv, vw, vh );
                g2d.setColor( Color.DARK_GRAY );
                g2d.drawRect( xv, yv, vw, vh );
            }
        }

        if( label != null ) {
            g2d.setColor( Color.BLACK );
            g2d.drawString( label, x +5, y +h -5 );
            g2d.setColor( Color.WHITE );
            g2d.drawString( label, x +6, y +h -4 );
        }
    }

    public static void paintRGB1(
        Graphics2D g2d,
        int x,
        int y,
        int w,
        int h,
        Volume rgb,
        String label,
        Color labelColour ) {
        paintRGB1( g2d, x, y, w, h, rgb._model, 0, label, labelColour );
    }

    public static void paintRGB1(
        Graphics2D g2d,
        int x,
        int y,
        int w,
        int h,
        float[] rgb,
        int offset ) {
        paintRGB1( g2d, x, y, w, h, rgb, offset, null, null );
    }

    public static void paintRGB1(
        Graphics2D g2d,
        int x,
        int y,
        int w,
        int h,
        float[] rgb,
        int offset,
        String label,
        Color labelColour ) {

        int r = (int)( rgb[ offset +0 ] * 255.0 );
        int g = (int)( rgb[ offset +1 ] * 255.0 );
        int b = (int)( rgb[ offset +2 ] * 255.0 );

        Color c = new Color( r, g, b );

        paintBox( g2d, x, y, w, h, c, label, labelColour );
    }
    
    public static void paintScalar1(
        Graphics2D g2d,
        int x,
        int y,
        int w,
        int h,
        double scalar,
        String label,
        Color labelColour ) {

        scalar = Maths.clamp1( scalar );
        int r = (int)( scalar * 255.0 );
        int g = (int)( scalar * 255.0 );
        int b = (int)( scalar * 255.0 );

        Color c = new Color( r, g, b );

        paintBox( g2d, x, y, w, h, c, label, labelColour );
    }

    public static void paintBox(
        Graphics2D g2d,
        int x,
        int y,
        int w,
        int h,
        Color c,
        String label,
        Color labelColour ) {

        g2d.setColor( c );
        g2d.fillRect( x, y, w, h );
        g2d.setColor( Color.BLACK );
//        Stroke s = g2d.getStroke();
//        g2d.setStroke( new BasicStroke( 3 ) );
        g2d.drawRect( x, y, w, h );
//        g2d.setStroke( s );

        if( label != null ) {
            g2d.drawString( label, x, y );
            g2d.setColor( labelColour );
            g2d.drawString( label, x, y );
        }
    }

    public static void paintBackground( Graphics2D g2d, Paintable p, Color c ) {
        AbstractPair< Integer, Integer > ap = p.size();

        if( ap == null ) {
            return;
        }

        g2d.setBackground( c );
        g2d.clearRect( 0, 0, ap._first, ap._second );
    }

    public static void maxSet(
        HashMap< Float, Coordinate > hm,
        float value,
        Coordinate c,
        int size ) {

        hm.put( value, c );

        if( hm.size() <= size ) {
            return;
        }

        Set< Entry< Float, Coordinate > > es = hm.entrySet();

        float min = Float.MAX_VALUE;

        Iterator i = es.iterator();

        while( i.hasNext() ) {

            Entry< Float, Coordinate > e = (Entry< Float, Coordinate >)( i.next() );

            float key = e.getKey();
//System.out.print( ",k="+key);
            if( key < min ) {
                min = key;
            }
        }
//System.out.println( ", removing k_min="+min);
        hm.remove( min );
    }

    public static InputSequence mostSignificant( NeocorticalUnit nu, int n ) {

        HashMap< Float, Coordinate > hm = new HashMap< Float, Coordinate >();

        Coordinate ct = nu._tp._vof.maxAt();
//System.out.println( ct.toString() );
        String dj = "o.1";
        String di = "o.0";

        int sizeJ = nu._tp._vw._d.size( "som.j" );
        int sizeI = nu._tp._vw._d.size( "som.i" );
        int sizeW = nu._tp._vw._d.size( "som.w" );
        int sizeJW = sizeJ * sizeW;
        int sizeIs = nu._sp._vw._d.size("som.i");

        int j = ct._indices[ ct._d.index( dj ) ];
        int i = ct._indices[ ct._d.index( di ) ];

        int offset = ( i * sizeJW ) + ( j * sizeW );

        for( int w = 0; w < sizeW; ++w ) {
            float x = nu._tp._vw._model[ offset ];

            // offset 2d = i*size +j
            int is = w / sizeIs;
            int js = w % sizeIs; // wSOM is the coords.
            Coordinate cs = nu._sp._vof.start();
            cs.set( dj, js );
            cs.set( di, is );

            maxSet( hm, x, cs, n );

            ++offset;
        }

        // now I know the N most significant SU models in the TU.
        // invert them back to their original forms.
        InputSequence is = new InputSequence( nu._sp._dif, n );

        sizeJ = nu._sp._vw._d.size( "som.j" );
        sizeI = nu._sp._vw._d.size( "som.i" );
        sizeW = nu._sp._vw._d.size( "som.w" );
        sizeJW  = sizeJ * sizeW;

        int order = 0;

        Set< Entry< Float, Coordinate > > es = hm.entrySet();

        Iterator ies = es.iterator();

        while( ies.hasNext() ) {

            Entry< Float, Coordinate > e = (Entry< Float, Coordinate >)( ies.next() );

            Coordinate cs = e.getValue();

            j = cs._indices[ cs._d.index( dj ) ];
            i = cs._indices[ cs._d.index( di ) ];
//fix this
            offset = ( i * sizeJW ) + ( j * sizeW );

            Volume v = is.get( order );

            for( int w = 0; w < sizeW; ++w ) {
                v._model[ w ] = nu._sp._vw._model[ offset ];
                ++offset;
            }

            ++order;
        }

        return is;
    }

}
