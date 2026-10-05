package ar.com.catgis;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * STAC (SpatioTemporal Asset Catalog) client.
 * Discovers and downloads geospatial datasets from STAC APIs.
 * <p>
 * Supports STAC 1.0.0 with pagination, collection extent parsing,
 * conformance class checking, and advanced search.
 * </p>
 *
 * @see <a href="https://stacspec.org/">STAC Specification</a>
 */
public final class StacClient {

    private StacClient() {}

    public record StacCollection(String id, String title, String description,
                                  String license, double[] bbox,
                                  String temporalStart, String temporalEnd,
                                  int itemCount) {
        public StacCollection(String id, String title, String description, int itemCount) {
            this(id, title, description, null, null, null, null, itemCount);
        }
    }

    public record StacItem(String id, String collectionId, String geometry,
                            String datetime, List<String> assetUrls,
                            String thumbnailUrl) {
        public StacItem(String id, String collectionId, String geometry,
                         String datetime, List<String> assetUrls) {
            this(id, collectionId, geometry, datetime, assetUrls, null);
        }
    }

    public record StacSearchResult(List<StacItem> items, String nextPageUrl,
                                    int totalMatched, int returnedCount) {}

    /**
     * Check STAC API conformance.
     */
    public static List<String> getConformance(String apiUrl) throws Exception {
        String url = apiUrl.endsWith("/") ? apiUrl : apiUrl + "/";
        String json = fetchUrl(url);
        return extractJsonArray(json, "conformsTo");
    }

    /**
     * List all collections from a STAC API.
     */
    public static List<StacCollection> getCollections(String apiUrl) throws Exception {
        String baseUrl = apiUrl.endsWith("/") ? apiUrl : apiUrl + "/";
        List<StacCollection> allCollections = new ArrayList<>();
        String nextUrl = baseUrl + "collections";

        while (nextUrl != null) {
            String json = fetchUrl(nextUrl);

            // Parse this page
            int idx = 0;
            while (true) {
                int start = json.indexOf("\"id\":", idx);
                if (start < 0) break;
                start = json.indexOf("\"", start + 5) + 1;
                int end = json.indexOf("\"", start);
                if (end < 0) break;
                String id = json.substring(start, end);

                // Title
                String title = extractJsonString(json, "title", idx);
                if (title == null || title.isEmpty()) title = id;

                // Description
                String desc = extractJsonString(json, "description", idx);

                // License
                String license = extractJsonString(json, "license", idx);

                // Spatial extent (bbox)
                double[] bbox = null;
                int extIdx = json.indexOf("\"spatial\":", idx);
                if (extIdx > 0 && extIdx < idx + 2000) {
                    int bboxIdx = json.indexOf("\"bbox\":", extIdx);
                    if (bboxIdx > 0 && bboxIdx < extIdx + 500) {
                        bbox = extractJsonBbox(json, bboxIdx);
                    }
                }

                // Temporal extent
                String tempStart = null;
                String tempEnd = null;
                int tempIdx = json.indexOf("\"temporal\":", idx);
                if (tempIdx > 0 && tempIdx < idx + 2000) {
                    int intIdx = json.indexOf("\"interval\":", tempIdx);
                    if (intIdx > 0 && intIdx < tempIdx + 500) {
                        tempStart = extractJsonNestedString(json, intIdx,
                                new String[]{"interval", "0", "start"});
                        tempEnd = extractJsonNestedString(json, intIdx,
                                new String[]{"interval", "0", "end"});
                    }
                }

                allCollections.add(new StacCollection(id, title, desc != null ? desc : "",
                        license, bbox, tempStart, tempEnd, 0));
                idx = end + 1;
            }

            // Check for next page
            nextUrl = extractNextLink(json);
            if (nextUrl != null && nextUrl.equals(nextUrl)) break; // safety
        }

        return allCollections;
    }

