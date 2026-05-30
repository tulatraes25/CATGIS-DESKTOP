/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ImmutableFirstElementList<T>
implements List<T> {
    private List<T> list = new ArrayList<T>();

    public ImmutableFirstElementList(T firstElement) {
        this.list.add(firstElement);
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.list.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(this.list).iterator();
    }

    @Override
    public Object[] toArray() {
        return this.list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.list.toArray(a);
    }

    @Override
    public boolean add(T o) {
        return this.list.add(o);
    }

    @Override
    public boolean remove(Object o) {
        return this.list.subList(1, this.list.size()).remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return this.list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return this.list.addAll(index == 0 ? 1 : index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.list.subList(1, this.list.size()).remove(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.list.subList(1, this.list.size()).retainAll(c);
    }

    @Override
    public void clear() {
        this.list.subList(1, this.list.size()).clear();
    }

    @Override
    public T get(int index) {
        return this.list.get(index);
    }

    @Override
    public T set(int index, T element) {
        if (index == 0) {
            return this.get(0);
        }
        return this.list.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        this.list.add(index == 0 ? 1 : index, element);
    }

    @Override
    public T remove(int index) {
        if (index == 0) {
            return this.get(0);
        }
        return this.list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return this.list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.list.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return Collections.unmodifiableList(this.list).listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return Collections.unmodifiableList(this.list).listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        if (fromIndex > 0) {
            return this.list.subList(fromIndex, toIndex);
        }
        return Collections.unmodifiableList(this.list).subList(fromIndex, toIndex);
    }
}

