/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.StyledLayer;

public class StyledLayerImpl
implements StyledLayer {
    protected String name;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        if (name == this.name || name != null && name.equals(this.name)) {
            return;
        }
        this.name = name;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
}

