/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.ICoordTrans
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.ui.AttributeTab;
import com.vividsolutions.jump.workbench.ui.AttributeTablePanel;
import com.vividsolutions.jump.workbench.ui.AttributeTablePanelListener;
import com.vividsolutions.jump.workbench.ui.InfoFrame;
import com.vividsolutions.jump.workbench.ui.InfoModel;
import com.vividsolutions.jump.workbench.ui.InfoModelListener;
import com.vividsolutions.jump.workbench.ui.LayerTableModel;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedItemsPlugIn;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.Table;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.model.relations.LayerRelation;
import org.saig.core.model.relations.Relation;
import org.saig.core.model.relations.RelationType;
import org.saig.core.model.relations.TableRelation;
import org.saig.core.util.I18NUnsupportedOperationException;

public class AttributePanel
extends JPanel
implements InfoModelListener,
AttributeTablePanelListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AttributePanel.class);
    private Map<Layer, AttributeTablePanel> layerToTablePanelMap = new HashMap<Layer, AttributeTablePanel>();
    private InfoModel model;
    private WorkbenchContext workbenchContext;
    private ZoomToSelectedItemsPlugIn zoomToSelectedItemsPlugIn = new ZoomToSelectedItemsPlugIn();
    private Row nullRow = new Row(){

        @Override
        public boolean isFirstRow() {
            return AttributePanel.this.rowCount() == 0;
        }

        @Override
        public boolean isLastRow() {
            return AttributePanel.this.rowCount() == 0;
        }

        @Override
        public AttributeTablePanel getPanel() {
            throw new I18NUnsupportedOperationException();
        }

        @Override
        public int getIndex() {
            throw new I18NUnsupportedOperationException();
        }

        @Override
        public Row nextRow() {
            return this.firstRow();
        }

        @Override
        public Row previousRow() {
            return this.firstRow();
        }

        private Row firstRow() {
            return new BasicRow(AttributePanel.this.getTablePanel(AttributePanel.this.getModel().getLayers().get(0)), 0);
        }

        @Override
        public Feature getFeature() {
            throw new I18NUnsupportedOperationException();
        }
    };
    private TaskFrame taskFrame;
    private LayerManagerProxy layerManagerProxy;
    private boolean addScrollPanesToChildren;

    protected AttributePanel(InfoModel model, WorkbenchContext workbenchContext, TaskFrame taskFrame, LayerManagerProxy layerManagerProxy, boolean addScrollPanesToChildren) {
        this.addScrollPanesToChildren = addScrollPanesToChildren;
        this.taskFrame = taskFrame;
        this.workbenchContext = workbenchContext;
        this.layerManagerProxy = layerManagerProxy;
        this.setModel(model);
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public AttributeTablePanel getTablePanel(Layer layer) {
        return this.layerToTablePanelMap.get(layer);
    }

    public InfoModel getModel() {
        return this.model;
    }

    public void setModel(InfoModel model) {
        this.model = model;
        model.addListener(this);
    }

    @Override
    public void layerAdded(LayerTableModel layerTableModel) {
        this.addTablePanel(layerTableModel);
        ((AttributeTab)this.getParent()).setLayerViewPanelUpdates(false);
        this.selectRows(layerTableModel.getLayer());
        ((AttributeTab)this.getParent()).setLayerViewPanelUpdates(true);
    }

    @Override
    public void layerRemoved(LayerTableModel layerTableModel) {
        this.removeTablePanel(layerTableModel);
    }

    void jbInit() throws Exception {
        this.setLayout(new GridBagLayout());
    }

    private void removeTablePanel(LayerTableModel layerTableModel) {
        Layer layer = layerTableModel.getLayer();
        if (!this.layerToTablePanelMap.containsKey(layer)) {
            return;
        }
        AttributeTablePanel tablePanel = this.getTablePanel(layer);
        this.remove(tablePanel);
        if (tablePanel != null && layer.getLayerManager() != null) {
            layer.getLayerManager().removeLayerListeners(tablePanel.getLayerListeners());
        }
        this.layerToTablePanelMap.remove(layer);
        this.revalidate();
        this.repaint();
    }

    private void addTablePanel(final LayerTableModel layerTableModel) {
        Assert.isTrue((!this.layerToTablePanelMap.containsKey(layerTableModel.getLayer()) ? 1 : 0) != 0);
        final AttributeTablePanel tablePanel = new AttributeTablePanel(layerTableModel, this.addScrollPanesToChildren, this.workbenchContext, this);
        tablePanel.addListener(this);
        this.layerToTablePanelMap.put(layerTableModel.getLayer(), tablePanel);
        this.add((Component)tablePanel, new GridBagConstraints(0, this.getComponentCount(), 1, 1, 1.0, 1.0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
        this.revalidate();
        this.repaint();
        tablePanel.getTable().addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    int row = tablePanel.getTable().rowAtPoint(e.getPoint());
                    if (row == -1) {
                        return;
                    }
                    ArrayList<Feature> features = new ArrayList<Feature>();
                    Feature feat = layerTableModel.getFeature(row);
                    features.add(feat);
                    if (AttributePanel.this.taskFrame.isVisible() && !AttributePanel.this.taskFrame.isIcon()) {
                        AttributePanel.this.zoomToSelectedItemsPlugIn.flash(FeatureUtil.toGeometries(features), AttributePanel.this.taskFrame.getLayerViewPanel(), layerTableModel.getLayer().getCoordTrans());
                    }
                }
                catch (Throwable t) {
                    AttributePanel.this.workbenchContext.getErrorHandler().handleThrowable(t);
                }
            }
        });
        tablePanel.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && ((AttributeTab)AttributePanel.this.getParent()).isLayerViewPanelUpdates()) {
                    AttributePanel.this.selectInLayerViewPanel();
                    ((AttributeTab)AttributePanel.this.getParent()).getToolBar().updateEnabledState();
                }
            }
        });
    }

    private void selectRows(Layer layer) {
        AttributeTablePanel tablePanel = this.layerToTablePanelMap.get(layer);
        ListSelectionModel selectionModel = tablePanel.getTable().getSelectionModel();
        LayerTableModel model = (LayerTableModel)tablePanel.getTable().getModel();
        if (!(layer.isEditable() || model.isSort() || !(layer.getUltimateFeatureCollectionWrapper() instanceof FeatureCollectionOnDemand) || !(((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor() instanceof ShapeFileDataSource) || this.getRootPane() != null && this.getRootPane().getParent() instanceof InfoFrame)) {
            List<int[]> intervalos = layer.getUltimateFeatureCollectionWrapper().getIntervalSelection();
            for (int[] intervalo : intervalos) {
                selectionModel.addSelectionInterval(intervalo[0], intervalo[1]);
            }
        } else {
            Collection<Feature> features = this.workbenchContext.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(layer);
            for (Feature feature : features) {
                int j = -1;
                j = feature.getPrimaryKey() != null ? model.getRow(feature.getPrimaryKey()) : model.getRow(feature);
                selectionModel.addSelectionInterval(j, j);
            }
        }
        tablePanel.updateLabel();
    }

    public void inverseSelection() {
        for (AttributeTablePanel tablePanel : this.layerToTablePanelMap.values()) {
            Layer layer = tablePanel.getModel().getLayer();
            this.inverseSelection(layer);
        }
    }

    private void inverseSelection(Layer layer) {
        AttributeTablePanel tablePanel = this.layerToTablePanelMap.get(layer);
        ListSelectionModel selectionModel = tablePanel.getTable().getSelectionModel();
        selectionModel.setValueIsAdjusting(true);
        LayerTableModel model = (LayerTableModel)tablePanel.getTable().getModel();
        if (!(layer.isEditable() || model.isSort() || !(layer.getUltimateFeatureCollectionWrapper() instanceof FeatureCollectionOnDemand) || !(((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor() instanceof ShapeFileDataSource) || this.getRootPane() != null && this.getRootPane().getParent() instanceof InfoFrame)) {
            selectionModel.removeSelectionInterval(0, tablePanel.getTable().getRowCount());
            layer.getUltimateFeatureCollectionWrapper().invertSelection();
            List<int[]> intervalos = layer.getUltimateFeatureCollectionWrapper().getIntervalSelection();
            for (int[] intervalo : intervalos) {
                selectionModel.addSelectionInterval(intervalo[0], intervalo[1]);
            }
        } else {
            int[] rowsSelected = tablePanel.getTable().getSelectedRows();
            selectionModel.removeSelectionInterval(0, tablePanel.getTable().getRowCount());
            int pos = 0;
            int j = 0;
            while (j < rowsSelected.length) {
                if (pos != rowsSelected[j]) {
                    selectionModel.addSelectionInterval(pos, rowsSelected[j] - 1);
                }
                pos = rowsSelected[j] + 1;
                ++j;
            }
            if (pos <= tablePanel.getTable().getRowCount() - 1) {
                selectionModel.addSelectionInterval(pos, tablePanel.getTable().getRowCount() - 1);
            }
        }
        this.selectInLayerViewPanel();
        tablePanel.updateLabel();
    }

    public void selectFeatures(Collection<Feature> col, Layer layer) {
        AttributeTablePanel tablePanel = this.layerToTablePanelMap.get(layer);
        try {
            ListSelectionModel selectionModel = tablePanel.getTable().getSelectionModel();
            selectionModel.removeSelectionInterval(0, tablePanel.getTable().getRowCount());
            if (col == null || col.size() == 0) {
                return;
            }
            try {
                int posIni;
                LayerTableModel model = (LayerTableModel)tablePanel.getTable().getModel();
                ArrayList<Integer> rows = new ArrayList<Integer>();
                for (Feature element : col) {
                    if (element.isUnsaved()) {
                        rows.add(new Integer(model.getRow(element)));
                        continue;
                    }
                    rows.add(new Integer(model.getRow(element.getPrimaryKey())));
                }
                Collections.sort(rows, new Comparator<Integer>(){

                    @Override
                    public int compare(Integer i1, Integer i2) {
                        return i1.compareTo(i2);
                    }
                });
                int posFin = posIni = ((Integer)rows.get(0)).intValue();
                int j = 1;
                while (j < rows.size()) {
                    int pos = (Integer)rows.get(j);
                    if (pos == posFin + 1) {
                        posFin = pos;
                    } else {
                        selectionModel.addSelectionInterval(posIni, posFin);
                        posIni = pos;
                        posFin = pos;
                    }
                    ++j;
                }
                if (posIni != -1 && posFin != -1) {
                    selectionModel.addSelectionInterval(posIni, posFin);
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        finally {
            tablePanel.updateLabel();
        }
    }

    public int rowCount() {
        int rowCount = 0;
        for (AttributeTablePanel tablePanel : this.layerToTablePanelMap.values()) {
            rowCount += tablePanel.getTable().getRowCount();
        }
        return rowCount;
    }

    public void flashSelectedFeatures() throws NoninvertibleTransformException {
        if (this.taskFrame.isVisible() && !this.taskFrame.isIcon()) {
            ZoomToSelectedItemsPlugIn.flash(this.selectedGeometries(), this.taskFrame.getLayerViewPanel());
        }
    }

    public void zoom(Collection<Geometry> geometries) throws NoninvertibleTransformException {
        this.zoomToSelectedItemsPlugIn.zoom(geometries, this.taskFrame.getLayerViewPanel());
    }

    public boolean hasSelectedFeatures() {
        for (AttributeTablePanel tablePanel : this.layerToTablePanelMap.values()) {
            int[] selectedRows = tablePanel.getTable().getSelectedRows();
            if (selectedRows.length <= 0) continue;
            return true;
        }
        return false;
    }

    public boolean layerWithSelectedFeaturesAreEditable() {
        for (Layer layer : this.layerToTablePanelMap.keySet()) {
            AttributeTablePanel tablePanel = this.layerToTablePanelMap.get(layer);
            int[] selectedRows = tablePanel.getTable().getSelectedRows();
            if (selectedRows.length <= 0 || layer.isEditable()) continue;
            return false;
        }
        return true;
    }

    public Collection<Geometry> selectedGeometries() {
        ArrayList<Geometry> selectedGeometries = new ArrayList<Geometry>();
        for (AttributeTablePanel tablePanel : this.layerToTablePanelMap.values()) {
            Feature currentFeat;
            int j;
            int[] selectedRows = tablePanel.getTable().getSelectedRows();
            ICoordTrans coordTrans = tablePanel.getModel().getLayer().getCoordTrans();
            LayerTableModel model = tablePanel.getModel();
            if (coordTrans != null) {
                j = 0;
                while (j < selectedRows.length) {
                    currentFeat = model.getFeature(selectedRows[j]);
                    IShapeGeometry pathGeom = ShapeGeometryConverter.jts_to_igeometry(currentFeat.getGeometry());
                    pathGeom.reProject(coordTrans);
                    selectedGeometries.add(ShapeGeometryConverter.java2d_to_jts(pathGeom.getShp()));
                    ++j;
                }
                continue;
            }
            j = 0;
            while (j < selectedRows.length) {
                currentFeat = model.getFeature(selectedRows[j]);
                selectedGeometries.add(currentFeat.getGeometry());
                ++j;
            }
        }
        return selectedGeometries;
    }

    public void selectInLayerViewPanel() {
        this.taskFrame.getLayerViewPanel().getSelectionManager().clear(true);
        for (AttributeTablePanel tablePanel : this.layerToTablePanelMap.values()) {
            Layer currentLayer;
            Collection<Relation<?>> relations;
            tablePanel.updateLabel();
            Collection<Feature> selectedFeatures = tablePanel.getSelectedFeatures();
            if (CollectionUtils.isNotEmpty(selectedFeatures)) {
                this.taskFrame.getLayerViewPanel().getSelectionManager().getFeatureSelection().selectItems(tablePanel.getModel().getLayer(), selectedFeatures, true);
            }
            if (!CollectionUtils.isNotEmpty(relations = (currentLayer = tablePanel.getModel().getLayer()).getAllRelations())) continue;
            this.updateRelationSelection(currentLayer, relations, selectedFeatures, false);
        }
    }

    public void updateRelationSelection(Layer currentLayer, Collection<Relation<?>> relations, Collection<Feature> selectedFeatures, boolean fireLayerSelection) {
        for (Relation<?> currentRelation : relations) {
            Object linkValue;
            String linkAttrName;
            if (!currentRelation.getRelationType().equals((Object)RelationType.RELATE)) continue;
            if (currentRelation instanceof LayerRelation) {
                LayerRelation layerRelation = (LayerRelation)currentRelation;
                linkAttrName = layerRelation.getSourceAttribute();
                HashSet<Feature> relationSelectedFeats = new HashSet<Feature>();
                for (Feature selectedFeat : selectedFeatures) {
                    linkValue = selectedFeat.getAttribute(linkAttrName);
                    try {
                        relationSelectedFeats.addAll(layerRelation.getRelationRecords(linkValue));
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                }
                if (!CollectionUtils.isNotEmpty(relationSelectedFeats)) continue;
                this.taskFrame.getLayerViewPanel().getSelectionManager().getFeatureSelection().selectItems(layerRelation.getTargetLayer(), relationSelectedFeats, fireLayerSelection);
                continue;
            }
            if (!(currentRelation instanceof TableRelation)) continue;
            TableRelation tableRelation = (TableRelation)currentRelation;
            linkAttrName = tableRelation.getSourceAttribute();
            HashSet<Record> relationSelectedRecords = new HashSet<Record>();
            for (Feature selectedFeat : selectedFeatures) {
                linkValue = selectedFeat.getAttribute(linkAttrName);
                try {
                    relationSelectedRecords.addAll(tableRelation.getRelationRecords(linkValue));
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }
            Table targetTable = tableRelation.getTable();
            if (CollectionUtils.isNotEmpty(relationSelectedRecords)) {
                JUMPWorkbench.getFrameInstance().getContext().getDataManager().getRecordSelectionManager().selectRecords(targetTable, relationSelectedRecords);
                continue;
            }
            JUMPWorkbench.getFrameInstance().getContext().getDataManager().getRecordSelectionManager().clearSelection(targetTable, true);
        }
    }

    public Row topSelectedRow() {
        for (AttributeTablePanel panel : this.layerToTablePanelMap.values()) {
            int selectedRow = panel.getTable().getSelectedRow();
            if (selectedRow == -1) continue;
            return new BasicRow(panel, selectedRow);
        }
        return this.nullRow;
    }

    @Override
    public void selectionReplaced(AttributeTablePanel panel) {
        for (AttributeTablePanel tablePanel : this.layerToTablePanelMap.values()) {
            if (tablePanel == panel) continue;
            tablePanel.getTable().clearSelection();
        }
    }

    public void clearSelection() {
        for (AttributeTablePanel tablePanel : this.layerToTablePanelMap.values()) {
            tablePanel.getTable().clearSelection();
        }
    }

    public List<LayerListener> getLayerListeners() {
        ArrayList<LayerListener> layerListeners = new ArrayList<LayerListener>();
        for (AttributeTablePanel tablePanel : this.layerToTablePanelMap.values()) {
            layerListeners.addAll(tablePanel.getLayerListeners());
        }
        return layerListeners;
    }

    private class BasicRow
    implements Row {
        private AttributeTablePanel panel = null;
        private int index;

        public BasicRow(AttributeTablePanel panel, int index) {
            this.panel = panel;
            this.index = index;
        }

        @Override
        public boolean isFirstRow() {
            return this.panel.getModel().getLayer() == AttributePanel.this.getModel().getLayers().get(0) && this.index == 0;
        }

        @Override
        public boolean isLastRow() {
            return this.panel.getModel().getLayer() == AttributePanel.this.getModel().getLayers().get(AttributePanel.this.getModel().getLayers().size() - 1) && this.index == this.panel.getTable().getRowCount() - 1;
        }

        @Override
        public AttributeTablePanel getPanel() {
            return this.panel;
        }

        @Override
        public int getIndex() {
            return this.index;
        }

        @Override
        public Row previousRow() {
            if (this.isFirstRow()) {
                return this;
            }
            if (this.index > 0) {
                return new BasicRow(this.panel, this.index - 1);
            }
            return new BasicRow(this.previousPanel(), this.previousPanel().getTable().getRowCount() - 1);
        }

        @Override
        public Row nextRow() {
            if (this.isLastRow()) {
                return this;
            }
            if (this.index < this.panel.getTable().getRowCount() - 1) {
                return new BasicRow(this.panel, this.index + 1);
            }
            return new BasicRow(this.nextPanel(), 0);
        }

        private AttributeTablePanel previousPanel() {
            return AttributePanel.this.getTablePanel(this.previousLayer());
        }

        private AttributeTablePanel nextPanel() {
            return AttributePanel.this.getTablePanel(this.nextLayer());
        }

        private Layer previousLayer() {
            return AttributePanel.this.getModel().getLayers().get(AttributePanel.this.getModel().getLayers().indexOf(this.panel.getModel().getLayer()) - 1);
        }

        private Layer nextLayer() {
            return AttributePanel.this.getModel().getLayers().get(AttributePanel.this.getModel().getLayers().indexOf(this.panel.getModel().getLayer()) + 1);
        }

        @Override
        public Feature getFeature() {
            return this.panel.getModel().getFeature(this.index);
        }
    }

    public static interface Row {
        public boolean isFirstRow();

        public boolean isLastRow();

        public AttributeTablePanel getPanel();

        public int getIndex();

        public Row nextRow();

        public Row previousRow();

        public Feature getFeature();
    }
}

