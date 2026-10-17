package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.online.OnlineWmsLayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectLocalSourcePreflightContractTest {
    @TempDir Path directory;

    @AfterEach
    void cleanup() {
        AppContext.setCurrentProject(null);
    }

    @Test
    void missingVectorSourceCannotReplaceLiveDirtyProject() throws Exception {
        Project previous = new Project("Edición en curso");
        previous.setProjectCRS("EPSG:32719");
        previous.setModified(true);
        AppContext.setCurrentProject(previous);
        Path candidate = directory.resolve("con capas faltantes.catgis");
        Path missing = directory.resolve("no_existe.shp");
        Files.writeString(candidate, "CATGIS_PROJECT\n"
                + "PROJECT_CRS|EPSG:4326\n"
                + "[VECTOR]|Parcelas|" + missing + "\n", StandardCharsets.UTF_8);

        assertFalse(LoadProjectAction.loadProjectFile(candidate.toFile(), false));
        assertSame(previous, AppContext.project());
        assertEquals("EPSG:32719", previous.getProjectCRS());
        assertTrue(previous.isModified());
    }

    @Test
    void missingRasterAndEmptyPathAreRejected() throws Exception {
        RasterLayer raster = new RasterLayer("DEM", directory.resolve("missing.tif").toString());
        IOException failure = assertThrows(IOException.class,
                () -> ProjectLocalSourcePreflight.requireExistingLocalSource(raster));
        assertTrue(failure.getMessage().contains("DEM"));

        Layer vector = new Layer("Sin archivo", "", "VECTOR");
        assertThrows(IOException.class,
                () -> ProjectLocalSourcePreflight.requireExistingLocalSource(vector));
    }

    @Test
    void localVectorRasterGeoPackageAndLegacySpatiaLiteFilesAreAccepted() throws Exception {
        Path local = directory.resolve("data con espacios.gpkg");
        Files.write(local, new byte[]{0x01, 0x02});
        GeoPackageLayer gpkg = new GeoPackageLayer("Banco", local.toString());
        ProjectLocalSourcePreflight.requireExistingLocalSource(gpkg);

        RasterLayer raster = new RasterLayer("DEM", local.toString());
        ProjectLocalSourcePreflight.requireExistingLocalSource(raster);

        Layer oldSpatiaLite = new Layer("SQLite", "spatialite://" + local
                + "#roads", "SPATIALITE");
        ProjectLocalSourcePreflight.requireExistingLocalSource(oldSpatiaLite);
    }

    @Test
    void deferredServicesAreNotRequiredToBeLocalFiles() throws Exception {
        OnlineWfsLayer wfs = new OnlineWfsLayer("WFS");
        wfs.setServiceUrl("https://example.invalid/wfs");
        OnlineWmsLayer wms = new OnlineWmsLayer("WMS");
        wms.setServiceUrl("https://example.invalid/wms");
        OnlineTileLayer tile = new OnlineTileLayer("Tiles");
        tile.setPath("https://example.invalid/{z}/{x}/{y}.png");
        PostgisLayer postgis = new PostgisLayer("PostGIS");
        postgis.setPath("postgis://example.invalid/municipio");
        ProjectLocalSourcePreflight.requireExistingLocalSources(
                List.of(wfs, wms, tile, postgis));
    }

    @Test
    void symlinkAndEmptyLocalFilesDoNotPass() throws Exception {
        Path empty = directory.resolve("empty.geojson");
        Files.write(empty, new byte[0]);
        assertThrows(IOException.class,
                () -> ProjectLocalSourcePreflight.requireExistingLocalSource(
                        new Layer("Sin contenido", empty.toString(), "VECTOR")));
    }
}
