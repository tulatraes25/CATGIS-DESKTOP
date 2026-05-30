/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import com.pcauto.gui.table.EntityJTable;
import com.pcauto.gui.table.EntityList;
import com.pcauto.gui.table.EntityListEvent;
import com.pcauto.gui.table.EntityListException;
import com.pcauto.gui.table.EntityListListener;
import com.pcauto.gui.table.EntityTableColumn;
import com.pcauto.gui.table.EntityTableColumnModel;
import com.pcauto.gui.table.EntityTableFocusType;
import com.pcauto.gui.table.OrderTranslatorEntityList;
import com.pcauto.gui.table.ProxyColumnModel;
import com.pcauto.gui.table.ProxyListSelectionModel;
import com.pcauto.gui.table.ProxyTableModel;
import com.pcauto.gui.table.RowHeaderColumnModel;
import com.pcauto.gui.table.RowHeaderModel;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.saig.core.model.data.widgets.tables.management.EntityTableListener;
import org.saig.core.util.SwingWorker;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class EntityTable
extends JPanel
implements EntityListListener,
TableColumnModelListener {
    private static final long serialVersionUID = 1L;
    private List<EntityTableListener> entityTableListenerList;
    JPopupMenu popup;
    private OrderTranslatorEntityList orderTranslatorEntityList;
    private ProxyColumnModel proxyColumnModel;
    private ProxyTableModel proxyTable;
    private RowHeaderColumnModel rowHeaderColumnModel;
    private RowHeaderModel rowHeaderModel;
    private ProxyListSelectionModel proxyListSelectionModel;
    private JScrollPane scrollPane;
    private EntityJTable mainTable;
    private EntityList entityList = null;
    private EntityTableColumnModel columnModel = null;
    private boolean readOnly = false;
    private KeyAdapter cellEditorAdapter = null;
    private FocusAdapter cellFocusAdapter = null;
    private boolean autoCalculateWidth = false;
    private boolean autoCalculateHeight = false;
    private int lastRowCount = -1;
    private int selectionMode = 1;
    private boolean virtualRow = false;
    private boolean virtualRowEdited = false;
    private boolean virtualColumn = false;
    private boolean rowNumbers = true;
    private boolean entityIndex = false;
    private EntityTableFocusType currentFocus = EntityTableFocusType.ROW_FOCUS;
    private EntityTableFocusType defaultFocus = EntityTableFocusType.ROW_FOCUS;
    private boolean allowTableFocus = true;
    private boolean allowRowFocus = true;
    private boolean allowCellFocus = true;
    private EntityJTable rowHeader = null;
    private JViewport rowHeaderViewport = null;
    private static JComponent myComponent = null;
    private boolean orderLocked = false;
    private boolean sortingAllowed = true;
    private EditingTerminator et;
    private Color selectionBackground = new Color(204, 204, 255);

    public EntityTable() {
        this.entityTableListenerList = new ArrayList<EntityTableListener>();
        this.setSelectionMode(2);
    }

    public EntityTable(EntityList eList, EntityTableColumnModel cModel) {
        this();
        this.setEntityList(eList);
        this.setColumnModel(cModel);
    }

    public int getRowHeight() {
        return this.mainTable.getRowHeight();
    }

    public void setRowHeight(int rowHeight) {
        this.mainTable.setRowHeight(rowHeight);
    }

    public int columnAtPoint(Point p) {
        int retVal = -1;
        int x = (int)p.getX();
        int y = (int)p.getY();
        Point mainTablePoint = new Point(x -= this.rowHeader.getWidth(), y);
        if (this.mainTable.columnAtPoint(mainTablePoint) != -1) {
            retVal = this.mainTable.columnAtPoint(mainTablePoint);
        }
        if (this.rowHeader.columnAtPoint(p) != -1) {
            retVal = this.rowHeader.columnAtPoint(p);
        } else if (retVal != -1) {
            retVal += this.rowHeader.getColumnCount();
        }
        if (this.isVirtualColumnEnabled()) {
            --retVal;
        }
        return retVal;
    }

    public int rowAtPoint(Point p) {
        return this.mainTable.rowAtPoint(p);
    }

    public int getDisplayedEntityIndexAtPoint(Point p) {
        return this.rowAtPoint(p);
    }

    public int getEntityIndexAtPoint(Point p) {
        return this.orderTranslatorEntityList.getEntityIndex(this.rowAtPoint(p));
    }

    public Object getEntityAtPoint(Point p) {
        return this.orderTranslatorEntityList.getEntity(this.rowAtPoint(p));
    }

    public EntityList getEntityList() {
        return this.entityList;
    }

    public void setEntityList(EntityList eList) {
        EntityList oldEntityList = this.entityList;
        this.entityList = eList;
        if (oldEntityList == null && this.entityList != null && this.columnModel != null) {
            this.initComponents();
            this.setupRowHeader();
        }
        if (this.entityList != null && this.orderTranslatorEntityList != null) {
            this.orderTranslatorEntityList.setEntityList(this.entityList);
        }
    }

    public EntityTableColumnModel getColumnModel() {
        return this.columnModel;
    }

    public void setColumnModel(EntityTableColumnModel cModel) {
        int i = 0;
        while (i < cModel.getColumnCount()) {
            if (!(cModel.getColumn(i) instanceof EntityTableColumn)) {
                System.out.println("EntityTable: setColumnModel: NON-EntityTableColumn Column Detected.  Unable to set Column Model");
                return;
            }
            ++i;
        }
        this.columnModel = cModel;
        if (this.entityList != null && this.columnModel != null) {
            this.initComponents();
            this.setupRowHeader();
        }
        this.revalidate();
    }

    public JScrollPane getScrollPane() {
        return this.scrollPane;
    }

    private void setupRowHeader() {
        this.rowHeader = new EntityJTable();
        this.rowHeader.setAutoCreateColumnsFromModel(false);
        this.rowHeader.setModel(this.rowHeaderModel);
        this.rowHeader.setColumnModel(this.rowHeaderColumnModel);
        this.rowHeader.setColumnSelectionAllowed(this.mainTable.getColumnSelectionAllowed());
        this.rowHeader.setRowSelectionAllowed(this.mainTable.getRowSelectionAllowed());
        this.rowHeader.setCellSelectionEnabled(this.mainTable.getCellSelectionEnabled());
        this.rowHeader.setAutoResizeMode(0);
        this.rowHeaderColumnModel.removeColumnModelListener(this);
        this.rowHeaderColumnModel.addColumnModelListener(this);
        this.rowHeader.getTableHeader().setFont(this.rowHeader.getTableHeader().getFont().deriveFont(1));
        TableCellRenderer headerRenderer = new TableCellRenderer(){
            private Icon downIcon = IconLoader.icon("Down.gif");
            private TableCellRenderer originalRenderer;
            private Icon upIcon;
            {
                this.originalRenderer = EntityTable.this.rowHeader.getTableHeader().getDefaultRenderer();
                this.upIcon = IconLoader.icon("Up.gif");
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel)this.originalRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int pos = EntityTable.this.orderTranslatorEntityList.getSortedColumn();
                if (pos != -1 && column == pos) {
                    if (!EntityTable.this.orderTranslatorEntityList.isAscending()) {
                        label.setIcon(this.upIcon);
                    } else {
                        label.setIcon(this.downIcon);
                    }
                } else {
                    label.setIcon(null);
                }
                label.setHorizontalTextPosition(2);
                return label;
            }
        };
        this.rowHeader.setSelectionModel(this.mainTable.getSelectionModel());
        this.mainTable.getTableHeader().setDefaultRenderer(headerRenderer);
        this.rowHeaderViewport = new JViewport();
        this.rowHeaderViewport.setView(this.rowHeader);
        this.proxyTable.addTableModelListener(new TableModelListener(){

            @Override
            public void tableChanged(TableModelEvent t) {
                if (EntityTable.this.proxyTable.getRowCount() != EntityTable.this.lastRowCount) {
                    EntityTable.this.resizeTable();
                }
            }
        });
        this.setupListListener();
        this.setupKeyListener();
        this.setupMouseListener();
        this.rowHeader.setOpaque(false);
        this.mainTable.setOpaque(false);
        this.setCurrentFocusMode(this.currentFocus);
        this.resizeTable();
    }

    private void setupListListener() {
    }

    private void resizeTable() {
        this.lastRowCount = this.proxyTable.getRowCount();
        int tableHeight = this.mainTable.getRowHeight() + this.mainTable.getRowMargin();
        int totalColumnWidth = this.rowHeaderColumnModel.getTotalColumnWidth();
        this.rowHeader.setPreferredSize(new Dimension(totalColumnWidth, tableHeight *= this.proxyTable.getRowCount()));
        this.rowHeader.setMinimumSize(new Dimension(totalColumnWidth, tableHeight));
        this.rowHeader.setMaximumSize(new Dimension(totalColumnWidth, tableHeight));
        this.rowHeaderViewport.setPreferredSize(new Dimension(totalColumnWidth, tableHeight));
        this.rowHeaderViewport.setMinimumSize(new Dimension(totalColumnWidth, tableHeight));
        this.rowHeaderViewport.setMaximumSize(new Dimension(totalColumnWidth, tableHeight));
        totalColumnWidth = this.proxyColumnModel.getTotalColumnWidth();
        this.mainTable.setPreferredSize(new Dimension(totalColumnWidth, tableHeight));
        this.mainTable.setMinimumSize(new Dimension(totalColumnWidth, tableHeight));
        this.mainTable.setMaximumSize(new Dimension(totalColumnWidth, tableHeight));
        this.scrollPane.setCorner("UPPER_LEFT_CORNER", this.rowHeader.getTableHeader());
        this.scrollPane.setRowHeaderView(this.rowHeaderViewport);
        if (this.autoCalculateWidth) {
            int requiredWidth = this.rowHeaderColumnModel.getTotalColumnWidth() + this.proxyColumnModel.getTotalColumnWidth() + (int)this.scrollPane.getVerticalScrollBar().getPreferredSize().getWidth() + this.scrollPane.getBorder().getBorderInsets((Component)this.rowHeader).left + this.scrollPane.getBorder().getBorderInsets((Component)this.mainTable).right + this.scrollPane.getBorder().getBorderInsets((Component)this.scrollPane.getVerticalScrollBar()).left + this.scrollPane.getBorder().getBorderInsets((Component)this.scrollPane.getVerticalScrollBar()).right;
            this.setPreferredSize(new Dimension(requiredWidth, (int)this.getPreferredSize().getHeight()));
            this.setMaximumSize(new Dimension(requiredWidth, (int)this.getPreferredSize().getHeight()));
        }
        this.mainTable.revalidate();
        this.mainTable.repaint();
        this.rowHeader.revalidate();
        this.rowHeader.repaint();
        this.scrollPane.revalidate();
        this.scrollPane.repaint();
    }

    public void setWidthAutoCalculated(boolean b) {
        this.autoCalculateWidth = b;
    }

    public boolean isWidthAutoCalculated() {
        return this.autoCalculateWidth;
    }

    public boolean isVirtualRowEnabled() {
        return this.virtualRow;
    }

    public void setVirtualRowEnabled(boolean v) {
        if (this.virtualRow == v) {
            return;
        }
        this.virtualRow = v;
        if (this.proxyTable != null) {
            this.proxyTable.setVirtualRowEnabled(v);
        }
        if (this.rowHeaderModel != null) {
            this.rowHeaderModel.setVirtualRowEnabled(v);
        }
    }

    public boolean isVirtualColumnEnabled() {
        return this.virtualColumn;
    }

    public void setVirtualColumnEnabled(boolean v) {
        if (this.virtualColumn == v) {
            return;
        }
        this.virtualColumn = v;
        if (this.rowHeaderModel != null) {
            this.rowHeaderModel.setVirtualColumnEnabled(v);
        }
        if (this.rowHeaderColumnModel != null) {
            this.rowHeaderColumnModel.setVirtualColumnEnabled(v);
        }
    }

    public boolean isRowNumberDisplayed() {
        return this.rowNumbers;
    }

    public void setRowNumberDisplayed(boolean d) {
        this.rowNumbers = d;
        if (this.rowHeaderModel != null) {
            this.rowHeaderModel.setRowNumberDisplayed(d);
        }
    }

    public boolean isEntityIndexDisplayed() {
        return this.entityIndex;
    }

    public void setEntityIndexDisplayed(boolean d) {
        this.entityIndex = d;
        if (this.rowHeaderModel != null) {
            this.rowHeaderModel.setEntityIndexDisplayed(d);
        }
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public void setReadOnly(boolean r) {
        if (this.readOnly == r) {
            return;
        }
        this.readOnly = r;
        if (this.proxyTable != null) {
            this.proxyTable.setReadOnly(r);
        }
        if (this.rowHeaderModel != null) {
            this.rowHeaderModel.setReadOnly(r);
        }
    }

    public boolean isOrderLockedToList() {
        return this.orderLocked;
    }

    public void setOrderLockedToList(boolean b) {
        this.orderLocked = b;
        if (this.orderTranslatorEntityList != null) {
            this.orderTranslatorEntityList.setOrderLockedToList(b);
        }
    }

    public boolean isRowSortingAllowed() {
        return this.sortingAllowed;
    }

    public void setRowSortingAllowed(boolean b) {
        this.sortingAllowed = b;
        if (this.orderTranslatorEntityList != null) {
            this.orderTranslatorEntityList.setRowSortingAllowed(b);
        }
    }

    public void sortByColumns(Vector<Integer> columns) {
        this.orderTranslatorEntityList.sortByColumns(columns);
    }

    public void setListToTableOrder() {
        this.orderTranslatorEntityList.setEntityListOrder();
    }

    public void setTableToListOrder() {
        this.orderTranslatorEntityList.getEntityListOrder();
    }

    public EntityTableFocusType getDefaultFocusMode() {
        return this.defaultFocus;
    }

    public void setDefaultFocusMode(EntityTableFocusType f) {
        if (f == EntityTableFocusType.TABLE_FOCUS) {
            if (this.allowTableFocus) {
                this.defaultFocus = f;
            }
        } else if (f == EntityTableFocusType.ROW_FOCUS) {
            if (this.allowRowFocus) {
                this.defaultFocus = f;
            }
        } else if (f == EntityTableFocusType.CELL_FOCUS && this.allowCellFocus) {
            this.defaultFocus = f;
        }
    }

    public int getSelectionMode() {
        return this.selectionMode;
    }

    public void setSelectionMode(int mode) {
        switch (mode) {
            case 2: {
                this.selectionMode = mode;
                break;
            }
            case 1: {
                this.selectionMode = mode;
                break;
            }
            case 0: {
                this.selectionMode = mode;
                break;
            }
            default: {
                System.out.println("EntityTable:setSelectionMode() invalid mode");
            }
        }
        if (this.mainTable != null) {
            this.mainTable.setSelectionMode(this.selectionMode);
        }
        if (this.rowHeader != null) {
            this.rowHeader.setSelectionMode(this.selectionMode);
        }
    }

    public void setSelectionBackground(Color c) {
        this.selectionBackground = c;
        if (this.mainTable != null) {
            this.mainTable.setSelectionBackground(this.selectionBackground);
        }
        if (this.rowHeader != null) {
            this.rowHeader.setSelectionBackground(this.selectionBackground);
        }
    }

    public Color getSelectionBackground() {
        return this.selectionBackground;
    }

    @Override
    public void setBackground(Color c) {
        if (this.mainTable != null) {
            this.mainTable.setBackground(c);
        }
        if (this.rowHeader != null) {
            this.rowHeader.setBackground(c);
        }
    }

    @Override
    public Color getBackground() {
        return super.getBackground();
    }

    public EntityTableFocusType getCurrentFocusMode() {
        return this.currentFocus;
    }

    public void setCurrentFocusMode(EntityTableFocusType f) {
        if (this.rowHeader != null) {
            this.rowHeader.setSelectionBackground(this.selectionBackground);
        }
        if (this.mainTable != null) {
            this.mainTable.setSelectionBackground(this.selectionBackground);
        }
        EntityTableFocusType oldFocus = this.currentFocus;
        if (f == EntityTableFocusType.TABLE_FOCUS) {
            if (this.allowTableFocus) {
                TableCellEditor e;
                this.currentFocus = f;
                if (this.mainTable != null) {
                    this.mainTable.setSelectionMode(this.selectionMode);
                    e = this.mainTable.getCellEditor();
                    if (e != null) {
                        e.stopCellEditing();
                    }
                    this.getSelectionModel().clearSelection();
                    this.getSelectionModel().addSelectionInterval(0, 0);
                }
                if (this.rowHeader != null) {
                    this.rowHeader.setSelectionMode(this.selectionMode);
                    e = this.rowHeader.getCellEditor();
                    if (e != null) {
                        e.stopCellEditing();
                    }
                }
                this.setCellSelectionEnabled(false);
                this.setRowSelectionAllowed(true);
            }
        } else if (f == EntityTableFocusType.ROW_FOCUS) {
            if (this.allowRowFocus) {
                TableCellEditor e;
                this.currentFocus = f;
                if (this.mainTable != null) {
                    this.mainTable.setSelectionMode(this.selectionMode);
                    e = this.mainTable.getCellEditor();
                    if (e != null) {
                        e.stopCellEditing();
                    }
                    this.getSelectionModel().clearSelection();
                    this.getSelectionModel().addSelectionInterval(0, 0);
                }
                if (this.rowHeader != null) {
                    this.rowHeader.setSelectionMode(this.selectionMode);
                    e = this.rowHeader.getCellEditor();
                    if (e != null) {
                        e.stopCellEditing();
                    }
                }
                this.setCellSelectionEnabled(false);
                this.setRowSelectionAllowed(true);
            }
        } else if (f == EntityTableFocusType.CELL_FOCUS && this.allowCellFocus) {
            this.currentFocus = f;
            if (this.mainTable != null && !this.getDisplaySelectionModel().isSelectionEmpty()) {
                this.moveEditCell(this.getDisplaySelectionModel().getMinSelectionIndex(), 0, 0, 0);
            }
            this.setRowSelectionAllowed(false);
            this.setCellSelectionEnabled(true);
        }
        this.firePropertyChange("CurrentFocusMode", oldFocus, this.currentFocus);
    }

    public boolean isTableFocusAllowed() {
        return this.allowTableFocus;
    }

    public void setTableFocusAllowed(boolean a) {
        if (!(a || this.currentFocus != EntityTableFocusType.TABLE_FOCUS && this.defaultFocus != EntityTableFocusType.TABLE_FOCUS)) {
            return;
        }
        this.allowTableFocus = a;
    }

    public boolean isRowFocusAllowed() {
        return this.allowRowFocus;
    }

    public void setRowFocusAllowed(boolean a) {
        if (!(a || this.currentFocus != EntityTableFocusType.ROW_FOCUS && this.defaultFocus != EntityTableFocusType.ROW_FOCUS)) {
            return;
        }
        this.allowRowFocus = a;
    }

    public boolean isCellFocusAllowed() {
        return this.allowCellFocus;
    }

    public void setCellFocusAllowed(boolean a) {
        if (!(a || this.currentFocus != EntityTableFocusType.CELL_FOCUS && this.defaultFocus != EntityTableFocusType.CELL_FOCUS)) {
            return;
        }
        this.allowCellFocus = a;
    }

    private void setCellSelectionEnabled(boolean b) {
        if (this.mainTable != null) {
            this.mainTable.setCellSelectionEnabled(b);
        }
        if (this.rowHeader != null) {
            this.rowHeader.setCellSelectionEnabled(b);
        }
        if (this.proxyTable != null) {
            this.proxyTable.setCellEditingEnabled(b);
        }
        if (this.rowHeaderModel != null) {
            this.rowHeaderModel.setCellEditingEnabled(b);
        }
    }

    private void setRowSelectionAllowed(boolean b) {
        if (this.mainTable != null) {
            this.mainTable.setRowSelectionAllowed(b);
        }
        if (this.rowHeader != null) {
            this.rowHeader.setRowSelectionAllowed(b);
        }
    }

    public EntityList getDisplayEntityList() {
        return this.orderTranslatorEntityList;
    }

    public ListSelectionModel getDisplaySelectionModel() {
        return this.mainTable.getSelectionModel();
    }

    public void setDisplaySelectionModel(ListSelectionModel l) {
        this.mainTable.setSelectionModel(l);
        this.rowHeader.setSelectionModel(l);
        this.proxyListSelectionModel.setSelectionModel(l);
    }

    public ListSelectionModel getSelectionModel() {
        return this.proxyListSelectionModel;
    }

    public void setSelectionModel(ListSelectionModel l) {
        this.proxyListSelectionModel.setSelectionModel(l);
    }

    public void refresh() {
        this.revalidate();
    }

    @Override
    public synchronized void revalidate() {
        super.revalidate();
        if (this.proxyTable != null) {
            this.proxyTable.revalidate();
        }
        if (this.proxyColumnModel != null) {
            this.proxyColumnModel.revalidate();
        }
        if (this.rowHeaderModel != null) {
            this.rowHeaderModel.revalidate();
        }
        if (this.rowHeaderColumnModel != null) {
            this.rowHeaderColumnModel.revalidate();
        }
        if (this.scrollPane != null) {
            this.resizeTable();
        }
    }

    @Override
    public void setMinimumSize(Dimension p) {
        super.setMinimumSize(p);
        if (this.scrollPane != null) {
            this.scrollPane.setMinimumSize(p);
        }
    }

    @Override
    public void setMaximumSize(Dimension p) {
        super.setMaximumSize(p);
        if (this.scrollPane != null) {
            this.scrollPane.setMaximumSize(p);
        }
    }

    @Override
    public void setPreferredSize(Dimension p) {
        super.setPreferredSize(p);
        if (this.scrollPane != null) {
            this.scrollPane.setPreferredSize(p);
        }
    }

    @Override
    public void listChanged(EntityListEvent e) {
        this.proxyTable.fireTableDataChanged();
    }

    @Override
    public void columnAdded(TableColumnModelEvent e) {
        this.resizeTable();
    }

    @Override
    public void columnMarginChanged(ChangeEvent e) {
        this.resizeTable();
    }

    @Override
    public void columnMoved(TableColumnModelEvent e) {
        this.resizeTable();
    }

    @Override
    public void columnRemoved(TableColumnModelEvent e) {
        this.resizeTable();
    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e) {
        this.resizeTable();
    }

    private boolean moveEditCell(int row, int col, int deltaRow, int deltaCol) {
        boolean editable = false;
        int i = 0;
        while (i < this.columnModel.getColumnCount()) {
            if (((EntityTableColumn)this.columnModel.getColumn(i)).isEditable()) {
                editable = true;
                break;
            }
            ++i;
        }
        if (!editable) {
            return false;
        }
        int newRow = row + deltaRow;
        int newCol = col + deltaCol;
        EntityJTable table = this.getTableAt(col);
        if (table == null) {
            return false;
        }
        if (this.getTableAt(newCol) == null) {
            if (newCol >= this.columnModel.getRealColumnCount()) {
                if (newRow == this.entityList.getCount() - 1) {
                    this.addVirtualEntity();
                }
                ++deltaRow;
            }
            if ((newCol %= this.columnModel.getRealColumnCount()) < 0) {
                newCol += this.columnModel.getRealColumnCount();
                --deltaRow;
            }
            return this.moveEditCell(row, col, deltaRow, newCol - col);
        }
        if (newRow < 0) {
            newRow = 0;
        }
        if (newRow >= this.proxyTable.getRowCount()) {
            newRow = this.proxyTable.getRowCount() - 1;
        }
        ProxyColumnModel colModel = this.getColumnModelAt(newCol);
        int newModelCol = this.convertColumnIndexToModel(newCol);
        if (!this.columnModel.isEditable(newModelCol) || this.columnModel.isHidden(newModelCol)) {
            return this.moveEditCell(row, col, deltaRow, deltaCol + 1);
        }
        int newRelativeCol = colModel.getColumnIndex(this.columnModel.getColumn(newModelCol));
        if (this.getTableAt(newCol) != table) {
            TableCellEditor e = table.getCellEditor();
            if (e != null) {
                e.stopCellEditing();
            }
            table.getSelectionModel().removeSelectionInterval(0, this.proxyTable.getRowCount() - 1);
            if (table == this.mainTable) {
                this.scrollPane.getViewport().setViewPosition(this.mainTable.getLocation());
            }
            table = this.getTableAt(newCol);
        }
        return this.editCellAt(table, newRow, newRelativeCol);
    }

    private int convertColumnIndexToModel(int viewCol) {
        int retVal = -1;
        if (this.mainTable == null || this.rowHeader == null) {
            return retVal;
        }
        int i = 0;
        int viewIndex = 0;
        while (i < this.columnModel.getColumnCount()) {
            if (this.columnModel.isLocked(i) && !this.columnModel.isHidden(i)) {
                if (viewIndex == viewCol) {
                    retVal = i;
                    break;
                }
                ++viewIndex;
            }
            ++i;
        }
        if (retVal == -1) {
            i = 0;
            while (i < this.columnModel.getColumnCount()) {
                if (!this.columnModel.isHidden(i) && !this.columnModel.isLocked(i)) {
                    if (viewIndex == viewCol) {
                        retVal = i;
                        break;
                    }
                    ++viewIndex;
                }
                ++i;
            }
        }
        return retVal;
    }

    private EntityJTable getTableAt(int col) {
        EntityJTable retVal = null;
        int modelCol = this.convertColumnIndexToModel(col);
        if (modelCol == -1) {
            return retVal;
        }
        if (this.rowHeaderColumnModel == null || this.proxyColumnModel == null) {
            return retVal;
        }
        try {
            this.rowHeaderColumnModel.getColumnIndex(this.columnModel.getColumn(modelCol));
            retVal = this.rowHeader;
        }
        catch (IllegalArgumentException e1) {
            try {
                this.proxyColumnModel.getColumnIndex(this.columnModel.getColumn(modelCol));
                retVal = this.mainTable;
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        return retVal;
    }

    private ProxyColumnModel getColumnModelAt(int col) {
        ProxyColumnModel retVal = null;
        EntityJTable table = this.getTableAt(col);
        if (table == this.mainTable) {
            retVal = this.proxyColumnModel;
        } else if (table == this.rowHeader) {
            retVal = this.rowHeaderColumnModel;
        }
        return retVal;
    }

    private boolean editCellAt(EntityJTable table, int row, int col) {
        if (table == this.mainTable) {
            this.rowHeader.setSelectionBackground(Color.white);
            this.mainTable.setSelectionBackground(this.selectionBackground);
        } else if (table == this.rowHeader) {
            this.rowHeader.setSelectionBackground(this.selectionBackground);
            this.mainTable.setSelectionBackground(Color.white);
        }
        if (!table.isCellEditable(row, col)) {
            return false;
        }
        JComponent c = (JComponent)table.getEditorComponent();
        if (c != null) {
            c.removeKeyListener(this.cellEditorAdapter);
            c.removeFocusListener(this.cellFocusAdapter);
        }
        this.updateVirtualRow(row);
        boolean retVal = table.editCellAt(row, col);
        c = (JComponent)table.getEditorComponent();
        if (c != null) {
            c.removeKeyListener(this.cellEditorAdapter);
            c.removeFocusListener(this.cellFocusAdapter);
            c.addKeyListener(this.cellEditorAdapter);
            c.addFocusListener(this.cellFocusAdapter);
            c.requestFocus();
            myComponent = c;
        } else if (this.et != null && !((EntityTableColumn)table.getColumnModel().getColumn(col)).isEditable()) {
            this.et.finalizeTerminator();
            this.et = null;
        }
        return retVal;
    }

    private void updateVirtualRow(int row) {
        if (!this.virtualRow || this.currentFocus != EntityTableFocusType.CELL_FOCUS) {
            return;
        }
        if (this.virtualRowEdited) {
            if (row == this.proxyTable.getRowCount() - 1) {
                return;
            }
            this.addVirtualEntity();
            this.virtualRowEdited = false;
        } else if (row == this.entityList.getCount()) {
            this.virtualRowEdited = true;
        }
    }

    private void addVirtualEntity() {
        if (!this.virtualRow) {
            return;
        }
        Object newEntity = this.proxyTable.getVirtualEntity();
        this.proxyTable.setVirtualEntity(null);
        try {
            this.orderTranslatorEntityList.addEntity(newEntity);
        }
        catch (EntityListException e) {
            DialogFactory.showWarningDialog(null, e.getMessage(), "");
        }
    }

    private void setupKeyListener() {
        this.cellEditorAdapter = new KeyAdapter(){

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getSource() instanceof JComboBox) {
                    if (keyEvent.getKeyCode() == 10 && ((JComboBox)keyEvent.getSource()).isPopupVisible()) {
                        return;
                    }
                    if (keyEvent.getKeyCode() == 40 || keyEvent.getKeyCode() == 38) {
                        return;
                    }
                }
                EntityJTable table = EntityTable.this.mainTable;
                if (keyEvent.getSource() == EntityTable.this.mainTable.getEditorComponent()) {
                    table = EntityTable.this.mainTable;
                } else if (keyEvent.getSource() == EntityTable.this.rowHeader.getEditorComponent()) {
                    table = EntityTable.this.rowHeader;
                } else {
                    return;
                }
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();
                if (table == EntityTable.this.rowHeader) {
                    if (EntityTable.this.rowHeaderColumnModel.isVirtualColumnEnabled()) {
                        --col;
                    }
                } else {
                    col += EntityTable.this.rowHeaderColumnModel.getRealColumnCount();
                }
                int deltaRow = row;
                int deltaCol = 0;
                switch (keyEvent.getKeyCode()) {
                    case 10: {
                        ++deltaCol;
                        break;
                    }
                    case 38: {
                        if (keyEvent.getSource() instanceof JComboBox) break;
                        EntityTable.this.setCurrentFocusMode(EntityTableFocusType.ROW_FOCUS);
                        table.getSelectionModel().setSelectionInterval(--deltaRow, deltaRow);
                        break;
                    }
                    case 40: {
                        if (keyEvent.getSource() instanceof JComboBox) break;
                        EntityTable.this.setCurrentFocusMode(EntityTableFocusType.ROW_FOCUS);
                        table.getSelectionModel().setSelectionInterval(++deltaRow, deltaRow);
                        break;
                    }
                    default: {
                        return;
                    }
                }
                keyEvent.consume();
            }
        };
        KeyAdapter keyAdapter = new KeyAdapter(){

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getSource() instanceof JComboBox) {
                    if (keyEvent.getKeyCode() == 10 && ((JComboBox)keyEvent.getSource()).isPopupVisible()) {
                        return;
                    }
                    if (keyEvent.getKeyCode() == 40 || keyEvent.getKeyCode() == 38) {
                        return;
                    }
                }
                EntityJTable table = (EntityJTable)keyEvent.getSource();
                if (EntityTable.this.getCurrentFocusMode() == EntityTableFocusType.TABLE_FOCUS) {
                    switch (keyEvent.getKeyCode()) {
                        default: 
                    }
                } else if (EntityTable.this.getCurrentFocusMode() == EntityTableFocusType.ROW_FOCUS) {
                    switch (keyEvent.getKeyCode()) {
                        case 10: {
                            break;
                        }
                        case 40: {
                            int row = table.getSelectedRow();
                            if (row < EntityTable.this.entityList.getCount() - 1) {
                                table.getSelectionModel().setSelectionInterval(row + 1, row + 1);
                            } else if (row < EntityTable.this.entityList.getCount()) {
                                EntityTable.this.fireLastRowReachedEvent();
                                table.getSelectionModel().setSelectionInterval(row + 1, row + 1);
                            }
                            keyEvent.consume();
                        }
                    }
                } else if (EntityTable.this.getCurrentFocusMode() == EntityTableFocusType.CELL_FOCUS) {
                    int row = table.getSelectedRow();
                    int col = table.getSelectedColumn();
                    if (table == EntityTable.this.rowHeader) {
                        if (EntityTable.this.rowHeaderColumnModel.isVirtualColumnEnabled()) {
                            --col;
                        }
                    } else {
                        col += EntityTable.this.rowHeaderColumnModel.getRealColumnCount();
                    }
                    int deltaRow = row;
                    int deltaCol = 0;
                    switch (keyEvent.getKeyCode()) {
                        case 10: {
                            ++deltaCol;
                            break;
                        }
                        case 38: {
                            --deltaRow;
                            break;
                        }
                        case 40: {
                            ++deltaRow;
                            break;
                        }
                        case 37: {
                            --deltaCol;
                            break;
                        }
                        case 39: {
                            ++deltaCol;
                            break;
                        }
                    }
                    if (deltaCol != 0 || deltaRow != 0) {
                        table.getSelectionModel().setSelectionInterval(deltaRow, deltaRow);
                        keyEvent.consume();
                    }
                }
            }
        };
        this.mainTable.addKeyListener(keyAdapter);
        this.rowHeader.addKeyListener(keyAdapter);
    }

    public void addEntityTableListener(EntityTableListener listener) {
        this.entityTableListenerList.add(listener);
    }

    public void removeEntityTableListener(EntityTableListener listener) {
        this.entityTableListenerList.remove(listener);
    }

    public void clearEntityTableListener() {
        this.entityTableListenerList.clear();
    }

    private void fireLastRowReachedEvent() {
        Iterator<EntityTableListener> it = this.entityTableListenerList.iterator();
        while (it.hasNext()) {
            it.next().lastRowReachedEventFired();
        }
    }

    private void fireOrderByColumnEvent(int col, boolean ascending) {
        Iterator<EntityTableListener> it = this.entityTableListenerList.iterator();
        while (it.hasNext()) {
            it.next().orderByColumnEventFired(col, ascending);
        }
    }

    private void setupMouseListener() {
        PopupListener popupListener = new PopupListener();
        this.mainTable.addMouseListener(popupListener);
        this.mainTable.getTableHeader().addMouseListener(popupListener);
        this.rowHeader.addMouseListener(popupListener);
        this.rowHeader.getTableHeader().addMouseListener(popupListener);
        MouseAdapter mouseListener = new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent evt) {
                EntityJTable table = null;
                if (evt.getSource() == EntityTable.this.mainTable || evt.getSource() == EntityTable.this.rowHeader) {
                    table = (EntityJTable)evt.getSource();
                    int viewCol = table.getColumnModel().getColumnIndexAtX(evt.getX());
                    int modelCol = table.convertColumnIndexToModel(viewCol);
                    int row = table.rowAtPoint(evt.getPoint());
                    if (row == -1 || modelCol == -1) {
                        System.out.println("Out of bounds mouse click consumed");
                        evt.consume();
                        return;
                    }
                    if (table == EntityTable.this.rowHeader && modelCol == 0 && EntityTable.this.virtualColumn) {
                        if (row < EntityTable.this.entityList.getCount()) {
                            if (!EntityTable.this.mainTable.getRowSelectionAllowed()) {
                                EntityTable.this.mainTable.setRowSelectionAllowed(true);
                                if (EntityTable.this.rowHeader != null) {
                                    EntityTable.this.rowHeader.setRowSelectionAllowed(true);
                                }
                            }
                            if (EntityTable.this.getCurrentFocusMode() != EntityTableFocusType.ROW_FOCUS) {
                                EntityTable.this.setCurrentFocusMode(EntityTableFocusType.ROW_FOCUS);
                                if (!EntityTable.this.mainTable.getRowSelectionAllowed()) {
                                    EntityTable.this.mainTable.setRowSelectionAllowed(true);
                                    if (EntityTable.this.rowHeader != null) {
                                        EntityTable.this.rowHeader.setRowSelectionAllowed(true);
                                    }
                                }
                                EntityTable.this.mainTable.getSelectionModel().setSelectionInterval(row, row);
                            }
                        } else {
                            evt.consume();
                        }
                        EntityTable.this.dispatchMyEvent(evt);
                        return;
                    }
                    if (evt.getClickCount() == 2) {
                        if (EntityTable.this.isCellFocusAllowed()) {
                            EntityTable.this.setCurrentFocusMode(EntityTableFocusType.CELL_FOCUS);
                            EntityTable.this.editCellAt(table, row, viewCol);
                            evt.consume();
                        } else if (EntityTable.this.isRowFocusAllowed()) {
                            EntityTable.this.setCurrentFocusMode(EntityTableFocusType.ROW_FOCUS);
                            if (row >= 0 && row < EntityTable.this.entityList.getCount()) {
                                table.getSelectionModel().addSelectionInterval(row, row);
                            } else {
                                evt.consume();
                            }
                        } else if (EntityTable.this.isTableFocusAllowed()) {
                            EntityTable.this.setCurrentFocusMode(EntityTableFocusType.TABLE_FOCUS);
                            table.getSelectionModel().setSelectionInterval(0, EntityTable.this.entityList.getCount() - 1);
                        }
                    } else if (evt.getClickCount() == 1) {
                        table.getSelectionModel().setSelectionInterval(row, row);
                        evt.consume();
                    }
                } else if (evt.getSource() == EntityTable.this.mainTable.getTableHeader()) {
                    int viewCol = EntityTable.this.mainTable.getTableHeader().getColumnModel().getColumnIndexAtX(evt.getX());
                    int modelCol = EntityTable.this.mainTable.convertColumnIndexToModel(viewCol);
                    if (evt.getClickCount() == 2 && modelCol != -1) {
                        new SortWaitDialog(JUMPWorkbench.getFrameInstance(), true, EntityTable.this.orderTranslatorEntityList, modelCol).setVisible(true);
                    }
                } else if (evt.getSource() == EntityTable.this.rowHeader.getTableHeader()) {
                    int viewCol = EntityTable.this.rowHeader.getTableHeader().getColumnModel().getColumnIndexAtX(evt.getX());
                    int modelCol = EntityTable.this.rowHeader.convertColumnIndexToModel(viewCol);
                    if (evt.getClickCount() == 1 && EntityTable.this.virtualColumn && modelCol == 0) {
                        EntityTable.this.setCurrentFocusMode(EntityTableFocusType.TABLE_FOCUS);
                        if (EntityTable.this.getCurrentFocusMode() == EntityTableFocusType.TABLE_FOCUS) {
                            EntityTable.this.mainTable.getSelectionModel().setSelectionInterval(0, EntityTable.this.entityList.getCount() - 1);
                        }
                    } else if (evt.getClickCount() == 2 && modelCol != -1) {
                        if (EntityTable.this.virtualColumn) {
                            --modelCol;
                        }
                        if (modelCol >= 0) {
                            EntityTable.this.orderTranslatorEntityList.sortByColumn(modelCol);
                        }
                    }
                }
                EntityTable.this.dispatchMyEvent(evt);
            }
        };
        this.mainTable.addMouseListener(mouseListener);
        this.mainTable.getTableHeader().addMouseListener(mouseListener);
        this.rowHeader.addMouseListener(mouseListener);
        this.rowHeader.getTableHeader().addMouseListener(mouseListener);
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        final EntityTableColumnModel columnas = this.getColumnModel();
        int i = 0;
        while (i < columnas.getColumnCount()) {
            final int pos = i;
            String name = columnas.getColumnName(i);
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(name);
            menuItem.setSelected(!columnas.isHidden(pos));
            menuItem.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    columnas.setHidden(pos, !columnas.isHidden(pos));
                    EntityTable.this.refresh();
                }
            });
            popup.add(menuItem);
            ++i;
        }
        return popup;
    }

    private void dispatchMyEvent(MouseEvent evt) {
        int x = evt.getX();
        int y = evt.getY();
        if (evt.getSource() == this.mainTable) {
            x += this.rowHeader.getWidth();
        }
        super.dispatchEvent(new MouseEvent((Component)evt.getSource(), evt.getID(), evt.getWhen(), evt.getModifiers(), x, y, evt.getClickCount(), false));
    }

    private boolean isColumnAtEventPointEditable(MouseEvent evt) {
        EntityTableColumn column;
        return evt.getSource() == this.rowHeader ? (column = (EntityTableColumn)this.rowHeaderColumnModel.getColumn(this.rowHeader.columnAtPoint(evt.getPoint()))).isEditable() && !this.rowHeaderModel.isReadOnly() : evt.getSource() == this.mainTable && (column = (EntityTableColumn)this.proxyColumnModel.getColumn(this.mainTable.columnAtPoint(evt.getPoint()))).isEditable() && !this.proxyTable.isReadOnly();
    }

    private void initComponents() {
        this.orderTranslatorEntityList = new OrderTranslatorEntityList();
        this.proxyColumnModel = new ProxyColumnModel();
        this.proxyTable = new ProxyTableModel();
        this.rowHeaderColumnModel = new RowHeaderColumnModel();
        this.rowHeaderModel = new RowHeaderModel();
        this.proxyListSelectionModel = new ProxyListSelectionModel();
        this.scrollPane = new JScrollPane();
        this.mainTable = new EntityJTable();
        this.mainTable.getTableHeader().setFont(this.mainTable.getTableHeader().getFont().deriveFont(1));
        this.orderTranslatorEntityList.setEntityList(this.entityList);
        this.orderTranslatorEntityList.setOrderLockedToList(this.orderLocked);
        this.orderTranslatorEntityList.setColumnModel(this.columnModel);
        this.orderTranslatorEntityList.setRowSortingAllowed(this.sortingAllowed);
        this.proxyColumnModel.setModel(this.columnModel);
        this.proxyTable.setEntityList(this.orderTranslatorEntityList);
        this.proxyTable.setColumnModel(this.columnModel);
        this.proxyTable.setVirtualRowEnabled(this.virtualRow);
        this.proxyTable.setReadOnly(this.readOnly);
        this.rowHeaderColumnModel.setVirtualColumnEnabled(this.virtualColumn);
        this.rowHeaderColumnModel.setModel(this.columnModel);
        this.rowHeaderModel.setRowNumberDisplayed(this.rowNumbers);
        this.rowHeaderModel.setEntityList(this.orderTranslatorEntityList);
        this.rowHeaderModel.setMainTableModel(this.proxyTable);
        this.rowHeaderModel.setVirtualColumnEnabled(this.virtualColumn);
        this.rowHeaderModel.setColumnModel(this.columnModel);
        this.rowHeaderModel.setEntityIndexDisplayed(this.entityIndex);
        this.rowHeaderModel.setVirtualRowEnabled(this.virtualRow);
        this.rowHeaderModel.setReadOnly(this.readOnly);
        this.proxyListSelectionModel.setSelectionModel(this.mainTable.getSelectionModel());
        this.proxyListSelectionModel.setOrderTranslatorEntityList(this.orderTranslatorEntityList);
        this.setLayout(new BorderLayout());
        this.scrollPane.setMinimumSize(this.getMinimumSize());
        this.scrollPane.setMaximumSize(this.getMaximumSize());
        this.mainTable.setModel(this.proxyTable);
        this.mainTable.setColumnModel(this.proxyColumnModel);
        this.mainTable.setPreferredSize(new Dimension(this.columnModel.getTotalColumnWidth(), 400));
        this.mainTable.setAutoResizeMode(0);
        this.mainTable.setCellSelectionEnabled(this.getCurrentFocusMode() == EntityTableFocusType.CELL_FOCUS);
        this.mainTable.setMaximumSize(new Dimension(65000, 65000));
        this.mainTable.setPreferredScrollableViewportSize(new Dimension(4000, 2000));
        this.mainTable.setRowSelectionAllowed(this.getCurrentFocusMode() != EntityTableFocusType.CELL_FOCUS);
        this.mainTable.setAutoCreateColumnsFromModel(false);
        this.mainTable.setMinimumSize(new Dimension(10, 10));
        this.scrollPane.setViewportView(this.mainTable);
        this.add((Component)this.scrollPane, "Center");
    }

    private class EditingTerminator {
        private int currentRow = -1;
        private int currentColumn = -1;
        protected JComponent currentEditor = null;
        protected EntityJTable table = null;
        protected AWTEventListener awtListener = null;

        public EditingTerminator() {
        }

        public EditingTerminator(Object n_currentEditor, EntityJTable n_table) {
            this.currentEditor = (JComponent)n_currentEditor;
            this.table = n_table;
            this.currentRow = this.table.getEditingRow();
            this.currentColumn = this.table.getEditingColumn();
            this.awtListener = new AWTEventListener(){

                @Override
                public void eventDispatched(AWTEvent event) {
                    if (EditingTerminator.this.currentEditor != null && event.getID() == 1004) {
                        Rectangle rr = null;
                        Point pp = null;
                        try {
                            rr = new Rectangle(EditingTerminator.this.currentEditor.getLocationOnScreen(), EditingTerminator.this.currentEditor.getSize());
                            pp = new Point(((JComponent)event.getSource()).getLocationOnScreen());
                        }
                        catch (IllegalComponentStateException ex) {
                            return;
                        }
                        Container parent = EditingTerminator.this.currentEditor;
                        while (parent != null) {
                            if (parent instanceof JDialog || parent instanceof JFrame) break;
                            parent = parent.getParent();
                        }
                        Component newParent = (Component)event.getSource();
                        while (newParent != null) {
                            if (newParent instanceof JDialog || newParent instanceof JFrame) break;
                            newParent = newParent.getParent();
                        }
                        if (parent != null && !parent.equals(newParent)) {
                            return;
                        }
                        newParent = (Component)event.getSource();
                        while (newParent != null) {
                            if (newParent instanceof JTable) break;
                            newParent = newParent.getParent();
                        }
                        if (event.getSource() == EditingTerminator.this.table) {
                            return;
                        }
                        if (newParent == EntityTable.this.rowHeader) {
                            if (EntityTable.this.mainTable.getCellEditor() != null) {
                                EntityTable.this.mainTable.getCellEditor().stopCellEditing();
                                return;
                            }
                        } else if (newParent == EntityTable.this.mainTable && EntityTable.this.rowHeader.getCellEditor() != null) {
                            EntityTable.this.rowHeader.getCellEditor().stopCellEditing();
                            return;
                        }
                        if (!rr.contains(pp)) {
                            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                            EditingTerminator.this.myTerminate();
                        }
                    }
                }
            };
            Toolkit.getDefaultToolkit().addAWTEventListener(this.awtListener, 4L);
        }

        private void myTerminate() {
            TableCellEditor ce;
            if (this.table != null && (ce = this.table.getCellEditor()) != null) {
                TableCellEditor editor = this.table.getCellEditor();
                if (this.currentRow == this.table.getSelectedRow() && this.currentColumn == this.table.getSelectedColumn() && editor != null) {
                    Object value = editor.getCellEditorValue();
                    this.table.setValueAt(value, this.table.getEditingRow(), this.table.getEditingColumn());
                    editor.removeCellEditorListener(this.table);
                    if (this.currentEditor != null) {
                        this.table.remove(this.currentEditor);
                    }
                    if (this.table.getCellEditor().getTableCellEditorComponent(this.table, null, true, this.table.getSelectedRow(), this.table.getSelectedColumn()) != null) {
                        this.table.remove(this.table.getCellEditor().getTableCellEditorComponent(this.table, null, true, this.table.getSelectedRow(), this.table.getSelectedColumn()));
                    }
                    Rectangle cellRect = this.table.getCellRect(this.table.getEditingRow(), this.table.getEditingColumn(), false);
                    this.table.setCellEditor(null);
                    this.table.repaint(cellRect);
                    this.table.setEditingColumn(-1);
                    this.table.setEditingRow(-1);
                    this.currentEditor = null;
                }
            }
        }

        protected void finalizeTerminator() {
            Toolkit.getDefaultToolkit().removeAWTEventListener(this.awtListener);
            this.awtListener = null;
        }
    }

    class PopupListener
    extends MouseAdapter {
        PopupListener() {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            this.maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            this.maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                EntityTable.this.popup = EntityTable.this.createPopupMenu();
                EntityTable.this.popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private class SortWaitDialog
    extends JDialog {
        private static final long serialVersionUID = 1L;

        SortWaitDialog(JFrame parent, boolean modal, final OrderTranslatorEntityList model, final int column) {
            super((Frame)parent, modal);
            this.getContentPane().setLayout(new BorderLayout());
            this.setTitle(I18N.getString("org.saig.core.model.data.widgets.DataPanel.sorting-rows"));
            JLabel label = new JLabel();
            label.setIcon(IconLoader.icon("loading.gif"));
            label.setHorizontalAlignment(0);
            this.getContentPane().add((Component)label, "Center");
            this.setSize(new Dimension(200, 100));
            GUIUtil.centreOnWindow(this);
            SwingWorker worker = new SwingWorker(){

                @Override
                public Object construct() {
                    try {
                        model.sortByColumn(column);
                        EntityTable.this.fireOrderByColumnEvent(column, model.isAscending());
                        return model;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        SortWaitDialog.this.dispose();
                        return null;
                    }
                }

                @Override
                public void finished() {
                    SortWaitDialog.this.closeWindow();
                }
            };
            worker.start();
        }

        void closeWindow() {
            this.dispose();
        }
    }
}

