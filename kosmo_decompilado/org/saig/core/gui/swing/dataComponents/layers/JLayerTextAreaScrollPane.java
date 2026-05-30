/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.dataComponents.layers;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import java.awt.event.KeyListener;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.saig.core.gui.swing.dataComponents.DataComponent;

public class JLayerTextAreaScrollPane
extends JScrollPane
implements DataComponent<Object> {
    private static final long serialVersionUID = 1L;
    private String field;
    private Feature feature;
    private JTextArea textArea;

    public JLayerTextAreaScrollPane(String field) {
        this(field, 3, 80);
    }

    public JLayerTextAreaScrollPane(String field, int rows, int cols) {
        super(22, 31);
        this.field = field;
        this.textArea = new JTextArea();
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        this.textArea.setColumns(cols);
        this.textArea.setRows(rows);
        JLabel label = new JLabel();
        this.textArea.setFont(label.getFont());
        this.textArea.revalidate();
        this.setViewportView(this.textArea);
        this.refresh();
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
        this.refresh();
    }

    @Override
    public void refresh() {
        if (this.feature != null) {
            Object value = this.feature.getAttribute(this.field);
            if (value != null) {
                this.textArea.setText(value.toString());
                this.textArea.setCaretPosition(0);
            } else {
                this.textArea.setText("");
            }
        } else {
            this.textArea.setText("");
        }
    }

    @Override
    public Object getValue() {
        if (this.feature == null) {
            return this.textArea.getText().trim();
        }
        AttributeType tipo = this.feature.getSchema().getAttributeType(this.field);
        String value = this.textArea.getText().trim();
        if (value.equals("")) {
            return null;
        }
        return FeatureUtil.getGoodAttribute(tipo, value);
    }

    public Object getValue(Feature feature) {
        AttributeType tipo = feature.getSchema().getAttributeType(this.field);
        String value = this.textArea.getText().trim();
        if (value.equals("")) {
            return null;
        }
        return FeatureUtil.getGoodAttribute(tipo, value);
    }

    public void setEditable(boolean editable) {
        this.textArea.setEditable(editable);
    }

    public void setText(String text) {
        this.textArea.setText(text);
    }

    @Override
    public void clear() {
        this.textArea.setText("");
    }

    public JTextArea getTextArea() {
        return this.textArea;
    }

    @Override
    public void addKeyListener(KeyListener keyListener) {
        this.textArea.addKeyListener(keyListener);
    }

    @Override
    public void setToolTipText(String text) {
        super.setToolTipText(text);
        this.textArea.setToolTipText(text);
    }
}

