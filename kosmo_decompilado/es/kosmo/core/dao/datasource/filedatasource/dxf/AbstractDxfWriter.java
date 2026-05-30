/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package es.kosmo.core.dao.datasource.filedatasource.dxf;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import es.kosmo.core.dao.datasource.filedatasource.dxf.IDxfWriter;
import es.kosmo.core.dao.datasource.filedatasource.dxf.LineType;
import es.kosmo.core.dao.datasource.filedatasource.dxf.LineTypeItem;
import es.kosmo.core.dao.datasource.filedatasource.dxf.utils.DxfCodePage;
import es.kosmo.core.dao.datasource.filedatasource.dxf.utils.FakeWriter;
import es.kosmo.core.dao.datasource.filedatasource.dxf.utils.JulianDate;
import es.kosmo.core.dao.datasource.filedatasource.dxf.utils.XData;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.FeatureIterator;

public abstract class AbstractDxfWriter
implements IDxfWriter {
    private static final Logger LOGGER = Logger.getLogger(AbstractDxfWriter.class);
    protected Writer writer = null;
    protected Writer fakeWriter;
    protected Writer currentWriter;
    protected NumberFormat format = null;
    protected NumberFormat julianDateFormat = null;
    protected NumberFormat ltypeFormat = null;
    protected static final String EOL = "\n";
    public static final Integer BASE_GEOMETRY_HANDLE = 3000000;
    protected static final String LTYPE_HANDLER_TYPE = "LType";
    protected static final String VPORT_HANDLER_TYPE = "VPort";
    protected static final String LAYER_HANDLER_TYPE = "Layer";
    protected static final String APPID_HANDLER_TYPE = "AppId";
    protected static final String BLOCK_RECORD_HANDLER_TYPE = "BlockRecord";
    protected static final String GEOMETRY_HANDLER_TYPE = "Geometry";
    protected String encoding = "ANSI_1252";
    protected GeometryFactory geomFact;
    protected boolean geometryAsBlock = false;
    protected boolean writeFeatureAttrsAsXData = false;
    protected boolean writePointFcsAsInsertsWithAttrs = false;
    protected long limitFeatures = -1L;
    protected int[] defaultColors = new int[]{7, 1, 2, 3, 4, 5, 6, 8, 9};
    protected int defaultColorPos = 0;
    protected int[] colors = new int[]{7, 1, 2, 3, 4, 5, 6, 8, 9};
    protected LineType[] lineTypes = new LineType[]{new LineType("CONTINUOUS", "Solid line")};
    protected int ltypePos = 0;
    protected String[] layerNames = null;
    protected String[] layerBlockNames = null;
    protected int layerCounter = 0;
    protected Map<String, String> cachedLayerNames = new HashMap<String, String>();
    protected Envelope e = null;
    protected Map<String, Integer> handles = new HashMap<String, Integer>();
    protected Map<Object, String> blockNames = null;
    protected Map<Object, String> blockHandles = null;
    protected int blockCounter = 0;
    protected Set<String> protectedFieldNames;
    protected String[] labelAttributeNames;
    protected String[] rotationAttributeNames;
    protected int arraysCursorPos = 0;
    protected long maxHandler;
    protected Integer insUnitsValue;

    public AbstractDxfWriter() {
    }

    public AbstractDxfWriter(Writer writer) {
        this(writer, null);
    }

    public AbstractDxfWriter(Writer writer, String encoding) {
        this.initializeHandles();
        this.writer = writer;
        if (encoding != null) {
            this.encoding = DxfCodePage.toDXFCodePage(encoding);
        }
        this.format = NumberFormat.getInstance(Locale.US);
        this.format.setMaximumFractionDigits(12);
        this.format.setGroupingUsed(false);
        this.format.setMinimumFractionDigits(1);
        this.ltypeFormat = NumberFormat.getInstance(Locale.US);
        this.ltypeFormat.setMaximumFractionDigits(4);
        this.ltypeFormat.setGroupingUsed(false);
        this.ltypeFormat.setMinimumFractionDigits(1);
        this.julianDateFormat = NumberFormat.getInstance(Locale.US);
        this.julianDateFormat.setMaximumFractionDigits(10);
        this.julianDateFormat.setGroupingUsed(false);
        this.julianDateFormat.setMinimumFractionDigits(1);
        this.protectedFieldNames = new HashSet<String>();
        this.protectedFieldNames.add("Color");
        this.protectedFieldNames.add("Elevation");
        this.protectedFieldNames.add("HeightText");
        this.protectedFieldNames.add(LAYER_HANDLER_TYPE);
        this.protectedFieldNames.add("RotationText");
        this.protectedFieldNames.add("Text");
        this.protectedFieldNames.add("Thickness");
        this.geomFact = new GeometryFactory();
    }

    protected void initializeHandles() {
        this.handles.put(LTYPE_HANDLER_TYPE, 22);
        this.handles.put(VPORT_HANDLER_TYPE, 41);
        this.handles.put(LAYER_HANDLER_TYPE, 46);
        this.handles.put(APPID_HANDLER_TYPE, 200);
        this.handles.put(BLOCK_RECORD_HANDLER_TYPE, 300);
        this.handles.put(GEOMETRY_HANDLER_TYPE, BASE_GEOMETRY_HANDLE);
    }

    @Override
    public boolean supportsVersion(String version) {
        if (version == null) {
            return true;
        }
        return this.getVersion().equalsIgnoreCase(version);
    }

    protected Envelope getEnvelope(List<FeatureCollection> fcList) throws Exception {
        if (this.e == null) {
            int i = 0;
            while (i < fcList.size()) {
                FeatureCollection fc = fcList.get(i);
                Envelope fcEnv = fc.getEnvelope();
                if (fcEnv != null) {
                    if (this.e == null) {
                        this.e = new Envelope(fcEnv);
                    } else {
                        this.e.expandToInclude(fcEnv);
                    }
                }
                ++i;
            }
        }
        return this.normalizeEnvelope(this.e);
    }

    private Envelope normalizeEnvelope(Envelope e) {
        if (e != null && e.getWidth() == 0.0 && e.getHeight() == 0.0) {
            e.expandBy(1.0);
        }
        return e;
    }

    protected void writeGroup(int code, String value) throws Exception {
        this.currentWriter.write(String.valueOf(StringUtils.leftPad((String)String.valueOf(code), (int)3)) + EOL);
        this.currentWriter.write(String.valueOf(value) + EOL);
    }

    protected void writeEof() throws Exception {
        this.writeStart("EOF");
    }

    protected void writeStart(String entity) throws Exception {
        this.writeGroup(0, entity);
    }

    protected void writeSectionStart(String name) throws Exception {
        this.writeStart("SECTION");
        this.writeName(name);
    }

    protected void writeSectionEnd() throws Exception {
        this.writeGroup(0, "ENDSEC");
    }

    protected void writeTableStart(String table) throws Exception {
        this.writeGroup(0, "TABLE");
        this.writeName(table);
    }

    protected void writeTableEnd() throws Exception {
        this.writeGroup(0, "ENDTAB");
    }

    protected String writeHandle(String type) throws Exception {
        String handle = this.getNewHandle(type);
        this.writeGroup(5, handle);
        return handle;
    }

    protected String getNewHandle(String type) {
        int currentHandle = this.handles.get(type);
        if (type.equals(GEOMETRY_HANDLER_TYPE) && 995000 < currentHandle && currentHandle < 995999) {
            currentHandle = 996000;
        }
        String handle = Integer.toHexString(currentHandle).toUpperCase();
        this.handles.put(type, ++currentHandle);
        return handle;
    }

    protected String getLayerName(FeatureCollection coll) {
        String name = this.getCachedName(String.valueOf(coll.hashCode()));
        if (name == null) {
            name = this.layerNames[this.layerCounter];
            if (name.equals("")) {
                name = "LAYER" + this.layerCounter;
            }
            ++this.layerCounter;
            this.storeCachedName(String.valueOf(coll.hashCode()), name);
        }
        return name;
    }

    private void storeCachedName(String id, String name) {
        this.cachedLayerNames.put(id, name);
    }

    private String getCachedName(String id) {
        return this.cachedLayerNames.get(id);
    }

    protected int getColor(FeatureCollection coll) {
        if (this.colors != null && this.colors.length - 1 >= this.arraysCursorPos) {
            return this.colors[this.arraysCursorPos];
        }
        int color = this.defaultColors[this.defaultColorPos];
        this.defaultColorPos = this.defaultColorPos < this.defaultColors.length - 1 ? ++this.defaultColorPos : 0;
        return color;
    }

    protected int getLineType(FeatureCollection coll) {
        int ltype = this.ltypePos;
        this.ltypePos = this.ltypePos < this.lineTypes.length - 1 ? ++this.ltypePos : 0;
        return ltype;
    }

    protected void writeLayer(String layer) throws Exception {
        this.writeGroup(8, layer);
    }

    protected void writePath(String path) throws Exception {
        this.writeGroup(1, path);
    }

    protected void writeColor(int color) throws Exception {
        this.writeIntegerGroup(62, color);
    }

    protected void writeLineType(int lineType) throws Exception {
        this.writeGroup(6, this.lineTypes[lineType].getName());
    }

    protected void writeOwnerHandle(String handle) throws Exception {
        this.writeGroup(330, handle);
    }

    protected void writeSubClass(String subclass) throws Exception {
        this.writeGroup(100, subclass);
    }

    protected void writeSize(int size) throws Exception {
        this.writeIntegerGroup(70, size);
    }

    protected void writeName(String name) throws Exception {
        this.writeGroup(2, name);
    }

    protected void writeVariable(String varName) throws Exception {
        this.writeGroup(9, "$" + varName);
    }

    protected void writeIntegerGroup(int code, int value) throws Exception {
        this.writeGroup(code, StringUtils.leftPad((String)String.valueOf(value), (int)6));
    }

    protected void writeFlags(int code, int value) throws Exception {
        this.writeGroup(code, StringUtils.leftPad((String)String.valueOf(value), (int)9));
    }

    protected void writePoint(double x, double y, double z) throws Exception {
        this.writePoint(10, x, y, z);
    }

    protected void writePoint(int baseCode, double x, double y, double z) throws Exception {
        this.writeDoubleGroup(baseCode, x);
        this.writeDoubleGroup(baseCode + 10, y);
        if (!Double.isNaN(z)) {
            this.writeDoubleGroup(baseCode + 20, z);
        }
    }

    protected void writeDoubleGroup(int code, double val) throws Exception {
        this.writeGroup(code, this.format.format(val));
    }

    protected void writeLength(int code, double val) throws Exception {
        this.writeGroup(code, this.ltypeFormat.format(val));
    }

    protected void writeJulianDate(Date dt) throws Exception {
        this.writeGroup(40, this.julianDateFormat.format(JulianDate.toJulian(dt)));
    }

    protected void writeGeometryStart(String geometryName, String layer, String ownerHandle, int lineType, int color) throws Exception {
        this.writeGroup(0, geometryName);
        this.writeHandle(GEOMETRY_HANDLER_TYPE);
        if (ownerHandle != null) {
            this.writeOwnerHandle(ownerHandle);
        }
        this.writeSubClass("AcDbEntity");
        this.writeLayer(layer);
        if (lineType != -1) {
            this.writeLineType(lineType);
        }
        if (color != -1) {
            this.writeColor(7);
        }
    }

    protected void writeGeometryStart(String geometryName, String layer, String ownerHandle) throws Exception {
        this.writeGeometryStart(geometryName, layer, ownerHandle, -1, -1);
    }

    @Override
    public void setOption(String optionName, Object optionValue) {
        if (optionName.equalsIgnoreCase("GEOMETRY_AS_BLOCK")) {
            this.setGeometryAsBlock((Boolean)optionValue);
        } else if ("WRITE_FEATURE_ATTRS_AS_XDATA".equalsIgnoreCase(optionName)) {
            this.setWriteFeatureAttrsAsXData((Boolean)optionValue);
        } else if ("MAX_NUMBER_FEATURES".equalsIgnoreCase(optionName)) {
            this.setMaxNumberFeatures(((Number)optionValue).longValue());
        } else if ("WRITE_POINT_FCS_AS_INSERTS_WITH_ATTRS".equalsIgnoreCase(optionName)) {
            this.setWritePointFcsAsInsertsWithAttrs((Boolean)optionValue);
        } else if ("LAYER_COLORS".equalsIgnoreCase(optionName)) {
            this.setColors((int[])optionValue);
        } else if ("LAYER_LABEL_ATTRIBUTE_NAMES".equalsIgnoreCase(optionName)) {
            this.setLabelAttributes((String[])optionValue);
        } else if ("LAYER_ROTATION_ATTRIBUTE_NAMES".equalsIgnoreCase(optionName)) {
            this.setRotationAttributes((String[])optionValue);
        } else if ("LAYER_BLOCK_NAMES".equalsIgnoreCase(optionName)) {
            this.setLayerBlockNames((String[])optionValue);
        } else if ("DXF_INSUNITS_VALUE".equalsIgnoreCase(optionName)) {
            this.setInsUnitsValue((Integer)optionValue);
        } else {
            LOGGER.warn((Object)("Unsupported option " + optionName));
        }
    }

    public void setLabelAttributes(String[] attrNames) {
        this.labelAttributeNames = attrNames;
    }

    public void setRotationAttributes(String[] attrNames) {
        this.rotationAttributeNames = attrNames;
    }

    public void setMaxNumberFeatures(long limit) {
        this.limitFeatures = limit;
    }

    public void setWriteFeatureAttrsAsXData(boolean write) {
        this.writeFeatureAttrsAsXData = write;
    }

    public void setGeometryAsBlock(boolean geometryAsBlock) {
        this.geometryAsBlock = geometryAsBlock;
    }

    public void setColors(int[] colors) {
        this.colors = colors;
    }

    public void setLineTypes(LineType[] lineTypes) {
        this.lineTypes = lineTypes;
    }

    public void setLayerNames(String[] layerNames) {
        this.layerNames = layerNames;
    }

    public void setLayerBlockNames(String[] blockNames) {
        this.layerBlockNames = blockNames;
    }

    @Override
    public void write(List<FeatureCollection> fcList, String version) throws Exception {
        this.currentWriter = this.fakeWriter = new FakeWriter();
        this.performWriting(fcList, version);
        this.currentWriter = this.writer;
        this.performWriting(fcList, version);
    }

    protected void performWriting(List<FeatureCollection> fcList, String version) throws Exception {
        this.preProcess(fcList);
        this.writeHeader(fcList);
        this.writeClasses(fcList);
        this.writeTables(fcList);
        this.writeBlocks(fcList);
        this.writeEntities(fcList);
        this.writeObjects(fcList);
        this.writeEof();
        this.postProcess();
    }

    @Override
    public void preProcess(List<FeatureCollection> fcList) throws Exception {
        this.setLayerNames(fcList);
    }

    @Override
    public void postProcess() throws IOException {
        this.maxHandler = this.getMaximumHandler();
        this.blockNames.clear();
        this.blockNames = null;
        this.blockHandles.clear();
        this.blockHandles = null;
        this.initializeHandles();
        this.blockCounter = 0;
        this.arraysCursorPos = 0;
        this.layerCounter = 0;
        this.defaultColorPos = 0;
    }

    private int getMaximumHandler() {
        Integer maxValue = -1;
        for (Integer handleValue : this.handles.values()) {
            if (handleValue.compareTo(maxValue) <= 0) continue;
            maxValue = handleValue;
        }
        return maxValue;
    }

    protected void setLayerNames(List<FeatureCollection> fcList) {
        if (this.layerNames == null) {
            this.layerNames = new String[fcList.size()];
            int cont = 0;
            for (FeatureCollection fc : fcList) {
                String name = fc.getName();
                if (StringUtils.isEmpty((String)name)) {
                    name = "LAYER_" + cont;
                }
                this.layerNames[cont] = name;
                ++cont;
            }
        }
    }

    protected void writeHeader(List<FeatureCollection> fcList) throws Exception {
        this.writeGroup(999, "Generated by Kosmo Desktop");
        this.writeGroup(999, "DATE : " + new Date().toString());
        this.writeSectionStart("HEADER");
        this.writeVariables(fcList);
        this.writeSectionEnd();
    }

    protected void writeVariables(List<FeatureCollection> fcList) throws Exception {
        this.writeVariable("ACADVER");
        this.writeGroup(1, this.getVersion());
        this.writeVariable("ACADMAINTVER");
        this.writeIntegerGroup(70, 9);
        this.writeVariable("DWGCODEPAGE");
        this.writeGroup(3, this.encoding);
        this.writeVariable("INSBASE");
        this.writePoint(0.0, 0.0, 0.0);
        Envelope e = this.getEnvelope(fcList);
        if (e != null) {
            this.writeVariable("EXTMIN");
            this.writePoint(e.getMinX(), e.getMinY(), 0.0);
            this.writeVariable("EXTMAX");
            this.writePoint(e.getMaxX(), e.getMaxY(), 0.0);
        }
        this.writeVariable("TDCREATE");
        this.writeJulianDate(new GregorianCalendar().getTime());
        this.writeVariable("TDUPDATE");
        this.writeJulianDate(new GregorianCalendar().getTime());
        if (this.insUnitsValue != null) {
            this.writeVariable("INSUNITS");
            this.writeIntegerGroup(70, this.insUnitsValue);
        }
        this.loadFromResource("header");
    }

    protected abstract String getVersion();

    protected abstract void writeClasses(List<FeatureCollection> var1) throws Exception;

    protected void writeClass(String name, String devname, String description, int flags, boolean proxy, boolean entities) throws Exception {
        this.writeGroup(0, "CLASS");
        this.writeGroup(1, name);
        this.writeGroup(2, devname);
        this.writeGroup(3, description);
        this.writeFlags(90, flags);
        this.writeIntegerGroup(280, proxy ? 1 : 0);
        this.writeIntegerGroup(281, entities ? 1 : 0);
    }

    protected void writeTables(List<FeatureCollection> fcList) throws Exception {
        this.writeSectionStart("TABLES");
        this.writeViewPort(fcList);
        this.writeLineTypes();
        this.writeLayers(fcList);
        this.writeStyles();
        this.writeView();
        this.writeUCS();
        this.writeApplications(fcList);
        this.writeDimensionStyles();
        this.writeBlockRecords(fcList);
        this.writeSectionEnd();
    }

    protected void writeBlocks(List<FeatureCollection> fcList) throws Exception {
        this.writeSectionStart("BLOCKS");
        this.writeModelSpaceBlock();
        this.writePaperSpaceBlock();
        this.writeEntityBlocks(fcList);
        this.writeSectionEnd();
    }

    protected void writeEntities(List<FeatureCollection> fcList) throws Exception {
        this.writeSectionStart("ENTITIES");
        this.arraysCursorPos = 0;
        for (FeatureCollection fc : fcList) {
            this.writeEntity(fc);
            ++this.arraysCursorPos;
        }
        this.arraysCursorPos = 0;
        this.writeSectionEnd();
    }

    protected void writeObjects(List<FeatureCollection> fcList) throws Exception {
        this.loadFromResource("objects");
    }

    /*
     * Unable to fully structure code
     */
    protected void writeEntity(FeatureCollection fc) throws Exception {
        block11: {
            block10: {
                layer = this.getLayerName(fc);
                if (!this.geometryAsBlock) break block10;
                for (String name : this.blockNames.values()) {
                    this.writeInsert(layer, name);
                }
                break block11;
            }
            writeInsertWithAttribs = this.writePointFcsAsInsertsWithAttrs != false && fc.getFeatureSchema().getGeometryType() == 1;
            color = this.getColor(fc);
            itFeats = fc.iterator();
            try {
                cont = 0L;
                if (true) ** GOTO lbl26
                do {
                    f = itFeats.next();
                    pk = this.getFeatureBlockKey(fc.getName(), f.getPrimaryKey());
                    if (this.blockNames.containsKey(pk)) {
                        name = this.blockNames.get(pk);
                        this.writeInsert(layer, name);
                    } else if (writeInsertWithAttribs) {
                        name = this.blockNames.get(fc.getName());
                        this.writeInsertWithAttribs(layer, name, f, color);
                    } else {
                        this.writeGeometry(layer, "1F", f, f.getGeometry());
                    }
lbl26:
                    // 4 sources

                    if (!itFeats.hasNext()) break;
                } while (this.limitFeatures == -1L || cont++ < this.limitFeatures);
            }
            finally {
                if (itFeats != null) {
                    itFeats.close();
                }
            }
        }
    }

    protected void writeInsertWithAttribs(String layer, String name, Feature f, int color) throws Exception {
        this.writeGroup(0, "INSERT");
        String handle = this.writeHandle(GEOMETRY_HANDLER_TYPE);
        this.writeSubClass("AcDbEntity");
        this.writeLayer(layer);
        this.writeSubClass("AcDbBlockReference");
        this.writeGroup(66, "1");
        this.writeName(name);
        Geometry g = f.getGeometry();
        Point p = (Point)g;
        Coordinate c = p.getCoordinate();
        this.writePoint(10, c.x, c.y, c.z);
        this.writeRotation(f);
        this.writeAttribs(layer, name, f, handle, color);
    }

    protected void writeRotation(Feature f) throws Exception {
        Attribute attr;
        if (ArrayUtils.isEmpty((Object[])this.rotationAttributeNames) || this.arraysCursorPos > this.rotationAttributeNames.length - 1) {
            return;
        }
        String attrName = this.rotationAttributeNames[this.arraysCursorPos];
        if (f.getSchema().hasAttribute(attrName) && AttributeType.isNumeric((attr = f.getSchema().getAttribute(attrName)).getType())) {
            Number attrValue = (Number)f.getAttribute(attrName);
            this.writeGroup(50, attrValue.toString());
        }
    }

    protected void writeAttribs(String layerName, String name, Feature f, String ownerHandle, int color) throws Exception {
        FeatureSchema schema = f.getSchema();
        for (String attrName : schema.getAttributeNames()) {
            Attribute attr;
            if (this.protectedFieldNames.contains(attrName) || (attr = schema.getAttribute(attrName)).isPrimaryKey() || attr.getType().equals(AttributeType.GEOMETRY)) continue;
            this.writeAttrib(attr, layerName, f, ownerHandle, color);
        }
        this.writeSeqEnd(layerName, ownerHandle);
    }

    protected void writeAttrib(Attribute attr, String layerName, Feature f, String ownerHandle, int color) throws Exception {
        boolean isLabelAttr = this.isLabelAttribute(attr, this.arraysCursorPos);
        this.writeGroup(0, "ATTRIB");
        this.writeHandle(GEOMETRY_HANDLER_TYPE);
        if (ownerHandle != null) {
            this.writeOwnerHandle(ownerHandle);
        }
        this.writeSubClass("AcDbEntity");
        this.writeLayer(layerName);
        this.writeColor(color);
        this.writeSubClass("AcDbText");
        if (isLabelAttr) {
            Coordinate c = f.getGeometry().getCoordinate();
            this.writePoint(c.x + 0.7, c.y + 0.7, c.z);
        } else {
            this.writePoint(0.0, 0.0, 0.0);
        }
        this.writeGroup(40, "1.5");
        this.writeRotation(f);
        int groupCode = XData.toDXFGroupCode(attr.getType());
        String value = XData.toDXFValue(groupCode, f.getAttribute(attr.getName()));
        this.writeGroup(1, value);
        this.writeSubClass("AcDbAttribute");
        this.writeGroup(2, attr.getName());
        if (isLabelAttr) {
            this.writeGroup(70, "0");
        } else {
            this.writeGroup(70, "1");
        }
    }

    private boolean isLabelAttribute(Attribute attr, int pos) {
        if (ArrayUtils.isEmpty((Object[])this.labelAttributeNames) || pos > this.labelAttributeNames.length - 1) {
            return false;
        }
        return attr.getName().equals(this.labelAttributeNames[pos]);
    }

    protected void writeInsert(String layer, String name) throws Exception {
        this.writeGroup(0, "INSERT");
        this.writeHandle(GEOMETRY_HANDLER_TYPE);
        this.writeSubClass("AcDbEntity");
        this.writeLayer(layer);
        this.writeSubClass("AcDbBlockReference");
        this.writeName(name);
        this.writePoint(0.0, 0.0, 0.0);
    }

    protected void writeEntityBlocks(List<FeatureCollection> fcList) throws Exception {
        this.arraysCursorPos = 0;
        for (FeatureCollection fc : fcList) {
            if (this.writePointFcsAsInsertsWithAttrs && fc.getFeatureSchema().getGeometryType() == 1) {
                this.writeFeatureCollectionBlock(fc);
            } else {
                this.writeFeatureBlocks(fc);
            }
            ++this.arraysCursorPos;
        }
        this.arraysCursorPos = 0;
    }

    protected void writeFeatureCollectionBlock(FeatureCollection fc) throws Exception {
        String ownerHandle = this.blockHandles.get(fc.getName());
        String name = this.blockNames.get(fc.getName());
        String startHandle = this.getNewHandle(GEOMETRY_HANDLER_TYPE);
        String endHandle = this.getNewHandle(GEOMETRY_HANDLER_TYPE);
        String layer = this.getLayerName(fc);
        int color = this.getColor(fc);
        this.writeStartBlock(startHandle, ownerHandle, false, "0", name);
        this.writePointGeometry(layer, ownerHandle, new BasicFeature(fc.getFeatureSchema()), this.geomFact.createPoint(new Coordinate(0.0, 0.0, 0.0)));
        FeatureSchema schema = fc.getFeatureSchema();
        for (String attrName : schema.getAttributeNames()) {
            Attribute attr;
            if (this.protectedFieldNames.contains(attrName) || (attr = schema.getAttribute(attrName)).isPrimaryKey() || attr.getType().equals(AttributeType.GEOMETRY)) continue;
            this.writeAttDef(attr, layer, startHandle, color);
        }
        this.writeEndBlock(endHandle, ownerHandle, false, "0", name);
    }

    protected void writeAttDef(Attribute attr, String layerName, String ownerHandle, int color) throws Exception {
        boolean isLabelAttr = this.isLabelAttribute(attr, this.arraysCursorPos);
        this.writeGroup(0, "ATTDEF");
        this.writeHandle(GEOMETRY_HANDLER_TYPE);
        if (ownerHandle != null) {
            this.writeOwnerHandle(ownerHandle);
        }
        this.writeSubClass("AcDbEntity");
        this.writeLayer(layerName);
        this.writeColor(color);
        this.writeSubClass("AcDbText");
        if (isLabelAttr) {
            this.writePoint(0.0, 0.0, 0.0);
        } else {
            this.writePoint(0.0, 0.0, 0.0);
        }
        this.writeGroup(40, "1.5");
        this.writeGroup(1, "");
        this.writeSubClass("AcDbAttributeDefinition");
        this.writeGroup(3, attr.getPublicName());
        this.writeGroup(2, attr.getName());
        if (isLabelAttr) {
            this.writeGroup(70, "0");
        } else {
            this.writeGroup(70, "1");
        }
    }

    /*
     * Handled impossible loop by duplicating code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected void writeFeatureBlocks(FeatureCollection fc) throws Exception {
        FeatureIterator itFeats = fc.iterator();
        try {
            block8: {
                long cont;
                block7: {
                    cont = 0L;
                    if (!true) break block7;
                    if (!itFeats.hasNext()) return;
                    if (this.limitFeatures != -1L && cont++ >= this.limitFeatures) break block8;
                }
                do {
                    Feature f = itFeats.next();
                    String pk = this.getFeatureBlockKey(fc.getName(), f.getPrimaryKey());
                    if (this.blockNames.containsKey(pk)) {
                        String ownerHandle = this.blockHandles.get(pk);
                        String name = this.blockNames.get(pk);
                        String startHandle = this.getNewHandle(GEOMETRY_HANDLER_TYPE);
                        String endHandle = this.getNewHandle(GEOMETRY_HANDLER_TYPE);
                        String layer = this.getLayerName(fc);
                        this.writeStartBlock(startHandle, ownerHandle, false, "0", name);
                        this.writeGeometry(layer, ownerHandle, f, f.getGeometry());
                        this.writeEndBlock(endHandle, ownerHandle, false, "0", name);
                    }
                    if (!itFeats.hasNext()) return;
                } while (this.limitFeatures == -1L || cont++ < this.limitFeatures);
            }
            return;
        }
        finally {
            if (itFeats != null) {
                itFeats.close();
            }
        }
    }

    protected void writeGeometry(String layer, String ownerHandle, Feature f, Geometry geom) throws Exception {
        if (geom instanceof GeometryCollection) {
            GeometryCollection coll = (GeometryCollection)geom;
            int count = 0;
            while (count < coll.getNumGeometries()) {
                this.writeGeometry(layer, ownerHandle, f, coll.getGeometryN(count));
                ++count;
            }
        } else if (geom instanceof Polygon) {
            Polygon p = (Polygon)geom;
            LineString l = p.getExteriorRing();
            Coordinate[] coords = l.getCoordinates();
            this.writePolylineGeometry(layer, ownerHandle, f, coords, true);
            int count = 0;
            while (count < p.getNumInteriorRing()) {
                l = p.getInteriorRingN(count);
                coords = l.getCoordinates();
                this.writePolylineGeometry(layer, ownerHandle, f, coords, true);
                ++count;
            }
        } else if (geom instanceof LineString) {
            LineString l = (LineString)geom;
            Coordinate[] coords = l.getCoordinates();
            this.writePolylineGeometry(layer, ownerHandle, f, coords, false);
        } else if (geom instanceof Point) {
            Point p = (Point)geom;
            this.writePointGeometry(layer, ownerHandle, f, p);
        }
    }

    protected void writePointGeometry(String layer, String ownerHandle, Feature f, Point p) throws Exception {
        this.writeGeometryStart("POINT", layer, ownerHandle);
        this.writeSubClass("AcDbPoint");
        this.writePoint(p.getX(), p.getY(), p.getCoordinate().z);
        if (this.writeFeatureAttrsAsXData) {
            this.writeXData(layer, f);
        }
    }

    protected void write3dVertex(String layer, String ownerHandle, double x, double y, double z) throws Exception {
        this.writeGeometryStart("VERTEX", layer, ownerHandle);
        this.writeSubClass("AcDbVertex");
        this.writeSubClass("AcDb3dPolylineVertex");
        this.writePoint(x, y, z);
        this.writeIntegerGroup(70, 32);
    }

    protected void writePolylineGeometry(String layer, String ownerHandle, Feature f, Coordinate[] coords, boolean closed) throws Exception {
        if (this.constantElevation(coords)) {
            this.writeGeometryStart("LWPOLYLINE", layer, ownerHandle);
            this.writeSubClass("AcDbPolyline");
            this.writeIntegerGroup(90, coords.length);
            this.writeDoubleGroup(43, 0.0);
            if (closed) {
                this.writeIntegerGroup(70, 1);
            }
            if (!Double.isNaN(coords[0].z)) {
                this.writeDoubleGroup(38, coords[0].z);
            }
            Coordinate[] coordinateArray = coords;
            int n = coords.length;
            int n2 = 0;
            while (n2 < n) {
                Coordinate coord = coordinateArray[n2];
                this.writePoint(coord.x, coord.y, Double.NaN);
                ++n2;
            }
            if (this.writeFeatureAttrsAsXData) {
                this.writeXData(layer, f);
            }
        } else {
            this.writeGeometryStart("POLYLINE", layer, ownerHandle);
            this.writeSubClass("AcDb3dPolyline");
            this.writePoint(0.0, 0.0, 0.0);
            if (closed) {
                this.writeIntegerGroup(70, 9);
            } else {
                this.writeIntegerGroup(70, 8);
            }
            if (this.writeFeatureAttrsAsXData) {
                this.writeXData(layer, f);
            }
            Coordinate[] coordinateArray = coords;
            int n = coords.length;
            int n3 = 0;
            while (n3 < n) {
                Coordinate coord = coordinateArray[n3];
                this.write3dVertex(layer, ownerHandle, coord.x, coord.y, coord.z);
                ++n3;
            }
            this.writeSeqEnd(layer, ownerHandle);
        }
    }

    protected void writeSeqEnd(String layerName, String ownerHandle) throws Exception {
        this.writeGroup(0, "SEQEND");
        this.writeHandle(GEOMETRY_HANDLER_TYPE);
        if (ownerHandle != null) {
            this.writeOwnerHandle(ownerHandle);
        }
        this.writeSubClass("AcDbEntity");
        this.writeLayer(layerName);
    }

    protected void writeModelSpaceBlock() throws Exception {
        this.writeStartBlock("20", "1F", false, "0", "*MODEL_SPACE");
        this.writeEndBlock("21", "1F", false, "0", "*MODEL_SPACE");
    }

    protected void writePaperSpaceBlock() throws Exception {
        this.writeStartBlock("1C", "1B", true, "0", "*PAPER_SPACE");
        this.writeEndBlock("1D", "1B", true, "0", "*MODEL_SPACE");
    }

    protected void writeStartBlock(String handle, String ownerHandle, boolean paperSpace, String layer, String name) throws Exception {
        this.writeGroup(0, "BLOCK");
        this.writeGroup(5, handle);
        this.writeOwnerHandle(ownerHandle);
        this.writeSubClass("AcDbEntity");
        if (paperSpace) {
            this.writeIntegerGroup(67, 1);
        }
        this.writeLayer(layer);
        this.writeSubClass("AcDbBlockBegin");
        this.writeName(name);
        this.writeIntegerGroup(70, 0);
        this.writeIntegerGroup(71, 0);
        this.writePoint(0.0, 0.0, 0.0);
        this.writeGroup(3, name);
        this.writePath("");
    }

    protected void writeEndBlock(String handle, String ownerHandle, boolean paperSpace, String layer, String name) throws Exception {
        this.writeGroup(0, "ENDBLK");
        this.writeGroup(5, handle);
        this.writeOwnerHandle(ownerHandle);
        this.writeSubClass("AcDbEntity");
        if (paperSpace) {
            this.writeIntegerGroup(67, 1);
        }
        this.writeLayer(layer);
        this.writeSubClass("AcDbBlockEnd");
    }

    protected void writeBlockRecords(List<FeatureCollection> fcList) throws Exception {
        this.writeTableStart("BLOCK_RECORD");
        this.writeGroup(5, "1");
        this.writeOwnerHandle("0");
        this.writeSubClass("AcDbSymbolTable");
        this.writeSize(2 + this.countBlocks(fcList));
        this.writeModelSpaceBlockRecord();
        this.writePaperSpaceBlockRecord();
        if (this.blockNames != null) {
            for (Object pk : this.blockNames.keySet()) {
                this.blockHandles.put(pk, this.writeBlockRecord(this.blockNames.get(pk)));
            }
        }
        this.writeTableEnd();
    }

    protected String writeBlockRecord(String blockName) throws Exception {
        String handle = this.getNewHandle(BLOCK_RECORD_HANDLER_TYPE);
        this.writeBlockRecord(handle, "1", blockName);
        return handle;
    }

    protected void writeModelSpaceBlockRecord() throws Exception {
        this.writeBlockRecord("1F", "1", "*MODEL_SPACE");
    }

    protected void writePaperSpaceBlockRecord() throws Exception {
        this.writeBlockRecord("1B", "1", "*PAPER_SPACE");
    }

    protected void writeBlockRecord(String handle, String ownerHandle, String name) throws Exception {
        this.writeGroup(0, "BLOCK_RECORD");
        this.writeGroup(5, handle);
        this.writeOwnerHandle(ownerHandle);
        this.writeSubClass("AcDbSymbolTableRecord");
        this.writeSubClass("AcDbBlockTableRecord");
        this.writeName(name);
    }

    protected int countBlocks(List<FeatureCollection> fcList) throws Exception {
        if (this.blockNames == null) {
            this.blockNames = new HashMap<Object, String>();
            this.blockHandles = new HashMap<Object, String>();
            this.arraysCursorPos = 0;
            for (FeatureCollection fc : fcList) {
                this.addBlocks(fc);
                ++this.arraysCursorPos;
            }
            this.arraysCursorPos = 0;
        }
        return this.blockNames.size();
    }

    /*
     * Handled impossible loop by duplicating code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected void addBlocks(FeatureCollection fc) throws Exception {
        if (this.writePointFcsAsInsertsWithAttrs && fc.getFeatureSchema().getGeometryType() == 1) {
            this.blockNames.put(fc.getName(), this.getLayerBlockName(fc));
            return;
        }
        FeatureIterator itFeats = fc.iterator();
        try {
            block8: {
                long cont;
                block7: {
                    cont = 0L;
                    if (!true) break block7;
                    if (!itFeats.hasNext()) return;
                    if (this.limitFeatures != -1L && cont++ >= this.limitFeatures) break block8;
                }
                do {
                    Feature f = itFeats.next();
                    Geometry geom = f.getGeometry();
                    if (this.geometryAsBlock || this.isBlockGeometry(geom)) {
                        this.blockNames.put(this.getFeatureBlockKey(fc.getName(), f.getPrimaryKey()), String.valueOf(this.blockCounter++));
                    }
                    if (!itFeats.hasNext()) return;
                } while (this.limitFeatures == -1L || cont++ < this.limitFeatures);
            }
            return;
        }
        finally {
            itFeats.close();
        }
    }

    private String getFeatureBlockKey(String fcName, Object primaryKey) {
        return String.valueOf(fcName) + " % " + primaryKey;
    }

    protected String getLayerBlockName(FeatureCollection fc) {
        if (this.layerBlockNames != null && this.layerBlockNames.length - 1 >= this.arraysCursorPos && this.layerBlockNames[this.arraysCursorPos] != null) {
            return this.layerBlockNames[this.arraysCursorPos];
        }
        return fc.getName();
    }

    protected boolean isBlockGeometry(Geometry geom) {
        if (geom != null) {
            if (GeometryCollection.class.isAssignableFrom(geom.getClass())) {
                return true;
            }
            if (Polygon.class.isAssignableFrom(geom.getClass())) {
                return ((Polygon)geom).getNumInteriorRing() > 0;
            }
        }
        return false;
    }

    protected void writeDimensionStyles() throws Exception {
        this.writeTableStart("DIMSTYLE");
        this.writeGroup(5, "A");
        this.writeOwnerHandle("0");
        this.writeSubClass("AcDbSymbolTable");
        this.writeSize(1);
        this.writeDimensionStyle("27", "A", "11", "STANDARD", 0);
        this.writeTableEnd();
    }

    protected void writeDimensionStyle(String handle, String ownerHandle, String styleHandle, String name, int flags) throws Exception {
        this.writeGroup(0, "DIMSTYLE");
        this.writeGroup(105, handle);
        this.writeOwnerHandle(ownerHandle);
        this.writeSubClass("AcDbSymbolTableRecord");
        this.writeSubClass("AcDbDimStyleTableRecord");
        this.writeName(name);
        this.writeIntegerGroup(70, flags);
        this.writeGroup(3, "");
        this.writeGroup(4, "");
        this.writeGroup(5, "");
        this.writeGroup(6, "");
        this.writeGroup(7, "");
        this.writeDoubleGroup(40, 1.0);
        this.writeDoubleGroup(41, 0.18);
        this.writeLength(42, 0.0625);
        this.writeDoubleGroup(43, 0.38);
        this.writeDoubleGroup(44, 0.18);
        this.writeDoubleGroup(45, 0.0);
        this.writeDoubleGroup(46, 0.0);
        this.writeDoubleGroup(47, 0.0);
        this.writeDoubleGroup(48, 0.0);
        this.writeDoubleGroup(140, 0.18);
        this.writeDoubleGroup(141, 0.09);
        this.writeDoubleGroup(142, 0.0);
        this.writeDoubleGroup(143, 25.4);
        this.writeDoubleGroup(144, 1.0);
        this.writeDoubleGroup(145, 0.0);
        this.writeDoubleGroup(146, 1.0);
        this.writeDoubleGroup(147, 0.09);
        this.writeIntegerGroup(71, 0);
        this.writeIntegerGroup(72, 0);
        this.writeIntegerGroup(73, 1);
        this.writeIntegerGroup(74, 1);
        this.writeIntegerGroup(75, 0);
        this.writeIntegerGroup(76, 0);
        this.writeIntegerGroup(77, 0);
        this.writeIntegerGroup(78, 0);
        this.writeIntegerGroup(170, 0);
        this.writeIntegerGroup(171, 2);
        this.writeIntegerGroup(172, 0);
        this.writeIntegerGroup(173, 0);
        this.writeIntegerGroup(174, 0);
        this.writeIntegerGroup(175, 0);
        this.writeIntegerGroup(176, 0);
        this.writeIntegerGroup(177, 0);
        this.writeIntegerGroup(178, 0);
        this.writeIntegerGroup(270, 2);
        this.writeIntegerGroup(271, 4);
        this.writeIntegerGroup(272, 4);
        this.writeIntegerGroup(273, 2);
        this.writeIntegerGroup(274, 2);
        this.writeGroup(340, styleHandle);
        this.writeIntegerGroup(275, 0);
        this.writeIntegerGroup(280, 0);
        this.writeIntegerGroup(281, 0);
        this.writeIntegerGroup(282, 0);
        this.writeIntegerGroup(283, 1);
        this.writeIntegerGroup(284, 0);
        this.writeIntegerGroup(285, 0);
        this.writeIntegerGroup(286, 0);
        this.writeIntegerGroup(287, 3);
        this.writeIntegerGroup(288, 0);
    }

    protected void writeView() throws Exception {
        this.writeTableStart("VIEW");
        this.writeGroup(5, "6");
        this.writeOwnerHandle("0");
        this.writeSubClass("AcDbSymbolTable");
        this.writeSize(0);
        this.writeTableEnd();
    }

    protected void writeLayers(List<FeatureCollection> fcList) throws Exception {
        this.writeTableStart("LAYER");
        this.writeGroup(5, "2");
        this.writeOwnerHandle("0");
        this.writeSubClass("AcDbSymbolTable");
        this.writeSize(fcList.size() + 1);
        this.writeBgLayer();
        this.arraysCursorPos = 0;
        for (FeatureCollection fc : fcList) {
            this.writeLayer(fc);
            ++this.arraysCursorPos;
        }
        this.arraysCursorPos = 0;
        this.writeTableEnd();
    }

    protected void writeLayerItem(String handle, String ownerHandle, String name, boolean frozen, int color, int ltype) throws Exception {
        this.writeGroup(0, "LAYER");
        this.writeGroup(5, handle);
        this.writeOwnerHandle(ownerHandle);
        this.writeSubClass("AcDbSymbolTableRecord");
        this.writeSubClass("AcDbLayerTableRecord");
        this.writeName(name);
        this.writeIntegerGroup(70, frozen ? 2 : 0);
        if (color != -1) {
            this.writeColor(color);
        }
        if (ltype != -1) {
            this.writeLineType(ltype);
        }
    }

    protected void writeBgLayer() throws Exception {
        this.writeLayerItem("10", "2", "0", false, 7, 0);
    }

    protected void writeLayer(FeatureCollection fc) throws Exception {
        this.writeLayerItem(this.getNewHandle(LAYER_HANDLER_TYPE), "2", this.getLayerName(fc), false, this.getColor(fc), this.getLineType(fc));
    }

    protected void writeLineTypes() throws Exception {
        this.writeTableStart("LTYPE");
        this.writeGroup(5, "5");
        this.writeOwnerHandle("0");
        this.writeSubClass("AcDbSymbolTable");
        this.writeSize(this.lineTypes.length);
        this.writeLineType("14", "5", "BYBLOCK", "", 0.0, new LineTypeItem[0]);
        this.writeLineType("15", "5", "BYLAYER", "", 0.0, new LineTypeItem[0]);
        LineType[] lineTypeArray = this.lineTypes;
        int n = this.lineTypes.length;
        int n2 = 0;
        while (n2 < n) {
            LineType ltype = lineTypeArray[n2];
            this.writeLineType(this.getNewHandle(LTYPE_HANDLER_TYPE), "5", ltype.getName(), ltype.getDescription(), ltype.getLength(), ltype.getItems());
            ++n2;
        }
        this.writeTableEnd();
    }

    protected void writeLineType(String handle, String ownerHandle, String name, String description, double length, LineTypeItem[] items) throws Exception {
        this.writeGroup(0, "LTYPE");
        this.writeGroup(5, handle);
        this.writeOwnerHandle(ownerHandle);
        this.writeSubClass("AcDbSymbolTableRecord");
        this.writeSubClass("AcDbLinetypeTableRecord");
        this.writeName(name);
        this.writeIntegerGroup(70, 0);
        this.writeGroup(3, description);
        this.writeIntegerGroup(72, 65);
        this.writeIntegerGroup(73, items.length);
        this.writeLength(40, length);
        LineTypeItem[] lineTypeItemArray = items;
        int n = items.length;
        int n2 = 0;
        while (n2 < n) {
            LineTypeItem item = lineTypeItemArray[n2];
            this.writeLength(49, item.getLength());
            this.writeIntegerGroup(74, 0);
            ++n2;
        }
    }

    protected void writeApplications(List<FeatureCollection> fcList) throws Exception {
        this.writeTableStart("APPID");
        this.writeGroup(5, "9");
        this.writeOwnerHandle("0");
        this.writeSubClass("AcDbSymbolTable");
        this.writeSize(1);
        this.writeApplication("12", "9", "ACAD");
        if (this.writeFeatureAttrsAsXData) {
            HashSet<String> writtenAttrs = new HashSet<String>();
            for (FeatureCollection fc : fcList) {
                FeatureSchema schema = fc.getFeatureSchema();
                for (String attrName : schema.getAttributeNames()) {
                    Attribute attr;
                    if (this.protectedFieldNames.contains(attrName) || (attr = schema.getAttribute(attrName)).isPrimaryKey() || attr.getType().equals(AttributeType.GEOMETRY) || writtenAttrs.contains(attrName)) continue;
                    this.writeApplication(this.getNewHandle(APPID_HANDLER_TYPE), "9", attrName);
                    writtenAttrs.add(attrName);
                }
            }
        }
        this.writeTableEnd();
    }

    protected void writeApplication(String handle, String ownerHandle, String name) throws Exception {
        this.writeGroup(0, "APPID");
        this.writeGroup(5, handle);
        this.writeOwnerHandle(ownerHandle);
        this.writeSubClass("AcDbSymbolTableRecord");
        this.writeSubClass("AcDbRegAppTableRecord");
        this.writeName(name);
        this.writeIntegerGroup(70, 0);
    }

    protected void writeUCS() throws Exception {
        this.writeTableStart("UCS");
        this.writeGroup(5, "7");
        this.writeOwnerHandle("0");
        this.writeSubClass("AcDbSymbolTable");
        this.writeSize(0);
        this.writeTableEnd();
    }

    protected void writeStyles() throws Exception {
        this.writeTableStart("STYLE");
        this.writeGroup(5, "3");
        this.writeOwnerHandle("0");
        this.writeSubClass("AcDbSymbolTable");
        this.writeSize(1);
        this.writeStyleItem("11", "3", "STANDARD", 0);
        this.writeTableEnd();
    }

    protected void writeStyleItem(String handle, String ownerHandle, String name, int flags) throws Exception {
        this.writeGroup(0, "STYLE");
        this.writeGroup(5, handle);
        this.writeOwnerHandle(ownerHandle);
        this.writeSubClass("AcDbSymbolTableRecord");
        this.writeSubClass("AcDbTextStyleTableRecord");
        this.writeName(name);
        this.writeIntegerGroup(70, flags);
        this.writeDoubleGroup(40, 0.0);
        this.writeDoubleGroup(41, 1.0);
        this.writeDoubleGroup(50, 0.0);
        this.writeIntegerGroup(71, 0);
        this.writeDoubleGroup(42, 0.2);
        this.writeGroup(3, "txt");
        this.writeGroup(4, "");
    }

    protected void writeViewPort(List<FeatureCollection> fcList) throws Exception {
        this.writeTableStart("VPORT");
        this.writeGroup(5, "8");
        this.writeOwnerHandle("0");
        this.writeSubClass("AcDbSymbolTable");
        this.writeSize(1);
        this.writeViewPortItem("*ACTIVE", fcList);
        this.writeTableEnd();
    }

    protected void writeViewPortItem(String name, List<FeatureCollection> fcList) throws Exception {
        this.writeGroup(0, "VPORT");
        this.writeHandle(VPORT_HANDLER_TYPE);
        this.writeSubClass("AcDbSymbolTableRecord");
        this.writeSubClass("AcDbViewportTableRecord");
        this.writeName(name);
        this.writeIntegerGroup(70, 0);
        this.writePoint(10, 0.0, 0.0, Double.NaN);
        this.writePoint(11, 1.0, 1.0, Double.NaN);
        Envelope env = this.getEnvelope(fcList);
        this.writePoint(12, env.centre().x, env.centre().y, Double.NaN);
        this.writePoint(13, 0.0, 0.0, Double.NaN);
        this.writePoint(14, 0.5, 0.5, Double.NaN);
        this.writePoint(15, 0.5, 0.5, Double.NaN);
        this.writePoint(16, 0.0, 0.0, 1.0);
        this.writePoint(17, 0.0, 0.0, 0.0);
        this.writeDoubleGroup(40, env.getHeight());
        this.writeDoubleGroup(41, env.getWidth() / env.getHeight());
        this.writeDoubleGroup(42, 50.0);
        this.writeDoubleGroup(43, 0.0);
        this.writeDoubleGroup(44, 0.0);
        this.writeDoubleGroup(50, 0.0);
        this.writeDoubleGroup(51, 0.0);
        this.writeIntegerGroup(71, 0);
        this.writeIntegerGroup(72, 100);
        this.writeIntegerGroup(73, 1);
        this.writeIntegerGroup(74, 3);
        this.writeIntegerGroup(75, 0);
        this.writeIntegerGroup(76, 0);
        this.writeIntegerGroup(77, 0);
        this.writeIntegerGroup(78, 0);
    }

    protected void loadFromResource(String resource) throws IOException {
        InputStream tpl = AbstractDxfWriter.class.getResourceAsStream(String.valueOf(resource) + ".dxf");
        if (tpl != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(tpl));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if ("%MAX_HANDLER_VALUE%".equals(line)) {
                    line = Long.toHexString(this.maxHandler).toUpperCase();
                }
                this.currentWriter.write(String.valueOf(line) + EOL);
            }
            reader.close();
        }
    }

    protected boolean constantElevation(Coordinate[] coords) {
        boolean constant = true;
        int i = 0;
        while (i < coords.length) {
            if (!(Double.isNaN(coords[0].z) && Double.isNaN(coords[i].z) || coords[0].z == coords[i].z)) {
                constant = false;
                break;
            }
            ++i;
        }
        return constant;
    }

    protected void writeXData(String layerName, Feature feat) throws Exception {
        FeatureSchema schema = feat.getSchema();
        for (String attrName : schema.getAttributeNames()) {
            Attribute attr;
            if (this.protectedFieldNames.contains(attrName) || (attr = schema.getAttribute(attrName)).isPrimaryKey() || attr.getType().equals(AttributeType.GEOMETRY)) continue;
            int groupCode = XData.toDXFGroupCode(attr.getType());
            this.writeGroup(1001, attrName);
            this.writeGroup(groupCode, XData.toDXFValue(groupCode, feat.getAttribute(attrName)));
        }
    }

    public void setWritePointFcsAsInsertsWithAttrs(boolean writePointFcsAsInsertsWithAttrs) {
        this.writePointFcsAsInsertsWithAttrs = writePointFcsAsInsertsWithAttrs;
    }

    public void setInsUnitsValue(Integer value) {
        this.insUnitsValue = value;
    }
}

