/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import com.pcauto.gui.table.EntityList;
import com.pcauto.gui.table.EntityListEvent;
import com.pcauto.gui.table.EntityListException;
import com.pcauto.gui.table.EntityListListener;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;
import javax.swing.event.EventListenerList;
import org.saig.core.model.data.Record;

public class ProxyEntityList
implements EntityList {
    private Object entityPrototype = null;
    private List entityList = null;
    private boolean orderLocked = false;
    private EventListenerList listenerList = new EventListenerList();

    public ProxyEntityList() {
        this.entityList = new Vector();
    }

    public ProxyEntityList(Object prototype) {
        this.entityPrototype = prototype;
        this.entityList = new Vector();
    }

    public ProxyEntityList(Object prototype, List list) {
        this.entityPrototype = prototype;
        this.entityList = list;
    }

    public List getList() {
        return this.entityList;
    }

    public void setList(List list) {
        this.entityList = list;
        this.fireListChanged(null);
    }

    @Override
    public int getCount() {
        if (this.entityList != null) {
            return this.entityList.size();
        }
        return 0;
    }

    @Override
    public Object getEntity(int index) {
        return this.entityList.get(index);
    }

    @Override
    public boolean setEntity(int index, Object entityInstance) throws EntityListException {
        this.entityList.set(index, entityInstance);
        this.fireListChanged(new EntityListEvent(this, index, 2));
        return true;
    }

    @Override
    public Object getNewEntity() {
        Object retVal = null;
        try {
            retVal = this.newEntity();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (InstantiationException ex) {
            ex.printStackTrace();
        }
        return retVal;
    }

    private Object newEntity() throws InstantiationException, IllegalAccessException {
        Comparable<Record> retVal = null;
        if (this.entityPrototype instanceof Record) {
            Record record = (Record)this.entityPrototype;
            retVal = new Record(record.getSchema());
        } else if (this.entityPrototype instanceof Feature) {
            Feature record = (Feature)this.entityPrototype;
            retVal = new BasicFeature(record.getSchema());
        } else {
            retVal = (Comparable<Record>)this.entityPrototype.getClass().newInstance();
        }
        return retVal;
    }

    @Override
    public Object getNewDefaultEntity() {
        Object retVal = null;
        Method cloneMethod = null;
        try {
            cloneMethod = this.findMethod(this.entityPrototype.getClass(), "clone", new Class[0]);
            retVal = cloneMethod.invoke(this.entityPrototype, new Object[0]);
        }
        catch (Exception ex) {
            try {
                retVal = this.newEntity();
            }
            catch (IllegalAccessException illegalAccessException) {
            }
            catch (InstantiationException instantiationException) {
                // empty catch block
            }
        }
        return retVal;
    }

    @Override
    public Object getDefaultEntity() {
        return this.entityPrototype;
    }

    @Override
    public void setDefaultEntity(Object prototype) {
        this.entityPrototype = prototype;
    }

    @Override
    public boolean addEntity(Object entityInstance) throws EntityListException {
        this.entityList.add(entityInstance);
        this.fireListChanged(new EntityListEvent(this, this.entityList.size() - 1, 0));
        return true;
    }

    @Override
    public boolean insertEntity(int index, Object entityValue) throws EntityListException {
        if (index < 0 || index > this.entityList.size()) {
            return false;
        }
        this.entityList.add(index, entityValue);
        this.fireListChanged(new EntityListEvent(this, index, this.entityList.size() - 1, 2));
        return true;
    }

    @Override
    public boolean moveEntity(int startIndex, int endIndex, int newIndex) throws EntityListException {
        if (newIndex < 0 || newIndex + (endIndex - startIndex) >= this.entityList.size() || startIndex >= this.entityList.size() || startIndex < 0) {
            return false;
        }
        Vector tmpList = new Vector();
        int i = startIndex;
        while (i <= endIndex) {
            tmpList.add(this.entityList.remove(startIndex));
            ++i;
        }
        i = 0;
        while (i <= endIndex - startIndex) {
            this.entityList.add(newIndex + i, tmpList.remove(0));
            ++i;
        }
        this.fireListChanged(new EntityListEvent(this, startIndex, endIndex, 1));
        this.fireListChanged(new EntityListEvent(this, newIndex, newIndex + (endIndex - startIndex), 0));
        return true;
    }

    @Override
    public boolean removeEntity(int index) throws EntityListException {
        if (index < 0 || index >= this.entityList.size()) {
            return false;
        }
        this.entityList.remove(index);
        this.fireListChanged(new EntityListEvent(this, index, 1));
        return true;
    }

    @Override
    public void addEntityListListener(EntityListListener e) {
        this.listenerList.add(EntityListListener.class, e);
    }

    @Override
    public void removeEntityListListener(EntityListListener e) {
        this.listenerList.remove(EntityListListener.class, e);
    }

    @Override
    public void replaceAll(EntityList v) {
        this.entityList.clear();
        if (v != null) {
            int i = 0;
            while (i < v.getCount()) {
                this.entityList.add(v.getEntity(i));
                ++i;
            }
        }
        this.fireListChanged(null);
    }

    protected void fireListChanged(EntityListEvent e) {
        Object[] listeners = this.listenerList.getListenerList();
        int i = listeners.length - 2;
        while (i >= 0) {
            if (listeners[i] == EntityListListener.class) {
                if (e == null) {
                    e = new EntityListEvent(this);
                }
                ((EntityListListener)listeners[i + 1]).listChanged(e);
            }
            i -= 2;
        }
    }

    protected Method findMethod(Class<?> type, String name, Class<?>[] classes) throws NoSuchMethodException, SecurityException {
        try {
            return type.getMethod(name, classes);
        }
        catch (NoSuchMethodException nsmex) {
            if (type == Object.class) {
                throw nsmex;
            }
            return this.findMethod(type.getSuperclass(), name, classes);
        }
    }
}

