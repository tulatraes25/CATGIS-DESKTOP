package ar.com.catgis;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ProMetadataSidecarSupport {

    private static final int VERSION = 1;
    private static final String EXTENSION = ".catgis-pro.json";

    private ProMetadataSidecarSupport() {
    }

    static File write(File dataFile, Metadata metadata) throws IOException {
        if (dataFile == null || metadata == null) {
            throw new IOException("No se pudo escribir el sidecar Pro: faltan datos de entrada.");
        }
        File sidecar = sidecarFile(dataFile);
        File parent = sidecar.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
            throw new IOException("No se pudo crear la carpeta del sidecar Pro.");
        }

        Files.writeString(sidecar.toPath(), encode(metadata, sidecar), StandardCharsets.UTF_8);
        return sidecar;
    }

    static Metadata read(File dataFile) {
        return dataFile != null ? readSidecar(sidecarFile(dataFile)) : null;
    }

    static Metadata readSidecar(File sidecar) {
        if (sidecar == null || !sidecar.exists() || !sidecar.isFile()) {
            return null;
        }
        try {
            String json = Files.readString(sidecar.toPath(), StandardCharsets.UTF_8);
            int version = parseIntField(json, "version", -1);
            if (version != VERSION) {
                return null;
            }

            ProDatasetDescriptor dataset = new ProDatasetDescriptor();
            dataset.setDatasetId(readStringField(json, "datasetId"));
            dataset.setFamily(readStringField(json, "family"));
            dataset.setProvider(readStringField(json, "provider"));
            dataset.setPlatform(readStringField(json, "platform"));
            dataset.setInstrument(readStringField(json, "instrument"));
            dataset.setProcessingLevel(readStringField(json, "processingLevel"));
            dataset.setAcquisitionStart(readStringField(json, "acquisitionStart"));
            dataset.setAcquisitionEnd(readStringField(json, "acquisitionEnd"));

            ProVariableDescriptor variable = new ProVariableDescriptor();
            variable.setName(readStringField(json, "name"));
            variable.setLongName(readStringField(json, "longName"));
            variable.setStandardName(readStringField(json, "standardName"));
            variable.setUnits(readStringField(json, "units"));
            variable.setDimensions(readStringArray(json, "dimensions"));
            variable.setNodata(readDoubleField(json, "nodata"));
            variable.setScaleFactor(readDoubleField(json, "scaleFactor"));
            variable.setAddOffset(readDoubleField(json, "addOffset"));
            variable.setValidMin(readDoubleField(json, "validMin"));
            variable.setValidMax(readDoubleField(json, "validMax"));
            variable.setQaDescriptor(readStringField(json, "qaDescriptor"));
            variable.setBandFamily(readStringField(json, "bandFamily"));

            return new Metadata(
                    dataset,
                    variable,
                    readStringField(json, "preset"),
                    readStringArray(json, "flagsApplied"),
                    readStringField(json, "recipe"),
                    readStringField(json, "maturity"),
                    sidecar
            );
        } catch (Exception ex) {
            return null;
        }
    }

    static File sidecarFile(File dataFile) {
        return new File(dataFile.getAbsolutePath() + EXTENSION);
    }

    private static String encode(Metadata metadata, File sidecar) {
        ProDatasetDescriptor dataset = metadata.dataset() != null ? metadata.dataset() : new ProDatasetDescriptor();
        ProVariableDescriptor variable = metadata.variable() != null ? metadata.variable() : new ProVariableDescriptor();
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"version\": ").append(VERSION).append(",\n");
        json.append("  \"datasetId\": \"").append(escapeJson(dataset.getDatasetId())).append("\",\n");
        json.append("  \"source\": {\n");
        json.append("    \"family\": \"").append(escapeJson(dataset.getFamily())).append("\",\n");
        json.append("    \"provider\": \"").append(escapeJson(dataset.getProvider())).append("\",\n");
        json.append("    \"platform\": \"").append(escapeJson(dataset.getPlatform())).append("\",\n");
        json.append("    \"instrument\": \"").append(escapeJson(dataset.getInstrument())).append("\",\n");
        json.append("    \"processingLevel\": \"").append(escapeJson(dataset.getProcessingLevel())).append("\",\n");
        json.append("    \"acquisitionStart\": \"").append(escapeJson(dataset.getAcquisitionStart())).append("\",\n");
        json.append("    \"acquisitionEnd\": \"").append(escapeJson(dataset.getAcquisitionEnd())).append("\"\n");
        json.append("  },\n");
        json.append("  \"variable\": {\n");
        json.append("    \"name\": \"").append(escapeJson(variable.getName())).append("\",\n");
        json.append("    \"longName\": \"").append(escapeJson(variable.getLongName())).append("\",\n");
        json.append("    \"standardName\": \"").append(escapeJson(variable.getStandardName())).append("\",\n");
        json.append("    \"units\": \"").append(escapeJson(variable.getUnits())).append("\",\n");
        json.append("    \"dimensions\": ").append(encodeStringArray(variable.getDimensions())).append(",\n");
        json.append("    \"nodata\": ").append(encodeDouble(variable.getNodata())).append(",\n");
        json.append("    \"scaleFactor\": ").append(encodeDouble(variable.getScaleFactor())).append(",\n");
        json.append("    \"addOffset\": ").append(encodeDouble(variable.getAddOffset())).append(",\n");
        json.append("    \"validMin\": ").append(encodeDouble(variable.getValidMin())).append(",\n");
        json.append("    \"validMax\": ").append(encodeDouble(variable.getValidMax())).append(",\n");
        json.append("    \"qaDescriptor\": \"").append(escapeJson(variable.getQaDescriptor())).append("\",\n");
        json.append("    \"bandFamily\": \"").append(escapeJson(variable.getBandFamily())).append("\"\n");
        json.append("  },\n");
        json.append("  \"quality\": {\n");
        json.append("    \"preset\": \"").append(escapeJson(metadata.qualityPreset())).append("\",\n");
        json.append("    \"flagsApplied\": ").append(encodeStringArray(metadata.flagsApplied())).append("\n");
        json.append("  },\n");
        json.append("  \"lineage\": {\n");
        json.append("    \"recipe\": \"").append(escapeJson(metadata.recipe())).append("\",\n");
        json.append("    \"maturity\": \"").append(escapeJson(metadata.maturity())).append("\"\n");
        json.append("  }\n");
        json.append("}\n");
        return json.toString();
    }

    private static String encodeStringArray(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }
        StringBuilder encoded = new StringBuilder("[");
        boolean first = true;
        for (String value : values) {
            if (!first) {
                encoded.append(", ");
            }
            encoded.append("\"").append(escapeJson(value)).append("\"");
            first = false;
        }
        encoded.append("]");
        return encoded.toString();
    }

    private static String encodeDouble(Double value) {
        if (value == null || value.isNaN() || value.isInfinite()) {
            return "null";
        }
        return Double.toString(value);
    }

    private static String readStringField(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\\\\\"])*)\"", Pattern.DOTALL)
                .matcher(json);
        return matcher.find() ? unescapeJson(matcher.group(1)) : "";
    }

    private static int parseIntField(String json, String key, int defaultValue) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        if (!matcher.find()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private static Double readDoubleField(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(null|-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?)")
                .matcher(json);
        if (!matcher.find()) {
            return null;
        }
        String raw = matcher.group(1);
        if (raw == null || "null".equalsIgnoreCase(raw.trim())) {
            return null;
        }
        try {
            return Double.parseDouble(raw);
        } catch (Exception ex) {
            return null;
        }
    }

    private static List<String> readStringArray(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL)
                .matcher(json);
        if (!matcher.find()) {
            return List.of();
        }
        Matcher itemMatcher = Pattern.compile("\"((?:\\\\.|[^\\\\\"])*)\"").matcher(matcher.group(1));
        List<String> values = new ArrayList<>();
        while (itemMatcher.find()) {
            values.add(unescapeJson(itemMatcher.group(1)));
        }
        return values;
    }

    private static String escapeJson(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private static String unescapeJson(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }

    static final class Metadata {
        private final ProDatasetDescriptor dataset;
        private final ProVariableDescriptor variable;
        private final String qualityPreset;
        private final List<String> flagsApplied;
        private final String recipe;
        private final String maturity;
        private final File sidecarFile;

        Metadata(ProDatasetDescriptor dataset,
                 ProVariableDescriptor variable,
                 String qualityPreset,
                 List<String> flagsApplied,
                 String recipe,
                 String maturity,
                 File sidecarFile) {
            this.dataset = dataset;
            this.variable = variable;
            this.qualityPreset = qualityPreset != null ? qualityPreset.trim() : "";
            this.flagsApplied = flagsApplied != null ? List.copyOf(flagsApplied) : List.of();
            this.recipe = recipe != null ? recipe.trim() : "";
            this.maturity = maturity != null ? maturity.trim() : "";
            this.sidecarFile = sidecarFile;
        }

        ProDatasetDescriptor dataset() {
            return dataset;
        }

        ProVariableDescriptor variable() {
            return variable;
        }

        String qualityPreset() {
            return qualityPreset;
        }

        List<String> flagsApplied() {
            return flagsApplied;
        }

        String recipe() {
            return recipe;
        }

        String maturity() {
            return maturity;
        }

        File sidecarFile() {
            return sidecarFile;
        }
    }
}
