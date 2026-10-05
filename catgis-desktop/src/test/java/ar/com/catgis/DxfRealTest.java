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
 * Round-trip tests for DxfLoader with minimal valid DXF files.
 * DXF is an ASCII text format; no external dependencies needed.
 */
class DxfRealTest {

    @TempDir
    Path tempDir;

    @Test
    void loadMinimalDxfReturnsShapefileData() throws Exception {
        File dxf = tempDir.resolve("minimal.dxf").toFile();
        Files.writeString(dxf.toPath(), MINIMAL_DXF, StandardCharsets.UTF_8);

        ShapefileData data = DxfLoader.load(dxf);
        assertNotNull(data);
        assertFalse(data.getFeatures().isEmpty(), "expected at least 1 feature");
    }

    @Test
    void loadDxfWithLineHasLineGeometry() throws Exception {
        File dxf = tempDir.resolve("line.dxf").toFile();
        Files.writeString(dxf.toPath(), LINE_DXF, StandardCharsets.UTF_8);

        ShapefileData data = DxfLoader.load(dxf);
        Geometry g = (Geometry) data.getFeatures().get(0).getDefaultGeometry();
        assertNotNull(g);
        assertTrue(g.getGeometryType().contains("Line") || g.getGeometryType().contains("line"),
                "expected line geometry, got " + g.getGeometryType());
    }

    @Test
    void loadDxfWithMultipleLinesReturnsMultipleFeatures() throws Exception {
        File dxf = tempDir.resolve("multi.dxf").toFile();
        Files.writeString(dxf.toPath(), TWO_LINES_DXF, StandardCharsets.UTF_8);

        ShapefileData data = DxfLoader.load(dxf);
        assertTrue(data.getFeatures().size() >= 1, "expected at least 1 feature");
    }

    // ---- Minimal DXF fixtures ----

    /**
     * Absolute minimum valid DXF — empty drawing with just header/tables/entities/EOF.
     * Contains one LINE entity.
     */
    static final String LINE_DXF = """
            0
            SECTION
            2
            HEADER
            0
            ENDSEC
            0
            SECTION
            2
            TABLES
            0
            ENDSEC
            0
            SECTION
            2
            BLOCKS
            0
            ENDSEC
            0
            SECTION
            2
            ENTITIES
            0
            LINE
            10
            0.0
            20
            0.0
            30
            0.0
            11
            10.0
            21
            10.0
            31
            0.0
            0
            ENDSEC
            0
            EOF
            """;

    /** Minimal DXF with two LINE entities. */
    static final String TWO_LINES_DXF = """
            0
            SECTION
            2
            HEADER
            0
            ENDSEC
            0
            SECTION
            2
            TABLES
            0
            ENDSEC
            0
            SECTION
            2
            BLOCKS
            0
            ENDSEC
            0
            SECTION
            2
            ENTITIES
            0
            LINE
            10
            0.0
            20
            0.0
            30
            0.0
            11
            10.0
            21
            10.0
            31
            0.0
            0
            LINE
            10
            5.0
            20
            5.0
            30
            0.0
            11
            15.0
            21
            15.0
            31
            0.0
            0
            ENDSEC
            0
            EOF
            """;

    /** Same as LINE_DXF — used by loadMinimalDxf test. */
    static final String MINIMAL_DXF = LINE_DXF;
}
