package ar.com.catgis;

import org.locationtech.jts.geom.Envelope;

public final class OnlineMapUtils {

    public static final double WEB_MERCATOR_LIMIT = 20037508.342789244;
    public static final double WEB_MERCATOR_WORLD_WIDTH = WEB_MERCATOR_LIMIT * 2.0;
    public static final Envelope WEB_MERCATOR_WORLD = new Envelope(
            -WEB_MERCATOR_LIMIT,
            WEB_MERCATOR_LIMIT,
            -WEB_MERCATOR_LIMIT,
            WEB_MERCATOR_LIMIT
    );

    private OnlineMapUtils() {
    }

    public static Envelope clampToWorld(Envelope envelope) {
        if (envelope == null || envelope.isNull()) {
            return null;
        }
        return new Envelope(envelope).intersection(WEB_MERCATOR_WORLD);
    }

    public static int chooseZoom(Envelope mercatorView, int panelWidth, int panelHeight, OnlineRasterSource source) {
        if (mercatorView == null || mercatorView.isNull() || source == null) {
            return source != null ? source.getMinZoom() : 0;
        }

        double width = Math.max(1.0, mercatorView.getWidth());
        double height = Math.max(1.0, mercatorView.getHeight());
        double resolutionX = width / Math.max(1, panelWidth);
        double resolutionY = height / Math.max(1, panelHeight);
        double resolution = Math.max(resolutionX, resolutionY);

        double baseResolution = WEB_MERCATOR_WORLD_WIDTH / source.getTileSize();
        int zoom = (int) Math.round(Math.log(baseResolution / resolution) / Math.log(2.0));
        zoom = Math.max(source.getMinZoom(), Math.min(source.getMaxZoom(), zoom));

        while (zoom > source.getMinZoom()) {
            TileRange range = calculateTileRange(mercatorView, zoom);
            if (range.tileCount() <= 120) {
                break;
            }
            zoom--;
        }

        return zoom;
    }

    public static TileRange calculateTileRange(Envelope mercatorView, int zoom) {
        Envelope view = clampToWorld(mercatorView);
        if (view == null || view.isNull()) {
            return new TileRange(0, -1, 0, -1);
        }

        int tileCountAtZoom = 1 << Math.max(0, zoom);
        int minX = clampTileIndex(tileXFromMercator(view.getMinX(), zoom), tileCountAtZoom);
        int maxX = clampTileIndex(tileXFromMercator(view.getMaxX(), zoom), tileCountAtZoom);
        int minY = clampTileIndex(tileYFromMercator(view.getMaxY(), zoom), tileCountAtZoom);
        int maxY = clampTileIndex(tileYFromMercator(view.getMinY(), zoom), tileCountAtZoom);

        return new TileRange(minX, maxX, minY, maxY);
    }

    public static Envelope tileBounds(int x, int y, int zoom) {
        int tileCountAtZoom = 1 << Math.max(0, zoom);
        double minX = ((double) x / tileCountAtZoom) * WEB_MERCATOR_WORLD_WIDTH - WEB_MERCATOR_LIMIT;
        double maxX = ((double) (x + 1) / tileCountAtZoom) * WEB_MERCATOR_WORLD_WIDTH - WEB_MERCATOR_LIMIT;
        double maxY = WEB_MERCATOR_LIMIT - ((double) y / tileCountAtZoom) * WEB_MERCATOR_WORLD_WIDTH;
        double minY = WEB_MERCATOR_LIMIT - ((double) (y + 1) / tileCountAtZoom) * WEB_MERCATOR_WORLD_WIDTH;
        return new Envelope(minX, maxX, minY, maxY);
    }

    public static String buildTileUrl(OnlineRasterSource source, int z, int x, int y) {
        if (source == null) {
            return "";
        }
        return source.getUrlTemplate()
                .replace("{z}", String.valueOf(z))
                .replace("{x}", String.valueOf(x))
                .replace("{y}", String.valueOf(y));
    }

    private static int tileXFromMercator(double mercatorX, int zoom) {
        int n = 1 << Math.max(0, zoom);
        double normalized = (mercatorX + WEB_MERCATOR_LIMIT) / WEB_MERCATOR_WORLD_WIDTH;
        return (int) Math.floor(normalized * n);
    }

    private static int tileYFromMercator(double mercatorY, int zoom) {
        int n = 1 << Math.max(0, zoom);
        double normalized = (WEB_MERCATOR_LIMIT - mercatorY) / WEB_MERCATOR_WORLD_WIDTH;
        return (int) Math.floor(normalized * n);
    }

    private static int clampTileIndex(int value, int tileCountAtZoom) {
        return Math.max(0, Math.min(tileCountAtZoom - 1, value));
    }

    public static final class TileRange {
        public final int minX;
        public final int maxX;
        public final int minY;
        public final int maxY;

        public TileRange(int minX, int maxX, int minY, int maxY) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }

        public int tileCount() {
            if (maxX < minX || maxY < minY) {
                return 0;
            }
            return (maxX - minX + 1) * (maxY - minY + 1);
        }
    }
}
