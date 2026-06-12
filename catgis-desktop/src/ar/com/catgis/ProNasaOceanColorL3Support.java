package ar.com.catgis;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProNasaOceanColorL3Support {

    static final String FAMILY = "NASA OceanColor L3 mapped";
    static final String FAMILY_L3B = "NASA OceanColor L3 binned";
    static final String PROVIDER = "NASA OceanColor";
    static final String QA_DESCRIPTOR_PRELIMINARY_RANGE = "nasa_oceancolor_l3m_preliminary_range_mask";

    private static final Pattern NEW_L3M_NAME_PATTERN = Pattern.compile(
            "^(?<mission>[A-Z0-9]+)_(?<instrument>[A-Z0-9]+)(?:_(?<type>[A-Z0-9]+))?\\.(?<time>[0-9T_]+)\\.(?<level>L3m)\\.(?<period>[A-Z0-9]+)\\.(?<suite>[A-Z0-9]+)\\.(?<product>[A-Za-z0-9_]+)\\.(?<resolution>[A-Za-z0-9]+)(?:\\.(?<stage>[A-Z0-9]+))?\\.nc$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern NEW_L3B_NAME_PATTERN = Pattern.compile(
            "^(?<mission>[A-Z0-9]+)_(?<instrument>[A-Z0-9]+)(?:_(?<type>[A-Z0-9]+))?\\.(?<time>[0-9T_]+)\\.(?<level>L3b)\\.(?<period>[A-Z0-9]+)\\.(?<suite>[A-Z0-9]+)(?:\\.(?<stage>[A-Z0-9]+))?\\.nc$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern LEGACY_L3M_NAME_PATTERN = Pattern.compile(
            "^(?<mission>[A-Z])(?<time>\\d{7}(?:\\d{6})?(?:\\d{7}(?:\\d{6})?)?)\\.(?<level>L3m)_(?<period>[A-Z0-9]+)_(?<suite>[A-Z0-9]+)_(?<product>[A-Za-z0-9_]+)_(?<resolution>[A-Za-z0-9]+)(?:_(?<stage>[A-Z0-9]+))?\\.nc$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern LEGACY_L3B_NAME_PATTERN = Pattern.compile(
            "^(?<mission>[A-Z])(?<time>\\d{7}(?:\\d{6})?(?:\\d{7}(?:\\d{6})?)?)\\.(?<level>L3b)_(?<period>[A-Z0-9]+)_(?<suite>[A-Z0-9]+)(?:_(?<stage>[A-Z0-9]+))?\\.nc$",
            Pattern.CASE_INSENSITIVE
    );

    private ProNasaOceanColorL3Support() {
    }

    static ResolvedHints apply(File sourceFile,
                               String gdalInfo,
                               String sourceExpression,
                               ProDatasetDescriptor dataset,
                               ProVariableDescriptor variable) {
        if (!matches(sourceFile, gdalInfo)) {
            return ResolvedHints.none();
        }
        Map<String, String> metadata = parseMetadata(gdalInfo);
        String fileName = sourceFile != null ? sourceFile.getName() : "";
        FilenameInfo filenameInfo = parseFilename(fileName);
        ProductDefaults productDefaults = resolveProductDefaults(
                firstNonBlank(
                        variable != null ? variable.getName() : "",
                        filenameInfo != null ? filenameInfo.product() : "",
                        sourceExpression,
                        filenameInfo != null ? filenameInfo.suite() : ""
                )
        );

        if (dataset != null) {
            if (dataset.getDatasetId().isBlank() && sourceFile != null) {
                dataset.setDatasetId(stripExtension(sourceFile.getName()));
            }
            if (dataset.getFamily().isBlank()) {
                dataset.setFamily(filenameInfo != null ? filenameInfo.family() : FAMILY);
            }
            if (dataset.getProvider().isBlank() || "Structured source".equalsIgnoreCase(dataset.getProvider())) {
                dataset.setProvider(PROVIDER);
            }
            if (dataset.getPlatform().isBlank()) {
                dataset.setPlatform(resolvePlatform(metadata, filenameInfo));
            }
            if (dataset.getInstrument().isBlank()) {
                dataset.setInstrument(resolveInstrument(metadata, filenameInfo));
            }
            if (dataset.getProcessingLevel().isBlank()) {
                dataset.setProcessingLevel(firstNonBlank(
                        readMetadata(metadata, "processing_level"),
                        readMetadata(metadata, "processingLevel"),
                        filenameInfo != null ? filenameInfo.defaultProcessingLevelLabel() : "L3m"
                ));
            }
            if (dataset.getAcquisitionStart().isBlank()) {
                dataset.setAcquisitionStart(resolveAcquisitionStart(metadata, filenameInfo));
            }
            if (dataset.getAcquisitionEnd().isBlank()) {
                dataset.setAcquisitionEnd(resolveAcquisitionEnd(metadata, filenameInfo));
            }
        }

        if (variable != null) {
            String filenameProduct = filenameInfo != null ? filenameInfo.product() : "";
            if (shouldReplaceVariableName(variable.getName(), sourceFile, filenameProduct)) {
                variable.setName(filenameProduct);
            }
            if (variable.getLongName().isBlank()) {
                variable.setLongName(firstNonBlank(
                        readMetadata(metadata, "long_name"),
                        productDefaults.longName()
                ));
            }
            if (variable.getStandardName().isBlank()) {
                variable.setStandardName(firstNonBlank(
                        readMetadata(metadata, "standard_name"),
                        productDefaults.standardName()
                ));
            }
            if (variable.getUnits().isBlank()) {
                variable.setUnits(firstNonBlank(readMetadata(metadata, "units"), productDefaults.units()));
            }
            if (variable.getBandFamily().isBlank()) {
                variable.setBandFamily(firstNonBlank(productDefaults.bandFamily(), filenameInfo != null ? filenameInfo.suite() : ""));
            }
            if (variable.getQaDescriptor().isBlank()) {
                variable.setQaDescriptor(QA_DESCRIPTOR_PRELIMINARY_RANGE);
            }
            if (variable.getNodata() == null) {
                variable.setNodata(readDouble(metadata, "_FillValue", "missing_value", "fill_value", "bad_value_scaled"));
            }
            if (variable.getScaleFactor() == null) {
                variable.setScaleFactor(readDouble(metadata, "scale_factor"));
            }
            if (variable.getAddOffset() == null) {
                variable.setAddOffset(readDouble(metadata, "add_offset"));
            }
            if (variable.getValidMin() == null) {
                variable.setValidMin(firstNonNull(readDouble(metadata, "valid_min"), productDefaults.validMin()));
            }
            if (variable.getValidMax() == null) {
                variable.setValidMax(firstNonNull(readDouble(metadata, "valid_max"), productDefaults.validMax()));
            }
        }

        return new ResolvedHints(
                productDefaults.presetId(),
                productDefaults.flagsApplied(),
                filenameInfo != null && filenameInfo.isBinnedLevel() ? "nasa_oceancolor_l3b_detected" : "nasa_oceancolor_l3m_open",
                filenameInfo != null && filenameInfo.isBinnedLevel() ? "exploratorio" : "preliminar",
                true,
                filenameInfo == null || !filenameInfo.isBinnedLevel()
        );
    }

    static boolean matches(File sourceFile, String gdalInfo) {
        if (sourceFile == null) {
            return false;
        }
        String name = sourceFile.getName();
        String lowerName = name.toLowerCase(Locale.ROOT);
        if (!(lowerName.endsWith(".nc") || lowerName.endsWith(".nc4"))) {
            return false;
        }
        if (parseFilename(name) != null) {
            return true;
        }
        String text = gdalInfo != null ? gdalInfo.toLowerCase(Locale.ROOT) : "";
        return text.contains("oceancolor")
                || text.contains("ocean biology processing group")
                || text.contains("l3m")
                || text.contains("l3b");
    }

    static FilenameInfo parseFilename(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        Matcher newMatcher = NEW_L3M_NAME_PATTERN.matcher(fileName.trim());
        if (newMatcher.matches()) {
            return new FilenameInfo(
                    newMatcher.group("mission"),
                    newMatcher.group("instrument"),
                    newMatcher.group("level"),
                    newMatcher.group("period"),
                    newMatcher.group("suite"),
                    newMatcher.group("product"),
                    newMatcher.group("resolution"),
                    newMatcher.group("stage"),
                    parseTimeRange(newMatcher.group("time"), false)
            );
        }
        Matcher newL3bMatcher = NEW_L3B_NAME_PATTERN.matcher(fileName.trim());
        if (newL3bMatcher.matches()) {
            return new FilenameInfo(
                    newL3bMatcher.group("mission"),
                    newL3bMatcher.group("instrument"),
                    newL3bMatcher.group("level"),
                    newL3bMatcher.group("period"),
                    newL3bMatcher.group("suite"),
                    inferProductFromSuite(newL3bMatcher.group("suite")),
                    "",
                    newL3bMatcher.group("stage"),
                    parseTimeRange(newL3bMatcher.group("time"), false)
            );
        }
        Matcher legacyMatcher = LEGACY_L3M_NAME_PATTERN.matcher(fileName.trim());
        if (legacyMatcher.matches()) {
            return new FilenameInfo(
                    expandLegacyMission(legacyMatcher.group("mission")),
                    inferInstrumentFromMission(expandLegacyMission(legacyMatcher.group("mission"))),
                    legacyMatcher.group("level"),
                    legacyMatcher.group("period"),
                    legacyMatcher.group("suite"),
                    legacyMatcher.group("product"),
                    legacyMatcher.group("resolution"),
                    legacyMatcher.group("stage"),
                    parseTimeRange(legacyMatcher.group("time"), true)
            );
        }
        Matcher legacyL3bMatcher = LEGACY_L3B_NAME_PATTERN.matcher(fileName.trim());
        if (legacyL3bMatcher.matches()) {
            return new FilenameInfo(
                    expandLegacyMission(legacyL3bMatcher.group("mission")),
                    inferInstrumentFromMission(expandLegacyMission(legacyL3bMatcher.group("mission"))),
                    legacyL3bMatcher.group("level"),
                    legacyL3bMatcher.group("period"),
                    legacyL3bMatcher.group("suite"),
                    inferProductFromSuite(legacyL3bMatcher.group("suite")),
                    "",
                    legacyL3bMatcher.group("stage"),
                    parseTimeRange(legacyL3bMatcher.group("time"), true)
            );
        }
        return null;
    }

    private static String resolvePlatform(Map<String, String> metadata, FilenameInfo filenameInfo) {
        return firstNonBlank(
                readMetadata(metadata, "platform"),
                readMetadata(metadata, "sensor_platform"),
                humanizePlatform(filenameInfo != null ? filenameInfo.mission() : "")
        );
    }

    private static String resolveInstrument(Map<String, String> metadata, FilenameInfo filenameInfo) {
        return firstNonBlank(
                readMetadata(metadata, "instrument"),
                readMetadata(metadata, "instrument_name"),
                humanizeInstrument(filenameInfo != null ? filenameInfo.instrument() : "")
        );
    }

    private static String resolveAcquisitionStart(Map<String, String> metadata, FilenameInfo filenameInfo) {
        return firstNonBlank(
                readMetadata(metadata, "time_coverage_start"),
                readMetadata(metadata, "start_time"),
                filenameInfo != null ? filenameInfo.timeRange().start() : ""
        );
    }

    private static String resolveAcquisitionEnd(Map<String, String> metadata, FilenameInfo filenameInfo) {
        return firstNonBlank(
                readMetadata(metadata, "time_coverage_end"),
                readMetadata(metadata, "stop_time"),
                filenameInfo != null ? filenameInfo.timeRange().end() : ""
        );
    }

    private static ProductDefaults resolveProductDefaults(String rawName) {
        String normalized = normalize(rawName);
        if (normalized.contains("chlor") || "chl".equals(normalized)) {
            return new ProductDefaults(
                    "chlor_a_mvp",
                    "Chlorophyll-a concentration",
                    "mass_concentration_of_chlorophyll_a_in_sea_water",
                    "mg m-3",
                    "CHL",
                    0.001d,
                    100.0d,
                    List.of("fill_value", "outside_valid_range", "non_positive")
            );
        }
        if (normalized.contains("kd490")) {
            return new ProductDefaults(
                    "kd490_mvp",
                    "Diffuse attenuation coefficient at 490 nm",
                    "",
                    "m-1",
                    "CHL",
                    0.001d,
                    6.0d,
                    List.of("fill_value", "outside_valid_range", "non_positive")
            );
        }
        if (normalized.contains("tsm") || normalized.contains("turb") || normalized.contains("spm")) {
            return new ProductDefaults(
                    "turbidity_tsm_mvp",
                    "Total suspended matter",
                    "",
                    "g m-3",
                    "TURB",
                    0.0d,
                    300.0d,
                    List.of("fill_value", "outside_valid_range", "high_suspended_matter_review")
            );
        }
        return new ProductDefaults(
                "",
                "",
                "",
                "",
                "",
                null,
                null,
                List.of("fill_value", "review")
        );
    }

    private static boolean shouldReplaceVariableName(String currentName, File sourceFile, String productName) {
        if (productName == null || productName.isBlank()) {
            return false;
        }
        if (currentName == null || currentName.isBlank()) {
            return true;
        }
        String normalizedCurrent = normalize(currentName);
        String normalizedProduct = normalize(productName);
        if (normalizedCurrent.equals(normalizedProduct)) {
            return false;
        }
        String sourceName = sourceFile != null ? sourceFile.getName() : "";
        String strippedSourceName = stripExtension(sourceName);
        return normalizedCurrent.equals(normalize(sourceName))
                || normalizedCurrent.equals(normalize(strippedSourceName))
                || normalizedCurrent.contains(".l3m.")
                || normalizedCurrent.contains(".l3b.")
                || normalizedCurrent.endsWith(".nc")
                || normalizedCurrent.endsWith(".nc4");
    }

    private static Map<String, String> parseMetadata(String gdalInfo) {
        Map<String, String> values = new LinkedHashMap<>();
        if (gdalInfo == null || gdalInfo.isBlank()) {
            return values;
        }
        String[] lines = gdalInfo.replace('\r', '\n').split("\n");
        for (String rawLine : lines) {
            String line = rawLine != null ? rawLine.trim() : "";
            if (line.isBlank()) {
                continue;
            }
            int equalsIndex = line.indexOf('=');
            if (equalsIndex <= 0 || equalsIndex >= line.length() - 1) {
                continue;
            }
            String key = line.substring(0, equalsIndex).trim();
            String value = line.substring(equalsIndex + 1).trim();
            String normalizedKey = normalizeMetadataKey(key);
            if (!normalizedKey.isBlank() && !values.containsKey(normalizedKey)) {
                values.put(normalizedKey, value);
            }
        }
        return values;
    }

    private static String readMetadata(Map<String, String> metadata, String key) {
        if (metadata == null || key == null || key.isBlank()) {
            return "";
        }
        String value = metadata.get(normalizeMetadataKey(key));
        return value != null ? value.trim() : "";
    }

    private static Double readDouble(Map<String, String> metadata, String... keys) {
        if (metadata == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            String raw = readMetadata(metadata, key);
            if (raw.isBlank()) {
                continue;
            }
            try {
                return Double.parseDouble(raw);
            } catch (Exception ignored) { CatgisLogger.warn("ProNasaOceanColorL3Support: operation failed", ignored); }
        }
        return null;
    }

    private static String normalizeMetadataKey(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        String normalized = key.trim();
        int lastHash = normalized.lastIndexOf('#');
        if (lastHash >= 0 && lastHash < normalized.length() - 1) {
            normalized = normalized.substring(lastHash + 1);
        }
        int lastColon = normalized.lastIndexOf(':');
        if (lastColon >= 0 && lastColon < normalized.length() - 1) {
            normalized = normalized.substring(lastColon + 1);
        }
        return normalize(normalized);
    }

    private static String expandLegacyMission(String missionCode) {
        String normalized = normalize(missionCode);
        return switch (normalized) {
            case "a" -> "AQUA";
            case "t" -> "TERRA";
            case "s" -> "SEASTAR";
            case "v" -> "VIIRS";
            default -> missionCode != null ? missionCode.trim().toUpperCase(Locale.ROOT) : "";
        };
    }

    private static String inferInstrumentFromMission(String mission) {
        String normalized = normalize(mission);
        return switch (normalized) {
            case "aqua", "terra" -> "MODIS";
            case "s3a", "s3b" -> "OLCI";
            case "snpp", "jpss1", "jpss2", "viirs" -> "VIIRS";
            case "pace" -> "OCI";
            case "seastar" -> "SEAWIFS";
            default -> "";
        };
    }

    private static String humanizePlatform(String mission) {
        String normalized = normalize(mission);
        return switch (normalized) {
            case "aqua" -> "Aqua";
            case "terra" -> "Terra";
            case "s3a" -> "Sentinel-3A";
            case "s3b" -> "Sentinel-3B";
            case "snpp" -> "Suomi NPP";
            case "jpss1" -> "NOAA-20 / JPSS-1";
            case "jpss2" -> "NOAA-21 / JPSS-2";
            case "pace" -> "PACE";
            case "seastar" -> "SeaStar";
            default -> mission != null ? mission.trim() : "";
        };
    }

    private static String humanizeInstrument(String instrument) {
        String normalized = normalize(instrument);
        return switch (normalized) {
            case "modis" -> "MODIS";
            case "olci" -> "OLCI";
            case "viirs" -> "VIIRS";
            case "oci" -> "OCI";
            case "seawifs" -> "SeaWiFS";
            default -> instrument != null ? instrument.trim() : "";
        };
    }

    private static String inferProductFromSuite(String suite) {
        String normalized = normalize(suite);
        return switch (normalized) {
            case "chl" -> "chlor_a";
            case "kd490" -> "kd490";
            case "tsm", "turb", "spm" -> "tsm";
            default -> suite != null ? suite.trim().toLowerCase(Locale.ROOT) : "";
        };
    }

    private static TimeRange parseTimeRange(String raw, boolean legacyDayOfYear) {
        if (raw == null || raw.isBlank()) {
            return new TimeRange("", "");
        }
        String[] tokens = raw.trim().split("_");
        if (tokens.length == 1) {
            String start = legacyDayOfYear ? formatLegacyToken(tokens[0], false) : formatIsoToken(tokens[0], false);
            String end = legacyDayOfYear ? formatLegacyToken(tokens[0], true) : formatIsoToken(tokens[0], true);
            return new TimeRange(start, end);
        }
        String start = legacyDayOfYear ? formatLegacyToken(tokens[0], false) : formatIsoToken(tokens[0], false);
        String end = legacyDayOfYear ? formatLegacyToken(tokens[tokens.length - 1], true) : formatIsoToken(tokens[tokens.length - 1], true);
        return new TimeRange(start, end);
    }

    private static String formatIsoToken(String token, boolean endOfPeriod) {
        String value = token != null ? token.trim() : "";
        if (value.isBlank()) {
            return "";
        }
        if (value.length() == 8) {
            try {
                LocalDate date = LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE);
                return (endOfPeriod ? date.atTime(23, 59, 59) : date.atStartOfDay()).atOffset(ZoneOffset.UTC).toString();
            } catch (Exception ignored) { CatgisLogger.warn("ProNasaOceanColorL3Support: operation failed", ignored); }
        }
        if (value.length() == 15 && value.charAt(8) == 'T') {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
                return dateTime.atOffset(ZoneOffset.UTC).toString();
            } catch (Exception ignored) { CatgisLogger.warn("ProNasaOceanColorL3Support: operation failed", ignored); }
        }
        return value;
    }

    private static String formatLegacyToken(String token, boolean endOfPeriod) {
        String value = token != null ? token.trim() : "";
        if (value.isBlank()) {
            return "";
        }
        try {
            if (value.length() == 7) {
                int year = Integer.parseInt(value.substring(0, 4));
                int dayOfYear = Integer.parseInt(value.substring(4, 7));
                LocalDate date = LocalDate.ofYearDay(year, dayOfYear);
                return (endOfPeriod ? date.atTime(23, 59, 59) : date.atStartOfDay()).atOffset(ZoneOffset.UTC).toString();
            }
            if (value.length() == 13) {
                int year = Integer.parseInt(value.substring(0, 4));
                int dayOfYear = Integer.parseInt(value.substring(4, 7));
                LocalDate date = LocalDate.ofYearDay(year, dayOfYear);
                int hour = Integer.parseInt(value.substring(7, 9));
                int minute = Integer.parseInt(value.substring(9, 11));
                int second = Integer.parseInt(value.substring(11, 13));
                return date.atTime(hour, minute, second).atOffset(ZoneOffset.UTC).toString();
            }
        } catch (Exception ignored) { CatgisLogger.warn("ProNasaOceanColorL3Support: operation failed", ignored); }
        return value;
    }

    private static String stripExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    record ResolvedHints(String qualityPreset,
                         List<String> flagsApplied,
                         String recipe,
                         String maturity,
                         boolean matched,
                         boolean materializable) {

        static ResolvedHints none() {
            return new ResolvedHints("", List.of(), "", "", false, false);
        }
    }

    record FilenameInfo(String mission,
                        String instrument,
                        String level,
                        String period,
                        String suite,
                        String product,
                        String resolution,
                        String stage,
                        TimeRange timeRange) {

        boolean isBinnedLevel() {
            return "l3b".equals(normalize(level));
        }

        String family() {
            return isBinnedLevel() ? FAMILY_L3B : FAMILY;
        }

        String defaultProcessingLevelLabel() {
            String levelLabel = level != null && !level.isBlank() ? level : "L3m";
            String periodLabel = period != null && !period.isBlank() ? " " + period : "";
            return levelLabel + periodLabel;
        }
    }

    record TimeRange(String start, String end) {
    }

    private record ProductDefaults(String presetId,
                                   String longName,
                                   String standardName,
                                   String units,
                                   String bandFamily,
                                   Double validMin,
                                   Double validMax,
                                   List<String> flagsApplied) {
    }
}
