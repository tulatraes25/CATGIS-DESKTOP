/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.renderer.decorators.config;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.core.renderer.decorators.impl.ChartMarkerDecorator;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.AbstractDecoratorConfigPanel;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.AttributeRow;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ChartAttributeConfigurationTable;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ChartAttributeSelectionTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.feature.Attribute;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.NumberSpinner;

public abstract class ChartMarkerDecoratorConfigPanel
extends AbstractDecoratorConfigPanel
implements ListSelectionListener,
ActionListener {
    private static final long serialVersionUID = 1L;
    protected JLabel sizeLabel;
    protected NumberSpinner sizeNumberSpinner;
    private JLabel unitsLabel;
    private JComboBox unitsCbb;
    protected JCheckBox is3dCheckBox;
    protected JCheckBox drawOutlineCheckBox;
    protected JList attributeSelectionJList;
    protected JButton toRightButton;
    protected JButton toLeftButton;
    protected JButton allRightButton;
    protected JButton allLeftButton;
    protected ChartAttributeConfigurationTable table;
    protected ChartAttributeSelectionTableModel tableModel;

    public ChartMarkerDecoratorConfigPanel() {
        this.setLayout(new GridBagLayout());
        this.sizeLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ChartMarkerDecoratorConfigPanel.Size")) + ": ");
        this.sizeNumberSpinner = new NumberSpinner(5.0, 0.1, 2.147483647E9, 0.1);
        this.unitsLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ArrowMarkerDecoratorConfigPanel.Units")) + ": ");
        this.unitsCbb = new JComboBox<String>(Symbolizer.UOM_ALLOWED);
        JLabel is3dLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ChartMarkerDecoratorConfigPanel.3D")) + ":");
        this.is3dCheckBox = new JCheckBox();
        JLabel drawOutlineLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ChartMarkerDecoratorConfigPanel.Border")) + ":");
        this.drawOutlineCheckBox = new JCheckBox();
        JPanel attributeSelectionPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(buttonPanel, 1);
        buttonPanel.setLayout(boxLayout);
        this.toRightButton = new JButton(">");
        this.toRightButton.addActionListener(this);
        this.toLeftButton = new JButton("<");
        this.toLeftButton.addActionListener(this);
        this.allRightButton = new JButton(">>");
        this.allRightButton.addActionListener(this);
        this.allLeftButton = new JButton("<<");
        this.allLeftButton.addActionListener(this);
        buttonPanel.add(this.toRightButton);
        buttonPanel.add(this.toLeftButton);
        buttonPanel.add(this.allRightButton);
        buttonPanel.add(this.allLeftButton);
        this.tableModel = new ChartAttributeSelectionTableModel();
        this.table = new ChartAttributeConfigurationTable(this.tableModel);
        this.table.getSelectionModel().addListSelectionListener(this);
        JScrollPane tableScrollPane = new JScrollPane(this.table);
        this.table.setFillsViewportHeight(true);
        Dimension dim = new Dimension(300, 200);
        tableScrollPane.setPreferredSize(dim);
        tableScrollPane.setMinimumSize(dim);
        this.attributeSelectionJList = new JList(new DefaultListModel());
        this.attributeSelectionJList.addListSelectionListener(this);
        JScrollPane pane = new JScrollPane(this.attributeSelectionJList);
        pane.setBorder(BorderFactory.createTitledBorder(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ChartMarkerDecoratorConfigPanel.Fields")));
        attributeSelectionPanel.add((Component)pane, "West");
        attributeSelectionPanel.add((Component)buttonPanel, "Center");
        attributeSelectionPanel.add((Component)tableScrollPane, "East");
        int cont = 0;
        FormUtils.addRowInGBL((JComponent)this, cont++, 0, this.sizeLabel, (JComponent)this.sizeNumberSpinner);
        FormUtils.addRowInGBL((JComponent)this, cont++, 0, this.unitsLabel, (JComponent)this.unitsCbb);
        FormUtils.addRowInGBL((JComponent)this, cont++, 0, is3dLabel, (JComponent)this.is3dCheckBox);
        FormUtils.addRowInGBL((JComponent)this, cont++, 0, drawOutlineLabel, (JComponent)this.drawOutlineCheckBox);
        FormUtils.addRowInGBL(this, cont++, 0, attributeSelectionPanel);
        this.addCustomComponents(cont++);
        FormUtils.addFiller(this, 20, 0);
    }

    protected abstract void addCustomComponents(int var1);

    @Override
    public void setDecorator(IDecorator decorator) {
        if (decorator != null) {
            this.sizeNumberSpinner.setDefaultValue(decorator.getSize());
            this.unitsCbb.setSelectedItem(decorator.getUnit());
            ChartMarkerDecorator chartDecorator = (ChartMarkerDecorator)decorator;
            this.is3dCheckBox.setSelected(chartDecorator.is3d());
            this.drawOutlineCheckBox.setSelected(chartDecorator.isDrawOutline());
            this.setChartProperties(chartDecorator);
        }
        this.refresh();
    }

    protected abstract void setChartProperties(ChartMarkerDecorator var1);

    protected void refresh() {
        boolean atLeastOneSelectedInList = this.attributeSelectionJList.getSelectedIndices().length > 0;
        boolean atLeastOneAttributeInList = this.attributeSelectionJList.getModel().getSize() > 0;
        boolean atLeastOneSelectedInTable = this.table.getSelectedRows().length > 0;
        boolean atLeastOneAttributeInTable = this.table.getModel().getRowCount() > 0;
        this.toRightButton.setEnabled(atLeastOneSelectedInList);
        this.toLeftButton.setEnabled(atLeastOneSelectedInTable);
        this.allRightButton.setEnabled(atLeastOneAttributeInList);
        this.allLeftButton.setEnabled(atLeastOneAttributeInTable);
    }

    @Override
    public IDecorator getDecorator(IDecorator baseDecorator) {
        baseDecorator.setSize(this.sizeNumberSpinner.getDoubleValue());
        baseDecorator.setUnit((String)this.unitsCbb.getSelectedItem());
        ChartMarkerDecorator chartDecorator = (ChartMarkerDecorator)baseDecorator;
        chartDecorator.set3d(this.is3dCheckBox.isSelected());
        chartDecorator.setDrawOutline(this.drawOutlineCheckBox.isSelected());
        this.fillChartProperties(chartDecorator);
        return chartDecorator;
    }

    @Override
    public void setSchema(FeatureSchema layerSchema) {
        super.setSchema(layerSchema);
        ArrayList<Attribute> attrs = new ArrayList<Attribute>();
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            Attribute attr = this.schema.getAttribute(i);
            if (i != this.schema.getPrimaryKeyIndex() && AttributeType.isNumeric(attr.getType())) {
                attrs.add(attr);
            }
            ++i;
        }
        Collections.sort(attrs);
        DefaultListModel model = new DefaultListModel();
        int i2 = 0;
        while (i2 < attrs.size()) {
            model.add(i2, attrs.get(i2));
            ++i2;
        }
        this.attributeSelectionJList.setModel(model);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        this.refresh();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.toRightButton)) {
            int[] selectedIndexes = this.attributeSelectionJList.getSelectedIndices();
            DefaultListModel model = (DefaultListModel)this.attributeSelectionJList.getModel();
            int i = selectedIndexes.length - 1;
            while (i >= 0) {
                Object value = this.attributeSelectionJList.getModel().getElementAt(selectedIndexes[i]);
                Attribute attr = (Attribute)value;
                AttributeRow rowAttr = new AttributeRow(attr, Color.BLACK);
                this.table.getModel().addAttributeRow(rowAttr);
                model.remove(selectedIndexes[i]);
                --i;
            }
        } else if (e.getSource().equals(this.toLeftButton)) {
            int[] selectedRows = this.table.getSelectedRows();
            DefaultListModel model = (DefaultListModel)this.attributeSelectionJList.getModel();
            int i = selectedRows.length - 1;
            while (i >= 0) {
                AttributeRow rowAttr = this.table.getModel().removeAttributeRow(selectedRows[i]);
                model.addElement(rowAttr.getAttribute());
                --i;
            }
        } else if (e.getSource().equals(this.allRightButton)) {
            int i = 0;
            while (i < this.attributeSelectionJList.getModel().getSize()) {
                Attribute attr = (Attribute)this.attributeSelectionJList.getModel().getElementAt(i);
                AttributeRow rowAttr = new AttributeRow(attr, Color.BLACK);
                this.table.getModel().addAttributeRow(rowAttr);
                ++i;
            }
            ((DefaultListModel)this.attributeSelectionJList.getModel()).removeAllElements();
            this.refresh();
        } else if (e.getSource().equals(this.allLeftButton)) {
            DefaultListModel model = (DefaultListModel)this.attributeSelectionJList.getModel();
            int i = this.table.getModel().getRowCount() - 1;
            while (i >= 0) {
                AttributeRow rowAttr = this.table.getModel().removeAttributeRow(i);
                model.addElement(rowAttr.getAttribute());
                --i;
            }
            this.attributeSelectionJList.repaint();
            this.refresh();
        }
    }

    protected abstract void fillChartProperties(ChartMarkerDecorator var1);
}

