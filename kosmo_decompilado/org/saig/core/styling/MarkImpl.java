/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import org.apache.log4j.Logger;
import org.opengis.util.Cloneable;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Mark;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.StyleFactoryImpl;
import org.saig.core.styling.StyleVisitor;

public class MarkImpl
implements Mark,
Cloneable {
    private static final Logger LOGGER = Logger.getLogger(MarkImpl.class);
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    protected Fill fill;
    protected Stroke stroke;
    protected Expression wellKnownName = null;
    protected Expression rotation = null;
    protected Expression size = null;

    public MarkImpl() {
    }

    public static MarkImpl createDefaultMark() {
        MarkImpl mark = new MarkImpl();
        LOGGER.debug((Object)"creating defaultMark");
        try {
            StyleFactoryImpl sfac = new StyleFactoryImpl();
            mark.setFill(((StyleFactory)sfac).getDefaultFill());
            mark.setStroke(((StyleFactory)sfac).getDefaultStroke());
            mark.setWellKnownName(filterFactory.createLiteralExpression("square"));
            mark.setSize(filterFactory.createLiteralExpression(new Integer(6)));
            mark.setRotation(filterFactory.createLiteralExpression(new Double(0.0)));
        }
        catch (IllegalFilterException ife) {
            MarkImpl.severe("<init>", "Failed to build default mark: ", ife);
        }
        return mark;
    }

    public MarkImpl(String name) {
        this();
        LOGGER.debug((Object)("Creating " + name + " type mark"));
        this.setWellKnownName(name);
    }

    private static void severe(String method, String message, Exception exception) {
        LOGGER.error((Object)message, (Throwable)exception);
    }

    @Override
    public Fill getFill() {
        return this.fill;
    }

    @Override
    public Stroke getStroke() {
        return this.stroke;
    }

    @Override
    public Expression getWellKnownName() {
        return this.wellKnownName;
    }

    @Override
    public void setFill(Fill fill) {
        this.fill = fill;
    }

    @Override
    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    @Override
    public void setSize(Expression size) {
        this.size = size;
    }

    public void setSize(int size) {
        try {
            this.setSize(filterFactory.createLiteralExpression(new Integer(size)));
        }
        catch (IllegalFilterException mfe) {
            MarkImpl.severe("setSize", "Problem setting Opacity", mfe);
        }
    }

    @Override
    public void setWellKnownName(Expression wellKnownName) {
        this.wellKnownName = wellKnownName;
    }

    public void setWellKnownName(String name) {
        this.setWellKnownName(filterFactory.createLiteralExpression(name));
    }

    @Override
    public void setRotation(Expression rotation) {
        this.rotation = rotation;
    }

    public void setRotation(double rotation) {
        try {
            this.setRotation(filterFactory.createLiteralExpression(new Double(rotation)));
        }
        catch (IllegalFilterException mfe) {
            MarkImpl.severe("setRotation", "Problem setting Rotation", mfe);
        }
    }

    @Override
    public Expression getSize() {
        return this.size;
    }

    @Override
    public Expression getRotation() {
        return this.rotation;
    }

    public String toString() {
        return this.wellKnownName.toString();
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        MarkImpl clone = new MarkImpl();
        if (this.fill != null) {
            clone.setFill((Fill)((Cloneable)this.fill).clone());
        }
        clone.setRotation(this.getRotation());
        clone.setSize(this.getSize());
        if (this.stroke != null) {
            clone.setStroke((Stroke)this.stroke.clone());
        }
        clone.setWellKnownName(this.getWellKnownName());
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
        if (this.wellKnownName != null) {
            result = 1000003 * result + this.wellKnownName.hashCode();
        }
        if (this.rotation != null) {
            result = 1000003 * result + this.rotation.hashCode();
        }
        if (this.size != null) {
            result = 1000003 * result + this.size.hashCode();
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
        MarkImpl other = (MarkImpl)oth;
        if (this.wellKnownName == null ? other.wellKnownName != null : !this.wellKnownName.equals(other.wellKnownName)) {
            return false;
        }
        if (this.rotation == null ? other.rotation != null : !this.rotation.equals(other.rotation)) {
            return false;
        }
        if (this.size == null ? other.size != null : !this.size.equals(other.size)) {
            return false;
        }
        if (this.fill == null ? other.fill != null : !this.fill.equals(other.fill)) {
            return false;
        }
        return !(this.stroke == null ? other.stroke != null : !this.stroke.equals(other.stroke));
    }

    @Override
    public boolean isEmptyMark() {
        StyleFactoryImpl sfac = new StyleFactoryImpl();
        boolean solucion = false;
        try {
            solucion = ((StyleFactory)sfac).getDefaultFill().equals(this.fill) && ((StyleFactory)sfac).getDefaultStroke().equals(this.stroke) && filterFactory.createLiteralExpression("square").equals(this.wellKnownName) && filterFactory.createLiteralExpression(new Integer(6)).equals(this.size) && filterFactory.createLiteralExpression(new Double(0.0)).equals(this.rotation);
        }
        catch (IllegalFilterException ife) {
            MarkImpl.severe("<init>", "Failed to check empty mark: ", ife);
        }
        return solucion;
    }
}

