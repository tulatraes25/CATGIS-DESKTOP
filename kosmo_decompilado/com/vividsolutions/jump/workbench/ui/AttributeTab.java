/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.commons.collections.CollectionUtils
 *  org.cresques.cts.ICoordTrans
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.AttributePanel;
import com.vividsolutions.jump.workbench.ui.AttributeTablePanel;
import com.vividsolutions.jump.workbench.ui.EnableableToolBar;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.InfoModel;
import com.vividsolutions.jump.workbench.ui.InfoModelListener;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelListener;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerTableModel;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.TitledPopupMenu;
import com.vividsolutions.jump.workbench.ui.TreeLayerNamePanel;
import com.vividsolutions.jump.workbench.ui.cursortool.FeatureInfoTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.ClearSelectionPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.DeleteSelectedItemsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInfoPlugIn;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import es.kosmo.desktop.plugins.analysis.CalculateAttributeByExpressionPlugIn;
import es.kosmo.desktop.plugins.editing.DiscardChangesPlugIn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.collections.CollectionUtils;
import org.cresques.cts.ICoordTrans;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.model.relations.Relation;
import org.saig.core.util.SwingWorker;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.editing.CommitPlugIn;
import org.saig.jump.plugin.stats.CalculateStatsPlugIn;
import org.saig.jump.plugin.utils.window.AlwaysOnTopPlugIn;

