/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property;

import javax.swing.JComponent;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.sldeditor.SLDEditor;

public abstract class FilterEditor
extends JComponent
implements SLDEditor {
    private static final long serialVersionUID = 1L;

    public abstract Filter getFilter();

    public abstract String getFormattedErrorMessage();

    public abstract void setFilter(Filter var1);
}

