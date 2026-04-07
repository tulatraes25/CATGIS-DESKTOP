package ar.com.catgis;

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
import org.locationtech.jts.geom.Polygon;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.BufferedWriter;
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

public class ExportVectorLayerAction {

    private static final String SHAPEFILE_OPTION = "Shapefile (*.shp)";
    private static final String GEOJSON_OPTION = "GeoJSON (*.geojson)";
    private static final String KML_OPTION = "KML (*.kml)";

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
        return new String[]{SHAPEFILE_OPTION, GEOJSON_OPTION, KML_OPTION};
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
                new String[]{SHAPEFILE_OPTION, GEOJSON_OPTION, KML_OPTION},
                SHAPEFILE_OPTION
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
        } else if (GEOJSON_OPTION.equals(option)) {
            chooser.setFileFilter(new FileNameExtensionFilter("GeoJSON (*.geojson)", "geojson"));
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
        } else if (GEOJSON_OPTION.equals(option)) {
            chooser.setFileFilter(new FileNameExtensionFilter("GeoJSON (*.geojson)", "geojson"));
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

        String option = optionForPath(layer.getPath());
        if (option == null) {
            return false;
        }

        return saveLayerToFile(layer, data, new File(layer.getPath()), option, parent, showSuccessMessage);
    }

    public static boolean hasSupportedVectorPath(Layer layer) {
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

            if (SHAPEFILE_OPTION.equals(option)) {
                deleteShapefileSidecars(file);
                exportToShapefile(layer, data, file, resolveTargetCode(layer, option, targetCode));
            } else if (GEOJSON_OPTION.equals(option)) {
                exportToGeoJson(layer, data, file, resolveTargetCode(layer, option, targetCode));
            } else {
                exportToKml(layer, data, file);
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
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error al exportar capa: " + ex.getMessage());
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

            if (SHAPEFILE_OPTION.equals(option)) {
                deleteShapefileSidecars(file);
                exportToShapefile(layer, data, file, resolveTargetCode(layer, option));
            } else if (GEOJSON_OPTION.equals(option)) {
                exportToGeoJson(layer, data, file, resolveTargetCode(layer, option));
            } else {
                exportToKml(layer, data, file);
            }

            refreshLayerFromFile(layer, file);

            if (showSuccessMessage) {
                JOptionPane.showMessageDialog(parent, "Capa exportada correctamente:\n" + file.getAbsolutePath());
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error al exportar capa: " + ex.getMessage());
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
            featureJSON.writeFeatureCollection(new ListFeatureCollection(featureType, features), writer);
        }
    }

    private static void exportToKml(Layer layer, ShapefileData data, File file) throws Exception {
        TransformResult transformResult = transformFeaturesToTarget(layer, data, "EPSG:4326", KML_OPTION);
        List<SimpleFeature> features = transformResult.features;

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.newLine();
            writer.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
            writer.newLine();
            writer.write("  <Document>");
            writer.newLine();
            writer.write("    <name>" + xml(layer.getName()) + "</name>");
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

                String name = getFeatureName(feature, layer);
                if (name != null && !name.isBlank()) {
                    writer.write("      <name>" + xml(name) + "</name>");
                    writer.newLine();
                }

                writer.write("      <description>" + xml(buildDescription(feature)) + "</description>");
                writer.newLine();

                writeKmlGeometry(writer, geometry, "      ");

                writer.write("    </Placemark>");
                writer.newLine();
            }

            writer.write("  </Document>");
            writer.newLine();
            writer.write("</kml>");
            writer.newLine();
        }
    }

    private static TransformResult transformFeaturesToTarget(Layer layer,
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

        CoordinateReferenceSystem sourceCRS = null;
        CoordinateReferenceSystem targetCRS = null;
        MathTransform transform = null;

        if (!sourceCode.equalsIgnoreCase(targetCode)) {
            sourceCRS = CRS.decode(sourceCode, true);
            targetCRS = CRS.decode(targetCode, true);
            transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
        } else {
            targetCRS = CRS.decode(targetCode, true);
        }

        List<TransformFeatureRow> transformedRows = new ArrayList<>();
        List<Geometry> transformedGeometries = new ArrayList<>();
        for (SimpleFeature feature : inputFeatures) {
            Geometry geometry = geometryOf(feature);
            if (geometry != null && transform != null) {
                geometry = JTS.transform(geometry, transform);
            } else if (geometry != null) {
                geometry = (Geometry) geometry.copy();
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
        } catch (Exception ignored) {
        }

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
        if (KML_OPTION.equals(option)) {
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

    private static String safeTypeName(String text) {
        String name = safeFileName(text).replaceAll("\\s+", "_");
        if (name.isBlank()) {
            name = "layer";
        }
        if (Character.isDigit(name.charAt(0))) {
            name = "layer_" + name;
        }
        return name;
    }

    private static String getFeatureName(SimpleFeature feature, Layer layer) {
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
        if (GEOJSON_OPTION.equals(option)) {
            return ".geojson";
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
        if (lower.endsWith(".geojson") || lower.endsWith(".json")) {
            return GEOJSON_OPTION;
        }
        if (lower.endsWith(".kml")) {
            return KML_OPTION;
        }
        return null;
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

    public static void refreshLayerFromFile(Layer layer, File file) throws Exception {
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
        } else if (lower.endsWith(".geojson") || lower.endsWith(".json")) {
            reloaded = GeoJsonLoader.load(file);
            if (sourceCRS == null || sourceCRS.isBlank()) {
                sourceCRS = "EPSG:4326";
            }
        } else if (lower.endsWith(".kml")) {
            reloaded = KmlLoader.load(file);
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

    private static class TransformResult {
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
