/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.util;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.FenceLayerFinder;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DelegatingTool;
import com.vividsolutions.jump.workbench.ui.cursortool.LeftClickFilter;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.SaveProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedItemsPlugIn;
import java.awt.geom.NoninvertibleTransformException;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JInternalFrame;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.gvsig.crs.CrsException;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.model.bookmark.BookmarkCategory;
import org.saig.core.model.bookmark.BookmarkManager;
import org.saig.core.model.bookmark.IBookmark;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.model.relations.topology.TopologyRelationException;
import org.saig.core.model.task.TaskManager;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.plugin.datasource.IndexedShapeFileDataSource;
import org.saig.jump.plugin.utils.LoadCategoryPlugIn;
import org.saig.jump.util.LayerUtil;
import org.saig.jump.widgets.config.ConfigPathPanel;

public class KosmoDesktopUtils {
    private static final Logger LOGGER = Logger.getLogger(KosmoDesktopUtils.class);
    public static final String DEFAULT_STRING_CHARSET = "ISO-8859-1";

    public static Task getTask(String name) {
        WorkbenchFrame frameInstance = JUMPWorkbench.getFrameInstance();
        TaskManager taskManager = frameInstance.getContext().getTaskManager();
        TaskFrame taskframe = taskManager.getTask(name);
        if (taskframe != null) {
            return taskframe.getTask();
        }
        return null;
    }

    public static Layer addMemoryLayerToKosmo(String taskName, String category, String layerName, FeatureSchema schema, File styleFile) throws CrsException {
        Layer layer = KosmoDesktopUtils.addMemoryLayerToKosmo(taskName, category, layerName, schema);
        LayerUtil.loadStyleToLayer(layer, styleFile);
        return layer;
    }

    public static Layer addMemoryLayerToKosmo(String taskName, String category, String layerName, FeatureSchema schema, String projection, File styleFile) throws CrsException {
        Layer layer = KosmoDesktopUtils.addMemoryLayerToKosmo(taskName, category, layerName, schema);
        LayerUtil.setLayerProjection(layer, projection);
        LayerUtil.loadStyleToLayer(layer, styleFile);
        return layer;
    }

    public static Layer addMemoryLayerToKosmo(String taskName, String category, String layerName, FeatureSchema schema) {
        Task task = KosmoDesktopUtils.getTask(taskName);
        LayerManager layerManager = task.getLayerManager();
        FeatureDataset fcLayer = new FeatureDataset(schema);
        Layer layer = layerManager.addLayerToTop(category, layerName, fcLayer);
        return layer;
    }

    public static Layer addMemoryLayerToKosmo(String category, String layerName, FeatureSchema schema) {
        Task task = JUMPWorkbench.getFrameInstance().getContext().getTask();
        if (task != null) {
            return KosmoDesktopUtils.addMemoryLayerToKosmo(task.getName(), category, layerName, schema);
        }
        return null;
    }

    public static Layer addMemoryLayerToKosmo(String categoryName, String layerName, FeatureCollection fc) {
        Layer layer = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getLayerManager().addLayer(categoryName, layerName, fc);
        return layer;
    }

    public static Layer addShapeLayerToKosmo(String taskName, String category, String layerName, FeatureSchema schema, File shapeFile) throws Exception {
        Task task = KosmoDesktopUtils.getTask(taskName);
        LayerManager layerManager = task.getLayerManager();
        ShapeFileDataSource shapeDs = KosmoDesktopUtils.createShapeDataAccesor(shapeFile);
        FeatureCollectionOnDemand onDemand = new FeatureCollectionOnDemand();
        onDemand.setDataAccesor(shapeDs);
        Layer layer = layerManager.addLayerToTop(category, layerName, onDemand);
        DataSourceQuery dsq = new DataSourceQuery();
        dsq.setQuery(shapeFile.getAbsolutePath());
        dsq.setDataSource(new IndexedShapeFileDataSource());
        layer.setDataSourceQuery(dsq);
        return layer;
    }

