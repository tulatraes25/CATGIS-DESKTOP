package ar.com.catgis;

import ar.com.catgis.service.EventBus;
import org.locationtech.jts.geom.Coordinate;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.SwingUtilities;

/**
 * Handles mouse interaction for MapPanel.
 * Extracted from MapPanel to reduce its scope.
 */
public class MapInteractionHandler extends MouseAdapter {

    private final MapPanel panel;

    public MapInteractionHandler(MapPanel panel) {
        this.panel = panel;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isMiddleMouseButton(e)) {
            panel.beginTemporaryMiddlePan(e);
            return;
        }
        if (panel.cadPlacementDragActive) {
            if (SwingUtilities.isRightMouseButton(e)) {
                panel.cancelCadPlacementDrag();
                return;
            }
            if (SwingUtilities.isLeftMouseButton(e)) {
                panel.beginCadPlacementDrag(e);
            }
            return;
        }
        if (panel.pointCaptureActive) {
            if (SwingUtilities.isRightMouseButton(e)) {
                panel.cancelPointCapture();
            }
            return;
        }
        if (panel.topographicProfileCaptureActive) {
            if (SwingUtilities.isRightMouseButton(e)) {
                panel.cancelTopographicProfileCapture();
            }
            return;
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            if (panel.isDrawingActive() || panel.isMeasurementActive()) {
                panel.showMapPopup(e);
                return;
            }
        }

        if (panel.featureEditMode && MapPanel.EDIT_OP_MOVE_VERTEX.equals(panel.featureEditOperation) && SwingUtilities.isLeftMouseButton(e)) {
            int vertexIndex = panel.findEditableVertexIndex(e.getX(), e.getY());
            if (vertexIndex >= 0) {
                panel.pushUndoSnapshotForSelectedLayer();
                panel.activeEditVertexIndex = vertexIndex;
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return;
            }
        }

