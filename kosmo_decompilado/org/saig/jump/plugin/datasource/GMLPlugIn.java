/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.datasource.SaveFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.GMLFileDataSource;
import org.saig.jump.widgets.datasource.GMLFileLoadQueryChooser;

public class GMLPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.datasource.GMLPlugIn.GML-files");
    public static final String GML_EXTENSION = "gml";

    @Override
    public void initialize(PlugInContext context) {
        GMLFileLoadQueryChooser loadQueryChooser = new GMLFileLoadQueryChooser(GMLFileDataSource.class, NAME, new String[]{GML_EXTENSION}, context.getWorkbenchContext());
        SaveFileDataSourceQueryChooser saveQueryChooser = new SaveFileDataSourceQueryChooser(GMLFileDataSource.class, NAME, new String[]{GML_EXTENSION}, context.getWorkbenchContext());
        DataSourceQueryChooserManager dataSourceManager = DataSourceQueryChooserManager.get(JUMPWorkbench.getBlackboard());
        dataSourceManager.addLoadDataSourceQueryChooser(loadQueryChooser);
        dataSourceManager.addSaveDataSourceQueryChooser(saveQueryChooser);
    }

    @Override
    public String getName() {
        return NAME;
    }
}

