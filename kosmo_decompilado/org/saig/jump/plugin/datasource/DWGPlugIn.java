/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.datasource.LoadFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.DWGFileDataSource;

public class DWGPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.datasource.DWGPlugIn.dwg-files");
    public static final String DWG_EXTENSION = "dwg";

    @Override
    public void initialize(PlugInContext context) {
        LoadFileDataSourceQueryChooser loadQueryChooser = new LoadFileDataSourceQueryChooser(DWGFileDataSource.class, NAME, new String[]{DWG_EXTENSION}, context.getWorkbenchContext());
        DataSourceQueryChooserManager dataSourceManager = DataSourceQueryChooserManager.get(JUMPWorkbench.getBlackboard());
        dataSourceManager.addLoadDataSourceQueryChooser(loadQueryChooser);
    }

    @Override
    public String getName() {
        return NAME;
    }
}