    public static Layer addShapeLayerToKosmo(String taskName, String category, String layerName, FeatureSchema schema, String projection, File shapeFile) throws Exception {
        Layer layer = KosmoDesktopUtils.addShapeLayerToKosmo(taskName, category, layerName, schema, shapeFile);
        LayerUtil.setLayerProjection(layer, projection);
        return layer;
    }

    public static Layer addShapeLayerToKosmo(String taskName, String category, String layerName, FeatureSchema schema, File styleFile, File shapeFile) throws Exception {
        Layer layer = KosmoDesktopUtils.addShapeLayerToKosmo(taskName, category, layerName, schema, shapeFile);
        LayerUtil.loadStyleToLayer(layer, styleFile);
        return layer;
    }

    public static ShapeFileDataSource createShapeDataAccesor(File shpFile, String selectedCharset) throws Exception {
        ShapeFileDataSource dataAccesor = new ShapeFileDataSource();
        if (!StringUtils.isEmpty((String)selectedCharset) && Charset.isSupported(selectedCharset)) {
            Charset charset = Charset.forName(selectedCharset);
            dataAccesor.setCharset(charset);
        }
        dataAccesor.setFile(shpFile);
        dataAccesor.createSpatialIndex();
        return dataAccesor;
    }

    public static ShapeFileDataSource createShapeDataAccesor(File shpFile) throws Exception {
        return KosmoDesktopUtils.createShapeDataAccesor(shpFile, DEFAULT_STRING_CHARSET);
    }

    public static Layer addShapeLayerToKosmo(String taskName, String category, String layerName, FeatureSchema schema, String projection, File styleFile, File shapeFile) throws Exception {
        Layer layer = KosmoDesktopUtils.addShapeLayerToKosmo(taskName, category, layerName, schema, projection, shapeFile);
        LayerUtil.loadStyleToLayer(layer, styleFile);
        return layer;
    }

    public static Layer addShapeLayerToKosmo(String category, String layerName, FeatureSchema schema, String projection, File styleFile, File shapeFile) throws Exception {
        Task task = JUMPWorkbench.getFrameInstance().getContext().getTask();
        Layer layer = null;
        if (task != null) {
            String taskName = task.getName();
            layer = KosmoDesktopUtils.addShapeLayerToKosmo(taskName, category, layerName, schema, projection, shapeFile);
            LayerUtil.loadStyleToLayer(layer, styleFile);
        }
        return layer;
    }

    public static List<Feature> getSelectedFeatures() {
        ArrayList<Feature> features = new ArrayList<Feature>();
        LayerViewPanel layerViewPanel = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel();
        if (layerViewPanel != null) {
            SelectionManager selectionManager = layerViewPanel.getSelectionManager();
            Collection<Feature> featuresWithSelectedItems = selectionManager.getFeaturesWithSelectedItems();
            features.addAll(featuresWithSelectedItems);
        }
        return features;
    }

    public static List<Feature> getSelectedFeatures(Layer layer) {
        ArrayList<Feature> features = new ArrayList<Feature>();
        LayerViewPanel layerViewPanel = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel();
        if (layerViewPanel != null) {
            SelectionManager selectionManager = layerViewPanel.getSelectionManager();
            Collection<Feature> featuresWithSelectedItems = selectionManager.getFeaturesWithSelectedItems(layer);
            features.addAll(featuresWithSelectedItems);
        }
        return features;
    }

    public static void discardSelection() {
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        LayerViewPanel layerViewPanel = context.getLayerViewPanel();
        if (layerViewPanel != null) {
            SelectionManager selectionManager = layerViewPanel.getSelectionManager();
            selectionManager.clearFeatureSelection();
        }
    }

