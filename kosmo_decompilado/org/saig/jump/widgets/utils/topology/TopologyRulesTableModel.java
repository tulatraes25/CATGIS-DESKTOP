/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.utils.topology;

import com.vividsolutions.jump.workbench.model.Layerable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;
import org.saig.core.filter.Filter;
import org.saig.core.model.relations.topology.ITopologyBinaryRelation;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.core.model.relations.topology.TopologyRelationsRepository;
import org.saig.jump.lang.I18N;

public class TopologyRulesTableModel
extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TopologyRulesTableModel.class);
    private List<ITopologyRelation> topologyRules;
    public static final String[] COLUMN_NAMES = new String[]{I18N.getString(TopologyRulesTableModel.class, "source-layer"), I18N.getString(TopologyRulesTableModel.class, "input-filter"), I18N.getString(TopologyRulesTableModel.class, "topological-rule"), I18N.getString(TopologyRulesTableModel.class, "additional-condition"), I18N.getString(TopologyRulesTableModel.class, "crossing-layer"), I18N.getString(TopologyRulesTableModel.class, "output-filter"), I18N.getString(TopologyRulesTableModel.class, "active")};
    public static final int SOURCE_LAYER_COLUMN = 0;
    public static final int ENTRY_SOURCE_FILTER_COLUMN = 1;
    public static final int TOPOLOGY_RULE_COLUMN = 2;
    public static final int CONDITION_FILTER_COLUMN = 3;
    public static final int TARGET_LAYER_COLUMN = 4;
    public static final int ENTRY_TARGET_FILTER_COLUMN = 5;
    public static final int ENABLED_COLUMN = 6;

    public TopologyRulesTableModel() {
        this.initialize();
    }

    public void initialize() {
        this.topologyRules = new ArrayList<ITopologyRelation>();
    }

    public void setTopologyRulesList(List<ITopologyRelation> topologyRules) {
        this.topologyRules = topologyRules;
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMN_NAMES[col];
    }

    @Override
    public int getRowCount() {
        return this.topologyRules.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ITopologyRelation relation = this.topologyRules.get(rowIndex);
        if (relation == null) {
            return null;
        }
        return this.getColumnValue(relation, columnIndex);
    }

    public Collection<ITopologyRelation> getTopologyRulesAt(int[] selectedRows) {
        ArrayList<ITopologyRelation> relations = new ArrayList<ITopologyRelation>();
        int i = 0;
        while (i < selectedRows.length) {
            relations.add(this.topologyRules.get(selectedRows[i]));
            ++i;
        }
        return relations;
    }

    private Object getColumnValue(ITopologyRelation relation, int columnIndex) {
        Object value = null;
        switch (columnIndex) {
            case 0: {
                value = relation.getSourceLayerName();
                break;
            }
            case 2: {
                value = relation.getName();
                break;
            }
            case 4: {
                if (relation instanceof ITopologyBinaryRelation) {
                    value = ((ITopologyBinaryRelation)relation).getTargetLayerName();
                    break;
                }
                return null;
            }
            case 1: {
                value = relation.getEntrySourceFilter();
                break;
            }
            case 5: {
                if (relation instanceof ITopologyBinaryRelation) {
                    value = ((ITopologyBinaryRelation)relation).getEntryTargetFilter();
                    break;
                }
                return null;
            }
            case 3: {
                value = relation.getAlphanumericFilter();
                break;
            }
            case 6: {
                value = relation.isEnabled();
                break;
            }
            default: {
                value = null;
            }
        }
        return value;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        ITopologyRelation relation = this.topologyRules.get(row);
        this.setValueAt(relation, row, col, value);
        this.fireTableRowsUpdated(row, row);
    }

    private void setValueAt(ITopologyRelation relation, int rowIndex, int columnIndex, Object value) {
        switch (columnIndex) {
            case 0: {
                relation.setSourceLayerName(((Layerable)value).getName());
                break;
            }
            case 2: {
                if (relation.getName().equals(((ITopologyRelation)value).getName())) break;
                try {
                    this.createAndReplaceRelation(relation, ((ITopologyRelation)value).getId(), rowIndex);
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                break;
            }
            case 4: {
                if (!(relation instanceof ITopologyBinaryRelation)) break;
                ((ITopologyBinaryRelation)relation).setTargetLayerName(((Layerable)value).getName());
                break;
            }
            case 1: {
                relation.setEntrySourceFilter((Filter)value);
                break;
            }
            case 5: {
                if (!(relation instanceof ITopologyBinaryRelation)) break;
                ((ITopologyBinaryRelation)relation).setEntryTargetFilter((Filter)value);
                break;
            }
            case 3: {
                relation.setAlphanumericFilter((Filter)value);
                break;
            }
            case 6: {
                relation.setEnabled((Boolean)value);
            }
        }
    }

    private void createAndReplaceRelation(ITopologyRelation oldRelation, String newRelationName, int rowIndex) throws Exception {
        ITopologyRelation newRelation = TopologyRelationsRepository.getTopologyRelation(newRelationName);
        newRelation.setSourceLayerName(oldRelation.getSourceLayerName());
        newRelation.setAlphanumericFilter(oldRelation.getAlphanumericFilter());
        newRelation.setEntrySourceFilter(oldRelation.getEntrySourceFilter());
        newRelation.setEnabled(oldRelation.isEnabled());
        if (oldRelation instanceof ITopologyBinaryRelation && newRelation instanceof ITopologyBinaryRelation) {
            ITopologyBinaryRelation newBinaryRelation = (ITopologyBinaryRelation)newRelation;
            ITopologyBinaryRelation oldBinaryRelation = (ITopologyBinaryRelation)oldRelation;
            newBinaryRelation.setTargetLayerName(oldBinaryRelation.getTargetLayerName());
            newBinaryRelation.setEntryTargetFilter(oldBinaryRelation.getEntryTargetFilter());
        }
        this.topologyRules.set(rowIndex, newRelation);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (row >= this.topologyRules.size()) {
            return false;
        }
        ITopologyRelation relation = this.topologyRules.get(row);
        return relation instanceof ITopologyBinaryRelation || col != 4 && col != 5;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        Class columnClass = String.class;
        switch (col) {
            case 1: 
            case 3: 
            case 5: {
                columnClass = Filter.class;
                break;
            }
            case 6: {
                columnClass = Boolean.class;
            }
        }
        return columnClass;
    }

    public String getAssociatedLayerName(int row, int column) {
        String associatedLayerName = null;
        switch (column) {
            case 1: {
                associatedLayerName = (String)this.getValueAt(row, 0);
                break;
            }
            case 5: {
                associatedLayerName = (String)this.getValueAt(row, 4);
                break;
            }
        }
        return associatedLayerName;
    }

    public List<ITopologyRelation> getTopologyRules() {
        return this.topologyRules;
    }
}

