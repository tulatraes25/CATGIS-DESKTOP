package ar.com.catgis;

import java.util.List;
import java.util.Locale;

public final class ProOceanColorPresetSupport {

    private static final List<Definition> DEFINITIONS = List.of(
            new Definition(
                    "chlor_a_mvp",
                    "Clorofila-a",
                    List.of("chlor_a", "chlorophyll_a", "chlorophyll", "chl", "chl_a"),
                    "ocean_color_chlor_a_mvp",
                    List.of("cloud", "land", "high_glint"),
                    "preliminar"
            ),
            new Definition(
                    "kd490_mvp",
                    "Kd490",
                    List.of("kd490", "kd_490", "diffuse_attenuation_490"),
                    "ocean_color_kd490_mvp",
                    List.of("cloud", "land", "high_glint"),
                    "preliminar"
            ),
            new Definition(
                    "turbidity_tsm_mvp",
                    "Turbidez / TSM",
                    List.of("turbidity", "turbidez", "tsm", "total_suspended_matter", "spm", "suspended_particulate_matter"),
                    "ocean_color_turbidity_tsm_mvp",
                    List.of("cloud", "land", "high_glint", "adjacency"),
                    "exploratorio"
            ),
            new Definition(
                    "landsat_sr_l2sp_mvp",
                    "Reflectancia superficial Landsat",
                    List.of("sr_b1", "sr_b2", "sr_b3", "sr_b4", "sr_b5", "sr_b6", "sr_b7"),
                    "landsat_l2sp_open",
                    List.of("fill", "dilated_cloud", "cirrus", "cloud", "cloud_shadow", "snow", "water"),
                    "preliminar"
            ),
            new Definition(
                    "landsat_st_l2sp_mvp",
                    "Temperatura superficial Landsat",
                    List.of("st_b10"),
                    "landsat_l2sp_open",
                    List.of("fill", "dilated_cloud", "cirrus", "cloud", "cloud_shadow", "snow", "st_qa"),
                    "preliminar"
            ),
            new Definition(
                    "landsat_qapixel_mvp",
                    "QA_PIXEL Landsat",
                    List.of("qa_pixel"),
                    "landsat_l2sp_open",
                    List.of("fill", "dilated_cloud", "cirrus", "cloud", "cloud_shadow", "snow", "water"),
                    "preliminar"
            ),
            new Definition(
                    "landsat_aerosol_qa_mvp",
                    "QA aerosol Landsat",
                    List.of("sr_qa_aerosol"),
                    "landsat_l2sp_open",
                    List.of("fill", "water", "interpolated_aerosol", "aerosol_level"),
                    "preliminar"
            ),
            new Definition(
                    "landsat_st_qa_mvp",
                    "QA temperatura Landsat",
                    List.of("st_qa"),
                    "landsat_l2sp_open",
                    List.of("temperature_uncertainty"),
                    "preliminar"
            ),
            new Definition(
                    "landsat_radsat_qa_mvp",
                    "QA saturacion Landsat",
                    List.of("qa_radsat"),
                    "landsat_l2sp_open",
                    List.of("saturation_bits"),
                    "preliminar"
            )
    );

    private ProOceanColorPresetSupport() {
    }

    static ResolvedLineage resolve(ProVariableDescriptor variable,
                                   String qualityPreset,
                                   List<String> flagsApplied,
                                   String recipe,
                                   String maturity) {
        Definition definition = findDefinition(qualityPreset, variable != null ? variable.getName() : "");
        String presetId = firstNonBlank(qualityPreset, definition != null ? definition.id() : "");
        List<String> effectiveFlags = flagsApplied != null && !flagsApplied.isEmpty()
                ? List.copyOf(flagsApplied)
                : definition != null ? definition.defaultFlags() : List.of();
        String effectiveRecipe = firstNonBlank(recipe, definition != null ? definition.defaultRecipe() : "");
        String effectiveMaturity = normalizeMaturity(firstNonBlank(maturity, definition != null ? definition.defaultMaturity() : ""));
        return new ResolvedLineage(definition, presetId, effectiveFlags, effectiveRecipe, effectiveMaturity);
    }

