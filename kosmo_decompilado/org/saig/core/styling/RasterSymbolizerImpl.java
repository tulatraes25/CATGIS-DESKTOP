/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.resources.Utilities
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.styling.ChannelSelection;
import org.saig.core.styling.ChannelSelectionImpl;
import org.saig.core.styling.ColorMap;
import org.saig.core.styling.ColorMapImpl;
import org.saig.core.styling.ContrastEnhancement;
import org.saig.core.styling.ContrastEnhancementImpl;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.ShadedRelief;
import org.saig.core.styling.ShadedReliefImpl;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.Symbolizer;

public class RasterSymbolizerImpl
implements RasterSymbolizer,
Cloneable {
    private FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private ChannelSelection channelSelection = new ChannelSelectionImpl();
    private ColorMap colorMap = new ColorMapImpl();
    private ContrastEnhancement contrastEnhancement = new ContrastEnhancementImpl();
    private ShadedRelief shadedRelief = new ShadedReliefImpl();
    private String geometryName = "raster";
    private Symbolizer symbolizer;
    private Expression opacity = this.filterFactory.createLiteralExpression(1.0);
    private Expression overlap = this.filterFactory.createLiteralExpression("Random");
    private boolean active = true;
    protected String unitsOfMeasurement;

    public int hashCode() {
        int key = 0;
        key = this.channelSelection.hashCode();
        key = key * 13 + this.colorMap.hashCode();
        key = key * 13 + this.contrastEnhancement.hashCode();
        key = key * 13 + this.shadedRelief.hashCode();
        key = key * 13 + this.opacity.hashCode();
        key = key * 13 + this.overlap.hashCode();
        if (this.geometryName != null) {
            key = key * 13 + this.geometryName.hashCode();
        }
        return key;
    }

    @Override
    public ChannelSelection getChannelSelection() {
        return this.channelSelection;
    }

    @Override
    public ColorMap getColorMap() {
        return this.colorMap;
    }

    @Override
    public ContrastEnhancement getContrastEnhancement() {
        return this.contrastEnhancement;
    }

    @Override
    public String getGeometryPropertyName() {
        return this.geometryName;
    }

    @Override
    public Symbolizer getImageOutline() {
        return this.symbolizer;
    }

    @Override
    public Expression getOpacity() {
        return this.opacity;
    }

    @Override
    public Expression getOverlap() {
        return this.overlap;
    }

    @Override
    public ShadedRelief getShadedRelief() {
        return this.shadedRelief;
    }

    @Override
    public void setChannelSelection(ChannelSelection channel) {
        this.channelSelection = channel;
    }

    @Override
    public void setColorMap(ColorMap colorMap) {
        this.colorMap = colorMap;
    }

    @Override
    public void setContrastEnhancement(ContrastEnhancement cEnhancement) {
        this.contrastEnhancement = cEnhancement;
    }

    @Override
    public void setGeometryPropertyName(String geometryPropertyName) {
        this.geometryName = geometryPropertyName;
    }

    @Override
    public void setImageOutline(Symbolizer symbolizer) {
        if (symbolizer instanceof LineSymbolizer || symbolizer instanceof PolygonSymbolizer) {
            this.symbolizer = symbolizer;
            return;
        }
        throw new IllegalArgumentException("Only a line or polygon symbolizer may be used to outline a raster");
    }

    @Override
    public void setOpacity(Expression opacity) {
        this.opacity = opacity;
    }

    @Override
    public void setOverlap(Expression overlap) {
        this.overlap = overlap;
    }

    @Override
    public void setShadedRelief(ShadedRelief relief) {
        this.shadedRelief = relief;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        RasterSymbolizerImpl clone = new RasterSymbolizerImpl();
        clone.setActive(this.isActive());
        if (this.channelSelection != null) {
            clone.setChannelSelection((ChannelSelection)((Cloneable)this.channelSelection).clone());
        }
        if (this.colorMap != null) {
            clone.setColorMap((ColorMap)((Cloneable)this.colorMap).clone());
        }
        if (this.contrastEnhancement != null) {
            clone.setContrastEnhancement((ContrastEnhancement)((Cloneable)this.contrastEnhancement).clone());
        }
        clone.setGeometryPropertyName(this.getGeometryPropertyName());
        if (this.symbolizer != null) {
            clone.setImageOutline((Symbolizer)((Cloneable)this.symbolizer).clone());
        }
        clone.setOpacity(this.getOpacity());
        clone.setOverlap(this.getOverlap());
        if (this.shadedRelief != null) {
            clone.setShadedRelief((ShadedRelief)((Cloneable)this.shadedRelief).clone());
        }
        clone.setUnitsOfMeasurement(this.getUnitsOfMeasurement());
        return clone;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public String getUnitsOfMeasurement() {
        return this.unitsOfMeasurement;
    }

    @Override
    public void setUnitsOfMeasurement(String units) {
        this.unitsOfMeasurement = units;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth instanceof RasterSymbolizerImpl) {
            RasterSymbolizerImpl other = (RasterSymbolizerImpl)oth;
            return Utilities.equals((Object)this.geometryName, (Object)other.geometryName) && Utilities.equals((Object)this.channelSelection, (Object)other.channelSelection) && Utilities.equals((Object)this.colorMap, (Object)other.colorMap) && Utilities.equals((Object)this.contrastEnhancement, (Object)other.contrastEnhancement) && Utilities.equals((Object)this.opacity, (Object)other.opacity) && Utilities.equals((Object)this.overlap, (Object)other.overlap) && Utilities.equals((Object)this.shadedRelief, (Object)other.shadedRelief);
        }
        return false;
    }
}

