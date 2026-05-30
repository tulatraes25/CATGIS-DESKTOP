/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.datasource;

import com.vividsolutions.jump.workbench.ui.MainMenuNames;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.dao.datasource.dbdatasource.DataBaseDataSourceFactory;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.utils.conversion.ExplodeEntitiesPlugIn;
import org.saig.jump.widgets.util.ChangeFirerPanel;

public class JDBCSavePropertiesPanel
extends ChangeFirerPanel
implements ActionListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(JDBCSavePropertiesPanel.class);
    private AbstractJDBCDataSource datasource = null;
    private JPanel attributesPanel = null;
    private JLabel hostLabel = null;
    private JTextField hostTextField = null;
    private JLabel portLabel = null;
    private JTextField portTextField = null;
    private JLabel databaseNameLabel = null;
    private JTextField databaseNameTextField = null;
    private JLabel schemaLabel = null;
    private JTextField schemaTextField = null;
    private JLabel usernameLabel = null;
    private JTextField usernameTextField = null;
    private JLabel passwordLabel = null;
    private JPasswordField passwordTextField = null;
    private JLabel tableLabel = null;
    private JTextField tableTextField = null;
    private JLabel geometryAttributeNameLabel;
    private JTextField geometryAttributeNameTextField;
    private JCheckBox addRestrictionsCheckbox;
    private JCheckBox ignore3DCheckbox;
    private JCheckBox forceMultigeometryTypeCheckbox;
    private JCheckBox useLowerCaseFieldNamesCheckbox;
    private JLabel addRestrictionsTextArea;
    private JPanel drawPanel = null;
    private JPanel centerPanel = null;
    private JLabel dbLabel = null;
    private JPanel databasePanel = null;
    private JLabel databaseTypeLabel = null;
    private JComboBox databaseTypeComboBox = null;
    private String error;
    private boolean showTableNameTextField = true;
    private KeyListener keyListener;

    public JDBCSavePropertiesPanel() {
        this(true);
    }

    public JDBCSavePropertiesPanel(boolean showTableName) {
        this.showTableNameTextField = showTableName;
        this.keyListener = new KeyAdapter(){

            @Override
            public void keyTyped(KeyEvent e) {
                JDBCSavePropertiesPanel.this.firePanelChanged();
            }
        };
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new BorderLayout());
        Dimension dim = null;
        dim = this.showTableNameTextField ? new Dimension(550, 450) : new Dimension(450, 380);
        this.setMinimumSize(dim);
        this.setPreferredSize(dim);
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(1), I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.connection-properties")));
        if (this.showTableNameTextField) {
            this.add((Component)this.getDrawPanel(), "West");
        }
        this.add((Component)this.getCenterPanel(), "Center");
    }

    private JPanel getAttributesPanel() {
        if (this.attributesPanel == null) {
            this.hostLabel = new JLabel();
            this.hostLabel.setText(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.host-name")) + ":");
            this.hostLabel.setHorizontalAlignment(2);
            this.portLabel = new JLabel();
            this.portLabel.setText(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.port")) + ":");
            this.portLabel.setHorizontalAlignment(2);
            this.databaseNameLabel = new JLabel();
            this.databaseNameLabel.setText(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.database-name")) + ":");
            this.databaseNameLabel.setHorizontalAlignment(2);
            this.usernameLabel = new JLabel();
            this.usernameLabel.setText(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.user-name")) + ":");
            this.usernameLabel.setHorizontalAlignment(2);
            this.passwordLabel = new JLabel();
            this.passwordLabel.setText(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.password")) + ":");
            this.passwordLabel.setHorizontalAlignment(2);
            this.schemaLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel.Schema")) + ":");
            this.schemaLabel.setHorizontalAlignment(2);
            this.tableLabel = new JLabel();
            this.tableLabel.setText(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.table")) + ":");
            this.tableLabel.setHorizontalAlignment(2);
            this.geometryAttributeNameLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel.Geometry-field")) + ":");
            this.geometryAttributeNameLabel.setHorizontalAlignment(2);
            this.addRestrictionsCheckbox = new JCheckBox(I18N.getString("org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel.add-geometry-type-restriction-to-the-table"));
            this.addRestrictionsCheckbox.setSelected(true);
            this.ignore3DCheckbox = new JCheckBox(I18N.getString("org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel.Ignore-3d"));
            this.ignore3DCheckbox.setSelected(true);
            this.forceMultigeometryTypeCheckbox = new JCheckBox(I18N.getString("org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel.Force-conversion-to-multigeometry"));
            this.forceMultigeometryTypeCheckbox.setSelected(false);
            this.useLowerCaseFieldNamesCheckbox = new JCheckBox(I18N.getString("org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel.Force-field-names-to-lowercase"));
            this.useLowerCaseFieldNamesCheckbox.setSelected(false);
            String explode = " (<b>" + MainMenuNames.TOOLS + " > " + MainMenuNames.TOOLS_CONVERSION + " > " + ExplodeEntitiesPlugIn.NAME + "</b>) ";
            String violatesCheckConstraint = "(violates check constraint ...)";
            String addRestriction = " <b>" + I18N.getString("org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel.add-geometry-type-restriction-to-the-table") + "</b> ";
            String recommended = " (" + I18N.getString("org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel.recommended") + ") ";
            this.addRestrictionsTextArea = new JLabel("<html><p align=\"justify\">" + I18N.getMessage("org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel.if-while-you-are-saving-the-layer-you-get-an-error-message-that-indicates-that-a-geometry-type-restriction-has-been-violated-{0}-you-can-explode-your-data-{1}-or-uncheck-the-mark-{2}-to-solve-it", new Object[]{violatesCheckConstraint, String.valueOf(explode) + recommended, addRestriction}) + "</p></html>");
            this.attributesPanel = new JPanel();
            this.attributesPanel.setLayout(new GridBagLayout());
            FormUtils.addRowInGBL((JComponent)this.attributesPanel, 1, 0, this.hostLabel, (JComponent)this.getHostTextField());
            FormUtils.addRowInGBL((JComponent)this.attributesPanel, 2, 0, this.portLabel, (JComponent)this.getPortTextField());
            FormUtils.addRowInGBL((JComponent)this.attributesPanel, 3, 0, this.databaseNameLabel, (JComponent)this.getDatabaseNameTextField());
            FormUtils.addRowInGBL((JComponent)this.attributesPanel, 4, 0, this.usernameLabel, (JComponent)this.getUsernameTextField());
            FormUtils.addRowInGBL((JComponent)this.attributesPanel, 5, 0, this.passwordLabel, (JComponent)this.getPasswordTextField());
            FormUtils.addRowInGBL((JComponent)this.attributesPanel, 6, 0, this.schemaLabel, (JComponent)this.getSchemaTextField());
            JTextField tableTextField = this.getTableTextField();
            if (this.showTableNameTextField) {
                FormUtils.addRowInGBL((JComponent)this.attributesPanel, 7, 0, this.tableLabel, (JComponent)tableTextField);
                FormUtils.addRowInGBL((JComponent)this.attributesPanel, 8, 0, this.geometryAttributeNameLabel, (JComponent)this.getGeometryAttributeNameTextField());
            }
            FormUtils.addRowInGBL(this.attributesPanel, 9, 0, this.addRestrictionsCheckbox);
            FormUtils.addRowInGBL(this.attributesPanel, 10, 0, this.addRestrictionsTextArea);
            FormUtils.addRowInGBL(this.attributesPanel, 11, 0, this.ignore3DCheckbox);
            FormUtils.addRowInGBL(this.attributesPanel, 12, 0, this.forceMultigeometryTypeCheckbox);
            FormUtils.addRowInGBL(this.attributesPanel, 13, 0, this.useLowerCaseFieldNamesCheckbox);
        }
        return this.attributesPanel;
    }

    public boolean checkConnectionWithSchema() {
        try {
            this.datasource = this.createDataSource();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            this.error = e.getMessage();
            return false;
        }
        return this.datasource != null;
    }

    private AbstractJDBCDataSource createDataSource() throws Exception {
        AbstractJDBCDataSource datasource = this.initializeDataSource();
        return datasource;
    }

    private AbstractJDBCDataSource initializeDataSource() throws SQLException, IOException {
        String host = this.getHost();
        int port = Integer.parseInt(this.getPort());
        String databaseName = this.getDatabaseName();
        String userName = this.getUserName();
        String password = this.getPassword();
        String databaseSchema = this.getSchema();
        String tableName = this.getTableName();
        String geometryColumnName = this.getGeometryColumnName();
        this.addRestrictions();
        String databaseType = (String)this.getDatabaseTypeComboBox().getSelectedItem();
        AbstractJDBCDataSource datasource = DataBaseDataSourceFactory.createLayerDataSource(databaseType, host, port, databaseName, userName, password, null);
        datasource.setDataBaseSchema(databaseSchema);
        datasource.setTableName(tableName);
        datasource.setGeomColName(geometryColumnName);
        return datasource;
    }

    private JTextField getHostTextField() {
        if (this.hostTextField == null) {
            this.hostTextField = new JTextField();
            this.hostTextField.setText("localhost");
            this.hostTextField.addKeyListener(this.keyListener);
        }
        return this.hostTextField;
    }

    private JTextField getPortTextField() {
        if (this.portTextField == null) {
            this.portTextField = new JTextField();
            this.portTextField.setText("5432");
            this.portTextField.addKeyListener(this.keyListener);
        }
        return this.portTextField;
    }

    private JTextField getDatabaseNameTextField() {
        if (this.databaseNameTextField == null) {
            this.databaseNameTextField = new JTextField();
            this.databaseNameTextField.addKeyListener(this.keyListener);
        }
        return this.databaseNameTextField;
    }

    private JTextField getUsernameTextField() {
        if (this.usernameTextField == null) {
            this.usernameTextField = new JTextField();
            this.usernameTextField.addKeyListener(this.keyListener);
        }
        return this.usernameTextField;
    }

    private JPasswordField getPasswordTextField() {
        if (this.passwordTextField == null) {
            this.passwordTextField = new JPasswordField();
            this.passwordTextField.setFont(new JLabel().getFont());
            this.passwordTextField.addKeyListener(this.keyListener);
        }
        return this.passwordTextField;
    }

    private JTextField getTableTextField() {
        if (this.tableTextField == null) {
            this.tableTextField = new JTextField();
            this.tableTextField.addKeyListener(this.keyListener);
        }
        return this.tableTextField;
    }

    private JTextField getGeometryAttributeNameTextField() {
        if (this.geometryAttributeNameTextField == null) {
            this.geometryAttributeNameTextField = new JTextField();
            this.geometryAttributeNameTextField.addKeyListener(this.keyListener);
        }
        return this.geometryAttributeNameTextField;
    }

    private JPanel getDrawPanel() {
        if (this.drawPanel == null) {
            this.dbLabel = new JLabel();
            this.dbLabel.setText("");
            this.dbLabel.setPreferredSize(new Dimension(50, 250));
            this.dbLabel.setIcon(IconLoader.icon("database.gif"));
            this.drawPanel = new JPanel();
            this.drawPanel.setBorder(BorderFactory.createEtchedBorder(1));
            this.drawPanel.setPreferredSize(new Dimension(55, 300));
            this.drawPanel.add((Component)this.dbLabel, null);
        }
        return this.drawPanel;
    }

    private JPanel getCenterPanel() {
        if (this.centerPanel == null) {
            this.centerPanel = new JPanel(new GridBagLayout());
            if (this.showTableNameTextField) {
                this.centerPanel.setBorder(BorderFactory.createEtchedBorder(1));
            }
            FormUtils.addRowInGBL(this.centerPanel, 0, 0, this.getDatabasePanel());
            FormUtils.addRowInGBL(this.centerPanel, 1, 0, this.getAttributesPanel());
            FormUtils.addFiller(this.centerPanel, 2, 0);
        }
        return this.centerPanel;
    }

    private JPanel getDatabasePanel() {
        if (this.databasePanel == null) {
            this.databaseTypeLabel = new JLabel();
            this.databaseTypeLabel.setText(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.database-type")) + ":");
            this.databasePanel = new JPanel(new FlowLayout());
            this.databasePanel.add(this.databaseTypeLabel);
            this.databasePanel.add(this.getDatabaseTypeComboBox());
        }
        return this.databasePanel;
    }

    private JComboBox getDatabaseTypeComboBox() {
        if (this.databaseTypeComboBox == null) {
            this.databaseTypeComboBox = new JComboBox();
            Collection<String> ids = DataBaseDataSourceFactory.getRegisteredLayerJDBCDataSources();
            ArrayList<String> datasources = new ArrayList<String>(ids.size());
            if (ids.contains("PostgreSQL")) {
                datasources.add(0, "PostgreSQL");
            }
            for (String currentID : ids) {
                if (currentID.equals("PostgreSQL")) continue;
                datasources.add(currentID);
            }
            ids = datasources;
            for (String currentID : ids) {
                this.databaseTypeComboBox.addItem(currentID);
            }
            this.databaseTypeComboBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    JDBCSavePropertiesPanel.this.refresh();
                }
            });
        }
        return this.databaseTypeComboBox;
    }

    public void refresh() {
        String selected = (String)this.databaseTypeComboBox.getSelectedItem();
        if (this.showTableNameTextField) {
            this.tableTextField.setText("");
        }
        this.databaseNameTextField.setText("");
        this.schemaTextField.setText("");
        this.schemaTextField.setEnabled(!selected.equals("MySQL"));
        this.geometryAttributeNameTextField.setText("");
        this.usernameTextField.setText("");
        this.passwordTextField.setText("");
        this.addRestrictionsCheckbox.setVisible(selected.equals("PostgreSQL"));
        this.addRestrictionsTextArea.setVisible(selected.equals("PostgreSQL"));
        this.ignore3DCheckbox.setVisible(selected.equals("PostgreSQL"));
        int defaultPort = DataBaseDataSourceFactory.getDefaultPort(selected);
        if (defaultPort != -1) {
            this.portTextField.setText("" + defaultPort);
            Object[] databaseNames = DataBaseConnectionFactory.getAllAvaliablesConnections(selected);
            String[] properties = null;
            if (!ArrayUtils.isEmpty((Object[])databaseNames)) {
                properties = DataBaseConnectionFactory.getPropertiesConnection(selected, (String)databaseNames[0]);
                if (properties == null) {
                    return;
                }
                this.refreshConnectionProperties((String[])databaseNames, properties);
            }
        }
    }

    private void refreshConnectionProperties(String[] schemas, String[] properties) {
        int i = 0;
        while (i < schemas.length) {
            this.databaseNameTextField.setText(schemas[i]);
            ++i;
        }
        if (schemas.length > 0) {
            this.usernameTextField.setText(properties[0]);
            this.passwordTextField.setText(properties[1]);
            this.hostTextField.setText(properties[2]);
            this.portTextField.setText(properties[3]);
        }
    }

    public String getHost() {
        return this.getHostTextField().getText().trim();
    }

    public String getPort() {
        return this.getPortTextField().getText().trim();
    }

    public String getDatabaseName() {
        return this.getDatabaseNameTextField().getText().trim();
    }

    public String getTableName() {
        String selectItem = this.tableTextField.getText();
        if (selectItem != null) {
            return selectItem;
        }
        return "";
    }

    public String getGeometryColumnName() {
        String selectItem = this.getGeometryAttributeNameTextField().getText();
        if (selectItem != null) {
            return selectItem;
        }
        return "";
    }

    public String getUserName() {
        return this.getUsernameTextField().getText().trim();
    }

    public String getPassword() {
        String password = new String(this.getPasswordTextField().getPassword());
        return password.trim();
    }

    public boolean checkParameters() {
        if (StringUtils.isEmpty((String)this.getHost())) {
            this.error = I18N.getString("org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel.Wrong-host-name");
            return false;
        }
        if (StringUtils.isEmpty((String)this.getPort())) {
            this.error = I18N.getString("org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel.Wrong-connection-port");
            return false;
        }
        try {
            Integer.parseInt(this.getPort());
        }
        catch (NumberFormatException nfe) {
            this.error = I18N.getString("org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel.Wrong-connection-port");
            return false;
        }
        if (StringUtils.isEmpty((String)this.getDatabaseName())) {
            this.error = I18N.getString("org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel.Wrong-data-base-name");
            return false;
        }
        if (StringUtils.isEmpty((String)this.getUserName())) {
            this.error = I18N.getString("org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel.Wrong-user-name");
            return false;
        }
        if (this.showTableNameTextField && StringUtils.isEmpty((String)this.getTableName())) {
            this.error = I18N.getString("org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel.Wrong-table-name");
            return false;
        }
        if (!((String)this.databaseTypeComboBox.getSelectedItem()).equals("MySQL") && StringUtils.isEmpty((String)this.getSchema())) {
            this.error = I18N.getString("org.saig.jump.widgets.datasource.JDBCSavePropertiesPanel.Wrong-schema-name");
            return false;
        }
        return true;
    }

    public AbstractJDBCDataSource getDataSource() {
        return this.datasource;
    }

    public void initializeTable() throws Exception {
        if (this.tableTextField.getText().equals("")) {
            throw new Exception(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.there-is-not-selected-table"));
        }
        this.datasource.setTableName(this.tableTextField.getText());
        this.datasource.initialize();
    }

    public String getError() {
        return this.error;
    }

    public JTextField getSchemaTextField() {
        if (this.schemaTextField == null) {
            this.schemaTextField = new JTextField();
            this.schemaTextField.addKeyListener(this.keyListener);
            String selectedDatabaseType = (String)this.databaseTypeComboBox.getSelectedItem();
            if (selectedDatabaseType != null) {
                this.schemaTextField.setEnabled(!selectedDatabaseType.equals("MySQL"));
            }
        }
        return this.schemaTextField;
    }

    public String getSelectedDataBaseType() {
        return (String)this.databaseTypeComboBox.getSelectedItem();
    }

    public String getSchema() {
        String schema = this.getSchemaTextField().getText().trim();
        if (schema.equals("")) {
            schema = null;
        }
        return schema;
    }

    public boolean addRestrictions() {
        return this.addRestrictionsCheckbox.isVisible() && this.addRestrictionsCheckbox.isSelected();
    }

    public boolean ignore3d() {
        return this.ignore3DCheckbox.isVisible() && this.ignore3DCheckbox.isSelected();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.firePanelChanged();
    }

    public boolean forceMultiGeometry() {
        return this.forceMultigeometryTypeCheckbox.isVisible() && this.forceMultigeometryTypeCheckbox.isSelected();
    }

    public boolean useLowerCaseFieldNames() {
        return this.useLowerCaseFieldNamesCheckbox.isVisible() && this.useLowerCaseFieldNamesCheckbox.isSelected();
    }
}

