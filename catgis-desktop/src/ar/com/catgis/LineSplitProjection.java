package ar.com.catgis;

import org.locationtech.jts.geom.Coordinate;

/**
 * Result of projecting a coordinate onto a linestring,
 * identifying the best segment and the projected point.
 * Extracted from MapPanel inner class.
 */
public class LineSplitProjection {
    final int segmentIndex;
    final Coordinate projected;
    final double distance;

    public LineSplitProjection(int segmentIndex, Coordinate projected, double distance) {
        this.segmentIndex = segmentIndex;
        this.projected = projected;
        this.distance = distance;
    }
}
