/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer3;

import es.kosmo.core.renderer.decorators.IDecoratorPoint2DTransformer;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class DecoratorTransformer
implements IDecoratorPoint2DTransformer {
    private AffineTransform tr;

    public DecoratorTransformer(AffineTransform tr) {
        this.tr = tr;
    }

    @Override
    public Point2D toViewPoint(Point2D modelPoint) throws NoninvertibleTransformException {
        return this.tr.transform(modelPoint, null);
    }
}

