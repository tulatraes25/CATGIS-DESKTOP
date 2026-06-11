package ar.com.catgis;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

final class ProDatasetOpenService {

    private static final String SIDECAR_EXTENSION = ".catgis-pro.json";
    private static final int DIRECTORY_SCAN_DEPTH = 4;
    private static final Pattern SUBDATASET_NAME_PATTERN = Pattern.compile("SUBDATASET_(\\d+)_NAME=(.+)");
    private static final Pattern SUBDATASET_DESC_PATTERN = Pattern.compile("SUBDATASET_(\\d+)_DESC=(.+)");

    private ProDatasetOpenService() {
    }

    static Catalog inspect(File target) {
        if (target == null) {
            return new Catalog(null, List.of(), List.of("No se selecciono ninguna fuente Pro."));
        }
        if (!target.exists()) {
            return new Catalog(target, List.of(), List.of("No existe la fuente seleccionada: " + target.getAbsolutePath()));
        }

        List<Entry> entries = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        if (target.isDirectory()) {
            inspectDirectory(target, entries, warnings);
        } else if (isSidecarFile(target)) {
            Entry entry = buildEntryFromSidecar(target);
            if (entry != null) {
                entries.add(entry);
            } else {
                warnings.add("El sidecar Pro seleccionado no pudo vincularse con un raster utilizable.");
            }
        } else if (isStructuredSource(target)) {
            inspectStructuredSource(target, entries, warnings);
        } else if (isDirectRasterSource(target)) {
            Entry entry = buildEntryFromRaster(target, null);
            if (entry != null) {
                entries.add(entry);
            }
        } else {
            warnings.add("La fuente seleccionada no corresponde a un raster Pro, sidecar Pro ni contenedor estructurado soportado.");
        }

        entries.sort(Comparator.comparing(Entry::sortKey, String.CASE_INSENSITIVE_ORDER));
        return new Catalog(target, List.copyOf(entries), List.copyOf(warnings));
    }

    static List<GdalSubdataset> parseGdalInfoSubdatasets(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        Map<Integer, String> names = new LinkedHashMap<>();
        Map<Integer, String> descriptions = new LinkedHashMap<>();
        String[] lines = text.replace('\r', '\n').split("\n");
        for (String rawLine : lines) {
            String line = rawLine != null ? rawLine.trim() : "";
            if (line.isBlank()) {
                continue;
            }
            Matcher nameMatcher = SUBDATASET_NAME_PATTERN.matcher(line);
            if (nameMatcher.matches()) {
                names.put(parseIndex(nameMatcher.group(1)), nameMatcher.group(2).trim());
                continue;
            }
            Matcher descMatcher = SUBDATASET_DESC_PATTERN.matcher(line);
            if (descMatcher.matches()) {
                descriptions.put(parseIndex(descMatcher.group(1)), descMatcher.group(2).trim());
            }
        }

        List<GdalSubdataset> parsed = new ArrayList<>();
        for (Map.Entry<Integer, String> item : names.entrySet()) {
            int index = item.getKey();
            parsed.add(new GdalSubdataset(index, item.getValue(), descriptions.getOrDefault(index, "")));
        }
        parsed.sort(Comparator.comparingInt(GdalSubdataset::index));
        return List.copyOf(parsed);
    }

    private static void inspectDirectory(File directory, List<Entry> entries, List<String> warnings) {
        try (Stream<Path> paths = Files.walk(directory.toPath(), DIRECTORY_SCAN_DEPTH)) {
            paths.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .sorted(Comparator.comparing(File::getAbsolutePath, String.CASE_INSENSITIVE_ORDER))
                    .forEach(file -> inspectFileCandidate(file, entries, warnings));
        } catch (Exception ex) {
            warnings.add("No se pudo recorrer la carpeta Pro: " + ex.getMessage());
        }
        if (entries.isEmpty()) {
            warnings.add("No se detectaron variables Pro abiertas dentro de la carpeta seleccionada.");
        }
    }

