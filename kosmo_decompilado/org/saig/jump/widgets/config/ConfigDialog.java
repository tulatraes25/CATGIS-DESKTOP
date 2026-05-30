/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.l2fprod.common.swing.JButtonBar
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.config;

import com.l2fprod.common.swing.JButtonBar;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashMap;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class ConfigDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ConfigDialog.class);
    public static final String TITLE = I18N.getString("org.saig.jump.widgets.config.ConfigDialog.application-configuration");
    private static final int MINIMUM_WIDTH = 500;
    private static final int MINIMUM_HEIGHT = 250;
    private JButtonBar buttonBar;
    private ButtonGroup buttonGroup;
    private JPanel optionsPanel;
    private OKCancelPanel okCancelPanel;
    private String errorMessage = null;
    public static final String DEFAULT_SUBCATEGORY_NAME = I18N.getString("org.saig.jump.widgets.config.ConfigDialog.others");
    private HashMap categoryMap = new HashMap();
    public static final String OTHERS_MAIN_CATEGORY_NAME = I18N.getString("org.saig.jump.widgets.config.ConfigDialog.others");
    public static final Icon OTHERS_MAIN_CATEGORY_ICON = IconLoader.icon("config/package-x-generic.png");
    public static final String SCREEN_MAIN_CATEGORY_NAME = I18N.getString("org.saig.jump.widgets.config.ConfigDialog.screen");
    public static final Icon SCREEN_MAIN_CATEGORY_ICON = IconLoader.icon("config/video-display.png");
    public static final String TOOLS_MAIN_CATEGORY_NAME = I18N.getString("org.saig.jump.widgets.config.ConfigDialog.tools");
    public static final Icon TOOLS_MAIN_CATEGORY_ICON = IconLoader.icon("config/preferences-system.png");
    public static final String PATHS_MAIN_CATEGORY_NAME = I18N.getString("org.saig.jump.widgets.config.ConfigDialog.paths");
    public static final Icon PATHS_MAIN_CATEGORY_ICON = IconLoader.icon("config/folder.png");
    public static final String ADVANCED_MAIN_CATEGORY_NAME = I18N.getString("org.saig.jump.widgets.config.ConfigDialog.advanced");
    public static final Icon ADVANCED_MAIN_CATEGORY_ICON = IconLoader.icon("config/emblem-system.png");
    public static final String NET_MAIN_CATEGORY_NAME = I18N.getString(ConfigDialog.class, "net");
    public static final Icon NET_CATEGORY_ICON = IconLoader.icon("config/internet-connection.png");

    public ConfigDialog(JFrame owner, boolean modal) {
        super((Frame)owner, modal);
        this.setTitle(TITLE);
        this.initialize();
        this.initializeBasicMainCategories();
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);
        mainPanel.add((Component)this.getButtonPanel(), "North");
        mainPanel.add((Component)this.getOptionsPanel(), "Center");
        mainPanel.add((Component)this.getOkCancelPanel(), "South");
        this.setResizable(false);
    }

    private JButtonBar getButtonPanel() {
        if (this.buttonBar == null) {
            this.buttonBar = new JButtonBar(0);
            this.buttonGroup = new ButtonGroup();
        }
        return this.buttonBar;
    }

    private JPanel getOptionsPanel() {
        if (this.optionsPanel == null) {
            this.optionsPanel = new JPanel(new CardLayout());
        }
        return this.optionsPanel;
    }

    public OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ConfigDialog.this.okCancelPanel_actionPerformed(e);
                }
            });
        }
        return this.okCancelPanel;
    }

    private void okCancelPanel_actionPerformed(ActionEvent e) {
        if (this.okCancelPanel.wasOKPressed()) {
            if (this.isInputValid()) {
                for (String categoryName : this.categoryMap.keySet()) {
                    JTabbedPane currentTabbedPane = (JTabbedPane)this.categoryMap.get(categoryName);
                    int i = 0;
                    while (i < currentTabbedPane.getTabCount()) {
                        OptionsPanel pane = (OptionsPanel)currentTabbedPane.getComponentAt(i);
                        pane.okPressed();
                        ++i;
                    }
                }
                this.setVisible(false);
            } else {
                DialogFactory.showErrorDialog(this, this.errorMessage, I18N.getString("org.saig.jump.widgets.config.ConfigDialog.Configuration-error"));
            }
        } else {
            this.setVisible(false);
        }
    }

    private boolean isInputValid() {
        for (String categoryName : this.categoryMap.keySet()) {
            JTabbedPane currentTabbedPane = (JTabbedPane)this.categoryMap.get(categoryName);
            int i = 0;
            while (i < currentTabbedPane.getTabCount()) {
                OptionsPanel pane = (OptionsPanel)currentTabbedPane.getComponentAt(i);
                this.errorMessage = pane.validateInput();
                if (this.errorMessage != null) {
                    return false;
                }
                ++i;
            }
        }
        return true;
    }

    public void initializePanels() {
        for (String categoryName : this.categoryMap.keySet()) {
            JTabbedPane currentTabbedPane = (JTabbedPane)this.categoryMap.get(categoryName);
            int i = 0;
            while (i < currentTabbedPane.getTabCount()) {
                OptionsPanel pane = (OptionsPanel)currentTabbedPane.getComponentAt(i);
                pane.init();
                ++i;
            }
        }
    }

    public void addConfigPanel(OptionsPanel panel) {
        this.addConfigPanel(panel, OTHERS_MAIN_CATEGORY_NAME, panel.getName());
    }

    public void addConfigPanel(OptionsPanel panel, String mainCategoryName, String subcategoryName) {
        JTabbedPane categoryPanel;
        if (subcategoryName == null) {
            subcategoryName = DEFAULT_SUBCATEGORY_NAME;
        }
        if ((categoryPanel = (JTabbedPane)this.categoryMap.get(mainCategoryName)) == null) {
            LOGGER.warn((Object)I18N.getMessage(this.getClass(), "the-panel-{0}-was-not-found", new Object[]{mainCategoryName}));
            return;
        }
        if (categoryPanel.indexOfTab(subcategoryName) == -1) {
            categoryPanel.addTab(panel.getName(), panel.getIcon(), panel);
        } else {
            LOGGER.warn((Object)I18N.getMessage("org.saig.jump.widgets.config.ConfigDialog.the-panel-{0}-has-already-been-registered", new Object[]{panel.getName()}));
        }
    }

    public void removeConfigPanel(OptionsPanel panel) {
        this.removeConfigPanel(panel, OTHERS_MAIN_CATEGORY_NAME);
    }

    public void removeConfigPanel(OptionsPanel panel, String mainCategoryName) {
        JTabbedPane categoryTabbedPane = (JTabbedPane)this.categoryMap.get(mainCategoryName);
        if (categoryTabbedPane != null) {
            categoryTabbedPane.remove(panel);
        }
    }

    @Override
    public void pack() {
        super.pack();
        int newWidth = Math.max(this.getSize().width, 500);
        int newHeight = Math.max(this.getSize().height, 250);
        this.setSize(newWidth, newHeight);
    }

    public void addMainCategory(String mainCategoryName, Icon mainCategoryIcon) {
        JTabbedPane categoryTabbedPane = new JTabbedPane();
        categoryTabbedPane.setName(mainCategoryName);
        this.optionsPanel.add((Component)categoryTabbedPane, mainCategoryName);
        this.addMainCategoryButton(mainCategoryName, mainCategoryIcon);
        this.categoryMap.put(mainCategoryName, categoryTabbedPane);
    }

    private void addMainCategoryButton(String mainCategoryName, Icon mainCategoryIcon) {
        JToggleButton button = new JToggleButton(mainCategoryName, mainCategoryIcon);
        button.setName(mainCategoryName);
        button.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ((CardLayout)ConfigDialog.this.optionsPanel.getLayout()).show(ConfigDialog.this.optionsPanel, ((JToggleButton)e.getSource()).getName());
            }
        });
        button.setMinimumSize(new Dimension(80, 50));
        button.setPreferredSize(new Dimension(80, 50));
        this.buttonBar.add((Component)button);
        this.buttonGroup.add(button);
    }

    public void initializeBasicMainCategories() {
        this.addMainCategory(PATHS_MAIN_CATEGORY_NAME, PATHS_MAIN_CATEGORY_ICON);
        this.addMainCategory(SCREEN_MAIN_CATEGORY_NAME, SCREEN_MAIN_CATEGORY_ICON);
        this.addMainCategory(TOOLS_MAIN_CATEGORY_NAME, TOOLS_MAIN_CATEGORY_ICON);
        this.addMainCategory(NET_MAIN_CATEGORY_NAME, NET_CATEGORY_ICON);
        this.addMainCategory(ADVANCED_MAIN_CATEGORY_NAME, ADVANCED_MAIN_CATEGORY_ICON);
        this.addMainCategory(OTHERS_MAIN_CATEGORY_NAME, OTHERS_MAIN_CATEGORY_ICON);
    }

    public void refreshMainCategoryButtons() {
        Enumeration<AbstractButton> buttonEnum = this.buttonGroup.getElements();
        while (buttonEnum.hasMoreElements()) {
            JToggleButton currentButton;
            JTabbedPane currentPane = (JTabbedPane)this.categoryMap.get((currentButton = (JToggleButton)buttonEnum.nextElement()).getName());
            currentButton.setEnabled(currentPane != null && currentPane.getTabCount() > 0);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.refreshMainCategoryButtons();
        }
        super.setVisible(visible);
    }
}

