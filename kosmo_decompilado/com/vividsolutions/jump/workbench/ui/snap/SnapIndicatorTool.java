/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.lang.ArrayUtils
 */
package com.vividsolutions.jump.workbench.ui.snap;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.snap.SnapManager;
import com.vividsolutions.jump.workbench.ui.snap.SnapPolicy;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Collection;
import javax.swing.Icon;
import org.apache.commons.lang.ArrayUtils;

public class SnapIndicatorTool
extends AbstractCursorTool {
    private Point2D indicatorLocation;
    private Color snappedColor;
    private Color unsnappedColor;
    private double diameter;

    public SnapIndicatorTool(Collection<SnapPolicy> snapPolicies) {
        this(Color.green, Color.red, 20.0, snapPolicies);
    }

    public SnapIndicatorTool(Color snappedColor, Color unsnappedColor, double diameter, Collection<SnapPolicy> snapPolicies) {
        this.getSnapManager().addPolicies(snapPolicies);
        this.setFilling(false);
        this.setStroke(new BasicStroke(4.0f));
        this.snappedColor = snappedColor;
        this.unsnappedColor = unsnappedColor;
        this.diameter = diameter;
    }

    public void setNewPolicies(Collection<SnapPolicy> snapPolicies) {
        this.getSnapManager().setNewPolicies(snapPolicies);
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    protected void gestureFinished() throws Exception {
        Assert.shouldNeverReachHere();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.mouseLocationChanged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.mouseLocationChanged(e);
    }

    @Override
    protected Shape getShape() throws NoninvertibleTransformException {
        return new Ellipse2D.Double(this.indicatorLocation.getX() - this.diameter / 2.0, this.indicatorLocation.getY() - this.diameter / 2.0, this.diameter, this.diameter);
    }

    private void mouseLocationChanged(MouseEvent e) {
        try {
            Point p = e.getPoint();
            Point2D pe = this.getPanel().getViewport().toViewPoint(this.snap(p));
            this.clearShape();
            if (!(Math.abs(pe.getX() - (double)p.x) <= 1.0E-7) || !(Math.abs(pe.getY() - (double)p.y) <= 1.0E-7)) {
                this.indicatorLocation = pe;
                SnapManager snapManager = this.getSnapManager();
                if (snapManager.wasSnapCoordinateFound()) {
                    this.setColor(snapManager.getSnapCurrentColor());
                    this.redrawShape();
                }
            }
        }
        catch (Throwable t) {
            this.getPanel().getContext().handleThrowable(t);
        }
    }

    @Override
    public boolean isGestureInProgress() {
        return false;
    }

    @Override
    public void activate(LayerViewPanel layerViewPanel) {
        super.activate(layerViewPanel);
        if (!ArrayUtils.contains((Object[])this.getPanel().getMouseMotionListeners(), (Object)this)) {
            this.getPanel().addMouseMotionListener(this);
        }
    }

    @Override
    public void deactivate() {
        this.getPanel().removeMouseMotionListener(this);
        super.deactivate();
    }
}

