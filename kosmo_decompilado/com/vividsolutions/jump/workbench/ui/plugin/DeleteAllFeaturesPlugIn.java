/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.EditOptionsPanel;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class DeleteAllFeaturesPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.DeleteAllFeaturesPlugIn.name");
    public static final Icon ICON = IconLoader.icon("deleteAll.png");
    private static final Logger LOGGER = Logger.getLogger(DeleteAllFeaturesPlugIn.class);

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        ArrayList<EditTransaction> transactions = new ArrayList<EditTransaction>();
        for (Layer layer : Arrays.asList(context.getLayerNamePanel().getSelectedLayers())) {
            transactions.add(this.createTransaction(layer, context));
        }
        return EditTransaction.commit(transactions);
    }

    private EditTransaction createTransaction(Layer layer, PlugInContext context) {
        EditTransaction transaction = new EditTransaction(new ArrayList<Feature>(), this.getName(), layer, EditOptionsPanel.isRollingBackInvalidEdits(), true, context.getWorkbenchFrame());
        FeatureIterator itFeatures = null;
        try {
            try {
                itFeatures = layer.getUltimateFeatureCollectionWrapper().iterator();
                while (itFeatures.hasNext()) {
                    Feature feature = itFeatures.next();
                    transaction.deleteFeature(feature);
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (itFeatures != null) {
                    itFeatures.close();
                }
            }
        }
        finally {
            if (itFeatures != null) {
                itFeatures.close();
            }
        }
        return transaction;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustBeEditableCheck());
    }
}

