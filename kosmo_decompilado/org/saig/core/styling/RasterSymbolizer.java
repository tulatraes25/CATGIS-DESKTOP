/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.filter.Expression;
import org.saig.core.styling.ChannelSelection;
import org.saig.core.styling.ColorMap;
import org.saig.core.styling.ContrastEnhancement;
import org.saig.core.styling.ShadedRelief;
import org.saig.core.styling.Symbolizer;

public interface RasterSymbolizer
extends Symbolizer {
    public void setGeometryPropertyName(String var1);

    public String getGeometryPropertyName();

    public void setOpacity(Expression var1);

    public Expression getOpacity();

    public void setChannelSelection(ChannelSelection var1);

    public ChannelSelection getChannelSelection();

    public void setOverlap(Expression var1);

    public Expression getOverlap();

    public void setColorMap(ColorMap var1);

    public ColorMap getColorMap();

    public void setContrastEnhancement(ContrastEnhancement var1);

    public ContrastEnhancement getContrastEnhancement();

    public void setShadedRelief(ShadedRelief var1);

    public ShadedRelief getShadedRelief();

    public void setImageOutline(Symbolizer var1);

    public Symbolizer getImageOutline();
}

