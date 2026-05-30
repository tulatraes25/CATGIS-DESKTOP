/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import org.saig.core.renderer.style.DrawnMarkStep;

public class DrawnMarkStepText
extends DrawnMarkStep {
    public DrawnMarkStepText(int type, Object[] params) {
        super(type, params);
    }

    @Override
    public void paint(GeneralPath g) {
        String rp = (String)this.getParam(0);
        float size = ((Float)this.getParam(1)).floatValue();
        float x = ((Float)this.getParam(2)).floatValue();
        float y = ((Float)this.getParam(3)).floatValue();
        AttributedString as = new AttributedString(rp);
        AttributedCharacterIterator aci = as.getIterator();
        FontRenderContext frc = new FontRenderContext(AffineTransform.getScaleInstance(1.0, 1.0), false, false);
        TextLayout tl = new TextLayout(aci, frc);
        float sw = (float)tl.getBounds().getWidth();
        float sh = (float)tl.getBounds().getHeight();
        AffineTransform aft = AffineTransform.getTranslateInstance(x - sw / 2.0f, y + sh / 2.0f);
        aft.preConcatenate(AffineTransform.getScaleInstance(size, -size));
        Shape text = tl.getOutline(aft);
        g.append(text, false);
    }
}

