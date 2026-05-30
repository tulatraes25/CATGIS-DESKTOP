/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.symbolizer;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.Component;
import java.util.Collections;
import java.util.Map;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerChooserDialog;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultSymbolizerEditorFactory;

public abstract class SymbolizerEditorFactory {
    private static SymbolizerEditorFactory factory = null;
    protected boolean inExpertMode;

    public static SymbolizerEditorFactory createPropertyEditorFactory() {
        if (factory == null) {
            factory = new DefaultSymbolizerEditorFactory();
        }
        return factory;
    }

    public abstract SymbolizerEditor createLineSymbolizerEditor(FeatureSchema var1);

    public abstract SymbolizerEditor createPointSymbolizerEditor(FeatureSchema var1);

    public abstract SymbolizerEditor createPolygonSymbolizerEditor(FeatureSchema var1);

    public abstract SymbolizerEditor createTextSymbolizerEditor(FeatureSchema var1);

    public abstract SymbolizerEditor createRasterSymbolizerEditor(FeatureSchema var1);

    public abstract SymbolizerChooserDialog createSymbolizerChooserDialog(Component var1, FeatureSchema var2);

    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}

