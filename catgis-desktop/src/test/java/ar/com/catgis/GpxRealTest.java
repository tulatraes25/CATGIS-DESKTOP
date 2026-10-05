package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip tests for GPX import via GpxLoader.
 * Uses inline GPX fixtures written to @TempDir.
 */
class GpxRealTest {

    @TempDir
    Path tempDir;

    @Test
    void loadGpxReturnsWaypoints() throws Exception {
        String gpx = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<gpx version=\"1.1\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n"
                + "  <wpt lat=\"-34.6\" lon=\"-58.5\">\n"
                + "    <name>Buenos Aires</name>\n"
                + "  </wpt>\n"
                + "  <wpt lat=\"-31.4\" lon=\"-64.2\">\n"
                + "    <name>Cordoba</name>\n"
                + "  </wpt>\n"
                + "</gpx>\n";
        File f = writeFile("waypoints.gpx", gpx);

        GpxImportResult result = GpxLoader.load(f);
        assertNotNull(result);
        assertNotNull(result.waypoints());
        assertFalse(result.waypoints().getFeatures().isEmpty(),
                "expected >= 1 waypoint feature");

        assertEquals("Buenos Aires",
                result.waypoints().getFeatures().get(0).getAttribute("name"));
    }

    @Test
    void loadGpxReturnsTracks() throws Exception {
        String gpx = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<gpx version=\"1.1\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n"
                + "  <trk>\n"
                + "    <name>Ruta Test</name>\n"
                + "    <trkseg>\n"
                + "      <trkpt lat=\"-34.6\" lon=\"-58.5\"/>\n"
                + "      <trkpt lat=\"-31.4\" lon=\"-64.2\"/>\n"
                + "    </trkseg>\n"
                + "  </trk>\n"
                + "</gpx>\n";
        File f = writeFile("track.gpx", gpx);

        GpxImportResult result = GpxLoader.load(f);
        assertNotNull(result);
        assertNotNull(result.tracks());
        assertFalse(result.tracks().getFeatures().isEmpty(),
                "expected >= 1 track feature");
    }

    private File writeFile(String name, String content) throws Exception {
        File f = tempDir.resolve(name).toFile();
        Files.writeString(f.toPath(), content, StandardCharsets.UTF_8);
        return f;
    }
}
