/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Blackboard;
import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

public abstract class BasicFillPattern
implements Paint,
Cloneable {
    public static final String COLOR_KEY = "COLOR";
    private Paint paint;
    private Blackboard properties;

    public BasicFillPattern() {
    }

    public BasicFillPattern(Blackboard properties) {
        this.setProperties(properties);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        BasicFillPattern other = (BasicFillPattern)obj;
        if (this.getProperties().getProperties().size() != other.getProperties().getProperties().size()) {
            return false;
        }
        for (String key : this.getProperties().getProperties().keySet()) {
            if (other.getProperties().getProperties().get(key) == null) {
                return false;
            }
            if (this.getProperties().getProperties().get(key).equals(other.getProperties().getProperties().get(key))) continue;
            return false;
        }
        return true;
    }

    private Paint getPaint() {
        if (this.paint == null) {
            BufferedImage image = this.createImage(this.properties);
            this.paint = new TexturePaint(image, new Rectangle2D.Double(0.0, 0.0, image.getWidth(), image.getHeight()));
        }
        return this.paint;
    }

    public Blackboard getProperties() {
        return this.properties;
    }

    public BasicFillPattern setProperties(Blackboard properties) {
        this.properties = properties;
        this.paint = null;
        return this;
    }

    public abstract BufferedImage createImage(Blackboard var1);

    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
        return this.getPaint().createContext(cm, deviceBounds, userBounds, xform, hints);
    }

    @Override
    public int getTransparency() {
        return this.getPaint().getTransparency();
    }

    public BasicFillPattern setColor(Color color) {
        this.setProperties(this.getProperties().put(COLOR_KEY, color));
        return this;
    }

    public Object clone() {
        try {
            return ((BasicFillPattern)this.getClass().newInstance()).setProperties((Blackboard)this.properties.clone());
        }
        catch (InstantiationException e) {
            Assert.shouldNeverReachHere();
        }
        catch (IllegalAccessException e) {
            Assert.shouldNeverReachHere();
        }
        return null;
    }
}

