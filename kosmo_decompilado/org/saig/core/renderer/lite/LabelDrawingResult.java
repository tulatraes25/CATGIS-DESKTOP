/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.Geometry;
import java.util.ArrayList;
import java.util.List;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;

public class LabelDrawingResult {
    protected List<Geometry> anchorPoints = new ArrayList<Geometry>();
    protected LabelDrawingResultType type;

    public LabelDrawingResult() {
        this((Geometry)null, (LabelDrawingResultType)null);
    }

    public LabelDrawingResult(LabelDrawingResultType drawingResultType) {
        this((Geometry)null, drawingResultType);
    }

    public LabelDrawingResult(IShape shape, LabelDrawingResultType type) {
        this(LabelDrawingResult.toGeometry(shape), type);
    }

    public LabelDrawingResult(Geometry g, LabelDrawingResultType drawingResultType) {
        if (g != null) {
            this.anchorPoints.add(g);
        }
        this.type = drawingResultType;
    }

    private static Geometry toGeometry(IShape shape) {
        return ShapeGeometryConverter.java2d_to_jts(shape);
    }

    public List<Geometry> getAnchorPoints() {
        return this.anchorPoints;
    }

    public void setAnchorPoints(List<Geometry> anchorPoints) {
        this.anchorPoints = anchorPoints;
    }

    public void addAnchorPoint(Geometry g) {
        if (g != null) {
            this.anchorPoints.add(g);
        }
    }

    public LabelDrawingResultType getType() {
        return this.type;
    }

    public void setType(LabelDrawingResultType type) {
        this.type = type;
    }

    public static enum LabelDrawingResultType {
        SUCCEDED,
        OVERLAPPED,
        OUTSCREEN;

    }
}

