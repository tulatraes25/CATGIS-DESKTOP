/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.operation.distance.DistanceOp
 */
package es.kosmo.core.renderer.label;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import es.kosmo.core.renderer.label.LineStringCursor;
import es.kosmo.core.renderer.label.TransformedIcon;
import es.kosmo.core.renderer.style.IconStyle2D;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.Bidi;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.Icon;
import org.saig.core.renderer.lite.LabelCacheDefault;
import org.saig.core.renderer.lite.LabelCacheItem;
import org.saig.core.renderer.lite.LineToLabel;
import org.saig.core.renderer.lite.LiteShape2;
import org.saig.core.renderer.lite.StyledShapePainter;
import org.saig.core.renderer.lite.TransformedShape;
import org.saig.core.renderer.style.GraphicStyle2D;
import org.saig.core.renderer.style.MarkStyle2D;
import org.saig.core.renderer.style.Style2D;
import org.saig.core.renderer.style.TextStyle2D;
import org.saig.core.styling.TextSymbolizer;

public class LabelPainter {
    protected static final double EPS = 1.0E-6;
    protected StyledShapePainter shapePainter = new StyledShapePainter();
    protected LabelCacheItem labelItem;
    protected List<LineInfo> lines;
    protected Graphics2D graphics;
    protected LabelCacheDefault.LabelRenderingMode labelRenderingMode;
    protected GeometryFactory gf = new GeometryFactory();
    protected Rectangle2D labelBounds;
    private Point anchorPoint;

    public LabelPainter(Graphics2D graphics, LabelCacheDefault.LabelRenderingMode labelRenderingMode) {
        this.graphics = graphics;
        this.labelRenderingMode = labelRenderingMode;
    }

    public void setLabel(LabelCacheItem labelItem) {
        this.labelItem = labelItem;
        labelItem.getTextStyle().setLabel(labelItem.getLabel());
        this.labelBounds = null;
        this.lines = null;
        String text = labelItem.getLabel();
        if (!text.contains("\\n") && labelItem.getAutoWrap() <= 0 || labelItem.isFollowLineEnabled()) {
            FontRenderContext frc = this.graphics.getFontRenderContext();
            TextLayout layout = new TextLayout(text, labelItem.getTextStyle().getFont(), frc);
            LineInfo line = new LineInfo(text, this.layoutSentence(text, labelItem), layout);
            this.labelBounds = line.gv.getVisualBounds();
            this.normalizeBounds(this.labelBounds);
            this.lines = Collections.singletonList(line);
            return;
        }
        String[] splitted = text.split("\\\\n");
        this.lines = new ArrayList<LineInfo>();
        if (labelItem.getAutoWrap() <= 0) {
            String[] stringArray = splitted;
            int n = splitted.length;
            int line = 0;
            while (line < n) {
                String line2 = stringArray[line];
                FontRenderContext frc = this.graphics.getFontRenderContext();
                TextLayout layout = new TextLayout(line2, labelItem.getTextStyle().getFont(), frc);
                LineInfo info = new LineInfo(line2, this.layoutSentence(line2, labelItem), layout);
                this.lines.add(info);
                ++line;
            }
        } else {
            HashMap<TextAttribute, Font> map = new HashMap<TextAttribute, Font>();
            map.put(TextAttribute.FONT, labelItem.getTextStyle().getFont());
            int i = 0;
            while (i < splitted.length) {
                String line = splitted[i];
                AttributedString attributed = new AttributedString(line, map);
                AttributedCharacterIterator iter = attributed.getIterator();
                LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(iter, BreakIterator.getWordInstance(), this.graphics.getFontRenderContext());
                BreakIterator breaks = BreakIterator.getWordInstance();
                breaks.setText(line);
                int prevPosition = 0;
                while (lineMeasurer.getPosition() < iter.getEndIndex()) {
                    TextLayout layout = lineMeasurer.nextLayout(labelItem.getAutoWrap(), line.length(), true);
                    int newPosition = prevPosition;
                    if (layout != null) {
                        newPosition = lineMeasurer.getPosition();
                    } else {
                        int nextBoundary = breaks.following(prevPosition);
                        newPosition = nextBoundary == -1 ? line.length() : nextBoundary;
                        AttributedCharacterIterator subIter = attributed.getIterator(null, prevPosition, newPosition);
                        layout = new TextLayout(subIter, this.graphics.getFontRenderContext());
                        lineMeasurer.setPosition(newPosition);
                    }
                    String extracted = line.substring(prevPosition, newPosition).trim();
                    if (!"".equals(extracted)) {
                        LineInfo info = new LineInfo(extracted, this.layoutSentence(extracted, labelItem), layout);
                        this.lines.add(info);
                    }
                    prevPosition = newPosition;
                }
                ++i;
            }
        }
        double maxWidth = 0.0;
        for (LineInfo line : this.lines) {
            maxWidth = Math.max(line.gv.getVisualBounds().getWidth(), maxWidth);
        }
        double boundsY = 0.0;
        double labelY = 0.0;
        for (LineInfo info : this.lines) {
            double minX;
            Rectangle2D currBounds = info.gv.getVisualBounds();
            TextLayout layout = info.layout;
            info.x = minX = (maxWidth - currBounds.getWidth()) * labelItem.getTextStyle().getAnchorX() - currBounds.getMinX();
            if (this.labelBounds == null) {
                this.labelBounds = currBounds;
                boundsY = currBounds.getMinY() + (double)layout.getAscent() + (double)layout.getDescent() + (double)layout.getLeading();
            } else {
                Rectangle2D.Double translated = new Rectangle2D.Double(minX, boundsY, currBounds.getWidth(), currBounds.getHeight());
                boundsY += (double)(layout.getAscent() + layout.getDescent() + layout.getLeading());
                labelY += (double)(layout.getAscent() + layout.getDescent() + layout.getLeading());
                this.labelBounds = this.labelBounds.createUnion(translated);
            }
            info.y = labelY;
        }
        this.normalizeBounds(this.labelBounds);
    }

