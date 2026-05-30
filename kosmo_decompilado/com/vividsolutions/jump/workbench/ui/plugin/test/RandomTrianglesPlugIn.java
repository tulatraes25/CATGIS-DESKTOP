/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.io.ParseException
 *  com.vividsolutions.jts.io.WKTReader
 */
package com.vividsolutions.jump.workbench.ui.plugin.test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.saig.jump.lang.I18N;

public class RandomTrianglesPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.test.RandomTrianglesPlugIn.name");
    private static int dummyLayerCount = 0;
    private GeometryFactory geometryFactory = new GeometryFactory();
    private WKTReader wktReader = new WKTReader(this.geometryFactory);
    private List cities = Arrays.asList("Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        return this.execute(context, 50);
    }

    public boolean execute(PlugInContext context, int layerSize) throws Exception {
        int n = 1;
        int i = 0;
        while (i < n) {
            this.generateLayer(context, layerSize);
            ++i;
        }
        return true;
    }

    private void generateLayer(PlugInContext context, int size) throws Exception {
        ++dummyLayerCount;
        FeatureSchema featureSchema = new FeatureSchema();
        featureSchema.addAttribute("Geometry", AttributeType.GEOMETRY);
        featureSchema.addAttribute("City", AttributeType.STRING);
        featureSchema.addAttribute("A Code", AttributeType.DATE);
        featureSchema.addAttribute("B Code", AttributeType.INTEGER);
        featureSchema.addAttribute("C Code", AttributeType.DOUBLE);
        featureSchema.addAttribute("D Code", AttributeType.STRING);
        featureSchema.addAttribute("E Code", AttributeType.STRING);
        featureSchema.addAttribute("F Code", AttributeType.STRING);
        featureSchema.addAttribute("G Code", AttributeType.STRING);
        featureSchema.addAttribute("H Code", AttributeType.STRING);
        featureSchema.addAttribute("I Code", AttributeType.STRING);
        featureSchema.addAttribute("J Code", AttributeType.STRING);
        featureSchema.addAttribute("K Code", AttributeType.STRING);
        featureSchema.addAttribute("L Code", AttributeType.STRING);
        featureSchema.addAttribute("M Code", AttributeType.STRING);
        featureSchema.addAttribute("N Code", AttributeType.STRING);
        featureSchema.addAttribute("O Code", AttributeType.STRING);
        featureSchema.addAttribute("P Code", AttributeType.STRING);
        FeatureDataset featureCollection = new FeatureDataset(featureSchema);
        this.addFeature(this.cornerSquare(), featureCollection);
        int i = 0;
        while (i < size) {
            this.addFeature(this.randomTriangle(), featureCollection);
            ++i;
        }
        Layer layer = context.addLayer(StandardCategoryNames.WORKING, NAME, featureCollection);
        layer.setDescription("ABCDE");
    }

    private Geometry cornerSquare() throws ParseException {
        return this.wktReader.read("POLYGON ((-50 -50, 50 -50, 50 50, -50 50, -50 -50))");
    }

    private void addFeature(Geometry geometry, FeatureCollection featureCollection) throws Exception {
        Feature feature = FeatureUtil.toFeature(geometry, featureCollection.getFeatureSchema());
        feature.setAttribute("City", this.cities.get((int)Math.floor(Math.random() * (double)this.cities.size())));
        feature.setAttribute("A Code", (Object)new Date());
        feature.setAttribute("B Code", (Object)new Integer((int)(Math.random() * 100000.0)));
        feature.setAttribute("C Code", (Object)new Double(Math.random() * 100000.0));
        feature.setAttribute("D Code", (Object)new Date((int)Math.pow(Math.random() * 100000.0, 20.0)).toString());
        feature.setAttribute("E Code", (Object)("" + (int)(Math.random() * 100000.0)));
        feature.setAttribute("F Code", (Object)("" + (int)(Math.random() * 100000.0)));
        feature.setAttribute("G Code", (Object)("" + (int)(Math.random() * 100000.0)));
        feature.setAttribute("H Code", (Object)("" + (int)(Math.random() * 100000.0)));
        feature.setAttribute("I Code", (Object)("" + (int)(Math.random() * 100000.0)));
        feature.setAttribute("J Code", (Object)("" + (int)(Math.random() * 100000.0)));
        feature.setAttribute("K Code", (Object)("" + (int)(Math.random() * 100000.0)));
        feature.setAttribute("L Code", (Object)("" + (int)(Math.random() * 100000.0)));
        feature.setAttribute("M Code", (Object)("" + (int)(Math.random() * 100000.0)));
        feature.setAttribute("N Code", (Object)("" + (int)(Math.random() * 100000.0)));
        feature.setAttribute("O Code", (Object)("" + (int)(Math.random() * 100000.0)));
        feature.setAttribute("P Code", (Object)("" + (int)(Math.random() * 100000.0)));
        if (Math.random() > 0.8) {
            feature.setAttribute("E Code", null);
        }
        featureCollection.add(feature);
    }

    private Geometry randomTriangle() {
        int perturbation = 30;
        int x = (int)(Math.random() * 700.0);
        int y = (int)(Math.random() * 700.0);
        Coordinate firstPoint = this.perturbedPoint(x, y, perturbation);
        return this.geometryFactory.createPolygon(this.geometryFactory.createLinearRing(new Coordinate[]{firstPoint, this.perturbedPoint(x, y, perturbation), this.perturbedPoint(x, y, perturbation), firstPoint}), null);
    }

    private Coordinate perturbedPoint(int x, int y, int perturbation) {
        return new Coordinate((double)x + Math.random() * (double)perturbation, (double)y + Math.random() * (double)perturbation);
    }

    public void setCities(List cities) {
        this.cities = cities;
    }
}

