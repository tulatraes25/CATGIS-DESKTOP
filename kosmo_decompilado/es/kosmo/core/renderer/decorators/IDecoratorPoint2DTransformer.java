/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.renderer.decorators;

import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public interface IDecoratorPoint2DTransformer {
    public Point2D toViewPoint(Point2D var1) throws NoninvertibleTransformException;
}

