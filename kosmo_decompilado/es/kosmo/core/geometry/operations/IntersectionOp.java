/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineSegment
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.Point
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 */
package es.kosmo.core.geometry.operations;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.algorithm.LengthSubstring;
import com.vividsolutions.jump.algorithm.LengthToPoint;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.geom.LineSegmentUtil;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.plugin.edit.SegmentsExtracter;
import es.kosmo.core.geometry.operations.GeometryOp;
import es.kosmo.core.geometry.operations.GeometryOpException;
import es.kosmo.core.geometry.operations.PrimaryKeyWrapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.Utils;
import org.saig.jump.widgets.util.DialogFactory;

public class IntersectionOp
implements GeometryOp<FeatureCollection> {
    private static final Logger LOGGER = Logger.getLogger(IntersectionOp.class);
    public static final String DEFAULT_OLD_PK_ATTR_NAME = "OLD_GID";
    private static final String ERROR_PK_ATTR_NAME = "id";
    private static final String ERROR_FK1_ATTR_NAME = "fk_1";
    private static final String ERROR_FK2_ATTR_NAME = "fk_2";
    private static final String ERROR_DIFFERENCE_ATTR_NAME = "dif";
    protected FeatureCollection fcSource;
    protected FeatureDataset fcFinal;
    protected FeatureDataset fcErrorPoints;
    protected double increaseLineSizeValue;
    protected Double zInterpolationTolerance;
    protected boolean generateErrorFeatureCollection;

    public IntersectionOp(FeatureCollection fc, double increaseLineSize, Double zTolerance, boolean generateErrorFc) {
        this.fcSource = fc;
        this.increaseLineSizeValue = increaseLineSize;
        this.zInterpolationTolerance = zTolerance;
        this.generateErrorFeatureCollection = generateErrorFc;
    }

    /*
     * Unable to fully structure code
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void executeOperation(TaskMonitor monitor) throws GeometryOpException {
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.topology.IntersectPlugIn.Loading-data")) + "...");
        try {
            this.fcSource = this.explodeSourceFeatureCollection(monitor, this.fcSource);
        }
        catch (Exception e) {
            throw new GeometryOpException(String.valueOf(I18N.getString("es.kosmo.core.geometry.operations.IntersectionOp.Error-executing-intersection-operation")) + ": " + e.getMessage(), e);
        }
        try {
            numElements = this.fcSource.size();
        }
        catch (Exception e) {
            throw new GeometryOpException(String.valueOf(I18N.getString("es.kosmo.core.geometry.operations.IntersectionOp.Error-executing-intersection-operation")) + ": " + e.getMessage(), e);
        }
        pointMap = new HashMap<Object, HashSet<E>>();
        processedKeySet = new HashSet<Object>();
        oldPKAttrName = this.getOldPKAttrName(this.fcSource.getFeatureSchema());
        newFS = this.generateNewFeatureSchema(this.fcSource.getFeatureSchema(), oldPKAttrName);
        errorFS = this.generateErrorFeatureSchema(this.fcSource.getFeatureSchema());
        this.fcFinal = new FeatureDataset(newFS);
        this.fcErrorPoints = new FeatureDataset(errorFS);
        contador = 1;
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.topology.IntersectPlugIn.Generating-net")) + "...");
        itFeats = null;
        try {
            try {
                itFeats = this.fcSource.iterator();
                if (true) ** GOTO lbl134
                do {
                    currentFeat = itFeats.next();
                    currentGeom = currentFeat.getGeometry();
                    if (this.increaseLineSizeValue > 0.0) {
                        currentGeom = Utils.extendLine((LineString)currentGeom, this.increaseLineSizeValue);
                    }
                    currentGeom.geometryChanged();
                    queryEnvelope = currentGeom.getEnvelopeInternal();
                    currentPK = currentFeat.getPrimaryKey();
                    v0 = mapKey = currentPK != null ? currentPK : new PrimaryKeyWrapper(currentFeat.getID());
                    if (pointMap.get(mapKey) == null) {
                        pointMap.put(mapKey, new HashSet<E>());
                    }
                    processedPointSet = (Set)pointMap.get(mapKey);
                    processedKeySet.add(mapKey);
                    candidateFeats = this.fcSource.query(queryEnvelope);
                    crossingLinesTmp = new ArrayList<Feature>();
                    i = 0;
                    while (i < candidateFeats.size() && !monitor.isCancelRequested()) {
                        currentCandidate = candidateFeats.get(i);
                        candidatePK = currentCandidate.getPrimaryKey();
                        v1 = candidateMapKey = candidatePK != null ? candidatePK : new PrimaryKeyWrapper(currentCandidate.getID());
                        if (!processedKeySet.contains(candidateMapKey)) {
                            candidateGeom = currentCandidate.getGeometry();
                            if (this.increaseLineSizeValue > 0.0) {
                                candidateGeom = Utils.extendLine((LineString)candidateGeom, this.increaseLineSizeValue);
                            }
                            candidateGeom.geometryChanged();
                            intersection = currentGeom.intersection(candidateGeom);
                            if (!intersection.isEmpty()) {
                                crossingLinesTmp.add(currentCandidate);
                                if (pointMap.get(candidateMapKey) == null) {
                                    pointMap.put(candidateMapKey, new HashSet<E>());
                                }
                                candidatePointSet = (Set)pointMap.get(candidateMapKey);
                                j = 0;
                                while (j < intersection.getNumGeometries()) {
                                    geom_inters_j = intersection.getGeometryN(j);
                                    if (geom_inters_j instanceof Point) {
                                        processedPointSet.add((Point)geom_inters_j);
                                        candidatePointSet.add((Point)geom_inters_j.clone());
                                    }
                                    ++j;
                                }
                            }
                        }
                        ++i;
                    }
                    if (!monitor.isCancelRequested()) {
                        if (CollectionUtils.isEmpty((Collection)processedPointSet)) {
                            simpleLines = this.toSimpleLineStrings(currentGeom);
                            for (LineString currentLineString : simpleLines) {
                                tempFeat = FeatureUtil.copyFeature(newFS, currentFeat);
                                tempFeat.setAttribute(oldPKAttrName, currentPK);
                                tempFeat.setGeometry((Geometry)currentLineString);
                                this.fcFinal.addWithNewKey(tempFeat);
                            }
                        } else {
                            geometrias = new ArrayList<LineString>();
                            geometrias.addAll(this.toSimpleLineStrings(currentGeom));
                            for (Point currentPoint : processedPointSet) {
                                cortado = false;
                                i = 0;
                                while (i < geometrias.size() && !cortado) {
                                    geom_i = (LineString)geometrias.get(i);
                                    if (geom_i != null && !geom_i.intersection(currentPoint.buffer(1.0E-6)).isEmpty()) {
                                        lineas_cortadas = this.split(geom_i, currentPoint.getCoordinate(), true);
                                        zOfCutPoint = lineas_cortadas[1].getPointN((int)0).getCoordinate().z;
                                        linesAux = this.getIntersectionLine(crossingLinesTmp, currentPoint);
                                        if (this.zInterpolationTolerance != null) {
                                            for (Feature lineFeatAux : linesAux) {
                                                lsAux = (LineString)lineFeatAux.getGeometry();
                                                segment = this.getSegment(lsAux, currentPoint);
                                                zInterpolated = this.interpolateZInSegment(segment, currentPoint);
                                                dif = Math.abs(zOfCutPoint - zInterpolated);
                                                if (!(dif > this.zInterpolationTolerance)) continue;
                                                errorPointFeat = new BasicFeature(errorFS);
                                                errorPointFeat.setAttribute("fk_1", currentPK);
                                                errorPointFeat.setAttribute("fk_2", lineFeatAux.getPrimaryKey());
                                                errorPointFeat.setAttribute("dif", (Object)new Double(dif));
                                                errorPointFeat.setGeometry((Geometry)currentPoint);
                                                if (!this.generateErrorFeatureCollection) continue;
                                                this.fcErrorPoints.add(errorPointFeat);
                                            }
                                        }
                                        cortado = true;
                                        geometrias.remove(geom_i);
                                        j = 0;
                                        while (j < lineas_cortadas.length) {
                                            lineString = lineas_cortadas[j];
                                            if (lineString.isValid()) {
                                                geometrias.add(lineas_cortadas[j]);
                                            }
                                            ++j;
                                        }
                                    }
                                    ++i;
                                }
                            }
                            i = 0;
                            while (i < geometrias.size()) {
                                geom_temp = (Geometry)geometrias.get(i);
                                tempFeat = FeatureUtil.copyFeature(newFS, currentFeat);
                                tempFeat.setAttribute(oldPKAttrName, currentPK);
                                tempFeat.setGeometry(geom_temp);
                                this.fcFinal.addWithNewKey(tempFeat);
                                ++i;
                            }
                        }
                        monitor.report(contador++, numElements, I18N.getString("org.saig.jump.plugin.utils.topology.IntersectPlugIn.Features-processed"));
                    }
lbl134:
                    // 4 sources

                    if (monitor.isCancelRequested()) return;
                } while (itFeats.hasNext());
                return;
            }
            catch (Exception ex) {
                IntersectionOp.LOGGER.error((Object)"", (Throwable)ex);
                throw new GeometryOpException(String.valueOf(I18N.getString("es.kosmo.core.geometry.operations.IntersectionOp.Error-executing-intersection-operation")) + ": " + ex.getMessage(), ex);
            }
        }
        finally {
            if (itFeats != null) {
                itFeats.close();
            }
        }
    }

    protected FeatureSchema generateErrorFeatureSchema(FeatureSchema featureSchema) {
        AttributeType pkAttrType = featureSchema.getPrimaryKey().getType();
        FeatureSchema fsErrorPoints = new FeatureSchema();
        fsErrorPoints.addAttribute(ERROR_PK_ATTR_NAME, AttributeType.INTEGER, true);
        fsErrorPoints.addAttribute(ERROR_FK1_ATTR_NAME, pkAttrType);
        fsErrorPoints.addAttribute(ERROR_FK2_ATTR_NAME, pkAttrType);
        fsErrorPoints.addAttribute(ERROR_DIFFERENCE_ATTR_NAME, AttributeType.DOUBLE);
        fsErrorPoints.addAttribute("geometry", AttributeType.GEOMETRY);
        fsErrorPoints.setGeometryType(1);
        return fsErrorPoints;
    }

    protected FeatureSchema generateNewFeatureSchema(FeatureSchema featureSchema, String oldPkAttrName) {
        FeatureSchema newFS = (FeatureSchema)featureSchema.clone();
        AttributeType oldPKAttrType = newFS.getPrimaryKey().getType();
        newFS.addAttribute(oldPkAttrName, oldPKAttrType);
        newFS.setGeometryType(featureSchema.getGeometryType());
        return newFS;
    }

    protected String getOldPKAttrName(FeatureSchema featureSchema) {
        List<String> attrNames = featureSchema.getAttributeNames();
        String oldPkAttrName = DEFAULT_OLD_PK_ATTR_NAME;
        boolean found = false;
        if (attrNames.contains(oldPkAttrName)) {
            int i = 1;
            while (!found) {
                if (!attrNames.contains(String.valueOf(oldPkAttrName) + "_" + i)) {
                    oldPkAttrName = String.valueOf(oldPkAttrName) + "_" + i;
                    found = true;
                }
                ++i;
            }
        }
        return oldPkAttrName;
    }

    protected Collection<LineString> toSimpleLineStrings(Geometry geom) {
        List<LineString> solution = null;
        ArrayList<LineString> tempLines = new ArrayList<LineString>();
        tempLines.add((LineString)geom);
        Geometry nodedGeometry = this.nodeLines(tempLines);
        SegmentsExtracter extracter = new SegmentsExtracter();
        extracter.add(nodedGeometry);
        Collection<LineSegment> uniqueFSList = extracter.getSegments();
        solution = this.toLineStrings(uniqueFSList);
        return solution;
    }

    protected Geometry nodeLines(Collection<LineString> lines) {
        MultiLineString linesGeom = geomFac.createMultiLineString(GeometryFactory.toLineStringArray(lines));
        MultiLineString unionInput = geomFac.createMultiLineString(null);
        Geometry minLine = this.extractPoint(lines);
        if (minLine != null) {
            unionInput = minLine;
        }
        Geometry noded = linesGeom.union((Geometry)unionInput);
        return noded;
    }

    protected Geometry extractPoint(Collection<LineString> lines) {
        Point point = null;
        for (LineString g : lines) {
            if (g.isEmpty()) continue;
            Coordinate p = g.getCoordinate();
            point = g.getFactory().createPoint(p);
        }
        return point;
    }

    protected LineString[] split(LineString lineString, Coordinate target, boolean moveSplitToTarget) {
        LineString[] lineStrings = new LineString[]{LengthSubstring.getSubstring(lineString, 0.0, LengthToPoint.length(lineString, target)), LengthSubstring.getSubstring(lineString, LengthToPoint.length(lineString, target), lineString.getLength())};
        if (moveSplitToTarget) {
            this.last(lineStrings[0]).setCoordinate(target);
            this.first(lineStrings[1]).setCoordinate(target);
        }
        double newZ = this.interpolateZ(lineStrings);
        if (Double.isNaN(this.last((LineString)lineStrings[0]).z)) {
            this.last((LineString)lineStrings[0]).z = newZ;
        }
        if (Double.isNaN(this.first((LineString)lineStrings[1]).z)) {
            this.first((LineString)lineStrings[1]).z = newZ;
        }
        return lineStrings;
    }

    protected double interpolateZ(LineString[] lineStrings) {
        Coordinate a = this.secondToLast(lineStrings[0]);
        Coordinate b = this.last(lineStrings[0]);
        Coordinate c = this.second(lineStrings[1]);
        if (Double.isNaN(a.z)) {
            return Double.NaN;
        }
        if (Double.isNaN(c.z)) {
            return Double.NaN;
        }
        return a.z + (c.z - a.z) * a.distance(b) / (a.distance(b) + b.distance(c));
    }

    protected Coordinate first(LineString lineString) {
        return lineString.getCoordinateN(0);
    }

    protected Coordinate second(LineString lineString) {
        return lineString.getCoordinateN(1);
    }

    protected Coordinate last(LineString lineString) {
        return lineString.getCoordinateN(lineString.getNumPoints() - 1);
    }

    protected Coordinate secondToLast(LineString lineString) {
        return lineString.getCoordinateN(lineString.getNumPoints() - 2);
    }

    protected List<Feature> getIntersectionLine(List<Feature> lines, Point point) {
        ArrayList<Feature> result = new ArrayList<Feature>();
        for (Feature lineFeat : lines) {
            Geometry lineGeom = lineFeat.getGeometry();
            if (!lineGeom.intersects((Geometry)point)) continue;
            result.add(lineFeat);
        }
        return result;
    }

    protected LineString getSegment(LineString line, Point point) {
        int numPoints = line.getNumPoints();
        GeometryFactory gf = new GeometryFactory();
        LineString segmento = null;
        int i = 0;
        while (i < numPoints - 1) {
            segmento = gf.createLineString(new Coordinate[]{line.getPointN(i).getCoordinate(), line.getPointN(i + 1).getCoordinate()});
            if (point.intersects((Geometry)segmento)) {
                return segmento;
            }
            ++i;
        }
        return null;
    }

    private double interpolateZInSegment(LineString segment, Point point) {
        Coordinate a = segment.getStartPoint().getCoordinate();
        Coordinate b = point.getCoordinate();
        Coordinate c = segment.getEndPoint().getCoordinate();
        if (Double.isNaN(a.z)) {
            return Double.NaN;
        }
        if (Double.isNaN(c.z)) {
            return Double.NaN;
        }
        return a.z + (c.z - a.z) * a.distance(b) / (a.distance(b) + b.distance(c));
    }

    @Override
    public FeatureCollection getResults() {
        return this.fcFinal;
    }

    @Override
    public void dispose() {
        this.fcSource = null;
        this.fcFinal = null;
        this.fcErrorPoints = null;
    }

    public FeatureCollection[] getErrors() {
        return new FeatureCollection[]{this.fcErrorPoints};
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected FeatureCollection explodeSourceFeatureCollection(TaskMonitor monitor, FeatureCollection sourceFc) throws Exception {
        FeatureCollection fcNuevo = null;
        if (sourceFc.getFeatureSchema().getGeometryType() != 2) return sourceFc;
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.topology.IntersectPlugIn.Separating-multiple-lines")) + "...");
        FeatureSchema fs = (FeatureSchema)sourceFc.getFeatureSchema().clone();
        fcNuevo = new FeatureDataset(fs);
        int contador = 0;
        int numElements = sourceFc.size();
        FeatureIterator it = null;
        try {
            it = sourceFc.iterator();
            monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.topology.IntersectPlugIn.Separating-multiple-lines")) + "...");
            while (!monitor.isCancelRequested() && it.hasNext()) {
                Feature featOrig = it.next();
                Feature feat = (Feature)featOrig.clone();
                feat.setSchema(fs);
                String featureType = feat.getGeometry().getGeometryType();
                if (featureType != null) {
                    Geometry geom = featOrig.getGeometry();
                    int i = 0;
                    while (i < geom.getNumGeometries()) {
                        feat = (Feature)feat.clone();
                        feat.setGeometry(geom.getGeometryN(i));
                        ((FeatureDataset)fcNuevo).addWithNewKey(feat);
                        ++i;
                    }
                }
                if (contador++ % 100 != 0) continue;
                monitor.report(contador, numElements, I18N.getString("org.saig.jump.plugin.utils.conversion.ExplodeEntitiesPlugIn.Processed-features"));
            }
            if (monitor.isCancelRequested()) return fcNuevo;
            if (fcNuevo.size() < numElements) {
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.utils.conversion.ExplodeEntitiesPlugIn.The-number-of-processed-features-has-been-lesser-than-the-original"), I18N.getString("org.saig.jump.plugin.utils.conversion.ExplodeEntitiesPlugIn.Error-in-the-number-of-precessed-features"));
            }
            fcNuevo = sourceFc;
            return fcNuevo;
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }

    protected List<LineString> toLineStrings(Collection<LineSegment> segments) {
        ArrayList<LineString> lineStringList = new ArrayList<LineString>();
        for (LineSegment seg : segments) {
            LineString ls = LineSegmentUtil.asGeometry(geomFac, seg);
            lineStringList.add(ls);
        }
        return lineStringList;
    }
}

