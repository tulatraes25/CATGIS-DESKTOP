/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.Point
 */
package org.saig.jump.plugin.utils.labels;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.AbstractBasicFeature;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.LayerUtil;

public class SaveLabelsAslayerPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.labels.SaveLabelsAslayerPlugIn.name");
    public static final Icon ICON = IconLoader.icon("extractLabels.png");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return SaveLabelsAslayerPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck result = new MultiEnableCheck();
        result.add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck());
        result.add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
        result.add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (!LayerUtil.hasLabelsActiveInCurrentStyle(workbenchContext.getLayerNamePanel().getSelectedLayers()[0])) {
                    return I18N.getString("org.saig.jump.plugin.utils.labels.SaveLabelsAslayerPlugIn.The-layer-default-style-does-not-contain-labels");
                }
                return null;
            }
        });
        return result;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.labels.SaveLabelsAslayerPlugIn.Saving-labels-to-a-new-layer")) + "...");
        Layer layer = (Layer)context.getWorkbenchContext().getLayerNamePanel().getSelectedLayers()[0];
        Rule[] rules = layer.getModelStyle().getSelectedFeatureTypeStyle().getRules();
        TextSymbolizer textSymbol = null;
        int i = 0;
        while (i < rules.length && textSymbol == null) {
            Symbolizer[] simbolos = rules[i].getSymbolizers();
            int j = 0;
            while (j < simbolos.length && textSymbol == null) {
                if (simbolos[j] instanceof TextSymbolizer) {
                    textSymbol = (TextSymbolizer)simbolos[j];
                }
                ++j;
            }
            ++i;
        }
        FeatureSchema schema = new FeatureSchema();
        schema.addAttribute("GEOM", AttributeType.GEOMETRY);
        schema.addAttribute("GID", AttributeType.LONG, Boolean.TRUE);
        String label = textSymbol.getLabel().toString();
        schema.addAttribute(label, AttributeType.STRING);
        schema.setGeometryType(1);
        FeatureDataset fc = new FeatureDataset(schema);
        int cont = 0;
        int totalItems = layer.getUltimateFeatureCollectionWrapper().size();
        boolean hasLabel = layer.getFeatureSchema().hasAttribute(label);
        FeatureIterator itFeatures = null;
        try {
            itFeatures = layer.getUltimateFeatureCollectionWrapper().iterator();
            while (itFeatures.hasNext()) {
                Feature element = itFeatures.next();
                if (element.getGeometry() == null) continue;
                BasicFeature newFeature = new BasicFeature(schema);
                Point geom = element.getGeometry().getCentroid();
                newFeature.setGeometry((Geometry)geom);
                if (hasLabel) {
                    newFeature.setAttribute(label, element.getAttribute(label));
                } else {
                    newFeature.setAttribute(label, ((AbstractBasicFeature)element).getExpression(label));
                }
                fc.addWithNewKey(newFeature);
                if (cont % 1000 != 0) continue;
                monitor.report(cont, totalItems, I18N.getString("org.saig.jump.plugin.utils.labels.SaveLabelsAslayerPlugIn.Processed-features"));
            }
        }
        finally {
            if (itFeatures != null) {
                itFeatures.close();
            }
        }
        if (fc.size() > 0) {
            Layer newLayer = context.addLayer(StandardCategoryNames.WORKING, String.valueOf(layer.getName()) + "_ETQ", fc);
            newLayer.setFeatureCollectionModified(false);
            context.getWorkbenchFrame().warnUser(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.labels.SaveLabelsAslayerPlugIn.Labels-layer-{0}-successfully-created")) + newLayer.getName() + " generada con \u00e9xito");
        } else {
            context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.labels.SaveLabelsAslayerPlugIn.No-labels-were-found"));
        }
    }
}

