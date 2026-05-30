/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.styling.ColorMapEntry;
import org.saig.core.styling.StyleVisitor;

public interface ColorMap {
    public static final int TYPE_RAMP = 1;
    public static final int TYPE_INTERVALS = 2;
    public static final int TYPE_VALUES = 3;

    public void addColorMapEntry(ColorMapEntry var1);

    public ColorMapEntry[] getColorMapEntries();

    public ColorMapEntry getColorMapEntry(int var1);

    public int getType();

    public void setType(int var1);

    public void accept(StyleVisitor var1);
}

