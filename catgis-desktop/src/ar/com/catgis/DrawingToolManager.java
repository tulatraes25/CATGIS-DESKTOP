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

    DrawingToolManager(MapPanel map) {
        this.map = map;
    }

    public void enableDrawPointMode() {
        map.cancelCurrentMeasurement();
        map.drawingMode = "POINT";
        map.drawingCoordinates.clear();
        map.drawingContinuationLayer = null;
        map.drawingContinuationFeatureId = null;
        map.setTool("DRAW");
        map.showCopiedMessage("Modo dibujo puntos activo. Hacé varios clics. Clic derecho para terminar y Escape para cancelar.");
        map.repaint();
    }

    public void enableDrawMultiPointMode() {
        map.cancelCurrentMeasurement();
        map.drawingMode = "MULTIPOINT";
        map.drawingCoordinates.clear();
        map.drawingContinuationLayer = null;
        map.drawingContinuationFeatureId = null;
        map.setTool("DRAW");
        map.showCopiedMessage("Modo dibujo multipunto activo. Hacé varios clics. Clic derecho para terminar y Escape para cancelar.");
        map.repaint();
    }

    public void enableDrawLineMode() {
        map.cancelCurrentMeasurement();
        map.drawingMode = "LINE";
        map.drawingCoordinates.clear();
        map.drawingContinuationLayer = null;
        map.drawingContinuationFeatureId = null;
        map.setTool("DRAW");
        map.showCopiedMessage("Modo dibujo línea activo. Clic para vértices. Doble clic o clic derecho para terminar. Escape para cancelar.");
        map.repaint();
    }

    public void enableContinueLineMode() {
        if (map.getSelectedFeatureCount() != 1 && map.selectedFeature != null) {
            JOptionPane.showMessageDialog(map, "Para continuar una linea tenes que seleccionar exactamente una sola entidad lineal.");
            return;
        }
        if (map.selectedLayer == null || map.selectedFeature == null) {
            JOptionPane.showMessageDialog(map, "Primero seleccioná una línea para continuar.");
            return;
        }

        Coordinate[] baseCoordinates = extractContinuableLineCoordinates(map.extractFeatureGeometryCopy(map.selectedFeature));
        if (baseCoordinates == null || baseCoordinates.length < 2) {
            JOptionPane.showMessageDialog(map, "La entidad seleccionada no es una línea continua compatible.");
            return;
        }

        map.cancelCurrentMeasurement();
        map.drawingMode = "CONTINUE_LINE";
        map.drawingCoordinates.clear();
        map.drawingContinuationBaseCoordinates = cloneCoordinates(baseCoordinates);
        map.drawingContinuationFromStart = false;
        map.drawingContinuationEndpointChosen = false;
        for (Coordinate coordinate : baseCoordinates) {
            if (coordinate != null) {
                map.drawingCoordinates.add(new Coordinate(coordinate));
            }
        }
        map.drawingContinuationLayer = map.selectedLayer;
        map.drawingContinuationFeatureId = map.selectedFeature.getID();
        map.drawingSessionLayer = map.selectedLayer;
        map.setTool("DRAW");
        map.showCopiedMessage("Continuación de línea activa. Agregá vértices y terminá con doble clic o clic derecho.");
        map.repaint();
    }

    public void enableDrawRectangleMode() {
        map.cancelCurrentMeasurement();
        map.drawingMode = "RECTANGLE";
        map.drawingCoordinates.clear();
        map.drawingContinuationLayer = null;
        map.drawingContinuationFeatureId = null;
        map.setTool("DRAW");
        map.showCopiedMessage("Modo rectángulo activo. Marcá la primera esquina y luego la opuesta.");
        map.repaint();
    }

    public void enableDrawCircleMode() {
        map.cancelCurrentMeasurement();
        map.drawingMode = "CIRCLE";
        map.drawingCoordinates.clear();
        map.drawingContinuationLayer = null;
        map.drawingContinuationFeatureId = null;
        map.clearCadConstructionState();
        map.setTool("DRAW");
        map.showCopiedMessage("Modo circulo activo. Marca el centro y despues un punto del radio.");
        map.repaint();
    }

    public void enableDrawCircleThreePointMode() {
        map.cancelCurrentMeasurement();
        map.drawingMode = "CIRCLE_3P";
        map.drawingCoordinates.clear();
        map.drawingContinuationLayer = null;
        map.drawingContinuationFeatureId = null;
        map.clearCadConstructionState();
        map.setTool("DRAW");
        map.showCopiedMessage("Modo circulo por 3 puntos activo. Marca tres puntos sobre la circunferencia.");
        map.repaint();
    }

    public void enableDrawPolygonMode() {
        map.cancelCurrentMeasurement();
        map.drawingMode = "POLYGON";
        map.drawingCoordinates.clear();
        map.drawingContinuationLayer = null;
        map.drawingContinuationFeatureId = null;
        map.setTool("DRAW");
        map.showCopiedMessage("Modo dibujo polígono activo. Clic para vértices. Doble clic o clic derecho para terminar. Escape para cancelar.");
        map.repaint();
    }

    public void cancelCurrentDrawing() {
        map.drawingMode = null;
        map.drawingCoordinates.clear();
        map.pendingDrawingSessionGeometries.clear();
        map.drawingSessionLayer = null;
        map.drawingSessionDirty = false;
        map.drawingContinuationLayer = null;
        map.drawingContinuationFeatureId = null;
        map.drawingContinuationBaseCoordinates = null;
        map.drawingContinuationFromStart = false;
        map.drawingContinuationEndpointChosen = false;
        map.clearCadConstructionState();
        CatgisDesktopApp.syncFloatingVectorEditToolbar();
        map.repaint();
    }

    public void finishCurrentDrawing() {
        if (!map.isDrawingActive()) {
            return;
        }

        try {
            if ("CONTINUE_LINE".equalsIgnoreCase(map.drawingMode)) {
                Geometry continuationGeometry = buildContinuationLineGeometry();
                if (continuationGeometry == null) {
                    JOptionPane.showMessageDialog(map, "Para continuar la línea necesitás agregar al menos un vértice nuevo.");
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
                map.pendingDrawingSessionGeometries.addAll(sessionGeometries);
                map.drawingSessionDirty = true;
                map.drawingCoordinates.clear();
                CatgisDesktopApp.syncFloatingVectorEditToolbar();
                map.repaint();
                map.showCopiedMessage(sessionGeometries.size() == 1
                        ? "Entidad cerrada. Podes seguir dibujando y decidir la capa al terminar."
                        : sessionGeometries.size() + " entidades preparadas en la sesion de dibujo.");
                return;
            }
            if (targetLayer == null) {
                int choice = JOptionPane.showConfirmDialog(
                        map,
                        "No hay una capa vectorial editable compatible para este dibujo.\n\nÂ¿Querés crearla ahora?",
                        "Crear capa destino",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (choice != JOptionPane.YES_OPTION) {
                    map.showCopiedMessage("Seleccioná o creá una capa compatible para guardar el dibujo.");
                    return;
                }

                targetLayer = NewVectorLayerAction.createNewVectorLayer(resolveDrawingGeometryFamily(map.drawingMode), map);
                if (targetLayer == null) {
                    map.showCopiedMessage("El dibujo sigue activo hasta que completes la capa destino o lo canceles.");
                    return;
                }
            }

            if (!map.pendingDrawingSessionGeometries.isEmpty()) {
                if (!appendGeometriesToLayer(
                        targetLayer,
                        new ArrayList<>(map.pendingDrawingSessionGeometries),
                        map.pendingDrawingSessionGeometries.size() == 1
                                ? "Entidad pendiente agregada a la capa."
                                : map.pendingDrawingSessionGeometries.size() + " entidades pendientes agregadas a la capa."
                )) {
                    return;
                }
                map.pendingDrawingSessionGeometries.clear();
            }

            if (!appendCurrentDrawingToLayer(targetLayer)) {
                return;
            }

            map.drawingSessionLayer = targetLayer;
            map.drawingSessionDirty = true;
            map.drawingCoordinates.clear();
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

        boolean hasPendingCurrentGeometry = !map.drawingCoordinates.isEmpty();
        if ("CONTINUE_LINE".equalsIgnoreCase(map.drawingMode)) {
            hasPendingCurrentGeometry = map.drawingContinuationEndpointChosen
                    && map.drawingContinuationBaseCoordinates != null
                    && map.drawingCoordinates.size() > map.drawingContinuationBaseCoordinates.length;
        }

        if (hasPendingCurrentGeometry) {
            int closeCurrent = JOptionPane.showConfirmDialog(
                    map,
                    "La entidad actual todavia no fue cerrada.\n\nQueres guardarla antes de cerrar el dibujo?",
                    "Cerrar dibujo",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (closeCurrent == JOptionPane.CANCEL_OPTION || closeCurrent == JOptionPane.CLOSED_OPTION) {
                return;
            }
            if (closeCurrent == JOptionPane.YES_OPTION) {
                finishCurrentDrawing();
                if (!map.drawingCoordinates.isEmpty()) {
                    return;
                }
            } else {
                map.drawingCoordinates.clear();
            }
        }

        Layer layerToSave = map.drawingSessionLayer != null ? map.drawingSessionLayer : resolveDrawingTargetLayer();
        if (!map.pendingDrawingSessionGeometries.isEmpty() && layerToSave == null) {
            int choice = JOptionPane.showConfirmDialog(
                    map,
                    "No hay una capa vectorial compatible todavia.\n\nQueres crearla ahora para guardar las entidades dibujadas?",
                    "Crear capa destino",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
            layerToSave = NewVectorLayerAction.createNewVectorLayer(resolveDrawingGeometryFamily(map.drawingMode), map);
            if (layerToSave == null) {
                return;
            }
        }

        if (!map.pendingDrawingSessionGeometries.isEmpty()) {
            if (!appendGeometriesToLayer(
                    layerToSave,
                    new ArrayList<>(map.pendingDrawingSessionGeometries),
                    map.pendingDrawingSessionGeometries.size() == 1
                            ? "Entidad de la sesion agregada a la capa."
                            : map.pendingDrawingSessionGeometries.size() + " entidades de la sesion agregadas a la capa."
            )) {
                return;
            }
            map.drawingSessionLayer = layerToSave;
            map.pendingDrawingSessionGeometries.clear();
        }

        if (map.drawingSessionDirty && layerToSave != null) {
            int saveChoice = JOptionPane.showConfirmDialog(
                    map,
                    "Queres guardar ahora la capa vectorial?\n\n" + layerToSave.getName(),
                    "Guardar capa vectorial",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
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
        if (map.drawingCoordinates.isEmpty() || !map.drawingCoordinates.get(map.drawingCoordinates.size() - 1).equals2D(coordinate)) {
            map.drawingCoordinates.add(coordinate);
        }
    }

    Layer resolveDrawingTargetLayer() {
        if (isCompatibleDrawingTarget(map.drawingSessionLayer, map.drawingMode)) {
            return map.drawingSessionLayer;
        }

        if (isCompatibleDrawingTarget(map.activeVectorEditingLayer, map.drawingMode)) {
            return map.activeVectorEditingLayer;
        }

        if (map.selectedLayer != null && isCompatibleDrawingTarget(map.selectedLayer, map.drawingMode)) {
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
            JOptionPane.showMessageDialog(map, "La capa destino no tiene esquema vectorial disponible.");
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
            JOptionPane.showMessageDialog(map, "La capa destino no tiene esquema vectorial disponible.");
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
            JOptionPane.showMessageDialog(map, "No se pudo crear la entidad en la capa seleccionada.");
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

        if ("POINT".equalsIgnoreCase(map.drawingMode) || "MULTIPOINT".equalsIgnoreCase(map.drawingMode)) {
            if (map.drawingCoordinates.isEmpty()) {
                JOptionPane.showMessageDialog(map, "Para crear puntos necesitás hacer clic en el mapa.");
                return geometries;
            }

            GeometryFactory gf = new GeometryFactory();
            if (geometryBinding != null && MultiPoint.class.isAssignableFrom(geometryBinding)) {
                Point[] points = new Point[map.drawingCoordinates.size()];
                for (int i = 0; i < map.drawingCoordinates.size(); i++) {
                    points[i] = gf.createPoint(new Coordinate(map.drawingCoordinates.get(i)));
                }
                geometries.add(gf.createMultiPoint(points));
            } else {
                for (Coordinate coordinate : map.drawingCoordinates) {
                    geometries.add(DrawFeatureBuilder.buildPoint(coordinate));
                }
            }
            return geometries;
        }

        if ("LINE".equalsIgnoreCase(map.drawingMode)) {
            Geometry geometry = DrawFeatureBuilder.buildLine(map.drawingCoordinates);
            if (geometry == null) {
                JOptionPane.showMessageDialog(map, "Para una línea necesitás al menos 2 vértices.");
                return geometries;
            }
            geometries.add(geometry);
            return geometries;
        }

        if ("RECTANGLE".equalsIgnoreCase(map.drawingMode)) {
            Geometry geometry = buildRectangleGeometry(map.drawingCoordinates);
            if (geometry == null) {
                JOptionPane.showMessageDialog(map, "Para un rectángulo necesitás marcar dos esquinas opuestas.");
                return geometries;
            }
            geometries.add(geometry);
            return geometries;
        }

        if ("POLYGON".equalsIgnoreCase(map.drawingMode)) {
            Geometry geometry = DrawFeatureBuilder.buildPolygon(map.drawingCoordinates);
            if (geometry == null) {
                JOptionPane.showMessageDialog(map, "Para un polígono necesitás al menos 3 vértices.");
                return geometries;
            }
            geometries.add(geometry);
        }

        if ("CIRCLE".equalsIgnoreCase(map.drawingMode)) {
            Geometry geometry = buildCircleGeometry(map.drawingCoordinates);
            if (geometry == null) {
                JOptionPane.showMessageDialog(map, "Para un circulo necesitas marcar centro y radio.");
                return geometries;
            }
            geometries.add(geometry);
            return geometries;
        }

        if ("CIRCLE_3P".equalsIgnoreCase(map.drawingMode)) {
            Geometry geometry = buildCircleThreePointGeometry(map.drawingCoordinates);
            if (geometry == null) {
                JOptionPane.showMessageDialog(map, "No se pudo construir el circulo con esos tres puntos.");
                return geometries;
            }
            geometries.add(geometry);
            return geometries;
        }

        return geometries;
    }

    void chooseContinuationEndpoint(int screenX, int screenY) {
        if (map.drawingContinuationBaseCoordinates == null || map.drawingContinuationBaseCoordinates.length < 2 || map.drawingContinuationLayer == null) {
            map.showCopiedMessage("No se encontro una linea base valida para continuar.");
            return;
        }

        Coordinate start = map.toProjectCoordinate(map.drawingContinuationBaseCoordinates[0], map.drawingContinuationLayer);
        Coordinate end = map.toProjectCoordinate(
                map.drawingContinuationBaseCoordinates[map.drawingContinuationBaseCoordinates.length - 1],
                map.drawingContinuationLayer
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

        map.drawingCoordinates.clear();
        map.drawingContinuationFromStart = startDistance <= endDistance;
        Coordinate[] oriented = map.drawingContinuationFromStart
                ? MapGeometryUtils.reverseCoordinates(map.drawingContinuationBaseCoordinates)
                : cloneCoordinates(map.drawingContinuationBaseCoordinates);
        for (Coordinate coordinate : oriented) {
            if (coordinate != null) {
                map.drawingCoordinates.add(new Coordinate(coordinate));
            }
        }
        map.drawingContinuationEndpointChosen = true;
        map.showCopiedMessage(map.drawingContinuationFromStart
                ? "Extremo inicial seleccionado. Ahora agrega los nuevos vertices y termina con doble clic."
                : "Extremo final seleccionado. Ahora agrega los nuevos vertices y termina con doble clic.");
        map.repaint();
    }

    Geometry buildContinuationLineGeometry() {
        if (map.drawingContinuationBaseCoordinates == null
                || !map.drawingContinuationEndpointChosen
                || map.drawingCoordinates.size() <= map.drawingContinuationBaseCoordinates.length
                || map.drawingContinuationLayer == null
                || map.drawingContinuationFeatureId == null) {
            return null;
        }

        if (map.selectedLayer != map.drawingContinuationLayer || !map.sameFeatureId(map.selectedFeature, map.drawingContinuationFeatureId)) {
            ShapefileData data = map.getShapefileData(map.drawingContinuationLayer);
            map.selectedLayer = map.drawingContinuationLayer;
            map.selectedFeature = data != null ? map.findFeatureById(data.getFeatures(), map.drawingContinuationFeatureId) : null;
        }

        if (map.selectedFeature == null) {
            return null;
        }

        Coordinate[] oriented = cloneCoordinates(map.drawingCoordinates.toArray(new Coordinate[0]));
        if (map.drawingContinuationFromStart) {
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
