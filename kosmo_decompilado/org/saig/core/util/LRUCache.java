/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class LRUCache {
    private static final Logger LOGGER = Logger.getLogger(LRUCache.class);
    private Map<Object, Object> cache;

    public LRUCache(final int maxEntries) {
        LOGGER.debug((Object)I18N.getMessage("org.saig.core.util.LRUCache.Initializing-cache-with-{0}-features", new Object[]{Integer.toString(maxEntries)}));
        this.cache = new LinkedHashMap<Object, Object>(maxEntries + 1, 0.75f, true){
            private static final long serialVersionUID = 1L;

            @Override
            public boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
                return this.size() > maxEntries;
            }
        };
        this.cache = Collections.synchronizedMap(this.cache);
    }

    public void add(Object key, Object value) {
        this.cache.put(key, value);
    }

    public Object get(Object key) {
        return this.cache.get(key);
    }

    public void clear() {
        this.cache.clear();
    }
}

