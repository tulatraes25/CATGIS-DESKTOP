/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.styling.FeatureTypeConstraint;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.StyledLayer;

public interface NamedLayer
extends StyledLayer {
    public FeatureTypeConstraint[] getLayerFeatureConstraints();

    public void setLayerFeatureConstraints(FeatureTypeConstraint[] var1);

    public Style[] getStyles();

    public void addStyle(Style var1);

    @Override
    public void accept(StyleVisitor var1);
}

