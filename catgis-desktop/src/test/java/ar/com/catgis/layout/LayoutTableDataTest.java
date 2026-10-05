package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.io.File;
import java.nio.file.Files;

public class LayoutTableDataTest {

    @Test
    public void testLoadSimpleCsv() throws Exception {
        File tmp = File.createTempFile("test-table", ".csv");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "Nombre,X,Y\nPozo A,100,200\nPozo B,150,300\n".getBytes());
        LayoutTable t = new LayoutTable("t1", 10, 10, 200, 80);
        t.loadCsv(tmp);
        assertFalse(t.getRows().isEmpty());
        assertEquals(3, t.getRows().size());
        assertEquals("Nombre", t.getRows().get(0)[0]);
        assertEquals("Pozo A", t.getRows().get(1)[0]);
        tmp.delete();
    }

    @Test
    public void testLoadCsvWithQuotes() throws Exception {
        File tmp = File.createTempFile("test-table", ".csv");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "\"Nombre Completo\",\"Valor\"\n\"Juan, Perez\",\"100\"\n".getBytes());
        LayoutTable t = new LayoutTable("t1", 10, 10, 200, 80);
        t.loadCsv(tmp);
        assertEquals(2, t.getRows().size());
        assertEquals("Nombre Completo", t.getRows().get(0)[0]);
        assertEquals("Juan, Perez", t.getRows().get(1)[0]); // comma inside quotes
        tmp.delete();
    }

    @Test
    public void testLoadEmptyCsv() throws Exception {
        File tmp = File.createTempFile("test-table", ".csv");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "".getBytes());
        LayoutTable t = new LayoutTable("t1", 10, 10, 200, 80);
        t.loadCsv(tmp);
        assertTrue(t.getRows().isEmpty());
        tmp.delete();
    }

    @Test
    public void testLoadCsvHeaderOnly() throws Exception {
        File tmp = File.createTempFile("test-table", ".csv");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "A,B,C\n".getBytes());
        LayoutTable t = new LayoutTable("t1", 10, 10, 200, 80);
        t.loadCsv(tmp);
        assertEquals(1, t.getRows().size());
        assertEquals("A", t.getRows().get(0)[0]);
        tmp.delete();
    }

    @Test
    public void testTableRenderWithData() throws Exception {
        File tmp = File.createTempFile("test-table", ".csv");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "Nombre,Valor\nItem 1,100\nItem 2,200\n".getBytes());
        LayoutTable t = new LayoutTable("t1", 10, 10, 200, 80);
        t.loadCsv(tmp);
        t.setShowBorders(true);
        t.setAlternateRows(true);
        t.setFirstRowIsHeader(true);
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(400, 200, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 200, 297, 210);
        t.render(g, ctx);
        g.dispose();
        tmp.delete();
    }

    @Test
    public void testTableColorProperties() {
        LayoutTable t = new LayoutTable("t1", 10, 10, 200, 80);
        t.setTextColor(new Color(50, 50, 50));
        t.setHeaderBg(new Color(200, 210, 220));
        t.setBorderColor(new Color(150, 160, 170));
        t.setMaxVisibleRows(15);
        // no getters - verify no exception
    }

    @Test
    public void testCsvLineParsing() throws Exception {
        // Test parser through loadCsv
        File tmp = File.createTempFile("test-table", ".csv");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "simple,line,here\n1,2,3\n".getBytes());
        LayoutTable t = new LayoutTable("t1", 10, 10, 200, 80);
        t.loadCsv(tmp);
        assertEquals(2, t.getRows().size());
        assertEquals(3, t.getRows().get(0).length);
        assertEquals(3, t.getRows().get(1).length);
        tmp.delete();
    }
}
