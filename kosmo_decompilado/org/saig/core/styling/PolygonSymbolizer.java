/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import es.kosmo.core.renderer.decorators.IDecorator;
import java.util.List;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.Symbolizer;

public interface PolygonSymbolizer
extends Symbolizer {
    public Fill getFill();

    public void setFill(Fill var1);

    public Stroke getStroke();

    public void setStroke(Stroke var1);

    public String getGeometryPropertyName();

    public void setGeometryPropertyName(String var1);

    @Override
    public void accept(StyleVisitor var1);

    public List<IDecorator> getDecorators();

    public void setDecorators(List<IDecorator> var1);
}

