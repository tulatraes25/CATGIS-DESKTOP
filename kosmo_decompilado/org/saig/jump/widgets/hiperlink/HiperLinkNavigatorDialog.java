/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.hiperlink;

import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.utils.DesktopUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.hiperlink.HiperLinkValue;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.utils.hiperlink.HiperLinkCursorTool;
import org.saig.jump.widgets.hiperlink.LoadPreviewWaitDialog;
import org.saig.jump.widgets.hiperlink.ShowJaiImagesWaitDialog;
import org.saig.jump.widgets.util.DialogFactory;

public class HiperLinkNavigatorDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    public static int ONLY_ONE_LAYER_MODE = 1;
    public static int SELECTED_LAYERS_MODE = 2;
    public static int VISIBLE_LAYERS_MODE = 3;
    private static final Logger LOGGER = Logger.getLogger(HiperLinkNavigatorDialog.class);
    private String basicTitle;
    private HiperLinkValue[] hiperlinks;
    private JTextArea hiperlinkTextArea;
    private JTextArea descriptionTextArea;
    private JLabel imageLabel;
    private JButton initButton;
    private JButton endButton;
    private JButton nextButton;
    private JButton prevButton;
    private JButton openButton;
    private JButton openAllButton;
    private JButton closeButton;
    private int index = 0;
    private boolean showImagesInInternalViewer;
    private String relativePathDirectory;

    public HiperLinkNavigatorDialog(JFrame parent, boolean modal, String layerName, Collection<HiperLinkValue> features, int mode, boolean showImagesInInternalViewer, String relativePathDirectory) {
        super((Frame)parent, modal);
        this.hiperlinks = new HiperLinkValue[features.size()];
        this.showImagesInInternalViewer = showImagesInInternalViewer;
        this.relativePathDirectory = relativePathDirectory;
        features.toArray(this.hiperlinks);
        this.basicTitle = mode == ONLY_ONE_LAYER_MODE ? I18N.getMessage("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.{0}-hyperlinks-founded-for-the-layer-{1}", new Object[]{new Integer(features.size()), layerName}) : (mode == SELECTED_LAYERS_MODE ? I18N.getMessage("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.{0}-hyperlinks-founded-for-the-selected-layers", new Object[]{new Integer(features.size())}) : I18N.getMessage("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.{0}-hyperlinks-founded-for-the-visible-layers", new Object[]{new Integer(features.size())}));
        this.setTitle(this.generateTitle());
        this.initialize();
        this.pack();
        this.loadHiperLink(this.index);
    }

    private String generateTitle() {
        return String.valueOf(this.basicTitle) + " (" + (this.index + 1) + " - " + this.hiperlinks.length + ")";
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.setContentPane(mainPanel);
        JPanel hiperlinkPanel = new JPanel(new BorderLayout());
        hiperlinkPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.hiperlink-path")));
        this.hiperlinkTextArea = new JTextArea(2, 70);
        this.hiperlinkTextArea.setEditable(false);
        this.hiperlinkTextArea.setFont(new JLabel().getFont());
        hiperlinkPanel.add((Component)this.hiperlinkTextArea, "Center");
        JPanel descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.Description")));
        this.descriptionTextArea = new JTextArea(3, 70);
        this.descriptionTextArea.setEditable(false);
        this.descriptionTextArea.setFont(new JLabel().getFont());
        descriptionPanel.add((Component)this.descriptionTextArea, "Center");
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.preview-image")));
        this.imageLabel = new JLabel();
        this.imageLabel.setBorder(BorderFactory.createLineBorder(Color.red, 2));
        this.imageLabel.setMinimumSize(new Dimension(450, 260));
        this.imageLabel.setPreferredSize(new Dimension(450, 260));
        this.imageLabel.setHorizontalAlignment(0);
        this.imageLabel.setVerticalAlignment(0);
        imagePanel.add((Component)this.imageLabel, "Center");
        FormUtils.addRowInGBL(mainPanel, 0, 0, hiperlinkPanel);
        FormUtils.addRowInGBL(mainPanel, 1, 0, descriptionPanel);
        FormUtils.addRowInGBL(mainPanel, 2, 0, imagePanel);
        FormUtils.addRowInGBL(mainPanel, 3, 0, this.getPanelButton());
        FormUtils.addFiller(mainPanel, 4, 0);
    }

    private JPanel getPanelButton() {
        JPanel panelButton = new JPanel(new FlowLayout());
        this.initButton = new JButton(IconLoader.icon("Start.gif"));
        this.initButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                HiperLinkNavigatorDialog.this.index = 0;
                HiperLinkNavigatorDialog.this.loadHiperLink(HiperLinkNavigatorDialog.this.index);
                HiperLinkNavigatorDialog.this.refreshPanel();
            }
        });
        this.initButton.setToolTipText(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.Start"));
        panelButton.add(this.initButton);
        this.prevButton = new JButton(IconLoader.icon("Prev.gif"));
        this.prevButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (HiperLinkNavigatorDialog.this.index == 0) {
                    return;
                }
                HiperLinkNavigatorDialog hiperLinkNavigatorDialog = HiperLinkNavigatorDialog.this;
                hiperLinkNavigatorDialog.index = hiperLinkNavigatorDialog.index - 1;
                HiperLinkNavigatorDialog.this.loadHiperLink(HiperLinkNavigatorDialog.this.index);
                HiperLinkNavigatorDialog.this.refreshPanel();
            }
        });
        this.prevButton.setToolTipText(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.Previous"));
        panelButton.add(this.prevButton);
        this.nextButton = new JButton(IconLoader.icon("Next.gif"));
        this.nextButton.setToolTipText(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.Next"));
        this.nextButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (HiperLinkNavigatorDialog.this.index == HiperLinkNavigatorDialog.this.hiperlinks.length - 1) {
                    return;
                }
                HiperLinkNavigatorDialog hiperLinkNavigatorDialog = HiperLinkNavigatorDialog.this;
                hiperLinkNavigatorDialog.index = hiperLinkNavigatorDialog.index + 1;
                HiperLinkNavigatorDialog.this.loadHiperLink(HiperLinkNavigatorDialog.this.index);
                HiperLinkNavigatorDialog.this.refreshPanel();
            }
        });
        panelButton.add(this.nextButton);
        this.endButton = new JButton(IconLoader.icon("End.gif"));
        this.endButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                HiperLinkNavigatorDialog.this.index = HiperLinkNavigatorDialog.this.hiperlinks.length - 1;
                HiperLinkNavigatorDialog.this.loadHiperLink(HiperLinkNavigatorDialog.this.index);
                HiperLinkNavigatorDialog.this.refreshPanel();
            }
        });
        this.endButton.setToolTipText(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.End"));
        panelButton.add(this.endButton);
        panelButton.add(new JLabel("     "));
        this.openButton = new JButton(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.Open"));
        this.openButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                HiperLinkNavigatorDialog.this.openHiperLinkWindow(HiperLinkNavigatorDialog.this.index);
            }
        });
        panelButton.add(this.openButton);
        this.openAllButton = new JButton(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.Open-all"));
        this.openAllButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                int i = 0;
                while (i < HiperLinkNavigatorDialog.this.hiperlinks.length) {
                    HiperLinkNavigatorDialog.this.openHiperLinkWindow(i);
                    ++i;
                }
            }
        });
        panelButton.add(this.openAllButton);
        this.closeButton = new JButton(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.Close"));
        this.closeButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                HiperLinkNavigatorDialog.this.setVisible(false);
                HiperLinkNavigatorDialog.this.dispose();
            }
        });
        panelButton.add(this.closeButton);
        this.refreshPanel();
        return panelButton;
    }

    private void refreshPanel() {
        this.initButton.setEnabled(this.index != 0);
        this.endButton.setEnabled(this.index != this.hiperlinks.length - 1);
        this.nextButton.setEnabled(this.index != this.hiperlinks.length - 1);
        this.prevButton.setEnabled(this.index != 0);
        this.setTitle(this.generateTitle());
    }

    private void loadHiperLink(int index) {
        HiperLinkValue hiperLinkValue = this.hiperlinks[index];
        this.hiperlinkTextArea.setText(hiperLinkValue.getValue());
        this.descriptionTextArea.setText(hiperLinkValue.getDescription());
        this.loadPreview(hiperLinkValue.getValue());
    }

    private void loadPreview(String path) {
        File f = new File(path);
        if (!f.canRead()) {
            f = StringUtils.isNotEmpty((String)this.relativePathDirectory) ? new File(String.valueOf(this.relativePathDirectory) + "/" + path) : new File(String.valueOf(JUMPWorkbench.USER_DIR) + "/" + path);
        }
        if (!f.exists()) {
            this.imageLabel.setIcon(IconLoader.icon("Delete.jpg"));
            return;
        }
        new LoadPreviewWaitDialog(this, true, this.imageLabel, f).setVisible(true);
    }

    private void openHiperLinkWindow(int index) {
        HiperLinkValue selectedhiperLink = this.hiperlinks[index];
        HiperLinkNavigatorDialog.openHiperLinkWindow(selectedhiperLink, this.showImagesInInternalViewer, null);
    }

    public static void openHiperLinkWindow(String path, String description, boolean showImagesInInternalViewer) {
        HiperLinkValue hlv = new HiperLinkValue(description, path);
        HiperLinkNavigatorDialog.openHiperLinkWindow(hlv, showImagesInInternalViewer, null);
    }

    public static void openHiperLinkWindow(String path, String description) {
        HiperLinkValue hlv = new HiperLinkValue(description, path);
        HiperLinkNavigatorDialog.openHiperLinkWindow(hlv, true, null);
    }

    public static void openHiperLinkWindow(HiperLinkValue hiperLinkValue) {
        HiperLinkNavigatorDialog.openHiperLinkWindow(hiperLinkValue, true, null);
    }

    public static void openHiperLinkWindow(HiperLinkValue hiperLinkValue, boolean showImagesInInternalViewer, String relativePathDirectory) {
        String value = hiperLinkValue.getValue();
        if (StringUtils.isEmpty((String)value)) {
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.Selected-item-has-a-null-hyperlink"), I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.Null-hyperlink"));
            return;
        }
        String ext = FileUtil.getExtension(value);
        if (!value.toLowerCase().startsWith("http") && HiperLinkCursorTool.isValidImage(value) && showImagesInInternalViewer) {
            new ShowJaiImagesWaitDialog(JUMPWorkbench.getFrameInstance(), false, value, relativePathDirectory).setVisible(true);
            return;
        }
        if (ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".htm") || value.toLowerCase().startsWith("http")) {
            URL url = null;
            try {
                url = HiperLinkNavigatorDialog.buildURLFromValue(value);
                LOGGER.info((Object)I18N.getMessage("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.url-path-generated-for-the-hiperlink-{0}", new Object[]{url}));
                DesktopUtils.browse(url);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("org.saig.jump.plugin.utils.hiperlink.HiperLinkCursorTool.the-hiperlink-{0}-can-not-be-read", new Object[]{url}), I18N.getString("org.saig.jump.plugin.utils.hiperlink.HiperLinkCursorTool.hiperlink"));
            }
        } else {
            File file = new File(value);
            if (!file.canRead()) {
                file = StringUtils.isNotEmpty((String)relativePathDirectory) ? new File(String.valueOf(relativePathDirectory) + "/" + value) : new File(String.valueOf(JUMPWorkbench.USER_DIR) + "/" + value);
            }
            LOGGER.info((Object)I18N.getMessage("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.hiperlink-file-to-open-{0}", new Object[]{file.getAbsolutePath()}));
            if (file.canRead()) {
                try {
                    DesktopUtils.open(file);
                }
                catch (Exception e) {
                    DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getMessage("org.saig.jump.plugin.utils.hiperlink.HiperLinkCursorTool.the-hiperlink-{0}-can-not-be-read", new Object[]{value})) + ". " + I18N.getMessage("org.saig.jump.widgets.hiperlink.HiperLinkNavigatorDialog.there-is-not-any-program-associated-to-the-file-extension-{0}", new Object[]{ext}), I18N.getString("org.saig.jump.plugin.utils.hiperlink.HiperLinkCursorTool.hiperlink"));
                }
            } else {
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("org.saig.jump.plugin.utils.hiperlink.HiperLinkCursorTool.the-hiperlink-{0}-can-not-be-read", new Object[]{value}), I18N.getString("org.saig.jump.plugin.utils.hiperlink.HiperLinkCursorTool.hiperlink"));
            }
        }
    }

    private static URL buildURLFromValue(String value) throws MalformedURLException {
        URL url = null;
        if (value.toLowerCase().startsWith("http")) {
            url = new URL(value);
        } else {
            File file = new File(value);
            URI uri = file.toURI();
            url = uri.toURL();
        }
        return url;
    }
}

