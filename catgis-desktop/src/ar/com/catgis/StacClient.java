package ar.com.catgis;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * STAC (SpatioTemporal Asset Catalog) client.
 * Discovers and downloads geospatial datasets from STAC APIs.
 * Spec: https://stacspec.org/
 */
public final class StacClient {

    private StacClient() {}

    public record StacCollection(String id, String title, String description, int itemCount) {}
    public record StacItem(String id, String collectionId, String geometry, String datetime, List<String> assetUrls) {}

    /**
     * List all collections from a STAC API.
     */
    public static List<StacCollection> getCollections(String apiUrl) throws Exception {
        String url = apiUrl.endsWith("/") ? apiUrl + "collections" : apiUrl + "/collections";
        String json = fetchUrl(url);
        return parseCollections(json);
    }

    /**
     * Search for items in a collection.
     */
    public static List<StacItem> searchItems(String apiUrl, String collectionId,
                                               String bbox, String datetime) throws Exception {
        StringBuilder url = new StringBuilder(apiUrl);
        if (!url.toString().endsWith("/")) url.append("/");
        url.append("search");

        StringBuilder body = new StringBuilder("{");
        if (collectionId != null && !collectionId.isEmpty()) {
            body.append("\"collections\":[\"").append(collectionId).append("\"]");
        }
        if (bbox != null && !bbox.isEmpty()) {
            if (body.length() > 1) body.append(",");
            body.append("\"bbox\":[").append(bbox).append("]");
        }
        if (datetime != null && !datetime.isEmpty()) {
            if (body.length() > 1) body.append(",");
            body.append("\"datetime\":\"").append(datetime).append("\"");
        }
        body.append("}");

        String json = postUrl(url.toString(), body.toString());
        return parseItems(json);
    }

    /**
     * Download an asset from a STAC item.
     */
    public static void downloadAsset(String assetUrl, File outputFile) throws Exception {
        byte[] data = fetchUrlBytes(assetUrl);
        if (data == null || data.length == 0) {
            throw new Exception("No se pudo descargar el asset.");
        }
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(data);
        fos.close();
    }

    private static List<StacCollection> parseCollections(String json) {
        List<StacCollection> collections = new ArrayList<>();
        if (json == null || json.isEmpty()) return collections;

        int idx = 0;
        while (true) {
            int start = json.indexOf("\"id\":", idx);
            if (start < 0) break;
            start = json.indexOf("\"", start + 5) + 1;
            int end = json.indexOf("\"", start);
            if (end < 0) break;
            String id = json.substring(start, end);

            String title = "";
            int titleIdx = json.indexOf("\"title\":", idx);
            if (titleIdx > 0 && titleIdx < idx + 500) {
                int tStart = json.indexOf("\"", titleIdx + 8) + 1;
                int tEnd = json.indexOf("\"", tStart);
                if (tEnd > tStart) title = json.substring(tStart, tEnd);
            }

            collections.add(new StacCollection(id, title, "", 0));
            idx = end + 1;
        }
        return collections;
    }

    private static List<StacItem> parseItems(String json) {
        List<StacItem> items = new ArrayList<>();
        if (json == null || json.isEmpty()) return items;

        int idx = 0;
        while (true) {
            int start = json.indexOf("\"id\":", idx);
            if (start < 0) break;
            start = json.indexOf("\"", start + 5) + 1;
            int end = json.indexOf("\"", start);
            if (end < 0) break;
            String id = json.substring(start, end);

            String datetime = "";
            int dtIdx = json.indexOf("\"datetime\":", idx);
            if (dtIdx > 0 && dtIdx < idx + 500) {
                int dStart = json.indexOf("\"", dtIdx + 12) + 1;
                int dEnd = json.indexOf("\"", dStart);
                if (dEnd > dStart) datetime = json.substring(dStart, dEnd);
            }

            List<String> assetUrls = new ArrayList<>();
            int assetsIdx = json.indexOf("\"assets\":{", idx);
            if (assetsIdx > 0 && assetsIdx < idx + 2000) {
                int aEnd = json.indexOf("}", assetsIdx);
                if (aEnd > assetsIdx) {
                    String assetsBlock = json.substring(assetsIdx, aEnd);
                    int uIdx = 0;
                    while (true) {
                        int uStart = assetsBlock.indexOf("\"href\":", uIdx);
                        if (uStart < 0) break;
                        uStart = assetsBlock.indexOf("\"", uStart + 7) + 1;
                        int uEnd = assetsBlock.indexOf("\"", uStart);
                        if (uEnd > uStart) {
                            String url = assetsBlock.substring(uStart, uEnd);
                            if (url.startsWith("http")) assetUrls.add(url);
                        }
                        uIdx = uStart + 1;
                    }
                }
            }

            items.add(new StacItem(id, "", "", datetime, assetUrls));
            idx = end + 1;
        }
        return items;
    }

    private static String fetchUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "CATGIS-Desktop/1.0");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("HTTP " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
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

        OutputStream os = conn.getOutputStream();
        os.write(body.getBytes(StandardCharsets.UTF_8));
        os.close();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("HTTP " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
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
        if (responseCode != 200) throw new Exception("HTTP " + responseCode);

        InputStream is = conn.getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) bos.write(buffer, 0, bytesRead);
        is.close();
        conn.disconnect();
        return bos.toByteArray();
    }
}
