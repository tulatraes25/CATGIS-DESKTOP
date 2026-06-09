package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import org.locationtech.jts.geom.Coordinate;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Cursor;
import java.awt.event.MouseEvent;

public class CadEngine {

    private final MapPanel panel;

    public CadEngine(MapPanel panel) {
        this.panel = panel;
    }

    public void clearCadConstructionState() {
        panel.cadReferenceSegmentStart = null;
        panel.cadReferenceSegmentEnd = null;
        panel.cadReferenceFromStart = false;
        panel.cadReferenceEndpointChosen = false;
    }

    public void startCadPlacementDrag(Layer layer,
                                      MapPanel.CadPlacementDragHandler handler,
                                      String startMessage,
                                      String successMessage,
                                      String cancelMessage) {
        if (layer == null || !CadLayerSupport.isCadLayer(layer) || handler == null) {
            return;
        }
        if (panel.isDrawingActive() || panel.isMeasurementActive() || panel.pointCaptureActive || panel.topographicProfileCaptureActive || panel.cadPlacementDragActive) {
            JOptionPane.showMessageDialog(panel, I18n.t("Termina o cancela la captura, dibujo o medicion actual antes de arrastrar la referencia CAD."));
            return;
        }
        panel.cadPlacementDragLayer = layer;
        panel.cadPlacementDragHandler = handler;
        panel.cadPlacementDragActive = true;
        panel.cadPlacementDragStarted = false;
        panel.cadPlacementDragMoved = false;
        panel.cadPlacementDragStartX = Double.NaN;
        panel.cadPlacementDragStartY = Double.NaN;
        panel.cadPlacementDragOriginalOffsetX = layer.getCadOffsetX();
        panel.cadPlacementDragOriginalOffsetY = layer.getCadOffsetY();
        panel.cadPlacementDragStartMessage = startMessage != null && !startMessage.isBlank()
                ? startMessage
                : I18n.t("Arrastre CAD activo: clic izquierdo y arrastra para mover. Suelta para aplicar. Usa clic derecho o Esc para cancelar.");
        panel.cadPlacementDragSuccessMessage = successMessage != null && !successMessage.isBlank()
                ? successMessage
                : I18n.t("Arrastre CAD aplicado.");
        panel.cadPlacementDragCancelMessage = cancelMessage != null && !cancelMessage.isBlank()
                ? cancelMessage
                : I18n.t("Arrastre CAD cancelado.");
        panel.requestFocusInWindow();
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(panel.cadPlacementDragStartMessage);
        }
        panel.repaint();
    }

    public boolean isCadPlacementDragActive() {
        return panel.cadPlacementDragActive;
    }

    public void cancelCadPlacementDrag() {
        if (!panel.cadPlacementDragActive) {
            return;
        }
        Layer layer = panel.cadPlacementDragLayer;
        if (layer != null) {
            layer.setCadOffsetX(panel.cadPlacementDragOriginalOffsetX);
            layer.setCadOffsetY(panel.cadPlacementDragOriginalOffsetY);
        }
        MapPanel.CadPlacementDragHandler handler = panel.cadPlacementDragHandler;
        panel.cadPlacementDragActive = false;
        panel.cadPlacementDragStarted = false;
        panel.cadPlacementDragMoved = false;
        panel.cadPlacementDragStartX = Double.NaN;
        panel.cadPlacementDragStartY = Double.NaN;
        panel.cadPlacementDragLayer = null;
        panel.cadPlacementDragHandler = null;
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(panel.cadPlacementDragCancelMessage);
        }
        panel.applyCursorForCurrentMode();
        panel.repaint();
        if (handler != null) {
            handler.onDragCanceled();
        }
    }

    public void beginCadPlacementDrag(MouseEvent e) {
        if (!panel.cadPlacementDragActive || panel.cadPlacementDragLayer == null || !SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        Coordinate coordinate = panel.resolveInteractiveCoordinate(e.getX(), e.getY(), false);
        panel.cadPlacementDragStarted = true;
        panel.cadPlacementDragMoved = false;
        panel.cadPlacementDragStartX = coordinate.x;
        panel.cadPlacementDragStartY = coordinate.y;
        panel.cadPlacementDragOriginalOffsetX = panel.cadPlacementDragLayer.getCadOffsetX();
        panel.cadPlacementDragOriginalOffsetY = panel.cadPlacementDragLayer.getCadOffsetY();
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    public void updateCadPlacementDrag(MouseEvent e) {
        if (!panel.cadPlacementDragActive || !panel.cadPlacementDragStarted || panel.cadPlacementDragLayer == null) {
            return;
        }
        Coordinate coordinate = panel.resolveInteractiveCoordinate(e.getX(), e.getY(), true);
        double dx = coordinate.x - panel.cadPlacementDragStartX;
        double dy = coordinate.y - panel.cadPlacementDragStartY;
        panel.cadPlacementDragLayer.setCadOffsetX(panel.cadPlacementDragOriginalOffsetX + dx);
        panel.cadPlacementDragLayer.setCadOffsetY(panel.cadPlacementDragOriginalOffsetY + dy);
        panel.cadPlacementDragMoved = panel.cadPlacementDragMoved || Math.abs(dx) > 1e-9 || Math.abs(dy) > 1e-9;
        panel.repaint();
    }

    public void finishCadPlacementDrag() {
        if (!panel.cadPlacementDragActive) {
            return;
        }
        if (!panel.cadPlacementDragStarted) {
            return;
        }
        Layer layer = panel.cadPlacementDragLayer;
        MapPanel.CadPlacementDragHandler handler = panel.cadPlacementDragHandler;
        boolean moved = panel.cadPlacementDragMoved;
        double offsetX = layer != null ? layer.getCadOffsetX() : 0d;
        double offsetY = layer != null ? layer.getCadOffsetY() : 0d;

        panel.cadPlacementDragActive = false;
        panel.cadPlacementDragStarted = false;
        panel.cadPlacementDragMoved = false;
        panel.cadPlacementDragStartX = Double.NaN;
        panel.cadPlacementDragStartY = Double.NaN;
        panel.cadPlacementDragLayer = null;
        panel.cadPlacementDragHandler = null;

        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(moved ? panel.cadPlacementDragSuccessMessage : panel.cadPlacementDragCancelMessage);
        }
        panel.applyCursorForCurrentMode();
        panel.repaint();
        if (handler != null) {
            if (moved) {
                handler.onDragApplied(offsetX, offsetY);
            } else {
                handler.onDragCanceled();
            }
        }
    }

    public void startPointCapture(MapPanel.MapPointCaptureHandler handler) {
        startPointCapture(
                handler,
                I18n.t("Pour point: haz clic sobre el mapa para indicar el outlet. Usa clic derecho o Esc para cancelar."),
                I18n.t("Pour point capturado."),
                I18n.t("Captura de pour point cancelada.")
        );
    }

    public void startPointCapture(MapPanel.MapPointCaptureHandler handler,
                                  String startMessage,
                                  String successMessage,
                                  String cancelMessage) {
        if (handler == null) {
            return;
        }
        if (panel.isDrawingActive() || panel.isMeasurementActive() || panel.topographicProfileCaptureActive || panel.cadPlacementDragActive) {
            JOptionPane.showMessageDialog(panel, I18n.t("Termina o cancela la captura, dibujo o medicion actual antes de capturar un punto en el mapa."));
            return;
        }
        panel.pointCaptureHandler = handler;
        panel.pointCaptureActive = true;
        panel.pointCaptureStartMessage = startMessage != null && !startMessage.isBlank()
                ? startMessage
                : I18n.t("Pour point: haz clic sobre el mapa para indicar el outlet. Usa clic derecho o Esc para cancelar.");
        panel.pointCaptureSuccessMessage = successMessage != null && !successMessage.isBlank()
                ? successMessage
                : I18n.t("Pour point capturado.");
        panel.pointCaptureCancelMessage = cancelMessage != null && !cancelMessage.isBlank()
                ? cancelMessage
                : I18n.t("Captura de pour point cancelada.");
        panel.requestFocusInWindow();
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(panel.pointCaptureStartMessage);
        }
        panel.repaint();
    }

    public void cancelPointCapture() {
        MapPanel.MapPointCaptureHandler handler = panel.pointCaptureHandler;
        panel.pointCaptureActive = false;
        panel.pointCaptureHandler = null;
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(panel.pointCaptureCancelMessage);
        }
        panel.repaint();
        if (handler != null) {
            handler.onCaptureCanceled();
        }
    }

    public boolean isPointCaptureActive() {
        return panel.pointCaptureActive;
    }

    public void finishPointCapture(Coordinate coordinate) {
        if (!panel.pointCaptureActive || coordinate == null) {
            return;
        }
        MapPanel.MapPointCaptureHandler handler = panel.pointCaptureHandler;
        String projectCrs = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "EPSG:4326";
        panel.pointCaptureActive = false;
        panel.pointCaptureHandler = null;
        panel.repaint();
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(panel.pointCaptureSuccessMessage);
        }
        if (handler != null) {
            handler.onPointCaptured(coordinate, projectCrs);
        }
    }
}
