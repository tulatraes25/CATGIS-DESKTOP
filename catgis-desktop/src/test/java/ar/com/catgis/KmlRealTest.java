package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip tests for KML import via KmlLoader.
 * Uses inline KML fixtures written to @TempDir.
 */
class KmlRealTest {

    @TempDir
    Path tempDir;

    @Test
    void loadKmlReturnsFeatures() throws Exception {
        String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
                + "  <Document>\n"
                + "    <Placemark>\n"
                + "      <name>Buenos Aires</name>\n"
                + "      <Point><coordinates>-58.5,-34.6,0</coordinates></Point>\n"
                + "    </Placemark>\n"
                + "    <Placemark>\n"
                + "      <name>Cordoba</name>\n"
                + "      <Point><coordinates>-64.2,-31.4,0</coordinates></Point>\n"
                + "    </Placemark>\n"
                + "  </Document>\n"
                + "</kml>\n";
        File f = writeFile("kml-points.kml", kml);

        ShapefileData data = KmlLoader.load(f);
        assertNotNull(data);
        assertTrue(data.getFeatures().size() >= 2, "expected >= 2 features, got " + data.getFeatures().size());

        Geometry g = (Geometry) data.getFeatures().get(0).getDefaultGeometry();
        assertNotNull(g);
        assertEquals("Point", g.getGeometryType());
    }

    @Test
    void loadKmlWithPolygon() throws Exception {
        String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
                + "  <Document>\n"
                + "    <Placemark>\n"
                + "      <name>Zona Test</name>\n"
                + "      <Polygon>\n"
                + "        <outerBoundaryIs><LinearRing>\n"
                + "          <coordinates>0,0,0 10,0,0 10,10,0 0,10,0 0,0,0</coordinates>\n"
                + "        </LinearRing></outerBoundaryIs>\n"
                + "      </Polygon>\n"
                + "    </Placemark>\n"
                + "  </Document>\n"
                + "</kml>\n";
        File f = writeFile("kml-polygon.kml", kml);

        ShapefileData data = KmlLoader.load(f);
        assertEquals(1, data.getFeatures().size());

        Geometry g = (Geometry) data.getFeatures().get(0).getDefaultGeometry();
        assertEquals("Polygon", g.getGeometryType());
        assertEquals("Zona Test", data.getFeatures().get(0).getAttribute("name"));
    }

    private File writeFile(String name, String content) throws Exception {
        File f = tempDir.resolve(name).toFile();
        Files.writeString(f.toPath(), content, StandardCharsets.UTF_8);
        return f;
    }
}
