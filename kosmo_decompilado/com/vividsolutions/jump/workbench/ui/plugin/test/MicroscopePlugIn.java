/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.workbench.ui.plugin.test;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;
import com.vividsolutions.jump.geom.GeometryMicroscope;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.MainMenuNames;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.saig.jump.lang.I18N;

public class MicroscopePlugIn
extends AbstractPlugIn {
    @Override
    public void initialize(PlugInContext context) throws Exception {
        EnableCheckFactory checkFactory = new EnableCheckFactory(context.getWorkbenchContext());
        context.getFeatureInstaller().addMainMenuItem(this, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_OTHERS}, this.getName(), false, null, new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createFenceMustBeDrawnCheck()));
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        FeatureCollectionWrapper fc = ((Layer)context.getSelectedLayer(0)).getFeatureCollectionWrapper();
        Envelope fence = context.getLayerViewPanel().getFence().getEnvelopeInternal();
        FeatureCollection magFC = this.magnify(fc, fence);
        Layer lyr = context.addLayer(StandardCategoryNames.QA, I18N.getString("workbench.ui.plugin.test.MicroscopePlugIn.microscope"), magFC);
        lyr.getBasicStyle().setFillColor(Color.red);
        lyr.getBasicStyle().setLineColor(Color.red);
        lyr.getBasicStyle().setAlpha(100);
        lyr.getVertexStyle().setEnabled(true);
        lyr.fireAppearanceChanged();
        return true;
    }

    private FeatureCollection magnify(FeatureCollection fc, Envelope env) throws Exception {
        ArrayList<Geometry> geomList = new ArrayList<Geometry>();
        for (Feature feature : fc.query(env)) {
            geomList.add((Geometry)feature.getGeometry().clone());
        }
        double minSep = env.getWidth() / 20.0;
        GeometryMicroscope micro = new GeometryMicroscope(geomList, env, minSep);
        List<Geometry> result = micro.getAdjusted();
        return FeatureDatasetFactory.createFromGeometry(result);
    }
}

