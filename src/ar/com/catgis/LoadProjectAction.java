package ar.com.catgis;

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
import java.lang.reflect.Method;

public class LoadProjectAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        loadProject();
    }

    public static void loadProject() {
        if (!CatgisDesktopApp.confirmProjectContinuation("abrir otro proyecto")) {
            return;
        }

        JFileChooser chooser = FileChooserSupport.createChooser("project-open", "Abrir proyecto CATGIS");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Proyectos CATGIS (*.catgis)", "catgis"));

        int result = chooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        FileChooserSupport.rememberSelection("project-open", chooser);
        FileChooserSupport.rememberSelection("project-save", chooser);

        if (file == null || !file.getName().toLowerCase().endsWith(".catgis")) {
            JOptionPane.showMessageDialog(null, "Seleccione un archivo .catgis válido.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();

            if (firstLine == null || !firstLine.trim().equals("CATGIS_PROJECT")) {
                JOptionPane.showMessageDialog(null, "Archivo de proyecto inválido.");
                return;
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

                if (line.startsWith("VIEW|")) {
                    String[] viewParts = line.split("\\|", -1);
                    if (viewParts.length >= 4) {
                        try {
                            savedViewMinX = Double.parseDouble(viewParts[1]);
                            savedViewMinY = Double.parseDouble(viewParts[2]);
                            savedZoomFactor = Double.parseDouble(viewParts[3]);
                            viewLoaded = true;
                        } catch (Exception ignored) {
                        }
                    }
                    continue;
                }

                Layer layer = parseLayer(line);
                if (layer == null) {
                    continue;
                }

                loadedProject.addLayer(layer);
            }

            if (CatgisDesktopApp.layersPanel != null) {
                CatgisDesktopApp.layersPanel.clearLayers();
            }
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.clearAllLayers();
            }

            CatgisDesktopApp.currentProject = loadedProject;

            for (Layer layer : loadedProject.getLayers()) {
                CatgisDesktopApp.layersPanel.addLayer(layer);
                loadLayerData(layer);
            }

            CatgisDesktopApp.mapPanel.refreshLayerVisibility();
            CatgisDesktopApp.layersPanel.refreshLayerList();
            CatgisDesktopApp.markProjectClean();

            boolean finalViewLoaded = viewLoaded;
            double finalSavedViewMinX = savedViewMinX;
            double finalSavedViewMinY = savedViewMinY;
            double finalSavedZoomFactor = savedZoomFactor;

            SwingUtilities.invokeLater(() -> {
                if (finalViewLoaded) {
                    CatgisDesktopApp.mapPanel.restoreView(finalSavedViewMinX, finalSavedViewMinY, finalSavedZoomFactor);
                } else {
                    CatgisDesktopApp.mapPanel.resetView();
                }
                CatgisDesktopApp.mapPanel.repaint();
            });

            if (CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage(
                        "Proyecto cargado | CRS: " + CRSDefinitions.getLabelForCode(CatgisDesktopApp.currentProject.getProjectCRS())
                );
            }

            JOptionPane.showMessageDialog(null, "Proyecto cargado correctamente.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al abrir proyecto: " + ex.getMessage());
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
            if (layer instanceof PostgisLayer) {
                PostgisConnectionInfo info = PostgisConnectionStore.applyStoredPassword(((PostgisLayer) layer).toConnectionInfo());
                if (info == null || info.getPassword().isBlank()) {
                    info = PostgisConnectionStore.promptForPassword(
                            CatgisDesktopApp.getMainFrameSafe(),
                            ((PostgisLayer) layer).toConnectionInfo(),
                            "Ingresá la clave para reconstruir la capa PostGIS guardada en el proyecto."
                    );
                    if (info == null) {
                        return;
                    }
                }
                ShapefileData data = PostgisLoader.loadLayerData((PostgisLayer) layer, info);
                if (data != null) {
                    layer.setSourceName(data.getSourceName());
                    layer.setFeatureCount(data.getFeatureCount());
                    CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(layer, data);
                }
                return;
            }
            if (layer instanceof GeoPackageLayer) {
                ShapefileData data = GeoPackageLoader.loadLayerData((GeoPackageLayer) layer);
                if (data != null) {
                    layer.setSourceName(data.getSourceName());
                    layer.setFeatureCount(data.getFeatureCount());
                    CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(layer, data);
                }
                return;
            }
            if (layer instanceof OnlineWfsLayer) {
                ShapefileData data = WfsFeatureLoader.loadLayerData((OnlineWfsLayer) layer);
                if (data != null) {
                    layer.setSourceName(data.getSourceName());
                    layer.setFeatureCount(data.getFeatureCount());
                    CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(layer, data);
                }
                return;
            }

            if (layer.getPath() == null || layer.getPath().isBlank()) {
                return;
            }

            String path = layer.getPath().trim().toLowerCase();

            if (isRasterLayer(layer)) {
                File rasterFile = new File(layer.getPath());
                LocalRasterData rasterData;
                String projectCRS = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
                String sourceCRS = layer.getSourceCRS();
                if (layer instanceof RasterLayer) {
                    RasterLayer rasterLayer = (RasterLayer) layer;
                    String mode = rasterLayer.getRasterMode();
                    if (RasterImageLoader.MODE_REAL.equalsIgnoreCase(mode)) {
                        rasterData = RasterImageLoader.loadReal(rasterFile, projectCRS, sourceCRS);
                    } else if (RasterImageLoader.MODE_VIRTUAL.equalsIgnoreCase(mode)) {
                        rasterData = RasterImageLoader.loadVirtual(rasterFile, projectCRS, sourceCRS);
                    } else {
                        rasterData = RasterImageLoader.loadPreview(rasterFile, projectCRS, sourceCRS);
                        rasterLayer.setRasterMode(rasterData.getRasterMode());
                    }
                } else {
                    rasterData = RasterImageLoader.loadPreview(rasterFile, projectCRS, sourceCRS);
                }
                if (rasterData != null) {
                    CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(layer, rasterData);
                    if (layer.getSourceCRS() == null || layer.getSourceCRS().isBlank()) {
                        layer.setSourceCRS(rasterData.getSourceCRS());
                    }
                }
                return;
            }

            if (!"VECTOR".equalsIgnoreCase(layer.getType())) {
                return;
            }

            ShapefileData data = null;

            if (path.endsWith(".shp")) {
                data = loadShapefileCompat(layer.getPath());
                if (layer.getSourceCRS() == null || layer.getSourceCRS().isBlank()) {
                    layer.setSourceCRS(ShapefileLoader.getCRSCode(new File(layer.getPath())));
                }
            } else if (path.endsWith(".geojson") || path.endsWith(".json")) {
                data = loadGeoJsonCompat(layer.getPath());
                if (layer.getSourceCRS() == null || layer.getSourceCRS().isBlank()) {
                    layer.setSourceCRS("EPSG:4326");
                }
            } else if (path.endsWith(".kml")) {
                data = loadKmlCompat(layer.getPath());
                if (layer.getSourceCRS() == null || layer.getSourceCRS().isBlank()) {
                    layer.setSourceCRS("EPSG:4326");
                }
            }

            if (data != null) {
                layer.setSourceName(data.getSourceName());
                layer.setFeatureCount(data.getFeatureCount());
                CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(layer, data);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("No se pudo cargar la capa desde proyecto: " + layer.getPath());
            if (layer instanceof PostgisLayer) {
                JOptionPane.showMessageDialog(
                        CatgisDesktopApp.getMainFrameSafe(),
                        "No se pudo reconstruir la capa PostGIS \"" + layer.getName() + "\".\n"
                                + PostgisErrorSupport.toUserMessage(ex, ((PostgisLayer) layer).toConnectionInfo()),
                        "PostGIS",
                        JOptionPane.ERROR_MESSAGE
                );
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
                } catch (Exception ignored) {
                }
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
                } catch (Exception ignored) {
                }
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
                    layer.getLineCategorizedSymbology().clearRules();
                    mergeSymbology(layer.getLineCategorizedSymbology(), LayerSymbologyCodec.decodeCategorizedSymbology(parts[16].trim()));
                    layer.getPolygonCategorizedSymbology().clearRules();
                    mergeSymbology(layer.getPolygonCategorizedSymbology(), LayerSymbologyCodec.decodeCategorizedSymbology(parts[17].trim()));
                    payloadStart = 18;
                }
            }

            if (layer instanceof RasterLayer) {
                RasterLayer raster = (RasterLayer) layer;
                if (parts.length > payloadStart) {
                    try {
                        raster.setOpacity(Float.parseFloat(parts[payloadStart].trim().replace(",", ".")));
                    } catch (Exception ignored) {
                    }
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
                    } catch (Exception ignored) {
                    }
                }
                if (parts.length > payloadStart + 4) {
                    try {
                        raster.setGreenBand(Integer.parseInt(parts[payloadStart + 4].trim()));
                    } catch (Exception ignored) {
                    }
                }
                if (parts.length > payloadStart + 5) {
                    try {
                        raster.setBlueBand(Integer.parseInt(parts[payloadStart + 5].trim()));
                    } catch (Exception ignored) {
                    }
                }
                if (parts.length > payloadStart + 6) {
                    raster.setRasterMode(parts[payloadStart + 6].trim());
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
                    } catch (Exception ignored) {
                    }
                }
                if (parts.length > payloadStart + 11) {
                    try {
                        tile.setMaxZoom(Integer.parseInt(parts[payloadStart + 11].trim()));
                    } catch (Exception ignored) {
                    }
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
                    } catch (Exception ignored) {
                    }
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
                    } catch (Exception ignored) {
                    }
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

            return layer;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static void applyProjectMetadata(Project project, String key, String value) {
        if (project == null || key == null) {
            return;
        }
        switch (key) {
            case "STUDY_NAME" -> project.setStudyName(value);
            case "COMPANY_NAME" -> project.setCompanyName(value);
            case "CARTOGRAPHER_NAME" -> project.setCartographerName(value);
            case "IMAGE_SOURCE" -> project.setImageSource(value);
            case "COORDINATE_REFERENCE" -> project.setCoordinateReference(value);
            case "LEGEND_TITLE" -> project.setLegendTitle(value);
            case "LEGEND_SUBTITLE" -> project.setLegendSubtitle(value);
            case "LOGO_PATH" -> project.setLogoPath(value);
            case "LAYOUT_IMAGE_PATH" -> project.setLayoutImagePath(value);
            default -> {
            }
        }
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
        } catch (Exception ignored) {
        }

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
        target.getRules().putAll(source.getRules());
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

    private static ShapefileData loadShapefileCompat(String path) throws Exception {
        return invokeLoader(ShapefileLoader.class, path,
                new String[]{"load", "loadShapefile", "open", "openShapefile", "read", "readShapefile"});
    }

    private static ShapefileData loadGeoJsonCompat(String path) throws Exception {
        return invokeLoader(GeoJsonLoader.class, path,
                new String[]{"load", "loadGeoJson", "open", "read"});
    }

    private static ShapefileData loadKmlCompat(String path) throws Exception {
        return invokeLoader(KmlLoader.class, path,
                new String[]{"load", "loadKml", "open", "read"});
    }

    private static ShapefileData invokeLoader(Class<?> clazz, String path, String[] methodNames) throws Exception {
        if (path == null || path.isBlank()) {
            return null;
        }

        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("No existe el archivo: " + path);
        }

        for (String methodName : methodNames) {
            try {
                Method m = clazz.getMethod(methodName, String.class);
                Object result = m.invoke(null, path);
                if (result instanceof ShapefileData) {
                    return (ShapefileData) result;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }

        for (String methodName : methodNames) {
            try {
                Method m = clazz.getMethod(methodName, File.class);
                Object result = m.invoke(null, file);
                if (result instanceof ShapefileData) {
                    return (ShapefileData) result;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }

        throw new RuntimeException("No se encontró un método compatible en el loader para: " + path);
    }
}
