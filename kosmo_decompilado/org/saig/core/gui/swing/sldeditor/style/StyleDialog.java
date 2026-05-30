/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.style;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.style.StyleEditorChooser;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.gui.swing.sldeditor.util.WindowMinSizer;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Style;
import org.saig.jump.lang.I18N;

public class StyleDialog
extends JDialog
implements ActionListener {
    private static final long serialVersionUID = 1L;
    private WindowMinSizer minSizer;
    private StyleEditorChooser editorChooser;
    private OKCancelPanel okCancelPanel;
    private static PlugInContext context = null;

    public StyleDialog(Dialog parent, FeatureCollection fs, Style s) {
        super(parent, true);
        this.initialize(fs, s);
    }

    public StyleDialog(Frame parent, FeatureCollection fs, Style s) {
        super(parent, true);
        this.initialize(fs, s);
    }

    public static StyleDialog createDialog(Component parent, FeatureCollection fs, Style s) {
        Window w = FormUtils.getWindowForComponent(parent);
        if (w instanceof Dialog) {
            return new StyleDialog((Dialog)w, fs, s);
        }
        return new StyleDialog((Frame)w, fs, s);
    }

    private void initialize(FeatureCollection fs, Style s) {
        this.editorChooser = new StyleEditorChooser(fs, s);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(this.editorChooser);
        panel.add((Component)this.getOkCancelPanel(), "South");
        this.setContentPane(panel);
        this.setTitle(I18N.getString("org.saig.core.gui.swing.sldeditor.style.StyleDialog.edit-map-layer-style"));
        this.pack();
        this.minSizer = new WindowMinSizer(this);
    }

    private OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(this);
        }
        return this.okCancelPanel;
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    public Style getStyle() {
        return this.editorChooser.getStyle();
    }

    public static PlugInContext getMapContext() {
        return context;
    }

    public void setMapContext(PlugInContext context) {
        StyleDialog.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.okCancelPanel) {
            this.setVisible(false);
            this.dispose();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.okCancelPanel.setOKPressed(false);
        }
        super.setVisible(visible);
    }

    public void setSelectedRule(Rule rule) {
        this.editorChooser.setSelectedRule(rule);
    }
}

