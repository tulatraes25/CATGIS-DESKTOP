/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import org.saig.core.renderer.style.DrawnMarkStep;

public class DrawnMarkStepEllipse
extends DrawnMarkStep {
    public DrawnMarkStepEllipse(int type, Object[] params) {
        super(type, params);
    }

    @Override
    public void paint(GeneralPath g) {
        float x = ((Float)this.getParam(0)).floatValue();
        float y = ((Float)this.getParam(1)).floatValue();
        float w = ((Float)this.getParam(2)).floatValue();
        float h = ((Float)this.getParam(3)).floatValue();
        g.append(new Ellipse2D.Double(x, y, w, h), true);
    }
}

