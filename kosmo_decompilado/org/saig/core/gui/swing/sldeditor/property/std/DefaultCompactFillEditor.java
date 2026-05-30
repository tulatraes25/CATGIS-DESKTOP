/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.saig.core.gui.swing.sldeditor.property.FillEditor;
import org.saig.core.gui.swing.sldeditor.property.std.FillDialog;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.gui.swing.sldeditor.util.StyleCloner;
import org.saig.core.renderer.Renderer;
import org.saig.core.styling.Fill;
import org.saig.core.styling.StyleFactory;

public class DefaultCompactFillEditor
extends FillEditor {
    private static final long serialVersionUID = 1L;
    private static Renderer renderer = Renderer.getUniqueInstance();
    protected FillButton fillButton;
    protected StyleCloner styleCloner = new StyleCloner(StyleFactory.createStyleFactory());
    protected Fill fill;
    protected FeatureSchema featureType;

    public DefaultCompactFillEditor(FeatureSchema featureType) {
        this(featureType, null);
    }

    public DefaultCompactFillEditor(FeatureSchema featureType, Fill fill) {
        this.featureType = featureType;
        this.fill = fill;
        this.fillButton = new FillButton();
        this.setLayout(new BorderLayout());
        this.add(this.fillButton);
        this.fillButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultCompactFillEditor.this.buttonPressed();
            }
        });
    }

    @Override
    public Fill getFill() {
        return this.fill;
    }

    @Override
    public void setFill(Fill fill) {
        this.fill = fill;
        this.fillButton.repaint();
    }

    protected void buttonPressed() {
        Window w = FormUtils.getWindowForComponent(this);
        Fill clonedFill = this.styleCloner.clone(this.fill);
        FillDialog dialog = w instanceof Frame ? new FillDialog((Frame)w, true, this.featureType, clonedFill) : new FillDialog((Dialog)w, true, this.featureType, clonedFill);
        dialog.setVisible(true);
        if (dialog.exitOk()) {
            this.fill = dialog.getFill();
            this.fillButton.repaint();
        }
    }

    private class FillButton
    extends JButton {
        private static final long serialVersionUID = 1L;

        private FillButton() {
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            if (DefaultCompactFillEditor.this.fill != null) {
                try {
                    renderer.applyFill((Graphics2D)g, DefaultCompactFillEditor.this.fill, null);
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
            } else {
                g.setColor(Color.LIGHT_GRAY);
                g.drawLine(0, 0, this.getWidth(), this.getHeight());
                g.drawLine(0, this.getHeight(), this.getWidth(), 0);
            }
        }
    }
}

