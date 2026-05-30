/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.DecoratorChooser;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.GradientFillEditor;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.GraphicResizeEditor;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.LineToLabelEndingAnchorTypeEditor;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DefaultDecoratorChooser;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DefaultGradientFillEditor;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DefaultGraphicResizeEditor;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DefaultLineToLabelEndingAnchorTypeEditor;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DefaultPercentEditor;
import org.saig.core.gui.swing.sldeditor.property.DashArrayEditor;
import org.saig.core.gui.swing.sldeditor.property.ExpressionEditor;
import org.saig.core.gui.swing.sldeditor.property.ExternalGraphicEditor;
import org.saig.core.gui.swing.sldeditor.property.FeatureTypeChooser;
import org.saig.core.gui.swing.sldeditor.property.FillEditor;
import org.saig.core.gui.swing.sldeditor.property.FilterEditor;
import org.saig.core.gui.swing.sldeditor.property.FixedOrByAttributeExpressionEditor;
import org.saig.core.gui.swing.sldeditor.property.FontListChooser;
import org.saig.core.gui.swing.sldeditor.property.GeometryChooser;
import org.saig.core.gui.swing.sldeditor.property.GraphicEditor;
import org.saig.core.gui.swing.sldeditor.property.LabelPlacementEditor;
import org.saig.core.gui.swing.sldeditor.property.MarkEditor;
import org.saig.core.gui.swing.sldeditor.property.PropertyEditorFactory;
import org.saig.core.gui.swing.sldeditor.property.ScaleEditor;
import org.saig.core.gui.swing.sldeditor.property.StrokeEditor;
import org.saig.core.gui.swing.sldeditor.property.SymbolEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultColorEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultCompactFillEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultDashArrayEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultExpressionEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultExternalGraphicEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultFeatureAttributeChooser;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultFeatureTypeChooser;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultFillEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultFilterEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultFixedOrByAttributeEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultFontListChooser;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultGeometryChooser;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicFillEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultLabelPlacementEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultMarkEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultNumberEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultScaleEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultSymbolEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultWKTFillPatternEditor;
import org.saig.core.gui.swing.sldeditor.property.std.SimpleDashArrayEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.FeatureTypeStyle;

