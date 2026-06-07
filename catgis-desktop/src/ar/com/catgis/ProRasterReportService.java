package ar.com.catgis;
import ar.com.catgis.ProRasterDerivedService;
import ar.com.catgis.ProRasterDerivedService;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.ProRasterDerivedService;
import ar.com.catgis.core.model.Layer;

import org.locationtech.jts.geom.Envelope;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class ProRasterReportService {

    private static final String MARKDOWN_EXTENSION = ".md";
    private static final DateTimeFormatter EXPORT_STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DecimalFormat ENVELOPE_FORMAT = new DecimalFormat("0.0000");

    private ProRasterReportService() {
    }

    static String buildSuggestedFileName(RasterLayer layer) {
        String baseName = layer != null ? safeToken(layer.getName()) : "raster_pro";
        if (baseName.isBlank()) {
            baseName = "raster_pro";
        }
        return baseName + "_ficha_pro" + MARKDOWN_EXTENSION;
    }

    static File ensureMarkdownExtension(File targetFile) {
        if (targetFile == null) {
            throw new IllegalArgumentException("No se indico un archivo de salida para la ficha Pro.");
        }
        String lower = targetFile.getName().toLowerCase(Locale.ROOT);
        if (lower.endsWith(MARKDOWN_EXTENSION) || lower.endsWith(".markdown")) {
            return targetFile;
        }
        File parent = targetFile.getParentFile();
        return parent != null
                ? new File(parent, targetFile.getName() + MARKDOWN_EXTENSION)
                : new File(targetFile.getAbsolutePath() + MARKDOWN_EXTENSION);
    }

    static File exportMarkdownReport(RasterLayer layer,
                                     LocalRasterData rasterData,
                                     String projectName,
                                     File targetFile) throws IOException {
        File outputFile = ensureMarkdownExtension(targetFile);
        File parent = outputFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
            throw new IOException("No se pudo crear la carpeta de salida para la ficha Pro.");
        }
        Files.writeString(outputFile.toPath(), buildMarkdownReport(layer, rasterData, projectName), StandardCharsets.UTF_8);
        return outputFile;
    }

    static String buildMarkdownReport(RasterLayer layer, LocalRasterData rasterData, String projectName) {
        if (layer == null) {
            throw new IllegalArgumentException("No se selecciono una capa raster Pro.");
        }
        ProMetadataSidecarSupport.Metadata metadata = resolveMetadata(layer);
        String effectiveMaturity = resolveEffectiveMaturity(layer, metadata);
        ProOceanColorPresetSupport.ResolvedLineage lineage = ProOceanColorPresetSupport.resolve(
                metadata != null ? metadata.variable() : null,
                metadata != null ? metadata.qualityPreset() : "",
                metadata != null ? metadata.flagsApplied() : List.of(),
                metadata != null ? metadata.recipe() : "",
                effectiveMaturity
        );
        Map<String, String> derivedParameters = layer.isDerivedLayer()
                ? ProRasterDerivedService.parseParameterSpec(layer.getDerivedParameters())
                : Map.of();

        StringBuilder md = new StringBuilder();
        md.append("# CATGIS Pro - Ficha Raster\n\n");
        appendBullet(md, "Proyecto CATGIS", firstNonBlank(projectName, "Proyecto CATGIS"));
        appendBullet(md, "Fecha de exportacion", EXPORT_STAMP.format(LocalDateTime.now()));
        appendBullet(md, "Flujo", "proyecto -> capas -> analisis -> salida");
        md.append('\n');

        md.append("## Estado metodologico\n\n");
        appendBullet(md, "Clasificacion metodologica", ProOceanColorPresetSupport.methodologyLabel(effectiveMaturity));
        appendBullet(md, "Alcance", ProOceanColorPresetSupport.methodologyDescription(effectiveMaturity));
        appendBullet(md, "Estado de resultado", layer.isDerivedLayer() ? "Salida derivada CATGIS Pro" : "Raster Pro base");
        appendBullet(md, "Modo raster", rasterModeLabel(firstNonBlank(layer.getRasterMode(), rasterData != null ? rasterData.getRasterMode() : "")));
        if (layer.isDerivedLayer()) {
            appendBullet(md, "Operacion derivada", ProRasterDerivedService.describeOperation(layer.getDerivedOperation()));
        }
        md.append('\n');

        md.append("## Fuente satelital y variable\n\n");
        appendBullet(md, "Capa", layer.getName());
        appendBullet(md, "Ruta raster", layer.getPath());
        appendBullet(md, "CRS", crsLabel(firstNonBlank(layer.getSourceCRS(), rasterData != null ? rasterData.getSourceCRS() : "")));
        appendBullet(md, "Dataset Pro", firstNonBlank(layer.getProDatasetRef(), metadata != null && metadata.dataset() != null ? metadata.dataset().getDatasetId() : ""));
        appendBullet(md, "Variable Pro", firstNonBlank(layer.getProVariableName(), metadata != null && metadata.variable() != null ? metadata.variable().getName() : ""));
        appendBullet(md, "Tiempo Pro", firstNonBlank(layer.getProAcquisitionStart(), metadata != null && metadata.dataset() != null ? metadata.dataset().getAcquisitionStart() : ""));
        if (metadata != null && metadata.dataset() != null) {
            appendBullet(md, "Familia", metadata.dataset().getFamily());
            appendBullet(md, "Proveedor", metadata.dataset().getProvider());
            appendBullet(md, "Plataforma", metadata.dataset().getPlatform());
            appendBullet(md, "Instrumento", metadata.dataset().getInstrument());
            appendBullet(md, "Nivel de procesamiento", metadata.dataset().getProcessingLevel());
            appendBullet(md, "Fin adquisicion", metadata.dataset().getAcquisitionEnd());
        }
        if (metadata != null && metadata.variable() != null) {
            appendBullet(md, "Descripcion", metadata.variable().getLongName());
            appendBullet(md, "standard_name", metadata.variable().getStandardName());
            appendBullet(md, "Unidades", metadata.variable().getUnits());
            if (!metadata.variable().getDimensions().isEmpty()) {
                appendBullet(md, "Dimensiones", String.join(", ", metadata.variable().getDimensions()));
            }
            if (metadata.variable().getNodata() != null) {
                appendBullet(md, "NoData", String.valueOf(metadata.variable().getNodata()));
            }
            if (metadata.variable().getScaleFactor() != null) {
                appendBullet(md, "scale_factor", String.valueOf(metadata.variable().getScaleFactor()));
            }
            if (metadata.variable().getAddOffset() != null) {
                appendBullet(md, "add_offset", String.valueOf(metadata.variable().getAddOffset()));
            }
            if (metadata.variable().getValidMin() != null) {
                appendBullet(md, "valid_min", String.valueOf(metadata.variable().getValidMin()));
            }
            if (metadata.variable().getValidMax() != null) {
                appendBullet(md, "valid_max", String.valueOf(metadata.variable().getValidMax()));
            }
            appendBullet(md, "QA descriptor", metadata.variable().getQaDescriptor());
            appendBullet(md, "QA prevista", ProRasterDerivedService.describeExpectedQa(metadata));
            appendBullet(md, "Familia tematica", metadata.variable().getBandFamily());
        }
        appendBullet(md, "Preset tematico", lineage.presetLabel());
        appendBullet(md, "Preset / recipe base", lineage.presetId());
        if (!lineage.flagsApplied().isEmpty()) {
            appendBullet(md, "Flags QA", String.join(", ", lineage.flagsApplied()));
        }
        appendBullet(md, "Receta MVP", lineage.recipe());
        appendBullet(md, "Sidecar Pro", firstNonBlank(layer.getProMetadataSidecarPath(), metadata != null && metadata.sidecarFile() != null ? metadata.sidecarFile().getAbsolutePath() : ""));
        appendBullet(md, "Job Pro", layer.getProJobRef());
        md.append('\n');

        md.append("## Datos raster disponibles\n\n");
        if (rasterData != null) {
            appendBullet(md, "Ancho", rasterData.getWidth() + " px");
            appendBullet(md, "Alto", rasterData.getHeight() + " px");
            appendBullet(md, "Bandas", Integer.toString(rasterData.getBandCount()));
            appendBullet(md, "Georreferenciado", rasterData.isGeoreferenced() ? "Si" : "No");
            appendBullet(md, "CRS de visualizacion", crsLabel(rasterData.getDisplayCRS()));
            appendBullet(md, "Extension", formatEnvelope(rasterData.getEnvelope()));
        } else {
            appendBullet(md, "Estado", "La capa no tiene datos raster cargados en memoria en este momento.");
        }
        md.append('\n');

        md.append("## Trazabilidad del resultado\n\n");
        if (layer.isDerivedLayer()) {
            appendBullet(md, "Perfil derivado", firstNonBlank(derivedParameters.get("profile"), layer.getDerivedOperation()));
            appendBullet(md, "Operacion derivada", ProRasterDerivedService.describeOperation(layer.getDerivedOperation()));
            appendBullet(md, "Raster fuente", firstNonBlank(derivedParameters.get("sourcePath"), layer.getPath()));
            appendBullet(md, "Nombre fuente", derivedParameters.get("sourceName"));
            appendBullet(md, "Dataset fuente", derivedParameters.get("datasetRef"));
            appendBullet(md, "Variable fuente", derivedParameters.get("variableName"));
            appendBullet(md, "Tiempo fuente", derivedParameters.get("acquisition"));
            appendBullet(md, "Madurez fuente", ProOceanColorPresetSupport.methodologyLabel(derivedParameters.get("sourceMaturity")));
            appendBullet(md, "Madurez de salida", ProOceanColorPresetSupport.methodologyLabel(derivedParameters.get("outputMaturity")));
            appendBullet(md, "Familia fuente", derivedParameters.get("sourceFamily"));
            appendBullet(md, "QA fuente", derivedParameters.get("sourceQaDescriptor"));
            appendBullet(md, "Flags heredados", humanizeFlags(derivedParameters.get("flags")));
            appendBullet(md, "Receta heredada", derivedParameters.get("recipe"));
            appendBullet(md, "Comparacion contra", derivedParameters.get("compareName"));
            appendBullet(md, "Fecha comparada", derivedParameters.get("compareAcquisition"));
            appendBullet(md, "Dataset comparado", derivedParameters.get("compareDatasetRef"));
            appendBullet(md, "Raster comparado", derivedParameters.get("comparePath"));
            appendBullet(md, "Madurez comparada", ProOceanColorPresetSupport.methodologyLabel(derivedParameters.get("compareMaturity")));
            appendBullet(md, "Familia comparada", derivedParameters.get("compareFamily"));
            appendBullet(md, "QA comparada", derivedParameters.get("compareQaDescriptor"));
            appendBullet(md, "QA aplicada", derivedParameters.get("qaCriteriaLabel"));
            appendBullet(md, "Criterio QA", derivedParameters.get("qaCriteriaSummary"));
            appendBullet(md, "Modo QA", ProRasterDerivedService.qaValueModeLabel(derivedParameters.get("qaValueMode")));
            String qaBits = derivedParameters.get("qaBits");
            String qaTargetBits = derivedParameters.get("qaTargetBits");
            if (qaTargetBits == null || qaTargetBits.isBlank() || !qaTargetBits.equalsIgnoreCase(qaBits)) {
                appendBullet(md, "Bits QA", qaBits);
            }
            appendBullet(md, "Mascara QA", derivedParameters.get("qaTargetLabel"));
            appendBullet(md, "Bits objetivo", qaTargetBits);
            appendBullet(md, "Bits excluidos", derivedParameters.get("qaRejectBits"));
            appendBullet(md, "Raster QA companero", derivedParameters.get("qaCompanionPath"));
        } else {
            appendBullet(md, "Estado", "Raster Pro base sin derivacion tematica adicional.");
        }
        md.append('\n');

        md.append("## Advertencia metodologica\n\n");
        md.append(ProOceanColorPresetSupport.methodologyDescription(effectiveMaturity)).append('\n').append('\n');
        md.append("Este reporte refleja el estado actual del flujo CATGIS Pro. ");
        md.append("No implica validacion cientifica o ingenieril completa salvo documentacion especifica de Cal/Val.\n");
        return md.toString();
    }

    private static ProMetadataSidecarSupport.Metadata resolveMetadata(RasterLayer layer) {
        if (layer == null) {
            return null;
        }
        if (layer.getProMetadataSidecarPath() != null && !layer.getProMetadataSidecarPath().isBlank()) {
            ProMetadataSidecarSupport.Metadata metadata = ProMetadataSidecarSupport.readSidecar(new File(layer.getProMetadataSidecarPath()));
            if (metadata != null) {
                return metadata;
            }
        }
        if (layer.getPath() != null && !layer.getPath().isBlank()) {
            return ProMetadataSidecarSupport.read(new File(layer.getPath()));
        }
        return null;
    }

    private static String resolveEffectiveMaturity(RasterLayer layer, ProMetadataSidecarSupport.Metadata metadata) {
        return ProOceanColorPresetSupport.normalizeMaturity(firstNonBlank(
                layer != null ? layer.getProMaturityLevel() : "",
                metadata != null ? metadata.maturity() : ""
        ));
    }

    private static String rasterModeLabel(String mode) {
        if (mode == null || mode.isBlank()) {
            return "Desconocido";
        }
        String normalized = mode.trim().toLowerCase(Locale.ROOT);
        if ("derived".equals(normalized)) {
            return "Derivado";
        }
        if ("preview".equals(normalized)) {
            return "Vista rapida";
        }
        if ("real".equals(normalized)) {
            return "Zoom real";
        }
        if ("virtual".equals(normalized)) {
            return "Virtual";
        }
        return mode;
    }

    private static String crsLabel(String code) {
        if (code == null || code.isBlank()) {
            return "-";
        }
        return CRSDefinitions.getLabelForCode(code);
    }

    private static String humanizeFlags(String rawFlags) {
        if (rawFlags == null || rawFlags.isBlank()) {
            return "";
        }
        String[] tokens = rawFlags.split(",");
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            String clean = token != null ? token.trim() : "";
            if (clean.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(clean);
        }
        return builder.toString();
    }

    private static String formatEnvelope(Envelope envelope) {
        if (envelope == null) {
            return "";
        }
        return "[" + ENVELOPE_FORMAT.format(envelope.getMinX()) + ", " + ENVELOPE_FORMAT.format(envelope.getMinY()) + "] - ["
                + ENVELOPE_FORMAT.format(envelope.getMaxX()) + ", " + ENVELOPE_FORMAT.format(envelope.getMaxY()) + "]";
    }

    private static void appendBullet(StringBuilder md, String label, String value) {
        if (md == null || label == null || label.isBlank() || value == null || value.isBlank()) {
            return;
        }
        md.append("- ").append(label).append(": ").append(value).append('\n');
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        return fallback != null ? fallback.trim() : "";
    }

    private static String safeToken(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text.trim()
                .replaceAll("[^A-Za-z0-9._-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^[._-]+", "")
                .replaceAll("[._-]+$", "");
    }
}
