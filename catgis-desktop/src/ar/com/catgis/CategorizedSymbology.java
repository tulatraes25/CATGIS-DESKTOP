package ar.com.catgis;

import java.util.LinkedHashMap;
import java.util.Map;

public class CategorizedSymbology {
    private String fieldName = "";
    private String legendTitle = "Categorias";
    private String legendSubtitle = "Clasificacion por campo";
    private final Map<String, CategoryStyleRule> rules = new LinkedHashMap<>();

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName != null ? fieldName.trim() : "";
    }

    public String getLegendTitle() {
        return legendTitle;
    }

    public void setLegendTitle(String legendTitle) {
        this.legendTitle = legendTitle != null && !legendTitle.isBlank() ? legendTitle.trim() : "Categorias";
    }

    public String getLegendSubtitle() {
        return legendSubtitle;
    }

    public void setLegendSubtitle(String legendSubtitle) {
        this.legendSubtitle = legendSubtitle != null && !legendSubtitle.isBlank()
                ? legendSubtitle.trim()
                : "Clasificacion por campo";
    }

    public Map<String, CategoryStyleRule> getRules() {
        return rules;
    }

    public boolean isConfigured() {
        return fieldName != null && !fieldName.isBlank() && !rules.isEmpty();
    }

    public CategoryStyleRule getRule(String value) {
        return rules.get(valueKey(value));
    }

    public CategoryStyleRule getOrCreateRule(String value) {
        return rules.computeIfAbsent(valueKey(value), key -> new CategoryStyleRule(valueDisplay(value)));
    }

    public void clearRules() {
        rules.clear();
    }

    public static String valueKey(String value) {
        return value == null || value.isBlank() ? "(sin valor)" : value.trim();
    }

    public static String valueDisplay(String value) {
        return valueKey(value);
    }
}