    /**
     * Advanced search with full pagination.
     */
    public static StacSearchResult search(String apiUrl, String collectionId,
                                           String bbox, String datetime,
                                           int limit, int page) throws Exception {
        String baseUrl = apiUrl.endsWith("/") ? apiUrl : apiUrl + "/";
        StringBuilder body = new StringBuilder("{");
        boolean hasParam = false;

        if (collectionId != null && !collectionId.isEmpty()) {
            body.append("\"collections\":[\"").append(escapeJson(collectionId)).append("\"]");
            hasParam = true;
        }
        if (bbox != null && !bbox.isEmpty()) {
            if (hasParam) body.append(",");
            body.append("\"bbox\":[").append(bbox).append("]");
            hasParam = true;
        }
        if (datetime != null && !datetime.isEmpty()) {
            if (hasParam) body.append(",");
            body.append("\"datetime\":\"").append(escapeJson(datetime)).append("\"");
            hasParam = true;
        }
        if (limit > 0) {
            if (hasParam) body.append(",");
            body.append("\"limit\":").append(limit);
            hasParam = true;
        }
        body.append("}");

        String json = postUrl(baseUrl + "search", body.toString());

        // Parse result
        List<StacItem> items = parseItems(json);

        // Extract context (total matched)
        int totalMatched = items.size();
        int ctxIdx = json.indexOf("\"context\":");
        if (ctxIdx > 0) {
            String matched = extractJsonStringRaw(json, "matched", ctxIdx);
            try { totalMatched = Integer.parseInt(matched); } catch (Exception ignored) { CatgisLogger.warn("StacClient: operation failed", ignored); }
        }

        // Check for next page link
        String nextUrl = extractNextLink(json);

        return new StacSearchResult(items, nextUrl, totalMatched, items.size());
    }

    /**
     * Search with default parameters.
     */
    public static List<StacItem> searchItems(String apiUrl, String collectionId,
                                               String bbox, String datetime) throws Exception {
        return search(apiUrl, collectionId, bbox, datetime, 0, 0).items();
    }

