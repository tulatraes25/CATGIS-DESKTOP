/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.trigger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.saig.core.model.data.trigger.IPostAddTrigger;
import org.saig.core.model.data.trigger.IPostDeleteTrigger;
import org.saig.core.model.data.trigger.IPostUpdateTrigger;
import org.saig.core.model.data.trigger.IPreAddTrigger;
import org.saig.core.model.data.trigger.IPreDeleteTrigger;
import org.saig.core.model.data.trigger.IPreUpdateTrigger;
import org.saig.core.model.data.trigger.ITrigger;
import org.saig.core.model.data.trigger.TriggerException;
import org.saig.core.model.data.trigger.impl.ApplyPatternFeatureTrigger;
import org.saig.core.model.data.trigger.impl.RemoveDescendantsFromLayerTrigger;
import org.saig.jump.lang.I18N;

public class TriggerFactory {
    private static final Logger LOGGER = Logger.getLogger(TriggerFactory.class);
    private static Map<String, Class<? extends ITrigger>> triggerNameToClassMap;

    private TriggerFactory() {
        this.registerDefaultTriggers();
    }

    private void registerDefaultTriggers() {
        triggerNameToClassMap = new TreeMap<String, Class<? extends ITrigger>>();
        triggerNameToClassMap.put(RemoveDescendantsFromLayerTrigger.ID, RemoveDescendantsFromLayerTrigger.class);
        triggerNameToClassMap.put(ApplyPatternFeatureTrigger.ID, ApplyPatternFeatureTrigger.class);
    }

    public static TriggerFactory getInstance() {
        return TriggerFactoryHolder.instance;
    }

    public ITrigger getTrigger(String triggerName) throws TriggerException {
        return this.getTrigger(triggerName, null, null);
    }

    public ITrigger getTrigger(String triggerName, Class<?>[] args, Object[] values) throws TriggerException {
        Class<? extends ITrigger> triggerClass = null;
        if (!triggerNameToClassMap.containsKey(triggerName)) {
            throw new TriggerException(I18N.getMessage(this.getClass(), "could-not-find-trigger-class-associated-to-the-name-{0}", new Object[]{triggerName}));
        }
        triggerClass = triggerNameToClassMap.get(triggerName);
        ITrigger trigger = null;
        try {
            Constructor<? extends ITrigger> hazNuevo = triggerClass.getConstructor(args);
            trigger = hazNuevo.newInstance(values);
        }
        catch (IllegalAccessException e) {
            String errorMessage = I18N.getMessage(this.getClass(), "access-error-occured-while-recovering-trigger-{0}", new Object[]{triggerClass.getName()});
            LOGGER.warn((Object)errorMessage);
            throw new TriggerException(errorMessage, e);
        }
        catch (InstantiationException e) {
            String errorMessage = I18N.getMessage(this.getClass(), "could-not-instantiate-trigger-{0}", new Object[]{triggerClass.getName()});
            LOGGER.warn((Object)errorMessage);
            throw new TriggerException(errorMessage, e);
        }
        catch (SecurityException e) {
            String errorMessage = I18N.getMessage(this.getClass(), "could-not-instantiate-trigger-{0}", new Object[]{triggerClass.getName()});
            LOGGER.warn((Object)errorMessage);
            throw new TriggerException(errorMessage, e);
        }
        catch (NoSuchMethodException e) {
            String errorMessage = I18N.getMessage(this.getClass(), "could-not-find-constructor-for-trigger-{0}", new Object[]{triggerClass.getName()});
            LOGGER.warn((Object)errorMessage);
            throw new TriggerException(errorMessage, e);
        }
        catch (IllegalArgumentException e) {
            String errorMessage = I18N.getMessage(this.getClass(), "illegal-argument-for-trigger-{0}", new Object[]{triggerClass.getName()});
            LOGGER.warn((Object)errorMessage);
            throw new TriggerException(errorMessage, e);
        }
        catch (InvocationTargetException e) {
            String errorMessage = I18N.getMessage(this.getClass(), "could-not-find-constructor-for-trigger-{0}", new Object[]{triggerClass.getName()});
            LOGGER.warn((Object)errorMessage);
            throw new TriggerException(errorMessage, e);
        }
        return trigger;
    }