    private static void inspectFileCandidate(File file, List<Entry> entries, List<String> warnings) {
        if (file == null || !file.isFile()) {
            return;
        }
        if (isDuplicateArtifact(file)) {
            return;
        }
        if (isSidecarFile(file)) {
            return;
        }
        if (isStructuredSource(file)) {
            inspectStructuredSource(file, entries, warnings);
            return;
        }
        if (isDirectRasterSource(file)) {
            Entry entry = buildEntryFromRaster(file, null);
            if (entry != null) {
                entries.add(entry);
            }
        }
    }

    private static void inspectStructuredSource(File file, List<Entry> entries, List<String> warnings) {
        ProMetadataSidecarSupport.Metadata sidecarMetadata = ProMetadataSidecarSupport.read(file);
        String gdalInfo = runGdalInfo(file, warnings);
        List<GdalSubdataset> subdatasets = parseGdalInfoSubdatasets(gdalInfo);
        if (subdatasets.isEmpty()) {
            ProDatasetDescriptor dataset = copyDataset(sidecarMetadata != null ? sidecarMetadata.dataset() : null);
            fillDatasetDefaults(dataset, file);
            ProVariableDescriptor variable = copyVariable(sidecarMetadata != null ? sidecarMetadata.variable() : null);
            fillStructuredVariableDefaults(variable, file.getName(), "");
            ProNasaOceanColorL3Support.ResolvedHints hints = ProNasaOceanColorL3Support.apply(
                    file,
                    gdalInfo,
                    file.getName(),
                    dataset,
                    variable
            );
            boolean directStructuredOpenable = hints.matched()
                    && hints.materializable()
                    && !hints.qualityPreset().isBlank()
                    && ProRasterMaterializationService.isMaterializationSupported();
            String directStructuredAvailability = directStructuredOpenable
                    ? "Producto L3m directo listo para materializar con GDAL."
                    : hints.matched() && !hints.materializable()
                    ? "Producto NASA OceanColor L3b detectado. El flujo actual trabaja sobre L3m mapeado; usa el L3m derivado del mismo dia."
                    : hints.matched() && !hints.qualityPreset().isBlank()
                    ? "Producto L3m directo detectado, pero falta backend GDAL para materializarlo."
                    : "Catalogo Pro detectado, pero la materializacion directa de este contenedor queda para el backend Pro.";
            entries.add(buildEntry(
                    file,
                    null,
                    resolveSidecarFile(file, sidecarMetadata),
                    dataset,
                    variable,
                    firstNonBlank(sidecarMetadata != null ? sidecarMetadata.qualityPreset() : "", hints.qualityPreset()),
                    firstNonEmpty(sidecarMetadata != null ? sidecarMetadata.flagsApplied() : List.of(), hints.flagsApplied()),
                    firstNonBlank(sidecarMetadata != null ? sidecarMetadata.recipe() : "", hints.recipe()),
                    firstNonBlank(sidecarMetadata != null ? sidecarMetadata.maturity() : "", hints.maturity()),
                    "",
                    "",
                    directStructuredOpenable,
                    directStructuredAvailability
            ));
            if ((gdalInfo == null || gdalInfo.isBlank()) && !hints.matched()) {
                warnings.add("No se pudieron listar subdatasets para " + file.getName() + ". Si es un NetCDF/HDF complejo, conviene materializar variables raster primero.");
            }
            return;
        }

        for (GdalSubdataset subdataset : subdatasets) {
            ProDatasetDescriptor dataset = copyDataset(sidecarMetadata != null ? sidecarMetadata.dataset() : null);
            fillDatasetDefaults(dataset, file);

            ProVariableDescriptor variable = copyVariable(sidecarMetadata != null ? sidecarMetadata.variable() : null);
            fillStructuredVariableDefaults(variable, subdataset.name(), subdataset.description());
            ProNasaOceanColorL3Support.ResolvedHints hints = ProNasaOceanColorL3Support.apply(
                    file,
                    gdalInfo,
                    subdataset.name(),
                    dataset,
                    variable
            );
            if (shouldSkipStructuredSubdataset(variable, subdataset, hints)) {
                continue;
            }

            entries.add(buildEntry(
                    file,
                    null,
                    resolveSidecarFile(file, sidecarMetadata),
                    dataset,
                    variable,
                    firstNonBlank(sidecarMetadata != null ? sidecarMetadata.qualityPreset() : "", hints.qualityPreset()),
                    firstNonEmpty(sidecarMetadata != null ? sidecarMetadata.flagsApplied() : List.of(), hints.flagsApplied()),
                    firstNonBlank(sidecarMetadata != null ? sidecarMetadata.recipe() : "", hints.recipe()),
                    firstNonBlank(sidecarMetadata != null ? sidecarMetadata.maturity() : "", hints.maturity()),
                    subdataset.name(),
                    subdataset.description(),
                    ProRasterMaterializationService.isMaterializationSupported(),
                    ProRasterMaterializationService.isMaterializationSupported()
                            ? "Lista para materializar con GDAL."
                            : "Subdataset detectado, pero falta backend GDAL para materializarlo."
            ));
        }
    }

