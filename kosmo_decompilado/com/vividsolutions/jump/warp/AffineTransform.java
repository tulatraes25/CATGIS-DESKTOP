/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  Jama.Matrix
 *  com.vividsolutions.jts.geom.Coordinate
 */
package com.vividsolutions.jump.warp;

import Jama.Matrix;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.warp.CoordinateTransform;

public class AffineTransform
extends CoordinateTransform {
    private Matrix a;

    public AffineTransform(Coordinate p1, Coordinate p1_) {
        Coordinate p2 = new Coordinate(p1.x + 10.0, p1.y);
        Coordinate p2_ = new Coordinate(p1_.x + 10.0, p1_.y);
        Coordinate p3 = new Coordinate(p1.x, p1.y + 10.0);
        Coordinate p3_ = new Coordinate(p1_.x, p1_.y + 10.0);
        this.initialize(p1, p1_, p2, p2_, p3, p3_);
    }

    public AffineTransform(Coordinate p1, Coordinate p1_, Coordinate p2, Coordinate p2_) {
        Coordinate p3 = AffineTransform.rotate90(p1, p2);
        Coordinate p3_ = AffineTransform.rotate90(p1_, p2_);
        this.initialize(p1, p1_, p2, p2_, p3, p3_);
    }

    public AffineTransform(Coordinate p1, Coordinate p1_, Coordinate p2, Coordinate p2_, Coordinate p3, Coordinate p3_) {
        this.initialize(p1, p1_, p2, p2_, p3, p3_);
    }

    public static Coordinate rotate90(Coordinate a, Coordinate b) {
        return new Coordinate(b.y - a.y + a.x, a.x - b.x + a.y);
    }

    private void initialize(Coordinate p1, Coordinate p1_, Coordinate p2, Coordinate p2_, Coordinate p3, Coordinate p3_) {
        double[][] Xarray = new double[][]{{p1.x, p1.y, 1.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 0.0, p1.x, p1.y, 1.0}, {p2.x, p2.y, 1.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 0.0, p2.x, p2.y, 1.0}, {p3.x, p3.y, 1.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 0.0, p3.x, p3.y, 1.0}};
        Matrix X = new Matrix((double[][])Xarray);
        double[][] x_array = new double[][]{{p1_.x}, {p1_.y}, {p2_.x}, {p2_.y}, {p3_.x}, {p3_.y}};
        Matrix x_ = new Matrix((double[][])x_array);
        this.a = X.solve(x_);
    }

    @Override
    public Coordinate transform(Coordinate c) {
        double x_ = this.a.get(0, 0) * c.x + this.a.get(1, 0) * c.y + this.a.get(2, 0);
        double y_ = this.a.get(3, 0) * c.x + this.a.get(4, 0) * c.y + this.a.get(5, 0);
        return new Coordinate(x_, y_);
    }
}

