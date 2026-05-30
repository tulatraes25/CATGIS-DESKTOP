/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import java.awt.BorderLayout;
import java.util.Arrays;
import javax.swing.JComboBox;
import org.saig.core.gui.swing.sldeditor.property.FeatureTypeChooser;
import org.saig.core.styling.FeatureTypeStyle;

public class DefaultFeatureTypeChooser
extends FeatureTypeChooser {
    private static final long serialVersionUID = 1L;
    protected JComboBox cmbNames;
    protected String[] typeNames;

    public DefaultFeatureTypeChooser(FeatureTypeStyle[] featureTypeStyles) {
        if (featureTypeStyles != null) {
            this.typeNames = new String[featureTypeStyles.length];
            int i = 0;
            while (i < featureTypeStyles.length) {
                this.typeNames[i] = featureTypeStyles[i].getName();
                ++i;
            }
        } else {
            this.typeNames = new String[]{""};
        }
        Arrays.sort(this.typeNames);
        this.cmbNames = new JComboBox<String>(this.typeNames);
        this.cmbNames.setSelectedIndex(0);
        this.cmbNames.setEditable(false);
        this.setLayout(new BorderLayout());
        this.add(this.cmbNames);
    }

    @Override
    public void setFeatureTypeName(String typeName) {
        int i = 0;
        while (i < this.typeNames.length) {
            if (this.typeNames[i].equalsIgnoreCase(typeName)) {
                this.cmbNames.setSelectedIndex(i);
            }
            ++i;
        }
    }

    @Override
    public String getFeatureTypeName() {
        return (String)this.cmbNames.getSelectedItem();
    }

    @Override
    public void setFeaturesTypesStyle(FeatureTypeStyle[] featureTypeStyles, String selectedFeatureTypeStyleName) {
        if (featureTypeStyles != null) {
            this.typeNames = new String[featureTypeStyles.length];
            int i = 0;
            while (i < featureTypeStyles.length) {
                this.typeNames[i] = featureTypeStyles[i].getName();
                ++i;
            }
        } else {
            this.typeNames = new String[]{""};
        }
        Arrays.sort(this.typeNames);
        this.remove(this.cmbNames);
        this.cmbNames = new JComboBox<String>(this.typeNames);
        this.cmbNames.setSelectedItem(selectedFeatureTypeStyleName);
        this.cmbNames.setEditable(false);
        this.setLayout(new BorderLayout());
        this.add(this.cmbNames);
    }
}

