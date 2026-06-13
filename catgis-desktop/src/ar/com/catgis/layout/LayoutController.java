package ar.com.catgis.layout;

/**
 * Orchestrator for the layout subsystem.
 * Coordinates model, canvas, selection, toolbar, and element list.
 */
public class LayoutController {

    private final LayoutModel model;
    private CanvasRenderer canvasRenderer;
    private LayoutSelectionManager selectionManager;
    private ElementListPanel elementListPanel;
    private ElementInspectorPanel inspectorPanel;

    public LayoutController(LayoutModel model) {
        this.model = model;
    }

    public void setCanvasRenderer(CanvasRenderer cr) { canvasRenderer = cr; }
    public void setSelectionManager(LayoutSelectionManager sm) { selectionManager = sm; }
    public void setElementListPanel(ElementListPanel elp) { elementListPanel = elp; }
    public void setInspectorPanel(ElementInspectorPanel ip) { inspectorPanel = ip; }

    public LayoutModel getModel() { return model; }
    public CanvasRenderer getCanvasRenderer() { return canvasRenderer; }
    public LayoutSelectionManager getSelectionManager() { return selectionManager; }

    // --- Element operations ---

    public void addMapFrame() { addDefaultElement("map"); }
    public void addLegend() { addDefaultElement("legend"); }
    public void addScaleBar() { addDefaultElement("scale"); }
    public void addNorthArrow() { addDefaultElement("north"); }
    public void addText() { addDefaultElement("text"); }
    public void addImage() { addDefaultElement("image"); }

    private void addDefaultElement(String type) {
        // Delegate to the concrete implementation (MapLayoutComposerDialog)
        // This is a placeholder — real implementation creates LayoutElement subtypes
    }

    public void deleteSelected() {
        LayoutElement sel = model.getSelected();
        if (sel != null) {
            model.removeElement(sel.getId());
            refreshAll();
        }
    }

    public void duplicateSelected() {
        LayoutElement sel = model.getSelected();
        if (sel != null) {
            // Clone logic delegated to concrete implementation
        }
    }

    // --- Undo / Redo ---

    public void undo() {
        if (model.canUndo()) {
            model.undo();
            refreshAll();
        }
    }

    public void redo() {
        if (model.canRedo()) {
            model.redo();
            refreshAll();
        }
    }

    // --- Export ---

    public void exportPdf() {
        // Delegated to concrete implementation
    }

    // --- Refresh ---

    public void refreshAll() {
        if (elementListPanel != null) elementListPanel.refresh();
        if (inspectorPanel != null) inspectorPanel.refresh();
    }

    /**
     * Notify that the selection changed from the element list.
     */
    public void onListSelectionChanged() {
        if (elementListPanel == null) return;
        LayoutElement sel = elementListPanel.getSelectedElement();
        if (sel != null && selectionManager != null) {
            selectionManager.select(sel);
        }
        if (inspectorPanel != null) inspectorPanel.refresh();
    }

    /**
     * Notify that a canvas click occurred at mm coordinates.
     */
    public void onCanvasClicked(double xMm, double yMm) {
        if (selectionManager == null) return;
        LayoutElement hit = selectionManager.hitTest(xMm, yMm);
        if (hit != null) {
            selectionManager.select(hit);
        } else {
            selectionManager.clearSelection();
        }
        refreshAll();
    }
}
