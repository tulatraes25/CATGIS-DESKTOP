package ar.com.catgis.layout;

import java.awt.Dimension;
import java.awt.Point;
import java.util.EnumMap;
import java.util.EnumSet;

/**
 * Mutable state holder for layout composer interaction: selection,
 * element offsets, size adjustments, visibility, locking, zoom,
 * and pan state. Extracted from MapLayoutComposerDialog.
 */
public class LayoutInteractionState {
    private LayoutTemplate template = LayoutTemplate.TECHNICAL_RIGHT;
    private PreviewScaleMode previewScaleMode = PreviewScaleMode.FIT_PAGE;
    private double customPreviewZoom = 1d;
    private double mapZoom = 1d;
    private double mapOffsetX = 0d;
    private double mapOffsetY = 0d;
    private MapFrameTool mapFrameTool = MapFrameTool.MOVE_FRAME;
    private LayoutElementType selectedElement = null;
    private String selectedCustomItemId = null;
    private final EnumMap<LayoutElementType, Point> elementOffsets = new EnumMap<>(LayoutElementType.class);
    private final EnumMap<LayoutElementType, Dimension> elementSizeAdjustments = new EnumMap<>(LayoutElementType.class);
    private final EnumSet<LayoutElementType> hiddenElements = EnumSet.noneOf(LayoutElementType.class);
    private final EnumSet<LayoutElementType> lockedElements = EnumSet.noneOf(LayoutElementType.class);

    public LayoutTemplate getTemplate() {
        return template;
    }

    public void setTemplate(LayoutTemplate template) {
        this.template = template != null ? template : LayoutTemplate.TECHNICAL_RIGHT;
    }

    public void resetForTemplate(LayoutTemplate template) {
        setTemplate(template);
        previewScaleMode = PreviewScaleMode.FIT_PAGE;
        customPreviewZoom = 1d;
        resetMapView();
        elementOffsets.clear();
        elementSizeAdjustments.clear();
        hiddenElements.clear();
        lockedElements.clear();
        mapFrameTool = MapFrameTool.MOVE_FRAME;
        selectedElement = null;
        selectedCustomItemId = null;
    }

    public void fitPage() {
        previewScaleMode = PreviewScaleMode.FIT_PAGE;
        customPreviewZoom = 1d;
    }

    public void fitWidth() {
        previewScaleMode = PreviewScaleMode.FIT_WIDTH;
        customPreviewZoom = 1d;
    }

    public void zoomPreview(double factor) {
        previewScaleMode = PreviewScaleMode.CUSTOM;
        customPreviewZoom = Math.max(0.35d, Math.min(12d, customPreviewZoom * factor));
    }

    public double resolvePreviewScale(double fitPageScale, double fitWidthScale) {
        return switch (previewScaleMode) {
            case FIT_WIDTH -> Math.max(0.08d, fitWidthScale);
            case CUSTOM -> Math.max(0.08d, fitPageScale * customPreviewZoom);
            default -> Math.max(0.08d, fitPageScale);
        };
    }

    public void select(LayoutElementType elementType) {
        selectedElement = elementType;
        if (elementType != LayoutElementType.CATMAP_ITEM) {
            selectedCustomItemId = null;
        }
    }

    public void selectCustomItem(String itemId) {
        selectedElement = itemId != null && !itemId.isBlank() ? LayoutElementType.CATMAP_ITEM : null;
        selectedCustomItemId = itemId != null ? itemId.trim() : null;
    }

    public LayoutElementType getSelectedElement() {
        return selectedElement;
    }

    public String getSelectedCustomItemId() {
        return selectedCustomItemId;
    }

    public void setMapFrameTool(MapFrameTool mapFrameTool) {
        this.mapFrameTool = mapFrameTool != null ? mapFrameTool : MapFrameTool.MOVE_FRAME;
    }

    public boolean isMapFrameMoveToolActive() {
        return mapFrameTool == MapFrameTool.MOVE_FRAME;
    }

    public boolean isMapFramePanToolActive() {
        return mapFrameTool == MapFrameTool.PAN;
    }

    public boolean isMapFrameZoomToolActive() {
        return mapFrameTool == MapFrameTool.ZOOM;
    }

    public Point getOffset(LayoutElementType elementType) {
        Point point = elementOffsets.get(elementType);
        return point != null ? point : new Point();
    }

    public Dimension getSizeAdjustment(LayoutElementType elementType) {
        Dimension dimension = elementSizeAdjustments.get(elementType);
        return dimension != null ? dimension : new Dimension();
    }

    public boolean isElementVisible(LayoutElementType elementType) {
        return elementType != null && !hiddenElements.contains(elementType);
    }

    public void setElementVisible(LayoutElementType elementType, boolean visible) {
        if (!LayoutElementType.isFixed(elementType)) {
            return;
        }
        if (visible) {
            hiddenElements.remove(elementType);
        } else {
            hiddenElements.add(elementType);
        }
    }

