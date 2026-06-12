package ar.com.catgis.core.model;

import ar.com.catgis.CatgisLogger;
import ar.com.catgis.core.model.LabelConfig;
import ar.com.catgis.core.model.GradientFill;

import ar.com.catgis.ProportionalSymbols;
import ar.com.catgis.PointSymbolCatalog;
import ar.com.catgis.GraduatedSymbology;
import ar.com.catgis.FieldConfig;
import ar.com.catgis.CRSDefinitions;
import ar.com.catgis.CategorizedSymbology;
import ar.com.catgis.RuleBasedSymbology;
import ar.com.catgis.CadLayerSupport;
import ar.com.catgis.CadGeoreference;
import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Layer {
    public enum LabelPlacementMode {
        AUTO("Auto (intenta mejor posicion)"),
        POINT_ABOVE("Punto - Arriba"),
        POINT_BELOW("Punto - Abajo"),
        POINT_LEFT("Punto - Izquierda"),
        POINT_RIGHT("Punto - Derecha"),
        POINT_CENTER("Punto - Centro"),
        LINE_CENTER("Linea - Centro"),
        LINE_FOLLOW("Linea - Seguir trazo"),
        POLYGON_CENTROID("Poligono - Centroide"),
        POLYGON_INTERIOR("Poligono - Punto interior");

        private final String label;
        LabelPlacementMode(String label) { this.label = label; }
        public static LabelPlacementMode fromValue(String value) {
            if (value != null) {
                for (LabelPlacementMode m : values()) {
                    if (m.name().equalsIgnoreCase(value.trim())) return m;
                }
            }
            return AUTO;
        }
        @Override public String toString() { return label; }
    }

    public enum PointSymbolStyle {
        CIRCLE("Circulo"),
        SQUARE("Cuadrado"),
        DIAMOND("Rombo"),
        TRIANGLE("Triangulo"),
        TARGET("Objetivo / Diana"),
        PIN("Pin / Marcador"),
        FLAG("Bandera"),
        STAR("Estrella 5 puntas"),
        STAR_6("Estrella 6 puntas"),
        WELL("Pozo / Sondeo"),
        CROSS("Cruz"),
        CROSS_DIAGONAL("Cruz diagonal (X)"),
        HEXAGON("Hexagono"),
        PENTAGON("Pentagono"),
        ARROW_UP("Flecha arriba"),
        ARROW_DOWN("Flecha abajo"),
        CAMERA("Camara"),
        TOWER("Torre / Antena"),
        TRIANGLE_INVERTED("Triangulo invertido"),
        RING("Anillo / Aro"),
        DOUBLE_CIRCLE("Doble circulo"),
        RECTANGLE_H("Rectangulo horizontal"),
        RECTANGLE_V("Rectangulo vertical"),
        ALERT("Alerta / Advertencia"),
        LOCATION("Ubicacion destacada"),
        SAMPLING("Muestreo ambiental"),
        CONTROL("Punto de control"),
        ACCESS("Acceso / Entrada");

        private final String label;

        PointSymbolStyle(String label) {
            this.label = label;
        }

        public static PointSymbolStyle fromValue(String value) {
            if (value != null) {
                for (PointSymbolStyle style : values()) {
                    if (style.name().equalsIgnoreCase(value.trim())) {
                        return style;
                    }
                }
            }
            return CIRCLE;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum LineSymbolStyle {
        SOLID("Solida"),
        DASHED("Discontinua / Guion"),
        DOTTED("Punteada"),
        DASH_DOT("Trazo y punto"),
        DASH_DOT_DOT("Trazo doble punto"),
        DOUBLE_LINE("Doble linea"),
        BOLD("Linea gruesa / Resaltada"),
        THIN("Linea fina / Tenue"),
        PATH_PRIMARY("Camino principal"),
        PATH_SECONDARY("Camino secundario / Sendero"),
        BOUNDARY("Limite / Lindero"),
        FENCE("Alambrado / Cerco"),
        WATERCOURSE("Cauce / Hidrografia"),
        DUCT("Ducto / Conducto"),
        AXIS("Eje / Directriz"),
        PROFILE("Perfil / Traza"),
        EASEMENT("Servidumbre"),
        BORDERED("Linea con borde");

        private final String label;

        LineSymbolStyle(String label) {
            this.label = label;
        }

        public static LineSymbolStyle fromValue(String value) {
            if (value != null) {
                for (LineSymbolStyle style : values()) {
                    if (style.name().equalsIgnoreCase(value.trim())) {
                        return style;
                    }
                }
            }
            return SOLID;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum PolygonFillStyle {
        SOLID("Solido / Relleno pleno"),
        DIAGONAL_HATCH("Trama diagonal (/)"),
        CROSS_HATCH("Trama cruzada (X)"),
        DOTS("Punteado"),
        OUTLINE_ONLY("Solo contorno"),
        TRANSPARENT("Transparente / Sin relleno"),
        SOFT_SHADOW("Sombreado suave"),
        HORIZONTAL_LINES("Lineas horizontales"),
        VERTICAL_LINES("Lineas verticales"),
        DIAGONAL_REVERSE("Trama diagonal invertida (\\)"),
        ENVIRONMENTAL("Zona ambiental"),
        WATER("Agua / Hidrografia"),
        VEGETATION("Vegetacion"),
        INFRASTRUCTURE("Infraestructura"),
        PARCEL("Parcela / Catastro"),
        RESTRICTION("Restriccion / Riesgo"),
        BUFFER_SOFT("Buffer / Area de influencia"),
        SATELLITE_OVERLAY("Overlay satelital semitransparente");

        private final String label;

        PolygonFillStyle(String label) {
            this.label = label;
        }

        public static PolygonFillStyle fromValue(String value) {
            if (value != null) {
                for (PolygonFillStyle style : values()) {
                    if (style.name().equalsIgnoreCase(value.trim())) {
                        return style;
                    }
                }
            }
            return SOLID;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private String name;
    private String path;
    private String type;
    private boolean visible = true;
    private String groupName = "";
    private String sourceName;
    private int featureCount;

    // Label configuration (extracted to LabelConfig value object)
    private final LabelConfig labelConfig = new LabelConfig();
    // Legacy fields kept for backward compatibility (delegated to labelConfig)
    private boolean labelsVisible = false;
    private String labelField;
    private String labelExpression;
    private String labelFontFamily = "SansSerif";
    private int labelFontSize = 10;
    private boolean labelBold = false;
    private boolean labelItalic = false;
    private boolean labelUnderline = false;
    private Color labelColor = Color.BLACK;
    private boolean labelHaloEnabled = false;
    private Color labelHaloColor = new Color(255, 255, 255, 200);
    private float labelHaloWidth = 2f;
    private int labelOffsetX = 0;
    private int labelOffsetY = 0;
    private String labelPlacement = "AUTO"; // kept for backward compat
    private LabelPlacementMode labelPlacementMode = LabelPlacementMode.AUTO;
    private int labelPriority = 5; // 1=highest, 10=lowest
    private boolean labelCollisionAvoid = true;
    private boolean labelBackgroundEnabled = false;
    private Color labelBackgroundColor = new Color(255, 255, 255, 180);
    private double labelMinScale = 0; // 0 = no limit
    private double labelMaxScale = 0; // 0 = no limit

    private Color fillColor = new Color(120, 170, 255, 120);
    private transient GradientFill gradientFill;
    private Color borderColor = Color.BLUE;
    private Color lineColor = Color.RED;
    private float lineWidth = 1.5f;
    private Color pointColor = Color.BLUE;
    private int pointSize = 8;
    private PointSymbolStyle pointSymbolStyle = PointSymbolStyle.CIRCLE;
    private String catalogSymbolId = "circle"; // ID from PointSymbolCatalog
    private String pointGraphicSymbol = "";
    private LineSymbolStyle lineSymbolStyle = LineSymbolStyle.SOLID;
    private PolygonFillStyle polygonFillStyle = PolygonFillStyle.SOLID;
    private final CategorizedSymbology pointCategorizedSymbology = new CategorizedSymbology();
    private final CategorizedSymbology lineCategorizedSymbology = new CategorizedSymbology();
    private final CategorizedSymbology polygonCategorizedSymbology = new CategorizedSymbology();
    private final GraduatedSymbology pointGraduatedSymbology = new GraduatedSymbology();
    private final GraduatedSymbology lineGraduatedSymbology = new GraduatedSymbology();
    private final GraduatedSymbology polygonGraduatedSymbology = new GraduatedSymbology();
    private final ProportionalSymbols proportionalSymbols = new ProportionalSymbols();
    private final RuleBasedSymbology pointRuleBasedSymbology = new RuleBasedSymbology();
    private final RuleBasedSymbology lineRuleBasedSymbology = new RuleBasedSymbology();
    private final RuleBasedSymbology polygonRuleBasedSymbology = new RuleBasedSymbology();
    private boolean heatmapEnabled = false;
    private int heatmapRadius = 30;
    private float heatmapOpacity = 0.6f;
    private boolean clusteringEnabled = false;
    private int clusterRadius = 30;

    private String sourceCRS = "";
    // CAD georeference (extracted to CadGeoreference value object)
    private final CadGeoreference cadGeoref = new CadGeoreference();
    // Legacy fields kept for backward compatibility
    private double cadOffsetX = 0d;
    private double cadOffsetY = 0d;
    private double cadScale = 1d;
    private double cadRotationDegrees = 0d;
    private String cadGeoreferenceMethod = "";
    private double cadGeorefM00 = 1d;
    private double cadGeorefM01 = 0d;
    private double cadGeorefM02 = 0d;
    private double cadGeorefM10 = 0d;
    private double cadGeorefM11 = 1d;
    private double cadGeorefM12 = 0d;
    private double cadGeorefResidualMean = Double.NaN;
    private double cadGeorefResidualMax = Double.NaN;
    private int cadGeorefReferenceCount = 0;
    private int cadGeorefCheckCount = 0;
    private final Set<String> cadHiddenInternalLayers = new LinkedHashSet<>();

    private float opacity = 1.0f;

    private final Map<String, FieldConfig> fieldConfigs = new LinkedHashMap<>();
    private final Map<String, Object> userData = new HashMap<>();

    public Layer(String name, String path, String type) {
        this.name = name;
        this.path = path;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public int getFeatureCount() {
        return featureCount;
    }

    public void setFeatureCount(int featureCount) {
        this.featureCount = featureCount;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName != null ? groupName.trim() : "";
    }

    public boolean isInGroup() {
        return groupName != null && !groupName.isBlank();
    }

    public boolean isLabelsVisible() {
        return labelsVisible;
    }

    public void setLabelsVisible(boolean labelsVisible) {
        this.labelsVisible = labelsVisible;
    }

    public String getLabelField() {
        return labelField;
    }

    public void setLabelField(String labelField) {
        this.labelField = labelField;
    }

    public String getLabelExpression() { return labelExpression; }
    public void setLabelExpression(String expr) { this.labelExpression = expr; }

    public String getLabelFontFamily() { return labelFontFamily; }
    public void setLabelFontFamily(String f) { if (f != null) labelFontFamily = f; }
    public int getLabelFontSize() { return labelFontSize; }
    public void setLabelFontSize(int s) { labelFontSize = Math.max(6, s); }
    public boolean isLabelBold() { return labelBold; }
    public void setLabelBold(boolean b) { labelBold = b; }
    public boolean isLabelItalic() { return labelItalic; }
    public void setLabelItalic(boolean b) { labelItalic = b; }
    public Color getLabelColor() { return labelColor; }
    public void setLabelColor(Color c) { if (c != null) labelColor = c; }
    public boolean isLabelHaloEnabled() { return labelHaloEnabled; }
    public void setLabelHaloEnabled(boolean b) { labelHaloEnabled = b; }
    public Color getLabelHaloColor() { return labelHaloColor; }
    public void setLabelHaloColor(Color c) { if (c != null) labelHaloColor = c; }
    public float getLabelHaloWidth() { return labelHaloWidth; }
    public void setLabelHaloWidth(float w) { labelHaloWidth = Math.max(0.5f, w); }
    public int getLabelOffsetX() { return labelOffsetX; }
    public void setLabelOffsetX(int x) { labelOffsetX = x; }
    public int getLabelOffsetY() { return labelOffsetY; }
    public void setLabelOffsetY(int y) { labelOffsetY = y; }
    public String getLabelPlacement() { return labelPlacement; }
    public void setLabelPlacement(String p) { if (p != null) labelPlacement = p; }
    public LabelPlacementMode getLabelPlacementMode() { return labelPlacementMode; }
    public void setLabelPlacementMode(LabelPlacementMode m) { if (m != null) labelPlacementMode = m; }
    public int getLabelPriority() { return labelPriority; }
    public void setLabelPriority(int p) { labelPriority = Math.max(1, Math.min(10, p)); }
    public boolean isLabelCollisionAvoid() { return labelCollisionAvoid; }
    public void setLabelCollisionAvoid(boolean b) { labelCollisionAvoid = b; }
    public boolean isLabelUnderline() { return labelUnderline; }
    public void setLabelUnderline(boolean b) { labelUnderline = b; }
    public boolean isLabelBackgroundEnabled() { return labelBackgroundEnabled; }
    public void setLabelBackgroundEnabled(boolean b) { labelBackgroundEnabled = b; }
    public Color getLabelBackgroundColor() { return labelBackgroundColor; }
    public void setLabelBackgroundColor(Color c) { if (c != null) labelBackgroundColor = c; }
    public double getLabelMinScale() { return labelMinScale; }
    public void setLabelMinScale(double s) { labelMinScale = s >= 0 ? s : 0; }
    public double getLabelMaxScale() { return labelMaxScale; }
    public void setLabelMaxScale(double s) { labelMaxScale = s >= 0 ? s : 0; }

    public boolean isLabelVisibleAtScale(double scaleDenominator) {
        if (scaleDenominator <= 0) return true;
        if (labelMinScale > 0 && scaleDenominator < labelMinScale) return false;
        if (labelMaxScale > 0 && scaleDenominator > labelMaxScale) return false;
        return true;
    }

    /**
     * Get the LabelConfig value object for this layer.
     * New code should use this instead of individual label getters/setters.
     */
    public LabelConfig getLabelConfig() { return labelConfig; }

    /**
     * Sync legacy label fields to LabelConfig.
     * Called after deserialization or manual field setting.
     */
    public void syncLabelConfigFromFields() {
        labelConfig.setVisible(labelsVisible);
        labelConfig.setField(labelField);
        labelConfig.setExpression(labelExpression);
        labelConfig.setFontFamily(labelFontFamily);
        labelConfig.setFontSize(labelFontSize);
        labelConfig.setBold(labelBold);
        labelConfig.setItalic(labelItalic);
        labelConfig.setUnderline(labelUnderline);
        labelConfig.setColor(labelColor);
        labelConfig.setHaloEnabled(labelHaloEnabled);
        labelConfig.setHaloColor(labelHaloColor);
        labelConfig.setHaloWidth(labelHaloWidth);
        labelConfig.setOffsetX(labelOffsetX);
        labelConfig.setOffsetY(labelOffsetY);
        labelConfig.setPlacement(labelPlacement);
        labelConfig.setPlacementMode(labelPlacementMode.name());
        labelConfig.setPriority(labelPriority);
        labelConfig.setCollisionAvoid(labelCollisionAvoid);
        labelConfig.setBackgroundEnabled(labelBackgroundEnabled);
        labelConfig.setBackgroundColor(labelBackgroundColor);
        labelConfig.setMinScale(labelMinScale);
        labelConfig.setMaxScale(labelMaxScale);
    }

    /**
     * Sync LabelConfig back to legacy fields.
     * Called before serialization.
     */
    public void syncLabelFieldsFromConfig() {
        labelsVisible = labelConfig.isVisible();
        labelField = labelConfig.getField();
        labelExpression = labelConfig.getExpression();
        labelFontFamily = labelConfig.getFontFamily();
        labelFontSize = labelConfig.getFontSize();
        labelBold = labelConfig.isBold();
        labelItalic = labelConfig.isItalic();
        labelUnderline = labelConfig.isUnderline();
        labelColor = labelConfig.getColor();
        labelHaloEnabled = labelConfig.isHaloEnabled();
        labelHaloColor = labelConfig.getHaloColor();
        labelHaloWidth = labelConfig.getHaloWidth();
        labelOffsetX = labelConfig.getOffsetX();
        labelOffsetY = labelConfig.getOffsetY();
        labelPlacement = labelConfig.getPlacement();
        try { labelPlacementMode = LabelPlacementMode.fromValue(labelConfig.getPlacementMode()); } catch (Exception ignored) { CatgisLogger.warn("Layer: operation failed", ignored); }
        labelPriority = labelConfig.getPriority();
        labelCollisionAvoid = labelConfig.isCollisionAvoid();
        labelBackgroundEnabled = labelConfig.isBackgroundEnabled();
        labelBackgroundColor = labelConfig.getBackgroundColor();
        labelMinScale = labelConfig.getMinScale();
        labelMaxScale = labelConfig.getMaxScale();
    }

    public Color getFillColor() {
        return fillColor;
    }

    public GradientFill getGradientFill() { return gradientFill; }
    public void setGradientFill(GradientFill gf) { this.gradientFill = gf; }

    public void setFillColor(Color fillColor) {
        if (fillColor != null) {
            this.fillColor = fillColor;
        }
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor != null) {
            this.borderColor = borderColor;
        }
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        if (lineColor != null) {
            this.lineColor = lineColor;
        }
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        if (lineWidth > 0) {
            this.lineWidth = lineWidth;
        }
    }

    public Color getPointColor() {
        return pointColor;
    }

    public void setPointColor(Color pointColor) {
        if (pointColor != null) {
            this.pointColor = pointColor;
        }
    }

    public int getPointSize() {
        return pointSize;
    }

    public void setPointSize(int pointSize) {
        if (pointSize > 0) {
            this.pointSize = pointSize;
        }
    }

    public PointSymbolStyle getPointSymbolStyle() {
        return pointSymbolStyle;
    }

    public void setPointSymbolStyle(PointSymbolStyle pointSymbolStyle) {
        if (pointSymbolStyle != null) {
            this.pointSymbolStyle = pointSymbolStyle;
        }
    }

    public String getCatalogSymbolId() { return catalogSymbolId; }
    public void setCatalogSymbolId(String id) { this.catalogSymbolId = id != null ? id : "circle"; }

    public String getPointGraphicSymbol() {
        return pointGraphicSymbol;
    }

    public void setPointGraphicSymbol(String pointGraphicSymbol) {
        this.pointGraphicSymbol = pointGraphicSymbol != null ? pointGraphicSymbol.trim() : "";
    }

    public boolean hasPointGraphicSymbol() {
        return pointGraphicSymbol != null && !pointGraphicSymbol.isBlank();
    }

    public LineSymbolStyle getLineSymbolStyle() {
        return lineSymbolStyle;
    }

    public void setLineSymbolStyle(LineSymbolStyle lineSymbolStyle) {
        if (lineSymbolStyle != null) {
            this.lineSymbolStyle = lineSymbolStyle;
        }
    }

    public PolygonFillStyle getPolygonFillStyle() {
        return polygonFillStyle;
    }

    public void setPolygonFillStyle(PolygonFillStyle polygonFillStyle) {
        if (polygonFillStyle != null) {
            this.polygonFillStyle = polygonFillStyle;
        }
    }

    public CategorizedSymbology getLineCategorizedSymbology() {
        return lineCategorizedSymbology;
    }

    public CategorizedSymbology getPointCategorizedSymbology() {
        return pointCategorizedSymbology;
    }

    public CategorizedSymbology getPolygonCategorizedSymbology() {
        return polygonCategorizedSymbology;
    }

    public GraduatedSymbology getPointGraduatedSymbology() {
        return pointGraduatedSymbology;
    }

    public GraduatedSymbology getLineGraduatedSymbology() {
        return lineGraduatedSymbology;
    }

    public GraduatedSymbology getPolygonGraduatedSymbology() {
        return polygonGraduatedSymbology;
    }

    public ProportionalSymbols getProportionalSymbols() {
        return proportionalSymbols;
    }

    public RuleBasedSymbology getPointRuleBasedSymbology() { return pointRuleBasedSymbology; }
    public RuleBasedSymbology getLineRuleBasedSymbology() { return lineRuleBasedSymbology; }
    public RuleBasedSymbology getPolygonRuleBasedSymbology() { return polygonRuleBasedSymbology; }

    public boolean isHeatmapEnabled() { return heatmapEnabled; }
    public void setHeatmapEnabled(boolean e) { this.heatmapEnabled = e; }

    public int getHeatmapRadius() { return heatmapRadius; }
    public void setHeatmapRadius(int r) { this.heatmapRadius = Math.max(5, Math.min(200, r)); }

    public float getHeatmapOpacity() { return heatmapOpacity; }
    public void setHeatmapOpacity(float o) { this.heatmapOpacity = Math.max(0.1f, Math.min(1.0f, o)); }

    public boolean isClusteringEnabled() { return clusteringEnabled; }
    public void setClusteringEnabled(boolean e) { this.clusteringEnabled = e; }

    public int getClusterRadius() { return clusterRadius; }
    public void setClusterRadius(int r) { this.clusterRadius = Math.max(5, Math.min(200, r)); }

    public String getSourceCRS() {
        return sourceCRS;
    }

    public void setSourceCRS(String sourceCRS) {
        this.sourceCRS = CRSDefinitions.normalizeCode(sourceCRS);
    }

    public double getCadOffsetX() {
        return cadOffsetX;
    }

    public void setCadOffsetX(double cadOffsetX) {
        if (Double.isFinite(cadOffsetX)) {
            this.cadOffsetX = cadOffsetX;
        }
    }

    public double getCadOffsetY() {
        return cadOffsetY;
    }

    public void setCadOffsetY(double cadOffsetY) {
        if (Double.isFinite(cadOffsetY)) {
            this.cadOffsetY = cadOffsetY;
        }
    }

    public double getCadScale() {
        return cadScale;
    }

    public void setCadScale(double cadScale) {
        if (Double.isFinite(cadScale) && cadScale > 0d) {
            this.cadScale = cadScale;
        }
    }

    public double getCadRotationDegrees() {
        return cadRotationDegrees;
    }

    public void setCadRotationDegrees(double cadRotationDegrees) {
        if (Double.isFinite(cadRotationDegrees)) {
            this.cadRotationDegrees = cadRotationDegrees;
        }
    }

    public boolean hasCadPlacementAdjustment() {
        return Math.abs(cadOffsetX) > 1e-9
                || Math.abs(cadOffsetY) > 1e-9
                || Math.abs(cadScale - 1d) > 1e-9
                || Math.abs(cadRotationDegrees) > 1e-9;
    }

    public String getCadGeoreferenceMethod() {
        return cadGeoreferenceMethod;
    }

    public void setCadGeoreferenceMethod(String cadGeoreferenceMethod) {
        this.cadGeoreferenceMethod = cadGeoreferenceMethod != null ? cadGeoreferenceMethod.trim() : "";
    }

    public double getCadGeorefM00() {
        return cadGeorefM00;
    }

    public void setCadGeorefM00(double cadGeorefM00) {
        if (Double.isFinite(cadGeorefM00)) {
            this.cadGeorefM00 = cadGeorefM00;
        }
    }

    public double getCadGeorefM01() {
        return cadGeorefM01;
    }

    public void setCadGeorefM01(double cadGeorefM01) {
        if (Double.isFinite(cadGeorefM01)) {
            this.cadGeorefM01 = cadGeorefM01;
        }
    }

    public double getCadGeorefM02() {
        return cadGeorefM02;
    }

    public void setCadGeorefM02(double cadGeorefM02) {
        if (Double.isFinite(cadGeorefM02)) {
            this.cadGeorefM02 = cadGeorefM02;
        }
    }

    public double getCadGeorefM10() {
        return cadGeorefM10;
    }

    public void setCadGeorefM10(double cadGeorefM10) {
        if (Double.isFinite(cadGeorefM10)) {
            this.cadGeorefM10 = cadGeorefM10;
        }
    }

    public double getCadGeorefM11() {
        return cadGeorefM11;
    }

    public void setCadGeorefM11(double cadGeorefM11) {
        if (Double.isFinite(cadGeorefM11)) {
            this.cadGeorefM11 = cadGeorefM11;
        }
    }

    public double getCadGeorefM12() {
        return cadGeorefM12;
    }

    public void setCadGeorefM12(double cadGeorefM12) {
        if (Double.isFinite(cadGeorefM12)) {
            this.cadGeorefM12 = cadGeorefM12;
        }
    }

    public void setCadGeoreferenceTransform(String method,
                                            double m00,
                                            double m01,
                                            double m02,
                                            double m10,
                                            double m11,
                                            double m12) {
        setCadGeoreferenceMethod(method);
        setCadGeorefM00(m00);
        setCadGeorefM01(m01);
        setCadGeorefM02(m02);
        setCadGeorefM10(m10);
        setCadGeorefM11(m11);
        setCadGeorefM12(m12);
    }

    public double getCadGeorefResidualMean() {
        return cadGeorefResidualMean;
    }

    public void setCadGeorefResidualMean(double cadGeorefResidualMean) {
        this.cadGeorefResidualMean = Double.isFinite(cadGeorefResidualMean) ? cadGeorefResidualMean : Double.NaN;
    }

    public double getCadGeorefResidualMax() {
        return cadGeorefResidualMax;
    }

    public void setCadGeorefResidualMax(double cadGeorefResidualMax) {
        this.cadGeorefResidualMax = Double.isFinite(cadGeorefResidualMax) ? cadGeorefResidualMax : Double.NaN;
    }

    public int getCadGeorefReferenceCount() {
        return Math.max(0, cadGeorefReferenceCount);
    }

    public void setCadGeorefReferenceCount(int cadGeorefReferenceCount) {
        this.cadGeorefReferenceCount = Math.max(0, cadGeorefReferenceCount);
    }

    public int getCadGeorefCheckCount() {
        return Math.max(0, cadGeorefCheckCount);
    }

    public void setCadGeorefCheckCount(int cadGeorefCheckCount) {
        this.cadGeorefCheckCount = Math.max(0, cadGeorefCheckCount);
    }

    public void setCadGeoreferenceDiagnostics(double residualMean,
                                              double residualMax,
                                              int referenceCount,
                                              int checkCount) {
        setCadGeorefResidualMean(residualMean);
        setCadGeorefResidualMax(residualMax);
        setCadGeorefReferenceCount(referenceCount);
        setCadGeorefCheckCount(checkCount);
    }

    public boolean hasCadGeoreferenceResidualCheck() {
        return getCadGeorefCheckCount() > 0
                && Double.isFinite(cadGeorefResidualMean)
                && Double.isFinite(cadGeorefResidualMax);
    }

    public void clearCadGeoreference() {
        cadGeoreferenceMethod = "";
        cadGeorefM00 = 1d;
        cadGeorefM01 = 0d;
        cadGeorefM02 = 0d;
        cadGeorefM10 = 0d;
        cadGeorefM11 = 1d;
        cadGeorefM12 = 0d;
        cadGeorefResidualMean = Double.NaN;
        cadGeorefResidualMax = Double.NaN;
        cadGeorefReferenceCount = 0;
        cadGeorefCheckCount = 0;
    }

    /**
     * Get the CadGeoreference value object for this layer.
     */
    public CadGeoreference getCadGeoref() { return cadGeoref; }

    public boolean hasCadGeoreference() {
        return (cadGeoreferenceMethod != null && !cadGeoreferenceMethod.isBlank())
                || Math.abs(cadGeorefM00 - 1d) > 1e-9
                || Math.abs(cadGeorefM01) > 1e-9
                || Math.abs(cadGeorefM02) > 1e-9
                || Math.abs(cadGeorefM10) > 1e-9
                || Math.abs(cadGeorefM11 - 1d) > 1e-9
                || Math.abs(cadGeorefM12) > 1e-9;
    }

    public Set<String> getCadHiddenInternalLayers() {
        return Collections.unmodifiableSet(cadHiddenInternalLayers);
    }

    public void setCadHiddenInternalLayers(Collection<String> names) {
        cadHiddenInternalLayers.clear();
        if (names == null) {
            return;
        }
        for (String name : names) {
            if (name != null) {
                String trimmed = name.trim();
                if (!trimmed.isBlank()) {
                    cadHiddenInternalLayers.add(trimmed);
                }
            }
        }
    }

    public void setCadHiddenInternalLayersEncoded(String encoded) {
        setCadHiddenInternalLayers(CadLayerSupport.decodeCadLayerNames(encoded));
    }

    public String getCadHiddenInternalLayersEncoded() {
        return CadLayerSupport.encodeCadLayerNames(cadHiddenInternalLayers);
    }

    public boolean hasCadInternalLayerFilter() {
        return !cadHiddenInternalLayers.isEmpty();
    }

    public boolean isCadInternalLayerVisible(String cadLayerName) {
        if (cadLayerName == null || cadLayerName.isBlank()) {
            return true;
        }
        return !cadHiddenInternalLayers.contains(cadLayerName.trim());
    }

    public Map<String, FieldConfig> getFieldConfigs() {
        return fieldConfigs;
    }

    public FieldConfig getOrCreateFieldConfig(String fieldName, String typeName) {
        FieldConfig config = fieldConfigs.get(fieldName);
        if (config == null) {
            config = new FieldConfig(fieldName, typeName);
            fieldConfigs.put(fieldName, config);
        } else if (typeName != null && !typeName.isBlank()) {
            config.setTypeName(typeName);
        }
        return config;
    }

    @Override
    public String toString() {
        return name + (visible ? "" : " [Oculta]");
    }

    /**
     * Get the user data properties map for this layer.
     * Used for arbitrary metadata (e.g., AID/AII area marking, climate attributes).
     */
    public Map<String, Object> getUserData() {
        return userData;
    }

    /**
     * Get a user data property value by key.
     */
    public Object getUserData(String key) {
        return key != null ? userData.get(key) : null;
    }

    /**
     * Set a user data property on this layer.
     */
    public void putUserData(String key, Object value) {
        if (key != null) {
            userData.put(key, value);
        }
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = Math.max(0f, Math.min(1f, opacity));
    }
}
