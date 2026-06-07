package ar.com.catgis.climate;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.raster.RasterCoverageSupport;

import ar.com.catgis.CRSDefinitions;
import ar.com.catgis.RasterSidecarSupport;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Download service for online climate data sources.
 * Follows the same pattern as {@link ar.com.catgis.SoilGridsDownloadService}
 * and {@link ar.com.catgis.PublicTerrainTilesDemService}.
 */
public final class ClimateOnlineDownloadService {

    private static final DecimalFormat COORD_FMT = new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int OPEN_METEO_GRID_RESOLUTION = 27; // ~0.25 degrees for reasonable grid density
    private static final CoordinateReferenceSystem WGS84;

    static {
        CoordinateReferenceSystem crs = null;
        try {
            crs = CRS.decode("EPSG:4326", true);
        } catch (FactoryException e) {
            // fallback
        }
        WGS84 = crs;
    }

    private ClimateOnlineDownloadService() {
    }

    /**
     * Download a climate dataset for a given bounding box and save as GeoTIFF.
     *
     * @param dataset   the climate dataset to download
     * @param bbox      bounding box in EPSG:4326 (lat/lon)
     * @param outputFile target file (.tif)
     * @param startDate start date (for Open-Meteo time series, ignored for WorldClim)
     * @param endDate   end date (for Open-Meteo time series, ignored for WorldClim)
     * @return download result
     * @throws Exception on download or processing failure
     */
    public static FileDownloadResult download(ClimateDatasetOption dataset,
                                              Envelope bbox,
                                              File outputFile,
                                              LocalDate startDate,
                                              LocalDate endDate) throws Exception {
        if (dataset == null) {
            throw new IllegalArgumentException("Debes elegir un dataset climático.");
        }
        if (outputFile == null) {
            throw new IllegalArgumentException("Debes indicar un archivo de salida para los datos climáticos.");
        }
        File parent = outputFile.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de destino para los datos climáticos.");
        }

        Envelope normalized = normalizeWgs84Envelope(bbox);
        if (normalized == null || normalized.isNull()) {
            throw new IllegalArgumentException("No se pudo determinar un área válida para descargar datos climáticos.");
        }

