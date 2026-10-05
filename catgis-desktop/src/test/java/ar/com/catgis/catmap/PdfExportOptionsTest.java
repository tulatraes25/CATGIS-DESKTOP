package ar.com.catgis.catmap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PdfExportOptionsTest {

    @TempDir
    Path tempDir;

    @Test
    void builderPatternWorks() {
        PdfExportOptions opts = new PdfExportOptions()
                .title("Mi Mapa")
                .author("Claudio")
                .subject("Topografía")
                .pageSize(420, 297)
                .dpi(150)
                .watermark("BORRADOR");

        assertEquals("Mi Mapa", opts.title);
        assertEquals("Claudio", opts.author);
        assertEquals("Topografía", opts.subject);
        assertEquals(420.0, opts.pageWidthMm);
        assertEquals(297.0, opts.pageHeightMm);
        assertEquals(150, opts.dpi);
        assertEquals("BORRADOR", opts.watermark);
    }

    @Test
    void defaultValuesAreSane() {
        PdfExportOptions opts = new PdfExportOptions();
        assertEquals(297.0, opts.pageWidthMm);
        assertEquals(210.0, opts.pageHeightMm);
        assertEquals(300, opts.dpi);
        assertEquals("", opts.title);
        assertEquals("", opts.watermark);
    }
}
