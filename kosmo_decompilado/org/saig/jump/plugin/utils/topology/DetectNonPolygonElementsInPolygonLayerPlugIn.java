/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Polygon
 */
package org.saig.jump.plugin.utils.topology;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.ArrayList;
import javax.swing.Icon;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class DetectNonPolygonElementsInPolygonLayerPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.topology.DetectNonPolygonElementsInPolygonLayerPlugIn.Detect-non-polygon-elements-in-polygon-layer");
    public static final Icon ICON = IconLoader.icon("blank.png");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return DetectNonPolygonElementsInPolygonLayerPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        return true;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustNotBeRasterCheck()).add(checkFactory.createSelectedLayerTypeGeometryCheck(new int[]{5, 4}));
        return solucion;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.topology.DetectNonPolygonElementsInPolygonLayerPlugIn.Detecting-incorrect-geometries")) + "...");
        ArrayList<Feature> pointFeatures = new ArrayList<Feature>();
        ArrayList<Feature> lineFeatures = new ArrayList<Feature>();
        Layer selectedLayer = (Layer)context.getSelectedLayers()[0];
        int contador = 1;
        int numFeatures = selectedLayer.getFeatureCollectionWrapper().size();
        FeatureIterator itFeatures = null;
        try {
            itFeatures = selectedLayer.getFeatureCollectionWrapper().iterator();
            while (!monitor.isCancelRequested() && itFeatures.hasNext()) {
                Feature currentFeat = itFeatures.next();
                Geometry currentGeom = currentFeat.getGeometry();
                if (!(currentGeom instanceof Polygon) && !(currentGeom instanceof MultiPolygon)) {
                    if (currentGeom instanceof LineString || currentGeom instanceof MultiLineString) {
                        lineFeatures.add(currentFeat);
                    } else {
                        pointFeatures.add(currentFeat);
                    }
                }
                if (contador++ % 100 != 0) continue;
                monitor.report(contador, numFeatures, I18N.getString("org.saig.jump.plugin.utils.topology.DetectNonPolygonElementsInPolygonLayerPlugIn.Processed-features"));
            }
            if (monitor.isCancelRequested()) {
                this.warnOperationCancelled(context);
                return;
            }
        }
        finally {
            if (itFeatures != null) {
                itFeatures.close();
            }
        }
        if (!lineFeatures.isEmpty() || !pointFeatures.isEmpty()) {
            if (!lineFeatures.isEmpty()) {
                FeatureDataset fcLines = new FeatureDataset(lineFeatures, selectedLayer.getFeatureSchema());
                context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, String.valueOf(selectedLayer.getName()) + " - " + I18N.getString("org.saig.jump.plugin.utils.topology.DetectNonPolygonElementsInPolygonLayerPlugIn.Lines"), fcLines);
            }
            if (!pointFeatures.isEmpty()) {
                FeatureDataset fcPoints = new FeatureDataset(pointFeatures, selectedLayer.getFeatureSchema());
                context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, String.valueOf(selectedLayer.getName()) + " - " + I18N.getString("org.saig.jump.plugin.utils.topology.DetectNonPolygonElementsInPolygonLayerPlugIn.Points"), fcPoints);
            }
            context.getWorkbenchFrame().warnUser(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.topology.DetectNonPolygonElementsInPolygonLayerPlugIn.Elements-non-polygon-were-detected-in-the-polygon-layer")) + selectedLayer.getName());
        } else {
            context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.topology.DetectNonPolygonElementsInPolygonLayerPlugIn.The-layer-{0}-is-correct", new Object[]{selectedLayer.getName()}));
        }
    }
}

