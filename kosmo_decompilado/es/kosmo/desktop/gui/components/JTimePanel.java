/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.toedter.calendar.JDateChooser
 */
package es.kosmo.desktop.gui.components;

import com.toedter.calendar.JDateChooser;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.jump.widgets.util.validating.NullDateChooserValidator;

public class JTimePanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DEFAULT_DATE_MASK = "##/##/####";
    public static final char DEFAULT_CHAR_MASK = '_';
    protected JDateChooser dateChooser;
    protected JComboBox hourComboBox;
    protected JComboBox minuteComboBox;
    protected JComboBox secondsComboBox;
    protected JDialog parent;

    public JTimePanel(JDialog parent, String label) {
        super(new FlowLayout());
        this.parent = parent;
        this.initialize(label);
    }

    public JTimePanel(JDialog parent) {
        super(new FlowLayout(3));
        this.parent = parent;
        this.initialize(null);
    }

    private void initialize(String label) {
        if (label != null) {
            this.add(new JLabel(label));
        }
        this.dateChooser = new JDateChooser(DEFAULT_DATE_FORMAT, DEFAULT_DATE_MASK, '_');
        this.dateChooser.setPreferredSize(new Dimension(100, 20));
        this.dateChooser.setMinimumSize(new Dimension(100, 20));
        if (this.parent != null) {
            this.dateChooser.setInputVerifier((InputVerifier)new NullDateChooserValidator(this.parent, this.dateChooser));
        }
        String[] hours = new String[24];
        int i = 0;
        while (i < 24) {
            hours[i] = Integer.toString(i);
            ++i;
        }
        this.hourComboBox = new JComboBox<String>(hours);
        String[] minutes = new String[60];
        int i2 = 0;
        while (i2 < 60) {
            minutes[i2] = Integer.toString(i2);
            ++i2;
        }
        this.minuteComboBox = new JComboBox<String>(minutes);
        String[] seconds = new String[60];
        int i3 = 0;
        while (i3 < 60) {
            seconds[i3] = Integer.toString(i3);
            ++i3;
        }
        this.secondsComboBox = new JComboBox<String>(seconds);
        this.add((Component)this.dateChooser);
        this.add(this.hourComboBox);
        this.add(new JLabel(":"));
        this.add(this.minuteComboBox);
        this.add(new JLabel(":"));
        this.add(this.secondsComboBox);
    }

    public boolean isInputValid() {
        if (this.isEnabled()) {
            return this.dateChooser.getInputVerifier().verify((JComponent)this.dateChooser);
        }
        return true;
    }

    public void clear() {
        this.dateChooser.setDate(null);
        this.hourComboBox.setSelectedIndex(0);
        this.minuteComboBox.setSelectedIndex(0);
        this.secondsComboBox.setSelectedIndex(0);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.dateChooser.setEnabled(enabled);
        this.hourComboBox.setEnabled(enabled);
        this.minuteComboBox.setEnabled(enabled);
        this.secondsComboBox.setEnabled(enabled);
    }

    public void refresh(Date time) {
        this.dateChooser.setDate(time);
        if (time == null) {
            this.hourComboBox.setSelectedIndex(0);
            this.minuteComboBox.setSelectedIndex(0);
            this.secondsComboBox.setSelectedIndex(0);
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        this.hourComboBox.setSelectedItem(Integer.toString(calendar.get(11)));
        this.minuteComboBox.setSelectedItem(Integer.toString(calendar.get(12)));
        this.secondsComboBox.setSelectedItem(Integer.toString(calendar.get(13)));
    }

    public Timestamp getTime() {
        Date date = this.dateChooser.getDate();
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(11, Integer.parseInt((String)this.hourComboBox.getSelectedItem()));
        calendar.set(12, Integer.parseInt((String)this.minuteComboBox.getSelectedItem()));
        calendar.set(13, Integer.parseInt((String)this.secondsComboBox.getSelectedItem()));
        return new Timestamp(calendar.getTime().getTime());
    }
}

