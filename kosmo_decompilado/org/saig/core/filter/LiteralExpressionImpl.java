/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import org.saig.core.filter.DefaultExpression;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.util.DateFormatManager;

public class LiteralExpressionImpl
extends DefaultExpression
implements LiteralExpression {
    protected Object literal = null;

    public LiteralExpressionImpl() {
    }

    public LiteralExpressionImpl(Object literal) throws IllegalFilterException {
        this.setLiteral(literal);
    }

    public LiteralExpressionImpl(int value) {
        try {
            this.setLiteral(new Integer(value));
        }
        catch (IllegalFilterException ile) {
            throw new AssertionError((Object)"LiteralExpressionImpl is broken, it should accept Integers");
        }
    }

    public LiteralExpressionImpl(double value) {
        try {
            this.setLiteral(new Double(value));
        }
        catch (IllegalFilterException ile) {
            throw new AssertionError((Object)"LiteralExpressionImpl is broken, it should accept Doubles");
        }
    }

    public LiteralExpressionImpl(long value) {
        try {
            this.setLiteral(new Long(value));
        }
        catch (IllegalFilterException ile) {
            throw new AssertionError((Object)"LiteralExpressionImpl is broken, it should accept Doubles");
        }
    }

    public LiteralExpressionImpl(String value) {
        try {
            this.setLiteral(value);
        }
        catch (IllegalFilterException ile) {
            throw new AssertionError((Object)"LiteralExpressionImpl is broken, it should accept Strings");
        }
    }

    @Override
    public short getType() {
        return this.expressionType;
    }

    @Override
    public final void setLiteral(Object literal) throws IllegalFilterException {
        if (literal instanceof Double || literal instanceof BigDecimal) {
            this.expressionType = (short)101;
        } else if (literal instanceof Integer || literal instanceof Long) {
            this.expressionType = (short)102;
        } else if (literal instanceof String || literal instanceof Date || literal instanceof Boolean) {
            this.expressionType = (short)103;
        } else if (literal instanceof Geometry) {
            this.expressionType = (short)104;
        } else {
            throw new IllegalFilterException("Attempted to add a literal with non-supported type (ie. not Double, Integer, String).");
        }
        this.literal = literal instanceof Time || literal instanceof Timestamp ? DateFormatManager.getJDBCTimestampFormatter().format(literal) : (literal instanceof Date ? DateFormatManager.getDateFormat().format(literal) : literal);
    }

    @Override
    public Object getLiteral() {
        return this.literal;
    }

    @Override
    public Object getValue(Feature feature) throws IllegalArgumentException {
        return this.literal;
    }

    public String toString() {
        if (this.literal.equals("")) {
            return "'" + this.literal + "'";
        }
        return this.literal.toString();
    }

    public boolean equals(Object obj) {
        if (obj == null && this.literal == null) {
            return true;
        }
        if (obj instanceof LiteralExpressionImpl) {
            boolean isEqual;
            LiteralExpressionImpl expLit = (LiteralExpressionImpl)obj;
            boolean bl = isEqual = expLit.getType() == this.expressionType;
            if (!isEqual) {
                return false;
            }
            if (this.expressionType == 104) {
                return ((Geometry)this.literal).equals((Geometry)expLit.getLiteral());
            }
            if (this.expressionType == 102) {
                return ((Integer)this.literal).equals(((Number)expLit.getLiteral()).intValue());
            }
            if (this.expressionType == 103) {
                return ((String)this.literal).equals((String)expLit.getLiteral());
            }
            if (this.expressionType == 101) {
                return ((Double)this.literal).equals(((Number)expLit.getLiteral()).doubleValue());
            }
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + (this.literal == null ? 0 : this.literal.hashCode());
        result = 37 * result + this.expressionType;
        return result;
    }

    @Override
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }
}

