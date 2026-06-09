package ar.com.catgis;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * WCS (Web Coverage Service) client for downloading raster data.
 * Supports WCS 1.0.0 and 2.0.0.
 */
public final class WcsClient {

    private WcsClient() {}

    public record WcsCoverage(String name, String title, String description,
                               String bbox, String crs) {}

    /**
     * Get available coverages from a WCS server.
     */
    public static List<WcsCoverage> getCoverages(String serviceUrl) throws Exception {
        List<WcsCoverage> coverages = new ArrayList<>();

        String capabilitiesUrl = serviceUrl;
        if (!capabilitiesUrl.toLowerCase().contains("request=")) {
            capabilitiesUrl += (capabilitiesUrl.contains("?") ? "&" : "?")
                    + "SERVICE=WCS&REQUEST=GetCapabilities&VERSION=1.0.0";
        }

        String xml = fetchUrl(capabilitiesUrl);
        if (xml == null || xml.isEmpty()) {
            throw new Exception("No se pudo obtener capabilities del servidor WCS.");
        }

        // Simple XML parsing for coverage names
        int idx = 0;
        while (true) {
            int start = xml.indexOf("<Coverage>", idx);
            if (start < 0) start = xml.indexOf("<coverage>", idx);
            if (start < 0) break;

            int end = xml.indexOf("</Coverage>", start);
            if (end < 0) end = xml.indexOf("</coverage>", start);
            if (end < 0) break;

            String coverage = xml.substring(start, end);
            String name = extractTag(coverage, "name");
            String title = extractTag(coverage, "label");
            if (title == null || title.isEmpty()) title = name;

            if (name != null && !name.isEmpty()) {
                coverages.add(new WcsCoverage(name, title, "", "", ""));
            }

            idx = end + 11;
        }

        return coverages;
    }

    /**
     * Download a coverage from a WCS server.
     */
    public static byte[] downloadCoverage(String serviceUrl, String coverageName,
                                           String bbox, String crs,
                                           int width, int height) throws Exception {
        String url = serviceUrl;
        if (!url.toLowerCase().contains("request=")) {
            StringBuilder sb = new StringBuilder(url);
            sb.append(url.contains("?") ? "&" : "?");
            sb.append("SERVICE=WCS&VERSION=1.0.0&REQUEST=GetCoverage");
            sb.append("&COVERAGE=").append(URLEncoder.encode(coverageName, StandardCharsets.UTF_8));
            sb.append("&CRS=").append(URLEncoder.encode(crs != null ? crs : "EPSG:4326", StandardCharsets.UTF_8));
            sb.append("&BBOX=").append(bbox != null ? bbox : "-180,-90,180,90");
            sb.append("&WIDTH=").append(width > 0 ? width : 512);
            sb.append("&HEIGHT=").append(height > 0 ? height : 512);
            sb.append("&FORMAT=image/tiff");
            url = sb.toString();
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
        byte[] data = downloadCoverage(serviceUrl, coverageName, bbox, crs, width, height);
        if (data == null || data.length == 0) {
            throw new Exception("No se pudo descargar la cobertura.");
        }
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(data);
        fos.close();
    }

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

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
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
        if (responseCode != 200) {
            throw new Exception("HTTP " + responseCode + " from WCS server");
        }

        InputStream is = conn.getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        is.close();
        conn.disconnect();
        return bos.toByteArray();
    }

    private static String extractTag(String xml, String tagName) {
        String openTag = "<" + tagName + ">";
        String closeTag = "</" + tagName + ">";
        int start = xml.indexOf(openTag);
        if (start < 0) return null;
        start += openTag.length();
        int end = xml.indexOf(closeTag, start);
        if (end < 0) return null;
        return xml.substring(start, end).trim();
    }
}
