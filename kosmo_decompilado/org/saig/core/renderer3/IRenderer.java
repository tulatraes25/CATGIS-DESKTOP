/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package org.saig.core.renderer3;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.model.Layerable;
import java.awt.Image;
import java.util.Map;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

public interface IRenderer {
    public void render(Image var1, Envelope var2, Layerable var3, double var4, double var6, boolean var8, Unit<Length> var9, Map<Object, Object> var10);

    public void cancel();
}

