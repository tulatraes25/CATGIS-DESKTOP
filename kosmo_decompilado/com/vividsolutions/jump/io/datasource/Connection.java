/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.IProjection
 */
package com.vividsolutions.jump.io.datasource;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.TaskMonitor;
import java.util.List;
import java.util.Map;
import org.cresques.cts.IProjection;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.styling.Style;

public interface Connection {
    public FeatureCollection[] executeQuery(String var1) throws Exception;

    public FeatureCollection[] executeQuery(String var1, List<Exception> var2) throws Exception;

    public FeatureCollection[] executeQuery(String var1, IProjection var2) throws Exception;

    public FeatureCollection[] executeQuery(String var1, IProjection var2, Map<String, Object> var3) throws Exception;

    public void executeUpdate(String var1, FeatureCollection var2, boolean var3, Style var4) throws Exception;

    public void executeUpdate(String var1, FeatureCollection var2, boolean var3, Style var4, IProjection var5) throws Exception;

    public void executeUpdate(String var1, FeatureCollection var2, boolean var3, Style var4, IProjection var5, TaskMonitor var6) throws Exception;

    public List<AbstractDataSource> getDataSources();

    public void close();
}

