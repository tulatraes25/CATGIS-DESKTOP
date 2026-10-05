package ar.com.catgis.ui.components.layout;

import ar.com.catgis.layout.LayoutElement;

/**
 * All toolbar actions the preview toolbar can invoke.
 * The dialog implements this interface and passes itself as the callback.
 */
public interface PreviewToolbarActions {
    void saveLayout();
    void loadLayout();
    void exportPdf();
    void exportImage();
    void exportSvg();
    void printLayout();
    void showTemplatePicker();

    void activateSelectionTool();
    void activateMapPanTool();
    void activateMapFrameZoomTool();

    void adjustMapZoom(double factor);
    void resetMapFrameView();
    void refreshSnapshot();

    void adjustPageZoom(double factor);
    void fitPageView();
    void fitWidthView();
    void resetLayoutView();

    void alignElements(int mode);

    void duplicateLayoutElement(LayoutElement src);
    void refreshElementList();
    void repaintPreview();

    void openElementProperties(LayoutElement el);

    void startDrawing(String type);
}
