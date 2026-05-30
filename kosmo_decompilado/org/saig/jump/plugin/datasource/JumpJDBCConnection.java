/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import es.kosmo.core.dao.datasource.memory.DynamicResultsFeatureCollection;
import es.kosmo.core.utils.FeatureSchemaUtils;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.crs.ICrs;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource;
import org.saig.core.dao.datasource.dbdatasource.utils.EpsgToOracleCodeConverter;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.feature.TemporalFeatureDataset;
import org.saig.core.styling.Style;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.DataSourceUtil;

public class JumpJDBCConnection
implements Connection {
    private static final Logger LOGGER = Logger.getLogger(JumpJDBCConnection.class);
    private static final int BLOCK = 5000;
    private List<AbstractDataSource> dataAccesors = new ArrayList<AbstractDataSource>();
    private AbstractJDBCDataSource datasource;
    protected boolean addRestrictions = true;
    protected boolean ignore3d = true;
    protected boolean forceMultiGeometry = false;
    protected boolean userLowerCaseFieldNames = false;
    protected String specificGeometryColumnName = null;

    @Override
    public FeatureCollection[] executeQuery(String query, List<Exception> exceptions) throws Exception {
        return this.createFeatureCollectionFromJDBCDataSource();
    }

    @Override
    public FeatureCollection[] executeQuery(String query, IProjection proj) throws Exception {
        return this.createFeatureCollectionFromJDBCDataSource();
    }

    @Override
    public FeatureCollection[] executeQuery(String query, IProjection projection, Map<String, Object> properties) throws Exception {
        return this.createFeatureCollectionFromJDBCDataSource();
    }

    @Override
    public FeatureCollection[] executeQuery(String query) throws Exception {
        return this.createFeatureCollectionFromJDBCDataSource();
    }

    @Override
    public void executeUpdate(String query, FeatureCollection featureCollection, boolean saveCalculatedAttributes, Style currentStyle) throws Exception {
        this.executeUpdate(query, featureCollection, saveCalculatedAttributes, null, null, new DummyTaskMonitor());
    }

    @Override
    public void executeUpdate(String query, FeatureCollection featureCollection, boolean saveCalculatedAttributes, Style currentStyle, IProjection proj) throws Exception {
        this.executeUpdate(query, featureCollection, saveCalculatedAttributes, null, proj, new DummyTaskMonitor());
    }

    @Override
    public void executeUpdate(String query, FeatureCollection featureCollection, boolean saveCalculatedAttributes, Style style, IProjection projection, TaskMonitor monitor) throws Exception {
        block32: {
            monitor.allowCancellationRequests();
            monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.datasource.JumpJDBCConnection.Saving-layer")) + "...");
            int srid = -1;
            srid = projection != null ? ((ICrs)projection).getCode() : JUMPWorkbench.getFrameInstance().getContext().getTask().getCrsCode();
            if (this.datasource instanceof OracleSpatialDataSource) {
                try {
                    srid = EpsgToOracleCodeConverter.getInstance().epsgCodeToOracleCode(srid);
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    srid = -1;
                }
            }
            if (!featureCollection.isCad()) {
                Attribute attr;
                int i;
                monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.datasource.JumpJDBCConnection.Generating-attribute-schema")) + "...");
                FeatureSchema newSchema = new FeatureSchema();
                FeatureSchema oldSchema = featureCollection.getFeatureSchema();
                if (!saveCalculatedAttributes) {
                    i = 0;
                    while (i < oldSchema.getAttributeCount()) {
                        attr = oldSchema.getAttribute(i);
                        if (!attr.isCalculated()) {
                            if (oldSchema.getGeometryIndex() == i && StringUtils.isNotEmpty((String)this.specificGeometryColumnName)) {
                                newSchema.addAttribute(this.specificGeometryColumnName, attr.getType(), attr.isPrimaryKey());
                            } else {
                                newSchema.addAttribute(attr.getName(), attr.getType(), attr.isPrimaryKey());
                                newSchema.getAttribute(attr.getName()).setPublicName(attr.getPublicName());
                                newSchema.getAttribute(attr.getName()).setVisibility(attr.isVisibility());
                            }
                        }
                        ++i;
                    }
                    newSchema.setGeometryType(oldSchema.getGeometryType());
                } else if (oldSchema.hasCalculatedAttributes() || StringUtils.isNotEmpty((String)this.specificGeometryColumnName)) {
                    i = 0;
                    while (i < oldSchema.getAttributeCount()) {
                        attr = oldSchema.getAttribute(i);
                        if (oldSchema.getGeometryIndex() == i && StringUtils.isNotEmpty((String)this.specificGeometryColumnName)) {
                            newSchema.addAttribute(this.specificGeometryColumnName, attr.getType(), attr.isPrimaryKey());
                        } else {
                            newSchema.addAttribute(attr.getName(), attr.getType(), attr.isPrimaryKey());
                            newSchema.getAttribute(attr.getName()).setPublicName(attr.getPublicName());
                            newSchema.getAttribute(attr.getName()).setVisibility(attr.isVisibility());
                        }
                        ++i;
                    }
                    newSchema.setGeometryType(oldSchema.getGeometryType());
                } else {
                    newSchema = (FeatureSchema)oldSchema.clone();
                }
                if (this.userLowerCaseFieldNames) {
                    newSchema = FeatureSchemaUtils.convertFieldNamesToLowerCase(newSchema);
                }
                if (this.forceMultiGeometry && (featureCollection instanceof FeatureCollectionOnDemand || featureCollection instanceof DynamicResultsFeatureCollection || featureCollection instanceof TemporalFeatureDataset) && this.isSimple(featureCollection)) {
                    newSchema.setGeometryType(this.toMultiGeometryType(newSchema.getGeometryType()));
                }
                monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.datasource.JumpJDBCConnection.Creating-table")) + "...");
                this.datasource.initialize(newSchema, featureCollection.getEnvelope(), srid, this.addRestrictions, !this.ignore3d && featureCollection.is3d());
                this.datasource.setEditable(true);
                FeatureIterator iterator = null;
                AbstractJDBCDataSource jdbcDataSource = null;
                try {
                    try {
                        iterator = featureCollection.iterator();
                        jdbcDataSource = this.datasource;
                        jdbcDataSource.beginTransaction();
                        jdbcDataSource.setInMemory(false);
                        int cont = 0;
                        int total = featureCollection.size();
                        ArrayList<Feature> features = new ArrayList<Feature>();
                        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.datasource.JumpJDBCConnection.Saving-features")) + "...");
                        while (!monitor.isCancelRequested() && iterator.hasNext()) {
                            Feature feature = iterator.next().clone(true);
                            if (StringUtils.isNotEmpty((String)this.specificGeometryColumnName)) {
                                FeatureUtil.changeGeometryAttributeName(feature, this.specificGeometryColumnName);
                            }
                            feature.setSchema(jdbcDataSource.getSchema());
                            if (this.userLowerCaseFieldNames) {
                                FeatureUtil.convertAttributeNamesToLowerCase(feature);
                            }
                            features.add(feature);
                            if (++cont % 5000 != 0) continue;
                            monitor.report(cont, total, I18N.getString("org.saig.jump.plugin.datasource.JumpJDBCConnection.processed-features"));
                            jdbcDataSource.addAll(features, true);
                            features.clear();
                            LOGGER.debug((Object)(String.valueOf(I18N.getString("org.saig.jump.plugin.datasource.JumpJDBCConnection.Added")) + cont));
                        }
                        if (!monitor.isCancelRequested()) {
                            if (features.size() > 0) {
                                jdbcDataSource.addAll(features, true);
                                LOGGER.debug((Object)I18N.getString("org.saig.jump.plugin.datasource.JumpJDBCConnection.Finishing"));
                            }
                            jdbcDataSource.setInMemory(true);
                            jdbcDataSource.endTransaction();
                            if (jdbcDataSource.isNewTable()) {
                                jdbcDataSource.createSpatialIndex();
                            }
                        } else {
                            jdbcDataSource.rollback(true);
                            jdbcDataSource.clearTransaction();
                        }
                        break block32;
                    }
                    catch (Exception e) {
                        jdbcDataSource.rollback(false);
                        jdbcDataSource.clearTransaction();
                        if (jdbcDataSource != null && jdbcDataSource.isNewTable()) {
                            jdbcDataSource.removeDataStore();
                        }
                        throw e;
                    }
                }
                finally {
                    if (iterator != null) {
                        iterator.close();
                    }
                }
            }
            this.dataAccesors.clear();
            monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.datasource.JumpJDBCConnection.Classifying-features")) + "...");
            FeatureCollection[] fcs = DataSourceUtil.classifyFeatures(featureCollection);
            query = this.datasource.getTableName();
            if (this.datasource.isNewTable()) {
                this.datasource.removeDataStore();
            }
            monitor.report(0, 3, String.valueOf(I18N.getString("org.saig.jump.plugin.datasource.JumpJDBCConnection.Saving-point-layer")) + "...");
            this.saveCadFeatureCollection(fcs[0], String.valueOf(query) + "_point", saveCalculatedAttributes, projection, srid, this.addRestrictions, this.ignore3d, style, monitor);
            monitor.report(1, 3, String.valueOf(I18N.getString("org.saig.jump.plugin.datasource.JumpJDBCConnection.Saving-line-layer")) + "...");
            this.saveCadFeatureCollection(fcs[1], String.valueOf(query) + "_line", saveCalculatedAttributes, projection, srid, this.addRestrictions, this.ignore3d, style, monitor);
            monitor.report(2, 3, String.valueOf(I18N.getString("org.saig.jump.plugin.datasource.JumpJDBCConnection.Saving-polygon-layer")) + "...");
            this.saveCadFeatureCollection(fcs[2], String.valueOf(query) + "_polygon", saveCalculatedAttributes, projection, srid, this.addRestrictions, this.ignore3d, style, monitor);
        }
    }

    @Override
    public List<AbstractDataSource> getDataSources() {
        return this.dataAccesors;
    }

    @Override
    public void close() {
        this.datasource = null;
    }

    private boolean isSimple(FeatureCollection sourceFc) {
        int geomType = sourceFc.getFeatureSchema().getGeometryType();
        return geomType == 1 || geomType == 3 || geomType == 5;
    }

    private int toMultiGeometryType(int geometryType) {
        int result = 0;
        switch (geometryType) {
            case 1: {
                result = 8;
                break;
            }
            case 3: {
                result = 2;
                break;
            }
            case 5: {
                result = 4;
                break;
            }
            default: {
                result = geometryType;
            }
        }
        return result;
    }

    private void saveCadFeatureCollection(FeatureCollection fc, String query, boolean saveCalculateAttributes, IProjection proj, int srid, boolean addRestrictions, boolean ignore3d, Style style, TaskMonitor monitor) throws Exception {
        if (fc.isEmpty()) {
            return;
        }
        this.datasource.setTableName(query);
        FeatureSchema newSchema = (FeatureSchema)fc.getFeatureSchema().clone();
        if (this.userLowerCaseFieldNames) {
            newSchema = FeatureSchemaUtils.convertFieldNamesToLowerCase(newSchema);
        }
        if (this.forceMultiGeometry && fc instanceof FeatureCollectionOnDemand && this.isSimple(fc)) {
            newSchema.setGeometryType(this.toMultiGeometryType(newSchema.getGeometryType()));
        }
        this.datasource.initialize(newSchema, fc.getEnvelope(), srid, addRestrictions, !ignore3d && fc.is3d());
        this.executeUpdate(query, fc, saveCalculateAttributes, style, proj, monitor);
        this.dataAccesors.add((AbstractJDBCDataSource)this.datasource.clone());
    }

    private FeatureCollection[] createFeatureCollectionFromJDBCDataSource() throws SQLException {
        FeatureCollectionOnDemand fc = new FeatureCollectionOnDemand();
        if (!this.datasource.isInitialized()) {
            this.datasource.initialize(true);
            if (this.datasource.getPkName() == null) {
                this.datasource.setPkName(null);
            }
        }
        fc.setId(1L);
        fc.setName(this.datasource.getTableName());
        fc.setDataAccesor(this.datasource);
        fc.setSchema(this.datasource.getSchema());
        return new FeatureCollection[]{fc};
    }

    public void setDataSource(AbstractDataSource datasource) {
        this.datasource = (AbstractJDBCDataSource)datasource;
        if (datasource != null) {
            this.dataAccesors.add(datasource);
        }
    }

    public void setAddRestrictions(boolean addRestrictions) {
        this.addRestrictions = addRestrictions;
    }

    public void setIgnore3d(boolean ignore3D) {
        this.ignore3d = ignore3D;
    }

    public void setForceMultiGeometry(boolean forceMultiGeom) {
        this.forceMultiGeometry = forceMultiGeom;
    }

    public void setUseLowerCaseFieldNames(boolean lowerCaseFieldNames) {
        this.userLowerCaseFieldNames = lowerCaseFieldNames;
    }

    public void setGeometryColumnName(String columnName) {
        this.specificGeometryColumnName = columnName;
    }
}

