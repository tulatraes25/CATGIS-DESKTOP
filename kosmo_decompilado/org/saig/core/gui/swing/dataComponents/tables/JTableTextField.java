/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.gui.swing.dataComponents.tables;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.text.Format;
import java.text.ParseException;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.dataComponents.DataComponent;
import org.saig.core.model.data.Record;
import org.saig.core.model.feature.Attribute;

public class JTableTextField
extends JTextField
implements DataComponent<Object> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(JTableTextField.class);
    private Attribute field;
    private Record record;
    private Format format;
    private String defaultValue = "";

    public JTableTextField(Attribute field, int w, int h) {
        this.field = field;
        this.setMinimumSize(new Dimension(w, h));
        this.setPreferredSize(new Dimension(w, h));
        this.refresh();
    }

    public JTableTextField(Attribute field) {
        this.field = field;
        this.refresh();
    }

    public JTableTextField(Attribute field, Format format) {
        this.field = field;
        this.format = format;
        this.refresh();
    }

    public JTableTextField(Attribute field, Format format, int w, int h) {
        this.setMinimumSize(new Dimension(w, h));
        this.setPreferredSize(new Dimension(w, h));
        this.field = field;
        this.format = format;
        this.refresh();
    }

    public void setRecord(Record record) {
        this.record = record;
        this.refresh();
    }

    @Override
    public void refresh() {
        if (this.record != null) {
            Object value = this.record.getAttribute(this.field.getName());
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
            LOGGER.error((Object)"", (Throwable)e);
            return null;
        }
    }

    public Object getValue(Record record) {
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
            LOGGER.error((Object)"", (Throwable)e);
            return null;
        }
    }

    @Override
    public void clear() {
        this.setText(this.defaultValue);
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
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