    static boolean shouldSkipStructuredSubdataset(ProVariableDescriptor variable,
                                                  GdalSubdataset subdataset,
                                                  ProNasaOceanColorL3Support.ResolvedHints hints) {
        String variableName = variable != null ? normalizeToken(variable.getName()) : "";
        String description = subdataset != null ? normalizeToken(subdataset.description()) : "";
        String sourceName = subdataset != null ? normalizeToken(subdataset.name()) : "";
        if ("palette".equals(variableName)
                || description.contains("palette")
                || sourceName.endsWith(":palette")) {
            return true;
        }
        return hints != null
                && hints.matched()
                && hints.qualityPreset().isBlank()
                && (variableName.startsWith("palette") || description.contains("palette"));
    }

    private static Entry buildEntryFromSidecar(File sidecarFile) {
        if (sidecarFile == null || !isSidecarFile(sidecarFile)) {
            return null;
        }
        ProMetadataSidecarSupport.Metadata metadata = ProMetadataSidecarSupport.readSidecar(sidecarFile);
        if (metadata == null) {
            return null;
        }
        String absolutePath = sidecarFile.getAbsolutePath();
        String rasterPath = absolutePath.substring(0, absolutePath.length() - SIDECAR_EXTENSION.length());
        File rasterFile = new File(rasterPath);
        if (!rasterFile.exists() || !isDirectRasterSource(rasterFile)) {
            ProDatasetDescriptor dataset = copyDataset(metadata.dataset());
            fillDatasetDefaults(dataset, sidecarFile);
            ProVariableDescriptor variable = copyVariable(metadata.variable());
            fillRasterVariableDefaults(variable, sidecarFile.getName());
            return buildEntry(
                    rasterFile,
                    null,
                    sidecarFile,
                    dataset,
                    variable,
                    metadata.qualityPreset(),
                    metadata.flagsApplied(),
                    metadata.recipe(),
                    metadata.maturity(),
                    "",
                    "",
                    false,
                    "El sidecar existe, pero todavia no hay un raster materializado asociado."
            );
        }
        return buildEntryFromRaster(rasterFile, metadata);
    }

