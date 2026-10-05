package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LayerSldStyleIO {

    private static final String NS_SLD = "http://www.opengis.net/sld";
    private static final String NS_OGC = "http://www.opengis.net/ogc";
    private static final String NS_XLINK = "http://www.w3.org/1999/xlink";

    private LayerSldStyleIO() {
    }

    public static void exportStyle(Layer layer, File file, ShapefileData data) throws Exception {
        if (layer == null) {
            throw new IllegalArgumentException("La capa es obligatoria para exportar estilo.");
        }
        if (file == null) {
            throw new IllegalArgumentException("El archivo SLD es obligatorio.");
        }

        String geometryFamily = resolveGeometryFamily(layer, data);
        if (geometryFamily.isBlank()) {
            throw new IllegalArgumentException("No se pudo determinar la geometria de la capa para exportar el estilo.");
        }

        Document document = newDocument();
        Element root = document.createElementNS(NS_SLD, "StyledLayerDescriptor");
        root.setAttribute("version", "1.0.0");
        root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns", NS_SLD);
        root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:ogc", NS_OGC);
        root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xlink", NS_XLINK);
        document.appendChild(root);

        Element namedLayer = document.createElementNS(NS_SLD, "NamedLayer");
        root.appendChild(namedLayer);
        appendTextElement(document, namedLayer, NS_SLD, "Name", layer.getName());

        Element userStyle = document.createElementNS(NS_SLD, "UserStyle");
        namedLayer.appendChild(userStyle);
        appendTextElement(document, userStyle, NS_SLD, "Title", layer.getName() + " | CATGIS");

        Element featureTypeStyle = document.createElementNS(NS_SLD, "FeatureTypeStyle");
        userStyle.appendChild(featureTypeStyle);

        CategorizedSymbology categorized = resolveCategorizedSymbology(layer, geometryFamily);
        if (categorized != null && categorized.isConfigured()) {
            for (CategoryStyleRule rule : categorized.getRules().values()) {
                featureTypeStyle.appendChild(buildRuleElement(document, geometryFamily, layer, categorized, rule));
            }
        } else {
            featureTypeStyle.appendChild(buildRuleElement(document, geometryFamily, layer, null, null));
        }

        writeDocument(document, file);
    }

    public static StyleImportResult importStyle(Layer layer, File file, ShapefileData data) throws Exception {
        if (layer == null) {
            throw new IllegalArgumentException("La capa es obligatoria para importar estilo.");
        }
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("No existe el archivo SLD seleccionado.");
        }

        Document document = newDocumentBuilder().parse(file);
        document.getDocumentElement().normalize();

        List<ParsedRule> parsedRules = parseRules(document);
        if (parsedRules.isEmpty()) {
            throw new IllegalArgumentException("El SLD no contiene reglas o simbolizadores compatibles.");
        }

        String fallbackGeometry = resolveGeometryFamily(layer, data);
        String geometryFamily = fallbackGeometry;
        for (ParsedRule parsedRule : parsedRules) {
            if (parsedRule.geometryFamily() != null && !parsedRule.geometryFamily().isBlank()) {
                geometryFamily = parsedRule.geometryFamily();
                break;
            }
        }
        if (geometryFamily == null || geometryFamily.isBlank()) {
            throw new IllegalArgumentException("No se pudo determinar la geometria principal del estilo SLD.");
        }

        clearCategorizedSymbologies(layer);
        ImportMode mode = resolveImportMode(parsedRules);
        if (mode == ImportMode.CATEGORIZED) {
            CategorizedSymbology target = resolveCategorizedSymbology(layer, geometryFamily);
            if (target == null) {
                throw new IllegalArgumentException("La capa no soporta simbologia categorizada para la geometria detectada.");
            }
            ParsedRule first = parsedRules.get(0);
            target.setFieldName(first.filterField());
            target.setLegendTitle(first.legendTitle().isBlank() ? "Categorias" : first.legendTitle());
            target.setLegendSubtitle(first.legendSubtitle().isBlank() ? "Clasificacion por campo" : first.legendSubtitle());
            for (ParsedRule parsedRule : parsedRules) {
                CategoryStyleRule targetRule = target.getOrCreateRule(parsedRule.filterValue());
                applyParsedRuleToCategory(targetRule, parsedRule, geometryFamily);
            }
            return new StyleImportResult(true, geometryFamily, parsedRules.size(), target.getFieldName());
        }

        applyParsedRuleToLayer(layer, parsedRules.get(0), geometryFamily);
        return new StyleImportResult(false, geometryFamily, parsedRules.size(), "");
    }

    private static ImportMode resolveImportMode(List<ParsedRule> parsedRules) {
        if (parsedRules == null || parsedRules.size() < 2) {
            return ImportMode.SIMPLE;
        }
        String fieldName = "";
        for (ParsedRule parsedRule : parsedRules) {
            if (parsedRule.filterField() == null || parsedRule.filterField().isBlank()) {
                return ImportMode.SIMPLE;
            }
            if (fieldName.isBlank()) {
                fieldName = parsedRule.filterField();
            } else if (!fieldName.equalsIgnoreCase(parsedRule.filterField())) {
                return ImportMode.SIMPLE;
            }
        }
        return fieldName.isBlank() ? ImportMode.SIMPLE : ImportMode.CATEGORIZED;
    }

    private static Element buildRuleElement(Document document,
                                            String geometryFamily,
                                            Layer layer,
                                            CategorizedSymbology categorized,
                                            CategoryStyleRule categoryRule) {
        Element rule = document.createElementNS(NS_SLD, "Rule");
        boolean categorizedMode = categorized != null && categoryRule != null;
        String ruleName = categorizedMode ? categoryRule.getValue() : "Estilo base";
        appendTextElement(document, rule, NS_SLD, "Name", ruleName);
        appendTextElement(document, rule, NS_SLD, "Title", categorizedMode ? categorized.getLegendTitle() : layer.getName());

        if (categorizedMode) {
            Element filter = document.createElementNS(NS_OGC, "ogc:Filter");
            Element propertyEquals = document.createElementNS(NS_OGC, "ogc:PropertyIsEqualTo");
            appendTextElement(document, propertyEquals, NS_OGC, "ogc:PropertyName", categorized.getFieldName());
            appendTextElement(document, propertyEquals, NS_OGC, "ogc:Literal", categoryRule.getValue());
            filter.appendChild(propertyEquals);
            rule.appendChild(filter);
        }

        switch (geometryFamily.toUpperCase(Locale.ROOT)) {
            case "POINT" -> rule.appendChild(buildPointSymbolizer(document, layer, categoryRule));
            case "LINE" -> rule.appendChild(buildLineSymbolizer(document, layer, categoryRule));
            case "POLYGON" -> rule.appendChild(buildPolygonSymbolizer(document, layer, categoryRule));
            default -> {
            }
        }
        return rule;
    }

    private static Element buildPointSymbolizer(Document document, Layer layer, CategoryStyleRule rule) {
        Element symbolizer = document.createElementNS(NS_SLD, "PointSymbolizer");
        Element graphic = document.createElementNS(NS_SLD, "Graphic");
        symbolizer.appendChild(graphic);

        String pointGraphic = layer.getPointGraphicSymbol();
        if (pointGraphic != null && !pointGraphic.isBlank()) {
            Element externalGraphic = document.createElementNS(NS_SLD, "ExternalGraphic");
            Element onlineResource = document.createElementNS(NS_SLD, "OnlineResource");
            onlineResource.setAttributeNS(NS_XLINK, "xlink:type", "simple");
            onlineResource.setAttributeNS(NS_XLINK, "xlink:href", pointGraphic);
            externalGraphic.appendChild(onlineResource);
            appendTextElement(document, externalGraphic, NS_SLD, "Format", guessGraphicFormat(pointGraphic));
            graphic.appendChild(externalGraphic);
            appendVendorOption(document, symbolizer, "catgis-point-graphic", pointGraphic);
        } else {
            Element mark = document.createElementNS(NS_SLD, "Mark");
            appendTextElement(document, mark, NS_SLD, "WellKnownName", toWellKnownName(rule != null ? rule.getPointSymbolStyle() : layer.getPointSymbolStyle()));
            mark.appendChild(buildFillElement(document, rule != null ? rule.getPrimaryColor() : layer.getPointColor()));
            mark.appendChild(buildStrokeElement(document,
                    darkerColor(rule != null ? rule.getPrimaryColor() : layer.getPointColor()),
                    1.2f,
                    Layer.LineSymbolStyle.SOLID));
            graphic.appendChild(mark);
        }

        appendTextElement(document, graphic, NS_SLD, "Size",
                String.valueOf(rule != null ? rule.getPointSize() : layer.getPointSize()));
        appendVendorOption(document, symbolizer, "catgis-point-style",
                String.valueOf(rule != null ? rule.getPointSymbolStyle().name() : layer.getPointSymbolStyle().name()));
        return symbolizer;
    }

    private static Element buildLineSymbolizer(Document document, Layer layer, CategoryStyleRule rule) {
        Element symbolizer = document.createElementNS(NS_SLD, "LineSymbolizer");
        Color color = rule != null ? rule.getPrimaryColor() : layer.getLineColor();
        float width = rule != null ? rule.getLineWidth() : layer.getLineWidth();
        Layer.LineSymbolStyle style = rule != null ? rule.getLineStyle() : layer.getLineSymbolStyle();
        symbolizer.appendChild(buildStrokeElement(document, color, width, style));
        appendVendorOption(document, symbolizer, "catgis-line-style", style.name());
        return symbolizer;
    }

    private static Element buildPolygonSymbolizer(Document document, Layer layer, CategoryStyleRule rule) {
        Element symbolizer = document.createElementNS(NS_SLD, "PolygonSymbolizer");
        Color fill = rule != null ? rule.getPrimaryColor() : layer.getFillColor();
        Color border = rule != null ? rule.getSecondaryColor() : layer.getBorderColor();
        float width = rule != null ? rule.getLineWidth() : layer.getLineWidth();
        Layer.PolygonFillStyle fillStyle = rule != null ? rule.getPolygonFillStyle() : layer.getPolygonFillStyle();

        symbolizer.appendChild(buildFillElement(document, fill));
        symbolizer.appendChild(buildStrokeElement(document, border, width, layer.getLineSymbolStyle()));
        appendVendorOption(document, symbolizer, "catgis-polygon-fill-style", fillStyle.name());
        return symbolizer;
    }

    private static Element buildFillElement(Document document, Color color) {
        Element fill = document.createElementNS(NS_SLD, "Fill");
        Color safe = safeColor(color, new Color(120, 170, 255, 120));
        appendCssParameter(document, fill, "fill", toHexRgb(safe));
        appendCssParameter(document, fill, "fill-opacity", formatOpacity(safe));
        return fill;
    }

    private static Element buildStrokeElement(Document document, Color color, float width, Layer.LineSymbolStyle style) {
        Element stroke = document.createElementNS(NS_SLD, "Stroke");
        Color safe = safeColor(color, new Color(30, 41, 59));
        appendCssParameter(document, stroke, "stroke", toHexRgb(safe));
        appendCssParameter(document, stroke, "stroke-opacity", formatOpacity(safe));
        appendCssParameter(document, stroke, "stroke-width", formatFloat(width));
        String dashArray = toDashArray(style);
        if (!dashArray.isBlank()) {
            appendCssParameter(document, stroke, "stroke-dasharray", dashArray);
        }
        return stroke;
    }

    private static void appendCssParameter(Document document, Element parent, String name, String value) {
        Element parameter = document.createElementNS(NS_SLD, "CssParameter");
        parameter.setAttribute("name", name);
        parameter.setTextContent(value);
        parent.appendChild(parameter);
    }

    private static void appendVendorOption(Document document, Element parent, String name, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        Element option = document.createElementNS(NS_SLD, "VendorOption");
        option.setAttribute("name", name);
        option.setTextContent(value);
        parent.appendChild(option);
    }

    private static void appendTextElement(Document document, Element parent, String namespace, String tagName, String text) {
        Element child = document.createElementNS(namespace, tagName);
        child.setTextContent(text != null ? text : "");
        parent.appendChild(child);
    }

    private static List<ParsedRule> parseRules(Document document) {
        List<ParsedRule> parsedRules = new ArrayList<>();
        NodeList rules = document.getElementsByTagNameNS("*", "Rule");
        for (int i = 0; i < rules.getLength(); i++) {
            Node node = rules.item(i);
            if (!(node instanceof Element ruleElement)) {
                continue;
            }
            ParsedRule parsed = parseRule(ruleElement);
            if (parsed != null) {
                parsedRules.add(parsed);
            }
        }
        return parsedRules;
    }

    private static ParsedRule parseRule(Element ruleElement) {
        String title = childText(ruleElement, "Title");
        String filterField = "";
        String filterValue = "";
        Element filter = firstChild(ruleElement, "Filter");
        if (filter != null) {
            Element equalTo = firstChild(filter, "PropertyIsEqualTo");
            if (equalTo != null) {
                filterField = childText(equalTo, "PropertyName");
                filterValue = childText(equalTo, "Literal");
            }
        }

        Element pointSymbolizer = firstChild(ruleElement, "PointSymbolizer");
        if (pointSymbolizer != null) {
            GeometryStyle style = parsePointStyle(pointSymbolizer);
            return new ParsedRule("POINT", title, "", filterField, filterValue, style);
        }

        Element lineSymbolizer = firstChild(ruleElement, "LineSymbolizer");
        if (lineSymbolizer != null) {
            GeometryStyle style = parseLineStyle(lineSymbolizer);
            return new ParsedRule("LINE", title, "", filterField, filterValue, style);
        }

        Element polygonSymbolizer = firstChild(ruleElement, "PolygonSymbolizer");
        if (polygonSymbolizer != null) {
            GeometryStyle style = parsePolygonStyle(polygonSymbolizer);
            return new ParsedRule("POLYGON", title, "", filterField, filterValue, style);
        }
        return null;
    }

    private static GeometryStyle parsePointStyle(Element symbolizer) {
        GeometryStyle style = new GeometryStyle();
        style.primaryColor = parseColorParameter(firstDescendant(symbolizer, "Fill"), "fill", new Color(59, 130, 246));
        style.secondaryColor = parseColorParameter(firstDescendant(symbolizer, "Stroke"), "stroke", darkerColor(style.primaryColor));
        style.pointSize = parseInteger(childText(firstDescendant(symbolizer, "Graphic"), "Size"), 9);
        style.pointSymbolStyle = parsePointStyleName(symbolizer);
        style.pointGraphic = parsePointGraphic(symbolizer);
        return style;
    }

    private static GeometryStyle parseLineStyle(Element symbolizer) {
        GeometryStyle style = new GeometryStyle();
        Element stroke = firstChild(symbolizer, "Stroke");
        style.primaryColor = parseColorParameter(stroke, "stroke", new Color(220, 38, 38));
        style.lineWidth = parseFloat(cssValue(stroke, "stroke-width"), 1.5f);
        style.lineStyle = parseLineStyleName(symbolizer, stroke);
        return style;
    }

    private static GeometryStyle parsePolygonStyle(Element symbolizer) {
        GeometryStyle style = new GeometryStyle();
        Element fill = firstChild(symbolizer, "Fill");
        Element stroke = firstChild(symbolizer, "Stroke");
        style.primaryColor = parseColorParameter(fill, "fill", new Color(120, 170, 255, 120));
        style.secondaryColor = parseColorParameter(stroke, "stroke", new Color(30, 41, 59));
        style.lineWidth = parseFloat(cssValue(stroke, "stroke-width"), 1.5f);
        style.lineStyle = parseLineStyleName(symbolizer, stroke);
        style.polygonFillStyle = parsePolygonFillStyle(symbolizer);
        return style;
    }

    private static Layer.PointSymbolStyle parsePointStyleName(Element symbolizer) {
        String vendor = vendorOption(symbolizer, "catgis-point-style");
        if (!vendor.isBlank()) {
            return Layer.PointSymbolStyle.fromValue(vendor);
        }
        String wellKnownName = childText(firstDescendant(symbolizer, "Mark"), "WellKnownName").toLowerCase(Locale.ROOT);
        return switch (wellKnownName) {
            case "square" -> Layer.PointSymbolStyle.SQUARE;
            case "triangle" -> Layer.PointSymbolStyle.TRIANGLE;
            case "star" -> Layer.PointSymbolStyle.STAR;
            case "cross" -> Layer.PointSymbolStyle.TARGET;
            case "x" -> Layer.PointSymbolStyle.DIAMOND;
            default -> Layer.PointSymbolStyle.CIRCLE;
        };
    }

    private static String parsePointGraphic(Element symbolizer) {
        String vendor = vendorOption(symbolizer, "catgis-point-graphic");
        if (!vendor.isBlank()) {
            return vendor;
        }
        Element external = firstDescendant(symbolizer, "ExternalGraphic");
        if (external == null) {
            return "";
        }
        Element resource = firstDescendant(external, "OnlineResource");
        if (resource == null) {
            return "";
        }
        String href = resource.getAttributeNS(NS_XLINK, "href");
        if (href == null || href.isBlank()) {
            href = resource.getAttribute("xlink:href");
        }
        return href != null ? href.trim() : "";
    }

    private static Layer.LineSymbolStyle parseLineStyleName(Element symbolizer, Element stroke) {
        String vendor = vendorOption(symbolizer, "catgis-line-style");
        if (!vendor.isBlank()) {
            return Layer.LineSymbolStyle.fromValue(vendor);
        }
        String dashArray = cssValue(stroke, "stroke-dasharray");
        if (dashArray == null || dashArray.isBlank()) {
            return Layer.LineSymbolStyle.SOLID;
        }
        String normalized = dashArray.replace(',', ' ').trim().replaceAll("\\s+", " ");
        return switch (normalized) {
            case "12 8" -> Layer.LineSymbolStyle.DASHED;
            case "2 8" -> Layer.LineSymbolStyle.DOTTED;
            case "12 6 2 6" -> Layer.LineSymbolStyle.DASH_DOT;
            default -> Layer.LineSymbolStyle.DASHED;
        };
    }

    private static Layer.PolygonFillStyle parsePolygonFillStyle(Element symbolizer) {
        String vendor = vendorOption(symbolizer, "catgis-polygon-fill-style");
        if (!vendor.isBlank()) {
            return Layer.PolygonFillStyle.fromValue(vendor);
        }
        return Layer.PolygonFillStyle.SOLID;
    }

    private static void applyParsedRuleToCategory(CategoryStyleRule targetRule, ParsedRule parsedRule, String geometryFamily) {
        if (targetRule == null || parsedRule == null) {
            return;
        }
        GeometryStyle style = parsedRule.style();
        if (style == null) {
            return;
        }
        switch (geometryFamily.toUpperCase(Locale.ROOT)) {
            case "POINT" -> {
                targetRule.setPrimaryColor(style.primaryColor);
                targetRule.setPointSymbolStyle(style.pointSymbolStyle);
                targetRule.setPointSize(style.pointSize);
            }
            case "LINE" -> {
                targetRule.setPrimaryColor(style.primaryColor);
                targetRule.setLineStyle(style.lineStyle);
                targetRule.setLineWidth(style.lineWidth);
            }
            case "POLYGON" -> {
                targetRule.setPrimaryColor(style.primaryColor);
                targetRule.setSecondaryColor(style.secondaryColor);
                targetRule.setLineWidth(style.lineWidth);
                targetRule.setPolygonFillStyle(style.polygonFillStyle);
            }
            default -> {
            }
        }
    }

    private static void applyParsedRuleToLayer(Layer layer, ParsedRule parsedRule, String geometryFamily) {
        if (layer == null || parsedRule == null || parsedRule.style() == null) {
            return;
        }
        GeometryStyle style = parsedRule.style();
        switch (geometryFamily.toUpperCase(Locale.ROOT)) {
            case "POINT" -> {
                layer.setPointColor(style.primaryColor);
                layer.setPointSymbolStyle(style.pointSymbolStyle);
                layer.setPointSize(style.pointSize);
                layer.setPointGraphicSymbol(style.pointGraphic);
            }
            case "LINE" -> {
                layer.setLineColor(style.primaryColor);
                layer.setLineWidth(style.lineWidth);
                layer.setLineSymbolStyle(style.lineStyle);
            }
            case "POLYGON" -> {
                layer.setFillColor(style.primaryColor);
                layer.setBorderColor(style.secondaryColor);
                layer.setLineWidth(style.lineWidth);
                layer.setLineSymbolStyle(style.lineStyle);
                layer.setPolygonFillStyle(style.polygonFillStyle);
            }
            default -> {
            }
        }
    }

    private static void clearCategorizedSymbologies(Layer layer) {
        if (layer == null) {
            return;
        }
        resetCategorized(layer.getPointCategorizedSymbology());
        resetCategorized(layer.getLineCategorizedSymbology());
        resetCategorized(layer.getPolygonCategorizedSymbology());
    }

    private static void resetCategorized(CategorizedSymbology symbology) {
        if (symbology == null) {
            return;
        }
        symbology.clearRules();
        symbology.setFieldName("");
    }

    private static CategorizedSymbology resolveCategorizedSymbology(Layer layer, String geometryFamily) {
        if (layer == null || geometryFamily == null) {
            return null;
        }
        return switch (geometryFamily.toUpperCase(Locale.ROOT)) {
            case "POINT" -> layer.getPointCategorizedSymbology();
            case "LINE" -> layer.getLineCategorizedSymbology();
            case "POLYGON" -> layer.getPolygonCategorizedSymbology();
            default -> null;
        };
    }

    private static String resolveGeometryFamily(Layer layer, ShapefileData data) {
        String fromData = VectorLayerUtils.resolveGeometryFamily(data);
        if (!fromData.isBlank()) {
            return fromData;
        }
        String type = layer != null ? layer.getType() : "";
        String normalized = type != null ? type.toUpperCase(Locale.ROOT) : "";
        if (normalized.contains("POLYGON")) {
            return "POLYGON";
        }
        if (normalized.contains("LINE")) {
            return "LINE";
        }
        if (normalized.contains("POINT")) {
            return "POINT";
        }
        return "";
    }

    private static String guessGraphicFormat(String reference) {
        String lower = reference != null ? reference.toLowerCase(Locale.ROOT) : "";
        if (lower.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".bmp")) {
            return "image/bmp";
        }
        return "image/png";
    }

    private static String toWellKnownName(Layer.PointSymbolStyle style) {
        if (style == null) {
            return "circle";
        }
        return switch (style) {
            case SQUARE -> "square";
            case TRIANGLE -> "triangle";
            case STAR -> "star";
            case TARGET -> "cross";
            case DIAMOND -> "x";
            default -> "circle";
        };
    }

    private static String toDashArray(Layer.LineSymbolStyle style) {
        if (style == null) {
            return "";
        }
        return switch (style) {
            case DASHED -> "12 8";
            case DOTTED -> "2 8";
            case DASH_DOT -> "12 6 2 6";
            default -> "";
        };
    }

    private static String toHexRgb(Color color) {
        Color safe = safeColor(color, Color.WHITE);
        return String.format("#%02X%02X%02X", safe.getRed(), safe.getGreen(), safe.getBlue());
    }

    private static String formatOpacity(Color color) {
        Color safe = safeColor(color, Color.WHITE);
        return formatFloat(safe.getAlpha() / 255f);
    }

    private static String formatFloat(float value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static Color safeColor(Color color, Color fallback) {
        return color != null ? color : fallback;
    }

    private static Color darkerColor(Color color) {
        Color safe = safeColor(color, new Color(59, 130, 246));
        return new Color(
                Math.max(0, safe.getRed() - 45),
                Math.max(0, safe.getGreen() - 45),
                Math.max(0, safe.getBlue() - 45),
                safe.getAlpha()
        );
    }

    private static Color parseColorParameter(Element parent, String name, Color fallback) {
        String value = cssValue(parent, name);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            String hex = value.trim().replace("#", "");
            if (hex.length() != 6) {
                return fallback;
            }
            int red = Integer.parseInt(hex.substring(0, 2), 16);
            int green = Integer.parseInt(hex.substring(2, 4), 16);
            int blue = Integer.parseInt(hex.substring(4, 6), 16);
            float opacity = parseFloat(cssValue(parent, name.contains("fill") ? "fill-opacity" : "stroke-opacity"), 1f);
            int alpha = Math.max(0, Math.min(255, Math.round(opacity * 255f)));
            return new Color(red, green, blue, alpha);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static String cssValue(Element parent, String name) {
        if (parent == null || name == null || name.isBlank()) {
            return "";
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (!(node instanceof Element child)) {
                continue;
            }
            String localName = child.getLocalName() != null ? child.getLocalName() : child.getNodeName();
            if (!"CssParameter".equalsIgnoreCase(localName) && !"SvgParameter".equalsIgnoreCase(localName)) {
                continue;
            }
            if (name.equalsIgnoreCase(child.getAttribute("name"))) {
                return child.getTextContent() != null ? child.getTextContent().trim() : "";
            }
        }
        return "";
    }

    private static String vendorOption(Element parent, String name) {
        if (parent == null || name == null || name.isBlank()) {
            return "";
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (!(node instanceof Element child)) {
                continue;
            }
            String localName = child.getLocalName() != null ? child.getLocalName() : child.getNodeName();
            if (!"VendorOption".equalsIgnoreCase(localName)) {
                continue;
            }
            if (name.equalsIgnoreCase(child.getAttribute("name"))) {
                return child.getTextContent() != null ? child.getTextContent().trim() : "";
            }
        }
        return "";
    }

    private static Element firstChild(Element parent, String localName) {
        if (parent == null || localName == null || localName.isBlank()) {
            return null;
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element child) {
                String currentName = child.getLocalName() != null ? child.getLocalName() : child.getNodeName();
                if (localName.equalsIgnoreCase(currentName) || currentName.endsWith(":" + localName)) {
                    return child;
                }
            }
        }
        return null;
    }

    private static Element firstDescendant(Element parent, String localName) {
        if (parent == null || localName == null || localName.isBlank()) {
            return null;
        }
        NodeList nodes = parent.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() == 0) {
            return null;
        }
        Node node = nodes.item(0);
        return node instanceof Element ? (Element) node : null;
    }

    private static String childText(Element parent, String localName) {
        Element child = firstChild(parent, localName);
        if (child == null || child.getTextContent() == null) {
            return "";
        }
        return child.getTextContent().trim();
    }

    private static int parseInteger(String text, int fallback) {
        try {
            return text != null && !text.isBlank() ? Integer.parseInt(text.trim()) : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static float parseFloat(String text, float fallback) {
        try {
            return text != null && !text.isBlank() ? Float.parseFloat(text.trim()) : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static Document newDocument() throws Exception {
        return newDocumentBuilder().newDocument();
    }

    private static DocumentBuilder newDocumentBuilder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory.newDocumentBuilder();
    }

    private static void writeDocument(Document document, File file) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(new DOMSource(document), new StreamResult(file));
    }

    private enum ImportMode {
        SIMPLE,
        CATEGORIZED
    }

    private static final class GeometryStyle {
        private Color primaryColor = new Color(59, 130, 246);
        private Color secondaryColor = new Color(30, 41, 59);
        private Layer.LineSymbolStyle lineStyle = Layer.LineSymbolStyle.SOLID;
        private float lineWidth = 1.5f;
        private Layer.PolygonFillStyle polygonFillStyle = Layer.PolygonFillStyle.SOLID;
        private Layer.PointSymbolStyle pointSymbolStyle = Layer.PointSymbolStyle.CIRCLE;
        private int pointSize = 9;
        private String pointGraphic = "";
    }

    private record ParsedRule(String geometryFamily,
                              String legendTitle,
                              String legendSubtitle,
                              String filterField,
                              String filterValue,
                              GeometryStyle style) {
    }

    public record StyleImportResult(boolean categorized, String geometryFamily, int ruleCount, String fieldName) {
    }
}
