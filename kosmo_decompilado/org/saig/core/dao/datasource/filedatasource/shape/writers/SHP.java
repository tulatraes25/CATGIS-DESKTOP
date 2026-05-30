/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.algorithm.CGAlgorithms
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.filedatasource.shape.writers;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.FileUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFieldDef;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFile;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFileException;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFileWriter;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.ShapefileException;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHPFileWrite;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHPMultiLine;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHPMultiPoint;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHPPoint;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHPPolygon;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHPShape;
import org.saig.core.dao.datasource.filedatasource.shape.writers.ShapeFileFilter;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class SHP {
    private static final Logger LOGGER = Logger.getLogger(SHP.class);
    private static GeometryFactory geomfac = new GeometryFactory();

    public static SHPShape create(int type) throws ShapefileException {
        SHPShape shape;
        switch (type) {
            case 1: 
            case 11: 
            case 21: {
                shape = new SHPPoint(type);
                break;
            }
            case 3: 
            case 13: 
            case 23: {
                shape = new SHPMultiLine(type);
                break;
            }
            case 5: 
            case 15: 
            case 25: {
                shape = new SHPPolygon(type);
                break;
            }
            case 8: 
            case 18: 
            case 28: {
                shape = new SHPMultiPoint(type);
                break;
            }
            default: {
                shape = null;
            }
        }
        return shape;
    }

    public static DbfFileWriter writeDBFHeader(FeatureSchema fs, int size, String fileName, boolean savePK, boolean saveAttrCal, Charset charset) throws Exception {
        ArrayList<String> fieldsNamesList = new ArrayList<String>();
        ArrayList<DbfFieldDef> fieldsList = new ArrayList<DbfFieldDef>();
        int t = 0;
        while (t < fs.getAttributeCount()) {
            Attribute attr = fs.getAttribute(t);
            AttributeType columnType = fs.getAttributeType(t);
            if (!(attr.isPrimaryKey() && !savePK || attr.isCalculated() && !saveAttrCal || columnType.equals(AttributeType.GEOMETRY))) {
                String columnName = fs.getAttributeName(t);
                if (columnType == AttributeType.INTEGER || columnType == AttributeType.TINYINT || columnType == AttributeType.SMALLINT || columnType == AttributeType.BIT) {
                    fieldsNamesList.add(columnName);
                    fieldsList.add(new DbfFieldDef(columnName, 'N', 32, 0));
                } else if (columnType == AttributeType.LONG || columnType == AttributeType.BIGINT) {
                    fieldsNamesList.add(columnName);
                    fieldsList.add(new DbfFieldDef(columnName, 'N', 33, 0));
                } else if (columnType == AttributeType.DOUBLE || columnType == AttributeType.REAL || columnType == AttributeType.NUMERIC || columnType == AttributeType.FLOAT || columnType == AttributeType.BIGDECIMAL || columnType == AttributeType.DECIMAL) {
                    fieldsNamesList.add(columnName);
                    fieldsList.add(new DbfFieldDef(columnName, 'N', 33, 16));
                } else if (columnType == AttributeType.STRING || columnType == AttributeType.CHAR || columnType == AttributeType.VARCHAR || columnType == AttributeType.LONGVARCHAR || columnType == AttributeType.TEXT || columnType == AttributeType.OBJECT) {
                    fieldsNamesList.add(columnName);
                    fieldsList.add(new DbfFieldDef(columnName, 'C', 255, 0));
                } else if (columnType == AttributeType.DATE || columnType == AttributeType.TIME || columnType == AttributeType.TIMESTAMP) {
                    fieldsNamesList.add(columnName);
                    fieldsList.add(new DbfFieldDef(columnName, 'D', 8, 0));
                } else if (columnType == AttributeType.BOOLEAN) {
                    fieldsNamesList.add(columnName);
                    fieldsList.add(new DbfFieldDef(columnName, 'L', 1, 0));
                } else {
                    throw new Exception("Unsupported attribute type found in featurecollection");
                }
            }
            ++t;
        }
        DbfFieldDef[] fields = new DbfFieldDef[fieldsList.size()];
        fieldsList.toArray(fields);
        String[] fieldsNames = new String[fieldsNamesList.size()];
        fieldsNamesList.toArray(fieldsNames);
        DbfFileWriter dbf = new DbfFileWriter(fileName, charset);
        dbf.writeHeader(fields, size);
        dbf.setFieldsNames(fieldsNames);
        return dbf;
    }

    private static void writeDBFRow(FeatureSchema fs, Feature feat, DbfFileWriter dbf) throws DbfFileException, IOException {
        String[] fieldsNames = dbf.getFieldsNames();
        Vector<Object> dbfRow = new Vector<Object>();
        int u = 0;
        while (u < fieldsNames.length) {
            String fieldName = fieldsNames[u];
            AttributeType columnType = fs.getAttributeType(fieldName);
            Object a = feat.getAttribute(fieldName);
            if (columnType == AttributeType.INTEGER || columnType == AttributeType.TINYINT || columnType == AttributeType.SMALLINT || columnType == AttributeType.BIT) {
                if (a == null || a instanceof String && StringUtils.isEmpty((String)((String)a))) {
                    dbfRow.add(null);
                } else {
                    dbfRow.add(new Integer(((Number)a).intValue()));
                }
            } else if (columnType == AttributeType.LONG || columnType == AttributeType.BIGINT) {
                if (a == null || a instanceof String && StringUtils.isEmpty((String)((String)a))) {
                    dbfRow.add(null);
                } else {
                    dbfRow.add(new Long(((Number)a).longValue()));
                }
            } else if (columnType == AttributeType.DOUBLE || columnType == AttributeType.REAL || columnType == AttributeType.NUMERIC || columnType == AttributeType.FLOAT || columnType == AttributeType.BIGDECIMAL || columnType == AttributeType.DECIMAL) {
                if (a == null || a instanceof String && StringUtils.isEmpty((String)((String)a))) {
                    dbfRow.add(null);
                } else {
                    dbfRow.add(new Double(((Number)a).doubleValue()));
                }
            } else if (columnType == AttributeType.DATE || columnType == AttributeType.TIME || columnType == AttributeType.TIMESTAMP) {
                if (a == null || a instanceof String && StringUtils.isEmpty((String)((String)a))) {
                    dbfRow.add(null);
                } else {
                    dbfRow.add(DbfFile.DATE_PARSER.format((Date)a));
                }
            } else if (columnType == AttributeType.STRING || columnType == AttributeType.CHAR || columnType == AttributeType.VARCHAR || columnType == AttributeType.LONGVARCHAR || columnType == AttributeType.TEXT || columnType == AttributeType.OBJECT) {
                if (a == null) {
                    dbfRow.add(null);
                } else if (a instanceof String) {
                    dbfRow.add(a);
                } else {
                    dbfRow.add(a.toString());
                }
            } else if (columnType == AttributeType.BOOLEAN) {
                if (a == null || a instanceof String && StringUtils.isEmpty((String)((String)a))) {
                    dbfRow.add(null);
                } else {
                    dbfRow.add(a.toString());
                }
            }
            ++u;
        }
        dbf.writeRecord(dbfRow);
    }

    /*
     * Unable to fully structure code
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static boolean checkMultiGeometry(Iterator it, int geomType) {
        check = false;
        try {
            try {
                block13: {
                    block12: {
                        if (geomType != 1) break block12;
                        if (true) ** GOTO lbl9
                        do {
                            if ((feat = (Feature)it.next()).getGeometry() instanceof MultiPoint) {
                                check = true;
                            }
lbl9:
                            // 4 sources

                            if (!it.hasNext()) return check;
                        } while (!check);
                        return check;
                    }
                    if (geomType != 3) break block13;
                    if (true) ** GOTO lbl18
                    do {
                        if ((feat = (Feature)it.next()).getGeometry() instanceof MultiLineString) {
                            check = true;
                        }
lbl18:
                        // 4 sources

                        if (!it.hasNext()) return check;
                    } while (!check);
                    return check;
                }
                if (geomType != 5) return check;
                if (true) ** GOTO lbl27
                do {
                    if ((feat = (Feature)it.next()).getGeometry() instanceof MultiPolygon) {
                        check = true;
                    }
lbl27:
                    // 4 sources

                    if (!it.hasNext()) return check;
                } while (!check);
                return check;
            }
            catch (Exception e) {
                SHP.LOGGER.error((Object)"", (Throwable)e);
                if (it == null || it == null || !(it instanceof FeatureIterator)) return check;
                ((FeatureIterator)it).close();
            }
            return check;
        }
        finally {
            if (it != null && it != null && it instanceof FeatureIterator) {
                ((FeatureIterator)it).close();
            }
        }
    }

    private static Polygon makeGoodSHAPEPolygon(Polygon p) {
        LinearRing[] holes = new LinearRing[p.getNumInteriorRing()];
        Coordinate[] coords = p.getExteriorRing().getCoordinates();
        LinearRing outer = CGAlgorithms.isCCW((Coordinate[])coords) ? SHP.reverseRing((LinearRing)p.getExteriorRing()) : (LinearRing)p.getExteriorRing();
        int t = 0;
        while (t < p.getNumInteriorRing()) {
            coords = p.getInteriorRingN(t).getCoordinates();
            holes[t] = !CGAlgorithms.isCCW((Coordinate[])coords) ? SHP.reverseRing((LinearRing)p.getInteriorRingN(t)) : (LinearRing)p.getInteriorRingN(t);
            ++t;
        }
        return geomfac.createPolygon(outer, holes);
    }

    private static MultiPolygon makeGoodSHAPEMultiPolygon(MultiPolygon mp) {
        Polygon[] ps = new Polygon[mp.getNumGeometries()];
        int t = 0;
        while (t < mp.getNumGeometries()) {
            ps[t] = SHP.makeGoodSHAPEPolygon((Polygon)mp.getGeometryN(t));
            ++t;
        }
        return geomfac.createMultiPolygon(ps);
    }

    private static LinearRing reverseRing(LinearRing lr) {
        int numPoints = lr.getNumPoints();
        Coordinate[] newCoords = new Coordinate[numPoints];
        int t = 0;
        while (t < numPoints) {
            newCoords[t] = lr.getCoordinateN(numPoints - t - 1);
            ++t;
        }
        return geomfac.createLinearRing(newCoords);
    }

    public static double[] getZMinMax(double[] zs) {
        if (zs == null) {
            return null;
        }
        double min = Double.MAX_VALUE;
        double max = Double.NEGATIVE_INFINITY;
        int i = 0;
        while (i < zs.length) {
            if (zs[i] > max) {
                max = zs[i];
            }
            if (zs[i] < min) {
                min = zs[i];
            }
            ++i;
        }
        return new double[]{min, max};
    }

    private static SHPFileWrite createSHPFileWrite(File shapeFile) throws Exception {
        String shpPath = shapeFile.getAbsolutePath();
        String shxPath = SHP.getShxFile(shapeFile).getAbsolutePath();
        return new SHPFileWrite((FileChannel)SHP.getWriteChannel(shpPath), (FileChannel)SHP.getWriteChannel(shxPath));
    }

    public static WritableByteChannel getWriteChannel(String path) throws IOException {
        File f = new File(path);
        if (!f.exists() && !f.createNewFile()) {
            System.err.print("Error al crear el fichero " + f.getAbsolutePath());
            throw new IOException("Cannot create file " + f);
        }
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        FileChannel channel = raf.getChannel();
        return channel;
    }

    public static void createShapeFile(FeatureSchema fs, IShapeGeometry geomSample, FeatureIterator it, File file, boolean savePK, boolean saveAttrCal, boolean createTempFile, Charset charset) throws Exception {
        int shapeType = SHP.getTypeShape(geomSample.getGeometryType());
        SHP.createShapeFile(fs, shapeType, it, file, savePK, saveAttrCal, createTempFile, charset);
    }

    public static void createShapeFile(FeatureSchema fs, int shapeType, FeatureIterator it, File file, boolean savePK, boolean saveAttrCal, boolean createTempFile, Charset charset) throws Exception {
        SHP.checkFiles(file);
        if (createTempFile) {
            File dbfFile = SHP.getDbfFile(file);
            File shxFile = SHP.getShxFile(file);
            String tempSHPName = FileUtil.uniqueTempFileName(String.valueOf(file.getName()) + "_temp", "shp");
            String tempSHXName = String.valueOf(FileUtil.nameWithoutExtension(tempSHPName)) + ".shx";
            String tempDBFName = String.valueOf(FileUtil.nameWithoutExtension(tempSHPName)) + ".dbf";
            File shpTempFile = new File(tempSHPName);
            SHP.createShapeFile(fs, shapeType, it, shpTempFile, savePK, saveAttrCal, charset);
            File shxTempFile = new File(tempSHXName);
            File dbfTempFile = new File(tempDBFName);
            try {
                FileUtil.copy(shpTempFile, file);
                FileUtil.copy(shxTempFile, shxFile);
                FileUtil.copy(dbfTempFile, dbfFile);
            }
            finally {
                shpTempFile.delete();
                shxTempFile.delete();
                dbfTempFile.delete();
            }
        } else {
            SHP.createShapeFile(fs, shapeType, it, file, savePK, saveAttrCal, charset);
        }
    }

    /*
     * Unable to fully structure code
     */
    private static void createShapeFile(FeatureSchema fs, int shapeType, FeatureIterator it, File file, boolean savePK, boolean saveAttrCal, Charset charset) throws Exception {
        block7: {
            dbfFile = SHP.getDbfFile(file);
            dbfName = dbfFile.getAbsolutePath();
            dbfFileWriter = SHP.writeDBFHeader(fs, 0, dbfName, savePK, saveAttrCal, charset);
            shpFileWrite = SHP.createSHPFileWrite(file);
            shpFileWrite.initialize(shapeType);
            featSchemaGeomType = fs.getGeometryType();
            try {
                if (featSchemaGeomType != 5 && featSchemaGeomType != 4) ** GOTO lbl24
                while (it.hasNext()) {
                    feat = it.next().clone(true);
                    SHP.writeDBFRow(fs, feat, dbfFileWriter);
                    geom = feat.getGeometry();
                    pathGeom = null;
                    pathGeom = geom instanceof Polygon != false ? ShapeGeometryConverter.jts_to_igeometry((Geometry)SHP.makeGoodSHAPEPolygon((Polygon)geom)) : ShapeGeometryConverter.jts_to_igeometry((Geometry)SHP.makeGoodSHAPEMultiPolygon((MultiPolygon)geom));
                    shpFileWrite.processGeometry(pathGeom);
                }
                break block7;
lbl-1000:
                // 1 sources

                {
                    feat = it.next();
                    if (feat.getGeometry().isEmpty()) {
                        SHP.LOGGER.info((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.writers.SHP.empty-geometry-skip-it"));
                        continue;
                    }
                    SHP.writeDBFRow(fs, feat, dbfFileWriter);
                    shpFileWrite.processGeometry(ShapeGeometryConverter.jts_to_igeometry(feat.getGeometry()));
lbl24:
                    // 3 sources

                    ** while (it.hasNext())
                }
lbl25:
                // 1 sources

            }
            finally {
                if (it != null) {
                    it.close();
                }
            }
        }
        shpFileWrite.writeRealHeaders();
        shpFileWrite.close();
        dbfFileWriter.writeRealHeader(shpFileWrite.getNumberOfGeometries());
        dbfFileWriter.close();
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static void createShapeFile(FeatureSchema fs, IShapeGeometry geomSample, FeatureIterator it, File file, long maxNumFeatures, Charset charset) throws Exception {
        block7: {
            shapeType = SHP.getTypeShape(geomSample.getGeometryType());
            dbfFile = SHP.getDbfFile(file);
            dbfName = dbfFile.getAbsolutePath();
            dbfFileWriter = SHP.writeDBFHeader(fs, 0, dbfName, false, false, charset);
            shpFileWrite = SHP.createSHPFileWrite(file);
            shpFileWrite.initialize(shapeType);
            featSchemaGeomType = fs.getGeometryType();
            total = 0;
            try {
                if (featSchemaGeomType != 5 && featSchemaGeomType != 4) ** GOTO lbl28
                while (it.hasNext() && (long)total < maxNumFeatures) {
                    feat = it.next();
                    SHP.writeDBFRow(fs, feat, dbfFileWriter);
                    geom = feat.getGeometry();
                    pathGeom = null;
                    pathGeom = geom instanceof Polygon != false ? ShapeGeometryConverter.jts_to_igeometry((Geometry)SHP.makeGoodSHAPEPolygon((Polygon)geom)) : ShapeGeometryConverter.jts_to_igeometry((Geometry)SHP.makeGoodSHAPEMultiPolygon((MultiPolygon)geom));
                    shpFileWrite.processGeometry(pathGeom);
                    ++total;
                }
                break block7;
lbl-1000:
                // 1 sources

                {
                    feat = it.next();
                    if (feat.getGeometry().isEmpty()) {
                        SHP.LOGGER.info((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.writers.SHP.empty-geometry-skip-it"));
                        continue;
                    }
                    SHP.writeDBFRow(fs, feat, dbfFileWriter);
                    shpFileWrite.processGeometry(ShapeGeometryConverter.jts_to_igeometry(feat.getGeometry()));
                    ++total;
lbl28:
                    // 3 sources

                    ** while (it.hasNext() && (long)total < maxNumFeatures)
                }
lbl29:
                // 1 sources

            }
            finally {
                if (it != null) {
                    it.close();
                }
            }
        }
        shpFileWrite.writeRealHeaders();
        shpFileWrite.close();
        dbfFileWriter.writeRealHeader(total);
        dbfFileWriter.close();
    }

    public static int getTypeShape(int geometryType) {
        if (geometryType >= 512) {
            switch (geometryType - 512) {
                case 1: {
                    return 11;
                }
                case 2: {
                    return 13;
                }
                case 4: {
                    return 15;
                }
                case 64: {
                    return 15;
                }
                case 256: {
                    return 15;
                }
                case 128: {
                    return 13;
                }
                case 32: {
                    return 18;
                }
            }
        } else {
            switch (geometryType) {
                case 1: {
                    return 1;
                }
                case 2: {
                    return 3;
                }
                case 4: {
                    return 5;
                }
                case 64: {
                    return 15;
                }
                case 256: {
                    return 15;
                }
                case 128: {
                    return 13;
                }
                case 32: {
                    return 8;
                }
            }
        }
        return 0;
    }

    private static void checkFiles(File shpFile) throws Exception {
        File dbfFile = SHP.getDbfFile(shpFile);
        File shxFile = SHP.getShxFile(shpFile);
        if (shpFile.exists() && !shpFile.canWrite()) {
            throw new ShapefileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.shape.writers.SHP.file-{0}-is-write-only-and-can-not-be-overwriten", new Object[]{shpFile.getAbsolutePath()}));
        }
        if (shxFile.exists() && !shxFile.canWrite()) {
            throw new ShapefileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.shape.writers.SHP.file-{0}-is-write-only-and-can-not-be-overwriten", new Object[]{shxFile.getAbsolutePath()}));
        }
        if (dbfFile.exists() && !dbfFile.canWrite()) {
            throw new ShapefileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.shape.writers.SHP.file-{0}-is-write-only-and-can-not-be-overwriten", new Object[]{dbfFile.getAbsolutePath()}));
        }
    }

    public static File getDbfFile(File shpFile) throws FileNotFoundException {
        ShapeFileFilter myFileFilter;
        String str = shpFile.getAbsolutePath();
        File directory = shpFile.getParentFile();
        File[] files = directory.listFiles(myFileFilter = new ShapeFileFilter(str));
        if (files == null) {
            throw new FileNotFoundException(I18N.getMessage(SHP.class, "dbf-file-linked-to-shape-file-{0}-was-not-found-in-directory-{1}", new Object[]{str, directory.getAbsolutePath()}));
        }
        String[] ends = new String[]{"dbf", "DBF", "Dbf", "dBf", "DBf", "dbF", "DbF", "dBF"};
        File dbfFile = FileUtil.findEnd(str, files, ends);
        return dbfFile;
    }

    public static File getShxFile(File shpFile) throws IOException {
        ShapeFileFilter myFileFilter;
        String str = shpFile.getAbsolutePath();
        File directory = shpFile.getParentFile();
        File[] files = directory.listFiles(myFileFilter = new ShapeFileFilter(str));
        if (files == null) {
            throw new FileNotFoundException(I18N.getMessage(SHP.class, "shx-file-linked-to-shape-file-{0}-was-not-found-in-directory-{1}", new Object[]{str, directory.getAbsolutePath()}));
        }
        String[] ends = new String[]{"shx", "SHX", "Shx", "sHx", "SHx", "shX", "ShX", "sHX"};
        File shxFile = FileUtil.findEnd(str, files, ends);
        return shxFile;
    }

    public static int getShapeType(int featureSchemaType, boolean is3d) {
        int shapeType = 0;
        switch (featureSchemaType) {
            case 1: {
                if (is3d) {
                    shapeType = 11;
                    break;
                }
                shapeType = 1;
                break;
            }
            case 8: {
                if (is3d) {
                    shapeType = 18;
                    break;
                }
                shapeType = 8;
                break;
            }
            case 2: 
            case 3: {
                if (is3d) {
                    shapeType = 13;
                    break;
                }
                shapeType = 3;
                break;
            }
            case 4: 
            case 5: {
                shapeType = is3d ? 15 : 5;
            }
        }
        return shapeType;
    }

    public static boolean createEmptyShapeFile(File f, FeatureSchema fs, boolean is3d) throws Exception {
        FeatureDataset fd = new FeatureDataset(fs);
        SHP.createShapeFile(fs, SHP.getShapeType(fs.getGeometryType(), is3d), fd.iterator(), f, false, false, true, ShapeFileDataSource.DEFAULT_STRING_CHARSET);
        return true;
    }
}

