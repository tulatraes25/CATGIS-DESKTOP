/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.ICoordTrans
 */
package org.saig.core.renderer.print;

import com.vividsolutions.jump.feature.Feature;
import org.cresques.cts.ICoordTrans;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometry;
import org.saig.core.renderer.lite.LiteShape2;

public class PrintElement {
    private IShapeGeometry pathShape;
    private LiteShape2 shape;
    private Feature feature;
    private ICoordTrans transf;

    public PrintElement(LiteShape2 shape, Feature feature, IShapeGeometry pathShape, ICoordTrans transf) {
        this.pathShape = pathShape;
        this.feature = feature;
        this.shape = shape;
        this.transf = transf;
    }

    public Feature getFeature() {
        return this.feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public LiteShape2 getShape() {
        return this.shape;
    }

    public void setShape(LiteShape2 shape) {
        this.shape = shape;
    }

    public IShapeGeometry getPathShape() {
        return this.pathShape;
    }

    public void setPathShape(ShapeGeometry pathShape) {
        this.pathShape = pathShape;
    }

    public ICoordTrans getTransf() {
        return this.transf;
    }

    public void setTransf(ICoordTrans transf) {
        this.transf = transf;
    }
}

