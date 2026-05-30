/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.feature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import java.util.Collection;
import java.util.Iterator;

public class FeatureDatasetFactory {
    public static FeatureCollection createFromGeometry(Collection<Geometry> geoms) throws Exception {
        FeatureSchema featureSchema = new FeatureSchema();
        featureSchema.addAttribute("GID", AttributeType.INTEGER, Boolean.TRUE);
        featureSchema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        FeatureDataset fc = new FeatureDataset(featureSchema);
        Iterator<Geometry> i = geoms.iterator();
        while (i.hasNext()) {
            Feature feature = FeatureUtil.toFeature(i.next(), fc.getFeatureSchema());
            fc.addWithNewKey(feature);
        }
        return fc;
    }

    public static FeatureDataset createFromGeometryWithLength(Collection<Geometry> geoms, String attrName) throws Exception {
        FeatureSchema featureSchema = new FeatureSchema();
        featureSchema.addAttribute("GID", AttributeType.INTEGER, Boolean.TRUE);
        featureSchema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        featureSchema.addAttribute(attrName, AttributeType.DOUBLE);
        FeatureDataset fc = new FeatureDataset(featureSchema);
        for (Geometry g : geoms) {
            Feature feature = FeatureUtil.toFeature(g, fc.getFeatureSchema());
            feature.setAttribute(attrName, (Object)new Double(g.getLength()));
            fc.addWithNewKey(feature);
        }
        return fc;
    }
}

