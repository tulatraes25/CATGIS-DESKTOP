/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.plugins.utils;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.datasource.SaveDatasetAsPlugIn;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.plugin.EditablePlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.CopySelectedItemsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.CutSelectedItemsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.PasteItemsPlugIn;
import es.kosmo.desktop.plugins.conversion.AffineTransformationPlugIn;
import es.kosmo.desktop.plugins.conversion.PrecisionReducerPlugIn;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import org.saig.core.model.sdi.wfs.WFSFeatureCollection;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.config.EnabledLayerPlugIn;
import org.saig.jump.plugin.editing.CopySelectedItemsToEditableLayerPlugIn;
import org.saig.jump.plugin.editing.PasteAttributesToSelectedFeaturesPlugIn;
import org.saig.jump.plugin.info.LayerInfoPlugIn;
import org.saig.jump.plugin.query.QueryWizardPlugIn;
import org.saig.jump.plugin.stats.CalculateStatsPlugIn;
import org.saig.jump.plugin.utils.ChangeDataSourceInMemoryPlugIn;
import org.saig.jump.plugin.utils.SaveAllViewLayersToShapePlugIn;
import org.saig.jump.plugin.utils.conversion.ExplodeEntitiesPlugIn;
import org.saig.jump.plugin.utils.conversion.ExtractSegmentsPlugIn;
import org.saig.jump.plugin.utils.conversion.ExtractVertexLayerPlugIn;
import org.saig.jump.plugin.utils.conversion.GetCentroidsPlugIn;
import org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn;
import org.saig.jump.plugin.utils.conversion.GetLinesFromPolygonsPlugIn;
import org.saig.jump.plugin.utils.conversion.GetPointsFromLinesPlugIn;
import org.saig.jump.plugin.utils.generalization.DouglasPeuckerSimplificationPlugIn;
import org.saig.jump.plugin.utils.generalization.TopologyPreservingSimplifierPlugIn;
import org.saig.jump.tools.editing.RecalculateXYPointSortNumerationPlugIn;

public class DeactivateExportAndEditabilityForWFSLayersPlugIn
extends AbstractPlugIn {
    private Set<String> forbiddenPlugIns = new HashSet<String>();
    private Set<String> notWFSLayerSelectedPlugIns;

    public DeactivateExportAndEditabilityForWFSLayersPlugIn() {
        this.forbiddenPlugIns.add(QueryWizardPlugIn.NAME);
        this.forbiddenPlugIns.add(SaveAllViewLayersToShapePlugIn.NAME);
        this.forbiddenPlugIns.add(CalculateStatsPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns = new HashSet<String>();
        this.notWFSLayerSelectedPlugIns.add(EditablePlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(EnabledLayerPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(LayerInfoPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(ChangeDataSourceInMemoryPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(CopySelectedItemsPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(CopySelectedItemsToEditableLayerPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(CutSelectedItemsPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(PasteItemsPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(PasteAttributesToSelectedFeaturesPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(SaveDatasetAsPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(RecalculateXYPointSortNumerationPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(GetCentroidsPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(ExplodeEntitiesPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(ExtractSegmentsPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(ExtractVertexLayerPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(GetLinesFromPolygonsPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(GetLinesFromPointsPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(GetLinesFromPointsPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(GetPointsFromLinesPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(AffineTransformationPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(DouglasPeuckerSimplificationPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(TopologyPreservingSimplifierPlugIn.NAME);
        this.notWFSLayerSelectedPlugIns.add(PrecisionReducerPlugIn.NAME);
    }

    @Override
    public void initialize(final PlugInContext context) throws Exception {
        EnableCheck check = new EnableCheck(){

            @Override
            public String check(JComponent component) {
                String pluginName = component.getName();
                if (DeactivateExportAndEditabilityForWFSLayersPlugIn.this.forbiddenPlugIns.contains(pluginName)) {
                    List layers = context.getWorkbenchContext().getAllLayers();
                    boolean hasWFSLayers = false;
                    Iterator itLayers = layers.iterator();
                    while (itLayers.hasNext() && !hasWFSLayers) {
                        Layer currentLayer = (Layer)itLayers.next();
                        boolean bl = hasWFSLayers = currentLayer.isEnabled() && currentLayer.getUltimateFeatureCollectionWrapper() instanceof WFSFeatureCollection;
                    }
                    if (hasWFSLayers) {
                        return I18N.getString("es.kosmo.desktop.plugins.utils.DeactivateExportAndEditabilityForWFSLayersPlugIn.The-tool-is-disabled-if-there-are-WFS-layers-loaded");
                    }
                } else if (DeactivateExportAndEditabilityForWFSLayersPlugIn.this.notWFSLayerSelectedPlugIns.contains(pluginName)) {
                    if (context.getWorkbenchContext().getLayerNamePanel() != null) {
                        Layerable[] layerables = context.getWorkbenchContext().getLayerNamePanel().getSelectedLayers();
                        for (Layerable obj : Arrays.asList(layerables)) {
                            Layer layer;
                            if (!(obj instanceof Layer) || !(layer = (Layer)obj).isEnabled() || !(layer.getUltimateFeatureCollectionWrapper() instanceof WFSFeatureCollection)) continue;
                            return I18N.getString("es.kosmo.desktop.plugins.utils.DeactivateExportAndEditabilityForWFSLayersPlugIn.Tool-disabled-for-WFS-layers");
                        }
                    }
                    return null;
                }
                return null;
            }
        };
        JUMPWorkbench.addGenericCheck(check, false);
    }
}

