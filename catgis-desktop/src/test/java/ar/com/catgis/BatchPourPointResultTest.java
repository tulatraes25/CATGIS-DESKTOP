package ar.com.catgis;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.data.vector.ShapefileData;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;

import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.WritableRaster;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import ar.com.catgis.core.model.Layer;

class BatchPourPointResultTest {

    @AfterEach
    void tearDown() {
        ReleaseTestSupport.clearAppContext();
    }

    @Test
    void generatesBasinsFromMultipleOutletsInProjectCrs() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-batch-outlets");
        Path demPath = tempDir.resolve("dem_batch_3857.tif");
        writeSplitBasinDem3857(demPath);

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Batch outlets 22182");
            AppContext.project().setProjectCRS("EPSG:22182");

            LocalRasterData demData = RasterImageLoader.loadReal(demPath.toFile(), "EPSG:22182", "EPSG:3857");
            RasterLayer demLayer = new RasterLayer("DEM batch outlet", demPath.toString());
            demLayer.setSourceName("DEM local");
            demLayer.setFeatureCount(1);
            demLayer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(demData, "EPSG:22182"));
            AppContext.project().addLayer(demLayer);
            CatgisDesktopApp.layersPanel.addLayer(demLayer);
            CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(demLayer, demData);

            double[] left = CoordinateTransformSupport.transformPoint(-7664550, -3877600, "EPSG:3857", "EPSG:22182");
            double[] right = CoordinateTransformSupport.transformPoint(-7661500, -3877600, "EPSG:3857", "EPSG:22182");
            SimpleFeatureType outletType = ReleaseTestSupport.createFeatureType(
                    "outlets_batch",
                    "EPSG:22182",
                    Point.class,
                    new Object[][]{{"name", String.class}}
            );
            SimpleFeature leftFeature = ReleaseTestSupport.buildFeature(
                    outletType,
                    "outlets_batch.1",
                    ReleaseTestSupport.GEOMETRY_FACTORY.createPoint(new Coordinate(left[0], left[1])),
                    "Outlet izquierda"
            );
            SimpleFeature rightFeature = ReleaseTestSupport.buildFeature(
                    outletType,
                    "outlets_batch.2",
                    ReleaseTestSupport.GEOMETRY_FACTORY.createPoint(new Coordinate(right[0], right[1])),
                    "Outlet derecha"
            );
            Envelope outletsEnvelope = new Envelope();
            outletsEnvelope.expandToInclude(((Point) leftFeature.getDefaultGeometry()).getCoordinate());
            outletsEnvelope.expandToInclude(((Point) rightFeature.getDefaultGeometry()).getCoordinate());
            ShapefileData outletData = new ShapefileData(
                    List.of(leftFeature, rightFeature),
                    outletsEnvelope,
                    "Outlets batch",
                    2,
                    "ok",
                    outletType
            );

            TerrainHydrologyAnalysisService.BatchPourPointResult result =
                    TerrainHydrologyAnalysisService.generateBasinsFromOutletLayer(
                            new TerrainHydrologyAnalysisService.BatchPourPointRequest(
                                    demLayer,
                                    "DEM batch outlet",
                                    DrainageExtractionService.AnalysisDetail.BALANCED,
                                    DrainageExtractionService.HydrologicConditioning.ADVANCED,
                                    4,
                                    6,
                                    outletData,
                                    "EPSG:22182",
                                    "Outlets batch"
                            )
                    );

            assertNotNull(result.basinsLayer());
            assertNotNull(result.outletsLayer());
            assertEquals("EPSG:22182", result.basinsLayer().layer().getSourceCRS());
            assertEquals("EPSG:22182", result.outletsLayer().layer().getSourceCRS());
            assertTrue(result.basinsLayer().data().getFeatureCount() >= 2);
            assertTrue(result.outletsLayer().data().getFeatureCount() >= 2);
            assertTrue(result.generatedCount() >= 2);
        });
    }

    private static void writeSplitBasinDem3857(Path file) throws Exception {
        int width = 24;
        int height = 18;
        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(width * height);
        float[] values = buffer.getData();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                double valley = col < (width / 2)
                        ? Math.abs(col - 4) * 3.2d
                        : Math.abs(col - 19) * 3.2d;
                double ridge = (col >= 10 && col <= 13) ? 30d : 0d;
                double downslope = (height - row) * 5.5d;
                values[(row * width) + col] = (float) (180d + valley + ridge + downslope);
            }
        }
        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, buffer, null);
        ReferencedEnvelope envelope = new ReferencedEnvelope(
                -7665200, -7660400,
                -3881200, -3877000,
                CRSDefinitions.decode("EPSG:3857", true)
        );
        GridCoverage2D coverage = new GridCoverageFactory().create("batch-dem-3857", raster, envelope);
        GeoTiffWriter writer = new GeoTiffWriter(file.toFile());
        try {
            writer.write(coverage, (org.geotools.api.parameter.GeneralParameterValue[]) null);
        } finally {
            writer.dispose();
        }
        RasterSidecarSupport.write(
                file.toFile(),
                new Envelope(envelope.getMinX(), envelope.getMaxX(), envelope.getMinY(), envelope.getMaxY()),
                "EPSG:3857"
        );
    }
}