/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.symbolizer.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.GridBagLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.gui.swing.sldeditor.property.ExpressionEditor;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;

public class DefaultRasterSymbolizerEditor
extends SymbolizerEditor
implements SLDEditor {
    private static final long serialVersionUID = 1L;
    RasterSymbolizer symbolizer;
    ExpressionEditor opacityEditor;

    public DefaultRasterSymbolizerEditor(FeatureSchema featureType) {
        this(featureType, null);
    }

    public DefaultRasterSymbolizerEditor(FeatureSchema ft, RasterSymbolizer symbolizer) {
        if (symbolizer == null) {
            symbolizer = styleBuilder.createRasterSymbolizer();
        }
        this.setLayout(new GridBagLayout());
        JLabel opacityLabel = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFillEditor.opacity"));
        this.opacityEditor = propertyEditorFactory.createOpacityEditor(ft);
        FormUtils.addRowInGBL(this, 0, 0, FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.general")));
        FormUtils.addRowInGBL((JComponent)this, 1, 0, opacityLabel, (JComponent)this.opacityEditor, false);
        FormUtils.addFiller(this, 2, 0);
        this.setSymbolizer(symbolizer);
    }

    @Override
    public void setSymbolizer(Symbolizer symbolizer) {
        if (!(symbolizer instanceof RasterSymbolizer)) {
            throw new IllegalArgumentException(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultPointSymbolizerEditor.cannot-set-symbolizer-other-than-a-point-symbolizer"));
        }
        this.symbolizer = (RasterSymbolizer)symbolizer;
        this.opacityEditor.setExpression(this.symbolizer.getOpacity());
    }

    @Override
    public Symbolizer getSymbolizer() {
        this.symbolizer.setGeometryPropertyName(null);
        this.symbolizer.setOpacity(this.opacityEditor.getExpression());
        return this.symbolizer;
    }
}

