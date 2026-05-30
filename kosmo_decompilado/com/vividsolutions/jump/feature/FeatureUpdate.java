/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.feature;

import com.vividsolutions.jump.feature.Feature;

public class FeatureUpdate {
    private Feature origVersion;
    private Feature newVersion;

    public FeatureUpdate(Feature origVersion, Feature newVersion) {
        this.origVersion = origVersion;
        this.newVersion = newVersion;
    }

    public Feature getOriginal() {
        return this.origVersion;
    }

    public Feature getNew() {
        return this.newVersion;
    }
}

