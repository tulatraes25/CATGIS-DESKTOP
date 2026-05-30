/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.geotools.resources.Utilities
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import es.kosmo.core.renderer.decorators.IDecorator;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;
import org.saig.core.styling.Fill;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.StyleVisitor;

public class PolygonSymbolizerImpl
implements PolygonSymbolizer,
Cloneable {
    private Fill fill = null;
    private Stroke stroke = null;
    private String geometryPropertyName = null;
    private boolean active = true;
    protected List<IDecorator> decorators = new ArrayList<IDecorator>();
    protected String unitsOfMeasurement;

    @Override
    public String getGeometryPropertyName() {
        return this.geometryPropertyName;
    }

    @Override
    public void setGeometryPropertyName(String name) {
        this.geometryPropertyName = name;
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
    public Stroke getStroke() {
        return this.stroke;
    }

    @Override
    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        PolygonSymbolizerImpl clone = new PolygonSymbolizerImpl();
        clone.setActive(this.isActive());
        if (this.fill != null) {
            clone.setFill((Fill)((Cloneable)this.fill).clone());
        }
        clone.setGeometryPropertyName(this.getGeometryPropertyName());
        if (this.stroke != null) {
            clone.setStroke((Stroke)((Cloneable)this.stroke).clone());
        }
        clone.setDecorators(this.getDecorators());
        clone.setUnitsOfMeasurement(this.getUnitsOfMeasurement());
        return clone;
    }

    public int hashCode() {
        int PRIME = 1000003;
        int result = 0;
        if (this.fill != null) {
            result = 1000003 * result + this.fill.hashCode();
        }
        if (this.stroke != null) {
            result = 1000003 * result + this.stroke.hashCode();
        }
        if (this.geometryPropertyName != null) {
            result = 1000003 * result + this.geometryPropertyName.hashCode();
        }
        return result;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth instanceof PolygonSymbolizerImpl) {
            PolygonSymbolizerImpl other = (PolygonSymbolizerImpl)oth;
            return Utilities.equals((Object)this.geometryPropertyName, (Object)other.geometryPropertyName) && Utilities.equals((Object)this.fill, (Object)other.fill) && Utilities.equals((Object)this.stroke, (Object)other.stroke);
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
    public List<IDecorator> getDecorators() {
        return this.decorators;
    }

    @Override
    public void setDecorators(List<IDecorator> decs) {
        this.decorators = decs;
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

