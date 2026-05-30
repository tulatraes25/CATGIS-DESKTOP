/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.utils.conversion;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.SortedAttribute;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.AbstractSaveResultsPlugIn;
import org.saig.jump.plugin.datasource.MemoryDataSource;
import org.saig.jump.widgets.utils.conversion.GetLinesFromPointsDialog;

public class GetLinesFromPointsPlugIn
extends AbstractSaveResultsPlugIn
implements ThreadedPlugIn {
    public static final Logger LOGGER = Logger.getLogger(GetLinesFromPointsPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Get-lines-from-points");
    public static final Icon ICON = IconLoader.icon("blank.png");
    private String sourceLayerName;
    private FeatureSchema oldSchema;
    private FeatureSchema schemaResults;
    private FeatureSchema schemaErrors;
    private DataSourceQuery queryResults;
    private DataSourceQuery queryErrors;
    private String[] atributos;
    private String[] atributos2;
    private String atributoLinea;
    private String atributoOrden;
    private boolean errores;

    @Override
    public EnableCheck getCheck() {
        return GetLinesFromPointsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
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
        solucion.add(checkFactory.createSelectedLayerTypeGeometryCheck(new int[]{1, 8}));
        return solucion;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        this.errores = false;
        Layerable[] selectedLayers = context.getLayerNamePanel().getSelectedLayers();
        Layer sourceLayer = (Layer)selectedLayers[0];
        this.sourceLayerName = sourceLayer.getName();
        FeatureCollection fcOrig = sourceLayer.getUltimateFeatureCollectionWrapper();
        this.oldSchema = fcOrig.getFeatureSchema();
        int numAtributos = this.oldSchema.getAttributeCount();
        this.atributos = new String[numAtributos - 1];
        this.atributos2 = new String[numAtributos];
        this.atributos2[0] = I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.No-specified");
        Vector<String> attrNames = new Vector<String>();
        int i = 0;
        while (i < numAtributos) {
            String tmp = this.oldSchema.getAttributeName(i);
            if (!this.esGeometria(this.oldSchema.getAttributeType(tmp))) {
                attrNames.add(tmp);
            }
            ++i;
        }
        Collections.sort(attrNames);
        int indice = 0;
        for (String attrName : attrNames) {
            this.atributos[indice++] = attrName;
            this.atributos2[indice] = attrName;
        }
        GetLinesFromPointsDialog opcionesDialog = new GetLinesFromPointsDialog(JUMPWorkbench.getFrameInstance(), true, attrNames);
        opcionesDialog.setVisible(true);
        if (opcionesDialog == null || !opcionesDialog.isExitOk()) {
            return false;
        }
        this.atributoLinea = opcionesDialog.getSelectedLineAttribute();
        this.atributoOrden = opcionesDialog.getSelectedOrderAttribute();
        this.schemaResults = (FeatureSchema)this.oldSchema.clone();
        this.schemaResults.addRelations(this.oldSchema.recoverRelations());
        this.schemaResults.setGeometryType(3);
        this.schemaErrors = (FeatureSchema)this.oldSchema.clone();
        this.queryResults = opcionesDialog.getQueryResults();
        this.queryErrors = opcionesDialog.getQueryErrors();
        return this.queryResults != null && this.queryErrors != null;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        List vertices2;
        monitor.allowCancellationRequests();
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Creating-new-data-structure")) + "...");
        boolean isMemoryQuery = this.queryResults.getDataSource() instanceof MemoryDataSource;
        boolean isMemoryError = this.queryErrors.getDataSource() instanceof MemoryDataSource;
        Layer sourceLayer = JUMPWorkbench.getLayer(this.sourceLayerName);
        FeatureCollection fcOrig = sourceLayer.getUltimateFeatureCollectionWrapper();
        int contador = 0;
        int gid = 0;
        int numElements = fcOrig.size();
        HashMap lineas = new HashMap();
        boolean ordenados = this.atributoOrden != null && !this.atributoOrden.equals(I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.No-specified"));
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Grouping-points")) + "...");
        contador = 0;
        numElements = fcOrig.size();
        boolean isString = sourceLayer.getUltimateFeatureCollectionWrapper().getFeatureSchema().getAttribute(this.atributoOrden).getType().toJavaClass().equals(String.class);
        FeatureIterator itFeatures = null;
        try {
            itFeatures = fcOrig.iterator();
            while (!monitor.isCancelRequested() && itFeatures.hasNext()) {
                block27: {
                    Feature feat = itFeatures.next();
                    try {
                        Object key = feat.getAttribute(this.atributoLinea);
                        if (ordenados) {
                            if (lineas.containsKey(key)) {
                                ((List)lineas.get(key)).add(new SortedAttribute(feat.getAttribute(this.atributoOrden), feat, true, isString));
                                break block27;
                            } else {
                                vertices2 = new LinkedList<Comparable<SortedAttribute>>();
                                vertices2.add(new SortedAttribute(feat.getAttribute(this.atributoOrden), feat, true, isString));
                                lineas.put(key, vertices2);
                            }
                            break block27;
                        }
                        if (lineas.containsKey(key)) {
                            ((List)lineas.get(key)).add(feat);
                        } else {
                            vertices2 = new LinkedList();
                            vertices2.add(feat);
                            lineas.put(key, vertices2);
                        }
                    }
                    catch (ClassCastException e) {
                        LOGGER.warn((Object)I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.The-attribute-that-shows-which-line-the-points-belongs-to-is-not-valid"), (Throwable)e);
                    }
                }
                if (contador++ % 100 != 0) continue;
                monitor.report(contador, numElements, I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Processed-features"));
            }
        }
        finally {
            if (itFeatures != null) {
                itFeatures.close();
            }
        }
        if (monitor.isCancelRequested()) {
            context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Operation-aborted-by-user"));
            return;
        }
        if (ordenados) {
            monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Ordering-points")) + "...");
            contador = 0;
            numElements = lineas.size();
            for (List vertices2 : lineas.values()) {
                Collections.sort(vertices2);
                if (contador++ % 100 != 0) continue;
                monitor.report(contador, numElements, I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Processed-features"));
                if (!monitor.isCancelRequested()) continue;
                context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Operation-aborted-by-user"));
                return;
            }
        }
        GeometryFactory gf = new GeometryFactory();
        FeatureDataset fcNuevo = new FeatureDataset(this.schemaResults);
        fcNuevo.setName(String.valueOf(sourceLayer.getName()) + " - " + I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Lines"));
        fcNuevo.set3d(fcOrig.is3d());
        FeatureDataset fcError = new FeatureDataset(this.schemaErrors);
        fcError.setName(String.valueOf(sourceLayer.getName()) + " - " + I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Errors"));
        fcError.set3d(fcOrig.is3d());
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Getting-lines-from-points")) + "...");
        contador = 0;
        numElements = lineas.size();
        AttributeType newSchemaPrimaryKeyType = this.schemaResults.getPrimaryKey().getType();
        Iterator it = lineas.values().iterator();
        while (!monitor.isCancelRequested() && it.hasNext()) {
            BasicFeature newFeat = new BasicFeature(this.schemaResults);
            vertices2 = (List)it.next();
            Feature oldFeat = ordenados ? (Feature)((SortedAttribute)vertices2.get(0)).getRecordNumber() : (Feature)vertices2.get(0);
            int i = 0;
            while (i < this.schemaResults.getAttributeCount()) {
                String nombreAtributo = this.schemaResults.getAttributeName(i);
                newFeat.setAttribute(nombreAtributo, oldFeat.getAttribute(nombreAtributo));
                ++i;
            }
            newFeat.setAttribute(this.schemaResults.getPrimaryKeyName(), FeatureUtil.getGoodAttribute(newSchemaPrimaryKeyType, gid++));
            Coordinate[] coors = new Coordinate[vertices2.size()];
            int i2 = 0;
            while (i2 < vertices2.size()) {
                coors[i2] = ordenados ? ((Feature)((SortedAttribute)vertices2.get(i2)).getRecordNumber()).getGeometry().getCoordinate() : ((Feature)vertices2.get(i2)).getGeometry().getCoordinate();
                ++i2;
            }
            if (coors.length > 1) {
                newFeat.setGeometry((Geometry)gf.createLineString(coors));
                fcNuevo.addWithNewKey(newFeat);
            } else if (coors.length == 1) {
                this.errores = true;
                Feature errFeat = (Feature)newFeat.clone();
                errFeat.setSchema(this.schemaErrors);
                errFeat.setGeometry((Geometry)gf.createPoint(coors[0]));
                fcError.addWithNewKey(errFeat);
            }
            if (contador++ % 100 != 0) continue;
            monitor.report(contador, numElements, I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Processed-features"));
        }
        if (monitor.isCancelRequested()) {
            context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Operation-aborted-by-user"));
            return;
        }
        if (isMemoryQuery) {
            monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Creating-data-layer")) + "...");
            Layer newLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, fcNuevo.getName(), fcNuevo);
            newLayer.setProjection(sourceLayer.getProjection());
            newLayer.setFeatureCollectionModified(true);
        } else {
            GetLinesFromPointsPlugIn.saveResults(this.queryResults, fcNuevo, fcNuevo.getName(), null, sourceLayer.getProjection(), true, monitor);
        }
        if (this.errores) {
            if (isMemoryError) {
                Layer newLayerError = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, fcError.getName(), fcError);
                newLayerError.setProjection(sourceLayer.getProjection());
                newLayerError.setFeatureCollectionModified(true);
            } else {
                GetLinesFromPointsPlugIn.saveResults(this.queryErrors, fcError, fcError.getName(), null, sourceLayer.getProjection(), true, monitor);
            }
        }
        context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Getting-lines-finished"));
    }

    private boolean esGeometria(AttributeType at) {
        return at != null && at.equals(AttributeType.GEOMETRY);
    }
}

