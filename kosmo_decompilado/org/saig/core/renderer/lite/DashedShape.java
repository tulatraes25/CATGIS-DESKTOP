/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.lite;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class DashedShape
implements Shape {
    Shape shape;
    float[] dashArray;
    float dashPhase;

    public DashedShape(Shape shape, float[] dashArray, float dashPhase) {
        this.shape = shape;
        this.dashArray = dashArray;
        this.dashPhase = dashPhase;
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return this.shape.contains(x, y, w, h);
    }

    @Override
    public boolean contains(double x, double y) {
        return this.shape.contains(x, y);
    }

    @Override
    public boolean contains(Point2D p) {
        return this.shape.contains(p);
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return this.shape.contains(r);
    }

    @Override
    public Rectangle getBounds() {
        return this.shape.getBounds();
    }

    @Override
    public Rectangle2D getBounds2D() {
        return this.shape.getBounds2D();
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return this.shape.intersects(x, y, w, h);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return this.shape.intersects(r);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new DashedIterator(this.shape.getPathIterator(at, 1.0), this.dashArray, this.dashPhase);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        if (flatness < 1.0) {
            flatness = 1.0;
        }
        return new DashedIterator(this.shape.getPathIterator(at, flatness), this.dashArray, this.dashPhase);
    }

    public static void main(String[] args) throws Exception {
        BufferedImage image = new BufferedImage(800, 600, 5);
        Graphics2D graphics = image.createGraphics();
        DashedShape stroked = new DashedShape(new Rectangle2D.Double(0.0, 0.0, 4.0, 4.0), new float[]{2.0f, 2.0f}, 0.0f);
        graphics.draw(stroked);
        graphics.dispose();
        ImageIO.write((RenderedImage)image, "png", new File("/tmp/image.png"));
        PathIterator pi = stroked.getPathIterator(new AffineTransform());
        float[] point = new float[2];
        while (!pi.isDone()) {
            int type = pi.currentSegment(point);
            System.out.println(String.valueOf(type) + " - " + Arrays.toString(point));
            pi.next();
        }
    }

    public class DashedIterator
    implements PathIterator {
        static final float EPS = 0.001f;
        PathIterator delegate;
        float[] dashOffsets;
        int dashIndex = 0;
        float dashOffset;
        float[] prevCoords = new float[2];
        float[] currCoords = new float[2];
        float segmentLength;
        float segmentOffset;
        int lastType;
        float[] dashedSegment = new float[2];
        int dashedType;
        boolean done;
        private float dy;
        private float dx;
        float dashPhase;
        int baseDashIndex;

        public DashedIterator(PathIterator delegate, float[] dashArray, float dashPhase) {
            this.delegate = delegate;
            this.dashOffsets = new float[dashArray.length];
            this.dashOffsets[0] = dashArray[0];
            int i = 1;
            while (i < dashArray.length) {
                this.dashOffsets[i] = this.dashOffsets[i - 1] + dashArray[i];
                ++i;
            }
            dashPhase %= this.dashOffsets[this.dashOffsets.length - 1];
            i = 0;
            while (i < dashArray.length && dashPhase > dashArray[i]) {
                ++this.dashIndex;
                ++i;
            }
            this.baseDashIndex = this.dashIndex;
            this.dashPhase = dashPhase;
            this.dashOffset = dashPhase;
            if (delegate.isDone()) {
                this.done = true;
            } else {
                this.dashedType = delegate.currentSegment(this.dashedSegment);
                this.currCoords[0] = this.dashedSegment[0];
                this.currCoords[1] = this.dashedSegment[1];
                delegate.next();
            }
        }

        @Override
        public int currentSegment(float[] coords) {
            coords[0] = this.dashedSegment[0];
            coords[1] = this.dashedSegment[1];
            return this.dashedType;
        }

        @Override
        public int currentSegment(double[] coords) {
            float[] fcoord = new float[2];
            int retval = this.currentSegment(fcoord);
            coords[0] = fcoord[0];
            coords[1] = fcoord[1];
            return retval;
        }

        @Override
        public int getWindingRule() {
            return this.delegate.getWindingRule();
        }

        @Override
        public boolean isDone() {
            return this.done;
        }

        @Override
        public void next() {
            if (this.segmentLength == 0.0f) {
                if (!this.delegate.isDone()) {
                    this.prevCoords[0] = this.currCoords[0];
                    this.prevCoords[1] = this.currCoords[1];
                    this.lastType = this.delegate.currentSegment(this.currCoords);
                    if (this.lastType == 0) {
                        this.segmentOffset = 0.0f;
                        this.dashOffset = this.dashPhase;
                        this.dashIndex = this.baseDashIndex;
                        this.dashedSegment[0] = this.currCoords[0];
                        this.dashedSegment[1] = this.currCoords[1];
                        this.dashedType = 0;
                        this.dx = 0.0f;
                        this.dy = 0.0f;
                        this.delegate.next();
                        this.done = this.delegate.isDone();
                    } else {
                        this.dx = this.currCoords[0] - this.prevCoords[0];
                        this.dy = this.currCoords[1] - this.prevCoords[1];
                        this.segmentLength = (float)Math.sqrt(Math.pow(this.dx, 2.0) + Math.pow(this.dy, 2.0));
                        this.segmentOffset = 0.0f;
                        this.delegate.next();
                    }
                } else {
                    this.done = true;
                }
            }
            if (!this.done && this.lastType != 0) {
                float dashResidual = this.dashOffsets[this.dashIndex] - this.dashOffset;
                float segmentResidual = this.segmentLength - this.segmentOffset;
                float residual = Math.min(dashResidual, segmentResidual);
                if (Math.abs(this.segmentLength) > 0.001f) {
                    this.dashedSegment[0] = this.dashedSegment[0] + this.dx * residual / this.segmentLength;
                    this.dashedSegment[1] = this.dashedSegment[1] + this.dy * residual / this.segmentLength;
                }
                this.dashedType = this.lastType == 1 ? (this.dashIndex % 2 == 0 ? 1 : 0) : this.lastType;
                this.dashOffset += residual;
                this.segmentOffset += residual;
                if (Math.abs(this.dashOffsets[this.dashIndex] - this.dashOffset) < 0.001f) {
                    ++this.dashIndex;
                    if (this.dashIndex >= this.dashOffsets.length) {
                        this.dashIndex = 0;
                        this.dashOffset = 0.0f;
                    }
                }
                if (Math.abs(this.segmentOffset - this.segmentLength) < 0.001f) {
                    this.segmentLength = 0.0f;
                    this.segmentOffset = 0.0f;
                }
            }
        }
    }
}