    public static Layer getSelectedFeaturesLayer() {
        SelectionManager selectionManager;
        Collection<Layer> layersWithSelectedItems;
        Layer layer = null;
        LayerViewPanel layerViewPanel = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel();
        if (layerViewPanel != null && (layersWithSelectedItems = (selectionManager = layerViewPanel.getSelectionManager()).getLayersWithSelectedItems()).size() == 1) {
            layer = layersWithSelectedItems.iterator().next();
        }
        return layer;
    }

    public static Collection<Category> getSelectedCategories() {
        Collection<Category> selectedCategories = new ArrayList<Category>();
        WorkbenchFrame frameInstance = JUMPWorkbench.getFrameInstance();
        WorkbenchContext context = frameInstance.getContext();
        LayerNamePanel layerNamePanel = context.getLayerNamePanel();
        if (layerNamePanel != null) {
            selectedCategories = layerNamePanel.getSelectedCategories();
        }
        return selectedCategories;
    }

    public static Layer getSelectedLayer() {
        WorkbenchFrame frameInstance = JUMPWorkbench.getFrameInstance();
        WorkbenchContext context = frameInstance.getContext();
        LayerNamePanel layerNamePanel = context.getLayerNamePanel();
        if (layerNamePanel == null) {
            return null;
        }
        Layerable[] selectedLayers = layerNamePanel.getSelectedLayers();
        if (selectedLayers.length > 0) {
            Layerable layerable = selectedLayers[0];
            if (layerable instanceof Layer) {
                return (Layer)layerable;
            }
            return null;
        }
        return null;
    }

    public static Layerable[] getSelectedLayerables() {
        WorkbenchFrame frameInstance = JUMPWorkbench.getFrameInstance();
        WorkbenchContext context = frameInstance.getContext();
        LayerNamePanel layerNamePanel = context.getLayerNamePanel();
        if (layerNamePanel == null) {
            return null;
        }
        Layerable[] selectedLayers = layerNamePanel.getSelectedLayers();
        return selectedLayers;
    }

    public static List<Layer> getSelectedLayers() {
        Layerable[] selectedLayers = KosmoDesktopUtils.getSelectedLayerables();
        ArrayList<Layer> layers = new ArrayList<Layer>();
        if (selectedLayers == null) {
            return layers;
        }
        Layerable[] layerableArray = selectedLayers;
        int n = selectedLayers.length;
        int n2 = 0;
        while (n2 < n) {
            Layerable layerable = layerableArray[n2];
            if (layerable instanceof Layer) {
                layers.add((Layer)layerable);
            }
            ++n2;
        }
        return layers;
    }

    public static List<Layer> getVisibleNotRasterLayers() {
        ArrayList<Layer> layers = new ArrayList<Layer>();
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        LayerManager layerManager = context.getLayerManager();
        if (layerManager != null) {
            List<Layerable> visibleLayerables = layerManager.getVisibleLayerables();
            for (Layerable layerable : visibleLayerables) {
                Layer layer;
                if (!(layerable instanceof Layer) || (layer = (Layer)layerable).isRaster()) continue;
                layers.add(layer);
            }
        }
        return layers;
    }

    public static Layer getEditableLayer() {
        WorkbenchFrame frameInstance = JUMPWorkbench.getFrameInstance();
        WorkbenchContext context = frameInstance.getContext();
        LayerManager layerManager = context.getLayerManager();
        if (layerManager != null) {
            Collection<Layer> editableLayers = layerManager.getEditableLayers();
            Layer editableLayer = null;
            if (editableLayers.size() > 0) {
                editableLayer = editableLayers.iterator().next();
                return editableLayer;
            }
            return null;
        }
        return null;
    }

    public static double getPixelSize() {
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        LayerViewPanel layerViewPanel = context.getLayerViewPanel();
        if (layerViewPanel != null) {
            int screenWidth = layerViewPanel.getWidth();
            Envelope worldEnvelope = layerViewPanel.getViewport().getEnvelopeInModelCoordinates();
            return worldEnvelope.getWidth() / (double)screenWidth;
        }
        return -1.0;
    }

