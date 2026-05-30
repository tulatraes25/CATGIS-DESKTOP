/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.symbolizer;

import javax.swing.JComponent;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.styling.Symbolizer;

public abstract class SymbolizerEditor
extends JComponent
implements SLDEditor {
    private static final long serialVersionUID = 1L;

    public abstract Symbolizer getSymbolizer();

    public abstract void setSymbolizer(Symbolizer var1);
}

