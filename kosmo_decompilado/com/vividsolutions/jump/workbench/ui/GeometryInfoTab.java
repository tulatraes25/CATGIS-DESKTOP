/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.EnableableToolBar;
import com.vividsolutions.jump.workbench.ui.FeatureInfoWriter;
import com.vividsolutions.jump.workbench.ui.GeometryInfoPanel;
import com.vividsolutions.jump.workbench.ui.InfoModel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.saig.jump.lang.I18N;

public class GeometryInfoTab
extends JPanel {
    private BorderLayout borderLayout2 = new BorderLayout();
    private JToggleButton gmlButton = new JToggleButton();
    private JToggleButton wktButton = new JToggleButton();
    private JToggleButton coordinatesButton = new JToggleButton();
    private JToggleButton showAttributesButton = new JToggleButton();
    private JToggleButton showGeometriesButton = new JToggleButton();
    private EnableableToolBar toolBar = new EnableableToolBar();
    private GeometryInfoPanel geometryInfoPanel;
    private EnableCheck geometriesShownEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            return !GeometryInfoTab.this.showGeometriesButton.isSelected() ? "X" : null;
        }
    };

    public GeometryInfoTab(InfoModel model) {
        this.geometryInfoPanel = new GeometryInfoPanel(model);
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        this.toolBar.add(this.showGeometriesButton, I18N.getString("workbench.ui.GeometryInfoTab.geometries"), IconLoader.icon("Geometry.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                GeometryInfoTab.this.updateText();
            }
        }, new MultiEnableCheck());
        this.toolBar.addSpacer();
        this.toolBar.add(this.showAttributesButton, I18N.getString("workbench.ui.GeometryInfoTab.attributes"), IconLoader.icon("Attribute.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                GeometryInfoTab.this.updateText();
            }
        }, new MultiEnableCheck());
        this.toolBar.addSpacer();
        this.toolBar.add(this.wktButton, I18N.getString("workbench.ui.GeometryInfoTab.wkt"), IconLoader.icon("WKT.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                GeometryInfoTab.this.updateText();
            }
        }, this.geometriesShownEnableCheck);
        this.toolBar.add(this.gmlButton, I18N.getString("workbench.ui.GeometryInfoTab.gml"), IconLoader.icon("GML.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                GeometryInfoTab.this.updateText();
            }
        }, this.geometriesShownEnableCheck);
        this.toolBar.add(this.coordinatesButton, I18N.getString("workbench.ui.GeometryInfoTab.coordinate-list"), IconLoader.icon("CL.gif"), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                GeometryInfoTab.this.updateText();
            }
        }, this.geometriesShownEnableCheck);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(this.wktButton);
        buttonGroup.add(this.gmlButton);
        buttonGroup.add(this.coordinatesButton);
        this.showGeometriesButton.doClick();
        this.wktButton.doClick();
    }

    private void updateText() {
        if (this.showAttributesButton.isSelected()) {
            this.geometryInfoPanel.setAttributeWriter(FeatureInfoWriter.ATTRIBUTE_WRITER);
        } else {
            this.geometryInfoPanel.setAttributeWriter(FeatureInfoWriter.EMPTY_WRITER);
        }
        if (this.showGeometriesButton.isSelected()) {
            if (this.wktButton.isSelected()) {
                this.geometryInfoPanel.setGeometryWriter(FeatureInfoWriter.WKT_WRITER);
            }
            if (this.gmlButton.isSelected()) {
                this.geometryInfoPanel.setGeometryWriter(FeatureInfoWriter.GML_WRITER);
            }
            if (this.coordinatesButton.isSelected()) {
                this.geometryInfoPanel.setGeometryWriter(FeatureInfoWriter.COORDINATE_WRITER);
            }
        } else {
            this.geometryInfoPanel.setGeometryWriter(FeatureInfoWriter.EMPTY_WRITER);
        }
        this.geometryInfoPanel.updateText();
    }

    void jbInit() throws Exception {
        this.setLayout(this.borderLayout2);
        this.toolBar.setOrientation(1);
        this.add((Component)this.geometryInfoPanel, "Center");
        this.add((Component)this.toolBar, "West");
    }
}

