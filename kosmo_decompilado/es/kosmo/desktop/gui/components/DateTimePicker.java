/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdesktop.swingx.JXDatePicker
 *  org.jdesktop.swingx.calendar.DateSelectionModel
 *  org.jdesktop.swingx.calendar.SingleDaySelectionModel
 */
package es.kosmo.desktop.gui.components;

import java.awt.Color;
import java.awt.FlowLayout;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.calendar.DateSelectionModel;
import org.jdesktop.swingx.calendar.SingleDaySelectionModel;
import org.saig.jump.lang.I18N;

public class DateTimePicker
extends JXDatePicker {
    private static final long serialVersionUID = 1L;
    private JSpinner timeSpinner;
    private JPanel timePanel;
    private DateFormat timeFormat;

    public DateTimePicker() {
        this.getMonthView().setSelectionModel((DateSelectionModel)new SingleDaySelectionModel());
    }

    public DateTimePicker(Date d) {
        this();
        this.setDate(d);
    }

    public void commitEdit() throws ParseException {
        this.commitTime();
        super.commitEdit();
    }

    public void cancelEdit() {
        super.cancelEdit();
        this.setTimeSpinners();
    }

    public JPanel getLinkPanel() {
        super.getLinkPanel();
        if (this.timePanel == null) {
            this.timePanel = this.createTimePanel();
        }
        this.setTimeSpinners();
        return this.timePanel;
    }

    private JPanel createTimePanel() {
        JPanel newPanel = new JPanel();
        newPanel.setLayout(new FlowLayout());
        SpinnerDateModel dateModel = new SpinnerDateModel();
        this.timeSpinner = new JSpinner(dateModel);
        if (this.timeFormat == null) {
            this.timeFormat = DateFormat.getTimeInstance(3);
        }
        this.updateTextFieldFormat();
        newPanel.add(new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.gui.components.DateTimePicker.Time")) + ":"));
        newPanel.add(this.timeSpinner);
        newPanel.setBackground(Color.WHITE);
        return newPanel;
    }

    private void updateTextFieldFormat() {
        if (this.timeSpinner == null) {
            return;
        }
        JFormattedTextField tf = ((JSpinner.DefaultEditor)this.timeSpinner.getEditor()).getTextField();
        DefaultFormatterFactory factory = (DefaultFormatterFactory)tf.getFormatterFactory();
        DateFormatter formatter = (DateFormatter)factory.getDefaultFormatter();
        formatter.setFormat(this.timeFormat);
    }

    private void commitTime() {
        Date date = this.getDate();
        if (date != null) {
            Date time = (Date)this.timeSpinner.getValue();
            GregorianCalendar timeCalendar = new GregorianCalendar();
            timeCalendar.setTime(time);
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.set(11, timeCalendar.get(11));
            calendar.set(12, timeCalendar.get(12));
            calendar.set(13, 0);
            calendar.set(14, 0);
            Date newDate = calendar.getTime();
            this.setDate(newDate);
        }
    }

    private void setTimeSpinners() {
        Date date = this.getDate();
        if (date != null) {
            this.timeSpinner.setValue(date);
        }
    }

    public DateFormat getTimeFormat() {
        return this.timeFormat;
    }

    public void setTimeFormat(DateFormat timeFormat) {
        this.timeFormat = timeFormat;
        this.updateTextFieldFormat();
    }

    public Timestamp getTimestamp() {
        Date date = this.getDate();
        Date time = (Date)this.timeSpinner.getValue();
        GregorianCalendar timeCalendar = new GregorianCalendar();
        timeCalendar.setTime(time);
        GregorianCalendar calendar = new GregorianCalendar();
        if (date != null) {
            calendar.setTime(date);
        }
        calendar.set(11, timeCalendar.get(11));
        calendar.set(12, timeCalendar.get(12));
        calendar.set(13, 0);
        calendar.set(14, 0);
        Timestamp setDate = new Timestamp(calendar.getTimeInMillis());
        return setDate;
    }

    public void setTimestamp(Timestamp value) {
        if (value != null) {
            this.timeSpinner.setValue(value);
        }
        this.setDate(value);
    }
}

