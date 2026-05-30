/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.zoom;

import com.vividsolutions.jump.util.MathUtil;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.config.ConfigZoomPanel;

public class ZoomTool
extends DragTool {
    public static final String NAME = I18N.getString("workbench.ui.zoom.ZoomTool.name");
    public static final Icon ICON = IconLoader.icon("Magnify.gif");
    public static final Cursor CURSOR = ZoomTool.createCursor(IconLoader.icon("MagnifyCursor.gif").getImage());
    private static final int BOX_TOLERANCE = 4;

    public ZoomTool() {
        this.setColor(Color.BLACK);
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
    public Cursor getCursor() {
        return CURSOR;
    }

    @Override
    protected void gestureFinished() throws NoninvertibleTransformException {
        this.reportNothingToUndoYet();
        double minX = Math.min(this.getViewSource().getX(), this.getViewDestination().getX());
        double maxX = Math.max(this.getViewSource().getX(), this.getViewDestination().getX());
        double minY = Math.min(this.getViewSource().getY(), this.getViewDestination().getY());
        double maxY = Math.max(this.getViewSource().getY(), this.getViewDestination().getY());
        double widthOfNewViewAsPerceivedByOldView = maxX - minX;
        double heightOfNewViewAsPerceivedByOldView = maxY - minY;
        if (widthOfNewViewAsPerceivedByOldView == 0.0 && heightOfNewViewAsPerceivedByOldView == 0.0) {
            return;
        }
        if (widthOfNewViewAsPerceivedByOldView < 4.0 && heightOfNewViewAsPerceivedByOldView < 4.0) {
            double zoomFactor = ConfigZoomPanel.getZoomFactor();
            this.zoomAt(new Point2D.Double(MathUtil.avg(minX, maxX), MathUtil.avg(minY, maxY)), zoomFactor);
            return;
        }
        Point2D.Double centreOfNewViewAsPerceivedByOldView = new Point2D.Double(minX + widthOfNewViewAsPerceivedByOldView / 2.0, minY + heightOfNewViewAsPerceivedByOldView / 2.0);
        this.getPanel().getViewport().zoom(centreOfNewViewAsPerceivedByOldView, widthOfNewViewAsPerceivedByOldView, heightOfNewViewAsPerceivedByOldView);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            double zoomFactor = ConfigZoomPanel.getZoomFactor();
            zoomFactor = SwingUtilities.isRightMouseButton(e) ? 1.0 / zoomFactor : zoomFactor;
            this.zoomAt(e.getPoint(), zoomFactor);
        }
        catch (Throwable t) {
            this.getPanel().getContext().handleThrowable(t);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        try {
            if (SwingUtilities.isLeftMouseButton(e)) {
                super.mousePressed(e);
            }
        }
        catch (Throwable t) {
            this.getPanel().getContext().handleThrowable(t);
        }
    }

    protected void zoomAt(Point2D p, double zoomFactor) throws NoninvertibleTransformException {
        this.getPanel().getViewport().zoomToViewPoint(p, zoomFactor);
    }

    @Override
    public boolean isRightMouseButtonUsed() {
        return true;
    }
}

