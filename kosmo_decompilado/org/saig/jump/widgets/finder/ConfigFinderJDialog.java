/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.jump.widgets.finder;

import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.addremove.ButtonCustomAddRemovePanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.collections.CollectionUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class ConfigFinderJDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    public static final String BASE_TITLE = I18N.getString("org.saig.jump.widgets.finder.ConfigFinderJDialog.Finder-configuration");
    protected JPanel imagePanel;
    protected ButtonCustomAddRemovePanel<String> availableFieldsPanel;
    protected OKCancelPanel okCancelPanel;

    public ConfigFinderJDialog(JFrame owner, boolean modal) {
        super((Frame)owner, modal);
        this.setTitle(BASE_TITLE);
        this.initialize();
        this.pack();
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);
        mainPanel.add((Component)this.getImageLabel(), "West");
        mainPanel.add(this.getFieldsPanel(), "Center");
        mainPanel.add((Component)this.getOkCancelPanel(), "South");
    }

    private JPanel getImageLabel() {
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        JLabel imageLabel = new JLabel(IconLoader.icon("toolImages/earth.png"));
        Dimension dim = new Dimension(190, 370);
        imageLabel.setMinimumSize(dim);
        imageLabel.setPreferredSize(dim);
        imagePanel.add((Component)imageLabel, "Center");
        return imagePanel;
    }

    public ButtonCustomAddRemovePanel<String> getFieldsPanel() {
        if (this.availableFieldsPanel == null) {
            this.availableFieldsPanel = new ButtonCustomAddRemovePanel(true);
            this.availableFieldsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.finder.ConfigFinderJDialog.Available-fields")));
            Dimension dim = new Dimension(400, 300);
            this.availableFieldsPanel.setMinimumSize(dim);
            this.availableFieldsPanel.setPreferredSize(dim);
        }
        return this.availableFieldsPanel;
    }

    public OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
        }
        return this.okCancelPanel;
    }

    public boolean isInputValid() {
        if (CollectionUtils.isEmpty(this.getFieldsPanel().getRightItems())) {
            DialogFactory.showWarningDialog(this, I18N.getString("org.saig.jump.widgets.finder.ConfigFinderJDialog.At-least-one-search-field-must-be-selected"), this.getTitle());
            return false;
        }
        return true;
    }
}

