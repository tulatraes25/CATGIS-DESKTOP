/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property;

import javax.swing.JComponent;
import org.saig.core.gui.swing.sldeditor.SLDEditor;

public abstract class ScaleEditor
extends JComponent
implements SLDEditor {
    private static final long serialVersionUID = 1L;

    public abstract void setScaleDenominator(double var1);

    public abstract double getScaleDenominator();
}

