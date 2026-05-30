/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class ScalableImagePanel
extends JPanel {
    private static final long serialVersionUID = 1225889092248864634L;
    private static final ScaleType DEFAULT_SCALABLE_TYPE = ScaleType.FIT_TO_PANEL;
    private BufferedImage image;
    private float minScale;
    private float maxScale;

    public ScalableImagePanel() {
        this.resetScales(DEFAULT_SCALABLE_TYPE);
    }

    public ScalableImagePanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        this.resetScales(DEFAULT_SCALABLE_TYPE);
    }

    public ScalableImagePanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
        this.resetScales(DEFAULT_SCALABLE_TYPE);
    }

    public ScalableImagePanel(LayoutManager layout) {
        super(layout);
        this.resetScales(DEFAULT_SCALABLE_TYPE);
    }

    public ScalableImagePanel(BufferedImage image) {
        this(image, DEFAULT_SCALABLE_TYPE);
    }

    public ScalableImagePanel(BufferedImage image, ScaleType type) {
        this(image, type.minScale, type.maxScale);
    }

    public ScalableImagePanel(BufferedImage image, double minScale, double maxScale) {
        this(image, (float)minScale, (float)maxScale);
    }

    public ScalableImagePanel(BufferedImage image, float minScale, float maxScale) {
        this.image = image;
        this.resetScales(minScale, maxScale);
    }

    private static void checkForValidScale(float minScale, float maxScale) {
        String error = null;
        if (minScale > maxScale) {
            error = "minScale must be lower than maxScale";
        }
        if (minScale < 0.0f || maxScale < 0.0f) {
            error = "scales must be over 0";
        }
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        float panelAspectRatio;
        int imageHeight;
        super.paintComponent(g);
        int panelWidth = this.getWidth();
        int panelHeight = this.getHeight();
        g.setColor(this.getBackground());
        g.fillRect(0, 0, panelWidth, panelHeight);
        if (this.image == null) {
            return;
        }
        int imageWidth = this.image.getWidth();
        float imageAspectRatio = (float)imageWidth / (float)(imageHeight = this.image.getHeight());
        float scale = imageAspectRatio > (panelAspectRatio = (float)panelWidth / (float)panelHeight) ? (float)panelWidth / (float)imageWidth : (float)panelHeight / (float)imageHeight;
        if (scale > this.maxScale) {
            scale = this.maxScale;
        }
        if (scale < this.minScale) {
            scale = this.minScale;
        }
        int scaledWidth = (int)((float)imageWidth * scale);
        int scaledHeight = (int)((float)imageHeight * scale);
        int panelCenterX = panelWidth / 2;
        int panelCenterY = panelHeight / 2;
        int x = panelCenterX - scaledWidth / 2;
        int y = panelCenterY - scaledHeight / 2;
        g.drawImage(this.image, x, y, scaledWidth, scaledHeight, null);
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        this.repaint();
    }

    public void resetScales(ScaleType type) {
        this.minScale = type.minScale;
        this.maxScale = type.maxScale;
        this.repaint();
    }

    public void resetScales(float minScale, float maxScale) {
        ScalableImagePanel.checkForValidScale(minScale, maxScale);
        this.minScale = minScale;
        this.maxScale = maxScale;
        this.repaint();
    }

    public float getMinScale() {
        return this.minScale;
    }

    public void setMinScale(float minScale) {
        this.resetScales(minScale, this.maxScale);
    }

    public float getMaxScale() {
        return this.maxScale;
    }

    public void setMaxScale(float maxScale) {
        this.resetScales(this.minScale, maxScale);
    }

    public static enum ScaleType {
        FIT_TO_PANEL(0.0f, Float.MAX_VALUE),
        REDUCE_ONLY(0.0f, 1.0f),
        ENLARGE_ONLY(1.0f, Float.MAX_VALUE),
        DON_NOT_SCALE(1.0f, 1.0f);

        private float minScale;
        private float maxScale;

        private ScaleType(float minScale, float maxScale) {
            this.minScale = minScale;
            this.maxScale = maxScale;
        }
    }
}

