/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.GeometryCollection
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;

public class ExplodeSelectedFeaturesPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.ExplodeSelectedFeaturesPlugIn.name");

    @Override
    public boolean execute(final PlugInContext context) throws Exception {
        final ArrayList<EditTransaction> transactions = new ArrayList<EditTransaction>();
        for (Layer layerWithSelectedItems : context.getLayerViewPanel().getSelectionManager().getLayersWithSelectedItems()) {
            transactions.add(this.createTransaction(layerWithSelectedItems, context));
        }
        return EditTransaction.commit(transactions, new EditTransaction.SuccessAction(){

            @Override
            public void run() {
                for (EditTransaction transaction : transactions) {
                    context.getLayerViewPanel().getSelectionManager().getFeatureSelection().selectItems(transaction.getLayer(), ExplodeSelectedFeaturesPlugIn.this.newFeatures(transaction));
                }
            }
        });
    }

    @Override
    public String getName() {
        return NAME;
    }

    private Collection newFeatures(EditTransaction transaction) {
        ArrayList<Feature> newFeatures = new ArrayList<Feature>();
        int i = 0;
        while (i < transaction.size()) {
            if (!transaction.getGeometry(i).isEmpty()) {
                newFeatures.add(transaction.getFeature(i));
            }
            ++i;
        }
        return newFeatures;
    }

    private EditTransaction createTransaction(Layer layer, PlugInContext context) {
        Collection<Feature> intactFeatures = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(layer);
        EditTransaction transaction = new EditTransaction(new ArrayList<Feature>(), this.getName(), layer, this.isRollingBackInvalidEdits(), true, context.getLayerViewPanel());
        for (Feature intactFeature : intactFeatures) {
            transaction.deleteFeature(intactFeature);
        }
        for (Feature explodedFeature : this.explode(intactFeatures)) {
            transaction.createFeature(explodedFeature);
        }
        return transaction;
    }

    private List explode(Collection features) {
        ArrayList<Feature> explodedFeatures = new ArrayList<Feature>();
        for (Feature feature : features) {
            GeometryCollection collection = (GeometryCollection)feature.getGeometry();
            int j = 0;
            while (j < collection.getNumGeometries()) {
                Feature explodedFeature = (Feature)feature.clone();
                explodedFeature.setGeometry(collection.getGeometryN(j));
                explodedFeatures.add(explodedFeature);
                ++j;
            }
        }
        return explodedFeatures;
    }

    public static MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createAtLeastNFeaturesMustHaveSelectedItemsCheck(1)).add(checkFactory.createSelectedItemsLayersMustBeEditableCheck()).add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Collection<Feature> featuresWithSelectedItems = workbenchContext.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems();
                for (Feature feature : featuresWithSelectedItems) {
                    if (feature.getGeometry() instanceof GeometryCollection) continue;
                    return String.valueOf(I18N.getString("workbench.ui.plugin.ExplodeSelectedFeaturesPlugIn.selected-feature")) + StringUtil.s(featuresWithSelectedItems.size()) + " " + I18N.getString("workbench.ui.plugin.ExplodeSelectedFeaturesPlugIn.must-be-geometry-collection") + StringUtil.s(featuresWithSelectedItems.size());
                }
                return null;
            }
        });
    }
}

