/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.utils.conversion;

import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.JQueryChooserPanel;

public class GetLinesFromPointsDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private OKCancelPanel okCancelPanel;
    private JQueryChooserPanel queryChooserResults;
    private JQueryChooserPanel queryChooserErrors;
    private JComboBox atributoLineaComboBox;
    private JComboBox atributoOrdenComboBox;
    private boolean exitOk = false;
    private final Vector<String> attrNames;

    public GetLinesFromPointsDialog(JFrame parent, boolean modal, Vector<String> attrNames) {
        super((Frame)parent, modal);
        this.attrNames = attrNames;
        this.setTitle(I18N.getString(this.getClass(), "get-lines-from-points-options"));
        this.setContentPane(this.getMainPanel());
        this.pack();
        GUIUtil.centreOnWindow(this);
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.atributoLineaComboBox = new JComboBox<String>(this.attrNames);
        this.atributoLineaComboBox.setSelectedIndex(0);
        this.atributoOrdenComboBox = new JComboBox<String>(this.attrNames);
        this.atributoOrdenComboBox.setSelectedIndex(0);
        this.queryChooserResults = new JQueryChooserPanel(I18N.getString(this.getClass(), "results-layer"), I18N.getString(this.getClass(), "save-results-layer"), false);
        this.queryChooserErrors = new JQueryChooserPanel(I18N.getString(this.getClass(), "errors-layer"), I18N.getString(this.getClass(), "save-errors-layer"), false);
        FormUtils.addRowInGBL(mainPanel, 1, 0, new JLabel(I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Choose-the-attribute-that-shows-the-line-wich-the-points-belongs-to")));
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.atributoLineaComboBox);
        FormUtils.addRowInGBL(mainPanel, 3, 0, new JLabel(I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Choose-the-attribute-that-shows-the-order-of-the-points-in-the-line")));
        FormUtils.addRowInGBL(mainPanel, 4, 0, this.atributoOrdenComboBox);
        FormUtils.addRowInGBL(mainPanel, 5, 0, this.queryChooserResults);
        FormUtils.addRowInGBL(mainPanel, 6, 0, this.queryChooserErrors);
        FormUtils.addRowInGBL(mainPanel, 7, 0, this.createOKCancelPanel());
        FormUtils.addFiller(mainPanel, 8, 0);
        return mainPanel;
    }

    private JComponent createOKCancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (GetLinesFromPointsDialog.this.okCancelPanel.wasOKPressed()) {
                    if (GetLinesFromPointsDialog.this.isInputValid()) {
                        GetLinesFromPointsDialog.this.exitOk = true;
                        GetLinesFromPointsDialog.this.setVisible(false);
                    } else {
                        GetLinesFromPointsDialog.this.exitOk = false;
                        GetLinesFromPointsDialog.this.okCancelPanel.setOKPressed(false);
                    }
                } else {
                    GetLinesFromPointsDialog.this.exitOk = false;
                    GetLinesFromPointsDialog.this.okCancelPanel.setOKPressed(false);
                    GetLinesFromPointsDialog.this.setVisible(false);
                }
            }
        });
        return this.okCancelPanel;
    }

    public boolean isInputValid() {
        if (this.atributoLineaComboBox.getSelectedItem() == null) {
            DialogFactory.showInformationDialog(this, I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Choose-the-attribute-that-shows-the-line-wich-the-points-belongs-to"), I18N.getString(this.getClass(), "warning"));
            this.atributoLineaComboBox.requestFocus();
            return false;
        }
        if (this.atributoOrdenComboBox.getSelectedItem() == null) {
            DialogFactory.showInformationDialog(this, I18N.getString("org.saig.jump.plugin.utils.conversion.GetLinesFromPointsPlugIn.Choose-the-attribute-that-shows-the-order-of-the-points-in-the-line"), I18N.getString(this.getClass(), "warning"));
            this.atributoOrdenComboBox.requestFocus();
            return false;
        }
        if (!this.queryChooserResults.isInputValid()) {
            return false;
        }
        return this.queryChooserErrors.isInputValid();
    }

    public DataSourceQuery getQueryResults() {
        return this.queryChooserResults.getDataSourceQuery();
    }

    public DataSourceQuery getQueryErrors() {
        return this.queryChooserErrors.getDataSourceQuery();
    }

    public boolean isExitOk() {
        return this.exitOk;
    }

    public String getSelectedLineAttribute() {
        return (String)this.atributoLineaComboBox.getSelectedItem();
    }

    public String getSelectedOrderAttribute() {
        return (String)this.atributoOrdenComboBox.getSelectedItem();
    }
}

