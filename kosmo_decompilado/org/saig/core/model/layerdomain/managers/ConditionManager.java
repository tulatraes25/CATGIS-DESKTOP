/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.layerdomain.managers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import org.saig.core.model.layerdomain.conditions.ConditionFactory;

public class ConditionManager {
    private ServiceLoader<ConditionFactory> loader = ServiceLoader.load(ConditionFactory.class);
    private static ConditionManager instance;

    public static ConditionManager getInstance() {
        if (instance == null) {
            instance = new ConditionManager();
        }
        return instance;
    }

    protected ConditionManager() {
    }

    public ConditionFactory getConditionFactoryByTag(String tag) {
        Iterator<ConditionFactory> iterator = this.loader.iterator();
        ConditionFactory factory = null;
        boolean found = false;
        while (iterator.hasNext() && !found) {
            ConditionFactory next = iterator.next();
            if (!next.acceptsTag(tag)) continue;
            factory = next;
            found = true;
        }
        return factory;
    }

    public ConditionFactory getConditionFactoryByName(String name) {
        Iterator<ConditionFactory> iterator = this.loader.iterator();
        ConditionFactory factory = null;
        boolean found = false;
        while (iterator.hasNext() && !found) {
            ConditionFactory next = iterator.next();
            if (!next.getName().equals(name)) continue;
            factory = next;
            found = true;
        }
        return factory;
    }

    public List<String> getConditionFactoryNames() {
        Iterator<ConditionFactory> iterator = this.loader.iterator();
        ArrayList<String> names = new ArrayList<String>();
        while (iterator.hasNext()) {
            ConditionFactory next = iterator.next();
            names.add(next.getName());
        }
        return names;
    }
}