    public static void executeUndoableAddAndSelect(String name, SelectionManager selMan, Layer editableLayer, List<Feature> toAdd) throws Exception {
        KosmoDesktopUtils.executeUndoableChanges(name, selMan, editableLayer, toAdd, new ArrayList<Feature>(), new ArrayList<Feature>(), new ArrayList<Feature>(), true);
    }

    public static void executeUndoableUpdateChanges(String name, Layer editableLayer, List<Feature> toUpdate, List<Feature> original) throws Exception {
        KosmoDesktopUtils.executeUndoableChanges(name, null, editableLayer, new ArrayList<Feature>(), new ArrayList<Feature>(), toUpdate, original, false);
    }

    public static void executeUndoableUpdateChangesAndSelect(String name, SelectionManager selMan, Layer editableLayer, List<Feature> toUpdate, List<Feature> original) throws Exception {
        KosmoDesktopUtils.executeUndoableChanges(name, selMan, editableLayer, new ArrayList<Feature>(), new ArrayList<Feature>(), toUpdate, original, true);
    }

    public static void executeUndoableUpdateAndAddChangesAndSelectAll(String name, SelectionManager selMan, Layer editableLayer, List<Feature> toUpdate, List<Feature> original, List<Feature> toAdd) throws Exception {
        KosmoDesktopUtils.executeUndoableChanges(name, selMan, editableLayer, toAdd, new ArrayList<Feature>(), toUpdate, original, true);
    }

    public static void executeUndoableUpdateChanges(String name, Layer editableLayer, Feature toUpdate, Feature original) throws Exception {
        ArrayList<Feature> toUpdateList = new ArrayList<Feature>();
        toUpdateList.add(toUpdate);
        ArrayList<Feature> originalList = new ArrayList<Feature>();
        originalList.add(original);
        KosmoDesktopUtils.executeUndoableChanges(name, null, editableLayer, new ArrayList<Feature>(), new ArrayList<Feature>(), toUpdateList, originalList, false);
    }

    protected static void executeUndoableChanges(String name, final SelectionManager selectionManager, final Layer editableLayer, final List<Feature> toAdd, final List<Feature> toDelete, final List<Feature> toUpdate, final List<Feature> unchangedToUpdate, final boolean selectChanges) throws Exception {
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        LayerViewPanel layerViewPanel = context.getLayerViewPanel();
        if (layerViewPanel != null) {
            AbstractPlugIn.execute(new UndoableCommand(name){

                @Override
                public void execute() throws Exception {
                    if (selectChanges) {
                        selectionManager.unselectItems(editableLayer);
                    }
                    try {
                        editableLayer.getFeatureCollectionWrapper().addAll(toAdd);
                        editableLayer.getFeatureCollectionWrapper().updateAll(toUpdate);
                        editableLayer.getFeatureCollectionWrapper().removeAll(toDelete);
                        if (selectChanges) {
                            ArrayList<Feature> addedAndModified = new ArrayList<Feature>();
                            addedAndModified.addAll(toAdd);
                            addedAndModified.addAll(toUpdate);
                            selectionManager.getFeatureSelection().selectItems(editableLayer, addedAndModified);
                        }
                    }
                    catch (TopologyRelationException e) {
                        selectionManager.getFeatureSelection().selectItems(editableLayer, toAdd);
                        JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                    }
                }

                @Override
                public void unexecute() throws Exception {
                    if (selectChanges) {
                        selectionManager.unselectItems(editableLayer);
                    }
                    editableLayer.getFeatureCollectionWrapper().addAll(toDelete);
                    editableLayer.getFeatureCollectionWrapper().updateAll(unchangedToUpdate);
                    editableLayer.getFeatureCollectionWrapper().removeAll(toAdd);
                    if (selectChanges) {
                        selectionManager.clear();
                    }
                }
            }, layerViewPanel);
        }
    }

