/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.IProjection
 */
package com.vividsolutions.jump.io.datasource;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.JUMPReader;
import com.vividsolutions.jump.io.JUMPWriter;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.task.TaskMonitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cresques.cts.IProjection;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.styling.Style;

public class ReaderWriterFileDataSource
extends DataSource {
    protected JUMPReader reader;
    protected JUMPWriter writer;

    public ReaderWriterFileDataSource(JUMPReader reader, JUMPWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public Connection getConnection() {
        return new Connection(){

            @Override
            public FeatureCollection[] executeQuery(String query, List<Exception> exceptions) {
                try {
                    return new FeatureCollection[]{ReaderWriterFileDataSource.this.reader.read(ReaderWriterFileDataSource.this.getReaderDriverProperties())};
                }
                catch (Exception e) {
                    exceptions.add(e);
                    FeatureSchema schema = new FeatureSchema();
                    schema.addAttribute("Geometry", AttributeType.GEOMETRY);
                    return new FeatureCollection[]{new FeatureDataset(schema)};
                }
            }

            @Override
            public FeatureCollection[] executeQuery(String query, IProjection proj) throws Exception {
                return this.executeQuery(query);
            }

            @Override
            public FeatureCollection[] executeQuery(String query, IProjection proj, Map<String, Object> properties) throws Exception {
                return this.executeQuery(query);
            }

            @Override
            public FeatureCollection[] executeQuery(String query) throws Exception {
                ArrayList<Exception> exceptions = new ArrayList<Exception>();
                FeatureCollection[] featureCollection = this.executeQuery(query, exceptions);
                if (!exceptions.isEmpty()) {
                    throw (Exception)exceptions.iterator().next();
                }
                return featureCollection;
            }

            @Override
            public void executeUpdate(String update, FeatureCollection fcToSave, boolean saveCalculatedAttributes, Style currentStyle) throws Exception {
                ReaderWriterFileDataSource.this.writer.write(fcToSave, ReaderWriterFileDataSource.this.getWriterDriverProperties());
            }

            @Override
            public void executeUpdate(String query, FeatureCollection fcToSave, boolean saveCalculatedAttributes, Style currentStyle, IProjection proj) throws Exception {
                this.executeUpdate(query, fcToSave, saveCalculatedAttributes, currentStyle);
            }

            @Override
            public void executeUpdate(String query, FeatureCollection fcToSave, boolean saveCalculatedAttributes, Style currentStyle, IProjection proj, TaskMonitor monitor) throws Exception {
                this.executeUpdate(query, fcToSave, saveCalculatedAttributes, currentStyle);
            }

            @Override
            public void close() {
            }

            @Override
            public List<AbstractDataSource> getDataSources() {
                return null;
            }
        };
    }

    protected DriverProperties getReaderDriverProperties() {
        return this.getDriverProperties();
    }

    protected DriverProperties getWriterDriverProperties() {
        return this.getDriverProperties();
    }

    private DriverProperties getDriverProperties() {
        DriverProperties properties = new DriverProperties();
        properties.putAll(this.getProperties());
        return properties;
    }
}

