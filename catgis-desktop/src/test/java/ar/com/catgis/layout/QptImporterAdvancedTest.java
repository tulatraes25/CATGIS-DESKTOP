package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;

public class QptImporterAdvancedTest {

    @Test
    public void testImportNorthArrow() throws Exception {
        File tmp = File.createTempFile("test", ".qpt");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), ("<Composer>"
            + "<ComposerArrow id='north1' x='250' y='20' width='30' height='30'/>"
            + "</Composer>").getBytes());
        QgisQptImporter.ImportResult result = QgisQptImporter.importQpt(tmp);
        assertTrue(result.imported.size() >= 1);
        assertTrue(result.imported.get(0) instanceof LayoutNorthArrow);
        tmp.delete();
    }

    @Test
    public void testImportRectangle() throws Exception {
        File tmp = File.createTempFile("test", ".qpt");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), ("<Composer>"
            + "<ComposerShape id='rect1' x='20' y='30' width='100' height='60'/>"
            + "</Composer>").getBytes());
        QgisQptImporter.ImportResult result = QgisQptImporter.importQpt(tmp);
        assertTrue(result.imported.size() >= 1);
        assertTrue(result.imported.get(0) instanceof LayoutRectangle);
        tmp.delete();
    }

    @Test
    public void testImportMultipleItems() throws Exception {
        File tmp = File.createTempFile("test", ".qpt");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), ("<Composer>"
            + "<ComposerLabel id='lbl1' x='10' y='10' width='100' height='20'><labelText>Titulo</labelText></ComposerLabel>"
            + "<ComposerMap id='map1' x='10' y='40' width='200' height='150'/>"
            + "<ComposerLegend id='leg1' x='220' y='40' width='70' height='100'/>"
            + "</Composer>").getBytes());
        QgisQptImporter.ImportResult result = QgisQptImporter.importQpt(tmp);
        assertTrue(result.imported.size() >= 3);
        tmp.delete();
    }

    @Test
    public void testImportWithUnknownElement() throws Exception {
        File tmp = File.createTempFile("test", ".qpt");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), ("<Composer>"
            + "<ComposerHtml id='html1' x='10' y='10' width='100' height='50'/>"
            + "</Composer>").getBytes());
        QgisQptImporter.ImportResult result = QgisQptImporter.importQpt(tmp);
        assertTrue(result.skipped.size() >= 1); // HTML frame not supported
        tmp.delete();
    }

    @Test
    public void testImportWithPaperSize() throws Exception {
        File tmp = File.createTempFile("test", ".qpt");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), ("<Composer>"
            + "<Composition width='420' height='297'/>"
            + "<ComposerMap id='map1' x='10' y='10' width='400' height='277'/>"
            + "</Composer>").getBytes());
        QgisQptImporter.ImportResult result = QgisQptImporter.importQpt(tmp);
        // Should detect page size from Composition element
        assertTrue(result.pageWidthMm > 200);
        tmp.delete();
    }
}
