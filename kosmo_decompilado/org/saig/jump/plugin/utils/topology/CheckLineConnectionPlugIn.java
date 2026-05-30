/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.Point
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.utils.topology;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
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
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.AbstractSaveResultsPlugIn;
import org.saig.jump.plugin.datasource.MemoryDataSource;
import org.saig.jump.widgets.util.DialogFactory;

public class CheckLineConnectionPlugIn
extends AbstractSaveResultsPlugIn
implements ThreadedPlugIn {
    public static final Logger LOGGER = Logger.getLogger(CheckLineConnectionPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Check-line-connection");
    public static final Icon ICON = IconLoader.icon("check_line_connection.png");
    private String sourceLayerName;
    private String attrSelected;
    private static final String NOT_CONNECTED_KEY = I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Not-connected");
    private static final String NOT_THE_SAME_VALUE_AT_ENDPOINTS_KEY = I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Distinct-values");
    private static final String INTERSECTION_NOT_AT_ENDPOINTS_KEY = I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Not-at-extremes");
    private static final String OVERLAPPING_LINES_KEY = I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Superposition");
    private FeatureSchema pointSchema;
    private DataSourceQuery queryPoints;
    private FeatureSchema lineSchema;
    private DataSourceQuery queryLines;

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layerable[] selectedLayers = context.getLayerNamePanel().getSelectedLayers();
        Layer sourceLayer = (Layer)selectedLayers[0];
        this.sourceLayerName = sourceLayer.getName();
        FeatureCollection fcOrig = sourceLayer.getFeatureCollectionWrapper().getWrappee();
        FeatureSchema oldSchema = fcOrig.getFeatureSchema();
        int numAtributos = fcOrig.getFeatureSchema().getAttributeCount();
        if (numAtributos <= 2) {
            LOGGER.warn((Object)I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Selected-layer-has-not-enough-attributes"));
            DialogFactory.showWarningDialog(context.getWorkbenchFrame(), I18N.getMessage("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.The-selected-layer-{0}-must-have-three-attributes-at-least", new Object[]{this.sourceLayerName}), I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Wrong-number-of-attributes"));
            return false;
        }
        Object[] atributos = new String[numAtributos - 2];
        int j = 0;
        int i = 0;
        while (i < oldSchema.getAttributeCount()) {
            if (i != oldSchema.getPrimaryKeyIndex() && i != oldSchema.getGeometryIndex()) {
                atributos[j++] = oldSchema.getAttributeName(i);
            }
            ++i;
        }
        Arrays.sort(atributos);
        this.attrSelected = (String)DialogFactory.showSelectionDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Choose-comparation-attribute"), I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Attribute-selection"), atributos, atributos[0]);
        if (this.attrSelected == null) {
            LOGGER.warn((Object)I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.No-attribute-was-selected"));
            return false;
        }
        this.pointSchema = new FeatureSchema();
        this.pointSchema.addAttribute("GID", AttributeType.INTEGER, new Boolean(true));
        this.pointSchema.addAttribute("COD", AttributeType.STRING);
        this.pointSchema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        this.pointSchema.setGeometryType(1);
        this.lineSchema = new FeatureSchema();
        this.lineSchema.addAttribute("GID", AttributeType.INTEGER, true);
        this.lineSchema.addAttribute("COD", AttributeType.STRING);
        this.lineSchema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        this.lineSchema.setGeometryType(3);
        this.queryPoints = this.getSaveAsQuery(I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Save-intersections-and-no-connections-layer"), this.pointSchema, fcOrig.getEnvelope(), sourceLayer.getSrid());
        if (this.queryPoints == null) {
            return false;
        }
        this.queryLines = this.getSaveAsQuery(I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Save-superpositions-layer"), this.lineSchema, fcOrig.getEnvelope(), sourceLayer.getSrid());
        return this.queryLines != null;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Checking-connections")) + "...");
        boolean isMemoryPoints = this.queryPoints.getDataSource() instanceof MemoryDataSource;
        boolean isMemoryLines = this.queryLines.getDataSource() instanceof MemoryDataSource;
        FeatureDataset fcNuevo = new FeatureDataset(this.pointSchema);
        fcNuevo.setName(String.valueOf(this.sourceLayerName) + " - " + I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Incidencies"));
        FeatureDataset fcErroresLineas = new FeatureDataset(this.lineSchema);
        fcErroresLineas.setName(String.valueOf(this.sourceLayerName) + " - " + I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Superposition"));
        int id = 0;
        int contador = 0;
        Layer sourceLayer = JUMPWorkbench.getLayer(this.sourceLayerName);
        FeatureCollection fcOrig = sourceLayer.getUltimateFeatureCollectionWrapper();
        int numElements = fcOrig.size();
        GeometryFactory gf = new GeometryFactory();
        HashSet<Feature> linesAlreadyProcessed = new HashSet<Feature>();
        HashSet<Point> notConnectedPoints = new HashSet<Point>();
        HashSet<Point> notSameValueAtEndPoints = new HashSet<Point>();
        HashSet<Point> intersectionNotAtEndPoints = new HashSet<Point>();
        HashSet<Geometry> overlappingLines = new HashSet<Geometry>();
        TreeSet<Point> connectedPoints = new TreeSet<Point>();
        FeatureIterator it = null;
        try {
            Feature currentFeature;
            it = fcOrig.iterator();
            ArrayList<String> labels = new ArrayList<String>();
            labels.add(this.attrSelected);
            while (!monitor.isCancelRequested() && it.hasNext()) {
                ArrayList<Feature> intersected = new ArrayList<Feature>();
                Feature feat = it.next();
                linesAlreadyProcessed.add(feat);
                Geometry linea = feat.getGeometry();
                Coordinate[] coordinateArray = linea.getCoordinates();
                Point initialPoint = gf.createPoint(coordinateArray[0]);
                Point finalPoint = gf.createPoint(coordinateArray[coordinateArray.length - 1]);
                FeatureIterator queryIterator = null;
                try {
                    queryIterator = fcOrig.queryOnlyGeometryIterator(linea.getEnvelopeInternal(), labels);
                    boolean conectados = false;
                    boolean initialPointConnected = false;
                    boolean finalPointConnected = false;
                    while (queryIterator.hasNext()) {
                        Geometry linAux;
                        Feature featAux = queryIterator.next();
                        if (linesAlreadyProcessed.contains(featAux) || !(linAux = featAux.getGeometry()).intersects(linea)) continue;
                        conectados = true;
                        intersected.add(featAux);
                        initialPointConnected |= linAux.intersects((Geometry)initialPoint);
                        finalPointConnected |= linAux.intersects((Geometry)finalPoint);
                    }
                    if (initialPoint.equals((Geometry)finalPoint)) {
                        initialPointConnected = true;
                        finalPointConnected = true;
                    }
                    Coordinate[] puntos = linea.getCoordinates();
                    if (!initialPointConnected) {
                        if (!connectedPoints.contains(initialPoint) && !this.setContains(intersectionNotAtEndPoints, initialPoint)) {
                            notConnectedPoints.add(initialPoint);
                        }
                    } else {
                        connectedPoints.add(initialPoint);
                    }
                    if (!finalPointConnected) {
                        if (!connectedPoints.contains(finalPoint) && !this.setContains(intersectionNotAtEndPoints, finalPoint)) {
                            notConnectedPoints.add(finalPoint);
                        }
                    } else {
                        connectedPoints.add(finalPoint);
                    }
                    if (conectados) {
                        for (Feature linAux : intersected) {
                            boolean equalValues = this.checkEqualsValue(linAux, feat, this.attrSelected);
                            if (equalValues || linesAlreadyProcessed.contains(linAux)) continue;
                            Geometry intersection = linAux.getGeometry().intersection(linea);
                            int i = 0;
                            while (i < intersection.getNumGeometries()) {
                                Geometry geom = intersection.getGeometryN(i);
                                boolean toPoint = geom instanceof Point;
                                if (toPoint) {
                                    boolean isFirstEndPoint;
                                    Point currentPoint = (Point)geom;
                                    boolean bl = isFirstEndPoint = geom.getCoordinate().equals((Object)puntos[0]) || geom.getCoordinate().equals((Object)puntos[puntos.length - 1]);
                                    if (isFirstEndPoint && !this.setContains(notSameValueAtEndPoints, currentPoint)) {
                                        notSameValueAtEndPoints.add(currentPoint);
                                    } else if (!isFirstEndPoint && !this.setContains(intersectionNotAtEndPoints, currentPoint)) {
                                        this.setRemove(notSameValueAtEndPoints, currentPoint);
                                        intersectionNotAtEndPoints.add(currentPoint);
                                    }
                                } else {
                                    overlappingLines.add(geom);
                                }
                                ++i;
                            }
                        }
                    }
                    if (contador++ % 100 != 0) continue;
                    monitor.report(contador, numElements, I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Processed-features"));
                }
                finally {
                    if (queryIterator != null) {
                        queryIterator.close();
                    }
                }
            }
            if (monitor.isCancelRequested()) {
                context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Operation-{0}-aborted-by-user", new Object[]{this.getName()}));
                return;
            }
            for (Geometry geometry : notConnectedPoints) {
                currentFeature = this.buildFeature(this.pointSchema, geometry);
                currentFeature.setAttribute("GID", (Object)new Integer(id++));
                currentFeature.setAttribute("COD", (Object)NOT_CONNECTED_KEY);
                fcNuevo.addWithNewKey(currentFeature);
                if (!monitor.isCancelRequested()) continue;
                context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Operation-{0}-aborted-by-user", new Object[]{this.getName()}));
                return;
            }
            for (Geometry geometry : notSameValueAtEndPoints) {
                currentFeature = this.buildFeature(this.pointSchema, geometry);
                currentFeature.setAttribute("GID", (Object)new Integer(id++));
                currentFeature.setAttribute("COD", (Object)NOT_THE_SAME_VALUE_AT_ENDPOINTS_KEY);
                fcNuevo.addWithNewKey(currentFeature);
                if (!monitor.isCancelRequested()) continue;
                context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Operation-{0}-aborted-by-user", new Object[]{this.getName()}));
                return;
            }
            for (Geometry geometry : intersectionNotAtEndPoints) {
                currentFeature = this.buildFeature(this.pointSchema, geometry);
                currentFeature.setAttribute("GID", (Object)new Integer(id++));
                currentFeature.setAttribute("COD", (Object)INTERSECTION_NOT_AT_ENDPOINTS_KEY);
                fcNuevo.addWithNewKey(currentFeature);
                if (!monitor.isCancelRequested()) continue;
                context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Operation-{0}-aborted-by-user", new Object[]{this.getName()}));
                return;
            }
            for (Geometry geometry : overlappingLines) {
                currentFeature = this.buildFeature(this.lineSchema, geometry);
                currentFeature.setAttribute("GID", (Object)new Integer(id++));
                currentFeature.setAttribute("COD", (Object)OVERLAPPING_LINES_KEY);
                fcErroresLineas.addWithNewKey(currentFeature);
                if (!monitor.isCancelRequested()) continue;
                context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Operation-{0}-aborted-by-user", new Object[]{this.getName()}));
                return;
            }
            monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Creating-new-layer")) + " ...");
            if (fcNuevo.size() > 0) {
                if (isMemoryPoints) {
                    Layer newLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, fcNuevo.getName(), fcNuevo);
                    newLayer.setProjection(sourceLayer.getProjection());
                    newLayer.setFeatureCollectionModified(true);
                    this.applyStyle(newLayer);
                } else {
                    CheckLineConnectionPlugIn.saveResults(this.queryPoints, fcNuevo, fcNuevo.getName(), null, sourceLayer.getProjection(), true, monitor);
                }
            }
            if (fcErroresLineas.size() > 0) {
                if (isMemoryLines) {
                    Layer lineasLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, fcErroresLineas.getName(), fcErroresLineas);
                    lineasLayer.setFeatureCollectionModified(true);
                    this.applyLineStyle(lineasLayer);
                } else {
                    CheckLineConnectionPlugIn.saveResults(this.queryLines, fcErroresLineas, fcErroresLineas.getName(), null, sourceLayer.getProjection(), true, monitor);
                }
            }
            if (fcNuevo.size() > 0 || fcErroresLineas.size() > 0) {
                context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Check-finished-Corresponding-layers-has-been-created"));
            } else {
                context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.topology.CheckLineConnectionPlugIn.Check-finished-No-incidencies-were-detected"));
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }

    private boolean checkEqualsValue(Feature feat1, Feature feat2, String attrSelected) {
        Object valueFeat1 = feat1.getAttribute(attrSelected);
        Object valueFeat2 = feat2.getAttribute(attrSelected);
        if (valueFeat1 == null) {
            return valueFeat2 == null;
        }
        return valueFeat1.equals(valueFeat2);
    }

    private boolean setContains(Set<Point> set, Point point) {
        boolean contains = false;
        Iterator<Point> it = set.iterator();
        while (it.hasNext() && !contains) {
            Point p = it.next();
            contains = p.equalsExact((Geometry)point);
        }
        return contains;
    }

    private void setRemove(Set<Point> set, Point point) {
        boolean contains = false;
        Point p = null;
        Iterator<Point> it = set.iterator();
        while (it.hasNext() && !contains) {
            p = it.next();
            contains = p.equalsExact((Geometry)point);
        }
        if (p != null && contains) {
            set.remove(p);
        }
    }

    private void applyLineStyle(Layer lineasLayer) {
        BasicStyle stl1 = (BasicStyle)lineasLayer.getStyle(BasicStyle.class);
        stl1.setLineColor(Color.RED);
        stl1.setRenderingLine(true);
        stl1.setRenderingFill(true);
        stl1.setFillColor(Color.RED);
        stl1.setLineWidth(2);
        stl1.setAlpha(255);
        stl1.setEnabled(true);
        lineasLayer.removeStyle(stl1);
        lineasLayer.addStyle(stl1);
        ArrayList<Style> styles = new ArrayList<Style>();
        styles.add(stl1);
        lineasLayer.addStyles(styles);
        lineasLayer.fireAppearanceChanged();
    }

    private Feature buildFeature(FeatureSchema schema, Geometry geom) {
        Feature feat = FeatureUtil.toFeature(geom, schema);
        return feat;
    }

    @Override
    public EnableCheck getCheck() {
        return CheckLineConnectionPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private void applyStyle(Layer layer) {
        BasicStyle stl1 = new BasicStyle();
        stl1.setLineColor(Color.BLUE);
        stl1.setRenderingLine(true);
        stl1.setRenderingFill(true);
        stl1.setFillColor(Color.BLUE);
        stl1.setAlpha(255);
        stl1.setEnabled(true);
        BasicStyle stl2 = new BasicStyle();
        stl2.setLineColor(Color.RED);
        stl2.setRenderingLine(true);
        stl2.setRenderingFill(true);
        stl2.setFillColor(Color.RED);
        stl2.setAlpha(255);
        stl2.setLineWidth(2);
        stl2.setEnabled(true);
        BasicStyle stl3 = new BasicStyle();
        stl3.setLineColor(Color.ORANGE);
        stl3.setRenderingLine(true);
        stl3.setRenderingFill(true);
        stl3.setFillColor(Color.ORANGE);
        stl3.setAlpha(255);
        stl3.setLineWidth(2);
        stl3.setEnabled(true);
        HashMap<Object, BasicStyle> styles = new HashMap<Object, BasicStyle>();
        styles.put(NOT_CONNECTED_KEY, stl1);
        styles.put(NOT_THE_SAME_VALUE_AT_ENDPOINTS_KEY, stl2);
        styles.put(INTERSECTION_NOT_AT_ENDPOINTS_KEY, stl3);
        ColorThemingStyle colorThem = new ColorThemingStyle("COD", styles, null);
        colorThem.setEnabled(true);
        ArrayList<Style> l = new ArrayList<Style>();
        l.add(colorThem);
        layer.getBasicStyle().setEnabled(false);
        layer.addStyles(l);
        layer.setDrawingLast(true);
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
        solucion.add(checkFactory.createSelectedLayersMustNotBeRasterCheck());
        solucion.add(checkFactory.createSelectedLayerTypeGeometryCheck(new int[]{3, 2}));
        return solucion;
    }
}

