/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.warp.Triangulator;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.PasteItemsPlugIn;
import com.vividsolutions.jump.workbench.ui.warp.WarpingVectorLayerFinder;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;

public class CopySelectedLayersToWarpingVectorsPlugIn
extends AbstractPlugIn {
    public EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeSelectedCheck(1)).add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return workbenchContext.getLayerNamePanel().getSelectedLayers().length == 1 && workbenchContext.getLayerNamePanel().getSelectedLayers()[0] == new WarpingVectorLayerFinder(workbenchContext).getLayer() ? I18N.getMessage("workbench.ui.plugin.CopySelectedLayersToWarpingVectorsPlugin.a-layer-other-than-{0}-must-be-selected", new Object[]{new WarpingVectorLayerFinder(workbenchContext).getLayerName()}) : null;
            }
        });
    }

    public static Collection<Feature> removeNonVectorFeaturesAndWarn(Collection<Feature> features, LayerViewPanelContext context) {
        ArrayList<Feature> newFeatures = new ArrayList<Feature>(features);
        Collection<Feature> nonVectorFeatures = CopySelectedLayersToWarpingVectorsPlugIn.nonVectorFeatures(newFeatures);
        if (!nonVectorFeatures.isEmpty()) {
            String message = "";
            message = nonVectorFeatures.size() > 1 ? I18N.getMessage("workbench.ui.plugin.CopySelectedLayersToWarpingVectorsPlugin.skipped-{0}-non-two-point-linestring-e.g.-{1}", new Object[]{new Integer(nonVectorFeatures.size()), nonVectorFeatures.iterator().next().getGeometry().toText()}) : I18N.getMessage("workbench.ui.plugin.CopySelectedLayersToWarpingVectorsPlugin.skipped-one-non-two-point-linestrings-e.g.-{0}", new Object[]{nonVectorFeatures.iterator().next().getGeometry().toText()});
            context.warnUser(message);
            newFeatures.removeAll(nonVectorFeatures);
        }
        return newFeatures;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Collection<Feature> newWarpingVectors = new ArrayList<Feature>();
        Layer[] selectedLayers = (Layer[])context.getSelectedLayers();
        int i = 0;
        while (i < selectedLayers.length) {
            if (selectedLayers[i] != new WarpingVectorLayerFinder(context).getLayer()) {
                newWarpingVectors.addAll(selectedLayers[i].getFeatureCollectionWrapper().getFeatures());
            }
            ++i;
        }
        final Collection<Feature> finalNewWarpingVectors = newWarpingVectors = CopySelectedLayersToWarpingVectorsPlugIn.removeNonVectorFeaturesAndWarn(newWarpingVectors, context.getWorkbenchFrame());
        final WarpingVectorLayerFinder finder = new WarpingVectorLayerFinder(context);
        this.execute(Layer.addUndo(finder.getLayerName(), context, new UndoableCommand(this.getName()){

            @Override
            public void execute() throws Exception {
                if (finder.getLayer() == null) {
                    finder.createLayer();
                }
                finder.getLayer().getFeatureCollectionWrapper().addAll(PasteItemsPlugIn.conform(finalNewWarpingVectors, finder.getLayer().getFeatureCollectionWrapper().getFeatureSchema()));
            }

            @Override
            public void unexecute() throws Exception {
            }
        }), context);
        return true;
    }

    private static Collection<Feature> nonVectorFeatures(Collection<Feature> candidates) {
        ArrayList<Feature> nonVectorFeatures = new ArrayList<Feature>();
        for (Feature candidate : candidates) {
            if (Triangulator.vector(candidate.getGeometry())) continue;
            nonVectorFeatures.add(candidate);
        }
        return nonVectorFeatures;
    }
}

