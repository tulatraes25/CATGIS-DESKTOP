/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.expr;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.saig.core.expr.AttributeExpr;
import org.saig.core.expr.AttributeGeometryExpr;
import org.saig.core.expr.AttributeMathExpr;
import org.saig.core.expr.Expr;
import org.saig.core.expr.FidsExpr;
import org.saig.core.expr.FunctionExpr;
import org.saig.core.expr.GeometryExpr;
import org.saig.core.expr.LiteralExpr;
import org.saig.core.expr.LiteralGeometryExpr;
import org.saig.core.expr.LiteralMathExpr;
import org.saig.core.expr.MathExpr;
import org.saig.core.filter.FilterFactory;

public class Exprs {
    protected static FilterFactory factory = FilterFactory.createFilterFactory();

    public static Expr fid(String featureID) {
        HashSet<String> set = new HashSet<String>();
        set.add(featureID);
        return new FidsExpr(set);
    }

    public static Expr fid(Set fids) {
        return new FidsExpr(fids);
    }

    public static Expr and(Expr[] expr) {
        if (expr.length == 0) {
            return Exprs.literal(false);
        }
        if (expr.length == 1) {
            return expr[0];
        }
        Expr e = expr[0];
        int i = 1;
        while (i < expr.length) {
            e = e.and(expr[i]);
            ++i;
        }
        return e;
    }

    public static Expr or(Expr[] expr) {
        if (expr.length == 0) {
            return Exprs.literal(true);
        }
        if (expr.length == 1) {
            return expr[0];
        }
        Expr e = expr[0];
        int i = 1;
        while (i < expr.length) {
            e = e.or(expr[i]);
            ++i;
        }
        return e;
    }

    public static Expr literal(boolean b) {
        return new LiteralExpr(b);
    }

    public static MathExpr literal(int number) {
        return new LiteralMathExpr(number);
    }

    public static MathExpr literal(double number) {
        return new LiteralMathExpr(number);
    }

    public static MathExpr literal(Number number) {
        return new LiteralMathExpr(number);
    }

    public static Expr literal(Object literal) {
        return new LiteralExpr(literal);
    }

    public static GeometryExpr literal(Envelope extent) {
        return new LiteralGeometryExpr(extent);
    }

    public static GeometryExpr literal(Geometry geom) {
        return new LiteralGeometryExpr(geom);
    }

    public static MathExpr add(MathExpr[] expr) {
        if (expr.length == 0) {
            return Exprs.literal(0);
        }
        if (expr.length == 1) {
            return expr[0];
        }
        MathExpr e = expr[0];
        int i = 1;
        while (i < expr.length) {
            e = e.add(expr[i]);
            ++i;
        }
        return e;
    }

    public static MathExpr subtract(MathExpr[] expr) {
        if (expr.length == 0) {
            return Exprs.literal(0);
        }
        if (expr.length == 1) {
            return expr[0];
        }
        MathExpr e = expr[0];
        int i = 1;
        while (i < expr.length) {
            e = e.subtract(expr[i]);
            ++i;
        }
        return e;
    }

    public static MathExpr divide(MathExpr[] expr) {
        if (expr.length == 0) {
            return Exprs.literal(1);
        }
        if (expr.length == 1) {
            return expr[0];
        }
        MathExpr e = expr[0];
        int i = 1;
        while (i < expr.length) {
            e = e.divide(expr[i]);
            ++i;
        }
        return e;
    }

    public static Expr multiply(MathExpr[] expr) {
        if (expr.length == 0) {
            return Exprs.literal(1);
        }
        if (expr.length == 1) {
            return expr[0];
        }
        MathExpr e = expr[0];
        int i = 1;
        while (i < expr.length) {
            e = e.multiply(expr[i]);
            ++i;
        }
        return e;
    }

    public static Expr attribute(String attribute) {
        return new AttributeExpr(attribute);
    }

    public static Expr meta(String xpath) {
        return null;
    }

    public static GeometryExpr geom() {
        return new AttributeGeometryExpr();
    }

    public static GeometryExpr geom(String attribute) {
        return new AttributeGeometryExpr(attribute);
    }

    public static MathExpr math(String attribute) {
        return new AttributeMathExpr(attribute);
    }

    public static Expr fn(String name, Expr expr) {
        return new FunctionExpr(name, expr);
    }

    public static Expr fn(String name, Expr expr1, Expr expr2) {
        return new FunctionExpr(name, expr1, expr2);
    }

    public static Expr fn(String name, Expr[] expr) {
        return new FunctionExpr(name, expr);
    }

    static boolean truth(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean)value;
        }
        if (value instanceof Number) {
            return ((Number)value).doubleValue() != 0.0;
        }
        if (value instanceof String) {
            return ((String)value).length() != 0;
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) != 0;
        }
        if (value instanceof Collection) {
            return !((Collection)value).isEmpty();
        }
        if (value instanceof Map) {
            return !((Map)value).isEmpty();
        }
        if (value instanceof Envelope) {
            return ((Envelope)value).isNull();
        }
        return false;
    }
}