    void normalizeBounds(Rectangle2D bounds) {
        if (bounds.isEmpty()) {
            bounds.setRect(bounds.getCenterX() - 1.0, bounds.getCenterY() - 1.0, 2.0, 2.0);
        }
    }

    protected GlyphVector layoutSentence(String label, LabelCacheItem item) {
        int length;
        Font font = item.getTextStyle().getFont();
        char[] chars = label.toCharArray();
        if (Bidi.requiresBidi(chars, 0, length = label.length())) {
            Bidi bidi = new Bidi(label, -2);
            if (bidi.isRightToLeft()) {
                return font.layoutGlyphVector(this.graphics.getFontRenderContext(), chars, 0, length, 1);
            }
            if (bidi.isMixed()) {
                String r = "";
                int i = 0;
                while (i < bidi.getRunCount()) {
                    String s1 = label.substring(bidi.getRunStart(i), bidi.getRunLimit(i));
                    if (bidi.getRunLevel(i) % 2 == 0) {
                        s1 = new StringBuffer(s1).reverse().toString();
                    }
                    r = String.valueOf(r) + s1;
                    ++i;
                }
                char[] chars2 = r.toCharArray();
                return font.layoutGlyphVector(this.graphics.getFontRenderContext(), chars2, 0, length, 1);
            }
        }
        return font.createGlyphVector(this.graphics.getFontRenderContext(), chars);
    }

    public LabelCacheItem getLabel() {
        return this.labelItem;
    }

    public double getLineHeight() {
        return this.lines.get((int)0).gv.getVisualBounds().getHeight() - (double)this.lines.get((int)0).layout.getDescent();
    }

    public double getAscent() {
        return this.lines.get((int)0).layout.getAscent();
    }

    public int getStraightLabelWidth() {
        return (int)Math.round(this.getLabelBounds().getWidth());
    }

