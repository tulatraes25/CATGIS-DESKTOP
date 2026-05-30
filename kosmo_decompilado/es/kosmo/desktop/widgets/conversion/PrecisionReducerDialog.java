/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jcs.precision.NumberPrecisionReducer
 */
package es.kosmo.desktop.widgets.conversion;

import com.vividsolutions.jcs.precision.NumberPrecisionReducer;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.JQueryChooserPanel;
import org.saig.jump.widgets.util.ToolTargetSelectorPanel;
import org.saig.jump.widgets.util.validating.IntegerTextFieldValidator;

public class PrecisionReducerDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final double EXAMPLE_VALUE = 1234567.123123123;
    private static final int DEFAULT_DECIMAL_PLACES = 0;
    private static final int DEFAULT_SCALE_FACTOR = 1;
    private JQueryChooserPanel invalidInputGeometriesQueryChooserPanel;
    private JQueryChooserPanel invalidReducedGeometriesQueryChooserPanel;
    private ToolTargetSelectorPanel toolTargetSelectorPanel;
    private JTextField decimalPlacesField;
    private JTextField scaleFactorField;
    private JLabel exampleLabel;
    private OKCancelPanel okCancelPanel;
    protected boolean exitOK;
    private int decimalPlaces = 0;
    private int scaleFactor = 1;
    private final Layer layer;
    private final SelectionManager sm;

    public PrecisionReducerDialog(JFrame parent, boolean modal, Layer layer, SelectionManager sm) {
        super((Frame)parent, modal);
        this.layer = layer;
        this.sm = sm;
        this.exitOK = false;
        this.setTitle(I18N.getString("org.saig.jump.widgets.utils.conversion.PrecisionReducerDialog.precision-reducer"));
        this.setContentPane(this.getMainPanel());
        this.pack();
        GUIUtil.centreOnWindow(this);
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add((Component)this.createImagePanel(), "West");
        mainPanel.add((Component)this.createOptionsPanel(), "Center");
        mainPanel.add((Component)this.createOKCancelPanel(), "South");
        this.updateExample();
        return mainPanel;
    }

    private JPanel createImagePanel() {
        JPanel imagePanel = new JPanel(new GridBagLayout());
        JLabel imageLabel = new JLabel(IconLoader.icon("toolImages/PrecisionReducer.png"));
        JTextArea descriptionTextArea = new JTextArea(I18N.getString("org.saig.jump.widgets.utils.conversion.PrecisionReducerDialog.reduce-coordinates-precision-in-a-layer-applied-changes-will-not-be-reversible"));
        descriptionTextArea.setOpaque(false);
        descriptionTextArea.setEnabled(false);
        descriptionTextArea.setEditable(false);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setFont(imageLabel.getFont());
        descriptionTextArea.setDisabledTextColor(imageLabel.getForeground());
        FormUtils.addRowInGBL(imagePanel, 0, 0, imageLabel);
        FormUtils.addRowInGBL(imagePanel, 1, 0, descriptionTextArea);
        return imagePanel;
    }

    private JPanel createOptionsPanel() {
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        this.scaleFactorField = new JTextField(8);
        this.scaleFactorField.setText("1");
        this.scaleFactorField.setToolTipText(I18N.getString("org.saig.jump.widgets.utils.conversion.PrecisionReducerDialog.the-scale-factor-to-multiply-by-previous-rounding-negative-for-left-decimal-point-zero-is-not-used"));
        this.scaleFactorField.getDocument().addDocumentListener(new ScaleFactorDocumentListener());
        IntegerTextFieldValidator integerScaleFactorValidator = new IntegerTextFieldValidator(this, this.scaleFactorField);
        integerScaleFactorValidator.setObligatorio(true);
        this.scaleFactorField.setInputVerifier(integerScaleFactorValidator);
        this.decimalPlacesField = new JTextField(4);
        this.decimalPlacesField.setText("0");
        this.decimalPlacesField.setToolTipText(I18N.getString("org.saig.jump.widgets.utils.conversion.PrecisionReducerDialog.number-of-decimals-displaced-to-round-negative-for-decimal-points-to-the-left"));
        this.decimalPlacesField.getDocument().addDocumentListener(new DecimalPlacesDocumentListener());
        IntegerTextFieldValidator integerDecimalPlacesValidator = new IntegerTextFieldValidator(this, this.decimalPlacesField);
        integerDecimalPlacesValidator.setObligatorio(true);
        this.decimalPlacesField.setInputVerifier(integerDecimalPlacesValidator);
        this.exampleLabel = new JLabel("");
        this.invalidReducedGeometriesQueryChooserPanel = new JQueryChooserPanel(I18N.getString("org.saig.jump.widgets.utils.conversion.PrecisionReducerDialog.not-valid-reduced-geometries-layer"), I18N.getString("org.saig.jump.widgets.utils.conversion.PrecisionReducerDialog.save-not-valid-reduced-geometries-layer"), true);
        this.invalidInputGeometriesQueryChooserPanel = new JQueryChooserPanel(I18N.getString("org.saig.jump.widgets.utils.conversion.PrecisionReducerDialog.not-valid-input-geometries-layer"), I18N.getString("org.saig.jump.widgets.utils.conversion.PrecisionReducerDialog.save-not-valid-input-geometries-layer"), true);
        this.toolTargetSelectorPanel = new ToolTargetSelectorPanel(this.layer, this.sm);
        FormUtils.addRowInGBL((JComponent)optionsPanel, 0, 0, new JLabel(I18N.getString("org.saig.jump.widgets.utils.conversion.PrecisionReducerDialog.scale-factor")), (JComponent)this.scaleFactorField, true);
        FormUtils.addRowInGBL((JComponent)optionsPanel, 2, 0, new JLabel(I18N.getString("org.saig.jump.widgets.utils.conversion.PrecisionReducerDialog.decimals-position")), (JComponent)this.decimalPlacesField, true);
        FormUtils.addRowInGBL(optionsPanel, 3, 0, new JLabel(""));
        FormUtils.addRowInGBL(optionsPanel, 4, 0, new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.utils.conversion.PrecisionReducerDialog.example")) + 1234567.123123123));
        FormUtils.addRowInGBL(optionsPanel, 5, 0, this.exampleLabel);
        FormUtils.addRowInGBL(optionsPanel, 6, 0, this.toolTargetSelectorPanel);
        FormUtils.addRowInGBL(optionsPanel, 7, 0, this.invalidReducedGeometriesQueryChooserPanel);
        FormUtils.addRowInGBL(optionsPanel, 8, 0, this.invalidInputGeometriesQueryChooserPanel);
        return optionsPanel;
    }

    public int getSelectedTargetOption() {
        return this.toolTargetSelectorPanel.getSelectedOption();
    }

    private void decimalPlacesChanged() {
        this.decimalPlaces = this.parseValidInt(this.decimalPlacesField.getText());
        double sf = NumberPrecisionReducer.scaleFactorForDecimalPlaces((int)this.decimalPlaces);
        this.scaleFactorField.setText("" + (int)sf);
        this.updateExample();
    }

    private void scaleFactorChanged() {
        this.scaleFactor = this.parseValidInt(this.scaleFactorField.getText());
        this.updateExample();
    }

    private void updateExample() {
        NumberPrecisionReducer cpr = new NumberPrecisionReducer((double)this.scaleFactor);
        double exampleOutput = cpr.reducePrecision(1234567.123123123);
        this.exampleLabel.setText("      ==>  " + exampleOutput);
    }

    private int parseValidInt(String text) {
        int i = 0;
        try {
            i = Integer.parseInt(text);
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        return i;
    }

    private JPanel createOKCancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.addActionListener(new ActionListener(){
            boolean error = false;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (PrecisionReducerDialog.this.okCancelPanel.wasOKPressed()) {
                    if (PrecisionReducerDialog.this.isInputValid()) {
                        PrecisionReducerDialog.this.exitOK = true;
                        this.error = false;
                    } else {
                        PrecisionReducerDialog.this.exitOK = false;
                        this.error = true;
                    }
                } else {
                    PrecisionReducerDialog.this.exitOK = false;
                    this.error = false;
                }
                if (!this.error) {
                    PrecisionReducerDialog.this.setVisible(false);
                    PrecisionReducerDialog.this.dispose();
                }
            }
        });
        return this.okCancelPanel;
    }

    public boolean isExitOk() {
        return this.exitOK;
    }

    private boolean isInputValid() {
        if (!this.scaleFactorField.getInputVerifier().verify(this.scaleFactorField)) {
            return false;
        }
        if (!this.decimalPlacesField.getInputVerifier().verify(this.decimalPlacesField)) {
            return false;
        }
        if (!this.invalidReducedGeometriesQueryChooserPanel.isInputValid()) {
            return false;
        }
        return this.invalidInputGeometriesQueryChooserPanel.isInputValid();
    }

    public int getScaleFactor() {
        return this.scaleFactor;
    }

    public int getDecimalPlaces() {
        return this.decimalPlaces;
    }

    public DataSourceQuery getInvalidReducedQuery() {
        return this.invalidReducedGeometriesQueryChooserPanel.getDataSourceQuery();
    }

    public DataSourceQuery getInvalidInputQuery() {
        return this.invalidInputGeometriesQueryChooserPanel.getDataSourceQuery();
    }

    public FeatureIterator getFeaturesToProcess(PlugInContext context) throws Exception {
        return this.toolTargetSelectorPanel.getFeaturesToProcess(context);
    }

    public boolean hasFeaturesToProcess(PlugInContext context) throws Exception {
        return this.toolTargetSelectorPanel.hasFeaturesToProcess(context);
    }

    class DecimalPlacesDocumentListener
    implements DocumentListener {
        DecimalPlacesDocumentListener() {
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            PrecisionReducerDialog.this.decimalPlacesChanged();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            PrecisionReducerDialog.this.decimalPlacesChanged();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            PrecisionReducerDialog.this.decimalPlacesChanged();
        }
    }

    class ScaleFactorDocumentListener
    implements DocumentListener {
        ScaleFactorDocumentListener() {
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            PrecisionReducerDialog.this.scaleFactorChanged();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            PrecisionReducerDialog.this.scaleFactorChanged();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            PrecisionReducerDialog.this.scaleFactorChanged();
        }
    }
}

