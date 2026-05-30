/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.style;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.style.StyleEditor;
import org.saig.core.gui.swing.sldeditor.style.full.FullStyleEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Style;

public class StyleEditorChooser
extends JPanel
implements StyleEditor {
    private static final long serialVersionUID = 1L;
    private JPanel editorPanel;
    private FullStyleEditor styleEditor;
    private FeatureCollection featureSource;

    public StyleEditorChooser(FeatureCollection featureSource) {
        this(featureSource, null);
    }

    public StyleEditorChooser(FeatureCollection featureSource, Style s) {
        this.featureSource = featureSource;
        this.initialize();
        this.setStyle(s);
        this.setOpaque(true);
    }

    private void initialize() {
        this.editorPanel = new JPanel();
        this.editorPanel.setLayout(new BorderLayout());
        this.setLayout(new GridBagLayout());
        FormUtils.addFiller(this, 1, 0, this.editorPanel, false);
        this.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentResized(ComponentEvent e) {
                StyleEditorChooser.this.editorPanel.revalidate();
            }
        });
    }

    private FeatureSchema getFeatureType() {
        if (this.featureSource != null) {
            return this.featureSource.getFeatureSchema();
        }
        return null;
    }

    @Override
    public Style getStyle() {
        return this.styleEditor.getStyle();
    }

    @Override
    public void setStyle(Style s) {
        this.styleEditor = new FullStyleEditor(this.getFeatureType(), s);
        this.editorPanel.removeAll();
        this.editorPanel.add(this.styleEditor);
    }

    @Override
    public boolean canEdit(Style s) {
        return true;
    }

    public void setSelectedRule(Rule rule) {
        this.styleEditor.setSelectedRule(rule);
    }
}

