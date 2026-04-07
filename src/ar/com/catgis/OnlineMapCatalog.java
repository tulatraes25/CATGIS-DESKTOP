package ar.com.catgis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class OnlineMapCatalog {

    public static final String SOURCE_OSM = "osm-standard";
    public static final String SOURCE_ESRI_WORLD_IMAGERY = "esri-world-imagery";

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
                "© OpenStreetMap contributors",
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
