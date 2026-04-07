package ar.com.catgis;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class SaveProjectAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        saveProject();
    }

    public static boolean saveProject() {
        if (CatgisDesktopApp.currentProject == null) {
            JOptionPane.showMessageDialog(null, "No hay proyecto actual.");
            return false;
        }

        File targetFile = CatgisDesktopApp.currentProject.getProjectFile();
        if (targetFile == null) {
            return saveProjectAs();
        }

        return saveProjectToFile(targetFile);
    }

    public static boolean saveProjectAs() {
        if (CatgisDesktopApp.currentProject == null) {
            JOptionPane.showMessageDialog(null, "No hay proyecto actual.");
            return false;
        }

        JFileChooser chooser = FileChooserSupport.createChooser("project-save", "Guardar proyecto CATGIS como");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Proyectos CATGIS (*.catgis)", "catgis"));
        chooser.setSelectedFile(FileChooserSupport.resolveSuggestedFile("project-save", buildSuggestedFile()));

        int result = chooser.showSaveDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".catgis")) {
            file = new File(file.getAbsolutePath() + ".catgis");
        }

        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(
                    null,
                    "El archivo ya existe.\n¿Querés reemplazarlo?\n\n" + file.getAbsolutePath(),
                    "Guardar proyecto",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (overwrite != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        return saveProjectToFile(file);
    }

    private static boolean saveProjectToFile(File file) {
        if (!persistVectorLayers()) {
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("CATGIS_PROJECT");
            writer.newLine();

            writer.write("PROJECT_CRS|" + safe(CatgisDesktopApp.currentProject.getProjectCRS()));
            writer.newLine();

            writer.write("PROJECT_META|STUDY_NAME|" + safe(CatgisDesktopApp.currentProject.getStudyName()));
            writer.newLine();
            writer.write("PROJECT_META|COMPANY_NAME|" + safe(CatgisDesktopApp.currentProject.getCompanyName()));
            writer.newLine();
            writer.write("PROJECT_META|CARTOGRAPHER_NAME|" + safe(CatgisDesktopApp.currentProject.getCartographerName()));
            writer.newLine();
            writer.write("PROJECT_META|IMAGE_SOURCE|" + safe(CatgisDesktopApp.currentProject.getImageSource()));
            writer.newLine();
            writer.write("PROJECT_META|COORDINATE_REFERENCE|" + safe(CatgisDesktopApp.currentProject.getCoordinateReference()));
            writer.newLine();
            writer.write("PROJECT_META|LEGEND_TITLE|" + safe(CatgisDesktopApp.currentProject.getLegendTitle()));
            writer.newLine();
            writer.write("PROJECT_META|LEGEND_SUBTITLE|" + safe(CatgisDesktopApp.currentProject.getLegendSubtitle()));
            writer.newLine();
            writer.write("PROJECT_META|LOGO_PATH|" + safe(CatgisDesktopApp.currentProject.getLogoPath()));
            writer.newLine();
            writer.write("PROJECT_META|LAYOUT_IMAGE_PATH|" + safe(CatgisDesktopApp.currentProject.getLayoutImagePath()));
            writer.newLine();

            writer.write("VIEW|"
                    + CatgisDesktopApp.mapPanel.getViewMinX() + "|"
                    + CatgisDesktopApp.mapPanel.getViewMinY() + "|"
                    + CatgisDesktopApp.mapPanel.getZoomFactor());
            writer.newLine();

            for (Layer layer : CatgisDesktopApp.currentProject.getLayers()) {
                String line = buildLayerLine(layer);
                writer.write(line);
                writer.newLine();
            }

            CatgisDesktopApp.currentProject.setProjectFile(file);
            CatgisDesktopApp.currentProject.setName(stripExtension(file.getName()));
            CatgisDesktopApp.markProjectClean();
            FileChooserSupport.rememberFile("project-save", file);
            FileChooserSupport.rememberFile("project-open", file);

            if (CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage("Proyecto guardado: " + file.getAbsolutePath());
            }

            JOptionPane.showMessageDialog(null, "Proyecto guardado correctamente.");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al guardar proyecto: " + ex.getMessage());
            return false;
        }
    }

    private static String buildLayerLine(Layer layer) {
        String type = safe(layer.getType());
        String name = safe(layer.getName());
        String path = safe(layer.getPath());
        String visible = String.valueOf(layer.isVisible());
        String labelsVisible = String.valueOf(layer.isLabelsVisible());
        String labelField = safe(layer.getLabelField());
        String sourceCRS = safe(layer.getSourceCRS());

        String fillColor = colorToText(layer.getFillColor());
        String borderColor = colorToText(layer.getBorderColor());
        String lineColor = colorToText(layer.getLineColor());
        String lineWidth = String.valueOf(layer.getLineWidth());
        String pointColor = colorToText(layer.getPointColor());
        String pointSize = String.valueOf(layer.getPointSize());
        String pointStyle = safe(layer.getPointSymbolStyle().name());
        String lineStyle = safe(layer.getLineSymbolStyle().name());
        String polygonStyle = safe(layer.getPolygonFillStyle().name());
        String lineTheme = safeOrPlaceholder(LayerSymbologyCodec.encodeCategorizedSymbology(layer.getLineCategorizedSymbology()));
        String polygonTheme = safeOrPlaceholder(LayerSymbologyCodec.encodeCategorizedSymbology(layer.getPolygonCategorizedSymbology()));

        StringBuilder sb = new StringBuilder();
        sb.append("[").append(type).append("]")
                .append("|").append(name)
                .append("|").append(path)
                .append("|").append(visible)
                .append("|").append(labelsVisible)
                .append("|").append(labelField)
                .append("|").append(fillColor)
                .append("|").append(borderColor)
                .append("|").append(lineColor)
                .append("|").append(lineWidth)
                .append("|").append(pointColor)
                .append("|").append(pointSize)
                .append("|").append(sourceCRS)
                .append("|").append(pointStyle)
                .append("|").append(lineStyle)
                .append("|").append(polygonStyle)
                .append("|").append(lineTheme)
                .append("|").append(polygonTheme);

        if (layer instanceof RasterLayer) {
            RasterLayer raster = (RasterLayer) layer;
            sb.append("|").append(raster.getOpacity())
              .append("|").append(raster.isGrayscale())
              .append("|").append(raster.isAutoContrast())
              .append("|").append(raster.getRedBand())
              .append("|").append(raster.getGreenBand())
              .append("|").append(raster.getBlueBand())
              .append("|").append(safe(raster.getRasterMode()));
        }

        if (layer instanceof OnlineTileLayer) {
            OnlineTileLayer tile = (OnlineTileLayer) layer;
            sb.append("|").append(safe(tile.getSourceId()))
              .append("|").append(safe(tile.getProviderName()))
              .append("|").append(safe(tile.getUrlTemplate()))
              .append("|").append(tile.getMinZoom())
              .append("|").append(tile.getMaxZoom())
              .append("|").append(safe(tile.getAttribution()))
              .append("|").append(safe(tile.getTermsUrl()))
              .append("|").append(tile.isRequiresApiKey());
        }

        if (layer instanceof OnlineWmsLayer) {
            OnlineWmsLayer wms = (OnlineWmsLayer) layer;
            sb.append("|").append(safe(wms.getSourceId()))
              .append("|").append(safe(wms.getProviderName()))
              .append("|").append(safe(wms.getServiceUrl()))
              .append("|").append(safe(wms.getLayerNames()))
              .append("|").append(safe(wms.getStyleNames()))
              .append("|").append(safe(wms.getRequestCrs()))
              .append("|").append(safe(wms.getVersion()))
              .append("|").append(safe(wms.getImageFormat()))
              .append("|").append(wms.isTransparent())
              .append("|").append(safe(wms.getAttribution()))
              .append("|").append(safe(wms.getTermsUrl()))
              .append("|").append(safe(wms.getExtentCrs()))
              .append("|").append(wms.getExtentMinX())
              .append("|").append(wms.getExtentMinY())
              .append("|").append(wms.getExtentMaxX())
              .append("|").append(wms.getExtentMaxY());
        }

        if (layer instanceof OnlineWfsLayer) {
            OnlineWfsLayer wfs = (OnlineWfsLayer) layer;
            sb.append("|").append(safe(wfs.getProviderName()))
              .append("|").append(safe(wfs.getServiceUrl()))
              .append("|").append(safe(wfs.getTypeName()))
              .append("|").append(safe(wfs.getTypeTitle()))
              .append("|").append(safe(wfs.getRequestCrs()))
              .append("|").append(safe(wfs.getVersion()))
              .append("|").append(wfs.isReadOnly());
        }

        if (layer instanceof GeoPackageLayer) {
            GeoPackageLayer geoPackage = (GeoPackageLayer) layer;
            sb.append("|").append(safe(geoPackage.getTableName()))
              .append("|").append(safe(geoPackage.getIdentifier()))
              .append("|").append(safe(geoPackage.getDescription()))
              .append("|").append(safe(geoPackage.getGeometryTypeLabel()))
              .append("|").append(geoPackage.isReadOnly());
        }

        if (layer instanceof PostgisLayer) {
            PostgisLayer postgis = (PostgisLayer) layer;
            sb.append("|").append(safe(postgis.getHost()))
              .append("|").append(postgis.getPort())
              .append("|").append(safe(postgis.getDatabaseName()))
              .append("|").append(safe(postgis.getSchemaName()))
              .append("|").append(safe(postgis.getUserName()))
              .append("|").append(safe(postgis.getTypeName()))
              .append("|").append(safe(postgis.getTableName()))
              .append("|").append(safe(postgis.getGeometryTypeLabel()))
              .append("|").append(postgis.isReadOnly());
        }

        return sb.toString();
    }

    private static File buildSuggestedFile() {
        File currentFile = CatgisDesktopApp.currentProject.getProjectFile();
        if (currentFile != null) {
            return currentFile;
        }

        String projectName = CatgisDesktopApp.currentProject.getName();
        if (projectName == null || projectName.isBlank()) {
            projectName = "proyecto";
        }

        String safeName = projectName.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        if (safeName.isEmpty()) {
            safeName = "proyecto";
        }
        return new File(safeName + ".catgis");
    }

    private static String stripExtension(String name) {
        if (name == null || name.isBlank()) {
            return "Proyecto sin nombre";
        }

        int idx = name.lastIndexOf('.');
        if (idx > 0) {
            return name.substring(0, idx);
        }
        return name;
    }

    private static String colorToText(java.awt.Color color) {
        if (color == null) {
            return "";
        }
        return color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha();
    }

    private static boolean persistVectorLayers() {
        if (CatgisDesktopApp.currentProject == null || CatgisDesktopApp.mapPanel == null) {
            return true;
        }

        for (Layer layer : CatgisDesktopApp.currentProject.getLayers()) {
            if (layer == null || layer instanceof RasterLayer || VectorLayerUtils.isReadOnlyVectorLayer(layer)) {
                continue;
            }

            ShapefileData data = CatgisDesktopApp.mapPanel.getShapefileData(layer);
            if (!ExportVectorLayerAction.hasExportableVectorData(data)) {
                continue;
            }

            boolean ok;
            if (ExportVectorLayerAction.hasSupportedVectorPath(layer)) {
                ok = ExportVectorLayerAction.saveLayerToCurrentPath(layer, CatgisDesktopApp.getMainFrameSafe(), false);
            } else {
                JOptionPane.showMessageDialog(
                        CatgisDesktopApp.getMainFrameSafe(),
                        "La capa \"" + layer.getName() + "\" no tiene archivo asociado.\nElegÃ­ dÃ³nde guardarla antes de guardar el proyecto.",
                        "Guardar capa vectorial",
                        JOptionPane.INFORMATION_MESSAGE
                );
                File exported = ExportVectorLayerAction.exportLayerWithDialog(
                        layer,
                        data,
                        CatgisDesktopApp.getMainFrameSafe(),
                        "Guardar capa vectorial",
                        false
                );
                ok = exported != null;
            }

            if (!ok) {
                return false;
            }
        }

        return true;
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace("|", " ");
    }

    private static String safeOrPlaceholder(String value) {
        String normalized = safe(value);
        return normalized.isBlank() ? "-" : normalized;
    }
}
