package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.nio.file.Files;

public class QgisQptImporterTest {

    @Test
    public void testImportEmptyFile() throws Exception {
        File tmp = File.createTempFile("empty", ".qpt");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "<Layout></Layout>".getBytes());
        QgisQptImporter.ImportResult result = QgisQptImporter.importQpt(tmp);
        assertNotNull(result);
        assertEquals(297, result.pageWidthMm, 0.1);
        assertEquals(210, result.pageHeightMm, 0.1);
        tmp.delete();
    }

    @Test
    public void testImportWithLabel() throws Exception {
        File tmp = File.createTempFile("test", ".qpt");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), ("<Composer title='test'>"
            + "<ComposerLabel id='lbl1' x='10' y='20' width='100' height='30'>"
            + "<labelText>Hello World</labelText>"
            + "</ComposerLabel>"
            + "</Composer>").getBytes());
        QgisQptImporter.ImportResult result = QgisQptImporter.importQpt(tmp);
        assertNotNull(result);
        assertTrue(result.imported.size() >= 1);
        assertTrue(result.imported.get(0) instanceof LayoutLabel);
        assertEquals("Hello World", ((LayoutLabel)result.imported.get(0)).getText());
        tmp.delete();
    }

    @Test
    public void testImportWithComposerMap() throws Exception {
        File tmp = File.createTempFile("test", ".qpt");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), ("<Composer>"
            + "<ComposerMap id='map1' x='15' y='25' width='267' height='160'/>"
            + "</Composer>").getBytes());
        QgisQptImporter.ImportResult result = QgisQptImporter.importQpt(tmp);
        assertTrue(result.imported.size() >= 1);
        assertTrue(result.imported.get(0) instanceof LayoutMap);
        tmp.delete();
    }

    @Test
    public void testImportWithLegend() throws Exception {
        File tmp = File.createTempFile("test", ".qpt");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), ("<Composer>"
            + "<ComposerLegend id='leg1' x='150' y='50' width='80' height='120'>"
            + "<title>Referencias</title>"
            + "</ComposerLegend>"
            + "</Composer>").getBytes());
        QgisQptImporter.ImportResult result = QgisQptImporter.importQpt(tmp);
        assertTrue(result.imported.size() >= 1);
        assertTrue(result.imported.get(0) instanceof LayoutLegend);
        LayoutLegend leg = (LayoutLegend) result.imported.get(0);
        assertEquals("Referencias", leg.getTitle());
        assertTrue(leg.isAutoHeight());
        tmp.delete();
    }
}
