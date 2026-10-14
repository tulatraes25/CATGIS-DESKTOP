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

        // A caller-supplied ToolInfo must not introduce unreviewed
        // arbitrary command IDs or parameter names into the argv.
        WhiteboxToolRegistry.ToolInfo reviewed = WhiteboxToolRegistry.getTools()
                .stream().filter(candidate -> candidate.id().equals(tool.id()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "La herramienta no pertenece al catalogo permitido."));
        List<String> command = new ArrayList<>();
        command.add(executable);
        command.add("--run=" + reviewed.id());

        Map<String, String> values = inputValues != null ? inputValues : Map.of();
        for (WhiteboxToolRegistry.ParamInfo param : reviewed.params()) {
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
