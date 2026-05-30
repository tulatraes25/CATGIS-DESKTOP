/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.sldeditor.property.std;

import es.kosmo.desktop.widgets.symbology.sldeditor.property.GraphicResizeEditor;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.TextSymbolizer;
import org.saig.jump.lang.I18N;

public class DefaultGraphicResizeEditor
extends GraphicResizeEditor {
    private static final long serialVersionUID = 1L;
    private JLabel resizeGraphicLabel;
    private JRadioButton noneRadioButton;
    private JRadioButton stretchRadioButton;
    private JRadioButton proportionalRadioButton;

    public DefaultGraphicResizeEditor() {
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createEmptyBorder());
        this.resizeGraphicLabel = new JLabel(I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DefaultGraphicResizeEditor.Resizing-options"));
        ButtonGroup optionGroup = new ButtonGroup();
        this.noneRadioButton = new JRadioButton(I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DefaultGraphicResizeEditor.None"));
        this.stretchRadioButton = new JRadioButton(I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DefaultGraphicResizeEditor.Stretch"));
        this.proportionalRadioButton = new JRadioButton(I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DefaultGraphicResizeEditor.Proportional"));
        optionGroup.add(this.noneRadioButton);
        optionGroup.add(this.stretchRadioButton);
        optionGroup.add(this.proportionalRadioButton);
        FormUtils.addRowInGBL(this, 0, 0, this.resizeGraphicLabel);
        FormUtils.addRowInGBL(this, 1, 0, this.noneRadioButton);
        FormUtils.addRowInGBL(this, 2, 0, this.stretchRadioButton);
        FormUtils.addRowInGBL(this, 3, 0, this.proportionalRadioButton);
        this.setGraphicResize(TextSymbolizer.GraphicResize.NONE);
    }

    @Override
    public TextSymbolizer.GraphicResize getGraphicResize() {
        TextSymbolizer.GraphicResize selectedOption = this.noneRadioButton.isSelected() ? TextSymbolizer.GraphicResize.NONE : (this.stretchRadioButton.isSelected() ? TextSymbolizer.GraphicResize.STRETCH : (this.proportionalRadioButton.isSelected() ? TextSymbolizer.GraphicResize.PROPORTIONAL : TextSymbolizer.GraphicResize.NONE));
        return selectedOption;
    }

    @Override
    public void setGraphicResize(TextSymbolizer.GraphicResize gResize) {
        switch (gResize) {
            case STRETCH: {
                this.stretchRadioButton.setSelected(true);
                break;
            }
            case PROPORTIONAL: {
                this.proportionalRadioButton.setSelected(true);
                break;
            }
            case NONE: {
                this.noneRadioButton.setSelected(true);
                break;
            }
            default: {
                this.noneRadioButton.setSelected(true);
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.resizeGraphicLabel.setEnabled(enabled);
        this.noneRadioButton.setEnabled(enabled);
        this.proportionalRadioButton.setEnabled(enabled);
        this.stretchRadioButton.setEnabled(enabled);
    }
}

