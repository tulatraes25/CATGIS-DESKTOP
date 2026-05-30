/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.resources.Utilities
 */
package org.saig.core.styling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.geotools.resources.Utilities;
import org.saig.core.styling.FeatureTypeConstraint;
import org.saig.core.styling.RemoteOWS;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.StyledLayerImpl;
import org.saig.core.styling.UserLayer;

public class UserLayerImpl
extends StyledLayerImpl
implements UserLayer {
    RemoteOWS remoteOWS;
    List<Style> styles = new ArrayList<Style>();
    FeatureTypeConstraint[] constraints = new FeatureTypeConstraint[0];

    @Override
    public RemoteOWS getRemoteOWS() {
        return this.remoteOWS;
    }

    @Override
    public void setRemoteOWS(RemoteOWS service) {
        this.remoteOWS = service;
    }

    @Override
    public FeatureTypeConstraint[] getLayerFeatureConstraints() {
        return this.constraints;
    }

    @Override
    public void setLayerFeatureConstraints(FeatureTypeConstraint[] constraints) {
        this.constraints = constraints;
    }

    @Override
    public Style[] getUserStyles() {
        return this.styles.toArray(new Style[0]);
    }

    @Override
    public void setUserStyles(Style[] styles) {
        this.styles.clear();
        this.styles.addAll(Arrays.asList(styles));
    }

    @Override
    public void addUserStyle(Style style) {
        this.styles.add(style);
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public boolean equals(Object oth) {
        if (!(oth instanceof UserLayerImpl)) {
            return false;
        }
        UserLayerImpl usl = (UserLayerImpl)oth;
        if (this == usl) {
            return true;
        }
        if (!Utilities.equals((Object)this.remoteOWS, (Object)usl.remoteOWS) && Utilities.equals(this.styles, usl.styles)) {
            return false;
        }
        int length = this.constraints.length;
        if (length != usl.constraints.length) {
            return false;
        }
        int i = 0;
        while (i < length) {
            if (!Utilities.equals((Object)this.constraints[i], (Object)usl.constraints[i])) {
                return false;
            }
            ++i;
        }
        return true;
    }
}

