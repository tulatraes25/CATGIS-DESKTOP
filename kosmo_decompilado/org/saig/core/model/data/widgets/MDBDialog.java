/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.dao.jdbc.MDBDataSource;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class MDBDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    protected static final Logger LOGGER = Logger.getLogger(MDBDialog.class);
    private JTextField userTextField;
    private JPasswordField passwordTextField;
    private JComboBox tablesComboBox;
    private JComboBox pkComboBox;
    private JButton connectButton;
    private MDBDataSource odbcDataSource;
    private String filePath;
    private boolean exitOk;

    public MDBDialog(JFrame parent, boolean modal, String path) {
        super((Frame)parent, modal);
        this.filePath = path;
        this.setTitle(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.access-data-base-connection"));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.createPanel());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.createOKcancelPanel());
        this.setContentPane(mainPanel);
        this.setSize(450, 250);
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel userLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.user")) + ":");
        this.userTextField = new JTextField();
        FormUtils.addRowInGBL((JComponent)panel, 0, 0, userLabel, (JComponent)this.userTextField);
        JLabel passwordLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.password")) + ":");
        this.passwordTextField = new JPasswordField();
        this.passwordTextField.setFont(this.userTextField.getFont());
        FormUtils.addRowInGBL((JComponent)panel, 1, 0, passwordLabel, (JComponent)this.passwordTextField);
        this.connectButton = new JButton(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.connect"), IconLoader.icon("database_connect.png"));
        this.connectButton.setPreferredSize(new Dimension(100, 25));
        this.connectButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                MDBDialog.this.odbcDataSource = new MDBDataSource(MDBDialog.this.filePath, MDBDialog.this.userTextField.getText().trim(), new String(MDBDialog.this.passwordTextField.getPassword()).trim());
                if (!MDBDialog.this.odbcDataSource.checkConnection()) {
                    DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.the-connection-with-the-database-can-not-be-established")) + ".\n" + I18N.getString("org.saig.core.model.data.widgets.MDBDialog.please-you-should-revise-the-connection-parameters"), I18N.getString("org.saig.core.model.data.widgets.MDBDialog.connection-error"));
                    return;
                }
                List<String> tables = null;
                try {
                    tables = MDBDialog.this.odbcDataSource.getAllTables("");
                    MDBDialog.this.tablesComboBox.removeAllItems();
                    MDBDialog.this.pkComboBox.removeAllItems();
                    boolean tablesFound = tables.size() > 0;
                    MDBDialog.this.tablesComboBox.setEnabled(tablesFound);
                    MDBDialog.this.pkComboBox.setEnabled(tablesFound);
                    if (tablesFound) {
                        Iterator<String> iter = tables.iterator();
                        while (iter.hasNext()) {
                            MDBDialog.this.tablesComboBox.addItem(iter.next());
                        }
                        String tableName = tables.get(0);
                        List<String> columns = MDBDialog.this.odbcDataSource.getColumns(tableName);
                        MDBDialog.this.pkComboBox.removeAllItems();
                        Iterator<String> iter2 = columns.iterator();
                        while (iter2.hasNext()) {
                            MDBDialog.this.pkComboBox.addItem(iter2.next());
                        }
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.an-unexpected-error-has-been-produced")) + ".\n" + I18N.getString("org.saig.core.model.data.widgets.MDBDialog.please-you-should-revise-the-connection-parameters"), I18N.getString("org.saig.core.model.data.widgets.MDBDialog.unexpected-error"));
                }
            }
        });
        JPanel jbuttonPanel = new JPanel(new FlowLayout());
        jbuttonPanel.add(this.connectButton);
        FormUtils.addRowInGBL(panel, 2, 0, jbuttonPanel);
        JLabel tablaslabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.select-a-table")) + ":");
        this.tablesComboBox = new JComboBox();
        this.tablesComboBox.setEnabled(false);
        this.tablesComboBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                String tableName = (String)MDBDialog.this.tablesComboBox.getSelectedItem();
                List<String> columns = MDBDialog.this.odbcDataSource.getColumns(tableName);
                MDBDialog.this.pkComboBox.removeAllItems();
                Iterator<String> iter = columns.iterator();
                while (iter.hasNext()) {
                    MDBDialog.this.pkComboBox.addItem(iter.next());
                }
            }
        });
        FormUtils.addRowInGBL((JComponent)panel, 3, 0, tablaslabel, (JComponent)this.tablesComboBox);
        JLabel pkLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.select-the-primary-key")) + ":");
        this.pkComboBox = new JComboBox();
        this.pkComboBox.setEnabled(false);
        FormUtils.addRowInGBL((JComponent)panel, 4, 0, pkLabel, (JComponent)this.pkComboBox);
        return panel;
    }

    private OKCancelPanel createOKcancelPanel() {
        final OKCancelPanel okCancelPanel = new OKCancelPanel();
        GridBagLayout gbPaneOKCancel = new GridBagLayout();
        okCancelPanel.setLayout(gbPaneOKCancel);
        okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (okCancelPanel.wasOKPressed() && MDBDialog.this.isInputValid()) {
                    MDBDialog.this.exitOk = true;
                    if (MDBDialog.this.odbcDataSource != null) {
                        MDBDialog.this.odbcDataSource.setTableName((String)MDBDialog.this.tablesComboBox.getSelectedItem());
                        try {
                            MDBDialog.this.odbcDataSource.buildSchema((String)MDBDialog.this.pkComboBox.getSelectedItem());
                        }
                        catch (Exception e) {
                            LOGGER.error((Object)"", (Throwable)e);
                            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.an-unexpected-error-has-been-produced")) + ".\n" + I18N.getString("org.saig.core.model.data.widgets.MDBDialog.please-you-should-revise-the-connection-parameters"), I18N.getString("org.saig.core.model.data.widgets.MDBDialog.unexpected-error"));
                            return;
                        }
                    }
                    MDBDialog.this.setVisible(false);
                } else if (!okCancelPanel.wasOKPressed()) {
                    MDBDialog.this.exitOk = false;
                    MDBDialog.this.setVisible(false);
                }
            }
        });
        return okCancelPanel;
    }

    public boolean isOk() {
        return this.exitOk;
    }

    public MDBDataSource getOdbcDataSource() {
        return this.odbcDataSource;
    }

    public boolean isInputValid() {
        if (this.odbcDataSource != null) {
            this.odbcDataSource.setTableName((String)this.tablesComboBox.getSelectedItem());
            try {
                this.odbcDataSource.buildSchema((String)this.pkComboBox.getSelectedItem());
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.an-unexpected-error-has-been-produced")) + ".\n" + I18N.getString("org.saig.core.model.data.widgets.MDBDialog.please-you-should-revise-the-connection-parameters"), I18N.getString("org.saig.core.model.data.widgets.MDBDialog.unexpected-error"));
                return false;
            }
            List<Object> keys = this.odbcDataSource.getOrderedPrimaryKeyList();
            HashSet<Object> finalKeys = new HashSet<Object>();
            boolean hasNullKeys = false;
            Iterator<Object> iter = keys.iterator();
            while (iter.hasNext() && !hasNullKeys) {
                Object key = iter.next();
                if (key == null) {
                    hasNullKeys = true;
                    continue;
                }
                finalKeys.add(key);
            }
            if (hasNullKeys) {
                DialogFactory.showWarningDialog(this, I18N.getMessage("org.saig.core.model.data.widgets.MDBDialog.the-column-{0}-selected-as-primery-key-for-the-table-{1}-has-at-least-one-null-or-empty-value", new Object[]{(String)this.pkComboBox.getSelectedItem(), (String)this.tablesComboBox.getSelectedItem()}), I18N.getString("org.saig.core.model.data.widgets.MDBDialog.incorrect-selected-column"));
                return false;
            }
            if (keys.size() != finalKeys.size()) {
                DialogFactory.showWarningDialog(this, I18N.getMessage("org.saig.core.model.data.widgets.MDBDialog.the-column-{0}-selected-as-primary-key-for-the-table-{1}-has-duplicated-values", new Object[]{(String)this.pkComboBox.getSelectedItem(), (String)this.tablesComboBox.getSelectedItem()}), I18N.getString("org.saig.core.model.data.widgets.MDBDialog.incorrect-selected-column"));
                return false;
            }
            return true;
        }
        return false;
    }
}

