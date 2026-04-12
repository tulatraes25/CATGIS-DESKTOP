package ar.com.catgis;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.Window;

public final class CadWorkflowSupport {

    private CadWorkflowSupport() {
    }

    public static void openGeoreferenceWorkflow(Component owner, Layer layer) {
        if (layer == null || !CadLayerSupport.isCadLayer(layer)) {
            JOptionPane.showMessageDialog(owner, "Selecciona una capa CAD (DWG/DXF) para georreferenciar.");
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
                CatgisDesktopApp.layersPanel.refreshLayerList();
            }
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.resetView();
                CatgisDesktopApp.mapPanel.repaint();
            }
            if (CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage(
                        "Georreferenciacion CAD actualizada: " + layer.getName() + " -> " + CadGeoreferenceSupport.buildDetailedSummary(layer)
                );
            }
        });
    }

    public static void openCadInternalLayers(Component owner, Layer layer) {
        if (layer == null || !CadLayerSupport.isCadLayer(layer)) {
            JOptionPane.showMessageDialog(owner, "Selecciona una capa CAD (DWG/DXF) para revisar sus capas internas.");
            return;
        }

        ShapefileData data = OpenAttributeTableAction.ensureLayerDataAvailable(layer);
        if (data == null || data.getFeatures() == null || data.getFeatures().isEmpty()) {
            JOptionPane.showMessageDialog(owner, "La capa CAD no tiene elementos cargados para revisar sus capas internas.");
            return;
        }

        CadInternalLayersDialog.Result result = CadInternalLayersDialog.open(owner, layer, data);
        if (result == null || !result.approved()) {
            return;
        }

        layer.setCadHiddenInternalLayers(result.hiddenLayerNames());
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.layersPanel != null) {
            CatgisDesktopApp.layersPanel.refreshLayerList();
        }
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.repaint();
        }
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(
                    "Capas internas CAD actualizadas: " + layer.getName() + " -> " + CadLayerSupport.buildCadInternalLayerFilterLabel(layer)
            );
        }
    }
}
