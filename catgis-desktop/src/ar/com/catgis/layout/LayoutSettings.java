package ar.com.catgis.layout;

import ar.com.catgis.CatmapLayoutItem;

import java.util.List;

/**
 * Immutable settings snapshot for layout composition rendering.
 */
public record LayoutSettings(String title,
                             String subtitle,
                             String footer,
                             String studyName,
                             String cartoucheProjectName,
                             String companyName,
                             String cartographerName,
                             String imageSource,
                             String coordinateReference,
                             String legendTitle,
                             String legendSubtitle,
                             String logoPath,
                             String layoutImagePath,
                             LayoutTemplate template,
                             PageSizePreset pageSize,
                             PageOrientation orientation,
                             int dpi,
                             LegendPlacement legendPlacement,
                             ScaleStyle scaleStyle,
                             ScaleRule scaleRule,
                             NorthStyle northStyle,
                             boolean showNorth,
                             boolean showScale,
                             boolean showLegend,
                             boolean showGrid,
                             int gridColumns,
                             int gridRows,
                             boolean showGridLabels,
                             List<CatmapLayoutItem> catmapItems) {
}
