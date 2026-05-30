/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.renderer.decorators.config;

import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.AttributeRow;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.saig.jump.lang.I18N;

public class ChartAttributeSelectionTableModel
extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private List<AttributeRow> attrs = new ArrayList<AttributeRow>();

    @Override
    public int getRowCount() {
        return this.attrs.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        AttributeRow attr = this.attrs.get(rowIndex);
        Object value = null;
        switch (columnIndex) {
            case 0: {
                value = attr.getFieldName();
                break;
            }
            case 1: {
                value = attr.getLabel();
                break;
            }
            case 2: {
                value = attr.getColor();
            }
        }
        return value;
    }

    @Override
    public String getColumnName(int col) {
        String value = null;
        switch (col) {
            case 0: {
                value = I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ChartAttributeSelectionTableModel.Field");
                break;
            }
            case 1: {
                value = I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ChartAttributeSelectionTableModel.Label");
                break;
            }
            case 2: {
                value = I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ChartAttributeSelectionTableModel.Color");
            }
        }
        return value;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        Class value = null;
        switch (col) {
            case 0: 
            case 1: {
                value = String.class;
                break;
            }
            case 2: {
                value = Color.class;
            }
        }
        return value;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col > 0;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        AttributeRow attrRow = this.attrs.get(row);
        switch (col) {
            case 0: {
                break;
            }
            case 1: {
                attrRow.setLabel((String)value);
                break;
            }
            case 2: {
                attrRow.setColor((Color)value);
            }
        }
        this.fireTableCellUpdated(row, col);
    }

    public void addAttributeRow(AttributeRow row) {
        this.attrs.add(row);
        this.fireTableDataChanged();
    }

    public AttributeRow removeAttributeRow(int row) {
        AttributeRow rowAttr = this.attrs.remove(row);
        this.fireTableDataChanged();
        return rowAttr;
    }

    public AttributeRow getAttributeRow(int i) {
        return this.attrs.get(i);
    }
}

