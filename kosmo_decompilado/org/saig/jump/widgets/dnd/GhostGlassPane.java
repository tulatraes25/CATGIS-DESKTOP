/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.dnd;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.saig.jump.widgets.dnd.DragAndDropLock;

public class GhostGlassPane
extends JPanel {
    private static final int ANIMATION_DELAY = 500;
    private BufferedImage dragged = null;
    private Point location = new Point(0, 0);
    private Point oldLocation = new Point(0, 0);
    private int width;
    private int height;
    private Rectangle visibleRect = null;
    private float zoom = 1.0f;
    private float alpha = 0.6f;

    public GhostGlassPane() {
        this.setOpaque(false);
    }

    public void setImage(BufferedImage dragged) {
        this.setImage(dragged, dragged == null ? 0 : dragged.getWidth());
    }

    public void setImage(BufferedImage dragged, int width) {
        if (dragged != null) {
            float ratio = (float)dragged.getWidth() / (float)dragged.getHeight();
            this.width = width;
            this.height = (int)((float)width / ratio);
        }
        this.dragged = dragged;
    }

    public void setPoint(Point location) {
        this.oldLocation = this.location;
        this.location = location;
    }

    public Rectangle getRepaintRect() {
        int x = (int)(this.location.getX() - (double)((float)this.width * this.zoom / 2.0f));
        int y = (int)(this.location.getY() - (double)((float)this.height * this.zoom / 2.0f));
        int x2 = (int)(this.oldLocation.getX() - (double)((float)this.width * this.zoom / 2.0f));
        int y2 = (int)(this.oldLocation.getY() - (double)((float)this.height * this.zoom / 2.0f));
        int width = (int)((float)this.width * this.zoom);
        int height = (int)((float)this.height * this.zoom);
        return new Rectangle(x, y, width, height).union(new Rectangle(x2, y2, width, height));
    }

    public Rectangle getGhostGlassPaneBounds() {
        int x = (int)(this.location.getX() - (double)((float)this.width * this.zoom / 2.0f));
        int y = (int)(this.location.getY() - (double)((float)this.height * this.zoom / 2.0f));
        int x2 = (int)(this.location.getX() + (double)((float)this.width * this.zoom / 2.0f));
        int y2 = (int)(this.location.getY() + (double)((float)this.height * this.zoom / 2.0f));
        return new Rectangle(x, y, x2, y2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (this.dragged == null || !this.isVisible()) {
            return;
        }
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setClip(null);
        int x = (int)(this.location.getX() - (double)((float)this.width * this.zoom / 2.0f));
        int y = (int)(this.location.getY() - (double)((float)this.height * this.zoom / 2.0f));
        g2.setComposite(AlphaComposite.getInstance(3, this.alpha));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (this.visibleRect != null) {
            g2.setClip(this.visibleRect);
        }
        if (this.visibleRect != null) {
            Area clip = new Area(this.visibleRect);
            g2.setClip(clip);
        }
        g2.drawImage(this.dragged, x, y, (int)((float)this.width * this.zoom), (int)((float)this.height * this.zoom), null);
        g2.dispose();
    }

    public void startAnimation(Rectangle visibleRect) {
        this.visibleRect = visibleRect;
        new Timer(33, new FadeOutAnimation()).start();
    }

    private class FadeOutAnimation
    implements ActionListener {
        private long start = System.currentTimeMillis();

        FadeOutAnimation() {
            GhostGlassPane.this.oldLocation = GhostGlassPane.this.location;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            long elapsed = System.currentTimeMillis() - this.start;
            if (elapsed > 500L) {
                ((Timer)e.getSource()).stop();
                GhostGlassPane.this.setVisible(false);
                GhostGlassPane.this.zoom = 1.0f;
                GhostGlassPane.this.alpha = 0.6f;
                GhostGlassPane.this.visibleRect = null;
                GhostGlassPane.this.dragged = null;
                DragAndDropLock.setLocked(false);
            } else {
                GhostGlassPane.this.alpha = 0.6f - 0.6f * (float)elapsed / 500.0f;
                GhostGlassPane.this.zoom = 1.0f + 3.0f * ((float)elapsed / 500.0f);
            }
            GhostGlassPane.this.repaint(GhostGlassPane.this.getRepaintRect());
        }
    }
}

