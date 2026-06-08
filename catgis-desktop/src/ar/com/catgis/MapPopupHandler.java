package ar.com.catgis;

import ar.com.catgis.core.model.Layer;

import ar.com.catgis.service.EventBus;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Handles popup menus for MapPanel.
 * Extracted from MapPanel to reduce its scope.
 */
public class MapPopupHandler {

    private final MapPanel panel;

    public MapPopupHandler(MapPanel panel) {
        this.panel = panel;
    }

    public void showMapPopup(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (panel.isDrawingActive()) {
            JPopupMenu popupMenu = new JPopupMenu();

            if (!panel.drawingCoordinates.isEmpty()) {
                JMenuItem finishEntityItem = new JMenuItem("Cerrar entidad actual");
                finishEntityItem.addActionListener(ev -> panel.finishCurrentDrawing());
                popupMenu.add(finishEntityItem);
            }

            JMenuItem closeItem = new JMenuItem("Cerrar dibujo...");
            closeItem.addActionListener(ev -> panel.closeCurrentDrawingSession());
            popupMenu.add(closeItem);

            JMenuItem cancelItem = new JMenuItem("Cancelar dibujo");
            cancelItem.addActionListener(ev -> panel.cancelCurrentDrawing());
            popupMenu.add(cancelItem);

            popupMenu.show(panel, x, y);
            return;
        }

        if (panel.isMeasurementActive()) {
            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem finishItem = new JMenuItem("Terminar medici\u00f3n");
            finishItem.addActionListener(ev -> panel.finishCurrentMeasurement());
            popupMenu.add(finishItem);

            JMenuItem cancelItem = new JMenuItem("Cancelar medici\u00f3n");
            cancelItem.addActionListener(ev -> panel.cancelCurrentMeasurement());
            popupMenu.add(cancelItem);

            popupMenu.show(panel, x, y);
            return;
        }

        if (panel.featureEditMode && panel.isFeatureEditSketchMode() && !panel.featureEditSketchCoordinates.isEmpty()) {
            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem finishItem = new JMenuItem("Aplicar edici\u00f3n");
            finishItem.addActionListener(ev -> panel.applyFeatureEditSketchOperationEnhanced());
            popupMenu.add(finishItem);

            JMenuItem cancelItem = new JMenuItem("Cancelar boceto");
            cancelItem.addActionListener(ev -> {
                panel.featureEditSketchCoordinates.clear();
                panel.repaint();
                panel.showCopiedMessage("Boceto de edici\u00f3n cancelado.");
            });
            popupMenu.add(cancelItem);

            popupMenu.show(panel, x, y);
            return;
        }

        PinMarker clickedPin = panel.findPinAtScreen(x, y);
        if (clickedPin != null) {
            showPinPopup(e, clickedPin);
            return;
        }

        List<IdentifyResultItem> vectorHits = panel.collectIdentifyResults(x, y);
        if (!vectorHits.isEmpty()) {
            IdentifyResultItem hit = vectorHits.get(0);
            panel.highlightIdentifiedFeature(hit.getLayer(), hit.getFeature());
            showFeaturePopup(e, hit);
            return;
        }

        double projectX = panel.screenToWorldX(x);
        double projectY = panel.screenToWorldY(y);
        String projectCRS = (CatgisDesktopApp.currentProject != null) ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        double[] geographic = panel.transformPoint(projectX, projectY, projectCRS, "EPSG:4326");

        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem addPinItem = new JMenuItem("Agregar pin aqu\u00ed");
        addPinItem.addActionListener(ev -> {
            PinMarker pin = panel.addPin(projectX, projectY);
            panel.showCopiedMessage("Pin P" + pin.getId() + " agregado.");
        });
        popupMenu.add(addPinItem);

        JMenuItem copyProjectItem = new JMenuItem("Copiar coordenada proyecto (X/Y)");
        copyProjectItem.addActionListener(ev -> {
            String text = panel.formatNumber(projectX) + ", " + panel.formatNumber(projectY);
            panel.copyToClipboard(text);
            panel.showCopiedMessage("Coordenada del proyecto copiada.");
        });
        popupMenu.add(copyProjectItem);

        JMenuItem copyLatLonItem = new JMenuItem("Copiar Lat/Long");
        copyLatLonItem.addActionListener(ev -> {
            if (geographic == null) {
                JOptionPane.showMessageDialog(panel, "No se pudo transformar a EPSG:4326.");
                return;
            }
            String text = panel.formatNumber(geographic[0]) + ", " + panel.formatNumber(geographic[1]);
            panel.copyToClipboard(text);
            panel.showCopiedMessage("Lat/Long copiada.");
        });
        popupMenu.add(copyLatLonItem);

        JMenuItem copyDmsItem = new JMenuItem("Copiar Lat/Long DMS");
        copyDmsItem.addActionListener(ev -> {
            if (geographic == null) {
                JOptionPane.showMessageDialog(panel, "No se pudo transformar a EPSG:4326.");
                return;
            }
            String text = panel.toDms(geographic[0], false) + " , " + panel.toDms(geographic[1], true);
            panel.copyToClipboard(text);
            panel.showCopiedMessage("Lat/Long DMS copiada.");
        });
        popupMenu.add(copyDmsItem);

        JMenuItem showItem = new JMenuItem("Ver coordenadas del punto");
        showItem.addActionListener(ev -> panel.showCoordinateDialog(x, y));
        popupMenu.add(showItem);

        if (!panel.pins.isEmpty()) {
            popupMenu.addSeparator();

            JMenuItem convertPinsItem = new JMenuItem("Convertir pines en capa");
            convertPinsItem.addActionListener(ev -> panel.convertPinsToLayer());
            popupMenu.add(convertPinsItem);

            JMenuItem clearPinsItem = new JMenuItem("Borrar todos los pines");
            clearPinsItem.addActionListener(ev -> {
                panel.clearAllPins();
                panel.showCopiedMessage("Todos los pines fueron eliminados.");
            });
            popupMenu.add(clearPinsItem);
        }

        popupMenu.show(panel, x, y);
    }

