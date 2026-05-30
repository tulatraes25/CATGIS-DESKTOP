/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property;

import java.util.Map;
import javax.swing.JComponent;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.styling.LabelPlacement;

public abstract class LabelPlacementEditor
extends JComponent
implements SLDEditor {
    private static final long serialVersionUID = 1L;

    public abstract void setLabelPlacement(LabelPlacement var1);

    public abstract LabelPlacement getLabelPlacement();

    public abstract boolean isSelected();

    public abstract void setSelected(boolean var1);

    public abstract Map<String, String> getLabelPlacementOptions();

    public abstract void setLabelPlacementOptions(Map<String, String> var1);
}

