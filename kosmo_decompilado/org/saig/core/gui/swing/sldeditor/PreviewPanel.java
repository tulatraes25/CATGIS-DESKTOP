/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.renderer.LegendIconMaker;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;

public class PreviewPanel
extends JComponent
implements SLDEditor {
    private static final long serialVersionUID = 1L;
    private Symbolizer symbolizer;
    private Symbolizer[] symbolizers;
    private JLabel lblPreview = new JLabel();

    public PreviewPanel() {
        this(null);
    }

    public PreviewPanel(Symbolizer symb) {
        this.setLayout(new BorderLayout());
        this.setBorder(new TitledBorder(I18N.getString("org.saig.core.gui.swing.sldeditor.PreviewPanel.preview")));
        this.add((Component)this.lblPreview, "Center");
        if (symb == null) {
            this.setSymbolizer(styleFactory.getDefaultLineSymbolizer());
        }
    }

    public Symbolizer getSymbolizer() {
        return this.symbolizer;
    }

    public void setSymbolizer(Symbolizer symb) {
        this.setSymbolizer(new Symbolizer[]{this.symbolizer});
    }

    public void setSymbolizer(Symbolizer[] symb) {
        this.symbolizers = symb;
        this.updateIcon();
        this.repaint();
    }

    private void updateIcon() {
        this.lblPreview.setIcon(LegendIconMaker.makeLegendIcon(50, 50, new Color(0, 0, 0, 0), this.symbolizers, null, false));
    }
}

