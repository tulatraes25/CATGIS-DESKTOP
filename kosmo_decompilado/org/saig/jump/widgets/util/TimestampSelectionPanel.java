/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.toedter.calendar.JDateChooser
 */
package org.saig.jump.widgets.util;

import com.toedter.calendar.JDateChooser;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.core.util.LocaleManager;

public class TimestampSelectionPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DEFAULT_DATE_MASK = "##/##/####";
    public static final char DEFAULT_CHAR_MASK = '_';
    private JDateChooser dateChooser;
    private JComboBox hourSelectionCombobox;
    private JComboBox minuteSelectionCombobox;
    private JComboBox secondSelectionCombobox;

    public TimestampSelectionPanel() {
        this.initialize();
    }

    public TimestampSelectionPanel(Timestamp currentTimestamp) {
        this();
        this.setDate(currentTimestamp);
    }

    private void initialize() {
        this.setLayout(new FlowLayout());
        this.dateChooser = new JDateChooser(DEFAULT_DATE_FORMAT, DEFAULT_DATE_MASK, '_');
        this.dateChooser.setMinimumSize(new Dimension(100, 20));
        this.dateChooser.setPreferredSize(new Dimension(100, 20));
        this.hourSelectionCombobox = new JComboBox<Integer>(this.generateNumberArray(24));
        this.minuteSelectionCombobox = new JComboBox<Integer>(this.generateNumberArray(60));
        this.secondSelectionCombobox = new JComboBox<Integer>(this.generateNumberArray(60));
        this.add((Component)this.dateChooser);
        this.add(new JLabel("  -  "));
        this.add(this.hourSelectionCombobox);
        this.add(new JLabel(" : "));
        this.add(this.minuteSelectionCombobox);
        this.add(new JLabel(" : "));
        this.add(this.secondSelectionCombobox);
    }

    public Timestamp getTimestamp() {
        Date selectedDate = this.dateChooser.getDate();
        if (selectedDate == null || !this.dateChooser.isEnabled()) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.dateChooser.getDate());
        calendar.set(11, (Integer)this.hourSelectionCombobox.getSelectedItem());
        calendar.set(12, (Integer)this.minuteSelectionCombobox.getSelectedItem());
        calendar.set(13, (Integer)this.secondSelectionCombobox.getSelectedItem());
        return new Timestamp(calendar.getTime().getTime());
    }

    private Integer[] generateNumberArray(int num) {
        Integer[] result = new Integer[num];
        int i = 0;
        while (i < num) {
            result[i] = new Integer(i);
            ++i;
        }
        return result;
    }

    public void setDate(Date newDate) {
        Calendar calendar = Calendar.getInstance(LocaleManager.getActiveLocale());
        calendar.setTime(newDate);
        this.dateChooser.setDate(newDate);
        this.hourSelectionCombobox.setSelectedItem(new Integer(calendar.get(11)));
        this.minuteSelectionCombobox.setSelectedItem(new Integer(calendar.get(12)));
        this.secondSelectionCombobox.setSelectedItem(new Integer(calendar.get(13)));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.dateChooser.setEnabled(enabled);
        this.hourSelectionCombobox.setEnabled(enabled);
        this.minuteSelectionCombobox.setEnabled(enabled);
        this.secondSelectionCombobox.setEnabled(enabled);
    }
}

