/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.factory.Factory
 */
package org.saig.core.gui.swing.sldeditor.property;

import com.vividsolutions.jump.feature.FeatureSchema;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.DecoratorChooser;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.GradientFillEditor;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.GraphicResizeEditor;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.LineToLabelEndingAnchorTypeEditor;
import java.util.Collections;
import java.util.Map;
import org.geotools.factory.Factory;
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
import org.saig.core.gui.swing.sldeditor.property.ScaleEditor;
import org.saig.core.gui.swing.sldeditor.property.StrokeEditor;
import org.saig.core.gui.swing.sldeditor.property.SymbolEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultPropertyEditorFactory;
import org.saig.core.styling.FeatureTypeStyle;

public abstract class PropertyEditorFactory
implements Factory {
    private static PropertyEditorFactory factory = null;
    protected boolean inExpertMode;

    public static PropertyEditorFactory createPropertyEditorFactory() {
        if (factory == null) {
            factory = new DefaultPropertyEditorFactory();
        }
        return factory;
    }

    public void setInExpertMode(boolean inExpertMode) {
        this.inExpertMode = inExpertMode;
    }

    public boolean isInExpertMode() {
        return this.inExpertMode;
    }

    public abstract ExpressionEditor createExpressionEditor(FeatureSchema var1);

    public abstract FilterEditor createFilterEditor(FeatureSchema var1);

    public abstract ExpressionEditor createColorEditor(FeatureSchema var1);

    public abstract DashArrayEditor createDashArrayEditor();

    public abstract DashArrayEditor createSimpleDashArrayEditor();

    public abstract ExternalGraphicEditor createExternalGraphicEditor();

    public abstract ExpressionEditor createFeatureAttributeChooser(FeatureSchema var1);

    public abstract ExpressionEditor createFeatureAttributeChooser(FeatureSchema var1, Class<?>[] var2);

    public abstract FillEditor createFillEditor(FeatureSchema var1);

    public abstract FillEditor createCompactFillEditor(FeatureSchema var1);

    public abstract FontListChooser createFontListChooser();

    public abstract GeometryChooser createGeometryChooser(FeatureSchema var1);

    public abstract GraphicEditor createGraphicEditor(FeatureSchema var1);

    public abstract GraphicEditor createGraphicFillEditor(FeatureSchema var1);

    public abstract GraphicEditor createGraphicStrokeEditor(FeatureSchema var1);

    public abstract GraphicEditor createGraphicWKTFillPatternEditor(FeatureSchema var1);

    public abstract LabelPlacementEditor createLabelPlacementEditor(FeatureSchema var1);

    public abstract MarkEditor createMarkEditor(FeatureSchema var1);

    public abstract FeatureTypeChooser createFeatureTypeChooser(FeatureTypeStyle[] var1);

    public abstract ExpressionEditor createNumberEditor(Number var1, Number var2, Number var3, Number var4, FeatureSchema var5);

    public abstract ExpressionEditor createOpacityEditor(FeatureSchema var1);

    public abstract ExpressionEditor createIntSizeEditor(FeatureSchema var1);

    public abstract ExpressionEditor createDoubleSizeEditor(FeatureSchema var1);

    public abstract ExpressionEditor createDoubleEditor(FeatureSchema var1);

    public abstract ExpressionEditor createRotationEditor(FeatureSchema var1);

    public abstract ScaleEditor createScaleEditor();

    public abstract StrokeEditor createStrokeEditor(FeatureSchema var1);

    public abstract SymbolEditor createSymbolEditor(FeatureSchema var1);

    public abstract FixedOrByAttributeExpressionEditor createFixedOrByAttributeEditor(FeatureSchema var1, boolean var2);

    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }

    public abstract FixedOrByAttributeExpressionEditor createFixedOrByAttributeEditor(FeatureSchema var1, Number var2, Number var3, Number var4, Number var5, boolean var6);

    public abstract DecoratorChooser createDecoratorChooser(FeatureSchema var1);

    public abstract LineToLabelEndingAnchorTypeEditor createLineToLabelEndingAnchorTypeEditor();

    public abstract GraphicResizeEditor createGraphicResizeEditor();

    public abstract GradientFillEditor createGradientFillEditor();
}

