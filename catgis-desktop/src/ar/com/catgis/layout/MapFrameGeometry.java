package ar.com.catgis.layout;

import java.awt.Rectangle;

/**
 * Geometry of a rendered map frame: outer frame bounds, inner image bounds, and ground extent.
 */
public record MapFrameGeometry(Rectangle frameBounds, Rectangle imageBounds, double shownGroundMeters) {
}
