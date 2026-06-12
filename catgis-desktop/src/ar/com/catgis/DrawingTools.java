package ar.com.catgis;

import ar.com.catgis.service.EventBus;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;

import javax.swing.JOptionPane;
import java.io.File;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.ExportVectorLayerAction;
import ar.com.catgis.data.online.OnlineRasterSource;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DrawingTools {
    private final MapPanel panel;

    public DrawingTools(MapPanel panel) {
        this.panel = panel;
    }

    public void cancelCurrentDrawing() {
        panel.drawingToolManager.drawingMode = null;
        panel.drawingToolManager.drawingCoordinates.clear();
        panel.drawingToolManager.pendingDrawingSessionGeometries.clear();
        panel.drawingToolManager.drawingSessionLayer = null;
        panel.drawingToolManager.drawingSessionDirty = false;
        panel.drawingToolManager.drawingContinuationLayer = null;
        panel.drawingToolManager.drawingContinuationFeatureId = null;
        panel.drawingToolManager.drawingContinuationBaseCoordinates = null;
        panel.drawingToolManager.drawingContinuationFromStart = false;
        panel.drawingToolManager.drawingContinuationEndpointChosen = false;
        panel.clearCadConstructionState();
        EventBus.emit(EventBus.EventType.TOOLBAR_SYNC);
        panel.repaint();
    }

    public void cancelCurrentMeasurement() {
        panel.measurementTool.cancelMeasurement();
        EventBus.emit(EventBus.EventType.TOOLBAR_SYNC);
        panel.repaint();
    }

    public void finishCurrentMeasurement() {
        if (!panel.isMeasurementActive()) {
            return;
        }

        try {
            String projectCRS = (AppContext.project() != null &&
                    AppContext.project().getProjectCRS() != null &&
                    !AppContext.project().getProjectCRS().isBlank())
                    ? AppContext.project().getProjectCRS()
                    : "EPSG:4326";

            if ("DISTANCE".equalsIgnoreCase(panel.getMeasurementMode())) {
                if (panel.measurementTool.getPoints().size() < 2) {
                    JOptionPane.showMessageDialog(panel, "Para medir distancia necesit\u00E1s al menos 2 v\u00E9rtices.");
                    return;
                }

                Geometry metricLine = buildMeasurementLineInMeters(panel.measurementTool.getPoints(), projectCRS);
                if (metricLine == null) {
                    JOptionPane.showMessageDialog(panel, "No se pudo calcular la distancia.");
                    return;
                }

                double totalMeters = metricLine.getLength();

                JOptionPane.showMessageDialog(
                        panel,
                        "Distancia total: " + formatDistance(totalMeters),
                        "Medici\u00F3n de distancia",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else if ("AREA".equalsIgnoreCase(panel.getMeasurementMode())) {
                if (panel.measurementTool.getPoints().size() < 3) {
                    JOptionPane.showMessageDialog(panel, "Para medir \u00E1rea necesit\u00E1s al menos 3 v\u00E9rtices.");
                    return;
                }

                Geometry metricPolygon = buildMeasurementPolygonInMeters(panel.measurementTool.getPoints(), projectCRS);
                if (metricPolygon == null) {
                    JOptionPane.showMessageDialog(panel, "No se pudo calcular el \u00E1rea.");
                    return;
                }

                double areaMeters = metricPolygon.getArea();
                double perimeterMeters = metricPolygon.getLength();

                JOptionPane.showMessageDialog(
                        panel,
                        "\u00C1rea: " + formatArea(areaMeters) + "\n" +
                                "Per\u00EDmetro: " + formatDistance(perimeterMeters),
                        "Medici\u00F3n de \u00E1rea",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } finally {
            cancelCurrentMeasurement();
            EventBus.emit(EventBus.EventType.TOOLBAR_SYNC);
        }
    }

    private Geometry buildMeasurementLineInMeters(List<Coordinate> coordinates, String sourceCRSCode) {
        try {
            if (coordinates == null || coordinates.size() < 2) {
                return null;
            }

            org.locationtech.jts.geom.GeometryFactory gf = new org.locationtech.jts.geom.GeometryFactory();
            org.locationtech.jts.geom.LineString line =
                    gf.createLineString(coordinates.toArray(new Coordinate[0]));

            return reprojectGeometryToMetric(line, sourceCRSCode);
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo construir la geometria de medicion lineal", ex);
            return null;
        }
    }

    private Geometry buildMeasurementPolygonInMeters(List<Coordinate> coordinates, String sourceCRSCode) {
        try {
            Geometry polygon = DrawFeatureBuilder.buildPolygon(coordinates);
            if (polygon == null) {
                return null;
            }

            return reprojectGeometryToMetric(polygon, sourceCRSCode);
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo construir la geometria de medicion de area", ex);
            return null;
        }
    }

    private Geometry reprojectGeometryToMetric(Geometry geometry, String sourceCRSCode) {
        try {
            if (geometry == null || geometry.isEmpty()) {
                return geometry;
            }

            String sourceCode = (sourceCRSCode != null && !sourceCRSCode.isBlank())
                    ? sourceCRSCode
                    : "EPSG:4326";

            String targetMetricCode = panel.chooseMetricCRSForMeasurement(sourceCode);

            if (sourceCode.equalsIgnoreCase(targetMetricCode)) {
                return geometry;
            }

            CoordinateReferenceSystem sourceCRS = CRSDefinitions.decode(sourceCode, true);
            CoordinateReferenceSystem targetCRS = CRSDefinitions.decode(targetMetricCode, true);
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            return JTS.transform(geometry, transform);
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo reproyectar la geometria para medicion", ex);
            return geometry;
        }
    }

    private String formatDistance(double meters) {
        if (meters >= 1000.0) {
            return String.format(Locale.US, "%.3f km", meters / 1000.0);
        }
        return String.format(Locale.US, "%.2f m", meters);
    }

    private String formatArea(double squareMeters) {
        if (squareMeters >= 10000.0) {
            return String.format(Locale.US, "%.3f ha", squareMeters / 10000.0);
        }
        return String.format(Locale.US, "%.2f m\u00B2", squareMeters);
    }

    public void finishCurrentDrawing() {
        if (!panel.isDrawingActive()) {
            return;
        }

        try {
            if ("CONTINUE_LINE".equalsIgnoreCase(panel.drawingToolManager.drawingMode)) {
                Geometry continuationGeometry = buildContinuationLineGeometry();
                if (continuationGeometry == null) {
                    JOptionPane.showMessageDialog(panel, "Para continuar la l\u00EDnea necesit\u00E1s agregar al menos un v\u00E9rtice nuevo.");
                    return;
                }
                panel.updateSelectedFeatureGeometry(continuationGeometry, "L\u00EDnea continuada.");
                cancelCurrentDrawing();
                return;
            }

            Layer targetLayer = resolveDrawingTargetLayer();
            if (targetLayer == null) {
                List<Geometry> sessionGeometries = buildDrawingGeometriesForLayer(null);
                if (sessionGeometries.isEmpty()) {
                    return;
                }
                panel.drawingToolManager.pendingDrawingSessionGeometries.addAll(sessionGeometries);
                panel.drawingToolManager.drawingSessionDirty = true;
                panel.drawingToolManager.drawingCoordinates.clear();
                EventBus.emit(EventBus.EventType.TOOLBAR_SYNC);
                panel.repaint();
                panel.showCopiedMessage(sessionGeometries.size() == 1
                        ? "Entidad cerrada. Podes seguir dibujando y decidir la capa al terminar."
                        : sessionGeometries.size() + " entidades preparadas en la sesion de dibujo.");
                return;
            }
            if (targetLayer == null) {
                int choice = JOptionPane.showConfirmDialog(
                        panel,
                        "No hay una capa vectorial editable compatible para este dibujo.\n\n\u00BFQuer\u00E9s crearla ahora?",
                        "Crear capa destino",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (choice != JOptionPane.YES_OPTION) {
                    panel.showCopiedMessage("Seleccion\u00E1 o cre\u00E1 una capa compatible para guardar el dibujo.");
                    return;
                }

                targetLayer = NewVectorLayerAction.createNewVectorLayer(resolveDrawingGeometryFamily(panel.drawingToolManager.drawingMode), panel);
                if (targetLayer == null) {
                    panel.showCopiedMessage("El dibujo sigue activo hasta que completes la capa destino o lo canceles.");
                    return;
                }
            }

            if (!panel.drawingToolManager.pendingDrawingSessionGeometries.isEmpty()) {
                if (!appendGeometriesToLayer(
                        targetLayer,
                        new ArrayList<>(panel.drawingToolManager.pendingDrawingSessionGeometries),
                        panel.drawingToolManager.pendingDrawingSessionGeometries.size() == 1
                                ? "Entidad pendiente agregada a la capa."
                                : panel.drawingToolManager.pendingDrawingSessionGeometries.size() + " entidades pendientes agregadas a la capa."
                )) {
                    return;
                }
                panel.drawingToolManager.pendingDrawingSessionGeometries.clear();
            }

            if (!appendCurrentDrawingToLayer(targetLayer)) {
                return;
            }

            panel.drawingToolManager.drawingSessionLayer = targetLayer;
            panel.drawingToolManager.drawingSessionDirty = true;
            panel.drawingToolManager.drawingCoordinates.clear();
            EventBus.emit(EventBus.EventType.TOOLBAR_SYNC);
            panel.repaint();
        } catch (Exception ex) {
            AppErrorSupport.logFailure("Error al agregar la geometria dibujada a la capa", ex);
            AppErrorSupport.showErrorDialog(panel, "Dibujo vectorial", "Error al agregar la geometria a la capa.", ex);
        }
    }

    public void closeCurrentDrawingSession() {
        if (!panel.isDrawingActive()) {
            return;
        }

        boolean hasPendingCurrentGeometry = !panel.drawingToolManager.drawingCoordinates.isEmpty();
        if ("CONTINUE_LINE".equalsIgnoreCase(panel.drawingToolManager.drawingMode)) {
            hasPendingCurrentGeometry = panel.drawingToolManager.drawingContinuationEndpointChosen
                    && panel.drawingToolManager.drawingContinuationBaseCoordinates != null
                    && panel.drawingToolManager.drawingCoordinates.size() > panel.drawingToolManager.drawingContinuationBaseCoordinates.length;
        }

        if (hasPendingCurrentGeometry) {
            int closeCurrent = JOptionPane.showConfirmDialog(
                    panel,
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
                if (!panel.drawingToolManager.drawingCoordinates.isEmpty()) {
                    return;
                }
            } else {
                panel.drawingToolManager.drawingCoordinates.clear();
            }
        }

        Layer layerToSave = panel.drawingToolManager.drawingSessionLayer != null ? panel.drawingToolManager.drawingSessionLayer : resolveDrawingTargetLayer();
        if (!panel.drawingToolManager.pendingDrawingSessionGeometries.isEmpty() && layerToSave == null) {
            int choice = JOptionPane.showConfirmDialog(
                    panel,
                    "No hay una capa vectorial compatible todavia.\n\nQueres crearla ahora para guardar las entidades dibujadas?",
                    "Crear capa destino",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
            layerToSave = NewVectorLayerAction.createNewVectorLayer(resolveDrawingGeometryFamily(panel.drawingToolManager.drawingMode), panel);
            if (layerToSave == null) {
                return;
            }
        }

        if (!panel.drawingToolManager.pendingDrawingSessionGeometries.isEmpty()) {
            if (!appendGeometriesToLayer(
                    layerToSave,
                    new ArrayList<>(panel.drawingToolManager.pendingDrawingSessionGeometries),
                    panel.drawingToolManager.pendingDrawingSessionGeometries.size() == 1
                            ? "Entidad de la sesion agregada a la capa."
                            : panel.drawingToolManager.pendingDrawingSessionGeometries.size() + " entidades de la sesion agregadas a la capa."
            )) {
                return;
            }
            panel.drawingToolManager.drawingSessionLayer = layerToSave;
            panel.drawingToolManager.pendingDrawingSessionGeometries.clear();
        }

        if (panel.drawingToolManager.drawingSessionDirty && layerToSave != null) {
            int saveChoice = JOptionPane.showConfirmDialog(
                    panel,
                    "Queres guardar ahora la capa vectorial?\n\n" + layerToSave.getName(),
                    "Guardar capa vectorial",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (saveChoice == JOptionPane.CANCEL_OPTION || saveChoice == JOptionPane.CLOSED_OPTION) {
                return;
            }
            if (saveChoice == JOptionPane.YES_OPTION && !saveVectorLayerNow(layerToSave)) {
                return;
            }
        }

        cancelCurrentDrawing();
        panel.showCopiedMessage("Sesion de dibujo cerrada.");
    }

    private boolean saveVectorLayerNow(Layer layer) {
        if (layer == null) {
            return true;
        }

        ShapefileData data = panel.getShapefileData(layer);
        if (!ExportVectorLayerAction.hasExportableVectorData(data)) {
            return true;
        }

        if (ExportVectorLayerAction.hasSupportedVectorPath(layer)) {
            return ExportVectorLayerAction.saveLayerToCurrentPath(layer, panel, false);
        }

        File exported = ExportVectorLayerAction.exportLayerWithDialog(
                layer,
                data,
                panel,
                "Guardar capa vectorial",
                false
        );
        return exported != null;
    }

    private Layer resolveDrawingTargetLayer() {
        if (isCompatibleDrawingTarget(panel.drawingToolManager.drawingSessionLayer, panel.drawingToolManager.drawingMode)) {
            return panel.drawingToolManager.drawingSessionLayer;
        }

        if (isCompatibleDrawingTarget(panel.activeVectorEditingLayer, panel.drawingToolManager.drawingMode)) {
            return panel.activeVectorEditingLayer;
        }

        if (panel.selectedLayer != null && isCompatibleDrawingTarget(panel.selectedLayer, panel.drawingToolManager.drawingMode)) {
            return panel.selectedLayer;
        }

        return null;
    }

    private boolean isCompatibleDrawingTarget(Layer layer, String mode) {
        if (layer == null || layer instanceof RasterLayer || panel.isReadOnlyVectorLayer(layer)) {
            return false;
        }

        ShapefileData data = panel.getShapefileData(layer);
        if (data == null || data.getSchema() == null) {
            return false;
        }

        String drawingFamily = resolveDrawingGeometryFamily(mode);
        String layerFamily = resolveLayerGeometryFamily(data.getSchema());
        return !drawingFamily.isBlank() && drawingFamily.equalsIgnoreCase(layerFamily);
    }

    private String resolveDrawingGeometryFamily(String mode) {
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

    private String resolveLayerGeometryFamily(SimpleFeatureType schema) {
        if (schema == null || schema.getGeometryDescriptor() == null || schema.getGeometryDescriptor().getType() == null) {
            return "";
        }
        return DrawFeatureBuilder.resolveGeometryFamily(schema.getGeometryDescriptor().getType().getBinding());
    }

    private boolean appendCurrentDrawingToLayer(Layer layer) {
        if (layer == null) {
            return false;
        }

        ShapefileData targetData = panel.getShapefileData(layer);
        if (targetData == null || targetData.getSchema() == null) {
            JOptionPane.showMessageDialog(panel, "La capa destino no tiene esquema vectorial disponible.");
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

        ShapefileData targetData = panel.getShapefileData(layer);
        if (targetData == null || targetData.getSchema() == null) {
            JOptionPane.showMessageDialog(panel, "La capa destino no tiene esquema vectorial disponible.");
            return false;
        }

        panel.pushUndoSnapshot(layer, null);

        List<SimpleFeature> features = new ArrayList<>(targetData.getFeatures());
        List<String> createdIds = new ArrayList<>();
        for (Geometry geometry : newGeometries) {
            SimpleFeature createdFeature = panel.buildNewFeatureForLayer(targetData, geometry, features);
            if (createdFeature == null) {
                continue;
            }
            features.add(createdFeature);
            createdIds.add(createdFeature.getID());
        }

        if (createdIds.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "No se pudo crear la entidad en la capa seleccionada.");
            return false;
        }

        panel.replaceLayerFeatures(layer, features, createdIds.size() == 1 ? createdIds.get(0) : null, false, null);
        panel.applyFeatureSelection(
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

        if ("POINT".equalsIgnoreCase(panel.drawingToolManager.drawingMode) || "MULTIPOINT".equalsIgnoreCase(panel.drawingToolManager.drawingMode)) {
            if (panel.drawingToolManager.drawingCoordinates.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Para crear puntos necesit\u00E1s hacer clic en el mapa.");
                return geometries;
            }

            GeometryFactory gf = new GeometryFactory();
            if (geometryBinding != null && MultiPoint.class.isAssignableFrom(geometryBinding)) {
                Point[] points = new Point[panel.drawingToolManager.drawingCoordinates.size()];
                for (int i = 0; i < panel.drawingToolManager.drawingCoordinates.size(); i++) {
                    points[i] = gf.createPoint(new Coordinate(panel.drawingToolManager.drawingCoordinates.get(i)));
                }
                geometries.add(gf.createMultiPoint(points));
            } else {
                for (Coordinate coordinate : panel.drawingToolManager.drawingCoordinates) {
                    geometries.add(DrawFeatureBuilder.buildPoint(coordinate));
                }
            }
            return geometries;
        }

        if ("LINE".equalsIgnoreCase(panel.drawingToolManager.drawingMode)) {
            Geometry geometry = DrawFeatureBuilder.buildLine(panel.drawingToolManager.drawingCoordinates);
            if (geometry == null) {
                JOptionPane.showMessageDialog(panel, "Para una l\u00EDnea necesit\u00E1s al menos 2 v\u00E9rtices.");
                return geometries;
            }
            geometries.add(geometry);
            return geometries;
        }

        if ("RECTANGLE".equalsIgnoreCase(panel.drawingToolManager.drawingMode)) {
            Geometry geometry = buildRectangleGeometry(panel.drawingToolManager.drawingCoordinates);
            if (geometry == null) {
                JOptionPane.showMessageDialog(panel, "Para un rect\u00E1ngulo necesit\u00E1s marcar dos esquinas opuestas.");
                return geometries;
            }
            geometries.add(geometry);
            return geometries;
        }

        if ("POLYGON".equalsIgnoreCase(panel.drawingToolManager.drawingMode)) {
            Geometry geometry = DrawFeatureBuilder.buildPolygon(panel.drawingToolManager.drawingCoordinates);
            if (geometry == null) {
                JOptionPane.showMessageDialog(panel, "Para un pol\u00EDgono necesit\u00E1s al menos 3 v\u00E9rtices.");
                return geometries;
            }
            geometries.add(geometry);
        }

        if ("CIRCLE".equalsIgnoreCase(panel.drawingToolManager.drawingMode)) {
            Geometry geometry = buildCircleGeometry(panel.drawingToolManager.drawingCoordinates);
            if (geometry == null) {
                JOptionPane.showMessageDialog(panel, "Para un circulo necesitas marcar centro y radio.");
                return geometries;
            }
            geometries.add(geometry);
            return geometries;
        }

        if ("CIRCLE_3P".equalsIgnoreCase(panel.drawingToolManager.drawingMode)) {
            Geometry geometry = buildCircleThreePointGeometry(panel.drawingToolManager.drawingCoordinates);
            if (geometry == null) {
                JOptionPane.showMessageDialog(panel, "No se pudo construir el circulo con esos tres puntos.");
                return geometries;
            }
            geometries.add(geometry);
            return geometries;
        }

        return geometries;
    }

    void chooseContinuationEndpoint(int screenX, int screenY) {
        if (panel.drawingToolManager.drawingContinuationBaseCoordinates == null || panel.drawingToolManager.drawingContinuationBaseCoordinates.length < 2 || panel.drawingToolManager.drawingContinuationLayer == null) {
            panel.showCopiedMessage("No se encontro una linea base valida para continuar.");
            return;
        }

        Coordinate start = panel.toProjectCoordinate(panel.drawingToolManager.drawingContinuationBaseCoordinates[0], panel.drawingToolManager.drawingContinuationLayer);
        Coordinate end = panel.toProjectCoordinate(
                panel.drawingToolManager.drawingContinuationBaseCoordinates[panel.drawingToolManager.drawingContinuationBaseCoordinates.length - 1],
                panel.drawingToolManager.drawingContinuationLayer
        );
        if (start == null || end == null) {
            panel.showCopiedMessage("No se pudieron ubicar los extremos de la linea seleccionada.");
            return;
        }

        int startX = panel.worldToScreenX(start.x);
        int startY = panel.worldToScreenY(start.y);
        int endX = panel.worldToScreenX(end.x);
        int endY = panel.worldToScreenY(end.y);
        double startDistance = Math.hypot(screenX - startX, screenY - startY);
        double endDistance = Math.hypot(screenX - endX, screenY - endY);
        double tolerancePx = Math.max(MapPanel.EDIT_VERTEX_TOLERANCE_PX + 6, 16);

        if (startDistance > tolerancePx && endDistance > tolerancePx) {
            panel.showCopiedMessage("Hace clic sobre uno de los extremos resaltados para indicar desde donde continuar.");
            return;
        }

        panel.drawingToolManager.drawingCoordinates.clear();
        panel.drawingToolManager.drawingContinuationFromStart = startDistance <= endDistance;
        Coordinate[] oriented = panel.drawingToolManager.drawingContinuationFromStart
                ? panel.reverseCoordinates(panel.drawingToolManager.drawingContinuationBaseCoordinates)
                : panel.cloneCoordinates(panel.drawingToolManager.drawingContinuationBaseCoordinates);
        for (Coordinate coordinate : oriented) {
            if (coordinate != null) {
                panel.drawingToolManager.drawingCoordinates.add(new Coordinate(coordinate));
            }
        }
        panel.drawingToolManager.drawingContinuationEndpointChosen = true;
        panel.showCopiedMessage(panel.drawingToolManager.drawingContinuationFromStart
                ? "Extremo inicial seleccionado. Ahora agrega los nuevos vertices y termina con doble clic."
                : "Extremo final seleccionado. Ahora agrega los nuevos vertices y termina con doble clic.");
        panel.repaint();
    }

    private Geometry buildContinuationLineGeometry() {
        if (panel.drawingToolManager.drawingContinuationBaseCoordinates == null
                || !panel.drawingToolManager.drawingContinuationEndpointChosen
                || panel.drawingToolManager.drawingCoordinates.size() <= panel.drawingToolManager.drawingContinuationBaseCoordinates.length
                || panel.drawingToolManager.drawingContinuationLayer == null
                || panel.drawingToolManager.drawingContinuationFeatureId == null) {
            return null;
        }

        if (panel.selectedLayer != panel.drawingToolManager.drawingContinuationLayer || !panel.sameFeatureId(panel.selectedFeature, panel.drawingToolManager.drawingContinuationFeatureId)) {
            ShapefileData data = panel.getShapefileData(panel.drawingToolManager.drawingContinuationLayer);
            panel.selectedLayer = panel.drawingToolManager.drawingContinuationLayer;
            panel.selectedFeature = data != null ? panel.findFeatureById(data.getFeatures(), panel.drawingToolManager.drawingContinuationFeatureId) : null;
        }

        if (panel.selectedFeature == null) {
            return null;
        }

        Coordinate[] oriented = panel.cloneCoordinates(panel.drawingToolManager.drawingCoordinates.toArray(new Coordinate[0]));
        if (panel.drawingToolManager.drawingContinuationFromStart) {
            oriented = panel.reverseCoordinates(oriented);
        }
        oriented = panel.collapseDuplicateLineCoordinates(oriented);
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

    private Geometry buildRectangleGeometry(List<Coordinate> coordinates) {
        List<Coordinate> rectangleCoordinates = buildRectangleCoordinates(coordinates);
        if (rectangleCoordinates.isEmpty()) {
            return null;
        }
        return DrawFeatureBuilder.buildPolygon(rectangleCoordinates);
    }

    public Geometry buildCircleGeometry(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.size() < 2) {
            return null;
        }
        Coordinate center = coordinates.get(0);
        Coordinate radiusPoint = coordinates.get(coordinates.size() - 1);
        if (center == null || radiusPoint == null) {
            return null;
        }
        double radius = center.distance(radiusPoint);
        if (!(radius > 0.0)) {
            return null;
        }
        return buildCirclePolygon(center, radius);
    }

    public Geometry buildCircleThreePointGeometry(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.size() < 3) {
            return null;
        }
        Coordinate a = coordinates.get(0);
        Coordinate b = coordinates.get(1);
        Coordinate c = coordinates.get(2);
        Coordinate center = computeCircumcenter(a, b, c);
        if (center == null) {
            return null;
        }
        double radius = center.distance(a);
        if (!(radius > 0.0)) {
            return null;
        }
        return buildCirclePolygon(center, radius);
    }

    private Geometry buildCirclePolygon(Coordinate center, double radius) {
        if (center == null || !(radius > 0.0)) {
            return null;
        }
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] shell = new Coordinate[MapPanel.CIRCLE_SEGMENTS + 1];
        for (int i = 0; i < MapPanel.CIRCLE_SEGMENTS; i++) {
            double angle = (Math.PI * 2.0 * i) / MapPanel.CIRCLE_SEGMENTS;
            shell[i] = new Coordinate(
                    center.x + (Math.cos(angle) * radius),
                    center.y + (Math.sin(angle) * radius)
            );
        }
        shell[MapPanel.CIRCLE_SEGMENTS] = new Coordinate(shell[0]);
        return factory.createPolygon(factory.createLinearRing(shell), null);
    }

    private Coordinate computeCircumcenter(Coordinate a, Coordinate b, Coordinate c) {
        if (a == null || b == null || c == null) {
            return null;
        }

        double d = (2.0 * ((a.x * (b.y - c.y)) + (b.x * (c.y - a.y)) + (c.x * (a.y - b.y))));
        if (Math.abs(d) < 0.0000001) {
            return null;
        }

        double ax2ay2 = (a.x * a.x) + (a.y * a.y);
        double bx2by2 = (b.x * b.x) + (b.y * b.y);
        double cx2cy2 = (c.x * c.x) + (c.y * c.y);

        double ux = ((ax2ay2 * (b.y - c.y)) + (bx2by2 * (c.y - a.y)) + (cx2cy2 * (a.y - b.y))) / d;
        double uy = ((ax2ay2 * (c.x - b.x)) + (bx2by2 * (a.x - c.x)) + (cx2cy2 * (b.x - a.x))) / d;
        return new Coordinate(ux, uy);
    }

    public List<Coordinate> buildRectangleCoordinates(List<Coordinate> coordinates) {
        List<Coordinate> rectangle = new ArrayList<>();
        if (coordinates == null || coordinates.size() < 2) {
            return rectangle;
        }

        Coordinate first = coordinates.get(0);
        Coordinate opposite = coordinates.get(coordinates.size() - 1);
        if (first == null || opposite == null) {
            return rectangle;
        }
        if (Math.abs(first.x - opposite.x) < 0.0000001 || Math.abs(first.y - opposite.y) < 0.0000001) {
            return rectangle;
        }

        rectangle.add(new Coordinate(first.x, first.y));
        rectangle.add(new Coordinate(opposite.x, first.y));
        rectangle.add(new Coordinate(opposite.x, opposite.y));
        rectangle.add(new Coordinate(first.x, opposite.y));
        rectangle.add(new Coordinate(first.x, first.y));
        return rectangle;
    }
}
