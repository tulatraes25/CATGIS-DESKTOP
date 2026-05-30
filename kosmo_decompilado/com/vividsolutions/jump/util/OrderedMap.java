/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.util;

import com.vividsolutions.jump.util.UniqueList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrderedMap<K, V>
implements Map<K, V> {
    private Map<K, V> map;
    private List<K> keyList;

    public OrderedMap(List<K> keyList, Map<K, V> map) {
        this.keyList = keyList;
        this.map = map;
    }

    public OrderedMap() {
        this(new HashMap());
    }

    public OrderedMap(Map<K, V> map) {
        this(new UniqueList(), map);
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.map.get(key);
    }

    @Override
    public V put(K key, V value) {
        this.keyList.add(key);
        return this.map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        this.keyList.remove(key);
        return this.map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> t) {
        this.keyList.addAll(t.keySet());
        this.map.putAll(t);
    }

    @Override
    public void clear() {
        this.keyList.clear();
        this.map.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.map.keySet();
    }

    public List<K> keyList() {
        return this.keyList;
    }

    public List<V> valueList() {
        return (List)this.values();
    }

    @Override
    public Collection<V> values() {
        ArrayList<V> values = new ArrayList<V>();
        for (K key : this.keyList) {
            values.add(this.map.get(key));
        }
        return values;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return this.map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return this.map.equals(o);
    }
}