    public static void zoomElement(String taskName, String layerName, Object pk) throws NoninvertibleTransformException {
        FeatureCollectionWrapper featureCollectionWrapper;
        Feature byPrimaryKey;
        Layer layer;
        LayerManager layerManager;
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        TaskManager taskManager = context.getTaskManager();
        TaskFrame task = taskManager.getTask(taskName);
        if (task != null && (layerManager = task.getLayerManager()) != null && (layer = layerManager.getLayer(layerName)) != null && (byPrimaryKey = (featureCollectionWrapper = layer.getFeatureCollectionWrapper()).getByPrimaryKey(pk)) != null) {
            Geometry geometry = byPrimaryKey.getGeometry();
            Envelope envelopeInternal = geometry.getEnvelopeInternal();
            envelopeInternal.expandBy(1.0);
            LayerViewPanel layerViewPanel = task.getLayerViewPanel();
            layerViewPanel.getViewport().zoom(envelopeInternal);
        }
    }

    public static void zoom(Envelope env) throws NoninvertibleTransformException {
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        LayerViewPanel layerViewPanel = context.getLayerViewPanel();
        if (layerViewPanel != null && env != null) {
            layerViewPanel.getViewport().zoom(env);
        }
    }

    public static void zoomFeatures(List<Feature> features, double margin) throws NoninvertibleTransformException {
        Envelope env = null;
        for (Feature feature : features) {
            Geometry geometry = feature.getGeometry();
            if (geometry == null) continue;
            if (env == null) {
                env = geometry.getEnvelopeInternal();
                continue;
            }
            env.expandToInclude(geometry.getEnvelopeInternal());
        }
        if (env != null) {
            env.expandBy(margin);
            KosmoDesktopUtils.zoom(env);
        }
    }

    public static void zoomElement(String layerName, Object pk) throws NoninvertibleTransformException {
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        Task task = context.getTask();
        if (task != null) {
            KosmoDesktopUtils.zoomElement(task.getName(), layerName, pk);
        }
    }

    public static SelectionManager getFrontViewSelectionManager() {
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        LayerViewPanel layerViewPanel = context.getLayerViewPanel();
        if (layerViewPanel != null) {
            return layerViewPanel.getSelectionManager();
        }
        return null;
    }

    public static List<Layer> getActiveViewLayers() {
        return JUMPWorkbench.getFrameInstance().getContext().getAllLayers();
    }

    public static List<Layer> getActiveNotRasterLayers() {
        ArrayList<Layer> layers = new ArrayList<Layer>();
        List<Layer> some = KosmoDesktopUtils.getActiveViewLayers();
        for (Layer layer : some) {
            if (layer.isRaster()) continue;
            layers.add(layer);
        }
        return layers;
    }

