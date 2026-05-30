/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.toedter.calendar.IDateEditor
 *  com.toedter.calendar.JDateChooser
 *  com.toedter.calendar.JDateChooserCellEditor
 *  com.toedter.calendar.JSpinnerDateEditor
 *  com.toedter.calendar.JTextFieldDateEditor
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets.tables.management;

import com.pcauto.gui.table.AbstractEntityTableColumnModel;
import com.pcauto.gui.table.EntityJTable;
import com.pcauto.gui.table.EntityList;
import com.pcauto.gui.table.EntityListException;
import com.pcauto.gui.table.EntityTable;
import com.pcauto.gui.table.EntityTableColumn;
import com.pcauto.gui.table.EntityTableColumnModel;
import com.pcauto.gui.table.EntityTableFocusType;
import com.pcauto.gui.table.ProxyEntityList;
import com.toedter.calendar.IDateEditor;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JDateChooserCellEditor;
import com.toedter.calendar.JSpinnerDateEditor;
import com.toedter.calendar.JTextFieldDateEditor;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import org.apache.log4j.Logger;
import org.saig.core.context.GenericContext;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.dataComponents.layers.JLayerComboBox;
import org.saig.core.gui.swing.dataComponents.tables.JTableWithDataSourceComboBox;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.export.FeatureCollectionToCalc;
import org.saig.core.model.data.dao.export.OpenOfficeLibLoader;
import org.saig.core.model.data.widgets.tables.management.EntityTableListener;
import org.saig.core.model.data.widgets.tables.management.PagesNavigationListener;
import org.saig.core.model.data.widgets.tables.management.PagesNavigationPanel;
import org.saig.core.model.data.widgets.tables.management.TableFilterPanel;
import org.saig.core.model.data.widgets.tables.management.combo.ExtendedDefaultCellEditor;
import org.saig.core.model.data.widgets.tables.management.combo.ExtendedDefaultCellRenderer;
import org.saig.core.model.data.widgets.tables.management.combo.RelationData;
import org.saig.core.model.data.widgets.tables.management.control.AdvancedControlPanel;
import org.saig.core.model.data.widgets.tables.management.definition.Column;
import org.saig.core.model.data.widgets.tables.management.definition.TableDef;
import org.saig.core.model.data.widgets.tables.management.filter.SQLFilterBuilderDialog;
import org.saig.core.model.data.widgets.tables.management.navigation.INavigationHelper;
import org.saig.core.model.data.widgets.tables.management.navigation.NavigationHelperFactory;
import org.saig.core.model.data.widgets.tables.management.operations.OperationsManager;
import org.saig.core.model.feature.Attribute;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.AbstractWaitDialog;
import org.saig.jump.widgets.util.DateCellRenderer;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.SpinnerNumberAndNullModel;

