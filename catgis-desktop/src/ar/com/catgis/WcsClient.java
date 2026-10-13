package ar.com.catgis;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * WCS (Web Coverage Service) client for downloading raster data.
 * Supports WCS 1.0.0 and 2.0.1 with format negotiation and pyramid resolution.
 */
public final class WcsClient {

    private WcsClient() {}

    /** Supported output formats in preference order. */
    public static final List<String> SUPPORTED_FORMATS = List.of(
            "image/tiff", "image/geotiff", "application/x-netcdf",
            "image/jpeg2000", "image/png", "image/jpeg"
    );

    public enum WcsVersion { V1_0_0, V2_0_1 }

    public record WcsCoverage(String name, String title, String description,
                               String bbox, String crs, List<String> formats,
                               List<Double> nativeResolution) {
        public WcsCoverage(String name, String title, String desc, String bbox, String crs) {
            this(name, title, desc, bbox, crs, List.of(), List.of());
        }
    }

    /**
     * Get available coverages from a WCS server with version negotiation.
     */
    public static List<WcsCoverage> getCoverages(String serviceUrl) throws Exception {
        // Try WCS 2.0.1 first, fallback to 1.0.0
        try {
            List<WcsCoverage> v2 = getCoveragesVersion(serviceUrl, WcsVersion.V2_0_1);
            if (!v2.isEmpty()) return v2;
            CatgisLogger.debug("WCS: 2.0.1 returned empty, trying 1.0.0 fallback");
            return getCoveragesVersion(serviceUrl, WcsVersion.V1_0_0);
        } catch (Exception e) {
            CatgisLogger.debug("WCS: 2.0.1 failed (" + e.getMessage() + "), trying 1.0.0 fallback");
            return getCoveragesVersion(serviceUrl, WcsVersion.V1_0_0);
        }
    }

