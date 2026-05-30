/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.core.gui.swing.dataComponents.layers;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.text.Format;
import java.text.ParseException;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.dataComponents.DataComponent;
import org.saig.core.model.feature.Attribute;

public class JLayerTextField
extends JTextField
implements DataComponent<Object> {
    private static final long serialVersionUID = 1L;
    private Attribute field;
    private Feature feature;
    private Format format;
    private String defaultValue = "";

    public JLayerTextField(Attribute field) {
        this.field = field;
        this.refresh();
    }

    public JLayerTextField(Attribute field, int w, int h) {
        this.setMinimumSize(new Dimension(w, h));
        this.setPreferredSize(new Dimension(w, h));
        this.field = field;
        this.refresh();
    }

    public JLayerTextField(Attribute field, Format format) {
        this.field = field;
        this.format = format;
        this.refresh();
    }

    public JLayerTextField(Attribute field, Format format, int w, int h) {
        this.field = field;
        this.format = format;
        this.setMinimumSize(new Dimension(w, h));
        this.setPreferredSize(new Dimension(w, h));
        this.refresh();
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
        this.refresh();
    }

    @Override
    public void refresh() {
        if (this.feature != null) {
            Object value = this.feature.getAttribute(this.field.getName());
            if (value != null) {
                if (this.format != null) {
                    this.setText(this.format.format(value));
                } else {
                    this.setText(value.toString());
                }
            } else {
                this.setText(this.defaultValue);
            }
        } else {
            this.setText(this.defaultValue);
        }
    }

    @Override
    public Object getValue() {
        AttributeType tipo = this.field.getType();
        String value = this.getText().trim();
        if (StringUtils.isEmpty((String)value)) {
            return null;
        }
        if (this.format == null) {
            return FeatureUtil.getGoodAttribute(tipo, value);
        }
        try {
            return this.format.parseObject(value);
        }
        catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object getValue(Feature feature) {
        AttributeType tipo = this.field.getType();
        String value = this.getText().trim();
        if (StringUtils.isEmpty((String)value)) {
            return null;
        }
        if (this.format == null) {
            return FeatureUtil.getGoodAttribute(tipo, value);
        }
        try {
            return this.format.parseObject(value);
        }
        catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public void clear() {
        this.setText(this.defaultValue);
    }

    @Override
    public void setEditable(boolean editable) {
        super.setEditable(editable);
        if (editable) {
            this.setBackground(Color.WHITE);
        } else {
            this.setBackground(new Color(248, 248, 255));
        }
    }
}