    private static Entry buildEntryFromRaster(File rasterFile, ProMetadataSidecarSupport.Metadata metadataOverride) {
        if (rasterFile == null || !rasterFile.exists()) {
            return null;
        }
        ProMetadataSidecarSupport.Metadata metadata = metadataOverride != null ? metadataOverride : ProMetadataSidecarSupport.read(rasterFile);
        ProDatasetDescriptor dataset = copyDataset(metadata != null ? metadata.dataset() : null);
        fillDatasetDefaults(dataset, rasterFile);

        ProVariableDescriptor variable = copyVariable(metadata != null ? metadata.variable() : null);
        fillRasterVariableDefaults(variable, rasterFile.getName());
        ProNasaOceanColorL3Support.ResolvedHints hints = ProNasaOceanColorL3Support.apply(
                rasterFile,
                "",
                rasterFile.getName(),
                dataset,
                variable
        );
        ProLandsatL2Support.ResolvedHints landsatHints = ProLandsatL2Support.apply(
                rasterFile,
                dataset,
                variable
        );

        return buildEntry(
                rasterFile,
                rasterFile,
                resolveSidecarFile(rasterFile, metadata),
                dataset,
                variable,
                firstNonBlank(metadata != null ? metadata.qualityPreset() : "", hints.qualityPreset(), landsatHints.qualityPreset()),
                firstNonEmpty(metadata != null ? metadata.flagsApplied() : List.of(), hints.flagsApplied(), landsatHints.flagsApplied()),
                firstNonBlank(metadata != null ? metadata.recipe() : "", hints.recipe(), landsatHints.recipe()),
                firstNonBlank(metadata != null ? metadata.maturity() : "", hints.maturity(), landsatHints.maturity()),
                "",
                "",
                true,
                ""
        );
    }

    private static Entry buildEntry(File sourceFile,
                                    File rasterFile,
                                    File sidecarFile,
                                    ProDatasetDescriptor dataset,
                                    ProVariableDescriptor variable,
                                    String qualityPreset,
                                    List<String> flagsApplied,
                                    String recipe,
                                    String maturity,
                                    String gdalSubdatasetName,
                                    String gdalSubdatasetDescription,
                                    boolean openable,
                                    String availabilityNote) {
        ProOceanColorPresetSupport.ResolvedLineage lineage = ProOceanColorPresetSupport.resolve(
                variable,
                qualityPreset,
                flagsApplied,
                recipe,
                maturity
        );
        return new Entry(
                sourceFile,
                rasterFile,
                sidecarFile,
                dataset,
                variable,
                lineage.presetId(),
                lineage.flagsApplied(),
                lineage.recipe(),
                lineage.maturity(),
                gdalSubdatasetName,
                gdalSubdatasetDescription,
                openable,
                availabilityNote
        );
    }

    private static File resolveSidecarFile(File dataFile, ProMetadataSidecarSupport.Metadata metadata) {
        if (metadata != null && metadata.sidecarFile() != null) {
            return metadata.sidecarFile();
        }
        if (dataFile == null) {
            return null;
        }
        if (isSidecarFile(dataFile)) {
            return dataFile;
        }
        File candidate = ProMetadataSidecarSupport.sidecarFile(dataFile);
        return candidate.exists() ? candidate : null;
    }

    private static void fillDatasetDefaults(ProDatasetDescriptor dataset, File sourceFile) {
        if (dataset == null) {
            return;
        }
        if (dataset.getDatasetId().isBlank()) {
            dataset.setDatasetId(stripKnownExtension(sourceFile != null ? sourceFile.getName() : "dataset_pro"));
        }
        if (dataset.getProvider().isBlank() && sourceFile != null && isStructuredSource(sourceFile)) {
            dataset.setProvider("Structured source");
        }
    }

    private static void fillRasterVariableDefaults(ProVariableDescriptor variable, String fileName) {
        if (variable == null) {
            return;
        }
        String baseName = stripKnownExtension(fileName);
        if (variable.getName().isBlank()) {
            variable.setName(sanitizeVariableToken(baseName));
        }
        if (variable.getLongName().isBlank()) {
            variable.setLongName(baseName.replace('_', ' '));
        }
    }

    private static void fillStructuredVariableDefaults(ProVariableDescriptor variable, String sourceExpression, String description) {
        if (variable == null) {
            return;
        }
        String inferredName = inferVariableNameFromSource(sourceExpression, description);
        if (variable.getName().isBlank()) {
            variable.setName(inferredName);
        }
        if (variable.getLongName().isBlank()) {
            variable.setLongName(cleanStructuredDescription(description, inferredName));
        }
    }