public class AdvancedTableManagementPanel
extends JPanel
implements PagesNavigationListener {
    protected static final Logger LOGGER = Logger.getLogger(AdvancedTableManagementPanel.class);
    protected EntityTableColumnModel columnModel = null;
    protected EntityTable mainTable;
    protected AdvancedControlPanel controlPanel;
    protected PagesNavigationPanel navigationPanel;
    protected JButton filterButton;
    protected OperationsManager manager;
    protected FeatureSchema fs;
    protected boolean pkEditable = true;
    protected TableDef tableDef;
    private String tableName;
    protected SQLFilterBuilderDialog filterBuilderDialog;
    private JButton tableButton;
    private JButton formButton;
    private JScrollPane formPanel;
    private Object currentRecord;
    private JButton buttonReport;
    private JPanel buttonPanel;
    private JInternalFrame parent;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private String cardLayoutActualIndex;
    private static final String CARDLAYOUT_TABLE_INDEX = "CARDLAYOUT_TABLE_INDEX";
    private static final String CARDLAYOUT_FORM_INDEX = "CARDLAYOUT_FORM_INDEX";
    protected boolean navigating = false;
    private TableFilterPanel tableFilterPanel;
    private boolean notEditable;
    private INavigationHelper navHelper;
    private final boolean useCache;

    public AdvancedTableManagementPanel() {
        this.useCache = false;
    }

    public AdvancedTableManagementPanel(String tableName, AdvancedControlPanel control, TableDef tableDef, JInternalFrame parent, boolean notEditable) throws Exception {
        this(tableName, control, tableDef, parent, notEditable, false);
    }

    public AdvancedTableManagementPanel(String tableName, AdvancedControlPanel control, TableDef tableDef, JInternalFrame parent, boolean notEditable, boolean useCache) throws Exception {
        this.useCache = useCache;
        this.notEditable = notEditable;
        this.parent = parent;
        this.tableName = tableName;
        this.tableDef = tableDef;
        if (tableDef != null) {
            this.pkEditable = tableDef.isPkEditable();
        }
        this.manager = new OperationsManager(tableName, tableDef);
        this.navHelper = NavigationHelperFactory.getNavigationHelper(tableName);
        this.controlPanel = control;
        this.controlPanel.setManager(this.manager);
        this.mainTable = new EntityTable();
        this.fs = this.manager.getSchema();
        this.filterBuilderDialog = new SQLFilterBuilderDialog(this.fs, GenericContext.getGenericContext().getTableDataSource(tableName));
        this.defineColumnModel();
        this.initNavigationPanel();
        this.loadData();
        this.controlPanel.setTablePanel(this);
        this.createFormPanel();
        this.initComponents();
        this.addComponents();
        EntityJTable table = (EntityJTable)this.mainTable.getScrollPane().getViewport().getComponent(0);
        table.setDefaultRenderer(Class.forName("java.lang.Object"), new NoEditableRenderer());
        table.setDefaultRenderer(Number.class, new NoEditableRenderer());
        table.setDefaultRenderer(Date.class, new NoEditableDateRenderer());
        table.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent me) {
                EntityJTable table = (EntityJTable)me.getSource();
                int selRow = table.getSelectedRow();
                AdvancedTableManagementPanel.this.formButton.setEnabled(true);
                AdvancedTableManagementPanel.this.tableButton.setEnabled(false);
                if (me.getClickCount() == 2) {
                    AdvancedTableManagementPanel.this.controlPanel.setEditing(true);
                    AdvancedTableManagementPanel.this.controlPanel.evaluateButtons();
                } else if (me.getClickCount() == 1) {
                    if (selRow + 1 != AdvancedTableManagementPanel.this.navigationPanel.getActualRecord()) {
                        AdvancedTableManagementPanel.this.mainTable.setCurrentFocusMode(EntityTableFocusType.ROW_FOCUS);
                        AdvancedTableManagementPanel.this.mainTable.getSelectionModel().setSelectionInterval(selRow, selRow);
                        AdvancedTableManagementPanel.this.controlPanel.setEditing(false);
                        try {
                            AdvancedTableManagementPanel.this.manager.doOperations();
                        }
                        catch (Exception e) {
                            LOGGER.error((Object)"", (Throwable)e);
                        }
                    }
                    AdvancedTableManagementPanel.this.controlPanel.evaluateButtons();
                }
                AdvancedTableManagementPanel.this.navigationPanel.setActualRecord(selRow + 1);
            }
        });
        this.resizeColumns();
    }

    public AdvancedTableManagementPanel(Layer layer, AdvancedControlPanel control, TableDef tableDef, JInternalFrame parent, boolean notEditable) throws Exception {
        this(layer, control, tableDef, parent, notEditable, false);
    }

    public AdvancedTableManagementPanel(Layer layer, AdvancedControlPanel control, TableDef tableDef, JInternalFrame parent, boolean notEditable, boolean useCache) throws Exception {
        this.useCache = useCache;
        this.notEditable = notEditable;
        this.parent = parent;
        this.tableDef = tableDef;
        if (tableDef != null) {
            this.pkEditable = tableDef.isPkEditable();
        }
        this.manager = new OperationsManager(layer, tableDef);
        List<Feature> featList = layer.getUltimateFeatureCollectionWrapper().getFeatures();
        ArrayList<Feature> featListCopy = new ArrayList<Feature>();
        featListCopy.addAll(featList);
        this.navHelper = NavigationHelperFactory.getNavigationHelper(featListCopy);
        this.controlPanel = control;
        this.controlPanel.setManager(this.manager);
        this.mainTable = new EntityTable();
        this.fs = this.manager.getSchema();
        this.filterBuilderDialog = new SQLFilterBuilderDialog(this.fs, GenericContext.getGenericContext().getTableDataSource(this.tableName));
        this.defineColumnModel();
        this.initNavigationPanel();
        this.loadData();
        this.controlPanel.setTablePanel(this);
        this.createFormPanel();
        this.initComponents();
        this.addComponents();
        EntityJTable table = (EntityJTable)this.mainTable.getScrollPane().getViewport().getComponent(0);
        table.setDefaultRenderer(Class.forName("java.lang.Object"), new NoEditableRenderer());
        table.setDefaultRenderer(Number.class, new NoEditableRenderer());
        table.setDefaultRenderer(Date.class, new NoEditableDateRenderer());
        table.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent me) {
                EntityJTable table = (EntityJTable)me.getSource();
                int selRow = table.getSelectedRow();
                AdvancedTableManagementPanel.this.formButton.setEnabled(true);
                AdvancedTableManagementPanel.this.tableButton.setEnabled(false);
                if (me.getClickCount() == 2) {
                    AdvancedTableManagementPanel.this.controlPanel.setEditing(true);
                    AdvancedTableManagementPanel.this.controlPanel.evaluateButtons();
                } else if (me.getClickCount() == 1) {
                    if (selRow + 1 != AdvancedTableManagementPanel.this.navigationPanel.getActualRecord()) {
                        AdvancedTableManagementPanel.this.mainTable.setCurrentFocusMode(EntityTableFocusType.ROW_FOCUS);
                        AdvancedTableManagementPanel.this.mainTable.getSelectionModel().setSelectionInterval(selRow, selRow);
                        AdvancedTableManagementPanel.this.controlPanel.setEditing(false);
                        try {
                            AdvancedTableManagementPanel.this.manager.doOperations();
                        }
                        catch (Exception e) {
                            LOGGER.error((Object)"", (Throwable)e);
                        }
                    }
                    AdvancedTableManagementPanel.this.controlPanel.evaluateButtons();
                }
                AdvancedTableManagementPanel.this.navigationPanel.setActualRecord(selRow + 1);
            }
        });
        this.resizeColumns();
    }

    public AdvancedTableManagementPanel(String tableName, AdvancedControlPanel control, TableDef tableDef, JInternalFrame parent) throws Exception {
        this(tableName, control, tableDef, parent, false, false);
    }

    private void createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        this.formPanel = new JScrollPane(panel);
        this.formPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "form-view")));
        List<String> atributos = this.fs.getAttributeNames();
        Iterator<String> it = atributos.iterator();
        int pos = 0;
        while (it.hasNext()) {
            String atributo = it.next();
            Attribute at = this.fs.getAttribute(atributo);
            JLabel etiqueta = new JLabel(atributo);
            JComponent componente = this.getComponentByAttribute(at, pos);
            FormUtils.addRowInGBL((JComponent)panel, pos, 0, etiqueta, componente, componente instanceof JScrollPane);
            ++pos;
        }
    }

    private void fillFormPanel(Record record) {
        this.fillFormPanel((Object)record);
    }

    private void fillFormPanel(Object obj) {
        if (obj != null) {
            if (obj instanceof Record) {
                this.currentRecord = obj;
                JPanel panel = (JPanel)this.formPanel.getViewport().getComponents()[0];
                Component[] components = panel.getComponents();
                int componentPos = 0;
                int tamComponent = components.length;
                Component first = null;
                int i = 0;
                while (i < ((Record)obj).getAttributes().size()) {
                    boolean asigned = false;
                    while (componentPos < tamComponent && !asigned) {
                        Component component = components[componentPos];
                        ++componentPos;
                        if (component instanceof JLabel) continue;
                        this.assignValueToComponent(((Record)obj).getAttribute(i), component);
                        asigned = true;
                        if (first != null) continue;
                        first = component;
                    }
                    ++i;
                }
                first.requestFocus();
                this.formPanel.getVerticalScrollBar().setValue(this.formPanel.getVerticalScrollBar().getMinimum());
            } else if (obj instanceof Feature) {
                this.currentRecord = obj;
                JPanel panel = (JPanel)this.formPanel.getViewport().getComponents()[0];
                Component[] components = panel.getComponents();
                int componentPos = 0;
                int tamComponent = components.length;
                Component first = null;
                int i = 0;
                while (i < ((Feature)obj).getAttributes().size()) {
                    boolean asigned = false;
                    while (componentPos < tamComponent && !asigned) {
                        Component component = components[componentPos];
                        ++componentPos;
                        if (component instanceof JLabel) continue;
                        this.assignValueToComponent(((Feature)obj).getAttribute(i), component);
                        asigned = true;
                        if (first != null) continue;
                        first = component;
                    }
                    ++i;
                }
                first.requestFocus();
                this.formPanel.getVerticalScrollBar().setValue(this.formPanel.getVerticalScrollBar().getMinimum());
            }
        }
    }

    private void assignValueToComponent(Object attribute, Component component) {
        if (component instanceof JDateChooser) {
            if (!(attribute instanceof Date)) {
                LOGGER.error((Object)I18N.getString(this.getClass(), "field-could-not-be-updated-value-was-not-a-date"));
                return;
            }
            JDateChooser comp = (JDateChooser)component;
            comp.setDate((Date)attribute);
        } else if (component instanceof JSpinner) {
            JSpinner comp = (JSpinner)component;
            comp.setValue(attribute);
        } else if (component instanceof JCheckBox) {
            JCheckBox comp = (JCheckBox)component;
            if (attribute != null) {
                if (!(attribute instanceof Boolean)) {
                    LOGGER.error((Object)I18N.getString(this.getClass(), "value-was-not-a-boolean-uses-false-by-default"));
                }
                comp.setSelected((Boolean)attribute);
            } else {
                comp.setSelected(false);
            }
        } else if (component instanceof JScrollPane) {
            JScrollPane comp = (JScrollPane)component;
            JTextArea area = (JTextArea)comp.getViewport().getComponent(0);
            if (attribute != null) {
                area.setText(attribute.toString());
            } else {
                area.setText("");
            }
        }
    }

    private JComponent getComponentByAttribute(Attribute at, int pos) {
        Object campo;
        boolean isNotEditableField = this.isNotEditableField(pos);
        AttributeType type = at.getType();
        if (AttributeType.isDate(type)) {
            campo = new JDateChooser();
            this.setDimensions((JComponent)campo, new Dimension(90, 20));
            IDateEditor dateEditor = campo.getDateEditor();
            PropertyChangeListener pcl = new PropertyChangeListener(){

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    AdvancedTableManagementPanel.this.notifyFormComponentStateChanged();
                }
            };
            dateEditor.addPropertyChangeListener("date", pcl);
            if (isNotEditableField) {
                if (dateEditor instanceof JSpinnerDateEditor) {
                    ((JSpinnerDateEditor)dateEditor).getEditor().setEnabled(false);
                } else if (dateEditor instanceof JTextFieldDateEditor) {
                    ((JTextFieldDateEditor)dateEditor).setEditable(false);
                }
            }
        } else if (type.equals(AttributeType.INTEGER) || type.equals(AttributeType.BIGINT)) {
            campo = new JSpinner(new SpinnerNumberAndNullModel(null, Integer.valueOf(-999999999), Integer.valueOf(999999999), (Number)1, 0));
            JComponent editor = ((JSpinner)campo).getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor)editor).getTextField().setEditable(true);
            }
            ((JSpinner)campo).addChangeListener(new ChangeListener(){

                @Override
                public void stateChanged(ChangeEvent e) {
                    AdvancedTableManagementPanel.this.notifyFormComponentStateChanged();
                }
            });
            if (isNotEditableField && editor instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor)editor).getTextField().setEditable(false);
            }
            this.setDimensions((JComponent)campo, new Dimension(90, 20));
        } else if (type.equals(AttributeType.DOUBLE) || type.equals(AttributeType.FLOAT)) {
            campo = new JSpinner(new SpinnerNumberAndNullModel(null, Double.valueOf(-9.99999999E8), Double.valueOf(9.99999999E8), (Number)1.0, 2));
            JComponent editor = ((JSpinner)campo).getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor)editor).getTextField().setEditable(true);
            }
            ((JSpinner)campo).addChangeListener(new ChangeListener(){

                @Override
                public void stateChanged(ChangeEvent e) {
                    AdvancedTableManagementPanel.this.notifyFormComponentStateChanged();
                }
            });
            if (isNotEditableField && editor instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor)editor).getTextField().setEditable(false);
            }
            this.setDimensions((JComponent)campo, new Dimension(90, 20));
        } else if (type.equals(AttributeType.BOOLEAN)) {
            campo = new JCheckBox();
            ((JCheckBox)campo).addItemListener(new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent ie) {
                    AdvancedTableManagementPanel.this.notifyFormComponentStateChanged();
                }
            });
            if (isNotEditableField) {
                ((JCheckBox)campo).setEnabled(false);
            }
        } else {
            JTextArea area = new JTextArea();
            JScrollPane areaScrollPane = new JScrollPane(area);
            areaScrollPane.setVerticalScrollBarPolicy(22);
            area.setRows(3);
            campo = areaScrollPane;
            area.addKeyListener(new KeyAdapter(){

                @Override
                public void keyTyped(KeyEvent ke) {
                    if (ke.getKeyCode() == 10 || ke.getKeyChar() == '\n') {
                        AdvancedTableManagementPanel.this.notifyFormComponentStateChanged();
                    }
                }
            });
            if (isNotEditableField) {
                area.setEditable(false);
            }
            area.addFocusListener(new FocusAndChangeListener(area));
        }
        ((JComponent)campo).setEnabled(false);
        return campo;
    }

    private boolean isNotEditableField(int pos) {
        boolean isNotEditable = false;
        if (this.notEditable || this.fs.getPrimaryKeyIndex() == pos && !this.pkEditable || this.fs.isVersionable() && this.manager.getVersionableFieldsPositions().contains(pos)) {
            isNotEditable = true;
        }
        return isNotEditable;
    }

    private void notifyFormComponentStateChanged() {
        if (!this.navigating) {
            this.modifyCurrentRecord();
            this.controlPanel.setEditing(true);
            this.controlPanel.evaluateButtons();
            this.manager.addUpdate(this.currentRecord);
        }
    }

    private void setDimensions(JComponent comp, Dimension dim) {
        comp.setMinimumSize(dim);
        comp.setMaximumSize(dim);
        comp.setPreferredSize(dim);
    }

    private void modifyCurrentRecord() {
        if (this.cardLayoutActualIndex.equals(CARDLAYOUT_TABLE_INDEX)) {
            return;
        }
        if (this.currentRecord == null) {
            return;
        }
        JPanel panel = (JPanel)this.formPanel.getViewport().getComponents()[0];
        Component[] components = panel.getComponents();
        int componentPos = 0;
        int tamComponent = components.length;
        int n = 0;
        if (this.currentRecord instanceof Record) {
            n = ((Record)this.currentRecord).getAttributes().size();
        } else if (this.currentRecord instanceof Feature) {
            n = ((Feature)this.currentRecord).getAttributes().size();
        }
        int i = 0;
        while (i < n) {
            boolean asigned = false;
            while (componentPos < tamComponent && !asigned) {
                Component component = components[componentPos];
                ++componentPos;
                if (component instanceof JLabel) continue;
                this.assignValueToAttribute(this.currentRecord, i, component);
                asigned = true;
            }
            ++i;
        }
    }

    private void assignValueToAttribute(Record record, int i, Component component) {
        this.assignValueToAttribute((Object)record, i, component);
    }

    private void assignValueToAttribute(Object obj, int i, Component component) {
        JTextArea area;
        Object comp;
        if (obj instanceof Record) {
            if (component instanceof JDateChooser) {
                comp = (JDateChooser)component;
                ((Record)obj).setAttribute(i, (Object)comp.getDate());
            } else if (component instanceof JSpinner) {
                comp = (JSpinner)component;
                ((Record)obj).setAttribute(i, ((JSpinner)comp).getValue());
            } else if (component instanceof JCheckBox) {
                comp = (JCheckBox)component;
                ((Record)obj).setAttribute(i, (Object)((AbstractButton)comp).isSelected());
            } else if (component instanceof JScrollPane) {
                comp = (JScrollPane)component;
                area = (JTextArea)((JScrollPane)comp).getViewport().getComponent(0);
                ((Record)obj).setAttribute(i, (Object)area.getText());
            }
        }
        if (obj instanceof Feature) {
            if (component instanceof JDateChooser) {
                comp = (JDateChooser)component;
                ((Feature)obj).setAttribute(i, (Object)comp.getDate());
            } else if (component instanceof JSpinner) {
                comp = (JSpinner)component;
                ((Feature)obj).setAttribute(i, ((JSpinner)comp).getValue());
            } else if (component instanceof JCheckBox) {
                comp = (JCheckBox)component;
                ((Feature)obj).setAttribute(i, (Object)((AbstractButton)comp).isSelected());
            } else if (component instanceof JScrollPane) {
                comp = (JScrollPane)component;
                area = (JTextArea)((JScrollPane)comp).getViewport().getComponent(0);
                ((Feature)obj).setAttribute(i, (Object)area.getText());
            }
        }
    }

    protected void resizeColumns() {
        this.mainTable.setWidthAutoCalculated(false);
        JTable jTable = (JTable)this.mainTable.getScrollPane().getViewport().getComponent(0);
        GUIUtil.chooseGoodColumnWidths(jTable);
        jTable.setMinimumSize(new Dimension(635, 150));
    }

    protected void defineColumnModel() {
        this.columnModel = new AbstractEntityTableColumnModel(){
            private static final long serialVersionUID = 1L;

            @Override
            public Object getCellValue(int col, Object entity) {
                return AdvancedTableManagementPanel.this.manager.getValue(col, entity);
            }

            @Override
            public void setCellValue(int col, Object entity, Object newValue) {
                if (entity != null) {
                    AdvancedTableManagementPanel.this.manager.setValue(col, entity, newValue);
                }
            }
        };
        Map<String, RelationData> relationMap = this.generateRelationMap();
        Map<Integer, String> indexMap = this.fs.getIndexAttributes();
        Set<Integer> keySet = indexMap.keySet();
        ArrayList<Integer> keyList = new ArrayList<Integer>(keySet);
        Collections.sort(keyList);
        Iterator it = keyList.iterator();
        while (it.hasNext()) {
            int pos = (Integer)it.next();
            if (!this.fs.getVisibility(pos).booleanValue() || this.fs.isAttributeCalculate(pos)) continue;
            RelationData relData = relationMap.get(this.fs.getAttributeName(pos));
            EntityTableColumn column = this.createColumn(relData, pos);
            this.columnModel.addColumn(column);
            if (this.notEditable) {
                this.columnModel.setEditable(pos, false);
                continue;
            }
            if ((this.fs.getPrimaryKeyIndex() != pos || this.pkEditable) && (!this.fs.isVersionable() || !this.manager.getVersionableFieldsPositions().contains(pos))) continue;
            this.columnModel.setEditable(pos, false);
        }
        this.mainTable.setColumnModel(this.columnModel);
    }

    private EntityTableColumn createColumn(RelationData relData, int pos) {
        Column columnDef;
        EntityTableColumn column = null;
        int size = 0;
        if (this.tableDef != null && (columnDef = this.tableDef.getColumn(this.fs.getAttributeName(pos))) != null) {
            size = columnDef.getSize();
        }
        if (relData != null) {
            column = this.createComboColumn(relData, pos, size);
        } else if (this.isDateTime(pos)) {
            if (size == 0) {
                size = 50;
            }
            column = new EntityTableColumn(this.fs.getPublicName(pos), this.fs.getAttributeType(pos).toJavaClass(), size, (TableCellEditor)new JDateChooserCellEditor(), new NoEditableDateRenderer());
        } else {
            column = size == 0 ? new EntityTableColumn(this.fs.getPublicName(pos), this.fs.getAttributeType(pos).toJavaClass()) : new EntityTableColumn(this.fs.getPublicName(pos), this.fs.getAttributeType(pos).toJavaClass(), size);
        }
        return column;
    }

    protected EntityTableColumn createComboColumn(RelationData relData, int pos, int size) {
        EntityTableColumn comboColumn = null;
        if (size == 0) {
            size = 100;
        }
        if (relData.getType().equals("table")) {
            JTableWithDataSourceComboBox combo = new JTableWithDataSourceComboBox(GenericContext.getGenericContext().getTableDataSource(relData.getTableName()), relData.getCode(), relData.getValue());
            comboColumn = new EntityTableColumn(this.fs.getPublicName(pos), this.fs.getAttributeType(pos).toJavaClass(), size, (TableCellEditor)new ExtendedDefaultCellEditor(combo), new ExtendedDefaultCellRenderer(combo, relData.getValue(), this.useCache));
        } else {
            JLayerComboBox combo = new JLayerComboBox(JUMPWorkbench.getLayer(relData.getTableName()), relData.getCode(), relData.getValue());
            comboColumn = new EntityTableColumn(this.fs.getPublicName(pos), this.fs.getAttributeType(pos).toJavaClass(), size, (TableCellEditor)new ExtendedDefaultCellEditor(combo), new ExtendedDefaultCellRenderer(combo, relData.getValue(), this.useCache));
        }
        return comboColumn;
    }

    private boolean isDateTime(int pos) {
        return Date.class.isAssignableFrom(this.fs.getAttributeType(pos).toJavaClass());
    }

    protected Map<String, RelationData> generateRelationMap() {
        HashMap<String, RelationData> map = new HashMap<String, RelationData>();
        if (this.tableDef != null) {
            Map<String, Column> relations = this.tableDef.getColumns();
            Iterator<String> it = relations.keySet().iterator();
            while (it.hasNext()) {
                Column column = relations.get(it.next());
                if (column.getRelationTable() == null) continue;
                String name = column.getName();
                String comboCode = column.getRelationField();
                String comboValue = column.getRelationFieldToShow();
                String comboTableName = column.getRelationTable();
                map.put(name, new RelationData(comboCode, comboValue, comboTableName));
            }
        }
        return map;
    }

    public void loadData() {
        this.loadData(false);
    }

    public void loadData(boolean applyFilter) {
        this.loadData(applyFilter, new String[]{this.manager.getSchema().getPrimaryKeyName()}, true);
    }

    public void loadData(boolean applyFilter, String[] colList, boolean ascending) {
        Filter filter = null;
        if (this.filterBuilderDialog != null && applyFilter) {
            filter = this.filterBuilderDialog.getFilter();
        }
        this.navHelper.setFilter(filter);
        this.navHelper.setAscendingOrdering(ascending);
        this.navHelper.setOrderBy(colList);
        this.navigationPanel.setTotalNumRecords(this.navHelper.getNumElements());
        this.reloadData();
    }

    public void reloadData() {
        ProxyEntityList testTableEntityList = new ProxyEntityList();
        Object defaultEntity = this.getDefaultEntity();
        testTableEntityList.setDefaultEntity(defaultEntity);
        int firstRecord = this.navigationPanel.getFirstRecordShown();
        int numElemPerPage = this.navigationPanel.getNumRecordsPerPage();
        try {
            List<Object> entitiesToShow = this.navHelper.getElements(firstRecord, numElemPerPage);
            int i = 0;
            while (i < entitiesToShow.size()) {
                testTableEntityList.addEntity(entitiesToShow.get(i));
                ++i;
            }
        }
        catch (EntityListException e) {
            LOGGER.error((Object)"", (Throwable)e);
            return;
        }
        this.mainTable.setEntityList(testTableEntityList);
        this.mainTable.getSelectionModel().setSelectionInterval(0, 0);
        if (this.formPanel != null) {
            Object obj;
            int i = this.navigationPanel.getActualRecord();
            if (i > this.navHelper.getNumElements()) {
                this.navigationPanel.setActualRecord(1);
                i = 1;
            }
            if (i > 1 && (obj = this.mainTable.getEntityList().getEntity(i - 1)) != null) {
                this.fillFormPanel(obj);
            }
        }
    }

    protected void initComponents() {
        this.setLayout(new BorderLayout());
        this.filterButton = new JButton(IconLoader.icon("navegacion_filtro.png"));
        this.filterButton.setToolTipText(I18N.getString(this.getClass(), "opens-filter-edition-window"));
        FilterActionListener filterButtonActionListener = new FilterActionListener();
        this.filterButton.addActionListener(filterButtonActionListener);
        this.tableFilterPanel = new TableFilterPanel(filterButtonActionListener, new DeleteFilterActionListener(), new EnabledFilterActionListener(), this.filterBuilderDialog);
        this.mainTable.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.core.model.data.widgets.tables.management.TableManagementPanel.data-table")));
        this.mainTable.setCurrentFocusMode(EntityTableFocusType.ROW_FOCUS);
        this.mainTable.setVirtualRowEnabled(false);
        this.mainTable.setVirtualColumnEnabled(true);
        this.mainTable.setSelectionMode(0);
        this.mainTable.setMinimumSize(new Dimension(700, 200));
        this.mainTable.setPreferredSize(new Dimension(700, 200));
        this.mainTable.setMaximumSize(new Dimension(700, 200));
        this.mainTable.addEntityTableListener(new EntityTableListener(){

            @Override
            public void lastRowReachedEventFired() {
            }

            @Override
            public void orderByColumnEventFired(int col, boolean ascending) {
                AdvancedTableManagementPanel.this.loadData(true, new String[]{AdvancedTableManagementPanel.this.columnModel.getColumnName(col)}, ascending);
            }
        });
        this.formPanel.setMinimumSize(new Dimension(700, 200));
        this.formPanel.setPreferredSize(new Dimension(700, 200));
        this.formPanel.setMaximumSize(new Dimension(700, 200));
        this.tableButton = new JButton(I18N.getString(this.getClass(), "table-view"));
        this.tableButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ev) {
                AdvancedTableManagementPanel.this.setView(AdvancedTableManagementPanel.CARDLAYOUT_TABLE_INDEX);
                AdvancedTableManagementPanel.this.modifyCurrentRecord();
                AdvancedTableManagementPanel.this.formButton.setEnabled(true);
                AdvancedTableManagementPanel.this.tableButton.setEnabled(false);
                AdvancedTableManagementPanel.this.manager.addUpdate(AdvancedTableManagementPanel.this.currentRecord);
                AdvancedTableManagementPanel.this.mainTable.getSelectionModel().setSelectionInterval(AdvancedTableManagementPanel.this.navigationPanel.getActualRecord() - 1, AdvancedTableManagementPanel.this.navigationPanel.getActualRecord() - 1);
            }
        });
        this.tableButton.setEnabled(false);
        this.formButton = new JButton(I18N.getString(this.getClass(), "from-view"));
        this.formButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AdvancedTableManagementPanel.this.refreshFormRecord();
                AdvancedTableManagementPanel.this.setView(AdvancedTableManagementPanel.CARDLAYOUT_FORM_INDEX);
                AdvancedTableManagementPanel.this.modifyCurrentRecord();
                AdvancedTableManagementPanel.this.formButton.setEnabled(false);
                AdvancedTableManagementPanel.this.tableButton.setEnabled(true);
            }
        });
        this.formButton.setEnabled(false);
        this.buttonReport = new JButton(I18N.getString(this.getClass(), "report"));
        this.buttonReport.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                if (AdvancedTableManagementPanel.this.currentRecord != null) {
                    AdvancedTableManagementPanel.this.modifyCurrentRecord();
                    AdvancedTableManagementPanel.this.manager.addUpdate(AdvancedTableManagementPanel.this.currentRecord);
                    try {
                        AdvancedTableManagementPanel.this.manager.doOperations();
                    }
                    catch (Exception ex) {
                        LOGGER.error((Object)"", (Throwable)ex);
                    }
                }
                AdvancedTableManagementPanel.this.loadData(AdvancedTableManagementPanel.this.tableFilterPanel.isEnabledFilter());
                AdvancedTableManagementPanel.this.reportButtonActionPerformed(evt);
            }
        });
    }

    protected void reportButtonActionPerformed(ActionEvent evt) {
        JFileChooser fileChooser = GUIUtil.createJFileChooserWithOverwritePrompting();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(I18N.getString(this.getClass(), "calc-files"), "ods");
        fileChooser.setFileFilter(filter);
        fileChooser.showSaveDialog(this);
        File file = fileChooser.getSelectedFile();
        if (file == null) {
            JUMPWorkbench.getFrameInstance().warnUser(I18N.getString(this.getClass(), "operation-cancelled"));
            return;
        }
        String path = file.getPath();
        if (!path.endsWith(".ods")) {
            path = path.concat(".ods");
        }
        FeatureSchema fs = this.getSchema();
        ArrayList<Object> data = new ArrayList<Object>();
        data.addAll(this.navHelper.getElements(1, this.navHelper.getNumElements()));
        try {
            OpenOfficeLibLoader.loadLibs();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        final FeatureCollectionToCalc fcToCalc = new FeatureCollectionToCalc(fs, data, path);
        new AbstractWaitDialog(JUMPWorkbench.getFrameInstance(), I18N.getString(this.getClass(), "generating-spreadsheet")){

            @Override
            protected void methodToPerform() {
                try {
                    fcToCalc.create(false);
                    JUMPWorkbench.getFrameInstance().warnUser(I18N.getString(this.getClass(), "report-successfully-generated"));
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString(this.getClass(), "an-error-related-to-openoffice-occurred-please-be-sure-that-you-have-openoffice-installed-version-two-or-greater-and-that-kosmo-is-correctly-configurated-to-use-it-file-configuration-")) + " correctamente configurado para usarlo (Archivo -> Configuraci\u00f3n)", I18N.getString(this.getClass(), "error"));
                }
            }
        }.setVisible(true);
    }

    protected void initNavigationPanel() {
        this.navigationPanel = new PagesNavigationPanel();
        this.navigationPanel.addPageNavigationListener(this);
    }

    protected void addComponents() {
        JToolBar filterToolBar = new JToolBar();
        filterToolBar.setFloatable(false);
        filterToolBar.addSeparator();
        filterToolBar.add(this.filterButton);
        filterToolBar.addSeparator();
        JPanel buttonPanel = new JPanel(new FlowLayout(0));
        buttonPanel.add((Component)((Object)this.controlPanel));
        buttonPanel.add(filterToolBar);
        buttonPanel.add(this.navigationPanel);
        JPanel viewTypePanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL((JComponent)viewTypePanel, 0, 0, (JComponent)this.formButton, false, false);
        FormUtils.addRowInGBL((JComponent)viewTypePanel, 0, 1, (JComponent)this.tableButton, false, false);
        FormUtils.addRowInGBL((JComponent)viewTypePanel, 0, 2, (JComponent)this.buttonReport, false, false);
        this.add((Component)buttonPanel, "North");
        this.add((Component)this.getCenterPanel(), "Center");
        this.add((Component)this.getButtonPanel(viewTypePanel), "South");
    }

    private JPanel getButtonPanel(JPanel viewTypePanel) {
        this.buttonPanel = new JPanel(new BorderLayout());
        this.buttonPanel.add((Component)viewTypePanel, "Center");
        this.buttonPanel.add((Component)this.navigationPanel.getInfoPanel(), "South");
        return this.buttonPanel;
    }

    private JPanel getCenterPanel() {
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(this.cardLayout);
        this.cardPanel.add((Component)this.mainTable, CARDLAYOUT_TABLE_INDEX);
        this.cardPanel.add((Component)this.formPanel, CARDLAYOUT_FORM_INDEX);
        this.setView(CARDLAYOUT_TABLE_INDEX);
        return this.cardPanel;
    }

    protected void setView(String viewKey) {
        this.cardLayout.show(this.cardPanel, viewKey);
        this.cardLayoutActualIndex = viewKey;
    }

    protected Object getDefaultEntity() {
        return this.manager.getNewEntity();
    }

    private List getDataList(String fieldOrdered, Filter filter) throws Exception {
        return this.manager.getDataList(fieldOrdered, filter);
    }

    private List getDataList() {
        return this.manager.getDataList();
    }

    public EntityTable getMainTable() {
        return this.mainTable;
    }

    public boolean hasOperations() {
        return this.manager.hasOperations();
    }

    public void refreshFormRecord() {
        Object obj;
        int selectedPos = this.mainTable.getSelectionModel().getMinSelectionIndex();
        if (selectedPos != -1 && (obj = this.mainTable.getEntityList().getEntity(selectedPos)) != null) {
            this.fillFormPanel(obj);
        }
    }

    @Override
    public void pagesNavigationEventFired(int eventType) {
        this.navigating = true;
        int actual = this.navigationPanel.getActualRecord();
        switch (eventType) {
            case 0: 
            case 1: 
            case 4: 
            case 5: {
                if (this.cardLayoutActualIndex.equals(CARDLAYOUT_FORM_INDEX)) {
                    try {
                        this.manager.doOperations();
                    }
                    catch (Exception ex) {
                        LOGGER.error((Object)"", (Throwable)ex);
                    }
                }
                if (actual > 0) {
                    this.mainTable.getSelectionModel().clearSelection();
                    this.mainTable.getSelectionModel().setSelectionInterval(actual - 1, actual - 1);
                }
                if (!this.cardLayoutActualIndex.equals(CARDLAYOUT_FORM_INDEX)) break;
                this.modifyCurrentRecord();
                this.refreshFormRecord();
                break;
            }
            case 2: 
            case 3: 
            case 6: 
            case 7: 
            case 8: {
                if (actual > 0) {
                    this.mainTable.getSelectionModel().clearSelection();
                    this.mainTable.getSelectionModel().setSelectionInterval(actual - 1, actual - 1);
                }
                if (this.cardLayoutActualIndex.equals(CARDLAYOUT_FORM_INDEX)) {
                    this.modifyCurrentRecord();
                    this.reloadData();
                    this.refreshFormRecord();
                } else {
                    this.reloadData();
                }
                this.reloadData();
                break;
            }
            case 9: {
                this.navigationPanel.setTotalNumRecords(this.navigationPanel.getTotalNumRecords() + 1);
                this.loadData(this.tableFilterPanel.isEnabledFilter());
                break;
            }
            case 10: {
                this.navigationPanel.setTotalNumRecords(this.navigationPanel.getTotalNumRecords() - 1);
                this.loadData(this.tableFilterPanel.isEnabledFilter());
            }
        }
        this.navigating = false;
    }

    public List<Object> getEntityListData() {
        EntityList el = this.mainTable.getEntityList();
        ArrayList<Object> data = new ArrayList<Object>();
        int i = 0;
        while (i < el.getCount()) {
            data.add(el.getEntity(i));
            ++i;
        }
        return data;
    }

    public FeatureSchema getSchema() {
        return this.fs;
    }

    public boolean isShowingTableView() {
        return this.cardLayoutActualIndex.equals(CARDLAYOUT_TABLE_INDEX);
    }

    public boolean isShowingFormView() {
        return this.cardLayoutActualIndex.equals(CARDLAYOUT_FORM_INDEX);
    }

    public void close() {
    }

    private class DeleteFilterActionListener
    implements ActionListener {
        private DeleteFilterActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AdvancedTableManagementPanel.this.filterBuilderDialog.clear();
            AdvancedTableManagementPanel.this.tableFilterPanel.enabledFilter(false);
            AdvancedTableManagementPanel.this.tableFilterPanel.refresh();
            AdvancedTableManagementPanel.this.buttonPanel.remove(AdvancedTableManagementPanel.this.tableFilterPanel);
            AdvancedTableManagementPanel.this.parent.pack();
            AdvancedTableManagementPanel.this.loadData(AdvancedTableManagementPanel.this.tableFilterPanel.isEnabledFilter());
        }
    }

    private class EnabledFilterActionListener
    implements ActionListener {
        private EnabledFilterActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBox enabledFilterCheckBox = (JCheckBox)e.getSource();
            AdvancedTableManagementPanel.this.loadData(enabledFilterCheckBox.isSelected());
        }
    }

    private class FilterActionListener
    implements ActionListener {
        private FilterActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (AdvancedTableManagementPanel.this.filterBuilderDialog == null) {
                return;
            }
            AdvancedTableManagementPanel.this.filterBuilderDialog.setVisible(true);
            if (AdvancedTableManagementPanel.this.filterBuilderDialog.wasOKPressed()) {
                AdvancedTableManagementPanel.this.tableFilterPanel.enabledFilter(true);
                AdvancedTableManagementPanel.this.tableFilterPanel.refresh();
                AdvancedTableManagementPanel.this.buttonPanel.add((Component)AdvancedTableManagementPanel.this.tableFilterPanel, "North");
                AdvancedTableManagementPanel.this.parent.pack();
            }
            AdvancedTableManagementPanel.this.loadData(AdvancedTableManagementPanel.this.tableFilterPanel.isEnabledFilter());
        }
    }

    private class FocusAndChangeListener
    implements FocusListener {
        String oldValue;
        JTextArea area;

        public FocusAndChangeListener(JTextArea area) {
            this.area = area;
        }

        @Override
        public void focusLost(FocusEvent fe) {
            if (this.oldValue == null || !this.oldValue.equals(this.area.getText())) {
                AdvancedTableManagementPanel.this.notifyFormComponentStateChanged();
            }
        }

        @Override
        public void focusGained(FocusEvent e) {
            this.oldValue = this.area.getText();
        }
    }

    protected class NoEditableDateRenderer
    extends DateCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = null;
            try {
                label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!AdvancedTableManagementPanel.this.columnModel.isEditable(column)) {
                    label = new JLabel(label.getText());
                    label.setBackground(Color.LIGHT_GRAY);
                    label.setFont(label.getFont().deriveFont(2));
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)e);
            }
            return label;
        }
    }

    protected class NoEditableRenderer
    extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = null;
            try {
                label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!AdvancedTableManagementPanel.this.columnModel.isEditable(column)) {
                    label = new JLabel(label.getText());
                    label.setBackground(Color.LIGHT_GRAY);
                    label.setFont(label.getFont().deriveFont(2));
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)e);
            }
            return label;
        }
    }
}

