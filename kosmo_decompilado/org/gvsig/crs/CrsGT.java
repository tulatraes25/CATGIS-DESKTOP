/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IDatum
 *  org.cresques.cts.IProjection
 *  org.cresques.geo.ViewPortData
 *  org.geotools.referencing.crs.AbstractDerivedCRS
 *  org.geotools.referencing.crs.AbstractSingleCRS
 *  org.geotools.referencing.datum.DefaultGeodeticDatum
 *  org.opengis.referencing.crs.CoordinateReferenceSystem
 */
package org.gvsig.crs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IDatum;
import org.cresques.cts.IProjection;
import org.cresques.geo.ViewPortData;
import org.geotools.referencing.crs.AbstractDerivedCRS;
import org.geotools.referencing.crs.AbstractSingleCRS;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.gvsig.crs.COperation;
import org.gvsig.crs.CRSDatum;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.CrsWkt;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.Proj4;
import org.gvsig.crs.proj.CrsProj;
import org.gvsig.crs.proj.JNIBaseCrs;
import org.gvsig.crs.proj.OperationCrsException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class CrsGT
implements ICrs {
    private static final Color basicGridColor = new Color(64, 64, 64, 128);
    private CoordinateReferenceSystem crsGT = null;
    private String proj4String = null;
    private Color gridColor = basicGridColor;
    private String sourceTrParams = null;
    private String targetTrParams = null;
    private CrsProj crsProj = null;
    private CrsProj crsProjBase = null;
    private CrsWkt crsWkt = null;
    private Proj4 proj4 = null;

    public CrsGT(CoordinateReferenceSystem crsGT) {
        this.crsGT = crsGT;
    }

    @Override
    public int getCode() {
        return Integer.valueOf(this.getAbrev().split(":")[1]);
    }

    @Override
    public CrsWkt getCrsWkt() {
        if (this.crsWkt == null) {
            this.crsWkt = new CrsWkt(this.crsGT);
        }
        return this.crsWkt;
    }

    @Override
    public String getWKT() {
        return this.crsGT.toWKT();
    }

    @Override
    public void setTransformationParams(String SourceParams, String TargetParams) {
        this.sourceTrParams = SourceParams;
        this.targetTrParams = TargetParams;
    }

    @Override
    public String getSourceTransformationParams() {
        return this.sourceTrParams;
    }

    @Override
    public String getTargetTransformationParams() {
        return this.targetTrParams;
    }

    public Point2D createPoint(double x, double y) {
        return new Point2D.Double(x, y);
    }

    public void drawGrid(Graphics2D g, ViewPortData vp) {
    }

    public Point2D fromGeo(Point2D gPt, Point2D mPt) {
        return null;
    }

    public String getAbrev() {
        return ((AbstractSingleCRS)this.crsGT).getIdentifiers().iterator().next().toString();
    }

    public ICoordTrans getCT(IProjection dest) {
        block3: {
            try {
                if (dest != this) break block3;
                return null;
            }
            catch (CrsException e) {
                e.printStackTrace();
                return null;
            }
        }
        COperation operation = null;
        operation = ((ICrs)dest).getSourceTransformationParams() != null || ((ICrs)dest).getTargetTransformationParams() != null ? new COperation(this, (ICrs)dest, ((ICrs)dest).getTargetTransformationParams(), ((ICrs)dest).getSourceTransformationParams()) : new COperation(this, (ICrs)dest, this.sourceTrParams, this.targetTrParams);
        return operation;
    }

    public IDatum getDatum() {
        DefaultGeodeticDatum datumGT = (DefaultGeodeticDatum)((AbstractSingleCRS)this.crsGT).getDatum();
        CRSDatum datum = new CRSDatum(datumGT.getEllipsoid().getSemiMajorAxis(), datumGT.getEllipsoid().getInverseFlattening());
        return datum;
    }

    public Color getGridColor() {
        return this.gridColor;
    }

    public double getScale(double minX, double maxX, double width, double dpi) {
        double scale = 0.0;
        scale = (maxX - minX) * (dpi / 2.54 * 100.0) / width;
        return scale;
    }

    public double getScale(double minX, double maxX, double minY, double maxY, double width, double dpi) {
        double scale = 0.0;
        double incX = maxX - minX;
        if (!this.isProjected()) {
            double a = this.getDatum().getESemiMajorAxis();
            double invF = this.getDatum().getEIFlattening();
            double meanY = (minY + maxY) / 2.0;
            double radius = 0.0;
            if (invF == Double.POSITIVE_INFINITY) {
                radius = a;
            } else {
                double e2 = 2.0 / invF - Math.pow(1.0 / invF, 2.0);
                radius = a / Math.sqrt(1.0 - e2 * Math.pow(Math.sin(meanY * Math.PI / 180.0), 2.0)) * Math.cos(meanY * Math.PI / 180.0);
            }
            incX *= Math.PI / 180 * radius;
        }
        scale = incX * (dpi / 2.54 * 100.0) / width;
        return scale;
    }

    public boolean isProjected() {
        return this.crsGT instanceof AbstractDerivedCRS;
    }

    public void setGridColor(Color c) {
        this.gridColor = c;
    }

    public Point2D toGeo(Point2D pt) {
        if (this.isProjected()) {
            double[] x = new double[]{pt.getX()};
            double[] y = new double[]{pt.getY()};
            double[] z = new double[]{0.0};
            try {
                JNIBaseCrs.operate(x, y, z, this.getCrsProj(), this.getCrsProjBase());
            }
            catch (OperationCrsException e) {
                e.printStackTrace();
            }
            return new Point2D.Double(x[0], y[0]);
        }
        return pt;
    }

    @Override
    public String getProj4String() throws CrsException {
        if (this.proj4String == null) {
            this.proj4String = this.getProj4().exportToProj4(this.crsGT);
        }
        return this.proj4String;
    }

    public CoordinateReferenceSystem getCrsGT() {
        return this.crsGT;
    }

    @Override
    public CrsProj getCrsProj() {
        if (this.crsProj == null) {
            try {
                this.crsProj = new CrsProj(this.getProj4String());
            }
            catch (CrsException e) {
                e.printStackTrace();
            }
        }
        return this.crsProj;
    }

    private CrsProj getCrsProjBase() {
        if (this.crsProjBase == null) {
            AbstractDerivedCRS derivedCRS = (AbstractDerivedCRS)this.crsGT;
            try {
                this.crsProjBase = new CrsProj(this.getProj4().exportToProj4(derivedCRS.getBaseCRS()));
            }
            catch (CrsException e) {
                e.printStackTrace();
            }
        }
        return this.crsProjBase;
    }

    private Proj4 getProj4() {
        if (this.proj4 == null) {
            try {
                this.proj4 = new Proj4();
            }
            catch (CrsException e) {
                e.printStackTrace();
            }
        }
        return this.proj4;
    }

    public String getFullCode() {
        if (this.sourceTrParams == null && this.targetTrParams == null) {
            return this.getAbrev();
        }
        String sourceParams = "";
        String targetParams = "";
        if (this.sourceTrParams != null) {
            sourceParams = this.sourceTrParams;
        }
        if (this.targetTrParams != null) {
            targetParams = this.targetTrParams;
        }
        return String.valueOf(this.getAbrev()) + ":proj@" + sourceParams + "@" + targetParams;
    }

    public Rectangle2D getExtent(Rectangle2D extent, double scale, double wImage, double hImage, double mapUnits, double distanceUnits, double dpi) {
        double w = 0.0;
        double h = 0.0;
        double wExtent = 0.0;
        double hExtent = 0.0;
        w = wImage / dpi * 2.54;
        h = hImage / dpi * 2.54;
        wExtent = w * scale / mapUnits;
        hExtent = h * scale / mapUnits;
        double xExtent = extent.getCenterX() - wExtent / 2.0;
        double yExtent = extent.getCenterY() - hExtent / 2.0;
        Rectangle2D.Double rec = new Rectangle2D.Double(xExtent, yExtent, wExtent, hExtent);
        return rec;
    }

    public Rectangle2D getExtent(Rectangle2D extent, double scale, double wImage, double hImage, double changeUnits, double dpi) {
        return null;
    }

    @Override
    public void setTransParam(String string) {
    }

    @Override
    public String getTransParam() {
        return null;
    }

    @Override
    public boolean isTransInTarget() {
        return false;
    }

    @Override
    public void setTransInTarget(boolean targetNad) {
    }
}

