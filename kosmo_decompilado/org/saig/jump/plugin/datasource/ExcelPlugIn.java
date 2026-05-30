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
import org.saig.jump.plugin.datasource.ExcelFileDataSource;

public class ExcelPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.datasource.ExcelPlugIn.excel-files");
    public static final String XLS_EXTENSION = "xls";

    @Override
    public void initialize(PlugInContext context) {
        SaveFileDataSourceQueryChooser saveQueryChooser = new SaveFileDataSourceQueryChooser(ExcelFileDataSource.class, NAME, new String[]{XLS_EXTENSION}, context.getWorkbenchContext());
        DataSourceQueryChooserManager dataSourceManager = DataSourceQueryChooserManager.get(JUMPWorkbench.getBlackboard());
        dataSourceManager.addSaveDataSourceQueryChooser(saveQueryChooser);
    }

    @Override
    public String getName() {
        return NAME;
    }
}

