package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.online.OnlineWmsLayer;

import java.util.HashMap;
import java.util.Map;

/**
 * Version-tolerant payload codec for project layer subtypes.
 * New files use keyed suffixes. Legacy positional payloads remain readable.
 */
final class ProjectLayerPayloadCodec {

    private ProjectLayerPayloadCodec() {}

    static void appendKeyedPayload(StringBuilder target, Layer layer) {
        if (target == null || layer == null) {
            return;
        }

        if (layer instanceof OnlineTileLayer tile) {
            append(target, "TILE_SOURCE_ID", tile.getSourceId());
            append(target, "TILE_PROVIDER", tile.getProviderName());
            append(target, "TILE_URL", tile.getUrlTemplate());
            append(target, "TILE_MIN_ZOOM", tile.getMinZoom());
            append(target, "TILE_MAX_ZOOM", tile.getMaxZoom());
            append(target, "TILE_ATTRIBUTION", tile.getAttribution());
            append(target, "TILE_TERMS", tile.getTermsUrl());
            append(target, "TILE_REQUIRES_KEY", tile.isRequiresApiKey());
        } else if (layer instanceof OnlineWmsLayer wms) {
            append(target, "WMS_SOURCE_ID", wms.getSourceId());
            append(target, "WMS_PROVIDER", wms.getProviderName());
            append(target, "WMS_URL", wms.getServiceUrl());
            append(target, "WMS_LAYERS", wms.getLayerNames());
            append(target, "WMS_STYLES", wms.getStyleNames());
            append(target, "WMS_REQUEST_CRS", wms.getRequestCrs());
            append(target, "WMS_VERSION", wms.getVersion());
            append(target, "WMS_FORMAT", wms.getImageFormat());
            append(target, "WMS_TRANSPARENT", wms.isTransparent());
            append(target, "WMS_ATTRIBUTION", wms.getAttribution());
            append(target, "WMS_TERMS", wms.getTermsUrl());
            append(target, "WMS_EXTENT_CRS", wms.getExtentCrs());
            append(target, "WMS_MIN_X", wms.getExtentMinX());
            append(target, "WMS_MIN_Y", wms.getExtentMinY());
            append(target, "WMS_MAX_X", wms.getExtentMaxX());
            append(target, "WMS_MAX_Y", wms.getExtentMaxY());
        } else if (layer instanceof OnlineWfsLayer wfs) {
            append(target, "WFS_PROVIDER", wfs.getProviderName());
            append(target, "WFS_URL", wfs.getServiceUrl());
            append(target, "WFS_TYPE_NAME", wfs.getTypeName());
            append(target, "WFS_TYPE_TITLE", wfs.getTypeTitle());
            append(target, "WFS_REQUEST_CRS", wfs.getRequestCrs());
            append(target, "WFS_VERSION", wfs.getVersion());
            append(target, "WFS_READ_ONLY", wfs.isReadOnly());
        } else if (layer instanceof GeoPackageLayer geoPackage) {
            append(target, "GPKG_TABLE", geoPackage.getTableName());
            append(target, "GPKG_IDENTIFIER", geoPackage.getIdentifier());
            append(target, "GPKG_DESCRIPTION", geoPackage.getDescription());
            append(target, "GPKG_GEOMETRY", geoPackage.getGeometryTypeLabel());
            append(target, "GPKG_READ_ONLY", geoPackage.isReadOnly());
        } else if (layer instanceof PostgisLayer postgis) {
            append(target, "POSTGIS_HOST", postgis.getHost());
            append(target, "POSTGIS_PORT", postgis.getPort());
            append(target, "POSTGIS_DATABASE", postgis.getDatabaseName());
            append(target, "POSTGIS_SCHEMA", postgis.getSchemaName());
            append(target, "POSTGIS_USER", postgis.getUserName());
            append(target, "POSTGIS_TYPE_NAME", postgis.getTypeName());
            append(target, "POSTGIS_TABLE", postgis.getTableName());
            append(target, "POSTGIS_GEOMETRY", postgis.getGeometryTypeLabel());
            append(target, "POSTGIS_READ_ONLY", postgis.isReadOnly());
        } else if (layer instanceof SpatiaLiteLayer spatialite) {
            append(target, "SPATIALITE_TABLE", spatialite.getTableName());
            append(target, "SPATIALITE_CRS", spatialite.getResolvedCrs());
            append(target, "SPATIALITE_GEOMETRY", spatialite.getGeometryTypeLabel());
        }
    }

