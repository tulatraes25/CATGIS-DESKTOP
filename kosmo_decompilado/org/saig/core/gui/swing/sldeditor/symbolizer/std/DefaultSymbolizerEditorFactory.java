/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.symbolizer.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import es.kosmo.desktop.widgets.symbology.sldeditor.symbolizer.std.DefaultSymbolizerChooserDialog;
import java.awt.Component;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerChooserDialog;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerEditorFactory;
import org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultLineSymbolizerEditor;
import org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultPointSymbolizerEditor;
import org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultPolygonSymbolizerEditor;
import org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultRasterSymbolizerEditor;
import org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor;

public class DefaultSymbolizerEditorFactory
extends SymbolizerEditorFactory {
    @Override
    public SymbolizerEditor createLineSymbolizerEditor(FeatureSchema featureType) {
        return new DefaultLineSymbolizerEditor(featureType);
    }

    @Override
    public SymbolizerEditor createPointSymbolizerEditor(FeatureSchema featureType) {
        return new DefaultPointSymbolizerEditor(featureType);
    }

    @Override
    public SymbolizerEditor createPolygonSymbolizerEditor(FeatureSchema featureType) {
        return new DefaultPolygonSymbolizerEditor(featureType);
    }

    @Override
    public SymbolizerEditor createTextSymbolizerEditor(FeatureSchema featureType) {
        return new DefaultTextSymbolizerEditor(featureType);
    }

    @Override
    public SymbolizerEditor createRasterSymbolizerEditor(FeatureSchema featureType) {
        return new DefaultRasterSymbolizerEditor(featureType);
    }

    @Override
    public SymbolizerChooserDialog createSymbolizerChooserDialog(Component parent, FeatureSchema featureType) {
        return new DefaultSymbolizerChooserDialog(parent, featureType);
    }
}

