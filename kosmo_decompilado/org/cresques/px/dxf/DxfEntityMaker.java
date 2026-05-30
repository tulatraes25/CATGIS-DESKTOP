/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IProjection
 *  org.cresques.geo.Point3D
 *  org.cresques.geo.Projected
 *  org.cresques.px.Extent
 *  org.cresques.px.Extent$Has
 *  org.cresques.px.IObjList
 *  org.cresques.px.dxf.DxfBlock
 *  org.cresques.px.dxf.DxfCalXtru
 *  org.cresques.px.dxf.DxfEntityList
 *  org.cresques.px.dxf.DxfLayer
 *  org.cresques.px.dxf.DxfTable
 *  org.cresques.px.dxf.DxfTableItem
 */
package org.cresques.px.dxf;

import java.awt.geom.Point2D;
import java.util.Vector;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.cresques.geo.Point3D;
import org.cresques.geo.Projected;
import org.cresques.io.DxfFile;
import org.cresques.io.DxfGroup;
import org.cresques.io.DxfGroupVector;
import org.cresques.px.Extent;
import org.cresques.px.IObjList;
import org.cresques.px.dxf.DxfArc;
import org.cresques.px.dxf.DxfAttrib;
import org.cresques.px.dxf.DxfBlock;
import org.cresques.px.dxf.DxfCalXtru;
import org.cresques.px.dxf.DxfCircle;
import org.cresques.px.dxf.DxfConvTexts;
import org.cresques.px.dxf.DxfEllipse;
import org.cresques.px.dxf.DxfEntity;
import org.cresques.px.dxf.DxfEntityList;
import org.cresques.px.dxf.DxfInsert;
import org.cresques.px.dxf.DxfLayer;
import org.cresques.px.dxf.DxfLine;
import org.cresques.px.dxf.DxfLwPolyline;
import org.cresques.px.dxf.DxfPoint;
import org.cresques.px.dxf.DxfPolyline;
import org.cresques.px.dxf.DxfSolid;
import org.cresques.px.dxf.DxfTable;
import org.cresques.px.dxf.DxfTableItem;
import org.cresques.px.dxf.DxfText;

