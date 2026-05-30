/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer2;

import com.vividsolutions.jump.workbench.ui.Viewport;
import es.kosmo.core.renderer.decorators.IDecoratorPoint2DTransformer;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class ViewportDecoratorTransformerWrapper
implements IDecoratorPoint2DTransformer {
    private Viewport viewport;

    public ViewportDecoratorTransformerWrapper(Viewport viewport) {
        this.viewport = viewport;
    }

    @Override
    public Point2D toViewPoint(Point2D modelPoint) throws NoninvertibleTransformException {
        return this.viewport.toViewPoint(modelPoint);
    }
}

