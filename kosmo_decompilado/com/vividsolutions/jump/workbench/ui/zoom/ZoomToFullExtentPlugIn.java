/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.zoom;

import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.config.ConfigZoomPanel;

public class ZoomToFullExtentPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.zoom.ZoomToFullExtentPlugIn.name");
    public static final Icon ICON = IconLoader.icon("World.gif");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Viewport viewport = context.getLayerViewPanel().getViewport();
        viewport.zoom(EnvelopeUtil.bufferByFraction(viewport.fullExtent(), ConfigZoomPanel.getExtentFraction()));
        return true;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustExistCheck(1));
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EnableCheck getCheck() {
        return ZoomToFullExtentPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

