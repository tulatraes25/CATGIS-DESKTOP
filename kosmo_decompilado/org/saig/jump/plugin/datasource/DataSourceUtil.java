/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class DataSourceUtil {
    public static final Logger LOGGER = Logger.getLogger(DataSourceUtil.class);

    public static FeatureCollection[] classifyFeatures(FeatureCollection sourceFc) throws Exception {
        return DataSourceUtil.classifyFeatures(sourceFc, "_point", "_line", "_polygon");
    }

    public static FeatureCollection[] classifyFeatures(FeatureCollection sourceFc, String pointFcSuffix, String lineFcSuffix, String polygonFcSuffix) throws Exception {
        boolean hasMultiPoints = false;
        boolean hasMultiLines = false;
        boolean hasMultiPolygons = false;
        FeatureDataset pointsFc = new FeatureDataset((FeatureSchema)sourceFc.getFeatureSchema().clone());
        FeatureDataset linesFc = new FeatureDataset((FeatureSchema)sourceFc.getFeatureSchema().clone());
        FeatureDataset polygonsFc = new FeatureDataset((FeatureSchema)sourceFc.getFeatureSchema().clone());
        FeatureIterator it = null;
        try {
            it = sourceFc.iterator();
            while (it.hasNext()) {
                Feature feat = it.next();
                Geometry geom = feat.getGeometry();
                if (geom instanceof Point) {
                    pointsFc.add(feat);
                    continue;
                }
                if (geom instanceof MultiPoint) {
                    hasMultiPoints = true;
                    pointsFc.add(feat);
                    continue;
                }
                if (geom instanceof LineString) {
                    linesFc.add(feat);
                    continue;
                }
                if (geom instanceof MultiLineString) {
                    hasMultiLines = true;
                    linesFc.add(feat);
                    continue;
                }
                if (geom instanceof Polygon) {
                    polygonsFc.add(feat);
                    continue;
                }
                if (geom instanceof MultiPolygon) {
                    hasMultiPolygons = true;
                    polygonsFc.add(feat);
                    continue;
                }
                LOGGER.warn((Object)I18N.getString("org.saig.jump.plugin.datasource.DataSourceUtil.Empty-geometry"));
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        pointsFc.getFeatureSchema().setGeometryType(hasMultiPoints ? 8 : 1);
        linesFc.getFeatureSchema().setGeometryType(hasMultiLines ? 2 : 3);
        polygonsFc.getFeatureSchema().setGeometryType(hasMultiPolygons ? 4 : 5);
        pointsFc.set3d(sourceFc.is3d());
        linesFc.set3d(sourceFc.is3d());
        polygonsFc.set3d(sourceFc.is3d());
        pointsFc.setName(String.valueOf(sourceFc.getName()) + pointFcSuffix);
        linesFc.setName(String.valueOf(sourceFc.getName()) + lineFcSuffix);
        polygonsFc.setName(String.valueOf(sourceFc.getName()) + polygonFcSuffix);
        return new FeatureCollection[]{pointsFc, linesFc, polygonsFc};
    }
}

