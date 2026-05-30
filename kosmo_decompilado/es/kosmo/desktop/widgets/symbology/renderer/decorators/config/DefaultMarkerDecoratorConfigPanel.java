/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.renderer.decorators.config;

import com.vividsolutions.jump.workbench.ui.ColorChooserPanel;
import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.AbstractDecoratorConfigPanel;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.NumberSpinner;

public class DefaultMarkerDecoratorConfigPanel
extends AbstractDecoratorConfigPanel {
    private static final long serialVersionUID = 1L;
    private JLabel sizeLabel;
    private NumberSpinner sizeNumberSpinner;
    private JLabel unitsLabel;
    private JComboBox unitsCbb;
    private ColorChooserPanel colorChooserPanel;

    public DefaultMarkerDecoratorConfigPanel() {
        this.setLayout(new GridBagLayout());
        this.sizeLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.DefaultMarkerDecoratorConfigPanel.Size")) + ": ");
        this.sizeNumberSpinner = new NumberSpinner(5.0, 0.1, 2.147483647E9, 0.1);
        this.unitsLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ArrowMarkerDecoratorConfigPanel.Units")) + ": ");
        this.unitsCbb = new JComboBox<String>(Symbolizer.UOM_ALLOWED);
        JLabel colorLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.DefaultMarkerDecoratorConfigPanel.Color")) + ": ");
        Dimension dimension = new Dimension(50, 30);
        this.colorChooserPanel = new ColorChooserPanel();
        this.colorChooserPanel.setMinimumSize(dimension);
        this.colorChooserPanel.setPreferredSize(dimension);
        this.colorChooserPanel.setMaximumSize(dimension);
        this.colorChooserPanel.setAlpha(255);
        FormUtils.addRowInGBL((JComponent)this, 0, 0, this.sizeLabel, (JComponent)this.sizeNumberSpinner);
        FormUtils.addRowInGBL((JComponent)this, 1, 0, this.unitsLabel, (JComponent)this.unitsCbb);
        FormUtils.addRowInGBL((JComponent)this, 2, 0, colorLabel, (JComponent)this.colorChooserPanel);
        FormUtils.addFiller(this, 3, 0);
    }

    @Override
    public void setDecorator(IDecorator decorator) {
        if (decorator != null) {
            this.sizeNumberSpinner.setDefaultValue(decorator.getSize());
            this.unitsCbb.setSelectedItem(decorator.getUnit());
            this.colorChooserPanel.setColor(decorator.getColor());
        }
    }

    @Override
    public IDecorator getDecorator(IDecorator baseDecorator) {
        baseDecorator.setColor(this.colorChooserPanel.getColor());
        baseDecorator.setSize(this.sizeNumberSpinner.getDoubleValue());
        baseDecorator.setUnit((String)this.unitsCbb.getSelectedItem());
        return baseDecorator;
    }
}

