/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.media.jai.util.Range
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jump.feature.Feature;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.media.jai.util.Range;
import org.saig.core.renderer.lite.LiteShape2;
import org.saig.core.styling.TextSymbolizer;

public interface LabelCache {
    public void start();

    public void startLayer(String var1);

    public void put(String var1, TextSymbolizer var2, Feature var3, LiteShape2 var4, LiteShape2 var5, Range var6, Number var7, Color var8, Number var9);

    public void put(String var1, TextSymbolizer var2, Feature var3, LiteShape2 var4, LiteShape2 var5, Range var6, Number var7, Color var8, Number var9, double var10);

    public void endLayer(String var1, Graphics2D var2, Rectangle var3);

    public void end(Graphics2D var1, Rectangle var2);

    public void print(Graphics2D var1, Rectangle var2);

    public void stop();
}

