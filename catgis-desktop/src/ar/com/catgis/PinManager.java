package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.service.EventBus;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

public class PinManager {
    private final MapPanel panel;

    public PinManager(MapPanel panel) {
        this.panel = panel;
    }

    public PinMarker addPin(double x, double y) {
        PinMarker pin = new PinMarker(panel.nextPinId++, x, y);
        panel.pins.add(pin);
        panel.activePin = pin;
        panel.repaint();
        return pin;
    }

    public void removePin(PinMarker pin) {
        panel.pins.remove(pin);
        if (panel.activePin == pin) {
            panel.activePin = null;
        }
        panel.repaint();
    }

    public void clearAllPins() {
        panel.pins.clear();
        panel.activePin = null;
        panel.repaint();
    }

    public void convertPinsToLayer() {
        if (panel.pins.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "No hay pines para convertir.");
            return;
        }

        try {
            String projectCRS = (CatgisDesktopApp.currentProject != null &&
                    CatgisDesktopApp.currentProject.getProjectCRS() != null &&
                    !CatgisDesktopApp.currentProject.getProjectCRS().isBlank())
                    ? CatgisDesktopApp.currentProject.getProjectCRS()
                    : "EPSG:4326";

            ShapefileData data = PinLayerBuilder.buildFromPins(panel.pins, projectCRS);

            String layerName = "Pines_" + System.currentTimeMillis();
            Layer layer = new Layer(layerName, "", "VECTOR");
            layer.setVisible(true);
            layer.setSourceName(data.getSourceName());
            layer.setFeatureCount(data.getFeatureCount());
            layer.setSourceCRS(projectCRS);
            layer.setLabelsVisible(true);
            layer.setLabelField("id");

            File exportedFile = ExportVectorLayerAction.exportLayerWithDialog(
                    layer,
                    data,
                    panel,
                    "Guardar capa de pines",
                    false
            );
            if (exportedFile == null) {
                panel.showCopiedMessage("Los pines siguen disponibles hasta que guardes la nueva capa o canceles.");
                return;
            }

            if (CatgisDesktopApp.currentProject == null) {
                CatgisDesktopApp.currentProject = new Project("Proyecto actual");
            }

            CatgisDesktopApp.currentProject.addLayer(layer);
            CatgisDesktopApp.markProjectDirty();
            CatgisDesktopApp.layersPanel.addLayer(layer);
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.showOpenedFile(layer.getName());
                CatgisDesktopApp.mapPanel.repaint();
            }

