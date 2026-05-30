/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IProjection
 *  org.cresques.geo.Point3D
 *  org.cresques.geo.Projected
 *  org.cresques.geo.ViewPortData
 *  org.cresques.px.Extent
 *  org.cresques.px.Extent$Has
 *  org.cresques.px.IObjList
 *  org.cresques.px.dxf.DxfBlock
 *  org.cresques.px.dxf.DxfCalXtru
 *  org.cresques.px.dxf.DxfEntityList
 *  org.cresques.px.dxf.DxfLayer
 *  org.cresques.px.dxf.DxfTable
 *  org.cresques.px.dxf.DxfTableItem
 *  org.cresques.px.gml.Feature
 *  org.cresques.px.gml.FeatureCollection
 *  org.cresques.px.gml.Geometry
 *  org.cresques.px.gml.InsPoint3D
 *  org.cresques.px.gml.LineString
 *  org.cresques.px.gml.LineString3D
 *  org.cresques.px.gml.Point3D
 *  org.cresques.px.gml.Polygon
 */
package org.cresques.px.dxf;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.cresques.geo.Projected;
import org.cresques.geo.ViewPortData;
import org.cresques.io.DxfFile;
import org.cresques.io.DxfGroup;
import org.cresques.io.DxfGroupVector;
import org.cresques.px.Extent;
import org.cresques.px.IObjList;
import org.cresques.px.dxf.DxfBlock;
import org.cresques.px.dxf.DxfCalArcs;
import org.cresques.px.dxf.DxfCalXtru;
import org.cresques.px.dxf.DxfConvTexts;
import org.cresques.px.dxf.DxfEntityList;
import org.cresques.px.dxf.DxfLayer;
import org.cresques.px.dxf.DxfTable;
import org.cresques.px.dxf.DxfTableItem;
import org.cresques.px.gml.Feature;
import org.cresques.px.gml.FeatureCollection;
import org.cresques.px.gml.Geometry;
import org.cresques.px.gml.InsPoint3D;
import org.cresques.px.gml.LineString;
import org.cresques.px.gml.LineString3D;
import org.cresques.px.gml.Point3D;
import org.cresques.px.gml.Polygon;
import org.cresques.px.gml.Polygon3D;
import org.saig.jump.lang.I18N;

