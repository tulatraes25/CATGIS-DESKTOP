package ar.com.catgis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Exact values and actual UTF-8 persistence, not substring-only success. */
class BatchTemplateCodecContractTest {
    @TempDir Path temporary;

    @Test
    void literalWindowsUncPathsAndEscapeLookingTextSurviveExactly() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("source", "C:\\new\\roads\\north.tif");
        values.put("unc", "\\\\server\\share\\raw\\new\\");
        values.put("expression", "\\n|\\r|\\t|\\u1234|\\\"");
        var original = recipe("Windows", values);
        assertEquals(original, roundtrip(original));
        assertEquals(values, roundtrip(original).settings());
    }

    @Test
    void bracesQuotesAndUnicodeInKeysAndValuesCannotTruncateSettings() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("r\u00edo\\\"}", "before } after { \"value\"");
        values.put("last", "must survive the earlier closing brace");
        var original = new BatchProcessor.BatchTemplate("Regi\u00f3n \uD83D\uDDFA", "\u00c1rea\nSur",
                "copy", ".tif", false, values);
        assertEquals(original, roundtrip(original));
        assertEquals(List.copyOf(values.keySet()), new ArrayList<>(roundtrip(original).settings().keySet()));
    }

    @Test
    void booleanMeaningIsIndependentOfWhitespaceOrderAndTextValues() throws Exception {
        String compact = "{\"name\":\"r\",\"operation\":\"copy\",\"includeSubdirs\":true,\"settings\":{}}";
        assertTrue(decode(compact).includeSubdirs());
        assertTrue(decode("{\"includeSubdirs\" : \n true,\"operation\":\"copy\",\"name\":\"r\"}").includeSubdirs());
        var tree = new ObjectMapper().readTree(compact);
        ((com.fasterxml.jackson.databind.node.ObjectNode) tree).put("includeSubdirs", false);
        ((com.fasterxml.jackson.databind.node.ObjectNode) tree).put("description", "\"includeSubdirs\": true");
        assertFalse(decode(tree.toString()).includeSubdirs());
    }

    @Test
    void independentlyWrittenUnicodeEscapesInPropertyNamesAndValuesDecodeCorrectly() {
        String json = "{\"na\\u006de\":\"Regi\\u00f3n\",\"operation\":\"copy\","
                + "\"settings\":{\"r\\u00edo\":\"x\\u0041\\tZ\"}}";
        var result = decode(json);
        assertEquals("Regi\u00f3n", result.name());
        assertEquals("xA\tZ", result.settings().get("r\u00edo"));
    }

    @Test
    void allJsonControlCharactersAreEscapedAndRestoredWithoutLoss() throws Exception {
        StringBuilder controls = new StringBuilder();
        for (int i = 0; i < 32; i++) { controls.append((char) i); }
        String exact = controls + "quotes\" slash\\ \u00f1 \uD83D\uDDFA";
        var original = new BatchProcessor.BatchTemplate("controls", exact, "copy", "", true, Map.of(exact, exact));
        String encoded = original.toJson();
        var parsed = new ObjectMapper().readTree(encoded);
        assertEquals(exact, parsed.get("description").textValue());
        assertEquals(exact, parsed.get("settings").get(exact).textValue());
        assertEquals(original, decode(encoded));
    }

    @Test
    void oldPrettyPrintedTemplateSchemaRetainsEveryField() {
        String legacy = "{\n  \"name\": \"NDVI Batch\",\n  \"description\": \"Compute NDVI\",\n"
                + "  \"operation\": \"spectral_index\",\n  \"extensionFilter\": \".tif\",\n"
                + "  \"includeSubdirs\": true,\n  \"settings\": {\n"
                + "    \"indexId\": \"ndvi\",\n    \"bandA\": \"3\",\n    \"bandB\": \"4\"\n  }\n}";
        var expected = new BatchProcessor.BatchTemplate("NDVI Batch", "Compute NDVI", "spectral_index",
                ".tif", true, Map.of("indexId", "ndvi", "bandA", "3", "bandB", "4"));
        assertEquals(expected, decode(legacy));
        assertEquals(expected, roundtrip(decode(legacy)));
    }

    @Test
    void documentedOptionalDefaultsAndProgrammaticNullValuesRemainCompatible() {
        var original = new BatchProcessor.BatchTemplate("Minimal", null, "convert", null, false, null);
        var expected = new BatchProcessor.BatchTemplate("Minimal", "", "convert", "", false, Map.of());
        assertEquals(expected, roundtrip(original));
        assertEquals(expected, decode("{\"name\":\"Minimal\",\"operation\":\"convert\"}"));
        assertEquals(expected, decode("{\"name\":\"Minimal\",\"operation\":\"convert\","
                + "\"description\":null,\"extensionFilter\":null,\"settings\":null}"));
        Map<String, String> nullable = new LinkedHashMap<>();
        nullable.put("optional", null);
        assertEquals("", roundtrip(recipe("Null value", nullable)).settings().get("optional"));
        assertNull(nullable.get("optional"), "Encoding must not normalize the original map in place.");
    }

    @Test
    void malformedTruncatedAndMissingRequiredFieldsNeverBecomeAnEmptySuccessfulRecipe() {
        for (String json : List.of("", " ", "not JSON", "null", "[]", "1", "true", "{}",
                "{\"name\":\"r\"}", "{\"operation\":\"copy\"}", "{\"name\":\"r\",",
                "{\"name\":null,\"operation\":\"copy\"}", "{\"name\":42,\"operation\":\"copy\"}")) {
            assertThrows(IllegalArgumentException.class, () -> decode(json), json);
        }
        assertThrows(IllegalArgumentException.class, () -> decode(null));
    }

    @Test
    void duplicateKeysIncludingUnicodeAliasesAndTrailingValuesAreRejected() {
        for (String json : List.of(
                "{\"name\":\"a\",\"name\":\"b\",\"operation\":\"copy\"}",
                "{\"name\":\"a\",\"na\\u006de\":\"b\",\"operation\":\"copy\"}",
                "{\"name\":\"r\",\"operation\":\"copy\",\"settings\":{\"x\":\"1\",\"x\":\"2\"}}",
                minimal() + " {}", minimal() + " null", minimal() + " trailing")) {
            assertThrows(IllegalArgumentException.class, () -> decode(json), json);
        }
    }

    @Test
    void wrongTypesAndUnsupportedFieldsCannotBeSilentlyIgnoredOrCoerced() {
        for (String extra : List.of("\"includeSubdirs\":\"true\"", "\"includeSubdirs\":1",
                "\"includeSubdirs\":null", "\"description\":[]", "\"extensionFilter\":false",
                "\"settings\":[]", "\"settings\":{\"x\":1}", "\"settings\":{\"x\":null}",
                "\"settings\":{\"x\":true}", "\"settings\":{\"x\":{}}",
                "\"unknownOption\":true", "\"@class\":\"java.lang.Runtime\"")) {
            assertThrows(IllegalArgumentException.class, () -> decode(withExtra(extra)), extra);
        }
    }

    @Test
    void textKeySettingCountAndInputBudgetsHaveAcceptedBoundariesAndRejectExcess() throws Exception {
        String maximumText = "x".repeat(BatchTemplateCodec.MAX_TEXT_CHARS);
        String maximumKey = "k".repeat(BatchTemplateCodec.MAX_KEY_CHARS);
        assertEquals(maximumText, roundtrip(recipe("boundary", Map.of(maximumKey, maximumText))).settings().get(maximumKey));
        Map<String, String> settings = new LinkedHashMap<>();
        for (int i = 0; i < BatchTemplateCodec.MAX_SETTINGS; i++) { settings.put("k" + i, "v"); }
        assertEquals(settings, roundtrip(recipe("max count", settings)).settings());
        var tree = new ObjectMapper().readTree(recipe("max count", settings).toJson());
        ((com.fasterxml.jackson.databind.node.ObjectNode) tree.get("settings")).put("extra", "v");
        assertThrows(IllegalArgumentException.class, () -> decode(tree.toString()));
        settings.put("extra", "v");
        assertThrows(IllegalArgumentException.class, () -> recipe("excess count", settings).toJson());
        assertThrows(IllegalArgumentException.class,
                () -> recipe("long value", Map.of("k", maximumText + "x")).toJson());
        assertThrows(IllegalArgumentException.class,
                () -> recipe("long key", Map.of(maximumKey + "k", "v")).toJson());
        assertThrows(IllegalArgumentException.class,
                () -> decode(withExtra("\"description\":\"" + maximumText + "x\"")));
        assertThrows(IllegalArgumentException.class,
                () -> decode(withExtra("\"settings\":{\"" + maximumKey + "k\":\"v\"}")));
        assertThrows(IllegalArgumentException.class, () -> decode(" ".repeat(BatchTemplateCodec.MAX_JSON_CHARS) + minimal()));
        assertThrows(IllegalArgumentException.class,
                () -> decode(withExtra("\"settings\":{\"x\":" + "[".repeat(20) + "0" + "]".repeat(20) + "}")));
    }

    @Test
    void writerRejectsEscapedOutputOverBudgetAndAmbiguousNullParameterKeys() {
        Map<String, String> controls = new LinkedHashMap<>();
        for (int i = 0; i < 3; i++) { controls.put("control" + i, String.valueOf((char) 0).repeat(BatchTemplateCodec.MAX_TEXT_CHARS)); }
        assertThrows(IllegalArgumentException.class, () -> recipe("escaped size", controls).toJson());
        Map<String, String> nullKey = new LinkedHashMap<>();
        nullKey.put(null, "one");
        nullKey.put("", "another");
        assertThrows(IllegalArgumentException.class, () -> recipe("null key", nullKey).toJson());
        assertEquals(2, nullKey.size());
        assertThrows(IllegalArgumentException.class, () -> BatchTemplateCodec.encode(null));
    }

    @Test
    void encodingAndDecodingDoNotShareOrMutateCallerParameterMaps() {
        Map<String, String> original = new LinkedHashMap<>();
        original.put("first", "1");
        original.put("second", "2");
        String encoded = recipe("ownership", original).toJson();
        var first = decode(encoded);
        var second = decode(encoded);
        first.settings().put("first", "changed");
        original.put("second", "changed at caller");
        assertEquals(Map.of("first", "1", "second", "2"), second.settings());
        assertEquals("2", first.settings().get("second"));
        assertEquals(List.of("first", "second"), new ArrayList<>(second.settings().keySet()));
    }

    @Test
    void realUtf8FileRoundtripKeepsExactParametersAndSourceBytes() throws Exception {
        var original = recipe("Relevamiento \u00f1", Map.of("source", "C:\\new\\roads\\datos \u00f1.tif",
                "formula", "{ value }\nnext\trow", "label", "\uD83D\uDDFA"));
        Path file = temporary.resolve("plantilla con espacios \u00f1.json");
        Files.writeString(file, original.toJson(), StandardCharsets.UTF_8);
        byte[] before = Files.readAllBytes(file);
        for (int i = 0; i < 20; i++) {
            assertEquals(original, decode(Files.readString(file, StandardCharsets.UTF_8)));
        }
        assertArrayEquals(before, Files.readAllBytes(file));
        Path renamed = temporary.resolve("renamed.json");
        Files.move(file, renamed);
        Files.delete(renamed);
    }

    @Test
    void concurrentIndependentRecipesCannotExchangeParameters() throws Exception {
        var workers = Executors.newFixedThreadPool(4);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                int id = i;
                tasks.add(() -> {
                    var original = recipe("recipe " + id, Map.of("path", "C:\\new\\r" + id, "id", String.valueOf(id)));
                    for (int repeat = 0; repeat < 20; repeat++) {
                        if (!original.equals(roundtrip(original))) { return false; }
                    }
                    return true;
                });
            }
            for (var result : workers.invokeAll(tasks, 20, TimeUnit.SECONDS)) {
                assertFalse(result.isCancelled(), "Concurrent codec test did not complete.");
                assertTrue(result.get());
            }
        } finally {
            workers.shutdownNow();
            assertTrue(workers.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private static BatchProcessor.BatchTemplate recipe(String name, Map<String, String> settings) {
        return new BatchProcessor.BatchTemplate(name, "exact values", "copy", ".tif", true, settings);
    }

    private static BatchProcessor.BatchTemplate roundtrip(BatchProcessor.BatchTemplate template) {
        return decode(template.toJson());
    }

    private static BatchProcessor.BatchTemplate decode(String json) {
        return BatchProcessor.BatchTemplate.fromJson(json);
    }

    private static String minimal() { return "{\"name\":\"r\",\"operation\":\"copy\"}"; }
    private static String withExtra(String extra) {
        return "{\"name\":\"r\",\"operation\":\"copy\"," + extra + "}";
    }
}
