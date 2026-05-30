/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import java.util.ArrayList;
import java.util.Collection;
import org.saig.jump.lang.I18N;

public class CombineSelectedFeaturesPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.CombineSelectedFeaturesPlugIn.name");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        final ArrayList<Feature> originalFeatures = new ArrayList<Feature>(context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems());
        final Feature combinedFeature = this.combine(originalFeatures);
        final Layer layer = context.getLayerViewPanel().getSelectionManager().getLayersWithSelectedItems().iterator().next();
        this.execute(new UndoableCommand(this.getName()){

            @Override
            public void execute() throws Exception {
                layer.getFeatureCollectionWrapper().removeAll(originalFeatures);
                layer.getFeatureCollectionWrapper().add(combinedFeature);
            }

            @Override
            public void unexecute() throws Exception {
                layer.getFeatureCollectionWrapper().remove(combinedFeature);
                layer.getFeatureCollectionWrapper().addAll(originalFeatures);
            }
        }, context);
        context.getLayerViewPanel().getSelectionManager().getFeatureSelection().selectItems(layer, combinedFeature);
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private Feature combine(Collection<Feature> originalFeatures) {
        GeometryFactory factory = new GeometryFactory();
        Feature feature = (Feature)originalFeatures.iterator().next().clone();
        Class<?> narrowestCollectionClass = this.narrowestCollectionClass(originalFeatures);
        if (narrowestCollectionClass == MultiPoint.class) {
            feature.setGeometry((Geometry)factory.createMultiPoint(FeatureUtil.toGeometries(originalFeatures).toArray(new Point[originalFeatures.size()])));
        } else if (narrowestCollectionClass == MultiLineString.class) {
            feature.setGeometry((Geometry)factory.createMultiLineString(FeatureUtil.toGeometries(originalFeatures).toArray(new LineString[originalFeatures.size()])));
        } else if (narrowestCollectionClass == MultiPolygon.class) {
            feature.setGeometry((Geometry)factory.createMultiPolygon(FeatureUtil.toGeometries(originalFeatures).toArray(new Polygon[originalFeatures.size()])));
        } else {
            feature.setGeometry((Geometry)factory.createGeometryCollection(FeatureUtil.toGeometries(originalFeatures).toArray(new Geometry[originalFeatures.size()])));
        }
        return feature;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustHaveSelectedItemsCheck(1)).add(checkFactory.createAtLeastNFeaturesMustHaveSelectedItemsCheck(2)).add(checkFactory.createSelectedItemsLayersMustBeEditableCheck());
    }

    private Class<?> narrowestCollectionClass(Collection<Feature> features) {
        boolean hasPoints = false;
        boolean hasLineStrings = false;
        boolean hasPolygons = false;
        for (Feature feature : features) {
            if (feature.getGeometry() instanceof Point) {
                hasPoints = true;
                continue;
            }
            if (feature.getGeometry() instanceof LineString) {
                hasLineStrings = true;
                continue;
            }
            if (feature.getGeometry() instanceof Polygon) {
                hasPolygons = true;
                continue;
            }
            return GeometryCollection.class;
        }
        if (hasPoints && !hasLineStrings && !hasPolygons) {
            return MultiPoint.class;
        }
        if (!hasPoints && hasLineStrings && !hasPolygons) {
            return MultiLineString.class;
        }
        if (!hasPoints && !hasLineStrings && hasPolygons) {
            return MultiPolygon.class;
        }
        return GeometryCollection.class;
    }
}

