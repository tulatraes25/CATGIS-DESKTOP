package ar.com.catgis;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.renderer.PolygonSymbolRenderer;
import ar.com.catgis.renderer.LineSymbolRenderer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.ProRasterDerivedService;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.renderer.PolygonSymbolRenderer;
import ar.com.catgis.renderer.LineSymbolRenderer;

import ar.com.catgis.climate.GribLoader;
import ar.com.catgis.climate.NetCdfLoader;
import javax.swing.AbstractAction;
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
            } else if (lowerName.endsWith(".spatialite") || lowerName.endsWith(".sl3") || lowerName.endsWith(".sl4") || lowerName.endsWith(".db")) {
                // SpatiaLite: validate file and load first available table
                ValidationResult vr = SpatiaLiteLoader.validateFile(file);
                if (!vr.isValid()) {
                    NotificationManager.warn(parent, "SpatiaLite — archivo inválido", vr.message());
                    return false;
                }
                SpatiaLiteConnectionInfo connInfo = new SpatiaLiteConnectionInfo();
                connInfo.setFilePath(file.getAbsolutePath());
                try {
                    List<SpatiaLiteFeatureTypeInfo> tables = SpatiaLiteLoader.listFeatureTypes(connInfo);
                    if (tables.isEmpty()) {
                        NotificationManager.warn(parent, "SpatiaLite",
                                "No se encontraron tablas en " + file.getName()
                                + ".\nSpatiaLite requiere mod_spatialite para leer datos espaciales.");
                        return false;
                    }
                    // Load first available table
                    SpatiaLiteLayer slLayer = new SpatiaLiteLayer(file.getName(), file.getAbsolutePath());
                    slLayer.setTableName(tables.get(0).getTableName());
                    slLayer.setVisible(true);
                    ShapefileData slData = SpatiaLiteLoader.loadLayerData(slLayer);
                    if (slData != null) {
                        data = slData;
                        layerDisplayName = file.getName();
                    }
                } catch (Exception ex) {
                    AppErrorSupport.showErrorDialog(parent, "SpatiaLite", "No se pudo cargar el archivo.", ex);
                    return false;
                }
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
            } else if (lowerName.endsWith(".fgb")) {
                ValidationResult vr = FlatGeobufLoader.validateFile(file);
                if (!vr.isValid()) {
                    NotificationManager.warn(parent, "FlatGeobuf — archivo inválido", vr.message());
                    return false;
                }
                data = FlatGeobufLoader.load(file);
                layerDisplayName = file.getName();
            } else if (lowerName.endsWith(".pmtiles")) {
                // PMTiles: read metadata and show info (tiles are not directly displayable as vector/raster)
                try {
                    var header = PmtilesReader.readHeader(file);
                    var entries = PmtilesReader.readDirectory(file, header);
                    String info = PmtilesReader.getStats(file);
                    NotificationManager.info(parent, "PMTiles Info", info);
                    return false; // PMTiles is a tile archive, not directly loadable as a layer
                } catch (Exception ex) {
                    AppErrorSupport.showErrorDialog(parent, "PMTiles", "No se pudo leer el archivo PMTiles.", ex);
                    return false;
                }
            } else if (lowerName.endsWith(".parquet") || lowerName.endsWith(".geoparquet")) {
                // GeoParquet: read metadata and show info
                try {
                    var meta = GeoParquetReader.readMetadata(file);
                    String info = GeoParquetReader.getSummary(file);
                    NotificationManager.info(parent, "GeoParquet Info", info);
                    return false; // GeoParquet requires parquet-hadoop for full support
                } catch (Exception ex) {
                    AppErrorSupport.showErrorDialog(parent, "GeoParquet", "No se pudo leer el archivo GeoParquet.", ex);
                    return false;
                }
            }

            if (data != null) {
                Layer layer = new VectorLayer(layerDisplayName, layerStorageFile.getAbsolutePath());
                layer.setVisible(true);
                if (CadLayerSupport.isCadFile(layerStorageFile)) {
                    CadCrsAssignmentDialog.Result cadCrsResult = CadCrsAssignmentDialog.chooseForImport(parent, layerStorageFile, sourceCRS);
                    if (cadCrsResult.selectorRequested()) {
                        String chosenCode = CRSSelectorDialog.chooseBlocking(
                                CatgisDesktopApp.getMainFrameSafe(),
                                "Seleccionar CRS para CAD",
                                cadCrsResult.sourceCrs()
                        );
                        if (chosenCode == null || chosenCode.isBlank()) {
                            return false;
                        }
                        sourceCRS = chosenCode;
                    } else if (!cadCrsResult.approved()) {
                        return false;
                    } else {
                        sourceCRS = cadCrsResult.sourceCrs() != null ? cadCrsResult.sourceCrs() : "";
                    }
                }
                layer.setSourceCRS(sourceCRS);
                configureImportedLayerDefaults(layer, data, lowerName);
                if (CadLayerSupport.isCadLayer(layer)) {
                    vectorDialogMessage = buildCadLoadMessage(layer, file, resolvedCad);
                }
                addVectorLayer(layer, data);
                if (CatgisDesktopApp.statusBar != null) {
                    AppContext.setStatusMessage("Capa agregada: " + layer.getName());
                }
                if (showDialogs) {
                    NotificationManager.warn(
                            parent,
                            "Cargar datos",
                            vectorDialogMessage != null ? vectorDialogMessage : buildVectorLoadMessage(layer)
                    );
                }
                return true;
            }

            // Climate data files
            if (NetCdfLoader.isNetCdfFile(lowerName)) {
                RasterLayer climateLayer = NetCdfLoader.loadNetCdfFile(file, parent);
                if (climateLayer != null) {
                    if (CatgisDesktopApp.statusBar != null) {
                        AppContext.setStatusMessage("Datos climáticos NetCDF agregados: " + climateLayer.getName());
                    }
                    return true;
                }
                return false;
            }

            if (NetCdfLoader.isGribFile(lowerName)) {
                RasterLayer gribLayer = GribLoader.loadGribFile(file, parent);
                if (gribLayer != null) {
                    if (CatgisDesktopApp.statusBar != null) {
                        AppContext.setStatusMessage("Datos GRIB agregados: " + gribLayer.getName());
                    }
                    return true;
                }
                return false;
            }

            if (isRasterOrImage(lowerName)) {
                return openRasterFileInternal(file, parent, false, showDialogs);
            }
            String suffix = (requestedFormat != null && !requestedFormat.isBlank())
                    ? "\nFormato seleccionado: " + requestedFormat
                    : "";
            if (showDialogs) {
                NotificationManager.warn(
                        parent,
                        "Cargar datos",
                        "No se pudo cargar \"" + file.getName() + "\".\n\n"
                                + "CATGIS no reconoce ese formato como capa soportada en este flujo."
                                + suffix
                                + "\n\nFormatos vectoriales principales: SHP, GeoJSON, GPKG, KML, KMZ, DXF, DWG, GPX."
                                + "\nFormatos raster principales: GeoTIFF, TIFF, JPG, PNG, BMP, GIF, IMG, ECW."
                );
            }
            return false;

        } catch (Exception ex) {
            return handleOpenFailure(
                    file,
                    parent,
                    showDialogs,
                    "No se pudo agregar la capa \"" + file.getName() + "\".",
                    "Cargar datos",
                    ex
            );
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
            NotificationManager.warn(
                    parent,
                    "Arrastrar y soltar",
                    "No mezcles proyectos .catgis con capas en el mismo arrastre.\n\nSolta el proyecto por separado o arrastra solo capas."
            );
            return false;
        }

        if (!projectFiles.isEmpty()) {
            if (projectFiles.size() > 1) {
                NotificationManager.warn(
                        parent,
                        "Arrastrar y soltar",
                        "Arrastra un solo proyecto .catgis por vez."
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
            AppContext.setStatusMessage(
                    loadedCount == 1
                            ? "Archivo agregado por arrastre."
                            : loadedCount + " archivos agregados por arrastre."
            );
        }

        if (!failedFiles.isEmpty()) {
            NotificationManager.warn(
                    parent,
                    "Arrastrar y soltar",
                    "No se pudieron cargar estos archivos:\n- " + String.join("\n- ", failedFiles)
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
                    NotificationManager.warn(
                            parent,
                            "Cargar datos",
                            "El archivo GPX no contiene waypoints, tracks ni routes utilizables: " + file.getName()
                    );
                }
                return false;
            }

            List<String> loadedSummaries = new ArrayList<>();
            addGpxLayer(file, result, GpxLayer.ContentKind.WAYPOINTS, loadedSummaries);
            addGpxLayer(file, result, GpxLayer.ContentKind.TRACKS, loadedSummaries);
            addGpxLayer(file, result, GpxLayer.ContentKind.ROUTES, loadedSummaries);

            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage("GPX agregado: " + file.getName());
            }
            if (showDialogs) {
                NotificationManager.info(
                        parent,
                        "Cargar datos",
                        buildGpxLoadMessage(file.getName(), loadedSummaries)
                );
            }
            return true;
        } catch (Exception ex) {
            return handleOpenFailure(file, parent, showDialogs, "Error al cargar GPX.", "Cargar datos", ex);
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
        AppContext.project().addLayer(layer);
        CatgisDesktopApp.markProjectDirty();
        AppContext.addLayer(layer);
        AppContext.mapPanel().addOrUpdateShapefileLayer(layer, data);
        AppContext.mapPanel().showOpenedFile(layer.getName());
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
        if (AppContext.project() == null) {
            AppContext.setCurrentProject(new Project("Proyecto actual"));
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

    static boolean openProRasterEntry(ProDatasetOpenService.Entry entry, Component parent, boolean showDialogs) {
        try {
            PreparedProRaster prepared = prepareProRasterEntry(entry, ProJobMonitor.noop());
            RasterLayer layer = commitPreparedProRaster(prepared);

            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage(
                        "Variable Pro agregada: " + layer.getName() + " | " + entry.methodologyLabel()
                );
            }
            if (showDialogs) {
                if (layer.getSourceCRS().isBlank()) {
                    NotificationManager.warn(parent, "Abrir dataset", buildProRasterLoadMessage(layer, prepared));
                } else {
                    NotificationManager.info(parent, "Abrir dataset", buildProRasterLoadMessage(layer, prepared));
                }
            }
            return true;
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo abrir la variable Pro seleccionada.", ex);
            if (showDialogs) {
                AppErrorSupport.showErrorDialog(
                        parent,
                        "Abrir dataset",
                        "No se pudo abrir la variable Pro seleccionada.",
                        ex
                );
            }
            return false;
        }
    }

    static PreparedProRaster prepareProRasterEntry(ProDatasetOpenService.Entry entry) throws Exception {
        return prepareProRasterEntry(entry, ProJobMonitor.noop());
    }

    static PreparedProRaster prepareProRasterEntry(ProDatasetOpenService.Entry entry, ProJobMonitor monitor) throws Exception {
        ProJobMonitor effectiveMonitor = monitor != null ? monitor : ProJobMonitor.noop();
        effectiveMonitor.checkCanceled();
        if (entry == null || (entry.rasterFile() == null && !entry.openable())) {
            throw new IllegalArgumentException("No hay una entrada Pro valida para preparar.");
        }
        effectiveMonitor.report("Validando variable Pro: " + entry.variableLabel());
        String projectCRS = AppContext.project() != null ? AppContext.project().getProjectCRS() : "";
        ProRasterMaterializationService.MaterializedRaster materialized = null;
        File rasterFile = entry.rasterFile();
        if ((rasterFile == null || !rasterFile.exists()) && entry.openable()) {
            effectiveMonitor.report("Materializando variable Pro: " + entry.variableLabel());
            materialized = ProRasterMaterializationService.materialize(entry, effectiveMonitor);
            rasterFile = materialized.rasterFile();
        }
        effectiveMonitor.checkCanceled();
        if (rasterFile == null || !rasterFile.exists()) {
            throw new IllegalStateException("No se encontro el raster Pro materializado para abrir.");
        }
        effectiveMonitor.report("Cargando vista rapida Pro: " + entry.variableLabel());
        LocalRasterData rasterData = RasterImageLoader.loadPreview(rasterFile, projectCRS, null);
        effectiveMonitor.checkCanceled();
        effectiveMonitor.report("Variable Pro preparada: " + entry.variableLabel());
        return new PreparedProRaster(entry, materialized, rasterFile, rasterData, projectCRS);
    }

    static RasterLayer commitPreparedProRaster(PreparedProRaster prepared) {
        if (prepared == null || prepared.entry() == null || prepared.rasterFile() == null || prepared.rasterData() == null) {
            throw new IllegalArgumentException("No hay una apertura Pro preparada para incorporar al proyecto.");
        }
        ensureProject();
        ProDatasetOpenService.Entry entry = prepared.entry();
        LocalRasterData rasterData = prepared.rasterData();
        RasterLayer layer = new RasterLayer(entry.layerName(), prepared.rasterFile().getAbsolutePath());
        layer.setVisible(true);
        layer.setSourceName(entry.dataset() != null && entry.dataset().getProvider() != null && !entry.dataset().getProvider().isBlank()
                ? entry.dataset().getProvider()
                : "CATGIS");
        layer.setFeatureCount(1);
        layer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(rasterData, prepared.projectCRS()));
        layer.setRasterMode(rasterData.getRasterMode());
        applyProMetadataToRasterLayer(layer, entry, prepared.materialized());

        AppContext.project().addLayer(layer);
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.layersPanel != null) {
            AppContext.addLayer(layer);
        }
        if (AppContext.mapPanel() != null) {
            AppContext.mapPanel().addOrUpdateRasterLayer(layer, rasterData);
            AppContext.mapPanel().showOpenedFile(layer.getName());
            AppContext.mapPanel().zoomToLayer(layer);
        }
        return layer;
    }

    private static boolean openRasterFileInternal(File file, Component parent, boolean demMode, boolean showDialogs) {
        try {
            ensureProject();
            String projectCRS = AppContext.project() != null ? AppContext.project().getProjectCRS() : "";
            LocalRasterData rasterData = RasterImageLoader.loadPreview(file, projectCRS, null);
            RasterLayer layer = new RasterLayer(file.getName(), file.getAbsolutePath());
            layer.setVisible(true);
            layer.setSourceName(demMode ? I18n.t("DEM local") : file.getName());
            layer.setFeatureCount(1);
            layer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(rasterData, projectCRS));
            layer.setRasterMode(rasterData.getRasterMode());
            AppContext.project().addLayer(layer);
            CatgisDesktopApp.markProjectDirty();
            AppContext.addLayer(layer);
            AppContext.mapPanel().addOrUpdateRasterLayer(layer, rasterData);
            AppContext.mapPanel().showOpenedFile(layer.getName());
            AppContext.mapPanel().zoomToLayer(layer);
            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage(
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
                String loadTitle = demMode ? I18n.t("Cargar datos DEM") : "Cargar datos";
                if (layer.getSourceCRS().isBlank()) {
                    NotificationManager.warn(parent, loadTitle, msg.toString());
                } else {
                    NotificationManager.info(parent, loadTitle, msg.toString());
                }
            }
            return true;
        } catch (Exception ex) {
            return handleOpenFailure(
                    file,
                    parent,
                    showDialogs,
                    demMode ? I18n.t("Error al cargar DEM.") : "Error al agregar capa.",
                    demMode ? I18n.t("Cargar datos DEM") : "Cargar datos",
                    ex
            );
        }
    }

    private static boolean handleOpenFailure(File file,
                                             Component parent,
                                             boolean showDialogs,
                                             String intro,
                                             String title,
                                             Exception ex) {
        String context = file != null
                ? "Operacion de apertura fallida para " + file.getAbsolutePath()
                : "Operacion de apertura fallida";
        AppErrorSupport.logFailure(context, ex);
        if (showDialogs) {
            AppErrorSupport.showErrorDialog(parent, title, intro, ex);
        }
        return false;
    }

    private static void applyProMetadataToRasterLayer(RasterLayer layer,
                                                      ProDatasetOpenService.Entry entry,
                                                      ProRasterMaterializationService.MaterializedRaster materialized) {
        if (layer == null || entry == null) {
            return;
        }
        layer.setProDatasetRef(entry.datasetRef());
        layer.setProVariableName(entry.variableLabel());
        if (entry.dataset() != null) {
            layer.setProAcquisitionStart(entry.dataset().getAcquisitionStart());
        }
        layer.setProMaturityLevel(entry.maturity());
        File effectiveSidecar = materialized != null && materialized.sidecarFile() != null
                ? materialized.sidecarFile()
                : entry.sidecarFile();
        if (effectiveSidecar == null && entry.dataset() != null && entry.variable() != null) {
            effectiveSidecar = writeGeneratedProSidecar(layer, entry);
        }
        if (effectiveSidecar != null) {
            layer.setProMetadataSidecarPath(effectiveSidecar.getAbsolutePath());
        }
        if (materialized != null && materialized.jobRef() != null && !materialized.jobRef().isBlank()) {
            layer.setProJobRef(materialized.jobRef());
        }
    }

    private static File writeGeneratedProSidecar(RasterLayer layer, ProDatasetOpenService.Entry entry) {
        if (layer == null || entry == null || layer.getPath() == null || layer.getPath().isBlank()) {
            return null;
        }
        try {
            return ProMetadataSidecarSupport.write(
                    new File(layer.getPath()),
                    new ProMetadataSidecarSupport.Metadata(
                            copyDataset(entry.dataset()),
                            copyVariable(entry.variable()),
                            entry.qualityPreset(),
                            entry.flagsApplied() != null ? entry.flagsApplied() : List.of(),
                            entry.recipe(),
                            entry.maturity(),
                            null
                    )
            );
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo persistir el sidecar Pro para " + layer.getName() + ".", ex);
        }
    }

    private static ProDatasetDescriptor copyDataset(ProDatasetDescriptor source) {
        ProDatasetDescriptor copy = new ProDatasetDescriptor();
        if (source == null) {
            return copy;
        }
        copy.setDatasetId(source.getDatasetId());
        copy.setFamily(source.getFamily());
        copy.setProvider(source.getProvider());
        copy.setPlatform(source.getPlatform());
        copy.setInstrument(source.getInstrument());
        copy.setProcessingLevel(source.getProcessingLevel());
        copy.setAcquisitionStart(source.getAcquisitionStart());
        copy.setAcquisitionEnd(source.getAcquisitionEnd());
        return copy;
    }

    private static ProVariableDescriptor copyVariable(ProVariableDescriptor source) {
        ProVariableDescriptor copy = new ProVariableDescriptor();
        if (source == null) {
            return copy;
        }
        copy.setName(source.getName());
        copy.setLongName(source.getLongName());
        copy.setStandardName(source.getStandardName());
        copy.setUnits(source.getUnits());
        copy.setDimensions(source.getDimensions());
        copy.setNodata(source.getNodata());
        copy.setScaleFactor(source.getScaleFactor());
        copy.setAddOffset(source.getAddOffset());
        copy.setValidMin(source.getValidMin());
        copy.setValidMax(source.getValidMax());
        copy.setQaDescriptor(source.getQaDescriptor());
        copy.setBandFamily(source.getBandFamily());
        return copy;
    }

    private static String buildProRasterLoadMessage(RasterLayer layer, PreparedProRaster prepared) {
        ProDatasetOpenService.Entry entry = prepared.entry();
        LocalRasterData rasterData = prepared.rasterData();
        ProRasterMaterializationService.MaterializedRaster materialized = prepared.materialized();
        StringBuilder msg = new StringBuilder();
        msg.append("Variable Pro agregada correctamente: ").append(layer.getName());
        msg.append("\nVariable: ").append(entry.variableLabel());
        if (entry.datasetRef() != null && !entry.datasetRef().isBlank()) {
            msg.append("\nDataset: ").append(entry.datasetRef());
        }
        if (entry.dataset() != null && entry.dataset().getFamily() != null && !entry.dataset().getFamily().isBlank()) {
            msg.append("\nFamilia: ").append(entry.dataset().getFamily());
        }
        if (entry.dataset() != null && entry.dataset().getProvider() != null && !entry.dataset().getProvider().isBlank()) {
            msg.append("\nProveedor: ").append(entry.dataset().getProvider());
        }
        if (entry.dataset() != null && entry.dataset().getPlatform() != null && !entry.dataset().getPlatform().isBlank()) {
            msg.append("\nPlataforma: ").append(entry.dataset().getPlatform());
        }
        if (entry.dataset() != null && entry.dataset().getInstrument() != null && !entry.dataset().getInstrument().isBlank()) {
            msg.append("\nInstrumento: ").append(entry.dataset().getInstrument());
        }
        if (entry.dataset() != null && entry.dataset().getAcquisitionStart() != null && !entry.dataset().getAcquisitionStart().isBlank()) {
            msg.append("\nTiempo: ").append(entry.dataset().getAcquisitionStart());
        }
        if (entry.maturity() != null && !entry.maturity().isBlank()) {
            msg.append("\nMadurez: ").append(entry.maturity());
        }
        msg.append("\nClasificacion metodologica: ").append(entry.methodologyLabel());
        msg.append("\nPreset tematico: ").append(entry.presetLabel());
        if (entry.variable() != null && entry.variable().getQaDescriptor() != null && !entry.variable().getQaDescriptor().isBlank()) {
            msg.append("\nQA fuente-especifico: ").append(entry.variable().getQaDescriptor());
        }
        String expectedQa = ProRasterDerivedService.describeExpectedQa(new ProMetadataSidecarSupport.Metadata(
                entry.dataset(),
                entry.variable(),
                entry.qualityPreset(),
                entry.flagsApplied(),
                entry.recipe(),
                entry.maturity(),
                entry.sidecarFile()
        ));
        if (!expectedQa.isBlank()) {
            msg.append("\nQA prevista: ").append(expectedQa);
        }
        if (materialized != null) {
            msg.append("\nRaster gestionado: ").append(materialized.rasterFile().getAbsolutePath());
        }
        msg.append("\nBandas: ").append(rasterData.getBandCount());
        if (layer.getSourceCRS() != null && !layer.getSourceCRS().isBlank()) {
            msg.append("\nCRS operativo: ").append(layer.getSourceCRS());
        } else {
            msg.append("\nCRS: no definido");
        }
        File sidecar = materialized != null && materialized.sidecarFile() != null
                ? materialized.sidecarFile()
                : entry.sidecarFile();
        if (sidecar != null) {
            msg.append("\nSidecar Pro: ").append(sidecar.getAbsolutePath());
        }
        msg.append("\nAlcance: ").append(entry.methodologyDescription());
        msg.append("\n\nModo inicial: Vista rapida.");
        msg.append("\nPuedes pasar a zoom virtual o real desde clic derecho sobre la capa.");
        return msg.toString();
    }

    record PreparedProRaster(ProDatasetOpenService.Entry entry,
                             ProRasterMaterializationService.MaterializedRaster materialized,
                             File rasterFile,
                             LocalRasterData rasterData,
                             String projectCRS) {
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