/*
 * Decompiled with CFR 0.152.
 */
package com.iver.cit.gvsig.fmap.core.styles;

import com.iver.cit.gvsig.fmap.core.styles.ParallelLinesCannotBeResolvedException;
import java.awt.geom.Point2D;

class LineEquation {
    double theta;
    double m;
    double x;
    double y;
    double xEnd;
    double yEnd;

    public LineEquation(double theta, double x, double y, double xEnd, double yEnd) {
        this.theta = theta;
        this.m = Math.tan(theta);
        this.x = x;
        this.y = y;
        this.xEnd = xEnd;
        this.yEnd = yEnd;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public Point2D resolve(LineEquation otherLine) throws ParallelLinesCannotBeResolvedException {
        double Y;
        double X;
        if (Math.abs(this.x - this.xEnd) < 1.0E-5) {
            X = this.xEnd;
            if (Math.abs(otherLine.x - otherLine.xEnd) < 1.0E-5) {
                if (!(Math.abs(this.x - otherLine.x) < 1.0E-5)) throw new ParallelLinesCannotBeResolvedException(this, otherLine);
                Y = otherLine.y;
                return new Point2D.Double(X, Y);
            } else {
                Y = Math.abs(otherLine.y - otherLine.yEnd) < 1.0E-5 ? otherLine.y : otherLine.m * (X - otherLine.x) + otherLine.y;
            }
            return new Point2D.Double(X, Y);
        } else if (Math.abs(this.y - this.yEnd) < 1.0E-5) {
            Y = this.yEnd;
            if (Math.abs(otherLine.y - otherLine.yEnd) < 1.0E-5) {
                if (!(Math.abs(this.y - otherLine.y) < 1.0E-5)) throw new ParallelLinesCannotBeResolvedException(this, otherLine);
                X = otherLine.x;
                return new Point2D.Double(X, Y);
            } else {
                X = Math.abs(otherLine.x - otherLine.xEnd) < 1.0E-5 ? otherLine.x : (Y - otherLine.y) / otherLine.m + otherLine.x;
            }
            return new Point2D.Double(X, Y);
        } else if (Math.abs(otherLine.y - otherLine.yEnd) < 1.0E-5) {
            Y = otherLine.y;
            X = (Y - this.y) / this.m + this.x;
            return new Point2D.Double(X, Y);
        } else if (Math.abs(otherLine.x - otherLine.xEnd) < 1.0E-5) {
            X = otherLine.x;
            Y = this.m * (X - this.x) + this.y;
            return new Point2D.Double(X, Y);
        } else if (Math.abs(otherLine.m - this.m) < 1.0E-5) {
            Y = otherLine.m * (this.x - otherLine.x) + otherLine.y;
            if (!(Math.abs(this.y - Y) < 1.0E-5)) throw new ParallelLinesCannotBeResolvedException(this, otherLine);
            X = otherLine.x;
            Y = otherLine.y;
            return new Point2D.Double(X, Y);
        } else {
            double mTimesX = this.m * this.x;
            X = (mTimesX - this.y - otherLine.m * otherLine.x + otherLine.y) / (this.m - otherLine.m);
            Y = this.m * X - mTimesX + this.y;
        }
        return new Point2D.Double(X, Y);
    }

    public String toString() {
        return "Y - " + this.y + " = " + this.m + "*(X - " + this.x + ")";
    }
}

