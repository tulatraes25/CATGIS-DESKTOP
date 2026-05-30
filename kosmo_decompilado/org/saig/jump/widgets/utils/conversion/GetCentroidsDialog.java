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
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.JQueryChooserPanel;

public class GetCentroidsDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private JQueryChooserPanel resultQueryChooserPanel;
    private JQueryChooserPanel errorQueryChooserPanel;
    private JCheckBox forceInsideCheckBox;
    private OKCancelPanel okCancelPanel;
    private boolean exitOk;
    private final int geometryType;

    public GetCentroidsDialog(JFrame parent, boolean modal, String layerName, int geometryType) {
        super((Frame)parent, modal);
        this.geometryType = geometryType;
        this.setTitle(I18N.getMessage(this.getClass(), "get-centroids-options-layer-{0}", new Object[]{layerName}));
        this.setContentPane(this.getMainPanel());
        this.pack();
        GUIUtil.centreOnWindow(this);
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.resultQueryChooserPanel = new JQueryChooserPanel(I18N.getString(this.getClass(), "results-layer"), I18N.getString(this.getClass(), "save-results-layer"), false);
        this.errorQueryChooserPanel = new JQueryChooserPanel(I18N.getString(this.getClass(), "errors-layer"), I18N.getString(this.getClass(), "save-errors-layer"), true);
        this.forceInsideCheckBox = new JCheckBox(I18N.getString("org.saig.jump.plugin.utils.conversion.GetCentroidsPlugIn.Force-centroids-inside-the-polygon"));
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.resultQueryChooserPanel);
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.errorQueryChooserPanel);
        if (this.geometryType == 5 || this.geometryType == 4) {
            FormUtils.addRowInGBL(mainPanel, 3, 0, this.forceInsideCheckBox);
        }
        FormUtils.addRowInGBL(mainPanel, 4, 0, this.createOKCancelPanel());
        FormUtils.addFiller(mainPanel, 5, 0);
        return mainPanel;
    }

    private JPanel createOKCancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (GetCentroidsDialog.this.okCancelPanel.wasOKPressed()) {
                    if (GetCentroidsDialog.this.isInputValid()) {
                        GetCentroidsDialog.this.exitOk = true;
                        GetCentroidsDialog.this.setVisible(false);
                    } else {
                        GetCentroidsDialog.this.exitOk = false;
                        GetCentroidsDialog.this.okCancelPanel.setOKPressed(false);
                    }
                } else {
                    GetCentroidsDialog.this.exitOk = false;
                    GetCentroidsDialog.this.okCancelPanel.setOKPressed(false);
                    GetCentroidsDialog.this.setVisible(false);
                }
            }
        });
        return this.okCancelPanel;
    }

    public boolean isInputValid() {
        if (!this.resultQueryChooserPanel.isInputValid()) {
            return false;
        }
        return this.errorQueryChooserPanel.isInputValid();
    }

    public DataSourceQuery getResultQuery() {
        return this.resultQueryChooserPanel.getDataSourceQuery();
    }

    public DataSourceQuery getErrorQuery() {
        return this.errorQueryChooserPanel.getDataSourceQuery();
    }

    public boolean isForceInsideSelected() {
        return this.forceInsideCheckBox.isSelected();
    }

    public boolean isExitOk() {
        return this.exitOk;
    }
}

