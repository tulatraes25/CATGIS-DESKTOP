/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.symbolizer.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.DecoratorChooser;
import java.awt.GridBagLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.gui.swing.sldeditor.property.FillEditor;
import org.saig.core.gui.swing.sldeditor.property.GeometryChooser;
import org.saig.core.gui.swing.sldeditor.property.StrokeEditor;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;

public class DefaultPolygonSymbolizerEditor
extends SymbolizerEditor
implements SLDEditor {
    private static final long serialVersionUID = 1L;
    private PolygonSymbolizer symbolizer;
    private JLabel lblGeometry;
    private GeometryChooser geomChooser;
    private FillEditor fillEditor;
    private StrokeEditor strokeEditor;
    private DecoratorChooser decoratorChooser;

    public DefaultPolygonSymbolizerEditor(FeatureSchema ft) {
        this(ft, styleBuilder.createPolygonSymbolizer());
    }

    public DefaultPolygonSymbolizerEditor(FeatureSchema ft, PolygonSymbolizer symbolizer) {
        this.setLayout(new GridBagLayout());
        JTabbedPane tabPane = new JTabbedPane();
        JPanel basicPanel = new JPanel(new GridBagLayout());
        JPanel advancedPanel = new JPanel(new GridBagLayout());
        JPanel geomChooserPanel = new JPanel(new GridBagLayout());
        this.lblGeometry = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultPolygonSymbolizerEditor.geometry-property"));
        this.geomChooser = propertyEditorFactory.createGeometryChooser(ft);
        this.fillEditor = propertyEditorFactory.createFillEditor(ft);
        this.strokeEditor = propertyEditorFactory.createStrokeEditor(ft);
        this.decoratorChooser = propertyEditorFactory.createDecoratorChooser(ft);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultPolygonSymbolizerEditor.fill"), this.fillEditor);
        tabbedPane.add(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultPolygonSymbolizerEditor.stroke"), this.strokeEditor);
        if (this.geomChooser.getGeomPropertiesCount() >= 2) {
            FormUtils.addRowInGBL((JComponent)geomChooserPanel, 0, 0, (JComponent)FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultPointSymbolizerEditor.geometry-property")), true, true);
            FormUtils.addRowInGBL((JComponent)geomChooserPanel, 1, 0, this.lblGeometry, (JComponent)this.geomChooser, true);
            FormUtils.addRowInGBL((JComponent)basicPanel, 0, 0, (JComponent)geomChooserPanel, true, true);
        }
        FormUtils.addRowInGBL((JComponent)basicPanel, 2, 0, (JComponent)tabbedPane, true, true);
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
        if (!(symbolizer instanceof PolygonSymbolizer)) {
            throw new IllegalArgumentException(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultPolygonSymbolizerEditor.cannot-set-symbolizer-other-than-a-polygon-symbolizer"));
        }
        this.symbolizer = (PolygonSymbolizer)symbolizer;
        this.geomChooser.setSelectedName(this.symbolizer.getGeometryPropertyName());
        this.fillEditor.setFill(this.symbolizer.getFill());
        this.strokeEditor.setStroke(this.symbolizer.getStroke());
        this.strokeEditor.setUnitsOfMeasurement(this.symbolizer.getUnitsOfMeasurement());
        this.decoratorChooser.setDecorators(this.symbolizer.getDecorators());
    }

    @Override
    public Symbolizer getSymbolizer() {
        if (this.geomChooser.isVisible()) {
            this.symbolizer.setGeometryPropertyName(this.geomChooser.getSelectedName());
        } else {
            this.symbolizer.setGeometryPropertyName(null);
        }
        this.symbolizer.setFill(this.fillEditor.getFill());
        this.symbolizer.setStroke(this.strokeEditor.getStroke());
        this.symbolizer.setUnitsOfMeasurement(this.strokeEditor.getUnitsOfMeasurement());
        this.symbolizer.setDecorators(this.decoratorChooser.getDecorators());
        return this.symbolizer;
    }
}

