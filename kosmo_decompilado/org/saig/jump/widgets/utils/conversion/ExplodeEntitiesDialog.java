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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.JQueryChooserPanel;

public class ExplodeEntitiesDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private OKCancelPanel okCancelPanel;
    private JQueryChooserPanel queryChooserSolucion;
    private boolean exitOk = false;

    public ExplodeEntitiesDialog(JFrame parent, boolean modal) {
        super((Frame)parent, modal);
        this.setTitle(I18N.getString(this.getClass(), "explode-entities-options"));
        this.setContentPane(this.getMainPanel());
        this.pack();
        GUIUtil.centreOnWindow(this);
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.queryChooserSolucion = new JQueryChooserPanel(I18N.getString(this.getClass(), "results-layer"), I18N.getString(this.getClass(), "save-results-layer"), false);
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.queryChooserSolucion);
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.createOKCancelPanel());
        FormUtils.addFiller(mainPanel, 3, 0);
        return mainPanel;
    }

    private JComponent createOKCancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (ExplodeEntitiesDialog.this.okCancelPanel.wasOKPressed()) {
                    if (ExplodeEntitiesDialog.this.isInputValid()) {
                        ExplodeEntitiesDialog.this.exitOk = true;
                        ExplodeEntitiesDialog.this.setVisible(false);
                    } else {
                        ExplodeEntitiesDialog.this.exitOk = false;
                        ExplodeEntitiesDialog.this.okCancelPanel.setOKPressed(false);
                    }
                } else {
                    ExplodeEntitiesDialog.this.exitOk = false;
                    ExplodeEntitiesDialog.this.okCancelPanel.setOKPressed(false);
                    ExplodeEntitiesDialog.this.setVisible(false);
                }
            }
        });
        return this.okCancelPanel;
    }

    public boolean isInputValid() {
        return this.queryChooserSolucion.isInputValid();
    }

    public DataSourceQuery getQuerySolucion() {
        return this.queryChooserSolucion.getDataSourceQuery();
    }

    public boolean isExitOk() {
        return this.exitOk;
    }
}