    static void decodePayload(Layer layer, String[] parts, int payloadStart) {
        if (layer == null || parts == null) {
            return;
        }
        Map<String, String> keyed = collectKeyed(parts);

        if (layer instanceof OnlineTileLayer tile) {
            if (hasPrefix(keyed, "TILE_")) {
                tile.setSourceId(value(keyed, "TILE_SOURCE_ID"));
                tile.setProviderName(value(keyed, "TILE_PROVIDER"));
                tile.setUrlTemplate(value(keyed, "TILE_URL"));
                tile.setMinZoom(parseInt(keyed, "TILE_MIN_ZOOM", tile.getMinZoom()));
                tile.setMaxZoom(parseInt(keyed, "TILE_MAX_ZOOM", tile.getMaxZoom()));
                tile.setAttribution(value(keyed, "TILE_ATTRIBUTION"));
                tile.setTermsUrl(value(keyed, "TILE_TERMS"));
                tile.setRequiresApiKey(parseBoolean(keyed, "TILE_REQUIRES_KEY", tile.isRequiresApiKey()));
            } else {
                decodeLegacyTile(tile, parts, rasterLegacyStart(payloadStart));
            }
        } else if (layer instanceof OnlineWmsLayer wms) {
            if (hasPrefix(keyed, "WMS_")) {
                wms.setSourceId(value(keyed, "WMS_SOURCE_ID"));
                wms.setProviderName(value(keyed, "WMS_PROVIDER"));
                wms.setServiceUrl(value(keyed, "WMS_URL"));
                wms.setLayerNames(value(keyed, "WMS_LAYERS"));
                wms.setStyleNames(value(keyed, "WMS_STYLES"));
                wms.setRequestCrs(value(keyed, "WMS_REQUEST_CRS"));
                wms.setVersion(value(keyed, "WMS_VERSION"));
                wms.setImageFormat(value(keyed, "WMS_FORMAT"));
                wms.setTransparent(parseBoolean(keyed, "WMS_TRANSPARENT", wms.isTransparent()));
                wms.setAttribution(value(keyed, "WMS_ATTRIBUTION"));
                wms.setTermsUrl(value(keyed, "WMS_TERMS"));
                applyWmsExtent(wms,
                        value(keyed, "WMS_EXTENT_CRS"),
                        parseDouble(keyed, "WMS_MIN_X"),
                        parseDouble(keyed, "WMS_MIN_Y"),
                        parseDouble(keyed, "WMS_MAX_X"),
                        parseDouble(keyed, "WMS_MAX_Y"));
            } else {
                decodeLegacyWms(wms, parts, rasterLegacyStart(payloadStart));
            }
        } else if (layer instanceof OnlineWfsLayer wfs) {
            if (hasPrefix(keyed, "WFS_")) {
                wfs.setProviderName(value(keyed, "WFS_PROVIDER"));
                wfs.setServiceUrl(value(keyed, "WFS_URL"));
                wfs.setTypeName(value(keyed, "WFS_TYPE_NAME"));
                wfs.setTypeTitle(value(keyed, "WFS_TYPE_TITLE"));
                wfs.setRequestCrs(value(keyed, "WFS_REQUEST_CRS"));
                wfs.setVersion(value(keyed, "WFS_VERSION"));
                wfs.setReadOnly(parseBoolean(keyed, "WFS_READ_ONLY", wfs.isReadOnly()));
            } else {
                decodeLegacyWfs(wfs, parts, nonRasterLegacyStart(payloadStart));
            }
        } else if (layer instanceof GeoPackageLayer gpkg) {
            if (hasPrefix(keyed, "GPKG_")) {
                gpkg.setTableName(value(keyed, "GPKG_TABLE"));
                gpkg.setIdentifier(value(keyed, "GPKG_IDENTIFIER"));
                gpkg.setDescription(value(keyed, "GPKG_DESCRIPTION"));
                gpkg.setGeometryTypeLabel(value(keyed, "GPKG_GEOMETRY"));
                gpkg.setReadOnly(parseBoolean(keyed, "GPKG_READ_ONLY", gpkg.isReadOnly()));
            } else {
                decodeLegacyGeoPackage(gpkg, parts, nonRasterLegacyStart(payloadStart));
            }
        } else if (layer instanceof PostgisLayer postgis) {
            if (hasPrefix(keyed, "POSTGIS_")) {
                postgis.setHost(value(keyed, "POSTGIS_HOST"));
                postgis.setPort(parseInt(keyed, "POSTGIS_PORT", postgis.getPort()));
                postgis.setDatabaseName(value(keyed, "POSTGIS_DATABASE"));
                postgis.setSchemaName(value(keyed, "POSTGIS_SCHEMA"));
                postgis.setUserName(value(keyed, "POSTGIS_USER"));
                postgis.setTypeName(value(keyed, "POSTGIS_TYPE_NAME"));
                postgis.setTableName(value(keyed, "POSTGIS_TABLE"));
                postgis.setGeometryTypeLabel(value(keyed, "POSTGIS_GEOMETRY"));
                postgis.setReadOnly(parseBoolean(keyed, "POSTGIS_READ_ONLY", postgis.isReadOnly()));
            } else {
                decodeLegacyPostgis(postgis, parts, nonRasterLegacyStart(payloadStart));
            }
        } else if (layer instanceof SpatiaLiteLayer spatialite) {
            if (hasPrefix(keyed, "SPATIALITE_")) {
                spatialite.setTableName(value(keyed, "SPATIALITE_TABLE"));
                spatialite.setResolvedCrs(value(keyed, "SPATIALITE_CRS"));
                spatialite.setGeometryTypeLabel(value(keyed, "SPATIALITE_GEOMETRY"));
            }
        }
    }

