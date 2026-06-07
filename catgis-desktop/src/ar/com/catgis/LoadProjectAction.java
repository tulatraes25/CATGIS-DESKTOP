package ar.com.catgis;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.online.OnlineWmsLayer;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.core.model.LayerGroup;
import ar.com.catgis.core.model.Layer;

import ar.com.catgis.core.model.Project;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

public class LoadProjectAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        loadProject();
    }

    public static void loadProject() {
        if (!CatgisDesktopApp.confirmProjectContinuation("abrir otro proyecto")) {
            return;
        }

        File initialDirectory = FileChooserSupport.resolveInitialDirectoryHint("project-open");
        JFileChooser chooser = initialDirectory != null ? new JFileChooser(initialDirectory) : new JFileChooser();
        chooser.setDialogTitle("Abrir proyecto CATGIS");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Proyectos CATGIS (*.catgis)", "catgis"));

        int result = chooser.showOpenDialog(CatgisDesktopApp.getMainFrameSafe());
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        FileChooserSupport.rememberSelection("project-open", chooser);
        FileChooserSupport.rememberSelection("project-save", chooser);
        loadProjectFile(file);
    }

    public static boolean loadProjectFile(File file) {
        return loadProjectFile(file, true);
    }

    static boolean loadProjectFile(File file, boolean showDialogs) {
        java.awt.Component owner = CatgisDesktopApp.getMainFrameSafe();

        if (file == null || !file.getName().toLowerCase().endsWith(".catgis")) {
            if (showDialogs) {
                JOptionPane.showMessageDialog(owner, "Seleccione un archivo .catgis valido.");
            }
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine();

            if (firstLine == null || !firstLine.trim().equals("CATGIS_PROJECT")) {
                if (showDialogs) {
                    JOptionPane.showMessageDialog(owner, "Archivo de proyecto invalido.");
                }
                return false;
            }

            Project loadedProject = new Project(stripExtension(file.getName()));
            loadedProject.setProjectFile(file);

            boolean viewLoaded = false;
            double savedViewMinX = 0;
            double savedViewMinY = 0;
            double savedZoomFactor = 1.0;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                if (line.startsWith("PROJECT_CRS|")) {
                    String[] crsParts = line.split("\\|", -1);
                    if (crsParts.length >= 2) {
                        String code = crsParts[1].trim();
                        if (!code.isEmpty()) {
                            loadedProject.setProjectCRS(code);
                        }
                    }
                    continue;
                }

                if (line.startsWith("PROJECT_META|")) {
                    String[] metaParts = line.split("\\|", -1);
                    if (metaParts.length >= 3) {
                        applyProjectMetadata(loadedProject, metaParts[1].trim(), metaParts[2].trim());
                    }
                    continue;
                }

                if (line.startsWith("PROJECT_LAYOUT_ITEM|")) {
                    String[] itemParts = line.split("\\|", 2);
                    if (itemParts.length >= 2) {
                        CatmapLayoutItem item = CatmapLayoutItem.decode(itemParts[1].trim());
                        if (item != null) {
                            loadedProject.addCatmapItem(item);
                        }
                    }
                    continue;
                }

                if (line.startsWith("PROJECT_LEGEND_ITEM|")) {
                    String[] itemParts = line.split("\\|", 2);
                    if (itemParts.length >= 2) {
                        CatmapLegendItem item = CatmapLegendItem.decode(itemParts[1].trim());
                        if (item != null) {
                            loadedProject.addCatmapLegendItem(item);
                        }
                    }
                    continue;
                }

                if (line.startsWith("PROJECT_LAYER_GROUP|")) {
                    String[] groupParts = line.split("\\|", -1);
                    if (groupParts.length >= 2) {
                        LayerGroup group = new LayerGroup(groupParts[1].trim());
                        if (groupParts.length >= 3) {
                            group.setVisible(Boolean.parseBoolean(groupParts[2].trim()));
                        }
                        if (groupParts.length >= 4) {
                            group.setExpanded(Boolean.parseBoolean(groupParts[3].trim()));
                        }
                        loadedProject.addLayerGroup(group);
                    }
                    continue;
                }

                if (line.startsWith("VIEW|")) {
                    String[] viewParts = line.split("\\|", -1);
                    if (viewParts.length >= 4) {
                        try {
                            savedViewMinX = Double.parseDouble(viewParts[1]);
                            savedViewMinY = Double.parseDouble(viewParts[2]);
                            savedZoomFactor = Double.parseDouble(viewParts[3]);
                            viewLoaded = true;
                        } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar vista guardada en proyecto CATGIS", ignored); }
                    }
                    continue;
                }

                Layer layer = parseLayer(line);
                if (layer == null) {
                    continue;
                }

                if (layer.isInGroup() && loadedProject.getLayerGroup(layer.getGroupName()) == null) {
                    loadedProject.addLayerGroup(layer.getGroupName());
                }
                loadedProject.addLayer(layer);
            }

            normalizeCatserverGrouping(loadedProject);

            if (CatgisDesktopApp.layersPanel != null) {
                CatgisDesktopApp.layersPanel.clearLayers();
            }
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.clearAllLayers();
            }

            CatgisDesktopApp.currentProject = loadedProject;
            AppContext.get().setProject(loadedProject);

            for (Layer layer : loadedProject.getLayers()) {
                CatgisDesktopApp.layersPanel.addLayer(layer);
                loadLayerData(layer);
            }

            TopographyWorkflowSupport.normalizeTopographyOverlayOrder();
            CatgisDesktopApp.mapPanel.refreshLayerVisibility();
            CatgisDesktopApp.layersPanel.refreshLayerList();
            CatgisDesktopApp.markProjectClean();

            boolean finalViewLoaded = viewLoaded;
            double finalSavedViewMinX = savedViewMinX;
            double finalSavedViewMinY = savedViewMinY;
            double finalSavedZoomFactor = savedZoomFactor;

            SwingUtilities.invokeLater(() -> {
                CatgisDesktopApp.mapPanel.restoreViewOrReset(
                        finalSavedViewMinX,
                        finalSavedViewMinY,
                        finalSavedZoomFactor,
                        finalViewLoaded
                );
                CatgisDesktopApp.mapPanel.repaint();
            });

            if (CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage(
                        "Proyecto cargado | CRS: " + CRSDefinitions.getLabelForCode(CatgisDesktopApp.currentProject.getProjectCRS())
                );
            }

            // Track recent file
            MainMenuBar.addRecentFile(file.getAbsolutePath());

            if (showDialogs) {
                JOptionPane.showMessageDialog(owner, "Proyecto cargado correctamente.");
            }
            return true;
        } catch (Exception ex) {
            AppErrorSupport.logFailure("Error al abrir proyecto " + file.getAbsolutePath(), ex);
            if (showDialogs) {
                AppErrorSupport.showErrorDialog(owner, "Abrir proyecto", "Error al abrir proyecto.", ex);
            }
            return false;
        }
    }

    private static void loadLayerData(Layer layer) {
        if (layer == null) {
            return;
        }

        if (layer instanceof OnlineTileLayer) {
            CatgisDesktopApp.mapPanel.addOrUpdateOnlineTileLayer((OnlineTileLayer) layer);
            return;
        }
        if (layer instanceof OnlineWmsLayer) {
            CatgisDesktopApp.mapPanel.addOrUpdateOnlineWmsLayer((OnlineWmsLayer) layer);
            return;
        }
        try {
            String path = layer.getPath() != null ? layer.getPath().trim().toLowerCase() : "";

            if (isRasterLayer(layer)) {
                if (path.isBlank()) {
                    return;
                }
                File rasterFile = new File(layer.getPath());
                LocalRasterData rasterData;
                String projectCRS = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
                String sourceCRS = layer.getSourceCRS();
                if (layer instanceof RasterLayer) {
                    RasterLayer rasterLayer = (RasterLayer) layer;
                    if (rasterLayer.isDerivedLayer()) {
                        if (FloodScenarioService.OP_PRELIMINARY_FLOOD.equalsIgnoreCase(rasterLayer.getDerivedOperation())) {
                            rasterData = FloodScenarioService.regenerateDerivedRasterData(rasterLayer);
                        } else if (BooleanRiskService.OP_SLOPE_BOOLEAN_MASK.equalsIgnoreCase(rasterLayer.getDerivedOperation())
                                || BooleanRiskService.OP_SOIL_BOOLEAN_MASK.equalsIgnoreCase(rasterLayer.getDerivedOperation())
                                || BooleanRiskService.OP_PRELIMINARY_BOOLEAN_RISK.equalsIgnoreCase(rasterLayer.getDerivedOperation())) {
                            rasterData = BooleanRiskService.regenerateDerivedRasterData(rasterLayer);
                        } else {
                            rasterData = TerrainHydrologyAnalysisService.regenerateDerivedRasterData(rasterLayer);
                        }
                    } else {
                    String mode = rasterLayer.getRasterMode();
                    if (RasterImageLoader.MODE_REAL.equalsIgnoreCase(mode)) {
                        rasterData = RasterImageLoader.loadReal(rasterFile, projectCRS, sourceCRS);
                    } else if (RasterImageLoader.MODE_VIRTUAL.equalsIgnoreCase(mode)) {
                        rasterData = RasterImageLoader.loadVirtual(rasterFile, projectCRS, sourceCRS);
                    } else {
                        rasterData = RasterImageLoader.loadPreview(rasterFile, projectCRS, sourceCRS);
                        rasterLayer.setRasterMode(rasterData.getRasterMode());
                    }
                    }
                } else {
                    rasterData = RasterImageLoader.loadPreview(rasterFile, projectCRS, sourceCRS);
                }
                if (rasterData != null) {
                    String savedCrs = layer.getSourceCRS();
                    if (savedCrs == null || savedCrs.isBlank()) {
                        layer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(rasterData, projectCRS));
                    }
                    CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(layer, rasterData);
                }
                return;
            }
            if ("VECTOR".equalsIgnoreCase(layer.getType())) {
                LayerVectorDataSupport.ensureDataLoaded(
                        layer,
                        CatgisDesktopApp.getMainFrameSafe(),
                        "Ingresá la clave para reconstruir la capa PostGIS guardada en el proyecto.",
                        false
                );
            }
        } catch (Exception ex) {
            LayerVectorDataSupport.showLoadFailure(
                    layer,
                    CatgisDesktopApp.getMainFrameSafe(),
                    "Abrir proyecto",
                    "No se pudo reconstruir la capa seleccionada del proyecto.",
                    ex
            );
            if (!(layer instanceof PostgisLayer) && CatgisDesktopApp.statusBar != null) {
                String layerName = layer != null ? layer.getName() : "capa desconocida";
                CatgisDesktopApp.statusBar.setMessage("No se pudo reconstruir la capa \"" + layerName + "\".");
            }
        }
    }

    private static Layer parseLayer(String line) {
        try {
            String[] parts = line.split("\\|", -1);
            if (parts.length < 3) {
                return null;
            }

            String typePart = parts[0].trim();
            String type = typePart.replace("[", "").replace("]", "").trim();

            String name = parts[1].trim();
            String path = parts[2].trim();

            Layer layer;
            if ("ONLINE_TILE".equalsIgnoreCase(type)) {
                OnlineTileLayer tile = new OnlineTileLayer(name);
                tile.setPath(path);
                layer = tile;
            } else if ("ONLINE_WMS".equalsIgnoreCase(type)) {
                OnlineWmsLayer wms = new OnlineWmsLayer(name);
                wms.setPath(path);
                layer = wms;
            } else if ("ONLINE_WFS".equalsIgnoreCase(type)) {
                OnlineWfsLayer wfs = new OnlineWfsLayer(name);
                wfs.setPath(path);
                layer = wfs;
            } else if ("POSTGIS".equalsIgnoreCase(type)) {
                PostgisLayer postgis = new PostgisLayer(name);
                postgis.setPath(path);
                layer = postgis;
            } else if ("GPX".equalsIgnoreCase(type)) {
                GpxLayer gpx = new GpxLayer(name, path, GpxLayer.ContentKind.WAYPOINTS);
                layer = gpx;
            } else if ("GEOPACKAGE".equalsIgnoreCase(type)) {
                GeoPackageLayer geoPackage = new GeoPackageLayer(name, path);
                layer = geoPackage;
            } else {
                layer = isRasterType(type, path)
                        ? new RasterLayer(name, path)
                        : new Layer(name, path, type);
            }

            if (parts.length > 3) {
                layer.setVisible(Boolean.parseBoolean(parts[3].trim()));
            }

            if (parts.length > 4) {
                layer.setLabelsVisible(Boolean.parseBoolean(parts[4].trim()));
            }

            if (parts.length > 5) {
                String labelField = parts[5].trim();
                layer.setLabelField(labelField.isEmpty() ? null : labelField);
            }

            if (parts.length > 6) {
                Color fillColor = parseColor(parts[6].trim());
                if (fillColor != null) {
                    layer.setFillColor(fillColor);
                }
            }

            if (parts.length > 7) {
                Color borderColor = parseColor(parts[7].trim());
                if (borderColor != null) {
                    layer.setBorderColor(borderColor);
                }
            }

            if (parts.length > 8) {
                Color lineColor = parseColor(parts[8].trim());
                if (lineColor != null) {
                    layer.setLineColor(lineColor);
                }
            }

            if (parts.length > 9) {
                try {
                    float lineWidth = Float.parseFloat(parts[9].trim().replace(",", "."));
                    if (lineWidth > 0) {
                        layer.setLineWidth(lineWidth);
                    }
                } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar ancho de linea en proyecto CATGIS", ignored); }
            }

            if (parts.length > 10) {
                Color pointColor = parseColor(parts[10].trim());
                if (pointColor != null) {
                    layer.setPointColor(pointColor);
                }
            }

            if (parts.length > 11) {
                try {
                    int pointSize = Integer.parseInt(parts[11].trim());
                    if (pointSize > 0) {
                        layer.setPointSize(pointSize);
                    }
                } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar tamaño de punto en proyecto CATGIS", ignored); }
            }

            if (parts.length > 12) {
                layer.setSourceCRS(parts[12].trim());
            }

            int payloadStart = 13;
            if (parts.length > 15 && looksLikeStyleSection(parts, 13)) {
                layer.setPointSymbolStyle(Layer.PointSymbolStyle.fromValue(parts[13].trim()));
                layer.setLineSymbolStyle(Layer.LineSymbolStyle.fromValue(parts[14].trim()));
                layer.setPolygonFillStyle(Layer.PolygonFillStyle.fromValue(parts[15].trim()));
                payloadStart = 16;
                if (parts.length > 17 && looksLikeThemeSection(parts, 16)) {
                    layer.getPointCategorizedSymbology().clearRules();
                    layer.getLineCategorizedSymbology().clearRules();
                    mergeSymbology(layer.getLineCategorizedSymbology(), LayerSymbologyCodec.decodeCategorizedSymbology(parts[16].trim()));
                    layer.getPolygonCategorizedSymbology().clearRules();
                    mergeSymbology(layer.getPolygonCategorizedSymbology(), LayerSymbologyCodec.decodeCategorizedSymbology(parts[17].trim()));
                    payloadStart = 18;
                }
            }
            if (parts.length > payloadStart && parts[payloadStart].startsWith("POINT_ICON=")) {
                layer.setPointGraphicSymbol(parts[payloadStart].substring("POINT_ICON=".length()).trim());
                payloadStart++;
            }
            if (parts.length > payloadStart && parts[payloadStart].startsWith("POINT_THEME=")) {
                mergeSymbology(
                        layer.getPointCategorizedSymbology(),
                        LayerSymbologyCodec.decodeCategorizedSymbology(parts[payloadStart].substring("POINT_THEME=".length()).trim())
                );
                payloadStart++;
            }
            if (layer instanceof GpxLayer gpxLayer && parts.length > payloadStart && parts[payloadStart].startsWith("GPX_KIND=")) {
                gpxLayer.setContentKind(GpxLayer.ContentKind.fromValue(parts[payloadStart].substring("GPX_KIND=".length()).trim()));
                payloadStart++;
            }
            // Skip LABEL_* keyed suffixes so raster payload is read from correct positions
            while (parts.length > payloadStart && (parts[payloadStart].startsWith("LABEL_") || parts[payloadStart].startsWith("CAD_") || parts[payloadStart].startsWith("SOURCE_NAME="))) {
                payloadStart++;
            }

            if (layer instanceof RasterLayer) {
                RasterLayer raster = (RasterLayer) layer;
                if (parts.length > payloadStart) {
                    try {
                        raster.setOpacity(Float.parseFloat(parts[payloadStart].trim().replace(",", ".")));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                }
                if (parts.length > payloadStart + 1) {
                    raster.setGrayscale(Boolean.parseBoolean(parts[payloadStart + 1].trim()));
                }
                if (parts.length > payloadStart + 2) {
                    raster.setAutoContrast(Boolean.parseBoolean(parts[payloadStart + 2].trim()));
                }
                if (parts.length > payloadStart + 3) {
                    try {
                        raster.setRedBand(Integer.parseInt(parts[payloadStart + 3].trim()));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                }
                if (parts.length > payloadStart + 4) {
                    try {
                        raster.setGreenBand(Integer.parseInt(parts[payloadStart + 4].trim()));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                }
                if (parts.length > payloadStart + 5) {
                    try {
                        raster.setBlueBand(Integer.parseInt(parts[payloadStart + 5].trim()));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                }
                if (parts.length > payloadStart + 6) {
                    raster.setRasterMode(parts[payloadStart + 6].trim());
                }
                if (parts.length > payloadStart + 7 && parts[payloadStart + 7].startsWith("DERIVED_OP=")) {
                    raster.setDerivedOperation(parts[payloadStart + 7].substring("DERIVED_OP=".length()).trim());
                }
                if (parts.length > payloadStart + 8 && parts[payloadStart + 8].startsWith("DERIVED_ARGS=")) {
                    raster.setDerivedParameters(parts[payloadStart + 8].substring("DERIVED_ARGS=".length()).trim());
                }
            } else {
                if (parts.length > payloadStart) {
                    try {
                        layer.setOpacity(Float.parseFloat(parts[payloadStart].trim().replace(",", ".")));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                }
            }

            if (layer instanceof OnlineTileLayer) {
                OnlineTileLayer tile = (OnlineTileLayer) layer;
                if (parts.length > payloadStart + 7) {
                    tile.setSourceId(parts[payloadStart + 7].trim());
                }
                if (parts.length > payloadStart + 8) {
                    tile.setProviderName(parts[payloadStart + 8].trim());
                }
                if (parts.length > payloadStart + 9) {
                    tile.setUrlTemplate(parts[payloadStart + 9].trim());
                }
                if (parts.length > payloadStart + 10) {
                    try {
                        tile.setMinZoom(Integer.parseInt(parts[payloadStart + 10].trim()));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                }
                if (parts.length > payloadStart + 11) {
                    try {
                        tile.setMaxZoom(Integer.parseInt(parts[payloadStart + 11].trim()));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                }
                if (parts.length > payloadStart + 12) {
                    tile.setAttribution(parts[payloadStart + 12].trim());
                }
                if (parts.length > payloadStart + 13) {
                    tile.setTermsUrl(parts[payloadStart + 13].trim());
                }
                if (parts.length > payloadStart + 14) {
                    tile.setRequiresApiKey(Boolean.parseBoolean(parts[payloadStart + 14].trim()));
                }
            }

            if (layer instanceof OnlineWmsLayer) {
                OnlineWmsLayer wms = (OnlineWmsLayer) layer;
                if (parts.length > payloadStart + 7) {
                    wms.setSourceId(parts[payloadStart + 7].trim());
                }
                if (parts.length > payloadStart + 8) {
                    wms.setProviderName(parts[payloadStart + 8].trim());
                }
                if (parts.length > payloadStart + 9) {
                    wms.setServiceUrl(parts[payloadStart + 9].trim());
                }
                if (parts.length > payloadStart + 10) {
                    wms.setLayerNames(parts[payloadStart + 10].trim());
                }
                if (parts.length > payloadStart + 11) {
                    wms.setStyleNames(parts[payloadStart + 11].trim());
                }
                if (parts.length > payloadStart + 12) {
                    wms.setRequestCrs(parts[payloadStart + 12].trim());
                }
                if (parts.length > payloadStart + 13) {
                    wms.setVersion(parts[payloadStart + 13].trim());
                }
                if (parts.length > payloadStart + 14) {
                    wms.setImageFormat(parts[payloadStart + 14].trim());
                }
                if (parts.length > payloadStart + 15) {
                    wms.setTransparent(Boolean.parseBoolean(parts[payloadStart + 15].trim()));
                }
                if (parts.length > payloadStart + 16) {
                    wms.setAttribution(parts[payloadStart + 16].trim());
                }
                if (parts.length > payloadStart + 17) {
                    wms.setTermsUrl(parts[payloadStart + 17].trim());
                }
                String extentCrs = parts.length > payloadStart + 18 ? parts[payloadStart + 18].trim() : "";
                if (parts.length > payloadStart + 22) {
                    try {
                        double minX = Double.parseDouble(parts[payloadStart + 19].trim().replace(",", "."));
                        double minY = Double.parseDouble(parts[payloadStart + 20].trim().replace(",", "."));
                        double maxX = Double.parseDouble(parts[payloadStart + 21].trim().replace(",", "."));
                        double maxY = Double.parseDouble(parts[payloadStart + 22].trim().replace(",", "."));
                        wms.setExtent(minX, minY, maxX, maxY, extentCrs);
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                }
            }

            if (layer instanceof OnlineWfsLayer) {
                OnlineWfsLayer wfs = (OnlineWfsLayer) layer;
                if (parts.length > payloadStart) {
                    wfs.setProviderName(parts[payloadStart].trim());
                }
                if (parts.length > payloadStart + 1) {
                    wfs.setServiceUrl(parts[payloadStart + 1].trim());
                }
                if (parts.length > payloadStart + 2) {
                    wfs.setTypeName(parts[payloadStart + 2].trim());
                }
                if (parts.length > payloadStart + 3) {
                    wfs.setTypeTitle(parts[payloadStart + 3].trim());
                }
                if (parts.length > payloadStart + 4) {
                    wfs.setRequestCrs(parts[payloadStart + 4].trim());
                }
                if (parts.length > payloadStart + 5) {
                    wfs.setVersion(parts[payloadStart + 5].trim());
                }
                if (parts.length > payloadStart + 6) {
                    wfs.setReadOnly(Boolean.parseBoolean(parts[payloadStart + 6].trim()));
                }
            }

            if (layer instanceof GeoPackageLayer) {
                GeoPackageLayer geoPackage = (GeoPackageLayer) layer;
                if (parts.length > payloadStart) {
                    geoPackage.setTableName(parts[payloadStart].trim());
                }
                if (parts.length > payloadStart + 1) {
                    geoPackage.setIdentifier(parts[payloadStart + 1].trim());
                }
                if (parts.length > payloadStart + 2) {
                    geoPackage.setDescription(parts[payloadStart + 2].trim());
                }
                if (parts.length > payloadStart + 3) {
                    geoPackage.setGeometryTypeLabel(parts[payloadStart + 3].trim());
                }
                if (parts.length > payloadStart + 4) {
                    geoPackage.setReadOnly(Boolean.parseBoolean(parts[payloadStart + 4].trim()));
                }
            }

            if (layer instanceof PostgisLayer) {
                PostgisLayer postgis = (PostgisLayer) layer;
                if (parts.length > payloadStart) {
                    postgis.setHost(parts[payloadStart].trim());
                }
                if (parts.length > payloadStart + 1) {
                    try {
                        postgis.setPort(Integer.parseInt(parts[payloadStart + 1].trim()));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                }
                if (parts.length > payloadStart + 2) {
                    postgis.setDatabaseName(parts[payloadStart + 2].trim());
                }
                if (parts.length > payloadStart + 3) {
                    postgis.setSchemaName(parts[payloadStart + 3].trim());
                }
                if (parts.length > payloadStart + 4) {
                    postgis.setUserName(parts[payloadStart + 4].trim());
                }
                if (parts.length > payloadStart + 5) {
                    postgis.setTypeName(parts[payloadStart + 5].trim());
                }
                if (parts.length > payloadStart + 6) {
                    postgis.setTableName(parts[payloadStart + 6].trim());
                }
                if (parts.length > payloadStart + 7) {
                    postgis.setGeometryTypeLabel(parts[payloadStart + 7].trim());
                }
                if (parts.length > payloadStart + 8) {
                    postgis.setReadOnly(Boolean.parseBoolean(parts[payloadStart + 8].trim()));
                }
            }

            for (String part : parts) {
                if (part == null || part.isBlank()) {
                    continue;
                }
                if (part.startsWith("GROUP=")) {
                    layer.setGroupName(part.substring("GROUP=".length()).trim());
                } else if (part.startsWith("CAD_SHIFT_X=")) {
                    try {
                        layer.setCadOffsetX(Double.parseDouble(part.substring("CAD_SHIFT_X=".length()).trim().replace(",", ".")));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                } else if (part.startsWith("CAD_SHIFT_Y=")) {
                    try {
                        layer.setCadOffsetY(Double.parseDouble(part.substring("CAD_SHIFT_Y=".length()).trim().replace(",", ".")));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                } else if (part.startsWith("CAD_SCALE=")) {
                    try {
                        layer.setCadScale(Double.parseDouble(part.substring("CAD_SCALE=".length()).trim().replace(",", ".")));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                } else if (part.startsWith("CAD_ROT=")) {
                    try {
                        layer.setCadRotationDegrees(Double.parseDouble(part.substring("CAD_ROT=".length()).trim().replace(",", ".")));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                } else if (part.startsWith("CAD_GEOREF_METHOD=")) {
                    layer.setCadGeoreferenceMethod(part.substring("CAD_GEOREF_METHOD=".length()).trim());
                } else if (part.startsWith("CAD_GEOREF_M00=")) {
                    try {
                        layer.setCadGeorefM00(Double.parseDouble(part.substring("CAD_GEOREF_M00=".length()).trim().replace(",", ".")));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                } else if (part.startsWith("CAD_GEOREF_M01=")) {
                    try {
                        layer.setCadGeorefM01(Double.parseDouble(part.substring("CAD_GEOREF_M01=".length()).trim().replace(",", ".")));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                } else if (part.startsWith("CAD_GEOREF_M02=")) {
                    try {
                        layer.setCadGeorefM02(Double.parseDouble(part.substring("CAD_GEOREF_M02=".length()).trim().replace(",", ".")));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                } else if (part.startsWith("CAD_GEOREF_M10=")) {
                    try {
                        layer.setCadGeorefM10(Double.parseDouble(part.substring("CAD_GEOREF_M10=".length()).trim().replace(",", ".")));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                } else if (part.startsWith("CAD_GEOREF_M11=")) {
                    try {
                        layer.setCadGeorefM11(Double.parseDouble(part.substring("CAD_GEOREF_M11=".length()).trim().replace(",", ".")));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                } else if (part.startsWith("CAD_GEOREF_M12=")) {
                    try {
                        layer.setCadGeorefM12(Double.parseDouble(part.substring("CAD_GEOREF_M12=".length()).trim().replace(",", ".")));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                } else if (part.startsWith("CAD_GEOREF_RES_MEAN=")) {
                    try {
                        layer.setCadGeorefResidualMean(Double.parseDouble(part.substring("CAD_GEOREF_RES_MEAN=".length()).trim().replace(",", ".")));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                } else if (part.startsWith("CAD_GEOREF_RES_MAX=")) {
                    try {
                        layer.setCadGeorefResidualMax(Double.parseDouble(part.substring("CAD_GEOREF_RES_MAX=".length()).trim().replace(",", ".")));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                } else if (part.startsWith("CAD_GEOREF_REF_COUNT=")) {
                    try {
                        layer.setCadGeorefReferenceCount(Integer.parseInt(part.substring("CAD_GEOREF_REF_COUNT=".length()).trim()));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                } else if (part.startsWith("CAD_GEOREF_CHECK_COUNT=")) {
                    try {
                        layer.setCadGeorefCheckCount(Integer.parseInt(part.substring("CAD_GEOREF_CHECK_COUNT=".length()).trim()));
                    } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar valor numerico de proyecto CATGIS", ignored); }
                } else if (part.startsWith("CAD_HIDDEN_LAYERS=")) {
                    layer.setCadHiddenInternalLayersEncoded(part.substring("CAD_HIDDEN_LAYERS=".length()).trim());
                } else if (part.startsWith("SOURCE_NAME=")) {
                    layer.setSourceName(part.substring("SOURCE_NAME=".length()).trim());
                } else if (part.startsWith("LABEL_FONT=")) {
                    layer.setLabelFontFamily(part.substring("LABEL_FONT=".length()).trim());
                } else if (part.startsWith("LABEL_SIZE=")) {
                    try { layer.setLabelFontSize(Integer.parseInt(part.substring("LABEL_SIZE=".length()).trim())); } catch (Exception ignored) {}
                } else if (part.startsWith("LABEL_BOLD=")) {
                    layer.setLabelBold(Boolean.parseBoolean(part.substring("LABEL_BOLD=".length()).trim()));
                } else if (part.startsWith("LABEL_ITALIC=")) {
                    layer.setLabelItalic(Boolean.parseBoolean(part.substring("LABEL_ITALIC=".length()).trim()));
                } else if (part.startsWith("LABEL_COLOR=")) {
                    Color c = parseColor(part.substring("LABEL_COLOR=".length()).trim()); if (c != null) layer.setLabelColor(c);
                } else if (part.startsWith("LABEL_HALO=")) {
                    layer.setLabelHaloEnabled(Boolean.parseBoolean(part.substring("LABEL_HALO=".length()).trim()));
                } else if (part.startsWith("LABEL_HALO_COLOR=")) {
                    Color c = parseColor(part.substring("LABEL_HALO_COLOR=".length()).trim()); if (c != null) layer.setLabelHaloColor(c);
                } else if (part.startsWith("LABEL_HALO_WIDTH=")) {
                    try { layer.setLabelHaloWidth(Float.parseFloat(part.substring("LABEL_HALO_WIDTH=".length()).trim())); } catch (Exception ignored) {}
                } else if (part.startsWith("LABEL_OFFSET_X=")) {
                    try { layer.setLabelOffsetX(Integer.parseInt(part.substring("LABEL_OFFSET_X=".length()).trim())); } catch (Exception ignored) {}
                } else if (part.startsWith("LABEL_OFFSET_Y=")) {
                    try { layer.setLabelOffsetY(Integer.parseInt(part.substring("LABEL_OFFSET_Y=".length()).trim())); } catch (Exception ignored) {}
                } else if (part.startsWith("LABEL_UNDERLINE=")) {
                    layer.setLabelUnderline(Boolean.parseBoolean(part.substring("LABEL_UNDERLINE=".length()).trim()));
                } else if (part.startsWith("LABEL_PLACEMENT=")) {
                    layer.setLabelPlacement(part.substring("LABEL_PLACEMENT=".length()).trim());
                } else if (part.startsWith("LABEL_PLACEMENT_MODE=")) {
                    try { layer.setLabelPlacementMode(Layer.LabelPlacementMode.fromValue(part.substring("LABEL_PLACEMENT_MODE=".length()).trim())); } catch (Exception ignored) {}
                } else if (part.startsWith("LABEL_PRIORITY=")) {
                    try { layer.setLabelPriority(Integer.parseInt(part.substring("LABEL_PRIORITY=".length()).trim())); } catch (Exception ignored) {}
                } else if (part.startsWith("LABEL_COLLISION_AVOID=")) {
                    layer.setLabelCollisionAvoid(Boolean.parseBoolean(part.substring("LABEL_COLLISION_AVOID=".length()).trim()));
                } else if (part.startsWith("LABEL_BG=")) {
                    layer.setLabelBackgroundEnabled(Boolean.parseBoolean(part.substring("LABEL_BG=".length()).trim()));
                } else if (part.startsWith("LABEL_BG_COLOR=")) {
                    Color c = parseColor(part.substring("LABEL_BG_COLOR=".length()).trim()); if (c != null) layer.setLabelBackgroundColor(c);
                } else if (part.startsWith("LABEL_MIN_SCALE=")) {
                    try { layer.setLabelMinScale(Double.parseDouble(part.substring("LABEL_MIN_SCALE=".length()).trim())); } catch (Exception ignored) {}
                } else if (part.startsWith("LABEL_MAX_SCALE=")) {
                    try { layer.setLabelMaxScale(Double.parseDouble(part.substring("LABEL_MAX_SCALE=".length()).trim())); } catch (Exception ignored) {}
                }
            }

            return layer;
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo interpretar una entrada de capa del proyecto", ex);
            return null;
        }
    }

    private static void applyProjectMetadata(Project project, String key, String value) {
        if (project == null || key == null) {
            return;
        }
        String repaired = repairMojibake(value);
        switch (key) {
            case "STUDY_NAME" -> project.setStudyName(repaired);
            case "COMPANY_NAME" -> project.setCompanyName(repaired);
            case "CARTOGRAPHER_NAME" -> project.setCartographerName(repaired);
            case "IMAGE_SOURCE" -> project.setImageSource(repaired);
            case "COORDINATE_REFERENCE" -> project.setCoordinateReference(repaired);
            case "LEGEND_TITLE" -> project.setLegendTitle(repaired);
            case "LEGEND_SUBTITLE" -> project.setLegendSubtitle(repaired);
            case "LOGO_PATH" -> project.setLogoPath(repaired);
            case "LAYOUT_IMAGE_PATH" -> project.setLayoutImagePath(repaired);
            case "CATMAP_NORTH_STYLE" -> project.setCatmapNorthStyle(repaired);
            case "CATMAP_SHOW_NORTH" -> project.setCatmapShowNorth(Boolean.parseBoolean(repaired));
            default -> {
            }
        }
    }

    static String repairMojibake(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (value.contains("\u00C3\u00B3") || value.contains("\u00C3\u00A1")
                || value.contains("\u00C3\u00A9") || value.contains("\u00C3\u00B1")
                || value.contains("\u00C3\u00BA") || value.contains("\u00C3\u00AD")
                || value.contains("\u00C3\u00B1") || value.contains("\u00C3\u00BC")
                || value.contains("\u00C2\u00A1") || value.contains("\u00C2\u00BF")) {
            try {
                return new String(value.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1),
                        java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ignored) { }
        }
        return value;
    }

    private static Color parseColor(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        try {
            String[] values = text.split(",");
            if (values.length == 3) {
                int r = Integer.parseInt(values[0].trim());
                int g = Integer.parseInt(values[1].trim());
                int b = Integer.parseInt(values[2].trim());
                return new Color(r, g, b);
            }

            if (values.length == 4) {
                int r = Integer.parseInt(values[0].trim());
                int g = Integer.parseInt(values[1].trim());
                int b = Integer.parseInt(values[2].trim());
                int a = Integer.parseInt(values[3].trim());
                return new Color(r, g, b, a);
            }
        } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar color en proyecto CATGIS", ignored); }

        return null;
    }

    private static boolean looksLikeStyleSection(String[] parts, int start) {
        if (parts == null || parts.length <= start + 2) {
            return false;
        }
        return isPointStyle(parts[start]) && isLineStyle(parts[start + 1]) && isPolygonStyle(parts[start + 2]);
    }

    private static boolean looksLikeThemeSection(String[] parts, int start) {
        if (parts == null || parts.length <= start + 1) {
            return false;
        }
        return isEncodedTheme(parts[start]) || isEncodedTheme(parts[start + 1]);
    }

    private static boolean isEncodedTheme(String value) {
        return value != null && ("-".equals(value.trim()) || value.contains("~"));
    }

    private static void mergeSymbology(CategorizedSymbology target, CategorizedSymbology source) {
        if (target == null || source == null) {
            return;
        }
        target.setFieldName(source.getFieldName());
        target.setLegendTitle(source.getLegendTitle());
        target.setLegendSubtitle(source.getLegendSubtitle());
        target.getRules().clear();
        for (CategoryStyleRule sourceRule : source.getRules().values()) {
            if (sourceRule == null) {
                continue;
            }
            CategoryStyleRule targetRule = target.getOrCreateRule(sourceRule.getValue());
            targetRule.setPrimaryColor(sourceRule.getPrimaryColor());
            targetRule.setSecondaryColor(sourceRule.getSecondaryColor());
            targetRule.setLineStyle(sourceRule.getLineStyle());
            targetRule.setLineWidth(sourceRule.getLineWidth());
            targetRule.setPolygonFillStyle(sourceRule.getPolygonFillStyle());
            targetRule.setPointSymbolStyle(sourceRule.getPointSymbolStyle());
            targetRule.setPointSize(sourceRule.getPointSize());
        }
    }

    private static boolean isPointStyle(String value) {
        return enumContains(Layer.PointSymbolStyle.values(), value);
    }

    private static boolean isLineStyle(String value) {
        return enumContains(Layer.LineSymbolStyle.values(), value);
    }

    private static boolean isPolygonStyle(String value) {
        return enumContains(Layer.PolygonFillStyle.values(), value);
    }

    private static <E extends Enum<E>> boolean enumContains(E[] values, String value) {
        if (value == null) {
            return false;
        }
        for (E enumValue : values) {
            if (enumValue.name().equalsIgnoreCase(value.trim())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRasterType(String type, String path) {
        String t = type != null ? type.trim().toUpperCase() : "";
        if (t.contains("RASTER") || t.contains("ONLINE_TILE") || t.contains("ONLINE_WMS")) {
            return true;
        }
        String p = path != null ? path.trim().toLowerCase() : "";
        return p.endsWith(".tif")
                || p.endsWith(".tiff")
                || p.endsWith(".jpg")
                || p.endsWith(".jpeg")
                || p.endsWith(".png")
                || p.endsWith(".bmp")
                || p.endsWith(".gif")
                || p.endsWith(".img")
                || p.endsWith(".ecw");
    }

    private static boolean isRasterLayer(Layer layer) {
        return layer instanceof RasterLayer || isRasterType(layer != null ? layer.getType() : null, layer != null ? layer.getPath() : null);
    }

    private static String stripExtension(String name) {
        if (name == null || name.isBlank()) {
            return "Proyecto cargado";
        }

        int idx = name.lastIndexOf('.');
        if (idx > 0) {
            return name.substring(0, idx);
        }
        return name;
    }

    private static void normalizeCatserverGrouping(Project project) {
        if (project == null) {
            return;
        }
        LayerGroup catserverGroup = null;
        for (Layer layer : project.getLayers()) {
            if (!(layer instanceof PostgisLayer postgisLayer)) {
                continue;
            }
            if (layer.isInGroup()) {
                continue;
            }
            if (!isCatserverLayer(postgisLayer)) {
                continue;
            }
            if (catserverGroup == null) {
                catserverGroup = project.getLayerGroup("CATSERVER");
                if (catserverGroup == null) {
                    catserverGroup = project.addLayerGroup("CATSERVER");
                }
                catserverGroup.setVisible(true);
                catserverGroup.setExpanded(true);
            }
            project.assignLayerToGroup(layer, catserverGroup.getName());
        }
    }

    private static boolean isCatserverLayer(PostgisLayer layer) {
        if (layer == null) {
            return false;
        }
        String databaseName = layer.getDatabaseName();
        if (databaseName != null && "catserver".equalsIgnoreCase(databaseName.trim())) {
            return true;
        }
        String sourceName = layer.getSourceName();
        return sourceName != null && sourceName.toLowerCase().contains("catserver");
    }

}
