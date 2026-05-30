/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.utils.topology;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.commons.lang.StringUtils;
import org.saig.core.filter.Filter;
import org.saig.core.filter.visitor.FilterToStringTranslator;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.JAvailableLayersComboBox;
import org.saig.jump.widgets.utils.topology.FilterTableCellEditor;
import org.saig.jump.widgets.utils.topology.FilterTableCellRenderer;
import org.saig.jump.widgets.utils.topology.JAvailableTopologyRulesComboBox;
import org.saig.jump.widgets.utils.topology.TopologyRulesTableModel;

public class ConfigureTopologyRulesTablePanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private JTable topologyRulesTable;
    private String filterLayerName;

    public ConfigureTopologyRulesTablePanel() {
        this((String)null);
    }

    public ConfigureTopologyRulesTablePanel(String filterLayerName) {
        this.filterLayerName = filterLayerName;
        this.setLayout(new BorderLayout());
        GUIUtil.chooseGoodColumnWidths(this.getTopologyRulesTable());
        this.add((Component)this.getTopologyRulesTable(), "Center");
    }

    private JTable getTopologyRulesTable() {
        if (this.topologyRulesTable == null) {
            this.topologyRulesTable = new JTable(new TopologyRulesTableModel()){
                private static final long serialVersionUID = 1L;
                private FilterToStringTranslator translator;
                {
                    this.translator = new FilterToStringTranslator();
                }

                @Override
                public String getToolTipText(MouseEvent e) {
                    String tip = null;
                    Point p = e.getPoint();
                    int rowIndex = this.rowAtPoint(p);
                    int colIndex = this.columnAtPoint(p);
                    if (rowIndex == -1 || colIndex == -1) {
                        return super.getToolTipText();
                    }
                    int realColumnIndex = this.convertColumnIndexToModel(colIndex);
                    Object value = null;
                    switch (realColumnIndex) {
                        case 0: {
                            tip = "<HTML><B>" + I18N.getString(this.getClass(), "source-layer-to-apply-the-topology-rule-to") + ":</B> " + this.getValueAt(rowIndex, colIndex) + "</HTML>";
                            break;
                        }
                        case 2: {
                            tip = "<HTML><B>" + I18N.getString(this.getClass(), "topological-rule") + ":</B> " + this.getValueAt(rowIndex, colIndex);
                            break;
                        }
                        case 4: {
                            value = this.getValueAt(rowIndex, colIndex);
                            if (value == null) break;
                            tip = "<HTML><B>" + I18N.getString(this.getClass(), "target-layer-against-which-to-apply-the-topological-rule") + ":</B> " + value;
                            break;
                        }
                        case 1: {
                            value = this.getValueAt(rowIndex, colIndex);
                            tip = "<HTML><B>" + I18N.getString(this.getClass(), "input-layer-filter") + ":</B> ";
                            if (value != null) {
                                tip = String.valueOf(tip) + this.translator.translateFilter((Filter)value);
                                break;
                            }
                            tip = String.valueOf(tip) + I18N.getString(this.getClass(), "without-filter-assigned");
                            break;
                        }
                        case 5: {
                            value = this.getValueAt(rowIndex, colIndex);
                            tip = "<HTML><B>" + I18N.getString(this.getClass(), "output-layer-filter") + ":</B> ";
                            if (value != null) {
                                tip = String.valueOf(tip) + this.translator.translateFilter((Filter)value);
                                break;
                            }
                            tip = String.valueOf(tip) + I18N.getString(this.getClass(), "without-filter-assigned");
                            break;
                        }
                        case 3: {
                            value = this.getValueAt(rowIndex, colIndex);
                            tip = "<HTML><B>" + I18N.getString(this.getClass(), "additional-condition-for-rule") + ":</B> ";
                            if (value != null) {
                                tip = String.valueOf(tip) + this.translator.translateFilter((Filter)value);
                                break;
                            }
                            tip = String.valueOf(tip) + I18N.getString(this.getClass(), "without-additional-condition-assigned");
                            break;
                        }
                        default: {
                            tip = super.getToolTipText(e);
                        }
                    }
                    return tip;
                }

                @Override
                protected JTableHeader createDefaultTableHeader() {
                    return new JTableHeader(this.columnModel){
                        private static final long serialVersionUID = 1L;

                        @Override
                        public String getToolTipText(MouseEvent e) {
                            String tip = null;
                            Point p = e.getPoint();
                            int index = this.columnModel.getColumnIndexAtX(p.x);
                            int realIndex = this.columnModel.getColumn(index).getModelIndex();
                            switch (realIndex) {
                                case 0: {
                                    tip = I18N.getString(this.getClass(), "shows-the-name-of-the-source-layer-to-apply-the-topological-rule-to");
                                    break;
                                }
                                case 2: {
                                    tip = I18N.getString(this.getClass(), "shows-the-type-of-the-topological-rule-to-apply");
                                    break;
                                }
                                case 4: {
                                    tip = I18N.getString(this.getClass(), "shows-the-name-of-the-target-layer-against-which-to-apply-the-topological-rule");
                                    break;
                                }
                                default: {
                                    tip = super.getToolTipText(e);
                                }
                            }
                            return tip;
                        }
                    };
                }

                @Override
                public void tableChanged(TableModelEvent e) {
                    if (this.isEditing()) {
                        TableCellEditor _editor = this.getCellEditor();
                        _editor.cancelCellEditing();
                    }
                    super.tableChanged(e);
                }
            };
            this.topologyRulesTable.getTableHeader().setReorderingAllowed(false);
            ((DefaultTableCellRenderer)this.topologyRulesTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(0);
            this.topologyRulesTable.setDefaultRenderer(Filter.class, new FilterTableCellRenderer());
            this.buildTableParameters(this.topologyRulesTable);
        }
        return this.topologyRulesTable;
    }

    public void buildTableParameters(JTable table) {
        ArrayList<String> validSourceLayers = new ArrayList<String>();
        if (!StringUtils.isEmpty((String)this.filterLayerName)) {
            validSourceLayers.add(this.filterLayerName);
        }
        TableColumn sourceLayerColumn = table.getColumnModel().getColumn(0);
        sourceLayerColumn.setCellEditor(new DefaultCellEditor(new JAvailableLayersComboBox(JUMPWorkbench.getFrameInstance().getContext().getLayerManager(), false, false, true, validSourceLayers, null)));
        TableColumn targetLayerColumn = table.getColumnModel().getColumn(4);
        targetLayerColumn.setCellEditor(new DefaultCellEditor(new JAvailableLayersComboBox(JUMPWorkbench.getFrameInstance().getContext().getLayerManager(), false, false, true, null, validSourceLayers)));
        TableColumn topologyRuleColumn = table.getColumnModel().getColumn(2);
        topologyRuleColumn.setCellEditor(new DefaultCellEditor(new JAvailableTopologyRulesComboBox()));
        TableColumn entrySourceFilterColumn = table.getColumnModel().getColumn(1);
        entrySourceFilterColumn.setCellEditor(new FilterTableCellEditor());
        TableColumn entryTargetFilterColumn = table.getColumnModel().getColumn(5);
        entryTargetFilterColumn.setCellEditor(new FilterTableCellEditor());
        TableColumn conditionRuleFilterColumn = table.getColumnModel().getColumn(3);
        conditionRuleFilterColumn.setCellEditor(new FilterTableCellEditor());
        TableRowSorter<TopologyRulesTableModel> sorter = new TableRowSorter<TopologyRulesTableModel>((TopologyRulesTableModel)table.getModel());
        RowFilter<TopologyRulesTableModel, Integer> entryLayerFilter = new RowFilter<TopologyRulesTableModel, Integer>(){

            @Override
            public boolean include(RowFilter.Entry<? extends TopologyRulesTableModel, ? extends Integer> entry) {
                TopologyRulesTableModel model = entry.getModel();
                String layerName = (String)model.getValueAt(entry.getIdentifier(), 0);
                return ConfigureTopologyRulesTablePanel.this.filterLayerName == null || ConfigureTopologyRulesTablePanel.this.filterLayerName.equals(layerName);
            }
        };
        sorter.setRowFilter(entryLayerFilter);
        table.setRowSorter(sorter);
    }

    public void setTopologyRulesList(List<ITopologyRelation> topologyRulesList) {
        ((TopologyRulesTableModel)this.getTopologyRulesTable().getModel()).setTopologyRulesList(topologyRulesList);
        ((TopologyRulesTableModel)this.topologyRulesTable.getModel()).fireTableDataChanged();
    }

    public int getTableSize() {
        return this.topologyRulesTable.getTableHeader().getPreferredSize().width;
    }

    public List<ITopologyRelation> getTopologyRules() {
        return ((TopologyRulesTableModel)this.topologyRulesTable.getModel()).getTopologyRules();
    }

    public void addTopologyRules(Collection<ITopologyRelation> newRelations) {
        ((TopologyRulesTableModel)this.topologyRulesTable.getModel()).getTopologyRules().addAll(newRelations);
        ((TopologyRulesTableModel)this.topologyRulesTable.getModel()).fireTableDataChanged();
    }

    public void removeSelectedTopologyRules() {
        int[] selectedRows = this.topologyRulesTable.getSelectedRows();
        int i = 0;
        while (i < selectedRows.length) {
            selectedRows[i] = this.topologyRulesTable.convertRowIndexToModel(selectedRows[i]);
            ++i;
        }
        Collection<ITopologyRelation> selectedRelations = ((TopologyRulesTableModel)this.topologyRulesTable.getModel()).getTopologyRulesAt(selectedRows);
        ((TopologyRulesTableModel)this.topologyRulesTable.getModel()).getTopologyRules().removeAll(selectedRelations);
        ((TopologyRulesTableModel)this.topologyRulesTable.getModel()).fireTableDataChanged();
    }

    public boolean hasSelectedTopologyRules() {
        return this.topologyRulesTable.getSelectedRows().length > 0;
    }

    public void removeAllTopologyRules() {
        RowSorter<? extends TableModel> topologyRulesRowSorter = this.topologyRulesTable.getRowSorter();
        if (topologyRulesRowSorter == null || topologyRulesRowSorter.getViewRowCount() == topologyRulesRowSorter.getModelRowCount()) {
            ((TopologyRulesTableModel)this.topologyRulesTable.getModel()).getTopologyRules().clear();
        } else {
            int[] selectedRows = new int[topologyRulesRowSorter.getViewRowCount()];
            int i = 0;
            while (i < topologyRulesRowSorter.getViewRowCount()) {
                selectedRows[i] = this.topologyRulesTable.convertRowIndexToModel(i);
                ++i;
            }
            Collection<ITopologyRelation> selectedRelations = ((TopologyRulesTableModel)this.topologyRulesTable.getModel()).getTopologyRulesAt(selectedRows);
            ((TopologyRulesTableModel)this.topologyRulesTable.getModel()).getTopologyRules().removeAll(selectedRelations);
        }
        ((TopologyRulesTableModel)this.topologyRulesTable.getModel()).fireTableDataChanged();
    }

    public JTableHeader getTableHeader() {
        return this.topologyRulesTable.getTableHeader();
    }

    public void addListSelectionListener(ListSelectionListener selectionListener) {
        this.topologyRulesTable.getSelectionModel().addListSelectionListener(selectionListener);
    }

    public void refresh(String selectedLayerName) {
        this.filterLayerName = selectedLayerName;
        this.buildTableParameters(this.topologyRulesTable);
    }
}

