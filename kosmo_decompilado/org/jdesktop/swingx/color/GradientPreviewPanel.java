/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdesktop.swingx.JXPanel
 *  org.jdesktop.swingx.color.ColorUtil
 *  org.jdesktop.swingx.multislider.MultiThumbModel
 *  org.jdesktop.swingx.multislider.Thumb
 */
package org.jdesktop.swingx.color;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.event.MouseInputAdapter;
import org.jdesktop.swingx.JXGradientChooser;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.color.ColorUtil;
import org.jdesktop.swingx.multislider.MultiThumbModel;
import org.jdesktop.swingx.multislider.Thumb;

public class GradientPreviewPanel
extends JXPanel {
    private Paint checker_texture = null;
    private Point2D start;
    private Point2D end;
    public JXGradientChooser picker;
    boolean moving_start = false;
    boolean moving_end = false;
    private boolean radial = false;
    private boolean reversed = false;
    private boolean reflected = false;
    private boolean repeated = false;
    private MultipleGradientPaint gradient;
    private boolean wasReversed = false;
    private MultiThumbModel model;
    private Logger log = Logger.getLogger(GradientPreviewPanel.class.getName());

    public GradientPreviewPanel() {
        this.start = new Point2D.Float(10.0f, 10.0f);
        this.end = new Point2D.Float(80.0f, 10.0f);
        this.checker_texture = ColorUtil.getCheckerPaint();
        GradientMouseHandler ma = new GradientMouseHandler();
        this.addMouseListener(ma);
        this.addMouseMotionListener(ma);
    }

    public void setGradient() {
        this.repaint();
    }

    public void setGradient(MultipleGradientPaint grad) {
        MultipleGradientPaint old = this.getGradient();
        if (grad instanceof LinearGradientPaint) {
            LinearGradientPaint paint = (LinearGradientPaint)grad;
            this.start = paint.getStartPoint();
            this.end = paint.getEndPoint();
        } else {
            RadialGradientPaint paint = (RadialGradientPaint)grad;
            this.start = paint.getCenterPoint();
            this.end = new Point2D.Double(this.start.getX(), this.start.getY() + (double)paint.getRadius());
        }
        this.gradient = grad;
        this.firePropertyChange("gradient", old, this.getGradient());
        this.repaint();
    }

    public MultipleGradientPaint getGradient() {
        return this.gradient;
    }

    public MultipleGradientPaint calculateGradient() {
        List<Thumb<Color>> stops = this.getStops();
        int len = stops.size();
        float[] fractions = new float[len];
        Color[] colors = new Color[len];
        int i = 0;
        for (Thumb<Color> thumb : stops) {
            colors[i] = (Color)thumb.getObject();
            fractions[i] = thumb.getPosition();
            ++i;
        }
        this.setGradient(this.calculateGradient(fractions, colors));
        return this.getGradient();
    }

    private List<Thumb<Color>> getStops() {
        return this.model == null ? null : this.model.getSortedThumbs();
    }

    public void setMultiThumbModel(MultiThumbModel model) {
        MultiThumbModel old = this.getMultiThumbModel();
        this.model = model;
        this.firePropertyChange("multiThumbModel", old, this.getMultiThumbModel());
    }

    public MultiThumbModel getMultiThumbModel() {
        return this.model;
    }

    protected void paintComponent(Graphics g) {
        try {
            Graphics2D g2 = (Graphics2D)g;
            g2.setPaint(this.checker_texture);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            MultipleGradientPaint paint = this.getGradient();
            if (paint != null) {
                g2.setPaint(paint);
            } else {
                g2.setPaint(Color.black);
            }
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            this.drawHandles(g2);
        }
        catch (Exception ex) {
            this.log.severe("ex: " + ex);
        }
    }

    private MultipleGradientPaint calculateGradient(float[] fractions, Color[] colors) {
        Point2D start = this.start;
        Point2D end = this.end;
        if (this.isReversed() || this.wasReversed) {
            start = this.end;
            end = this.start;
            this.wasReversed = true;
        }
        MultipleGradientPaint.CycleMethod cycle = MultipleGradientPaint.CycleMethod.NO_CYCLE;
        if (this.isRepeated()) {
            cycle = MultipleGradientPaint.CycleMethod.REPEAT;
        }
        if (this.isReflected()) {
            cycle = MultipleGradientPaint.CycleMethod.REFLECT;
        }
        MultipleGradientPaint paint = null;
        paint = this.isRadial() ? new RadialGradientPaint(start, (float)start.distance(end), start, fractions, colors, cycle) : new LinearGradientPaint((float)start.getX(), (float)start.getY(), (float)end.getX(), (float)end.getY(), fractions, colors, cycle);
        return paint;
    }

    private void drawHandles(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.black);
        g2.drawOval((int)this.start.getX() - 5, (int)this.start.getY() - 5, 10, 10);
        g2.setColor(Color.white);
        g2.drawOval((int)this.start.getX() - 4, (int)this.start.getY() - 4, 8, 8);
        g2.setColor(Color.black);
        g2.drawOval((int)this.end.getX() - 5, (int)this.end.getY() - 5, 10, 10);
        g2.setColor(Color.white);
        g2.drawOval((int)this.end.getX() - 4, (int)this.end.getY() - 4, 8, 8);
        g2.setColor(Color.darkGray);
        g2.drawLine((int)this.start.getX(), (int)this.start.getY(), (int)this.end.getX(), (int)this.end.getY());
        g2.setColor(Color.gray);
        g2.drawLine((int)this.start.getX() - 1, (int)this.start.getY() - 1, (int)this.end.getX() - 1, (int)this.end.getY() - 1);
    }

    public boolean isRadial() {
        return this.radial;
    }

    public void setRadial(boolean radial) {
        boolean old = this.isRadial();
        this.radial = radial;
        this.firePropertyChange("radial", old, this.isRadial());
    }

    public boolean isReversed() {
        return this.reversed;
    }

    public void setReversed(boolean reversed) {
        boolean old = this.isReversed();
        this.reversed = reversed;
        this.firePropertyChange("reversed", old, this.isReversed());
    }

    public boolean isReflected() {
        return this.reflected;
    }

    public void setReflected(boolean reflected) {
        boolean old = this.isReflected();
        this.reflected = reflected;
        this.firePropertyChange("reflected", old, this.isReflected());
    }

    public boolean isRepeated() {
        return this.repeated;
    }

    public void setRepeated(boolean repeated) {
        boolean old = this.isRepeated();
        this.repeated = repeated;
        this.firePropertyChange("repeated", old, this.isRepeated());
    }

    private class GradientMouseHandler
    extends MouseInputAdapter {
        private GradientMouseHandler() {
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            GradientPreviewPanel.this.moving_start = false;
            GradientPreviewPanel.this.moving_end = false;
            if (evt.getPoint().distance(GradientPreviewPanel.this.start) < 5.0) {
                GradientPreviewPanel.this.moving_start = true;
                GradientPreviewPanel.this.start = evt.getPoint();
                return;
            }
            if (evt.getPoint().distance(GradientPreviewPanel.this.end) < 5.0) {
                GradientPreviewPanel.this.moving_end = true;
                GradientPreviewPanel.this.end = evt.getPoint();
                return;
            }
            GradientPreviewPanel.this.start = evt.getPoint();
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            if (GradientPreviewPanel.this.moving_start) {
                GradientPreviewPanel.this.start = evt.getPoint();
            } else {
                GradientPreviewPanel.this.end = evt.getPoint();
            }
            GradientPreviewPanel.this.calculateGradient();
            GradientPreviewPanel.this.repaint();
        }
    }
}

