package ar.com.catgis;

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
import java.util.Locale;

public final class SoilGridsDownloadService {

    static final URI DEFAULT_WCS_BASE = URI.create("https://maps.isric.org/mapserv");
    private static final DecimalFormat DECIMAL = new DecimalFormat("0.000000", DecimalFormatSymbols.getInstance(Locale.US));

    private SoilGridsDownloadService() {
    }

    public static URI buildDownloadUri(SoilGridsDataset dataset, Envelope latLonEnvelope) {
        return buildDownloadUri(dataset, latLonEnvelope, DEFAULT_WCS_BASE);
    }

    static URI buildDownloadUri(SoilGridsDataset dataset, Envelope latLonEnvelope, URI baseUri) {
        if (dataset == null) {
            throw new IllegalArgumentException("Debes elegir un dataset de suelos.");
        }
        Envelope normalized = normalizeWgs84Envelope(latLonEnvelope);
        if (normalized == null || normalized.isNull()) {
            throw new IllegalArgumentException("No se pudo determinar un area valida para descargar suelos.");
        }
        URI safeBase = baseUri != null ? baseUri : DEFAULT_WCS_BASE;

        StringBuilder query = new StringBuilder();
        appendQuery(query, "map", "/map/" + dataset.getMapName() + ".map");
        appendQuery(query, "SERVICE", "WCS");
        appendQuery(query, "VERSION", "2.0.1");
        appendQuery(query, "REQUEST", "GetCoverage");
        appendQuery(query, "COVERAGEID", dataset.getCoverageId());
        appendQuery(query, "FORMAT", "GEOTIFF_INT16");
        appendQuery(query, "SUBSET", "X(" + DECIMAL.format(normalized.getMinX()) + "," + DECIMAL.format(normalized.getMaxX()) + ")");
        appendQuery(query, "SUBSET", "Y(" + DECIMAL.format(normalized.getMinY()) + "," + DECIMAL.format(normalized.getMaxY()) + ")");
        appendQuery(query, "SUBSETTINGCRS", "http://www.opengis.net/def/crs/EPSG/0/4326");
        appendQuery(query, "OUTPUTCRS", "http://www.opengis.net/def/crs/EPSG/0/4326");
        return URI.create(safeBase.toString() + "?" + query);
    }

    public static FileDownloadResult download(SoilGridsDataset dataset,
                                              Envelope projectEnvelope,
                                              String projectCrs,
                                              java.io.File outputFile) throws Exception {
        return download(dataset, projectEnvelope, projectCrs, outputFile, DEFAULT_WCS_BASE);
    }

    static FileDownloadResult download(SoilGridsDataset dataset,
                                       Envelope projectEnvelope,
                                       String projectCrs,
                                       java.io.File outputFile,
                                       URI baseUri) throws Exception {
        if (outputFile == null) {
            throw new IllegalArgumentException("Debes indicar un archivo de salida para el mapa de suelos.");
        }
        java.io.File parent = outputFile.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de destino para el mapa de suelos.");
        }

        Envelope latLonEnvelope = projectEnvelope;
        if (projectEnvelope != null && projectCrs != null && !projectCrs.isBlank()) {
            latLonEnvelope = RasterCoverageSupport.reprojectEnvelope(projectEnvelope, projectCrs, "EPSG:4326");
        }
        URI uri = buildDownloadUri(dataset, latLonEnvelope, baseUri);

        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Accept", "*/*")
                .header("User-Agent", "CATGIS Desktop Review")
                .GET()
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String errorText = readBodyText(response.body());
            throw new IOException("SoilGrids devolvio " + response.statusCode() + ": " + sanitizeError(errorText));
        }

        String contentType = response.headers().firstValue("Content-Type").orElse("").toLowerCase(Locale.ROOT);
        if (contentType.contains("text") || contentType.contains("json") || contentType.contains("xml") || contentType.contains("html")) {
            String errorText = readBodyText(response.body());
            throw new IOException("La respuesta del servicio de suelos no fue un GeoTIFF valido: " + sanitizeError(errorText));
        }

        try (InputStream stream = response.body()) {
            Files.copy(stream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        Envelope normalized = latLonEnvelope != null ? normalizeWgs84Envelope(latLonEnvelope) : null;
        RasterSidecarSupport.write(outputFile, normalized, dataset.getSourceCrsCode());
        return new FileDownloadResult(outputFile, normalized, uri, dataset);
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

    private static void appendQuery(StringBuilder query, String key, String value) {
        if (!query.isEmpty()) {
            query.append('&');
        }
        query.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
        query.append('=');
        query.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
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

    public record FileDownloadResult(java.io.File file, Envelope latLonEnvelope, URI requestUri, SoilGridsDataset dataset) {
    }
}
