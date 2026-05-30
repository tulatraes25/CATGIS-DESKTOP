/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource;

import com.vividsolutions.jump.workbench.model.IQueryable;
import java.util.Hashtable;
import java.util.Set;
import org.saig.core.dao.datasource.AbstractDataSource;

public abstract class AbstractCadDataSource
extends AbstractDataSource
implements IQueryable {
    public abstract Hashtable<String, Set<Integer>> getLayerToColor();
}

