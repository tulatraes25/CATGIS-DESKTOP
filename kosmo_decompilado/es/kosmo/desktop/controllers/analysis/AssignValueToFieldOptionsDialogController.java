/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package es.kosmo.desktop.controllers.analysis;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import es.kosmo.desktop.plugins.analysis.AssignValueToFieldPlugIn;
import es.kosmo.desktop.widgets.analysis.AssignValueToFieldOptionsDialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import org.apache.commons.lang.StringUtils;
import org.saig.core.model.feature.Attribute;
import org.saig.jump.lang.I18N;

public class AssignValueToFieldOptionsDialogController
implements ActionListener {
    private AssignValueToFieldOptionsDialog dialog = new AssignValueToFieldOptionsDialog(JUMPWorkbench.getFrameInstance(), true);
    private FeatureSchema layerSchema;

    public AssignValueToFieldOptionsDialogController() {
        this.registerListeners();
    }

    public void registerListeners() {
        this.dialog.getOkCancelPanel().addActionListener(this);
    }

    public void show() {
        GUIUtil.centreOnScreen(this.dialog);
        this.dialog.setVisible(true);
    }

    public boolean wasOkPressed() {
        return this.dialog.wasOkPressed();
    }

    public void refresh(Layer layer, int numFeaturesSelected) {
        this.layerSchema = layer.getFeatureSchema();
        this.dialog.setTitle(String.valueOf(I18N.getMessage("org.saig.jump.widgets.utils.AbstractBasicOptionsDialog.{0}-options", new Object[]{AssignValueToFieldPlugIn.NAME})) + " - " + layer.getTitle());
        List<String> candidateAttrNames = this.candidateAttributeNames(this.layerSchema);
        this.dialog.getFieldNameComboBox().setModel(new DefaultComboBoxModel<String>(candidateAttrNames.toArray(new String[0])));
        this.dialog.getFieldValueTextField().setText("");
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

    public String getAttributeName() {
        String attrName = (String)this.dialog.getFieldNameComboBox().getSelectedItem();
        if (attrName.equals("----------")) {
            return null;
        }
        return attrName;
    }

    protected List<String> candidateAttributeNames(FeatureSchema schema) {
        ArrayList<String> candidateAttributeNames = new ArrayList<String>();
        candidateAttributeNames.add("----------");
        int keyIndex = schema.getPrimaryKeyIndex();
        int geomIndex = schema.getGeometryIndex();
        int i = 0;
        while (i < schema.getAttributeCount()) {
            if (i != keyIndex && i != geomIndex) {
                Attribute attr = schema.getAttribute(i);
                candidateAttributeNames.add(attr.getPublicName());
            }
            ++i;
        }
        return candidateAttributeNames;
    }

    public boolean isInputValid() {
        boolean ok = true;
        String errorMsg = null;
        if (StringUtils.isEmpty((String)this.getAttributeName())) {
            ok = false;
            errorMsg = I18N.getString("workbench.ui.plugin.analysis.CalculateAreasAndLengthsPlugIn.you-must-select-an-attribute-from-the-list");
        }
        if (ok) {
            try {
                this.getNewValue();
            }
            catch (Exception e) {
                ok = false;
                errorMsg = I18N.getMessage("org.saig.jump.widgets.utils.AssignValueToFieldDialog.The-input-value-for-the-field-{0}-is-not-valid", new Object[]{this.getAttributeName()});
            }
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
        }
    }

    public Object getNewValue() {
        Attribute attr = this.layerSchema.getPublicAttribute(this.getAttributeName());
        String selectedValue = this.dialog.getFieldValueTextField().getText().trim();
        return FeatureUtil.getGoodAttribute(attr.getType(), selectedValue);
    }

    public boolean useSelectedOnly() {
        return this.dialog.getApplyToSelectedOnlyRadioButton().isSelected();
    }
}

