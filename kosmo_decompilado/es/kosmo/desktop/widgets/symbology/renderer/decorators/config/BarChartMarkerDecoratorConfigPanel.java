/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.renderer.decorators.config;

import es.kosmo.core.renderer.decorators.impl.BarChartMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.ChartMarkerDecorator;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.AttributeRow;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ChartMarkerDecoratorConfigPanel;
import java.awt.Color;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.feature.Attribute;
import org.saig.jump.lang.I18N;

public class BarChartMarkerDecoratorConfigPanel
extends ChartMarkerDecoratorConfigPanel {
    private static final long serialVersionUID = 1L;
    protected JCheckBox verticalCheckBox;

    @Override
    protected void fillChartProperties(ChartMarkerDecorator decorator) {
        BarChartMarkerDecorator baseDecorator = (BarChartMarkerDecorator)decorator;
        int rowNumber = this.table.getModel().getRowCount();
        String[] rowKeys = new String[rowNumber];
        String[] columnKeys = new String[rowNumber];
        Color[] colors = new Color[rowNumber];
        int i = 0;
        while (i < rowNumber) {
            AttributeRow rowAttr = this.table.getModel().getAttributeRow(i);
            rowKeys[i] = rowAttr.getFieldName();
            columnKeys[i] = rowAttr.getLabel();
            colors[i] = rowAttr.getColor();
            ++i;
        }
        baseDecorator.setRowKeys(rowKeys);
        baseDecorator.setColumnKeys(columnKeys);
        baseDecorator.setColors(colors);
        baseDecorator.setVertical(this.verticalCheckBox.isSelected());
    }

    @Override
    protected void addCustomComponents(int i) {
        JLabel verticalLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.BarChartMarkerDecoratorConfigPanel.Vertical")) + ":");
        this.verticalCheckBox = new JCheckBox();
        FormUtils.addRowInGBL((JComponent)this, i, 0, verticalLabel, (JComponent)this.verticalCheckBox);
    }

    @Override
    protected void setChartProperties(ChartMarkerDecorator chartDecorator) {
        BarChartMarkerDecorator barChartDecorator = (BarChartMarkerDecorator)chartDecorator;
        this.verticalCheckBox.setSelected(barChartDecorator.isVertical());
        String[] rowKeys = barChartDecorator.getRowKeys();
        String[] columnKeys = barChartDecorator.getColumnKeys();
        Color[] colors = barChartDecorator.getColors();
        if (rowKeys != null) {
            DefaultListModel model = (DefaultListModel)this.attributeSelectionJList.getModel();
            int i = 0;
            while (i < rowKeys.length) {
                String attrName = rowKeys[i];
                boolean found = false;
                int j = 0;
                while (j < model.getSize() && !found) {
                    Attribute attr = (Attribute)model.get(j);
                    if (attr.getName().equals(attrName)) {
                        found = true;
                        AttributeRow rowAttr = new AttributeRow(attr, colors[i]);
                        rowAttr.setLabel(columnKeys[i]);
                        this.table.getModel().addAttributeRow(rowAttr);
                    }
                    if (found) {
                        model.remove(j);
                    }
                    ++j;
                }
                ++i;
            }
        }
    }
}

