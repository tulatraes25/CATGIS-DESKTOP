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
 */
package es.kosmo.core.utils;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.FeatureSchema;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;

public class FeatureSchemaUtils {
    public static FeatureSchema convertFieldNamesToLowerCase(FeatureSchema sourceSchema) {
        FeatureSchema resultSchema = new FeatureSchema();
        int i = 0;
        while (i < sourceSchema.getAttributeCount()) {
            Attribute attribute = sourceSchema.getAttribute(i);
            if (!(attribute instanceof AttributeCalculate)) {
                resultSchema.addAttribute(attribute.getName().toLowerCase(), attribute.getPublicName().toLowerCase(), attribute.isVisibility(), attribute.getType(), attribute.isPrimaryKey());
            }
            ++i;
        }
        resultSchema.addRelations(sourceSchema.recoverRelations());
        resultSchema.setGeometryType(sourceSchema.getGeometryType());
        return resultSchema;
    }

    public static String getUniqueAttributeName(FeatureSchema schema, String root) {
        String result = root;
        int cont = 1;
        while (schema.hasAttribute(result)) {
            result = String.valueOf(root) + "_" + cont++;
        }
        return result;
    }

    public static Class<?> getGeometryClass(FeatureSchema featureSchema) {
        Class<Point> geometryClass = null;
        switch (featureSchema.getGeometryType()) {
            case 1: {
                geometryClass = Point.class;
                break;
            }
            case 8: {
                geometryClass = MultiPoint.class;
                break;
            }
            case 5: {
                geometryClass = Polygon.class;
                break;
            }
            case 4: {
                geometryClass = MultiPolygon.class;
                break;
            }
            case 3: {
                geometryClass = LineString.class;
                break;
            }
            case 2: {
                geometryClass = MultiLineString.class;
                break;
            }
            default: {
                geometryClass = Geometry.class;
            }
        }
        return geometryClass;
    }

    public static FeatureSchema removeAttribute(FeatureSchema sourceSchema, String attrName) {
        FeatureSchema resultSchema = new FeatureSchema();
        int i = 0;
        while (i < sourceSchema.getAttributeCount()) {
            Attribute attribute = sourceSchema.getAttribute(i);
            if (!attribute.getName().equals(attrName) && !(attribute instanceof AttributeCalculate)) {
                resultSchema.addAttribute(attribute.getName().toLowerCase(), attribute.getPublicName().toLowerCase(), attribute.isVisibility(), attribute.getType(), attribute.isPrimaryKey());
            }
            ++i;
        }
        resultSchema.addRelations(sourceSchema.recoverRelations());
        resultSchema.setGeometryType(sourceSchema.getGeometryType());
        return resultSchema;
    }
}

