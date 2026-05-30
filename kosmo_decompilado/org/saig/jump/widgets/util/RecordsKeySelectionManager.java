/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import org.saig.core.model.data.Record;
import org.saig.jump.widgets.util.AbstractMultiKeySelectionManager;

public class RecordsKeySelectionManager
extends AbstractMultiKeySelectionManager {
    public RecordsKeySelectionManager(String searchField) {
        super(searchField);
    }

    @Override
    public Object getAttributeValue(Object rec) {
        if (rec instanceof Record) {
            return ((Record)rec).getAttribute(this.searchField);
        }
        return null;
    }
}

