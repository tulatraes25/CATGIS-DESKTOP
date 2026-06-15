package ar.com.catgis;

import ar.com.catgis.data.online.OnlineRasterSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OnlineMapCatalogTest {

    @Test
    void shouldExposeOsmAndEsriSourcesWithoutStaticInitializationFailure() {
        OnlineRasterSource osm = assertDoesNotThrow(
                () -> OnlineMapCatalog.getById(OnlineMapCatalog.SOURCE_OSM)
        );
        OnlineRasterSource esri = assertDoesNotThrow(
                () -> OnlineMapCatalog.getById(OnlineMapCatalog.SOURCE_ESRI_WORLD_IMAGERY)
        );

        assertNotNull(osm);
        assertNotNull(esri);
        assertEquals("OpenStreetMap", osm.getName());
        assertEquals("Esri World Imagery", esri.getName());
    }
}
