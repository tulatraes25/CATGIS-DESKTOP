/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateSequence
 *  com.vividsolutions.jts.geom.LineString
 */
package es.kosmo.core.renderer.label;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineString;
import java.util.Arrays;

public class LineStringCursor {
    static final double ONE_DEGREE = Math.PI / 180;
    LineString lineString;
    CoordinateSequence coords;
    int segment;
    double offsetDistance;
    double[] segmentLenghts;
    double[] segmentStartOrdinate;
    double[] segmentAngles;

    public LineStringCursor(LineString ls) {
        this.lineString = ls;
        this.coords = ls.getCoordinateSequence();
        this.segment = 0;
        this.offsetDistance = 0.0;
        int coordsCount = this.coords.size();
        this.segmentLenghts = new double[coordsCount - 1];
        this.segmentStartOrdinate = new double[coordsCount - 1];
        this.segmentStartOrdinate[0] = 0.0;
        Coordinate c1 = new Coordinate();
        Coordinate c2 = new Coordinate();
        c2.x = this.coords.getX(0);
        c2.y = this.coords.getY(0);
        int i = 1;
        while (i < coordsCount) {
            double distance;
            c1.x = c2.x;
            c1.y = c2.y;
            c2.x = this.coords.getX(i);
            c2.y = this.coords.getY(i);
            this.segmentLenghts[i - 1] = distance = c1.distance(c2);
            if (i < this.coords.size() - 1) {
                this.segmentStartOrdinate[i] = this.segmentStartOrdinate[i - 1] + distance;
            }
            ++i;
        }
        this.segmentAngles = new double[this.segmentLenghts.length];
        Arrays.fill(this.segmentAngles, Double.NaN);
    }

    public LineStringCursor(LineStringCursor cursor) {
        this.lineString = cursor.lineString;
        this.coords = cursor.coords;
        this.segment = cursor.segment;
        this.offsetDistance = cursor.offsetDistance;
        this.segmentLenghts = cursor.segmentLenghts;
        this.segmentStartOrdinate = cursor.segmentStartOrdinate;
        this.segmentAngles = cursor.segmentAngles;
    }

    public double getLineStringLength() {
        return this.segmentStartOrdinate[this.coords.size() - 2] + this.segmentLenghts[this.coords.size() - 2];
    }

    public void moveTo(double ordinate) {
        double position = 0.0;
        if (ordinate < 0.0) {
            this.segment = 0;
            this.offsetDistance = 0.0;
        } else if (ordinate > this.getLineStringLength()) {
            this.segment = this.segmentLenghts.length - 1;
            this.offsetDistance = this.segmentLenghts[this.segment];
        } else {
            int i = 0;
            while (i < this.segmentLenghts.length) {
                double length = this.segmentLenghts[i];
                if (ordinate < length + position) {
                    this.segment = i;
                    this.offsetDistance = ordinate - position;
                    break;
                }
                position += length;
                ++i;
            }
        }
    }

    /*
     * Unable to fully structure code
     */
    public boolean moveRelative(double offset) {
        block7: {
            if (offset == 0.0) {
                return true;
            }
            if (!(offset > 0.0)) ** GOTO lbl25
            while (offset > 0.0) {
                if (this.offsetDistance + offset <= this.segmentLenghts[this.segment]) {
                    this.offsetDistance += offset;
                    return true;
                }
                if (this.segment == this.segmentLenghts.length - 1) {
                    this.offsetDistance = this.segmentLenghts[this.segment];
                    return false;
                }
                offset -= this.segmentLenghts[this.segment] - this.offsetDistance;
                this.offsetDistance = 0.0;
                ++this.segment;
            }
            break block7;
lbl-1000:
            // 1 sources

            {
                if (this.offsetDistance + offset >= 0.0) {
                    this.offsetDistance += offset;
                    return true;
                }
                if (this.segment == 0) {
                    this.offsetDistance = 0.0;
                    return false;
                }
                offset += this.offsetDistance;
                --this.segment;
                this.offsetDistance = this.segmentLenghts[this.segment];
lbl25:
                // 2 sources

                ** while (offset < 0.0)
            }
        }
        throw new RuntimeException("You have stumbled into a software bug, the code should never get here. Please report with a reproducable test case");
    }

