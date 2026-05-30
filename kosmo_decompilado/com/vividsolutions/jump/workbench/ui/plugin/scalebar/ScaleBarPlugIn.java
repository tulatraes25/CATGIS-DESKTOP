/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin.scalebar;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.InstallScaleBarPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.ScaleBarRenderer;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;

public class ScaleBarPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.scalebar.ScaleBarPlugIn.name");
    public static final Icon ICON = IconLoader.icon("Ruler.gif");

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
        return ScaleBarPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        InstallScaleBarPlugIn installScaleBarPlugIn = new InstallScaleBarPlugIn();
        installScaleBarPlugIn.initialize(context);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        ScaleBarRenderer.setEnabled(!ScaleBarRenderer.isEnabled(context.getLayerViewPanel()), context.getLayerViewPanel());
        context.getLayerViewPanel().getRenderingManager().render("SCALE_BAR");
        return true;
    }

    public static MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck());
        check.add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                ((JCheckBoxMenuItem)component).setSelected(ScaleBarRenderer.isEnabled(workbenchContext.getLayerViewPanel()));
                return null;
            }
        });
        return check;
    }
}

