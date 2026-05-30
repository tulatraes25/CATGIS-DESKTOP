/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.log4j.Logger
 *  org.jdesktop.swingx.JXTable
 *  org.jdesktop.swingx.decorator.AlignmentHighlighter
 *  org.jdesktop.swingx.decorator.ColorHighlighter
 *  org.jdesktop.swingx.decorator.HighlightPredicate
 *  org.jdesktop.swingx.decorator.HighlightPredicate$ColumnHighlightPredicate
 *  org.jdesktop.swingx.decorator.Highlighter
 *  org.jdesktop.swingx.decorator.HighlighterFactory
 */
package es.kosmo.desktop.gui.components;

import es.kosmo.desktop.gui.components.ModelComponent;
import es.kosmo.desktop.gui.components.ModelHelperEvent;
import es.kosmo.desktop.gui.renderers.DefaultTableHeaderCellRenderer;
import java.awt.Color;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlignmentHighlighter;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

public class ReflectionModelTableHelper<T>
extends JXTable
implements ModelComponent {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ReflectionModelTableHelper.class);
    protected Class<T> modelClass;
    protected String[] columnTitles;
    protected String[] methodNames;
    protected List<T> records;
    protected HashMap<ModelHelperEvent, ModelComponent> actions = new HashMap();
    protected boolean[] editableColumn;
    protected long rowStart = 1L;

    public ReflectionModelTableHelper() {
        this.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent e) {
                ReflectionModelTableHelper.this.fireEvents();
            }
        });
        this.setGUI();
    }

    protected void setGUI() {
        this.setColumnControlVisible(true);
        this.addHighlighter(HighlighterFactory.createSimpleStriping());
        this.addHighlighter((Highlighter)new ColorHighlighter((HighlightPredicate)new HighlightPredicate.ColumnHighlightPredicate(new int[]{0}), Color.ORANGE, Color.BLACK));
        this.addHighlighter((Highlighter)new AlignmentHighlighter(0));
        this.getTableHeader().setDefaultRenderer(new DefaultTableHeaderCellRenderer());
        this.setFillsViewportHeight(true);
        this.getTableHeader().setReorderingAllowed(false);
    }

    public void addModelWrapper(T mw) {
        this.records.add(mw);
        this.refresh(this.records);
    }

    public void addAllModelWrappers(Collection<T> mws) {
        this.records.addAll(mws);
        this.refresh(this.records);
    }

    public void setModel(Class<T> modelClass, String[] columnTitles, String[] columnNames) {
        this.modelClass = modelClass;
        this.columnTitles = columnTitles;
        this.methodNames = columnNames;
        this.setModel(new RecordTableModel(new LinkedList()));
        this.prepareRowColumn();
        this.setAllColumnsEditable();
    }

    public void setAllColumnsEditable() {
        this.editableColumn = new boolean[this.getColumnCount()];
        int i = 0;
        while (i < this.editableColumn.length) {
            this.editableColumn[i] = i != 0;
            ++i;
        }
    }

    public void setAllColumnsNotEditable() {
        this.editableColumn = new boolean[this.getColumnCount()];
        int i = 0;
        while (i < this.editableColumn.length) {
            this.editableColumn[i] = false;
            ++i;
        }
    }

    public void setColumnEditable(int i, boolean editable) {
        this.editableColumn[i] = editable;
    }

    public void clear() {
        if (this.records != null) {
            this.records.clear();
            this.refresh(this.records);
        }
    }

    public void refresh(List<T> records) {
        if (this.modelClass == null) {
            return;
        }
        this.records = records;
        RecordTableModel model = (RecordTableModel)this.getModel();
        model.records = records;
        model.fireTableDataChanged();
        this.prepareRowColumn();
    }

    protected void prepareRowColumn() {
        this.setAutoResizeMode(0);
        this.getColumnModel().getColumn(0).setMinWidth(20);
        this.getColumnModel().getColumn(0).setPreferredWidth(20);
        this.getColumnModel().getColumn(0).setMaxWidth(20);
        this.setAutoResizeMode(1);
    }

    public T getSelectedModelWrapper() {
        int sc = this.getSelectedRow();
        if (sc >= this.getRowCount()) {
            return null;
        }
        if (sc != -1) {
            if (this.getRowSorter() != null) {
                sc = this.getRowSorter().convertRowIndexToModel(sc);
            }
            return this.records.get(sc);
        }
        return null;
    }

    public List<T> getSelectedModelWrappers() {
        int[] selectedRows = this.getSelectedRows();
        if (!ArrayUtils.isEmpty((int[])selectedRows)) {
            ArrayList<T> selectedObjects = new ArrayList<T>(selectedRows.length);
            int[] nArray = selectedRows;
            int n = selectedRows.length;
            int n2 = 0;
            while (n2 < n) {
                int row;
                int trueRow = row = nArray[n2];
                if (this.getRowSorter() != null) {
                    trueRow = this.getRowSorter().convertRowIndexToModel(row);
                }
                selectedObjects.add(this.records.get(trueRow));
                ++n2;
            }
            return selectedObjects;
        }
        return null;
    }

    public void addAction(ModelHelperEvent event, ModelComponent mc) {
        this.actions.put(event, mc);
    }

    public void fireEvents() {
        Set<ModelHelperEvent> events = this.actions.keySet();
        for (ModelHelperEvent event : events) {
            event.eventFired(this, this.actions.get(event));
        }
    }

    public List<T> getAllModelWrappers() {
        return this.records;
    }

    @Override
    public void refresh() {
    }

    @Override
    public void update() {
    }

    public boolean isCellEditable(int row, int column) {
        return this.editableColumn[column];
    }

    public long getRowStart() {
        return this.rowStart;
    }

    public void setRowStart(long rowStart) {
        this.rowStart = rowStart;
    }

    public void removeSelectedRows() {
        int[] selectedRows = this.getSelectedRows();
        if (!ArrayUtils.isEmpty((int[])selectedRows)) {
            int[] trueRows = new int[selectedRows.length];
            int i = 0;
            while (i < selectedRows.length) {
                int trueRow = selectedRows[i];
                if (this.getRowSorter() != null) {
                    trueRow = this.getRowSorter().convertRowIndexToModel(trueRow);
                }
                trueRows[i] = trueRow;
                ++i;
            }
            Arrays.sort(trueRows);
            ArrayUtils.reverse((int[])trueRows);
            int[] nArray = trueRows;
            int n = trueRows.length;
            int n2 = 0;
            while (n2 < n) {
                int row = nArray[n2];
                this.records.remove(row);
                ++n2;
            }
            RecordTableModel model = (RecordTableModel)this.getModel();
            model.fireTableDataChanged();
        }
    }

    public class RecordTableModel<V>
    extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private List<V> records;

        public RecordTableModel(List<V> records) {
            this.records = records;
        }

        @Override
        public String getColumnName(int i) {
            if (i == 0) {
                return "";
            }
            return ReflectionModelTableHelper.this.columnTitles[--i];
        }

        @Override
        public int getColumnCount() {
            return ReflectionModelTableHelper.this.methodNames.length + 1;
        }

        @Override
        public int getRowCount() {
            return this.records.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return ReflectionModelTableHelper.this.rowStart + (long)rowIndex;
            }
            Object value = null;
            V modelWrapper = this.records.get(rowIndex);
            String methodName = null;
            methodName = this.getColumnClass(--columnIndex + 1).equals(Boolean.class) ? "is" + ReflectionModelTableHelper.this.methodNames[columnIndex] : "get" + ReflectionModelTableHelper.this.methodNames[columnIndex];
            try {
                Method method = modelWrapper.getClass().getMethod(methodName, new Class[0]);
                value = method.invoke(modelWrapper, new Object[0]);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            return value;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            V modelWrapper = this.records.get(rowIndex);
            Class<?> returnClass = this.getColumnClass(--columnIndex + 1);
            String methodName = "set" + ReflectionModelTableHelper.this.methodNames[columnIndex];
            try {
                Method method = modelWrapper.getClass().getMethod(methodName, returnClass);
                method.invoke(modelWrapper, value);
                this.fireTableDataChanged();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Integer.class;
            }
            String methodName = "get" + ReflectionModelTableHelper.this.methodNames[--columnIndex];
            try {
                Method method = ReflectionModelTableHelper.this.modelClass.getMethod(methodName, new Class[0]);
                return method.getReturnType();
            }
            catch (Exception e) {
                try {
                    methodName = "is" + ReflectionModelTableHelper.this.methodNames[columnIndex];
                    Method method = ReflectionModelTableHelper.this.modelClass.getMethod(methodName, new Class[0]);
                    return method.getReturnType();
                }
                catch (Exception e2) {
                    LOGGER.error((Object)"", (Throwable)e2);
                    return null;
                }
            }
        }
    }
}

