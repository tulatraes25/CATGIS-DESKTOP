/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.io.ParseException
 *  com.vividsolutions.jts.io.WKTReader
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.geom.Angle;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.warp.AffineTransform;
import com.vividsolutions.jump.workbench.ui.renderer.java2D.Java2DConverter;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicFillPattern;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class WKTFillPattern
extends BasicFillPattern {
    public static final String LINE_WIDTH_KEY = "LINE WIDTH";
    public static final String EXTENT_KEY = "EXTENT";
    public static final String PATTERN_WKT_KEY = "PATTERN WKT";

    public WKTFillPattern() {
    }

    public WKTFillPattern(int lineWidth, int extent, String patternWKT) {
        super(new Blackboard().putAll(CollectionUtil.createMap(new Object[]{"COLOR", Color.black, LINE_WIDTH_KEY, new Integer(lineWidth), EXTENT_KEY, new Integer(extent), PATTERN_WKT_KEY, patternWKT})));
    }

    public String toString() {
        return (String)this.getProperties().get(PATTERN_WKT_KEY);
    }

    @Override
    public BufferedImage createImage(Blackboard properties) {
        BufferedImage image = new BufferedImage(properties.getInt(EXTENT_KEY), properties.getInt(EXTENT_KEY), 2);
        Graphics2D g = image.createGraphics();
        g.setComposite(AlphaComposite.getInstance(3, (float)((Color)this.getProperties().get("COLOR")).getAlpha() / 255.0f));
        g.scale(1.0, -1.0);
        g.translate((double)properties.getInt(EXTENT_KEY) / 2.0, (double)(-properties.getInt(EXTENT_KEY)) / 2.0);
        g.setColor((Color)properties.get("COLOR"));
        g.setStroke(new BasicStroke(properties.getInt(LINE_WIDTH_KEY)));
        try {
            g.draw(new Java2DConverter(new Java2DConverter.PointConverter(){

                @Override
                public Point2D toViewPoint(Coordinate modelCoordinate) {
                    return new Point2D.Double(modelCoordinate.x, modelCoordinate.y);
                }
            }).toShape(new WKTReader().read((String)properties.get(PATTERN_WKT_KEY))));
        }
        catch (NoninvertibleTransformException noninvertibleTransformException) {
        }
        catch (ParseException e) {
            Assert.shouldNeverReachHere((String)((String)properties.get(PATTERN_WKT_KEY)));
        }
        return image;
    }

    public static WKTFillPattern createDiagonalStripePattern(int lineWidth, double centerlineSeparationInLineWidths, boolean forward, boolean back) {
        double centerlineSeparation = centerlineSeparationInLineWidths * (double)lineWidth;
        return new WKTFillPattern(lineWidth, (int)Math.rint(Math.sqrt(2.0) * centerlineSeparation), "GEOMETRYCOLLECTION(" + WKTFillPattern.wktForThreeLines(centerlineSeparation, 45.0, forward) + ", " + WKTFillPattern.wktForThreeLines(centerlineSeparation, -45.0, back) + ")");
    }

    public static WKTFillPattern createVerticalHorizontalStripePattern(int lineWidth, double centerlineSeparationInLineWidths, boolean vertical, boolean horizontal) {
        double centerlineSeparation = centerlineSeparationInLineWidths * (double)lineWidth;
        return new WKTFillPattern(lineWidth, (int)Math.rint(2.0 * centerlineSeparation), "GEOMETRYCOLLECTION(" + WKTFillPattern.wktForThreeLines(centerlineSeparation, 90.0, vertical) + ", " + WKTFillPattern.wktForThreeLines(centerlineSeparation, 0.0, horizontal) + ")");
    }

    private static String wktForThreeLines(double centerlineSeparation, double angleInDegrees, boolean enabled) {
        return enabled ? WKTFillPattern.wktForThreeLines(4.0 * centerlineSeparation, centerlineSeparation, angleInDegrees) : "POINT EMPTY";
    }

    private static String wktForThreeLines(double length, double centerlineSeparation, double angleInDegrees) {
        AffineTransform transform = new AffineTransform(new Coordinate(), new Coordinate(), new Coordinate(1.0, 0.0), new Coordinate(Math.cos(Angle.toRadians(angleInDegrees)), Math.sin(Angle.toRadians(angleInDegrees))));
        try {
            return transform.transform(new WKTReader().read("MULTILINESTRING(" + "(" + -length / 2.0 + " " + -centerlineSeparation + ", " + length / 2.0 + " " + -centerlineSeparation + "), " + "(" + -length / 2.0 + " " + 0 + ", " + length / 2.0 + " " + 0 + "), " + "(" + -length / 2.0 + " " + centerlineSeparation + ", " + length / 2.0 + " " + centerlineSeparation + ") " + ")")).toText();
        }
        catch (Exception e) {
            Assert.shouldNeverReachHere();
            return null;
        }
    }
}

