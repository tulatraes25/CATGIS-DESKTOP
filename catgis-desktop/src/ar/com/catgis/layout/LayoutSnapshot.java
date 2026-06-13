package ar.com.catgis.layout;

import ar.com.catgis.core.model.Layer;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Frozen snapshot of the map state for layout rendering.
 */
public record LayoutSnapshot(BufferedImage mapImage,
                             List<Layer> visibleLayers,
                             String projectName,
                             String projectCrsLabel,
                             String projectCrsCode,
                             String scaleLabel,
                             double representativeMeters,
                             double baseViewMinX,
                             double baseViewMinY,
                             double baseZoomFactor,
                             int basePixelWidth,
                             int basePixelHeight) {
}
