/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.sldeditor.property.std;

import es.kosmo.desktop.widgets.symbology.sldeditor.property.LineToLabelEndingAnchorTypeEditor;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.TextSymbolizer;
import org.saig.jump.lang.I18N;

public class DefaultLineToLabelEndingAnchorTypeEditor
extends LineToLabelEndingAnchorTypeEditor {
    private static final long serialVersionUID = 1L;
    private JLabel labelLineEndingAnchorTypeLabel;
    private JRadioButton centroidRadioButton;
    private JRadioButton centroidInsideRadioButton;
    private JRadioButton closestPointRadioButton;

    public DefaultLineToLabelEndingAnchorTypeEditor() {
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createEmptyBorder());
        this.labelLineEndingAnchorTypeLabel = new JLabel(I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DefaultLineToLabelEndingAnchorTypeEditor.Geometry-anchor-point"));
        ButtonGroup optionGroup = new ButtonGroup();
        this.centroidRadioButton = new JRadioButton(I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DefaultLineToLabelEndingAnchorTypeEditor.Centroid"));
        this.centroidInsideRadioButton = new JRadioButton(I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DefaultLineToLabelEndingAnchorTypeEditor.Centroid-or-interior-point"));
        this.closestPointRadioButton = new JRadioButton(I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DefaultLineToLabelEndingAnchorTypeEditor.Nearest-point"));
        optionGroup.add(this.closestPointRadioButton);
        optionGroup.add(this.centroidRadioButton);
        optionGroup.add(this.centroidInsideRadioButton);
        FormUtils.addRowInGBL(this, 0, 0, this.labelLineEndingAnchorTypeLabel);
        FormUtils.addRowInGBL(this, 1, 0, this.closestPointRadioButton);
        FormUtils.addRowInGBL(this, 2, 0, this.centroidRadioButton);
        FormUtils.addRowInGBL(this, 3, 0, this.centroidInsideRadioButton);
        this.setLineToLabelEndingAnchorType(TextSymbolizer.DEFAULT_LINE_TO_LABEL_ENDING_ANCHOR);
    }

    @Override
    public void setLineToLabelEndingAnchorType(TextSymbolizer.LineToLabelEndingAnchorOptions type) {
        switch (type) {
            case CENTROID: {
                this.centroidRadioButton.setSelected(true);
                break;
            }
            case CENTROID_INSIDE: {
                this.centroidRadioButton.setSelected(true);
                break;
            }
            case CLOSEST_POINT: {
                this.closestPointRadioButton.setSelected(true);
                break;
            }
            default: {
                this.closestPointRadioButton.setSelected(true);
            }
        }
    }

    @Override
    public TextSymbolizer.LineToLabelEndingAnchorOptions getLineToLabelEndingAnchorType() {
        TextSymbolizer.LineToLabelEndingAnchorOptions option = this.centroidRadioButton.isSelected() ? TextSymbolizer.LineToLabelEndingAnchorOptions.CENTROID : (this.centroidInsideRadioButton.isSelected() ? TextSymbolizer.LineToLabelEndingAnchorOptions.CENTROID_INSIDE : (this.closestPointRadioButton.isSelected() ? TextSymbolizer.LineToLabelEndingAnchorOptions.CLOSEST_POINT : TextSymbolizer.DEFAULT_LINE_TO_LABEL_ENDING_ANCHOR));
        return option;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.labelLineEndingAnchorTypeLabel.setEnabled(enabled);
        this.centroidRadioButton.setEnabled(enabled);
        this.centroidInsideRadioButton.setEnabled(enabled);
        this.closestPointRadioButton.setEnabled(enabled);
    }
}

