package ar.com.catgis;

import ar.com.catgis.FeatureBuilder;
import ar.com.catgis.MapGeometryUtils;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import java.awt.Rectangle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

class MapEditingEngine {

    private final MapPanel map;

    MapEditingEngine(MapPanel map) {
        this.map = map;
    }

    public void activateMoveVertexMode() {
        if (!map.featureEditMode) {
            return;
        }
        if (!map.isSelectedFeatureLinearOrPolygonal()) {
            NotificationManager.warn(map, null, "Mover vÃ©rtices sÃ³lo funciona sobre lÃ­neas o polÃ­gonos.");
            return;
        }
        map.featureEditOperation = MapPanel.EDIT_OP_MOVE_VERTEX;
        map.featureEditSketchCoordinates.clear();
        map.joinTargetVertexIndex = -1;
        map.clearAdjacentPolygonState();
        map.clearCadConstructionState();
        map.setTool("SELECT");
        map.showCopiedMessage("Modo mover vÃ©rtice activo.");
        map.refreshEditingUi();
    }

    public void activateMoveFeatureMode() {
        if (!map.hasFeatureSelection()) {
            return;
        }
        if (map.isReadOnlyVectorLayer(map.selectedLayer)) {
            NotificationManager.warn(map, null, map.getReadOnlyLayerMessage(map.selectedLayer));
            return;
        }
        map.featureEditOperation = MapPanel.EDIT_OP_MOVE_FEATURE;
        map.featureEditSketchCoordinates.clear();
        map.activeEditVertexIndex = -1;
        map.joinTargetVertexIndex = -1;
        map.clearAdjacentPolygonState();
        map.clearCadConstructionState();
        map.setTool("SELECT");
        map.showCopiedMessage("Modo mover elementos activo. ArrastrÃ¡ una entidad seleccionada.");
        map.refreshEditingUi();
    }

    public void activateAddVertexMode() {
        if (!map.featureEditMode) {
            return;
        }
        if (!map.isSelectedFeatureLinearOrPolygonal()) {
            NotificationManager.warn(map, null, "Agregar vÃ©rtices sÃ³lo funciona sobre lÃ­neas o polÃ­gonos.");
            return;
        }
        map.featureEditOperation = MapPanel.EDIT_OP_ADD_VERTEX;
        map.featureEditSketchCoordinates.clear();
        map.joinTargetVertexIndex = -1;
        map.clearAdjacentPolygonState();
        map.clearCadConstructionState();
        map.setTool("SELECT");
        map.showCopiedMessage("Modo agregar vÃ©rtice activo. HacÃ© clic o arrastrÃ¡ una caja sobre un tramo.");
        map.refreshEditingUi();
    }

    public void activateRemoveVertexMode() {
        if (!map.featureEditMode) {
            return;
        }
        if (!map.isSelectedFeatureLinearOrPolygonal()) {
            NotificationManager.warn(map, null, "Eliminar vÃ©rtices sÃ³lo funciona sobre lÃ­neas o polÃ­gonos.");
            return;
        }
        map.featureEditOperation = MapPanel.EDIT_OP_REMOVE_VERTEX;
        map.featureEditSketchCoordinates.clear();
        map.joinTargetVertexIndex = -1;
        map.clearAdjacentPolygonState();
        map.clearCadConstructionState();
        map.setTool("SELECT");
        map.showCopiedMessage("Modo eliminar vÃ©rtice activo. HacÃ© clic o arrastrÃ¡ una caja para quitar uno o varios vÃ©rtices.");
        map.refreshEditingUi();
    }

    public void activateJoinVerticesMode() {
        if (!map.featureEditMode) {
            return;
        }
        if (!map.isSelectedFeatureLinearOrPolygonal()) {
            NotificationManager.warn(map, null, "Unir vÃ©rtices sÃ³lo funciona sobre lÃ­neas o polÃ­gonos.");
            return;
        }
        map.featureEditOperation = MapPanel.EDIT_OP_JOIN_VERTEX;
        map.featureEditSketchCoordinates.clear();
        map.activeEditVertexIndex = -1;
        map.joinTargetVertexIndex = -1;
        map.clearAdjacentPolygonState();
        map.clearCadConstructionState();
        map.setTool("SELECT");
        map.showCopiedMessage("Modo unir vÃ©rtices activo. ElegÃ­ un vÃ©rtice base y despuÃ©s otro vÃ©rtice o un rectÃ¡ngulo.");
        map.refreshEditingUi();
    }

    public void activateCutFeatureMode() {
        if (!map.featureEditMode) {
            return;
        }
        if (!map.isSelectedFeatureLinearOrPolygonal()) {
            NotificationManager.warn(map, null, "Cortar geometrÃ­a sÃ³lo funciona sobre lÃ­neas o polÃ­gonos.");
            return;
        }
        map.featureEditOperation = MapPanel.EDIT_OP_CUT;
        map.featureEditSketchCoordinates.clear();
        map.joinTargetVertexIndex = -1;
        map.clearAdjacentPolygonState();
        map.clearCadConstructionState();
        map.setTool("SELECT");
        String hint = map.isSelectedFeaturePolygonal()
                ? "Modo cortar activo. DibujÃ¡ la lÃ­nea de corte y terminÃ¡ con doble clic."
                : "Modo cortar activo. HacÃ© clic sobre la lÃ­nea en el punto donde querÃ©s cortarla.";
        map.showCopiedMessage(hint);
        map.refreshEditingUi();
    }

    public void activateHoleMode() {
        if (!map.featureEditMode) {
            return;
        }
        if (!map.isSelectedFeaturePolygonal()) {
            NotificationManager.warn(map, null, "La opciÃ³n agujero solo funciona sobre polÃ­gonos.");
            return;
        }
        map.featureEditOperation = MapPanel.EDIT_OP_HOLE;
        map.featureEditSketchCoordinates.clear();
        map.joinTargetVertexIndex = -1;
        map.clearAdjacentPolygonState();
        map.clearCadConstructionState();
        map.setTool("SELECT");
        map.showCopiedMessage("Modo agujero activo. DibujÃ¡ el polÃ­gono interior y terminÃ¡ con doble clic.");
        map.refreshEditingUi();
    }

    public void activateAdjacentPolygonMode() {
        if (!map.featureEditMode) {
            return;
        }
        if (!map.isSelectedFeaturePolygonal()) {
            NotificationManager.warn(map, null, "Poligono adyacente solo funciona sobre poligonos.");
            return;
        }
        map.featureEditOperation = MapPanel.EDIT_OP_ADJACENT_POLYGON;
        map.featureEditSketchCoordinates.clear();
        map.activeEditVertexIndex = -1;
        map.joinTargetVertexIndex = -1;
        map.clearAdjacentPolygonState();
        map.clearCadConstructionState();
        map.setTool("SELECT");
        map.showCopiedMessage("Modo poligono adyacente activo. Elegi un borde del poligono y despues un punto exterior para definir el nuevo lateral.");
        map.refreshEditingUi();
    }

    public void activateExtendLineMode() {
        if (!map.ensureSelectedLineReadyForCad("extender la linea")) {
            return;
        }
        map.featureEditOperation = MapPanel.EDIT_OP_EXTEND_LINE;
        map.featureEditSketchCoordinates.clear();
        map.activeEditVertexIndex = -1;
        map.joinTargetVertexIndex = -1;
        map.clearAdjacentPolygonState();
        map.clearCadConstructionState();
        map.setTool("SELECT");
        map.showCopiedMessage("Modo extender linea activo. Hace clic sobre un extremo y despues marca hasta donde extender.");
        map.refreshEditingUi();
    }

    public void activateShortenLineMode() {
        if (!map.ensureSelectedLineReadyForCad("acortar la linea")) {
            return;
        }
        map.featureEditOperation = MapPanel.EDIT_OP_SHORTEN_LINE;
        map.featureEditSketchCoordinates.clear();
        map.activeEditVertexIndex = -1;
        map.joinTargetVertexIndex = -1;
        map.clearAdjacentPolygonState();
        map.clearCadConstructionState();
        map.setTool("SELECT");
        map.showCopiedMessage("Modo acortar linea activo. Hace clic sobre un extremo y despues marca el nuevo fin.");
        map.refreshEditingUi();
    }

    public void activateParallelLineMode() {
        if (!map.ensureSelectedLineReadyForCad("crear una paralela")) {
            return;
        }
        if (resolveCadLineTargetLayer() == null) {
            NotificationManager.warn(map, null, "Necesitas una capa de lineas compatible para guardar la paralela.");
            return;
        }
        map.featureEditOperation = MapPanel.EDIT_OP_PARALLEL;
        map.featureEditSketchCoordinates.clear();
        map.activeEditVertexIndex = -1;
        map.joinTargetVertexIndex = -1;
        map.clearAdjacentPolygonState();
        map.clearCadConstructionState();
        map.setTool("SELECT");
        map.showCopiedMessage("Modo paralela activo. Elegi un tramo base y despues marca el desplazamiento lateral.");
        map.refreshEditingUi();
    }

