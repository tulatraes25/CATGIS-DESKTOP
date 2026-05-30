/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdesktop.swingx.JXPanel
 */
package es.kosmo.desktop.gui.components;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Paint;
import org.jdesktop.swingx.JXPanel;

public class GradientPanel
extends JXPanel {
    private static final long serialVersionUID = 1L;
    protected Color gradientStart = new Color(185, 209, 234);
    protected Color gradientEnd = new Color(152, 180, 208);

    public GradientPanel() {
    }

    public GradientPanel(LayoutManager manager) {
        super(manager);
    }

    public void paintComponent(Graphics g) {
        int height = this.getHeight();
        Graphics2D g2 = (Graphics2D)g;
        GradientPaint painter = new GradientPaint(0.0f, 0.0f, this.gradientStart, 0.0f, height, this.gradientEnd);
        Paint oldPainter = g2.getPaint();
        g2.setPaint(painter);
        g2.fill(g2.getClip());
        painter = new GradientPaint(0.0f, 0.0f, this.gradientEnd, 0.0f, height / 2, this.gradientStart);
        g2.setPaint(painter);
        g2.fill(g2.getClip());
        painter = new GradientPaint(0.0f, height / 2, this.gradientStart, 0.0f, height, this.gradientEnd);
        g2.setPaint(painter);
        g2.fill(g2.getClip());
        g2.setPaint(oldPainter);
    }
}

