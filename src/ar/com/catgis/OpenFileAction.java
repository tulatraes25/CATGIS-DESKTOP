package ar.com.catgis;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        return openSelectedFiles(files, requestedFormat, parent, true);
    }

    static boolean openSelectedFiles(File[] files, String requestedFormat, Component parent, boolean showDialogs) {
        if (files == null || files.length == 0) {
            return false;
        }

        boolean anyLoaded = false;
        for (File file : files) {
            if (file == null) {
                continue;
            }
            anyLoaded |= openSelectedFile(file, requestedFormat, parent, showDialogs);
        }
        return anyLoaded;
    }

    public static boolean openSelectedFile(File file, String requestedFormat, Component parent) {
        return openSelectedFile(file, requestedFormat, parent, true);
    }

    static boolean openSelectedFile(File file, String requestedFormat, Component parent, boolean showDialogs) {
        if (file == null) {
            return false;
        }

        try {
            String lowerName = file.getName().toLowerCase();
            ShapefileData data = null;
            DwgImportSupport.ResolvedCadReference resolvedCad = null;
            String sourceCRS = "";
            String vectorDialogMessage = null;
            File layerStorageFile = file;
            String layerDisplayName = file.getName();

            if (lowerName.endsWith(".gpkg")) {
                return GeoPackageDataSourceAction.openGeoPackageDataSource(file, parent);
            } else if (lowerName.endsWith(".gpx")) {
                return openGpxFile(file, parent, showDialogs);
            } else if (lowerName.endsWith(".shp")) {
                data = ShapefileLoader.load(file);
                sourceCRS = ShapefileLoader.getCRSCode(file);
            } else if (lowerName.endsWith(".geojson") || lowerName.endsWith(".json")) {
                data = GeoJsonLoader.load(file);
                sourceCRS = "EPSG:4326";
            } else if (lowerName.endsWith(".kml") || lowerName.endsWith(".kmz")) {
                data = KmlLoader.load(file);
                sourceCRS = "EPSG:4326";
            } else if (lowerName.endsWith(".dxf")) {
                data = DxfLoader.load(file);
            } else if (lowerName.endsWith(".dwg")) {
                resolvedCad = DwgImportSupport.resolveDwgReference(file, parent, showDialogs);
                if (resolvedCad == null || resolvedCad.dxfFile() == null) {
                    return false;
                }
                ShapefileData dxfData = DxfLoader.load(resolvedCad.dxfFile());
                data = new ShapefileData(
                        dxfData.getFeatures(),
                        dxfData.getEnvelope(),
                        resolvedCad.resolutionMessage(),
                        dxfData.getFeatureCount(),
                        dxfData.getMessage(),
                        dxfData.getSchema()
                );
                layerStorageFile = file;
                layerDisplayName = file.getName();
            }

            if (data != null) {
                Layer layer = new VectorLayer(layerDisplayName, layerStorageFile.getAbsolutePath());
                layer.setVisible(true);
                if (CadLayerSupport.isCadFile(layerStorageFile)) {
                    CadCrsAssignmentDialog.Result cadCrsResult = CadCrsAssignmentDialog.chooseForImport(parent, layerStorageFile, sourceCRS);
                    if (!cadCrsResult.approved()) {
                        return false;
                    }
                    sourceCRS = cadCrsResult.sourceCrs() != null ? cadCrsResult.sourceCrs() : "";
                }
                layer.setSourceCRS(sourceCRS);
                configureImportedLayerDefaults(layer, data, lowerName);
                if (CadLayerSupport.isCadLayer(layer)) {
                    vectorDialogMessage = buildCadLoadMessage(layer, file, resolvedCad);
                }
                addVectorLayer(layer, data);
                if (CatgisDesktopApp.statusBar != null) {
                    CatgisDesktopApp.statusBar.setMessage("Capa agregada: " + layer.getName());
                }
                if (showDialogs) {
                    JOptionPane.showMessageDialog(
                            parent,
                            vectorDialogMessage != null ? vectorDialogMessage : buildVectorLoadMessage(layer),
                            "Cargar datos",
                            layer.getSourceCRS().isBlank() ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE
                    );
                }
                return true;
            }

            if (isRasterOrImage(lowerName)) {
                return openRasterFileInternal(file, parent, false, showDialogs);
            }
            String suffix = (requestedFormat != null && !requestedFormat.isBlank())
                    ? "\nFormato seleccionado: " + requestedFormat
                    : "";
            if (showDialogs) {
                JOptionPane.showMessageDialog(
                        parent,
                        "No se pudo cargar \"" + file.getName() + "\".\n\n"
                                + "CATGIS no reconoce ese formato como capa soportada en este flujo."
                                + suffix
                                + "\n\nFormatos vectoriales principales: SHP, GeoJSON, GPKG, KML, KMZ, DXF, DWG, GPX."
                                + "\nFormatos raster principales: GeoTIFF, TIFF, JPG, PNG, BMP, GIF, IMG, ECW.",
                        "Cargar datos",
                        JOptionPane.WARNING_MESSAGE
                );
            }
            return false;

        } catch (Exception ex) {
            ex.printStackTrace();
            if (showDialogs) {
                JOptionPane.showMessageDialog(
                        parent,
                        "No se pudo agregar la capa \"" + file.getName() + "\".\n\n"
                                + (ex.getMessage() != null && !ex.getMessage().isBlank()
                                ? ex.getMessage()
                                : ex.getClass().getSimpleName()),
                        "Cargar datos",
                        JOptionPane.ERROR_MESSAGE
                );
            }
            return false;
        }
    }

    public static boolean openDroppedFiles(File[] files, Component parent) {
        if (files == null || files.length == 0) {
            return false;
        }

        List<File> projectFiles = new ArrayList<>();
        List<File> layerFiles = new ArrayList<>();
        for (File file : files) {
            if (file == null) {
                continue;
            }
            String lowerName = file.getName().toLowerCase();
            if (lowerName.endsWith(".catgis")) {
                projectFiles.add(file);
            } else {
                layerFiles.add(file);
            }
        }

        if (!projectFiles.isEmpty() && !layerFiles.isEmpty()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "No mezcles proyectos .catgis con capas en el mismo arrastre.\n\nSolta el proyecto por separado o arrastra solo capas.",
                    "Arrastrar y soltar",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        if (!projectFiles.isEmpty()) {
            if (projectFiles.size() > 1) {
                JOptionPane.showMessageDialog(
                        parent,
                        "Arrastra un solo proyecto .catgis por vez.",
                        "Arrastrar y soltar",
                        JOptionPane.WARNING_MESSAGE
                );
                return false;
            }
            if (!CatgisDesktopApp.confirmProjectContinuation("abrir otro proyecto")) {
                return false;
            }
            return LoadProjectAction.loadProjectFile(projectFiles.get(0));
        }

        int loadedCount = 0;
        List<String> failedFiles = new ArrayList<>();
        for (File file : layerFiles) {
            boolean loaded = openSelectedFile(file, "Drag & Drop", parent, false);
            if (loaded) {
                loadedCount++;
            } else {
                failedFiles.add(file.getName());
            }
        }

        if (loadedCount > 0 && CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(
                    loadedCount == 1
                            ? "Archivo agregado por arrastre."
                            : loadedCount + " archivos agregados por arrastre."
            );
        }

        if (!failedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "No se pudieron cargar estos archivos:\n- " + String.join("\n- ", failedFiles),
                    "Arrastrar y soltar",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        return loadedCount > 0;
    }

    private static boolean openGpxFile(File file, Component parent, boolean showDialogs) {
        try {
            ensureProject();
            GpxImportResult result = GpxLoader.load(file);
            if (!result.hasAnyData()) {
                if (showDialogs) {
                    JOptionPane.showMessageDialog(
                            parent,
                            "El archivo GPX no contiene waypoints, tracks ni routes utilizables: " + file.getName(),
                            "Cargar datos",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
                return false;
            }

            List<String> loadedSummaries = new ArrayList<>();
            addGpxLayer(file, result, GpxLayer.ContentKind.WAYPOINTS, loadedSummaries);
            addGpxLayer(file, result, GpxLayer.ContentKind.TRACKS, loadedSummaries);
            addGpxLayer(file, result, GpxLayer.ContentKind.ROUTES, loadedSummaries);

            if (CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage("GPX agregado: " + file.getName());
            }
            if (showDialogs) {
                JOptionPane.showMessageDialog(
                        parent,
                        buildGpxLoadMessage(file.getName(), loadedSummaries),
                        "Cargar datos",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            if (showDialogs) {
                JOptionPane.showMessageDialog(
                        parent,
                        "Error al cargar GPX: " + ex.getMessage(),
                        "Cargar datos",
                        JOptionPane.ERROR_MESSAGE
                );
            }
            return false;
        }
    }

    private static void addGpxLayer(File file,
                                    GpxImportResult result,
                                    GpxLayer.ContentKind kind,
                                    List<String> loadedSummaries) {
        ShapefileData data = result.get(kind);
        if (!GpxImportResult.hasFeatures(data)) {
            return;
        }

        String baseName = stripExtension(file.getName());
        GpxLayer layer = new GpxLayer(baseName + " | " + kind.getLabel(), file.getAbsolutePath(), kind);
        layer.setVisible(true);
        if (kind == GpxLayer.ContentKind.WAYPOINTS) {
            layer.setPointSymbolStyle(Layer.PointSymbolStyle.PIN);
            layer.setPointSize(14);
        } else if (kind == GpxLayer.ContentKind.TRACKS) {
            layer.setLineColor(new java.awt.Color(16, 185, 129));
            layer.setLineWidth(2.2f);
        } else {
            layer.setLineColor(new java.awt.Color(245, 158, 11));
            layer.setLineWidth(2.0f);
            layer.setLineSymbolStyle(Layer.LineSymbolStyle.DASHED);
        }
        addVectorLayer(layer, data);
        loadedSummaries.add(kind.getLabel() + ": " + data.getFeatureCount());
    }

    private static void addVectorLayer(Layer layer, ShapefileData data) {
        if (layer == null || data == null) {
            return;
        }
        ensureProject();
        data = TopographyWorkflowSupport.projectVectorDataToCurrentProject(layer, data);
        layer.setSourceName(data.getSourceName());
        layer.setFeatureCount(data.getFeatureCount());
        VectorLayerUtils.populateFieldConfigs(layer, data.getSchema());
        CatgisDesktopApp.currentProject.addLayer(layer);
        CatgisDesktopApp.markProjectDirty();
        CatgisDesktopApp.layersPanel.addLayer(layer);
        CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(layer, data);
        CatgisDesktopApp.mapPanel.showOpenedFile(layer.getName());
    }

    private static void configureImportedLayerDefaults(Layer layer, ShapefileData data, String lowerName) {
        if (layer == null || data == null || lowerName == null) {
            return;
        }
        if (lowerName.endsWith(".dxf") || lowerName.endsWith(".dwg")) {
            layer.setLineColor(new Color(55, 65, 81));
            layer.setBorderColor(new Color(55, 65, 81));
            layer.setPointColor(new Color(55, 65, 81));
            layer.setFillColor(new Color(55, 65, 81, 0));
            layer.setLineWidth(1.4f);
            layer.setPointSize(4);
            if (hasMeaningfulAttribute(data, "text")) {
                layer.setLabelField("text");
                layer.setLabelsVisible(true);
            }
        }
    }

    private static boolean hasMeaningfulAttribute(ShapefileData data, String fieldName) {
        if (data == null || data.getFeatures() == null || fieldName == null || fieldName.isBlank()) {
            return false;
        }
        for (org.geotools.api.feature.simple.SimpleFeature feature : data.getFeatures()) {
            if (feature == null) {
                continue;
            }
            Object value = feature.getAttribute(fieldName);
            if (value != null && !String.valueOf(value).isBlank()) {
                return true;
            }
        }
        return false;
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
        return openRasterFileInternal(file, parent, true, true);
    }

    private static boolean openRasterFileInternal(File file, Component parent, boolean demMode, boolean showDialogs) {
        try {
            ensureProject();
            String projectCRS = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
            LocalRasterData rasterData = RasterImageLoader.loadPreview(file, projectCRS, null);
            RasterLayer layer = new RasterLayer(file.getName(), file.getAbsolutePath());
            layer.setVisible(true);
            layer.setSourceName(demMode ? I18n.t("DEM local") : file.getName());
            layer.setFeatureCount(1);
            layer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(rasterData, projectCRS));
            layer.setRasterMode(rasterData.getRasterMode());
            CatgisDesktopApp.currentProject.addLayer(layer);
            CatgisDesktopApp.markProjectDirty();
            CatgisDesktopApp.layersPanel.addLayer(layer);
            CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(layer, rasterData);
            CatgisDesktopApp.mapPanel.showOpenedFile(layer.getName());
            CatgisDesktopApp.mapPanel.zoomToLayer(layer);
            if (CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage(
                        (demMode ? I18n.t("DEM local agregado: ") : "Raster agregado en vista rapida: ") + layer.getName()
                );
            }

            StringBuilder msg = new StringBuilder();
            msg.append(demMode ? I18n.t("DEM local agregado correctamente: ") : "Raster agregado correctamente: ")
                    .append(layer.getName());
            msg.append("\n").append(I18n.t("Bandas:")).append(" ").append(rasterData.getBandCount());
            if (!layer.getSourceCRS().isBlank()) {
                msg.append("\nCRS operativo: ").append(layer.getSourceCRS());
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
            if (layer.getSourceCRS().isBlank()) {
                msg.append("\n\n").append(I18n.t("Recomendacion: defini el CRS real de la capa desde el panel de capas para ubicarla correctamente con otros datos."));
            }

            if (showDialogs) {
                JOptionPane.showMessageDialog(
                        parent,
                        msg.toString(),
                        demMode ? I18n.t("Cargar datos DEM") : "Cargar datos",
                        layer.getSourceCRS().isBlank() ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE
                );
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            if (showDialogs) {
                JOptionPane.showMessageDialog(
                        parent,
                        (demMode ? I18n.t("Error al cargar DEM: ") : "Error al agregar capa: ") + ex.getMessage(),
                        demMode ? I18n.t("Cargar datos DEM") : "Cargar datos",
                        JOptionPane.ERROR_MESSAGE
                );
            }
            return false;
        }
    }

    private static String buildVectorLoadMessage(Layer layer) {
        StringBuilder msg = new StringBuilder();
        msg.append("Capa agregada correctamente: ").append(layer.getName());

        if (!layer.getSourceCRS().isBlank()) {
            msg.append("\nCRS: ").append(CadLayerSupport.formatSourceCrsLabel(layer.getSourceCRS()));
        } else {
            msg.append("\nCRS: no definido");
            msg.append("\n\nRecomendacion: defini el CRS real de la capa desde el panel de capas para ubicarla correctamente en el proyecto.");
        }

        return msg.toString();
    }

    private static String buildCadLoadMessage(Layer layer, File cadFile, DwgImportSupport.ResolvedCadReference resolvedCad) {
        StringBuilder msg = new StringBuilder();
        boolean dwg = cadFile != null && cadFile.getName().toLowerCase().endsWith(".dwg");
        msg.append(dwg
                ? "Referencia CAD cargada desde DWG asistido: " + cadFile.getName()
                : "Referencia CAD cargada desde DXF: " + (cadFile != null ? cadFile.getName() : layer.getName()));
        String mode;
        if (!dwg || resolvedCad == null) {
            mode = "Lectura CAD DXF directa";
        } else {
            mode = resolvedCad.sidecar()
                    ? "DXF gemelo detectado automaticamente"
                    : resolvedCad.autoConverted()
                    ? "Conversion automatica DWG -> DXF"
                    : "DXF convertido seleccionado por el usuario";
        }
        msg.append("\nModo de carga: ").append(mode);
        if (resolvedCad != null && resolvedCad.dxfFile() != null) {
            msg.append("\nDXF usado: ").append(resolvedCad.dxfFile().getAbsolutePath());
        }
        if (resolvedCad != null && resolvedCad.converterFile() != null) {
            msg.append("\nConvertidor usado: ").append(resolvedCad.converterFile().getAbsolutePath());
        }
        if (layer.getSourceCRS() != null && !layer.getSourceCRS().isBlank()) {
            msg.append("\nCRS asignado: ").append(CadLayerSupport.formatSourceCrsLabel(layer.getSourceCRS()));
        } else {
            msg.append("\nCRS: no definido");
            msg.append("\n\nRecomendacion: defini el CRS real de la capa desde el panel de capas si el dibujo ya esta en coordenadas GIS.");
        }
        msg.append("\nGeorreferenciacion por puntos: disponible desde propiedades o menu CAD.");
        msg.append("\nAjuste fino CAD: disponible despues del CRS y de la georreferenciacion.");
        return msg.toString();
    }

    private static String buildGpxLoadMessage(String fileName, List<String> loadedSummaries) {
        StringBuilder msg = new StringBuilder();
        msg.append("GPX agregado correctamente: ").append(fileName);
        for (String summary : loadedSummaries) {
            msg.append("\n").append("- ").append(summary);
        }
        msg.append("\nCRS: EPSG:4326");
        return msg.toString();
    }

    private static String stripExtension(String name) {
        if (name == null || name.isBlank()) {
            return "GPX";
        }
        int idx = name.lastIndexOf('.');
        return idx > 0 ? name.substring(0, idx) : name;
    }
}
