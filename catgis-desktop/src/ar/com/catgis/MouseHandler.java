package ar.com.catgis;

import javax.swing.SwingUtilities;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import org.locationtech.jts.geom.Coordinate;

class MouseHandler extends MouseAdapter {

    private final MapPanel map;

    MouseHandler(MapPanel map) {
        this.map = map;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isMiddleMouseButton(e)) {
            map.beginTemporaryMiddlePan(e);
            return;
        }
        if (map.cadEngine.cadPlacementDragActive) {
            if (SwingUtilities.isRightMouseButton(e)) {
                map.cancelCadPlacementDrag();
                return;
            }
            if (SwingUtilities.isLeftMouseButton(e)) {
                map.beginCadPlacementDrag(e);
            }
            return;
        }
        if (map.cadEngine.pointCaptureActive) {
            if (SwingUtilities.isRightMouseButton(e)) {
                map.cancelPointCapture();
            }
            return;
        }
        if (map.topographicProfileTool.isActive()) {
            if (SwingUtilities.isRightMouseButton(e)) {
                map.topographicProfileTool.cancelCapture();
            }
            return;
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            if (map.isDrawingActive() || map.isMeasurementActive()) {
                map.showMapPopup(e);
                return;
            }
        }

        if (map.featureEditMode && MapPanel.EDIT_OP_MOVE_VERTEX.equals(map.featureEditOperation) && SwingUtilities.isLeftMouseButton(e)) {
            int vertexIndex = map.findEditableVertexIndex(e.getX(), e.getY());
            if (vertexIndex >= 0) {
                map.pushUndoSnapshotForSelectedLayer();
                map.activeEditVertexIndex = vertexIndex;
                map.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return;
            }
        }

