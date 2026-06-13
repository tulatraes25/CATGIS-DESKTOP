package ar.com.catgis;

import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Extracts popup menu construction from MapPanel.
 * Takes a {@link PopupContext} (implemented by MapPanel) and a parent
 * {@link JComponent} for positioning dialogs and popups.
 */
class MapPopupMenuBuilder {

    /**
     * Methods MapPanel exposes that popup menus need.
     * MapPanel already has every one of these; it just needs to
     * {@code implements PopupContext}.
     */
    interface PopupContext {

        // ---- drawing state ----
        boolean isDrawingActive();
        List<Coordinate> getDrawingCoordinates();
        void finishCurrentDrawing();
        void closeCurrentDrawingSession();
        void cancelCurrentDrawing();

        // ---- measurement state ----
        boolean isMeasurementActive();
        void finishCurrentMeasurement();
        void cancelCurrentMeasurement();

        // ---- feature-edit state ----
        boolean isFeatureEditMode();
        boolean isFeatureEditSketchMode();
        List<Coordinate> getFeatureEditSketchCoordinates();
        void applyFeatureEditSketchOperationEnhanced();

        // ---- display helpers ----
        void repaint();
        void showCopiedMessage(String message);
        void showCoordinateDialog(int screenX, int screenY);

        // ---- pins ----
        PinMarker findPinAtScreen(int mouseX, int mouseY);
        PinMarker addPin(double x, double y);
        boolean hasPins();
        void clearAllPins();
        void convertPinsToLayer();
        void showPinPopup(MouseEvent e, PinMarker pin);

        // ---- identify / hit-test ----
        List<IdentifyResultItem> collectIdentifyResults(int screenX, int screenY);
        void highlightIdentifiedFeature(Layer layer, SimpleFeature feature);

        // ---- coordinate transforms ----
        double screenToWorldX(int screenX);
        double screenToWorldY(int screenY);
        double[] transformPoint(double x, double y, String sourceCode, String targetCode);

        // ---- layer / feature operations ----
        boolean isReadOnlyVectorLayer(Layer layer);
        void enableFeatureEdit(Layer layer, SimpleFeature feature);
        void activateMoveVertexMode();
        void activateAddVertexMode();
        void activateRemoveVertexMode();
        void activateCutFeatureMode();
        void activateHoleMode();
        void showFeatureInfo(SimpleFeature feature, Layer layer);
        void zoomToFeature(SimpleFeature feature, Layer layer);
        void clearSelectedFeature();
    }

    private final PopupContext ctx;
    private final JComponent parent;

    MapPopupMenuBuilder(PopupContext ctx, JComponent parent) {
        this.ctx = ctx;
        this.parent = parent;
    }

    // -----------------------------------------------------------------
    //  Popup entry points  (moved from MapPanel)
    // -----------------------------------------------------------------

