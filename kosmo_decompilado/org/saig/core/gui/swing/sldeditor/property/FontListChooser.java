/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property;

import javax.swing.JComponent;
import org.saig.core.gui.swing.sldeditor.SLDEditor;

public abstract class FontListChooser
extends JComponent
implements SLDEditor {
    private static final long serialVersionUID = 1L;

    public abstract String[] getFontNames();

    public abstract void setFontNames(String[] var1);
}

