/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import org.saig.core.dao.coverage.CoverageFactory;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.ImageFileDataSource;
import org.saig.jump.widgets.datasource.ImageFileLoadQueryChooser;

public class ImageFilePlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.datasource.ImageFilePlugIn.name");
    public static final String OPTION_NAME = I18N.getString("org.saig.jump.plugin.datasource.ImageFilePlugIn.image-file");

    @Override
    public void initialize(PlugInContext context) {
        String[] supportedExtensions = CoverageFactory.getInstance().getSupportedExtensions();
        ImageFileLoadQueryChooser loadQueryChooser = new ImageFileLoadQueryChooser(ImageFileDataSource.class, OPTION_NAME, supportedExtensions, context.getWorkbenchContext());
        DataSourceQueryChooserManager dataSourceManager = DataSourceQueryChooserManager.get(JUMPWorkbench.getBlackboard());
        dataSourceManager.addLoadDataSourceQueryChooser(loadQueryChooser);
    }

    @Override
    public String getName() {
        return NAME;
    }
}

