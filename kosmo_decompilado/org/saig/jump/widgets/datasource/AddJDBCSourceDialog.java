/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.jump.widgets.datasource;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.datasource.JDBCPropertiesPanel;

public class AddJDBCSourceDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private OKCancelPanel okCancelPanel;
    private JDBCPropertiesPanel jdbcPanel;
    private boolean exitOk = false;

    public AddJDBCSourceDialog(JFrame parent, boolean modal, PlugInContext context) {
        super((Frame)parent, modal);
        this.setTitle(I18N.getString("org.saig.jump.widgets.datasource.AddJDBCSourceDialog.data-loading-from-JDBC-source"));
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.setContentPane(mainPanel);
        this.createOKcancelPanel();
        this.jdbcPanel = new JDBCPropertiesPanel(true);
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.jdbcPanel);
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.okCancelPanel);
        this.pack();
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }

    private void createOKcancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        GridBagLayout gbPaneOKCancel = new GridBagLayout();
        this.okCancelPanel.setLayout(gbPaneOKCancel);
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (AddJDBCSourceDialog.this.okCancelPanel.wasOKPressed()) {
                    AddJDBCSourceDialog.this.exitOk = true;
                } else {
                    AddJDBCSourceDialog.this.exitOk = false;
                }
                AddJDBCSourceDialog.this.setVisible(false);
            }
        });
    }

    public boolean isOk() {
        return this.exitOk;
    }

    public AbstractJDBCDataSource getDataSource() throws Exception {
        this.jdbcPanel.initializeTable();
        List<AbstractJDBCDataSource> dataSources = this.jdbcPanel.getLayerDataSources();
        if (CollectionUtils.isNotEmpty(dataSources)) {
            return (AbstractJDBCDataSource)dataSources.iterator().next();
        }
        return null;
    }
}

