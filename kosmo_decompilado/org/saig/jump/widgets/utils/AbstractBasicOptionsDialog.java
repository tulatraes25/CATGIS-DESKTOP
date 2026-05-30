/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.utils;

import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.JQueryChooserPanel;

public class AbstractBasicOptionsDialog
extends JDialog
implements ActionListener {
    private static final long serialVersionUID = 1L;
    protected JQueryChooserPanel queryChooser;
    protected OKCancelPanel okCancelPanel;
    protected boolean okPressed = false;
    protected String descriptionText;
    protected String imagePath;

    public AbstractBasicOptionsDialog(JFrame parent, boolean modal, String toolName, String toolDescription, String toolImagePath) {
        super((Frame)parent, modal);
        this.descriptionText = toolDescription;
        this.imagePath = toolImagePath;
        this.setDialogTitle(toolName);
        this.initialize();
        this.pack();
    }

    protected void setDialogTitle(String toolName) {
        this.setTitle(I18N.getMessage("org.saig.jump.widgets.utils.AbstractBasicOptionsDialog.{0}-options", new Object[]{toolName}));
    }

    protected void initialize() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);
        if (StringUtils.isNotEmpty((String)this.descriptionText)) {
            mainPanel.add((Component)this.getDescriptionPanel(), "North");
        }
        if (StringUtils.isNotEmpty((String)this.imagePath)) {
            mainPanel.add((Component)this.getImageLabel(), "West");
        }
        mainPanel.add((Component)this.getCenterPanel(), "Center");
        mainPanel.add((Component)this.getOkCancelPanel(), "South");
    }

    private JPanel getDescriptionPanel() {
        JPanel descriptionPanel = new JPanel(new GridBagLayout());
        JLabel descriptionLabel = new JLabel(this.descriptionText);
        FormUtils.addRowInGBL((JComponent)descriptionPanel, 0, 0, (JComponent)descriptionLabel, true, true);
        return descriptionPanel;
    }

    private JLabel getImageLabel() {
        ImageIcon icon = IconLoader.icon(this.imagePath);
        JLabel photoLabel = new JLabel(icon);
        Dimension dim = new Dimension(icon.getIconWidth(), icon.getIconHeight());
        photoLabel.setMinimumSize(dim);
        photoLabel.setPreferredSize(dim);
        return photoLabel;
    }

    protected JPanel getCenterPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        this.queryChooser = new JQueryChooserPanel(I18N.getString("org.saig.jump.widgets.utils.AbstractBasicOptionsDialog.Results-layer"), I18N.getString("org.saig.jump.widgets.utils.AbstractBasicOptionsDialog.Save-results-layer"), false);
        FormUtils.addRowInGBL(centerPanel, 1, 0, this.queryChooser);
        FormUtils.addFiller(centerPanel, 2, 0);
        return centerPanel;
    }

    protected OKCancelPanel getOkCancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.addActionListener(this);
        return this.okCancelPanel;
    }

    public boolean isInputValid() {
        return this.queryChooser.isInputValid();
    }

    public DataSourceQuery getResultsQuery() {
        return this.queryChooser.getDataSourceQuery();
    }

    public boolean wasOkPressed() {
        return this.okPressed;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.okPressed = false;
        }
        super.setVisible(visible);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.okCancelPanel)) {
            if (this.okCancelPanel.wasOKPressed()) {
                if (this.isInputValid()) {
                    this.okPressed = true;
                    this.setVisible(false);
                } else {
                    this.okPressed = false;
                    this.okCancelPanel.setOKPressed(false);
                }
            } else {
                this.okPressed = false;
                this.okCancelPanel.setOKPressed(false);
                this.setVisible(false);
            }
        }
    }
}

