/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.util.feature;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;

public class FeatureStatistics {
    private static final Logger LOGGER = Logger.getLogger(FeatureStatistics.class);

    public static double[] minMaxValue(FeatureCollection fc, String col) {
        double[] minMax = new double[]{0.0, 0.0};
        int adjDistanceIndex = -1;
        if (fc.getFeatureSchema().getAttribute(col) == null) {
            return minMax;
        }
        FeatureIterator itFeatures = null;
        try {
            try {
                itFeatures = fc.iterator();
                int cont = 0;
                while (itFeatures.hasNext()) {
                    Feature currentFeat = itFeatures.next();
                    if (adjDistanceIndex == -1) {
                        adjDistanceIndex = currentFeat.getSchema().getAttributeIndex(col);
                    }
                    double adjDistance = currentFeat.getDouble(adjDistanceIndex);
                    if (cont == 0 || adjDistance < minMax[0]) {
                        minMax[0] = adjDistance;
                    }
                    if (cont == 0 || adjDistance > minMax[1]) {
                        minMax[1] = adjDistance;
                    }
                    ++cont;
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (itFeatures != null) {
                    itFeatures.close();
                }
            }
        }
        finally {
            if (itFeatures != null) {
                itFeatures.close();
            }
        }
        return minMax;
    }
}

