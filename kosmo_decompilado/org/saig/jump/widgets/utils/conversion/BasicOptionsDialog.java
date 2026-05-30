/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.utils.conversion;

import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.JQueryChooserPanel;

public class BasicOptionsDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    protected JPanel descriptionPanel;
    protected JPanel optionsPanel;
    protected JQueryChooserPanel resultsQueryChooser;
    protected JQueryChooserPanel errorsQueryChooser;
    protected boolean showErrorQueryChooser;
    protected String descriptionText;
    protected Icon descriptionImage;
    protected OKCancelPanel okCancelPanel;
    protected LayerManager layerManager;

    public BasicOptionsDialog(JFrame owner, boolean modal, String title, LayerManager layerManager, boolean showErrorQueryChooser, String description, Icon image) {
        super((Frame)owner, modal);
        this.setTitle(title);
        this.layerManager = layerManager;
        this.showErrorQueryChooser = showErrorQueryChooser;
        this.descriptionText = description;
        this.descriptionImage = image;
        this.initialize();
        this.pack();
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);
        mainPanel.add((Component)this.getOptionsPanel(), "Center");
        if (StringUtils.isNotEmpty((String)this.descriptionText) || this.descriptionImage != null) {
            mainPanel.add((Component)this.getDescriptionPanel(), "West");
        }
        mainPanel.add((Component)this.getOkCancelPanel(), "South");
        this.pack();
        int descriptionImageWidth = 0;
        if (this.descriptionImage != null) {
            descriptionImageWidth += this.descriptionImage.getIconWidth();
        }
        this.okCancelPanel.setMinimumSize(new Dimension(400 + descriptionImageWidth, this.okCancelPanel.getAcceptButton().getHeight() + 20));
        this.okCancelPanel.setPreferredSize(new Dimension(400 + descriptionImageWidth, this.okCancelPanel.getAcceptButton().getHeight() + 20));
    }

    protected JPanel getOptionsPanel() {
        if (this.optionsPanel == null) {
            this.optionsPanel = new JPanel(new GridBagLayout());
            this.optionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "options")));
            JPanel anotherOptionsPanel = this.getAnotherOptionsPanel();
            if (anotherOptionsPanel != null) {
                FormUtils.addRowInGBL(this.optionsPanel, 0, 0, anotherOptionsPanel);
            }
            FormUtils.addRowInGBL(this.optionsPanel, 1, 0, this.getResultsQueryChooser());
            if (this.showErrorQueryChooser) {
                FormUtils.addRowInGBL(this.optionsPanel, 2, 0, this.getErrorsQueryChooser());
            }
            FormUtils.addFiller(this.optionsPanel, 10, 0);
        }
        return this.optionsPanel;
    }

    protected JPanel getAnotherOptionsPanel() {
        return null;
    }

    protected JPanel getResultsQueryChooser() {
        this.resultsQueryChooser = new JQueryChooserPanel(I18N.getString(this.getClass(), "results-layer"), I18N.getString(this.getClass(), "select-target-for-results-layer"), false);
        return this.resultsQueryChooser;
    }

    protected JPanel getErrorsQueryChooser() {
        this.errorsQueryChooser = new JQueryChooserPanel(I18N.getString(this.getClass(), "errors-layer"), I18N.getString(this.getClass(), "select-target-for-errors-layer"), true);
        return this.errorsQueryChooser;
    }

    protected JPanel getDescriptionPanel() {
        if (this.descriptionPanel == null) {
            this.descriptionPanel = new JPanel(new GridBagLayout());
            if (this.descriptionImage != null) {
                JLabel imageLabel = new JLabel(this.descriptionImage);
                FormUtils.addRowInGBL(this.descriptionPanel, 0, 0, imageLabel);
            }
            if (StringUtils.isNotEmpty((String)this.descriptionText)) {
                JLabel descriptionLabel = new JLabel("<HTML><P align=\"justify\">" + this.descriptionText + "</P></HTML>");
                descriptionLabel.setMinimumSize(new Dimension(80, 50));
                descriptionLabel.setPreferredSize(new Dimension(80, 50));
                FormUtils.addRowInGBL(this.descriptionPanel, 1, 0, descriptionLabel);
            }
        }
        return this.descriptionPanel;
    }

    private OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    BasicOptionsDialog.this.setVisible(false);
                }
            });
        }
        return this.okCancelPanel;
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    public DataSourceQuery getResultQuery() {
        return this.resultsQueryChooser.getDataSourceQuery();
    }

    public DataSourceQuery getErrorQuery() {
        DataSourceQuery query = null;
        if (this.showErrorQueryChooser) {
            query = this.errorsQueryChooser.getDataSourceQuery();
        }
        return query;
    }

    public void refresh() {
    }
}

