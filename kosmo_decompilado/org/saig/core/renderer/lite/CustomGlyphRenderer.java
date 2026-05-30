/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.renderer.lite.GlyphPropertiesList;
import org.saig.core.renderer.lite.GlyphRenderer;
import org.saig.core.styling.ExternalGraphic;
import org.saig.core.styling.Graphic;

public class CustomGlyphRenderer
implements GlyphRenderer {
    private static final Logger LOGGER = Logger.getLogger(CustomGlyphRenderer.class);
    private GlyphPropertiesList list = new GlyphPropertiesList();
    private boolean maxFound = false;
    private int maxBarHeight = 0;

    public CustomGlyphRenderer() {
        FilterFactory factory = FilterFactory.createFilterFactory();
        this.list.addProperty("radius", Expression.class, factory.createLiteralExpression(50));
        this.list.addProperty("circle color", Expression.class, factory.createLiteralExpression("#000066"));
        this.list.addProperty("bar height", Expression.class, factory.createLiteralExpression(150));
        this.list.addProperty("bar color", Expression.class, factory.createLiteralExpression("#000000"));
        this.list.addProperty("bar uncertainty", Expression.class, factory.createLiteralExpression(50));
        this.list.addProperty("bar uncertainty width", Expression.class, factory.createLiteralExpression(5));
        this.list.addProperty("bar uncertainty color", Expression.class, factory.createLiteralExpression("#999999"));
        this.list.addProperty("pointer length", Expression.class, factory.createLiteralExpression(100));
        this.list.addProperty("pointer color", Expression.class, factory.createLiteralExpression("#FF0000"));
        this.list.addProperty("pointer direction", Expression.class, factory.createLiteralExpression(21));
        this.list.addProperty("wedge width", Expression.class, factory.createLiteralExpression(25));
        this.list.addProperty("wedge color", Expression.class, factory.createLiteralExpression("#9999FF"));
    }

    @Override
    public boolean canRender(String format) {
        return format.equalsIgnoreCase("image/hack");
    }

    @Override
    public List<String> getFormats() {
        Vector<String> ret = new Vector<String>();
        ret.add("image/hack");
        return ret;
    }

    public String getGlyphName() {
        return "exploded clock";
    }

    public GlyphPropertiesList getGlyphProperties() {
        return this.list;
    }

    public void setGlyphProperties(GlyphPropertiesList gpl) {
        this.list = gpl;
    }

    @Override
    public BufferedImage render(Graphic graphic, ExternalGraphic eg, Feature feature) {
        Color wedgeColor;
        int wedgeWidth;
        int pointerLength;
        Color pointerColor;
        int pointerDirection;
        Color barUncColor;
        int barUncWidth;
        int barUncertainty;
        Color barColor;
        int barHeight;
        Color circleColor;
        int radius;
        block26: {
            Map<String, Object> props = eg.getCustomProperties();
            Set<String> propNames = props.keySet();
            for (String nextName : propNames) {
                if (this.list.hasProperty(nextName)) {
                    this.list.setPropertyValue(nextName, props.get(nextName));
                    continue;
                }
                System.out.println("Tried to set the property " + nextName + " to a glyph that does not have this property.");
            }
            radius = 50;
            Expression e = (Expression)this.list.getPropertyValue("radius");
            if (e != null) {
                radius = ((Number)e.getValue(feature)).intValue();
            }
            circleColor = Color.BLUE.darker();
            e = (Expression)this.list.getPropertyValue("circle color");
            if (e != null) {
                circleColor = Color.decode((String)e.getValue(feature));
            }
            barHeight = 150;
            e = (Expression)this.list.getPropertyValue("bar height");
            if (e != null) {
                barHeight = ((Number)e.getValue(feature)).intValue();
            }
            barColor = Color.BLACK;
            e = (Expression)this.list.getPropertyValue("bar color");
            if (e != null) {
                barColor = Color.decode((String)e.getValue(feature));
            }
            barUncertainty = 50;
            e = (Expression)this.list.getPropertyValue("bar uncertainty");
            if (e != null) {
                barUncertainty = ((Number)e.getValue(feature)).intValue();
            }
            barUncWidth = 5;
            e = (Expression)this.list.getPropertyValue("bar uncertainty width");
            if (e != null) {
                barUncWidth = ((Number)e.getValue(feature)).intValue();
            }
            barUncColor = Color.GRAY;
            e = (Expression)this.list.getPropertyValue("bar uncertainty color");
            if (e != null) {
                barUncColor = Color.decode((String)e.getValue(feature));
            }
            pointerDirection = 21;
            e = (Expression)this.list.getPropertyValue("pointer direction");
            if (e != null) {
                pointerDirection = ((Number)e.getValue(feature)).intValue();
            }
            pointerColor = Color.RED;
            e = (Expression)this.list.getPropertyValue("pointer color");
            if (e != null) {
                pointerColor = Color.decode((String)e.getValue(feature));
            }
            pointerLength = 100;
            e = (Expression)this.list.getPropertyValue("pointer length");
            if (e != null) {
                pointerLength = ((Number)e.getValue(feature)).intValue();
            }
            wedgeWidth = 25;
            e = (Expression)this.list.getPropertyValue("wedge width");
            if (e != null) {
                wedgeWidth = ((Number)e.getValue(feature)).intValue();
            }
            wedgeColor = Color.BLUE;
            e = (Expression)this.list.getPropertyValue("wedge color");
            if (e != null) {
                wedgeColor = Color.decode((String)e.getValue(feature));
            }
            if (!this.maxFound) {
                this.maxFound = true;
                FeatureCollection fc = feature.getParent();
                FeatureIterator features = null;
                try {
                    try {
                        features = fc.iterator();
                        while (features.hasNext()) {
                            Feature next = features.next();
                            Expression tempExp = (Expression)this.list.getPropertyValue("bar height");
                            int temp1 = 0;
                            if (tempExp != null) {
                                temp1 = ((Number)tempExp.getValue(next)).intValue();
                            }
                            tempExp = (Expression)this.list.getPropertyValue("bar uncertainty");
                            int temp2 = 0;
                            if (tempExp != null) {
                                temp2 = ((Number)tempExp.getValue(next)).intValue();
                            }
                            if (temp1 + temp2 <= this.maxBarHeight) continue;
                            this.maxBarHeight = temp1 + temp2;
                        }
                    }
                    catch (Exception e1) {
                        LOGGER.error((Object)"", (Throwable)e1);
                        if (features != null) {
                            features.close();
                        }
                        break block26;
                    }
                }
                catch (Throwable throwable) {
                    if (features != null) {
                        features.close();
                    }
                    throw throwable;
                }
                if (features != null) {
                    features.close();
                }
            }
        }
        int circleCenterX = Math.max(pointerLength, radius);
        int circleCenterY = Math.max(this.maxBarHeight, Math.max(pointerLength, radius));
        int imageHeight = Math.max(radius * 2, Math.max(radius + pointerLength, Math.max(radius + this.maxBarHeight, pointerLength + this.maxBarHeight)));
        int imageWidth = Math.max(radius * 2, pointerLength * 2);
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, 2);
        pointerLength = Math.max(pointerLength, radius);
        Graphics2D imageGraphic = image.createGraphics();
        imageGraphic.setColor(circleColor);
        imageGraphic.fillOval(circleCenterX - radius, circleCenterY - radius, radius * 2, radius * 2);
        imageGraphic.setColor(wedgeColor);
        imageGraphic.fillArc(circleCenterX - radius, circleCenterY - radius, radius * 2, radius * 2, this.calculateWedgeAngle(pointerDirection, wedgeWidth), wedgeWidth * 2);
        imageGraphic.setColor(barUncColor);
        imageGraphic.fillRect(circleCenterX - barUncWidth, circleCenterY - barHeight - barUncertainty, barUncWidth * 2, barUncertainty * 2);
        int[] endPoint = this.calculateEndOfPointer(circleCenterX, circleCenterY, pointerLength, pointerDirection);
        imageGraphic.setStroke(new BasicStroke(3.0f));
        imageGraphic.setColor(pointerColor);
        imageGraphic.draw(new Line2D.Double(circleCenterX, circleCenterY, endPoint[0], endPoint[1]));
        imageGraphic.setStroke(new BasicStroke(3.0f));
        imageGraphic.setColor(barColor);
        imageGraphic.draw(new Line2D.Double(circleCenterX, circleCenterY, circleCenterX, circleCenterY - barHeight));
        imageGraphic.dispose();
        return image;
    }

    private int calculateWedgeAngle(int pointerDirection, int wedgeWidth) {
        return 450 - (pointerDirection + wedgeWidth);
    }

    private int[] calculateEndOfPointer(int circleCenterX, int circleCenterY, int pointerLength, int pointerDirection) {
        int x = circleCenterX + (int)Math.round((double)pointerLength * Math.cos(Math.toRadians(pointerDirection - 90)));
        int y = circleCenterY + (int)Math.round((double)pointerLength * Math.sin(Math.toRadians(pointerDirection - 90)));
        return new int[]{x, y};
    }
}

