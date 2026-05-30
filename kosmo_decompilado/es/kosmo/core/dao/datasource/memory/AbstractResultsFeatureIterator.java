/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.GeometryFactory
 */
package es.kosmo.core.dao.datasource.memory;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import org.saig.core.model.feature.FeatureIterator;

public abstract class AbstractResultsFeatureIterator
implements FeatureIterator {
    protected GeometryFactory geomFact = new GeometryFactory();
    protected static final int DEFAULT_UNKNOW_SIZE = -1;
    protected Feature nextFeat;
    protected boolean initialized;
    protected FeatureSchema schema;

    @Override
    public Feature next() throws Exception {
        return this.nextFeat;
    }

    @Override
    public boolean hasNext() throws Exception {
        if (!this.initialized) {
            this.initialize();
            this.initialized = true;
        }
        this.nextFeat = this.generateNextFeature();
        return this.nextFeat != null;
    }

    protected void initialize() throws Exception {
    }

    protected abstract Feature generateNextFeature() throws Exception;

    public void setSchema(FeatureSchema newSchema) {
        this.schema = newSchema;
    }

    @Override
    public void close() {
        this.nextFeat = null;
    }

    @Override
    public void close(boolean isCancel) {
        this.nextFeat = null;
    }

    @Override
    public void setIgnoredUpdate(boolean ignored) {
    }

    public abstract int size() throws Exception;

    public abstract void reset();
}

