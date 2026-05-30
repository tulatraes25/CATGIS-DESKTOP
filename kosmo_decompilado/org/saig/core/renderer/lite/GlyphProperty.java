/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.lite;

public class GlyphProperty {
    private String name;
    private Class<?> type;
    private Object value;

    public GlyphProperty(String s, Class<?> c, Object o) {
        this.name = s;
        this.type = c;
        this.value = o;
    }

    public String getName() {
        return this.name;
    }

    public Class<?> getType() {
        return this.type;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object v) {
        this.value = v;
    }
}