        if (SwingUtilities.isLeftMouseButton(e)
                && MapPanel.EDIT_OP_MOVE_FEATURE.equals(panel.featureEditOperation)
                && panel.hasFeatureSelection()
                && panel.isHitOnCurrentSelection(e.getX(), e.getY())) {
            panel.pushUndoSnapshotForSelectedLayer();
            panel.movingSelectedFeatures = true;
            panel.moveSelectionLastProjectX = panel.screenToWorldX(e.getX());
            panel.moveSelectionLastProjectY = panel.screenToWorldY(e.getY());
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }

        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
            panel.showMapPopup(e);
            return;
        }

        if (panel.isDrawingActive() || panel.isMeasurementActive()) {
            return;
        }

        if (SwingUtilities.isLeftMouseButton(e)
                && "SELECT".equalsIgnoreCase(panel.currentTool)
                && (!panel.featureEditMode
                || MapPanel.EDIT_OP_MOVE_VERTEX.equals(panel.featureEditOperation)
                || MapPanel.EDIT_OP_ADD_VERTEX.equals(panel.featureEditOperation)
                || MapPanel.EDIT_OP_REMOVE_VERTEX.equals(panel.featureEditOperation)
                || MapPanel.EDIT_OP_JOIN_VERTEX.equals(panel.featureEditOperation))) {
            panel.selectionBoxActive = true;
            panel.selectionBoxDragging = false;
            panel.selectionBoxStartX = e.getX();
            panel.selectionBoxStartY = e.getY();
            panel.selectionBoxEndX = e.getX();
            panel.selectionBoxEndY = e.getY();
            return;
        }

        if (SwingUtilities.isLeftMouseButton(e)) {
            PinMarker pin = panel.findPinAtScreen(e.getX(), e.getY());
            if (pin != null) {
                panel.activePin = pin;
                panel.draggingPin = true;
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return;
            }
        }

        if ("MOVE".equalsIgnoreCase(panel.currentTool)) {
            panel.dragging = true;
            panel.captureViewDragStart();
            panel.lastMouseX = e.getX();
            panel.lastMouseY = e.getY();
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (panel.temporaryMiddlePanActive && SwingUtilities.isMiddleMouseButton(e)) {
            panel.finishTemporaryMiddlePan();
            return;
        }
        if (panel.cadPlacementDragActive) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                panel.finishCadPlacementDrag();
            }
            return;
        }
        if (panel.pointCaptureActive) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            return;
        }
        if (panel.topographicProfileCaptureActive) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            return;
        }
        if (e.isPopupTrigger()) {
            if (!panel.isDrawingActive() && !panel.isMeasurementActive()) {
                panel.showMapPopup(e);
            }
            return;
        }

        if (panel.draggingPin) {
            panel.draggingPin = false;
            if (panel.activePin != null) {
                panel.showCopiedMessage("Pin P" + panel.activePin.getId() + " movido.");
            }
        }

        if (panel.activeEditVertexIndex >= 0) {
            panel.activeEditVertexIndex = -1;
            EventBus.emit(EventBus.EventType.PROJECT_DIRTY_STATE_CHANGED, true);
            panel.showCopiedMessage("Vertice actualizado.");
            panel.refreshEditingUi();
        }

        if (panel.movingSelectedFeatures) {
            panel.movingSelectedFeatures = false;
            panel.moveSelectionLastProjectX = Double.NaN;
            panel.moveSelectionLastProjectY = Double.NaN;
            EventBus.emit(EventBus.EventType.PROJECT_DIRTY_STATE_CHANGED, true);
            panel.showCopiedMessage(panel.getSelectedFeatureCount() <= 1
                    ? "Entidad movida."
                    : panel.getSelectedFeatureCount() + " entidades movidas.");
            panel.refreshEditingUi();
        }

        if (panel.selectionBoxActive && "SELECT".equalsIgnoreCase(panel.currentTool)) {
            boolean shouldSelectByBox = panel.selectionBoxDragging;
            Rectangle boxBounds = panel.getSelectionBoxBounds();
            panel.selectionBoxActive = false;
            panel.selectionBoxDragging = false;
            if (shouldSelectByBox) {
                if (panel.featureEditMode && MapPanel.EDIT_OP_REMOVE_VERTEX.equals(panel.featureEditOperation)) {
                    panel.removeVerticesFromSelectedGeometry(boxBounds);
                } else if (panel.featureEditMode && MapPanel.EDIT_OP_JOIN_VERTEX.equals(panel.featureEditOperation)) {
                    panel.joinVerticesFromSelection(boxBounds);
                } else if (panel.featureEditMode && MapPanel.EDIT_OP_ADD_VERTEX.equals(panel.featureEditOperation)) {
                    panel.addVertexToSelectedGeometry(boxBounds.x + (boxBounds.width / 2), boxBounds.y + (boxBounds.height / 2));
                } else {
                    panel.selectFeatureForEditing(boxBounds, e.isControlDown());
                }
                panel.suppressNextSelectClick = true;
            }
            panel.repaint();
        }

        if (panel.dragging && ("MOVE".equalsIgnoreCase(panel.currentTool))) {
            panel.rememberViewState(panel.dragStartViewMinX, panel.dragStartViewMinY, panel.zoomFactor);
            panel.rememberCurrentView();
            panel.refreshStatusBarScale();
        }

        panel.dragging = false;

        panel.applyCursorForCurrentMode();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        panel.updateStatusCoordinates(e.getX(), e.getY());

        panel.updateHoverAndSnap(e.getX(), e.getY());

        if (panel.temporaryMiddlePanActive) {
            panel.dragViewTemporarily(e);
            return;
        }

        if (panel.topographicProfileCaptureActive) {
            panel.repaint();
            return;
        }

        if (panel.cadPlacementDragActive && panel.cadPlacementDragStarted) {
            panel.updateCadPlacementDrag(e);
            return;
        }

        if (panel.draggingPin && panel.activePin != null) {
            panel.activePin.setX(panel.screenToWorldX(e.getX()));
            panel.activePin.setY(panel.screenToWorldY(e.getY()));
            panel.repaint();
            return;
        }

        if (panel.activeEditVertexIndex >= 0 && panel.featureEditMode && MapPanel.EDIT_OP_MOVE_VERTEX.equals(panel.featureEditOperation)) {
            Coordinate targetCoordinate = panel.resolveInteractiveCoordinate(e.getX(), e.getY(), true);
            panel.moveSelectedVertex(targetCoordinate.x, targetCoordinate.y, panel.activeEditVertexIndex);
            panel.repaint();
            return;
        }

        if (panel.movingSelectedFeatures && MapPanel.EDIT_OP_MOVE_FEATURE.equals(panel.featureEditOperation)) {
            panel.moveSelectedFeatures(panel.screenToWorldX(e.getX()), panel.screenToWorldY(e.getY()));
            panel.repaint();
            return;
        }

        if (panel.selectionBoxActive && "SELECT".equalsIgnoreCase(panel.currentTool)) {
            panel.selectionBoxEndX = e.getX();
            panel.selectionBoxEndY = e.getY();
            panel.selectionBoxDragging = Math.abs(panel.selectionBoxEndX - panel.selectionBoxStartX) >= MapPanel.SELECTION_BOX_DRAG_THRESHOLD_PX
                    || Math.abs(panel.selectionBoxEndY - panel.selectionBoxStartY) >= MapPanel.SELECTION_BOX_DRAG_THRESHOLD_PX;
            panel.repaint();
            return;
        }

        if (!panel.dragging || !"MOVE".equalsIgnoreCase(panel.currentTool) || panel.isDrawingActive() || panel.isMeasurementActive()) {
            panel.repaint();
            return;
        }

        int dx = e.getX() - panel.lastMouseX;
        int dy = e.getY() - panel.lastMouseY;

        panel.viewController.setViewMinX(panel.viewController.getViewMinX() - dx / panel.viewController.getZoomFactor());
        panel.viewController.setViewMinY(panel.viewController.getViewMinY() + dy / panel.viewController.getZoomFactor());

        panel.lastMouseX = e.getX();
        panel.lastMouseY = e.getY();

        panel.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        panel.updateStatusCoordinates(e.getX(), e.getY());

        panel.updateHoverAndSnap(e.getX(), e.getY());

        if (panel.cadPlacementDragActive) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            panel.repaint();
            return;
        }

        if (panel.pointCaptureActive) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            panel.repaint();
            return;
        }

        if (panel.topographicProfileCaptureActive) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            panel.repaint();
            return;
        }

        if (panel.isDrawingActive() || panel.isMeasurementActive()) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            panel.repaint();
            return;
        }

        PinMarker pin = panel.findPinAtScreen(e.getX(), e.getY());
        if (pin != null) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (panel.featureEditMode && MapPanel.EDIT_OP_MOVE_VERTEX.equals(panel.featureEditOperation) && panel.findEditableVertexIndex(e.getX(), e.getY()) >= 0) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (MapPanel.EDIT_OP_MOVE_FEATURE.equals(panel.featureEditOperation) && panel.hasFeatureSelection() && panel.isHitOnCurrentSelection(e.getX(), e.getY())) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (panel.featureEditMode && (MapPanel.EDIT_OP_MOVE_VERTEX.equals(panel.featureEditOperation)
                || MapPanel.EDIT_OP_ADD_VERTEX.equals(panel.featureEditOperation)
                || MapPanel.EDIT_OP_REMOVE_VERTEX.equals(panel.featureEditOperation)
                || MapPanel.EDIT_OP_JOIN_VERTEX.equals(panel.featureEditOperation)
                || MapPanel.EDIT_OP_ADJACENT_POLYGON.equals(panel.featureEditOperation)
                || MapPanel.EDIT_OP_CUT.equals(panel.featureEditOperation)
                || MapPanel.EDIT_OP_EXTEND_LINE.equals(panel.featureEditOperation)
                || MapPanel.EDIT_OP_SHORTEN_LINE.equals(panel.featureEditOperation)
                || MapPanel.EDIT_OP_PARALLEL.equals(panel.featureEditOperation)
                || MapPanel.EDIT_OP_PERPENDICULAR.equals(panel.featureEditOperation))) {
            panel.setCursor(panel.resolveFeatureEditCursor());
        } else if (MapPanel.EDIT_OP_MOVE_FEATURE.equals(panel.featureEditOperation) && panel.hasFeatureSelection()) {
            panel.setCursor(panel.resolveFeatureEditCursor());
        } else if (panel.featureEditMode && panel.isFeatureEditSketchMode()) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else if ("MOVE".equalsIgnoreCase(panel.currentTool)) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } else if ("IDENTIFY".equalsIgnoreCase(panel.currentTool)) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            panel.setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        panel.hoverWorldX = Double.NaN;
        panel.hoverWorldY = Double.NaN;
        panel.snapManager.setSnapPreviewCoordinate(null);
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.clearStatusCoordinates();
        }
        panel.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (panel.suppressNextSelectClick) {
            panel.suppressNextSelectClick = false;
            return;
        }

        if (panel.pointCaptureActive) {
            if (SwingUtilities.isRightMouseButton(e)) {
                panel.cancelPointCapture();
                return;
            }
            if (SwingUtilities.isLeftMouseButton(e)) {
                Coordinate coordinate = panel.resolveInteractiveCoordinate(e.getX(), e.getY(), false);
                panel.finishPointCapture(coordinate);
            }
            return;
        }

        if (panel.cadPlacementDragActive) {
            if (SwingUtilities.isRightMouseButton(e)) {
                panel.cancelCadPlacementDrag();
            }
            return;
        }

        if (panel.topographicProfileCaptureActive) {
            if (SwingUtilities.isRightMouseButton(e)) {
                if (panel.topographicProfileCaptureCoordinates.size() >= 2) {
                    panel.finishTopographicProfileCapture();
                } else {
                    panel.cancelTopographicProfileCapture();
                }
                return;
            }
            if (SwingUtilities.isLeftMouseButton(e)) {
                Coordinate coordinate = panel.resolveInteractiveCoordinate(e.getX(), e.getY(), false);
                panel.topographicProfileCaptureCoordinates.add(coordinate);
                if (CatgisDesktopApp.statusBar != null) {
                    if (panel.topographicProfileCaptureCoordinates.size() == 1) {
                        AppContext.setStatusMessage(I18n.t("Perfil topografico: agrega otro punto para comenzar el trazado."));
                    } else {
                        AppContext.setStatusMessage(I18n.t("Perfil topografico: agrega mas vertices o clic derecho para terminar el trazado."));
                    }
                }
                panel.repaint();
            }
            return;
        }

        if (SwingUtilities.isRightMouseButton(e) || panel.draggingPin) {
            return;
        }

        if (panel.featureEditMode && SwingUtilities.isLeftMouseButton(e) && panel.handleFeatureEditClick(e)) {
            return;
        }

        if (panel.isDrawingActive() && SwingUtilities.isLeftMouseButton(e)) {
            Coordinate c = panel.resolveInteractiveCoordinate(e.getX(), e.getY(), false);

            if ("POINT".equalsIgnoreCase(panel.drawingToolManager.drawingMode) || "MULTIPOINT".equalsIgnoreCase(panel.drawingToolManager.drawingMode)) {
                panel.drawingToolManager.drawingCoordinates.add(c);
                panel.repaint();
                return;
            }

            if ("CIRCLE".equalsIgnoreCase(panel.drawingToolManager.drawingMode)) {
                panel.appendDrawingCoordinateIfNeeded(c);
                if (panel.drawingToolManager.drawingCoordinates.size() >= 2) {
                    panel.finishCurrentDrawing();
                } else {
                    panel.repaint();
                }
                return;
            }

            if ("CIRCLE_3P".equalsIgnoreCase(panel.drawingToolManager.drawingMode)) {
                panel.appendDrawingCoordinateIfNeeded(c);
                if (panel.drawingToolManager.drawingCoordinates.size() >= 3) {
                    panel.finishCurrentDrawing();
                } else {
                    panel.repaint();
                }
                return;
            }

            if ("CONTINUE_LINE".equalsIgnoreCase(panel.drawingToolManager.drawingMode) && !panel.drawingToolManager.drawingContinuationEndpointChosen) {
                panel.chooseContinuationEndpoint(e.getX(), e.getY());
                return;
            }

            if ("RECTANGLE".equalsIgnoreCase(panel.drawingToolManager.drawingMode)) {
                panel.appendDrawingCoordinateIfNeeded(c);
                if (panel.drawingToolManager.drawingCoordinates.size() >= 2) {
                    panel.finishCurrentDrawing();
                } else {
                    panel.repaint();
                }
                return;
            }

            if (e.getClickCount() >= 2) {
                panel.appendDrawingCoordinateIfNeeded(c);
                if (!panel.drawingToolManager.drawingCoordinates.isEmpty()) {
                    panel.finishCurrentDrawing();
                }
                return;
            }

            panel.appendDrawingCoordinateIfNeeded(c);
            panel.repaint();
            return;
        }

        if (panel.isMeasurementActive() && SwingUtilities.isLeftMouseButton(e)) {
            Coordinate c = panel.resolveInteractiveCoordinate(e.getX(), e.getY(), false);

            if (e.getClickCount() >= 2) {
                if (!panel.measurementTool.getPoints().isEmpty()) {
                    panel.finishCurrentMeasurement();
                }
                return;
            }

            panel.measurementTool.addPoint(c.x, c.y);
            panel.repaint();
            return;
        }

        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
            panel.showCoordinateDialog(e.getX(), e.getY());
            return;
        }

        if ("IDENTIFY".equalsIgnoreCase(panel.currentTool)) {
            panel.identifyFeature(e.getX(), e.getY());
        } else if ("SELECT".equalsIgnoreCase(panel.currentTool)) {
            panel.selectFeatureForEditing(e.getX(), e.getY(), e.isControlDown());
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        panel.handleZoom(e);
    }
}
