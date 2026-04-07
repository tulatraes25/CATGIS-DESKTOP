package ar.com.catgis;

import org.geotools.api.parameter.GeneralParameterValue;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Envelope;

import javax.imageio.ImageIO;
import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;

public final class PublicTerrainTilesDemService {

    private static final String TILE_BASE = "https://s3.amazonaws.com/elevation-tiles-prod/terrarium";
    private static final int TILE_SIZE = 256;
    private static final int MIN_ZOOM = 5;
    private static final int MAX_ZOOM = 13;
    private static final int MAX_OUTPUT_DIMENSION = 2048;
    private static final int MAX_TILE_COUNT = 42;
    private static final double WEB_MERCATOR_LIMIT = 85.05112878d;
    private static final double EARTH_RADIUS = 6378137d;

    private PublicTerrainTilesDemService() {
    }

    public static FileDownloadResult download(TerrainTilesDataset dataset,
                                              Envelope latLonEnvelope,
                                              PublicDemDetailLevel detailLevel,
                                              java.io.File outputFile) throws Exception {
        if (dataset == null) {
            throw new IllegalArgumentException("Debes elegir una fuente DEM publica.");
        }
        if (outputFile == null) {
            throw new IllegalArgumentException("Debes indicar un archivo de salida para el DEM.");
        }
        java.io.File parent = outputFile.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de destino para el DEM.");
        }

        Envelope normalized = normalizeWgs84Envelope(latLonEnvelope);
        if (normalized == null || normalized.isNull()) {
            throw new IllegalArgumentException("No se pudo determinar un area valida para descargar el DEM.");
        }

