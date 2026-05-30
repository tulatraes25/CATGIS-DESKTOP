/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

import com.vividsolutions.jump.feature.Feature;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Paint;
import org.saig.core.renderer.style.PolygonStyle2D;
import org.saig.core.renderer.style.SLDStyleFactory;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.PolygonSymbolizer;

public class DynamicPolygonStyle2D
extends PolygonStyle2D {
    Feature feature;
    PolygonSymbolizer ps;

    public DynamicPolygonStyle2D(Feature f, PolygonSymbolizer sym) {
        this.feature = f;
        this.ps = sym;
    }

    @Override
    public Paint getFill() {
        Fill fill = this.ps.getFill();
        if (fill == null) {
            return null;
        }
        Paint fillPaint = Color.decode((String)fill.getColor().getValue(this.feature));
        Graphic gr = fill.getGraphicFill();
        if (gr != null) {
            SLDStyleFactory fac = new SLDStyleFactory();
            fillPaint = fac.getTexturePaint(gr, this.feature);
        }
        return fillPaint;
    }

    @Override
    public Composite getFillComposite() {
        Fill fill = this.ps.getFill();
        if (fill == null) {
            return null;
        }
        float opacity = ((Number)fill.getOpacity().getValue(this.feature)).floatValue();
        if (opacity == 1.0f) {
            return null;
        }
        return AlphaComposite.getInstance(3, opacity);
    }
}

