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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class OpenTopographyDemService {

    private static final String API_BASE = "https://portal.opentopography.org/API/globaldem";

    private OpenTopographyDemService() {
    }

    /** Returns the authenticated request URI. Treat it as sensitive, never diagnostic text. */
    public static URI buildDownloadUri(OpenTopographyDataset dataset, Envelope latLonEnvelope, String apiKey) {
        return encodeRequest(dataset, requestBounds(latLonEnvelope), apiKey);
    }

    private static URI encodeRequest(OpenTopographyDataset dataset,
                                     OpenTopographyRequestBounds bounds, String apiKey) {
        if (dataset == null) {
            throw new IllegalArgumentException("Debe elegir un dataset DEM.");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Debes indicar una API key de OpenTopography.");
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("demtype", dataset.getApiCode());
        params.put("south", bounds.southText());
        params.put("north", bounds.northText());
        params.put("west", bounds.westText());
        params.put("east", bounds.eastText());
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

    /** Omitted projectCrs retains the historical API convention: input coordinates are WGS84 lon/lat. */
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
        // Snapshot the source before projection. Later caller mutation must not
        // change the geographic description of the already-issued request.
        Envelope latLonEnvelope = snapshotFiniteArea(projectEnvelope);
        if (projectCrs != null && !projectCrs.isBlank()) {
            latLonEnvelope = RasterCoverageSupport.reprojectEnvelope(latLonEnvelope, projectCrs, "EPSG:4326");
        }
        OpenTopographyRequestBounds bounds = requestBounds(latLonEnvelope);
        URI uri = encodeRequest(dataset, bounds, apiKey);
        checkInterrupted();
        // Invalid coordinates, precision or authentication never create output
        // directories or contact the provider. Keep valid mkdir behavior.
        java.io.File parent = outputFile.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de destino para el DEM descargado.");
        }

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
            // This records the effective REQUEST extent, not verified embedded
            // GeoTIFF bounds. Delivered raster validation is a separate gate.
            RasterSidecarSupport.write(outputFile, requestEnvelope(bounds), dataset.getSourceCrsCode());
            CatgisLogger.info("[EMERGENCY-DEM] OpenTopography download finished in "
                    + ((System.nanoTime() - startedAt) / 1_000_000L) + " ms");
            return new FileDownloadResult(outputFile, requestEnvelope(bounds), uri);
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

    private static Envelope snapshotFiniteArea(Envelope envelope) {
        if (envelope == null || envelope.isNull()) {
            throw new IllegalArgumentException("No se pudo determinar un area valida para descargar el DEM.");
        }
        double west = envelope.getMinX();
        double east = envelope.getMaxX();
        double south = envelope.getMinY();
        double north = envelope.getMaxY();
        OpenTopographyRequestBounds.requireFiniteArea(west, east, south, north);
        return new Envelope(west, east, south, north);
    }

    private static OpenTopographyRequestBounds requestBounds(Envelope envelope) {
        Envelope snapshot = snapshotFiniteArea(envelope);
        return OpenTopographyRequestBounds.of(snapshot.getMinX(), snapshot.getMaxX(),
                snapshot.getMinY(), snapshot.getMaxY());
    }

    private static Envelope requestEnvelope(OpenTopographyRequestBounds bounds) {
        return new Envelope(bounds.westValue(), bounds.eastValue(), bounds.southValue(), bounds.northValue());
    }

    /** requestUri() is sensitive and retained for API compatibility, not logging. */
    public record FileDownloadResult(java.io.File file, Envelope latLonEnvelope, URI requestUri) {
        @Override
        public String toString() {
            return "FileDownloadResult[file=<local>, requestUri=<sensitive>]";
        }
    }
}
