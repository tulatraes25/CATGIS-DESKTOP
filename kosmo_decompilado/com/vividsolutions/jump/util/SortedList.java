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
import org.saig.core.util.I18NUnsupportedOperationException;

public class SortedList<T extends Comparable<T>>
implements List<T> {
    private List<T> list;

    public SortedList() {
        this(new ArrayList());
    }

    public SortedList(List<T> list) {
        this.list = list;
    }

    private void sort() {
        Collections.sort(this.list);
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
        return this.list.iterator();
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
        try {
            boolean bl = this.list.add(o);
            return bl;
        }
        finally {
            this.sort();
        }
    }

    @Override
    public boolean remove(Object o) {
        try {
            boolean bl = this.list.remove(o);
            return bl;
        }
        finally {
            this.sort();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        try {
            boolean bl = this.list.addAll(c);
            return bl;
        }
        finally {
            this.sort();
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        try {
            boolean bl = this.list.addAll(index, c);
            return bl;
        }
        finally {
            this.sort();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        try {
            boolean bl = this.list.removeAll(c);
            return bl;
        }
        finally {
            this.sort();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        try {
            boolean bl = this.list.retainAll(c);
            return bl;
        }
        finally {
            this.sort();
        }
    }

    @Override
    public void clear() {
        this.list.clear();
    }

    @Override
    public T get(int index) {
        return (T)((Comparable)this.list.get(index));
    }

    @Override
    public T set(int index, T element) {
        try {
            Comparable comparable = (Comparable)this.list.set(index, element);
            return (T)comparable;
        }
        finally {
            this.sort();
        }
    }

    @Override
    public void add(int index, T element) {
        try {
            this.list.add(index, element);
        }
        finally {
            this.sort();
        }
    }

    @Override
    public T remove(int index) {
        try {
            Comparable comparable = (Comparable)this.list.remove(index);
            return (T)comparable;
        }
        finally {
            this.sort();
        }
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
        return this.list.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return this.list.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new I18NUnsupportedOperationException();
    }
}

