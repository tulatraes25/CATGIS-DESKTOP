package ar.com.catgis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class OnlineMapCatalog {

    public static final String SOURCE_OSM = "osm-standard";
    public static final String SOURCE_ESRI_WORLD_IMAGERY = "esri-world-imagery";
    public static final String SOURCE_ESRI_WORLD_TOPO = "esri-world-topo";
    public static final String SOURCE_ESRI_WORLD_STREET = "esri-world-street";
    public static final String SOURCE_ESRI_LIGHT_GRAY = "esri-light-gray";
    public static final String SOURCE_ESRI_NATGEO = "esri-natgeo";

    private static final Map<String, OnlineRasterSource> SOURCES = new LinkedHashMap<>();

    static {
        register(new OnlineRasterSource(
                SOURCE_OSM,
                "OpenStreetMap",
                "OpenStreetMap",
                OnlineServiceType.XYZ,
                "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
                0,
                19,
                256,
                "\u00A9 OpenStreetMap contributors",
                "https://operations.osmfoundation.org/policies/tiles/",
                false,
                "EPSG:3857"
        ));

        register(new OnlineRasterSource(
                SOURCE_ESRI_WORLD_IMAGERY,
                "Esri World Imagery",
                "Esri",
                OnlineServiceType.XYZ,
                "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}",
                0,
                19,
                256,
                "Source: Esri, Maxar, Earthstar Geographics, and the GIS User Community",
                "https://developers.arcgis.com/documentation/mapping-and-location-services/mapping/basemaps/",
                false,
                "EPSG:3857"
        ));

        register(new OnlineRasterSource(
                SOURCE_ESRI_WORLD_TOPO,
                "Esri World Topo",
                "Esri",
                OnlineServiceType.XYZ,
                "https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}",
                0,
                19,
                256,
                "Source: Esri and contributors",
                "https://developers.arcgis.com/documentation/mapping-and-location-services/mapping/basemaps/",
                false,
                "EPSG:3857"
        ));

        register(new OnlineRasterSource(
                SOURCE_ESRI_WORLD_STREET,
                "Esri World Street Map",
                "Esri",
                OnlineServiceType.XYZ,
                "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}",
                0,
                19,
                256,
                "Source: Esri, HERE, Garmin, FAO, NOAA, USGS and contributors",
                "https://developers.arcgis.com/documentation/mapping-and-location-services/mapping/basemaps/",
                false,
                "EPSG:3857"
        ));

        register(new OnlineRasterSource(
                SOURCE_ESRI_LIGHT_GRAY,
                "Esri Light Gray Canvas",
                "Esri",
                OnlineServiceType.XYZ,
                "https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}",
                0,
                16,
                256,
                "Source: Esri, DeLorme, NAVTEQ and contributors",
                "https://developers.arcgis.com/documentation/mapping-and-location-services/mapping/basemaps/",
                false,
                "EPSG:3857"
        ));

        register(new OnlineRasterSource(
                SOURCE_ESRI_NATGEO,
                "Esri NatGeo World Map",
                "Esri",
                OnlineServiceType.XYZ,
                "https://server.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/{z}/{y}/{x}",
                0,
                16,
                256,
                "Source: National Geographic, Esri, Garmin, HERE and contributors",
                "https://developers.arcgis.com/documentation/mapping-and-location-services/mapping/basemaps/",
                false,
                "EPSG:3857"
        ));
    }

    private OnlineMapCatalog() {
    }

    private static void register(OnlineRasterSource source) {
        if (source != null && source.getId() != null && !source.getId().isBlank()) {
            SOURCES.put(source.getId(), source);
        }
    }

    public static List<OnlineRasterSource> getBaseMaps() {
        return Collections.unmodifiableList(new ArrayList<>(SOURCES.values()));
    }

    public static OnlineRasterSource getById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return SOURCES.get(id);
    }
}