    private static String inferVariableNameFromSource(String sourceExpression, String description) {
        String candidate = sourceExpression != null ? sourceExpression.trim() : "";
        if (!candidate.isBlank()) {
            int lastColon = candidate.lastIndexOf(':');
            if (lastColon >= 0 && lastColon < candidate.length() - 1) {
                candidate = candidate.substring(lastColon + 1).trim();
            }
            while (candidate.startsWith("/") || candidate.startsWith("\\")) {
                candidate = candidate.substring(1);
            }
            int lastSlash = Math.max(candidate.lastIndexOf('/'), candidate.lastIndexOf('\\'));
            if (lastSlash >= 0 && lastSlash < candidate.length() - 1) {
                candidate = candidate.substring(lastSlash + 1).trim();
            }
            candidate = candidate.replace("\"", "").trim();
        }
        if (candidate.isBlank()) {
            candidate = cleanStructuredDescription(description, "");
        }
        return sanitizeVariableToken(candidate);
    }

    private static String cleanStructuredDescription(String description, String fallback) {
        String candidate = description != null ? description.trim() : "";
        if (!candidate.isBlank()) {
            candidate = candidate.replaceFirst("^\\[[^\\]]+\\]\\s*", "").trim();
            candidate = candidate.replaceFirst("\\s*\\([^\\)]*\\)\\s*$", "").trim();
        }
        if (candidate.isBlank()) {
            candidate = fallback != null ? fallback.trim() : "";
        }
        return candidate;
    }

    private static String sanitizeVariableToken(String value) {
        if (value == null || value.isBlank()) {
            return "variable";
        }
        String candidate = value.trim();
        candidate = candidate.replaceAll("^[\\._\\-\\s]+", "");
        candidate = candidate.replaceAll("[\\._\\-\\s]+$", "");
        candidate = candidate.replace(' ', '_');
        return candidate.isBlank() ? "variable" : candidate;
    }

