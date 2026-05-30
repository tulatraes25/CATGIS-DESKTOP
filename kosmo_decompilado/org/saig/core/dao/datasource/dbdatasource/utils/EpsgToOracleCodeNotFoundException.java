/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.dbdatasource.utils;

import org.saig.jump.lang.I18N;

public class EpsgToOracleCodeNotFoundException
extends Exception {
    private static final long serialVersionUID = 1L;

    public EpsgToOracleCodeNotFoundException(int epsgCode) {
        super(I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource.an-oracle-code-corresponding-to-the-epsg-code-{0}-can-not-be-found", new Object[]{epsgCode}));
    }
}

