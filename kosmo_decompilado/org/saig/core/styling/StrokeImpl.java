/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import com.vividsolutions.jump.feature.Feature;
import java.awt.Color;
import java.util.Arrays;
import org.opengis.util.Cloneable;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.StyleVisitor;

public class StrokeImpl
implements Stroke,
Cloneable {
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private Expression color;
    private float[] dashArray;
    private Expression dashOffset;
    private Graphic fillGraphic;
    private Graphic strokeGraphic;
    private Expression lineCap;
    private Expression lineJoin;
    private Expression opacity;
    private Expression width;

    @Override
    public Expression getColor() {
        return this.color;
    }

    @Override
    public void setColor(Expression color) {
        if (color == null) {
            return;
        }
        this.color = color;
    }

    public void setColor(String color) {
        this.setColor(filterFactory.createLiteralExpression(color));
    }

    @Override
    public float[] getDashArray() {
        float[] ret = new float[]{};
        if (this.dashArray != null) {
            ret = new float[this.dashArray.length];
            System.arraycopy(this.dashArray, 0, ret, 0, this.dashArray.length);
        }
        return ret;
    }

    @Override
    public void setDashArray(float[] dashPattern) {
        this.dashArray = dashPattern;
    }

    @Override
    public Expression getDashOffset() {
        return this.dashOffset;
    }

    @Override
    public void setDashOffset(Expression offset) {
        if (offset == null) {
            return;
        }
        this.dashOffset = offset;
    }

    @Override
    public Graphic getGraphicFill() {
        return this.fillGraphic;
    }

    @Override
    public void setGraphicFill(Graphic graphic) {
        this.fillGraphic = graphic;
    }

    @Override
    public Graphic getGraphicStroke() {
        return this.strokeGraphic;
    }

    @Override
    public void setGraphicStroke(Graphic graphic) {
        this.strokeGraphic = graphic;
    }

    @Override
    public Expression getLineCap() {
        return this.lineCap;
    }

    @Override
    public void setLineCap(Expression cap) {
        if (cap == null) {
            return;
        }
        this.lineCap = cap;
    }

    @Override
    public Expression getLineJoin() {
        return this.lineJoin;
    }

    @Override
    public void setLineJoin(Expression join) {
        if (join == null) {
            return;
        }
        this.lineJoin = join;
    }

    @Override
    public Expression getOpacity() {
        return this.opacity;
    }

    @Override
    public void setOpacity(Expression level) {
        if (level == null) {
            return;
        }
        this.opacity = level;
    }

    @Override
    public Expression getWidth() {
        return this.width;
    }

    @Override
    public void setWidth(Expression expr) {
        if (expr == null) {
            return;
        }
        this.width = expr;
    }

    public String toString() {
        StringBuffer out = new StringBuffer("org.saig.core.styling.StrokeImpl:\n");
        out.append("\tColor " + this.color + "\n");
        out.append("\tWidth " + this.width + "\n");
        out.append("\tOpacity " + this.opacity + "\n");
        out.append("\tLineCap " + this.lineCap + "\n");
        out.append("\tLineJoin " + this.lineJoin + "\n");
        out.append("\tDash Array " + Arrays.toString(this.dashArray) + "\n");
        out.append("\tDash Offset " + this.dashOffset + "\n");
        out.append("\tFill Graphic " + this.fillGraphic + "\n");
        out.append("\tStroke Graphic " + this.strokeGraphic);
        return out.toString();
    }

    @Override
    public Color getColor(Feature feature) {
        return Color.decode((String)this.getColor().getValue(feature));
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Object clone() {
        StrokeImpl clone = new StrokeImpl();
        clone.setColor(this.getColor());
        clone.setDashOffset(this.getDashOffset());
        clone.setLineCap(this.getLineCap());
        clone.setLineJoin(this.getLineJoin());
        clone.setOpacity(this.getOpacity());
        clone.setWidth(this.getWidth());
        if (this.dashArray != null) {
            float[] cloneDashArray = new float[this.dashArray.length];
            int i = 0;
            while (i < this.dashArray.length) {
                cloneDashArray[i] = this.dashArray[i];
                ++i;
            }
            clone.setDashArray(cloneDashArray);
        }
        if (this.fillGraphic != null) {
            clone.setGraphicFill((Graphic)((Cloneable)this.fillGraphic).clone());
        }
        if (this.strokeGraphic != null) {
            clone.setGraphicStroke((Graphic)((Cloneable)this.strokeGraphic).clone());
        }
        return clone;
    }

    public int hashCode() {
        int PRIME = 1000003;
        int result = 0;
        if (this.color != null) {
            result = 1000003 * result + this.color.hashCode();
        }
        if (this.dashOffset != null) {
            result = 1000003 * result + this.dashOffset.hashCode();
        }
        if (this.fillGraphic != null) {
            result = 1000003 * result + this.fillGraphic.hashCode();
        }
        if (this.strokeGraphic != null) {
            result = 1000003 * result + this.strokeGraphic.hashCode();
        }
        if (this.lineCap != null) {
            result = 1000003 * result + this.lineCap.hashCode();
        }
        if (this.lineJoin != null) {
            result = 1000003 * result + this.lineJoin.hashCode();
        }
        if (this.opacity != null) {
            result = 1000003 * result + this.opacity.hashCode();
        }
        if (this.width != null) {
            result = 1000003 * result + this.width.hashCode();
        }
        if (this.dashArray != null) {
            result = 1000003 * result + this.hashCodeDashArray(this.dashArray);
        }
        return result;
    }

    private int hashCodeDashArray(float[] a) {
        int PRIME = 1000003;
        if (a == null) {
            return 0;
        }
        int result = 0;
        int i = 0;
        while (i < a.length) {
            result = 1000003 * result + Float.floatToIntBits(a[i]);
            ++i;
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
        StrokeImpl other = (StrokeImpl)oth;
        if (this.color == null ? other.color != null : !this.color.equals(other.color)) {
            return false;
        }
        if (this.width == null ? other.width != null : !this.width.equals(other.width)) {
            return false;
        }
        if (this.dashOffset == null ? other.dashOffset != null : !this.dashOffset.equals(other.dashOffset)) {
            return false;
        }
        if (this.lineCap == null ? other.lineCap != null : !this.lineCap.equals(other.lineCap)) {
            return false;
        }
        if (this.lineJoin == null ? other.lineJoin != null : !this.lineJoin.equals(other.lineJoin)) {
            return false;
        }
        if (this.opacity == null ? other.opacity != null : !this.opacity.equals(other.opacity)) {
            return false;
        }
        if (this.fillGraphic == null ? other.fillGraphic != null : !this.fillGraphic.equals(other.fillGraphic)) {
            return false;
        }
        if (this.strokeGraphic == null ? other.strokeGraphic != null : !this.strokeGraphic.equals(other.strokeGraphic)) {
            return false;
        }
        return Arrays.equals(this.dashArray, other.dashArray);
    }
}

