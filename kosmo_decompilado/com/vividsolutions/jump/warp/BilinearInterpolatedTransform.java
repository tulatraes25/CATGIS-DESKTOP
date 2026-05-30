/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.warp;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.warp.CoordinateTransform;
import com.vividsolutions.jump.warp.Triangle;
import java.util.Map;
import org.saig.jump.lang.I18N;

public class BilinearInterpolatedTransform
extends CoordinateTransform {
    private Map<Triangle, Triangle> triangleMap;
    private TaskMonitor monitor;
    private int coordinatesTransformed = 0;

    public BilinearInterpolatedTransform(Map<Triangle, Triangle> triangleMap, TaskMonitor monitor) {
        this.triangleMap = triangleMap;
        this.monitor = monitor;
        monitor.report(String.valueOf(I18N.getString("com.vividsolutions.jump.warp.BilinearInterpolatedTransform.transforming")) + "...");
    }

    @Override
    public Coordinate transform(Coordinate c) {
        this.monitor.report(++this.coordinatesTransformed, -1, I18N.getString("com.vividsolutions.jump.warp.BilinearInterpolatedTransform.coordinates"));
        Triangle sourceTriangle = this.sourceTriangle(c);
        Assert.isTrue((sourceTriangle != null ? 1 : 0) != 0, (String)I18N.getMessage("com.vividsolutions.jump.warp.BilinearInterpolatedTransform.unable-to-determine-source-triangle-for-{0}", new Object[]{c}));
        Triangle destTriangle = this.destTriangle(sourceTriangle);
        return destTriangle.toEuclideanCoordinate(sourceTriangle.toSimplicialCoordinate(c));
    }

    private Triangle sourceTriangle(Coordinate c) {
        for (Triangle triangle : this.triangleMap.keySet()) {
            if (!triangle.getEnvelope().contains(c) || !triangle.contains(c)) continue;
            return triangle;
        }
        return null;
    }

    private Triangle destTriangle(Triangle sourceTriangle) {
        return this.triangleMap.get(sourceTriangle);
    }
}

