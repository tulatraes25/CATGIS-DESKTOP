package ar.com.catgis;

import com.uber.h3core.AreaUnit;
import com.uber.h3core.H3Core;
import com.uber.h3core.util.LatLng;
import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * H3 hexagonal indexing service using the uber/h3 library.
 */
public final class H3Service {

    private H3Service() {}

    private static volatile H3Core h3;
    private static final GeometryFactory GF = new GeometryFactory();

    public record HexBin(String hexIndex, int count, double centerLat, double centerLng) {}

    private static H3Core h3() {
        if (h3 == null) {
            synchronized (H3Service.class) {
                if (h3 == null) {
                    try {
                        h3 = H3Core.newInstance();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to initialize H3 core", e);
                    }
                }
            }
        }
        return h3;
    }

    /** Convert lat/lng to H3 cell address. */
    public static String latLngToCell(double lat, double lng, int resolution) {
        return h3().latLngToCellAddress(lat, lng, clampRes(resolution));
    }

    /** Get cell boundary as JTS Polygon. */
    public static Polygon cellToBoundary(String h3Address) {
        List<LatLng> boundary = h3().cellToBoundary(h3Address);
        if (boundary == null || boundary.isEmpty()) return null;

        Coordinate[] coords = new Coordinate[boundary.size() + 1];
        for (int i = 0; i < boundary.size(); i++) {
            coords[i] = new Coordinate(boundary.get(i).lng, boundary.get(i).lat);
        }
        coords[boundary.size()] = new Coordinate(coords[0].x, coords[0].y);
        return GF.createPolygon(coords);
    }

    /** Get cell center (lat, lng). */
    public static double[] cellToLatLng(String h3Address) {
        LatLng center = h3().cellToLatLng(h3Address);
        return new double[]{center.lat, center.lng};
    }

    /** Ring of cells at distance k. */
    public static List<String> gridDisk(String h3Address, int k) {
        return h3().gridDisk(h3Address, clampRing(k));
    }

    /** Grid distance between cells. */
    public static long gridDistance(String a, String b) {
        return h3().gridDistance(a, b);
    }

    /** Cells covering a polygon envelope. */
    public static List<String> polygonToCells(Geometry polygon, int resolution) {
        if (polygon == null) return List.of();
        int res = clampRes(resolution);
        Envelope env = polygon.getEnvelopeInternal();
        List<String> cells = new ArrayList<>();

        double cellAreaKm2 = h3().getHexagonAreaAvg(res, AreaUnit.km2);
        double stepDegrees = Math.sqrt(cellAreaKm2) / 111.0;

        for (double lat = env.getMinY(); lat <= env.getMaxY(); lat += stepDegrees) {
            for (double lng = env.getMinX(); lng <= env.getMaxX(); lng += stepDegrees * 1.5) {
                String cell = latLngToCell(lat, lng, res);
                if (!cells.contains(cell)) cells.add(cell);
            }
        }
        return cells;
    }

    /** Bin points into hexagonal cells. */
    public static List<HexBin> hexBin(List<SimpleFeature> points, int resolution) {
        Map<String, Integer> counts = new HashMap<>();
        Map<String, double[]> centers = new HashMap<>();
        int res = clampRes(resolution);

        for (SimpleFeature f : points) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g == null) continue;
            Point p = g instanceof Point pt ? pt : g.getCentroid();
            String cell = latLngToCell(p.getY(), p.getX(), res);
            counts.merge(cell, 1, Integer::sum);
            centers.computeIfAbsent(cell, k -> {
                LatLng gc = h3().cellToLatLng(k);
                return new double[]{gc.lat, gc.lng};
            });
        }

        List<HexBin> bins = new ArrayList<>();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            double[] c = centers.get(e.getKey());
            bins.add(new HexBin(e.getKey(), e.getValue(),
                    c != null ? c[0] : 0, c != null ? c[1] : 0));
        }
        return bins;
    }

    /** Parent at coarser resolution. */
    public static String cellToParent(String h3Address, int parentRes) {
        return h3().cellToParentAddress(h3Address, clampRes(parentRes));
    }

    /** Children at finer resolution. */
    public static List<String> cellToChildren(String h3Address, int childRes) {
        return h3().cellToChildren(h3Address, clampRes(childRes));
    }

    /** Cell area in km². */
    public static double cellAreaKm2(String h3Address) {
        return h3().getHexagonAreaAvg(h3().getResolution(h3Address), AreaUnit.km2);
    }

    /** Validate H3 address. */
    public static boolean isValidCell(String h3Address) {
        return h3().isValidCell(h3Address);
    }

    /** Get cell resolution. */
    public static int getResolution(String h3Address) {
        return h3().getResolution(h3Address);
    }

    private static int clampRes(int r) { return Math.max(0, Math.min(15, r)); }
    private static int clampRing(int k) { return Math.max(0, Math.min(k, 10)); }
}
