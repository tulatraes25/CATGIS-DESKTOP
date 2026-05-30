/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.datasource;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.commons.lang.StringUtils;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class ConfigLayerSQLKeyResolverDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private AbstractJDBCDataSource datasource;
    private JTextArea sqlTextArea;
    private JCheckBox hasKeyGeneratorCheckBox;
    private boolean okPressed;

    public ConfigLayerSQLKeyResolverDialog(JFrame parent, boolean modal, AbstractJDBCDataSource dataSource) {
        super((Frame)parent, modal);
        this.datasource = dataSource;
        this.setTitle(I18N.getString("org.saig.jump.widgets.datasource.ConfigLayerSQLKeyResolverDialog.Configure-key-generator"));
        this.setContentPane(this.getMainPanel());
        this.pack();
        GUIUtil.centreOnScreen(this);
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel northPanel = new JPanel(new GridBagLayout());
        this.hasKeyGeneratorCheckBox = new JCheckBox(I18N.getString("org.saig.jump.widgets.datasource.ConfigLayerSQLKeyResolverDialog.Activate-Deactivate-the-automatic-key-generation"));
        this.hasKeyGeneratorCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigLayerSQLKeyResolverDialog.this.sqlTextArea.setEnabled(ConfigLayerSQLKeyResolverDialog.this.hasKeyGeneratorCheckBox.isSelected());
            }
        });
        FormUtils.addRowInGBL(northPanel, 0, 0, this.hasKeyGeneratorCheckBox);
        JScrollPane sqlScrollPane = new JScrollPane(22, 31);
        sqlScrollPane.setBorder(BorderFactory.createTitledBorder("SQL"));
        String sqlTxt = "";
        if (this.datasource.getKeyResolver() != null) {
            sqlTxt = this.datasource.getKeyResolver().getSql();
        }
        this.hasKeyGeneratorCheckBox.setSelected(StringUtils.isNotEmpty((String)sqlTxt));
        this.sqlTextArea = new JTextArea(sqlTxt);
        this.sqlTextArea.setEnabled(StringUtils.isNotEmpty((String)sqlTxt));
        this.sqlTextArea.setLineWrap(true);
        this.sqlTextArea.setWrapStyleWord(true);
        this.sqlTextArea.setColumns(50);
        this.sqlTextArea.setRows(6);
        JLabel label = new JLabel();
        this.sqlTextArea.setFont(label.getFont());
        this.sqlTextArea.revalidate();
        sqlScrollPane.setViewportView(this.sqlTextArea);
        mainPanel.add((Component)northPanel, "North");
        mainPanel.add((Component)sqlScrollPane, "Center");
        mainPanel.add((Component)this.getOkCancelPanel(), "South");
        return mainPanel;
    }

    private boolean isInputValid() {
        String errMsg = null;
        if (this.hasKeyGeneratorCheckBox.isSelected()) {
            if (StringUtils.isEmpty((String)this.sqlTextArea.getText().trim())) {
                errMsg = I18N.getString("org.saig.jump.widgets.datasource.ConfigLayerSQLKeyResolverDialog.The-associated-query-is-obligatory");
            }
            if (errMsg == null) {
                Connection conn = null;
                Statement st = null;
                try {
                    try {
                        conn = this.datasource.getDirectConnection();
                        st = conn.createStatement();
                        ResultSet res = st.executeQuery(this.sqlTextArea.getText().trim());
                        if (res.next()) {
                            ResultSetMetaData resMetaData = res.getMetaData();
                            int i = 1;
                            while (i <= resMetaData.getColumnCount() && errMsg == null) {
                                String colName = resMetaData.getColumnLabel(i);
                                if (!this.datasource.getSchema().hasAttribute(colName)) {
                                    errMsg = I18N.getMessage("org.saig.jump.widgets.datasource.ConfigLayerSQLKeyResolverDialog.The-column-{0}-does-not-exist-in-the-layer", new Object[]{colName});
                                }
                                ++i;
                            }
                        }
                        res.close();
                    }
                    finally {
                        if (st != null) {
                            st.close();
                        }
                        if (conn != null) {
                            conn.close();
                        }
                    }
                }
                catch (Exception e) {
                    errMsg = I18N.getMessage("org.saig.jump.widgets.datasource.ConfigLayerSQLKeyResolverDialog.The-query-execution-has-failed-The-error-is-{0}", new Object[]{e.getMessage()});
                }
            }
            if (errMsg != null) {
                DialogFactory.showWarningDialog(this, errMsg, I18N.getString("org.saig.jump.widgets.datasource.ConfigLayerSQLKeyResolverDialog.Parameters-error"));
                return false;
            }
        }
        return true;
    }

    private OKCancelPanel getOkCancelPanel() {
        final OKCancelPanel okCancelPanel = new OKCancelPanel();
        okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (okCancelPanel.wasOKPressed() && ConfigLayerSQLKeyResolverDialog.this.isInputValid()) {
                    ConfigLayerSQLKeyResolverDialog.this.okPressed = true;
                } else {
                    ConfigLayerSQLKeyResolverDialog.this.okPressed = false;
                }
                ConfigLayerSQLKeyResolverDialog.this.dispose();
            }
        });
        return okCancelPanel;
    }

    public boolean isOkPressed() {
        return this.okPressed;
    }

    public String getSQL() {
        return this.sqlTextArea.getText();
    }

    public boolean hasKeyGenerator() {
        return this.hasKeyGeneratorCheckBox.isSelected();
    }
}

