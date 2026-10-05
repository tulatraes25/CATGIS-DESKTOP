package ar.com.catgis;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;

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
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectContourOperationalCrsRoundTripTest {

    @AfterEach
    void tearDown() {
        ReleaseTestSupport.clearAppContext();
    }

    @Test
    void savesAndReloadsContoursInProjectCrs() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-project-contours-22182");
        Path demPath = tempDir.resolve("dem_3857.tif");
        Path projectFile = tempDir.resolve("Proyecto curvas 22182.catgis");
        writeDem3857(demPath);

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Proyecto curvas 22182");
            AppContext.project().setProjectCRS("EPSG:22182");
            AppContext.project().setProjectFile(projectFile.toFile());

            LocalRasterData demData = RasterImageLoader.loadReal(demPath.toFile(), "EPSG:22182", "EPSG:3857");
            RasterLayer demLayer = new RasterLayer("DEM curvas 22182", demPath.toString());
            demLayer.setSourceName("DEM local");
            demLayer.setFeatureCount(1);
            demLayer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(demData, "EPSG:22182"));
            AppContext.project().addLayer(demLayer);
            CatgisDesktopApp.layersPanel.addLayer(demLayer);
            AppContext.mapPanel().addOrUpdateRasterLayer(demLayer, demData);

            ContourGenerationService.GeneratedContourLayer contours =
                    ContourGenerationService.generateContours(demLayer, 10d, 5, "Curvas 10m - DEM curvas 22182", true, false, null);
            ShapefileData projectedContours = TopographyWorkflowSupport.projectVectorDataToCurrentProject(
                    contours.layer(),
                    contours.data()
            );
            contours.layer().setSourceName(projectedContours.getSourceName());
            contours.layer().setFeatureCount(projectedContours.getFeatureCount());
            AppContext.project().addLayer(contours.layer());
            CatgisDesktopApp.layersPanel.addLayer(contours.layer());
            AppContext.mapPanel().addOrUpdateShapefileLayer(contours.layer(), projectedContours);

            assertTrue(SaveProjectAction.saveProjectToFile(projectFile.toFile(), false));
        });

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Reload curvas 22182");
            assertTrue(LoadProjectAction.loadProjectFile(projectFile.toFile(), false));
            assertNotNull(AppContext.project());
            assertEquals("EPSG:22182", AppContext.project().getProjectCRS());

            Layer demLayer = findLayer("DEM curvas 22182");
            Layer contourLayer = findLayer("Curvas 10m - DEM curvas 22182");

            assertNotNull(demLayer);
            assertNotNull(contourLayer);
            assertEquals("EPSG:3857", demLayer.getSourceCRS());
            assertEquals("EPSG:22182", contourLayer.getSourceCRS());
            LocalRasterData demData = AppContext.mapPanel().getRasterData(demLayer);
            assertNotNull(demData);
            assertEquals("EPSG:22182", demData.getDisplayCRS());

            ShapefileData contourData = AppContext.mapPanel().getShapefileData(contourLayer);
            assertNotNull(contourData);
            assertNotNull(contourData.getSchema());
            assertTrue(contourData.getFeatureCount() > 0);
            assertEquals("EPSG:22182",
                    CRSDefinitions.normalizeCode(org.geotools.referencing.CRS.toSRS(
                            contourData.getSchema().getCoordinateReferenceSystem(),
                            true
                    )));
        });
    }

    private static Layer findLayer(String name) {
        return AppContext.project().getLayers().stream()
                .filter(layer -> name.equals(layer.getName()))
                .findFirst()
                .orElse(null);
    }

    private static void writeDem3857(Path file) throws Exception {
        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, 16, 16, 1);
        DataBufferFloat buffer = new DataBufferFloat(16 * 16);
        float[] values = buffer.getData();
        for (int row = 0; row < 16; row++) {
            for (int col = 0; col < 16; col++) {
                values[row * 16 + col] = 400f + (row * 5f) - (Math.abs(col - 8) * 8f);
            }
        }
        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, buffer, null);
        ReferencedEnvelope envelope = new ReferencedEnvelope(
                -7665200, -7661200,
                -3881200, -3877200,
                CRSDefinitions.decode("EPSG:3857", true)
        );
        GridCoverage2D coverage = new GridCoverageFactory().create("dem-3857", raster, envelope);
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
