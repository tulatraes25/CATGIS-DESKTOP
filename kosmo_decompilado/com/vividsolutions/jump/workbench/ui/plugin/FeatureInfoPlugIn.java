/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.AttributeTab;
import com.vividsolutions.jump.workbench.ui.InfoFrame;
import com.vividsolutions.jump.workbench.ui.SelectionManagerProxy;
import com.vividsolutions.jump.workbench.ui.TaskFrameProxy;
import com.vividsolutions.jump.workbench.ui.cursortool.FeatureInfoTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.config.ConfigPlugIn;
import org.saig.jump.widgets.config.ConfigDialog;
import org.saig.jump.widgets.config.ConfigInfoToolPanel;
import org.saig.jump.widgets.info.FeatureInfoDialog;

public class FeatureInfoPlugIn
extends AbstractPlugIn {
    public static final String NAME = String.valueOf(I18N.getString("workbench.ui.plugin.FeatureInfoPlugIn.name")) + "...";
    public static final Icon ICON = IconLoader.icon("Info.gif");

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
        return FeatureInfoPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithSelectionManagerMustBeActiveCheck()).add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(checkFactory.createAtLeastNItemsMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersWithPrimaryKeyCheck()).add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                boolean allVisibles = PersistentBlackboardPlugIn.get(workbenchContext).get(FeatureInfoTool.VISIBLE_LAYERS_KEY, true);
                EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
                if (!allVisibles) {
                    return new MultiEnableCheck().add(checkFactory.createAtLeastNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustNotBeRasterCheck()).add(checkFactory.createSelectedLayerMustBeActiveCheck()).check(component);
                }
                return new MultiEnableCheck().add(checkFactory.createAtLeastNVisibleLayersMustNotBeRasterCheck(1)).add(checkFactory.createSelectedLayerMustBeActiveCheck()).check(component);
            }
        });
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        InfoFrame infoFrame = new InfoFrame(context.getWorkbenchContext(), (LayerManagerProxy)((Object)context.getActiveInternalFrame()), ((TaskFrameProxy)((Object)context.getActiveInternalFrame())).getTaskFrame());
        boolean allVisibles = PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).get(FeatureInfoTool.VISIBLE_LAYERS_KEY, true);
        Object[] layers = null;
        layers = !allVisibles ? context.getWorkbenchContext().getLayerNamePanel().getSelectedLayers() : context.getWorkbenchContext().getLayerManager().getVisibleLayers(true).toArray();
        boolean hasSelectedFeatures = false;
        boolean isTable = PersistentBlackboardPlugIn.get(context.getWorkbenchContext().getBlackboard()).get(FeatureInfoTool.CONFIG_INFO_KEY, true);
        if (!isTable && layers != null && layers.length > 0) {
            HashMap<Layer, Collection<Feature>> map = new HashMap<Layer, Collection<Feature>>();
            int i = 0;
            while (i < layers.length) {
                Collection<Feature> features = ((SelectionManagerProxy)((Object)context.getActiveInternalFrame())).getSelectionManager().getFeaturesWithSelectedItems((Layer)layers[i]);
                map.put((Layer)layers[i], features);
                ++i;
            }
            new FeatureInfoDialog(JUMPWorkbench.getFrameInstance(), false, map, layers, context.getWorkbenchContext().getLayerViewPanel());
        } else {
            int i = 0;
            while (i < layers.length) {
                Layer layer = (Layer)layers[i];
                if (!layer.isRaster() && !((SelectionManagerProxy)((Object)context.getActiveInternalFrame())).getSelectionManager().getFeaturesWithSelectedItems(layer).isEmpty()) {
                    hasSelectedFeatures = true;
                    infoFrame.getModel().add(layer, ((SelectionManagerProxy)((Object)context.getActiveInternalFrame())).getSelectionManager().getFeaturesWithSelectedItems(layer));
                }
                ++i;
            }
            if (hasSelectedFeatures) {
                Dimension dim = ((AttributeTab)infoFrame.getAttributeTab()).getTableSize();
                Rectangle parentBounds = context.getWorkbenchFrame().getDesktopPane().getBounds();
                infoFrame.setSize(Math.min(parentBounds.width, dim.width + 57), 266);
                infoFrame.setLocation(0, Math.max(0, parentBounds.height - 266));
                context.getWorkbenchFrame().addInternalFrame(infoFrame);
            } else {
                context.getWorkbenchFrame().warnUser(I18N.getString("workbench.ui.plugin.FeatureInfoPlugIn.there-are-no-selected-items-in-the-selected-layers"));
            }
        }
        return true;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        ConfigPlugIn.getDialog().addConfigPanel(new ConfigInfoToolPanel(context.getWorkbenchContext().getBlackboard()), ConfigDialog.TOOLS_MAIN_CATEGORY_NAME, FeatureInfoTool.NAME);
    }
}

