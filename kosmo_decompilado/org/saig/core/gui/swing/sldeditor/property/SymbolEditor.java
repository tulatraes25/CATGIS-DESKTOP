/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property;

import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.styling.Symbol;

public abstract class SymbolEditor
extends JPanel
implements SLDEditor {
    private static final long serialVersionUID = 1L;

    public abstract void setSymbol(Symbol var1);

    public abstract Symbol getSymbol();
}

