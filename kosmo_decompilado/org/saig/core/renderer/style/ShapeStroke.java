/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

public class ShapeStroke
implements Stroke {
    protected Shape[] shapes;
    protected float advance;
    protected boolean stretchToFit = false;
    protected boolean repeat = true;
    protected AffineTransform t = new AffineTransform();
    protected static final float FLATNESS = 1.0f;

    public ShapeStroke(Shape shapes, float advance) {
        this(new Shape[]{shapes}, advance);
    }

    public ShapeStroke(Shape[] shapes, float advance) {
        this.advance = advance;
        this.shapes = new Shape[shapes.length];
        int i = 0;
        while (i < this.shapes.length) {
            Rectangle2D bounds = shapes[i].getBounds2D();
            this.t.setToTranslation(-bounds.getCenterX(), -bounds.getCenterY());
            this.shapes[i] = this.t.createTransformedShape(shapes[i]);
            ++i;
        }
    }

    @Override
    public Shape createStrokedShape(Shape shape) {
        GeneralPath result = new GeneralPath();
        FlatteningPathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 1.0);
        float[] points = new float[6];
        float moveX = 0.0f;
        float moveY = 0.0f;
        float lastX = 0.0f;
        float lastY = 0.0f;
        float thisX = 0.0f;
        float thisY = 0.0f;
        int type = 0;
        boolean first = false;
        float next = 0.0f;
        int currentShape = 0;
        int length = this.shapes.length;
        while (currentShape < length && !it.isDone()) {
            type = it.currentSegment(points);
            switch (type) {
                case 0: {
                    moveX = lastX = points[0];
                    moveY = lastY = points[1];
                    result.moveTo(moveX, moveY);
                    first = true;
                    next = 0.0f;
                    break;
                }
                case 4: {
                    points[0] = moveX;
                    points[1] = moveY;
                }
                case 1: {
                    thisX = points[0];
                    thisY = points[1];
                    float dx = thisX - lastX;
                    float dy = thisY - lastY;
                    float distance = (float)Math.sqrt(dx * dx + dy * dy);
                    if (distance >= next) {
                        float r = 1.0f / distance;
                        float angle = (float)Math.atan2(dy, dx);
                        while (currentShape < length && distance >= next) {
                            float x = lastX + next * dx * r;
                            float y = lastY + next * dy * r;
                            this.t.setToTranslation(x, y);
                            this.t.rotate(angle);
                            result.append(this.t.createTransformedShape(this.shapes[currentShape]), false);
                            next += this.advance;
                            ++currentShape;
                            if (!this.repeat) continue;
                            currentShape %= length;
                        }
                    }
                    next -= distance;
                    first = false;
                    lastX = thisX;
                    lastY = thisY;
                }
            }
            it.next();
        }
        return result;
    }
}

