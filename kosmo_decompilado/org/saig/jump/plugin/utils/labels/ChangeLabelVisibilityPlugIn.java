/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils.labels;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Style;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.LayerUtil;

public class ChangeLabelVisibilityPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.ChangeLabelVisibility.labels");
    public static final Icon ICON = IconLoader.icon("font.gif");

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
        return ChangeLabelVisibilityPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layer layer = (Layer)context.getSelectedLayer(0);
        Style modelStyle = layer.getModelStyle();
        FeatureTypeStyle[] featureTypeStyles = modelStyle.getFeatureTypeStyles();
        int cont = 0;
        while (cont < featureTypeStyles.length) {
            FeatureTypeStyle fts = featureTypeStyles[cont];
            Rule[] rules = fts.getRules();
            int i = 0;
            while (i < rules.length) {
                Symbolizer[] simbolos = rules[i].getSymbolizers();
                int j = 0;
                while (j < simbolos.length) {
                    Symbolizer simbolo = simbolos[j];
                    if (simbolo instanceof TextSymbolizer) {
                        simbolo.setActive(!simbolo.isActive());
                    }
                    ++j;
                }
                ++i;
            }
            ++cont;
        }
        layer.fireAppearanceChanged();
        return true;
    }

    public static EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck result = new MultiEnableCheck();
        result.add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck());
        result.add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
        result.add(checkFactory.createSelectedLayersMustNotBeWMSLayersCheck());
        result.add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                ((JCheckBoxMenuItem)component).setSelected(LayerUtil.hasLabelsActive(workbenchContext.getLayerNamePanel().getSelectedLayers()[0]));
                return null;
            }
        });
        result.add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (!LayerUtil.hasLabels(workbenchContext.getLayerNamePanel().getSelectedLayers()[0])) {
                    return I18N.getString("org.saig.jump.plugin.utils.ChangeLabelVisibility.the-layer-does-not-contain-labels");
                }
                return null;
            }
        });
        return result;
    }
}

