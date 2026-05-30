/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.lang.StringUtils
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.SchemaTableModel;
import com.vividsolutions.jump.workbench.ui.WorkbenchToolBar;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.EditingPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.EditablePlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.locale.TranslatableSelectionDialog;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultFilterEditor;
import org.saig.jump.lang.I18N;

public class SchemaPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private JPanel jPanel3 = new JPanel();
    private JPanel jPanel1 = new JPanel();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JLabel statusLabel = new JLabel();
    private Layer layer;
    private Point currentClickPoint;
    private JPopupMenu popupMenu = new JPopupMenu();
    private GridBagLayout gridBagLayout2 = new GridBagLayout();
    private JPanel buttonPanel = new JPanel();
    private JButton applyButton = new JButton();
    private JCheckBox forceInvalidConversionsToNullCheckBox = new JCheckBox();
    private GridBagLayout gridBagLayout3 = new GridBagLayout();
    private JPanel jPanel2 = new JPanel();
    private boolean modified = false;
    private List<ActionListener> listeners = new ArrayList<ActionListener>();
    private JButton revertButton = new JButton();
    private BorderLayout borderLayout1 = new BorderLayout();
    private LayerListener layerListener;
    private Map<AttributeType, String> attributeNameToUserReadableName = new HashMap<AttributeType, String>();
    private WorkbenchToolBar toolBar = new WorkbenchToolBar(null){
        private static final long serialVersionUID = 1L;

        @Override
        public JButton addPlugIn(Icon icon, PlugIn plugIn, EnableCheck enableCheck, WorkbenchContext workbenchContext) {
            return super.addPlugIn(icon, SchemaPanel.this.addCleanUp(plugIn), enableCheck, workbenchContext);
        }
    };
    private EnableCheck basicEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            if (!SchemaPanel.this.layer.isEditable()) {
                return I18N.getString("workbench.ui.SchemaPanel.layer-must-be-editable");
            }
            if (SchemaPanel.this.rowsToActOn().length == 0) {
                return I18N.getString("workbench.ui.SchemaPanel.at-least-1-row-must-be-selected");
            }
            return null;
        }
    };
    private MyPlugIn insertPlugIn = new MyPlugIn(){

        @Override
        public String getName() {
            return I18N.getString("workbench.ui.SchemaPanel.insert");
        }

        @Override
        public Icon getIcon() {
            return GUIUtil.toSmallIcon(IconLoader.icon("Plus.gif"));
        }

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            SchemaPanel.this.markAsModified();
            SchemaPanel.this.getModel().insertBlankRow(SchemaPanel.this.rowsToActOn()[0]);
            return true;
        }
    };
    private MyPlugIn deletePlugIn = new MyPlugIn(){

        @Override
        public String getName() {
            return I18N.getString("workbench.ui.SchemaPanel.delete");
        }

        @Override
        public Icon getIcon() {
            return GUIUtil.toSmallIcon(IconLoader.icon("Delete.gif"));
        }

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            SchemaPanel.this.markAsModified();
            SchemaPanel.this.getModel().removeFields(SchemaPanel.this.rowsToActOn());
            return true;
        }
    };
    private MyPlugIn moveUpPlugIn = new MyPlugIn(){

        @Override
        public Icon getIcon() {
            return GUIUtil.toSmallIcon(IconLoader.icon("VCRUp.gif"));
        }

        @Override
        public String getName() {
            return I18N.getString("workbench.ui.SchemaPanel.move-field-up");
        }

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            SchemaPanel.this.markAsModified();
            SchemaPanel.this.move(CollectionUtil.itemsToMoveUp(SchemaPanel.this.getModel().getFields(), SchemaPanel.this.toFields(SchemaPanel.this.rowsToActOn())), -1);
            return true;
        }

        @Override
        public MultiEnableCheck createEnableCheck() {
            return super.createEnableCheck().add(new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    return SchemaPanel.this.min(SchemaPanel.this.rowsToActOn()) == 0 ? I18N.getString("workbench.ui.SchemaPanel.field-is-already-at-the-top") : null;
                }
            });
        }
    };
    private EditablePlugIn editablePlugIn;
    private MyPlugIn moveDownPlugIn = new MyPlugIn(){

        @Override
        public Icon getIcon() {
            return GUIUtil.toSmallIcon(IconLoader.icon("VCRDown.gif"));
        }

        @Override
        public String getName() {
            return I18N.getString("workbench.ui.SchemaPanel.move-field-down");
        }

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            SchemaPanel.this.markAsModified();
            SchemaPanel.this.move(CollectionUtil.itemsToMoveDown(SchemaPanel.this.getModel().getFields(), SchemaPanel.this.toFields(SchemaPanel.this.rowsToActOn())), 1);
            return true;
        }
    };
    private JScrollPane jScrollPane1 = new JScrollPane();
    private JTable table = new JTable();

    private PlugIn addCleanUp(final PlugIn plugIn) {
        return new PlugIn(){

            public String toString() {
                return plugIn.toString();
            }

            @Override
            public boolean execute(PlugInContext context) throws Exception {
                try {
                    boolean bl = plugIn.execute(context);
                    return bl;
                }
                finally {
                    SchemaPanel.this.setCurrentClickPoint(null);
                    SchemaPanel.this.updateComponents();
                }
            }

            @Override
            public void initialize(PlugInContext context) throws Exception {
                plugIn.initialize(context);
            }

            @Override
            public String getName() {
                return plugIn.getName();
            }

            @Override
            public void finish(PlugInContext context) {
            }

            @Override
            public Icon getIcon() {
                return null;
            }

            @Override
            public Icon getDisabledIcon() {
                return null;
            }

            @Override
            public EnableCheck getCheck() {
                return null;
            }
        };
    }

    public SchemaPanel(final Layer layer, EditingPlugIn editingPlugIn, WorkbenchContext context) {
        this.editablePlugIn = new EditablePlugIn(editingPlugIn);
        try {
            this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    SchemaPanel.this.updateComponents();
                }
            });
            this.layer = layer;
            this.initializeAttributeNameMap();
            this.jbInit();
            this.initPopupMenu(context);
            this.initToolBar(context);
            SchemaTableModel model = new SchemaTableModel(layer);
            this.setModel(model);
            this.layerListener = new LayerListener(){

                @Override
                public void categoryChanged(CategoryEvent e) {
                }

                @Override
                public void featuresChanged(FeatureEvent e) {
                }

                @Override
                public void layerChanged(LayerEvent e) {
                    if (e.getLayerable() != layer) {
                        return;
                    }
                    if (e.getType() == LayerEventType.METADATA_CHANGED) {
                        SchemaPanel.this.updateComponents();
                        SchemaPanel.this.repaint();
                    }
                }
            };
            layer.getLayerManager().addLayerListener(this.layerListener);
        }
        catch (Exception ex) {
            Assert.shouldNeverReachHere((String)ex.toString());
        }
    }

    private void initializeAttributeNameMap() {
        this.attributeNameToUserReadableName.put(AttributeType.STRING, I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.String"));
        this.attributeNameToUserReadableName.put(AttributeType.INTEGER, I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.Integer"));
        this.attributeNameToUserReadableName.put(AttributeType.LONG, I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.Long"));
        this.attributeNameToUserReadableName.put(AttributeType.FLOAT, I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.Float"));
        this.attributeNameToUserReadableName.put(AttributeType.DOUBLE, I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.Double"));
        this.attributeNameToUserReadableName.put(AttributeType.DATE, I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.Date"));
        this.attributeNameToUserReadableName.put(AttributeType.TIMESTAMP, I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.Timestamp"));
        this.attributeNameToUserReadableName.put(AttributeType.BOOLEAN, I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.Boolean-Yes-No"));
        this.attributeNameToUserReadableName.put(AttributeType.GEOMETRY, I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.Geometry"));
    }

    public void setModel(SchemaTableModel model) {
        this.table.setModel(model);
        this.table.getModel().addTableModelListener(new TableModelListener(){

            @Override
            public void tableChanged(TableModelEvent e) {
                SchemaPanel.this.updateComponents();
            }
        });
        this.initCellEditors();
        this.updateComponents();
    }

    private int[] rowsToActOn() {
        if (this.table.getSelectedRowCount() > 0) {
            return this.table.getSelectedRows();
        }
        if (this.getCurrentClickPoint() != null && this.table.rowAtPoint(this.getCurrentClickPoint()) != -1) {
            return new int[]{this.table.rowAtPoint(this.getCurrentClickPoint())};
        }
        return new int[0];
    }

    private void initToolBar(WorkbenchContext context) {
        this.toolBar.addPlugIn(this.insertPlugIn.getIcon(), this.insertPlugIn, this.insertPlugIn.createEnableCheck(), context);
        this.toolBar.addPlugIn(this.deletePlugIn.getIcon(), this.deletePlugIn, this.deletePlugIn.createEnableCheck(), context);
        this.toolBar.addPlugIn(this.moveUpPlugIn.getIcon(), this.moveUpPlugIn, this.moveUpPlugIn.createEnableCheck(), context);
        this.toolBar.addPlugIn(this.moveDownPlugIn.getIcon(), this.moveDownPlugIn, this.moveDownPlugIn.createEnableCheck(), context);
    }

    private void initPopupMenu(WorkbenchContext context) {
        this.table.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseReleased(MouseEvent e) {
                SchemaPanel.this.setCurrentClickPoint(e.getPoint());
                if (SwingUtilities.isRightMouseButton(e)) {
                    SchemaPanel.this.popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        this.addPopupMenuItem(this.editablePlugIn, true, null, EditablePlugIn.createEnableCheck(context), context);
        this.popupMenu.addSeparator();
        this.addPopupMenuItem(this.insertPlugIn, false, this.insertPlugIn.getIcon(), this.insertPlugIn.createEnableCheck(), context);
        this.addPopupMenuItem(this.deletePlugIn, false, this.deletePlugIn.getIcon(), this.deletePlugIn.createEnableCheck(), context);
        this.popupMenu.addSeparator();
        this.addPopupMenuItem(this.moveUpPlugIn, false, this.moveUpPlugIn.getIcon(), this.moveUpPlugIn.createEnableCheck(), context);
        this.addPopupMenuItem(this.moveDownPlugIn, false, this.moveDownPlugIn.getIcon(), this.moveDownPlugIn.createEnableCheck(), context);
    }

    private void addPopupMenuItem(PlugIn plugIn, boolean checkBox, Icon icon, EnableCheck enableCheck, WorkbenchContext context) {
        FeatureInstaller installer = new FeatureInstaller(context);
        installer.addPopupMenuItem(this.popupMenu, this.addCleanUp(plugIn), plugIn.getName(), checkBox, icon, enableCheck);
    }

    public boolean isModified() {
        return this.modified;
    }

    private Collection<SchemaTableModel.Field> toFields(int[] rows) {
        ArrayList<SchemaTableModel.Field> fields = new ArrayList<SchemaTableModel.Field>();
        int i = 0;
        while (i < rows.length) {
            fields.add(this.getModel().get(rows[i]));
            ++i;
        }
        return fields;
    }

    private void updateComponents() {
        this.table.setShowGrid(this.layer.isEditable());
        this.forceInvalidConversionsToNullCheckBox.setEnabled(this.layer.isEditable());
        this.reportError(this.validateInput());
        this.toolBar.updateEnabledState();
    }

    public SchemaTableModel getModel() {
        return (SchemaTableModel)this.table.getModel();
    }

    private TableColumn fieldNameColumn() {
        return this.table.getColumnModel().getColumn(this.getModel().indexOfColumn(SchemaTableModel.FIELD_NAME_COLUMN_NAME));
    }

    private TableColumn dataTypeColumn() {
        return this.table.getColumnModel().getColumn(this.getModel().indexOfColumn(SchemaTableModel.DATA_TYPE_COLUMN_NAME));
    }

    private TableColumn publicNameColumn() {
        return this.table.getColumnModel().getColumn(this.getModel().indexOfColumn(SchemaTableModel.FIELD_PUBLIC_NAME_COLUMN_NAME));
    }

    private TableColumn visibilityColumn() {
        return this.table.getColumnModel().getColumn(this.getModel().indexOfColumn(SchemaTableModel.FIELD_VISIBILITY_COLUMN_NAME));
    }

    private void initCellEditors() {
        this.fieldNameColumn().setCellEditor(new MyFieldNameEditor());
        this.publicNameColumn().setCellEditor(new PublicNameCellEditor());
        this.dataTypeColumn().setCellEditor(new MyDataTypeEditor(AttributeType.basicTypes().toArray()));
        this.visibilityColumn().setCellEditor(new MyBooleanEditor(new Object[]{Boolean.FALSE, Boolean.TRUE}));
        this.fieldNameColumn().setCellRenderer(new StripingRenderer(this.table.getDefaultRenderer(String.class)));
        this.publicNameColumn().setCellRenderer(new PublicNameCellRenderer());
        this.visibilityColumn().setCellRenderer(new StripingRenderer(this.table.getDefaultRenderer(Boolean.class)));
        this.dataTypeColumn().setCellRenderer(new StripingRenderer(new TableCellRenderer(){

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Object temp = value;
                if (SchemaPanel.this.attributeNameToUserReadableName.containsKey(value)) {
                    temp = SchemaPanel.this.attributeNameToUserReadableName.get(value);
                }
                return table.getDefaultRenderer(String.class).getTableCellRendererComponent(table, temp != null ? SchemaPanel.this.capitalizeFirstLetter(temp.toString()) : null, isSelected, hasFocus, row, column);
            }
        }));
        this.table.getModel().addTableModelListener(new TableModelListener(){

            @Override
            public void tableChanged(TableModelEvent e) {
                int i = 0;
                while (i < SchemaPanel.this.table.getColumnCount()) {
                    SchemaPanel.this.table.getColumnModel().getColumn(i).getCellEditor().cancelCellEditing();
                    ++i;
                }
            }
        });
    }

    private String capitalizeFirstLetter(String string) {
        return String.valueOf(string.toUpperCase().charAt(0)) + string.toLowerCase().substring(1);
    }

    void jbInit() throws Exception {
        this.toolBar.setOrientation(1);
        this.setLayout(this.gridBagLayout2);
        this.jPanel1.setLayout(this.gridBagLayout1);
        this.statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        this.statusLabel.setText(" ");
        this.applyButton.setText(I18N.getString("workbench.ui.SchemaPanel.apply-changes"));
        this.applyButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                SchemaPanel.this.applyButton_actionPerformed(e);
            }
        });
        this.forceInvalidConversionsToNullCheckBox.setToolTipText(I18N.getString("workbench.ui.SchemaPanel.leave-unchecked-if-you-want-to-be-notified-of-conversion-errors"));
        this.forceInvalidConversionsToNullCheckBox.setText(I18N.getString("workbench.ui.SchemaPanel.force-invalidad-conversions-to-null"));
        this.buttonPanel.setLayout(this.gridBagLayout3);
        this.buttonPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        this.revertButton.setText(I18N.getString("workbench.ui.SchemaPanel.revert-changes"));
        this.revertButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                SchemaPanel.this.revertButton_actionPerformed(e);
            }
        });
        this.jPanel3.setLayout(this.borderLayout1);
        this.add((Component)this.jPanel3, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
        this.jPanel3.add((Component)this.toolBar, "West");
        this.jPanel3.add((Component)this.jScrollPane1, "Center");
        this.jScrollPane1.getViewport().add((Component)this.table, null);
        this.add((Component)this.jPanel1, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.jPanel1.add((Component)this.statusLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.buttonPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.buttonPanel.add((Component)this.applyButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(4, 4, 4, 4), 0, 0));
        this.buttonPanel.add((Component)this.forceInvalidConversionsToNullCheckBox, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.buttonPanel.add((Component)this.jPanel2, new GridBagConstraints(3, 1, 1, 1, 1.0, 0.0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.buttonPanel.add((Component)this.revertButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 4), 0, 0));
    }

    private void reportError(String message) {
        if (message != null) {
            this.statusLabel.setText(message);
            this.statusLabel.setIcon(GUIUtil.toSmallIcon(IconLoader.icon("Delete.gif")));
        } else {
            this.statusLabel.setText(" ");
            this.statusLabel.setIcon(null);
        }
    }

    private int geometryCount() {
        int geometryCount = 0;
        int i = 0;
        while (i < this.getModel().getRowCount()) {
            if (this.getModel().get(i).getType() == AttributeType.GEOMETRY) {
                ++geometryCount;
            }
            ++i;
        }
        return geometryCount;
    }

    public String validateInput() {
        String error = null;
        int i = 0;
        while (i < this.table.getColumnCount()) {
            IValidableCellEditor editor = (IValidableCellEditor)((Object)this.table.getColumnModel().getColumn(i).getCellEditor());
            error = editor.getCurrentErrorMessage();
            if (error != null) {
                return error;
            }
            ++i;
        }
        error = this.validateRows();
        if (this.geometryCount() == 0) {
            return I18N.getString("workbench.ui.SchemaPanel.a-geometry-field-must-be-defined");
        }
        return error;
    }

    private String validateRows() {
        String errorMessage = null;
        int i = 0;
        while (i < this.getModel().getRowCount() && StringUtils.isEmpty(errorMessage)) {
            SchemaTableModel.Field currentField = this.getModel().get(i);
            if (!StringUtils.isEmpty((String)currentField.getName()) || !StringUtils.isEmpty((String)currentField.getPublicName())) {
                if (StringUtils.isNotEmpty((String)currentField.getName()) && StringUtils.isEmpty((String)currentField.getPublicName())) {
                    errorMessage = I18N.getMessage("com.vividsolutions.jump.workbench.ui.SchemaPanel.You-must-insert-a-public-name-for-the-attribute-{0}", new Object[]{currentField.getName()});
                }
                if (StringUtils.isEmpty((String)currentField.getName()) && StringUtils.isNotEmpty((String)currentField.getPublicName())) {
                    errorMessage = I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.You-must-insert-a-name-for-the-field");
                }
            }
            ++i;
        }
        return errorMessage;
    }

    private String validate(int row, AttributeType type) {
        if (type == AttributeType.GEOMETRY) {
            int i = 0;
            while (i < this.getModel().getRowCount()) {
                if (i == row || this.getModel().get(i).getType() == null) {
                    // empty if block
                }
                ++i;
            }
        }
        return null;
    }

    private String validate(int row, String name) {
        if (name.trim().length() == 0) {
            return I18N.getString("workbench.ui.SchemaPanel.field-name-cannot-be-blank");
        }
        int i = 0;
        while (i < this.getModel().getRowCount()) {
            if (i != row && this.getModel().get(i).getName() != null && this.getModel().get(i).getName().equalsIgnoreCase(name.trim())) {
                return String.valueOf(I18N.getString("workbench.ui.SchemaPanel.field-name-already-exists")) + ": " + name;
            }
            ++i;
        }
        return null;
    }

    private void markAsModified() {
        this.modified = true;
    }

    public void markAsUnmodified() {
        this.modified = false;
    }

    public JTable getTable() {
        return this.table;
    }

    void applyButton_actionPerformed(ActionEvent e) {
        this.fireActionPerformed();
    }

    public void add(ActionListener l) {
        this.listeners.add(l);
    }

    private void fireActionPerformed() {
        for (ActionListener l : this.listeners) {
            l.actionPerformed(null);
        }
    }

    public boolean isForcingInvalidConversionsToNull() {
        return this.forceInvalidConversionsToNullCheckBox.isSelected();
    }

    public void move(Collection<SchemaTableModel.Field> fieldsToMove, int displacement) {
        int guaranteedVisibleRow = displacement > 0 ? this.max(this.rowsToActOn()) : this.min(this.rowsToActOn());
        guaranteedVisibleRow += displacement;
        ArrayList<SchemaTableModel.Field> selectedFields = new ArrayList<SchemaTableModel.Field>();
        int[] selectedRows = this.table.getSelectedRows();
        int i = 0;
        while (i < selectedRows.length) {
            selectedFields.add(this.getModel().get(selectedRows[i]));
            ++i;
        }
        this.getModel().move(fieldsToMove, displacement);
        this.table.clearSelection();
        for (SchemaTableModel.Field field : selectedFields) {
            this.table.addRowSelectionInterval(this.getModel().indexOf(field), this.getModel().indexOf(field));
        }
        Rectangle r = this.table.getCellRect(guaranteedVisibleRow, 0, true);
        this.table.scrollRectToVisible(r);
    }

    private int min(int[] ints) {
        int min = ints[0];
        int i = 0;
        while (i < ints.length) {
            min = Math.min(min, ints[i]);
            ++i;
        }
        return min;
    }

    private int max(int[] ints) {
        int max = ints[0];
        int i = 0;
        while (i < ints.length) {
            max = Math.max(max, ints[i]);
            ++i;
        }
        return max;
    }

    void revertButton_actionPerformed(ActionEvent e) {
        this.setModel(new SchemaTableModel(this.layer));
    }

    private void setCurrentClickPoint(Point currentClickPoint) {
        this.currentClickPoint = currentClickPoint;
    }

    private Point getCurrentClickPoint() {
        return this.currentClickPoint;
    }

    public LayerListener getLayerListener() {
        return this.layerListener;
    }

    static interface IValidableCellEditor {
        public String validate();

        public String getCurrentErrorMessage();
    }

    public class MyBooleanEditor
    extends MyEditor {
        private static final long serialVersionUID = 1L;
        private Boolean originalType;

        public MyBooleanEditor(Object[] items) {
            super(new JCheckBox());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.originalType = (Boolean)value;
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        private JCheckBox checkBox() {
            return (JCheckBox)this.getComponent();
        }

        @Override
        public boolean stopCellEditing() {
            Boolean value = new Boolean(this.checkBox().isSelected());
            if (this.originalType != value) {
                SchemaPanel.this.markAsModified();
            }
            return super.stopCellEditing();
        }

        @Override
        public String validate() {
            return null;
        }
    }

    public class MyDataTypeEditor
    extends MyEditor {
        private static final long serialVersionUID = 1L;
        private AttributeType originalType;

        public MyDataTypeEditor(Object[] items) {
            super(new JComboBox<Object>(items));
            final ListCellRenderer originalRenderer = this.comboBox().getRenderer();
            this.comboBox().setRenderer(new ListCellRenderer(){

                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Object temp = value;
                    if (SchemaPanel.this.attributeNameToUserReadableName.containsKey(value)) {
                        temp = SchemaPanel.this.attributeNameToUserReadableName.get(value);
                    }
                    return originalRenderer.getListCellRendererComponent(list, temp != null ? SchemaPanel.this.capitalizeFirstLetter(temp.toString()) : null, index, isSelected, cellHasFocus);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.originalType = (AttributeType)value;
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        private JComboBox comboBox() {
            return (JComboBox)this.getComponent();
        }

        @Override
        public boolean stopCellEditing() {
            if (this.originalType != this.comboBox().getSelectedItem()) {
                SchemaPanel.this.markAsModified();
            }
            return super.stopCellEditing();
        }

        @Override
        public String validate() {
            return SchemaPanel.this.validate(this.row, (AttributeType)this.comboBox().getSelectedItem());
        }
    }

    public abstract class MyEditor
    extends DefaultCellEditor
    implements IValidableCellEditor {
        private static final long serialVersionUID = 1L;
        protected int row;
        private String currentErrorMessage;

        public MyEditor(JComboBox comboBox) {
            super(comboBox);
            this.currentErrorMessage = null;
        }

        public MyEditor(JTextField textField) {
            super(textField);
            this.currentErrorMessage = null;
        }

        public MyEditor(JCheckBox checkBox) {
            super(checkBox);
            this.currentErrorMessage = null;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            ((JComponent)this.getComponent()).setBorder(new LineBorder(Color.black));
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        @Override
        public void cancelCellEditing() {
            this.currentErrorMessage = null;
            SchemaPanel.this.updateComponents();
            super.cancelCellEditing();
        }

        @Override
        public boolean stopCellEditing() {
            try {
                if (this.validate() != null) {
                    ((JComponent)this.getComponent()).setBorder(new LineBorder(Color.red));
                    return false;
                }
                boolean bl = super.stopCellEditing();
                return bl;
            }
            finally {
                this.currentErrorMessage = this.validate();
                SchemaPanel.this.updateComponents();
            }
        }

        @Override
        public abstract String validate();

        @Override
        public String getCurrentErrorMessage() {
            return this.currentErrorMessage;
        }
    }

    public class MyFieldNameEditor
    extends MyEditor {
        private static final long serialVersionUID = 1L;
        private String originalText;

        public MyFieldNameEditor() {
            super(new JTextField());
        }

        @Override
        public boolean stopCellEditing() {
            if (!this.textField().getText().equals(this.originalText)) {
                SchemaPanel.this.markAsModified();
            }
            return super.stopCellEditing();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.originalText = (String)value;
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        private JTextField textField() {
            return (JTextField)this.getComponent();
        }

        @Override
        public String validate() {
            return SchemaPanel.this.validate(this.row, this.textField().getText());
        }
    }

    private abstract class MyPlugIn
    extends AbstractPlugIn {
        private MyPlugIn() {
        }

        public MultiEnableCheck createEnableCheck() {
            return new MultiEnableCheck().add(SchemaPanel.this.basicEnableCheck);
        }

        @Override
        public abstract Icon getIcon();
    }

    private class PublicNameCellEditor
    extends AbstractCellEditor
    implements TableCellEditor,
    IValidableCellEditor {
        private static final long serialVersionUID = 1L;
        private PublicNameCellRenderer cellRenderer;
        private String currentErrorMessage;
        private SchemaTableModel.Field currentField;

        private PublicNameCellEditor() {
            this.cellRenderer = new PublicNameCellRenderer(this);
            this.currentErrorMessage = null;
        }

        @Override
        public Object getCellEditorValue() {
            return this.cellRenderer.getField();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentField = (SchemaTableModel.Field)value;
            this.cellRenderer.setField(this.currentField);
            return this.cellRenderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
        }

        @Override
        public String validate() {
            if (this.cellRenderer.getField() != null && StringUtils.isEmpty((String)this.cellRenderer.getField().getPublicName())) {
                this.currentErrorMessage = I18N.getString("com.vividsolutions.jump.workbench.ui.SchemaPanel.You-must-insert-a-public-name-for-the-attribute");
                return this.currentErrorMessage;
            }
            return null;
        }

        @Override
        public String getCurrentErrorMessage() {
            return this.currentErrorMessage;
        }

        @Override
        public void cancelCellEditing() {
            this.currentErrorMessage = null;
            SchemaPanel.this.updateComponents();
            super.cancelCellEditing();
        }

        @Override
        public boolean stopCellEditing() {
            try {
                if (this.validate() != null) {
                    this.cellRenderer.setBorder(BorderFactory.createLineBorder(Color.RED));
                    return false;
                }
                this.cellRenderer.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                boolean bl = super.stopCellEditing();
                return bl;
            }
            finally {
                this.currentErrorMessage = this.validate();
                SchemaPanel.this.updateComponents();
            }
        }
    }

    private class PublicNameCellRenderer
    extends JPanel
    implements TableCellRenderer {
        private static final long serialVersionUID = 1L;
        private JTextField publicNameTextField;
        private JButton setLanguageButton;
        private SchemaTableModel.Field currentField;
        private PublicNameCellEditor editor;

        public PublicNameCellRenderer(PublicNameCellEditor publicNameCellEditor) {
            this();
            this.editor = publicNameCellEditor;
        }

        public PublicNameCellRenderer() {
            this.setLayout(new BorderLayout());
            JPanel buttonPanel = new JPanel(new FlowLayout(0, 0, 0));
            Dimension dim = new Dimension(15, 15);
            this.publicNameTextField = new JTextField();
            this.publicNameTextField.setEditable(false);
            this.setLanguageButton = new JButton(DefaultFilterEditor.LANGUAGE_ICON);
            this.setLanguageButton.setMaximumSize(dim);
            this.setLanguageButton.setPreferredSize(dim);
            this.setLanguageButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    PublicNameCellRenderer.this.openChangeLanguageDialog();
                }
            });
            buttonPanel.add(this.setLanguageButton);
            this.add((Component)this.publicNameTextField, "Center");
            this.add((Component)buttonPanel, "East");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value == null) {
                this.publicNameTextField.setText("");
            } else {
                this.publicNameTextField.setText(((SchemaTableModel.Field)value).getPublicName());
            }
            if (isSelected) {
                this.publicNameTextField.setBackground(table.getSelectionBackground());
            } else {
                this.publicNameTextField.setBackground(table.getBackground());
            }
            boolean isEditable = table.isCellEditable(row, column);
            this.publicNameTextField.setEnabled(isEditable);
            this.setLanguageButton.setEnabled(isEditable);
            return this;
        }

        private void openChangeLanguageDialog() {
            TranslatableSelectionDialog dialog = new TranslatableSelectionDialog(JUMPWorkbench.getFrameInstance(), true, I18N.getMessage("com.vividsolutions.jump.workbench.ui.SchemaPanel.Translations-for-the-attribute-{0}-public-name", new Object[]{this.currentField.getName()}), this.currentField);
            if (dialog.isOk()) {
                this.publicNameTextField.setText(this.currentField.getPublicName());
                SchemaPanel.this.markAsModified();
            }
            if (this.editor != null) {
                this.editor.stopCellEditing();
            }
        }

        public SchemaTableModel.Field getField() {
            return this.currentField;
        }

        public void setField(SchemaTableModel.Field field) {
            this.currentField = field;
        }
    }

    private class StripingRenderer
    implements TableCellRenderer {
        private TableCellRenderer originalRenderer;
        private final Color LIGHT_GRAY = new Color(230, 230, 230);

        public StripingRenderer(TableCellRenderer originalRenderer) {
            this.originalRenderer = originalRenderer;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JComponent component = (JComponent)this.originalRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            component.setOpaque(true);
            if (!isSelected) {
                component.setForeground(Color.black);
                component.setBackground(SchemaPanel.this.layer.isEditable() || row % 2 == 0 ? Color.white : this.LIGHT_GRAY);
                component.setEnabled(((SchemaTableModel)table.getModel()).isCellEditable(row, column));
            }
            return component;
        }
    }
}

