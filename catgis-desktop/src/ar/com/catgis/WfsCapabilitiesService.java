package ar.com.catgis;

import org.locationtech.jts.geom.Envelope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class WfsCapabilitiesService {

    private static final String USER_AGENT = "CATGIS Desktop/1.0 (+wfs)";

    private WfsCapabilitiesService() {
    }

    public static WfsCapabilities fetchCapabilities(String baseUrl) throws Exception {
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
            if (!rootName.contains("wfs_capabilities")) {
                throw new IllegalArgumentException("La URL no parece devolver un GetCapabilities WFS valido.");
            }

            String version = root.getAttribute("version");
            String serviceTitle = extractServiceTitle(root);
            String serviceAbstract = extractServiceAbstract(root);
            List<WfsFeatureTypeInfo> featureTypes = parseFeatureTypes(root);

            return new WfsCapabilities(serviceTitle, serviceAbstract, version, featureTypes);
        }
    }

    public static String buildCapabilitiesUrl(String baseUrl) {
        String normalized = normalizeServiceUrl(baseUrl);
        if (normalized.isBlank()) {
            return "";
        }
        String separator = normalized.contains("?") ? (normalized.endsWith("?") || normalized.endsWith("&") ? "" : "&") : "?";
        return normalized + separator + "SERVICE=WFS&REQUEST=GetCapabilities";
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
                    || "TYPENAME".equals(normalizedKey)
                    || "TYPENAMES".equals(normalizedKey)
                    || "OUTPUTFORMAT".equals(normalizedKey)
                    || "SRSNAME".equals(normalizedKey)
                    || "COUNT".equals(normalizedKey)
                    || "MAXFEATURES".equals(normalizedKey)
                    || "STARTINDEX".equals(normalizedKey)
                    || "BBOX".equals(normalizedKey)) {
                continue;
            }
            kept.add(part);
        }
        if (kept.isEmpty()) {
            return path;
        }
        return path + "?" + String.join("&", kept);
    }

    public static String resolveProviderName(String serviceUrl) {
        try {
            URI uri = URI.create(serviceUrl);
            return uri.getHost() != null ? uri.getHost() : "WFS";
        } catch (Exception ignored) {
            return "WFS";
        }
    }

    private static String extractServiceTitle(Element root) {
        Element serviceId = firstDescendant(root, "ServiceIdentification");
        if (serviceId != null) {
            String title = text(serviceId, "Title");
            if (!title.isBlank()) {
                return title;
            }
        }
        Element service = firstDescendant(root, "Service");
        return service != null ? text(service, "Title") : "";
    }

    private static String extractServiceAbstract(Element root) {
        Element serviceId = firstDescendant(root, "ServiceIdentification");
        if (serviceId != null) {
            String abs = text(serviceId, "Abstract");
            if (!abs.isBlank()) {
                return abs;
            }
        }
        Element service = firstDescendant(root, "Service");
        return service != null ? text(service, "Abstract") : "";
    }

    private static List<WfsFeatureTypeInfo> parseFeatureTypes(Element root) {
        List<WfsFeatureTypeInfo> out = new ArrayList<>();
        Element featureTypeList = firstDescendant(root, "FeatureTypeList");
        if (featureTypeList == null) {
            return out;
        }
        for (Element featureType : children(featureTypeList, "FeatureType")) {
            String name = text(featureType, "Name");
            if (name.isBlank()) {
                continue;
            }
            String title = text(featureType, "Title");
            WfsFeatureTypeInfo info = new WfsFeatureTypeInfo(name.trim(), title.trim());
            String defaultCrs = firstNonBlank(
                    text(featureType, "DefaultCRS"),
                    text(featureType, "DefaultSRS"),
                    text(featureType, "SRS")
            );
            info.setDefaultCrs(defaultCrs);

            Set<String> crsCodes = new LinkedHashSet<>();
            addIfPresent(crsCodes, defaultCrs);
            for (String crs : texts(featureType, "OtherCRS")) {
                addIfPresent(crsCodes, crs);
            }
            for (String crs : texts(featureType, "OtherSRS")) {
                addIfPresent(crsCodes, crs);
            }
            for (String crs : texts(featureType, "SRS")) {
                addIfPresent(crsCodes, crs);
            }
            for (String crs : crsCodes) {
                info.addCrs(crs);
            }

            Envelope geographic = parseWgs84Bounds(featureType);
            if (geographic != null) {
                info.setGeographicBounds(geographic);
            }
            out.add(info);
        }
        return out;
    }

    private static Envelope parseWgs84Bounds(Element featureType) {
        Element bbox = firstDescendant(featureType, "WGS84BoundingBox");
        if (bbox != null) {
            try {
                String lower = text(bbox, "LowerCorner");
                String upper = text(bbox, "UpperCorner");
                String[] lowerParts = lower.trim().split("\\s+");
                String[] upperParts = upper.trim().split("\\s+");
                if (lowerParts.length >= 2 && upperParts.length >= 2) {
                    double minX = Double.parseDouble(lowerParts[0]);
                    double minY = Double.parseDouble(lowerParts[1]);
                    double maxX = Double.parseDouble(upperParts[0]);
                    double maxY = Double.parseDouble(upperParts[1]);
                    return new Envelope(minX, maxX, minY, maxY);
                }
            } catch (Exception ignored) { CatgisLogger.warn("WfsCapabilitiesService: operation failed", ignored); }
        }

        Element latLon = firstDescendant(featureType, "LatLongBoundingBox");
        if (latLon != null) {
            try {
                double minX = Double.parseDouble(attrAny(latLon, "minx", "minX"));
                double minY = Double.parseDouble(attrAny(latLon, "miny", "minY"));
                double maxX = Double.parseDouble(attrAny(latLon, "maxx", "maxX"));
                double maxY = Double.parseDouble(attrAny(latLon, "maxy", "maxY"));
                return new Envelope(minX, maxX, minY, maxY);
            } catch (Exception ignored) { CatgisLogger.warn("WfsCapabilitiesService: operation failed", ignored); }
        }
        return null;
    }

    private static void addIfPresent(Set<String> target, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        target.add(CRSDefinitions.normalizeCode(value.trim()));
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private static String text(Element parent, String childName) {
        Element child = child(parent, childName);
        return child != null ? child.getTextContent().trim() : "";
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

    private static Element firstDescendant(Element parent, String name) {
        if (parent == null) {
            return null;
        }
        if (name.equalsIgnoreCase(stripPrefix(parent.getNodeName()))) {
            return parent;
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (!(node instanceof Element)) {
                continue;
            }
            Element found = firstDescendant((Element) node, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static Element child(Element parent, String name) {
        if (parent == null) {
            return null;
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element && name.equalsIgnoreCase(stripPrefix(node.getNodeName()))) {
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
            if (node instanceof Element && name.equalsIgnoreCase(stripPrefix(node.getNodeName()))) {
                out.add((Element) node);
            }
        }
        return out;
    }

    private static String stripPrefix(String nodeName) {
        if (nodeName == null) {
            return "";
        }
        int idx = nodeName.indexOf(':');
        return idx >= 0 ? nodeName.substring(idx + 1) : nodeName;
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
