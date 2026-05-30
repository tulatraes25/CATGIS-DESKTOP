/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.AbstractSelection;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.selecting.MultiSelectDialog;
import org.saig.jump.widgets.util.DialogFactory;

public class CrossesLayersPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = String.valueOf(I18N.getString("org.saig.jump.plugin.utils.CrossesLayersPlugIn.name")) + "...";
    public static final Icon ICON = IconLoader.icon("ToFront.gif");
    private String operation;
    private AbstractSelection selection;
    private Layer sourceLayer;
    private Object[] targetLayers;
    private Set seleccionados = new HashSet();

    @Override
    public void initialize(PlugInContext context) throws Exception {
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
        return CrossesLayersPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        LayerViewPanel layerViewPanel = context.getLayerViewPanel();
        this.selection = layerViewPanel.getSelectionManager().getFeatureSelection();
        List<Layer> layers = layerViewPanel.getLayerManager().getNoRasterLayers();
        MultiSelectDialog dialog = new MultiSelectDialog(context.getWorkbenchFrame(), layers, true);
        if (!dialog.isOk()) return false;
        if (dialog.isInputValid()) {
            this.sourceLayer = dialog.getSourceLayer();
            if (dialog.getTargetLayers() == null) return true;
            this.targetLayers = dialog.getTargetLayers();
            this.operation = dialog.getSelectedOperation();
            return true;
        } else {
            DialogFactory.showErrorDialog(context.getWorkbenchFrame(), I18N.getString("org.saig.jump.plugin.utils.CrossesLayersPlugIn.you-must-select-at-least-one-layer-to-cross"), I18N.getString("org.saig.jump.plugin.utils.CrossesLayersPlugIn.error"));
            return false;
        }
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        if (this.sourceLayer == null || this.targetLayers == null) {
            return;
        }
        panel = context.getWorkbenchContext().getLayerViewPanel();
        numFeaturesSelected = panel.getSelectionManager().getNumFeaturesWithSelectedItems(this.sourceLayer);
        this.seleccionados = new HashSet<E>();
        this.selection.unselectItems(this.sourceLayer);
        fcSource = null;
        if (numFeaturesSelected == 0) {
            fcSource = this.sourceLayer.getUltimateFeatureCollectionWrapper();
        } else {
            col = panel.getSelectionManager().getFeaturesWithSelectedItems(this.sourceLayer);
            fcSource = new FeatureDataset(this.sourceLayer.getUltimateFeatureCollectionWrapper().getFeatureSchema());
            fcSource.addAll(col);
        }
        i = 0;
        while (i < this.targetLayers.length) {
            block23: {
                targetLayer = context.getLayerManager().getLayer((String)this.targetLayers[i]);
                monitor.report(I18N.getMessage("org.saig.jump.plugin.utils.CrossesLayersPlugIn.processing-layer-{0}", new Object[]{targetLayer.getName()}));
                fcTarget = targetLayer.getUltimateFeatureCollectionWrapper();
                itSource = null;
                itCandidatos = null;
                numFeatures = false;
                try {
                    block24: {
                        if (fcSource.size() > fcTarget.size()) break block24;
                        itSource = fcSource.iterator();
                        block10: while (itSource.hasNext()) {
                            featSource = itSource.next();
                            geomSource = featSource.getGeometry();
                            try {
                                itCandidatos = fcTarget.queryIterator(geomSource.getEnvelopeInternal());
                                if (true) ** GOTO lbl34
                                do {
                                    if ((featOverlay = itCandidatos.next()) != null && this.computeOperation(featSource, featOverlay, this.operation)) {
                                        continue block10;
                                    }
lbl34:
                                    // 3 sources

                                    if (!itCandidatos.hasNext()) continue block10;
                                } while (!monitor.isCancelRequested());
                            }
                            finally {
                                if (itCandidatos != null) {
                                    itCandidatos.close();
                                }
                            }
                        }
                        break block23;
                    }
                    itSource = fcTarget.iterator();
                    block12: while (itSource.hasNext()) {
                        featOverlay = itSource.next();
                        geomOverlay = featOverlay.getGeometry();
                        try {
                            itCandidatos = fcSource.queryIterator(geomOverlay.getEnvelopeInternal());
                            if (true) ** GOTO lbl54
                            do {
                                if ((featSource = itCandidatos.next()) != null) {
                                    this.computeOperation(featSource, featOverlay, this.operation);
                                }
lbl54:
                                // 4 sources

                                if (!itCandidatos.hasNext()) continue block12;
                            } while (!monitor.isCancelRequested());
                        }
                        finally {
                            if (itCandidatos != null) {
                                itCandidatos.close();
                            }
                        }
                    }
                }
                finally {
                    if (itSource != null) {
                        itSource.close();
                    }
                }
            }
            ++i;
        }
        if (this.seleccionados != null && this.seleccionados.size() > 0) {
            this.selection.selectItems(this.sourceLayer, this.seleccionados);
        }
        this.sourceLayer = null;
        this.targetLayers = null;
        this.seleccionados = null;
    }

    private boolean computeOperation(Feature featSource, Feature featOverlay, String operation) {
        boolean ok = false;
        if (operation.equals(MultiSelectDialog.CUBRE_A)) {
            if (featSource.getGeometry().covers(featOverlay.getGeometry())) {
                this.seleccionados.add(featSource);
                ok = true;
            }
        } else if (operation.equals(MultiSelectDialog.CUBIERTA_POR)) {
            if (featSource.getGeometry().coveredBy(featOverlay.getGeometry())) {
                this.seleccionados.add(featSource);
                ok = true;
            }
        } else if (operation.equals(MultiSelectDialog.INTERSECTA_CON)) {
            if (featSource.getGeometry().intersects(featOverlay.getGeometry())) {
                this.seleccionados.add(featSource);
                ok = true;
            }
        } else if (operation.equals(MultiSelectDialog.CRUZA_CON)) {
            if (featSource.getGeometry().crosses(featOverlay.getGeometry())) {
                this.seleccionados.add(featSource);
                ok = true;
            }
        } else if (operation.equals(MultiSelectDialog.TOCA_CON) && featSource.getGeometry().touches(featOverlay.getGeometry())) {
            this.seleccionados.add(featSource);
            ok = true;
        }
        return ok;
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createAtLeastNLayersMustExistCheck(2));
        solucion.add(checkFactory.createAtLeastNLayersMustNotBeWMSLayersCheck(1));
        return solucion;
    }
}

