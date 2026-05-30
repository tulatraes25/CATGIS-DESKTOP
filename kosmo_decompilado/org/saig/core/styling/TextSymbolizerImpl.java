/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.geotools.resources.Utilities
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import com.vividsolutions.jump.feature.Feature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.styling.Fill;
import org.saig.core.styling.FillImpl;
import org.saig.core.styling.Font;
import org.saig.core.styling.FontImpl;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.Halo;
import org.saig.core.styling.ILabelResolver;
import org.saig.core.styling.LabelPlacement;
import org.saig.core.styling.PointPlacementImpl;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.TextSymbolizer;

public class TextSymbolizerImpl
implements TextSymbolizer,
Cloneable {
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private Fill fill;
    private List<Font> fonts = new ArrayList<Font>();
    private Graphic graphic = null;
    private Halo halo;
    private LabelPlacement labelPlacement;
    private String geometryPropertyName = null;
    private Expression label = null;
    private String unitsOfMeasurement;
    private Map<String, String> optionsMap = null;
    private Expression priority = null;
    protected ILabelResolver labelResolver;
    private boolean scale = false;
    private double minScaleValue;
    private double maxScaleValue;
    private boolean active = true;
    private Expression heightAttr;
    private Expression attrRotation;

    public TextSymbolizerImpl() {
        this.fill = new FillImpl();
        this.fill.setColor(filterFactory.createLiteralExpression("#000000"));
        this.fill.setOpacity(filterFactory.createLiteralExpression(1.0));
        this.fill.setGraphicFill(null);
        this.halo = null;
        this.labelPlacement = new PointPlacementImpl();
    }

    @Override
    public Fill getFill() {
        return this.fill;
    }

    @Override
    public void setFill(Fill fill) {
        this.fill = fill;
    }

    @Override
    public Font[] getFonts() {
        if (this.fonts.size() == 0) {
            this.fonts.add(new FontImpl());
        }
        return this.fonts.toArray(new Font[0]);
    }

    public void addFont(Font font) {
        this.fonts.add(font);
    }

    @Override
    public void setFonts(Font[] fonts) {
        this.fonts.clear();
        int i = 0;
        while (i < fonts.length) {
            if (fonts.length == 1 || fonts.length > 1 && !fonts[i].isEmptyFont()) {
                this.addFont(fonts[i]);
            }
            ++i;
        }
    }

    @Override
    public Halo getHalo() {
        return this.halo;
    }

    @Override
    public void setHalo(Halo halo) {
        this.halo = halo;
    }

    @Override
    public Expression getLabel() {
        return this.label;
    }

    @Override
    public void setLabel(Expression label) {
        this.label = label;
    }

    @Override
    public LabelPlacement getLabelPlacement() {
        return this.labelPlacement;
    }

    @Override
    public void setLabelPlacement(LabelPlacement labelPlacement) {
        this.labelPlacement = labelPlacement;
    }

    @Override
    public String getGeometryPropertyName() {
        return this.geometryPropertyName;
    }

    @Override
    public void setGeometryPropertyName(String geometryPropertyName) {
        this.geometryPropertyName = geometryPropertyName;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        TextSymbolizerImpl clone = new TextSymbolizerImpl();
        clone.setActive(this.isActive());
        if (this.fill != null) {
            clone.setFill((Fill)((Cloneable)this.fill).clone());
        }
        if (this.fonts != null) {
            clone.fonts = new ArrayList<Font>();
            Font[] fuentes = new Font[this.fonts.size()];
            int i = 0;
            while (i < fuentes.length) {
                Font currentFont = this.fonts.get(i);
                fuentes[i] = (Font)((Cloneable)currentFont).clone();
                ++i;
            }
            clone.setFonts(fuentes);
        }
        clone.setGeometryPropertyName(this.getGeometryPropertyName());
        if (this.halo != null) {
            clone.setHalo((Halo)((Cloneable)this.halo).clone());
        }
        clone.setLabel(this.getLabel());
        if (this.labelPlacement != null) {
            clone.setLabelPlacement((LabelPlacement)((Cloneable)this.labelPlacement).clone());
        }
        clone.setScale(this.isScale());
        if (clone.isScale()) {
            clone.setScaleMaxValue(this.getScaleMaxValue());
            clone.setScaleMinValue(this.getScaleMinValue());
        }
        clone.setHeightAttribute(this.getHeightAttribute());
        clone.setAttributeRotation(this.getAttributeRotation());
        clone.setPriority(this.getPriority());
        if (this.optionsMap != null) {
            for (String key : this.optionsMap.keySet()) {
                clone.addToOptions(key, this.optionsMap.get(key));
            }
        }
        if (this.unitsOfMeasurement != null) {
            clone.setUnitsOfMeasurement(this.unitsOfMeasurement);
        }
        if (this.graphic != null) {
            clone.setGraphic((Graphic)((Cloneable)this.graphic).clone());
        }
        return clone;
    }

    public int hashCode() {
        int PRIME = 1000003;
        int result = 0;
        if (this.fill != null) {
            result = 1000003 * result + this.fill.hashCode();
        }
        if (this.fonts != null) {
            result = 1000003 * result + this.fonts.hashCode();
        }
        if (this.halo != null) {
            result = 1000003 * result + this.halo.hashCode();
        }
        if (this.labelPlacement != null) {
            result = 1000003 * result + this.labelPlacement.hashCode();
        }
        if (this.geometryPropertyName != null) {
            result = 1000003 * result + this.geometryPropertyName.hashCode();
        }
        if (this.label != null) {
            result = 1000003 * result + this.label.hashCode();
        }
        if (this.unitsOfMeasurement != null) {
            result = 1000003 * result + this.unitsOfMeasurement.hashCode();
        }
        return result;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth == null) {
            return false;
        }
        if (oth instanceof TextSymbolizerImpl) {
            TextSymbolizerImpl other = (TextSymbolizerImpl)oth;
            return Utilities.equals((Object)this.geometryPropertyName, (Object)other.geometryPropertyName) && Utilities.equals((Object)this.label, (Object)other.label) && Utilities.equals((Object)this.halo, (Object)other.halo) && Utilities.equals(this.fonts, other.fonts) && Utilities.equals((Object)this.labelPlacement, (Object)other.labelPlacement) && Utilities.equals((Object)this.fill, (Object)other.fill);
        }
        return false;
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
    public double getScaleMaxValue() {
        return this.maxScaleValue;
    }

    @Override
    public double getScaleMinValue() {
        return this.minScaleValue;
    }

    @Override
    public boolean isScale() {
        return this.scale;
    }

    @Override
    public void setScale(boolean scale) {
        this.scale = scale;
    }

    @Override
    public void setScaleMaxValue(double maxScale) {
        this.maxScaleValue = maxScale;
    }

    @Override
    public void setScaleMinValue(double minScale) {
        this.minScaleValue = minScale;
    }

    @Override
    public Expression getHeightAttribute() {
        return this.heightAttr;
    }

    @Override
    public void setHeightAttribute(Expression heightAttr) {
        this.heightAttr = heightAttr;
    }

    @Override
    public Expression getAttributeRotation() {
        return this.attrRotation;
    }

    @Override
    public void setAttributeRotation(Expression attrRotation) {
        this.attrRotation = attrRotation;
    }

    @Override
    public ILabelResolver getLabelResolver() {
        return this.labelResolver;
    }

    @Override
    public void setLabelResolver(ILabelResolver labelResolver) {
        this.labelResolver = labelResolver;
    }

    @Override
    public Object resolveLabel(Feature feature) {
        if (this.labelResolver != null) {
            return this.labelResolver.resolveLabel(feature);
        }
        return this.getLabel().getValue(feature);
    }

    @Override
    public void setPriority(Expression priority) {
        if (this.priority == priority) {
            return;
        }
        this.priority = priority;
    }

    @Override
    public Expression getPriority() {
        return this.priority;
    }

    @Override
    public void addToOptions(String key, String value) {
        if (this.optionsMap == null) {
            this.optionsMap = new HashMap<String, String>();
        }
        if (StringUtils.isEmpty((String)value)) {
            this.optionsMap.remove(key);
        } else {
            this.optionsMap.put(key, value);
        }
    }

    @Override
    public String getOption(String key) {
        if (this.optionsMap == null) {
            return null;
        }
        String value = this.optionsMap.get(key);
        if (StringUtils.isEmpty((String)value)) {
            return null;
        }
        return value;
    }

    @Override
    public Map<String, String> getOptions() {
        return this.optionsMap;
    }

    @Override
    public void setOptions(Map<String, String> options) {
        this.optionsMap = options;
    }

    @Override
    public Graphic getGraphic() {
        return this.graphic;
    }

    @Override
    public void setGraphic(Graphic graphic) {
        if (this.graphic == graphic) {
            return;
        }
        this.graphic = graphic;
    }

    @Override
    public String getUnitsOfMeasurement() {
        if (StringUtils.isEmpty((String)this.unitsOfMeasurement)) {
            this.unitsOfMeasurement = "pixel";
        }
        return this.unitsOfMeasurement;
    }

    @Override
    public void setUnitsOfMeasurement(String units) {
        this.unitsOfMeasurement = units;
    }
}

