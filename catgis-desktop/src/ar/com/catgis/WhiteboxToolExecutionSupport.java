package ar.com.catgis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds an argv array, never a shell command string. File paths with spaces are
 * preserved as one argument and required inputs cannot be silently discarded.
 */
final class WhiteboxToolExecutionSupport {

    private WhiteboxToolExecutionSupport() {
    }

    static String[] buildCommand(String executable,
                                 WhiteboxToolRegistry.ToolInfo tool,
                                 Map<String, String> inputValues) {
        if (executable == null || executable.isBlank()) {
            throw new IllegalArgumentException("WhiteboxTools no esta disponible.");
        }
        if (tool == null || tool.id() == null || tool.id().isBlank()) {
            throw new IllegalArgumentException("Selecciona una herramienta valida.");
        }

        List<String> command = new ArrayList<>();
        command.add(executable);
        command.add("--run=" + tool.id());

        Map<String, String> values = inputValues != null ? inputValues : Map.of();
        for (WhiteboxToolRegistry.ParamInfo param : tool.params()) {
            String rawValue = values.get(param.name());
            String value = rawValue != null ? rawValue.trim() : "";
            if (param.required() && value.isEmpty()) {
                throw new IllegalArgumentException("Falta el parametro obligatorio: " + param.label());
            }
            if (!value.isEmpty()) {
                command.add("--" + param.name() + "=" + value);
            }
        }
        return command.toArray(new String[0]);
    }
}
