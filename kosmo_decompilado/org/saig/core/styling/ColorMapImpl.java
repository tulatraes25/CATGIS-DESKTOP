/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.resources.Utilities
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import java.util.ArrayList;
import java.util.List;
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;
import org.saig.core.styling.ColorMap;
import org.saig.core.styling.ColorMapEntry;
import org.saig.core.styling.StyleVisitor;

public class ColorMapImpl
implements ColorMap,
Cloneable {
    private List<ColorMapEntry> colorMapEntries = new ArrayList<ColorMapEntry>();
    private int type = 1;

    @Override
    public void addColorMapEntry(ColorMapEntry entry) {
        this.colorMapEntries.add(entry);
    }

    @Override
    public ColorMapEntry[] getColorMapEntries() {
        return this.colorMapEntries.toArray(new ColorMapEntry[0]);
    }

    @Override
    public ColorMapEntry getColorMapEntry(int index) {
        return this.colorMapEntries.get(index);
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public void setType(int type) {
        if (type < 1 || type > 3) {
            throw new IllegalArgumentException();
        }
        this.type = type;
    }

    public Object clone() {
        ColorMapImpl clone = new ColorMapImpl();
        clone.setType(this.getType());
        if (this.colorMapEntries != null) {
            clone.colorMapEntries = new ArrayList<ColorMapEntry>();
            for (ColorMapEntry entry : this.colorMapEntries) {
                clone.addColorMapEntry((ColorMapEntry)((Cloneable)entry).clone());
            }
        }
        return clone;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth instanceof ColorMapImpl) {
            ColorMapImpl other = (ColorMapImpl)oth;
            return this.type == other.type && Utilities.equals(this.colorMapEntries, other.colorMapEntries);
        }
        return false;
    }
}

