/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.io.ParseException
 *  com.vividsolutions.jts.io.WKTReader
 */
package com.vividsolutions.jump.io;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.CompressedFile;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.IllegalParametersException;
import com.vividsolutions.jump.io.JUMPReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;

public class WKTReader
implements JUMPReader {
    private GeometryFactory geometryFactory = new GeometryFactory();
    private com.vividsolutions.jts.io.WKTReader wktReader = new com.vividsolutions.jts.io.WKTReader(this.geometryFactory);

    @Override
    public FeatureCollection read(DriverProperties dp) throws IllegalParametersException, Exception {
        FeatureCollection fc;
        boolean isCompressed = dp.getProperty("CompressedFile") != null;
        String inputFname = dp.getProperty("File");
        if (inputFname == null) {
            inputFname = dp.getProperty("DefaultValue");
        }
        if (inputFname == null) {
            throw new IllegalParametersException("call to WKTReader.read() has DataProperties w/o a InputFile specified");
        }
        InputStreamReader fileReader = isCompressed ? new InputStreamReader(CompressedFile.openFile(inputFname, dp.getProperty("CompressedFile"))) : new FileReader(inputFname);
        try {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            try {
                fc = this.read(bufferedReader);
            }
            finally {
                bufferedReader.close();
            }
        }
        finally {
            ((Reader)fileReader).close();
        }
        return fc;
    }

    public FeatureCollection read(Reader reader) throws Exception {
        FeatureSchema featureSchema = new FeatureSchema();
        featureSchema.addAttribute("Geometry", AttributeType.GEOMETRY);
        FeatureDataset featureCollection = new FeatureDataset(featureSchema);
        BufferedReader bufferedReader = new BufferedReader(reader);
        try {
            while (!this.isAtEndOfFile(bufferedReader)) {
                featureCollection.addWithNewKey(this.nextFeature(bufferedReader, featureSchema));
            }
        }
        finally {
            bufferedReader.close();
        }
        return featureCollection;
    }

    private boolean isAtEndOfFile(BufferedReader bufferedReader) throws IOException, ParseException {
        bufferedReader.mark(1000);
        try {
            StreamTokenizer tokenizer = new StreamTokenizer(bufferedReader);
            int type = tokenizer.nextToken();
            if (type == -1) {
                return true;
            }
            if (type == -3) {
                return false;
            }
            throw new ParseException("Expected word or end-of-file but encountered StreamTokenizer type " + type);
        }
        finally {
            bufferedReader.reset();
        }
    }

    private Feature nextFeature(Reader reader, FeatureSchema featureSchema) throws ParseException {
        BasicFeature feature = new BasicFeature(featureSchema);
        feature.setGeometry(this.wktReader.read(reader));
        return feature;
    }
}

