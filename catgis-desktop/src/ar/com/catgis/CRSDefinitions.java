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
                        } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar entrada de catalogo CRS", ignored); }
                    }
                    for (CrsCatalogEntry entry : authorityEntries.values()) {
                        merged.put(entry.code(), entry);
                    }
        } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar datum CRS", ignored); }
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
        } catch (Exception ignored) { CatgisLogger.warn("Error al describir metrica CRS", ignored); }
        return "No disponible";
    }

    private static String describeProjectionMethod(CoordinateReferenceSystem crs) {
        try {
            if (crs instanceof ProjectedCRS projected && projected.getConversionFromBase() != null
                    && projected.getConversionFromBase().getMethod() != null
                    && projected.getConversionFromBase().getMethod().getName() != null) {
                return projected.getConversionFromBase().getMethod().getName().toString();
            }
        } catch (Exception ignored) { CatgisLogger.warn("Error al describir metodo de proyeccion CRS", ignored); }
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
                } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar parametro CRS", ignored); }
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
        } catch (Exception ignored) { CatgisLogger.warn("Error al resolver envolvente CRS", ignored); }
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
        } catch (Exception ignored) { CatgisLogger.warn("Error al cargar entradas de catalogo CRS", ignored); }
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
                } catch (Exception ignored) { CatgisLogger.warn("Error al interpretar ruta HSQLDB", ignored); }
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
