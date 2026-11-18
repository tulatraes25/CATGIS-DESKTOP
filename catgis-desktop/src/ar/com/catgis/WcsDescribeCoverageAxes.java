package ar.com.catgis;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * T4/#108: WCS 2.x DescribeCoverage axis negotiation.
 *
 * <p>WCS 2.0.1 GetCoverage dimensions are addressed by the coverage's native
 * axis labels, which are not always {@code x}/{@code y} and whose order is not
 * implied by the user EPSG code. This parser resolves the advertised labels from a
 * bounded DescribeCoverage document, maps horizontal components by meaning and
 * fails closed when the axes are ambiguous or unsupported instead of swapping
 * coordinates.</p>
 */
public final class WcsDescribeCoverageAxes {

    static final int MAX_DOCUMENT_BYTES = 4 * 1024 * 1024;

    /** Advertised labels for the two horizontal axes. */
    public record Plan(String crs, String xAxisLabel, String yAxisLabel,
                       List<String> advertisedAxisLabels) {
        public String describe() {
            return "CRS=" + crs + " axisLabels=" + advertisedAxisLabels
                    + " x=" + xAxisLabel + " y=" + yAxisLabel;
        }
    }

    private WcsDescribeCoverageAxes() {}

    public static Plan parse(byte[] document) throws IOException {
        if (document == null || document.length == 0
                || document.length > MAX_DOCUMENT_BYTES) {
            throw new IOException("WCS DescribeCoverage is empty or exceeds 4 MiB.");
        }
        try {
            Document xml = OgcCapabilitiesXml.parse(
                    new ByteArrayInputStream(document), false);
            Element coverage = firstDescendantByLocalName(xml.getDocumentElement(), "CoverageDescription");
            if (coverage == null) {
                throw new IOException("WCS DescribeCoverage lacks CoverageDescription.");
            }
            Element envelope = firstDescendantByLocalName(coverage, "Envelope");
            if (envelope == null) {
                throw new IOException("WCS DescribeCoverage lacks a boundedBy Envelope.");
            }
            String crs = envelope.getAttribute("srsName");
            if (crs == null || crs.isBlank()) {
                throw new IOException("WCS DescribeCoverage Envelope lacks srsName; "
                        + "native CRS unknown.");
            }
            List<String> labels = splitLabels(envelope.getAttribute("axisLabels"));
            if (labels.isEmpty()) {
                labels = splitLabels(descendantAttribute(coverage, "Grid", "axisLabels"));
            }
            if (labels.size() != 2) {
                throw new IOException("WCS coverage advertises " + labels.size()
                        + " axis labels; exactly two horizontal axes are required.");
            }
            String xLabel = findHorizontal(labels, true);
            String yLabel = findHorizontal(labels, false);
            if (xLabel == null || yLabel == null || xLabel.equalsIgnoreCase(yLabel)) {
                throw new IOException("WCS coverage axis labels are ambiguous ("
                        + labels + "); refusing to guess axis order.");
            }
            return new Plan(crs.trim(), xLabel, yLabel, List.copyOf(labels));
        } catch (IOException io) {
            throw io;
        } catch (Exception invalid) {
            throw new IOException("WCS DescribeCoverage XML is invalid.", invalid);
        }
    }

    /**
     * Builds the WCS 2.0.1 GetCoverage dimension parameters with the advertised
     * axis labels. BBOX order is {@code minLon,minLat,maxLon,maxLat}.
     */
    public static String dimensionParameters(Plan plan, String bbox, int width, int height)
            throws IOException {
        if (plan == null) {
            throw new IOException("WCS axis plan is required for WCS 2.0.1 GetCoverage.");
        }
        if (width <= 0 || height <= 0) {
            throw new IOException("WCS requested dimensions must be positive.");
        }
        String[] parts = bbox == null ? new String[0] : bbox.split(",");
        if (parts.length != 4) {
            throw new IOException("WCS BBOX must contain exactly four coordinates.");
        }
        double minLon = parseCoordinate(parts[0]);
        double minLat = parseCoordinate(parts[1]);
        double maxLon = parseCoordinate(parts[2]);
        double maxLat = parseCoordinate(parts[3]);
        if (!(maxLon > minLon) || !(maxLat > minLat)) {
            throw new IOException("WCS BBOX is inverted or empty.");
        }
        String sep = "&";
        return "SUBSET=" + encode(plan.xAxisLabel() + "(" + minLon + "," + maxLon + ")")
                + sep + "SUBSET=" + encode(plan.yAxisLabel() + "(" + minLat + "," + maxLat + ")")
                + sep + "SCALESIZE=" + encode(plan.xAxisLabel() + "(" + width + ")")
                + sep + "SCALESIZE=" + encode(plan.yAxisLabel() + "(" + height + ")");
    }

    private static double parseCoordinate(String text) throws IOException {
        try {
            double value = Double.parseDouble(text.trim());
            if (!Double.isFinite(value)) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException invalid) {
            throw new IOException("WCS BBOX contains a non-finite coordinate: " + text);
        }
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static List<String> splitLabels(String raw) {
        List<String> labels = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return labels;
        }
        for (String part : raw.trim().split("[\\s,]+")) {
            if (!part.isBlank()) {
                labels.add(part.trim());
            }
        }
        return labels;
    }

    private static String findHorizontal(List<String> labels, boolean xAxis) {
        String found = null;
        for (String label : labels) {
            String normalized = label.toLowerCase(Locale.ROOT);
            boolean matches = xAxis
                    ? normalized.equals("x") || normalized.equals("lon") || normalized.equals("long")
                            || normalized.equals("longitude") || normalized.equals("e")
                            || normalized.equals("east") || normalized.equals("easting")
                    : normalized.equals("y") || normalized.equals("lat")
                            || normalized.equals("latitude") || normalized.equals("n")
                            || normalized.equals("north") || normalized.equals("northing");
            if (matches) {
                if (found != null) {
                    return null; // ambiguous duplicate horizontal semantics
                }
                found = label;
            }
        }
        return found;
    }

    private static Element firstByLocalName(Element root, String localName) {
        if (root == null) {
            return null;
        }
        if (localName.equalsIgnoreCase(root.getLocalName()) || localName.equals(root.getNodeName())) {
            return root;
        }
        return null;
    }

    private static Element firstDescendantByLocalName(Element root, String localName) {
        if (root == null) {
            return null;
        }
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) node;
            String name = element.getLocalName() != null
                    ? element.getLocalName() : element.getNodeName();
            if (name != null && name.endsWith(":" + localName)) {
                name = name.substring(name.indexOf(':') + 1);
            }
            if (localName.equalsIgnoreCase(name)) {
                return element;
            }
            Element nested = firstDescendantByLocalName(element, localName);
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    private static String descendantAttribute(Element root, String localName, String attribute) {
        Element element = firstDescendantByLocalName(root, localName);
        return element == null ? "" : element.getAttribute(attribute);
    }
}
