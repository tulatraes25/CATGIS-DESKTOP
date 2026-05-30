/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property;

import org.saig.core.gui.swing.sldeditor.property.ExpressionEditor;

public abstract class FixedOrByAttributeExpressionEditor
extends ExpressionEditor {
    private static final long serialVersionUID = 1L;

    public abstract String getUnitsOfMeasurement();

    public abstract void setUnitsOfMeasurement(String var1);
}

