/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.cit.gvsig.fmap.drivers.shp.ShapeFileHeader2
 *  com.iver.utiles.bigfile.BigByteBuffer2
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.filedatasource.shape;

import com.iver.cit.gvsig.fmap.drivers.shp.ShapeFileHeader2;
import com.iver.utiles.bigfile.BigByteBuffer2;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFileException;
import org.saig.core.dao.datasource.filedatasource.dbf.nio.DbaseFileNIO;
import org.saig.core.dao.datasource.filedatasource.shape.ShapefileException;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeFactory;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHP;
import org.saig.jump.lang.I18N;

public class ShapeConnection {
    private static final Logger LOGGER = Logger.getLogger(ShapeConnection.class);
    private DbaseFileNIO dbaseFileChannel;
    private File fileShp;
    private BigByteBuffer2 bb;
    private FileChannel channel;
    private FileInputStream fin;
    private int type;
    private int numReg;
    private Rectangle2D extent;
    private BigByteBuffer2 bbShx;
    private FileChannel channelShx;
    private FileInputStream finShx;
    private Charset dbfCharset;
    private static Map<Integer, String> shapeTypeToDescriptionMap = new HashMap<Integer, String>();

    static {
        shapeTypeToDescriptionMap.put(new Integer(0), "Null shape (0)");
        shapeTypeToDescriptionMap.put(new Integer(1), "Point (1)");
        shapeTypeToDescriptionMap.put(new Integer(3), "PolyLine (3)");
        shapeTypeToDescriptionMap.put(new Integer(5), "Polygon (5)");
        shapeTypeToDescriptionMap.put(new Integer(8), "MultiPoint (8)");
        shapeTypeToDescriptionMap.put(new Integer(11), "PointZ (11)");
        shapeTypeToDescriptionMap.put(new Integer(13), "PolyLineZ (13)");
        shapeTypeToDescriptionMap.put(new Integer(15), "PolygonZ (15)");
        shapeTypeToDescriptionMap.put(new Integer(18), "MultiPointZ (18)");
        shapeTypeToDescriptionMap.put(new Integer(21), "PointM (21)");
        shapeTypeToDescriptionMap.put(new Integer(23), "PolyLineM (23)");
        shapeTypeToDescriptionMap.put(new Integer(25), "PolygonM (25)");
        shapeTypeToDescriptionMap.put(new Integer(28), "MultiPointM (28)");
        shapeTypeToDescriptionMap.put(new Integer(31), "MultiPatch (31)");
    }

    public ShapeConnection(File file) {
        this.fileShp = file;
    }

    public ShapeConnection(File file, Charset dbfCharset) {
        this.fileShp = file;
        this.dbfCharset = dbfCharset;
    }

    public void open() throws IOException, DbfFileException {
        this.fin = new FileInputStream(this.fileShp);
        this.channel = this.fin.getChannel();
        this.bb = new BigByteBuffer2(this.channel, FileChannel.MapMode.READ_ONLY);
        this.finShx = new FileInputStream(SHP.getShxFile(this.fileShp));
        this.channelShx = this.finShx.getChannel();
        this.bbShx = new BigByteBuffer2(this.channelShx, FileChannel.MapMode.READ_ONLY);
        this.bbShx.order(ByteOrder.BIG_ENDIAN);
        File dbfFile = SHP.getDbfFile(this.fileShp);
        this.dbaseFileChannel = new DbaseFileNIO(this.dbfCharset);
        this.dbaseFileChannel.setFile(dbfFile);
        this.dbaseFileChannel.open();
        this.numReg = this.dbaseFileChannel.getRecordCount();
    }

    public void close() throws Exception {
        IOException ret;
        block21: {
            ret = null;
            try {
                try {
                    if (this.channel != null) {
                        this.channel.close();
                    }
                    if (this.channelShx != null) {
                        this.channelShx.close();
                    }
                    if (this.dbaseFileChannel != null) {
                        this.dbaseFileChannel.close(false);
                    }
                }
                catch (IOException e) {
                    ret = e;
                    try {
                        if (this.fin != null) {
                            this.fin.close();
                        }
                        if (this.finShx != null) {
                            this.finShx.close();
                        }
                        break block21;
                    }
                    catch (IOException e1) {
                        ret = e1;
                    }
                    break block21;
                }
            }
            catch (Throwable throwable) {
                try {
                    if (this.fin != null) {
                        this.fin.close();
                    }
                    if (this.finShx != null) {
                        this.finShx.close();
                    }
                }
                catch (IOException e1) {
                    ret = e1;
                }
                throw throwable;
            }
            try {
                if (this.fin != null) {
                    this.fin.close();
                }
                if (this.finShx != null) {
                    this.finShx.close();
                }
            }
            catch (IOException e1) {
                ret = e1;
            }
        }
        if (ret != null) {
            throw ret;
        }
        this.bb = null;
        this.bbShx = null;
    }

