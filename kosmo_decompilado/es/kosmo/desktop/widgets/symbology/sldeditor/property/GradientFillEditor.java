/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.sldeditor.property;

import es.kosmo.core.styling.Gradient;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.SLDEditor;

public abstract class GradientFillEditor
extends JPanel
implements SLDEditor {
    private static final long serialVersionUID = 1L;

    public abstract Gradient getGradient();

    public abstract void setGradient(Gradient var1);
}

