package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.LayerGroup;
import ar.com.catgis.data.online.OnlineWmsLayer;
import ar.com.catgis.data.vector.ShapefileData;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

public class LayerCellRenderer extends DefaultListCellRenderer {

    private final LayersPanel panel;
    private final JPanel rendererPanel = new JPanel(new BorderLayout(8, 4));
    private final JCheckBox visibleCheck = new JCheckBox();
    private final JLabel iconLabel = new JLabel();
    private final JLabel nameLabel = new JLabel();
    private final JLabel metaLabel = new JLabel();
    private final JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 6));
    private final JPanel centerPanel = new JPanel(new BorderLayout(2, 2));
    private final Icon pointLayerIcon = createPointLayerIcon();
    private final Icon lineLayerIcon = createLineLayerIcon();
    private final Icon polygonLayerIcon = createPolygonLayerIcon();
    private final Icon rasterLayerIcon = createRasterLayerIcon();

    public LayerCellRenderer(LayersPanel panel) {
        this.panel = panel;
        visibleCheck.setOpaque(false);
        visibleCheck.setFocusable(false);
        visibleCheck.setPreferredSize(new Dimension(18, 18));

        iconLabel.setPreferredSize(new Dimension(20, 20));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 13f));
        metaLabel.setFont(metaLabel.getFont().deriveFont(Font.PLAIN, 11f));

        leftPanel.setOpaque(false);
        centerPanel.setOpaque(false);
        rendererPanel.setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));

        leftPanel.add(visibleCheck);
        leftPanel.add(iconLabel);
        centerPanel.add(nameLabel, BorderLayout.NORTH);
        centerPanel.add(metaLabel, BorderLayout.SOUTH);
        rendererPanel.add(leftPanel, BorderLayout.WEST);
        rendererPanel.add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof LayerGroup group) {
            boolean groupVisible = group.isVisible();
            int memberCount = AppContext.project() != null
                    ? AppContext.project().getLayersForGroup(group.getName()).size()
                    : 0;

            Color bg = isSelected ? new Color(217, 231, 251) : new Color(236, 244, 253);
            Color fg = new Color(30, 38, 52);
            Color metaFg = new Color(95, 106, 122);
            Color accent = isSelected ? new Color(53, 105, 189) : new Color(138, 171, 214);

            rendererPanel.setBackground(bg);
            leftPanel.setBackground(bg);
            centerPanel.setBackground(bg);

            visibleCheck.setSelected(groupVisible);
            iconLabel.setIcon(AppIcons.openIcon());
            nameLabel.setText((group.isExpanded() ? "\u25BE " : "\u25B8 ") + group.getName());
            nameLabel.setForeground(fg);
            metaLabel.setText("Grupo | " + memberCount + " capas" + (groupVisible ? "" : " | Oculto"));
            metaLabel.setForeground(metaFg);

            rendererPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(1, 6, 1, 1, accent),
                            BorderFactory.createLineBorder(isSelected ? new Color(113, 147, 201) : new Color(196, 210, 229))
                    ),
                    BorderFactory.createEmptyBorder(6, 8, 6, 6)
            ));
            return rendererPanel;
        }

        Layer layer = (Layer) value;
        boolean missingCrs = hasMissingCRS(layer);
        boolean editingLayer = AppContext.mapPanel() != null && AppContext.mapPanel().isLayerArmedForEditing(layer);
        boolean effectiveVisible = AppContext.project() == null
                ? layer.isVisible()
                : AppContext.project().isLayerEffectivelyVisible(layer);
        boolean layerInGroup = layer.isInGroup();

        Color bg = editingLayer
                ? new Color(255, 239, 239)
                : (isSelected ? new Color(220, 235, 255) : (layerInGroup ? new Color(248, 251, 255) : Color.WHITE));
        Color fg = editingLayer ? new Color(170, 24, 24) : new Color(30, 30, 30);
        Color metaFg = missingCrs
                ? new Color(170, 70, 20)
                : (editingLayer
                ? new Color(185, 48, 48)
                : (isSelected ? new Color(50, 70, 100) : new Color(110, 110, 110)));

        rendererPanel.setBackground(bg);
        leftPanel.setBackground(bg);
        centerPanel.setBackground(bg);

        visibleCheck.setSelected(layer.isVisible());
        iconLabel.setIcon(resolveLayerIcon(layer));
        nameLabel.setText((layerInGroup ? "  - " : "") + layer.getName());
        nameLabel.setForeground(effectiveVisible ? fg : new Color(120, 120, 120));
        String metaText = buildMetaText(layer);
        if (layerInGroup && layer.getGroupName() != null && !layer.getGroupName().isBlank()) {
            metaText = "Carpeta: " + layer.getGroupName() + " | " + metaText;
        }
        metaLabel.setText(metaText);
        metaLabel.setForeground(metaFg);

        if (layerInGroup) {
            rendererPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(1, 14, 1, 1, isSelected ? new Color(78, 125, 198) : new Color(184, 206, 240)),
                            BorderFactory.createLineBorder(editingLayer
                                    ? new Color(220, 90, 90)
                                    : (isSelected ? new Color(120, 160, 220) : new Color(220, 228, 239)))
                    ),
                    BorderFactory.createEmptyBorder(5, 14, 5, 6)
            ));
        } else {
            rendererPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(editingLayer
                            ? new Color(220, 90, 90)
                            : (isSelected ? new Color(120, 160, 220) : new Color(230, 230, 230))),
                    BorderFactory.createEmptyBorder(5, 6, 5, 6)
            ));
        }

        return rendererPanel;
    }

    private String buildMetaText(Layer layer) {
        String crsInfo = formatCRSInfo(layer);
        if (layer instanceof OnlineTileLayer) {
            OnlineTileLayer online = (OnlineTileLayer) layer;
            String hidden = buildVisibilitySuffix(layer);
            return "Mapa base online | " + (online.getProviderName() != null && !online.getProviderName().isBlank() ? online.getProviderName() : layer.getName())
                    + " | " + crsInfo + hidden;
        }
        if (layer instanceof OnlineWmsLayer) {
            OnlineWmsLayer wms = (OnlineWmsLayer) layer;
            String hidden = buildVisibilitySuffix(layer);
            return "WMS | " + (wms.getProviderName() != null && !wms.getProviderName().isBlank() ? wms.getProviderName() : layer.getName())
                    + " | " + crsInfo + hidden;
        }
        if (layer instanceof OnlineWfsLayer) {
            OnlineWfsLayer wfs = (OnlineWfsLayer) layer;
            String hidden = buildVisibilitySuffix(layer);
            return "WFS | " + resolveGeometryTypeLabel(layer) + " | " + layer.getFeatureCount()
                    + " elementos | " + crsInfo + " | Solo lectura"
                    + (wfs.getProviderName() != null && !wfs.getProviderName().isBlank() ? " | " + wfs.getProviderName() : "")
                    + hidden;
        }
        if (layer instanceof GeoPackageLayer) {
            GeoPackageLayer geoPackage = (GeoPackageLayer) layer;
            String hidden = buildVisibilitySuffix(layer);
            return "GeoPackage | " + resolveGeometryTypeLabel(layer) + " | " + layer.getFeatureCount()
                    + " elementos | " + crsInfo + " | Solo lectura"
                    + (geoPackage.getTableName() != null && !geoPackage.getTableName().isBlank() ? " | " + geoPackage.getTableName() : "")
                    + hidden;
        }
        if (panel.isRasterLayer(layer)) {
            String hidden = buildVisibilitySuffix(layer);
            if (layer instanceof RasterLayer rasterLayer && panel.hasProRasterMetadata(rasterLayer)) {
                String variable = rasterLayer.getProVariableName() != null && !rasterLayer.getProVariableName().isBlank()
                        ? rasterLayer.getProVariableName()
                        : "variable";
                String time = rasterLayer.getProAcquisitionStart() != null && !rasterLayer.getProAcquisitionStart().isBlank()
                        ? " | " + rasterLayer.getProAcquisitionStart()
                        : "";
                return "Raster Pro | " + variable + time + " | " + crsInfo + hidden;
            }
            return "Raster | " + crsInfo + hidden;
        }
        if (CadLayerSupport.isCadLayer(layer)) {
            String hidden = buildVisibilitySuffix(layer);
            return resolveGeometryTypeLabel(layer) + " CAD | " + layer.getFeatureCount() + " elementos | "
                    + crsInfo + " | " + CadGeoreferenceSupport.buildDetailedSummary(layer)
                    + " | " + CadPlacementSupport.buildPlacementSummary(layer)
                    + " | " + CadLayerSupport.buildCadInternalLayerFilterLabel(layer) + hidden;
        }

        String type = resolveGeometryTypeLabel(layer);
        int count = layer.getFeatureCount();
        String labelInfo = layer.isLabelsVisible()
                ? " | Etiquetas: " + (layer.getLabelField() != null ? layer.getLabelField() : "Si")
                : "";
        String hidden = buildVisibilitySuffix(layer);
        String editing = (AppContext.mapPanel() != null && AppContext.mapPanel().isLayerArmedForEditing(layer))
                ? " | En edicion"
                : "";
        return type + " | " + count + " elementos | " + crsInfo + editing + labelInfo + hidden;
    }

    private String buildVisibilitySuffix(Layer layer) {
        if (layer == null) {
            return "";
        }
        if (!layer.isVisible()) {
            return " | Oculta";
        }
        if (AppContext.project() != null && !AppContext.project().isLayerEffectivelyVisible(layer)) {
            return " | Oculta por grupo";
        }
        return "";
    }

    private String formatCRSInfo(Layer layer) {
        String crs = layer.getSourceCRS();
        if (crs == null || crs.isBlank()) {
            return "Sin CRS definido";
        }
        return crs;
    }

    private boolean hasMissingCRS(Layer layer) {
        return layer == null || layer.getSourceCRS() == null || layer.getSourceCRS().isBlank();
    }

    private javax.swing.Icon resolveLayerIcon(Layer layer) {
        if (panel.isRasterLayer(layer)) {
            return rasterLayerIcon;
        }
        String type = resolveGeometryTypeLabel(layer);
        if ("PUNTO".equals(type)) {
            return pointLayerIcon;
        }
        if ("LINEA".equals(type)) {
            return lineLayerIcon;
        }
        if ("POLIGONO".equals(type)) {
            return polygonLayerIcon;
        }
        return AppIcons.genericLayerIcon();
    }

    private String resolveGeometryTypeLabel(Layer layer) {
        if (layer == null) {
            return "-";
        }
        if (panel.isRasterLayer(layer)) {
            return "Raster";
        }

        String geometryType = resolveGeometryTypeFromData(layer);
        if (geometryType != null) {
            return geometryType;
        }

        if (layer instanceof GeoPackageLayer) {
            String geometryLabel = ((GeoPackageLayer) layer).getGeometryTypeLabel();
            if (geometryLabel != null && !geometryLabel.isBlank()) {
                String upper = geometryLabel.toUpperCase();
                if (upper.contains("POINT") || upper.contains("PUNTO")) {
                    return "PUNTO";
                }
                if (upper.contains("LINE")) {
                    return "LINEA";
                }
                if (upper.contains("POLYGON") || upper.contains("POLIG")) {
                    return "POLIGONO";
                }
            }
        }

        if (layer instanceof PostgisLayer) {
            String geometryLabel = ((PostgisLayer) layer).getGeometryTypeLabel();
            if (geometryLabel != null && !geometryLabel.isBlank()) {
                String upper = geometryLabel.toUpperCase();
                if (upper.contains("POINT") || upper.contains("PUNTO")) {
                    return "PUNTO";
                }
                if (upper.contains("LINE")) {
                    return "LINEA";
                }
                if (upper.contains("POLYGON") || upper.contains("POLIG")) {
                    return "POLIGONO";
                }
            }
        }

        String rawType = layer.getType() != null ? layer.getType().toUpperCase() : "";
        if (rawType.contains("POINT") || rawType.contains("PUNTO")) {
            return "PUNTO";
        }
        if (rawType.contains("LINE")) {
            return "LINEA";
        }
        if (rawType.contains("POLYGON") || rawType.contains("POLIG")) {
            return "POLIGONO";
        }
        return layer.getType() != null ? layer.getType() : "-";
    }

    private String resolveGeometryTypeFromData(Layer layer) {
        if (layer == null || AppContext.mapPanel() == null) {
            return null;
        }

        ShapefileData data = AppContext.mapPanel().getShapefileData(layer);
        if (data == null) {
            return null;
        }

        SimpleFeatureCollection featureCollection = data.getFeatureCollection();
        if (featureCollection != null) {
            SimpleFeatureType schema = featureCollection.getSchema();
            if (schema != null && schema.getGeometryDescriptor() != null) {
                String byBinding = resolveGeometryTypeFromBinding(
                        schema.getGeometryDescriptor().getType().getBinding()
                );
                if (byBinding != null) {
                    return byBinding;
                }
            }
        }

        List<SimpleFeature> features = data.getFeatures();
        if (features != null) {
            for (SimpleFeature feature : features) {
                if (feature == null) {
                    continue;
                }
                Object geometry = feature.getDefaultGeometry();
                if (geometry instanceof Geometry) {
                    String byGeometry = resolveGeometryTypeFromGeometry((Geometry) geometry);
                    if (byGeometry != null) {
                        return byGeometry;
                    }
                }
            }
        }
        return null;
    }

    private String resolveGeometryTypeFromBinding(Class<?> binding) {
        if (binding == null) {
            return null;
        }
        if (Point.class.isAssignableFrom(binding) || MultiPoint.class.isAssignableFrom(binding)) {
            return "PUNTO";
        }
        if (LineString.class.isAssignableFrom(binding) || MultiLineString.class.isAssignableFrom(binding)) {
            return "LINEA";
        }
        if (Polygon.class.isAssignableFrom(binding) || MultiPolygon.class.isAssignableFrom(binding)) {
            return "POLIGONO";
        }
        return null;
    }

    private String resolveGeometryTypeFromGeometry(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        if (geometry instanceof Point || geometry instanceof MultiPoint) {
            return "PUNTO";
        }
        if (geometry instanceof LineString || geometry instanceof MultiLineString) {
            return "LINEA";
        }
        if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
            return "POLIGONO";
        }
        return null;
    }

    private Icon createPointLayerIcon() {
        BufferedImage img = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(37, 99, 235));
        g.fillOval(6, 6, 6, 6);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(6, 6, 6, 6);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createLineLayerIcon() {
        BufferedImage img = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(22, 163, 74));
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(3, 12, 7, 8);
        g.drawLine(7, 8, 11, 10);
        g.drawLine(11, 10, 15, 5);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createPolygonLayerIcon() {
        BufferedImage img = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(251, 191, 36, 170));
        g.fillRoundRect(4, 4, 10, 10, 2, 2);
        g.setColor(new Color(180, 83, 9));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(4, 4, 10, 10, 2, 2);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createRasterLayerIcon() {
        BufferedImage img = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(226, 232, 240));
        g.fillRoundRect(2, 3, 14, 12, 2, 2);
        g.setColor(new Color(100, 116, 139));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(2, 3, 14, 12, 2, 2);
        g.setColor(new Color(59, 130, 246));
        g.drawLine(4, 11, 7, 8);
        g.drawLine(7, 8, 10, 10);
        g.drawLine(10, 10, 13, 6);
        g.dispose();
        return new ImageIcon(img);
    }
}
