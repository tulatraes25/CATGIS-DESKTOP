/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements.text;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.elements.BorderPanel;
import org.saig.jump.widgets.print.elements.FontPanel;
import org.saig.jump.widgets.print.elements.TextPanel;
import org.saig.jump.widgets.print.elements.TextPreviewPanel;
import org.saig.jump.widgets.print.elements.TramePanel;
import org.saig.jump.widgets.print.elements.text.GraphicText;

public class TextProperties
extends JFrame {
    private GraphicText text;
    private TextPreviewPanel textPreviewPanel;
    private JLabel previewLabel = new JLabel();
    private FontPanel fontPanel;
    private TextPanel textPanel;
    private BorderPanel borderPanel;
    private TramePanel tramePanel;
    private OKCancelPanel okCancelPanel = new OKCancelPanel();

    public TextProperties(GraphicText gt) {
        this.text = gt;
        this.setName(I18N.getString("org.saig.jump.widgets.print.elements.text.TextProperties.text-properties"));
        this.setTitle(I18N.getString("org.saig.jump.widgets.print.elements.text.TextProperties.text-properties"));
        this.setIconImage(JUMPWorkbench.APP_ICON.getImage());
        this.init(gt);
        this.borderPanel = new BorderPanel(this, this.previewLabel);
        this.tramePanel = new TramePanel(this, this.previewLabel);
        this.textPanel = new TextPanel(this.previewLabel);
        this.textPreviewPanel = new TextPreviewPanel(this.previewLabel);
        this.fontPanel = new FontPanel(this, this.previewLabel);
        this.okCancelPanel.setPreferredSize(new Dimension(400, 30));
        this.okCancelPanel.addActionListener(new OkCancelActionListener());
        this.getContentPane().setLayout(new GridBagLayout());
        this.getContentPane().add((Component)this.textPanel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.getContentPane().add((Component)this.fontPanel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.getContentPane().add((Component)this.borderPanel, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.getContentPane().add((Component)this.tramePanel, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.getContentPane().add((Component)this.textPreviewPanel, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.getContentPane().add((Component)this.okCancelPanel, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.pack();
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }

    public JLabel getPreviewLabel() {
        return this.previewLabel;
    }

    private void termine() {
        this.dispose();
    }

    private void init(GraphicText gt) {
        this.previewLabel.setText(gt.getText());
        this.previewLabel.setVerticalAlignment(gt.getVerticalAlignment());
        this.previewLabel.setHorizontalAlignment(gt.getHorizontalAlignment());
        this.previewLabel.setFont(gt.getFont());
        this.previewLabel.setForeground(gt.getFontColor());
        this.previewLabel.setBorder(gt.getBorder());
        this.previewLabel.setBackground(gt.getBackgroundColor());
        this.previewLabel.setOpaque(gt.isOpaque());
    }

    private class OkCancelActionListener
    implements ActionListener {
        private OkCancelActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (TextProperties.this.okCancelPanel.wasOKPressed()) {
                TextProperties.this.text.setText(TextProperties.this.previewLabel.getText());
                TextProperties.this.text.setVerticalAlignment(TextProperties.this.previewLabel.getVerticalAlignment());
                TextProperties.this.text.setHorizontalAlignment(TextProperties.this.previewLabel.getHorizontalAlignment());
                TextProperties.this.text.setFont(TextProperties.this.previewLabel.getFont());
                TextProperties.this.text.setFontColor(TextProperties.this.previewLabel.getForeground());
                TextProperties.this.text.setBorder(TextProperties.this.previewLabel.getBorder());
                TextProperties.this.text.setOpaque(TextProperties.this.previewLabel.isOpaque());
                TextProperties.this.text.setBackgroundColor(TextProperties.this.previewLabel.getBackground());
                TextProperties.this.text.setFontName(TextProperties.this.previewLabel.getFont().getName());
                TextProperties.this.text.setFontSize(TextProperties.this.previewLabel.getFont().getSize());
                TextProperties.this.text.setFontStyle(TextProperties.this.previewLabel.getFont().getStyle());
                TextProperties.this.text.setUnderline(this.isUnderlined(TextProperties.this.previewLabel.getText()));
            }
            TextProperties.this.termine();
        }

        private boolean isUnderlined(String text) {
            return text.toUpperCase().lastIndexOf("<HTML><U>") != -1 && text.toUpperCase().lastIndexOf("</U></HTML") != -1;
        }

        private boolean isUpperCase(String text) {
            return text.equals(text.toUpperCase());
        }
    }
}

