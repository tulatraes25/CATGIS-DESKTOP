/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.Range;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.ColorPanel;
import com.vividsolutions.jump.workbench.ui.EnableableToolBar;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyleListCellRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorScheme;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorSchemeListCellRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingTableModel;
import com.vividsolutions.jump.workbench.ui.renderer.style.DiscreteColorThemingState;
import com.vividsolutions.jump.workbench.ui.renderer.style.RangeColorThemingState;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import com.vividsolutions.jump.workbench.ui.style.BasicStylePanel;
import com.vividsolutions.jump.workbench.ui.style.RenderingStylePanel;
import com.vividsolutions.jump.workbench.ui.style.StylePanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.feature.Attribute;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.simbology.ColorSchemesConfigDialog;
import org.saig.jump.widgets.util.NumberSpinner;

public class ColorThemingStylePanel
extends JPanel
implements StylePanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ColorThemingStylePanel.class);
    public static final String TITLE = I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.title");
    private static final String CUSTOM_ENTRY = String.valueOf(I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.custom")) + "...";
    private static final int DEFAULT_CLASSIFICATION_VALUES_LIMIT = 30;
    private int clasificationValuesLimit = 30;
    private static RenderingStylePanel renderingPanel;
    private String lastAttributeName;
    private WorkbenchContext workbenchContext;
    private Layer layer;
    private JPanel topPanel;
    private JCheckBox enableColorThemingCheckBox;
    private JCheckBox byRangeCheckBox;
    private JLabel attributeLabel;
    private JComboBox attributeNameComboBox;
    private CardLayout cardLayout;
    private JPanel statePanel;
    private JCheckBox maxValJCheckbox;
    private JLabel maxValJLabel;
    private JSpinner maxValJSpinner;
    private JLabel colorSchemeLabel;
    private JComboBox colorSchemeComboBox;
    private JLabel warningJLabel;
    private JPanel tablePanel;
    private JScrollPane scrollPane;
    private JTable table = new JTable(){

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            TableCellRenderer renderer = this.getCellRendererProper(row, column);
            if (renderer instanceof JComponent) {
                ColorThemingStylePanel.this.updateBackground((JComponent)((Object)renderer));
            }
            return renderer;
        }

        private TableCellRenderer getCellRendererProper(int row, int column) {
            if (row == 0 && column == ColorThemingStylePanel.this.attributeColumn()) {
                return ColorThemingStylePanel.this.allOtherValuesRenderer;
            }
            TableCellRenderer renderer = super.getCellRenderer(row, column);
            if (renderer instanceof JLabel) {
                ((JLabel)((Object)renderer)).setHorizontalAlignment(2);
            }
            return renderer;
        }
    };
    private JLabel statusLabel;
    private EnableableToolBar toolBar = new EnableableToolBar();
    private JSlider transparencySlider;
    private DiscreteColorThemingState discreteColorThemingState = new DiscreteColorThemingState(this.table);
    private RangeColorThemingState rangeColorThemingState;
    private State state = this.discreteColorThemingState;
    private DefaultTableCellRenderer allOtherValuesRenderer = new DefaultTableCellRenderer();
    private boolean updatingComponents = false;
    private boolean initializing = true;
    private BasicStyleListCellRenderer basicStyleListCellRenderer = new BasicStyleListCellRenderer();
    private BasicStylePanel basicStylePanel = new BasicStylePanel();
    private HashSet errorMessages = new HashSet();
    private ColorScheme colorSchemeForInserts = null;
    private TableCellEditor basicStyleTableCellEditor = new TableCellEditor(){
        private DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        private BasicStyle originalStyle;
        private DefaultCellEditor editor;
        private JComboBox comboBox = new JComboBox(this.comboBoxModel){

            @Override
            public void setSelectedItem(Object anObject) {
                if (anObject != CUSTOM_ENTRY) {
                    super.setSelectedItem(anObject);
                    return;
                }
                BasicStyle style = ColorThemingStylePanel.this.promptBasicStyle(originalStyle);
                if (style == null) {
                    return;
                }
                comboBox.addItem(style);
                super.setSelectedItem(style);
            }
        };
        {
            this.comboBox.setRenderer(ColorThemingStylePanel.this.basicStyleListCellRenderer);
            this.editor = new DefaultCellEditor(this.comboBox);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.originalStyle = (BasicStyle)value;
            this.comboBoxModel.removeAllElements();
            this.comboBoxModel.addElement(CUSTOM_ENTRY);
            this.comboBoxModel.addElement(value);
            for (Color color : ColorScheme.create((String)ColorThemingStylePanel.this.colorSchemeComboBox.getSelectedItem()).getColors()) {
                this.comboBoxModel.addElement(new BasicStyle(color));
            }
            this.comboBoxModel.setSelectedItem(value);
            return this.editor.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        @Override
        public Object getCellEditorValue() {
            return this.editor.getCellEditorValue();
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return this.editor.isCellEditable(anEvent);
        }

        @Override
        public boolean shouldSelectCell(EventObject anEvent) {
            return this.editor.shouldSelectCell(anEvent);
        }

        @Override
        public boolean stopCellEditing() {
            return this.editor.stopCellEditing();
        }

        @Override
        public void cancelCellEditing() {
            this.editor.cancelCellEditing();
        }

        @Override
        public void addCellEditorListener(CellEditorListener l) {
            this.editor.addCellEditorListener(l);
        }

        @Override
        public void removeCellEditorListener(CellEditorListener l) {
            this.editor.removeCellEditorListener(l);
        }
    };
    private DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
    public static final String COLOR_SCHEME_KEY;
    private MyPlugIn configColorSchemesPlugIn = new MyPlugIn(){

        @Override
        public String getName() {
            return I18N.getString("com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStylePanel.configure-color-schemes");
        }

        @Override
        public Icon getIcon() {
            return GUIUtil.toSmallIcon(IconLoader.icon("Palette.gif"));
        }

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            this.reportNothingToUndoYet(context);
            ColorSchemesConfigDialog dialog = new ColorSchemesConfigDialog((JDialog)FormUtils.getWindowForComponent(ColorThemingStylePanel.this), true);
            GUIUtil.centreOnScreen(dialog);
            dialog.setVisible(true);
            ColorThemingStylePanel.this.setState(ColorThemingStylePanel.this.state);
            return true;
        }
    };
    private MyPlugIn insertPlugIn = new MyPlugIn(){

        @Override
        public String getName() {
            return I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.insert-row");
        }

        @Override
        public Icon getIcon() {
            return GUIUtil.toSmallIcon(IconLoader.icon("Plus.gif"));
        }

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            this.reportNothingToUndoYet(context);
            ColorThemingStylePanel.this.stopCellEditing();
            ColorThemingStylePanel.this.tableModel().insertAttributeValue(ColorThemingStylePanel.this.table.getSelectedRowCount() > 0 ? ColorThemingStylePanel.this.table.getSelectedRows()[0] : ColorThemingStylePanel.this.table.getRowCount(), ColorThemingStylePanel.this.getColorSchemeForInserts());
            if (ColorThemingStylePanel.this.table.getSelectedRowCount() == 0) {
                ColorThemingStylePanel.this.table.scrollRectToVisible(ColorThemingStylePanel.this.table.getCellRect(ColorThemingStylePanel.this.table.getRowCount() - 1, 0, true));
            }
            if (ColorThemingStylePanel.this.table.getSelectedRowCount() != 0) {
                int firstSelectedRow = ColorThemingStylePanel.this.table.getSelectedRows()[0];
                ColorThemingStylePanel.this.table.clearSelection();
                ColorThemingStylePanel.this.table.addRowSelectionInterval(firstSelectedRow, firstSelectedRow);
            }
            return true;
        }
    };
    private MyPlugIn deletePlugIn = new MyPlugIn(){

        @Override
        public String getName() {
            return I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.delete-row");
        }

        @Override
        public Icon getIcon() {
            return GUIUtil.toSmallIcon(IconLoader.icon("Delete.gif"));
        }

        @Override
        public boolean execute(PlugInContext context) throws Exception {
            this.reportNothingToUndoYet(context);
            ColorThemingStylePanel.this.stopCellEditing();
            ColorThemingStylePanel.this.tableModel().removeAttributeValues(ColorThemingStylePanel.this.table.getSelectedRows());
            return true;
        }
    };

    static {
        COLOR_SCHEME_KEY = String.valueOf(ColorThemingStylePanel.class.getName()) + " - COLOR SCHEME";
    }

    public ColorThemingStylePanel(Layer layer, WorkbenchContext workbenchContext, RenderingStylePanel renderingStylePanel) {
        try {
            try {
                this.layer = layer;
                this.workbenchContext = workbenchContext;
                renderingPanel = renderingStylePanel;
                this.rangeColorThemingState = new RangeColorThemingState(this);
                this.jbInit();
                this.byRangeCheckBox.setSelected(this.colorThemingStyleHasRanges(layer));
                this.state = this.byRangeCheckBox.isSelected() ? this.rangeColorThemingState : this.discreteColorThemingState;
                this.initTable(layer);
                this.setState(this.state);
                this.initAttributeNameComboBox(layer);
                this.initColorSchemeComboBox(layer.getLayerManager());
                this.initTransparencySlider(layer);
                this.initToolBar();
                this.enableColorThemingCheckBox.setSelected(ColorThemingStyle.get(layer).isEnabled());
                this.updateComponents();
                GUIUtil.sync(this.basicStylePanel.getTransparencySlider(), this.transparencySlider);
                this.basicStylePanel.setSynchronizingLineColor(layer.isSynchronizingLineColor());
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.initializing = false;
            }
        }
        finally {
            this.initializing = false;
        }
    }

    private void updateBackground(JComponent component) {
        component.setBackground(this.enableColorThemingCheckBox.isSelected() ? Color.white : this.getBackground());
    }

    private int attributeColumn() {
        return this.table.convertColumnIndexToView(1);
    }

    private int colorColumn() {
        return this.table.convertColumnIndexToView(0);
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Style updateStyles() {
        ColorThemingStyle estilo = null;
        this.layer.removeStyle(ColorThemingStyle.get(this.layer));
        String attributePublicName = this.getAttributePublicName();
        Attribute attr = this.layer.getFeatureSchema().getPublicAttribute(attributePublicName);
        String attributeName = attr.getName();
        estilo = new ColorThemingStyle(attributeName, this.state.toExternalFormat(this.tableModel().getAttributeValueToBasicStyleMap()), this.tableModel().getDefaultStyle());
        this.layer.addStyle(estilo);
        ColorThemingStyle.get(this.layer).setAlpha(this.getAlpha());
        ColorThemingStyle.get(this.layer).setEnabled(this.enableColorThemingCheckBox.isSelected());
        this.layer.getBasicStyle().setEnabled(!this.enableColorThemingCheckBox.isSelected());
        return estilo;
    }

    private String getAttributePublicName() {
        return (String)this.attributeNameComboBox.getSelectedItem();
    }

    private void stopCellEditing() {
        if (this.table.getCellEditor() instanceof DefaultCellEditor) {
            ((DefaultCellEditor)this.table.getCellEditor()).stopCellEditing();
        }
    }

    public JCheckBox getSynchronizeCheckBox() {
        return this.basicStylePanel.getSynchronizeCheckBox();
    }

    public Layer getLayer() {
        return this.layer;
    }

    private void initTransparencySlider(Layer layer) {
        this.transparencySlider.setValue(this.transparencySlider.getMaximum() - ColorThemingStyle.get(layer).getDefaultStyle().getAlpha());
        this.transparencySlider.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                ColorThemingStylePanel.this.basicStyleListCellRenderer.setAlpha(ColorThemingStylePanel.this.getAlpha());
            }
        });
        this.basicStyleListCellRenderer.setAlpha(this.getAlpha());
    }

    private boolean colorThemingStyleHasRanges(Layer layer) {
        return !ColorThemingStyle.get(layer).getAttributeValueToBasicStyleMap().isEmpty() && ColorThemingStyle.get(layer).getAttributeValueToBasicStyleMap().keySet().iterator().next() instanceof Range;
    }

    private void initToolBar() {
        EnableCheck atLeast1RowMustBeSelectedCheck = new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return ColorThemingStylePanel.this.table.getSelectedRowCount() == 0 ? I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.at-least-1-row-must-be-selected") : null;
            }
        };
        EnableCheck layerMustHaveAtLeast1AttributeCheck = new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return ColorThemingStylePanel.this.attributeNameComboBox.getItemCount() == 0 ? I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.layer-must-have-at-least-1-attribute") : null;
            }
        };
        EnableCheck colorThemingMustBeEnabledCheck = new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return !ColorThemingStylePanel.this.enableColorThemingCheckBox.isSelected() ? I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.colour-theming-must-be-enabled") : null;
            }
        };
        this.addPlugIn(this.insertPlugIn, new MultiEnableCheck().add(layerMustHaveAtLeast1AttributeCheck).add(colorThemingMustBeEnabledCheck));
        this.addPlugIn(this.deletePlugIn, new MultiEnableCheck().add(layerMustHaveAtLeast1AttributeCheck).add(atLeast1RowMustBeSelectedCheck).add(colorThemingMustBeEnabledCheck));
        this.addPlugIn(this.configColorSchemesPlugIn, new MultiEnableCheck());
    }

    private void addPlugIn(MyPlugIn plugIn, EnableCheck enableCheck) {
        JButton button = new JButton();
        this.toolBar.add(button, plugIn.getName(), plugIn.getIcon(), AbstractPlugIn.toActionListener(plugIn, this.workbenchContext, null), enableCheck);
    }

    private void updateComponents() {
        if (this.updatingComponents) {
            return;
        }
        this.updatingComponents = true;
        try {
            this.attributeLabel.setEnabled(this.enableColorThemingCheckBox.isSelected());
            this.attributeNameComboBox.setEnabled(this.enableColorThemingCheckBox.isSelected());
            this.state.getPanel().setEnabled(this.enableColorThemingCheckBox.isSelected() && this.attributeNameComboBox.getItemCount() > 0);
            this.colorSchemeLabel.setEnabled(this.enableColorThemingCheckBox.isSelected() && this.attributeNameComboBox.getItemCount() > 0);
            this.byRangeCheckBox.setEnabled(this.enableColorThemingCheckBox.isSelected() && this.attributeNameComboBox.getItemCount() > 0);
            this.colorSchemeComboBox.setEnabled(this.enableColorThemingCheckBox.isSelected() && this.attributeNameComboBox.getItemCount() > 0);
            this.table.setEnabled(this.enableColorThemingCheckBox.isSelected() && this.attributeNameComboBox.getItemCount() > 0);
            this.scrollPane.setEnabled(this.enableColorThemingCheckBox.isSelected() && this.attributeNameComboBox.getItemCount() > 0);
            this.transparencySlider.setEnabled(this.enableColorThemingCheckBox.isSelected() && this.attributeNameComboBox.getItemCount() > 0);
            this.maxValJCheckbox.setEnabled(this.enableColorThemingCheckBox.isSelected());
            this.maxValJSpinner.setEnabled(this.enableColorThemingCheckBox.isSelected() && this.maxValJCheckbox.isSelected());
            this.maxValJLabel.setEnabled(this.enableColorThemingCheckBox.isSelected() && this.maxValJCheckbox.isSelected());
            this.statusLabel.setEnabled(this.enableColorThemingCheckBox.isSelected());
            this.toolBar.updateEnabledState();
            if (!this.setErrorMessage(new ErrorMessage(I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.cannot-colour-theme-layer-with-no-attributes")), this.attributeNameComboBox.getItemCount() == 0)) {
                this.setErrorMessage(new ErrorMessage(I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.table-must-not-be-empty")), this.table.getRowCount() == 0);
            }
            this.updateErrorDisplay();
            if (this.table.getColumnCount() > 0) {
                this.table.getColumnModel().getColumn(this.table.convertColumnIndexToView(1)).setHeaderValue(this.state.getAttributeValueColumnTitle());
            }
            this.updateMaxValueWarning();
        }
        finally {
            this.updatingComponents = false;
        }
    }

    private BasicStyle promptBasicStyle(BasicStyle basicStyle) {
        int originalTransparencySliderValue = this.transparencySlider.getValue();
        this.basicStylePanel.setBasicStyle(basicStyle, false, false, false);
        this.basicStylePanel.getTransparencySlider().setValue(originalTransparencySliderValue);
        OKCancelPanel okCancelPanel = new OKCancelPanel();
        final JDialog dialog = new JDialog((JDialog)SwingUtilities.windowForComponent(this), I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.custom"), true);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add((Component)this.basicStylePanel, "Center");
        dialog.getContentPane().add((Component)okCancelPanel, "South");
        okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });
        dialog.pack();
        GUIUtil.centreOnWindow(dialog);
        dialog.setVisible(true);
        if (!okCancelPanel.wasOKPressed()) {
            this.transparencySlider.setValue(originalTransparencySliderValue);
        }
        return okCancelPanel.wasOKPressed() ? this.basicStylePanel.getBasicStyle() : null;
    }

    private void initTable(Layer layer) {
        ColorThemingStyle themingStyle = ColorThemingStyle.get(layer);
        themingStyle.setDefaultStyle(renderingPanel.getBasicStyle());
        this.table.setModel(new ColorThemingTableModel(themingStyle.getDefaultStyle(), themingStyle.getAttributeName(), this.attributeValueToBasicStyleMap(layer), layer.getFeatureCollectionWrapper().getFeatureSchema()){

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                if (rowIndex == 0 && columnIndex == 1) {
                    return ColorThemingStylePanel.this.state.getAllOtherValuesDescription();
                }
                return super.getValueAt(rowIndex, columnIndex);
            }
        });
        this.table.createDefaultColumnsFromModel();
        this.table.setRowSelectionAllowed(true);
        this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent e) {
                ColorThemingStylePanel.this.updateComponents();
            }
        });
        this.table.getTableHeader().addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1 && ColorThemingStylePanel.this.table.isEnabled()) {
                    ColorThemingStylePanel.this.tableModel().sort();
                }
            }
        });
        this.table.getColumnModel().getColumn(this.colorColumn()).setCellRenderer(new TableCellRenderer(){

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JComponent renderer = (JComponent)ColorThemingStylePanel.this.basicStyleListCellRenderer.getListCellRendererComponent(new JList(), value, row, isSelected, hasFocus);
                if (!isSelected) {
                    ColorThemingStylePanel.this.updateBackground(renderer);
                }
                return renderer;
            }
        });
        this.table.getColumnModel().getColumn(this.colorColumn()).setCellEditor(this.basicStyleTableCellEditor);
        this.table.getModel().addTableModelListener(new TableModelListener(){

            @Override
            public void tableChanged(TableModelEvent e) {
                ColorThemingStylePanel.this.updateComponents();
                Object duplicateAttributeValue = ColorThemingStylePanel.this.tableModel().findDuplicateAttributeValue();
                ColorThemingStylePanel.this.setErrorMessage(new ErrorMessage(I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.table-must-not-have-duplicate-attribute-values"), "(" + duplicateAttributeValue + ")"), duplicateAttributeValue != null);
                ColorThemingStylePanel.this.setErrorMessage(new ErrorMessage(I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.table-must-not-have-null-attribute-values")), ColorThemingStylePanel.this.tableModel().containsNullAttributeValues());
            }
        });
        int colorWidth = 10 + (int)this.basicStyleListCellRenderer.getListCellRendererComponent(new JList(), new BasicStyle(), 0, false, false).getPreferredSize().getWidth();
        this.table.getColumnModel().getColumn(this.colorColumn()).setPreferredWidth(colorWidth);
        this.table.getColumnModel().getColumn(this.colorColumn()).setMinWidth(colorWidth);
        this.table.getColumnModel().getColumn(this.colorColumn()).setMaxWidth(colorWidth);
    }

    private Map attributeValueToBasicStyleMap(Layer layer) {
        if (ColorThemingStyle.get(layer).getAttributeName() == null) {
            return new TreeMap();
        }
        if (!layer.getFeatureCollectionWrapper().getFeatureSchema().hasAttribute(ColorThemingStyle.get(layer).getAttributeName())) {
            return new TreeMap();
        }
        return this.state.fromExternalFormat(ColorThemingStyle.get(layer).getAttributeValueToBasicStyleMap());
    }

    private void initColorSchemeComboBox(LayerManager layerManager) {
        this.colorSchemeComboBox.setRenderer(new ColorSchemeListCellRenderer(){

            @Override
            protected void color(ColorPanel colorPanel, Color fillColor, Color lineColor) {
                super.color(colorPanel, GUIUtil.alphaColor(fillColor, ColorThemingStylePanel.this.getAlpha()), GUIUtil.alphaColor(lineColor, ColorThemingStylePanel.this.getAlpha()));
            }

            @Override
            protected ColorScheme colorScheme(String name) {
                return ColorThemingStylePanel.this.state.filterColorScheme(super.colorScheme(name));
            }
        });
    }

    private int getAlpha() {
        return this.transparencySlider.getMaximum() - this.transparencySlider.getValue();
    }

    private void initAttributeNameComboBox(Layer layer) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
        FeatureSchema fs = layer.getFeatureCollectionWrapper().getFeatureSchema();
        ArrayList<String> visibleAttrsPublicNames = new ArrayList<String>();
        int i = 0;
        while (i < fs.getAttributeCount()) {
            Attribute attr;
            if (i != fs.getGeometryIndex() && (attr = fs.getAttribute(i)).isVisibility()) {
                visibleAttrsPublicNames.add(attr.getPublicName());
            }
            ++i;
        }
        Collections.sort(visibleAttrsPublicNames, Collator.getInstance(I18N.getLocale()));
        for (String publicName : visibleAttrsPublicNames) {
            model.addElement(publicName);
        }
        this.attributeNameComboBox.setModel(model);
        if (model.getSize() == 0) {
            return;
        }
        this.attributeNameComboBox.setSelectedItem(ColorThemingStyle.get(layer).getAttributeName());
    }

    private void jbInit() throws Exception {
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, this.getTopPanel());
        FormUtils.addRowInGBL(this, 1, 0, this.getTablePanel());
        FormUtils.addFiller(this, 2, 0);
    }

    private JComponent getTopPanel() {
        if (this.topPanel == null) {
            this.topPanel = new JPanel(new GridBagLayout());
            this.enableColorThemingCheckBox = new JCheckBox(I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.enable-colour-theming"));
            this.enableColorThemingCheckBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ColorThemingStylePanel.this.enableColorThemingCheckBox_actionPerformed(e);
                }
            });
            this.byRangeCheckBox = new JCheckBox(I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.by-range"));
            this.byRangeCheckBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ColorThemingStylePanel.this.byRangeCheckBox_actionPerformed(e);
                }
            });
            this.attributeLabel = new JLabel(String.valueOf(I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.attribute")) + ": ");
            this.attributeNameComboBox = new JComboBox();
            this.attributeNameComboBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ColorThemingStylePanel.this.attributeNameComboBox_actionPerformed(e);
                }
            });
            this.cardLayout = new CardLayout();
            this.statePanel = new JPanel(this.cardLayout);
            this.statePanel.add((Component)this.discreteColorThemingState.getPanel(), this.discreteColorThemingState.getClass().getName());
            this.statePanel.add((Component)this.rangeColorThemingState.getPanel(), this.rangeColorThemingState.getClass().getName());
            this.maxValJCheckbox = new JCheckBox(I18N.getString("com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStylePanel.Enable-max-value"));
            this.maxValJCheckbox.setSelected(true);
            this.maxValJCheckbox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean selected = ColorThemingStylePanel.this.maxValJCheckbox.isSelected();
                    if (selected) {
                        ColorThemingStylePanel.this.maxValSpinnerChanged();
                        ColorThemingStylePanel.this.updateComponents();
                    } else {
                        ColorThemingStylePanel.this.clasificationValuesLimit = Integer.MAX_VALUE;
                        ColorThemingStylePanel.this.populateTable();
                        ColorThemingStylePanel.this.updateComponents();
                    }
                }
            });
            this.maxValJLabel = new JLabel(I18N.getString("com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStylePanel.Maximum-classified-values"));
            this.maxValJSpinner = new NumberSpinner(this.clasificationValuesLimit, 2, Integer.MAX_VALUE, 1);
            this.maxValJSpinner.setPreferredSize(new Dimension(80, this.maxValJSpinner.getPreferredSize().height));
            this.maxValJSpinner.addChangeListener(new ChangeListener(){

                @Override
                public void stateChanged(ChangeEvent ce) {
                    ColorThemingStylePanel.this.maxValSpinnerChanged();
                }
            });
            this.colorSchemeLabel = new JLabel(String.valueOf(I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.colour-scheme")) + ": ");
            this.colorSchemeComboBox = new JComboBox();
            this.colorSchemeComboBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ColorThemingStylePanel.this.colorSchemeComboBox_actionPerformed(e);
                }
            });
            this.warningJLabel = new JLabel(" ");
            FormUtils.addRowInGBL((JComponent)this.topPanel, 0, 0, (JComponent)this.enableColorThemingCheckBox, false, false);
            FormUtils.addRowInGBL((JComponent)this.topPanel, 0, 1, (JComponent)this.byRangeCheckBox, true, false);
            FormUtils.addRowInGBL((JComponent)this.topPanel, 1, 0, this.attributeLabel, (JComponent)this.attributeNameComboBox, false);
            FormUtils.addRowInGBL((JComponent)this.topPanel, 2, 0, (JComponent)this.statePanel, true, false);
            FormUtils.addRowInGBL((JComponent)this.topPanel, 3, 0, (JComponent)this.maxValJCheckbox, true, false);
            FormUtils.addRowInGBL((JComponent)this.topPanel, 4, 0, this.maxValJLabel, (JComponent)this.maxValJSpinner, false);
            FormUtils.addFiller(this.topPanel, 5, 0);
            FormUtils.addRowInGBL((JComponent)this.topPanel, 5, 1, (JComponent)this.warningJLabel, false, false);
            FormUtils.addRowInGBL((JComponent)this.topPanel, 6, 0, this.colorSchemeLabel, (JComponent)this.colorSchemeComboBox);
        }
        return this.topPanel;
    }

    private JComponent getTablePanel() {
        if (this.tablePanel == null) {
            this.tablePanel = new JPanel(new GridBagLayout());
            this.scrollPane = new JScrollPane(this.table);
            this.scrollPane.setMinimumSize(new Dimension(400, 200));
            this.scrollPane.setPreferredSize(new Dimension(400, 200));
            this.statusLabel = new JLabel(){

                @Override
                public void setText(String text) {
                    super.setText(text);
                    this.setToolTipText(text);
                }
            };
            this.statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
            this.statusLabel.setText(" ");
            this.transparencySlider = new JSlider();
            this.transparencySlider.setMaximum(255);
            this.transparencySlider.setMinimumSize(new Dimension(75, 24));
            this.transparencySlider.setPreferredSize(new Dimension(75, 24));
            this.transparencySlider.setMinimumSize(new Dimension(75, 24));
            this.transparencySlider.setToolTipText(I18N.getString("workbench.ui.renderer.style.ColorThemingStylePanel.transparency"));
            this.transparencySlider.addChangeListener(new ChangeListener(){

                @Override
                public void stateChanged(ChangeEvent e) {
                    ColorThemingStylePanel.this.transparencySlider_stateChanged(e);
                }
            });
            this.toolBar.setFloatable(false);
            FormUtils.addRowInGBL((JComponent)this.tablePanel, 0, 0, (JComponent)this.scrollPane, true, false);
            FormUtils.addRowInGBL((JComponent)this.tablePanel, 1, 0, (JComponent)this.toolBar, false, false);
            FormUtils.addRowInGBL((JComponent)this.tablePanel, 1, 1, (JComponent)this.transparencySlider, false, false);
            FormUtils.addRowInGBL((JComponent)this.tablePanel, 1, 2, (JComponent)this.statusLabel, false, false);
        }
        return this.tablePanel;
    }

    private void maxValSpinnerChanged() {
        this.clasificationValuesLimit = (Integer)this.maxValJSpinner.getValue();
        this.populateTable();
        this.updateComponents();
    }

    protected void enableColorThemingCheckBox_actionPerformed(ActionEvent e) {
        if (this.table.getRowCount() == 1) {
            this.populateTable();
        }
        this.updateComponents();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected void attributeNameComboBox_actionPerformed(ActionEvent e) {
        try {
            if (this.initializing) {
                return;
            }
            if (this.attributeNameComboBox.getItemCount() == 0) {
                return;
            }
            if (this.attributeNameComboBox.getSelectedItem().equals(this.lastAttributeName)) {
                return;
            }
            this.stopCellEditing();
            this.populateTable();
            return;
        }
        finally {
            this.lastAttributeName = this.getAttributePublicName();
            if (this.table.getModel() instanceof ColorThemingTableModel) {
                this.tableModel().setAttributeName(this.getAttributePublicName());
            }
        }
    }

    public ColorThemingTableModel tableModel() {
        return (ColorThemingTableModel)this.table.getModel();
    }

    private SortedSet getNonNullAttributeValues() {
        String attributePublicName = this.getAttributePublicName();
        String attributeName = this.layer.getFeatureSchema().getPublicAttribute(attributePublicName).getName();
        TreeSet distintsValues = (TreeSet)this.layer.getUltimateFeatureCollectionWrapper().getDistintsValues(attributeName, this.clasificationValuesLimit);
        return distintsValues;
    }

    public void populateTable() {
        if (!this.enableColorThemingCheckBox.isSelected() || this.attributeNameComboBox.getItemCount() <= 0) {
            return;
        }
        this.stopCellEditing();
        this.tableModel().clear();
        Collection values = this.state.filterAttributeValues(this.getNonNullAttributeValues());
        this.tableModel().setAttributeValueToBasicStyleMap(this.toAttributeValueToBasicStyleMap(values));
        this.tableModel().sort(this.tableModel().wasLastSortAscending());
        this.applyColorScheme();
        this.updateMaxValueWarning();
    }

    private void updateMaxValueWarning() {
        if (!this.enableColorThemingCheckBox.isSelected() || this.attributeNameComboBox.getItemCount() <= 0) {
            return;
        }
        Collection values = this.state.filterAttributeValues(this.getNonNullAttributeValues());
        if (values.size() >= this.clasificationValuesLimit) {
            this.warningJLabel.setForeground(Color.RED);
            this.warningJLabel.setText(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStylePanel.Warning")) + ":" + I18N.getString("com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStylePanel.There-are-more-values-than-the-maximum-limit"));
        } else {
            this.warningJLabel.setText(" ");
        }
    }

    private Map toAttributeValueToBasicStyleMap(Collection attributeValues) {
        TreeMap attributeValueToBasicStyleMap = new TreeMap();
        for (Object attributeValue : attributeValues) {
            attributeValueToBasicStyleMap.put(attributeValue, new BasicStyle());
        }
        return attributeValueToBasicStyleMap;
    }

    protected void colorSchemeComboBox_actionPerformed(ActionEvent e) {
        if (this.initializing) {
            return;
        }
        this.stopCellEditing();
        this.layer.getLayerManager().getBlackboard().put(COLOR_SCHEME_KEY, this.colorSchemeComboBox.getSelectedItem());
        this.applyColorScheme();
        this.colorSchemeForInserts = null;
    }

    private ColorScheme getColorSchemeForInserts() {
        if (this.colorSchemeForInserts == null || !this.colorSchemeForInserts.getName().equalsIgnoreCase((String)this.colorSchemeComboBox.getSelectedItem())) {
            this.colorSchemeForInserts = ColorScheme.create((String)this.colorSchemeComboBox.getSelectedItem());
        }
        return this.colorSchemeForInserts;
    }

    public void applyColorScheme() {
        this.stopCellEditing();
        this.state.applyColorScheme(this.state.filterColorScheme(ColorScheme.create((String)this.colorSchemeComboBox.getSelectedItem())));
    }

    private void cancelCellEditing() {
        if (this.table.getCellEditor() instanceof DefaultCellEditor) {
            ((DefaultCellEditor)this.table.getCellEditor()).cancelCellEditing();
        }
    }

    @Override
    public String validateInput() {
        this.stopCellEditing();
        return this.internalValidateInput();
    }

    private String internalValidateInput() {
        if (!this.enableColorThemingCheckBox.isSelected()) {
            return null;
        }
        if (this.errorMessages.isEmpty()) {
            return null;
        }
        return this.errorMessages.iterator().next().toString();
    }

    private boolean setErrorMessage(ErrorMessage message, boolean enabled) {
        this.errorMessages.remove(message);
        if (enabled) {
            this.errorMessages.add(message);
        }
        this.updateErrorDisplay();
        return enabled;
    }

    private void updateErrorDisplay() {
        String errorMessage = this.internalValidateInput();
        if (errorMessage != null) {
            this.statusLabel.setText(errorMessage);
            this.statusLabel.setIcon(GUIUtil.toSmallIcon(IconLoader.icon("Delete.gif")));
        } else {
            this.statusLabel.setText(" ");
            this.statusLabel.setIcon(null);
        }
    }

    private void setState(State state) {
        this.state.deactivate();
        this.state = state;
        this.initializing = true;
        try {
            this.colorSchemeComboBox.setModel(new DefaultComboBoxModel(new Vector(state.getColorSchemeNames())));
            this.colorSchemeComboBox.setSelectedItem(this.layer.getLayerManager().getBlackboard().get(COLOR_SCHEME_KEY, this.colorSchemeComboBox.getItemAt(0)));
            this.cardLayout.show(this.statePanel, state.getClass().getName());
        }
        finally {
            this.initializing = false;
        }
        this.updateComponents();
        this.state.activate();
        this.getTopPanel().repaint();
    }

    protected void byRangeCheckBox_actionPerformed(ActionEvent e) {
        this.setState(this.byRangeCheckBox.isSelected() ? this.rangeColorThemingState : this.discreteColorThemingState);
        this.populateTable();
    }

    protected void transparencySlider_stateChanged(ChangeEvent e) {
        this.repaint();
    }

    public JSlider getTransparencySlider() {
        return this.transparencySlider;
    }

    public JTable getTable() {
        return this.table;
    }

    public static BasicStyle getDefaultBasicStyle() {
        return renderingPanel.getBasicStyle();
    }

    private class ErrorMessage {
        private String commonPart;
        private String specificPart;

        public ErrorMessage(String commonPart) {
            this(commonPart, "");
        }

        public ErrorMessage(String commonPart, String specificPart) {
            this.commonPart = commonPart;
            this.specificPart = specificPart;
        }

        public int hashCode() {
            return this.commonPart.hashCode();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof ErrorMessage)) {
                return false;
            }
            return this.commonPart.equals(((ErrorMessage)obj).commonPart);
        }

        public String toString() {
            return String.valueOf(this.commonPart) + this.specificPart;
        }
    }

    private abstract class MyPlugIn
    extends AbstractPlugIn {
        private MyPlugIn() {
        }

        @Override
        public abstract Icon getIcon();
    }

    public static interface State {
        public String getAllOtherValuesDescription();

        public ColorScheme filterColorScheme(ColorScheme var1);

        public void activate();

        public void deactivate();

        public Collection getColorSchemeNames();

        public void applyColorScheme(ColorScheme var1);

        public Collection filterAttributeValues(SortedSet var1);

        public String getAttributeValueColumnTitle();

        public JComponent getPanel();

        public Map toExternalFormat(Map var1);

        public Map fromExternalFormat(Map var1);
    }
}