public class AttributeTab
extends JPanel
implements LayerNamePanel {
    private static final long serialVersionUID = 1L;
    private BorderLayout borderLayout1 = new BorderLayout();
    protected boolean layerViewPanelUpdates = true;
    private Layer lastRightClickLayer;
    private ErrorHandler errorHandler;
    private TaskFrame taskFrame;
    private LayerManagerProxy layerManagerProxy;
    private EnableCheck taskFrameEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            return !AttributeTab.this.taskFrame.isVisible() ? I18N.getString("workbench.ui.AttributeTab.task-frame-must-be-open") : null;
        }
    };
    private EnableCheck layersEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            return AttributeTab.this.panel.getModel().getLayers().isEmpty() ? I18N.getString("workbench.ui.AttributeTab.one-or-more-layers-must-be-present") : null;
        }
    };
    private EnableCheck exactlyOneLayersMustBePresentEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            int numLayers = AttributeTab.this.panel.getModel().getLayers().size();
            if (numLayers != 1) {
                return I18N.getString("com.vividsolutions.jump.workbench.ui.AttributeTab.only-one-layer-must-be-present");
            }
            return null;
        }
    };
    private EnableCheck singlePresentLayerMustNotBeEditableEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            List<Layer> layers = AttributeTab.this.panel.getModel().getLayers();
            if (layers.get(0).isEditable()) {
                return I18N.getString("com.vividsolutions.jump.workbench.ui.AttributeTab.layer-must-not-be-in-edition");
            }
            return null;
        }
    };
    private EnableCheck singlePresentLayerMustBeEditableEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            List<Layer> layers = AttributeTab.this.panel.getModel().getLayers();
            if (!layers.get(0).isEditable()) {
                return I18N.getString("com.vividsolutions.jump.workbench.ui.AttributeTab.layer-must-be-in-edition");
            }
            return null;
        }
    };
    private EnableCheck singlePresentLayerMustHaveChangesEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            List<Layer> layers = AttributeTab.this.panel.getModel().getLayers();
            if (!layers.get(0).isEditable()) {
                return I18N.getString("com.vividsolutions.jump.workbench.ui.AttributeTab.layer-must-be-in-edition");
            }
            if (!layers.get(0).isFeatureCollectionModified()) {
                return I18N.getString("org.saig.jump.plugin.editing.CommitPlugIn.at-least-one-layer-must-be-modified");
            }
            return null;
        }
    };
    private EnableCheck rowsSelectedEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            return !AttributeTab.this.panel.hasSelectedFeatures() ? I18N.getString("workbench.ui.AttributeTab.one-or-more-rows-must-be-selected") : null;
        }
    };
    private EnableCheck layersWithSelectedFeaturesAreEditableEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            return !AttributeTab.this.panel.layerWithSelectedFeaturesAreEditable() ? I18N.getString("workbench.ui.AttributeTab.one-or-more-rows-must-be-selected") : null;
        }
    };
    private AttributePanel panel;
    private JScrollPane scrollPane = new JScrollPane();
    private EnableableToolBar toolBar = new EnableableToolBar();
    private InfoModel model;
    private Layer[] selectedLayers = new Layer[0];
    private Layer[] lastSelectedLayers = new Layer[0];
    private Dimension tableSize;
    protected LayerListener layerListener;

    public InfoModel getModel() {
        return this.model;
    }

    public AttributeTab(InfoModel model, WorkbenchContext workbenchContext, TaskFrame taskFrame, LayerManagerProxy layerManagerProxy, boolean addScrollPanesToChildren) {
        this.layerManagerProxy = layerManagerProxy;
        this.model = model;
        this.taskFrame = taskFrame;
        taskFrame.addInternalFrameListener(GUIUtil.toInternalFrameListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AttributeTab.this.toolBar.updateEnabledState();
            }
        }));
        this.panel = new AttributePanel(model, workbenchContext, taskFrame, layerManagerProxy, addScrollPanesToChildren){
            private static final long serialVersionUID = 1L;

            @Override
            public void layerAdded(LayerTableModel layerTableModel) {
                super.layerAdded(layerTableModel);
                final AttributeTablePanel tablePanel = this.getTablePanel(layerTableModel.getLayer());
                if (AttributeTab.this.tableSize == null || tablePanel.getTableSize().getWidth() > AttributeTab.this.tableSize.getWidth()) {
                    AttributeTab.this.tableSize = tablePanel.getTableSize();
                }
                MouseAdapter mouseListener = new MouseAdapter(){

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (!SwingUtilities.isRightMouseButton(e)) {
                            return;
                        }
                        int column = tablePanel.getTable().columnAtPoint(e.getPoint());
                        if (column != -1) {
                            tablePanel.setRightClickColumn(column);
                            AttributeTab.this.setLastRightClickLayer(tablePanel.getModel().getLayer());
                            TitledPopupMenu popupMenu = JUMPWorkbench.getFrameInstance().getAttributeTabLayerNamePopupMenu();
                            popupMenu.setTitle(String.valueOf(tablePanel.getModel().getLayer().getTitle()) + " - " + tablePanel.getTable().getColumnName(column));
                            AttributeTab.this.lastSelectedLayers = new Layer[]{tablePanel.getModel().getLayer()};
                            AttributeTab.setEnableLastSelectedLayers(true, AttributeTab.this);
                            try {
                                popupMenu.show(tablePanel.getLayerNameRenderer(), e.getX(), e.getY());
                            }
                            finally {
                                AttributeTab.setEnableLastSelectedLayers(false, AttributeTab.this);
                            }
                        }
                    }
                };
                tablePanel.addMouseListener(mouseListener);
                tablePanel.getTable().addMouseListener(mouseListener);
                tablePanel.getTable().getTableHeader().addMouseListener(mouseListener);
                tablePanel.getLayerNameRenderer().addMouseListener(mouseListener);
            }
        };
        this.panel.setBackground(Color.BLUE);
        this.layerListener = new LayerListener(){

            @Override
            public void featuresChanged(FeatureEvent e) {
            }

            @Override
            public void layerChanged(LayerEvent e) {
                if (e.getType() == LayerEventType.METADATA_CHANGED) {
                    AttributeTab.this.toolBar.updateEnabledState();
                }
            }

            @Override
            public void categoryChanged(CategoryEvent e) {
            }
        };
        layerManagerProxy.getLayerManager().addLayerListener(this.layerListener);
        model.addListener(new InfoModelListener(){

            @Override
            public void layerAdded(LayerTableModel layerTableModel) {
                AttributeTab.this.panel.getTablePanel(layerTableModel.getLayer()).getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener(){

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        AttributeTab.this.toolBar.updateEnabledState();
                    }
                });
                AttributeTab.this.toolBar.updateEnabledState();
            }

            @Override
            public void layerRemoved(LayerTableModel layerTableModel) {
                AttributeTab.this.toolBar.updateEnabledState();
            }
        });
        this.errorHandler = workbenchContext.getErrorHandler();
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        this.initScrollPane();
        if (addScrollPanesToChildren) {
            this.remove(this.scrollPane);
            this.add((Component)this.panel, "Center");
        }
        this.installToolBarButtons(workbenchContext, taskFrame);
        this.toolBar.updateEnabledState();
    }

    public void setLastRightClickLayer(Layer layer) {
        if (this.panel.getModel().getLayers().contains(layer)) {
            this.lastRightClickLayer = layer;
        }
    }

    public Dimension getTableSize() {
        return this.tableSize;
    }

    private void installToolBarButtons(WorkbenchContext workbenchContext, final TaskFrame taskFrame) {
        TaskMonitorManager taskMonitor = new TaskMonitorManager();
        this.toolBar.add(new JButton(), I18N.getString("workbench.ui.AttributeTab.zoom-to-previous-row"), IconLoader.icon("SmallUp.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    AttributeTab.this.zoom(AttributeTab.this.panel.topSelectedRow().previousRow());
                    AttributeTab.this.panel.selectInLayerViewPanel();
                }
                catch (Throwable t) {
                    AttributeTab.this.errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(this.taskFrameEnableCheck).add(this.layersEnableCheck));
        this.toolBar.add(new JButton(), I18N.getString("workbench.ui.AttributeTab.zoom-to-next-row"), IconLoader.icon("SmallDown.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    AttributeTab.this.zoom(AttributeTab.this.panel.topSelectedRow().nextRow());
                    AttributeTab.this.panel.selectInLayerViewPanel();
                }
                catch (Throwable t) {
                    AttributeTab.this.errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(this.taskFrameEnableCheck).add(this.layersEnableCheck));
        this.toolBar.add(new JButton(), I18N.getString("workbench.ui.AttributeTab.zoom-to-selected-rows"), IconLoader.icon("SmallMagnify.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    AttributeTab.this.panel.zoom(AttributeTab.this.panel.selectedGeometries());
                }
                catch (Throwable t) {
                    AttributeTab.this.errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(this.taskFrameEnableCheck).add(this.layersEnableCheck).add(this.rowsSelectedEnableCheck));
        this.toolBar.add(new JButton(), I18N.getString("workbench.ui.AttributeTab.zoom-to-full-extent"), IconLoader.icon("SmallWorld.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    taskFrame.getLayerViewPanel().getViewport().zoomToFullExtent();
                }
                catch (Throwable t) {
                    AttributeTab.this.errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(this.taskFrameEnableCheck).add(this.layersEnableCheck));
        this.toolBar.add(new JButton(), I18N.getString("workbench.ui.AttributeTab.inverse"), IconLoader.icon("inverse.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new InverseSelectionWaitDialog(JUMPWorkbench.getFrameInstance(), true).setVisible(true);
                }
                catch (Throwable t) {
                    AttributeTab.this.errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(this.taskFrameEnableCheck).add(this.layersEnableCheck));
        ClearSelectionPlugIn clearSelectionPlugIn = new ClearSelectionPlugIn();
        this.toolBar.add(new JButton(), clearSelectionPlugIn.getName(), GUIUtil.toSmallIcon(clearSelectionPlugIn.getIcon()), AbstractPlugIn.toActionListener(clearSelectionPlugIn, workbenchContext, taskMonitor), new MultiEnableCheck().add(this.taskFrameEnableCheck).add(this.layersEnableCheck).add(this.rowsSelectedEnableCheck));
        this.toolBar.add(new JButton(), I18N.getString("workbench.ui.AttributeTab.flash-selected-rows"), IconLoader.icon("Flashlight.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    AttributeTab.this.panel.flashSelectedFeatures();
                }
                catch (Throwable t) {
                    AttributeTab.this.errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(this.taskFrameEnableCheck).add(this.layersEnableCheck).add(this.rowsSelectedEnableCheck));
        FeatureInfoPlugIn featureInfoPlugIn = new FeatureInfoPlugIn();
        this.toolBar.add(new JButton(), featureInfoPlugIn.getName(), GUIUtil.toSmallIcon(FeatureInfoTool.ICON), FeatureInfoPlugIn.toActionListener(featureInfoPlugIn, workbenchContext, taskMonitor), new MultiEnableCheck().add(this.taskFrameEnableCheck).add(this.layersEnableCheck).add(this.rowsSelectedEnableCheck));
        DeleteSelectedItemsPlugIn deleleteSelectedItemsPlugin = new DeleteSelectedItemsPlugIn();
        this.toolBar.add(new JButton(), deleleteSelectedItemsPlugin.getName(), GUIUtil.toSmallIcon(DeleteSelectedItemsPlugIn.ICON), DeleteSelectedItemsPlugIn.toActionListener(deleleteSelectedItemsPlugin, workbenchContext, taskMonitor), new MultiEnableCheck().add(this.taskFrameEnableCheck).add(this.layersEnableCheck).add(this.rowsSelectedEnableCheck).add(this.layersWithSelectedFeaturesAreEditableEnableCheck));
        final AlwaysOnTopPlugIn alwaysOnTopPlugin = new AlwaysOnTopPlugIn((JInternalFrame)((Object)this.layerManagerProxy));
        JButton alwaysOnTopButton = new JButton();
        alwaysOnTopButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                ((JButton)arg0.getSource()).setIcon(GUIUtil.toSmallIcon(alwaysOnTopPlugin.getIcon()));
            }
        });
        this.toolBar.add(alwaysOnTopButton, alwaysOnTopPlugin.getName(), GUIUtil.toSmallIcon(alwaysOnTopPlugin.getIcon()), AlwaysOnTopPlugIn.toActionListener(alwaysOnTopPlugin, workbenchContext, null), new MultiEnableCheck().add(this.taskFrameEnableCheck));
        CalculateStatsPlugIn calculateStatsPlugIn = new CalculateStatsPlugIn(2);
        this.toolBar.add(new JButton(), calculateStatsPlugIn.getName(), GUIUtil.toSmallIcon(CalculateStatsPlugIn.ICON), CalculateStatsPlugIn.toActionListener(calculateStatsPlugIn, workbenchContext, taskMonitor), new MultiEnableCheck().add(this.taskFrameEnableCheck).add(this.layersEnableCheck).add(this.exactlyOneLayersMustBePresentEnableCheck).add(this.singlePresentLayerMustNotBeEditableEnableCheck));
        CalculateAttributeByExpressionPlugIn calculatorPlugIn = new CalculateAttributeByExpressionPlugIn();
        this.toolBar.add(new JButton(), calculatorPlugIn.getName(), GUIUtil.toSmallIcon(CalculateAttributeByExpressionPlugIn.ICON), CalculateAttributeByExpressionPlugIn.toActionListener(calculatorPlugIn, workbenchContext, taskMonitor), new MultiEnableCheck().add(this.taskFrameEnableCheck).add(this.layersEnableCheck).add(this.exactlyOneLayersMustBePresentEnableCheck).add(this.singlePresentLayerMustBeEditableEnableCheck));
        this.toolBar.add(new JButton(), I18N.getString("workbench.ui.AttributeTab.AttributeTab.move-selection-to-the-top"), IconLoader.icon("SelectionUp.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    AttributeTab.this.setLayerViewPanelUpdates(false);
                    int i = 0;
                    while (i < AttributeTab.this.getSelectedLayers().length) {
                        AttributeTab.this.panel.getTablePanel((Layer)AttributeTab.this.getSelectedLayers()[i]).sortSelectedFeatures(true);
                        ++i;
                    }
                    AttributeTab.this.setLayerViewPanelUpdates(true);
                }
                catch (Throwable t) {
                    AttributeTab.this.errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(this.taskFrameEnableCheck).add(this.layersEnableCheck).add(this.rowsSelectedEnableCheck));
        this.toolBar.add(new JButton(), I18N.getString("workbench.ui.AttributeTab.AttributeTab.move-selection-to-the-bottom"), IconLoader.icon("SelectionDown.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    AttributeTab.this.setLayerViewPanelUpdates(false);
                    int i = 0;
                    while (i < AttributeTab.this.getSelectedLayers().length) {
                        AttributeTab.this.panel.getTablePanel((Layer)AttributeTab.this.getSelectedLayers()[i]).sortSelectedFeatures(false);
                        ++i;
                    }
                    AttributeTab.this.setLayerViewPanelUpdates(true);
                }
                catch (Throwable t) {
                    AttributeTab.this.errorHandler.handleThrowable(t);
                }
            }
        }, new MultiEnableCheck().add(this.taskFrameEnableCheck).add(this.layersEnableCheck).add(this.rowsSelectedEnableCheck));
        this.toolBar.addSeparator();
        DiscardChangesPlugIn discardChangesPlugIn = new DiscardChangesPlugIn();
        this.toolBar.add(new JButton(), discardChangesPlugIn.getName(), discardChangesPlugIn.getIcon(), DiscardChangesPlugIn.toActionListener(discardChangesPlugIn, workbenchContext, taskMonitor), new MultiEnableCheck().add(this.taskFrameEnableCheck).add(this.layersEnableCheck).add(this.exactlyOneLayersMustBePresentEnableCheck).add(this.singlePresentLayerMustBeEditableEnableCheck).add(this.singlePresentLayerMustHaveChangesEnableCheck));
        CommitPlugIn commitPlugIn = new CommitPlugIn();
        this.toolBar.add(new JButton(), commitPlugIn.getName(), commitPlugIn.getIcon(), CommitPlugIn.toActionListener(commitPlugIn, workbenchContext, taskMonitor), new MultiEnableCheck().add(this.taskFrameEnableCheck).add(this.layersEnableCheck).add(this.exactlyOneLayersMustBePresentEnableCheck).add(this.singlePresentLayerMustBeEditableEnableCheck).add(this.singlePresentLayerMustHaveChangesEnableCheck));
    }

    public TaskFrame getTaskFrame() {
        return this.taskFrame;
    }

    @Override
    public Layer chooseEditableLayer() {
        return TreeLayerNamePanel.chooseEditableLayer(this);
    }

    @Override
    public LayerManager getLayerManager() {
        return this.layerManagerProxy.getLayerManager();
    }

    void jbInit() throws Exception {
        this.setLayout(this.borderLayout1);
        this.toolBar.setOrientation(0);
        this.scrollPane.getViewport().add((Component)this.panel, null);
        this.add((Component)this.scrollPane, "Center");
        this.add((Component)this.toolBar, "North");
    }

    private void initScrollPane() {
        this.scrollPane.getVerticalScrollBar().setUnitIncrement(new JTable().getRowHeight());
    }

    private void zoom(AttributePanel.Row row) throws NoninvertibleTransformException {
        this.panel.clearSelection();
        row.getPanel().getTable().getSelectionModel().setSelectionInterval(row.getIndex(), row.getIndex());
        Rectangle r = row.getPanel().getTable().getCellRect(row.getIndex(), 0, true);
        row.getPanel().getTable().scrollRectToVisible(r);
        if (row.isFirstRow()) {
            row.getPanel().scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        }
        ArrayList<Geometry> geometries = new ArrayList<Geometry>();
        ICoordTrans coordTrans = row.getPanel().getModel().getLayer().getCoordTrans();
        Geometry geom = null;
        if (coordTrans != null) {
            IShapeGeometry pathGeom = ShapeGeometryConverter.jts_to_igeometry(row.getFeature().getGeometry());
            pathGeom.reProject(coordTrans);
            geom = ShapeGeometryConverter.java2d_to_jts(pathGeom.getShp());
        } else {
            geom = row.getFeature().getGeometry();
        }
        geometries.add(geom);
        this.panel.zoom(geometries);
    }

    private static void setEnableLastSelectedLayers(boolean enabled, AttributeTab attributeTab) {
        attributeTab.selectedLayers = enabled ? attributeTab.lastSelectedLayers : new Layer[]{};
    }

    @Override
    public Collection<Category> getSelectedCategories() {
        return new ArrayList<Category>();
    }

    @Override
    public Layerable[] getSelectedLayers() {
        if (this.model.getLayers().size() == 1) {
            return new Layer[]{this.model.getLayers().get(0)};
        }
        return this.selectedLayers;
    }

    public void selectFeatures(Collection<Feature> col, Layer layer) {
        this.panel.selectFeatures(col, layer);
    }

    public Collection<Layerable> selectedNodes(Class c) {
        if (!Layerable.class.isAssignableFrom(c)) {
            return new ArrayList<Layerable>();
        }
        return Arrays.asList(this.getSelectedLayers());
    }

    public AttributePanel getPanel() {
        return this.panel;
    }

    public EnableableToolBar getToolBar() {
        return this.toolBar;
    }

    @Override
    public void addListener(LayerNamePanelListener listener) {
    }

    @Override
    public void removeListener(LayerNamePanelListener listener) {
    }

    @Override
    public void dispose() {
        this.lastRightClickLayer = null;
    }

    @Override
    public void saveStatus() {
    }

    @Override
    public void loadStatus() {
    }

    public void setLayerViewPanelUpdates(boolean b) {
        this.layerViewPanelUpdates = b;
    }

    public boolean isLayerViewPanelUpdates() {
        return this.layerViewPanelUpdates;
    }

    public void explicitSort(boolean ascending) {
        AttributeTablePanel atp = this.panel.getTablePanel(this.lastRightClickLayer);
        if (atp != null) {
            atp.explicitSortRight(ascending);
        }
    }

    public List<LayerListener> getLayerListeners() {
        ArrayList<LayerListener> listeners = new ArrayList<LayerListener>();
        listeners.add(this.layerListener);
        listeners.addAll(this.panel.getLayerListeners());
        return listeners;
    }

    public Layer getLastRightClickLayer() {
        return this.lastRightClickLayer;
    }

    public String getLastRightClickColumnName() {
        AttributeTablePanel atp = this.panel.getTablePanel(this.lastRightClickLayer);
        if (atp != null) {
            return atp.getLastRightClickColumnName();
        }
        return null;
    }

    public static EnableCheck createNotGeometryRightClickEnableCheck(final WorkbenchContext workbenchContext) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                LayerNamePanelProxy panelProxy = (LayerNamePanelProxy)((Object)workbenchContext.getWorkbench().getFrame().getActiveInternalFrame());
                AttributeTab attributeTab = (AttributeTab)panelProxy.getLayerNamePanel();
                if (attributeTab.getLastRightClickLayer() == null) {
                    return I18N.getString("com.vividsolutions.jump.workbench.ui.AttributeTab.click-were-not-done-over-a-table");
                }
                String columnName = attributeTab.getLastRightClickColumnName();
                if (columnName == null) {
                    return I18N.getString("com.vividsolutions.jump.workbench.ui.AttributeTab.click-were-not-done-over-a-column");
                }
                if (columnName.equals("....")) {
                    return I18N.getString("com.vividsolutions.jump.workbench.ui.AttributeTab.it-is-not-applicable-over-geometric-field");
                }
                return null;
            }
        };
    }

    public static EnableCheck createNotEditableRightClickEnableCheck(final WorkbenchContext workbenchContext) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                LayerNamePanelProxy panelProxy = (LayerNamePanelProxy)((Object)workbenchContext.getWorkbench().getFrame().getActiveInternalFrame());
                AttributeTab attributeTab = (AttributeTab)panelProxy.getLayerNamePanel();
                if (attributeTab.getLastRightClickLayer() == null) {
                    return I18N.getString("com.vividsolutions.jump.workbench.ui.AttributeTab.click-were-not-done-over-a-table");
                }
                if (attributeTab.getLastRightClickLayer().isEditable()) {
                    return I18N.getString("com.vividsolutions.jump.workbench.ui.AttributeTab.layer-must-not-be-in-edition");
                }
                return null;
            }
        };
    }

    public void updateRelationSelection(Layer layer, Collection<Feature> selectedFeatures) {
        Collection<Relation<?>> relations = layer.getAllRelations();
        if (CollectionUtils.isNotEmpty(relations)) {
            this.panel.updateRelationSelection(layer, relations, selectedFeatures, true);
        }
    }

    private class InverseSelectionWaitDialog
    extends JDialog {
        private static final long serialVersionUID = 1L;

        public InverseSelectionWaitDialog(JFrame parent, boolean modal) {
            super((Frame)parent, modal);
            this.getContentPane().setLayout(new BorderLayout());
            this.setTitle(String.valueOf(I18N.getString("workbench.ui.AttributeTab.inverse")) + " ...");
            JLabel label = new JLabel();
            label.setIcon(IconLoader.icon("loading.gif"));
            label.setHorizontalAlignment(0);
            this.getContentPane().add((Component)label, "Center");
            this.setSize(new Dimension(200, 100));
            GUIUtil.centreOnWindow(this);
            SwingWorker worker = new SwingWorker(){

                @Override
                public Object construct() {
                    try {
                        AttributeTab.this.panel.inverseSelection();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        InverseSelectionWaitDialog.this.dispose();
                    }
                    return null;
                }

                @Override
                public void finished() {
                    InverseSelectionWaitDialog.this.closeWindow();
                }
            };
            worker.start();
        }

        public void closeWindow() {
            this.dispose();
        }
    }
}

