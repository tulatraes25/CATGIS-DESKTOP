package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.util.List;

final class ReleaseTestSupport {

    static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private ReleaseTestSupport() {
    }

    static SimpleFeatureType createFeatureType(String name,
                                               String crsCode,
                                               Class<? extends Geometry> geometryClass,
                                               Object[][] attributes) throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(name);
        if (crsCode != null && !crsCode.isBlank()) {
            if ("EPSG:4326".equalsIgnoreCase(crsCode.trim())) {
                builder.setCRS(DefaultGeographicCRS.WGS84);
            } else {
                builder.setCRS(CRSDefinitions.decode(crsCode, false));
            }
        }
        builder.add("the_geom", geometryClass);
        if (attributes != null) {
            for (Object[] attribute : attributes) {
                builder.add(String.valueOf(attribute[0]), (Class<?>) attribute[1]);
            }
        }
        return builder.buildFeatureType();
    }

    static SimpleFeature buildFeature(SimpleFeatureType type, String id, Object... values) {
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
        for (int i = 0; i < values.length; i++) {
            featureBuilder.add(values[i]);
        }
        return featureBuilder.buildFeature(id);
    }

    static ShapefileData buildPointData(String name, String crsCode, Coordinate coordinate, String label, Integer code) throws Exception {
        SimpleFeatureType type = createFeatureType(
                name,
                crsCode,
                Point.class,
                new Object[][]{
                        {"name", String.class},
                        {"codigo", Integer.class}
                }
        );
        Point point = GEOMETRY_FACTORY.createPoint(coordinate);
        SimpleFeature feature = buildFeature(type, name + ".1", point, label, code);
        return new ShapefileData(
                List.of(feature),
                new Envelope(point.getEnvelopeInternal()),
                name,
                1,
                "ok",
                type
        );
    }

    static Layer buildVectorLayer(String name, Path path, String sourceCrs) {
        Layer layer = new VectorLayer(name, path != null ? path.toString() : "");
        layer.setSourceCRS(sourceCrs != null ? sourceCrs : "");
        layer.setVisible(true);
        return layer;
    }

    static void initializeAppContext(String projectName) {
        ModuleRegistry.initializeDefaults();
        AppContext.setCurrentProject(new Project(projectName));
        CatgisDesktopApp.mapPanel = new MapPanel();
        CatgisDesktopApp.layersPanel = new LayersPanel();
        CatgisDesktopApp.statusBar = new StatusBar();
    }

    static void clearAppContext() {
        AppContext.setCurrentProject(null);
        CatgisDesktopApp.mapPanel = null;
        CatgisDesktopApp.layersPanel = null;
        CatgisDesktopApp.statusBar = null;
    }

    static void runOnEdt(ThrowingRunnable runnable) throws Exception {
        final Exception[] failure = new Exception[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                runnable.run();
            } catch (Exception ex) {
                failure[0] = ex;
            }
        });
        if (failure[0] != null) {
            throw failure[0];
        }
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }
}
