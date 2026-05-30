/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.dbdatasource.iterators;

import com.vividsolutions.jump.feature.Feature;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.filter.Filter;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class JDBCIterator
implements FeatureIterator {
    public static final Logger LOGGER = Logger.getLogger((String)"org.saig.core.dao.datasource.dbdatasource.iterators.JDBCIterator");
    protected ResultSet resultset;
    protected Connection connection;
    protected Statement statement;
    protected Iterator<Feature> itExternos;
    protected boolean itExternoAgotado = false;
    protected BitSet procesados;
    protected String consultaBD;
    protected boolean closed;
    protected AbstractJDBCDataSource dataSource;
    protected Feature dataBaseFeature;
    protected Feature elementFeature;
    protected Filter filter;
    protected boolean checkFilter;
    protected List<String> labels;
    protected final int BLOCK = 100;
    protected boolean ignoreInMemoryChanges;

    public JDBCIterator() {
        this.closed = false;
    }

    public JDBCIterator(String consultaBD, Filter filter, AbstractJDBCDataSource dataSource, List<String> labels, boolean checkFilter) {
        this.consultaBD = consultaBD;
        this.filter = filter;
        this.procesados = new BitSet();
        this.checkFilter = checkFilter;
        for (Feature feat : dataSource.getDeletedFeatures()) {
            this.procesados.set(((Number)feat.getPrimaryKey()).intValue());
        }
        HashSet<Feature> newMod = new HashSet<Feature>();
        newMod.addAll(dataSource.getUpdatedFeatures());
        newMod.addAll(dataSource.getNewFeatures());
        this.itExternos = newMod.iterator();
        this.closed = false;
        this.dataSource = dataSource;
        this.labels = labels;
    }

    @Override
    public void close() {
        this.close(false);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean hasNext() throws Exception {
        Feature feat;
        if (this.closed) {
            return false;
        }
        if (!this.ignoreInMemoryChanges && !this.itExternoAgotado) {
            if (this.itExternos.hasNext()) {
                feat = null;
                while (feat == null && this.itExternos.hasNext()) {
                    Feature object = this.itExternos.next();
                    if (object == null) continue;
                    Feature feature = object;
                    if (!feature.isUnsaved()) {
                        this.procesados.set(((Number)feature.getPrimaryKey()).intValue());
                    }
                    if (this.filter != null) {
                        if (!this.filter.contains(feature)) continue;
                        feat = feature;
                        continue;
                    }
                    feat = feature;
                }
                this.elementFeature = feat;
                if (feat != null) return true;
                this.itExternoAgotado = true;
                this.executeSQLIterator();
            } else {
                this.itExternoAgotado = true;
                this.executeSQLIterator();
            }
        }
        feat = null;
        while (feat == null && this.resultset.next()) {
            Feature feature = this.dataSource.readOptimizedFeature(this.resultset, this.labels, this.ignoreInMemoryChanges);
            if (feature != null && this.filter != null && this.checkFilter) {
                Feature feature2 = feature = this.filter.contains(feature) ? feature : null;
            }
            if (feature == null) {
                LOGGER.warn((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.iterators.JDBCIterator.null-feature-the-query-is-{0}", new Object[]{this.consultaBD}));
                continue;
            }
            if (this.procesados.get(((Number)feature.getPrimaryKey()).intValue())) continue;
            feat = feature;
        }
        this.dataBaseFeature = feat;
        return feat != null;
    }

    protected void executeSQLIterator() throws SQLException {
        this.connection = DataBaseConnectionFactory.getConnection(this.dataSource);
        this.statement = this.connection.createStatement();
        this.statement.setFetchSize(100);
        this.resultset = this.statement.executeQuery(this.consultaBD);
    }

    @Override
    public Feature next() throws Exception {
        if (!this.itExternoAgotado) {
            return this.elementFeature;
        }
        return this.dataBaseFeature;
    }

    public void remove() {
    }

    @Override
    public void close(boolean isCancel) {
        if (this.closed) {
            return;
        }
        this.closed = true;
        try {
            if (this.resultset != null) {
                this.resultset.close();
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        try {
            if (this.statement != null) {
                if (isCancel) {
                    this.statement.cancel();
                }
                this.statement.close();
            }
        }
        catch (Exception ex) {
            LOGGER.error((Object)"", (Throwable)ex);
        }
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        }
        catch (Exception ex1) {
            LOGGER.error((Object)"", (Throwable)ex1);
        }
        LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.dbdatasource.iterators.JDBCIterator.closing-database-connection"));
    }

    @Override
    public void setIgnoredUpdate(boolean ignored) {
        this.ignoreInMemoryChanges = ignored;
    }
}

