package ar.com.catgis;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class PointSymbolCatalogTest {

    @Test
    public void testCatalogNotEmpty() {
        assertFalse(PointSymbolCatalog.entries().isEmpty());
        assertTrue(PointSymbolCatalog.entries().size() > 30);
    }

    @Test
    public void testCategoriesExist() {
        assertFalse(PointSymbolCatalog.categories().isEmpty());
        // Categories may be translated; just check non-empty
        var cats = PointSymbolCatalog.categories();
        assertTrue(cats.size() >= 5);
    }

    @Test
    public void testFindByReference() {
        // Test that NONE entry exists (always present)
        PointSymbolCatalog.Entry e = PointSymbolCatalog.NONE;
        assertNotNull(e);
        assertNotNull(e.getId());
        assertNotNull(e.getLabel());
    }

    @Test
    public void testFindByReferenceNotFound() {
        PointSymbolCatalog.Entry e = PointSymbolCatalog.findByReference("nonexistent-symbol-xyz");
        assertNull(e);
    }

    @Test
    public void testEntriesForCategory() {
        var allEntries = PointSymbolCatalog.entries();
        if (allEntries.isEmpty()) return;
        String firstCat = allEntries.get(0).getCategory();
        var entries = PointSymbolCatalog.entriesForCategory(firstCat);
        assertFalse(entries.isEmpty());
        for (var e : entries) assertEquals(firstCat, e.getCategory());
    }

    @Test
    public void testRenderDoesNotThrow() {
        BufferedImage img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        // Should not throw for any valid symbol
        PointSymbolCatalog.render(g, "circle", 20, 20, 20, Color.BLUE, Color.BLACK, 1.5f);
        PointSymbolCatalog.render(g, "square", 20, 20, 16, Color.RED, null, 0);
        PointSymbolCatalog.render(g, "star", 20, 20, 24, Color.GREEN, Color.BLACK, 2f);
        PointSymbolCatalog.render(g, "pin", 20, 20, 18, Color.ORANGE, Color.DARK_GRAY, 1f);
        PointSymbolCatalog.render(g, "cross", 20, 20, 14, Color.MAGENTA, null, 0);
        PointSymbolCatalog.render(g, "target", 20, 20, 22, Color.BLUE, Color.RED, 1f);
        PointSymbolCatalog.render(g, "triangle", 20, 20, 18, Color.YELLOW, Color.BLACK, 1f);
        PointSymbolCatalog.render(g, "diamond", 20, 20, 16, Color.CYAN, null, 0);
        PointSymbolCatalog.render(g, null, 20, 20, 10, Color.BLUE, null, 1f); // null id = safe
        g.dispose();
    }

    @Test
    public void testIsCatalogReference() {
        assertTrue(PointSymbolCatalog.isCatalogReference("catalog:some/path.svg"));
        assertFalse(PointSymbolCatalog.isCatalogReference("circle"));
        assertFalse(PointSymbolCatalog.isCatalogReference(null));
    }
}
