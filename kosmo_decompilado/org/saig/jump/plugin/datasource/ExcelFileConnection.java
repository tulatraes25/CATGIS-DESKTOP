/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.IProjection
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.FileUtil;
import java.util.List;
import java.util.Map;
import org.cresques.cts.IProjection;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.model.data.dao.export.ExportUtils;
import org.saig.core.styling.Style;
import org.saig.core.util.I18NUnsupportedOperationException;

public class ExcelFileConnection
implements Connection {
    @Override
    public FeatureCollection[] executeQuery(String query, List<Exception> exceptions) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public FeatureCollection[] executeQuery(String query) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public FeatureCollection[] executeQuery(String query, IProjection proj) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public FeatureCollection[] executeQuery(String query, IProjection projection, Map<String, Object> properties) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void executeUpdate(String query, FeatureCollection fcToSave, boolean saveCalculatedAttributes, Style currentStyle, IProjection proj) throws Exception {
        this.executeUpdate(query, fcToSave, saveCalculatedAttributes, currentStyle, proj, null);
    }

    @Override
    public void executeUpdate(String query, FeatureCollection fcToSave, boolean saveCalculatedAttributes, Style clone, IProjection proj, TaskMonitor monitor) throws Exception {
        query = FileUtil.addValidExtension(query, "xls");
        if (monitor == null) {
            ExportUtils.exportFcToExcelSpreadsheet(fcToSave, query, saveCalculatedAttributes);
        } else {
            ExportUtils.exportFcToExcelSpreadsheet(fcToSave, query, saveCalculatedAttributes, monitor);
        }
    }

    @Override
    public void executeUpdate(String query, FeatureCollection featureCollection, boolean saveCalculatedAttributes, Style currentStyle) throws Exception {
        this.executeUpdate(query, featureCollection, saveCalculatedAttributes, currentStyle, null, null);
    }

    @Override
    public void close() {
    }

    @Override
    public List<AbstractDataSource> getDataSources() {
        return null;
    }
}

