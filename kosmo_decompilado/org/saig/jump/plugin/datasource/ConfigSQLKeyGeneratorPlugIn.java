/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.core.dao.datasource.dbdatasource.keys_resolver.DBBySQLKeyResolver;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.datasource.ConfigLayerSQLKeyResolverDialog;

public class ConfigSQLKeyGeneratorPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.datasource.ConfigSQLKeyGeneratorPlugIn.Configure-key-generation");
    public static final Icon ICON = IconLoader.icon("db_key.png");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Layer layer = (Layer)context.getLayerNamePanel().getSelectedLayers()[0];
        ConfigLayerSQLKeyResolverDialog dialog = new ConfigLayerSQLKeyResolverDialog(JUMPWorkbench.getFrameInstance(), true, layer.getTransactionalDataSource());
        dialog.setVisible(true);
        if (dialog.isOkPressed()) {
            if (dialog.hasKeyGenerator()) {
                DBBySQLKeyResolver dbBySQLKeyResolver = new DBBySQLKeyResolver(dialog.getSQL());
                layer.getTransactionalDataSource().setKeyResolver(dbBySQLKeyResolver);
            } else {
                layer.getTransactionalDataSource().setKeyResolver(null);
            }
            this.warnOperationSuccessful(context);
        }
        return true;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EnableCheck getCheck() {
        return ConfigSQLKeyGeneratorPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustExistCheck(1)).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createLayerMustBeDataBaseCheck());
    }
}

