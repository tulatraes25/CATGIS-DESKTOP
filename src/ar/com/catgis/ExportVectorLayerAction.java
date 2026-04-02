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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportVectorLayerAction {

    public static void exportLayer(Layer layer) {
        if (layer == null) {
            JOptionPane.showMessageDialog(null, "No hay una capa seleccionada.");
            return;
        }

        ShapefileData data = CatgisDesktopApp.mapPanel.getShapefileData(layer);
        if (data == null || data.getFeatures() == null || data.getFeatures().isEmpty()) {
            JOptionPane.showMessageDialog(null, "La capa no tiene datos disponibles para exportar.");
            return;
        }

        Object selected = JOptionPane.showInputDialog(
                null,
                "Seleccione formato de exportación:",
                "Exportar capa",
                JOptionPane.PLAIN_MESSAGE,
                null,
                new String[]{"Shapefile (*.shp)", "GeoJSON (*.geojson)", "KML (*.kml)"},
                "Shapefile (*.shp)"
        );

        if (selected == null) {
            return;
        }

        String option = selected.toString();

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exportar capa");
        chooser.setAcceptAllFileFilterUsed(false);

        String extension;
        if (option.startsWith("Shapefile")) {
            chooser.setFileFilter(new FileNameExtensionFilter("Shapefile (*.shp)", "shp"));
            extension = ".shp";
        } else if (option.startsWith("GeoJSON")) {
            chooser.setFileFilter(new FileNameExtensionFilter("GeoJSON (*.geojson)", "geojson"));
            extension = ".geojson";
        } else {
            chooser.setFileFilter(new FileNameExtensionFilter("KML (*.kml)", "kml"));
            extension = ".kml";
        }

        chooser.setSelectedFile(new File(safeFileName(layer.getName()) + extension));

        int result = chooser.showSaveDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(extension)) {
            file = new File(file.getAbsolutePath() + extension);
        }

        try {
            if (option.startsWith("Shapefile")) {
                exportToShapefile(layer, data, file);
            } else if (option.startsWith("GeoJSON")) {
                exportToGeoJson(layer, data, file);
            } else {
                exportToKml(layer, data, file);
            }

            JOptionPane.showMessageDialog(null, "Capa exportada correctamente:\n" + file.getAbsolutePath());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al exportar capa: " + ex.getMessage());
        }
    }

    private static void exportToShapefile(Layer layer, ShapefileData data, File file) throws Exception {
        TransformResult transformResult = transformFeaturesToTarget(layer, data, getProjectCRSCode());
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

    private static void exportToGeoJson(Layer layer, ShapefileData data, File file) throws Exception {
        TransformResult transformResult = transformFeaturesToTarget(layer, data, getProjectCRSCode());
        SimpleFeatureType featureType = transformResult.featureType;
        List<SimpleFeature> features = transformResult.features;

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            FeatureJSON featureJSON = new FeatureJSON();
            featureJSON.writeFeatureCollection(new ListFeatureCollection(featureType, features), writer);
        }
    }

    private static void exportToKml(Layer layer, ShapefileData data, File file) throws Exception {
        TransformResult transformResult = transformFeaturesToTarget(layer, data, "EPSG:4326");
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

    private static TransformResult transformFeaturesToTarget(Layer layer, ShapefileData data, String targetCode) throws Exception {
        List<SimpleFeature> inputFeatures = data.getFeatures();
        if (inputFeatures == null || inputFeatures.isEmpty()) {
            throw new RuntimeException("La capa no contiene entidades.");
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

        SimpleFeature first = inputFeatures.get(0);
        SimpleFeatureType sourceType = first.getFeatureType();

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
                typeBuilder.add(gd.getLocalName(), gd.getType().getBinding());
            } else {
                typeBuilder.add(descriptor.getLocalName(), String.class);
            }
        }

        SimpleFeatureType targetType = typeBuilder.buildFeatureType();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(targetType);

        List<SimpleFeature> output = new ArrayList<>();
        int id = 1;

        for (SimpleFeature feature : inputFeatures) {
            for (AttributeDescriptor descriptor : sourceType.getAttributeDescriptors()) {
                String name = descriptor.getLocalName();
                Object value = feature.getAttribute(name);

                if (descriptor instanceof GeometryDescriptor) {
                    Geometry geometry = value instanceof Geometry ? (Geometry) value : null;
                    if (geometry != null && transform != null) {
                        geometry = JTS.transform(geometry, transform);
                    }
                    builder.add(geometry);
                } else {
                    builder.add(value != null ? String.valueOf(value) : "");
                }
            }

            output.add(builder.buildFeature(String.valueOf(id++)));
            builder.reset();
        }

        return new TransformResult(targetType, output);
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

    private static class TransformResult {
        final SimpleFeatureType featureType;
        final List<SimpleFeature> features;

        TransformResult(SimpleFeatureType featureType, List<SimpleFeature> features) {
            this.featureType = featureType;
            this.features = features;
        }
    }
}