    public static void loadCategory(File categoryFile) throws Exception {
        LoadCategoryPlugIn loader = new LoadCategoryPlugIn();
        loader.setLayerGroupFile(categoryFile);
        loader.run(new DummyTaskMonitor(), JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
    }

    public static boolean layerExist(String layerName) {
        return JUMPWorkbench.getLayer(layerName) != null;
    }

    public static Layer addHiddenInvisibleLayer(String name, FeatureCollection fc) {
        LayerManager manager = new LayerManager();
        Category dummyCategory = new Category();
        Layer layer = new Layer(name, new Symbolizer[0], fc, manager);
        manager.addCategory(dummyCategory, 0);
        layer.setLayerManager(manager);
        dummyCategory.add(0, layer);
        JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getLayerManager().addHideLayer(layer);
        manager.addLayerListener(JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel());
        return layer;
    }

    public static Layer addHiddenShapeLayer(String name, File shapeFile) throws Exception {
        ShapeFileDataSource shapeDs = KosmoDesktopUtils.createShapeDataAccesor(shapeFile);
        FeatureCollectionOnDemand onDemand = new FeatureCollectionOnDemand();
        onDemand.setDataAccesor(shapeDs);
        Layer hiddenLayer = KosmoDesktopUtils.addHiddenInvisibleLayer(name, onDemand);
        hiddenLayer.setVisible(true);
        return hiddenLayer;
    }

    public static File getKosmoTemporalDir() {
        File f;
        String defaultPath = (String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigPathPanel.TEMP_FILES_PATH_KEY);
        if (!StringUtils.isEmpty((String)defaultPath) && (f = new File(defaultPath)).exists()) {
            return f;
        }
        return null;
    }

    public static Envelope getActiveViewportEnvelope() {
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        LayerViewPanel layerViewPanel = context.getLayerViewPanel();
        if (layerViewPanel != null) {
            return layerViewPanel.getViewport().getEnvelopeInModelCoordinates();
        }
        return null;
    }

    public static CursorTool getLastNotDelegatingCursorTool(CursorTool ct) {
        if (ct == null) {
            return null;
        }
        if (ct instanceof DelegatingTool) {
            return KosmoDesktopUtils.getLastNotDelegatingCursorTool(((DelegatingTool)ct).getDelegate());
        }
        if (ct instanceof LeftClickFilter) {
            return KosmoDesktopUtils.getLastNotDelegatingCursorTool(((LeftClickFilter)ct).getWrappee());
        }
        return ct;
    }

    public static void selectFeaturesInLayer(Layer layer, Collection<Feature> features) {
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        LayerViewPanel layerViewPanel = context.getLayerViewPanel();
        if (layerViewPanel != null) {
            SelectionManager selectionManager = layerViewPanel.getSelectionManager();
            selectionManager.getFeatureSelection().selectItems(layer, features);
        }
    }

    public static void selectElement(String layerName, Object id) {
        Feature byPrimaryKey;
        Layer layer;
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        LayerViewPanel layerViewPanel = context.getLayerViewPanel();
        if (layerViewPanel != null && (layer = layerViewPanel.getLayerManager().getLayer(layerName)) != null && (byPrimaryKey = layer.getFeatureCollectionWrapper().getByPrimaryKey(id)) != null) {
            layerViewPanel.getSelectionManager().clear();
            KosmoDesktopUtils.selectFeaturesInLayer(layer, Arrays.asList(byPrimaryKey));
        }
    }

    public static void saveProject() {
        File projectFile = JUMPWorkbench.getFrameInstance().getContext().getProjectFile();
        if (projectFile != null) {
            SaveProjectPlugIn.saveProject(projectFile.getAbsolutePath());
        }
    }

    public static void quitKosmo() {
        JUMPWorkbench.getFrameInstance().getApplicationExitHandler().exitApplication(JUMPWorkbench.getFrameInstance());
    }

    public static void zoom(String taskName, String bookmarkName) throws NoninvertibleTransformException {
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        TaskManager taskManager = context.getTaskManager();
        TaskFrame taskFrame = taskManager.getTask(taskName);
        if (taskFrame != null) {
            taskFrame.toFront();
            BookmarkManager bookManager = BookmarkManager.getInstance();
            Collection<BookmarkCategory> categories = bookManager.getCategories();
            for (BookmarkCategory category : categories) {
                Collection<IBookmark> bookmarks = bookManager.getBookmarks(category.getName());
                for (IBookmark bookmark : bookmarks) {
                    if (!bookmarkName.equals(bookmark.getName())) continue;
                    Geometry localization = bookmark.getLocalization();
                    Envelope toZoom = localization.getEnvelopeInternal();
                    LayerViewPanel layerViewPanel = taskFrame.getLayerViewPanel();
                    if (layerViewPanel != null) {
                        layerViewPanel.getViewport().zoom(toZoom);
                    }
                    return;
                }
            }
        }
    }

    public static List<Layerable> getVisibleLayerables() {
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        if (context == null) {
            return new ArrayList<Layerable>();
        }
        LayerNamePanel lnp = context.getLayerNamePanel();
        if (lnp == null) {
            return new ArrayList<Layerable>();
        }
        Collection<Layerable> allLayers = lnp.getLayerManager().getAllLayers();
        ArrayList<Layerable> allLayersList = new ArrayList<Layerable>(allLayers);
        ArrayList<Layerable> visibleLayers = new ArrayList<Layerable>();
        ListIterator<Layerable> it = allLayersList.listIterator(allLayersList.size());
        while (it.hasPrevious()) {
            Layerable layerable = it.previous();
            if (!layerable.isVisible()) continue;
            visibleLayers.add(layerable);
        }
        return visibleLayers;
    }

    public static void showLeftTabbedPane(String name) {
        JInternalFrame[] internalFrames;
        JInternalFrame[] jInternalFrameArray = internalFrames = JUMPWorkbench.getFrameInstance().getInternalFrames();
        int n = internalFrames.length;
        int n2 = 0;
        while (n2 < n) {
            JInternalFrame internalFrame = jInternalFrameArray[n2];
            if (internalFrame instanceof TaskFrame) {
                TaskFrame taskFrame = (TaskFrame)internalFrame;
                taskFrame.showLeftComponent(name);
            }
            ++n2;
        }
    }

    public static void flash(Geometry geom) throws NoninvertibleTransformException {
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        LayerViewPanel layerViewPanel = context.getLayerViewPanel();
        if (layerViewPanel != null && geom != null) {
            ArrayList<Geometry> geoms = new ArrayList<Geometry>(1);
            geoms.add(geom);
            ZoomToSelectedItemsPlugIn.flash(geoms, layerViewPanel);
        }
    }

    public static CursorTool getWrappedTool(CursorTool currentCursorTool) {
        CursorTool wrappedCursorTool = null;
        if (currentCursorTool instanceof QuasimodeTool) {
            CursorTool defaultCursorTool = ((QuasimodeTool)currentCursorTool).getDefaultTool();
            if (defaultCursorTool instanceof LeftClickFilter) {
                CursorTool wrappeeCursorTool = ((LeftClickFilter)defaultCursorTool).getWrappee();
                if (wrappeeCursorTool instanceof DelegatingTool) {
                    wrappeeCursorTool = ((DelegatingTool)wrappeeCursorTool).getDelegate();
                }
                wrappedCursorTool = wrappeeCursorTool;
            }
        } else if (currentCursorTool instanceof DelegatingTool) {
            DelegatingTool delegatingTool = (DelegatingTool)currentCursorTool;
            wrappedCursorTool = delegatingTool.getDelegate();
        } else {
            wrappedCursorTool = currentCursorTool;
        }
        return wrappedCursorTool;
    }

    public static void showFence(Geometry geom) throws Exception {
        FenceLayerFinder fenceLayerFinder = new FenceLayerFinder(JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
        fenceLayerFinder.setFence(geom);
        fenceLayerFinder.getLayer().setVisible(true);
    }

    public static void clearFence() throws Exception {
        FenceLayerFinder fenceLayerFinder = new FenceLayerFinder(JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
        fenceLayerFinder.clearFence();
    }

    public static void setFenceStyle(File slsFile) throws Exception {
        FenceLayerFinder fenceLayerFinder = new FenceLayerFinder(JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
        Layer layer = fenceLayerFinder.getLayer();
        if (layer == null) {
            layer = fenceLayerFinder.createLayer();
        }
        LayerUtil.loadStyleToLayer(layer, slsFile);
    }

    public static void activateTool(CursorTool tool) {
        PlugInContext pContext = JUMPWorkbench.getFrameInstance().getContext().createPlugInContext();
        pContext.getLayerViewPanel().setCurrentCursorTool(tool);
    }
}

