/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.lang.StringUtils
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;

public abstract class AbstractLayerable
implements Layerable {
    protected LayerManager layerManager;
    protected String name;
    protected boolean visible = false;
    protected boolean enabled = true;
    protected String oldCategoryName;
    protected int oldCategoryIndex;
    protected String metadata;
    protected Map<Locale, String> titleByLang = new HashMap<Locale, String>();

    public AbstractLayerable() {
    }

    public AbstractLayerable(String name, LayerManager layerManager) {
        if (StringUtils.isEmpty((String)name)) {
            name = I18N.getString("com.vividsolutions.jump.workbench.model.AbstractLayerable.unnamed");
        }
        Assert.isTrue((layerManager != null ? 1 : 0) != 0);
        this.setLayerManager(layerManager);
        boolean firingEvents = layerManager.isFiringEvents();
        layerManager.setFiringEvents(false);
        try {
            this.setName(layerManager.uniqueLayerName(name));
        }
        finally {
            layerManager.setFiringEvents(firingEvents);
        }
    }

    @Override
    public void setLayerManager(LayerManager layerManager) {
        this.layerManager = layerManager;
    }

    @Override
    public LayerManager getLayerManager() {
        return this.layerManager;
    }

    public void fireLayerChanged(LayerEventType type) {
        if (this.getLayerManager() == null) {
            return;
        }
        this.getLayerManager().fireLayerChanged(this, type);
    }

    @Override
    public void fireAppearanceChanged() {
        this.fireLayerChanged(LayerEventType.APPEARANCE_CHANGED);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String newName) {
        this.setName(newName, true);
    }

    public void setName(String newName, boolean fireEvent) {
        this.name = newName;
        if (fireEvent) {
            this.fireLayerChanged(LayerEventType.METADATA_CHANGED);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        this.setVisible(visible, true);
    }

    public void setVisible(boolean visible, boolean fireEvent) {
        if (this.visible == visible) {
            return;
        }
        this.visible = visible;
        if (fireEvent) {
            this.fireLayerChanged(LayerEventType.VISIBILITY_CHANGED);
        }
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getOldCategoryIndex() {
        return this.oldCategoryIndex;
    }

    public int getCategoryIndex() {
        return this.oldCategoryIndex;
    }

    public void setOldCategoryIndex(int categoryIndex) {
        this.oldCategoryIndex = categoryIndex;
    }

    public void setCategoryIndex(int categoryIndex) {
        this.oldCategoryIndex = categoryIndex;
    }

    public String getOldCategoryName() {
        return this.oldCategoryName;
    }

    public String getCategoryName() {
        return this.oldCategoryName;
    }

    public void setOldCategoryName(String categoryName) {
        this.oldCategoryName = categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.oldCategoryName = categoryName;
    }

    public String toString() {
        return this.getName();
    }

    @Override
    public String getMetadata() {
        return this.metadata;
    }

    @Override
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String getTitle(Locale locale) {
        if (this.titleByLang.containsKey(locale)) {
            return this.titleByLang.get(locale);
        }
        return this.name;
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
        this.titleByLang.put(locale, title);
    }

    @Override
    public Map<Locale, String> getTitleByLang() {
        return this.titleByLang;
    }

    @Override
    public void setTitleByLang(Map<Locale, String> titleByLang) {
        this.titleByLang = titleByLang;
    }
}

