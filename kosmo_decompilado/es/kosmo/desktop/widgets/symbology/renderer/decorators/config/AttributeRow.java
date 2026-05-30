/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.renderer.decorators.config;

import java.awt.Color;
import org.saig.core.model.feature.Attribute;
import org.saig.core.util.LocaleManager;

public class AttributeRow {
    private Attribute attr;
    private String fieldName;
    private String label;
    private Color color;

    public AttributeRow(Attribute attr, Color color) {
        this.attr = attr;
        this.fieldName = attr.getName();
        this.label = attr.getTitle(LocaleManager.getActiveLocale());
        this.color = color;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public String getLabel() {
        return this.label;
    }

    public Color getColor() {
        return this.color;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setAttribute(Attribute attr) {
        this.attr = attr;
    }

    public Attribute getAttribute() {
        return this.attr;
    }
}

