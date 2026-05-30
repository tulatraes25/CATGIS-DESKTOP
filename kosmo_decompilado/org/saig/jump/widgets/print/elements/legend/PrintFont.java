/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements.legend;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class PrintFont {
    private Font font;
    private Color color;
    private boolean isUnderline;
    private Border border;
    private Color backgroundColor;
    private boolean isOpaque;
    private int borderThickness;
    private Color borderColor;

    public PrintFont() {
        this.font = new Font("Arial", 0, 12);
        this.color = Color.BLACK;
        this.isUnderline = false;
        this.isOpaque = false;
    }

    public PrintFont(Font f, Color c, boolean underline) {
        this.font = f;
        this.color = c;
        this.isUnderline = underline;
        this.isOpaque = false;
    }

    public PrintFont(Font f, Color c, Color bc, Border border, boolean underline, boolean opaque) {
        this.font = f;
        this.color = c;
        this.isUnderline = underline;
        this.border = border;
        this.backgroundColor = bc;
        this.isOpaque = opaque;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Font getFont() {
        return this.font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public boolean isUnderline() {
        return this.isUnderline;
    }

    public void setUnderline(boolean isUnderline) {
        this.isUnderline = isUnderline;
    }

    public Border getBorder() {
        return this.border;
    }

    public void setBorder(Border border) {
        this.border = border;
        if (border != null) {
            this.borderThickness = ((LineBorder)this.getBorder()).getThickness();
            this.borderColor = ((LineBorder)this.getBorder()).getLineColor();
        }
    }

    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean isOpaque() {
        return this.isOpaque;
    }

    public void setOpaque(boolean isOpaque) {
        this.isOpaque = isOpaque;
    }

    public String getFontName() {
        return this.font.getName();
    }

    public void setFontName(String fn) {
        Font nf;
        int style = this.font.getStyle();
        int size = this.font.getSize();
        this.font = nf = new Font(fn, style, size);
    }

    public int getFontStyle() {
        return this.font.getStyle();
    }

    public void setFontStyle(int st) {
        Font nf;
        String name = this.font.getName();
        int size = this.font.getSize();
        this.font = nf = new Font(name, st, size);
    }

    public int getFontSize() {
        return this.font.getSize();
    }

    public void setFontSize(int s) {
        Font nf;
        String name = this.font.getName();
        int style = this.font.getStyle();
        this.font = nf = new Font(name, style, s);
    }

    public Color getBorderColor() {
        return this.borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        this.border = BorderFactory.createLineBorder(borderColor, this.borderThickness);
    }

    public int getBorderThickness() {
        return this.borderThickness;
    }

    public void setBorderThickness(int borderThickness) {
        this.borderThickness = borderThickness;
        this.border = BorderFactory.createLineBorder(this.borderColor, borderThickness);
    }
}

