/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.xerces.parsers.SAXParser
 */
package com.vividsolutions.jump.io;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.CompressedFile;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.GMLInputTemplate;
import com.vividsolutions.jump.io.IllegalParametersException;
import com.vividsolutions.jump.io.JUMPReader;
import com.vividsolutions.jump.io.ParseException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class GMLReader
extends DefaultHandler
implements JUMPReader {
    static int STATE_GET_COLUMNS = 3;
    static int STATE_INIT = 0;
    static int STATE_PARSE_GEOM_NESTED = 1000;
    static int STATE_PARSE_GEOM_SIMPLE = 4;
    static int STATE_WAIT_COLLECTION_TAG = 1;
    static int STATE_WAIT_FEATURE_TAG = 2;
    GMLInputTemplate GMLinput = null;
    int STATE = STATE_INIT;
    Point apoint;
    Feature currentFeature;
    int currentGeometryNumb = 1;
    FeatureCollection fc;
    FeatureSchema fcmd;
    Geometry finalGeometry;
    ArrayList geometry;
    GeometryFactory geometryFactory = new GeometryFactory();
    ArrayList innerBoundaries = new ArrayList();
    Attributes lastStartTag_atts;
    String lastStartTag_name;
    String lastStartTag_qName;
    String lastStartTag_uri;
    LineString lineString;
    LinearRing linearRing;
    LinearRing outerBoundary;
    ArrayList pointList = new ArrayList();
    Polygon polygon;
    ArrayList recursivegeometry = new ArrayList();
    Coordinate singleCoordinate = new Coordinate();
    String streamName;
    String tagBody;
    XMLReader xr = new SAXParser();

    public GMLReader() {
        this.xr.setContentHandler(this);
        this.xr.setErrorHandler(this);
    }

    public void setInputTemplate(GMLInputTemplate template) {
        this.GMLinput = template;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            String part = new String(ch, start, length);
            this.tagBody = String.valueOf(this.tagBody) + part;
        }
        catch (Exception e) {
            throw new SAXException(e.getMessage());
        }
    }

    @Override
    public void endDocument() {
        this.STATE = STATE_INIT;
    }

    @Override
    public void endElement(String uri, String name, String qName) throws SAXException {
        block40: {
            try {
                if (this.STATE == STATE_INIT) {
                    this.tagBody = "";
                    return;
                }
                if (this.STATE > STATE_GET_COLUMNS) {
                    Coordinate[] c;
                    if (this.isMultiGeometryTag(qName)) {
                        if (this.STATE == STATE_PARSE_GEOM_NESTED) {
                            this.STATE = STATE_PARSE_GEOM_SIMPLE;
                        } else {
                            Geometry g = this.geometryFactory.buildGeometry((Collection)this.geometry);
                            this.geometry = (ArrayList)this.recursivegeometry.get(this.STATE - STATE_PARSE_GEOM_NESTED - 1);
                            this.geometry.add(g);
                            this.recursivegeometry.remove(this.STATE - STATE_PARSE_GEOM_NESTED);
                            g = null;
                            --this.STATE;
                        }
                    }
                    if (this.GMLinput.isGeometryElement(qName)) {
                        this.tagBody = "";
                        this.STATE = STATE_GET_COLUMNS;
                        this.finalGeometry = this.geometryFactory.buildGeometry((Collection)this.geometry);
                        this.currentFeature.setGeometry(this.finalGeometry);
                        ++this.currentGeometryNumb;
                        return;
                    }
                    if (qName.compareToIgnoreCase("X") == 0 || qName.compareToIgnoreCase("gml:X") == 0) {
                        this.singleCoordinate.x = new Double(this.tagBody);
                    } else if (qName.compareToIgnoreCase("Y") == 0 || qName.compareToIgnoreCase("gml:y") == 0) {
                        this.singleCoordinate.y = new Double(this.tagBody);
                    } else if (qName.compareToIgnoreCase("Z") == 0 || qName.compareToIgnoreCase("gml:z") == 0) {
                        this.singleCoordinate.z = new Double(this.tagBody);
                    } else if (qName.compareToIgnoreCase("COORD") == 0 || qName.compareToIgnoreCase("gml:coord") == 0) {
                        this.pointList.add(new Coordinate(this.singleCoordinate));
                    } else if (qName.compareToIgnoreCase("COORDINATES") == 0 || qName.compareToIgnoreCase("gml:coordinates") == 0) {
                        this.parsePoints(this.tagBody, this.geometryFactory);
                    } else if (qName.compareToIgnoreCase("linearring") == 0 || qName.compareToIgnoreCase("gml:linearring") == 0) {
                        c = new Coordinate[]{};
                        c = this.pointList.toArray(c);
                        this.linearRing = this.geometryFactory.createLinearRing(c);
                    } else if (qName.compareToIgnoreCase("outerBoundaryIs") == 0 || qName.compareToIgnoreCase("gml:outerBoundaryIs") == 0) {
                        this.outerBoundary = this.linearRing;
                    } else if (qName.compareToIgnoreCase("innerBoundaryIs") == 0 || qName.compareToIgnoreCase("gml:innerBoundaryIs") == 0) {
                        this.innerBoundaries.add(this.linearRing);
                    } else if (qName.compareToIgnoreCase("polygon") == 0 || qName.compareToIgnoreCase("gml:polygon") == 0) {
                        LinearRing[] lrs = new LinearRing[]{};
                        lrs = this.innerBoundaries.toArray(lrs);
                        this.polygon = this.geometryFactory.createPolygon(this.outerBoundary, lrs);
                        this.geometry.add(this.polygon);
                    } else if (qName.compareToIgnoreCase("linestring") == 0 || qName.compareToIgnoreCase("gml:linestring") == 0) {
                        c = new Coordinate[]{};
                        c = this.pointList.toArray(c);
                        this.lineString = this.geometryFactory.createLineString(c);
                        this.geometry.add(this.lineString);
                    } else if (qName.compareToIgnoreCase("point") == 0 || qName.compareToIgnoreCase("gml:point") == 0) {
                        this.apoint = this.geometryFactory.createPoint((Coordinate)this.pointList.get(0));
                        this.geometry.add(this.apoint);
                    }
                    break block40;
                }
                if (this.STATE == STATE_GET_COLUMNS) {
                    if (qName.compareToIgnoreCase(this.GMLinput.featureTag) == 0) {
                        this.tagBody = "";
                        this.STATE = STATE_WAIT_FEATURE_TAG;
                        if (this.currentFeature.getGeometry() == null) {
                            Geometry g = this.currentFeature.getGeometry();
                            if (g != null) {
                                System.out.println(g.toString());
                            }
                            throw new ParseException("no geometry specified in feature");
                        }
                        this.fc.add(this.currentFeature);
                        this.currentFeature = null;
                        return;
                    }
                    try {
                        int index = this.GMLinput.match(this.lastStartTag_qName, this.lastStartTag_atts);
                        if (index > -1) {
                            this.currentFeature.setAttribute(this.GMLinput.columnName(index), this.GMLinput.getColumnValue(index, this.tagBody, this.lastStartTag_atts));
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    this.tagBody = "";
                    break block40;
                }
                if (this.STATE == STATE_WAIT_FEATURE_TAG) {
                    if (qName.compareToIgnoreCase(this.GMLinput.collectionTag) == 0) {
                        this.STATE = STATE_INIT;
                        this.tagBody = "";
                        return;
                    }
                } else if (this.STATE == STATE_WAIT_COLLECTION_TAG) {
                    this.tagBody = "";
                    return;
                }
            }
            catch (Exception e) {
                throw new SAXException(e.getMessage());
            }
        }
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        throw exception;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        throw exception;
    }

    @Override
    public FeatureCollection read(DriverProperties dp) throws IllegalParametersException, Exception {
        GMLInputTemplate gmlTemplate;
        InputStream in;
        boolean isCompressed_template = dp.getProperty("CompressedFileTemplate") != null;
        boolean isCompressed = dp.getProperty("CompressedFile") != null;
        String inputFname = dp.getProperty("File");
        if (inputFname == null) {
            inputFname = dp.getProperty("DefaultValue");
        }
        if (inputFname == null) {
            throw new IllegalParametersException("call to GMLReader.read() has DataProperties w/o a InputFile specified");
        }
        if (dp.getProperty("TemplateFile") == null) {
            if (isCompressed) {
                in = CompressedFile.openFile(inputFname, dp.getProperty("CompressedFile"));
                gmlTemplate = this.inputTemplateFromFile(in);
                in.close();
            } else {
                gmlTemplate = this.inputTemplateFromFile(inputFname);
            }
        } else if (isCompressed_template) {
            in = CompressedFile.openFile(dp.getProperty("TemplateFile"), dp.getProperty("CompressedFileTemplate"));
            gmlTemplate = this.inputTemplateFromFile(in);
            in.close();
        } else if (isCompressed) {
            if (dp.getProperty("CompressedFile").equals(dp.getProperty("TemplateFile"))) {
                in = CompressedFile.openFile(inputFname, dp.getProperty("CompressedFile"));
                gmlTemplate = this.inputTemplateFromFile(in);
                in.close();
            } else {
                gmlTemplate = this.inputTemplateFromFile(dp.getProperty("TemplateFile"));
            }
        } else {
            gmlTemplate = this.inputTemplateFromFile(dp.getProperty("TemplateFile"));
        }
        this.setInputTemplate(gmlTemplate);
        BufferedReader r = isCompressed ? new BufferedReader(new InputStreamReader(CompressedFile.openFile(inputFname, dp.getProperty("CompressedFile")))) : new BufferedReader(new FileReader(inputFname));
        FeatureCollection fc = this.read(r, inputFname);
        ((Reader)r).close();
        return fc;
    }

    public FeatureCollection read(Reader r) throws Exception {
        return this.read(r, "Unknown Stream");
    }

    public FeatureCollection read(Reader r, String readerName) throws Exception {
        LineNumberReader myReader = new LineNumberReader(r);
        if (this.GMLinput == null) {
            throw new ParseException("you must set the GMLinput template first!");
        }
        this.streamName = readerName;
        this.fcmd = this.GMLinput.toFeatureSchema();
        this.fc = new FeatureDataset(this.fcmd);
        try {
            this.xr.parse(new InputSource(myReader));
        }
        catch (SAXParseException e) {
            throw new ParseException(String.valueOf(e.getMessage()) + "  Last Opened Tag: " + this.lastStartTag_qName + ".  Reader reports last line read as " + myReader.getLineNumber(), String.valueOf(this.streamName) + " - " + e.getPublicId() + " (" + e.getSystemId() + ") ", e.getLineNumber(), e.getColumnNumber());
        }
        catch (SAXException e) {
            throw new ParseException(String.valueOf(e.getMessage()) + "  Last Opened Tag: " + this.lastStartTag_qName, this.streamName, myReader.getLineNumber(), 0);
        }
        return this.fc;
    }

    @Override
    public void startDocument() {
        this.tagBody = "";
        this.STATE = STATE_WAIT_COLLECTION_TAG;
    }

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) throws SAXException {
        try {
            this.tagBody = "";
            this.lastStartTag_uri = uri;
            this.lastStartTag_name = name;
            this.lastStartTag_qName = qName;
            this.lastStartTag_atts = atts;
            if (this.STATE == STATE_INIT) {
                return;
            }
            if (this.STATE == STATE_WAIT_COLLECTION_TAG && qName.compareToIgnoreCase(this.GMLinput.collectionTag) == 0) {
                this.STATE = STATE_WAIT_FEATURE_TAG;
                return;
            }
            if (this.STATE == STATE_WAIT_FEATURE_TAG && qName.compareToIgnoreCase(this.GMLinput.featureTag) == 0) {
                this.currentFeature = new BasicFeature(this.fcmd);
                this.STATE = STATE_GET_COLUMNS;
                return;
            }
            if (this.STATE == STATE_GET_COLUMNS && this.GMLinput.isGeometryElement(qName)) {
                this.recursivegeometry = new ArrayList();
                this.geometry = new ArrayList();
                this.recursivegeometry.add(this.geometry);
                this.finalGeometry = null;
                this.STATE = STATE_PARSE_GEOM_SIMPLE;
                return;
            }
            if (this.STATE >= STATE_PARSE_GEOM_SIMPLE && (qName.compareToIgnoreCase("coord") == 0 || qName.compareToIgnoreCase("gml:coord") == 0)) {
                this.singleCoordinate.x = Double.NaN;
                this.singleCoordinate.y = Double.NaN;
                this.singleCoordinate.z = Double.NaN;
            }
            if (this.STATE >= STATE_PARSE_GEOM_SIMPLE && qName.compareToIgnoreCase("X") != 0 && qName.compareToIgnoreCase("gml:x") != 0 && qName.compareToIgnoreCase("y") != 0 && qName.compareToIgnoreCase("gml:y") != 0 && qName.compareToIgnoreCase("z") != 0 && qName.compareToIgnoreCase("gml:z") != 0 && qName.compareToIgnoreCase("coord") != 0 && qName.compareToIgnoreCase("gml:coord") != 0) {
                this.pointList.clear();
            }
            if (this.STATE >= STATE_PARSE_GEOM_SIMPLE && (qName.compareToIgnoreCase("polygon") == 0 || qName.compareToIgnoreCase("gml:polygon") == 0)) {
                this.innerBoundaries.clear();
            }
            if (this.STATE > STATE_GET_COLUMNS && this.isMultiGeometryTag(qName)) {
                if (this.STATE == STATE_PARSE_GEOM_SIMPLE) {
                    this.STATE = STATE_PARSE_GEOM_NESTED;
                } else {
                    ++this.STATE;
                    this.geometry = new ArrayList();
                    this.recursivegeometry.add(this.geometry);
                }
            }
        }
        catch (Exception e) {
            throw new SAXException(e.getMessage());
        }
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        throw exception;
    }

    private boolean isMultiGeometryTag(String s) {
        if (s.length() > 5 && s.substring(0, 5).compareToIgnoreCase("gml:") == 0) {
            s = s.substring(5);
        }
        return s.compareToIgnoreCase("multigeometry") == 0 || s.compareToIgnoreCase("multipoint") == 0 || s.compareToIgnoreCase("multilinestring") == 0 || s.compareToIgnoreCase("multipolygon") == 0;
    }

    private GMLInputTemplate inputTemplateFromFile(InputStream in) throws ParseException, FileNotFoundException, IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        GMLInputTemplate result = this.inputTemplate(r);
        ((Reader)r).close();
        return result;
    }

    private GMLInputTemplate inputTemplateFromFile(String filename) throws ParseException, FileNotFoundException, IOException {
        BufferedReader r = new BufferedReader(new FileReader(filename));
        GMLInputTemplate result = this.inputTemplate(r);
        ((Reader)r).close();
        return result;
    }

    private void parsePoints(String ptString, GeometryFactory geometryFactory) {
        Coordinate coord = new Coordinate();
        StringBuffer sb = new StringBuffer(ptString);
        int t = 0;
        while (t < sb.length()) {
            char ch = sb.charAt(t);
            if (ch == '\n' || ch == '\r') {
                sb.setCharAt(t, ' ');
            }
            ++t;
        }
        StringTokenizer stokenizer = new StringTokenizer(new String(sb), " ", false);
        while (stokenizer.hasMoreElements()) {
            String aPoint = stokenizer.nextToken();
            StringTokenizer stokenizerPoint = new StringTokenizer(aPoint, ",", false);
            coord.z = Double.NaN;
            coord.y = Double.NaN;
            coord.x = Double.NaN;
            int dim = 0;
            while (stokenizerPoint.hasMoreElements()) {
                String numb = stokenizerPoint.nextToken();
                if (dim == 0) {
                    coord.x = Double.parseDouble(numb);
                } else if (dim == 1) {
                    coord.y = Double.parseDouble(numb);
                } else if (dim == 3) {
                    coord.z = Double.parseDouble(numb);
                }
                ++dim;
            }
            this.pointList.add(coord);
            coord = new Coordinate();
            stokenizerPoint = null;
        }
    }

    private GMLInputTemplate inputTemplate(Reader r) throws IOException, ParseException {
        GMLInputTemplate gmlTemplate = new GMLInputTemplate();
        gmlTemplate.load(r);
        r.close();
        if (!gmlTemplate.loaded) {
            throw new ParseException("Failed to load GML input template");
        }
        return gmlTemplate;
    }
}

