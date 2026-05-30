/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package es.kosmo.desktop.controllers.analysis;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.analysis.CalculateAreasAndLengthsPlugIn;
import es.kosmo.desktop.widgets.analysis.AssignValueToFieldOptionsDialog;
import es.kosmo.desktop.widgets.analysis.CalculateAreasAndLengthsOptionsDialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import org.apache.commons.lang.StringUtils;
import org.saig.core.model.feature.Attribute;
import org.saig.jump.lang.I18N;

public class CalculateAreasAndLengthsOptionsDialogController
implements ActionListener {
    public static final Set<AttributeType> VALID_ATTRIBUTE_TYPES = new HashSet<AttributeType>();
    private CalculateAreasAndLengthsOptionsDialog dialog = new CalculateAreasAndLengthsOptionsDialog(JUMPWorkbench.getFrameInstance(), true);

    static {
        VALID_ATTRIBUTE_TYPES.add(AttributeType.INTEGER);
        VALID_ATTRIBUTE_TYPES.add(AttributeType.LONG);
        VALID_ATTRIBUTE_TYPES.add(AttributeType.FLOAT);
        VALID_ATTRIBUTE_TYPES.add(AttributeType.DOUBLE);
        VALID_ATTRIBUTE_TYPES.add(AttributeType.STRING);
        VALID_ATTRIBUTE_TYPES.add(AttributeType.LONGVARCHAR);
        VALID_ATTRIBUTE_TYPES.add(AttributeType.TEXT);
        VALID_ATTRIBUTE_TYPES.add(AttributeType.VARCHAR);
        VALID_ATTRIBUTE_TYPES.add(AttributeType.TINYINT);
        VALID_ATTRIBUTE_TYPES.add(AttributeType.SMALLINT);
        VALID_ATTRIBUTE_TYPES.add(AttributeType.BIGINT);
        VALID_ATTRIBUTE_TYPES.add(AttributeType.REAL);
        VALID_ATTRIBUTE_TYPES.add(AttributeType.DECIMAL);
        VALID_ATTRIBUTE_TYPES.add(AttributeType.NUMERIC);
    }

    public CalculateAreasAndLengthsOptionsDialogController() {
        this.registerListeners();
    }

    public void registerListeners() {
        this.dialog.getOkCancelPanel().addActionListener(this);
        this.dialog.getCalculateAreaCheckBox().addActionListener(this);
        this.dialog.getCalculateLengthCheckBox().addActionListener(this);
    }

    public void show() {
        GUIUtil.centreOnScreen(this.dialog);
        this.dialog.setVisible(true);
    }

    public boolean wasOkPressed() {
        return this.dialog.wasOkPressed();
    }

    public void refresh(Layer layer, int numFeaturesSelected) {
        boolean isLineString;
        FeatureSchema schema = layer.getFeatureSchema();
        this.dialog.setTitle(String.valueOf(I18N.getMessage("org.saig.jump.widgets.utils.AbstractBasicOptionsDialog.{0}-options", new Object[]{CalculateAreasAndLengthsPlugIn.NAME})) + " - " + layer.getTitle());
        boolean bl = isLineString = schema.getGeometryType() == 3 || schema.getGeometryType() == 2;
        if (isLineString) {
            this.dialog.getCalculateLengthCheckBox().setText(CalculateAreasAndLengthsOptionsDialog.CALCULATE_LENGTH_CHECKBOX_TEXT);
            this.dialog.getLengthFieldLabel().setText(CalculateAreasAndLengthsOptionsDialog.CALCULATE_LENGTH_FIELD_TEXT);
            this.dialog.getCalculateAreaCheckBox().setVisible(false);
            this.dialog.getAreaFieldLabel().setVisible(false);
            this.dialog.getAreaFieldComboBox().setVisible(false);
        } else {
            this.dialog.getCalculateLengthCheckBox().setText(CalculateAreasAndLengthsOptionsDialog.CALCULATE_PERIMETER_CHECKBOX_TEXT);
            this.dialog.getLengthFieldLabel().setText(CalculateAreasAndLengthsOptionsDialog.CALCULATE_PERIMETER_FIELD_TEXT);
            this.dialog.getCalculateAreaCheckBox().setVisible(true);
            this.dialog.getAreaFieldLabel().setVisible(true);
            this.dialog.getAreaFieldComboBox().setVisible(true);
        }
        List<String> candidateAttrNames = this.candidateAttributeNames(schema);
        this.dialog.getAreaFieldComboBox().setModel(new DefaultComboBoxModel<String>(candidateAttrNames.toArray(new String[0])));
        this.dialog.getLengthFieldComboBox().setModel(new DefaultComboBoxModel<String>(candidateAttrNames.toArray(new String[0])));
        if (numFeaturesSelected > 0) {
            this.dialog.getApplyToSelectedOnlyRadioButton().setEnabled(true);
        } else {
            this.dialog.getApplyToSelectedOnlyRadioButton().setEnabled(false);
        }
        this.dialog.getApplyToSelectedOnlyRadioButton().setText(String.valueOf(AssignValueToFieldOptionsDialog.BASE_SELECTION_LABEL) + " (" + I18N.getMessage("es.kosmo.desktop.controllers.analysis.AssignValueToFieldOptionsDialogController.{0}-selected", new Object[]{numFeaturesSelected}) + ")");
        this.dialog.getApplyToLayerRadioButton().setSelected(true);
        this.dialog.pack();
        if (this.dialog.getTitle() != null) {
            FontMetrics fm = this.dialog.getFontMetrics(this.dialog.getFont());
            int width = fm.stringWidth(this.dialog.getTitle()) + 75;
            width = Math.max(width, this.dialog.getPreferredSize().width);
            this.dialog.setSize(new Dimension(width, this.dialog.getPreferredSize().height));
        }
    }

    public boolean isCalculateAreasSelected() {
        return this.dialog.getCalculateAreaCheckBox().isVisible() && this.dialog.getCalculateAreaCheckBox().isSelected();
    }

    public boolean isCalculateLengthsSelected() {
        return this.dialog.getCalculateLengthCheckBox().isSelected();
    }

    public String getAreasAttributeName() {
        String attrName = (String)this.dialog.getAreaFieldComboBox().getSelectedItem();
        if (attrName.equals("----------")) {
            return null;
        }
        return attrName;
    }

    public String getLengthsAttributeName() {
        String attrName = (String)this.dialog.getLengthFieldComboBox().getSelectedItem();
        if (attrName.equals("----------")) {
            return null;
        }
        return attrName;
    }

    protected List<String> candidateAttributeNames(FeatureSchema schema) {
        ArrayList<String> candidateAttributeNames = new ArrayList<String>();
        candidateAttributeNames.add("----------");
        int keyIndex = schema.getPrimaryKeyIndex();
        int i = 0;
        while (i < schema.getAttributeCount()) {
            if (i != keyIndex && this.isValidAttribute(schema.getAttributeType(i))) {
                Attribute attr = schema.getAttribute(i);
                candidateAttributeNames.add(attr.getPublicName());
            }
            ++i;
        }
        return candidateAttributeNames;
    }

    protected boolean isValidAttribute(AttributeType attributeType) {
        return VALID_ATTRIBUTE_TYPES.contains(attributeType);
    }

    public boolean isInputValid() {
        boolean ok = true;
        String errorMsg = null;
        if (this.dialog.getCalculateAreaCheckBox().isVisible() && this.dialog.getCalculateAreaCheckBox().isSelected() && StringUtils.isEmpty((String)this.getAreasAttributeName())) {
            ok = false;
            errorMsg = I18N.getString("workbench.ui.plugin.analysis.CalculateAreasAndLengthsPlugIn.you-must-select-an-attribute-from-the-list");
        }
        if (ok && this.dialog.getCalculateLengthCheckBox().isSelected() && StringUtils.isEmpty((String)this.getLengthsAttributeName())) {
            ok = false;
            errorMsg = I18N.getString("workbench.ui.plugin.analysis.CalculateAreasAndLengthsPlugIn.you-must-select-an-attribute-from-the-list");
        }
        if (ok && this.dialog.getCalculateAreaCheckBox().isVisible() && this.dialog.getCalculateAreaCheckBox().isSelected() && this.dialog.getCalculateLengthCheckBox().isSelected() && this.getAreasAttributeName().equals(this.getLengthsAttributeName())) {
            ok = false;
            errorMsg = I18N.getString("workbench.ui.plugin.analysis.CalculateAreasAndLengthsPlugIn.area-and-length-attibute-names-must-be-different");
        }
        if (!ok) {
            this.dialog.warnUser(errorMsg);
        }
        return ok;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.dialog.getOkCancelPanel())) {
            if (this.dialog.getOkCancelPanel().wasOKPressed()) {
                if (this.isInputValid()) {
                    this.dialog.setVisible(false);
                } else {
                    this.dialog.getOkCancelPanel().setOKPressed(false);
                }
            } else {
                this.dialog.getOkCancelPanel().setOKPressed(false);
                this.dialog.setVisible(false);
            }
        } else if (e.getSource().equals(this.dialog.getCalculateAreaCheckBox())) {
            boolean selected = this.dialog.getCalculateAreaCheckBox().isSelected();
            this.dialog.getAreaFieldLabel().setEnabled(selected);
            this.dialog.getAreaFieldComboBox().setEnabled(selected);
        } else if (e.getSource().equals(this.dialog.getCalculateLengthCheckBox())) {
            boolean selected = this.dialog.getCalculateLengthCheckBox().isSelected();
            this.dialog.getLengthFieldLabel().setEnabled(selected);
            this.dialog.getLengthFieldComboBox().setEnabled(selected);
        }
    }

    public boolean useSelectedOnly() {
        return this.dialog.getApplyToSelectedOnlyRadioButton().isSelected();
    }
}

