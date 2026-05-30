/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.core.gui.swing.dataComponents.layers;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.model.Layer;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JList;
import javax.swing.JScrollPane;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.dataComponents.DataListComponent;

public class JLayerListScrollPane
extends JScrollPane
implements DataListComponent<Feature> {
    private static final long serialVersionUID = 1L;
    protected Layer layer;
    protected String field;
    protected String keyField;
    protected Object keyValue;
    protected JList list;
    protected String fieldOrdered;
    private Filter filter;

    public JLayerListScrollPane(Layer layer, String field, String keyField, int width, int height) {
        this(layer, field, keyField, width, height, null);
    }

    public JLayerListScrollPane(Layer layer, String field, String keyField, int filas, int cols, String fieldOrdered) {
        super(22, 31);
        this.layer = layer;
        this.field = field;
        this.keyField = keyField;
        this.fieldOrdered = fieldOrdered;
        this.list = new JList();
        this.setViewportView(this.list);
        this.setMinimumSize(new Dimension(filas, cols));
        this.setPreferredSize(new Dimension(filas, cols));
        this.refresh();
    }

    @Override
    public void selectItemByValue(Object key) {
        if (key != null) {
            this.list.setSelectedValue(key, true);
        }
    }

    public JList getList() {
        return this.list;
    }

    @Override
    public List<Feature> getRowsByValue(Object value) {
        if (value != null) {
            FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
            return fc.getByAttribute(new String[]{this.field}, new Object[]{value}, this.fieldOrdered, this.filter);
        }
        return null;
    }

    @Override
    public void refresh() {
        this.list.removeAll();
        FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
        List<Feature> features = null;
        features = this.keyValue == null ? fc.getFeatures() : fc.getByAttribute(new String[]{this.keyField}, new Object[]{this.keyValue}, this.fieldOrdered, this.filter);
        if (CollectionUtils.isEmpty(features)) {
            this.list.setListData(new Vector());
            return;
        }
        Object[] values = new Object[features.size()];
        int cont = 0;
        for (Feature element : features) {
            values[cont] = element.getAttribute(this.field);
            ++cont;
        }
        this.list.setListData(values);
    }

    @Override
    public Object getKeyValue() {
        Feature feat = this.getValue();
        if (feat == null) {
            return null;
        }
        return feat.getAttribute(this.keyField);
    }

    @Override
    public Feature getValue() {
        Object selectedItem = this.list.getSelectedValue();
        FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
        List<Feature> features = fc.getByAttribute(new String[]{this.field}, new Object[]{selectedItem}, this.fieldOrdered, this.filter);
        return features.get(0);
    }

    @Override
    public List<Feature> getValues() {
        ArrayList<Feature> values = new ArrayList<Feature>();
        FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
        Object[] selectedValues = this.list.getSelectedValues();
        int i = 0;
        while (i < selectedValues.length) {
            Object selectedItem = selectedValues[i];
            List<Feature> features = fc.getByAttribute(new String[]{this.field}, new Object[]{selectedItem}, this.fieldOrdered, this.filter);
            values.add(features.get(0));
            ++i;
        }
        return values;
    }

    public void setKeyValue(Object keyValue) {
        this.keyValue = keyValue;
        this.refresh();
    }

    @Override
    public Feature getValueByKey(Object key) {
        FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
        List<Feature> features = fc.getByAttribute(new String[]{this.keyField}, new Object[]{key}, this.fieldOrdered, this.filter);
        if (CollectionUtils.isEmpty(features)) {
            return null;
        }
        return features.get(0);
    }

    @Override
    public void clear() {
        this.list.removeAll();
        this.keyValue = null;
        this.list.setListData(new Vector());
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
    }
}

