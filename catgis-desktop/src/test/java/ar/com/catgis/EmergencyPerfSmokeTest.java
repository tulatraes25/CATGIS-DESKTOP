package ar.com.catgis;

import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.raster.LocalRasterData;
import com.sun.net.httpserver.HttpServer;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Envelope;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Real Terrarium PNG -> loopback HTTP -> raster GeoTIFF -> reopened DEM.
 * Tests are hermetic: no public Internet, global temp paths or credentials.
 */
class EmergencyPerfSmokeTest {
    @TempDir Path temporary;

    @AfterEach
    void restoreProject() {
        AppContext.setCurrentProject(null);
    }

    @Test
    void measuresPanRelatedCrsAndDemPath() throws Exception {
        AppContext.setCurrentProject(new Project("Emergency"));
        AppContext.project().setProjectCRS("EPSG:22182");
        MapPanel map = new MapPanel();
        map.setSize(1200, 800);
        MapUtilities utilities = new MapUtilities(map);
        Envelope projected = new Envelope(-6540000, -6530000, -4100000, -4090000);
        for (int i = 0; i < 42; i++) {
            Envelope transformed = utilities.reprojectEnvelopeIfNeeded(
                    new Envelope(projected), "EPSG:3857", "EPSG:22182");
            assertNotNull(transformed, "CRS conversion cannot disappear silently");
            assertTrue(!transformed.isNull(), "Transformed CRS envelope must not be null");
        }

        // Terrarium RGB(128,42,0) encodes exactly +42 metres.
        BufferedImage tile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = tile.createGraphics();
        try {
            graphics.setColor(new Color(128, 42, 0));
            graphics.fillRect(0, 0, 256, 256);
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(tile, "png", encoded));
        byte[] png = encoded.toByteArray();
        AtomicInteger requests = new AtomicInteger();

        HttpServer server = HttpServer.create(
                new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 0);
        server.createContext("/terrain/", exchange -> {
            try {
                if (!"GET".equals(exchange.getRequestMethod())
                        || !exchange.getRequestURI().getPath().endsWith(".png")) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }
                requests.incrementAndGet();
                exchange.getResponseHeaders().add("Content-Type", "image/png");
                exchange.sendResponseHeaders(200, png.length);
                try (var body = exchange.getResponseBody()) {
                    body.write(png);
                }
            } finally {
                exchange.close();
            }
        });
        server.start();

        Path tif = temporary.resolve("synthetic-terrain.tif");
        try {
            String tileBase = "http://127.0.0.1:" + server.getAddress().getPort() + "/terrain";
            Envelope bbox = new Envelope(-58.50, -58.49, -34.70, -34.69);
            PublicTerrainTilesDemService.FileDownloadResult result =
                    PublicTerrainTilesDemService.downloadFromTileBase(
                            TerrainTilesDataset.TERRARIUM_GLOBAL, bbox,
                            PublicDemDetailLevel.BALANCED, tif.toFile(), tileBase);
            assertTrue(result.tileCount() > 0);
            assertEquals(result.tileCount(), requests.get(), "Every planned tile fetched from local server");
            assertTrue(Files.size(tif) > 100, "A real TIFF must have been produced");
            assertTrue(result.width() > 0 && result.height() > 0);

            // Use streams owned by this test; GeoTools may keep Windows file
            // handles after reader.dispose() when constructed from a File.
            try (FileInputStream rasterFile = new FileInputStream(tif.toFile());
                    ImageInputStream rasterInput = ImageIO.createImageInputStream(rasterFile)) {
                assertNotNull(rasterInput, "An explicit TIFF image stream is required");
                GeoTiffReader reader = new GeoTiffReader(rasterInput);
                org.geotools.coverage.grid.GridCoverage2D coverage = null;
                try {
                    coverage = reader.read((org.geotools.api.parameter.GeneralParameterValue[]) null);
                    assertNotNull(coverage);
                    var raster = coverage.getRenderedImage().getData();
                    assertEquals(42.0, raster.getSampleDouble(0, 0, 0), 0.2,
                            "Actual TIFF pixel must preserve Terrarium elevation, not just metadata");
                } finally {
                    try {
                        if (coverage != null) {
                            coverage.dispose(true);
                        }
                    } finally {
                        reader.dispose();
                    }
                }
            }

            LocalRasterData data = RasterImageLoader.loadReal(
                    tif.toFile(), "EPSG:22182", "EPSG:3857");
            assertNotNull(data.getImage());
            assertTrue(data.isGeoreferenced(), "Raster must survive GeoTIFF reopen with geokeys");
            assertEquals(result.width(), data.getWidth());
            assertEquals(result.height(), data.getHeight());
            data.dispose();
            // Explicitly verify that Windows released the generated TIFF handle
            // before JUnit attempts to clean the temporary directory.
            Files.delete(tif);
            assertTrue(Files.notExists(tif), "TIFF must be unlocked on every supported OS");
        } finally {
            server.stop(0);
        }
    }
}
