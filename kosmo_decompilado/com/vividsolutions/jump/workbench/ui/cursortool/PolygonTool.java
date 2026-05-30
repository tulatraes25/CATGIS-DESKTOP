/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.operation.valid.IsValidOp
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import org.saig.jump.lang.I18N;

public abstract class PolygonTool
extends MultiClickTool {
    public PolygonTool(boolean check) {
        super(check);
        this.setCloseRing(true);
    }

    public PolygonTool() {
        this.setCloseRing(true);
    }

    protected Polygon getPolygon() throws NoninvertibleTransformException {
        ArrayList<Coordinate> closedPoints = new ArrayList<Coordinate>(this.getCoordinates());
        if (!((Coordinate)closedPoints.get(0)).equals(closedPoints.get(closedPoints.size() - 1))) {
            closedPoints.add(new Coordinate((Coordinate)closedPoints.get(0)));
        }
        return geomFac.createPolygon(geomFac.createLinearRing(this.toArray(closedPoints)), null);
    }

    protected boolean checkPolygon() throws NoninvertibleTransformException {
        if (this.getCoordinates().size() < 3) {
            this.getPanel().getContext().warnUser(I18N.getString("workbench.ui.cursortool.PolygonTool.the-polygon-must-have-at-least-3-points"));
            return false;
        }
        IsValidOp isValidOp = new IsValidOp((Geometry)this.getPolygon());
        if (!isValidOp.isValid()) {
            this.getPanel().getContext().warnUser(isValidOp.getValidationError().getMessage());
            if (this.isRollingBackInvalidEdits()) {
                return false;
            }
        }
        return true;
    }
}

