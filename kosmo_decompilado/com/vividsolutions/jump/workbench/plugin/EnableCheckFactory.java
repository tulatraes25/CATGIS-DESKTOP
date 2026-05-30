/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package com.vividsolutions.jump.workbench.plugin;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.JUMPWorkbenchContext;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.model.TextBalloonLayer;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelProxy;
import com.vividsolutions.jump.workbench.ui.SelectionManagerProxy;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.TaskFrameProxy;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.warp.WarpingVectorLayerFinder;
import es.kosmo.desktop.utils.GUITranslationsUtils;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.saig.core.geometry.Arc;
import org.saig.core.geometry.Circle;
import org.saig.core.geometry.Ellipse;
import org.saig.core.model.data.Table;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.core.model.sdi.wfs.WFSLayer;
import org.saig.core.styling.Style;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.balloon.DefineBalloonEditingScalePlugIn;
import org.saig.jump.plugin.utils.topology.ConfigureTopologyRulesPlugIn;
import org.saig.jump.util.KosmoDesktopUtils;
import org.saig.jump.util.LayerUtil;

public class EnableCheckFactory {
    protected static final Logger LOGGER = Logger.getLogger(EnableCheckFactory.class);
    protected WorkbenchContext workbenchContext;

    public EnableCheckFactory(WorkbenchContext workbenchContext) {
        Assert.isTrue((workbenchContext != null ? 1 : 0) != 0);
        this.workbenchContext = workbenchContext;
    }

    public WorkbenchContext getWorkbenchContext() {
        return this.workbenchContext;
    }

