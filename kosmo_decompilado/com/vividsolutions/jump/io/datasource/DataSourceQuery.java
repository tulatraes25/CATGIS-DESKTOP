/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io.datasource;

import com.vividsolutions.jump.io.datasource.DataSource;

public class DataSourceQuery {
    private String name;
    private String layerName;
    private DataSource dataSource;
    private String query;

    public DataSourceQuery() {
    }

    public DataSourceQuery(DataSource dataSource, String query, String name) {
        this.dataSource = dataSource;
        this.query = query;
        this.name = name;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public String getQuery() {
        return this.query;
    }

    public String toString() {
        return this.name;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLayerName() {
        return this.layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }
}

