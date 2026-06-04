package ar.com.catgis.catmap;

import ar.com.catgis.*;
import ar.com.catgis.layout.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Socket client for CATMAP to communicate with CATGIS Desktop.
 */
public final class CatmapSocketClient {

    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static boolean connected = false;

    private CatmapSocketClient() {}

    /**
     * Try to connect to CATGIS server.
     */
    public static boolean connect() {
        try {
            socket = new Socket("127.0.0.1", 8899);
            socket.setSoTimeout(5000);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            // Test connection
            String response = sendCommand("PING");
            connected = response != null && response.contains("\"ok\"");
            return connected;
        } catch (Exception e) {
            connected = false;
            return false;
        }
    }

    /**
     * Disconnect from server.
     */
    public static void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    /**
     * Check if connected.
     */
    public static boolean isConnected() { return connected; }

    /**
     * Get project state from CATGIS.
     */
    public static ProjectState getProjectState() {
        if (!connected) return null;
        String response = sendCommand("GET_PROJECT_STATE");
        if (response == null) return null;
        return parseProjectState(response);
    }

    /**
     * Get layers from CATGIS.
     */
    public static List<LayerInfo> getLayers() {
        if (!connected) return new ArrayList<>();
        String response = sendCommand("GET_LAYERS");
        if (response == null) return new ArrayList<>();
        return parseLayers(response);
    }

    private static String sendCommand(String command) {
        if (out == null || in == null) return null;
        try {
            out.println(command);
            return in.readLine();
        } catch (Exception e) {
            connected = false;
            return null;
        }
    }

    private static ProjectState parseProjectState(String json) {
        try {
            String name = extractJsonValue(json, "projectName");
            String crs = extractJsonValue(json, "crs");
            double minX = extractJsonDouble(json, "minX");
            double minY = extractJsonDouble(json, "minY");
            double zoom = extractJsonDouble(json, "zoomFactor");
            return new ProjectState(name, crs, minX, minY, zoom);
        } catch (Exception e) {
            return null;
        }
    }

    private static List<LayerInfo> parseLayers(String json) {
        List<LayerInfo> layers = new ArrayList<>();
        try {
            int start = json.indexOf("\"layers\":[");
            if (start < 0) return layers;
            start = json.indexOf('[', start) + 1;
            int end = json.indexOf(']', start);
            if (end < 0) return layers;
            String array = json.substring(start, end);
            if (array.trim().isEmpty()) return layers;

            String[] items = array.split("\\},\\{");
            for (String item : items) {
                String clean = item.replaceAll("[{}]", "");
                String name = extractJsonValue(clean, "name");
                String type = extractJsonValue(clean, "type");
                boolean visible = clean.contains("\"visible\":true");
                String crs = extractJsonValue(clean, "crs");
                layers.add(new LayerInfo(name, type, visible, crs));
            }
        } catch (Exception ignored) {}
        return layers;
    }

    private static String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return "";
        return json.substring(start, end);
    }

    private static double extractJsonDouble(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start < 0) return 0;
        start += search.length();
        int end = json.indexOf(",", start);
        if (end < 0) end = json.indexOf("}", start);
        if (end < 0) return 0;
        try {
            return Double.parseDouble(json.substring(start, end).trim());
        } catch (Exception e) {
            return 0;
        }
    }

    // --- Data classes ---

    public record ProjectState(String name, String crs, double minX, double minY, double zoomFactor) {}
    public record LayerInfo(String name, String type, boolean visible, String crs) {}
}
