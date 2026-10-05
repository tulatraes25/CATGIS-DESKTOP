package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

import java.awt.Component;
import java.awt.Window;

public final class CadWorkflowSupport {

    private CadWorkflowSupport() {
    }

    public static void openGeoreferenceWorkflow(Component owner, Layer layer) {
        if (layer == null || !CadLayerSupport.isCadLayer(layer)) {
            NotificationManager.warn(owner, null, "Selecciona una capa CAD (DWG/DXF) para georreferenciar.");
            return;
        }

        Window dialogOwner = owner instanceof Window
                ? (Window) owner
                : owner != null
                ? javax.swing.SwingUtilities.getWindowAncestor(owner)
                : CatgisDesktopApp.getMainFrameSafe();

        CadGeoreferenceDialog.openInteractive(dialogOwner, layer, result -> {
            if (result == null || !result.approved()) {
                return;
            }
            CadGeoreferenceSupport.applyResultToLayer(layer, result);
            CatgisDesktopApp.markProjectDirty();
            if (CatgisDesktopApp.layersPanel != null) {
                AppContext.refreshLayerList();
            }
            if (AppContext.mapPanel() != null) {
                AppContext.mapPanel().resetView();
                AppContext.mapPanel().repaint();
            }
            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage(
                        "Georreferenciacion CAD actualizada: " + layer.getName() + " -> " + CadGeoreferenceSupport.buildDetailedSummary(layer)
                );
            }
        });
    }

    public static void openCadInternalLayers(Component owner, Layer layer) {
        if (layer == null || !CadLayerSupport.isCadLayer(layer)) {
            NotificationManager.warn(owner, null, "Selecciona una capa CAD (DWG/DXF) para revisar sus capas internas.");
            return;
        }

        ShapefileData data = OpenAttributeTableAction.ensureLayerDataAvailable(layer);
        if (data == null || data.getFeatures() == null || data.getFeatures().isEmpty()) {
            NotificationManager.warn(owner, null, "La capa CAD no tiene elementos cargados para revisar sus capas internas.");
            return;
        }

        CadInternalLayersDialog.Result result = CadInternalLayersDialog.open(owner, layer, data);
        if (result == null || !result.approved()) {
            return;
        }

        layer.setCadHiddenInternalLayers(result.hiddenLayerNames());
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.layersPanel != null) {
            AppContext.refreshLayerList();
        }
        if (AppContext.mapPanel() != null) {
            AppContext.mapPanel().repaint();
        }
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(
                    "Capas internas CAD actualizadas: " + layer.getName() + " -> " + CadLayerSupport.buildCadInternalLayerFilterLabel(layer)
            );
        }
    }

    public static void openCadDragPlacementWorkflow(Component owner, Layer layer) {
        if (layer == null || !CadLayerSupport.isCadLayer(layer)) {
            NotificationManager.warn(owner, null, "Selecciona una capa CAD (DWG/DXF) para moverla sobre el mapa.");
            return;
        }
        if (AppContext.mapPanel() == null) {
            NotificationManager.warn(owner, null, "No se encontro la vista de mapa activa para arrastrar la referencia CAD.");
            return;
        }
        if (AppContext.mapPanel().isCadPlacementDragActive()) {
            NotificationManager.warn(owner, null, "Ya hay un arrastre CAD activo. Termina o cancela el flujo actual.");
            return;
        }
        AppContext.mapPanel().startCadPlacementDrag(
                layer,
                new CadPlacementDragHandler() {
                    @Override
                    public void onDragApplied(double offsetX, double offsetY) {
                        layer.setCadOffsetX(offsetX);
                        layer.setCadOffsetY(offsetY);
                        CatgisDesktopApp.markProjectDirty();
                        if (CatgisDesktopApp.layersPanel != null) {
                            AppContext.refreshLayerList();
                        }
                        if (AppContext.mapPanel() != null) {
                            AppContext.mapPanel().repaint();
                        }
                        if (CatgisDesktopApp.statusBar != null) {
                            AppContext.setStatusMessage(
                                    "Ajuste CAD por arrastre aplicado: " + layer.getName() + " -> " + CadPlacementSupport.buildPlacementSummary(layer)
                            );
                        }
                    }

                    @Override
                    public void onDragCanceled() {
                        if (CatgisDesktopApp.layersPanel != null) {
                            AppContext.refreshLayerList();
                        }
                        if (AppContext.mapPanel() != null) {
                            AppContext.mapPanel().repaint();
                        }
                        if (CatgisDesktopApp.statusBar != null) {
                            AppContext.setStatusMessage("Arrastre CAD cancelado: " + layer.getName());
                        }
                    }
                },
                "Arrastre CAD activo: clic izquierdo y arrastra sobre el mapa para mover la referencia. Suelta para aplicar. Clic derecho o Esc cancela.",
                "Arrastre CAD aplicado.",
                "Arrastre CAD cancelado."
        );
    }
}

