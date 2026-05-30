/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.GMLOutputTemplate;
import com.vividsolutions.jump.io.GMLWriter;
import com.vividsolutions.jump.io.IllegalParametersException;
import com.vividsolutions.jump.io.JUMPWriter;
import com.vividsolutions.jump.io.ParseException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FMEGMLWriter
implements JUMPWriter {
    int outputFormatType = 1;
    private SimpleDateFormat fmeDateFormatter = new SimpleDateFormat("yyyyMMdd");

    @Override
    public void write(FeatureCollection featureCollection, DriverProperties dp) throws IllegalParametersException, Exception {
        String outputfname = dp.getProperty("File");
        if (outputfname == null) {
            outputfname = dp.getProperty("DefaultValue");
        }
        if (outputfname == null) {
            throw new IllegalParametersException("call to FMEGMLWriter.write() has DataProperties w/o a OutputFile specified");
        }
        if (dp.getProperty("FMEFormatVersion") != null) {
            if (dp.getProperty("FMEFormatVersion").equals("2000")) {
                this.outputFormatType = 0;
            }
            if (dp.getProperty("FMEFormatVersion").equals("2001")) {
                this.outputFormatType = 1;
            }
        }
        GMLOutputTemplate gmlTemplate = this.createOutputTemplate(featureCollection.getFeatureSchema());
        GMLWriter gmlWriter = new GMLWriter(){

            @Override
            protected String format(Date date) {
                return FMEGMLWriter.this.fmeDateFormatter.format(date);
            }
        };
        gmlWriter.setOutputTemplate(gmlTemplate);
        BufferedWriter w = new BufferedWriter(new FileWriter(outputfname));
        gmlWriter.write(featureCollection, w);
        w.close();
    }

    public GMLOutputTemplate createOutputTemplate(FeatureSchema fs) throws ParseException, Exception {
        String colName;
        String templateText = "";
        switch (this.outputFormatType) {
            case 0: {
                templateText = "<?xml version='1.0' encoding='UTF-8'?>\n<dataset xmlns=\"http://www.safe.com/xml/namespaces/fmegml2\" xmlns:fme=\"http://www.safe.com/xml/namespaces/fmegml2\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xsi=\"http://www.w3.org/2000/10/XMLSchema-instance\" xsi:schemaLocation=\"http://www.safe.com/xml/schemas/fmegml2.xsd\">\n";
                break;
            }
            case 1: {
                templateText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<dataset xmlns=\"http://www.safe.com/xml/schemas/FMEFeatures\" xmlns:fme=\"http://www.safe.com/xml/schemas/FMEFeatures\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.safe.com/xml/schemas/FMEFeatures FMEFeatures.xsd\">\n";
            }
        }
        templateText = String.valueOf(templateText) + "<schemaFeatures>\n<gml:featureMember>\n<Feature>\n<featureType>JCSOutput</featureType>\n";
        int t = 0;
        while (t < fs.getAttributeCount()) {
            AttributeType attributeType = fs.getAttributeType(t);
            if (t != fs.getGeometryIndex()) {
                try {
                    colName = fs.getAttributeName(t);
                    String colType = this.JCSattributeType2FMEtype(attributeType.toString());
                    String column = "";
                    switch (this.outputFormatType) {
                        case 0: {
                            column = "<property fme:name=\"" + colName + "\">" + colType + "</property>\n";
                            break;
                        }
                        case 1: {
                            column = "<property name=\"" + colName + "\">" + colType + "</property>\n";
                        }
                    }
                    templateText = String.valueOf(templateText) + column;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            ++t;
        }
        templateText = String.valueOf(templateText) + "</Feature>\n</gml:featureMember>\n</schemaFeatures>\n<dataFeatures>\n";
        templateText = String.valueOf(templateText) + "<% FEATURE %>\n<gml:featureMember>\n<Feature>\n<featureType>JCSOutput</featureType>\n";
        t = 0;
        while (t < fs.getAttributeCount()) {
            colName = fs.getAttributeName(t);
            if (t != fs.getGeometryIndex()) {
                switch (this.outputFormatType) {
                    case 0: {
                        templateText = String.valueOf(templateText) + "<property fme:name=\"" + colName + "\">";
                        break;
                    }
                    case 1: {
                        templateText = String.valueOf(templateText) + "<property name=\"" + colName + "\">";
                    }
                }
                templateText = String.valueOf(templateText) + "<%=COLUMN " + colName + "%></property>\n";
            } else {
                switch (this.outputFormatType) {
                    case 0: {
                        templateText = String.valueOf(templateText) + "<property fme:name=\"gml2_coordsys\"></property>\n";
                        break;
                    }
                    case 1: {
                        templateText = String.valueOf(templateText) + "<property name=\"gml2_coordsys\"></property>\n";
                    }
                }
                templateText = String.valueOf(templateText) + "<gml:<%=GEOMETRYTYPE%>Property>\n<%=GEOMETRY %>\n</gml:<%=GEOMETRYTYPE%>Property>\n";
            }
            ++t;
        }
        templateText = String.valueOf(templateText) + "</Feature>\n</gml:featureMember>\n<% ENDFEATURE %>\n</dataFeatures>\n</dataset>\n";
        StringReader stringreader = new StringReader(templateText);
        GMLOutputTemplate result = new GMLOutputTemplate();
        result.load(stringreader, "Auto Generated FME GML input template");
        stringreader.close();
        return result;
    }

    String JCSattributeType2FMEtype(String jcsType) throws ParseException {
        switch (this.outputFormatType) {
            case 0: {
                if (jcsType.equalsIgnoreCase("STRING")) {
                    return "fme_char(1024)";
                }
                if (jcsType.equalsIgnoreCase("INTEGER")) {
                    return "long";
                }
                if (jcsType.equalsIgnoreCase("DOUBLE")) {
                    return "fme_decimal(15,15)";
                }
                if (jcsType.equalsIgnoreCase("DATE")) {
                    return "string";
                }
                throw new ParseException("couldn't convert JCS type '" + jcsType + "' to a FME type.");
            }
            case 1: {
                if (jcsType.equalsIgnoreCase("STRING")) {
                    return "string";
                }
                if (jcsType.equalsIgnoreCase("INTEGER")) {
                    return "long";
                }
                if (jcsType.equalsIgnoreCase("DOUBLE")) {
                    return "long";
                }
                if (jcsType.equalsIgnoreCase("DATE")) {
                    return "string";
                }
                throw new ParseException("couldn't convert JCS type '" + jcsType + "' to a FME type.");
            }
        }
        throw new ParseException("couldn't convert JCS type '" + jcsType + "' to a FME type.");
    }
}

