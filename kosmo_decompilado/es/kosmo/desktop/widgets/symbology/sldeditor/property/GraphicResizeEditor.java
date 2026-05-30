/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.sldeditor.property;

import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.styling.TextSymbolizer;

public abstract class GraphicResizeEditor
extends JPanel
implements SLDEditor {
    private static final long serialVersionUID = 1L;

    public abstract TextSymbolizer.GraphicResize getGraphicResize();

    public abstract void setGraphicResize(TextSymbolizer.GraphicResize var1);
}

