/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.layerdomain.managers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import org.saig.core.model.layerdomain.actions.ActionFactory;

public class ActionManager {
    private ServiceLoader<ActionFactory> loader = ServiceLoader.load(ActionFactory.class);
    private static ActionManager instance;

    public static ActionManager getInstance() {
        if (instance == null) {
            instance = new ActionManager();
        }
        return instance;
    }

    protected ActionManager() {
    }

    public ActionFactory getActionFactoryByTag(String tag) {
        Iterator<ActionFactory> iterator = this.loader.iterator();
        ActionFactory factory = null;
        boolean found = false;
        while (iterator.hasNext() && !found) {
            ActionFactory next = iterator.next();
            if (!next.acceptsTag(tag)) continue;
            factory = next;
            found = true;
        }
        return factory;
    }

    public ActionFactory getActionFactoryByName(String name) {
        Iterator<ActionFactory> iterator = this.loader.iterator();
        ActionFactory factory = null;
        boolean found = false;
        while (iterator.hasNext() && !found) {
            ActionFactory next = iterator.next();
            if (!next.getName().equals(name)) continue;
            factory = next;
            found = true;
        }
        return factory;
    }

    public List<String> getActionFactoryNames() {
        Iterator<ActionFactory> iterator = this.loader.iterator();
        ArrayList<String> names = new ArrayList<String>();
        while (iterator.hasNext()) {
            ActionFactory next = iterator.next();
            names.add(next.getName());
        }
        return names;
    }
}

