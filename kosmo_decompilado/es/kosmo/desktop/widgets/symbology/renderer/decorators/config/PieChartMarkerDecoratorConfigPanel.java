/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.renderer.decorators.config;

import es.kosmo.core.renderer.decorators.impl.ChartMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.PieChartMarkerDecorator;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.AttributeRow;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ChartMarkerDecoratorConfigPanel;
import java.awt.Color;
import javax.swing.DefaultListModel;
import org.saig.core.model.feature.Attribute;

public class PieChartMarkerDecoratorConfigPanel
extends ChartMarkerDecoratorConfigPanel {
    private static final long serialVersionUID = 1L;

    @Override
    protected void fillChartProperties(ChartMarkerDecorator decorator) {
        PieChartMarkerDecorator baseDecorator = (PieChartMarkerDecorator)decorator;
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
        baseDecorator.setKeys(rowKeys);
        baseDecorator.setColors(colors);
    }

    @Override
    protected void addCustomComponents(int i) {
    }

    @Override
    protected void setChartProperties(ChartMarkerDecorator chartDecorator) {
        PieChartMarkerDecorator pieChartDecorator = (PieChartMarkerDecorator)chartDecorator;
        String[] rowKeys = pieChartDecorator.getKeys();
        Color[] colors = pieChartDecorator.getColors();
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

