package ar.com.catgis.data.online;

import ar.com.catgis.CatgisLogger;
import ar.com.catgis.data.online.OnlineRasterSource;

import ar.com.catgis.OnlineMapUtils;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class OnlineTileCache {

    private static final String USER_AGENT = "CATGIS Desktop/1.0 (+online-basemaps)";
    private static final String ACCEPT_IMAGES = "image/png,image/jpeg,image/*;q=0.9,*/*;q=0.5";
    private static final String ACCEPT_LANGUAGE = "es-AR,es;q=0.9,en;q=0.7";
    private static final long RETRY_DELAY_MS = 30000L;
    private static final int MAX_FAILURE_MESSAGE_LENGTH = 180;
    // Use AppData for persistent cache (survives reboots)
    private static final File CACHE_DIR;
    private static final int MEMORY_CACHE_MAX = 512;
    private static final int DISK_CACHE_MAX_MB = 200;
    static {
        String appData = System.getenv("APPDATA");
        if (appData != null) {
            CACHE_DIR = new File(appData, "CATGIS/online-tile-cache");
        } else {
            CACHE_DIR = new File(System.getProperty("java.io.tmpdir"), "catgis-online-tiles");
        }
        CACHE_DIR.mkdirs();
    }

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(6, runnable -> {
        Thread thread = new Thread(runnable, "catgis-online-tile");
        thread.setDaemon(true);
        return thread;
    });

    // Schedule periodic disk cache eviction (every 30 minutes)
    static {
        java.util.Timer evictionTimer = new java.util.Timer("catgis-tile-evict-timer", true);
        evictionTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                evictDiskCache();
            }
        }, 300000L, 1800000L); // 5min initial delay, 30min repeat
    }

    private static final Map<String, BufferedImage> MEMORY_CACHE = Collections.synchronizedMap(
            new LinkedHashMap<String, BufferedImage>(MEMORY_CACHE_MAX, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
                    return size() > MEMORY_CACHE_MAX;
                }
            }
    );
    private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();
    private static final Map<String, Long> RETRY_AFTER = new ConcurrentHashMap<>();
    private static final Map<String, String> LAST_SOURCE_FAILURE = new ConcurrentHashMap<>();
    private static final Map<String, Long> LAST_SOURCE_FAILURE_AT = new ConcurrentHashMap<>();

    private OnlineTileCache() {
    }

    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }

    public static BufferedImage getTile(OnlineRasterSource source, int z, int x, int y, Runnable repaintCallback) {
        if (source == null) {
            return null;
        }

        String key = buildKey(source, z, x, y);
        BufferedImage cached = MEMORY_CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        File tileFile = resolveTileFile(source, z, x, y);
        if (tileFile.isFile()) {
            try {
                BufferedImage image = ImageIO.read(tileFile);
                if (image != null) {
                    if (isUnavailablePlaceholder(source, image, tileFile.length())) {
                        markPlaceholderUnavailable(key, tileFile);
                        return null;
                    }
                    MEMORY_CACHE.put(key, image);
                    return image;
                }
            } catch (Exception ignored) { CatgisLogger.warn("OnlineTileCache: operation failed", ignored); }
        }

        Long retryAfter = RETRY_AFTER.get(key);
        if (retryAfter != null && retryAfter > System.currentTimeMillis()) {
            return null;
        }

        if (PENDING.add(key)) {
            EXECUTOR.submit(() -> fetchTile(source, z, x, y, key, tileFile, repaintCallback));
        }

        return null;
    }

    private static void fetchTile(OnlineRasterSource source,
                                  int z,
                                  int x,
                                  int y,
                                  String key,
                                  File tileFile,
                                  Runnable repaintCallback) {
        String failureReason = null;
        try {
            File parent = tileFile.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            List<String> candidateUrls = buildCandidateUrls(source, z, x, y);
            for (String tileUrl : candidateUrls) {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(tileUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(7000);
                    connection.setReadTimeout(12000);
                    connection.setInstanceFollowRedirects(true);
                    connection.setRequestProperty("User-Agent", USER_AGENT);
                    connection.setRequestProperty("Accept", ACCEPT_IMAGES);
                    connection.setRequestProperty("Accept-Language", ACCEPT_LANGUAGE);

                    int code = connection.getResponseCode();
                    if (code < 200 || code >= 300) {
                        failureReason = "HTTP " + code + " en " + source.getName();
                        continue;
                    }

                    byte[] payload;
                    try (InputStream input = connection.getInputStream()) {
                        payload = readAllBytes(input);
                    }
                    if (payload.length == 0) {
                        failureReason = "Respuesta vacia en " + source.getName();
                        continue;
                    }

                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(payload));
                    if (image == null) {
                        failureReason = "Formato no soportado (" + safeContentType(connection.getContentType()) + ")";
                        continue;
                    }

                    long byteLength = connection.getContentLengthLong();
                    if (byteLength <= 0L) {
                        byteLength = payload.length;
                    }
                    if (isUnavailablePlaceholder(source, image, byteLength)) {
                        markPlaceholderUnavailable(key, tileFile);
                        return;
                    }

                    String format = guessFormat(source);
                    ImageIO.write(image, format, tileFile);
                    MEMORY_CACHE.put(key, image);
                    RETRY_AFTER.remove(key);
                    clearSourceFailure(source);
                    if (repaintCallback != null) {
                        SwingUtilities.invokeLater(repaintCallback);
                    }
                    return;
                } catch (Exception ex) {
                    failureReason = ex.getClass().getSimpleName() + ": " + safeMessage(ex.getMessage());
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }

            if (failureReason == null || failureReason.isBlank()) {
                failureReason = "No se pudo descargar tesela de " + source.getName();
            }
            registerSourceFailure(source, failureReason);
            RETRY_AFTER.put(key, System.currentTimeMillis() + RETRY_DELAY_MS);
        } catch (Exception ex) {
            registerSourceFailure(source, ex.getClass().getSimpleName() + ": " + safeMessage(ex.getMessage()));
            RETRY_AFTER.put(key, System.currentTimeMillis() + RETRY_DELAY_MS);
        } finally {
            PENDING.remove(key);
        }
    }

    private static List<String> buildCandidateUrls(OnlineRasterSource source, int z, int x, int y) {
        if (source == null) {
            return Collections.emptyList();
        }

        LinkedHashSet<String> urls = new LinkedHashSet<>();
        String primary = OnlineMapUtils.buildTileUrl(source, z, x, y);
        if (primary != null && !primary.isBlank()) {
            urls.add(primary);
        }

        String sourceId = source.getId() != null ? source.getId().toLowerCase() : "";
        if (sourceId.contains("osm")) {
            urls.add("https://a.tile.openstreetmap.org/" + z + "/" + x + "/" + y + ".png");
            urls.add("https://b.tile.openstreetmap.org/" + z + "/" + x + "/" + y + ".png");
            urls.add("https://c.tile.openstreetmap.org/" + z + "/" + x + "/" + y + ".png");
        }
        if (sourceId.startsWith("esri-")) {
            for (String url : new ArrayList<>(urls)) {
                if (url.contains("server.arcgisonline.com")) {
                    urls.add(url.replace("server.arcgisonline.com", "services.arcgisonline.com"));
                }
            }
        }

        return new ArrayList<>(urls);
    }

    public static String getRecentSourceFailure(String sourceId, long maxAgeMillis) {
        if (sourceId == null || sourceId.isBlank()) {
            return "";
        }
        Long at = LAST_SOURCE_FAILURE_AT.get(sourceId);
        if (at == null || at <= 0L) {
            return "";
        }
        if (maxAgeMillis > 0 && (System.currentTimeMillis() - at) > maxAgeMillis) {
            return "";
        }
        String failure = LAST_SOURCE_FAILURE.get(sourceId);
        return failure != null ? failure : "";
    }

    private static void registerSourceFailure(OnlineRasterSource source, String reason) {
        if (source == null || source.getId() == null || source.getId().isBlank()) {
            return;
        }
        LAST_SOURCE_FAILURE.put(source.getId(), truncate(reason));
        LAST_SOURCE_FAILURE_AT.put(source.getId(), System.currentTimeMillis());
    }

    private static void clearSourceFailure(OnlineRasterSource source) {
        if (source == null || source.getId() == null || source.getId().isBlank()) {
            return;
        }
        LAST_SOURCE_FAILURE.remove(source.getId());
        LAST_SOURCE_FAILURE_AT.remove(source.getId());
    }

    private static byte[] readAllBytes(InputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream(16 * 1024);
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) >= 0) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private static String safeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "desconocido";
        }
        return contentType.trim();
    }

    private static String safeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "sin detalle";
        }
        return message.replace('\n', ' ').replace('\r', ' ').trim();
    }

    private static String truncate(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.trim();
        if (normalized.length() <= MAX_FAILURE_MESSAGE_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_FAILURE_MESSAGE_LENGTH - 1) + "...";
    }

    private static String guessFormat(OnlineRasterSource source) {
        String template = source != null ? source.getUrlTemplate().toLowerCase() : "";
        if (template.contains(".jpg") || template.contains(".jpeg")) {
            return "jpg";
        }
        return "png";
    }

    private static boolean isUnavailablePlaceholder(OnlineRasterSource source, BufferedImage image, long byteLength) {
        if (source == null || image == null) {
            return false;
        }
        if (!"esri-world-imagery".equalsIgnoreCase(source.getId())) {
            return false;
        }
        if (image.getWidth() != 256 || image.getHeight() != 256) {
            return false;
        }

        int grayishCount = 0;
        int sampleCount = 0;
        double brightnessSum = 0d;
        double brightnessSquaredSum = 0d;
        for (int y = 0; y < image.getHeight(); y += 8) {
            for (int x = 0; x < image.getWidth(); x += 8) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));
                if ((max - min) <= 8) {
                    grayishCount++;
                }
                double brightness = (r + g + b) / 3d;
                brightnessSum += brightness;
                brightnessSquaredSum += brightness * brightness;
                sampleCount++;
            }
        }
        if (sampleCount == 0) {
            return false;
        }

        double grayRatio = grayishCount / (double) sampleCount;
        double averageBrightness = brightnessSum / sampleCount;
        double variance = Math.max(0d, (brightnessSquaredSum / sampleCount) - (averageBrightness * averageBrightness));
        double stdDev = Math.sqrt(variance);

        return grayRatio >= 0.995d
                && averageBrightness >= 185d
                && averageBrightness <= 225d
                && stdDev <= 12d
                && (byteLength <= 0L || byteLength <= 12000L);
    }

    private static void markPlaceholderUnavailable(String key, File tileFile) {
        if (key == null || key.isBlank()) {
            return;
        }
        MEMORY_CACHE.remove(key);
        RETRY_AFTER.put(key, System.currentTimeMillis() + 600000L);
        try {
            if (tileFile != null && tileFile.isFile()) {
                tileFile.delete();
            }
        } catch (Exception ignored) { CatgisLogger.warn("OnlineTileCache: operation failed", ignored); }
    }

    private static String buildKey(OnlineRasterSource source, int z, int x, int y) {
        return source.getId() + "_" + z + "_" + x + "_" + y;
    }

    private static File resolveTileFile(OnlineRasterSource source, int z, int x, int y) {
        String folderName = source.getId().replaceAll("[^a-zA-Z0-9._-]", "_");
        String extension = guessFormat(source);
        return new File(CACHE_DIR, folderName + File.separator + z + File.separator + x + File.separator + y + "." + extension);
    }

    /**
     * Evict old disk cache files when total size exceeds DISK_CACHE_MAX_MB.
     * Called periodically and on app shutdown.
     */
    public static void evictDiskCache() {
        new Thread(() -> {
            try {
                long maxBytes = DISK_CACHE_MAX_MB * 1024L * 1024L;
                long totalSize = 0;
                java.util.List<File> allFiles = new java.util.ArrayList<>();
                collectFiles(CACHE_DIR, allFiles);

                // Sort by last modified (oldest first)
                allFiles.sort((a, b) -> Long.compare(a.lastModified(), b.lastModified()));

                for (File f : allFiles) {
                    totalSize += f.length();
                }

                // Remove oldest files until under limit
                if (totalSize > maxBytes) {
                    long toRemove = totalSize - maxBytes;
                    for (File f : allFiles) {
                        if (toRemove <= 0) break;
                        long len = f.length();
                        if (f.delete()) {
                            toRemove -= len;
                        }
                    }
                }
            } catch (Exception ignored) { CatgisLogger.warn("OnlineTileCache: operation failed", ignored); }
        }, "catgis-tile-evict").start();
    }

    private static void collectFiles(File dir, java.util.List<File> result) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                collectFiles(f, result);
            } else if (f.isFile()) {
                result.add(f);
            }
        }
    }

    /**
     * Prefetch tiles surrounding the current view for smoother panning.
     * Loads tiles 1 ring outward from the visible area at current zoom.
     */
    public static void prefetchNeighbors(OnlineRasterSource source, int z, int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (nx < 0 || ny < 0) continue;
                String key = buildKey(source, z, nx, ny);
                if (MEMORY_CACHE.containsKey(key)) continue;
                if (PENDING.contains(key)) continue;
                File tileFile = resolveTileFile(source, z, nx, ny);
                if (tileFile.isFile() && tileFile.length() > 100) {
                    EXECUTOR.submit(() -> {
                        try {
                            BufferedImage img = ImageIO.read(tileFile);
                            if (img != null) MEMORY_CACHE.put(key, img);
                        } catch (Exception ignored) { CatgisLogger.warn("OnlineTileCache: operation failed", ignored); }
                    });
                }
            }
        }
    }
}
