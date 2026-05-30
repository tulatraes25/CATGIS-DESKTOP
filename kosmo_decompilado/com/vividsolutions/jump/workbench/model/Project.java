/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.CategoryEventType;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.Task;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.saig.core.model.data.Table;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.Page;

public class Project {
    private String name = "";
    private String version = "";
    private String author;
    private Date creationDate;
    private Date lastModificationDate;
    private String description;
    private File projectFile;
    private Locale activeLocale;
    private Set<Locale> availablesLocales = new HashSet<Locale>();
    private Map<Object, Object> properties = new HashMap<Object, Object>();
    private List<Task> tasks = new ArrayList<Task>();
    private List<Table> tables = new ArrayList<Table>();
    private List<Page> layouts = new ArrayList<Page>();

    public Project() {
        this.creationDate = new Date(System.currentTimeMillis());
        this.lastModificationDate = new Date(System.currentTimeMillis());
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTasks(List<Task> tasksList) {
        this.tasks = tasksList;
    }

    public List<Task> getTasks() {
        return this.tasks;
    }

    public List<Page> getLayouts() {
        return this.layouts;
    }

    public void setLayouts(List<Page> layouts) {
        this.layouts = layouts;
    }

    public List<Table> getTables() {
        return this.tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public File getProjectFile() {
        return this.projectFile;
    }

    public void setProjectFile(File projectFile) {
        this.projectFile = projectFile;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModificationDate() {
        return this.lastModificationDate;
    }

    public void setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Locale getActiveLocale() {
        return this.activeLocale;
    }

    public void setActiveLocale(Locale activeLocale) {
        this.setActiveLocale(activeLocale, true);
    }

    public void setActiveLocale(Locale activeLocale, boolean fireAppearanceChanged) {
        this.addLocale(activeLocale);
        this.activeLocale = activeLocale;
        if (fireAppearanceChanged) {
            List<Category> projectCategories = JUMPWorkbench.getFrameInstance().getContext().getAllCategories();
            for (Category category : projectCategories) {
                category.fireCategoryChanged(CategoryEventType.METADATA_CHANGED);
            }
            List layerables = JUMPWorkbench.getFrameInstance().getContext().getAllLayers();
            for (Layerable layerable : layerables) {
                layerable.fireAppearanceChanged();
            }
        }
    }

    public Set<Locale> getAvailablesLocales() {
        return this.availablesLocales;
    }

    public List<Locale> getOrderedAvailablesLocales() {
        ArrayList<Locale> availablesLocalesList = new ArrayList<Locale>();
        for (Locale locale : this.availablesLocales) {
            availablesLocalesList.add(locale);
        }
        Collections.sort(availablesLocalesList, new Comparator<Locale>(){

            @Override
            public int compare(Locale o1, Locale o2) {
                return o1.getDisplayName().toLowerCase().compareTo(o2.getDisplayName().toLowerCase());
            }
        });
        return availablesLocalesList;
    }

    public void setAvailablesLocales(Set<Locale> availablesLocales) {
        this.availablesLocales = availablesLocales;
    }

    public void refreshLocales(Set<Locale> selectedLocales) {
        ArrayList<Locale> deletedLocales = new ArrayList<Locale>();
        for (Locale locale : this.availablesLocales) {
            if (selectedLocales.contains(locale)) continue;
            deletedLocales.add(locale);
        }
        for (Locale locale : deletedLocales) {
            this.removeLocale(locale);
        }
        for (Locale locale : selectedLocales) {
            this.addLocale(locale);
        }
        if (this.activeLocale == null || !this.availablesLocales.contains(this.activeLocale)) {
            if (this.availablesLocales.isEmpty()) {
                this.setActiveLocale(I18N.getLocale());
            } else {
                this.setActiveLocale(this.availablesLocales.iterator().next());
            }
        }
    }

    public boolean isAvalaibleLocale(Locale locale) {
        return this.availablesLocales.contains(locale);
    }

    public boolean removeLocale(Locale locale) {
        if (this.availablesLocales.contains(locale)) {
            this.availablesLocales.remove(locale);
            List layerables = JUMPWorkbench.getFrameInstance().getContext().getAllLayers();
            for (Layerable layer : layerables) {
                layer.removeLocale(locale);
            }
            return true;
        }
        return false;
    }

    public boolean addLocale(Locale locale) {
        if (!this.availablesLocales.contains(locale)) {
            this.availablesLocales.add(locale);
            List layerables = JUMPWorkbench.getFrameInstance().getContext().getAllLayers();
            for (Layerable layer : layerables) {
                layer.addLocale(locale);
            }
            return true;
        }
        return false;
    }

    public void setProperty(Object key, Object value) {
        this.properties.put(key, value);
    }

    public Object getProperty(Object key) {
        return this.properties.get(key);
    }

    public Map<Object, Object> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<Object, Object> properties) {
        this.properties = properties;
    }
}

