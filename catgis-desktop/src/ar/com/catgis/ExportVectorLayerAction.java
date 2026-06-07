package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.api.data.Transaction;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import javax.swing.JFileChooser;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExportVectorLayerAction {

    private static final String SHAPEFILE_OPTION = "Shapefile (*.shp)";
    private static final String DXF_OPTION = "CAD DXF (*.dxf)";
    private static final String GEOJSON_OPTION = "GeoJSON (*.geojson)";
    private static final String KML_OPTION = "KML (*.kml)";
    private static final String KMZ_OPTION = "KMZ (*.kmz)";
    private static final String GPX_OPTION = "GPX (*.gpx)";

    public static void exportLayer(Layer layer) {
        if (layer == null) {
            JOptionPane.showMessageDialog(null, "No hay una capa seleccionada.");
            return;
        }

        ShapefileData data = CatgisDesktopApp.mapPanel.getShapefileData(layer);
        if (!hasExportableVectorData(data)) {
            JOptionPane.showMessageDialog(null, "La capa no tiene datos disponibles para exportar.");
            return;
        }

        exportLayerWithDialog(layer, data, CatgisDesktopApp.getMainFrameSafe(), "Exportar capa", true);
    }

    public static String[] getSupportedVectorFormats() {
        return new String[]{SHAPEFILE_OPTION, DXF_OPTION, GEOJSON_OPTION, KML_OPTION, KMZ_OPTION, GPX_OPTION};
    }

    public static File exportLayerWithDialog(Layer layer,
                                             ShapefileData data,
                                             Component parent,
                                             String dialogTitle,
                                             boolean showSuccessMessage) {
        if (layer == null) {
            JOptionPane.showMessageDialog(parent, "No hay una capa seleccionada.");
            return null;
        }
        if (!hasExportableVectorData(data)) {
            JOptionPane.showMessageDialog(parent, "La capa no tiene datos disponibles para exportar.");
            return null;
        }

        Object selected = JOptionPane.showInputDialog(
                parent,
                "Seleccione formato de exportación:",
                dialogTitle != null && !dialogTitle.isBlank() ? dialogTitle : "Exportar capa",
                JOptionPane.PLAIN_MESSAGE,
                null,
                getSupportedVectorFormats(),
                CadLayerSupport.isCadLayer(layer) ? DXF_OPTION : SHAPEFILE_OPTION
        );

        if (selected == null) {
            return null;
        }

        String option = selected.toString();
        String extension = extensionForOption(option);

        JFileChooser chooser = FileChooserSupport.createChooser(
                "vector-export",
                dialogTitle != null && !dialogTitle.isBlank() ? dialogTitle : "Exportar capa"
        );
        chooser.setAcceptAllFileFilterUsed(false);

        if (SHAPEFILE_OPTION.equals(option)) {
            chooser.setFileFilter(new FileNameExtensionFilter("Shapefile (*.shp)", "shp"));
        } else if (DXF_OPTION.equals(option)) {
            chooser.setFileFilter(new FileNameExtensionFilter("CAD DXF (*.dxf)", "dxf"));
        } else if (GEOJSON_OPTION.equals(option)) {
            chooser.setFileFilter(new FileNameExtensionFilter("GeoJSON (*.geojson)", "geojson"));
        } else if (GPX_OPTION.equals(option)) {
            chooser.setFileFilter(new FileNameExtensionFilter("GPX (*.gpx)", "gpx"));
        } else if (KMZ_OPTION.equals(option)) {
            chooser.setFileFilter(new FileNameExtensionFilter("KMZ (*.kmz)", "kmz"));
        } else {
            chooser.setFileFilter(new FileNameExtensionFilter("KML (*.kml)", "kml"));
        }

        chooser.setSelectedFile(FileChooserSupport.resolveSuggestedFile(
                "vector-export",
                new File(safeFileName(layer.getName()) + extension)
        ));

        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(extension)) {
            file = new File(file.getAbsolutePath() + extension);
        }
        FileChooserSupport.rememberFile("vector-export", file);

        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(
                    parent,
                    "El archivo ya existe.\n¿Querés reemplazarlo?\n\n" + file.getAbsolutePath(),
                    dialogTitle != null && !dialogTitle.isBlank() ? dialogTitle : "Exportar capa",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (overwrite != JOptionPane.YES_OPTION) {
                return null;
            }
        }

        if (!saveLayerToFile(layer, data, file, option, parent, showSuccessMessage)) {
            return null;
        }

        return file;
    }

    public static File exportLayerWithOptions(Layer layer,
                                              ShapefileData data,
                                              Component parent,
                                              String dialogTitle,
                                              boolean showSuccessMessage,
                                              String option,
                                              String targetCode) {
        if (layer == null) {
            JOptionPane.showMessageDialog(parent, "No hay una capa seleccionada.");
            return null;
        }
        if (!hasExportableVectorData(data)) {
            JOptionPane.showMessageDialog(parent, "La capa no tiene datos disponibles para exportar.");
            return null;
        }
        if (option == null || option.isBlank()) {
            JOptionPane.showMessageDialog(parent, "Debe indicar un formato de salida.");
            return null;
        }

        String extension = extensionForOption(option);
        JFileChooser chooser = FileChooserSupport.createChooser(
                "vector-export",
                dialogTitle != null && !dialogTitle.isBlank() ? dialogTitle : "Exportar capa reproyectada"
        );
        chooser.setAcceptAllFileFilterUsed(false);

        if (SHAPEFILE_OPTION.equals(option)) {
            chooser.setFileFilter(new FileNameExtensionFilter("Shapefile (*.shp)", "shp"));
        } else if (DXF_OPTION.equals(option)) {
            chooser.setFileFilter(new FileNameExtensionFilter("CAD DXF (*.dxf)", "dxf"));
        } else if (GEOJSON_OPTION.equals(option)) {
            chooser.setFileFilter(new FileNameExtensionFilter("GeoJSON (*.geojson)", "geojson"));
        } else if (GPX_OPTION.equals(option)) {
            chooser.setFileFilter(new FileNameExtensionFilter("GPX (*.gpx)", "gpx"));
        } else if (KMZ_OPTION.equals(option)) {
            chooser.setFileFilter(new FileNameExtensionFilter("KMZ (*.kmz)", "kmz"));
        } else {
            chooser.setFileFilter(new FileNameExtensionFilter("KML (*.kml)", "kml"));
        }

        chooser.setSelectedFile(FileChooserSupport.resolveSuggestedFile(
                "vector-export",
                new File(safeFileName(layer.getName()) + extension)
        ));
        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(extension)) {
            file = new File(file.getAbsolutePath() + extension);
        }
        FileChooserSupport.rememberFile("vector-export", file);

        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(
                    parent,
                    "El archivo ya existe.\nDesea reemplazarlo?\n\n" + file.getAbsolutePath(),
                    dialogTitle != null && !dialogTitle.isBlank() ? dialogTitle : "Exportar capa reproyectada",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (overwrite != JOptionPane.YES_OPTION) {
                return null;
            }
        }

        if (!saveLayerToFileWithTarget(layer, data, file, option, targetCode, parent, showSuccessMessage)) {
            return null;
        }
        return file;
    }

    public static boolean saveLayerToCurrentPath(Layer layer, Component parent, boolean showSuccessMessage) {
        if (layer == null || layer.getPath() == null || layer.getPath().isBlank() || CatgisDesktopApp.mapPanel == null) {
            return false;
        }

        ShapefileData data = CatgisDesktopApp.mapPanel.getShapefileData(layer);
        if (!hasExportableVectorData(data)) {
            return false;
        }

        if (layer instanceof PostgisLayer postgisLayer) {
            return PostgisWriteService.saveLayerToCurrentPath(postgisLayer, data, parent, showSuccessMessage);
        }

        String option = optionForPath(layer.getPath());
        if (option == null) {
            return false;
        }

        return saveLayerToFile(layer, data, new File(layer.getPath()), option, parent, showSuccessMessage);
    }

    public static boolean hasSupportedVectorPath(Layer layer) {
        if (layer instanceof PostgisLayer postgisLayer) {
            return postgisLayer.getTableName() != null && !postgisLayer.getTableName().isBlank();
        }
        return layer != null && optionForPath(layer.getPath()) != null;
    }

    public static boolean saveLayerDataToFile(Layer layer,
                                              ShapefileData data,
                                              File file,
                                              Component parent,
                                              boolean showSuccessMessage) {
        if (layer == null || file == null || !hasExportableVectorData(data)) {
            return false;
        }

        String option = optionForPath(file.getName());
        if (option == null) {
            return false;
        }

        return saveLayerToFile(layer, data, file, option, parent, showSuccessMessage);
    }

    private static boolean saveLayerToFileWithTarget(Layer layer,
                                                     ShapefileData data,
                                                     File file,
                                                     String option,
                                                     String targetCode,
                                                     Component parent,
                                                     boolean showSuccessMessage) {
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            KmlExportOptions kmlOptions = requiresKmlOptions(option)
                    ? resolveKmlExportOptions(layer, data, parent)
                    : KmlExportOptions.defaults();
            if (kmlOptions == null) {
                return false;
            }

            if (SHAPEFILE_OPTION.equals(option)) {
                deleteShapefileSidecars(file);
                exportToShapefile(layer, data, file, resolveTargetCode(layer, option, targetCode));
            } else if (DXF_OPTION.equals(option)) {
                exportToDxf(layer, data, file, resolveTargetCode(layer, option, targetCode));
            } else if (GEOJSON_OPTION.equals(option)) {
                exportToGeoJson(layer, data, file, resolveTargetCode(layer, option, targetCode));
            } else if (GPX_OPTION.equals(option)) {
                exportToGpx(layer, data, file);
            } else {
                exportToKml(layer, data, file, kmlOptions);
            }

            if (showSuccessMessage) {
                JOptionPane.showMessageDialog(parent, "Capa exportada correctamente:\n" + file.getAbsolutePath());
            }
            int addToProject = JOptionPane.showConfirmDialog(
                    parent,
                    "La capa reproyectada se exporto correctamente.\n\nDesea agregar el resultado al proyecto actual?",
                    "Exportar capa reproyectada",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (addToProject == JOptionPane.YES_OPTION) {
                if (GEOJSON_OPTION.equals(option) && targetCode != null && !targetCode.isBlank()) {
                    ShapefileData reloaded = GeoJsonLoader.load(file);
                    Layer resultLayer = new VectorLayer(file.getName(), file.getAbsolutePath());
                    resultLayer.setVisible(true);
                    resultLayer.setSourceName(file.getName());
                    resultLayer.setFeatureCount(reloaded.getFeatureCount());
                    resultLayer.setSourceCRS(targetCode);
                    if (CatgisDesktopApp.currentProject == null) {
                        CatgisDesktopApp.currentProject = new Project("Proyecto actual");
                    }
                    CatgisDesktopApp.currentProject.addLayer(resultLayer);
                    CatgisDesktopApp.markProjectDirty();
                    if (CatgisDesktopApp.layersPanel != null) {
                        CatgisDesktopApp.layersPanel.addLayer(resultLayer);
                    }
                    if (CatgisDesktopApp.mapPanel != null) {
                        CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(resultLayer, reloaded);
                        CatgisDesktopApp.mapPanel.showOpenedFile(resultLayer.getName());
                    }
                } else {
                    OpenFileAction.openSelectedFile(file, option, parent);
                }
            }
            return true;
        } catch (Exception ex) {
            AppErrorSupport.logFailure("Error al exportar capa reproyectada a " + file.getAbsolutePath(), ex);
            showExportError(parent, ex);
            return false;
        }
    }

    private static boolean saveLayerToFile(Layer layer,
                                           ShapefileData data,
                                           File file,
                                           String option,
                                           Component parent,
                                           boolean showSuccessMessage) {
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            String resolvedTargetCode = resolveTargetCode(layer, option);

            KmlExportOptions kmlOptions = requiresKmlOptions(option)
                    ? resolveKmlExportOptions(layer, data, parent)
                    : KmlExportOptions.defaults();
            if (kmlOptions == null) {
                return false;
            }

            if (SHAPEFILE_OPTION.equals(option)) {
                deleteShapefileSidecars(file);
                exportToShapefile(layer, data, file, resolvedTargetCode);
            } else if (DXF_OPTION.equals(option)) {
                exportToDxf(layer, data, file, resolvedTargetCode);
            } else if (GEOJSON_OPTION.equals(option)) {
                exportToGeoJson(layer, data, file, resolvedTargetCode);
            } else if (GPX_OPTION.equals(option)) {
                exportToGpx(layer, data, file);
            } else {
                exportToKml(layer, data, file, kmlOptions);
            }

            if (!GPX_OPTION.equals(option)) {
                refreshLayerFromFile(layer, file, resolvedTargetCode);
            }

            if (showSuccessMessage) {
                JOptionPane.showMessageDialog(parent, "Capa exportada correctamente:\n" + file.getAbsolutePath());
            }
            return true;
        } catch (Exception ex) {
            AppErrorSupport.logFailure("Error al exportar capa a " + file.getAbsolutePath(), ex);
            showExportError(parent, ex);
            return false;
        }
    }

    private static void exportToShapefile(Layer layer, ShapefileData data, File file, String targetCode) throws Exception {
        TransformResult transformResult = transformFeaturesToTarget(layer, data, targetCode, SHAPEFILE_OPTION);
        SimpleFeatureType featureType = transformResult.featureType;
        List<SimpleFeature> features = transformResult.features;

        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
        Map<String, java.io.Serializable> params = new HashMap<>();
        params.put("url", file.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore store = (ShapefileDataStore) factory.createNewDataStore(params);
        store.setCharset(StandardCharsets.UTF_8);
        store.createSchema(featureType);

        Transaction transaction = new DefaultTransaction("create");
        try {
            String typeName = store.getTypeNames()[0];
            SimpleFeatureSource source = store.getFeatureSource(typeName);

            if (!(source instanceof SimpleFeatureStore)) {
                throw new RuntimeException("No se pudo crear el Shapefile de salida.");
            }

            SimpleFeatureStore featureStore = (SimpleFeatureStore) source;
            featureStore.setTransaction(transaction);
            featureStore.addFeatures(new ListFeatureCollection(featureType, features));
            transaction.commit();
        } catch (Exception ex) {
            transaction.rollback();
            throw ex;
        } finally {
            transaction.close();
            store.dispose();
        }
    }

    private static void exportToGeoJson(Layer layer, ShapefileData data, File file, String targetCode) throws Exception {
        TransformResult transformResult = transformFeaturesToTarget(layer, data, targetCode, GEOJSON_OPTION);
        SimpleFeatureType featureType = transformResult.featureType;
        List<SimpleFeature> features = transformResult.features;

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            FeatureJSON featureJSON = new FeatureJSON();
            featureJSON.setFeatureType(featureType);
            featureJSON.setEncodeNullValues(true);
            featureJSON.writeFeatureCollection(new ListFeatureCollection(featureType, features), writer);
        }
    }

    private static void exportToDxf(Layer layer, ShapefileData data, File file, String targetCode) throws Exception {
        TransformResult transformResult = transformFeaturesToTarget(layer, data, targetCode, DXF_OPTION);
        List<SimpleFeature> features = transformResult.features;

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writeDxfPair(writer, "0", "SECTION");
            writeDxfPair(writer, "2", "HEADER");
            writeDxfPair(writer, "9", "$INSUNITS");
            writeDxfPair(writer, "70", "0");
            writeDxfPair(writer, "0", "ENDSEC");

            writeDxfPair(writer, "0", "SECTION");
            writeDxfPair(writer, "2", "ENTITIES");

            String defaultCadLayer = sanitizeCadLayerName(layer != null ? layer.getName() : "CATGIS");
            for (SimpleFeature feature : features) {
                writeDxfFeature(writer, feature, defaultCadLayer);
            }

            writeDxfPair(writer, "0", "ENDSEC");
            writeDxfPair(writer, "0", "EOF");
        }
    }

    private static void exportToKml(Layer layer, ShapefileData data, File file, KmlExportOptions options) throws Exception {
        TransformResult transformResult = transformFeaturesToTarget(layer, data, "EPSG:4326", KML_OPTION);
        List<SimpleFeature> features = transformResult.features;

        ByteArrayOutputStream xmlBytes = new ByteArrayOutputStream();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(xmlBytes, StandardCharsets.UTF_8))) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.newLine();
            writer.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
            writer.newLine();
            writer.write("  <Document>");
            writer.newLine();
            writer.write("    <name>" + xml(layer.getName()) + "</name>");
            writer.newLine();
            writer.write("    <Style id=\"catgis-default\">");
            writer.newLine();
            writeKmlStyle(writer, layer, options);
            writer.write("    </Style>");
            writer.newLine();

            for (SimpleFeature feature : features) {
                Object geomObj = feature.getDefaultGeometry();
                if (!(geomObj instanceof Geometry)) {
                    continue;
                }

                Geometry geometry = (Geometry) geomObj;
                if (geometry.isEmpty()) {
                    continue;
                }

                writer.write("    <Placemark>");
                writer.newLine();
                writer.write("      <styleUrl>#catgis-default</styleUrl>");
                writer.newLine();

                String name = getFeatureName(feature, layer, options);
                if (name != null && !name.isBlank()) {
                    writer.write("      <name>" + xml(name) + "</name>");
                    writer.newLine();
                }

                if (options.includeDescription()) {
                    writer.write("      <description>" + xml(buildDescription(feature)) + "</description>");
                    writer.newLine();
                }
                writeKmlExtendedData(writer, feature);

                writeKmlGeometry(writer, geometry, "      ");

                writer.write("    </Placemark>");
                writer.newLine();
            }

            writer.write("  </Document>");
            writer.newLine();
            writer.write("</kml>");
            writer.newLine();
        }

        if (file.getName().toLowerCase(Locale.ROOT).endsWith(".kmz")) {
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(file))) {
                zipOutputStream.putNextEntry(new ZipEntry("doc.kml"));
                zipOutputStream.write(xmlBytes.toByteArray());
                zipOutputStream.closeEntry();
            }
            return;
        }

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(xmlBytes.toByteArray());
        }
    }

    private static void exportToGpx(Layer layer, ShapefileData data, File file) throws Exception {
        TransformResult transformResult = transformFeaturesToTarget(layer, data, "EPSG:4326", GPX_OPTION);
        List<SimpleFeature> features = transformResult.features;

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.newLine();
            writer.write("<gpx version=\"1.1\" creator=\"CATGIS Desktop\" xmlns=\"http://www.topografix.com/GPX/1/1\">");
            writer.newLine();
            writer.write("  <metadata>");
            writer.newLine();
            writer.write("    <name>" + xml(layer.getName()) + "</name>");
            writer.newLine();
            writer.write("  </metadata>");
            writer.newLine();

            int pointIndex = 1;
            int lineIndex = 1;
            int polygonIndex = 1;

            for (SimpleFeature feature : features) {
                Object geomObj = feature.getDefaultGeometry();
                if (!(geomObj instanceof Geometry geometry) || geometry.isEmpty()) {
                    continue;
                }

                pointIndex = writeGpxWaypoints(writer, feature, geometry, pointIndex);
                lineIndex = writeGpxTracks(writer, feature, geometry, lineIndex);
                polygonIndex = writeGpxPolygonBoundaries(writer, feature, geometry, polygonIndex);
            }

            writer.write("</gpx>");
            writer.newLine();
        }
    }

    static TransformResult transformFeaturesToTarget(Layer layer,
                                                     ShapefileData data,
                                                     String targetCode,
                                                     String outputOption) throws Exception {
        List<SimpleFeature> inputFeatures = data != null && data.getFeatures() != null
                ? data.getFeatures()
                : List.of();
        SimpleFeatureType sourceType = data != null ? data.getSchema() : null;
        if (sourceType == null && !inputFeatures.isEmpty()) {
            sourceType = inputFeatures.get(0).getFeatureType();
        }
        if (sourceType == null) {
            throw new RuntimeException("La capa no tiene esquema vectorial disponible.");
        }

        if (targetCode == null || targetCode.isBlank()) {
            targetCode = getProjectCRSCode();
        }

        String sourceCode = normalizeLayerCRS(layer);
        if (sourceCode.isBlank()) {
            sourceCode = targetCode;
        }
        String projectCode = getProjectCRSCode();
        if (projectCode == null || projectCode.isBlank()) {
            projectCode = targetCode;
        }
        boolean cadLayer = CadLayerSupport.isCadLayer(layer);

        CoordinateReferenceSystem sourceCRS = null;
        CoordinateReferenceSystem projectCRS = null;
        CoordinateReferenceSystem targetCRS = null;
        MathTransform transform = null;
        MathTransform sourceToProjectTransform = null;
        MathTransform projectToTargetTransform = null;

        if (cadLayer) {
            targetCRS = CRSDefinitions.decode(targetCode, true);
            if (!sourceCode.equalsIgnoreCase(projectCode)) {
                sourceCRS = CRSDefinitions.decode(sourceCode, true);
                projectCRS = CRSDefinitions.decode(projectCode, true);
                sourceToProjectTransform = CRS.findMathTransform(sourceCRS, projectCRS, true);
            } else {
                projectCRS = CRSDefinitions.decode(projectCode, true);
            }
            if (!projectCode.equalsIgnoreCase(targetCode)) {
                projectToTargetTransform = CRS.findMathTransform(projectCRS, targetCRS, true);
            }
        } else if (!sourceCode.equalsIgnoreCase(targetCode)) {
            sourceCRS = CRSDefinitions.decode(sourceCode, true);
            targetCRS = CRSDefinitions.decode(targetCode, true);
            transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
        } else {
            targetCRS = CRSDefinitions.decode(targetCode, true);
        }

        List<TransformFeatureRow> transformedRows = new ArrayList<>();
        List<Geometry> transformedGeometries = new ArrayList<>();
        for (SimpleFeature feature : inputFeatures) {
            Geometry geometry = geometryOf(feature);
            if (geometry != null) {
                geometry = (Geometry) geometry.copy();
                if (cadLayer) {
                    if (sourceToProjectTransform != null) {
                        geometry = JTS.transform(geometry, sourceToProjectTransform);
                    }
                    geometry = CadPlacementSupport.applyPlacement(layer, geometry);
                    if (geometry != null && projectToTargetTransform != null) {
                        geometry = JTS.transform(geometry, projectToTargetTransform);
                    }
                } else if (transform != null) {
                    geometry = JTS.transform(geometry, transform);
                }
            }
            transformedRows.add(new TransformFeatureRow(feature, geometry));
            if (geometry != null && !geometry.isEmpty()) {
                transformedGeometries.add(geometry);
            }
        }

        Class<? extends Geometry> geometryBinding = VectorLayerUtils.resolveConcreteGeometryBinding(
                transformedGeometries,
                resolveSchemaGeometryBinding(sourceType)
        );
        if (geometryBinding == null || Geometry.class.equals(geometryBinding)) {
            geometryBinding = resolveSchemaGeometryBinding(sourceType);
        }
        if (geometryBinding == null) {
            geometryBinding = Geometry.class;
        }

        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName(safeTypeName(layer.getName()));

        boolean crsSet = false;

        for (AttributeDescriptor descriptor : sourceType.getAttributeDescriptors()) {
            if (descriptor instanceof GeometryDescriptor) {
                GeometryDescriptor gd = (GeometryDescriptor) descriptor;
                if (!crsSet) {
                    typeBuilder.setCRS(targetCRS);
                    crsSet = true;
                }
                typeBuilder.add(gd.getLocalName(), geometryBinding);
            } else {
                FieldConfig config = layer != null ? layer.getFieldConfigs().get(descriptor.getLocalName()) : null;
                Class<?> binding = config != null
                        ? DrawFeatureBuilder.resolveAttributeClass(config.getTypeName())
                        : descriptor.getType().getBinding();
                if (config != null && config.getLength() > 0) {
                    typeBuilder.length(config.getLength());
                }
                typeBuilder.add(descriptor.getLocalName(), binding != null ? binding : String.class);
            }
        }

        SimpleFeatureType targetType = typeBuilder.buildFeatureType();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(targetType);

        List<SimpleFeature> output = new ArrayList<>();
        int id = 1;

        for (TransformFeatureRow row : transformedRows) {
            SimpleFeature feature = row.feature;
            for (AttributeDescriptor descriptor : sourceType.getAttributeDescriptors()) {
                String name = descriptor.getLocalName();
                Object value = feature.getAttribute(name);

                if (descriptor instanceof GeometryDescriptor) {
                    Geometry geometry = row.geometry;
                    if (geometry != null) {
                        geometry = normalizeGeometryForExport(geometry, geometryBinding, outputOption);
                    }
                    builder.add(geometry);
                } else {
                    Class<?> targetBinding = targetType.getDescriptor(name) != null
                            && targetType.getDescriptor(name).getType() != null
                            ? targetType.getDescriptor(name).getType().getBinding()
                            : String.class;
                    builder.add(coerceAttributeValue(value, targetBinding));
                }
            }

            output.add(builder.buildFeature(String.valueOf(id++)));
            builder.reset();
        }

        return new TransformResult(targetType, output);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Geometry> resolveSchemaGeometryBinding(SimpleFeatureType schema) {
        if (schema == null || schema.getGeometryDescriptor() == null || schema.getGeometryDescriptor().getType() == null) {
            return Geometry.class;
        }
        Class<?> binding = schema.getGeometryDescriptor().getType().getBinding();
        if (binding != null && Geometry.class.isAssignableFrom(binding)) {
            return (Class<? extends Geometry>) binding;
        }
        return Geometry.class;
    }

    private static Geometry normalizeGeometryForExport(Geometry geometry,
                                                       Class<? extends Geometry> targetBinding,
                                                       String option) {
        if (geometry == null || geometry.isEmpty()) {
            return geometry;
        }
        Geometry normalized = VectorLayerUtils.normalizeGeometryForBinding(geometry, targetBinding);
        if (normalized == null && SHAPEFILE_OPTION.equals(option)) {
            throw new RuntimeException(
                    "La capa tiene geometrias incompatibles para exportar a Shapefile como "
                            + VectorLayerUtils.describeGeometryBinding(targetBinding)
                            + "."
            );
        }
        return normalized != null ? normalized : geometry;
    }

    private static Geometry geometryOf(SimpleFeature feature) {
        if (feature == null) {
            return null;
        }
        Object geometry = feature.getDefaultGeometry();
        return geometry instanceof Geometry ? (Geometry) geometry : null;
    }

    private static Object coerceAttributeValue(Object value, Class<?> targetBinding) {
        if (targetBinding == null || value == null) {
            return value;
        }
        if (targetBinding.isInstance(value)) {
            return value;
        }

        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return String.class.isAssignableFrom(targetBinding) ? "" : null;
        }

        try {
            if (String.class.isAssignableFrom(targetBinding)) {
                return text;
            }
            if (Integer.class.isAssignableFrom(targetBinding) || int.class.isAssignableFrom(targetBinding)) {
                return Integer.parseInt(text);
            }
            if (Long.class.isAssignableFrom(targetBinding) || long.class.isAssignableFrom(targetBinding)) {
                return Long.parseLong(text);
            }
            if (Float.class.isAssignableFrom(targetBinding) || float.class.isAssignableFrom(targetBinding)) {
                return Float.parseFloat(text.replace(',', '.'));
            }
            if (Double.class.isAssignableFrom(targetBinding) || double.class.isAssignableFrom(targetBinding)) {
                return Double.parseDouble(text.replace(',', '.'));
            }
            if (Boolean.class.isAssignableFrom(targetBinding) || boolean.class.isAssignableFrom(targetBinding)) {
                return "true".equalsIgnoreCase(text) || "1".equals(text) || "si".equalsIgnoreCase(text);
            }
            if (java.util.Date.class.isAssignableFrom(targetBinding)) {
                return value instanceof java.util.Date ? value : null;
            }
        } catch (Exception ignored) { CatgisLogger.warn("Error al convertir atributo en exportacion", ignored); }

        return String.class.isAssignableFrom(targetBinding) ? text : null;
    }

    public static boolean hasExportableVectorData(ShapefileData data) {
        if (data == null) {
            return false;
        }
        if (data.getSchema() == null) {
            return false;
        }
        return data.getFeatures() != null;
    }

    private static boolean requiresKmlOptions(String option) {
        return KML_OPTION.equals(option) || KMZ_OPTION.equals(option);
    }

    private static KmlExportOptions promptKmlExportOptions(Layer layer, ShapefileData data, Component parent) {
        List<String> availableFields = data != null ? data.getAttributeNames() : List.of();
        JComboBox<String> labelCombo = new JComboBox<>();
        labelCombo.addItem("<sin etiquetas>");
        for (String field : availableFields) {
            labelCombo.addItem(field);
        }

        JCheckBox exportLabelsCheck = new JCheckBox("Exportar etiquetas");
        String suggestedField = layer != null ? layer.getLabelField() : "";
        if (suggestedField != null && !suggestedField.isBlank() && availableFields.contains(suggestedField)) {
            exportLabelsCheck.setSelected(true);
            labelCombo.setSelectedItem(suggestedField);
        } else {
            exportLabelsCheck.setSelected(false);
            labelCombo.setSelectedIndex(0);
        }
        labelCombo.setEnabled(exportLabelsCheck.isSelected());
        exportLabelsCheck.addActionListener(e -> labelCombo.setEnabled(exportLabelsCheck.isSelected()));

        JCheckBox includeDescriptionCheck = new JCheckBox("Incluir descripcion con atributos", true);

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Salida KML/KMZ"));
        panel.add(exportLabelsCheck);

        JPanel labelRow = new JPanel(new BorderLayout(6, 0));
        labelRow.add(new JLabel("Campo etiqueta"), BorderLayout.WEST);
        labelRow.add(labelCombo, BorderLayout.CENTER);
        panel.add(labelRow);
        panel.add(includeDescriptionCheck);

        int choice = JOptionPane.showConfirmDialog(
                parent,
                panel,
                "Opciones KML / KMZ",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (choice != JOptionPane.OK_OPTION) {
            return null;
        }

        String labelField = null;
        if (exportLabelsCheck.isSelected() && labelCombo.getSelectedItem() != null) {
            String selected = String.valueOf(labelCombo.getSelectedItem());
            if (!selected.startsWith("<")) {
                labelField = selected;
            }
        }

        return new KmlExportOptions(exportLabelsCheck.isSelected(), labelField, includeDescriptionCheck.isSelected());
    }

    private static KmlExportOptions resolveKmlExportOptions(Layer layer, ShapefileData data, Component parent) {
        if (parent == null || GraphicsEnvironment.isHeadless()) {
            String labelField = layer != null && layer.getLabelField() != null && !layer.getLabelField().isBlank()
                    ? layer.getLabelField()
                    : resolveSuggestedLabelField(data);
            boolean exportLabels = labelField != null && !labelField.isBlank();
            return new KmlExportOptions(exportLabels, labelField, true);
        }
        return promptKmlExportOptions(layer, data, parent);
    }

    private static void showExportError(Component parent, Exception ex) {
        if (!GraphicsEnvironment.isHeadless()) {
            AppErrorSupport.showErrorDialog(parent, "Exportar capa", "Error al exportar capa.", ex);
        }
    }

    private static String resolveSuggestedLabelField(ShapefileData data) {
        List<String> fields = data != null ? data.getAttributeNames() : List.of();
        if (fields.isEmpty()) {
            return null;
        }

        for (String candidate : List.of("name", "nombre", "label", "titulo", "title", "codigo", "id")) {
            for (String field : fields) {
                if (field != null && field.equalsIgnoreCase(candidate)) {
                    return field;
                }
            }
        }

        return fields.get(0);
    }

    private static String getProjectCRSCode() {
        if (CatgisDesktopApp.currentProject != null &&
                CatgisDesktopApp.currentProject.getProjectCRS() != null &&
                !CatgisDesktopApp.currentProject.getProjectCRS().isBlank()) {
            return CRSDefinitions.normalizeCode(CatgisDesktopApp.currentProject.getProjectCRS());
        }
        return "EPSG:4326";
    }

    private static String normalizeLayerCRS(Layer layer) {
        if (layer == null || layer.getSourceCRS() == null) {
            return "";
        }
        return CRSDefinitions.normalizeCode(layer.getSourceCRS());
    }

    private static String resolveTargetCode(Layer layer, String option) {
        return resolveTargetCode(layer, option, null);
    }

    private static String resolveTargetCode(Layer layer, String option, String requestedTargetCode) {
        if (KML_OPTION.equals(option) || KMZ_OPTION.equals(option) || GPX_OPTION.equals(option)) {
            return "EPSG:4326";
        }
        if (requestedTargetCode != null && !requestedTargetCode.isBlank()) {
            return CRSDefinitions.normalizeCode(requestedTargetCode);
        }
        String sourceCode = normalizeLayerCRS(layer);
        if (!sourceCode.isBlank()) {
            return sourceCode;
        }
        return getProjectCRSCode();
    }

    private static String safeFileName(String text) {
        if (text == null || text.isBlank()) {
            return "capa";
        }
        return text.replaceAll("[\\\\/:*?\"<>|]+", "_").trim();
    }

    static String safeTypeName(String text) {
        String name = safeFileName(text).replaceAll("\\s+", "_");
        if (name.isBlank()) {
            name = "layer";
        }
        if (Character.isDigit(name.charAt(0))) {
            name = "layer_" + name;
        }
        return name;
    }

    private static String getFeatureName(SimpleFeature feature, Layer layer, KmlExportOptions options) {
        if (feature == null || options == null || !options.exportLabels()) {
            return null;
        }

        if (options.labelField() != null && !options.labelField().isBlank()) {
            Object value = feature.getAttribute(options.labelField());
            return value != null ? String.valueOf(value) : null;
        }

        if (layer != null && layer.getLabelField() != null && !layer.getLabelField().isBlank()) {
            Object value = feature.getAttribute(layer.getLabelField());
            if (value != null) {
                return String.valueOf(value);
            }
        }

        for (AttributeDescriptor descriptor : feature.getFeatureType().getAttributeDescriptors()) {
            if (!(descriptor instanceof GeometryDescriptor)) {
                Object value = feature.getAttribute(descriptor.getLocalName());
                if (value != null && !String.valueOf(value).isBlank()) {
                    return String.valueOf(value);
                }
            }
        }

        return feature.getID();
    }

    private static String getFeatureName(SimpleFeature feature, Layer layer) {
        return getFeatureName(feature, layer, new KmlExportOptions(true, null, true));
    }

    private static void writeKmlStyle(BufferedWriter writer, Layer layer, KmlExportOptions options) throws Exception {
        writer.write("      <LabelStyle><scale>" + (options.exportLabels() ? "1.0" : "0.0") + "</scale></LabelStyle>");
        writer.newLine();

        String lineColor = toKmlColor(layer != null ? layer.getLineColor() : null, "ff0000ff");
        String polygonLine = toKmlColor(layer != null ? layer.getBorderColor() : null, lineColor);
        String fillColor = toKmlColor(layer != null ? layer.getFillColor() : null, "7fffaa55");
        String pointColor = toKmlColor(layer != null ? layer.getPointColor() : null, lineColor);
        float lineWidth = layer != null ? Math.max(1f, layer.getLineWidth()) : 1.5f;
        double pointScale = layer != null ? Math.max(0.8d, layer.getPointSize() / 8d) : 1.0d;

        writer.write("      <IconStyle><color>" + pointColor + "</color><scale>" + String.format(Locale.US, "%.2f", pointScale) + "</scale></IconStyle>");
        writer.newLine();
        writer.write("      <LineStyle><color>" + polygonLine + "</color><width>" + String.format(Locale.US, "%.2f", lineWidth) + "</width></LineStyle>");
        writer.newLine();
        writer.write("      <PolyStyle><color>" + fillColor + "</color><outline>1</outline></PolyStyle>");
        writer.newLine();
    }

    private static void writeKmlExtendedData(BufferedWriter writer, SimpleFeature feature) throws Exception {
        if (feature == null || feature.getFeatureType() == null) {
            return;
        }
        writer.write("      <ExtendedData>");
        writer.newLine();
        for (AttributeDescriptor descriptor : feature.getFeatureType().getAttributeDescriptors()) {
            if (descriptor instanceof GeometryDescriptor) {
                continue;
            }
            String name = descriptor.getLocalName();
            Object value = feature.getAttribute(name);
            writer.write("        <Data name=\"" + xml(name) + "\"><value>" + xml(value != null ? String.valueOf(value) : "") + "</value></Data>");
            writer.newLine();
        }
        writer.write("      </ExtendedData>");
        writer.newLine();
    }

    private static String toKmlColor(java.awt.Color color, String fallback) {
        if (color == null) {
            return fallback;
        }
        return String.format(Locale.US, "%02x%02x%02x%02x",
                color.getAlpha(),
                color.getBlue(),
                color.getGreen(),
                color.getRed());
    }

    private static String buildDescription(SimpleFeature feature) {
        StringBuilder sb = new StringBuilder();

        for (AttributeDescriptor descriptor : feature.getFeatureType().getAttributeDescriptors()) {
            if (descriptor instanceof GeometryDescriptor) {
                continue;
            }

            String name = descriptor.getLocalName();
            Object value = feature.getAttribute(name);

            sb.append(name).append(": ").append(value != null ? value : "").append("\n");
        }

        return sb.toString().trim();
    }

    private static void writeKmlGeometry(BufferedWriter writer, Geometry geometry, String indent) throws Exception {
        String type = geometry.getGeometryType();

        if ("Point".equalsIgnoreCase(type)) {
            Coordinate c = geometry.getCoordinate();
            writer.write(indent + "<Point><coordinates>" + c.x + "," + c.y + ",0</coordinates></Point>");
            writer.newLine();
            return;
        }

        if ("LineString".equalsIgnoreCase(type)) {
            writer.write(indent + "<LineString><coordinates>" + buildCoordinateString(geometry.getCoordinates()) + "</coordinates></LineString>");
            writer.newLine();
            return;
        }

        if ("Polygon".equalsIgnoreCase(type)) {
            Polygon polygon = (Polygon) geometry;
            writer.write(indent + "<Polygon>");
            writer.newLine();
            writer.write(indent + "  <outerBoundaryIs><LinearRing><coordinates>"
                    + buildCoordinateString(polygon.getExteriorRing().getCoordinates())
                    + "</coordinates></LinearRing></outerBoundaryIs>");
            writer.newLine();

            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                writer.write(indent + "  <innerBoundaryIs><LinearRing><coordinates>"
                        + buildCoordinateString(polygon.getInteriorRingN(i).getCoordinates())
                        + "</coordinates></LinearRing></innerBoundaryIs>");
                writer.newLine();
            }

            writer.write(indent + "</Polygon>");
            writer.newLine();
            return;
        }

        if (geometry.getNumGeometries() > 1) {
            writer.write(indent + "<MultiGeometry>");
            writer.newLine();
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                writeKmlGeometry(writer, geometry.getGeometryN(i), indent + "  ");
            }
            writer.write(indent + "</MultiGeometry>");
            writer.newLine();
        }
    }

    private static void writeDxfFeature(BufferedWriter writer, SimpleFeature feature, String fallbackLayer) throws Exception {
        if (feature == null) {
            return;
        }
        Geometry geometry = geometryOf(feature);
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        DxfEntityStyle style = resolveDxfEntityStyle(feature, fallbackLayer);
        String entityType = resolveDxfEntityType(feature);
        String textValue = resolveDxfTextValue(feature);
        if (isTextEntityType(entityType) && textValue != null && !textValue.isBlank()) {
            Coordinate anchor = resolveDxfTextAnchor(geometry);
            if (anchor != null) {
                writeDxfText(writer, anchor, style, textValue, "MTEXT".equalsIgnoreCase(entityType));
                return;
            }
        }

        writeDxfGeometry(writer, geometry, style);
    }

    private static void writeDxfGeometry(BufferedWriter writer, Geometry geometry, DxfEntityStyle style) throws Exception {
        if (geometry == null || geometry.isEmpty()) {
            return;
        }
        if (geometry instanceof Point point) {
            writeDxfPoint(writer, point.getCoordinate(), style);
            return;
        }
        if (geometry instanceof LineString lineString) {
            writeDxfPolyline(writer, lineString.getCoordinates(), lineString.isClosed(), style);
            return;
        }
        if (geometry instanceof Polygon polygon) {
            writeDxfPolyline(writer, polygon.getExteriorRing().getCoordinates(), true, style);
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                writeDxfPolyline(writer, polygon.getInteriorRingN(i).getCoordinates(), true, style);
            }
            return;
        }
        if (geometry instanceof MultiPoint multiPoint) {
            for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
                writeDxfGeometry(writer, multiPoint.getGeometryN(i), style);
            }
            return;
        }
        if (geometry instanceof MultiLineString multiLineString) {
            for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
                writeDxfGeometry(writer, multiLineString.getGeometryN(i), style);
            }
            return;
        }
        if (geometry instanceof MultiPolygon multiPolygon) {
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                writeDxfGeometry(writer, multiPolygon.getGeometryN(i), style);
            }
            return;
        }
        if (geometry instanceof GeometryCollection collection) {
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                writeDxfGeometry(writer, collection.getGeometryN(i), style);
            }
        }
    }

    private static void writeDxfPoint(BufferedWriter writer, Coordinate coordinate, DxfEntityStyle style) throws Exception {
        if (coordinate == null || !isFiniteCoordinate(coordinate)) {
            return;
        }
        writeDxfPair(writer, "0", "POINT");
        writeDxfStyle(writer, style);
        writeDxfPair(writer, "10", dxfNumber(coordinate.x));
        writeDxfPair(writer, "20", dxfNumber(coordinate.y));
        writeDxfPair(writer, "30", dxfNumber(resolveCoordinateZ(coordinate, style)));
    }

    private static void writeDxfPolyline(BufferedWriter writer,
                                         Coordinate[] coordinates,
                                         boolean closed,
                                         DxfEntityStyle style) throws Exception {
        if (coordinates == null || coordinates.length < 2) {
            return;
        }
        List<Coordinate> cleaned = new ArrayList<>();
        for (Coordinate coordinate : coordinates) {
            if (coordinate != null && isFiniteCoordinate(coordinate)) {
                cleaned.add(coordinate);
            }
        }
        if (cleaned.size() < 2) {
            return;
        }
        if (cleaned.size() >= 2 && almostSameCoordinate(cleaned.get(0), cleaned.get(cleaned.size() - 1))) {
            cleaned.remove(cleaned.size() - 1);
        }
        if (cleaned.size() < 2) {
            return;
        }

        writeDxfPair(writer, "0", "LWPOLYLINE");
        writeDxfStyle(writer, style);
        writeDxfPair(writer, "90", String.valueOf(cleaned.size()));
        writeDxfPair(writer, "70", closed ? "1" : "0");
        if (style != null && style.elevation != null && Double.isFinite(style.elevation)) {
            writeDxfPair(writer, "38", dxfNumber(style.elevation));
        }
        for (Coordinate coordinate : cleaned) {
            writeDxfPair(writer, "10", dxfNumber(coordinate.x));
            writeDxfPair(writer, "20", dxfNumber(coordinate.y));
        }
    }

    private static void writeDxfText(BufferedWriter writer,
                                     Coordinate coordinate,
                                     DxfEntityStyle style,
                                     String text,
                                     boolean multiline) throws Exception {
        if (coordinate == null || !isFiniteCoordinate(coordinate)) {
            return;
        }
        String normalized = normalizeDxfText(text, multiline);
        if (normalized.isBlank()) {
            return;
        }

        writeDxfPair(writer, "0", multiline ? "MTEXT" : "TEXT");
        writeDxfStyle(writer, style);
        writeDxfPair(writer, "10", dxfNumber(coordinate.x));
        writeDxfPair(writer, "20", dxfNumber(coordinate.y));
        writeDxfPair(writer, "30", dxfNumber(resolveCoordinateZ(coordinate, style)));
        writeDxfPair(writer, "40", "2.5");
        if (multiline) {
            writeDxfPair(writer, "71", "1");
            writeDxfPair(writer, "72", "5");
            writeDxfPair(writer, "1", normalized);
        } else {
            writeDxfPair(writer, "7", "Standard");
            writeDxfPair(writer, "1", normalized);
        }
    }

    private static void writeDxfStyle(BufferedWriter writer, DxfEntityStyle style) throws Exception {
        String layerName = style != null ? style.layerName : null;
        writeDxfPair(writer, "8", sanitizeCadLayerName(layerName));
        if (style != null && style.lineType != null && !style.lineType.isBlank()) {
            writeDxfPair(writer, "6", style.lineType.trim());
        }
        if (style != null && style.colorIndex != null) {
            writeDxfPair(writer, "62", String.valueOf(style.colorIndex));
        }
    }

    private static DxfEntityStyle resolveDxfEntityStyle(SimpleFeature feature, String fallbackLayer) {
        String cadLayer = resolveCadLayerName(feature, fallbackLayer);
        Integer cadColor = resolveCadColor(feature);
        String cadLineType = resolveCadLineType(feature);
        Double cadElevation = resolveCadElevation(feature);
        return new DxfEntityStyle(cadLayer, cadColor, cadLineType, cadElevation);
    }

    private static Integer resolveCadColor(SimpleFeature feature) {
        Object value = firstPresentAttribute(feature, "cad_color", "color", "aci_color");
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar color CAD en exportacion", ignored); return null; }
    }

    private static String resolveCadLineType(SimpleFeature feature) {
        Object value = firstPresentAttribute(feature, "cad_ltype", "linetype");
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).trim();
        return text;
    }

    private static Double resolveCadElevation(SimpleFeature feature) {
        Object value = firstPresentAttribute(feature, "elev_z", "elevation", "z");
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(String.valueOf(value).trim().replace(',', '.'));
        } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar ancho de linea CAD en exportacion", ignored); return null; }
    }

    private static String resolveDxfEntityType(SimpleFeature feature) {
        Object value = firstPresentAttribute(feature, "entity_type", "type");
        if (value == null) {
            return "";
        }
        return String.valueOf(value).trim().toUpperCase(Locale.ROOT);
    }

    private static String resolveDxfTextValue(SimpleFeature feature) {
        Object value = firstPresentAttribute(feature, "text", "label", "name", "nombre", "value");
        if (value == null) {
            return "";
        }
        return String.valueOf(value).trim();
    }

    private static Coordinate resolveDxfTextAnchor(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        if (geometry instanceof Point point) {
            return point.getCoordinate();
        }
        Coordinate coordinate = geometry.getCoordinate();
        if (coordinate != null && isFiniteCoordinate(coordinate)) {
            return coordinate;
        }
        Geometry centroid = geometry.getCentroid();
        if (centroid instanceof Point point) {
            return point.getCoordinate();
        }
        return null;
    }

    private static boolean isTextEntityType(String entityType) {
        if (entityType == null || entityType.isBlank()) {
            return false;
        }
        return "TEXT".equalsIgnoreCase(entityType)
                || "MTEXT".equalsIgnoreCase(entityType)
                || "ATTRIB".equalsIgnoreCase(entityType);
    }

    private static String normalizeDxfText(String text, boolean multiline) {
        if (text == null) {
            return "";
        }
        String cleaned = text
                .replace('\u0000', ' ')
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .trim();
        if (cleaned.isBlank()) {
            return "";
        }
        if (multiline) {
            return cleaned.replace("\n", "\\P");
        }
        return cleaned.replace('\n', ' ').replaceAll("\\s+", " ");
    }

    private static double resolveCoordinateZ(Coordinate coordinate, DxfEntityStyle style) {
        if (coordinate != null && Double.isFinite(coordinate.getZ())) {
            return coordinate.getZ();
        }
        if (style != null && style.elevation != null && Double.isFinite(style.elevation)) {
            return style.elevation;
        }
        return 0d;
    }

    private static Object firstPresentAttribute(SimpleFeature feature, String... names) {
        if (feature == null || names == null || names.length == 0) {
            return null;
        }
        for (String name : names) {
            if (name == null || name.isBlank()) {
                continue;
            }
            if (feature.getFeatureType() == null || feature.getFeatureType().getDescriptor(name) == null) {
                continue;
            }
            Object value = feature.getAttribute(name);
            if (value == null) {
                continue;
            }
            if (value instanceof String text && text.isBlank()) {
                continue;
            }
            return value;
        }
        return null;
    }

    private static String resolveCadLayerName(SimpleFeature feature, String fallback) {
        String base = fallback != null && !fallback.isBlank() ? fallback : "CATGIS";
        if (feature == null) {
            return sanitizeCadLayerName(base);
        }
        Object cadLayerAttr = feature.getAttribute("cad_layer");
        if (cadLayerAttr == null) {
            cadLayerAttr = feature.getAttribute("layer");
        }
        String candidate = cadLayerAttr != null ? String.valueOf(cadLayerAttr).trim() : "";
        if (candidate.isBlank()) {
            return sanitizeCadLayerName(base);
        }
        return sanitizeCadLayerName(candidate);
    }

    private static String sanitizeCadLayerName(String name) {
        String normalized = name != null ? name.trim() : "";
        if (normalized.isBlank()) {
            return "CATGIS";
        }
        normalized = normalized.replace('\r', ' ').replace('\n', ' ');
        if (normalized.length() > 60) {
            normalized = normalized.substring(0, 60);
        }
        return normalized;
    }

    private static void writeDxfPair(BufferedWriter writer, String code, String value) throws Exception {
        writer.write(code != null ? code : "");
        writer.newLine();
        writer.write(value != null ? value : "");
        writer.newLine();
    }

    private static String dxfNumber(double value) {
        return String.format(Locale.US, "%.6f", value);
    }

    private static boolean isFiniteCoordinate(Coordinate coordinate) {
        return coordinate != null
                && Double.isFinite(coordinate.x)
                && Double.isFinite(coordinate.y);
    }

    private static boolean almostSameCoordinate(Coordinate a, Coordinate b) {
        if (a == null || b == null) {
            return false;
        }
        return Math.abs(a.x - b.x) < 1e-9 && Math.abs(a.y - b.y) < 1e-9;
    }

    private static class DxfEntityStyle {
        final String layerName;
        final Integer colorIndex;
        final String lineType;
        final Double elevation;

        private DxfEntityStyle(String layerName, Integer colorIndex, String lineType, Double elevation) {
            this.layerName = layerName;
            this.colorIndex = colorIndex;
            this.lineType = lineType;
            this.elevation = elevation;
        }
    }

    private static String buildCoordinateString(Coordinate[] coordinates) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < coordinates.length; i++) {
            Coordinate c = coordinates[i];
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(c.x).append(",").append(c.y).append(",0");
        }
        return sb.toString();
    }

    private static String xml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static String extensionForOption(String option) {
        if (SHAPEFILE_OPTION.equals(option)) {
            return ".shp";
        }
        if (DXF_OPTION.equals(option)) {
            return ".dxf";
        }
        if (GEOJSON_OPTION.equals(option)) {
            return ".geojson";
        }
        if (KMZ_OPTION.equals(option)) {
            return ".kmz";
        }
        if (GPX_OPTION.equals(option)) {
            return ".gpx";
        }
        return ".kml";
    }

    private static String optionForPath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String lower = path.trim().toLowerCase(Locale.ROOT);
        if (lower.endsWith(".shp")) {
            return SHAPEFILE_OPTION;
        }
        if (lower.endsWith(".dxf")) {
            return DXF_OPTION;
        }
        if (lower.endsWith(".geojson") || lower.endsWith(".json")) {
            return GEOJSON_OPTION;
        }
        if (lower.endsWith(".kml")) {
            return KML_OPTION;
        }
        if (lower.endsWith(".kmz")) {
            return KMZ_OPTION;
        }
        if (lower.endsWith(".gpx")) {
            return GPX_OPTION;
        }
        return null;
    }

    private record KmlExportOptions(boolean exportLabels, String labelField, boolean includeDescription) {
        private static KmlExportOptions defaults() {
            return new KmlExportOptions(false, null, true);
        }
    }

    private static void deleteShapefileSidecars(File shpFile) {
        String absolutePath = shpFile.getAbsolutePath();
        int dot = absolutePath.lastIndexOf('.');
        String base = dot > 0 ? absolutePath.substring(0, dot) : absolutePath;
        String[] extensions = {".shp", ".shx", ".dbf", ".prj", ".fix", ".qix", ".cpg"};
        for (String extension : extensions) {
            File candidate = new File(base + extension);
            if (candidate.exists() && !candidate.delete()) {
                throw new RuntimeException("No se pudo reemplazar el archivo existente: " + candidate.getAbsolutePath());
            }
        }
    }

    public static void refreshLayerFromFile(Layer layer, File file, String sourceCrsOverride) throws Exception {
        if (layer == null || file == null) {
            return;
        }

        String lower = file.getName().toLowerCase(Locale.ROOT);
        ShapefileData reloaded;
        String sourceCRS = layer.getSourceCRS();

        if (lower.endsWith(".shp")) {
            reloaded = ShapefileLoader.load(file);
            String detected = ShapefileLoader.getCRSCode(file);
            if (detected != null && !detected.isBlank()) {
                sourceCRS = detected;
            }
        } else if (lower.endsWith(".dxf")) {
            reloaded = DxfLoader.load(file);
            if (sourceCrsOverride != null && !sourceCrsOverride.isBlank()) {
                sourceCRS = CRSDefinitions.normalizeCode(sourceCrsOverride);
            } else if (sourceCRS == null || sourceCRS.isBlank()) {
                sourceCRS = getProjectCRSCode();
            }
        } else if (lower.endsWith(".geojson") || lower.endsWith(".json")) {
            reloaded = GeoJsonLoader.load(file);
            if (sourceCRS == null || sourceCRS.isBlank()) {
                sourceCRS = "EPSG:4326";
            }
        } else if (lower.endsWith(".kml") || lower.endsWith(".kmz")) {
            reloaded = KmlLoader.load(file);
            sourceCRS = "EPSG:4326";
        } else if (lower.endsWith(".gpx")) {
            if (layer instanceof GpxLayer gpxLayer) {
                reloaded = GpxLoader.load(file, gpxLayer.getContentKind());
            } else {
                GpxImportResult importResult = GpxLoader.load(file);
                reloaded = pickFirstGpxData(importResult);
            }
            sourceCRS = "EPSG:4326";
        } else {
            throw new RuntimeException("Formato vectorial no soportado: " + file.getAbsolutePath());
        }

        layer.setPath(file.getAbsolutePath());
        layer.setSourceName(file.getName());
        layer.setFeatureCount(reloaded.getFeatureCount());
        layer.setSourceCRS(sourceCRS);
        if (looksTemporaryLayerName(layer.getName())) {
            layer.setName(stripExtension(file.getName()));
        }

        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(layer, reloaded);
            CatgisDesktopApp.mapPanel.repaint();
        }
        if (CatgisDesktopApp.layersPanel != null) {
            CatgisDesktopApp.layersPanel.refreshLayerList();
        }
    }

    private static boolean looksTemporaryLayerName(String name) {
        if (name == null) {
            return false;
        }
        return name.startsWith("Dibujo_")
                || name.startsWith("Pines_")
                || name.startsWith("Consulta_");
    }

    private static String stripExtension(String name) {
        if (name == null || name.isBlank()) {
            return "capa";
        }
        int idx = name.lastIndexOf('.');
        return idx > 0 ? name.substring(0, idx) : name;
    }

    private static ShapefileData pickFirstGpxData(GpxImportResult importResult) {
        if (importResult == null) {
            throw new RuntimeException("No se pudo recargar el GPX exportado.");
        }
        if (GpxImportResult.hasFeatures(importResult.get(GpxLayer.ContentKind.WAYPOINTS))) {
            return importResult.get(GpxLayer.ContentKind.WAYPOINTS);
        }
        if (GpxImportResult.hasFeatures(importResult.get(GpxLayer.ContentKind.TRACKS))) {
            return importResult.get(GpxLayer.ContentKind.TRACKS);
        }
        if (GpxImportResult.hasFeatures(importResult.get(GpxLayer.ContentKind.ROUTES))) {
            return importResult.get(GpxLayer.ContentKind.ROUTES);
        }
        throw new RuntimeException("El GPX exportado no contiene entidades utilizables.");
    }

    private static int writeGpxWaypoints(BufferedWriter writer,
                                         SimpleFeature feature,
                                         Geometry geometry,
                                         int nextIndex) throws Exception {
        List<Point> points = new ArrayList<>();
        collectPoints(geometry, points);
        for (Point point : points) {
            Coordinate coordinate = point.getCoordinate();
            if (coordinate == null) {
                continue;
            }
            String fallbackName = buildIndexedName(feature, "waypoint", nextIndex);
            writer.write("  <wpt lat=\"" + coordinate.y + "\" lon=\"" + coordinate.x + "\">");
            writer.newLine();
            writeGpxMetadata(writer, feature, fallbackName, "    ");
            writer.write("  </wpt>");
            writer.newLine();
            nextIndex++;
        }
        return nextIndex;
    }

    private static int writeGpxTracks(BufferedWriter writer,
                                      SimpleFeature feature,
                                      Geometry geometry,
                                      int nextIndex) throws Exception {
        List<LineString> lines = new ArrayList<>();
        collectLines(geometry, lines);
        if (lines.isEmpty()) {
            return nextIndex;
        }

        String fallbackName = buildIndexedName(feature, "track", nextIndex);
        writer.write("  <trk>");
        writer.newLine();
        writeGpxMetadata(writer, feature, fallbackName, "    ");
        for (LineString line : lines) {
            if (line == null || line.isEmpty() || line.getNumPoints() < 2) {
                continue;
            }
            writer.write("    <trkseg>");
            writer.newLine();
            for (Coordinate coordinate : line.getCoordinates()) {
                writer.write("      <trkpt lat=\"" + coordinate.y + "\" lon=\"" + coordinate.x + "\"></trkpt>");
                writer.newLine();
            }
            writer.write("    </trkseg>");
            writer.newLine();
        }
        writer.write("  </trk>");
        writer.newLine();
        return nextIndex + 1;
    }

    private static int writeGpxPolygonBoundaries(BufferedWriter writer,
                                                 SimpleFeature feature,
                                                 Geometry geometry,
                                                 int nextIndex) throws Exception {
        List<Polygon> polygons = new ArrayList<>();
        collectPolygons(geometry, polygons);
        for (Polygon polygon : polygons) {
            if (polygon == null || polygon.isEmpty()) {
                continue;
            }
            writer.write("  <trk>");
            writer.newLine();
            String fallbackName = buildIndexedName(feature, "polygon_boundary", nextIndex);
            writeGpxMetadata(writer, feature, fallbackName, "    ");
            writePolygonRingAsTrack(writer, polygon.getExteriorRing().getCoordinates());
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                writePolygonRingAsTrack(writer, polygon.getInteriorRingN(i).getCoordinates());
            }
            writer.write("  </trk>");
            writer.newLine();
            nextIndex++;
        }
        return nextIndex;
    }

    private static void writePolygonRingAsTrack(BufferedWriter writer, Coordinate[] coordinates) throws Exception {
        if (coordinates == null || coordinates.length < 4) {
            return;
        }
        writer.write("    <trkseg>");
        writer.newLine();
        for (Coordinate coordinate : coordinates) {
            writer.write("      <trkpt lat=\"" + coordinate.y + "\" lon=\"" + coordinate.x + "\"></trkpt>");
            writer.newLine();
        }
        writer.write("    </trkseg>");
        writer.newLine();
    }

    private static void writeGpxMetadata(BufferedWriter writer,
                                         SimpleFeature feature,
                                         String fallbackName,
                                         String indent) throws Exception {
        String name = getFeatureName(feature, null);
        if (name == null || name.isBlank() || name.equals(feature.getID())) {
            name = fallbackName;
        }
        writeOptionalGpxTag(writer, indent, "name", name);
        writeOptionalGpxTag(writer, indent, "desc", buildDescription(feature));

        Object typeValue = null;
        if (feature.getFeatureType() != null) {
            if (feature.getFeatureType().getDescriptor("type_name") != null) {
                typeValue = feature.getAttribute("type_name");
            } else if (feature.getFeatureType().getDescriptor("tipo") != null) {
                typeValue = feature.getAttribute("tipo");
            }
        }
        if (typeValue != null) {
            writeOptionalGpxTag(writer, indent, "type", String.valueOf(typeValue));
        }
    }

    private static void writeOptionalGpxTag(BufferedWriter writer, String indent, String tag, String value) throws Exception {
        if (value == null || value.isBlank()) {
            return;
        }
        writer.write(indent + "<" + tag + ">" + xml(value) + "</" + tag + ">");
        writer.newLine();
    }

    private static String buildIndexedName(SimpleFeature feature, String fallbackPrefix, int index) {
        String name = getFeatureName(feature, null);
        if (name != null && !name.isBlank() && !name.equals(feature.getID())) {
            return name;
        }
        return fallbackPrefix + " " + index;
    }

    private static void collectPoints(Geometry geometry, List<Point> points) {
        if (geometry == null || points == null) {
            return;
        }
        if (geometry instanceof Point point) {
            points.add(point);
            return;
        }
        if (geometry instanceof MultiPoint multiPoint) {
            for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
                collectPoints(multiPoint.getGeometryN(i), points);
            }
            return;
        }
        if (geometry instanceof GeometryCollection collection) {
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                collectPoints(collection.getGeometryN(i), points);
            }
        }
    }

    private static void collectLines(Geometry geometry, List<LineString> lines) {
        if (geometry == null || lines == null) {
            return;
        }
        if (geometry instanceof LineString lineString) {
            lines.add(lineString);
            return;
        }
        if (geometry instanceof MultiLineString multiLineString) {
            for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
                collectLines(multiLineString.getGeometryN(i), lines);
            }
            return;
        }
        if (geometry instanceof GeometryCollection collection) {
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                collectLines(collection.getGeometryN(i), lines);
            }
        }
    }

    private static void collectPolygons(Geometry geometry, List<Polygon> polygons) {
        if (geometry == null || polygons == null) {
            return;
        }
        if (geometry instanceof Polygon polygon) {
            polygons.add(polygon);
            return;
        }
        if (geometry instanceof MultiPolygon multiPolygon) {
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                collectPolygons(multiPolygon.getGeometryN(i), polygons);
            }
            return;
        }
        if (geometry instanceof GeometryCollection collection) {
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                collectPolygons(collection.getGeometryN(i), polygons);
            }
        }
    }

    static class TransformResult {
        final SimpleFeatureType featureType;
        final List<SimpleFeature> features;

        TransformResult(SimpleFeatureType featureType, List<SimpleFeature> features) {
            this.featureType = featureType;
            this.features = features;
        }
    }

    private static class TransformFeatureRow {
        final SimpleFeature feature;
        final Geometry geometry;

        TransformFeatureRow(SimpleFeature feature, Geometry geometry) {
            this.feature = feature;
            this.geometry = geometry;
        }
    }
}
