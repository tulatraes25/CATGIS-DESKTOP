/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.utils.topology;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.FileUtil;
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
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.io.File;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHP;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class DiscoverGapsPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final Logger LOGGER = Logger.getLogger(DiscoverGapsPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.topology.DiscoverGapsPlugIn.Discover-gaps");
    public static final Icon ICON = IconLoader.icon("discoverHole.png");
    private boolean inMemory = false;
    private String filePath;
    private JFileChooser chooser;

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
        return DiscoverGapsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layer selectedLayer = (Layer)context.getSelectedLayers()[0];
        if (selectedLayer.getFeatureCollectionWrapper().size() == 0) {
            context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.topology.DiscoverGapsPlugIn.The-layer-{0}-has-no-features", new Object[]{selectedLayer.getName()}));
            return false;
        }
        Object[] options = new Object[]{I18N.getString("org.saig.jump.plugin.utils.topology.DiscoverGapsPlugIn.To-memory"), I18N.getString("org.saig.jump.plugin.utils.topology.DiscoverGapsPlugIn.To-disk"), I18N.getString("org.saig.jump.plugin.utils.topology.DiscoverGapsPlugIn.Cancel")};
        int n = JOptionPane.showOptionDialog(context.getWorkbenchFrame(), I18N.getString("org.saig.jump.plugin.utils.topology.DiscoverGapsPlugIn.Where-do-you-want-to-save-the-results"), this.getName(), 1, 3, null, options, options[2]);
        if (n == 0) {
            this.inMemory = true;
            return true;
        }
        if (n == 1) {
            this.inMemory = false;
            this.chooser = GUIUtil.createJFileChooserWithOverwritePrompting();
            int returned = this.chooser.showSaveDialog(JUMPWorkbench.getFrameInstance());
            if (returned == 0) {
                File archivo = this.chooser.getSelectedFile();
                this.filePath = archivo.getAbsolutePath();
                return true;
            }
            return false;
        }
        return false;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayerTypeGeometryCheck(new int[]{5, 4}));
        return solucion;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        Layer selectedLayer = (Layer)context.getSelectedLayers()[0];
        monitor.report(I18N.getMessage("org.saig.jump.plugin.utils.topology.DiscoverGapsPlugIn.Processing-layer-{0}", new Object[]{selectedLayer.getName()}));
        FeatureCollection featCol = selectedLayer.getUltimateFeatureCollectionWrapper();
        FeatureIterator itPol = null;
        int pos = 0;
        int numElements = featCol.size();
        ArrayList<Geometry> list = new ArrayList<Geometry>();
        try {
            itPol = featCol.iterator();
            while (!monitor.isCancelRequested() && itPol.hasNext()) {
                Feature feature = itPol.next();
                Geometry geom = feature.getGeometry();
                if (geom != null) {
                    list.add(geom);
                }
                if (pos++ % 100 != 0) continue;
                monitor.report(pos, numElements, I18N.getString("org.saig.jump.plugin.utils.topology.DiscoverGapsPlugIn.Processed-features"));
            }
            if (monitor.isCancelRequested()) {
                context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.topology.DiscoverGapsPlugIn.Operation-{0}-cancelled-by-user", new Object[]{this.getName()}));
                return;
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        finally {
            if (itPol != null) {
                itPol.close();
            }
        }
        Geometry[] geomArray = list.toArray(new Geometry[0]);
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.topology.DiscoverGapsPlugIn.Creating-full-geometry")) + "...");
        GeometryCollection geomCol = new GeometryCollection(geomArray, new GeometryFactory());
        Geometry total = geomCol.buffer(0.0);
        Envelope envAjustado = geomCol.getEnvelopeInternal();
        double edgeWidth = envAjustado.getWidth() * 0.05;
        double edgeHeith = envAjustado.getHeight() * 0.05;
        Envelope envExtendido = new Envelope(envAjustado.getMinX() - edgeWidth, envAjustado.getMaxX() + edgeWidth, envAjustado.getMinY() - edgeHeith, envAjustado.getMaxY() + edgeHeith);
        Geometry box = new GeometryFactory().toGeometry(envExtendido);
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.topology.DiscoverGapsPlugIn.Creating-difference-with-original-layer")) + "...");
        Geometry holes = box.difference(total);
        Feature feat = FeatureUtil.toFeature(holes, selectedLayer.getFeatureSchema());
        ArrayList<Feature> featureArray = new ArrayList<Feature>();
        featureArray.add(feat);
        if (monitor.isCancelRequested()) {
            context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.topology.DiscoverGapsPlugIn.Operation-{0}-cancelled-by-user", new Object[]{this.getName()}));
            return;
        }
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.topology.DiscoverGapsPlugIn.Creating-output-layer")) + "...");
        if (this.inMemory) {
            FeatureDataset featColPol = new FeatureDataset(featureArray, selectedLayer.getFeatureSchema());
            Layer newLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, String.valueOf(selectedLayer.getName()) + " - " + I18N.getString("org.saig.jump.plugin.utils.topology.DiscoverGapsPlugIn.Gaps"), featColPol);
            newLayer.setProjection(selectedLayer.getProjection());
            newLayer.setFeatureCollectionModified(false);
        } else {
            FeatureDataset featColPol = new FeatureDataset(featureArray, selectedLayer.getFeatureSchema());
            String shapePath = FileUtil.addValidExtension(this.filePath, "shp");
            SHP.createShapeFile(featColPol.getFeatureSchema(), ShapeGeometryConverter.jts_to_igeometry(featColPol.getFeaturesSamples(1).get(0).getGeometry()), featColPol.iterator(), new File(shapePath), false, false, false, ShapeFileDataSource.DEFAULT_STRING_CHARSET);
        }
    }
}