    /**
     * Download an asset from a STAC item.
     */
    public static void downloadAsset(String assetUrl, File outputFile) throws Exception {
        byte[] data = fetchUrlBytes(assetUrl);
        if (data == null || data.length == 0) {
            throw new Exception("No se pudo descargar el asset.");
        }
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(data);
        }
    }

    // --- Parser helpers ---

    private static List<StacItem> parseItems(String json) {
        List<StacItem> items = new ArrayList<>();
        if (json == null || json.isEmpty()) return items;

        int featuresIdx = json.indexOf("\"features\":");
        if (featuresIdx < 0) return items;

        int arrayStart = json.indexOf('[', featuresIdx);
        int arrayEnd = findMatchingBracket(json, arrayStart, '[', ']');
        if (arrayEnd < 0) return items;

        String featuresBlock = json.substring(arrayStart + 1, arrayEnd);

        int idx = 0;
        while (true) {
            int idStart = featuresBlock.indexOf("\"id\":", idx);
            if (idStart < 0) break;
            idStart = featuresBlock.indexOf("\"", idStart + 5) + 1;
            int idEnd = featuresBlock.indexOf("\"", idStart);
            if (idEnd < 0) break;
            String id = featuresBlock.substring(idStart, idEnd);

            // Datetime
            String datetime = extractJsonString(featuresBlock, "datetime", 0);

            // Collection
            String collection = extractJsonString(featuresBlock, "collection", idx);

            // Thumbnail
            String thumb = null;
            int thumbIdx = featuresBlock.indexOf("\"thumbnail\":", idx);
            if (thumbIdx > 0 && thumbIdx < idx + 2000) {
                thumb = extractJsonNestedString(featuresBlock, thumbIdx,
                        new String[]{"thumbnail", "href"});
            }

            // Asset URLs
            List<String> assetUrls = new ArrayList<>();
            int assetsIdx = featuresBlock.indexOf("\"assets\":{", idx);
            if (assetsIdx > 0 && assetsIdx < idx + 5000) {
                int aEnd = findMatchingBracket(featuresBlock, assetsIdx, '{', '}');
                if (aEnd > assetsIdx) {
                    String assetsBlock = featuresBlock.substring(assetsIdx + 10, aEnd);
                    int uIdx = 0;
                    while (true) {
                        int hrefIdx = assetsBlock.indexOf("\"href\":", uIdx);
                        if (hrefIdx < 0) break;
                        int hStart = assetsBlock.indexOf("\"", hrefIdx + 7) + 1;
                        int hEnd = assetsBlock.indexOf("\"", hStart);
                        if (hEnd > hStart) {
                            String url = assetsBlock.substring(hStart, hEnd);
                            if (url.startsWith("http")) assetUrls.add(url);
                        }
                        uIdx = hEnd + 1;
                    }
                }
            }

            items.add(new StacItem(id, collection, "", datetime, assetUrls, thumb));
            idx = idEnd + 1;
        }
        return items;
    }

    private static String extractJsonString(String json, String key, int fromIndex) {
        int start = json.indexOf("\"" + key + "\":", fromIndex);
        if (start < 0) return null;
        start = json.indexOf("\"", start + key.length() + 3) + 1;
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    private static String extractJsonStringRaw(String json, String key, int fromIndex) {
        int start = json.indexOf("\"" + key + "\":", fromIndex);
        if (start < 0) return null;
        start += key.length() + 4; // "key":
        if (json.charAt(start) == '"') {
            start++;
            int end = json.indexOf("\"", start);
            return end > start ? json.substring(start, end) : null;
        }
        // Number or boolean
        int end = start;
        while (end < json.length() && !Character.isWhitespace(json.charAt(end))
                && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
        return json.substring(start, end);
    }

    private static String extractJsonNestedString(String json, int fromIndex, String[] path) {
        int pos = fromIndex;
        String value = null;
        for (String key : path) {
            int keyIdx = json.indexOf("\"" + key + "\":", pos);
            if (keyIdx < 0) return null;
            pos = keyIdx + key.length() + 3;
        }
        if (json.charAt(pos) == '"') {
            pos++;
            int end = json.indexOf("\"", pos);
            if (end > pos) value = json.substring(pos, end);
        }
        return value;
    }

    private static double[] extractJsonBbox(String json, int bboxIdx) {
        int start = json.indexOf('[', bboxIdx);
        if (start < 0) return null;
        int end = json.indexOf(']', start);
        if (end < 0) return null;
        String bboxStr = json.substring(start + 1, end);
        String[] parts = bboxStr.split(",");
        if (parts.length < 4) return null;
        try {
            return new double[]{
                    Double.parseDouble(parts[0].trim()),
                    Double.parseDouble(parts[1].trim()),
                    Double.parseDouble(parts[2].trim()),
                    Double.parseDouble(parts[3].trim())
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String extractNextLink(String json) {
        int linksIdx = json.indexOf("\"links\":");
        if (linksIdx < 0) return null;
        int nextIdx = json.indexOf("\"rel\":\"next\"", linksIdx);
        if (nextIdx < 0) return null;
        return extractJsonString(json, "href", nextIdx);
    }

    private static List<String> extractJsonArray(String json, String key) {
        List<String> values = new ArrayList<>();
        int idx = json.indexOf("\"" + key + "\":");
        if (idx < 0) return values;
        int arrayStart = json.indexOf('[', idx);
        if (arrayStart < 0) return values;
        int arrayEnd = json.indexOf(']', arrayStart);
        if (arrayEnd < 0) return values;
        String content = json.substring(arrayStart + 1, arrayEnd);
        int pos = 0;
        while (pos < content.length()) {
            int vStart = content.indexOf('"', pos);
            if (vStart < 0) break;
            int vEnd = content.indexOf('"', vStart + 1);
            if (vEnd < 0) break;
            values.add(content.substring(vStart + 1, vEnd));
            pos = vEnd + 1;
        }
        return values;
    }

    private static int findMatchingBracket(String s, int start, char open, char close) {
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == open) depth++;
            else if (c == close) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // --- HTTP helpers ---

    private static String fetchUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "CATGIS-Desktop/1.0");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) throw new Exception("HTTP " + responseCode);

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        conn.disconnect();
        return sb.toString();
    }

    private static String postUrl(String urlString, String body) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "CATGIS-Desktop/1.0");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) throw new Exception("HTTP " + responseCode);

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
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
        if (responseCode != 200) throw new Exception("HTTP " + responseCode);

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