        if (SwingUtilities.isLeftMouseButton(e)
                && MapPanel.EDIT_OP_MOVE_FEATURE.equals(map.featureEditOperation)
                && map.hasFeatureSelection()
                && map.isHitOnCurrentSelection(e.getX(), e.getY())) {
            map.pushUndoSnapshotForSelectedLayer();
            map.movingSelectedFeatures = true;
            map.moveSelectionLastProjectX = map.screenToWorldX(e.getX());
            map.moveSelectionLastProjectY = map.screenToWorldY(e.getY());
            map.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }

        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
            map.showMapPopup(e);
            return;
        }

        if (map.isDrawingActive() || map.isMeasurementActive()) {
            return;
        }

        if (SwingUtilities.isLeftMouseButton(e)
                && "SELECT".equalsIgnoreCase(map.currentTool)
                && (!map.featureEditMode
                || MapPanel.EDIT_OP_MOVE_VERTEX.equals(map.featureEditOperation)
                || MapPanel.EDIT_OP_ADD_VERTEX.equals(map.featureEditOperation)
                || MapPanel.EDIT_OP_REMOVE_VERTEX.equals(map.featureEditOperation)
                || MapPanel.EDIT_OP_JOIN_VERTEX.equals(map.featureEditOperation))) {
            map.selectionBoxActive = true;
            map.selectionBoxDragging = false;
            map.selectionBoxStartX = e.getX();
            map.selectionBoxStartY = e.getY();
            map.selectionBoxEndX = e.getX();
            map.selectionBoxEndY = e.getY();
            return;
        }

        if (SwingUtilities.isLeftMouseButton(e)) {
            PinMarker pin = map.findPinAtScreen(e.getX(), e.getY());
            if (pin != null) {
                map.pinManager.activePin = pin;
                map.draggingPin = true;
                map.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return;
            }
        }

        if ("MOVE".equalsIgnoreCase(map.currentTool)) {
            map.dragging = true;
            map.captureViewDragStart();
            map.lastMouseX = e.getX();
            map.lastMouseY = e.getY();
            map.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (map.temporaryMiddlePanActive && SwingUtilities.isMiddleMouseButton(e)) {
            map.finishTemporaryMiddlePan();
            return;
        }
        if (map.cadEngine.cadPlacementDragActive) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                map.finishCadPlacementDrag();
            }
            return;
        }
        if (map.cadEngine.pointCaptureActive) {
            map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            return;
        }
        if (map.topographicProfileTool.isActive()) {
            map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            return;
        }
        if (e.isPopupTrigger()) {
            if (!map.isDrawingActive() && !map.isMeasurementActive()) {
                map.showMapPopup(e);
            }
            return;
        }

        if (map.draggingPin) {
            map.draggingPin = false;
            if (map.pinManager.getActivePin() != null) {
                map.showCopiedMessage("Pin P" + map.pinManager.getActivePin().getId() + " movido.");
            }
        }

        if (map.activeEditVertexIndex >= 0) {
            map.activeEditVertexIndex = -1;
            CatgisDesktopApp.markProjectDirty();
            map.showCopiedMessage("Vertice actualizado.");
            map.refreshEditingUi();
        }

        if (map.movingSelectedFeatures) {
            map.movingSelectedFeatures = false;
            map.moveSelectionLastProjectX = Double.NaN;
            map.moveSelectionLastProjectY = Double.NaN;
            CatgisDesktopApp.markProjectDirty();
            map.showCopiedMessage(map.getSelectedFeatureCount() <= 1
                    ? "Entidad movida."
                    : map.getSelectedFeatureCount() + " entidades movidas.");
            map.refreshEditingUi();
        }

        if (map.selectionBoxActive && "SELECT".equalsIgnoreCase(map.currentTool)) {
            boolean shouldSelectByBox = map.selectionBoxDragging;
            Rectangle boxBounds = map.getSelectionBoxBounds();
            map.selectionBoxActive = false;
            map.selectionBoxDragging = false;
            if (shouldSelectByBox) {
                if (map.featureEditMode && MapPanel.EDIT_OP_REMOVE_VERTEX.equals(map.featureEditOperation)) {
                    map.removeVerticesFromSelectedGeometry(boxBounds);
                } else if (map.featureEditMode && MapPanel.EDIT_OP_JOIN_VERTEX.equals(map.featureEditOperation)) {
                    map.joinVerticesFromSelection(boxBounds);
                } else if (map.featureEditMode && MapPanel.EDIT_OP_ADD_VERTEX.equals(map.featureEditOperation)) {
                    map.addVertexToSelectedGeometry(boxBounds.x + (boxBounds.width / 2), boxBounds.y + (boxBounds.height / 2));
                } else {
                    map.selectFeatureForEditing(boxBounds, e.isControlDown());
                }
                map.suppressNextSelectClick = true;
            }
            map.repaint();
        }

        if (map.dragging && ("MOVE".equalsIgnoreCase(map.currentTool))) {
            map.rememberViewState(map.dragStartViewMinX, map.dragStartViewMinY, map.zoomFactor);
            map.rememberCurrentView();
            map.refreshStatusBarScale();
        }

        map.dragging = false;

        map.applyCursorForCurrentMode();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        map.updateStatusCoordinates(e.getX(), e.getY());

        map.updateHoverAndSnap(e.getX(), e.getY());

        if (map.temporaryMiddlePanActive) {
            map.dragViewTemporarily(e);
            return;
        }

        if (map.topographicProfileTool.isActive()) {
            map.repaint();
            return;
        }

        if (map.cadEngine.cadPlacementDragActive && map.cadEngine.cadPlacementDragStarted) {
            map.updateCadPlacementDrag(e);
            return;
        }

        if (map.draggingPin && map.pinManager.getActivePin() != null) {
            map.pinManager.getActivePin().setX(map.screenToWorldX(e.getX()));
            map.pinManager.getActivePin().setY(map.screenToWorldY(e.getY()));
            map.repaint();
            return;
        }

        if (map.activeEditVertexIndex >= 0 && map.featureEditMode && MapPanel.EDIT_OP_MOVE_VERTEX.equals(map.featureEditOperation)) {
            Coordinate targetCoordinate = map.resolveInteractiveCoordinate(e.getX(), e.getY(), true);
            map.moveSelectedVertex(targetCoordinate.x, targetCoordinate.y, map.activeEditVertexIndex);
            map.repaint();
            return;
        }

        if (map.movingSelectedFeatures && MapPanel.EDIT_OP_MOVE_FEATURE.equals(map.featureEditOperation)) {
            map.moveSelectedFeatures(map.screenToWorldX(e.getX()), map.screenToWorldY(e.getY()));
            map.repaint();
            return;
        }

        if (map.selectionBoxActive && "SELECT".equalsIgnoreCase(map.currentTool)) {
            map.selectionBoxEndX = e.getX();
            map.selectionBoxEndY = e.getY();
            map.selectionBoxDragging = Math.abs(map.selectionBoxEndX - map.selectionBoxStartX) >= MapPanel.SELECTION_BOX_DRAG_THRESHOLD_PX
                    || Math.abs(map.selectionBoxEndY - map.selectionBoxStartY) >= MapPanel.SELECTION_BOX_DRAG_THRESHOLD_PX;
            map.repaint();
            return;
        }

        if (!map.dragging || !"MOVE".equalsIgnoreCase(map.currentTool) || map.isDrawingActive() || map.isMeasurementActive()) {
            map.repaint();
            return;
        }

        int dx = e.getX() - map.lastMouseX;
        int dy = e.getY() - map.lastMouseY;

        map.shiftViewByPixels(dx, dy);

        map.lastMouseX = e.getX();
        map.lastMouseY = e.getY();

        map.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        map.updateStatusCoordinates(e.getX(), e.getY());

        map.updateHoverAndSnap(e.getX(), e.getY());

        if (map.cadEngine.cadPlacementDragActive) {
            map.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            map.repaint();
            return;
        }

        if (map.cadEngine.pointCaptureActive) {
            map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            map.repaint();
            return;
        }

        if (map.topographicProfileTool.isActive()) {
            map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            map.repaint();
            return;
        }

        if (map.isDrawingActive() || map.isMeasurementActive()) {
            map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            map.repaint();
            return;
        }

        PinMarker pin = map.findPinAtScreen(e.getX(), e.getY());
        if (pin != null) {
            map.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (map.featureEditMode && MapPanel.EDIT_OP_MOVE_VERTEX.equals(map.featureEditOperation) && map.findEditableVertexIndex(e.getX(), e.getY()) >= 0) {
            map.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (MapPanel.EDIT_OP_MOVE_FEATURE.equals(map.featureEditOperation) && map.hasFeatureSelection() && map.isHitOnCurrentSelection(e.getX(), e.getY())) {
            map.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (map.featureEditMode && (MapPanel.EDIT_OP_MOVE_VERTEX.equals(map.featureEditOperation)
                || MapPanel.EDIT_OP_ADD_VERTEX.equals(map.featureEditOperation)
                || MapPanel.EDIT_OP_REMOVE_VERTEX.equals(map.featureEditOperation)
                || MapPanel.EDIT_OP_JOIN_VERTEX.equals(map.featureEditOperation)
                || MapPanel.EDIT_OP_ADJACENT_POLYGON.equals(map.featureEditOperation)
                || MapPanel.EDIT_OP_CUT.equals(map.featureEditOperation)
                || MapPanel.EDIT_OP_EXTEND_LINE.equals(map.featureEditOperation)
                || MapPanel.EDIT_OP_SHORTEN_LINE.equals(map.featureEditOperation)
                || MapPanel.EDIT_OP_PARALLEL.equals(map.featureEditOperation)
                || MapPanel.EDIT_OP_PERPENDICULAR.equals(map.featureEditOperation))) {
            map.setCursor(map.resolveFeatureEditCursor());
        } else if (MapPanel.EDIT_OP_MOVE_FEATURE.equals(map.featureEditOperation) && map.hasFeatureSelection()) {
            map.setCursor(map.resolveFeatureEditCursor());
        } else if (map.featureEditMode && map.isFeatureEditSketchMode()) {
            map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else if ("MOVE".equalsIgnoreCase(map.currentTool)) {
            map.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } else if ("IDENTIFY".equalsIgnoreCase(map.currentTool)) {
            map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            map.setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        map.hoverWorldX = Double.NaN;
        map.hoverWorldY = Double.NaN;
        map.snapManager.setSnapPreviewCoordinate(null);
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.clearStatusCoordinates();
        }
        map.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (map.suppressNextSelectClick) {
            map.suppressNextSelectClick = false;
            return;
        }

        if (map.cadEngine.pointCaptureActive) {
            if (SwingUtilities.isRightMouseButton(e)) {
                map.cancelPointCapture();
                return;
            }
            if (SwingUtilities.isLeftMouseButton(e)) {
                Coordinate coordinate = map.resolveInteractiveCoordinate(e.getX(), e.getY(), false);
                map.finishPointCapture(coordinate);
            }
            return;
        }

        if (map.cadEngine.cadPlacementDragActive) {
            if (SwingUtilities.isRightMouseButton(e)) {
                map.cancelCadPlacementDrag();
            }
            return;
        }

        if (map.topographicProfileTool.isActive()) {
            if (SwingUtilities.isRightMouseButton(e)) {
                if (map.topographicProfileTool.getCoordinates().size() >= 2) {
                    map.topographicProfileTool.finishCapture();
                } else {
                    map.topographicProfileTool.cancelCapture();
                }
                return;
            }
            if (SwingUtilities.isLeftMouseButton(e)) {
                Coordinate coordinate = map.resolveInteractiveCoordinate(e.getX(), e.getY(), false);
                map.topographicProfileTool.getCoordinates().add(coordinate);
                if (CatgisDesktopApp.statusBar != null) {
                    if (map.topographicProfileTool.getCoordinates().size() == 1) {
                        AppContext.setStatusMessage(I18n.t("Perfil topografico: agrega otro punto para comenzar el trazado."));
                    } else {
                        AppContext.setStatusMessage(I18n.t("Perfil topografico: agrega mas vertices o clic derecho para terminar el trazado."));
                    }
                }
                map.repaint();
            }
            return;
        }

        if (SwingUtilities.isRightMouseButton(e) || map.draggingPin) {
            return;
        }

        if (map.featureEditMode && SwingUtilities.isLeftMouseButton(e) && map.handleFeatureEditClick(e)) {
            return;
        }

        if (map.isDrawingActive() && SwingUtilities.isLeftMouseButton(e)) {
            Coordinate c = map.resolveInteractiveCoordinate(e.getX(), e.getY(), false);

            if ("POINT".equalsIgnoreCase(map.drawingToolManager.drawingMode) || "MULTIPOINT".equalsIgnoreCase(map.drawingToolManager.drawingMode)) {
                map.drawingToolManager.drawingCoordinates.add(c);
                map.repaint();
                return;
            }

            if ("CIRCLE".equalsIgnoreCase(map.drawingToolManager.drawingMode)) {
                map.appendDrawingCoordinateIfNeeded(c);
                if (map.drawingToolManager.drawingCoordinates.size() >= 2) {
                    map.finishCurrentDrawing();
                } else {
                    map.repaint();
                }
                return;
            }

            if ("CIRCLE_3P".equalsIgnoreCase(map.drawingToolManager.drawingMode)) {
                map.appendDrawingCoordinateIfNeeded(c);
                if (map.drawingToolManager.drawingCoordinates.size() >= 3) {
                    map.finishCurrentDrawing();
                } else {
                    map.repaint();
                }
                return;
            }

            if ("CONTINUE_LINE".equalsIgnoreCase(map.drawingToolManager.drawingMode) && !map.drawingToolManager.drawingContinuationEndpointChosen) {
                map.chooseContinuationEndpoint(e.getX(), e.getY());
                return;
            }

            if ("RECTANGLE".equalsIgnoreCase(map.drawingToolManager.drawingMode)) {
                map.appendDrawingCoordinateIfNeeded(c);
                if (map.drawingToolManager.drawingCoordinates.size() >= 2) {
                    map.finishCurrentDrawing();
                } else {
                    map.repaint();
                }
                return;
            }

            if (e.getClickCount() >= 2) {
                map.appendDrawingCoordinateIfNeeded(c);
                if (!map.drawingToolManager.drawingCoordinates.isEmpty()) {
                    map.finishCurrentDrawing();
                }
                return;
            }

            map.appendDrawingCoordinateIfNeeded(c);
            map.repaint();
            return;
        }

        if (map.isMeasurementActive() && SwingUtilities.isLeftMouseButton(e)) {
            Coordinate c = map.resolveInteractiveCoordinate(e.getX(), e.getY(), false);

            if (e.getClickCount() >= 2) {
                if (!map.measurementTool.getPoints().isEmpty()) {
                    map.finishCurrentMeasurement();
                }
                return;
            }

            map.measurementTool.addPoint(c.x, c.y);
            map.repaint();
            return;
        }

        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
            map.showCoordinateDialog(e.getX(), e.getY());
            return;
        }

        if ("IDENTIFY".equalsIgnoreCase(map.currentTool)) {
            map.identifyFeature(e.getX(), e.getY());
        } else if ("SELECT".equalsIgnoreCase(map.currentTool)) {
            map.selectFeatureForEditing(e.getX(), e.getY(), e.isControlDown());
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        map.handleZoom(e);
    }
}
