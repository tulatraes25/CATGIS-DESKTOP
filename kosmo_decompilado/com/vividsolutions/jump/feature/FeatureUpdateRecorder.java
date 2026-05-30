/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.feature;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureUpdate;
import java.util.HashMap;
import java.util.Map;
import org.saig.core.model.feature.FeatureIterator;

public class FeatureUpdateRecorder {
    private Map<Feature, FeatureUpdate> updates = new HashMap<Feature, FeatureUpdate>();
    private int updateCount = 0;

    public void update(Feature origFeat, Feature newFeat) {
        this.updates.put(origFeat, new FeatureUpdate(origFeat, newFeat));
        ++this.updateCount;
    }

    public int getCount() {
        return this.updateCount;
    }

    public FeatureDataset applyUpdates(FeatureCollection fc) throws Exception {
        FeatureDataset fd = new FeatureDataset(fc.getFeatureSchema());
        FeatureIterator i = null;
        try {
            i = fc.iterator();
            while (i.hasNext()) {
                Feature f = i.next();
                FeatureUpdate fu = this.updates.get(f);
                if (fu == null) {
                    fd.add(f);
                    continue;
                }
                fd.add(fu.getNew());
            }
        }
        finally {
            if (i != null) {
                i.close();
            }
        }
        return fd;
    }
}

