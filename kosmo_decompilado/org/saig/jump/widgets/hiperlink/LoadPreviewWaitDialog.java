/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.hiperlink;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.saig.core.util.SwingWorker;
import org.saig.jump.lang.I18N;

public class LoadPreviewWaitDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    public static final String UNAVAILABLE_IMAGE = I18N.getString("org.saig.jump.widgets.hiperlink.LoadPreviewWaitDialog.Unavailable-image");

    public LoadPreviewWaitDialog(JDialog parent, boolean modal, final JLabel previewLabel, final File f) {
        super((Dialog)parent, modal);
        this.getContentPane().setLayout(new BorderLayout());
        this.setTitle(I18N.getString("org.saig.jump.widgets.hiperlink.LoadPreviewWaitDialog.loading-preview"));
        JLabel label = new JLabel();
        label.setIcon(IconLoader.icon("loading.gif"));
        label.setHorizontalAlignment(0);
        this.getContentPane().add((Component)label, "Center");
        this.setSize(new Dimension(200, 100));
        GUIUtil.centreOnWindow(this);
        SwingWorker worker = new SwingWorker(){

            @Override
            public Object construct() {
                BufferedImage bufi;
                block11: {
                    bufi = ImageIO.read(f);
                    previewLabel.setText("");
                    if (bufi != null) break block11;
                    previewLabel.setIcon(IconLoader.icon("Delete.jpg"));
                    previewLabel.setToolTipText(UNAVAILABLE_IMAGE);
                    return null;
                }
                try {
                    ImageIcon icon = null;
                    int maxImageHeight = Math.max(0, previewLabel.getHeight() - 2);
                    int maxImageWidth = Math.max(0, previewLabel.getWidth() - 2);
                    int imageHeight = bufi.getHeight();
                    int imageWidth = bufi.getWidth();
                    double aspectRatio = (double)imageHeight / (double)imageWidth;
                    int newIconHeight = 0;
                    int newIconWidth = 0;
                    if (aspectRatio == 1.0) {
                        int min;
                        newIconHeight = min = Math.min(maxImageHeight, maxImageWidth);
                        newIconWidth = min;
                    } else if (imageHeight > maxImageHeight) {
                        newIconHeight = maxImageHeight;
                        newIconWidth = (int)((double)maxImageHeight / aspectRatio);
                        if (newIconWidth > maxImageWidth) {
                            newIconHeight = (int)((double)maxImageWidth * aspectRatio);
                            newIconWidth = maxImageWidth;
                        }
                    } else if (imageWidth > maxImageWidth) {
                        newIconHeight = (int)((double)maxImageWidth * aspectRatio);
                        newIconWidth = maxImageWidth;
                        if (imageHeight > maxImageHeight) {
                            newIconHeight = maxImageHeight;
                            newIconWidth = (int)((double)maxImageHeight / aspectRatio);
                        }
                    }
                    icon = newIconHeight > 0 && newIconWidth > 0 ? new ImageIcon(bufi.getScaledInstance(newIconWidth, newIconHeight, 4)) : new ImageIcon(bufi);
                    if (icon != null) {
                        previewLabel.setIcon(icon);
                        previewLabel.setToolTipText(f.getAbsolutePath());
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    previewLabel.setIcon(IconLoader.icon("Delete.jpg"));
                    previewLabel.setText(UNAVAILABLE_IMAGE);
                    previewLabel.setToolTipText(f.getAbsolutePath());
                    LoadPreviewWaitDialog.this.dispose();
                }
                return null;
            }

            @Override
            public void finished() {
                LoadPreviewWaitDialog.this.closeWindow();
            }
        };
        worker.start();
    }

    public LoadPreviewWaitDialog(JFrame parent, boolean modal, final JLabel previewLabel, final File f) {
        super((Frame)parent, modal);
        this.getContentPane().setLayout(new BorderLayout());
        this.setTitle(I18N.getString("org.saig.jump.widgets.hiperlink.LoadPreviewWaitDialog.loading-preview"));
        JLabel label = new JLabel();
        label.setIcon(IconLoader.icon("loading.gif"));
        label.setHorizontalAlignment(0);
        this.getContentPane().add((Component)label, "Center");
        this.setSize(new Dimension(200, 100));
        GUIUtil.centreOnWindow(this);
        SwingWorker worker = new SwingWorker(){

            @Override
            public Object construct() {
                BufferedImage bufi;
                block13: {
                    block12: {
                        if (f != null) break block12;
                        previewLabel.setIcon(IconLoader.icon("Delete.jpg"));
                        previewLabel.setToolTipText(UNAVAILABLE_IMAGE);
                        return null;
                    }
                    bufi = ImageIO.read(f);
                    previewLabel.setText("");
                    if (bufi != null) break block13;
                    previewLabel.setIcon(IconLoader.icon("Delete.jpg"));
                    previewLabel.setToolTipText(UNAVAILABLE_IMAGE);
                    return null;
                }
                try {
                    ImageIcon icon = null;
                    int maxImageHeight = Math.max(0, previewLabel.getHeight() - 2);
                    int maxImageWidth = Math.max(0, previewLabel.getWidth() - 2);
                    int imageHeight = bufi.getHeight();
                    int imageWidth = bufi.getWidth();
                    double aspectRatio = (double)imageHeight / (double)imageWidth;
                    int newIconHeight = 0;
                    int newIconWidth = 0;
                    if (aspectRatio == 1.0) {
                        int min;
                        newIconHeight = min = Math.min(maxImageHeight, maxImageWidth);
                        newIconWidth = min;
                    } else if (imageHeight > maxImageHeight) {
                        newIconHeight = maxImageHeight;
                        newIconWidth = (int)((double)maxImageHeight / aspectRatio);
                        if (newIconWidth > maxImageWidth) {
                            newIconHeight = (int)((double)maxImageWidth * aspectRatio);
                            newIconWidth = maxImageWidth;
                        }
                    } else if (imageWidth > maxImageWidth) {
                        newIconHeight = (int)((double)maxImageWidth * aspectRatio);
                        newIconWidth = maxImageWidth;
                        if (imageHeight > maxImageHeight) {
                            newIconHeight = maxImageHeight;
                            newIconWidth = (int)((double)maxImageHeight / aspectRatio);
                        }
                    }
                    icon = newIconHeight > 0 && newIconWidth > 0 ? new ImageIcon(bufi.getScaledInstance(newIconWidth, newIconHeight, 4)) : new ImageIcon(bufi);
                    if (icon != null) {
                        previewLabel.setIcon(icon);
                        previewLabel.setToolTipText(f.getAbsolutePath());
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    previewLabel.setIcon(IconLoader.icon("Delete.jpg"));
                    previewLabel.setText(UNAVAILABLE_IMAGE);
                    previewLabel.setToolTipText(f.getAbsolutePath());
                    LoadPreviewWaitDialog.this.dispose();
                }
                return null;
            }

            @Override
            public void finished() {
                LoadPreviewWaitDialog.this.closeWindow();
            }
        };
        worker.start();
    }

    void closeWindow() {
        this.dispose();
    }
}

