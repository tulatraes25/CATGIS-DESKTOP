/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.sldeditor.property;

import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.styling.TextSymbolizer;

public abstract class LineToLabelEndingAnchorTypeEditor
extends JPanel
implements SLDEditor {
    private static final long serialVersionUID = 1L;

    public abstract void setLineToLabelEndingAnchorType(TextSymbolizer.LineToLabelEndingAnchorOptions var1);

    public abstract TextSymbolizer.LineToLabelEndingAnchorOptions getLineToLabelEndingAnchorType();
}

