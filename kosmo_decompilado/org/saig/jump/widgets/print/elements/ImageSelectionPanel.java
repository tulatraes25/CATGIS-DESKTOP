/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements;

import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.SelectFilePanel;

public class ImageSelectionPanel
extends JPanel {
    public static final String[] IMAGE_EXTENSIONS = new String[]{"jpg", "gif", "png"};
    public static final String DESCRIPTION = I18N.getString("org.saig.jump.widgets.print.elements.ImageSelectionPanel.image-files");
    private JLabel previewLabel = null;
    private SelectFilePanel filePanel = new SelectFilePanel(DESCRIPTION, IMAGE_EXTENSIONS);
    private JPanel imagePreviewPanel = new JPanel();
    private ImageIcon selectedIcon = null;
    private double aspectRatio;

    public ImageSelectionPanel(ImageIcon icon) {
        this.initialize();
        this.initializePreviewLabel(icon);
    }

    private void initializePreviewLabel(ImageIcon icon) {
        this.selectedIcon = icon;
        this.resizeIcon();
        if (!icon.getDescription().equals("unknow.png")) {
            this.filePanel.setSelectedPath(icon.getDescription());
        }
    }

    private void resizeIcon() {
        Image image = null;
        int newOnScreenHeight = 0;
        int newOnScreenWidth = 0;
        double aspectRatio = (double)this.selectedIcon.getIconHeight() / (double)this.selectedIcon.getIconWidth();
        this.previewLabel.setSize(new Dimension(300, 300));
        if ((this.selectedIcon.getIconHeight() > this.previewLabel.getHeight() || this.selectedIcon.getIconWidth() > this.previewLabel.getWidth()) && this.previewLabel.getHeight() > 0 && this.previewLabel.getWidth() > 0) {
            if (this.previewLabel.getHeight() != 0 && this.previewLabel.getWidth() != 0) {
                if (aspectRatio == 1.0) {
                    int min;
                    newOnScreenHeight = min = Math.min(this.previewLabel.getHeight(), this.previewLabel.getWidth());
                    newOnScreenWidth = min;
                } else {
                    int maxHeight = (int)((double)this.previewLabel.getWidth() * aspectRatio);
                    int maxWidth = (int)((double)this.previewLabel.getHeight() / aspectRatio);
                    if (maxHeight > this.previewLabel.getHeight()) {
                        newOnScreenHeight = this.previewLabel.getHeight();
                        newOnScreenWidth = maxWidth;
                    } else if (maxWidth > this.previewLabel.getWidth()) {
                        newOnScreenHeight = maxHeight;
                        newOnScreenWidth = this.previewLabel.getWidth();
                    } else {
                        newOnScreenHeight = this.previewLabel.getHeight();
                        newOnScreenWidth = this.previewLabel.getWidth();
                    }
                }
                if (newOnScreenHeight > 0 && newOnScreenWidth > 0) {
                    image = this.selectedIcon.getImage().getScaledInstance(newOnScreenWidth, newOnScreenHeight, 4);
                }
                if (image != null) {
                    this.previewLabel.setIcon(new ImageIcon(image));
                }
            }
        } else {
            this.previewLabel.setIcon(this.selectedIcon);
        }
    }

    private void initialize() {
        GridBagConstraints gridBagConstraintsSelectedFile = new GridBagConstraints();
        gridBagConstraintsSelectedFile.gridx = 0;
        gridBagConstraintsSelectedFile.gridy = 0;
        gridBagConstraintsSelectedFile.weightx = 0.0;
        gridBagConstraintsSelectedFile.weighty = 0.0;
        gridBagConstraintsSelectedFile.gridheight = 1;
        gridBagConstraintsSelectedFile.gridwidth = 1;
        gridBagConstraintsSelectedFile.anchor = 10;
        gridBagConstraintsSelectedFile.insets = new Insets(10, 10, 10, 10);
        gridBagConstraintsSelectedFile.fill = 2;
        GridBagConstraints gridBagConstraintsPreview = new GridBagConstraints();
        gridBagConstraintsPreview.gridx = 0;
        gridBagConstraintsPreview.gridy = 1;
        gridBagConstraintsPreview.weightx = 1.0;
        gridBagConstraintsPreview.weighty = 1.0;
        gridBagConstraintsPreview.gridheight = 1;
        gridBagConstraintsPreview.gridwidth = 1;
        gridBagConstraintsPreview.anchor = 10;
        gridBagConstraintsPreview.insets = new Insets(10, 10, 10, 10);
        gridBagConstraintsPreview.fill = 0;
        this.previewLabel = new JLabel();
        this.previewLabel.setHorizontalAlignment(0);
        this.previewLabel.setVerticalAlignment(0);
        Dimension dim = new Dimension(300, 300);
        this.previewLabel.setMinimumSize(dim);
        this.previewLabel.setPreferredSize(dim);
        this.previewLabel.setMaximumSize(dim);
        this.previewLabel.setIcon(IconLoader.icon("Magnify.gif"));
        this.filePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(1), I18N.getString("org.saig.jump.widgets.print.elements.ImageSelectionPanel.custom-image"), 0, 0, null, null));
        this.filePanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (ImageSelectionPanel.this.filePanel.wasOKPressed()) {
                    ImageSelectionPanel.this.selectedIcon = new ImageIcon(ImageSelectionPanel.this.filePanel.getSelectedPath());
                    ImageSelectionPanel.this.resizeIcon();
                }
            }
        });
        this.imagePreviewPanel.add(this.previewLabel);
        this.imagePreviewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(1), I18N.getString("org.saig.jump.widgets.print.elements.ImageSelectionPanel.preview"), 0, 0, null, null));
        this.setLayout(new GridBagLayout());
        this.setSize(600, 400);
        this.add((Component)this.imagePreviewPanel, gridBagConstraintsPreview);
        this.add((Component)this.filePanel, gridBagConstraintsSelectedFile);
    }

    public ImageIcon getSelectedIcon() {
        return this.selectedIcon;
    }
}

