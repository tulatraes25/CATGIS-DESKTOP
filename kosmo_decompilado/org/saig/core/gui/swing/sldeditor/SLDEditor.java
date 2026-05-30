/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor;

import org.saig.core.filter.FilterFactory;
import org.saig.core.gui.swing.sldeditor.property.PropertyEditorFactory;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerEditorFactory;
import org.saig.core.gui.swing.sldeditor.util.StyleCloner;
import org.saig.core.styling.StyleBuilder;
import org.saig.core.styling.StyleFactory;

public interface SLDEditor {
    public static final StyleFactory styleFactory = StyleFactory.createStyleFactory();
    public static final PropertyEditorFactory propertyEditorFactory = PropertyEditorFactory.createPropertyEditorFactory();
    public static final SymbolizerEditorFactory symbolizerEditorFactory = SymbolizerEditorFactory.createPropertyEditorFactory();
    public static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    public static final StyleCloner styleCloner = new StyleCloner(styleFactory);
    public static final StyleBuilder styleBuilder = new StyleBuilder(styleFactory, filterFactory);
}

