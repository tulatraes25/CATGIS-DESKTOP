package ar.com.catgis;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * Measurement tool for distance and area measurement on the map.
 * Extracted from MapPanel to reduce its responsibilities.
 */
public class MeasurementTool {

    public enum MeasureMode { NONE, DISTANCE, AREA }

    private MeasureMode mode = MeasureMode.NONE;
    private final List<Coordinate> measurementPoints = new ArrayList<>();
    private double accumulatedDistance = 0;
    private boolean measurementActive = false;

    // --- Getters ---

    public MeasureMode getMode() { return mode; }
    public boolean isActive() { return measurementActive; }
    public List<Coordinate> getPoints() { return measurementPoints; }
    public double getAccumulatedDistance() { return accumulatedDistance; }

    // --- Measurement operations ---

    public void startDistanceMeasurement() {
        mode = MeasureMode.DISTANCE;
        measurementActive = true;
        measurementPoints.clear();
        accumulatedDistance = 0;
    }

    public void startAreaMeasurement() {
        mode = MeasureMode.AREA;
        measurementActive = true;
        measurementPoints.clear();
        accumulatedDistance = 0;
    }

    public void addPoint(double worldX, double worldY) {
        if (!measurementActive) return;
        Coordinate c = new Coordinate(worldX, worldY);
        if (!measurementPoints.isEmpty()) {
            Coordinate last = measurementPoints.get(measurementPoints.size() - 1);
            accumulatedDistance += Math.hypot(c.x - last.x, c.y - last.y);
        }
        measurementPoints.add(c);
    }

    public void finishMeasurement() {
        measurementActive = false;
    }

    public void cancelMeasurement() {
        mode = MeasureMode.NONE;
        measurementActive = false;
        measurementPoints.clear();
        accumulatedDistance = 0;
    }

    // --- Results ---

    public double getDistance() {
        return accumulatedDistance;
    }

    public double getArea() {
        if (mode != MeasureMode.AREA || measurementPoints.size() < 3) return 0;
        // Shoelace formula
        double area = 0;
        int n = measurementPoints.size();
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            area += measurementPoints.get(i).x * measurementPoints.get(j).y;
            area -= measurementPoints.get(j).x * measurementPoints.get(i).y;
        }
        return Math.abs(area) / 2.0;
    }

    public String formatDistance(double meters) {
        if (meters >= 1000) return String.format("%.2f km", meters / 1000);
        if (meters >= 1) return String.format("%.2f m", meters);
        return String.format("%.0f mm", meters * 1000);
    }

    public String formatArea(double sqMeters) {
        if (sqMeters >= 1000000) return String.format("%.2f km²", sqMeters / 1000000);
        if (sqMeters >= 10000) return String.format("%.2f ha", sqMeters / 10000);
        return String.format("%.2f m²", sqMeters);
    }
}
