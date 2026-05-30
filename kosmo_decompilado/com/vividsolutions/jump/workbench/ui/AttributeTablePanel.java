/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.cresques.cts.ICoordTrans
 *  org.jdesktop.swingx.table.DatePickerCellEditor
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.AttributePanel;
import com.vividsolutions.jump.workbench.ui.AttributeTab;
import com.vividsolutions.jump.workbench.ui.AttributeTablePanelListener;
import com.vividsolutions.jump.workbench.ui.FeatureInfoWriter;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerNameRenderer;
import com.vividsolutions.jump.workbench.ui.LayerTableModel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.EditSelectedFeaturePlugIn;
import es.kosmo.desktop.gui.components.DateTimePickerCellEditor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.cresques.cts.ICoordTrans;
import org.jdesktop.swingx.table.DatePickerCellEditor;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.model.feature.Attribute;
import org.saig.core.util.DateFormatManager;
import org.saig.core.util.SwingWorker;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DateNumberCellRenderer;

public class AttributeTablePanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final ImageIcon ICON_PENCIL = IconLoader.icon("Pencil.gif");
    private LayerListener layerListener;
    private FeatureEditor featureEditor = new FeatureEditor(){

        @Override
        public void edit(PlugInContext context, Feature feature, final Layer myLayer) throws Exception {
            new EditSelectedFeaturePlugIn(){

                @Override
                protected Layer layer(PlugInContext context) {
                    return myLayer;
                }
            }.execute(context, feature, myLayer.isEditable());
        }
    };
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private boolean columnWidthsInitialized = false;
    private int rightClickColumn;
    private MyTable table = new MyTable();
    private TableCellRenderer headerRenderer = new TableCellRenderer(){
        private Icon clearIcon = IconLoader.icon("Clear.gif");
        private Icon downIcon = IconLoader.icon("Down.gif");
        private TableCellRenderer originalRenderer;
        private Icon upIcon;
        private Icon keyIcon;
        private Icon keyUpIcon;
        private Icon keyDownIcon;
        {
            this.originalRenderer = AttributeTablePanel.this.table.getTableHeader().getDefaultRenderer();
            this.upIcon = IconLoader.icon("Up.gif");
            this.keyIcon = IconLoader.icon("key2.gif");
            this.keyUpIcon = IconLoader.icon("key2_up.gif");
            this.keyDownIcon = IconLoader.icon("key2_down.gif");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel)this.originalRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setFont(label.getFont().deriveFont(3));
            boolean isKey = AttributeTablePanel.this.getModel().isPKName(label.getText());
            String sortedColumnName = AttributeTablePanel.this.getModel().getSortedColumnName();
            Attribute attr = null;
            if (sortedColumnName != null) {
                attr = AttributeTablePanel.this.getModel().getLayer().getUltimateFeatureCollectionWrapper().getFeatureSchema().getAttribute(sortedColumnName);
            }
            if (attr == null || !attr.getPublicName().equals(table.getColumnName(column))) {
                if (isKey) {
                    label.setIcon(this.keyIcon);
                } else {
                    label.setIcon(this.clearIcon);
                }
            } else if (AttributeTablePanel.this.getModel().isSortAscending()) {
                if (isKey) {
                    label.setIcon(this.keyUpIcon);
                } else {
                    label.setIcon(this.upIcon);
                }
            } else if (isKey) {
                label.setIcon(this.keyDownIcon);
            } else {
                label.setIcon(this.downIcon);
            }
            label.setHorizontalTextPosition(2);
            label.setHorizontalAlignment(0);
            return label;
        }
    };
    private LayerNameRenderer layerNameRenderer = new LayerNameRenderer();
    private List<AttributeTablePanelListener> listeners = new ArrayList<AttributeTablePanelListener>();
    private WorkbenchContext workbenchContext;
    EditSelectedFeaturePlugIn editFeaturePlugIn = new EditSelectedFeaturePlugIn(){

        @Override
        protected Layer layer(PlugInContext context) {
            return AttributeTablePanel.this.getModel().getLayer();
        }
    };
    private AttributePanel panel;

    public Dimension getTableSize() {
        return this.table.getTableHeader().getPreferredSize();
    }

    public AttributeTablePanel(final LayerTableModel model, boolean addScrollPane, final WorkbenchContext workbenchContext, final AttributePanel panel) {
        this();
        this.panel = panel;
        if (addScrollPane) {
            this.remove(this.table);
            this.remove(this.table.getTableHeader());
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setMinimumSize(new Dimension(300, 200));
            scrollPane.setPreferredSize(new Dimension(300, 200));
            scrollPane.getViewport().add(this.table);
            this.add((Component)scrollPane, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
        }
        this.updateGrid(model.getLayer());
        this.layerListener = new LayerListener(){

            @Override
            public void categoryChanged(CategoryEvent e) {
            }

            @Override
            public void featuresChanged(FeatureEvent e) {
            }

            @Override
            public void layerChanged(LayerEvent e) {
                if (e.getLayerable() != model.getLayer()) {
                    return;
                }
                if (e.getType() == LayerEventType.METADATA_CHANGED) {
                    AttributeTablePanel.this.updateGrid(model.getLayer());
                    AttributeTablePanel.this.repaint();
                }
                if (e.getType() == LayerEventType.COMMITED) {
                    ((AttributeTab)panel.getParent()).setLayerViewPanelUpdates(false);
                    model.clear(false);
                    model.initColumns(model.getLayer());
                    model.fireTableChanged(new TableModelEvent(model, -1));
                    List<Object> keys = model.getLayer().getUltimateFeatureCollectionWrapper().getKeys();
                    model.addAllKeys(keys);
                    ((AttributeTab)panel.getParent()).setLayerViewPanelUpdates(true);
                }
            }
        };
        model.getLayer().getLayerManager().addLayerListener(this.layerListener);
        try {
            JList list = new JList();
            list.setBackground(new JLabel().getBackground());
            this.layerNameRenderer.getListCellRendererComponent(list, model.getLayer(), -1, false, false);
            this.layerNameRenderer.setCheckBoxVisible(false);
            this.table.setModel(model);
            model.addTableModelListener(new TableModelListener(){

                @Override
                public void tableChanged(TableModelEvent e) {
                    if (e.getFirstRow() == -1) {
                        AttributeTablePanel.this.initColumnWidths();
                    }
                }
            });
            this.layerNameRenderer.getLabel().setFont(this.layerNameRenderer.getLabel().getFont().deriveFont(1));
            model.addTableModelListener(new TableModelListener(){

                @Override
                public void tableChanged(TableModelEvent e) {
                    AttributeTablePanel.this.updateLabel();
                }
            });
            this.workbenchContext = workbenchContext;
            this.table.setSelectionModel(new SelectionModelWrapper(this));
            this.table.getTableHeader().setDefaultRenderer(this.headerRenderer);
            this.initColumnWidths();
            this.setToolTips();
            this.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, new FeatureInfoWriter().sidebarColor(model.getLayer())));
            this.table.addKeyListener(new KeyListener(){

                @Override
                public void keyPressed(KeyEvent e) {
                    if (model.getLayer().isEditable() && e.getKeyCode() == 127) {
                        int row = AttributeTablePanel.this.table.getSelectedRow();
                        int column = AttributeTablePanel.this.table.getSelectedColumn();
                        if (!AttributeTablePanel.this.isEditButtonColumn(column)) {
                            AttributeTablePanel.this.table.getModel().setValueAt("", row, column);
                        }
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }

                @Override
                public void keyTyped(KeyEvent e) {
                }
            });
            this.table.addMouseListener(new MouseAdapter(){

                @Override
                public void mouseClicked(MouseEvent e) {
                    block5: {
                        try {
                            int column = AttributeTablePanel.this.table.columnAtPoint(e.getPoint());
                            int row = AttributeTablePanel.this.table.rowAtPoint(e.getPoint());
                            if (!AttributeTablePanel.this.isEditButtonColumn(column)) break block5;
                            PlugInContext context = new PlugInContext(workbenchContext, null, model.getLayer(), null, null);
                            model.getLayer().getLayerManager().getUndoableEditReceiver().startReceiving();
                            try {
                                AttributeTablePanel.this.featureEditor.edit(context, model.getFeature(row), model.getLayer());
                            }
                            finally {
                                model.getLayer().getLayerManager().getUndoableEditReceiver().stopReceiving();
                            }
                            return;
                        }
                        catch (Throwable t) {
                            workbenchContext.getErrorHandler().handleThrowable(t);
                        }
                    }
                }
            });
            this.updateLabel();
        }
        catch (Throwable t) {
            workbenchContext.getErrorHandler().handleThrowable(t);
        }
    }

    private AttributeTablePanel() {
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateGrid(Layer layer) {
        this.table.setShowGrid(layer.isEditable());
    }

    private boolean isEditButtonColumn(int column) {
        return this.getModel().getColumnName(0).equals(this.table.getColumnName(column));
    }

    public void updateLabel() {
        this.layerNameRenderer.getLabel().setText(this.getModel().getRowCount() == 1 ? I18N.getMessage("workbench.ui.AttributeTablePanel.{0}-1-feature-1-selected", new Object[]{this.getModel().getLayer().getTitle()}) : I18N.getMessage("workbench.ui.AttributeTablePanel.{0}-{1}-features-{2}-selected", new Object[]{this.getModel().getLayer().getTitle(), new Integer(this.getModel().getRowCount()), new Integer(this.getTable().getSelectedRowCount())}));
    }

    public LayerTableModel getModel() {
        return (LayerTableModel)this.table.getModel();
    }

    public JTable getTable() {
        return this.table;
    }

    public void addListener(AttributeTablePanelListener listener) {
        this.listeners.add(listener);
    }

    void jbInit() throws Exception {
        this.setLayout(this.gridBagLayout1);
        this.add((Component)this.layerNameRenderer, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, 18, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.table.getTableHeader(), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.table, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, 10, 1, new Insets(0, 0, 0, 200), 0, 0));
    }

    private void initColumnWidths() {
        GUIUtil.chooseGoodColumnWidths(this.table);
        int editButtonWidth = 15;
        this.table.getColumnModel().getColumn(0).setMinWidth(editButtonWidth);
        this.table.getColumnModel().getColumn(0).setMaxWidth(editButtonWidth);
        this.table.getColumnModel().getColumn(0).setPreferredWidth(editButtonWidth);
        this.columnWidthsInitialized = true;
    }

    private void setToolTips() {
        this.table.addMouseMotionListener(new MouseMotionAdapter(){

            @Override
            public void mouseMoved(MouseEvent e) {
                int column = AttributeTablePanel.this.table.columnAtPoint(e.getPoint());
                if (column == -1) {
                    return;
                }
                int row = AttributeTablePanel.this.table.rowAtPoint(e.getPoint());
                if (row == -1) {
                    return;
                }
                Object obj = AttributeTablePanel.this.table.getValueAt(row, column);
                StringBuilder toolTipSB = new StringBuilder("<HTML><b>");
                toolTipSB.append(AttributeTablePanel.this.table.getColumnName(column));
                toolTipSB.append(" [");
                toolTipSB.append(AttributeTablePanel.this.getModel().getLayer().getTitle());
                toolTipSB.append("]</b><br>");
                if (obj != null) {
                    toolTipSB.append("<pre>");
                    if (obj instanceof Date && !(obj instanceof Time) && !(obj instanceof Timestamp)) {
                        toolTipSB.append(DateFormatManager.getDateFormat().format((Date)obj));
                    } else {
                        toolTipSB.append(obj.toString());
                    }
                    toolTipSB.append("</pre>");
                } else {
                    toolTipSB.append("<i><b>NULL</i></b>");
                }
                toolTipSB.append("</HTML>");
                AttributeTablePanel.this.table.setToolTipText(StringUtil.formatTooltip(toolTipSB.toString(), 200, 15));
            }
        });
    }

    private void fireSelectionReplaced() {
        for (AttributeTablePanelListener listener : this.listeners) {
            listener.selectionReplaced(this);
        }
        this.updateLabel();
    }

    public Collection<Feature> getSelectedFeatures() {
        int[] rows = this.table.getSelectedRows();
        LayerTableModel model = (LayerTableModel)this.table.getModel();
        ArrayList<Object> realKeys = new ArrayList<Object>();
        ArrayList<Feature> memoKeys = new ArrayList<Feature>();
        int i = 0;
        while (i < rows.length) {
            Object k = model.getKey(rows[i]);
            if (k instanceof Feature) {
                memoKeys.add((Feature)k);
            } else {
                realKeys.add(k);
            }
            ++i;
        }
        ArrayList<Feature> result = new ArrayList<Feature>();
        if (!realKeys.isEmpty()) {
            result.addAll(model.getLayer().getUltimateFeatureCollectionWrapper().getByPrimaryKeys(realKeys.toArray()));
        }
        result.addAll(memoKeys);
        ICoordTrans coordTrans = model.getLayer().getCoordTrans();
        if (coordTrans != null) {
            for (Feature currentFeature : result) {
                IShapeGeometry pathGeom = ShapeGeometryConverter.jts_to_igeometry(currentFeature.getGeometry());
                pathGeom.reProject(coordTrans);
                Geometry geomRepro = ShapeGeometryConverter.java2d_to_jts(pathGeom.getShp());
                currentFeature.setGeometry(geomRepro);
            }
        }
        return result;
    }

    public LayerNameRenderer getLayerNameRenderer() {
        return this.layerNameRenderer;
    }

    public void setFeatureEditor(FeatureEditor featureEditor) {
        this.featureEditor = featureEditor;
    }

    public void sortSelectedFeatures(boolean ascending) {
        LayerTableModel model = (LayerTableModel)this.table.getModel();
        int[] rows = this.table.getSelectedRows();
        if (model.getRowCount() == rows.length) {
            return;
        }
        Object[] keys = new Object[rows.length];
        int i = 0;
        while (i < keys.length) {
            keys[i] = model.getKey(rows[i]);
            ++i;
        }
        List<Feature> selectedFeatures = model.getLayer().getUltimateFeatureCollectionWrapper().getByPrimaryKeys(keys);
        model.sortSelectedRows(ascending, rows);
        this.panel.selectFeatures(selectedFeatures, model.getLayer());
        this.repaint();
    }

    public AttributePanel getPanel() {
        return this.panel;
    }

    public void setRightClickColumn(int column) {
        this.rightClickColumn = column;
    }

    public void explicitSortRight(boolean ascending) {
        try {
            LayerTableModel model = (LayerTableModel)this.table.getModel();
            if (this.rightClickColumn == -1) {
                return;
            }
            if (this.isEditButtonColumn(this.rightClickColumn)) {
                return;
            }
            ((AttributeTab)this.panel.getParent()).setLayerViewPanelUpdates(false);
            Collection<Feature> col = this.getSelectedFeatures();
            new SortWaitDialog(JUMPWorkbench.getFrameInstance(), true, model, this.table, this.rightClickColumn, new Boolean(ascending)).setVisible(true);
            this.panel.selectFeatures(col, model.getLayer());
            ((AttributeTab)this.panel.getParent()).setLayerViewPanelUpdates(true);
        }
        catch (Throwable t) {
            this.workbenchContext.getErrorHandler().handleThrowable(t);
        }
    }

    public List<LayerListener> getLayerListeners() {
        ArrayList<LayerListener> layerListeners = new ArrayList<LayerListener>(1);
        layerListeners.add(this.layerListener);
        return layerListeners;
    }

    public String getLastRightClickColumnName() {
        return this.table.getColumnName(this.rightClickColumn);
    }

    static /* synthetic */ ImageIcon access$2() {
        return ICON_PENCIL;
    }

    public static interface FeatureEditor {
        public void edit(PlugInContext var1, Feature var2, Layer var3) throws Exception;
    }

    private class MyTable
    extends JTable {
        private static final long serialVersionUID = 1L;
        private final Color LIGHT_GRAY = new Color(230, 230, 230);
        private DateNumberCellRenderer myTableCellRenderer = new DateNumberCellRenderer();

        public MyTable() {
            this.setAutoResizeMode(0);
            GUIUtil.doNotRoundDoubles(this);
            this.setDefaultEditor(Date.class, (TableCellEditor)new DatePickerCellEditor());
            this.setDefaultEditor(Timestamp.class, new DateTimePickerCellEditor());
            this.setDefaultEditor(Time.class, new DateTimePickerCellEditor());
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            Object valor;
            Class<?> columnClass = this.getModel().getColumnClass(column);
            if (columnClass != null && columnClass.equals(Boolean.class) && (valor = this.getModel().getValueAt(row, column)) != null && valor instanceof Boolean) {
                return new TableCellRenderer(){

                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        Component comp = table.getDefaultRenderer(Boolean.class).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        if (!table.isRowSelected(row)) {
                            comp.setBackground(AttributeTablePanel.this.getModel().getLayer().isEditable() || row % 2 == 0 ? Color.white : MyTable.this.LIGHT_GRAY);
                        }
                        return comp;
                    }
                };
            }
            if (!AttributeTablePanel.this.isEditButtonColumn(column)) {
                DateNumberCellRenderer renderer = this.myTableCellRenderer;
                if (this.isRowSelected(row)) {
                    ((JComponent)renderer).setBackground(Color.YELLOW);
                } else {
                    ((JComponent)renderer).setBackground(AttributeTablePanel.this.getModel().getLayer().isEditable() || row % 2 == 0 ? Color.white : this.LIGHT_GRAY);
                }
                return renderer;
            }
            return new TableCellRenderer(){
                private JButton button = new JButton(AttributeTablePanel.access$2());

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    this.button.setToolTipText(I18N.getString("workbench.ui.AttributeTablePanel.edit-geometry"));
                    return this.button;
                }
            };
        }
    }

    private static class SelectionModelWrapper
    implements ListSelectionModel {
        private AttributeTablePanel panel;
        private ListSelectionModel selectionModel;

        public SelectionModelWrapper(AttributeTablePanel panel) {
            this.panel = panel;
            this.selectionModel = panel.table.getSelectionModel();
        }

        @Override
        public void setAnchorSelectionIndex(int index) {
            this.selectionModel.setAnchorSelectionIndex(index);
        }

        @Override
        public void setLeadSelectionIndex(int index) {
            this.selectionModel.setLeadSelectionIndex(index);
        }

        @Override
        public void setSelectionInterval(int index0, int index1) {
            this.selectionModel.setSelectionInterval(index0, index1);
            this.panel.fireSelectionReplaced();
        }

        @Override
        public void setSelectionMode(int selectionMode) {
            this.selectionModel.setSelectionMode(selectionMode);
        }

        @Override
        public void setValueIsAdjusting(boolean valueIsAdjusting) {
            this.selectionModel.setValueIsAdjusting(valueIsAdjusting);
        }

        @Override
        public int getAnchorSelectionIndex() {
            return this.selectionModel.getAnchorSelectionIndex();
        }

        @Override
        public int getLeadSelectionIndex() {
            return this.selectionModel.getLeadSelectionIndex();
        }

        @Override
        public int getMaxSelectionIndex() {
            return this.selectionModel.getMaxSelectionIndex();
        }

        @Override
        public int getMinSelectionIndex() {
            return this.selectionModel.getMinSelectionIndex();
        }

        @Override
        public int getSelectionMode() {
            return this.selectionModel.getSelectionMode();
        }

        @Override
        public boolean getValueIsAdjusting() {
            return this.selectionModel.getValueIsAdjusting();
        }

        @Override
        public boolean isSelectedIndex(int index) {
            return this.selectionModel.isSelectedIndex(index);
        }

        @Override
        public boolean isSelectionEmpty() {
            return this.selectionModel.isSelectionEmpty();
        }

        @Override
        public void addListSelectionListener(ListSelectionListener x) {
            this.selectionModel.addListSelectionListener(x);
        }

        @Override
        public void addSelectionInterval(int index0, int index1) {
            this.selectionModel.addSelectionInterval(index0, index1);
        }

        @Override
        public void clearSelection() {
            this.selectionModel.clearSelection();
        }

        @Override
        public void insertIndexInterval(int index, int length, boolean before) {
            this.selectionModel.insertIndexInterval(index, length, before);
        }

        @Override
        public void removeIndexInterval(int index0, int index1) {
            this.selectionModel.removeIndexInterval(index0, index1);
        }

        @Override
        public void removeListSelectionListener(ListSelectionListener x) {
            this.selectionModel.removeListSelectionListener(x);
        }

        @Override
        public void removeSelectionInterval(int index0, int index1) {
            this.selectionModel.removeSelectionInterval(index0, index1);
        }
    }

    private class SortWaitDialog
    extends JDialog {
        private static final long serialVersionUID = 1L;

        public SortWaitDialog(JFrame parent, boolean modal, final LayerTableModel model, final JTable table, final int column, final Boolean ascending) {
            super((Frame)parent, modal);
            this.getContentPane().setLayout(new BorderLayout());
            this.setTitle(String.valueOf(I18N.getString("workbench.ui.AttributeTablePanel.sorting-rows")) + " ...");
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
                        if (ascending == null) {
                            model.sort(table.getColumnName(column));
                        } else {
                            model.explicitSort(table.getColumnName(AttributeTablePanel.this.rightClickColumn), ascending);
                        }
                        table.repaint();
                        table.getTableHeader().repaint();
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

        public void closeWindow() {
            this.dispose();
        }
    }
}

