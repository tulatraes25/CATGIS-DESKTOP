/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.dbdatasource.iterators.full;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.ILayerIterator;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.filter.Filter;
import org.saig.jump.lang.I18N;

public class PostGisFullIterator
implements ILayerIterator {
    long size = -1L;
    public static final Logger LOGGER = Logger.getLogger(PostGisFullIterator.class);
    protected Connection connection;
    protected Statement statement;
    protected String consultaBD;
    protected AbstractJDBCDataSource dataSource;

    public PostGisFullIterator(Envelope envelope, Filter filter, String[] fieldsToOrdered, boolean ascending, AbstractJDBCDataSource dataSource) throws Exception {
        this.consultaBD = dataSource.getSQLForQuery(envelope, filter, fieldsToOrdered, ascending);
        this.dataSource = dataSource;
        this.connection = DataBaseConnectionFactory.getConnection(dataSource);
        this.open();
    }

    @Override
    public Feature absolute(int n) throws Exception {
        Feature feature = null;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH ABSOLUTE " + n + " FROM my_cursor");
            if (resultset.next()) {
                feature = this.dataSource.readOptimizedFeature(resultset, this.dataSource.getAllLabels(), true);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return feature;
    }

    @Override
    public Feature backward() throws Exception {
        Feature feature = null;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH BACKWARD FROM my_cursor");
            if (resultset.next()) {
                feature = this.dataSource.readOptimizedFeature(resultset, this.dataSource.getAllLabels(), true);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return feature;
    }

    @Override
    public List<Feature> backward(int n) throws Exception {
        ArrayList<Feature> features = new ArrayList<Feature>();
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH BACKWARD " + n + " FROM my_cursor");
            while (resultset.next()) {
                features.add(this.dataSource.readOptimizedFeature(resultset, this.dataSource.getAllLabels(), true));
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return features;
    }

    @Override
    public List<Feature> backward_all() throws Exception {
        ArrayList<Feature> features = new ArrayList<Feature>();
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH BACKWARD ALL FROM my_cursor");
            while (resultset.next()) {
                features.add(this.dataSource.readOptimizedFeature(resultset, this.dataSource.getAllLabels(), true));
            }
        }
        finally {
            resultset.close();
        }
        return features;
    }

    @Override
    public void close() throws Exception {
        if (this.statement != null) {
            this.statement.execute("CLOSE my_cursor");
        }
        if (this.connection != null) {
            this.connection.close();
        }
    }

    @Override
    public Feature first() throws Exception {
        Feature feature = null;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH FIRST FROM my_cursor");
            if (resultset.next()) {
                feature = this.dataSource.readOptimizedFeature(resultset, this.dataSource.getAllLabels(), true);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return feature;
    }

    @Override
    public Feature forward() throws Exception {
        Feature feature = null;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH FORWARD FROM my_cursor");
            if (resultset.next()) {
                feature = this.dataSource.readOptimizedFeature(resultset, this.dataSource.getAllLabels(), true);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return feature;
    }

    @Override
    public List<Feature> forward(int n) throws Exception {
        ArrayList<Feature> features = new ArrayList<Feature>();
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH FORWARD " + n + " FROM my_cursor");
            while (resultset.next()) {
                features.add(this.dataSource.readOptimizedFeature(resultset, this.dataSource.getAllLabels(), true));
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return features;
    }

    @Override
    public List<Feature> forward_all() throws Exception {
        ArrayList<Feature> features = new ArrayList<Feature>();
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH FORWARD ALL FROM my_cursor");
            while (resultset.next()) {
                features.add(this.dataSource.readOptimizedFeature(resultset, this.dataSource.getAllLabels(), true));
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return features;
    }

    @Override
    public Feature last() throws Exception {
        Feature feature = null;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH LAST FROM my_cursor");
            if (resultset.next()) {
                feature = this.dataSource.readOptimizedFeature(resultset, this.dataSource.getAllLabels(), true);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return feature;
    }

    @Override
    public Feature next() throws Exception {
        Feature feature = null;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH NEXT FROM my_cursor");
            if (resultset.next()) {
                feature = this.dataSource.readOptimizedFeature(resultset, this.dataSource.getAllLabels(), true);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return feature;
    }

    @Override
    public void open() throws Exception {
        LOGGER.info((Object)I18N.getMessage(this.getClass(), "opening-iterator-for-query-{0}", new Object[]{this.consultaBD}));
        this.statement = this.connection.createStatement();
        this.statement.execute("BEGIN");
        this.statement.execute("DECLARE my_cursor binary SCROLL CURSOR for " + this.consultaBD);
    }

    @Override
    public Feature prior() throws Exception {
        Feature feature = null;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH PRIOR FROM my_cursor");
            if (resultset.next()) {
                feature = this.dataSource.readOptimizedFeature(resultset, this.dataSource.getAllLabels(), true);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return feature;
    }

    @Override
    public Feature relative(int n) throws Exception {
        Feature feature = null;
        ResultSet resultset = null;
        try {
            resultset = this.statement.executeQuery("FETCH RELATIVE " + n + " FROM my_cursor");
            if (resultset.next()) {
                feature = this.dataSource.readOptimizedFeature(resultset, this.dataSource.getAllLabels(), true);
            }
        }
        finally {
            if (resultset != null) {
                resultset.close();
            }
        }
        return feature;
    }

    @Override
    public long size() throws Exception {
        if (this.size == -1L) {
            ResultSet resultset = null;
            try {
                resultset = this.statement.executeQuery("SELECT COUNT(*) FROM (" + this.consultaBD + ") a");
                if (resultset.next()) {
                    this.size = resultset.getLong(1);
                }
            }
            finally {
                if (resultset != null) {
                    resultset.close();
                }
            }
        }
        return this.size;
    }
}

