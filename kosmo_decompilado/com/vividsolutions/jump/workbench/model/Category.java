/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.lang.StringUtils
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.model.CategoryEventType;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.saig.core.util.LocaleManager;
import org.saig.core.util.language.ITranslatable;
import org.saig.jump.util.LayerUtil;

public class Category
implements ITranslatable {
    protected List<Layerable> layerables;
    protected String name;
    protected LayerManager layerManager;
    protected boolean collapsed;
    private Map<Object, Object> properties = new HashMap<Object, Object>();
    private Map<Locale, String> titleByLang = new HashMap<Locale, String>();

    public Category() {
        this.layerables = new ArrayList<Layerable>();
    }

    public void setName(String newName) {
        this.name = newName;
        this.setTitle(newName, LocaleManager.getActiveLocale());
        this.fireCategoryChanged(CategoryEventType.METADATA_CHANGED);
    }

    public void setLayerManager(LayerManager layerManager) {
        this.layerManager = layerManager;
    }

    public void fireCategoryChanged(CategoryEventType type) {
        if (this.getLayerManager() == null) {
            return;
        }
        this.getLayerManager().fireCategoryChanged(this, type);
    }

    public LayerManager getLayerManager() {
        return this.layerManager;
    }

    public List<Layerable> getPersistentLayerables() {
        ArrayList<Layerable> persistentLayerables = new ArrayList<Layerable>();
        for (Layerable layerable : this.layerables) {
            Layer layer;
            if (layerable instanceof Layer && !LayerUtil.isSystemLayer(layer = (Layer)layerable) && (!layer.hasReadableDataSource() || layer.isInternal() || LayerUtil.isAppInternalSystemLayer(layer))) continue;
            persistentLayerables.add(layerable);
        }
        return persistentLayerables;
    }

    public List<Layerable> getEnabledLayerables() {
        ArrayList<Layerable> enableLayerables = new ArrayList<Layerable>();
        for (Layerable layerable : this.layerables) {
            if (!layerable.isEnabled()) continue;
            enableLayerables.add(layerable);
        }
        return enableLayerables;
    }

    public List<Layerable> getLayerables() {
        return Collections.unmodifiableList(this.layerables);
    }

    public void remove(Layerable layerable) {
        Assert.isTrue((boolean)this.contains(layerable));
        this.layerables.remove(layerable);
    }

    public int indexOf(Layerable layerable) {
        return this.layerables.indexOf(layerable);
    }

    public boolean contains(Layerable layerable) {
        return this.layerables.contains(layerable);
    }

    public void add(int index, Layerable layerable) {
        if (this.layerables.size() - 1 < index) {
            this.layerables.add(layerable);
        } else {
            this.layerables.add(index, layerable);
        }
        if (this.getLayerManager() != null) {
            this.getLayerManager().fireLayerChanged(layerable, LayerEventType.ADDED);
        }
    }

    public void addPersistentLayerable(Layerable layerable) {
        this.add(this.layerables.size(), layerable);
    }

    public boolean isEmpty() {
        return this.layerables.isEmpty();
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.getName();
    }

    public boolean isCollapsed() {
        return this.collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    @Override
    public String getTitle(Locale locale) {
        if (this.titleByLang == null) {
            this.titleByLang = new HashMap<Locale, String>();
        }
        if (!this.titleByLang.containsKey(locale) || StringUtils.isEmpty((String)this.titleByLang.get(locale))) {
            this.addLocale(locale);
        }
        return this.titleByLang.get(locale);
    }

    @Override
    public String getTitle() {
        Locale locale = LocaleManager.getActiveLocale();
        if (!this.titleByLang.containsKey(locale) || StringUtils.isEmpty((String)this.titleByLang.get(locale))) {
            this.addLocale(locale);
        }
        return this.titleByLang.get(locale);
    }

    @Override
    public void setTitle(String title, Locale locale) {
        if (this.titleByLang == null) {
            this.titleByLang = new HashMap<Locale, String>();
        }
        this.titleByLang.put(locale, title);
    }

    @Override
    public Map<Locale, String> getTitleByLang() {
        for (Locale locale : LocaleManager.getAvailablesLocales()) {
            if (this.titleByLang.containsKey(locale) && !StringUtils.isEmpty((String)this.titleByLang.get(locale))) continue;
            this.titleByLang.put(locale, this.name);
        }
        return this.titleByLang;
    }

    @Override
    public void setTitleByLang(Map<Locale, String> titleByLang) {
        this.titleByLang = titleByLang;
    }

    @Override
    public void addLocale(Locale locale) {
        if (!this.titleByLang.containsKey(locale) || StringUtils.isEmpty((String)this.titleByLang.get(locale))) {
            this.titleByLang.put(locale, this.name);
        }
    }

    @Override
    public void removeLocale(Locale locale) {
        if (this.titleByLang.containsKey(locale)) {
            this.titleByLang.remove(locale);
        }
    }

    public Map<Object, Object> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<Object, Object> properties) {
        this.properties = properties;
    }

    public void setProperty(Object key, Object value) {
        this.properties.put(key, value);
    }

    public void removeProperty(Object key) {
        this.properties.remove(key);
    }

    public Object getProperty(Object key) {
        return this.properties.get(key);
    }
}

