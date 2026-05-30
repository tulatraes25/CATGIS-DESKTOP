/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.style.full;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import org.saig.core.gui.swing.locale.TranslatableSelectionDialog;
import org.saig.core.gui.swing.sldeditor.style.full.BasicMetadataEditor;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.jump.lang.I18N;

public class FTSMetadataEditor
extends BasicMetadataEditor {
    private static final long serialVersionUID = 1L;
    private FeatureTypeStyle fts;

    public FTSMetadataEditor(FeatureTypeStyle fts, FeatureSchema ft) {
        super(true);
        this.fts = fts;
        this.metadataLabel.setText(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.FTSMetadataEditor.feature-type-style-metadata"));
        this.txtName.setText(this.toText(fts.getName()));
        this.txtTitle.setText(this.toText(fts.getTitle()));
        this.txaAbstract.setText(this.toText(fts.getAbstract()));
    }

    public void fillFeatureTypeStyle(FeatureTypeStyle fts) {
        fts.setName(this.txtName.getText());
        fts.setTitle(this.txtTitle.getText());
        fts.setAbstract(this.txaAbstract.getText());
    }

    @Override
    protected void langSelecButtonActionListener() {
        TranslatableSelectionDialog localeSelDia = new TranslatableSelectionDialog(JUMPWorkbench.getFrameInstance(), true, I18N.getMessage("org.saig.core.gui.swing.sldeditor.style.full.FTSMetadataEditor.Translations-for-the-feature-type-{0}-title", new Object[]{this.fts.getName()}), this.fts);
        if (localeSelDia.isOk()) {
            this.txtTitle.setText(this.fts.getTitle());
        }
    }
}

