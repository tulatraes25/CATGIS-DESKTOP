/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets.tables.management.navigation;

import com.vividsolutions.jump.feature.Feature;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.apache.log4j.Logger;
import org.saig.core.model.data.widgets.tables.management.navigation.AbstractNavigationHelper;

public class FeaturesListNavigationHelper
extends AbstractNavigationHelper {
    Logger LOGGER = Logger.getLogger(FeaturesListNavigationHelper.class);
    List<Feature> list;

    public FeaturesListNavigationHelper(List<Feature> list) {
        this.list = list;
        this.type = 3;
        this.setAscendingOrdering(true);
        this.setFilter(null);
        this.setOrderBy(null);
    }

    @Override
    public List<Object> getElements(int startIndex, int nElements) {
        ArrayList<Object> result = new ArrayList<Object>();
        int firstIndex = startIndex - 1;
        int lastIndex = startIndex + nElements - 1;
        if (firstIndex < 0 || firstIndex > this.list.size()) {
            firstIndex = 0;
        }
        if (lastIndex < 0 || lastIndex > this.list.size()) {
            lastIndex = this.list.size();
        }
        if (lastIndex < firstIndex) {
            lastIndex = firstIndex;
        }
        if (this.filter == null) {
            result.addAll(this.list.subList(firstIndex, lastIndex));
        } else {
            ListIterator<Feature> it = this.list.listIterator(firstIndex);
            int processedElements = 0;
            while (processedElements < nElements && it.hasNext()) {
                Feature feat = it.next();
                if (!this.filter.contains(feat)) continue;
                result.add(feat);
                ++processedElements;
            }
        }
        return result;
    }

    @Override
    public int getNumElements() {
        int size = 0;
        if (this.filter == null) {
            size = this.list.size();
        } else {
            ListIterator<Feature> it = this.list.listIterator();
            while (it.hasNext()) {
                Feature feat = it.next();
                if (!this.filter.contains(feat)) continue;
                ++size;
            }
        }
        return size;
    }
}

