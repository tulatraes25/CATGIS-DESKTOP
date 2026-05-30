/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.lite;

import java.util.Vector;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.renderer.lite.GlyphProperty;

public class GlyphPropertiesList {
    private Vector<GlyphProperty> list = new Vector();
    private Vector<String> names = new Vector();
    private FilterFactory factory = FilterFactory.createFilterFactory();

    public void addProperty(String name, Class<?> type, Object value) {
        if (!type.isAssignableFrom(value.getClass())) {
            throw new RuntimeException("Wrong class for setting variable " + name + ". Expected a " + type + " but received a " + value.getClass() + ".");
        }
        this.list.add(new GlyphProperty(name, type, value));
        this.names.add(name);
    }

    public String getPropertyName(int i) {
        return this.list.get(i).getName();
    }

    public int getPropertyIndex(String name) {
        return this.names.indexOf(name);
    }

    public Class<?> getPropertyType(int i) {
        return this.list.get(i).getType();
    }

    public Class<?> getPropertyType(String name) {
        int index = this.names.indexOf(name);
        if (index != -1) {
            return this.getPropertyType(index);
        }
        throw new RuntimeException("Tried to get the class of a non-existent property: " + name);
    }

    public boolean hasProperty(String name) {
        return this.names.contains(name);
    }

    public Object getPropertyValue(int i) {
        return this.list.get(i).getValue();
    }

    public Object getPropertyValue(String name) {
        int index = this.names.indexOf(name);
        if (index != -1) {
            return this.getPropertyValue(index);
        }
        throw new RuntimeException("Tried to get the class of a non-existent property: " + name);
    }

    private Expression stringToLiteral(String s) {
        return this.factory.createLiteralExpression(s);
    }

    private Expression numberToLiteral(Double d) {
        return this.factory.createLiteralExpression(d);
    }

    private Expression numberToLiteral(Integer i) {
        return this.factory.createLiteralExpression(i);
    }

    public void setPropertyValue(String name, int value) {
        this.setPropertyValue(name, new Integer(value));
    }

    public void setPropertyValue(String name, double value) {
        this.setPropertyValue(name, new Double(value));
    }

    public void setPropertyValue(String name, Object value) {
        GlyphProperty prop;
        int index = this.names.indexOf(name);
        if (index != -1) {
            prop = this.list.get(index);
            if (value instanceof String) {
                value = this.stringToLiteral((String)value);
            }
            if (value instanceof Integer) {
                value = this.numberToLiteral((Integer)value);
            }
            if (value instanceof Double) {
                value = this.numberToLiteral((Double)value);
            }
            if (!prop.getType().isAssignableFrom(value.getClass())) {
                throw new RuntimeException("Wrong class for setting variable " + name + ". Expected a " + prop.getType() + " but received a " + value.getClass() + ".");
            }
        } else {
            throw new RuntimeException("Tried to set the value of a non-existent property: " + name);
        }
        prop.setValue(value);
    }
}

