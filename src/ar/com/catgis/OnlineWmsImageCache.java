package ar.com.catgis;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class OnlineWmsImageCache {

    private static final String USER_AGENT = "CATGIS Desktop/1.0 (+wms)";
    private static final File CACHE_DIR = new File(System.getProperty("java.io.tmpdir"), "catgis-online-wms");
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3, runnable -> {
        Thread thread = new Thread(runnable, "catgis-online-wms");
        thread.setDaemon(true);
        return thread;
    });
    private static final Map<String, BufferedImage> MEMORY_CACHE = Collections.synchronizedMap(
            new LinkedHashMap<String, BufferedImage>(48, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
                    return size() > 48;
                }
            }
    );
    private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();
    private static final Map<String, Long> RETRY_AFTER = new ConcurrentHashMap<>();

    private OnlineWmsImageCache() {
    }

    public static BufferedImage getImage(String requestUrl, String formatHint, Runnable repaintCallback) {
        if (requestUrl == null || requestUrl.isBlank()) {
            return null;
        }

        String key = sha1(requestUrl);
        BufferedImage cached = MEMORY_CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        File file = new File(CACHE_DIR, key + "." + extension(formatHint));
        if (file.isFile()) {
            try {
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    MEMORY_CACHE.put(key, image);
                    return image;
                }
            } catch (Exception ignored) {
            }
        }

        Long retryAfter = RETRY_AFTER.get(key);
        if (retryAfter != null && retryAfter > System.currentTimeMillis()) {
            return null;
        }

        if (PENDING.add(key)) {
            EXECUTOR.submit(() -> fetch(requestUrl, formatHint, key, file, repaintCallback));
        }
        return null;
    }

    private static void fetch(String requestUrl, String formatHint, String key, File file, Runnable repaintCallback) {
        try {
            CACHE_DIR.mkdirs();
            HttpURLConnection connection = (HttpURLConnection) new URL(requestUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(18000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", formatHint != null && !formatHint.isBlank() ? formatHint + ",image/*;q=0.9,*/*;q=0.6" : "image/*,*/*;q=0.6");

            int code = connection.getResponseCode();
            if (code >= 200 && code < 300) {
                try (InputStream input = connection.getInputStream()) {
                    BufferedImage image = ImageIO.read(input);
                    if (image != null) {
                        ImageIO.write(image, extension(formatHint), file);
                        MEMORY_CACHE.put(key, image);
                        RETRY_AFTER.remove(key);
                        if (repaintCallback != null) {
                            SwingUtilities.invokeLater(repaintCallback);
                        }
                    }
                }
            } else {
                RETRY_AFTER.put(key, System.currentTimeMillis() + 30000L);
            }
        } catch (Exception ex) {
            RETRY_AFTER.put(key, System.currentTimeMillis() + 30000L);
        } finally {
            PENDING.remove(key);
        }
    }

    private static String extension(String formatHint) {
        String f = formatHint != null ? formatHint.toLowerCase() : "";
        if (f.contains("jpeg") || f.contains("jpg")) {
            return "jpg";
        }
        return "png";
    }

    private static String sha1(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            return Integer.toHexString(text.hashCode());
        }
    }
}
