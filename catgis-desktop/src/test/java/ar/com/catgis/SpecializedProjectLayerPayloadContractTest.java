package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecializedProjectLayerPayloadContractTest {

    @Test
    void geopackageRoundTripPreservesOpacityAndSubtypeFields() {
        GeoPackageLayer layer = new GeoPackageLayer("Catastro", "/data/catastro.gpkg");
        layer.setOpacity(0.42f);
        layer.setTableName("parcelas");
        layer.setIdentifier("Parcelas urbanas");
        layer.setDescription("catastro|municipal");
        layer.setGeometryTypeLabel("MultiPolygon");
        layer.setReadOnly(false);

        Layer parsed = LoadProjectAction.parseLayer(SaveProjectAction.buildLayerLine(layer));
        GeoPackageLayer restored = assertInstanceOf(GeoPackageLayer.class, parsed);

        assertEquals(0.42f, restored.getOpacity(), 1e-6);
        assertEquals("parcelas", restored.getTableName());
        assertEquals("Parcelas urbanas", restored.getIdentifier());
        assertEquals("catastro municipal", restored.getDescription());
        assertEquals("MultiPolygon", restored.getGeometryTypeLabel());
        assertFalse(restored.isReadOnly());
    }

    @Test
    void postgisRoundTripPreservesConnectionIdentityWithoutPassword() {
        PostgisLayer layer = new PostgisLayer("Red");
        layer.setOpacity(0.61f);
        layer.setHost("db.internal");
        layer.setPort(5544);
        layer.setDatabaseName("gis");
        layer.setSchemaName("infra");
        layer.setUserName("reader");
        layer.setTypeName("infra.red");
        layer.setTableName("red");
        layer.setGeometryTypeLabel("LineString");
        layer.setReadOnly(true);

        PostgisLayer restored = assertInstanceOf(PostgisLayer.class,
                LoadProjectAction.parseLayer(SaveProjectAction.buildLayerLine(layer)));

        assertEquals(0.61f, restored.getOpacity(), 1e-6);
        assertEquals("db.internal", restored.getHost());
        assertEquals(5544, restored.getPort());
        assertEquals("gis", restored.getDatabaseName());
        assertEquals("infra", restored.getSchemaName());
        assertEquals("reader", restored.getUserName());
        assertEquals("infra.red", restored.getTypeName());
        assertEquals("red", restored.getTableName());
        assertEquals("LineString", restored.getGeometryTypeLabel());
        assertTrue(restored.isReadOnly());
    }

    @Test
    void wfsRoundTripPreservesServiceAndType() {
        OnlineWfsLayer layer = new OnlineWfsLayer("Parcelas WFS");
        layer.setOpacity(0.75f);
        layer.setProviderName("Municipio");
        layer.setServiceUrl("https://example.invalid/wfs?token=a=b");
        layer.setTypeName("cat:parcelas");
        layer.setTypeTitle("Parcelas");
        layer.setRequestCrs("EPSG:22182");
        layer.setVersion("2.0.0");
        layer.setReadOnly(true);

        OnlineWfsLayer restored = assertInstanceOf(OnlineWfsLayer.class,
                LoadProjectAction.parseLayer(SaveProjectAction.buildLayerLine(layer)));

        assertEquals(0.75f, restored.getOpacity(), 1e-6);
        assertEquals("Municipio", restored.getProviderName());
        assertEquals("https://example.invalid/wfs?token=a=b", restored.getServiceUrl());
        assertEquals("cat:parcelas", restored.getTypeName());
        assertEquals("Parcelas", restored.getTypeTitle());
        assertEquals("EPSG:22182", restored.getRequestCrs());
        assertEquals("2.0.0", restored.getVersion());
        assertTrue(restored.isReadOnly());
    }

    @Test
    void tileAndWmsRoundTripUseKeyedPayloadNotPositionalOffsets() {
        OnlineTileLayer tile = new OnlineTileLayer("Base");
        tile.setOpacity(0.33f);
        tile.setSourceId("osm");
        tile.setProviderName("Proveedor");
        tile.setUrlTemplate("https://tiles.invalid/{z}/{x}/{y}.png?key=a=b");
        tile.setMinZoom(2);
        tile.setMaxZoom(18);
        tile.setAttribution("Atribucion");
        tile.setTermsUrl("https://terms.invalid/?a=b");
        tile.setRequiresApiKey(true);

        OnlineTileLayer restoredTile = assertInstanceOf(OnlineTileLayer.class,
                LoadProjectAction.parseLayer(SaveProjectAction.buildLayerLine(tile)));
        assertEquals(0.33f, restoredTile.getOpacity(), 1e-6);
        assertEquals("osm", restoredTile.getSourceId());
        assertEquals("Proveedor", restoredTile.getProviderName());
        assertEquals(tile.getUrlTemplate(), restoredTile.getUrlTemplate());
        assertEquals(2, restoredTile.getMinZoom());
        assertEquals(18, restoredTile.getMaxZoom());
        assertTrue(restoredTile.isRequiresApiKey());

        OnlineWmsLayer wms = new OnlineWmsLayer("WMS");
        wms.setOpacity(0.58f);
        wms.setSourceId("source");
        wms.setProviderName("Provider");
        wms.setServiceUrl("https://wms.invalid/service?x=a=b");
        wms.setLayerNames("roads,parcels");
        wms.setStyleNames("default");
        wms.setRequestCrs("EPSG:3857");
        wms.setVersion("1.3.0");
        wms.setImageFormat("image/png");
        wms.setTransparent(true);
        wms.setAttribution("attr");
        wms.setTermsUrl("https://terms.invalid");
        wms.setExtent(-10, -20, 30, 40, "EPSG:4326");

        OnlineWmsLayer restoredWms = assertInstanceOf(OnlineWmsLayer.class,
                LoadProjectAction.parseLayer(SaveProjectAction.buildLayerLine(wms)));
        assertEquals(0.58f, restoredWms.getOpacity(), 1e-6);
        assertEquals("source", restoredWms.getSourceId());
        assertEquals("Provider", restoredWms.getProviderName());
        assertEquals(wms.getServiceUrl(), restoredWms.getServiceUrl());
        assertEquals("roads,parcels", restoredWms.getLayerNames());
        assertEquals("EPSG:3857", restoredWms.getRequestCrs());
        assertEquals("EPSG:4326", restoredWms.getExtentCrs());
        assertEquals(-10.0, restoredWms.getExtentMinX(), 0.0);
        assertEquals(40.0, restoredWms.getExtentMaxY(), 0.0);
    }

    @Test
    void spatialiteKeepsRealDatabasePathAndTableSeparate() {
        SpatiaLiteLayer layer = new SpatiaLiteLayer("Calles", "/data/city.sqlite");
        layer.setOpacity(0.87f);
        layer.setTableName("roads");
        layer.setResolvedCrs("EPSG:22182");
        layer.setGeometryTypeLabel("LineString");

        SpatiaLiteLayer restored = assertInstanceOf(SpatiaLiteLayer.class,
                LoadProjectAction.parseLayer(SaveProjectAction.buildLayerLine(layer)));

        assertEquals("/data/city.sqlite", restored.getPath());
        assertEquals("roads", restored.getTableName());
        assertEquals("EPSG:22182", restored.getResolvedCrs());
        assertEquals("LineString", restored.getGeometryTypeLabel());
        assertEquals(0.87f, restored.getOpacity(), 1e-6);
    }

    @Test
    void spatialiteLegacyPseudoUriIsMigratedWithoutGuessingAnotherTable() {
        SpatiaLiteLayer migrated =
                new SpatiaLiteLayer("Legacy", "spatialite:///data/legacy.sqlite#parcels");

        assertEquals("/data/legacy.sqlite", migrated.getPath());
        assertEquals("parcels", migrated.getTableName());
    }

    @Test
    void legacyPositionalTileWmsAndWfsPayloadsStartAfterOpacity() {
        OnlineTileLayer tile = new OnlineTileLayer("Legacy tile");
        tile.setOpacity(0.31f);
        String tileLegacy = replaceKeyedWithLegacy(
                SaveProjectAction.buildLayerLine(tile),
                "TILE_",
                List.of("osm", "Provider", "https://tiles.invalid/{z}/{x}/{y}.png",
                        "1", "19", "attr", "https://terms.invalid", "true"));
        OnlineTileLayer restoredTile = assertInstanceOf(OnlineTileLayer.class,
                LoadProjectAction.parseLayer(tileLegacy));
        assertEquals(0.31f, restoredTile.getOpacity(), 1e-6);
        assertEquals("osm", restoredTile.getSourceId());
        assertEquals(19, restoredTile.getMaxZoom());
        assertTrue(restoredTile.isRequiresApiKey());

        OnlineWfsLayer wfs = new OnlineWfsLayer("Legacy WFS");
        wfs.setOpacity(0.47f);
        String wfsLegacy = replaceKeyedWithLegacy(
                SaveProjectAction.buildLayerLine(wfs),
                "WFS_",
                List.of("Provider", "https://wfs.invalid", "cat:roads", "Roads",
                        "EPSG:4326", "2.0.0", "true"));
        OnlineWfsLayer restoredWfs = assertInstanceOf(OnlineWfsLayer.class,
                LoadProjectAction.parseLayer(wfsLegacy));
        assertEquals(0.47f, restoredWfs.getOpacity(), 1e-6);
        assertEquals("Provider", restoredWfs.getProviderName());
        assertEquals("cat:roads", restoredWfs.getTypeName());

        OnlineWmsLayer wms = new OnlineWmsLayer("Legacy WMS");
        wms.setOpacity(0.63f);
        String wmsLegacy = replaceKeyedWithLegacy(
                SaveProjectAction.buildLayerLine(wms),
                "WMS_",
                List.of("src", "Provider", "https://wms.invalid", "roads", "style",
                        "EPSG:3857", "1.3.0", "image/png", "true", "attr",
                        "https://terms.invalid", "EPSG:4326",
                        "-10", "-20", "30", "40"));
        OnlineWmsLayer restoredWms = assertInstanceOf(OnlineWmsLayer.class,
                LoadProjectAction.parseLayer(wmsLegacy));
        assertEquals(0.63f, restoredWms.getOpacity(), 1e-6);
        assertEquals("src", restoredWms.getSourceId());
        assertEquals("roads", restoredWms.getLayerNames());
        assertEquals(-10.0, restoredWms.getExtentMinX(), 0.0);
        assertEquals(40.0, restoredWms.getExtentMaxY(), 0.0);
    }

    @Test
    void legacyPositionalGeopackagePayloadStartsAfterOpacity() {
        GeoPackageLayer layer = new GeoPackageLayer("Legacy GPKG", "/data/legacy.gpkg");
        layer.setOpacity(0.44f);
        layer.setTableName("roads");
        layer.setIdentifier("Roads");
        layer.setDescription("legacy");
        layer.setGeometryTypeLabel("LineString");
        layer.setReadOnly(true);

        String keyed = SaveProjectAction.buildLayerLine(layer);
        String legacy = replaceKeyedWithLegacy(keyed, "GPKG_",
                List.of("roads", "Roads", "legacy", "LineString", "true"));

        GeoPackageLayer restored = assertInstanceOf(GeoPackageLayer.class,
                LoadProjectAction.parseLayer(legacy));
        assertEquals(0.44f, restored.getOpacity(), 1e-6);
        assertEquals("roads", restored.getTableName());
        assertEquals("Roads", restored.getIdentifier());
        assertEquals("legacy", restored.getDescription());
        assertEquals("LineString", restored.getGeometryTypeLabel());
        assertTrue(restored.isReadOnly());
    }

    @Test
    void legacyPositionalPostgisPayloadStartsAfterOpacity() {
        PostgisLayer layer = new PostgisLayer("Legacy PG");
        layer.setOpacity(0.52f);
        String keyed = SaveProjectAction.buildLayerLine(layer);
        String legacy = replaceKeyedWithLegacy(keyed, "POSTGIS_",
                List.of("db.host", "5433", "gis", "public", "reader",
                        "public.roads", "roads", "LineString", "true"));

        PostgisLayer restored = assertInstanceOf(PostgisLayer.class,
                LoadProjectAction.parseLayer(legacy));
        assertEquals(0.52f, restored.getOpacity(), 1e-6);
        assertEquals("db.host", restored.getHost());
        assertEquals(5433, restored.getPort());
        assertEquals("gis", restored.getDatabaseName());
        assertEquals("roads", restored.getTableName());
    }

    private static String replaceKeyedWithLegacy(String encoded,
                                                  String prefix,
                                                  List<String> legacyValues) {
        List<String> parts = new ArrayList<>(Arrays.asList(encoded.split("\\|", -1)));
        parts.removeIf(part -> part.startsWith(prefix));
        parts.addAll(legacyValues);
        return String.join("|", parts);
    }
}