            JOptionPane.showMessageDialog(panel, "Pines convertidos y guardados correctamente:\n" + exportedFile.getAbsolutePath());
        } catch (Exception ex) {
            AppErrorSupport.logFailure("Error al convertir pines en capa", ex);
            AppErrorSupport.showErrorDialog(panel, "Pines", "Error al convertir pines en capa.", ex);
        }
    }

    public PinMarker findPinAtScreen(int mouseX, int mouseY) {
        for (int i = panel.pins.size() - 1; i >= 0; i--) {
            PinMarker pin = panel.pins.get(i);
            int pinScreenX = panel.worldToScreenX(pin.getX());
            int pinScreenY = panel.worldToScreenY(pin.getY());

            int dx = mouseX - pinScreenX;
            int dy = mouseY - pinScreenY;

            int tolerance = 10;
            if ((dx * dx + dy * dy) <= (tolerance * tolerance)) {
                return pin;
            }
        }
        return null;
    }

    public void showPinDialog(PinMarker pin) {
        if (pin == null) {
            JOptionPane.showMessageDialog(panel, "No hay pin seleccionado.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Coordenadas del pin P").append(pin.getId()).append("\n\n");

        String projectCRS = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        if (projectCRS != null && !projectCRS.isBlank()) {
            sb.append("CRS proyecto: ").append(projectCRS).append("\n");
        }

        sb.append("X: ").append(panel.formatNumber(pin.getX())).append("\n");
        sb.append("Y: ").append(panel.formatNumber(pin.getY())).append("\n");

        double[] geographic = panel.transformPoint(pin.getX(), pin.getY(), projectCRS, "EPSG:4326");
        if (geographic != null) {
            sb.append("\nEPSG:4326\n");
            sb.append("Lon: ").append(panel.formatNumber(geographic[0])).append("\n");
            sb.append("Lat: ").append(panel.formatNumber(geographic[1])).append("\n");
            sb.append("Lon DMS: ").append(panel.toDms(geographic[0], false)).append("\n");
            sb.append("Lat DMS: ").append(panel.toDms(geographic[1], true)).append("\n");
        }

        JOptionPane.showMessageDialog(panel, sb.toString(), "Pin P" + pin.getId(), JOptionPane.INFORMATION_MESSAGE);
    }

    public void copyPinLatLon(PinMarker pin, boolean dms) {
        if (pin == null) {
            JOptionPane.showMessageDialog(panel, "No hay pin seleccionado.");
            return;
        }

        String projectCRS = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        double[] geographic = panel.transformPoint(pin.getX(), pin.getY(), projectCRS, "EPSG:4326");

        if (geographic == null) {
            JOptionPane.showMessageDialog(panel, "No se pudo transformar el pin a EPSG:4326.");
            return;
        }

        String text;
        if (dms) {
            text = panel.toDms(geographic[0], false) + " , " + panel.toDms(geographic[1], true);
            panel.showCopiedMessage("Lat/Long DMS de P" + pin.getId() + " copiada.");
        } else {
            text = panel.formatNumber(geographic[0]) + ", " + panel.formatNumber(geographic[1]);
            panel.showCopiedMessage("Lat/Long de P" + pin.getId() + " copiada.");
        }

        panel.copyToClipboard(text);
    }

    public void showPinPopup(MouseEvent e, PinMarker pin) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem showPinItem = new JMenuItem("Ver coordenadas de P" + pin.getId());
        showPinItem.addActionListener(ev -> showPinDialog(pin));
        popupMenu.add(showPinItem);

        JMenuItem copyPinProjectItem = new JMenuItem("Copiar coordenada de P" + pin.getId() + " (X/Y)");
        copyPinProjectItem.addActionListener(ev -> {
            String text = panel.formatNumber(pin.getX()) + ", " + panel.formatNumber(pin.getY());
            panel.copyToClipboard(text);
            panel.showCopiedMessage("Coordenada de P" + pin.getId() + " copiada.");
        });
        popupMenu.add(copyPinProjectItem);

        JMenuItem copyPinLatLonItem = new JMenuItem("Copiar Lat/Long de P" + pin.getId());
        copyPinLatLonItem.addActionListener(ev -> copyPinLatLon(pin, false));
        popupMenu.add(copyPinLatLonItem);

        JMenuItem copyPinDmsItem = new JMenuItem("Copiar Lat/Long DMS de P" + pin.getId());
        copyPinDmsItem.addActionListener(ev -> copyPinLatLon(pin, true));
        popupMenu.add(copyPinDmsItem);

        JMenuItem removePinItem = new JMenuItem("Borrar P" + pin.getId());
        removePinItem.addActionListener(ev -> {
            removePin(pin);
            panel.showCopiedMessage("Pin P" + pin.getId() + " eliminado.");
        });
        popupMenu.add(removePinItem);

        if (!panel.pins.isEmpty()) {
            popupMenu.addSeparator();

            JMenuItem convertPinsItem = new JMenuItem("Convertir pines en capa");
            convertPinsItem.addActionListener(ev -> convertPinsToLayer());
            popupMenu.add(convertPinsItem);

            JMenuItem clearPinsItem = new JMenuItem("Borrar todos los pines");
            clearPinsItem.addActionListener(ev -> {
                clearAllPins();
                panel.showCopiedMessage("Todos los pines fueron eliminados.");
            });
            popupMenu.add(clearPinsItem);
        }

        popupMenu.show(panel, e.getX(), e.getY());
    }

    public void deletePin(PinMarker pin) {
        removePin(pin);
    }
}
