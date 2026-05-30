/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.hsqldb.lib.FileUtil
 */
package es.kosmo.desktop.widgets.datasource;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import es.kosmo.desktop.widgets.datasource.AbstractFileBasedDBTableSelectionPanel;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.lang.StringUtils;
import org.hsqldb.lib.FileUtil;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.dao.jdbc.MDBDataSource;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class MDBPanel
extends AbstractFileBasedDBTableSelectionPanel {
    private static final long serialVersionUID = 1L;
    protected JComboBox tableSelectionComboBox;
    protected JComboBox pkFieldSelectionComboBox;

    @Override
    protected String checkConnection() {
        String filePath = StringUtils.trim((String)this.filePanel.getSelectedPath());
        String userName = StringUtils.trim((String)this.userTextField.getText());
        String password = StringUtils.trim((String)new String(this.passwordTextField.getPassword()));
        if (StringUtils.isEmpty((String)filePath) || !FileUtil.exists((String)filePath)) {
            return I18N.getString("es.kosmo.desktop.widgets.datasource.MDBPanel.Incorrect-file-path");
        }
        this.datasource = new MDBDataSource(filePath, userName, password);
        if (!((MDBDataSource)this.datasource).checkConnection()) {
            this.datasource = null;
            return I18N.getString("org.saig.core.model.data.widgets.MDBDialog.please-you-should-revise-the-connection-parameters");
        }
        return null;
    }

    @Override
    protected String[] getFileExtensions() {
        return new String[]{"mdb"};
    }

    @Override
    protected JPanel getTableSelectionPanel() {
        JPanel tableSelectionPanel = new JPanel(new GridBagLayout());
        tableSelectionPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("es.kosmo.desktop.widgets.datasource.MDBPanel.Select-the-table-and-primary-key")));
        JLabel tablesLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.select-a-table")) + ":");
        this.tableSelectionComboBox = new JComboBox();
        this.tableSelectionComboBox.setEnabled(false);
        this.tableSelectionComboBox.addActionListener(this);
        JLabel pkLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.select-the-primary-key")) + ":");
        this.pkFieldSelectionComboBox = new JComboBox();
        this.pkFieldSelectionComboBox.setEnabled(false);
        FormUtils.addRowInGBL((JComponent)tableSelectionPanel, 0, 0, tablesLabel, (JComponent)this.tableSelectionComboBox);
        FormUtils.addRowInGBL((JComponent)tableSelectionPanel, 1, 0, pkLabel, (JComponent)this.pkFieldSelectionComboBox);
        return tableSelectionPanel;
    }

    @Override
    public String getID() {
        return "MS Access";
    }

    @Override
    public void refresh() {
    }

    @Override
    public String getDescription() {
        return I18N.getString("es.kosmo.desktop.widgets.datasource.MDBPanel.MS-Access-files");
    }

    @Override
    public Component getComponent() {
        return this;
    }

    public List<TableRecordDataSource> getTableDataSources() throws Exception {
        ArrayList<TableRecordDataSource> datasources = new ArrayList<TableRecordDataSource>();
        if (this.datasource != null) {
            ((MDBDataSource)this.datasource).setTableName((String)this.tableSelectionComboBox.getSelectedItem());
            ((MDBDataSource)this.datasource).buildSchema((String)this.pkFieldSelectionComboBox.getSelectedItem());
            datasources.add(this.datasource);
        }
        return datasources;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.connectButton)) {
            super.actionPerformed(e);
        } else if (e.getSource().equals(this.tableSelectionComboBox)) {
            String tableName = (String)this.tableSelectionComboBox.getSelectedItem();
            List<String> columns = ((MDBDataSource)this.datasource).getColumns(tableName);
            this.pkFieldSelectionComboBox.removeAllItems();
            Iterator<String> itColumns = columns.iterator();
            while (itColumns.hasNext()) {
                this.pkFieldSelectionComboBox.addItem(itColumns.next());
            }
        }
    }

    @Override
    protected void refreshTableSelectionPanel(boolean enable) {
        if (enable) {
            this.pkFieldSelectionComboBox.setEnabled(enable);
            this.tableSelectionComboBox.setEnabled(enable);
            List<String> tables = null;
            try {
                tables = ((MDBDataSource)this.datasource).getAllTables("");
                this.tableSelectionComboBox.removeAllItems();
                this.pkFieldSelectionComboBox.removeAllItems();
                boolean tablesFound = tables.size() > 0;
                this.tableSelectionComboBox.setEnabled(tablesFound);
                this.pkFieldSelectionComboBox.setEnabled(tablesFound);
                if (tablesFound) {
                    Iterator<String> iter = tables.iterator();
                    while (iter.hasNext()) {
                        this.tableSelectionComboBox.addItem(iter.next());
                    }
                    String tableName = tables.get(0);
                    List<String> columns = ((MDBDataSource)this.datasource).getColumns(tableName);
                    this.pkFieldSelectionComboBox.removeAllItems();
                    Iterator<String> iter2 = columns.iterator();
                    while (iter2.hasNext()) {
                        this.pkFieldSelectionComboBox.addItem(iter2.next());
                    }
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.core.model.data.widgets.MDBDialog.an-unexpected-error-has-been-produced")) + ".\n" + I18N.getString("org.saig.core.model.data.widgets.MDBDialog.please-you-should-revise-the-connection-parameters"), I18N.getString("org.saig.core.model.data.widgets.MDBDialog.unexpected-error"));
            }
        } else {
            this.tableSelectionComboBox.removeAllItems();
            this.pkFieldSelectionComboBox.removeAllItems();
            this.tableSelectionComboBox.setEnabled(false);
            this.pkFieldSelectionComboBox.setEnabled(false);
        }
    }

    @Override
    public boolean isInputValid() {
        try {
            return this.getTableDataSources().size() > 0;
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            return false;
        }
    }
}

