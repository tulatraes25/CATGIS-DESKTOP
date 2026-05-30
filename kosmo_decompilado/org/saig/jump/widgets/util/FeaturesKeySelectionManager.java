/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import com.vividsolutions.jump.feature.Feature;
import org.saig.jump.widgets.util.AbstractMultiKeySelectionManager;

public class FeaturesKeySelectionManager
extends AbstractMultiKeySelectionManager {
    public FeaturesKeySelectionManager(String searchField) {
        super(searchField);
    }

    @Override
    public Object getAttributeValue(Object feat) {
        if (feat instanceof Feature) {
            return ((Feature)feat).getAttribute(this.searchField);
        }
        return null;
    }
}

