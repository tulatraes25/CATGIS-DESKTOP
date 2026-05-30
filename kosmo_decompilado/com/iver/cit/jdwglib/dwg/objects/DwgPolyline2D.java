/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.cit.gvsig.fmap.core.FPolyline2D
 *  com.iver.cit.gvsig.fmap.core.IGeometry
 *  com.iver.cit.gvsig.fmap.core.ShapeFactory
 *  com.iver.cit.jdwglib.dwg.DwgFile
 *  com.iver.cit.jdwglib.dwg.DwgHandleReference
 *  com.iver.cit.jdwglib.dwg.DwgObject
 *  com.iver.cit.jdwglib.dwg.IDwg2FMap
 *  com.iver.cit.jdwglib.dwg.IDwg3DTestable
 *  com.iver.cit.jdwglib.dwg.IDwgBlockMember
 *  com.iver.cit.jdwglib.dwg.IDwgExtrusionable
 *  com.iver.cit.jdwglib.dwg.IDwgPolyline
 *  com.iver.cit.jdwglib.dwg.IDwgVertex
 *  com.iver.cit.jdwglib.dwg.objects.DwgSeqend
 *  com.iver.cit.jdwglib.dwg.objects.DwgVertex2D
 *  com.iver.cit.jdwglib.util.AcadExtrusionCalculator
 *  com.iver.cit.jdwglib.util.FMapUtil
 *  com.iver.cit.jdwglib.util.GisModelCurveCalculator
 */
package com.iver.cit.jdwglib.dwg.objects;

import com.iver.cit.gvsig.fmap.core.FPolyline2D;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.jdwglib.dwg.DwgFile;
import com.iver.cit.jdwglib.dwg.DwgHandleReference;
import com.iver.cit.jdwglib.dwg.DwgObject;
import com.iver.cit.jdwglib.dwg.IDwg2FMap;
import com.iver.cit.jdwglib.dwg.IDwg3DTestable;
import com.iver.cit.jdwglib.dwg.IDwgBlockMember;
import com.iver.cit.jdwglib.dwg.IDwgExtrusionable;
import com.iver.cit.jdwglib.dwg.IDwgPolyline;
import com.iver.cit.jdwglib.dwg.IDwgVertex;
import com.iver.cit.jdwglib.dwg.objects.DwgSeqend;
import com.iver.cit.jdwglib.dwg.objects.DwgVertex2D;
import com.iver.cit.jdwglib.util.AcadExtrusionCalculator;
import com.iver.cit.jdwglib.util.FMapUtil;
import com.iver.cit.jdwglib.util.GisModelCurveCalculator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.saig.jump.lang.I18N;

