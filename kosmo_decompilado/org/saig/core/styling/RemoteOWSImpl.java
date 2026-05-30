/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.styling.RemoteOWS;
import org.saig.core.styling.StyleVisitor;

public class RemoteOWSImpl
implements RemoteOWS {
    private String service;
    private String onlineResource;

    @Override
    public String getService() {
        return this.service;
    }

    @Override
    public void setService(String service) {
        this.service = service;
    }

    @Override
    public String getOnlineResource() {
        return this.onlineResource;
    }

    @Override
    public void setOnlineResource(String onlineResource) {
        this.onlineResource = onlineResource;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
}

