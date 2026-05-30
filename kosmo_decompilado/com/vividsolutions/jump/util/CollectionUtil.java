/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.util;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Block;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class CollectionUtil {
    public static <T> List<List<T>> combinations(List<T> original, int maxCombinationSize) {
        return CollectionUtil.combinations(original, maxCombinationSize, null);
    }

    public static List<Object> list(Object a, Object b) {
        ArrayList<Object> list = new ArrayList<Object>();
        list.add(a);
        list.add(b);
        return list;
    }

    public static <T> Map<T, T> inverse(Map<T, T> map) {
        Map inverse;
        try {
            inverse = (Map)map.getClass().newInstance();
        }
        catch (InstantiationException e) {
            Assert.shouldNeverReachHere((String)e.toString());
            return null;
        }
        catch (IllegalAccessException e) {
            Assert.shouldNeverReachHere((String)e.toString());
            return null;
        }
        for (T key : map.keySet()) {
            T value = map.get(key);
            inverse.put(value, key);
        }
        return inverse;
    }

    public static <T> List<List<T>> combinations(List<T> original, int maxCombinationSize, Object mandatoryItem) {
        ArrayList<List<T>> combinations = new ArrayList<List<T>>();
        int i = 1;
        while (i <= (int)Math.pow(2.0, original.size()) - 1) {
            ArrayList<T> combination = new ArrayList<T>();
            int j = 0;
            while (j < original.size()) {
                if ((i & (int)Math.pow(2.0, j)) > 0) {
                    combination.add(original.get(j));
                }
                ++j;
            }
            if (combination.size() <= maxCombinationSize && (mandatoryItem == null || combination.contains(mandatoryItem))) {
                combinations.add(combination);
            }
            ++i;
        }
        return combinations;
    }

    public static <T> List<List<T>> combinations(List<T> original) {
        return CollectionUtil.combinations(original, original.size(), null);
    }

    public static void removeKeys(Collection<Object> keys, Map<Object, ?> map) {
        for (Object key : keys) {
            map.remove(key);
        }
    }

    public static List[] keysAndCorrespondingValues(Map<?, ?> map) {
        ArrayList keys = new ArrayList(map.keySet());
        ArrayList values = new ArrayList();
        for (Object key : keys) {
            values.add(map.get(key));
        }
        return new List[]{keys, values};
    }

    public static Collection concatenate(Collection collections) {
        ArrayList concatenation = new ArrayList();
        for (Collection collection : collections) {
            concatenation.addAll(collection);
        }
        return concatenation;
    }

    public static Object randomElement(List<?> list) {
        return list.get((int)Math.floor(Math.random() * (double)list.size()));
    }

    public static SortedSet<Integer> reverseSortedSet(int[] ints) {
        TreeSet<Integer> sortedSet = new TreeSet<Integer>(Collections.reverseOrder());
        int i = 0;
        while (i < ints.length) {
            sortedSet.add(new Integer(ints[i]));
            ++i;
        }
        return sortedSet;
    }

    public static List<?> reverse(List<?> list) {
        Collections.reverse(list);
        return list;
    }

    public static <T> Collection<T> stretch(Collection<T> source, Collection<T> destination, int destinationSize) {
        Assert.isTrue((boolean)destination.isEmpty());
        List<Object> originalList = source instanceof List ? (List<Object>)source : new ArrayList<T>(source);
        int finalSize = Math.min(source.size(), destinationSize);
        int i = 0;
        while (i < finalSize) {
            destination.add(originalList.get((int)Math.round((double)(i * originalList.size()) / (double)finalSize)));
            ++i;
        }
        return destination;
    }

    public static Object ifNotIn(Object o, Collection<?> c, Object alternative) {
        return c.contains(o) ? o : alternative;
    }

    public static void setIfNull(int i, List<Object> list, Object value) {
        if (i >= list.size()) {
            CollectionUtil.resize(list, i + 1);
        }
        if (list.get(i) != null) {
            return;
        }
        list.set(i, value);
    }

    public static void resize(List<Object> list, int newSize) {
        if (newSize < list.size()) {
            list.subList(newSize, list.size()).clear();
        } else {
            list.addAll(Collections.nCopies(newSize - list.size(), null));
        }
    }

    public static boolean containsReference(Object[] objects, Object o) {
        return CollectionUtil.indexOf(o, objects) > -1;
    }

    public static int indexOf(Object o, Object[] objects) {
        int i = 0;
        while (i < objects.length) {
            if (objects[i] == o) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    public static <T> Collection<T> removeDuplicates(Collection<T> original) {
        ArrayList<T> result = new ArrayList<T>();
        for (T item : original) {
            if (result.contains(item)) continue;
            result.add(item);
        }
        return result;
    }

    public static void addIfNotNull(Object item, Collection<Object> collection) {
        if (item != null) {
            collection.add(item);
        }
    }

    public static <T> Collection<T> filterByClass(Collection<T> collection, Class<?> c) {
        Iterator<T> i = collection.iterator();
        while (i.hasNext()) {
            T item = i.next();
            if (c.isInstance(item)) continue;
            i.remove();
        }
        return collection;
    }

    public static Map createMap(Object[] alternatingKeysAndValues) {
        return CollectionUtil.createMap(HashMap.class, alternatingKeysAndValues);
    }

    public static Map createMap(Class<?> mapClass, Object[] alternatingKeysAndValues) {
        Map map = null;
        try {
            map = (Map)mapClass.newInstance();
        }
        catch (Exception e) {
            Assert.shouldNeverReachHere((String)e.toString());
        }
        int i = 0;
        while (i < alternatingKeysAndValues.length) {
            map.put(alternatingKeysAndValues[i], alternatingKeysAndValues[i + 1]);
            i += 2;
        }
        return map;
    }

    public static <T> Collection<T> collect(Collection<T> collection, Block block) {
        ArrayList<Object> result = new ArrayList<Object>();
        for (T item : collection) {
            result.add(block.yield(item));
        }
        return result;
    }

    public static <T> Collection<T> select(Collection<T> collection, Block block) {
        ArrayList<T> result = new ArrayList<T>();
        for (T item : collection) {
            if (!Boolean.TRUE.equals(block.yield(item))) continue;
            result.add(item);
        }
        return result;
    }

    public static Object get(Class c, Map map) {
        if (map.keySet().contains(c)) {
            return map.get(c);
        }
        for (Class candidateClass : map.keySet()) {
            if (!candidateClass.isAssignableFrom(c)) continue;
            return map.get(candidateClass);
        }
        return null;
    }

    public static <T> Collection<T> itemsToMoveDown(List<T> items, Collection<T> selectedItems) {
        ArrayList<T> reverseItems = new ArrayList<T>(items);
        Collections.reverse(reverseItems);
        return CollectionUtil.itemsToMoveUp(reverseItems, selectedItems);
    }

    public static <T> Collection<T> itemsToMoveUp(List<T> items, Collection<T> selectedItems) {
        int firstUnselectedIndex = CollectionUtil.firstUnselectedIndex(items, selectedItems);
        if (firstUnselectedIndex == -1) {
            return new ArrayList();
        }
        ArrayList<T> itemsToMoveUp = new ArrayList<T>();
        int i = firstUnselectedIndex;
        while (i < items.size()) {
            T item = items.get(i);
            if (selectedItems.contains(item)) {
                itemsToMoveUp.add(item);
            }
            ++i;
        }
        return itemsToMoveUp;
    }

    protected static <T> int firstUnselectedIndex(List<T> items, Collection<T> selectedItems) {
        int i = 0;
        while (i < items.size()) {
            T item = items.get(i);
            if (!selectedItems.contains(item)) {
                return i;
            }
            ++i;
        }
        return -1;
    }
}

