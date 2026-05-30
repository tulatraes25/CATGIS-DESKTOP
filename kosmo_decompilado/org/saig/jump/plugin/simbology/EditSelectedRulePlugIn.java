/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.simbology;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.TreeLayerNamePanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import org.saig.core.styling.Rule;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.simbology.AbstractSLDEditorPlugIn;

public class EditSelectedRulePlugIn
extends AbstractSLDEditorPlugIn {
    public static final String NAME = String.valueOf(I18N.getString("org.saig.jump.plugin.config.EditSelectedRulePlugIn.name")) + "...";
    public static final Icon ICON = IconLoader.icon("advancedStyleEditor.png");
    public static final Logger LOGGER = Logger.getLogger(EditSelectedRulePlugIn.class);

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
        return EditSelectedRulePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    protected Rule getRule(PlugInContext context) {
        LayerNamePanel layerNamePanel = context.getLayerNamePanel();
        if (!(layerNamePanel instanceof TreeLayerNamePanel)) {
            return null;
        }
        Collection<Rule> selectedRules = layerNamePanel.selectedNodes(Rule.class);
        if (selectedRules == null || selectedRules.size() != 1) {
            return null;
        }
        Rule rule = selectedRules.iterator().next();
        return rule;
    }

    @Override
    protected Layer getLayer(PlugInContext context) {
        TreeLayerNamePanel tree = (TreeLayerNamePanel)context.getLayerNamePanel();
        Object[] path = tree.getTree().getSelectionPath().getPath();
        if (path == null || path.length != 4) {
            return null;
        }
        Object layerPath = path[2];
        if (layerPath == null || !(layerPath instanceof Layer)) {
            return null;
        }
        return (Layer)layerPath;
    }

    public static EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory ecf = new EnableCheckFactory(workbenchContext);
        EnableCheck checkOnlyOneRuleIsSelected = new EnableCheck(){

            @Override
            public String check(JComponent component) {
                String errorMsg = null;
                LayerNamePanel panel = workbenchContext.getLayerNamePanel();
                int numRules = panel.selectedNodes(Rule.class).size();
                if (numRules != 1) {
                    errorMsg = I18N.getString("org.saig.jump.plugin.config.EditSelectedRulePlugIn.exactly-one-rule-must-be-selected");
                }
                return errorMsg;
            }
        };
        EnableCheck[] checks = new EnableCheck[]{ecf.createWindowWithLayerNamePanelMustBeActiveCheck(), checkOnlyOneRuleIsSelected};
        MultiEnableCheck mec = new MultiEnableCheck();
        EnableCheck[] enableCheckArray = checks;
        int n = checks.length;
        int n2 = 0;
        while (n2 < n) {
            EnableCheck check = enableCheckArray[n2];
            mec.add(check);
            ++n2;
        }
        return mec;
    }
}

