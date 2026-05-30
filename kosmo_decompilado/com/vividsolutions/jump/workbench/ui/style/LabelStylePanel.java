/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.style;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.FontChooser;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.ValidatingTextField;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.style.LabelStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import com.vividsolutions.jump.workbench.ui.style.StylePanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.feature.Attribute;
import org.saig.jump.lang.I18N;

public class LabelStylePanel
extends JPanel
implements StylePanel,
ActionListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(LabelStylePanel.class);
    private static final String NONE = "<< " + I18N.getString("workbench.ui.style.LabelStylePanel.none") + " >>";
    private WeakHashMap<String, Feature> firstFeaturesMap = new WeakHashMap();
    private Map<String, String> verticalAlignmentMap;
    private JLabel attributeLabel = new JLabel();
    private JComboBox attributeComboBox = new JComboBox();
    private JComboBox angleAttributeComboBox = new JComboBox();
    private JPanel previewPanel = new JPanel(){
        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            LabelStyle labelStyle = LabelStylePanel.this.createLabelStyle(LabelStylePanel.this.layer.getLabelStyle());
            if (!labelStyle.isEnabled()) {
                return;
            }
            labelStyle.initialize(LabelStylePanel.this.layer);
            try {
                labelStyle.paint((Graphics2D)g, LabelStylePanel.this.sampleText(), LabelStylePanel.this.layerViewPanel.getViewport().getScale(), new Point2D.Double((double)this.getWidth() / 2.0, (double)this.getHeight() / 2.0), LabelStylePanel.this.layer.getFeatureCollectionWrapper().isEmpty() ? 0.0 : LabelStyle.angle(LabelStylePanel.this.getFirstFeature(), LabelStylePanel.this.getAngleAttribute(), 0.0), LabelStylePanel.this.layer.getFeatureCollectionWrapper().isEmpty() ? LabelStylePanel.this.getLabelHeight() : LabelStyle.height(LabelStylePanel.this.getFirstFeature(), LabelStylePanel.this.getHeightAttribute(), LabelStylePanel.this.getLabelHeight()), false);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
    };
    private JCheckBox glowingCheckBox = new JCheckBox();
    private Layer layer;
    private JCheckBox labellingCheckBox = new JCheckBox();
    private LayerViewPanel layerViewPanel;
    private JDialog parent;
    private Color color;
    private Font labelFont;
    private ValidatingTextField heightTextField = new ValidatingTextField("999", 7, new ValidatingTextField.Validator(){

        @Override
        public boolean isValid(String text) {
            if (text.length() == 0) {
                return true;
            }
            try {
                Double.parseDouble(text);
                return true;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
    });
    private JLabel heightLabel = new JLabel();
    private JLabel previewLabel = new JLabel();
    private JPanel buttonPanel = new JPanel();
    private JButton colorButton = new JButton();
    private JButton colorGlowingButton = new JButton();
    private JButton fontButton = new JButton();
    private JLabel verticalAlignmentLabel = new JLabel();
    private JComboBox verticalAlignmentComboBox = new JComboBox();
    private JLabel angleLabel = new JLabel();
    private Color colorGlowing;
    private JCheckBox hideOverlappingLabelsCheckBox = new JCheckBox();
    private JLabel heightAttributeLabel = new JLabel();
    private JComboBox heightAttributeComboBox = new JComboBox();

    public LabelStylePanel(Layer layer, LayerViewPanel layerViewPanel, JDialog parent, ErrorHandler errorHandler) {
        try {
            this.parent = parent;
            this.layerViewPanel = layerViewPanel;
            this.verticalAlignmentMap = new HashMap<String, String>();
            this.verticalAlignmentMap.put("ABOVE_LINE", I18N.getString("workbench.ui.style.LabelStylePanel.Above-the-line"));
            this.verticalAlignmentMap.put("ON_LINE", I18N.getString("workbench.ui.style.LabelStylePanel.On-the-line"));
            this.verticalAlignmentMap.put("BELOW_LINE", I18N.getString("workbench.ui.style.LabelStylePanel.Below-the-line"));
            this.verticalAlignmentComboBox.addItem("ABOVE_LINE");
            this.verticalAlignmentComboBox.addItem("ON_LINE");
            this.verticalAlignmentComboBox.addItem("BELOW_LINE");
            this.setLayer(layer);
            this.jbInit();
            this.heightTextField.getDocument().addDocumentListener(new DocumentListener(){

                @Override
                public void insertUpdate(DocumentEvent e) {
                    this.documentChanged();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    this.documentChanged();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    this.documentChanged();
                }

                private void documentChanged() {
                    LabelStylePanel.this.updateControls();
                }
            });
            this.colorButton.setToolTipText(I18N.getString("workbench.ui.style.LabelStylePanel.browse"));
            this.fontButton.setToolTipText(I18N.getString("workbench.ui.style.LabelStylePanel.browse"));
            this.updateControls();
            this.verticalAlignmentComboBox.setRenderer(new ListCellRenderer(){
                private Icon aboveIcon = IconLoader.icon("BigLabelAbove.gif");
                private Icon onIcon = IconLoader.icon("BigLabelOn.gif");
                private Icon belowIcon = IconLoader.icon("BigLabelBelow.gif");
                private DefaultListCellRenderer renderer = new DefaultListCellRenderer();

                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel)this.renderer.getListCellRendererComponent((JList<?>)list, "", index, isSelected, cellHasFocus);
                    label.setText((String)LabelStylePanel.this.verticalAlignmentMap.get(value));
                    return label;
                }
            });
        }
        catch (Throwable t) {
            errorHandler.handleThrowable(t);
        }
    }

    @Override
    public String getTitle() {
        return I18N.getString("workbench.ui.style.LabelStylePanel.title");
    }

    @Override
    public String validateInput() {
        return null;
    }

    private String sampleText() {
        String sampleText = "Abc123";
        try {
            if (this.layer.getFeatureCollectionWrapper().isEmpty()) {
                return sampleText;
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            return sampleText;
        }
        Feature firstFeature = this.getFirstFeature();
        if (firstFeature != null) {
            Object attribute;
            Attribute attr = this.layer.getFeatureSchema().getPublicAttribute(this.getLabelAttribute());
            if (attr == null) {
                return sampleText;
            }
            String attributeRealName = attr.getName();
            Object object = attribute = this.getLabelAttribute().equals("$FID") ? String.valueOf(firstFeature.getID()) : firstFeature.getAttribute(attributeRealName);
            if (attribute == null || StringUtils.isEmpty((String)attribute.toString())) {
                return sampleText;
            }
            return attribute.toString().trim();
        }
        return sampleText;
    }

    private Feature getFirstFeature() {
        Feature firstFeature = this.firstFeaturesMap.get(this.layer.getName());
        if (firstFeature != null) {
            return firstFeature;
        }
        Feature obj = this.layer.getFeatureCollectionWrapper().getUltimateWrappee().getFeaturesSamples(1).get(0);
        if (obj == null) {
            return null;
        }
        firstFeature = obj;
        this.firstFeaturesMap.put(this.layer.getName(), firstFeature);
        return firstFeature;
    }

    private void setLayer(Layer layer) {
        this.layer = layer;
        if (layer.getLabelStyle() == null) {
            layer.addStyle(new LabelStyle());
        }
        this.setLabelling(layer.getLabelStyle().isEnabled());
        this.setAttributes(layer.getFeatureCollectionWrapper().getFeatureSchema());
        this.setAttribute(layer.getLabelStyle().getAttribute());
        this.setAngleAttribute(layer.getLabelStyle().getAngleAttribute());
        this.setHeightAttribute(layer.getLabelStyle().getHeightAttribute());
        this.setColor(layer.getLabelStyle().getColor());
        this.setLabelFont(layer.getLabelStyle().getFont());
        this.setGlowing(layer.getLabelStyle().isGlowing());
        this.setColorGlowing(layer.getLabelStyle().getColorGlowing());
        this.hideOverlappingLabelsCheckBox.setSelected(layer.getLabelStyle().isHidingOverlappingLabels());
        this.heightTextField.setText(String.valueOf(layer.getLabelStyle().getHeight()));
        this.verticalAlignmentComboBox.setSelectedItem(layer.getLabelStyle().getVerticalAlignment());
    }

    private void setAttributes(FeatureSchema schema) {
        this.attributeComboBox.removeAllItems();
        this.angleAttributeComboBox.removeAllItems();
        this.heightAttributeComboBox.removeAllItems();
        this.angleAttributeComboBox.addItem(NONE);
        this.heightAttributeComboBox.addItem(NONE);
        int i = 0;
        while (i < schema.getAttributeCount()) {
            this.attributeComboBox.addItem(schema.getPublicName(i));
            if (AttributeType.isNumeric(schema.getAttributeType(i))) {
                this.angleAttributeComboBox.addItem(schema.getPublicName(i));
                this.heightAttributeComboBox.addItem(schema.getPublicName(i));
            }
            ++i;
        }
    }

    private void setLabelling(boolean labelling) {
        this.labellingCheckBox.setSelected(labelling);
    }

    private void setColor(Color labelColor) {
        this.color = labelColor;
    }

    private void setColorGlowing(Color glowingColor) {
        this.colorGlowing = glowingColor;
    }

    private void setAttribute(String attribute) {
        Assert.isTrue((!attribute.equals("") ? 1 : 0) != 0);
        this.attributeComboBox.setSelectedItem(attribute);
    }

    private void setAngleAttribute(String angleAttribute) {
        if (StringUtils.isEmpty((String)angleAttribute)) {
            this.angleAttributeComboBox.setSelectedItem(NONE);
        } else {
            this.angleAttributeComboBox.setSelectedItem(angleAttribute);
        }
    }

    private void setHeightAttribute(String heightAttribute) {
        if (StringUtils.isEmpty((String)heightAttribute)) {
            this.heightAttributeComboBox.setSelectedItem(NONE);
        } else {
            this.heightAttributeComboBox.setSelectedItem(heightAttribute);
        }
    }

    private void setLabelFont(Font labelFont) {
        this.labelFont = labelFont;
    }

    private void setGlowing(boolean glowing) {
        this.glowingCheckBox.setSelected(glowing);
    }

    private void jbInit() throws Exception {
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setLayout(new GridBagLayout());
        this.attributeLabel.setText(String.valueOf(I18N.getString("workbench.ui.style.LabelStylePanel.label-attribute")) + ":");
        this.previewPanel.setBackground(Color.white);
        this.previewPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        this.previewPanel.setMaximumSize(new Dimension(200, 40));
        this.previewPanel.setMinimumSize(new Dimension(200, 40));
        this.previewPanel.setPreferredSize(new Dimension(200, 40));
        this.glowingCheckBox.setText(I18N.getString("com.vividsolutions.jump.workbench.ui.style.LabelStylePanel.halo"));
        this.glowingCheckBox.addActionListener(this);
        this.colorGlowingButton.setText(String.valueOf(I18N.getString("workbench.ui.style.LabelStylePanel.change-colour")) + " ...");
        this.colorGlowingButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                LabelStylePanel.this.colorGlowingButton_actionPerformed(e);
            }
        });
        this.labellingCheckBox.setText(I18N.getString("workbench.ui.style.LabelStylePanel.enable-labelling"));
        this.labellingCheckBox.addActionListener(this);
        this.heightLabel.setText(String.valueOf(I18N.getString("workbench.ui.style.LabelStylePanel.height")) + ":");
        this.attributeComboBox.addActionListener(this);
        this.angleAttributeComboBox.addActionListener(this);
        this.previewLabel.setText(String.valueOf(I18N.getString("workbench.ui.style.LabelStylePanel.preview-at-current-zoom-level")) + ":");
        this.buttonPanel.setLayout(new FlowLayout());
        this.colorButton.setText(String.valueOf(I18N.getString("workbench.ui.style.LabelStylePanel.change-colour")) + " ...");
        this.colorButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                LabelStylePanel.this.colorButton_actionPerformed(e);
            }
        });
        this.fontButton.setText(String.valueOf(I18N.getString("workbench.ui.style.LabelStylePanel.change-font")) + " ...");
        this.fontButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                LabelStylePanel.this.fontButton_actionPerformed(e);
            }
        });
        this.verticalAlignmentLabel.setText(String.valueOf(I18N.getString("workbench.ui.style.LabelStylePanel.vertical-alignment-for-lines")) + ":");
        this.verticalAlignmentComboBox.addActionListener(this);
        this.angleLabel.setText(String.valueOf(I18N.getString("workbench.ui.style.LabelStylePanel.rotation-attribute-degrees")) + ":");
        this.hideOverlappingLabelsCheckBox.setText(I18N.getString("workbench.ui.style.LabelStylePanel.hide-overlapping-labels"));
        this.hideOverlappingLabelsCheckBox.addActionListener(this);
        this.heightAttributeLabel.setText(String.valueOf(I18N.getString("workbench.ui.style.LabelStylePanel.height-attribute")) + ":");
        this.heightAttributeComboBox.addActionListener(this);
        this.buttonPanel.add(this.colorButton);
        this.buttonPanel.add(this.fontButton);
        FormUtils.addRowInGBL(this, 0, 0, this.labellingCheckBox);
        FormUtils.addRowInGBL((JComponent)this, 1, 0, (JComponent)this.attributeLabel, false, true);
        FormUtils.addRowInGBL((JComponent)this, 1, 30, (JComponent)this.attributeComboBox, false, false, true);
        FormUtils.addRowInGBL((JComponent)this, 2, 0, (JComponent)this.verticalAlignmentLabel, false, true);
        FormUtils.addRowInGBL((JComponent)this, 2, 30, (JComponent)this.verticalAlignmentComboBox, false, false, true);
        FormUtils.addRowInGBL((JComponent)this, 3, 0, (JComponent)this.angleLabel, false, true);
        FormUtils.addRowInGBL((JComponent)this, 3, 30, (JComponent)this.angleAttributeComboBox, false, false, true);
        FormUtils.addRowInGBL((JComponent)this, 4, 0, (JComponent)this.heightAttributeLabel, false, true);
        FormUtils.addRowInGBL((JComponent)this, 4, 30, (JComponent)this.heightAttributeComboBox, false, false, true);
        FormUtils.addRowInGBL((JComponent)this, 5, 0, (JComponent)this.heightLabel, false, true);
        FormUtils.addRowInGBL((JComponent)this, 5, 30, (JComponent)this.heightTextField, false, false, true);
        FormUtils.addRowInGBL((JComponent)this, 6, 0, (JComponent)this.glowingCheckBox, false, true);
        FormUtils.addRowInGBL((JComponent)this, 6, 30, (JComponent)this.colorGlowingButton, false, false, true);
        FormUtils.addRowInGBL((JComponent)this, 7, 0, (JComponent)this.hideOverlappingLabelsCheckBox, false, false, true);
        FormUtils.addRowInGBL((JComponent)this, 8, 0, (JComponent)this.buttonPanel, false, true, true);
        FormUtils.addRowInGBL(this, 9, 0, this.previewLabel);
        FormUtils.addRowInGBL((JComponent)this, 10, 0, (JComponent)this.previewPanel, false, true, true);
        FormUtils.addFiller(this, 20, 0);
    }

    @Override
    public Style updateStyles() {
        LabelStyle newLabelStyle = this.createLabelStyle(this.layer.getLabelStyle());
        this.layer.removeStyle(this.layer.getLabelStyle());
        this.layer.addStyle(newLabelStyle);
        return newLabelStyle;
    }

    private String getAngleAttribute() {
        if (NONE == this.angleAttributeComboBox.getSelectedItem()) {
            return "";
        }
        return (String)this.angleAttributeComboBox.getSelectedItem();
    }

    private String getHeightAttribute() {
        if (NONE == this.heightAttributeComboBox.getSelectedItem()) {
            return "";
        }
        return (String)this.heightAttributeComboBox.getSelectedItem();
    }

    private String getLabelAttribute() {
        return (String)this.attributeComboBox.getSelectedItem();
    }

    public LabelStyle createLabelStyle(LabelStyle defaultValues) {
        LabelStyle labelStyle = (LabelStyle)defaultValues.clone();
        labelStyle.setEnabled(this.labellingCheckBox.isSelected());
        Assert.isTrue((this.attributeComboBox.getSelectedIndex() != -1 ? 1 : 0) != 0);
        FeatureSchema schema = this.layer.getFeatureSchema();
        if (StringUtils.isNotEmpty((String)this.getLabelAttribute())) {
            labelStyle.setAttribute(schema.getPublicAttribute(this.getLabelAttribute()).getName());
        }
        if (StringUtils.isNotEmpty((String)this.getAngleAttribute())) {
            labelStyle.setAngleAttribute(schema.getPublicAttribute(this.getAngleAttribute()).getName());
        }
        if (StringUtils.isNotEmpty((String)this.getHeightAttribute())) {
            labelStyle.setHeightAttribute(schema.getPublicAttribute(this.getHeightAttribute()).getName());
        }
        labelStyle.setColor(this.color);
        labelStyle.setFont(this.labelFont);
        labelStyle.setScaling(false);
        labelStyle.setGlowing(this.glowingCheckBox.isSelected());
        labelStyle.setColorGlowing(this.colorGlowing);
        labelStyle.setHidingOverlappingLabels(false);
        labelStyle.setHeight(this.getLabelHeight());
        labelStyle.setVerticalAlignment((String)this.verticalAlignmentComboBox.getSelectedItem());
        return labelStyle;
    }

    private double getLabelHeight() {
        return StringUtils.isEmpty((String)this.heightTextField.getText()) ? 12.0 : Double.parseDouble(this.heightTextField.getText());
    }

    public void updateControls() {
        this.previewPanel.repaint();
        this.attributeLabel.setEnabled(this.labellingCheckBox.isSelected());
        this.angleLabel.setEnabled(this.labellingCheckBox.isSelected());
        this.heightAttributeLabel.setEnabled(this.labellingCheckBox.isSelected());
        this.attributeComboBox.setEnabled(this.labellingCheckBox.isSelected());
        this.angleAttributeComboBox.setEnabled(this.labellingCheckBox.isSelected());
        this.heightAttributeComboBox.setEnabled(this.labellingCheckBox.isSelected());
        this.colorButton.setEnabled(this.labellingCheckBox.isSelected());
        this.fontButton.setEnabled(this.labellingCheckBox.isSelected());
        this.heightLabel.setEnabled(this.labellingCheckBox.isSelected() && this.getHeightAttribute().equals(""));
        this.heightTextField.setEnabled(this.labellingCheckBox.isSelected() && this.getHeightAttribute().equals(""));
        this.glowingCheckBox.setEnabled(this.labellingCheckBox.isSelected());
        this.colorGlowingButton.setEnabled(this.labellingCheckBox.isSelected() && this.glowingCheckBox.isSelected());
        this.hideOverlappingLabelsCheckBox.setEnabled(this.labellingCheckBox.isSelected());
        this.previewLabel.setEnabled(this.labellingCheckBox.isSelected());
        this.previewPanel.setEnabled(this.labellingCheckBox.isSelected());
        this.verticalAlignmentLabel.setEnabled(this.labellingCheckBox.isSelected());
        this.verticalAlignmentComboBox.setEnabled(this.labellingCheckBox.isSelected());
    }

    private void colorButton_actionPerformed(ActionEvent e) {
        Color newColor = JColorChooser.showDialog(this, I18N.getString("workbench.ui.style.LabelStylePanel.choose-colour"), this.color);
        if (newColor == null) {
            return;
        }
        this.setColor(newColor);
        this.updateControls();
    }

    private void colorGlowingButton_actionPerformed(ActionEvent e) {
        Color newColor = JColorChooser.showDialog(this, I18N.getString("workbench.ui.style.LabelStylePanel.choose-colour"), this.color);
        if (newColor == null) {
            return;
        }
        this.setColorGlowing(newColor);
        this.updateControls();
    }

    private void fontButton_actionPerformed(ActionEvent e) {
        Font newFont = FontChooser.showDialog(this.parent, I18N.getString("workbench.ui.style.LabelStylePanel.choose-font"), this.labelFont);
        if (newFont == null) {
            return;
        }
        this.setLabelFont(newFont);
        this.updateControls();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.updateControls();
    }
}

