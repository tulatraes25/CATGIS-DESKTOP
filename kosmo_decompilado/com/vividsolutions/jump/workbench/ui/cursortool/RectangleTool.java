/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Polygon
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import org.saig.jump.plugin.editing.ZManager;

public abstract class RectangleTool
extends DragTool {
    protected Polygon getRectangle() throws NoninvertibleTransformException {
        Envelope e = new Envelope(this.getModelSource().x, this.getModelDestination().x, this.getModelSource().y, this.getModelDestination().y);
        if (!ZManager.isZUseActive()) {
            return geomFac.createPolygon(geomFac.createLinearRing(new Coordinate[]{new Coordinate(e.getMinX(), e.getMinY()), new Coordinate(e.getMinX(), e.getMaxY()), new Coordinate(e.getMaxX(), e.getMaxY()), new Coordinate(e.getMaxX(), e.getMinY()), new Coordinate(e.getMinX(), e.getMinY())}), null);
        }
        double z = ZManager.getActiveZ();
        return geomFac.createPolygon(geomFac.createLinearRing(new Coordinate[]{new Coordinate(e.getMinX(), e.getMinY(), z), new Coordinate(e.getMinX(), e.getMaxY(), z), new Coordinate(e.getMaxX(), e.getMaxY(), z), new Coordinate(e.getMaxX(), e.getMinY(), z), new Coordinate(e.getMinX(), e.getMinY(), z)}), null);
    }

    private Collection<Coordinate> verticesToSnap(Coordinate source, Coordinate destination) {
        ArrayList<Coordinate> verticesToSnap = new ArrayList<Coordinate>();
        verticesToSnap.add(destination);
        verticesToSnap.add(new Coordinate(source.x, destination.y));
        verticesToSnap.add(new Coordinate(destination.x, source.y));
        return verticesToSnap;
    }

    @Override
    protected void setModelDestination(Coordinate modelDestination) {
        for (Coordinate vertex : this.verticesToSnap(this.getModelSource(), modelDestination)) {
            Coordinate snappedVertex = this.snap(vertex);
            if (!this.getSnapManager().wasSnapCoordinateFound()) continue;
            this.modelDestination = CoordUtil.add(modelDestination, CoordUtil.subtract(snappedVertex, vertex));
            return;
        }
        this.modelDestination = modelDestination;
    }

    @Override
    protected void setModelSource(Coordinate modelSource) {
        this.modelSource = this.snap(modelSource);
    }
}

