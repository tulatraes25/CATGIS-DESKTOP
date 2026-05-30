/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.memory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.saig.core.filter.Filter;
import org.saig.core.model.feature.FeatureIterator;

public class FeatureDatasetIterator
implements FeatureIterator {
    private static final Logger LOGGER = Logger.getLogger(FeatureDatasetIterator.class);
    private FeatureDataset ds;
    private Filter filter;
    private Feature readObject;
    private Envelope layerView;
    private Iterator<Feature> featIterator;
    private Iterator<Feature> newFeatIterator;
    private boolean contains;
    protected boolean ignored;

    public FeatureDatasetIterator(FeatureDataset ds, Filter filter, Envelope envelope) {
        this.ds = ds;
        this.filter = filter;
        this.featIterator = ds.getSpatialIndexCandidatesIterator(envelope);
        this.newFeatIterator = ds.getNewFeatures().iterator();
        this.contains = false;
        if (envelope != null && ds.getEnvelope() != null) {
            this.layerView = new Envelope(envelope.getMinX() - 1.0E-4, envelope.getMaxX() + 2.0E-4, envelope.getMinY() - 1.0E-4, envelope.getMaxY() + 2.0E-4);
            this.contains = envelope.contains(ds.getEnvelope());
        }
    }

    @Override
    public void close() {
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public boolean hasNext() {
        block5: {
            this.readObject = null;
            try {
                if (!this.featIterator.hasNext()) ** GOTO lbl15
                while (this.featIterator.hasNext() && this.readObject == null) {
                    feat = null;
                    feat = this.ignored == false ? this.ds.getRealFeature(this.featIterator.next()) : this.featIterator.next();
                    this.processNext(feat);
                }
                if (this.readObject != null) break block5;
                while (this.newFeatIterator.hasNext() && this.readObject == null) {
                    this.processNext(this.newFeatIterator.next());
                }
                break block5;
lbl-1000:
                // 1 sources

                {
                    this.processNext(this.newFeatIterator.next());
lbl15:
                    // 2 sources

                    ** while (this.newFeatIterator.hasNext() && this.readObject == null)
                }
lbl16:
                // 1 sources

            }
            catch (Exception e) {
                FeatureDatasetIterator.LOGGER.error((Object)"", (Throwable)e);
                return false;
            }
        }
        return this.readObject != null;
    }

    private void processNext(Feature feat) {
        if (feat == null) {
            return;
        }
        if (this.layerView != null) {
            if ((this.contains || feat.getGeometry() != null && this.layerView.intersects(feat.getGeometry().getEnvelopeInternal())) && (this.filter == null || this.filter.contains(feat))) {
                this.readObject = feat;
            }
        } else if (this.filter == null || this.filter.contains(feat)) {
            this.readObject = feat;
        }
    }

    @Override
    public Feature next() {
        return this.readObject;
    }

    @Override
    public void close(boolean isCancel) {
        this.close();
    }

    @Override
    public void setIgnoredUpdate(boolean ignored) {
        this.ignored = ignored;
    }
}

