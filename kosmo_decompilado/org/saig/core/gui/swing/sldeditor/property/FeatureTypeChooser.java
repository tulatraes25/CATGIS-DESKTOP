/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property;

import javax.swing.JComponent;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.styling.FeatureTypeStyle;

public abstract class FeatureTypeChooser
extends JComponent
implements SLDEditor {
    private static final long serialVersionUID = 1L;

    public abstract void setFeatureTypeName(String var1);

    public abstract String getFeatureTypeName();

    public abstract void setFeaturesTypesStyle(FeatureTypeStyle[] var1, String var2);
}

