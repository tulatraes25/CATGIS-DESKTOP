/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.dataComponents.layers;

import com.vividsolutions.jump.feature.Feature;
import javax.swing.JCheckBox;
import org.saig.core.gui.swing.dataComponents.DataComponent;

public class JLayerCheckBox
extends JCheckBox
implements DataComponent<Boolean> {
    private static final long serialVersionUID = 1L;
    private String field;
    private Feature feature;

    public JLayerCheckBox(String field, String textName) {
        this.field = field;
        this.setText(textName);
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
                if (value instanceof Boolean) {
                    this.setSelected((Boolean)value);
                } else if (value instanceof Number) {
                    this.setSelected(((Number)value).intValue() == 1);
                } else if (value instanceof String) {
                    this.setSelected(((String)value).toUpperCase().equals("1"));
                }
            } else {
                this.setSelected(false);
            }
        } else {
            this.setSelected(false);
        }
    }

    @Override
    public Boolean getValue() {
        return new Boolean(this.isSelected());
    }

    @Override
    public void clear() {
        this.setSelected(false);
    }
}

