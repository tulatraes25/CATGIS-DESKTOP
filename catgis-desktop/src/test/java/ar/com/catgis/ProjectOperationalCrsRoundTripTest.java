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

class ProjectOperationalCrsRoundTripTest {

    @AfterEach
    void tearDown() {
        ReleaseTestSupport.clearAppContext();
    }

    @Test
    void savesAndReloadsTopographyWorkflowInProjectCrs() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-project-crs-22182");
        Path demPath = tempDir.resolve("dem_3857.tif");
        Path projectFile = tempDir.resolve("Proyecto 22182.catgis");
        Path drainagePath = tempDir.resolve("escorrentias_22182.shp");
        Path basinsPath = tempDir.resolve("cuencas_22182.shp");
        writeDem3857(demPath);

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Proyecto 22182");
            CatgisDesktopApp.currentProject.setProjectCRS("EPSG:22182");
            CatgisDesktopApp.currentProject.setProjectFile(projectFile.toFile());

            LocalRasterData demData = RasterImageLoader.loadReal(demPath.toFile(), "EPSG:22182", "EPSG:3857");
            RasterLayer demLayer = new RasterLayer("DEM 22182", demPath.toString());
            demLayer.setSourceName("DEM local");
            demLayer.setFeatureCount(1);
            demLayer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(demData, "EPSG:22182"));
            CatgisDesktopApp.currentProject.addLayer(demLayer);
            CatgisDesktopApp.layersPanel.addLayer(demLayer);
            CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(demLayer, demData);

            DrainageExtractionService.GeneratedDrainageLayer drainage =
                    DrainageExtractionService.generateDrainage(
                            demLayer,
                            4,
                            "Escorrentias 22182",
                            DrainageExtractionService.AnalysisDetail.BALANCED,
                            DrainageExtractionService.HydrologicConditioning.ROBUST,
                            0d,
                            DrainageExtractionService.CleanupLevel.BALANCED
                    );
            ShapefileData projectedDrainage = TopographyWorkflowSupport.projectVectorDataToCurrentProject(
                    drainage.layer(),
                    drainage.data()
            );
            assertTrue(ExportVectorLayerAction.saveLayerDataToFile(
                    drainage.layer(),
                    projectedDrainage,
                    drainagePath.toFile(),
                    null,
                    false
            ));
            drainage.layer().setPath(drainagePath.toString());
            CatgisDesktopApp.currentProject.addLayer(drainage.layer());
            CatgisDesktopApp.layersPanel.addLayer(drainage.layer());
            CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(drainage.layer(), projectedDrainage);

            TerrainHydrologyAnalysisService.AnalysisResult analysis =
                    TerrainHydrologyAnalysisService.generateAnalysis(
                            new TerrainHydrologyAnalysisService.AnalysisRequest(
                                    demLayer,
                                    "Topo 22182",
                                    DrainageExtractionService.AnalysisDetail.BALANCED,
                                    DrainageExtractionService.HydrologicConditioning.ADVANCED,
                                    4,
                                    8,
                                    false,
                                    true,
                                    false,
                                    false,
                                    true,
                                    false,
                                    true,
                                    true,
                                    false
                            )
                    );

            TerrainHydrologyAnalysisService.GeneratedRasterLayer analysisRaster = analysis.rasterLayers().get(0);
            CatgisDesktopApp.currentProject.addLayer(analysisRaster.layer());
            CatgisDesktopApp.layersPanel.addLayer(analysisRaster.layer());
            CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(analysisRaster.layer(), analysisRaster.data());

            TerrainHydrologyAnalysisService.GeneratedVectorLayer basins = analysis.vectorLayers().stream()
                    .filter(layer -> "basins".equalsIgnoreCase(layer.operation()))
                    .findFirst()
                    .orElseThrow();
            ShapefileData projectedBasins = TopographyWorkflowSupport.projectVectorDataToCurrentProject(
                    basins.layer(),
                    basins.data()
            );
            assertTrue(ExportVectorLayerAction.saveLayerDataToFile(
                    basins.layer(),
                    projectedBasins,
                    basinsPath.toFile(),
                    null,
                    false
            ));
            basins.layer().setPath(basinsPath.toString());
            CatgisDesktopApp.currentProject.addLayer(basins.layer());
            CatgisDesktopApp.layersPanel.addLayer(basins.layer());
            CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(basins.layer(), projectedBasins);

            assertTrue(SaveProjectAction.saveProjectToFile(projectFile.toFile(), false));
        });

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Reload 22182");
            assertTrue(LoadProjectAction.loadProjectFile(projectFile.toFile(), false));
            assertNotNull(CatgisDesktopApp.currentProject);
            assertEquals("EPSG:22182", CatgisDesktopApp.currentProject.getProjectCRS());

            Layer demLayer = findLayer("DEM 22182");
            Layer drainageLayer = findLayer("Escorrentias 22182");
            Layer basinLayer = findLayer("Cuencas - Topo 22182");
            Layer analysisRaster = findLayer("Pendiente - Topo 22182");

            assertNotNull(demLayer);
            assertNotNull(drainageLayer);
            assertNotNull(basinLayer);
            assertNotNull(analysisRaster);

            assertEquals("EPSG:3857", demLayer.getSourceCRS());
            assertEquals("EPSG:22182", drainageLayer.getSourceCRS());
            assertEquals("EPSG:22182", basinLayer.getSourceCRS());
            assertEquals("EPSG:22182", analysisRaster.getSourceCRS());

            LocalRasterData demData = CatgisDesktopApp.mapPanel.getRasterData(demLayer);
            assertNotNull(demData);
            assertEquals("EPSG:22182", demData.getDisplayCRS());
            assertNotNull(CatgisDesktopApp.mapPanel.getRasterData(analysisRaster));
            assertNotNull(CatgisDesktopApp.mapPanel.getShapefileData(drainageLayer));
            assertNotNull(CatgisDesktopApp.mapPanel.getShapefileData(basinLayer));
        });
    }

    private static Layer findLayer(String name) {
        return CatgisDesktopApp.currentProject.getLayers().stream()
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
