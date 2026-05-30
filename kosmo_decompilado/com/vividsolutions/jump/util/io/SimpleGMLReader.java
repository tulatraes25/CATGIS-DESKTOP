/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.util.io;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.io.FMEGMLReader;
import com.vividsolutions.jump.io.GMLInputTemplate;
import com.vividsolutions.jump.io.GMLReader;
import com.vividsolutions.jump.io.ParseException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public class SimpleGMLReader {
    public List<Geometry> toGeometries(Reader gml, String collectionElement, String featureElement, String geometryElement) throws Exception {
        GMLInputTemplate template = this.template(collectionElement, featureElement, geometryElement);
        GMLReader gmlReader = new GMLReader();
        gmlReader.setInputTemplate(template);
        return FeatureUtil.toGeometries(gmlReader.read(gml).getFeatures());
    }

    private GMLInputTemplate template(String collectionElement, String featureElement, String geometryElement) throws IOException, ParseException {
        String s = "";
        s = String.valueOf(s) + "<?xml version='1.0' encoding='UTF-8'?>";
        s = String.valueOf(s) + "<JCSGMLInputTemplate>";
        s = String.valueOf(s) + "<CollectionElement>" + collectionElement + "</CollectionElement>";
        s = String.valueOf(s) + "<FeatureElement>" + featureElement + "</FeatureElement>";
        s = String.valueOf(s) + "<GeometryElement>" + geometryElement + "</GeometryElement>";
        s = String.valueOf(s) + "<ColumnDefinitions></ColumnDefinitions>";
        s = String.valueOf(s) + "</JCSGMLInputTemplate>";
        GMLInputTemplate template = new GMLInputTemplate();
        StringReader sr = new StringReader(s);
        try {
            template.load(sr);
        }
        finally {
            sr.close();
        }
        return template;
    }

    public List<Geometry> toGeometries(String gml, String collectionElement, String featureElement, String geometryElement) throws Exception {
        StringReader r = new StringReader(gml);
        try {
            List<Geometry> list = this.toGeometries(r, collectionElement, featureElement, geometryElement);
            return list;
        }
        finally {
            r.close();
        }
    }

    public FeatureCollection readFMEFile(File file) throws Exception {
        FeatureCollection fc;
        GMLInputTemplate inputTemplate;
        FMEGMLReader fmeGMLReader = new FMEGMLReader();
        FileReader fileReader = new FileReader(file);
        try {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            try {
                inputTemplate = fmeGMLReader.getGMLInputTemplate(bufferedReader, file.getPath());
            }
            finally {
                bufferedReader.close();
            }
        }
        finally {
            fileReader.close();
        }
        GMLReader gmlReader = new GMLReader();
        gmlReader.setInputTemplate(inputTemplate);
        fileReader = new FileReader(file);
        try {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            try {
                fc = gmlReader.read(bufferedReader);
            }
            finally {
                bufferedReader.close();
            }
        }
        finally {
            fileReader.close();
        }
        return fc;
    }
}