    public void activatePerpendicularLineMode() {
        if (!map.ensureSelectedLineReadyForCad("crear una perpendicular")) {
            return;
        }
        if (resolveCadLineTargetLayer() == null) {
            NotificationManager.warn(map, null, "Necesitas una capa de lineas compatible para guardar la perpendicular.");
            return;
        }
        map.featureEditOperation = MapPanel.EDIT_OP_PERPENDICULAR;
        map.featureEditSketchCoordinates.clear();
        map.activeEditVertexIndex = -1;
        map.joinTargetVertexIndex = -1;
        map.clearAdjacentPolygonState();
        map.clearCadConstructionState();
        map.setTool("SELECT");
        map.showCopiedMessage("Modo perpendicular activo. Elegi un tramo base y despues define la perpendicular con un segundo clic.");
        map.refreshEditingUi();
    }

    boolean handleFeatureEditClick(java.awt.event.MouseEvent e) {
        if (map.selectedFeature == null || map.selectedLayer == null) {
            return false;
        }

        if (MapPanel.EDIT_OP_ADD_VERTEX.equals(map.featureEditOperation)) {
            return addVertexToSelectedGeometry(e.getX(), e.getY());
        }

        if (MapPanel.EDIT_OP_REMOVE_VERTEX.equals(map.featureEditOperation)) {
            return removeVertexFromSelectedGeometry(e.getX(), e.getY());
        }

        if (MapPanel.EDIT_OP_JOIN_VERTEX.equals(map.featureEditOperation)) {
            return joinVerticesFromClick(e.getX(), e.getY());
        }

        if (MapPanel.EDIT_OP_ADJACENT_POLYGON.equals(map.featureEditOperation)) {
            return handleAdjacentPolygonClick(e.getX(), e.getY());
        }

        if (MapPanel.EDIT_OP_EXTEND_LINE.equals(map.featureEditOperation)) {
            return handleExtendOrShortenLineClick(e.getX(), e.getY(), true);
        }

        if (MapPanel.EDIT_OP_SHORTEN_LINE.equals(map.featureEditOperation)) {
            return handleExtendOrShortenLineClick(e.getX(), e.getY(), false);
        }

        if (MapPanel.EDIT_OP_PARALLEL.equals(map.featureEditOperation)) {
            return handleParallelLineClick(e.getX(), e.getY());
        }

        if (MapPanel.EDIT_OP_PERPENDICULAR.equals(map.featureEditOperation)) {
            return handlePerpendicularLineClick(e.getX(), e.getY());
        }

        if (MapPanel.EDIT_OP_CUT.equals(map.featureEditOperation)) {
            if (map.isSelectedFeaturePolygonal()) {
                map.featureEditSketchCoordinates.add(map.resolveInteractiveCoordinate(e.getX(), e.getY(), false));
                if (e.getClickCount() >= 2 && map.featureEditSketchCoordinates.size() >= 2) {
                    applyFeatureEditSketchOperationEnhanced();
                } else {
                    map.repaint();
                }
                return true;
            }
            return cutSelectedGeometryAtClick(e.getX(), e.getY());
        }

        if (MapPanel.EDIT_OP_HOLE.equals(map.featureEditOperation)) {
            map.featureEditSketchCoordinates.add(map.resolveInteractiveCoordinate(e.getX(), e.getY(), false));
            if (e.getClickCount() >= 2 && map.featureEditSketchCoordinates.size() >= 3) {
                applyFeatureEditSketchOperationEnhanced();
            } else {
                map.repaint();
            }
            return true;
        }

        return false;
    }

    boolean addVertexToSelectedGeometry(int screenX, int screenY) {
        Object geomObj = map.selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return false;
        }

        Geometry displayGeometry = map.getEditableDisplayGeometry(map.selectedFeature, map.selectedLayer);
        Coordinate displayTarget = new Coordinate(map.screenToWorldX(screenX), map.screenToWorldY(screenY));
        LineSplitProjection projection = map.findEditableSegmentProjection(displayGeometry, displayTarget, screenX, screenY, MapPanel.EDIT_SEGMENT_TOLERANCE_PX);
        if (projection == null || projection.segmentIndex < 0 || projection.projected == null) {
            map.showCopiedMessage("No se encontrÃ³ un tramo cercano para agregar el vÃ©rtice.");
            return true;
        }

        Coordinate sourceCoordinate = map.toSourceCoordinate(projection.projected.x, projection.projected.y, map.selectedLayer);
        Geometry updated = buildGeometryWithAddedVertex((Geometry) geomObj, projection.segmentIndex, sourceCoordinate);
        if (updated == null) {
            map.showCopiedMessage("No se pudo agregar el vÃ©rtice en esa geometrÃ­a.");
            return true;
        }

