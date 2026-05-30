/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.zoom;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxPlugIn;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomBar;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.NoninvertibleTransformException;

public class ZoomBarPlugIn
extends ToolboxPlugIn {
    private static final int WIDTH = 300;

    @Override
    protected void initializeToolbox(ToolboxDialog toolbox) {
        try {
            final ZoomBar zoomBar = new ZoomBar(true, false, toolbox.getContext().getWorkbench().getFrame());
            toolbox.getCenterPanel().add((Component)zoomBar, "Center");
            zoomBar.setPreferredSize(new Dimension(300, (int)zoomBar.getPreferredSize().getHeight()));
            toolbox.addWindowListener(new WindowAdapter(){

                @Override
                public void windowOpened(WindowEvent e) {
                    try {
                        zoomBar.updateComponents();
                    }
                    catch (NoninvertibleTransformException noninvertibleTransformException) {
                        // empty catch block
                    }
                }
            });
            toolbox.setInitialLocation(new GUIUtil.Location(20, false, 20, true));
        }
        catch (NoninvertibleTransformException x) {
            toolbox.getContext().getWorkbench().getFrame().handleThrowable(x);
        }
    }
}

