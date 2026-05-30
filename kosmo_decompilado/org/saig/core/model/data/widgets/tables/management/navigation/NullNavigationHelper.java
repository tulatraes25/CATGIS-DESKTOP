/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.navigation;

import java.util.ArrayList;
import java.util.List;
import org.saig.core.model.data.widgets.tables.management.navigation.AbstractNavigationHelper;

public class NullNavigationHelper
extends AbstractNavigationHelper {
    @Override
    public List<Object> getElements(int startIndex, int elements) {
        return new ArrayList<Object>();
    }

    @Override
    public int getNumElements() {
        return 0;
    }
}

