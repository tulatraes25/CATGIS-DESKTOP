/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.cit.gvsig.fmap.core.FPoint2D
 *  com.iver.cit.gvsig.fmap.core.FPoint3D
 *  com.iver.cit.gvsig.fmap.core.IGeometry
 *  com.iver.cit.gvsig.fmap.edition.EditionException
 *  com.iver.cit.gvsig.fmap.edition.UtilFunctions
 *  org.apache.log4j.Logger
 *  org.cresques.geo.Point3D
 */
package org.saig.core.dao.datasource.filedatasource.dxf;

import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FPoint3D;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.edition.EditionException;
import com.iver.cit.gvsig.fmap.edition.UtilFunctions;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import es.kosmo.core.crs.CrsRepositoryManager;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.cresques.geo.Point3D;
import org.cresques.io.DxfFile;
import org.cresques.io.DxfGroup;
import org.cresques.io.DxfGroupVector;
import org.cresques.px.dxf.DxfEntityMaker;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.ICrs;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPathIterator;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeArc2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeArc3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeCircle2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeCircle3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeEllipse2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint3D;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.FeatureIterator;

public class DXFWriter {
    public static final Logger LOGGER = Logger.getLogger(DXFWriter.class);
    public static final String DEFAULT_LAYER_NAME = "Default";
    public static final Object DEFAULT_COLOR = new Integer(0);
    public static final Object DEFAULT_THICKNESS = null;
    public static final Object DEFAULT_ELEVATION = null;
    public static final Object DEFAULT_ROTATION_TEXT = null;
    public static final Object DEFAULT_HEIGHT_TEXT = null;
    public static final Object DEFAULT_TEXT = null;
    public static final String KOSMO_DESKTOP_APPID = "KOSMO_DESKTOP";
    protected File file;
    protected DxfFile.EntityFactory entityMaker;
    protected int handle = 40;
    protected int k = 0;
    protected boolean dxf3DFile = false;
    protected Set<String> protectedFieldNames;
    protected Map<String, DxfGroup> attrNameToDxfGroupMap;
    public static final int XDATA_INT_VALUE_CODE = 1070;
    public static final int XDATA_LONG_VALUE_CODE = 1071;
    public static final int XDATA_REAL_VALUE_CODE = 1040;
    public static final int XDATA_STRING_VALUE_CODE = 1000;
    public static final int XDATA_CONTROL_VALUE_CODE = 1002;

    public void setFile(File f) {
        this.file = f;
    }

