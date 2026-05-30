/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import es.kosmo.core.renderer.decorators.IDecorator;
import java.util.List;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.Symbolizer;

public interface PointSymbolizer
extends Symbolizer {
    public Graphic getGraphic();

    public void setGraphic(Graphic var1);

    public String getGeometryPropertyName();

    public void setGeometryPropertyName(String var1);

    @Override
    public void accept(StyleVisitor var1);

    public List<IDecorator> getDecorators();

    public void setDecorators(List<IDecorator> var1);
}

