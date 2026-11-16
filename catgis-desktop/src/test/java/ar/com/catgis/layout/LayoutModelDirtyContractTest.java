package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unsaved-change tracking used by the safe exit confirmation. */
class LayoutModelDirtyContractTest {

    @Test
    void newModelIsCleanAndEditsMarkItDirty() {
        LayoutModel model = new LayoutModel();
        assertFalse(model.isDirty());
        model.addElement(new LayoutLabel("lbl", "Hola", 10, 10, 100, 14));
        assertTrue(model.isDirty());
        model.markSaved();
        assertFalse(model.isDirty());
    }

    @Test
    void pageChangesOnlyMarkDirtyWhenTheyDiffer() {
        LayoutModel model = new LayoutModel();
        model.markSaved();
        model.setPageSpec(model.getPageSpec());
        assertFalse(model.isDirty(), "same page spec must not dirty the document");
        model.setPageSpec(LayoutPageSpec.preset(PageSizePreset.A3,
                PageOrientation.PORTRAIT));
        assertTrue(model.isDirty());
        model.markSaved();
        model.setPageSpec(null);
        assertTrue(model.isDirty(), "resetting to the legacy A4 sheet is a change");
    }

    @Test
    void undoSnapshotsKeepTheModelDirtyUntilSaved() {
        LayoutModel model = new LayoutModel();
        model.addElement(new LayoutLabel("lbl", "A", 0, 0, 10, 10));
        model.markSaved();
        model.undo();
        assertTrue(model.isDirty(), "undo/redo are edits and must dirty the document");
    }
}