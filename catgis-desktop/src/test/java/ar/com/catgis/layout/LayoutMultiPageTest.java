package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutMultiPageTest {

    @Test
    public void testDefaultSinglePage() {
        LayoutModel model = new LayoutModel();
        assertEquals(1, model.getPageCount());
        assertEquals(0, model.getCurrentPage());
    }

    @Test
    public void testAddPage() {
        LayoutModel model = new LayoutModel();
        model.addPage();
        assertEquals(2, model.getPageCount());
        assertEquals(1, model.getCurrentPage());
    }

    @Test
    public void testAddMultiplePages() {
        LayoutModel model = new LayoutModel();
        model.addPage(); model.addPage(); model.addPage();
        assertEquals(4, model.getPageCount());
    }

    @Test
    public void testPageNavigation() {
        LayoutModel model = new LayoutModel();
        model.addPage(); model.addPage();
        assertEquals(3, model.getPageCount());
        model.setCurrentPage(0);
        assertEquals(0, model.getCurrentPage());
        model.setCurrentPage(2);
        assertEquals(2, model.getCurrentPage());
    }

    @Test
    public void testCurrentPageElements() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r1 = new LayoutRectangle("r1", 0, 0, 100, 100);
        r1.setZOrder(1); model.addElement(r1);
        assertEquals(1, model.getCurrentPageElements().size());

        model.addPage();
        LayoutRectangle r2 = new LayoutRectangle("r2", 0, 0, 50, 50);
        r2.setZOrder(1);
        model.getCurrentPageElements().add(r2);
        assertEquals(1, model.getCurrentPageElements().size());

        model.setCurrentPage(0);
        assertEquals(1, model.getCurrentPageElements().size());
    }

    @Test
    public void testPageClamp() {
        LayoutModel model = new LayoutModel();
        model.setCurrentPage(999); // should clamp
        assertEquals(0, model.getCurrentPage());
        model.setCurrentPage(-5);
        assertEquals(0, model.getCurrentPage());
    }
}