    void showMapPopup(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (ctx.isDrawingActive()) {
            JPopupMenu popupMenu = new JPopupMenu();

            if (!ctx.getDrawingCoordinates().isEmpty()) {
                JMenuItem finishEntityItem = new JMenuItem("Cerrar entidad actual");
                finishEntityItem.addActionListener(ev -> ctx.finishCurrentDrawing());
                popupMenu.add(finishEntityItem);
            }

            JMenuItem closeItem = new JMenuItem("Cerrar dibujo...");
            closeItem.addActionListener(ev -> ctx.closeCurrentDrawingSession());
            popupMenu.add(closeItem);

            JMenuItem cancelItem = new JMenuItem("Cancelar dibujo");
            cancelItem.addActionListener(ev -> ctx.cancelCurrentDrawing());
            popupMenu.add(cancelItem);

            popupMenu.show(parent, x, y);
            return;
        }

        if (ctx.isMeasurementActive()) {
            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem finishItem = new JMenuItem("Terminar medici\u00f3n");
            finishItem.addActionListener(ev -> ctx.finishCurrentMeasurement());
            popupMenu.add(finishItem);

            JMenuItem cancelItem = new JMenuItem("Cancelar medici\u00f3n");
            cancelItem.addActionListener(ev -> ctx.cancelCurrentMeasurement());
            popupMenu.add(cancelItem);

            popupMenu.show(parent, x, y);
            return;
        }

        if (ctx.isFeatureEditMode() && ctx.isFeatureEditSketchMode()
                && !ctx.getFeatureEditSketchCoordinates().isEmpty()) {
            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem finishItem = new JMenuItem("Aplicar edici\u00f3n");
            finishItem.addActionListener(ev -> ctx.applyFeatureEditSketchOperationEnhanced());
            popupMenu.add(finishItem);

            JMenuItem cancelItem = new JMenuItem("Cancelar boceto");
            cancelItem.addActionListener(ev -> {
                ctx.getFeatureEditSketchCoordinates().clear();
                ctx.repaint();
                ctx.showCopiedMessage("Boceto de edici\u00f3n cancelado.");
            });
            popupMenu.add(cancelItem);

            popupMenu.show(parent, x, y);
            return;
        }

        PinMarker clickedPin = ctx.findPinAtScreen(x, y);
        if (clickedPin != null) {
            showPinPopup(e, clickedPin);
            return;
        }

        List<IdentifyResultItem> vectorHits = ctx.collectIdentifyResults(x, y);
        if (!vectorHits.isEmpty()) {
            IdentifyResultItem hit = vectorHits.get(0);
            ctx.highlightIdentifiedFeature(hit.getLayer(), hit.getFeature());
            showFeaturePopup(e, hit);
            return;
        }

        double projectX = ctx.screenToWorldX(x);
        double projectY = ctx.screenToWorldY(y);
        String projectCRS = (AppContext.project() != null) ? AppContext.project().getProjectCRS() : "";
        double[] geographic = ctx.transformPoint(projectX, projectY, projectCRS, "EPSG:4326");

        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem addPinItem = new JMenuItem("Agregar pin aqu\u00ed");
        addPinItem.addActionListener(ev -> {
            PinMarker pin = ctx.addPin(projectX, projectY);
            ctx.showCopiedMessage("Pin P" + pin.getId() + " agregado.");
        });
        popupMenu.add(addPinItem);

        JMenuItem copyProjectItem = new JMenuItem("Copiar coordenada proyecto (X/Y)");
        copyProjectItem.addActionListener(ev -> {
            String text = MapUtilities.formatNumber(projectX) + ", " + MapUtilities.formatNumber(projectY);
            MapUtilities.copyToClipboard(text);
            ctx.showCopiedMessage("Coordenada del proyecto copiada.");
        });
        popupMenu.add(copyProjectItem);

        JMenuItem copyLatLonItem = new JMenuItem("Copiar Lat/Long");
        copyLatLonItem.addActionListener(ev -> {
            if (geographic == null) {
                JOptionPane.showMessageDialog(parent, "No se pudo transformar a EPSG:4326.");
                return;
            }
            String text = MapUtilities.formatNumber(geographic[0]) + ", " + MapUtilities.formatNumber(geographic[1]);
            MapUtilities.copyToClipboard(text);
            ctx.showCopiedMessage("Lat/Long copiada.");
        });
        popupMenu.add(copyLatLonItem);

        JMenuItem copyDmsItem = new JMenuItem("Copiar Lat/Long DMS");
        copyDmsItem.addActionListener(ev -> {
            if (geographic == null) {
                JOptionPane.showMessageDialog(parent, "No se pudo transformar a EPSG:4326.");
                return;
            }
            String text = MapUtilities.toDms(geographic[0], false) + " , " + MapUtilities.toDms(geographic[1], true);
            MapUtilities.copyToClipboard(text);
            ctx.showCopiedMessage("Lat/Long DMS copiada.");
        });
        popupMenu.add(copyDmsItem);

        JMenuItem showItem = new JMenuItem("Ver coordenadas del punto");
        showItem.addActionListener(ev -> ctx.showCoordinateDialog(x, y));
        popupMenu.add(showItem);

        if (ctx.hasPins()) {
            popupMenu.addSeparator();

            JMenuItem convertPinsItem = new JMenuItem("Convertir pines en capa");
            convertPinsItem.addActionListener(ev -> ctx.convertPinsToLayer());
            popupMenu.add(convertPinsItem);

            JMenuItem clearPinsItem = new JMenuItem("Borrar todos los pines");
            clearPinsItem.addActionListener(ev -> {
                ctx.clearAllPins();
                ctx.showCopiedMessage("Todos los pines fueron eliminados.");
            });
            popupMenu.add(clearPinsItem);
        }

        popupMenu.show(parent, x, y);
    }

