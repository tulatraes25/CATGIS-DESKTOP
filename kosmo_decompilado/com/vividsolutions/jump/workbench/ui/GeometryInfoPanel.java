/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.FeatureInfoWriter;
import com.vividsolutions.jump.workbench.ui.InfoModel;
import com.vividsolutions.jump.workbench.ui.InfoModelListener;
import com.vividsolutions.jump.workbench.ui.LayerTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class GeometryInfoPanel
extends JPanel
implements InfoModelListener {
    private BorderLayout borderLayout1 = new BorderLayout();
    private JEditorPane editorPane = new JEditorPane();
    private FeatureInfoWriter writer = new FeatureInfoWriter();
    private InfoModel model;
    private JScrollPane scrollPane = new JScrollPane();
    private FeatureInfoWriter.FeatureWriter geometryWriter;
    private FeatureInfoWriter.FeatureWriter attributeWriter;

    public GeometryInfoPanel(InfoModel model) {
        this.setModel(model);
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        this.editorPane.setEditable(false);
        this.editorPane.setText("jEditorPane1");
        this.editorPane.setContentType("text/html");
        this.setLayout(this.borderLayout1);
        this.add((Component)this.scrollPane, "Center");
        this.scrollPane.getViewport().add((Component)this.editorPane, null);
    }

    public void setModel(InfoModel model) {
        this.model = model;
        model.addListener(this);
    }

    @Override
    public void layerAdded(LayerTableModel layerTableModel) {
        this.updateText();
        layerTableModel.addTableModelListener(new TableModelListener(){

            @Override
            public void tableChanged(TableModelEvent e) {
                GeometryInfoPanel.this.updateText();
            }
        });
    }

    @Override
    public void layerRemoved(LayerTableModel layerTableModel) {
        this.updateText();
    }

    public void updateText() {
        this.editorPane.setText(this.writer.writeGeom(this.layerToFeaturesMap(), this.geometryWriter, this.attributeWriter));
        this.editorPane.setCaretPosition(0);
    }

    private Map layerToFeaturesMap() {
        HashMap<Layer, List<Feature>> layerToFeaturesMap = new HashMap<Layer, List<Feature>>();
        for (Layer layer : this.model.getLayers()) {
            layerToFeaturesMap.put(layer, this.model.getTableModel(layer).getFeatures());
        }
        return layerToFeaturesMap;
    }

    public void setGeometryWriter(FeatureInfoWriter.FeatureWriter geometryWriter) {
        this.geometryWriter = geometryWriter;
    }

    public void setAttributeWriter(FeatureInfoWriter.FeatureWriter attributeWriter) {
        this.attributeWriter = attributeWriter;
    }
}

