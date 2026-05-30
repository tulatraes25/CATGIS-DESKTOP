/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import com.pcauto.gui.table.AbstractEntityTableColumnModel;
import com.pcauto.gui.table.EntityList;
import com.pcauto.gui.table.EntityListEvent;
import com.pcauto.gui.table.EntityListException;
import com.pcauto.gui.table.EntityListListener;
import com.pcauto.gui.table.EntityTable;
import com.pcauto.gui.table.EntityTableColumn;
import com.pcauto.gui.table.EntityTableColumnModel;
import com.pcauto.gui.table.EntityTableFocusType;
import com.pcauto.gui.table.ProxyEntityList;
import com.pcauto.gui.table.TestColumnTableColumnModel;
import com.pcauto.gui.table.TestControlPanel;
import com.pcauto.gui.table.TestEntity;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellEditor;
import org.saig.jump.widgets.util.DialogFactory;

public class TableTestBed
extends JPanel {
    private static final long serialVersionUID = 1L;
    private JCheckBox allowCellFocus;
    private JLabel allowLabel;
    private JCheckBox allowRowFocus;
    private JCheckBox allowTableFocus;
    private JPanel buttonPanel;
    private JLabel cellFocusLabel;
    private EntityTable columnTable;
    private TestColumnTableColumnModel columnTableColumnModel;
    private ProxyEntityList columnTableEntityList;
    private EntityTableColumn columnTablePrototype;
    private JCheckBox delEnableVirtColumn;
    private JCheckBox delEnableVirtualRow;
    private JPanel delFocusModeGroup;
    private JCheckBox delOrderLocked;
    private JPanel delPropLeftPanel;
    private JPanel delPropRightPanel;
    private JCheckBox delReadOnly;
    private JCheckBox delRowSort;
    private JRadioButton delVCDisplayIndex;
    private JRadioButton delVCEntityIndex;
    private JRadioButton delVCNone;
    private JPanel delVirtualColumnGroup;
    private JPanel delegateProperties;
    private JRadioButton dfltCellFocus;
    private JLabel dfltLabel;
    private JRadioButton dfltRowFocus;
    private JRadioButton dfltTableFocus;
    private JLabel emptyLabel;
    private JTextArea entityListEvents;
    private JLabel entityListLabel;
    private JScrollPane entityListPane;
    private JPanel entityListPanel;
    private JTextArea entityTableEvents;
    private JLabel entityTableLabel;
    private JScrollPane entityTablePane;
    private JPanel entityTablePanel;
    private JPanel eventPanel;
    private EntityTable mainTable;
    private JComboBox modeField;
    private JLabel modeLabel;
    private JPanel modePanel;
    private JPanel modelProperties;
    private JPanel propertiesPanel;
    private JButton refreshButton;
    private JLabel rowFocusLabel;
    private JLabel tableFocusLabel;
    private TestControlPanel tablePanel;
    private JButton testFocusB;
    private ProxyEntityList testTableEntityList;
    private TestEntity testTablePrototype;
    private Vector columnTableVector = new Vector();
    private EntityTableColumnModel columnModel = new AbstractEntityTableColumnModel(){
        private static final long serialVersionUID = 1L;

        @Override
        public Object getCellValue(int col, Object entity) {
            TestEntity t = (TestEntity)entity;
            switch (col) {
                case 0: {
                    return t.getKey();
                }
                case 1: {
                    return t.getBooleanval();
                }
                case 2: {
                    return t.getDiscreteval();
                }
                case 3: {
                    return t.getKey();
                }
                case 4: {
                    return t.getBooleanval();
                }
            }
            return null;
        }

        @Override
        public void setCellValue(int col, Object entity, Object newValue) {
            TestEntity t = (TestEntity)entity;
            switch (col) {
                case 0: {
                    t.setKey((String)newValue);
                    break;
                }
                case 1: {
                    t.setBooleanval((Boolean)newValue);
                    break;
                }
                case 2: {
                    t.setDiscreteval((String)newValue);
                    break;
                }
                case 3: {
                    t.setKey((String)newValue);
                    break;
                }
                case 4: {
                    t.setBooleanval((Boolean)newValue);
                    break;
                }
            }
        }
    };
    private Hashtable hash;

    public TableTestBed() {
        this.columnModel.addColumn(new EntityTableColumn("Column 1", String.class));
        this.columnModel.addColumn(new EntityTableColumn("Column 3", Boolean.class));
        this.columnModel.addColumn(new EntityTableColumn("Column 7", String.class, 100, (TableCellEditor)new DefaultCellEditor(new JComboBox<String>(new String[]{"test1", " test 2", " test 3", "test 4"}))));
        this.columnModel.addColumn(new EntityTableColumn("Column 9", String.class));
        this.columnModel.addColumn(new EntityTableColumn("Column 10", Boolean.class));
        int i = 0;
        while (i < this.columnModel.getColumnCount()) {
            this.columnTableVector.addElement(this.columnModel.getColumn(i));
            ++i;
        }
        this.initComponents();
        String[] modes = new String[]{"Table Focus", "Row Focus", "Cell Focus"};
        this.modeField.setModel(new DefaultComboBoxModel<String>(modes));
        this.refresh();
        try {
            this.testTableEntityList.addEntity(new TestEntity("1", "7", new Boolean(false), "test1"));
            this.testTableEntityList.addEntity(new TestEntity("2", "2", new Boolean(true), "test1"));
            this.testTableEntityList.addEntity(new TestEntity("3", "4", new Boolean(false), "test1"));
            this.columnModel.getColumn(1).setPreferredWidth(200);
        }
        catch (EntityListException entityListException) {
            // empty catch block
        }
        Vector<Integer> v = new Vector<Integer>();
        v.add(new Integer(0));
        v.add(new Integer(3));
        this.mainTable.sortByColumns(v);
    }

    private void refresh() {
        this.mainTable.refresh();
        this.delReadOnly.setSelected(this.mainTable.isReadOnly());
        this.delRowSort.setSelected(this.mainTable.isRowSortingAllowed());
        this.delOrderLocked.setSelected(this.mainTable.isOrderLockedToList());
        this.delEnableVirtColumn.setSelected(this.mainTable.isVirtualColumnEnabled());
        this.delEnableVirtualRow.setSelected(this.mainTable.isVirtualRowEnabled());
        this.delVCNone.setEnabled(this.mainTable.isVirtualColumnEnabled());
        this.delVCEntityIndex.setEnabled(this.mainTable.isVirtualColumnEnabled());
        this.delVCDisplayIndex.setEnabled(this.mainTable.isVirtualColumnEnabled());
        this.delVCNone.setSelected(!this.mainTable.isRowNumberDisplayed() && !this.mainTable.isEntityIndexDisplayed());
        this.delVCEntityIndex.setSelected(this.mainTable.isEntityIndexDisplayed());
        this.delVCDisplayIndex.setSelected(this.mainTable.isRowNumberDisplayed());
        this.allowTableFocus.setSelected(this.mainTable.isTableFocusAllowed());
        this.allowRowFocus.setSelected(this.mainTable.isRowFocusAllowed());
        this.allowCellFocus.setSelected(this.mainTable.isCellFocusAllowed());
        this.dfltTableFocus.setSelected(this.mainTable.getDefaultFocusMode() == EntityTableFocusType.TABLE_FOCUS);
        this.dfltRowFocus.setSelected(this.mainTable.getDefaultFocusMode() == EntityTableFocusType.ROW_FOCUS);
        this.dfltCellFocus.setSelected(this.mainTable.getDefaultFocusMode() == EntityTableFocusType.CELL_FOCUS);
        this.setFocusModeComboBox();
    }

    private void initComponents() {
        this.columnTableEntityList = new ProxyEntityList();
        this.columnTablePrototype = new EntityTableColumn();
        this.columnTableColumnModel = new TestColumnTableColumnModel();
        this.testTablePrototype = new TestEntity();
        this.testTableEntityList = new ProxyEntityList();
        this.propertiesPanel = new JPanel();
        this.delegateProperties = new JPanel();
        this.delPropLeftPanel = new JPanel();
        this.delReadOnly = new JCheckBox();
        this.delOrderLocked = new JCheckBox();
        this.delRowSort = new JCheckBox();
        this.delEnableVirtualRow = new JCheckBox();
        this.delPropRightPanel = new JPanel();
        this.delVirtualColumnGroup = new JPanel();
        this.delEnableVirtColumn = new JCheckBox();
        this.delVCNone = new JRadioButton();
        this.delVCEntityIndex = new JRadioButton();
        this.delVCDisplayIndex = new JRadioButton();
        this.delFocusModeGroup = new JPanel();
        this.emptyLabel = new JLabel();
        this.allowLabel = new JLabel();
        this.dfltLabel = new JLabel();
        this.tableFocusLabel = new JLabel();
        this.allowTableFocus = new JCheckBox();
        this.dfltTableFocus = new JRadioButton();
        this.rowFocusLabel = new JLabel();
        this.allowRowFocus = new JCheckBox();
        this.dfltRowFocus = new JRadioButton();
        this.cellFocusLabel = new JLabel();
        this.allowCellFocus = new JCheckBox();
        this.dfltCellFocus = new JRadioButton();
        this.modelProperties = new JPanel();
        this.columnTable = new EntityTable();
        this.mainTable = new EntityTable();
        this.buttonPanel = new JPanel();
        this.tablePanel = new TestControlPanel();
        this.refreshButton = new JButton();
        this.eventPanel = new JPanel();
        this.modePanel = new JPanel();
        this.modeLabel = new JLabel();
        this.modeField = new JComboBox();
        this.entityListPanel = new JPanel();
        this.entityListLabel = new JLabel();
        this.entityListPane = new JScrollPane();
        this.entityListEvents = new JTextArea();
        this.entityTablePanel = new JPanel();
        this.entityTableLabel = new JLabel();
        this.entityTablePane = new JScrollPane();
        this.entityTableEvents = new JTextArea();
        this.testFocusB = new JButton();
        this.columnTableEntityList.setList(this.columnTableVector);
        this.columnTableEntityList.setDefaultEntity(this.columnTablePrototype);
        this.testTableEntityList.setDefaultEntity(this.testTablePrototype);
        this.testTableEntityList.addEntityListListener(new EntityListListener(){

            @Override
            public void listChanged(EntityListEvent evt) {
                TableTestBed.this.testTableEntityListListChanged(evt);
            }
        });
        this.setLayout(new BorderLayout());
        this.propertiesPanel.setLayout(new GridLayout(1, 2));
        this.propertiesPanel.setPreferredSize(new Dimension(400, 200));
        this.delegateProperties.setLayout(new GridLayout(1, 2));
        this.delegateProperties.setBorder(new TitledBorder("Delegate Properties"));
        this.delPropLeftPanel.setLayout(new BoxLayout(this.delPropLeftPanel, 1));
        this.delReadOnly.setText("Read-Only Mode");
        this.delReadOnly.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.delReadOnlyActionPerformed(evt);
            }
        });
        this.delPropLeftPanel.add(this.delReadOnly);
        this.delOrderLocked.setText("Order Locked To List");
        this.delOrderLocked.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.delOrderLockedActionPerformed(evt);
            }
        });
        this.delPropLeftPanel.add(this.delOrderLocked);
        this.delRowSort.setText("Allow Row Sort");
        this.delRowSort.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.delRowSortActionPerformed(evt);
            }
        });
        this.delPropLeftPanel.add(this.delRowSort);
        this.delEnableVirtualRow.setText("Enable Virtual Row");
        this.delEnableVirtualRow.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.delEnableVirtualRowActionPerformed(evt);
            }
        });
        this.delPropLeftPanel.add(this.delEnableVirtualRow);
        this.delegateProperties.add(this.delPropLeftPanel);
        this.delPropRightPanel.setLayout(new GridLayout(2, 1));
        this.delVirtualColumnGroup.setLayout(new BoxLayout(this.delVirtualColumnGroup, 1));
        this.delVirtualColumnGroup.setBorder(new TitledBorder("Virtual Column"));
        this.delVirtualColumnGroup.setPreferredSize(new Dimension(141, 100));
        this.delEnableVirtColumn.setText("Enable Virtual Column");
        this.delEnableVirtColumn.setPreferredSize(new Dimension(150, 15));
        this.delEnableVirtColumn.setMinimumSize(new Dimension(150, 15));
        this.delEnableVirtColumn.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.delEnableVirtColumnActionPerformed(evt);
            }
        });
        this.delVirtualColumnGroup.add(this.delEnableVirtColumn);
        this.delVCNone.setText("None");
        this.delVCNone.setPreferredSize(new Dimension(100, 15));
        this.delVCNone.setMinimumSize(new Dimension(50, 15));
        this.delVCNone.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.delVCNoneActionPerformed(evt);
            }
        });
        this.delVirtualColumnGroup.add(this.delVCNone);
        this.delVCEntityIndex.setText("Entity Index");
        this.delVCEntityIndex.setPreferredSize(new Dimension(100, 15));
        this.delVCEntityIndex.setMinimumSize(new Dimension(50, 15));
        this.delVCEntityIndex.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.delVCEntityIndexActionPerformed(evt);
            }
        });
        this.delVirtualColumnGroup.add(this.delVCEntityIndex);
        this.delVCDisplayIndex.setPreferredSize(new Dimension(100, 15));
        this.delVCDisplayIndex.setText("Display Index");
        this.delVCDisplayIndex.setMinimumSize(new Dimension(50, 15));
        this.delVCDisplayIndex.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.delVCDisplayIndexActionPerformed(evt);
            }
        });
        this.delVirtualColumnGroup.add(this.delVCDisplayIndex);
        this.delPropRightPanel.add(this.delVirtualColumnGroup);
        this.delFocusModeGroup.setLayout(new GridLayout(4, 3));
        this.delFocusModeGroup.setBorder(new TitledBorder("Focus Mode"));
        this.delFocusModeGroup.setPreferredSize(new Dimension(120, 45));
        this.delFocusModeGroup.setMinimumSize(new Dimension(120, 30));
        this.delFocusModeGroup.add(this.emptyLabel);
        this.allowLabel.setText("Allow");
        this.delFocusModeGroup.add(this.allowLabel);
        this.dfltLabel.setText("Default");
        this.delFocusModeGroup.add(this.dfltLabel);
        this.tableFocusLabel.setText("Table");
        this.delFocusModeGroup.add(this.tableFocusLabel);
        this.allowTableFocus.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.allowTableFocusActionPerformed(evt);
            }
        });
        this.delFocusModeGroup.add(this.allowTableFocus);
        this.dfltTableFocus.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.dfltTableFocusActionPerformed(evt);
            }
        });
        this.delFocusModeGroup.add(this.dfltTableFocus);
        this.rowFocusLabel.setText("Row");
        this.delFocusModeGroup.add(this.rowFocusLabel);
        this.allowRowFocus.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.allowRowFocusActionPerformed(evt);
            }
        });
        this.delFocusModeGroup.add(this.allowRowFocus);
        this.dfltRowFocus.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.dfltRowFocusActionPerformed(evt);
            }
        });
        this.delFocusModeGroup.add(this.dfltRowFocus);
        this.cellFocusLabel.setText("Cell");
        this.delFocusModeGroup.add(this.cellFocusLabel);
        this.allowCellFocus.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.allowCellFocusActionPerformed(evt);
            }
        });
        this.delFocusModeGroup.add(this.allowCellFocus);
        this.dfltCellFocus.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.dfltCellFocusActionPerformed(evt);
            }
        });
        this.delFocusModeGroup.add(this.dfltCellFocus);
        this.delPropRightPanel.add(this.delFocusModeGroup);
        this.delegateProperties.add(this.delPropRightPanel);
        this.propertiesPanel.add(this.delegateProperties);
        this.modelProperties.setLayout(new GridLayout(1, 1));
        this.modelProperties.setBorder(new TitledBorder("Model Properties"));
        this.columnTable.setEntityList(this.columnTableEntityList);
        this.columnTable.setPreferredSize(new Dimension(150, 40));
        this.columnTable.setName("Column Properties");
        this.columnTable.setColumnModel(this.columnTableColumnModel);
        this.columnTable.setMinimumSize(new Dimension(150, 40));
        this.modelProperties.add(this.columnTable);
        this.propertiesPanel.add(this.modelProperties);
        this.add((Component)this.propertiesPanel, "South");
        this.mainTable.setCurrentFocusMode(EntityTableFocusType.CELL_FOCUS);
        this.mainTable.setEntityList(this.testTableEntityList);
        this.mainTable.setColumnModel(this.columnModel);
        this.mainTable.setVirtualRowEnabled(true);
        this.mainTable.setVirtualColumnEnabled(true);
        this.mainTable.setSelectionMode(2);
        this.mainTable.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent evt) {
                TableTestBed.this.mainTableMousePressed(evt);
            }

            @Override
            public void mouseClicked(MouseEvent evt) {
                TableTestBed.this.mainTableMouseClicked(evt);
            }
        });
        this.mainTable.addPropertyChangeListener(new PropertyChangeListener(){

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                TableTestBed.this.mainTablePropertyChange(evt);
            }
        });
        this.add((Component)this.mainTable, "Center");
        this.buttonPanel.setPreferredSize(new Dimension(120, 400));
        this.buttonPanel.setMinimumSize(new Dimension(120, 400));
        this.tablePanel.setEntityList(this.testTableEntityList);
        this.tablePanel.setEntityTable(this.mainTable);
        this.buttonPanel.add(this.tablePanel);
        this.refreshButton.setText("Refresh");
        this.refreshButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.refreshButtonActionPerformed(evt);
            }
        });
        this.buttonPanel.add(this.refreshButton);
        this.add((Component)this.buttonPanel, "West");
        this.eventPanel.setLayout(new BoxLayout(this.eventPanel, 1));
        this.eventPanel.setPreferredSize(new Dimension(250, 234));
        this.eventPanel.setMinimumSize(new Dimension(250, 210));
        this.modePanel.setLayout(new GridBagLayout());
        this.modeLabel.setText("Current Focus Mode : ");
        this.modePanel.add((Component)this.modeLabel, new GridBagConstraints());
        this.modeField.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.modeFieldActionPerformed(evt);
            }
        });
        this.modePanel.add((Component)this.modeField, new GridBagConstraints());
        this.eventPanel.add(this.modePanel);
        this.entityListPanel.setLayout(new BorderLayout());
        this.entityListLabel.setText("Entity List Events : ");
        this.entityListPanel.add((Component)this.entityListLabel, "North");
        this.entityListPane.setPreferredSize(new Dimension(250, 35));
        this.entityListPane.setMinimumSize(new Dimension(200, 24));
        this.entityListPane.setViewportView(this.entityListEvents);
        this.entityListPanel.add((Component)this.entityListPane, "Center");
        this.eventPanel.add(this.entityListPanel);
        this.entityTablePanel.setLayout(new BorderLayout());
        this.entityTableLabel.setText("Entity Table Events : ");
        this.entityTablePanel.add((Component)this.entityTableLabel, "North");
        this.entityTablePane.setPreferredSize(new Dimension(250, 34));
        this.entityTablePane.setMinimumSize(new Dimension(250, 22));
        this.entityTablePane.setViewportView(this.entityTableEvents);
        this.entityTablePanel.add((Component)this.entityTablePane, "Center");
        this.eventPanel.add(this.entityTablePanel);
        this.testFocusB.setText("test");
        this.testFocusB.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                TableTestBed.this.testFocusBActionPerformed(evt);
            }
        });
        this.eventPanel.add(this.testFocusB);
        this.add((Component)this.eventPanel, "East");
    }

    private void testFocusBActionPerformed(ActionEvent evt) {
        EntityList list = this.mainTable.getDisplayEntityList();
        try {
            System.out.println("Selected Entity:\n" + list.getEntity(this.mainTable.getDisplaySelectionModel().getMaxSelectionIndex()));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void mainTableMouseClicked(MouseEvent evt) {
    }

    private void mainTableMousePressed(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            DialogFactory.showInformationDialog(this, "Double Click", " ");
        }
    }

    private void delRowSortActionPerformed(ActionEvent evt) {
        this.mainTable.setRowSortingAllowed(this.delRowSort.isSelected());
        this.refresh();
    }

    private void delOrderLockedActionPerformed(ActionEvent evt) {
        this.mainTable.setOrderLockedToList(this.delOrderLocked.isSelected());
        this.refresh();
    }

    private void modeFieldActionPerformed(ActionEvent evt) {
        int selected = this.modeField.getSelectedIndex();
        if (selected == 0) {
            this.mainTable.setCurrentFocusMode(EntityTableFocusType.TABLE_FOCUS);
        } else if (selected == 1) {
            this.mainTable.setCurrentFocusMode(EntityTableFocusType.ROW_FOCUS);
        } else if (selected == 2) {
            this.mainTable.setCurrentFocusMode(EntityTableFocusType.CELL_FOCUS);
        }
        this.refresh();
    }

    private void mainTablePropertyChange(PropertyChangeEvent evt) {
        this.entityTableEvents.append(String.valueOf(evt.toString()) + "\n");
        this.setFocusModeComboBox();
    }

    private void testTableEntityListListChanged(EntityListEvent evt) {
        this.entityListEvents.append(String.valueOf(evt.toString()) + "\n");
    }

    private void delVCDisplayIndexActionPerformed(ActionEvent evt) {
        if (this.delVCDisplayIndex.isSelected()) {
            this.mainTable.setRowNumberDisplayed(true);
            this.mainTable.setEntityIndexDisplayed(false);
        }
        this.refresh();
    }

    private void delVCEntityIndexActionPerformed(ActionEvent evt) {
        if (this.delVCEntityIndex.isSelected()) {
            this.mainTable.setRowNumberDisplayed(false);
            this.mainTable.setEntityIndexDisplayed(true);
        }
        this.refresh();
    }

    private void delVCNoneActionPerformed(ActionEvent evt) {
        if (this.delVCNone.isSelected()) {
            this.mainTable.setRowNumberDisplayed(false);
            this.mainTable.setEntityIndexDisplayed(false);
        }
        this.refresh();
    }

    private void dfltCellFocusActionPerformed(ActionEvent evt) {
        this.mainTable.setDefaultFocusMode(EntityTableFocusType.CELL_FOCUS);
        this.refresh();
    }

    private void dfltRowFocusActionPerformed(ActionEvent evt) {
        this.mainTable.setDefaultFocusMode(EntityTableFocusType.ROW_FOCUS);
        this.refresh();
    }

    private void dfltTableFocusActionPerformed(ActionEvent evt) {
        this.mainTable.setDefaultFocusMode(EntityTableFocusType.TABLE_FOCUS);
        this.refresh();
    }

    private void allowCellFocusActionPerformed(ActionEvent evt) {
        this.mainTable.setCellFocusAllowed(this.allowCellFocus.isSelected());
        this.refresh();
    }

    private void allowRowFocusActionPerformed(ActionEvent evt) {
        this.mainTable.setRowFocusAllowed(this.allowRowFocus.isSelected());
        this.refresh();
    }

    private void allowTableFocusActionPerformed(ActionEvent evt) {
        this.mainTable.setTableFocusAllowed(this.allowTableFocus.isSelected());
        this.refresh();
    }

    private void delReadOnlyActionPerformed(ActionEvent evt) {
        this.mainTable.setReadOnly(this.delReadOnly.isSelected());
    }

    private void refreshButtonActionPerformed(ActionEvent evt) {
        this.refresh();
    }

    private void delEnableVirtualRowActionPerformed(ActionEvent evt) {
        this.mainTable.setVirtualRowEnabled(this.delEnableVirtualRow.isSelected());
    }

    private void delEnableVirtColumnActionPerformed(ActionEvent evt) {
        this.mainTable.setVirtualColumnEnabled(this.delEnableVirtColumn.isSelected());
        this.refresh();
    }

    private void setFocusModeComboBox() {
        EntityTableFocusType etft = this.mainTable.getCurrentFocusMode();
        if (etft == EntityTableFocusType.TABLE_FOCUS) {
            this.modeField.setSelectedIndex(0);
        } else if (etft == EntityTableFocusType.ROW_FOCUS) {
            this.modeField.setSelectedIndex(1);
        } else if (etft == EntityTableFocusType.CELL_FOCUS) {
            this.modeField.setSelectedIndex(2);
        }
    }

    public static void main(String[] args) {
        System.out.println("version: " + System.getProperty("java.vm.version"));
        TableTestBed test = new TableTestBed();
        JFrame m = new JFrame();
        m.getContentPane().add(test);
        m.setSize(800, 600);
        m.setVisible(true);
    }
}

