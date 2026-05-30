/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

import java.awt.Font;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class TextStroke
implements Stroke {
    private String text;
    private Font font;
    private boolean stretchToFit = false;
    private boolean repeat = false;
    private AffineTransform t = new AffineTransform();
    private static final float FLATNESS = 1.0f;

    public TextStroke(String text, Font font) {
        this(text, font, true, false);
    }

    public TextStroke(String text, Font font, boolean stretchToFit, boolean repeat) {
        this.text = text;
        this.font = font;
        this.stretchToFit = stretchToFit;
        this.repeat = repeat;
    }

    @Override
    public Shape createStrokedShape(Shape shape) {
        FontRenderContext frc = new FontRenderContext(null, true, true);
        GlyphVector glyphVector = this.font.createGlyphVector(frc, this.text);
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
        int currentChar = 0;
        int length = glyphVector.getNumGlyphs();
        if (length == 0) {
            return result;
        }
        float factor = this.stretchToFit ? this.measurePathLength(shape) / (float)glyphVector.getLogicalBounds().getWidth() : 1.0f;
        float nextAdvance = 0.0f;
        while (currentChar < length && !it.isDone()) {
            type = it.currentSegment(points);
            switch (type) {
                case 0: {
                    moveX = lastX = points[0];
                    moveY = lastY = points[1];
                    result.moveTo(moveX, moveY);
                    first = true;
                    next = nextAdvance = glyphVector.getGlyphMetrics(currentChar).getAdvance() * 0.5f;
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
                        while (currentChar < length && distance >= next) {
                            Shape glyph = glyphVector.getGlyphOutline(currentChar);
                            Point2D p = glyphVector.getGlyphPosition(currentChar);
                            float px = (float)p.getX();
                            float py = (float)p.getY();
                            float x = lastX + next * dx * r;
                            float y = lastY + next * dy * r;
                            float advance = nextAdvance;
                            nextAdvance = currentChar < length - 1 ? glyphVector.getGlyphMetrics(currentChar + 1).getAdvance() * 0.5f : 0.0f;
                            this.t.setToTranslation(x, y);
                            this.t.rotate(angle);
                            this.t.translate(-px - advance, -py);
                            result.append(this.t.createTransformedShape(glyph), false);
                            next += (advance + nextAdvance) * factor;
                            ++currentChar;
                            if (!this.repeat) continue;
                            currentChar %= length;
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

    public float measurePathLength(Shape shape) {
        FlatteningPathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 1.0);
        float[] points = new float[6];
        float moveX = 0.0f;
        float moveY = 0.0f;
        float lastX = 0.0f;
        float lastY = 0.0f;
        float thisX = 0.0f;
        float thisY = 0.0f;
        int type = 0;
        float total = 0.0f;
        while (!it.isDone()) {
            type = it.currentSegment(points);
            switch (type) {
                case 0: {
                    moveX = lastX = points[0];
                    moveY = lastY = points[1];
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
                    total += (float)Math.sqrt(dx * dx + dy * dy);
                    lastX = thisX;
                    lastY = thisY;
                }
            }
            it.next();
        }
        return total;
    }
}

