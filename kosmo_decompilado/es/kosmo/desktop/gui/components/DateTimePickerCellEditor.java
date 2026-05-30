/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode
 */
package es.kosmo.desktop.gui.components;

import es.kosmo.desktop.gui.components.DateTimePicker;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

public class DateTimePickerCellEditor
extends AbstractCellEditor
implements TableCellEditor,
TreeCellEditor {
    protected DateTimePicker datePicker;
    protected DateFormat dateFormat;
    protected int clickCountToStart = 2;
    private ActionListener pickerActionListener;
    protected boolean ignoreAction;
    private static Logger logger = Logger.getLogger(DateTimePickerCellEditor.class.getName());
    private static final long serialVersionUID = -1L;

    public DateTimePickerCellEditor() {
        this(null);
    }

    public DateTimePickerCellEditor(DateFormat dateFormat) {
        this.dateFormat = dateFormat != null ? dateFormat : DateFormat.getDateInstance();
        this.datePicker = new DateTimePicker();
        this.datePicker.getEditor().setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
        this.datePicker.setFont(UIManager.getDefaults().getFont("TextField.font"));
        if (dateFormat != null) {
            this.datePicker.setFormats(new DateFormat[]{dateFormat});
        }
        this.datePicker.addActionListener(this.getPickerActionListener());
    }

    @Override
    public Timestamp getCellEditorValue() {
        return this.datePicker.getTimestamp();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent)anEvent).getClickCount() >= this.getClickCountToStart();
        }
        return super.isCellEditable(anEvent);
    }

    @Override
    public boolean stopCellEditing() {
        this.ignoreAction = true;
        boolean canCommit = this.commitChange();
        this.ignoreAction = false;
        if (canCommit) {
            return super.stopCellEditing();
        }
        return false;
    }

    public void setClickCountToStart(int count) {
        this.clickCountToStart = count;
    }

    public int getClickCountToStart() {
        return this.clickCountToStart;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.ignoreAction = true;
        this.datePicker.setTimestamp(this.getValueAsTimestamp(value));
        this.ignoreAction = false;
        return this.datePicker;
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        this.ignoreAction = true;
        this.datePicker.setTimestamp(this.getValueAsTimestamp(value));
        this.ignoreAction = false;
        return this.datePicker;
    }

    protected Timestamp getValueAsTimestamp(Object value) {
        if (this.isEmpty(value)) {
            return null;
        }
        if (value instanceof Timestamp) {
            return (Timestamp)value;
        }
        if (value instanceof Date) {
            return new Timestamp(((Date)value).getTime());
        }
        if (value instanceof Long) {
            return new Timestamp((Long)value);
        }
        if (value instanceof String) {
            try {
                return new Timestamp(this.dateFormat.parse((String)value).getTime());
            }
            catch (ParseException e) {
                this.handleParseException(e);
            }
        }
        if (value instanceof DefaultMutableTreeNode) {
            return this.getValueAsTimestamp(((DefaultMutableTreeNode)value).getUserObject());
        }
        if (value instanceof AbstractMutableTreeTableNode) {
            return this.getValueAsTimestamp(((AbstractMutableTreeTableNode)value).getUserObject());
        }
        return null;
    }

    protected void handleParseException(ParseException e) {
        logger.log(Level.SEVERE, e.getMessage(), e.getMessage());
    }

    protected boolean isEmpty(Object value) {
        return value == null || value instanceof String && ((String)value).length() == 0;
    }

    protected boolean commitChange() {
        try {
            this.datePicker.commitEdit();
            return true;
        }
        catch (ParseException parseException) {
            return false;
        }
    }

    public DateFormat[] getFormats() {
        return this.datePicker.getFormats();
    }

    public void setFormats(DateFormat ... formats) {
        this.datePicker.setFormats(formats);
    }

    protected ActionListener getPickerActionListener() {
        if (this.pickerActionListener == null) {
            this.pickerActionListener = this.createPickerActionListener();
        }
        return this.pickerActionListener;
    }

    protected ActionListener createPickerActionListener() {
        ActionListener l = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (DateTimePickerCellEditor.this.ignoreAction) {
                    return;
                }
                this.terminateEdit(e);
            }

            private void terminateEdit(ActionEvent e) {
                if (e != null && "datePickerCommit".equals(e.getActionCommand())) {
                    DateTimePickerCellEditor.this.stopCellEditing();
                } else {
                    DateTimePickerCellEditor.this.cancelCellEditing();
                }
            }
        };
        return l;
    }
}

