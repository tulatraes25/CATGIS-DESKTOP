/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import org.saig.core.filter.Filter;
import org.xml.sax.ContentHandler;

public interface FilterHandler
extends ContentHandler {
    public void filter(Filter var1);
}

