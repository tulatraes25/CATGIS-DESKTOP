/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.widgets.TableAttributeTab;
import org.saig.core.model.data.widgets.TableAttributeTablePanel;
import org.saig.core.model.data.widgets.TableAttributeTablePanelListener;
import org.saig.core.model.data.widgets.TableInfoModel;
import org.saig.core.model.data.widgets.TableInfoModelListener;
import org.saig.core.model.data.widgets.TableTableModel;
import org.saig.core.model.relations.LayerRelation;
import org.saig.core.model.relations.Relation;
import org.saig.core.model.relations.RelationType;
import org.saig.core.model.relations.TableRelation;
import org.saig.core.util.I18NUnsupportedOperationException;

public class TableAttributePanel
extends JPanel
implements TableInfoModelListener,
TableAttributeTablePanelListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TableAttributePanel.class);
    private Map<Table, TableAttributeTablePanel> tableToTablePanelMap = new HashMap<Table, TableAttributeTablePanel>();
    private TableInfoModel model;
    private boolean addScrollPanesToChildren;
    private WorkbenchContext workbenchContext;
    private Row nullRow = new Row(){

        @Override
        public boolean isFirstRow() {
            return TableAttributePanel.this.rowCount() == 0;
        }

        @Override
        public boolean isLastRow() {
            return TableAttributePanel.this.rowCount() == 0;
        }

        @Override
        public TableAttributeTablePanel getPanel() {
            throw new I18NUnsupportedOperationException();
        }

        @Override
        public int getIndex() {
            throw new I18NUnsupportedOperationException();
        }

        @Override
        public Row nextRow() {
            return this.firstRow();
        }

        @Override
        public Row previousRow() {
            return this.firstRow();
        }

        private Row firstRow() {
            return new BasicRow(TableAttributePanel.this.getTablePanel(TableAttributePanel.this.getModel().getTable()), 0);
        }

        @Override
        public Record getRecord() {
            throw new I18NUnsupportedOperationException();
        }
    };

    protected TableAttributePanel(TableInfoModel model, WorkbenchContext workbenchContext, boolean addScrollPanesToChildren) {
        this.addScrollPanesToChildren = addScrollPanesToChildren;
        this.workbenchContext = workbenchContext;
        this.setModel(model);
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            LOGGER.error((Object)"", (Throwable)ex);
        }
    }

    public TableAttributeTablePanel getTablePanel(Table table) {
        return this.tableToTablePanelMap.get(table);
    }

    public TableInfoModel getModel() {
        return this.model;
    }

    public void setModel(TableInfoModel model) {
        this.model = model;
        model.addListener(this);
    }

    @Override
    public void tableAdded(TableTableModel tableTableModel) {
        this.addTablePanel(tableTableModel);
        ((TableAttributeTab)this.getParent()).setLayerViewPanelUpdates(false);
        this.selectRows(tableTableModel.getTable());
        ((TableAttributeTab)this.getParent()).setLayerViewPanelUpdates(true);
    }

    @Override
    public void tableRemoved(TableTableModel tableTableModel) {
        this.removeTablePanel(tableTableModel);
    }

    public void jbInit() throws Exception {
        this.setLayout(new GridBagLayout());
    }

    private void removeTablePanel(TableTableModel tableTableModel) {
        Table table = tableTableModel.getTable();
        Assert.isTrue((boolean)this.tableToTablePanelMap.containsKey(table));
        TableAttributeTablePanel tablePanel = this.getTablePanel(table);
        this.remove(tablePanel);
        this.tableToTablePanelMap.remove(table);
        this.revalidate();
        this.repaint();
    }

    private void addTablePanel(TableTableModel tableTableModel) {
        Assert.isTrue((!this.tableToTablePanelMap.containsKey(tableTableModel.getTable()) ? 1 : 0) != 0);
        TableAttributeTablePanel tablePanel = new TableAttributeTablePanel(tableTableModel, this.addScrollPanesToChildren, this.workbenchContext, this);
        tablePanel.addListener(this);
        this.tableToTablePanelMap.put(tableTableModel.getTable(), tablePanel);
        this.add((Component)tablePanel, new GridBagConstraints(0, this.getComponentCount(), 1, 1, 1.0, 1.0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
        this.revalidate();
        this.repaint();
        tablePanel.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && ((TableAttributeTab)TableAttributePanel.this.getParent()).isLayerViewPanelUpdates()) {
                    TableAttributePanel.this.selectInLayerViewPanel();
                    ((TableAttributeTab)TableAttributePanel.this.getParent()).getToolBar().updateEnabledState();
                }
            }
        });
    }

    private void selectRows(Table table) {
        TableAttributeTablePanel tablePanel = this.tableToTablePanelMap.get(table);
        ListSelectionModel selectionModel = tablePanel.getTable().getSelectionModel();
        TableTableModel model = (TableTableModel)tablePanel.getTable().getModel();
        tablePanel.updateLabel();
    }

    public void inverseSelection() {
        for (TableAttributeTablePanel tablePanel : this.tableToTablePanelMap.values()) {
            Table table = tablePanel.getModel().getTable();
            this.inverseSelection(table);
        }
    }

    private void inverseSelection(Table table) {
        TableAttributeTablePanel tablePanel = this.tableToTablePanelMap.get(table);
        ListSelectionModel selectionModel = tablePanel.getTable().getSelectionModel();
        selectionModel.setValueIsAdjusting(true);
        TableTableModel model = (TableTableModel)tablePanel.getTable().getModel();
        int[] rowsSelected = tablePanel.getTable().getSelectedRows();
        selectionModel.removeSelectionInterval(0, tablePanel.getTable().getRowCount());
        int pos = 0;
        int j = 0;
        while (j < rowsSelected.length) {
            if (pos != rowsSelected[j]) {
                selectionModel.addSelectionInterval(pos, rowsSelected[j] - 1);
            }
            pos = rowsSelected[j] + 1;
            ++j;
        }
        if (pos <= tablePanel.getTable().getRowCount() - 1) {
            selectionModel.addSelectionInterval(pos, tablePanel.getTable().getRowCount() - 1);
        }
        this.selectInLayerViewPanel();
        tablePanel.updateLabel();
    }

    public void selectRecords(Collection<Record> col, Table table) {
        TableAttributeTablePanel tablePanel = this.tableToTablePanelMap.get(table);
        try {
            ListSelectionModel selectionModel = tablePanel.getTable().getSelectionModel();
            selectionModel.removeSelectionInterval(0, tablePanel.getTable().getRowCount());
            if (col == null || col.size() == 0) {
                return;
            }
            try {
                int posIni;
                TableTableModel model = (TableTableModel)tablePanel.getTable().getModel();
                ArrayList<Integer> rows = new ArrayList<Integer>();
                for (Record element : col) {
                    if (element.isUnsaved()) {
                        rows.add(new Integer(model.getRow(element)));
                        continue;
                    }
                    rows.add(new Integer(model.getRow(element.getPrimaryKey())));
                }
                Collections.sort(rows, new Comparator(){

                    public int compare(Object o1, Object o2) {
                        Integer i1 = (Integer)o1;
                        Integer i2 = (Integer)o2;
                        return i1.compareTo(i2);
                    }
                });
                int posFin = posIni = ((Integer)rows.get(0)).intValue();
                int j = 1;
                while (j < rows.size()) {
                    int pos = (Integer)rows.get(j);
                    if (pos == posFin + 1) {
                        posFin = pos;
                    } else {
                        selectionModel.addSelectionInterval(posIni, posFin);
                        posIni = pos;
                        posFin = pos;
                    }
                    ++j;
                }
                if (posIni != -1 && posFin != -1) {
                    selectionModel.addSelectionInterval(posIni, posFin);
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        finally {
            tablePanel.updateLabel();
        }
    }

    public int rowCount() {
        int rowCount = 0;
        for (TableAttributeTablePanel tablePanel : this.tableToTablePanelMap.values()) {
            rowCount += tablePanel.getTable().getRowCount();
        }
        return rowCount;
    }

    public boolean hasSelectedRecords() {
        for (TableAttributeTablePanel tablePanel : this.tableToTablePanelMap.values()) {
            int[] selectedRows = tablePanel.getTable().getSelectedRows();
            if (selectedRows.length <= 0) continue;
            return true;
        }
        return false;
    }

    public boolean tableWithSelectedRecordsAreEditable() {
        for (Table table : this.tableToTablePanelMap.keySet()) {
            TableAttributeTablePanel tablePanel = this.tableToTablePanelMap.get(table);
            int[] selectedRows = tablePanel.getTable().getSelectedRows();
            if (selectedRows.length <= 0 || table.getDataSource().isEditable()) continue;
            return false;
        }
        return true;
    }

    public void selectInLayerViewPanel() {
        for (TableAttributeTablePanel tablePanel : this.tableToTablePanelMap.values()) {
            tablePanel.updateLabel();
            Collection<Record> selectedRecords = tablePanel.getSelectedRecords();
            Table currentTable = tablePanel.getModel().getTable();
            Collection<Relation<?>> relations = currentTable.getAllRelations();
            if (!CollectionUtils.isNotEmpty(relations)) continue;
            this.updateRelationSelection(currentTable, relations, selectedRecords, false);
        }
    }

    public void updateRelationSelection(Table currentTable, Collection<Relation<?>> relations, Collection<Record> selectedRecords, boolean fireLayerSelection) {
        for (Relation<?> currentRelation : relations) {
            Object linkValue;
            String linkAttrName;
            if (!currentRelation.getRelationType().equals((Object)RelationType.RELATE)) continue;
            if (currentRelation instanceof LayerRelation) {
                LayerRelation layerRelation = (LayerRelation)currentRelation;
                linkAttrName = layerRelation.getSourceAttribute();
                HashSet<Feature> relationSelectedFeats = new HashSet<Feature>();
                for (Record selectedRecord : selectedRecords) {
                    linkValue = selectedRecord.getAttribute(linkAttrName);
                    try {
                        relationSelectedFeats.addAll(layerRelation.getRelationRecords(linkValue));
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                }
                if (!CollectionUtils.isNotEmpty(relationSelectedFeats)) continue;
                TaskFrame[] taskFrames = JUMPWorkbench.getFrameInstance().getTaskFrames();
                Layer targetLayer = layerRelation.getTargetLayer();
                TaskFrame[] taskFrameArray = taskFrames;
                int n = taskFrames.length;
                int e = 0;
                while (e < n) {
                    TaskFrame currentTaskFrame = taskFrameArray[e];
                    if (currentTaskFrame.isVisible() && currentTaskFrame.getLayerManager().getLayers().contains(targetLayer)) {
                        currentTaskFrame.getLayerViewPanel().getSelectionManager().getFeatureSelection().selectItems(layerRelation.getTargetLayer(), relationSelectedFeats, fireLayerSelection);
                    }
                    ++e;
                }
                continue;
            }
            if (!(currentRelation instanceof TableRelation)) continue;
            TableRelation tableRelation = (TableRelation)currentRelation;
            linkAttrName = tableRelation.getSourceAttribute();
            HashSet<Record> relationSelectedRecords = new HashSet<Record>();
            for (Record selectedRecord : selectedRecords) {
                linkValue = selectedRecord.getAttribute(linkAttrName);
                try {
                    relationSelectedRecords.addAll(tableRelation.getRelationRecords(linkValue));
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }
            Table targetTable = tableRelation.getTable();
            if (CollectionUtils.isNotEmpty(relationSelectedRecords)) {
                JUMPWorkbench.getFrameInstance().getContext().getDataManager().getRecordSelectionManager().selectRecords(targetTable, relationSelectedRecords);
                continue;
            }
            JUMPWorkbench.getFrameInstance().getContext().getDataManager().getRecordSelectionManager().clearSelection(targetTable, true);
        }
    }

    public Row topSelectedRow() {
        for (TableAttributeTablePanel panel : this.tableToTablePanelMap.values()) {
            int selectedRow = panel.getTable().getSelectedRow();
            if (selectedRow == -1) continue;
            return new BasicRow(panel, selectedRow);
        }
        return this.nullRow;
    }

    @Override
    public void selectionReplaced(TableAttributeTablePanel panel) {
        for (TableAttributeTablePanel tablePanel : this.tableToTablePanelMap.values()) {
            if (tablePanel == panel) continue;
            tablePanel.getTable().clearSelection();
        }
    }

    public void clearSelection() {
        for (TableAttributeTablePanel tablePanel : this.tableToTablePanelMap.values()) {
            tablePanel.getTable().clearSelection();
            tablePanel.updateLabel();
        }
    }

    public List<LayerListener> getTableListeners() {
        ArrayList<LayerListener> layerListeners = new ArrayList<LayerListener>();
        return layerListeners;
    }

    private class BasicRow
    implements Row {
        private TableAttributeTablePanel panel = null;
        private int index;

        public BasicRow(TableAttributeTablePanel panel, int index) {
            this.panel = panel;
            this.index = index;
        }

        @Override
        public boolean isFirstRow() {
            return this.panel.getModel().getTable() == TableAttributePanel.this.getModel().getTable() && this.index == 0;
        }

        @Override
        public boolean isLastRow() {
            return this.index == this.panel.getTable().getRowCount() - 1;
        }

        @Override
        public TableAttributeTablePanel getPanel() {
            return this.panel;
        }

        @Override
        public int getIndex() {
            return this.index;
        }

        @Override
        public Row previousRow() {
            if (this.isFirstRow()) {
                return this;
            }
            if (this.index > 0) {
                return new BasicRow(this.panel, this.index - 1);
            }
            return new BasicRow(this.previousPanel(), this.previousPanel().getTable().getRowCount() - 1);
        }

        @Override
        public Row nextRow() {
            if (this.isLastRow()) {
                return this;
            }
            if (this.index < this.panel.getTable().getRowCount() - 1) {
                return new BasicRow(this.panel, this.index + 1);
            }
            return new BasicRow(this.nextPanel(), 0);
        }

        private TableAttributeTablePanel previousPanel() {
            return TableAttributePanel.this.getTablePanel(this.previousTable());
        }

        private TableAttributeTablePanel nextPanel() {
            return TableAttributePanel.this.getTablePanel(this.nextTable());
        }

        private Table previousTable() {
            return TableAttributePanel.this.getModel().getTable();
        }

        private Table nextTable() {
            return TableAttributePanel.this.getModel().getTable();
        }

        @Override
        public Record getRecord() {
            return this.panel.getModel().getRecord(this.index);
        }
    }

    public static interface Row {
        public boolean isFirstRow();

        public boolean isLastRow();

        public TableAttributeTablePanel getPanel();

        public int getIndex();

        public Row nextRow();

        public Row previousRow();

        public Record getRecord();
    }
}

