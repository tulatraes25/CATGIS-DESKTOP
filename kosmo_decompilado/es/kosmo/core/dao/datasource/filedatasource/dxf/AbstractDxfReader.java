/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateSequence
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.geom.impl.CoordinateArraySequence
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.cresques.geo.Point3D
 *  org.cresques.px.dxf.DxfCalXtru
 *  org.cresques.px.dxf.DxfLayer
 *  org.cresques.px.dxf.DxfTable
 *  org.cresques.px.dxf.DxfTableItem
 */
package es.kosmo.core.dao.datasource.filedatasource.dxf;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import es.kosmo.core.dao.datasource.filedatasource.dxf.IDxfReader;
import es.kosmo.core.dao.datasource.filedatasource.dxf.utils.DxfBlock;
import es.kosmo.core.dao.datasource.filedatasource.dxf.utils.DxfVersion;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cresques.geo.Point3D;
import org.cresques.io.DxfGroup;
import org.cresques.io.DxfGroupVector;
import org.cresques.px.dxf.DxfCalArcs;
import org.cresques.px.dxf.DxfCalXtru;
import org.cresques.px.dxf.DxfConvTexts;
import org.cresques.px.dxf.DxfLayer;
import org.cresques.px.dxf.DxfTable;
import org.cresques.px.dxf.DxfTableItem;
import org.saig.core.geometry.Arc;
import org.saig.core.geometry.Circle;
import org.saig.core.geometry.Ellipse;
import org.saig.core.geometry.ExtendedGeometryFactory;
import org.saig.core.model.feature.Attribute;
import org.saig.jump.plugin.datasource.DataSourceUtil;

