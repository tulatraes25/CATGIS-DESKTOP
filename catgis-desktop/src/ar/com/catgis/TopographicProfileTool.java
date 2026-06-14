package ar.com.catgis;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import javax.swing.JOptionPane;
import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;

public class TopographicProfileTool {

    private final MapPanel panel;

    boolean active = false;
    final List<Coordinate> coordinates = new ArrayList<>();
    TopographicProfileCaptureHandler handler = null;

    public boolean isActive() { return active; }
    public List<Coordinate> getCoordinates() { return coordinates; }

    public TopographicProfileTool(MapPanel panel) {
        this.panel = panel;
    }

    public void startCapture(TopographicProfileCaptureHandler handler) {
        if (handler == null) {
            return;
        }
        if (panel.isDrawingActive() || panel.isMeasurementActive() || panel.cadEngine.pointCaptureActive || panel.cadEngine.cadPlacementDragActive) {
            NotificationManager.warn(panel, null, I18n.t("Termina o cancela el dibujo/medicion actual antes de capturar un perfil."));
            return;
        }
        this.handler = handler;
        active = true;
        coordinates.clear();
        panel.requestFocusInWindow();
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(I18n.t("Perfil topografico: haz clics sobre el mapa para dibujar la linea. Usa clic derecho para terminar o Esc para cancelar."));
        }
        panel.repaint();
    }

    public void cancelCapture() {
        TopographicProfileCaptureHandler h = this.handler;
        active = false;
        coordinates.clear();
        this.handler = null;
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(I18n.t("Captura de perfil topografico cancelada."));
        }
        panel.repaint();
        if (h != null) {
            h.onCaptureCanceled();
        }
    }

    public void finishCapture() {
        if (!active || coordinates.size() < 2) {
            return;
        }
        LineString line = TopographicProfileService.buildLineFromProjectCoordinates(coordinates);
        TopographicProfileCaptureHandler h = this.handler;
        String projectCrs = AppContext.project() != null ? AppContext.project().getProjectCRS() : "EPSG:4326";
        active = false;
        coordinates.clear();
        this.handler = null;
        panel.repaint();
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(I18n.t("Linea de perfil capturada."));
        }
        if (h != null && line != null) {
            h.onLineCaptured(line, projectCrs);
        }
    }
}

