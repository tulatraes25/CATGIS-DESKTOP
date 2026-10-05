package ar.com.catgis;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;

import ar.com.catgis.core.model.Layer;

/**
 * Context interface for screen-dependent geometry editing operations.
 * MapPanel implements this implicitly (package-private interface).
 */
interface ScreenCoordinateContext {
	int worldToScreenX(double worldX);
	int worldToScreenY(double worldY);
	Geometry getEditableDisplayGeometry(SimpleFeature feature, Layer layer);
	Coordinate[] getEditableVertexCoordinates(Geometry geometry);
	boolean isFeatureEditMode();
	SimpleFeature getSelectedFeature();
	Layer getSelectedLayer();
	String chooseMetricCRSForMeasurement(String sourceCRSCode);
}

/**
 * Stateless pure geometry builder operations extracted from MapPanel.
 * All methods operate solely on JTS types and MapGeometryUtils.
 */
class EditingGeometryOperations {

	final ScreenCoordinateContext screenCtx;

	EditingGeometryOperations() {
		this(null);
	}

	EditingGeometryOperations(ScreenCoordinateContext screenCtx) {
		this.screenCtx = screenCtx;
	}

	Coordinate[] getSegmentCoordinates(Coordinate[] coordinates, int segmentIndex) {
		if (coordinates == null || segmentIndex < 0 || segmentIndex >= coordinates.length - 1) {
			return null;
		}
		return new Coordinate[]{
				new Coordinate(coordinates[segmentIndex]),
				new Coordinate(coordinates[segmentIndex + 1])
		};
	}

