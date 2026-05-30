/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.dbdatasource.utils;

import org.saig.jump.lang.I18N;

public class OracleToEpsgCodeNotFoundException
extends Exception {
    private static final long serialVersionUID = 1L;

    public OracleToEpsgCodeNotFoundException(int oracleCode) {
        super(I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource.an-epsg-code-corresponding-to-the-oracle-code-{0}-can-not-be-found", new Object[]{oracleCode}));
    }
}

