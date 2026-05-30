/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Blackboard
implements Cloneable,
Serializable {
    private static final long serialVersionUID = 6504993615735124204L;
    private HashMap<String, Object> properties = new HashMap();

    public Blackboard put(String key, Object value) {
        this.properties.put(key, value);
        return this;
    }

    public HashMap<String, Object> getProperties() {
        return this.properties;
    }

    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

    public Object get(String key) {
        return this.properties.get(key);
    }

    public Blackboard put(String key, boolean value) {
        this.put(key, new Boolean(value));
        return this;
    }

    public Blackboard putAll(Map<String, Object> properties) {
        this.properties.putAll(properties);
        return this;
    }

    public boolean get(String key, boolean defaultValue) {
        if (this.get(key) == null) {
            this.put(key, defaultValue);
        }
        return this.getBoolean(key);
    }

    public boolean getBoolean(String key) {
        if (this.get(key) == null) {
            this.put(key, new Boolean(false));
        }
        return (Boolean)this.get(key);
    }

    public Blackboard put(String key, int value) {
        this.put(key, new Integer(value));
        return this;
    }

    public Blackboard put(String key, double value) {
        this.put(key, new Double(value));
        return this;
    }

    public double get(String key, double defaultValue) {
        if (this.get(key) == null) {
            this.put(key, defaultValue);
        }
        return this.getDouble(key);
    }

    public int get(String key, int defaultValue) {
        if (this.get(key) == null) {
            this.put(key, defaultValue);
        }
        return this.getInt(key);
    }

    public int getInt(String key) {
        return ((Number)this.get(key)).intValue();
    }

    public double getDouble(String key) {
        return ((Number)this.get(key)).doubleValue();
    }

    public Object get(String key, Object defaultValue) {
        if (this.get(key) == null) {
            this.put(key, defaultValue);
        }
        return this.get(key);
    }

    public Blackboard remove(String key) {
        this.properties.remove(key);
        return this;
    }

    public Object clone() {
        return new Blackboard().putAll(this.properties);
    }
}