    public EnableCheck createAngleOfTheActiveViewMustBe(final double angle) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Viewport viewport;
                LayerViewPanel lvp = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel();
                if (lvp != null && (viewport = lvp.getViewport()) != null && viewport.getAngle() != angle) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-active-view-angle-must-be-{0}", new Object[]{angle});
                }
                return null;
            }
        };
    }

    public EnableCheck createTaskWindowMustBeActiveCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return !(EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame() instanceof TaskFrame) ? I18N.getString("workbench.plugin.EnableCheckFactory.a-task-window-must-be-active") : null;
            }
        };
    }

    public EnableCheck createAtLeastNTablesMustExistCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return EnableCheckFactory.this.workbenchContext.getDataManager().getTables().size() < n ? I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.It-must-exist-at-least-{0}-tables", new Object[]{n}) : null;
            }
        };
    }

    public EnableCheck createWindowWithSelectionManagerMustBeActiveCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return !(EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame() instanceof SelectionManagerProxy) ? I18N.getString("workbench.plugin.EnableCheckFactory.a-window-with-a-selection-manager-must-be-active") : null;
            }
        };
    }

    public EnableCheck createWindowWithLayerManagerMustBeActiveCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return !(EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame() instanceof LayerManagerProxy) ? I18N.getString("workbench.plugin.EnableCheckFactory.a-window-with-a-layer-manager-must-be-active") : null;
            }
        };
    }

    public EnableCheck createWindowWithAssociatedTaskFrameMustBeActiveCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return !(EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame() instanceof TaskFrameProxy) ? I18N.getString("workbench.plugin.EnableCheckFactory.a-window-with-an-associated-task-frame-must-be-active") : null;
            }
        };
    }

    public EnableCheck createWindowWithAssociatedTaskMustBeActiveCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                JInternalFrame activeInternalFrame = EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame();
                if (!(activeInternalFrame instanceof TaskFrameProxy)) {
                    return I18N.getString("workbench.plugin.EnableCheckFactory.a-window-with-an-associated-task-frame-must-be-active");
                }
                Task task = ((TaskFrameProxy)((Object)activeInternalFrame)).getTaskFrame().getTask();
                if (task == null) {
                    return I18N.getString("workbench.plugin.EnableCheckFactory.a-window-with-an-associated-task-must-be-active");
                }
                return null;
            }
        };
    }

    public EnableCheck createWindowWithLayerNamePanelMustBeActiveCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return !(EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame() instanceof LayerNamePanelProxy) ? I18N.getString("workbench.plugin.EnableCheckFactory.a-window-with-a-layer-name-panel-must-be-active") : null;
            }
        };
    }

    public EnableCheck createSelectedLayersMustBeNoInternals() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layerable[] layers = EnableCheckFactory.this.workbenchContext.createPlugInContext().getSelectedLayers();
                int i = 0;
                while (i < layers.length) {
                    Layer layer;
                    if (layers[i] instanceof Layer && (layer = (Layer)layers[i]).isInternal()) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.selected-layer-is-internal");
                    }
                    ++i;
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersMustNotBeAppInternalSystemLayersCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layerable[] layers = EnableCheckFactory.this.workbenchContext.createPlugInContext().getSelectedLayers();
                int i = 0;
                while (i < layers.length) {
                    Layer layer;
                    if (layers[i] instanceof Layer && LayerUtil.isAppInternalSystemLayer(layer = (Layer)layers[i])) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.selected-layer-is-a-system-layer");
                    }
                    ++i;
                }
                return null;
            }
        };
    }

    public EnableCheck createAttributeTabLayersMustNotBeHidden() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layerable[] layers = EnableCheckFactory.this.workbenchContext.createPlugInContext().getSelectedLayers();
                int i = 0;
                while (i < layers.length) {
                    Layer layer;
                    if (layers[i] instanceof Layer && (layer = (Layer)layers[i]).isHidden()) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.layers-shown-in-panel-can-not-be-hidden");
                    }
                    ++i;
                }
                return null;
            }
        };
    }

    public EnableCheck createWindowWithLayerViewPanelMustBeActiveCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return !(EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame() instanceof LayerViewPanelProxy) ? I18N.getString("workbench.plugin.EnableCheckFactory.a-window-with-a-layer-view-panel-must-be-active") : null;
            }
        };
    }

    public EnableCheck createNTasksMustBeExistsCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                JInternalFrame[] frames = EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getInternalFrames();
                int count = 0;
                int i = 0;
                while (i < frames.length) {
                    if (frames[i] instanceof TaskFrame) {
                        ++count;
                    }
                    ++i;
                }
                return count <= 1 ? I18N.getString("workbench.plugin.EnableCheckFactory.a-window-with-a-layer-view-panel-must-be-active") : null;
            }
        };
    }

    public EnableCheck createOnlyOneLayerMayHaveSelectedFeaturesCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                int numLayersWithSelectedItems = ((SelectionManagerProxy)((Object)EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame())).getSelectionManager().getLayersWithSelectedItems().size();
                return numLayersWithSelectedItems > 1 ? I18N.getString("workbench.plugin.EnableCheckFactory.only-one-layer-may-have-selected-features") : null;
            }
        };
    }

    public EnableCheck createOnlyOneLayerMayHaveSelectedItemsCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                int numLayersWithSelectedItems = ((SelectionManagerProxy)((Object)EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame())).getSelectionManager().getLayersWithSelectedItems().size();
                return numLayersWithSelectedItems > 1 ? I18N.getString("workbench.plugin.EnableCheckFactory.only-one-layer-may-have-selected-items") : null;
            }
        };
    }

    public EnableCheck createSelectedItemsLayersMustBeEditableCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Collection<Layer> col = EnableCheckFactory.this.workbenchContext.getLayerViewPanel().getSelectionManager().getLayersWithSelectedItems();
                for (Layer layer : col) {
                    if (layer.isEditable()) continue;
                    return String.valueOf(I18N.getString("workbench.plugin.EnableCheckFactory.selected-items-layers-must-be-editable")) + " (" + layer.getName() + ")";
                }
                return null;
            }
        };
    }

    public EnableCheck createExactlyNCategoriesMustBeSelectedCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (n != EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedCategories().size()) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.exactly-a-category-must-be-selected");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.exactly-{0}-categories-must-be-selected", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createExactlyNLayerablesMustBeSelectedCheck(final int n, final Class<? extends Layerable> layerableClass) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (EnableCheckFactory.this.workbenchContext.getLayerNamePanel() == null) {
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.An-active-view-must-exist");
                }
                if (n != EnableCheckFactory.this.workbenchContext.getLayerNamePanel().selectedNodes(layerableClass).size()) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.exactly-a-layer-must-be-selected");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.exactly-{0}-layers-must-be-selected", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayerMustHaveDomainsDefined() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layerable[] layers = EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers();
                if (layers.length == 0) {
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-one-layer-must-be-selected");
                }
                if (layers.length > 1) {
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.only-one-layer-must-be-selected");
                }
                if (!(layers[0] instanceof Layer)) {
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.the-selected-layer-must-be-valid-type");
                }
                Layer layer = (Layer)layers[0];
                if (layer.getDomains() == null) {
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.selected-layer-does-not-have-defined-domains");
                }
                return null;
            }
        };
    }

    public EnableCheck createExactlyNLayersMustBeSelectedCheck(int n) {
        return this.createExactlyNLayerablesMustBeSelectedCheck(n, Layer.class);
    }

    public EnableCheck createAtLeastNCategoriesMustBeSelectedCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (n > EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedCategories().size()) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.at-least-a-category-must-be-selected");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.at-least-{0}-categories-must-be-selected", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedCategoriesMustNotContainInnerLayersCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                LayerNamePanel layerNamePanel = EnableCheckFactory.this.workbenchContext.getLayerNamePanel();
                Iterator<Category> iterator = layerNamePanel.getSelectedCategories().iterator();
                while (iterator.hasNext()) {
                    Category objCategory;
                    Category category = objCategory = iterator.next();
                    for (Layerable objLayerable : category.getLayerables()) {
                        Layer layer;
                        Layerable layerable = objLayerable;
                        if (!(layerable instanceof Layer) || !(layer = (Layer)layerable).isInternal() && !LayerUtil.isAppInternalSystemLayer(layer)) continue;
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.selected-categories-must-not-contain-inner-layers");
                    }
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedCategoriesMustNotContainInternalLayersCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                LayerNamePanel layerNamePanel = EnableCheckFactory.this.workbenchContext.getLayerNamePanel();
                Iterator<Category> iterator = layerNamePanel.getSelectedCategories().iterator();
                while (iterator.hasNext()) {
                    Category objCategory;
                    Category category = objCategory = iterator.next();
                    for (Layerable objLayerable : category.getLayerables()) {
                        Layer layer;
                        Layerable layerable = objLayerable;
                        if (!(layerable instanceof Layer) || !(layer = (Layer)layerable).isInternal()) continue;
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.selected-categories-must-not-contain-inner-layers");
                    }
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNLayerablesMustBeSelectedCheck(final int n, final Class<? extends Layerable> layerableClass) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (EnableCheckFactory.this.workbenchContext.getLayerNamePanel() != null && n > EnableCheckFactory.this.workbenchContext.getLayerNamePanel().selectedNodes(layerableClass).size()) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.at-least-a-layer-must-be-selected");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.at-least-{0}-layers-must-be-selected", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNLayersMustBeSelectedCheck(int n) {
        return this.createAtLeastNLayerablesMustBeSelectedCheck(n, Layerable.class);
    }

    public EnableCheck createAtLeastNLayersMustBeEditableCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (n > EnableCheckFactory.this.workbenchContext.getLayerManager().getEditableLayers().size()) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.at-least-a-layer-must-be-editable");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.at-least-{0}-layers-must-be-editable", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNLayersMustExistCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (n > EnableCheckFactory.this.workbenchContext.getLayerManager().size()) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.at-least-a-layer-must-exist");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.at-least-{0}-layers-must-exist", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createAtMostNLayersMustExistCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (n < EnableCheckFactory.this.workbenchContext.getLayerManager().size()) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.at-most-a-layer-must-exist");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.at-most-{0}-layers-must-exist", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createExactlyNVectorsMustBeDrawnCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (n != EnableCheckFactory.this.vectorCount()) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.exactly-a-vector-must-be-drawn");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.exactly-{0}-vectors-must-be-drawn", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNVectorsMustBeDrawnCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (n > EnableCheckFactory.this.vectorCount()) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.at-least-a-vector-must-be-drawn");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.at-least-{0}-vectors-must-be-drawn", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNFeaturesMustBeSelectedCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                JInternalFrame activeInternalFrame = EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame();
                if (activeInternalFrame == null) {
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.An-active-view-must-exist");
                }
                if (!(activeInternalFrame instanceof SelectionManagerProxy)) {
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.An-active-view-must-exist");
                }
                int size = ((SelectionManagerProxy)((Object)activeInternalFrame)).getSelectionManager().getFeatureSelection().getNumFeaturesWithSelectedItems();
                if (n > size) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.at-least-a-feature-must-be-selected");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.at-least-{0}-features-must-be-selected", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNFeaturesMustBeSelectedCheckInLayer(final int n, final String layerName) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layer layer = JUMPWorkbench.getLayer(layerName);
                if (layer == null && (layer = JUMPWorkbench.getHiddenLayer(layerName)) == null) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.layer-{0}-does-not-exist", new Object[]{layerName});
                }
                int size = ((SelectionManagerProxy)((Object)EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame())).getSelectionManager().getFeatureSelection().getNumFeaturesWithSelectedItems(layer);
                if (n > size) {
                    if (n == 1) {
                        return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-one-element-must-be-selected-in-layer-{0}", new Object[]{layerName});
                    }
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-{0}-elements-must-be-selected-in-layer{1}", new Object[]{n, layerName});
                }
                return null;
            }
        };
    }

    public EnableCheck createExactlyNFeaturesMustBeSelectedCheckInLayer(final int n, final String layerName) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layer layer = JUMPWorkbench.getLayer(layerName);
                if (layer == null && (layer = JUMPWorkbench.getHiddenLayer(layerName)) == null) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.layer-{0}-does-not-exist", new Object[]{layerName});
                }
                boolean hasFrame = EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame() != null && EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame() != null && EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame() instanceof SelectionManagerProxy;
                int numSelected = -1;
                if (hasFrame) {
                    numSelected = ((SelectionManagerProxy)((Object)EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame())).getSelectionManager().getFeatureSelection().getNumFeaturesWithSelectedItems(layer);
                }
                if (numSelected == -1 || n != numSelected) {
                    if (n == 1) {
                        return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.exactly-one-element-from-layer-{0}-must-be-selected", new Object[]{layerName});
                    }
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.exactly-{0}-elements-from-layer-{1}-must-be-selected", new Object[]{n, layerName});
                }
                return null;
            }
        };
    }

    public EnableCheck createExactlyNFeaturesMustBeSelectedInSelectedLayersCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Collection<Layer> layers = EnableCheckFactory.this.workbenchContext.getLayerNamePanel().selectedNodes(Layer.class);
                boolean hasSelectedLayers = CollectionUtils.isNotEmpty(layers);
                boolean hasFrame = EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame() != null && EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame() != null && EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame() instanceof SelectionManagerProxy;
                int numSelected = 0;
                if (hasFrame && hasSelectedLayers) {
                    for (Layer layer : layers) {
                        numSelected += ((SelectionManagerProxy)((Object)EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame())).getSelectionManager().getFeatureSelection().getNumFeaturesWithSelectedItems(layer);
                    }
                }
                if (!hasSelectedLayers && n != 0 || hasSelectedLayers && n != numSelected) {
                    if (n == 1) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.exactly-one-element-from-selected-layers-must-be-selected");
                    }
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.exactly-{0}-elements-from-selected-layers-must-be-selected", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNFeaturesMustBeSelectedCheck(final int[] types, final int[] noTypes, final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Collection<Feature> features = ((SelectionManagerProxy)((Object)EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame())).getSelectionManager().getFeatureSelection().getFeaturesWithSelectedItems();
                if (n > features.size()) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.at-least-a-feature-must-be-selected");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.at-least-{0}-features-must-be-selected", new Object[]{n});
                }
                for (Feature element : features) {
                    int type;
                    boolean check = false;
                    int i = 0;
                    while (i < types.length && !check) {
                        type = types[i];
                        if (type == 1) {
                            if (element.getGeometry() instanceof Point) {
                                check = true;
                            }
                        } else if (type == 8) {
                            if (element.getGeometry() instanceof MultiPoint) {
                                check = true;
                            }
                        } else if (type == 10) {
                            if (element.getGeometry() instanceof Circle) {
                                check = true;
                            }
                        } else if (type == 11) {
                            if (element.getGeometry() instanceof Ellipse) {
                                check = true;
                            }
                        } else if (type == 9) {
                            if (element.getGeometry() instanceof Arc) {
                                check = true;
                            }
                        } else if (type == 5) {
                            if (element.getGeometry() instanceof Polygon) {
                                check = true;
                            }
                        } else if (type == 4) {
                            if (element.getGeometry() instanceof MultiPolygon) {
                                check = true;
                            }
                        } else if (type == 3) {
                            if (element.getGeometry() instanceof LineString) {
                                check = true;
                            }
                        } else if (type == 2 && element.getGeometry() instanceof MultiLineString) {
                            check = true;
                        }
                        ++i;
                    }
                    if (!check) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.Incorrect-geometry-type");
                    }
                    if (noTypes == null) continue;
                    i = 0;
                    while (i < noTypes.length && check) {
                        type = noTypes[i];
                        if (type == 1) {
                            if (element.getGeometry() instanceof Point) {
                                check = false;
                            }
                        } else if (type == 8) {
                            if (element.getGeometry() instanceof MultiPoint) {
                                check = false;
                            }
                        } else if (type == 10) {
                            if (element.getGeometry() instanceof Circle) {
                                check = false;
                            }
                        } else if (type == 11) {
                            if (element.getGeometry() instanceof Ellipse) {
                                check = false;
                            }
                        } else if (type == 9) {
                            if (element.getGeometry() instanceof Arc) {
                                check = false;
                            }
                        } else if (type == 5) {
                            if (element.getGeometry() instanceof Polygon) {
                                check = false;
                            }
                        } else if (type == 4) {
                            if (element.getGeometry() instanceof MultiPolygon) {
                                check = false;
                            }
                        } else if (type == 3) {
                            if (element.getGeometry() instanceof LineString) {
                                check = false;
                            }
                        } else if (type == 2 && element.getGeometry() instanceof MultiLineString) {
                            check = false;
                        }
                        ++i;
                    }
                    if (check) continue;
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.Incorrect-geometry-type");
                }
                return null;
            }
        };
    }

    public EnableCheck createExactlyNFeaturesMustBeSelectedCheck(final int[] types, final int[] noTypes, final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Collection<Feature> features = ((SelectionManagerProxy)((Object)EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame())).getSelectionManager().getFeatureSelection().getFeaturesWithSelectedItems();
                if (n != features.size()) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.exactly-a-feature-must-be-selected");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.exactly-{0}-features-must-be-selected", new Object[]{n});
                }
                for (Feature element : features) {
                    int type;
                    boolean check = false;
                    int i = 0;
                    while (i < types.length && !check) {
                        type = types[i];
                        if (type == 1) {
                            if (element.getGeometry() instanceof Point) {
                                check = true;
                            }
                        } else if (type == 8) {
                            if (element.getGeometry() instanceof MultiPoint) {
                                check = true;
                            }
                        } else if (type == 5) {
                            if (element.getGeometry() instanceof Polygon) {
                                check = true;
                            }
                        } else if (type == 4) {
                            if (element.getGeometry() instanceof MultiPolygon) {
                                check = true;
                            }
                        } else if (type == 4) {
                            if (element.getGeometry() instanceof MultiPolygon) {
                                check = true;
                            }
                        } else if (type == 3) {
                            if (element.getGeometry() instanceof LineString) {
                                check = true;
                            }
                        } else if (type == 2 && element.getGeometry() instanceof MultiLineString) {
                            check = true;
                        }
                        ++i;
                    }
                    if (!check) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.Incorrect-geometry-type");
                    }
                    if (noTypes == null) continue;
                    i = 0;
                    while (i < noTypes.length && check) {
                        type = noTypes[i];
                        if (type == 1) {
                            if (element.getGeometry() instanceof Point) {
                                check = false;
                            }
                        } else if (type == 8) {
                            if (element.getGeometry() instanceof MultiPoint) {
                                check = false;
                            }
                        } else if (type == 10) {
                            if (element.getGeometry() instanceof Circle) {
                                check = false;
                            }
                        } else if (type == 11) {
                            if (element.getGeometry() instanceof Ellipse) {
                                check = false;
                            }
                        } else if (type == 9) {
                            if (element.getGeometry() instanceof Arc) {
                                check = false;
                            }
                        } else if (type == 5) {
                            if (element.getGeometry() instanceof Polygon) {
                                check = true;
                            }
                        } else if (type == 4) {
                            if (element.getGeometry() instanceof MultiPolygon) {
                                check = true;
                            }
                        } else if (type == 4) {
                            if (element.getGeometry() instanceof MultiPolygon) {
                                check = true;
                            }
                        } else if (type == 3) {
                            if (element.getGeometry() instanceof LineString) {
                                check = true;
                            }
                        } else if (type == 2 && element.getGeometry() instanceof MultiLineString) {
                            check = true;
                        }
                        ++i;
                    }
                    if (check) continue;
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.Incorrect-geometry-type");
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNItemsMustBeSelectedCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (EnableCheckFactory.this.workbenchContext.getLayerViewPanel() != null && EnableCheckFactory.this.workbenchContext.getLayerViewPanel().getSelectionManager() != null) {
                    if (n > EnableCheckFactory.this.workbenchContext.getLayerViewPanel().getSelectionManager().getNumFeaturesWithSelectedItems()) {
                        if (n == 1) {
                            return I18N.getString("workbench.plugin.EnableCheckFactory.at-least-a-item-must-be-selected");
                        }
                        return I18N.getMessage("workbench.plugin.EnableCheckFactory.at-least-{0}-items-must-be-selected", new Object[]{n});
                    }
                    return null;
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNSegmentsMustBeSelectedCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (n > EnableCheckFactory.this.workbenchContext.getLayerViewPanel().getSelectionManager().getSegmentSelection().getSelectedItems().size()) {
                    if (n == 1) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-one-segment-must-be-selected");
                    }
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-{0}-segments-must-be-selected", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNItemsMustHaveSelectedSegmentsCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (n > EnableCheckFactory.this.workbenchContext.getLayerViewPanel().getSelectionManager().getSegmentSelection().getNumFeaturesWithSelectedItems()) {
                    if (n == 1) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-one-element-must-have-selected-segments");
                    }
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-{0}-elements-must-have-selected-segments", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createExactlyNFeaturesMustBeSelectedCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                boolean hasFrame = EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame() != null && EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame() != null && EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame() instanceof SelectionManagerProxy;
                int numSelected = -1;
                if (hasFrame) {
                    numSelected = ((SelectionManagerProxy)((Object)EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame())).getSelectionManager().getFeatureSelection().getNumFeaturesWithSelectedItems();
                }
                if (numSelected == -1 || n != numSelected) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.exactly-a-feature-must-be-selected");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.exactly-{0}-features-must-be-selected", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createLayerWithSelectedFeaturesNameMustBe(final String layerName) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (EnableCheckFactory.this.workbenchContext.getLayerViewPanel().getSelectionManager().getLayersWithSelectedItems().size() > 0) {
                    Collection<Layer> layerCollection = Collections.unmodifiableCollection(EnableCheckFactory.this.workbenchContext.getLayerViewPanel().getSelectionManager().getLayersWithSelectedItems());
                    Layer selectedLayer = layerCollection.iterator().next();
                    if (!selectedLayer.getName().equals(layerName)) {
                        return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-is-not-selected", new Object[]{layerName});
                    }
                    return null;
                }
                return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-is-not-selected", new Object[]{layerName});
            }
        };
    }

    public EnableCheck createLayerWithSelectedFeaturesNameMustBeAny(final String[] layerNames) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (EnableCheckFactory.this.workbenchContext.getLayerViewPanel().getSelectionManager().getLayersWithSelectedItems().size() > 0) {
                    Collection<Layer> layerCollection = Collections.unmodifiableCollection(EnableCheckFactory.this.workbenchContext.getLayerViewPanel().getSelectionManager().getLayersWithSelectedItems());
                    Layer selectedLayer = layerCollection.iterator().next();
                    if (!EnableCheckFactory.equalsToAny(layerNames, selectedLayer.getName())) {
                        return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-is-not-selected", new Object[]{EnableCheckFactory.buildNameMessage(layerNames)});
                    }
                    return null;
                }
                return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-is-not-selected", new Object[]{EnableCheckFactory.buildNameMessage(layerNames)});
            }
        };
    }

    public EnableCheck createSelectedLayerNameMustBeAny(final String[] layerNames) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layerable[] layers = EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers();
                if (layers.length > 0) {
                    Layer selectedLayer = (Layer)layers[0];
                    if (!EnableCheckFactory.equalsToAny(layerNames, selectedLayer.getName())) {
                        return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-is-not-selected", new Object[]{EnableCheckFactory.buildNameMessage(layerNames)});
                    }
                    return null;
                }
                return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-is-not-selected", new Object[]{EnableCheckFactory.buildNameMessage(layerNames)});
            }
        };
    }

    public EnableCheck createExactlyNItemsMustBeSelectedCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                int numSelected = ((SelectionManagerProxy)((Object)EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame())).getSelectionManager().getFeatureSelection().getNumFeaturesWithSelectedItems();
                if (n != numSelected) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.exactly-an-item-must-be-selected");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.exactly-{0}-items-must-be-selected", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createExactlyNLayersMustHaveSelectedItemsCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                int numLayersWithSelectedItems = ((SelectionManagerProxy)((Object)EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame())).getSelectionManager().getLayersWithSelectedItems().size();
                if (n != numLayersWithSelectedItems) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.exactly-a-layer-must-have-selected-items");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.exactly-{0}-layers-must-have-selected-items", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createExactlyNFeaturesMustHaveSelectedItemsCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                int numSelected = ((SelectionManagerProxy)((Object)EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame())).getSelectionManager().getNumFeaturesWithSelectedItems();
                if (n != numSelected) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.exactly-a-feature-must-have-selected-items");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.exactly-{0}-features-must-have-selected-items", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersMustBeEditableCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (Layerable obj : Arrays.asList(EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers())) {
                    Layer layer;
                    if (!(obj instanceof Layer) || (layer = (Layer)obj).isEditable()) continue;
                    return String.valueOf(I18N.getString("workbench.plugin.EnableCheckFactory.selected-layers-must-be-editable")) + " (" + layer.getName() + ")";
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersMustHaveTopologyRelationsCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (Layerable obj : Arrays.asList(EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers())) {
                    Layer layer;
                    if (!(obj instanceof Layer) || !(layer = (Layer)obj).getTopologyRelations().isEmpty()) continue;
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.layer-{0}-does-not-have-topology-relations", new Object[]{layer.getName()});
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersMustNotBeEditableCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (Layerable obj : Arrays.asList(EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers())) {
                    Layer layer;
                    if (!(obj instanceof Layer) || !(layer = (Layer)obj).isEditable()) continue;
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-is-editable", new Object[]{layer.getName()});
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersMustNotVersionableWithTimeCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (Layerable obj : Arrays.asList(EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers())) {
                    Layer layer;
                    if (!(obj instanceof Layer) || !(layer = (Layer)obj).isVersionable() || layer.getVersionableViewDate() == null) continue;
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-is-not-editable", new Object[]{layer.getName()});
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersMustNotBeWMSLayersCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (Layerable obj : Arrays.asList(EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers())) {
                    if (!(obj instanceof WMSLayer)) continue;
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.At-least-one-layer-is-WMS-type");
                }
                return null;
            }
        };
    }

    public EnableCheck createLayerMustNotBeSelectedItemsCheck(final String layerName) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                int n = EnableCheckFactory.this.workbenchContext.getLayerViewPanel().getSelectionManager().getNumFeaturesWithSelectedItems(JUMPWorkbench.getLayer(layerName));
                if (n > 0) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.layer-{0}-can-not-have-selected-elements", new Object[]{layerName});
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNLayersMustNotBeWMSLayersCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                int nonWMS = 0;
                for (Layer layer : EnableCheckFactory.this.workbenchContext.getLayerManager().getLayers()) {
                    if (layer.isRaster() || !layer.isEnabled()) continue;
                    ++nonWMS;
                }
                if (nonWMS >= n) {
                    return null;
                }
                return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.At-least-{0}-layers-must-not-be-WMS", new Object[]{n});
            }
        };
    }

    public EnableCheck createSelectedLayersWithPrimaryKeyCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (Layerable obj : Arrays.asList(EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers())) {
                    Layer layer;
                    if (!(obj instanceof Layer) || (layer = (Layer)obj).getFeatureSchema() != null && layer.getFeatureSchema().getPrimaryKey() != null) continue;
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-must-have-primary-key", new Object[]{layer.getName()});
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersMustNotBeRasterCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (Layerable obj : Arrays.asList(EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers())) {
                    Layer layer;
                    if (!(obj instanceof Layer) || !(layer = (Layer)obj).isRaster()) continue;
                    return I18N.getString("workbench.plugin.EnableCheckFactory.at-least-one-raster-layer-is-selected");
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersMustBeRasterCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (Layerable obj : Arrays.asList(EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers())) {
                    if (!(obj instanceof Layer)) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.At-least-one-selected-layer-is-not-raster");
                    }
                    Layer layer = (Layer)obj;
                    if (layer.isRaster()) continue;
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.At-least-one-selected-layer-is-not-raster");
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersWithHiperLinkCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (Layerable obj : Arrays.asList(EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers())) {
                    Layer layer;
                    if (!(obj instanceof Layer) || (layer = (Layer)obj).getHiperLink() != null) continue;
                    return I18N.getString("workbench.plugin.EnableCheckFactory.a-hiperlink-is-not-configured");
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNSelectedLayerablesWithHiperLinkCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layerable[] selectedLayerables = EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers();
                int cont = 0;
                if (selectedLayerables.length >= n) {
                    Layerable[] layerableArray = selectedLayerables;
                    int n3 = selectedLayerables.length;
                    int n2 = 0;
                    while (n2 < n3) {
                        Layer layer;
                        Layerable currentLayerable = layerableArray[n2];
                        if (currentLayerable instanceof Layer && (layer = (Layer)currentLayerable).getHiperLink() != null) {
                            ++cont;
                        }
                        ++n2;
                    }
                }
                if (cont < n) {
                    if (n == 1) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-one-selected-layer-must-have-configured-hiperlinks");
                    }
                    I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-{0}-selected-layers-must-have-configured-hiperlinks", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNVisibleLayerablesWithHiperLinkCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                List<Layerable> visibleLayerables = EnableCheckFactory.this.workbenchContext.getLayerManager().getVisibleLayerables();
                int cont = 0;
                for (Layerable currentLayerable : visibleLayerables) {
                    Layer layer;
                    if (!(currentLayerable instanceof Layer) || (layer = (Layer)currentLayerable).getHiperLink() == null) continue;
                    ++cont;
                }
                if (cont < n) {
                    if (n == 1) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-one-visible-layer-must-have-configured-hiperlinks");
                    }
                    I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-{0}-visible-layers-must-have-configured-hiperlinks", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayerMustBeActiveCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layerable[] layers = EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers();
                int i = 0;
                while (i < layers.length) {
                    if (!layers[i].isEnabled()) {
                        return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-must-be-active", new Object[]{layers[i].getName()});
                    }
                    ++i;
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersWithRelationCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (Layerable obj : Arrays.asList(EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers())) {
                    Layer layer;
                    if (!(obj instanceof Layer) || (layer = (Layer)obj).hasRelations()) continue;
                    return I18N.getString("workbench.plugin.EnableCheckFactory.the-layer-has-not-any-relations-configured");
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNVisibleLayersMustNotBeRasterCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                int noRasters = 0;
                for (Layerable obj : EnableCheckFactory.this.workbenchContext.getLayerManager().getVisibleLayerables()) {
                    if (!(obj instanceof Layer) && obj instanceof Layerable && obj.isVisible()) {
                        ++noRasters;
                        continue;
                    }
                    Layer layer = (Layer)obj;
                    if (layer.isRaster() || !layer.isVisible()) continue;
                    ++noRasters;
                }
                if (noRasters >= n) {
                    return null;
                }
                return I18N.getMessage("workbench.plugin.EnableCheckFactory.at-least-{0}-non-raster-visible-layers-must-exist", new Object[]{n});
            }
        };
    }

    public EnableCheck createAtLeastNVisibleLayersMustBeRasterCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                int rasters = 0;
                for (Layerable obj : EnableCheckFactory.this.workbenchContext.getLayerManager().getVisibleLayerables()) {
                    Layer layer;
                    if (!(obj instanceof Layer) || !(layer = (Layer)obj).isRaster() || !layer.isVisible()) continue;
                    ++rasters;
                }
                if (rasters >= n) {
                    return null;
                }
                return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.At-least-it-must-exist-{0}-visible-raster-layers", new Object[]{n});
            }
        };
    }

    public EnableCheck createAtLeastNLayersMustBeActiveCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                int enabled = 0;
                for (Layer layer : EnableCheckFactory.this.workbenchContext.getLayerManager().getLayers()) {
                    if (layer.isRaster() || !layer.isEnabled()) continue;
                    ++enabled;
                }
                if (enabled >= n) {
                    return null;
                }
                return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.At-least-{0}-layers-must-be-active", new Object[]{n});
            }
        };
    }

    public EnableCheck createLayerMustBeVersionableCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layer layer = (Layer)EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers()[0];
                FeatureSchema schema = layer.getFeatureSchema();
                int count = 0;
                int i = 0;
                while (i < schema.getAttributeCount()) {
                    Attribute attr = schema.getAttribute(i);
                    if (attr.getType().equals(AttributeType.TIMESTAMP)) {
                        ++count;
                    }
                    ++i;
                }
                if (count <= 1) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-has-not-got-its-time-variable-enabled", new Object[]{layer.getName()});
                }
                return null;
            }
        };
    }

    public EnableCheck createLayerIsNotVersionable() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layer layer = (Layer)EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers()[0];
                FeatureSchema schema = layer.getFeatureSchema();
                int count = 0;
                int i = 0;
                while (i < schema.getAttributeCount()) {
                    Attribute attr = schema.getAttribute(i);
                    if (attr.getType().equals(AttributeType.TIMESTAMP)) {
                        ++count;
                    }
                    ++i;
                }
                if (count >= 2) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-has-its-time-variable-enabled", new Object[]{layer.getName()});
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayerIsNotCAD() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layerable layerable = EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers()[0];
                if (layerable instanceof Layer) {
                    if (LayerUtil.isCADLayer((Layer)layerable)) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-selected-layer-is-CAD-type");
                    }
                    return null;
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayerIsCADCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layer layer = (Layer)EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers()[0];
                if (LayerUtil.isCADLayer(layer)) {
                    return null;
                }
                return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-selected-layer-is-not-CAD-type");
            }
        };
    }

    public EnableCheck createLayerIsVersionableCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layer layer = (Layer)EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers()[0];
                if (!layer.isVersionable()) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-has-not-got-its-time-variable-enabled", new Object[]{layer.getName()});
                }
                return null;
            }
        };
    }

    public EnableCheck createLayerMustBeDataBaseCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layer layer = (Layer)EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers()[0];
                if (!layer.isDataBaseDataSource()) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-must-be-a-database-layer", new Object[]{layer.getName()});
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNLayersMustNotBeRasterCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                int noRasters = 0;
                for (Layer layer : EnableCheckFactory.this.workbenchContext.getLayerManager().getLayers()) {
                    if (layer.isRaster()) continue;
                    ++noRasters;
                }
                if (noRasters >= n) {
                    return null;
                }
                return I18N.getMessage("workbench.plugin.EnableCheckFactory.at-least-{0}-non-raster-layers-must-exist", new Object[]{n});
            }
        };
    }

    public EnableCheck createAtLeastNLayersMustBeRasterCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                int rasters = 0;
                for (Layer layer : EnableCheckFactory.this.workbenchContext.getLayerManager().getLayers()) {
                    if (!layer.isRaster()) continue;
                    ++rasters;
                }
                if (rasters >= n) {
                    return null;
                }
                return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.At-least-{0}-layers-must-be-raster", new Object[]{n});
            }
        };
    }

    public EnableCheck createFenceMustBeDrawnCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return EnableCheckFactory.this.workbenchContext.getLayerViewPanel().getFence() == null ? I18N.getString("workbench.plugin.EnableCheckFactory.a-fence-must-be-drawn") : null;
            }
        };
    }

    public EnableCheck createEditableLayerTypeGeometryCheck(final int[] types) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                boolean check = false;
                if (!EnableCheckFactory.this.workbenchContext.getLayerManager().getEditableLayers().isEmpty()) {
                    Layer layer = EnableCheckFactory.this.workbenchContext.getLayerManager().getEditableLayers().iterator().next();
                    int layerType = layer.getFeatureCollectionWrapper().getFeatureSchema().getGeometryType();
                    int i = 0;
                    while (i < types.length && !check) {
                        check = layerType == types[i];
                        ++i;
                    }
                }
                return !check ? String.valueOf(I18N.getString("workbench.plugin.EnableCheckFactory.incompatible-geometry-types")) + " " + GUITranslationsUtils.getGeometryTypeDescription(types) : null;
            }
        };
    }

    public EnableCheck createSelectedLayerTypeGeometryCheck(final int[] types) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                boolean check = true;
                Layerable[] layers = EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers();
                if (layers.length != 0) {
                    int i = 0;
                    while (i < layers.length && check) {
                        if (layers[i] instanceof Layerable && !(layers[i] instanceof Layer)) {
                            return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.At-least-one-layer-is-WMS-type");
                        }
                        Layer layer = (Layer)layers[i];
                        int layerType = layer.getFeatureCollectionWrapper().getFeatureSchema().getGeometryType();
                        boolean coincideAlgunTipo = false;
                        int j = 0;
                        while (j < types.length && !coincideAlgunTipo) {
                            coincideAlgunTipo = layerType == types[j];
                            ++j;
                        }
                        check = coincideAlgunTipo;
                        ++i;
                    }
                }
                return !check ? String.valueOf(I18N.getString("workbench.plugin.EnableCheckFactory.incompatible-geometry-types")) + " " + GUITranslationsUtils.getGeometryTypeDescription(types) : null;
            }
        };
    }

    public EnableCheck createAtLeastNLayersTypeGeometryCheck(final int n, final int[] types) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                int validLayers = 0;
                Iterator<Layer> i = EnableCheckFactory.this.workbenchContext.getLayerManager().getLayers().iterator();
                while (i.hasNext() && validLayers < n) {
                    Layer layer = i.next();
                    boolean valido = false;
                    int j = 0;
                    while (j < types.length && !valido) {
                        if (layer.getGeometryType() == types[j]) {
                            ++validLayers;
                            valido = true;
                        }
                        ++j;
                    }
                }
                if (validLayers >= n) {
                    return null;
                }
                return String.valueOf(I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.At-least-{0}-layers-must-have-specific-geometry-type", new Object[]{n})) + GUITranslationsUtils.getGeometryTypeDescription(types);
            }
        };
    }

    public EnableCheck createBetweenNAndMVectorsMustBeDrawnCheck(final int min, final int max) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (EnableCheckFactory.this.vectorCount() > max || EnableCheckFactory.this.vectorCount() < min) {
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.between-{0}-and-{1}-vectors-must-be-drawn", new Object[]{min, max});
                }
                return null;
            }
        };
    }

    private int vectorCount() {
        return new WarpingVectorLayerFinder(this.workbenchContext).getVectors().size();
    }

    public EnableCheck createAtLeastNFeaturesMustHaveSelectedItemsCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                int numSelected = ((SelectionManagerProxy)((Object)EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame())).getSelectionManager().getFeatureSelection().getNumFeaturesWithSelectedItems();
                if (n > numSelected) {
                    if (n == 1) {
                        return I18N.getString("workbench.plugin.EnableCheckFactory.at-least-a-feature-must-have-selected-items");
                    }
                    return I18N.getMessage("workbench.plugin.EnableCheckFactory.at-least-{0}-features-must-have-selected-items", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createProjectMustBeOpenedCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (EnableCheckFactory.this.workbenchContext.getProject() == null) {
                    return I18N.getString("workbench.plugin.EnableCheckFactory.there-is-not-any-project-loaded");
                }
                return null;
            }
        };
    }

    public EnableCheck createLayerNameMustExistCheck(final String layerName) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layer layer = EnableCheckFactory.this.workbenchContext.getLayerManager().getLayer(layerName);
                if (layer == null) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.layer-{0}-does-not-exist", new Object[]{layerName});
                }
                return null;
            }
        };
    }

    public EnableCheck createLayerNameMustNotExistCheck(final String layerName) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layer layer = EnableCheckFactory.this.workbenchContext.getLayerManager().getLayer(layerName);
                if (layer != null) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.layer-{0}-already-exists", new Object[]{layerName});
                }
                return null;
            }
        };
    }

    public EnableCheck createTableNameMustExistCheck(final String tableName) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Table table = JUMPWorkbench.getTable(tableName);
                if (table == null) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-table-{0}-does-not-exist", new Object[]{tableName});
                }
                return null;
            }
        };
    }

    public EnableCheck createEditableLayerNameMustBe(final String layerName) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Collection<Layer> editableLayers = EnableCheckFactory.this.workbenchContext.getLayerManager().getEditableLayers();
                if (editableLayers.size() == 0) {
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.There-is-no-editable-layer");
                }
                Layer editableLayer = editableLayers.iterator().next();
                if (!editableLayer.getName().equals(layerName)) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-is-not-in-editable-mode", new Object[]{layerName});
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersMustNotBeInMemoryCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (Layerable obj : Arrays.asList(EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers())) {
                    Layer layer;
                    if (!(obj instanceof Layer) || !(layer = (Layer)obj).isMemory()) continue;
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.At-least-one-of-the-selected-layers-is-in-memory");
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersMustNotBeFromMemoryCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (Layerable obj : Arrays.asList(EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers())) {
                    Layer layer;
                    if (!(obj instanceof Layer) || !(layer = (Layer)obj).isEnabled() || layer.isMemory() || !(layer.getUltimateFeatureCollectionWrapper() instanceof FeatureDataset)) continue;
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.selected-layer-must-not-be-from-memory");
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersMustNotBeReprojectedCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (Layerable obj : Arrays.asList(EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers())) {
                    Layer layer;
                    if (!(obj instanceof Layer) || (layer = (Layer)obj).getCoordTrans() == null) continue;
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.At-least-one-of-the-selected-layers-is-reprojected");
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersMustBeReprojectedCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (Layerable obj : Arrays.asList(EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers())) {
                    Layer layer;
                    if (!(obj instanceof Layer) || (layer = (Layer)obj).getCoordTrans() != null) continue;
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.selected-layers-must-be-reprojected");
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayerMustHaveMetadata() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                String document;
                Layerable[] layers = EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers();
                if (layers.length > 0 && ((document = layers[0].getMetadata()) == null || document.isEmpty())) {
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.selected-layer-must-have-metadata");
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNEnabledTopologyRulesMustExistCheck(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                List<ITopologyRelation> relations = ConfigureTopologyRulesPlugIn.recoverCurrentRelations(EnableCheckFactory.this.workbenchContext.getLayerManager());
                if (n > relations.size()) {
                    if (n == 1) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-one-topological-rule-must-exist");
                    }
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-{0}-topological-rules-must-exist", new Object[]{n});
                }
                int cont = 0;
                Iterator<ITopologyRelation> iterator = relations.iterator();
                while (iterator.hasNext() && cont < n) {
                    ITopologyRelation currentTopologyRule = iterator.next();
                    if (!currentTopologyRule.isEnabled()) continue;
                    ++cont;
                }
                if (n > cont) {
                    if (n == 1) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-one-active-topological-rule-must-exist");
                    }
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-{0}-active-topological-rules-must-exist-(-{1}-active)", new Object[]{n, cont});
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersMustHaveFeaturesCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                boolean anyOk = false;
                Layerable[] layers = EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers();
                if (layers.length > 0) {
                    int i = 0;
                    while (i < layers.length && !anyOk) {
                        Layerable currentLayerable = layers[i];
                        if (currentLayerable.isEnabled()) {
                            if (currentLayerable instanceof Layer) {
                                Layer currentLayer = (Layer)currentLayerable;
                                int size = -1;
                                try {
                                    size = currentLayer.getFeatureCollectionWrapper().size();
                                }
                                catch (Exception e) {
                                    LOGGER.error((Object)"", (Throwable)e);
                                }
                                anyOk = currentLayer.getFeatureCollectionWrapper() != null && size > 0;
                            } else if (currentLayerable instanceof WMSLayer) {
                                Envelope env = ((WMSLayer)currentLayerable).getFullEnvelope();
                                anyOk = env != null && !env.isNull();
                            } else if (currentLayerable instanceof TextBalloonLayer) {
                                anyOk = true;
                            }
                        }
                        ++i;
                    }
                    if (!anyOk) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.selected-layers-has-no-elements");
                    }
                }
                return null;
            }
        };
    }

    public EnableCheck createFileMustBeReadableCheck(final File file) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (file.canRead()) {
                    return null;
                }
                if (file.exists()) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.file-{0}-can-not-be-read", new Object[]{file.getAbsolutePath()});
                }
                return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.file-{0}-does-not-exist", new Object[]{file.getAbsolutePath()});
            }
        };
    }

    public EnableCheck createTaskViewMustNotBeTemporallyDisplacedCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                boolean temporallyDisplaced = false;
                Iterator<Layer> i = EnableCheckFactory.this.workbenchContext.getLayerManager().getLayers().iterator();
                while (i.hasNext() && !temporallyDisplaced) {
                    Layer layer = i.next();
                    if (!layer.isVersionable() || layer.getVersionableViewDate() == null) continue;
                    temporallyDisplaced = true;
                }
                if (temporallyDisplaced) {
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.view-is-temporally-displaced");
                }
                return null;
            }
        };
    }

    public EnableCheck createTaskViewMustBeTemporallyDisplacedCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                boolean temporallyDisplaced = false;
                Iterator<Layer> i = EnableCheckFactory.this.workbenchContext.getLayerManager().getLayers().iterator();
                while (i.hasNext() && !temporallyDisplaced) {
                    Layer layer = i.next();
                    if (!layer.isVersionable() || layer.getVersionableViewDate() == null) continue;
                    temporallyDisplaced = true;
                }
                if (temporallyDisplaced) {
                    return null;
                }
                return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-view-is-not-temporally-displaced");
            }
        };
    }

    public EnableCheck createAtLeastNVisibleLayerablesMustBeQueryable(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                List<Layerable> visibleLayerables = EnableCheckFactory.this.workbenchContext.getLayerManager().getVisibleLayerables();
                int cont = 0;
                if (visibleLayerables.size() >= n) {
                    for (Layerable currentLayerable : visibleLayerables) {
                        if (!LayerUtil.isQueryable(currentLayerable)) continue;
                        ++cont;
                    }
                }
                if (cont < n) {
                    if (n == 1) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-one-visible-layer-must-be-queryable");
                    }
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-{0}-visible-layers-must-be-queryable", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createAtLeastNSelectedLayerablesMustBeQueryable(final int n) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layerable[] selectedLayerables = EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers();
                int cont = 0;
                if (selectedLayerables.length >= n) {
                    Layerable[] layerableArray = selectedLayerables;
                    int n3 = selectedLayerables.length;
                    int n2 = 0;
                    while (n2 < n3) {
                        Layerable currentLayerable = layerableArray[n2];
                        if (LayerUtil.isQueryable(currentLayerable)) {
                            ++cont;
                        }
                        ++n2;
                    }
                }
                if (cont < n) {
                    if (n == 1) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-one-selected-layer-must-be-queryable");
                    }
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.at-least-{0}-selected-layers-must-be-queryable", new Object[]{n});
                }
                return null;
            }
        };
    }

    public EnableCheck createMustExistHiddenLayerNamed(final String hiddenLayerName) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return ((JUMPWorkbenchContext)JUMPWorkbench.getFrameInstance().getContext()).getLayerManager().getHideLayer(hiddenLayerName) == null ? I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-hidden-layer-{0}-must-exist", new Object[]{hiddenLayerName}) : null;
            }
        };
    }

    public EnableCheck createMustExistProjectFile() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                File pf = JUMPWorkbench.getFrameInstance().getContext().getProjectFile();
                if (pf == null) {
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.A-project-file-must-exist");
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayerMustBeBalloonLayerCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layerable[] layerables = EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers();
                return layerables.length == 1 && layerables[0] instanceof TextBalloonLayer ? null : I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.selected-layer-must-be-balloon-type");
            }
        };
    }

    public EnableCheck createBalloonEditingScaleMustBeDefinedCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Integer number = (Integer)PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(DefineBalloonEditingScalePlugIn.EDITING_SCALE_KEY);
                if (number == null) {
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.an-balloon-editing-scale-must-be-defined");
                }
                if ((double)Math.round(JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getScale()) != number.doubleValue()) {
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.must-be-at-balloon-edition-scale");
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersHasFinderDefined() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layerable[] layers = EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers();
                int i = 0;
                while (i < layers.length) {
                    if (layers[i] instanceof Layer) {
                        Layer layer = (Layer)layers[i];
                        if (layer.getFinderFields() == null || layer.getFinderFields().isEmpty()) {
                            return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-finder-must-be-configured");
                        }
                    } else {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.Vectorial-layers-must-be-selected");
                    }
                    ++i;
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayerNameMustBe(final String layerName) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layerable[] layers = EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers();
                if (layers.length > 0) {
                    Layer selectedLayer = (Layer)layers[0];
                    if (!selectedLayer.getName().equals(layerName)) {
                        return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-is-not-selected", new Object[]{layerName});
                    }
                    return null;
                }
                return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-is-not-selected", new Object[]{layerName});
            }
        };
    }

    public EnableCheck createSelectedLayerMustHaveOnlyOneFeatureTypeStyleCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (EnableCheckFactory.this.workbenchContext.getLayerNamePanel() == null) {
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.An-active-view-must-exist");
                }
                Collection<Layer> layers = EnableCheckFactory.this.workbenchContext.getLayerNamePanel().selectedNodes(Layer.class);
                if (layers.size() == 1) {
                    Layer selectedLayer = layers.iterator().next();
                    Style modelStyle = selectedLayer.getModelStyle();
                    if (modelStyle == null || modelStyle.getFeatureTypeStyles().length != 1) {
                        return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-selected-layer-does-not-have-more-than-one-selectable-style");
                    }
                } else {
                    return I18N.getString("workbench.plugin.EnableCheckFactory.exactly-a-layer-must-be-selected");
                }
                return null;
            }
        };
    }

    public EnableCheck createTaskProjectionCodeMustBe(final String projCode) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (!(EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame() instanceof TaskFrame)) {
                    return I18N.getString("workbench.plugin.EnableCheckFactory.a-task-window-must-be-active");
                }
                TaskFrame taskFrame = (TaskFrame)EnableCheckFactory.this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame();
                IProjection taskProj = taskFrame.getTask().getProjection();
                if (taskProj == null) {
                    return I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.Active-view-projection-is-null");
                }
                if (!StringUtils.equalsIgnoreCase((String)taskProj.getFullCode(), (String)projCode)) {
                    return String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.Active-view-projection-is-not-valid")) + " (" + taskProj.getFullCode() + ", " + projCode + ")";
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedLayersMustNotBeLabelLayersCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                for (Layerable obj : Arrays.asList(EnableCheckFactory.this.workbenchContext.getLayerNamePanel().getSelectedLayers())) {
                    Layer layer;
                    if (!(obj instanceof Layer) || !LayerUtil.isLabelLayer(layer = (Layer)obj)) continue;
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.Selected-layer-{0}-is-label-type-layer", new Object[]{layer.getTitle()});
                }
                return null;
            }
        };
    }

    private static boolean equalsToAny(String[] any, String str) {
        boolean eq = false;
        int i = 0;
        while (i < any.length && !eq) {
            eq = str.equals(any[i]);
            ++i;
        }
        return eq;
    }

    private static String buildNameMessage(String[] names) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[ ");
        int i = 0;
        while (i < names.length) {
            buffer.append(names);
            if (i < names.length - 1) {
                buffer.append(", ");
            }
            ++i;
        }
        buffer.append(" ]");
        return buffer.toString();
    }

    public EnableCheck createEditableLayerMustBeDataBaseCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layer layer = KosmoDesktopUtils.getEditableLayer();
                if (layer == null) {
                    return null;
                }
                if (!layer.isDataBaseDataSource()) {
                    String errorMsg = I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.The-layer-{0}-must-be-a-database-layer", new Object[]{layer.getName()});
                    return errorMsg;
                }
                return null;
            }
        };
    }

    public EnableCheck createAllSelectedFeaturesAreInLayerCheck(final String layerName) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                int numSelectedFeaturesInLayer;
                Layer layer = JUMPWorkbench.getLayer(layerName);
                if (layer == null) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.layer-{0}-does-not-exist", new Object[]{layerName});
                }
                int numSelectedFeatures = KosmoDesktopUtils.getSelectedFeatures().size();
                if (numSelectedFeatures != (numSelectedFeaturesInLayer = KosmoDesktopUtils.getSelectedFeatures(layer).size())) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.All-the-selected-features-must-be-at-the-layer-{0}", new Object[]{layerName});
                }
                return null;
            }
        };
    }

    public EnableCheck createSelectedWFSLayersMustBeTransactionalCheck() {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layerable[] layers = EnableCheckFactory.this.workbenchContext.createPlugInContext().getSelectedLayers();
                int i = 0;
                while (i < layers.length) {
                    WFSLayer wfsLayer;
                    if (layers[i] instanceof WFSLayer && !(wfsLayer = (WFSLayer)layers[i]).isTransactional()) {
                        return String.valueOf(I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.Selected-WFS-layer-{0}-is-not-transactional", new Object[]{wfsLayer.getTitle()})) + " (WFS-T)";
                    }
                    ++i;
                }
                return null;
            }
        };
    }

    public EnableCheck createEditableLayerMustHaveAtLeastOneValidAttributeType(final Set<AttributeType> validAttrTypes) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layer layer = KosmoDesktopUtils.getEditableLayer();
                if (layer == null) {
                    return null;
                }
                FeatureSchema schema = layer.getFeatureSchema();
                boolean ok = false;
                int i = 0;
                while (i < schema.getAttributeCount() && !ok) {
                    Attribute attr = schema.getAttribute(i);
                    if (!attr.isPrimaryKey() && !attr.getType().equals(AttributeType.GEOMETRY)) {
                        ok = CollectionUtils.isNotEmpty((Collection)validAttrTypes) ? validAttrTypes.contains(attr.getType()) : true;
                    }
                    ++i;
                }
                if (!ok) {
                    return I18N.getMessage("com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.the-editable-layer-{0}-must-have-at-least-one-non-primary-key-attribute-with-type-{1}", new Object[]{layer.getTitle(), GUITranslationsUtils.getAttributeTypesDescription(validAttrTypes)});
                }
                return null;
            }
        };
    }
}