    public Coordinate getCurrentPosition() {
        return this.getCurrentPosition(new Coordinate());
    }

    public Coordinate getCurrentPosition(Coordinate c) {
        c.setCoordinate(this.coords.getCoordinate(this.segment));
        if (this.offsetDistance > 0.0) {
            double angle = this.getCurrentAngle();
            c.x += this.offsetDistance * Math.cos(angle);
            c.y += this.offsetDistance * Math.sin(angle);
        }
        return c;
    }

    public double getCurrentOrdinate() {
        return this.segmentStartOrdinate[this.segment] + this.offsetDistance;
    }

    public double getCurrentAngle() {
        return this.getSegmentAngle(this.segment);
    }

    protected double getSegmentAngle(int segmentIdx) {
        if (Double.isNaN(this.segmentAngles[segmentIdx])) {
            double dx = this.coords.getX(segmentIdx + 1) - this.coords.getX(segmentIdx);
            double dy = this.coords.getY(segmentIdx + 1) - this.coords.getY(segmentIdx);
            this.segmentAngles[segmentIdx] = Math.atan2(dy, dx);
        }
        return this.segmentAngles[segmentIdx];
    }

    public double getLabelOrientation() {
        double dx = this.coords.getX(this.segment + 1) - this.coords.getX(this.segment);
        double dy = this.coords.getY(this.segment + 1) - this.coords.getY(this.segment);
        double slope = dy / dx;
        double angle = Math.atan(slope);
        if (Math.abs(angle - 1.5707963267948966) < Math.PI / 180) {
            angle = -1.5707963267948966 + Math.abs(angle - 1.5707963267948966);
        }
        return angle;
    }

    public double getMaxAngleChange(double startOrdinate, double endOrdinate) {
        if (startOrdinate > endOrdinate) {
            throw new IllegalArgumentException("Invalid arguments, endOrdinate < starOrdinate");
        }
        LineStringCursor delegate = new LineStringCursor(this);
        delegate.moveTo(startOrdinate);
        int startSegment = delegate.segment;
        delegate.moveTo(endOrdinate);
        int endSegment = delegate.segment;
        if (startSegment == endSegment) {
            return 0.0;
        }
        double maxDifference = 0.0;
        double prevAngle = this.getSegmentAngle(startSegment);
        int i = startSegment + 1;
        while (i <= endSegment) {
            double currAngle = this.getSegmentAngle(i);
            double difference = Math.abs(currAngle - prevAngle);
            if (difference > maxDifference) {
                maxDifference = difference;
            }
            prevAngle = currAngle;
            ++i;
        }
        return maxDifference;
    }

    public LineStringCursor reverse() {
        return new LineStringCursor((LineString)this.lineString.reverse());
    }

    public LineString getLineString() {
        return this.lineString;
    }

    public LineString getSubLineString(double startOrdinate, double endOrdinate) {
        LineStringCursor clone = new LineStringCursor(this);
        clone.moveTo(startOrdinate);
        int startSegment = clone.segment;
        Coordinate start = clone.getCurrentPosition();
        clone.moveTo(endOrdinate);
        int endSegment = clone.segment;
        Coordinate end = clone.getCurrentPosition();
        Coordinate[] subCoords = new Coordinate[endSegment - startSegment + 2];
        subCoords[0] = start;
        int i = startSegment;
        while (i < endSegment) {
            subCoords[i - startSegment + 1] = this.coords.getCoordinate(i + 1);
            ++i;
        }
        subCoords[subCoords.length - 1] = end;
        return this.lineString.getFactory().createLineString(subCoords);
    }
}

