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
import org.saig.core.styling.Extent;
import org.saig.core.styling.StyleVisitor;

public class ExtentImpl
implements Extent,
Cloneable {
    private String name;
    private String value;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ExtentImpl) {
            ExtentImpl other = (ExtentImpl)obj;
            return Utilities.equals((Object)this.name, (Object)other.name) && Utilities.equals((Object)this.value, (Object)other.value);
        }
        return false;
    }

    public int hashCode() {
        int PRIME = 1000003;
        int result = 0;
        if (this.name != null) {
            result = 1000003 * result + this.name.hashCode();
        }
        if (this.value != null) {
            result = 1000003 * result + this.value.hashCode();
        }
        return result;
    }

    public Object clone() {
        ExtentImpl clone = new ExtentImpl();
        clone.setName(this.name);
        clone.setValue(this.value);
        return clone;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
}

