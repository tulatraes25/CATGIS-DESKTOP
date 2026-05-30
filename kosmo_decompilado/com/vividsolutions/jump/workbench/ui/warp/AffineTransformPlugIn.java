/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.warp;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.warp.AffineTransform;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.warp.WarpingVectorLayerFinder;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class AffineTransformPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.warp.AffineTransformPlugIn.name");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        AffineTransform transform = this.affineTransform(context);
        FeatureCollection featureCollection = transform.transform(((Layer)context.getSelectedLayer(0)).getFeatureCollectionWrapper());
        context.getLayerManager().addLayer(StandardCategoryNames.WORKING, String.valueOf(I18N.getString("workbench.ui.warp.AffineTransformPlugIn.affined")) + " - " + context.getSelectedLayer(0).getName(), featureCollection);
        AffineTransformPlugIn.checkValid(featureCollection, context);
        return true;
    }

    public static void checkValid(FeatureCollection featureCollection, PlugInContext context) throws Exception {
        FeatureIterator i = null;
        try {
            i = featureCollection.iterator();
            Feature feature = i.next();
            if (!feature.getGeometry().isValid()) {
                context.getLayerViewPanel().getContext().warnUser(I18N.getString("workbench.ui.warp.AffineTransformPlugIn.some-geometries-are-not-valid"));
                return;
            }
        }
        finally {
            if (i != null && i instanceof FeatureIterator) {
                i.close();
            }
        }
    }

    private Coordinate vectorCoordinate(int n, boolean tip, PlugInContext context, WarpingVectorLayerFinder vectorLayerManager) {
        LineString vector = (LineString)vectorLayerManager.getVectors().get(n);
        return tip ? vector.getCoordinateN(1) : vector.getCoordinateN(0);
    }

    private AffineTransform affineTransform(PlugInContext context) {
        WarpingVectorLayerFinder vlm = new WarpingVectorLayerFinder(context);
        switch (vlm.getVectors().size()) {
            case 1: {
                return new AffineTransform(this.vectorCoordinate(0, false, context, vlm), this.vectorCoordinate(0, true, context, vlm));
            }
            case 2: {
                return new AffineTransform(this.vectorCoordinate(0, false, context, vlm), this.vectorCoordinate(0, true, context, vlm), this.vectorCoordinate(1, false, context, vlm), this.vectorCoordinate(1, true, context, vlm));
            }
            case 3: {
                return new AffineTransform(this.vectorCoordinate(0, false, context, vlm), this.vectorCoordinate(0, true, context, vlm), this.vectorCoordinate(1, false, context, vlm), this.vectorCoordinate(1, true, context, vlm), this.vectorCoordinate(2, false, context, vlm), this.vectorCoordinate(2, true, context, vlm));
            }
        }
        Assert.shouldNeverReachHere();
        return null;
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck());
        check.add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck());
        check.add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
        check.add(checkFactory.createBetweenNAndMVectorsMustBeDrawnCheck(1, 3));
        return check;
    }
}

