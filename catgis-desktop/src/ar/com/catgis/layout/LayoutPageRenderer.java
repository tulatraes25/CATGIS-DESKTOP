package ar.com.catgis.layout;

import ar.com.catgis.CatmapLayoutItem;
import ar.com.catgis.CatmapLegendItem;
import ar.com.catgis.CatmapLegendSupport;
import ar.com.catgis.CategoryStyleRule;
import ar.com.catgis.CRSDefinitions;
import ar.com.catgis.MapLayoutComposerDialog;
import ar.com.catgis.PointSymbolRenderer;
import ar.com.catgis.PointSymbolCatalog;
import ar.com.catgis.PointGraphicSymbolSupport;
import ar.com.catgis.OnlineTileLayer;
import ar.com.catgis.OnlineWfsLayer;
import ar.com.catgis.RasterLayer;
import ar.com.catgis.PostgisLayer;
import ar.com.catgis.GeoPackageLayer;
import ar.com.catgis.GpxLayer;
import ar.com.catgis.TopographyWorkflowSupport;
import ar.com.catgis.renderer.LineSymbolRenderer;
import ar.com.catgis.data.online.OnlineWmsLayer;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class LayoutPageRenderer {

    private LayoutPageRenderer() {}

    public static BufferedImage render(LayoutSettings settings, LayoutSnapshot snapshot, int width, int height, LayoutInteractionState interactionState, int renderDpi) {
            return renderResult(settings, snapshot, width, height, interactionState, renderDpi).image();
        }

    public static boolean isRenderableElementVisible(LayoutInteractionState interactionState, LayoutElementType type) {
            return !LayoutElementType.isFixed(type) || interactionState == null || interactionState.isElementVisible(type);
        }

    public static LayoutRenderResult renderResult(LayoutSettings settings, LayoutSnapshot snapshot, int width, int height, LayoutInteractionState interactionState, int renderDpi) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            EnumMap<LayoutElementType, Rectangle> elementBounds = new EnumMap<>(LayoutElementType.class);
            java.util.LinkedHashMap<String, Rectangle> customItemBounds = new java.util.LinkedHashMap<>();
            BufferedImage layoutImage = loadImageAsset(settings.layoutImagePath());
            MapFrameGeometry mapFrame = null;
            Graphics2D g2 = image.createGraphics();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, width, height);

                int margin = Math.max(40, width / 36);
                int headerHeight = Math.max(112, height / 8);
                int footerHeight = Math.max(150, height / 6);
                if (settings.template() == LayoutTemplate.CLEAN_CENTERED) {
                    headerHeight = Math.max(MapLayoutComposerDialog.CLEAN_HEADER_MIN_HEIGHT, (int) Math.round(height * MapLayoutComposerDialog.CLEAN_HEADER_HEIGHT_RATIO));
                    footerHeight = Math.max(MapLayoutComposerDialog.CLEAN_FOOTER_MIN_HEIGHT, (int) Math.round(height * MapLayoutComposerDialog.CLEAN_FOOTER_HEIGHT_RATIO));
                } else if (settings.template() == LayoutTemplate.STRONG_CARTOUCHE) {
                    footerHeight = Math.max(180, height / 5);
                } else if (settings.template() == LayoutTemplate.BOTTOM_REFERENCE) {
                    headerHeight = Math.max(108, height / 9);
                }
                if (layoutImage != null) {
                    footerHeight = Math.max(220, footerHeight + 70);
                }
                boolean showHeader = isRenderableElementVisible(interactionState, LayoutElementType.HEADER);
                boolean showMapContent = isRenderableElementVisible(interactionState, LayoutElementType.MAP_CONTENT);
                boolean showNorth = settings.showNorth() && isRenderableElementVisible(interactionState, LayoutElementType.NORTH);
                boolean showScale = settings.showScale() && isRenderableElementVisible(interactionState, LayoutElementType.SCALE);
                boolean showLegend = settings.showLegend() && isRenderableElementVisible(interactionState, LayoutElementType.LEGEND);
                boolean legendOutsideRight = showLegend && settings.legendPlacement() == LegendPlacement.RIGHT_PANEL;
                boolean legendBottom = showLegend && settings.legendPlacement() == LegendPlacement.BOTTOM_PANEL;
                int legendWidth = legendOutsideRight ? Math.max(260, width / 5) : 0;
                int legendHeight = legendBottom ? Math.max(140, height / 5) : 0;
                int gap = showLegend ? Math.max(18, width / 60) : 0;
                int mapX = margin;
                int mapY = margin + headerHeight;
                int mapW = Math.max(200, width - (margin * 2) - legendWidth - gap);
                int mapH = Math.max(220, height - mapY - footerHeight - margin - (legendBottom ? legendHeight + gap : 0));

                Rectangle headerBounds = applyElementAdjustment(new Rectangle(margin, margin, width - (margin * 2), headerHeight - 14), interactionState, LayoutElementType.HEADER);
                if (showHeader) {
                    drawHeader(g2, settings, snapshot, headerBounds);
                    elementBounds.put(LayoutElementType.HEADER, new Rectangle(headerBounds));
                }

                Rectangle requestedMapBounds = applyElementAdjustment(new Rectangle(mapX, mapY, mapW, mapH), interactionState, LayoutElementType.MAP_CONTENT);
                if (!interactionState.hasCustomSize(LayoutElementType.MAP_CONTENT)) {
                    requestedMapBounds = optimizeMapFrame(requestedMapBounds, snapshot.mapImage(), settings.template());
                }
                if (showMapContent) {
                    mapFrame = drawMapFrame(g2, snapshot, requestedMapBounds, interactionState);
                    elementBounds.put(LayoutElementType.MAP_CONTENT, new Rectangle(mapFrame.frameBounds()));
                } else {
                    Rectangle hiddenMapBounds = new Rectangle(requestedMapBounds);
                    mapFrame = new MapFrameGeometry(hiddenMapBounds, hiddenMapBounds, 0d);
                }
                if (showMapContent && settings.showGrid()) {
                    drawGrid(g2, settings, mapFrame);
                }

                if (showNorth) {
                    Rectangle northBounds = applyElementAdjustment(new Rectangle(
                            mapFrame.frameBounds().x + mapFrame.frameBounds().width - 92,
                            mapFrame.frameBounds().y + 20,
                            Math.max(54, width / 22),
                            Math.max(54, width / 22)
                    ), interactionState, LayoutElementType.NORTH);
                    int northSize = Math.max(32, Math.min(northBounds.width, northBounds.height));
                    Rectangle northVisualBounds = new Rectangle(northBounds.x, northBounds.y, northSize, northSize);
                    drawNorthArrow(g2, settings.northStyle(), northVisualBounds.x, northVisualBounds.y, northVisualBounds.width);
                    elementBounds.put(LayoutElementType.NORTH, northVisualBounds);
                }
                if (showScale && showMapContent) {
                    int scaleMaxW = settings.template() == LayoutTemplate.CLEAN_CENTERED
                            ? Math.min(160, mapFrame.frameBounds().width / 5)
                            : Math.min(240, mapFrame.frameBounds().width / 3);
                    Rectangle scaleBounds = applyElementAdjustment(new Rectangle(
                            mapFrame.frameBounds().x + 8,
                            mapFrame.frameBounds().y + mapFrame.frameBounds().height - 74,
                            scaleMaxW,
                            54
                    ), interactionState, LayoutElementType.SCALE);
                    drawScaleBar(g2, settings, snapshot, mapFrame, scaleBounds.x + 14, scaleBounds.y + 18, renderDpi);
                    elementBounds.put(LayoutElementType.SCALE, scaleBounds);
                }
                if (showLegend) {
                    Rectangle legendBounds = resolveLegendBounds(settings, width, margin, gap, legendWidth, legendHeight, mapFrame, mapFrame.frameBounds().y + mapFrame.frameBounds().height);
                    legendBounds = applyElementAdjustment(legendBounds, interactionState, LayoutElementType.LEGEND);
                    drawLegend(g2, settings, snapshot.visibleLayers(), legendBounds.x, legendBounds.y, legendBounds.width, legendBounds.height, interactionState);
                    elementBounds.put(LayoutElementType.LEGEND, legendBounds);
                }

                FooterRenderResult footerResult = drawFooter(g2, settings, snapshot, width, height, margin, footerHeight, mapFrame, interactionState, layoutImage, renderDpi);
                if (footerResult.cartoucheBounds() != null) {
                    elementBounds.put(LayoutElementType.CARTOUCHE, footerResult.cartoucheBounds());
                }
                if (footerResult.profileImageBounds() != null) {
                    elementBounds.put(LayoutElementType.PROFILE_IMAGE, footerResult.profileImageBounds());
                }
                drawCatmapItems(g2, settings.catmapItems(), customItemBounds);
            } finally {
                g2.dispose();
            }
            double exactScaleDenominator = estimateScaleDenominator(mapFrame, renderDpi);
            return new LayoutRenderResult(image, elementBounds, customItemBounds, exactScaleDenominator);
        }

    public static void exportPdf(LayoutSettings settings, LayoutSnapshot snapshot, File file, LayoutInteractionState interactionState) throws Exception {
            try (PDDocument document = new PDDocument()) {
                PDRectangle rectangle = settings.pageSize().toPdfRectangle(settings.orientation());
                PDPage page = new PDPage(rectangle);
                document.addPage(page);

                Dimension size = settings.pageSize().pixelSize(settings.orientation(), settings.dpi());
                BufferedImage layoutArgb = render(settings, snapshot, size.width, size.height, interactionState, settings.dpi());
                BufferedImage layout = new BufferedImage(layoutArgb.getWidth(), layoutArgb.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D rgbG2 = layout.createGraphics();
                try {
                    rgbG2.setColor(Color.WHITE);
                    rgbG2.fillRect(0, 0, layout.getWidth(), layout.getHeight());
                    rgbG2.drawImage(layoutArgb, 0, 0, null);
                } finally {
                    rgbG2.dispose();
                }
                PDImageXObject pdfImage = LosslessFactory.createFromImage(document, layout);

                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.drawImage(pdfImage, 0, 0, rectangle.getWidth(), rectangle.getHeight());
                }
                document.save(file);
            }
        }

    public static Rectangle resolveLegendBounds(LayoutSettings settings, int width, int margin, int gap, int legendWidth, int legendHeight, MapFrameGeometry mapFrame, int bottomY) {
            return switch (settings.legendPlacement()) {
                case BOTTOM_PANEL -> new Rectangle(margin, bottomY + gap, width - (margin * 2), legendHeight);
                case MAP_TOP_RIGHT -> new Rectangle(
                        mapFrame.frameBounds().x + mapFrame.frameBounds().width - Math.max(180, mapFrame.frameBounds().width / 4),
                        mapFrame.frameBounds().y + 18,
                        Math.max(180, mapFrame.frameBounds().width / 4),
                        Math.max(120, mapFrame.frameBounds().height / 4)
                );
                case MAP_BOTTOM_RIGHT -> {
                    int maxW = Math.max(180, mapFrame.frameBounds().width / 4);
                    int maxH = Math.max(120, mapFrame.frameBounds().height / 4);
                    if (settings.template() == LayoutTemplate.CLEAN_CENTERED) {
                        maxW = Math.max(maxW, mapFrame.frameBounds().width / 5);
                        maxH = Math.max(maxH, mapFrame.frameBounds().height / 4);
                    }
                    yield new Rectangle(
                            mapFrame.frameBounds().x + mapFrame.frameBounds().width - maxW - 12,
                            mapFrame.frameBounds().y + mapFrame.frameBounds().height - maxH - 12,
                            maxW,
                            maxH
                    );
                }
                case MAP_BOTTOM_LEFT -> new Rectangle(
                        mapFrame.frameBounds().x + 18,
                        mapFrame.frameBounds().y + mapFrame.frameBounds().height - Math.max(120, mapFrame.frameBounds().height / 4) - 18,
                        Math.max(180, mapFrame.frameBounds().width / 4),
                        Math.max(120, mapFrame.frameBounds().height / 4)
                );
                default -> new Rectangle(
                        mapFrame.frameBounds().x + mapFrame.frameBounds().width + gap,
                        mapFrame.frameBounds().y,
                        legendWidth,
                        mapFrame.frameBounds().height
                );
            };
        }

    public static Rectangle applyOffset(Rectangle source, Point offset) {
            Rectangle result = new Rectangle(source);
            if (offset != null) {
                result.translate(offset.x, offset.y);
            }
            return result;
        }

    public static Rectangle applyElementAdjustment(Rectangle source, LayoutInteractionState interactionState, LayoutElementType type) {
            Rectangle result = new Rectangle(source);
            if (interactionState == null || type == null) {
                return result;
            }
            Point offset = interactionState.getOffset(type);
            Dimension sizeAdjustment = interactionState.getSizeAdjustment(type);
            result.translate(offset.x, offset.y);
            int minWidth = switch (type) {
                case NORTH -> 32;
                case SCALE -> 96;
                case HEADER -> 180;
                case MAP_CONTENT -> 260;
                default -> 80;
            };
            int minHeight = switch (type) {
                case NORTH -> 32;
                case SCALE -> 34;
                case HEADER -> 56;
                case MAP_CONTENT -> 180;
                default -> 60;
            };
            result.width = Math.max(minWidth, result.width + sizeAdjustment.width);
            result.height = Math.max(minHeight, result.height + sizeAdjustment.height);
            return result;
        }

    public static Rectangle optimizeMapFrame(Rectangle availableBounds, BufferedImage mapImage, LayoutTemplate template) {
            if (template == LayoutTemplate.CLEAN_CENTERED) {
                return new Rectangle(availableBounds);
            }
            if (mapImage == null || availableBounds.width <= 0 || availableBounds.height <= 0) {
                return new Rectangle(availableBounds);
            }
            double mapAspect = mapImage.getWidth() / (double) Math.max(1, mapImage.getHeight());
            double availableAspect = availableBounds.getWidth() / Math.max(1d, availableBounds.getHeight());
            Rectangle adjusted = new Rectangle(availableBounds);

            if (Math.abs(mapAspect - availableAspect) < 0.02d) {
                return adjusted;
            }

            if (mapAspect > availableAspect) {
                int targetHeight = Math.max(180, (int) Math.round(adjusted.width / mapAspect));
                if (targetHeight < adjusted.height) {
                    int anchorY = template == LayoutTemplate.CLEAN_CENTERED
                            ? adjusted.y + Math.max(0, (adjusted.height - targetHeight) / 2)
                            : adjusted.y;
                    adjusted = new Rectangle(adjusted.x, anchorY, adjusted.width, targetHeight);
                }
            } else {
                int targetWidth = Math.max(220, (int) Math.round(adjusted.height * mapAspect));
                if (targetWidth < adjusted.width) {
                    adjusted = new Rectangle(
                            adjusted.x + Math.max(0, (adjusted.width - targetWidth) / 2),
                            adjusted.y,
                            targetWidth,
                            adjusted.height
                    );
                }
            }
            return adjusted;
        }

    public static void drawHeader(Graphics2D g2, LayoutSettings settings, LayoutSnapshot snapshot, Rectangle bounds) {
            boolean cleanTemplate = settings.template() == LayoutTemplate.CLEAN_CENTERED;
            int titleFontSize = cleanTemplate ? Math.max(22, bounds.width / 48) : Math.max(28, bounds.width / 34);
            int subtitleFontSize = cleanTemplate ? Math.max(12, bounds.width / 110) : Math.max(15, bounds.width / 90);
            int metaFontSize = cleanTemplate ? Math.max(11, bounds.width / 120) : Math.max(13, bounds.width / 100);
            int titleY = bounds.y + Math.max(22, bounds.height / 5);
            int rowGap = cleanTemplate ? 24 : 30;

            g2.setColor(new Color(27, 38, 56));
            g2.setFont(new Font("SansSerif", Font.BOLD, titleFontSize));
            String title = !settings.title().isBlank() ? settings.title() : snapshot.projectName();
            java.awt.FontMetrics titleMetrics = g2.getFontMetrics();
            if (cleanTemplate && titleMetrics.stringWidth(title) > bounds.width - 10) {
                title = clipText(title, (bounds.width - 10) / Math.max(1, (int) Math.round(titleMetrics.stringWidth("W"))));
            }
            g2.drawString(title, bounds.x, titleY);

            if (cleanTemplate) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, metaFontSize));
                g2.setColor(new Color(105, 114, 126));
                String meta = snapshot.projectName() + " | " + snapshot.projectCrsLabel();
                g2.drawString(meta, bounds.x, titleY + rowGap);
            } else {
                g2.setFont(new Font("SansSerif", Font.PLAIN, subtitleFontSize));
                g2.setColor(new Color(91, 103, 120));
                String subtitle = !settings.subtitle().isBlank() ? settings.subtitle() : "Salida cartografica del proyecto actual";
                g2.drawString(subtitle, bounds.x, titleY + rowGap);

                g2.setFont(new Font("SansSerif", Font.PLAIN, metaFontSize));
                g2.setColor(new Color(105, 114, 126));
                String meta = snapshot.projectName() + " | " + snapshot.projectCrsLabel();
                g2.drawString(meta, bounds.x, titleY + rowGap * 2);
            }

        }

    public static void drawChip(Graphics2D g2, int x, int y, String text) {
            Font chipFont = new Font("SansSerif", Font.BOLD, 11);
            g2.setFont(chipFont);
            java.awt.FontMetrics metrics = g2.getFontMetrics();
            int width = metrics.stringWidth(text) + 18;
            g2.setColor(new Color(239, 244, 251));
            g2.fillRoundRect(x, y, width, 24, 12, 12);
            g2.setColor(new Color(197, 210, 227));
            g2.drawRoundRect(x, y, width, 24, 12, 12);
            g2.setColor(new Color(58, 71, 90));
            g2.drawString(text, x + 9, y + 16);
        }

    public static MapFrameGeometry drawMapFrame(Graphics2D g2, LayoutSnapshot snapshot, Rectangle requestedBounds, LayoutInteractionState interactionState) {
            int x = requestedBounds.x;
            int y = requestedBounds.y;
            int w = requestedBounds.width;
            int h = requestedBounds.height;
            g2.setColor(new Color(255, 255, 255));
            g2.fillRoundRect(x, y, w, h, 12, 12);
            g2.setColor(new Color(180, 190, 204));
            g2.setStroke(new BasicStroke(0.7f));
            g2.drawRoundRect(x, y, w, h, 12, 12);

            int innerPadding = Math.max(12, Math.min(w, h) / 50);
            int contentX = x + innerPadding;
            int contentY = y + innerPadding;
            int contentW = w - (innerPadding * 2);
            int contentH = h - (innerPadding * 2);

            g2.setColor(new Color(255, 255, 255));
            g2.fillRect(contentX, contentY, contentW, contentH);

            BufferedImage mapImage = snapshot != null ? snapshot.mapImage() : null;
            double shownGroundMeters = snapshot != null ? snapshot.representativeMeters() : 0d;
            if (snapshot != null && MapLayoutComposerDialog.ctxMapPanel() != null && snapshot.basePixelWidth() > 0 && snapshot.basePixelHeight() > 0) {
                double zoomMultiplier = interactionState != null ? Math.max(0.02d, interactionState.getMapZoom()) : 1d;
                double targetZoom = Math.max(0.000001d, snapshot.baseZoomFactor() * zoomMultiplier);
                double baseWorldWidth = snapshot.basePixelWidth() / Math.max(snapshot.baseZoomFactor(), 0.000001d);
                double baseWorldHeight = snapshot.basePixelHeight() / Math.max(snapshot.baseZoomFactor(), 0.000001d);
                double baseCenterX = snapshot.baseViewMinX() + (baseWorldWidth / 2d);
                double baseCenterY = snapshot.baseViewMinY() + (baseWorldHeight / 2d);
                double offsetWorldX = (interactionState != null ? interactionState.getMapOffsetX() : 0d) / targetZoom;
                double offsetWorldY = (interactionState != null ? interactionState.getMapOffsetY() : 0d) / targetZoom;
                double currentWorldWidth = Math.max(1d, contentW) / targetZoom;
                double currentWorldHeight = Math.max(1d, contentH) / targetZoom;
                double viewCenterX = baseCenterX - offsetWorldX;
                double viewCenterY = baseCenterY + offsetWorldY;
                double viewMinX = viewCenterX - (currentWorldWidth / 2d);
                double viewMinY = viewCenterY - (currentWorldHeight / 2d);
                BufferedImage rendered = MapLayoutComposerDialog.ctxMapPanel().renderMapViewImage(viewMinX, viewMinY, targetZoom, contentW, contentH);
                if (rendered != null) {
                    mapImage = rendered;
                }
                shownGroundMeters = convertWorldWidthToMeters(snapshot, currentWorldWidth, viewCenterY);
            }
            if (mapImage == null) {
                mapImage = new BufferedImage(Math.max(1, contentW), Math.max(1, contentH), BufferedImage.TYPE_INT_ARGB);
            }
            double visibleGroundMeters = shownGroundMeters;
            Graphics2D imageGraphics = (Graphics2D) g2.create();
            try {
                imageGraphics.setClip(contentX, contentY, contentW, contentH);
                imageGraphics.drawImage(mapImage, contentX, contentY, contentW, contentH, null);
            } finally {
                imageGraphics.dispose();
            }

            g2.setColor(new Color(157, 169, 184));
            g2.drawRect(contentX, contentY, contentW, contentH);

            g2.setColor(new Color(103, 112, 124, 88));
            g2.drawLine(contentX + contentW / 2, contentY, contentX + contentW / 2, contentY + contentH);
            g2.drawLine(contentX, contentY + contentH / 2, contentX + contentW, contentY + contentH / 2);

            return new MapFrameGeometry(new Rectangle(contentX, contentY, contentW, contentH), new Rectangle(contentX, contentY, contentW, contentH), visibleGroundMeters);
        }

    public static void drawGrid(Graphics2D g2, LayoutSettings settings, MapFrameGeometry mapFrame) {
            int cols = Math.max(2, settings.gridColumns());
            int rows = Math.max(2, settings.gridRows());
            Rectangle bounds = mapFrame.imageBounds();
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(new Color(37, 99, 235, 110));
                copy.setStroke(new BasicStroke(1.1f));
                for (int col = 1; col < cols; col++) {
                    int x = bounds.x + (int) Math.round(bounds.width * (col / (double) cols));
                    copy.drawLine(x, bounds.y, x, bounds.y + bounds.height);
                }
                for (int row = 1; row < rows; row++) {
                    int y = bounds.y + (int) Math.round(bounds.height * (row / (double) rows));
                    copy.drawLine(bounds.x, y, bounds.x + bounds.width, y);
                }

                if (settings.showGridLabels()) {
                    copy.setFont(new Font("SansSerif", Font.BOLD, 11));
                    copy.setColor(new Color(29, 78, 216));
                    for (int col = 0; col < cols; col++) {
                        int centerX = bounds.x + (int) Math.round(bounds.width * ((col + 0.5d) / cols));
                        copy.drawString(letterLabel(col + 1), centerX - 4, bounds.y - 6);
                    }
                    for (int row = 0; row < rows; row++) {
                        int centerY = bounds.y + (int) Math.round(bounds.height * ((row + 0.5d) / rows));
                        copy.drawString(String.valueOf(row + 1), bounds.x - 18, centerY + 4);
                    }
                }
            } finally {
                copy.dispose();
            }
        }

    public static String letterLabel(int index) {
            int value = Math.max(1, index);
            StringBuilder label = new StringBuilder();
            while (value > 0) {
                value--;
                label.insert(0, (char) ('A' + (value % 26)));
                value /= 26;
            }
            return label.toString();
        }

    public static void drawNorthArrow(Graphics2D g2, NorthStyle style, int x, int y, int size) {
            paintNorthSymbol(g2, style, x, y, size);
        }

    public static ImageIcon createNorthPreviewIcon(NorthStyle style, int size) {
        int iconSize = Math.max(18, size);
        BufferedImage image = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintNorthSymbol(g2, style, 0, 0, iconSize);
        } finally {
            g2.dispose();
        }
        return new ImageIcon(image);
    }

    public static void paintNorthSymbol(Graphics2D g2, NorthStyle style, int x, int y, int size) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                NorthStyle resolved = style != null ? style : NorthStyle.CLASSIC;
                int centerX = x + size / 2;
                int top = y + 10;
                int bottom = y + size - 12;

                if (resolved == NorthStyle.SIMPLE) {
                    copy.setColor(new Color(255, 255, 255, 220));
                    copy.fillRoundRect(x + 6, y + 4, size - 12, size - 8, 14, 14);
                    copy.setColor(new Color(34, 44, 60));
                    copy.setFont(new Font("SansSerif", Font.BOLD, Math.max(14, size / 3)));
                    copy.drawString("N", centerX - 5, y + 18);
                    copy.setStroke(new BasicStroke(2f));
                    copy.drawLine(centerX, y + 22, centerX, bottom);
                    copy.drawLine(centerX, y + 22, centerX - 8, y + 34);
                    copy.drawLine(centerX, y + 22, centerX + 8, y + 34);
                    return;
                }

                if (resolved == NorthStyle.TECHNICAL) {
                    copy.setColor(new Color(255, 255, 255, 232));
                    copy.fillRoundRect(x + 3, y + 3, size - 6, size - 6, 12, 12);
                    copy.setColor(new Color(196, 205, 216));
                    copy.drawRoundRect(x + 3, y + 3, size - 6, size - 6, 12, 12);
                    copy.setColor(new Color(31, 41, 55));
                    copy.setStroke(new BasicStroke(Math.max(1.8f, size / 20f)));
                    copy.drawLine(centerX, y + 12, centerX, bottom - 2);
                    copy.drawLine(x + 14, y + size / 2, x + size - 14, y + size / 2);
                    Path2D head = new Path2D.Double();
                    head.moveTo(centerX, y + 8);
                    head.lineTo(centerX + size / 9d, y + size / 3d);
                    head.lineTo(centerX - size / 9d, y + size / 3d);
                    head.closePath();
                    copy.setColor(new Color(14, 116, 144));
                    copy.fill(head);
                    copy.setColor(new Color(31, 41, 55));
                    copy.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, size / 4)));
                    copy.drawString("N", centerX - 5, y + 18);
                    return;
                }

                copy.setColor(new Color(255, 255, 255, 226));
                copy.fill(new Ellipse2D.Double(x, y, size, size));
                copy.setColor(new Color(207, 214, 224));
                copy.draw(new Ellipse2D.Double(x, y, size, size));

                if (resolved == NorthStyle.ROSE) {
                    copy.setColor(new Color(241, 245, 249));
                    copy.fill(new Ellipse2D.Double(x + size * 0.16, y + size * 0.16, size * 0.68, size * 0.68));
                    copy.setColor(new Color(207, 214, 224));
                    copy.draw(new Ellipse2D.Double(x + size * 0.16, y + size * 0.16, size * 0.68, size * 0.68));
                    Path2D northNeedle = new Path2D.Double();
                    northNeedle.moveTo(centerX, top);
                    northNeedle.lineTo(centerX + size / 9d, centerX);
                    northNeedle.lineTo(centerX, bottom - size / 4d);
                    northNeedle.lineTo(centerX - size / 9d, centerX);
                    northNeedle.closePath();
                    Path2D southNeedle = new Path2D.Double();
                    southNeedle.moveTo(centerX, bottom);
                    southNeedle.lineTo(centerX + size / 10d, centerX);
                    southNeedle.lineTo(centerX, top + size / 3d);
                    southNeedle.lineTo(centerX - size / 10d, centerX);
                    southNeedle.closePath();
                    copy.setColor(new Color(15, 23, 42));
                    copy.fill(northNeedle);
                    copy.setColor(new Color(245, 158, 11));
                    copy.fill(southNeedle);
                    copy.setColor(new Color(14, 116, 144));
                    copy.fillPolygon(
                            new int[]{centerX, x + size - 12, centerX, x + 12},
                            new int[]{y + size / 2, y + size / 2, y + size / 2 + 6, y + size / 2},
                            4
                    );
                    copy.setColor(new Color(28, 38, 54));
                    copy.setFont(new Font("SansSerif", Font.BOLD, Math.max(14, size / 3)));
                    copy.drawString("N", centerX - 6, top - 2);
                    return;
                }

                Path2D arrow = new Path2D.Double();
                arrow.moveTo(centerX, top);
                arrow.lineTo(centerX + size / 7d, bottom - size / 5d);
                arrow.lineTo(centerX, bottom - size / 3d);
                arrow.lineTo(centerX - size / 7d, bottom - size / 5d);
                arrow.closePath();

                if (resolved == NorthStyle.MODERN) {
                    copy.setColor(new Color(15, 23, 42));
                    copy.fill(arrow);
                    copy.setColor(new Color(96, 165, 250));
                    copy.fillPolygon(new int[]{centerX, centerX + 6, centerX}, new int[]{top + 6, bottom - 10, bottom - 18}, 3);
                } else {
                    copy.setPaint(new GradientPaint(x, y, new Color(22, 94, 188), x + size, y + size, new Color(8, 54, 117)));
                    copy.fill(arrow);
                }
                copy.setColor(new Color(28, 38, 54));
                copy.setFont(new Font("SansSerif", Font.BOLD, Math.max(14, size / 3)));
                copy.drawString("N", centerX - 6, top - 2);
            } finally {
                copy.dispose();
            }
    }

    public static void drawScaleBar(Graphics2D g2, LayoutSettings settings, LayoutSnapshot snapshot, MapFrameGeometry mapFrame, int x, int y, int renderDpi) {
            if (mapFrame.shownGroundMeters() <= 0 || mapFrame.imageBounds().width <= 0) {
                return;
            }

            double metersPerPixel = mapFrame.shownGroundMeters() / Math.max(1d, mapFrame.imageBounds().width);
            double maxMeters = metersPerPixel * Math.min(drawScaleBarMaxMetricMeters(mapFrame), mapFrame.imageBounds().width * 0.28d);
            double roundedMeters = settings.scaleRule().roundValue(maxMeters);
            int barWidth = (int) Math.max(72, Math.round(roundedMeters / metersPerPixel));
            int maxBarPx = (int) (mapFrame.imageBounds().width * 0.30d);
            barWidth = Math.min(barWidth, maxBarPx);
            int segmentCount = barWidth >= 160 ? 4 : 2;
            int segmentWidth = Math.max(1, barWidth / segmentCount);

            double exactDenominator = estimateScaleDenominator(mapFrame, renderDpi);
            String scaleText = exactDenominator > 0
                    ? MapLayoutComposerDialog.formatScaleDenominator(exactDenominator)
                    : snapshot.scaleLabel();

            if (settings.scaleStyle() == ScaleStyle.NUMERIC) {
                g2.setColor(new Color(255, 255, 255, 218));
                g2.fillRoundRect(x - 12, y - 26, 138, 36, 14, 14);
                g2.setColor(new Color(70, 80, 96));
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2.drawString(scaleText, x, y - 3);
                return;
            }

            g2.setColor(new Color(255, 255, 255, 218));
            g2.fillRoundRect(x - 12, y - 34, barWidth + 110, 50, 14, 14);
            g2.setColor(new Color(70, 80, 96));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(scaleText, x, y - 14);

            if (settings.scaleStyle() == ScaleStyle.SIMPLE_BAR) {
                g2.setColor(Color.BLACK);
                g2.fillRect(x, y + 2, barWidth, 7);
                g2.drawString("0", x - 4, y + 28);
                g2.drawString(MapLayoutComposerDialog.formatDistance(roundedMeters), x + barWidth - 12, y + 28);
                return;
            }

            for (int i = 0; i < segmentCount; i++) {
                g2.setColor(i % 2 == 0 ? Color.BLACK : Color.WHITE);
                int segmentX = x + (i * segmentWidth);
                int width = i == segmentCount - 1 ? barWidth - (segmentWidth * i) : segmentWidth;
                g2.fillRect(segmentX, y, width, 12);
                g2.setColor(Color.BLACK);
                g2.drawRect(segmentX, y, width, 12);
            }
            g2.drawString("0", x - 4, y + 28);
            g2.drawString(MapLayoutComposerDialog.formatDistance(roundedMeters), x + barWidth - 12, y + 28);
        }

    public static double drawScaleBarMaxMetricMeters(MapFrameGeometry mapFrame) {
            if (mapFrame == null || mapFrame.imageBounds().width <= 0) {
                return 120d;
            }
            double aspectRatio = mapFrame.imageBounds().width / Math.max(1d, mapFrame.imageBounds().height);
            if (aspectRatio > 1.6d) {
                return 96d;
            }
            if (aspectRatio < 1.1d) {
                return 150d;
            }
            return 120d;
        }

    public static void drawLegend(Graphics2D g2, LayoutSettings settings, List<Layer> layers, int x, int y, int width, int height, LayoutInteractionState interactionState) {
            List<LayoutLegendEntry> items = buildLegendItems(layers);

            boolean userResized = interactionState != null && !interactionState.getSizeAdjustment(LayoutElementType.LEGEND).equals(new java.awt.Dimension(0, 0));

            int fontSizeTitle;
            int fontSizeSub;
            int fontSizeItem;
            int itemH;
            int headerH;
            int padBottom;
            int padX;

            if (userResized) {
                fontSizeTitle = Math.max(12, Math.min(22, height / 8));
                fontSizeSub = Math.max(10, Math.min(16, height / 12));
                fontSizeItem = Math.max(10, Math.min(16, height / 12));
                padX = Math.max(10, Math.min(20, width / 18));
                headerH = Math.max(40, height / 4);
                itemH = Math.max(22, Math.min(40, (height - headerH - 14) / Math.max(1, items.size())));
                padBottom = Math.max(10, height / 16);
            } else {
                fontSizeTitle = 14;
                fontSizeSub = 12;
                fontSizeItem = 12;
                padX = 14;
                headerH = 56;
                itemH = 28;
                padBottom = 16;
                int needed = headerH + (items.size() * itemH) + padBottom;
                if (needed < height) {
                    int diff = height - needed;
                    y += diff / 2;
                    height = needed;
                }
            }

            if (items.isEmpty()) {
                height = Math.max(56, headerH + padBottom);
            }

            g2.setColor(new Color(250, 252, 255));
            g2.fillRoundRect(x, y, width, height, 14, 14);
            g2.setColor(new Color(210, 216, 224));
            g2.drawRoundRect(x, y, width, height, 14, 14);

            g2.setColor(new Color(26, 36, 52));
            g2.setFont(new Font("SansSerif", Font.BOLD, fontSizeTitle));
            String legendTitle = !settings.legendTitle().isBlank() ? settings.legendTitle() : "Referencias";
            g2.drawString(legendTitle, x + padX, y + fontSizeTitle + 6);

            g2.setFont(new Font("SansSerif", Font.PLAIN, fontSizeSub));
            g2.setColor(new Color(103, 114, 128));
            String legendSubtitle = !settings.legendSubtitle().isBlank() ? settings.legendSubtitle() : "Capas del mapa";
            g2.drawString(legendSubtitle, x + padX, y + fontSizeTitle + fontSizeSub + 12);

            if (items.isEmpty()) {
                g2.drawString("Sin capas para mostrar.", x + padX, y + headerH);
                return;
            }

            int itemY = y + headerH;
            int count = 0;
            for (LayoutLegendEntry item : items) {
                if (itemY + itemH > y + height - padBottom) {
                    int remaining = Math.max(0, items.size() - count);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, fontSizeSub));
                    g2.setColor(new Color(108, 116, 128));
                    g2.drawString("+" + remaining + " mas", x + padX, y + height - 6);
                    break;
                }
                drawLegendItemScaled(g2, item, x + padX, itemY, width - (padX * 2), fontSizeItem, userResized);
                itemY += itemH;
                count++;
            }
        }

    public static void drawLegendItemScaled(Graphics2D g2, LayoutLegendEntry item, int x, int y, int availableWidth, int fontSize, boolean scaled) {
            Layer layer = item.layer();
            ShapefileData data = MapLayoutComposerDialog.ctxMapPanel() != null ? MapLayoutComposerDialog.ctxMapPanel().getShapefileData(layer) : null;
            String geometryFamily = VectorLayerUtils.resolveGeometryFamily(data);

            int symSize = scaled ? Math.max(12, fontSize + 4) : 20;

            if (layer instanceof RasterLayer || layer instanceof OnlineTileLayer || layer instanceof OnlineWmsLayer) {
                g2.setPaint(new GradientPaint(x, y, new Color(96, 165, 250), x + symSize, y + symSize, new Color(59, 130, 246)));
                g2.fillRect(x, y - symSize/2, symSize, symSize/2 + 4);
                g2.setColor(new Color(30, 41, 59));
                g2.drawRect(x, y - symSize/2, symSize, symSize/2 + 4);
            } else if ("POINT".equalsIgnoreCase(item.geometryType())) {
                drawPointSymbolPreview(g2, layer, x, y, item.categoryRule());
            } else if ("LINE".equalsIgnoreCase(item.geometryType())) {
                drawLineSymbolPreview(g2, layer, x, y, item.categoryRule());
            } else if ("POLYGON".equalsIgnoreCase(item.geometryType())) {
                drawPolygonSymbolPreview(g2, layer, x, y, item.categoryRule());
            } else if ("POINT".equalsIgnoreCase(geometryFamily)) {
                drawPointSymbolPreview(g2, layer, x, y, null);
            } else if ("LINE".equalsIgnoreCase(geometryFamily)) {
                drawLineSymbolPreview(g2, layer, x, y, null);
            } else {
                drawPolygonSymbolPreview(g2, layer, x, y, null);
            }

            g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
            g2.setColor(new Color(37, 45, 58));
            String name = item.label() != null ? item.label() : "Capa";
            int maxChars = Math.max(8, (availableWidth - 30) / (fontSize / 2));
            if (name.length() > maxChars) {
                name = name.substring(0, Math.max(1, maxChars - 3)) + "...";
            }
            g2.drawString(name, x + 32, y + (fontSize / 3));

            g2.setFont(new Font("SansSerif", Font.PLAIN, Math.max(9, fontSize - 1)));
            g2.setColor(new Color(107, 114, 128));
            String detail = item.subtitle() != null ? item.subtitle() : layerTypeLabel(layer);
            if (detail.length() > maxChars) {
                detail = detail.substring(0, Math.max(1, maxChars - 3)) + "...";
            }
            g2.drawString(detail, x + 32, y + fontSize + 2);
        }

    public static List<LayoutLegendEntry> buildLegendItems(List<Layer> layers) {
            List<LayoutLegendEntry> automaticItems = buildAutomaticLegendItems(layers);
            if (automaticItems.isEmpty()) {
                return automaticItems;
            }

            List<CatmapLegendItem> automaticEntries = new ArrayList<>();
            java.util.Map<String, LayoutLegendEntry> automaticByKey = new java.util.LinkedHashMap<>();
            for (LayoutLegendEntry item : automaticItems) {
                automaticEntries.add(new CatmapLegendItem(
                        item.key(),
                        item.label(),
                        item.subtitle(),
                        CatmapLegendSupport.isLegendVisibleByDefault(item.layer())
                ));
                automaticByKey.put(item.key(), item);
            }

            List<CatmapLegendItem> configuredEntries = CatmapLegendSupport.mergeEntries(
                    automaticEntries,
                    MapLayoutComposerDialog.ctxProject() != null ? MapLayoutComposerDialog.ctxProject().getCatmapLegendItems() : null
            );

            List<LayoutLegendEntry> mergedItems = new ArrayList<>();
            for (CatmapLegendItem configured : configuredEntries) {
                if (configured == null || !configured.isVisible()) {
                    continue;
                }
                LayoutLegendEntry automatic = automaticByKey.get(configured.getKey());
                if (automatic == null) {
                    continue;
                }
                mergedItems.add(new LayoutLegendEntry(
                        automatic.key(),
                        !configured.getLabel().isBlank() ? configured.getLabel() : automatic.label(),
                        !configured.getSubtitle().isBlank() ? configured.getSubtitle() : automatic.subtitle(),
                        automatic.layer(),
                        automatic.categoryRule(),
                        automatic.geometryType()
                ));
            }
            return mergedItems;
        }

    public static List<LayoutLegendEntry> buildAutomaticLegendItems(List<Layer> layers) {
            List<LayoutLegendEntry> items = new ArrayList<>();
            if (layers == null) {
                return items;
            }
            for (Layer layer : layers) {
                if (layer == null) {
                    continue;
                }
                if (layer.getPointCategorizedSymbology().isConfigured()) {
                    for (CategoryStyleRule rule : layer.getPointCategorizedSymbology().getRules().values()) {
                        items.add(new LayoutLegendEntry(
                                CatmapLegendSupport.buildKey(layer, rule, "POINT"),
                                rule.getValue(),
                                layer.getName(),
                                layer,
                                rule,
                                "POINT"
                        ));
                    }
                    continue;
                }
                if (layer.getLineCategorizedSymbology().isConfigured()) {
                    for (CategoryStyleRule rule : layer.getLineCategorizedSymbology().getRules().values()) {
                        items.add(new LayoutLegendEntry(
                                CatmapLegendSupport.buildKey(layer, rule, "LINE"),
                                rule.getValue(),
                                layer.getName(),
                                layer,
                                rule,
                                "LINE"
                        ));
                    }
                    continue;
                }
                if (layer.getPolygonCategorizedSymbology().isConfigured()) {
                    for (CategoryStyleRule rule : layer.getPolygonCategorizedSymbology().getRules().values()) {
                        items.add(new LayoutLegendEntry(
                                CatmapLegendSupport.buildKey(layer, rule, "POLYGON"),
                                rule.getValue(),
                                layer.getName(),
                                layer,
                                rule,
                                "POLYGON"
                        ));
                    }
                    continue;
                }
                String geometryType = CatmapLegendSupport.resolveLegendGeometryType(layer);
                items.add(new LayoutLegendEntry(
                        CatmapLegendSupport.buildKey(layer, null, geometryType),
                        layer.getName(),
                        CatmapLegendSupport.resolveLayerTypeLabel(layer),
                        layer,
                        null,
                        geometryType
                ));
            }
            return items;
        }

    public static void drawLegendItem(Graphics2D g2, LayoutLegendEntry item, int x, int y, int availableWidth) {
            Layer layer = item.layer();
            ShapefileData data = MapLayoutComposerDialog.ctxMapPanel() != null ? MapLayoutComposerDialog.ctxMapPanel().getShapefileData(layer) : null;
            String geometryFamily = VectorLayerUtils.resolveGeometryFamily(data);
            if (layer instanceof RasterLayer || layer instanceof OnlineTileLayer || layer instanceof OnlineWmsLayer) {
                g2.setPaint(new GradientPaint(x, y, new Color(96, 165, 250), x + 18, y + 18, new Color(59, 130, 246)));
                g2.fillRect(x, y - 12, 20, 16);
                g2.setColor(new Color(30, 41, 59));
                g2.drawRect(x, y - 12, 20, 16);
            } else if ("POINT".equalsIgnoreCase(item.geometryType())) {
                drawPointSymbolPreview(g2, layer, x, y, item.categoryRule());
            } else if ("LINE".equalsIgnoreCase(item.geometryType())) {
                drawLineSymbolPreview(g2, layer, x, y, item.categoryRule());
            } else if ("POLYGON".equalsIgnoreCase(item.geometryType())) {
                drawPolygonSymbolPreview(g2, layer, x, y, item.categoryRule());
            } else if ("POINT".equalsIgnoreCase(geometryFamily)) {
                drawPointSymbolPreview(g2, layer, x, y, null);
            } else if ("LINE".equalsIgnoreCase(geometryFamily)) {
                drawLineSymbolPreview(g2, layer, x, y, null);
            } else {
                drawPolygonSymbolPreview(g2, layer, x, y, null);
            }

            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(new Color(37, 45, 58));
            String name = item.label() != null ? item.label() : "Capa";
            int labelWidth = Math.max(60, availableWidth - 30);
            if (name.length() > 34) {
                name = name.substring(0, 31) + "...";
            }
            g2.drawString(name, x + 30, y - 1);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(107, 114, 128));
            String detail = item.subtitle() != null ? item.subtitle() : layerTypeLabel(layer);
            if (detail.length() > labelWidth / 6) {
                detail = detail.substring(0, Math.max(0, labelWidth / 6 - 3)) + "...";
            }
            g2.drawString(detail, x + 30, y + 13);
        }

    public static FooterRenderResult drawFooter(Graphics2D g2, LayoutSettings settings, LayoutSnapshot snapshot, int width, int height, int margin, int footerHeight, MapFrameGeometry mapFrame, LayoutInteractionState interactionState, BufferedImage layoutImage, int renderDpi) {
            int top = height - margin - footerHeight;
            boolean showCartouche = isRenderableElementVisible(interactionState, LayoutElementType.CARTOUCHE);
            boolean showProfileImage = layoutImage != null && isRenderableElementVisible(interactionState, LayoutElementType.PROFILE_IMAGE);

            boolean cleanTemplate = settings.template() == LayoutTemplate.CLEAN_CENTERED;

            if (showCartouche || showProfileImage) {
                g2.setColor(new Color(200, 208, 218));
                g2.setStroke(new BasicStroke(0.5f));
                g2.drawLine(margin, top, width - margin, top);
                g2.setStroke(new BasicStroke(1.0f));
            }

            if (cleanTemplate && showCartouche) {
                java.awt.FontMetrics baseMetrics = g2.getFontMetrics(g2.getFont().deriveFont(Font.PLAIN, Math.max(11, width / 130)));
                int lineHeight = baseMetrics.getHeight() + 2;

                int logoAreaW = 0;
                BufferedImage logoImage = loadImageAsset(settings.logoPath());
                if (logoImage != null) {
                    int logoMaxH = footerHeight - 18;
                    double s = Math.min(1d, logoMaxH / (double) Math.max(1, logoImage.getHeight()));
                    int logoW = Math.max(1, (int) Math.round(logoImage.getWidth() * s));
                    int logoH = Math.max(1, (int) Math.round(logoImage.getHeight() * s));
                    int logoX = width - margin - logoW;
                    int logoY = top + (footerHeight - logoH) / 2;
                    g2.drawImage(logoImage, logoX, logoY, logoW, logoH, null);
                    logoAreaW = logoW + 14;
                }

                g2.setColor(new Color(27, 38, 56));
                g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, width / 120)));
                g2.drawString("Datos cartograficos", margin, top + 18);

                g2.setFont(new Font("SansSerif", Font.PLAIN, Math.max(11, width / 130)));
                int rowY = top + 20 + lineHeight;

                int col1x = margin;
                int availW = (width - (margin * 2) - logoAreaW);
                int colW3 = availW / 3;
                int col2x = margin + colW3;
                int col3x = margin + colW3 * 2;

                String projName = blankOr(settings.cartoucheProjectName(), snapshot.projectName());
                drawCompactFooterRow(g2, "Estudio", blankOr(settings.studyName(), projName), col1x, rowY, colW3 - 6);
                drawCompactFooterRow(g2, "Proyecto", projName, col2x, rowY, colW3 - 6);
                String genText = "Fecha: " + MapLayoutComposerDialog.FOOTER_DATE.format(LocalDateTime.now());
                drawCompactFooterRow(g2, genText, "", col3x, rowY, colW3 - 6);
                rowY += lineHeight + 2;

                drawCompactFooterRow(g2, "Empresa", blankOr(settings.companyName(), "No especificada"), col1x, rowY, colW3 - 6);
                drawCompactFooterRow(g2, "Cartografo", blankOr(settings.cartographerName(), "No especificado"), col2x, rowY, colW3 - 6);
                double exactDenominator = estimateScaleDenominator(mapFrame, renderDpi);
                String scaleText = settings.showScale()
                        ? "Escala: " + (exactDenominator > 0 ? MapLayoutComposerDialog.formatScaleDenominator(exactDenominator) : snapshot.scaleLabel())
                        : "Escala: â€”";
                drawCompactFooterRow(g2, scaleText, "", col3x, rowY, colW3 - 6);
                rowY += lineHeight + 2;

                drawCompactFooterRow(g2, "Fuente", blankOr(settings.imageSource(), "Vista actual del proyecto"), col1x, rowY, colW3 - 6);
                drawCompactFooterRow(g2, "CRS", blankOr(settings.coordinateReference(), snapshot.projectCrsLabel()), col2x, rowY, colW3 - 6);
                drawCompactFooterRow(g2, "Generado en CATGIS Desktop", "", col3x, rowY, colW3 - 6);

                return new FooterRenderResult(
                        new Rectangle(margin, top + 4, width - (margin * 2), footerHeight - 12),
                        null
                );
            }

            int baseCartoucheWidth = switch (settings.template()) {
                case CLEAN_CENTERED -> Math.min(width / 2, 420);
                case STRONG_CARTOUCHE -> Math.min((int) (width * 0.58d), 660);
                default -> Math.min(width / 2, 520);
            };
            Rectangle cartoucheBounds = showCartouche ? applyElementAdjustment(
                    new Rectangle(margin, top + 14, baseCartoucheWidth, footerHeight - 24),
                    interactionState,
                    LayoutElementType.CARTOUCHE
            ) : null;
            if (showCartouche) {
                drawCartouche(g2, settings, snapshot, cartoucheBounds);
            }

            int infoX = cartoucheBounds != null ? cartoucheBounds.x + cartoucheBounds.width + 26 : margin;
            if (showCartouche) {
                g2.setColor(new Color(37, 45, 58));
                g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, width / 108)));
                String footer = !settings.footer().isBlank() ? settings.footer() : "Generado desde CATGIS Desktop";
                g2.drawString(footer, infoX, top + 34);

                g2.setFont(new Font("SansSerif", Font.PLAIN, Math.max(12, width / 115)));
                g2.setColor(new Color(99, 110, 124));
                String reference = "Proyecto: " + snapshot.projectName() + " | CRS: " + snapshot.projectCrsLabel();
                g2.drawString(reference, infoX, top + 54);

                java.awt.FontMetrics metrics = g2.getFontMetrics();
                String generation = "Fecha de salida: " + MapLayoutComposerDialog.FOOTER_DATE.format(LocalDateTime.now());
                g2.drawString(generation, width - margin - metrics.stringWidth(generation), top + 34);

                double exactDenominator = estimateScaleDenominator(mapFrame, renderDpi);
                String scale = settings.showScale()
                        ? "Escala tecnica: " + (exactDenominator > 0 ? MapLayoutComposerDialog.formatScaleDenominator(exactDenominator) : snapshot.scaleLabel())
                        : "Escala grafica oculta";
                g2.drawString(scale, width - margin - metrics.stringWidth(scale), top + 54);
            }
            Rectangle profileImageBounds = null;
            if (showProfileImage) {
                Rectangle baseImageBounds = new Rectangle(
                        infoX,
                        top + 86,
                        Math.max(200, width - margin - infoX),
                        Math.max(110, footerHeight - 104)
                );
                profileImageBounds = applyElementAdjustment(baseImageBounds, interactionState, LayoutElementType.PROFILE_IMAGE);
                drawLayoutImage(g2, profileImageBounds, layoutImage);
            }
            return new FooterRenderResult(cartoucheBounds, profileImageBounds);
        }

    public static void drawCompactFooterRow(Graphics2D g2, String label, String value, int x, int y, int maxWidth) {
            g2.setColor(new Color(58, 68, 84));
            g2.setFont(g2.getFont().deriveFont(Font.BOLD));
            String line;
            if (value != null && !value.isBlank()) {
                line = label + ": " + clipText(value, Math.max(8, maxWidth / 8));
            } else {
                line = label;
            }
            java.awt.FontMetrics fm = g2.getFontMetrics();
            if (fm.stringWidth(line) > maxWidth) {
                line = clipText(line, Math.max(4, (int) Math.round(maxWidth / (fm.stringWidth("W") + 1))));
            }
            g2.drawString(line, x, y + fm.getAscent());
        }

    public static void drawCartouche(Graphics2D g2, LayoutSettings settings, LayoutSnapshot snapshot, Rectangle bounds) {
            g2.setColor(new Color(248, 250, 253));
            g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 18, 18);
            g2.setColor(new Color(201, 210, 222));
            g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 18, 18);

            java.awt.Shape clip = g2.getClip();
            g2.clip(new java.awt.geom.RoundRectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height, 18, 18));

            int contentX = bounds.x + 16;
            int contentY = bounds.y + 18;
            int textX = contentX;
            BufferedImage logoImage = loadImageAsset(settings.logoPath());
            if (logoImage != null) {
                int logoBoxW = 84;
                int logoBoxH = Math.min(bounds.height - 28, 72);
                int logoX = contentX;
                int logoY = bounds.y + 16;
                double scale = Math.min(logoBoxW / (double) logoImage.getWidth(), logoBoxH / (double) logoImage.getHeight());
                int drawW = Math.max(1, (int) Math.round(logoImage.getWidth() * scale));
                int drawH = Math.max(1, (int) Math.round(logoImage.getHeight() * scale));
                g2.drawImage(logoImage, logoX, logoY + (logoBoxH - drawH) / 2, drawW, drawH, null);
                textX += logoBoxW + 14;
            }

            int fontTitleSize = bounds.height >= 160 ? 13 : 11;
            int fontRowSize = bounds.height >= 160 ? 11 : 10;
            int rowSpacing = bounds.height >= 160 ? 16 : 13;

            g2.setColor(new Color(27, 38, 56));
            g2.setFont(new Font("SansSerif", Font.BOLD, fontTitleSize));
            g2.drawString("Datos cartograficos", textX, contentY);

            g2.setFont(new Font("SansSerif", Font.PLAIN, fontRowSize));
            g2.setColor(new Color(86, 96, 110));
            int rowY = contentY + 20;
            drawCartoucheRowScaled(g2, "Estudio", blankOr(settings.studyName(), snapshot.projectName()), textX, rowY, fontRowSize);
            rowY += rowSpacing;
            drawCartoucheRowScaled(g2, "Proyecto", blankOr(settings.cartoucheProjectName(), snapshot.projectName()), textX, rowY, fontRowSize);
            rowY += rowSpacing;
            drawCartoucheRowScaled(g2, "Empresa", blankOr(settings.companyName(), "No especificada"), textX, rowY, fontRowSize);
            rowY += rowSpacing;
            drawCartoucheRowScaled(g2, "Cartografo", blankOr(settings.cartographerName(), "No especificado"), textX, rowY, fontRowSize);
            rowY += rowSpacing;
            drawCartoucheRowScaled(g2, "Fuente", blankOr(settings.imageSource(), "Vista actual del proyecto"), textX, rowY, fontRowSize);
            rowY += rowSpacing;
            drawCartoucheRowScaled(g2, "Coord.", blankOr(settings.coordinateReference(), snapshot.projectCrsLabel()), textX, rowY, fontRowSize);

            g2.setClip(clip);
        }

    public static void drawCartoucheRowScaled(Graphics2D g2, String label, String value, int x, int y, int fontSize) {
            g2.setColor(new Color(28, 38, 54));
            g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
            g2.drawString(label + ":", x, y);
            g2.setColor(new Color(86, 96, 110));
            g2.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
            int maxChars = fontSize >= 11 ? 38 : 30;
            int offsetX = fontSize >= 11 ? 62 : 54;
            g2.drawString(clipText(value, maxChars), x + offsetX, y);
        }

    public static void drawCartoucheRow(Graphics2D g2, String label, String value, int x, int y) {
            drawCartoucheRowScaled(g2, label, value, x, y, 11);
        }

    public static void drawLayoutImage(Graphics2D g2, Rectangle bounds, BufferedImage image) {
            if (bounds == null || image == null) {
                return;
            }
            g2.setColor(new Color(248, 250, 253));
            g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 18, 18);
            g2.setColor(new Color(201, 210, 222));
            g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 18, 18);

            g2.setColor(new Color(27, 38, 56));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("Perfil / imagen cartografica", bounds.x + 14, bounds.y + 18);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(new Color(96, 105, 118));
            g2.drawString("Movelo o redimensionalo desde el layout", bounds.x + 14, bounds.y + 31);

            int innerX = bounds.x + 12;
            int innerY = bounds.y + 40;
            int innerW = Math.max(40, bounds.width - 24);
            int innerH = Math.max(40, bounds.height - 52);
            g2.setColor(Color.WHITE);
            g2.fillRect(innerX, innerY, innerW, innerH);
            g2.setColor(new Color(214, 220, 228));
            g2.drawRect(innerX, innerY, innerW, innerH);

            double scale = Math.min(innerW / (double) Math.max(1, image.getWidth()), innerH / (double) Math.max(1, image.getHeight()));
            int drawW = Math.max(1, (int) Math.round(image.getWidth() * scale));
            int drawH = Math.max(1, (int) Math.round(image.getHeight() * scale));
            int drawX = innerX + (innerW - drawW) / 2;
            int drawY = innerY + (innerH - drawH) / 2;
            g2.drawImage(image, drawX, drawY, drawW, drawH, null);
        }

    public static void drawCatmapItems(Graphics2D g2, List<CatmapLayoutItem> items, java.util.Map<String, Rectangle> customItemBounds) {
            if (items == null || items.isEmpty()) {
                return;
            }
            for (CatmapLayoutItem item : items) {
                if (item == null || !item.isVisible()) {
                    continue;
                }
                Rectangle bounds = new Rectangle(item.getX(), item.getY(), Math.max(24, item.getWidth()), Math.max(24, item.getHeight()));
                customItemBounds.put(item.getId(), bounds);
                switch (item.getKind()) {
                    case TEXT -> drawCatmapText(g2, item, bounds);
                    case IMAGE -> drawCatmapImage(g2, item, bounds);
                    case RECTANGLE -> drawCatmapRectangle(g2, item, bounds);
                    case ELLIPSE -> drawCatmapEllipse(g2, item, bounds);
                    case LINE -> drawCatmapLine(g2, item, bounds);
                }
                if (item.isLocked()) {
                    drawCatmapLockBadge(g2, bounds);
                }
            }
        }

    public static void drawCatmapLockBadge(Graphics2D g2, Rectangle bounds) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                int badgeSize = 18;
                int badgeX = bounds.x + Math.max(4, bounds.width - badgeSize - 4);
                int badgeY = bounds.y + 4;
                copy.setColor(new Color(30, 41, 59, 210));
                copy.fillRoundRect(badgeX, badgeY, badgeSize, badgeSize, 8, 8);
                copy.setColor(Color.WHITE);
                copy.setFont(new Font("SansSerif", Font.BOLD, 10));
                FontMetrics metrics = copy.getFontMetrics();
                String text = "B";
                int tx = badgeX + (badgeSize - metrics.stringWidth(text)) / 2;
                int ty = badgeY + ((badgeSize - metrics.getHeight()) / 2) + metrics.getAscent();
                copy.drawString(text, tx, ty);
            } finally {
                copy.dispose();
            }
        }

    public static void drawCatmapText(Graphics2D g2, CatmapLayoutItem item, Rectangle bounds) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                Color fill = item.getFillColor();
                if (fill.getAlpha() > 0) {
                    copy.setColor(fill);
                    copy.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);
                }
                copy.setColor(item.getTextColor());
                int style = Font.PLAIN;
                if (item.isBold()) {
                    style |= Font.BOLD;
                }
                if (item.isItalic()) {
                    style |= Font.ITALIC;
                }
                copy.setFont(new Font("SansSerif", style, item.getFontSize()));
                copy.setClip(bounds.x + 6, bounds.y + 6, Math.max(12, bounds.width - 12), Math.max(12, bounds.height - 12));
                FontMetrics metrics = copy.getFontMetrics();
                List<String> lines = wrapText(item.getText().isBlank() ? item.getLabel() : item.getText(), metrics, Math.max(40, bounds.width - 12));
                int lineHeight = metrics.getHeight();
                int textY = bounds.y + 8 + metrics.getAscent();
                for (String line : lines) {
                    int drawX = switch (item.getAlign()) {
                        case CENTER -> bounds.x + Math.max(6, (bounds.width - metrics.stringWidth(line)) / 2);
                        case RIGHT -> bounds.x + Math.max(6, bounds.width - metrics.stringWidth(line) - 8);
                        default -> bounds.x + 8;
                    };
                    copy.drawString(line, drawX, textY);
                    textY += lineHeight;
                    if (textY > bounds.y + bounds.height - 4) {
                        break;
                    }
                }
            } finally {
                copy.dispose();
            }
        }

    public static void drawCatmapImage(Graphics2D g2, CatmapLayoutItem item, Rectangle bounds) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(new Color(248, 250, 253));
                copy.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);
                copy.setColor(new Color(203, 213, 225));
                copy.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);
                BufferedImage image = loadImageAsset(item.getImagePath());
                if (image == null) {
                    copy.setColor(new Color(100, 116, 139));
                    copy.setFont(new Font("SansSerif", Font.BOLD, 12));
                    copy.drawString(item.getLabel().isBlank() ? "Imagen" : item.getLabel(), bounds.x + 10, bounds.y + 18);
                    copy.setFont(new Font("SansSerif", Font.PLAIN, 11));
                    copy.drawString("Selecciona un archivo valido", bounds.x + 10, bounds.y + 34);
                    return;
                }
                int innerBoxX = bounds.x + 8;
                int innerBoxY = bounds.y + 8;
                int innerBoxW = Math.max(1, bounds.width - 16);
                int innerBoxH = Math.max(1, bounds.height - 16);
                double scaleValue = Math.min(innerBoxW / (double) Math.max(1, image.getWidth()), innerBoxH / (double) Math.max(1, image.getHeight()));
                int drawW = Math.max(1, (int) Math.round(image.getWidth() * scaleValue));
                int drawH = Math.max(1, (int) Math.round(image.getHeight() * scaleValue));
                int drawX = innerBoxX + Math.max(0, (innerBoxW - drawW) / 2);
                int drawY = innerBoxY + Math.max(0, (innerBoxH - drawH) / 2);
                copy.drawImage(image, drawX, drawY, drawW, drawH, null);
            } finally {
                copy.dispose();
            }
        }

    public static void drawCatmapRectangle(Graphics2D g2, CatmapLayoutItem item, Rectangle bounds) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(item.getFillColor());
                if (item.getFillColor().getAlpha() > 0) {
                    copy.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                }
                copy.setColor(item.getStrokeColor());
                copy.setStroke(new BasicStroke(item.getLineWidth()));
                copy.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
            } finally {
                copy.dispose();
            }
        }

    public static void drawCatmapEllipse(Graphics2D g2, CatmapLayoutItem item, Rectangle bounds) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                Ellipse2D ellipse = new Ellipse2D.Double(bounds.x, bounds.y, bounds.width, bounds.height);
                copy.setColor(item.getFillColor());
                if (item.getFillColor().getAlpha() > 0) {
                    copy.fill(ellipse);
                }
                copy.setColor(item.getStrokeColor());
                copy.setStroke(new BasicStroke(item.getLineWidth()));
                copy.draw(ellipse);
            } finally {
                copy.dispose();
            }
        }

    public static void drawCatmapLine(Graphics2D g2, CatmapLayoutItem item, Rectangle bounds) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(item.getStrokeColor());
                copy.setStroke(new BasicStroke(item.getLineWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                copy.drawLine(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
            } finally {
                copy.dispose();
            }
        }

    public static List<String> wrapText(String text, FontMetrics metrics, int maxWidth) {
            List<String> lines = new ArrayList<>();
            String content = text != null ? text : "";
            for (String paragraph : content.split("\\R", -1)) {
                String current = "";
                for (String word : paragraph.split(" ")) {
                    if (word.isBlank()) {
                        continue;
                    }
                    String candidate = current.isBlank() ? word : current + " " + word;
                    if (!current.isBlank() && metrics.stringWidth(candidate) > maxWidth) {
                        lines.add(current);
                        current = word;
                    } else {
                        current = candidate;
                    }
                }
                if (!current.isBlank()) {
                    lines.add(current);
                } else if (paragraph.isBlank()) {
                    lines.add("");
                }
            }
            if (lines.isEmpty()) {
                lines.add("");
            }
            return lines;
        }

    public static BufferedImage loadImageAsset(String path) {
            if (path == null || path.isBlank()) {
                return null;
            }
            try {
                File file = new File(path);
                if (!file.isFile()) {
                    return null;
                }
                return ImageIO.read(file);
            } catch (Exception ex) {
                return null;
            }
        }

    public static String blankOr(String primary, String fallback) {
            return primary != null && !primary.isBlank() ? primary : fallback;
        }

    public static String clipText(String value, int max) {
            if (value == null) {
                return "";
            }
            return value.length() > max ? value.substring(0, Math.max(0, max - 3)) + "..." : value;
        }

    public static Color colorOr(Color color, Color fallback) {
            return color != null ? color : fallback;
        }

    public static double estimateScaleDenominator(MapFrameGeometry mapFrame, int renderDpi) {
            if (mapFrame == null || mapFrame.shownGroundMeters() <= 0 || renderDpi <= 0) {
                return 0;
            }
            double shownGroundMeters = mapFrame.shownGroundMeters();
            double mapWidthMetersOnPaper = (mapFrame.imageBounds().width / (double) renderDpi) * 0.0254d;
            if (mapWidthMetersOnPaper <= 0) {
                return 0;
            }
            return shownGroundMeters / mapWidthMetersOnPaper;
        }

    public static double convertWorldWidthToMeters(LayoutSnapshot snapshot, double worldWidthUnits, double centerY) {
            if (snapshot == null || worldWidthUnits <= 0) {
                return 0d;
            }
            String projectCrs = CRSDefinitions.normalizeCode(snapshot.projectCrsCode());
            if (isGeographicCrs(projectCrs)) {
                double metersPerDegreeLon = 111320d * Math.cos(Math.toRadians(centerY));
                metersPerDegreeLon = Math.max(0.0001d, Math.abs(metersPerDegreeLon));
                return worldWidthUnits * metersPerDegreeLon;
            }
            return worldWidthUnits;
        }

    public static boolean isGeographicCrs(String projectCrs) {
            return "EPSG:4326".equalsIgnoreCase(projectCrs)
                    || "EPSG:4258".equalsIgnoreCase(projectCrs)
                    || "EPSG:4269".equalsIgnoreCase(projectCrs)
                    || "EPSG:4674".equalsIgnoreCase(projectCrs)
                    || "EPSG:4190".equalsIgnoreCase(projectCrs)
                    || "EPSG:4221".equalsIgnoreCase(projectCrs);
        }

    public static void drawPointSymbolPreview(Graphics2D g2, Layer layer, int x, int y, CategoryStyleRule categoryRule) {
            Color color = colorOr(categoryRule != null ? categoryRule.getPrimaryColor() : layer.getPointColor(), new Color(59, 130, 246));
            int left = x + 3;
            int top = y - 11;
            int size = Math.max(12, categoryRule != null ? categoryRule.getPointSize() + 2 : 12);
            String catId = categoryRule != null ? categoryRule.getCatalogSymbolId() : layer.getCatalogSymbolId();
            if (catId != null && !catId.isEmpty() && !"circle".equals(catId)) {
                PointSymbolCatalog.render(g2, catId, left + size/2, top + size/2, size + 2, color, color.darker(), 1.2f);
                return;
            }
            if (categoryRule == null && PointGraphicSymbolSupport.paintLayerSymbol(g2, layer, left + (size / 2), top + (size / 2), 18)) {
                return;
            }
            Layer.PointSymbolStyle style = categoryRule != null ? categoryRule.getPointSymbolStyle() : layer.getPointSymbolStyle();
            if (style == null) {
                style = Layer.PointSymbolStyle.CIRCLE;
            }
            PointSymbolRenderer.paint(g2, style, left + (size / 2), top + (size / 2), size, color, new Color(33, 33, 33));
        }

    public static void drawLineSymbolPreview(Graphics2D g2, Layer layer, int x, int y, CategoryStyleRule categoryRule) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(colorOr(categoryRule != null ? categoryRule.getPrimaryColor() : layer.getLineColor(), new Color(16, 185, 129)));
                float previewWidth = categoryRule != null ? categoryRule.getLineWidth() : layer.getLineWidth();
                copy.setStroke(LineSymbolRenderer.buildStroke(categoryRule != null ? categoryRule.getLineStyle() : layer.getLineSymbolStyle(), Math.max(1.8f, previewWidth)));
                copy.drawLine(x, y - 4, x + 20, y - 4);
            } finally {
                copy.dispose();
            }
        }

    public static void drawPolygonSymbolPreview(Graphics2D g2, Layer layer, int x, int y, CategoryStyleRule categoryRule) {
            Graphics2D copy = (Graphics2D) g2.create();
            try {
                Rectangle bounds = new Rectangle(x, y - 12, 20, 16);
                Layer.PolygonFillStyle style = categoryRule != null
                        ? categoryRule.getPolygonFillStyle()
                        : layer.getPolygonFillStyle() != null ? layer.getPolygonFillStyle() : Layer.PolygonFillStyle.SOLID;
                if (style != Layer.PolygonFillStyle.OUTLINE_ONLY) {
                    copy.setPaint(buildPolygonPreviewPaint(layer, bounds, categoryRule));
                    copy.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                }
                copy.setPaint(null);
                copy.setColor(colorOr(categoryRule != null ? categoryRule.getSecondaryColor() : layer.getBorderColor(), new Color(146, 64, 14)));
                copy.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
            } finally {
                copy.dispose();
            }
        }

    public static Paint buildPolygonPreviewPaint(Layer layer, Rectangle bounds, CategoryStyleRule categoryRule) {
            Color fill = colorOr(categoryRule != null ? categoryRule.getPrimaryColor() : layer.getFillColor(), new Color(251, 191, 36));
            Layer.PolygonFillStyle style = categoryRule != null
                    ? categoryRule.getPolygonFillStyle()
                    : layer.getPolygonFillStyle() != null ? layer.getPolygonFillStyle() : Layer.PolygonFillStyle.SOLID;
            if (style == Layer.PolygonFillStyle.SOLID) {
                return fill;
            }

            BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            try {
                g.setColor(new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 60));
                g.fillRect(0, 0, 10, 10);
                g.setColor(colorOr(categoryRule != null ? categoryRule.getSecondaryColor() : layer.getBorderColor(), new Color(146, 64, 14)));
                switch (style) {
                    case DIAGONAL_HATCH -> {
                        g.drawLine(-2, 9, 9, -2);
                        g.drawLine(2, 11, 11, 2);
                    }
                    case CROSS_HATCH -> {
                        g.drawLine(0, 5, 10, 5);
                        g.drawLine(5, 0, 5, 10);
                    }
                    case DOTS -> {
                        g.fillOval(2, 2, 2, 2);
                        g.fillOval(6, 6, 2, 2);
                    }
                    default -> {
                    }
                }
            } finally {
                g.dispose();
            }
            return new java.awt.TexturePaint(img, bounds);
        }

    public static Path2D buildStar(double cx, double cy, double outer, double inner) {
            Path2D path = new Path2D.Double();
            for (int i = 0; i < 10; i++) {
                double radius = i % 2 == 0 ? outer : inner;
                double angle = Math.toRadians(-90 + (i * 36));
                double x = cx + Math.cos(angle) * radius;
                double y = cy + Math.sin(angle) * radius;
                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            path.closePath();
            return path;
        }

    public static String layerTypeLabel(Layer layer) {
            if (layer instanceof OnlineTileLayer) {
                return "Mapa base online";
            }
            if (layer instanceof OnlineWmsLayer) {
                return "WMS";
            }
            if (layer instanceof RasterLayer) {
                return TopographyWorkflowSupport.isDemLikeRaster(layer) ? "DEM raster" : "Raster";
            }
            if (layer instanceof OnlineWfsLayer) {
                return "WFS";
            }
            if (layer instanceof PostgisLayer) {
                return "PostGIS";
            }
            if (layer instanceof GeoPackageLayer) {
                return "GeoPackage";
            }
            if (layer instanceof GpxLayer gpxLayer) {
                return "GPX " + gpxLayer.getContentKind().getLabel();
            }
            String geometryFamily = VectorLayerUtils.resolveGeometryFamily(
                    MapLayoutComposerDialog.ctxMapPanel() != null ? MapLayoutComposerDialog.ctxMapPanel().getShapefileData(layer) : null
            );
            if ("POINT".equalsIgnoreCase(geometryFamily)) {
                return "Punto";
            }
            if ("LINE".equalsIgnoreCase(geometryFamily)) {
                return "Linea";
            }
            if ("POLYGON".equalsIgnoreCase(geometryFamily)) {
                return "Poligono";
            }
            String type = layer.getType();
            if (type == null || type.isBlank()) {
                return "Vectorial";
            }
            return type;
        }

    public static double chooseRoundedDistance(double targetMeters) {
            if (targetMeters <= 0) {
                return 0;
            }
            double exponent = Math.pow(10, Math.floor(Math.log10(targetMeters)));
            double normalized = targetMeters / exponent;
            double rounded;
            if (normalized < 1.5d) {
                rounded = 1d;
            } else if (normalized < 3.5d) {
                rounded = 2d;
            } else if (normalized < 7.5d) {
                rounded = 5d;
            } else {
                rounded = 10d;
            }
            return rounded * exponent;
        }
    }
