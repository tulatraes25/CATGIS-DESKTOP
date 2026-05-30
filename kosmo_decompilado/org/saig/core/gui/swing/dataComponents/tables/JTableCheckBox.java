/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.dataComponents.tables;

import java.awt.Color;
import javax.swing.JCheckBox;
import org.saig.core.gui.swing.dataComponents.DataComponent;
import org.saig.core.model.data.Record;

public class JTableCheckBox
extends JCheckBox
implements DataComponent<Boolean> {
    private static final long serialVersionUID = 1L;
    private String field;
    private Record record;

    public JTableCheckBox(String field, String textName) {
        this.field = field;
        this.setText(textName);
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

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            this.setForeground(Color.BLACK);
        } else {
            this.setForeground(Color.BLACK);
        }
    }
}