public class DefaultPropertyEditorFactory
extends PropertyEditorFactory {
    @Override
    public ExpressionEditor createExpressionEditor(FeatureSchema featureType) {
        return new DefaultExpressionEditor(featureType);
    }

    @Override
    public FilterEditor createFilterEditor(FeatureSchema featureType) {
        return new DefaultFilterEditor(featureType);
    }

    @Override
    public ExpressionEditor createColorEditor(FeatureSchema featureType) {
        return new DefaultColorEditor();
    }

    @Override
    public DashArrayEditor createDashArrayEditor() {
        return new DefaultDashArrayEditor();
    }

    @Override
    public ExternalGraphicEditor createExternalGraphicEditor() {
        return new DefaultExternalGraphicEditor();
    }

    @Override
    public ExpressionEditor createFeatureAttributeChooser(FeatureSchema featureType) {
        return new DefaultFeatureAttributeChooser(featureType);
    }

    @Override
    public ExpressionEditor createFeatureAttributeChooser(FeatureSchema featureType, Class<?>[] filterClass) {
        return new DefaultFeatureAttributeChooser(null, featureType, filterClass);
    }

    @Override
    public FillEditor createFillEditor(FeatureSchema featureType) {
        return new DefaultFillEditor(featureType);
    }

    @Override
    public FillEditor createCompactFillEditor(FeatureSchema featureType) {
        return new DefaultCompactFillEditor(featureType);
    }

    @Override
    public FontListChooser createFontListChooser() {
        return new DefaultFontListChooser();
    }

    @Override
    public GeometryChooser createGeometryChooser(FeatureSchema featureType) {
        return new DefaultGeometryChooser(featureType);
    }

    @Override
    public GraphicEditor createGraphicEditor(FeatureSchema featureType) {
        return new DefaultGraphicEditor(featureType);
    }

    @Override
    public GraphicEditor createGraphicFillEditor(FeatureSchema featureType) {
        return new DefaultGraphicFillEditor(FormUtils.getColorButtonDimension(), true, featureType);
    }

    @Override
    public GraphicEditor createGraphicStrokeEditor(FeatureSchema featureType) {
        return new DefaultGraphicFillEditor(FormUtils.getColorButtonDimension(), false, featureType);
    }

    @Override
    public GraphicEditor createGraphicWKTFillPatternEditor(FeatureSchema featureType) {
        return new DefaultWKTFillPatternEditor(featureType);
    }

    @Override
    public LabelPlacementEditor createLabelPlacementEditor(FeatureSchema featureType) {
        return new DefaultLabelPlacementEditor(featureType);
    }

    @Override
    public MarkEditor createMarkEditor(FeatureSchema featureType) {
        return new DefaultMarkEditor(featureType);
    }

    @Override
    public FeatureTypeChooser createFeatureTypeChooser(FeatureTypeStyle[] featureTypeStyles) {
        return new DefaultFeatureTypeChooser(featureTypeStyles);
    }

    @Override
    public ExpressionEditor createNumberEditor(Number startValue, Number minValue, Number maxValue, Number step, FeatureSchema featureType) {
        return new DefaultNumberEditor(startValue, minValue, maxValue, step);
    }

    @Override
    public ExpressionEditor createOpacityEditor(FeatureSchema featureType) {
        return new DefaultPercentEditor(100.0);
    }

    @Override
    public ExpressionEditor createDoubleEditor(FeatureSchema featureType) {
        return new DefaultNumberEditor(new Double(0.0), new Double(Double.NEGATIVE_INFINITY), new Double(Double.MAX_VALUE), new Double(1.0));
    }

    @Override
    public ExpressionEditor createIntSizeEditor(FeatureSchema featureType) {
        return new DefaultNumberEditor(new Integer(1), new Integer(0), new Integer(Integer.MAX_VALUE), new Integer(1));
    }

    @Override
    public ExpressionEditor createDoubleSizeEditor(FeatureSchema featureType) {
        return new DefaultNumberEditor(new Double(0.0), new Double(0.0), new Double(Double.MAX_VALUE), new Double(0.1));
    }

    @Override
    public ExpressionEditor createRotationEditor(FeatureSchema featureType) {
        DefaultNumberEditor editor = new DefaultNumberEditor(new Integer(0), new Integer(0), new Integer(360), new Integer(1), 1.0);
        editor.setCyclic(true);
        return editor;
    }

    @Override
    public ScaleEditor createScaleEditor() {
        return new DefaultScaleEditor();
    }

    @Override
    public StrokeEditor createStrokeEditor(FeatureSchema featureType) {
        return new DefaultStrokeEditor(featureType);
    }

    @Override
    public SymbolEditor createSymbolEditor(FeatureSchema featureType) {
        return new DefaultSymbolEditor(featureType);
    }

    @Override
    public FixedOrByAttributeExpressionEditor createFixedOrByAttributeEditor(FeatureSchema featureType, boolean allowUOMSelection) {
        return new DefaultFixedOrByAttributeEditor(featureType, allowUOMSelection);
    }

    @Override
    public FixedOrByAttributeExpressionEditor createFixedOrByAttributeEditor(FeatureSchema featureType, Number value, Number minimum, Number maximum, Number step, boolean allowUOMSelection) {
        return new DefaultFixedOrByAttributeEditor(featureType, value, minimum, maximum, step, allowUOMSelection);
    }

    @Override
    public DashArrayEditor createSimpleDashArrayEditor() {
        return new SimpleDashArrayEditor();
    }

    @Override
    public DecoratorChooser createDecoratorChooser(FeatureSchema ft) {
        return new DefaultDecoratorChooser(ft);
    }

    @Override
    public LineToLabelEndingAnchorTypeEditor createLineToLabelEndingAnchorTypeEditor() {
        return new DefaultLineToLabelEndingAnchorTypeEditor();
    }

    @Override
    public GraphicResizeEditor createGraphicResizeEditor() {
        return new DefaultGraphicResizeEditor();
    }

    @Override
    public GradientFillEditor createGradientFillEditor() {
        return new DefaultGradientFillEditor(FormUtils.getColorButtonDimension());
    }
}