    public void showFeaturePopup(MouseEvent e, IdentifyResultItem hit) {
        if (hit == null || hit.getLayer() == null || hit.getFeature() == null) {
            return;
        }

        Layer layer = hit.getLayer();
        SimpleFeature feature = hit.getFeature();

        JPopupMenu popupMenu = new JPopupMenu();
        boolean readOnlyLayer = panel.isReadOnlyVectorLayer(layer);

        JMenuItem editItem = new JMenuItem(readOnlyLayer ? "Capa en solo lectura" : "Editar vector");
        editItem.setEnabled(!readOnlyLayer);
        editItem.addActionListener(ev -> {
            panel.enableFeatureEdit(layer, feature);
            EventBus.emit(EventBus.EventType.STATUS_MESSAGE, "Edicion vectorial lista para: " + layer.getName());
            EventBus.emit(EventBus.EventType.TOOLBAR_SYNC);
        });
        popupMenu.add(editItem);

        Object geometry = feature.getDefaultGeometry();
        boolean linearOrPolygonal = geometry instanceof LineString
                || geometry instanceof MultiLineString
                || geometry instanceof Polygon
                || geometry instanceof MultiPolygon;

        if (linearOrPolygonal && !readOnlyLayer) {
            JMenuItem moveVertexItem = new JMenuItem("Mover vertices");
            moveVertexItem.addActionListener(ev -> {
                panel.enableFeatureEdit(layer, feature);
                panel.activateMoveVertexMode();
            });
            popupMenu.add(moveVertexItem);

            JMenuItem addVertexItem = new JMenuItem("Agregar vertice");
            addVertexItem.addActionListener(ev -> {
                panel.enableFeatureEdit(layer, feature);
                panel.activateAddVertexMode();
            });
            popupMenu.add(addVertexItem);

            JMenuItem removeVertexItem = new JMenuItem("Eliminar vertice");
            removeVertexItem.addActionListener(ev -> {
                panel.enableFeatureEdit(layer, feature);
                panel.activateRemoveVertexMode();
            });
            popupMenu.add(removeVertexItem);

            JMenuItem cutItem = new JMenuItem("Cortar geometria");
            cutItem.addActionListener(ev -> {
                panel.enableFeatureEdit(layer, feature);
                panel.activateCutFeatureMode();
            });
            popupMenu.add(cutItem);
        }

        if (!readOnlyLayer && (geometry instanceof Polygon || geometry instanceof MultiPolygon)) {
            JMenuItem holeItem = new JMenuItem("Crear agujero");
            holeItem.addActionListener(ev -> {
                panel.enableFeatureEdit(layer, feature);
                panel.activateHoleMode();
            });
            popupMenu.add(holeItem);
        }

        popupMenu.addSeparator();

        JMenuItem infoItem = new JMenuItem("Ver informacion");
        infoItem.addActionListener(ev -> panel.showFeatureInfo(feature, layer));
        popupMenu.add(infoItem);

        JMenuItem zoomItem = new JMenuItem("Zoom a entidad");
        zoomItem.addActionListener(ev -> panel.zoomToFeature(feature, layer));
        popupMenu.add(zoomItem);

        JMenuItem attrItem = new JMenuItem(readOnlyLayer ? "Ver atributos" : "Editar atributos");
        attrItem.addActionListener(ev -> OpenAttributeTableAction.openTable(layer));
        popupMenu.add(attrItem);

        JMenuItem propertiesItem = new JMenuItem("Opciones de capa");
        propertiesItem.addActionListener(ev -> LayerPropertiesDialog.open(layer));
        popupMenu.add(propertiesItem);

        JMenuItem clearItem = new JMenuItem("Limpiar seleccion");
        clearItem.addActionListener(ev -> panel.clearSelectedFeature());
        popupMenu.add(clearItem);

        popupMenu.show(panel, e.getX(), e.getY());
    }