	Coordinate[] getEditableSegmentCoordinates(Geometry geometry, int segmentIndex) {
		if (geometry == null || segmentIndex < 0) {
			return null;
		}

		if (geometry instanceof LineString lineString) {
			return getSegmentCoordinates(lineString.getCoordinates(), segmentIndex);
		}
		if (geometry instanceof MultiLineString multiLineString) {
			int offset = 0;
			for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
				Coordinate[] coords = ((LineString) multiLineString.getGeometryN(i)).getCoordinates();
				int segments = Math.max(0, coords.length - 1);
				if (segmentIndex >= offset && segmentIndex < offset + segments) {
					return getSegmentCoordinates(coords, segmentIndex - offset);
				}
				offset += segments;
			}
			return null;
		}
		if (geometry instanceof Polygon polygon) {
			return getSegmentCoordinates(polygon.getExteriorRing().getCoordinates(), segmentIndex);
		}
		if (geometry instanceof MultiPolygon multiPolygon) {
			int offset = 0;
			for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
				Coordinate[] coords = ((Polygon) multiPolygon.getGeometryN(i)).getExteriorRing().getCoordinates();
				int segments = Math.max(0, coords.length - 1);
				if (segmentIndex >= offset && segmentIndex < offset + segments) {
					return getSegmentCoordinates(coords, segmentIndex - offset);
				}
				offset += segments;
			}
		}

		return null;
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

	Polygon buildAdjacentPolygonAlongSegment(GeometryFactory factory,
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

	Geometry buildGeometryWithMovedVertex(Geometry geometry, int vertexIndex, Coordinate newCoordinate) {
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

	Geometry buildGeometryWithAddedVertex(Geometry geometry, int segmentIndex, Coordinate newCoordinate) {
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

	Geometry buildGeometryWithRemovedVertex(Geometry geometry, int vertexIndex) {
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
					if (visibleVertices <= 3) {
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

	Geometry buildGeometryWithRemovedVertices(Geometry geometry, List<Integer> vertexIndexes) {
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

	Geometry buildGeometryWithJoinedVertices(Geometry geometry, int targetVertexIndex, List<Integer> vertexIndexes) {
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

	Geometry buildLineStringWithJoinedVertices(LineString line, int targetVertexIndex, Collection<Integer> joinIndexes) {
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

	Geometry buildPolygonWithJoinedVertices(Polygon polygon, int targetVertexIndex, Collection<Integer> joinIndexes) {
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

	Geometry buildMultiLineStringWithJoinedVertices(MultiLineString multi, int targetVertexIndex, Collection<Integer> joinIndexes) {
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

	Geometry buildMultiPolygonWithJoinedVertices(MultiPolygon multi, int targetVertexIndex, Collection<Integer> joinIndexes) {
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

	Geometry buildCutGeometryAtPoint(Geometry geometry, Coordinate coordinate) {
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

	Geometry splitLineStringAtCoordinate(LineString line, Coordinate coordinate) {
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

	Geometry buildCutGeometryWithSketch(Geometry geometry, List<Coordinate> sketchCoordinates) {
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
					List<Polygon> splitPolygons = MapGeometryUtils.collectPolygons(split);
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

	Geometry splitPolygonWithBlade(Polygon polygon, List<Coordinate> sketchCoordinates) {
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

			return MapGeometryUtils.assemblePolygons(parts, factory);
		} catch (Exception ex) {
			return null;
		}
	}

	Geometry buildGeometryWithHole(Geometry geometry, List<Coordinate> sketchCoordinates) {
		if (geometry == null || sketchCoordinates == null || sketchCoordinates.size() < 3) {
			return null;
		}

		GeometryFactory factory = geometry.getFactory();
		Polygon holePolygon = MapGeometryUtils.buildPolygonFromCoordinates(sketchCoordinates, factory);
		if (holePolygon == null) {
			return null;
		}

		if (geometry instanceof Polygon) {
			Polygon polygon = (Polygon) geometry;
			if (!polygon.covers(holePolygon)) {
				return null;
			}
			return MapGeometryUtils.normalizePolygonalGeometry(polygon.difference(holePolygon), factory);
		}

		if (geometry instanceof MultiPolygon) {
			MultiPolygon multi = (MultiPolygon) geometry;
			for (int i = 0; i < multi.getNumGeometries(); i++) {
				Polygon polygon = (Polygon) multi.getGeometryN(i);
				if (polygon.covers(holePolygon)) {
					Geometry diff = MapGeometryUtils.normalizePolygonalGeometry(polygon.difference(holePolygon), factory);
					List<Polygon> pieces = MapGeometryUtils.collectPolygons(diff);
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

	// ---- Screen-dependent finder methods (require ScreenCoordinateContext) ----

	int findEditableVertexIndex(int screenX, int screenY) {
		if (screenCtx == null || !screenCtx.isFeatureEditMode()
				|| screenCtx.getSelectedFeature() == null || screenCtx.getSelectedLayer() == null) {
			return -1;
		}

		Geometry geometry = screenCtx.getEditableDisplayGeometry(
				screenCtx.getSelectedFeature(), screenCtx.getSelectedLayer());
		Coordinate[] vertices = screenCtx.getEditableVertexCoordinates(geometry);
		if (vertices == null || vertices.length == 0) {
			return -1;
		}

		for (int i = 0; i < vertices.length; i++) {
			Coordinate c = vertices[i];
			int vx = screenCtx.worldToScreenX(c.x);
			int vy = screenCtx.worldToScreenY(c.y);
			double distance = Math.hypot(screenX - vx, screenY - vy);
			if (distance <= MapPanel.EDIT_VERTEX_TOLERANCE_PX) {
				return i;
			}
		}

		return -1;
	}

	List<Integer> collectEditableVertexIndexes(Rectangle selectionBounds) {
		List<Integer> indexes = new ArrayList<>();
		if (screenCtx == null || !screenCtx.isFeatureEditMode()
				|| screenCtx.getSelectedFeature() == null || screenCtx.getSelectedLayer() == null
				|| selectionBounds == null) {
			return indexes;
		}

		Geometry geometry = screenCtx.getEditableDisplayGeometry(
				screenCtx.getSelectedFeature(), screenCtx.getSelectedLayer());
		Coordinate[] vertices = screenCtx.getEditableVertexCoordinates(geometry);
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
			int vx = screenCtx.worldToScreenX(c.x);
			int vy = screenCtx.worldToScreenY(c.y);
			if (expanded.contains(vx, vy)) {
				indexes.add(i);
			}
		}
		return indexes;
	}

	int findEditableSegmentIndex(Geometry geometry, int screenX, int screenY) {
		if (geometry == null) {
			return -1;
		}

		if (geometry instanceof LineString) {
			return findNearestSegmentInCoordinates(((LineString) geometry).getCoordinates(), screenX, screenY, 0);
		}
		if (geometry instanceof MultiLineString) {
			int offset = 0;
			int bestIndex = -1;
			double bestDistance = Double.MAX_VALUE;
			MultiLineString multi = (MultiLineString) geometry;
			for (int i = 0; i < multi.getNumGeometries(); i++) {
				Coordinate[] coords = ((LineString) multi.getGeometryN(i)).getCoordinates();
				int local = findNearestSegmentInCoordinates(coords, screenX, screenY, offset);
				if (local >= 0) {
					double distance = distanceToSegmentIndex(coords, screenX, screenY, local - offset);
					if (distance < bestDistance) {
						bestDistance = distance;
						bestIndex = local;
					}
				}
				offset += Math.max(0, coords.length - 1);
			}
			return bestDistance <= 14.0 ? bestIndex : -1;
		}
		if (geometry instanceof Polygon) {
			return findNearestSegmentInCoordinates(((Polygon) geometry).getExteriorRing().getCoordinates(), screenX, screenY, 0);
		}
		if (geometry instanceof MultiPolygon) {
			int offset = 0;
			int bestIndex = -1;
			double bestDistance = Double.MAX_VALUE;
			MultiPolygon multi = (MultiPolygon) geometry;
			for (int i = 0; i < multi.getNumGeometries(); i++) {
				Coordinate[] coords = ((Polygon) multi.getGeometryN(i)).getExteriorRing().getCoordinates();
				int local = findNearestSegmentInCoordinates(coords, screenX, screenY, offset);
				if (local >= 0) {
					double distance = distanceToSegmentIndex(coords, screenX, screenY, local - offset);
					if (distance < bestDistance) {
						bestDistance = distance;
						bestIndex = local;
					}
				}
				offset += Math.max(0, coords.length - 1);
			}
			return bestDistance <= 14.0 ? bestIndex : -1;
		}

		return -1;
	}

	LineSplitProjection findEditableSegmentProjection(Geometry geometry, Coordinate target,
	                                                   int screenX, int screenY, double maxDistancePx) {
		if (geometry == null || target == null) {
			return null;
		}

		if (geometry instanceof LineString) {
			return projectLineSegmentProjection((LineString) geometry, target, screenX, screenY, maxDistancePx, 0);
		}
		if (geometry instanceof MultiLineString) {
			int offset = 0;
			LineSplitProjection best = null;
			MultiLineString multi = (MultiLineString) geometry;
			for (int i = 0; i < multi.getNumGeometries(); i++) {
				LineString line = (LineString) multi.getGeometryN(i);
				LineSplitProjection candidate = projectLineSegmentProjection(line, target, screenX, screenY, maxDistancePx, offset);
				if (candidate != null && (best == null || candidate.distance < best.distance)) {
					best = candidate;
				}
				offset += Math.max(0, line.getCoordinates().length - 1);
			}
			return best;
		}
		if (geometry instanceof Polygon) {
			LineString ring = ((Polygon) geometry).getExteriorRing();
			return projectLineSegmentProjection(ring, target, screenX, screenY, maxDistancePx, 0);
		}
		if (geometry instanceof MultiPolygon) {
			int offset = 0;
			LineSplitProjection best = null;
			MultiPolygon multi = (MultiPolygon) geometry;
			for (int i = 0; i < multi.getNumGeometries(); i++) {
				Polygon polygon = (Polygon) multi.getGeometryN(i);
				LineString ring = polygon.getExteriorRing();
				LineSplitProjection candidate = projectLineSegmentProjection(ring, target, screenX, screenY, maxDistancePx, offset);
				if (candidate != null && (best == null || candidate.distance < best.distance)) {
					best = candidate;
				}
				offset += Math.max(0, ring.getCoordinates().length - 1);
			}
			return best;
		}

		return null;
	}

	LineSplitProjection projectLineSegmentProjection(LineString line, Coordinate target,
	                                                  int screenX, int screenY, double maxDistancePx, int baseIndex) {
		LineSplitProjection projection = MapGeometryUtils.projectCoordinateOntoLine(line, target);
		if (projection == null || projection.projected == null) {
			return null;
		}

		double distancePx = Math.hypot(
				screenCtx.worldToScreenX(projection.projected.x) - screenX,
				screenCtx.worldToScreenY(projection.projected.y) - screenY);
		if (distancePx > maxDistancePx) {
			return null;
		}

		return new LineSplitProjection(baseIndex + projection.segmentIndex, projection.projected, distancePx);
	}

	int findNearestSegmentInCoordinates(Coordinate[] coords, int screenX, int screenY, int baseIndex) {
		if (coords == null || coords.length < 2) {
			return -1;
		}

		double bestDistance = Double.MAX_VALUE;
		int bestIndex = -1;
		for (int i = 0; i < coords.length - 1; i++) {
			double distance = pointToSegmentDistance(
					screenX, screenY,
					screenCtx.worldToScreenX(coords[i].x), screenCtx.worldToScreenY(coords[i].y),
					screenCtx.worldToScreenX(coords[i + 1].x), screenCtx.worldToScreenY(coords[i + 1].y)
			);
			if (distance < bestDistance) {
				bestDistance = distance;
				bestIndex = baseIndex + i;
			}
		}
		return bestDistance <= 14.0 ? bestIndex : -1;
	}

	double distanceToSegmentIndex(Coordinate[] coords, int screenX, int screenY, int localIndex) {
		if (coords == null || localIndex < 0 || localIndex >= coords.length - 1) {
			return Double.MAX_VALUE;
		}
		return pointToSegmentDistance(
				screenX, screenY,
				screenCtx.worldToScreenX(coords[localIndex].x), screenCtx.worldToScreenY(coords[localIndex].y),
				screenCtx.worldToScreenX(coords[localIndex + 1].x), screenCtx.worldToScreenY(coords[localIndex + 1].y)
		);
	}

	static double pointToSegmentDistance(double px, double py, double x1, double y1, double x2, double y2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		if (dx == 0 && dy == 0) {
			return Math.hypot(px - x1, py - y1);
		}
		double t = ((px - x1) * dx + (py - y1) * dy) / ((dx * dx) + (dy * dy));
		t = Math.max(0, Math.min(1, t));
		double projX = x1 + (t * dx);
		double projY = y1 + (t * dy);
		return Math.hypot(px - projX, py - projY);
	}

	Geometry buildBufferedPolygonGeometry(Geometry geometry, Layer layer, double distance) {
		if (geometry == null || geometry.isEmpty()) {
			return null;
		}
		if (!(geometry instanceof Polygon) && !(geometry instanceof MultiPolygon)) {
			return null;
		}

		String sourceCode = layer != null ? layer.getSourceCRS() : "";
		GeometryFactory factory = geometry.getFactory();
		Geometry working = (Geometry) geometry.copy();
		String metricCode = screenCtx != null ? screenCtx.chooseMetricCRSForMeasurement(sourceCode) : sourceCode;
		boolean reprojectBack = sourceCode != null
				&& !sourceCode.isBlank()
				&& metricCode != null
				&& !metricCode.isBlank()
				&& !sourceCode.equalsIgnoreCase(metricCode);

		if (reprojectBack) {
			working = reprojectGeometry(working, sourceCode, metricCode);
		}
		if (working == null || working.isEmpty()) {
			return null;
		}

		Geometry buffered;
		try {
			buffered = working.buffer(distance);
		} catch (Exception ex) {
			return null;
		}
		if (buffered == null || buffered.isEmpty()) {
			return null;
		}

		if (reprojectBack) {
			buffered = reprojectGeometry(buffered, metricCode, sourceCode);
		}

		return MapGeometryUtils.normalizePolygonalGeometry(buffered, factory);
	}

	Geometry reprojectGeometry(Geometry geometry, String sourceCode, String targetCode) {
		try {
			if (geometry == null || geometry.isEmpty()) {
				return geometry;
			}
			if (sourceCode == null || sourceCode.isBlank() || targetCode == null || targetCode.isBlank()) {
				return geometry;
			}
			if (sourceCode.equalsIgnoreCase(targetCode)) {
				return geometry;
			}

			CoordinateReferenceSystem sourceCRS = CRSDefinitions.decode(sourceCode, true);
			CoordinateReferenceSystem targetCRS = CRSDefinitions.decode(targetCode, true);
			MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
			return JTS.transform(geometry, transform);
		} catch (Exception ex) {
			return null;
		}
	}

	Geometry buildAdjustedSelectedLineGeometry(Geometry geometry, Coordinate targetCoordinate, boolean extend, boolean fromStart) {
		if (geometry == null || targetCoordinate == null) {
			return null;
		}

		Coordinate[] baseCoordinates = MapGeometryUtils.extractContinuableLineCoordinates(geometry);
		if (baseCoordinates == null || baseCoordinates.length < 2) {
			return null;
		}

		Coordinate[] updatedCoordinates = MapGeometryUtils.cloneCoordinates(baseCoordinates);
		int endpointIndex = fromStart ? 0 : updatedCoordinates.length - 1;
		int anchorIndex = fromStart ? 1 : updatedCoordinates.length - 2;
		Coordinate endpoint = updatedCoordinates[endpointIndex];
		Coordinate anchor = updatedCoordinates[anchorIndex];
		double dx = endpoint.x - anchor.x;
		double dy = endpoint.y - anchor.y;
		double lengthSquared = (dx * dx) + (dy * dy);
		if (lengthSquared <= 0.0000001) {
			return null;
		}

		double factor = ((targetCoordinate.x - endpoint.x) * dx + (targetCoordinate.y - endpoint.y) * dy) / lengthSquared;
		if (extend) {
			if (factor <= 0.02) {
				return null;
			}
		} else if (factor >= -0.02 || factor <= -0.98) {
			return null;
		}

		updatedCoordinates[endpointIndex] = new Coordinate(
				endpoint.x + (dx * factor),
				endpoint.y + (dy * factor)
		);
		GeometryFactory factory = geometry.getFactory() != null ? geometry.getFactory() : new GeometryFactory();
		return factory.createLineString(updatedCoordinates);
	}

	Geometry buildParallelLineGeometry(Coordinate segmentStart, Coordinate segmentEnd, Coordinate sideCoordinate) {
		if (segmentStart == null || segmentEnd == null || sideCoordinate == null) {
			return null;
		}

		double dx = segmentEnd.x - segmentStart.x;
		double dy = segmentEnd.y - segmentStart.y;
		double length = Math.hypot(dx, dy);
		if (length <= 0.0000001) {
			return null;
		}

		double nx = -dy / length;
		double ny = dx / length;
		Coordinate midpoint = new Coordinate(
				(segmentStart.x + segmentEnd.x) / 2.0,
				(segmentStart.y + segmentEnd.y) / 2.0
		);
		double offset = ((sideCoordinate.x - midpoint.x) * nx) + ((sideCoordinate.y - midpoint.y) * ny);
		if (Math.abs(offset) <= 0.0000001) {
			return null;
		}

		GeometryFactory factory = new GeometryFactory();
		return factory.createLineString(new Coordinate[]{
				new Coordinate(segmentStart.x + (nx * offset), segmentStart.y + (ny * offset)),
				new Coordinate(segmentEnd.x + (nx * offset), segmentEnd.y + (ny * offset))
		});
	}

	Geometry buildPerpendicularLineGeometry(Coordinate segmentStart, Coordinate segmentEnd, Coordinate targetCoordinate) {
		if (segmentStart == null || segmentEnd == null || targetCoordinate == null) {
			return null;
		}

		double dx = segmentEnd.x - segmentStart.x;
		double dy = segmentEnd.y - segmentStart.y;
		double lengthSquared = (dx * dx) + (dy * dy);
		if (lengthSquared <= 0.0000001) {
			return null;
		}

		double factor = ((targetCoordinate.x - segmentStart.x) * dx + (targetCoordinate.y - segmentStart.y) * dy) / lengthSquared;
		Coordinate foot = new Coordinate(segmentStart.x + (dx * factor), segmentStart.y + (dy * factor));
		if (foot.distance(targetCoordinate) <= 0.0000001) {
			return null;
		}

		GeometryFactory factory = new GeometryFactory();
		return factory.createLineString(new Coordinate[]{foot, new Coordinate(targetCoordinate)});
	}

	/**
	 * Returns the label anchor coordinate for a geometry.
	 * Pure JTS — no screen, layer, or MapPanel dependencies.
	 */
	Coordinate getLabelCoordinate(Geometry geometry) {
		if (geometry == null || geometry.isEmpty()) {
			return null;
		}

		if (geometry instanceof Point) {
			return ((Point) geometry).getCoordinate();
		}

		if (geometry instanceof MultiPoint) {
			MultiPoint mp = (MultiPoint) geometry;
			if (mp.getNumGeometries() > 0 && mp.getGeometryN(0) instanceof Point) {
				return ((Point) mp.getGeometryN(0)).getCoordinate();
			}
		}

		if (geometry instanceof Polygon) {
			Point p = ((Polygon) geometry).getInteriorPoint();
			if (p != null) {
				return p.getCoordinate();
			}
			return geometry.getCentroid().getCoordinate();
		}

		if (geometry instanceof MultiPolygon) {
			Point p = geometry.getInteriorPoint();
			if (p != null) {
				return p.getCoordinate();
			}
			return geometry.getCentroid().getCoordinate();
		}

		if (geometry instanceof LineString || geometry instanceof MultiLineString) {
			Point p = geometry.getCentroid();
			if (p != null) {
				return p.getCoordinate();
			}
		}

		Point centroid = geometry.getCentroid();
		if (centroid != null) {
			return centroid.getCoordinate();
		}

		return null;
	}
}