    void showFeaturePopup(MouseEvent e, IdentifyResultItem hit) {
        if (hit == null || hit.getLayer() == null || hit.getFeature() == null) {
            return;
        }

        Layer layer = hit.getLayer();
        SimpleFeature feature = hit.getFeature();

        JPopupMenu popupMenu = new JPopupMenu();
        boolean readOnlyLayer = ctx.isReadOnlyVectorLayer(layer);

        JMenuItem editItem = new JMenuItem(readOnlyLayer ? "Capa en solo lectura" : "Editar vector");
        editItem.setEnabled(!readOnlyLayer);
        editItem.addActionListener(ev -> {
            ctx.enableFeatureEdit(layer, feature);
            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage("Edicion vectorial lista para: " + layer.getName());
            }
            CatgisDesktopApp.syncFloatingVectorEditToolbar();
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
                ctx.enableFeatureEdit(layer, feature);
                ctx.activateMoveVertexMode();
            });
            popupMenu.add(moveVertexItem);

            JMenuItem addVertexItem = new JMenuItem("Agregar vertice");
            addVertexItem.addActionListener(ev -> {
                ctx.enableFeatureEdit(layer, feature);
                ctx.activateAddVertexMode();
            });
            popupMenu.add(addVertexItem);

            JMenuItem removeVertexItem = new JMenuItem("Eliminar vertice");
            removeVertexItem.addActionListener(ev -> {
                ctx.enableFeatureEdit(layer, feature);
                ctx.activateRemoveVertexMode();
            });
            popupMenu.add(removeVertexItem);

            JMenuItem cutItem = new JMenuItem("Cortar geometria");
            cutItem.addActionListener(ev -> {
                ctx.enableFeatureEdit(layer, feature);
                ctx.activateCutFeatureMode();
            });
            popupMenu.add(cutItem);
        }

        if (!readOnlyLayer && (geometry instanceof Polygon || geometry instanceof MultiPolygon)) {
            JMenuItem holeItem = new JMenuItem("Crear agujero");
            holeItem.addActionListener(ev -> {
                ctx.enableFeatureEdit(layer, feature);
                ctx.activateHoleMode();
            });
            popupMenu.add(holeItem);
        }

        popupMenu.addSeparator();

        JMenuItem infoItem = new JMenuItem("Ver informacion");
        infoItem.addActionListener(ev -> ctx.showFeatureInfo(feature, layer));
        popupMenu.add(infoItem);

        JMenuItem zoomItem = new JMenuItem("Zoom a entidad");
        zoomItem.addActionListener(ev -> ctx.zoomToFeature(feature, layer));
        popupMenu.add(zoomItem);

        JMenuItem attrItem = new JMenuItem(readOnlyLayer ? "Ver atributos" : "Editar atributos");
        attrItem.addActionListener(ev -> OpenAttributeTableAction.openTable(layer));
        popupMenu.add(attrItem);

        JMenuItem propertiesItem = new JMenuItem("Opciones de capa");
        propertiesItem.addActionListener(ev -> LayerPropertiesDialog.open(layer));
        popupMenu.add(propertiesItem);

        JMenuItem clearItem = new JMenuItem("Limpiar seleccion");
        clearItem.addActionListener(ev -> ctx.clearSelectedFeature());
        popupMenu.add(clearItem);

        popupMenu.show(parent, e.getX(), e.getY());
    }

    private void showPinPopup(MouseEvent e, PinMarker pin) {
        ctx.showPinPopup(e, pin);
    }
}
