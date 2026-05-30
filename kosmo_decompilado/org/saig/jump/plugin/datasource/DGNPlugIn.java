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
import org.saig.jump.plugin.datasource.DGNFileDataSource;

public class DGNPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.datasource.DGNPlugIn.DGN-files");
    public static final String DGN_EXTENSION = "dgn";

    @Override
    public void initialize(PlugInContext context) {
        LoadFileDataSourceQueryChooser loadQueryChooser = new LoadFileDataSourceQueryChooser(DGNFileDataSource.class, NAME, new String[]{DGN_EXTENSION}, context.getWorkbenchContext());
        DataSourceQueryChooserManager dataSourceManager = DataSourceQueryChooserManager.get(JUMPWorkbench.getBlackboard());
        dataSourceManager.addLoadDataSourceQueryChooser(loadQueryChooser);
    }

    @Override
    public String getName() {
        return NAME;
    }
}

