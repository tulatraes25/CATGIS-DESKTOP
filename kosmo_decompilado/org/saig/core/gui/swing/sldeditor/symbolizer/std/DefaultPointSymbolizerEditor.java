/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.symbolizer.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.DecoratorChooser;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.gui.swing.sldeditor.property.GeometryChooser;
import org.saig.core.gui.swing.sldeditor.property.GraphicEditor;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;

public class DefaultPointSymbolizerEditor
extends SymbolizerEditor
implements SLDEditor {
    private static final long serialVersionUID = 1L;
    private PointSymbolizer symbolizer;
    private JLabel lblGeometry;
    private GeometryChooser geomChooser;
    private GraphicEditor graphicEditor;
    private DecoratorChooser decoratorChooser;

    public DefaultPointSymbolizerEditor(FeatureSchema schema) {
        this(schema, null);
    }

    public DefaultPointSymbolizerEditor(FeatureSchema fs, PointSymbolizer symbolizer) {
        this.setLayout(new GridBagLayout());
        if (symbolizer == null) {
            symbolizer = styleBuilder.createPointSymbolizer();
        }
        JTabbedPane tabPane = new JTabbedPane();
        JPanel basicPanel = new JPanel(new GridBagLayout());
        JPanel advancedPanel = new JPanel(new GridBagLayout());
        JPanel geomChooserPanel = new JPanel(new GridBagLayout());
        this.lblGeometry = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultPointSymbolizerEditor.geometry-property"));
        this.geomChooser = propertyEditorFactory.createGeometryChooser(fs);
        this.geomChooser.setMinimumSize(new Dimension(300, 20));
        this.geomChooser.setPreferredSize(new Dimension(300, 20));
        this.graphicEditor = propertyEditorFactory.createGraphicEditor(fs);
        this.decoratorChooser = propertyEditorFactory.createDecoratorChooser(fs);
        if (this.geomChooser.getGeomPropertiesCount() >= 2) {
            FormUtils.addRowInGBL((JComponent)geomChooserPanel, 0, 0, (JComponent)FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultPointSymbolizerEditor.geometry-property")), true, true);
            FormUtils.addRowInGBL((JComponent)geomChooserPanel, 1, 0, this.lblGeometry, (JComponent)this.geomChooser, true);
            FormUtils.addRowInGBL((JComponent)basicPanel, 1, 0, (JComponent)geomChooserPanel, true, true);
        }
        FormUtils.addRowInGBL((JComponent)basicPanel, 2, 0, (JComponent)this.graphicEditor, true, false);
        FormUtils.addFiller(basicPanel, 3, 0);
        FormUtils.addRowInGBL((JComponent)advancedPanel, 0, 0, (JComponent)this.decoratorChooser, true, true);
        FormUtils.addFiller(advancedPanel, 1, 0);
        tabPane.add(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicEditor.basic"), basicPanel);
        tabPane.add(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultPointSymbolizerEditor.Advanced"), advancedPanel);
        FormUtils.addRowInGBL(this, 0, 0, tabPane);
        FormUtils.addFiller(this, 1, 0);
        this.setSymbolizer(symbolizer);
    }

    @Override
    public void setSymbolizer(Symbolizer symbolizer) {
        if (!(symbolizer instanceof PointSymbolizer)) {
            throw new IllegalArgumentException(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultPointSymbolizerEditor.cannot-set-symbolizer-other-than-a-point-symbolizer"));
        }
        this.symbolizer = (PointSymbolizer)symbolizer;
        this.geomChooser.setSelectedName(this.symbolizer.getGeometryPropertyName());
        this.graphicEditor.setGraphic(this.symbolizer.getGraphic());
        this.graphicEditor.setUnitsOfMeasurement(this.symbolizer.getUnitsOfMeasurement());
        this.decoratorChooser.setDecorators(this.symbolizer.getDecorators());
    }

    @Override
    public Symbolizer getSymbolizer() {
        if (this.geomChooser.isVisible()) {
            this.symbolizer.setGeometryPropertyName(this.geomChooser.getSelectedName());
        } else {
            this.symbolizer.setGeometryPropertyName(null);
        }
        this.symbolizer.setGraphic(this.graphicEditor.getGraphic());
        this.symbolizer.setUnitsOfMeasurement(this.graphicEditor.getUnitsOfMeasurement());
        this.symbolizer.setDecorators(this.decoratorChooser.getDecorators());
        return this.symbolizer;
    }
}

