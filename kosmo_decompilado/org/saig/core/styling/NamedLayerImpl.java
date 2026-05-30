/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.resources.Utilities
 */
package org.saig.core.styling;

import java.util.ArrayList;
import java.util.List;
import org.geotools.resources.Utilities;
import org.saig.core.styling.FeatureTypeConstraint;
import org.saig.core.styling.NamedLayer;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.StyledLayerImpl;

public class NamedLayerImpl
extends StyledLayerImpl
implements NamedLayer {
    protected List<Style> styles = new ArrayList<Style>();
    FeatureTypeConstraint[] featureTypeConstraints = new FeatureTypeConstraint[0];

    @Override
    public FeatureTypeConstraint[] getLayerFeatureConstraints() {
        return this.featureTypeConstraints;
    }

    @Override
    public void setLayerFeatureConstraints(FeatureTypeConstraint[] featureTypeConstraints) {
        this.featureTypeConstraints = featureTypeConstraints;
    }

    @Override
    public Style[] getStyles() {
        return this.styles.toArray(new Style[0]);
    }

    @Override
    public void addStyle(Style sl) {
        this.styles.add(sl);
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth instanceof NamedLayerImpl) {
            NamedLayerImpl other = (NamedLayerImpl)oth;
            if (!Utilities.equals(this.styles, other.styles)) {
                return false;
            }
            if (this.featureTypeConstraints.length != other.featureTypeConstraints.length) {
                return false;
            }
            int i = 0;
            while (i < this.featureTypeConstraints.length) {
                if (!Utilities.equals((Object)this.featureTypeConstraints[i], (Object)other.featureTypeConstraints[i])) {
                    return false;
                }
                ++i;
            }
            return true;
        }
        return false;
    }
}

