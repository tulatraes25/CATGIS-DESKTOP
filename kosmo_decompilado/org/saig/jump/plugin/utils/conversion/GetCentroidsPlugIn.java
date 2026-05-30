/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.Point
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.utils.conversion;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.AbstractSaveResultsPlugIn;
import org.saig.jump.plugin.datasource.MemoryDataSource;
import org.saig.jump.widgets.utils.conversion.GetCentroidsDialog;

public class GetCentroidsPlugIn
extends AbstractSaveResultsPlugIn
implements ThreadedPlugIn {
    public static final Logger LOGGER = Logger.getLogger(GetCentroidsPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.conversion.GetCentroidsPlugIn.Get-centroids");
    public static final Icon ICON = IconLoader.icon("blank.png");
    private String sourceLayerName;
    private boolean forzarInterior;
    private FeatureSchema schemaCentroids;
    private FeatureSchema schemaErrors;
    private DataSourceQuery queryCentroids;
    private DataSourceQuery queryErrors;
    private String idAttributeName;
    private GeometryFactory gf = new GeometryFactory();

    @Override
    public EnableCheck getCheck() {
        return GetCentroidsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
        solucion.add(checkFactory.createSelectedLayersMustNotBeRasterCheck());
        solucion.add(checkFactory.createSelectedLayerTypeGeometryCheck(new int[]{5, 4, 3, 2}));
        return solucion;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layerable[] selectedLayers = context.getLayerNamePanel().getSelectedLayers();
        Layer sourceLayer = (Layer)selectedLayers[0];
        this.sourceLayerName = sourceLayer.getName();
        FeatureCollection fcOrig = sourceLayer.getUltimateFeatureCollectionWrapper();
        if (fcOrig == null) {
            context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.conversion.GetCentroidsPlugIn.An-error-was-produced-while-processing-selected-layer"));
            return false;
        }
        GetCentroidsDialog dialog = new GetCentroidsDialog(JUMPWorkbench.getFrameInstance(), true, this.sourceLayerName, fcOrig.getFeatureSchema().getGeometryType());
        dialog.setVisible(true);
        if (dialog == null || !dialog.isExitOk()) {
            return false;
        }
        this.forzarInterior = dialog.isForceInsideSelected();
        this.schemaCentroids = (FeatureSchema)fcOrig.getFeatureSchema().clone();
        this.idAttributeName = "ID_ORIG";
        int cont = 0;
        boolean exists = this.schemaCentroids.getAttribute(this.idAttributeName) != null;
        while (exists) {
            this.idAttributeName = "ID_ORIG_" + cont++;
            boolean bl = exists = this.schemaCentroids.getAttribute(this.idAttributeName) != null;
        }
        AttributeType primaryKeyType = this.schemaCentroids.getPrimaryKey().getType();
        this.schemaCentroids.addAttribute(this.idAttributeName, primaryKeyType);
        this.schemaCentroids.setGeometryType(1);
        this.schemaErrors = (FeatureSchema)this.schemaCentroids.clone();
        this.schemaErrors.setGeometryType(fcOrig.getFeatureSchema().getGeometryType());
        this.queryCentroids = dialog.getResultQuery();
        this.queryErrors = dialog.getErrorQuery();
        return this.queryCentroids != null;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.GetCentroidsPlugIn.Getting-centroids")) + "...");
        boolean isMemoryQueryCentroids = this.queryCentroids.getDataSource() instanceof MemoryDataSource;
        boolean isMemoryQueryErrors = false;
        if (this.queryErrors != null) {
            isMemoryQueryErrors = this.queryErrors.getDataSource() instanceof MemoryDataSource;
        }
        boolean error = false;
        Layer sourceLayer = JUMPWorkbench.getLayer(this.sourceLayerName);
        FeatureCollection fcOrig = sourceLayer.getUltimateFeatureCollectionWrapper();
        int id = 0;
        int idError = 0;
        int contador = 0;
        int numElements = fcOrig.size();
        FeatureDataset fcNuevo = new FeatureDataset(this.schemaCentroids);
        fcNuevo.setName(String.valueOf(sourceLayer.getName()) + " - " + I18N.getString("org.saig.jump.plugin.utils.conversion.GetCentroidsPlugIn.Centroids"));
        fcNuevo.set3d(fcOrig.is3d());
        FeatureDataset fcError = new FeatureDataset(this.schemaErrors);
        fcError.setName(String.valueOf(sourceLayer.getName()) + " - " + I18N.getString("org.saig.jump.plugin.utils.conversion.GetCentroidsPlugIn.Centroids-error"));
        fcError.set3d(fcOrig.is3d());
        String primaryKey = fcOrig.getFeatureSchema().getPrimaryKeyName();
        AttributeType primaryKeyType = this.schemaCentroids.getPrimaryKey().getType();
        FeatureIterator it = null;
        try {
            it = fcOrig.iterator();
            while (!monitor.isCancelRequested() && it.hasNext()) {
                Feature featOrig = it.next();
                Feature feat = (Feature)featOrig.clone();
                feat.setSchema(this.schemaCentroids);
                feat.setAttribute(this.idAttributeName, featOrig.getAttribute(primaryKey));
                Geometry geometria = featOrig.getGeometry();
                Object goodValuePK = FeatureUtil.getGoodAttribute(primaryKeyType, id++);
                try {
                    Point centroide;
                    int i;
                    if (geometria.getGeometryType().equals("Polygon")) {
                        Point centroide2 = this.getCentroide(geometria, this.forzarInterior);
                        feat.setGeometry((Geometry)centroide2);
                        feat.setID(FeatureUtil.nextID());
                        feat.setAttribute(primaryKey, goodValuePK);
                        fcNuevo.addWithNewKey(feat);
                    } else if (geometria.getGeometryType().equals("MultiPolygon")) {
                        int numPoligonos = geometria.getNumGeometries();
                        i = 0;
                        while (i < numPoligonos) {
                            centroide = this.getCentroide(geometria.getGeometryN(i), this.forzarInterior);
                            feat = (Feature)feat.clone();
                            feat.setGeometry((Geometry)centroide);
                            feat.setID(FeatureUtil.nextID());
                            feat.setAttribute(primaryKey, goodValuePK);
                            fcNuevo.addWithNewKey(feat);
                            ++i;
                        }
                    } else if (geometria.getGeometryType().equals("LineString")) {
                        Point centroide3 = this.getCentroide(geometria, this.forzarInterior);
                        feat.setGeometry((Geometry)centroide3);
                        feat.setID(FeatureUtil.nextID());
                        feat.setAttribute(primaryKey, goodValuePK);
                        fcNuevo.addWithNewKey(feat);
                    } else {
                        int numLineas = geometria.getNumGeometries();
                        i = 0;
                        while (i < numLineas) {
                            centroide = this.getCentroide(geometria, this.forzarInterior);
                            feat = (Feature)feat.clone();
                            feat.setGeometry((Geometry)centroide);
                            feat.setID(FeatureUtil.nextID());
                            feat.setAttribute(primaryKey, goodValuePK);
                            fcNuevo.addWithNewKey(feat);
                            ++i;
                        }
                    }
                }
                catch (Exception e) {
                    feat = (Feature)featOrig.clone();
                    feat.setSchema(this.schemaErrors);
                    feat.setID(FeatureUtil.nextID());
                    goodValuePK = FeatureUtil.getGoodAttribute(this.schemaErrors.getPrimaryKey().getType(), idError++);
                    feat.setAttribute(primaryKey, goodValuePK);
                    fcError.addWithNewKey(feat);
                    error = true;
                    --id;
                }
                if (contador++ % 100 != 0) continue;
                monitor.report(contador, numElements, I18N.getString("org.saig.jump.plugin.utils.conversion.GetCentroidsPlugIn.Processed-features"));
            }
            if (monitor.isCancelRequested()) {
                context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.conversion.GetCentroidsPlugIn.Operation-{0}-cancelled-by-user", new Object[]{this.getName()}));
                return;
            }
            if (isMemoryQueryCentroids) {
                monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.GetCentroidsPlugIn.Creating-centroids-layer")) + " ...");
                Layer newLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, fcNuevo.getName(), fcNuevo);
                newLayer.setProjection(sourceLayer.getProjection());
                newLayer.setFeatureCollectionModified(true);
            } else {
                GetCentroidsPlugIn.saveResults(this.queryCentroids, fcNuevo, fcNuevo.getName(), null, sourceLayer.getProjection(), true, monitor);
            }
            if (this.queryErrors != null && error) {
                if (isMemoryQueryErrors) {
                    monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.GetCentroidsPlugIn.Creating-errors-layer")) + " ...");
                    Layer errorLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, fcError.getName(), fcError);
                    errorLayer.setProjection(sourceLayer.getProjection());
                    errorLayer.setFeatureCollectionModified(true);
                } else {
                    GetCentroidsPlugIn.saveResults(this.queryErrors, fcError, fcError.getName(), null, sourceLayer.getProjection(), true, monitor);
                }
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.conversion.GetCentroidsPlugIn.Centroids-getting-finished"));
    }

    private Point getCentroide(Geometry geom, boolean forzar) {
        Point candidateCentroid = geom.getCentroid();
        if (!forzar || candidateCentroid.within(geom)) {
            return candidateCentroid;
        }
        return this.forzarCentroide(geom, candidateCentroid);
    }

    private Point forzarCentroide(Geometry geom, Point candidateCentroid) {
        Coordinate endCoordinate;
        double envWidth = geom.getEnvelopeInternal().getWidth() + 100.0;
        Coordinate startCoordinate = new Coordinate(candidateCentroid.getX() - envWidth, candidateCentroid.getY());
        LineString line = this.gf.createLineString(new Coordinate[]{startCoordinate, endCoordinate = new Coordinate(candidateCentroid.getX() + envWidth, candidateCentroid.getY())});
        Geometry lineasInterseccion = geom.intersection((Geometry)line);
        if (lineasInterseccion.getGeometryType().equals("LineString")) {
            double startPointX = ((LineString)lineasInterseccion).getStartPoint().getX();
            double endPointX = ((LineString)lineasInterseccion).getEndPoint().getX();
            return this.gf.createPoint(new Coordinate((startPointX + endPointX) / 2.0, candidateCentroid.getY()));
        }
        if (lineasInterseccion.getGeometryType().equals("MultiLineString")) {
            LineString candidata = (LineString)lineasInterseccion.getGeometryN(0);
            int numLineas = lineasInterseccion.getNumGeometries();
            int i = 0;
            while (i < numLineas) {
                LineString aux = (LineString)lineasInterseccion.getGeometryN(i);
                if (aux.distance((Geometry)candidateCentroid) < candidata.distance((Geometry)candidateCentroid)) {
                    candidata = aux;
                }
                ++i;
            }
            double startPointX = candidata.getStartPoint().getX();
            double endPointX = candidata.getEndPoint().getX();
            return this.gf.createPoint(new Coordinate((startPointX + endPointX) / 2.0, candidateCentroid.getY()));
        }
        LineString candidata = this.gf.createLineString(new Coordinate[0]);
        int numGeom = lineasInterseccion.getNumGeometries();
        int i = 0;
        while (i < numGeom) {
            if (!lineasInterseccion.getGeometryN(i).getGeometryType().equals("Point")) {
                candidata = (LineString)lineasInterseccion.getGeometryN(i);
                break;
            }
            ++i;
        }
        while (i < numGeom) {
            LineString aux;
            if (!lineasInterseccion.getGeometryN(i).getGeometryType().equals("Point") && (aux = (LineString)lineasInterseccion.getGeometryN(i)).distance((Geometry)candidateCentroid) < candidata.distance((Geometry)candidateCentroid)) {
                candidata = aux;
            }
            ++i;
        }
        double startPointX = candidata.getStartPoint().getX();
        double endPointX = candidata.getEndPoint().getX();
        return this.gf.createPoint(new Coordinate((startPointX + endPointX) / 2.0, candidateCentroid.getY()));
    }
}