    public int getLineCount() {
        return this.lines.size();
    }

    public Rectangle2D getFullLabelBounds() {
        Rectangle2D bounds = (Rectangle2D)this.getLabelBounds().clone();
        int haloRadius = Math.round(this.labelItem.getTextStyle().getHaloFill() != null ? this.labelItem.getTextStyle().getHaloRadius() : 0.0f);
        bounds.add(bounds.getMinX() - (double)haloRadius, bounds.getMinY() - (double)haloRadius);
        bounds.add(bounds.getMaxX() + (double)haloRadius, bounds.getMaxY() + (double)haloRadius);
        if (this.labelItem.getTextStyle().getGraphic() != null) {
            Rectangle2D shieldBounds;
            Rectangle area = this.labelItem.getTextStyle().getGraphicDimensions();
            int[] margin = this.labelItem.getGraphicMargin();
            TextSymbolizer.GraphicResize mode = this.labelItem.getGraphicsResize();
            if (mode == TextSymbolizer.GraphicResize.STRETCH) {
                shieldBounds = this.applyMargins(margin, bounds);
            } else if (mode == TextSymbolizer.GraphicResize.PROPORTIONAL) {
                double factor = 1.0;
                factor = bounds.getWidth() > bounds.getHeight() ? bounds.getWidth() / ((RectangularShape)area).getWidth() : bounds.getHeight() / ((RectangularShape)area).getHeight();
                double width = ((RectangularShape)area).getWidth() * factor;
                double height = ((RectangularShape)area).getHeight() * factor;
                shieldBounds = new Rectangle2D.Double(bounds.getCenterX() - width / 2.0, bounds.getCenterY() - height / 2.0, width, height);
                shieldBounds = this.applyMargins(margin, shieldBounds);
            } else {
                shieldBounds = new Rectangle2D.Double(-((RectangularShape)area).getWidth() / 2.0 + bounds.getMinX() - bounds.getWidth() / 2.0, -((RectangularShape)area).getHeight() / 2.0 + bounds.getMinY() - bounds.getHeight() / 2.0, ((RectangularShape)area).getWidth(), ((RectangularShape)area).getHeight());
            }
            bounds = bounds.createUnion(shieldBounds);
        }
        this.normalizeBounds(bounds);
        return bounds;
    }

    Rectangle2D applyMargins(int[] margin, Rectangle2D bounds) {
        if (margin != null) {
            double xmin = bounds.getMinX() - (double)margin[3];
            double ymin = bounds.getMinY() - (double)margin[0];
            double width = bounds.getWidth() + (double)margin[1] + (double)margin[3];
            double height = bounds.getHeight() + (double)margin[0] + (double)margin[2];
            return new Rectangle2D.Double(xmin, ymin, width, height);
        }
        return bounds;
    }

    public Rectangle2D getLabelBounds() {
        return this.labelBounds;
    }

    public void paintStraightLabel(AffineTransform transform) throws Exception {
        AffineTransform oldTransform = this.graphics.getTransform();
        try {
            this.drawLabelLine(this.graphics, this.labelItem);
            AffineTransform newTransform = new AffineTransform(oldTransform);
            newTransform.concatenate(transform);
            this.graphics.setTransform(newTransform);
            Style2D graphic = this.labelItem.getTextStyle().getGraphic();
            if (graphic != null) {
                double offsetY = 0.0;
                double offsetX = 0.0;
                int[] margin = this.labelItem.getGraphicMargin();
                if (margin != null) {
                    offsetX = margin[1] - margin[3];
                    offsetY = margin[2] - margin[0];
                }
                LiteShape2 tempShape = new LiteShape2((Geometry)this.gf.createPoint(new Coordinate(this.labelBounds.getCenterX() + offsetX, this.labelBounds.getCenterY() + offsetY)), null, null, false, false);
                graphic = this.resizeGraphic(graphic);
                this.shapePainter.paint(this.graphics, tempShape, graphic, 1.0);
            }
            if (this.labelItem.getTextStyle().getFont().getSize() == 0) {
                return;
            }
            if (this.lines.size() == 1) {
                this.drawGlyphVector(this.lines.get((int)0).gv, true);
            } else {
                AffineTransform lineTx = new AffineTransform(transform);
                for (LineInfo line : this.lines) {
                    lineTx.setTransform(transform);
                    lineTx.translate(line.x, line.y);
                    this.graphics.setTransform(lineTx);
                    this.drawHalo(line.gv);
                }
                lineTx = new AffineTransform(transform);
                for (LineInfo line : this.lines) {
                    lineTx.setTransform(transform);
                    lineTx.translate(line.x, line.y);
                    this.graphics.setTransform(lineTx);
                    this.drawGlyphVector(line.gv, false);
                }
            }
        }
        finally {
            this.graphics.setTransform(oldTransform);
        }
    }

