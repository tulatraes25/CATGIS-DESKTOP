/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.core.gui.swing.dataComponents.tables;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureUtil;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.dataComponents.DataComponent;
import org.saig.core.model.data.Record;

public class JTableTextArea
extends JTextArea
implements DataComponent<Object> {
    private static final long serialVersionUID = 1L;
    private String field;
    private Record record;

    public JTableTextArea(String field) {
        this(field, 3, 80);
    }

    public JTableTextArea(String field, int rows, int cols) {
        this.setLineWrap(true);
        this.setWrapStyleWord(true);
        this.setColumns(rows);
        this.setRows(cols);
        this.field = field;
        JLabel label = new JLabel();
        this.setFont(label.getFont());
        this.revalidate();
        this.refresh();
    }

    public void setRecord(Record record) {
        this.record = record;
        this.refresh();
    }

    @Override
    public void refresh() {
        if (this.record != null) {
            Object value = this.record.getAttribute(this.field);
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
        if (this.record == null) {
            return this.getText().trim();
        }
        AttributeType tipo = this.record.getSchema().getAttributeType(this.field);
        String value = this.getText().trim();
        if (StringUtils.isEmpty((String)value)) {
            return null;
        }
        return FeatureUtil.getGoodAttribute(tipo, value);
    }

    @Override
    public void clear() {
        this.setText("");
    }
}

