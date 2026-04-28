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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.Normalizer;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CRSDefinitions {

    private static final String DEFAULT_CRS = "EPSG:4326";
    private static final String EMBEDDED_EPSG_ZIP_RESOURCE = "org/geotools/referencing/factory/epsg/hsql/EPSG.zip";
    private static final int MIN_WORLD_CATALOG_SIZE = 1000;
    private static final double COUNTRY_ALIAS_SCALE_FACTOR = 20d;
    private static final Locale SPANISH_LOCALE = Locale.forLanguageTag("es");
    private static volatile List<CrsCatalogEntry> cachedCatalog;
    private static volatile LinkedHashMap<String, String> cachedCatalogMap;
    private static final Map<String, CrsTechnicalDetails> cachedDetails = Collections.synchronizedMap(
            new LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, CrsTechnicalDetails> eldest) {
                    return size() > 1500;
                }
            }
    );

    private CRSDefinitions() {
    }

    public record CrsCatalogEntry(
            String code,
            String label,
            String description,
            boolean featured,
            String searchText,
            String kind,
            String areaName,
            String areaDescription,
            double west,
            double south,
            double east,
            double north,
            boolean hasBounds
    ) {
        public static CrsCatalogEntry featured(String code, String label, String description, String searchText) {
            return new CrsCatalogEntry(code, label, description, true, searchText, "", "", "", 0d, 0d, 0d, 0d, false);
        }

        public static CrsCatalogEntry catalog(String code,
                                              String label,
                                              String description,
                                              String searchText,
                                              String kind,
                                              String areaName,
                                              String areaDescription,
                                              double west,
                                              double south,
                                              double east,
                                              double north,
                                              boolean hasBounds) {
            return new CrsCatalogEntry(
                    code,
                    label,
                    description,
                    false,
                    searchText,
                    kind,
                    areaName,
                    areaDescription,
                    west,
                    south,
                    east,
                    north,
                    hasBounds
            );
        }

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

            for (CrsCatalogEntry entry : loadEmbeddedWorldEntries()) {
                CrsCatalogEntry existing = merged.get(entry.code());
                if (existing == null) {
                    merged.put(entry.code(), entry);
                } else {
                    merged.put(entry.code(), mergeEntries(existing, entry));
                }
            }

            if (merged.size() < MIN_WORLD_CATALOG_SIZE) {
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
                                    CrsCatalogEntry.catalog(
                                            code,
                                            code + " - " + description,
                                            description,
                                            buildSearchText(code, description),
                                            "",
                                            "",
                                            "",
                                            0d,
                                            0d,
                                            0d,
                                            0d,
                                            false
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

        String query = canonicalizeSearchText(searchText);
        String digitQuery = extractDigits(query);
        boolean codeFirstSearch = !digitQuery.isBlank();
        List<CrsCatalogEntry> filtered = new ArrayList<>();
        for (CrsCatalogEntry entry : entries) {
            if (entry == null) {
                continue;
            }
            if (codeFirstSearch) {
                String codeDigits = extractDigits(entry.code());
                if (!codeDigits.isBlank() && codeDigits.contains(digitQuery)) {
                    filtered.add(entry);
                    continue;
                }
                if (!entry.searchText().contains(query)) {
                    continue;
                }
                filtered.add(entry);
                continue;
            }
            if (entry.searchText().contains(query)) {
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

        CrsCatalogEntry entry = findCatalogEntry(normalized);
        if (entry != null) {
            return entry.label();
        }

        return normalized;
    }

    private static CrsCatalogEntry findCatalogEntry(String code) {
        String normalized = normalizeCode(code);
        if (normalized.isBlank()) {
            return null;
        }
        for (CrsCatalogEntry entry : getCatalogEntries()) {
            if (normalized.equalsIgnoreCase(entry.code())) {
                return entry;
            }
        }
        return null;
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

        if (alias.equals("PAMPA DEL CASTILLO")) {
            return "EPSG:4161";
        }
        if (alias.equals("PAMPA DEL CASTILLO (DEG)")) {
            return "EPSG:61616405";
        }
        if (alias.equals("PAMPA DEL CASTILLO / ARGENTINA 1")) {
            return "EPSG:9284";
        }
        if (alias.equals("PAMPA DEL CASTILLO / ARGENTINA 2")) {
            return "EPSG:2082";
        }
        if (alias.equals("PAMPA DEL CASTILLO / ARGENTINA 3")) {
            return "EPSG:9285";
        }

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

        try {
            return CRS.decode(normalized, longitudeFirst);
        } catch (Exception primary) {
            CoordinateReferenceSystem manualFallback = decodeManualFallback(normalized);
            if (manualFallback != null) {
                return manualFallback;
            }
            throw primary;
        }
    }

    public static CrsTechnicalDetails describe(String spec) {
        String normalized = normalizeCode(spec);
        if (normalized.isBlank()) {
            return CrsTechnicalDetails.unavailable(DEFAULT_CRS);
        }
        if (!isManualDefinition(normalized)) {
            CrsTechnicalDetails cached = cachedDetails.get(normalized);
            if (cached != null) {
                return cached;
            }
        }

        try {
            CoordinateReferenceSystem crs = decode(normalized, true);
            Bounds bounds = resolveBounds(crs);
            CrsTechnicalDetails details = new CrsTechnicalDetails(
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
            details = applyBoundsFallback(normalized, details);
            if (!isManualDefinition(normalized)) {
                cachedDetails.put(normalized, details);
            }
            return details;
        } catch (Exception ex) {
            CrsTechnicalDetails fallback = describeFromCatalogFallback(normalized);
            if (!isManualDefinition(normalized)
                    && fallback != null
                    && !"CRS no disponible".equalsIgnoreCase(fallback.name())) {
                cachedDetails.put(normalized, fallback);
            }
            return fallback;
        }
    }

    private static CrsTechnicalDetails describeFromCatalogFallback(String normalized) {
        CrsCatalogEntry entry = findCatalogEntry(normalized);
        if (entry == null) {
            return CrsTechnicalDetails.unavailable(normalized);
        }

        boolean geographic = isGeographicKind(entry.kind(), entry.description());
        String areaText = !safeTrim(entry.areaName()).isBlank()
                ? safeTrim(entry.areaName())
                : (!safeTrim(entry.areaDescription()).isBlank() ? safeTrim(entry.areaDescription()) : "Sin area de uso reportada");
        String name = entry.description();
        int areaSeparator = name.indexOf(" | ");
        if (areaSeparator > 0) {
            name = name.substring(0, areaSeparator).trim();
        }

        return new CrsTechnicalDetails(
                normalized,
                entry.label(),
                name,
                describeFallbackKind(entry.kind(), geographic),
                "No disponible",
                describeFallbackUnit(entry.kind(), geographic),
                areaText,
                geographic ? "Coordenadas geograficas" : "Detalle recuperado desde catalogo EPSG",
                "Detalle parcial recuperado desde el catalogo EPSG integrado.",
                entry.west(),
                entry.south(),
                entry.east(),
                entry.north(),
                entry.hasBounds(),
                geographic,
                isManualDefinition(normalized)
        );
    }

    private static CrsTechnicalDetails applyBoundsFallback(String normalized, CrsTechnicalDetails base) {
        if (base == null) {
            return null;
        }

        CrsCatalogEntry entry = findCatalogEntry(normalized);
        Bounds known = knownBoundsForCode(normalized);
        if (known != null && known.hasBounds() && shouldPreferKnownBounds(base, known)) {
            return withBounds(base, known, known.formatArea());
        }

        if (entry != null && entry.hasBounds() && (!base.hasBounds() || isGlobalishBounds(base))) {
            String areaText = !safeTrim(entry.areaName()).isBlank()
                    ? safeTrim(entry.areaName())
                    : (!safeTrim(entry.areaDescription()).isBlank() ? safeTrim(entry.areaDescription()) : base.areaOfUse());
            return withBounds(base, new Bounds(entry.west(), entry.south(), entry.east(), entry.north(), true), areaText);
        }

        if (!base.hasBounds() && known != null && known.hasBounds()) {
            return withBounds(base, known, known.formatArea());
        }

        return base;
    }

    private static CrsTechnicalDetails withBounds(CrsTechnicalDetails base, Bounds bounds, String areaText) {
        return new CrsTechnicalDetails(
                base.code(),
                base.label(),
                base.name(),
                base.type(),
                base.datum(),
                base.unit(),
                !safeTrim(areaText).isBlank() ? areaText : bounds.formatArea(),
                base.projectionMethod(),
                base.parameters(),
                bounds.west(),
                bounds.south(),
                bounds.east(),
                bounds.north(),
                true,
                base.geographic(),
                base.manual()
        );
    }

    private static boolean shouldPreferKnownBounds(CrsTechnicalDetails base, Bounds known) {
        if (base == null || known == null || !known.hasBounds()) {
            return false;
        }
        if (!base.hasBounds()) {
            return true;
        }
        if (isGlobalishBounds(base) && !isGlobalishBounds(known)) {
            return true;
        }
        return false;
    }

    private static boolean isGlobalishBounds(CrsTechnicalDetails details) {
        return details != null && isGlobalishBounds(details.west(), details.south(), details.east(), details.north());
    }

    private static boolean isGlobalishBounds(Bounds bounds) {
        return bounds != null && bounds.hasBounds() && isGlobalishBounds(bounds.west(), bounds.south(), bounds.east(), bounds.north());
    }

    private static boolean isGlobalishBounds(double west, double south, double east, double north) {
        return west <= -170d && east >= 170d && south <= -80d && north >= 80d;
    }

    private static Bounds knownBoundsForCode(String normalizedCode) {
        String code = normalizeCode(normalizedCode);
        Bounds argentina = new Bounds(-73.6d, -55.2d, -53.6d, -21.7d, true);
        if (code.matches("EPSG:2218[1-7]")) {
            return argentinaZoneBounds(Integer.parseInt(code.substring(code.length() - 1)));
        }
        if (code.matches("EPSG:534[3-9]")) {
            return argentinaZoneBounds(Integer.parseInt(code.substring(code.length() - 1)) - 2);
        }
        if (code.matches("EPSG:2219[1-7]")) {
            return argentinaZoneBounds(Integer.parseInt(code.substring(code.length() - 1)));
        }
        return switch (code) {
            case "EPSG:3857", "EPSG:3395" -> new Bounds(-180d, -85.06d, 180d, 85.06d, true);
            case "EPSG:4674", "EPSG:4190", "EPSG:4221", "EPSG:4161", "EPSG:61616405" -> argentina;
            case "EPSG:9284" -> argentinaZoneBounds(1);
            case "EPSG:2082" -> argentinaZoneBounds(2);
            case "EPSG:9285" -> argentinaZoneBounds(3);
            case "EPSG:4326", "EPSG:4258", "EPSG:4269", "EPSG:4087", "EPSG:4490" ->
                    new Bounds(-180d, -90d, 180d, 90d, true);
            default -> null;
        };
    }

    private static Bounds argentinaZoneBounds(int zone) {
        int normalizedZone = Math.max(1, Math.min(7, zone));
        double centralMeridian = -75.0d + (normalizedZone * 3.0d);
        return new Bounds(centralMeridian - 1.5d, -55.2d, centralMeridian + 1.5d, -21.7d, true);
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
        entries.add(CrsCatalogEntry.featured(
                normalized,
                normalized + " - " + description,
                description,
                buildSearchText(normalized, description)
        ));
    }

    private static String buildSearchText(String... parts) {
        StringBuilder sb = new StringBuilder();
        if (parts != null) {
            for (String part : parts) {
                if (part == null || part.isBlank()) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(part);
            }
        }
        return canonicalizeSearchText(sb.toString());
    }

    private static String canonicalizeSearchText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    private static String extractDigits(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isDigit(ch)) {
                digits.append(ch);
            }
        }
        return digits.toString();
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
            case "EPSG:4161" -> buildGeographicWkt(
                    "Pampa del Castillo",
                    "Pampa_del_Castillo",
                    "International 1924",
                    6378388.0,
                    297.0,
                    "TOWGS84[-233.43,6.65,173.64,0,0,0,0]"
            );
            case "EPSG:61616405" -> buildGeographicWkt(
                    "Pampa del Castillo (deg)",
                    "Pampa_del_Castillo",
                    "International 1924",
                    6378388.0,
                    297.0,
                    "TOWGS84[-233.43,6.65,173.64,0,0,0,0]"
            );
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
        if ("EPSG:9284".equals(code)) {
            return buildHistoricArgentinaProjectedWkt(
                    "Pampa del Castillo / Argentina 1",
                    "Pampa del Castillo",
                    "Pampa_del_Castillo",
                    "International 1924",
                    6378388.0,
                    297.0,
                    "TOWGS84[-233.43,6.65,173.64,0,0,0,0]",
                    -90.0,
                    -72.0,
                    1.0,
                    1500000.0,
                    0.0
            );
        }
        if ("EPSG:2082".equals(code)) {
            return buildHistoricArgentinaProjectedWkt(
                    "Pampa del Castillo / Argentina 2",
                    "Pampa del Castillo",
                    "Pampa_del_Castillo",
                    "International 1924",
                    6378388.0,
                    297.0,
                    "TOWGS84[-233.43,6.65,173.64,0,0,0,0]",
                    -90.0,
                    -69.0,
                    1.0,
                    2500000.0,
                    0.0
            );
        }
        if ("EPSG:9285".equals(code)) {
            return buildHistoricArgentinaProjectedWkt(
                    "Pampa del Castillo / Argentina 3",
                    "Pampa del Castillo",
                    "Pampa_del_Castillo",
                    "International 1924",
                    6378388.0,
                    297.0,
                    "TOWGS84[-233.43,6.65,173.64,0,0,0,0]",
                    -90.0,
                    -66.0,
                    1.0,
                    3500000.0,
                    0.0
            );
        }
        return null;
    }

    private static String buildGeographicWkt(String name, String datumName, String spheroidName, double semiMajor, double inverseFlattening) {
        return buildGeographicWkt(name, datumName, spheroidName, semiMajor, inverseFlattening, null);
    }

    private static String buildGeographicWkt(String name,
                                             String datumName,
                                             String spheroidName,
                                             double semiMajor,
                                             double inverseFlattening,
                                             String datumExtra) {
        String extra = (datumExtra == null || datumExtra.isBlank()) ? "" : "," + datumExtra;
        return "GEOGCS[\"" + name + "\"," +
                "DATUM[\"" + datumName + "\"," +
                "SPHEROID[\"" + spheroidName + "\"," + semiMajor + "," + inverseFlattening + "]" + extra + "]," +
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
                "PARAMETER[\"latitude_of_origin\",-90.0]," +
                "PARAMETER[\"central_meridian\"," + centralMeridian + "]," +
                "PARAMETER[\"scale_factor\",1.0]," +
                "PARAMETER[\"false_easting\"," + falseEasting + "]," +
                "PARAMETER[\"false_northing\",0.0]," +
                "UNIT[\"metre\",1.0]]";
    }

    private static String buildHistoricArgentinaProjectedWkt(String name,
                                                             String geographicName,
                                                             String datumName,
                                                             String spheroidName,
                                                             double semiMajor,
                                                             double inverseFlattening,
                                                             String datumExtra,
                                                             double latitudeOfOrigin,
                                                             double centralMeridian,
                                                             double scaleFactor,
                                                             double falseEasting,
                                                             double falseNorthing) {
        return "PROJCS[\"" + name + "\"," +
                buildGeographicWkt(geographicName, datumName, spheroidName, semiMajor, inverseFlattening, datumExtra) + "," +
                "PROJECTION[\"Transverse_Mercator\"]," +
                "PARAMETER[\"latitude_of_origin\"," + latitudeOfOrigin + "]," +
                "PARAMETER[\"central_meridian\"," + centralMeridian + "]," +
                "PARAMETER[\"scale_factor\"," + scaleFactor + "]," +
                "PARAMETER[\"false_easting\"," + falseEasting + "]," +
                "PARAMETER[\"false_northing\"," + falseNorthing + "]," +
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

    private static boolean isGeographicKind(String kind, String description) {
        String combined = (safeTrim(kind) + " " + safeTrim(description)).toLowerCase(Locale.ROOT);
        return combined.contains("geograph") || combined.contains("geograf") || combined.contains("lat/long");
    }

    private static String describeFallbackKind(String kind, boolean geographic) {
        if (geographic) {
            return "Geografico";
        }
        String normalized = safeTrim(kind).toLowerCase(Locale.ROOT);
        if (normalized.contains("project")) {
            return "Proyectado";
        }
        return normalized.isBlank() ? "No disponible" : safeTrim(kind);
    }

    private static String describeFallbackUnit(String kind, boolean geographic) {
        if (geographic) {
            return "deg";
        }
        String normalized = safeTrim(kind).toLowerCase(Locale.ROOT);
        if (normalized.contains("project")) {
            return "m";
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

    private static List<CrsCatalogEntry> loadEmbeddedWorldEntries() {
        List<CrsCatalogEntry> entries = new ArrayList<>();
        try {
            Path databasePath = ensureEmbeddedEpsgDatabase();
            Path hsqldbJar = resolveHsqldbJar();
            if (databasePath == null) {
                return entries;
            }
            if (hsqldbJar == null || !Files.exists(hsqldbJar)) {
                return entries;
            }

            System.setProperty("hsqldb.reconfig_logging", "false");

            String url = "jdbc:hsqldb:file:" + databasePath.toAbsolutePath().toString().replace('\\', '/') + ";ifexists=true;readonly=true";
            String sql = """
                    select
                        c.COORD_REF_SYS_CODE,
                        c.COORD_REF_SYS_NAME,
                        c.COORD_REF_SYS_KIND,
                        c.DEPRECATED,
                        e.EXTENT_NAME,
                        e.EXTENT_DESCRIPTION,
                        e.ISO_A2_CODE,
                        e.ISO_A3_CODE,
                        e.BBOX_WEST_BOUND_LON,
                        e.BBOX_SOUTH_BOUND_LAT,
                        e.BBOX_EAST_BOUND_LON,
                        e.BBOX_NORTH_BOUND_LAT
                    from EPSG_COORDINATEREFERENCESYSTEM c
                    left join (
                        select OBJECT_CODE, min(EXTENT_CODE) as EXTENT_CODE
                        from EPSG_USAGE
                        where OBJECT_TABLE_NAME = 'epsg_coordinatereferencesystem'
                        group by OBJECT_CODE
                    ) u on u.OBJECT_CODE = c.COORD_REF_SYS_CODE
                    left join EPSG_EXTENT e on u.EXTENT_CODE = e.EXTENT_CODE
                    where c.SHOW_CRS = 1
                    order by c.COORD_REF_SYS_CODE
                    """;

            try (URLClassLoader loader = new URLClassLoader(new URL[]{hsqldbJar.toUri().toURL()}, ClassLoader.getPlatformClassLoader());
                 Connection connection = openIsolatedHsqlConnection(loader, url);
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sql)) {
                List<CountryExtent> countryExtents = loadCountryExtents(connection);
                while (rs.next()) {
                    String code = normalizeCode("EPSG:" + rs.getInt("COORD_REF_SYS_CODE"));
                    if (code.isBlank()) {
                        continue;
                    }
                    String name = safeTrim(rs.getString("COORD_REF_SYS_NAME"));
                    if (name.isBlank()) {
                        continue;
                    }
                    String areaName = safeTrim(rs.getString("EXTENT_NAME"));
                    String areaDescription = safeTrim(rs.getString("EXTENT_DESCRIPTION"));
                    String isoA2 = safeTrim(rs.getString("ISO_A2_CODE"));
                    String isoA3 = safeTrim(rs.getString("ISO_A3_CODE"));
                    String kind = safeTrim(rs.getString("COORD_REF_SYS_KIND"));
                    boolean deprecated = rs.getInt("DEPRECATED") != 0;
                    Double west = getNullableDouble(rs, "BBOX_WEST_BOUND_LON");
                    Double south = getNullableDouble(rs, "BBOX_SOUTH_BOUND_LAT");
                    Double east = getNullableDouble(rs, "BBOX_EAST_BOUND_LON");
                    Double north = getNullableDouble(rs, "BBOX_NORTH_BOUND_LAT");

                    String description = name;
                    if (!areaName.isBlank()) {
                        description += " | " + areaName;
                    } else if (!areaDescription.isBlank()) {
                        description += " | " + areaDescription;
                    }
                    if (deprecated) {
                        description += " | deprecated";
                    }

                    boolean hasBounds = west != null && south != null && east != null && north != null;
                    entries.add(CrsCatalogEntry.catalog(
                            code,
                            code + " - " + description,
                            description,
                            buildSearchText(
                                    code,
                                    name,
                                    kind,
                                    areaName,
                                    areaDescription,
                                    isoA2,
                                    isoA3,
                                    localizedCountryNames(isoA2, isoA3),
                                    buildIntersectingCountryAliases(countryExtents, west, south, east, north)
                            ),
                            kind,
                            areaName,
                            areaDescription,
                            west != null ? west : 0d,
                            south != null ? south : 0d,
                            east != null ? east : 0d,
                            north != null ? north : 0d,
                            hasBounds
                    ));
                }
            }
        } catch (Exception ignored) {
        }
        return entries;
    }

    private static Path ensureEmbeddedEpsgDatabase() {
        try {
            Path baseDir = Path.of(System.getProperty("java.io.tmpdir"), "catgis-epsg-cache");
            Files.createDirectories(baseDir);
            Path marker = baseDir.resolve("EPSG.script");
            if (Files.exists(marker)) {
                return baseDir.resolve("EPSG");
            }

            try (InputStream raw = CRSDefinitions.class.getClassLoader().getResourceAsStream(EMBEDDED_EPSG_ZIP_RESOURCE)) {
                if (raw == null) {
                    return null;
                }
                try (ZipInputStream zip = new ZipInputStream(raw)) {
                    ZipEntry entry;
                    while ((entry = zip.getNextEntry()) != null) {
                        Path target = baseDir.resolve(entry.getName()).normalize();
                        if (!target.startsWith(baseDir)) {
                            continue;
                        }
                        if (entry.isDirectory()) {
                            Files.createDirectories(target);
                        } else {
                            Files.createDirectories(target.getParent());
                            try (OutputStream out = Files.newOutputStream(target)) {
                                zip.transferTo(out);
                            }
                        }
                        zip.closeEntry();
                    }
                }
            }
            return Files.exists(marker) ? baseDir.resolve("EPSG") : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private static Connection openIsolatedHsqlConnection(URLClassLoader loader, String url) throws Exception {
        Class<?> driverClass = Class.forName("org.hsqldb.jdbc.JDBCDriver", true, loader);
        Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();
        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("user", "SA");
        properties.setProperty("password", "");
        return driver.connect(url, properties);
    }

    private static List<CountryExtent> loadCountryExtents(Connection connection) throws Exception {
        List<CountryExtent> countries = new ArrayList<>();
        String sql = """
                select
                    EXTENT_NAME,
                    EXTENT_DESCRIPTION,
                    ISO_A2_CODE,
                    ISO_A3_CODE,
                    BBOX_WEST_BOUND_LON,
                    BBOX_SOUTH_BOUND_LAT,
                    BBOX_EAST_BOUND_LON,
                    BBOX_NORTH_BOUND_LAT
                from EPSG_EXTENT
                where (ISO_A2_CODE is not null or ISO_A3_CODE is not null)
                  and BBOX_WEST_BOUND_LON is not null
                  and BBOX_SOUTH_BOUND_LAT is not null
                  and BBOX_EAST_BOUND_LON is not null
                  and BBOX_NORTH_BOUND_LAT is not null
                """;
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                String aliases = buildSearchText(
                        rs.getString("EXTENT_NAME"),
                        rs.getString("EXTENT_DESCRIPTION"),
                        rs.getString("ISO_A2_CODE"),
                        rs.getString("ISO_A3_CODE"),
                        localizedCountryNames(rs.getString("ISO_A2_CODE"), rs.getString("ISO_A3_CODE"))
                );
                if (aliases.isBlank()) {
                    continue;
                }
                countries.add(new CountryExtent(
                        aliases,
                        rs.getDouble("BBOX_WEST_BOUND_LON"),
                        rs.getDouble("BBOX_SOUTH_BOUND_LAT"),
                        rs.getDouble("BBOX_EAST_BOUND_LON"),
                        rs.getDouble("BBOX_NORTH_BOUND_LAT")
                ));
            }
        }
        return countries;
    }

    private static String buildIntersectingCountryAliases(List<CountryExtent> countryExtents,
                                                          Double west,
                                                          Double south,
                                                          Double east,
                                                          Double north) {
        if (countryExtents == null || countryExtents.isEmpty() || west == null || south == null || east == null || north == null) {
            return "";
        }
        double crsArea = Math.max(0d, Math.abs(east - west) * Math.abs(north - south));
        if (crsArea <= 0d) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (CountryExtent country : countryExtents) {
            if (country == null || !country.intersects(west, south, east, north) || !country.isReasonableScale(crsArea)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(country.searchText());
        }
        return sb.toString();
    }

    private static Path resolveHsqldbJar() {
        String classPath = System.getProperty("java.class.path", "");
        String[] entries = classPath.split(java.util.regex.Pattern.quote(File.pathSeparator));
        for (String entry : entries) {
            String normalized = safeTrim(entry).toLowerCase(Locale.ROOT);
            if (normalized.endsWith(".jar") && normalized.contains("hsqldb")) {
                try {
                    return Path.of(entry);
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    private static String localizedCountryNames(String isoA2, String isoA3) {
        StringBuilder sb = new StringBuilder();
        appendCountryVariant(sb, isoA2);
        if (sb.length() == 0) {
            appendCountryVariant(sb, isoA3ToIsoA2(isoA3));
        }
        return sb.toString();
    }

    private static void appendCountryVariant(StringBuilder sb, String isoA2) {
        String code = safeTrim(isoA2);
        if (code.length() != 2) {
            return;
        }
        Locale locale = new Locale("", code.toUpperCase(Locale.ROOT));
        String english = safeTrim(locale.getDisplayCountry(Locale.ENGLISH));
        String spanish = safeTrim(locale.getDisplayCountry(SPANISH_LOCALE));
        if (!english.isBlank()) {
            sb.append(english);
        }
        if (!spanish.isBlank() && !spanish.equalsIgnoreCase(english)) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(spanish);
        }
    }

    private static String isoA3ToIsoA2(String isoA3) {
        String normalized = safeTrim(isoA3).toUpperCase(Locale.ROOT);
        if (normalized.length() != 3) {
            return "";
        }
        for (String country : Locale.getISOCountries()) {
            Locale locale = new Locale("", country);
            if (normalized.equalsIgnoreCase(safeTrim(locale.getISO3Country()))) {
                return country;
            }
        }
        return "";
    }

    private static String safeTrim(String value) {
        return value != null ? value.trim() : "";
    }

    private static CrsCatalogEntry mergeEntries(CrsCatalogEntry primary, CrsCatalogEntry secondary) {
        String code = primary != null ? primary.code() : secondary.code();
        String label = secondary != null && secondary.label() != null && !secondary.label().isBlank()
                ? secondary.label()
                : primary.label();
        String description = secondary != null && secondary.description() != null && !secondary.description().isBlank()
                ? secondary.description()
                : primary.description();
        boolean featured = (primary != null && primary.featured()) || (secondary != null && secondary.featured());
        String searchText = buildSearchText(
                primary != null ? primary.searchText() : "",
                secondary != null ? secondary.searchText() : "",
                primary != null ? primary.description() : "",
                secondary != null ? secondary.description() : "",
                label
        );
        String kind = secondary != null && secondary.kind() != null && !secondary.kind().isBlank()
                ? secondary.kind()
                : (primary != null ? primary.kind() : "");
        String areaName = secondary != null && secondary.areaName() != null && !secondary.areaName().isBlank()
                ? secondary.areaName()
                : (primary != null ? primary.areaName() : "");
        String areaDescription = secondary != null && secondary.areaDescription() != null && !secondary.areaDescription().isBlank()
                ? secondary.areaDescription()
                : (primary != null ? primary.areaDescription() : "");
        boolean hasBounds = (secondary != null && secondary.hasBounds()) || (primary != null && primary.hasBounds());
        double west = secondary != null && secondary.hasBounds() ? secondary.west() : (primary != null ? primary.west() : 0d);
        double south = secondary != null && secondary.hasBounds() ? secondary.south() : (primary != null ? primary.south() : 0d);
        double east = secondary != null && secondary.hasBounds() ? secondary.east() : (primary != null ? primary.east() : 0d);
        double north = secondary != null && secondary.hasBounds() ? secondary.north() : (primary != null ? primary.north() : 0d);
        return new CrsCatalogEntry(code, label, description, featured, searchText, kind, areaName, areaDescription, west, south, east, north, hasBounds);
    }

    private static Double getNullableDouble(ResultSet rs, String column) throws Exception {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private record CountryExtent(String searchText, double west, double south, double east, double north) {
        private boolean intersects(double otherWest, double otherSouth, double otherEast, double otherNorth) {
            return !(otherEast < west || otherWest > east || otherNorth < south || otherSouth > north);
        }

        private boolean isReasonableScale(double crsArea) {
            double countryArea = Math.max(0.01d, Math.abs(east - west) * Math.abs(north - south));
            return crsArea <= countryArea * COUNTRY_ALIAS_SCALE_FACTOR;
        }
    }
}
