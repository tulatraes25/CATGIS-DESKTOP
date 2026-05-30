/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.styling.StyleVisitor;

public interface RemoteOWS {
    public String getService();

    public void setService(String var1);

    public String getOnlineResource();

    public void setOnlineResource(String var1);

    public void accept(StyleVisitor var1);
}

