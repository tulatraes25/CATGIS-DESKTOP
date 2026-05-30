/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management;

import java.util.EventListener;

public interface PagesNavigationListener
extends EventListener {
    public static final int EVENT_NEXT_RECORD = 0;
    public static final int EVENT_PREVIOUS_RECORD = 1;
    public static final int EVENT_NEXT_PAGE = 2;
    public static final int EVENT_PREVIOUS_PAGE = 3;
    public static final int EVENT_FIRST_RECORD = 4;
    public static final int EVENT_LAST_RECORD = 5;
    public static final int EVENT_FIRST_PAGE = 6;
    public static final int EVENT_LAST_PAGE = 7;
    public static final int EVENT_NUMBER_OF_RECORDS_PER_PAGE = 8;
    public static final int EVENT_RECORD_ADDED = 9;
    public static final int EVENT_RECORD_REMOVED = 10;

    public void pagesNavigationEventFired(int var1);
}

