/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateFilter
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.warp;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import org.saig.core.model.feature.FeatureIterator;

public abstract class CoordinateTransform {
    private CoordinateFilter coordinateFilter = new CoordinateFilter(){

        public void filter(Coordinate coordinate) {
            coordinate.setCoordinate(CoordinateTransform.this.transform(coordinate));
        }
    };

    public abstract Coordinate transform(Coordinate var1);

    public FeatureCollection transform(FeatureCollection featureCollection) throws Exception {
        FeatureDataset newCollection = new FeatureDataset(featureCollection.getFeatureSchema());
        FeatureIterator i = null;
        try {
            i = featureCollection.iterator();
            while (i.hasNext()) {
                Feature feature = i.next();
                Geometry newGeometry = this.transform(feature.getGeometry());
                Feature newFeature = feature.clone(false);
                newFeature.setGeometry(newGeometry);
                newCollection.add(newFeature);
            }
        }
        finally {
            if (i != null) {
                i.close();
            }
        }
        return newCollection;
    }

    public Geometry transform(Geometry oldGeometry) {
        Geometry newGeometry = (Geometry)oldGeometry.clone();
        newGeometry.apply(this.coordinateFilter);
        newGeometry.geometryChanged();
        return newGeometry;
    }
}

