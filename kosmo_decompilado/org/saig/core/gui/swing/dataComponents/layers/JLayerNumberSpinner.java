/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.dataComponents.layers;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import org.saig.core.gui.swing.dataComponents.DataComponent;
import org.saig.core.model.feature.Attribute;
import org.saig.jump.widgets.util.NumberSpinner;

public class JLayerNumberSpinner
extends NumberSpinner
implements DataComponent<Object> {
    private static final long serialVersionUID = 1L;
    protected Attribute field;
    protected Feature feature;
    protected double defaultValue;
    protected boolean isIntegerSpinner;

    public JLayerNumberSpinner(Attribute field, int defaultValue, int minValue, int maxValue, int step) {
        super(defaultValue, minValue, maxValue, step);
        this.field = field;
        this.defaultValue = defaultValue;
        this.isIntegerSpinner = true;
        this.refresh();
    }

    public JLayerNumberSpinner(Attribute field, double defaultValue, double minValue, double maxValue, double step) {
        super(defaultValue, minValue, maxValue, step);
        this.field = field;
        this.defaultValue = defaultValue;
        this.isIntegerSpinner = false;
        this.refresh();
    }

    @Override
    public void clear() {
        if (this.isIntegerSpinner) {
            this.setValue((int)this.defaultValue);
        } else {
            this.setValue(this.defaultValue);
        }
    }

    @Override
    public void refresh() {
        if (this.feature != null) {
            Object value = this.feature.getAttribute(this.field.getName());
            if (value != null && value instanceof Number) {
                if (this.isIntegerSpinner) {
                    this.setValue(((Number)value).intValue());
                } else {
                    this.setValue(((Number)value).doubleValue());
                }
            } else {
                this.setValue(this.defaultValue);
            }
        } else if (this.isIntegerSpinner) {
            this.setValue((int)this.defaultValue);
        } else {
            this.setValue(this.defaultValue);
        }
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
        this.refresh();
    }

    public Object getComponentValue() {
        if (this.field == null) {
            return super.getValue();
        }
        AttributeType tipo = this.field.getType();
        Double value = this.getDoubleValue();
        if (value == null) {
            return null;
        }
        return FeatureUtil.getGoodAttribute(tipo, value);
    }

    @Override
    public int getIntValue() {
        return ((Number)super.getValue()).intValue();
    }

    @Override
    public double getDoubleValue() {
        return ((Number)super.getValue()).doubleValue();
    }
}