public class DwgPolyline2D
extends DwgObject
implements IDwgPolyline,
IDwgExtrusionable,
IDwg3DTestable,
IDwg2FMap,
IDwgBlockMember {
    private int flags;
    private int curveType;
    private double initWidth;
    private double endWidth;
    private double thickness;
    private double elevation;
    private double[] extrusion;
    private DwgHandleReference firstVertexHandle = null;
    private DwgHandleReference lastVertexHandle = null;
    private DwgHandleReference seqendHandle = null;
    private List<double[]> vertices = new ArrayList<double[]>();
    private double[] bulges;

    public DwgPolyline2D(int index) {
        super(index);
    }

    public DwgHandleReference getFirstVertexHandle() {
        return this.firstVertexHandle;
    }

    public void setFirstVertexHandle(DwgHandleReference firstVertexHandle) {
        this.firstVertexHandle = firstVertexHandle;
    }

    public int getFlags() {
        return this.flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public DwgHandleReference getLastVertexHandle() {
        return this.lastVertexHandle;
    }

    public void setLastVertexHandle(DwgHandleReference lastVertexHandle) {
        this.lastVertexHandle = lastVertexHandle;
    }

    public List<double[]> getPts() {
        return this.vertices;
    }

    public void setPts(List<double[]> pts) {
        this.vertices = pts;
    }

    public double[] getBulges() {
        return this.bulges;
    }

    public void setBulges(double[] bulges) {
        this.bulges = bulges;
    }

    public double getInitWidth() {
        return this.initWidth;
    }

    public void setInitWidth(double initWidth) {
        this.initWidth = initWidth;
    }

    public DwgHandleReference getSeqendHandle() {
        return this.seqendHandle;
    }

    public void setSeqendHandle(DwgHandleReference seqendHandle) {
        this.seqendHandle = seqendHandle;
    }

    public double getThickness() {
        return this.thickness;
    }

    public void setThickness(double thickness) {
        this.thickness = thickness;
    }

    public int getCurveType() {
        return this.curveType;
    }

    public void setCurveType(int curveType) {
        this.curveType = curveType;
    }

    public double getElevation() {
        return this.elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public double getEndWidth() {
        return this.endWidth;
    }

    public void setEndWidth(double endWidth) {
        this.endWidth = endWidth;
    }

    public double[] getExtrusion() {
        return this.extrusion;
    }

    public void setExtrusion(double[] extrusion) {
        this.extrusion = extrusion;
    }

    public void calculateGisModel(DwgFile dwgFile) {
        int flags = this.getFlags();
        int firstHandle = this.getFirstVertexHandle().getOffset();
        int lastHandle = this.getLastVertexHandle().getOffset();
        ArrayList<double[]> pts = new ArrayList<double[]>();
        ArrayList<Double> bulges = new ArrayList<Double>();
        double[] pt = new double[3];
        double bulge = 0.0;
        DwgObject first = dwgFile.getDwgObjectFromHandle(firstHandle);
        DwgObject last = dwgFile.getDwgObjectFromHandle(lastHandle);
        if (first == null || last == null) {
            System.out.println(I18N.getString("com.iver.cit.jdwglib.dwg.objects.DwgPolyline2D.Polyline2D-with-null-initial-or-final-vertex"));
            return;
        }
        if (!(first instanceof DwgVertex2D)) {
            System.out.println(String.valueOf(I18N.getString("com.iver.cit.jdwglib.dwg.objects.DwgPolyline2D.First-vertex-of-Polyline2D-is")) + first.getClass().getName());
            return;
        }
        if (!(last instanceof DwgVertex2D)) {
            System.out.println(String.valueOf(I18N.getString("com.iver.cit.jdwglib.dwg.objects.DwgPolyline2D.First-vertex-of-Polyline2D-is")) + first.getClass().getName());
            return;
        }
        int firstObjIdx = dwgFile.getIndexOf(first);
        int lastObjIdx = dwgFile.getIndexOf(last);
        if (firstObjIdx == -1 || lastObjIdx == -1) {
            System.out.println("Calculate GIS Model: Problemas en la LinkedList: 1\u00ba=" + firstObjIdx + ",\u00daltimo=" + lastObjIdx);
            return;
        }
        int i = firstObjIdx;
        while (i <= lastObjIdx) {
            DwgObject obj = dwgFile.getDwgObject(i);
            if (obj instanceof DwgVertex2D) {
                DwgVertex2D vertex = (DwgVertex2D)obj;
                pt = vertex.getPoint();
                pts.add(new double[]{pt[0], pt[1]});
                bulge = vertex.getBulge();
                bulges.add(new Double(bulge));
            } else {
                System.out.println(String.valueOf(I18N.getString("com.iver.cit.jdwglib.dwg.objects.DwgPolyline2D.Found")) + obj.getClass().getName() + I18N.getString("com.iver.cit.jdwglib.dwg.objects.DwgPolyline2D.in-vertex-list-of-Polyline2D"));
            }
            ++i;
        }
        if (pts.size() > 0) {
            int j;
            ArrayList<double[]> newPts = new ArrayList<double[]>();
            if ((flags & 1) == 1) {
                j = 0;
                while (j < pts.size()) {
                    newPts.add((double[])pts.get(j));
                    ++j;
                }
                newPts.add((double[])pts.get(0));
                bulges.add(new Double(0.0));
            } else {
                j = 0;
                while (j < pts.size()) {
                    newPts.add((double[])pts.get(j));
                    ++j;
                }
            }
            double[] bs = new double[bulges.size()];
            int j2 = 0;
            while (j2 < bulges.size()) {
                bs[j2] = (Double)bulges.get(j2);
                ++j2;
            }
            this.setBulges(bs);
            List points = GisModelCurveCalculator.calculateGisModelBulge(newPts, (double[])bs);
            this.setPts(points);
        } else {
            System.out.println(String.valueOf(I18N.getString("com.iver.cit.jdwglib.dwg.objects.DwgPolyline2D.Found-polyline-without-points")) + "...");
        }
    }

    public void calculateGisModel(List<DwgObject> dwgObjects) {
        int flags = this.getFlags();
        int firstHandle = this.getFirstVertexHandle().getOffset();
        int lastHandle = this.getLastVertexHandle().getOffset();
        ArrayList<double[]> pts = new ArrayList<double[]>();
        ArrayList<Double> bulges = new ArrayList<Double>();
        double[] pt = new double[3];
        int j = 0;
        while (j < dwgObjects.size()) {
            int vertexHandle;
            DwgObject firstVertex = dwgObjects.get(j);
            if (firstVertex instanceof DwgVertex2D && (vertexHandle = firstVertex.getHandle().getOffset()) == firstHandle) {
                int k = 0;
                while (true) {
                    DwgObject vertex = dwgObjects.get(j + k);
                    int vHandle = vertex.getHandle().getOffset();
                    if (vertex instanceof DwgVertex2D) {
                        pt = ((DwgVertex2D)vertex).getPoint();
                        pts.add(new double[]{pt[0], pt[1]});
                        double bulge = ((DwgVertex2D)vertex).getBulge();
                        bulges.add(new Double(bulge));
                        ++k;
                        if (vHandle != lastHandle || !(vertex instanceof DwgVertex2D)) continue;
                        break;
                    }
                    if (!(vertex instanceof DwgSeqend)) continue;
                    ++k;
                }
            }
            ++j;
        }
        if (pts.size() > 0) {
            int j2;
            ArrayList<double[]> newPts = new ArrayList<double[]>();
            if ((flags & 1) == 1) {
                j2 = 0;
                while (j2 < pts.size()) {
                    newPts.add((double[])pts.get(j2));
                    ++j2;
                }
                newPts.add((double[])pts.get(0));
                bulges.add(new Double(0.0));
            } else {
                j2 = 0;
                while (j2 < pts.size()) {
                    newPts.add((double[])pts.get(j2));
                    ++j2;
                }
            }
            double[] bs = new double[bulges.size()];
            int j3 = 0;
            while (j3 < bulges.size()) {
                bs[j3] = (Double)bulges.get(j3);
                ++j3;
            }
            this.setBulges(bs);
            List points = GisModelCurveCalculator.calculateGisModelBulge(newPts, (double[])bs);
            this.setPts(points);
        }
    }

    public void applyExtrussion() {
        if (this.getPts() == null) {
            return;
        }
        List<double[]> vertices = this.getPts();
        double[] polyline2DExt = this.getExtrusion();
        double elev = this.getElevation();
        double[][] polylinePoints3D = new double[vertices.size()][3];
        int j = 0;
        while (j < vertices.size()) {
            polylinePoints3D[j][0] = vertices.get(j)[0];
            polylinePoints3D[j][1] = vertices.get(j)[1];
            polylinePoints3D[j][2] = elev;
            polylinePoints3D[j] = AcadExtrusionCalculator.extrude2((double[])polylinePoints3D[j], (double[])polyline2DExt);
            ++j;
        }
        this.setElevation(elev);
        j = 0;
        while (j < vertices.size()) {
            vertices.add(new double[]{polylinePoints3D[j][0], polylinePoints3D[j][1]});
            ++j;
        }
        this.setPts(vertices);
    }

    public boolean has3DData() {
        return this.getElevation() != 0.0;
    }

    public double getZ() {
        return this.getElevation();
    }

    public IGeometry toFMapGeometry(boolean is3DFile) {
        FPolyline2D pline = null;
        List<double[]> points = this.getPts();
        double elev = this.getElevation();
        if (points != null) {
            if (is3DFile) {
                ArrayList<double[]> pline3D = new ArrayList<double[]>();
                int j = 0;
                while (j < points.size()) {
                    double[] point = new double[]{this.vertices.get(j)[0], this.vertices.get(j)[1], elev};
                    pline3D.add(point);
                    ++j;
                }
                pline = FMapUtil.points3DToFPolyline3D(pline3D);
            } else {
                pline = FMapUtil.points2DToFPolyline2D(points);
            }
        }
        return ShapeFactory.createGeometry(pline);
    }

    public String toFMapString(boolean is3DFile) {
        if (is3DFile) {
            return "FPolyline3D";
        }
        return "FPolyline2D";
    }

    public String toString() {
        return "Polyline2D";
    }

    public void transform2Block(double[] bPoint, Point2D insPoint, double[] scale, double rot, List dwgObjectsWithoutBlocks, Map handleObjWithoutBlocks, DwgFile callBack) {
        DwgPolyline2D transformedEntity = null;
        List<double[]> vertices = this.getPts();
        if (vertices != null) {
            ArrayList<double[]> transformedVertices = new ArrayList<double[]>();
            int i = 0;
            while (i < vertices.size()) {
                double[] pointAux = null;
                pointAux = new double[]{vertices.get(i)[0] - bPoint[0], vertices.get(i)[1] - bPoint[1]};
                double laX = insPoint.getX() + (pointAux[0] * scale[0] * Math.cos(rot) + pointAux[1] * scale[1] * -1.0 * Math.sin(rot));
                double laY = insPoint.getY() + (pointAux[0] * scale[0] * Math.sin(rot) + pointAux[1] * scale[1] * Math.cos(rot));
                transformedVertices.add(new double[]{laX, laY});
                ++i;
            }
            transformedEntity = (DwgPolyline2D)((Object)this.clone());
            transformedEntity.setPts(transformedVertices);
            transformedEntity.setElevation(this.getElevation() * scale[2]);
            dwgObjectsWithoutBlocks.add(transformedEntity);
            handleObjWithoutBlocks.put(new Integer(transformedEntity.getHandle().getOffset()), transformedEntity);
        }
    }

    public Object clone() {
        DwgPolyline2D obj = new DwgPolyline2D(this.index);
        this.fill(obj);
        return obj;
    }

    protected void fill(DwgObject obj) {
        super.fill(obj);
        DwgPolyline2D myObj = (DwgPolyline2D)obj;
        myObj.setBulges(this.bulges);
        myObj.setCurveType(this.curveType);
        myObj.setElevation(this.elevation);
        myObj.setEndWidth(this.endWidth);
        myObj.setExtrusion(this.extrusion);
        myObj.setFirstVertexHandle(this.firstVertexHandle);
        myObj.setFlags(this.flags);
        myObj.setInitWidth(this.initWidth);
        myObj.setLastVertexHandle(this.lastVertexHandle);
        myObj.setPts(this.vertices);
        myObj.setSeqendHandle(this.seqendHandle);
        myObj.setThickness(this.thickness);
    }

    public void addVertex(IDwgVertex vertex) {
        this.vertices.add(vertex.getPoint());
    }
}

