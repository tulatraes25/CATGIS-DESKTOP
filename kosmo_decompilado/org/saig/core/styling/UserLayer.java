/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.styling.FeatureTypeConstraint;
import org.saig.core.styling.RemoteOWS;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.StyledLayer;

public interface UserLayer
extends StyledLayer {
    public RemoteOWS getRemoteOWS();

    public void setRemoteOWS(RemoteOWS var1);

    public FeatureTypeConstraint[] getLayerFeatureConstraints();

    public void setLayerFeatureConstraints(FeatureTypeConstraint[] var1);

    public Style[] getUserStyles();

    public void setUserStyles(Style[] var1);

    public void addUserStyle(Style var1);

    @Override
    public void accept(StyleVisitor var1);
}