    public void initialize() throws Exception {
        this.open();
        ShapeFileHeader2 myHeader = new ShapeFileHeader2();
        this.bb.position(0L);
        myHeader.readHeader(this.bb);
        this.extent = new Rectangle2D.Double(myHeader.myXmin, myHeader.myYmin, myHeader.myXmax - myHeader.myXmin, myHeader.myYmax - myHeader.myYmin);
        this.type = myHeader.myShapeType;
        if (!this.checkValidShapeType()) {
            Object typeDesc = new Integer(this.type);
            if (shapeTypeToDescriptionMap.containsKey(typeDesc)) {
                typeDesc = shapeTypeToDescriptionMap.get(typeDesc);
            }
            throw new ShapefileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.shape.ShapeConnection.Unsupported-shape-type-{0}", new Object[]{typeDesc}));
        }
        double x = myHeader.myXmin;
        double y = myHeader.myYmin;
        double w = myHeader.myXmax - myHeader.myXmin;
        double h = myHeader.myYmax - myHeader.myYmin;
        if (w == 0.0) {
            x -= 0.1;
            w = 0.2;
        }
        if (h == 0.0) {
            y -= 0.1;
            h = 0.2;
        }
    }

    public FeatureSchema getShapefileSchema() {
        FeatureSchema fs = new FeatureSchema();
        int numfields = this.dbaseFileChannel.getFieldCount();
        int j = 0;
        while (j < numfields) {
            String currentAttrName = this.dbaseFileChannel.getFieldName(j);
            if (fs.hasAttribute(currentAttrName)) {
                String baseName = StringUtils.substring((String)currentAttrName, (int)0, (int)(currentAttrName.length() - 2));
                String name = "";
                int cont = 1;
                while (fs.hasAttribute(name = String.valueOf(baseName) + "_" + cont++)) {
                }
                LOGGER.warn((Object)I18N.getMessage(this.getClass(), "attribute-{0}-already-exist-and-will-be-substituted-with-attribute-{1}", new Object[]{currentAttrName, name}));
                currentAttrName = name;
            }
            fs.addAttribute(currentAttrName, AttributeType.toAttributeType(this.dbaseFileChannel.getRealFieldType(j)));
            ++j;
        }
        if (!fs.hasAttribute("GID") && !fs.hasAttribute("gid")) {
            fs.addAttribute("GID", AttributeType.INTEGER, Boolean.TRUE);
        } else if (fs.hasAttribute("GID")) {
            fs.getAttribute("GID").setPrimaryKey(true);
        } else {
            fs.getAttribute("gid").setPrimaryKey(true);
        }
        fs.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        fs.setGeometryType(this.getGeometryType());
        return fs;
    }

    public synchronized Rectangle2D getShapeBounds(int index) throws IOException {
        Point2D.Double p = new Point2D.Double();
        Rectangle2D.Double BoundingBox2 = new Rectangle2D.Double();
        this.bb.position(this.getPositionForRecord(index));
        this.bb.order(ByteOrder.LITTLE_ENDIAN);
        int tipoShape = this.bb.getInt();
        switch (tipoShape) {
            case 1: 
            case 11: {
                p = this.readPoint(this.bb);
                BoundingBox2 = new Rectangle2D.Double(((Point2D)p).getX() - 0.1, ((Point2D)p).getY() - 0.1, 0.2, 0.2);
                break;
            }
            case 3: 
            case 5: 
            case 8: 
            case 13: 
            case 15: 
            case 18: {
                BoundingBox2 = this.readRectangle(this.bb);
            }
        }
        return BoundingBox2;
    }