    protected Style2D resizeGraphic(Style2D graphic) {
        TextSymbolizer.GraphicResize mode = this.labelItem.getGraphicsResize();
        if (mode == TextSymbolizer.GraphicResize.NONE || mode == null) {
            return graphic;
        }
        double width = this.getFullLabelBounds().getWidth();
        double height = this.getFullLabelBounds().getHeight();
        width = Math.round(width);
        height = Math.round(height);
        if (width <= 0.0 || height <= 0.0) {
            return null;
        }
        if (graphic instanceof MarkStyle2D) {
            MarkStyle2D mark = (MarkStyle2D)graphic;
            Shape original = mark.getShape();
            Rectangle2D bounds = original.getBounds2D();
            MarkStyle2D resized = (MarkStyle2D)mark.clone();
            if (mode == TextSymbolizer.GraphicResize.PROPORTIONAL) {
                if (width > height) {
                    resized.setSize((int)Math.round(bounds.getHeight() * width / bounds.getWidth()));
                } else {
                    resized.setSize((int)height);
                }
            } else {
                TransformedShape tss = new TransformedShape();
                tss.shape = original;
                tss.setTransform(AffineTransform.getScaleInstance(width / bounds.getWidth(), height / bounds.getHeight()));
                resized.setShape(tss);
                if (width > height) {
                    resized.setSize((int)width);
                } else {
                    resized.setSize((int)height);
                }
            }
            return resized;
        }
        if (graphic instanceof IconStyle2D) {
            AffineTransform at;
            IconStyle2D iconStyle = (IconStyle2D)graphic;
            IconStyle2D resized = (IconStyle2D)iconStyle.clone();
            Icon icon = iconStyle.getIcon();
            if (mode == TextSymbolizer.GraphicResize.PROPORTIONAL) {
                double factor = width > height ? width / (double)icon.getIconWidth() : height / (double)icon.getIconHeight();
                at = AffineTransform.getScaleInstance(factor, factor);
            } else {
                at = AffineTransform.getScaleInstance(width / (double)icon.getIconWidth(), height / (double)icon.getIconHeight());
            }
            resized.setIcon(new TransformedIcon(icon, at));
            return resized;
        }
        if (graphic instanceof GraphicStyle2D) {
            AffineTransform at;
            GraphicStyle2D gstyle = (GraphicStyle2D)graphic;
            GraphicStyle2D resized = (GraphicStyle2D)graphic.clone();
            BufferedImage image = gstyle.getImage();
            if (mode == TextSymbolizer.GraphicResize.PROPORTIONAL) {
                double factor = width > height ? width / (double)image.getWidth() : height / (double)image.getHeight();
                at = AffineTransform.getScaleInstance(factor, factor);
            } else {
                at = AffineTransform.getScaleInstance(width / (double)image.getWidth(), height / (double)image.getHeight());
            }
            AffineTransformOp ato = new AffineTransformOp(at, 2);
            image = ato.filter(image, null);
            resized.setImage(image);
            return resized;
        }
        return graphic;
    }

