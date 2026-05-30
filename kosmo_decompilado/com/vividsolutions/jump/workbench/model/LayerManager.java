/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.CategoryEventType;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.FenceLayerFinder;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.LayerListenerImplement;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.UndoableEditReceiver;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.InfoFrame;
import com.vividsolutions.jump.workbench.ui.InfoModel;
import com.vividsolutions.jump.workbench.ui.plugin.ViewAttributesPlugIn;
import com.vividsolutions.wms.MapStyle;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JInternalFrame;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.saig.core.util.MultiColorsGenerator;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.LayerUtil;

public class LayerManager
implements LayerListener {
    private static final Logger LOGGER = Logger.getLogger(LayerManager.class);
    private Map<String, Layer> hideLayers = new HashMap<String, Layer>();
    private static int layerManagerCount = 0;
    private List<Category> categories = new ArrayList<Category>();
    private List<Layer> layersToDispose = new ArrayList<Layer>();
    private boolean firingEvents = true;
    private List<LayerListener> layerListeners = new ArrayList<LayerListener>();
    private UndoableEditReceiver undoableEditReceiver = new UndoableEditReceiver();
    private Blackboard blackboard = new Blackboard();

    public LayerManager() {
        ++layerManagerCount;
    }

    public UndoableEditReceiver getUndoableEditReceiver() {
        return this.undoableEditReceiver;
    }

    public void deferFiringEvents(Runnable r) {
        boolean firingEvents = this.isFiringEvents();
        this.setFiringEvents(false);
        try {
            r.run();
        }
        finally {
            this.setFiringEvents(firingEvents);
        }
    }

    public Color generateLayerFillColor() {
        return MultiColorsGenerator.getInstance().generateColor();
    }

    public Layer addLayer(String categoryName, Layer layer) {
        this.addLayerable(categoryName, layer);
        return layer;
    }

    public void addLayerable(String categoryName, Layerable layerable) {
        if (layerable instanceof Layer) {
            this.layersToDispose.add((Layer)layerable);
            LayerListener selectedListener = null;
            for (LayerListener element : this.layerListeners) {
                if (!(element instanceof LayerListenerImplement) || !((LayerListenerImplement)element).getLayer().equals(layerable)) continue;
                selectedListener = element;
            }
            if (selectedListener == null && (selectedListener = ((Layer)layerable).getLayerListener()) == null) {
                this.layerListeners.add(selectedListener);
            }
        }
        this.addCategory(categoryName);
        Category cat = this.getCategory(categoryName);
        cat.add(0, layerable);
        this.fireLayerChanged(layerable, LayerEventType.METADATA_CHANGED);
    }

    public void addLayerable(String categoryName, Layerable layerable, int index) {
        if (layerable instanceof Layer) {
            this.layersToDispose.add((Layer)layerable);
            LayerListener selectedListener = null;
            for (LayerListener element : this.layerListeners) {
                if (!(element instanceof LayerListenerImplement) || !((LayerListenerImplement)element).getLayer().equals(layerable)) continue;
                selectedListener = element;
            }
            if (selectedListener == null && (selectedListener = ((Layer)layerable).getLayerListener()) != null) {
                this.layerListeners.add(selectedListener);
            }
        }
        this.addCategory(categoryName);
        Category cat = this.getCategory(categoryName);
        cat.add(index, layerable);
        this.fireLayerChanged(layerable, LayerEventType.METADATA_CHANGED);
    }

    public Category addCategory(String categoryName) {
        return this.addCategory(categoryName, this.categories.size(), false);
    }

    public Category addCategory(String categoryName, boolean collapsed) {
        return this.addCategory(categoryName, this.categories.size(), collapsed);
    }

    public Category addCategory(String categoryName, boolean collapsed, Map<Locale, String> titleByLang, Map<Object, Object> properties) {
        return this.addCategory(categoryName, this.categories.size(), collapsed, titleByLang, properties);
    }

    public Category addCategory(String categoryName, int index, boolean collapsed) {
        return this.addCategory(categoryName, index, collapsed, new HashMap<Locale, String>(), new HashMap<Object, Object>());
    }

    public Category addCategory(String categoryName, int index, boolean collapsed, Map<Locale, String> titleByLang, Map<Object, Object> properties) {
        Category oldCategory = this.getCategory(categoryName);
        if (oldCategory != null) {
            return oldCategory;
        }
        Category category = new Category();
        category.setLayerManager(this);
        boolean firingEvents = this.isFiringEvents();
        this.setFiringEvents(false);
        try {
            category.setName(categoryName);
            category.setCollapsed(collapsed);
            category.setTitleByLang(titleByLang);
            category.setProperties(properties);
        }
        finally {
            this.setFiringEvents(firingEvents);
        }
        this.categories.add(index, category);
        this.fireCategoryChanged(category, CategoryEventType.ADDED, this.indexOf(category));
        return category;
    }

    public Category getCategory(String name) {
        for (Category category : this.categories) {
            if (!category.getName().equals(name)) continue;
            return category;
        }
        return null;
    }

    public List<Category> getCategories() {
        return this.categories;
    }

    public Layer addLayer(String categoryName, String layerName, FeatureCollection featureCollection) {
        String actualName = layerName == null ? I18N.getString("workbench.model.LayerManager.layer") : layerName;
        Layer layer = new Layer(actualName, this.generateLayerFillColor(), featureCollection, this);
        this.addLayerable(categoryName, layer);
        return layer;
    }

    public Layer addLayerToTop(String categoryName, String layerName, FeatureCollection featureCollection) {
        String actualName = layerName == null ? I18N.getString("workbench.model.LayerManager.layer") : layerName;
        Layer layer = new Layer(actualName, this.generateLayerFillColor(), featureCollection, this);
        Category category = this.getCategory(categoryName);
        if (category == null) {
            Category cat = new Category();
            cat.setName(categoryName);
            this.addCategory(cat, 0);
            cat.setLayerManager(this);
            cat.setCollapsed(false);
        }
        this.addLayerable(categoryName, layer, 0);
        return layer;
    }

    public Layer addLayer(String categoryName, String layerName, FeatureCollection featureCollection, IProjection proj) {
        String actualName = layerName == null ? I18N.getString("workbench.model.LayerManager.layer") : layerName;
        Layer layer = new Layer(actualName, this.generateLayerFillColor(), featureCollection, this);
        layer.setProjection(proj);
        this.addLayerable(categoryName, layer);
        return layer;
    }

    public Layer addOrReplaceLayer(String categoryName, String layerName, FeatureCollection featureCollection) {
        Layer oldLayer = this.getLayer(layerName);
        Layer newLayer = this.addLayer(categoryName, layerName, featureCollection);
        if (oldLayer != null) {
            newLayer.setStyles(oldLayer.cloneStyles());
        }
        return newLayer;
    }

    public String uniqueLayerName(String name) {
        String newName;
        if (!this.isExistingLayerableName(name)) {
            return name;
        }
        int i = 2;
        do {
            newName = String.valueOf(name) + " (" + i + ")";
            ++i;
        } while (this.isExistingLayerableName(newName));
        return newName;
    }

    public boolean isExistingLayerableName(String name) {
        for (Layerable layerable : this.getLayerables(Layerable.class)) {
            if (!layerable.getName().equals(name)) continue;
            return true;
        }
        return false;
    }

    public void remove(Layerable layerable, boolean disposedLayer) {
        this.removeLayerFromAllAssociatedInfoFrames(layerable);
        this.removeViewAttributesFramesAssociatedToLayer(layerable);
        for (Category c : this.categories) {
            int index = c.indexOf(layerable);
            if (index == -1) continue;
            c.remove(layerable);
            LayerListener selectedListener = null;
            for (LayerListener element : this.layerListeners) {
                if (!(element instanceof LayerListenerImplement) || !((LayerListenerImplement)element).getLayer().equals(layerable)) continue;
                selectedListener = element;
            }
            this.layerListeners.remove(selectedListener);
            if (disposedLayer) {
                ((Layer)layerable).dispose();
            }
            this.layersToDispose.remove(layerable);
            this.fireLayerChanged(layerable, LayerEventType.REMOVED, c, index);
        }
    }

    public void remove(Layerable layerable) {
        this.remove(layerable, false);
    }

    public void removeViewAttributesFramesAssociatedToLayer(Layerable layer) {
        JInternalFrame[] jif;
        JInternalFrame[] jInternalFrameArray = jif = JUMPWorkbench.getFrameInstance().getInternalFrames();
        int n = jif.length;
        int n2 = 0;
        while (n2 < n) {
            ViewAttributesPlugIn.ViewAttributesFrame vaf;
            JInternalFrame internalFrame = jInternalFrameArray[n2];
            if (internalFrame instanceof ViewAttributesPlugIn.ViewAttributesFrame && (vaf = (ViewAttributesPlugIn.ViewAttributesFrame)internalFrame).getAssociatedLayer().equals(layer)) {
                JUMPWorkbench.getFrameInstance().removeInternalFrame(vaf);
            }
            ++n2;
        }
    }

    public void removeLayerFromAllAssociatedInfoFrames(Layerable layer) {
        JInternalFrame[] jif;
        if (!(layer instanceof Layer)) {
            return;
        }
        JInternalFrame[] jInternalFrameArray = jif = JUMPWorkbench.getFrameInstance().getInternalFrames();
        int n = jif.length;
        int n2 = 0;
        while (n2 < n) {
            InfoFrame infoFrame;
            InfoModel model;
            JInternalFrame internalFrame = jInternalFrameArray[n2];
            if (internalFrame instanceof InfoFrame && (model = (infoFrame = (InfoFrame)internalFrame).getModel()) != null) {
                model.remove((Layer)layer);
            }
            ++n2;
        }
    }

    public void removeIfEmpty(Category category) {
        if (!category.isEmpty()) {
            return;
        }
        this.remove(category);
    }

    public void remove(Category category) {
        int categoryIndex = this.indexOf(category);
        this.categories.remove(category);
        this.fireCategoryChanged(category, CategoryEventType.REMOVED, categoryIndex);
    }

    public int indexOf(Category category) {
        return this.categories.indexOf(category);
    }

    public void fireCategoryChanged(Category category, CategoryEventType type) {
        this.fireCategoryChanged(category, type, this.indexOf(category));
    }

    private void fireCategoryChanged(final Category category, final CategoryEventType type, final int categoryIndex) {
        if (!this.firingEvents) {
            return;
        }
        for (final LayerListener layerListener : this.layerListeners) {
            this.fireLayerEvent(new Runnable(){

                @Override
                public void run() {
                    layerListener.categoryChanged(new CategoryEvent(category, type, categoryIndex));
                }
            });
        }
    }

    public void fireFeaturesChanged(Collection<Feature> features, FeatureEventType type, Layer layer) {
        Assert.isTrue((type != FeatureEventType.GEOMETRY_MODIFIED ? 1 : 0) != 0);
        this.fireFeaturesChanged(features, type, layer, null);
    }

    public void fireGeometryModified(Collection<Feature> features, Layer layer, Collection<Feature> oldFeatureClones) {
        Assert.isTrue((oldFeatureClones != null ? 1 : 0) != 0);
        this.fireFeaturesChanged(features, FeatureEventType.GEOMETRY_MODIFIED, layer, oldFeatureClones);
    }

    private void fireFeaturesChanged(final Collection<Feature> features, final FeatureEventType type, final Layer layer, final Collection<Feature> oldFeatureClones) {
        if (!this.firingEvents) {
            return;
        }
        for (final LayerListener layerListener : this.layerListeners) {
            this.fireLayerEvent(new Runnable(){

                @Override
                public void run() {
                    layerListener.featuresChanged(new FeatureEvent(features, type, layer, oldFeatureClones));
                }
            });
        }
    }

    private void fireLayerEvent(Runnable eventFirer) {
        try {
            GUIUtil.invokeOnEventThread(eventFirer);
        }
        catch (InterruptedException e) {
            Assert.shouldNeverReachHere();
        }
        catch (InvocationTargetException e) {
            e.getTargetException().printStackTrace();
            Assert.shouldNeverReachHere();
        }
    }

    private void fireLayerChanged(final Layerable layerable, final LayerEventType layerChangeType, final Category category, final int layerIndex) {
        if (!this.firingEvents) {
            return;
        }
        for (final LayerListener layerListener : new ArrayList<LayerListener>(this.layerListeners)) {
            this.fireLayerEvent(new Runnable(){

                @Override
                public void run() {
                    layerListener.layerChanged(new LayerEvent(layerable, layerChangeType, category, layerIndex));
                }
            });
        }
    }

    public void fireLayerChanged(Layerable layerable, LayerEventType type) {
        Category cat = this.getCategory(layerable);
        if (cat == null) {
            Assert.isTrue((!this.isFiringEvents() ? 1 : 0) != 0, (String)I18N.getMessage("workbench.model.LayerManager.if-this-event-is-being-fired-because-you-are-.-layerable-{0}", new Object[]{layerable.getName()}));
            return;
        }
        this.fireLayerChanged(layerable, type, cat, cat.indexOf(layerable));
    }

    public void setFiringEvents(boolean firingEvents) {
        this.firingEvents = firingEvents;
    }

    public boolean isFiringEvents() {
        return this.firingEvents;
    }

    public Iterator<Layerable> reverseIterator(Class<? extends Layerable> layerableClass) {
        ArrayList<Layerable> layerablesCopy = new ArrayList<Layerable>(this.getLayerables(layerableClass));
        Collections.reverse(layerablesCopy);
        this.moveLayersDrawnLastToEnd(layerablesCopy);
        return layerablesCopy.iterator();
    }

    private void moveLayersDrawnLastToEnd(List<Layerable> layerables) {
        ArrayList<Layer> layersDrawnLast = new ArrayList<Layer>();
        Iterator<Layerable> i = layerables.iterator();
        while (i.hasNext()) {
            Layer layer;
            Layerable layerable = i.next();
            if (!(layerable instanceof Layer) || !(layer = (Layer)layerable).isDrawingLast()) continue;
            layersDrawnLast.add(layer);
            i.remove();
        }
        layerables.addAll(layersDrawnLast);
    }

    public Iterator<Layer> iterator() {
        return this.getLayers().iterator();
    }

    public Layer getLayer(String name) {
        Iterator<Layer> i = this.iterator();
        while (i.hasNext()) {
            Layer layer = i.next();
            if (!layer.getName().equals(name)) continue;
            return layer;
        }
        return null;
    }

    public Layerable getLayerable(String name) {
        for (Layerable layer : this.getAllLayers()) {
            if (!layer.getName().equals(name)) continue;
            return layer;
        }
        return null;
    }

    public void addLayerListener(LayerListener layerListener) {
        Assert.isTrue((!this.layerListeners.contains(layerListener) ? 1 : 0) != 0);
        this.layerListeners.add(layerListener);
    }

    public void removeLayerListener(LayerListener layerListener) {
        this.layerListeners.remove(layerListener);
    }

    public void removeLayerListeners(Collection<LayerListener> removeLayerListeners) {
        this.layerListeners.removeAll(removeLayerListeners);
    }

    public Layer getLayer(int index) {
        return this.getLayers().get(index);
    }

    public int size() {
        return this.getLayers().size() + this.getWMSLayers().size();
    }

    public Envelope getEnvelopeOfAllLayers() {
        Envelope envelope = new Envelope();
        Iterator<Layer> i = this.iterator();
        while (i.hasNext()) {
            Layer layer = i.next();
            if (!layer.isEnabled()) continue;
            Envelope layerEnv = layer.getTransformedEnvelope();
            int size = -1;
            try {
                size = layer.getFeatureCollectionWrapper().size();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            if (layerEnv != null && layer.getFeatureCollectionWrapper() != null && size > 0) {
                envelope.expandToInclude(layerEnv);
                continue;
            }
            LOGGER.warn((Object)I18N.getMessage("workbench.model.LayerManager.layer-{0}-has-not-envelope", new Object[]{layer.getName()}));
        }
        List<WMSLayer> wmslayers = this.getWMSLayers();
        for (WMSLayer layer : wmslayers) {
            envelope.expandToInclude(layer.getFullEnvelope());
        }
        return envelope;
    }

    public int indexOf(Layer layer) {
        return this.getLayers().indexOf(layer);
    }

    public int indexOf(Layerable layerable) {
        return this.getLayerables(Layerable.class).indexOf(layerable);
    }

    public Category getCategory(Layerable layerable) {
        for (Category category : this.categories) {
            if (!category.contains(layerable)) continue;
            return category;
        }
        return null;
    }

    public List<Layer> getLayers() {
        return this.getLayerables(Layer.class);
    }

    public List<Layer> getLayersNoInternals() {
        ArrayList<Layer> result = new ArrayList<Layer>();
        List<Layer> layers = this.getLayers();
        for (Layer layer : layers) {
            if (layer.isInternal() || LayerUtil.isAppInternalSystemLayer(layer) || LayerUtil.isSystemLayer(layer)) continue;
            result.add(layer);
        }
        return result;
    }

    public List<WMSLayer> getWMSLayers() {
        return this.getLayerables(WMSLayer.class);
    }

    public Collection<Layerable> getAllLayers() {
        ArrayList<Layerable> allLayers = new ArrayList<Layerable>();
        allLayers.addAll(this.getLayers());
        allLayers.addAll(this.getWMSLayers());
        return allLayers;
    }

    public List<? extends Layerable> getLayerables(Class<? extends Layerable> layerableClass) {
        Assert.isTrue((boolean)Layerable.class.isAssignableFrom(layerableClass));
        ArrayList<Layerable> layers = new ArrayList<Layerable>();
        for (Category c : new ArrayList<Category>(this.categories)) {
            for (Layerable l : new ArrayList<Layerable>(c.getLayerables())) {
                if (!layerableClass.isInstance(l)) continue;
                layers.add(l);
            }
        }
        return layers;
    }

    public List<Layerable> getVisibleLayerables() {
        ArrayList<Layerable> layers = new ArrayList<Layerable>();
        for (Category c : new ArrayList<Category>(this.categories)) {
            for (Layerable l : new ArrayList<Layerable>(c.getLayerables())) {
                if (!l.isVisible()) continue;
                layers.add(l);
            }
        }
        return layers;
    }

    public List<Layerable> getVisibleLayerablesForLegend() {
        ArrayList<Layerable> layers = new ArrayList<Layerable>();
        for (Category c : new ArrayList<Category>(this.categories)) {
            for (Layerable l : new ArrayList<Layerable>(c.getLayerables())) {
                if (!l.isVisible()) continue;
                if (l instanceof WMSLayer) {
                    MapStyle style;
                    WMSLayer wmsLayer = (WMSLayer)l;
                    List<MapStyle> styles = wmsLayer.getLayerStyles();
                    if (styles.isEmpty() || (style = styles.iterator().next()).getLegendIcon() == null) continue;
                    layers.add(l);
                    continue;
                }
                if (l.isRaster()) continue;
                layers.add(l);
            }
        }
        return layers;
    }

    public List<Layer> getVisibleLayers(boolean includeFence) {
        ArrayList<Layer> visibleLayers = new ArrayList<Layer>(this.getLayers());
        Iterator i = visibleLayers.iterator();
        while (i.hasNext()) {
            Layer layer = (Layer)i.next();
            if (layer.getName().equals(FenceLayerFinder.LAYER_NAME) && !includeFence) {
                i.remove();
                continue;
            }
            if (layer.isVisible()) continue;
            i.remove();
        }
        return visibleLayers;
    }

    public List<WMSLayer> getVisibleWMSLayers() {
        ArrayList<WMSLayer> visibleLayers = new ArrayList<WMSLayer>(this.getWMSLayers());
        Iterator i = visibleLayers.iterator();
        while (i.hasNext()) {
            WMSLayer layer = (WMSLayer)i.next();
            if (layer.isVisible()) continue;
            i.remove();
        }
        return visibleLayers;
    }

    public List<Layer> getNoRasterLayers() {
        ArrayList<Layer> noRasterLayers = new ArrayList<Layer>(this.getLayers());
        Iterator i = noRasterLayers.iterator();
        while (i.hasNext()) {
            Layer layer = (Layer)i.next();
            if (!layer.isRaster() && layer.isEnabled()) continue;
            i.remove();
        }
        return noRasterLayers;
    }

    public void dispose() {
        for (Layer currentLayer : this.layersToDispose) {
            currentLayer.dispose();
        }
        --layerManagerCount;
        for (Layer hiddenLayer : this.hideLayers.values()) {
            hiddenLayer.dispose();
        }
        for (LayerListener listener : this.layerListeners) {
            if (!(listener instanceof LayerListenerImplement)) continue;
            LayerListenerImplement layerListenerImplement = (LayerListenerImplement)listener;
            layerListenerImplement.dispose();
        }
        this.undoableEditReceiver.getUndoManager().discardAllEdits();
        this.layersToDispose.clear();
        this.hideLayers.clear();
        this.layerListeners.clear();
    }

    public static int layerManagerCount() {
        return layerManagerCount;
    }

    public Collection<Layer> getEditableLayers() {
        ArrayList<Layer> editableLayers = new ArrayList<Layer>();
        for (Layer layer : this.getLayers()) {
            if (!layer.isEditable()) continue;
            editableLayers.add(layer);
        }
        return editableLayers;
    }

    public Blackboard getBlackboard() {
        return this.blackboard;
    }

    public Collection<Layer> getLayersWithModifiedFeatureCollections() {
        ArrayList<Layer> layersWithModifiedFeatureCollections = new ArrayList<Layer>();
        Iterator<Layer> i = this.iterator();
        while (i.hasNext()) {
            Layer layer = i.next();
            if (!layer.isFeatureCollectionModified()) continue;
            layersWithModifiedFeatureCollections.add(layer);
        }
        return layersWithModifiedFeatureCollections;
    }

    public void moveCategory(int srcIndex, int destIndex) {
        if (srcIndex <= this.categories.size() && destIndex <= this.categories.size() && srcIndex != destIndex) {
            Collections.swap(this.categories, srcIndex, destIndex);
        }
    }

    public void addCategory(Category category, int index) {
        if (!this.categories.contains(category)) {
            this.categories.add(index, category);
            this.fireCategoryChanged(category, CategoryEventType.ADDED, this.indexOf(category));
        }
    }

    @Override
    public void featuresChanged(FeatureEvent e) {
    }

    @Override
    public void layerChanged(LayerEvent e) {
        if (e.getLayerable() != null) {
            this.remove(e.getLayerable());
        }
    }

    @Override
    public void categoryChanged(CategoryEvent e) {
    }

    public Collection<Layer> getHideLayers() {
        return this.hideLayers.values();
    }

    public Layer getHideLayer(String name) {
        return this.hideLayers.get(name);
    }

    public void addHideLayer(Layer layer) {
        layer.setHidden(true);
        this.hideLayers.put(layer.getName(), layer);
    }

    public void removeHideLayer(String name) {
        if (this.hideLayers.containsKey(name)) {
            this.hideLayers.remove(name);
        }
    }
}

