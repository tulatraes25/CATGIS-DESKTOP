/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.dataComponents.layers;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.model.Layer;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.JComboBox;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.dataComponents.DataListWithPatternComponent;
import org.saig.jump.lang.I18N;

public class JLayerPatternComboBox
extends JComboBox
implements DataListWithPatternComponent<Feature>,
ItemListener {
    private static final long serialVersionUID = 1L;
    private Layer layer;
    private String[] fields;
    private String keyField;
    private Object keyValue;
    private Collator collator = Collator.getInstance(I18N.getLocale());
    private String fieldOrdered;
    private Filter filter;
    private String pattern;
    private Map<String, Object> valuesToKey;

    public JLayerPatternComboBox(Layer layer, String keyField, String[] fieldsToShow, String fieldOrdered, Filter filter, String pattern) {
        this.layer = layer;
        this.keyField = keyField;
        this.fields = fieldsToShow;
        this.fieldOrdered = fieldOrdered;
        this.filter = filter;
        this.pattern = pattern != null ? pattern : this.getDefaultPattern(fieldsToShow.length);
        this.valuesToKey = new HashMap<String, Object>();
        this.refresh();
    }

    @Override
    public void selectItemByValue(Object key) {
        if (key != null) {
            if (this.fields.length == 1 && this.keyField.equals(this.fields[0])) {
                this.setSelectedItem(key);
            } else {
                FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
                List<Feature> features = fc.getByAttribute(new String[]{this.keyField}, new Object[]{key}, this.fieldOrdered, this.filter);
                if (features.size() == 0) {
                    this.setSelectedItem("----------");
                    return;
                }
                Feature feat = features.get(0);
                String value = this.applyPattern(this.getValuesOfFieldsToShow(feat));
                if (feat != null && value != null) {
                    this.setSelectedItem(value);
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
        return this.getRowsByValues(new Object[]{value});
    }

    @Override
    public List<Feature> getRowsByValues(Object[] values) {
        if (values != null) {
            ArrayList<Object> keys = new ArrayList<Object>();
            int i = 0;
            while (i < values.length) {
                keys.add(this.valuesToKey.get(values[i]));
                ++i;
            }
            FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
            return fc.getByPrimaryKeys(keys.toArray());
        }
        return null;
    }

    @Override
    public void refresh() {
        this.removeAllItems();
        this.valuesToKey.clear();
        FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
        List<Feature> features = null;
        features = this.keyValue == null ? fc.getByAttribute(new String[0], null, this.fieldOrdered, this.filter) : fc.getByAttribute(new String[]{this.keyField}, new Object[]{this.keyValue}, this.fieldOrdered, this.filter);
        Vector<String> featuresValues = new Vector<String>();
        if (features != null && features.size() > 0) {
            for (Feature element : features) {
                String valueToShow = this.applyPattern(this.getValuesOfFieldsToShow(element));
                this.valuesToKey.put(valueToShow, element.getPrimaryKey());
                featuresValues.add(valueToShow);
            }
            Collections.sort(featuresValues, this.collator);
        }
        this.addItem("----------");
        for (String item : featuresValues) {
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
        Object key = this.valuesToKey.get(selectedItem);
        return fc.getByPrimaryKey(key);
    }

    public void setKeyValue(Object keyValue) {
        this.keyValue = keyValue;
        this.refresh();
    }

    @Override
    public Feature getValueByKey(Object key) {
        FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
        List<Feature> features = fc.getByAttribute(new String[]{this.keyField}, new Object[]{key}, this.fieldOrdered, this.filter);
        if (features == null || features.isEmpty()) {
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

    @Override
    public String applyPattern(Object[] values) {
        MessageFormat mf = new MessageFormat(this.pattern);
        return mf.format(values);
    }

    private String getDefaultPattern(int length) {
        String defaultPattern = "{0}";
        int i = 1;
        while (i < length) {
            defaultPattern = String.valueOf(defaultPattern) + " - {" + i + "}";
            ++i;
        }
        return defaultPattern;
    }

    private Object[] getValuesOfFieldsToShow(Feature feat) {
        Object[] values = new Object[this.fields.length];
        int i = 0;
        while (i < this.fields.length) {
            values[i] = feat.getAttribute(this.fields[i]);
            ++i;
        }
        return values;
    }
}

