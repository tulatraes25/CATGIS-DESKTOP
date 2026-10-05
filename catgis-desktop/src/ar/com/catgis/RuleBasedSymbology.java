package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container for rule-based symbology configuration on a Layer.
 * <p>
 * Similar to CategorizedSymbology/GraduatedSymbology but uses expression-based
 * RuleBasedStyleRule instances that can be nested (parent-child hierarchy).
 * <p>
 * Resolution order:
 * <ol>
 *   <li>Evaluate each top-level rule's filter expression against the feature</li>
 *   <li>If a rule matches AND has children, recursively check children</li>
 *   <li>If a rule matches AND has no children (or no child matches), use the rule</li>
 *   <li>If enabled, the ELSE rule (catch-all) matches when no other rule does</li>
 * </ol>
 */
public class RuleBasedSymbology {

    private boolean enabled;
    private String description = "Reglas";
    private final List<RuleBasedStyleRule> rules = new ArrayList<>();

    public RuleBasedSymbology() {
    }

    // ─── Configuration ───────────────────────────────────────────────────

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null && !description.isBlank()
                ? description.trim()
                : "Reglas";
    }

    public boolean isConfigured() {
        return enabled && !rules.isEmpty();
    }

    // ─── Rules management ────────────────────────────────────────────────

    public List<RuleBasedStyleRule> getRules() {
        return rules;
    }

    public List<RuleBasedStyleRule> getRulesView() {
        return Collections.unmodifiableList(rules);
    }

    public void addRule(RuleBasedStyleRule rule) {
        if (rule != null) {
            rules.add(rule);
        }
    }

    public void removeRule(RuleBasedStyleRule rule) {
        rules.remove(rule);
    }

    public void clearRules() {
        rules.clear();
    }

    public RuleBasedStyleRule getRule(int index) {
        if (index >= 0 && index < rules.size()) {
            return rules.get(index);
        }
        return null;
    }

    public int getRuleCount() {
        return rules.size();
    }

    // ─── Resolution ──────────────────────────────────────────────────────

    /**
     * Resolve the best matching rule-based style for the given feature.
     *
     * @param feature the feature to style
     * @param scaleDenominator current map scale denominator (0 = unknown)
     * @return the matching CategoryStyleRule, or null if no rule matches
     */
    public CategoryStyleRule resolve(SimpleFeature feature, double scaleDenominator) {
        if (!isConfigured() || feature == null) return null;
        return resolveRules(rules, feature, scaleDenominator, false);
    }

    /**
     * Resolve using default scale (no scale filtering).
     */
    public CategoryStyleRule resolve(SimpleFeature feature) {
        return resolve(feature, 0);
    }

    /**
     * Recursively resolve rules.
     */
    private CategoryStyleRule resolveRules(List<RuleBasedStyleRule> ruleList,
                                           SimpleFeature feature,
                                           double scaleDenominator,
                                           boolean isChildContext) {
        RuleBasedStyleRule elseRule = null;

        for (RuleBasedStyleRule rule : ruleList) {
            // Skip else rules here, handle at the end
            if (rule.isElseRule()) {
                elseRule = rule;
                continue;
            }

            // Check scale range
            if (scaleDenominator > 0 && !rule.isVisibleAtScale(scaleDenominator)) {
                continue;
            }

            // Check filter expression
            if (!rule.evaluate(feature)) {
                continue;
            }

            // Rule matches! Check children
            if (rule.hasChildren()) {
                CategoryStyleRule childMatch = resolveRules(rule.getChildren(), feature, scaleDenominator, true);
                if (childMatch != null) {
                    return childMatch; // child matched, use child style
                }
                // No child matched — use parent style
            }

            return rule;
        }

        // No rule matched — try else rule
        if (elseRule != null) {
            if (scaleDenominator <= 0 || elseRule.isVisibleAtScale(scaleDenominator)) {
                return elseRule;
            }
        }

        // In child context, no match = fall back to parent
        // In root context, no match = return null (use default style)
        return null;
    }

    /**
     * Count total rules including nested children.
     */
    public int totalRuleCount() {
        int count = 0;
        for (RuleBasedStyleRule rule : rules) {
            count += rule.totalRuleCount();
        }
        return count;
    }

    /**
     * Check if any rule matches the given feature (useful for validation).
     */
    public boolean hasMatchingRule(SimpleFeature feature) {
        return resolve(feature) != null;
    }

    // ─── Internal ────────────────────────────────────────────────────────

}
