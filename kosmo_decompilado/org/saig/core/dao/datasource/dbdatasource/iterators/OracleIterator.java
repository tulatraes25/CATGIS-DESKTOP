/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.dbdatasource.iterators;

import com.vividsolutions.jump.feature.Feature;
import java.sql.SQLException;
import java.util.List;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.dao.datasource.dbdatasource.iterators.JDBCIterator;
import org.saig.core.filter.Filter;
import org.saig.jump.lang.I18N;

public class OracleIterator
extends JDBCIterator {
    protected boolean initialized = false;

    public OracleIterator(String consultaBD, Filter filter, AbstractJDBCDataSource dataSource, List<String> labels) {
        this(consultaBD, filter, dataSource, labels, false);
    }

    public OracleIterator(String consultaBD, Filter filter, AbstractJDBCDataSource dataSource, List<String> labels, boolean checkFilter) {
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
    public Feature next() throws Exception {
        return this.dataBaseFeature;
    }

    @Override
    public boolean hasNext() throws Exception {
        if (this.closed) {
            return false;
        }
        if (!this.initialized) {
            try {
                this.executeSQLIterator();
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
                throw e;
            }
            this.initialized = true;
        }
        Feature feat = null;
        while (feat == null && this.resultset.next()) {
            Feature feature = this.dataSource.readOptimizedFeature(this.resultset, this.labels, true);
            if (feature != null && this.filter != null && this.checkFilter) {
                Feature feature2 = feature = this.filter.contains(feature) ? feature : null;
            }
            if (feature == null) {
                LOGGER.warn((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.iterators.JDBCIterator.null-feature-the-query-is-{0}", new Object[]{this.consultaBD}));
                continue;
            }
            feat = feature;
        }
        this.dataBaseFeature = feat;
        return feat != null;
    }

    @Override
    protected void executeSQLIterator() throws SQLException {
        this.connection = DataBaseConnectionFactory.getConnection(this.dataSource);
        this.statement = this.connection.createStatement(1003, 1007);
        this.statement.setFetchSize(100);
        this.statement.execute("ALTER SESSION SET NLS_DATE_FORMAT = 'YYYY-MM-DD'");
        this.resultset = this.statement.executeQuery(this.consultaBD);
    }
}

