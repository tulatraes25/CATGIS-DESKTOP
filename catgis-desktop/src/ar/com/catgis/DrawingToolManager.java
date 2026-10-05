package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;

import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class DrawingToolManager {

    private final MapPanel map;

    final List<Coordinate> drawingCoordinates = new ArrayList<>();
    final List<Geometry> pendingDrawingSessionGeometries = new ArrayList<>();
    String drawingMode = null;
    Layer drawingSessionLayer = null;
    boolean drawingSessionDirty = false;
    Layer drawingContinuationLayer = null;
    String drawingContinuationFeatureId = null;
    Coordinate[] drawingContinuationBaseCoordinates = null;
    boolean drawingContinuationFromStart = false;
    boolean drawingContinuationEndpointChosen = false;

    DrawingToolManager(MapPanel map) {
        this.map = map;
    }

    public void enableDrawPointMode() {
        map.cancelCurrentMeasurement();
        drawingMode = "POINT";
        drawingCoordinates.clear();
        drawingContinuationLayer = null;
        drawingContinuationFeatureId = null;
        map.setTool("DRAW");
        map.showCopiedMessage("Modo dibujo puntos activo. Hacé varios clics. Clic derecho para terminar y Escape para cancelar.");
        map.repaint();
    }

    public void enableDrawMultiPointMode() {
        map.cancelCurrentMeasurement();
        drawingMode = "MULTIPOINT";
        drawingCoordinates.clear();
        drawingContinuationLayer = null;
        drawingContinuationFeatureId = null;
        map.setTool("DRAW");
        map.showCopiedMessage("Modo dibujo multipunto activo. Hacé varios clics. Clic derecho para terminar y Escape para cancelar.");
        map.repaint();
    }

    public void enableDrawLineMode() {
        map.cancelCurrentMeasurement();
        drawingMode = "LINE";
        drawingCoordinates.clear();
        drawingContinuationLayer = null;
        drawingContinuationFeatureId = null;
        map.setTool("DRAW");
        map.showCopiedMessage("Modo dibujo línea activo. Clic para vértices. Doble clic o clic derecho para terminar. Escape para cancelar.");
        map.repaint();
    }

    public void enableContinueLineMode() {
        if (map.getSelectedFeatureCount() != 1 && map.selectedFeature != null) {
            NotificationManager.warn(map, null, "Para continuar una linea tenes que seleccionar exactamente una sola entidad lineal.");
            return;
        }
        if (map.selectedLayer == null || map.selectedFeature == null) {
            NotificationManager.warn(map, null, "Primero seleccioná una línea para continuar.");
            return;
        }

        Coordinate[] baseCoordinates = extractContinuableLineCoordinates(map.extractFeatureGeometryCopy(map.selectedFeature));
        if (baseCoordinates == null || baseCoordinates.length < 2) {
            NotificationManager.warn(map, null, "La entidad seleccionada no es una línea continua compatible.");
            return;
        }

        map.cancelCurrentMeasurement();
        drawingMode = "CONTINUE_LINE";
        drawingCoordinates.clear();
        drawingContinuationBaseCoordinates = cloneCoordinates(baseCoordinates);
        drawingContinuationFromStart = false;
        drawingContinuationEndpointChosen = false;
        for (Coordinate coordinate : baseCoordinates) {
            if (coordinate != null) {
                drawingCoordinates.add(new Coordinate(coordinate));
            }
        }
        drawingContinuationLayer = map.selectedLayer;
        drawingContinuationFeatureId = map.selectedFeature.getID();
        drawingSessionLayer = map.selectedLayer;
        map.setTool("DRAW");
        map.showCopiedMessage("Continuación de línea activa. Agregá vértices y terminá con doble clic o clic derecho.");
        map.repaint();
    }

    public void enableDrawRectangleMode() {
        map.cancelCurrentMeasurement();
        drawingMode = "RECTANGLE";
        drawingCoordinates.clear();
        drawingContinuationLayer = null;
        drawingContinuationFeatureId = null;
        map.setTool("DRAW");
        map.showCopiedMessage("Modo rectángulo activo. Marcá la primera esquina y luego la opuesta.");
        map.repaint();
    }

    public void enableDrawCircleMode() {
        map.cancelCurrentMeasurement();
        drawingMode = "CIRCLE";
        drawingCoordinates.clear();
        drawingContinuationLayer = null;
        drawingContinuationFeatureId = null;
        map.clearCadConstructionState();
        map.setTool("DRAW");
        map.showCopiedMessage("Modo circulo activo. Marca el centro y despues un punto del radio.");
        map.repaint();
    }

    public void enableDrawCircleThreePointMode() {
        map.cancelCurrentMeasurement();
        drawingMode = "CIRCLE_3P";
        drawingCoordinates.clear();
        drawingContinuationLayer = null;
        drawingContinuationFeatureId = null;
        map.clearCadConstructionState();
        map.setTool("DRAW");
        map.showCopiedMessage("Modo circulo por 3 puntos activo. Marca tres puntos sobre la circunferencia.");
        map.repaint();
    }

    public void enableDrawPolygonMode() {
        map.cancelCurrentMeasurement();
        drawingMode = "POLYGON";
        drawingCoordinates.clear();
        drawingContinuationLayer = null;
        drawingContinuationFeatureId = null;
        map.setTool("DRAW");
        map.showCopiedMessage("Modo dibujo polígono activo. Clic para vértices. Doble clic o clic derecho para terminar. Escape para cancelar.");
        map.repaint();
    }

    public void cancelCurrentDrawing() {
        drawingMode = null;
        drawingCoordinates.clear();
        pendingDrawingSessionGeometries.clear();
        drawingSessionLayer = null;
        drawingSessionDirty = false;
        drawingContinuationLayer = null;
        drawingContinuationFeatureId = null;
        drawingContinuationBaseCoordinates = null;
        drawingContinuationFromStart = false;
        drawingContinuationEndpointChosen = false;
        map.clearCadConstructionState();
        CatgisDesktopApp.syncFloatingVectorEditToolbar();
        map.repaint();
    }

    public void finishCurrentDrawing() {
        if (!map.isDrawingActive()) {
            return;
        }

        try {
            if ("CONTINUE_LINE".equalsIgnoreCase(drawingMode)) {
                Geometry continuationGeometry = buildContinuationLineGeometry();
                if (continuationGeometry == null) {
                    NotificationManager.warn(map, null, "Para continuar la línea necesitás agregar al menos un vértice nuevo.");
                    return;
                }
                map.updateSelectedFeatureGeometry(continuationGeometry, "Línea continuada.");
                cancelCurrentDrawing();
                return;
            }

            Layer targetLayer = resolveDrawingTargetLayer();
            if (targetLayer == null) {
                List<Geometry> sessionGeometries = buildDrawingGeometriesForLayer(null);
                if (sessionGeometries.isEmpty()) {
                    return;
                }
                pendingDrawingSessionGeometries.addAll(sessionGeometries);
                drawingSessionDirty = true;
                drawingCoordinates.clear();
                CatgisDesktopApp.syncFloatingVectorEditToolbar();
                map.repaint();
                map.showCopiedMessage(sessionGeometries.size() == 1
                        ? "Entidad cerrada. Podes seguir dibujando y decidir la capa al terminar."
                        : sessionGeometries.size() + " entidades preparadas en la sesion de dibujo.");
                return;
            }
            if (targetLayer == null) {
                boolean yes = NotificationManager.confirm(
                        map,
                        "Crear capa destino",
                        "No hay una capa vectorial editable compatible para este dibujo.\n\n¿Querés crearla ahora?");
                if (!yes) {
                    map.showCopiedMessage("Seleccioná o creá una capa compatible para guardar el dibujo.");
                    return;
                }

                targetLayer = NewVectorLayerAction.createNewVectorLayer(resolveDrawingGeometryFamily(drawingMode), map);
                if (targetLayer == null) {
                    map.showCopiedMessage("El dibujo sigue activo hasta que completes la capa destino o lo canceles.");
                    return;
                }
            }

            if (!pendingDrawingSessionGeometries.isEmpty()) {
                if (!appendGeometriesToLayer(
                        targetLayer,
                        new ArrayList<>(pendingDrawingSessionGeometries),
                        pendingDrawingSessionGeometries.size() == 1
                                ? "Entidad pendiente agregada a la capa."
                                : pendingDrawingSessionGeometries.size() + " entidades pendientes agregadas a la capa."
                )) {
                    return;
                }
                pendingDrawingSessionGeometries.clear();
            }

            if (!appendCurrentDrawingToLayer(targetLayer)) {
                return;
            }

            drawingSessionLayer = targetLayer;
            drawingSessionDirty = true;
            drawingCoordinates.clear();
            CatgisDesktopApp.syncFloatingVectorEditToolbar();
            map.repaint();
        } catch (Exception ex) {
            AppErrorSupport.logFailure("Error al agregar la geometria dibujada a la capa", ex);
            AppErrorSupport.showErrorDialog(map, "Dibujo vectorial", "Error al agregar la geometria a la capa.", ex);
        }
    }

    public void closeCurrentDrawingSession() {
        if (!map.isDrawingActive()) {
            return;
        }

        boolean hasPendingCurrentGeometry = !drawingCoordinates.isEmpty();
        if ("CONTINUE_LINE".equalsIgnoreCase(drawingMode)) {
            hasPendingCurrentGeometry = drawingContinuationEndpointChosen
                    && drawingContinuationBaseCoordinates != null
                    && drawingCoordinates.size() > drawingContinuationBaseCoordinates.length;
        }

        if (hasPendingCurrentGeometry) {
            int closeCurrent = NotificationManager.confirmCancel(
                    map,
                    "Cerrar dibujo",
                    "La entidad actual todavia no fue cerrada.\n\nQueres guardarla antes de cerrar el dibujo?");
            if (closeCurrent == JOptionPane.CANCEL_OPTION || closeCurrent == JOptionPane.CLOSED_OPTION) {
                return;
            }
            if (closeCurrent == JOptionPane.YES_OPTION) {
                finishCurrentDrawing();
                if (!drawingCoordinates.isEmpty()) {
                    return;
                }
            } else {
                drawingCoordinates.clear();
            }
        }

        Layer layerToSave = drawingSessionLayer != null ? drawingSessionLayer : resolveDrawingTargetLayer();
        if (!pendingDrawingSessionGeometries.isEmpty() && layerToSave == null) {
            int choice = NotificationManager.confirmCancel(
                    map,
                    "Crear capa destino",
                    "No hay una capa vectorial compatible todavia.\n\nQueres crearla ahora para guardar las entidades dibujadas?");
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
            layerToSave = NewVectorLayerAction.createNewVectorLayer(resolveDrawingGeometryFamily(drawingMode), map);
            if (layerToSave == null) {
                return;
            }
        }

        if (!pendingDrawingSessionGeometries.isEmpty()) {
            if (!appendGeometriesToLayer(
                    layerToSave,
                    new ArrayList<>(pendingDrawingSessionGeometries),
                    pendingDrawingSessionGeometries.size() == 1
                            ? "Entidad de la sesion agregada a la capa."
                            : pendingDrawingSessionGeometries.size() + " entidades de la sesion agregadas a la capa."
            )) {
                return;
            }
            drawingSessionLayer = layerToSave;
            pendingDrawingSessionGeometries.clear();
        }

        if (drawingSessionDirty && layerToSave != null) {
            int saveChoice = NotificationManager.confirmCancel(
                    map,
                    "Guardar capa vectorial",
                    "Queres guardar ahora la capa vectorial?\n\n" + layerToSave.getName());
            if (saveChoice == JOptionPane.CANCEL_OPTION || saveChoice == JOptionPane.CLOSED_OPTION) {
                return;
            }
            if (saveChoice == JOptionPane.YES_OPTION && !map.saveVectorLayerNow(layerToSave)) {
                return;
            }
        }

        cancelCurrentDrawing();
        map.showCopiedMessage("Sesion de dibujo cerrada.");
    }

    void appendDrawingCoordinateIfNeeded(Coordinate coordinate) {
        if (coordinate == null) {
            return;
        }
        if (drawingCoordinates.isEmpty() || !drawingCoordinates.get(drawingCoordinates.size() - 1).equals2D(coordinate)) {
            drawingCoordinates.add(coordinate);
        }
    }

    Layer resolveDrawingTargetLayer() {
        if (isCompatibleDrawingTarget(drawingSessionLayer, drawingMode)) {
            return drawingSessionLayer;
        }

        if (isCompatibleDrawingTarget(map.activeVectorEditingLayer, drawingMode)) {
            return map.activeVectorEditingLayer;
        }

        if (map.selectedLayer != null && isCompatibleDrawingTarget(map.selectedLayer, drawingMode)) {
            return map.selectedLayer;
        }

        return null;
    }

    boolean isCompatibleDrawingTarget(Layer layer, String mode) {
        if (layer == null || layer instanceof RasterLayer || map.isReadOnlyVectorLayer(layer)) {
            return false;
        }

        ShapefileData data = map.getShapefileData(layer);
        if (data == null || data.getSchema() == null) {
            return false;
        }

        String drawingFamily = resolveDrawingGeometryFamily(mode);
        String layerFamily = resolveLayerGeometryFamily(data.getSchema());
        return !drawingFamily.isBlank() && drawingFamily.equalsIgnoreCase(layerFamily);
    }

    String resolveDrawingGeometryFamily(String mode) {
        if (mode == null) {
            return "";
        }
        switch (mode.trim().toUpperCase(Locale.ROOT)) {
            case "POINT":
            case "MULTIPOINT":
                return "POINT";
            case "LINE":
            case "CONTINUE_LINE":
                return "LINE";
            case "CIRCLE":
            case "CIRCLE_3P":
            case "RECTANGLE":
            case "POLYGON":
                return "POLYGON";
            default:
                return "";
        }
    }

    String resolveLayerGeometryFamily(SimpleFeatureType schema) {
        if (schema == null || schema.getGeometryDescriptor() == null || schema.getGeometryDescriptor().getType() == null) {
            return "";
        }
        return DrawFeatureBuilder.resolveGeometryFamily(schema.getGeometryDescriptor().getType().getBinding());
    }

    boolean appendCurrentDrawingToLayer(Layer layer) {
        if (layer == null) {
            return false;
        }

        ShapefileData targetData = map.getShapefileData(layer);
        if (targetData == null || targetData.getSchema() == null) {
            NotificationManager.warn(map, null, "La capa destino no tiene esquema vectorial disponible.");
            return false;
        }

        List<Geometry> newGeometries = buildDrawingGeometriesForLayer(targetData.getSchema());
        if (newGeometries.isEmpty()) {
            return false;
        }

        return appendGeometriesToLayer(layer, newGeometries, null);
    }

    boolean appendGeometriesToLayer(Layer layer, List<Geometry> newGeometries, String successMessage) {
        if (layer == null || newGeometries == null || newGeometries.isEmpty()) {
            return false;
        }

        ShapefileData targetData = map.getShapefileData(layer);
        if (targetData == null || targetData.getSchema() == null) {
            NotificationManager.warn(map, null, "La capa destino no tiene esquema vectorial disponible.");
            return false;
        }

        map.pushUndoSnapshot(layer, null);

        List<SimpleFeature> features = new ArrayList<>(targetData.getFeatures());
        List<String> createdIds = new ArrayList<>();
        for (Geometry geometry : newGeometries) {
            SimpleFeature createdFeature = map.buildNewFeatureForLayer(targetData, geometry, features);
            if (createdFeature == null) {
                continue;
            }
            features.add(createdFeature);
            createdIds.add(createdFeature.getID());
        }

        if (createdIds.isEmpty()) {
            NotificationManager.warn(map, null, "No se pudo crear la entidad en la capa seleccionada.");
            return false;
        }

        map.replaceLayerFeatures(layer, features, createdIds.size() == 1 ? createdIds.get(0) : null, false, null);
        map.applyFeatureSelection(
                layer,
                createdIds,
                false,
                true,
                false,
                successMessage != null && !successMessage.isBlank()
                        ? successMessage
                        : createdIds.size() == 1
                        ? "Entidad agregada a la capa."
                        : createdIds.size() + " entidades agregadas a la capa."
        );
        return true;
    }

    List<Geometry> buildDrawingGeometriesForLayer(SimpleFeatureType targetType) {
        List<Geometry> geometries = new ArrayList<>();
        Class<?> geometryBinding = targetType != null
                && targetType.getGeometryDescriptor() != null
                && targetType.getGeometryDescriptor().getType() != null
                ? targetType.getGeometryDescriptor().getType().getBinding()
                : null;

        if ("POINT".equalsIgnoreCase(drawingMode) || "MULTIPOINT".equalsIgnoreCase(drawingMode)) {
            if (drawingCoordinates.isEmpty()) {
                NotificationManager.warn(map, null, "Para crear puntos necesitás hacer clic en el mapa.");
                return geometries;
            }

            GeometryFactory gf = new GeometryFactory();
            if (geometryBinding != null && MultiPoint.class.isAssignableFrom(geometryBinding)) {
                Point[] points = new Point[drawingCoordinates.size()];
                for (int i = 0; i < drawingCoordinates.size(); i++) {
                    points[i] = gf.createPoint(new Coordinate(drawingCoordinates.get(i)));
                }
                geometries.add(gf.createMultiPoint(points));
            } else {
                for (Coordinate coordinate : drawingCoordinates) {
                    geometries.add(DrawFeatureBuilder.buildPoint(coordinate));
                }
            }
            return geometries;
        }

        if ("LINE".equalsIgnoreCase(drawingMode)) {
            Geometry geometry = DrawFeatureBuilder.buildLine(drawingCoordinates);
            if (geometry == null) {
                NotificationManager.warn(map, null, "Para una línea necesitás al menos 2 vértices.");
                return geometries;
            }
            geometries.add(geometry);
            return geometries;
        }

        if ("RECTANGLE".equalsIgnoreCase(drawingMode)) {
            Geometry geometry = buildRectangleGeometry(drawingCoordinates);
            if (geometry == null) {
                NotificationManager.warn(map, null, "Para un rectángulo necesitás marcar dos esquinas opuestas.");
                return geometries;
            }
            geometries.add(geometry);
            return geometries;
        }

        if ("POLYGON".equalsIgnoreCase(drawingMode)) {
            Geometry geometry = DrawFeatureBuilder.buildPolygon(drawingCoordinates);
            if (geometry == null) {
                NotificationManager.warn(map, null, "Para un polígono necesitás al menos 3 vértices.");
                return geometries;
            }
            geometries.add(geometry);
        }

        if ("CIRCLE".equalsIgnoreCase(drawingMode)) {
            Geometry geometry = buildCircleGeometry(drawingCoordinates);
            if (geometry == null) {
                NotificationManager.warn(map, null, "Para un circulo necesitas marcar centro y radio.");
                return geometries;
            }
            geometries.add(geometry);
            return geometries;
        }

        if ("CIRCLE_3P".equalsIgnoreCase(drawingMode)) {
            Geometry geometry = buildCircleThreePointGeometry(drawingCoordinates);
            if (geometry == null) {
                NotificationManager.warn(map, null, "No se pudo construir el circulo con esos tres puntos.");
                return geometries;
            }
            geometries.add(geometry);
            return geometries;
        }

        return geometries;
    }

    void chooseContinuationEndpoint(int screenX, int screenY) {
        if (drawingContinuationBaseCoordinates == null || drawingContinuationBaseCoordinates.length < 2 || drawingContinuationLayer == null) {
            map.showCopiedMessage("No se encontro una linea base valida para continuar.");
            return;
        }

        Coordinate start = map.toProjectCoordinate(drawingContinuationBaseCoordinates[0], drawingContinuationLayer);
        Coordinate end = map.toProjectCoordinate(
                drawingContinuationBaseCoordinates[drawingContinuationBaseCoordinates.length - 1],
                drawingContinuationLayer
        );
        if (start == null || end == null) {
            map.showCopiedMessage("No se pudieron ubicar los extremos de la linea seleccionada.");
            return;
        }

        int startX = map.worldToScreenX(start.x);
        int startY = map.worldToScreenY(start.y);
        int endX = map.worldToScreenX(end.x);
        int endY = map.worldToScreenY(end.y);
        double startDistance = Math.hypot(screenX - startX, screenY - startY);
        double endDistance = Math.hypot(screenX - endX, screenY - endY);
        double tolerancePx = Math.max(MapPanel.EDIT_VERTEX_TOLERANCE_PX + 6, 16);

        if (startDistance > tolerancePx && endDistance > tolerancePx) {
            map.showCopiedMessage("Hace clic sobre uno de los extremos resaltados para indicar desde donde continuar.");
            return;
        }

        drawingCoordinates.clear();
        drawingContinuationFromStart = startDistance <= endDistance;
        Coordinate[] oriented = drawingContinuationFromStart
                ? MapGeometryUtils.reverseCoordinates(drawingContinuationBaseCoordinates)
                : cloneCoordinates(drawingContinuationBaseCoordinates);
        for (Coordinate coordinate : oriented) {
            if (coordinate != null) {
                drawingCoordinates.add(new Coordinate(coordinate));
            }
        }
        drawingContinuationEndpointChosen = true;
        map.showCopiedMessage(drawingContinuationFromStart
                ? "Extremo inicial seleccionado. Ahora agrega los nuevos vertices y termina con doble clic."
                : "Extremo final seleccionado. Ahora agrega los nuevos vertices y termina con doble clic.");
        map.repaint();
    }

    Geometry buildContinuationLineGeometry() {
        if (drawingContinuationBaseCoordinates == null
                || !drawingContinuationEndpointChosen
                || drawingCoordinates.size() <= drawingContinuationBaseCoordinates.length
                || drawingContinuationLayer == null
                || drawingContinuationFeatureId == null) {
            return null;
        }

        if (map.selectedLayer != drawingContinuationLayer || !map.sameFeatureId(map.selectedFeature, drawingContinuationFeatureId)) {
            ShapefileData data = map.getShapefileData(drawingContinuationLayer);
            map.selectedLayer = drawingContinuationLayer;
            map.selectedFeature = data != null ? map.findFeatureById(data.getFeatures(), drawingContinuationFeatureId) : null;
        }

        if (map.selectedFeature == null) {
            return null;
        }

        Coordinate[] oriented = cloneCoordinates(drawingCoordinates.toArray(new Coordinate[0]));
        if (drawingContinuationFromStart) {
            oriented = MapGeometryUtils.reverseCoordinates(oriented);
        }
        oriented = MapGeometryUtils.collapseDuplicateLineCoordinates(oriented);
        if (oriented == null || oriented.length < 2) {
            return null;
        }

        List<Coordinate> continuedCoordinates = new ArrayList<>();
        for (Coordinate coordinate : oriented) {
            if (coordinate != null) {
                continuedCoordinates.add(new Coordinate(coordinate));
            }
        }
        return DrawFeatureBuilder.buildLine(continuedCoordinates);
    }

    Geometry buildRectangleGeometry(List<Coordinate> coordinates) {
        List<Coordinate> rectangleCoordinates = MapGeometryUtils.buildRectangleCoordinates(coordinates);
        if (rectangleCoordinates.isEmpty()) return null;
        return DrawFeatureBuilder.buildPolygon(rectangleCoordinates);
    }

    Geometry buildCircleGeometry(List<Coordinate> coordinates) {
        return MapGeometryUtils.buildCircleFromTwoPoints(coordinates, MapPanel.CIRCLE_SEGMENTS);
    }

    Geometry buildCircleThreePointGeometry(List<Coordinate> coordinates) {
        return MapGeometryUtils.buildCircleFromThreePoints(coordinates, MapPanel.CIRCLE_SEGMENTS);
    }

    Geometry buildCirclePolygon(Coordinate center, double radius) {
        return MapGeometryUtils.buildCirclePolygon(center, radius, MapPanel.CIRCLE_SEGMENTS);
    }

    Coordinate computeCircumcenter(Coordinate a, Coordinate b, Coordinate c) {
        return MapGeometryUtils.computeCircumcenter(a, b, c);
    }

    List<Coordinate> buildRectangleCoordinates(List<Coordinate> coordinates) {
        return MapGeometryUtils.buildRectangleCoordinates(coordinates);
    }

    Coordinate[] extractContinuableLineCoordinates(Geometry geometry) {
        return MapGeometryUtils.extractContinuableLineCoordinates(geometry);
    }

    Coordinate[] cloneCoordinates(Coordinate[] coordinates) {
        return MapGeometryUtils.cloneCoordinates(coordinates);
    }
}
