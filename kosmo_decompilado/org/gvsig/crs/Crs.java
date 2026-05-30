/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IDatum
 *  org.cresques.cts.IProjection
 *  org.cresques.geo.ViewPortData
 */
package org.gvsig.crs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IDatum;
import org.cresques.cts.IProjection;
import org.cresques.geo.ViewPortData;
import org.gvsig.crs.COperation;
import org.gvsig.crs.CRSDatum;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.CrsWkt;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.Proj4;
import org.gvsig.crs.proj.CrsProj;
import org.gvsig.crs.proj.JNIBaseCrs;

public class Crs
implements ICrs {
    private static final Logger LOGGER = Logger.getLogger(Crs.class);
    private static final Color basicGridColor = new Color(64, 64, 64, 128);
    private Proj4 proj4;
    private String proj4String;
    private String trans;
    private String name = "";
    private String abrev;
    private CrsProj crsProj4;
    private CrsProj crsBase = null;
    private CrsWkt crsWkt;
    private int epsg_code = 23030;
    boolean targetNad = false;
    String sourceTrParams = null;
    String targetTrParams = null;
    String wkt = null;
    Color gridColor = basicGridColor;
    CRSDatum datum = null;
    String TransParam = "";

    public Crs(int epsgCode, int aut) throws CrsException {
        String strEpsgCode = "";
        if (aut == 1) {
            strEpsgCode = "EPSG:" + epsgCode;
        } else if (aut == 2) {
            strEpsgCode = "ESRI:" + epsgCode;
        } else if (aut == 3) {
            strEpsgCode = "IAU2000:" + epsgCode;
        } else if (aut == 4) {
            strEpsgCode = "USR:" + epsgCode;
        } else {
            System.out.println("Error, autorithy err\u00f3neo");
        }
        this.crsWkt = new CrsWkt(strEpsgCode);
        this.setWKT(this.crsWkt.getWkt());
    }

    public Crs(String code) throws CrsException {
        this.setWKT(code);
        String fullCode = code.charAt(0) == 'E' || code.charAt(0) == 'G' || code.charAt(0) == 'P' ? code : "EPSG:" + code;
        String cod = "";
        if (code.length() < 15) {
            code = code.substring(code.indexOf(":") + 1);
            try {
                this.crsWkt = new CrsWkt(fullCode);
                this.setName(fullCode);
                this.setAbrev(fullCode);
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            String code2 = "";
            int i = 0;
            while (i < code.length()) {
                String aux = "" + code.charAt(i);
                code2 = !aux.equals(" ") ? String.valueOf(code2) + aux : String.valueOf(code2);
                ++i;
            }
            this.crsWkt = new CrsWkt(code2);
            this.setName(fullCode);
            this.setAbrev(String.valueOf(this.crsWkt.getAuthority()[0]) + ":" + this.crsWkt.getAuthority()[1]);
        }
        if (!this.crsWkt.getSpheroid()[1].equals("") && !this.crsWkt.getSpheroid()[2].equals("")) {
            double eSemiMajorAxis = Double.valueOf(this.crsWkt.getSpheroid()[1]);
            double eIFlattening = Double.valueOf(this.crsWkt.getSpheroid()[2]);
            this.datum = new CRSDatum(eSemiMajorAxis, eIFlattening);
        }
    }

    public Crs(int epsg_cod, String code) throws CrsException {
        this.setWKT(code);
        this.setCode(epsg_cod);
        String fullCode = code != null || code.charAt(0) == 'E' || code.charAt(0) == 'G' || code.charAt(0) == 'P' ? code : "EPSG:" + code;
        String cod = "";
        if (code.length() < 15) {
            code = code.substring(code.indexOf(":") + 1);
            try {
                this.crsWkt = new CrsWkt(fullCode);
                this.setName(fullCode);
                this.setAbrev(fullCode);
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            String code2 = "";
            int i = 0;
            while (i < code.length()) {
                String aux = "" + code.charAt(i);
                code2 = !aux.equals(" ") ? String.valueOf(code2) + aux : String.valueOf(code2);
                ++i;
            }
            this.crsWkt = new CrsWkt(code2);
            if (!this.crsWkt.getProjection().equals("Equidistant_Cylindrical") && !this.crsWkt.getProjection().equals("Oblique_Cylindrical_Equal_Area")) {
                this.setName(fullCode);
                this.setAbrev(String.valueOf(this.crsWkt.getAuthority()[0]) + ":" + this.crsWkt.getAuthority()[1]);
            } else if (this.crsWkt.getProjection().equals("Equidistant_Cylindrical")) {
                String spheroid1 = this.crsWkt.getSpheroid()[1];
                String spheroid2 = this.crsWkt.getSpheroid()[2];
                String centralMeridian = "";
                String falseNorthing = "";
                String falseEasting = "";
                String standardParallel1 = "0.0";
                int i2 = 0;
                while (i2 < this.crsWkt.getParam_name().length) {
                    if (this.crsWkt.getParam_name()[i2].equals("Central_Meridian")) {
                        centralMeridian = this.crsWkt.getParam_value()[i2];
                    }
                    if (this.crsWkt.getParam_name()[i2].equals("False_Easting")) {
                        falseEasting = this.crsWkt.getParam_value()[i2];
                    }
                    if (this.crsWkt.getParam_name()[i2].equals("False_Northing")) {
                        falseNorthing = this.crsWkt.getParam_value()[i2];
                    }
                    if (this.crsWkt.getParam_name()[i2].equals("Standard_Parallel_1")) {
                        standardParallel1 = this.crsWkt.getParam_value()[i2];
                    }
                    ++i2;
                }
                this.proj4String = spheroid2.equals("0.0") ? "+proj=eqc +a=" + spheroid1 + " +lon_0=" + centralMeridian + " +x_0=" + falseEasting + " +y_0=" + falseNorthing + " +lat_ts=" + standardParallel1 : "+proj=eqc +a=" + spheroid1 + " +rf=" + spheroid2 + " +lon_0=" + centralMeridian + " +x_0=" + falseEasting + " +y_0=" + falseNorthing + " +lat_ts=" + standardParallel1;
                this.setName(fullCode);
                this.setAbrev(String.valueOf(this.crsWkt.getAuthority()[0]) + ":" + this.crsWkt.getAuthority()[1]);
            } else if (this.crsWkt.getProjection().equals("Oblique_Cylindrical_Equal_Area")) {
                String spheroid1 = this.crsWkt.getSpheroid()[1];
                String spheroid2 = this.crsWkt.getSpheroid()[2];
                String centralMeridian = "";
                String falseNorthing = "";
                String falseEasting = "";
                String standardParallel1 = "";
                String standardParallel2 = "0.0";
                int i3 = 0;
                while (i3 < this.crsWkt.getParam_name().length) {
                    if (this.crsWkt.getParam_name()[i3].equals("Central_Meridian")) {
                        centralMeridian = this.crsWkt.getParam_value()[i3];
                    }
                    if (this.crsWkt.getParam_name()[i3].equals("False_Easting")) {
                        falseEasting = this.crsWkt.getParam_value()[i3];
                    }
                    if (this.crsWkt.getParam_name()[i3].equals("False_Northing")) {
                        falseNorthing = this.crsWkt.getParam_value()[i3];
                    }
                    if (this.crsWkt.getParam_name()[i3].equals("Standard_Parallel_1")) {
                        standardParallel1 = this.crsWkt.getParam_value()[i3];
                    }
                    if (this.crsWkt.getParam_name()[i3].equals("Standard_Parallel_2")) {
                        standardParallel2 = this.crsWkt.getParam_value()[i3];
                    }
                    ++i3;
                }
                this.proj4String = spheroid2.equals("0.0") ? "+proj=ocea +a=" + spheroid1 + " +lon_0=" + centralMeridian + " +x_0=" + falseEasting + " +y_0=" + falseNorthing + " +lat_1=" + standardParallel1 + " +lat_2=" + standardParallel2 + " +lon_1=long_1" + " +lon_2=long_2 +no_defs" : "+proj=ocea +a=" + spheroid1 + " +rf=" + spheroid2 + " +lon_0=" + centralMeridian + " +x_0=" + falseEasting + " +y_0=" + falseNorthing + " +lat_1=" + standardParallel1 + " +lat_2=" + standardParallel2 + " +lon_1=long_1" + " +lon_2=long_2 +no_defs";
                this.setName(fullCode);
                this.setAbrev(String.valueOf(this.crsWkt.getAuthority()[0]) + ":" + this.crsWkt.getAuthority()[1]);
            }
        }
        if (!this.crsWkt.getSpheroid()[1].equals("") && !this.crsWkt.getSpheroid()[2].equals("")) {
            double eSemiMajorAxis = Double.valueOf(this.crsWkt.getSpheroid()[1]);
            double eIFlattening = Double.valueOf(this.crsWkt.getSpheroid()[2]);
            this.datum = new CRSDatum(eSemiMajorAxis, eIFlattening);
        }
    }

    public Crs(int epsg_cod, String code, String params) throws CrsException {
        this.setCode(epsg_cod);
        this.setWKT(code);
        this.setTransformationParams(params, null);
        String fullCode = code.charAt(0) == 'E' || code.charAt(0) == 'G' || code.charAt(0) == 'P' ? code : "EPSG:" + code;
        String cod = "";
        if (code.length() < 15) {
            code = code.substring(code.indexOf(":") + 1);
            try {
                this.crsWkt = new CrsWkt(fullCode);
                this.setName(fullCode);
                this.setAbrev(fullCode);
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            String code2 = "";
            int i = 0;
            while (i < code.length()) {
                String aux = "" + code.charAt(i);
                code2 = !aux.equals(" ") ? String.valueOf(code2) + aux : String.valueOf(code2);
                ++i;
            }
            this.crsWkt = new CrsWkt(code2);
            this.setName(fullCode);
            this.setAbrev(String.valueOf(this.crsWkt.getAuthority()[0]) + ":" + this.crsWkt.getAuthority()[1]);
        }
        if (!this.crsWkt.getSpheroid()[1].equals("") && !this.crsWkt.getSpheroid()[2].equals("")) {
            double eSemiMajorAxis = Double.valueOf(this.crsWkt.getSpheroid()[1]);
            double eIFlattening = Double.valueOf(this.crsWkt.getSpheroid()[2]);
            this.datum = new CRSDatum(eSemiMajorAxis, eIFlattening);
        }
    }

    public void setName(String nom) {
        this.name = nom;
    }

    public void setTrans(String code) {
        this.trans = code;
        this.changeTrans(this.trans);
    }

    public void changeTrans(String code) {
        this.getCrsProj().changeStrCrs(code);
    }

    public String getAbrev() {
        return this.abrev;
    }

    protected void setAbrev(String code) {
        this.abrev = code;
    }

    public IDatum getDatum() {
        return this.datum;
    }

    @Override
    public CrsWkt getCrsWkt() {
        return this.crsWkt;
    }

    public void setWKT(String wkt) {
        this.wkt = wkt;
    }

    @Override
    public String getWKT() {
        return this.wkt;
    }

    @Override
    public void setTransParam(String param) {
        this.TransParam = param;
    }

    @Override
    public String getTransParam() {
        return this.TransParam;
    }

    @Override
    public CrsProj getCrsProj() {
        if (this.crsProj4 == null) {
            try {
                this.crsProj4 = new CrsProj(this.getProj4String());
            }
            catch (CrsException e) {
                e.printStackTrace();
            }
        }
        return this.crsProj4;
    }

    public Point2D createPoint(double x, double y) {
        return new Point2D.Double(x, y);
    }

    public void drawGrid(Graphics2D g, ViewPortData vp) {
    }

    public void setGridColor(Color c) {
        this.gridColor = c;
    }

    public Color getGridColor() {
        return this.gridColor;
    }

    public ICoordTrans getCT(IProjection dest) {
        block3: {
            try {
                if (dest != this) break block3;
                return null;
            }
            catch (CrsException e) {
                LOGGER.error((Object)"", (Throwable)e);
                return null;
            }
        }
        COperation operation = null;
        operation = ((ICrs)dest).getSourceTransformationParams() != null || ((ICrs)dest).getTargetTransformationParams() != null ? new COperation(this, (ICrs)dest, ((ICrs)dest).getTargetTransformationParams(), ((ICrs)dest).getSourceTransformationParams()) : new COperation(this, (ICrs)dest, this.sourceTrParams, this.targetTrParams);
        return operation;
    }

    public Point2D toGeo(Point2D pt) {
        if (this.isProjected()) {
            double[] x = new double[]{pt.getX()};
            double[] y = new double[]{pt.getY()};
            double[] z = new double[]{0.0};
            try {
                JNIBaseCrs.operate(x, y, z, this.getCrsProj(), this.getCrsBase());
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            return new Point2D.Double(x[0], y[0]);
        }
        return pt;
    }

    public Point2D fromGeo(Point2D gPt, Point2D mPt) {
        return null;
    }

    public boolean isProjected() {
        return !this.getCrsProj().isLatlong();
    }

    public double getScale(double minX, double maxX, double width, double dpi) {
        double scale = 0.0;
        scale = !this.isProjected() ? (maxX - minX) * (dpi / 2.54 * 100.0 * 1852.0 * 60.0) / width : (maxX - minX) * (dpi / 2.54 * 100.0) / width;
        return scale;
    }

    public void setCode(int epsg_cod) {
        this.epsg_code = epsg_cod;
    }

    @Override
    public int getCode() {
        return this.epsg_code;
    }

    public Rectangle2D getExtent(Rectangle2D extent, double scale, double wImage, double hImage, double changeUnits, double dpi) {
        double w = 0.0;
        double h = 0.0;
        double wExtent = 0.0;
        double hExtent = 0.0;
        if (this.isProjected()) {
            w = wImage / dpi * 2.54;
            h = hImage / dpi * 2.54;
            wExtent = w * scale / changeUnits;
            hExtent = h * scale / changeUnits;
        } else {
            w = wImage / dpi * 2.54;
            h = hImage / dpi * 2.54;
            wExtent = w * scale / (changeUnits * 1852.0 * 60.0);
            hExtent = h * scale / (changeUnits * 1852.0 * 60.0);
        }
        double xExtent = extent.getCenterX() - wExtent / 2.0;
        double yExtent = extent.getCenterY() - hExtent / 2.0;
        Rectangle2D.Double rec = new Rectangle2D.Double(xExtent, yExtent, wExtent, hExtent);
        return rec;
    }

    @Override
    public boolean isTransInTarget() {
        return this.targetNad;
    }

    @Override
    public void setTransInTarget(boolean targetNad) {
        this.targetNad = targetNad;
    }

    public String getFullCode() {
        if (!this.TransParam.equals("")) {
            if (this.isTransInTarget()) {
                return String.valueOf(this.getAbrev()) + ":" + "proj@" + this.TransParam + "@1";
            }
            return String.valueOf(this.getAbrev()) + ":" + "proj@" + this.TransParam + "@0";
        }
        return this.getAbrev();
    }

    public CrsProj getCrsBase() throws CrsException {
        if (this.crsBase == null) {
            this.crsBase = new CrsProj(this.getProj4String());
        }
        return this.crsBase;
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

    @Override
    public String getProj4String() throws CrsException {
        if (this.proj4String == null) {
            this.proj4String = this.getProj4().exportToProj4(this);
        }
        return this.proj4String;
    }

    @Override
    public void setTransformationParams(String sourceParams, String targetParams) {
        this.sourceTrParams = sourceParams;
        this.targetTrParams = targetParams;
    }

    @Override
    public String getSourceTransformationParams() {
        return this.sourceTrParams;
    }

    @Override
    public String getTargetTransformationParams() {
        return this.targetTrParams;
    }

    public String getTransformationParams() {
        return this.sourceTrParams;
    }
}