    private static String stripKnownExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "dataset_pro";
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        String[] extensions = {
                SIDECAR_EXTENSION,
                ".tif", ".tiff", ".img", ".asc", ".adf", ".grd", ".bil", ".flt",
                ".jpg", ".jpeg", ".png", ".bmp", ".gif",
                ".nc", ".nc4", ".hdf", ".h5"
        };
        for (String extension : extensions) {
            if (lower.endsWith(extension)) {
                return fileName.substring(0, fileName.length() - extension.length());
            }
        }
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }

    private static String runGdalInfo(File file, List<String> warnings) {
        if (file == null || !file.exists()) {
            return "";
        }
        String executable = GdalSupport.resolve("gdalinfo.exe");
        Process process = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(executable, "-mdd", "all", file.getAbsolutePath());
            builder.redirectErrorStream(true);
            process = builder.start();
            String output;
            try (InputStream inputStream = process.getInputStream()) {
                output = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
            int code = process.waitFor();
            if (code != 0) {
                warnings.add("gdalinfo devolvio codigo " + code + " para " + file.getName() + ".");
            }
            return output;
        } catch (Exception ex) {
            warnings.add("No se pudo ejecutar gdalinfo sobre " + file.getName() + ": " + ex.getMessage());
            return "";
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static ProDatasetDescriptor copyDataset(ProDatasetDescriptor source) {
        ProDatasetDescriptor copy = new ProDatasetDescriptor();
        if (source == null) {
            return copy;
        }
        copy.setDatasetId(source.getDatasetId());
        copy.setFamily(source.getFamily());
        copy.setProvider(source.getProvider());
        copy.setPlatform(source.getPlatform());
        copy.setInstrument(source.getInstrument());
        copy.setProcessingLevel(source.getProcessingLevel());
        copy.setAcquisitionStart(source.getAcquisitionStart());
        copy.setAcquisitionEnd(source.getAcquisitionEnd());
        return copy;
    }

    private static ProVariableDescriptor copyVariable(ProVariableDescriptor source) {
        ProVariableDescriptor copy = new ProVariableDescriptor();
        if (source == null) {
            return copy;
        }
        copy.setName(source.getName());
        copy.setLongName(source.getLongName());
        copy.setStandardName(source.getStandardName());
        copy.setUnits(source.getUnits());
        copy.setDimensions(source.getDimensions());
        copy.setNodata(source.getNodata());
        copy.setScaleFactor(source.getScaleFactor());
        copy.setAddOffset(source.getAddOffset());
        copy.setValidMin(source.getValidMin());
        copy.setValidMax(source.getValidMax());
        copy.setQaDescriptor(source.getQaDescriptor());
        copy.setBandFamily(source.getBandFamily());
        return copy;
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        return fallback != null ? fallback.trim() : "";
    }

    private static String firstNonBlank(String first, String second, String third) {
        return firstNonBlank(firstNonBlank(first, second), third);
    }

    private static List<String> firstNonEmpty(List<String> primary, List<String> fallback) {
        if (primary != null && !primary.isEmpty()) {
            return List.copyOf(primary);
        }
        return fallback != null && !fallback.isEmpty() ? List.copyOf(fallback) : List.of();
    }

    private static List<String> firstNonEmpty(List<String> first, List<String> second, List<String> third) {
        return firstNonEmpty(firstNonEmpty(first, second), third);
    }

    private static String normalizeToken(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private static boolean isDirectRasterSource(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        String lowerName = file.getName().toLowerCase(Locale.ROOT);
        return lowerName.endsWith(".tif")
                || lowerName.endsWith(".tiff")
                || lowerName.endsWith(".img")
                || lowerName.endsWith(".asc")
                || lowerName.endsWith(".adf")
                || lowerName.endsWith(".grd")
                || lowerName.endsWith(".bil")
                || lowerName.endsWith(".flt")
                || lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".png")
                || lowerName.endsWith(".bmp")
                || lowerName.endsWith(".gif");
    }

    private static boolean isStructuredSource(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        String lowerName = file.getName().toLowerCase(Locale.ROOT);
        return lowerName.endsWith(".nc")
                || lowerName.endsWith(".nc4")
                || lowerName.endsWith(".hdf")
                || lowerName.endsWith(".h5");
    }

    private static boolean isSidecarFile(File file) {
        return file != null && file.isFile() && file.getName().toLowerCase(Locale.ROOT).endsWith(SIDECAR_EXTENSION);
    }

    private static boolean isDuplicateArtifact(File file) {
        if (file == null || !file.isFile() || file.getParentFile() == null) {
            return false;
        }
        String normalizedName = ProLandsatL2Support.normalizeDuplicateArtifactName(file.getName());
        if (normalizedName.equals(file.getName())) {
            return false;
        }
        File canonical = new File(file.getParentFile(), normalizedName);
        return canonical.exists() && canonical.isFile();
    }

    private static int parseIndex(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception ex) {
            return Integer.MAX_VALUE;
        }
    }

    record Catalog(File target, List<Entry> entries, List<String> warnings) {

        String summaryText() {
            StringBuilder summary = new StringBuilder();
            summary.append("Fuente: ").append(target != null ? target.getAbsolutePath() : "-");
            String datasetId = primaryDatasetId();
            if (!datasetId.isBlank()) {
                summary.append("\nDataset: ").append(datasetId);
            }
            summary.append("\nVariables detectadas: ").append(entries.size());
            summary.append("\nListas para abrir hoy: ").append(openableCount());
            if (pendingCount() > 0) {
                summary.append("\nPendientes de backend Pro: ").append(pendingCount());
            }
            if (hasGdalBlockedEntries()) {
                summary.append("\nBackend GDAL: no disponible. Los productos NetCDF/HDF quedaron catalogados, pero no podran abrirse hasta contar con gdal_translate.");
            }
            if (!warnings.isEmpty()) {
                summary.append("\nAvisos: ").append(String.join(" | ", warnings));
            }
            return summary.toString();
        }

        int openableCount() {
            int count = 0;
            for (Entry entry : entries) {
                if (entry != null && entry.openable()) {
                    count++;
                }
            }
            return count;
        }

        int pendingCount() {
            return Math.max(0, entries.size() - openableCount());
        }

        boolean hasGdalBlockedEntries() {
            for (Entry entry : entries) {
                if (entry != null && entry.blockedByGdal()) {
                    return true;
                }
            }
            return false;
        }

        String primaryDatasetId() {
            String first = "";
            for (Entry entry : entries) {
                if (entry == null || entry.dataset() == null || entry.dataset().getDatasetId().isBlank()) {
                    continue;
                }
                if (first.isBlank()) {
                    first = entry.dataset().getDatasetId();
                    continue;
                }
                if (!first.equalsIgnoreCase(entry.dataset().getDatasetId())) {
                    return "Multiple";
                }
            }
            return first;
        }
    }

    record Entry(File sourceFile,
                 File rasterFile,
                 File sidecarFile,
                 ProDatasetDescriptor dataset,
                 ProVariableDescriptor variable,
                 String qualityPreset,
                 List<String> flagsApplied,
                 String recipe,
                 String maturity,
                 String gdalSubdatasetName,
                 String gdalSubdatasetDescription,
                 boolean openable,
                 String availabilityNote) {

        String variableLabel() {
            if (variable != null && variable.getName() != null && !variable.getName().isBlank()) {
                return variable.getName();
            }
            if (sourceFile != null) {
                return stripKnownExtension(sourceFile.getName());
            }
            return "variable";
        }

        String descriptionLabel() {
            if (variable != null && variable.getLongName() != null && !variable.getLongName().isBlank()) {
                return variable.getLongName();
            }
            if (gdalSubdatasetDescription != null && !gdalSubdatasetDescription.isBlank()) {
                return cleanStructuredDescription(gdalSubdatasetDescription, variableLabel());
            }
            return variableLabel();
        }

        String acquisitionLabel() {
            return dataset != null && dataset.getAcquisitionStart() != null && !dataset.getAcquisitionStart().isBlank()
                    ? dataset.getAcquisitionStart()
                    : "-";
        }

        String maturityLabel() {
            if (maturity != null && !maturity.isBlank()) {
                return maturity;
            }
            return dataset != null && dataset.getProcessingLevel() != null && !dataset.getProcessingLevel().isBlank()
                    ? dataset.getProcessingLevel()
                    : "-";
        }

        String sourceLabel() {
            if (sourceFile == null) {
                return "-";
            }
            if (gdalSubdatasetName != null && !gdalSubdatasetName.isBlank()) {
                return sourceFile.getName() + " | subdataset";
            }
            return sourceFile.getName();
        }

        String statusLabel() {
            if (openable) {
                if (requiresMaterialization()) {
                    return "Lista (materializa)";
                }
                return "Lista";
            }
            return availabilityNote != null && !availabilityNote.isBlank() ? availabilityNote : "Catalogo";
        }

        boolean requiresMaterialization() {
            return rasterFile == null && gdalSubdatasetName != null && !gdalSubdatasetName.isBlank();
        }

        boolean blockedByGdal() {
            return !openable
                    && availabilityNote != null
                    && availabilityNote.toLowerCase(Locale.ROOT).contains("gdal");
        }

        String layerName() {
            String label = descriptionLabel();
            if (label == null || label.isBlank()) {
                label = variableLabel();
            }
            String acquisition = acquisitionLabel();
            if (!"-".equals(acquisition) && !label.contains(acquisition)) {
                return label + " | " + acquisition;
            }
            return label;
        }

        String datasetRef() {
            return dataset != null ? dataset.getDatasetId() : "";
        }

        String presetLabel() {
            return ProOceanColorPresetSupport.resolve(variable, qualityPreset, flagsApplied, recipe, maturity).presetLabel();
        }

        String methodologyLabel() {
            return ProOceanColorPresetSupport.methodologyLabel(maturity);
        }

        String methodologyDescription() {
            return ProOceanColorPresetSupport.methodologyDescription(maturity);
        }

        String sortKey() {
            return datasetRef() + "|" + variableLabel() + "|" + acquisitionLabel() + "|" + sourceLabel();
        }
    }

    record GdalSubdataset(int index, String name, String description) {
    }
}
