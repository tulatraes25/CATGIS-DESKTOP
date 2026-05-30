/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package es.kosmo.desktop.widgets.utils.topology;

import com.vividsolutions.jump.io.datasource.DataSourceQuery;
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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.JAvailableLayersComboBox;
import org.saig.jump.widgets.util.JQueryChooserPanel;
import org.saig.jump.widgets.util.NumberSpinner;
import org.saig.jump.widgets.util.SelectFilePanel;

public class AssignmentByProximityOptionsDialog
extends JDialog
implements ActionListener {
    private static final long serialVersionUID = 1L;
    private JPanel decorationPanel;
    private JPanel sourceOptionsPanel;
    private JAvailableLayersComboBox startLayerSelectionComboBox;
    private JAvailableLayersComboBox crossLayerSelectionComboBox;
    private JAvailableLayersComboBox sourceLayerSelectionComboBox;
    private JPanel outputOptionsPanel;
    private JQueryChooserPanel pointOutputLayerSelectionPanel;
    private JQueryChooserPanel errorLayerSelectionPanel;
    private JQueryChooserPanel linealOutputLayerSelectionPanel;
    private SelectFilePanel selectTablePanel;
    private JPanel tolerancePanel;
    private NumberSpinner startToCrossToleranceNumberSpinner;
    private NumberSpinner startToSourceToleranceNumberSpinner;
    private OKCancelPanel okCancelPanel;
    private LayerManager layerManager;

    public AssignmentByProximityOptionsDialog(JFrame owner, boolean modal, LayerManager manager) {
        super((Frame)owner, modal);
        this.setTitle(I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Options-for-the-tool-Assigment-by-proximity"));
        this.layerManager = manager;
        this.initialize();
        this.pack();
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);
        JPanel centerPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(centerPanel, 0, 0, this.getSourceOptionsPanel());
        FormUtils.addRowInGBL(centerPanel, 1, 0, this.getOutputOptionsPanel());
        FormUtils.addRowInGBL(centerPanel, 2, 0, this.getTolerancePanel());
        mainPanel.add((Component)this.getDecorationPanel(), "West");
        mainPanel.add((Component)centerPanel, "Center");
        mainPanel.add((Component)this.getOkCancelPanel(), "South");
    }

    private JPanel getDecorationPanel() {
        if (this.decorationPanel == null) {
            this.decorationPanel = new JPanel(new BorderLayout());
            this.decorationPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            JLabel imageLabel = new JLabel(IconLoader.icon("toolImages/assignByProximity.png"));
            this.decorationPanel.add((Component)imageLabel, "Center");
        }
        return this.decorationPanel;
    }

    private JPanel getSourceOptionsPanel() {
        if (this.sourceOptionsPanel == null) {
            this.sourceOptionsPanel = new JPanel(new GridBagLayout());
            this.sourceOptionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Input")));
            JLabel startLayerSelectionLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Input-layer")) + ":");
            this.startLayerSelectionComboBox = new JAvailableLayersComboBox(this.layerManager, false, false, true, false);
            JLabel crossLayerSelectionLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Crossing-layer")) + ":");
            this.crossLayerSelectionComboBox = new JAvailableLayersComboBox(this.layerManager, false, false, true, false);
            JLabel sourceLayerSelectionLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Source-layer")) + ":");
            this.sourceLayerSelectionComboBox = new JAvailableLayersComboBox(this.layerManager, false, false, true, false);
            FormUtils.addRowInGBL((JComponent)this.sourceOptionsPanel, 0, 0, startLayerSelectionLabel, (JComponent)this.startLayerSelectionComboBox);
            FormUtils.addRowInGBL((JComponent)this.sourceOptionsPanel, 1, 0, crossLayerSelectionLabel, (JComponent)this.crossLayerSelectionComboBox);
            FormUtils.addRowInGBL((JComponent)this.sourceOptionsPanel, 2, 0, sourceLayerSelectionLabel, (JComponent)this.sourceLayerSelectionComboBox);
        }
        return this.sourceOptionsPanel;
    }

    private JPanel getOutputOptionsPanel() {
        if (this.outputOptionsPanel == null) {
            this.outputOptionsPanel = new JPanel(new GridBagLayout());
            this.outputOptionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Output")));
            this.pointOutputLayerSelectionPanel = new JQueryChooserPanel(I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Point-layer"), I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Save-point-layer"), false, false, false);
            this.linealOutputLayerSelectionPanel = new JQueryChooserPanel(I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Validating-layer"), I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Save-validating-layer"), false, true, false);
            this.errorLayerSelectionPanel = new JQueryChooserPanel(I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Error-layer"), I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Save-errors-layer"), true, true, true);
            this.selectTablePanel = new SelectFilePanel(I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Select-the-output-file-for-the-data-table"), new String[]{"dbf"}, false);
            this.selectTablePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Data-table")));
            this.selectTablePanel.setMinimumSize(new Dimension(450, 50));
            this.selectTablePanel.setPreferredSize(new Dimension(450, 50));
            FormUtils.addRowInGBL(this.outputOptionsPanel, 0, 0, this.pointOutputLayerSelectionPanel);
            FormUtils.addRowInGBL(this.outputOptionsPanel, 1, 0, this.selectTablePanel);
            FormUtils.addRowInGBL(this.outputOptionsPanel, 2, 0, this.linealOutputLayerSelectionPanel);
            FormUtils.addRowInGBL(this.outputOptionsPanel, 3, 0, this.errorLayerSelectionPanel);
        }
        return this.outputOptionsPanel;
    }

    private JPanel getTolerancePanel() {
        if (this.tolerancePanel == null) {
            this.tolerancePanel = new JPanel(new GridBagLayout());
            this.tolerancePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Tolerance")));
            JLabel startToCrossToleranceLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.From-input-to-crossing")) + ":");
            this.startToCrossToleranceNumberSpinner = new NumberSpinner(0.0, 0.0, 9.999999999E8, 0.5);
            JLabel startToSourceToleranceLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.From-input-to-source")) + ":");
            this.startToSourceToleranceNumberSpinner = new NumberSpinner(0.0, 0.0, 9.999999999E8, 0.5);
            FormUtils.addRowInGBL((JComponent)this.tolerancePanel, 0, 0, startToCrossToleranceLabel, (JComponent)this.startToCrossToleranceNumberSpinner);
            FormUtils.addRowInGBL((JComponent)this.tolerancePanel, 1, 0, startToSourceToleranceLabel, (JComponent)this.startToSourceToleranceNumberSpinner);
        }
        return this.tolerancePanel;
    }

    private OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(this);
        }
        return this.okCancelPanel;
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.okCancelPanel.setOKPressed(false);
        }
        super.setVisible(visible);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.okCancelPanel) {
            if (this.okCancelPanel.wasOKPressed()) {
                if (this.isInputValid()) {
                    this.setVisible(false);
                }
            } else {
                this.setVisible(false);
            }
        }
    }

    private boolean isInputValid() {
        boolean outputOptionsSelected;
        boolean sourceLayersSelected;
        boolean bl = sourceLayersSelected = StringUtils.isNotEmpty((String)this.getStartLayerName()) && StringUtils.isNotEmpty((String)this.getCrossLayerName()) && StringUtils.isNotEmpty((String)this.getSourceLayerName());
        if (!sourceLayersSelected) {
            DialogFactory.showWarningDialog(this, I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.You-must-select-the-three-input-layers"), I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Input-layers-not-selected"));
            return false;
        }
        boolean bl2 = outputOptionsSelected = this.pointOutputLayerSelectionPanel.isInputValid() && this.linealOutputLayerSelectionPanel.isInputValid() && this.errorLayerSelectionPanel.isInputValid();
        if (outputOptionsSelected && !this.selectTablePanel.isInputValid()) {
            outputOptionsSelected = false;
            DialogFactory.showWarningDialog(this, I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.You-must-select-the-output-data-table-file-path"), I18N.getString("es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog.Path-not-selected"));
        }
        return outputOptionsSelected;
    }

    public String getSourceLayerName() {
        return this.sourceLayerSelectionComboBox.getSelectedLayerName();
    }

    public String getCrossLayerName() {
        return this.crossLayerSelectionComboBox.getSelectedLayerName();
    }

    public String getStartLayerName() {
        return this.startLayerSelectionComboBox.getSelectedLayerName();
    }

    public DataSourceQuery getPointOutputQuery() {
        return this.pointOutputLayerSelectionPanel.getDataSourceQuery();
    }

    public DataSourceQuery getLinealOutputQuery() {
        return this.linealOutputLayerSelectionPanel.getDataSourceQuery();
    }

    public DataSourceQuery getErrorOutputQuery() {
        return this.errorLayerSelectionPanel.getDataSourceQuery();
    }

    public String getDataTablePath() {
        return this.selectTablePanel.getSelectedPath();
    }

    public double getStartToCrossTolerance() {
        return this.startToCrossToleranceNumberSpinner.getDoubleValue();
    }

    public double getStartToSourceTolerance() {
        return this.startToSourceToleranceNumberSpinner.getDoubleValue();
    }
}

