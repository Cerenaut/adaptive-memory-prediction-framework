/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.general.util.ui;

import java.util.HashMap;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

/**
 * Quick n dirty way to allow runtime user control of parameters..
 * 
 * @author dave
 */
public class ControlPanel extends JPanel {

    HashMap< String, JSlider > _labelledSliders = new HashMap< String, JSlider >();

    public ControlPanel() {
        setLayout( new BoxLayout( this, BoxLayout.PAGE_AXIS ) );
    }

    public void addUnit100SliderControl( int max, double initial, String label, ChangeListener cl ) {
        int i = (int)( initial * max );
        addSliderControl( 0, max, i, label, cl );
    }
    
    public void addSliderControl( int min, int max, int initial, String label, ChangeListener cl ) {
        JSlider s = new JSlider( JSlider.HORIZONTAL, min, max, initial );
        JLabel l = new JLabel();
        l.setText( label );
        s.addChangeListener( cl );
        s.setMajorTickSpacing( 10 );
        s.setMinorTickSpacing( 1 );
        s.setPaintTicks( true );
        s.setPaintLabels( true );
        add( l );
        add( s );

        _labelledSliders.put( label, s );
    }

    public JSlider getSliderControl( String label ) {
        return _labelledSliders.get( label );
    }
}
