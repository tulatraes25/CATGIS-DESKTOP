/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.filter;

import org.apache.log4j.Logger;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.XMLFilterImpl;

public class FilterHandlerImpl
extends XMLFilterImpl
implements ContentHandler,
FilterHandler {
    private static Logger LOGGER = Logger.getLogger(FilterHandlerImpl.class);
    private Filter currentFilter = null;

    @Override
    public void filter(Filter filter) {
        LOGGER.debug((Object)("found filter: " + filter.toString()));
        this.currentFilter = filter;
    }

    public Filter getFilter() {
        return this.currentFilter;
    }
}

