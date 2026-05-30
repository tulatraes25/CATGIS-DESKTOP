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
import java.awt.Graphics2D;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

public interface IG2dRenderer {
    public void render(Graphics2D var1, int var2, int var3, Envelope var4, Layerable var5, double var6, double var8, boolean var10, Unit<Length> var11);

    public void cancel();
}

