/*
 * Decompiled with CFR 0.152.
 */
package org.deegree.ogcwebservices.getcapabilities;

import java.io.Serializable;

public abstract class OGCCapabilities
implements Serializable {
    private String version;
    private String updateSequence;

    public OGCCapabilities(String version, String updateSequence) {
        this.version = version;
        this.updateSequence = updateSequence;
    }

    public String getUpdateSequence() {
        return this.updateSequence;
    }

    public void setUpdateSequence(String updateSequence) {
        this.updateSequence = updateSequence;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}

