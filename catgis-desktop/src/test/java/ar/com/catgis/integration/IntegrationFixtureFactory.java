package ar.com.catgis.integration;

import ar.com.catgis.CRSDefinitions;
import ar.com.catgis.GeoPackageFeatureInfo;
import ar.com.catgis.GeoPackageLayer;
import ar.com.catgis.GeoPackageLoader;
import ar.com.catgis.RasterSidecarSupport;
import ar.com.catgis.ShapefileLoader;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geopkg.FeatureEntry;
import org.geotools.geopkg.GeoPackage;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;

import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.WritableRaster;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class IntegrationFixtureFactory {

    private static final String FIXTURE_CRS = "EPSG:4326";

    private IntegrationFixtureFactory() {
    }

    static Path repoRoot() throws Exception {
        Path current = Path.of("").toAbsolutePath();
        while (current != null && !Files.exists(current.resolve("build.gradle"))) {
            current = current.getParent();
        }
        if (current == null) {
            throw new IllegalStateException("No se pudo localizar build.gradle para resolver el root del repo.");
        }
        return current;
    }

    static Path fixturesDir() throws Exception {
        Path dir = repoRoot().resolve("src/test/resources/fixtures/integration");
        Files.createDirectories(dir);
        return dir;
    }

    static Path outputsDir() throws Exception {
        Path dir = repoRoot().resolve("build/catgis-integration-audit");
        Files.createDirectories(dir);
        return dir;
    }

    static Path ensureDemFixture() throws Exception {
        Path file = fixturesDir().resolve("test_dem.tif");
        if (Files.exists(file)) {
            return file;
        }
        writeSlopeDem(file, 20, 20, 100f, 200f);
        return file;
    }

    static Path ensureHydrologyDemFixture() throws Exception {
        Path file = fixturesDir().resolve("test_hydro_dem.tif");
        if (Files.exists(file)) {
            return file;
        }
        writeHydrologyDem(file, 32, 32);
        return file;
    }

    static Path ensureShapefileFixture() throws Exception {
        Path shp = fixturesDir().resolve("test_points.shp");
        if (Files.exists(shp)) {
            return shp;
        }

        SimpleFeatureType type = IntegrationTestSupport.createFeatureType(
                "test_points",
                FIXTURE_CRS,
                Point.class,
                new Object[][]{{"name", String.class}}
        );

        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        builder.add(IntegrationTestSupport.GEOMETRY_FACTORY.createPoint(new Coordinate(-58.45, -34.62)));
        builder.add("Point A");
        SimpleFeature a = builder.buildFeature("test_points.1");
        builder.reset();
        builder.add(IntegrationTestSupport.GEOMETRY_FACTORY.createPoint(new Coordinate(-58.37, -34.57)));
        builder.add("Point B");
        SimpleFeature b = builder.buildFeature("test_points.2");

        ListFeatureCollection collection = new ListFeatureCollection(type, List.of(a, b));
        ShapefileDataStore store = new ShapefileDataStore(shp.toUri().toURL());
        try {
            store.createSchema(type);
            SimpleFeatureStore featureStore = (SimpleFeatureStore) store.getFeatureSource(store.getTypeNames()[0]);
            featureStore.addFeatures(collection);
        } finally {
            store.dispose();
        }
        return shp;
    }

    static Path ensureGeoPackageFixture() throws Exception {
        Path gpkg = fixturesDir().resolve("test.gpkg");
        if (Files.exists(gpkg)) {
            return gpkg;
        }

        SimpleFeatureType type = IntegrationTestSupport.createFeatureType(
                "test_points",
                FIXTURE_CRS,
                Point.class,
                new Object[][]{{"name", String.class}}
        );

        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        builder.add(IntegrationTestSupport.GEOMETRY_FACTORY.createPoint(new Coordinate(-58.45, -34.62)));
        builder.add("Point A");
        SimpleFeature a = builder.buildFeature("test_points.1");
        builder.reset();
        builder.add(IntegrationTestSupport.GEOMETRY_FACTORY.createPoint(new Coordinate(-58.37, -34.57)));
        builder.add("Point B");
        SimpleFeature b = builder.buildFeature("test_points.2");

        FeatureEntry entry = new FeatureEntry();
        entry.setTableName("test_points");
        entry.setIdentifier("Test Points");
        entry.setDescription("Synthetic integration fixture");
        entry.setGeometryColumn("the_geom");
        entry.setSrid(4326);

        try (GeoPackage geoPackage = new GeoPackage(gpkg.toFile())) {
            geoPackage.init();
            geoPackage.add(entry, new ListFeatureCollection(type, List.of(a, b)));
        }
        return gpkg;
    }

    static GeoPackageLayer buildGeoPackageLayer(Path gpkg) throws Exception {
        List<GeoPackageFeatureInfo> entries = GeoPackageLoader.listFeatureEntries(gpkg.toFile());
        if (entries.isEmpty()) {
            throw new IllegalStateException("El GeoPackage fixture no tiene capas espaciales.");
        }
        GeoPackageFeatureInfo entry = entries.get(0);
        GeoPackageLayer layer = new GeoPackageLayer("test_points", gpkg.toString());
        layer.setTableName(entry.getTableName());
        layer.setSourceCRS(entry.getCrsCode());
        return layer;
    }

    static ShapefileData loadShapefileFixture() throws Exception {
        return ShapefileLoader.load(ensureShapefileFixture().toFile());
    }

    static void ensureAllFixtures() throws Exception {
        ensureDemFixture();
        ensureHydrologyDemFixture();
        ensureShapefileFixture();
        ensureGeoPackageFixture();
        outputsDir();
    }

    private static void writeSlopeDem(Path file, int width, int height, float startElevation, float endElevation) throws Exception {
        float[] values = new float[width * height];
        float range = endElevation - startElevation;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                float ratio = ((row * width) + col) / (float) Math.max(1, (width * height) - 1);
                values[(row * width) + col] = startElevation + (range * ratio);
            }
        }
        writeGeoTiff(file, width, height, values, "test-dem");
    }

    private static void writeHydrologyDem(Path file, int width, int height) throws Exception {
        float[] values = new float[width * height];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                double northSouthSlope = 240d - (row * 3.2d);
                double valley = Math.abs(col - (width / 2.0d)) * 7.5d;
                double headwaterBoost = row < 5 ? 8d : 0d;
                values[(row * width) + col] = (float) (northSouthSlope + valley + headwaterBoost);
            }
        }
        writeGeoTiff(file, width, height, values, "test-hydro-dem");
    }

    private static void writeGeoTiff(Path file, int width, int height, float[] values, String coverageName) throws Exception {
        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(values, values.length);
        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, buffer, null);
        ReferencedEnvelope envelope = new ReferencedEnvelope(
                -58.60, -58.20,
                -34.80, -34.40,
                DefaultGeographicCRS.WGS84
        );
        GridCoverage2D coverage = new GridCoverageFactory().create(coverageName, raster, envelope);
        GeoTiffWriter writer = new GeoTiffWriter(file.toFile());
        try {
            writer.write(coverage, null);
        } finally {
            writer.dispose();
        }
        RasterSidecarSupport.write(
                file.toFile(),
                new Envelope(envelope.getMinX(), envelope.getMaxX(), envelope.getMinY(), envelope.getMaxY()),
                CRSDefinitions.normalizeCode("EPSG:4326")
        );
    }
}