        DownloadPlan plan = choosePlan(normalized, detailLevel);
        writeGeoTiff(plan, outputFile);
        return new FileDownloadResult(outputFile, normalized, plan.zoom, plan.tileCount, plan.outputWidth, plan.outputHeight, dataset.getSourceLabel());
    }

    public static PlanSummary estimatePlan(Envelope latLonEnvelope, PublicDemDetailLevel detailLevel) {
        Envelope normalized = normalizeWgs84Envelope(latLonEnvelope);
        if (normalized == null || normalized.isNull()) {
            throw new IllegalArgumentException("No se pudo determinar un area valida para el DEM.");
        }
        DownloadPlan plan = choosePlan(normalized, detailLevel);
        return new PlanSummary(
                plan.zoom,
                plan.tileCount,
                plan.outputWidth,
                plan.outputHeight,
                estimateResolutionMeters(normalized, plan.zoom),
                plan.tileCount >= (MAX_TILE_COUNT * 0.8),
                Math.max(plan.outputWidth, plan.outputHeight) >= (MAX_OUTPUT_DIMENSION * 0.8)
        );
    }

    private static DownloadPlan choosePlan(Envelope latLonEnvelope, PublicDemDetailLevel detailLevel) {
        PublicDemDetailLevel resolved = detailLevel != null ? detailLevel : PublicDemDetailLevel.BALANCED;
        int maxZoom = Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, resolved.preferredMaxZoom()));
        int minZoom = Math.min(maxZoom, Math.max(MIN_ZOOM, resolved.preferredMinZoom()));

        for (int zoom = maxZoom; zoom >= minZoom; zoom--) {
            DownloadPlan plan = buildPlan(latLonEnvelope, zoom);
            if (plan.tileCount <= MAX_TILE_COUNT
                    && plan.outputWidth > 0
                    && plan.outputHeight > 0
                    && Math.max(plan.outputWidth, plan.outputHeight) <= MAX_OUTPUT_DIMENSION) {
                return plan;
            }
        }
        for (int zoom = minZoom - 1; zoom >= MIN_ZOOM; zoom--) {
            DownloadPlan plan = buildPlan(latLonEnvelope, zoom);
            if (plan.tileCount <= MAX_TILE_COUNT
                    && plan.outputWidth > 0
                    && plan.outputHeight > 0
                    && Math.max(plan.outputWidth, plan.outputHeight) <= MAX_OUTPUT_DIMENSION) {
                return plan;
            }
        }
        DownloadPlan fallback = buildPlan(latLonEnvelope, MIN_ZOOM);
        if (fallback.tileCount > MAX_TILE_COUNT) {
            throw new IllegalArgumentException("El area solicitada es demasiado grande para una descarga DEM publica compacta. Acerca la vista o recorta el area.");
        }
        return fallback;
    }

    private static DownloadPlan buildPlan(Envelope latLonEnvelope, int zoom) {
        double minTileX = lonToTileX(latLonEnvelope.getMinX(), zoom);
        double maxTileX = lonToTileX(latLonEnvelope.getMaxX(), zoom);
        double minTileY = latToTileY(latLonEnvelope.getMaxY(), zoom);
        double maxTileY = latToTileY(latLonEnvelope.getMinY(), zoom);

        int tileMinX = Math.max(0, (int) Math.floor(minTileX));
        int tileMaxX = Math.max(tileMinX, (int) Math.floor(maxTileX));
        int tileMinY = Math.max(0, (int) Math.floor(minTileY));
        int tileMaxY = Math.max(tileMinY, (int) Math.floor(maxTileY));

        int mosaicWidth = ((tileMaxX - tileMinX) + 1) * TILE_SIZE;
        int mosaicHeight = ((tileMaxY - tileMinY) + 1) * TILE_SIZE;

        int cropMinX = Math.max(0, (int) Math.floor((minTileX - tileMinX) * TILE_SIZE));
        int cropMaxX = Math.min(mosaicWidth, (int) Math.ceil((maxTileX - tileMinX) * TILE_SIZE));
        int cropMinY = Math.max(0, (int) Math.floor((minTileY - tileMinY) * TILE_SIZE));
        int cropMaxY = Math.min(mosaicHeight, (int) Math.ceil((maxTileY - tileMinY) * TILE_SIZE));

        int outputWidth = Math.max(1, cropMaxX - cropMinX);
        int outputHeight = Math.max(1, cropMaxY - cropMinY);
        int tileCount = (tileMaxX - tileMinX + 1) * (tileMaxY - tileMinY + 1);

        return new DownloadPlan(
                latLonEnvelope,
                zoom,
                tileMinX,
                tileMaxX,
                tileMinY,
                tileMaxY,
                cropMinX,
                cropMinY,
                outputWidth,
                outputHeight,
                tileCount
        );
    }

    private static void writeGeoTiff(DownloadPlan plan, java.io.File outputFile) throws Exception {
        SampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, plan.outputWidth, plan.outputHeight, 1);
        DataBufferFloat buffer = new DataBufferFloat(plan.outputWidth * plan.outputHeight);
        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, buffer, null);
        float[] elevationData = buffer.getData();

        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        int mosaicWidth = ((plan.tileMaxX - plan.tileMinX) + 1) * TILE_SIZE;

        for (int tileY = plan.tileMinY; tileY <= plan.tileMaxY; tileY++) {
            for (int tileX = plan.tileMinX; tileX <= plan.tileMaxX; tileX++) {
                int placementX = (tileX - plan.tileMinX) * TILE_SIZE;
                int placementY = (tileY - plan.tileMinY) * TILE_SIZE;
                placeTile(client, plan, elevationData, mosaicWidth, tileX, tileY, placementX, placementY);
            }
        }

        ReferencedEnvelope env3857 = new ReferencedEnvelope(
                lonToMetersX(plan.latLonEnvelope.getMinX()),
                lonToMetersX(plan.latLonEnvelope.getMaxX()),
                latToMetersY(plan.latLonEnvelope.getMinY()),
                latToMetersY(plan.latLonEnvelope.getMaxY()),
                CRS.decode("EPSG:3857", true)
        );

        GridCoverage2D coverage = new GridCoverageFactory().create("terrain-tiles-dem", raster, env3857);
        GeoTiffWriter writer = new GeoTiffWriter(outputFile);
        try {
            writer.write(coverage, (GeneralParameterValue[]) null);
        } finally {
            writer.dispose();
        }
    }

    private static void placeTile(HttpClient client,
                                  DownloadPlan plan,
                                  float[] destination,
                                  int mosaicWidth,
                                  int tileX,
                                  int tileY,
                                  int placementX,
                                  int placementY) throws Exception {
        URI uri = URI.create(TILE_BASE + "/" + plan.zoom + "/" + tileX + "/" + tileY + ".png");
        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Accept", "image/png")
                .header("User-Agent", "CATGIS Desktop Review")
                .GET()
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Terrain Tiles devolvio " + response.statusCode() + " para " + uri);
        }

        try (InputStream body = response.body()) {
            java.awt.image.BufferedImage image = ImageIO.read(body);
            if (image == null) {
                throw new IOException("La respuesta del DEM publico no fue una imagen valida para " + uri);
            }
            for (int py = 0; py < TILE_SIZE; py++) {
                int mosaicY = placementY + py;
                if (mosaicY < plan.cropMinY || mosaicY >= plan.cropMinY + plan.outputHeight) {
                    continue;
                }
                int targetY = mosaicY - plan.cropMinY;
                for (int px = 0; px < TILE_SIZE; px++) {
                    int mosaicX = placementX + px;
                    if (mosaicX < plan.cropMinX || mosaicX >= plan.cropMinX + plan.outputWidth) {
                        continue;
                    }
                    int targetX = mosaicX - plan.cropMinX;
                    int rgb = image.getRGB(px, py);
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    float elevation = (float) ((red * 256d + green + (blue / 256d)) - 32768d);
                    destination[targetY * plan.outputWidth + targetX] = elevation;
                }
            }
        }
    }

    private static Envelope normalizeWgs84Envelope(Envelope envelope) {
        if (envelope == null || envelope.isNull()) {
            return null;
        }
        double south = clampLatitude(Math.min(envelope.getMinY(), envelope.getMaxY()));
        double north = clampLatitude(Math.max(envelope.getMinY(), envelope.getMaxY()));
        double west = Math.max(-180d, Math.min(180d, Math.min(envelope.getMinX(), envelope.getMaxX())));
        double east = Math.max(-180d, Math.min(180d, Math.max(envelope.getMinX(), envelope.getMaxX())));
        if (north <= south || east <= west) {
            return null;
        }
        return new Envelope(west, east, south, north);
    }

    private static double clampLatitude(double value) {
        return Math.max(-WEB_MERCATOR_LIMIT, Math.min(WEB_MERCATOR_LIMIT, value));
    }

    private static double lonToTileX(double lon, int zoom) {
        double tiles = 1 << zoom;
        return ((lon + 180d) / 360d) * tiles;
    }

    private static double latToTileY(double lat, int zoom) {
        double tiles = 1 << zoom;
        double latRad = Math.toRadians(clampLatitude(lat));
        double mercator = Math.log(Math.tan(Math.PI / 4d + latRad / 2d));
        return (1d - mercator / Math.PI) / 2d * tiles;
    }

    private static double lonToMetersX(double lon) {
        return EARTH_RADIUS * Math.toRadians(lon);
    }

    private static double latToMetersY(double lat) {
        double latRad = Math.toRadians(clampLatitude(lat));
        return EARTH_RADIUS * Math.log(Math.tan(Math.PI / 4d + latRad / 2d));
    }

    private static double estimateResolutionMeters(Envelope latLonEnvelope, int zoom) {
        double centerLat = (latLonEnvelope.getMinY() + latLonEnvelope.getMaxY()) / 2d;
        double metersPerPixel = 156543.03392804097d * Math.cos(Math.toRadians(clampLatitude(centerLat))) / (1 << zoom);
        return Math.max(0.1d, metersPerPixel);
    }

    public record FileDownloadResult(java.io.File file,
                                     Envelope latLonEnvelope,
                                     int zoom,
                                     int tileCount,
                                     int width,
                                     int height,
                                     String sourceLabel) {
    }

    public record PlanSummary(int zoom,
                              int tileCount,
                              int width,
                              int height,
                              double estimatedResolutionMeters,
                              boolean nearTileLimit,
                              boolean nearOutputLimit) {
    }

    private record DownloadPlan(Envelope latLonEnvelope,
                                int zoom,
                                int tileMinX,
                                int tileMaxX,
                                int tileMinY,
                                int tileMaxY,
                                int cropMinX,
                                int cropMinY,
                                int outputWidth,
                                int outputHeight,
                                int tileCount) {
    }
}