    public synchronized ShapeGeometry getShape(int index) throws IOException {
        Point2D.Double p = new Point2D.Double();
        this.bb.position(this.getPositionForRecord(index));
        this.bb.order(ByteOrder.LITTLE_ENDIAN);
        int shapeType = this.bb.getInt();
        if (shapeType == 0) {
            return null;
        }
        switch (shapeType) {
            case 1: {
                p = this.readPoint(this.bb);
                return ShapeFactory.createPoint2D(p.getX(), p.getY());
            }
            case 3: {
                this.bb.position(this.bb.position() + 32L);
                int numParts = this.bb.getInt();
                int numPoints = this.bb.getInt();
                SAIGGeneralPath elShape = new SAIGGeneralPath(0, numPoints);
                int[] tempParts = new int[numParts];
                int i = 0;
                while (i < numParts) {
                    tempParts[i] = this.bb.getInt();
                    ++i;
                }
                int j = 0;
                i = 0;
                while (i < numPoints) {
                    p = this.readPoint(this.bb);
                    if (i == tempParts[j]) {
                        elShape.moveTo(p.x, p.y);
                        if (j < numParts - 1) {
                            ++j;
                        }
                    } else {
                        elShape.lineTo(p.x, p.y);
                    }
                    ++i;
                }
                return ShapeFactory.createPolyline2D(elShape);
            }
            case 8: {
                this.bb.position(this.bb.position() + 32L);
                int numPoints = this.bb.getInt();
                double[] tempX = new double[numPoints];
                double[] tempY = new double[numPoints];
                int i = 0;
                while (i < numPoints) {
                    tempX[i] = this.bb.getDouble();
                    tempY[i] = this.bb.getDouble();
                    ++i;
                }
                return ShapeFactory.createMultipoint2D(tempX, tempY);
            }
            case 5: {
                this.bb.getDouble();
                this.bb.getDouble();
                this.bb.getDouble();
                this.bb.getDouble();
                int numParts = this.bb.getInt();
                int numPoints = this.bb.getInt();
                SAIGGeneralPath elShape = new SAIGGeneralPath(0, numPoints);
                int[] tempParts = new int[numParts];
                int i = 0;
                while (i < numParts) {
                    tempParts[i] = this.bb.getInt();
                    ++i;
                }
                int j = 0;
                i = 0;
                while (i < numPoints) {
                    p = this.readPoint(this.bb);
                    if (i == tempParts[j]) {
                        elShape.moveTo(p.x, p.y);
                        if (j < numParts - 1) {
                            ++j;
                        }
                    } else {
                        elShape.lineTo(p.x, p.y);
                    }
                    ++i;
                }
                return ShapeFactory.createPolygon2D(elShape);
            }
            case 11: {
                double x = this.bb.getDouble();
                double y = this.bb.getDouble();
                double z = this.bb.getDouble();
                return ShapeFactory.createPoint3D(x, y, z);
            }
            case 13: {
                this.bb.position(this.bb.position() + 32L);
                int numParts = this.bb.getInt();
                int numPoints = this.bb.getInt();
                SAIGGeneralPath elShape = new SAIGGeneralPath(0, numPoints);
                int[] tempParts = new int[numParts];
                int i = 0;
                while (i < numParts) {
                    tempParts[i] = this.bb.getInt();
                    ++i;
                }
                int j = 0;
                i = 0;
                while (i < numPoints) {
                    p = this.readPoint(this.bb);
                    if (i == tempParts[j]) {
                        elShape.moveTo(p.x, p.y);
                        if (j < numParts - 1) {
                            ++j;
                        }
                    } else {
                        elShape.lineTo(p.x, p.y);
                    }
                    ++i;
                }
                double[] boxZ = new double[]{this.bb.getDouble(), this.bb.getDouble()};
                double[] pZ = new double[numPoints];
                int i2 = 0;
                while (i2 < numPoints) {
                    pZ[i2] = this.bb.getDouble();
                    ++i2;
                }
                return ShapeFactory.createPolyline3D(elShape, pZ);
            }
            case 15: {
                this.bb.position(this.bb.position() + 32L);
                int numParts = this.bb.getInt();
                int numPoints = this.bb.getInt();
                SAIGGeneralPath elShape = new SAIGGeneralPath(0, numPoints);
                int[] tempParts = new int[numParts];
                int i = 0;
                while (i < numParts) {
                    tempParts[i] = this.bb.getInt();
                    ++i;
                }
                int j = 0;
                i = 0;
                while (i < numPoints) {
                    p = this.readPoint(this.bb);
                    if (i == tempParts[j]) {
                        elShape.moveTo(p.x, p.y);
                        if (j < numParts - 1) {
                            ++j;
                        }
                    } else {
                        elShape.lineTo(p.x, p.y);
                    }
                    ++i;
                }
                double[] boxpoZ = new double[]{this.bb.getDouble(), this.bb.getDouble()};
                double[] poZ = new double[numPoints];
                int i3 = 0;
                while (i3 < numPoints) {
                    poZ[i3] = this.bb.getDouble();
                    ++i3;
                }
                return ShapeFactory.createPolygon3D(elShape, poZ);
            }
            case 18: {
                this.bb.position(this.bb.position() + 32L);
                int numPoints = this.bb.getInt();
                double[] temX = new double[numPoints];
                double[] temY = new double[numPoints];
                double[] temZ = new double[numPoints];
                int i = 0;
                while (i < numPoints) {
                    temX[i] = this.bb.getDouble();
                    temY[i] = this.bb.getDouble();
                    ++i;
                }
                i = 0;
                while (i < numPoints) {
                    temZ[i] = this.bb.getDouble();
                    ++i;
                }
                return ShapeFactory.createMultipoint3D(temX, temY, temZ);
            }
        }
        return null;
    }

