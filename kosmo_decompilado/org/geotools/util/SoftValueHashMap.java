/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.geotools.resources.i18n.Errors
 *  org.geotools.util.MapEntry
 *  org.geotools.util.WeakCollectionCleaner
 */
package org.geotools.util;

import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.log4j.Logger;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.MapEntry;
import org.geotools.util.WeakCollectionCleaner;

public class SoftValueHashMap<K, V>
extends AbstractMap<K, V> {
    private static final Logger LOGGER = Logger.getLogger(SoftValueHashMap.class);
    private static final int DEFAULT_HARD_REFERENCE_COUNT = 20;
    private final Map<K, Object> hash = new HashMap<K, Object>();
    private final LinkedList<K> hardCache = new LinkedList();
    private final int hardReferencesCount;
    private transient Set<Map.Entry<K, V>> entries;
    private final ValueCleaner cleaner;

    public SoftValueHashMap() {
        this.cleaner = null;
        this.hardReferencesCount = 20;
    }

    public SoftValueHashMap(int hardReferencesCount) {
        this.cleaner = null;
        this.hardReferencesCount = hardReferencesCount;
    }

    public SoftValueHashMap(int hardReferencesCount, ValueCleaner cleaner) {
        this.cleaner = cleaner;
        this.hardReferencesCount = hardReferencesCount;
    }

    private static void ensureNotNull(Object value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException(Errors.format((int)105, (Object)"value"));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final boolean isValid() {
        int count = 0;
        int size = 0;
        Map<K, Object> map = this.hash;
        synchronized (map) {
            for (Map.Entry<K, Object> entry : this.hash.entrySet()) {
                if (entry.getValue() instanceof Reference) {
                    ++count;
                } else assert (this.hardCache.contains(entry.getKey()));
                ++size;
            }
            assert (size == this.hash.size());
            assert (this.hardCache.size() == Math.min(size, this.hardReferencesCount));
        }
        return count == Math.max(size - this.hardReferencesCount, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int size() {
        Map<K, Object> map = this.hash;
        synchronized (map) {
            return this.hash.size();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean containsKey(Object key) {
        Map<K, Object> map = this.hash;
        synchronized (map) {
            return this.hash.containsKey(key);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean containsValue(Object value) {
        SoftValueHashMap.ensureNotNull(value);
        Map<K, Object> map = this.hash;
        synchronized (map) {
            return super.containsValue(value);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public V get(Object key) {
        Map<K, Object> map = this.hash;
        synchronized (map) {
            Object value = this.hash.get(key);
            if (value instanceof Reference) {
                if ((value = ((Reference)value).getAndClear()) != null) {
                    Object k = key;
                    this.hash.put(k, value);
                    this.retainStrongly(k);
                } else {
                    this.hash.remove(key);
                }
            }
            Object v = value;
            return (V)v;
        }
    }

    private void retainStrongly(K key) {
        assert (Thread.holdsLock(this.hash));
        assert (!this.hardCache.contains(key)) : key;
        this.hardCache.addFirst(key);
        if (this.hardCache.size() > this.hardReferencesCount) {
            K toRemove = this.hardCache.removeLast();
            Object value = this.hash.get(toRemove);
            assert (value != null && !(value instanceof Reference)) : toRemove;
            Object v = value;
            this.hash.put(toRemove, new Reference<K, Object>(this.hash, toRemove, v));
            assert (this.hardCache.size() == this.hardReferencesCount);
        }
        assert (this.isValid());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public V put(K key, V value) {
        SoftValueHashMap.ensureNotNull(value);
        Map<K, Object> map = this.hash;
        synchronized (map) {
            Object oldValue = this.hash.put(key, value);
            if (oldValue instanceof Reference) {
                oldValue = ((Reference)oldValue).getAndClear();
            } else if (oldValue != null && !this.hardCache.remove(key)) {
                throw new AssertionError(key);
            }
            this.retainStrongly(key);
            Object v = oldValue;
            return (V)v;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        Map<K, Object> map2 = this.hash;
        synchronized (map2) {
            super.putAll(map);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public V remove(Object key) {
        Map<K, Object> map = this.hash;
        synchronized (map) {
            Object oldValue = this.hash.remove(key);
            if (oldValue instanceof Reference) {
                oldValue = ((Reference)oldValue).getAndClear();
            } else if (oldValue != null && !this.hardCache.remove(key)) {
                throw new AssertionError(key);
            }
            Object v = oldValue;
            return (V)v;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void clear() {
        Map<K, Object> map = this.hash;
        synchronized (map) {
            for (Object value : this.hash.values()) {
                if (!(value instanceof Reference)) continue;
                ((Reference)value).getAndClear();
            }
            this.hash.clear();
            this.hardCache.clear();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        Map<K, Object> map = this.hash;
        synchronized (map) {
            if (this.entries == null) {
                this.entries = new Entries();
            }
            return this.entries;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean equals(Object object) {
        Map<K, Object> map = this.hash;
        synchronized (map) {
            return super.equals(object);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int hashCode() {
        Map<K, Object> map = this.hash;
        synchronized (map) {
            return super.hashCode();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String toString() {
        Map<K, Object> map = this.hash;
        synchronized (map) {
            return super.toString();
        }
    }

    private final class Entries
    extends AbstractSet<Map.Entry<K, V>> {
        private Entries() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            Map map = SoftValueHashMap.this.hash;
            synchronized (map) {
                return new Iter(SoftValueHashMap.this.hash);
            }
        }

        @Override
        public int size() {
            return SoftValueHashMap.this.size();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean contains(Object entry) {
            Map map = SoftValueHashMap.this.hash;
            synchronized (map) {
                return super.contains(entry);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public Object[] toArray() {
            Map map = SoftValueHashMap.this.hash;
            synchronized (map) {
                return super.toArray();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public <T> T[] toArray(T[] array) {
            Map map = SoftValueHashMap.this.hash;
            synchronized (map) {
                return super.toArray(array);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean remove(Object entry) {
            Map map = SoftValueHashMap.this.hash;
            synchronized (map) {
                return super.remove(entry);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean containsAll(Collection<?> collection) {
            Map map = SoftValueHashMap.this.hash;
            synchronized (map) {
                return super.containsAll(collection);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean addAll(Collection<? extends Map.Entry<K, V>> collection) {
            Map map = SoftValueHashMap.this.hash;
            synchronized (map) {
                return super.addAll(collection);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean removeAll(Collection<?> collection) {
            Map map = SoftValueHashMap.this.hash;
            synchronized (map) {
                return super.removeAll(collection);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean retainAll(Collection<?> collection) {
            Map map = SoftValueHashMap.this.hash;
            synchronized (map) {
                return super.retainAll(collection);
            }
        }

        @Override
        public void clear() {
            SoftValueHashMap.this.clear();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public String toString() {
            Map map = SoftValueHashMap.this.hash;
            synchronized (map) {
                return super.toString();
            }
        }
    }

    private static final class Iter<K, V>
    implements Iterator<Map.Entry<K, V>> {
        private final Map<K, Object> hash;
        private final Iterator<Map.Entry<K, Object>> iterator;
        private transient Map.Entry<K, V> entry;

        Iter(Map<K, Object> hash) {
            this.hash = hash;
            this.iterator = hash.entrySet().iterator();
        }

        /*
         * Unable to fully structure code
         */
        private boolean findNext() {
            if (Iter.$assertionsDisabled || Thread.holdsLock(this.hash)) ** GOTO lbl12
            throw new AssertionError();
lbl-1000:
            // 1 sources

            {
                candidate = this.iterator.next();
                value = candidate.getValue();
                if (value instanceof Reference) {
                    value = ((Reference)value).get();
                    this.entry = new MapEntry(candidate.getKey(), value);
                    return true;
                }
                if (value == null) continue;
                this.entry = candidate;
                return true;
lbl12:
                // 2 sources

                ** while (this.iterator.hasNext())
            }
lbl13:
            // 1 sources

            return false;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean hasNext() {
            Map<K, Object> map = this.hash;
            synchronized (map) {
                return this.entry != null || this.findNext();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public Map.Entry<K, V> next() {
            Map<K, Object> map = this.hash;
            synchronized (map) {
                if (this.entry == null && !this.findNext()) {
                    throw new NoSuchElementException();
                }
                Map.Entry<K, V> next = this.entry;
                this.entry = null;
                return next;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void remove() {
            Map<K, Object> map = this.hash;
            synchronized (map) {
                this.iterator.remove();
            }
        }
    }

    private static final class Reference<K, V>
    extends SoftReference<V> {
        private final Map<K, Object> hash;
        private final K key;
        private ValueCleaner cleaner;

        Reference(Map<K, Object> hash, K key, V value) {
            super(value, WeakCollectionCleaner.DEFAULT.referenceQueue);
            this.hash = hash;
            this.key = key;
        }

        final Object getAndClear() {
            assert (Thread.holdsLock(this.hash));
            Object value = this.get();
            super.clear();
            return value;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void clear() {
            Object value;
            if (this.cleaner != null && (value = this.get()) != null) {
                try {
                    this.cleaner.clean(value);
                }
                catch (Throwable t) {
                    LOGGER.error((Object)"Exception occurred while cleaning soft referenced object", t);
                }
            }
            super.clear();
            Map<K, Object> map = this.hash;
            synchronized (map) {
                Object old = this.hash.remove(this.key);
                if (old != this && old != null) {
                    this.hash.put(this.key, old);
                }
            }
        }
    }

    public static interface ValueCleaner {
        public void clean(Object var1);
    }
}

