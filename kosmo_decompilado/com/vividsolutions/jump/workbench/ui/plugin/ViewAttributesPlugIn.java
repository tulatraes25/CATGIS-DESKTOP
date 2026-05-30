/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.CloneableInternalFrame;
import com.vividsolutions.jump.workbench.ui.InfoModel;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelListener;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelProxy;
import com.vividsolutions.jump.workbench.ui.OneLayerAttributeTab;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.SelectionManagerProxy;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.TaskFrameProxy;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.EditingPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.saig.jump.lang.I18N;

public class ViewAttributesPlugIn
extends AbstractPlugIn {
    public static final String NAME = String.valueOf(I18N.getString("workbench.ui.plugin.ViewAttributesPlugIn.name")) + "...";
    public static final Icon ICON = IconLoader.icon("Row.gif");

    public ViewAttributesPlugIn(EditingPlugIn editingPlugIn) {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        ViewAttributesFrame frame = new ViewAttributesFrame((Layer)context.getSelectedLayer(0), context);
        context.getWorkbenchFrame().addInternalFrame(frame);
        return true;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustNotBeRasterCheck()).add(checkFactory.createSelectedLayerMustBeActiveCheck()).add(checkFactory.createSelectedLayersWithPrimaryKeyCheck());
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return ViewAttributesPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static class ViewAttributesFrame
    extends JInternalFrame
    implements LayerManagerProxy,
    SelectionManagerProxy,
    LayerNamePanelProxy,
    TaskFrameProxy,
    LayerViewPanelProxy,
    LayerViewPanelListener {
        private static final long serialVersionUID = 1L;
        private LayerManager layerManager;
        private OneLayerAttributeTab attributeTab;
        private LayerListener layerListener;

        public ViewAttributesFrame(Layer layer, final PlugInContext context) {
            this.layerManager = context.getLayerManager();
            context.getLayerViewPanel().addListener(this);
            final InfoModel model = new InfoModel();
            this.addInternalFrameListener(new InternalFrameAdapter(){

                @Override
                public void internalFrameClosed(InternalFrameEvent e) {
                    if (ViewAttributesFrame.this.attributeTab.getModel() != null) {
                        ViewAttributesFrame.this.attributeTab.getModel().dispose();
                    }
                    model.dispose();
                    if (ViewAttributesFrame.this.getTaskFrame() != null) {
                        context.getWorkbenchFrame().activateFrame(ViewAttributesFrame.this.getTaskFrame());
                    }
                    context.getLayerViewPanel().removeListener(ViewAttributesFrame.this);
                    ViewAttributesFrame.this.removeLayerListeners(context);
                }
            });
            this.setResizable(true);
            this.setClosable(true);
            this.setMaximizable(true);
            this.setIconifiable(true);
            this.getContentPane().setLayout(new BorderLayout());
            this.attributeTab = new OneLayerAttributeTab(context.getWorkbenchContext(), ((TaskFrameProxy)((Object)context.getActiveInternalFrame())).getTaskFrame(), this).setLayer(layer);
            this.addInternalFrameListener(new InternalFrameAdapter(){

                @Override
                public void internalFrameOpened(InternalFrameEvent e) {
                    ViewAttributesFrame.this.attributeTab.getToolBar().updateEnabledState();
                }
            });
            this.getContentPane().add((Component)this.attributeTab, "Center");
            Rectangle parentBounds = context.getWorkbenchFrame().getDesktopPane().getBounds();
            Dimension dim = this.attributeTab.getTableSize();
            int frameHeight = 300;
            int desiredFrameWidth = Math.min(parentBounds.width, dim.width + 57);
            int toolbarWidth = (int)this.attributeTab.getToolBar().getPreferredSize().getWidth();
            if (desiredFrameWidth <= toolbarWidth) {
                this.setSize(toolbarWidth + 50, frameHeight);
            } else {
                this.setSize(desiredFrameWidth, frameHeight);
            }
            this.setLocation(0, Math.max(0, parentBounds.height - 300));
            this.updateTitle(this.attributeTab.getLayer());
            this.layerListener = new LayerListener(){

                @Override
                public void layerChanged(LayerEvent e) {
                    if (ViewAttributesFrame.this.attributeTab.getLayer() != null) {
                        ViewAttributesFrame.this.updateTitle(ViewAttributesFrame.this.attributeTab.getLayer());
                    }
                }

                @Override
                public void categoryChanged(CategoryEvent e) {
                }

                @Override
                public void featuresChanged(FeatureEvent e) {
                }
            };
            context.getLayerManager().addLayerListener(this.layerListener);
            Assert.isTrue((!(this instanceof CloneableInternalFrame) ? 1 : 0) != 0, (String)I18N.getString("workbench.ui.plugin.ViewAttributesPlugIn.there-can-be-no-other-views-on-the-infomodel"));
        }

        protected void removeLayerListeners(PlugInContext context) {
            if (context.getLayerManager() != null) {
                context.getLayerManager().removeLayerListener(this.layerListener);
                context.getLayerManager().removeLayerListeners(this.attributeTab.getLayerListeners());
            }
        }

        @Override
        public LayerViewPanel getLayerViewPanel() {
            return this.getTaskFrame().getLayerViewPanel();
        }

        @Override
        public LayerManager getLayerManager() {
            return this.layerManager;
        }

        private void updateTitle(Layer layer) {
            this.setTitle(String.valueOf(layer.isEditable() ? I18N.getString("workbench.ui.plugin.ViewAttributesPlugIn.edit-attributes") : I18N.getString("workbench.ui.plugin.ViewAttributesPlugIn.view-attributes")) + " : " + layer.getTitle());
        }

        @Override
        public TaskFrame getTaskFrame() {
            return this.attributeTab.getTaskFrame();
        }

        @Override
        public SelectionManager getSelectionManager() {
            return this.attributeTab.getTaskFrame().getSelectionManager();
        }

        @Override
        public LayerNamePanel getLayerNamePanel() {
            return this.attributeTab;
        }

        @Override
        public void selectionChanged() {
            this.attributeTab.setLayerViewPanelUpdates(false);
            try {
                Collection<Feature> col = this.getTaskFrame().getSelectionManager().getFeaturesWithSelectedItems(this.attributeTab.getLayer());
                this.attributeTab.selectFeatures(col, this.attributeTab.getLayer());
                this.attributeTab.updateRelationSelection(this.attributeTab.getLayer(), col);
            }
            finally {
                this.attributeTab.setLayerViewPanelUpdates(true);
            }
        }

        @Override
        public void cursorPositionChanged(String x, String y) {
        }

        @Override
        public void painted(Graphics graphics) {
        }

        @Override
        public void renderingFinished() {
        }

        @Override
        public void renderingStarted() {
        }

        public Layer getAssociatedLayer() {
            return this.attributeTab.getLayer();
        }
    }
}

