/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.utiles.swing.JComboBox
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.util;

import com.iver.utiles.swing.JComboBox;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class SelectGeometryTypeDialog
extends JDialog
implements ActionListener {
    private static final long serialVersionUID = 1L;
    public static final String NULL_SELECTION = "-------";
    private static final String TITLE = I18N.getString("org.saig.jump.widgets.util.SelectGeometryTypeDialog.select-geometry-type");
    private JPanel geometryTypePanel;
    private JLabel polygonalLabel;
    private JLabel linealLabel;
    private JLabel pointLabel;
    private JLabel multiPolygonalLabel;
    private JLabel multiLinealLabel;
    private JLabel multiPointLabel;
    private ButtonGroup group = new ButtonGroup();
    private JRadioButton polygonalRadioButton;
    private JRadioButton linealRadioButton;
    private JRadioButton pointRadioButton;
    private JRadioButton multiPolygonalRadioButton;
    private JRadioButton multiLinealRadioButton;
    private JRadioButton multiPointRadioButton;
    private JPanel schemaSelectionPanel;
    private JComboBox layersList;
    private JCheckBox is3dCheckBox;
    private OKCancelPanel okCancelPanel;
    private Map<String, FeatureSchema> layerNameToSchemaMap;
    private boolean copyAttributes;

    public SelectGeometryTypeDialog(JFrame parent, boolean modal) {
        this(parent, modal, false, null);
    }

    public SelectGeometryTypeDialog(JFrame parent, boolean modal, boolean allowCopyAttributes, String layerName) {
        super((Frame)parent, modal);
        String title = TITLE;
        if (StringUtils.isNotEmpty((String)layerName)) {
            title = String.valueOf(title) + " - " + layerName;
        }
        this.setTitle(title);
        this.copyAttributes = allowCopyAttributes;
        this.initialize();
        this.pack();
    }

    private void initialize() {
        this.setMinimumSize(new Dimension(350, 180));
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.setContentPane(mainPanel);
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getGeometryTypePanel());
        if (this.copyAttributes) {
            FormUtils.addRowInGBL(mainPanel, 2, 0, this.getSchemaSelectionPanel());
        }
        FormUtils.addRowInGBL(mainPanel, 3, 0, this.getOkCancelPanel());
        FormUtils.addFiller(mainPanel, 4, 0);
    }

    private OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(this);
        }
        return this.okCancelPanel;
    }

    private JPanel getSchemaSelectionPanel() {
        this.schemaSelectionPanel = new JPanel();
        this.schemaSelectionPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.util.SelectGeometryTypeDialog.copy-schema")));
        this.layersList = new JComboBox(new DefaultComboBoxModel());
        this.layersList.setPreferredSize(new Dimension(200, (int)this.layersList.getPreferredSize().getHeight()));
        this.layersList.setMaximumSize(new Dimension(200, (int)this.layersList.getPreferredSize().getHeight()));
        this.recalculateLayers();
        this.schemaSelectionPanel.add((Component)this.layersList);
        return this.schemaSelectionPanel;
    }

    private Comparator<String> getComparator() {
        return new Comparator<String>(){

            @Override
            public int compare(String arg0, String arg1) {
                return arg0.compareToIgnoreCase(arg1);
            }
        };
    }

    public void recalculateLayers() {
        String selectedLayerTmp = null;
        if (this.layersList == null) {
            return;
        }
        selectedLayerTmp = (String)this.layersList.getSelectedItem();
        ((DefaultComboBoxModel)this.layersList.getModel()).removeAllElements();
        this.layersList.addItem((Object)NULL_SELECTION);
        this.layerNameToSchemaMap = new HashMap<String, FeatureSchema>();
        List allLayers = null;
        try {
            allLayers = JUMPWorkbench.getFrameInstance().getContext().getAllLayers();
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (CollectionUtils.isEmpty((Collection)allLayers)) {
            this.layersList.setEnabled(false);
        } else {
            this.layersList.setEnabled(true);
            ArrayList<String> namesList = new ArrayList<String>();
            for (Layer aLayer : allLayers) {
                if (aLayer.isRaster()) continue;
                String aLayerName = aLayer.getName();
                this.layerNameToSchemaMap.put(aLayerName, aLayer.getFeatureSchema());
                namesList.add(aLayerName);
            }
            Collections.sort(namesList, this.getComparator());
            int i = 0;
            while (i < namesList.size()) {
                this.layersList.addItem(namesList.get(i));
                ++i;
            }
            if (selectedLayerTmp != null) {
                this.layersList.setSelectedItem((Object)selectedLayerTmp);
            }
        }
    }

    public boolean isListEnabled() {
        return this.layersList.isEnabled();
    }

    public String getSelectedLayerName() {
        return (String)this.layersList.getSelectedItem();
    }

    public boolean is3D() {
        return this.is3dCheckBox.isSelected();
    }

    public FeatureSchema getSelectedSchema() {
        return this.layerNameToSchemaMap.get(this.layersList.getSelectedItem());
    }

    private JPanel getGeometryTypePanel() {
        if (this.geometryTypePanel == null) {
            this.geometryTypePanel = new JPanel();
            this.geometryTypePanel.setLayout(new GridBagLayout());
            this.geometryTypePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.util.SelectGeometryTypeDialog.geometry-type")));
            this.polygonalRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.util.SelectGeometryTypeDialog.polygon"));
            this.polygonalLabel = new JLabel(IconLoader.icon("polygon.gif"));
            this.polygonalRadioButton.setSelected(true);
            this.group.add(this.polygonalRadioButton);
            this.linealRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.util.SelectGeometryTypeDialog.line"));
            this.linealLabel = new JLabel(IconLoader.icon("lineal.gif"));
            this.group.add(this.linealRadioButton);
            this.pointRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.util.SelectGeometryTypeDialog.point"));
            this.pointLabel = new JLabel(IconLoader.icon("point.gif"));
            this.group.add(this.pointRadioButton);
            this.multiPolygonalRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.util.SelectGeometryTypeDialog.MultiPolygon"));
            this.multiPolygonalLabel = new JLabel(IconLoader.icon("multipolygon.gif"));
            this.group.add(this.multiPolygonalRadioButton);
            this.multiLinealRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.util.SelectGeometryTypeDialog.MultiLinestring"));
            this.multiLinealLabel = new JLabel(IconLoader.icon("multiline.gif"));
            this.group.add(this.multiLinealRadioButton);
            this.multiPointRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.util.SelectGeometryTypeDialog.MultiPoint"));
            this.multiPointLabel = new JLabel(IconLoader.icon("multipoint.gif"));
            this.group.add(this.multiPointRadioButton);
            this.is3dCheckBox = new JCheckBox(I18N.getString("org.saig.jump.widgets.util.SelectGeometryTypeDialog.Use-Z"));
            FormUtils.addRowInGBL((JComponent)this.geometryTypePanel, 0, 0, (JComponent)FormUtils.getTitleLabel(I18N.getString("org.saig.jump.widgets.util.SelectGeometryTypeDialog.Simples")), true, false);
            FormUtils.addRowInGBL((JComponent)this.geometryTypePanel, 1, 0, (JComponent)this.polygonalRadioButton, false, false);
            FormUtils.addRowInGBL((JComponent)this.geometryTypePanel, 1, 1, (JComponent)this.polygonalLabel, true, false);
            FormUtils.addRowInGBL((JComponent)this.geometryTypePanel, 2, 0, (JComponent)this.linealRadioButton, false, false);
            FormUtils.addRowInGBL((JComponent)this.geometryTypePanel, 2, 1, (JComponent)this.linealLabel, true, false);
            FormUtils.addRowInGBL((JComponent)this.geometryTypePanel, 3, 0, (JComponent)this.pointRadioButton, false, false);
            FormUtils.addRowInGBL((JComponent)this.geometryTypePanel, 3, 1, (JComponent)this.pointLabel, true, false);
            FormUtils.addRowInGBL((JComponent)this.geometryTypePanel, 4, 0, (JComponent)FormUtils.getTitleLabel(I18N.getString("org.saig.jump.widgets.util.SelectGeometryTypeDialog.MultiGeometries")), true, false);
            FormUtils.addRowInGBL((JComponent)this.geometryTypePanel, 5, 0, (JComponent)this.multiPolygonalRadioButton, false, false);
            FormUtils.addRowInGBL((JComponent)this.geometryTypePanel, 5, 1, (JComponent)this.multiPolygonalLabel, true, false);
            FormUtils.addRowInGBL((JComponent)this.geometryTypePanel, 6, 0, (JComponent)this.multiLinealRadioButton, false, false);
            FormUtils.addRowInGBL((JComponent)this.geometryTypePanel, 6, 1, (JComponent)this.multiLinealLabel, true, false);
            FormUtils.addRowInGBL((JComponent)this.geometryTypePanel, 7, 0, (JComponent)this.multiPointRadioButton, false, false);
            FormUtils.addRowInGBL((JComponent)this.geometryTypePanel, 7, 1, (JComponent)this.multiPointLabel, true, false);
            FormUtils.addRowInGBL((JComponent)this.geometryTypePanel, 8, 0, (JComponent)this.is3dCheckBox, true, false);
        }
        return this.geometryTypePanel;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.okCancelPanel.setOKPressed(false);
        }
        super.setVisible(visible);
    }

    public boolean wasOKPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    public int getGeometryType() {
        if (this.polygonalRadioButton.isSelected()) {
            return 5;
        }
        if (this.linealRadioButton.isSelected()) {
            return 3;
        }
        if (this.pointRadioButton.isSelected()) {
            return 1;
        }
        if (this.multiPolygonalRadioButton.isSelected()) {
            return 4;
        }
        if (this.multiLinealRadioButton.isSelected()) {
            return 2;
        }
        if (this.multiPointRadioButton.isSelected()) {
            return 8;
        }
        return 0;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.okCancelPanel) {
            this.setVisible(false);
            return;
        }
    }
}