    protected void drawGlyphVector(GlyphVector gv, boolean drawHalo) {
        Shape outline = gv.getOutline();
        if (this.labelItem.getTextStyle().getHaloFill() != null && drawHalo) {
            this.configureHalo();
            this.graphics.draw(outline);
        }
        this.configureLabelStyle();
        if (this.labelRenderingMode == LabelCacheDefault.LabelRenderingMode.STRING) {
            this.graphics.drawGlyphVector(gv, 0.0f, 0.0f);
        } else if (this.labelRenderingMode == LabelCacheDefault.LabelRenderingMode.OUTLINE) {
            this.graphics.fill(outline);
        } else {
            AffineTransform tx = this.graphics.getTransform();
            if (Math.abs(tx.getShearX()) >= 1.0E-6 || Math.abs(tx.getShearY()) > 1.0E-6) {
                this.graphics.fill(outline);
            } else {
                this.graphics.drawGlyphVector(gv, 0.0f, 0.0f);
            }
        }
    }

    protected void drawHalo(GlyphVector gv) {
        Shape outline = gv.getOutline();
        if (this.labelItem.getTextStyle().getHaloFill() != null) {
            this.configureHalo();
            this.graphics.draw(outline);
        }
    }

    protected void configureHalo() {
        this.graphics.setPaint(this.labelItem.getTextStyle().getHaloFill());
        this.graphics.setComposite(this.labelItem.getTextStyle().getHaloComposite());
        float haloRadius = this.labelItem.getTextStyle().getHaloFill() != null ? this.labelItem.getTextStyle().getHaloRadius() : 0.0f;
        this.graphics.setStroke(new BasicStroke(2.0f * haloRadius, 1, 1));
    }

    protected void configureLabelStyle() {
        Paint fill = this.labelItem.getTextStyle().getFill();
        Composite comp = this.labelItem.getTextStyle().getComposite();
        if (fill == null) {
            fill = Color.BLACK;
            comp = AlphaComposite.getInstance(3, 1.0f);
        }
        this.graphics.setPaint(fill);
        this.graphics.setComposite(comp);
    }

    public void paintCurvedLabel(LineStringCursor cursor) {
        if (this.labelItem.getTextStyle().getFont().getSize() == 0) {
            return;
        }
        GlyphVector glyphVector = this.lines.get((int)0).gv;
        AffineTransform oldTransform = this.graphics.getTransform();
        try {
            if (!this.isLabelUpwards(cursor) && this.labelItem.isForceLeftToRightEnabled()) {
                LineStringCursor reverse = cursor.reverse();
                reverse.moveTo(cursor.getLineStringLength() - cursor.getCurrentOrdinate());
                cursor = reverse;
            }
            double anchorY = this.getLinePlacementYAnchor();
            double mid = cursor.getCurrentOrdinate();
            Coordinate c = new Coordinate();
            c = cursor.getCurrentPosition(c);
            this.graphics.setPaint(Color.BLACK);
            double startOrdinate = mid - (double)(this.getStraightLabelWidth() / 2);
            if (startOrdinate < 0.0) {
                startOrdinate = 0.0;
            }
            cursor.moveTo(startOrdinate);
            int numGlyphs = glyphVector.getNumGlyphs();
            float nextAdvance = glyphVector.getGlyphMetrics(0).getAdvance() * 0.5f;
            Shape[] outlines = new Shape[numGlyphs];
            AffineTransform[] transforms = new AffineTransform[numGlyphs];
            int i = 0;
            while (i < numGlyphs) {
                outlines[i] = glyphVector.getGlyphOutline(i);
                Point2D p = glyphVector.getGlyphPosition(i);
                float advance = nextAdvance;
                nextAdvance = i < numGlyphs - 1 ? glyphVector.getGlyphMetrics(i + 1).getAdvance() * 0.5f : 0.0f;
                c = cursor.getCurrentPosition(c);
                AffineTransform t = new AffineTransform();
                t.setToTranslation(c.x, c.y);
                t.rotate(cursor.getCurrentAngle());
                t.translate(-p.getX() - (double)advance, -p.getY() + this.getLineHeight() * anchorY);
                transforms[i] = t;
                cursor.moveTo(cursor.getCurrentOrdinate() + (double)advance + (double)nextAdvance);
                ++i;
            }
            if (this.labelItem.getTextStyle().getHaloFill() != null) {
                this.configureHalo();
                i = 0;
                while (i < numGlyphs) {
                    this.graphics.setTransform(transforms[i]);
                    this.graphics.draw(outlines[i]);
                    ++i;
                }
            }
            this.configureLabelStyle();
            i = 0;
            while (i < numGlyphs) {
                this.graphics.setTransform(transforms[i]);
                this.graphics.fill(outlines[i]);
                ++i;
            }
        }
        finally {
            this.graphics.setTransform(oldTransform);
        }
    }

