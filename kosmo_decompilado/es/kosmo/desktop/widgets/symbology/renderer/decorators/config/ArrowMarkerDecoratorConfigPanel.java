/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.renderer.decorators.config;

import com.vividsolutions.jump.workbench.ui.ColorChooserPanel;
import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.core.renderer.decorators.impl.ArrowMarkerDecorator;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.AbstractDecoratorConfigPanel;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.NumberSpinner;

public class ArrowMarkerDecoratorConfigPanel
extends AbstractDecoratorConfigPanel {
    private static final long serialVersionUID = 1L;
    private JLabel rotationLabel;
    private JRadioButton fixedRotationAngleRadioButton;
    private NumberSpinner fixedRotationNumberSpinner;
    private JRadioButton followLineRotationAngleRadioButton;
    private JLabel spaceLabel;
    private JLabel sizeLabel;
    private NumberSpinner sizeNumberSpinner;
    private JLabel angleLabel;
    private NumberSpinner angleNumberSpinner;
    private JLabel unitsLabel;
    private JComboBox unitsCbb;
    private ColorChooserPanel colorChooserPanel;

    public ArrowMarkerDecoratorConfigPanel() {
        this.setLayout(new GridBagLayout());
        this.rotationLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ArrowMarkerDecoratorConfigPanel.Rotation")) + ": ");
        this.followLineRotationAngleRadioButton = new JRadioButton(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ArrowMarkerDecoratorConfigPanel.Follow-the-line"));
        this.fixedRotationAngleRadioButton = new JRadioButton(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ArrowMarkerDecoratorConfigPanel.Fixed-angle"));
        this.fixedRotationNumberSpinner = new NumberSpinner(0.0, -2.147483648E9, 2.147483647E9, 0.1);
        this.spaceLabel = new JLabel(" ");
        ButtonGroup group = new ButtonGroup();
        group.add(this.fixedRotationAngleRadioButton);
        group.add(this.followLineRotationAngleRadioButton);
        this.followLineRotationAngleRadioButton.setSelected(true);
        this.sizeLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ArrowMarkerDecoratorConfigPanel.Size")) + ": ");
        this.sizeNumberSpinner = new NumberSpinner(5.0, 0.1, 2.147483647E9, 0.1);
        this.angleLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ArrowMarkerDecoratorConfigPanel.Angle")) + " (\u00ba): ");
        this.angleNumberSpinner = new NumberSpinner(45.0, 5.0, 2.147483647E9, 0.1);
        this.unitsLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ArrowMarkerDecoratorConfigPanel.Units")) + ": ");
        this.unitsCbb = new JComboBox<String>(Symbolizer.UOM_ALLOWED);
        JLabel colorLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ArrowMarkerDecoratorConfigPanel.Color")) + ": ");
        Dimension dimension = new Dimension(50, 30);
        this.colorChooserPanel = new ColorChooserPanel();
        this.colorChooserPanel.setMinimumSize(dimension);
        this.colorChooserPanel.setPreferredSize(dimension);
        this.colorChooserPanel.setMaximumSize(dimension);
        this.colorChooserPanel.setAlpha(255);
        int row = 0;
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.sizeLabel, (JComponent)this.sizeNumberSpinner, true);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.unitsLabel, (JComponent)this.unitsCbb, true);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.angleLabel, (JComponent)this.angleNumberSpinner, true);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.rotationLabel, (JComponent)this.followLineRotationAngleRadioButton, true);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.spaceLabel, (JComponent)this.fixedRotationAngleRadioButton, false);
        FormUtils.addRowInGBL(this, row++, 30, this.fixedRotationNumberSpinner);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, colorLabel, (JComponent)this.colorChooserPanel, true);
        FormUtils.addFiller(this, row++, 0);
    }

    @Override
    public void setDecorator(IDecorator decorator) {
        if (decorator != null) {
            this.sizeNumberSpinner.setDefaultValue(decorator.getSize());
            this.colorChooserPanel.setColor(decorator.getColor());
            this.unitsCbb.setSelectedItem(decorator.getUnit());
        }
        ArrowMarkerDecorator selectedDecorator = (ArrowMarkerDecorator)decorator;
        this.angleNumberSpinner.setValue(selectedDecorator.getSharpness());
        if (selectedDecorator.isFixedRotation()) {
            this.fixedRotationAngleRadioButton.setSelected(true);
            this.fixedRotationNumberSpinner.setDefaultValue(selectedDecorator.getRotation());
        } else {
            this.followLineRotationAngleRadioButton.setSelected(true);
        }
    }

    @Override
    public IDecorator getDecorator(IDecorator baseDecorator) {
        baseDecorator.setColor(this.colorChooserPanel.getColor());
        baseDecorator.setSize(this.sizeNumberSpinner.getDoubleValue());
        baseDecorator.setUnit((String)this.unitsCbb.getSelectedItem());
        if (this.fixedRotationAngleRadioButton.isSelected()) {
            baseDecorator.setFixedRotation(true);
            baseDecorator.setRotation(this.fixedRotationNumberSpinner.getDoubleValue());
        } else {
            baseDecorator.setFixedRotation(false);
        }
        if (baseDecorator instanceof ArrowMarkerDecorator) {
            ArrowMarkerDecorator arrowMarkerDecorator = (ArrowMarkerDecorator)baseDecorator;
            arrowMarkerDecorator.setSharpness(this.angleNumberSpinner.getDoubleValue());
        }
        return baseDecorator;
    }
}

