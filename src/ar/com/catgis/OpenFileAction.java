package ar.com.catgis;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

public class OpenFileAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        openFile();
    }

    public static void openFile() {
        Window owner = CatgisDesktopApp.getMainFrameSafe();
        AddLayerDialog.open(owner);
    }

    public static boolean openSelectedFiles(File[] files, String requestedFormat, Component parent) {
        if (files == null || files.length == 0) {
            return false;
        }

        boolean anyLoaded = false;
        for (File file : files) {
            if (file == null) {
                continue;
            }
            anyLoaded |= openSelectedFile(file, requestedFormat, parent);
        }
        return anyLoaded;
    }

    public static boolean openSelectedFile(File file, String requestedFormat, Component parent) {
        if (file == null) {
            return false;
        }

        try {
            String lowerName = file.getName().toLowerCase();
            ShapefileData data = null;
            String sourceCRS = "";

            if (lowerName.endsWith(".gpkg")) {
                return GeoPackageDataSourceAction.openGeoPackageDataSource(file, parent);
            } else if (lowerName.endsWith(".shp")) {
                data = ShapefileLoader.load(file);
                sourceCRS = ShapefileLoader.getCRSCode(file);
            } else if (lowerName.endsWith(".geojson") || lowerName.endsWith(".json")) {
                data = GeoJsonLoader.load(file);
                sourceCRS = "EPSG:4326";
            } else if (lowerName.endsWith(".kml")) {
                data = KmlLoader.load(file);
                sourceCRS = "EPSG:4326";
            }

            if (data != null) {
                Layer layer = new VectorLayer(file.getName(), file.getAbsolutePath());
                layer.setVisible(true);
                layer.setSourceName(data.getSourceName());
                layer.setFeatureCount(data.getFeatureCount());
                layer.setSourceCRS(sourceCRS);
                ensureProject();
                CatgisDesktopApp.currentProject.addLayer(layer);
                CatgisDesktopApp.markProjectDirty();
                CatgisDesktopApp.layersPanel.addLayer(layer);
                CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(layer, data);
                CatgisDesktopApp.mapPanel.showOpenedFile(layer.getName());
                if (CatgisDesktopApp.statusBar != null) {
                    CatgisDesktopApp.statusBar.setMessage("Capa agregada: " + layer.getName());
                }
                JOptionPane.showMessageDialog(
                        parent,
                        buildVectorLoadMessage(layer),
                        "Cargar datos",
                        layer.getSourceCRS().isBlank() ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE
                );
                return true;
            }

            if (isRasterOrImage(lowerName)) {
                return openRasterFileInternal(file, parent, false);
            }

            if (lowerName.endsWith(".dxf")) {
                JOptionPane.showMessageDialog(
                        parent,
                        "La carga de DXF todavia no esta implementada en esta version.\n\nArchivo: " + file.getName(),
                        "Cargar datos",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return false;
            }

            String suffix = (requestedFormat != null && !requestedFormat.isBlank())
                    ? "\nFormato seleccionado: " + requestedFormat
                    : "";
            JOptionPane.showMessageDialog(
                    parent,
                    "Formato todavia no implementado o no reconocido: " + file.getName() + suffix,
                    "Cargar datos",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    parent,
                    "Error al agregar capa: " + ex.getMessage(),
                    "Cargar datos",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    private static void ensureProject() {
        if (CatgisDesktopApp.currentProject == null) {
            CatgisDesktopApp.currentProject = new Project("Proyecto actual");
        }
    }

    private static boolean isRasterOrImage(String lowerName) {
        return lowerName.endsWith(".tif")
                || lowerName.endsWith(".tiff")
                || lowerName.endsWith(".asc")
                || lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".png")
                || lowerName.endsWith(".bmp")
                || lowerName.endsWith(".gif")
                || lowerName.endsWith(".img");
    }

    public static boolean openDemRasterFile(File file, Component parent) {
        return openRasterFileInternal(file, parent, true);
    }

    private static boolean openRasterFileInternal(File file, Component parent, boolean demMode) {
        try {
            ensureProject();
            String projectCRS = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
            LocalRasterData rasterData = RasterImageLoader.loadPreview(file, projectCRS, null);
            RasterLayer layer = new RasterLayer(file.getName(), file.getAbsolutePath());
            layer.setVisible(true);
            layer.setSourceName(demMode ? I18n.t("DEM local") : file.getName());
            layer.setFeatureCount(1);
            layer.setSourceCRS(rasterData.getSourceCRS());
            layer.setRasterMode(rasterData.getRasterMode());
            CatgisDesktopApp.currentProject.addLayer(layer);
            CatgisDesktopApp.markProjectDirty();
            CatgisDesktopApp.layersPanel.addLayer(layer);
            CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(layer, rasterData);
            CatgisDesktopApp.mapPanel.showOpenedFile(layer.getName());
            if (CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage(
                        (demMode ? I18n.t("DEM local agregado: ") : "Raster agregado en vista rapida: ") + layer.getName()
                );
            }

            StringBuilder msg = new StringBuilder();
            msg.append(demMode ? I18n.t("DEM local agregado correctamente: ") : "Raster agregado correctamente: ")
                    .append(layer.getName());
            msg.append("\n").append(I18n.t("Bandas:")).append(" ").append(rasterData.getBandCount());
            if (!rasterData.getSourceCRS().isBlank()) {
                msg.append("\nCRS: ").append(rasterData.getSourceCRS());
            } else {
                msg.append("\nCRS: ").append(I18n.t("no definido"));
            }
            msg.append("\n\n").append(I18n.t("Modo inicial: Vista rapida."));
            msg.append("\n").append(I18n.t("Puedes usar clic derecho sobre la capa para cambiar a Zoom virtual o Zoom real."));
            if (demMode) {
                msg.append("\n").append(I18n.t("Esta capa DEM ya queda disponible para curvas de nivel y perfil topografico."));
            }
            if (!rasterData.isGeoreferenced()) {
                msg.append("\n\n").append(I18n.t("Aviso: no se encontro world file ni georreferenciacion interna util. Se mostrara en coordenadas de imagen."));
            }
            if (rasterData.getSourceCRS().isBlank()) {
                msg.append("\n\n").append(I18n.t("Recomendacion: defini el CRS real de la capa desde el panel de capas para ubicarla correctamente con otros datos."));
            }

            JOptionPane.showMessageDialog(
                    parent,
                    msg.toString(),
                    demMode ? I18n.t("Cargar datos DEM") : "Cargar datos",
                    rasterData.getSourceCRS().isBlank() ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE
            );
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    parent,
                    (demMode ? I18n.t("Error al cargar DEM: ") : "Error al agregar capa: ") + ex.getMessage(),
                    demMode ? I18n.t("Cargar datos DEM") : "Cargar datos",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    private static String buildVectorLoadMessage(Layer layer) {
        StringBuilder msg = new StringBuilder();
        msg.append("Capa agregada correctamente: ").append(layer.getName());

        if (!layer.getSourceCRS().isBlank()) {
            msg.append("\nCRS: ").append(layer.getSourceCRS());
        } else {
            msg.append("\nCRS: no definido");
            msg.append("\n\nRecomendacion: defini el CRS real de la capa desde el panel de capas para ubicarla correctamente en el proyecto.");
        }

        return msg.toString();
    }
}
