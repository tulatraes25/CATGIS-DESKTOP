package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.online.OnlineWmsLayer;
import ar.com.catgis.data.online.OnlineRasterSource;
import ar.com.catgis.core.model.Layer;

import javax.swing.JOptionPane;

public final class OnlineBaseMapAction {

    private OnlineBaseMapAction() {
    }

    public static void openDialog() {
        OnlineBaseMapDialog.open(CatgisDesktopApp.getMainFrameSafe());
    }

    public static boolean addBaseMap(String sourceId) {
        OnlineRasterSource source = OnlineMapCatalog.getById(sourceId);
        if (source == null) {
            JOptionPane.showMessageDialog(
                    CatgisDesktopApp.getMainFrameSafe(),
                    "No se encontro el proveedor de mapa base solicitado.",
                    "Mapas base online",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }
        return addBaseMap(source);
    }

    public static boolean addBaseMap(OnlineRasterSource source) {
        if (source == null) {
            return false;
        }

        ensureProject();
        OnlineTileLayer existing = findExistingLayer(source.getId());
        if (existing != null) {
            hideOtherOnlineBaseMaps(existing);
            existing.setVisible(true);
            if (CatgisDesktopApp.layersPanel != null) {
                AppContext.selectLayer(existing);
                AppContext.refreshLayerList();
            }
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.addOrUpdateOnlineTileLayer(existing);
                if (hasNoProjectDataLayers()) {
                    CatgisDesktopApp.mapPanel.zoomToLayer(existing);
                } else {
                    CatgisDesktopApp.mapPanel.refreshMap();
                }
            }
            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage("Mapa base activo: " + existing.getName());
            }
            CatgisDesktopApp.markProjectDirty();
            return true;
        }

        OnlineTileLayer layer = source.createLayer();
        hideOtherOnlineBaseMaps(layer);
        AppContext.project().addLayer(layer);
        if (CatgisDesktopApp.layersPanel != null) {
            AppContext.addLayer(layer);
            AppContext.selectLayer(layer);
        }
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.addOrUpdateOnlineTileLayer(layer);
            if (hasNoProjectDataLayers()) {
                CatgisDesktopApp.mapPanel.zoomToLayer(layer);
            }
        }
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage("Mapa base online agregado: " + layer.getName());
        }
        return true;
    }

    private static void ensureProject() {
        if (AppContext.project() == null) {
            AppContext.setCurrentProject(new Project("Proyecto actual"));
        }
    }

    private static OnlineTileLayer findExistingLayer(String sourceId) {
        if (AppContext.project() == null || sourceId == null || sourceId.isBlank()) {
            return null;
        }
        for (Layer layer : AppContext.project().getLayers()) {
            if (layer instanceof OnlineTileLayer) {
                OnlineTileLayer tileLayer = (OnlineTileLayer) layer;
                if (sourceId.equalsIgnoreCase(tileLayer.getSourceId())) {
                    return tileLayer;
                }
            }
        }
        return null;
    }

    private static void hideOtherOnlineBaseMaps(Layer keepLayer) {
        if (AppContext.project() == null) {
            return;
        }
        for (Layer layer : AppContext.project().getLayers()) {
            if (layer instanceof OnlineTileLayer && layer != keepLayer) {
                layer.setVisible(false);
            }
        }
    }

    private static boolean hasNoProjectDataLayers() {
        if (AppContext.project() == null) {
            return true;
        }
        for (Layer layer : AppContext.project().getLayers()) {
            if (layer == null) {
                continue;
            }
            if (layer instanceof OnlineTileLayer || layer instanceof OnlineWmsLayer) {
                continue;
            }
            return false;
        }
        return true;
    }
}
