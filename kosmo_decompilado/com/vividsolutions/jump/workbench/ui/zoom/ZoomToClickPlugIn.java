/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.zoom;

import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.config.ConfigZoomPanel;

public class ZoomToClickPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.zoom.ZoomToClickPlugIn.name");
    public static final String NAME_IN = I18N.getString("workbench.ui.zoom.ZoomToClickPlugIn.zoom-in");
    public static final String NAME_OUT = I18N.getString("workbench.ui.zoom.ZoomToClickPlugIn.zoom-out");
    public static final Icon ICON = IconLoader.icon("QuickSnap.gif");
    public static final Icon ICON_IN = IconLoader.icon("acercarse.png");
    public static final Icon ICON_OUT = IconLoader.icon("alejarse.png");
    private double zoomType;

    public ZoomToClickPlugIn(double zoomType) {
        this.zoomType = zoomType;
        if (zoomType == 1.0) {
            this.registerCenterKey(JUMPWorkbench.getFrameInstance().getContext());
        }
    }

    @Override
    public String getName() {
        if (this.zoomType != 1.0) {
            if (this.zoomType > 1.0) {
                return NAME_IN;
            }
            return NAME_OUT;
        }
        return NAME;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        LayerViewPanel viewPanel = context.getWorkbenchContext().getLastClickedLayerViewPanel();
        if (viewPanel != null) {
            double zoomFactor = ConfigZoomPanel.getZoomFactor();
            if (this.zoomType == 1.0) {
                zoomFactor = 1.0;
            } else if (this.zoomType < 1.0) {
                zoomFactor = 1.0 / zoomFactor;
            }
            viewPanel.getViewport().zoomToViewPoint(viewPanel.getLastClickedPoint(), zoomFactor);
        }
        return true;
    }

    @Override
    public Icon getIcon() {
        if (this.zoomType != 1.0) {
            if (this.zoomType > 1.0) {
                return ICON_IN;
            }
            return ICON_OUT;
        }
        return ICON;
    }

    private void registerCenterKey(final WorkbenchContext context) {
        context.getWorkbench().getFrame().addEasyKeyListener(new KeyListener(){

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (context.getWorkbench().getFrame().getActiveInternalFrame() instanceof TaskFrame && e.isControlDown() && e.getKeyCode() == 88) {
                    context.getLayerManager().getUndoableEditReceiver().startReceiving();
                    try {
                        try {
                            Point2D point = context.getLayerViewPanel().getViewport().toViewPoint(context.getLayerViewPanel().getCurrentCursorPoint());
                            context.setLastClickedLayerViewPanel(context.getLayerViewPanel());
                            context.getLayerViewPanel().setLastClickedPoint(new Point((int)point.getX(), (int)point.getY()));
                            ZoomToClickPlugIn.this.execute(context.createPlugInContext());
                        }
                        catch (Exception x) {
                            context.getWorkbench().getFrame().log(StringUtil.stackTrace(x));
                            context.getLayerManager().getUndoableEditReceiver().stopReceiving();
                        }
                    }
                    finally {
                        context.getLayerManager().getUndoableEditReceiver().stopReceiving();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
    }
}

