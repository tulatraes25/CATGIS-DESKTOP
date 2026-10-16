package ar.com.catgis;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Strict parser for batch raster reclassification rules. */
final class RasterReclassRuleParser {
    private static final int MAX_RULE_TEXT = 65_536;
    private static final int MAX_RULES = 4_096;
    private static final int MAX_LABEL_LENGTH = 1_024;
    private static final String NUMBER =
            "[+-]?(?:(?:\\d+(?:\\.\\d*)?)|(?:\\.\\d+))(?:[eE][+-]?\\d+)?";
    private static final Pattern RULE = Pattern.compile(
            "^\\s*(" + NUMBER + ")\\s*-\\s*(" + NUMBER + ")"
                    + "\\s*=\\s*(" + NUMBER + ")"
                    + "(?:\\s*=\\s*(.*))?\\s*$");

    private RasterReclassRuleParser() {}

    static List<RasterReclassifyEngine.ReclassRule> parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Las reglas de reclasificacion estan vacias.");
        }
        if (text.length() > MAX_RULE_TEXT) {
            throw new IllegalArgumentException("Las reglas de reclasificacion exceden el limite.");
        }

        String[] chunks = text.split(";", -1);
        if (chunks.length > MAX_RULES) {
            throw new IllegalArgumentException("Hay demasiadas reglas de reclasificacion.");
        }

        List<RasterReclassifyEngine.ReclassRule> rules = new ArrayList<>(chunks.length);
        for (int index = 0; index < chunks.length; index++) {
            String chunk = chunks[index];
            if (chunk.isBlank()) {
                throw invalid(index, chunk);
            }
            Matcher matcher = RULE.matcher(chunk);
            if (!matcher.matches()) {
                throw invalid(index, chunk);
            }

            double from = parseFinite(matcher.group(1), index);
            double to = parseFinite(matcher.group(2), index);
            double newValue = parseFinite(matcher.group(3), index);
            String label = matcher.group(4) == null ? "" : matcher.group(4).trim();
            if (label.length() > MAX_LABEL_LENGTH) {
                throw new IllegalArgumentException(
                        "La etiqueta de la regla " + (index + 1) + " es demasiado larga.");
            }
            rules.add(new RasterReclassifyEngine.ReclassRule(from, to, newValue, label));
        }
        return List.copyOf(rules);
    }

    private static double parseFinite(String value, int index) {
        double parsed;
        try {
            parsed = Double.parseDouble(value);
        } catch (NumberFormatException malformed) {
            throw new IllegalArgumentException(
                    "Numero invalido en la regla " + (index + 1) + ".", malformed);
        }
        if (!Double.isFinite(parsed)) {
            throw new IllegalArgumentException(
                    "La regla " + (index + 1) + " contiene un valor no finito.");
        }
        return parsed;
    }

    private static IllegalArgumentException invalid(int index, String chunk) {
        return new IllegalArgumentException(
                "Regla de reclasificacion invalida en posicion " + (index + 1)
                        + ": " + chunk);
    }
}