    static String methodologyLabel(String maturity) {
        return switch (normalizeMaturity(maturity)) {
            case "operativo_mvp" -> "Operativo MVP";
            case "preliminar" -> "Preliminar";
            case "exploratorio" -> "Exploratorio";
            default -> maturity != null && !maturity.isBlank() ? humanizeToken(maturity) : "Sin clasificacion";
        };
    }

    static String methodologyDescription(String maturity) {
        return switch (normalizeMaturity(maturity)) {
            case "operativo_mvp" -> "Operativo MVP: utilizable en flujo interno CATGIS Pro, aun sin validacion cientifica completa.";
            case "preliminar" -> "Preliminar: apto para exploracion tecnica y contraste visual, sujeto a revision tematica.";
            case "exploratorio" -> "Exploratorio: orientado a inspeccion inicial y entrenamiento del flujo, no para decision final.";
            default -> maturity != null && !maturity.isBlank()
                    ? humanizeToken(maturity)
                    : "Sin clasificacion metodologica declarada.";
        };
    }

    static String normalizeMaturity(String maturity) {
        if (maturity == null || maturity.isBlank()) {
            return "";
        }
        String normalized = maturity.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        if ("operativo".equals(normalized) || "operativo_mvp".equals(normalized) || "operativomvp".equals(normalized)) {
            return "operativo_mvp";
        }
        if ("preliminar".equals(normalized)) {
            return "preliminar";
        }
        if ("exploratorio".equals(normalized) || "exploracion".equals(normalized)) {
            return "exploratorio";
        }
        return normalized;
    }

    private static Definition findDefinition(String presetId, String variableName) {
        if (presetId != null && !presetId.isBlank()) {
            String normalizedPreset = normalizeToken(presetId);
            for (Definition definition : DEFINITIONS) {
                if (normalizeToken(definition.id()).equals(normalizedPreset)) {
                    return definition;
                }
            }
        }
        for (Definition definition : DEFINITIONS) {
            if (definition.matchesVariable(variableName)) {
                return definition;
            }
        }
        return null;
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        return fallback != null ? fallback.trim() : "";
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

    private static String humanizeToken(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.trim().replace('_', ' ').replace('-', ' ');
        String[] parts = normalized.split("\\s+");
        StringBuilder humanized = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!humanized.isEmpty()) {
                humanized.append(' ');
            }
            humanized.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                humanized.append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return humanized.toString();
    }

    record Definition(String id,
                      String displayName,
                      List<String> variableAliases,
                      String defaultRecipe,
                      List<String> defaultFlags,
                      String defaultMaturity) {

        boolean matchesVariable(String variableName) {
            String normalized = normalizeToken(variableName);
            if (normalized.isBlank()) {
                return false;
            }
            for (String alias : variableAliases) {
                if (normalizeToken(alias).equals(normalized)) {
                    return true;
                }
            }
            return false;
        }
    }

    record ResolvedLineage(Definition definition,
                           String presetId,
                           List<String> flagsApplied,
                           String recipe,
                           String maturity) {

        ResolvedLineage {
            presetId = presetId != null ? presetId.trim() : "";
            flagsApplied = flagsApplied != null ? List.copyOf(flagsApplied) : List.of();
            recipe = recipe != null ? recipe.trim() : "";
            maturity = maturity != null ? maturity.trim() : "";
        }

        String presetLabel() {
            if (definition != null) {
                if (presetId.isBlank() || normalizeToken(definition.id()).equals(normalizeToken(presetId))) {
                    return definition.displayName();
                }
                return definition.displayName() + " [" + presetId + "]";
            }
            return !presetId.isBlank() ? presetId : "Sin preset tematico";
        }

        String methodologyLabel() {
            return ProOceanColorPresetSupport.methodologyLabel(maturity);
        }
    }
}
