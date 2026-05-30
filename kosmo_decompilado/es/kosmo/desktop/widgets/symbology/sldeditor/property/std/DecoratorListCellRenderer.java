/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.sldeditor.property.std;

import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.core.renderer.decorators.impl.ArrowMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.VertexMarkerDecorator;
import java.awt.Component;
import java.awt.Font;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import org.saig.jump.lang.I18N;

public class DecoratorListCellRenderer
extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel)super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
        IDecorator decorator = (IDecorator)value;
        boolean isArrowDecorator = decorator instanceof ArrowMarkerDecorator;
        boolean isTextDecorator = decorator instanceof VertexMarkerDecorator;
        String labelText = String.valueOf(decorator.getName()) + " - ";
        String sizeText = String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DecoratorListCellRenderer.Size")) + " " + decorator.getSize();
        String unit = decorator.getUnit();
        if (unit != null) {
            sizeText = String.valueOf(sizeText) + " " + unit;
        }
        if (isArrowDecorator) {
            String angleText = String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DecoratorListCellRenderer.Angle")) + " " + ((ArrowMarkerDecorator)decorator).getSharpness();
            String rotationText = String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DecoratorListCellRenderer.Rotation")) + " " + (decorator.isFixedRotation() ? String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DecoratorListCellRenderer.Fixed")) + " (" + decorator.getRotation() + ")" : I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DecoratorListCellRenderer.Follow-line"));
            labelText = String.valueOf(labelText) + sizeText + " - " + rotationText + " - " + angleText;
        } else if (isTextDecorator) {
            Font decoratorFont = ((VertexMarkerDecorator)decorator).getFont();
            labelText = String.valueOf(labelText) + I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DecoratorListCellRenderer.Font") + " " + decoratorFont.getFontName() + " - " + I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DecoratorListCellRenderer.Size") + " " + decoratorFont.getSize();
            if (unit != null) {
                labelText = String.valueOf(labelText) + " " + decorator.getUnit();
            }
        } else {
            labelText = String.valueOf(labelText) + sizeText;
        }
        label.setText(labelText);
        label.setIcon(decorator.getIcon());
        return label;
    }
}

