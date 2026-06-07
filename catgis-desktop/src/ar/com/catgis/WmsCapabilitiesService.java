package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

import org.locationtech.jts.geom.Envelope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class WmsCapabilitiesService {

    private static final String USER_AGENT = "CATGIS Desktop/1.0 (+wms)";

    private WmsCapabilitiesService() {
    }

    public static WmsCapabilities fetchCapabilities(String baseUrl) throws Exception {
        String url = buildCapabilitiesUrl(baseUrl);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(9000);
        connection.setReadTimeout(15000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Accept", "application/xml,text/xml,*/*;q=0.8");

        int status = connection.getResponseCode();
        if (status < 200 || status >= 300) {
            throw new IllegalArgumentException("El servicio respondio con codigo HTTP " + status + ".");
        }

        try (InputStream input = connection.getInputStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            Document document = factory.newDocumentBuilder().parse(input);
            Element root = document.getDocumentElement();
            if (root == null) {
                throw new IllegalArgumentException("La respuesta del servicio no contiene XML valido.");
            }

            String rootName = root.getTagName() != null ? root.getTagName().toLowerCase(Locale.ROOT) : "";
            if (!rootName.contains("wms_capabilities") && !rootName.contains("wmt_ms_capabilities")) {
                throw new IllegalArgumentException("La URL no parece devolver un GetCapabilities WMS valido.");
            }

            String version = root.getAttribute("version");
            Element serviceNode = child(root, "Service");
            String serviceTitle = text(serviceNode, "Title");
            String serviceAbstract = text(serviceNode, "Abstract");

            Element capabilityNode = child(root, "Capability");
            Element requestNode = child(capabilityNode, "Request");
            Element getMapNode = child(requestNode, "GetMap");

            List<String> formats = new ArrayList<>();
            if (getMapNode != null) {
                NodeList formatNodes = getMapNode.getElementsByTagName("Format");
                for (int i = 0; i < formatNodes.getLength(); i++) {
                    String fmt = formatNodes.item(i).getTextContent();
                    if (fmt != null && !fmt.isBlank() && !formats.contains(fmt.trim())) {
                        formats.add(fmt.trim());
                    }
                }
            }

            Element rootLayer = child(capabilityNode, "Layer");
            List<WmsLayerInfo> layers = new ArrayList<>();
            if (rootLayer != null) {
                parseLayerTree(rootLayer, 0, new LinkedHashSet<>(), new ArrayList<>(), null, layers);
            }

            return new WmsCapabilities(serviceTitle, serviceAbstract, version, formats, layers);
        }
    }

    public static String buildCapabilitiesUrl(String baseUrl) {
        String normalized = normalizeServiceUrl(baseUrl);
        if (normalized.isBlank()) {
            return "";
        }
        String separator = normalized.contains("?") ? (normalized.endsWith("?") || normalized.endsWith("&") ? "" : "&") : "?";
        return normalized + separator + "SERVICE=WMS&REQUEST=GetCapabilities";
    }

    public static String normalizeServiceUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }
        String trimmed = baseUrl.trim();
        int idx = trimmed.indexOf('?');
        if (idx < 0) {
            return trimmed;
        }

        String path = trimmed.substring(0, idx);
        String query = trimmed.substring(idx + 1);
        List<String> kept = new ArrayList<>();
        for (String part : query.split("&")) {
            if (part == null || part.isBlank()) {
                continue;
            }
            String key = part.contains("=") ? part.substring(0, part.indexOf('=')) : part;
            String normalizedKey = key.trim().toUpperCase(Locale.ROOT);
            if ("SERVICE".equals(normalizedKey)
                    || "REQUEST".equals(normalizedKey)
                    || "VERSION".equals(normalizedKey)
                    || "LAYERS".equals(normalizedKey)
                    || "STYLES".equals(normalizedKey)
                    || "FORMAT".equals(normalizedKey)
                    || "TRANSPARENT".equals(normalizedKey)
                    || "WIDTH".equals(normalizedKey)
                    || "HEIGHT".equals(normalizedKey)
                    || "BBOX".equals(normalizedKey)
                    || "CRS".equals(normalizedKey)
                    || "SRS".equals(normalizedKey)) {
                continue;
            }
            kept.add(part);
        }
        if (kept.isEmpty()) {
            return path;
        }
        return path + "?" + String.join("&", kept);
    }

    private static void parseLayerTree(Element layerElement,
                                       int depth,
                                       Set<String> inheritedCrs,
                                       List<WmsStyleInfo> inheritedStyles,
                                       Envelope inheritedGeoBounds,
                                       List<WmsLayerInfo> result) {
        Set<String> currentCrs = new LinkedHashSet<>(inheritedCrs);
        for (String crs : texts(layerElement, "CRS")) {
            addSplitCrs(currentCrs, crs);
        }
        for (String crs : texts(layerElement, "SRS")) {
            addSplitCrs(currentCrs, crs);
        }

        List<WmsStyleInfo> currentStyles = new ArrayList<>(inheritedStyles);
        for (Element styleElement : children(layerElement, "Style")) {
            String styleName = text(styleElement, "Name");
            String styleTitle = text(styleElement, "Title");
            if ((styleName != null && !styleName.isBlank()) || (styleTitle != null && !styleTitle.isBlank())) {
                currentStyles.add(new WmsStyleInfo(styleName, styleTitle));
            }
        }

        Envelope geoBounds = parseGeographicBounds(layerElement);
        if (geoBounds == null) {
            geoBounds = inheritedGeoBounds != null ? new Envelope(inheritedGeoBounds) : null;
        }

        String name = text(layerElement, "Name");
        String title = text(layerElement, "Title");
        if (name != null && !name.isBlank()) {
            WmsLayerInfo info = new WmsLayerInfo(name.trim(), title != null ? title.trim() : name.trim(), depth);
            for (String crs : currentCrs) {
                info.addCrs(crs);
            }
            for (WmsStyleInfo style : currentStyles) {
                info.addStyle(style);
            }
            if (geoBounds != null) {
                info.setGeographicBounds(geoBounds);
            }
            for (Element bbox : children(layerElement, "BoundingBox")) {
                String crs = attrAny(bbox, "CRS", "SRS");
                Envelope env = parseBoundingBoxEnvelope(bbox);
                if (crs != null && !crs.isBlank() && env != null && !env.isNull()) {
                    info.putBoundingBox(crs.trim().toUpperCase(Locale.ROOT), env);
                }
            }
            result.add(info);
        }

        for (Element childLayer : children(layerElement, "Layer")) {
            parseLayerTree(childLayer, depth + 1, currentCrs, currentStyles, geoBounds, result);
        }
    }

    private static Envelope parseGeographicBounds(Element layerElement) {
        Element ex = child(layerElement, "EX_GeographicBoundingBox");
        if (ex != null) {
            try {
                double west = Double.parseDouble(text(ex, "westBoundLongitude"));
                double east = Double.parseDouble(text(ex, "eastBoundLongitude"));
                double south = Double.parseDouble(text(ex, "southBoundLatitude"));
                double north = Double.parseDouble(text(ex, "northBoundLatitude"));
                return new Envelope(west, east, south, north);
            } catch (Exception ignored) {
            }
        }

        Element latLon = child(layerElement, "LatLonBoundingBox");
        if (latLon != null) {
            return parseBoundingBoxEnvelope(latLon);
        }
        return null;
    }

    private static Envelope parseBoundingBoxEnvelope(Element bbox) {
        if (bbox == null) {
            return null;
        }
        try {
            double minX = Double.parseDouble(attrAny(bbox, "minx", "minX"));
            double minY = Double.parseDouble(attrAny(bbox, "miny", "minY"));
            double maxX = Double.parseDouble(attrAny(bbox, "maxx", "maxX"));
            double maxY = Double.parseDouble(attrAny(bbox, "maxy", "maxY"));
            return new Envelope(minX, maxX, minY, maxY);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void addSplitCrs(Set<String> target, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        for (String token : value.trim().split("\\s+")) {
            if (!token.isBlank()) {
                target.add(token.trim().toUpperCase(Locale.ROOT));
            }
        }
    }

    private static String text(Element parent, String childName) {
        Element child = child(parent, childName);
        return child != null ? child.getTextContent() : "";
    }

    private static List<String> texts(Element parent, String childName) {
        List<String> out = new ArrayList<>();
        if (parent == null) {
            return out;
        }
        for (Element child : children(parent, childName)) {
            String text = child.getTextContent();
            if (text != null && !text.isBlank()) {
                out.add(text.trim());
            }
        }
        return out;
    }

    private static Element child(Element parent, String name) {
        if (parent == null) {
            return null;
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element && name.equalsIgnoreCase(node.getNodeName())) {
                return (Element) node;
            }
        }
        return null;
    }

    private static List<Element> children(Element parent, String name) {
        List<Element> out = new ArrayList<>();
        if (parent == null) {
            return out;
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element && name.equalsIgnoreCase(node.getNodeName())) {
                out.add((Element) node);
            }
        }
        return out;
    }

    private static String attrAny(Element element, String... names) {
        if (element == null || names == null) {
            return "";
        }
        for (String name : names) {
            if (name == null) {
                continue;
            }
            String value = element.getAttribute(name);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
