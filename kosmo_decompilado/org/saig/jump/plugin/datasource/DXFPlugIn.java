/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import es.kosmo.desktop.widgets.datasource.DXFFileSaveQueryChooser;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.DXFFileDataSource;
import org.saig.jump.widgets.datasource.DXFFileLoadQueryChooser;

public class DXFPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.datasource.DXFPlugIn.dxf-file");
    public static final String DXF_EXTENSION = "dxf";

    @Override
    public void initialize(PlugInContext context) {
        DXFFileLoadQueryChooser dxfLoadQueryChooser = new DXFFileLoadQueryChooser(DXFFileDataSource.class, NAME, new String[]{DXF_EXTENSION}, context.getWorkbenchContext());
        DXFFileSaveQueryChooser dxfSaveQueryChooser = new DXFFileSaveQueryChooser(DXFFileDataSource.class, NAME, new String[]{DXF_EXTENSION}, context.getWorkbenchContext());
        DataSourceQueryChooserManager dataSourceManager = DataSourceQueryChooserManager.get(JUMPWorkbench.getBlackboard());
        dataSourceManager.addLoadDataSourceQueryChooser(dxfLoadQueryChooser);
        dataSourceManager.addSaveDataSourceQueryChooser(dxfSaveQueryChooser);
    }

    @Override
    public String getName() {
        return NAME;
    }
}

