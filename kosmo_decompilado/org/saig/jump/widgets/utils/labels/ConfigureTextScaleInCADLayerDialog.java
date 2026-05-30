/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.utils.labels;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class ConfigureTextScaleInCADLayerDialog
extends JDialog
implements ActionListener {
    private static final long serialVersionUID = 1L;
    protected static final Logger LOGGER = Logger.getLogger(ConfigureTextScaleInCADLayerDialog.class);
    private JCheckBox chkScale;
    private JTextField neScaleMin;
    private JTextField neScaleMax;
    private OKCancelPanel okCancelPanel;

    public ConfigureTextScaleInCADLayerDialog(JFrame parent, boolean modal, Layer layer) {
        super((Frame)parent, modal);
        this.setTitle(I18N.getString("org.saig.jump.widgets.utils.labels.ConfigureTextScaleInCADLayerDialog.Configure-scale"));
        this.setContentPane(this.getMainPanel(layer));
        this.refresh(layer);
        this.pack();
    }

    private JPanel getMainPanel(Layer layer) {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.chkScale = new JCheckBox(I18N.getString("org.saig.jump.widgets.utils.labels.ConfigureTextScaleInCADLayerDialog.Scale-label"));
        this.chkScale.addActionListener(this);
        this.neScaleMax = new JTextField();
        this.neScaleMin = new JTextField();
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.chkScale);
        FormUtils.addRowInGBL((JComponent)mainPanel, 2, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.utils.labels.ConfigureTextScaleInCADLayerDialog.Minimum-value")) + ":", (JComponent)this.neScaleMin);
        FormUtils.addRowInGBL((JComponent)mainPanel, 3, 0, String.valueOf(I18N.getString("org.saig.jump.widgets.utils.labels.ConfigureTextScaleInCADLayerDialog.Maximum-value")) + ":", (JComponent)this.neScaleMax);
        FormUtils.addRowInGBL(mainPanel, 4, 0, this.getOkCancelPanel());
        FormUtils.addFiller(mainPanel, 5, 0);
        return mainPanel;
    }

    private OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(this);
        }
        return this.okCancelPanel;
    }

    protected boolean isInputValid() {
        double valueMax = 0.0;
        double valueMin = 0.0;
        if (this.chkScale.isSelected()) {
            try {
                valueMax = Double.parseDouble(this.neScaleMax.getText());
                valueMin = Double.parseDouble(this.neScaleMin.getText());
                if (valueMin > valueMax) {
                    DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.widgets.utils.labels.ConfigureTextScaleInCADLayerDialog.The-maximum-value-must-be-higher-than-the-minimum-one"), I18N.getString("org.saig.jump.widgets.utils.labels.ConfigureTextScaleInCADLayerDialog.Error"));
                    return false;
                }
            }
            catch (NumberFormatException e) {
                LOGGER.error((Object)e);
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.widgets.utils.labels.ConfigureTextScaleInCADLayerDialog.The-minimum-and-maximum-values-must-be-numeric"), I18N.getString("org.saig.jump.widgets.utils.labels.ConfigureTextScaleInCADLayerDialog.Error"));
                return false;
            }
        }
        return true;
    }

    private void refresh(Layer layer) {
        FeatureTypeStyle style = layer.getModelStyle().getSelectedFeatureTypeStyle();
        Rule[] rules = style.getRules();
        int i = 0;
        while (i < rules.length) {
            Rule rule = rules[i];
            Symbolizer[] simbolos = rule.getSymbolizers();
            int j = 0;
            while (j < simbolos.length) {
                Symbolizer symbol = simbolos[j];
                if (symbol instanceof TextSymbolizer) {
                    TextSymbolizer textSymbol = (TextSymbolizer)symbol;
                    this.chkScale.setSelected(textSymbol.isScale());
                    if (textSymbol.isScale()) {
                        this.neScaleMax.setText(Double.toString(textSymbol.getScaleMaxValue()));
                        this.neScaleMin.setText(Double.toString(textSymbol.getScaleMinValue()));
                    }
                    this.neScaleMax.setEnabled(textSymbol.isScale());
                    this.neScaleMin.setEnabled(textSymbol.isScale());
                }
                ++j;
            }
            ++i;
        }
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
        if (e.getSource().equals(this.chkScale)) {
            this.neScaleMax.setEnabled(this.chkScale.isSelected());
            this.neScaleMin.setEnabled(this.chkScale.isSelected());
        } else if (e.getSource().equals(this.okCancelPanel)) {
            if (this.okCancelPanel.wasOKPressed()) {
                if (this.isInputValid()) {
                    this.setVisible(false);
                }
            } else {
                this.setVisible(false);
            }
        }
    }

    public boolean isScaled() {
        return this.chkScale.isSelected();
    }

    public double getScaleMaxValue() {
        return Double.parseDouble(this.neScaleMax.getText());
    }

    public double getScaleMinValue() {
        return Double.parseDouble(this.neScaleMin.getText());
    }
}

