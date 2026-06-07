package ar.com.catgis.catmap;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;

import ar.com.catgis.*;
import ar.com.catgis.layout.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Socket server for CATGIS-CATMAP communication.
 * Runs in CATGIS Desktop, accepts connections from CATMAP Standalone.
 * Protocol: simple JSON-like text format.
 */
public final class CatgisSocketServer {

    private static ServerSocket serverSocket;
    private static Thread listenerThread;
    private static boolean running = false;
    private static final int PORT = 8899;

    private CatgisSocketServer() {}

    /**
     * Start the server on a background thread.
     */
    public static void start() {
        if (running) return;
        running = true;
        listenerThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName("127.0.0.1"));
                System.out.println("[CATMAP Server] Listening on port " + PORT);
                while (running) {
                    Socket client = serverSocket.accept();
                    new Thread(() -> handleClient(client)).start();
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("[CATMAP Server] Error: " + e.getMessage());
                }
            }
        }, "CATMAP-Server");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Stop the server.
     */
    public static void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}
    }

    private static void handleClient(Socket client) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                String response = processCommand(line.trim());
                out.println(response);
            }
        } catch (IOException e) {
            // Client disconnected
        } finally {
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    private static String processCommand(String command) {
        if (command.startsWith("GET_PROJECT_STATE")) {
            return getProjectState();
        } else if (command.startsWith("GET_LAYERS")) {
            return getLayers();
        } else if (command.startsWith("GET_EXTENT")) {
            return getExtent();
        } else if (command.startsWith("PING")) {
            return "{\"status\":\"ok\",\"version\":\"1.0\"}";
        } else if (command.startsWith("ADD_TABLE|")) {
            String csvPath = command.substring("ADD_TABLE|".length()).trim();
            if (!csvPath.isEmpty() && new File(csvPath).exists()) {
                try {
                    // Use same Preferences key as ClimateAreaAnalysisDialog
                    Preferences prefs = Preferences.userNodeForPackage(
                        Class.forName("ar.com.catgis.climate.ClimateAreaAnalysisDialog"));
                    prefs.put("pendingCatmapTable", csvPath);
                } catch (Exception e) {
                    // Fallback
                    Preferences prefs = Preferences.userNodeForPackage(CatgisSocketServer.class);
                    prefs.put("pendingCatmapTable", csvPath);
                }
                return "{\"status\":\"ok\",\"message\":\"table stored\"}";
            }
            return "{\"status\":\"error\",\"message\":\"file not found\"}";
        }
        return "{\"error\":\"unknown command\"}";
    }

    private static String getProjectState() {
        Project project = CatgisDesktopApp.currentProject;
        if (project == null) return "{\"error\":\"no project loaded\"}";

        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\":\"ok\"");
        sb.append(",\"projectName\":\"").append(escapeJson(project.getName())).append("\"");
        sb.append(",\"crs\":\"").append(escapeJson(project.getProjectCRS())).append("\"");

        // Layers
        sb.append(",\"layers\":[");
        boolean first = true;
        if (project.getLayers() != null) {
            for (Layer layer : project.getLayers()) {
                if (layer == null) continue;
                if (!first) sb.append(",");
                first = false;
                sb.append("{\"name\":\"").append(escapeJson(layer.getName())).append("\"");
                sb.append(",\"type\":\"").append(escapeJson(layer.getType())).append("\"");
                sb.append(",\"visible\":").append(layer.isVisible());
                sb.append(",\"crs\":\"").append(escapeJson(layer.getSourceCRS())).append("\"");
                sb.append("}");
            }
        }
        sb.append("]");

        // Extent
        MapPanel map = CatgisDesktopApp.mapPanel;
        if (map != null) {
            sb.append(",\"extent\":{");
            sb.append("\"minX\":").append(map.getViewMinX());
            sb.append(",\"minY\":").append(map.getViewMinY());
            sb.append(",\"zoomFactor\":").append(map.getZoomFactor());
            sb.append("}");
        }

        sb.append("}");
        return sb.toString();
    }

    private static String getLayers() {
        Project project = CatgisDesktopApp.currentProject;
        if (project == null) return "{\"error\":\"no project loaded\"}";

        StringBuilder sb = new StringBuilder();
        sb.append("{\"layers\":[");
        boolean first = true;
        if (project.getLayers() != null) {
            for (Layer layer : project.getLayers()) {
                if (layer == null) continue;
                if (!first) sb.append(",");
                first = false;
                sb.append("{\"name\":\"").append(escapeJson(layer.getName())).append("\"");
                sb.append(",\"type\":\"").append(escapeJson(layer.getType())).append("\"");
                sb.append(",\"visible\":").append(layer.isVisible());
                sb.append(",\"crs\":\"").append(escapeJson(layer.getSourceCRS())).append("\"");
                sb.append("}");
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String getExtent() {
        MapPanel map = CatgisDesktopApp.mapPanel;
        if (map == null) return "{\"error\":\"no map panel\"}";

        return "{\"extent\":{"
                + "\"minX\":" + map.getViewMinX()
                + ",\"minY\":" + map.getViewMinY()
                + ",\"zoomFactor\":" + map.getZoomFactor()
                + "}}";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
