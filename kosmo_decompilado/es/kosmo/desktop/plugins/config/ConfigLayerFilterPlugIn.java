/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.plugins.config;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import es.kosmo.desktop.images.DesktopIconLoader;
import es.kosmo.desktop.widgets.config.ConfigLayerFilterDialog;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.saig.core.dao.datasource.dbdatasource.ExtendPostGisDataSource;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.jump.lang.I18N;

public class ConfigLayerFilterPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("es.kosmo.desktop.plugins.config.ConfigLayerFilterPlugIn.Configure-filter");
    public static final Icon ICON = GUIUtil.toSmallIcon(DesktopIconLoader.icon("layer_filter.png"));

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return ConfigLayerFilterPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layer selectedLayer = (Layer)context.getSelectedLayer(0);
        ConfigLayerFilterDialog configLayerFilterDialog = new ConfigLayerFilterDialog(JUMPWorkbench.getFrameInstance(), true, selectedLayer);
        configLayerFilterDialog.setVisible(true);
        if (configLayerFilterDialog.isOk()) {
            selectedLayer.setLayerFilter(configLayerFilterDialog.getFilter());
            selectedLayer.fireAppearanceChanged();
        }
        return true;
    }

    public static EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory cf = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(cf.createWindowWithLayerNamePanelMustBeActiveCheck());
        check.add(cf.createExactlyNLayersMustBeSelectedCheck(1));
        check.add(cf.createSelectedLayersMustNotBeWMSLayersCheck());
        check.add(cf.createSelectedLayerMustBeActiveCheck());
        check.add(cf.createLayerMustBeDataBaseCheck());
        check.add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layer layer = (Layer)workbenchContext.getLayerNamePanel().getSelectedLayers()[0];
                if (!layer.isDataBaseDataSource()) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-must-be-a-database-layer", new Object[]{layer.getTitle()});
                }
                if (((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor() instanceof ExtendPostGisDataSource) {
                    return I18N.getString("es.kosmo.desktop.plugins.config.ConfigLayerFilterPlugIn.The-filter-can-not-be-applied-to-a-PostgreSQL-optimized-layer");
                }
                return null;
            }
        });
        return check;
    }
}

