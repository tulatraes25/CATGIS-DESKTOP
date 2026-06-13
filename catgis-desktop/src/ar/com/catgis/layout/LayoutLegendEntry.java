package ar.com.catgis.layout;

import ar.com.catgis.CategoryStyleRule;
import ar.com.catgis.core.model.Layer;

/**
 * Internal legend entry used during layout legend assembly.
 * Not to be confused with {@link LayoutLegend.LegendItem} which is the public legend model element.
 */
public record LayoutLegendEntry(String key,
                                String label,
                                String subtitle,
                                Layer layer,
                                CategoryStyleRule categoryRule,
                                String geometryType) {
}
