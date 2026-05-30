/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io;

import java.util.Properties;

public class DriverProperties
extends Properties {
    private static final long serialVersionUID = 1L;

    public DriverProperties() {
    }

    public DriverProperties(String defaultValue) {
        this.set("DefaultValue", defaultValue);
    }

    public DriverProperties set(String key, String value) {
        this.setProperty(key, value);
        return this;
    }
}

