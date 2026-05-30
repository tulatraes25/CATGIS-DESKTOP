/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.Point
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.info;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.EnableableToolBar;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.LayerUtil;

public class FeatureInfoDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(FeatureInfoDialog.class);
    protected Map<Layer, Collection<Feature>> mapLayers;
    protected JList layerList;
    protected JTable table;
    protected Object[] layers;
    protected FeatureTableModel tableModel;
    protected LayerViewPanel layerViewPanel;
    protected EnableableToolBar toolBar;
    protected EnableCheck layersEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            return FeatureInfoDialog.this.layers == null ? I18N.getString("workbench.ui.AttributeTab.one-or-more-layers-must-be-present") : null;
        }
    };

    public FeatureInfoDialog(JFrame parent, boolean modal, Map<Layer, Collection<Feature>> mapLayers, Object[] layers, LayerViewPanel layerViewPanel) {
        super((Frame)parent, modal);
        this.mapLayers = mapLayers;
        this.layers = layers;
        this.layerViewPanel = layerViewPanel;
        JPanel centerPanel = new JPanel(new FlowLayout());
        try {
            centerPanel.add(this.getLayersPane());
        }
        catch (Exception e) {
            return;
        }
        centerPanel.add(this.getTablePane());
        try {
            this.refresh();
        }
        catch (Exception e) {
            return;
        }
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add((Component)centerPanel, "Center");
        this.installToolBarButtons();
        this.toolBar.setOrientation(1);
        mainPanel.add((Component)this.toolBar, "West");
        mainPanel.add((Component)this.getButtonPanel(), "South");
        this.setContentPane(mainPanel);
        this.pack();
        this.setResizable(false);
        GUIUtil.centreOnScreen(this);
        if (this.layerList.getModel().getSize() > 0) {
            this.layerList.setSelectedIndex(0);
        }
        this.setVisible(true);
    }

    protected void installToolBarButtons() {
        this.toolBar = new EnableableToolBar();
        this.toolBar.add(new JButton(), I18N.getString("workbench.ui.AttributeTab.zoom-to-selected-rows"), IconLoader.icon("SmallMagnify.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Geometry geom = ((FeatureInfo)FeatureInfoDialog.this.layerList.getSelectedValue()).getFeature().getGeometry();
                    Envelope envelope = geom.getEnvelopeInternal();
                    if (geom instanceof Point) {
                        Point p = (Point)geom;
                        envelope = new Envelope(p.getX() - 5.0, p.getX() + 5.0, p.getY() - 5.0, p.getY() + 5.0);
                    }
                    FeatureInfoDialog.this.layerViewPanel.getViewport().zoom(envelope);
                }
                catch (NoninvertibleTransformException ex) {
                    LOGGER.error((Object)"", (Throwable)ex);
                }
            }
        }, new MultiEnableCheck().add(this.layersEnableCheck));
        this.toolBar.add(new JButton(), I18N.getString("workbench.ui.AttributeTab.zoom-to-full-extent"), IconLoader.icon("SmallWorld.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    FeatureInfoDialog.this.layerViewPanel.getViewport().zoomToFullExtent();
                }
                catch (NoninvertibleTransformException ex) {
                    LOGGER.error((Object)"", (Throwable)ex);
                }
            }
        }, new MultiEnableCheck().add(this.layersEnableCheck));
        this.toolBar.add(new JButton(), I18N.getString("workbench.ui.AttributeTab.flash-selected-rows"), IconLoader.icon("Flashlight.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                FeatureInfo featureInfo = (FeatureInfo)FeatureInfoDialog.this.layerList.getSelectedValue();
                Feature feature = featureInfo.getFeature();
                try {
                    FeatureInfoDialog.this.layerViewPanel.flash(FeatureInfoDialog.this.layerViewPanel.getViewport().getJava2DConverter().toShape(feature.getGeometry()), Color.red, new BasicStroke(5.0f, 1, 1), 100);
                }
                catch (NoninvertibleTransformException ex) {
                    LOGGER.error((Object)"", (Throwable)ex);
                }
            }
        }, new MultiEnableCheck().add(this.layersEnableCheck));
    }

    protected JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton(I18N.getString("org.saig.jump.widgets.info.FeatureInfoDialog.close"));
        closeButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                FeatureInfoDialog.this.setVisible(false);
                FeatureInfoDialog.this.dispose();
            }
        });
        buttonPanel.add(closeButton);
        return buttonPanel;
    }

    protected JScrollPane getTablePane() {
        JScrollPane pane = new JScrollPane();
        this.tableModel = new FeatureTableModel();
        this.table = new JTable(this.tableModel);
        pane.setViewportView(this.table);
        pane.setMinimumSize(new Dimension(450, 300));
        pane.setPreferredSize(new Dimension(450, 300));
        TableRowSorter<FeatureTableModel> sorter = new TableRowSorter<FeatureTableModel>((FeatureTableModel)this.table.getModel());
        RowFilter<FeatureTableModel, Integer> visibleFilter = new RowFilter<FeatureTableModel, Integer>(){

            @Override
            public boolean include(RowFilter.Entry<? extends FeatureTableModel, ? extends Integer> entry) {
                FeatureTableModel featModel = entry.getModel();
                return featModel.isVisible(entry.getIdentifier());
            }
        };
        sorter.setRowFilter(visibleFilter);
        this.table.setRowSorter(sorter);
        return pane;
    }

    protected JScrollPane getLayersPane() throws Exception {
        JScrollPane pane = new JScrollPane();
        this.layerList = new JList();
        this.layerList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                FeatureInfo featureInfo = (FeatureInfo)FeatureInfoDialog.this.layerList.getSelectedValue();
                if (featureInfo == null) {
                    return;
                }
                Feature feature = featureInfo.getFeature();
                try {
                    FeatureInfoDialog.this.layerViewPanel.flash(FeatureInfoDialog.this.layerViewPanel.getViewport().getJava2DConverter().toShape(feature.getGeometry()), Color.red, new BasicStroke(5.0f, 1, 1), 100);
                }
                catch (NoninvertibleTransformException ex) {
                    LOGGER.error((Object)"", (Throwable)ex);
                }
                FeatureInfoDialog.this.tableModel.setFeature(feature);
                FeatureInfoDialog.this.tableModel.fireTableDataChanged();
            }
        });
        pane.setViewportView(this.layerList);
        pane.setMinimumSize(new Dimension(150, 300));
        pane.setPreferredSize(new Dimension(150, 300));
        return pane;
    }

    public void refresh(Map<Layer, Collection<Feature>> mapLayers, Object[] layers) throws Exception {
        this.mapLayers = mapLayers;
        this.layers = layers;
        this.refresh();
    }

    protected void refresh() throws Exception {
        this.layerList.setSelectionMode(0);
        int contador = 1;
        Vector<FeatureInfo> list = new Vector<FeatureInfo>();
        int i = 0;
        while (i < this.layers.length) {
            Collection<Feature> features;
            Layer layer;
            if (!(this.layers[i] instanceof WMSLayer || (layer = (Layer)this.layers[i]).isRaster() && !LayerUtil.isQueryable(layer) || (features = this.mapLayers.get(layer)) == null)) {
                for (Feature feat : features) {
                    FeatureInfo featInfo = new FeatureInfo(layer.getName(), feat, contador);
                    list.add(featInfo);
                    ++contador;
                }
            }
            ++i;
        }
        if (list.size() == 0) {
            throw new Exception();
        }
        this.setTitle(String.valueOf(I18N.getString("org.saig.jump.widgets.info.FeatureInfoDialog.info")) + " - " + list.size() + " " + I18N.getString("org.saig.jump.widgets.info.FeatureInfoDialog.selected"));
        this.layerList.setListData(list);
        if (this.layerList.getModel().getSize() > 0) {
            this.layerList.setSelectedIndex(0);
            FeatureInfo featureInfo = (FeatureInfo)this.layerList.getSelectedValue();
            if (featureInfo == null) {
                return;
            }
            Feature feature = featureInfo.getFeature();
            try {
                this.layerViewPanel.flash(this.layerViewPanel.getViewport().getJava2DConverter().toShape(feature.getGeometry()), Color.red, new BasicStroke(5.0f, 1, 1), 100);
            }
            catch (NoninvertibleTransformException ex) {
                LOGGER.error((Object)"", (Throwable)ex);
            }
            this.tableModel.setFeature(feature);
            this.tableModel.fireTableDataChanged();
        }
    }

    protected class FeatureInfo {
        String layerName;
        int id;
        Feature feature;

        public FeatureInfo(String layerName, Feature feature, int id) {
            this.layerName = layerName;
            this.feature = feature;
            this.id = id;
        }

        public Feature getFeature() {
            return this.feature;
        }

        public int getId() {
            return this.id;
        }

        public String getLayerName() {
            return this.layerName;
        }

        public String toString() {
            return String.valueOf(this.id) + " : " + this.layerName + " - " + this.feature.getSchema().getPrimaryKeyName() + " -> " + this.feature.getPrimaryKey();
        }
    }

    protected class FeatureTableModel
    extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private String[] columnNames = new String[]{I18N.getString("org.saig.jump.widgets.info.FeatureInfoDialog.field"), I18N.getString("org.saig.jump.widgets.info.FeatureInfoDialog.value")};
        private Feature feature;

        public boolean isVisible(int row) {
            if (this.feature == null || this.feature.getSchema() == null) {
                return false;
            }
            return this.feature.getSchema().getVisibility(row);
        }

        @Override
        public int getColumnCount() {
            return this.columnNames.length;
        }

        @Override
        public int getRowCount() {
            if (this.feature == null || this.feature.getSchema() == null) {
                return 0;
            }
            return this.feature.getSchema().getAttributeCount();
        }

        @Override
        public String getColumnName(int col) {
            return this.columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (this.feature == null || this.feature.getSchema() == null) {
                return null;
            }
            if (col == 0) {
                return this.feature.getSchema().getAttribute(row).getPublicName();
            }
            return this.feature.getAttribute(row);
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == 0) {
                Object value = this.getValueAt(0, c);
                return value != null ? value.getClass() : Object.class;
            }
            return Object.class;
        }

        public void setFeature(Feature feature) {
            this.feature = feature;
        }
    }
}

