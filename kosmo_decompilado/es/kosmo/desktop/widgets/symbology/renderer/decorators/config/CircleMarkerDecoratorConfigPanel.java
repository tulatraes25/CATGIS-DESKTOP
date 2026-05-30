/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.renderer.decorators.config;

import com.vividsolutions.jump.workbench.ui.ColorChooserPanel;
import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.core.renderer.decorators.impl.CircleMarkerDecorator;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.AbstractDecoratorConfigPanel;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.NumberSpinner;

public class CircleMarkerDecoratorConfigPanel
extends AbstractDecoratorConfigPanel {
    private static final long serialVersionUID = 1L;
    protected JLabel sizeLabel;
    protected NumberSpinner sizeNumberSpinner;
    protected ColorChooserPanel colorChooserPanel;
    protected JLabel borderSizeLabel;
    protected NumberSpinner borderSizeNumberSpinner;
    protected JLabel fillLabel;
    protected JCheckBox fillCheckBox;
    private JLabel unitsLabel;
    private JComboBox unitsCbb;

    public CircleMarkerDecoratorConfigPanel() {
        this.setLayout(new GridBagLayout());
        this.sizeLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.CircleMarkerDecoratorConfigPanel.Size")) + ": ");
        this.sizeNumberSpinner = new NumberSpinner(5.0, 0.1, 2.147483647E9, 0.1);
        JLabel colorLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.CircleMarkerDecoratorConfigPanel.Color")) + ": ");
        Dimension dimension = new Dimension(50, 30);
        this.colorChooserPanel = new ColorChooserPanel();
        this.colorChooserPanel.setMinimumSize(dimension);
        this.colorChooserPanel.setPreferredSize(dimension);
        this.colorChooserPanel.setMaximumSize(dimension);
        this.colorChooserPanel.setAlpha(255);
        this.borderSizeLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.CircleMarkerDecoratorConfigPanel.Border-size")) + ": ");
        this.borderSizeNumberSpinner = new NumberSpinner(2.0, 0.1, 2.147483647E9, 0.1);
        this.fillLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.CircleMarkerDecoratorConfigPanel.Fill")) + ":");
        this.fillCheckBox = new JCheckBox();
        this.unitsLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ArrowMarkerDecoratorConfigPanel.Units")) + ": ");
        this.unitsCbb = new JComboBox<String>(Symbolizer.UOM_ALLOWED);
        FormUtils.addRowInGBL((JComponent)this, 0, 0, this.sizeLabel, (JComponent)this.sizeNumberSpinner);
        FormUtils.addRowInGBL((JComponent)this, 1, 0, this.unitsLabel, (JComponent)this.unitsCbb);
        FormUtils.addRowInGBL((JComponent)this, 2, 0, colorLabel, (JComponent)this.colorChooserPanel);
        FormUtils.addRowInGBL((JComponent)this, 3, 0, this.borderSizeLabel, (JComponent)this.borderSizeNumberSpinner);
        FormUtils.addRowInGBL((JComponent)this, 4, 0, this.fillLabel, (JComponent)this.fillCheckBox);
        FormUtils.addFiller(this, 5, 0);
    }

    @Override
    public void setDecorator(IDecorator decorator) {
        if (decorator != null) {
            this.sizeNumberSpinner.setDefaultValue(decorator.getSize());
            this.colorChooserPanel.setColor(decorator.getColor());
            this.unitsCbb.setSelectedItem(decorator.getUnit());
            if (decorator instanceof CircleMarkerDecorator) {
                CircleMarkerDecorator circleMarkerDecorator = (CircleMarkerDecorator)decorator;
                this.fillCheckBox.setSelected(circleMarkerDecorator.isFilled());
                this.borderSizeNumberSpinner.setDefaultValue(circleMarkerDecorator.getBorderSize());
            }
        }
    }

    @Override
    public IDecorator getDecorator(IDecorator baseDecorator) {
        CircleMarkerDecorator circleDecorator = (CircleMarkerDecorator)baseDecorator;
        circleDecorator.setColor(this.colorChooserPanel.getColor());
        circleDecorator.setUnit((String)this.unitsCbb.getSelectedItem());
        circleDecorator.setSize(this.sizeNumberSpinner.getDoubleValue());
        circleDecorator.setFilled(this.fillCheckBox.isSelected());
        circleDecorator.setBorderSize(this.borderSizeNumberSpinner.getDoubleValue());
        return circleDecorator;
    }
}

