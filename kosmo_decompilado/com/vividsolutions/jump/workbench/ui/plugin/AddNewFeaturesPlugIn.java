/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.EnterWKTDialog;
import com.vividsolutions.jump.workbench.ui.plugin.WKTPlugIn;
import java.util.ArrayList;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class AddNewFeaturesPlugIn
extends WKTPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.AddNewFeaturesPlugIn.name");

    @Override
    protected Layer layer(PlugInContext context) {
        return context.getLayerNamePanel().chooseEditableLayer();
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        return super.execute(context);
    }

    @Override
    protected void apply(FeatureCollection c, PlugInContext context) throws Exception {
        final ArrayList<Feature> features = new ArrayList<Feature>();
        FeatureIterator i = null;
        try {
            i = c.iterator();
            Feature feature = i.next();
            features.add(FeatureUtil.toFeature(feature.getGeometry(), c.getFeatureSchema()));
        }
        finally {
            if (i != null && i instanceof FeatureIterator) {
                i.close();
            }
        }
        this.execute(new UndoableCommand(this.getName()){

            @Override
            public void execute() throws Exception {
                AddNewFeaturesPlugIn.this.layer.getFeatureCollectionWrapper().addAll(features);
            }

            @Override
            public void unexecute() throws Exception {
                AddNewFeaturesPlugIn.this.layer.getFeatureCollectionWrapper().removeAll(features);
            }
        }, context);
    }

    @Override
    protected EnterWKTDialog createDialog(PlugInContext context) {
        EnterWKTDialog d = super.createDialog(context);
        d.setTitle(String.valueOf(I18N.getString("workbench.ui.plugin.AddNewFeaturesPlugIn.add-features-to")) + " " + this.layer);
        d.setDescription("<HTML>" + I18N.getString("workbench.ui.plugin.AddNewFeaturesPlugIn.enter-WKT-for-one-or-more-geometries") + ".</HTML>");
        return d;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1)).add(checkFactory.createSelectedLayersMustBeEditableCheck());
    }

    @Override
    public String getName() {
        return NAME;
    }
}

