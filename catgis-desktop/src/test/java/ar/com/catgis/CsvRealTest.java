package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip tests for CSV point import via CsvTableReader.
 * Uses inline CSV fixtures written to @TempDir.
 */
class CsvRealTest {

    @TempDir
    Path tempDir;

    @Test
    void loadCsvReturnsPoints() throws Exception {
        String csv = "lat,lon,nombre\n"
                + "-34.6,-58.5,Buenos Aires\n"
                + "-31.4,-64.2,Cordoba\n";
        File f = writeCsv("puntos.csv", csv);

        TablePointData data = CsvTableReader.read(f);
        assertNotNull(data);
        assertTrue(data.getColumns().size() >= 3, "expected >= 3 columns");
        assertEquals(2, data.getRows().size(), "expected 2 rows");

        Map<String, String> first = data.getRows().get(0);
        assertTrue(first.containsKey("nombre"));
        assertEquals("Buenos Aires", first.get("nombre"));
    }

    @Test
    void loadCsvWithXyColumns() throws Exception {
        String csv = "x,y,nombre\n"
                + "-58.5,-34.6,BA\n"
                + "-64.2,-31.4,CBA\n";
        File f = writeCsv("xy.csv", csv);

        TablePointData data = CsvTableReader.read(f);
        assertNotNull(data);
        assertEquals(2, data.getRows().size());
    }

    @Test
    void loadEmptyCsvThrows() {
        File f = tempDir.resolve("empty.csv").toFile();
        assertThrows(Exception.class, () -> CsvTableReader.read(f));
    }

    private File writeCsv(String name, String content) throws Exception {
        File f = tempDir.resolve(name).toFile();
        Files.writeString(f.toPath(), content, StandardCharsets.UTF_8);
        return f;
    }
}
