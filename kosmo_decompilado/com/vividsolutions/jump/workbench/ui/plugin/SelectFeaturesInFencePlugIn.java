/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.FenceLayerFinder;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class SelectFeaturesInFencePlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.SelectFeaturesInFencePlugIn.name");
    public static final Icon ICON = IconLoader.icon("blank.png");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        context.getLayerViewPanel().getSelectionManager().clear();
        SelectFeaturesInFencePlugIn.execute(context.getLayerViewPanel(), context.getLayerNamePanel(), context.getLayerViewPanel().getFence(), true, false);
        return true;
    }

    public static void execute(LayerViewPanel layerViewPanel, LayerNamePanel layerNamePanel, Geometry fence, boolean skipUnselectedLayers, boolean mentionModifierHelp) {
        List<Layerable> selectedLayers = Arrays.asList(layerNamePanel.getSelectedLayers());
        Map<Layer, Collection<Feature>> layerToFeaturesInFenceMap = layerViewPanel.visibleLayerToFeaturesInFenceMap(fence);
        for (Layer layer : layerToFeaturesInFenceMap.keySet()) {
            if (layer == new FenceLayerFinder(layerViewPanel).getLayer() || skipUnselectedLayers && !selectedLayers.contains(layer)) continue;
            layerViewPanel.getSelectionManager().getFeatureSelection().selectItems(layer, layerToFeaturesInFenceMap.get(layer));
        }
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createFenceMustBeDrawnCheck()).add(checkFactory.createAtLeastNLayersMustBeSelectedCheck(1));
    }

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
        return SelectFeaturesInFencePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

