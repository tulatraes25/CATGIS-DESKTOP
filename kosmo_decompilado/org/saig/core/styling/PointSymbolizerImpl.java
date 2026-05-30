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
import org.saig.core.styling.Graphic;
import org.saig.core.styling.GraphicImpl;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.StyleVisitor;

public class PointSymbolizerImpl
implements PointSymbolizer,
Cloneable {
    protected List<IDecorator> decorators;
    private String geometryPropertyName = null;
    private Graphic graphic = new GraphicImpl();
    private boolean active = true;
    private String unitsOfMeasurement;

    public PointSymbolizerImpl() {
        this.decorators = new ArrayList<IDecorator>();
    }

    @Override
    public String getGeometryPropertyName() {
        return this.geometryPropertyName;
    }

    @Override
    public void setGeometryPropertyName(String name) {
        this.geometryPropertyName = name;
    }

    @Override
    public Graphic getGraphic() {
        return this.graphic;
    }

    @Override
    public void setGraphic(Graphic graphic) {
        this.graphic = graphic;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        PointSymbolizerImpl clone = new PointSymbolizerImpl();
        clone.setActive(this.isActive());
        clone.setGeometryPropertyName(this.getGeometryPropertyName());
        if (this.graphic != null) {
            clone.setGraphic((Graphic)((Cloneable)this.graphic).clone());
        }
        clone.setUnitsOfMeasurement(this.getUnitsOfMeasurement());
        clone.setDecorators(this.getDecorators());
        return clone;
    }

    public int hashCode() {
        int PRIME = 1000003;
        int result = 0;
        if (this.geometryPropertyName != null) {
            result = 1000003 * result + this.geometryPropertyName.hashCode();
        }
        if (this.graphic != null) {
            result = 1000003 * result + this.graphic.hashCode();
        }
        return result;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth instanceof PointSymbolizerImpl) {
            PointSymbolizerImpl other = (PointSymbolizerImpl)oth;
            return Utilities.equals((Object)this.geometryPropertyName, (Object)other.geometryPropertyName) && Utilities.equals((Object)this.graphic, (Object)other.graphic);
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

