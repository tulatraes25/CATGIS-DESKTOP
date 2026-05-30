/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.renderer.style;

import com.vividsolutions.jump.feature.Feature;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.saig.core.filter.Expression;
import org.saig.core.renderer.style.DrawnMark;
import org.saig.core.renderer.style.MarkFactory;
import org.saig.core.renderer.style.shape.ExplicitBoundsShape;
import org.saig.jump.lang.I18N;

public class WellKnownMarkFactory
implements MarkFactory {
    private static final Logger LOGGER = Logger.getLogger(WellKnownMarkFactory.class);
    private static Shape cross;
    private static Shape star;
    private static Shape triangle;
    private static Shape arrow;
    private static Shape X;
    static Shape hatch;
    private static Shape square;
    private static Map<String, Shape> markCache;

    static {
        markCache = new HashMap<String, Shape>();
        GeneralPath crossPath = new GeneralPath(0);
        crossPath.moveTo(0.5f, 0.125f);
        crossPath.lineTo(0.125f, 0.125f);
        crossPath.lineTo(0.125f, 0.5f);
        crossPath.lineTo(-0.125f, 0.5f);
        crossPath.lineTo(-0.125f, 0.125f);
        crossPath.lineTo(-0.5f, 0.125f);
        crossPath.lineTo(-0.5f, -0.125f);
        crossPath.lineTo(-0.125f, -0.125f);
        crossPath.lineTo(-0.125f, -0.5f);
        crossPath.lineTo(0.125f, -0.5f);
        crossPath.lineTo(0.125f, -0.125f);
        crossPath.lineTo(0.5f, -0.125f);
        crossPath.lineTo(0.5f, 0.125f);
        cross = new ExplicitBoundsShape(crossPath);
        ((ExplicitBoundsShape)cross).setBounds(new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0));
        AffineTransform at = new AffineTransform();
        at.rotate(0.7853981633974483);
        X = new ExplicitBoundsShape(crossPath.createTransformedShape(at));
        ((ExplicitBoundsShape)X).setBounds(new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0));
        GeneralPath starPath = new GeneralPath(0);
        starPath.moveTo(0.191f, 0.0f);
        starPath.lineTo(0.25f, 0.344f);
        starPath.lineTo(0.0f, 0.588f);
        starPath.lineTo(0.346f, 0.638f);
        starPath.lineTo(0.5f, 0.951f);
        starPath.lineTo(0.654f, 0.638f);
        starPath.lineTo(1.0f, 0.588f);
        starPath.lineTo(0.75f, 0.344f);
        starPath.lineTo(0.89f, 0.0f);
        starPath.lineTo(0.5f, 0.162f);
        starPath.lineTo(0.191f, 0.0f);
        at = new AffineTransform();
        at.translate(-0.5, -0.5);
        starPath.transform(at);
        star = new ExplicitBoundsShape(starPath);
        ((ExplicitBoundsShape)star).setBounds(new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0));
        GeneralPath trianglePath = new GeneralPath(0);
        trianglePath.moveTo(0.0f, 1.0f);
        trianglePath.lineTo(0.866f, -0.5f);
        trianglePath.lineTo(-0.866f, -0.5f);
        trianglePath.lineTo(0.0f, 1.0f);
        at = new AffineTransform();
        at.translate(0.0, -0.25);
        at.scale(0.5, 0.5);
        trianglePath.transform(at);
        triangle = new ExplicitBoundsShape(trianglePath);
        ((ExplicitBoundsShape)triangle).setBounds(new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0));
        GeneralPath arrowPath = new GeneralPath(0);
        arrowPath.moveTo(0.0f, -0.5f);
        arrowPath.lineTo(0.5f, 0.0f);
        arrowPath.lineTo(0.0f, 0.5f);
        arrowPath.lineTo(0.0f, 0.1f);
        arrowPath.lineTo(-0.5f, 0.1f);
        arrowPath.lineTo(-0.5f, -0.1f);
        arrowPath.lineTo(0.0f, -0.1f);
        arrowPath.lineTo(0.0f, -0.5f);
        arrow = new ExplicitBoundsShape(arrowPath);
        ((ExplicitBoundsShape)arrow).setBounds(new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0));
        GeneralPath hatchPath = new GeneralPath(0);
        hatchPath.moveTo(0.55f, 0.57f);
        hatchPath.lineTo(0.52f, 0.57f);
        hatchPath.lineTo(-0.57f, -0.52f);
        hatchPath.lineTo(-0.57f, -0.57f);
        hatchPath.lineTo(-0.52f, -0.57f);
        hatchPath.lineTo(0.57f, 0.52f);
        hatchPath.lineTo(0.57f, 0.57f);
        hatchPath.moveTo(0.57f, -0.49f);
        hatchPath.lineTo(0.49f, -0.57f);
        hatchPath.lineTo(0.57f, -0.57f);
        hatchPath.lineTo(0.57f, -0.49f);
        hatchPath.moveTo(-0.57f, 0.5f);
        hatchPath.lineTo(-0.5f, 0.57f);
        hatchPath.lineTo(-0.57f, 0.57f);
        hatchPath.lineTo(-0.57f, 0.5f);
        hatch = new ExplicitBoundsShape(hatchPath);
        ((ExplicitBoundsShape)hatch).setBounds(new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0));
        square = new Rectangle2D.Double(-0.5, -0.5, 1.0, 1.0);
    }

    @Override
    public Shape getShape(Graphics2D graphics, Expression symbolUrl, Feature feature) throws Exception {
        if (symbolUrl == null) {
            return null;
        }
        String wellKnownName = symbolUrl.getValue(feature).toString();
        if (wellKnownName.equalsIgnoreCase("square")) {
            return square;
        }
        if (wellKnownName.equalsIgnoreCase("cross")) {
            return cross;
        }
        if (wellKnownName.equalsIgnoreCase("circle")) {
            return new Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0);
        }
        if (wellKnownName.equalsIgnoreCase("triangle")) {
            return triangle;
        }
        if (wellKnownName.equalsIgnoreCase("x")) {
            return X;
        }
        if (wellKnownName.equalsIgnoreCase("star")) {
            return star;
        }
        if (wellKnownName.equalsIgnoreCase("arrow")) {
            return arrow;
        }
        if (wellKnownName.equalsIgnoreCase("hatch")) {
            return hatch;
        }
        Shape sh = markCache.get(wellKnownName);
        if (sh != null) {
            return sh;
        }
        try {
            DrawnMark dm = new DrawnMark(wellKnownName);
            GeneralPath path = new GeneralPath(0);
            dm.paint(path);
            markCache.put(wellKnownName, path);
            return path;
        }
        catch (Exception e) {
            LOGGER.info((Object)I18N.getMessage("org.saig.core.renderer.style.Java2DMark.symbol-{0}-not-found-in-library", new String[]{wellKnownName}));
            LOGGER.debug((Object)"Returning square");
            return square;
        }
    }

    public static boolean existsMark(String markName) {
        boolean exists = false;
        if (markCache.containsKey(markName)) {
            exists = true;
        } else {
            try {
                new DrawnMark(markName);
                exists = true;
            }
            catch (Exception e) {
                exists = false;
            }
        }
        return exists;
    }
}

