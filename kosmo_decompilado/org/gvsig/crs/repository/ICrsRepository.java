/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.repository;

import org.gvsig.crs.CrsException;
import org.gvsig.crs.ICrs;

public interface ICrsRepository {
    public ICrs getCrs(String var1) throws CrsException;

    public void close() throws Exception;
}

