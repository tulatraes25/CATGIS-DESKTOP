/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.utils;

import java.util.List;

public class CollectionUtils {
    public static <T> boolean addArrayToList(List<T> toAddTo, T[] arrayToBeAdded) {
        int i = 0;
        while (i < arrayToBeAdded.length) {
            toAddTo.add(arrayToBeAdded[i]);
            ++i;
        }
        return true;
    }
}

