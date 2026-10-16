package ar.com.catgis;

import ar.com.catgis.data.raster.RasterCoverageSupport;

import org.locationtech.jts.geom.Envelope;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
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

    /** Returns the authenticated request URI. Treat it as sensitive, never diagnostic text. */
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
        return download(dataset, projectEnvelope, projectCrs, apiKey, outputFile, request -> {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(java.time.Duration.ofSeconds(15))
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        });
    }

    /** Package-level transport seam: tests use synthetic credentials, never the public service. */
    @FunctionalInterface
    interface RequestSender {
        HttpResponse<InputStream> send(HttpRequest request) throws IOException, InterruptedException;
    }

    static FileDownloadResult download(OpenTopographyDataset dataset,
                                        Envelope projectEnvelope, String projectCrs,
                                        String apiKey, java.io.File outputFile,
                                        RequestSender sender) throws Exception {
        long startedAt = System.nanoTime();
        if (outputFile == null) {
            throw new IllegalArgumentException("Debes indicar un archivo de salida para el DEM.");
        }
        checkInterrupted();
        java.io.File parent = outputFile.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de destino para el DEM descargado.");
        }

        Envelope latLonEnvelope = projectEnvelope;
        if (projectEnvelope != null && projectCrs != null && !projectCrs.isBlank()) {
            latLonEnvelope = RasterCoverageSupport.reprojectEnvelope(projectEnvelope, projectCrs, "EPSG:4326");
        }
        URI uri = buildDownloadUri(dataset, latLonEnvelope, apiKey);
        // Only fixed local event names and numeric fields belong in diagnostics.
        // The request URI, response headers/body and exception chains may echo credentials.
        CatgisLogger.info("[EMERGENCY-DEM] OpenTopography request started");
        String failureCode = "REQUEST";
        try {
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("Accept", "*/*")
                    .header("User-Agent", "CATGIS Desktop")
                    .timeout(java.time.Duration.ofSeconds(60))
                    .GET()
                    .build();
            failureCode = "TRANSPORT";
            HttpResponse<InputStream> response = sender.send(request);
            failureCode = "RESPONSE";
            try (InputStream stream = response.body()) {
                checkInterrupted();
                int status = response.statusCode();
                CatgisLogger.info("[EMERGENCY-DEM] OpenTopography response status=" + status);
                failureCode = "HTTP_" + status;
                if (status < 200 || status >= 300) {
                    // Do not read a rejected remote body, not even a truncated prefix.
                    throw new IOException();
                }
                failureCode = "CONTENT_TYPE";
                String contentType = response.headers().firstValue("Content-Type")
                        .orElse("").toLowerCase(Locale.ROOT);
                if (contentType.contains("text") || contentType.contains("json")
                        || contentType.contains("xml") || contentType.contains("html")) {
                    throw new IOException();
                }
                failureCode = "BODY";
                if (stream == null) {
                    throw new IOException();
                }
                failureCode = "COPY";
                // Existing publication semantics are unchanged. Atomic raster +
                // sidecar replacement/content validation remains a separate gate.
                Files.copy(stream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                failureCode = "CLOSE";
            }
            checkInterrupted();
            failureCode = "SIDECAR";
            RasterSidecarSupport.write(outputFile,
                    latLonEnvelope != null ? normalizeWgs84Envelope(latLonEnvelope) : null,
                    dataset.getSourceCrsCode());
            CatgisLogger.info("[EMERGENCY-DEM] OpenTopography download finished in "
                    + ((System.nanoTime() - startedAt) / 1_000_000L) + " ms");
            return new FileDownloadResult(outputFile,
                    latLonEnvelope != null ? normalizeWgs84Envelope(latLonEnvelope) : null, uri);
        } catch (java.net.http.HttpTimeoutException | java.net.SocketTimeoutException timeout) {
            throw safeFailure("TIMEOUT");
        } catch (InterruptedException | InterruptedIOException interrupted) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Descarga OpenTopography cancelada [INTERRUPTED].");
        } catch (Exception failure) {
            checkInterrupted();
            // Deliberately do not retain remote messages, causes or suppressed
            // close errors. Callers routinely log complete exception chains.
            throw safeFailure(failureCode);
        }
    }

    private static IOException safeFailure(String localCode) {
        return new IOException("No se pudo completar la descarga OpenTopography ["
                + localCode + "]. No se incluyen detalles remotos ni credenciales.");
    }

    private static void checkInterrupted() throws InterruptedIOException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedIOException("Descarga OpenTopography cancelada [INTERRUPTED].");
        }
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

    /** requestUri() is sensitive and retained for API compatibility, not logging. */
    public record FileDownloadResult(java.io.File file, Envelope latLonEnvelope, URI requestUri) {
        @Override
        public String toString() {
            return "FileDownloadResult[file=<local>, requestUri=<sensitive>]";
        }
    }
}
