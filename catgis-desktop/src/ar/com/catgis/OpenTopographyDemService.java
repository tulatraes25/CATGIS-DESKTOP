package ar.com.catgis;
import ar.com.catgis.data.raster.RasterCoverageSupport;

import org.locationtech.jts.geom.Envelope;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class OpenTopographyDemService {

    private static final String API_BASE = "https://portal.opentopography.org/API/globaldem";
    private static final DecimalFormat DECIMAL = new DecimalFormat("0.000000", DecimalFormatSymbols.getInstance(Locale.US));

    private OpenTopographyDemService() {
    }

    public static URI buildDownloadUri(OpenTopographyDataset dataset, Envelope latLonEnvelope, String apiKey) {
        if (dataset == null) {
            throw new IllegalArgumentException("Debe elegir un dataset DEM.");
        }
        Envelope normalized = normalizeWgs84Envelope(latLonEnvelope);
        if (normalized == null || normalized.isNull()) {
            throw new IllegalArgumentException("No se pudo determinar un area valida para descargar el DEM.");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Debes indicar una API key de OpenTopography.");
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("demtype", dataset.getApiCode());
        params.put("south", DECIMAL.format(normalized.getMinY()));
        params.put("north", DECIMAL.format(normalized.getMaxY()));
        params.put("west", DECIMAL.format(normalized.getMinX()));
        params.put("east", DECIMAL.format(normalized.getMaxX()));
        params.put("outputFormat", "GTiff");
        params.put("API_Key", apiKey.trim());

        StringBuilder query = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                query.append('&');
            }
            first = false;
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            query.append('=');
            query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return URI.create(API_BASE + "?" + query);
    }

    public static FileDownloadResult download(OpenTopographyDataset dataset,
                                              Envelope projectEnvelope,
                                              String projectCrs,
                                              String apiKey,
                                              java.io.File outputFile) throws Exception {
        long startedAt = System.nanoTime();
        if (outputFile == null) {
            throw new IllegalArgumentException("Debes indicar un archivo de salida para el DEM.");
        }
        java.io.File parent = outputFile.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de destino para el DEM descargado.");
        }

        Envelope latLonEnvelope = projectEnvelope;
        if (projectEnvelope != null && projectCrs != null && !projectCrs.isBlank()) {
            latLonEnvelope = RasterCoverageSupport.reprojectEnvelope(projectEnvelope, projectCrs, "EPSG:4326");
        }
        URI uri = buildDownloadUri(dataset, latLonEnvelope, apiKey);
        CatgisLogger.info("[EMERGENCY-DEM] OpenTopography request uri=" + uri);

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(java.time.Duration.ofSeconds(15))
                .build();
        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Accept", "*/*")
                .header("User-Agent", "CATGIS Desktop")
                .timeout(java.time.Duration.ofSeconds(60))
                .GET()
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        CatgisLogger.info("[EMERGENCY-DEM] OpenTopography response"
                + " status=" + response.statusCode()
                + " contentType=" + response.headers().firstValue("Content-Type").orElse("")
                + " file=" + outputFile.getAbsolutePath());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String errorText = readBodyText(response.body());
            throw new IOException("OpenTopography devolvio " + response.statusCode() + ": " + sanitizeError(errorText));
        }

        String contentType = response.headers().firstValue("Content-Type").orElse("").toLowerCase(Locale.ROOT);
        if (contentType.contains("text") || contentType.contains("json") || contentType.contains("xml") || contentType.contains("html")) {
            String errorText = readBodyText(response.body());
            throw new IOException("La respuesta del servicio no fue un GeoTIFF valido: " + sanitizeError(errorText));
        }

        try (InputStream stream = response.body()) {
            Files.copy(stream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        RasterSidecarSupport.write(
                outputFile,
                latLonEnvelope != null ? normalizeWgs84Envelope(latLonEnvelope) : null,
                dataset != null ? dataset.getSourceCrsCode() : "EPSG:4326"
        );
        CatgisLogger.info("[EMERGENCY-DEM] OpenTopography download finished in "
                + ((System.nanoTime() - startedAt) / 1_000_000L)
                + " ms file=" + outputFile.getAbsolutePath());
        return new FileDownloadResult(outputFile, latLonEnvelope != null ? normalizeWgs84Envelope(latLonEnvelope) : null, uri);
    }

    private static String readBodyText(InputStream inputStream) throws IOException {
        try (InputStream stream = inputStream) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String sanitizeError(String text) {
        if (text == null) {
            return "sin detalle adicional.";
        }
        String sanitized = text.replace('\n', ' ').replace('\r', ' ').trim();
        if (sanitized.length() > 220) {
            sanitized = sanitized.substring(0, 217) + "...";
        }
        return sanitized.isBlank() ? "sin detalle adicional." : sanitized;
    }

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

    public record FileDownloadResult(java.io.File file, Envelope latLonEnvelope, URI requestUri) {
    }
}
