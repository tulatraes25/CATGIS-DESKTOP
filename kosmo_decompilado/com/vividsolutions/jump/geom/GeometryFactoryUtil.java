/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryCollectionIterator
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.Polygon
 */
package com.vividsolutions.jump.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryCollectionIterator;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.List;

public class GeometryFactoryUtil {
    public static Geometry buildGeometry(Geometry geom, int dimension) {
        GeometryFactory factory = new GeometryFactory(geom.getPrecisionModel(), geom.getSRID());
        if (geom instanceof GeometryCollection) {
            List<Geometry> geomList = GeometryFactoryUtil.dimensionFilter((GeometryCollection)geom, dimension);
            if (geomList.isEmpty()) {
                return GeometryFactoryUtil.getEmptyDimensionalGeometry(factory, dimension);
            }
            return factory.buildGeometry(geomList);
        }
        if (geom.getDimension() == dimension) {
            return geom;
        }
        return GeometryFactoryUtil.getEmptyDimensionalGeometry(factory, dimension);
    }

    public static List<Geometry> dimensionFilter(GeometryCollection gc, int dimension) {
        ArrayList<Geometry> geomList = new ArrayList<Geometry>();
        GeometryCollectionIterator i = new GeometryCollectionIterator((Geometry)gc);
        while (i.hasNext()) {
            Geometry g = (Geometry)i.next();
            if (g instanceof GeometryCollection || g.getDimension() != dimension) continue;
            geomList.add((Geometry)g.clone());
        }
        return geomList;
    }

    public static Geometry getEmptyDimensionalGeometry(GeometryFactory factory, int dimension) {
        switch (dimension) {
            case 0: {
                return factory.createMultiPoint(new Coordinate[0]);
            }
            case 1: {
                return factory.createMultiLineString(new LineString[0]);
            }
            case 2: {
                return factory.createMultiPolygon(new Polygon[0]);
            }
        }
        return factory.createGeometryCollection(new Geometry[0]);
    }
}