    public void showPinPopup(MouseEvent e, PinMarker pin) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem showPinItem = new JMenuItem("Ver coordenadas de P" + pin.getId());
        showPinItem.addActionListener(ev -> panel.showPinDialog(pin));
        popupMenu.add(showPinItem);

        JMenuItem copyPinProjectItem = new JMenuItem("Copiar coordenada de P" + pin.getId() + " (X/Y)");
        copyPinProjectItem.addActionListener(ev -> {
            String text = panel.formatNumber(pin.getX()) + ", " + panel.formatNumber(pin.getY());
            panel.copyToClipboard(text);
            panel.showCopiedMessage("Coordenada de P" + pin.getId() + " copiada.");
        });
        popupMenu.add(copyPinProjectItem);

        JMenuItem copyPinLatLonItem = new JMenuItem("Copiar Lat/Long de P" + pin.getId());
        copyPinLatLonItem.addActionListener(ev -> panel.copyPinLatLon(pin, false));
        popupMenu.add(copyPinLatLonItem);

        JMenuItem copyPinDmsItem = new JMenuItem("Copiar Lat/Long DMS de P" + pin.getId());
        copyPinDmsItem.addActionListener(ev -> panel.copyPinLatLon(pin, true));
        popupMenu.add(copyPinDmsItem);

        JMenuItem removePinItem = new JMenuItem("Borrar P" + pin.getId());
        removePinItem.addActionListener(ev -> {
            panel.removePin(pin);
            panel.showCopiedMessage("Pin P" + pin.getId() + " eliminado.");
        });
        popupMenu.add(removePinItem);

        if (!panel.pins.isEmpty()) {
            popupMenu.addSeparator();

            JMenuItem convertPinsItem = new JMenuItem("Convertir pines en capa");
            convertPinsItem.addActionListener(ev -> panel.convertPinsToLayer());
            popupMenu.add(convertPinsItem);

            JMenuItem clearPinsItem = new JMenuItem("Borrar todos los pines");
            clearPinsItem.addActionListener(ev -> {
                panel.clearAllPins();
                panel.showCopiedMessage("Todos los pines fueron eliminados.");
            });
            popupMenu.add(clearPinsItem);
        }

        popupMenu.show(panel, e.getX(), e.getY());
    }
}
