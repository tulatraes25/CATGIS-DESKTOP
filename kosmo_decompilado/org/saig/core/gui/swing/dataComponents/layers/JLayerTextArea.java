/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.dataComponents.layers;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import org.saig.core.gui.swing.dataComponents.DataComponent;

public class JLayerTextArea
extends JTextArea
implements DataComponent<Object> {
    private static final long serialVersionUID = 1L;
    private String field;
    private Feature feature;

    public JLayerTextArea(String field) {
        this(field, 3, 80);
    }

    public JLayerTextArea(String field, int rows, int cols) {
        this.setLineWrap(true);
        this.setWrapStyleWord(true);
        this.setColumns(cols);
        this.setRows(rows);
        this.field = field;
        JLabel label = new JLabel();
        this.setFont(label.getFont());
        this.revalidate();
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
                this.setText(value.toString());
                this.setCaretPosition(0);
            } else {
                this.setText("");
            }
        } else {
            this.setText("");
        }
    }

    @Override
    public Object getValue() {
        if (this.feature == null) {
            return this.getText().trim();
        }
        AttributeType tipo = this.feature.getSchema().getAttributeType(this.field);
        String value = this.getText().trim();
        if (value.equals("")) {
            return null;
        }
        return FeatureUtil.getGoodAttribute(tipo, value);
    }

    @Override
    public void clear() {
        this.setText("");
    }
}

