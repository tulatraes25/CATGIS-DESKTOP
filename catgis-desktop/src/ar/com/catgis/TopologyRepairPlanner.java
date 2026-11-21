package ar.com.catgis;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * T5/#113: non-destructive repair proposals for topology results.
 *
 * <p>This planner never edits geometries or project data. It turns validation
 * findings into reversible, human-reviewable proposals that can be exported
 * atomically (CSV/JSON) before any manual repair.</p>
 */
public final class TopologyRepairPlanner {

    public record Proposal(String severity, String rule, List<String> featureIds,
                           String action, String rationale) {
        public Proposal {
            severity = severity == null ? "Advertencia" : severity;
            rule = rule == null ? "" : rule;
            featureIds = featureIds == null ? List.of() : List.copyOf(featureIds);
            action = action == null ? "" : action;
            rationale = rationale == null ? "" : rationale;
        }
    }

    private TopologyRepairPlanner() {}

    public static List<Proposal> plan(List<TopologyCheckResult> results) {
        List<Proposal> proposals = new ArrayList<>();
        if (results == null) {
            return proposals;
        }
        for (TopologyCheckResult result : results) {
            if (result == null) {
                continue;
            }
            proposals.add(toProposal(result));
        }
        return proposals;
    }

    public static void export(List<Proposal> proposals, File file, boolean json)
            throws IOException {
        if (file == null) {
            throw new IOException("Archivo de propuestas requerido.");
        }
        String content = json ? toJson(proposals) : toCsv(proposals);
        Path target = file.toPath().toAbsolutePath().normalize();
        Path parent = target.getParent();
        if (parent == null || !Files.isDirectory(parent)) {
            throw new IOException("El directorio de destino no existe.");
        }
        Path staged = Files.createTempFile(parent, ".catgis-repair-", ".tmp");
        try {
            Files.writeString(staged, content, StandardCharsets.UTF_8);
            try {
                Files.move(staged, target, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException unsupported) {
                throw new IOException("El filesystem no soporta publicacion atomica de propuestas.",
                        unsupported);
            }
        } finally {
            Files.deleteIfExists(staged);
        }
    }

    static String toCsv(List<Proposal> proposals) {
        StringBuilder builder = new StringBuilder("severidad,regla,entidades,accion,justificacion\n");
        if (proposals != null) {
            for (Proposal proposal : proposals) {
                builder.append(csv(proposal.severity())).append(',')
                        .append(csv(proposal.rule())).append(',')
                        .append(csv(String.join("|", proposal.featureIds()))).append(',')
                        .append(csv(proposal.action())).append(',')
                        .append(csv(proposal.rationale())).append('\n');
            }
        }
        return builder.toString();
    }

    static String toJson(List<Proposal> proposals) {
        StringBuilder builder = new StringBuilder("{\"version\":1,\"proposals\":[");
        boolean first = true;
        if (proposals != null) {
            for (Proposal proposal : proposals) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                builder.append("{\"severity\":").append(json(proposal.severity()))
                        .append(",\"rule\":").append(json(proposal.rule()))
                        .append(",\"featureIds\":[");
                boolean firstId = true;
                for (String id : proposal.featureIds()) {
                    if (!firstId) {
                        builder.append(',');
                    }
                    firstId = false;
                    builder.append(json(id));
                }
                builder.append("],\"action\":").append(json(proposal.action()))
                        .append(",\"rationale\":").append(json(proposal.rationale()))
                        .append('}');
            }
        }
        return builder.append("]}\n").toString();
    }

    private static Proposal toProposal(TopologyCheckResult result) {
        String type = result.getCheckType();
        String normalized = type.toLowerCase(Locale.ROOT);
        String action;
        if (normalized.contains("duplic")) {
            action = "Conservar una entidad y marcar la copia para eliminacion (revisar antes)";
        } else if (normalized.contains("superpos") || normalized.contains("overlap")) {
            action = "Recortar o dividir la superposicion y revisar el resultado";
        } else if (normalized.contains("hueco") || normalized.contains("cobertura")) {
            action = "Crear poligono de relleno o ampliar la cobertura del AOI";
        } else if (normalized.contains("invalida") || normalized.contains("interseccion")
                || normalized.contains("degenerada")) {
            action = "Ejecutar buffer(0)/validacion y revisar la geometria corregida";
        } else if (normalized.contains("colgante") || normalized.contains("overshoot")
                || normalized.contains("undershoot") || normalized.contains("near")) {
            action = "Snap al extremo mas cercano con la tolerancia configurada";
        } else if (normalized.contains("multiparte")) {
            action = "Separar la multiparte o justificar su uso";
        } else {
            action = "Revisar manualmente la incidencia";
        }
        return new Proposal(result.getSeverity(), type, result.getFeatureIds(),
                action, result.getDetail());
    }

    private static String csv(String value) {
        String text = value == null ? "" : value;
        if (text.contains(",") || text.contains("\"") || text.contains("\n")
                || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private static String json(String value) {
        String text = value == null ? "" : value;
        StringBuilder out = new StringBuilder("\"");
        for (char ch : text.toCharArray()) {
            switch (ch) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        out.append(String.format(Locale.ROOT, "\\u%04x", (int) ch));
                    } else {
                        out.append(ch);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
