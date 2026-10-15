package ar.com.catgis;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Exact recipe values, with a fixed JSON schema; no execution or file access. */
final class BatchTemplateCodec {
    static final int MAX_JSON_CHARS = 256 * 1024;
    static final int MAX_TEXT_CHARS = 16 * 1024;
    static final int MAX_KEY_CHARS = 1024;
    static final int MAX_SETTINGS = 256;

    private static final Set<String> FIELDS = Set.of(
            "name", "description", "operation", "extensionFilter", "includeSubdirs", "settings");
    private static final ObjectMapper JSON = new ObjectMapper(JsonFactory.builder()
            .streamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(8)
                    .maxStringLength(MAX_TEXT_CHARS).maxNumberLength(32).build())
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build())
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    private BatchTemplateCodec() {}

    static String encode(BatchProcessor.BatchTemplate template) {
        if (template == null) { throw invalid("Falta la plantilla de procesos."); }
        String name = textOrEmpty(template.name());
        String description = textOrEmpty(template.description());
        String operation = textOrEmpty(template.operation());
        String extension = textOrEmpty(template.extensionFilter());
        Map<String, String> settings = snapshot(template.settings());
        long contentChars = (long) name.length() + description.length() + operation.length() + extension.length();

        ObjectNode root = JSON.createObjectNode();
        root.put("name", name);
        root.put("description", description);
        root.put("operation", operation);
        root.put("extensionFilter", extension);
        root.put("includeSubdirs", template.includeSubdirs());
        ObjectNode values = root.putObject("settings");
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String key = checkKey(entry.getKey());
            // Existing programmatic null values were serialized as empty text.
            // Preserve that default; explicit non-text JSON values are rejected.
            String value = textOrEmpty(entry.getValue());
            contentChars += (long) key.length() + value.length();
            if (contentChars > MAX_JSON_CHARS) { throw invalid("La plantilla excede el limite de texto."); }
            values.put(key, value);
        }
        try {
            String json = JSON.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            if (json.length() > MAX_JSON_CHARS) { throw invalid("El JSON de la plantilla excede 256 Ki caracteres."); }
            return json;
        } catch (JsonProcessingException failure) {
            throw new IllegalArgumentException("No se pudo serializar la plantilla de procesos.", failure);
        }
    }

    static BatchProcessor.BatchTemplate decode(String json) {
        if (json == null || json.length() > MAX_JSON_CHARS || json.isBlank()) {
            throw invalid("El JSON de la plantilla falta o excede 256 Ki caracteres.");
        }
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (JsonProcessingException failure) {
            throw new IllegalArgumentException("JSON de plantilla invalido; no se cargaron parametros parciales.", failure);
        }
        if (root == null || !root.isObject()) { throw invalid("La plantilla debe ser un objeto JSON."); }
        var fields = root.fieldNames();
        while (fields.hasNext()) {
            if (!FIELDS.contains(fields.next())) {
                throw invalid("Campo desconocido en la plantilla; no se descartaron parametros silenciosamente.");
            }
        }
        String name = requiredText(root.get("name"));
        String operation = requiredText(root.get("operation"));
        String description = optionalText(root.get("description"));
        String extension = optionalText(root.get("extensionFilter"));
        JsonNode subdirs = root.get("includeSubdirs");
        if (subdirs != null && !subdirs.isBoolean()) {
            throw invalid("includeSubdirs debe ser un booleano JSON.");
        }
        Map<String, String> settings = new LinkedHashMap<>();
        JsonNode values = root.get("settings");
        if (values != null && !values.isNull()) {
            if (!values.isObject()) { throw invalid("settings debe ser un objeto de parametros textuales."); }
            checkSettingsCount(values.size());
            var entries = values.fields();
            while (entries.hasNext()) {
                var entry = entries.next();
                settings.put(checkKey(entry.getKey()), requiredText(entry.getValue()));
            }
        }
        // New independent map per decode; keep the old public record's map
        // mutability instead of silently changing its caller contract here.
        return new BatchProcessor.BatchTemplate(name, description, operation, extension,
                subdirs != null && subdirs.booleanValue(), settings);
    }

    private static Map<String, String> snapshot(Map<String, String> source) {
        // Bound allocation before copying, and enforce the count again while
        // iterating: a weakly consistent/custom map may report a stale size.
        checkSettingsCount(source.size());
        Map<String, String> copy = new LinkedHashMap<>();
        int visited = 0;
        for (Map.Entry<String, String> entry : source.entrySet()) {
            checkSettingsCount(++visited);
            copy.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    private static String requiredText(JsonNode value) {
        if (value == null || !value.isTextual()) {
            throw invalid("Campo obligatorio ausente o no textual en la plantilla.");
        }
        return textOrEmpty(value.textValue());
    }

    private static String optionalText(JsonNode value) {
        return value == null || value.isNull() ? "" : requiredText(value);
    }

    private static String textOrEmpty(String value) {
        if (value == null) { return ""; }
        if (value.length() > MAX_TEXT_CHARS) { throw invalid("Un texto de la plantilla excede 16 Ki caracteres."); }
        return value;
    }

    private static String checkKey(String key) {
        if (key == null || key.length() > MAX_KEY_CHARS) {
            throw invalid("Nombre de parametro nulo o superior a 1024 caracteres.");
        }
        return key;
    }

    private static void checkSettingsCount(int count) {
        if (count > MAX_SETTINGS) { throw invalid("La plantilla excede 256 parametros."); }
    }

    private static IllegalArgumentException invalid(String message) {
        return new IllegalArgumentException(message);
    }
}
