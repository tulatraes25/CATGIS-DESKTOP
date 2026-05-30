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
import org.saig.core.gui.swing.sldeditor.property.ExpressionEditor;
import org.saig.core.gui.swing.sldeditor.property.GeometryChooser;
import org.saig.core.gui.swing.sldeditor.property.StrokeEditor;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;

public class DefaultLineSymbolizerEditor
extends SymbolizerEditor
implements SLDEditor {
    private static final long serialVersionUID = 1L;
    private LineSymbolizer symbolizer;
    private JLabel lblGeometry;
    private GeometryChooser geomChooser;
    private StrokeEditor strokeEditor;
    private ExpressionEditor offsetEditor;
    private DecoratorChooser decoratorChooser;

    public DefaultLineSymbolizerEditor(FeatureSchema featureType) {
        this(featureType, null);
    }

    public DefaultLineSymbolizerEditor(FeatureSchema ft, LineSymbolizer symbolizer) {
        this.setLayout(new GridBagLayout());
        if (symbolizer == null) {
            symbolizer = styleBuilder.createLineSymbolizer();
        }
        JTabbedPane tabPane = new JTabbedPane();
        JPanel basicPanel = new JPanel(new GridBagLayout());
        JPanel advancedPanel = new JPanel(new GridBagLayout());
        JPanel geomChooserPanel = new JPanel(new GridBagLayout());
        this.lblGeometry = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultLineSymbolizerEditor.geometry-property"));
        this.geomChooser = propertyEditorFactory.createGeometryChooser(ft);
        this.strokeEditor = propertyEditorFactory.createStrokeEditor(ft);
        this.strokeEditor.allowDisable(false);
        this.offsetEditor = propertyEditorFactory.createDoubleEditor(ft);
        this.decoratorChooser = propertyEditorFactory.createDecoratorChooser(ft);
        int basicPanelRowCont = 0;
        if (this.geomChooser.getGeomPropertiesCount() >= 2) {
            FormUtils.addRowInGBL((JComponent)geomChooserPanel, 0, 0, this.lblGeometry, (JComponent)this.geomChooser, true);
            FormUtils.addRowInGBL((JComponent)basicPanel, basicPanelRowCont++, 0, (JComponent)FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultPointSymbolizerEditor.geometry-property")), false, true);
            FormUtils.addRowInGBL((JComponent)basicPanel, basicPanelRowCont++, 0, (JComponent)geomChooserPanel, false, false);
        }
        FormUtils.addRowInGBL(basicPanel, basicPanelRowCont++, 0, FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.general")));
        FormUtils.addRowInGBL(basicPanel, basicPanelRowCont++, 0, this.strokeEditor);
        FormUtils.addFiller(basicPanel, basicPanelRowCont++, 0);
        int advancedPanelRowCont = 0;
        FormUtils.addRowInGBL((JComponent)advancedPanel, advancedPanelRowCont++, 0, new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultLineSymbolizerEditor.Displacement")), (JComponent)this.offsetEditor, false);
        FormUtils.addRowInGBL((JComponent)advancedPanel, advancedPanelRowCont++, 0, (JComponent)this.decoratorChooser, true, true);
        FormUtils.addFiller(advancedPanel, advancedPanelRowCont++, 0);
        tabPane.add(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicEditor.basic"), basicPanel);
        tabPane.add(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultLineSymbolizerEditor.Advanced"), advancedPanel);
        FormUtils.addRowInGBL(this, 0, 0, tabPane);
        FormUtils.addFiller(this, 1, 0);
        this.setSymbolizer(symbolizer);
    }

    @Override
    public void setSymbolizer(Symbolizer symbolizer) {
        if (!(symbolizer instanceof LineSymbolizer)) {
            throw new IllegalArgumentException(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultLineSymbolizerEditor.cannot-set-symbolizer-other-than-a-line-symbolizer"));
        }
        this.symbolizer = (LineSymbolizer)symbolizer;
        this.geomChooser.setSelectedName(this.symbolizer.getGeometryPropertyName());
        this.strokeEditor.setStroke(this.symbolizer.getStroke());
        this.strokeEditor.setUnitsOfMeasurement(symbolizer.getUnitsOfMeasurement());
        this.offsetEditor.setExpression(this.symbolizer.getOffset());
        this.decoratorChooser.setDecorators(this.symbolizer.getDecorators());
    }

    @Override
    public Symbolizer getSymbolizer() {
        if (this.geomChooser.isVisible()) {
            this.symbolizer.setGeometryPropertyName(this.geomChooser.getSelectedName());
        } else {
            this.symbolizer.setGeometryPropertyName(null);
        }
        this.symbolizer.setStroke(this.strokeEditor.getStroke());
        this.symbolizer.setUnitsOfMeasurement(this.strokeEditor.getUnitsOfMeasurement());
        this.symbolizer.setOffset(this.offsetEditor.getExpression());
        this.symbolizer.setDecorators(this.decoratorChooser.getDecorators());
        return this.symbolizer;
    }
}