    public Collection<IPreDeleteTrigger> filterPreDeleteTriggers(Collection<ITrigger> triggers) {
        LinkedHashSet<IPreDeleteTrigger> deleteTriggers = new LinkedHashSet<IPreDeleteTrigger>();
        for (ITrigger currentTrigger : triggers) {
            if (!(currentTrigger instanceof IPreDeleteTrigger)) continue;
            deleteTriggers.add((IPreDeleteTrigger)((Object)currentTrigger));
        }
        return deleteTriggers;
    }

    public Collection<IPostDeleteTrigger> filterPostDeleteTriggers(Collection<ITrigger> triggers) {
        LinkedHashSet<IPostDeleteTrigger> deleteTriggers = new LinkedHashSet<IPostDeleteTrigger>();
        for (ITrigger currentTrigger : triggers) {
            if (!(currentTrigger instanceof IPostDeleteTrigger)) continue;
            deleteTriggers.add((IPostDeleteTrigger)((Object)currentTrigger));
        }
        return deleteTriggers;
    }

    public Collection<IPreAddTrigger> filterPreAddTriggers(Set<ITrigger> triggers) {
        LinkedHashSet<IPreAddTrigger> addTriggers = new LinkedHashSet<IPreAddTrigger>();
        for (ITrigger currentTrigger : triggers) {
            if (!(currentTrigger instanceof IPreAddTrigger)) continue;
            addTriggers.add((IPreAddTrigger)((Object)currentTrigger));
        }
        return addTriggers;
    }

    public Collection<IPostAddTrigger> filterPostAddTriggers(Set<ITrigger> triggers) {
        LinkedHashSet<IPostAddTrigger> addTriggers = new LinkedHashSet<IPostAddTrigger>();
        for (ITrigger currentTrigger : triggers) {
            if (!(currentTrigger instanceof IPostAddTrigger)) continue;
            addTriggers.add((IPostAddTrigger)((Object)currentTrigger));
        }
        return addTriggers;
    }

    public Collection<IPreUpdateTrigger> filterPreUpdateTriggers(Set<ITrigger> triggers) {
        LinkedHashSet<IPreUpdateTrigger> updateTriggers = new LinkedHashSet<IPreUpdateTrigger>();
        for (ITrigger currentTrigger : triggers) {
            if (!(currentTrigger instanceof IPreUpdateTrigger)) continue;
            updateTriggers.add((IPreUpdateTrigger)((Object)currentTrigger));
        }
        return updateTriggers;
    }

    public Collection<IPostUpdateTrigger> filterPostUpdateTriggers(Set<ITrigger> triggers) {
        LinkedHashSet<IPostUpdateTrigger> updateTriggers = new LinkedHashSet<IPostUpdateTrigger>();
        for (ITrigger currentTrigger : triggers) {
            if (!(currentTrigger instanceof IPostUpdateTrigger)) continue;
            updateTriggers.add((IPostUpdateTrigger)((Object)currentTrigger));
        }
        return updateTriggers;
    }

    public void registerCustomTrigger(String id, Class<? extends ITrigger> triggerClass) throws TriggerException {
        if (!ITrigger.class.isAssignableFrom(triggerClass)) {
            throw new TriggerException(I18N.getMessage(this.getClass(), "class-{0}-does-not-represent-a-trigger", new Object[]{triggerClass}));
        }
        if (!triggerNameToClassMap.containsKey(id)) {
            triggerNameToClassMap.put(id, triggerClass);
        } else {
            LOGGER.warn((Object)I18N.getMessage(this.getClass(), "the-trigger-{0}-was-already-registered", new Object[]{id}));
        }
    }

    /* synthetic */ TriggerFactory(TriggerFactory triggerFactory) {
        this();
    }

    private static class TriggerFactoryHolder {
        private static final TriggerFactory instance = new TriggerFactory(null);

        private TriggerFactoryHolder() {
        }
    }
}

