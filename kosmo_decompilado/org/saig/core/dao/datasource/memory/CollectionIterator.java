/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.memory;

import com.vividsolutions.jump.feature.Feature;
import java.util.Collection;
import java.util.Iterator;
import org.saig.core.model.feature.FeatureIterator;

public class CollectionIterator
implements FeatureIterator {
    protected Iterator<Feature> itFeatures = null;

    public CollectionIterator(Collection<Feature> features) {
        this.itFeatures = features.iterator();
    }

    @Override
    public boolean hasNext() throws Exception {
        return this.itFeatures.hasNext();
    }

    @Override
    public Feature next() throws Exception {
        return this.itFeatures.next();
    }

    @Override
    public void close() {
        this.itFeatures = null;
    }

    @Override
    public void close(boolean isCancel) {
        this.itFeatures = null;
    }

    @Override
    public void setIgnoredUpdate(boolean ignored) {
    }
}

