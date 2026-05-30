/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.toedter.calendar.JDateChooser
 */
package org.saig.core.gui.swing.dataComponents.layers;

import com.toedter.calendar.JDateChooser;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import java.awt.Color;
import java.util.Date;
import javax.swing.JTextField;
import org.saig.core.gui.swing.dataComponents.DataComponent;
import org.saig.core.model.feature.Attribute;

public class JLayerDateChooser
extends JDateChooser
implements DataComponent<Object> {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DEFAULT_DATE_MASK = "##/##/####";
    public static final char DEFAULT_CHAR_MASK = '_';
    private String field;
    private Attribute fieldAttr;
    private Feature feature;

    public JLayerDateChooser(String field) {
        this(field, DEFAULT_DATE_FORMAT, DEFAULT_DATE_MASK, '_');
    }

    public JLayerDateChooser(String field, String dateFormat, String dateMask, char charMask) {
        super(dateFormat, dateMask, charMask);
        this.field = field;
        this.setFont(new JTextField().getFont());
    }

    public JLayerDateChooser(Attribute attr) {
        this(attr, DEFAULT_DATE_FORMAT, DEFAULT_DATE_MASK, '_');
    }

    public JLayerDateChooser(Attribute attr, String dateFormat, String dateMask, char charMask) {
        super(dateFormat, dateMask, charMask);
        this.field = attr.getName();
        this.fieldAttr = attr;
        this.setFont(new JTextField().getFont());
    }

    @Override
    public void refresh() {
        if (this.feature != null) {
            Object value = this.feature.getAttribute(this.field);
            if (value != null) {
                this.setDate((Date)value);
            } else {
                this.setDate(null);
            }
        } else {
            this.setDate(null);
        }
    }

    @Override
    public Object getValue() {
        Date date = this.getDate();
        if (this.fieldAttr != null) {
            return FeatureUtil.getGoodAttribute(this.fieldAttr.getType(), date);
        }
        return date;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
        this.refresh();
    }

    @Override
    public void clear() {
        this.setDate(null);
    }

    public void setEnabled(boolean editable) {
        super.setEnabled(editable);
        if (editable) {
            this.setBackground(Color.WHITE);
        } else {
            this.setBackground(new Color(248, 248, 255));
        }
    }
}

