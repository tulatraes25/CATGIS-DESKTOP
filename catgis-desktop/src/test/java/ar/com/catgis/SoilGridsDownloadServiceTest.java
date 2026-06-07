package ar.com.catgis;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.core.model.Layer;

import com.sun.net.httpserver.HttpServer;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoilGridsDownloadServiceTest {

    @AfterEach
    void tearDown() {
        ReleaseTestSupport.clearAppContext();
    }

    @Test
    void buildsSoilGridsWcsUriForSubsetDownload() {
        Envelope envelope = new Envelope(-68.60, -68.40, -45.90, -45.70);

        URI uri = SoilGridsDownloadService.buildDownloadUri(SoilGridsDataset.CLAY_0_5_Q50, envelope);
        String text = uri.toString();

        assertTrue(text.contains("maps.isric.org/mapserv"));
        assertTrue(text.contains("map=%2Fmap%2Fclay.map"));
        assertTrue(text.contains("SERVICE=WCS"));
        assertTrue(text.contains("COVERAGEID=clay_0-5cm_Q0.5"));
        assertTrue(text.contains("SUBSETTINGCRS=http%3A%2F%2Fwww.opengis.net%2Fdef%2Fcrs%2FEPSG%2F0%2F4326"));
        assertTrue(text.contains("OUTPUTCRS=http%3A%2F%2Fwww.opengis.net%2Fdef%2Fcrs%2FEPSG%2F0%2F4326"));
    }

    @Test
    void downloadsSoilRasterAndKeepsItOperationalForProjectCrs() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-soilgrids");
        Path sourcePath = tempDir.resolve("soilgrids_source_4326.tif");
        Path outputPath = tempDir.resolve("soilgrids_download_4326.tif");
        writeSoilGrid4326(sourcePath);

        AtomicReference<String> queryRef = new AtomicReference<>("");
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/mapserv", exchange -> {
            queryRef.set(exchange.getRequestURI().getRawQuery());
            byte[] data = Files.readAllBytes(sourcePath);
            exchange.getResponseHeaders().add("Content-Type", "image/tiff");
            exchange.sendResponseHeaders(200, data.length);
            exchange.getResponseBody().write(data);
            exchange.close();
        });
        server.start();
        try {
            URI baseUri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/mapserv");
            Envelope requestedEnvelope = new Envelope(-68.60, -68.40, -45.90, -45.70);
            SoilGridsDownloadService.FileDownloadResult result = SoilGridsDownloadService.download(
                    SoilGridsDataset.CLAY_0_5_Q50,
                    requestedEnvelope,
                    "EPSG:4326",
                    outputPath.toFile(),
                    baseUri
            );

            assertTrue(Files.exists(outputPath));
            assertTrue(Files.exists(RasterSidecarSupport.sidecarFile(outputPath.toFile()).toPath()));
            assertTrue(queryRef.get().contains("COVERAGEID=clay_0-5cm_Q0.5"));

            ReleaseTestSupport.runOnEdt(() -> {
                try {
                    ReleaseTestSupport.initializeAppContext("Soils 22182");
                    CatgisDesktopApp.currentProject.setProjectCRS("EPSG:22182");

                    LocalRasterData rasterData = RasterImageLoader.loadReal(result.file(), "EPSG:22182", "EPSG:4326");
                    assertEquals("EPSG:22182", rasterData.getDisplayCRS());
                    RasterLayer layer = new RasterLayer("Arcilla 0-5 cm", result.file().getAbsolutePath());
                    String layerOperationalCrs = CRSDefinitions.normalizeCode(rasterData.getDisplayCRS());
                    if (layerOperationalCrs.isBlank()) {
                        layerOperationalCrs = RasterCoverageSupport.resolveOperationalRasterCrs(rasterData, "EPSG:22182");
                    }
                    layer.setSourceCRS(layerOperationalCrs);
                    assertEquals("EPSG:22182", layer.getSourceCRS());
                    assertTrue(rasterData.getWidth() > 0);
                    assertTrue(rasterData.getHeight() > 0);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        } finally {
            server.stop(0);
        }
    }

    private static void writeSoilGrid4326(Path file) throws Exception {
        int width = 16;
        int height = 16;
        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(width * height);
        float[] values = buffer.getData();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                values[(row * width) + col] = 15f + row + (col * 0.5f);
            }
        }
        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, buffer, null);
        ReferencedEnvelope envelope = new ReferencedEnvelope(
                -68.60, -68.40,
                -45.90, -45.70,
                CRSDefinitions.decode("EPSG:4326", true)
        );
        GridCoverage2D coverage = new GridCoverageFactory().create("soilgrids-clay-4326", raster, envelope);
        GeoTiffWriter writer = new GeoTiffWriter(file.toFile());
        try {
            writer.write(coverage, (org.geotools.api.parameter.GeneralParameterValue[]) null);
        } finally {
            writer.dispose();
        }
        RasterSidecarSupport.write(
                file.toFile(),
                new Envelope(envelope.getMinX(), envelope.getMaxX(), envelope.getMinY(), envelope.getMaxY()),
                "EPSG:4326"
        );
    }
}