    public Object readField(int recordNumber, int indexColumn) throws Exception {
        return this.dbaseFileChannel.getRecord(recordNumber)[indexColumn];
    }

    public Object[] readField(int recordNumber) throws Exception {
        return this.dbaseFileChannel.getRecord(recordNumber);
    }

    private Point2D.Double readPoint(BigByteBuffer2 in) {
        Point2D.Double tempPoint = new Point2D.Double();
        in.order(ByteOrder.LITTLE_ENDIAN);
        tempPoint.setLocation(in.getDouble(), in.getDouble());
        return tempPoint;
    }

    private Rectangle2D.Double readRectangle(BigByteBuffer2 in) throws IOException {
        Rectangle2D.Double tempRect = new Rectangle2D.Double();
        in.order(ByteOrder.LITTLE_ENDIAN);
        tempRect.x = in.getDouble();
        tempRect.y = in.getDouble();
        tempRect.width = in.getDouble() - tempRect.x;
        if (tempRect.width == 0.0) {
            tempRect.width = 0.2;
            tempRect.x -= 0.1;
        }
        tempRect.height = in.getDouble() - tempRect.y;
        if (tempRect.height == 0.0) {
            tempRect.height = 0.2;
            tempRect.y -= 0.1;
        }
        return tempRect;
    }

    public Feature readFeature(int recordNumber, ShapeGeometry pathGeom, boolean loadJTSGeometry, FeatureSchema schema) throws IOException, Exception {
        Integer key = new Integer(recordNumber);
        BasicFeature feature = new BasicFeature(schema);
        Object[] values = this.dbaseFileChannel.getRecord(recordNumber);
        int numDbfFields = this.dbaseFileChannel.getFieldCount();
        int i = 0;
        while (i < numDbfFields) {
            feature.setAttribute(i, values[i]);
            ++i;
        }
        if (loadJTSGeometry) {
            if (pathGeom == null) {
                return null;
            }
            Geometry geom = pathGeom.toJTSGeometry();
            feature.setGeometry(geom);
        }
        if (schema.hasAttribute("GID")) {
            feature.setAttribute("GID", (Object)key);
        } else if (schema.hasAttribute("gid")) {
            feature.setAttribute("gid", (Object)key);
        }
        return feature;
    }

    private synchronized long getPositionForRecord(int numRec) {
        int posIndex = 100 + numRec * 8;
        long pos = 8 + 2 * this.bbShx.getInt(posIndex);
        return pos;
    }

    public Rectangle2D getFullExtent() throws IOException {
        return this.extent;
    }

    public int getShapeCount() {
        return this.numReg;
    }

    public int getShapeType() {
        return this.type;
    }

    public int getGeometryType() {
        int geometryType = 0;
        switch (this.type) {
            case 1: 
            case 11: 
            case 21: {
                geometryType = 1;
                break;
            }
            case 3: 
            case 13: 
            case 23: {
                geometryType = 3;
                break;
            }
            case 5: 
            case 15: 
            case 25: {
                geometryType = 5;
                break;
            }
            case 8: 
            case 18: 
            case 28: {
                geometryType = 8;
            }
        }
        return geometryType;
    }

    private boolean checkValidShapeType() {
        return this.getGeometryType() != 0;
    }

    public boolean is3d() {
        return this.type == 11 || this.type == 13 || this.type == 15 || this.type == 18;
    }
}

