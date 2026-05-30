/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.util;

import com.vividsolutions.jts.util.Assert;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class CollectionMap
implements Map {
    private Map map;
    private Class collectionClass = ArrayList.class;

    public CollectionMap(Class mapClass) {
        try {
            this.map = (Map)mapClass.newInstance();
        }
        catch (InstantiationException e) {
            Assert.shouldNeverReachHere();
        }
        catch (IllegalAccessException e) {
            Assert.shouldNeverReachHere();
        }
    }

    public CollectionMap(Class mapClass, Class collectionClass) {
        this.collectionClass = collectionClass;
        try {
            this.map = (Map)mapClass.newInstance();
        }
        catch (InstantiationException e) {
            Assert.shouldNeverReachHere();
        }
        catch (IllegalAccessException e) {
            Assert.shouldNeverReachHere();
        }
    }

    public CollectionMap() {
        this(HashMap.class);
    }

    private Collection getItemsInternal(Object key) {
        Collection collection = (Collection)this.map.get(key);
        if (collection == null) {
            try {
                collection = (Collection)this.collectionClass.newInstance();
            }
            catch (InstantiationException e) {
                Assert.shouldNeverReachHere();
            }
            catch (IllegalAccessException e) {
                Assert.shouldNeverReachHere();
            }
            this.map.put(key, collection);
        }
        return collection;
    }

    public boolean hasItems(Object key) {
        Collection collection = (Collection)this.map.get(key);
        return collection != null && !collection.isEmpty();
    }

    public void addItem(Object key, Object item) {
        this.getItemsInternal(key).add(item);
    }

    public void removeItem(Object key, Object item) {
        this.getItemsInternal(key).remove(item);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    public void addItems(Object key, Collection items) {
        Iterator i = items.iterator();
        while (i.hasNext()) {
            this.addItem(key, i.next());
        }
    }

    public void addItems(CollectionMap other) {
        for (Object key : other.keySet()) {
            this.addItems(key, other.getItems(key));
        }
    }

    public Collection values() {
        return this.map.values();
    }

    public Set keySet() {
        return this.map.keySet();
    }

    @Override
    public int size() {
        return this.map.size();
    }

    public Object get(Object key) {
        return this.getItems(key);
    }

    public Collection getItems(Object key) {
        return Collections.unmodifiableCollection(this.getItemsInternal(key));
    }

    public Object remove(Object key) {
        return this.map.remove(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    public Set entrySet() {
        return this.map.entrySet();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public Object put(Object key, Object value) {
        Assert.isTrue((boolean)(value instanceof Collection));
        return this.map.put(key, value);
    }

    public void putAll(Map map) {
        for (Object key : map.keySet()) {
            this.put(key, map.get(key));
        }
    }

    public void removeItems(Object key, Collection items) {
        this.getItemsInternal(key).removeAll(items);
    }

    public Map getMap() {
        return this.map;
    }
}

