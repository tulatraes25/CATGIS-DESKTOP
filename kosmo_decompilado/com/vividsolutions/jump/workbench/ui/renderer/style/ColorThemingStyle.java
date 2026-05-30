/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import org.saig.core.util.I18NUnsupportedOperationException;

public class ColorThemingStyle
implements Style {
    private BasicStyle defaultStyle;
    private Layer layer;
    private Map<Object, BasicStyle> attributeValueToBasicStyleMap = new HashMap<Object, BasicStyle>();
    private String attributeName;
    private boolean enabled = false;

    public ColorThemingStyle() {
    }

    public void setAlpha(int alpha) {
        this.defaultStyle.setAlpha(alpha);
        for (BasicStyle style : this.attributeValueToBasicStyleMap.values()) {
            style.setAlpha(alpha);
        }
    }

    public void setLineWidth(int lineWidth) {
        this.defaultStyle.setLineWidth(lineWidth);
        for (BasicStyle style : this.attributeValueToBasicStyleMap.values()) {
            style.setLineWidth(lineWidth);
        }
    }

    public ColorThemingStyle(String attributeName, Map<Object, BasicStyle> attributeValueToBasicStyleMap, BasicStyle defaultStyle) {
        this.setAttributeName(attributeName);
        this.setAttributeValueToBasicStyleMap(attributeValueToBasicStyleMap);
        this.setDefaultStyle(defaultStyle);
    }

    @Override
    public void paint(Feature f, Graphics2D g, Viewport viewport) throws Exception {
        this.getStyle(f).paint(f, g, viewport);
    }

    public BasicStyle getStyle(Feature feature) {
        BasicStyle style = this.attributeName != null && feature.getSchema().hasAttribute(this.attributeName) && feature.getAttribute(this.attributeName) != null ? this.attributeValueToBasicStyleMap.get(feature.getAttribute(this.attributeName)) : this.defaultStyle;
        return style == null ? this.defaultStyle : style;
    }

    @Override
    public Object clone() {
        try {
            ColorThemingStyle clone = (ColorThemingStyle)super.clone();
            Map mapClone = (Map)this.attributeValueToBasicStyleMap.getClass().newInstance();
            for (Object attribute : this.attributeValueToBasicStyleMap.keySet()) {
                mapClone.put(attribute, this.attributeValueToBasicStyleMap.get(attribute).clone());
            }
            clone.attributeValueToBasicStyleMap = mapClone;
            return clone;
        }
        catch (InstantiationException e) {
            Assert.shouldNeverReachHere();
            return null;
        }
        catch (IllegalAccessException e) {
            Assert.shouldNeverReachHere();
            return null;
        }
        catch (CloneNotSupportedException e) {
            Assert.shouldNeverReachHere();
            return null;
        }
    }

    public String getAttributeName() {
        return this.attributeName;
    }

    public void setAttributeValueToBasicStyleMap(Map<Object, BasicStyle> attributeValueToStyleMap) {
        this.attributeValueToBasicStyleMap = attributeValueToStyleMap;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public Map<Object, BasicStyle> getAttributeValueToBasicStyleMap() {
        return this.attributeValueToBasicStyleMap;
    }

    @Override
    public String getName() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Icon getIcon() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void initialize(Layer layer) {
        this.layer = layer;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public static ColorThemingStyle get(Layer layer) {
        if ((ColorThemingStyle)layer.getStyle(ColorThemingStyle.class) == null) {
            ColorThemingStyle colorThemingStyle = new ColorThemingStyle(ColorThemingStyle.pickNonSpatialAttributeName(layer.getFeatureCollectionWrapper().getFeatureSchema()), new HashMap<Object, BasicStyle>(), new BasicStyle(Color.lightGray));
            layer.addStyle(colorThemingStyle);
        }
        return (ColorThemingStyle)layer.getStyle(ColorThemingStyle.class);
    }

    private static String pickNonSpatialAttributeName(FeatureSchema schema) {
        int i = 0;
        while (i < schema.getAttributeCount()) {
            if (schema.getGeometryIndex() != i) {
                return schema.getAttributeName(i);
            }
            ++i;
        }
        return null;
    }

    public BasicStyle getDefaultStyle() {
        return this.defaultStyle;
    }

    public void setDefaultStyle(BasicStyle defaultStyle) {
        this.defaultStyle = defaultStyle;
    }
}

