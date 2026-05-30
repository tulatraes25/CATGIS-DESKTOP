/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.zoom;

import com.vividsolutions.jts.util.Assert;
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
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;

public class ZoomNextPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.zoom.ZoomNextPlugIn.name");
    public static final Icon ICON = IconLoader.icon("Right.gif");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Viewport viewport = context.getLayerViewPanel().getViewport();
        Assert.isTrue((boolean)viewport.getZoomHistory().hasNext());
        viewport.getZoomHistory().setAdding(false);
        try {
            viewport.zoom(viewport.getZoomHistory().next());
        }
        finally {
            viewport.getZoomHistory().setAdding(true);
        }
        return true;
    }

    public static MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return !workbenchContext.getLayerViewPanel().getViewport().getZoomHistory().hasNext() ? I18N.getString("workbench.ui.zoom.ZoomNextPlugIn.already-at-end") : null;
            }
        });
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
        return ZoomNextPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

