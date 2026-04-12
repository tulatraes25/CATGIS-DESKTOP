package ar.com.catgis;

import org.geotools.api.parameter.GeneralParameterValue;
import org.geotools.api.parameter.ParameterValueGroup;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.crs.GeographicCRS;
import org.geotools.api.referencing.crs.ProjectedCRS;
import org.geotools.api.referencing.crs.SingleCRS;
import org.geotools.api.referencing.datum.Datum;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class CRSDefinitions {

    private static final String DEFAULT_CRS = "EPSG:4326";
    private static volatile List<CrsCatalogEntry> cachedCatalog;
    private static volatile LinkedHashMap<String, String> cachedCatalogMap;

    private CRSDefinitions() {
    }

    public record CrsCatalogEntry(
            String code,
            String label,
            String description,
            boolean featured,
            String searchText
    ) {
        @Override
        public String toString() {
            return label;
        }
    }

    public record CrsTechnicalDetails(
            String code,
            String label,
            String name,
            String type,
            String datum,
            String unit,
            String areaOfUse,
            String projectionMethod,
            String parameters,
            double west,
            double south,
            double east,
            double north,
            boolean hasBounds,
            boolean geographic,
            boolean manual
    ) {
        public static CrsTechnicalDetails unavailable(String code) {
            String normalized = normalizeCode(code);
            return new CrsTechnicalDetails(
                    normalized,
                    normalized.isBlank() ? DEFAULT_CRS : normalized,
                    "CRS no disponible",
                    "No disponible",
                    "No disponible",
                    "No disponible",
                    "Sin area de uso reportada",
                    "No disponible",
                    "Sin parametros disponibles.",
                    0d,
                    0d,
                    0d,
                    0d,
                    false,
                    false,
                    isManualDefinition(normalized)
            );
        }
    }

    public static LinkedHashMap<String, String> createCRSMap() {
        LinkedHashMap<String, String> map = cachedCatalogMap;
        if (map != null) {
            return new LinkedHashMap<>(map);
        }

        LinkedHashMap<String, String> built = new LinkedHashMap<>();
        for (CrsCatalogEntry entry : getCatalogEntries()) {
            built.put(entry.label(), entry.code());
        }
        cachedCatalogMap = built;
        return new LinkedHashMap<>(built);
    }

    public static List<CrsCatalogEntry> getFeaturedEntries() {
        return List.copyOf(buildFeaturedEntries());
    }

    public static List<CrsCatalogEntry> getCatalogEntries() {
        List<CrsCatalogEntry> catalog = cachedCatalog;
        if (catalog != null) {
            return catalog;
        }

        synchronized (CRSDefinitions.class) {
            if (cachedCatalog != null) {
                return cachedCatalog;
            }

            LinkedHashMap<String, CrsCatalogEntry> merged = new LinkedHashMap<>();
            for (CrsCatalogEntry entry : buildFeaturedEntries()) {
                merged.put(entry.code(), entry);
            }

            try {
                Collection<String> supportedCodes = CRS.getSupportedCodes("EPSG");
                var factory = CRS.getAuthorityFactory(true);
                TreeMap<String, CrsCatalogEntry> authorityEntries = new TreeMap<>();
                for (String rawCode : supportedCodes) {
                    String code = normalizeCode(rawCode);
                    if (code.isBlank() || merged.containsKey(code)) {
                        continue;
                    }
                    try {
                        String description = factory.getDescriptionText(code).toString();
                        if (description == null || description.isBlank()) {
                            continue;
                        }
                        authorityEntries.put(
                                code + "|" + description,
                                new CrsCatalogEntry(
                                        code,
                                        code + " - " + description,
                                        description,
                                        false,
                                        buildSearchText(code, description)
                                )
                        );
                    } catch (Exception ignored) {
                    }
                }
                for (CrsCatalogEntry entry : authorityEntries.values()) {
                    merged.put(entry.code(), entry);
                }
            } catch (Exception ignored) {
            }

            cachedCatalog = List.copyOf(merged.values());
            cachedCatalogMap = createMapFromEntries(cachedCatalog);
            return cachedCatalog;
        }
    }

    public static List<CrsCatalogEntry> filterEntries(String searchText) {
        return filterEntries(searchText, getCatalogEntries());
    }

    public static List<CrsCatalogEntry> filterEntries(String searchText, List<CrsCatalogEntry> source) {
        List<CrsCatalogEntry> entries = source != null ? source : Collections.emptyList();
        if (searchText == null || searchText.isBlank()) {
            return entries;
        }

        String query = searchText.trim().toLowerCase(Locale.ROOT);
        List<CrsCatalogEntry> filtered = new ArrayList<>();
        for (CrsCatalogEntry entry : entries) {
            if (entry != null && entry.searchText().contains(query)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public static String getLabelForCode(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }

        String normalized = normalizeCode(code);
        if (isManualDefinition(normalized)) {
            return "WKT manual";
        }

        for (CrsCatalogEntry entry : getCatalogEntries()) {
            if (normalized.equalsIgnoreCase(entry.code())) {
                return entry.label();
            }
        }

        return normalized;
    }

    public static String normalizeCode(String text) {
        if (text == null) {
            return "";
        }

        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        if (trimmed.regionMatches(true, 0, "WKT:", 0, 4)) {
            return buildManualDefinitionValue(trimmed.substring(4).trim());
        }

        if (looksLikeWkt(trimmed)) {
            return buildManualDefinitionValue(trimmed);
        }

        String value = trimmed.toUpperCase(Locale.ROOT);
        if (value.startsWith("EPSG:") || value.startsWith("ESRI:") || value.startsWith("OGC:") || value.startsWith("CRS:")) {
            return value;
        }

        if (value.matches("\\d+")) {
            return "EPSG:" + value;
        }

        String aliasCode = normalizeAliasToCode(value);
        if (!aliasCode.isBlank()) {
            return aliasCode;
        }

        return value;
    }

    private static String normalizeAliasToCode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String alias = value
                .replace('_', ' ')
                .replaceAll("\\s+", " ")
                .trim();

        if (alias.equals("WGS 84") || alias.contains("LAT/LONG") || alias.contains("LONG/LAT")) {
            return "EPSG:4326";
        }
        if (alias.contains("PSEUDO-MERCATOR") || alias.contains("PSEUDO MERCATOR") || alias.equals("WEB MERCATOR")) {
            return "EPSG:3857";
        }
        if (alias.contains("WORLD MERCATOR")) {
            return "EPSG:3395";
        }
        if (alias.contains("WORLD EQUIDISTANT CYLINDRICAL")) {
            return "EPSG:4087";
        }
        if (alias.startsWith("POSGAR 2007 / ARGENTINA ")) {
            Integer zone = extractTrailingZone(alias);
            if (zone != null && zone >= 1 && zone <= 7) {
                return "EPSG:" + (5342 + zone);
            }
        }
        if (alias.startsWith("POSGAR 94 / ARGENTINA ")) {
            Integer zone = extractTrailingZone(alias);
            if (zone != null && zone >= 1 && zone <= 7) {
                return "EPSG:" + (22180 + zone);
            }
        }
        if (alias.startsWith("CAMPO INCHAUSPE / ARGENTINA ")) {
            Integer zone = extractTrailingZone(alias);
            if (zone != null && zone >= 1 && zone <= 7) {
                return "EPSG:" + (22190 + zone);
            }
        }
        if (alias.startsWith("POSGAR 2007")) {
            return "EPSG:4674";
        }
        if (alias.startsWith("POSGAR 94")) {
            return "EPSG:4190";
        }
        if (alias.startsWith("CAMPO INCHAUSPE")) {
            return "EPSG:4221";
        }
        if (alias.startsWith("CHINA GEODETIC COORDINATE SYSTEM 2000") || alias.startsWith("CGCS2000")) {
            return "EPSG:4490";
        }
        if (alias.startsWith("WGS 84 / UTM ")) {
            String utm = alias.substring("WGS 84 / UTM ".length()).trim();
            Integer zone = extractLeadingInteger(utm);
            if (zone != null && zone >= 1 && zone <= 60) {
                if (utm.endsWith("N")) {
                    return "EPSG:" + (32600 + zone);
                }
                if (utm.endsWith("S")) {
                    return "EPSG:" + (32700 + zone);
                }
            }
        }

        return "";
    }

    private static Integer extractTrailingZone(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        int lastSpace = text.lastIndexOf(' ');
        if (lastSpace < 0 || lastSpace >= text.length() - 1) {
            return null;
        }
        try {
            return Integer.parseInt(text.substring(lastSpace + 1).trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Integer extractLeadingInteger(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isDigit(c)) {
                digits.append(c);
            } else if (digits.length() > 0) {
                break;
            }
        }
        if (digits.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(digits.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static String buildManualDefinitionValue(String definition) {
        String trimmed = definition != null ? definition.trim() : "";
        return trimmed.isBlank() ? "" : "WKT:" + trimmed;
    }

    public static boolean isManualDefinition(String spec) {
        return spec != null && spec.trim().regionMatches(true, 0, "WKT:", 0, 4);
    }

    public static String stripManualPrefix(String spec) {
        if (!isManualDefinition(spec)) {
            return spec != null ? spec : "";
        }
        return spec.trim().substring(4).trim();
    }

    public static CoordinateReferenceSystem decode(String spec, boolean longitudeFirst) throws Exception {
        String normalized = normalizeCode(spec);
        if (normalized.isBlank()) {
            return DefaultGeographicCRS.WGS84;
        }
        if (isManualDefinition(normalized)) {
            return CRS.parseWKT(stripManualPrefix(normalized));
        }
        if ("EPSG:4326".equalsIgnoreCase(normalized)) {
            return DefaultGeographicCRS.WGS84;
        }

        CoordinateReferenceSystem manualFirst = decodeManualFallback(normalized);
        if (manualFirst != null) {
            return manualFirst;
        }

        try {
            return CRS.decode(normalized, longitudeFirst);
        } catch (Exception primary) {
            throw primary;
        }
    }

    public static CrsTechnicalDetails describe(String spec) {
        String normalized = normalizeCode(spec);
        if (normalized.isBlank()) {
            return CrsTechnicalDetails.unavailable(DEFAULT_CRS);
        }

        try {
            CoordinateReferenceSystem crs = decode(normalized, true);
            Bounds bounds = resolveBounds(crs);
            return new CrsTechnicalDetails(
                    normalized,
                    getLabelForCode(normalized),
                    safeName(crs),
                    describeType(crs),
                    describeDatum(crs),
                    describeUnit(crs),
                    bounds.hasBounds() ? bounds.formatArea() : "Sin area de uso reportada",
                    describeProjectionMethod(crs),
                    describeParameters(crs),
                    bounds.west(),
                    bounds.south(),
                    bounds.east(),
                    bounds.north(),
                    bounds.hasBounds(),
                    crs instanceof GeographicCRS,
                    isManualDefinition(normalized)
            );
        } catch (Exception ex) {
            return CrsTechnicalDetails.unavailable(normalized);
        }
    }

    public static LinkedHashMap<String, String> filter(String searchText) {
        LinkedHashMap<String, String> filtered = new LinkedHashMap<>();
        for (CrsCatalogEntry entry : filterEntries(searchText)) {
            filtered.put(entry.label(), entry.code());
        }
        return filtered;
    }

    private static LinkedHashMap<String, String> createMapFromEntries(List<CrsCatalogEntry> entries) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (CrsCatalogEntry entry : entries) {
            map.put(entry.label(), entry.code());
        }
        return map;
    }

    private static List<CrsCatalogEntry> buildFeaturedEntries() {
        List<CrsCatalogEntry> entries = new ArrayList<>();
        addFeatured(entries, "EPSG:4326", "WGS 84 (Lat/Long)");
        addFeatured(entries, "EPSG:4258", "ETRS89 geograficas");
        addFeatured(entries, "EPSG:4269", "NAD83 geograficas");
        addFeatured(entries, "EPSG:4674", "POSGAR 2007 geograficas");
        addFeatured(entries, "EPSG:4190", "POSGAR 94 geograficas");
        addFeatured(entries, "EPSG:4221", "Campo Inchauspe geograficas");
        addFeatured(entries, "EPSG:3857", "Web Mercator");
        addFeatured(entries, "EPSG:3395", "WGS 84 / World Mercator");
        addFeatured(entries, "EPSG:4087", "WGS 84 / World Equidistant Cylindrical");
        addFeatured(entries, "EPSG:3035", "ETRS89 / LAEA Europe");
        addFeatured(entries, "EPSG:27700", "OSGB36 / British National Grid");
        addFeatured(entries, "EPSG:2154", "RGF93 / Lambert-93");
        addFeatured(entries, "EPSG:4490", "China Geodetic Coordinate System 2000");

        for (int zone = 1; zone <= 60; zone++) {
            addFeatured(entries, "EPSG:" + (32600 + zone), "WGS 84 / UTM " + zone + "N");
            addFeatured(entries, "EPSG:" + (32700 + zone), "WGS 84 / UTM " + zone + "S");
        }
        for (int i = 1; i <= 7; i++) {
            addFeatured(entries, "EPSG:" + (22180 + i), "POSGAR 94 / Argentina " + i);
            addFeatured(entries, "EPSG:" + (5342 + i), "POSGAR 2007 / Argentina " + i);
            addFeatured(entries, "EPSG:" + (22190 + i), "Campo Inchauspe / Argentina " + i);
        }
        return entries;
    }

    private static void addFeatured(List<CrsCatalogEntry> entries, String code, String description) {
        String normalized = normalizeCode(code);
        entries.add(new CrsCatalogEntry(
                normalized,
                normalized + " - " + description,
                description,
                true,
                buildSearchText(normalized, description)
        ));
    }

    private static String buildSearchText(String code, String description) {
        return (code + " " + description).toLowerCase(Locale.ROOT);
    }

    private static boolean looksLikeWkt(String text) {
        String value = text != null ? text.trim().toUpperCase(Locale.ROOT) : "";
        return value.startsWith("GEOGCS[")
                || value.startsWith("PROJCS[")
                || value.startsWith("GEODCRS[")
                || value.startsWith("PROJCRS[")
                || value.startsWith("COMPOUNDCRS[");
    }

    private static CoordinateReferenceSystem decodeManualFallback(String normalized) throws Exception {
        String wkt = manualWktForCode(normalized);
        if (wkt == null || wkt.isBlank()) {
            return null;
        }
        return CRS.parseWKT(wkt);
    }

    private static String manualWktForCode(String normalized) {
        String code = normalizeCode(normalized);
        return switch (code) {
            case "EPSG:4258" -> buildGeographicWkt("ETRS89", "European_Terrestrial_Reference_System_1989", "GRS 1980", 6378137.0, 298.257222101);
            case "EPSG:4269" -> buildGeographicWkt("NAD83", "North_American_Datum_1983", "GRS 1980", 6378137.0, 298.257222101);
            case "EPSG:4674" -> buildGeographicWkt("POSGAR 2007", "POSGAR_2007", "GRS 1980", 6378137.0, 298.257222101);
            case "EPSG:4190" -> buildGeographicWkt("POSGAR 94", "POSGAR_1994", "WGS 84", 6378137.0, 298.257223563);
            case "EPSG:4221" -> buildGeographicWkt("Campo Inchauspe", "Campo_Inchauspe", "International 1924", 6378388.0, 297.0);
            case "EPSG:3857" -> buildMercatorWkt("WGS 84 / Pseudo-Mercator", "WGS_1984", "WGS 84", 6378137.0, 298.257223563);
            case "EPSG:3395" -> buildMercatorWkt("WGS 84 / World Mercator", "WGS_1984", "WGS 84", 6378137.0, 298.257223563);
            case "EPSG:4087" -> buildEquidistantCylindricalWkt("WGS 84 / World Equidistant Cylindrical", "WGS_1984", "WGS 84", 6378137.0, 298.257223563);
            case "EPSG:4490" -> buildGeographicWkt("China Geodetic Coordinate System 2000", "China_2000", "CGCS2000", 6378137.0, 298.257222101);
            default -> manualProjectedWkt(code);
        };
    }

    private static String manualProjectedWkt(String code) {
        if (code.matches("EPSG:326\\d\\d") || code.matches("EPSG:327\\d\\d")) {
            int zone = Integer.parseInt(code.substring(code.length() - 2));
            boolean north = code.startsWith("EPSG:326");
            return buildUtmWkt(zone, north);
        }
        if (code.matches("EPSG:2218[1-7]")) {
            int zone = Integer.parseInt(code.substring(code.length() - 1));
            return buildArgentinaZoneWkt("POSGAR 94 / Argentina " + zone, "POSGAR_1994", "WGS 84", 6378137.0, 298.257223563, zone);
        }
        if (code.matches("EPSG:534[3-9]")) {
            int zone = Integer.parseInt(code.substring(code.length() - 1)) - 2;
            return buildArgentinaZoneWkt("POSGAR 2007 / Argentina " + zone, "POSGAR_2007", "GRS 1980", 6378137.0, 298.257222101, zone);
        }
        if (code.matches("EPSG:2219[1-7]")) {
            int zone = Integer.parseInt(code.substring(code.length() - 1));
            return buildArgentinaZoneWkt("Campo Inchauspe / Argentina " + zone, "Campo_Inchauspe", "International 1924", 6378388.0, 297.0, zone);
        }
        return null;
    }

    private static String buildGeographicWkt(String name, String datumName, String spheroidName, double semiMajor, double inverseFlattening) {
        return "GEOGCS[\"" + name + "\"," +
                "DATUM[\"" + datumName + "\"," +
                "SPHEROID[\"" + spheroidName + "\"," + semiMajor + "," + inverseFlattening + "]]," +
                "PRIMEM[\"Greenwich\",0.0]," +
                "UNIT[\"degree\",0.0174532925199433]]";
    }

    private static String buildMercatorWkt(String name, String datumName, String spheroidName, double semiMajor, double inverseFlattening) {
        return "PROJCS[\"" + name + "\"," +
                buildGeographicWkt(name, datumName, spheroidName, semiMajor, inverseFlattening) + "," +
                "PROJECTION[\"Mercator_1SP\"]," +
                "PARAMETER[\"central_meridian\",0.0]," +
                "PARAMETER[\"scale_factor\",1.0]," +
                "PARAMETER[\"false_easting\",0.0]," +
                "PARAMETER[\"false_northing\",10000000.0]," +
                "UNIT[\"metre\",1.0]]";
    }

    private static String buildEquidistantCylindricalWkt(String name, String datumName, String spheroidName, double semiMajor, double inverseFlattening) {
        return "PROJCS[\"" + name + "\"," +
                buildGeographicWkt(name, datumName, spheroidName, semiMajor, inverseFlattening) + "," +
                "PROJECTION[\"Equidistant_Cylindrical\"]," +
                "PARAMETER[\"central_meridian\",0.0]," +
                "PARAMETER[\"standard_parallel_1\",0.0]," +
                "PARAMETER[\"false_easting\",0.0]," +
                "PARAMETER[\"false_northing\",0.0]," +
                "UNIT[\"metre\",1.0]]";
    }

    private static String buildUtmWkt(int zone, boolean north) {
        double falseNorthing = north ? 0.0 : 10000000.0;
        return "PROJCS[\"WGS 84 / UTM zone " + zone + (north ? "N" : "S") + "\"," +
                buildGeographicWkt("WGS 84", "WGS_1984", "WGS 84", 6378137.0, 298.257223563) + "," +
                "PROJECTION[\"Transverse_Mercator\"]," +
                "PARAMETER[\"latitude_of_origin\",0.0]," +
                "PARAMETER[\"central_meridian\"," + (-183 + zone * 6) + ".0]," +
                "PARAMETER[\"scale_factor\",0.9996]," +
                "PARAMETER[\"false_easting\",500000.0]," +
                "PARAMETER[\"false_northing\"," + falseNorthing + "]," +
                "UNIT[\"metre\",1.0]]";
    }

    private static String buildArgentinaZoneWkt(String name, String datumName, String spheroidName, double semiMajor, double inverseFlattening, int zone) {
        double centralMeridian = -75.0 + (zone * 3.0);
        double falseEasting = 500000.0 + (zone * 1000000.0);
        return "PROJCS[\"" + name + "\"," +
                buildGeographicWkt(name, datumName, spheroidName, semiMajor, inverseFlattening) + "," +
                "PROJECTION[\"Transverse_Mercator\"]," +
                "PARAMETER[\"latitude_of_origin\",0.0]," +
                "PARAMETER[\"central_meridian\"," + centralMeridian + "]," +
                "PARAMETER[\"scale_factor\",1.0]," +
                "PARAMETER[\"false_easting\"," + falseEasting + "]," +
                "PARAMETER[\"false_northing\",0.0]," +
                "UNIT[\"metre\",1.0]]";
    }

    private static String safeName(CoordinateReferenceSystem crs) {
        try {
            return crs != null && crs.getName() != null ? crs.getName().toString() : "CRS no disponible";
        } catch (Exception ex) {
            return "CRS no disponible";
        }
    }

    private static String describeType(CoordinateReferenceSystem crs) {
        if (crs instanceof ProjectedCRS) {
            return "Proyectado";
        }
        if (crs instanceof GeographicCRS) {
            return "Geografico";
        }
        if (crs instanceof SingleCRS) {
            return "Single CRS";
        }
        return crs != null ? crs.getClass().getSimpleName() : "No disponible";
    }

    private static String describeDatum(CoordinateReferenceSystem crs) {
        try {
            if (crs instanceof SingleCRS single) {
                Datum datum = single.getDatum();
                if (datum != null && datum.getName() != null) {
                    return datum.getName().toString();
                }
            }
        } catch (Exception ignored) {
        }
        return "No disponible";
    }

    private static String describeUnit(CoordinateReferenceSystem crs) {
        try {
            Object cs = crs != null ? crs.getCoordinateSystem() : null;
            if (cs != null) {
                Method axisMethod = cs.getClass().getMethod("getAxis", int.class);
                Object axis = axisMethod.invoke(cs, 0);
                if (axis != null) {
                    Method unitMethod = axis.getClass().getMethod("getUnit");
                    Object unit = unitMethod.invoke(axis);
                    if (unit != null) {
                        return unit.toString();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "No disponible";
    }

    private static String describeProjectionMethod(CoordinateReferenceSystem crs) {
        try {
            if (crs instanceof ProjectedCRS projected && projected.getConversionFromBase() != null
                    && projected.getConversionFromBase().getMethod() != null
                    && projected.getConversionFromBase().getMethod().getName() != null) {
                return projected.getConversionFromBase().getMethod().getName().toString();
            }
        } catch (Exception ignored) {
        }
        return crs instanceof GeographicCRS ? "Coordenadas geograficas" : "No disponible";
    }

    private static String describeParameters(CoordinateReferenceSystem crs) {
        if (!(crs instanceof ProjectedCRS projected)) {
            return "Sin parametros adicionales.";
        }
        try {
            ParameterValueGroup group = projected.getConversionFromBase().getParameterValues();
            if (group == null) {
                return "Sin parametros adicionales.";
            }
            StringBuilder sb = new StringBuilder();
            Collection<GeneralParameterValue> values = group.values();
            for (GeneralParameterValue value : values) {
                try {
                    Object descriptor = value.getDescriptor();
                    Method nameMethod = descriptor.getClass().getMethod("getName");
                    Object name = nameMethod.invoke(descriptor);
                    String label = name != null ? name.toString() : "parametro";
                    Method getValueMethod = value.getClass().getMethod("getValue");
                    Object parameterValue = getValueMethod.invoke(value);
                    if (sb.length() > 0) {
                        sb.append('\n');
                    }
                    sb.append(label).append(": ").append(parameterValue);
                } catch (Exception ignored) {
                }
            }
            return sb.length() == 0 ? "Sin parametros adicionales." : sb.toString();
        } catch (Exception ex) {
            return "Sin parametros adicionales.";
        }
    }

    private static Bounds resolveBounds(CoordinateReferenceSystem crs) {
        try {
            Method getEnvelope = CRS.class.getMethod("getEnvelope", CoordinateReferenceSystem.class);
            Object envelope = getEnvelope.invoke(null, crs);
            if (envelope != null) {
                Method min0 = envelope.getClass().getMethod("getMinimum", int.class);
                Method max0 = envelope.getClass().getMethod("getMaximum", int.class);
                double west = ((Number) min0.invoke(envelope, 0)).doubleValue();
                double east = ((Number) max0.invoke(envelope, 0)).doubleValue();
                double south = ((Number) min0.invoke(envelope, 1)).doubleValue();
                double north = ((Number) max0.invoke(envelope, 1)).doubleValue();
                return new Bounds(west, south, east, north, true);
            }
        } catch (Exception ignored) {
        }
        return new Bounds(0d, 0d, 0d, 0d, false);
    }

    private record Bounds(double west, double south, double east, double north, boolean hasBounds) {
        private String formatArea() {
            if (!hasBounds) {
                return "Sin area de uso reportada";
            }
            return String.format(
                    Locale.US,
                    "Oeste %.4f, Sur %.4f, Este %.4f, Norte %.4f",
                    west,
                    south,
                    east,
                    north
            );
        }
    }
}
