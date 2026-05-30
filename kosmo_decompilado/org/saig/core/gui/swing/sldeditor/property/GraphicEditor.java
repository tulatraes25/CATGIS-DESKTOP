/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property;

import javax.swing.JComponent;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.styling.Graphic;

public abstract class GraphicEditor
extends JComponent
implements SLDEditor {
    private static final long serialVersionUID = 1L;

    public abstract void setGraphic(Graphic var1);

    public abstract Graphic getGraphic();

    public abstract void setUnitsOfMeasurement(String var1);

    public abstract String getUnitsOfMeasurement();
}

