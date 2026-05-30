/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.printing.legend;

import java.awt.Font;
import org.saig.core.printing.legend.Legend;
import org.saig.jump.lang.I18N;

public class Theme {
    private static final String DEFAULT_LEGEND_NAME = I18N.getString("org.saig.core.printing.legend.Theme.Unnamed");
    private static final String DEFAULT_FONT_NAME = "Arial";
    private static final int DEFAULT_FONT_SIZE = 11;
    private static final String DEFAULT_FONT_STYLE = "PLAIN";
    private String name = DEFAULT_LEGEND_NAME;
    private String nameFont = "Arial";
    private int nameSize = 11;
    private String nameStyle = "PLAIN";
    private String header;
    private String headerFont = "Arial";
    private int headerSize = 11;
    private String headerStyle = "PLAIN";
    private boolean endColumn;
    private int frontSpaces;

    public int getFrontSpaces() {
        return this.frontSpaces;
    }

    public void setFrontSpaces(int frontSpaces) {
        this.frontSpaces = frontSpaces;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeader() {
        return this.header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String toString() {
        return this.name;
    }

    public String getNameFont() {
        return this.nameFont;
    }

    public void setNameFont(String nameFont) {
        this.nameFont = nameFont;
    }

    public int getNameSize() {
        return this.nameSize;
    }

    public void setNameSize(int nameSize) {
        this.nameSize = nameSize;
    }

    public String getNameStyle() {
        return this.nameStyle;
    }

    public void setNameStyle(String nameStyle) {
        this.nameStyle = nameStyle;
    }

    public String getHeaderFont() {
        return this.headerFont;
    }

    public void setHeaderFont(String headerFont) {
        this.headerFont = headerFont;
    }

    public int getHeaderSize() {
        return this.headerSize;
    }

    public void setHeaderSize(int headerSize) {
        this.headerSize = headerSize;
    }

    public String getHeaderStyle() {
        return this.headerStyle;
    }

    public void setHeaderStyle(String headerStyle) {
        this.headerStyle = headerStyle;
    }

    public boolean isEndColumn() {
        return this.endColumn;
    }

    public void setEndColumn(boolean endColumn) {
        this.endColumn = endColumn;
    }

    public Font getHeaderAwtFont() {
        if (this.getHeaderFont() == null || this.getHeaderFont().equals("")) {
            return null;
        }
        return new Font(this.getHeaderFont(), Legend.getStyleCode(this.getHeaderStyle()), this.getHeaderSize());
    }

    public Font getNameAwtFont() {
        if (this.getNameFont() == null || this.getNameFont().equals("")) {
            return null;
        }
        return new Font(this.getNameFont(), Legend.getStyleCode(this.getNameStyle()), this.getNameSize());
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Theme)) {
            return false;
        }
        Theme theme = (Theme)obj;
        return this.getName().equals(theme.getName());
    }

    public int hashCode() {
        return this.getName().hashCode();
    }
}

