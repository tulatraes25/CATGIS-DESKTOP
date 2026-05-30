/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.style;

import org.saig.core.styling.Style;

public interface StyleEditor {
    public Style getStyle();

    public void setStyle(Style var1);

    public boolean canEdit(Style var1);
}

