/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.plaf.UIResource;
import org.saig.core.gui.swing.sldeditor.property.ScaleEditor;

public class DefaultScaleEditor
extends ScaleEditor {
    private static final long serialVersionUID = 1L;
    private static double[] defaultScales = new double[]{1000.0, 5000.0, 10000.0, 20000.0, 30000.0, 40000.0, 50000.0, 100000.0, 200000.0, 300000.0, 400000.0, 500000.0, 1000000.0, 2000000.0, 3000000.0, 4000000.0, 5000000.0, 1.0E7};
    private static NumberFormat numberFormat = NumberFormat.getNumberInstance();
    private JComboBox cmbScale = new JComboBox();

    public DefaultScaleEditor() {
        this(defaultScales);
    }

    public DefaultScaleEditor(double[] scalesList) {
        this.cmbScale.setEditor(new DoubleComboBoxEditor());
        this.cmbScale.setRenderer(new NumberListCellRenderer());
        this.cmbScale.setEditable(true);
        Double[] scales = new Double[defaultScales.length];
        int i = 0;
        while (i < scales.length) {
            scales[i] = new Double(defaultScales[i]);
            ++i;
        }
        this.cmbScale.setModel(new DefaultComboBoxModel<Double>(scales));
        this.setLayout(new BorderLayout());
        this.add(this.cmbScale);
    }

    @Override
    public void setScaleDenominator(double scale) {
        this.cmbScale.setSelectedItem(new Double(scale));
    }

    @Override
    public double getScaleDenominator() {
        return ((Number)this.cmbScale.getSelectedItem()).doubleValue();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.cmbScale.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return this.cmbScale.isEnabled();
    }

    public static class DoubleComboBoxEditor
    implements ComboBoxEditor,
    FocusListener {
        protected JFormattedTextField editor = new JFormattedTextField(DefaultScaleEditor.access$0());
        private Object oldValue;

        public DoubleComboBoxEditor() {
            this.editor.setValue(new Double(1000.0));
            this.editor.setInputVerifier(new ScaleVerifier());
        }

        @Override
        public Component getEditorComponent() {
            return this.editor;
        }

        @Override
        public void setItem(Object anObject) {
            if (anObject instanceof Number) {
                this.editor.setValue(new Double(((Number)anObject).doubleValue()));
                this.oldValue = anObject;
            }
        }

        @Override
        public Object getItem() {
            Object newValue = this.editor.getValue();
            if (this.oldValue != null && this.oldValue.equals(newValue)) {
                return this.oldValue;
            }
            return newValue;
        }

        @Override
        public void selectAll() {
            this.editor.selectAll();
            this.editor.requestFocus();
        }

        @Override
        public void focusGained(FocusEvent e) {
        }

        @Override
        public void focusLost(FocusEvent e) {
        }

        @Override
        public void addActionListener(ActionListener l) {
            this.editor.addActionListener(l);
        }

        @Override
        public void removeActionListener(ActionListener l) {
            this.editor.removeActionListener(l);
        }

        public class ScaleVerifier
        extends InputVerifier {
            @Override
            public boolean verify(JComponent input) {
                JFormattedTextField ftf;
                JFormattedTextField.AbstractFormatter formatter;
                if (input instanceof JFormattedTextField && (formatter = (ftf = (JFormattedTextField)input).getFormatter()) != null) {
                    String text = ftf.getText();
                    try {
                        Number value = (Number)formatter.stringToValue(text);
                        return value.doubleValue() > 0.0;
                    }
                    catch (ParseException pe) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public boolean shouldYieldFocus(JComponent input) {
                return this.verify(input);
            }
        }

        public static class UIResource
        extends DoubleComboBoxEditor
        implements javax.swing.plaf.UIResource {
        }
    }

    public static class NumberListCellRenderer
    extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
            if (value instanceof Number) {
                this.setText(numberFormat.format(((Number)value).doubleValue()));
            }
            return this;
        }
    }
}

