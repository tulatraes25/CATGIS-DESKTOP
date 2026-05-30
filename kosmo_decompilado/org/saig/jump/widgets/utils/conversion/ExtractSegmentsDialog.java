/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.utils.conversion;

import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.utils.conversion.ExtractSegmentsPlugIn;
import org.saig.jump.widgets.util.JAvailableLayersComboBox;
import org.saig.jump.widgets.util.JQueryChooserPanel;

public class ExtractSegmentsDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final String OPCION_TODOS = I18N.getString(ExtractSegmentsDialog.class, "all");
    private static final String OPCION_TODOS_UNA_VEZ = I18N.getString(ExtractSegmentsDialog.class, "all-(-one-time-)");
    private static final String OPCION_NO_REPETIDOS = I18N.getString(ExtractSegmentsDialog.class, "not-repeated");
    private JPanel descriptionPanel;
    private JPanel optionsPanel;
    private JAvailableLayersComboBox selectableLayersComboBox;
    private JCheckBox copyAttributesCheckbox;
    private JLabel optionLabel;
    private JComboBox optionRepeatedComboBox;
    private JCheckBox mergeResultsCheckBox;
    private JQueryChooserPanel segmentsLayerChooserPanel;
    private OKCancelPanel okCancelPanel;
    private LayerManager layerManager;

    public ExtractSegmentsDialog(JFrame owner, boolean modal, LayerManager layerManager) {
        super((Frame)owner, modal);
        this.setTitle(ExtractSegmentsPlugIn.NAME);
        this.layerManager = layerManager;
        this.initialize();
        this.pack();
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);
        mainPanel.add((Component)this.getOptionsPanel(), "Center");
        mainPanel.add((Component)this.getDescriptionPanel(), "West");
        mainPanel.add((Component)this.getOkCancelPanel(), "South");
    }

    private JPanel getOptionsPanel() {
        if (this.optionsPanel == null) {
            this.optionsPanel = new JPanel(new GridBagLayout());
            this.optionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "options")));
            this.selectableLayersComboBox = new JAvailableLayersComboBox(this.layerManager, false, false, true);
            this.copyAttributesCheckbox = new JCheckBox(I18N.getString(ExtractSegmentsDialog.class, "copy-attributes"));
            this.copyAttributesCheckbox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (ExtractSegmentsDialog.this.copyAttributesCheckbox.isSelected()) {
                        ExtractSegmentsDialog.this.optionRepeatedComboBox.setSelectedItem(OPCION_TODOS);
                        ExtractSegmentsDialog.this.mergeResultsCheckBox.setSelected(false);
                    }
                    ExtractSegmentsDialog.this.optionLabel.setEnabled(!ExtractSegmentsDialog.this.copyAttributesCheckbox.isSelected());
                    ExtractSegmentsDialog.this.optionRepeatedComboBox.setEnabled(!ExtractSegmentsDialog.this.copyAttributesCheckbox.isSelected());
                    ExtractSegmentsDialog.this.mergeResultsCheckBox.setEnabled(!ExtractSegmentsDialog.this.copyAttributesCheckbox.isSelected());
                }
            });
            this.optionLabel = new JLabel(String.valueOf(I18N.getString(ExtractSegmentsDialog.class, "segments")) + ": ");
            this.optionRepeatedComboBox = new JComboBox();
            this.optionRepeatedComboBox.addItem(OPCION_TODOS);
            this.optionRepeatedComboBox.addItem(OPCION_TODOS_UNA_VEZ);
            this.optionRepeatedComboBox.addItem(OPCION_NO_REPETIDOS);
            this.optionRepeatedComboBox.setSelectedItem(OPCION_TODOS);
            this.mergeResultsCheckBox = new JCheckBox(I18N.getString(this.getClass(), "join-result-segments"));
            this.segmentsLayerChooserPanel = new JQueryChooserPanel(I18N.getString(this.getClass(), "segments-layer"), I18N.getString(this.getClass(), "select-target-for-segments-layer"), false);
            FormUtils.addRowInGBL(this.optionsPanel, 0, 0, this.selectableLayersComboBox);
            FormUtils.addRowInGBL(this.optionsPanel, 1, 0, this.copyAttributesCheckbox);
            FormUtils.addRowInGBL((JComponent)this.optionsPanel, 2, 0, this.optionLabel, (JComponent)this.optionRepeatedComboBox);
            FormUtils.addRowInGBL(this.optionsPanel, 3, 0, this.mergeResultsCheckBox);
            FormUtils.addRowInGBL(this.optionsPanel, 4, 0, this.segmentsLayerChooserPanel);
            FormUtils.addFiller(this.optionsPanel, 5, 0);
        }
        return this.optionsPanel;
    }

    private JPanel getDescriptionPanel() {
        if (this.descriptionPanel == null) {
            this.descriptionPanel = new JPanel(new GridBagLayout());
            JLabel imageLabel = new JLabel(IconLoader.icon("toolImages/ExtractSegments.png"));
            JLabel descriptionLabel = new JLabel("<HTML><P align=\"justify\">" + I18N.getString(this.getClass(), "extract-from-input-data-all-lineal-segments-that-form-part-of-them") + "</P></HTML>");
            descriptionLabel.setMinimumSize(new Dimension(80, 50));
            descriptionLabel.setPreferredSize(new Dimension(80, 50));
            FormUtils.addRowInGBL(this.descriptionPanel, 0, 0, imageLabel);
            FormUtils.addRowInGBL(this.descriptionPanel, 1, 0, descriptionLabel);
        }
        return this.descriptionPanel;
    }

    private OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ExtractSegmentsDialog.this.setVisible(false);
                }
            });
        }
        return this.okCancelPanel;
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    public Layer getSelectedLayer() {
        return this.selectableLayersComboBox.getSelectedLayer();
    }

    public boolean isMergingResults() {
        return this.mergeResultsCheckBox.isSelected();
    }

    public boolean isAllSegmentsOptionSelected() {
        return this.optionRepeatedComboBox.getSelectedItem() == OPCION_TODOS;
    }

    public boolean isAllSegmentsOneTimeOptionSelected() {
        return this.optionRepeatedComboBox.getSelectedItem() == OPCION_TODOS_UNA_VEZ;
    }

    public boolean isNoRepeatedSegmentsOptionSelected() {
        return this.optionRepeatedComboBox.getSelectedItem() == OPCION_NO_REPETIDOS;
    }

    public boolean isCopyAttributesSelected() {
        return this.copyAttributesCheckbox.isSelected();
    }

    public DataSourceQuery getResultQuery() {
        return this.segmentsLayerChooserPanel.getDataSourceQuery();
    }

    public void refresh() {
        this.selectableLayersComboBox.refresh();
    }
}

