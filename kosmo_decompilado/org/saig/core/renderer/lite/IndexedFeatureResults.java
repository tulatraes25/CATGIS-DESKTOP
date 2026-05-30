/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.index.strtree.STRtree
 *  org.geotools.data.FeatureReader
 *  org.geotools.data.FeatureResults
 *  org.geotools.feature.Feature
 *  org.geotools.feature.FeatureCollection
 *  org.geotools.feature.FeatureCollections
 *  org.geotools.feature.FeatureType
 *  org.geotools.feature.IllegalAttributeException
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

public class IndexedFeatureResults
implements FeatureResults {
    STRtree index = new STRtree();
    FeatureType schema;
    Envelope bounds;
    int count;
    private Envelope queryBounds;

    public IndexedFeatureResults(FeatureResults results) throws IOException, IllegalAttributeException {
        this.schema = results.getSchema();
        FeatureReader reader = null;
        this.bounds = new Envelope();
        this.count = 0;
        try {
            reader = results.reader();
            while (reader.hasNext()) {
                Feature f = reader.next();
                Envelope env = f.getDefaultGeometry().getEnvelopeInternal();
                this.bounds.expandToInclude(env);
                ++this.count;
                this.index.insert(env, (Object)f);
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public FeatureType getSchema() throws IOException {
        return this.schema;
    }

    public FeatureReader reader(Envelope envelope) throws IOException {
        List results = this.index.query(envelope);
        final Iterator resultsIterator = results.iterator();
        return new FeatureReader(){

            public FeatureType getFeatureType() {
                return IndexedFeatureResults.this.schema;
            }

            public Feature next() throws IOException, IllegalAttributeException, NoSuchElementException {
                return (Feature)resultsIterator.next();
            }

            public boolean hasNext() throws IOException {
                return resultsIterator.hasNext();
            }

            public void close() throws IOException {
            }
        };
    }

    public Envelope getBounds() {
        return this.bounds;
    }

    public int getCount() throws IOException {
        return this.count;
    }

    public FeatureCollection collection() throws IOException {
        FeatureCollection fc = FeatureCollections.newCollection();
        List results = this.index.query(this.bounds);
        Iterator it = results.iterator();
        while (it.hasNext()) {
            fc.add(it.next());
        }
        return fc;
    }

    public FeatureReader reader() throws IOException {
        if (this.queryBounds != null) {
            return this.reader(this.queryBounds);
        }
        return this.reader(this.bounds);
    }

    public void setQueryBounds(Envelope queryBounds) {
        this.queryBounds = queryBounds;
    }
}

