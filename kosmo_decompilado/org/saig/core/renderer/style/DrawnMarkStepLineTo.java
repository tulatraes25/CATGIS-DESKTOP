/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

import java.awt.geom.GeneralPath;
import org.saig.core.renderer.style.DrawnMarkStep;

public class DrawnMarkStepLineTo
extends DrawnMarkStep {
    public DrawnMarkStepLineTo(int type, Object[] params) {
        super(type, params);
    }

    @Override
    public void paint(GeneralPath g) {
        float x = ((Float)this.getParam(0)).floatValue();
        float y = ((Float)this.getParam(1)).floatValue();
        g.lineTo(x, y);
    }
}

