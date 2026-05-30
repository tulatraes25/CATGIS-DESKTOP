/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.l2fprod.common.swing.JFontChooser
 */
package es.kosmo.desktop.widgets.symbology.renderer.decorators.config;

import com.l2fprod.common.swing.JFontChooser;
import com.vividsolutions.jump.workbench.ui.ColorChooserPanel;
import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.core.renderer.decorators.impl.VertexMarkerDecorator;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.AbstractDecoratorConfigPanel;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;

public class VertexMarkerDecoratorConfigPanel
extends AbstractDecoratorConfigPanel {
    private static final long serialVersionUID = 1L;
    private ColorChooserPanel colorChooserPanel;
    private JLabel fontLabel;
    private JFontChooser fontChooser;
    private JLabel unitsLabel;
    private JComboBox unitsCbb;

    public VertexMarkerDecoratorConfigPanel() {
        this.setLayout(new GridBagLayout());
        JLabel colorLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.VertexMarkerDecoratorConfigPanel.Color")) + ": ");
        Dimension dimension = new Dimension(50, 30);
        this.colorChooserPanel = new ColorChooserPanel();
        this.colorChooserPanel.setMinimumSize(dimension);
        this.colorChooserPanel.setPreferredSize(dimension);
        this.colorChooserPanel.setMaximumSize(dimension);
        this.colorChooserPanel.setAlpha(255);
        this.fontLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.VertexMarkerDecoratorConfigPanel.Font")) + ": ");
        this.fontChooser = new JFontChooser();
        this.unitsLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ArrowMarkerDecoratorConfigPanel.Units")) + ": ");
        this.unitsCbb = new JComboBox<String>(Symbolizer.UOM_ALLOWED);
        FormUtils.addRowInGBL((JComponent)this, 0, 0, colorLabel, (JComponent)this.colorChooserPanel);
        FormUtils.addRowInGBL((JComponent)this, 1, 0, this.unitsLabel, (JComponent)this.unitsCbb);
        FormUtils.addRowInGBL((JComponent)this, 2, 0, this.fontLabel, (JComponent)this.fontChooser);
        FormUtils.addFiller(this, 3, 0);
    }

    @Override
    public void setDecorator(IDecorator decorator) {
        if (decorator != null) {
            this.colorChooserPanel.setColor(decorator.getColor());
            this.fontChooser.setSelectedFont(((VertexMarkerDecorator)decorator).getFont());
            this.unitsCbb.setSelectedItem(decorator.getUnit());
        }
    }

    @Override
    public IDecorator getDecorator(IDecorator baseDecorator) {
        baseDecorator.setColor(this.colorChooserPanel.getColor());
        baseDecorator.setUnit((String)this.unitsCbb.getSelectedItem());
        if (baseDecorator instanceof VertexMarkerDecorator) {
            VertexMarkerDecorator vertexMarkerDecorator = (VertexMarkerDecorator)baseDecorator;
            vertexMarkerDecorator.setFont(this.fontChooser.getSelectedFont());
        }
        return baseDecorator;
    }
}

