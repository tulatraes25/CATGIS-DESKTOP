/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.toedter.calendar.JDateChooserCellEditor
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets.tables.management;

import com.pcauto.gui.table.AbstractEntityTableColumnModel;
import com.pcauto.gui.table.EntityJTable;
import com.pcauto.gui.table.EntityList;
import com.pcauto.gui.table.EntityListException;
import com.pcauto.gui.table.EntityTable;
import com.pcauto.gui.table.EntityTableColumn;
import com.pcauto.gui.table.EntityTableColumnModel;
import com.pcauto.gui.table.EntityTableFocusType;
import com.pcauto.gui.table.ProxyEntityList;
import com.toedter.calendar.JDateChooserCellEditor;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import org.apache.log4j.Logger;
import org.saig.core.context.GenericContext;
import org.saig.core.gui.swing.dataComponents.layers.JLayerComboBox;
import org.saig.core.gui.swing.dataComponents.tables.JTableWithDataSourceComboBox;
import org.saig.core.model.data.widgets.tables.management.combo.ExtendedDefaultCellEditor;
import org.saig.core.model.data.widgets.tables.management.combo.ExtendedDefaultCellRenderer;
import org.saig.core.model.data.widgets.tables.management.combo.RelationData;
import org.saig.core.model.data.widgets.tables.management.control.ControlPanel;
import org.saig.core.model.data.widgets.tables.management.definition.Column;
import org.saig.core.model.data.widgets.tables.management.definition.TableDef;
import org.saig.core.model.data.widgets.tables.management.operations.OperationsManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DateCellRenderer;

