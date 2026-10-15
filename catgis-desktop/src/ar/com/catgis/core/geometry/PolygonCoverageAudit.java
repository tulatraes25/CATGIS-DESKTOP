package ar.com.catgis.core.geometry;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFilter;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

/** Planar polygon checks. No CRS conversion, GUI, files or source mutations. */
public final class PolygonCoverageAudit {
    private static final int MAX_FEATURES = 100000;
    private static final long MAX_VERTICES = 1000000L;
    private static final int MAX_CANDIDATE_PAIRS = 1000000;
    private static final int MAX_OVERLAPS = 10000;

    private PolygonCoverageAudit() {}

    public record Overlap(int firstIndex, int secondIndex, Geometry geometry) {}

    /**
     * Find only enclosed uncovered regions, not space outside a concave domain.
     * Holes in source polygons are included unless another polygon covers them.
     * This is not the OGC definition of valid coverage or narrow-gap detection.
     */
    public static Geometry enclosedGaps(List<? extends Geometry> inputs) {
        List<Geometry> polygons = snapshot(inputs);
        int srid = polygons.get(0).getSRID();
        Geometry coverage = checkedOutput(UnaryUnionOp.union(polygons), srid);
        List<Geometry> shells = new ArrayList<>();
        for (int i = 0; i < coverage.getNumGeometries(); i++) {
            checkActive();
            Polygon polygon = (Polygon) coverage.getGeometryN(i);
            shells.add(polygon.getFactory().createPolygon(
                    polygon.getExteriorRing().getCoordinates()));
        }
        Geometry domain = checkedOutput(UnaryUnionOp.union(shells), srid);
        // Subtract the complete coverage, including islands inside other holes.
        return checkedOutput(domain.difference(coverage), srid);
    }

    /** Uncovered area within an explicit polygonal study boundary, holes excluded. */
    public static Geometry uncoveredWithin(List<? extends Geometry> inputs, Geometry studyArea) {
        if (inputs == null) {
            throw new IllegalArgumentException("La lista de cobertura es obligatoria.");
        }
        if (inputs.size() >= MAX_FEATURES) {
            throw new IllegalArgumentException("La auditoria excede el limite de entidades.");
        }
        List<Geometry> combined = new ArrayList<>(inputs);
        combined.add(studyArea);
        List<Geometry> copies = snapshot(combined);
        Geometry boundary = copies.get(copies.size() - 1);
        if (copies.size() == 1) {
            return boundary;
        }
        Geometry coverage = checkedOutput(UnaryUnionOp.union(copies.subList(0, copies.size() - 1)), boundary.getSRID());
        return checkedOutput(boundary.difference(coverage), boundary.getSRID());
    }

    /** Area intersections only: a shared edge is not an overlapping area. */
    public static List<Overlap> overlaps(List<? extends Geometry> inputs) {
        List<Geometry> polygons = snapshot(inputs);
        STRtree index = new STRtree();
        for (int i = 0; i < polygons.size(); i++) {
            checkActive();
            index.insert(polygons.get(i).getEnvelopeInternal(), i);
        }
        index.build();
        List<Overlap> found = new ArrayList<>();
        int checked = 0;
        for (int i = 0; i < polygons.size(); i++) {
            checkActive();
            Geometry a = polygons.get(i);
            for (Object candidate : index.query(a.getEnvelopeInternal())) {
                checkActive();
                int j = (Integer) candidate;
                if (j <= i) {
                    continue;
                }
                if (++checked > MAX_CANDIDATE_PAIRS) {
                    throw new IllegalArgumentException("La auditoria excede el limite de pares candidatos.");
                }
                Geometry intersection = a.intersection(polygons.get(j));
                checkActive();
                double area = intersection.getArea();
                if (!Double.isFinite(area)) {
                    throw new IllegalArgumentException("La interseccion tiene area no finita.");
                }
                if (area > 0) {
                    Geometry areaGeometry = overlapArea(intersection, a.getSRID());
                    if (found.size() >= MAX_OVERLAPS) {
                        throw new IllegalArgumentException("Demasiados solapamientos; subdivida el trabajo.");
                    }
                    found.add(new Overlap(i, j, areaGeometry));
                }
            }
        }
        checkActive();
        return List.copyOf(found);
    }

    /** Keep all area components when JTS also returns boundary-only contacts. */
    private static Geometry overlapArea(Geometry intersection, int srid) {
        if (intersection instanceof Polygonal) {
            return checkedOutput(intersection, srid);
        }
        List<Polygon> areas = new ArrayList<>();
        intersection.apply((GeometryFilter) component -> {
            checkActive();
            if (component instanceof Polygon polygon && !polygon.isEmpty()) {
                areas.add(polygon);
            }
        });
        if (areas.isEmpty()) {
            throw new IllegalArgumentException("La interseccion declara area pero no contiene poligonos.");
        }
        // Do not union/buffer/repair: only separate lower-dimensional contacts
        // from the already-computed intersection, preserving the actual area.
        return checkedOutput(intersection.getFactory().buildGeometry(areas), srid);
    }

    private static List<Geometry> snapshot(List<? extends Geometry> inputs) {
        checkActive();
        if (inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException("No hay poligonos para evaluar.");
        }
        if (inputs.size() > MAX_FEATURES) {
            throw new IllegalArgumentException("La auditoria excede el limite de entidades.");
        }
        List<Geometry> copies = new ArrayList<>(inputs.size());
        long vertices = 0;
        Geometry first = null;
        for (int i = 0; i < inputs.size(); i++) {
            checkActive();
            Geometry geometry = inputs.get(i);
            if (geometry == null || !(geometry instanceof Polygonal) || geometry.isEmpty()) {
                throw new IllegalArgumentException("Geometria nula, vacia o no poligonal en indice " + i);
            }
            vertices += geometry.getNumPoints();
            if (vertices > MAX_VERTICES) {
                throw new IllegalArgumentException("La auditoria excede un millon de vertices.");
            }
            if (first != null && (first.getSRID() != geometry.getSRID()
                    || !first.getPrecisionModel().equals(geometry.getPrecisionModel()))) {
                throw new IllegalArgumentException("SRID o precision incompatibles; no se reproyecto ningun dato.");
            }
            Geometry copy = geometry.copy();
            for (Coordinate coordinate : copy.getCoordinates()) {
                if (!Double.isFinite(coordinate.x) || !Double.isFinite(coordinate.y)) {
                    throw new IllegalArgumentException("Coordenada no finita en indice " + i);
                }
            }
            if (!copy.isValid() || !Double.isFinite(copy.getArea())) {
                throw new IllegalArgumentException("Poligono invalido o area no finita en indice " + i);
            }
            first = first == null ? copy : first;
            copies.add(copy);
        }
        checkActive();
        return copies;
    }

    private static Geometry checkedOutput(Geometry geometry, int srid) {
        checkActive();
        if (geometry == null || (!geometry.isEmpty() && !(geometry instanceof Polygonal))
                || !geometry.isValid() || !Double.isFinite(geometry.getArea())
                || geometry.getNumPoints() > MAX_VERTICES) {
            throw new IllegalArgumentException("El calculo no produjo una geometria poligonal verificable.");
        }
        // A source SRID can differ from its GeometryFactory's default SRID.
        geometry.apply((GeometryFilter) component -> component.setSRID(srid));
        return geometry;
    }

    private static void checkActive() {
        if (Thread.currentThread().isInterrupted()) {
            throw new CancellationException("Auditoria cancelada; resultado no evaluado.");
        }
    }
}
