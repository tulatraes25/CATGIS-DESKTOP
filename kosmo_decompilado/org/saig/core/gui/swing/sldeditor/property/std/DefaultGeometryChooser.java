/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.BorderLayout;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.saig.core.gui.swing.sldeditor.property.GeometryChooser;

public class DefaultGeometryChooser
extends GeometryChooser {
    private static final long serialVersionUID = 1L;
    private JComboBox cmbGeomProperties;
    private String defaultGeometryField = null;
    private Vector<String> geomProperties;

    public DefaultGeometryChooser() {
        this(null);
    }

    public DefaultGeometryChooser(FeatureSchema type) {
        this.setLayout(new BorderLayout());
        this.cmbGeomProperties = new JComboBox();
        this.add(this.cmbGeomProperties);
        this.geomProperties = this.getGeomProperties(type);
        if (type == null) {
            this.cmbGeomProperties.setEnabled(false);
        } else {
            this.defaultGeometryField = type.getAttributeName(type.getGeometryIndex());
            this.cmbGeomProperties.setModel(new DefaultComboBoxModel<String>(this.geomProperties));
            this.cmbGeomProperties.setSelectedItem(type.getAttributeName(type.getGeometryIndex()));
        }
    }

    private Vector<String> getGeomProperties(FeatureSchema type) {
        Vector<String> names = new Vector<String>();
        if (type != null) {
            int i = 0;
            while (i < type.getAttributeCount()) {
                AttributeType at = type.getAttributeType(i);
                if (Geometry.class.isAssignableFrom(at.toJavaClass())) {
                    names.add(type.getAttributeName(i));
                }
                ++i;
            }
        }
        return names;
    }

    @Override
    public int getGeomPropertiesCount() {
        return this.geomProperties.size();
    }

    @Override
    public String getSelectedName() {
        return (String)this.cmbGeomProperties.getSelectedItem();
    }

    @Override
    public void setSelectedName(String name) {
        if (name == null) {
            this.cmbGeomProperties.setSelectedItem(this.defaultGeometryField);
        } else {
            this.cmbGeomProperties.setSelectedItem(name);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.cmbGeomProperties.setEnabled(enabled);
    }
}