    public double getLinePlacementYAnchor() {
        TextStyle2D textStyle = this.getLabel().getTextStyle();
        LineMetrics lm = textStyle.getFont().getLineMetrics(textStyle.getLabel(), this.graphics.getFontRenderContext());
        if (lm.getHeight() > 0.0f) {
            return (Math.abs(lm.getStrikethroughOffset()) + lm.getDescent() + lm.getLeading()) / lm.getHeight();
        }
        return 0.0;
    }

    protected boolean isLabelUpwards(LineStringCursor cursor) {
        double labelAngle = cursor.getCurrentAngle() + 1.5707963267948966;
        return (labelAngle %= Math.PI * 2) >= 0.0 && labelAngle < Math.PI;
    }

    protected void drawLabelLine(Graphics2D graphics, LabelCacheItem labelItem) {
        Coordinate c2;
        if (labelItem.getLineToLabel() == null || this.anchorPoint == null || labelItem.getOriginalShape() == null) {
            return;
        }
        Geometry originalGeom = labelItem.getOriginalShape();
        LineToLabel lineToLabel = labelItem.getLineToLabel();
        graphics.setStroke(new BasicStroke((float)lineToLabel.getWidth(), 0, 0, 10.0f, lineToLabel.getDash(), 0.0f));
        graphics.setColor(lineToLabel.getColor());
        Coordinate c1 = this.anchorPoint.getCoordinate();
        switch (lineToLabel.getLineToLabelEndingAnchorOption()) {
            case CENTROID: {
                c2 = originalGeom.getCentroid().getCoordinate();
                break;
            }
            case CENTROID_INSIDE: {
                try {
                    if (originalGeom.getNumPoints() < 4) {
                        c2 = originalGeom.getCoordinate();
                        break;
                    }
                    Point anchorPoint = originalGeom.getCentroid();
                    if (!originalGeom.contains((Geometry)anchorPoint)) {
                        anchorPoint = originalGeom.getInteriorPoint();
                    }
                    c2 = anchorPoint.getCoordinate();
                }
                catch (Exception e) {
                    c2 = originalGeom.getCoordinate();
                }
                break;
            }
            default: {
                c2 = DistanceOp.nearestPoints((Geometry)originalGeom, (Geometry)this.anchorPoint)[0];
            }
        }
        Line2D.Double line = new Line2D.Double(c1.x, c1.y, c2.x, c2.y);
        graphics.draw(line);
    }

    public void setAnchorPoint(Point point) {
        this.anchorPoint = point;
    }

    protected static class LineInfo {
        public double x;
        public double y;
        public String text;
        public GlyphVector gv;
        TextLayout layout;

        public LineInfo(String text, GlyphVector gv, TextLayout layout) {
            this.text = text;
            this.gv = gv;
            this.layout = layout;
        }

        public LineInfo(String text, GlyphVector gv) {
            this.text = text;
            this.gv = gv;
        }
    }
}

