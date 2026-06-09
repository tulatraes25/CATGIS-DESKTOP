package ar.com.catgis;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import javax.swing.JOptionPane;
import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;

public class TopographicProfileTool {

    private final MapPanel panel;

    public TopographicProfileTool(MapPanel panel) {
        this.panel = panel;
    }

    public boolean isActive() {
        return panel.topographicProfileCaptureActive;
    }

    public void startCapture(MapPanel.TopographicProfileCaptureHandler handler) {
        if (handler == null) {
            return;
        }
        if (panel.isDrawingActive() || panel.isMeasurementActive() || panel.pointCaptureActive || panel.cadPlacementDragActive) {
            JOptionPane.showMessageDialog(panel, I18n.t("Termina o cancela el dibujo/medicion actual antes de capturar un perfil."));
            return;
        }
        panel.topographicProfileCaptureHandler = handler;
        panel.topographicProfileCaptureActive = true;
        panel.topographicProfileCaptureCoordinates.clear();
        panel.requestFocusInWindow();
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(I18n.t("Perfil topografico: haz clics sobre el mapa para dibujar la linea. Usa clic derecho para terminar o Esc para cancelar."));
        }
        panel.repaint();
    }

    public void cancelCapture() {
        MapPanel.TopographicProfileCaptureHandler handler = panel.topographicProfileCaptureHandler;
        panel.topographicProfileCaptureActive = false;
        panel.topographicProfileCaptureCoordinates.clear();
        panel.topographicProfileCaptureHandler = null;
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(I18n.t("Captura de perfil topografico cancelada."));
        }
        panel.repaint();
        if (handler != null) {
            handler.onCaptureCanceled();
        }
    }

    public void finishCapture() {
        if (!panel.topographicProfileCaptureActive || panel.topographicProfileCaptureCoordinates.size() < 2) {
            return;
        }
        LineString line = TopographicProfileService.buildLineFromProjectCoordinates(panel.topographicProfileCaptureCoordinates);
        MapPanel.TopographicProfileCaptureHandler handler = panel.topographicProfileCaptureHandler;
        String projectCrs = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "EPSG:4326";
        panel.topographicProfileCaptureActive = false;
        panel.topographicProfileCaptureCoordinates.clear();
        panel.topographicProfileCaptureHandler = null;
        panel.repaint();
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(I18n.t("Linea de perfil capturada."));
        }
        if (handler != null && line != null) {
            handler.onLineCaptured(line, projectCrs);
        }
    }
}
