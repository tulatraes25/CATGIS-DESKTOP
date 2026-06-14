package ar.com.catgis.layout;

import ar.com.catgis.CatmapLayoutItem;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

/**
 * View-facing contract that decouples {@code LayoutPreviewPanel} from
 * {@code MapLayoutComposerDialog}. Every method that the preview panel
 * calls on the dialog is exposed here so the panel only depends on the
 * interface, not the concrete frame.
 */
public interface LayoutViewContext {

    // ── Model & state ──────────────────────────────────────────

    LayoutModel getLayoutModel();
    LayoutInteractionState getInteractionState();
    LayoutSnapshot getSnapshot();

    // ── Settings ───────────────────────────────────────────────

    LayoutSettings buildSettings();

    // ── Status feedback ────────────────────────────────────────

    void setStatusMessage(String msg);

    // ── UI refresh / sync ──────────────────────────────────────

    void refreshElementList();
    void syncLayoutStructureSelection();
    void repaintCanvas();
    void syncHardcodedLayoutFlagsFromModel();
    void updateScaleUiState(double exactScaleDenominator);

    // ── Selection ──────────────────────────────────────────────

    void selectItemInList(String id);
    CatmapLayoutItem getCatmapItem(String id);

    // ── Element operations ─────────────────────────────────────

    void persistCatmapItems();
    String elementLabel(LayoutElementType type);
    boolean isElementLocked(LayoutElementType type);

    // ── Popups / dialogs ───────────────────────────────────────

    void showCartouchePopup(LayoutCartouche el);
    void showScalePopup(LayoutScaleBar el);
    void showNorthPopup(LayoutNorthArrow el);
    void showMapPropsPopup(LayoutMap el);
    void openLegendEditor();
    void configureNorthFromToolbar();
    void editSelectedCatmapItem();
    void refreshPropertiesPanel();

    /**
     * Dispatches to the appropriate popup for the given element type.
     */
    void showPopupForElement(LayoutElement el);

    // ── Actions ────────────────────────────────────────────────

    void pushUndo(LayoutElement el, boolean isDelete);
    void pushUndoGroup(List<LayoutElement> elements);
    void resizeCanvasElement(int handleIndex, int dx, int dy);
    void populateLegend(LayoutLegend legend);
    void activateMapPanTool();
    void duplicateLayoutElement(LayoutElement src);
    int countOfType(String prefix);

    // ── Inline editing (cartouche / title fields) ──────────────

    String getTitleFieldText();
    void setTitleFieldText(String text);
    String getStudyFieldText();
    void setStudyFieldText(String text);
    String getProjectFieldText();
    void setProjectFieldText(String text);
    String getCompanyFieldText();
    void setCompanyFieldText(String text);
    String getCartographerFieldText();
    void setCartographerFieldText(String text);
    String getSourceFieldText();
    void setSourceFieldText(String text);
    String getCrsFieldText();
    void setCrsFieldText(String text);

    // ── Deprecated drag state (temporary bridge) ───────────────

    LayoutElement getDraggingLayoutElement();
    void setDraggingLayoutElement(LayoutElement el);
    Point getDragStartPagePoint();
    void setDragStartPagePoint(Point p);
    java.awt.geom.Rectangle2D.Double getDragStartBoundsMm();
    void setDragStartBoundsMm(java.awt.geom.Rectangle2D.Double r);
    int getActiveResizeHandleIndex();
    void setActiveResizeHandleIndex(int index);

    // ── Geometry helpers ───────────────────────────────────────

    RectMm toPageRectMm();
    int hitTestHandle(LayoutElement el, Point pagePoint, RectMm pageRect);

    // ── Rendering ──────────────────────────────────────────────

    void drawLayoutModelOverlay(Graphics2D g2, LayoutSettings settings,
                                int pageX, int pageY, double scale);
    CanvasRenderer getCanvasRenderer();

    // ── Viewport ───────────────────────────────────────────────

    Dimension getPreviewViewportSize();
}