        updateSelectedFeatureGeometry(updated, "VÃ©rtice agregado.");
        return true;
    }

    boolean removeVertexFromSelectedGeometry(int screenX, int screenY) {
        Object geomObj = map.selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return false;
        }

        int vertexIndex = findEditableVertexIndex(screenX, screenY);
        if (vertexIndex < 0) {
            map.showCopiedMessage("No se encontrÃ³ un vÃ©rtice cercano para eliminar.");
            return true;
        }

        Geometry updated = buildGeometryWithRemovedVertex((Geometry) geomObj, vertexIndex);
        if (updated == null) {
            map.showCopiedMessage("No se pudo eliminar ese vÃ©rtice.");
            return true;
        }

        updateSelectedFeatureGeometry(updated, "VÃ©rtice eliminado.");
        return true;
    }

    boolean removeVerticesFromSelectedGeometry(Rectangle selectionBounds) {
        if (selectionBounds == null || map.selectedFeature == null) {
            return false;
        }

        Object geomObj = map.selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry geometry)) {
            return false;
        }

        List<Integer> vertexIndexes = collectEditableVertexIndexes(selectionBounds);
        if (vertexIndexes.isEmpty()) {
            map.showCopiedMessage("No se encontrÃ³ ningÃºn vÃ©rtice dentro del rectÃ¡ngulo.");
            return true;
        }

        Geometry updated = buildGeometryWithRemovedVertices(geometry, vertexIndexes);
        if (updated == null) {
            map.showCopiedMessage("No se pudieron eliminar esos vÃ©rtices.");
            return true;
        }

        String message = vertexIndexes.size() == 1
                ? "VÃ©rtice eliminado."
                : vertexIndexes.size() + " vÃ©rtices eliminados.";
        updateSelectedFeatureGeometry(updated, message);
        return true;
    }

    boolean joinVerticesFromClick(int screenX, int screenY) {
        if (map.selectedFeature == null || map.selectedLayer == null) {
            return false;
        }

        int vertexIndex = findEditableVertexIndex(screenX, screenY);
        if (vertexIndex < 0) {
            map.showCopiedMessage("No se encontrÃ³ un vÃ©rtice cercano para unir.");
            return true;
        }

        if (map.joinTargetVertexIndex < 0 || map.joinTargetVertexIndex == vertexIndex) {
            map.joinTargetVertexIndex = vertexIndex;
            map.showCopiedMessage("VÃ©rtice base seleccionado. ElegÃ­ otro vÃ©rtice o arrastrÃ¡ un rectÃ¡ngulo para unirlos.");
            map.repaint();
            return true;
        }

        List<Integer> vertexIndexes = new ArrayList<>();
        vertexIndexes.add(vertexIndex);
        return joinVerticesIntoTarget(map.joinTargetVertexIndex, vertexIndexes, false);
    }

    boolean joinVerticesFromSelection(Rectangle selectionBounds) {
        if (selectionBounds == null || map.selectedFeature == null) {
            return false;
        }

        List<Integer> vertexIndexes = collectEditableVertexIndexes(selectionBounds);
        if (vertexIndexes.isEmpty()) {
            map.showCopiedMessage("No se encontrÃ³ ningÃºn vÃ©rtice dentro del rectÃ¡ngulo.");
            return true;
        }

        int targetIndex = map.joinTargetVertexIndex;
        if (targetIndex < 0) {
            targetIndex = vertexIndexes.remove(0);
            if (vertexIndexes.isEmpty()) {
                map.joinTargetVertexIndex = targetIndex;
                map.showCopiedMessage("VÃ©rtice base seleccionado. Ahora marcÃ¡ otros vÃ©rtices para unirlos.");
                map.repaint();
                return true;
            }
        } else {
            vertexIndexes.remove(Integer.valueOf(targetIndex));
        }

        return joinVerticesIntoTarget(targetIndex, vertexIndexes, true);
    }

    boolean joinVerticesIntoTarget(int targetIndex, List<Integer> vertexIndexes, boolean fromSelection) {
        Object geomObj = map.selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry geometry)) {
            return false;
        }

        if (vertexIndexes == null || vertexIndexes.isEmpty()) {
            map.showCopiedMessage(fromSelection
                    ? "MarcÃ¡ al menos otro vÃ©rtice para unirlo al vÃ©rtice base."
                    : "ElegÃ­ otro vÃ©rtice distinto del vÃ©rtice base.");
            return true;
        }

        Geometry updated = buildGeometryWithJoinedVertices(geometry, targetIndex, vertexIndexes);
        if (updated == null) {
            map.showCopiedMessage("No se pudieron unir esos vÃ©rtices. ProbÃ¡ dentro del mismo tramo o polÃ­gono.");
            return true;
        }

        map.joinTargetVertexIndex = -1;
        updateSelectedFeatureGeometry(updated, vertexIndexes.size() == 1 ? "VÃ©rtices unidos." : "VÃ©rtices unidos al vÃ©rtice base.");
        return true;
    }

    boolean handleExtendOrShortenLineClick(int screenX, int screenY, boolean extend) {
        if (!map.isSelectedFeatureLinear()) {
            map.showCopiedMessage("La herramienta solo funciona sobre lineas.");
            return true;
        }

        Geometry geometry = map.extractFeatureGeometryCopy(map.selectedFeature);
        Coordinate[] baseCoordinates = map.extractContinuableLineCoordinates(geometry);
        if (baseCoordinates == null || baseCoordinates.length < 2) {
            map.showCopiedMessage("La linea seleccionada no es continua o no se puede editar con esta herramienta.");
            return true;
        }

        if (!map.cadReferenceEndpointChosen) {
            chooseCadReferenceEndpoint(screenX, screenY, baseCoordinates);
            return true;
        }

        Coordinate targetCoordinate = map.resolveInteractiveCoordinate(screenX, screenY, false);
        Coordinate sourceCoordinate = map.toSourceCoordinate(targetCoordinate.x, targetCoordinate.y, map.selectedLayer);
        Geometry updated = map.editingGeomOps.buildAdjustedSelectedLineGeometry(geometry, sourceCoordinate, extend, map.cadReferenceFromStart);
        if (updated == null) {
            map.showCopiedMessage(extend
                    ? "El punto elegido no permite extender la linea en ese sentido."
                    : "El punto elegido no permite acortar la linea sin volverla invalida.");
            return true;
        }

        map.clearCadConstructionState();
        updateSelectedFeatureGeometry(updated, extend ? "Linea extendida." : "Linea acortada.");
        return true;
    }

    void chooseCadReferenceEndpoint(int screenX, int screenY, Coordinate[] baseCoordinates) {
        Coordinate start = map.toProjectCoordinate(baseCoordinates[0], map.selectedLayer);
        Coordinate end = map.toProjectCoordinate(baseCoordinates[baseCoordinates.length - 1], map.selectedLayer);
        if (start == null || end == null) {
            map.showCopiedMessage("No se pudieron ubicar los extremos de la linea.");
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
            map.showCopiedMessage("Hace clic sobre uno de los extremos resaltados para elegir desde donde modificar.");
            return;
        }

        map.cadReferenceFromStart = startDistance <= endDistance;
        map.cadReferenceEndpointChosen = true;
        map.cadReferenceSegmentStart = map.cadReferenceFromStart ? new Coordinate(baseCoordinates[0]) : new Coordinate(baseCoordinates[baseCoordinates.length - 2]);
        map.cadReferenceSegmentEnd = map.cadReferenceFromStart ? new Coordinate(baseCoordinates[1]) : new Coordinate(baseCoordinates[baseCoordinates.length - 1]);
        map.showCopiedMessage(map.cadReferenceFromStart
                ? "Extremo inicial elegido. Ahora marca el nuevo largo sobre esa direccion."
                : "Extremo final elegido. Ahora marca el nuevo largo sobre esa direccion.");
        map.repaint();
    }

    boolean handleParallelLineClick(int screenX, int screenY) {
        if (!map.isSelectedFeatureLinear()) {
            map.showCopiedMessage("Paralela solo funciona tomando una linea como referencia.");
            return true;
        }

        Object geomObj = map.selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry geometry)) {
            return false;
        }

        if (!map.cadReferenceEndpointChosen) {
            if (!chooseCadReferenceSegment(screenX, screenY, geometry)) {
                map.showCopiedMessage("No se encontro un tramo cercano para tomar como referencia.");
            }
            return true;
        }

        Coordinate targetCoordinate = map.resolveInteractiveCoordinate(screenX, screenY, false);
        Coordinate sourceCoordinate = map.toSourceCoordinate(targetCoordinate.x, targetCoordinate.y, map.selectedLayer);
        Geometry derived = map.editingGeomOps.buildParallelLineGeometry(map.cadReferenceSegmentStart, map.cadReferenceSegmentEnd, sourceCoordinate);
        if (derived == null) {
            map.showCopiedMessage("No se pudo construir la paralela con ese desplazamiento.");
            return true;
        }

        return appendCadDerivedLine(derived, "Linea paralela creada.");
    }

    boolean handlePerpendicularLineClick(int screenX, int screenY) {
        if (!map.isSelectedFeatureLinear()) {
            map.showCopiedMessage("Perpendicular solo funciona tomando una linea como referencia.");
            return true;
        }

        Object geomObj = map.selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry geometry)) {
            return false;
        }

        if (!map.cadReferenceEndpointChosen) {
            if (!chooseCadReferenceSegment(screenX, screenY, geometry)) {
                map.showCopiedMessage("No se encontro un tramo cercano para tomar como referencia.");
            }
            return true;
        }

        Coordinate targetCoordinate = map.resolveInteractiveCoordinate(screenX, screenY, false);
        Coordinate sourceCoordinate = map.toSourceCoordinate(targetCoordinate.x, targetCoordinate.y, map.selectedLayer);
        Geometry derived = map.editingGeomOps.buildPerpendicularLineGeometry(map.cadReferenceSegmentStart, map.cadReferenceSegmentEnd, sourceCoordinate);
        if (derived == null) {
            map.showCopiedMessage("No se pudo construir la perpendicular con ese punto.");
            return true;
        }

        return appendCadDerivedLine(derived, "Linea perpendicular creada.");
    }

    boolean chooseCadReferenceSegment(int screenX, int screenY, Geometry sourceGeometry) {
        Geometry displayGeometry = map.getEditableDisplayGeometry(map.selectedFeature, map.selectedLayer);
        Coordinate displayTarget = new Coordinate(map.screenToWorldX(screenX), map.screenToWorldY(screenY));
        LineSplitProjection projection = map.findEditableSegmentProjection(
                displayGeometry,
                displayTarget,
                screenX,
                screenY,
                MapPanel.EDIT_SEGMENT_TOLERANCE_PX
        );
        if (projection == null || projection.segmentIndex < 0) {
            return false;
        }

        Coordinate[] sourceSegment = map.getEditableSegmentCoordinates(sourceGeometry, projection.segmentIndex);
        if (sourceSegment == null || sourceSegment.length < 2) {
            return false;
        }

        map.cadReferenceSegmentStart = sourceSegment[0];
        map.cadReferenceSegmentEnd = sourceSegment[1];
        map.cadReferenceEndpointChosen = true;
        map.showCopiedMessage("Tramo base seleccionado. Ahora completa la herramienta con un segundo clic.");
        map.repaint();
        return true;
    }

    boolean appendCadDerivedLine(Geometry geometry, String successMessage) {
        Layer targetLayer = resolveCadLineTargetLayer();
        if (targetLayer == null || geometry == null || geometry.isEmpty()) {
            return false;
        }
        map.clearCadConstructionState();
        return map.appendGeometriesToLayer(targetLayer, List.of(geometry), successMessage);
    }

    Layer resolveCadLineTargetLayer() {
        if (map.activeVectorEditingLayer != null) {
            ShapefileData editingData = map.getShapefileData(map.activeVectorEditingLayer);
            if ("LINE".equals(map.resolveGeometryFamily(editingData != null ? editingData.getSchema() : null))) {
                return map.activeVectorEditingLayer;
            }
        }
        if (map.selectedLayer != null) {
            ShapefileData selectedData = map.getShapefileData(map.selectedLayer);
            if ("LINE".equals(map.resolveGeometryFamily(selectedData != null ? selectedData.getSchema() : null))) {
                return map.selectedLayer;
            }
        }
        return null;
    }

    boolean handleAdjacentPolygonClick(int screenX, int screenY) {
        if (map.selectedFeature == null || map.selectedLayer == null) {
            return false;
        }

        Object geomObj = map.selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry geometry)) {
            return false;
        }

        if (map.adjacentPolygonSegmentStart == null || map.adjacentPolygonSegmentEnd == null) {
            Geometry displayGeometry = map.getEditableDisplayGeometry(map.selectedFeature, map.selectedLayer);
            Coordinate displayTarget = new Coordinate(map.screenToWorldX(screenX), map.screenToWorldY(screenY));
            LineSplitProjection projection = map.findEditableSegmentProjection(
                    displayGeometry,
                    displayTarget,
                    screenX,
                    screenY,
                    MapPanel.EDIT_SEGMENT_TOLERANCE_PX
            );
            if (projection == null || projection.segmentIndex < 0) {
                map.showCopiedMessage("No se encontro un borde cercano para construir el poligono adyacente.");
                return true;
            }

            Coordinate[] sourceSegment = map.getEditableSegmentCoordinates(geometry, projection.segmentIndex);
            if (sourceSegment == null) {
                map.showCopiedMessage("No se pudo identificar el borde base del poligono.");
                return true;
            }

            map.adjacentPolygonSegmentStart = sourceSegment[0];
            map.adjacentPolygonSegmentEnd = sourceSegment[1];
            map.showCopiedMessage("Borde base seleccionado. Ahora hace clic afuera del poligono para definir el ancho del adyacente.");
            map.repaint();
            return true;
        }

        Coordinate displayTarget = map.resolveInteractiveCoordinate(screenX, screenY, false);
        Coordinate sourceTarget = map.toSourceCoordinate(displayTarget.x, displayTarget.y, map.selectedLayer);
        Geometry adjacentGeometry = buildAdjacentPolygonGeometry(
                geometry,
                map.adjacentPolygonSegmentStart,
                map.adjacentPolygonSegmentEnd,
                sourceTarget
        );
        if (adjacentGeometry == null || adjacentGeometry.isEmpty()) {
            map.showCopiedMessage("No se pudo construir el poligono adyacente con ese punto. Proba con un clic mas afuera del borde.");
            return true;
        }

        return appendAdjacentPolygonToSelectedLayer(adjacentGeometry);
    }

    boolean cutSelectedGeometryAtClick(int screenX, int screenY) {
        if (map.selectedFeature == null || map.selectedLayer == null) {
            return false;
        }

        Object geomObj = map.selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry geometry)) {
            return false;
        }

        Coordinate targetCoordinate = map.resolveInteractiveCoordinate(screenX, screenY, false);
        Coordinate sourceCoordinate = map.toSourceCoordinate(targetCoordinate.x, targetCoordinate.y, map.selectedLayer);
        Geometry updated = buildCutGeometryAtPoint(geometry, sourceCoordinate);
        if (updated == null) {
            map.showCopiedMessage("No se pudo cortar la linea en ese punto.");
            return true;
        }

        List<Geometry> parts = map.collectGeometryParts(updated);
        if (parts.size() >= 2) {
            map.pushUndoSnapshotForSelectedLayer();
            replaceSelectedFeatureWithGeometries(parts, "Geometria cortada.");
            return true;
        }

        updateSelectedFeatureGeometry(updated, "Geometria cortada.");
        return true;
    }

    void applyFeatureEditSketchOperationEnhanced() {
        if (map.selectedFeature == null || map.selectedLayer == null || map.featureEditSketchCoordinates.isEmpty()) {
            return;
        }

        Object geomObj = map.selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry sourceGeometry)) {
            return;
        }

        List<Coordinate> sourceSketch = map.toSourceCoordinates(map.featureEditSketchCoordinates, map.selectedLayer);
        Geometry updated = null;
        String message = null;

        if (MapPanel.EDIT_OP_CUT.equals(map.featureEditOperation)) {
            updated = buildCutGeometryWithSketch(sourceGeometry, sourceSketch);
            message = "Geometria cortada.";
        } else if (MapPanel.EDIT_OP_HOLE.equals(map.featureEditOperation)) {
            updated = buildGeometryWithHole(sourceGeometry, sourceSketch);
            message = "Agujero creado.";
        }

        if (updated == null) {
            map.showCopiedMessage("No se pudo aplicar la edicion geometrica.");
            return;
        }

        if (MapPanel.EDIT_OP_CUT.equals(map.featureEditOperation)) {
            List<Geometry> parts = map.collectGeometryParts(updated);
            if (parts.size() >= 2) {
                map.pushUndoSnapshotForSelectedLayer();
                replaceSelectedFeatureWithGeometries(parts, message);
                return;
            }
        }

        updateSelectedFeatureGeometry(updated, message);
    }

    void applyFeatureEditSketchOperation() {
        if (map.selectedFeature == null || map.selectedLayer == null || map.featureEditSketchCoordinates.isEmpty()) {
            return;
        }

        Object geomObj = map.selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return;
        }

        Geometry sourceGeometry = (Geometry) geomObj;
        List<Coordinate> sourceSketch = map.toSourceCoordinates(map.featureEditSketchCoordinates, map.selectedLayer);
        Geometry updated = null;
        String message = null;

        if (MapPanel.EDIT_OP_CUT.equals(map.featureEditOperation)) {
            updated = buildCutGeometryWithSketch(sourceGeometry, sourceSketch);
            message = "GeometrÃ­a cortada.";
        } else if (MapPanel.EDIT_OP_HOLE.equals(map.featureEditOperation)) {
            updated = buildGeometryWithHole(sourceGeometry, sourceSketch);
            message = "Agujero creado.";
        }

        if (updated == null) {
            map.showCopiedMessage("No se pudo aplicar la ediciÃ³n geomÃ©trica.");
            return;
        }

        updateSelectedFeatureGeometry(updated, message);
    }

    int findEditableVertexIndex(int screenX, int screenY) {
        if (!map.featureEditMode || map.selectedFeature == null || map.selectedLayer == null) {
            return -1;
        }

        Geometry geometry = map.getEditableDisplayGeometry(map.selectedFeature, map.selectedLayer);
        Coordinate[] vertices = map.getEditableVertexCoordinates(geometry);
        if (vertices == null || vertices.length == 0) {
            return -1;
        }

        for (int i = 0; i < vertices.length; i++) {
            Coordinate c = vertices[i];
            int vx = map.worldToScreenX(c.x);
            int vy = map.worldToScreenY(c.y);
            double distance = Math.hypot(screenX - vx, screenY - vy);
            if (distance <= MapPanel.EDIT_VERTEX_TOLERANCE_PX) {
                return i;
            }
        }

        return -1;
    }

    private List<Integer> collectEditableVertexIndexes(Rectangle selectionBounds) {
        List<Integer> indexes = new ArrayList<>();
        if (!map.featureEditMode || map.selectedFeature == null || map.selectedLayer == null || selectionBounds == null) {
            return indexes;
        }

        Geometry geometry = map.getEditableDisplayGeometry(map.selectedFeature, map.selectedLayer);
        Coordinate[] vertices = map.getEditableVertexCoordinates(geometry);
        if (vertices == null || vertices.length == 0) {
            return indexes;
        }

        Rectangle expanded = new Rectangle(
                selectionBounds.x - 2,
                selectionBounds.y - 2,
                selectionBounds.width + 4,
                selectionBounds.height + 4
        );
        for (int i = 0; i < vertices.length; i++) {
            Coordinate c = vertices[i];
            if (c == null) {
                continue;
            }
            int vx = map.worldToScreenX(c.x);
            int vy = map.worldToScreenY(c.y);
            if (expanded.contains(vx, vy)) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    void moveSelectedVertex(double projectX, double projectY, int vertexIndex) {
        if (map.selectedFeature == null || map.selectedLayer == null || vertexIndex < 0) {
            return;
        }

        Object geomObj = map.selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return;
        }

        Geometry sourceGeometry = (Geometry) geomObj;
        Coordinate sourceCoordinate = map.toSourceCoordinate(projectX, projectY, map.selectedLayer);

        Geometry updated = buildGeometryWithMovedVertex(sourceGeometry, vertexIndex, sourceCoordinate);
        if (updated == null) {
            return;
        }

        map.selectedFeature.setDefaultGeometry(updated);
        if (map.selectedLayer.getFeatureCount() <= 0) {
            map.selectedLayer.setFeatureCount(1);
        }
        map.featureEditDirty = true;
        CatgisDesktopApp.markProjectDirty();
        map.repaint();
    }

    void moveSelectedFeatures(double projectX, double projectY) {
        if (map.selectedLayer == null) {
            return;
        }

        List<String> selectedIds = map.getSelectedFeatureIdsForLayer(map.selectedLayer);
        if (selectedIds.isEmpty()) {
            return;
        }

        ShapefileData data = map.getShapefileData(map.selectedLayer);
        if (data == null || data.getFeatures() == null) {
            return;
        }

        Coordinate previousSource = map.toSourceCoordinate(map.moveSelectionLastProjectX, map.moveSelectionLastProjectY, map.selectedLayer);
        Coordinate currentSource = map.toSourceCoordinate(projectX, projectY, map.selectedLayer);
        if (previousSource == null || currentSource == null) {
            return;
        }

        double dx = currentSource.x - previousSource.x;
        double dy = currentSource.y - previousSource.y;
        if (Math.abs(dx) < 0.0000001 && Math.abs(dy) < 0.0000001) {
            return;
        }

        for (SimpleFeature feature : data.getFeatures()) {
            if (feature == null || feature.getID() == null || !selectedIds.contains(feature.getID())) {
                continue;
            }
            Object geomObj = feature.getDefaultGeometry();
            if (!(geomObj instanceof Geometry geometry)) {
                continue;
            }
            feature.setDefaultGeometry(map.translateGeometry(geometry, dx, dy));
        }

        map.moveSelectionLastProjectX = projectX;
        map.moveSelectionLastProjectY = projectY;
        map.featureEditDirty = true;
        CatgisDesktopApp.markProjectDirty();
    }

    void updateSelectedFeatureGeometry(Geometry updated, String statusMessage) {
        if (updated == null || map.selectedFeature == null || map.selectedLayer == null) {
            return;
        }

        map.pushUndoSnapshotForSelectedLayer();
        List<Geometry> replacementParts = new ArrayList<>();
        replacementParts.add(updated);
        replaceSelectedFeatureWithGeometries(replacementParts, statusMessage);
    }

    void replaceSelectedFeatureWithGeometries(List<Geometry> replacementParts, String statusMessage) {
        if (map.selectedLayer == null || map.selectedFeature == null || replacementParts == null || replacementParts.isEmpty()) {
            return;
        }

        ShapefileData data = map.getShapefileData(map.selectedLayer);
        if (data == null) {
            return;
        }

        List<SimpleFeature> features = new ArrayList<>();
        List<SimpleFeature> replacementFeatures = map.buildReplacementFeatures(map.selectedFeature, replacementParts);
        if (replacementFeatures.isEmpty()) {
            return;
        }

        boolean replaced = false;
        for (SimpleFeature feature : data.getFeatures()) {
            if (feature == null) {
                continue;
            }
            if (feature == map.selectedFeature || map.sameFeatureId(feature, map.selectedFeature)) {
                features.addAll(replacementFeatures);
                replaced = true;
            } else {
                features.add(feature);
            }
        }

        if (!replaced) {
            features = new ArrayList<>(data.getFeatures());
            int selectedIndex = features.indexOf(map.selectedFeature);
            if (selectedIndex >= 0) {
                features.remove(selectedIndex);
                features.addAll(selectedIndex, replacementFeatures);
                replaced = true;
            }
        }

        if (!replaced) {
            map.showCopiedMessage("No se pudo reemplazar la entidad original dentro de la capa.");
            return;
        }

        String finalMessage = statusMessage;
        if (replacementFeatures.size() >= 2) {
            finalMessage = (statusMessage != null && !statusMessage.isBlank() ? statusMessage + " " : "")
                    + "(" + replacementFeatures.size() + " tramos)";
        }
        replaceLayerFeatures(map.selectedLayer, features, replacementFeatures.get(0).getID(), true, finalMessage);
    }

    void replaceLayerFeatures(Layer layer, List<SimpleFeature> features, String selectedFeatureId, boolean keepEditMode, String statusMessage) {
        if (layer == null) {
            return;
        }

        ShapefileData currentData = map.getShapefileData(layer);
        String sourceName = currentData != null ? currentData.getSourceName() : layer.getName();
        String message = currentData != null ? currentData.getMessage() : "Edicion vectorial";
        Envelope envelope = map.computeEnvelope(features);
        SimpleFeatureType schema = currentData != null ? currentData.getSchema() : null;

        ShapefileData newData = new ShapefileData(
                features,
                envelope,
                sourceName,
                features != null ? features.size() : 0,
                message,
                schema
        );
        map.addOrUpdateShapefileLayer(layer, newData);

        String nextOperation = map.shouldPreserveFeatureEditOperation() ? map.featureEditOperation : MapPanel.EDIT_OP_MOVE_VERTEX;
        map.activeVectorEditingLayer = keepEditMode || map.activeVectorEditingLayer == layer ? layer : map.activeVectorEditingLayer;
        map.selectedLayer = layer;
        map.selectedFeature = map.findFeatureById(newData.getFeatures(), selectedFeatureId);
        if (selectedFeatureId != null && !selectedFeatureId.isBlank()) {
            map.tableSelectionIds.put(layer, new ArrayList<>(List.of(selectedFeatureId)));
            OpenAttributeTableAction.syncSelectionFromMap(layer, List.of(selectedFeatureId));
        } else {
            map.tableSelectionIds.remove(layer);
            OpenAttributeTableAction.clearSelectionInOpenTables();
        }
        map.featureEditMode = keepEditMode && map.selectedFeature != null;
        map.featureEditOriginalGeometry = map.extractFeatureGeometryCopy(map.selectedFeature);
        map.featureEditDirty = true;
        map.featureEditSketchCoordinates.clear();
        map.activeEditVertexIndex = -1;
        map.joinTargetVertexIndex = -1;
        map.clearAdjacentPolygonState();
        map.clearCadConstructionState();
        map.featureEditOperation = nextOperation;
        layer.setFeatureCount(newData.getFeatureCount());

        CatgisDesktopApp.markProjectDirty();
        if (statusMessage != null && !statusMessage.isBlank()) {
            map.showCopiedMessage(statusMessage);
        }
        map.refreshEditingUi();
    }

    Geometry buildAdjacentPolygonGeometry(Geometry sourceGeometry,
                                                  Coordinate segmentStart,
                                                  Coordinate segmentEnd,
                                                  Coordinate sideCoordinate) {
        if (sourceGeometry == null || segmentStart == null || segmentEnd == null || sideCoordinate == null) {
            return null;
        }

        double dx = segmentEnd.x - segmentStart.x;
        double dy = segmentEnd.y - segmentStart.y;
        double length = Math.hypot(dx, dy);
        if (length <= 0.0000001) {
            return null;
        }

        double nx1 = -dy / length;
        double ny1 = dx / length;
        double nx2 = dy / length;
        double ny2 = -dx / length;

        Coordinate midpoint = new Coordinate(
                (segmentStart.x + segmentEnd.x) / 2.0,
                (segmentStart.y + segmentEnd.y) / 2.0
        );
        double dot1 = ((sideCoordinate.x - midpoint.x) * nx1) + ((sideCoordinate.y - midpoint.y) * ny1);
        double dot2 = ((sideCoordinate.x - midpoint.x) * nx2) + ((sideCoordinate.y - midpoint.y) * ny2);

        double nx = Math.abs(dot1) >= Math.abs(dot2) ? nx1 : nx2;
        double ny = Math.abs(dot1) >= Math.abs(dot2) ? ny1 : ny2;
        double distance = Math.max(Math.abs(dot1), Math.abs(dot2));
        if (distance <= 0.0000001) {
            return null;
        }

        GeometryFactory factory = sourceGeometry.getFactory() != null ? sourceGeometry.getFactory() : new GeometryFactory();
        Polygon candidate = buildAdjacentPolygonAlongSegment(factory, segmentStart, segmentEnd, nx, ny, distance);
        if (candidate == null) {
            return null;
        }

        Point interiorPoint = candidate.getInteriorPoint();
        if (interiorPoint != null && sourceGeometry.covers(interiorPoint)) {
            candidate = buildAdjacentPolygonAlongSegment(factory, segmentStart, segmentEnd, -nx, -ny, distance);
            if (candidate == null) {
                return null;
            }
        }

        if (candidate.getArea() <= 0.0) {
            return null;
        }

        try {
            Geometry overlap = sourceGeometry.intersection(candidate);
            if (overlap != null && overlap.getArea() > 0.0000001) {
                return null;
            }
        } catch (Exception ignored) { CatgisLogger.warn("Error al verificar solapamiento de geometrias", ignored); }

        return candidate;
    }

    private Polygon buildAdjacentPolygonAlongSegment(GeometryFactory factory,
                                                     Coordinate segmentStart,
                                                     Coordinate segmentEnd,
                                                     double nx,
                                                     double ny,
                                                     double distance) {
        Coordinate offsetStart = new Coordinate(
                segmentStart.x + (nx * distance),
                segmentStart.y + (ny * distance)
        );
        Coordinate offsetEnd = new Coordinate(
                segmentEnd.x + (nx * distance),
                segmentEnd.y + (ny * distance)
        );

        Coordinate[] shell = MapGeometryUtils.normalizeRingCoordinates(new Coordinate[]{
                new Coordinate(segmentStart),
                new Coordinate(segmentEnd),
                offsetEnd,
                offsetStart,
                new Coordinate(segmentStart)
        });
        if (shell == null) {
            return null;
        }
        return factory.createPolygon(factory.createLinearRing(shell), null);
    }

    private boolean appendAdjacentPolygonToSelectedLayer(Geometry adjacentGeometry) {
        if (map.selectedLayer == null || map.selectedFeature == null || adjacentGeometry == null || adjacentGeometry.isEmpty()) {
            return false;
        }

        ShapefileData targetData = map.getShapefileData(map.selectedLayer);
        if (targetData == null || targetData.getSchema() == null) {
            NotificationManager.warn(map, null, "La capa editable no tiene esquema vectorial disponible.");
            return true;
        }

        map.pushUndoSnapshotForSelectedLayer();
        List<SimpleFeature> features = new ArrayList<>(targetData.getFeatures());
        SimpleFeature createdFeature = map.buildDerivedFeatureForLayer(targetData, adjacentGeometry, features, map.selectedFeature);
        if (createdFeature == null) {
            map.showCopiedMessage("No se pudo crear el poligono adyacente dentro de la capa.");
            return true;
        }

        String keepFeatureId = map.selectedFeature.getID();
        features.add(createdFeature);
        replaceLayerFeatures(map.selectedLayer, features, keepFeatureId, true, "Poligono adyacente creado.");
        map.featureEditOperation = MapPanel.EDIT_OP_ADJACENT_POLYGON;
        map.clearAdjacentPolygonState();
        if (map.selectedFeature != null) {
            map.startSelectionFlash(map.selectedLayer, map.selectedFeature);
        }
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage("Poligono adyacente creado. Elegi otro borde o cambia de herramienta.");
        }
        map.refreshEditingUi();
        return true;
    }

    private Geometry buildCutGeometryAtPoint(Geometry geometry, Coordinate coordinate) {
        if (geometry instanceof LineString) {
            return splitLineStringAtCoordinate((LineString) geometry, coordinate);
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString multi = (MultiLineString) geometry;
            List<LineString> parts = new ArrayList<>();
            boolean splitDone = false;
            int closestIndex = -1;
            double closestDistance = Double.MAX_VALUE;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                LineString line = (LineString) multi.getGeometryN(i);
                LineSplitProjection projection = MapGeometryUtils.projectCoordinateOntoLine(line, coordinate);
                if (projection != null && projection.distance < closestDistance) {
                    closestDistance = projection.distance;
                    closestIndex = i;
                }
            }
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                LineString line = (LineString) multi.getGeometryN(i);
                if (!splitDone && i == closestIndex) {
                    Geometry split = splitLineStringAtCoordinate(line, coordinate);
                    if (split instanceof MultiLineString) {
                        MultiLineString splitMulti = (MultiLineString) split;
                        for (int j = 0; j < splitMulti.getNumGeometries(); j++) {
                            parts.add((LineString) splitMulti.getGeometryN(j));
                        }
                        splitDone = true;
                        continue;
                    }
                }
                parts.add(line);
            }
            if (splitDone) {
                return geometry.getFactory().createMultiLineString(parts.toArray(new LineString[0]));
            }
        }

        return null;
    }

    private Geometry splitLineStringAtCoordinate(LineString line, Coordinate coordinate) {
        if (line == null || line.getNumPoints() < 2) {
            return null;
        }

        LineSplitProjection projection = MapGeometryUtils.projectCoordinateOntoLine(line, coordinate);
        if (projection == null || projection.segmentIndex < 0 || projection.projected == null) {
            return null;
        }

        Coordinate[] coords = line.getCoordinates();
        double tolerance = Math.max(1e-8, Math.max(line.getLength() * 0.00001, 0.0000001));
        if (projection.projected.distance(coords[0]) <= tolerance
                || projection.projected.distance(coords[coords.length - 1]) <= tolerance) {
            return null;
        }

        List<Coordinate> firstCoords = new ArrayList<>();
        for (int i = 0; i <= projection.segmentIndex; i++) {
            MapGeometryUtils.appendCoordinateIfNeeded(firstCoords, coords[i], tolerance);
        }
        MapGeometryUtils.appendCoordinateIfNeeded(firstCoords, projection.projected, tolerance);

        List<Coordinate> secondCoords = new ArrayList<>();
        MapGeometryUtils.appendCoordinateIfNeeded(secondCoords, projection.projected, tolerance);
        for (int i = projection.segmentIndex + 1; i < coords.length; i++) {
            MapGeometryUtils.appendCoordinateIfNeeded(secondCoords, coords[i], tolerance);
        }

        if (firstCoords.size() < 2 || secondCoords.size() < 2) {
            return null;
        }

        LineString first = line.getFactory().createLineString(firstCoords.toArray(new Coordinate[0]));
        LineString second = line.getFactory().createLineString(secondCoords.toArray(new Coordinate[0]));
        if (first.getLength() <= tolerance || second.getLength() <= tolerance) {
            return null;
        }

        return line.getFactory().createMultiLineString(new LineString[]{first, second});
    }

    private Geometry buildCutGeometryWithSketch(Geometry geometry, List<Coordinate> sketchCoordinates) {
        if (geometry == null || sketchCoordinates == null || sketchCoordinates.size() < 2) {
            return null;
        }

        if (geometry instanceof Polygon) {
            return splitPolygonWithBlade((Polygon) geometry, sketchCoordinates);
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon multi = (MultiPolygon) geometry;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multi.getGeometryN(i);
                Geometry split = splitPolygonWithBlade(polygon, sketchCoordinates);
                if (split != null) {
                    List<Polygon> splitPolygons = map.collectPolygons(split);
                    if (!splitPolygons.isEmpty()) {
                        List<Polygon> all = new ArrayList<>();
                        for (int j = 0; j < i; j++) {
                            all.add((Polygon) multi.getGeometryN(j));
                        }
                        all.addAll(splitPolygons);
                        for (int j = i + 1; j < multi.getNumGeometries(); j++) {
                            all.add((Polygon) multi.getGeometryN(j));
                        }
                        return geometry.getFactory().createMultiPolygon(all.toArray(new Polygon[0]));
                    }
                }
            }
        }

        return null;
    }

    private Geometry splitPolygonWithBlade(Polygon polygon, List<Coordinate> sketchCoordinates) {
        try {
            GeometryFactory factory = polygon.getFactory();
            LineString blade = factory.createLineString(sketchCoordinates.toArray(new Coordinate[0]));
            Geometry noded = polygon.getBoundary().union(blade);
            Polygonizer polygonizer = new Polygonizer();
            polygonizer.add(noded);

            List<Polygon> parts = new ArrayList<>();
            for (Object object : polygonizer.getPolygons()) {
                if (object instanceof Polygon) {
                    Polygon candidate = (Polygon) object;
                    Point interiorPoint = candidate.getInteriorPoint();
                    if (interiorPoint != null && polygon.covers(interiorPoint)) {
                        parts.add(candidate);
                    }
                }
            }

            if (parts.size() < 2) {
                return null;
            }

            return map.assemblePolygons(parts, factory);
        } catch (Exception ex) {
            return null;
        }
    }

    private Geometry buildGeometryWithHole(Geometry geometry, List<Coordinate> sketchCoordinates) {
        if (geometry == null || sketchCoordinates == null || sketchCoordinates.size() < 3) {
            return null;
        }

        GeometryFactory factory = geometry.getFactory();
        Polygon holePolygon = map.buildPolygonFromCoordinates(sketchCoordinates, factory);
        if (holePolygon == null) {
            return null;
        }

        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            if (!polygon.covers(holePolygon)) {
                return null;
            }
            return map.normalizePolygonalGeometry(polygon.difference(holePolygon), factory);
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon multi = (MultiPolygon) geometry;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multi.getGeometryN(i);
                if (polygon.covers(holePolygon)) {
                    Geometry diff = map.normalizePolygonalGeometry(polygon.difference(holePolygon), factory);
                    List<Polygon> pieces = map.collectPolygons(diff);
                    if (pieces.isEmpty()) {
                        return null;
                    }
                    List<Polygon> all = new ArrayList<>();
                    for (int j = 0; j < i; j++) {
                        all.add((Polygon) multi.getGeometryN(j));
                    }
                    all.addAll(pieces);
                    for (int j = i + 1; j < multi.getNumGeometries(); j++) {
                        all.add((Polygon) multi.getGeometryN(j));
                    }
                    return factory.createMultiPolygon(all.toArray(new Polygon[0]));
                }
            }
        }

        return null;
    }

    private Geometry buildGeometryWithMovedVertex(Geometry geometry, int vertexIndex, Coordinate newCoordinate) {
        if (geometry instanceof LineString) {
            Coordinate[] coords = geometry.getCoordinates().clone();
            if (vertexIndex >= coords.length) {
                return null;
            }
            coords[vertexIndex] = new Coordinate(newCoordinate);
            return geometry.getFactory().createLineString(coords);
        }

        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            Coordinate[] shellCoords = polygon.getExteriorRing().getCoordinates().clone();
            if (shellCoords.length <= 3 || vertexIndex >= shellCoords.length - 1) {
                return null;
            }

            shellCoords[vertexIndex] = new Coordinate(newCoordinate);
            if (vertexIndex == 0) {
                shellCoords[shellCoords.length - 1] = new Coordinate(newCoordinate);
            }

            LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                holes[i] = geometry.getFactory().createLinearRing(polygon.getInteriorRingN(i).getCoordinates());
            }

            return geometry.getFactory().createPolygon(
                    geometry.getFactory().createLinearRing(shellCoords),
                    holes
            );
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString multi = (MultiLineString) geometry;
            LineString[] lines = new LineString[multi.getNumGeometries()];
            int offset = 0;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                LineString line = (LineString) multi.getGeometryN(i);
                Coordinate[] coords = line.getCoordinates().clone();
                if (vertexIndex >= offset && vertexIndex < offset + coords.length) {
                    coords[vertexIndex - offset] = new Coordinate(newCoordinate);
                }
                lines[i] = geometry.getFactory().createLineString(coords);
                offset += coords.length;
            }
            return geometry.getFactory().createMultiLineString(lines);
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon multi = (MultiPolygon) geometry;
            Polygon[] polygons = new Polygon[multi.getNumGeometries()];
            int offset = 0;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multi.getGeometryN(i);
                Coordinate[] shellCoords = polygon.getExteriorRing().getCoordinates().clone();
                int visibleVertices = Math.max(0, shellCoords.length - 1);
                if (vertexIndex >= offset && vertexIndex < offset + visibleVertices) {
                    int localIndex = vertexIndex - offset;
                    shellCoords[localIndex] = new Coordinate(newCoordinate);
                    if (localIndex == 0) {
                        shellCoords[shellCoords.length - 1] = new Coordinate(newCoordinate);
                    }
                }
                polygons[i] = geometry.getFactory().createPolygon(
                        geometry.getFactory().createLinearRing(shellCoords),
                        MapGeometryUtils.copyInteriorRings(geometry.getFactory(), polygon)
                );
                offset += visibleVertices;
            }
            return geometry.getFactory().createMultiPolygon(polygons);
        }

        return null;
    }

    private Geometry buildGeometryWithAddedVertex(Geometry geometry, int segmentIndex, Coordinate newCoordinate) {
        if (geometry instanceof LineString) {
            return geometry.getFactory().createLineString(MapGeometryUtils.insertCoordinate(((LineString) geometry).getCoordinates(), segmentIndex + 1, newCoordinate));
        }

        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            Coordinate[] shell = MapGeometryUtils.insertCoordinate(polygon.getExteriorRing().getCoordinates(), segmentIndex + 1, newCoordinate);
            return geometry.getFactory().createPolygon(
                    geometry.getFactory().createLinearRing(shell),
                    MapGeometryUtils.copyInteriorRings(geometry.getFactory(), polygon)
            );
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString multi = (MultiLineString) geometry;
            LineString[] lines = new LineString[multi.getNumGeometries()];
            int offset = 0;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                LineString line = (LineString) multi.getGeometryN(i);
                Coordinate[] coords = line.getCoordinates();
                int segments = Math.max(0, coords.length - 1);
                if (segmentIndex >= offset && segmentIndex < offset + segments) {
                    coords = MapGeometryUtils.insertCoordinate(coords, (segmentIndex - offset) + 1, newCoordinate);
                }
                lines[i] = geometry.getFactory().createLineString(coords);
                offset += segments;
            }
            return geometry.getFactory().createMultiLineString(lines);
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon multi = (MultiPolygon) geometry;
            Polygon[] polygons = new Polygon[multi.getNumGeometries()];
            int offset = 0;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multi.getGeometryN(i);
                Coordinate[] shell = polygon.getExteriorRing().getCoordinates();
                int segments = Math.max(0, shell.length - 1);
                if (segmentIndex >= offset && segmentIndex < offset + segments) {
                    shell = MapGeometryUtils.insertCoordinate(shell, (segmentIndex - offset) + 1, newCoordinate);
                }
                polygons[i] = geometry.getFactory().createPolygon(
                        geometry.getFactory().createLinearRing(shell),
                        MapGeometryUtils.copyInteriorRings(geometry.getFactory(), polygon)
                );
                offset += segments;
            }
            return geometry.getFactory().createMultiPolygon(polygons);
        }

        return null;
    }

    private Geometry buildGeometryWithRemovedVertex(Geometry geometry, int vertexIndex) {
        if (geometry instanceof LineString) {
            Coordinate[] coords = ((LineString) geometry).getCoordinates();
            if (coords.length <= 2 || vertexIndex >= coords.length) {
                return null;
            }
            return geometry.getFactory().createLineString(MapGeometryUtils.removeCoordinate(coords, vertexIndex));
        }

        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            Coordinate[] shell = polygon.getExteriorRing().getCoordinates();
            if (shell.length <= 4 || vertexIndex >= shell.length - 1) {
                return null;
            }
            shell = MapGeometryUtils.removeRingCoordinate(shell, vertexIndex);
            return geometry.getFactory().createPolygon(
                    geometry.getFactory().createLinearRing(shell),
                    MapGeometryUtils.copyInteriorRings(geometry.getFactory(), polygon)
            );
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString multi = (MultiLineString) geometry;
            LineString[] lines = new LineString[multi.getNumGeometries()];
            int offset = 0;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                Coordinate[] coords = ((LineString) multi.getGeometryN(i)).getCoordinates();
                if (vertexIndex >= offset && vertexIndex < offset + coords.length) {
                    if (coords.length <= 2) {
                        return null;
                    }
                    coords = MapGeometryUtils.removeCoordinate(coords, vertexIndex - offset);
                }
                lines[i] = geometry.getFactory().createLineString(coords);
                offset += coords.length;
            }
            return geometry.getFactory().createMultiLineString(lines);
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon multi = (MultiPolygon) geometry;
            Polygon[] polygons = new Polygon[multi.getNumGeometries()];
            int offset = 0;
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multi.getGeometryN(i);
                Coordinate[] shell = polygon.getExteriorRing().getCoordinates();
                int visibleVertices = Math.max(0, shell.length - 1);
                if (vertexIndex >= offset && vertexIndex < offset + visibleVertices) {
                    if (shell.length <= 4) {
                        return null;
                    }
                    shell = MapGeometryUtils.removeRingCoordinate(shell, vertexIndex - offset);
                }
                polygons[i] = geometry.getFactory().createPolygon(
                        geometry.getFactory().createLinearRing(shell),
                        MapGeometryUtils.copyInteriorRings(geometry.getFactory(), polygon)
                );
                offset += visibleVertices;
            }
            return geometry.getFactory().createMultiPolygon(polygons);
        }

        return null;
    }

    private Geometry buildGeometryWithRemovedVertices(Geometry geometry, List<Integer> vertexIndexes) {
        if (geometry == null || vertexIndexes == null || vertexIndexes.isEmpty()) {
            return geometry;
        }

        List<Integer> sortedIndexes = new ArrayList<>(vertexIndexes);
        sortedIndexes.sort((a, b) -> Integer.compare(b, a));

        Geometry updated = geometry;
        for (Integer index : sortedIndexes) {
            if (index == null) {
                continue;
            }
            updated = buildGeometryWithRemovedVertex(updated, index);
            if (updated == null) {
                return null;
            }
        }
        return updated;
    }

    private Geometry buildGeometryWithJoinedVertices(Geometry geometry, int targetVertexIndex, List<Integer> vertexIndexes) {
        if (geometry == null || vertexIndexes == null || vertexIndexes.isEmpty()) {
            return null;
        }

        if (geometry instanceof LineString line) {
            return buildLineStringWithJoinedVertices(line, targetVertexIndex, vertexIndexes);
        }
        if (geometry instanceof Polygon polygon) {
            return buildPolygonWithJoinedVertices(polygon, targetVertexIndex, vertexIndexes);
        }
        if (geometry instanceof MultiLineString multiLine) {
            return buildMultiLineStringWithJoinedVertices(multiLine, targetVertexIndex, vertexIndexes);
        }
        if (geometry instanceof MultiPolygon multiPolygon) {
            return buildMultiPolygonWithJoinedVertices(multiPolygon, targetVertexIndex, vertexIndexes);
        }

        return null;
    }

    private Geometry buildLineStringWithJoinedVertices(LineString line, int targetVertexIndex, Collection<Integer> joinIndexes) {
        Coordinate[] coords = MapGeometryUtils.copyCoordinates(line.getCoordinates());
        if (coords.length < 2 || targetVertexIndex < 0 || targetVertexIndex >= coords.length) {
            return null;
        }

        Coordinate anchor = new Coordinate(coords[targetVertexIndex]);
        boolean changed = false;
        for (Integer joinIndex : joinIndexes) {
            if (joinIndex == null || joinIndex < 0 || joinIndex >= coords.length || joinIndex == targetVertexIndex) {
                continue;
            }
            coords[joinIndex] = new Coordinate(anchor);
            changed = true;
        }
        if (!changed) {
            return null;
        }

        Coordinate[] normalized = MapGeometryUtils.collapseDuplicateLineCoordinates(coords);
        if (normalized == null || normalized.length < 2) {
            return null;
        }
        return line.getFactory().createLineString(normalized);
    }

    private Geometry buildPolygonWithJoinedVertices(Polygon polygon, int targetVertexIndex, Collection<Integer> joinIndexes) {
        Coordinate[] shell = MapGeometryUtils.copyCoordinates(polygon.getExteriorRing().getCoordinates());
        int visibleVertices = Math.max(0, shell.length - 1);
        if (visibleVertices < 3 || targetVertexIndex < 0 || targetVertexIndex >= visibleVertices) {
            return null;
        }

        Coordinate anchor = new Coordinate(shell[targetVertexIndex]);
        boolean changed = false;
        for (Integer joinIndex : joinIndexes) {
            if (joinIndex == null || joinIndex < 0 || joinIndex >= visibleVertices || joinIndex == targetVertexIndex) {
                continue;
            }
            shell[joinIndex] = new Coordinate(anchor);
            changed = true;
        }
        if (!changed) {
            return null;
        }

        Coordinate[] normalizedShell = MapGeometryUtils.normalizeRingCoordinates(shell);
        if (normalizedShell == null) {
            return null;
        }
        return polygon.getFactory().createPolygon(
                polygon.getFactory().createLinearRing(normalizedShell),
                MapGeometryUtils.copyInteriorRings(polygon.getFactory(), polygon)
        );
    }

    private Geometry buildMultiLineStringWithJoinedVertices(MultiLineString multi, int targetVertexIndex, Collection<Integer> joinIndexes) {
        int targetPart = -1;
        int targetLocalIndex = -1;
        int offset = 0;
        for (int i = 0; i < multi.getNumGeometries(); i++) {
            LineString line = (LineString) multi.getGeometryN(i);
            int vertexCount = line.getCoordinates().length;
            if (targetVertexIndex >= offset && targetVertexIndex < offset + vertexCount) {
                targetPart = i;
                targetLocalIndex = targetVertexIndex - offset;
                break;
            }
            offset += vertexCount;
        }
        if (targetPart < 0) {
            return null;
        }

        LineString[] parts = new LineString[multi.getNumGeometries()];
        offset = 0;
        for (int i = 0; i < multi.getNumGeometries(); i++) {
            LineString line = (LineString) multi.getGeometryN(i);
            int vertexCount = line.getCoordinates().length;
            if (i == targetPart) {
                List<Integer> localIndexes = new ArrayList<>();
                for (Integer joinIndex : joinIndexes) {
                    if (joinIndex != null && joinIndex >= offset && joinIndex < offset + vertexCount) {
                        localIndexes.add(joinIndex - offset);
                    }
                }
                Geometry updated = buildLineStringWithJoinedVertices(line, targetLocalIndex, localIndexes);
                if (!(updated instanceof LineString updatedLine)) {
                    return null;
                }
                parts[i] = updatedLine;
            } else {
                parts[i] = (LineString) line.copy();
            }
            offset += vertexCount;
        }
        return multi.getFactory().createMultiLineString(parts);
    }

    private Geometry buildMultiPolygonWithJoinedVertices(MultiPolygon multi, int targetVertexIndex, Collection<Integer> joinIndexes) {
        int targetPart = -1;
        int targetLocalIndex = -1;
        int offset = 0;
        for (int i = 0; i < multi.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) multi.getGeometryN(i);
            int visibleVertices = Math.max(0, polygon.getExteriorRing().getCoordinates().length - 1);
            if (targetVertexIndex >= offset && targetVertexIndex < offset + visibleVertices) {
                targetPart = i;
                targetLocalIndex = targetVertexIndex - offset;
                break;
            }
            offset += visibleVertices;
        }
        if (targetPart < 0) {
            return null;
        }

        Polygon[] parts = new Polygon[multi.getNumGeometries()];
        offset = 0;
        for (int i = 0; i < multi.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) multi.getGeometryN(i);
            int visibleVertices = Math.max(0, polygon.getExteriorRing().getCoordinates().length - 1);
            if (i == targetPart) {
                List<Integer> localIndexes = new ArrayList<>();
                for (Integer joinIndex : joinIndexes) {
                    if (joinIndex != null && joinIndex >= offset && joinIndex < offset + visibleVertices) {
                        localIndexes.add(joinIndex - offset);
                    }
                }
                Geometry updated = buildPolygonWithJoinedVertices(polygon, targetLocalIndex, localIndexes);
                if (!(updated instanceof Polygon updatedPolygon)) {
                    return null;
                }
                parts[i] = updatedPolygon;
            } else {
                parts[i] = (Polygon) polygon.copy();
            }
            offset += visibleVertices;
        }
        return multi.getFactory().createMultiPolygon(parts);
    }

    // -----------------------------------------------------------------------
    // feature merge / explode / delete
    // -----------------------------------------------------------------------

    boolean canMergeSelectedFeatures() {
        if (map.selectedLayer == null || map.isReadOnlyVectorLayer(map.selectedLayer) || map.getSelectedFeatureCount() < 2) {
            return false;
        }

        ShapefileData data = map.getShapefileData(map.selectedLayer);
        String family = FeatureBuilder.resolveGeometryFamily(data != null ? data.getSchema() : null);
        return "LINE".equals(family) || "POLYGON".equals(family);
    }

    boolean mergeSelectedFeatures() {
        if (!canMergeSelectedFeatures()) {
            return false;
        }

        ShapefileData data = map.getShapefileData(map.selectedLayer);
        List<String> selectedIds = map.getSelectedFeatureIdsForLayer(map.selectedLayer);
        if (data == null || selectedIds.size() < 2) {
            return false;
        }

        List<SimpleFeature> selectedFeatures = FeatureBuilder.collectSelectedFeatures(data.getFeatures(), selectedIds);
        if (selectedFeatures.size() < 2) {
            return false;
        }

        String family = FeatureBuilder.resolveGeometryFamily(data.getSchema());
        Geometry mergedGeometry = FeatureBuilder.buildMergedGeometry(selectedFeatures, family);
        if (mergedGeometry == null || mergedGeometry.isEmpty()) {
            NotificationManager.info(
                    map,
                    "Unir elementos",
                    "No se pudieron unir las entidades seleccionadas.");
            return false;
        }

        List<SimpleFeature> replacementFeatures = FeatureBuilder.buildFeaturesForMergedGeometry(
                selectedFeatures.get(0), mergedGeometry, data.getSchema());
        if (replacementFeatures.isEmpty()) {
            NotificationManager.info(
                    map,
                    "Unir elementos",
                    "La geometria resultante no es compatible con la capa actual.");
            return false;
        }

        map.pushUndoSnapshotForSelectedLayer();
        List<SimpleFeature> updatedFeatures = FeatureBuilder.replaceFeaturesBySelection(
                data.getFeatures(), selectedIds, replacementFeatures);
        List<String> resultIds = FeatureBuilder.extractFeatureIds(replacementFeatures);
        map.replaceLayerFeatures(map.selectedLayer, updatedFeatures, resultIds.get(0), resultIds.size() == 1, null);
        map.applyFeatureSelection(
                map.selectedLayer,
                resultIds,
                resultIds.size() == 1,
                true,
                false,
                resultIds.size() == 1
                        ? "Elementos unidos."
                        : resultIds.size() + " entidades resultantes tras unir."
        );
        return true;
    }

    boolean canExplodeSelectedFeatures() {
        if (map.selectedLayer == null || map.isReadOnlyVectorLayer(map.selectedLayer)) {
            return false;
        }

        ShapefileData data = map.getShapefileData(map.selectedLayer);
        List<String> selectedIds = map.getSelectedFeatureIdsForLayer(map.selectedLayer);
        if (data == null || selectedIds.isEmpty()) {
            return false;
        }

        for (SimpleFeature feature : FeatureBuilder.collectSelectedFeatures(data.getFeatures(), selectedIds)) {
            if (FeatureBuilder.geometryPartCount(MapGeometryUtils.extractFeatureGeometryCopy(feature)) > 1) {
                return true;
            }
        }
        return false;
    }

    boolean explodeSelectedFeatures() {
        if (map.selectedLayer == null) {
            return false;
        }
        if (map.isReadOnlyVectorLayer(map.selectedLayer)) {
            NotificationManager.warn(map, null, map.getReadOnlyLayerMessage(map.selectedLayer));
            return false;
        }

        ShapefileData data = map.getShapefileData(map.selectedLayer);
        List<String> selectedIds = map.getSelectedFeatureIdsForLayer(map.selectedLayer);
        if (data == null || selectedIds.isEmpty()) {
            return false;
        }

        List<SimpleFeature> updatedFeatures = new ArrayList<>();
        List<String> resultIds = new ArrayList<>();
        boolean changed = false;
        for (SimpleFeature feature : data.getFeatures()) {
            if (feature == null) {
                continue;
            }

            if (!selectedIds.contains(feature.getID())) {
                updatedFeatures.add(feature);
                continue;
            }

            List<Geometry> parts = MapGeometryUtils.collectGeometryParts(
                    MapGeometryUtils.extractFeatureGeometryCopy(feature));
            if (parts.size() <= 1) {
                updatedFeatures.add(feature);
                resultIds.add(feature.getID());
                continue;
            }

            List<SimpleFeature> replacementFeatures = FeatureBuilder.buildReplacementFeatures(feature, parts);
            if (replacementFeatures.isEmpty()) {
                updatedFeatures.add(feature);
                resultIds.add(feature.getID());
                continue;
            }

            updatedFeatures.addAll(replacementFeatures);
            resultIds.addAll(FeatureBuilder.extractFeatureIds(replacementFeatures));
            changed = true;
        }

        if (!changed) {
            NotificationManager.info(
                    map,
                    "Explotar entidades",
                    "La seleccion no contiene entidades multiparte para explotar.");
            return false;
        }

        map.pushUndoSnapshotForSelectedLayer();
        map.replaceLayerFeatures(map.selectedLayer, updatedFeatures, resultIds.get(0), resultIds.size() == 1, null);
        map.applyFeatureSelection(
                map.selectedLayer,
                resultIds,
                resultIds.size() == 1,
                true,
                false,
                resultIds.size() == 1
                        ? "Entidad explotada."
                        : resultIds.size() + " entidades resultantes tras explotar."
        );
        return true;
    }

    boolean deleteSelectedFeatures() {
        if (map.selectedLayer == null) {
            return false;
        }
        if (map.isReadOnlyVectorLayer(map.selectedLayer)) {
            NotificationManager.warn(map, null, map.getReadOnlyLayerMessage(map.selectedLayer));
            return false;
        }

        ShapefileData data = map.getShapefileData(map.selectedLayer);
        List<String> selectedIds = map.getSelectedFeatureIdsForLayer(map.selectedLayer);
        if (data == null || selectedIds.isEmpty()) {
            return false;
        }

        map.pushUndoSnapshotForSelectedLayer();

        List<SimpleFeature> features = new ArrayList<>();
        for (SimpleFeature feature : data.getFeatures()) {
            if (feature != null && !selectedIds.contains(feature.getID())) {
                features.add(feature);
            }
        }

        map.replaceLayerFeatures(map.selectedLayer, features, null, false,
                selectedIds.size() == 1
                        ? "Entidad eliminada."
                        : selectedIds.size() + " entidades eliminadas.");
        return true;
    }

    boolean deleteSelectedFeature() {
        return deleteSelectedFeatures();
    }
}
