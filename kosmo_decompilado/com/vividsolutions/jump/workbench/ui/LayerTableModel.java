/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.ColumnBasedTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import javax.swing.event.TableModelEvent;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.Attribute;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.core.util.LRUCache;
import org.saig.jump.lang.I18N;

public class LayerTableModel
extends ColumnBasedTableModel {
    private static final Logger LOGGER = Logger.getLogger(LayerTableModel.class);
    public static final String GEOMETRY_COLUMN_NAME = "....";
    private Layer layer;
    private FeatureSchema schema;
    private List<Object> keys = new ArrayList<Object>();
    private LRUCache cache = new LRUCache(100);
    private String sortedColumnName = null;
    private String pkName = null;
    private boolean sortAscending = false;
    private boolean isSorted;
    private ColumnBasedTableModel.Column buttonColumn = new MyColumn(new Attribute("....", "....", true, null), null){

        @Override
        protected Object getValue(Feature feature) {
            return null;
        }

        @Override
        protected void setValue(Object value, Feature feature) {
            Assert.shouldNeverReachHere();
        }
    };
    private LayerListener layerListener = new LayerListener(){

        @Override
        public void categoryChanged(CategoryEvent e) {
        }

        @Override
        public void featuresChanged(FeatureEvent e) {
            if (e.getLayer() != LayerTableModel.this.getLayer()) {
                return;
            }
            if (e.getType() == FeatureEventType.DELETED) {
                LayerTableModel.this.removeAll(e.getFeatures());
            }
            if (e.getType() == FeatureEventType.ATTRIBUTES_MODIFIED) {
                for (Feature feature : e.getFeatures()) {
                    int row = LayerTableModel.this.keys.indexOf(feature.getPrimaryKey());
                    if (row == -1) continue;
                    LayerTableModel.this.fireTableChanged(new TableModelEvent(LayerTableModel.this, row, row));
                }
            }
        }

        @Override
        public void layerChanged(LayerEvent e) {
            if (e.getLayerable() != LayerTableModel.this.getLayer()) {
                return;
            }
            if (e.getType() == LayerEventType.METADATA_CHANGED && !LayerTableModel.this.schema.equals(LayerTableModel.this.layer.getFeatureCollectionWrapper().getFeatureSchema(), true)) {
                LayerTableModel.this.initColumns(LayerTableModel.this.layer);
                LayerTableModel.this.fireTableChanged(new TableModelEvent(LayerTableModel.this, -1));
            }
        }
    };

    public LayerTableModel(Layer layer) {
        this.layer = layer;
        layer.getLayerManager().addLayerListener(this.layerListener);
        this.initColumns(layer);
    }

    public Object getKey(int row) {
        return this.keys.get(row);
    }

    public int getRow(Object value) {
        return this.keys.indexOf(value);
    }

    public void initColumns(final Layer layer) {
        this.schema = layer.getFeatureCollectionWrapper().getFeatureSchema();
        this.pkName = this.schema.getPrimaryKeyName();
        ArrayList<ColumnBasedTableModel.Column> columns = new ArrayList<ColumnBasedTableModel.Column>();
        columns.add(this.buttonColumn);
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            if (this.schema.getVisibility(i).booleanValue() && this.schema.getAttributeType(i) != AttributeType.GEOMETRY) {
                final int j = i;
                columns.add(new MyColumn(this.schema.getAttribute(i), this.schema.getAttributeType(i).toJavaClass()){

                    @Override
                    protected Object getValue(Feature feature) {
                        if (feature == null) {
                            return null;
                        }
                        return feature.getAttribute(j);
                    }

                    @Override
                    protected void setValue(Object value, final Feature feature) {
                        Object oldValue = feature.getAttribute(j);
                        if (oldValue == null && value == null || oldValue != null && oldValue.equals(value)) {
                            return;
                        }
                        final Feature oldAttributes = (Feature)feature.clone();
                        final Feature newAttributes = (Feature)feature.clone();
                        newAttributes.setAttribute(j, value);
                        layer.getLayerManager().getUndoableEditReceiver().startReceiving();
                        try {
                            try {
                                UndoableCommand command = new UndoableCommand(String.valueOf(I18N.getString("workbench.ui.LayerTableModel.edit")) + LayerTableModel.this.schema.getAttributeName(j)){

                                    @Override
                                    public void execute() throws Exception {
                                        try {
                                            LayerTableModel.this.setAttributesOf(feature, newAttributes);
                                        }
                                        catch (Exception e) {
                                            this.unexecute();
                                            JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                                        }
                                    }

                                    @Override
                                    public void unexecute() throws Exception {
                                        LayerTableModel.this.setAttributesOf(feature, oldAttributes);
                                    }
                                };
                                command.execute();
                                layer.getLayerManager().getUndoableEditReceiver().receive(command.toUndoableEdit());
                            }
                            catch (Exception e) {
                                LOGGER.error((Object)"", (Throwable)e);
                                layer.getLayerManager().getUndoableEditReceiver().stopReceiving();
                            }
                        }
                        finally {
                            layer.getLayerManager().getUndoableEditReceiver().stopReceiving();
                        }
                    }
                });
            }
            ++i;
        }
        this.setColumns(columns);
    }

    private void setAttributesOf(Feature feature, Feature attributes) throws Exception {
        int i = 0;
        while (i < feature.getSchema().getAttributeCount()) {
            feature.setAttribute(i, attributes.getAttribute(i));
            ++i;
        }
        this.layer.getUltimateFeatureCollectionWrapper().update(feature);
        this.layer.getLayerManager().fireFeaturesChanged(Arrays.asList(feature), FeatureEventType.ATTRIBUTES_MODIFIED, this.layer);
    }

    public Layer getLayer() {
        return this.layer;
    }

    public Feature getFeature(int row) {
        int i;
        Object key = this.keys.get(row);
        if (key instanceof Feature) {
            return (Feature)key;
        }
        Object value = this.cache.get(key);
        if (value != null) {
            return (Feature)value;
        }
        ArrayList<Object> keys_ = new ArrayList<Object>();
        if (row == 0) {
            i = 1;
            while (i < 100 && i < this.keys.size()) {
                keys_.add(this.keys.get(i));
                ++i;
            }
        } else if (row == this.keys.size()) {
            i = this.keys.size();
            while (i < 100 && i > 0) {
                keys_.add(this.keys.get(i));
                ++i;
            }
        } else {
            i = 1;
            while (i < 50 && row - i > 0) {
                keys_.add(this.keys.get(row - i));
                ++i;
            }
            int size = this.keys.size();
            int i2 = 1;
            while (i2 < 50 && row + i2 < size) {
                keys_.add(this.keys.get(row + i2));
                ++i2;
            }
        }
        Feature feat = this.layer.getUltimateFeatureCollectionWrapper().getByPrimaryKey(key);
        this.cache.add(key, feat);
        List<Feature> features = this.layer.getUltimateFeatureCollectionWrapper().getByPrimaryKeys(keys_.toArray());
        for (Feature element : features) {
            if (element == null) continue;
            this.cache.add(element.getPrimaryKey(), element);
        }
        return feat;
    }

    @Override
    public int getRowCount() {
        return this.keys.size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (!this.layer.isEditable()) {
            return false;
        }
        ColumnBasedTableModel.Column column = this.getColumn(columnIndex);
        if (column == this.buttonColumn) {
            return false;
        }
        Attribute attr = this.layer.getFeatureCollectionWrapper().getFeatureSchema().getAttribute(column.getAttribute().getName());
        return !attr.isPrimaryKey() && !attr.isCalculated() && !attr.getType().equals(AttributeType.OBJECT) && (!this.layer.isVersionable() || !this.checkVersionableColumn(this.layer, attr.getName()));
    }

    private boolean checkVersionableColumn(Layer layer, String attrName) {
        return attrName.equals(layer.getEndDateField()) || attrName.equals(layer.getStartDateField()) || attrName.equals(layer.getHistoryField());
    }

    public void clear(boolean fireEvents) {
        this.keys.clear();
        this.cache.clear();
        if (fireEvents) {
            this.fireTableChanged(new TableModelEvent(this));
        }
    }

    public void clear() {
        this.clear(true);
    }

    public void removeAll(Collection<Feature> featuresToRemove) {
        for (Feature feature : featuresToRemove) {
            int row = -1;
            if (this.keys.contains(feature)) {
                row = this.keys.indexOf(feature);
                this.keys.remove(feature);
            } else {
                Object key = feature.getPrimaryKey();
                row = this.keys.indexOf(key);
                if (row == -1) continue;
                this.keys.remove(key);
            }
            this.fireTableChanged(new TableModelEvent(this, row, row, -1, -1));
        }
    }

    public void addAll(Collection<Feature> features) {
        int originalFeaturesSize = this.keys.size();
        ArrayList<Object> keys_ = new ArrayList<Object>();
        for (Feature element : features) {
            if (element.isUnsaved()) {
                keys_.add(element);
                continue;
            }
            keys_.add(element.getPrimaryKey());
        }
        this.keys.addAll(keys_);
        if (this.sortedColumnName != null) {
            this.sort(this.sortedColumnName, this.sortAscending);
        }
        this.fireTableChanged(new TableModelEvent(this, originalFeaturesSize, this.keys.size() - 1, -1, 1));
    }

    public void addAllKeys(Collection<Object> newKeys) {
        int originalFeaturesSize = this.keys.size();
        this.keys.addAll(newKeys);
        if (this.sortedColumnName != null) {
            this.sort(this.sortedColumnName, this.sortAscending);
        }
        this.fireTableChanged(new TableModelEvent(this, originalFeaturesSize, this.keys.size() - 1, -1, 1));
    }

    public void dispose() {
        if (this.layer.getLayerManager() != null) {
            this.layer.getLayerManager().removeLayerListener(this.layerListener);
        }
        this.keys.clear();
    }

    public LayerListener getLayerListener() {
        return this.layerListener;
    }

    public List<Feature> getFeatures() {
        throw new I18NUnsupportedOperationException();
    }

    public String getSortedColumnName() {
        return this.sortedColumnName;
    }

    public boolean isSortAscending() {
        return this.sortAscending;
    }

    public void sortSelectedRows(final boolean ascending, int[] selectedRows) {
        final Hashtable<Object, String> indexes = new Hashtable<Object, String>();
        int i = 0;
        while (i < selectedRows.length) {
            indexes.put(this.keys.get(selectedRows[i]), "");
            ++i;
        }
        Collections.sort(this.keys, new Comparator<Object>(){

            @Override
            public int compare(Object o1, Object o2) {
                return this.ascendingCompare(o1, o2) * (ascending ? 1 : -1);
            }

            private int ascendingCompare(Object key1, Object key2) {
                boolean check1 = indexes.containsKey(key1);
                boolean check2 = indexes.containsKey(key2);
                if (check1 && check2 || !check1 && !check2) {
                    return 0;
                }
                if (!check1) {
                    return 1;
                }
                return -1;
            }
        });
        this.isSorted = true;
    }

    public void sort(String columnName) {
        String name;
        Attribute attr = this.schema.getPublicAttribute(columnName);
        this.sort(name, (name = attr.getName()).equals(this.sortedColumnName) ? !this.sortAscending : true);
    }

    public void explicitSort(String columnName, boolean ascending) {
        Attribute attr = this.schema.getPublicAttribute(columnName);
        String name = attr.getName();
        this.sort(name, ascending);
    }

    private void sort(String columnName, boolean ascending) {
        this.sortAscending = ascending;
        this.sortedColumnName = columnName;
        FeatureSchema schema = this.layer.getUltimateFeatureCollectionWrapper().getFeatureSchema();
        if (!schema.hasAttribute(columnName)) {
            return;
        }
        int size = -1;
        try {
            size = this.layer.getUltimateFeatureCollectionWrapper().size();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        if (this.keys.size() == size) {
            LOGGER.debug((Object)I18N.getString(this.getClass(), "retrieving-all-keys"));
            this.keys = this.layer.getUltimateFeatureCollectionWrapper().getSortKeys(columnName, ascending, null);
        } else {
            this.keys = this.layer.getUltimateFeatureCollectionWrapper().getSortKeys(columnName, ascending, this.keys.toArray());
        }
    }

    public String getType(int column) {
        return null;
    }

    public boolean isPKName(String currentColumn) {
        return currentColumn.equals(this.pkName);
    }

    public boolean isSort() {
        return this.sortedColumnName != null || this.isSorted;
    }

    public void updateAll(Collection<Feature> features, Collection<Feature> oldFeatureClones) {
        for (Feature currentFeature : features) {
            int index;
            if (!currentFeature.isUnsaved() || (index = this.keys.indexOf(currentFeature)) == -1) continue;
            this.keys.set(index, currentFeature);
        }
        this.cache.clear();
    }

    private abstract class MyColumn
    extends ColumnBasedTableModel.Column {
        public MyColumn(Attribute attribute, Class<?> dataClass) {
            super(attribute, dataClass);
        }

        @Override
        public Object getValueAt(int rowIndex) {
            return this.getValue(LayerTableModel.this.getFeature(rowIndex));
        }

        @Override
        public void setValueAt(Object value, int rowIndex) {
            this.setValue(value, LayerTableModel.this.getFeature(rowIndex));
        }

        protected abstract Object getValue(Feature var1);

        protected abstract void setValue(Object var1, Feature var2);
    }
}

