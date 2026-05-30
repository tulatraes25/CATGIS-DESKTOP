/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import es.kosmo.core.renderer.decorators.IDecorator;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.opengis.util.Cloneable;
import org.saig.core.filter.Expression;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.StyleVisitor;

public class LineSymbolizerImpl
implements LineSymbolizer,
Cloneable {
    private Stroke stroke = null;
    private String geometryName = null;
    private boolean active = true;
    protected Expression offset;
    protected List<IDecorator> decorators = new ArrayList<IDecorator>();
    protected String unitsOfMeasurement;

    @Override
    public String getGeometryPropertyName() {
        return this.geometryName;
    }

    @Override
    public void setGeometryPropertyName(String name) {
        this.geometryName = name;
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
        LineSymbolizerImpl clone = new LineSymbolizerImpl();
        clone.setActive(this.isActive());
        clone.setGeometryPropertyName(this.getGeometryPropertyName());
        if (this.stroke != null) {
            clone.setStroke((Stroke)this.stroke.clone());
        }
        clone.setOffset(this.getOffset());
        clone.setDecorators(this.getDecorators());
        clone.setUnitsOfMeasurement(this.getUnitsOfMeasurement());
        return clone;
    }

    public int hashCode() {
        int PRIME = 1000003;
        int result = 0;
        if (this.stroke != null) {
            result = 1000003 * result + this.stroke.hashCode();
        }
        if (this.geometryName != null) {
            result = 1000003 * result + this.geometryName.hashCode();
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
        if (oth.getClass() != this.getClass()) {
            return false;
        }
        LineSymbolizerImpl other = (LineSymbolizerImpl)oth;
        if (this.geometryName == null ? other.geometryName != null : !this.geometryName.equals(other.geometryName)) {
            return false;
        }
        return !(this.stroke == null ? other.stroke != null : !this.stroke.equals(other.stroke));
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
    public Expression getOffset() {
        return this.offset;
    }

    @Override
    public void setOffset(Expression newOffset) {
        this.offset = newOffset;
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

