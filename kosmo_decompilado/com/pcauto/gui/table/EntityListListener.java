/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import com.pcauto.gui.table.EntityListEvent;
import java.util.EventListener;

public interface EntityListListener
extends EventListener {
    public void listChanged(EntityListEvent var1);
}

