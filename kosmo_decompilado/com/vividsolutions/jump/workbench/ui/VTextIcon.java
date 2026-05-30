/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;

public class VTextIcon
implements Icon,
PropertyChangeListener {
    static final int POSITION_NORMAL = 0;
    static final int POSITION_TOP_RIGHT = 1;
    static final int POSITION_FAR_TOP_RIGHT = 2;
    public static final int ROTATE_DEFAULT = 0;
    public static final int ROTATE_NONE = 1;
    public static final int ROTATE_LEFT = 2;
    public static final int ROTATE_RIGHT = 4;
    static final String sDrawsInTopRight = "\u3041\u3043\u3045\u3047\u3049\u3063\u3083\u3085\u3087\u308e\u30a1\u30a3\u30a5\u30a7\u30a9\u30c3\u30e3\u30e5\u30e7\u30ee\u30f5\u30f6";
    static final String sDrawsInFarTopRight = "\u3001\u3002";
    static final int DEFAULT_CJK = 1;
    static final int LEGAL_ROMAN = 7;
    static final int DEFAULT_ROMAN = 4;
    static final int LEGAL_MUST_ROTATE = 6;
    static final int DEFAULT_MUST_ROTATE = 2;
    static final double NINETY_DEGREES = Math.toRadians(90.0);
    static final int kBufferSpace = 5;
    String fLabel;
    String[] fCharStrings;
    int[] fCharWidths;
    int[] fPosition;
    int fWidth;
    int fHeight;
    int fCharHeight;
    int fDescent;
    int fRotation;
    Component fComponent;

    public VTextIcon(Component component, String label) {
        this(component, label, 0);
    }

    public VTextIcon(Component component, String label, int rotateHint) {
        this.fComponent = component;
        this.fLabel = label;
        this.fRotation = VTextIcon.verifyRotation(label, rotateHint);
        this.calcDimensions();
        this.fComponent.addPropertyChangeListener(this);
    }

    public void setLabel(String label) {
        this.fLabel = label;
        this.fRotation = VTextIcon.verifyRotation(label, this.fRotation);
        this.recalcDimensions();
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if ("font".equals(prop)) {
            this.recalcDimensions();
        }
    }

    void recalcDimensions() {
        int wOld = this.getIconWidth();
        int hOld = this.getIconHeight();
        this.calcDimensions();
        if (wOld != this.getIconWidth() || hOld != this.getIconHeight()) {
            this.fComponent.invalidate();
        }
    }

    void calcDimensions() {
        FontMetrics fm = this.fComponent.getFontMetrics(this.fComponent.getFont());
        this.fCharHeight = fm.getAscent() + fm.getDescent();
        this.fDescent = fm.getDescent();
        if (this.fRotation == 1) {
            int len = this.fLabel.length();
            char[] data = new char[len];
            this.fLabel.getChars(0, len, data, 0);
            this.fWidth = 0;
            this.fCharStrings = new String[len];
            this.fCharWidths = new int[len];
            this.fPosition = new int[len];
            int i = 0;
            while (i < len) {
                char ch = data[i];
                this.fCharWidths[i] = fm.charWidth(ch);
                if (this.fCharWidths[i] > this.fWidth) {
                    this.fWidth = this.fCharWidths[i];
                }
                this.fCharStrings[i] = new String(data, i, 1);
                this.fPosition[i] = sDrawsInTopRight.indexOf(ch) >= 0 ? 1 : (sDrawsInFarTopRight.indexOf(ch) >= 0 ? 2 : 0);
                ++i;
            }
            this.fHeight = this.fCharHeight * len + this.fDescent;
        } else {
            this.fWidth = this.fCharHeight;
            this.fHeight = fm.stringWidth(this.fLabel) + 10;
        }
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(c.getForeground());
        g.setFont(c.getFont());
        if (this.fRotation == 1) {
            int yPos = y + this.fCharHeight;
            int i = 0;
            while (i < this.fCharStrings.length) {
                switch (this.fPosition[i]) {
                    case 0: {
                        g.drawString(this.fCharStrings[i], x + (this.fWidth - this.fCharWidths[i]) / 2, yPos);
                        break;
                    }
                    case 1: {
                        int tweak = this.fCharHeight / 3;
                        g.drawString(this.fCharStrings[i], x + tweak / 2, yPos - tweak);
                        break;
                    }
                    case 2: {
                        int tweak = this.fCharHeight - this.fCharHeight / 3;
                        g.drawString(this.fCharStrings[i], x + tweak / 2, yPos - tweak);
                    }
                }
                yPos += this.fCharHeight;
                ++i;
            }
        } else if (this.fRotation == 2) {
            g.translate(x + this.fWidth, y + this.fHeight);
            ((Graphics2D)g).rotate(-NINETY_DEGREES);
            g.drawString(this.fLabel, 5, -this.fDescent);
            ((Graphics2D)g).rotate(NINETY_DEGREES);
            g.translate(-(x + this.fWidth), -(y + this.fHeight));
        } else if (this.fRotation == 4) {
            g.translate(x, y);
            ((Graphics2D)g).rotate(NINETY_DEGREES);
            g.drawString(this.fLabel, 5, -this.fDescent);
            ((Graphics2D)g).rotate(-NINETY_DEGREES);
            g.translate(-x, -y);
        }
    }

    @Override
    public int getIconWidth() {
        return this.fWidth;
    }

    @Override
    public int getIconHeight() {
        return this.fHeight;
    }

    public static int verifyRotation(String label, int rotateHint) {
        int legal;
        boolean hasCJK = false;
        boolean hasMustRotate = false;
        int len = label.length();
        char[] data = new char[len];
        label.getChars(0, len, data, 0);
        int i = 0;
        while (i < len) {
            char ch = data[i];
            if (ch >= '\u4e00' && ch <= '\u9fff' || ch >= '\u3400' && ch <= '\u4dff' || ch >= '\uf900' && ch <= '\ufaff' || ch >= '\u3040' && ch <= '\u309f' || ch >= '\u30a0' && ch <= '\u30ff') {
                hasCJK = true;
            }
            if (ch >= '\u0590' && ch <= '\u05ff' || ch >= '\u0600' && ch <= '\u06ff' || ch >= '\u0700' && ch <= '\u074f') {
                hasMustRotate = true;
            }
            ++i;
        }
        if (hasCJK) {
            return 1;
        }
        int n = legal = hasMustRotate ? 6 : 7;
        if ((rotateHint & legal) > 0) {
            return rotateHint;
        }
        return hasMustRotate ? 2 : 4;
    }
}

