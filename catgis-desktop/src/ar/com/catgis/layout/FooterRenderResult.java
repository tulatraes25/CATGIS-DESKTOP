package ar.com.catgis.layout;

import java.awt.Rectangle;

/**
 * Result of rendering the layout footer section.
 */
public record FooterRenderResult(Rectangle cartoucheBounds, Rectangle profileImageBounds) {
}