public class DxfFeatureMaker
implements DxfFile.EntityFactory,
Projected {
    private static final Logger LOGGER = Logger.getLogger(DxfFeatureMaker.class);
    protected IProjection proj = null;
    protected Feature lastFeaBordes = null;
    protected Feature lastFeaFondos = null;
    protected boolean isDoubleFeatured = false;
    protected FeatureCollection features = null;
    protected double bulge = 0.0;
    protected double xtruX = 0.0;
    protected double xtruY = 0.0;
    protected double xtruZ = 1.0;
    protected int polylineFlag = 0;
    protected org.cresques.geo.Point3D firstPt = new org.cresques.geo.Point3D();
    protected org.cresques.geo.Point3D ptAnterior = null;
    protected boolean addingToBlock = false;
    protected int iterator = 0;
    protected FeatureCollection blk = null;
    protected Vector<FeatureCollection> blkList = null;
    protected DxfTable layers = null;
    protected Vector<int[]> faces = null;
    protected boolean hasFaces = false;
    protected int facesIterador = 1;
    protected Point2D facesFirstPoint = null;
    protected Vector<String[]> attributes = null;
    protected boolean constantPolylineElevation;
    protected double lastVertexElevation;
    protected boolean dxf3DFile;
    private Map<String, Set<Feature>> blockNameToComponentsMap = new HashMap<String, Set<Feature>>();

    public DxfFeatureMaker(IProjection proj) {
        this.proj = proj;
        this.layers = new DxfTable();
        this.features = new FeatureCollection(proj);
        this.blkList = new Vector();
        this.attributes = new Vector();
        this.dxf3DFile = false;
    }

    @Override
    public void setAddingToBlock(boolean a) {
        this.addingToBlock = a;
    }

    @Override
    public void createLayer(DxfGroupVector v) throws Exception {
        int color = v.getDataAsInt(62);
        DxfLayer layer = new DxfLayer(v.getDataAsString(2), Math.abs(v.getDataAsInt(62)));
        if (color < 0) {
            layer.isOff = true;
        }
        layer.lType = v.getDataAsString(6);
        layer.setFlags(v.getDataAsInt(70));
        if ((layer.flags & 1) == 1) {
            layer.frozen = true;
        }
        if ((layer.flags & 2) == 2) {
            layer.frozen = true;
        }
        LOGGER.debug((Object)("LAYER " + layer.getName() + " color=" + layer.getColor()));
        this.layers.add((DxfTableItem)layer);
    }

    @Override
    public void createPolyline(DxfGroupVector grp) throws Exception {
        String string;
        Double doub;
        LineString3D lineString3D = new LineString3D();
        Polygon3D polygon3D = new Polygon3D();
        Feature feaBordes = new Feature();
        Feature feaFondos = new Feature();
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        int flags = 0;
        this.constantPolylineElevation = true;
        this.faces = new Vector();
        feaBordes.setProp("dxfEntity", "Polyline");
        feaFondos.setProp("dxfEntity", "Polyline");
        if (grp.hasCode(8)) {
            feaBordes.setProp("layer", grp.getDataAsString(8));
            feaFondos.setProp("layer", grp.getDataAsString(8));
        }
        if (grp.hasCode(39)) {
            doub = new Double(grp.getDataAsDouble(39));
            string = doub.toString();
            feaBordes.setProp("thickness", string);
            feaFondos.setProp("thickness", string);
        } else {
            doub = new Double(0.0);
            feaBordes.setProp("thickness", doub.toString());
            feaFondos.setProp("thickness", doub.toString());
        }
        if (grp.hasCode(62)) {
            Integer integer = new Integer(grp.getDataAsInt(62));
            string = integer.toString();
            feaBordes.setProp("color", string);
            feaFondos.setProp("color", string);
            feaBordes.setProp("colorByLayer", "false");
            feaFondos.setProp("colorByLayer", "false");
        } else {
            DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
            int clr = layer.colorNumber;
            Integer integer = new Integer(clr);
            String string2 = integer.toString();
            feaBordes.setProp("color", string2);
            feaFondos.setProp("color", string2);
            feaBordes.setProp("colorByLayer", "true");
            feaFondos.setProp("colorByLayer", "true");
        }
        if (grp.hasCode(10)) {
            x = grp.getDataAsDouble(10);
        }
        if (grp.hasCode(20)) {
            y = grp.getDataAsDouble(20);
        }
        if (grp.hasCode(30)) {
            z = grp.getDataAsDouble(30);
            if (z != 0.0) {
                this.dxf3DFile = true;
            }
            doub = new Double(z);
            String string3 = doub.toString();
            feaBordes.setProp("elevation", string3);
            feaFondos.setProp("elevation", string3);
        } else {
            doub = new Double(0.0);
            feaBordes.setProp("elevation", doub.toString());
            feaFondos.setProp("elevation", doub.toString());
        }
        if (grp.hasCode(70)) {
            flags = grp.getDataAsInt(70);
        }
        if (grp.hasCode(210)) {
            this.xtruX = grp.getDataAsDouble(210);
        }
        if (grp.hasCode(220)) {
            this.xtruY = grp.getDataAsDouble(220);
        }
        if (grp.hasCode(230)) {
            this.xtruZ = grp.getDataAsDouble(230);
        }
        if ((flags & 1) == 1 || (flags & 0x40) == 64) {
            feaBordes.setGeometry((Geometry)lineString3D);
            feaFondos.setGeometry((Geometry)polygon3D);
            this.lastFeaBordes = feaBordes;
            this.lastFeaFondos = feaFondos;
            this.isDoubleFeatured = true;
        } else if ((flags & 1) == 0) {
            feaBordes.setGeometry((Geometry)lineString3D);
            this.lastFeaBordes = feaBordes;
            this.isDoubleFeatured = false;
        } else {
            LOGGER.debug((Object)I18N.getString("org.cresques.px.dxf.DxfFeatureMaker.polyline-flag-detected-not-corresponding-to-normal-polyline-nor-a-closed-polyline"));
        }
    }

    @Override
    public void endSeq() throws Exception {
        if (this.isDoubleFeatured) {
            if (this.lastFeaBordes.getGeometry() instanceof LineString3D) {
                Feature feaBordes = this.lastFeaBordes;
                Feature feaFondos = this.lastFeaFondos;
                LineString3D lineString3D = (LineString3D)feaBordes.getGeometry();
                Polygon3D polygon3D = (Polygon3D)feaFondos.getGeometry();
                lineString3D.add(this.firstPt);
                if (this.bulge != 0.0 && (lineString3D.get(lineString3D.pointNr() - 2).getX() != lineString3D.get(lineString3D.pointNr() - 1).getX() || lineString3D.get(lineString3D.pointNr() - 2).getY() != lineString3D.get(lineString3D.pointNr() - 1).getY())) {
                    org.cresques.geo.Point3D ptAux3D;
                    double z;
                    Point2D ptAux;
                    int i;
                    Vector arc = DxfFeatureMaker.createArc(new Point2D.Double(lineString3D.get(lineString3D.pointNr() - 2).getX(), lineString3D.get(lineString3D.pointNr() - 2).getY()), new Point2D.Double(lineString3D.get(lineString3D.pointNr() - 1).getX(), lineString3D.get(lineString3D.pointNr() - 1).getY()), this.bulge);
                    lineString3D.remove(lineString3D.pointNr() - 1);
                    lineString3D.remove(lineString3D.pointNr() - 1);
                    polygon3D.remove(lineString3D.pointNr() - 1);
                    polygon3D.remove(lineString3D.pointNr() - 1);
                    if (this.bulge > 0.0) {
                        i = 0;
                        while (i < arc.size()) {
                            ptAux = new Point2D.Double();
                            z = ((org.cresques.geo.Point3D)lineString3D.get(lineString3D.pointNr() - 2)).getZ();
                            ptAux = this.proj.createPoint(((Point2D)arc.get(i)).getX(), ((Point2D)arc.get(i)).getY());
                            ptAux3D = new org.cresques.geo.Point3D(ptAux.getX(), ptAux.getY(), z);
                            lineString3D.add(ptAux3D);
                            polygon3D.add(ptAux3D);
                            if (lineString3D.pointNr() == 1) {
                                this.firstPt = ptAux3D;
                            }
                            ++i;
                        }
                    } else {
                        i = arc.size() - 1;
                        while (i >= 0) {
                            ptAux = new Point2D.Double();
                            z = ((org.cresques.geo.Point3D)lineString3D.get(lineString3D.pointNr() - 2)).getZ();
                            ptAux = this.proj.createPoint(((Point2D)arc.get(i)).getX(), ((Point2D)arc.get(i)).getY());
                            ptAux3D = new org.cresques.geo.Point3D(ptAux.getX(), ptAux.getY(), z);
                            lineString3D.add(ptAux3D);
                            polygon3D.add(ptAux3D);
                            if (lineString3D.pointNr() == 1 || polygon3D.pointNr() == 1) {
                                this.firstPt = ptAux3D;
                            }
                            --i;
                        }
                    }
                    this.bulge = 0.0;
                }
                if (this.hasFaces) {
                    int i;
                    LineString ls1 = new LineString();
                    Polygon pl1 = new Polygon();
                    LineString ls2 = new LineString();
                    Polygon pl2 = new Polygon();
                    LineString ls = new LineString();
                    Polygon pl = new Polygon();
                    for (int[] face : this.faces) {
                        int i0 = face[3];
                        i = 0;
                        while (i < 4) {
                            int i1 = face[i];
                            if (i0 > 0) {
                                if (this.facesIterador % 2 != 0) {
                                    ls1.add(lineString3D.get(i0 - 1));
                                    pl1.add(polygon3D.get(i0 - 1));
                                } else {
                                    ls2.add(lineString3D.get(i0 - 1));
                                    pl2.add(polygon3D.get(i0 - 1));
                                }
                                ++this.facesIterador;
                            }
                            i0 = i1;
                            ++i;
                        }
                    }
                    this.facesFirstPoint = new Point2D.Double(ls1.get(0).getX(), ls1.get(0).getY());
                    i = 0;
                    while (i < ls1.pointNr()) {
                        ls.add(ls1.get(i));
                        pl.add(pl1.get(i));
                        ++i;
                    }
                    i = ls2.pointNr() - 1;
                    while (i > 0) {
                        ls.add(ls2.get(i));
                        pl.add(pl2.get(i));
                        --i;
                    }
                    ls.add(this.facesFirstPoint);
                    pl.add(this.facesFirstPoint);
                    this.lastFeaBordes.setGeometry((Geometry)ls);
                    this.lastFeaFondos.setGeometry((Geometry)pl);
                } else {
                    this.lastFeaBordes.setGeometry((Geometry)lineString3D);
                    this.lastFeaFondos.setGeometry((Geometry)polygon3D);
                }
                this.completeAttributes(this.lastFeaBordes);
                this.completeAttributes(this.lastFeaFondos);
                this.setPolylineElevation(this.lastFeaBordes, this.lastFeaFondos);
                if (!this.addingToBlock) {
                    this.features.add((Extent.Has)this.lastFeaFondos);
                } else {
                    this.blk.add((Extent.Has)this.lastFeaFondos);
                }
                this.lastFeaBordes = null;
                this.lastFeaFondos = null;
            } else if (this.lastFeaBordes.getGeometry() instanceof InsPoint3D) {
                this.copyAttributes(this.lastFeaBordes);
                this.gestionaInsert(this.lastFeaBordes);
                if (!this.addingToBlock) {
                    this.features.add((Extent.Has)this.lastFeaFondos);
                } else {
                    this.blk.add((Extent.Has)this.lastFeaBordes);
                }
                this.lastFeaBordes = null;
                this.lastFeaFondos = null;
            }
        } else if (this.lastFeaBordes.getGeometry() instanceof LineString3D) {
            Feature feaBordes = this.lastFeaBordes;
            LineString3D lineString3D = (LineString3D)feaBordes.getGeometry();
            if (this.bulge != 0.0 && (lineString3D.get(lineString3D.pointNr() - 2).getX() != lineString3D.get(lineString3D.pointNr() - 1).getX() || lineString3D.get(lineString3D.pointNr() - 2).getY() != lineString3D.get(lineString3D.pointNr() - 1).getY())) {
                org.cresques.geo.Point3D ptAux3D;
                double z;
                Point2D ptAux;
                int i;
                Vector arc = DxfFeatureMaker.createArc(new Point2D.Double(lineString3D.get(lineString3D.pointNr() - 2).getX(), lineString3D.get(lineString3D.pointNr() - 2).getY()), new Point2D.Double(lineString3D.get(lineString3D.pointNr() - 1).getX(), lineString3D.get(lineString3D.pointNr() - 1).getY()), this.bulge);
                lineString3D.remove(lineString3D.pointNr() - 1);
                lineString3D.remove(lineString3D.pointNr() - 1);
                if (this.bulge > 0.0) {
                    i = 0;
                    while (i < arc.size()) {
                        ptAux = new Point2D.Double();
                        z = 0.0;
                        if (lineString3D.pointNr() >= 2) {
                            z = ((org.cresques.geo.Point3D)lineString3D.get(lineString3D.pointNr() - 2)).getZ();
                        }
                        ptAux = this.proj.createPoint(((Point2D)arc.get(i)).getX(), ((Point2D)arc.get(i)).getY());
                        ptAux3D = new org.cresques.geo.Point3D(ptAux.getX(), ptAux.getY(), z);
                        lineString3D.add(ptAux3D);
                        if (lineString3D.pointNr() == 1) {
                            this.firstPt = ptAux3D;
                        }
                        ++i;
                    }
                } else {
                    i = arc.size() - 1;
                    while (i >= 0) {
                        ptAux = new Point2D.Double();
                        z = 0.0;
                        if (lineString3D.pointNr() >= 2) {
                            z = ((org.cresques.geo.Point3D)lineString3D.get(lineString3D.pointNr() - 2)).getZ();
                        }
                        ptAux = this.proj.createPoint(((Point2D)arc.get(i)).getX(), ((Point2D)arc.get(i)).getY());
                        ptAux3D = new org.cresques.geo.Point3D(ptAux.getX(), ptAux.getY(), z);
                        lineString3D.add(ptAux3D);
                        if (lineString3D.pointNr() == 1) {
                            this.firstPt = ptAux3D;
                        }
                        --i;
                    }
                }
                this.bulge = 0.0;
            }
            if (this.hasFaces) {
                int i;
                LineString ls1 = new LineString();
                LineString ls2 = new LineString();
                LineString ls = new LineString();
                for (int[] face : this.faces) {
                    int i0 = face[3];
                    i = 0;
                    while (i < 4) {
                        int i1 = face[i];
                        if (i0 > 0) {
                            if (this.facesIterador % 2 != 0) {
                                ls1.add(lineString3D.get(i0 - 1));
                            } else {
                                ls2.add(lineString3D.get(i0 - 1));
                            }
                            ++this.facesIterador;
                        }
                        i0 = i1;
                        ++i;
                    }
                }
                this.facesFirstPoint = new Point2D.Double(ls1.get(0).getX(), ls1.get(0).getY());
                i = 0;
                while (i < ls1.pointNr()) {
                    ls.add(ls1.get(i));
                    ++i;
                }
                i = ls2.pointNr() - 1;
                while (i > 0) {
                    ls.add(ls2.get(i));
                    --i;
                }
                ls.add(this.facesFirstPoint);
                this.lastFeaBordes.setGeometry((Geometry)ls);
            } else {
                this.lastFeaBordes.setGeometry((Geometry)lineString3D);
            }
            this.completeAttributes(this.lastFeaBordes);
            this.setPolylineElevation(this.lastFeaBordes);
            if (!this.addingToBlock) {
                this.features.add((Extent.Has)this.lastFeaBordes);
            } else {
                this.blk.add((Extent.Has)this.lastFeaBordes);
            }
            this.lastFeaBordes = null;
        }
        this.xtruX = 0.0;
        this.xtruY = 0.0;
        this.xtruZ = 1.0;
        this.bulge = 0.0;
        this.isDoubleFeatured = false;
        this.hasFaces = false;
        this.facesIterador = 1;
    }

    private void setPolylineElevation(Feature feaBordes) {
        if (this.constantPolylineElevation) {
            Double doub = new Double(this.lastVertexElevation);
            String string = doub.toString();
            feaBordes.setProp("elevation", string);
        } else {
            Double doub = new Double(0.0);
            String string = doub.toString();
            feaBordes.setProp("elevation", string);
        }
    }

    private void setPolylineElevation(Feature feaBordes, Feature feaFondos) {
        if (this.constantPolylineElevation) {
            Double doub = new Double(this.lastVertexElevation);
            String string = doub.toString();
            feaBordes.setProp("elevation", string);
            feaFondos.setProp("elevation", string);
        } else {
            Double doub = new Double(0.0);
            String string = doub.toString();
            feaBordes.setProp("elevation", string);
            feaFondos.setProp("elevation", string);
        }
    }

    @Override
    public void addVertex(DxfGroupVector grp) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        int vFlags = 0;
        if (this.isDoubleFeatured) {
            Feature feaBordes = this.lastFeaBordes;
            Feature feaFondos = this.lastFeaFondos;
            LineString3D lineString3D = (LineString3D)feaBordes.getGeometry();
            Polygon3D polygon3D = (Polygon3D)feaFondos.getGeometry();
            if (grp.hasCode(8)) {
                feaBordes.setProp("layer", grp.getDataAsString(8));
                feaFondos.setProp("layer", grp.getDataAsString(8));
            }
            if (grp.hasCode(70)) {
                vFlags = grp.getDataAsInt(70);
            }
            x = grp.getDataAsDouble(10);
            y = grp.getDataAsDouble(20);
            z = grp.getDataAsDouble(30);
            org.cresques.geo.Point3D point_in = new org.cresques.geo.Point3D(x, y, z);
            org.cresques.geo.Point3D xtru = new org.cresques.geo.Point3D(this.xtruX, this.xtruY, this.xtruZ);
            org.cresques.geo.Point3D point_out = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in, (org.cresques.geo.Point3D)xtru);
            x = point_out.getX();
            y = point_out.getY();
            z = point_out.getZ();
            if (z != 0.0) {
                this.dxf3DFile = true;
            }
            if (z != this.lastVertexElevation && lineString3D.pointNr() > 0) {
                this.constantPolylineElevation = false;
            }
            this.lastVertexElevation = z;
            if ((vFlags & 0x80) == 128 && (vFlags & 0x40) == 0) {
                int[] face = new int[]{grp.getDataAsInt(71), grp.getDataAsInt(72), grp.getDataAsInt(73), grp.getDataAsInt(74)};
                this.addFace(face);
            } else if ((vFlags & 0x10) != 16) {
                org.cresques.geo.Point3D pt = new org.cresques.geo.Point3D(this.proj.createPoint(x, y).getX(), this.proj.createPoint(x, y).getY(), z);
                lineString3D.add(pt);
                polygon3D.add(pt);
                if (lineString3D.pointNr() == 1) {
                    this.firstPt = pt;
                }
                if (this.bulge == 0.0) {
                    this.bulge = grp.hasCode(42) ? grp.getDataAsDouble(42) : 0.0;
                } else {
                    double bulge_aux = 0.0;
                    bulge_aux = grp.hasCode(42) ? grp.getDataAsDouble(42) : 0.0;
                    if (this.ptAnterior.getX() != pt.getX() || this.ptAnterior.getY() != pt.getY()) {
                        lineString3D.remove(lineString3D.pointNr() - 1);
                        lineString3D.remove(lineString3D.pointNr() - 1);
                        polygon3D.remove(polygon3D.pointNr() - 1);
                        polygon3D.remove(polygon3D.pointNr() - 1);
                        Vector arc = DxfFeatureMaker.createArc((Point2D)this.ptAnterior, (Point2D)pt, this.bulge);
                        if (this.bulge > 0.0) {
                            int i = 0;
                            while (i < arc.size()) {
                                Point2D ptAux = new Point2D.Double();
                                ptAux = this.proj.createPoint(((Point2D)arc.get(i)).getX(), ((Point2D)arc.get(i)).getY());
                                org.cresques.geo.Point3D ptAux3D = new org.cresques.geo.Point3D(ptAux.getX(), ptAux.getY(), z);
                                lineString3D.add(ptAux3D);
                                polygon3D.add(ptAux3D);
                                if (lineString3D.pointNr() == 1) {
                                    this.firstPt = ptAux3D;
                                }
                                ++i;
                            }
                        } else {
                            int i = arc.size() - 1;
                            while (i >= 0) {
                                Point2D ptAux = new Point2D.Double();
                                ptAux = this.proj.createPoint(((Point2D)arc.get(i)).getX(), ((Point2D)arc.get(i)).getY());
                                org.cresques.geo.Point3D ptAux3D = new org.cresques.geo.Point3D(ptAux.getX(), ptAux.getY(), z);
                                lineString3D.add(ptAux3D);
                                polygon3D.add(ptAux3D);
                                if (lineString3D.pointNr() == 1 || polygon3D.pointNr() == 1) {
                                    this.firstPt = ptAux3D;
                                }
                                --i;
                            }
                        }
                    }
                    this.bulge = bulge_aux;
                }
                this.ptAnterior = pt;
            }
        } else {
            Feature feaBordes = this.lastFeaBordes;
            LineString3D lineString3D = (LineString3D)feaBordes.getGeometry();
            if (grp.hasCode(8)) {
                feaBordes.setProp("layer", grp.getDataAsString(8));
            }
            if (grp.hasCode(70)) {
                vFlags = grp.getDataAsInt(70);
            }
            x = grp.getDataAsDouble(10);
            y = grp.getDataAsDouble(20);
            if (grp.hasCode(30)) {
                z = grp.getDataAsDouble(30);
            }
            org.cresques.geo.Point3D point_in = new org.cresques.geo.Point3D(x, y, z);
            org.cresques.geo.Point3D xtru = new org.cresques.geo.Point3D(this.xtruX, this.xtruY, this.xtruZ);
            org.cresques.geo.Point3D point_out = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in, (org.cresques.geo.Point3D)xtru);
            x = point_out.getX();
            y = point_out.getY();
            z = point_out.getZ();
            if (z != 0.0) {
                this.dxf3DFile = true;
            }
            if (z != this.lastVertexElevation && lineString3D.pointNr() > 0) {
                this.constantPolylineElevation = false;
            }
            this.lastVertexElevation = z;
            if ((vFlags & 0x80) == 128 && (vFlags & 0x40) == 0) {
                int[] face = new int[]{grp.getDataAsInt(71), grp.getDataAsInt(72), grp.getDataAsInt(73), grp.getDataAsInt(74)};
                this.addFace(face);
            } else if ((vFlags & 0x10) != 16) {
                org.cresques.geo.Point3D pt = new org.cresques.geo.Point3D(this.proj.createPoint(x, y).getX(), this.proj.createPoint(x, y).getY(), z);
                lineString3D.add(pt);
                if (lineString3D.pointNr() == 1) {
                    this.firstPt = pt;
                }
                if (this.bulge == 0.0) {
                    this.bulge = grp.hasCode(42) ? grp.getDataAsDouble(42) : 0.0;
                } else {
                    double bulge_aux = 0.0;
                    bulge_aux = grp.hasCode(42) ? grp.getDataAsDouble(42) : 0.0;
                    if (this.ptAnterior.getX() != pt.getX() || this.ptAnterior.getY() != pt.getY()) {
                        lineString3D.remove(lineString3D.pointNr() - 1);
                        lineString3D.remove(lineString3D.pointNr() - 1);
                        Vector arc = DxfFeatureMaker.createArc((Point2D)this.ptAnterior, (Point2D)pt, this.bulge);
                        if (this.bulge > 0.0) {
                            int i = 0;
                            while (i < arc.size()) {
                                Point2D ptAux = new Point2D.Double();
                                ptAux = this.proj.createPoint(((Point2D)arc.get(i)).getX(), ((Point2D)arc.get(i)).getY());
                                org.cresques.geo.Point3D ptAux3D = new org.cresques.geo.Point3D(ptAux.getX(), ptAux.getY(), z);
                                lineString3D.add(ptAux3D);
                                if (lineString3D.pointNr() == 1) {
                                    this.firstPt = ptAux3D;
                                }
                                ++i;
                            }
                        } else {
                            int i = arc.size() - 1;
                            while (i >= 0) {
                                Point2D ptAux = new Point2D.Double();
                                ptAux = this.proj.createPoint(((Point2D)arc.get(i)).getX(), ((Point2D)arc.get(i)).getY());
                                org.cresques.geo.Point3D ptAux3D = new org.cresques.geo.Point3D(ptAux.getX(), ptAux.getY(), z);
                                lineString3D.add(ptAux3D);
                                if (lineString3D.pointNr() == 1) {
                                    this.firstPt = ptAux3D;
                                }
                                --i;
                            }
                        }
                    }
                    this.bulge = bulge_aux;
                }
                this.ptAnterior = pt;
            }
        }
    }

    @Override
    public void createLwPolyline(DxfGroupVector grp) throws Exception {
        String string;
        Double doub;
        double x = 0.0;
        double y = 0.0;
        double elev = 0.0;
        DxfGroup g = null;
        LineString3D lineString3D = new LineString3D();
        Polygon3D polygon3D = new Polygon3D();
        Feature feaBordes = new Feature();
        Feature feaFondos = new Feature();
        int flags = 0;
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        feaBordes.setProp("dxfEntity", "LwPolyline");
        feaFondos.setProp("dxfEntity", "LwPolyline");
        feaBordes.setProp("layer", grp.getDataAsString(8));
        feaFondos.setProp("layer", grp.getDataAsString(8));
        if (grp.hasCode(38)) {
            elev = grp.getDataAsDouble(38);
            if (elev != 0.0) {
                this.dxf3DFile = true;
            }
            doub = new Double(elev);
            string = doub.toString();
            feaBordes.setProp("elevation", string);
            feaFondos.setProp("elevation", string);
        } else {
            doub = new Double(0.0);
            feaBordes.setProp("elevation", doub.toString());
            feaFondos.setProp("elevation", doub.toString());
        }
        if (grp.hasCode(39)) {
            doub = new Double(grp.getDataAsDouble(39));
            string = doub.toString();
            feaBordes.setProp("thickness", string);
            feaFondos.setProp("thickness", string);
        } else {
            doub = new Double(0.0);
            feaBordes.setProp("thickness", doub.toString());
            feaFondos.setProp("thickness", doub.toString());
        }
        if (grp.hasCode(62)) {
            Integer integer = new Integer(grp.getDataAsInt(62));
            string = integer.toString();
            feaBordes.setProp("color", string);
            feaFondos.setProp("color", string);
            feaBordes.setProp("colorByLayer", "false");
            feaFondos.setProp("colorByLayer", "false");
        } else {
            DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
            int clr = layer.colorNumber;
            Integer integer = new Integer(clr);
            String string2 = integer.toString();
            feaBordes.setProp("color", string2);
            feaFondos.setProp("color", string2);
            feaBordes.setProp("colorByLayer", "true");
            feaFondos.setProp("colorByLayer", "true");
        }
        if (grp.hasCode(70)) {
            flags = grp.getDataAsInt(70);
        }
        if (flags & true) {
            feaBordes.setGeometry((Geometry)lineString3D);
            feaFondos.setGeometry((Geometry)polygon3D);
            this.isDoubleFeatured = true;
        } else {
            feaBordes.setGeometry((Geometry)lineString3D);
            this.isDoubleFeatured = false;
        }
        if (grp.hasCode(210)) {
            extx = grp.getDataAsDouble(210);
        }
        if (grp.hasCode(220)) {
            exty = grp.getDataAsDouble(220);
        }
        if (grp.hasCode(230)) {
            extz = grp.getDataAsDouble(230);
        }
        int j = 0;
        double firstX = 0.0;
        double firstY = 0.0;
        boolean hasBulge = false;
        double bulgeLwp = 0.0;
        int i = 0;
        while (i < grp.size()) {
            g = (DxfGroup)grp.get(i);
            if (g.getCode() == 10) {
                ++j;
                x = (Double)g.getData();
            } else if (g.getCode() == 20) {
                y = (Double)g.getData();
                org.cresques.geo.Point3D point_in1 = new org.cresques.geo.Point3D(x, y, elev);
                org.cresques.geo.Point3D xtru = new org.cresques.geo.Point3D(extx, exty, extz);
                org.cresques.geo.Point3D point_out1 = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in1, (org.cresques.geo.Point3D)xtru);
                x = point_out1.getX();
                y = point_out1.getY();
                elev = point_out1.getZ();
                if (hasBulge) {
                    Point2D.Double finalPoint = new Point2D.Double(x, y);
                    if (lineString3D.get(lineString3D.pointNr() - 1).getX() != ((Point2D)finalPoint).getX() || lineString3D.get(lineString3D.pointNr() - 1).getY() != ((Point2D)finalPoint).getY()) {
                        org.cresques.geo.Point3D ptAux3D;
                        Point2D ptAux;
                        int k;
                        Vector arc = DxfFeatureMaker.createArc(lineString3D.get(lineString3D.pointNr() - 1), finalPoint, bulgeLwp);
                        lineString3D.remove(lineString3D.pointNr() - 1);
                        if (this.isDoubleFeatured) {
                            polygon3D.remove(polygon3D.pointNr() - 1);
                        }
                        if (bulgeLwp > 0.0) {
                            k = 0;
                            while (k < arc.size()) {
                                ptAux = new Point2D.Double();
                                ptAux = this.proj.createPoint(((Point2D)arc.get(k)).getX(), ((Point2D)arc.get(k)).getY());
                                ptAux3D = new org.cresques.geo.Point3D(ptAux.getX(), ptAux.getY(), elev);
                                lineString3D.add(ptAux3D);
                                if (this.isDoubleFeatured) {
                                    polygon3D.add(ptAux3D);
                                }
                                if (lineString3D.pointNr() == 1 || polygon3D.pointNr() == 1) {
                                    this.firstPt = ptAux3D;
                                }
                                ++k;
                            }
                        } else {
                            k = arc.size() - 1;
                            while (k >= 0) {
                                ptAux = new Point2D.Double();
                                ptAux = this.proj.createPoint(((Point2D)arc.get(k)).getX(), ((Point2D)arc.get(k)).getY());
                                ptAux3D = new org.cresques.geo.Point3D(ptAux.getX(), ptAux.getY(), elev);
                                lineString3D.add(ptAux3D);
                                if (this.isDoubleFeatured) {
                                    polygon3D.add(ptAux3D);
                                }
                                if (lineString3D.pointNr() == 1 || polygon3D.pointNr() == 1) {
                                    this.firstPt = ptAux3D;
                                }
                                --k;
                            }
                        }
                    }
                    hasBulge = false;
                    bulgeLwp = 0.0;
                } else {
                    org.cresques.geo.Point3D ptAux3D = new org.cresques.geo.Point3D(this.proj.createPoint(x, y).getX(), this.proj.createPoint(x, y).getY(), elev);
                    lineString3D.add(ptAux3D);
                    if (this.isDoubleFeatured) {
                        polygon3D.add(ptAux3D);
                    }
                }
                if (j == 1) {
                    firstX = x;
                    firstY = y;
                }
                x = 0.0;
                y = 0.0;
            } else if (g.getCode() == 42 && (Double)g.getData() != 0.0) {
                hasBulge = true;
                bulgeLwp = (Double)g.getData();
            }
            ++i;
        }
        if (this.isDoubleFeatured) {
            org.cresques.geo.Point3D ptAux3D = new org.cresques.geo.Point3D(this.proj.createPoint(firstX, firstY).getX(), this.proj.createPoint(firstX, firstY).getY(), elev);
            lineString3D.add(ptAux3D);
            polygon3D.add(ptAux3D);
        }
        this.lastFeaBordes = feaBordes;
        if (this.isDoubleFeatured) {
            this.lastFeaFondos = feaFondos;
        }
        this.completeAttributes(this.lastFeaBordes);
        this.completeAttributes(this.lastFeaFondos);
        if (!this.addingToBlock) {
            if (this.isDoubleFeatured) {
                this.features.add((Extent.Has)feaFondos);
            } else {
                this.features.add((Extent.Has)feaBordes);
            }
        } else if (this.isDoubleFeatured) {
            this.blk.add((Extent.Has)feaFondos);
        } else {
            this.blk.add((Extent.Has)feaBordes);
        }
        this.isDoubleFeatured = false;
    }

    @Override
    public void createLine(DxfGroupVector grp) throws Exception {
        String string;
        Double doub;
        double x = 0.0;
        double y = 0.0;
        double z1 = 0.0;
        double z2 = 0.0;
        double elev = 0.0;
        Object g = null;
        Point2D pt1 = null;
        Point2D pt2 = null;
        LineString3D lineString3D = new LineString3D();
        Feature feature = new Feature();
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        feature.setProp("dxfEntity", "Line");
        if (grp.hasCode(8)) {
            feature.setProp("layer", grp.getDataAsString(8));
        }
        if (grp.hasCode(39)) {
            doub = new Double(grp.getDataAsDouble(39));
            string = doub.toString();
            feature.setProp("thickness", string);
        } else {
            doub = new Double(0.0);
            feature.setProp("thickness", doub.toString());
        }
        if (grp.hasCode(62)) {
            Integer integer = new Integer(grp.getDataAsInt(62));
            string = integer.toString();
            feature.setProp("color", string);
            feature.setProp("colorByLayer", "false");
        } else {
            DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
            int clr = layer.colorNumber;
            Integer integer = new Integer(clr);
            String string2 = integer.toString();
            feature.setProp("color", string2);
            feature.setProp("colorByLayer", "true");
        }
        x = grp.getDataAsDouble(10);
        y = grp.getDataAsDouble(20);
        z1 = grp.getDataAsDouble(30);
        pt1 = this.proj.createPoint(x, y);
        x = grp.getDataAsDouble(11);
        y = grp.getDataAsDouble(21);
        z2 = grp.getDataAsDouble(31);
        pt2 = this.proj.createPoint(x, y);
        if (grp.hasCode(210)) {
            extx = grp.getDataAsDouble(210);
        }
        if (grp.hasCode(220)) {
            exty = grp.getDataAsDouble(220);
        }
        if (grp.hasCode(230)) {
            extz = grp.getDataAsDouble(230);
        }
        org.cresques.geo.Point3D point_in1 = new org.cresques.geo.Point3D(pt1.getX(), pt1.getY(), z1);
        org.cresques.geo.Point3D point_in2 = new org.cresques.geo.Point3D(pt2.getX(), pt2.getY(), z2);
        org.cresques.geo.Point3D xtru = new org.cresques.geo.Point3D(extx, exty, extz);
        org.cresques.geo.Point3D point_out1 = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in1, (org.cresques.geo.Point3D)xtru);
        org.cresques.geo.Point3D point_out2 = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in2, (org.cresques.geo.Point3D)xtru);
        if (point_out1.getZ() != 0.0) {
            this.dxf3DFile = true;
        }
        if (point_out2.getZ() != 0.0) {
            this.dxf3DFile = true;
        }
        if (point_out1.getZ() == point_out2.getZ()) {
            elev = z1;
            Double doub2 = new Double(elev);
            String string3 = doub2.toString();
            feature.setProp("elevation", string3);
        } else {
            elev = 0.0;
            Double doub3 = new Double(elev);
            String string4 = doub3.toString();
            feature.setProp("elevation", string4);
        }
        lineString3D.add(point_out1);
        lineString3D.add(point_out2);
        feature.setGeometry((Geometry)lineString3D);
        this.completeAttributes(feature);
        if (!this.addingToBlock) {
            this.features.add((Extent.Has)feature);
        } else {
            this.blk.add((Extent.Has)feature);
        }
    }

    @Override
    public void createText(DxfGroupVector grp) throws Exception {
        String string;
        Double doub;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double h = 0.0;
        double rot = 0.0;
        Object g = null;
        Object pt = null;
        Point3D point = new Point3D();
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        point.setTextPoint(true);
        Feature feature = new Feature();
        feature.setProp("dxfEntity", "Text");
        if (grp.hasCode(8)) {
            feature.setProp("layer", grp.getDataAsString(8));
        }
        if (grp.hasCode(39)) {
            doub = new Double(grp.getDataAsDouble(39));
            string = doub.toString();
            feature.setProp("thickness", string);
        } else {
            doub = new Double(0.0);
            feature.setProp("thickness", doub.toString());
        }
        if (grp.hasCode(62)) {
            Integer integer = new Integer(grp.getDataAsInt(62));
            string = integer.toString();
            feature.setProp("color", string);
            feature.setProp("colorByLayer", "false");
        } else {
            DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
            int clr = layer.colorNumber;
            Integer integer = new Integer(clr);
            String string2 = integer.toString();
            feature.setProp("color", string2);
            feature.setProp("colorByLayer", "true");
        }
        if (grp.hasCode(1)) {
            String strAux1 = grp.getDataAsString(1);
            strAux1 = DxfConvTexts.ConvertText(strAux1);
            feature.setProp("text", strAux1);
        } else {
            feature.setProp("text", "No Text Code");
        }
        if (grp.hasCode(40)) {
            Double heightD = new Double(grp.getDataAsDouble(40));
            String heightS = heightD.toString();
            feature.setProp("textHeight", heightS);
        } else {
            feature.setProp("textHeight", "20.0");
        }
        if (grp.hasCode(50)) {
            Double rotD = new Double(grp.getDataAsDouble(50));
            String rotS = rotD.toString();
            feature.setProp("textRotation", rotS);
        } else {
            feature.setProp("textRotation", "0.0");
        }
        x = grp.getDataAsDouble(10);
        y = grp.getDataAsDouble(20);
        if (grp.hasCode(30)) {
            z = grp.getDataAsDouble(30);
        }
        if (grp.hasCode(210)) {
            extx = grp.getDataAsDouble(210);
        }
        if (grp.hasCode(220)) {
            exty = grp.getDataAsDouble(220);
        }
        if (grp.hasCode(230)) {
            extz = grp.getDataAsDouble(230);
        }
        org.cresques.geo.Point3D point_in = new org.cresques.geo.Point3D(x, y, z);
        org.cresques.geo.Point3D xtru = new org.cresques.geo.Point3D(extx, exty, extz);
        org.cresques.geo.Point3D point_out = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in, (org.cresques.geo.Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        z = point_out.getZ();
        Double doub2 = new Double(z);
        feature.setProp("elevation", doub2.toString());
        if (z != 0.0) {
            this.dxf3DFile = true;
        }
        point.add(new org.cresques.geo.Point3D(x, y, z));
        feature.setGeometry((Geometry)point);
        this.completeAttributes(feature);
        if (!this.addingToBlock) {
            this.features.add((Extent.Has)feature);
        } else {
            this.blk.add((Extent.Has)feature);
        }
    }

    @Override
    public void createMText(DxfGroupVector grp) throws Exception {
        int spacingStyle;
        int drawDirection;
        int attachPoint;
        String string;
        Double doub;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double h = 0.0;
        double rot = 0.0;
        Object g = null;
        Object pt = null;
        Point3D point = new Point3D();
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        point.setTextPoint(true);
        Feature feature = new Feature();
        feature.setProp("dxfEntity", "Text");
        if (grp.hasCode(8)) {
            feature.setProp("layer", grp.getDataAsString(8));
        }
        if (grp.hasCode(39)) {
            doub = new Double(grp.getDataAsDouble(39));
            string = doub.toString();
            feature.setProp("thickness", string);
        } else {
            doub = new Double(0.0);
            feature.setProp("thickness", doub.toString());
        }
        if (grp.hasCode(62)) {
            Integer integer = new Integer(grp.getDataAsInt(62));
            string = integer.toString();
            feature.setProp("color", string);
            feature.setProp("colorByLayer", "false");
        } else {
            DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
            int clr = layer.colorNumber;
            Integer integer = new Integer(clr);
            String string2 = integer.toString();
            feature.setProp("color", string2);
            feature.setProp("colorByLayer", "true");
        }
        if (grp.hasCode(1)) {
            String strAux1 = grp.getDataAsString(1);
            strAux1 = DxfConvTexts.ConvertText(strAux1);
            feature.setProp("text", strAux1);
        } else {
            feature.setProp("text", "No Text Code");
        }
        if (grp.hasCode(40)) {
            Double heightD = new Double(grp.getDataAsDouble(40));
            String heightS = heightD.toString();
            feature.setProp("textHeight", heightS);
        } else {
            feature.setProp("textHeight", "20.0");
        }
        if (grp.hasCode(50)) {
            Double rotD = new Double(grp.getDataAsDouble(50));
            String rotS = rotD.toString();
            feature.setProp("textRotation", rotS);
        } else {
            feature.setProp("textRotation", "0.0");
        }
        if (!grp.hasCode(71) || (attachPoint = grp.getDataAsInt(71)) == 1 || attachPoint == 2 || attachPoint == 3 || attachPoint == 4 || attachPoint == 5 || attachPoint == 6 || attachPoint == 7 || attachPoint != 8) {
            // empty if block
        }
        if (!grp.hasCode(72) || (drawDirection = grp.getDataAsInt(71)) == 1 || drawDirection != 3) {
            // empty if block
        }
        if (!grp.hasCode(73) || (spacingStyle = grp.getDataAsInt(71)) != 1) {
            // empty if block
        }
        x = grp.getDataAsDouble(10);
        y = grp.getDataAsDouble(20);
        z = grp.getDataAsDouble(30);
        if (grp.hasCode(210)) {
            extx = grp.getDataAsDouble(210);
        }
        if (grp.hasCode(220)) {
            exty = grp.getDataAsDouble(220);
        }
        if (grp.hasCode(230)) {
            extz = grp.getDataAsDouble(230);
        }
        org.cresques.geo.Point3D point_in = new org.cresques.geo.Point3D(x, y, z);
        org.cresques.geo.Point3D xtru = new org.cresques.geo.Point3D(extx, exty, extz);
        org.cresques.geo.Point3D point_out = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in, (org.cresques.geo.Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        z = point_out.getZ();
        Double doub2 = new Double(z);
        feature.setProp("elevation", doub2.toString());
        if (z != 0.0) {
            this.dxf3DFile = true;
        }
        point.add(new org.cresques.geo.Point3D(x, y, z));
        feature.setGeometry((Geometry)point);
        this.completeAttributes(feature);
        if (!this.addingToBlock) {
            this.features.add((Extent.Has)feature);
        } else {
            this.blk.add((Extent.Has)feature);
        }
    }

    @Override
    public void createPoint(DxfGroupVector grp) throws Exception {
        String string;
        Double doub;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        Object g = null;
        Object pt = null;
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        Point3D point = new Point3D();
        Feature feature = new Feature();
        feature.setProp("dxfEntity", "Point");
        if (grp.hasCode(8)) {
            feature.setProp("layer", grp.getDataAsString(8));
        }
        if (grp.hasCode(39)) {
            doub = new Double(grp.getDataAsDouble(39));
            string = doub.toString();
            feature.setProp("thickness", string);
        } else {
            doub = new Double(0.0);
            feature.setProp("thickness", doub.toString());
        }
        if (grp.hasCode(62)) {
            Integer integer = new Integer(grp.getDataAsInt(62));
            string = integer.toString();
            feature.setProp("color", string);
            feature.setProp("colorByLayer", "false");
        } else {
            DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
            int clr = layer.colorNumber;
            Integer integer = new Integer(clr);
            String string2 = integer.toString();
            feature.setProp("color", string2);
            feature.setProp("colorByLayer", "true");
        }
        x = grp.getDataAsDouble(10);
        y = grp.getDataAsDouble(20);
        z = grp.getDataAsDouble(30);
        if (grp.hasCode(210)) {
            extx = grp.getDataAsDouble(210);
        }
        if (grp.hasCode(220)) {
            exty = grp.getDataAsDouble(220);
        }
        if (grp.hasCode(230)) {
            extz = grp.getDataAsDouble(230);
        }
        org.cresques.geo.Point3D point_in = new org.cresques.geo.Point3D(x, y, z);
        org.cresques.geo.Point3D xtru = new org.cresques.geo.Point3D(extx, exty, extz);
        org.cresques.geo.Point3D point_out = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in, (org.cresques.geo.Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        z = point_out.getZ();
        Double doub2 = new Double(z);
        feature.setProp("elevation", doub2.toString());
        if (z != 0.0) {
            this.dxf3DFile = true;
        }
        point.add(new org.cresques.geo.Point3D(x, y, z));
        feature.setGeometry((Geometry)point);
        this.completeAttributes(feature);
        if (!this.addingToBlock) {
            this.features.add((Extent.Has)feature);
        } else {
            this.blk.add((Extent.Has)feature);
        }
    }

    @Override
    public void createCircle(DxfGroupVector grp) throws Exception {
        String string;
        Double doub;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double r = 0.0;
        org.cresques.geo.Point3D firstPt = new org.cresques.geo.Point3D();
        Object g = null;
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        LineString3D lineString3D = new LineString3D();
        Polygon3D polygon3D = new Polygon3D();
        Feature feaBordes = new Feature();
        Feature feaFondos = new Feature();
        feaBordes.setProp("dxfEntity", "Circle");
        feaFondos.setProp("dxfEntity", "Circle");
        if (grp.hasCode(8)) {
            feaBordes.setProp("layer", grp.getDataAsString(8));
        }
        feaFondos.setProp("layer", grp.getDataAsString(8));
        if (grp.hasCode(39)) {
            doub = new Double(grp.getDataAsDouble(39));
            string = doub.toString();
            feaBordes.setProp("thickness", string);
            feaFondos.setProp("thickness", string);
        } else {
            doub = new Double(0.0);
            feaBordes.setProp("thickness", doub.toString());
            feaFondos.setProp("thickness", doub.toString());
        }
        if (grp.hasCode(62)) {
            Integer integer = new Integer(grp.getDataAsInt(62));
            string = integer.toString();
            feaBordes.setProp("color", string);
            feaFondos.setProp("color", string);
            feaBordes.setProp("colorByLayer", "false");
            feaFondos.setProp("colorByLayer", "false");
        } else {
            DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
            int clr = layer.colorNumber;
            Integer integer = new Integer(clr);
            String string2 = integer.toString();
            feaBordes.setProp("color", string2);
            feaFondos.setProp("color", string2);
            feaBordes.setProp("colorByLayer", "true");
            feaFondos.setProp("colorByLayer", "true");
        }
        x = grp.getDataAsDouble(10);
        y = grp.getDataAsDouble(20);
        if (grp.hasCode(30)) {
            z = grp.getDataAsDouble(30);
        }
        if (grp.hasCode(40)) {
            r = grp.getDataAsDouble(40);
        }
        if (grp.hasCode(210)) {
            extx = grp.getDataAsDouble(210);
        }
        if (grp.hasCode(220)) {
            exty = grp.getDataAsDouble(220);
        }
        if (grp.hasCode(230)) {
            extz = grp.getDataAsDouble(230);
        }
        org.cresques.geo.Point3D point_in = new org.cresques.geo.Point3D(x, y, z);
        org.cresques.geo.Point3D xtru = new org.cresques.geo.Point3D(extx, exty, extz);
        org.cresques.geo.Point3D point_out = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in, (org.cresques.geo.Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        z = point_out.getZ();
        Double doub2 = new Double(z);
        feaBordes.setProp("elevation", doub2.toString());
        feaFondos.setProp("elevation", doub2.toString());
        if (z != 0.0) {
            this.dxf3DFile = true;
        }
        Point2D c = this.proj.createPoint(x, y);
        org.cresques.geo.Point3D center = new org.cresques.geo.Point3D(c.getX(), c.getY(), z);
        org.cresques.geo.Point3D[] pts = new org.cresques.geo.Point3D[360];
        int angulo = 0;
        angulo = 0;
        while (angulo < 360) {
            pts[angulo] = new org.cresques.geo.Point3D(center.getX(), center.getY(), center.getZ());
            pts[angulo] = new org.cresques.geo.Point3D(pts[angulo].getX() + r * Math.sin((double)angulo * Math.PI / 180.0), pts[angulo].getY() + r * Math.cos((double)angulo * Math.PI / 180.0), center.getZ());
            if (pts.length == 1) {
                firstPt = pts[angulo];
            }
            ++angulo;
        }
        int i = 0;
        while (i < pts.length) {
            lineString3D.add(pts[i]);
            polygon3D.add(pts[i]);
            ++i;
        }
        feaBordes.setGeometry((Geometry)lineString3D);
        feaFondos.setGeometry((Geometry)polygon3D);
        this.completeAttributes(feaBordes);
        this.completeAttributes(feaFondos);
        if (!this.addingToBlock) {
            this.features.add((Extent.Has)feaFondos);
        } else {
            this.blk.add((Extent.Has)feaFondos);
        }
    }

    @Override
    public void createArc(DxfGroupVector grp) throws Exception {
        int i;
        String string;
        Double doub;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double r = 0.0;
        double empieza = 0.0;
        double acaba = 0.0;
        Object g = null;
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        LineString3D lineString3D = new LineString3D();
        Feature feature = new Feature();
        feature.setProp("dxfEntity", "Arc");
        if (grp.hasCode(8)) {
            feature.setProp("layer", grp.getDataAsString(8));
        }
        if (grp.hasCode(39)) {
            doub = new Double(grp.getDataAsDouble(39));
            string = doub.toString();
            feature.setProp("thickness", string);
        } else {
            doub = new Double(0.0);
            feature.setProp("thickness", doub.toString());
        }
        if (grp.hasCode(62)) {
            Integer integer = new Integer(grp.getDataAsInt(62));
            string = integer.toString();
            feature.setProp("color", string);
            feature.setProp("colorByLayer", "false");
        } else {
            DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
            int clr = layer.colorNumber;
            Integer integer = new Integer(clr);
            String string2 = integer.toString();
            feature.setProp("color", string2);
            feature.setProp("colorByLayer", "true");
        }
        x = grp.getDataAsDouble(10);
        y = grp.getDataAsDouble(20);
        if (grp.hasCode(30)) {
            z = grp.getDataAsDouble(30);
        }
        if (grp.hasCode(40)) {
            r = grp.getDataAsDouble(40);
        }
        if (grp.hasCode(50)) {
            empieza = grp.getDataAsDouble(50);
        }
        if (grp.hasCode(51)) {
            acaba = grp.getDataAsDouble(51);
        }
        if (grp.hasCode(210)) {
            extx = grp.getDataAsDouble(210);
        }
        if (grp.hasCode(220)) {
            exty = grp.getDataAsDouble(220);
        }
        if (grp.hasCode(230)) {
            extz = grp.getDataAsDouble(230);
        }
        org.cresques.geo.Point3D point_in = new org.cresques.geo.Point3D(x, y, z);
        org.cresques.geo.Point3D xtru = new org.cresques.geo.Point3D(extx, exty, extz);
        org.cresques.geo.Point3D point_out = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in, (org.cresques.geo.Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        z = point_out.getZ();
        Double doub2 = new Double(z);
        feature.setProp("elevation", doub2.toString());
        if (z != 0.0) {
            this.dxf3DFile = true;
        }
        Point2D c = this.proj.createPoint(x, y);
        org.cresques.geo.Point3D center = new org.cresques.geo.Point3D(c.getX(), c.getY(), z);
        int iempieza = (int)empieza;
        int iacaba = (int)acaba;
        double angulo = 0.0;
        org.cresques.geo.Point3D[] pts = null;
        if (empieza <= acaba) {
            pts = new org.cresques.geo.Point3D[iacaba - iempieza + 2];
            angulo = empieza;
            pts[0] = new org.cresques.geo.Point3D(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0), center.getZ());
            i = 1;
            while (i <= iacaba - iempieza + 1) {
                angulo = iempieza + i;
                pts[i] = new org.cresques.geo.Point3D(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0), center.getZ());
                ++i;
            }
            angulo = acaba;
            pts[iacaba - iempieza + 1] = new org.cresques.geo.Point3D(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0), center.getZ());
        } else {
            pts = new org.cresques.geo.Point3D[360 - iempieza + iacaba + 2];
            angulo = empieza;
            pts[0] = new org.cresques.geo.Point3D(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0), center.getZ());
            i = 1;
            while (i <= 360 - iempieza) {
                angulo = iempieza + i;
                pts[i] = new org.cresques.geo.Point3D(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0), center.getZ());
                ++i;
            }
            i = 360 - iempieza + 1;
            while (i <= 360 - iempieza + iacaba) {
                angulo = i - (360 - iempieza);
                pts[i] = new org.cresques.geo.Point3D(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0), center.getZ());
                ++i;
            }
            angulo = acaba;
            pts[360 - iempieza + iacaba + 1] = new org.cresques.geo.Point3D(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0), center.getZ());
        }
        i = 0;
        while (i < pts.length) {
            lineString3D.add(pts[i]);
            ++i;
        }
        feature.setGeometry((Geometry)lineString3D);
        this.completeAttributes(feature);
        if (!this.addingToBlock) {
            this.features.add((Extent.Has)feature);
        } else {
            this.blk.add((Extent.Has)feature);
        }
    }

    @Override
    public void createInsert(DxfGroupVector grp) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        Object g = null;
        org.cresques.geo.Point3D pt = new org.cresques.geo.Point3D(0.0, 0.0, 0.0);
        org.cresques.geo.Point3D scaleFactor = new org.cresques.geo.Point3D(1.0, 1.0, 1.0);
        double rotAngle = 0.0;
        String blockName = "";
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        InsPoint3D insert = new InsPoint3D();
        Feature feature = new Feature();
        Point3D secondGeom = new Point3D();
        Feature secondFeat = new Feature();
        int attributesFollowFlag = 0;
        feature.setProp("dxfEntity", "Insert");
        secondFeat.setProp("dxfEntity", "Insert");
        if (grp.hasCode(2)) {
            blockName = grp.getDataAsString(2);
            feature.setProp("blockname", blockName);
            secondFeat.setProp("blockname", blockName);
            insert.setBlockName(blockName);
        }
        if (grp.hasCode(8)) {
            feature.setProp("layer", grp.getDataAsString(8));
            secondFeat.setProp("layer", grp.getDataAsString(8));
        }
        Double doub = new Double(0.0);
        secondFeat.setProp("thickness", doub.toString());
        if (grp.hasCode(62)) {
            Integer integer = new Integer(grp.getDataAsInt(62));
            String string = integer.toString();
            feature.setProp("color", string);
            secondFeat.setProp("color", string);
            feature.setProp("colorByLayer", "false");
            secondFeat.setProp("colorByLayer", "false");
        } else {
            DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
            int clr = layer.colorNumber;
            Integer integer = new Integer(clr);
            String string = integer.toString();
            feature.setProp("color", string);
            secondFeat.setProp("color", string);
            feature.setProp("colorByLayer", "true");
            secondFeat.setProp("colorByLayer", "true");
        }
        if (grp.hasCode(66)) {
            attributesFollowFlag = grp.getDataAsInt(66);
        }
        x = grp.getDataAsDouble(10);
        y = grp.getDataAsDouble(20);
        z = grp.getDataAsDouble(30);
        if (grp.hasCode(41)) {
            scaleFactor.setLocation(grp.getDataAsDouble(41), scaleFactor.getY());
            insert.setScaleFactor(scaleFactor);
        } else {
            insert.setScaleFactor(scaleFactor);
        }
        if (grp.hasCode(42)) {
            scaleFactor.setLocation(scaleFactor.getX(), grp.getDataAsDouble(42));
            insert.setScaleFactor(scaleFactor);
        } else {
            insert.setScaleFactor(scaleFactor);
        }
        if (grp.hasCode(43)) {
            scaleFactor = new org.cresques.geo.Point3D(scaleFactor.getX(), scaleFactor.getY(), grp.getDataAsDouble(43));
            insert.setScaleFactor(scaleFactor);
        } else {
            insert.setScaleFactor(scaleFactor);
        }
        if (grp.hasCode(50)) {
            rotAngle = grp.getDataAsDouble(50);
            insert.setRotAngle(rotAngle);
        }
        if (grp.hasCode(210)) {
            extx = grp.getDataAsDouble(210);
        }
        if (grp.hasCode(220)) {
            exty = grp.getDataAsDouble(220);
        }
        if (grp.hasCode(230)) {
            extz = grp.getDataAsDouble(230);
        }
        org.cresques.geo.Point3D point_in = new org.cresques.geo.Point3D(x, y, z);
        org.cresques.geo.Point3D xtru = new org.cresques.geo.Point3D(extx, exty, extz);
        org.cresques.geo.Point3D point_out = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in, (org.cresques.geo.Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        z = point_out.getZ();
        Double doubz = new Double(z);
        feature.setProp("elevation", doubz.toString());
        secondFeat.setProp("elevation", doubz.toString());
        if (z != 0.0) {
            this.dxf3DFile = true;
        }
        insert.setBlkList(this.blkList);
        insert.encuentraBloque(blockName);
        insert.add(new org.cresques.geo.Point3D(x, y, z));
        secondGeom.add(new org.cresques.geo.Point3D(x, y, z));
        feature.setGeometry((Geometry)insert);
        secondFeat.setGeometry((Geometry)secondGeom);
        this.completeAttributes(feature);
        this.completeAttributes(secondFeat);
        if (insert.getBlockFound() && attributesFollowFlag != 1) {
            this.gestionaInsert(feature);
        }
        if (attributesFollowFlag == 1) {
            this.isDoubleFeatured = true;
            this.lastFeaBordes = feature;
            this.lastFeaFondos = secondFeat;
        } else {
            if (!this.addingToBlock) {
                this.features.add((Extent.Has)secondFeat);
            }
            if (this.addingToBlock) {
                this.blk.add((Extent.Has)feature);
            }
        }
    }

    @Override
    public void createSolid(DxfGroupVector grp) throws Exception {
        String string;
        Double doub;
        org.cresques.geo.Point3D pto3D;
        double x = 0.0;
        double y = 0.0;
        double z1 = 0.0;
        double z2 = 0.0;
        double z3 = 0.0;
        double z4 = 0.0;
        Object g = null;
        org.cresques.geo.Point3D[] pts = new org.cresques.geo.Point3D[4];
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        LineString3D lineString3D = new LineString3D();
        Polygon3D polygon3D = new Polygon3D();
        Feature feaBordes = new Feature();
        Feature feaFondos = new Feature();
        feaBordes.setProp("dxfEntity", "Solid");
        feaFondos.setProp("dxfEntity", "Solid");
        if (grp.hasCode(8)) {
            feaBordes.setProp("layer", grp.getDataAsString(8));
        }
        feaFondos.setProp("layer", grp.getDataAsString(8));
        x = grp.getDataAsDouble(10);
        y = grp.getDataAsDouble(20);
        z1 = grp.getDataAsDouble(30);
        Point2D pto = this.proj.createPoint(x, y);
        pts[0] = pto3D = new org.cresques.geo.Point3D(pto.getX(), pto.getY(), z1);
        x = grp.getDataAsDouble(11);
        y = grp.getDataAsDouble(21);
        z2 = grp.getDataAsDouble(31);
        pto = this.proj.createPoint(x, y);
        pts[1] = pto3D = new org.cresques.geo.Point3D(pto.getX(), pto.getY(), z2);
        x = grp.getDataAsDouble(12);
        y = grp.getDataAsDouble(22);
        z3 = grp.getDataAsDouble(32);
        pto = this.proj.createPoint(x, y);
        pts[2] = pto3D = new org.cresques.geo.Point3D(pto.getX(), pto.getY(), z3);
        if (grp.hasCode(13)) {
            x = grp.getDataAsDouble(13);
        }
        if (grp.hasCode(23)) {
            y = grp.getDataAsDouble(23);
        }
        if (grp.hasCode(33)) {
            z4 = grp.getDataAsDouble(33);
        }
        pto = this.proj.createPoint(x, y);
        pts[3] = pto3D = new org.cresques.geo.Point3D(pto.getX(), pto.getY(), z4);
        if (grp.hasCode(39)) {
            doub = new Double(grp.getDataAsDouble(39));
            string = doub.toString();
            feaBordes.setProp("thickness", string);
            feaFondos.setProp("thickness", string);
        } else {
            doub = new Double(0.0);
            feaBordes.setProp("thickness", doub.toString());
            feaFondos.setProp("thickness", doub.toString());
        }
        if (grp.hasCode(62)) {
            Integer integer = new Integer(grp.getDataAsInt(62));
            string = integer.toString();
            feaBordes.setProp("color", string);
            feaFondos.setProp("color", string);
            feaBordes.setProp("colorByLayer", "false");
            feaFondos.setProp("colorByLayer", "false");
        } else {
            DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
            int clr = layer.colorNumber;
            Integer integer = new Integer(clr);
            String string2 = integer.toString();
            feaBordes.setProp("color", string2);
            feaFondos.setProp("color", string2);
            feaBordes.setProp("colorByLayer", "true");
            feaFondos.setProp("colorByLayer", "true");
        }
        if (grp.hasCode(210)) {
            extx = grp.getDataAsDouble(210);
        }
        if (grp.hasCode(220)) {
            exty = grp.getDataAsDouble(220);
        }
        if (grp.hasCode(230)) {
            extz = grp.getDataAsDouble(230);
        }
        org.cresques.geo.Point3D point_in1 = new org.cresques.geo.Point3D(pts[0].getX(), pts[0].getY(), z1);
        org.cresques.geo.Point3D point_in2 = new org.cresques.geo.Point3D(pts[1].getX(), pts[1].getY(), z2);
        org.cresques.geo.Point3D point_in3 = new org.cresques.geo.Point3D(pts[2].getX(), pts[2].getY(), z3);
        org.cresques.geo.Point3D point_in4 = new org.cresques.geo.Point3D(pts[3].getX(), pts[3].getY(), z4);
        org.cresques.geo.Point3D xtru = new org.cresques.geo.Point3D(extx, exty, extz);
        org.cresques.geo.Point3D point_out1 = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in1, (org.cresques.geo.Point3D)xtru);
        org.cresques.geo.Point3D point_out2 = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in2, (org.cresques.geo.Point3D)xtru);
        org.cresques.geo.Point3D point_out3 = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in3, (org.cresques.geo.Point3D)xtru);
        org.cresques.geo.Point3D point_out4 = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in4, (org.cresques.geo.Point3D)xtru);
        pts[0] = new org.cresques.geo.Point3D(point_out1);
        pts[1] = new org.cresques.geo.Point3D(point_out2);
        pts[2] = new org.cresques.geo.Point3D(point_out3);
        pts[3] = new org.cresques.geo.Point3D(point_out4);
        if (pts[0].getZ() != 0.0 || pts[1].getZ() != 0.0 || pts[2].getZ() != 0.0 || pts[3].getZ() != 0.0) {
            this.dxf3DFile = true;
        }
        org.cresques.geo.Point3D aux = new org.cresques.geo.Point3D(pts[2]);
        pts[2] = new org.cresques.geo.Point3D(pts[3]);
        pts[3] = aux;
        Double doub2 = new Double(0.0);
        if (pts[0].getZ() == pts[1].getZ() && pts[1].getZ() == pts[2].getZ() && pts[2].getZ() == pts[3].getZ()) {
            doub2 = new Double(pts[0].getZ());
        }
        String string3 = doub2.toString();
        feaBordes.setProp("elevation", string3);
        feaFondos.setProp("elevation", string3);
        int i = 0;
        while (i < pts.length) {
            lineString3D.add(pts[i]);
            polygon3D.add(pts[i]);
            ++i;
        }
        lineString3D.add(pts[0]);
        polygon3D.add(pts[0]);
        feaBordes.setGeometry((Geometry)lineString3D);
        feaFondos.setGeometry((Geometry)polygon3D);
        this.completeAttributes(feaBordes);
        this.completeAttributes(feaFondos);
        if (!this.addingToBlock) {
            this.features.add((Extent.Has)feaFondos);
        } else {
            this.blk.add((Extent.Has)feaFondos);
        }
    }

    @Override
    public void createSpline(DxfGroupVector grp) throws Exception {
        String string;
        Double doub;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        DxfGroup g = null;
        LineString3D lineString3D = new LineString3D();
        Polygon3D polygon3D = new Polygon3D();
        Feature feaBordes = new Feature();
        Feature feaFondos = new Feature();
        int flags = 0;
        feaBordes.setProp("dxfEntity", "Spline");
        feaFondos.setProp("dxfEntity", "Spline");
        if (grp.hasCode(8)) {
            feaBordes.setProp("layer", grp.getDataAsString(8));
            feaFondos.setProp("layer", grp.getDataAsString(8));
        }
        if (grp.hasCode(39)) {
            doub = new Double(grp.getDataAsDouble(39));
            string = doub.toString();
            feaBordes.setProp("thickness", string);
            feaFondos.setProp("thickness", string);
        } else {
            doub = new Double(0.0);
            feaBordes.setProp("thickness", doub.toString());
            feaFondos.setProp("thickness", doub.toString());
        }
        if (grp.hasCode(62)) {
            Integer integer = new Integer(grp.getDataAsInt(62));
            string = integer.toString();
            feaBordes.setProp("color", string);
            feaFondos.setProp("color", string);
            feaBordes.setProp("colorByLayer", "false");
            feaFondos.setProp("colorByLayer", "false");
        } else {
            DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
            int clr = layer.colorNumber;
            Integer integer = new Integer(clr);
            String string2 = integer.toString();
            feaBordes.setProp("color", string2);
            feaFondos.setProp("color", string2);
            feaBordes.setProp("colorByLayer", "true");
            feaFondos.setProp("colorByLayer", "true");
        }
        if (grp.hasCode(70)) {
            flags = grp.getDataAsInt(70);
        }
        if (flags & true) {
            feaBordes.setGeometry((Geometry)lineString3D);
            feaFondos.setGeometry((Geometry)polygon3D);
            this.isDoubleFeatured = true;
        } else {
            feaBordes.setGeometry((Geometry)lineString3D);
            this.isDoubleFeatured = false;
        }
        int j = 0;
        double firstX = 0.0;
        double firstY = 0.0;
        double firstZ = 0.0;
        int i = 0;
        while (i < grp.size()) {
            g = (DxfGroup)grp.get(i);
            if (g.getCode() == 10) {
                ++j;
                x = (Double)g.getData();
            } else if (g.getCode() == 20) {
                y = (Double)g.getData();
            } else if (g.getCode() == 30) {
                z = (Double)g.getData();
                Point2D p = this.proj.createPoint(x, y);
                org.cresques.geo.Point3D p3d = new org.cresques.geo.Point3D(p.getX(), p.getY(), z);
                lineString3D.add(p3d);
                if (this.isDoubleFeatured) {
                    polygon3D.add(p3d);
                }
                if (j == 1) {
                    firstX = x;
                    firstY = y;
                    firstZ = z;
                }
                x = 0.0;
                y = 0.0;
                z = 0.0;
            }
            ++i;
        }
        if (this.isDoubleFeatured) {
            Point2D p = this.proj.createPoint(firstX, firstY);
            org.cresques.geo.Point3D p3d = new org.cresques.geo.Point3D(p.getX(), p.getY(), z);
            lineString3D.add(p3d);
            polygon3D.add(p3d);
        }
        double zprev = 0.0;
        boolean constSplineElev = true;
        int i2 = 0;
        while (i2 < lineString3D.pointNr()) {
            z = lineString3D.getPoint3D(i2).getZ();
            if (z != 0.0) {
                this.dxf3DFile = true;
            }
            if (i2 > 0 && z != zprev) {
                constSplineElev = false;
            }
            zprev = z;
            ++i2;
        }
        if (constSplineElev) {
            Double doub2 = new Double(lineString3D.getPoint3D(0).getZ());
            String string3 = doub2.toString();
            feaBordes.setProp("elevation", string3);
            if (this.isDoubleFeatured) {
                feaFondos.setProp("elevation", string3);
            }
        } else {
            Double doub3 = new Double(0.0);
            String string4 = doub3.toString();
            feaBordes.setProp("elevation", string4);
            if (this.isDoubleFeatured) {
                feaFondos.setProp("elevation", string4);
            }
        }
        this.lastFeaBordes = feaBordes;
        if (this.isDoubleFeatured) {
            this.lastFeaFondos = feaFondos;
        }
        this.completeAttributes(feaBordes);
        this.completeAttributes(feaFondos);
        if (!this.addingToBlock) {
            if (this.isDoubleFeatured) {
                this.features.add((Extent.Has)feaFondos);
            } else {
                this.features.add((Extent.Has)feaBordes);
            }
        } else if (this.isDoubleFeatured) {
            this.blk.add((Extent.Has)feaFondos);
        } else {
            this.blk.add((Extent.Has)feaBordes);
        }
        this.isDoubleFeatured = false;
    }

    @Override
    public void createAttdef(DxfGroupVector grp) throws Exception {
        Object g = null;
        String defaultValue = "";
        String tagString = "";
        String textStyleName = "";
        String[] attribute = new String[2];
        boolean tagDefined = false;
        boolean defValDefined = false;
        if (grp.hasCode(1)) {
            defaultValue = grp.getDataAsString(1);
            attribute[1] = DxfConvTexts.ConvertText(defaultValue);
            defValDefined = true;
            if (tagDefined) {
                this.attributes.add(attribute);
            }
        }
        if (grp.hasCode(2)) {
            tagString = grp.getDataAsString(2);
            attribute[0] = DxfConvTexts.ConvertText(tagString);
            tagDefined = true;
            if (defValDefined) {
                this.attributes.add(attribute);
            }
        }
        if (grp.hasCode(7)) {
            textStyleName = grp.getDataAsString(7);
            textStyleName = DxfConvTexts.ConvertText(textStyleName);
        }
        this.setNewAttributes();
    }

    @Override
    public void createAttrib(DxfGroupVector grp) throws Exception {
        String string;
        Double doub;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double h = 0.0;
        double rot = 0.0;
        Object g = null;
        Object pt = null;
        Point3D point = new Point3D();
        point.setTextPoint(true);
        String defaultValue = "";
        String tagString = "";
        String textStyleName = "";
        String[] att = new String[2];
        boolean tagDefined = false;
        boolean defValDefined = false;
        int attributeFlags = 0;
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        Feature insFea = this.lastFeaBordes;
        Feature ptFea = this.lastFeaFondos;
        Feature feature = new Feature();
        feature.setProp("dxfEntity", "Attrib");
        if (grp.hasCode(8)) {
            feature.setProp("layer", grp.getDataAsString(8));
        }
        if (grp.hasCode(39)) {
            doub = new Double(grp.getDataAsDouble(39));
            string = doub.toString();
            feature.setProp("thickness", string);
        } else {
            doub = new Double(0.0);
            feature.setProp("thickness", doub.toString());
        }
        if (grp.hasCode(62)) {
            Integer integer = new Integer(grp.getDataAsInt(62));
            string = integer.toString();
            feature.setProp("color", string);
            feature.setProp("colorByLayer", "false");
        } else {
            DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
            int clr = layer.colorNumber;
            Integer integer = new Integer(clr);
            String string2 = integer.toString();
            feature.setProp("color", string2);
            feature.setProp("colorByLayer", "true");
        }
        if (grp.hasCode(1)) {
            String strAux1 = grp.getDataAsString(1);
            defaultValue = strAux1 = DxfConvTexts.ConvertText(strAux1);
            att[1] = DxfConvTexts.ConvertText(defaultValue);
            defValDefined = true;
            if (tagDefined) {
                insFea.setProp(att[0], att[1]);
                ptFea.setProp(att[0], att[1]);
                feature.setProp(att[0], att[1]);
            }
            feature.setProp("text", strAux1);
        }
        if (grp.hasCode(2)) {
            String strAux2 = grp.getDataAsString(2);
            tagString = strAux2 = DxfConvTexts.ConvertText(strAux2);
            att[0] = DxfConvTexts.ConvertText(tagString);
            tagDefined = true;
            if (defValDefined) {
                insFea.setProp(att[0], att[1]);
                ptFea.setProp(att[0], att[1]);
                feature.setProp(att[0], att[1]);
            }
        }
        if (grp.hasCode(7)) {
            textStyleName = grp.getDataAsString(7);
            textStyleName = DxfConvTexts.ConvertText(textStyleName);
        }
        if (grp.hasCode(70)) {
            attributeFlags = grp.getDataAsInt(70);
        }
        if (grp.hasCode(40)) {
            Double heightD = new Double(grp.getDataAsDouble(40));
            String heightS = heightD.toString();
            feature.setProp("textHeight", heightS);
        } else {
            feature.setProp("textHeight", "20.0");
        }
        if (grp.hasCode(50)) {
            Double rotD = new Double(grp.getDataAsDouble(50));
            String rotS = rotD.toString();
            feature.setProp("textRotation", rotS);
        } else {
            feature.setProp("textRotation", "0.0");
        }
        x = grp.getDataAsDouble(10);
        y = grp.getDataAsDouble(20);
        z = grp.getDataAsDouble(30);
        if (grp.hasCode(210)) {
            extx = grp.getDataAsDouble(210);
        }
        if (grp.hasCode(220)) {
            exty = grp.getDataAsDouble(220);
        }
        if (grp.hasCode(230)) {
            extz = grp.getDataAsDouble(230);
        }
        org.cresques.geo.Point3D point_in = new org.cresques.geo.Point3D(x, y, z);
        org.cresques.geo.Point3D xtru = new org.cresques.geo.Point3D(extx, exty, extz);
        org.cresques.geo.Point3D point_out = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in, (org.cresques.geo.Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        z = point_out.getZ();
        Double doub2 = new Double(z);
        feature.setProp("elevation", doub2.toString());
        if (z != 0.0) {
            this.dxf3DFile = true;
        }
        point.add(new org.cresques.geo.Point3D(x, y, z));
        feature.setGeometry((Geometry)point);
        this.completeAttributes(feature);
        if (attributeFlags == 8) {
            if (!this.addingToBlock) {
                this.features.add((Extent.Has)feature);
            } else {
                this.blk.add((Extent.Has)feature);
            }
        }
    }

    @Override
    public void createBlock(DxfGroupVector grp) throws Exception {
        this.blk = new FeatureCollection(this.proj);
        org.cresques.geo.Point3D basePoint = new org.cresques.geo.Point3D();
        String blockName = "";
        this.addingToBlock = true;
        this.blkList.add(this.iterator, this.blk);
        if (grp.hasCode(8)) {
            this.blk.setProp("layer", grp.getDataAsString(8));
        }
        if (grp.hasCode(62)) {
            Integer integer = new Integer(grp.getDataAsInt(62));
            String string = integer.toString();
            this.blk.setProp("color", string);
            this.blk.setProp("colorByLayer", "false");
        } else {
            DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
            int clr = layer.colorNumber;
            Integer integer = new Integer(clr);
            String string = integer.toString();
            this.blk.setProp("color", string);
            this.blk.setProp("colorByLayer", "true");
        }
        if (grp.hasCode(1)) {
            blockName = grp.getDataAsString(1);
            this.blk.setProp("blockName", blockName);
        }
        if (grp.hasCode(2)) {
            blockName = grp.getDataAsString(2);
            this.blk.setProp("blockName", blockName);
        }
        if (grp.hasCode(10)) {
            Double basePointX = new Double(grp.getDataAsDouble(10));
            basePoint.X = grp.getDataAsDouble(10);
            this.blk.setProp("basePointX", basePointX.toString());
        }
        if (grp.hasCode(20)) {
            Double basePointY = new Double(grp.getDataAsDouble(20));
            basePoint.Y = grp.getDataAsDouble(20);
            this.blk.setProp("basePointY", basePointY.toString());
        }
        if (grp.hasCode(30)) {
            Double basePointZ = new Double(grp.getDataAsDouble(30));
            basePoint.Z = grp.getDataAsDouble(30);
            if (basePoint.getZ() != 0.0) {
                this.dxf3DFile = true;
            }
            this.blk.setProp("basePointZ", basePointZ.toString());
        }
        if (grp.hasCode(70)) {
            Integer blockFlags = new Integer(grp.getDataAsInt(70));
            this.blk.setProp("blockFlags", blockFlags.toString());
        }
    }

    @Override
    public void endBlk(DxfGroupVector grp) throws Exception {
        this.setAddingToBlock(false);
        ++this.iterator;
    }

    @Override
    public void testBlocks() {
        Vector<FeatureCollection> blkList = this.getBlkList();
        FeatureCollection block = null;
        Feature feature = null;
        InsPoint3D insert = null;
        int i = 0;
        while (i < blkList.size()) {
            block = blkList.get(i);
            int aux = block.size();
            int j = 0;
            while (j < aux) {
                feature = (Feature)block.get(j);
                if (feature.getGeometry() instanceof InsPoint3D) {
                    insert = (InsPoint3D)feature.getGeometry();
                    String nomBlock = insert.getBlockName();
                    if (!insert.getBlockFound()) {
                        insert.encuentraBloque(nomBlock);
                    }
                }
                ++j;
            }
            ++i;
        }
    }

    private void gestionaInsert(Feature feature) {
        Feature feature2 = null;
        Point3D point = null;
        LineString3D lineString3D = null;
        Polygon3D polygon3D = null;
        InsPoint3D insert = new InsPoint3D();
        insert = (InsPoint3D)feature.getGeometry();
        double bPointX = 0.0;
        double bPointY = 0.0;
        double bPointZ = 0.0;
        if (!insert.getBlockFound()) {
            System.err.println(String.valueOf(I18N.getString("org.cresques.px.dxf.DxfFeatureMaker.block-not-found")) + insert.getBlockName() + " " + insert.toString());
            return;
        }
        String blockName = insert.getBlockName();
        bPointX = Double.parseDouble(insert.getBlock().getProp("basePointX"));
        bPointY = Double.parseDouble(insert.getBlock().getProp("basePointY"));
        bPointZ = Double.parseDouble(insert.getBlock().getProp("basePointZ"));
        double sFactorX = insert.getScaleFactor().getX();
        double sFactorY = insert.getScaleFactor().getY();
        double sFactorZ = insert.getScaleFactor().getZ();
        double rAngleGra = insert.getRotAngle();
        double rAngleRad = rAngleGra * Math.PI / 180.0;
        InsPoint3D insert2 = null;
        HashSet<Feature> blockComponents = new HashSet<Feature>();
        int i = 0;
        while (i < insert.getBlock().size()) {
            String[] att;
            Feature feature3;
            double laX;
            org.cresques.geo.Point3D[] points;
            double laZ;
            double laY;
            double laX2;
            feature2 = (Feature)insert.getBlock().get(i);
            if ((feature2.getProp("colorByLayer").equals("false") || feature2.getProp("layer").equals("0")) && !feature.getProp("layer").equals("0")) {
                feature2.setProp("color", feature.getProp("color"));
            }
            feature2.setProp("layer", feature.getProp("layer"));
            org.cresques.geo.Point3D point1 = new org.cresques.geo.Point3D();
            org.cresques.geo.Point3D point11 = new org.cresques.geo.Point3D();
            org.cresques.geo.Point3D pointAux = null;
            if (feature2.getGeometry() instanceof InsPoint3D) {
                insert2 = (InsPoint3D)feature2.getGeometry();
                point1 = insert2.getPoint3D(0);
                pointAux = new org.cresques.geo.Point3D(point1.getX() - bPointX, point1.getY() - bPointY, point1.getZ() - bPointZ);
                laX2 = insert.getPoint3D(0).getX() + (pointAux.getX() * sFactorX * Math.cos(rAngleRad) + pointAux.getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                laY = insert.getPoint3D(0).getY() + (pointAux.getX() * sFactorX * Math.sin(rAngleRad) + pointAux.getY() * sFactorY * Math.cos(rAngleRad));
                laZ = insert.getPoint3D(0).getZ() + pointAux.getZ() * sFactorZ;
                point11 = new org.cresques.geo.Point3D(laX2, laY, laZ);
                InsPoint3D insert3 = new InsPoint3D();
                insert3.add(point11);
                insert3.setBlkList(insert2.getBlkList());
                insert3.setBlock(insert2.getBlock());
                insert3.setBlockName(insert2.getBlockName());
                insert3.setRotAngle(insert2.getRotAngle());
                org.cresques.geo.Point3D newScale = new org.cresques.geo.Point3D(insert2.getScaleFactor().getX() * sFactorX, insert2.getScaleFactor().getY() * sFactorY, insert2.getScaleFactor().getZ() * sFactorZ);
                insert3.setScaleFactor(newScale);
                Feature feature32 = new Feature();
                feature32.setProp("layer", feature2.getProp("layer"));
                feature32.setProp("color", feature2.getProp("color"));
                feature32.setProp("dxfEntity", feature2.getProp("dxfEntity"));
                feature32.setProp("elevation", feature2.getProp("elevation"));
                feature32.setProp("blockname", blockName);
                int j = 0;
                while (j < this.attributes.size()) {
                    String[] att2 = new String[2];
                    att2 = this.attributes.get(j);
                    feature32.setProp(att2[0], feature2.getProp(att2[0]));
                    ++j;
                }
                feature32.setGeometry((Geometry)insert3);
                this.gestionaInsert(feature32);
            } else if (feature2.getGeometry() instanceof LineString3D) {
                lineString3D = (LineString3D)feature2.getGeometry();
                LineString3D lineString2 = new LineString3D();
                points = new org.cresques.geo.Point3D[lineString3D.pointNr()];
                org.cresques.geo.Point3D[] pointss = new org.cresques.geo.Point3D[lineString3D.pointNr()];
                int j = 0;
                while (j < lineString3D.pointNr()) {
                    points[j] = (org.cresques.geo.Point3D)lineString3D.get(j);
                    pointss[j] = new org.cresques.geo.Point3D();
                    pointAux = new org.cresques.geo.Point3D(points[j].getX() - bPointX, points[j].getY() - bPointY, points[j].getZ() - bPointZ);
                    laX = insert.getPoint3D(0).getX() + (pointAux.getX() * sFactorX * Math.cos(rAngleRad) + pointAux.getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                    double laY2 = insert.getPoint3D(0).getY() + (pointAux.getX() * sFactorX * Math.sin(rAngleRad) + pointAux.getY() * sFactorY * Math.cos(rAngleRad));
                    double laZ2 = insert.getPoint3D(0).getZ() + pointAux.getZ() * sFactorZ;
                    pointss[j] = new org.cresques.geo.Point3D(laX, laY2, laZ2);
                    lineString2.add(pointss[j]);
                    ++j;
                }
                feature3 = new Feature();
                feature3.setProp("layer", feature2.getProp("layer"));
                feature3.setProp("color", feature2.getProp("color"));
                feature3.setProp("dxfEntity", feature2.getProp("dxfEntity"));
                if (feature2.getProp("elevation") != null) {
                    feature3.setProp("elevation", feature2.getProp("elevation"));
                }
                feature3.setProp("thickness", feature2.getProp("thickness"));
                feature3.setProp("blockname", blockName);
                int j2 = 0;
                while (j2 < this.attributes.size()) {
                    att = new String[2];
                    att = this.attributes.get(j2);
                    String str = att[0];
                    feature3.setProp(str, feature2.getProp(str));
                    ++j2;
                }
                feature3.setGeometry((Geometry)lineString2);
                if (!this.addingToBlock) {
                    this.features.add((Extent.Has)feature3);
                }
                blockComponents.add(feature3);
            } else if (feature2.getGeometry() instanceof Polygon3D) {
                polygon3D = (Polygon3D)feature2.getGeometry();
                Polygon3D polygon2 = new Polygon3D();
                points = new org.cresques.geo.Point3D[polygon3D.pointNr()];
                org.cresques.geo.Point3D[] pointss = new org.cresques.geo.Point3D[polygon3D.pointNr()];
                int j = 0;
                while (j < polygon3D.pointNr()) {
                    points[j] = (org.cresques.geo.Point3D)polygon3D.get(j);
                    pointss[j] = new org.cresques.geo.Point3D();
                    pointAux = new org.cresques.geo.Point3D(points[j].getX() - bPointX, points[j].getY() - bPointY, points[j].getZ() - bPointZ);
                    laX = insert.getPoint3D(0).getX() + (pointAux.getX() * sFactorX * Math.cos(rAngleRad) + pointAux.getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                    double laY3 = insert.getPoint3D(0).getY() + (pointAux.getX() * sFactorX * Math.sin(rAngleRad) + pointAux.getY() * sFactorY * Math.cos(rAngleRad));
                    double laZ3 = insert.getPoint3D(0).getZ() + pointAux.getZ() * sFactorZ;
                    pointss[j] = new org.cresques.geo.Point3D(laX, laY3, laZ3);
                    polygon2.add(pointss[j]);
                    ++j;
                }
                feature3 = new Feature();
                feature3.setProp("layer", feature2.getProp("layer"));
                feature3.setProp("color", feature2.getProp("color"));
                feature3.setProp("dxfEntity", feature2.getProp("dxfEntity"));
                if (feature2.getProp("elevation") != null) {
                    feature3.setProp("elevation", feature2.getProp("elevation"));
                }
                feature3.setProp("thickness", feature2.getProp("thickness"));
                feature3.setProp("blockname", blockName);
                int j3 = 0;
                while (j3 < this.attributes.size()) {
                    att = new String[2];
                    att = this.attributes.get(j3);
                    feature3.setProp(att[0], feature2.getProp(att[0]));
                    ++j3;
                }
                feature3.setGeometry((Geometry)polygon2);
                if (!this.addingToBlock) {
                    this.features.add((Extent.Has)feature3);
                }
                blockComponents.add(feature3);
            } else if (feature2.getGeometry() instanceof Point3D) {
                point = (Point3D)feature2.getGeometry();
                point1 = point.getPoint3D(0);
                pointAux = new org.cresques.geo.Point3D(point1.getX() - bPointX, point1.getY() - bPointY, point1.getZ() - bPointZ);
                laX2 = insert.getPoint3D(0).getX() + (pointAux.getX() * sFactorX * Math.cos(rAngleRad) + pointAux.getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                laY = insert.getPoint3D(0).getY() + (pointAux.getX() * sFactorX * Math.sin(rAngleRad) + pointAux.getY() * sFactorY * Math.cos(rAngleRad));
                laZ = insert.getPoint3D(0).getZ() + pointAux.getZ() * sFactorZ;
                point11 = new org.cresques.geo.Point3D(laX2, laY, laZ);
                Point3D pointt = new Point3D();
                pointt.add(point11);
                Feature feature33 = new Feature();
                feature33.setProp("layer", feature2.getProp("layer"));
                feature33.setProp("color", feature2.getProp("color"));
                feature33.setProp("dxfEntity", feature2.getProp("dxfEntity"));
                feature33.setProp("elevation", feature2.getProp("elevation"));
                feature33.setProp("thickness", feature2.getProp("thickness"));
                feature33.setProp("blockname", blockName);
                if (point.isTextPoint()) {
                    feature33.setProp("text", feature2.getProp("text"));
                    feature33.setProp("textHeight", feature2.getProp("textHeight"));
                    double auxR = Double.parseDouble(feature2.getProp("textRotation"));
                    feature33.setProp("textRotation", Double.toString(auxR += rAngleGra));
                    pointt.setTextPoint(true);
                }
                int j = 0;
                while (j < this.attributes.size()) {
                    String[] att3 = new String[2];
                    att3 = this.attributes.get(j);
                    feature33.setProp(att3[0], feature2.getProp(att3[0]));
                    ++j;
                }
                feature33.setGeometry((Geometry)pointt);
                this.features.add((Extent.Has)feature33);
                blockComponents.add(feature33);
            } else {
                System.out.println("gestionaInsert(): " + I18N.getString("org.cresques.px.dxf.DxfFeatureMaker.unknown-element-found"));
            }
            ++i;
        }
        if (!this.blockNameToComponentsMap.containsKey(blockName)) {
            this.blockNameToComponentsMap.put(blockName, blockComponents);
        }
    }

    private void addFace(int[] face) {
        this.hasFaces = true;
        if (this.faces == null) {
            this.faces = new Vector();
        }
        this.faces.add(face);
    }

    @Override
    public void depureAttributes() {
        String[] att;
        String[] lastAtt = new String[2];
        int i = 0;
        while (i < this.attributes.size()) {
            att = this.attributes.get(i);
            int j = i + 1;
            while (j < this.attributes.size()) {
                String st1;
                String[] st = this.attributes.get(j);
                String st2 = st[0];
                if (st2.equals(st1 = att[0])) {
                    this.attributes.remove(j);
                }
                if (i == this.attributes.size() - 1) {
                    lastAtt = att;
                }
                ++j;
            }
            ++i;
        }
        i = this.attributes.size() - 2;
        while (i >= 0) {
            String st1;
            att = this.attributes.get(i);
            String st2 = att[0];
            if (st2.equals(st1 = lastAtt[0])) {
                this.attributes.remove(i);
            }
            --i;
        }
    }

    private void completeAttributes(Feature feature) {
        if (feature == null) {
            return;
        }
        int i = 0;
        while (i < this.attributes.size()) {
            String[] att = new String[2];
            att = this.attributes.get(i);
            feature.setProp(att[0], att[1]);
            ++i;
        }
    }

    private void copyAttributes(Feature feaInsert) {
        Feature feature = null;
        InsPoint3D insert = new InsPoint3D();
        insert = (InsPoint3D)feaInsert.getGeometry();
        int i = 0;
        while (i < insert.getBlock().size()) {
            feature = (Feature)insert.getBlock().get(i);
            int j = 0;
            while (j < this.attributes.size()) {
                String[] att = new String[2];
                att = this.attributes.get(j);
                String value = feaInsert.getProp(att[0]);
                if (StringUtils.isEmpty((String)value) && StringUtils.isEmpty((String)(value = feaInsert.getProp(StringUtils.swapCase((String)att[0]))))) {
                    value = "";
                }
                feature.setProp(att[0], value);
                ++j;
            }
            ++i;
        }
    }

    private void setNewAttributes() {
        int i = 0;
        while (i < this.features.size()) {
            Feature fea = new Feature();
            fea = (Feature)this.features.get(i);
            this.completeAttributes(fea);
            ++i;
        }
        i = 0;
        while (i < this.blkList.size()) {
            FeatureCollection bloque = this.blkList.get(i);
            int j = 0;
            while (j < bloque.size()) {
                Feature fea = new Feature();
                fea = (Feature)bloque.get(j);
                this.completeAttributes(fea);
                ++j;
            }
            ++i;
        }
    }

    @Override
    public Vector<String[]> getAttributes() {
        return this.attributes;
    }

    @Override
    public Extent getExtent() {
        Feature feature2 = new Feature();
        Extent extent = new Extent();
        for (Feature feature2 : this.features) {
            extent.add(feature2.getExtent());
        }
        return extent;
    }

    public void setProjection(IProjection p) {
        this.proj = p;
    }

    public void reProject(ICoordTrans rp) {
        Feature feature2 = new Feature();
        Extent extent = new Extent();
        for (Feature feature2 : this.features) {
            ((Projected)feature2).reProject(rp);
            extent.add(feature2.getExtent());
        }
        this.setProjection(rp.getPDest());
    }

    public IProjection getProjection() {
        return this.proj;
    }

    @Override
    public IObjList getObjects() {
        return this.features;
    }

    public void draw(Graphics2D g, ViewPortData vp) {
        for (Feature feature : this.features) {
            Extent extent = feature.getExtent();
            if (vp.getExtent().minX() > extent.maxX() || vp.getExtent().minY() > extent.maxY() || vp.getExtent().maxX() < extent.minX() || vp.getExtent().maxY() < extent.minY()) continue;
            feature.draw(g, vp);
        }
    }

    public static Vector createArc(Point2D coord1, Point2D coord2, double bulge) {
        return new DxfCalArcs(coord1, coord2, bulge).getPoints(1.0);
    }

    @Override
    public Vector<FeatureCollection> getBlkList() {
        return this.blkList;
    }

    public DxfEntityList getDxfEntityList() {
        return null;
    }

    public DxfBlock getBlk() {
        return null;
    }

    @Override
    public void createEllipse(DxfGroupVector grp) throws Exception {
        String string;
        Double doub;
        double cx = 0.0;
        double cy = 0.0;
        double cz = 0.0;
        double x_end_point_major_axis = 0.0;
        double y_end_point_major_axis = 0.0;
        double z_end_point_major_axis = 0.0;
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        double ratio_minor_to_major_axis = 1.0;
        double start = 0.0;
        double end = Math.PI * 2;
        LineString3D lineString3D = new LineString3D();
        Polygon3D polygon3D = new Polygon3D();
        Feature feaBordes = new Feature();
        Feature feaFondos = new Feature();
        feaBordes.setProp("dxfEntity", "Ellipse");
        feaFondos.setProp("dxfEntity", "Ellipse");
        if (grp.hasCode(8)) {
            feaBordes.setProp("layer", grp.getDataAsString(8));
        }
        feaFondos.setProp("layer", grp.getDataAsString(8));
        if (grp.hasCode(39)) {
            doub = new Double(grp.getDataAsDouble(39));
            string = doub.toString();
            feaBordes.setProp("thickness", string);
            feaFondos.setProp("thickness", string);
        } else {
            doub = new Double(0.0);
            feaBordes.setProp("thickness", doub.toString());
            feaFondos.setProp("thickness", doub.toString());
        }
        if (grp.hasCode(62)) {
            Integer integer = new Integer(grp.getDataAsInt(62));
            string = integer.toString();
            feaBordes.setProp("color", string);
            feaFondos.setProp("color", string);
            feaBordes.setProp("colorByLayer", "false");
            feaFondos.setProp("colorByLayer", "false");
        } else {
            DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
            int clr = layer.colorNumber;
            Integer integer = new Integer(clr);
            String string2 = integer.toString();
            feaBordes.setProp("color", string2);
            feaFondos.setProp("color", string2);
            feaBordes.setProp("colorByLayer", "true");
            feaFondos.setProp("colorByLayer", "true");
        }
        cx = grp.getDataAsDouble(10);
        cy = grp.getDataAsDouble(20);
        if (grp.hasCode(30)) {
            cz = grp.getDataAsDouble(30);
        }
        x_end_point_major_axis = grp.getDataAsDouble(11);
        y_end_point_major_axis = grp.getDataAsDouble(21);
        if (grp.hasCode(31)) {
            z_end_point_major_axis = grp.getDataAsDouble(31);
        }
        if (grp.hasCode(40)) {
            ratio_minor_to_major_axis = grp.getDataAsDouble(40);
        }
        double d = ratio_minor_to_major_axis * Math.sqrt(x_end_point_major_axis * x_end_point_major_axis + y_end_point_major_axis * y_end_point_major_axis);
        feaBordes.setProp("distancia", Double.toString(d));
        feaFondos.setProp("distancia", Double.toString(d));
        double initx = cx - x_end_point_major_axis;
        feaBordes.setProp("initX", Double.toString(initx));
        feaFondos.setProp("initX", Double.toString(initx));
        double inity = cy - y_end_point_major_axis;
        feaBordes.setProp("initY", Double.toString(inity));
        feaFondos.setProp("initY", Double.toString(inity));
        double endx = cx + x_end_point_major_axis;
        feaBordes.setProp("endX", Double.toString(endx));
        feaFondos.setProp("endX", Double.toString(endx));
        double endy = cy + y_end_point_major_axis;
        feaBordes.setProp("endY", Double.toString(endy));
        feaFondos.setProp("endY", Double.toString(endy));
        Point2D c = this.proj.createPoint(cx, cy);
        double r_major_axis_2D = Math.sqrt(x_end_point_major_axis * x_end_point_major_axis + y_end_point_major_axis * y_end_point_major_axis);
        double r_minor_axis_2D = r_major_axis_2D * ratio_minor_to_major_axis;
        double rotation_angle = Math.atan2(y_end_point_major_axis, x_end_point_major_axis);
        if (grp.hasCode(210)) {
            extx = grp.getDataAsDouble(210);
        }
        if (grp.hasCode(220)) {
            exty = grp.getDataAsDouble(220);
        }
        if (grp.hasCode(230)) {
            extz = grp.getDataAsDouble(230);
        }
        org.cresques.geo.Point3D point_in = new org.cresques.geo.Point3D(cx, cy, cz);
        org.cresques.geo.Point3D xtru = new org.cresques.geo.Point3D(extx, exty, extz);
        org.cresques.geo.Point3D point_out = DxfCalXtru.CalculateXtru((org.cresques.geo.Point3D)point_in, (org.cresques.geo.Point3D)xtru);
        cx = point_out.getX();
        cy = point_out.getY();
        cz = point_out.getZ();
        Double doub2 = new Double(cz);
        feaBordes.setProp("elevation", doub2.toString());
        feaFondos.setProp("elevation", doub2.toString());
        if (cz != 0.0) {
            this.dxf3DFile = true;
        }
        org.cresques.geo.Point3D center = new org.cresques.geo.Point3D(c.getX(), c.getY(), cz);
        org.cresques.geo.Point3D[] pts = new org.cresques.geo.Point3D[360];
        int angulo = 0;
        angulo = 0;
        while (angulo < 360) {
            pts[angulo] = new org.cresques.geo.Point3D(center.getX() + r_major_axis_2D * Math.cos(Math.toRadians(angulo)), center.getY() + r_minor_axis_2D * Math.sin(Math.toRadians(angulo)), center.getZ());
            if (pts.length == 1) {
                this.firstPt = pts[angulo];
            }
            ++angulo;
        }
        AffineTransform at = new AffineTransform();
        at.rotate(rotation_angle, cx, cy);
        int i = 0;
        while (i < pts.length) {
            org.cresques.geo.Point3D pAux = pts[i];
            Point2D pRot = at.transform((Point2D)pAux, null);
            lineString3D.add(pRot);
            polygon3D.add(pRot);
            ++i;
        }
        feaBordes.setGeometry((Geometry)lineString3D);
        feaFondos.setGeometry((Geometry)polygon3D);
        this.completeAttributes(feaBordes);
        this.completeAttributes(feaFondos);
        if (!this.addingToBlock) {
            this.features.add((Extent.Has)feaFondos);
        } else {
            this.blk.add((Extent.Has)feaFondos);
        }
    }

    @Override
    public boolean isDxf3DFile() {
        return this.dxf3DFile;
    }

    public void setDxf3DFile(boolean dxf3DFile) {
        this.dxf3DFile = dxf3DFile;
    }

    public Map<String, Set<Feature>> getBlockNameToComponentsMap() {
        return this.blockNameToComponentsMap;
    }
}

