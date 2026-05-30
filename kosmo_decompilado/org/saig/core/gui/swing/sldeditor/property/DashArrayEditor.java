/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property;

import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.SLDEditor;

public abstract class DashArrayEditor
extends JPanel
implements SLDEditor {
    private static final long serialVersionUID = 1L;

    public abstract void setDashArray(float[] var1);

    public abstract float[] getDashArray();
}

