/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.util.ui;

import ai.general.util.AbstractPair;
import java.awt.Graphics2D;

/**
 * My GUIs in this project typically render everything at fixed size, hence I
 * want to know what size that is. And be able to repaint() them. And I always
 * use g2d functions.
 *
 * @author dave
 */
public interface Paintable {

    public abstract AbstractPair< Integer, Integer > size(); // return null if don't care
    public abstract void paint( Graphics2D g2d );

}
