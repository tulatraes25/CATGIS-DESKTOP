/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.documents;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.saig.core.gui.swing.dataComponents.tables.JTableTextAreaScrollPane;

public class DocumentDetailsPanel
extends JPanel {
    private JPanel northPanel;
    private JButton saveChangesButton;
    private JLabel nombreLabel;
    private JLabel observacionesLabel;
    private JLabel rutaLabel;
    private JTextField nombreTextField;
    private JLabel sizeLabel;
    private JLabel fechaLabel;
    private JTableTextAreaScrollPane observacionesScrollPane;
    private JTextField sizeTextField;
    private JTextField fechaAltaTextField;
    private JCheckBox saveToDBCheckBox;
    private JButton selectButton;
    private JTextField rutaTextField;
    private JButton reloadButton;
    private JPanel buttonPanel;
    private JPanel southCenterPanel;
    private JPanel southPanel;
    private JPanel centerPanel;

    public DocumentDetailsPanel() {
        this.initGUI();
    }

    private void initGUI() {
        try {
            BorderLayout thisLayout = new BorderLayout();
            thisLayout.setHgap(4);
            thisLayout.setVgap(4);
            this.setLayout(thisLayout);
            this.northPanel = new JPanel();
            GridBagLayout northPanelLayout = new GridBagLayout();
            northPanelLayout.columnWidths = new int[]{85, 7, 7};
            northPanelLayout.rowHeights = new int[]{7};
            northPanelLayout.columnWeights = new double[]{0.0, 0.1, 0.1};
            northPanelLayout.rowWeights = new double[]{0.1};
            this.add((Component)this.northPanel, "North");
            this.northPanel.setLayout(northPanelLayout);
            this.nombreLabel = new JLabel();
            this.northPanel.add((Component)this.nombreLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 18, 0, new Insets(4, 4, 4, 4), 0, 0));
            this.nombreLabel.setText("Nombre");
            this.nombreTextField = new JTextField();
            this.northPanel.add((Component)this.nombreTextField, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0, 10, 1, new Insets(2, 2, 2, 2), 0, 0));
            this.centerPanel = new JPanel();
            GridBagLayout centerPanelLayout = new GridBagLayout();
            centerPanelLayout.columnWidths = new int[]{85, 7, 7};
            centerPanelLayout.rowHeights = new int[]{7};
            centerPanelLayout.columnWeights = new double[]{0.0, 0.1, 0.1};
            centerPanelLayout.rowWeights = new double[]{0.1};
            this.add((Component)this.centerPanel, "Center");
            this.centerPanel.setLayout(centerPanelLayout);
            this.centerPanel.setPreferredSize(new Dimension(340, 65));
            this.observacionesLabel = new JLabel();
            this.centerPanel.add((Component)this.observacionesLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 18, 0, new Insets(4, 4, 4, 4), 0, 0));
            this.observacionesLabel.setText("Observaciones");
            this.observacionesScrollPane = new JTableTextAreaScrollPane("observaciones", 1, 1);
            this.centerPanel.add((Component)this.observacionesScrollPane, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0, 10, 1, new Insets(2, 2, 2, 2), 0, 0));
            this.southPanel = new JPanel();
            BorderLayout southPanelLayout = new BorderLayout();
            this.add((Component)this.southPanel, "South");
            this.southPanel.setLayout(southPanelLayout);
            this.southCenterPanel = new JPanel();
            GridBagLayout southCenterPanelLayout = new GridBagLayout();
            southCenterPanelLayout.columnWidths = new int[]{85, 7, 30};
            southCenterPanelLayout.rowHeights = new int[]{7, 7, 7, 7};
            southCenterPanelLayout.columnWeights = new double[]{0.0, 0.1, 0.0};
            southCenterPanelLayout.rowWeights = new double[]{0.1, 0.1, 0.1, 0.1};
            this.southPanel.add((Component)this.southCenterPanel, "Center");
            this.southCenterPanel.setLayout(southCenterPanelLayout);
            this.rutaTextField = new JTextField();
            this.southCenterPanel.add((Component)this.getRutaTextField(), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 10, 1, new Insets(2, 2, 2, 2), 0, 0));
            this.rutaLabel = new JLabel();
            this.southCenterPanel.add((Component)this.rutaLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 18, 0, new Insets(4, 4, 4, 4), 0, 0));
            this.rutaLabel.setText("Ruta original");
            this.fechaLabel = new JLabel();
            this.southCenterPanel.add((Component)this.fechaLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, 18, 0, new Insets(4, 4, 4, 4), 0, 0));
            this.fechaLabel.setText("Modificado");
            this.sizeLabel = new JLabel();
            this.southCenterPanel.add((Component)this.sizeLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, 18, 0, new Insets(4, 4, 4, 4), 0, 0));
            this.sizeLabel.setText("Tama\u00f1o");
            this.selectButton = new JButton();
            this.southCenterPanel.add((Component)this.getSelectButton(), new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
            this.selectButton.setIcon(GUIUtil.toSmallIcon(IconLoader.icon("Open.gif")));
            this.saveToDBCheckBox = new JCheckBox();
            this.southCenterPanel.add((Component)this.saveToDBCheckBox, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
            this.saveToDBCheckBox.setText("Guardar contenido en base de datos");
            this.saveToDBCheckBox.setEnabled(false);
            this.sizeTextField = new JTextField();
            this.southCenterPanel.add((Component)this.sizeTextField, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0, 10, 1, new Insets(2, 2, 2, 2), 0, 0));
            this.sizeTextField.setEditable(false);
            this.fechaAltaTextField = new JTextField();
            this.southCenterPanel.add((Component)this.fechaAltaTextField, new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0, 10, 1, new Insets(2, 2, 2, 2), 0, 0));
            this.fechaAltaTextField.setEditable(false);
            this.buttonPanel = new JPanel();
            FlowLayout buttonPanelLayout = new FlowLayout();
            buttonPanelLayout.setHgap(10);
            this.southPanel.add((Component)this.buttonPanel, "South");
            this.buttonPanel.setLayout(buttonPanelLayout);
            this.saveChangesButton = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("Save.gif")));
            this.buttonPanel.add(this.saveChangesButton);
            this.saveChangesButton.setText("Guardar cambios");
            this.reloadButton = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("arrow_refresh.png")));
            this.buttonPanel.add(this.reloadButton);
            this.reloadButton.setText("Recargar");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JButton getSaveChangesButton() {
        return this.saveChangesButton;
    }

    public JButton getReloadButton() {
        return this.reloadButton;
    }

    public JTextField getRutaTextField() {
        return this.rutaTextField;
    }

    public JTextField getNombreTextField() {
        return this.nombreTextField;
    }

    public JButton getSelectButton() {
        return this.selectButton;
    }

    public JCheckBox getSaveToDBCheckBox() {
        return this.saveToDBCheckBox;
    }

    public JTextField getFechaAltaTextField() {
        return this.fechaAltaTextField;
    }

    public JTextField getSizeTextField() {
        return this.sizeTextField;
    }

    public JTextArea getObservacionesTextArea() {
        return this.observacionesScrollPane.getTextArea();
    }

    public JTableTextAreaScrollPane getObservacionesScrollPane() {
        return this.observacionesScrollPane;
    }
}

