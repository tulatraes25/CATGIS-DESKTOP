/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.dbdatasource.iterators;

import com.vividsolutions.jump.feature.Feature;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.dao.datasource.dbdatasource.iterators.JDBCIterator;
import org.saig.core.filter.Filter;
import org.saig.jump.lang.I18N;

public class PostGisIterator
extends JDBCIterator {
    private static final Logger LOGGER = Logger.getLogger(PostGisIterator.class);
    protected boolean initialized = false;

    public PostGisIterator(String consultaBD, Filter filter, AbstractJDBCDataSource dataSource, List<String> labels) {
        this(consultaBD, filter, dataSource, labels, false);
    }

    public PostGisIterator(String consultaBD, Filter filter, AbstractJDBCDataSource dataSource, List<String> labels, boolean checkFilter) {
        this.dataSource = dataSource;
        this.labels = labels;
        this.consultaBD = consultaBD;
        this.filter = filter;
        this.checkFilter = checkFilter;
        if (dataSource.getLayerFilter() != null) {
            this.filter = this.filter != null ? this.filter.and(dataSource.getLayerFilter()) : dataSource.getLayerFilter();
        }
    }

    @Override
    protected void executeSQLIterator() throws SQLException {
        this.connection = DataBaseConnectionFactory.getConnection(this.dataSource);
        this.statement = this.connection.createStatement();
        this.statement.execute("BEGIN");
        LOGGER.debug((Object)("Executing query " + this.consultaBD));
        this.statement.execute("DECLARE my_cursor binary CURSOR for " + this.consultaBD);
        this.resultset = this.statement.executeQuery("FETCH FORWARD 100 IN my_cursor");
    }

    @Override
    public Feature next() throws Exception {
        return this.dataBaseFeature;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean hasNext() throws Exception {
        Feature feature;
        if (this.closed) {
            return false;
        }
        if (!this.initialized) {
            try {
                this.executeSQLIterator();
            }
            catch (SQLException e) {
                LOGGER.error((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.iterators.PostGisIterator.The-SQL-query-{0}-has-failed", new Object[]{this.consultaBD}));
                LOGGER.error((Object)"", (Throwable)e);
                throw e;
            }
            this.initialized = true;
        }
        Feature feat = null;
        while (feat == null && this.resultset.next()) {
            feature = this.dataSource.readOptimizedFeature(this.resultset, this.labels, true);
            if (feature != null && this.filter != null && this.checkFilter) {
                Feature feature2 = feature = this.filter.contains(feature) ? feature : null;
            }
            if (feature == null) continue;
            feat = feature;
        }
        if (feat == null) {
            this.resultset.close();
            this.resultset = this.statement.executeQuery("FETCH FORWARD 100 IN my_cursor");
            if (!this.resultset.next()) return false;
            feature = this.dataSource.readOptimizedFeature(this.resultset, this.labels, true);
            if (feature != null && this.filter != null && this.checkFilter) {
                Feature feature3 = feature = this.filter.contains(feature) ? feature : null;
            }
            if (feature == null) return this.hasNext();
            feat = feature;
        }
        this.dataBaseFeature = feat;
        return true;
    }

    @Override
    public void close(boolean isCancel) {
        if (this.closed) {
            LOGGER.warn((Object)I18N.getString("org.saig.core.dao.datasource.dbdatasource.iterators.PostGisIterator.The-connection-was-already-closed"));
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
                this.statement.execute("CLOSE my_cursor");
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        try {
            if (this.statement != null) {
                this.statement.execute("END");
                this.statement.close();
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        }
        catch (Exception ex) {
            LOGGER.error((Object)"", (Throwable)ex);
        }
    }
}