    public void preProcess(FeatureSchema featureSchema) throws EditionException {
        ICrs proj = null;
        try {
            proj = CrsRepositoryManager.getInstance().getCRS("EPSG:4326");
        }
        catch (CrsException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        this.entityMaker = new DxfEntityMaker(proj);
        this.protectedFieldNames = new HashSet<String>();
        this.protectedFieldNames.add("Color");
        this.protectedFieldNames.add("Elevation");
        this.protectedFieldNames.add("HeightText");
        this.protectedFieldNames.add("Layer");
        this.protectedFieldNames.add("RotationText");
        this.protectedFieldNames.add("Text");
        this.protectedFieldNames.add("Thickness");
        this.attrNameToDxfGroupMap = new HashMap<String, DxfGroup>();
        if (featureSchema.getPrimaryKeyName() != null) {
            this.protectedFieldNames.add(featureSchema.getPrimaryKeyName());
        }
        for (Attribute attr : featureSchema.getAttributes().values()) {
            if (attr.getType().equals(AttributeType.GEOMETRY)) {
                this.protectedFieldNames.add(attr.getName());
                continue;
            }
            this.attrNameToDxfGroupMap.put(attr.getName(), this.createAppNameDxfGroup(attr.getName()));
        }
    }

    public void process(Feature feat) throws EditionException {
        try {
            IShapeGeometry geom = ShapeGeometryConverter.jts_to_igeometry(feat.getGeometry());
            if (geom == null) {
                LOGGER.warn((Object)("La geometria es nula:" + feat.getGeometry()));
                return;
            }
            boolean isDXFLayer = feat.getSchema().hasAttribute("Entity");
            if (isDXFLayer && "text".equalsIgnoreCase((String)feat.getAttribute("Entity"))) {
                this.createText(this.handle, this.k, geom, feat);
                ++this.k;
            } else if (geom.getGeometryType() == 1) {
                this.createPoint2D(this.handle, this.k, geom, feat);
                ++this.k;
            } else if (geom.getGeometryType() == 513) {
                this.dxf3DFile = true;
                this.createPoint3D(this.handle, this.k, geom, feat);
                ++this.k;
            } else if (geom.getGeometryType() == 2) {
                this.createLwPolyline2D(this.handle, this.k, geom, false, feat);
                ++this.k;
            } else if (geom.getGeometryType() == 514) {
                this.dxf3DFile = true;
                this.k = this.createPolyline3D(this.handle, this.k, (IShapeGeometry3D)geom, feat);
            } else if (geom.getGeometryType() == 4) {
                this.createLwPolyline2D(this.handle, this.k, geom, true, feat);
                ++this.k;
            } else if (geom.getGeometryType() == 516) {
                this.dxf3DFile = true;
                this.k = this.createPolyline3D(this.handle, this.k, (IShapeGeometry3D)geom, feat);
            } else if (geom.getGeometryType() == 64) {
                this.createCircle2D(this.handle, this.k, (ShapeCircle2D)geom.getShp(), feat);
                ++this.k;
            } else if (geom.getGeometryType() == 576) {
                this.createCircle3D(this.handle, this.k, (ShapeCircle3D)geom.getShp(), feat);
                ++this.k;
            } else if (geom.getGeometryType() == 128) {
                ShapeArc2D arc = (ShapeArc2D)geom.getShp();
                this.createArc2D(arc, feat);
                ++this.k;
            } else if (geom.getGeometryType() == 640) {
                ShapeArc3D arc = (ShapeArc3D)geom.getShp();
                this.createArc3D(arc, feat);
                ++this.k;
            } else if (geom.getGeometryType() == 256) {
                ShapeEllipse2D ellipse = (ShapeEllipse2D)geom.getShp();
                this.createEllipse2D(ellipse, feat);
                ++this.k;
            } else {
                LOGGER.warn((Object)"IGeometry type not supported yet");
                ++this.k;
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            throw new EditionException((Throwable)e);
        }
    }

    private void createArc2D(ShapeArc2D fArc, Feature feature) throws Exception {
        String layerName = this.getLayerName(feature);
        Object selectedColor = this.getColor(feature);
        Object thicknessObj = this.getThickness(feature);
        Point2D[] pts = new Point2D[]{fArc.getInit(), fArc.getMid(), fArc.getEnd()};
        Point2D center = fArc.getCenter();
        if (center == null) {
            center = pts[1];
        }
        double radius = center.distance(pts[0]);
        double initAngle = UtilFunctions.getAngle((Point2D)center, (Point2D)pts[0]);
        if (Double.isNaN(initAngle = Math.toDegrees(initAngle))) {
            initAngle = 0.0;
        }
        double midAngle = UtilFunctions.getAngle((Point2D)center, (Point2D)pts[1]);
        if (Double.isNaN(midAngle = Math.toDegrees(midAngle))) {
            midAngle = 0.0;
        }
        double endAngle = UtilFunctions.getAngle((Point2D)center, (Point2D)pts[2]);
        if (Double.isNaN(endAngle = Math.toDegrees(endAngle))) {
            endAngle = 0.0;
        }
        DxfGroup arcLayer = new DxfGroup(8, layerName);
        DxfGroup ax = new DxfGroup();
        DxfGroup ay = new DxfGroup();
        DxfGroup ac = new DxfGroup();
        DxfGroup ai = new DxfGroup();
        DxfGroup ae = new DxfGroup();
        ax.setCode(10);
        ax.setData(new Double(center.getX()));
        ay.setCode(20);
        ay.setData(new Double(center.getY()));
        ac.setCode(40);
        ac.setData(new Double(radius));
        ai.setCode(50);
        ai.setData(new Double(initAngle));
        ae.setCode(51);
        ae.setData(new Double(endAngle));
        DxfGroupVector av = new DxfGroupVector();
        DxfGroup handleGroup = new DxfGroup();
        handleGroup.setCode(5);
        handleGroup.setData(new Integer(this.handle + this.k).toString());
        av.add(arcLayer);
        av.add(handleGroup);
        av.add(ax);
        av.add(ay);
        av.add(ac);
        av.add(ai);
        av.add(ae);
        DxfGroup color = new DxfGroup();
        color.setCode(62);
        color.setData(selectedColor);
        av.add(color);
        if (thicknessObj != null) {
            DxfGroup thickness = new DxfGroup();
            thickness.setCode(39);
            thickness.setData(thicknessObj);
            av.add(thickness);
        }
        this.entityMaker.createArc(av);
    }

    private void createArc3D(ShapeArc3D fArc, Feature feature) throws Exception {
        String layerName = this.getLayerName(feature);
        Object selectedColor = this.getColor(feature);
        Object thicknessObj = this.getThickness(feature);
        Point2D[] pts = new Point2D[]{fArc.getInit(), fArc.getMid(), fArc.getEnd()};
        Point2D center = fArc.getCenter();
        double radius = center.distance(pts[0]);
        double initAngle = UtilFunctions.getAngle((Point2D)center, (Point2D)pts[0]);
        initAngle = Math.toDegrees(initAngle);
        double midAngle = UtilFunctions.getAngle((Point2D)center, (Point2D)pts[1]);
        midAngle = Math.toDegrees(midAngle);
        double endAngle = UtilFunctions.getAngle((Point2D)center, (Point2D)pts[2]);
        endAngle = Math.toDegrees(endAngle);
        DxfGroup arcLayer = new DxfGroup(8, layerName);
        DxfGroup ax = new DxfGroup();
        DxfGroup ay = new DxfGroup();
        DxfGroup az = new DxfGroup();
        DxfGroup ac = new DxfGroup();
        DxfGroup ai = new DxfGroup();
        DxfGroup ae = new DxfGroup();
        ax.setCode(10);
        ax.setData(new Double(center.getX()));
        ay.setCode(20);
        ay.setData(new Double(center.getY()));
        az.setCode(30);
        az.setData(new Double(fArc.getZs()[0]));
        ac.setCode(40);
        ac.setData(new Double(radius));
        ai.setCode(50);
        ai.setData(new Double(initAngle));
        ae.setCode(51);
        ae.setData(new Double(endAngle));
        DxfGroupVector av = new DxfGroupVector();
        DxfGroup handleGroup = new DxfGroup();
        handleGroup.setCode(5);
        handleGroup.setData(new Integer(this.handle + this.k).toString());
        av.add(handleGroup);
        av.add(arcLayer);
        av.add(ax);
        av.add(ay);
        av.add(az);
        av.add(ac);
        av.add(ai);
        av.add(ae);
        DxfGroup color = new DxfGroup();
        color.setCode(62);
        color.setData(selectedColor);
        av.add(color);
        if (thicknessObj != null) {
            DxfGroup thickness = new DxfGroup();
            thickness.setCode(39);
            thickness.setData(thicknessObj);
            av.add(thickness);
        }
        this.entityMaker.createArc(av);
    }

    private void createCircle2D(int handle, int k2, ShapeCircle2D geom, Feature feature) throws Exception {
        String layerName = this.getLayerName(feature);
        Object selectedColor = this.getColor(feature);
        Object thicknessObj = this.getThickness(feature);
        DxfGroupVector polv = new DxfGroupVector();
        DxfGroup polylineLayer = new DxfGroup(8, layerName);
        polv.add(polylineLayer);
        DxfGroup handleGroup = new DxfGroup();
        handleGroup.setCode(5);
        handleGroup.setData(new Integer(handle + this.k).toString());
        polv.add(handleGroup);
        DxfGroup circleFlag = new DxfGroup();
        circleFlag.setCode(100);
        polv.add(circleFlag);
        DxfGroup xvertex = new DxfGroup();
        xvertex.setCode(10);
        xvertex.setData(new Double(geom.getCenter().getX()));
        DxfGroup yvertex = new DxfGroup();
        yvertex.setCode(20);
        yvertex.setData(new Double(geom.getCenter().getY()));
        DxfGroup zvertex = new DxfGroup();
        zvertex.setCode(30);
        zvertex.setData(new Double(0.0));
        DxfGroup radius = new DxfGroup();
        radius.setCode(40);
        radius.setData(new Double(geom.getRadio()));
        polv.add(xvertex);
        polv.add(yvertex);
        polv.add(zvertex);
        polv.add(radius);
        DxfGroup color = new DxfGroup();
        color.setCode(62);
        color.setData(selectedColor);
        polv.add(color);
        if (thicknessObj != null) {
            DxfGroup thickness = new DxfGroup();
            thickness.setCode(39);
            thickness.setData(thicknessObj);
            polv.add(thickness);
        }
        this.entityMaker.createCircle(polv);
    }

    private void createCircle3D(int handle, int k2, ShapeCircle3D geom, Feature feature) throws Exception {
        String layerName = this.getLayerName(feature);
        Object selectedColor = this.getColor(feature);
        Object thicknessObj = this.getThickness(feature);
        DxfGroupVector polv = new DxfGroupVector();
        DxfGroup polylineLayer = new DxfGroup(8, layerName);
        polv.add(polylineLayer);
        DxfGroup handleGroup = new DxfGroup();
        handleGroup.setCode(5);
        handleGroup.setData(new Integer(handle + this.k).toString());
        polv.add(handleGroup);
        DxfGroup circleFlag = new DxfGroup();
        circleFlag.setCode(100);
        polv.add(circleFlag);
        DxfGroup xvertex = new DxfGroup();
        xvertex.setCode(10);
        xvertex.setData(new Double(geom.getCenter().getX()));
        DxfGroup yvertex = new DxfGroup();
        yvertex.setCode(20);
        yvertex.setData(new Double(geom.getCenter().getY()));
        DxfGroup zvertex = new DxfGroup();
        zvertex.setCode(30);
        zvertex.setData(new Double(geom.getZs()[0]));
        DxfGroup radius = new DxfGroup();
        radius.setCode(40);
        radius.setData(new Double(geom.getRadio()));
        polv.add(xvertex);
        polv.add(yvertex);
        polv.add(zvertex);
        polv.add(radius);
        DxfGroup color = new DxfGroup();
        color.setCode(62);
        color.setData(selectedColor);
        polv.add(color);
        if (thicknessObj != null) {
            DxfGroup thickness = new DxfGroup();
            thickness.setCode(39);
            thickness.setData(thicknessObj);
            polv.add(thickness);
        }
        this.entityMaker.createCircle(polv);
    }

    private void createEllipse2D(ShapeEllipse2D fElip, Feature feature) throws Exception {
        String layerName = this.getLayerName(feature);
        Object selectedColor = this.getColor(feature);
        Object thicknessObj = this.getThickness(feature);
        Point2D.Double center = new Point2D.Double((fElip.getInit().getX() + fElip.getEnd().getX()) / 2.0, (fElip.getInit().getY() + fElip.getEnd().getY()) / 2.0);
        double mAxisL = fElip.getDist() * 2.0;
        double maAxisL = fElip.getInit().distance(fElip.getEnd());
        Point2D endPointOfMajorAxis = fElip.getEnd();
        double azimut = Math.atan2(endPointOfMajorAxis.getX() - ((Point2D)center).getX(), endPointOfMajorAxis.getY() - ((Point2D)center).getY());
        double azimut2 = azimut + 1.5707963267948966;
        if (azimut2 >= Math.PI * 2) {
            azimut2 -= Math.PI * 2;
        }
        Point2D endPointOfMinorAxis = new Point2D.Double(((Point2D)center).getX() + fElip.getDist() * Math.sin(azimut2), ((Point2D)center).getY() + fElip.getDist() * Math.cos(azimut2));
        if (mAxisL >= maAxisL) {
            double aux = mAxisL;
            mAxisL = maAxisL;
            maAxisL = aux;
            Point2D.Double pAux = endPointOfMinorAxis;
            endPointOfMinorAxis = endPointOfMajorAxis;
            endPointOfMajorAxis = pAux;
        }
        double mToMAR = mAxisL / maAxisL;
        DxfGroup arcLayer = new DxfGroup(8, layerName);
        DxfGroup x = new DxfGroup();
        DxfGroup y = new DxfGroup();
        DxfGroup xc = new DxfGroup();
        DxfGroup yc = new DxfGroup();
        DxfGroup minToMaj = new DxfGroup();
        x.setCode(10);
        x.setData(new Double(((Point2D)center).getX()));
        y.setCode(20);
        y.setData(new Double(((Point2D)center).getY()));
        xc.setCode(11);
        xc.setData(new Double(endPointOfMajorAxis.getX() - ((Point2D)center).getX()));
        yc.setCode(21);
        yc.setData(new Double(endPointOfMajorAxis.getY() - ((Point2D)center).getY()));
        minToMaj.setCode(40);
        minToMaj.setData(new Double(mToMAR));
        DxfGroup handleGroup = new DxfGroup();
        handleGroup.setCode(5);
        handleGroup.setData(new Integer(this.handle + this.k).toString());
        DxfGroupVector av = new DxfGroupVector();
        av.add(handleGroup);
        av.add(arcLayer);
        av.add(x);
        av.add(y);
        av.add(xc);
        av.add(yc);
        av.add(minToMaj);
        DxfGroup color = new DxfGroup();
        color.setCode(62);
        color.setData(selectedColor);
        av.add(color);
        if (thicknessObj != null) {
            DxfGroup thickness = new DxfGroup();
            thickness.setCode(39);
            thickness.setData(thicknessObj);
            av.add(thickness);
        }
        this.entityMaker.createEllipse(av);
    }

    public void postProcess() throws EditionException {
        DxfFile dxfFile = new DxfFile(null, this.file.getAbsolutePath(), this.entityMaker);
        dxfFile.setCadFlag(true);
        if (this.dxf3DFile) {
            dxfFile.setDxf3DFlag(true);
        }
        try {
            dxfFile.save(this.file.getAbsolutePath());
        }
        catch (IOException e) {
            LOGGER.error((Object)"", (Throwable)e);
            throw new EditionException((Throwable)e);
        }
    }

    public String getName() {
        return "DXF Writer";
    }

    public void write(FeatureCollection fc, File file) throws Exception {
        this.write(fc, file, Long.MAX_VALUE);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void write(FeatureCollection fc, File file, long limit) throws Exception {
        this.file = file;
        this.preProcess(fc.getFeatureSchema());
        FeatureIterator it = null;
        try {
            long count = 0L;
            it = fc.iterator();
            while (it.hasNext() && count < limit) {
                Feature feat = it.next();
                this.process(feat);
                ++count;
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        this.postProcess();
    }

    @Deprecated
    private int createPolygon3D(int handle, int k, IShapeGeometry3D geom, Feature feature) throws Exception {
        String layerName = this.getLayerName(feature);
        Object selectedColor = this.getColor(feature);
        Object thicknessObj = this.getThickness(feature);
        DxfGroupVector polv = new DxfGroupVector();
        DxfGroup polylineLayer = new DxfGroup(8, layerName);
        polv.add(polylineLayer);
        DxfGroup handleGroup = new DxfGroup();
        handleGroup.setCode(5);
        handleGroup.setData(new Integer(handle + k).toString());
        polv.add(handleGroup);
        Vector<FPoint2D> vpoints = new Vector<FPoint2D>();
        SAIGGeneralPathIterator theIterator = geom.getGeneralPathXIterator();
        double[] theData = new double[6];
        double[] velev = geom.getZs();
        while (!theIterator.isDone()) {
            int theType = theIterator.currentSegment(theData);
            switch (theType) {
                case 0: {
                    vpoints.add(new FPoint2D(theData[0], theData[1]));
                    break;
                }
                case 1: {
                    vpoints.add(new FPoint2D(theData[0], theData[1]));
                }
            }
            theIterator.next();
        }
        if (this.constantElevation(velev)) {
            DxfGroup polylineFlag = new DxfGroup();
            polylineFlag.setCode(70);
            polylineFlag.setData(new Integer(1));
            polv.add(polylineFlag);
            DxfGroup elevation = new DxfGroup();
            elevation.setCode(38);
            elevation.setData(new Double(velev[0]));
            polv.add(elevation);
            int j = 0;
            while (j < vpoints.size()) {
                DxfGroup xvertex = new DxfGroup();
                xvertex.setCode(10);
                xvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getX()));
                DxfGroup yvertex = new DxfGroup();
                yvertex.setCode(20);
                yvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getY()));
                polv.add(xvertex);
                polv.add(yvertex);
                ++j;
            }
            DxfGroup color = new DxfGroup();
            color.setCode(62);
            color.setData(selectedColor);
            polv.add(color);
            if (thicknessObj != null) {
                DxfGroup thickness = new DxfGroup();
                thickness.setCode(39);
                thickness.setData(thicknessObj);
                polv.add(thickness);
            }
            this.entityMaker.createLwPolyline(polv);
            ++k;
        } else {
            DxfGroup polylineFlag = new DxfGroup();
            polylineFlag.setCode(70);
            polylineFlag.setData(new Integer(9));
            polv.add(polylineFlag);
            DxfGroup xgroup = new DxfGroup();
            xgroup.setCode(10);
            xgroup.setData(new Double(0.0));
            polv.add(xgroup);
            DxfGroup ygroup = new DxfGroup();
            ygroup.setCode(20);
            ygroup.setData(new Double(0.0));
            polv.add(ygroup);
            DxfGroup elevation = new DxfGroup();
            elevation.setCode(30);
            elevation.setData(new Double(0.0));
            polv.add(elevation);
            DxfGroup subclassMarker = new DxfGroup(100, "AcDb3dPolyline");
            polv.add(subclassMarker);
            DxfGroup color = new DxfGroup();
            color.setCode(62);
            color.setData(selectedColor);
            polv.add(color);
            if (thicknessObj != null) {
                DxfGroup thickness = new DxfGroup();
                thickness.setCode(39);
                thickness.setData(thicknessObj);
                polv.add(thickness);
            }
            this.entityMaker.createPolyline(polv);
            ++k;
            int j = 0;
            while (j < vpoints.size()) {
                DxfGroupVector verv = new DxfGroupVector();
                DxfGroup entityType = new DxfGroup(0, "VERTEX");
                verv.add(entityType);
                DxfGroup generalSubclassMarker = new DxfGroup(100, "AcDbEntity");
                verv.add(generalSubclassMarker);
                DxfGroup layerNameGroup = new DxfGroup(8, layerName);
                verv.add(layerNameGroup);
                DxfGroup vertexSubclassMarker = new DxfGroup(100, "AcDbVertex");
                verv.add(vertexSubclassMarker);
                DxfGroup xvertex = new DxfGroup();
                xvertex.setCode(10);
                xvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getX()));
                DxfGroup yvertex = new DxfGroup();
                yvertex.setCode(20);
                yvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getY()));
                DxfGroup zvertex = new DxfGroup();
                zvertex.setCode(30);
                zvertex.setData(new Double(velev[j]));
                verv.add(xvertex);
                verv.add(yvertex);
                verv.add(zvertex);
                this.entityMaker.addVertex(verv);
                ++k;
                ++j;
            }
            DxfGroupVector seqv = new DxfGroupVector();
            DxfGroup entityType = new DxfGroup(0, "SEQEND");
            seqv.add(entityType);
            DxfGroup generalSubclassMarker = new DxfGroup(100, "AcDbEntity");
            seqv.add(generalSubclassMarker);
            DxfGroup layerNameGroup = new DxfGroup(8, layerName);
            seqv.add(layerNameGroup);
            DxfGroup handleSeqGroup = new DxfGroup();
            handleSeqGroup.setCode(5);
            handleSeqGroup.setData(new Integer(handle + k).toString());
            seqv.add(handleSeqGroup);
            this.entityMaker.endSeq();
            ++k;
        }
        return k;
    }

    @Deprecated
    private void createPolygon2D(int handle, int k, IGeometry geom, Feature feature) throws Exception {
        String layerName = this.getLayerName(feature);
        Object selectedColor = this.getColor(feature);
        Object elevationObj = this.getElevation(feature);
        Object thicknessObj = this.getThickness(feature);
        DxfGroupVector polv = new DxfGroupVector();
        DxfGroup polylineLayer = new DxfGroup(8, layerName);
        polv.add(polylineLayer);
        DxfGroup handleGroup = new DxfGroup();
        handleGroup.setCode(5);
        handleGroup.setData(new Integer(handle + k).toString());
        polv.add(handleGroup);
        DxfGroup polylineFlag = new DxfGroup();
        polylineFlag.setCode(70);
        polylineFlag.setData(new Integer(1));
        polv.add(polylineFlag);
        Vector<FPoint2D> vpoints = new Vector<FPoint2D>();
        PathIterator theIterator = geom.getPathIterator(null);
        double[] theData = new double[6];
        while (!theIterator.isDone()) {
            int theType = theIterator.currentSegment(theData);
            switch (theType) {
                case 0: {
                    vpoints.add(new FPoint2D(theData[0], theData[1]));
                    break;
                }
                case 1: {
                    vpoints.add(new FPoint2D(theData[0], theData[1]));
                }
            }
            theIterator.next();
        }
        int j = 0;
        while (j < vpoints.size()) {
            DxfGroup xvertex = new DxfGroup();
            xvertex.setCode(10);
            xvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getX()));
            DxfGroup yvertex = new DxfGroup();
            yvertex.setCode(20);
            yvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getY()));
            polv.add(xvertex);
            polv.add(yvertex);
            ++j;
        }
        DxfGroup color = new DxfGroup();
        color.setCode(62);
        color.setData(selectedColor);
        polv.add(color);
        if (thicknessObj != null) {
            DxfGroup thickness = new DxfGroup();
            thickness.setCode(39);
            thickness.setData(thicknessObj);
            polv.add(thickness);
        }
        if (elevationObj != null) {
            DxfGroup elevation = new DxfGroup();
            elevation.setCode(38);
            elevation.setData(elevationObj);
            polv.add(elevation);
        }
        this.entityMaker.createLwPolyline(polv);
    }

    private int createPolyline3D(int handle, int k, IShapeGeometry3D geom, Feature feature) throws Exception {
        String layerName = this.getLayerName(feature);
        Object selectedColor = this.getColor(feature);
        Object thicknessObj = this.getThickness(feature);
        DxfGroupVector polv = new DxfGroupVector();
        DxfGroup polylineLayer = new DxfGroup(8, layerName);
        polv.add(polylineLayer);
        DxfGroup handleGroup = new DxfGroup();
        handleGroup.setCode(5);
        handleGroup.setData(new Integer(handle + k).toString());
        polv.add(handleGroup);
        Vector<FPoint2D> vpoints = new Vector<FPoint2D>();
        SAIGGeneralPathIterator theIterator = geom.getGeneralPathXIterator();
        double[] theData = new double[6];
        double[] velev = geom.getZs();
        while (!theIterator.isDone()) {
            int theType = theIterator.currentSegment(theData);
            switch (theType) {
                case 0: {
                    vpoints.add(new FPoint2D(theData[0], theData[1]));
                    break;
                }
                case 1: {
                    vpoints.add(new FPoint2D(theData[0], theData[1]));
                }
            }
            theIterator.next();
        }
        if (this.constantElevation(velev)) {
            DxfGroup polylineFlag = new DxfGroup();
            polylineFlag.setCode(70);
            polylineFlag.setData(new Integer(0));
            polv.add(polylineFlag);
            DxfGroup elevation = new DxfGroup();
            elevation.setCode(38);
            elevation.setData(new Double(velev[0]));
            polv.add(elevation);
            int j = 0;
            while (j < vpoints.size()) {
                DxfGroup xvertex = new DxfGroup();
                xvertex.setCode(10);
                xvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getX()));
                DxfGroup yvertex = new DxfGroup();
                yvertex.setCode(20);
                yvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getY()));
                polv.add(xvertex);
                polv.add(yvertex);
                ++j;
            }
            DxfGroup color = new DxfGroup();
            color.setCode(62);
            color.setData(selectedColor);
            polv.add(color);
            if (thicknessObj != null) {
                DxfGroup thickness = new DxfGroup();
                thickness.setCode(39);
                thickness.setData(thicknessObj);
                polv.add(thickness);
            }
            this.entityMaker.createLwPolyline(polv);
            ++k;
        } else {
            DxfGroup polylineFlag = new DxfGroup();
            polylineFlag.setCode(70);
            polylineFlag.setData(new Integer(8));
            polv.add(polylineFlag);
            DxfGroup xgroup = new DxfGroup();
            xgroup.setCode(10);
            xgroup.setData(new Double(0.0));
            polv.add(xgroup);
            DxfGroup ygroup = new DxfGroup();
            ygroup.setCode(20);
            ygroup.setData(new Double(0.0));
            polv.add(ygroup);
            DxfGroup elevation = new DxfGroup();
            elevation.setCode(30);
            elevation.setData(new Double(0.0));
            polv.add(elevation);
            DxfGroup subclassMarker = new DxfGroup(100, "AcDb3dPolyline");
            polv.add(subclassMarker);
            DxfGroup color = new DxfGroup();
            color.setCode(62);
            color.setData(selectedColor);
            polv.add(color);
            if (thicknessObj != null) {
                DxfGroup thickness = new DxfGroup();
                thickness.setCode(39);
                thickness.setData(thicknessObj);
                polv.add(thickness);
            }
            this.entityMaker.createPolyline(polv);
            ++k;
            int j = 0;
            while (j < vpoints.size()) {
                DxfGroupVector verv = new DxfGroupVector();
                DxfGroup entityType = new DxfGroup(0, "VERTEX");
                verv.add(entityType);
                DxfGroup generalSubclassMarker = new DxfGroup(100, "AcDbEntity");
                verv.add(generalSubclassMarker);
                DxfGroup layerNameGroup = new DxfGroup(8, layerName);
                verv.add(layerNameGroup);
                DxfGroup vertexSubclassMarker = new DxfGroup(100, "AcDbVertex");
                verv.add(vertexSubclassMarker);
                DxfGroup xvertex = new DxfGroup();
                xvertex.setCode(10);
                xvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getX()));
                DxfGroup yvertex = new DxfGroup();
                yvertex.setCode(20);
                yvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getY()));
                DxfGroup zvertex = new DxfGroup();
                zvertex.setCode(30);
                zvertex.setData(new Double(velev[j]));
                verv.add(xvertex);
                verv.add(yvertex);
                verv.add(zvertex);
                this.entityMaker.addVertex(verv);
                ++k;
                ++j;
            }
            DxfGroupVector seqv = new DxfGroupVector();
            DxfGroup entityType = new DxfGroup(0, "SEQEND");
            seqv.add(entityType);
            DxfGroup generalSubclassMarker = new DxfGroup(100, "AcDbEntity");
            seqv.add(generalSubclassMarker);
            DxfGroup layerNameGroup = new DxfGroup(8, DEFAULT_LAYER_NAME);
            seqv.add(layerNameGroup);
            DxfGroup handleSeqGroup = new DxfGroup();
            handleSeqGroup.setCode(5);
            handleSeqGroup.setData(new Integer(handle + k).toString());
            seqv.add(handleSeqGroup);
            this.entityMaker.endSeq();
            ++k;
        }
        return k;
    }

    private void createLwPolyline2D(int handle, int k, IShapeGeometry geom, boolean isPolygon, Feature feature) throws Exception {
        String layerName = this.getLayerName(feature);
        Object selectedColor = this.getColor(feature);
        Object elevationObj = this.getElevation(feature);
        Object thicknessObj = this.getThickness(feature);
        DxfGroupVector polv = null;
        DxfGroup polylineLayer = new DxfGroup(8, layerName);
        DxfGroup handleGroup = new DxfGroup();
        handleGroup.setCode(5);
        String handleStr = new Integer(handle + k).toString();
        handleGroup.setData(handleStr);
        Vector<FPoint2D> vpoints = new Vector<FPoint2D>();
        DxfGroup polylineFlag = new DxfGroup();
        polylineFlag.setCode(70);
        if (isPolygon) {
            polylineFlag.setData(new Integer(1));
        } else {
            polylineFlag.setData(new Integer(0));
        }
        SAIGGeneralPathIterator theIterator = geom.getGeneralPathXIterator();
        double[] theData = new double[6];
        while (!theIterator.isDone()) {
            int theType = theIterator.currentSegment(theData);
            switch (theType) {
                case 0: {
                    if (polv != null) {
                        int j = 0;
                        while (j < vpoints.size()) {
                            DxfGroup xvertex = new DxfGroup();
                            xvertex.setCode(10);
                            xvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getX()));
                            DxfGroup yvertex = new DxfGroup();
                            yvertex.setCode(20);
                            yvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getY()));
                            polv.add(xvertex);
                            polv.add(yvertex);
                            ++j;
                        }
                        DxfGroup color = new DxfGroup();
                        color.setCode(62);
                        color.setData(selectedColor);
                        polv.add(color);
                        if (thicknessObj != null) {
                            DxfGroup thickness = new DxfGroup();
                            thickness.setCode(39);
                            thickness.setData(thicknessObj);
                            polv.add(thickness);
                        }
                        if (elevationObj != null) {
                            DxfGroup elevation = new DxfGroup();
                            elevation.setCode(38);
                            elevation.setData(elevationObj);
                            polv.add(elevation);
                        }
                        this.entityMaker.createLwPolyline(polv);
                    }
                    polv = new DxfGroupVector();
                    polv.add(polylineLayer);
                    polv.add(handleGroup);
                    polv.add(polylineFlag);
                    vpoints.clear();
                    vpoints.add(new FPoint2D(theData[0], theData[1]));
                    break;
                }
                case 1: {
                    vpoints.add(new FPoint2D(theData[0], theData[1]));
                    break;
                }
                case 2: {
                    break;
                }
                case 3: {
                    break;
                }
                case 4: {
                    polylineFlag.setData(new Integer(1));
                }
            }
            theIterator.next();
        }
        int j = 0;
        while (j < vpoints.size()) {
            DxfGroup xvertex = new DxfGroup();
            xvertex.setCode(10);
            xvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getX()));
            DxfGroup yvertex = new DxfGroup();
            yvertex.setCode(20);
            yvertex.setData(new Double(((FPoint2D)vpoints.get(j)).getY()));
            polv.add(xvertex);
            polv.add(yvertex);
            ++j;
        }
        DxfGroup color = new DxfGroup();
        color.setCode(62);
        color.setData(selectedColor);
        polv.add(color);
        if (thicknessObj != null) {
            DxfGroup thickness = new DxfGroup();
            thickness.setCode(39);
            thickness.setData(thicknessObj);
            polv.add(thickness);
        }
        if (elevationObj != null) {
            DxfGroup elevation = new DxfGroup();
            elevation.setCode(38);
            elevation.setData(elevationObj);
            polv.add(elevation);
        }
        this.entityMaker.createLwPolyline(polv);
    }

    private void createPoint3D(int handle, int k, IShapeGeometry geom, Feature feature) throws Exception {
        String layerName = this.getLayerName(feature);
        Object selectedColor = this.getColor(feature);
        Object thicknessObj = this.getThickness(feature);
        FPoint3D point = new FPoint3D(0.0, 0.0, 0.0);
        double[] pointCoords = new double[6];
        double[] z = ((ShapePoint3D)geom.getShp()).getZs();
        SAIGGeneralPathIterator pointIt = geom.getGeneralPathXIterator();
        int p = 0;
        while (!pointIt.isDone()) {
            pointIt.currentSegment(pointCoords);
            point = new FPoint3D(pointCoords[0], pointCoords[1], z[p]);
            pointIt.next();
            ++p;
        }
        Point3D pto = new Point3D(point.getX(), point.getY(), point.getZs()[0]);
        DxfGroup pointLayer = new DxfGroup(8, layerName);
        DxfGroup handleGroup = new DxfGroup();
        handleGroup.setCode(5);
        handleGroup.setData(new Integer(handle + k).toString());
        DxfGroup px = new DxfGroup();
        DxfGroup py = new DxfGroup();
        DxfGroup pz = new DxfGroup();
        px.setCode(10);
        px.setData(new Double(pto.getX()));
        py.setCode(20);
        py.setData(new Double(pto.getY()));
        pz.setCode(30);
        pz.setData(new Double(pto.getZ()));
        DxfGroupVector pv = new DxfGroupVector();
        pv.add(pointLayer);
        pv.add(handleGroup);
        pv.add(px);
        pv.add(py);
        pv.add(pz);
        DxfGroup color = new DxfGroup();
        color.setCode(62);
        color.setData(selectedColor);
        pv.add(color);
        if (thicknessObj != null) {
            DxfGroup thickness = new DxfGroup();
            thickness.setCode(39);
            thickness.setData(thicknessObj);
            pv.add(thickness);
        }
        this.entityMaker.createPoint(pv);
    }

    private void createPoint2D(int handle, int k, IShapeGeometry geom, Feature feature) throws Exception {
        String layerName = this.getLayerName(feature);
        Object selectedColor = this.getColor(feature);
        Object thicknessObj = this.getThickness(feature);
        FPoint2D point = new FPoint2D(0.0, 0.0);
        double[] pointCoords = new double[6];
        SAIGGeneralPathIterator pointIt = geom.getGeneralPathXIterator();
        while (!pointIt.isDone()) {
            pointIt.currentSegment(pointCoords);
            point = new FPoint2D(pointCoords[0], pointCoords[1]);
            pointIt.next();
        }
        Point2D.Double pto = new Point2D.Double(point.getX(), point.getY());
        DxfGroup pointLayer = new DxfGroup(8, layerName);
        DxfGroup handleGroup = new DxfGroup();
        handleGroup.setCode(5);
        handleGroup.setData(new Integer(handle + k).toString());
        DxfGroup px = new DxfGroup();
        DxfGroup py = new DxfGroup();
        DxfGroup pz = new DxfGroup();
        px.setCode(10);
        px.setData(new Double(((Point2D)pto).getX()));
        py.setCode(20);
        py.setData(new Double(((Point2D)pto).getY()));
        pz.setCode(30);
        pz.setData(new Double(0.0));
        DxfGroupVector pv = new DxfGroupVector();
        pv.add(pointLayer);
        pv.add(handleGroup);
        pv.add(px);
        pv.add(py);
        pv.add(pz);
        DxfGroup color = new DxfGroup();
        color.setCode(62);
        color.setData(selectedColor);
        pv.add(color);
        if (thicknessObj != null) {
            DxfGroup thickness = new DxfGroup();
            thickness.setCode(39);
            thickness.setData(thicknessObj);
            pv.add(thickness);
        }
        this.entityMaker.createPoint(pv);
    }

    private void createText(int handle, int k, IShapeGeometry geom, Feature feature) throws Exception {
        double[] pointCoords;
        FPoint2D point;
        String layerName = this.getLayerName(feature);
        Object selectedColor = this.getColor(feature);
        Object thicknessObj = this.getThickness(feature);
        Object rotationText = this.getRotationText(feature);
        Object heightText = this.getHeightText(feature);
        Object text = this.getText(feature);
        double x = Double.NaN;
        double y = Double.NaN;
        double z = Double.NaN;
        if (geom.getShp() instanceof ShapePoint3D) {
            point = new FPoint3D(0.0, 0.0, 0.0);
            pointCoords = new double[6];
            double[] zs = ((ShapePoint3D)geom.getShp()).getZs();
            SAIGGeneralPathIterator pointIt = geom.getGeneralPathXIterator();
            int p = 0;
            while (!pointIt.isDone()) {
                pointIt.currentSegment(pointCoords);
                point = new FPoint3D(pointCoords[0], pointCoords[1], zs[p]);
                pointIt.next();
                ++p;
            }
            x = point.getX();
            y = point.getY();
            z = point.getZs()[0];
        } else {
            point = new FPoint2D(0.0, 0.0);
            pointCoords = new double[6];
            SAIGGeneralPathIterator pointIt = geom.getGeneralPathXIterator();
            while (!pointIt.isDone()) {
                pointIt.currentSegment(pointCoords);
                point = new FPoint2D(pointCoords[0], pointCoords[1]);
                pointIt.next();
            }
            x = point.getX();
            y = point.getY();
        }
        DxfGroup pointLayer = new DxfGroup(8, layerName);
        DxfGroup handleGroup = new DxfGroup();
        handleGroup.setCode(5);
        handleGroup.setData(new Integer(handle + k).toString());
        DxfGroup px = new DxfGroup();
        DxfGroup py = new DxfGroup();
        DxfGroup pz = new DxfGroup();
        px.setCode(10);
        px.setData(new Double(x));
        py.setCode(20);
        py.setData(new Double(y));
        pz.setCode(30);
        if (!Double.isNaN(z)) {
            pz.setData(new Double(z));
        }
        DxfGroupVector pv = new DxfGroupVector();
        pv.add(pointLayer);
        pv.add(handleGroup);
        pv.add(px);
        pv.add(py);
        if (!Double.isNaN(z)) {
            pv.add(pz);
        }
        DxfGroup color = new DxfGroup();
        color.setCode(62);
        color.setData(selectedColor);
        pv.add(color);
        if (thicknessObj != null) {
            DxfGroup thickness = new DxfGroup();
            thickness.setCode(39);
            thickness.setData(thicknessObj);
            pv.add(thickness);
        }
        DxfGroup rotation = new DxfGroup();
        rotation.setCode(50);
        rotation.setData(rotationText);
        pv.add(rotation);
        DxfGroup height = new DxfGroup();
        height.setCode(40);
        height.setData(heightText);
        pv.add(height);
        DxfGroup textGroup = new DxfGroup();
        textGroup.setCode(1);
        textGroup.setData(text);
        pv.add(textGroup);
        this.entityMaker.createText(pv);
    }

    private void createInsert(int handle, int k, IShapeGeometry geom, Feature feature) throws Exception {
        double[] pointCoords;
        FPoint2D point;
        String layerName = this.getLayerName(feature);
        Object selectedColor = this.getColor(feature);
        Object thicknessObj = this.getThickness(feature);
        Object rotationText = this.getRotationText(feature);
        Object heightText = this.getHeightText(feature);
        Object text = this.getText(feature);
        double x = Double.NaN;
        double y = Double.NaN;
        double z = Double.NaN;
        if (geom.getShp() instanceof ShapePoint3D) {
            point = new FPoint3D(0.0, 0.0, 0.0);
            pointCoords = new double[6];
            double[] zs = ((ShapePoint3D)geom.getShp()).getZs();
            SAIGGeneralPathIterator pointIt = geom.getGeneralPathXIterator();
            int p = 0;
            while (!pointIt.isDone()) {
                pointIt.currentSegment(pointCoords);
                point = new FPoint3D(pointCoords[0], pointCoords[1], zs[p]);
                pointIt.next();
                ++p;
            }
            x = point.getX();
            y = point.getY();
            z = point.getZs()[0];
        } else {
            point = new FPoint2D(0.0, 0.0);
            pointCoords = new double[6];
            SAIGGeneralPathIterator pointIt = geom.getGeneralPathXIterator();
            while (!pointIt.isDone()) {
                pointIt.currentSegment(pointCoords);
                point = new FPoint2D(pointCoords[0], pointCoords[1]);
                pointIt.next();
            }
            x = point.getX();
            y = point.getY();
        }
        DxfGroup pointLayer = new DxfGroup(8, layerName);
        DxfGroup handleGroup = new DxfGroup();
        handleGroup.setCode(5);
        handleGroup.setData(new Integer(handle + k).toString());
        DxfGroup px = new DxfGroup();
        DxfGroup py = new DxfGroup();
        DxfGroup pz = new DxfGroup();
        px.setCode(10);
        px.setData(new Double(x));
        py.setCode(20);
        py.setData(new Double(y));
        pz.setCode(30);
        if (!Double.isNaN(z)) {
            pz.setData(new Double(z));
        }
        DxfGroupVector pv = new DxfGroupVector();
        pv.add(pointLayer);
        pv.add(handleGroup);
        pv.add(px);
        pv.add(py);
        if (!Double.isNaN(z)) {
            pv.add(pz);
        }
        DxfGroup color = new DxfGroup();
        color.setCode(62);
        color.setData(selectedColor);
        pv.add(color);
        if (thicknessObj != null) {
            DxfGroup thicknessGroup = new DxfGroup();
            thicknessGroup.setCode(39);
            thicknessGroup.setData(thicknessObj);
            pv.add(thicknessGroup);
        }
        DxfGroup rotationTextGroup = new DxfGroup();
        rotationTextGroup.setCode(50);
        rotationTextGroup.setData(rotationText);
        pv.add(rotationTextGroup);
        DxfGroup heightTextGroup = new DxfGroup();
        heightTextGroup.setCode(40);
        heightTextGroup.setData(heightText);
        pv.add(heightTextGroup);
        DxfGroup textGroup = new DxfGroup();
        textGroup.setCode(1);
        textGroup.setData(text);
        pv.add(textGroup);
        this.entityMaker.createInsert(pv);
    }

    private boolean constantElevation(double[] velev) {
        boolean constant = true;
        int i = 0;
        while (i < velev.length) {
            if (velev[0] != velev[i]) {
                constant = false;
                break;
            }
            ++i;
        }
        return constant;
    }

    private String getLayerName(Feature feature) {
        String layerName = DEFAULT_LAYER_NAME;
        if (feature.getSchema().hasAttribute("Layer") && (layerName = (String)feature.getAttribute("Layer")) == null) {
            layerName = DEFAULT_LAYER_NAME;
        }
        return layerName;
    }

    private Object getColor(Feature feature) {
        Object selectedColor = DEFAULT_COLOR;
        if (feature.getSchema().hasAttribute("Color") && (selectedColor = feature.getAttribute("Color")) == null) {
            selectedColor = DEFAULT_COLOR;
        }
        return selectedColor;
    }

    private Object getThickness(Feature feature) {
        Object thicknessObj = DEFAULT_THICKNESS;
        if (feature.getSchema().hasAttribute("Thickness") && (thicknessObj = feature.getAttribute("Thickness")) == null) {
            thicknessObj = DEFAULT_THICKNESS;
        }
        return thicknessObj;
    }

    private Object getElevation(Feature feature) {
        Object elevationObj = DEFAULT_ELEVATION;
        if (feature.getSchema().hasAttribute("Elevation") && (elevationObj = feature.getAttribute("Elevation")) == null) {
            elevationObj = DEFAULT_ELEVATION;
        }
        return elevationObj;
    }

    private Object getRotationText(Feature feature) {
        Object rotationText = DEFAULT_ROTATION_TEXT;
        if (feature.getSchema().hasAttribute("RotationText") && (rotationText = feature.getAttribute("RotationText")) == null) {
            rotationText = DEFAULT_ROTATION_TEXT;
        }
        return rotationText;
    }

    private Object getHeightText(Feature feature) {
        Object heightText = DEFAULT_HEIGHT_TEXT;
        if (feature.getSchema().hasAttribute("HeightText") && (heightText = feature.getAttribute("HeightText")) == null) {
            heightText = DEFAULT_HEIGHT_TEXT;
        }
        return heightText;
    }

    private Object getText(Feature feature) {
        Object text = DEFAULT_TEXT;
        if (feature.getSchema().hasAttribute("Text") && (text = feature.getAttribute("Text")) == null) {
            text = DEFAULT_TEXT;
        }
        return text;
    }

    private void addXData(DxfGroupVector vectorGroup, Feature feat) {
        DxfGroup appNameGroup = new DxfGroup();
        appNameGroup.setCode(1001);
        appNameGroup.setData(KOSMO_DESKTOP_APPID);
        vectorGroup.add(appNameGroup);
        DxfGroup openBracketsGroup = new DxfGroup();
        openBracketsGroup.setCode(1002);
        openBracketsGroup.setData("{");
        vectorGroup.add(openBracketsGroup);
        FeatureSchema featSchema = feat.getSchema();
        for (String attrName : featSchema.getAttributeNames()) {
            if (this.protectedFieldNames.contains(attrName)) continue;
            Attribute attr = featSchema.getAttribute(attrName);
            DxfGroup attributeNameGroup = this.createAppNameDxfGroup(attr.getName());
            vectorGroup.add(attributeNameGroup);
            DxfGroup attributeValueGroup = new DxfGroup();
            attributeValueGroup.setCode(this.getDxfCode(attr.getType()));
            attributeValueGroup.setData(this.getAttributeValue(feat.getAttribute(attrName), attr.getType()));
            vectorGroup.add(attributeValueGroup);
        }
        DxfGroup closeBracketsGroup = new DxfGroup();
        closeBracketsGroup.setCode(1002);
        closeBracketsGroup.setData("}");
        vectorGroup.add(closeBracketsGroup);
    }

    private Object getAttributeValue(Object attribute, AttributeType type) {
        if (type.equals(AttributeType.INTEGER)) {
            return FeatureUtil.getGoodAttribute(AttributeType.INTEGER, (Number)attribute);
        }
        if (type.equals(AttributeType.LONG)) {
            return FeatureUtil.getGoodAttribute(AttributeType.LONG, (Number)attribute);
        }
        if (type.equals(AttributeType.DOUBLE)) {
            return FeatureUtil.getGoodAttribute(AttributeType.STRING, (Number)attribute);
        }
        return FeatureUtil.getGoodAttribute(AttributeType.STRING, attribute);
    }

    private int getDxfCode(AttributeType type) {
        if (type.equals(AttributeType.INTEGER)) {
            return 1070;
        }
        if (type.equals(AttributeType.LONG)) {
            return 1071;
        }
        if (type.equals(AttributeType.DOUBLE)) {
            return 1040;
        }
        return 1000;
    }

    protected DxfGroup createAppNameDxfGroup(String name) {
        DxfGroup g = new DxfGroup();
        g.setCode(1001);
        g.setData(name);
        return g;
    }
}

