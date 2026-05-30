/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.feature;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.Collection;
import java.util.List;
import org.saig.core.model.feature.FeatureIterator;

public abstract class FeatureCollectionWrapper
implements FeatureCollection {
    protected FeatureCollection fc;

    public FeatureCollectionWrapper(FeatureCollection fc) {
        this.fc = fc;
    }

    public FeatureCollection getUltimateWrappee() {
        FeatureCollection currentWrappee = this.fc;
        while (currentWrappee instanceof FeatureCollectionWrapper) {
            currentWrappee = ((FeatureCollectionWrapper)currentWrappee).fc;
        }
        return currentWrappee;
    }

    public void checkNotWrappingSameClass() {
        Assert.isTrue((!(this.fc instanceof FeatureCollectionWrapper) || !((FeatureCollectionWrapper)this.fc).hasWrapper(this.getClass()) ? 1 : 0) != 0);
    }

    @Override
    public Collection<Feature> remove(Envelope env) throws Exception {
        return this.fc.remove(env);
    }

    public boolean hasWrapper(Class<?> c) {
        Assert.isTrue((boolean)FeatureCollectionWrapper.class.isAssignableFrom(c));
        if (c.isInstance(this)) {
            return true;
        }
        return this.fc instanceof FeatureCollectionWrapper && ((FeatureCollectionWrapper)this.fc).hasWrapper(c);
    }

    public FeatureCollection getWrappee() {
        return this.fc;
    }

    @Override
    public FeatureSchema getFeatureSchema() {
        return this.fc.getFeatureSchema();
    }

    @Override
    public Envelope getEnvelope() throws Exception {
        return this.fc.getEnvelope();
    }

    @Override
    public int size() throws Exception {
        return this.fc.size();
    }

    @Override
    public boolean isEmpty() throws Exception {
        return this.fc.isEmpty();
    }

    @Override
    public List<Feature> getFeatures() {
        return this.fc.getFeatures();
    }

    @Override
    public FeatureIterator iterator() {
        return this.fc.iterator();
    }

    @Override
    public List<Feature> query(Envelope envelope) throws Exception {
        return this.fc.query(envelope);
    }

    @Override
    public FeatureIterator queryIterator(Envelope envelope) {
        return this.fc.queryIterator(envelope);
    }

    @Override
    public void add(Feature feature) throws Exception {
        this.fc.add(feature);
    }

    @Override
    public void remove(Feature feature) throws Exception {
        this.fc.remove(feature);
    }

    @Override
    public void addAll(Collection<Feature> features) throws Exception {
        this.fc.addAll(features);
    }

    @Override
    public void removeAll(Collection<Feature> features) throws Exception {
        this.fc.removeAll(features);
    }

    @Override
    public void clear() throws Exception {
        this.fc.clear();
    }

    @Override
    public Object clone() {
        return this.fc.clone();
    }

    @Override
    public void dispose() {
        this.fc.dispose();
    }
}

