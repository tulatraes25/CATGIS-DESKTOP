package ar.com.catgis;

import java.io.*;
import java.awt.Rectangle;
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
import javax.imageio.ImageReadParam;
import javax.imageio.plugins.tiff.TIFFDirectory;
import javax.imageio.plugins.tiff.TIFFField;
import javax.imageio.stream.ImageInputStream;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.GeneralBounds;
import org.geotools.referencing.CRS;
import org.geotools.util.factory.Hints;
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
                // Request URL defaults nonpositive dimensions to 512.
                // Validate the same effective grid, never the raw caller input:
                // otherwise zero/negative dimensions bypass the TIFF size gate.
                int effectiveWidth = width <= 0 ? 512 : width;
                int effectiveHeight = height <= 0 ? 512 : height;
                validateStagedTiff(staging, effectiveWidth, effectiveHeight);
                // The GUI requests bbox and CRS explicitly. Pixel dimensions
                // alone are insufficient: an unrelated place/projection may
                // otherwise be published as a valid GIS raster.
                validateStagedGeoMetadata(staging, bbox, crs, effectiveWidth, effectiveHeight);
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
        // WCS providers may advertise format=tif or format=geotiff rather
        // than MIME types (e.g. GeoServer WCS). Both still need TIFF checks.
        return normalized.startsWith("image/tiff") || normalized.startsWith("image/geotiff")
                || normalized.startsWith("image/x-tiff") || normalized.startsWith("image/x-geotiff")
                || normalized.startsWith("application/geotiff")
                || normalized.startsWith("application/x-geotiff")
                || normalized.equals("geotiff") || normalized.equals("tif")
                || normalized.equals("tiff") || normalized.equals("geotiff8")
                || normalized.equals("tiff8");
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
                // Header, CRS and dimensions are not evidence of readable pixels.
                // Reject strip/tile pointers past EOF before bounded sample reads.
                validateTiffDataRanges(reader, Files.size(staging));
                validateTiffPixelSamples(reader, actualWidth, actualHeight);
            } finally {
                reader.dispose();
            }
        }
    }

    /**
     * All primary-image TIFF strip or tile payload offsets and byte counts
     * must refer to bytes actually present in the staged response.
     * TIFFDirectory accesses IFD metadata, not the whole raster pixel array.
     */
    private static void validateTiffDataRanges(ImageReader reader, long fileLength)
            throws IOException {
        try {
            TIFFDirectory directory = TIFFDirectory.createFromMetadata(
                    reader.getImageMetadata(0));
            TIFFField stripOffsets = directory.getTIFFField(273);
            TIFFField stripBytes = directory.getTIFFField(279);
            TIFFField tileOffsets = directory.getTIFFField(324);
            TIFFField tileBytes = directory.getTIFFField(325);
            boolean hasStrips = stripOffsets != null || stripBytes != null;
            boolean hasTiles = tileOffsets != null || tileBytes != null;
            if (hasStrips == hasTiles) {
                int[] discoveredTags = java.util.Arrays.stream(directory.getTIFFFields())
                        .mapToInt(TIFFField::getTagNumber).limit(48).toArray();
                throw new IOException("WCS TIFF unexpected storage metadata: hasStrips="
                        + hasStrips + ", hasTiles=" + hasTiles
                        + ", tags=" + java.util.Arrays.toString(discoveredTags));
            }
            if (hasStrips) {
                verifyTiffRanges(stripOffsets, stripBytes, fileLength);
            } else {
                verifyTiffRanges(tileOffsets, tileBytes, fileLength);
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException invalid) {
            throw new IOException("WCS TIFF contains malformed image data offsets.", invalid);
        }
    }

    private static void verifyTiffRanges(TIFFField offsets, TIFFField byteCounts,
                                          long fileLength) throws IOException {
        if (offsets == null || byteCounts == null
                || offsets.getCount() == 0
                || offsets.getCount() != byteCounts.getCount()
                || offsets.getCount() > 2_000_000) {
            throw new IOException("WCS TIFF strip/tile storage metadata is inconsistent.");
        }
        for (int i = 0; i < offsets.getCount(); i++) {
            long from = offsets.getAsLong(i);
            long size = byteCounts.getAsLong(i);
            // Avoid addition overflow. Zero-byte sparse TIFF tiles are
            // deliberately unsupported until their NoData meaning is certified.
            if (from < 8L || size <= 0L || from > fileLength
                    || size > fileLength - from) {
                throw new IOException("WCS TIFF strip/tile data lies outside raster file.");
            }
        }
    }

    /**
     * Small decoded sample regions catch unreadable strips that valid TIFF
     * metadata alone cannot identify. Real TIFF codecs may decompress an
     * entire backing strip internally, so this is not a fixed heap ceiling
     * or full-pixel checksum for arbitrarily large scientific coverages.
     */
    private static void validateTiffPixelSamples(ImageReader reader, int width, int height)
            throws IOException {
        int[][] points = {{0, 0}, {width / 2, height / 2},
                {width - 1, height - 1}};
        for (int[] point : points) {
            ImageReadParam param = reader.getDefaultReadParam();
            param.setSourceRegion(new Rectangle(point[0], point[1], 1, 1));
            try {
                java.awt.image.BufferedImage sample = reader.read(0, param);
                if (sample == null || sample.getWidth() != 1 || sample.getHeight() != 1
                        || sample.getRaster() == null
                        || sample.getRaster().getNumBands() < 1
                        || sample.getRaster().getNumBands() > 256) {
                    throw new IOException("WCS TIFF sample pixel could not be read.");
                }
                // Ensure decoded band storage is truly accessible. NaN values
                // may be legitimate NoData, so do not require finite samples.
                for (int band = 0; band < sample.getRaster().getNumBands(); band++) {
                    sample.getRaster().getSampleDouble(0, 0, band);
                }
            } catch (RuntimeException unreadable) {
                throw new IOException("WCS TIFF has unreadable sampled pixel data.", unreadable);
            }
        }
    }

    /**
     * Validate embedded GeoTIFF CRS and envelope against the WCS request.
     * GeoTools reads the metadata headers only; no coverage or pixel array is
     * decoded. Always dispose the file-backed reader before atomic rename:
     * open handles would prevent rename/replacement on Windows.
     *
     * Caller APIs that specify neither extent nor CRS keep the existing
     * structural TIFF validation for backward compatibility.
     */
    static void validateStagedGeoMetadata(Path staging, String requestedBbox, String requestedCrs,
                                          int requestedWidth, int requestedHeight) throws IOException {
        boolean bboxGiven = requestedBbox != null && !requestedBbox.isBlank();
        boolean crsGiven = requestedCrs != null && !requestedCrs.isBlank();
        if (!bboxGiven && !crsGiven) {
            return;
        }
        double[] bounds = bboxGiven ? parseRequestedBbox(requestedBbox) : null;
        String effectiveCrs = crsGiven ? requestedCrs.trim() : "EPSG:4326";

        GeoTiffReader reader = null;
        try {
            CoordinateReferenceSystem wanted = CRSDefinitions.decode(effectiveCrs, true);
            reader = new GeoTiffReader(staging.toFile(),
                    new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
            CoordinateReferenceSystem embedded = reader.getCoordinateReferenceSystem();
            if (embedded == null || !CRS.equalsIgnoreMetadata(wanted, embedded)) {
                throw new IOException("WCS GeoTIFF CRS differs from requested " + effectiveCrs + ".");
            }

            if (bounds != null) {
                GeneralBounds actual = reader.getOriginalEnvelope();
                if (actual == null || actual.getDimension() != 2) {
                    throw new IOException("WCS GeoTIFF lacks a usable two-dimensional georeferenced envelope.");
                }
                double xTolerance = Math.max(1.0e-8,
                        (bounds[2] - bounds[0]) / Math.max(1, requestedWidth) * 0.51);
                double yTolerance = Math.max(1.0e-8,
                        (bounds[3] - bounds[1]) / Math.max(1, requestedHeight) * 0.51);
                if (!closeGeographicBound(actual.getMinimum(0), bounds[0], xTolerance)
                        || !closeGeographicBound(actual.getMinimum(1), bounds[1], yTolerance)
                        || !closeGeographicBound(actual.getMaximum(0), bounds[2], xTolerance)
                        || !closeGeographicBound(actual.getMaximum(1), bounds[3], yTolerance)) {
                    throw new IOException("WCS GeoTIFF geographic envelope differs from requested BBOX.");
                }
            }
        } catch (IOException error) {
            throw error;
        } catch (Exception error) {
            throw new IOException("Could not verify WCS GeoTIFF CRS/envelope metadata.", error);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }
    }

    private static boolean closeGeographicBound(double actual, double expected, double tolerance) {
        return Double.isFinite(actual) && Math.abs(actual - expected) <= tolerance;
    }

    private static double[] parseRequestedBbox(String bbox) throws IOException {
        String[] parts = bbox.split(",", -1);
        if (parts.length != 4) {
            throw new IOException("WCS BBOX must contain xmin,ymin,xmax,ymax.");
        }
        double[] out = new double[4];
        for (int i = 0; i < parts.length; i++) {
            try {
                out[i] = Double.parseDouble(parts[i].trim());
            } catch (NumberFormatException error) {
                throw new IOException("WCS BBOX has a nonnumeric coordinate.", error);
            }
            if (!Double.isFinite(out[i])) {
                throw new IOException("WCS BBOX has a non-finite coordinate.");
            }
        }
        if (out[2] <= out[0] || out[3] <= out[1]) {
            throw new IOException("WCS BBOX must have strictly positive width and height.");
        }
        return out;
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

    // Returning a byte[] necessarily retains the complete coverage in heap.
    // Large legitimate downloads must use downloadCoverageToFile instead.
    static final int MAX_IN_MEMORY_COVERAGE_BYTES = 32 * 1024 * 1024;

    private static byte[] fetchUrlBytes(String urlString) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        try {
            connection.setRequestProperty("User-Agent", "CATGIS-Desktop/1.0");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(60000);

            int status = connection.getResponseCode();
            if (status != 200) {
                throw new IOException("HTTP " + status + " from WCS server");
            }
            long declaredLength = connection.getContentLengthLong();
            // Reject an oversized declared body before requesting its stream.
            if (declaredLength > MAX_IN_MEMORY_COVERAGE_BYTES) {
                throw new IOException("WCS coverage exceeds 32 MiB in-memory limit; "
                        + "use downloadCoverageToFile.");
            }
            try (InputStream input = connection.getInputStream()) {
                return readBoundedCoverageBytes(input, declaredLength);
            }
        } finally {
            // Previously only the success path disconnected; repeated HTTP
            // failures could retain underlying sockets longer than necessary.
            connection.disconnect();
        }
    }

    static byte[] readBoundedCoverageBytes(InputStream input, long declaredLength)
            throws IOException {
        if (declaredLength > MAX_IN_MEMORY_COVERAGE_BYTES) {
            throw new IOException("WCS coverage exceeds 32 MiB in-memory limit; "
                    + "use downloadCoverageToFile.");
        }
        int initialCapacity = declaredLength > 0
                ? (int) Math.min(declaredLength, 65536L) : 16384;
        ByteArrayOutputStream output = new ByteArrayOutputStream(initialCapacity);
        byte[] buffer = new byte[8192];
        long received = 0L;
        int read;
        while ((read = input.read(buffer)) != -1) {
            if (read == 0) {
                continue;
            }
            received += read;
            if (received > MAX_IN_MEMORY_COVERAGE_BYTES) {
                throw new IOException("WCS coverage exceeds 32 MiB in-memory limit; "
                        + "use downloadCoverageToFile.");
            }
            output.write(buffer, 0, read);
        }
        if (received == 0L) {
            throw new IOException("WCS returned an empty in-memory coverage.");
        }
        if (declaredLength >= 0 && declaredLength != received) {
            throw new IOException("WCS in-memory Content-Length mismatch.");
        }
        return output.toByteArray();
    }

}
