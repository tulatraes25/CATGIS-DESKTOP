/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import com.pcauto.gui.table.EntityListException;
import com.pcauto.gui.table.EntityListListener;

public interface EntityList {
    public int getCount();

    public Object getEntity(int var1);

    public boolean setEntity(int var1, Object var2) throws EntityListException;

    public Object getNewEntity();

    public Object getNewDefaultEntity();

    public Object getDefaultEntity();

    public void setDefaultEntity(Object var1);

    public boolean addEntity(Object var1) throws EntityListException;

    public boolean insertEntity(int var1, Object var2) throws EntityListException;

    public boolean moveEntity(int var1, int var2, int var3) throws EntityListException;

    public boolean removeEntity(int var1) throws EntityListException;

    public void replaceAll(EntityList var1);

    public void addEntityListListener(EntityListListener var1);

    public void removeEntityListListener(EntityListListener var1);
}

