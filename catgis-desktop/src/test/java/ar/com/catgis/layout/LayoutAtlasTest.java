package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

public class LayoutAtlasTest {

    @Test
    public void testAtlasDefaults() {
        LayoutAtlas atlas = new LayoutAtlas();
        assertFalse(atlas.isEnabled());
        assertEquals(0, atlas.getPageCount());
        assertEquals(0, atlas.getCurrentPage());
    }

    @Test
    public void testAtlasEnable() {
        LayoutAtlas atlas = new LayoutAtlas();
        atlas.setEnabled(true);
        assertTrue(atlas.isEnabled());
    }

    @Test
    public void testAtlasPageNames() {
        LayoutAtlas atlas = new LayoutAtlas();
        atlas.setPageNames(Arrays.asList("Page1", "Page2", "Page3"));
        assertEquals(3, atlas.getPageCount());
        assertEquals("", atlas.getCurrentPageName()); // not enabled
        atlas.setEnabled(true);
        assertEquals("Page1", atlas.getCurrentPageName());
    }

    @Test
    public void testAtlasNavigation() {
        LayoutAtlas atlas = new LayoutAtlas();
        atlas.setEnabled(true);
        atlas.setPageNames(Arrays.asList("A", "B", "C"));
        assertEquals("A", atlas.getCurrentPageName());
        assertTrue(atlas.hasNext());
        assertFalse(atlas.hasPrev());
        atlas.nextPage();
        assertEquals("B", atlas.getCurrentPageName());
        assertTrue(atlas.hasNext());
        assertTrue(atlas.hasPrev());
        atlas.nextPage();
        assertEquals("C", atlas.getCurrentPageName());
        assertFalse(atlas.hasNext());
        atlas.prevPage();
        assertEquals("B", atlas.getCurrentPageName());
    }

    @Test
    public void testAtlasCoverageLayer() {
        LayoutAtlas atlas = new LayoutAtlas();
        atlas.setCoverageLayer("Municipios", "NOMBRE");
        assertEquals("Municipios", atlas.getCoverageLayerName());
        assertEquals("NOMBRE", atlas.getPageNameField());
    }

    @Test
    public void testAtlasEmptyNavigation() {
        LayoutAtlas atlas = new LayoutAtlas();
        atlas.setEnabled(true);
        atlas.nextPage();
        assertEquals(0, atlas.getCurrentPage());
        atlas.prevPage();
        assertEquals(0, atlas.getCurrentPage());
    }
}
