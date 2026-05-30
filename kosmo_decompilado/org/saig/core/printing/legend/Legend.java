/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.printing.legend;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import org.saig.core.printing.legend.Theme;
import org.saig.jump.lang.I18N;

public class Legend {
    public static final String DEFAULT_LEGEND_NAME = I18N.getString("org.saig.core.printing.legend.Legend.Unnamed");
    private static final String DEFAULT_FONT_NAME = "Arial";
    private static final int DEFAULT_FONT_SIZE = 11;
    private static final String DEFAULT_FONT_STYLE = "PLAIN";
    private String name = DEFAULT_LEGEND_NAME;
    private String nameFont = "Arial";
    private int nameSize = 11;
    private String nameStyle = "PLAIN";
    private String title;
    private String titleFont = "Arial";
    private int titleSize = 11;
    private String titleStyle = "PLAIN";
    private int columns = 1;
    private List<Theme> themes = new ArrayList<Theme>();

    public String toString() {
        return this.name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Theme> getThemes() {
        return this.themes;
    }

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
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

    public String getTitleFont() {
        return this.titleFont;
    }

    public void setTitleFont(String titleFont) {
        this.titleFont = titleFont;
    }

    public int getTitleSize() {
        return this.titleSize;
    }

    public void setTitleSize(int titleSize) {
        this.titleSize = titleSize;
    }

    public String getTitleStyle() {
        return this.titleStyle;
    }

    public void setTitleStyle(String titleStyle) {
        this.titleStyle = titleStyle;
    }

    public int getColumns() {
        return this.columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public static int getStyleCode(String code) {
        if (code.equals(DEFAULT_FONT_STYLE)) {
            return 0;
        }
        if (code.equals("BOLD")) {
            return 1;
        }
        if (code.equals("ITALIC")) {
            return 2;
        }
        if (code.equals("ITALIC_BOLD")) {
            return 3;
        }
        return -1;
    }

    public static String getStyleName(int style) {
        switch (style) {
            case 0: {
                return DEFAULT_FONT_STYLE;
            }
            case 1: {
                return "BOLD";
            }
            case 2: {
                return "ITALIC";
            }
            case 3: {
                return "ITALIC_BOLD";
            }
        }
        return null;
    }

    public Font getNameAwtFont() {
        if (this.getNameFont() == null || this.getNameFont().equals("")) {
            return null;
        }
        return new Font(this.getNameFont(), Legend.getStyleCode(this.getNameStyle()), this.getNameSize());
    }

    public Font getTitleAwtFont() {
        if (this.getTitleFont() == null || this.getTitleFont().equals("")) {
            return null;
        }
        return new Font(this.getTitleFont(), Legend.getStyleCode(this.getTitleStyle()), this.getTitleSize());
    }
}

