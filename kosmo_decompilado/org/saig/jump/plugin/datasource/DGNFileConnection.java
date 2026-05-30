/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.cit.gvsig.fmap.drivers.dgn.DgnMemoryDriver
 *  org.cresques.cts.IProjection
 */
package org.saig.jump.plugin.datasource;

import com.iver.cit.gvsig.fmap.drivers.dgn.DgnMemoryDriver;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cresques.cts.IProjection;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.dao.datasource.filedatasource.cad.CadDataAccesor;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.styling.Style;
import org.saig.core.util.I18NUnsupportedOperationException;

public class DGNFileConnection
implements Connection {
    private CadDataAccesor dataAccesor;

    @Override
    public FeatureCollection[] executeQuery(String query, List<Exception> exceptions) throws Exception {
        return this.createFeatureCollectionFromSelection(query);
    }

    @Override
    public FeatureCollection[] executeQuery(String query) throws Exception {
        return this.createFeatureCollectionFromSelection(query);
    }

    @Override
    public FeatureCollection[] executeQuery(String query, IProjection proj) throws Exception {
        return this.createFeatureCollectionFromSelection(query);
    }

    @Override
    public FeatureCollection[] executeQuery(String query, IProjection projection, Map<String, Object> properties) throws Exception {
        return this.createFeatureCollectionFromSelection(query);
    }

    private void executeUpdate(String query, FeatureCollection featureCollection, boolean saveCalculatedAttributes) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void executeUpdate(String query, FeatureCollection fcToSave, boolean saveCalculatedAttributes, Style currentStyle) throws Exception {
        this.executeUpdate(query, fcToSave, saveCalculatedAttributes);
    }

    @Override
    public void executeUpdate(String query, FeatureCollection fcToSave, boolean saveCalculatedAttributes, Style currentStyle, IProjection proj) throws Exception {
        this.executeUpdate(query, fcToSave, saveCalculatedAttributes);
    }

    @Override
    public void executeUpdate(String query, FeatureCollection fcToSave, boolean saveCalculatedAttributes, Style clone, IProjection proj, TaskMonitor monitor) throws Exception {
        this.executeUpdate(query, fcToSave, saveCalculatedAttributes);
    }

    @Override
    public void close() {
    }

    @Override
    public List<AbstractDataSource> getDataSources() {
        ArrayList<AbstractDataSource> list = new ArrayList<AbstractDataSource>();
        list.add(this.dataAccesor);
        return list;
    }

    private FeatureCollection[] createFeatureCollectionFromSelection(String selectedFile) throws Exception {
        this.updateDataAccesor(selectedFile);
        FeatureCollectionOnDemand fC = new FeatureCollectionOnDemand();
        fC.setDataAccesor(this.dataAccesor);
        fC.setSchema(this.dataAccesor.getSchema());
        fC.setName(FileUtil.nameWithoutExtension(new File(selectedFile).getName()));
        return new FeatureCollection[]{fC};
    }

    private void updateDataAccesor(String query) throws Exception {
        this.dataAccesor = new CadDataAccesor(query, DgnMemoryDriver.class);
    }
}

