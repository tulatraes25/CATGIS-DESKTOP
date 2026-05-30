/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.utils;

import com.vividsolutions.jump.workbench.model.Category;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CategoryUtil {
    public static List<Category> orderByIndex(Collection<Category> categories) {
        HashMap<Integer, Category> indexes = new HashMap<Integer, Category>();
        ArrayList<Category> solucion = new ArrayList<Category>();
        for (Category element : categories) {
            indexes.put(new Integer(CategoryUtil.indexOfLayerable(element)), element);
        }
        ArrayList keys = new ArrayList(indexes.keySet());
        Collections.sort(keys);
        for (Integer element : keys) {
            solucion.add((Category)indexes.get(element));
        }
        return solucion;
    }

    public static int indexOfLayerable(Category cat) {
        return cat.getLayerManager().getCategories().indexOf(cat);
    }
}

