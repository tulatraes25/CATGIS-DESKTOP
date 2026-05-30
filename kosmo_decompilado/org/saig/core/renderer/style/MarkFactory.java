/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

import com.vividsolutions.jump.feature.Feature;
import java.awt.Graphics2D;
import java.awt.Shape;
import org.saig.core.filter.Expression;

public interface MarkFactory {
    public Shape getShape(Graphics2D var1, Expression var2, Feature var3) throws Exception;
}

