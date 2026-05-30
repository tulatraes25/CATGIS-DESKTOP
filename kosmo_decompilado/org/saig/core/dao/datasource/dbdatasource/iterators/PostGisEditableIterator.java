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

public class PostGisEditableIterator
extends JDBCIterator {
    public PostGisEditableIterator(String consultaBD, Filter filter, AbstractJDBCDataSource dataSource, List<String> labels) {
        this(consultaBD, filter, dataSource, labels, false);
    }

    public PostGisEditableIterator(String consultaBD, Filter filter, AbstractJDBCDataSource dataSource, List<String> labels, boolean checkFilter) {
        super(consultaBD, filter, dataSource, labels, checkFilter);
        if (dataSource.getLayerFilter() != null) {
            this.filter = this.filter != null ? this.filter.and(dataSource.getLayerFilter()) : dataSource.getLayerFilter();
        }
    }

    @Override
    protected void executeSQLIterator() throws SQLException {
        this.connection = DataBaseConnectionFactory.getConnection(this.dataSource);
        this.statement = this.connection.createStatement();
        this.statement.execute("BEGIN");
        this.statement.execute("DECLARE my_cursor binary CURSOR for " + this.consultaBD);
        this.resultset = this.statement.executeQuery("FETCH FORWARD 100 IN my_cursor");
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean hasNext() throws Exception {
        Feature feature;
        Feature feat;
        if (this.closed) {
            return false;
        }
        if (!this.ignoreInMemoryChanges) {
            if (!this.itExternoAgotado) {
                if (this.itExternos.hasNext()) {
                    feat = null;
                    while (feat == null && this.itExternos.hasNext()) {
                        Object object = this.itExternos.next();
                        if (object == null) continue;
                        Feature feature2 = (Feature)object;
                        if (!feature2.isUnsaved()) {
                            this.procesados.set(((Number)feature2.getPrimaryKey()).intValue());
                        }
                        if (this.filter != null) {
                            if (!this.filter.contains(feature2)) continue;
                            feat = feature2;
                            continue;
                        }
                        feat = feature2;
                    }
                    this.elementFeature = feat;
                    if (feat != null) return true;
                    this.itExternoAgotado = true;
                    try {
                        this.executeSQLIterator();
                    }
                    catch (SQLException e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        LOGGER.warn((Object)I18N.getString("org.saig.core.dao.datasource.dbdatasource.iterators.PostGisIterator.Errors-have-been-produced-in-the-iterator-the-connection-will-be-closed"));
                        this.close(false);
                        throw e;
                    }
                } else {
                    this.itExternoAgotado = true;
                    try {
                        this.executeSQLIterator();
                    }
                    catch (SQLException e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        LOGGER.warn((Object)(String.valueOf(I18N.getString("org.saig.core.dao.datasource.dbdatasource.iterators.PostGisEditableIterator.query-failed")) + ": " + this.consultaBD));
                        LOGGER.warn((Object)I18N.getString("org.saig.core.dao.datasource.dbdatasource.iterators.PostGisIterator.Errors-have-been-produced-in-the-iterator-the-connection-will-be-closed"));
                        this.close(false);
                        throw e;
                    }
                }
            }
        } else if (!this.itExternoAgotado) {
            this.itExternoAgotado = true;
            try {
                this.executeSQLIterator();
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
                LOGGER.warn((Object)I18N.getString("org.saig.core.dao.datasource.dbdatasource.iterators.PostGisIterator.Errors-have-been-produced-in-the-iterator-the-connection-will-be-closed"));
                this.close(false);
                throw e;
            }
        }
        feat = null;
        while (feat == null && this.resultset.next()) {
            feature = this.dataSource.readOptimizedFeature(this.resultset, this.labels, this.ignoreInMemoryChanges);
            if (feature != null && this.filter != null && this.checkFilter) {
                Feature feature3 = feature = this.filter.contains(feature) ? feature : null;
            }
            if (feature == null || feature.getPrimaryKey() == null || !this.procesados.isEmpty() && this.procesados.get(((Number)feature.getPrimaryKey()).intValue())) continue;
            feat = feature;
        }
        if (feat == null) {
            this.resultset.close();
            this.resultset = this.statement.executeQuery("FETCH FORWARD 100 IN my_cursor");
            if (!this.resultset.next()) return false;
            feature = this.dataSource.readOptimizedFeature(this.resultset, this.labels, this.ignoreInMemoryChanges);
            if (feature != null && this.filter != null && this.checkFilter) {
                Feature feature4 = feature = this.filter.contains(feature) ? feature : null;
            }
            if (feature == null || !this.procesados.isEmpty() && this.procesados.get(((Number)feature.getPrimaryKey()).intValue())) return this.hasNext();
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

