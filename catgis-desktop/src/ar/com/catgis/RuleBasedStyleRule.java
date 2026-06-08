package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.renderer.labels.LabelExpressionEngine;
import org.geotools.api.feature.simple.SimpleFeature;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A style rule with an expression-based filter for rule-based symbology.
 * <p>
 * Extends CategoryStyleRule to reuse all style properties (colors, sizes, symbol styles)
 * and adds:
 * <ul>
 *   <li>Filter expression evaluated via LabelExpressionEngine</li>
 *   <li>Scale-dependent visibility (min/max scale denominator)</li>
 *   <li>Nested child rules (OR logic: parent must match, then children refine)</li>
 *   <li>Else rule flag (catch-all when no other rules match)</li>
 *   <li>Description for legend display</li>
 * </ul>
 */
public class RuleBasedStyleRule extends CategoryStyleRule {

    private String filterExpression = "";
    private double scaleMin;      // 0 = no limit
    private double scaleMax;      // 0 = no limit
    private boolean elseRule;
    private String description = "";
    private final List<RuleBasedStyleRule> children = new ArrayList<>();

    public RuleBasedStyleRule(String description) {
        super(description != null ? description : "");
        this.description = description != null ? description : "";
    }

    // ─── Filter evaluation ───────────────────────────────────────────────

    /**
     * Evaluate the filter expression against the given feature.
     */
    public boolean evaluate(SimpleFeature feature) {
        if (feature == null) return false;
        if (filterExpression == null || filterExpression.isBlank()) return true; // no filter = always match
        try {
            Object result = LabelExpressionEngine.evaluateRaw(filterExpression, feature);
            if (result instanceof Boolean) return (Boolean) result;
            if (result instanceof Number) return ((Number) result).doubleValue() != 0;
            if (result instanceof String) return !((String) result).isBlank();
            return result != null;
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Scale range ─────────────────────────────────────────────────────

    /**
     * Check whether this rule is visible at the given map scale.
     */
    public boolean isVisibleAtScale(double scaleDenominator) {
        if (scaleMin > 0 && scaleDenominator < scaleMin) return false;
        if (scaleMax > 0 && scaleDenominator > scaleMax) return false;
        return true;
    }

    // ─── Getters / Setters ───────────────────────────────────────────────

    public String getFilterExpression() {
        return filterExpression;
    }

    public void setFilterExpression(String filterExpression) {
        this.filterExpression = filterExpression != null ? filterExpression.trim() : "";
    }

    public double getScaleMin() {
        return scaleMin;
    }

    public void setScaleMin(double scaleMin) {
        this.scaleMin = Math.max(0, scaleMin);
    }

    public double getScaleMax() {
        return scaleMax;
    }

    public void setScaleMax(double scaleMax) {
        this.scaleMax = Math.max(0, scaleMax);
    }

    public boolean isElseRule() {
        return elseRule;
    }

    public void setElseRule(boolean elseRule) {
        this.elseRule = elseRule;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public List<RuleBasedStyleRule> getChildren() {
        return children;
    }

    /**
     * Returns an unmodifiable view of children for external iteration.
     */
    public List<RuleBasedStyleRule> getChildrenView() {
        return Collections.unmodifiableList(children);
    }

    public void addChild(RuleBasedStyleRule child) {
        if (child != null && child != this) {
            children.add(child);
        }
    }

    public void removeChild(RuleBasedStyleRule child) {
        children.remove(child);
    }

    public void clearChildren() {
        children.clear();
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Deep copy of this rule.
     */
    public RuleBasedStyleRule copy() {
        RuleBasedStyleRule copy = new RuleBasedStyleRule(this.description);
        copy.filterExpression = this.filterExpression;
        copy.scaleMin = this.scaleMin;
        copy.scaleMax = this.scaleMax;
        copy.elseRule = this.elseRule;
        copy.setPrimaryColor(this.getPrimaryColor());
        copy.setSecondaryColor(this.getSecondaryColor());
        copy.setLineStyle(this.getLineStyle());
        copy.setLineWidth(this.getLineWidth());
        copy.setPolygonFillStyle(this.getPolygonFillStyle());
        copy.setPointSymbolStyle(this.getPointSymbolStyle());
        copy.setPointSize(this.getPointSize());
        copy.setCatalogSymbolId(this.getCatalogSymbolId());
        for (RuleBasedStyleRule child : children) {
            copy.addChild(child.copy());
        }
        return copy;
    }

    /**
     * Find a child rule by description (first match).
     */
    public RuleBasedStyleRule findChild(String description) {
        if (description == null) return null;
        for (RuleBasedStyleRule child : children) {
            if (description.equals(child.getDescription())) return child;
            RuleBasedStyleRule found = child.findChild(description);
            if (found != null) return found;
        }
        return null;
    }

    /**
     * Recursively count all rules (this + children).
     */
    public int totalRuleCount() {
        int count = 1;
        for (RuleBasedStyleRule child : children) {
            count += child.totalRuleCount();
        }
        return count;
    }
}
