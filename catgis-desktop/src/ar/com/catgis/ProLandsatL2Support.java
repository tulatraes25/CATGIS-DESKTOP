package ar.com.catgis;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProLandsatL2Support {

    static final String FAMILY = "USGS Landsat Collection 2 Level 2";
    static final String PROVIDER = "USGS Landsat";
    static final String QA_DESCRIPTOR_REFLECTANCE = "landsat_l2sp_preliminary_qapixel_companion";
    static final String QA_DESCRIPTOR_SURFACE_TEMPERATURE = "landsat_l2sp_preliminary_st_qapixel_companion";
    static final String QA_DESCRIPTOR_QA_PIXEL = "landsat_l2sp_qa_pixel_bits";
    static final String QA_DESCRIPTOR_QA_RADSAT = "landsat_l2sp_qa_radsat_bits";
    static final String QA_DESCRIPTOR_SR_QA_AEROSOL = "landsat_l2sp_sr_qa_aerosol_bits";
    static final String QA_DESCRIPTOR_ST_QA = "landsat_l2sp_st_qa_uncertainty";
    static final int QA_PIXEL_FILL_BIT = 1 << 0;
    static final int QA_PIXEL_DILATED_CLOUD_BIT = 1 << 1;
    static final int QA_PIXEL_CIRRUS_BIT = 1 << 2;
    static final int QA_PIXEL_CLOUD_BIT = 1 << 3;
    static final int QA_PIXEL_CLOUD_SHADOW_BIT = 1 << 4;
    static final int QA_PIXEL_SNOW_BIT = 1 << 5;
    static final int QA_PIXEL_WATER_BIT = 1 << 7;
    static final int QA_PIXEL_CLOUDS_MASK = QA_PIXEL_DILATED_CLOUD_BIT
            | QA_PIXEL_CIRRUS_BIT
            | QA_PIXEL_CLOUD_BIT;
    static final int PRELIMINARY_INVALID_QA_PIXEL_MASK = QA_PIXEL_FILL_BIT
            | QA_PIXEL_DILATED_CLOUD_BIT
            | QA_PIXEL_CIRRUS_BIT
            | QA_PIXEL_CLOUD_BIT
            | QA_PIXEL_CLOUD_SHADOW_BIT
            | QA_PIXEL_SNOW_BIT;

    private static final Pattern PRODUCT_RASTER_PATTERN = Pattern.compile(
            "^(?<product>LC0[89]_L2SP_[A-Z0-9_]+)_(?<variable>SR_B[1-7]|ST_B10|ST_QA|QA_PIXEL|QA_RADSAT|SR_QA_AEROSOL)$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^\\s*(?<key>[A-Z0-9_]+)\\s*=\\s*(?<value>.+?)\\s*$");

    private ProLandsatL2Support() {
    }

    static ResolvedHints apply(File rasterFile, ProDatasetDescriptor dataset, ProVariableDescriptor variable) {
        SceneContext scene = resolveSceneContext(rasterFile);
        if (scene == null) {
            return ResolvedHints.none();
        }
        VariableDefaults defaults = scene.variableDefaults();
        if (defaults == null) {
            return ResolvedHints.none();
        }
        String rasterBaseName = stripExtension(normalizeDuplicateArtifactName(rasterFile != null ? rasterFile.getName() : ""));

        if (dataset != null) {
            if (shouldReplaceDatasetId(dataset.getDatasetId(), rasterBaseName)) {
                dataset.setDatasetId(scene.productId());
            }
            if (dataset.getFamily().isBlank()) {
                dataset.setFamily(FAMILY);
            }
            if (dataset.getProvider().isBlank() || "Structured source".equalsIgnoreCase(dataset.getProvider())) {
                dataset.setProvider(PROVIDER);
            }
            if (dataset.getPlatform().isBlank()) {
                dataset.setPlatform(scene.platform());
            }
            if (dataset.getInstrument().isBlank()) {
                dataset.setInstrument(scene.instrument());
            }
            if (dataset.getProcessingLevel().isBlank()) {
                dataset.setProcessingLevel(scene.processingLevel());
            }
            if (dataset.getAcquisitionStart().isBlank()) {
                dataset.setAcquisitionStart(scene.acquisitionStart());
            }
            if (dataset.getAcquisitionEnd().isBlank()) {
                dataset.setAcquisitionEnd(scene.acquisitionEnd());
            }
        }

        if (variable != null) {
            if (shouldReplaceVariableName(variable.getName(), rasterBaseName)) {
                variable.setName(defaults.name());
            }
            if (shouldReplaceVariableLongName(variable.getLongName(), rasterBaseName)) {
                variable.setLongName(defaults.longName());
            }
            if (variable.getStandardName().isBlank()) {
                variable.setStandardName(defaults.standardName());
            }
            if (variable.getUnits().isBlank()) {
                variable.setUnits(defaults.units());
            }
            if (variable.getDimensions().isEmpty()) {
                variable.setDimensions(List.of("y", "x"));
            }
            if (variable.getNodata() == null) {
                variable.setNodata(defaults.nodata());
            }
            if (variable.getScaleFactor() == null) {
                variable.setScaleFactor(defaults.scaleFactor());
            }
            if (variable.getAddOffset() == null) {
                variable.setAddOffset(defaults.addOffset());
            }
            if (variable.getValidMin() == null) {
                variable.setValidMin(defaults.validMin());
            }
            if (variable.getValidMax() == null) {
                variable.setValidMax(defaults.validMax());
            }
            if (variable.getQaDescriptor().isBlank()) {
                variable.setQaDescriptor(defaults.qaDescriptor());
            }
            if (variable.getBandFamily().isBlank()) {
                variable.setBandFamily(defaults.bandFamily());
            }
        }

        return new ResolvedHints(
                defaults.presetId(),
                defaults.flagsApplied(),
                "landsat_l2sp_open",
                "preliminar",
                true
        );
    }

    private static SceneContext resolveSceneContext(File rasterFile) {
        SceneRef ref = resolveSceneRef(rasterFile);
        if (ref == null || rasterFile == null || rasterFile.getParentFile() == null) {
            return null;
        }
        File manifest = findMetadataFile(rasterFile.getParentFile(), ref.productId());
        if (manifest == null || !manifest.exists()) {
            return null;
        }
        Map<String, String> metadata = parseMetadataFile(manifest);
        if (metadata.isEmpty()) {
            return null;
        }
        return new SceneContext(ref, manifest, metadata);
    }

    private static SceneRef resolveSceneRef(File rasterFile) {
        if (rasterFile == null || !rasterFile.isFile()) {
            return null;
        }
        String canonicalName = stripExtension(normalizeDuplicateArtifactName(rasterFile.getName()));
        Matcher matcher = PRODUCT_RASTER_PATTERN.matcher(canonicalName);
        if (!matcher.matches()) {
            return null;
        }
        return new SceneRef(
                matcher.group("product").toUpperCase(Locale.ROOT),
                matcher.group("variable").toUpperCase(Locale.ROOT)
        );
    }

    static String normalizeDuplicateArtifactName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        return fileName.replaceFirst(" \\((\\d+)\\)(?=\\.[^.]+$)", "");
    }

    static File resolveQaPixelCompanion(File rasterFile, String datasetIdHint) {
        SceneContext scene = resolveSceneContext(rasterFile);
        if (scene != null) {
            File companion = scene.resolveSceneFile("FILE_NAME_QUALITY_L1_PIXEL", scene.productId() + "_QA_PIXEL.TIF");
            if (companion != null && companion.exists() && companion.isFile()) {
                return companion;
            }
        }
        File directory = rasterFile != null ? rasterFile.getParentFile() : null;
        String productId = firstNonBlank(datasetIdHint, scene != null ? scene.productId() : "");
        return findSceneFile(directory, productId, "QA_PIXEL.TIF");
    }

    static String preliminaryQaPixelSummary() {
        return "Excluye fill, dilated_cloud, cirrus, cloud, cloud_shadow y snow; conserva agua cuando no coincide con esos bits.";
    }

    static String preliminaryQaPixelBitsLabel() {
        return "fill,dilated_cloud,cirrus,cloud,cloud_shadow,snow";
    }

    static String qaPixelBitsLabel(int bitMask) {
        StringBuilder builder = new StringBuilder();
        appendQaPixelBit(builder, bitMask, QA_PIXEL_FILL_BIT, "fill");
        appendQaPixelBit(builder, bitMask, QA_PIXEL_DILATED_CLOUD_BIT, "dilated_cloud");
        appendQaPixelBit(builder, bitMask, QA_PIXEL_CIRRUS_BIT, "cirrus");
        appendQaPixelBit(builder, bitMask, QA_PIXEL_CLOUD_BIT, "cloud");
        appendQaPixelBit(builder, bitMask, QA_PIXEL_CLOUD_SHADOW_BIT, "cloud_shadow");
        appendQaPixelBit(builder, bitMask, QA_PIXEL_SNOW_BIT, "snow");
        appendQaPixelBit(builder, bitMask, QA_PIXEL_WATER_BIT, "water");
        return builder.toString();
    }

    static boolean hasPreliminaryInvalidQaBits(int qaPixelValue) {
        return (qaPixelValue & PRELIMINARY_INVALID_QA_PIXEL_MASK) != 0;
    }

    static boolean hasAnyQaPixelBits(int qaPixelValue, int bitMask) {
        return (qaPixelValue & bitMask) != 0;
    }

    private static File findMetadataFile(File directory, String productId) {
        if (directory == null || productId == null || productId.isBlank() || !directory.isDirectory()) {
            return null;
        }
        File exact = new File(directory, productId + "_MTL.txt");
        if (exact.exists() && exact.isFile()) {
            return exact;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (file == null || !file.isFile()) {
                continue;
            }
            String normalized = normalizeDuplicateArtifactName(file.getName());
            if ((productId + "_MTL.txt").equalsIgnoreCase(normalized)) {
                return file;
            }
        }
        return null;
    }

    private static File findSceneFile(File directory, String productId, String suffix) {
        if (directory == null || productId == null || productId.isBlank() || suffix == null || suffix.isBlank() || !directory.isDirectory()) {
            return null;
        }
        File exact = new File(directory, productId + "_" + suffix);
        if (exact.exists() && exact.isFile()) {
            return exact;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return null;
        }
        String expectedName = (productId + "_" + suffix).toUpperCase(Locale.ROOT);
        for (File file : files) {
            if (file == null || !file.isFile()) {
                continue;
            }
            String normalized = normalizeDuplicateArtifactName(file.getName()).toUpperCase(Locale.ROOT);
            if (expectedName.equals(normalized)) {
                return file;
            }
        }
        return null;
    }

    private static Map<String, String> parseMetadataFile(File metadataFile) {
        Map<String, String> values = new LinkedHashMap<>();
        if (metadataFile == null || !metadataFile.exists() || !metadataFile.isFile()) {
            return values;
        }
        try {
            List<String> lines = Files.readAllLines(metadataFile.toPath(), StandardCharsets.UTF_8);
            for (String line : lines) {
                Matcher matcher = KEY_VALUE_PATTERN.matcher(line != null ? line : "");
                if (!matcher.matches()) {
                    continue;
                }
                String key = matcher.group("key").trim();
                String value = unquote(matcher.group("value").trim());
                if (!key.isBlank() && !values.containsKey(key)) {
                    values.put(key, value);
                }
            }
        } catch (Exception ignored) {
        }
        return values;
    }

    private static void appendQaPixelBit(StringBuilder builder, int bitMask, int expectedBit, String label) {
        if ((bitMask & expectedBit) == 0) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(',');
        }
        builder.append(label);
    }

    private static String unquote(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    private static String stripExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }

    private static boolean shouldReplaceDatasetId(String currentValue, String rasterBaseName) {
        return currentValue == null
                || currentValue.isBlank()
                || normalizeToken(currentValue).equals(normalizeToken(rasterBaseName));
    }

    private static boolean shouldReplaceVariableName(String currentValue, String rasterBaseName) {
        return currentValue == null
                || currentValue.isBlank()
                || normalizeToken(currentValue).equals(normalizeToken(rasterBaseName));
    }

    private static boolean shouldReplaceVariableLongName(String currentValue, String rasterBaseName) {
        return currentValue == null
                || currentValue.isBlank()
                || normalizeToken(currentValue).equals(normalizeToken(rasterBaseName.replace('_', ' ')));
    }

    private static String normalizeToken(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    private static String humanizePlatform(String raw) {
        String normalized = raw != null ? raw.trim().toUpperCase(Locale.ROOT) : "";
        return switch (normalized) {
            case "LANDSAT_8" -> "Landsat 8";
            case "LANDSAT_9" -> "Landsat 9";
            default -> raw != null ? raw.trim().replace('_', ' ') : "";
        };
    }

    private static String humanizeInstrument(String raw) {
        String normalized = raw != null ? raw.trim().toUpperCase(Locale.ROOT) : "";
        return switch (normalized) {
            case "OLI_TIRS" -> "OLI/TIRS";
            default -> raw != null ? raw.trim().replace('_', '/') : "";
        };
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

    private static Double parseDouble(Map<String, String> metadata, String key) {
        if (metadata == null || key == null || key.isBlank()) {
            return null;
        }
        String raw = metadata.get(key);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    record ResolvedHints(String qualityPreset,
                         List<String> flagsApplied,
                         String recipe,
                         String maturity,
                         boolean matched) {

        static ResolvedHints none() {
            return new ResolvedHints("", List.of(), "", "", false);
        }
    }

    private record SceneRef(String productId, String variableToken) {
    }

    private record SceneContext(SceneRef ref, File metadataFile, Map<String, String> metadata) {

        String productId() {
            return firstNonBlank(metadata.get("LANDSAT_PRODUCT_ID"), ref.productId());
        }

        String platform() {
            return humanizePlatform(metadata.get("SPACECRAFT_ID"));
        }

        String instrument() {
            return humanizeInstrument(metadata.get("SENSOR_ID"));
        }

        String processingLevel() {
            return firstNonBlank(metadata.get("PROCESSING_LEVEL"), "L2SP");
        }

        String acquisitionStart() {
            String date = firstNonBlank(metadata.get("DATE_ACQUIRED"));
            String time = firstNonBlank(metadata.get("SCENE_CENTER_TIME"));
            if (!date.isBlank() && !time.isBlank()) {
                return date + "T" + time;
            }
            if (!date.isBlank()) {
                return date + "T00:00:00Z";
            }
            return "";
        }

        String acquisitionEnd() {
            return acquisitionStart();
        }

        File resolveSceneFile(String metadataKey, String fallbackName) {
            File directory = metadataFile != null ? metadataFile.getParentFile() : null;
            String fileName = firstNonBlank(metadata.get(metadataKey), fallbackName);
            if (directory == null || fileName.isBlank()) {
                return null;
            }
            File exact = new File(directory, fileName);
            if (exact.exists() && exact.isFile()) {
                return exact;
            }
            return findSceneFile(directory, productId(), fileName.replaceFirst("^" + Pattern.quote(productId()) + "_", ""));
        }

        VariableDefaults variableDefaults() {
            return switch (ref.variableToken()) {
                case "SR_B1" -> reflectiveBand(1, "sr_b1", "Reflectancia superficial - Banda 1 (Coastal/Aerosol)", "coastal_aerosol");
                case "SR_B2" -> reflectiveBand(2, "sr_b2", "Reflectancia superficial - Banda 2 (Blue)", "blue");
                case "SR_B3" -> reflectiveBand(3, "sr_b3", "Reflectancia superficial - Banda 3 (Green)", "green");
                case "SR_B4" -> reflectiveBand(4, "sr_b4", "Reflectancia superficial - Banda 4 (Red)", "red");
                case "SR_B5" -> reflectiveBand(5, "sr_b5", "Reflectancia superficial - Banda 5 (NIR)", "nir");
                case "SR_B6" -> reflectiveBand(6, "sr_b6", "Reflectancia superficial - Banda 6 (SWIR 1)", "swir1");
                case "SR_B7" -> reflectiveBand(7, "sr_b7", "Reflectancia superficial - Banda 7 (SWIR 2)", "swir2");
                case "ST_B10" -> surfaceTemperatureBand();
                case "ST_QA" -> new VariableDefaults(
                        "st_qa",
                        "Incertidumbre ST_QA Landsat",
                        "",
                        "kelvin",
                        0d,
                        null,
                        null,
                        0d,
                        null,
                        QA_DESCRIPTOR_ST_QA,
                        "LANDSAT_ST_QA",
                        "landsat_st_qa_mvp",
                        List.of("temperature_uncertainty")
                );
                case "QA_PIXEL" -> new VariableDefaults(
                        "qa_pixel",
                        "Mascara QA_PIXEL Landsat",
                        "",
                        "bitmask",
                        null,
                        null,
                        null,
                        0d,
                        65535d,
                        QA_DESCRIPTOR_QA_PIXEL,
                        "LANDSAT_QA",
                        "landsat_qapixel_mvp",
                        List.of("fill", "dilated_cloud", "cirrus", "cloud", "cloud_shadow", "snow", "water")
                );
                case "QA_RADSAT" -> new VariableDefaults(
                        "qa_radsat",
                        "Mascara QA_RADSAT Landsat",
                        "",
                        "bitmask",
                        null,
                        null,
                        null,
                        0d,
                        65535d,
                        QA_DESCRIPTOR_QA_RADSAT,
                        "LANDSAT_QA",
                        "landsat_radsat_qa_mvp",
                        List.of("saturation_bits")
                );
                case "SR_QA_AEROSOL" -> new VariableDefaults(
                        "sr_qa_aerosol",
                        "Mascara SR_QA_AEROSOL Landsat",
                        "",
                        "bitmask",
                        null,
                        null,
                        null,
                        0d,
                        255d,
                        QA_DESCRIPTOR_SR_QA_AEROSOL,
                        "LANDSAT_QA",
                        "landsat_aerosol_qa_mvp",
                        List.of("fill", "water", "interpolated_aerosol", "aerosol_level")
                );
                default -> null;
            };
        }

        private VariableDefaults reflectiveBand(int band,
                                                String name,
                                                String longName,
                                                String standardName) {
            Double scaleFactor = parseDouble(metadata, "REFLECTANCE_MULT_BAND_" + band);
            Double addOffset = parseDouble(metadata, "REFLECTANCE_ADD_BAND_" + band);
            Double validMin = parseDouble(metadata, "REFLECTANCE_MINIMUM_BAND_" + band);
            Double validMax = parseDouble(metadata, "REFLECTANCE_MAXIMUM_BAND_" + band);
            return new VariableDefaults(
                    name,
                    longName,
                    standardName,
                    "reflectance",
                    0d,
                    scaleFactor,
                    addOffset,
                    validMin,
                    validMax,
                    QA_DESCRIPTOR_REFLECTANCE,
                    "LANDSAT_SR",
                    "landsat_sr_l2sp_mvp",
                    List.of("fill", "dilated_cloud", "cirrus", "cloud", "cloud_shadow", "snow", "water")
            );
        }

        private VariableDefaults surfaceTemperatureBand() {
            Double scaleFactor = parseDouble(metadata, "TEMPERATURE_MULT_BAND_ST_B10");
            Double addOffset = parseDouble(metadata, "TEMPERATURE_ADD_BAND_ST_B10");
            Double validMin = parseDouble(metadata, "TEMPERATURE_MINIMUM_BAND_ST_B10");
            Double validMax = parseDouble(metadata, "TEMPERATURE_MAXIMUM_BAND_ST_B10");
            return new VariableDefaults(
                    "st_b10",
                    "Temperatura superficial - Banda termica 10",
                    "surface_temperature",
                    "kelvin",
                    0d,
                    scaleFactor,
                    addOffset,
                    validMin,
                    validMax,
                    QA_DESCRIPTOR_SURFACE_TEMPERATURE,
                    "LANDSAT_ST",
                    "landsat_st_l2sp_mvp",
                    List.of("fill", "dilated_cloud", "cirrus", "cloud", "cloud_shadow", "snow", "st_qa")
            );
        }
    }

    private record VariableDefaults(String name,
                                    String longName,
                                    String standardName,
                                    String units,
                                    Double nodata,
                                    Double scaleFactor,
                                    Double addOffset,
                                    Double validMin,
                                    Double validMax,
                                    String qaDescriptor,
                                    String bandFamily,
                                    String presetId,
                                    List<String> flagsApplied) {
    }
}