public abstract class AbstractDxfReader
implements IDxfReader {
    private static final Logger LOGGER = Logger.getLogger(AbstractDxfReader.class);
    protected Reader reader;
    protected String encoding;
    protected BufferedReader br;
    protected DxfGroup grp;
    protected long currentLineNumber;
    protected String acadVersion;
    protected double maxZFromHeader;
    protected double minZFromHeader;
    protected DxfTable layers;
    protected Hashtable<String, Long> unknownBlockEntitiesTable;
    protected Hashtable<String, Long> unknownEntitiesTable;
    protected TreeMap<String, FeatureDataset> datasets;
    protected FeatureSchema baseSchema;
    protected ExtendedGeometryFactory geomFact;
    protected Feature tempFeatBorder;
    protected List<Coordinate> tempFeatBorderCoordList;
    protected Feature tempFeatBackground;
    protected List<Coordinate> tempFeatBackgroundCoordList;
    protected boolean isDoubleFeatured;
    protected boolean constantPolylineElevation;
    protected double lastVertexElevation;
    protected double xtruX;
    protected double xtruY;
    protected double xtruZ;
    protected Coordinate firstCoord;
    protected Coordinate lastCoord;
    protected double bulge;
    protected Vector<int[]> faces;
    protected boolean hasFaces = false;
    protected int facesIterador = 1;
    protected Coordinate facesFirstCoord = null;
    protected Map<String, DxfBlock> blocks;
    protected boolean addingToBlock = false;
    protected DxfBlock currentBlock;
    protected Hashtable<String, Set<Integer>> layerToColor;
    protected boolean generateOneFcByLayer = false;
    protected boolean schemaAttributeNamesToUpperCase = false;
    protected boolean ignoreInsertEntities = false;
    protected boolean classifyFcsByGeometryType = false;
    protected boolean ignoreEmptyLayers = false;
    protected String outputPointFcSuffix = "_point";
    protected String outputLineFcSuffix = "_line";
    protected String outputPolygonFcSuffix = "_polygon";
    protected String[] bannedLayerNames;

    public AbstractDxfReader() {
    }

    public AbstractDxfReader(Reader reader) {
        this(reader, null);
    }

    public AbstractDxfReader(Reader reader, String encoding) {
        this.reader = reader;
        this.encoding = encoding;
    }

    protected void preProcess() {
        this.layers = new DxfTable();
        this.unknownBlockEntitiesTable = new Hashtable();
        this.unknownEntitiesTable = new Hashtable();
        this.datasets = new TreeMap();
        this.baseSchema = this.generateBaseFeatureSchema();
        if (!this.generateOneFcByLayer) {
            FeatureDataset basicDataset = new FeatureDataset(this.baseSchema);
            this.datasets.put("", basicDataset);
        }
        this.geomFact = new ExtendedGeometryFactory();
        this.blocks = new HashMap<String, DxfBlock>();
        this.layerToColor = new Hashtable();
    }

    protected void postProcess() {
        if (!ArrayUtils.isEmpty((Object[])this.bannedLayerNames)) {
            String[] stringArray = this.bannedLayerNames;
            int n = this.bannedLayerNames.length;
            int n2 = 0;
            while (n2 < n) {
                String bannedLayerName = stringArray[n2];
                this.datasets.remove(bannedLayerName);
                ++n2;
            }
        }
    }

    @Override
    public void load() throws Exception {
        this.preProcess();
        this.br = new BufferedReader(this.reader);
        this.currentLineNumber = 0L;
        while ((this.grp = this.readGrp()) != null) {
            this.currentLineNumber += 2L;
            if (this.grp.equals(0, "EOF")) break;
            if (!this.grp.equals(0, "SECTION")) continue;
            this.readSection();
        }
        this.postProcess();
        this.reader.close();
        this.reader = null;
    }

    protected DxfGroup readGrp() throws NumberFormatException, IOException {
        DxfGroup g = DxfGroup.read(this.br);
        if (g != null) {
            this.currentLineNumber += 2L;
        }
        return g;
    }

    protected void readSection() throws Exception {
        do {
            this.grp = this.readGrp();
            if (this.grp.getCode() != 2) continue;
            String grpData = (String)this.grp.getData();
            if ("HEADER".equalsIgnoreCase(grpData)) {
                LOGGER.info((Object)("Reading HEADER section (Line " + this.currentLineNumber + ")..."));
                this.readHeader();
                continue;
            }
            if ("CLASSES".equalsIgnoreCase(grpData)) {
                LOGGER.info((Object)("Reading CLASSES section (Line " + this.currentLineNumber + ")..."));
                this.readAnySection();
                continue;
            }
            if ("TABLES".equalsIgnoreCase(grpData)) {
                LOGGER.info((Object)("Reading TABLES section (Line " + this.currentLineNumber + ")..."));
                this.readTables();
                continue;
            }
            if ("BLOCKS".equalsIgnoreCase(grpData)) {
                LOGGER.info((Object)("Reading BLOCKS section (Line " + this.currentLineNumber + ")..."));
                this.readBlocks();
                continue;
            }
            if ("ENTITIES".equalsIgnoreCase(grpData)) {
                LOGGER.info((Object)("Reading ENTITIES section(Line " + this.currentLineNumber + ")..."));
                this.readEntities();
                continue;
            }
            if ("OBJECTS".equalsIgnoreCase(grpData)) {
                LOGGER.info((Object)("Reading OBJECTS section(Line " + this.currentLineNumber + ")..."));
                this.readAnySection();
                continue;
            }
            LOGGER.warn((Object)("Unknow section: " + grpData));
            this.readAnySection();
        } while (!this.grp.equals(0, "EOF") && !this.grp.equals(0, "ENDSEC"));
    }

    protected void readHeader() throws Exception {
        DxfGroupVector v = new DxfGroupVector();
        this.grp = this.readGrp();
        block0: while (!this.grp.equals(0, "EOF")) {
            if (this.grp.getCode() != 9 && this.grp.getCode() != 0) continue;
            if (v.size() > 0) {
                String lastVariable = (String)((DxfGroup)v.get(0)).getData();
                if ("$ACADVER".equalsIgnoreCase(lastVariable)) {
                    this.setAcadVersion(v);
                } else if (lastVariable.compareTo("$EXTMIN") == 0) {
                    if (v.hasCode(3)) {
                        this.setMinZFromHeader((Double)((DxfGroup)v.get(3)).getData());
                    }
                } else if (lastVariable.compareTo("$EXTMAX") == 0) {
                    if (v.hasCode(3)) {
                        this.setMaxZFromHeader((Double)((DxfGroup)v.get(3)).getData());
                    }
                } else if ("ENDSEC".equalsIgnoreCase(lastVariable)) break;
            }
            v.clear();
            v.add(this.grp);
            while (true) {
                this.grp = this.readGrp();
                if (this.grp.getCode() == 9 || this.grp.getCode() == 0) continue block0;
                v.add(this.grp);
            }
        }
    }

    protected void setMinZFromHeader(double value) {
        this.minZFromHeader = value;
    }

    protected void setMaxZFromHeader(double value) {
        this.maxZFromHeader = value;
    }

    protected void readTables() throws Exception {
        int layerCnt = 0;
        String tableAct = "NONAME";
        Hashtable tables = new Hashtable();
        Vector<DxfGroupVector> table = new Vector<DxfGroupVector>();
        DxfGroupVector v = new DxfGroupVector();
        this.grp = this.readGrp();
        while (true) {
            if (this.grp.getCode() == 0) {
                String data = (String)this.grp.getData();
                if ("ENDSEC".equalsIgnoreCase(data) || "EOF".equalsIgnoreCase(data)) break;
                if ("ENDTAB".equalsIgnoreCase(data)) {
                    tables.put(tableAct, table);
                    table = new Vector();
                    this.grp = this.readGrp();
                    if (!"LAYER".equalsIgnoreCase(tableAct) || v.size() <= 0) continue;
                    this.createLayer(v);
                    LOGGER.info((Object)("Added layer " + v.getDataAsString(2)));
                    ++layerCnt;
                    v.clear();
                    continue;
                }
                if (table.size() == 1) {
                    tableAct = v.getDataAsString(2);
                } else if ("LAYER".equalsIgnoreCase(tableAct) && v.size() > 0) {
                    this.createLayer(v);
                    LOGGER.info((Object)("Added layer " + v.getDataAsString(2)));
                    ++layerCnt;
                }
                v.clear();
                v.add(this.grp);
                while (true) {
                    this.grp = this.readGrp();
                    if (this.grp.getCode() == 0) break;
                    v.add(this.grp);
                }
                table.add(v);
                continue;
            }
            LOGGER.warn((Object)"DXF sequence error");
            this.grp = this.readGrp();
        }
        LOGGER.info((Object)("End of TABLES section: " + layerCnt + " layers read"));
    }

    protected void readBlocks() throws Exception {
        int blkCnt = 0;
        int unknownEntityCnt = 0;
        DxfGroupVector v = new DxfGroupVector();
        this.grp = this.readGrp();
        while (!this.grp.equals(0, "EOF")) {
            if (this.grp.getCode() != 0) continue;
            if (v.size() > 0) {
                String lastEntity = (String)((DxfGroup)v.get(0)).getData();
                if ("BLOCK".equalsIgnoreCase(lastEntity)) {
                    this.createBlock(v);
                } else if ("POLYLINE".equalsIgnoreCase(lastEntity)) {
                    this.createPolyline(v);
                } else if ("VERTEX".equalsIgnoreCase(lastEntity)) {
                    this.addVertex(v);
                } else if ("SEQEND".equalsIgnoreCase(lastEntity)) {
                    this.endSeq();
                } else if ("LWPOLYLINE".equalsIgnoreCase(lastEntity)) {
                    this.createLwPolyline(v);
                } else if ("LINE".equalsIgnoreCase(lastEntity)) {
                    this.createLine(v);
                } else if ("TEXT".equalsIgnoreCase(lastEntity)) {
                    this.createText(v);
                } else if ("MTEXT".equalsIgnoreCase(lastEntity)) {
                    this.createMText(v);
                } else if ("POINT".equalsIgnoreCase(lastEntity)) {
                    this.createPoint(v);
                } else if ("CIRCLE".equalsIgnoreCase(lastEntity)) {
                    this.createCircle(v);
                } else if ("ELLIPSE".equalsIgnoreCase(lastEntity)) {
                    this.createEllipse(v);
                } else if ("ARC".equalsIgnoreCase(lastEntity)) {
                    this.createArc(v);
                } else if ("INSERT".equalsIgnoreCase(lastEntity)) {
                    this.createInsert(v);
                } else if ("SOLID".equalsIgnoreCase(lastEntity)) {
                    this.createSolid(v);
                } else if ("SPLINE".equalsIgnoreCase(lastEntity)) {
                    this.createSpline(v);
                } else if ("ATTDEF".equalsIgnoreCase(lastEntity)) {
                    this.createAttdef(v);
                } else if ("ENDBLK".equalsIgnoreCase(lastEntity)) {
                    this.endBlk(v);
                } else {
                    if ("ENDSEC".equalsIgnoreCase(lastEntity)) break;
                    Long numUnknown = this.unknownBlockEntitiesTable.get(lastEntity);
                    if (numUnknown == null) {
                        numUnknown = new Long(0L);
                    }
                    numUnknown = numUnknown + 1L;
                    this.unknownBlockEntitiesTable.put(lastEntity, numUnknown);
                    ++unknownEntityCnt;
                }
            }
            v.clear();
            v.add(this.grp);
            while (true) {
                this.grp = this.readGrp();
                if (this.grp.getCode() == 0) break;
                v.add(this.grp);
            }
            ++blkCnt;
        }
        LOGGER.info((Object)("End of BLOCKS section: " + blkCnt + " block entities read, " + unknownEntityCnt + " unknown"));
        for (String entityName : this.unknownBlockEntitiesTable.keySet()) {
            LOGGER.info((Object)("Unknown block entity " + entityName + " -> " + this.unknownBlockEntitiesTable.get(entityName)));
        }
    }

    protected void readEntities() throws Exception {
        int entityCnt = 0;
        int unknownEntityCnt = 0;
        DxfGroupVector v = new DxfGroupVector();
        this.grp = this.readGrp();
        while (!this.grp.equals(0, "EOF")) {
            if (this.grp.getCode() != 0) continue;
            if (v.size() > 0) {
                String lastEntity = (String)((DxfGroup)v.get(0)).getData();
                if ("POLYLINE".equalsIgnoreCase(lastEntity)) {
                    this.createPolyline(v);
                } else if ("VERTEX".equalsIgnoreCase(lastEntity)) {
                    this.addVertex(v);
                } else if ("SEQEND".equalsIgnoreCase(lastEntity)) {
                    this.endSeq();
                } else if ("LWPOLYLINE".equalsIgnoreCase(lastEntity)) {
                    this.createLwPolyline(v);
                } else if ("LINE".equalsIgnoreCase(lastEntity)) {
                    this.createLine(v);
                } else if ("TEXT".equalsIgnoreCase(lastEntity)) {
                    this.createText(v);
                } else if ("MTEXT".equalsIgnoreCase(lastEntity)) {
                    this.createMText(v);
                } else if ("POINT".equalsIgnoreCase(lastEntity)) {
                    this.createPoint(v);
                } else if ("CIRCLE".equalsIgnoreCase(lastEntity)) {
                    this.createCircle(v);
                } else if ("ELLIPSE".equalsIgnoreCase(lastEntity)) {
                    this.createEllipse(v);
                } else if ("ARC".equalsIgnoreCase(lastEntity)) {
                    this.createArc(v);
                } else if ("INSERT".equalsIgnoreCase(lastEntity)) {
                    this.createInsert(v);
                } else if ("SOLID".equalsIgnoreCase(lastEntity)) {
                    this.createSolid(v);
                } else if ("SPLINE".equalsIgnoreCase(lastEntity)) {
                    this.createSpline(v);
                } else if ("ATTRIB".equalsIgnoreCase(lastEntity)) {
                    this.createAttrib(v);
                } else {
                    if ("ENDSEC".equalsIgnoreCase(lastEntity)) break;
                    Long numUnknown = this.unknownEntitiesTable.get(lastEntity);
                    if (numUnknown == null) {
                        numUnknown = new Long(0L);
                    }
                    numUnknown = numUnknown + 1L;
                    this.unknownEntitiesTable.put(lastEntity, numUnknown);
                    ++unknownEntityCnt;
                }
            }
            v.clear();
            v.add(this.grp);
            while (true) {
                this.grp = this.readGrp();
                if (this.grp.getCode() == 0) break;
                v.add(this.grp);
            }
            ++entityCnt;
        }
        LOGGER.info((Object)("End of ENTITIES section: " + entityCnt + " entities read, " + unknownEntityCnt + " unknown"));
        for (String entityName : this.unknownEntitiesTable.keySet()) {
            LOGGER.info((Object)("Unknown entity " + entityName + " -> " + this.unknownEntitiesTable.get(entityName)));
        }
    }

    protected void readAnySection() throws Exception {
        do {
            this.grp = this.readGrp();
        } while (!this.grp.equals(0, "ENDSEC") && !this.grp.equals(0, "EOF"));
    }

    public void setAcadVersion(DxfGroupVector v) throws Exception {
        if (v.hasCode(1)) {
            String codedVersion = new String(v.getDataAsString(1));
            this.acadVersion = DxfVersion.decodeAcadVersion(codedVersion);
        }
    }

    protected abstract String getVersion();

    @Override
    public String getAcadVersion() {
        return this.acadVersion;
    }

    @Override
    public boolean supportsVersion(String version) {
        if (version == null) {
            return true;
        }
        return this.getVersion().equalsIgnoreCase(version);
    }

    @Override
    public void setOption(String optionName, Object optionValue) {
        if (optionName.equalsIgnoreCase("GENERATE_ONE_FC_BY_LAYER")) {
            this.setGenerateOneFcByLayer((Boolean)optionValue);
        } else if (optionName.equalsIgnoreCase("SCHEMA_ATTRIBUTE_NAMES_TO_UPPERCASE")) {
            this.setSchemaAttributeNamesToUpperCase((Boolean)optionValue);
        } else if (optionName.equalsIgnoreCase("IGNORE_INSERT_ENTITIES")) {
            this.setIgnoreInsertEntities((Boolean)optionValue);
        } else if (optionName.equalsIgnoreCase("CLASSIFY_FCS_BY_GEOMETRY_TYPE_OPTION")) {
            this.setClassifyFcsByGeometryType((Boolean)optionValue);
        } else if (optionName.equalsIgnoreCase("OUTPUT_POINT_FC_SUFFIX")) {
            this.setOutputPointFcSuffix((String)optionValue);
        } else if (optionName.equalsIgnoreCase("OUTPUT_LINE_FC_SUFFIX")) {
            this.setOutputLineFcSuffix((String)optionValue);
        } else if (optionName.equalsIgnoreCase("OUTPUT_POLYGON_FC_SUFFIX")) {
            this.setOutputPolygonFcSuffix((String)optionValue);
        } else if (optionName.equalsIgnoreCase("BANNED_LAYER_NAMES_FC_SUFFIX")) {
            this.setBannedLayerNames((String[])optionValue);
        } else if (optionName.equalsIgnoreCase("IGNORE_EMPTY_DXF_LAYERS")) {
            this.setIgnoreEmptyLayers((Boolean)optionValue);
        } else {
            LOGGER.warn((Object)("Unsupported option " + optionName));
        }
    }

    @Override
    public FeatureCollection[] getFeatureCollections() throws Exception {
        ArrayList<FeatureCollection> fcs = new ArrayList<FeatureCollection>();
        if (this.generateOneFcByLayer && this.classifyFcsByGeometryType) {
            LOGGER.info((Object)"Classifying feature collections...");
            Collection<FeatureDataset> dxfFcs = this.datasets.values();
            for (FeatureDataset dxfDS : dxfFcs) {
                if (this.ignoreEmptyLayers && dxfDS.isEmpty()) continue;
                FeatureCollection[] classifiedFcs = DataSourceUtil.classifyFeatures(dxfDS, this.outputPointFcSuffix, this.outputLineFcSuffix, this.outputPolygonFcSuffix);
                int numFcs = 0;
                int position = -1;
                int i = 0;
                while (i < classifiedFcs.length) {
                    if (classifiedFcs[i].size() > 0) {
                        ++numFcs;
                        position = i;
                    }
                    ++i;
                }
                if (numFcs > 1) {
                    FeatureCollection[] featureCollectionArray = classifiedFcs;
                    int n = classifiedFcs.length;
                    int n2 = 0;
                    while (n2 < n) {
                        FeatureCollection fc = featureCollectionArray[n2];
                        if (fc.size() > 0) {
                            fc.commit();
                            fcs.add(fc);
                        }
                        ++n2;
                    }
                    continue;
                }
                if (position == -1) {
                    dxfDS.getFeatureSchema().setGeometryType(1);
                } else {
                    dxfDS.getFeatureSchema().setGeometryType(classifiedFcs[position].getFeatureSchema().getGeometryType());
                }
                dxfDS.commit();
                fcs.add(dxfDS);
            }
            return fcs.toArray(new FeatureCollection[0]);
        }
        return this.datasets.values().toArray(new FeatureCollection[0]);
    }

    protected void createLayer(DxfGroupVector v) throws Exception {
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
        if (this.generateOneFcByLayer) {
            FeatureDataset dataset = new FeatureDataset((FeatureSchema)this.baseSchema.clone());
            dataset.setName(layer.getName());
            this.datasets.put(layer.getName(), dataset);
        }
    }

    protected FeatureSchema generateBaseFeatureSchema() {
        FeatureSchema schema = new FeatureSchema();
        schema.setGeometryType(15);
        schema.addAttribute("ID", AttributeType.LONG, Boolean.TRUE);
        schema.addAttribute("FShape", AttributeType.STRING);
        schema.addAttribute("Entity", AttributeType.STRING);
        schema.addAttribute("Layer", AttributeType.STRING);
        schema.addAttribute("Color", AttributeType.INTEGER);
        schema.addAttribute("Elevation", AttributeType.DOUBLE);
        schema.addAttribute("Thickness", AttributeType.DOUBLE);
        schema.addAttribute("Text", AttributeType.STRING);
        schema.addAttribute("HeightText", AttributeType.DOUBLE);
        schema.addAttribute("RotationText", AttributeType.DOUBLE);
        schema.addAttribute("BlockName", AttributeType.STRING);
        schema.addAttribute("Geometry", AttributeType.GEOMETRY);
        return schema;
    }

    protected void createPoint(DxfGroupVector gv) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        Feature feat = this.buildBasicFeature();
        feat = this.fillBasicAttributes(feat, gv, "Point");
        x = gv.getDataAsDouble(10);
        y = gv.getDataAsDouble(20);
        z = gv.getDataAsDouble(30);
        if (gv.hasCode(210)) {
            extx = gv.getDataAsDouble(210);
        }
        if (gv.hasCode(220)) {
            exty = gv.getDataAsDouble(220);
        }
        if (gv.hasCode(230)) {
            extz = gv.getDataAsDouble(230);
        }
        Point3D point_in = new Point3D(x, y, z);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        z = point_out.getZ();
        feat.setAttribute("Elevation", (Object)new Double(z));
        if (z != 0.0) {
            feat.setAttribute("FShape", (Object)"Point3D");
            this.getDatasetByName(feat).set3d(true);
        } else {
            feat.setAttribute("FShape", (Object)"Point2D");
        }
        feat.setGeometry((Geometry)this.geomFact.createPoint(new Coordinate(x, y, z)));
        this.completeAttributes(feat);
        if (this.addingToBlock) {
            this.currentBlock.addFeature(feat);
        } else {
            this.getDatasetByName(feat).addWithNewKey(feat);
        }
    }

    protected FeatureDataset getDatasetByName(Feature feat) {
        if (this.generateOneFcByLayer) {
            return this.datasets.get(feat.getString("Layer"));
        }
        return this.datasets.get("");
    }

    protected void createText(DxfGroupVector gv) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        Feature feat = this.buildBasicFeature();
        feat = this.fillBasicAttributes(feat, gv, "Text");
        if (gv.hasCode(1)) {
            String strAux1 = gv.getDataAsString(1);
            strAux1 = DxfConvTexts.ConvertText(strAux1);
            feat.setAttribute("Text", (Object)strAux1);
        } else {
            feat.setAttribute("Text", (Object)"No Text Code");
        }
        if (gv.hasCode(40)) {
            Double heightD = gv.getDataAsDouble(40);
            feat.setAttribute("HeightText", (Object)heightD);
        } else {
            feat.setAttribute("HeightText", (Object)20.0);
        }
        if (gv.hasCode(50)) {
            Double rotD = gv.getDataAsDouble(50);
            feat.setAttribute("RotationText", (Object)rotD);
        } else {
            feat.setAttribute("RotationText", (Object)0.0);
        }
        x = gv.getDataAsDouble(10);
        y = gv.getDataAsDouble(20);
        z = gv.getDataAsDouble(30);
        if (gv.hasCode(210)) {
            extx = gv.getDataAsDouble(210);
        }
        if (gv.hasCode(220)) {
            exty = gv.getDataAsDouble(220);
        }
        if (gv.hasCode(230)) {
            extz = gv.getDataAsDouble(230);
        }
        Point3D point_in = new Point3D(x, y, z);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        z = point_out.getZ();
        feat.setAttribute("Elevation", (Object)new Double(z));
        if (z != 0.0) {
            feat.setAttribute("FShape", (Object)"Point3D");
            this.getDatasetByName(feat).set3d(true);
        } else {
            feat.setAttribute("FShape", (Object)"Point2D");
        }
        feat.setGeometry((Geometry)this.geomFact.createPoint(new Coordinate(x, y, z)));
        this.completeAttributes(feat);
        if (this.addingToBlock) {
            this.currentBlock.addFeature(feat);
        } else {
            this.getDatasetByName(feat).addWithNewKey(feat);
        }
    }

    protected void createMText(DxfGroupVector gv) throws Exception {
        int spacingStyle;
        int drawDirection;
        int attachPoint;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        Feature feat = this.buildBasicFeature();
        feat = this.fillBasicAttributes(feat, gv, "Text");
        if (gv.hasCode(1)) {
            String strAux1 = gv.getDataAsString(1);
            strAux1 = DxfConvTexts.ConvertText(strAux1);
            feat.setAttribute("Text", (Object)strAux1);
        } else {
            feat.setAttribute("Text", (Object)"No Text Code");
        }
        if (gv.hasCode(40)) {
            Double heightD = gv.getDataAsDouble(40);
            feat.setAttribute("HeightText", (Object)heightD);
        } else {
            feat.setAttribute("HeightText", (Object)20.0);
        }
        if (gv.hasCode(50)) {
            Double rotD = gv.getDataAsDouble(50);
            feat.setAttribute("RotationText", (Object)rotD);
        } else {
            feat.setAttribute("RotationText", (Object)0.0);
        }
        if (!gv.hasCode(71) || (attachPoint = gv.getDataAsInt(71)) == 1 || attachPoint == 2 || attachPoint == 3 || attachPoint == 4 || attachPoint == 5 || attachPoint == 6 || attachPoint == 7 || attachPoint != 8) {
            // empty if block
        }
        if (!gv.hasCode(72) || (drawDirection = gv.getDataAsInt(71)) == 1 || drawDirection != 3) {
            // empty if block
        }
        if (!gv.hasCode(73) || (spacingStyle = gv.getDataAsInt(71)) != 1) {
            // empty if block
        }
        x = gv.getDataAsDouble(10);
        y = gv.getDataAsDouble(20);
        z = gv.getDataAsDouble(30);
        if (gv.hasCode(210)) {
            extx = gv.getDataAsDouble(210);
        }
        if (gv.hasCode(220)) {
            exty = gv.getDataAsDouble(220);
        }
        if (gv.hasCode(230)) {
            extz = gv.getDataAsDouble(230);
        }
        Point3D point_in = new Point3D(x, y, z);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        z = point_out.getZ();
        feat.setAttribute("Elevation", (Object)new Double(z));
        if (z != 0.0) {
            feat.setAttribute("FShape", (Object)"Point3D");
            this.getDatasetByName(feat).set3d(true);
        } else {
            feat.setAttribute("FShape", (Object)"Point2D");
        }
        feat.setGeometry((Geometry)this.geomFact.createPoint(new Coordinate(x, y, z)));
        this.completeAttributes(feat);
        if (this.addingToBlock) {
            this.currentBlock.addFeature(feat);
        } else {
            this.getDatasetByName(feat).addWithNewKey(feat);
        }
    }

    protected void createInsert(DxfGroupVector gv) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        Point3D scaleFactor = new Point3D(1.0, 1.0, 1.0);
        double rotAngle = 0.0;
        String blockName = "";
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        Feature feat = this.buildBasicFeature();
        feat = this.fillBasicAttributes(feat, gv, "Insert");
        Feature blockFeat = this.buildBasicFeature();
        blockFeat = this.fillBasicAttributes(blockFeat, gv, "Insert");
        int attributesFollowFlag = 0;
        if (gv.hasCode(2)) {
            blockName = gv.getDataAsString(2);
            feat.setAttribute("BlockName", (Object)blockName);
            blockFeat.setAttribute("BlockName", (Object)blockName);
        }
        if (gv.hasCode(66)) {
            attributesFollowFlag = gv.getDataAsInt(66);
        }
        if (gv.hasCode(41)) {
            scaleFactor.setLocation(gv.getDataAsDouble(41), scaleFactor.getY());
        }
        if (gv.hasCode(42)) {
            scaleFactor.setLocation(scaleFactor.getX(), gv.getDataAsDouble(42));
        }
        if (gv.hasCode(43)) {
            scaleFactor = new Point3D(scaleFactor.getX(), scaleFactor.getY(), gv.getDataAsDouble(43));
        }
        if (gv.hasCode(50)) {
            rotAngle = gv.getDataAsDouble(50);
        }
        x = gv.getDataAsDouble(10);
        y = gv.getDataAsDouble(20);
        z = gv.getDataAsDouble(30);
        if (gv.hasCode(210)) {
            extx = gv.getDataAsDouble(210);
        }
        if (gv.hasCode(220)) {
            exty = gv.getDataAsDouble(220);
        }
        if (gv.hasCode(230)) {
            extz = gv.getDataAsDouble(230);
        }
        Point3D point_in = new Point3D(x, y, z);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        z = point_out.getZ();
        feat.setAttribute("Elevation", (Object)z);
        blockFeat.setAttribute("Elevation", (Object)z);
        if (z != 0.0) {
            feat.setAttribute("FShape", (Object)"Point3D");
            blockFeat.setAttribute("FShape", (Object)"Point3D");
            this.getDatasetByName(feat).set3d(true);
        } else {
            feat.setAttribute("FShape", (Object)"Point2D");
            blockFeat.setAttribute("FShape", (Object)"Point2D");
        }
        feat.setGeometry((Geometry)this.geomFact.createPoint(new Coordinate(x, y, z)));
        blockFeat.setGeometry((Geometry)this.geomFact.createPoint(new Coordinate(x, y, z)));
        this.completeAttributes(feat);
        this.completeAttributes(blockFeat);
        if (StringUtils.isNotEmpty((String)blockName) && this.blocks.containsKey(blockName) && attributesFollowFlag != 1) {
            this.manageInsert(feat, blockName, scaleFactor, rotAngle);
        }
        if (attributesFollowFlag == 1) {
            this.isDoubleFeatured = true;
            this.tempFeatBorder = feat;
            this.tempFeatBackground = blockFeat;
        } else {
            if (!this.addingToBlock && (!this.ignoreInsertEntities || this.blocks.containsKey(blockName) && CollectionUtils.isEmpty(this.blocks.get(blockName).getFeatures()))) {
                this.getDatasetByName(blockFeat).addWithNewKey(blockFeat);
            }
            if (this.addingToBlock) {
                this.currentBlock.addFeature(feat);
            }
        }
    }

    protected void createAttdef(DxfGroupVector gv) throws Exception {
        String defaultValue = null;
        String tagString = "";
        String publicName = null;
        String textStyleName = "";
        if (gv.hasCode(2)) {
            tagString = DxfConvTexts.ConvertText(gv.getDataAsString(2));
        }
        if (gv.hasCode(1)) {
            defaultValue = DxfConvTexts.ConvertText(gv.getDataAsString(1));
        }
        publicName = gv.hasCode(3) ? DxfConvTexts.ConvertText(gv.getDataAsString(3)) : tagString;
        if (gv.hasCode(7)) {
            textStyleName = gv.getDataAsString(7);
            textStyleName = DxfConvTexts.ConvertText(textStyleName);
        }
        this.currentBlock.addAttribute(new Attribute(this.schemaAttributeNamesToUpperCase ? tagString.toUpperCase() : tagString, this.schemaAttributeNamesToUpperCase ? publicName.toUpperCase() : publicName, Boolean.TRUE, AttributeType.STRING), defaultValue);
    }

    protected void createAttrib(DxfGroupVector gv) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        String defaultValue = "";
        String tagString = "";
        String textStyleName = "";
        String[] att = new String[2];
        boolean defValDefined = false;
        int attributeFlags = 0;
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        Feature insFea = this.tempFeatBorder;
        Feature ptFea = this.tempFeatBackground;
        Feature feat = this.buildBasicFeature();
        feat = this.fillBasicAttributes(feat, gv, "Attrib");
        this.completeAttributes(feat);
        if (gv.hasCode(1)) {
            String strAux1 = gv.getDataAsString(1);
            defaultValue = strAux1 = DxfConvTexts.ConvertText(strAux1);
            att[1] = DxfConvTexts.ConvertText(defaultValue);
            defValDefined = true;
            feat.setAttribute("Text", (Object)strAux1);
        }
        if (gv.hasCode(2)) {
            String strAux2 = gv.getDataAsString(2);
            tagString = strAux2 = DxfConvTexts.ConvertText(strAux2);
            att[0] = DxfConvTexts.ConvertText(tagString);
            if (defValDefined) {
                if (insFea.getSchema().hasAttribute(this.schemaAttributeNamesToUpperCase ? att[0].toUpperCase() : att[0])) {
                    insFea.setAttribute(this.schemaAttributeNamesToUpperCase ? att[0].toUpperCase() : att[0], (Object)att[1]);
                    ptFea.setAttribute(this.schemaAttributeNamesToUpperCase ? att[0].toUpperCase() : att[0], (Object)att[1]);
                } else {
                    LOGGER.warn((Object)("Feature " + insFea + " - " + att[0]));
                }
                if (feat.getSchema().hasAttribute(this.schemaAttributeNamesToUpperCase ? att[0].toUpperCase() : att[0])) {
                    feat.setAttribute(this.schemaAttributeNamesToUpperCase ? att[0].toUpperCase() : att[0], (Object)att[1]);
                }
            }
        }
        if (gv.hasCode(7)) {
            textStyleName = gv.getDataAsString(7);
            textStyleName = DxfConvTexts.ConvertText(textStyleName);
        }
        if (gv.hasCode(70)) {
            attributeFlags = gv.getDataAsInt(70);
        }
        if (gv.hasCode(40)) {
            Double heightD = gv.getDataAsDouble(40);
            feat.setAttribute("HeightText", (Object)heightD);
        } else {
            feat.setAttribute("HeightText", (Object)20.0);
        }
        if (gv.hasCode(50)) {
            Double rotD = gv.getDataAsDouble(50);
            feat.setAttribute("RotationText", (Object)rotD);
        } else {
            feat.setAttribute("RotationText", (Object)0.0);
        }
        x = gv.getDataAsDouble(10);
        y = gv.getDataAsDouble(20);
        z = gv.getDataAsDouble(30);
        if (gv.hasCode(210)) {
            extx = gv.getDataAsDouble(210);
        }
        if (gv.hasCode(220)) {
            exty = gv.getDataAsDouble(220);
        }
        if (gv.hasCode(230)) {
            extz = gv.getDataAsDouble(230);
        }
        Point3D point_in = new Point3D(x, y, z);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        z = point_out.getZ();
        feat.setAttribute("Elevation", (Object)new Double(z));
        if (z != 0.0) {
            feat.setAttribute("FShape", (Object)"Point3D");
            this.getDatasetByName(feat).set3d(true);
        } else {
            feat.setAttribute("FShape", (Object)"Point2D");
        }
        feat.setGeometry((Geometry)this.geomFact.createPoint(new Coordinate(x, y, z)));
        if (attributeFlags == 8) {
            if (this.addingToBlock) {
                this.currentBlock.addFeature(feat);
            } else {
                this.getDatasetByName(feat).addWithNewKey(feat);
            }
        }
    }

    protected Feature fillBasicAttributes(Feature feat, DxfGroupVector gv, String entityName) {
        String layerName = null;
        Integer colorValue = null;
        feat.setAttribute("Entity", (Object)entityName);
        if (gv.hasCode(8)) {
            layerName = gv.getDataAsString(8);
            feat.setAttribute("Layer", (Object)layerName);
            FeatureSchema schema = this.getDatasetByName(feat).getFeatureSchema();
            if (schema != feat.getSchema()) {
                BasicFeature featAux = new BasicFeature(schema);
                FeatureUtil.copyOnlyExistentAttributes(feat, featAux, true, false);
                feat = featAux;
            }
        }
        if (gv.hasCode(39)) {
            Double doub = new Double(gv.getDataAsDouble(39));
            feat.setAttribute("Thickness", (Object)doub);
        } else {
            feat.setAttribute("Thickness", (Object)new Double(0.0));
        }
        if (gv.hasCode(62)) {
            colorValue = gv.getDataAsInt(62);
            feat.setAttribute("Color", (Object)colorValue);
        } else {
            DxfLayer layer = (DxfLayer)this.layers.getByName(gv.getDataAsString(8));
            colorValue = layer.colorNumber;
            feat.setAttribute("Color", (Object)colorValue);
        }
        if (StringUtils.isNotEmpty((String)layerName) && colorValue != null && !ArrayUtils.contains((Object[])this.bannedLayerNames, (Object)layerName)) {
            if (!this.layerToColor.keySet().contains(layerName)) {
                HashSet colors = new HashSet();
                this.layerToColor.put(layerName, colors);
            }
            this.layerToColor.get(layerName).add(colorValue);
        }
        return feat;
    }

    protected void createPolyline(DxfGroupVector gv) throws Exception {
        double z = 0.0;
        int flags = 0;
        this.constantPolylineElevation = true;
        this.faces = new Vector();
        Feature featBorder = this.buildBasicFeature();
        featBorder = this.fillBasicAttributes(featBorder, gv, "Polyline");
        Feature featBackground = this.buildBasicFeature();
        featBackground = this.fillBasicAttributes(featBackground, gv, "Polyline");
        if (gv.hasCode(30)) {
            z = gv.getDataAsDouble(30);
            if (z != 0.0) {
                this.getDatasetByName(featBorder).set3d(true);
                featBorder.setAttribute("FShape", (Object)new String("Polyline3D"));
                featBackground.setAttribute("FShape", (Object)new String("Polygon3D"));
            } else {
                featBorder.setAttribute("FShape", (Object)new String("Polyline2D"));
                featBackground.setAttribute("FShape", (Object)new String("Polygon2D"));
            }
            featBorder.setAttribute("Elevation", (Object)z);
            featBackground.setAttribute("Elevation", (Object)z);
        } else {
            featBorder.setAttribute("Elevation", (Object)0.0);
            featBackground.setAttribute("Elevation", (Object)0.0);
            featBorder.setAttribute("FShape", (Object)new String("Polyline2D"));
            featBackground.setAttribute("FShape", (Object)new String("Polygon2D"));
        }
        if (gv.hasCode(70)) {
            flags = gv.getDataAsInt(70);
        }
        if (gv.hasCode(210)) {
            this.xtruX = gv.getDataAsDouble(210);
        }
        if (gv.hasCode(220)) {
            this.xtruY = gv.getDataAsDouble(220);
        }
        this.xtruZ = gv.hasCode(230) ? gv.getDataAsDouble(230) : 1.0;
        if ((flags & 1) == 1 || (flags & 0x40) == 64) {
            featBorder.setGeometry((Geometry)this.geomFact.createLineString(new Coordinate[0]));
            featBackground.setGeometry((Geometry)this.geomFact.createPolygon(new Coordinate[0]));
            this.tempFeatBorder = featBorder;
            this.tempFeatBackground = featBackground;
            this.isDoubleFeatured = true;
        } else if ((flags & 1) == 0) {
            featBorder.setGeometry((Geometry)this.geomFact.createLineString(new Coordinate[0]));
            this.tempFeatBorder = featBorder;
            this.isDoubleFeatured = false;
        } else {
            LOGGER.debug((Object)"Polyline flag detected not corresponding to normal polyline nor a closed polyline");
        }
        this.tempFeatBorderCoordList = new ArrayList<Coordinate>();
        this.tempFeatBackgroundCoordList = new ArrayList<Coordinate>();
    }

    protected void addVertex(DxfGroupVector gv) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        int vFlags = 0;
        if (this.isDoubleFeatured) {
            if (gv.hasCode(70)) {
                vFlags = gv.getDataAsInt(70);
            }
            x = gv.getDataAsDouble(10);
            y = gv.getDataAsDouble(20);
            z = gv.getDataAsDouble(30);
            Point3D point_in = new Point3D(x, y, z);
            Point3D xtru = new Point3D(this.xtruX, this.xtruY, this.xtruZ);
            Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
            x = point_out.getX();
            y = point_out.getY();
            z = point_out.getZ();
            if (z != 0.0) {
                this.getDatasetByName(this.tempFeatBorder).set3d(true);
            }
            if (z != this.lastVertexElevation && this.tempFeatBorderCoordList != null && this.tempFeatBorderCoordList.size() > 0) {
                this.constantPolylineElevation = false;
            }
            this.lastVertexElevation = z;
            if ((vFlags & 0x80) == 128 && (vFlags & 0x40) == 0) {
                int[] face = new int[]{gv.getDataAsInt(71), gv.getDataAsInt(72), gv.getDataAsInt(73), gv.getDataAsInt(74)};
                this.addFace(face);
            } else if ((vFlags & 0x10) != 16) {
                Coordinate vertexCoord = new Coordinate(x, y, z);
                if (this.tempFeatBorderCoordList == null) {
                    this.tempFeatBorderCoordList = new ArrayList<Coordinate>();
                }
                if (this.tempFeatBackgroundCoordList == null) {
                    this.tempFeatBackgroundCoordList = new ArrayList<Coordinate>();
                }
                this.tempFeatBorderCoordList.add(vertexCoord);
                this.tempFeatBackgroundCoordList.add(vertexCoord);
                if (this.tempFeatBorderCoordList.size() == 1) {
                    this.firstCoord = vertexCoord;
                }
                if (this.bulge == 0.0) {
                    this.bulge = gv.hasCode(42) ? gv.getDataAsDouble(42) : 0.0;
                } else {
                    double bulge_aux = 0.0;
                    bulge_aux = gv.hasCode(42) ? gv.getDataAsDouble(42) : 0.0;
                    if (this.lastCoord.x != x || this.lastCoord.y != y) {
                        this.tempFeatBorderCoordList.remove(this.tempFeatBorderCoordList.size() - 1);
                        this.tempFeatBorderCoordList.remove(this.tempFeatBorderCoordList.size() - 1);
                        this.tempFeatBackgroundCoordList.remove(this.tempFeatBackgroundCoordList.size() - 1);
                        this.tempFeatBackgroundCoordList.remove(this.tempFeatBackgroundCoordList.size() - 1);
                        Vector<Point2D> arc = this.createArc(this.lastCoord, vertexCoord, this.bulge);
                        if (this.bulge > 0.0) {
                            int i = 0;
                            while (i < arc.size()) {
                                Coordinate coord = new Coordinate(arc.get(i).getX(), arc.get(i).getY(), z);
                                Coordinate coord2 = (Coordinate)coord.clone();
                                this.tempFeatBorderCoordList.add(coord);
                                this.tempFeatBackgroundCoordList.add(coord2);
                                if (this.tempFeatBorderCoordList.size() == 1) {
                                    this.firstCoord = coord;
                                }
                                ++i;
                            }
                        } else {
                            int i = arc.size() - 1;
                            while (i >= 0) {
                                Coordinate coord = new Coordinate(arc.get(i).getX(), arc.get(i).getY(), z);
                                Coordinate coord2 = (Coordinate)coord.clone();
                                this.tempFeatBorderCoordList.add(coord);
                                this.tempFeatBackgroundCoordList.add(coord2);
                                if (this.tempFeatBorderCoordList.size() == 1 || this.tempFeatBackgroundCoordList.size() == 1) {
                                    this.firstCoord = coord;
                                }
                                --i;
                            }
                        }
                    }
                    this.bulge = bulge_aux;
                }
                this.lastCoord = vertexCoord;
            }
        } else {
            if (gv.hasCode(70)) {
                vFlags = gv.getDataAsInt(70);
            }
            x = gv.getDataAsDouble(10);
            y = gv.getDataAsDouble(20);
            if (gv.hasCode(30)) {
                z = gv.getDataAsDouble(30);
            }
            Point3D point_in = new Point3D(x, y, z);
            Point3D xtru = new Point3D(this.xtruX, this.xtruY, this.xtruZ);
            Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
            x = point_out.getX();
            y = point_out.getY();
            z = point_out.getZ();
            if (z != 0.0) {
                this.getDatasetByName(this.tempFeatBorder).set3d(true);
            }
            if (z != this.lastVertexElevation && this.tempFeatBorderCoordList != null && this.tempFeatBorderCoordList.size() > 0) {
                this.constantPolylineElevation = false;
            }
            this.lastVertexElevation = z;
            if ((vFlags & 0x80) == 128 && (vFlags & 0x40) == 0) {
                int[] face = new int[]{gv.getDataAsInt(71), gv.getDataAsInt(72), gv.getDataAsInt(73), gv.getDataAsInt(74)};
                this.addFace(face);
            } else if ((vFlags & 0x10) != 16) {
                Coordinate vertexCoord = new Coordinate(x, y, z);
                if (this.tempFeatBorderCoordList == null) {
                    this.tempFeatBorderCoordList = new ArrayList<Coordinate>();
                }
                this.tempFeatBorderCoordList.add(vertexCoord);
                if (this.tempFeatBorderCoordList.size() == 1) {
                    this.firstCoord = vertexCoord;
                }
                if (this.bulge == 0.0) {
                    this.bulge = gv.hasCode(42) ? gv.getDataAsDouble(42) : 0.0;
                } else {
                    double bulge_aux = 0.0;
                    bulge_aux = gv.hasCode(42) ? gv.getDataAsDouble(42) : 0.0;
                    if (this.lastCoord.x != vertexCoord.x || this.lastCoord.y != vertexCoord.y) {
                        this.tempFeatBorderCoordList.remove(this.tempFeatBorderCoordList.size() - 1);
                        this.tempFeatBorderCoordList.remove(this.tempFeatBorderCoordList.size() - 1);
                        Vector<Point2D> arc = this.createArc(this.lastCoord, vertexCoord, this.bulge);
                        if (this.bulge > 0.0) {
                            int i = 0;
                            while (i < arc.size()) {
                                Coordinate coord = new Coordinate(arc.get(i).getX(), arc.get(i).getY(), z);
                                this.tempFeatBorderCoordList.add(coord);
                                if (this.tempFeatBorderCoordList.size() == 1) {
                                    this.firstCoord = coord;
                                }
                                ++i;
                            }
                        } else {
                            int i = arc.size() - 1;
                            while (i >= 0) {
                                Coordinate coord = new Coordinate(arc.get(i).getX(), arc.get(i).getY(), z);
                                this.tempFeatBorderCoordList.add(coord);
                                if (this.tempFeatBorderCoordList.size() == 1) {
                                    this.firstCoord = coord;
                                }
                                --i;
                            }
                        }
                    }
                    this.bulge = bulge_aux;
                }
                this.lastCoord = vertexCoord;
            }
        }
    }

    protected void endSeq() throws Exception {
        if (this.tempFeatBorder != null) {
            if (this.isDoubleFeatured) {
                if (this.tempFeatBorder.getGeometry() instanceof LineString) {
                    this.tempFeatBorderCoordList.add(this.firstCoord);
                    if (this.bulge != 0.0 && (this.tempFeatBorderCoordList.get((int)(this.tempFeatBorderCoordList.size() - 2)).x != this.tempFeatBorderCoordList.get((int)(this.tempFeatBorderCoordList.size() - 1)).x || this.tempFeatBorderCoordList.get((int)(this.tempFeatBorderCoordList.size() - 2)).y != this.tempFeatBorderCoordList.get((int)(this.tempFeatBorderCoordList.size() - 1)).y)) {
                        Coordinate coord2;
                        Coordinate coord;
                        double z;
                        int i;
                        Vector<Point2D> arc = this.createArc(this.tempFeatBorderCoordList.get(this.tempFeatBorderCoordList.size() - 2), this.tempFeatBorderCoordList.get(this.tempFeatBorderCoordList.size() - 1), this.bulge);
                        this.tempFeatBorderCoordList.remove(this.tempFeatBorderCoordList.size() - 1);
                        this.tempFeatBorderCoordList.remove(this.tempFeatBorderCoordList.size() - 1);
                        this.tempFeatBackgroundCoordList.remove(this.tempFeatBackgroundCoordList.size() - 1);
                        this.tempFeatBackgroundCoordList.remove(this.tempFeatBackgroundCoordList.size() - 1);
                        if (this.bulge > 0.0) {
                            i = 0;
                            while (i < arc.size()) {
                                z = 0.0;
                                if (this.tempFeatBorderCoordList.size() >= 2) {
                                    z = this.tempFeatBorderCoordList.get((int)(this.tempFeatBorderCoordList.size() - 2)).z;
                                }
                                coord = new Coordinate(arc.get(i).getX(), arc.get(i).getY(), z);
                                this.tempFeatBorderCoordList.add(coord);
                                coord2 = (Coordinate)coord.clone();
                                this.tempFeatBackgroundCoordList.add(coord2);
                                if (this.tempFeatBorderCoordList.size() == 1) {
                                    this.firstCoord = coord;
                                }
                                ++i;
                            }
                        } else {
                            i = arc.size() - 1;
                            while (i >= 0) {
                                z = 0.0;
                                if (this.tempFeatBorderCoordList.size() >= 2) {
                                    z = this.tempFeatBorderCoordList.get((int)(this.tempFeatBorderCoordList.size() - 2)).z;
                                }
                                coord = new Coordinate(arc.get(i).getX(), arc.get(i).getY(), z);
                                this.tempFeatBorderCoordList.add(coord);
                                coord2 = (Coordinate)coord.clone();
                                this.tempFeatBackgroundCoordList.add(coord2);
                                if (this.tempFeatBorderCoordList.size() == 1) {
                                    this.firstCoord = coord;
                                }
                                --i;
                            }
                        }
                        this.bulge = 0.0;
                    }
                    if (this.hasFaces) {
                        int i;
                        Iterator<int[]> iter = this.faces.iterator();
                        ArrayList<Coordinate> tempLine1 = new ArrayList<Coordinate>();
                        ArrayList<Coordinate> tempLine2 = new ArrayList<Coordinate>();
                        ArrayList<Coordinate> tempPol1 = new ArrayList<Coordinate>();
                        ArrayList<Coordinate> tempPol2 = new ArrayList<Coordinate>();
                        ArrayList<Coordinate> lineCoords = new ArrayList<Coordinate>();
                        ArrayList<Coordinate> polCoords = new ArrayList<Coordinate>();
                        while (iter.hasNext()) {
                            int[] face = iter.next();
                            int i0 = face[3];
                            i = 0;
                            while (i < 4) {
                                int i1 = face[i];
                                if (i0 > 0) {
                                    if (this.facesIterador % 2 != 0) {
                                        tempLine1.add(this.tempFeatBorderCoordList.get(i0 - 1));
                                        tempPol1.add(this.tempFeatBackgroundCoordList.get(i0 - 1));
                                    } else {
                                        tempLine2.add(this.tempFeatBorderCoordList.get(i0 - 1));
                                        tempPol2.add(this.tempFeatBackgroundCoordList.get(i0 - 1));
                                    }
                                    ++this.facesIterador;
                                }
                                i0 = i1;
                                ++i;
                            }
                        }
                        this.facesFirstCoord = new Coordinate(((Coordinate)tempLine1.get((int)0)).x, ((Coordinate)tempLine1.get((int)0)).y, ((Coordinate)tempLine1.get((int)0)).z);
                        i = 0;
                        while (i < tempLine1.size()) {
                            lineCoords.add((Coordinate)tempLine1.get(i));
                            polCoords.add((Coordinate)tempPol1.get(i));
                            ++i;
                        }
                        i = tempLine2.size() - 1;
                        while (i > 0) {
                            lineCoords.add((Coordinate)tempLine2.get(i));
                            polCoords.add((Coordinate)tempPol2.get(i));
                            --i;
                        }
                        lineCoords.add(this.facesFirstCoord);
                        polCoords.add(this.facesFirstCoord);
                        this.tempFeatBorderCoordList = lineCoords;
                        this.tempFeatBackgroundCoordList = polCoords;
                    }
                    this.completeAttributes(this.tempFeatBorder);
                    this.completeAttributes(this.tempFeatBackground);
                    this.setPolylineElevation(this.tempFeatBorder);
                    this.setPolylineElevation(this.tempFeatBackground);
                    if (!this.tempFeatBackgroundCoordList.get(0).equals3D(this.tempFeatBackgroundCoordList.get(this.tempFeatBackgroundCoordList.size() - 1))) {
                        this.tempFeatBackgroundCoordList.add(this.tempFeatBackgroundCoordList.get(0));
                    }
                    while (this.tempFeatBackgroundCoordList.size() < 4) {
                        this.tempFeatBackgroundCoordList.add(this.tempFeatBackgroundCoordList.get(0));
                    }
                    Polygon p = this.geomFact.createPolygon(this.tempFeatBackgroundCoordList.toArray(new Coordinate[0]));
                    this.tempFeatBackground.setGeometry((Geometry)p);
                    if (this.addingToBlock) {
                        this.currentBlock.addFeature(this.tempFeatBackground);
                    } else {
                        this.getDatasetByName(this.tempFeatBackground).addWithNewKey(this.tempFeatBackground);
                    }
                    this.tempFeatBorder = null;
                    this.tempFeatBackground = null;
                    this.tempFeatBorderCoordList = null;
                    this.tempFeatBackgroundCoordList = null;
                } else if (this.tempFeatBorder.getGeometry() instanceof Point && this.tempFeatBorder.getAttribute("Entity").equals("Insert")) {
                    this.manageInsert(this.tempFeatBorder, this.tempFeatBorder.getString("BlockName"), new Point3D(1.0, 1.0, 1.0), 0.0);
                    if (this.addingToBlock) {
                        this.currentBlock.addFeature(this.tempFeatBorder);
                    } else {
                        String blockName = (String)this.tempFeatBackground.getAttribute("BlockName");
                        if (!this.ignoreInsertEntities || this.blocks.containsKey(blockName) && CollectionUtils.isEmpty(this.blocks.get(blockName).getFeatures())) {
                            this.getDatasetByName(this.tempFeatBackground).addWithNewKey(this.tempFeatBackground);
                        }
                    }
                    this.tempFeatBorder = null;
                    this.tempFeatBackground = null;
                    this.tempFeatBorderCoordList = null;
                    this.tempFeatBackgroundCoordList = null;
                }
            } else if (this.tempFeatBorder.getGeometry() instanceof LineString) {
                if (this.bulge != 0.0 && (this.tempFeatBorderCoordList.get((int)(this.tempFeatBorderCoordList.size() - 2)).x != this.tempFeatBorderCoordList.get((int)(this.tempFeatBorderCoordList.size() - 1)).x || this.tempFeatBorderCoordList.get((int)(this.tempFeatBorderCoordList.size() - 2)).y != this.tempFeatBorderCoordList.get((int)(this.tempFeatBorderCoordList.size() - 1)).y)) {
                    Coordinate coord;
                    double z;
                    int i;
                    Vector<Point2D> arc = this.createArc(this.tempFeatBorderCoordList.get(this.tempFeatBorderCoordList.size() - 2), this.tempFeatBorderCoordList.get(this.tempFeatBorderCoordList.size() - 1), this.bulge);
                    this.tempFeatBorderCoordList.remove(this.tempFeatBorderCoordList.size() - 1);
                    this.tempFeatBorderCoordList.remove(this.tempFeatBorderCoordList.size() - 1);
                    if (this.bulge > 0.0) {
                        i = 0;
                        while (i < arc.size()) {
                            z = 0.0;
                            if (this.tempFeatBorderCoordList.size() >= 2) {
                                z = this.tempFeatBorderCoordList.get((int)(this.tempFeatBorderCoordList.size() - 2)).z;
                            }
                            coord = new Coordinate(arc.get(i).getX(), arc.get(i).getY(), z);
                            this.tempFeatBorderCoordList.add(coord);
                            if (this.tempFeatBorderCoordList.size() == 1) {
                                this.firstCoord = coord;
                            }
                            ++i;
                        }
                    } else {
                        i = arc.size() - 1;
                        while (i >= 0) {
                            z = 0.0;
                            if (this.tempFeatBorderCoordList.size() >= 2) {
                                z = this.tempFeatBorderCoordList.get((int)(this.tempFeatBorderCoordList.size() - 2)).z;
                            }
                            coord = new Coordinate(arc.get(i).getX(), arc.get(i).getY(), z);
                            this.tempFeatBorderCoordList.add(coord);
                            if (this.tempFeatBorderCoordList.size() == 1) {
                                this.firstCoord = coord;
                            }
                            --i;
                        }
                    }
                    this.bulge = 0.0;
                }
                if (this.hasFaces) {
                    int i;
                    ArrayList<Coordinate> tempLine1 = new ArrayList<Coordinate>();
                    ArrayList<Coordinate> tempLine2 = new ArrayList<Coordinate>();
                    ArrayList<Coordinate> lineCoords = new ArrayList<Coordinate>();
                    for (int[] face : this.faces) {
                        int i0 = face[3];
                        i = 0;
                        while (i < 4) {
                            int i1 = face[i];
                            if (i0 > 0) {
                                if (this.facesIterador % 2 != 0) {
                                    tempLine1.add(this.tempFeatBorderCoordList.get(i0 - 1));
                                } else {
                                    tempLine2.add(this.tempFeatBorderCoordList.get(i0 - 1));
                                }
                                ++this.facesIterador;
                            }
                            i0 = i1;
                            ++i;
                        }
                    }
                    this.facesFirstCoord = new Coordinate(((Coordinate)tempLine1.get((int)0)).x, ((Coordinate)tempLine1.get((int)0)).y, ((Coordinate)tempLine1.get((int)0)).z);
                    i = 0;
                    while (i < tempLine1.size()) {
                        lineCoords.add((Coordinate)tempLine1.get(i));
                        ++i;
                    }
                    i = tempLine2.size() - 1;
                    while (i > 0) {
                        lineCoords.add((Coordinate)tempLine2.get(i));
                        --i;
                    }
                    lineCoords.add(this.facesFirstCoord);
                    this.tempFeatBorderCoordList = lineCoords;
                }
                this.completeAttributes(this.tempFeatBorder);
                this.setPolylineElevation(this.tempFeatBorder);
                if (this.tempFeatBorderCoordList.size() == 1) {
                    this.tempFeatBorderCoordList.add((Coordinate)this.tempFeatBorderCoordList.get(0).clone());
                }
                LineString l = this.geomFact.createLineString(this.tempFeatBorderCoordList.toArray(new Coordinate[0]));
                this.tempFeatBorder.setGeometry((Geometry)l);
                if (this.addingToBlock) {
                    this.currentBlock.addFeature(this.tempFeatBorder);
                } else {
                    this.getDatasetByName(this.tempFeatBorder).addWithNewKey(this.tempFeatBorder);
                }
                this.tempFeatBorder = null;
                this.tempFeatBorderCoordList = null;
            }
        }
        this.xtruX = 0.0;
        this.xtruY = 0.0;
        this.xtruZ = 1.0;
        this.bulge = 0.0;
        this.isDoubleFeatured = false;
        this.hasFaces = false;
        this.facesIterador = 1;
    }

    protected void createLwPolyline(DxfGroupVector gv) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double elev = 0.0;
        DxfGroup g = null;
        Feature featBorder = this.buildBasicFeature();
        featBorder = this.fillBasicAttributes(featBorder, gv, "LwPolyline");
        Feature featBackground = this.buildBasicFeature();
        featBackground = this.fillBasicAttributes(featBackground, gv, "LwPolyline");
        int flags = 0;
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        if (gv.hasCode(38)) {
            elev = gv.getDataAsDouble(38);
            if (elev != 0.0) {
                this.getDatasetByName(featBorder).set3d(true);
                featBorder.setAttribute("FShape", (Object)new String("Polyline3D"));
                featBackground.setAttribute("FShape", (Object)new String("Polygon3D"));
            } else {
                featBorder.setAttribute("FShape", (Object)new String("Polyline2D"));
                featBackground.setAttribute("FShape", (Object)new String("Polygon2D"));
            }
            featBorder.setAttribute("Elevation", (Object)elev);
            featBackground.setAttribute("Elevation", (Object)elev);
        } else {
            featBorder.setAttribute("Elevation", (Object)0.0);
            featBackground.setAttribute("Elevation", (Object)0.0);
            featBorder.setAttribute("FShape", (Object)new String("Polyline2D"));
            featBackground.setAttribute("FShape", (Object)new String("Polygon2D"));
        }
        if (gv.hasCode(70)) {
            flags = gv.getDataAsInt(70);
        }
        this.isDoubleFeatured = flags & true;
        if (gv.hasCode(210)) {
            extx = gv.getDataAsDouble(210);
        }
        if (gv.hasCode(220)) {
            exty = gv.getDataAsDouble(220);
        }
        if (gv.hasCode(230)) {
            extz = gv.getDataAsDouble(230);
        }
        int j = 0;
        double firstX = 0.0;
        double firstY = 0.0;
        boolean hasBulge = false;
        double bulgeLwp = 0.0;
        this.tempFeatBorderCoordList = new ArrayList<Coordinate>();
        this.tempFeatBackgroundCoordList = new ArrayList<Coordinate>();
        int i = 0;
        while (i < gv.size()) {
            g = (DxfGroup)gv.get(i);
            if (g.getCode() == 10) {
                ++j;
                x = (Double)g.getData();
            } else if (g.getCode() == 20) {
                Coordinate vertexCoord;
                y = (Double)g.getData();
                Point3D point_in1 = new Point3D(x, y, elev);
                Point3D xtru = new Point3D(extx, exty, extz);
                Point3D point_out1 = DxfCalXtru.CalculateXtru((Point3D)point_in1, (Point3D)xtru);
                x = point_out1.getX();
                y = point_out1.getY();
                elev = point_out1.getZ();
                if (hasBulge) {
                    vertexCoord = new Coordinate(x, y, elev);
                    if (this.tempFeatBorderCoordList.get((int)(this.tempFeatBorderCoordList.size() - 1)).x != vertexCoord.x || this.tempFeatBorderCoordList.get((int)(this.tempFeatBorderCoordList.size() - 1)).y != vertexCoord.y) {
                        Coordinate coord2;
                        Coordinate coord;
                        int k;
                        Vector<Point2D> arc = this.createArc(this.tempFeatBorderCoordList.get(this.tempFeatBorderCoordList.size() - 1), vertexCoord, bulgeLwp);
                        this.tempFeatBorderCoordList.remove(this.tempFeatBorderCoordList.size() - 1);
                        if (this.isDoubleFeatured) {
                            this.tempFeatBackgroundCoordList.remove(this.tempFeatBackgroundCoordList.size() - 1);
                        }
                        if (bulgeLwp > 0.0) {
                            k = 0;
                            while (k < arc.size()) {
                                coord = new Coordinate(arc.get(k).getX(), arc.get(k).getY(), elev);
                                this.tempFeatBorderCoordList.add(coord);
                                if (this.isDoubleFeatured) {
                                    coord2 = (Coordinate)coord.clone();
                                    this.tempFeatBackgroundCoordList.add(coord2);
                                }
                                if (this.tempFeatBorderCoordList.size() == 1) {
                                    this.firstCoord = coord;
                                }
                                ++k;
                            }
                        } else {
                            k = arc.size() - 1;
                            while (k >= 0) {
                                coord = new Coordinate(arc.get(k).getX(), arc.get(k).getY(), elev);
                                this.tempFeatBorderCoordList.add(coord);
                                if (this.isDoubleFeatured) {
                                    coord2 = (Coordinate)coord.clone();
                                    this.tempFeatBackgroundCoordList.add(coord2);
                                }
                                if (this.tempFeatBorderCoordList.size() == 1) {
                                    this.firstCoord = coord;
                                }
                                --k;
                            }
                        }
                    }
                    hasBulge = false;
                    bulgeLwp = 0.0;
                } else {
                    vertexCoord = new Coordinate(x, y, elev);
                    this.tempFeatBorderCoordList.add(vertexCoord);
                    if (this.isDoubleFeatured) {
                        Coordinate coord2 = (Coordinate)vertexCoord.clone();
                        this.tempFeatBackgroundCoordList.add(coord2);
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
            Coordinate coord = new Coordinate(firstX, firstY, elev);
            if (this.tempFeatBorderCoordList == null) {
                this.tempFeatBorderCoordList = new ArrayList<Coordinate>();
            }
            this.tempFeatBorderCoordList.add(coord);
            if (this.tempFeatBackgroundCoordList == null) {
                this.tempFeatBackgroundCoordList = new ArrayList<Coordinate>();
            }
            this.tempFeatBackgroundCoordList.add(coord);
        }
        if (this.tempFeatBorderCoordList.size() == 1) {
            this.tempFeatBorderCoordList.add((Coordinate)this.tempFeatBorderCoordList.get(0).clone());
        }
        LineString l = this.geomFact.createLineString(this.tempFeatBorderCoordList.toArray(new Coordinate[0]));
        featBorder.setGeometry((Geometry)l);
        if (this.isDoubleFeatured) {
            if (!this.tempFeatBackgroundCoordList.get(0).equals3D(this.tempFeatBackgroundCoordList.get(this.tempFeatBackgroundCoordList.size() - 1))) {
                this.tempFeatBackgroundCoordList.add(this.tempFeatBackgroundCoordList.get(0));
            }
            while (this.tempFeatBackgroundCoordList.size() < 4) {
                this.tempFeatBackgroundCoordList.add(this.tempFeatBackgroundCoordList.get(0));
            }
            Polygon p = this.geomFact.createPolygon(this.tempFeatBackgroundCoordList.toArray(new Coordinate[0]));
            featBackground.setGeometry((Geometry)p);
        }
        this.tempFeatBorder = featBorder;
        if (this.isDoubleFeatured) {
            this.tempFeatBackground = featBackground;
        }
        this.completeAttributes(featBorder);
        this.completeAttributes(featBackground);
        if (this.addingToBlock) {
            if (this.isDoubleFeatured) {
                this.currentBlock.addFeature(featBackground);
            } else {
                this.currentBlock.addFeature(featBorder);
            }
        } else if (this.isDoubleFeatured) {
            this.getDatasetByName(featBackground).addWithNewKey(featBackground);
        } else {
            this.getDatasetByName(featBorder).addWithNewKey(featBorder);
        }
        this.isDoubleFeatured = false;
    }

    protected void createLine(DxfGroupVector gv) throws Exception {
        double x1 = 0.0;
        double y1 = 0.0;
        double z1 = 0.0;
        double x2 = 0.0;
        double y2 = 0.0;
        double z2 = 0.0;
        double elev = 0.0;
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        Feature feat = this.buildBasicFeature();
        feat = this.fillBasicAttributes(feat, gv, "Line");
        x1 = gv.getDataAsDouble(10);
        y1 = gv.getDataAsDouble(20);
        z1 = gv.getDataAsDouble(30);
        x2 = gv.getDataAsDouble(11);
        y2 = gv.getDataAsDouble(21);
        z2 = gv.getDataAsDouble(31);
        if (gv.hasCode(210)) {
            extx = gv.getDataAsDouble(210);
        }
        if (gv.hasCode(220)) {
            exty = gv.getDataAsDouble(220);
        }
        if (gv.hasCode(230)) {
            extz = gv.getDataAsDouble(230);
        }
        Point3D point_in1 = new Point3D(x1, y1, z1);
        Point3D point_in2 = new Point3D(x2, y2, z2);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out1 = DxfCalXtru.CalculateXtru((Point3D)point_in1, (Point3D)xtru);
        Point3D point_out2 = DxfCalXtru.CalculateXtru((Point3D)point_in2, (Point3D)xtru);
        if (point_out1.getZ() != 0.0) {
            this.getDatasetByName(feat).set3d(true);
            feat.setAttribute("FShape", (Object)new String("Polyline3D"));
        } else {
            feat.setAttribute("FShape", (Object)new String("Polyline2D"));
        }
        if (point_out2.getZ() != 0.0) {
            feat.setAttribute("FShape", (Object)new String("Polyline3D"));
        }
        if (point_out1.getZ() == point_out2.getZ()) {
            elev = z1;
            feat.setAttribute("Elevation", (Object)elev);
        } else {
            feat.setAttribute("Elevation", (Object)0.0);
        }
        Coordinate startCoord = new Coordinate(point_out1.getX(), point_out1.getY(), point_out1.getZ());
        Coordinate endCoord = new Coordinate(point_out2.getX(), point_out2.getY(), point_out2.getZ());
        feat.setGeometry((Geometry)this.geomFact.createLineString(new Coordinate[]{startCoord, endCoord}));
        this.completeAttributes(feat);
        if (this.addingToBlock) {
            this.currentBlock.addFeature(feat);
        } else {
            this.getDatasetByName(feat).addWithNewKey(feat);
        }
    }

    protected void createCircle(DxfGroupVector gv) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double r = 0.0;
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        Feature featBorder = this.buildBasicFeature();
        featBorder = this.fillBasicAttributes(featBorder, gv, "Circle");
        Feature featBackground = this.buildBasicFeature();
        featBackground = this.fillBasicAttributes(featBackground, gv, "Circle");
        x = gv.getDataAsDouble(10);
        y = gv.getDataAsDouble(20);
        if (gv.hasCode(30)) {
            z = gv.getDataAsDouble(30);
        }
        if (gv.hasCode(40)) {
            r = gv.getDataAsDouble(40);
        }
        if (gv.hasCode(210)) {
            extx = gv.getDataAsDouble(210);
        }
        if (gv.hasCode(220)) {
            exty = gv.getDataAsDouble(220);
        }
        if (gv.hasCode(230)) {
            extz = gv.getDataAsDouble(230);
        }
        Point3D point_in = new Point3D(x, y, z);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        z = point_out.getZ();
        featBorder.setAttribute("Elevation", (Object)z);
        featBackground.setAttribute("Elevation", (Object)z);
        if (z != 0.0) {
            this.getDatasetByName(featBorder).set3d(true);
            featBorder.setAttribute("FShape", (Object)new String("LineString3D"));
            featBackground.setAttribute("FShape", (Object)new String("Polygon3D"));
        } else {
            featBorder.setAttribute("FShape", (Object)new String("LineString2D"));
            featBackground.setAttribute("FShape", (Object)new String("Polygon2D"));
        }
        Point3D center = new Point3D(x, y, z);
        Point3D[] pts = new Point3D[360];
        int angulo = 0;
        angulo = 0;
        while (angulo < 360) {
            pts[angulo] = new Point3D(center.getX(), center.getY(), center.getZ());
            pts[angulo] = new Point3D(pts[angulo].getX() + r * Math.sin((double)angulo * Math.PI / 180.0), pts[angulo].getY() + r * Math.cos((double)angulo * Math.PI / 180.0), center.getZ());
            ++angulo;
        }
        this.tempFeatBorderCoordList = new ArrayList<Coordinate>();
        this.tempFeatBackgroundCoordList = new ArrayList<Coordinate>();
        int i = 0;
        while (i < pts.length) {
            this.tempFeatBorderCoordList.add(new Coordinate(pts[i].getX(), pts[i].getY(), pts[i].getZ()));
            this.tempFeatBackgroundCoordList.add(new Coordinate(pts[i].getX(), pts[i].getY(), pts[i].getZ()));
            ++i;
        }
        this.tempFeatBorderCoordList.add(new Coordinate(pts[0].getX(), pts[0].getY(), pts[0].getZ()));
        this.tempFeatBackgroundCoordList.add(new Coordinate(pts[0].getX(), pts[0].getY(), pts[0].getZ()));
        LineString line = this.geomFact.createLineString(this.tempFeatBorderCoordList.toArray(new Coordinate[0]));
        featBorder.setGeometry((Geometry)line);
        Circle circle = this.geomFact.createCircle(this.geomFact.createLinearRing(this.tempFeatBackgroundCoordList.toArray(new Coordinate[0])));
        featBackground.setGeometry((Geometry)circle);
        this.completeAttributes(featBorder);
        this.completeAttributes(featBackground);
        if (this.addingToBlock) {
            this.currentBlock.addFeature(featBackground);
        } else {
            this.getDatasetByName(featBackground).addWithNewKey(featBackground);
        }
        this.tempFeatBorderCoordList = null;
        this.tempFeatBackgroundCoordList = null;
    }

    protected void createEllipse(DxfGroupVector gv) throws Exception {
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
        Feature featBorder = this.buildBasicFeature();
        featBorder = this.fillBasicAttributes(featBorder, gv, "Ellipse");
        Feature featBackground = this.buildBasicFeature();
        featBackground = this.fillBasicAttributes(featBackground, gv, "Ellipse");
        cx = gv.getDataAsDouble(10);
        cy = gv.getDataAsDouble(20);
        if (gv.hasCode(30)) {
            cz = gv.getDataAsDouble(30);
        }
        x_end_point_major_axis = gv.getDataAsDouble(11);
        y_end_point_major_axis = gv.getDataAsDouble(21);
        if (gv.hasCode(31)) {
            z_end_point_major_axis = gv.getDataAsDouble(31);
        }
        if (gv.hasCode(40)) {
            ratio_minor_to_major_axis = gv.getDataAsDouble(40);
        }
        double d = ratio_minor_to_major_axis * Math.sqrt(x_end_point_major_axis * x_end_point_major_axis + y_end_point_major_axis * y_end_point_major_axis);
        double initx = cx - x_end_point_major_axis;
        double inity = cy - y_end_point_major_axis;
        double endx = cx + x_end_point_major_axis;
        double endy = cy + y_end_point_major_axis;
        double r_major_axis_2D = Math.sqrt(x_end_point_major_axis * x_end_point_major_axis + y_end_point_major_axis * y_end_point_major_axis);
        double r_minor_axis_2D = r_major_axis_2D * ratio_minor_to_major_axis;
        double rotation_angle = Math.atan2(y_end_point_major_axis, x_end_point_major_axis);
        if (gv.hasCode(210)) {
            extx = gv.getDataAsDouble(210);
        }
        if (gv.hasCode(220)) {
            exty = gv.getDataAsDouble(220);
        }
        if (gv.hasCode(230)) {
            extz = gv.getDataAsDouble(230);
        }
        Point3D point_in = new Point3D(cx, cy, cz);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
        cx = point_out.getX();
        cy = point_out.getY();
        cz = point_out.getZ();
        featBorder.setAttribute("Elevation", (Object)cz);
        featBackground.setAttribute("Elevation", (Object)cz);
        if (cz != 0.0) {
            this.getDatasetByName(featBorder).set3d(true);
            featBorder.setAttribute("FShape", (Object)new String("LineString3D"));
            featBackground.setAttribute("FShape", (Object)new String("Polygon3D"));
        } else {
            featBorder.setAttribute("FShape", (Object)new String("LineString2D"));
            featBackground.setAttribute("FShape", (Object)new String("Polygon2D"));
        }
        Point3D center = new Point3D(cx, cy, cz);
        Point3D[] pts = new Point3D[360];
        int angulo = 0;
        angulo = 0;
        while (angulo < 360) {
            pts[angulo] = new Point3D(center.getX() + r_major_axis_2D * Math.cos(Math.toRadians(angulo)), center.getY() + r_minor_axis_2D * Math.sin(Math.toRadians(angulo)), center.getZ());
            ++angulo;
        }
        this.tempFeatBorderCoordList = new ArrayList<Coordinate>();
        this.tempFeatBackgroundCoordList = new ArrayList<Coordinate>();
        int i = 0;
        while (i < pts.length) {
            this.tempFeatBorderCoordList.add(new Coordinate(pts[i].getX(), pts[i].getY(), pts[i].getZ()));
            this.tempFeatBackgroundCoordList.add(new Coordinate(pts[i].getX(), pts[i].getY(), pts[i].getZ()));
            ++i;
        }
        AffineTransform at = new AffineTransform();
        at.rotate(rotation_angle, cx, cy);
        int i2 = 0;
        while (i2 < pts.length) {
            Point3D pAux = pts[i2];
            Point2D pRot = at.transform((Point2D)pAux, null);
            this.tempFeatBorderCoordList.add(new Coordinate(pRot.getX(), pRot.getY(), cz));
            this.tempFeatBackgroundCoordList.add(new Coordinate(pRot.getX(), pRot.getY(), cz));
            ++i2;
        }
        this.tempFeatBorderCoordList.add(new Coordinate(pts[0].getX(), pts[0].getY(), pts[0].getZ()));
        this.tempFeatBackgroundCoordList.add(new Coordinate(pts[0].getX(), pts[0].getY(), pts[0].getZ()));
        LineString line = this.geomFact.createLineString(this.tempFeatBorderCoordList.toArray(new Coordinate[0]));
        featBorder.setGeometry((Geometry)line);
        Ellipse ellipse = this.geomFact.createEllipse(this.geomFact.createLinearRing(this.tempFeatBackgroundCoordList.toArray(new Coordinate[0])), d, new Point2D.Double(initx, inity), new Point2D.Double(endx, endy));
        featBackground.setGeometry((Geometry)ellipse);
        this.completeAttributes(featBorder);
        this.completeAttributes(featBackground);
        if (this.addingToBlock) {
            this.currentBlock.addFeature(featBackground);
        } else {
            this.getDatasetByName(featBackground).addWithNewKey(featBackground);
        }
        this.tempFeatBorderCoordList = null;
        this.tempFeatBackgroundCoordList = null;
    }

    protected void createArc(DxfGroupVector gv) throws Exception {
        int i;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double r = 0.0;
        double empieza = 0.0;
        double acaba = 0.0;
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        Feature feat = this.buildBasicFeature();
        feat = this.fillBasicAttributes(feat, gv, "Arc");
        x = gv.getDataAsDouble(10);
        y = gv.getDataAsDouble(20);
        if (gv.hasCode(30)) {
            z = gv.getDataAsDouble(30);
        }
        if (gv.hasCode(40)) {
            r = gv.getDataAsDouble(40);
        }
        if (gv.hasCode(50)) {
            empieza = gv.getDataAsDouble(50);
        }
        if (gv.hasCode(51)) {
            acaba = gv.getDataAsDouble(51);
        }
        if (gv.hasCode(210)) {
            extx = gv.getDataAsDouble(210);
        }
        if (gv.hasCode(220)) {
            exty = gv.getDataAsDouble(220);
        }
        if (gv.hasCode(230)) {
            extz = gv.getDataAsDouble(230);
        }
        Point3D point_in = new Point3D(x, y, z);
        Point3D xtru = new Point3D(extx, exty, extz);
        Point3D point_out = DxfCalXtru.CalculateXtru((Point3D)point_in, (Point3D)xtru);
        x = point_out.getX();
        y = point_out.getY();
        z = point_out.getZ();
        feat.setAttribute("Elevation", (Object)z);
        if (z != 0.0) {
            this.getDatasetByName(feat).set3d(true);
            feat.setAttribute("FShape", (Object)new String("Polyline3D"));
        } else {
            feat.setAttribute("FShape", (Object)new String("Polyline2D"));
        }
        Point3D center = new Point3D(x, y, z);
        int iempieza = (int)empieza;
        int iacaba = (int)acaba;
        double angulo = 0.0;
        Point3D[] pts = null;
        if (empieza <= acaba) {
            pts = new Point3D[iacaba - iempieza + 2];
            angulo = empieza;
            pts[0] = new Point3D(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0), center.getZ());
            i = 1;
            while (i <= iacaba - iempieza + 1) {
                angulo = iempieza + i;
                pts[i] = new Point3D(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0), center.getZ());
                ++i;
            }
            angulo = acaba;
            pts[iacaba - iempieza + 1] = new Point3D(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0), center.getZ());
        } else {
            pts = new Point3D[360 - iempieza + iacaba + 2];
            angulo = empieza;
            pts[0] = new Point3D(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0), center.getZ());
            i = 1;
            while (i <= 360 - iempieza) {
                angulo = iempieza + i;
                pts[i] = new Point3D(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0), center.getZ());
                ++i;
            }
            i = 360 - iempieza + 1;
            while (i <= 360 - iempieza + iacaba) {
                angulo = i - (360 - iempieza);
                pts[i] = new Point3D(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0), center.getZ());
                ++i;
            }
            angulo = acaba;
            pts[360 - iempieza + iacaba + 1] = new Point3D(center.getX() + r * Math.cos(angulo * Math.PI / 180.0), center.getY() + r * Math.sin(angulo * Math.PI / 180.0), center.getZ());
        }
        this.tempFeatBorderCoordList = new ArrayList<Coordinate>();
        i = 0;
        while (i < pts.length) {
            this.tempFeatBorderCoordList.add(new Coordinate(pts[i].getX(), pts[i].getY(), pts[i].getZ()));
            ++i;
        }
        Arc arc = this.geomFact.createArc((CoordinateSequence)new CoordinateArraySequence(this.tempFeatBorderCoordList.toArray(new Coordinate[0])));
        feat.setGeometry((Geometry)arc);
        this.completeAttributes(feat);
        if (this.addingToBlock) {
            this.currentBlock.addFeature(feat);
        } else {
            this.getDatasetByName(feat).addWithNewKey(feat);
        }
        this.tempFeatBorderCoordList = null;
    }

    protected void createSolid(DxfGroupVector gv) throws Exception {
        Point3D pto3D;
        double x = 0.0;
        double y = 0.0;
        double z1 = 0.0;
        double z2 = 0.0;
        double z3 = 0.0;
        double z4 = 0.0;
        Point3D[] pts = new Point3D[4];
        double extx = 0.0;
        double exty = 0.0;
        double extz = 1.0;
        Feature featBorder = this.buildBasicFeature();
        featBorder = this.fillBasicAttributes(featBorder, gv, "Solid");
        Feature featBackground = this.buildBasicFeature();
        featBackground = this.fillBasicAttributes(featBackground, gv, "Solid");
        x = gv.getDataAsDouble(10);
        y = gv.getDataAsDouble(20);
        z1 = gv.getDataAsDouble(30);
        pts[0] = pto3D = new Point3D(x, y, z1);
        x = gv.getDataAsDouble(11);
        y = gv.getDataAsDouble(21);
        z2 = gv.getDataAsDouble(31);
        pts[1] = pto3D = new Point3D(x, y, z2);
        x = gv.getDataAsDouble(12);
        y = gv.getDataAsDouble(22);
        z3 = gv.getDataAsDouble(32);
        pts[2] = pto3D = new Point3D(x, y, z3);
        if (gv.hasCode(13)) {
            x = gv.getDataAsDouble(13);
        }
        if (gv.hasCode(23)) {
            y = gv.getDataAsDouble(23);
        }
        if (gv.hasCode(33)) {
            z4 = gv.getDataAsDouble(33);
        }
        pts[3] = pto3D = new Point3D(x, y, z4);
        if (gv.hasCode(210)) {
            extx = gv.getDataAsDouble(210);
        }
        if (gv.hasCode(220)) {
            exty = gv.getDataAsDouble(220);
        }
        if (gv.hasCode(230)) {
            extz = gv.getDataAsDouble(230);
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
        pts[0] = new Point3D(point_out1);
        pts[1] = new Point3D(point_out2);
        pts[2] = new Point3D(point_out3);
        pts[3] = new Point3D(point_out4);
        if (pts[0].getZ() != 0.0 || pts[1].getZ() != 0.0 || pts[2].getZ() != 0.0 || pts[3].getZ() != 0.0) {
            this.getDatasetByName(featBorder).set3d(true);
            featBorder.setAttribute("FShape", (Object)new String("Polyline3D"));
            featBackground.setAttribute("FShape", (Object)new String("Polygon3D"));
        } else {
            featBorder.setAttribute("FShape", (Object)new String("Polyline2D"));
            featBackground.setAttribute("FShape", (Object)new String("Polygon2D"));
        }
        Point3D aux = new Point3D(pts[2]);
        pts[2] = new Point3D(pts[3]);
        pts[3] = aux;
        Double doub = new Double(0.0);
        if (pts[0].getZ() == pts[1].getZ() && pts[1].getZ() == pts[2].getZ() && pts[2].getZ() == pts[3].getZ()) {
            doub = new Double(pts[0].getZ());
        }
        featBorder.setAttribute("Elevation", (Object)doub);
        featBackground.setAttribute("Elevation", (Object)doub);
        this.tempFeatBorderCoordList = new ArrayList<Coordinate>();
        this.tempFeatBackgroundCoordList = new ArrayList<Coordinate>();
        int i = 0;
        while (i < pts.length) {
            this.tempFeatBorderCoordList.add(new Coordinate(pts[i].getX(), pts[i].getY(), pts[i].getZ()));
            this.tempFeatBackgroundCoordList.add(new Coordinate(pts[i].getX(), pts[i].getY(), pts[i].getZ()));
            ++i;
        }
        this.tempFeatBorderCoordList.add(new Coordinate(pts[0].getX(), pts[0].getY(), pts[0].getZ()));
        this.tempFeatBackgroundCoordList.add(new Coordinate(pts[0].getX(), pts[0].getY(), pts[0].getZ()));
        LineString line = this.geomFact.createLineString(this.tempFeatBorderCoordList.toArray(new Coordinate[0]));
        featBorder.setGeometry((Geometry)line);
        Polygon polygon = this.geomFact.createPolygon(this.tempFeatBackgroundCoordList.toArray(new Coordinate[0]));
        featBackground.setGeometry((Geometry)polygon);
        this.completeAttributes(featBorder);
        this.completeAttributes(featBackground);
        if (this.addingToBlock) {
            this.currentBlock.addFeature(featBackground);
        } else {
            this.getDatasetByName(featBackground).addWithNewKey(featBackground);
        }
    }

    protected void createSpline(DxfGroupVector gv) throws Exception {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        int flags = 0;
        DxfGroup g = null;
        Feature featBorder = this.buildBasicFeature();
        featBorder = this.fillBasicAttributes(featBorder, gv, "Spline");
        Feature featBackground = this.buildBasicFeature();
        featBackground = this.fillBasicAttributes(featBackground, gv, "Spline");
        if (gv.hasCode(70)) {
            flags = gv.getDataAsInt(70);
        }
        this.isDoubleFeatured = flags & true;
        int j = 0;
        double firstX = 0.0;
        double firstY = 0.0;
        double firstZ = 0.0;
        this.tempFeatBorderCoordList = new ArrayList<Coordinate>();
        this.tempFeatBackgroundCoordList = new ArrayList<Coordinate>();
        int i = 0;
        while (i < gv.size()) {
            g = (DxfGroup)gv.get(i);
            if (g.getCode() == 10) {
                ++j;
                x = (Double)g.getData();
            } else if (g.getCode() == 20) {
                y = (Double)g.getData();
            } else if (g.getCode() == 30) {
                z = (Double)g.getData();
                Coordinate coord = new Coordinate(x, y, z);
                this.tempFeatBorderCoordList.add(coord);
                if (this.isDoubleFeatured) {
                    this.tempFeatBackgroundCoordList.add((Coordinate)coord.clone());
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
            Coordinate coord = new Coordinate(firstX, firstY, firstZ);
            this.tempFeatBorderCoordList.add(coord);
            this.tempFeatBackgroundCoordList.add((Coordinate)coord.clone());
        }
        double zprev = 0.0;
        boolean constSplineElev = true;
        int i2 = 0;
        while (i2 < this.tempFeatBorderCoordList.size()) {
            z = this.tempFeatBorderCoordList.get((int)i2).z;
            if (z != 0.0) {
                this.getDatasetByName(featBorder).set3d(true);
            }
            if (i2 > 0 && z != zprev) {
                constSplineElev = false;
            }
            zprev = z;
            ++i2;
        }
        if (constSplineElev) {
            featBorder.setAttribute("Elevation", (Object)z);
            featBackground.setAttribute("Elevation", (Object)z);
            featBorder.setAttribute("FShape", (Object)new String("Polyline3D"));
            featBackground.setAttribute("FShape", (Object)new String("Polygon3D"));
        } else {
            Double doub = 0.0;
            featBorder.setAttribute("Elevation", (Object)doub);
            featBackground.setAttribute("Elevation", (Object)doub);
            featBorder.setAttribute("FShape", (Object)new String("Polyline2D"));
            featBackground.setAttribute("FShape", (Object)new String("Polygon2D"));
        }
        LineString line = this.geomFact.createLineString(this.tempFeatBorderCoordList.toArray(new Coordinate[0]));
        featBorder.setGeometry((Geometry)line);
        Polygon polygon = this.geomFact.createPolygon(this.tempFeatBackgroundCoordList.toArray(new Coordinate[0]));
        featBackground.setGeometry((Geometry)polygon);
        this.tempFeatBorder = featBorder;
        if (this.isDoubleFeatured) {
            this.tempFeatBackground = featBackground;
        }
        this.completeAttributes(featBorder);
        this.completeAttributes(featBackground);
        if (this.addingToBlock) {
            if (this.isDoubleFeatured) {
                this.currentBlock.addFeature(featBackground);
            } else {
                this.currentBlock.addFeature(featBorder);
            }
        } else if (this.isDoubleFeatured) {
            this.getDatasetByName(featBackground).addWithNewKey(featBackground);
        } else {
            this.getDatasetByName(featBorder).addWithNewKey(featBorder);
        }
        this.isDoubleFeatured = false;
    }

    protected void createBlock(DxfGroupVector gv) throws Exception {
        DxfBlock block = new DxfBlock();
        this.addingToBlock = true;
        Feature feat = this.buildBasicFeature();
        feat = this.fillBasicAttributes(feat, gv, "Unknown");
        if (gv.hasCode(1)) {
            block.setName(gv.getDataAsString(1));
        }
        if (gv.hasCode(2)) {
            block.setName(gv.getDataAsString(2));
        }
        if (gv.hasCode(10)) {
            block.setBasePointX(gv.getDataAsDouble(10));
        }
        if (gv.hasCode(20)) {
            block.setBasePointY(gv.getDataAsDouble(20));
        }
        if (gv.hasCode(30)) {
            Double basePointZ = gv.getDataAsDouble(30);
            if (basePointZ != 0.0) {
                this.getDatasetByName(feat).set3d(true);
            }
            block.setBasePointZ(basePointZ);
        }
        if (gv.hasCode(70)) {
            Integer blockFlags = new Integer(gv.getDataAsInt(70));
            block.setFlags(blockFlags);
        }
        this.blocks.put(block.getName(), block);
        this.currentBlock = block;
    }

    protected void endBlk(DxfGroupVector grp) throws Exception {
        this.addingToBlock = false;
        this.currentBlock = null;
    }

    protected void setPolylineElevation(Feature f) {
        if (this.constantPolylineElevation) {
            Double doub = new Double(this.lastVertexElevation);
            f.setAttribute("Elevation", (Object)doub);
        } else {
            f.setAttribute("Elevation", (Object)0.0);
        }
    }

    protected Feature buildBasicFeature() {
        BasicFeature f = new BasicFeature(this.baseSchema);
        f.setAttribute("HeightText", (Object)0.0);
        f.setAttribute("RotationText", (Object)0.0);
        return f;
    }

    @Override
    public Hashtable<String, Set<Integer>> getLayerToColor() {
        return this.layerToColor;
    }

    protected void manageInsert(Feature f, String blockName, Point3D scaleFactor, double rAngleGra) throws Exception {
        DxfBlock block = this.blocks.get(blockName);
        double bPointX = block.getBasePointX();
        double bPointY = block.getBasePointY();
        double bPointZ = block.getBasePointZ();
        double sFactorX = scaleFactor.getX();
        double sFactorY = scaleFactor.getY();
        double sFactorZ = scaleFactor.getZ();
        double rAngleRad = rAngleGra * Math.PI / 180.0;
        Point insertPoint = (Point)f.getGeometry();
        List<Feature> blockFeats = block.getFeatures();
        BasicFeature feature2 = null;
        int i = 0;
        while (i < blockFeats.size()) {
            Coordinate[] coords;
            Point geom;
            Feature blockFeat = blockFeats.get(i);
            feature2 = new BasicFeature(this.getDatasetByName(f).getFeatureSchema());
            FeatureUtil.copyOnlyExistentAttributes(blockFeat, feature2, true, false);
            feature2.setAttribute("Layer", f.getAttribute("Layer"));
            feature2.setAttribute("Color", f.getAttribute("Color"));
            block.assignAttributes(feature2, this.getDatasetByName(f).getFeatureSchema());
            for (Attribute attr : block.getAttributes()) {
                feature2.setAttribute(attr.getName(), f.getAttribute(attr.getName()));
            }
            feature2.setAttribute("BlockName", (Object)blockName);
            if (feature2.getGeometry() instanceof Point && feature2.getAttribute("Entity").equals("Insert")) {
                geom = (Point)feature2.getGeometry();
                coords = this.transformCoordinates(insertPoint, new Coordinate[]{geom.getCoordinate()}, bPointX, bPointY, bPointZ, sFactorX, sFactorY, sFactorZ, rAngleRad);
                feature2.setGeometry((Geometry)this.geomFact.createPoint(coords[0]));
                Point3D newScaleFactor = new Point3D(scaleFactor.getX() * sFactorX, scaleFactor.getY() * sFactorY, scaleFactor.getZ() * sFactorZ);
                this.manageInsert(feature2, blockFeat.getString("BlockName"), newScaleFactor, rAngleGra);
            } else if (feature2.getGeometry() instanceof LineString) {
                LineString lineString = (LineString)feature2.getGeometry();
                feature2.setGeometry((Geometry)this.geomFact.createLineString(this.transformCoordinates(insertPoint, lineString.getCoordinates(), bPointX, bPointY, bPointZ, sFactorX, sFactorY, sFactorZ, rAngleRad)));
                if (!this.addingToBlock) {
                    this.getDatasetByName(feature2).addWithNewKey(feature2);
                }
            } else if (feature2.getGeometry() instanceof Polygon) {
                Polygon polygon = (Polygon)feature2.getGeometry();
                Coordinate[] extRingNewCoords = this.transformCoordinates(insertPoint, polygon.getExteriorRing().getCoordinates(), bPointX, bPointY, bPointZ, sFactorX, sFactorY, sFactorZ, rAngleRad);
                LinearRing shell = this.geomFact.createLinearRing(extRingNewCoords);
                LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
                int j = 0;
                while (j < polygon.getNumInteriorRing()) {
                    LineString interiorRing = polygon.getInteriorRingN(j);
                    Coordinate[] intRingNewCoords = this.transformCoordinates(insertPoint, interiorRing.getCoordinates(), bPointX, bPointY, bPointZ, sFactorX, sFactorY, sFactorZ, rAngleRad);
                    holes[j] = this.geomFact.createLinearRing(intRingNewCoords);
                    ++j;
                }
                feature2.setGeometry((Geometry)this.geomFact.createPolygon(shell, holes));
                if (!this.addingToBlock) {
                    this.getDatasetByName(feature2).addWithNewKey(feature2);
                }
            } else if (feature2.getGeometry() instanceof Point) {
                geom = (Point)feature2.getGeometry();
                coords = this.transformCoordinates(insertPoint, new Coordinate[]{geom.getCoordinate()}, bPointX, bPointY, bPointZ, sFactorX, sFactorY, sFactorZ, rAngleRad);
                feature2.setGeometry((Geometry)this.geomFact.createPoint(coords[0]));
                Double textRotation = (Double)feature2.getAttribute("RotationText");
                if (textRotation != null) {
                    double auxR = textRotation + rAngleGra;
                    feature2.setAttribute("RotationText", (Object)auxR);
                }
                if (!this.addingToBlock) {
                    this.getDatasetByName(feature2).addWithNewKey(feature2);
                }
            } else {
                LOGGER.error((Object)"manageInsert() - Unknown element found");
            }
            ++i;
        }
    }

    protected Coordinate[] transformCoordinates(Point insertPoint, Coordinate[] coords, double bPointX, double bPointY, double bPointZ, double sFactorX, double sFactorY, double sFactorZ, double rAngleRad) {
        Coordinate[] newCoords = new Coordinate[coords.length];
        int i = 0;
        while (i < coords.length) {
            Coordinate currentCood = coords[i];
            Coordinate coordAux = new Coordinate(currentCood.x - bPointX, currentCood.y - bPointY, currentCood.z - bPointZ);
            double laX = insertPoint.getX() + (coordAux.x * sFactorX * Math.cos(rAngleRad) + coordAux.y * sFactorY * -1.0 * Math.sin(rAngleRad));
            double laY = insertPoint.getY() + (coordAux.x * sFactorX * Math.sin(rAngleRad) + coordAux.y * sFactorY * Math.cos(rAngleRad));
            double laZ = insertPoint.getCoordinate().z + coordAux.z * sFactorZ;
            newCoords[i] = new Coordinate(laX, laY, laZ);
            ++i;
        }
        return newCoords;
    }

    protected void addFace(int[] face) {
        this.hasFaces = true;
        if (this.faces == null) {
            this.faces = new Vector();
        }
        this.faces.add(face);
    }

    protected Vector<Point2D> createArc(Coordinate coord1, Coordinate coord2, double bulge) {
        return new DxfCalArcs((Point2D)new Point3D(coord1.x, coord1.y, coord1.z), (Point2D)new Point3D(coord2.x, coord2.y, coord2.z), bulge).getPoints(1.0);
    }

    public boolean isGenerateOneFcByLayer() {
        return this.generateOneFcByLayer;
    }

    public void setGenerateOneFcByLayer(boolean generateOneFcByLayer) {
        this.generateOneFcByLayer = generateOneFcByLayer;
    }

    public boolean isSchemaAttributeNamesToUpperCase() {
        return this.schemaAttributeNamesToUpperCase;
    }

    public void setSchemaAttributeNamesToUpperCase(boolean schemaAttributeNamesToUpperCase) {
        this.schemaAttributeNamesToUpperCase = schemaAttributeNamesToUpperCase;
    }

    public boolean isIgnoreInsertEntities() {
        return this.ignoreInsertEntities;
    }

    public void setIgnoreInsertEntities(boolean ignoreInsertEntities) {
        this.ignoreInsertEntities = ignoreInsertEntities;
    }

    protected void completeAttributes(Feature feature) {
        if (feature == null) {
            return;
        }
        String blockName = feature.getString("BlockName");
        if (StringUtils.isEmpty((String)blockName) || this.blocks.get(blockName) == null) {
            return;
        }
        DxfBlock block = this.blocks.get(blockName);
        block.assignAttributes(feature, this.getDatasetByName(feature).getFeatureSchema());
    }

    public boolean isClassifyFcsByGeometryType() {
        return this.classifyFcsByGeometryType;
    }

    public void setClassifyFcsByGeometryType(boolean classifyFcsByGeometryType) {
        this.classifyFcsByGeometryType = classifyFcsByGeometryType;
    }

    public void setBannedLayerNames(String[] bannedLayerNames) {
        this.bannedLayerNames = bannedLayerNames;
    }

    public void setOutputPointFcSuffix(String outputPointFcSuffix) {
        this.outputPointFcSuffix = outputPointFcSuffix;
    }

    public void setOutputLineFcSuffix(String outputLineFcSuffix) {
        this.outputLineFcSuffix = outputLineFcSuffix;
    }

    public void setOutputPolygonFcSuffix(String outputPolygonFcSuffix) {
        this.outputPolygonFcSuffix = outputPolygonFcSuffix;
    }

    public void setIgnoreEmptyLayers(boolean ignoreEmptyLayers) {
        this.ignoreEmptyLayers = ignoreEmptyLayers;
    }
}

