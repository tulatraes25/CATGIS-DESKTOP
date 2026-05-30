/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.CompressedFile;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.GMLInputTemplate;
import com.vividsolutions.jump.io.GMLReader;
import com.vividsolutions.jump.io.IllegalParametersException;
import com.vividsolutions.jump.io.JUMPReader;
import com.vividsolutions.jump.io.ParseException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;

public class FMEGMLReader
implements JUMPReader {
    @Override
    public FeatureCollection read(DriverProperties dp) throws IllegalParametersException, Exception {
        FeatureCollection result;
        GMLInputTemplate inputTemplate;
        GMLReader gmlReader = new GMLReader();
        boolean isCompressed = dp.getProperty("CompressedFile") != null;
        String inputfname = dp.getProperty("DefaultValue");
        if (inputfname == null) {
            inputfname = dp.getProperty("File");
        }
        if (inputfname == null) {
            throw new IllegalParametersException("call to FMEReader.read() has DataProperties w/o a InputFile specified");
        }
        BufferedReader r = isCompressed ? new BufferedReader(new InputStreamReader(CompressedFile.openFile(inputfname, dp.getProperty("CompressedFile")))) : new BufferedReader(new FileReader(inputfname));
        try {
            try {
                inputTemplate = this.getGMLInputTemplate(r, inputfname);
            }
            finally {
                ((Reader)r).close();
            }
        }
        finally {
            ((Reader)r).close();
        }
        r = isCompressed ? new BufferedReader(new InputStreamReader(CompressedFile.openFile(inputfname, dp.getProperty("CompressedFile")))) : new BufferedReader(new FileReader(inputfname));
        try {
            gmlReader.setInputTemplate(inputTemplate);
            try {
                result = gmlReader.read(r, inputfname);
            }
            finally {
                ((Reader)r).close();
            }
        }
        finally {
            ((Reader)r).close();
        }
        return result;
    }

    public GMLInputTemplate getGMLInputTemplate(Reader r, String fname) throws IOException, ParseException {
        String columnDef;
        String s;
        LineNumberReader reader = new LineNumberReader(r);
        int lineNo = 0;
        boolean foundStartTag = false;
        boolean foundEndTag = false;
        while (!foundStartTag && lineNo < 10) {
            s = reader.readLine();
            if (s == null) {
                throw new ParseException("Couldn't find a <schemaFeatures> tag in the input FME GML file.  This isn't a valid FME GML file.");
            }
            ++lineNo;
            if (s.indexOf("<schemaFeatures>") <= -1) continue;
            foundStartTag = true;
        }
        if (!foundStartTag) {
            throw new ParseException("Read first 10 lines of " + fname + " and couldn't find a <schemaFeatures> tag.  This isn't a valid FME GML file.");
        }
        String columns = "";
        while (!foundEndTag) {
            s = reader.readLine();
            if (s.indexOf("</schemaFeatures>") > -1) {
                foundEndTag = true;
            }
            if (s.indexOf("<property fme:name") == -1 && s.indexOf("<property name") == -1) continue;
            String propertyNamePrefix = "";
            if (s.indexOf("<property fme:name") != -1) {
                propertyNamePrefix = "fme:";
            }
            int start = s.indexOf("\"");
            int end = s.indexOf("\"", start + 1);
            if (start == -1 || end == -1) {
                throw new ParseException("Parsing file " + fname + " couldn't get column name on line # " + reader.getLineNumber() + " - " + s);
            }
            String columnName = s.substring(start + 1, end);
            start = s.indexOf(">");
            end = s.indexOf("<", start + 1);
            if (start == -1 || end == -1) {
                throw new ParseException("Parsing file " + fname + " couldn't get column type on line # " + reader.getLineNumber() + " - " + s);
            }
            String columnType = this.FMEtypeToJCSType(s.substring(start + 1, end));
            columnDef = "     <column>\n     <name>" + columnName + "</name>\n          <type>" + columnType + "</type>\n";
            columnDef = String.valueOf(columnDef) + "         <valueElement elementName=\"property\" attributeName=\"" + propertyNamePrefix + "name\" attributeValue=\"" + columnName + "\" />\n";
            columnDef = String.valueOf(columnDef) + "         <valueLocation position=\"body\" />\n";
            columnDef = String.valueOf(columnDef) + "     </column>\n";
            columns = String.valueOf(columns) + columnDef;
        }
        columnDef = "     <column>\n     <name>featuretype</name>\n";
        columnDef = String.valueOf(columnDef) + "           <type>STRING</type>\n";
        columnDef = String.valueOf(columnDef) + "           <valueElement elementName=\"featureType\"/>\n";
        columnDef = String.valueOf(columnDef) + "           <valueLocation position=\"body\"/>\n";
        columnDef = String.valueOf(columnDef) + "     </column>\n";
        columns = String.valueOf(columns) + columnDef;
        String templateText = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
        templateText = String.valueOf(templateText) + "<JCSGMLInputTemplate>\n";
        templateText = String.valueOf(templateText) + "     <CollectionElement>dataFeatures</CollectionElement>\n";
        templateText = String.valueOf(templateText) + "     <FeatureElement>Feature</FeatureElement>\n";
        templateText = String.valueOf(templateText) + "     <GeometryElement>gml:PointProperty</GeometryElement>\n";
        templateText = String.valueOf(templateText) + "     <GeometryElement>gml:PolygonProperty</GeometryElement>\n";
        templateText = String.valueOf(templateText) + "     <GeometryElement>gml:LineStringProperty</GeometryElement>\n";
        templateText = String.valueOf(templateText) + "     <GeometryElement>gml:MultiPointProperty</GeometryElement>\n";
        templateText = String.valueOf(templateText) + "     <GeometryElement>gml:MultiPolygonProperty</GeometryElement>\n";
        templateText = String.valueOf(templateText) + "     <GeometryElement>gml:MultiLineStringProperty</GeometryElement>\n";
        templateText = String.valueOf(templateText) + "     <GeometryElement>gml:MultiGeometryProperty</GeometryElement>\n";
        templateText = String.valueOf(templateText) + "     <ColumnDefinitions>\n";
        templateText = String.valueOf(templateText) + columns;
        templateText = String.valueOf(templateText) + "     </ColumnDefinitions>\n";
        templateText = String.valueOf(templateText) + "</JCSGMLInputTemplate>\n";
        StringReader sr = new StringReader(templateText);
        GMLInputTemplate result = new GMLInputTemplate();
        result.load(sr, "Auto created FME GML input template");
        sr.close();
        return result;
    }

    String FMEtypeToJCSType(String fmeType) {
        if (fmeType.indexOf("fme_char") > -1) {
            return "STRING";
        }
        if (fmeType.indexOf("fme_decimal") > -1) {
            int loc = fmeType.indexOf(",");
            if (loc == -1 || loc == fmeType.length() - 1) {
                return "STRING";
            }
            if (fmeType.substring(loc + 1, loc + 2).equalsIgnoreCase("0")) {
                return "INTEGER";
            }
            return "DOUBLE";
        }
        if (fmeType.indexOf("long") > -1) {
            return "DOUBLE";
        }
        return "STRING";
    }
}

