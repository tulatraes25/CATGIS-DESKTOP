/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.symbolizer;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.Component;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerChooserDialog;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.saig.core.gui.swing.sldeditor.util.AbstractPanelListEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.gui.swing.sldeditor.util.SymbolizerUtils;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;

public class SymbolizerListEditor
extends AbstractPanelListEditor
implements SLDEditor {
    private static final long serialVersionUID = 1L;
    protected FeatureSchema featureType;

    public SymbolizerListEditor(FeatureSchema schema, Symbolizer[] symbolizers) {
        this(schema);
        this.setSymbolizers(symbolizers);
    }

    public SymbolizerListEditor(FeatureSchema schema) {
        super(false);
        this.featureType = schema;
    }

    public void setSymbolizers(Symbolizer[] symbolizers) {
        if (symbolizers == null || symbolizers.length == 0) {
            throw new IllegalArgumentException(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerListEditor.at-least-one-symbolizer-must-be-provided"));
        }
        this.removeAllPanels();
        int i = 0;
        while (i < symbolizers.length) {
            Symbolizer s = symbolizers[i];
            this.addPanel(SymbolizerUtils.getSymbolizerName(s), SymbolizerUtils.getSymbolizerEditor(s, this.featureType));
            ++i;
        }
        this.invalidate();
    }

    public Symbolizer[] getSymbolizers() {
        Component[] cs = this.getPanels();
        Symbolizer[] symbolizers = new Symbolizer[cs.length];
        int i = 0;
        while (i < cs.length) {
            symbolizers[i] = ((SymbolizerEditor)cs[i]).getSymbolizer();
            ++i;
        }
        return symbolizers;
    }

    @Override
    protected void addButtonPressed() {
        SymbolizerChooserDialog dialog = symbolizerEditorFactory.createSymbolizerChooserDialog(this, this.featureType);
        dialog.setVisible(true);
        if (dialog.exitOk()) {
            Symbolizer s = dialog.getSelectedSymbolizer();
            this.addPanel(SymbolizerUtils.getSymbolizerName(s), SymbolizerUtils.getSymbolizerEditor(s, this.featureType));
            this.setSelectedIndex(this.getPanelCount() - 1);
            FormUtils.getWindowForComponent(this).pack();
        }
    }

    @Override
    protected void removeButtonPressed() {
        if (this.getPanelCount() <= 1) {
            return;
        }
        super.removeButtonPressed();
    }
}

