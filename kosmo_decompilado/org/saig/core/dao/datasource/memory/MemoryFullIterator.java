/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.memory;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.ILayerIterator;
import java.util.ArrayList;
import java.util.List;
import org.saig.core.filter.Filter;

public class MemoryFullIterator
implements ILayerIterator {
    private FeatureDataset featureDataset;
    private int cursor;
    private int size = -1;

    public MemoryFullIterator(FeatureDataset features) {
        this.featureDataset = features;
        this.cursor = 0;
    }

    @Override
    public Feature absolute(int n) throws Exception {
        this.cursor = n - 1;
        return this.featureDataset.getFeature(this.cursor);
    }

    @Override
    public Feature backward() throws Exception {
        --this.cursor;
        return this.featureDataset.getFeature(this.cursor);
    }

    @Override
    public List<Feature> backward(int n) throws Exception {
        ArrayList<Feature> resultado = new ArrayList<Feature>();
        int i = 0;
        while (i < n) {
            --this.cursor;
            resultado.add(this.featureDataset.getFeature(this.cursor));
            ++i;
        }
        return resultado;
    }

    @Override
    public List<Feature> backward_all() throws Exception {
        int originalCursor;
        ArrayList<Feature> resultado = new ArrayList<Feature>();
        int i = originalCursor = this.cursor;
        while (i <= 0) {
            --this.cursor;
            resultado.add(this.featureDataset.getFeature(this.cursor));
            --i;
        }
        return resultado;
    }

    @Override
    public void close() throws Exception {
        this.featureDataset = null;
    }

    @Override
    public Feature first() throws Exception {
        this.cursor = 0;
        return this.featureDataset.getFeature(this.cursor);
    }

    @Override
    public Feature forward() throws Exception {
        ++this.cursor;
        return this.featureDataset.getFeature(this.cursor);
    }

    @Override
    public List<Feature> forward(int n) throws Exception {
        ArrayList<Feature> resultado = new ArrayList<Feature>();
        int i = 0;
        while (i < n && (long)this.cursor < this.size() - 1L) {
            ++this.cursor;
            resultado.add(this.featureDataset.getFeature(this.cursor));
            ++i;
        }
        return resultado;
    }

    @Override
    public List<Feature> forward_all() throws Exception {
        ArrayList<Feature> resultado = new ArrayList<Feature>();
        int i = this.cursor;
        while ((long)i < this.size()) {
            ++this.cursor;
            resultado.add(this.featureDataset.getFeature(this.cursor));
            ++i;
        }
        return resultado;
    }

    @Override
    public Feature last() throws Exception {
        this.cursor = ((Number)this.size()).intValue() - 1;
        return this.featureDataset.getFeature(this.cursor);
    }

    @Override
    public Feature next() throws Exception {
        ++this.cursor;
        return this.featureDataset.getFeature(this.cursor);
    }

    @Override
    public void open() throws Exception {
        this.cursor = 0;
    }

    @Override
    public Feature prior() throws Exception {
        return this.backward();
    }

    @Override
    public Feature relative(int n) throws Exception {
        this.cursor += n;
        return this.featureDataset.getFeature(this.cursor);
    }

    @Override
    public long size() throws Exception {
        if (this.size == -1) {
            this.size = this.featureDataset.size();
        }
        return this.size;
    }

    public void sort(String fieldOrdered, Filter filter) throws Exception {
        List<Feature> featList = this.featureDataset.getByAttribute(null, null, fieldOrdered, filter);
        FeatureDataset featDataset = new FeatureDataset(featList, this.featureDataset.getFeatureSchema());
        this.close();
        this.open();
        this.featureDataset = featDataset;
    }
}

