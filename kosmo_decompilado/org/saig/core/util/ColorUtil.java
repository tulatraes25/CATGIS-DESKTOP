/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.util;

import java.awt.Color;

public class ColorUtil {
    public static String toHex(Color color) {
        String b;
        String g;
        String r = Integer.toHexString(color.getRed());
        if (r.length() < 2) {
            r = "0" + r;
        }
        if ((g = Integer.toHexString(color.getGreen())).length() < 2) {
            g = "0" + g;
        }
        if ((b = Integer.toHexString(color.getBlue())).length() < 2) {
            b = "0" + b;
        }
        return "#" + r + g + b;
    }

    public static Color fromHex(String hexColor) {
        if (!hexColor.startsWith("#")) {
            return null;
        }
        int r = Integer.decode("0x" + hexColor.substring(1, 3));
        int g = Integer.decode("0x" + hexColor.substring(3, 5));
        int b = Integer.decode("0x" + hexColor.substring(5, 7));
        return new Color(r, g, b);
    }
}

