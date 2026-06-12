package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import org.locationtech.jts.geom.Coordinate;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Cursor;
import java.awt.event.MouseEvent;

public class CadEngine {

    private final MapPanel panel;

    boolean cadPlacementDragActive = false;
    Layer cadPlacementDragLayer = null;
    MapPanel.CadPlacementDragHandler cadPlacementDragHandler = null;
    boolean cadPlacementDragStarted = false;
    boolean cadPlacementDragMoved = false;
    double cadPlacementDragStartX = Double.NaN;
    double cadPlacementDragStartY = Double.NaN;
    double cadPlacementDragOriginalOffsetX = 0d;
    double cadPlacementDragOriginalOffsetY = 0d;
    String cadPlacementDragStartMessage;
    String cadPlacementDragSuccessMessage;
    String cadPlacementDragCancelMessage;

    boolean pointCaptureActive = false;
    MapPanel.MapPointCaptureHandler pointCaptureHandler = null;
    String pointCaptureStartMessage;
    String pointCaptureSuccessMessage;
    String pointCaptureCancelMessage;

    public boolean isCadPlacementDragActive() { return cadPlacementDragActive; }
    public boolean isPointCaptureActive() { return pointCaptureActive; }

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
        if (panel.isDrawingActive() || panel.isMeasurementActive() || pointCaptureActive || panel.topographicProfileTool.isActive() || cadPlacementDragActive) {
            JOptionPane.showMessageDialog(panel, I18n.t("Termina o cancela la captura, dibujo o medicion actual antes de arrastrar la referencia CAD."));
            return;
        }
        cadPlacementDragLayer = layer;
        cadPlacementDragHandler = handler;
        cadPlacementDragActive = true;
        cadPlacementDragStarted = false;
        cadPlacementDragMoved = false;
        cadPlacementDragStartX = Double.NaN;
        cadPlacementDragStartY = Double.NaN;
        cadPlacementDragOriginalOffsetX = layer.getCadOffsetX();
        cadPlacementDragOriginalOffsetY = layer.getCadOffsetY();
        cadPlacementDragStartMessage = startMessage != null && !startMessage.isBlank()
                ? startMessage
                : I18n.t("Arrastre CAD activo: clic izquierdo y arrastra para mover. Suelta para aplicar. Usa clic derecho o Esc para cancelar.");
        cadPlacementDragSuccessMessage = successMessage != null && !successMessage.isBlank()
                ? successMessage
                : I18n.t("Arrastre CAD aplicado.");
        cadPlacementDragCancelMessage = cancelMessage != null && !cancelMessage.isBlank()
                ? cancelMessage
                : I18n.t("Arrastre CAD cancelado.");
        panel.requestFocusInWindow();
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        AppContext.setStatusMessage(cadPlacementDragStartMessage);
        panel.repaint();
    }

    public void cancelCadPlacementDrag() {
        if (!cadPlacementDragActive) {
            return;
        }
        Layer layer = cadPlacementDragLayer;
        if (layer != null) {
            layer.setCadOffsetX(cadPlacementDragOriginalOffsetX);
            layer.setCadOffsetY(cadPlacementDragOriginalOffsetY);
        }
        MapPanel.CadPlacementDragHandler handler = cadPlacementDragHandler;
        cadPlacementDragActive = false;
        cadPlacementDragStarted = false;
        cadPlacementDragMoved = false;
        cadPlacementDragStartX = Double.NaN;
        cadPlacementDragStartY = Double.NaN;
        cadPlacementDragLayer = null;
        cadPlacementDragHandler = null;
        AppContext.setStatusMessage(cadPlacementDragCancelMessage);
        panel.applyCursorForCurrentMode();
        panel.repaint();
        if (handler != null) {
            handler.onDragCanceled();
        }
    }

    public void beginCadPlacementDrag(MouseEvent e) {
        if (!cadPlacementDragActive || cadPlacementDragLayer == null || !SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        Coordinate coordinate = panel.resolveInteractiveCoordinate(e.getX(), e.getY(), false);
        cadPlacementDragStarted = true;
        cadPlacementDragMoved = false;
        cadPlacementDragStartX = coordinate.x;
        cadPlacementDragStartY = coordinate.y;
        cadPlacementDragOriginalOffsetX = cadPlacementDragLayer.getCadOffsetX();
        cadPlacementDragOriginalOffsetY = cadPlacementDragLayer.getCadOffsetY();
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    public void updateCadPlacementDrag(MouseEvent e) {
        if (!cadPlacementDragActive || !cadPlacementDragStarted || cadPlacementDragLayer == null) {
            return;
        }
        Coordinate coordinate = panel.resolveInteractiveCoordinate(e.getX(), e.getY(), true);
        double dx = coordinate.x - cadPlacementDragStartX;
        double dy = coordinate.y - cadPlacementDragStartY;
        cadPlacementDragLayer.setCadOffsetX(cadPlacementDragOriginalOffsetX + dx);
        cadPlacementDragLayer.setCadOffsetY(cadPlacementDragOriginalOffsetY + dy);
        cadPlacementDragMoved = cadPlacementDragMoved || Math.abs(dx) > 1e-9 || Math.abs(dy) > 1e-9;
        panel.repaint();
    }

    public void finishCadPlacementDrag() {
        if (!cadPlacementDragActive) {
            return;
        }
        if (!cadPlacementDragStarted) {
            return;
        }
        Layer layer = cadPlacementDragLayer;
        MapPanel.CadPlacementDragHandler handler = cadPlacementDragHandler;
        boolean moved = cadPlacementDragMoved;
        double offsetX = layer != null ? layer.getCadOffsetX() : 0d;
        double offsetY = layer != null ? layer.getCadOffsetY() : 0d;

        cadPlacementDragActive = false;
        cadPlacementDragStarted = false;
        cadPlacementDragMoved = false;
        cadPlacementDragStartX = Double.NaN;
        cadPlacementDragStartY = Double.NaN;
        cadPlacementDragLayer = null;
        cadPlacementDragHandler = null;

        AppContext.setStatusMessage(moved ? cadPlacementDragSuccessMessage : cadPlacementDragCancelMessage);
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
        if (panel.isDrawingActive() || panel.isMeasurementActive() || panel.topographicProfileTool.isActive() || cadPlacementDragActive) {
            JOptionPane.showMessageDialog(panel, I18n.t("Termina o cancela la captura, dibujo o medicion actual antes de capturar un punto en el mapa."));
            return;
        }
        pointCaptureHandler = handler;
        pointCaptureActive = true;
        pointCaptureStartMessage = startMessage != null && !startMessage.isBlank()
                ? startMessage
                : I18n.t("Pour point: haz clic sobre el mapa para indicar el outlet. Usa clic derecho o Esc para cancelar.");
        pointCaptureSuccessMessage = successMessage != null && !successMessage.isBlank()
                ? successMessage
                : I18n.t("Pour point capturado.");
        pointCaptureCancelMessage = cancelMessage != null && !cancelMessage.isBlank()
                ? cancelMessage
                : I18n.t("Captura de pour point cancelada.");
        panel.requestFocusInWindow();
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        AppContext.setStatusMessage(pointCaptureStartMessage);
        panel.repaint();
    }

    public void cancelPointCapture() {
        MapPanel.MapPointCaptureHandler handler = pointCaptureHandler;
        pointCaptureActive = false;
        pointCaptureHandler = null;
        AppContext.setStatusMessage(pointCaptureCancelMessage);
        panel.repaint();
        if (handler != null) {
            handler.onCaptureCanceled();
        }
    }

    public void finishPointCapture(Coordinate coordinate) {
        if (!pointCaptureActive || coordinate == null) {
            return;
        }
        MapPanel.MapPointCaptureHandler handler = pointCaptureHandler;
        String projectCrs = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "EPSG:4326";
        pointCaptureActive = false;
        pointCaptureHandler = null;
        panel.repaint();
        AppContext.setStatusMessage(pointCaptureSuccessMessage);
        if (handler != null) {
            handler.onPointCaptured(coordinate, projectCrs);
        }
    }
}
