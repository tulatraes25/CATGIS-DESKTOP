/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.print;

import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import java.util.List;
import org.saig.core.filter.Filter;
import org.saig.core.renderer.print.PrintElement;
import org.saig.core.renderer.style.RuleStyle;

public class PrintLayerCache {
    private List<PrintElement> elements;
    private List<RuleStyle> rulesWithFilter;
    private RuleStyle elseRule;
    private List<Style> jumpStyles;
    private boolean repeated = true;
    private boolean overlapping = true;
    private Filter fechaBajaFilter;
    private boolean cad;
    private boolean isLineLayer;
    private double layerWidth;
    private String layerName;

    public PrintLayerCache(String layerName, List<RuleStyle> rulesWithFilter, RuleStyle elseRule, List<PrintElement> elements, boolean repeated, boolean overlappping, List<Style> jumpStyles, Filter fechaBajaFilter, boolean cad, boolean isLineLayer, double layerWidth) {
        this.layerName = layerName;
        this.elements = elements;
        this.rulesWithFilter = rulesWithFilter;
        this.elseRule = elseRule;
        this.repeated = repeated;
        this.jumpStyles = jumpStyles;
        this.overlapping = overlappping;
        this.fechaBajaFilter = fechaBajaFilter;
        this.cad = cad;
        this.isLineLayer = isLineLayer;
        this.layerWidth = layerWidth;
    }

    public List<PrintElement> getElements() {
        return this.elements;
    }

    public RuleStyle getElseRule() {
        return this.elseRule;
    }

    public List<RuleStyle> getRulesWithFilter() {
        return this.rulesWithFilter;
    }

    public boolean isOverlapping() {
        return this.overlapping;
    }

    public boolean isRepeated() {
        return this.repeated;
    }

    public List<Style> getJumpStyles() {
        return this.jumpStyles;
    }

    public Filter getFechaBajaFilter() {
        return this.fechaBajaFilter;
    }

    public boolean isCad() {
        return this.cad;
    }

    public double getLayerWidth() {
        return this.layerWidth;
    }

    public boolean isLineLayer() {
        return this.isLineLayer;
    }

    public String getLayerName() {
        return this.layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }
}

