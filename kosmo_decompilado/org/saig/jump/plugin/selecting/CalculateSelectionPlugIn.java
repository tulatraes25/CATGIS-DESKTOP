/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.selecting;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.AbstractSelection;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.Utils;
import org.saig.jump.widgets.util.DialogFactory;

public class CalculateSelectionPlugIn
extends AbstractPlugIn {
    private static final Logger LOGGER = Logger.getLogger(CalculateSelectionPlugIn.class);
    protected boolean wasShiftPressed;
    protected Geometry fence;
    protected AbstractSelection selection;
    protected List<Layerable> layersToFilter;
    protected String name;

    public CalculateSelectionPlugIn(String toolName, boolean shiftPressed, Geometry fence, AbstractSelection selection, List<Layerable> layersToFilter) {
        this.name = toolName;
        this.wasShiftPressed = shiftPressed;
        this.fence = fence;
        this.selection = selection;
        this.layersToFilter = layersToFilter;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Map<Layer, Collection<Feature>> layerToFeaturesInFenceMap;
        this.reportNothingToUndoYet(context);
        if (!this.wasShiftPressed) {
            context.getLayerViewPanel().getSelectionManager().clear();
        }
        if (!(layerToFeaturesInFenceMap = context.getLayerViewPanel().layersToFeaturesInFenceMap(this.layersToFilter, this.fence)).isEmpty()) {
            this.refreshSelection(layerToFeaturesInFenceMap, context.getLayerViewPanel());
        }
        return false;
    }

    protected void refreshSelection(Map<Layer, Collection<Feature>> layerToFeaturesInFenceMap, LayerViewPanel panel) throws Exception {
        Layer[] layers = new Layer[layerToFeaturesInFenceMap.keySet().size()];
        layerToFeaturesInFenceMap.keySet().toArray(layers);
        int i = 0;
        while (i < layers.length) {
            block11: {
                Layer layer = layers[i];
                boolean originalPanelUpdatesEnabled = panel.getSelectionManager().arePanelUpdatesEnabled();
                panel.getSelectionManager().setPanelUpdatesEnabled(false);
                try {
                    Collection<Feature> featuresToSelect = layerToFeaturesInFenceMap.get(layer);
                    if (featuresToSelect == null) break block11;
                    Collection<Feature> featuresToUnselect = this.selection.getFeaturesWithSelectedItems(layer);
                    featuresToUnselect.retainAll(featuresToSelect);
                    if (layer.isEditable() && layer.isDataBaseDataSource() && this.isConcurrentEditionActivated()) {
                        try {
                            HashSet<Feature> listaTotal = new HashSet<Feature>();
                            for (Feature feature : featuresToSelect) {
                                if (featuresToUnselect.contains(feature)) continue;
                                listaTotal.add(feature);
                                listaTotal.addAll(Utils.getColindantes(feature.getGeometry(), layer));
                            }
                            layer.getTransactionalDataSource().lockFeatures(listaTotal);
                        }
                        catch (SQLException e) {
                            LOGGER.error((Object)"", (Throwable)e);
                            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("workbench.ui.cursortool.SelectFeaturesTool.another-user-has-blocked-the-records-selected-for-editing")) + ".\n" + I18N.getString("workbench.ui.cursortool.SelectFeaturesTool.the-transaction-is-cancelled"), I18N.getString("workbench.ui.cursortool.SelectFeaturesTool.blocking-error"));
                            panel.getSelectionManager().setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
                            return;
                        }
                    }
                    this.selection.selectItems(layer, featuresToSelect);
                    if (this.wasShiftPressed) {
                        this.selection.unselectItems(layer, featuresToUnselect);
                    }
                }
                finally {
                    panel.getSelectionManager().setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
                }
            }
            ++i;
        }
        panel.getSelectionManager().updatePanel();
    }
}

