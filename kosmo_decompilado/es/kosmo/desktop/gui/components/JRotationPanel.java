/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JRotationPanel
extends JPanel
implements MouseListener,
MouseMotionListener {
    private static final long serialVersionUID = 1L;
    protected float angle = 0.0f;
    protected int x;
    protected int y;
    protected ChangeListener cl = null;

    public JRotationPanel() {
        this(0.0f);
    }

    public JRotationPanel(float angle) {
        this.angle = angle;
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(170, 170);
    }

    public float getAngle() {
        return this.angle;
    }

    public void update() {
        if (this.cl != null) {
            ChangeEvent ev = new ChangeEvent(this);
            this.cl.stateChanged(ev);
            this.repaint();
        }
    }

    public void addChangeListener(ChangeListener cl) {
        this.cl = cl;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D)g;
        int ratio = Math.min(this.getWidth(), this.getHeight());
        ratio = (int)((double)ratio * 0.8);
        this.x = this.getWidth() / 2;
        this.y = this.getHeight() / 2;
        g2.setColor(Color.BLACK);
        Ellipse2D.Float circle = new Ellipse2D.Float((this.getWidth() - ratio) / 2, (this.getHeight() - ratio) / 2, ratio, ratio);
        g2.fill(circle);
        g2.setColor(Color.WHITE);
        int i = 0;
        while (i < 16) {
            g2.drawLine(this.x, this.y - ratio / 2, this.x, this.y - (int)((double)ratio * 0.8 / 2.0));
            g2.rotate(0.39269908169872414, this.x, this.y);
            ++i;
        }
        Polygon p = new Polygon();
        p.addPoint(this.x, this.y - (int)((double)ratio * 0.8 / 2.0));
        p.addPoint(this.x + 5, this.y + (int)((double)ratio * 0.1));
        p.addPoint(this.x - 5, this.y + (int)((double)ratio * 0.1));
        g2.rotate(this.angle, this.x, this.y);
        g2.setColor(Color.WHITE);
        g2.fill(p);
        Ellipse2D.Float circle2 = new Ellipse2D.Float(this.x - 2, this.y - 2, 4.0f, 4.0f);
        g2.setColor(Color.BLACK);
        g2.fill(circle2);
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        this.angle = this.getAngle(x, y);
        this.repaint();
        ChangeEvent event = new ChangeEvent(this);
        this.cl.stateChanged(event);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        this.angle = this.getAngle(x, y);
        this.repaint();
        ChangeEvent event = new ChangeEvent(this);
        this.cl.stateChanged(event);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    private float getAngle(int x, int y) {
        float d = (float)Math.sqrt((x - this.x) * (x - this.x) + (y - this.y) * (y - this.y));
        float sina = (float)(x - this.x) / d;
        float cosa = (float)(this.y - y) / d;
        float angle = cosa > 0.0f ? (float)Math.asin(sina) : (float)(-Math.asin(sina) + Math.PI);
        if (angle < 0.0f) {
            angle = (float)((double)angle + Math.PI * 2);
        }
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
        this.repaint();
    }
}

