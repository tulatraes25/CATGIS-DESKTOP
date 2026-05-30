/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils.labels;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.utils.labels.ConfigureTextScaleInCADLayerDialog;

public class ScaleTextInCADLayerPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.labels.ScaleTextInCADLayerPlugIn.Scale-labels-in-CAD-Layer");
    public static final Icon ICON = IconLoader.icon("blank.png");
    private String selectedLayerName;
    private ConfigureTextScaleInCADLayerDialog dialog;

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layer layer = (Layer)context.getLayerNamePanel().getSelectedLayers()[0];
        this.selectedLayerName = layer.getName();
        this.dialog = new ConfigureTextScaleInCADLayerDialog(JUMPWorkbench.getFrameInstance(), true, layer);
        GUIUtil.centreOnScreen(this.dialog);
        this.dialog.setVisible(true);
        return this.dialog.wasOkPressed();
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
        return ScaleTextInCADLayerPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck result = new MultiEnableCheck();
        result.add(checkFactory.createTaskWindowMustBeActiveCheck());
        result.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        result.add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
        result.add(checkFactory.createSelectedLayerIsCADCheck());
        return result;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        Layer selectedLayer = JUMPWorkbench.getLayer(this.selectedLayerName);
        boolean isScaled = this.dialog.isScaled();
        double scaleMaxValue = this.dialog.getScaleMaxValue();
        double scaleMinValue = this.dialog.getScaleMinValue();
        FeatureTypeStyle style = selectedLayer.getModelStyle().getSelectedFeatureTypeStyle();
        Rule[] rules = style.getRules();
        int i = 0;
        while (i < rules.length) {
            Rule rule = rules[i];
            Symbolizer[] simbolos = rule.getSymbolizers();
            int j = 0;
            while (j < simbolos.length) {
                Symbolizer symbol = simbolos[j];
                if (symbol instanceof TextSymbolizer) {
                    TextSymbolizer textSymbol = (TextSymbolizer)symbol;
                    textSymbol.setScale(isScaled);
                    if (isScaled) {
                        textSymbol.setScaleMaxValue(scaleMaxValue);
                        textSymbol.setScaleMinValue(scaleMinValue);
                    }
                }
                ++j;
            }
            ++i;
        }
        selectedLayer.fireAppearanceChanged();
    }
}

