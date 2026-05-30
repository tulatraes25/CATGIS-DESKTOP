/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.styling.StyleVisitor;

public interface StyledLayer {
    public String getName();

    public void setName(String var1);

    public void accept(StyleVisitor var1);
}