public class DxfEntityMaker
implements DxfFile.EntityFactory,
Projected {
    IProjection proj = null;
    DxfEntity lastEntity = null;
    DxfEntityList entities = null;
    Vector blkList = null;
    DxfBlock blk = null;
    DxfTable layers = null;
    double bulge = 0.0;
    double xtruX = 0.0;
    double xtruY = 0.0;
    double xtruZ = 1.0;
    int polylineFlag = 0;
    Point2D firstPt = new Point2D.Double();
    boolean addingToBlock = false;
    int iterator = 0;
    private Vector attributes = null;

    public DxfEntityMaker(IProjection proj) {
        this.proj = proj;
        this.layers = new DxfTable();
        this.entities = new DxfEntityList(proj);
        this.blkList = new Vector();
        this.attributes = new Vector();
    }

    @Override
    public Vector getBlkList() {
        return this.blkList;
    }

    @Override
    public IObjList getObjects() {
        return this.entities;
    }

    @Override
    public Extent getExtent() {
        return this.entities.getExtent();
    }

    @Override
    public void setAddingToBlock(boolean a) {
        this.addingToBlock = a;
    }

    @Override
    public void createLayer(DxfGroupVector grp) throws Exception {
        int color = grp.getDataAsInt(62);
        DxfLayer layer = new DxfLayer(grp.getDataAsString(2), Math.abs(grp.getDataAsInt(62)));
        if (color < 0) {
            layer.isOff = true;
        }
        layer.lType = grp.getDataAsString(6);
        layer.setFlags(grp.getDataAsInt(70));
        if ((layer.flags & 1) == 1) {
            layer.frozen = true;
        }
        if ((layer.flags & 2) == 2) {
            layer.frozen = true;
        }
        System.out.println("LAYER color=" + layer.getColor());
        this.layers.add((DxfTableItem)layer);
    }

    @Override
    public void createPolyline(DxfGroupVector grp) throws Exception {
        DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
        DxfPolyline entity = new DxfPolyline(this.proj, layer);
        if (grp.hasCode(5)) {
            String hexS = grp.getDataAsString(5);
            Integer hexI = Integer.decode("0x" + hexS);
            int hexi = hexI;
            entity.setHandle(hexi);
        } else {
            entity.setHandle(this.entities.size() + 40);
        }
        if (grp.hasCode(100)) {
            entity.setSubclassMarker(grp.getDataAsString(100));
        }
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double thickness = 0.0;
        if (grp.hasCode(10)) {
            x = grp.getDataAsDouble(10);
        }
        if (grp.hasCode(20)) {
            y = grp.getDataAsDouble(20);
        }
        if (grp.hasCode(30)) {
            z = grp.getDataAsDouble(30);
            entity.setElevation(z);
        }
        if (grp.hasCode(62)) {
            entity.dxfColor = grp.getDataAsInt(62);
        }
        if (grp.hasCode(66)) {
            entity.entitiesFollow = grp.getDataAsInt(66);
        }
        if (grp.hasCode(70)) {
            entity.flags = grp.getDataAsInt(70);
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
        if ((entity.flags & 1) == 1) {
            entity.closed = true;
        }
        this.lastEntity = entity;
    }

    @Override
    public void endSeq() throws Exception {
        if (this.lastEntity instanceof DxfPolyline) {
            DxfPolyline polyline = (DxfPolyline)this.lastEntity;
            if (polyline.closed) {
                Point2D pt;
                int i;
                Vector arc;
                int cnt;
                ((DxfPolyline)this.lastEntity).add(this.firstPt);
                if (this.bulge > 0.0) {
                    cnt = ((DxfPolyline)this.lastEntity).pts.size();
                    if (((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 2)).getX() != ((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 1)).getX() || ((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 2)).getY() != ((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 1)).getY()) {
                        arc = DxfPolyline.createArc((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 2), (Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 1), this.bulge);
                        ((DxfPolyline)this.lastEntity).pts.remove(cnt - 1);
                        i = 0;
                        while (i < arc.size()) {
                            pt = this.proj.createPoint(((Point2D)arc.get(i)).getX(), ((Point2D)arc.get(i)).getY());
                            ((DxfPolyline)this.lastEntity).add(pt);
                            if (((DxfPolyline)this.lastEntity).pts.size() == 1) {
                                this.firstPt = pt;
                            }
                            ++i;
                        }
                    }
                    this.bulge = 0.0;
                } else if (this.bulge < 0.0) {
                    cnt = ((DxfPolyline)this.lastEntity).pts.size();
                    if (((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 2)).getX() != ((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 1)).getX() || ((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 2)).getY() != ((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 1)).getY()) {
                        arc = DxfPolyline.createArc((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 2), (Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 1), this.bulge);
                        ((DxfPolyline)this.lastEntity).pts.remove(cnt - 1);
                        i = arc.size() - 1;
                        while (i >= 0) {
                            pt = this.proj.createPoint(((Point2D)arc.get(i)).getX(), ((Point2D)arc.get(i)).getY());
                            ((DxfPolyline)this.lastEntity).add(pt);
                            if (((DxfPolyline)this.lastEntity).pts.size() == 1) {
                                this.firstPt = pt;
                            }
                            --i;
                        }
                    }
                    this.bulge = 0.0;
                }
            }
            int i = 0;
            while (i < ((DxfPolyline)this.lastEntity).pts.size()) {
                ((DxfPolyline)this.lastEntity).addBulge(new Double(0.0));
                ++i;
            }
            if (!this.addingToBlock) {
                this.entities.add((Extent.Has)this.lastEntity);
            } else {
                this.blk.add((Extent.Has)this.lastEntity);
            }
            this.lastEntity = null;
        } else if (this.lastEntity instanceof DxfInsert) {
            this.gestionaInsert((DxfInsert)this.lastEntity, this.lastEntity.getLayer());
            if (!this.addingToBlock) {
                this.entities.add((Extent.Has)this.lastEntity);
            } else {
                this.blk.add((Extent.Has)this.lastEntity);
            }
            this.lastEntity = null;
        }
        this.xtruX = 0.0;
        this.xtruY = 0.0;
        this.xtruZ = 1.0;
        this.bulge = 0.0;
    }

    @Override
    public void addVertex(DxfGroupVector grp) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        int flags = 0;
        x = grp.getDataAsDouble(10);
        y = grp.getDataAsDouble(20);
        if (grp.hasCode(30)) {
            z = grp.getDataAsDouble(30);
        }
        if (grp.hasCode(70)) {
            flags = grp.getDataAsInt(70);
        }
        if (this.bulge == 0.0) {
            this.bulge = grp.hasCode(42) ? grp.getDataAsDouble(42) : 0.0;
            Point3D point_in = new Point3D(x, y, z);
            Point3D xtru = new Point3D(this.xtruX, this.xtruY, this.xtruZ);
            Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
            if ((flags & 0x80) == 128 && (flags & 0x40) == 0) {
                int[] face = new int[]{grp.getDataAsInt(71), grp.getDataAsInt(72), grp.getDataAsInt(73), grp.getDataAsInt(74)};
                ((DxfPolyline)this.lastEntity).addFace(face);
            } else {
                x = point_out.getX();
                y = point_out.getY();
                z = point_out.getZ();
                Point2D ptaux = this.proj.createPoint(x, y);
                Point3D pt = new Point3D(ptaux.getX(), ptaux.getY(), z);
                ((DxfPolyline)this.lastEntity).add((Point2D)pt);
                if (((DxfPolyline)this.lastEntity).pts.size() == 1) {
                    this.firstPt = pt;
                }
            }
        } else if (this.bulge > 0.0) {
            int cnt;
            double bulge_aux = 0.0;
            bulge_aux = grp.hasCode(42) ? grp.getDataAsDouble(42) : 0.0;
            Point3D point_in = new Point3D(x, y, z);
            Point3D xtru = new Point3D(this.xtruX, this.xtruY, this.xtruZ);
            Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
            x = point_out.getX();
            y = point_out.getY();
            z = point_out.getZ();
            Point2D ptaux = this.proj.createPoint(x, y);
            Point3D pt = new Point3D(ptaux.getX(), ptaux.getY(), z);
            ((DxfPolyline)this.lastEntity).add((Point2D)pt);
            if (((DxfPolyline)this.lastEntity).pts.size() == 1) {
                this.firstPt = pt;
            }
            if (((Point2D)((DxfPolyline)this.lastEntity).pts.get((cnt = ((DxfPolyline)this.lastEntity).pts.size()) - 2)).getX() != ((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 1)).getX() || ((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 2)).getY() != ((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 1)).getY()) {
                Vector arc = DxfPolyline.createArc((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 2), (Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 1), this.bulge);
                ((DxfPolyline)this.lastEntity).pts.remove(cnt - 1);
                int i = 0;
                while (i < arc.size()) {
                    ptaux = this.proj.createPoint(((Point2D)arc.get(i)).getX(), ((Point2D)arc.get(i)).getY());
                    pt = new Point3D(ptaux.getX(), ptaux.getY(), z);
                    ((DxfPolyline)this.lastEntity).add((Point2D)pt);
                    if (((DxfPolyline)this.lastEntity).pts.size() == 1) {
                        this.firstPt = pt;
                    }
                    ++i;
                }
            }
            this.bulge = bulge_aux;
        } else {
            int cnt;
            double bulge_aux = 0.0;
            bulge_aux = grp.hasCode(42) ? grp.getDataAsDouble(42) : 0.0;
            Point3D point_in = new Point3D(x, y, z);
            Point3D xtru = new Point3D(this.xtruX, this.xtruY, this.xtruZ);
            Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
            x = point_out.getX();
            y = point_out.getY();
            z = point_out.getZ();
            Point2D ptaux = this.proj.createPoint(x, y);
            Point3D pt = new Point3D(ptaux.getX(), ptaux.getY(), z);
            ((DxfPolyline)this.lastEntity).add((Point2D)pt);
            if (((DxfPolyline)this.lastEntity).pts.size() == 1) {
                this.firstPt = pt;
            }
            if (((Point2D)((DxfPolyline)this.lastEntity).pts.get((cnt = ((DxfPolyline)this.lastEntity).pts.size()) - 2)).getX() != ((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 1)).getX() || ((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 2)).getY() != ((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 1)).getY()) {
                Vector arc = DxfPolyline.createArc((Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 2), (Point2D)((DxfPolyline)this.lastEntity).pts.get(cnt - 1), this.bulge);
                ((DxfPolyline)this.lastEntity).pts.remove(cnt - 1);
                int i = arc.size() - 1;
                while (i >= 0) {
                    ptaux = this.proj.createPoint(((Point2D)arc.get(i)).getX(), ((Point2D)arc.get(i)).getY());
                    pt = new Point3D(ptaux.getX(), ptaux.getY(), z);
                    ((DxfPolyline)this.lastEntity).add((Point2D)pt);
                    if (((DxfPolyline)this.lastEntity).pts.size() == 1) {
                        this.firstPt = pt;
                    }
                    --i;
                }
            }
            this.bulge = bulge_aux;
        }
    }

    @Override
    public void createLwPolyline(DxfGroupVector grp) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double elev = 0.0;
        DxfGroup g = null;
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
        DxfLwPolyline entity = new DxfLwPolyline(this.proj, layer);
        if (grp.hasCode(38)) {
            entity.setElevation(grp.getDataAsDouble(38));
        }
        if (grp.hasCode(5)) {
            String hexS = grp.getDataAsString(5);
            Integer hexI = Integer.decode("0x" + hexS);
            int hexi = hexI;
            entity.setHandle(hexi);
        } else {
            entity.setHandle(this.entities.size() + 40);
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
        double bulge = 0.0;
        boolean isNewCoord = false;
        int i = 0;
        while (i < grp.size()) {
            bulge = 0.0;
            isNewCoord = false;
            g = (DxfGroup)grp.get(i);
            if (g.getCode() == 10) {
                x = (Double)g.getData();
            } else if (g.getCode() == 20) {
                y = (Double)g.getData();
                Point3D point_in1 = new Point3D(x, y, elev);
                Point3D xtru = new Point3D(extx, exty, extz);
                Point3D point_out1 = DxfCalXtru.CalculateXtru((Point3D)point_in1, (Point3D)xtru);
                x = point_out1.getX();
                y = point_out1.getY();
                elev = point_out1.getZ();
                entity.add(this.proj.createPoint(x, y));
                entity.addBulge(new Double(0.0));
                x = 0.0;
                y = 0.0;
                isNewCoord = true;
            } else if (g.getCode() == 42) {
                entity.getBulges().remove(entity.getBulges().size() - 1);
                entity.getBulges().add(g.getData());
                bulge = (Double)g.getData();
            }
            ++i;
        }
        if (grp.hasCode(62)) {
            entity.dxfColor = grp.getDataAsInt(62);
        }
        if (grp.hasCode(70)) {
            entity.flags = grp.getDataAsInt(70);
        }
        if ((entity.flags & 1) == 1) {
            entity.closed = true;
        }
        if (!this.addingToBlock) {
            this.entities.add((Extent.Has)entity);
        } else {
            this.blk.add((Extent.Has)entity);
        }
    }

    @Override
    public void createLine(DxfGroupVector grp) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double z1 = 0.0;
        double z2 = 0.0;
        Object g = null;
        Point2D pt1 = null;
        Point2D pt2 = null;
        DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        x = grp.getDataAsDouble(10);
        y = grp.getDataAsDouble(20);
        if (grp.hasCode(30)) {
            z1 = grp.getDataAsDouble(30);
        }
        pt1 = this.proj.createPoint(x, y);
        x = grp.getDataAsDouble(11);
        y = grp.getDataAsDouble(21);
        if (grp.hasCode(31)) {
            z2 = grp.getDataAsDouble(31);
        }
        pt2 = this.proj.createPoint(x, y);
        if (grp.hasCode(210)) {
            extx = grp.getDataAsDouble(210);
        }
        if (grp.hasCode(220)) {
            exty = grp.getDataAsInt(220);
        }
        if (grp.hasCode(230)) {
            extz = grp.getDataAsInt(230);
        }
        Point3D point_in1 = new Point3D(pt1.getX(), pt1.getY(), z1);
        Point3D point_in2 = new Point3D(pt2.getX(), pt2.getY(), z2);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out1 = DxfCalXtru.CalculateXtru((Point3D)point_in1, (Point3D)xtru);
        Point3D point_out2 = DxfCalXtru.CalculateXtru((Point3D)point_in2, (Point3D)xtru);
        pt1.setLocation((Point2D)point_out1);
        pt2.setLocation((Point2D)point_out2);
        DxfLine entity = new DxfLine(this.proj, layer, pt1, pt2);
        if (grp.hasCode(5)) {
            String hexS = grp.getDataAsString(5);
            Integer hexI = Integer.decode("0x" + hexS);
            int hexi = hexI;
            entity.setHandle(hexi);
        } else {
            entity.setHandle(this.entities.size() + 40);
        }
        if (grp.hasCode(62)) {
            entity.dxfColor = grp.getDataAsInt(62);
        }
        if (!this.addingToBlock) {
            this.entities.add((Extent.Has)entity);
        } else {
            this.blk.add((Extent.Has)entity);
        }
    }

    @Override
    public void createText(DxfGroupVector grp) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double h = 0.0;
        double rot = 0.0;
        Object g = null;
        Object pt1 = null;
        Object pt2 = null;
        String txt = null;
        DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
        txt = grp.getDataAsString(1);
        DxfText entity = new DxfText(this.proj, layer, txt);
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        x = grp.getDataAsDouble(10);
        y = grp.getDataAsDouble(20);
        if (grp.hasCode(30)) {
            z = grp.getDataAsDouble(30);
            entity.setZ(z);
        } else {
            z = 0.0;
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
        Point3D point_in = new Point3D(x, y, z);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        z = point_out.getZ();
        entity.setPt(this.proj.createPoint(x, y));
        if (grp.hasCode(11)) {
            entity.setTwoPointsFlag(true);
            entity.setPt1(this.proj.createPoint(entity.getPt().getX(), entity.getPt().getY()));
            x = grp.getDataAsDouble(11);
            y = grp.getDataAsDouble(21);
            z = grp.getDataAsDouble(31);
            point_in = new Point3D(x, y, z);
            point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
            x = point_out.getX();
            y = point_out.getY();
            z = point_out.getZ();
            entity.setPt2(this.proj.createPoint(x, y));
        }
        entity.setHeight(grp.getDataAsDouble(40));
        if (grp.hasCode(50)) {
            entity.setRotation(grp.getDataAsDouble(50));
        }
        if (grp.hasCode(62)) {
            entity.dxfColor = grp.getDataAsInt(62);
        }
        if (grp.hasCode(72)) {
            entity.align = grp.getDataAsInt(72);
        }
        if (!this.addingToBlock) {
            this.entities.add((Extent.Has)entity);
        } else {
            this.blk.add((Extent.Has)entity);
        }
    }

    @Override
    public void createMText(DxfGroupVector v) throws Exception {
    }

    @Override
    public void createPoint(DxfGroupVector grp) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        Object g = null;
        Object pt = null;
        DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        DxfPoint entity = new DxfPoint(this.proj, layer);
        if (grp.hasCode(5)) {
            String hexS = grp.getDataAsString(5);
            Integer hexI = Integer.decode("0x" + hexS);
            int hexi = hexI;
            entity.setHandle(hexi);
        } else {
            entity.setHandle(this.entities.size() + 40);
        }
        x = grp.getDataAsDouble(10);
        y = grp.getDataAsDouble(20);
        if (grp.hasCode(30)) {
            z = grp.getDataAsDouble(30);
            entity.setZ(z);
        }
        if (grp.hasCode(62)) {
            entity.dxfColor = grp.getDataAsInt(62);
        }
        if (grp.hasCode(210)) {
            extx = grp.getDataAsDouble(210);
        }
        if (grp.hasCode(220)) {
            exty = grp.getDataAsInt(220);
        }
        if (grp.hasCode(230)) {
            extz = grp.getDataAsInt(230);
        }
        Point3D point_in = new Point3D(x, y, z);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        entity.setPt(this.proj.createPoint(x, y));
        if (!this.addingToBlock) {
            this.entities.add((Extent.Has)entity);
        } else {
            this.blk.add((Extent.Has)entity);
        }
    }

    @Override
    public void createCircle(DxfGroupVector grp) throws Exception {
        double x = 0.0;
        double y = 0.0;
        boolean is3D = false;
        double z = 0.0;
        double r = 0.0;
        Object g = null;
        DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        x = grp.getDataAsDouble(10);
        y = grp.getDataAsDouble(20);
        if (grp.hasCode(30)) {
            z = grp.getDataAsDouble(30);
            is3D = true;
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
        Point3D point_in = new Point3D(x, y, z);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        Point2D center = this.proj.createPoint(x, y);
        Point2D[] pts = new Point2D[360];
        int angulo = 0;
        angulo = 0;
        while (angulo < 360) {
            pts[angulo] = new Point2D.Double(center.getX(), center.getY());
            pts[angulo].setLocation(pts[angulo].getX() + r * Math.sin((double)angulo * Math.PI / 180.0), pts[angulo].getY() + r * Math.cos((double)angulo * Math.PI / 180.0));
            if (pts.length == 1) {
                this.firstPt = pts[angulo];
            }
            ++angulo;
        }
        DxfCircle entity = new DxfCircle(this.proj, layer, pts);
        if (grp.hasCode(5)) {
            String hexS = grp.getDataAsString(5);
            Integer hexI = Integer.decode("0x" + hexS);
            int hexi = hexI;
            entity.setHandle(hexi);
        } else {
            entity.setHandle(this.entities.size() + 40);
        }
        entity.setCenter(new Point2D.Double(x, y));
        entity.setRadius(r);
        if (grp.hasCode(62)) {
            entity.dxfColor = grp.getDataAsInt(62);
        }
        if (is3D) {
            entity.setZ(z);
        }
        if (!this.addingToBlock) {
            this.entities.add((Extent.Has)entity);
        } else {
            this.blk.add((Extent.Has)entity);
        }
    }

    @Override
    public void createEllipse(DxfGroupVector grp) throws Exception {
        double incX = 0.0;
        double incY = 0.0;
        double incZ = 0.0;
        double xc = 0.0;
        double yc = 0.0;
        double zc = 0.0;
        double mMAxisRatio = 0.0;
        Object g = null;
        DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        xc = grp.getDataAsDouble(10);
        yc = grp.getDataAsDouble(20);
        if (grp.hasCode(30)) {
            zc = grp.getDataAsDouble(30);
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
        Point3D point_in = new Point3D(xc, yc, zc);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
        xc = point_out.getX();
        yc = point_out.getY();
        zc = point_out.getZ();
        incX = grp.getDataAsDouble(11);
        incY = grp.getDataAsDouble(21);
        if (grp.hasCode(31)) {
            incZ = grp.getDataAsDouble(31);
        }
        if (grp.hasCode(40)) {
            mMAxisRatio = grp.getDataAsDouble(40);
        }
        Point2D.Double pt2 = new Point2D.Double(xc + incX, yc + incY);
        Point2D.Double pt1 = new Point2D.Double(xc - incX, yc - incY);
        double majorAxisLength = pt1.distance(pt2);
        double minorAxisLength = majorAxisLength * mMAxisRatio;
        DxfEllipse entity = new DxfEllipse(this.proj, layer, pt1, pt2, minorAxisLength);
        if (grp.hasCode(5)) {
            String hexS = grp.getDataAsString(5);
            Integer hexI = Integer.decode("0x" + hexS);
            int hexi = hexI;
            entity.setHandle(hexi);
        } else {
            entity.setHandle(this.entities.size() + 40);
        }
        if (grp.hasCode(62)) {
            entity.dxfColor = grp.getDataAsInt(62);
        }
        if (!this.addingToBlock) {
            this.entities.add((Extent.Has)entity);
        } else {
            this.blk.add((Extent.Has)entity);
        }
    }

    @Override
    public void createArc(DxfGroupVector grp) throws Exception {
        int i;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        boolean is3D = false;
        double r = 0.0;
        double empieza = 0.0;
        double acaba = 0.0;
        Object g = null;
        DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        x = grp.getDataAsDouble(10);
        y = grp.getDataAsDouble(20);
        if (grp.hasCode(30)) {
            z = grp.getDataAsDouble(30);
            is3D = true;
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
        Point3D point_in = new Point3D(x, y, z);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        Point2D center = this.proj.createPoint(x, y);
        int iempieza = (int)empieza;
        int iacaba = (int)acaba;
        double angulo = 0.0;
        Point2D[] pts = null;
        if (empieza <= acaba) {
            pts = new Point2D[iacaba - iempieza + 2];
            angulo = empieza;
            pts[0] = new Point2D.Double(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0));
            i = 1;
            while (i <= iacaba - iempieza + 1) {
                angulo = iempieza + i;
                pts[i] = new Point2D.Double(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0));
                ++i;
            }
            angulo = acaba;
            pts[iacaba - iempieza + 1] = new Point2D.Double(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0));
        } else {
            pts = new Point2D[360 - iempieza + iacaba + 2];
            angulo = empieza;
            pts[0] = new Point2D.Double(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0));
            i = 1;
            while (i <= 360 - iempieza) {
                angulo = iempieza + i;
                pts[i] = new Point2D.Double(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0));
                ++i;
            }
            i = 360 - iempieza + 1;
            while (i <= 360 - iempieza + iacaba) {
                angulo = i - (360 - iempieza);
                pts[i] = new Point2D.Double(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0));
                ++i;
            }
            angulo = acaba;
            pts[360 - iempieza + iacaba + 1] = new Point2D.Double(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0));
        }
        DxfArc entity = new DxfArc(this.proj, layer, pts);
        if (grp.hasCode(5)) {
            String hexS = grp.getDataAsString(5);
            Integer hexI = Integer.decode("0x" + hexS);
            int hexi = hexI;
            entity.setHandle(hexi);
        } else {
            entity.setHandle(this.entities.size() + 40);
        }
        entity.setCentralPoint(pts[pts.length / 2]);
        entity.setInit(pts[0]);
        entity.setEnd(pts[pts.length - 1]);
        entity.setCenter(center);
        entity.setRadius(r);
        entity.setInitAngle(empieza);
        entity.setEndAngle(acaba);
        if (grp.hasCode(62)) {
            entity.dxfColor = grp.getDataAsInt(62);
        }
        if (is3D) {
            entity.setZ(z);
        }
        if (!this.addingToBlock) {
            this.entities.add((Extent.Has)entity);
        } else {
            this.blk.add((Extent.Has)entity);
        }
    }

    @Override
    public void createInsert(DxfGroupVector grp) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        Object g = null;
        Point2D.Double pt = new Point2D.Double(0.0, 0.0);
        Point2D.Double scaleFactor = new Point2D.Double(1.0, 1.0);
        double rotAngle = 0.0;
        String blockName = "";
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
        DxfInsert entity = new DxfInsert(this.proj, layer);
        DxfPoint secondEntity = new DxfPoint(this.proj, layer);
        int attributesFollowFlag = 0;
        if (grp.hasCode(2)) {
            blockName = grp.getDataAsString(2);
            entity.setBlockName(blockName);
        }
        if (grp.hasCode(10)) {
            x = grp.getDataAsDouble(10);
        }
        if (grp.hasCode(20)) {
            y = grp.getDataAsDouble(20);
        }
        if (grp.hasCode(30)) {
            z = grp.getDataAsDouble(30);
            entity.setZ(z);
            secondEntity.setZ(z);
        }
        if (grp.hasCode(41)) {
            ((Point2D)scaleFactor).setLocation(grp.getDataAsDouble(41), ((Point2D)scaleFactor).getY());
            entity.setScaleFactor(scaleFactor);
        } else {
            entity.setScaleFactor(scaleFactor);
        }
        if (grp.hasCode(42)) {
            ((Point2D)scaleFactor).setLocation(((Point2D)scaleFactor).getX(), grp.getDataAsDouble(42));
            entity.setScaleFactor(scaleFactor);
        } else {
            entity.setScaleFactor(scaleFactor);
        }
        grp.hasCode(43);
        if (grp.hasCode(50)) {
            rotAngle = grp.getDataAsDouble(50);
            entity.setRotAngle(rotAngle);
        }
        if (grp.hasCode(62)) {
            entity.dxfColor = grp.getDataAsInt(62);
        }
        if (grp.hasCode(66)) {
            attributesFollowFlag = grp.getDataAsInt(66);
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
        Point3D point_in = new Point3D(x, y, z);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        entity.setBlkList(this.blkList);
        entity.encuentraBloque(blockName);
        entity.setPt(this.proj.createPoint(x, y));
        secondEntity.setPt(this.proj.createPoint(x, y));
        if (entity.getBlockFound() && attributesFollowFlag != 1) {
            this.gestionaInsert(entity, layer);
        }
        if (attributesFollowFlag == 1) {
            this.lastEntity = entity;
        } else if (!this.addingToBlock) {
            this.entities.add((Extent.Has)secondEntity);
        } else if (this.addingToBlock && entity.blockFound) {
            this.blk.add((Extent.Has)entity);
        }
    }

    @Override
    public void createSolid(DxfGroupVector grp) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double z1 = 0.0;
        double z2 = 0.0;
        double z3 = 0.0;
        double z4 = 0.0;
        Object g = null;
        Point2D[] pts = new Point2D[4];
        DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        x = grp.getDataAsDouble(10);
        y = grp.getDataAsDouble(20);
        if (grp.hasCode(30)) {
            z1 = grp.getDataAsDouble(30);
        }
        pts[0] = this.proj.createPoint(x, y);
        x = grp.getDataAsDouble(11);
        y = grp.getDataAsDouble(21);
        if (grp.hasCode(31)) {
            z2 = grp.getDataAsDouble(31);
        }
        pts[1] = this.proj.createPoint(x, y);
        x = grp.getDataAsDouble(12);
        y = grp.getDataAsDouble(22);
        if (grp.hasCode(32)) {
            z3 = grp.getDataAsDouble(32);
        }
        pts[2] = this.proj.createPoint(x, y);
        x = grp.getDataAsDouble(13);
        y = grp.getDataAsDouble(23);
        if (grp.hasCode(33)) {
            z2 = grp.getDataAsDouble(33);
        }
        pts[3] = this.proj.createPoint(x, y);
        if (grp.hasCode(210)) {
            extx = grp.getDataAsDouble(210);
        }
        if (grp.hasCode(220)) {
            exty = grp.getDataAsDouble(220);
        }
        if (grp.hasCode(230)) {
            extz = grp.getDataAsDouble(230);
        }
        Point3D point_in1 = new Point3D(pts[0].getX(), pts[0].getY(), z1);
        Point3D point_in2 = new Point3D(pts[1].getX(), pts[1].getY(), z2);
        Point3D point_in3 = new Point3D(pts[2].getX(), pts[2].getY(), z3);
        Point3D point_in4 = new Point3D(pts[3].getX(), pts[3].getY(), z4);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out1 = DxfCalXtru.CalculateXtru((Point3D)point_in1, (Point3D)xtru);
        Point3D point_out2 = DxfCalXtru.CalculateXtru((Point3D)point_in2, (Point3D)xtru);
        Point3D point_out3 = DxfCalXtru.CalculateXtru((Point3D)point_in3, (Point3D)xtru);
        Point3D point_out4 = DxfCalXtru.CalculateXtru((Point3D)point_in4, (Point3D)xtru);
        pts[0].setLocation((Point2D)point_out1);
        pts[1].setLocation((Point2D)point_out2);
        pts[2].setLocation((Point2D)point_out3);
        pts[3].setLocation((Point2D)point_out4);
        DxfSolid entity = new DxfSolid(this.proj, layer, pts);
        if (grp.hasCode(62)) {
            entity.dxfColor = grp.getDataAsInt(62);
        }
        if (!this.addingToBlock) {
            this.entities.add((Extent.Has)entity);
        } else {
            this.blk.add((Extent.Has)entity);
        }
    }

    @Override
    public void createSpline(DxfGroupVector grp) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double elev = 0.0;
        DxfGroup g = null;
        DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
        DxfLwPolyline entity = new DxfLwPolyline(this.proj, layer);
        int i = 0;
        while (i < grp.size()) {
            g = (DxfGroup)grp.get(i);
            if (g.getCode() == 10) {
                x = (Double)g.getData();
            } else if (g.getCode() == 20) {
                y = (Double)g.getData();
                entity.add(this.proj.createPoint(x, y));
                x = 0.0;
                y = 0.0;
            }
            ++i;
        }
        if (grp.hasCode(62)) {
            entity.dxfColor = grp.getDataAsInt(62);
        }
        if (grp.hasCode(70)) {
            entity.flags = grp.getDataAsInt(70);
        }
        if ((entity.flags & 1) == 1) {
            entity.closed = true;
        }
        if (!this.addingToBlock) {
            this.entities.add((Extent.Has)entity);
        } else {
            this.blk.add((Extent.Has)entity);
        }
    }

    @Override
    public void createBlock(DxfGroupVector grp) throws Exception {
        DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
        this.blk = new DxfBlock(this.proj);
        Point2D.Double basePoint = new Point2D.Double();
        String blockName = "";
        this.addingToBlock = true;
        this.blkList.add(this.iterator, this.blk);
        if (grp.hasCode(1)) {
            blockName = grp.getDataAsString(1);
            this.blk.setBlkName(blockName);
        }
        if (grp.hasCode(2)) {
            blockName = grp.getDataAsString(2);
            this.blk.setBlkName(blockName);
        }
        if (grp.hasCode(3)) {
            blockName = grp.getDataAsString(3);
            this.blk.setBlkName(blockName);
        }
        if (grp.hasCode(10)) {
            basePoint = new Point2D.Double(grp.getDataAsDouble(10), ((Point2D)basePoint).getY());
            this.blk.setBPoint((Point2D)basePoint);
        }
        if (grp.hasCode(20)) {
            basePoint = new Point2D.Double(((Point2D)basePoint).getX(), grp.getDataAsDouble(20));
            this.blk.setBPoint((Point2D)basePoint);
        }
        grp.hasCode(30);
        if (grp.hasCode(70)) {
            this.blk.flags = grp.getDataAsInt(70);
        }
    }

    @Override
    public void endBlk(DxfGroupVector grp) throws Exception {
        this.setAddingToBlock(false);
        ++this.iterator;
    }

    @Override
    public void testBlocks() {
        Vector blkList = this.getBlkList();
        DxfBlock dxfBlock = null;
        DxfEntity dxfEntity = null;
        DxfLine dxfLine = null;
        DxfInsert dxfInsert = null;
        Point2D point1 = new Point2D.Double();
        Point2D point2 = new Point2D.Double();
        int i = 0;
        while (i < blkList.size()) {
            dxfBlock = (DxfBlock)blkList.get(i);
            int aux = dxfBlock.getBlkElements().size();
            int j = 0;
            while (j < aux) {
                dxfEntity = (DxfEntity)((Object)dxfBlock.getBlkElements().get(j));
                if (dxfEntity instanceof DxfLine) {
                    dxfLine = (DxfLine)dxfEntity;
                    point1 = dxfLine.getPts()[0];
                    point2 = dxfLine.getPts()[1];
                } else if (dxfEntity instanceof DxfInsert) {
                    dxfInsert = (DxfInsert)dxfEntity;
                    String nomBlock = dxfInsert.getBlockName();
                    if (!dxfInsert.getBlockFound()) {
                        boolean aux_bool = dxfInsert.encuentraBloque(nomBlock);
                        this.gestionaInsert(dxfInsert, dxfInsert.getDxfLayer());
                        dxfBlock.add((Extent.Has)dxfInsert);
                    }
                }
                ++j;
            }
            ++i;
        }
    }

    public void setProjection(IProjection proj) {
        this.proj = proj;
    }

    public IProjection getProjection() {
        return this.proj;
    }

    public void reProject(ICoordTrans rp) {
        this.entities.reProject(rp);
        this.setProjection(rp.getPDest());
    }

    public DxfEntityList getEntities() {
        return this.entities;
    }

    public DxfTable getLayers() {
        return this.layers;
    }

    public DxfBlock getBlk() {
        return this.blk;
    }

    public void gestionaInsert(DxfInsert entity, DxfLayer layer) {
        DxfEntity dxfEntity = null;
        DxfLine dxfLine = null;
        DxfInsert dxfInsert = null;
        DxfPolyline dxfPolyline = null;
        DxfArc dxfArc = null;
        DxfCircle dxfCircle = null;
        DxfLwPolyline dxfLwPolyline = null;
        DxfPoint dxfPoint = null;
        DxfText dxfText = null;
        DxfSolid dxfSolid = null;
        double bPointX = 0.0;
        double bPointY = 0.0;
        bPointX = entity.block.bPoint.getX();
        bPointY = entity.block.bPoint.getY();
        double sFactorX = entity.getScaleFactor().getX();
        double sFactorY = entity.getScaleFactor().getY();
        double rAngleGra = entity.getRotAngle();
        double rAngleRad = rAngleGra * Math.PI / 180.0;
        int i = 0;
        while (i < entity.block.size()) {
            Point2D[] pointss;
            double laY;
            Point2D[] points;
            dxfEntity = (DxfEntity)entity.block.get(i);
            Point2D point1 = new Point2D.Double();
            Point2D point2 = new Point2D.Double();
            Point2D.Double point11 = new Point2D.Double();
            Point2D.Double point22 = new Point2D.Double();
            Point2D.Double pointAux = null;
            if (dxfEntity instanceof DxfLine) {
                dxfLine = (DxfLine)dxfEntity;
                point1 = dxfLine.getPts()[0];
                pointAux = new Point2D.Double(point1.getX() - bPointX, point1.getY() - bPointY);
                double laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                double laY2 = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                ((Point2D)point11).setLocation(laX, laY2);
                point2 = dxfLine.getPts()[1];
                pointAux = new Point2D.Double(point2.getX() - bPointX, point2.getY() - bPointY);
                laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                laY2 = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                ((Point2D)point22).setLocation(laX, laY2);
                DxfLine dxfLinee = new DxfLine(this.proj, layer, point11, point22);
                if (!this.addingToBlock) {
                    this.entities.add((Extent.Has)dxfLinee);
                }
            } else if (dxfEntity instanceof DxfInsert) {
                dxfInsert = (DxfInsert)dxfEntity;
                point1 = dxfInsert.pt;
                pointAux = new Point2D.Double(point1.getX() - bPointX, point1.getY() - bPointY);
                double laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                double laY3 = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                ((Point2D)point11).setLocation(laX, laY3);
                DxfInsert dxfInsertt = new DxfInsert(this.proj, layer);
                dxfInsertt.pt = point11;
                dxfInsertt.blkList = dxfInsert.blkList;
                dxfInsertt.block = dxfInsert.block;
                dxfInsertt.blockName = dxfInsert.blockName;
                dxfInsertt.rotAngle = dxfInsert.rotAngle;
                dxfInsertt.layer = dxfInsert.layer;
                dxfInsertt.proj = dxfInsert.proj;
                Point2D.Double newScale = new Point2D.Double(dxfInsert.getScaleFactor().getX() * sFactorX, dxfInsert.getScaleFactor().getY() * sFactorY);
                dxfInsertt.setScaleFactor(newScale);
                this.gestionaInsert(dxfInsertt, layer);
            } else if (dxfEntity instanceof DxfPolyline) {
                dxfPolyline = (DxfPolyline)dxfEntity;
                DxfPolyline dxfPolylinee = new DxfPolyline(this.proj, layer);
                if (dxfPolyline.closed) {
                    dxfPolylinee.closed = true;
                }
                points = new Point2D[dxfPolyline.pts.size()];
                Point2D[] pointss2 = new Point2D[dxfPolyline.pts.size()];
                int j = 0;
                while (j < dxfPolyline.pts.size()) {
                    points[j] = (Point2D)dxfPolyline.pts.get(j);
                    pointss2[j] = new Point2D.Double();
                    pointAux = new Point2D.Double(points[j].getX() - bPointX, points[j].getY() - bPointY);
                    double laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                    laY = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                    pointss2[j].setLocation(laX, laY);
                    dxfPolylinee.add(pointss2[j]);
                    dxfPolylinee.addBulge((Double)dxfPolyline.getBulges().get(j));
                    ++j;
                }
                if (!this.addingToBlock) {
                    this.entities.add((Extent.Has)dxfPolylinee);
                }
            } else if (dxfEntity instanceof DxfArc) {
                dxfArc = (DxfArc)dxfEntity;
                Point2D[] points2 = new Point2D[dxfArc.pts.length];
                pointss = new Point2D[dxfArc.pts.length];
                int j = 0;
                while (j < dxfArc.pts.length) {
                    points2[j] = dxfArc.pts[j];
                    pointss[j] = new Point2D.Double();
                    pointAux = new Point2D.Double(points2[j].getX() - bPointX, points2[j].getY() - bPointY);
                    double laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                    double laY4 = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                    pointss[j].setLocation(laX, laY4);
                    ++j;
                }
                DxfArc dxfArcc = new DxfArc(this.proj, layer, pointss);
                pointAux = new Point2D.Double(dxfArc.getCentralPoint().getX() - bPointX, dxfArc.getCentralPoint().getY() - bPointY);
                double laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                double laY5 = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                ((Point2D)pointAux).setLocation(laX, laY5);
                dxfArcc.setCentralPoint(pointAux);
                pointAux = new Point2D.Double(dxfArc.getInit().getX() - bPointX, dxfArc.getInit().getY() - bPointY);
                laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                laY5 = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                ((Point2D)pointAux).setLocation(laX, laY5);
                dxfArcc.setInit(pointAux);
                pointAux = new Point2D.Double(dxfArc.getEnd().getX() - bPointX, dxfArc.getEnd().getY() - bPointY);
                laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                laY5 = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                ((Point2D)pointAux).setLocation(laX, laY5);
                dxfArcc.setEnd(pointAux);
                pointAux = new Point2D.Double(dxfArc.getCenter().getX() - bPointX, dxfArc.getCenter().getY() - bPointY);
                laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                laY5 = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                ((Point2D)pointAux).setLocation(laX, laY5);
                dxfArcc.setCenter(pointAux);
                dxfArcc.setRadius(dxfArc.getRadius() * sFactorX);
                dxfArcc.setInitAngle(dxfArc.getInitAngle());
                dxfArcc.setEndAngle(dxfArc.getEndAngle());
                if (!this.addingToBlock) {
                    this.entities.add((Extent.Has)dxfArcc);
                }
            } else if (dxfEntity instanceof DxfCircle) {
                dxfCircle = (DxfCircle)dxfEntity;
                Point2D[] points3 = new Point2D[dxfCircle.pts.length];
                pointss = new Point2D[dxfCircle.pts.length];
                int j = 0;
                while (j < dxfCircle.pts.length) {
                    points3[j] = dxfCircle.pts[j];
                    pointss[j] = new Point2D.Double();
                    pointAux = new Point2D.Double(points3[j].getX() - bPointX, points3[j].getY() - bPointY);
                    double laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                    double laY6 = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                    pointss[j].setLocation(laX, laY6);
                    ++j;
                }
                DxfCircle dxfCirclee = new DxfCircle(this.proj, layer, pointss);
                pointAux = new Point2D.Double(dxfCircle.getCenter().getX() - bPointX, dxfCircle.getCenter().getY() - bPointY);
                double laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                double laY7 = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                ((Point2D)pointAux).setLocation(laX, laY7);
                dxfCirclee.setCenter(pointAux);
                dxfCirclee.setRadius(dxfCircle.getRadius() * sFactorX);
                if (!this.addingToBlock) {
                    this.entities.add((Extent.Has)dxfCirclee);
                }
            } else if (dxfEntity instanceof DxfLwPolyline) {
                dxfLwPolyline = (DxfLwPolyline)dxfEntity;
                DxfLwPolyline dxfLwPolylinee = new DxfLwPolyline(this.proj, layer);
                points = new Point2D[dxfLwPolyline.pts.size()];
                Point2D[] pointss3 = new Point2D[dxfLwPolyline.pts.size()];
                int j = 0;
                while (j < dxfLwPolyline.pts.size()) {
                    points[j] = (Point2D)dxfLwPolyline.pts.get(j);
                    pointss3[j] = new Point2D.Double();
                    pointAux = new Point2D.Double(points[j].getX() - bPointX, points[j].getY() - bPointY);
                    double laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                    laY = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                    pointss3[j].setLocation(laX, laY);
                    dxfLwPolylinee.add(pointss3[j]);
                    dxfLwPolylinee.addBulge((Double)dxfPolyline.getBulges().get(j));
                    ++j;
                }
                if (!this.addingToBlock) {
                    this.entities.add((Extent.Has)dxfLwPolylinee);
                }
            } else if (dxfEntity instanceof DxfPoint) {
                dxfPoint = (DxfPoint)dxfEntity;
                point1 = dxfPoint.getPt();
                pointAux = new Point2D.Double(point1.getX() - bPointX, point1.getY() - bPointY);
                double laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                double laY8 = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                ((Point2D)point11).setLocation(laX, laY8);
                DxfPoint dxfPointt = new DxfPoint(this.proj, layer);
                dxfPointt.setPt(point11);
                if (!this.addingToBlock) {
                    this.entities.add((Extent.Has)dxfPointt);
                }
            } else if (dxfEntity instanceof DxfText) {
                dxfText = (DxfText)dxfEntity;
                if (dxfText.getTwoPointsFlag()) {
                    point1 = dxfText.pts[0];
                    pointAux = new Point2D.Double(point1.getX() - bPointX, point1.getY() - bPointY);
                    double laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                    double laY9 = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                    ((Point2D)point11).setLocation(laX, laY9);
                    point2 = dxfText.pts[1];
                    pointAux = new Point2D.Double(point2.getX() - bPointX, point2.getY() - bPointY);
                    laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                    laY9 = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                    ((Point2D)point22).setLocation(laX, laY9);
                    DxfText dxfTextt = new DxfText(this.proj, layer, dxfText.getText());
                    dxfTextt.pts[0] = point11;
                    dxfTextt.pts[1] = point22;
                    if (!this.addingToBlock) {
                        this.entities.add((Extent.Has)dxfTextt);
                    }
                } else {
                    point1 = dxfText.getPt();
                    pointAux = new Point2D.Double(point1.getX() - bPointX, point1.getY() - bPointY);
                    double laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                    double laY10 = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                    ((Point2D)point11).setLocation(laX, laY10);
                    DxfText dxfTextt = new DxfText(this.proj, layer, dxfText.getText());
                    dxfTextt.setPt(point11);
                    if (!this.addingToBlock) {
                        this.entities.add((Extent.Has)dxfTextt);
                    }
                }
            } else if (dxfEntity instanceof DxfSolid) {
                dxfSolid = (DxfSolid)dxfEntity;
                Point2D[] points4 = new Point2D[dxfSolid.pts.length];
                pointss = new Point2D[dxfSolid.pts.length];
                int j = 0;
                while (j < dxfSolid.pts.length) {
                    points4[j] = dxfSolid.pts[j];
                    pointss[j] = new Point2D.Double();
                    pointAux = new Point2D.Double(points4[j].getX() - bPointX, points4[j].getY() - bPointY);
                    double laX = entity.pt.getX() + (((Point2D)pointAux).getX() * sFactorX * Math.cos(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * -1.0 * Math.sin(rAngleRad));
                    double laY11 = entity.pt.getY() + (((Point2D)pointAux).getX() * sFactorX * Math.sin(rAngleRad) + ((Point2D)pointAux).getY() * sFactorY * Math.cos(rAngleRad));
                    pointss[j].setLocation(laX, laY11);
                    ++j;
                }
                DxfSolid dxfSolidd = new DxfSolid(this.proj, layer, pointss);
                Point2D aux = dxfSolidd.pts[2];
                dxfSolidd.pts[2] = dxfSolidd.pts[3];
                dxfSolidd.pts[3] = aux;
                if (!this.addingToBlock) {
                    this.entities.add((Extent.Has)dxfSolidd);
                }
            } else {
                System.out.println("gestionaInserts: Encontrado elemento desconocido");
            }
            ++i;
        }
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
    }

    @Override
    public void createAttrib(DxfGroupVector grp) throws Exception {
        String string;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double h = 0.0;
        double rot = 0.0;
        Object g = null;
        Object pt = null;
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
        DxfLayer layer = (DxfLayer)this.layers.getByName(grp.getDataAsString(8));
        DxfAttrib entity = new DxfAttrib(this.proj, layer);
        if (grp.hasCode(1)) {
            String strAux1 = grp.getDataAsString(1);
            defaultValue = strAux1 = DxfConvTexts.ConvertText(strAux1);
            att[1] = DxfConvTexts.ConvertText(defaultValue);
            defValDefined = true;
        }
        if (grp.hasCode(2)) {
            String strAux2 = grp.getDataAsString(2);
            tagString = strAux2 = DxfConvTexts.ConvertText(strAux2);
            att[0] = DxfConvTexts.ConvertText(tagString);
            tagDefined = true;
        }
        if (grp.hasCode(7)) {
            textStyleName = grp.getDataAsString(7);
            textStyleName = DxfConvTexts.ConvertText(textStyleName);
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
        Point3D point_in = new Point3D(x, y, z);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        z = point_out.getZ();
        entity.setPt(this.proj.createPoint(x, y));
        if (grp.hasCode(40)) {
            Double heightD = new Double(grp.getDataAsDouble(40));
            string = heightD.toString();
        }
        if (grp.hasCode(50)) {
            Double rotD = new Double(grp.getDataAsDouble(50));
            string = rotD.toString();
        }
        if (grp.hasCode(62)) {
            entity.dxfColor = grp.getDataAsInt(62);
        }
        if (grp.hasCode(70)) {
            attributeFlags = grp.getDataAsInt(70);
        }
        if (attributeFlags == 8) {
            if (!this.addingToBlock) {
                this.entities.add((Extent.Has)entity);
            } else {
                this.blk.add((Extent.Has)entity);
            }
        }
    }

    @Override
    public Vector getAttributes() {
        return this.attributes;
    }

    @Override
    public void depureAttributes() {
    }

    @Override
    public boolean isDxf3DFile() {
        return false;
    }
}

