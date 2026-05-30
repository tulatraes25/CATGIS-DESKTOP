/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.gui.swing.dataComponents.layers;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.model.Layer;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.JList;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.dataComponents.DataListWithPatternComponent;
import org.saig.jump.lang.I18N;

public class JLayerPatternList
extends JList
implements DataListWithPatternComponent<Feature> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(JLayerPatternList.class);
    private Layer layer;
    private String[] fields;
    private String keyField;
    private Object keyValue;
    private Filter filter;
    private Collator collator = Collator.getInstance(I18N.getLocale());
    private String pattern;
    private Map<String, Object> valuesToKey;

    public JLayerPatternList(Layer layer, String[] fields, String keyField, String pattern) {
        this.layer = layer;
        this.fields = fields;
        this.keyField = keyField;
        this.pattern = pattern != null ? pattern : this.getDefaultPattern(fields.length);
        this.valuesToKey = new HashMap<String, Object>();
    }

    @Override
    public void selectItemByValue(Object key) {
        if (key != null) {
            this.setSelectedValue(key, true);
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
        this.removeAll();
        this.valuesToKey.clear();
        FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
        List<Feature> features = null;
        if (this.keyValue == null) {
            try {
                features = fc.query(this.filter);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        } else {
            features = fc.getByAttribute(new String[]{this.keyField}, new Object[]{this.keyValue}, null, this.filter);
        }
        if (CollectionUtils.isEmpty(features)) {
            this.setListData(new Vector());
            return;
        }
        Vector<String> featuresValues = new Vector<String>();
        for (Feature element : features) {
            String valueToShow = this.applyPattern(this.getValuesOfFieldsToShow(element));
            this.valuesToKey.put(valueToShow, element.getPrimaryKey());
            featuresValues.add(valueToShow);
        }
        Collections.sort(featuresValues, this.collator);
        this.setListData(featuresValues);
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
            Object key = this.valuesToKey.get(selectedItem);
            values.add(fc.getByPrimaryKey(key));
            ++i;
        }
        return values;
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
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

