/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.images;

import javax.swing.ImageIcon;

public class PrintIconLoader {
    public static ImageIcon icon(String filename) {
        return new ImageIcon(PrintIconLoader.class.getResource(filename));
    }
}

