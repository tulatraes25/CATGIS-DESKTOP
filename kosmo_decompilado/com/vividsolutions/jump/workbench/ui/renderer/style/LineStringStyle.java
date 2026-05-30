/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import javax.swing.Icon;

public abstract class LineStringStyle
implements Style {
    protected boolean enabled = true;
    protected Stroke stroke;
    protected Color lineColorWithAlpha;
    protected Color fillColorWithAlpha;

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            Assert.shouldNeverReachHere();
            return null;
        }
    }

    public LineStringStyle(String name, Icon icon) {
    }

    protected void paintGeometry(Geometry geometry, Graphics2D graphics, Viewport viewport) throws Exception {
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

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void initialize(Layer layer) {
        this.stroke = new BasicStroke(layer.getBasicStyle().getLineWidth(), 1, 1);
        this.lineColorWithAlpha = GUIUtil.alphaColor(layer.getBasicStyle().getLineColor(), layer.getBasicStyle().getAlpha());
        this.fillColorWithAlpha = GUIUtil.alphaColor(layer.getBasicStyle().getFillColor(), layer.getBasicStyle().getAlpha());
    }

    @Override
    public void paint(Feature f, Graphics2D g, Viewport viewport) throws Exception {
        this.paintGeometry(f.getGeometry(), g, viewport);
    }
}

