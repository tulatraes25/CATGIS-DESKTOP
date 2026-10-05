package ar.com.catgis.layout;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

/**
 * Complete result of a layout render pass: rendered image, element bounds, and scale.
 */
public record LayoutRenderResult(BufferedImage image,
                                 EnumMap<LayoutElementType, Rectangle> elementBounds,
                                 Map<String, Rectangle> customItemBounds,
                                 double exactScaleDenominator) {
}
