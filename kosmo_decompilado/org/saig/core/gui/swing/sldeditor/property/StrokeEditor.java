/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property;

import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.styling.Stroke;

public abstract class StrokeEditor
extends JPanel
implements SLDEditor {
    private static final long serialVersionUID = 1L;

    public abstract void setStroke(Stroke var1);

    public abstract Stroke getStroke();

    public abstract void setUnitsOfMeasurement(String var1);

    public abstract String getUnitsOfMeasurement();

    public abstract void allowDisable(boolean var1);
}

