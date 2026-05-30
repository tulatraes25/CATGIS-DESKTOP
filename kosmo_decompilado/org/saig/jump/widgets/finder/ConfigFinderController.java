/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.jump.widgets.finder;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.model.feature.Attribute;
import org.saig.core.util.LocaleManager;
import org.saig.jump.widgets.finder.ConfigFinderJDialog;

public class ConfigFinderController
implements ActionListener {
    private ConfigFinderJDialog dialog;

    public ConfigFinderController(ConfigFinderJDialog dialog) {
        this.dialog = dialog;
        dialog.getOkCancelPanel().addActionListener(this);
    }

    public boolean wasOkPressed() {
        return this.dialog.getOkCancelPanel().wasOKPressed();
    }

    public void refresh(Layer layer) {
        ArrayList configuredFieldNames;
        ArrayList<String> availableFieldNames;
        this.dialog.setTitle(String.valueOf(ConfigFinderJDialog.BASE_TITLE) + " - " + layer.getTitle(LocaleManager.getActiveLocale()));
        this.dialog.getOkCancelPanel().setOKPressed(false);
        List<String> attrsNames = this.getValidAttrNames(layer.getFeatureSchema());
        List<String> finderFields = layer.getFinderFields();
        if (finderFields != null) {
            availableFieldNames = new ArrayList(CollectionUtils.subtract(attrsNames, finderFields));
            configuredFieldNames = new ArrayList(CollectionUtils.retainAll(finderFields, attrsNames));
        } else {
            availableFieldNames = new ArrayList<String>(attrsNames);
            configuredFieldNames = new ArrayList();
        }
        this.dialog.getFieldsPanel().getLeftList().getModel().setItems(availableFieldNames);
        this.dialog.getFieldsPanel().getRightList().getModel().setItems(configuredFieldNames);
    }

    private List<String> getValidAttrNames(FeatureSchema schema) {
        List<String> fieldNames = schema.getAttributeNames();
        ArrayList<String> validFieldNames = new ArrayList<String>();
        for (String fieldName : fieldNames) {
            Attribute attr = schema.getAttribute(fieldName);
            if (!AttributeType.isString(attr.getType()) && !AttributeType.isNumeric(attr.getType())) continue;
            validFieldNames.add(fieldName);
        }
        return validFieldNames;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.dialog.getOkCancelPanel()) {
            if (this.dialog.getOkCancelPanel().wasOKPressed()) {
                if (this.dialog.isInputValid()) {
                    this.dialog.setVisible(false);
                }
            } else {
                this.dialog.setVisible(false);
            }
        }
    }

    public List<String> getResult() {
        return this.dialog.getFieldsPanel().getRightItems();
    }
}

