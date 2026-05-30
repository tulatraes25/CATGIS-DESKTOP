/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.images.norths;

import javax.swing.ImageIcon;

public class PredefinedNorthLoader {
    public static ImageIcon icon(String filename) {
        return new ImageIcon(PredefinedNorthLoader.class.getResource(filename));
    }
}