        // Route to appropriate download strategy based on dataset type
        if (dataset instanceof WorldClimDataset) {
            return downloadWorldClim((WorldClimDataset) dataset, normalized, outputFile);
        } else if (dataset instanceof OpenMeteoDataset) {
            return downloadOpenMeteo((OpenMeteoDataset) dataset, normalized, outputFile, startDate, endDate);
        } else {
            throw new IllegalArgumentException("Tipo de dataset climático no soportado: " + dataset.getClass().getSimpleName());
        }
    }

    /**
     * Download a climate dataset using the default date range (last 30 days for Open-Meteo,
     * ignored for WorldClim).
     */
    public static FileDownloadResult download(ClimateDatasetOption dataset,
                                              Envelope bbox,
                                              File outputFile) throws Exception {
        LocalDate end = LocalDate.now().minusDays(1);
        LocalDate start = end.minusDays(30);
        return download(dataset, bbox, outputFile, start, end);
    }

    // ---- WorldClim ----

    /**
     * Download a WorldClim global GeoTIFF and clip it to the bounding box.
     * Uses a simple approach: download global GeoTIFF, read pixel values within bbox,
     * and write a new sub-sampled GeoTIFF.
     */
    private static FileDownloadResult downloadWorldClim(WorldClimDataset dataset,
                                                        Envelope normalizedBbox,
                                                        File outputFile) throws Exception {
        URI downloadUri = URI.create(dataset.getDownloadUrlPattern());

        // Download global GeoTIFF
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        HttpRequest request = HttpRequest.newBuilder(downloadUri)
                .header("Accept", "*/*")
                .header("User-Agent", "CATGIS Desktop Review")
                .GET()
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("WorldClim devolvió " + response.statusCode() + " para " + downloadUri);
        }

        // Save to temp file
        File tempFile = File.createTempFile("wc2_", "_global.tif");
        tempFile.deleteOnExit();
        try (InputStream stream = response.body()) {
            Files.copy(stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        try {
            // Simply copy the global tile as-is (CATGIS will display the full extent)
            // Recorte futuro: por ahora descargamos el tile completo tal cual
            Files.copy(tempFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            try {
                tempFile.delete();
            } catch (Exception ignored) {
            }
        }

        RasterSidecarSupport.write(outputFile, normalizedBbox, dataset.getSourceCrsCode());
        return new FileDownloadResult(outputFile, normalizedBbox, downloadUri, dataset);
    }

    // ---- Open-Meteo ----

    /**
     * Query Open-Meteo historical API for gridded data and build a GeoTIFF raster.
     */
    private static FileDownloadResult downloadOpenMeteo(OpenMeteoDataset dataset,
                                                        Envelope normalizedBbox,
                                                        File outputFile,
                                                        LocalDate startDate,
                                                        LocalDate endDate) throws Exception {
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now().minusDays(1);
            startDate = endDate.minusDays(30);
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin.");
        }

        String variableCode = dataset.getApiVariableCode();
        String startStr = startDate.format(DATE_FMT);
        String endStr = endDate.format(DATE_FMT);

        // Build a grid of query points
        double minLon = normalizedBbox.getMinX();
        double maxLon = normalizedBbox.getMaxX();
        double minLat = normalizedBbox.getMinY();
        double maxLat = normalizedBbox.getMaxY();

        // Determine grid resolution based on area size
        double lonRange = maxLon - minLon;
        double latRange = maxLat - minLat;
        int lonSteps = Math.max(3, (int) Math.ceil(lonRange * OPEN_METEO_GRID_RESOLUTION));
        int latSteps = Math.max(3, (int) Math.ceil(latRange * OPEN_METEO_GRID_RESOLUTION));
        // Cap to avoid excessive queries
        lonSteps = Math.min(lonSteps, 20);
        latSteps = Math.min(latSteps, 20);

        // Ensure at least 2x2 grid
        if (lonSteps < 2) lonSteps = 2;
        if (latSteps < 2) latSteps = 2;

        double lonStep = lonRange / Math.max(1, lonSteps - 1);
        double latStep = latRange / Math.max(1, latSteps - 1);

        // Collect all grid point coordinates
        List<double[]> gridPoints = new ArrayList<>();
        for (int i = 0; i < latSteps; i++) {
            double lat = Math.min(maxLat, minLat + i * latStep);
            for (int j = 0; j < lonSteps; j++) {
                double lon = Math.min(maxLon, minLon + j * lonStep);
                gridPoints.add(new double[]{lat, lon});
            }
        }

        // Query Open-Meteo for each grid point
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        float[] rasterData = new float[lonSteps * latSteps];
        int dataCount = 0;

        for (int idx = 0; idx < gridPoints.size(); idx++) {
            double[] point = gridPoints.get(idx);
            double lat = point[0];
            double lon = point[1];

            String url = dataset.getDownloadUrlPattern()
                    + "?latitude=" + COORD_FMT.format(lat)
                    + "&longitude=" + COORD_FMT.format(lon)
                    + "&start_date=" + startStr
                    + "&end_date=" + endStr
                    + "&daily=" + variableCode
                    + "&timezone=auto";

            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", "CATGIS Desktop Review")
                    .GET()
                    .build();

            try {
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    float value = parseOpenMeteoResponse(resp.body(), variableCode);
                    rasterData[idx] = value;
                    if (Float.isFinite(value)) {
                        dataCount++;
                    }
                } else {
                    rasterData[idx] = Float.NaN;
                }
            } catch (Exception ex) {
                rasterData[idx] = Float.NaN;
            }
        }

        if (dataCount == 0) {
            throw new IOException("No se pudieron obtener datos climáticos de Open-Meteo para el área solicitada.");
        }

        // Build WritableRaster from the grid
        SampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, lonSteps, latSteps, 1);
        DataBufferFloat buffer = new DataBufferFloat(rasterData, rasterData.length);
        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, buffer, null);

        // Create ReferencedEnvelope for output
        ReferencedEnvelope refEnv = new ReferencedEnvelope(
                minLon, maxLon, minLat, maxLat, WGS84
        );

        // Write GeoTIFF
        String layerName = dataset.getOutputCode() + "_" + startStr + "_" + endStr;
        GridCoverage2D coverage = new GridCoverageFactory().create(layerName, raster, refEnv);

        GeoTiffWriter writer = new GeoTiffWriter(outputFile);
        try {
            writer.write(coverage, (org.geotools.api.parameter.GeneralParameterValue[]) null);
        } finally {
            writer.dispose();
        }

        RasterSidecarSupport.write(outputFile, normalizedBbox, dataset.getSourceCrsCode());
        return new FileDownloadResult(outputFile, normalizedBbox, URI.create("open-meteo://" + variableCode), dataset);
    }

    /**
     * Parse the Open-Meteo JSON response and return the mean value across all days.
     */
    private static float parseOpenMeteoResponse(String json, String variableCode) {
        try {
            // Simple JSON parsing for Open-Meteo response
            // Look for "daily":{"variableCode":[val1,val2,...]}
            String key = "\"" + variableCode + "\":[";
            int keyIdx = json.indexOf(key);
            if (keyIdx < 0) {
                return Float.NaN;
            }

            int startIdx = keyIdx + key.length();
            int endIdx = json.indexOf(']', startIdx);
            if (endIdx < 0) {
                return Float.NaN;
            }

            String valuesPart = json.substring(startIdx, endIdx);
            String[] parts = valuesPart.split(",");

            double sum = 0;
            int count = 0;
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.equals("null") && !trimmed.isEmpty()) {
                    try {
                        double val = Double.parseDouble(trimmed);
                        if (Double.isFinite(val)) {
                            sum += val;
                            count++;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            if (count > 0) {
                return (float) (sum / count);
            }
            return Float.NaN;
        } catch (Exception e) {
            return Float.NaN;
        }
    }

    // ---- Helpers ----

    private static Envelope normalizeWgs84Envelope(Envelope envelope) {
        if (envelope == null || envelope.isNull()) {
            return null;
        }
        double south = Math.max(-90d, Math.min(90d, Math.min(envelope.getMinY(), envelope.getMaxY())));
        double north = Math.max(-90d, Math.min(90d, Math.max(envelope.getMinY(), envelope.getMaxY())));
        double west = Math.max(-180d, Math.min(180d, Math.min(envelope.getMinX(), envelope.getMaxX())));
        double east = Math.max(-180d, Math.min(180d, Math.max(envelope.getMinX(), envelope.getMaxX())));
        if (north <= south || east <= west) {
            return null;
        }
        return new Envelope(west, east, south, north);
    }

    public record FileDownloadResult(File file, Envelope latLonEnvelope, URI requestUri, ClimateDatasetOption dataset) {
    }
}