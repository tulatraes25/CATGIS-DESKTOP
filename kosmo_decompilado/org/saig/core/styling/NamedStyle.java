/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleVisitor;

public class NamedStyle
implements Style {
    private String name;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void setTitle(String title) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAbstract() {
        return null;
    }

    @Override
    public void setAbstract(String abstractStr) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public void setDefault(boolean isDefault) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FeatureTypeStyle[] getFeatureTypeStyles() {
        return null;
    }

    @Override
    public void setFeatureTypeStyles(FeatureTypeStyle[] types) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addFeatureTypeStyle(FeatureTypeStyle type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void setSelectedFeatureTypeStyle(int pos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FeatureTypeStyle getSelectedFeatureTypeStyle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSelectedFeatureTypeStyle(String featureTypeStyleName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FeatureTypeStyle getFeatureTypeStyle(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSelectedFeatureTypeStyle(FeatureTypeStyle featureTypeStyle) {
        throw new UnsupportedOperationException();
    }
}

