package ar.com.catgis;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            return getCoveragesVersion(serviceUrl, WcsVersion.V2_0_1);
        } catch (Exception e) {
            return getCoveragesVersion(serviceUrl, WcsVersion.V1_0_0);
        }
    }

    private static List<WcsCoverage> getCoveragesVersion(String serviceUrl, WcsVersion version) throws Exception {
        List<WcsCoverage> coverages = new ArrayList<>();

        String capabilitiesUrl = buildCapabilitiesUrl(serviceUrl, version);
        String xml = fetchUrl(capabilitiesUrl);

        if (xml == null || xml.isEmpty()) {
            throw new Exception("No se pudo obtener capabilities del servidor WCS.");
        }

        // Parse coverage offerings (WCS 1.0.0) or coverage summaries (WCS 2.0.1)
        String coverageTag = version == WcsVersion.V2_0_1 ? "CoverageSummary" : "CoverageOffering";
        int idx = 0;
        while (true) {
            int start = xml.indexOf("<" + coverageTag, idx);
            if (start < 0) start = xml.indexOf("<" + coverageTag.toLowerCase(), idx);
            if (start < 0) break;

            int end = xml.indexOf("</" + coverageTag + ">", start);
            if (end < 0) end = xml.indexOf("</" + coverageTag.toLowerCase() + ">", start);
            if (end < 0) break;

            String block = xml.substring(start, end);
            String name = extractXmlTag(block, version == WcsVersion.V2_0_1 ? "CoverageId" : "Name");
            if (name == null) name = extractXmlTag(block, "name");
            String title = extractXmlTag(block, "Title");
            if (title == null) title = extractXmlTag(block, "Label");
            String desc = extractXmlTag(block, "Abstract");
            if (desc == null) desc = extractXmlTag(block, "Description");

            // Parse supported formats
            List<String> formats = new ArrayList<>();
            int fIdx = 0;
            while (true) {
                int fStart = block.indexOf("<Format>", fIdx);
                if (fStart < 0) fStart = block.indexOf("<format>", fIdx);
                if (fStart < 0) break;
                int fEnd = block.indexOf("</Format>", fStart);
                if (fEnd < 0) fEnd = block.indexOf("</format>", fStart);
                if (fEnd < 0) break;
                formats.add(block.substring(fStart + 8, fEnd).trim());
                fIdx = fEnd + 9;
            }

            // Parse native resolution (WCS 2.0.1)
            List<Double> resolution = new ArrayList<>();
            String resX = extractXmlTag(block, "resolution");
            if (resX != null) {
                try { resolution.add(Double.parseDouble(resX)); } catch (NumberFormatException ignored) {}
            }

            if (name != null && !name.isEmpty()) {
                coverages.add(new WcsCoverage(name,
                        title != null ? title : name,
                        desc != null ? desc : "",
                        "", "", formats, resolution));
            }

            idx = end + coverageTag.length() + 3;
        }

        return coverages;
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
        if (bbox == null || bbox.isBlank()) bbox = "-180,-90,180,90";
        if (crs == null || crs.isBlank()) crs = "EPSG:4326";
        if (width <= 0) width = 512;
        if (height <= 0) height = 512;

        String fmt = format;
        if (fmt == null || fmt.isBlank()) {
            fmt = "image/tiff"; // default
        }

        String url;
        if (version == WcsVersion.V2_0_1) {
            url = buildGetCoverageUrl20(serviceUrl, coverageName, bbox, crs, width, height, fmt);
        } else {
            url = buildGetCoverageUrl10(serviceUrl, coverageName, bbox, crs, width, height, fmt);
        }

        return fetchUrlBytes(url);
    }

    /**
     * Download a coverage and save to file.
     */
    public static void downloadCoverageToFile(String serviceUrl, String coverageName,
                                               String bbox, String crs,
                                               int width, int height,
                                               File outputFile) throws Exception {
        downloadCoverageToFile(serviceUrl, coverageName, bbox, crs,
                width, height, null, outputFile);
    }

    /**
     * Download a coverage with format and save to file.
     */
    public static void downloadCoverageToFile(String serviceUrl, String coverageName,
                                               String bbox, String crs,
                                               int width, int height,
                                               String format, File outputFile) throws Exception {
        byte[] data = downloadCoverage(serviceUrl, coverageName, bbox, crs,
                width, height, format);
        if (data == null || data.length == 0) {
            throw new Exception("No se pudo descargar la cobertura.");
        }
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(data);
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

    private static String fetchUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "CATGIS-Desktop/1.0");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("HTTP " + responseCode + " from WCS server");
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line).append("\n");
        reader.close();
        conn.disconnect();
        return sb.toString();
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

    private static String extractXmlTag(String xml, String tagName) {
        for (String variant : new String[]{tagName, tagName.toLowerCase(), tagName.toUpperCase()}) {
            String openTag = "<" + variant + ">";
            String closeTag = "</" + variant + ">";
            int start = xml.indexOf(openTag);
            if (start < 0) continue;
            start += openTag.length();
            int end = xml.indexOf(closeTag, start);
            if (end < 0) continue;
            return xml.substring(start, end).trim();
        }
        return null;
    }
}
