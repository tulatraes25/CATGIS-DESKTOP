/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.datasource;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class DataSourceQueryChooserManager {
    private static final Logger LOGGER = Logger.getLogger(DataSourceQueryChooserManager.class);
    private List<DataSourceQueryChooser> loadDataSourceQueryChoosers = new ArrayList<DataSourceQueryChooser>();
    private List<DataSourceQueryChooser> saveDataSourceQueryChoosers = new ArrayList<DataSourceQueryChooser>();

    public List<DataSourceQueryChooser> getLoadDataSourceQueryChoosers() {
        return Collections.unmodifiableList(this.loadDataSourceQueryChoosers);
    }

    public DataSourceQueryChooserManager addLoadDataSourceQueryChooser(DataSourceQueryChooser chooser) {
        LOGGER.info((Object)I18N.getMessage(this.getClass(), "registering-panel-to-load-{0}", new Object[]{chooser.toString()}));
        this.loadDataSourceQueryChoosers.add(chooser);
        return this;
    }

    public List<DataSourceQueryChooser> getSaveDataSourceQueryChoosers() {
        return Collections.unmodifiableList(this.saveDataSourceQueryChoosers);
    }

    public DataSourceQueryChooserManager addSaveDataSourceQueryChooser(DataSourceQueryChooser chooser) {
        LOGGER.info((Object)I18N.getMessage(this.getClass(), "registering-panel-to-save-{0}", new Object[]{chooser.toString()}));
        this.saveDataSourceQueryChoosers.add(chooser);
        return this;
    }

    public static DataSourceQueryChooserManager get(Blackboard blackboard) {
        return (DataSourceQueryChooserManager)blackboard.get(String.valueOf(DataSourceQueryChooserManager.class.getName()) + " - INSTANCE", new DataSourceQueryChooserManager());
    }

    public DataSourceQueryChooserManager removeLoadDataSourceQueryChooser(DataSourceQueryChooser chooser) {
        LOGGER.info((Object)I18N.getMessage(this.getClass(), "unregistering-panel-to-load-{0}", new Object[]{chooser.toString()}));
        this.loadDataSourceQueryChoosers.remove(chooser);
        return this;
    }

    public DataSourceQueryChooserManager removeSaveDataSourceQueryChooser(DataSourceQueryChooser chooser) {
        LOGGER.info((Object)I18N.getMessage(this.getClass(), "unregistering-panel-to-save-{0}", new Object[]{chooser.toString()}));
        this.saveDataSourceQueryChoosers.remove(chooser);
        return this;
    }
}

