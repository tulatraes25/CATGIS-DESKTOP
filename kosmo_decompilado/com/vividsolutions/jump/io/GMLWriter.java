/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.io;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.GMLGeometryWriter;
import com.vividsolutions.jump.io.GMLOutputTemplate;
import com.vividsolutions.jump.io.IllegalParametersException;
import com.vividsolutions.jump.io.JUMPWriter;
import com.vividsolutions.jump.io.ParseException;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.saig.core.model.feature.FeatureIterator;

public class GMLWriter
implements JUMPWriter {
    public static String standard_geom = "geometry";
    public static String standard_feature = "feature";
    public static String standard_featureCollection = "featureCollection";
    private GMLOutputTemplate outputTemplate = null;
    private GMLGeometryWriter geometryWriter = new GMLGeometryWriter();
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public GMLWriter() {
        this.geometryWriter.setLinePrefix("                ");
    }

    @Override
    public void write(FeatureCollection featureCollection, DriverProperties dp) throws IllegalParametersException, Exception {
        GMLOutputTemplate gmlTemplate;
        String outputFname = dp.getProperty("File");
        if (outputFname == null) {
            outputFname = dp.getProperty("DefaultValue");
        }
        if (outputFname == null) {
            throw new IllegalParametersException("call to GMLWRite.write() has DataProperties w/o a OutputFile specified");
        }
        if (dp.getProperty("TemplateFile") == null) {
            gmlTemplate = GMLWriter.makeOutputTemplate(featureCollection.getFeatureSchema());
        } else {
            FileReader r = new FileReader(dp.getProperty("TemplateFile"));
            gmlTemplate = new GMLOutputTemplate();
            gmlTemplate.load(r);
            ((Reader)r).close();
        }
        this.setOutputTemplate(gmlTemplate);
        BufferedWriter w = new BufferedWriter(new FileWriter(outputFname));
        this.write(featureCollection, w);
        ((Writer)w).close();
    }

    public void write(FeatureCollection featureCollection, Writer writer) throws Exception {
        if (this.outputTemplate == null) {
            throw new Exception("attempt to write GML w/o specifying the output template");
        }
        BufferedWriter buffWriter = new BufferedWriter(writer);
        buffWriter.write(this.outputTemplate.headerText);
        FeatureIterator t = null;
        try {
            t = featureCollection.iterator();
            while (t.hasNext()) {
                Feature f = t.next();
                int u = 0;
                while (u < this.outputTemplate.featureText.size()) {
                    String pre = (String)this.outputTemplate.featureText.get(u);
                    String token = (String)this.outputTemplate.codingText.get(u);
                    buffWriter.write(pre);
                    String evaled = this.evaluateToken(f, token);
                    if (evaled == null) {
                        evaled = "";
                    }
                    buffWriter.write(evaled);
                    ++u;
                }
                buffWriter.write(this.outputTemplate.featureTextfooter);
                buffWriter.write("\n");
            }
        }
        finally {
            if (t != null && t instanceof FeatureIterator) {
                t.close();
            }
        }
        buffWriter.write(this.outputTemplate.footerText);
        buffWriter.flush();
    }

    public static String safeXML(String s) {
        StringBuffer sb = new StringBuffer(s);
        int t = 0;
        while (t < sb.length()) {
            char c = sb.charAt(t);
            if (c == '<') {
                sb.replace(t, t + 1, "&lt;");
            }
            if (c == '>') {
                sb.replace(t, t + 1, "&gt;");
            }
            if (c == '&') {
                sb.replace(t, t + 1, "&amp;");
            }
            if (c == '\'') {
                sb.replace(t, t + 1, "&apos;");
            }
            if (c == '\"') {
                sb.replace(t, t + 1, "&quot;");
            }
            ++t;
        }
        return sb.toString();
    }

    public void setOutputTemplate(GMLOutputTemplate ot) {
        this.outputTemplate = ot;
    }

    private String evaluateToken(Feature f, String token) throws Exception, ParseException {
        if (!(token = token.trim()).startsWith("=") || token.length() < 7) {
            throw new ParseException("couldn't understand token '" + token + "' in the output template");
        }
        token = token.substring(1);
        int index = (token = token.trim()).indexOf(" ");
        String cmd = index == -1 ? token : token.substring(0, token.indexOf(" "));
        if (cmd.equalsIgnoreCase("column")) {
            String column = token.substring(6);
            column = column.trim();
            String result = this.toString(f, column);
            result = GMLWriter.safeXML(result);
            return result;
        }
        if (cmd.equalsIgnoreCase("geometry")) {
            this.geometryWriter.setMaximumCoordinatesPerLine(1);
            return this.geometryWriter.write(f.getGeometry());
        }
        if (cmd.equalsIgnoreCase("geometrytype")) {
            return f.getGeometry().getGeometryType();
        }
        throw new ParseException("couldn't understand token '" + token + "' in the output template");
    }

    protected String toString(Feature f, String column) {
        Assert.isTrue((f.getSchema().getAttributeType(column) != AttributeType.GEOMETRY ? 1 : 0) != 0);
        Object attribute = f.getAttribute(column);
        if (attribute == null) {
            return "";
        }
        if (attribute instanceof Date) {
            return this.format((Date)attribute);
        }
        return attribute.toString();
    }

    protected String format(Date date) {
        return this.dateFormatter.format(date);
    }

    public static GMLOutputTemplate makeOutputTemplate(FeatureSchema fcmd) {
        String colText = "";
        String colCode = "";
        String colHeader = "";
        GMLOutputTemplate result = new GMLOutputTemplate();
        String inputTemplate = GMLWriter.makeInputTemplate(fcmd);
        result.setHeaderText("<?xml version='1.0' encoding='UTF-8'?>\n<JCSDataFile xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xsi=\"http://www.w3.org/2000/10/XMLSchema-instance\" >\n" + inputTemplate + "<" + standard_featureCollection + ">\n");
        colText = "";
        colHeader = "     <" + standard_feature + "> \n";
        int t = 0;
        while (t < fcmd.getAttributeCount()) {
            String colName = fcmd.getAttributeName(t);
            colText = "";
            if (t != fcmd.getGeometryIndex()) {
                colText = String.valueOf(colHeader) + "          <property name=\"" + colName + "\">";
                colCode = "=column " + colName;
                colHeader = "</property>\n";
            } else {
                colText = String.valueOf(colHeader) + "          <" + standard_geom + ">\n";
                colCode = "=geometry";
                colHeader = "          </" + standard_geom + ">\n";
            }
            result.addItem(colText, colCode);
            ++t;
        }
        result.setFeatureFooter(String.valueOf(colHeader) + "     </" + standard_feature + ">\n");
        result.setFooterText("     </" + standard_featureCollection + ">\n</JCSDataFile>\n");
        return result;
    }

    public static String makeInputTemplate(FeatureSchema fcmd) {
        String result = "<JCSGMLInputTemplate>\n<CollectionElement>" + standard_featureCollection + "</CollectionElement> \n<FeatureElement>" + standard_feature + "</FeatureElement>\n<GeometryElement>" + standard_geom + "</GeometryElement>\n<ColumnDefinitions>\n";
        int t = 0;
        while (t < fcmd.getAttributeCount()) {
            String colName = fcmd.getAttributeName(t);
            if (t != fcmd.getGeometryIndex()) {
                String colDef = "     <column>\n";
                colDef = String.valueOf(colDef) + "          <name>" + colName + "</name>\n";
                AttributeType attributeType = fcmd.getAttributeType(t);
                colDef = String.valueOf(colDef) + "          <type>" + attributeType + "</type>\n";
                colDef = String.valueOf(colDef) + "          <valueElement elementName=\"property\" attributeName=\"name\" attributeValue=\"" + colName + "\"/>\n";
                colDef = String.valueOf(colDef) + "          <valueLocation position=\"body\"/>\n";
                colDef = String.valueOf(colDef) + "     </column>\n";
                result = String.valueOf(result) + colDef;
            }
            ++t;
        }
        result = String.valueOf(result) + "</ColumnDefinitions>\n</JCSGMLInputTemplate>\n\n";
        return result;
    }
}

