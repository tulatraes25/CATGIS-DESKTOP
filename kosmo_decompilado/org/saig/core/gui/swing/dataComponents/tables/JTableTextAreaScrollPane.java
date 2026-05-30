/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.core.gui.swing.dataComponents.tables;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureUtil;
import java.awt.event.KeyListener;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.dataComponents.DataComponent;
import org.saig.core.model.data.Record;

public class JTableTextAreaScrollPane
extends JScrollPane
implements DataComponent<Object> {
    private static final long serialVersionUID = 1L;
    private String field;
    private Record record;
    private JTextArea textArea;

    public JTableTextAreaScrollPane(String field) {
        this(field, 3, 80);
    }

    public JTableTextAreaScrollPane(String field, int rows, int cols) {
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

    public void setRecord(Record record) {
        this.record = record;
        this.refresh();
    }

    @Override
    public void refresh() {
        if (this.record != null) {
            Object value = this.record.getAttribute(this.field);
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
        if (this.record == null) {
            return this.textArea.getText().trim();
        }
        AttributeType tipo = this.record.getSchema().getAttributeType(this.field);
        String value = this.textArea.getText().trim();
        if (StringUtils.isEmpty((String)value)) {
            return null;
        }
        return FeatureUtil.getGoodAttribute(tipo, value);
    }

    public Object getValue(Record record) {
        AttributeType tipo = record.getSchema().getAttributeType(this.field);
        String value = this.textArea.getText().trim();
        if (StringUtils.isEmpty((String)value)) {
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

    public void setBorderToTextArea(Border border) {
        this.textArea.setBorder(border);
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
}

