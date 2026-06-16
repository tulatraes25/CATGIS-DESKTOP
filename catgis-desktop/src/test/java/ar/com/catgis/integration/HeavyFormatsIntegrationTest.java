package ar.com.catgis.integration;

import ar.com.catgis.FlatGeobufLoader;
import ar.com.catgis.GeoParquetReader;
import ar.com.catgis.LasReader;
import ar.com.catgis.PmtilesReader;
import ar.com.catgis.SpatiaLiteConnectionInfo;
import ar.com.catgis.SpatiaLiteLayer;
import ar.com.catgis.SpatiaLiteLoader;
import ar.com.catgis.ValidationResult;
import ar.com.catgis.data.vector.ShapefileData;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HeavyFormatsIntegrationTest {

    private static final String OGR2OGR = "C:\\OSGeo4W64\\bin\\ogr2ogr.exe";

    @TempDir
    Path tempDir;

    @Test
    void pmtilesHeaderDirectoryAndTileReadWorkOnSyntheticArchive() throws Exception {
        File pmtiles = tempDir.resolve("synthetic.pmtiles").toFile();
        byte[] header = new byte[PmtilesReader.HEADER_SIZE];
        ByteBuffer hb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        hb.putInt(0, 0x4D50);
        hb.putInt(4, 3);
        hb.putInt(12, 1);
        hb.putInt(16, 0);
        hb.putInt(20, 0);
        hb.putLong(24, PmtilesReader.HEADER_SIZE + 24L);
        hb.putInt(32, PmtilesReader.COMPRESSION_NONE);
        hb.putInt(36, PmtilesReader.TILE_TYPE_MVT);

        byte[] tileBytes = "demo-tile".getBytes(StandardCharsets.UTF_8);
        byte[] entry = new byte[24];
        ByteBuffer eb = ByteBuffer.wrap(entry).order(ByteOrder.LITTLE_ENDIAN);
        eb.putInt(0, 0);
        eb.putInt(4, 0);
        eb.putInt(8, 0);
        eb.putLong(12, PmtilesReader.HEADER_SIZE + 24L);
        eb.putInt(20, tileBytes.length);

        Files.write(pmtiles.toPath(), concat(header, entry, tileBytes));

        PmtilesReader.PmtilesHeader parsedHeader = PmtilesReader.readHeader(pmtiles);
        assertEquals(3, parsedHeader.version());
        assertEquals(1, parsedHeader.numTiles());

        List<PmtilesReader.TileEntry> entries = PmtilesReader.readDirectory(pmtiles, parsedHeader);
        assertEquals(1, entries.size());
        byte[] loadedTile = PmtilesReader.readTile(pmtiles, entries, 0, 0, 0);
        assertArrayEquals(tileBytes, loadedTile);
    }

    @Test
    void geoparquetSummaryWorksOnSyntheticParquetContainer() throws Exception {
        File parquet = tempDir.resolve("synthetic.parquet").toFile();
        byte[] bytes = new byte[64];
        System.arraycopy("PAR1".getBytes(StandardCharsets.US_ASCII), 0, bytes, 0, 4);
        System.arraycopy("PAR1".getBytes(StandardCharsets.US_ASCII), 0, bytes, bytes.length - 4, 4);
        ByteBuffer.wrap(bytes, bytes.length - 8, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(16);
        Files.write(parquet.toPath(), bytes);

        assertTrue(GeoParquetReader.isGeoParquet(parquet));
        String summary = GeoParquetReader.getSummary(parquet);
        assertTrue(summary.contains("GeoParquet"));
        assertTrue(summary.contains("EPSG:4326"));
    }

    @Test
    void lasHeaderAndBoundsFollowSyntheticLasFile() throws Exception {
        File las = tempDir.resolve("synthetic.las").toFile();
        writeSyntheticLas(las);

        LasReader.LasHeader header = LasReader.readHeader(las);
        assertEquals(1, header.versionMajor());
        assertEquals(2, header.versionMinor());
        assertEquals(2L, header.pointCount());
        assertEquals(0.01d, header.scaleX(), 1e-9);
        assertEquals(100.0d, header.minX(), 1e-9);
        assertEquals(101.0d, header.maxX(), 1e-9);

        var bounds = LasReader.getBounds(las);
        assertEquals(100.0d, bounds.getMinX(), 1e-9);
        assertEquals(101.0d, bounds.getMaxX(), 1e-9);
        assertEquals(200.0d, bounds.getMinY(), 1e-9);
        assertEquals(201.0d, bounds.getMaxY(), 1e-9);
    }

    @Test
    void flatgeobufRoundTripWorksWhenOgr2ogrIsAvailable() throws Exception {
        Assumptions.assumeTrue(new File(OGR2OGR).exists(), "ogr2ogr no disponible");
        Assumptions.assumeTrue(gdalCompatible(), "GDAL >= 3.13: formato FlatGeobuf incompatible con wololo reader");

        File geojson = tempDir.resolve("flatgeobuf.geojson").toFile();
        Files.writeString(geojson.toPath(),
                "{ \"type\": \"FeatureCollection\", \"features\": [" +
                        "{ \"type\": \"Feature\", \"properties\": { \"name\": \"Zona A\" }," +
                        "\"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[[0,0],[10,0],[10,10],[0,10],[0,0]]] } } ] }",
                StandardCharsets.UTF_8);
        File fgb = tempDir.resolve("data.fgb").toFile();
        Process p = new ProcessBuilder(OGR2OGR, "-f", "FlatGeobuf", fgb.getAbsolutePath(), geojson.getAbsolutePath())
                .redirectErrorStream(true).start();
        assertEquals(0, p.waitFor(), "ogr2ogr failed: " + new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8));

        ValidationResult validation = FlatGeobufLoader.validateFile(fgb);
        assertTrue(validation.isValid(), validation.message());
        ShapefileData data = FlatGeobufLoader.load(fgb);
        assertEquals(1, data.getFeatureCount());
    }

    @Test
    void spatialiteRoundTripWorksWhenOgr2ogrIsAvailable() throws Exception {
        Assumptions.assumeTrue(new File(OGR2OGR).exists(), "ogr2ogr no disponible");
        Assumptions.assumeTrue(gdalCompatible(), "GDAL >= 3.13: formato SQLite/SpatiaLite incompatible");

        File geojson = tempDir.resolve("spatialite.geojson").toFile();
        Files.writeString(geojson.toPath(),
                "{ \"type\": \"FeatureCollection\", \"features\": [" +
                        "{ \"type\": \"Feature\", \"properties\": { \"name\": \"Point A\" }," +
                        "\"geometry\": { \"type\": \"Point\", \"coordinates\": [5,5] } } ] }",
                StandardCharsets.UTF_8);
        File sqlite = tempDir.resolve("data.sqlite").toFile();
        Process p = new ProcessBuilder(OGR2OGR, "-f", "SQLite", sqlite.getAbsolutePath(), geojson.getAbsolutePath(), "-dsco", "SPATIALITE=YES")
                .redirectErrorStream(true).start();
        assertEquals(0, p.waitFor(), "ogr2ogr failed: " + new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8));

        SpatiaLiteConnectionInfo info = new SpatiaLiteConnectionInfo();
        info.setFilePath(sqlite.getAbsolutePath());
        var types = SpatiaLiteLoader.listFeatureTypes(info);
        assertFalse(types.isEmpty());

        SpatiaLiteLayer layer = new SpatiaLiteLayer("test", sqlite.getAbsolutePath());
        layer.setTableName(types.get(0).getTableName());
        ShapefileData data = SpatiaLiteLoader.loadLayerData(layer);
        assertEquals(1, data.getFeatureCount());
        assertEquals("Point A", data.getFeatures().get(0).getAttribute("name"));
    }

    private static boolean gdalCompatible() {
        try {
            Process p = new ProcessBuilder(OGR2OGR, "--version").redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            p.waitFor();
            int dot1 = out.indexOf('.');
            int dot2 = out.indexOf('.', dot1 + 1);
            if (dot1 < 0 || dot2 < 0) return true;
            int major = Integer.parseInt(out.substring(dot1 - 1, dot1).trim());
            int minor = Integer.parseInt(out.substring(dot1 + 1, dot2));
            return major < 3 || (major == 3 && minor < 13);
        } catch (Exception e) {
            return true;
        }
    }

    private static byte[] concat(byte[]... arrays) {
        int total = 0;
        for (byte[] array : arrays) total += array.length;
        byte[] out = new byte[total];
        int pos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, out, pos, array.length);
            pos += array.length;
        }
        return out;
    }

    private static void writeSyntheticLas(File file) throws Exception {
        byte[] header = new byte[227];
        ByteBuffer b = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        b.put("LASF".getBytes(StandardCharsets.US_ASCII));
        b.put(24, (byte) 1);
        b.put(25, (byte) 2);
        b.putShort(94, (short) 227);
        b.putInt(96, 227);
        b.put(104, (byte) 0);
        b.putShort(105, (short) 20);
        b.putInt(107, 2);
        b.putDouble(131, 0.01d);
        b.putDouble(139, 0.01d);
        b.putDouble(147, 0.01d);
        b.putDouble(155, 0.0d);
        b.putDouble(163, 0.0d);
        b.putDouble(171, 0.0d);
        b.putDouble(179, 101.0d);
        b.putDouble(187, 100.0d);
        b.putDouble(195, 201.0d);
        b.putDouble(203, 200.0d);
        b.putDouble(211, 51.0d);
        b.putDouble(219, 50.0d);

        byte[] point1 = new byte[20];
        ByteBuffer p1 = ByteBuffer.wrap(point1).order(ByteOrder.LITTLE_ENDIAN);
        p1.putInt(0, 10000);
        p1.putInt(4, 20000);
        p1.putInt(8, 5000);
        p1.putShort(12, (short) 100);
        p1.put(14, (byte) 1);
        p1.put(15, (byte) 2);
        p1.put(16, (byte) 0);
        p1.put(17, (byte) 0);
        p1.put(18, (byte) 2);
        p1.put(19, (byte) 0);

        byte[] point2 = new byte[20];
        ByteBuffer p2 = ByteBuffer.wrap(point2).order(ByteOrder.LITTLE_ENDIAN);
        p2.putInt(0, 10100);
        p2.putInt(4, 20100);
        p2.putInt(8, 5100);
        p2.putShort(12, (short) 90);
        p2.put(14, (byte) 1);
        p2.put(15, (byte) 1);
        p2.put(16, (byte) 0);
        p2.put(17, (byte) 0);
        p2.put(18, (byte) 2);
        p2.put(19, (byte) 0);

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.write(header);
            raf.write(point1);
            raf.write(point2);
        }
    }
}
