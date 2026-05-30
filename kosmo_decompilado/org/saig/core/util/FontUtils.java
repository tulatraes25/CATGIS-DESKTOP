/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.util;

import java.awt.Font;

public class FontUtils {
    public static Font resizeFont(Font font, int size) {
        String name = font.getName();
        int style = font.getStyle();
        Font newFont = new Font(name, style, size);
        return newFont;
    }
}

