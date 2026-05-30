/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultPointSymbolizerEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;

public class SelectVertexSymbolizer
extends JDialog {
    private static final long serialVersionUID = 1L;
    public static final String TITLE = I18N.getString("org.saig.jump.widgets.config.SelectVertexSymbolizer.select-default-vertex-style");
    private DefaultPointSymbolizerEditor pointEditor;
    private OKCancelPanel okCancelPanel;

    public SelectVertexSymbolizer(JFrame parent, boolean modal) {
        super((Frame)parent, modal);
        this.setTitle(TITLE);
        this.initialize();
        this.pack();
    }

    public SelectVertexSymbolizer(JDialog parent, boolean modal) {
        super((Dialog)parent, modal);
        this.setTitle(TITLE);
        this.initialize();
        this.pack();
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.setContentPane(mainPanel);
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.getPointEditor());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getOkCancelPanel());
    }

    public DefaultPointSymbolizerEditor getPointEditor() {
        if (this.pointEditor == null) {
            this.pointEditor = new DefaultPointSymbolizerEditor(new FeatureSchema());
        }
        return this.pointEditor;
    }

    public OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    SelectVertexSymbolizer.this.setVisible(false);
                }
            });
        }
        return this.okCancelPanel;
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    public Symbolizer getSymbolizer() {
        return this.pointEditor.getSymbolizer();
    }

    public void setSymbolizer(Symbolizer symbolizer) {
        this.pointEditor.setSymbolizer(symbolizer);
    }
}

