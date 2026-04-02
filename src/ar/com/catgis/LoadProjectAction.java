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

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Abrir proyecto CATGIS");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Proyectos CATGIS (*.catgis)", "catgis"));

        int result = chooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

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
        if (layer == null || layer.getPath() == null || layer.getPath().isBlank()) {
            return;
        }

        String path = layer.getPath().trim().toLowerCase();

        try {
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

            Layer layer = isRasterType(type, path)
                    ? new RasterLayer(name, path)
                    : new Layer(name, path, type);

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

            if (layer instanceof RasterLayer) {
                RasterLayer raster = (RasterLayer) layer;
                if (parts.length > 13) {
                    try {
                        raster.setOpacity(Float.parseFloat(parts[13].trim().replace(",", ".")));
                    } catch (Exception ignored) {
                    }
                }
                if (parts.length > 14) {
                    raster.setGrayscale(Boolean.parseBoolean(parts[14].trim()));
                }
                if (parts.length > 15) {
                    raster.setAutoContrast(Boolean.parseBoolean(parts[15].trim()));
                }
                if (parts.length > 16) {
                    try {
                        raster.setRedBand(Integer.parseInt(parts[16].trim()));
                    } catch (Exception ignored) {
                    }
                }
                if (parts.length > 17) {
                    try {
                        raster.setGreenBand(Integer.parseInt(parts[17].trim()));
                    } catch (Exception ignored) {
                    }
                }
                if (parts.length > 18) {
                    try {
                        raster.setBlueBand(Integer.parseInt(parts[18].trim()));
                    } catch (Exception ignored) {
                    }
                }
                if (parts.length > 19) {
                    raster.setRasterMode(parts[19].trim());
                }
            }

            return layer;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
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

    private static boolean isRasterType(String type, String path) {
        String t = type != null ? type.trim().toUpperCase() : "";
        if (t.contains("RASTER")) {
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
