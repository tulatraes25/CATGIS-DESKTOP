/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Polygon
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.AbstractDecorator;
import java.awt.Graphics2D;
import javax.swing.Icon;

public abstract class LineStringDecorator
extends AbstractDecorator {
    public LineStringDecorator(String name, Icon icon) {
        super(name, icon);
    }

    @Override
    protected void paintGeometry(Geometry geometry, Graphics2D graphics, Viewport viewport) throws Exception {
        if (geometry instanceof MultiPoint) {
            return;
        }
        if (geometry instanceof MultiPolygon) {
            return;
        }
        if (geometry instanceof GeometryCollection) {
            this.paintGeometryCollection((GeometryCollection)geometry, graphics, viewport);
            return;
        }
        if (geometry instanceof Polygon) {
            this.paintPolygon((Polygon)geometry, graphics, viewport);
            return;
        }
        if (!(geometry instanceof LineString)) {
            return;
        }
        LineString lineString = (LineString)geometry;
        if (lineString.getNumPoints() < 2) {
            return;
        }
        this.paintLineString(lineString, viewport, graphics);
    }

    protected abstract void paintLineString(LineString var1, Viewport var2, Graphics2D var3) throws Exception;

    private void paintGeometryCollection(GeometryCollection gc, Graphics2D graphics, Viewport viewport) throws Exception {
        int i = 0;
        while (i < gc.getNumGeometries()) {
            this.paintGeometry(gc.getGeometryN(i), graphics, viewport);
            ++i;
        }
    }

    private void paintPolygon(Polygon polygon, Graphics2D graphics, Viewport viewport) throws Exception {
        this.paintGeometry((Geometry)polygon.getExteriorRing(), graphics, viewport);
        int i = 0;
        while (i < polygon.getNumInteriorRing()) {
            this.paintGeometry((Geometry)polygon.getInteriorRingN(i), graphics, viewport);
            ++i;
        }
    }
}

