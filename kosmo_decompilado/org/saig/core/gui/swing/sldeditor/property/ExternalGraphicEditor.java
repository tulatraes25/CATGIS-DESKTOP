/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property;

import javax.swing.JComponent;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.styling.ExternalGraphic;

public abstract class ExternalGraphicEditor
extends JComponent
implements SLDEditor {
    private static final long serialVersionUID = 1L;

    public abstract void setExternalGraphic(ExternalGraphic var1);

    public abstract ExternalGraphic getExternalGraphic();
}

