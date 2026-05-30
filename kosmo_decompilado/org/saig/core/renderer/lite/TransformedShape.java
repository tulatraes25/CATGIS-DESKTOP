/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.resources.geometry.XAffineTransform
 */
package org.saig.core.renderer.lite;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.geotools.resources.geometry.XAffineTransform;

public final class TransformedShape
extends AffineTransform
implements Shape {
    public Shape shape;
    private final Point2D.Double point = new Point2D.Double();
    private final Rectangle2D.Double rectangle = new Rectangle2D.Double();

    public void getMatrix(float[] matrix, int offset) {
        matrix[offset] = (float)this.getScaleX();
        matrix[++offset] = (float)this.getShearY();
        matrix[++offset] = (float)this.getShearX();
        matrix[++offset] = (float)this.getScaleY();
        matrix[++offset] = (float)this.getTranslateX();
        matrix[++offset] = (float)this.getTranslateY();
    }

    public void setTransform(float[] matrix, int offset) {
        this.setTransform(matrix[offset], matrix[++offset], matrix[++offset], matrix[++offset], matrix[++offset], matrix[++offset]);
    }

    public void setTransform(double[] matrix) {
        this.setTransform(matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
    }

    public void scale(double s) {
        this.scale(s, s);
    }

    @Override
    public boolean contains(double x, double y) {
        this.point.x = x;
        this.point.y = y;
        return this.contains(this.point);
    }

    @Override
    public boolean contains(Point2D p) {
        try {
            return this.shape.contains(this.inverseTransform(p, this.point));
        }
        catch (NoninvertibleTransformException exception) {
            TransformedShape.exceptionOccured(exception, "contains");
            return false;
        }
    }

    @Override
    public boolean contains(double x, double y, double width, double height) {
        this.rectangle.x = x;
        this.rectangle.y = y;
        this.rectangle.width = width;
        this.rectangle.height = height;
        return this.contains(this.rectangle);
    }

    @Override
    public boolean contains(Rectangle2D r) {
        try {
            return this.shape.contains(XAffineTransform.inverseTransform((AffineTransform)this, (Rectangle2D)r, (Rectangle2D)this.rectangle));
        }
        catch (NoninvertibleTransformException exception) {
            TransformedShape.exceptionOccured(exception, "contains");
            return false;
        }
    }

    @Override
    public boolean intersects(double x, double y, double width, double height) {
        this.rectangle.x = x;
        this.rectangle.y = y;
        this.rectangle.width = width;
        this.rectangle.height = height;
        return this.intersects(this.rectangle);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        try {
            return this.shape.intersects(XAffineTransform.inverseTransform((AffineTransform)this, (Rectangle2D)r, (Rectangle2D)this.rectangle));
        }
        catch (NoninvertibleTransformException exception) {
            TransformedShape.exceptionOccured(exception, "intersects");
            return false;
        }
    }

    @Override
    public Rectangle getBounds() {
        Rectangle rect = this.shape.getBounds();
        return (Rectangle)XAffineTransform.transform((AffineTransform)this, (Rectangle2D)rect, (Rectangle2D)rect);
    }

    @Override
    public Rectangle2D getBounds2D() {
        Rectangle2D rect = this.shape.getBounds2D();
        return XAffineTransform.transform((AffineTransform)this, (Rectangle2D)rect, null);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        if (!this.isIdentity()) {
            if (at == null || at.isIdentity()) {
                return this.shape.getPathIterator(this);
            }
            at = new AffineTransform(at);
            at.concatenate(this);
        }
        return this.shape.getPathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        if (!this.isIdentity()) {
            if (at == null || at.isIdentity()) {
                return this.shape.getPathIterator(this, flatness);
            }
            at = new AffineTransform(at);
            at.concatenate(this);
        }
        return this.shape.getPathIterator(at, flatness);
    }

    private static void exceptionOccured(NoninvertibleTransformException exception, String method) {
        LogRecord record = new LogRecord(Level.WARNING, exception.getLocalizedMessage());
        record.setSourceClassName("TransformedShape");
        record.setSourceMethodName(method);
        record.setThrown(exception);
        Logger.getLogger("org.geotools.renderer.j2d").log(record);
    }
}

