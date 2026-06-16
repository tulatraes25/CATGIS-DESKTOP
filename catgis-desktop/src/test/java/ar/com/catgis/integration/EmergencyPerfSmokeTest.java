package ar.com.catgis.integration;

import ar.com.catgis.AppContext;
import ar.com.catgis.MapPanel;
import ar.com.catgis.MapUtilities;
import ar.com.catgis.PublicDemDetailLevel;
import ar.com.catgis.PublicTerrainTilesDemService;
import ar.com.catgis.RasterImageLoader;
import ar.com.catgis.TerrainTilesDataset;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.raster.LocalRasterData;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

import java.io.File;

class EmergencyPerfSmokeTest {

    @Test
    void measuresPanRelatedCrsAndDemPath() throws Exception {
        AppContext.setCurrentProject(new Project("Emergency"));
        AppContext.project().setProjectCRS("EPSG:22182");

        MapPanel map = new MapPanel();
        map.setSize(1200, 800);
        MapUtilities utilities = new MapUtilities(map);

        Envelope env3857 = new Envelope(-6540000, -6530000, -4100000, -4090000);
        long reprojStart = System.nanoTime();
        for (int i = 0; i < 42; i++) {
            utilities.reprojectEnvelopeIfNeeded(new Envelope(env3857), "EPSG:3857", "EPSG:22182");
        }
        long reprojMs = (System.nanoTime() - reprojStart) / 1_000_000L;
        System.out.println("[EMERGENCY-TEST] REPROJECT_42_MS=" + reprojMs);

        Envelope bbox = new Envelope(-58.55, -58.35, -34.75, -34.55);
        File out = new File(System.getProperty("java.io.tmpdir"), "catgis-emergency-dem.tif");
        long downloadStart = System.nanoTime();
        PublicTerrainTilesDemService.FileDownloadResult result =
                PublicTerrainTilesDemService.download(TerrainTilesDataset.TERRARIUM_GLOBAL, bbox, PublicDemDetailLevel.BALANCED, out);
        long downloadMs = (System.nanoTime() - downloadStart) / 1_000_000L;
        System.out.println("[EMERGENCY-TEST] DEM_DOWNLOAD_MS=" + downloadMs
                + " tiles=" + result.tileCount()
                + " zoom=" + result.zoom()
                + " file=" + result.file().getAbsolutePath());

        long loadStart = System.nanoTime();
        LocalRasterData raster = RasterImageLoader.loadReal(out, "EPSG:22182", "EPSG:3857");
        long loadMs = (System.nanoTime() - loadStart) / 1_000_000L;
        System.out.println("[EMERGENCY-TEST] DEM_LOADREAL_MS=" + loadMs
                + " image=" + raster.getImage().getWidth() + "x" + raster.getImage().getHeight()
                + " displayCrs=" + raster.getDisplayCRS());
    }
}
