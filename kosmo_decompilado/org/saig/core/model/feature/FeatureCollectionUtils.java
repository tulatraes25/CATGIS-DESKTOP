/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.index.strtree.STRtree
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.feature;

import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;

public class FeatureCollectionUtils {
    private static final Logger LOGGER = Logger.getLogger(FeatureCollectionUtils.class);

    public static Quadtree<Feature> createQuadtreeFromFC(FeatureCollection fc) {
        Quadtree<Feature> tree = new Quadtree<Feature>();
        FeatureIterator it = fc.iterator();
        try {
            try {
                while (it.hasNext()) {
                    Feature feat = it.next();
                    tree.insert(feat.getGeometry().getEnvelopeInternal(), feat);
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it != null) {
                    it.close();
                }
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return tree;
    }

    public static STRtree createSTRtreeFromFC(FeatureCollection fc) {
        STRtree tree;
        block9: {
            tree = new STRtree();
            FeatureIterator it = fc.iterator();
            try {
                try {
                    while (it.hasNext()) {
                        Feature feat = it.next();
                        tree.insert(feat.getGeometry().getEnvelopeInternal(), (Object)feat);
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    if (it != null) {
                        it.close();
                    }
                    break block9;
                }
            }
            catch (Throwable throwable) {
                if (it != null) {
                    it.close();
                }
                throw throwable;
            }
            if (it != null) {
                it.close();
            }
        }
        tree.build();
        return tree;
    }
}

