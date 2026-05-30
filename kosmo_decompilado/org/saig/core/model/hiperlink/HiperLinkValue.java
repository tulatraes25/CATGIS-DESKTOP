/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.hiperlink;

public class HiperLinkValue {
    protected String description;
    protected String value;

    public HiperLinkValue(String description, String value) {
        this.description = description;
        this.value = value;
    }

    public String getDescription() {
        return this.description;
    }

    public String getValue() {
        return this.value;
    }
}

