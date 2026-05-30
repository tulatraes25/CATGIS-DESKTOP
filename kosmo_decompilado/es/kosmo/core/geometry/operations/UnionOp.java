/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.operation.linemerge.LineMerger
 */
package es.kosmo.core.geometry.operations;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.task.TaskMonitor;
import es.kosmo.core.geometry.operations.GeometryOp;
import es.kosmo.core.geometry.operations.GeometryOpException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UnionOp
implements GeometryOp<Feature> {
    protected List<Feature> selectedFeats;
    protected Feature sourceFeature;
    protected FeatureSchema resultSchema;
    protected boolean dissolveResult;
    protected Feature result;

    public UnionOp(List<Feature> features, Feature attrFeature, FeatureSchema schema, boolean dissolveGeometries) {
        this.selectedFeats = features;
        this.sourceFeature = attrFeature;
        this.resultSchema = schema;
        this.dissolveResult = dissolveGeometries;
    }

    @Override
    public void executeOperation(TaskMonitor monitor) throws GeometryOpException {
        this.result = this.combineFeatures(this.selectedFeats, this.resultSchema);
    }

    @Override
    public void dispose() {
        this.selectedFeats = null;
        this.sourceFeature = null;
        this.resultSchema = null;
    }

    protected Feature combineFeatures(List<Feature> selectedFeatures, FeatureSchema featureSchema) {
        List<Geometry> geometries = FeatureUtil.toGeometries(selectedFeatures);
        MultiPoint geom = null;
        int geometryType = featureSchema.getGeometryType();
        switch (geometryType) {
            case 1: 
            case 8: {
                ArrayList<Point> points = new ArrayList<Point>();
                for (Geometry element : geometries) {
                    if (element instanceof MultiPoint) {
                        MultiPoint mp = (MultiPoint)element;
                        int i = 0;
                        while (i < mp.getNumGeometries()) {
                            points.add((Point)mp.getGeometryN(i));
                            ++i;
                        }
                        continue;
                    }
                    points.add((Point)element);
                }
                geom = geomFac.createMultiPoint(points.toArray(new Point[points.size()]));
                if (geometryType != 1 || geom.getNumGeometries() != 1) break;
                geom = geom.getGeometryN(0);
                break;
            }
            case 2: 
            case 3: {
                ArrayList<LineString> lines = new ArrayList<LineString>();
                for (Geometry element : geometries) {
                    if (element instanceof MultiLineString) {
                        MultiLineString ml = (MultiLineString)element;
                        int i = 0;
                        while (i < ml.getNumGeometries()) {
                            lines.add((LineString)ml.getGeometryN(i));
                            ++i;
                        }
                        continue;
                    }
                    lines.add((LineString)element);
                }
                if (this.dissolveResult) {
                    LineMerger merger = new LineMerger();
                    merger.add(lines);
                    geom = geomFac.createMultiLineString(GeometryFactory.toLineStringArray((Collection)merger.getMergedLineStrings()));
                } else {
                    geom = geomFac.createMultiLineString(lines.toArray(new LineString[lines.size()]));
                }
                if (geometryType != 3 || geom.getNumGeometries() != 1) break;
                geom = geom.getGeometryN(0);
                break;
            }
            case 4: 
            case 5: {
                ArrayList<Polygon> polygons = new ArrayList<Polygon>();
                for (Geometry element : geometries) {
                    if (element instanceof MultiPolygon) {
                        MultiPolygon mp = (MultiPolygon)element;
                        int i = 0;
                        while (i < mp.getNumGeometries()) {
                            polygons.add((Polygon)mp.getGeometryN(i));
                            ++i;
                        }
                        continue;
                    }
                    polygons.add((Polygon)element);
                }
                if (this.dissolveResult) {
                    GeometryCollection gc = geomFac.createGeometryCollection(polygons.toArray(new Geometry[0]));
                    geom = gc.buffer(0.0);
                } else {
                    geom = geomFac.createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
                }
                if (geometryType != 5 || geom.getNumGeometries() != 1) break;
                geom = geom.getGeometryN(0);
            }
        }
        return this.fillFeatureAttributes((Geometry)geom, featureSchema, this.sourceFeature);
    }

    protected Feature fillFeatureAttributes(Geometry geom, FeatureSchema schema, Feature copyFeat) {
        if (copyFeat == null) {
            Feature feat = FeatureUtil.toFeature(geom, schema);
            return feat;
        }
        Feature feat = FeatureUtil.copyFeature(schema, copyFeat);
        feat.setID(FeatureUtil.nextID());
        feat.setGeometry(geom);
        return feat;
    }

    @Override
    public Feature getResults() {
        return this.result;
    }

    public Feature[] getErrors() {
        return null;
    }
}

