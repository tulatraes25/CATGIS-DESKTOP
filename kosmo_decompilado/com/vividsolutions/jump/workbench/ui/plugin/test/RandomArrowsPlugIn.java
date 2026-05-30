/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 */
package com.vividsolutions.jump.workbench.ui.plugin.test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.ArrowTerminalDecorator;
import java.util.ArrayList;
import org.saig.jump.lang.I18N;

public class RandomArrowsPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.test.RandomArrowsPlugIn.name");
    private static final int FEATURE_COUNT = 20;
    private static final double LAYER_SIDE_LENGTH = 100.0;
    private static final int MAX_SEGMENT_COUNT = 3;
    private static final double MAX_SEGMENT_LENGTH = 20.0;
    private GeometryFactory geometryFactory = new GeometryFactory();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        FeatureSchema schema = new FeatureSchema();
        schema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        FeatureDataset dataset = new FeatureDataset(schema);
        int i = 0;
        while (i < 20) {
            dataset.add(this.createFeature(schema));
            ++i;
        }
        this.addLayer(dataset, context);
        return true;
    }

    private void addLayer(FeatureCollection featureCollection, PlugInContext context) {
        Layer layer = new Layer(I18N.getString("workbench.ui.plugin.test.RandomArrowsPlugIn.name"), context.getLayerManager().generateLayerFillColor(), featureCollection, context.getLayerManager());
        boolean firingEvents = context.getLayerManager().isFiringEvents();
        context.getLayerManager().setFiringEvents(false);
        try {
            layer.addStyle(new ArrowTerminalDecorator.NarrowSolidEnd());
        }
        finally {
            context.getLayerManager().setFiringEvents(firingEvents);
        }
        context.getLayerManager().addLayer(StandardCategoryNames.WORKING, layer);
    }

    private Feature createFeature(FeatureSchema schema) {
        ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
        coordinates.add(CoordUtil.add(new Coordinate(50.0, 50.0), this.randomCoordinate(50.0)));
        int walkMax = (int)Math.ceil(Math.random() * 20.0);
        int segmentCount = (int)Math.ceil(Math.random() * 3.0);
        int i = 0;
        while (i < segmentCount) {
            Coordinate prevCoordinate = (Coordinate)coordinates.get(coordinates.size() - 1);
            coordinates.add(CoordUtil.add(prevCoordinate, this.randomCoordinate(walkMax)));
            ++i;
        }
        LineString lineString = this.geometryFactory.createLineString(coordinates.toArray(new Coordinate[0]));
        Feature feature = FeatureUtil.toFeature((Geometry)lineString, schema);
        return feature;
    }

    private Coordinate randomCoordinate(double walkMax) {
        return CoordUtil.add(new Coordinate(-walkMax / 2.0, -walkMax / 2.0), new Coordinate(Math.random() * walkMax, Math.random() * walkMax));
    }
}

