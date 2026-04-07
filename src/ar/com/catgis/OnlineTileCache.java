package ar.com.catgis;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class OnlineTileCache {

    private static final String USER_AGENT = "CATGIS Desktop/1.0 (+online-basemaps)";
    private static final File CACHE_DIR = new File(System.getProperty("java.io.tmpdir"), "catgis-online-tiles");
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable, "catgis-online-tile");
        thread.setDaemon(true);
        return thread;
    });
    private static final Map<String, BufferedImage> MEMORY_CACHE = Collections.synchronizedMap(
            new LinkedHashMap<String, BufferedImage>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
                    return size() > 256;
                }
            }
    );
    private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();
    private static final Map<String, Long> RETRY_AFTER = new ConcurrentHashMap<>();

    private OnlineTileCache() {
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
        try {
            File parent = tileFile.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            URL url = new URL(OnlineMapUtils.buildTileUrl(source, z, x, y));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(12000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8");

            int code = connection.getResponseCode();
            if (code >= 200 && code < 300) {
                try (InputStream input = connection.getInputStream()) {
                    BufferedImage image = ImageIO.read(input);
                    if (image != null) {
                        String format = guessFormat(source);
                        ImageIO.write(image, format, tileFile);
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

    private static String guessFormat(OnlineRasterSource source) {
        String template = source != null ? source.getUrlTemplate().toLowerCase() : "";
        if (template.contains(".jpg") || template.contains(".jpeg")) {
            return "jpg";
        }
        return "png";
    }

    private static String buildKey(OnlineRasterSource source, int z, int x, int y) {
        return source.getId() + "_" + z + "_" + x + "_" + y;
    }

    private static File resolveTileFile(OnlineRasterSource source, int z, int x, int y) {
        String folderName = source.getId().replaceAll("[^a-zA-Z0-9._-]", "_");
        String extension = guessFormat(source);
        return new File(CACHE_DIR, folderName + File.separator + z + File.separator + x + File.separator + y + "." + extension);
    }
}
