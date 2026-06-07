package ar.com.catgis;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.data.online.OnlineWmsLayer;
import ar.com.catgis.core.model.LayerGroup;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;

import ar.com.catgis.core.model.Project;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

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

        return saveProjectToFile(targetFile, true);
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

        return saveProjectToFile(file, true);
    }

    static boolean saveProjectToFile(File file, boolean showDialogs) {
        if (file == null) {
            return false;
        }
        CatgisDesktopApp.currentProject.setProjectFile(file);
        if (!persistVectorLayers(file, showDialogs)) {
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
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
            writer.write("PROJECT_META|CATMAP_NORTH_STYLE|" + safe(CatgisDesktopApp.currentProject.getCatmapNorthStyle()));
            writer.newLine();
            writer.write("PROJECT_META|CATMAP_SHOW_NORTH|" + CatgisDesktopApp.currentProject.isCatmapShowNorth());
            writer.newLine();
            for (CatmapLayoutItem item : CatgisDesktopApp.currentProject.getCatmapItems()) {
                if (item != null) {
                    writer.write("PROJECT_LAYOUT_ITEM|" + safe(item.encode()));
                    writer.newLine();
                }
            }
            for (CatmapLegendItem item : CatgisDesktopApp.currentProject.getCatmapLegendItems()) {
                if (item != null) {
                    writer.write("PROJECT_LEGEND_ITEM|" + safe(item.encode()));
                    writer.newLine();
                }
            }
            for (LayerGroup group : CatgisDesktopApp.currentProject.getLayerGroups()) {
                if (group != null) {
                    writer.write("PROJECT_LAYER_GROUP|"
                            + safe(group.getName()) + "|"
                            + group.isVisible() + "|"
                            + group.isExpanded());
                    writer.newLine();
                }
            }

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

            if (showDialogs) {
                JOptionPane.showMessageDialog(null, "Proyecto guardado correctamente.");
            }
            return true;
        } catch (Exception ex) {
            AppErrorSupport.logFailure("Error al guardar proyecto " + file.getAbsolutePath(), ex);
            if (showDialogs) {
                AppErrorSupport.showErrorDialog(
                        CatgisDesktopApp.getMainFrameSafe(),
                        "Guardar proyecto",
                        "Error al guardar proyecto.",
                        ex
                );
            }
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
        String pointTheme = "POINT_THEME=" + safe(LayerSymbologyCodec.encodeCategorizedSymbology(layer.getPointCategorizedSymbology()));
        String lineTheme = safeOrPlaceholder(LayerSymbologyCodec.encodeCategorizedSymbology(layer.getLineCategorizedSymbology()));
        String polygonTheme = safeOrPlaceholder(LayerSymbologyCodec.encodeCategorizedSymbology(layer.getPolygonCategorizedSymbology()));
        String pointGraphic = "POINT_ICON=" + safe(layer.getPointGraphicSymbol());

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
                .append("|").append(polygonTheme)
                .append("|").append(pointGraphic)
                .append("|").append(pointTheme);

        // Label properties as keyed suffixes (backward compatible)
        sb.append("|LABEL_FONT=").append(safe(layer.getLabelFontFamily()))
          .append("|LABEL_SIZE=").append(layer.getLabelFontSize())
          .append("|LABEL_BOLD=").append(layer.isLabelBold())
          .append("|LABEL_ITALIC=").append(layer.isLabelItalic())
          .append("|LABEL_UNDERLINE=").append(layer.isLabelUnderline())
          .append("|LABEL_COLOR=").append(colorToText(layer.getLabelColor()))
          .append("|LABEL_HALO=").append(layer.isLabelHaloEnabled())
          .append("|LABEL_HALO_COLOR=").append(colorToText(layer.getLabelHaloColor()))
          .append("|LABEL_HALO_WIDTH=").append(layer.getLabelHaloWidth())
          .append("|LABEL_OFFSET_X=").append(layer.getLabelOffsetX())
          .append("|LABEL_OFFSET_Y=").append(layer.getLabelOffsetY())
          .append("|LABEL_PLACEMENT=").append(safe(layer.getLabelPlacement()))
          .append("|LABEL_PLACEMENT_MODE=").append(layer.getLabelPlacementMode().name())
          .append("|LABEL_PRIORITY=").append(layer.getLabelPriority())
          .append("|LABEL_COLLISION_AVOID=").append(layer.isLabelCollisionAvoid())
          .append("|LABEL_BG=").append(layer.isLabelBackgroundEnabled())
          .append("|LABEL_BG_COLOR=").append(colorToText(layer.getLabelBackgroundColor()))
          .append("|LABEL_MIN_SCALE=").append((int) layer.getLabelMinScale())
          .append("|LABEL_MAX_SCALE=").append((int) layer.getLabelMaxScale());

        if (layer instanceof GpxLayer) {
            GpxLayer gpx = (GpxLayer) layer;
            sb.append("|").append("GPX_KIND=").append(safe(gpx.getContentKind().name()));
        }

        if (layer instanceof RasterLayer) {
            RasterLayer raster = (RasterLayer) layer;
            sb.append("|").append(raster.getOpacity())
              .append("|").append(raster.isGrayscale())
              .append("|").append(raster.isAutoContrast())
              .append("|").append(raster.getRedBand())
              .append("|").append(raster.getGreenBand())
              .append("|").append(raster.getBlueBand())
              .append("|").append(safe(raster.getRasterMode()));
            if (raster.isDerivedLayer()) {
                sb.append("|").append("DERIVED_OP=").append(safe(raster.getDerivedOperation()))
                  .append("|").append("DERIVED_ARGS=").append(safe(raster.getDerivedParameters()));
            }
        } else {
            sb.append("|").append(layer.getOpacity());
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

        if (layer.isInGroup()) {
            sb.append("|").append("GROUP=").append(safe(layer.getGroupName()));
        }
        if (layer.getSourceName() != null && !layer.getSourceName().isBlank()) {
            sb.append("|").append("SOURCE_NAME=").append(safe(layer.getSourceName()));
        }
        if (CadLayerSupport.isCadLayer(layer) || layer.hasCadPlacementAdjustment()) {
            sb.append("|").append("CAD_SHIFT_X=").append(layer.getCadOffsetX());
            sb.append("|").append("CAD_SHIFT_Y=").append(layer.getCadOffsetY());
            sb.append("|").append("CAD_SCALE=").append(layer.getCadScale());
            sb.append("|").append("CAD_ROT=").append(layer.getCadRotationDegrees());
            sb.append("|").append("CAD_GEOREF_METHOD=").append(safe(layer.getCadGeoreferenceMethod()));
            sb.append("|").append("CAD_GEOREF_M00=").append(layer.getCadGeorefM00());
            sb.append("|").append("CAD_GEOREF_M01=").append(layer.getCadGeorefM01());
            sb.append("|").append("CAD_GEOREF_M02=").append(layer.getCadGeorefM02());
            sb.append("|").append("CAD_GEOREF_M10=").append(layer.getCadGeorefM10());
            sb.append("|").append("CAD_GEOREF_M11=").append(layer.getCadGeorefM11());
            sb.append("|").append("CAD_GEOREF_M12=").append(layer.getCadGeorefM12());
            sb.append("|").append("CAD_GEOREF_RES_MEAN=").append(layer.getCadGeorefResidualMean());
            sb.append("|").append("CAD_GEOREF_RES_MAX=").append(layer.getCadGeorefResidualMax());
            sb.append("|").append("CAD_GEOREF_REF_COUNT=").append(layer.getCadGeorefReferenceCount());
            sb.append("|").append("CAD_GEOREF_CHECK_COUNT=").append(layer.getCadGeorefCheckCount());
            sb.append("|").append("CAD_HIDDEN_LAYERS=").append(safe(layer.getCadHiddenInternalLayersEncoded()));
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

    private static boolean persistVectorLayers(File projectFile, boolean showDialogs) {
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
            } else if (TopographyWorkflowSupport.isTransientTopographyVector(layer)) {
                File autoFile = buildAutoVectorFile(projectFile, layer, ".shp");
                ok = ExportVectorLayerAction.saveLayerDataToFile(
                        layer,
                        data,
                        autoFile,
                        CatgisDesktopApp.getMainFrameSafe(),
                        false
                );
            } else {
                if (showDialogs) {
                    JOptionPane.showMessageDialog(
                            CatgisDesktopApp.getMainFrameSafe(),
                            "La capa \"" + layer.getName() + "\" no tiene archivo asociado.\nElegí dónde guardarla antes de guardar el proyecto.",
                            "Guardar capa vectorial",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
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

    private static File buildAutoVectorFile(File projectFile, Layer layer, String extension) {
        File projectDir = projectFile != null && projectFile.getParentFile() != null
                ? projectFile.getParentFile()
                : new File(".");
        String projectBase = projectFile != null ? stripExtension(projectFile.getName()) : "proyecto";
        File layersDir = new File(projectDir, sanitizeFileName(projectBase) + "_capas");
        if (!layersDir.exists()) {
            layersDir.mkdirs();
        }
        String layerName = layer != null && layer.getName() != null && !layer.getName().isBlank()
                ? layer.getName()
                : "capa_vectorial";
        return new File(layersDir, sanitizeFileName(layerName) + extension);
    }

    private static String sanitizeFileName(String value) {
        String text = value != null ? value.trim() : "";
        if (text.isBlank()) {
            text = "archivo";
        }
        text = text.replaceAll("[\\\\/:*?\"<>|]", "_");
        text = text.replaceAll("\\s+", "_");
        text = text.replaceAll("[^A-Za-z0-9._-]", "_");
        text = text.replaceAll("_+", "_");
        text = text.replaceAll("^[._-]+|[._-]+$", "");
        if (text.isBlank()) {
            text = "archivo";
        }
        return text.toLowerCase(Locale.ROOT);
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace("|", " ");
    }

    private static String safeOrPlaceholder(String value) {
        String normalized = safe(value);
        return normalized.isBlank() ? "-" : normalized;
    }
}
