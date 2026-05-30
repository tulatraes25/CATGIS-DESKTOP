/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management;

import java.util.EventListener;

public interface EntityTableListener
extends EventListener {
    public void lastRowReachedEventFired();

    public void orderByColumnEventFired(int var1, boolean var2);
}

