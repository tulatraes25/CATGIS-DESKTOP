/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.jump.lang.I18N;

public class TextPreviewPanel
extends JPanel {
    JLabel previewLabel;

    public TextPreviewPanel(JLabel label) {
        this.previewLabel = label;
        this.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.TextPreviewPanel.text-preview")));
        this.previewLabel.setPreferredSize(new Dimension(400, 35));
        this.setLayout(new GridBagLayout());
        this.add((Component)this.previewLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 18, 0, new Insets(0, 0, 0, 0), 0, 0));
    }
}

