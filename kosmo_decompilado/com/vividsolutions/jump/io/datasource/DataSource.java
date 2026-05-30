/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io.datasource;

import com.vividsolutions.jump.io.datasource.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class DataSource {
    public static final String FILE_KEY = "File";
    public static final String SELECTED_CHARSET_KEY = "Selected charset";
    public static final String SYSTEM_CHARSET = "Sistema";
    public static final String SELECTED_PROJECTION_KEY = "Selected projection";
    public static final String CAD_IGNORE_BLOCKS = "Ignore blocks";
    public static final String OPTIMIZE_SHAPEFILE_MEMORY_RESOURCES = "Optimize shapefile memory resources";
    private Map<String, Object> properties;

    public void setProperties(Map<String, Object> properties) {
        this.properties = new HashMap<String, Object>(properties);
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public abstract Connection getConnection();

    public boolean isReadable() {
        return true;
    }

    public boolean isWritable() {
        return true;
    }

    public boolean isReadableFromProjectFile() {
        return true;
    }
}

