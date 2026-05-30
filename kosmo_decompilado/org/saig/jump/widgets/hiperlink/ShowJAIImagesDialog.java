/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.hiperlink;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.lang.StringUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class ShowJAIImagesDialog
extends JDialog {
    private int maxImageWidth = 0;
    private int maxImageHeight = 0;
    public static final double MAX_SCREEN_PERCENT = 0.65;
    private ImageIcon icon;
    private JPanel imagePanel;
    private JPanel buttonPanel;
    private JLabel imageLabel;
    private JButton closeButton;
    private boolean imageOk = false;

    public ShowJAIImagesDialog(JFrame parent, boolean modal, String path, String relativePathDirectory) {
        super((Frame)parent, modal);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension scrnsize = toolkit.getScreenSize();
        this.maxImageHeight = (int)(scrnsize.getHeight() * 0.65);
        this.maxImageWidth = (int)(scrnsize.getWidth() * 0.65);
        this.setTitle(I18N.getMessage("org.saig.jump.widgets.hiperlink.ShowJAIImagesDialog.hiperlink-result-{0}", new Object[]{path}));
        this.getContentPane().setLayout(new BorderLayout());
        File f = new File(path);
        if (!f.canRead()) {
            f = StringUtils.isNotEmpty((String)relativePathDirectory) ? new File(String.valueOf(relativePathDirectory) + "/" + path) : new File(String.valueOf(JUMPWorkbench.USER_DIR) + "/" + path);
        }
        if (!f.exists()) {
            DialogFactory.showErrorDialog(parent, I18N.getMessage("org.saig.jump.widgets.hiperlink.ShowJAIImagesDialog.the-file-with-path-{0}-does-not-exist", new Object[]{path}), I18N.getString("org.saig.jump.widgets.hiperlink.ShowJAIImagesDialog.error"));
            return;
        }
        BufferedImage bufi = null;
        try {
            bufi = ImageIO.read(f);
        }
        catch (IOException e) {
            DialogFactory.showErrorDialog(parent, String.valueOf(I18N.getMessage("org.saig.jump.widgets.hiperlink.ShowJAIImagesDialog.an-error-has-been-produced-while-loading-the-file-{0}", new Object[]{path})) + ".\n" + I18N.getString("org.saig.jump.widgets.hiperlink.ShowJAIImagesDialog.the-error-produced-is") + e.getMessage(), I18N.getString("org.saig.jump.widgets.hiperlink.ShowJAIImagesDialog.error"));
            e.printStackTrace();
            return;
        }
        if (bufi == null) {
            DialogFactory.showErrorDialog(parent, I18N.getMessage("org.saig.jump.widgets.hiperlink.ShowJAIImagesDialog.an-error-has-been-produced-while-loading-the-file-{0}", new Object[]{path}), I18N.getString("org.saig.jump.widgets.hiperlink.ShowJAIImagesDialog.error"));
            return;
        }
        this.getContentPane().add((Component)this.getImagePanel(bufi), "Center");
        this.getContentPane().add((Component)this.getButtonPanel(), "South");
        this.pack();
        GUIUtil.centreOnScreen(this);
        this.setResizable(false);
        this.imageOk = true;
    }

    private JPanel getImagePanel(BufferedImage bufi) {
        if (this.imagePanel == null) {
            this.imagePanel = new JPanel();
            this.imagePanel.setLayout(new BorderLayout());
            int imageHeight = bufi.getHeight();
            int imageWidth = bufi.getWidth();
            double aspectRatio = (double)imageHeight / (double)imageWidth;
            int newIconHeight = 0;
            int newIconWidth = 0;
            if (aspectRatio == 1.0) {
                int min;
                newIconHeight = min = Math.min(this.maxImageHeight, this.maxImageWidth);
                newIconWidth = min;
            } else if (imageHeight > this.maxImageHeight) {
                newIconHeight = this.maxImageHeight;
                newIconWidth = (int)((double)this.maxImageHeight / aspectRatio);
                if (newIconWidth > this.maxImageWidth) {
                    newIconHeight = (int)((double)this.maxImageWidth * aspectRatio);
                    newIconWidth = this.maxImageWidth;
                }
            } else if (imageWidth > this.maxImageWidth) {
                newIconHeight = (int)((double)this.maxImageWidth * aspectRatio);
                newIconWidth = this.maxImageWidth;
                if (imageHeight > this.maxImageHeight) {
                    newIconHeight = this.maxImageHeight;
                    newIconWidth = (int)((double)this.maxImageHeight / aspectRatio);
                }
            }
            this.icon = newIconHeight > 0 && newIconWidth > 0 ? new ImageIcon(bufi.getScaledInstance(newIconWidth, newIconHeight, 4)) : new ImageIcon(bufi);
            this.imageLabel = new JLabel();
            this.imageLabel.setHorizontalAlignment(0);
            this.imageLabel.setVerticalAlignment(0);
            this.imageLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            if (this.icon != null) {
                this.imageLabel.setIcon(this.icon);
            }
            this.imagePanel.add((Component)this.imageLabel, "Center");
        }
        return this.imagePanel;
    }

    private JPanel getButtonPanel() {
        if (this.buttonPanel == null) {
            this.buttonPanel = new JPanel();
            this.buttonPanel.setLayout(new FlowLayout());
            this.closeButton = new JButton(I18N.getString("org.saig.jump.widgets.hiperlink.ShowJAIImagesDialog.close"));
            this.closeButton.setToolTipText(I18N.getString("org.saig.jump.widgets.hiperlink.ShowJAIImagesDialog.close-hiperlink-window"));
            this.closeButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ShowJAIImagesDialog.this.dispose();
                }
            });
            this.buttonPanel.add(this.closeButton);
        }
        return this.buttonPanel;
    }

    public boolean isImageOk() {
        return this.imageOk;
    }
}

