/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.dbdatasource.keys_resolver;

import java.sql.Connection;

public interface IDBKeyResolver {
    public Object[] getKey(Connection var1) throws Exception;

    public void setDefaultKey(Object[] var1);

    public String getSql();

    public void setSql(String var1);

    public String toXML();

    public IDBKeyResolver clone();
}

