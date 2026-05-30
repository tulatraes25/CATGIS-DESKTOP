/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.sdi.wfs;

import es.kosmo.desktop.widgets.sdi.wfs.WFSFeatureTypeTableModel;
import es.kosmo.desktop.widgets.sdi.wfs.WFSFilterTableCellRenderer;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import org.saig.core.filter.Filter;
import org.saig.core.model.sdi.wfs.WFSFeatureTypeInfo;

public class WFSFilterTableCellEditor
extends AbstractCellEditor
implements TableCellEditor {
    private static final long serialVersionUID = 1L;
    private WFSFilterTableCellRenderer cellRenderer = new WFSFilterTableCellRenderer(this);
    private Filter currentFilter;
    private JTable table;

    public WFSFilterTableCellEditor(JTable table) {
        this.table = table;
    }

    @Override
    public Object getCellEditorValue() {
        return this.cellRenderer.getFilter();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.currentFilter = (Filter)value;
        this.cellRenderer.setFilter(this.currentFilter);
        return this.cellRenderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
    }

    @Override
    public boolean isCellEditable(EventObject event) {
        boolean returnValue = false;
        if (event instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent)event;
            int row = this.table.rowAtPoint(mouseEvent.getPoint());
            int realRowIndex = this.table.convertRowIndexToModel(row);
            List infos = (List)((WFSFeatureTypeTableModel)this.table.getModel()).getFeatureTypeInfosAt(new int[]{realRowIndex});
            WFSFeatureTypeInfo associatedFeatureTypeInfo = (WFSFeatureTypeInfo)infos.get(0);
            returnValue = associatedFeatureTypeInfo.getGeomAttrName() != null && associatedFeatureTypeInfo.getPkName() != null;
        }
        return returnValue;
    }
}

