/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.filedatasource.shape.iterators;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.ILayerIterator;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeConnection;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.filter.Filter;

public class ShapeFullIterator
implements ILayerIterator {
    private static final Logger LOGGER = Logger.getLogger(ShapeFullIterator.class);
    private ShapeConnection con = null;
    private ShapeFileDataSource ds = null;
    private int index = -1;
    private int size = -1;

    public ShapeFullIterator(ShapeFileDataSource ds) {
        this.ds = ds;
        this.con = ds.getConnection();
        try {
            this.open();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    @Override
    public Feature absolute(int n) throws Exception {
        this.index = n - 1;
        return this.con.readFeature(this.index, this.con.getShape(this.index), true, this.ds.getSchema());
    }

    @Override
    public Feature backward() throws Exception {
        --this.index;
        return this.con.readFeature(this.index, this.con.getShape(this.index), true, this.ds.getSchema());
    }

    @Override
    public List<Feature> backward(int n) throws Exception {
        ArrayList<Feature> resultado = new ArrayList<Feature>();
        int i = 0;
        while (i < n) {
            --this.index;
            resultado.add(this.con.readFeature(this.index, this.con.getShape(this.index), true, this.ds.getSchema()));
            ++i;
        }
        return resultado;
    }

    @Override
    public List<Feature> backward_all() throws Exception {
        int originalCursor;
        ArrayList<Feature> resultado = new ArrayList<Feature>();
        int i = originalCursor = this.index;
        while (i <= 0) {
            --this.index;
            resultado.add(this.con.readFeature(this.index, this.con.getShape(this.index), true, this.ds.getSchema()));
            --i;
        }
        return resultado;
    }

    @Override
    public void close() throws Exception {
        this.con.close();
    }

    @Override
    public Feature first() throws Exception {
        this.index = 0;
        return this.con.readFeature(this.index, this.con.getShape(this.index), true, this.ds.getSchema());
    }

    @Override
    public Feature forward() throws Exception {
        ++this.index;
        return this.con.readFeature(this.index, this.con.getShape(this.index), true, this.ds.getSchema());
    }

    @Override
    public List<Feature> forward(int n) throws Exception {
        ArrayList<Feature> resultado = new ArrayList<Feature>();
        int i = 0;
        while (i < n && (long)this.index < this.size() - 1L) {
            ++this.index;
            resultado.add(this.con.readFeature(this.index, this.con.getShape(this.index), true, this.ds.getSchema()));
            ++i;
        }
        return resultado;
    }

    @Override
    public List<Feature> forward_all() throws Exception {
        ArrayList<Feature> resultado = new ArrayList<Feature>();
        int i = this.index;
        while ((long)i < this.size()) {
            ++this.index;
            resultado.add(this.con.readFeature(this.index, this.con.getShape(this.index), true, this.ds.getSchema()));
            ++i;
        }
        return resultado;
    }

    @Override
    public Feature last() throws Exception {
        this.index = ((Number)this.size()).intValue() - 1;
        return this.con.readFeature(this.index, this.con.getShape(this.index), true, this.ds.getSchema());
    }

    @Override
    public Feature next() throws Exception {
        ++this.index;
        return this.con.readFeature(this.index, this.con.getShape(this.index), true, this.ds.getSchema());
    }

    @Override
    public void open() throws Exception {
        this.con.open();
        this.index = 0;
    }

    @Override
    public Feature prior() throws Exception {
        return this.backward();
    }

    @Override
    public Feature relative(int n) throws Exception {
        this.index += n;
        return this.con.readFeature(this.index, this.con.getShape(this.index), true, this.ds.getSchema());
    }

    @Override
    public long size() throws Exception {
        if (this.size == -1) {
            this.size = this.ds.size();
        }
        return this.size;
    }

    public void sort(String fieldOrdered, Filter filter) throws Exception {
        List<Feature> featList = this.ds.getByAttribute(null, null, fieldOrdered, filter);
        FeatureDataset featDataset = new FeatureDataset(featList, this.ds.getSchema());
        this.close();
        this.open();
    }
}

