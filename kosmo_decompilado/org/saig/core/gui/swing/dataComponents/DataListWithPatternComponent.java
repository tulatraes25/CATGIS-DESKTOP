/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.dataComponents;

import java.util.List;
import org.saig.core.gui.swing.dataComponents.DataListComponent;

public interface DataListWithPatternComponent<T>
extends DataListComponent<T> {
    public String applyPattern(Object[] var1);

    public List<T> getRowsByValues(Object[] var1);
}

