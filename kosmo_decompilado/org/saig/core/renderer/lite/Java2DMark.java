/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.lite;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;

class Java2DMark {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.rendering");
    private static GeneralPath cross = new GeneralPath(0);
    private static GeneralPath star;
    private static GeneralPath triangle;
    private static GeneralPath arrow;
    private static Shape X;

    static {
        cross.moveTo(0.5f, 0.125f);
        cross.lineTo(0.125f, 0.125f);
        cross.lineTo(0.125f, 0.5f);
        cross.lineTo(-0.125f, 0.5f);
        cross.lineTo(-0.125f, 0.125f);
        cross.lineTo(-0.5f, 0.125f);
        cross.lineTo(-0.5f, -0.125f);
        cross.lineTo(-0.125f, -0.125f);
        cross.lineTo(-0.125f, -0.5f);
        cross.lineTo(0.125f, -0.5f);
        cross.lineTo(0.125f, -0.125f);
        cross.lineTo(0.5f, -0.125f);
        cross.lineTo(0.5f, 0.125f);
        AffineTransform at = new AffineTransform();
        at.rotate(0.7853981633974483);
        X = cross.createTransformedShape(at);
        star = new GeneralPath(0);
        star.moveTo(0.191f, 0.0f);
        star.lineTo(0.25f, 0.344f);
        star.lineTo(0.0f, 0.588f);
        star.lineTo(0.346f, 0.638f);
        star.lineTo(0.5f, 0.951f);
        star.lineTo(0.654f, 0.638f);
        star.lineTo(1.0f, 0.588f);
        star.lineTo(0.75f, 0.344f);
        star.lineTo(0.89f, 0.0f);
        star.lineTo(0.5f, 0.162f);
        star.lineTo(0.191f, 0.0f);
        at = new AffineTransform();
        at.translate(-0.5, -0.5);
        star.transform(at);
        triangle = new GeneralPath(0);
        triangle.moveTo(0.0f, 1.0f);
        triangle.lineTo(0.866f, -0.5f);
        triangle.lineTo(-0.866f, -0.5f);
        triangle.lineTo(0.0f, 1.0f);
        at = new AffineTransform();
        at.translate(0.0, -0.25);
        at.scale(0.5, 0.5);
        triangle.transform(at);
        arrow = new GeneralPath(0);
        arrow.moveTo(0.0f, -0.5f);
        arrow.lineTo(0.5f, 0.0f);
        arrow.lineTo(0.0f, 0.5f);
        arrow.lineTo(0.0f, 0.1f);
        arrow.lineTo(-0.5f, 0.1f);
        arrow.lineTo(-0.5f, -0.1f);
        arrow.lineTo(0.0f, -0.1f);
        arrow.lineTo(0.0f, -0.5f);
    }

    private Java2DMark() {
    }

    public static Shape getWellKnownMark(String wellKnownName) {
        LOGGER.finer("fetching mark of name " + wellKnownName);
        if (wellKnownName.equalsIgnoreCase("cross")) {
            LOGGER.finer("returning cross");
            return cross;
        }
        if (wellKnownName.equalsIgnoreCase("circle")) {
            LOGGER.finer("returning circle");
            return new Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0);
        }
        if (wellKnownName.equalsIgnoreCase("triangle")) {
            LOGGER.finer("returning triangle");
            return triangle;
        }
        if (wellKnownName.equalsIgnoreCase("X")) {
            LOGGER.finer("returning X");
            return X;
        }
        if (wellKnownName.equalsIgnoreCase("star")) {
            LOGGER.finer("returning star");
            return star;
        }
        if (wellKnownName.equalsIgnoreCase("arrow")) {
            LOGGER.finer("returning arrow");
            return arrow;
        }
        LOGGER.finer("returning square");
        return new Rectangle2D.Double(-0.5, -0.5, 1.0, 1.0);
    }
}

