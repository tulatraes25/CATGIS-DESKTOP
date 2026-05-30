/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.style.full;

import javax.swing.JComponent;
import javax.swing.JLabel;
import org.saig.core.gui.swing.sldeditor.property.FeatureTypeChooser;
import org.saig.core.gui.swing.sldeditor.property.PropertyEditorFactory;
import org.saig.core.gui.swing.sldeditor.style.full.BasicMetadataEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.Style;
import org.saig.jump.lang.I18N;

public class StyleMetadataEditor
extends BasicMetadataEditor {
    private static final long serialVersionUID = 1L;
    private FeatureTypeChooser ftEditor;
    private JLabel lblFeatureType;

    public StyleMetadataEditor() {
        super(false);
        this.metadataLabel.setText(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.StyleMetadataEditor.style-metadata"));
        int lastRow = this.getLastRow();
        this.lblFeatureType = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.StyleMetadataEditor.default-feature-type-style"));
        this.ftEditor = PropertyEditorFactory.createPropertyEditorFactory().createFeatureTypeChooser(null);
        FormUtils.addRowInGBL(this, lastRow + 1, 0, FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.StyleMetadataEditor.default-feature-type-style")));
        FormUtils.addRowInGBL((JComponent)this, lastRow + 2, 0, this.lblFeatureType, (JComponent)this.ftEditor);
    }

    public void setStyle(Style s) {
        this.txtName.setText(this.toText(s.getName()));
        this.txtTitle.setText(this.toText(s.getTitle()));
        this.txaAbstract.setText(this.toText(s.getAbstract()));
        this.ftEditor.setFeaturesTypesStyle(s.getFeatureTypeStyles(), s.getSelectedFeatureTypeStyle().getName());
    }

    public void fillMetadata(Style s) {
        s.setName(this.txtName.getText());
        s.setTitle(this.txtTitle.getText());
        s.setAbstract(this.txaAbstract.getText());
        s.setSelectedFeatureTypeStyle(this.ftEditor.getFeatureTypeName());
    }

    @Override
    protected void langSelecButtonActionListener() {
    }
}