    private static int nonRasterLegacyStart(int payloadStart) {
        return payloadStart + 1;
    }

    private static int rasterLegacyStart(int payloadStart) {
        // RasterLayer serialization is:
        // opacity, grayscale, autoContrast, red, green, blue, rasterMode.
        return payloadStart + 7;
    }

    private static void decodeLegacyTile(OnlineTileLayer tile, String[] parts, int start) {
        tile.setSourceId(part(parts, start));
        tile.setProviderName(part(parts, start + 1));
        tile.setUrlTemplate(part(parts, start + 2));
        tile.setMinZoom(parseInt(part(parts, start + 3), tile.getMinZoom()));
        tile.setMaxZoom(parseInt(part(parts, start + 4), tile.getMaxZoom()));
        tile.setAttribution(part(parts, start + 5));
        tile.setTermsUrl(part(parts, start + 6));
        tile.setRequiresApiKey(parseBoolean(part(parts, start + 7), tile.isRequiresApiKey()));
    }

    private static void decodeLegacyWms(OnlineWmsLayer wms, String[] parts, int start) {
        wms.setSourceId(part(parts, start));
        wms.setProviderName(part(parts, start + 1));
        wms.setServiceUrl(part(parts, start + 2));
        wms.setLayerNames(part(parts, start + 3));
        wms.setStyleNames(part(parts, start + 4));
        wms.setRequestCrs(part(parts, start + 5));
        wms.setVersion(part(parts, start + 6));
        wms.setImageFormat(part(parts, start + 7));
        wms.setTransparent(parseBoolean(part(parts, start + 8), wms.isTransparent()));
        wms.setAttribution(part(parts, start + 9));
        wms.setTermsUrl(part(parts, start + 10));
        applyWmsExtent(wms, part(parts, start + 11),
                parseDouble(part(parts, start + 12)),
                parseDouble(part(parts, start + 13)),
                parseDouble(part(parts, start + 14)),
                parseDouble(part(parts, start + 15)));
    }

