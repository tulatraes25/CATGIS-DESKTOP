/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import java.util.Hashtable;

public class DriverPanelCache {
    public static final String DRIVER_CACHE_KEY = "DRIVER";
    public static final String FILE_CACHE_KEY = "FILE";
    private Hashtable<String, Object> map = new Hashtable();

    public Object get(String cacheKey) {
        return this.map.get(cacheKey);
    }

    public void put(String cacheKey, Object cachedValue) {
        this.map.put(cacheKey, cachedValue);
    }

    public void addAll(DriverPanelCache otherCache) {
        for (String otherCacheKey : otherCache.map.keySet()) {
            this.map.put(otherCacheKey, otherCache.get(otherCacheKey));
        }
    }
}

