/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.filter.Filter;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.Symbolizer;
import org.saig.core.util.language.ITranslatable;

public interface Rule
extends ITranslatable {
    public String getName();

    public void setName(String var1);

    @Override
    public String getTitle();

    public void setTitle(String var1);

    public String getAbstract();

    public void setAbstract(String var1);

    public double getMinScaleDenominator();

    public void setMinScaleDenominator(double var1);

    public double getMaxScaleDenominator();

    public void setMaxScaleDenominator(double var1);

    public Filter getFilter();

    public void setFilter(Filter var1);

    public boolean isElseFilter();

    public void setElseFilter(boolean var1);

    public Graphic[] getLegendGraphic();

    public void setLegendGraphic(Graphic[] var1);

    public Symbolizer[] getSymbolizers();

    public void setSymbolizers(Symbolizer[] var1);

    public void accept(StyleVisitor var1);

    public boolean isGeometryFilter();

    public boolean isEnabled();

    public void setEnabled(boolean var1);
}