    private static void decodeLegacyWfs(OnlineWfsLayer wfs, String[] parts, int start) {
        wfs.setProviderName(part(parts, start));
        wfs.setServiceUrl(part(parts, start + 1));
        wfs.setTypeName(part(parts, start + 2));
        wfs.setTypeTitle(part(parts, start + 3));
        wfs.setRequestCrs(part(parts, start + 4));
        wfs.setVersion(part(parts, start + 5));
        wfs.setReadOnly(parseBoolean(part(parts, start + 6), wfs.isReadOnly()));
    }

    private static void decodeLegacyGeoPackage(GeoPackageLayer gpkg, String[] parts, int start) {
        gpkg.setTableName(part(parts, start));
        gpkg.setIdentifier(part(parts, start + 1));
        gpkg.setDescription(part(parts, start + 2));
        gpkg.setGeometryTypeLabel(part(parts, start + 3));
        gpkg.setReadOnly(parseBoolean(part(parts, start + 4), gpkg.isReadOnly()));
    }

    private static void decodeLegacyPostgis(PostgisLayer postgis, String[] parts, int start) {
        postgis.setHost(part(parts, start));
        postgis.setPort(parseInt(part(parts, start + 1), postgis.getPort()));
        postgis.setDatabaseName(part(parts, start + 2));
        postgis.setSchemaName(part(parts, start + 3));
        postgis.setUserName(part(parts, start + 4));
        postgis.setTypeName(part(parts, start + 5));
        postgis.setTableName(part(parts, start + 6));
        postgis.setGeometryTypeLabel(part(parts, start + 7));
        postgis.setReadOnly(parseBoolean(part(parts, start + 8), postgis.isReadOnly()));
    }

    private static void applyWmsExtent(OnlineWmsLayer wms, String crs,
                                       Double minX, Double minY, Double maxX, Double maxY) {
        if (minX != null && minY != null && maxX != null && maxY != null) {
            wms.setExtent(minX, minY, maxX, maxY, crs);
        }
    }

    private static Map<String, String> collectKeyed(String[] parts) {
        Map<String, String> values = new HashMap<>();
        for (String part : parts) {
            if (part == null) {
                continue;
            }
            int separator = part.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String key = part.substring(0, separator).trim();
            String value = part.substring(separator + 1);
            values.putIfAbsent(key, value);
        }
        return values;
    }

    private static boolean hasPrefix(Map<String, String> values, String prefix) {
        return values.keySet().stream().anyMatch(key -> key.startsWith(prefix));
    }

    private static String value(Map<String, String> values, String key) {
        return values.getOrDefault(key, "");
    }

    private static String part(String[] parts, int index) {
        return index >= 0 && index < parts.length ? parts[index].trim() : "";
    }

    private static int parseInt(Map<String, String> values, String key, int fallback) {
        return parseInt(value(values, key), fallback);
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static boolean parseBoolean(Map<String, String> values,
                                        String key, boolean fallback) {
        return parseBoolean(value(values, key), fallback);
    }

    private static boolean parseBoolean(String value, boolean fallback) {
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        return fallback;
    }

    private static Double parseDouble(Map<String, String> values, String key) {
        return parseDouble(value(values, key));
    }

    private static Double parseDouble(String value) {
        try {
            double parsed = Double.parseDouble(value.trim().replace(",", "."));
            return Double.isFinite(parsed) ? parsed : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void append(StringBuilder target, String key, Object rawValue) {
        String value = rawValue == null ? "" : String.valueOf(rawValue);
        target.append('|').append(key).append('=').append(value.replace("|", " "));
    }
}
