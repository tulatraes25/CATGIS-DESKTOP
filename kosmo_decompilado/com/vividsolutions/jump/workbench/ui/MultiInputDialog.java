/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.CollectionMap;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerNameRenderer;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.NumberSpinner;

public class MultiInputDialog
extends JDialog {
    protected static final long serialVersionUID = 1L;
    protected static final int SIDEBAR_WIDTH = 150;
    protected Map<String, JComponent> fieldNameToComponentMap = new HashMap<String, JComponent>();
    protected Map<String, JComponent> fieldNameToLabelMap = new HashMap<String, JComponent>();
    protected OKCancelPanel okCancelPanel = new OKCancelPanel();
    protected GridBagLayout gridBagLayout2 = new GridBagLayout();
    protected JPanel outerMainPanel = new JPanel();
    private int rowCount = 0;
    private LayerNameRenderer layerListCellRenderer = new LayerNameRenderer();
    private CollectionMap fieldNameToEnableCheckListMap = new CollectionMap();
    private BorderLayout borderLayout2 = new BorderLayout();
    private JPanel imagePanel = new JPanel();
    private GridBagLayout gridBagLayout3 = new GridBagLayout();
    private JLabel imageLabel = new JLabel();
    private JPanel mainPanel = new JPanel();
    private GridBagLayout mainPanelGridBagLayout = new GridBagLayout();
    private JPanel innerMainPanel = new JPanel();
    private JPanel innerMainPanel2 = new JPanel();
    private GridBagLayout gridBagLayout5 = new GridBagLayout();
    private GridBagLayout gridBagLayout7 = new GridBagLayout();
    private GridBagLayout gridBagLayout6 = new GridBagLayout();
    private JTextArea descriptionTextArea = new JTextArea();
    private JPanel strutPanel = new JPanel();
    private JPanel currentMainPanel = this.innerMainPanel;
    private JPanel verticalSeparatorPanel = new JPanel();

    public MultiInputDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        this.imagePanel.setVisible(false);
        this.descriptionTextArea.setText("");
        this.imageLabel.setText("");
        this.innerMainPanel2.setVisible(false);
        this.verticalSeparatorPanel.setVisible(false);
    }

    public MultiInputDialog() {
        this((Frame)null, "", false);
    }

    private JComponent getComponent(String fieldName) {
        return this.fieldNameToComponentMap.get(fieldName);
    }

    public JComboBox getComboBox(String fieldName) {
        return (JComboBox)this.getComponent(fieldName);
    }

    public JCheckBox getCheckBox(String fieldName) {
        return (JCheckBox)this.getComponent(fieldName);
    }

    public JComponent getLabel(String fieldName) {
        return this.fieldNameToLabelMap.get(fieldName);
    }

    public JTextField getTextField(String fieldName) {
        return (JTextField)this.getComponent(fieldName);
    }

    public NumberSpinner getNumberSpinner(String fieldName) {
        return (NumberSpinner)this.getComponent(fieldName);
    }

    @Override
    public void setVisible(boolean visible) {
        this.pack();
        this.pack();
        GUIUtil.centreOnScreen(this);
        super.setVisible(visible);
    }

    public EnableCheck createDoubleCheck(final String fieldName) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                try {
                    Double.parseDouble(MultiInputDialog.this.getText(fieldName).trim());
                    return null;
                }
                catch (NumberFormatException e) {
                    return "\"" + MultiInputDialog.this.getText(fieldName).trim() + "\" " + I18N.getString("workbench.ui.MultiInputDialog.is-an-invalid-double") + " (" + fieldName + ")";
                }
            }
        };
    }

    public OKCancelPanel getOKCancelPanel() {
        return this.okCancelPanel;
    }

    public EnableCheck createIntegerCheck(final String fieldName) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                try {
                    Integer.parseInt(MultiInputDialog.this.getText(fieldName).trim());
                    return null;
                }
                catch (NumberFormatException e) {
                    return "\"" + MultiInputDialog.this.getText(fieldName).trim() + "\" " + I18N.getString("workbench.ui.MultiInputDialog.is-an-invalid-integer") + " (" + fieldName + ")";
                }
            }
        };
    }

    public EnableCheck createPositiveCheck(final String fieldName) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (Double.parseDouble(MultiInputDialog.this.getText(fieldName).trim()) > 0.0) {
                    return null;
                }
                return "\"" + MultiInputDialog.this.getText(fieldName).trim() + "\" " + I18N.getString("workbench.ui.MultiInputDialog.must-be") + " > 0 (" + fieldName + ")";
            }
        };
    }

    public EnableCheck createNonNegativeCheck(final String fieldName) {
        return new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (Double.parseDouble(MultiInputDialog.this.getText(fieldName).trim()) >= 0.0) {
                    return null;
                }
                return "\"" + MultiInputDialog.this.getText(fieldName).trim() + "\" " + I18N.getString("workbench.ui.MultiInputDialog.must-be") + " >= 0 (" + fieldName + ")";
            }
        };
    }

    public String getText(String fieldName) {
        if (this.fieldNameToComponentMap.get(fieldName) instanceof JTextField) {
            return ((JTextField)this.fieldNameToComponentMap.get(fieldName)).getText();
        }
        if (this.fieldNameToComponentMap.get(fieldName) instanceof JComboBox) {
            return ((JComboBox)this.fieldNameToComponentMap.get(fieldName)).getSelectedItem().toString();
        }
        if (this.fieldNameToComponentMap.get(fieldName) instanceof NumberSpinner) {
            return ((Double)((NumberSpinner)this.fieldNameToComponentMap.get(fieldName)).getValue()).toString();
        }
        Assert.shouldNeverReachHere((String)fieldName);
        return null;
    }

    public boolean getBoolean(String fieldName) {
        JCheckBox checkbox = (JCheckBox)this.fieldNameToComponentMap.get(fieldName);
        return checkbox.isSelected();
    }

    public double getDouble(String fieldName) {
        return Double.parseDouble(this.getText(fieldName).trim());
    }

    public int getInteger(String fieldName) {
        return Integer.parseInt(this.getText(fieldName).trim());
    }

    public Layer getLayer(String fieldName) {
        JComboBox comboBox = (JComboBox)this.fieldNameToComponentMap.get(fieldName);
        return (Layer)comboBox.getSelectedItem();
    }

    public JTextField addTextField(String fieldName, String initialValue, int approxWidthInChars, EnableCheck[] enableChecks, String toolTipText) {
        JTextField textField = new JTextField(initialValue, approxWidthInChars);
        this.addRow(fieldName, new JLabel(fieldName), textField, enableChecks, toolTipText);
        return textField;
    }

    public NumberSpinner addNumberSpinner(String fieldName, double defaultValue, double minValue, double maxValue, double step, EnableCheck[] enableChecks, String toolTipText) {
        final NumberSpinner numberSpinner = new NumberSpinner(defaultValue, minValue, maxValue, step);
        ((JSpinner.DefaultEditor)numberSpinner.getEditor()).getTextField().addFocusListener(new FocusListener(){

            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                JFormattedTextField formattedTextField = ((JSpinner.DefaultEditor)numberSpinner.getEditor()).getTextField();
                if (!formattedTextField.isEditValid()) {
                    String text = formattedTextField.getText();
                    DialogFactory.showWarningDialog(MultiInputDialog.this, I18N.getMessage("com.vividsolutions.jump.workbench.ui.MultiInputDialog.value-{0}-is-not-valid-it-will-return-to-the-last-valid-selected-value", new Object[]{text}), I18N.getString("com.vividsolutions.jump.workbench.ui.MultiInputDialog.warning"));
                }
            }
        });
        this.addRow(fieldName, new JLabel(fieldName), numberSpinner, enableChecks, toolTipText);
        return numberSpinner;
    }

    public JComboBox addComboBox(String fieldName, Object selectedItem, Collection items, String toolTipText) {
        JComboBox comboBox = new JComboBox(new Vector(items));
        comboBox.setSelectedItem(selectedItem);
        this.addRow(fieldName, new JLabel(fieldName), comboBox, null, toolTipText);
        return comboBox;
    }

    public JLabel addLabel(String text) {
        JLabel lbl = new JLabel(text);
        this.addRow(lbl);
        return lbl;
    }

    public void addRow(JComponent c) {
        this.addRow("DUMMY", new JLabel(""), c, null, null);
    }

    public void addSeparator() {
        JPanel separator = new JPanel();
        separator.setBackground(Color.black);
        separator.setPreferredSize(new Dimension(1, 1));
        this.addRow(separator);
    }

    private JTextField addNumericField(String fieldName, String initialValue, int approxWidthInChars, EnableCheck[] enableChecks, String toolTipText) {
        JTextField fld = this.addTextField(fieldName, initialValue, approxWidthInChars, enableChecks, toolTipText);
        fld.setHorizontalAlignment(4);
        return fld;
    }

    public JTextField addIntegerField(String fieldName, int initialValue, int approxWidthInChars, String toolTipText) {
        return this.addNumericField(fieldName, String.valueOf(initialValue), approxWidthInChars, new EnableCheck[]{this.createIntegerCheck(fieldName)}, toolTipText);
    }

    public JTextField addPositiveIntegerField(String fieldName, int initialValue, int approxWidthInChars) {
        return this.addNumericField(fieldName, String.valueOf(initialValue), approxWidthInChars, new EnableCheck[]{this.createIntegerCheck(fieldName), this.createPositiveCheck(fieldName)}, null);
    }

    public JTextField addDoubleField(String fieldName, double initialValue, int approxWidthInChars) {
        return this.addNumericField(fieldName, String.valueOf(initialValue), approxWidthInChars, new EnableCheck[]{this.createDoubleCheck(fieldName)}, null);
    }

    public JTextField addDoubleField(String fieldName, double initialValue, int approxWidthInChars, String toolTipText) {
        return this.addNumericField(fieldName, String.valueOf(initialValue), approxWidthInChars, new EnableCheck[]{this.createDoubleCheck(fieldName)}, toolTipText);
    }

    public JTextField addPositiveDoubleField(String fieldName, double initialValue, int approxWidthInChars) {
        return this.addNumericField(fieldName, String.valueOf(initialValue), approxWidthInChars, new EnableCheck[]{this.createDoubleCheck(fieldName), this.createPositiveCheck(fieldName)}, null);
    }

    public JTextField addNonNegativeDoubleField(String fieldName, double initialValue, int approxWidthInChars) {
        return this.addNumericField(fieldName, String.valueOf(initialValue), approxWidthInChars, new EnableCheck[]{this.createDoubleCheck(fieldName), this.createNonNegativeCheck(fieldName)}, null);
    }

    public static void main(String[] args) {
        MultiInputDialog d = new MultiInputDialog(null, "Title!", true);
        d.addLabel("Yay!");
        d.addLayerComboBox("LayerField", null, "ToolTip", new LayerManager());
        d.setVisible(true);
        System.out.println(d.getLayer("LayerField"));
        System.exit(0);
    }

    public JComboBox addLayerComboBox(String fieldName, Layer initialValue, LayerManager layerManager) {
        return this.addLayerComboBox(fieldName, initialValue, null, layerManager);
    }

    public JComboBox addLayerComboBox(String fieldName, Layer initialValue, String toolTipText, LayerManager layerManager) {
        return this.addLayerComboBox(fieldName, initialValue, toolTipText, layerManager.getLayers());
    }

    public JComboBox addEditableLayerComboBox(String fieldName, Layer initialValue, String toolTipText, LayerManager layerManager) {
        return this.addLayerComboBox(fieldName, initialValue, toolTipText, layerManager.getEditableLayers());
    }

    public JComboBox addLayerComboBox(String fieldName, Layer initialValue, String toolTipText, Collection layers) {
        this.addComboBox(fieldName, initialValue, layers, toolTipText);
        this.getComboBox(fieldName).setRenderer(this.layerListCellRenderer);
        return this.getComboBox(fieldName);
    }

    public JCheckBox addCheckBox(String fieldName, boolean initialValue) {
        return this.addCheckBox(fieldName, initialValue, null);
    }

    public JCheckBox addCheckBox(String fieldName, boolean initialValue, String toolTipText) {
        JCheckBox checkBox = new JCheckBox(fieldName, initialValue);
        this.addRow(fieldName, new JLabel(""), checkBox, null, toolTipText);
        return checkBox;
    }

    public void setSideBarImage(Icon icon) {
        this.imagePanel.add((Component)this.imageLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 18, 2, new Insets(10, 10, 0, 10), 0, 0));
        this.imagePanel.setVisible(true);
        this.imageLabel.setIcon(icon);
    }

    public void setSideBarDescription(String description) {
        this.imagePanel.setVisible(true);
        this.descriptionTextArea.setText(description);
    }

    public boolean wasOKPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    void jbInit() throws Exception {
        this.verticalSeparatorPanel.setBackground(Color.black);
        this.imageLabel.setText("image goes here");
        this.descriptionTextArea.setOpaque(false);
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                MultiInputDialog.this.okCancelPanel_actionPerformed(e);
            }
        });
        this.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                MultiInputDialog.this.this_componentShown(e);
            }
        });
        this.outerMainPanel.setLayout(this.gridBagLayout6);
        this.outerMainPanel.setAlignmentX(0.7f);
        this.setResizable(true);
        this.getContentPane().setLayout(this.borderLayout2);
        this.imagePanel.setBorder(BorderFactory.createEtchedBorder());
        this.imagePanel.setLayout(this.gridBagLayout3);
        this.mainPanel.setLayout(this.mainPanelGridBagLayout);
        this.innerMainPanel.setLayout(this.gridBagLayout5);
        this.innerMainPanel2.setLayout(this.gridBagLayout7);
        this.descriptionTextArea.setEnabled(false);
        this.descriptionTextArea.setEditable(false);
        this.descriptionTextArea.setText("description goes here");
        this.descriptionTextArea.setLineWrap(true);
        this.descriptionTextArea.setWrapStyleWord(true);
        this.strutPanel.setMaximumSize(new Dimension(150, 1));
        this.strutPanel.setMinimumSize(new Dimension(150, 1));
        this.strutPanel.setPreferredSize(new Dimension(150, 1));
        this.verticalSeparatorPanel.setPreferredSize(new Dimension(1, 1));
        this.getContentPane().add((Component)this.okCancelPanel, "South");
        this.getContentPane().add((Component)this.outerMainPanel, "Center");
        this.imagePanel.add((Component)this.descriptionTextArea, new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, 18, 1, new Insets(10, 10, 10, 10), 0, 0));
        this.imagePanel.add((Component)this.strutPanel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.outerMainPanel.add((Component)this.mainPanel, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, 18, 1, new Insets(0, 0, 0, 0), 0, 0));
        this.mainPanel.add((Component)this.innerMainPanel, new GridBagConstraints(1, 0, 1, 2, 1.0, 1.0, 18, 2, new Insets(10, 10, 10, 10), 0, 0));
        this.mainPanel.add((Component)this.innerMainPanel2, new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0, 18, 2, new Insets(10, 10, 10, 10), 0, 0));
        this.mainPanel.add((Component)this.verticalSeparatorPanel, new GridBagConstraints(2, 0, 1, 1, 0.0, 1.0, 10, 3, new Insets(0, 0, 0, 0), 0, 0));
        this.outerMainPanel.add((Component)this.imagePanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 18, 1, new Insets(0, 0, 0, 0), 0, 0));
        this.descriptionTextArea.setFont(this.imageLabel.getFont());
        this.descriptionTextArea.setDisabledTextColor(this.imageLabel.getForeground());
    }

    public void setInset(int inset) {
        this.setInset(inset, this.innerMainPanel);
        this.setInset(inset, this.innerMainPanel2);
    }

    private void setInset(int inset, JComponent component) {
        GridBagLayout layout = (GridBagLayout)component.getParent().getLayout();
        GridBagConstraints constraints = layout.getConstraints(component);
        constraints.insets = new Insets(inset, inset, inset, inset);
        layout.setConstraints(component, constraints);
    }

    void okCancelPanel_actionPerformed(ActionEvent e) {
        if (!this.okCancelPanel.wasOKPressed() || this.isInputValid()) {
            this.setVisible(false);
            return;
        }
        this.reportValidationError(this.firstValidationErrorMessage());
    }

    void this_componentShown(ComponentEvent e) {
        this.okCancelPanel.setOKPressed(false);
    }

    private boolean isInputValid() {
        return this.firstValidationErrorMessage() == null;
    }

    private void reportValidationError(String errorMessage) {
        DialogFactory.showWarningDialog(this, errorMessage, this.getTitle());
    }

    private String firstValidationErrorMessage() {
        for (String fieldName : this.fieldNameToEnableCheckListMap.keySet()) {
            for (EnableCheck enableCheck : this.fieldNameToEnableCheckListMap.getItems(fieldName)) {
                String message = enableCheck.check(null);
                if (message == null) continue;
                return message;
            }
        }
        return null;
    }

    public void startNewColumn() {
        if (this.innerMainPanel2.isVisible()) {
            Assert.shouldNeverReachHere((String)"#startNewColumn can be called once only");
        }
        this.currentMainPanel = this.innerMainPanel2;
        this.innerMainPanel2.setVisible(true);
        this.verticalSeparatorPanel.setVisible(true);
    }

    public void addRow(String fieldName, JComponent label, JComponent component, EnableCheck[] enableChecks, String toolTipText) {
        int labelWidth;
        int labelX;
        int componentWidth;
        int componentX;
        if (toolTipText != null) {
            label.setToolTipText(toolTipText);
        }
        this.fieldNameToLabelMap.put(fieldName, label);
        this.fieldNameToComponentMap.put(fieldName, component);
        if (enableChecks != null) {
            this.addEnableChecks(fieldName, Arrays.asList(enableChecks));
        }
        if (component instanceof JCheckBox || component instanceof JLabel || component instanceof JPanel) {
            componentX = 1;
            componentWidth = 3;
            labelX = 4;
            labelWidth = 1;
        } else {
            labelX = 1;
            labelWidth = 1;
            componentX = 2;
            componentWidth = 1;
        }
        this.currentMainPanel.add((Component)label, new GridBagConstraints(labelX, this.rowCount, labelWidth, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 5, 10), 0, 0));
        this.currentMainPanel.add((Component)component, new GridBagConstraints(componentX, this.rowCount, componentWidth, 1, 0.0, 0.0, 17, component instanceof JPanel ? 2 : 0, new Insets(0, 0, 5, 0), 0, 0));
        ++this.rowCount;
    }

    public void addEnableChecks(String fieldName, Collection enableChecks) {
        this.fieldNameToEnableCheckListMap.addItems(fieldName, enableChecks);
    }

    public void indentLabel(String comboBoxFieldName) {
        this.getLabel(comboBoxFieldName).setBorder(BorderFactory.createMatteBorder(0, (int)new JCheckBox().getPreferredSize().getWidth(), 0, 0, this.getLabel(comboBoxFieldName).getBackground()));
    }
}

