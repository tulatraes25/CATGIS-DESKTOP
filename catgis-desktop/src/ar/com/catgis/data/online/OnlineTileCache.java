package ar.com.catgis.data.online;

import ar.com.catgis.CatgisLogger;
import ar.com.catgis.data.online.OnlineRasterSource;

import ar.com.catgis.OnlineMapUtils;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferDouble;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HexFormat;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public final class OnlineTileCache {

    private static final String USER_AGENT = "CATGIS Desktop/1.0 (+online-basemaps)";
    private static final String ACCEPT_IMAGES = "image/png,image/jpeg,image/*;q=0.9,*/*;q=0.5";
    private static final String ACCEPT_LANGUAGE = "es-AR,es;q=0.9,en;q=0.7";
    private static final long RETRY_DELAY_MS = 30000L;
    private static final int MAX_FAILURE_MESSAGE_LENGTH = 180;
    // A remote XYZ tile must never be allowed to allocate an unbounded heap.
    static final int MAX_TILE_BYTES = 8 * 1024 * 1024;
    static final int MAX_TILE_DIMENSION = 2048;
    static final long MAX_TILE_PIXELS = 2048L * 2048L;
    // Use AppData for persistent cache (survives reboots)
    private static final File CACHE_DIR;
    private static final int MEMORY_CACHE_MAX = 512;
    // Count-only eviction permits 512 large (2048px) images to consume gigabytes.
    static final long MEMORY_CACHE_MAX_BYTES = 128L * 1024L * 1024L;
    private static final int DISK_CACHE_MAX_MB = 200;
    static {
        String appData = System.getenv("APPDATA");
        if (appData != null) {
            CACHE_DIR = new File(appData, "CATGIS/online-tile-cache");
        } else {
            CACHE_DIR = new File(System.getProperty("java.io.tmpdir"), "catgis-online-tiles");
        }
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
    // Prefetch must not suppress foreground getTile requests or their repaint callback.
    private static final Set<String> PREFETCH_PENDING = ConcurrentHashMap.newKeySet();
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

        // Map rendering cannot read tile files synchronously on Swing EDT.
        File tileFile = resolveTileFile(source, z, x, y);
        if (PENDING.add(key)) {
            try {
                EXECUTOR.submit(() -> {
                    try {
                        loadDiskOrFetch(source, z, x, y, key, tileFile, repaintCallback);
                    } finally {
                        PENDING.remove(key);
                    }
                });
            } catch (RejectedExecutionException shutdown) {
                PENDING.remove(key);
            }
        }

        return null;
    }

    private static void loadDiskOrFetch(OnlineRasterSource source, int z, int x, int y,
                                        String key, File tileFile, Runnable repaintCallback) {
        if (tileFile.isFile()) {
            try {
                BufferedImage image = readCachedTile(tileFile);
                if (image != null) {
                    if (isUnavailablePlaceholder(source, image, tileFile.length())) {
                        markPlaceholderUnavailable(key, tileFile);
                        return;
                    }
                    putMemoryTile(key, image);
                    RETRY_AFTER.remove(key);
                    clearSourceFailure(source);
                    if (repaintCallback != null) {
                        SwingUtilities.invokeLater(repaintCallback);
                    }
                    return;
                }
            } catch (Exception error) {
                CatgisLogger.warn("OnlineTileCache: cached tile decode failed", error);
            }
        }
        // Network backoff must never conceal an already cached offline image.
        Long retryAfter = RETRY_AFTER.get(key);
        if (retryAfter != null && retryAfter > System.currentTimeMillis()) {
            return;
        }
        fetchTile(source, z, x, y, key, tileFile, repaintCallback);
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

                    long declaredLength = connection.getContentLengthLong();
                    byte[] payload;
                    try (InputStream input = connection.getInputStream()) {
                        payload = readLimitedTileBytes(input, declaredLength);
                    }
                    if (payload.length == 0) {
                        failureReason = "Respuesta vacia en " + source.getName();
                        continue;
                    }

                    BufferedImage image = decodeTileBytes(payload);
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
                    putMemoryTile(key, image);
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

    public static String getRecentSourceFailure(OnlineRasterSource source, long maxAgeMillis) {
        if (source == null) {
            return "";
        }
        String identity = cacheNamespace(source);
        Long at = LAST_SOURCE_FAILURE_AT.get(identity);
        if (at == null || at <= 0L) {
            return "";
        }
        if (maxAgeMillis > 0 && (System.currentTimeMillis() - at) > maxAgeMillis) {
            return "";
        }
        String failure = LAST_SOURCE_FAILURE.get(identity);
        return failure != null ? failure : "";
    }

    private static void registerSourceFailure(OnlineRasterSource source, String reason) {
        if (source == null) {
            return;
        }
        String identity = cacheNamespace(source);
        LAST_SOURCE_FAILURE.put(identity, truncate(reason));
        LAST_SOURCE_FAILURE_AT.put(identity, System.currentTimeMillis());
    }

    private static void clearSourceFailure(OnlineRasterSource source) {
        if (source == null) {
            return;
        }
        String identity = cacheNamespace(source);
        LAST_SOURCE_FAILURE.remove(identity);
        LAST_SOURCE_FAILURE_AT.remove(identity);
    }

    private static void putMemoryTile(String key, BufferedImage image) {
        putMemoryTileWithBudget(MEMORY_CACHE, key, image, MEMORY_CACHE_MAX_BYTES);
    }

    /**
     * Enforce a bound on backing pixel storage, not just entry count.
     * LinkedHashMap is access-ordered, so the first iterator entry is oldest.
     * All traversal and mutation must share the synchronizedMap lock.
     */
    static void putMemoryTileWithBudget(Map<String, BufferedImage> cache, String key,
                                        BufferedImage image, long maxBytes) {
        synchronized (cache) {
            cache.put(key, image);
            long usedBytes = 0L;
            for (BufferedImage tile : cache.values()) {
                usedBytes += imageDataBytes(tile);
            }
            Iterator<Map.Entry<String, BufferedImage>> oldest = cache.entrySet().iterator();
            while (usedBytes > maxBytes && oldest.hasNext()) {
                Map.Entry<String, BufferedImage> entry = oldest.next();
                usedBytes -= imageDataBytes(entry.getValue());
                oldest.remove();
            }
        }
    }

    /**
     * Estimate decoded heap retention from the *actual bank array capacities*.
     * DataBuffer.getSize() is a logical element count: a reader may wrap a larger
     * preallocated backing bank, so using logical size alone undercounts memory.
     * Conservatively retain the maximum of physical arrays, logical storage and
     * pixel-format geometry. Raster allocation is never performed under cache lock.
     */
    static long imageDataBytes(BufferedImage image) {
        if (image == null) {
            return 0L;
        }
        DataBuffer data = image.getRaster().getDataBuffer();
        long bytesPerElement = Math.max(1L, (DataBuffer.getDataTypeSize(data.getDataType()) + 7L) / 8L);
        long logical = (long) data.getSize() * data.getNumBanks() * bytesPerElement;
        long physical = 0L;
        if (data instanceof DataBufferByte banks) {
            for (byte[] bank : banks.getBankData()) {
                physical += bank.length;
            }
        } else if (data instanceof DataBufferShort banks) {
            for (short[] bank : banks.getBankData()) {
                physical += 2L * bank.length;
            }
        } else if (data instanceof DataBufferUShort banks) {
            for (short[] bank : banks.getBankData()) {
                physical += 2L * bank.length;
            }
        } else if (data instanceof DataBufferInt banks) {
            for (int[] bank : banks.getBankData()) {
                physical += 4L * bank.length;
            }
        } else if (data instanceof DataBufferFloat banks) {
            for (float[] bank : banks.getBankData()) {
                physical += 4L * bank.length;
            }
        } else if (data instanceof DataBufferDouble banks) {
            for (double[] bank : banks.getBankData()) {
                physical += 8L * bank.length;
            }
        } else {
            // Unknown DataBuffer implementations: offsets may increase backing
            // capacity even though getSize reports a smaller logical region.
            for (int offset : data.getOffsets()) {
                physical += (data.getSize() + Math.max(0L, (long) offset)) * bytesPerElement;
            }
        }
        long bitsPerPixel = Math.max(1L, image.getColorModel().getPixelSize());
        long pixelBytes = (long) image.getWidth() * image.getHeight() * ((bitsPerPixel + 7L) / 8L);
        return Math.max(pixelBytes, Math.max(logical, physical));
    }

    static byte[] readLimitedTileBytes(InputStream input, long declaredLength) throws IOException {
        if (declaredLength > MAX_TILE_BYTES) {
            throw new IOException("XYZ tile exceeds maximum encoded size of " + MAX_TILE_BYTES + " bytes.");
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream(16 * 1024);
        byte[] buffer = new byte[8192];
        long total = 0L;
        int read;
        while ((read = input.read(buffer)) != -1) {
            if (read == 0) {
                continue;
            }
            total += read;
            if (total > MAX_TILE_BYTES) {
                throw new IOException("XYZ tile exceeds maximum encoded size of " + MAX_TILE_BYTES + " bytes.");
            }
            output.write(buffer, 0, read);
        }
        if (declaredLength >= 0 && declaredLength != total) {
            throw new IOException("XYZ tile body length does not match Content-Length.");
        }
        return output.toByteArray();
    }

    static BufferedImage decodeTileBytes(byte[] payload) throws IOException {
        try (ImageInputStream stream =
                new MemoryCacheImageInputStream(new ByteArrayInputStream(payload))) {
            return decodeCheckedTile(stream);
        }
    }

    static BufferedImage readCachedTile(File tileFile) throws IOException {
        if (tileFile.length() > MAX_TILE_BYTES) {
            throw new IOException("Cached XYZ tile exceeds maximum encoded size.");
        }
        try (ImageInputStream stream = ImageIO.createImageInputStream(tileFile)) {
            if (stream == null) {
                return null;
            }
            return decodeCheckedTile(stream);
        }
    }

    private static BufferedImage decodeCheckedTile(ImageInputStream stream) throws IOException {
        Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
        if (!readers.hasNext()) {
            return null;
        }
        ImageReader reader = readers.next();
        try {
            reader.setInput(stream, true, true);
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            if (width <= 0 || height <= 0
                    || width > MAX_TILE_DIMENSION || height > MAX_TILE_DIMENSION
                    || (long) width * height > MAX_TILE_PIXELS) {
                throw new IOException("XYZ tile image dimensions exceed bounded decode policy.");
            }
            return reader.read(0);
        } finally {
            reader.dispose();
        }
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

    /**
     * Stable versioned namespace for the immutable source configuration.
     * Source IDs alone are unsafe: users can change a provider URL while keeping
     * an ID, and old tiles must never masquerade as the new geographic imagery.
     * Hashing also prevents raw API tokens or path segments entering disk names.
     * Legacy ID-only disk entries are intentionally NOT reused.
     */
    static String cacheNamespace(OnlineRasterSource source) {
        StringBuilder identity = new StringBuilder(256);
        appendIdentityPart(identity, source.getId());
        appendIdentityPart(identity, source.getServiceType().name());
        appendIdentityPart(identity, source.getUrlTemplate());
        appendIdentityPart(identity, Integer.toString(source.getTileSize()));
        appendIdentityPart(identity, source.getSourceCRS());
        appendIdentityPart(identity, Boolean.toString(source.isRequiresApiKey()));
        try {
            byte[] sha256 = MessageDigest.getInstance("SHA-256")
                    .digest(identity.toString().getBytes(StandardCharsets.UTF_8));
            return "v2-" + HexFormat.of().formatHex(sha256, 0, 16);
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is required for safe tile cache identity", error);
        }
    }

    private static void appendIdentityPart(StringBuilder identity, String value) {
        identity.append(value.length()).append(':').append(value);
    }

    static String buildKey(OnlineRasterSource source, int z, int x, int y) {
        return cacheNamespace(source) + "_" + z + "_" + x + "_" + y;
    }

    static File resolveTileFile(OnlineRasterSource source, int z, int x, int y) {
        String extension = guessFormat(source);
        return new File(CACHE_DIR, cacheNamespace(source) + File.separator
                + z + File.separator + x + File.separator + y + "." + extension);
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
        if (source == null) {
            return;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (nx < 0 || ny < 0) continue;
                String key = buildKey(source, z, nx, ny);
                if (MEMORY_CACHE.containsKey(key) || PENDING.contains(key)
                        || !PREFETCH_PENDING.add(key)) {
                    continue;
                }
                File tileFile = resolveTileFile(source, z, nx, ny);
                try {
                    EXECUTOR.submit(() -> {
                        try {
                            // No disk metadata or ImageIO decode on the map's EDT.
                            if (tileFile.isFile() && tileFile.length() > 100) {
                                BufferedImage image = readCachedTile(tileFile);
                                if (image != null
                                        && !isUnavailablePlaceholder(source, image, tileFile.length())) {
                                    putMemoryTile(key, image);
                                }
                            }
                        } catch (Exception error) {
                            CatgisLogger.warn("OnlineTileCache: neighboring tile prefetch failed", error);
                        } finally {
                            PREFETCH_PENDING.remove(key);
                        }
                    });
                } catch (RejectedExecutionException shutdown) {
                    PREFETCH_PENDING.remove(key);
                }
            }
        }
    }
}
