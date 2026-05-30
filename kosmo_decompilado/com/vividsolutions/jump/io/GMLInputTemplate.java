/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.xerces.parsers.SAXParser
 */
package com.vividsolutions.jump.io;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.ColumnDescription;
import com.vividsolutions.jump.io.EndOfParseException;
import com.vividsolutions.jump.io.ParseException;
import com.vividsolutions.jump.util.FlexibleDateParser;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class GMLInputTemplate
extends DefaultHandler {
    LineNumberReader myReader;
    XMLReader xr;
    String tagBody = "";
    String collectionTag;
    String featureTag;
    private ArrayList geometryElements = new ArrayList(20);
    String streamName;
    boolean havecollectionTag = false;
    boolean havefeatureTag = false;
    boolean havegeometryElement = false;
    public boolean loaded = false;
    ArrayList columnDefinitions = new ArrayList();
    int columnDef_valueType = 0;
    String columnDef_valueAttribute = "";
    String columnDef_tagName = "";
    int columnDef_tagType = 0;
    String columnDef_tagAttribute = "";
    String columnDef_tagValue = "";
    String columnDef_columnName = "";
    AttributeType columnDef_type = null;
    String lastStartTag_uri;
    String lastStartTag_name;
    String lastStartTag_qName;
    Attributes lastStartTag_atts;
    private FlexibleDateParser dateParser = new FlexibleDateParser();

    public GMLInputTemplate() {
        this.xr = new SAXParser();
        this.xr.setContentHandler(this);
        this.xr.setErrorHandler(this);
    }

    public String columnName(int index) throws ParseException {
        if (this.loaded) {
            return ((ColumnDescription)this.columnDefinitions.get((int)index)).columnName;
        }
        throw new ParseException("requested columnName w/o loading the template");
    }

    public FeatureSchema toFeatureSchema() throws ParseException {
        if (!this.loaded) {
            throw new ParseException("requested toFeatureSchema w/o loading the template");
        }
        FeatureSchema fcmd = new FeatureSchema();
        fcmd.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        int t = 0;
        while (t < this.columnDefinitions.size()) {
            fcmd.addAttribute(((ColumnDescription)this.columnDefinitions.get((int)t)).columnName, ((ColumnDescription)this.columnDefinitions.get(t)).getType());
            ++t;
        }
        return fcmd;
    }

    public boolean isGeometryElement(String tag) {
        int t = 0;
        while (t < this.geometryElements.size()) {
            String s = (String)this.geometryElements.get(t);
            if (s.equalsIgnoreCase(tag)) {
                return true;
            }
            ++t;
        }
        return false;
    }

    public void load(Reader r) throws ParseException, IOException {
        this.load(r, "Unknown Stream");
    }

    public void load(Reader r, String readerName) throws ParseException, IOException {
        this.myReader = new LineNumberReader(r);
        this.streamName = readerName;
        try {
            this.xr.parse(new InputSource(this.myReader));
        }
        catch (EndOfParseException endOfParseException) {
        }
        catch (SAXParseException e) {
            throw new ParseException(String.valueOf(e.getMessage()) + "  Last Opened Tag: " + this.lastStartTag_qName + ".  Reader reports last line read as " + this.myReader.getLineNumber(), String.valueOf(this.streamName) + " - " + e.getPublicId() + " (" + e.getSystemId() + ") ", e.getLineNumber(), e.getColumnNumber());
        }
        catch (SAXException e) {
            throw new ParseException(String.valueOf(e.getMessage()) + "  Last Opened Tag: " + this.lastStartTag_qName, this.streamName, this.myReader.getLineNumber(), 0);
        }
        boolean bl = this.loaded = this.havecollectionTag && this.havefeatureTag && this.havegeometryElement;
        if (!this.loaded) {
            String miss = "";
            if (!this.havecollectionTag) {
                miss = String.valueOf(miss) + "Missing CollectionElement.  ";
            }
            if (!this.havefeatureTag) {
                miss = String.valueOf(miss) + "Missing FeatureElement.  ";
            }
            if (!this.havegeometryElement) {
                miss = String.valueOf(miss) + "Missing GeometryElement.  ";
            }
            throw new ParseException("Failed to load the GML Input Template.  " + miss);
        }
    }

    public String getFeatureCollectionElementName() throws ParseException {
        if (this.loaded) {
            return this.collectionTag;
        }
        throw new ParseException("requested FeatureCollectionElementName w/o loading the template");
    }

    public String getFeatureElementName() throws ParseException {
        if (this.loaded) {
            return this.featureTag;
        }
        throw new ParseException("requested FeatureCollectionElementName w/o loading the template");
    }

    public int match(String XMLtagName, Attributes xmlAtts) throws ParseException {
        if (this.loaded) {
            int t = 0;
            while (t < this.columnDefinitions.size()) {
                if (((ColumnDescription)this.columnDefinitions.get(t)).match(XMLtagName, xmlAtts) != 0) {
                    return t;
                }
                ++t;
            }
            return -1;
        }
        throw new ParseException("requested match() w/o loading the template");
    }

    public Object getColumnValue(int index, String tagBody, Attributes xmlAtts) throws ParseException {
        if (!this.loaded) {
            throw new ParseException("requested getColumnValue w/o loading the template");
        }
        String val = ((ColumnDescription)this.columnDefinitions.get((int)index)).valueType == 1 ? tagBody : xmlAtts.getValue(((ColumnDescription)this.columnDefinitions.get((int)index)).valueAttribute);
        ColumnDescription cd = (ColumnDescription)this.columnDefinitions.get(index);
        if (cd.type == AttributeType.STRING) {
            return val;
        }
        if (cd.type == AttributeType.INTEGER) {
            try {
                return new Long(val);
            }
            catch (Exception e) {
                return null;
            }
        }
        if (cd.type == AttributeType.DOUBLE) {
            try {
                return new Double(val);
            }
            catch (Exception e) {
                return null;
            }
        }
        if (cd.type == AttributeType.DATE) {
            try {
                return this.dateParser.parse(val, false);
            }
            catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        throw exception;
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
    public void startDocument() {
    }

    @Override
    public void endDocument() {
    }

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) throws SAXException {
        try {
            this.tagBody = "";
            if (qName.equals("column")) {
                this.columnDef_tagName = "";
                this.columnDef_tagType = 0;
                this.columnDef_tagAttribute = "";
                this.columnDef_tagValue = "";
                this.columnDef_valueType = 0;
                this.columnDef_valueAttribute = "";
                this.columnDef_columnName = "";
                this.columnDef_type = null;
            }
            this.lastStartTag_uri = uri;
            this.lastStartTag_name = name;
            this.lastStartTag_qName = qName;
            this.lastStartTag_atts = atts;
        }
        catch (Exception e) {
            throw new SAXException(e.getMessage());
        }
    }

    int lookupAttribute(Attributes atts, String att_name) {
        int t = 0;
        while (t < atts.getLength()) {
            if (atts.getQName(t).equalsIgnoreCase(att_name)) {
                return t;
            }
            ++t;
        }
        return -1;
    }

    @Override
    public void endElement(String uri, String name, String qName) throws SAXException {
        try {
            if (qName.equalsIgnoreCase("JCSGMLInputTemplate")) {
                throw new EndOfParseException("Finished parsing input template");
            }
            if (qName.equalsIgnoreCase("type")) {
                String t = this.tagBody.toUpperCase();
                t = t.trim();
                try {
                    this.columnDef_type = AttributeType.toAttributeType(t);
                }
                catch (IllegalArgumentException e) {
                    this.columnDef_type = null;
                }
            }
            if (qName.equalsIgnoreCase("GeometryElement")) {
                this.tagBody = this.tagBody.trim();
                this.geometryElements.add(new String(this.tagBody));
                this.havegeometryElement = true;
                return;
            }
            if (qName.equalsIgnoreCase("CollectionElement")) {
                this.collectionTag = this.tagBody = this.tagBody.trim();
                this.havecollectionTag = true;
                return;
            }
            if (qName.equalsIgnoreCase("FeatureElement")) {
                this.featureTag = this.tagBody = this.tagBody.trim();
                this.havefeatureTag = true;
                return;
            }
            if (qName.equalsIgnoreCase("name")) {
                this.columnDef_columnName = this.tagBody.trim();
            }
            if (qName.equalsIgnoreCase("valueelement")) {
                this.columnDef_tagType = 1;
                int attindex = this.lookupAttribute(this.lastStartTag_atts, "elementname");
                if (attindex == -1) {
                    throw new SAXException("column definition has 'valueelement' tag without 'elementname' attribute");
                }
                this.columnDef_tagName = new String(this.lastStartTag_atts.getValue(attindex));
                attindex = this.lookupAttribute(this.lastStartTag_atts, "attributename");
                if (attindex != -1) {
                    this.columnDef_tagAttribute = new String(this.lastStartTag_atts.getValue(attindex));
                    this.columnDef_tagType = 2;
                    attindex = this.lookupAttribute(this.lastStartTag_atts, "attributevalue");
                    if (attindex != -1) {
                        this.columnDef_tagValue = new String(this.lastStartTag_atts.getValue(attindex));
                        this.columnDef_tagType = 3;
                    }
                }
            }
            if (qName.equalsIgnoreCase("valuelocation")) {
                int attindex = this.lookupAttribute(this.lastStartTag_atts, "position");
                if (attindex == -1) {
                    throw new SAXException("column definition has 'valuelocation' tag without 'position' attribute");
                }
                if (this.lastStartTag_atts.getValue(attindex).equalsIgnoreCase("body")) {
                    this.columnDef_valueType = 1;
                } else {
                    attindex = this.lookupAttribute(this.lastStartTag_atts, "attributename");
                    this.columnDef_valueType = 2;
                    if (attindex == -1) {
                        throw new SAXException("column definition has 'valuelocation' tag, attribute type, but no 'attributename' attribute");
                    }
                    this.columnDef_valueAttribute = new String(this.lastStartTag_atts.getValue(attindex));
                }
            }
            if (qName.equalsIgnoreCase("column")) {
                if (this.columnDef_tagName.equalsIgnoreCase("")) {
                    throw new SAXException("column Definition didnt include tag name ('<name>...</name>')");
                }
                if (this.columnDef_tagType == 0) {
                    throw new SAXException("column Definition didnt include 'valueelement' ");
                }
                if (this.columnDef_valueType == 0) {
                    throw new SAXException("column Definition didnt have a 'valuelocation'");
                }
                ColumnDescription colDes = new ColumnDescription();
                colDes.setColumnName(this.columnDef_columnName);
                if (colDes.columnName.compareTo("GEOMETRY") == 0) {
                    throw new ParseException("Cannot have a column named GEOMETRY!");
                }
                if (this.columnDef_valueType == 2) {
                    colDes.setValueAttribute(this.columnDef_valueAttribute);
                }
                colDes.setTagName(this.columnDef_tagName);
                if (this.columnDef_tagType == 3) {
                    colDes.setTagAttribute(this.columnDef_tagAttribute, this.columnDef_tagValue);
                }
                if (this.columnDef_tagType == 2) {
                    colDes.setTagAttribute(this.columnDef_tagAttribute);
                }
                colDes.setType(this.columnDef_type);
                this.columnDefinitions.add(colDes);
            }
        }
        catch (EndOfParseException e) {
            throw e;
        }
        catch (Exception e) {
            throw new SAXException(e.getMessage());
        }
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
}

