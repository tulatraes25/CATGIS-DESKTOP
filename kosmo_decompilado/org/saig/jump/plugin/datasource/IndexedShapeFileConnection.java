/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import es.kosmo.desktop.widgets.datasource.ShapeFileSaveQueryChooser;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.styling.Style;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.DataSourceUtil;
import org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser;

public class IndexedShapeFileConnection
implements Connection {
    private static final Logger LOGGER = Logger.getLogger(IndexedShapeFileConnection.class);
    private List<AbstractDataSource> dataAccesors = new ArrayList<AbstractDataSource>();

    @Override
    public FeatureCollection[] executeQuery(String query, List<Exception> exceptions) {
        try {
            return this.createFeatureCollectionFromSelection(query, new HashMap<String, Object>());
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            return null;
        }
    }

    @Override
    public FeatureCollection[] executeQuery(String query) throws Exception {
        return this.createFeatureCollectionFromSelection(query, new HashMap<String, Object>());
    }

    @Override
    public FeatureCollection[] executeQuery(String query, IProjection proj) throws Exception {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("Selected charset", this.recoverUserOption(ShapeFileLoadQueryChooser.CHARSET_ENCODING_NAME_KEY, null));
        properties.put("Optimize shapefile memory resources", this.recoverUserOption(ShapeFileLoadQueryChooser.OPTIMIZE_SHAPEFILE_MEMORY_RESOURCES_KEY, ShapeFileLoadQueryChooser.DEFAULT_OPTIMIZE_SHAPEFILE_MEMORY_RESOURCES));
        return this.createFeatureCollectionFromSelection(query, properties);
    }

    @Override
    public FeatureCollection[] executeQuery(String query, IProjection projection, Map<String, Object> properties) throws Exception {
        return this.createFeatureCollectionFromSelection(query, properties);
    }

    @Override
    public void executeUpdate(String query, FeatureCollection featureCollection, boolean saveCalculatedAttributes, Style currentStyle) throws Exception {
        this.executeUpdate(query, featureCollection, saveCalculatedAttributes);
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
    public List<AbstractDataSource> getDataSources() {
        return this.dataAccesors;
    }

    @Override
    public void close() {
    }

    private void executeUpdate(String query, FeatureCollection featureCollection, boolean saveCalculatedAttributes) throws Exception {
        String selectedCharset = (String)this.recoverUserOption(ShapeFileLoadQueryChooser.CHARSET_ENCODING_NAME_KEY, null);
        Boolean optimizeShapefileMemoryResources = (Boolean)this.recoverUserOption(ShapeFileLoadQueryChooser.OPTIMIZE_SHAPEFILE_MEMORY_RESOURCES_KEY, ShapeFileLoadQueryChooser.DEFAULT_OPTIMIZE_SHAPEFILE_MEMORY_RESOURCES);
        if (!featureCollection.isCad()) {
            query = FileUtil.addValidExtension(query, "shp");
            Boolean savePk = (Boolean)this.recoverUserOption(ShapeFileSaveQueryChooser.SAVE_PRIMARY_KEY_OPTION_KEY, ShapeFileSaveQueryChooser.DEFAULT_SAVE_PRIMARY_KEY_VALUE);
            ShapeFileDataSource.toShape(featureCollection, query, saveCalculatedAttributes, savePk, this.getCharset(selectedCharset));
            this.dataAccesors.add(this.createDataAccesor(query, selectedCharset, optimizeShapefileMemoryResources));
        } else {
            FeatureCollection[] fcs = DataSourceUtil.classifyFeatures(featureCollection);
            this.saveCadFeatureCollection(fcs[0], query, saveCalculatedAttributes, "_point");
            this.saveCadFeatureCollection(fcs[1], query, saveCalculatedAttributes, "_line");
            this.saveCadFeatureCollection(fcs[2], query, saveCalculatedAttributes, "_polygon");
        }
    }

    private void saveCadFeatureCollection(FeatureCollection fc, String query, boolean saveCalculatedAttributes, String typeExtension) throws Exception {
        if (fc.isEmpty()) {
            return;
        }
        String selectedCharset = (String)this.recoverUserOption(ShapeFileLoadQueryChooser.CHARSET_ENCODING_NAME_KEY, null);
        boolean optimizeShapefileMemoryResources = (Boolean)this.recoverUserOption(ShapeFileLoadQueryChooser.OPTIMIZE_SHAPEFILE_MEMORY_RESOURCES_KEY, ShapeFileLoadQueryChooser.DEFAULT_OPTIMIZE_SHAPEFILE_MEMORY_RESOURCES);
        Boolean savePk = (Boolean)this.recoverUserOption(ShapeFileSaveQueryChooser.SAVE_PRIMARY_KEY_OPTION_KEY, ShapeFileSaveQueryChooser.DEFAULT_SAVE_PRIMARY_KEY_VALUE);
        String query1 = FileUtil.nameWithoutExtension(query);
        query1 = String.valueOf(query1) + typeExtension;
        query1 = FileUtil.addValidExtension(query1, "shp");
        ShapeFileDataSource.toShape(fc, query1, saveCalculatedAttributes, savePk, this.getCharset(selectedCharset));
        this.dataAccesors.add(this.createDataAccesor(query1, selectedCharset, optimizeShapefileMemoryResources));
    }

    private ShapeFileDataSource createDataAccesor(String query, String selectedCharset, boolean optimizeMemoryResources) throws Exception {
        ShapeFileDataSource dataAccesor = new ShapeFileDataSource();
        Charset charset = this.getCharset(selectedCharset);
        if (charset != null) {
            dataAccesor.setCharset(charset);
        }
        dataAccesor.setFile(new File(query));
        LOGGER.debug((Object)I18N.getString("org.saig.jump.plugin.datasource.IndexedShapeFileConnection.creating-the-spatial-index"));
        dataAccesor.createSpatialIndex(optimizeMemoryResources);
        return dataAccesor;
    }

    private Charset getCharset(String charsetName) {
        Charset charset = null;
        charset = !StringUtils.isEmpty((String)charsetName) && Charset.isSupported(charsetName) ? Charset.forName(charsetName) : ShapeFileDataSource.DEFAULT_STRING_CHARSET;
        return charset;
    }

    private FeatureCollection[] createFeatureCollectionFromSelection(String selectedFile, Map<String, Object> properties) throws Exception {
        String selectedCharset = null;
        boolean optimizeMemoryResources = false;
        if (properties != null) {
            if (properties.get("Selected charset") != null) {
                selectedCharset = (String)properties.get("Selected charset");
            }
            if (properties.get("Optimize shapefile memory resources") != null) {
                optimizeMemoryResources = (Boolean)properties.get("Optimize shapefile memory resources");
            }
        }
        ShapeFileDataSource dataAccesor = this.createDataAccesor(selectedFile, selectedCharset, optimizeMemoryResources);
        FeatureCollectionOnDemand fC = new FeatureCollectionOnDemand();
        fC.setDataAccesor(dataAccesor);
        fC.setSchema(dataAccesor.getSchema());
        fC.setName(FileUtil.nameWithoutExtension(dataAccesor.getFile().getName()));
        return new FeatureCollection[]{fC};
    }

    private Object recoverUserOption(String key, Object defaultValue) {
        return PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(key, defaultValue);
    }
}