public class TableManagementPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    protected static final Logger LOGGER = Logger.getLogger(TableManagementPanel.class);
    protected EntityTableColumnModel columnModel = null;
    protected EntityTable mainTable;
    protected ControlPanel controlPanel;
    protected OperationsManager manager;
    protected FeatureSchema fs;
    protected boolean pkEditable = true;
    protected TableDef tableDef;
    private final boolean useCache;

    public TableManagementPanel(boolean useCache) {
        this.useCache = useCache;
    }

    public TableManagementPanel(String tableName, ControlPanel control, TableDef tableDef) throws Exception {
        this(tableName, control, tableDef, false);
    }

    public TableManagementPanel(String tableName, ControlPanel control, TableDef tableDef, boolean useCache) throws Exception {
        this.useCache = useCache;
        this.tableDef = tableDef;
        if (tableDef != null) {
            this.pkEditable = tableDef.isPkEditable();
        }
        this.manager = new OperationsManager(tableName, tableDef);
        this.controlPanel = control;
        this.controlPanel.setManager(this.manager);
        this.mainTable = new EntityTable();
        this.fs = this.manager.getSchema();
        this.defineColumnModel();
        this.loadData();
        this.controlPanel.setTablePanel(this);
        this.initComponents();
        EntityJTable table = (EntityJTable)this.mainTable.getScrollPane().getViewport().getComponent(0);
        table.setDefaultRenderer(Class.forName("java.lang.Object"), new NoEditableRenderer());
        table.setDefaultRenderer(Number.class, new NoEditableRenderer());
        table.setDefaultRenderer(Date.class, new NoEditableDateRenderer());
        this.resizeColumns();
    }

    protected void resizeColumns() {
        this.mainTable.setWidthAutoCalculated(false);
        JTable jTable = (JTable)this.mainTable.getScrollPane().getViewport().getComponent(0);
        jTable.setMinimumSize(new Dimension(635, 150));
    }

    protected void defineColumnModel() {
        this.columnModel = new AbstractEntityTableColumnModel(){
            private static final long serialVersionUID = 1L;

            @Override
            public Object getCellValue(int col, Object entity) {
                return TableManagementPanel.this.manager.getValue(col, entity);
            }

            @Override
            public void setCellValue(int col, Object entity, Object newValue) {
                TableManagementPanel.this.manager.setValue(col, entity, newValue);
                TableManagementPanel.this.controlPanel.evaluateButtons();
            }
        };
        Map relationMap = this.generateRelationMap();
        Map<Integer, String> indexMap = this.fs.getIndexAttributes();
        Set<Integer> keySet = indexMap.keySet();
        ArrayList<Integer> keyList = new ArrayList<Integer>(keySet);
        Collections.sort(keyList);
        Iterator it = keyList.iterator();
        while (it.hasNext()) {
            int pos = (Integer)it.next();
            if (!this.fs.getVisibility(pos).booleanValue() || this.fs.isAttributeCalculate(pos)) continue;
            RelationData relData = (RelationData)relationMap.get(this.fs.getAttributeName(pos));
            EntityTableColumn column = this.createColumn(relData, pos);
            this.columnModel.addColumn(column);
            if ((this.fs.getPrimaryKeyIndex() != pos || this.pkEditable) && (!this.fs.isVersionable() || !this.manager.getVersionableFieldsPositions().contains(pos))) continue;
            this.columnModel.setEditable(pos, false);
        }
        this.mainTable.setColumnModel(this.columnModel);
    }

    private EntityTableColumn createColumn(RelationData relData, int pos) {
        Column columnDef;
        EntityTableColumn column = null;
        int size = 0;
        if (this.tableDef != null && (columnDef = this.tableDef.getColumn(this.fs.getAttributeName(pos))) != null) {
            size = columnDef.getSize();
        }
        if (relData != null) {
            column = this.createComboColumn(relData, pos, size);
        } else if (this.isDateTime(pos)) {
            if (size == 0) {
                size = 30;
            }
            column = new EntityTableColumn(this.fs.getPublicName(pos), this.fs.getAttributeType(pos).toJavaClass(), size, (TableCellEditor)new JDateChooserCellEditor(), new NoEditableDateRenderer());
        } else {
            column = size == 0 ? new EntityTableColumn(this.fs.getPublicName(pos), this.fs.getAttributeType(pos).toJavaClass()) : new EntityTableColumn(this.fs.getPublicName(pos), this.fs.getAttributeType(pos).toJavaClass(), size);
        }
        return column;
    }

    protected EntityTableColumn createComboColumn(RelationData relData, int pos, int size) {
        EntityTableColumn comboColumn = null;
        if (size == 0) {
            size = 100;
        }
        if (relData.getType().equals("table")) {
            JTableWithDataSourceComboBox combo = new JTableWithDataSourceComboBox(GenericContext.getGenericContext().getTableDataSource(relData.getTableName()), relData.getCode(), relData.getValue());
            comboColumn = new EntityTableColumn(this.fs.getPublicName(pos), this.fs.getAttributeType(pos).toJavaClass(), size, (TableCellEditor)new ExtendedDefaultCellEditor(combo), new ExtendedDefaultCellRenderer(combo, relData.getValue(), this.useCache));
        } else {
            JLayerComboBox combo = new JLayerComboBox(JUMPWorkbench.getLayer(relData.getTableName()), relData.getCode(), relData.getValue());
            comboColumn = new EntityTableColumn(this.fs.getPublicName(pos), this.fs.getAttributeType(pos).toJavaClass(), size, (TableCellEditor)new ExtendedDefaultCellEditor(combo), new ExtendedDefaultCellRenderer(combo, relData.getValue(), this.useCache));
        }
        return comboColumn;
    }

    private boolean isDateTime(int pos) {
        return this.fs.getAttributeType(pos).toJavaClass().equals(Date.class) || this.fs.getAttributeType(pos).toJavaClass().equals(Timestamp.class);
    }

    protected Map generateRelationMap() {
        HashMap<String, RelationData> map = new HashMap<String, RelationData>();
        if (this.tableDef != null) {
            Map<String, Column> relations = this.tableDef.getColumns();
            Iterator<String> it = relations.keySet().iterator();
            while (it.hasNext()) {
                Column column = relations.get(it.next());
                if (column.getRelationTable() == null) continue;
                String name = column.getName();
                String comboCode = column.getRelationField();
                String comboValue = column.getRelationFieldToShow();
                String comboTableName = column.getRelationTable();
                map.put(name, new RelationData(comboCode, comboValue, comboTableName));
            }
        }
        return map;
    }

    public void loadData() {
        ProxyEntityList testTableEntityList = new ProxyEntityList();
        Object defaultEntity = this.getDefaultEntity();
        testTableEntityList.setDefaultEntity(defaultEntity);
        List list = this.getDataList();
        Iterator it = list.iterator();
        try {
            while (it.hasNext()) {
                Object record = it.next();
                testTableEntityList.addEntity(record);
            }
        }
        catch (EntityListException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        this.mainTable.setEntityList(testTableEntityList);
        if (this.mainTable.getColumnModel().getColumnCount() > 0) {
            Vector<Integer> v = new Vector<Integer>();
            v.add(new Integer(0));
            this.mainTable.sortByColumns(v);
        }
    }

    protected void initComponents() {
        this.setLayout(new BorderLayout());
        this.mainTable.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.core.model.data.widgets.tables.management.TableManagementPanel.data-table")));
        this.mainTable.setCurrentFocusMode(EntityTableFocusType.ROW_FOCUS);
        this.mainTable.setVirtualRowEnabled(false);
        this.mainTable.setVirtualColumnEnabled(true);
        this.mainTable.setSelectionMode(0);
        this.mainTable.setMinimumSize(new Dimension(700, 200));
        this.mainTable.setPreferredSize(new Dimension(700, 200));
        this.mainTable.setMaximumSize(new Dimension(700, 200));
        this.add((Component)this.mainTable, "Center");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add((Component)((Object)this.controlPanel));
        this.add((Component)buttonPanel, "South");
    }

    public List<Object> getEntityListData() {
        EntityList el = this.mainTable.getEntityList();
        ArrayList<Object> data = new ArrayList<Object>();
        int i = 0;
        while (i < el.getCount()) {
            data.add(el.getEntity(i));
            ++i;
        }
        return data;
    }

    protected Object getDefaultEntity() {
        return this.manager.getNewEntity();
    }

    public FeatureSchema getSchema() {
        return this.fs;
    }

    private List getDataList() {
        return this.manager.getDataList();
    }

    public EntityTable getMainTable() {
        return this.mainTable;
    }

    public boolean hasOperations() {
        return this.manager.hasOperations();
    }

    protected class NoEditableDateRenderer
    extends DateCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = null;
            try {
                label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!TableManagementPanel.this.columnModel.isEditable(column)) {
                    label = new JLabel(label.getText());
                    label.setBackground(Color.LIGHT_GRAY);
                    label.setFont(label.getFont().deriveFont(2));
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)e);
            }
            return label;
        }
    }

    protected class NoEditableRenderer
    extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = null;
            try {
                label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!TableManagementPanel.this.columnModel.isEditable(column)) {
                    label = new JLabel(label.getText());
                    label.setBackground(Color.LIGHT_GRAY);
                    label.setFont(label.getFont().deriveFont(2));
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)e);
            }
            return label;
        }
    }
}

