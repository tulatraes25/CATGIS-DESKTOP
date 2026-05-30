/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.core.gui.swing.dataComponents.layers;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.model.Layer;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;
import javax.swing.JComboBox;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.dataComponents.DataListComponent;
import org.saig.jump.lang.I18N;

public class JLayerComboBox
extends JComboBox
implements DataListComponent<Feature>,
ItemListener {
    private static final long serialVersionUID = 1L;
    protected Layer layer;
    protected String field;
    protected String keyField;
    protected Object keyValue;
    protected Collator collator = Collator.getInstance(I18N.getLocale());
    protected String fieldOrdered;
    protected Filter filter;

    public JLayerComboBox(Layer layer, String keyField, String fieldToShow) {
        this(layer, keyField, fieldToShow, fieldToShow, null);
    }

    public JLayerComboBox(Layer layer, String keyField, String fieldToShow, String fieldOrdered) {
        this(layer, keyField, fieldToShow, fieldOrdered, null);
    }

    public JLayerComboBox(Layer layer, String keyField, String fieldToShow, String fieldOrdered, Filter filter) {
        this.layer = layer;
        this.keyField = keyField;
        this.field = fieldToShow;
        this.fieldOrdered = fieldOrdered;
        this.filter = filter;
        this.refresh();
    }

    @Override
    public void selectItemByValue(Object key) {
        if (key != null) {
            if (this.keyField.equals(this.field)) {
                this.setSelectedItem(key);
            } else {
                FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
                List<Feature> features = fc.getByAttribute(new String[]{this.keyField}, new Object[]{key}, this.fieldOrdered, this.filter);
                if (features.size() == 0) {
                    this.setSelectedItem("----------");
                    return;
                }
                Feature feat = features.get(0);
                if (feat != null && feat.getAttribute(this.field) != null) {
                    this.setSelectedItem(feat.getAttribute(this.field));
                } else {
                    this.setSelectedItem("----------");
                }
            }
        } else {
            this.setSelectedItem("----------");
        }
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
        this.removeAllItems();
        FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
        List<Feature> features = null;
        features = this.keyValue == null ? fc.getByAttribute(new String[0], null, this.fieldOrdered, this.filter) : fc.getByAttribute(new String[]{this.keyField}, new Object[]{this.keyValue}, this.fieldOrdered, this.filter);
        AbstractSet featuresValues = new LinkedHashSet();
        if (CollectionUtils.isNotEmpty(features)) {
            AttributeType tipo = features.get(0).getSchema().getAttributeType(this.field);
            if (tipo == AttributeType.STRING || tipo == AttributeType.VARCHAR || tipo == AttributeType.LONGVARCHAR || tipo == AttributeType.TEXT) {
                featuresValues = new TreeSet<Object>(this.collator);
            }
            for (Feature element : features) {
                Object elementValue = element.getAttribute(this.field);
                if (elementValue == null) continue;
                featuresValues.add(elementValue);
            }
        }
        this.addItem("----------");
        for (Object item : featuresValues) {
            this.addItem(item);
        }
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
        Object selectedItem = this.getSelectedItem();
        if (selectedItem == null || selectedItem.equals("----------")) {
            return null;
        }
        FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
        List<Feature> features = fc.getByAttribute(new String[]{this.field}, new Object[]{selectedItem}, this.fieldOrdered, this.filter);
        return features.get(0);
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
        this.setSelectedItem("----------");
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
        this.refresh();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (this.getInputVerifier() != null) {
            this.getInputVerifier().verify(this);
        }
    }

    @Override
    public List<Feature> getValues() {
        ArrayList<Feature> values = new ArrayList<Feature>();
        values.add(this.getValue());
        return values;
    }
}

