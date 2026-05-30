/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.config;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import org.saig.core.styling.Rule;
import org.saig.jump.lang.I18N;

public class EnabledRulePlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.config.EnabledLayerPlugIn.Enable-disable");
    public static final Icon ICON = IconLoader.icon("blank.png");
    public static final Logger LOGGER = Logger.getLogger(EnabledRulePlugIn.class);

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
        return EnabledRulePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Collection<Rule> selectedRules = context.getLayerNamePanel().selectedNodes(Rule.class);
        boolean repaint = false;
        for (Rule currentRule : selectedRules) {
            currentRule.setEnabled(!currentRule.isEnabled());
            repaint = true;
        }
        if (repaint) {
            context.getLayerViewPanel().repaint(true);
        }
        return true;
    }

    public static EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                ((JCheckBoxMenuItem)component).setSelected(workbenchContext.getLayerNamePanel().selectedNodes(Rule.class).iterator().next().isEnabled());
                return null;
            }
        });
    }
}

