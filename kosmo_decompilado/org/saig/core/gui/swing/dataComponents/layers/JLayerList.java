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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JList;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.dataComponents.DataListComponent;

public class JLayerList
extends JList
implements DataListComponent<Feature> {
    private static final long serialVersionUID = 1L;
    private Layer layer;
    private String field;
    private String keyField;
    private Object keyValue;
    private Filter filter;

    public JLayerList(Layer layer, String field, String keyField) {
        this.layer = layer;
        this.field = field;
        this.keyField = keyField;
    }

    @Override
    public void selectItemByValue(Object key) {
        if (key != null) {
            this.setSelectedValue(key, true);
        }
    }

    @Override
    public List<Feature> getRowsByValue(Object value) {
        if (value != null) {
            FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
            return fc.getByAttribute(new String[]{this.field}, new Object[]{value}, null, this.filter);
        }
        return null;
    }

    @Override
    public void refresh() {
        this.removeAll();
        FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
        List<Feature> features = null;
        if (this.keyValue == null) {
            this.setListData(new Vector());
            return;
        }
        features = fc.getByAttribute(new String[]{this.keyField}, new Object[]{this.keyValue}, null, this.filter);
        if (CollectionUtils.isEmpty(features)) {
            this.setListData(new Vector());
            return;
        }
        Object[] values = new Object[features.size()];
        int cont = 0;
        for (Feature element : features) {
            values[cont] = element.getAttribute(this.field);
            ++cont;
        }
        this.setListData(values);
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
        Object selectedItem = this.getSelectedValue();
        FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
        List<Feature> features = fc.getByAttribute(new String[]{this.field}, new Object[]{selectedItem}, null, this.filter);
        return features.get(0);
    }

    public void setKeyValue(Object keyValue) {
        this.keyValue = keyValue;
        this.refresh();
    }

    @Override
    public Feature getValueByKey(Object key) {
        FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
        List<Feature> features = fc.getByAttribute(new String[]{this.keyField}, new Object[]{key}, null, this.filter);
        if (CollectionUtils.isEmpty(features)) {
            return null;
        }
        return features.get(0);
    }

    @Override
    public void clear() {
        this.removeAll();
        this.keyValue = null;
        this.setListData(new Vector());
    }

    @Override
    public List<Feature> getValues() {
        ArrayList<Feature> values = new ArrayList<Feature>();
        FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
        Object[] selectedValues = this.getSelectedValues();
        int i = 0;
        while (i < selectedValues.length) {
            Object selectedItem = selectedValues[i];
            List<Feature> features = fc.getByAttribute(new String[]{this.field}, new Object[]{selectedItem}, null, this.filter);
            values.add(features.get(0));
            ++i;
        }
        return values;
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
    }
}

