/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.util.LinearComponentExtracter
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.utils.conversion;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;
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
import java.util.LinkedList;
import java.util.List;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.AbstractSaveResultsPlugIn;
import org.saig.jump.plugin.datasource.MemoryDataSource;

public class GetLinesFromPolygonsPlugIn
extends AbstractSaveResultsPlugIn
implements ThreadedPlugIn {
    public static final Logger LOGGER = Logger.getLogger(GetLinesFromPolygonsPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPolygonsPlugIn.Get-lines-from-polygons");
    public static final Icon ICON = IconLoader.icon("blank.png");
    private String sourceLayerName;
    private FeatureSchema schemaResults;
    private DataSourceQuery query;
    private String idAttributeName;
    private GeometryFactory gf = new GeometryFactory();

    @Override
    public EnableCheck getCheck() {
        return GetLinesFromPolygonsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
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
        solucion.add(checkFactory.createSelectedLayerTypeGeometryCheck(new int[]{5, 4}));
        return solucion;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layerable[] selectedLayers = context.getLayerNamePanel().getSelectedLayers();
        Layer sourceLayer = (Layer)selectedLayers[0];
        this.sourceLayerName = sourceLayer.getName();
        FeatureCollection fcOrig = sourceLayer.getUltimateFeatureCollectionWrapper();
        this.schemaResults = (FeatureSchema)fcOrig.getFeatureSchema().clone();
        this.idAttributeName = "ID_ORIG";
        int cont = 0;
        boolean exists = this.schemaResults.getAttribute(this.idAttributeName) != null;
        while (exists) {
            this.idAttributeName = "ID_ORIG_" + cont++;
            boolean bl = exists = this.schemaResults.getAttribute(this.idAttributeName) != null;
        }
        this.schemaResults.addAttribute(this.idAttributeName, AttributeType.INTEGER);
        this.schemaResults.setGeometryType(3);
        this.query = this.getSaveAsQuery(this.schemaResults, fcOrig.getEnvelope(), sourceLayer.getSrid());
        return this.query != null;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPolygonsPlugIn.Creating-new-data-structure")) + "...");
        boolean isMemoryQuery = this.query.getDataSource() instanceof MemoryDataSource;
        Layer sourceLayer = JUMPWorkbench.getLayer(this.sourceLayerName);
        FeatureCollection fcOrig = sourceLayer.getUltimateFeatureCollectionWrapper();
        FeatureDataset fcNuevo = new FeatureDataset(this.schemaResults);
        AttributeType idAttrType = this.schemaResults.getAttributeType(this.idAttributeName);
        AttributeType pkAttrType = this.schemaResults.getAttributeType(this.schemaResults.getPrimaryKeyName());
        fcNuevo.setName(String.valueOf(this.sourceLayerName) + " - " + I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPolygonsPlugIn.Lines"));
        fcNuevo.set3d(fcOrig.is3d());
        int id = 0;
        int contador = 0;
        int numElements = fcOrig.size();
        String primaryKey = this.schemaResults.getPrimaryKeyName();
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPolygonsPlugIn.Getting-lines-from-polygons")) + "...");
        FeatureIterator it = null;
        try {
            List lineas = new LinkedList();
            it = fcOrig.iterator();
            while (!monitor.isCancelRequested() && it.hasNext()) {
                Feature featOrig = it.next();
                Feature feat = (Feature)featOrig.clone();
                feat.setSchema(this.schemaResults);
                feat.setAttribute(this.idAttributeName, FeatureUtil.getGoodAttribute(idAttrType, (Number)featOrig.getAttribute(primaryKey)));
                Geometry poligono = featOrig.getGeometry();
                lineas = LinearComponentExtracter.getLines((Geometry)poligono);
                for (LineString currentLine : lineas) {
                    LineString line = this.gf.createLineString(currentLine.getCoordinateSequence());
                    Feature newFeat = (Feature)feat.clone();
                    newFeat.setGeometry((Geometry)line);
                    newFeat.setID(FeatureUtil.nextID());
                    newFeat.setAttribute(primaryKey, FeatureUtil.getGoodAttribute(pkAttrType, new Integer(id++)));
                    fcNuevo.addWithNewKey(newFeat);
                }
                if (contador++ % 100 != 0) continue;
                monitor.report(contador, numElements, I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPolygonsPlugIn.Processed-features"));
            }
            if (monitor.isCancelRequested()) {
                context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.conversion.GetLinesFromPolygonsPlugIn.Operation-{0}-aborted-by-user", new Object[]{this.getName()}));
                return;
            }
            if (isMemoryQuery) {
                monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPolygonsPlugIn.Creating-new-data-layer")) + "...");
                Layer newLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, fcNuevo.getName(), fcNuevo);
                newLayer.setProjection(sourceLayer.getProjection());
                newLayer.setFeatureCollectionModified(true);
            } else {
                GetLinesFromPolygonsPlugIn.saveResults(this.query, fcNuevo, fcNuevo.getName(), null, sourceLayer.getProjection(), true, monitor);
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPolygonsPlugIn.Getting-lines-from-polygons-finished"));
    }
}

