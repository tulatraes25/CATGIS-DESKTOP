/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import es.kosmo.desktop.widgets.datasource.ShapeFileSaveQueryChooser;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.IndexedShapeFileDataSource;
import org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser;

public class IndexedShapeFilePlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.datasource.ScalableIndexedShapeFilePlugIn.indexed-shapefile");
    public static final String SHP_EXTENSION = "shp";

    @Override
    public void initialize(PlugInContext context) {
        ShapeFileLoadQueryChooser loadQueryChooser = new ShapeFileLoadQueryChooser(IndexedShapeFileDataSource.class, NAME, new String[]{SHP_EXTENSION}, context.getWorkbenchContext());
        ShapeFileSaveQueryChooser saveQueryChooser = new ShapeFileSaveQueryChooser(IndexedShapeFileDataSource.class, NAME, new String[]{SHP_EXTENSION}, context.getWorkbenchContext());
        DataSourceQueryChooserManager dataSourceManager = DataSourceQueryChooserManager.get(JUMPWorkbench.getBlackboard());
        dataSourceManager.addLoadDataSourceQueryChooser(loadQueryChooser);
        dataSourceManager.addSaveDataSourceQueryChooser(saveQueryChooser);
    }

    @Override
    public String getName() {
        return NAME;
    }
}