    public boolean isElementLocked(LayoutElementType elementType) {
        return elementType != null && lockedElements.contains(elementType);
    }

    public void setElementLocked(LayoutElementType elementType, boolean locked) {
        if (!LayoutElementType.isFixed(elementType)) {
            return;
        }
        if (locked) {
            lockedElements.add(elementType);
        } else {
            lockedElements.remove(elementType);
        }
    }

    public void restoreDefaultElementControls() {
        hiddenElements.clear();
        lockedElements.clear();
        elementOffsets.clear();
        elementSizeAdjustments.clear();
        resetMapView();
    }

    public void translate(LayoutElementType elementType, int dx, int dy) {
        if (elementType == null) {
            return;
        }
        Point offset = elementOffsets.computeIfAbsent(elementType, key -> new Point());
        offset.translate(dx, dy);
    }

    public void resize(LayoutElementType elementType,
                ResizeHandle handle,
                int dx,
                int dy,
                int currentWidth,
                int currentHeight,
                int minWidth,
                int minHeight) {
        if (elementType == null || handle == null || handle == ResizeHandle.NONE) {
            return;
        }
        Point offset = elementOffsets.computeIfAbsent(elementType, key -> new Point());
        Dimension size = elementSizeAdjustments.computeIfAbsent(elementType, key -> new Dimension());
        int actualWidth = currentWidth;
        int actualHeight = currentHeight;
        int baseWidth = actualWidth - size.width;
        int baseHeight = actualHeight - size.height;

        switch (handle) {
            case EAST -> {
                int targetWidth = Math.max(minWidth, actualWidth + dx);
                size.width = targetWidth - baseWidth;
            }
            case SOUTH -> {
                int targetHeight = Math.max(minHeight, actualHeight + dy);
                size.height = targetHeight - baseHeight;
            }
            case SOUTH_EAST -> {
                int targetWidth = Math.max(minWidth, actualWidth + dx);
                int targetHeight = Math.max(minHeight, actualHeight + dy);
                size.width = targetWidth - baseWidth;
                size.height = targetHeight - baseHeight;
            }
            case WEST -> {
                int targetWidth = Math.max(minWidth, actualWidth - dx);
                int shift = actualWidth - targetWidth;
                offset.translate(shift, 0);
                size.width = targetWidth - baseWidth;
            }
            case NORTH -> {
                int targetHeight = Math.max(minHeight, actualHeight - dy);
                int shift = actualHeight - targetHeight;
                offset.translate(0, shift);
                size.height = targetHeight - baseHeight;
            }
            case NORTH_WEST -> {
                int targetWidth = Math.max(minWidth, actualWidth - dx);
                int targetHeight = Math.max(minHeight, actualHeight - dy);
                offset.translate(actualWidth - targetWidth, actualHeight - targetHeight);
                size.width = targetWidth - baseWidth;
                size.height = targetHeight - baseHeight;
            }
            case NORTH_EAST -> {
                int targetWidth = Math.max(minWidth, actualWidth + dx);
                int targetHeight = Math.max(minHeight, actualHeight - dy);
                offset.translate(0, actualHeight - targetHeight);
                size.width = targetWidth - baseWidth;
                size.height = targetHeight - baseHeight;
            }
            case SOUTH_WEST -> {
                int targetWidth = Math.max(minWidth, actualWidth - dx);
                int targetHeight = Math.max(minHeight, actualHeight + dy);
                offset.translate(actualWidth - targetWidth, 0);
                size.width = targetWidth - baseWidth;
                size.height = targetHeight - baseHeight;
            }
            default -> {
            }
        }
    }

    public boolean hasCustomSize(LayoutElementType elementType) {
        Dimension size = elementSizeAdjustments.get(elementType);
        return size != null && (size.width != 0 || size.height != 0);
    }

    public void zoomMap(double factor) {
        if (factor <= 0 || Double.isNaN(factor) || Double.isInfinite(factor)) {
            return;
        }
        setMapZoom(mapZoom * factor);
    }

    public void setMapZoom(double zoom) {
        if (zoom <= 0 || Double.isNaN(zoom) || Double.isInfinite(zoom)) {
            return;
        }
        mapZoom = Math.max(0.02d, Math.min(250d, zoom));
    }

    public void panMap(double dx, double dy) {
        mapOffsetX += dx;
        mapOffsetY += dy;
    }

    public void resetMapView() {
        mapZoom = 1d;
        mapOffsetX = 0d;
        mapOffsetY = 0d;
    }

    public double getMapZoom() {
        return mapZoom;
    }

    public double getMapOffsetX() {
        return mapOffsetX;
    }

    public double getMapOffsetY() {
        return mapOffsetY;
    }
}
