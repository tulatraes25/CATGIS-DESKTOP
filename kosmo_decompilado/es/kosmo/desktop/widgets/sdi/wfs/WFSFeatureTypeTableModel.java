/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.ogcwebservices.wfs.capabilities.FormatType
 */
package es.kosmo.desktop.widgets.sdi.wfs;

import java.awt.Component;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.collections.CollectionUtils;
import org.deegree.datatypes.QualifiedName;
import org.deegree.ogcwebservices.wfs.capabilities.FormatType;
import org.saig.core.filter.Filter;
import org.saig.core.model.sdi.wfs.WFSFeatureTypeInfo;
import org.saig.jump.lang.I18N;

public class WFSFeatureTypeTableModel
extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private List<WFSFeatureTypeInfo> featTypeInfos;
    public static final String[] COLUMN_NAMES = new String[]{I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSFeatureTypeTableModel.feature-type"), I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSFeatureTypeTableModel.geometric-field"), I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSFeatureTypeTableModel.format"), I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSFeatureTypeTableModel.SRS"), I18N.getString("org.saig.jump.widgets.sdi.wfs.WFSFeatureTypeTableModel.filter")};
    public static final int FEATURE_TYPE_COLUMN = 0;
    public static final int GEOMETRY_COLUMN = 1;
    public static final int FORMAT_COLUMN = 2;
    public static final int SRS_COLUMN = 3;
    public static final int FILTER_COLUMN = 4;

    public WFSFeatureTypeTableModel() {
        this.initialize();
    }

    public void initialize() {
        this.featTypeInfos = new ArrayList<WFSFeatureTypeInfo>();
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
        return this.featTypeInfos.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        WFSFeatureTypeInfo info = this.featTypeInfos.get(rowIndex);
        if (info == null) {
            return null;
        }
        return this.getColumnValue(info, columnIndex);
    }

    public Collection<WFSFeatureTypeInfo> getFeatureTypeInfosAt(int[] selectedRows) {
        ArrayList<WFSFeatureTypeInfo> infos = new ArrayList<WFSFeatureTypeInfo>();
        int i = 0;
        while (i < selectedRows.length) {
            infos.add(this.featTypeInfos.get(selectedRows[i]));
            ++i;
        }
        return infos;
    }

    private Object getColumnValue(WFSFeatureTypeInfo info, int columnIndex) {
        Object value = null;
        switch (columnIndex) {
            case 0: {
                value = info.getPrettyString();
                break;
            }
            case 1: {
                value = info.getGeomAttrName();
                break;
            }
            case 2: {
                value = info.getSelectedFormat();
                break;
            }
            case 3: {
                value = info.getSelectedSRS();
                break;
            }
            case 4: {
                value = info.getQueryFilter();
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
        WFSFeatureTypeInfo info = this.featTypeInfos.get(row);
        this.setValueAt(info, row, col, value);
        this.fireTableRowsUpdated(row, row);
    }

    private void setValueAt(WFSFeatureTypeInfo info, int rowIndex, int columnIndex, Object value) {
        switch (columnIndex) {
            case 1: {
                info.setGeomAttrName((QualifiedName)value);
                break;
            }
            case 2: {
                info.setSelectedFormat((FormatType)value);
                break;
            }
            case 3: {
                info.setSelectedSRS((URI)value);
                break;
            }
            case 4: {
                info.setQueryFilter((Filter)value);
                break;
            }
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (row >= this.featTypeInfos.size()) {
            return false;
        }
        return col != 0;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        Class columnClass = String.class;
        switch (col) {
            case 0: 
            case 1: {
                columnClass = QualifiedName.class;
                break;
            }
            case 2: {
                columnClass = FormatType.class;
                break;
            }
            case 3: {
                columnClass = URI.class;
                break;
            }
            case 4: {
                columnClass = Filter.class;
            }
        }
        return columnClass;
    }

    public void setFeatTypeInfos(List<WFSFeatureTypeInfo> featTypeInfos) {
        this.featTypeInfos = featTypeInfos;
    }

    public Object[] getValuesFor(int row, int column) {
        WFSFeatureTypeInfo info = this.featTypeInfos.get(row);
        if (info == null) {
            return null;
        }
        return this.getValuesFor(info, column);
    }

    private Object[] getValuesFor(WFSFeatureTypeInfo info, int column) {
        Object[] values = null;
        switch (column) {
            case 1: {
                List<QualifiedName> selectedGeomProperties = info.getGeometryAttributes();
                if (CollectionUtils.isEmpty(selectedGeomProperties)) break;
                values = new Object[]{selectedGeomProperties.size()};
                values = selectedGeomProperties.toArray(values);
                break;
            }
            case 2: {
                values = info.getAvailableFormats();
                break;
            }
            case 3: {
                values = info.getAvailableSRS();
                break;
            }
            default: {
                values = null;
            }
        }
        return values;
    }

    public ListCellRenderer getCellRendererFor(int row, int column) {
        WFSFeatureTypeInfo info = this.featTypeInfos.get(row);
        if (info == null) {
            return null;
        }
        return this.getCellRendererFor(info, column);
    }

    private ListCellRenderer getCellRendererFor(WFSFeatureTypeInfo info, int column) {
        DefaultListCellRenderer renderer = null;
        switch (column) {
            case 1: {
                renderer = new DefaultListCellRenderer();
                break;
            }
            case 2: {
                renderer = new DefaultListCellRenderer(){
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
                        if (value instanceof FormatType) {
                            this.setText(((FormatType)value).getValue());
                        }
                        return this;
                    }
                };
                break;
            }
            case 3: {
                renderer = new DefaultListCellRenderer();
                break;
            }
            case 4: {
                break;
            }
            default: {
                renderer = new DefaultListCellRenderer();
            }
        }
        return renderer;
    }
}