    private static List<WcsCoverage> getCoveragesVersion(String serviceUrl, WcsVersion version) throws Exception {
        Document document = fetchCapabilitiesDocument(buildCapabilitiesUrl(serviceUrl, version));
        Element root = document.getDocumentElement();
        if (root == null || !(hasLocalTag(root, "Capabilities")
                || hasLocalTag(root, "WCS_Capabilities"))) {
            throw new IOException("WCS GetCapabilities returned an unexpected XML root.");
        }
        String wantedTag = version == WcsVersion.V2_0_1 ? "CoverageSummary" : "CoverageOffering";
        List<WcsCoverage> coverages = new ArrayList<>();
        NodeList nodes = document.getElementsByTagName("*");
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            boolean isOffering = hasLocalTag(node, wantedTag)
                    || (version == WcsVersion.V1_0_0 && hasLocalTag(node, "CoverageOfferingBrief"));
            if (!(node instanceof Element element) || !isOffering) {
                continue;
            }
            String name = version == WcsVersion.V2_0_1
                    ? childText(element, "CoverageId", "Identifier", "Name")
                    : childText(element, "Name", "Identifier");
            if (name == null || name.isBlank()) {
                continue;
            }
            String title = childText(element, "Title", "Label");
            String description = childText(element, "Abstract", "Description");
            List<String> formats = descendantTexts(element, "Format", "SupportedFormat");
            List<Double> resolutions = new ArrayList<>();
            String nativeResolution = childText(element, "resolution", "NativeResolution");
            if (nativeResolution != null && !nativeResolution.isBlank()) {
                try {
                    resolutions.add(Double.parseDouble(nativeResolution));
                } catch (NumberFormatException malformed) {
                    CatgisLogger.debug("WCS: ignoring malformed native resolution.");
                }
            }
            coverages.add(new WcsCoverage(name, title != null ? title : name,
                    description != null ? description : "", "", "",
                    formats, resolutions));
        }
        return coverages;
    }

    private static boolean hasLocalTag(Node node, String local) {
        if (node == null) {
            return false;
        }
        String name = node.getLocalName();
        if (name == null) {
            name = node.getNodeName();
        }
        int separator = name.lastIndexOf(':');
        if (separator >= 0) {
            name = name.substring(separator + 1);
        }
        return name.equalsIgnoreCase(local);
    }

    private static String childText(Element parent, String... localNames) {
        for (String local : localNames) {
            NodeList children = parent.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child instanceof Element && hasLocalTag(child, local)) {
                    String value = child.getTextContent();
                    if (value != null && !value.isBlank()) {
                        return value.trim();
                    }
                }
            }
        }
        return null;
    }

    private static List<String> descendantTexts(Element parent, String... localNames) {
        List<String> result = new ArrayList<>();
        NodeList descendants = parent.getElementsByTagName("*");
        for (int i = 0; i < descendants.getLength(); i++) {
            Node node = descendants.item(i);
            for (String local : localNames) {
                if (hasLocalTag(node, local)) {
                    String value = node.getTextContent();
                    if (value != null && !value.isBlank()) {
                        result.add(value.trim());
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Download a coverage with automatic format selection.
     */
    public static byte[] downloadCoverage(String serviceUrl, String coverageName,
                                           String bbox, String crs,
                                           int width, int height) throws Exception {
        return downloadCoverage(serviceUrl, coverageName, bbox, crs, width, height, null);
    }

    /**
     * Download a coverage specifying preferred format.
     */
    public static byte[] downloadCoverage(String serviceUrl, String coverageName,
                                           String bbox, String crs,
                                           int width, int height,
                                           String format) throws Exception {
        // Try WCS 2.0.1 first
        try {
            return downloadCoverageVersion(serviceUrl, coverageName, bbox, crs,
                    width, height, format, WcsVersion.V2_0_1);
        } catch (Exception e) {
            return downloadCoverageVersion(serviceUrl, coverageName, bbox, crs,
                    width, height, format, WcsVersion.V1_0_0);
        }
    }

    private static byte[] downloadCoverageVersion(String serviceUrl, String coverageName,
                                                   String bbox, String crs,
                                                   int width, int height,
                                                   String format, WcsVersion version) throws Exception {
        return fetchUrlBytes(buildCoverageRequestUrl(
                serviceUrl, coverageName, bbox, crs, width, height, format, version));
    }

    private static String buildCoverageRequestUrl(String serviceUrl, String coverageName,
                                                  String bbox, String crs,
                                                  int width, int height,
                                                  String format, WcsVersion version) {
        if (bbox == null || bbox.isBlank()) bbox = "-180,-90,180,90";
        if (crs == null || crs.isBlank()) crs = "EPSG:4326";
        if (width <= 0) width = 512;
        if (height <= 0) height = 512;

        String fmt = format == null || format.isBlank() ? "image/tiff" : format;
        return version == WcsVersion.V2_0_1
                ? buildGetCoverageUrl20(serviceUrl, coverageName, bbox, crs, width, height, fmt)
                : buildGetCoverageUrl10(serviceUrl, coverageName, bbox, crs, width, height, fmt);
    }

    /**
     * Download a coverage and save to file, without buffering the raster in RAM.
     */
    public static void downloadCoverageToFile(String serviceUrl, String coverageName,
                                               String bbox, String crs,
                                               int width, int height,
                                               File outputFile) throws Exception {
        downloadCoverageToFile(serviceUrl, coverageName, bbox, crs,
                width, height, null, outputFile);
    }

    /**
     * WCS 2.0.1 -> 1.0.0 fallback with staged disk writes. A failed or
     * truncated response must never replace a previously valid raster.
     */
    public static void downloadCoverageToFile(String serviceUrl, String coverageName,
                                               String bbox, String crs,
                                               int width, int height,
                                               String format, File outputFile) throws Exception {
        if (outputFile == null) {
            throw new IllegalArgumentException("WCS output file is required.");
        }
        Path destination = outputFile.toPath().toAbsolutePath();
        Path parent = destination.getParent();
        if (parent == null || !Files.isDirectory(parent)) {
            throw new IOException("WCS output directory does not exist.");
        }

        Path staging = Files.createTempFile(parent, ".catgis-wcs-", ".part");
        try {
            try {
                streamCoverageVersion(serviceUrl, coverageName, bbox, crs,
                        width, height, format, WcsVersion.V2_0_1, staging);
            } catch (Exception v2Failure) {
                try {
                    streamCoverageVersion(serviceUrl, coverageName, bbox, crs,
                            width, height, format, WcsVersion.V1_0_0, staging);
                } catch (Exception v1Failure) {
                    v1Failure.addSuppressed(v2Failure);
                    throw v1Failure;
                }
            }

            try {
                Files.move(staging, destination, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(staging, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(staging);
        }
    }

    private static void streamCoverageVersion(String serviceUrl, String coverageName,
                                              String bbox, String crs,
                                              int width, int height, String format,
                                              WcsVersion version, Path staging) throws Exception {
        URL url = new URL(buildCoverageRequestUrl(
                serviceUrl, coverageName, bbox, crs, width, height, format, version));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestProperty("User-Agent", "CATGIS-Desktop/1.0");
            connection.setRequestProperty("Accept", "image/tiff,application/octet-stream,*/*");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(60000);

            int status = connection.getResponseCode();
            if (status != 200) {
                throw new IOException("HTTP " + status + " from WCS server.");
            }
            String type = connection.getContentType();
            String normalizedType = type != null ? type.toLowerCase(Locale.ROOT) : "";
            if (normalizedType.contains("xml") || normalizedType.contains("json")
                    || normalizedType.startsWith("text/")) {
                throw new IOException("WCS returned an error document, not a raster.");
            }

            long received = 0L;
            try (InputStream input = connection.getInputStream();
                 OutputStream output = Files.newOutputStream(staging,
                         StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                byte[] buffer = new byte[65536];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                    received += count;
                }
            }
            long expected = connection.getContentLengthLong();
            if (received == 0L || (expected >= 0L && received != expected)) {
                throw new IOException("WCS raster response is empty or truncated.");
            }
            // Some WCS providers mislabel XML/JSON exception reports as image/tiff
            // or application/octet-stream. Check a small prefix before publishing.
            byte[] prefix;
            try (InputStream stagedInput = Files.newInputStream(staging)) {
                prefix = stagedInput.readNBytes(256);
            }
            String start = new String(prefix, StandardCharsets.UTF_8)
                    .replace(String.valueOf((char) 0xFEFF), "").stripLeading().toLowerCase(Locale.ROOT);
            if (start.startsWith("<") || start.startsWith("{") || start.startsWith("[")) {
                throw new IOException("WCS returned an error document disguised as raster bytes.");
            }
            // Validate requested GeoTIFF transport before replacing an existing
            // raster. Keep metadata-only reads: full remote raster decode is
            // unbounded and must not be performed here.
            if (expectsTiff(format)) {
                validateStagedTiff(staging, width, height);
            }
        } finally {
            connection.disconnect();
        }
    }

    private static boolean expectsTiff(String requestedFormat) {
        if (requestedFormat == null || requestedFormat.isBlank()) {
            return true;
        }
        String normalized = requestedFormat.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("image/tiff") || normalized.startsWith("image/geotiff")
                || normalized.equals("geotiff");
    }

    static void validateStagedTiff(Path staging, int requestedWidth, int requestedHeight)
            throws IOException {
        byte[] signature;
        try (InputStream input = Files.newInputStream(staging)) {
            signature = input.readNBytes(4);
        }
        if (signature.length != 4) {
            throw new IOException("WCS TIFF header is incomplete.");
        }
        boolean littleEndian = signature[0] == 'I' && signature[1] == 'I';
        boolean bigEndian = signature[0] == 'M' && signature[1] == 'M';
        boolean classic = (littleEndian && signature[2] == 42 && signature[3] == 0)
                || (bigEndian && signature[2] == 0 && signature[3] == 42);
        boolean bigTiff = (littleEndian && signature[2] == 43 && signature[3] == 0)
                || (bigEndian && signature[2] == 0 && signature[3] == 43);
        if (!classic && !bigTiff) {
            throw new IOException("WCS response lacks a recognized TIFF/BigTIFF signature.");
        }

        try (ImageInputStream input = ImageIO.createImageInputStream(staging.toFile())) {
            if (input == null) {
                throw new IOException("WCS TIFF cannot be opened as an image stream.");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IOException("No TIFF image reader supports the staged coverage.");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String detected = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!detected.contains("tif")) {
                    throw new IOException("Staged coverage is not recognized as TIFF.");
                }
                int actualWidth = reader.getWidth(0);
                int actualHeight = reader.getHeight(0);
                if (actualWidth <= 0 || actualHeight <= 0) {
                    throw new IOException("WCS TIFF has invalid raster dimensions.");
                }
                if ((requestedWidth > 0 && actualWidth != requestedWidth)
                        || (requestedHeight > 0 && actualHeight != requestedHeight)) {
                    throw new IOException("WCS TIFF grid dimensions differ from requested width/height.");
                }
            } finally {
                reader.dispose();
            }
        }
    }

    /**
     * Find the best matching format from a list of server-supported formats.
     */
    public static String negotiateFormat(List<String> serverFormats) {
        for (String pref : SUPPORTED_FORMATS) {
            for (String sf : serverFormats) {
                if (sf.equalsIgnoreCase(pref)) return sf;
            }
        }
        return serverFormats.isEmpty() ? "image/tiff" : serverFormats.get(0);
    }

    // --- URL builders ---

    private static String buildCapabilitiesUrl(String serviceUrl, WcsVersion version) {
        String ver = version == WcsVersion.V2_0_1 ? "2.0.1" : "1.0.0";
        if (serviceUrl.toLowerCase().contains("request=")) return serviceUrl;
        String sep = serviceUrl.contains("?") ? "&" : "?";
        return serviceUrl + sep + "SERVICE=WCS&REQUEST=GetCapabilities&VERSION=" + ver;
    }

    private static String buildGetCoverageUrl10(String serviceUrl, String coverage,
                                                 String bbox, String crs,
                                                 int width, int height, String format) {
        String sep = serviceUrl.contains("?") ? "&" : "?";
        return serviceUrl + sep
                + "SERVICE=WCS&VERSION=1.0.0&REQUEST=GetCoverage"
                + "&COVERAGE=" + URLEncoder.encode(coverage, StandardCharsets.UTF_8)
                + "&CRS=" + URLEncoder.encode(crs, StandardCharsets.UTF_8)
                + "&BBOX=" + URLEncoder.encode(bbox, StandardCharsets.UTF_8)
                + "&WIDTH=" + width + "&HEIGHT=" + height
                + "&FORMAT=" + URLEncoder.encode(format, StandardCharsets.UTF_8);
    }

    private static String buildGetCoverageUrl20(String serviceUrl, String coverage,
                                                 String bbox, String crs,
                                                 int width, int height, String format) {
        String sep = serviceUrl.contains("?") ? "&" : "?";
        // WCS 2.0.1 uses subset and scaleSize
        String[] parts = bbox.split(",");
        String subset = "";
        if (parts.length == 4) {
            subset = "&SUBSET=x(" + parts[0] + "," + parts[2] + ")"
                    + "&SUBSET=y(" + parts[1] + "," + parts[3] + ")";
        }
        return serviceUrl + sep
                + "SERVICE=WCS&VERSION=2.0.1&REQUEST=GetCoverage"
                + "&COVERAGEID=" + URLEncoder.encode(coverage, StandardCharsets.UTF_8)
                + subset
                + "&SCALESIZE=x(" + width + ")"
                + "&SCALESIZE=y(" + height + ")"
                + "&FORMAT=" + URLEncoder.encode(format, StandardCharsets.UTF_8);
    }

    // --- HTTP helpers ---

    // Bound remote XML before parsing. A malicious server cannot fill the heap
    // with an arbitrarily large GetCapabilities response.
    private static final int MAX_CAPABILITIES_BYTES = 4 * 1024 * 1024;

    private static Document fetchCapabilitiesDocument(String urlString) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        try {
            connection.setRequestProperty("User-Agent", "CATGIS-Desktop/1.0");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("HTTP " + responseCode + " from WCS server");
            }
            long declaredLength = connection.getContentLengthLong();
            if (declaredLength > MAX_CAPABILITIES_BYTES) {
                throw new IOException("WCS GetCapabilities exceeds 4 MiB.");
            }
            byte[] document;
            try (InputStream input = connection.getInputStream()) {
                document = input.readNBytes(MAX_CAPABILITIES_BYTES + 1);
            }
            if (document.length == 0 || document.length > MAX_CAPABILITIES_BYTES) {
                throw new IOException("WCS GetCapabilities is empty or exceeds 4 MiB.");
            }
            if (declaredLength >= 0L && declaredLength != document.length) {
                throw new IOException("WCS GetCapabilities has incomplete Content-Length.");
            }
            try (InputStream input = new ByteArrayInputStream(document)) {
                return OgcCapabilitiesXml.parse(input, false);
            }
        } finally {
            connection.disconnect();
        }
    }

    private static byte[] fetchUrlBytes(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "CATGIS-Desktop/1.0");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(60000);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) throw new Exception("HTTP " + responseCode + " from WCS server");

        try (InputStream is = conn.getInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) bos.write(buffer, 0, bytesRead);
            conn.disconnect();
            return bos.toByteArray();
        }
    }

}
