/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.model.TextBalloonLayer;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.saig.core.model.globes.TextBalloon;

public class BalloonDrawer {
    private static BalloonDrawer instance;

    protected BalloonDrawer() {
    }

    public static BalloonDrawer getInstance() {
        if (instance == null) {
            instance = new BalloonDrawer();
        }
        return instance;
    }

    protected AffineTransform getTransform(Graphics2D g, Dimension d, Envelope viewportEnvelope) {
        double h = d.getHeight();
        double scale = d.getWidth() / viewportEnvelope.getWidth();
        AffineTransform newtr = new AffineTransform();
        newtr.preConcatenate(AffineTransform.getTranslateInstance(-viewportEnvelope.getMinX(), -viewportEnvelope.getMinY()));
        newtr.preConcatenate(AffineTransform.getScaleInstance(scale, -scale));
        newtr.preConcatenate(AffineTransform.getTranslateInstance(0.0, h));
        return newtr;
    }

    public void draw(Graphics2D g, TextBalloonLayer layer, Dimension d, Envelope viewportEnvelope) throws Exception {
        AffineTransform oldtr = g.getTransform();
        AffineTransform newtr = this.getTransform(g, d, viewportEnvelope);
        g.setTransform(newtr);
        List<TextBalloon> balloons = layer.getDataSource().query(viewportEnvelope);
        for (TextBalloon balloon : balloons) {
            this.drawBalloon(g, balloon);
            if (balloon.getText() == null) continue;
            this.drawBalloonText(g, balloon);
        }
        g.setTransform(oldtr);
    }

    protected void drawBalloonText(Graphics2D g, TextBalloon balloon) {
        Envelope tz = balloon.getBalloonTextZone();
        double margin = balloon.getMargin();
        double xmargin = margin * tz.getWidth();
        double ymargin = margin * tz.getHeight();
        double oneMinusMargin = 1.0 - 2.0 * margin;
        Rectangle2D.Double rz2D = new Rectangle2D.Double(tz.getMinX() + xmargin, tz.getMinY() + ymargin, tz.getWidth() * oneMinusMargin, tz.getHeight() * oneMinusMargin);
        g.setFont(balloon.getTextfont());
        FontMetrics fm = g.getFontMetrics();
        int row = 0;
        List<GlyphVector> vectors = this.getGlyphVectors(g, rz2D, balloon.getText(), balloon.getTextfont());
        for (GlyphVector gv : vectors) {
            ++row;
            int i = 0;
            while (i < gv.getNumGlyphs()) {
                gv.setGlyphTransform(i, AffineTransform.getScaleInstance(1.0, -1.0));
                ++i;
            }
            g.drawGlyphVector(gv, (int)(tz.getMinX() + xmargin), (int)(tz.getMaxY() - (double)(row * fm.getHeight()) - ymargin));
        }
    }

    protected List<GlyphVector> getGlyphVectors(Graphics2D g, Rectangle2D textZone, String text, Font font) {
        ArrayList<GlyphVector> vectors = new ArrayList<GlyphVector>();
        StringTokenizer strtok = new StringTokenizer(text, " \n", true);
        String line = "";
        while (strtok.hasMoreElements()) {
            String tok = strtok.nextToken();
            GlyphVector glyph = font.createGlyphVector(g.getFontRenderContext(), String.valueOf(line) + tok);
            if (glyph.getVisualBounds().getWidth() > textZone.getWidth() || this.hasEOL(tok)) {
                vectors.add(font.createGlyphVector(g.getFontRenderContext(), line));
                line = "";
            }
            line = String.valueOf(line) + tok;
        }
        if (!line.equals("")) {
            GlyphVector glyph = font.createGlyphVector(g.getFontRenderContext(), line);
            vectors.add(glyph);
        }
        return vectors;
    }

    private boolean hasEOL(String tok) {
        return tok.charAt(tok.length() - 1) == '\n';
    }

    protected void drawBalloon(Graphics2D g, TextBalloon balloon) {
        Shape balloonshape = this.getBalloonShape(balloon);
        g.setStroke(new BasicStroke((int)balloon.getLineWidth()));
        g.setPaint(balloon.getBackgroundColor());
        g.fill(balloonshape);
        g.setColor(balloon.getLineColor());
        g.draw(balloonshape);
    }

    public Shape getBalloonShape(TextBalloon balloon) {
        Envelope tz = balloon.getBalloonTextZone();
        Rectangle2D.Double tzr = new Rectangle2D.Double(tz.getMinX(), tz.getMinY(), tz.getWidth(), tz.getHeight());
        Coordinate cr = balloon.getBalloonEnd();
        GeneralPath arrow = new GeneralPath();
        double trianglebase = tz.getHeight() / 10.0;
        Area balloonShape = new Area(tzr);
        double vx = tz.centre().x - cr.x;
        double vy = tz.centre().y - cr.y;
        double distance = Math.sqrt(vx * vx + vy * vy);
        if (distance != 0.0) {
            double aux = vx /= distance;
            vx = (vy /= distance) * trianglebase;
            vy = -aux * trianglebase;
            arrow.moveTo(tz.centre().x - vx, tz.centre().y - vy);
            arrow.lineTo(tz.centre().x + vx, tz.centre().y + vy);
            arrow.lineTo(cr.x, cr.y);
            arrow.lineTo(tz.centre().x - vx, tz.centre().y - vy);
            balloonShape.add(new Area(arrow));
        }
        return balloonShape;
    }
}

