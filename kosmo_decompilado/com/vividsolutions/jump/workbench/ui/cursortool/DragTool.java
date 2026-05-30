/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public abstract class DragTool
extends AbstractCursorTool {
    private int viewClickBuffer = 2;
    protected Coordinate modelSource = null;
    protected Coordinate modelDestination = null;
    private boolean dragApproved = false;
    protected Point2D source;

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        this.dragApproved = true;
        try {
            this.setViewSource(e.getPoint());
        }
        catch (NoninvertibleTransformException x) {
            this.getPanel().getContext().handleThrowable(x);
        }
    }

    protected void setViewClickBuffer(int clickBuffer) {
        this.viewClickBuffer = clickBuffer;
    }

    protected boolean wasClick() {
        return this.getModelSource().equals((Object)this.getModelDestination());
    }

    protected Envelope getBoxInModelCoordinates() throws NoninvertibleTransformException {
        double minX = Math.min(this.getModelSource().x, this.getModelDestination().x);
        double maxX = Math.max(this.getModelSource().x, this.getModelDestination().x);
        double minY = Math.min(this.getModelSource().y, this.getModelDestination().y);
        double maxY = Math.max(this.getModelSource().y, this.getModelDestination().y);
        if (this.wasClick()) {
            minX -= this.modelClickBuffer();
            maxX += this.modelClickBuffer();
            minY -= this.modelClickBuffer();
            maxY += this.modelClickBuffer();
        }
        return new Envelope(minX, maxX, minY, maxY);
    }

    protected double modelClickBuffer() {
        return (double)this.viewClickBuffer / this.getPanel().getViewport().getScale();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        try {
            if (!this.dragApproved) {
                return;
            }
            this.setViewDestination(e.getPoint());
            this.redrawShape();
        }
        catch (Throwable t) {
            this.getPanel().getContext().handleThrowable(t);
        }
    }

    protected Coordinate getModelSource() {
        return this.modelSource;
    }

    protected Coordinate getModelDestination() {
        return this.modelDestination;
    }

    protected void setModelSource(Coordinate source) {
        this.modelSource = source;
    }

    protected void setViewSource(Point2D source) throws NoninvertibleTransformException {
        this.source = source;
        this.setModelSource(this.getPanel().getViewport().toModelCoordinate(source));
    }

    protected void setViewDestination(Point2D destination) throws NoninvertibleTransformException {
        this.setModelDestination(this.getPanel().getViewport().toModelCoordinate(destination));
    }

    protected void setModelDestination(Coordinate destination) {
        this.modelDestination = this.snap(destination);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!this.activate) {
            return;
        }
        try {
            boolean dragComplete = this.isShapeOnScreen();
            this.clearShape();
            if (dragComplete) {
                this.fireGestureFinished();
            }
            this.dragApproved = false;
        }
        catch (Throwable t) {
            this.getPanel().getContext().handleThrowable(t);
        }
    }

    @Override
    protected Shape getShape() throws Exception {
        return this.getShape(this.getViewSource(), this.getViewDestination());
    }

    protected Point2D getViewSource() throws NoninvertibleTransformException {
        return this.getPanel().getViewport().toViewPoint(this.getModelSource());
    }

    protected Point2D getViewDestination() throws NoninvertibleTransformException {
        return this.getPanel().getViewport().toViewPoint(this.getModelDestination());
    }

    protected Shape getShape(Point2D source, Point2D destination) throws Exception {
        double minX = Math.min(source.getX(), destination.getX());
        double minY = Math.min(source.getY(), destination.getY());
        double maxX = Math.max(source.getX(), destination.getX());
        double maxY = Math.max(source.getY(), destination.getY());
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    public Point2D getSource() {
        return this.source;
    }
}

