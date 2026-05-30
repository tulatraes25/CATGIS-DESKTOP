/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.log4j.Logger
 *  org.geotools.referencing.operation.DefaultMathTransformFactory
 *  org.opengis.referencing.operation.MathTransform
 *  org.opengis.referencing.operation.Matrix
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import es.kosmo.core.renderer.style.IconStyle2D;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.matrix.AffineTransform2D;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;
import org.saig.core.renderer.lite.DashedShape;
import org.saig.core.renderer.lite.Decimator;
import org.saig.core.renderer.lite.GeomCollectionIterator;
import org.saig.core.renderer.lite.LiteShape2;
import org.saig.core.renderer.style.GraphicStyle2D;
import org.saig.core.renderer.style.LineStyle2D;
import org.saig.core.renderer.style.MarkStyle2D;
import org.saig.core.renderer.style.PolygonStyle2D;
import org.saig.core.renderer.style.Style2D;

public class StyledShapePainter {
    public static final RenderingHints.Key TEXTURE_ANCHOR_HINT_KEY = new TextureAnchorKey();
    private static AffineTransform IDENTITY_TRANSFORM = new AffineTransform();
    private static final Logger LOGGER = Logger.getLogger((String)StyledShapePainter.class.getName());

    public void paint(Graphics2D graphics, LiteShape2 shape, Style2D style, double scale) {
        if (style == null) {
            LOGGER.warn((Object)"ShapePainter has been asked to paint a null style!!");
            return;
        }
        if (style instanceof IconStyle2D) {
            AffineTransform temp = graphics.getTransform();
            try {
                IconStyle2D icoStyle = (IconStyle2D)style;
                Icon icon = icoStyle.getIcon();
                graphics.setComposite(icoStyle.getComposite());
                float dx = -((float)((double)icon.getIconWidth() / 2.0 + (double)icoStyle.getDisplacementX()));
                float dy = -((float)((double)icon.getIconHeight() / 2.0 + (double)icoStyle.getDisplacementY()));
                float[] coords = new float[2];
                PathIterator citer = this.getPathIterator(shape);
                AffineTransform markAT = new AffineTransform(temp);
                while (!citer.isDone()) {
                    citer.currentSegment(coords);
                    markAT.setTransform(temp);
                    double x = coords[0] + dx;
                    double y = coords[1] + dy;
                    markAT.translate(x, y);
                    markAT.rotate(icoStyle.getRotation());
                    graphics.setTransform(markAT);
                    icon.paintIcon(null, graphics, 0, 0);
                    citer.next();
                }
            }
            finally {
                graphics.setTransform(temp);
            }
        }
        if (style instanceof MarkStyle2D) {
            PathIterator citer = this.getPathIterator(shape);
            float[] coords = new float[2];
            MarkStyle2D ms2d = (MarkStyle2D)style;
            while (!citer.isDone()) {
                Paint paint;
                citer.currentSegment(coords);
                Shape transformedShape = ms2d.getTransformedShape(coords[0], coords[1]);
                if (transformedShape == null) continue;
                if (ms2d.getFill() != null) {
                    paint = this.getPaint(ms2d.getFill(), graphics.getTransform(), transformedShape.getBounds2D());
                    graphics.setPaint(paint);
                    graphics.setComposite(ms2d.getFillComposite());
                    graphics.fill(transformedShape);
                }
                if (ms2d.getContour() != null) {
                    paint = this.getPaint(ms2d.getContour(), graphics.getTransform(), transformedShape.getBounds2D());
                    graphics.setPaint(paint);
                    graphics.setStroke(ms2d.getStroke());
                    graphics.setComposite(ms2d.getContourComposite());
                    graphics.draw(transformedShape);
                }
                citer.next();
            }
        } else if (style instanceof GraphicStyle2D) {
            float[] coords = new float[2];
            PathIterator iter = this.getPathIterator(shape);
            iter.currentSegment(coords);
            GraphicStyle2D gs2d = (GraphicStyle2D)style;
            while (!iter.isDone()) {
                iter.currentSegment(coords);
                this.renderImage(graphics, coords[0], coords[1], gs2d.getImage(), gs2d.getRotation(), gs2d.getOpacity());
                iter.next();
            }
        } else {
            LineStyle2D ls2d;
            Paint paint;
            if (style instanceof PolygonStyle2D) {
                PolygonStyle2D ps2d = (PolygonStyle2D)style;
                if (ps2d.getFill() != null) {
                    paint = this.getPaint(ps2d.getFill(), graphics.getTransform(), shape.getBounds2D());
                    graphics.setPaint(paint);
                    graphics.setComposite(ps2d.getFillComposite());
                    this.fillLiteShape(graphics, shape);
                }
                if (ps2d.getGraphicFill() != null) {
                    Shape oldClip = graphics.getClip();
                    try {
                        this.paintGraphicFill(graphics, shape, ps2d.getGraphicFill(), scale);
                    }
                    finally {
                        graphics.setClip(oldClip);
                    }
                }
            }
            if (style instanceof LineStyle2D && (ls2d = (LineStyle2D)style).getStroke() != null) {
                if (ls2d.getGraphicStroke() != null) {
                    this.drawWithGraphicsStroke(graphics, this.dashShape(shape, ls2d.getStroke()), ls2d.getGraphicStroke());
                } else {
                    paint = this.getPaint(ls2d.getContour(), graphics.getTransform(), shape.getBounds2D());
                    Stroke stroke = ls2d.getStroke();
                    if (graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING) == RenderingHints.VALUE_ANTIALIAS_ON && stroke instanceof BasicStroke) {
                        BasicStroke bs = (BasicStroke)stroke;
                        stroke = new BasicStroke(bs.getLineWidth() + 0.5f, bs.getEndCap(), bs.getLineJoin(), bs.getMiterLimit(), bs.getDashArray(), bs.getDashPhase());
                    }
                    graphics.setPaint(paint);
                    graphics.setStroke(stroke);
                    graphics.setComposite(ls2d.getContourComposite());
                    graphics.draw(shape);
                }
            }
        }
    }

    private PathIterator getPathIterator(LiteShape2 shape) {
        GeometryCollection gc;
        if (shape.getGeometry() instanceof GeometryCollection) {
            gc = (GeometryCollection)shape.getGeometry();
        } else {
            Geometry[] gs = new Geometry[]{shape.getGeometry()};
            gc = shape.getGeometry().getFactory().createGeometryCollection(gs);
        }
        GeomCollectionIterator citer = new GeomCollectionIterator(gc, IDENTITY_TRANSFORM, false, 1.0);
        return citer;
    }

    public void debugShape(Shape shape) {
        float[] pt = new float[2];
        PathIterator iter = shape.getPathIterator(null);
        while (!iter.isDone()) {
            int type = iter.currentSegment(pt);
            String event = "unknown";
            if (type == 4) {
                event = "SEG_CLOSE";
            }
            if (type == 3) {
                event = "SEG_CUBIC";
            }
            if (type == 1) {
                event = "SEG_LINETO";
            }
            if (type == 0) {
                event = "SEG_MOVETO";
            }
            if (type == 2) {
                event = "SEG_QUADTO";
            }
            System.out.println(String.valueOf(event) + " " + pt[0] + "," + pt[1]);
            iter.next();
        }
    }

    private void drawWithGraphicsStroke(Graphics2D graphics, Shape shape, Style2D graphicStroke) {
        double imageSize;
        PathIterator pi = shape.getPathIterator(null);
        double[] coords = new double[4];
        if (graphicStroke instanceof MarkStyle2D) {
            imageSize = ((MarkStyle2D)graphicStroke).getSize();
        } else if (graphicStroke instanceof IconStyle2D) {
            imageSize = ((IconStyle2D)graphicStroke).getIcon().getIconWidth();
        } else {
            GraphicStyle2D gs = (GraphicStyle2D)graphicStroke;
            imageSize = gs.getImage().getWidth() - gs.getBorder();
        }
        double[] first = new double[2];
        double[] previous = new double[2];
        int type = pi.currentSegment(coords);
        first[0] = coords[0];
        first[1] = coords[1];
        previous[0] = coords[0];
        previous[1] = coords[1];
        pi.next();
        double remainder = imageSize / 2.0;
        while (!pi.isDone()) {
            type = pi.currentSegment(coords);
            switch (type) {
                case 0: {
                    first[0] = coords[0];
                    first[1] = coords[1];
                    remainder = imageSize / 2.0;
                    break;
                }
                case 4: {
                    coords[0] = first[0];
                    coords[1] = first[1];
                    remainder = imageSize / 2.0;
                }
                case 1: {
                    double dx = coords[0] - previous[0];
                    double dy = coords[1] - previous[1];
                    double len = Math.sqrt(dx * dx + dy * dy);
                    if (len < remainder) {
                        remainder -= len;
                        break;
                    }
                    double theta = Math.atan2(dx, dy);
                    dx = Math.sin(theta) * imageSize;
                    dy = Math.cos(theta) * imageSize;
                    double rotation = -(theta - 1.5707963267948966);
                    double x = previous[0] + Math.sin(theta) * remainder;
                    double y = previous[1] + Math.cos(theta) * remainder;
                    double dist = 0.0;
                    dist = remainder;
                    while (dist < len) {
                        this.renderGraphicsStroke(graphics, x, y, graphicStroke, rotation, 1.0f);
                        x += dx;
                        y += dy;
                        dist += imageSize;
                    }
                    remainder = dist - len;
                    break;
                }
                default: {
                    LOGGER.warn((Object)"default branch reached in drawWithGraphicStroke");
                }
            }
            previous[0] = coords[0];
            previous[1] = coords[1];
            pi.next();
        }
    }

    private void renderGraphicsStroke(Graphics2D graphics, double x, double y, Style2D style, double rotation, float opacity) {
        graphics.setComposite(AlphaComposite.getInstance(3, opacity));
        if (style instanceof GraphicStyle2D) {
            BufferedImage image = ((GraphicStyle2D)style).getImage();
            this.renderImage(graphics, x, y, image, rotation, opacity);
        } else if (style instanceof MarkStyle2D) {
            MarkStyle2D ms2d = (MarkStyle2D)style;
            Shape transformedShape = ms2d.getTransformedShape((float)x, (float)y, (float)rotation);
            if (transformedShape != null) {
                if (ms2d.getFill() != null) {
                    graphics.setPaint(ms2d.getFill());
                    graphics.fill(transformedShape);
                }
                if (ms2d.getContour() != null) {
                    graphics.setPaint(ms2d.getContour());
                    graphics.setStroke(ms2d.getStroke());
                    graphics.draw(transformedShape);
                }
            }
        } else if (style instanceof IconStyle2D) {
            IconStyle2D icons = (IconStyle2D)style;
            Icon icon = icons.getIcon();
            AffineTransform markAT = new AffineTransform(graphics.getTransform());
            markAT.translate(x, y);
            markAT.rotate(rotation);
            double dx = (double)(-icon.getIconWidth()) / 2.0;
            double dy = (double)(-icon.getIconHeight()) / 2.0;
            markAT.translate(dx, dy);
            AffineTransform temp = graphics.getTransform();
            try {
                graphics.setTransform(markAT);
                icon.paintIcon(null, graphics, 0, 0);
            }
            finally {
                graphics.setTransform(temp);
            }
        }
    }

    private void renderImage(Graphics2D graphics, double x, double y, BufferedImage image, double rotation, float opacity) {
        AffineTransform markAT = new AffineTransform();
        markAT.translate(x, y);
        markAT.rotate(rotation);
        markAT.translate((double)(-image.getWidth()) / 2.0, (double)(-image.getHeight()) / 2.0);
        graphics.setComposite(AlphaComposite.getInstance(3, opacity));
        Object interpolation = graphics.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        if (interpolation == null) {
            interpolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        }
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawRenderedImage(image, markAT);
        }
        finally {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
        }
    }

    Shape dashShape(Shape shape, Stroke stroke) {
        if (!(stroke instanceof BasicStroke)) {
            return shape;
        }
        BasicStroke bs = (BasicStroke)stroke;
        if (bs.getDashArray() == null || bs.getDashArray().length == 0) {
            return shape;
        }
        return new DashedShape(shape, bs.getDashArray(), bs.getDashPhase());
    }

    void fillLiteShape(Graphics2D g, LiteShape2 shape) {
        if (shape.getGeometry() instanceof MultiPolygon && shape.getGeometry().getNumGeometries() > 1) {
            MultiPolygon mp = (MultiPolygon)shape.getGeometry();
            int i = 0;
            while (i < mp.getNumGeometries()) {
                Polygon p = (Polygon)mp.getGeometryN(i);
                try {
                    g.fill(new LiteShape2((Geometry)p, null, null, false, false));
                }
                catch (Exception e) {
                    throw new RuntimeException("Unexpected error occurred while rendering a multipolygon", e);
                }
                ++i;
            }
        } else {
            g.fill(shape);
        }
    }

    private void paintGraphicFill(Graphics2D graphics, Shape shape, Style2D graphicFill, double scale) {
        Rectangle2D boundsShape = shape.getBounds2D();
        Rectangle2D.Double stippleSize = null;
        if (graphicFill instanceof MarkStyle2D) {
            Rectangle2D boundsFill = ((MarkStyle2D)graphicFill).getShape().getBounds2D();
            double size = ((MarkStyle2D)graphicFill).getSize();
            double aspect = boundsFill.getHeight() > 0.0 && boundsFill.getWidth() > 0.0 ? boundsFill.getWidth() / boundsFill.getHeight() : 1.0;
            stippleSize = new Rectangle2D.Double(0.0, 0.0, size * aspect, size);
        } else if (graphicFill instanceof IconStyle2D) {
            Icon icon = ((IconStyle2D)graphicFill).getIcon();
            stippleSize = new Rectangle2D.Double(0.0, 0.0, icon.getIconWidth(), icon.getIconHeight());
        } else {
            return;
        }
        int toX = (int)Math.ceil(boundsShape.getWidth() / ((RectangularShape)stippleSize).getWidth());
        int toY = (int)Math.ceil(boundsShape.getHeight() / ((RectangularShape)stippleSize).getHeight());
        Graphics2D g = (Graphics2D)graphics.create();
        g.clip(shape);
        Shape clipShape = g.getClip();
        Rectangle2D boundsClip = clipShape.getBounds2D();
        int fromX = 0;
        if (boundsClip.getMinX() > boundsShape.getMinX()) {
            fromX = (int)Math.floor((boundsClip.getMinX() - boundsShape.getMinX()) / ((RectangularShape)stippleSize).getWidth());
        }
        if (boundsClip.getMaxX() < boundsShape.getMaxX()) {
            toX -= (int)Math.floor((boundsShape.getMaxX() - boundsClip.getMaxX()) / ((RectangularShape)stippleSize).getWidth());
        }
        int fromY = 0;
        if (boundsClip.getMinY() > boundsShape.getMinY()) {
            fromY = (int)Math.floor((boundsClip.getMinY() - boundsShape.getMinY()) / ((RectangularShape)stippleSize).getHeight());
        }
        if (boundsClip.getMaxY() < boundsShape.getMaxY()) {
            toY -= (int)Math.floor((boundsShape.getMaxY() - boundsClip.getMaxY()) / ((RectangularShape)stippleSize).getHeight());
        }
        int i = fromX;
        while (i < toX) {
            int j = fromY;
            while (j < toY) {
                double translateY;
                double translateX = boundsShape.getMinX() + (double)i * ((RectangularShape)stippleSize).getWidth();
                if (clipShape.intersects(translateX, translateY = boundsShape.getMinY() + (double)j * ((RectangularShape)stippleSize).getHeight(), ((RectangularShape)stippleSize).getWidth(), ((RectangularShape)stippleSize).getHeight())) {
                    LiteShape2 stippleShape = this.createStippleShape(stippleSize, translateX, translateY);
                    this.paint(g, stippleShape, graphicFill, scale);
                }
                ++j;
            }
            ++i;
        }
    }

    private LiteShape2 createStippleShape(Rectangle2D stippleSize, double translateX, double translateY) {
        LiteShape2 stippleShape;
        GeometryFactory geomFactory = new GeometryFactory();
        Coordinate coord = new Coordinate(stippleSize.getCenterX() + translateX, stippleSize.getCenterY() + translateY);
        Point geom = geomFactory.createPoint(coord);
        DefaultMathTransformFactory factory = new DefaultMathTransformFactory();
        try {
            MathTransform identityTransf = factory.createAffineTransform((Matrix)new AffineTransform2D());
            Decimator nullDecimator = new Decimator(-1.0, -1.0);
            stippleShape = new LiteShape2((Geometry)geom, identityTransf, nullDecimator, false);
        }
        catch (Exception e) {
            throw new RuntimeException("Unxpected exception building lite shape", e);
        }
        return stippleShape;
    }

    protected Paint getPaint(Paint paint, AffineTransform at, Rectangle2D anchor) {
        Paint newPaint;
        if (paint instanceof TexturePaint) {
            TexturePaint tp = (TexturePaint)paint;
            BufferedImage image = tp.getImage();
            Rectangle2D rect = tp.getAnchorRect();
            double width = rect.getWidth() * at.getScaleX();
            double height = rect.getHeight() * at.getScaleY();
            Rectangle2D.Double scaledRect = new Rectangle2D.Double(0.0, 0.0, width, height);
            newPaint = new TexturePaint(image, scaledRect);
        } else if (paint instanceof LinearGradientPaint) {
            double anchorHeight;
            LinearGradientPaint lgp = (LinearGradientPaint)paint;
            double anchorWidth = anchor.getWidth();
            if (anchorWidth == 0.0) {
                anchorWidth = 1.0;
            }
            if ((anchorHeight = anchor.getHeight()) == 0.0) {
                anchorHeight = 1.0;
            }
            Point2D.Double p1 = new Point2D.Double(anchorWidth / 250.0 * lgp.getStartPoint().getX() + anchor.getMinX(), anchorHeight / 250.0 * lgp.getStartPoint().getY() + anchor.getMinY());
            Point2D.Double p2 = new Point2D.Double(anchorWidth / 250.0 * lgp.getEndPoint().getX() + anchor.getMinX(), anchorHeight / 250.0 * lgp.getEndPoint().getY() + anchor.getMinY());
            newPaint = new LinearGradientPaint((float)((Point2D)p1).getX(), (float)((Point2D)p1).getY(), (float)((Point2D)p2).getX(), (float)((Point2D)p2).getY(), lgp.getFractions(), lgp.getColors(), lgp.getCycleMethod());
        } else if (paint instanceof RadialGradientPaint) {
            double anchorRadius = Math.max(anchor.getWidth(), anchor.getHeight());
            if (anchorRadius == 0.0) {
                anchorRadius = 1.0;
            }
            RadialGradientPaint rgp = (RadialGradientPaint)paint;
            Point2D.Double center = new Point2D.Double(anchor.getWidth() / 250.0 * rgp.getCenterPoint().getX() + anchor.getMinX(), anchor.getHeight() / 250.0 * rgp.getCenterPoint().getY() + anchor.getMinY());
            newPaint = new RadialGradientPaint((float)((Point2D)center).getX(), (float)((Point2D)center).getY(), (float)(anchorRadius / 250.0 * (double)rgp.getRadius()), rgp.getFractions(), rgp.getColors(), rgp.getCycleMethod());
        } else {
            newPaint = paint;
        }
        return newPaint;
    }

    public static class TextureAnchorKey
    extends RenderingHints.Key {
        protected TextureAnchorKey() {
            super(0);
        }

        @Override
        public boolean isCompatibleValue(Object val) {
            return val instanceof Point2D;
        }
    }
}

