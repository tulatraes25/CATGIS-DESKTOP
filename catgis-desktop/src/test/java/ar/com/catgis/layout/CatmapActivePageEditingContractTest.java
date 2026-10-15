package ar.com.catgis.layout;

import ar.com.catgis.catmap.CatmapSerializer;
import ar.com.catgis.catmap.LayoutPreviewPanel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.SwingUtilities;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Genuine current-page editing, history, navigation and serialization contracts. */
class CatmapActivePageEditingContractTest {

    @TempDir Path tempDir;

    private static LayoutLabel label(String id, String title, double x) {
        return new LayoutLabel(id, title, x, 10, 100, 20);
    }

    @Test
    void addingAndDeletingElementsCannotAffectAnotherPage() {
        LayoutModel model = new LayoutModel();
        model.addElement(label("a", "Primera pagina", 10));
        model.addPage();
        assertEquals(0, model.size());
        model.addElement(label("b", "Segunda pagina", 40));
        assertEquals("b", model.getElements().get(0).getId());
        assertEquals(1, model.getVisibleElementsSortedByZ().size());
        assertEquals(0, model.getVisibleElementsSortedByZ().get(0).getZOrder());
        model.removeElement("a"); // ID belongs to a different page.
        assertEquals(1, model.size());
        model.setCurrentPage(0);
        assertEquals(1, model.size());
        assertEquals("a", model.getElements().get(0).getId());
        model.removeElement("a");
        assertEquals(0, model.size());
        model.setCurrentPage(1);
        assertEquals(1, model.size());
        assertEquals("b", model.getElements().get(0).getId());
    }

    @Test
    void eachPageHasItsOwnUndoAndRedoHistory() {
        LayoutModel model = new LayoutModel();
        model.addElement(label("page0", "Primera", 10));
        model.addPage();
        model.addElement(label("page1", "Segunda", 50));
        model.setCurrentPage(0);
        assertTrue(model.canUndo());
        model.undo();
        assertEquals(0, model.size());
        assertTrue(model.canRedo());
        model.setCurrentPage(1);
        assertTrue(model.canUndo());
        assertFalse(model.canRedo());
        assertEquals("page1", model.getElements().get(0).getId());
        model.undo();
        assertTrue(model.getElements().isEmpty());
        model.redo();
        assertEquals("page1", model.getElements().get(0).getId());
        model.setCurrentPage(0);
        model.redo();
        assertEquals("page0", model.getElements().get(0).getId());
        model.setCurrentPage(1);
        assertEquals("page1", model.getElements().get(0).getId());
    }

    @Test
    void selectionHitTestingZOrderAndGroupsAreRestrictedToActivePage() {
        LayoutModel model = new LayoutModel();
        LayoutLabel first = label("first", "primera", 10);
        model.addElement(first);
        first.setSelected(true);
        model.addPage();
        LayoutLabel second = label("second", "segunda", 10);
        LayoutLabel second2 = label("second2", "separada", 130);
        model.addElement(second);
        model.addElement(second2);
        second.setSelected(true);
        assertEquals("second", model.getSelected().getId());
        assertEquals("second", model.findElementAtMm(15, 15).getId());
        assertEquals(2, model.getElements().size());
        assertThrows(IllegalArgumentException.class, () -> model.moveToFront(first));
        assertThrows(IllegalArgumentException.class,
                () -> model.groupElements(List.of(first, second)));
        String group = model.groupElements(List.of(second, second2));
        assertEquals(2, model.getGroupMembers(group).size());
        assertEquals(2, model.getGroupedElements().size());
        model.moveToFront(second);
        assertTrue(second.getZOrder() > second2.getZOrder());
        model.clearSelection();
        assertFalse(second.isSelected());
        model.setCurrentPage(0);
        assertEquals("first", model.getSelected().getId());
        assertTrue(first.isSelected());
        assertTrue(model.getGroupedElements().isEmpty());
        assertEquals("first", model.findElementAtMm(15, 15).getId());
    }

    @Test
    void realFileRoundtripPreservesEditedPageAndSelectedPage() throws Exception {
        LayoutModel model = new LayoutModel();
        model.addElement(label("title0", "Portada", 12));
        model.addPage();
        model.addElement(label("title1", "Detalle", 20));
        model.saveSnapshot();
        ((LayoutLabel) model.getElements().get(0)).setText("Detalle corregido");
        Path output = tempDir.resolve("CATMAP paginas activas.catmap");
        CatmapSerializer.save(model, output.toFile());
        LayoutModel loaded = CatmapSerializer.load(output.toFile());
        assertEquals(2, loaded.getPageCount());
        assertEquals(1, loaded.getCurrentPage());
        assertEquals("Detalle corregido",
                ((LayoutLabel) loaded.getElements().get(0)).getText());
        loaded.setCurrentPage(0);
        assertEquals("Portada", ((LayoutLabel) loaded.getElements().get(0)).getText());
        loaded.setCurrentPage(1);
        loaded.addElement(label("extra", "Nueva capa", 60));
        loaded.undo();
        assertEquals(1, loaded.size());
        loaded.setCurrentPage(0);
        assertEquals(1, loaded.size());
    }

    @Test
    void realMouseDragCanBeUndoneOnCurrentPageWithoutTouchingPageZero() throws Exception {
        LayoutModel model = new LayoutModel();
        model.addElement(label("stable", "Portada", 10));
        model.addPage();
        model.addElement(label("drag-me", "Mover esta", 10));
        LayoutPreviewPanel preview = new LayoutPreviewPanel(model,
                new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, 96, 297, 210));
        preview.setSize(800, 600);
        SwingUtilities.invokeAndWait(() -> {
            long when = System.currentTimeMillis();
            preview.dispatchEvent(new MouseEvent(preview, MouseEvent.MOUSE_PRESSED, when,
                    InputEvent.BUTTON1_DOWN_MASK, 50, 50, 1, false, MouseEvent.BUTTON1));
            preview.dispatchEvent(new MouseEvent(preview, MouseEvent.MOUSE_DRAGGED, when + 1,
                    InputEvent.BUTTON1_DOWN_MASK, 80, 60, 0, false, MouseEvent.BUTTON1));
            preview.dispatchEvent(new MouseEvent(preview, MouseEvent.MOUSE_RELEASED, when + 2,
                    0, 80, 60, 1, false, MouseEvent.BUTTON1));
        });
        assertEquals(1, model.getElements().size());
        double movedX = model.getElements().get(0).getBoundsMm().x;
        assertNotEquals(10d, movedX);
        assertTrue(model.canUndo());
        model.undo();
        preview.clearPageSelection();
        assertEquals(10d, model.getElements().get(0).getBoundsMm().x);
        model.setCurrentPage(0);
        assertEquals(10d, model.getElements().get(0).getBoundsMm().x);
        assertEquals("stable", model.getElements().get(0).getId());
        assertNotNull(preview);
    }
}
