package ar.com.catgis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                "https://developers.arcgisonline.com/documentation/mapping-and-location-services/mapping/basemaps/",
                false,
                "EPSG:3857"
        ));

        // Climate WMS/WMTS/XYZ sources
        registerClimateSources();
    }

    // =========================================================================
    // CLIMATE WMS/WMTS/XYZ sources — VISUALIZATION ONLY
    // These are tile overlays for display in the map viewer, NOT downloadable
    // analysis data. Use Climate Online Download (WorldClim, Open-Meteo) for
    // actual data that can be analyzed (zonal stats, etc.).
    // =========================================================================

    /** Climate-specific source IDs */
    public static final String SOURCE_NOAA_GFS_TEMP = "noaa-gfs-temperature";
    public static final String SOURCE_COPERNICUS_GLO_30 = "copernicus-global-30m";
    public static final String SOURCE_NASA_GIBS_VIIRS = "nasa-gibs-viirs";
    public static final String SOURCE_NASA_GIBS_MODIS_AQUA = "nasa-gibs-modis-aqua";
    public static final String SOURCE_OPENWEATHER = "openweather-temp";
    public static final String SOURCE_WINDY_LIVE = "windy-live";

    private static final Set<String> CLIMATE_SOURCE_IDS = new LinkedHashSet<>();

    private OnlineMapCatalog() {
    }

    /**
     * Register climate-specific data sources.
     * Called during initialization to add climate/environmental online sources.
     */
    public static void registerClimateSources() {
        register(new OnlineRasterSource(
                SOURCE_NOAA_GFS_TEMP,
                "NOAA GFS - Global Temperature",
                "NOAA",
                OnlineServiceType.WMS,
                "https://nomads.ncep.noaa.gov/wms/gfs",
                0, 18, 256,
                "GFS global temperature forecasts — verify endpoint availability",
                "", true,
                "EPSG:4326"
        ));
        CLIMATE_SOURCE_IDS.add(SOURCE_NOAA_GFS_TEMP);

        register(new OnlineRasterSource(
                SOURCE_COPERNICUS_GLO_30,
                "Copernicus GLO-30 DEM",
                "Copernicus",
                OnlineServiceType.WMS,
                "https://wms.dataspace.copernicus.eu",
                0, 18, 256,
                "Global 30m Digital Elevation Model — requires free Copernicus DataSpace account",
                "", true,
                "EPSG:4326"
        ));
        CLIMATE_SOURCE_IDS.add(SOURCE_COPERNICUS_GLO_30);

        register(new OnlineRasterSource(
                SOURCE_NASA_GIBS_VIIRS,
                "NASA GIBS VIIRS - True Color",
                "NASA",
                OnlineServiceType.XYZ,
                "https://gibs.earthdata.nasa.gov/wmts/epsg3857/best/VIIRS_SNPP_CorrectedReflectance_TrueColor/default/{Time}/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.jpg",
                0, 18, 256,
                "NASA GIBS VIIRS True Color — free satellite imagery, no auth required",
                "", true,
                "EPSG:3857"
        ));
        CLIMATE_SOURCE_IDS.add(SOURCE_NASA_GIBS_VIIRS);

        register(new OnlineRasterSource(
                SOURCE_NASA_GIBS_MODIS_AQUA,
                "NASA GIBS MODIS Aqua - Sea Surface Temperature",
                "NASA",
                OnlineServiceType.XYZ,
                "https://gibs.earthdata.nasa.gov/wmts/epsg3857/best/MODIS_Aqua_L3_SST_Monthly_4km_Night_SST/datestr/{Time}/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png",
                0, 18, 256,
                "NASA GIBS MODIS SST — monthly sea surface temperature, 4km resolution",
                "", true,
                "EPSG:3857"
        ));
        CLIMATE_SOURCE_IDS.add(SOURCE_NASA_GIBS_MODIS_AQUA);

        register(new OnlineRasterSource(
                SOURCE_OPENWEATHER,
                "OpenWeatherMap - Temperature",
                "OpenWeather",
                OnlineServiceType.XYZ,
                "https://tile.openweathermap.org/map/temp_new/{z}/{x}/{y}.png?appid=TU_API_KEY",
                0, 18, 256,
                "OpenWeatherMap temperature overlay — requiere API key gratuita de openweathermap.org",
                "", true,
                "EPSG:3857"
        ));
        CLIMATE_SOURCE_IDS.add(SOURCE_OPENWEATHER);

        register(new OnlineRasterSource(
                SOURCE_WINDY_LIVE,
                "Windy - Wind",
                "Windy",
                OnlineServiceType.XYZ,
                "https://tiles.windy.com/tiles/v9.25/wind/{z}/{x}/{y}.png",
                0, 18, 256,
                "Windy.com wind overlay — live global wind data; replace API key if needed",
                "", true,
                "EPSG:3857"
        ));
        CLIMATE_SOURCE_IDS.add(SOURCE_WINDY_LIVE);

        // Additional climate data WMS sources
        register(new OnlineRasterSource(
                "noaa-goes-wms",
                "NOAA GOES Este - Satélite",
                "NOAA",
                OnlineServiceType.WMS,
                "https://nowcoast.noaa.gov/arcgis/services/nowcoast/satellite/goes_conus_vis/MapServer/WMSServer",
                0, 18, 256,
                "NOAA GOES East satellite visible imagery — free, publicly available",
                "", false,
                "EPSG:4326"
        ));
        CLIMATE_SOURCE_IDS.add("noaa-goes-wms");

        register(new OnlineRasterSource(
                "nasa-modis-temperature",
                "NASA MODIS - Temperatura superficial",
                "NASA",
                OnlineServiceType.WMS,
                "https://gibs.earthdata.nasa.gov/wmts/epsg4326/best/MODIS_Terra_Land_Surface_Temp_Daily/default/2016-01-01/250m/{TileMatrix}/{TileRow}/{TileCol}.png",
                0, 8, 256,
                "NASA GIBS MODIS Terra land surface temperature — daily, 250m",
                "", true,
                "EPSG:4326"
        ));
        CLIMATE_SOURCE_IDS.add("nasa-modis-temperature");

        register(new OnlineRasterSource(
                "openweather-rain",
                "OpenWeather - Precipitación (requiere clave)",
                "OpenWeather",
                OnlineServiceType.XYZ,
                "https://tile.openweathermap.org/map/precipitation_new/{z}/{x}/{y}.png?appid=TU_API_KEY",
                0, 18, 256,
                "OpenWeatherMap precipitation overlay — requiere API key gratuita",
                "", true,
                "EPSG:3857"
        ));
        CLIMATE_SOURCE_IDS.add("openweather-rain");
    }

    public static List<String> getClimateSourceIds() {
        return List.copyOf(CLIMATE_SOURCE_IDS);
    }

    public static List<OnlineRasterSource> getClimateSources() {
        List<OnlineRasterSource> sources = new ArrayList<>();
        for (String id : CLIMATE_SOURCE_IDS) {
            OnlineRasterSource source = getById(id);
            if (source != null) sources.add(source);
        }
        return sources;
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
