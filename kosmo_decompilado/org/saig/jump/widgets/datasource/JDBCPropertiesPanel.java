/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.datasource;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import es.kosmo.core.dao.UnknownGeometryTypeException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.dao.datasource.dbdatasource.DataBaseDataSourceFactory;
import org.saig.core.dao.datasource.dbdatasource.ExtendPostGisDataSource;
import org.saig.core.dao.datasource.dbdatasource.utils.Field;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.datasource.ConnectionInformation;
import org.saig.jump.widgets.datasource.FieldCellRenderer;
import org.saig.jump.widgets.util.ChangeFirerPanel;
import org.saig.jump.widgets.util.CheckBoxJListSelectionPanel;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.SelectGeometryTypeDialog;
import org.saig.jump.widgets.util.validating.AbstractValidator;
import org.saig.jump.widgets.util.validating.GreaterThanTextFieldValidator;
import org.saig.jump.widgets.util.validating.MultipleValidator;
import org.saig.jump.widgets.util.validating.NullComboBoxValidator;
import org.saig.jump.widgets.util.validating.NullTextFieldValidator;

public class JDBCPropertiesPanel
extends ChangeFirerPanel
implements ActionListener,
ListSelectionListener,
PropertyChangeListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(JDBCPropertiesPanel.class);
    public static final String BASE_TITLE_BORDER = I18N.getString(JDBCPropertiesPanel.class, "table-properties");
    public static final String SHOW_ALL_SCHEMAS = I18N.getString(JDBCPropertiesPanel.class, "all-schemas");
    private AbstractJDBCDataSource baseLayerDatasource = null;
    private List<AbstractJDBCDataSource> layerDatasources = null;
    private TableDBRecordDataSource baseTableDatasource = null;
    private List<TableDBRecordDataSource> tableDatasources = null;
    private JPanel attributesPanel = null;
    private JPanel centerPanel;
    private JPanel databasePanel;
    private JLabel databaseTypeLabel;
    private JComboBox dataBaseTypeComboBox;
    private JPanel informationPanel;
    private JLabel instructionsLabel;
    private JLabel hostLabel;
    private JTextField hostTextField;
    private JLabel portLabel;
    private JTextField portTextField;
    private JLabel dataBaseLabel;
    private JComboBox dataBaseComboBox;
    private JLabel usernameLabel;
    private JTextField usernameTextField;
    private JLabel passwordLabel;
    private JPasswordField passwordTextField;
    private JLabel schemaLabel;
    private JComboBox schemaComboBox;
    private JPanel buttonPanel;
    private JButton testConnectionButton;
    private JLabel primaryKeyLabel;
    private JComboBox pkColumnsComboBox;
    private JLabel geometryColumnsLabel;
    private JComboBox geometryColumnsComboBox;
    private JPanel tableOptionsPanel;
    private JCheckBox advancedPostGisCheckbox;
    private JDialog parent;
    private boolean showSchemaTableNamePkComboboxes = true;
    private Dimension dimSmall = new Dimension(50, 20);
    private Dimension dimBasic = new Dimension(100, 20);
    private Dimension dimBig = new Dimension(200, 20);
    private CheckBoxJListSelectionPanel<ConnectionInformation> tableSelectionPanel;
    private boolean isLayer;
    private boolean checkingConnection;

    public JDBCPropertiesPanel(boolean layerLoading) {
        this(true, layerLoading);
    }

    public JDBCPropertiesPanel(boolean showSchemaTableNamePkComboboxes, boolean isLayerDialog) {
        this.showSchemaTableNamePkComboboxes = showSchemaTableNamePkComboboxes;
        this.isLayer = isLayerDialog;
        this.listeners = new ArrayList();
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new BorderLayout());
        if (this.showSchemaTableNamePkComboboxes) {
            this.setMinimumSize(new Dimension(700, 500));
            this.setPreferredSize(new Dimension(700, 500));
        } else {
            this.setMinimumSize(new Dimension(700, 330));
            this.setPreferredSize(new Dimension(700, 330));
        }
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(1), I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.connection-properties")));
        this.add((Component)this.getCenterPanel(), "Center");
    }

    private JPanel getAttributesPanel() {
        if (this.attributesPanel == null) {
            this.attributesPanel = new JPanel(new GridBagLayout());
            this.hostLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.host-name")) + ":");
            this.portLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.port")) + ":");
            this.dataBaseLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.database-name")) + ":");
            this.usernameLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.user-name")) + ":");
            this.passwordLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.password")) + ":");
            this.primaryKeyLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.Primary-key")) + ":");
            this.geometryColumnsLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.Geometry")) + ":");
            this.schemaLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.Schema")) + ":");
            this.tableSelectionPanel = new CheckBoxJListSelectionPanel(new ArrayList(), I18N.getString(this.getClass(), "available-tables"), new Dimension(250, 100), true, false);
            this.tableSelectionPanel.addListSelectionListener(this);
            this.tableSelectionPanel.addElementSelectionChangedListener(this);
            JComboBox pkComboBox = this.getPrimaryColumnsComboBox();
            JComboBox geomColumnNameComboBox = this.getGeometryColumnsComboBox();
            this.advancedPostGisCheckbox = new JCheckBox(I18N.getString(this.getClass(), "optimized-load"));
            this.advancedPostGisCheckbox.setEnabled(false);
            this.advancedPostGisCheckbox.addActionListener(this);
            FormUtils.addRowInGBL(this.attributesPanel, 0, 0, this.getInformationPanel());
            FormUtils.addRowInGBL((JComponent)this.attributesPanel, 1, 0, this.hostLabel, (JComponent)this.getHostTextField(), false);
            FormUtils.addRowInGBL((JComponent)this.attributesPanel, 1, 50, this.portLabel, (JComponent)this.getPortTextField(), false);
            FormUtils.addRowInGBL((JComponent)this.attributesPanel, 1, 70, this.dataBaseLabel, (JComponent)this.getDataBaseComboBox(), true);
            FormUtils.addRowInGBL((JComponent)this.attributesPanel, 4, 0, this.usernameLabel, (JComponent)this.getUsernameTextField(), false);
            FormUtils.addRowInGBL((JComponent)this.attributesPanel, 4, 50, this.passwordLabel, (JComponent)this.getPasswordTextField(), true);
            FormUtils.addRowInGBL(this.attributesPanel, 5, 0, this.getButtonPanel());
            FormUtils.addRowInGBL((JComponent)this.attributesPanel, 6, 0, this.schemaLabel, (JComponent)this.getSchemaComboBox(), true);
            if (this.showSchemaTableNamePkComboboxes) {
                FormUtils.addRowInGBL(this.attributesPanel, 10, 0, this.tableSelectionPanel);
                this.tableOptionsPanel = new JPanel(new GridBagLayout());
                this.tableOptionsPanel.setBorder(BorderFactory.createTitledBorder(BASE_TITLE_BORDER));
                FormUtils.addRowInGBL((JComponent)this.tableOptionsPanel, 1, 0, this.primaryKeyLabel, (JComponent)pkComboBox, true);
                if (this.isLayer) {
                    FormUtils.addRowInGBL((JComponent)this.tableOptionsPanel, 2, 0, this.geometryColumnsLabel, (JComponent)geomColumnNameComboBox, true);
                    FormUtils.addRowInGBL(this.tableOptionsPanel, 3, 0, this.advancedPostGisCheckbox);
                }
                FormUtils.addRowInGBL(this.attributesPanel, 11, 0, this.tableOptionsPanel);
            }
            FormUtils.addFiller(this.attributesPanel, 15, 0);
        }
        return this.attributesPanel;
    }

    private JPanel getButtonPanel() {
        if (this.buttonPanel == null) {
            this.buttonPanel = new JPanel(new FlowLayout());
            this.buttonPanel.add(this.getTestConnectionButton());
        }
        return this.buttonPanel;
    }

    private JPanel getInformationPanel() {
        if (this.informationPanel == null) {
            this.informationPanel = new JPanel(new GridBagLayout());
            this.instructionsLabel = new JLabel();
            this.instructionsLabel.setText("<HTML>" + I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.insert-the-data-needed-to-get-a-connection-to-the-database-you-can-use-the-testconnection-button") + ".</HTML>");
            this.instructionsLabel.setHorizontalAlignment(0);
            this.instructionsLabel.setHorizontalTextPosition(0);
            FormUtils.addRowInGBL(this.informationPanel, 0, 0, this.instructionsLabel);
        }
        return this.informationPanel;
    }

    private JButton getTestConnectionButton() {
        if (this.testConnectionButton == null) {
            this.testConnectionButton = new JButton(IconLoader.icon("database_connect.png"));
            this.testConnectionButton.setText(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.test-connection"));
            this.testConnectionButton.addActionListener(this);
        }
        return this.testConnectionButton;
    }

    public boolean checkConnection() {
        try {
            this.checkingConnection = true;
            List<String> schemas = null;
            if (this.isLayer) {
                this.baseLayerDatasource = this.createBaseLayerDataSource();
                schemas = this.baseLayerDatasource.getSchemas();
            } else {
                this.baseTableDatasource = this.createBaseTableDataSource();
                schemas = this.baseTableDatasource.getSchemas();
            }
            if (CollectionUtils.isEmpty(schemas) && !((String)this.dataBaseTypeComboBox.getSelectedItem()).equals("MySQL")) {
                DialogFactory.showWarningDialog(this, I18N.getMessage(this.getClass(), "database-{0}-{1}-{2}-does-not-contains-geometric-tables", new Object[]{this.getHost(), this.getPort(), this.getDatabaseName()}), I18N.getString(this.getClass(), "geometric-tables-not-found"));
                return false;
            }
            try {
                this.schemaComboBox.removeAllItems();
                Collections.sort(schemas, Collator.getInstance(I18N.getLocale()));
                this.schemaComboBox.addItem(SHOW_ALL_SCHEMAS);
                for (String currentSchema : schemas) {
                    this.schemaComboBox.addItem(currentSchema);
                }
                ArrayList<ConnectionInformation> tables = new ArrayList<ConnectionInformation>();
                if (CollectionUtils.isNotEmpty(schemas)) {
                    for (String currentSchema : schemas) {
                        tables.addAll(this.createConnectionInformationForSchema(currentSchema));
                    }
                } else {
                    tables.addAll(this.createConnectionInformationForSchema(""));
                }
                this.tableSelectionPanel.setListObjects(tables);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.baseLayerDatasource = null;
                this.baseTableDatasource = null;
                this.layerDatasources = null;
                this.tableDatasources = null;
                this.tableSelectionPanel.clearList();
                this.schemaComboBox.removeAllItems();
                this.pkColumnsComboBox.removeAllItems();
                this.geometryColumnsComboBox.removeAllItems();
                return false;
            }
        }
        finally {
            this.checkingConnection = false;
        }
        if (this.isLayer) {
            return this.baseLayerDatasource != null;
        }
        return this.baseTableDatasource != null;
    }

    private List<ConnectionInformation> createConnectionInformationForSchema(String schemaName) throws Exception {
        List<String> tablesForSchema = null;
        tablesForSchema = this.isLayer ? this.baseLayerDatasource.getAllGeometriesTables(schemaName) : this.baseTableDatasource.getAllTables(schemaName);
        ArrayList<ConnectionInformation> infos = new ArrayList<ConnectionInformation>();
        for (String tableName : tablesForSchema) {
            infos.add(new ConnectionInformation(schemaName, tableName, null, null));
        }
        return infos;
    }

    private AbstractJDBCDataSource createBaseLayerDataSource() throws SQLException, IOException {
        AbstractJDBCDataSource datasource = this.initializeLayerDataSource();
        return datasource;
    }

    private TableDBRecordDataSource createBaseTableDataSource() throws SQLException, IOException {
        TableDBRecordDataSource datasource = this.initializeTableDataSource();
        return datasource;
    }

    private AbstractJDBCDataSource initializeLayerDataSource() throws SQLException, IOException {
        String host = this.getHost();
        int port = Integer.parseInt(this.getPort());
        String databaseName = this.getDatabaseName();
        String userName = this.getUserName();
        String password = this.getPassword();
        String databaseType = (String)this.getDatabaseTypeComboBox().getSelectedItem();
        boolean advancedCheckBoxSelected = this.advancedPostGisCheckbox.isSelected();
        HashMap<String, Object> advancedProperties = new HashMap<String, Object>();
        advancedProperties.put("optimized_loading", advancedCheckBoxSelected);
        return DataBaseDataSourceFactory.createLayerDataSource(databaseType, host, port, databaseName, userName, password, advancedProperties);
    }

    private TableDBRecordDataSource initializeTableDataSource() throws SQLException, IOException {
        String host = this.getHost();
        int port = Integer.parseInt(this.getPort());
        String databaseName = this.getDatabaseName();
        String userName = this.getUserName();
        String password = this.getPassword();
        String databaseType = (String)this.getDatabaseTypeComboBox().getSelectedItem();
        return DataBaseDataSourceFactory.createTableDataSource(databaseType, host, port, databaseName, userName, password, null);
    }

    private JTextField getHostTextField() {
        if (this.hostTextField == null) {
            this.hostTextField = new JTextField();
            this.hostTextField.setMinimumSize(this.dimBasic);
            this.hostTextField.setPreferredSize(this.dimBasic);
            this.hostTextField.setText("localhost");
        }
        return this.hostTextField;
    }

    private JTextField getPortTextField() {
        if (this.portTextField == null) {
            this.portTextField = new JTextField();
            this.portTextField.setMinimumSize(this.dimSmall);
            this.portTextField.setPreferredSize(this.dimSmall);
            this.portTextField.setText("5432");
        }
        return this.portTextField;
    }

    private JComboBox getDataBaseComboBox() {
        if (this.dataBaseComboBox == null) {
            this.dataBaseComboBox = new JComboBox();
            this.dataBaseComboBox.setEditable(true);
            this.dataBaseComboBox.setMinimumSize(this.dimBig);
            this.dataBaseComboBox.setPreferredSize(this.dimBig);
            this.dataBaseComboBox.addActionListener(this);
        }
        return this.dataBaseComboBox;
    }

    private JComboBox getSchemaComboBox() {
        if (this.schemaComboBox == null) {
            this.schemaComboBox = new JComboBox();
            this.schemaComboBox.setMinimumSize(this.dimBig);
            this.schemaComboBox.setPreferredSize(this.dimBig);
            this.schemaComboBox.addActionListener(this);
        }
        return this.schemaComboBox;
    }

    private JComboBox getPrimaryColumnsComboBox() {
        if (this.pkColumnsComboBox == null) {
            this.pkColumnsComboBox = new JComboBox();
            this.pkColumnsComboBox.setRenderer(new FieldCellRenderer());
            this.pkColumnsComboBox.setEnabled(false);
            this.pkColumnsComboBox.addActionListener(this);
        }
        return this.pkColumnsComboBox;
    }

    private JComboBox getGeometryColumnsComboBox() {
        if (this.geometryColumnsComboBox == null) {
            this.geometryColumnsComboBox = new JComboBox();
            this.geometryColumnsComboBox.setRenderer(new FieldCellRenderer());
            this.geometryColumnsComboBox.setEnabled(false);
            this.geometryColumnsComboBox.addActionListener(this);
        }
        return this.geometryColumnsComboBox;
    }

    private JTextField getUsernameTextField() {
        if (this.usernameTextField == null) {
            this.usernameTextField = new JTextField();
            this.usernameTextField.setMinimumSize(this.dimBasic);
            this.usernameTextField.setPreferredSize(this.dimBasic);
        }
        return this.usernameTextField;
    }

    private JPasswordField getPasswordTextField() {
        if (this.passwordTextField == null) {
            this.passwordTextField = new JPasswordField();
            this.passwordTextField.setFont(new JLabel().getFont());
            this.passwordTextField.setMinimumSize(this.dimBasic);
            this.passwordTextField.setPreferredSize(this.dimBasic);
        }
        return this.passwordTextField;
    }

    private JPanel getCenterPanel() {
        if (this.centerPanel == null) {
            this.centerPanel = new JPanel(new GridBagLayout());
            FormUtils.addRowInGBL(this.centerPanel, 0, 0, this.getDatabasePanel());
            FormUtils.addRowInGBL(this.centerPanel, 1, 0, this.getAttributesPanel());
            FormUtils.addFiller(this.centerPanel, 2, 0);
        }
        return this.centerPanel;
    }

    private JPanel getDatabasePanel() {
        if (this.databasePanel == null) {
            this.databasePanel = new JPanel(new FlowLayout());
            this.databaseTypeLabel = new JLabel();
            this.databaseTypeLabel.setText(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.database-type")) + ":");
            this.databasePanel.add((Component)this.databaseTypeLabel, null);
            this.databasePanel.add((Component)this.getDatabaseTypeComboBox(), null);
        }
        return this.databasePanel;
    }

    private JComboBox getDatabaseTypeComboBox() {
        if (this.dataBaseTypeComboBox == null) {
            ArrayList<String> datasources;
            this.dataBaseTypeComboBox = new JComboBox();
            Collection<String> ids = null;
            if (this.isLayer) {
                ids = DataBaseDataSourceFactory.getRegisteredLayerJDBCDataSources();
                datasources = new ArrayList<String>(ids.size());
                if (ids.contains("PostgreSQL")) {
                    datasources.add(0, "PostgreSQL");
                }
                for (String currentID : ids) {
                    if (currentID.equals("PostgreSQL")) continue;
                    datasources.add(currentID);
                }
                ids = datasources;
            } else {
                ids = DataBaseDataSourceFactory.getRegisteredTableJDBCDataSources();
                datasources = new ArrayList(ids.size());
                if (ids.contains("PostgreSQL")) {
                    datasources.add(0, "PostgreSQL");
                }
                for (String currentID : ids) {
                    if (currentID.equals("PostgreSQL")) continue;
                    datasources.add(currentID);
                }
                ids = datasources;
            }
            for (String currentID : ids) {
                this.dataBaseTypeComboBox.addItem(currentID);
            }
            this.dataBaseTypeComboBox.addActionListener(this);
        }
        return this.dataBaseTypeComboBox;
    }

    private void refreshConnectionProperties(String[] schemas, String[] properties) {
        int i = 0;
        while (i < schemas.length) {
            this.dataBaseComboBox.addItem(schemas[i]);
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
        Object selectItem = this.dataBaseComboBox.getSelectedItem();
        if (selectItem != null) {
            return (String)selectItem;
        }
        return "";
    }

    public String getTableName() {
        ConnectionInformation selectedInfo = this.tableSelectionPanel.getSelectedItem();
        if (selectedInfo != null) {
            return selectedInfo.getTableName();
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
            return false;
        }
        if (StringUtils.isEmpty((String)this.getPort())) {
            return false;
        }
        try {
            Integer.parseInt(this.getPort());
        }
        catch (NumberFormatException nfe) {
            return false;
        }
        if (StringUtils.isEmpty((String)this.getDatabaseName())) {
            return false;
        }
        return !StringUtils.isEmpty((String)this.getUserName());
    }

    public List<AbstractJDBCDataSource> getLayerDataSources() {
        return this.layerDatasources;
    }

    public List<TableDBRecordDataSource> getTableDataSources() {
        return this.tableDatasources;
    }

    private List<TableDBRecordDataSource> createTableDatasources() throws Exception {
        List<ConnectionInformation> infos = this.tableSelectionPanel.getSelectedObjects();
        ArrayList<TableDBRecordDataSource> datasources = new ArrayList<TableDBRecordDataSource>();
        for (ConnectionInformation currentInfo : infos) {
            TableDBRecordDataSource newDS = (TableDBRecordDataSource)this.baseTableDatasource.clone();
            newDS.setDataBaseSchema(currentInfo.getSchemaName());
            newDS.setTableName(currentInfo.getTableName());
            newDS.setPkName(currentInfo.getPkName());
            newDS.buildSchema();
            datasources.add(newDS);
        }
        return datasources;
    }

    private List<AbstractJDBCDataSource> createLayerDatasources() throws SQLException {
        List<ConnectionInformation> infos = this.tableSelectionPanel.getSelectedObjects();
        ArrayList<AbstractJDBCDataSource> datasources = new ArrayList<AbstractJDBCDataSource>();
        for (ConnectionInformation currentInfo : infos) {
            AbstractJDBCDataSource newDS = (AbstractJDBCDataSource)this.baseLayerDatasource.clone();
            if (this.dataBaseTypeComboBox.getSelectedItem() == "PostgreSQL" && currentInfo.isOptimizedLoading()) {
                newDS = new ExtendPostGisDataSource(this.baseLayerDatasource.getHostName(), this.baseLayerDatasource.getPort(), this.baseLayerDatasource.getDataBase(), this.baseLayerDatasource.getUserName(), this.baseLayerDatasource.getPassword());
            }
            newDS.setDataBaseSchema(currentInfo.getSchemaName());
            newDS.setTableName(currentInfo.getTableName());
            newDS.setPkName(currentInfo.getPkName());
            newDS.setGeomColName(currentInfo.getGeometryColumnName());
            try {
                newDS.initialize(false);
            }
            catch (UnknownGeometryTypeException ugte) {
                newDS.initialize(true);
                int option = DialogFactory.showYesNoCancelWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.Geometry-type-can-not-be-determined-do-you-want-to-select-it"), String.valueOf(I18N.getString("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.Select-geometry-type")) + " - " + currentInfo.getTableName());
                if (option == 0) {
                    SelectGeometryTypeDialog selectDialog = new SelectGeometryTypeDialog(JUMPWorkbench.getFrameInstance(), true, false, currentInfo.getTableName());
                    GUIUtil.centreOnWindow(selectDialog);
                    selectDialog.setVisible(true);
                    int geomType = selectDialog.getGeometryType();
                    boolean is3d = selectDialog.is3D();
                    if (geomType == 0) {
                        throw new SQLException(I18N.getString("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.Geometry-type-can-not-be-determined"));
                    }
                    newDS.getSchema().setGeometryType(geomType);
                    newDS.set3d(is3d);
                }
                throw new SQLException(I18N.getString("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.Geometry-type-can-not-be-determined"));
            }
            datasources.add(newDS);
        }
        return datasources;
    }

    public void initializeTable() throws Exception {
        if (CollectionUtils.isEmpty(this.tableSelectionPanel.getSelectedObjects())) {
            throw new Exception(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.there-is-not-selected-table"));
        }
        if (this.isLayer) {
            this.layerDatasources = this.createLayerDatasources();
        } else {
            this.tableDatasources = this.createTableDatasources();
        }
    }

    public void refresh() {
        String selected = (String)this.dataBaseTypeComboBox.getSelectedItem();
        this.dataBaseComboBox.removeAllItems();
        this.schemaComboBox.removeAllItems();
        this.tableSelectionPanel.clearList();
        this.pkColumnsComboBox.removeAllItems();
        this.geometryColumnsComboBox.removeAllItems();
        this.usernameTextField.setText("");
        this.passwordTextField.setText("");
        this.advancedPostGisCheckbox.setSelected(false);
        if ("PostgreSQL".equals(selected)) {
            this.advancedPostGisCheckbox.setEnabled(true);
        } else {
            this.advancedPostGisCheckbox.setEnabled(false);
        }
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
                this.checkConnection();
            }
        }
    }

    public void initializeInputVerifiers() {
        if (this.parent == null) {
            this.parent = (JDialog)SwingUtilities.getAncestorOfClass(JDialog.class, this);
            this.hostTextField.setInputVerifier(new NullTextFieldValidator(this.parent, this.hostTextField));
            ArrayList<AbstractValidator> validators = new ArrayList<AbstractValidator>();
            validators.add(new NullTextFieldValidator(this.parent, this.portTextField));
            validators.add(new GreaterThanTextFieldValidator(this.parent, (JComponent)this.portTextField, 0.0));
            this.portTextField.setInputVerifier(new MultipleValidator(this.parent, (JComponent)this.portTextField, validators));
            this.dataBaseComboBox.setInputVerifier(new NullComboBoxValidator(this.parent, this.dataBaseComboBox));
            this.usernameTextField.setInputVerifier(new NullTextFieldValidator(this.parent, this.usernameTextField));
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.databaseTypeLabel.setEnabled(enabled);
        this.dataBaseTypeComboBox.setEnabled(enabled);
        this.instructionsLabel.setEnabled(enabled);
        this.hostLabel.setEnabled(enabled);
        this.hostTextField.setEnabled(enabled);
        this.portLabel.setEnabled(enabled);
        this.portTextField.setEnabled(enabled);
        this.dataBaseLabel.setEnabled(enabled);
        this.dataBaseComboBox.setEnabled(enabled);
        this.usernameLabel.setEnabled(enabled);
        this.usernameTextField.setEnabled(enabled);
        this.passwordLabel.setEnabled(enabled);
        this.passwordTextField.setEnabled(enabled);
        this.testConnectionButton.setEnabled(enabled);
        this.tableSelectionPanel.setEnabled(enabled);
        this.schemaComboBox.setEnabled(enabled);
        this.pkColumnsComboBox.setEnabled(enabled);
        this.geometryColumnsComboBox.setEnabled(enabled);
        this.advancedPostGisCheckbox.setEnabled(enabled);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        block31: {
            if (e.getSource() == this.advancedPostGisCheckbox) {
                try {
                    if (!this.isLayer) break block31;
                    this.baseLayerDatasource = this.createBaseLayerDataSource();
                    ConnectionInformation info = this.tableSelectionPanel.getSelectedItem();
                    if (info != null) {
                        info.setOptimizedLoading(this.advancedPostGisCheckbox.isSelected());
                    }
                }
                catch (Exception e1) {
                    LOGGER.error((Object)"", (Throwable)e1);
                    DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString(this.getClass(), "unexpected-error-occurred-check-system-log"), I18N.getString(this.getClass(), "connection-error"));
                }
            } else if (e.getSource() == this.testConnectionButton) {
                final TaskMonitorDialog progressDialog = new TaskMonitorDialog((Frame)JUMPWorkbench.getFrameInstance(), null);
                progressDialog.setTitle(I18N.getString("ui.plugin.wms.URLWizardPanel.connecting"));
                progressDialog.addComponentListener(new ComponentAdapter(){

                    @Override
                    public void componentShown(ComponentEvent e) {
                        new Thread(new Runnable(){

                            @Override
                            public void run() {
                                try {
                                    try {
                                        progressDialog.report(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.Checking-connection-parameters")) + "...");
                                        if (!JDBCPropertiesPanel.this.checkParameters()) {
                                            DialogFactory.showErrorDialog(JDBCPropertiesPanel.this.testConnectionButton.getRootPane(), I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.check-your-parameters"), I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.test-connection-error"));
                                        } else {
                                            progressDialog.report(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.Stablishing-the-connection-with-the-database")) + "...");
                                            if (!JDBCPropertiesPanel.this.checkConnection()) {
                                                DialogFactory.showErrorDialog(JDBCPropertiesPanel.this.testConnectionButton.getRootPane(), I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.connection-error-check-your-parameters"), I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.test-connection-error"));
                                            } else {
                                                DialogFactory.showInformationDialog(JDBCPropertiesPanel.this.testConnectionButton.getRootPane(), I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.connection-successfull"), I18N.getString("org.saig.jump.widgets.datasource.JDBCPropertiesPanel.test-connection-success"));
                                            }
                                        }
                                    }
                                    catch (Exception e) {
                                        LOGGER.error((Object)"", (Throwable)e);
                                        progressDialog.setExceptionMessage(e.getMessage());
                                        progressDialog.setVisible(false);
                                        return;
                                    }
                                }
                                finally {
                                    progressDialog.setVisible(false);
                                }
                            }
                        }).start();
                    }
                });
                GUIUtil.centre(progressDialog, this.getRootPane().getParent());
                progressDialog.setVisible(true);
                this.firePanelChanged();
            } else if (e.getSource() == this.dataBaseComboBox) {
                String selected = (String)this.dataBaseTypeComboBox.getSelectedItem();
                String dataBaseSelected = (String)this.dataBaseComboBox.getSelectedItem();
                if (dataBaseSelected == null || dataBaseSelected.equals("")) {
                    return;
                }
                if (((DefaultComboBoxModel)this.dataBaseComboBox.getModel()).getIndexOf(dataBaseSelected) == -1) {
                    ((DefaultComboBoxModel)this.dataBaseComboBox.getModel()).addElement(dataBaseSelected);
                }
                this.tableSelectionPanel.clearList();
                this.schemaComboBox.removeAllItems();
                this.pkColumnsComboBox.removeAllItems();
                this.geometryColumnsComboBox.removeAllItems();
                this.usernameTextField.setText("");
                this.passwordTextField.setText("");
                String[] properties = DataBaseConnectionFactory.getPropertiesConnection(selected, dataBaseSelected);
                if (properties == null) {
                    return;
                }
                this.usernameTextField.setText(properties[0]);
                this.passwordTextField.setText(properties[1]);
                this.hostTextField.setText(properties[2]);
                this.portTextField.setText(properties[3]);
                this.checkConnection();
            } else if (e.getSource() == this.dataBaseTypeComboBox) {
                this.refresh();
            } else if (e.getSource() == this.pkColumnsComboBox) {
                ConnectionInformation info = this.tableSelectionPanel.getSelectedItem();
                Field currentPkField = (Field)this.pkColumnsComboBox.getSelectedItem();
                if (info != null && currentPkField != null) {
                    info.setPkName(currentPkField.getName());
                }
            } else if (e.getSource() == this.geometryColumnsComboBox) {
                ConnectionInformation info = this.tableSelectionPanel.getSelectedItem();
                Field currentPkField = (Field)this.geometryColumnsComboBox.getSelectedItem();
                if (info != null && currentPkField != null) {
                    info.setGeometryColumnName(currentPkField.getName());
                }
            } else if (e.getSource() == this.schemaComboBox && !this.checkingConnection) {
                String selectedSchema = (String)this.schemaComboBox.getSelectedItem();
                ArrayList<ConnectionInformation> tables = new ArrayList<ConnectionInformation>();
                if (selectedSchema == null) {
                    return;
                }
                try {
                    if (selectedSchema.equals(SHOW_ALL_SCHEMAS)) {
                        List<String> schemas = null;
                        if (this.isLayer) {
                            this.baseLayerDatasource = this.createBaseLayerDataSource();
                            schemas = this.baseLayerDatasource.getSchemas();
                        } else {
                            this.baseTableDatasource = this.createBaseTableDataSource();
                            schemas = this.baseTableDatasource.getSchemas();
                        }
                        if (CollectionUtils.isNotEmpty(schemas)) {
                            for (String currentSchema : schemas) {
                                tables.addAll(this.createConnectionInformationForSchema(currentSchema));
                            }
                        } else {
                            tables.addAll(this.createConnectionInformationForSchema(""));
                        }
                    } else {
                        tables.addAll(this.createConnectionInformationForSchema(selectedSchema));
                    }
                    this.tableSelectionPanel.setListObjects(tables);
                }
                catch (Exception ex) {
                    LOGGER.error((Object)"", (Throwable)ex);
                    this.baseLayerDatasource = null;
                    this.baseTableDatasource = null;
                    this.layerDatasources = null;
                    this.tableDatasources = null;
                    this.tableSelectionPanel.clearList();
                    this.schemaComboBox.removeAllItems();
                    this.pkColumnsComboBox.removeAllItems();
                    this.geometryColumnsComboBox.removeAllItems();
                    return;
                }
            }
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        this.refreshTableSelection();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == this.tableSelectionPanel) {
            this.refreshTableSelection();
        }
    }

    protected void refreshTableSelection() {
        ConnectionInformation info = this.tableSelectionPanel.getSelectedItem();
        if (info == null) {
            return;
        }
        boolean enabled = this.tableSelectionPanel.getSelectedObjects().contains(info);
        String tableSelected = info.getTableName();
        String schemaName = info.getSchemaName();
        String pkName = info.getPkName();
        String geometryColumnName = info.getGeometryColumnName();
        boolean optimizedLoadingSelected = info.isOptimizedLoading();
        if (StringUtils.isEmpty((String)tableSelected)) {
            return;
        }
        ((TitledBorder)this.tableOptionsPanel.getBorder()).setTitle(String.valueOf(BASE_TITLE_BORDER) + " - " + tableSelected);
        this.tableOptionsPanel.repaint();
        this.pkColumnsComboBox.removeAllItems();
        this.geometryColumnsComboBox.removeAllItems();
        this.advancedPostGisCheckbox.setSelected(optimizedLoadingSelected);
        if (enabled) {
            List<Field> columns = info.getFields();
            try {
                if (CollectionUtils.isEmpty(columns)) {
                    columns = this.isLayer ? this.baseLayerDatasource.getCandidateFields(schemaName, tableSelected) : this.baseTableDatasource.getCandidateFields(schemaName, tableSelected);
                    info.setFields(columns);
                }
                Field pkField = null;
                for (Field currentField : info.getCandidatePkFields()) {
                    this.pkColumnsComboBox.addItem(currentField);
                    if (pkName != null && currentField.getName().equals(pkName)) {
                        pkField = currentField;
                        continue;
                    }
                    if (!currentField.isPrimaryKey()) continue;
                    pkField = currentField;
                }
                if (pkField != null) {
                    this.pkColumnsComboBox.setSelectedItem(pkField);
                }
                if (this.isLayer) {
                    Field geomColumnNameField = null;
                    for (Field currentField : info.getCandidateGeometryFields()) {
                        this.geometryColumnsComboBox.addItem(currentField);
                        if (geometryColumnName != null && currentField.getName().equals(geometryColumnName)) {
                            geomColumnNameField = currentField;
                            continue;
                        }
                        if (!currentField.isPrimaryKey()) continue;
                        geomColumnNameField = currentField;
                    }
                    if (geomColumnNameField != null) {
                        this.geometryColumnsComboBox.setSelectedItem(geomColumnNameField);
                    }
                }
            }
            catch (Exception e1) {
                LOGGER.error((Object)"", (Throwable)e1);
                return;
            }
        }
        this.pkColumnsComboBox.setEnabled(enabled);
        if (this.isLayer) {
            this.geometryColumnsComboBox.setEnabled(enabled);
            this.advancedPostGisCheckbox.setEnabled(enabled && this.getDatabaseTypeComboBox().getSelectedItem() == "PostgreSQL");
        }
    }

    public AbstractJDBCDataSource getBaseLayerDatasource() {
        return this.baseLayerDatasource;
    }

    public TableDBRecordDataSource getBaseTableDatasource() {
        return this.baseTableDatasource;
    }
}

