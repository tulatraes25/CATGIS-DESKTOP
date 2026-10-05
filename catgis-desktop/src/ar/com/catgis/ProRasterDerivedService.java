package ar.com.catgis;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;

import ar.com.catgis.RasterLayer;
import ar.com.catgis.ProOceanColorPresetSupport;
import ar.com.catgis.ProNasaOceanColorL3Support;
import ar.com.catgis.ProMetadataSidecarSupport;
import ar.com.catgis.ProLandsatL2Support;
import org.geotools.coverage.grid.GridCoverage2D;
import org.locationtech.jts.geom.Envelope;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

final class ProRasterDerivedService {

    static final String OP_PRO_THEMATIC_CHLOR_A = "pro_thematic_chlor_a";
    static final String OP_PRO_THEMATIC_KD490 = "pro_thematic_kd490";
    static final String OP_PRO_THEMATIC_TURBIDITY_TSM = "pro_thematic_turbidity_tsm";
    static final String OP_PRO_QA_BASIC_MASK = "pro_qa_basic_mask";
    static final String OP_PRO_QA_NASA_OCEANCOLOR_L3M = "pro_qa_nasa_oceancolor_l3m";
    static final String OP_PRO_QA_LANDSAT_L2SP = "pro_qa_landsat_l2sp";
    static final String OP_PRO_MASK_LANDSAT_CLOUDS = "pro_mask_landsat_clouds";
    static final String OP_PRO_MASK_LANDSAT_SHADOW = "pro_mask_landsat_shadow";
    static final String OP_PRO_MASK_LANDSAT_SNOW = "pro_mask_landsat_snow";
    static final String OP_PRO_MASK_LANDSAT_WATER = "pro_mask_landsat_water";
    static final String OP_PRO_COMPARE_DELTA = "pro_compare_delta";

    private static final int TRANSPARENT_ARGB = 0x00000000;
    private static final String QA_CRITERIA_BASIC_VALIDITY = "qa_basic_validity_v1";
    private static final String QA_CRITERIA_NASA_L3M_PHYSICAL_VALIDITY = "nasa_l3m_preliminary_physical_validity_v1";
    private static final String QA_CRITERIA_LANDSAT_L2SP_QAPIXEL = "landsat_l2sp_qapixel_preliminary_v1";
    private static final String QA_VALUE_MODE_RAW = "raw_raster_values";
    private static final String QA_VALUE_MODE_SCALE_OFFSET = "scale_offset_from_metadata";
    private static final String QA_SIGN_RULE_NONE = "none";
    private static final String QA_SIGN_RULE_POSITIVE = "positive";
    private static final String QA_SIGN_RULE_NON_NEGATIVE = "non_negative";

    private ProRasterDerivedService() {
    }

    static boolean supportsOperation(String operation) {
        String normalized = normalizeOperation(operation);
        return OP_PRO_THEMATIC_CHLOR_A.equals(normalized)
                || OP_PRO_THEMATIC_KD490.equals(normalized)
                || OP_PRO_THEMATIC_TURBIDITY_TSM.equals(normalized)
                || OP_PRO_QA_BASIC_MASK.equals(normalized)
                || OP_PRO_QA_NASA_OCEANCOLOR_L3M.equals(normalized)
                || OP_PRO_QA_LANDSAT_L2SP.equals(normalized)
                || OP_PRO_MASK_LANDSAT_CLOUDS.equals(normalized)
                || OP_PRO_MASK_LANDSAT_SHADOW.equals(normalized)
                || OP_PRO_MASK_LANDSAT_SNOW.equals(normalized)
                || OP_PRO_MASK_LANDSAT_WATER.equals(normalized)
                || OP_PRO_COMPARE_DELTA.equals(normalized);
    }

    static boolean isQaOperation(String operation) {
        String normalized = normalizeOperation(operation);
        return OP_PRO_QA_BASIC_MASK.equals(normalized)
                || OP_PRO_QA_NASA_OCEANCOLOR_L3M.equals(normalized)
                || OP_PRO_QA_LANDSAT_L2SP.equals(normalized)
                || OP_PRO_MASK_LANDSAT_CLOUDS.equals(normalized)
                || OP_PRO_MASK_LANDSAT_SHADOW.equals(normalized)
                || OP_PRO_MASK_LANDSAT_SNOW.equals(normalized)
                || OP_PRO_MASK_LANDSAT_WATER.equals(normalized);
    }

    static String qaValueModeLabel(String valueMode) {
        if (valueMode == null || valueMode.isBlank()) {
            return "";
        }
        return switch (normalizeToken(valueMode)) {
            case "scale_offset_from_metadata" -> "Valores escalados con scale_factor/add_offset";
            default -> "Valores raster directos";
        };
    }

    static String describeExpectedQa(ProMetadataSidecarSupport.Metadata metadata) {
        if (metadata == null || metadata.variable() == null) {
            return "";
        }
        if (isNasaOceanColorRangeQa(metadata)) {
            StringBuilder summary = new StringBuilder("Validez fisica preliminar NASA L3m");
            String range = qaRangeLabel(metadata.variable().getValidMin(), metadata.variable().getValidMax(), metadata.variable().getUnits());
            if (!range.isBlank()) {
                summary.append(" | ").append(range);
            }
            String signRule = qaSignRuleLabel(resolveSignRule(metadata.variable().getBandFamily(), metadata.variable().getValidMin()));
            if (!signRule.isBlank()) {
                summary.append(" | ").append(signRule);
            }
            if (hasMeaningfulScaleOffset(metadata.variable().getScaleFactor(), metadata.variable().getAddOffset())) {
                summary.append(" | autodetecta scale_factor/add_offset si el raster quedo codificado");
            }
            return summary.toString();
        }
        if (isLandsatReflectanceQa(metadata)) {
            StringBuilder summary = new StringBuilder("QA preliminar Landsat L2SP via QA_PIXEL companera");
            String range = qaRangeLabel(metadata.variable().getValidMin(), metadata.variable().getValidMax(), metadata.variable().getUnits());
            if (!range.isBlank()) {
                summary.append(" | ").append(range);
            }
            if (hasMeaningfulScaleOffset(metadata.variable().getScaleFactor(), metadata.variable().getAddOffset())) {
                summary.append(" | reflectancia interpretada con scale_factor/add_offset");
            }
            summary.append(" | ").append(ProLandsatL2Support.preliminaryQaPixelSummary());
            summary.append(" | disponible para mascaras operativas de nubes, sombra, nieve y agua");
            return summary.toString();
        }
        if (isLandsatSurfaceTemperatureQa(metadata)) {
            StringBuilder summary = new StringBuilder("QA preliminar Landsat L2SP para temperatura superficial via QA_PIXEL companera");
            String range = qaRangeLabel(metadata.variable().getValidMin(), metadata.variable().getValidMax(), metadata.variable().getUnits());
            if (!range.isBlank()) {
                summary.append(" | ").append(range);
            }
            if (hasMeaningfulScaleOffset(metadata.variable().getScaleFactor(), metadata.variable().getAddOffset())) {
                summary.append(" | temperatura interpretada con scale_factor/add_offset");
            }
            summary.append(" | banda ST_QA disponible como incertidumbre por pixel");
            summary.append(" | ").append(ProLandsatL2Support.preliminaryQaPixelSummary());
            return summary.toString();
        }
        if (isLandsatQaPixel(metadata)) {
            return "Bitmask QA_PIXEL Landsat L2SP: nubes, sombra, nieve, cirrus, agua y fill. Puede generar mascaras operativas derivadas.";
        }
        if (isLandsatSurfaceTemperatureUncertainty(metadata)) {
            return "Banda ST_QA Landsat L2SP: incertidumbre de temperatura superficial por pixel, en Kelvin.";
        }
        if (isLandsatAerosolQa(metadata)) {
            return "Bitmask SR_QA_AEROSOL Landsat L2SP: aerosol/interpolacion/agua.";
        }
        if (isLandsatRadsatQa(metadata)) {
            return "Bitmask QA_RADSAT Landsat L2SP: saturacion radiometrica por banda.";
        }
        return metadata.variable().getQaDescriptor();
    }

    static boolean supportsThematicOutput(RasterLayer layer) {
        OperationProfile profile = resolveOperationProfile(layer);
        return profile != null && !profile.qaMask();
    }

    static String describeOperation(String operation) {
        return switch (normalizeOperation(operation)) {
            case OP_PRO_THEMATIC_CHLOR_A -> "Mapa tematico Pro - Clorofila-a";
            case OP_PRO_THEMATIC_KD490 -> "Mapa tematico Pro - Kd490";
            case OP_PRO_THEMATIC_TURBIDITY_TSM -> "Mapa tematico Pro - Turbidez / TSM";
            case OP_PRO_QA_BASIC_MASK -> "Mascara QA basica Pro";
            case OP_PRO_QA_NASA_OCEANCOLOR_L3M -> "Mascara QA preliminar - NASA OceanColor L3m (validez fisica)";
            case OP_PRO_QA_LANDSAT_L2SP -> "Mascara QA preliminar - Landsat L2SP (QA_PIXEL)";
            case OP_PRO_MASK_LANDSAT_CLOUDS -> "Mascara Landsat QA_PIXEL - Nubes / Cirrus";
            case OP_PRO_MASK_LANDSAT_SHADOW -> "Mascara Landsat QA_PIXEL - Sombra de nube";
            case OP_PRO_MASK_LANDSAT_SNOW -> "Mascara Landsat QA_PIXEL - Nieve / Hielo";
            case OP_PRO_MASK_LANDSAT_WATER -> "Mascara Landsat QA_PIXEL - Agua";
            case OP_PRO_COMPARE_DELTA -> "Comparacion temporal Pro - diferencia simple";
            default -> operation != null && !operation.isBlank() ? operation.trim() : "Sin operacion derivada";
        };
    }

    static GeneratedRasterLayer generateThematicLayer(RasterLayer sourceLayer) throws Exception {
        validateSourceLayer(sourceLayer);
        OperationProfile profile = resolveOperationProfile(sourceLayer);
        if (profile == null || profile.qaMask()) {
            throw new IllegalArgumentException("La capa Pro actual no tiene un preset tematico MVP compatible.");
        }
        return generateDerivedLayer(sourceLayer, profile);
    }

    static GeneratedRasterLayer generateBasicQaMaskLayer(RasterLayer sourceLayer) throws Exception {
        validateSourceLayer(sourceLayer);
        return generateDerivedLayer(sourceLayer, OperationProfile.qaBasic());
    }

    static GeneratedRasterLayer generateQaLayer(RasterLayer sourceLayer) throws Exception {
        validateSourceLayer(sourceLayer);
        ProMetadataSidecarSupport.Metadata metadata = resolveMetadata(sourceLayer);
        if (isNasaOceanColorRangeQa(metadata)) {
            return generateDerivedLayer(sourceLayer, OperationProfile.qaNasaOceanColorL3m());
        }
        if (isLandsatReflectanceQa(metadata) || isLandsatSurfaceTemperatureQa(metadata) || isLandsatQaPixel(metadata)) {
            return generateDerivedLayer(sourceLayer, OperationProfile.qaLandsatL2sp());
        }
        return generateDerivedLayer(sourceLayer, OperationProfile.qaBasic());
    }

    static boolean supportsLandsatQaPixelMasks(RasterLayer sourceLayer) {
        ProMetadataSidecarSupport.Metadata metadata = resolveMetadata(sourceLayer);
        return isLandsatReflectanceQa(metadata) || isLandsatSurfaceTemperatureQa(metadata) || isLandsatQaPixel(metadata);
    }

    static GeneratedRasterLayer generateLandsatQaPixelMask(RasterLayer sourceLayer, String operation) throws Exception {
        validateSourceLayer(sourceLayer);
        if (!supportsLandsatQaPixelMasks(sourceLayer)) {
            throw new IllegalArgumentException("La capa Pro actual no dispone de QA_PIXEL Landsat para generar mascaras operativas.");
        }
        OperationProfile profile = resolveLandsatMaskProfile(operation);
        if (profile == null) {
            throw new IllegalArgumentException("La mascara Landsat solicitada no esta soportada.");
        }
        return generateDerivedLayer(sourceLayer, profile);
    }

    static GeneratedRasterLayer generateComparisonLayer(RasterLayer sourceLayer, RasterLayer comparisonLayer) throws Exception {
        validateSourceLayer(sourceLayer);
        validateSourceLayer(comparisonLayer);
        ComparisonCompatibility compatibility = evaluateComparisonCompatibility(sourceLayer, comparisonLayer);
        if (!compatibility.compatible()) {
            throw new IllegalArgumentException(compatibility.reason());
        }

        ProMetadataSidecarSupport.Metadata sourceMetadata = resolveMetadata(sourceLayer);
        ProMetadataSidecarSupport.Metadata comparisonMetadata = resolveMetadata(comparisonLayer);
        RasterSnapshot sourceSnapshot = readSnapshot(sourceLayer, sourceMetadata);
        RasterSnapshot comparisonSnapshot = readSnapshot(comparisonLayer, comparisonMetadata);
        if (!firstNonBlank(sourceSnapshot.sourceCrs(), "").equalsIgnoreCase(firstNonBlank(comparisonSnapshot.sourceCrs(), ""))) {
            throw new IllegalArgumentException("Las capas Pro no comparten el mismo CRS operativo para comparar.");
        }
        if (sourceSnapshot.width() != comparisonSnapshot.width() || sourceSnapshot.height() != comparisonSnapshot.height()) {
            throw new IllegalArgumentException("Las capas Pro no comparten la misma grilla raster para comparar.");
        }
        if (!sameEnvelope(sourceSnapshot.envelope(), comparisonSnapshot.envelope())) {
            throw new IllegalArgumentException("Las capas Pro no comparten la misma extension geografica.");
        }

        BufferedImage image = buildComparisonImage(sourceSnapshot, comparisonSnapshot);
        String sourceMaturity = firstNonBlank(sourceLayer.getProMaturityLevel(), sourceMetadata != null ? sourceMetadata.maturity() : "");
        String comparisonMaturity = firstNonBlank(comparisonLayer.getProMaturityLevel(), comparisonMetadata != null ? comparisonMetadata.maturity() : "");
        String outputMaturity = combineComparisonMaturity(sourceMaturity, comparisonMaturity);
        String presetLabel = sourceSnapshot.presetLabel();
        String label = buildComparisonLayerLabel(presetLabel, sourceLayer.getProAcquisitionStart(), comparisonLayer.getProAcquisitionStart(), outputMaturity);
        String parameterSpec = buildComparisonParameterSpec(
                sourceLayer,
                comparisonLayer,
                sourceSnapshot,
                comparisonSnapshot,
                sourceMetadata,
                comparisonMetadata,
                outputMaturity
        );

        RasterLayer derived = new RasterLayer(label, sourceLayer.getPath());
        derived.setVisible(true);
        derived.setSourceCRS(sourceSnapshot.sourceCrs());
        derived.setSourceName("Comparacion temporal Pro entre " + sourceLayer.getName() + " y " + comparisonLayer.getName());
        derived.setFeatureCount(1);
        derived.setOpacity(0.88f);
        derived.setGrayscale(false);
        derived.setAutoContrast(false);
        derived.setRedBand(0);
        derived.setGreenBand(1);
        derived.setBlueBand(2);
        derived.setRasterMode("derived");
        derived.setDerivedOperation(OP_PRO_COMPARE_DELTA);
        derived.setDerivedParameters(parameterSpec);
        derived.setProDatasetRef(sourceLayer.getProDatasetRef());
        derived.setProVariableName(sourceLayer.getProVariableName());
        derived.setProAcquisitionStart(sourceLayer.getProAcquisitionStart());
        derived.setProMaturityLevel(outputMaturity);
        derived.setProMetadataSidecarPath(resolveSidecarPath(sourceLayer, sourceMetadata));

        LocalRasterData data = new LocalRasterData(
                image,
                sourceSnapshot.envelope(),
                Math.min(3, Math.max(1, image.getRaster().getNumBands())),
                true,
                sourceSnapshot.sourceCrs(),
                "derived",
                sourceSnapshot.sourceCrs()
        );
        return new GeneratedRasterLayer(derived, data, OP_PRO_COMPARE_DELTA);
    }

    static LocalRasterData regenerateDerivedRasterData(RasterLayer layer) throws Exception {
        if (layer == null || !layer.isDerivedLayer() || !supportsOperation(layer.getDerivedOperation())) {
            throw new IllegalArgumentException("La capa raster no corresponde a una salida Pro derivada.");
        }
        Map<String, String> parameters = parseParameterSpec(layer.getDerivedParameters());
        RasterLayer sourceLayer = new RasterLayer(
                parameters.getOrDefault("sourceName", layer.getName()),
                parameters.getOrDefault("sourcePath", layer.getPath())
        );
        sourceLayer.setSourceCRS(parameters.getOrDefault("sourceCrs", layer.getSourceCRS()));
        sourceLayer.setSourceName(parameters.getOrDefault("sourceName", layer.getSourceName()));
        sourceLayer.setProDatasetRef(parameters.getOrDefault("datasetRef", layer.getProDatasetRef()));
        sourceLayer.setProVariableName(parameters.getOrDefault("variableName", layer.getProVariableName()));
        sourceLayer.setProAcquisitionStart(parameters.getOrDefault("acquisition", layer.getProAcquisitionStart()));
        sourceLayer.setProMaturityLevel(parameters.getOrDefault("sourceMaturity", layer.getProMaturityLevel()));
        sourceLayer.setProMetadataSidecarPath(parameters.getOrDefault("sidecarPath", layer.getProMetadataSidecarPath()));

        String operation = normalizeOperation(layer.getDerivedOperation());
        OperationProfile profile = switch (operation) {
            case OP_PRO_THEMATIC_CHLOR_A -> OperationProfile.thematic(
                    OP_PRO_THEMATIC_CHLOR_A,
                    "Mapa tematico Pro",
                    "chlor_a_mvp",
                    parameters.getOrDefault("outputMaturity", "preliminar")
            );
            case OP_PRO_THEMATIC_KD490 -> OperationProfile.thematic(
                    OP_PRO_THEMATIC_KD490,
                    "Mapa tematico Pro",
                    "kd490_mvp",
                    parameters.getOrDefault("outputMaturity", "preliminar")
            );
            case OP_PRO_THEMATIC_TURBIDITY_TSM -> OperationProfile.thematic(
                    OP_PRO_THEMATIC_TURBIDITY_TSM,
                    "Mapa tematico Pro",
                    "turbidity_tsm_mvp",
                    parameters.getOrDefault("outputMaturity", "exploratorio")
            );
            case OP_PRO_QA_NASA_OCEANCOLOR_L3M -> OperationProfile.qaNasaOceanColorL3m();
            case OP_PRO_QA_LANDSAT_L2SP -> OperationProfile.qaLandsatL2sp();
            case OP_PRO_MASK_LANDSAT_CLOUDS -> OperationProfile.landsatClouds();
            case OP_PRO_MASK_LANDSAT_SHADOW -> OperationProfile.landsatShadow();
            case OP_PRO_MASK_LANDSAT_SNOW -> OperationProfile.landsatSnow();
            case OP_PRO_MASK_LANDSAT_WATER -> OperationProfile.landsatWater();
            case OP_PRO_COMPARE_DELTA -> null;
            default -> OperationProfile.qaBasic();
        };
        if (OP_PRO_COMPARE_DELTA.equals(operation)) {
            RasterLayer comparisonLayer = new RasterLayer(
                    parameters.getOrDefault("compareName", "Comparacion"),
                    parameters.getOrDefault("comparePath", "")
            );
            comparisonLayer.setSourceCRS(parameters.getOrDefault("compareCrs", sourceLayer.getSourceCRS()));
            comparisonLayer.setSourceName(parameters.getOrDefault("compareName", comparisonLayer.getName()));
            comparisonLayer.setProDatasetRef(parameters.getOrDefault("compareDatasetRef", ""));
            comparisonLayer.setProVariableName(parameters.getOrDefault("compareVariableName", parameters.getOrDefault("variableName", layer.getProVariableName())));
            comparisonLayer.setProAcquisitionStart(parameters.getOrDefault("compareAcquisition", ""));
            comparisonLayer.setProMaturityLevel(parameters.getOrDefault("compareMaturity", ""));
            comparisonLayer.setProMetadataSidecarPath(parameters.getOrDefault("compareSidecarPath", ""));
            return generateComparisonLayer(sourceLayer, comparisonLayer).data();
        }
        return generateDerivedLayer(sourceLayer, profile).data();
    }

    private static GeneratedRasterLayer generateDerivedLayer(RasterLayer sourceLayer, OperationProfile profile) throws Exception {
        ProMetadataSidecarSupport.Metadata metadata = resolveMetadata(sourceLayer);
        ProOceanColorPresetSupport.ResolvedLineage lineage = ProOceanColorPresetSupport.resolve(
                metadata != null ? metadata.variable() : null,
                metadata != null ? metadata.qualityPreset() : "",
                metadata != null ? metadata.flagsApplied() : List.of(),
                metadata != null ? metadata.recipe() : "",
                firstNonBlank(sourceLayer.getProMaturityLevel(), metadata != null ? metadata.maturity() : "")
        );
        RasterSnapshot snapshot = readSnapshot(sourceLayer, metadata);
        BufferedImage image = profile.qaMask()
                ? buildQaMaskImage(snapshot, profile.operation())
                : buildThematicImage(snapshot, profile.operation());

        String outputMaturity = profile.qaMask()
                ? ProOceanColorPresetSupport.normalizeMaturity(profile.outputMaturity())
                : ProOceanColorPresetSupport.normalizeMaturity(firstNonBlank(profile.outputMaturity(), lineage.maturity()));
        String outputLabel = profile.qaMask()
                ? snapshot.presetLabel() + " | " + profile.label() + " (" + ProOceanColorPresetSupport.methodologyLabel(outputMaturity) + ")"
                : snapshot.presetLabel() + " | Mapa tematico Pro (" + ProOceanColorPresetSupport.methodologyLabel(outputMaturity) + ")";
        QaProfile qaProfile = profile.qaMask() ? resolveQaProfile(snapshot, profile.operation()) : null;
        String parameterSpec = buildParameterSpec(sourceLayer, snapshot, lineage, profile, outputMaturity, qaProfile);

        RasterLayer derived = new RasterLayer(outputLabel, sourceLayer.getPath());
        derived.setVisible(true);
        derived.setSourceCRS(snapshot.sourceCrs());
        derived.setSourceName(profile.qaMask()
                ? profile.label() + " derivada de " + sourceLayer.getName()
                : "Salida tematica Pro derivada de " + sourceLayer.getName());
        derived.setFeatureCount(1);
        derived.setOpacity(profile.qaMask() ? 0.74f : 0.86f);
        derived.setGrayscale(false);
        derived.setAutoContrast(false);
        derived.setRedBand(0);
        derived.setGreenBand(1);
        derived.setBlueBand(2);
        derived.setRasterMode("derived");
        derived.setDerivedOperation(profile.operation());
        derived.setDerivedParameters(parameterSpec);
        derived.setProDatasetRef(firstNonBlank(sourceLayer.getProDatasetRef(), metadata != null && metadata.dataset() != null ? metadata.dataset().getDatasetId() : ""));
        derived.setProVariableName(firstNonBlank(sourceLayer.getProVariableName(), metadata != null && metadata.variable() != null ? metadata.variable().getName() : ""));
        derived.setProAcquisitionStart(firstNonBlank(sourceLayer.getProAcquisitionStart(), metadata != null && metadata.dataset() != null ? metadata.dataset().getAcquisitionStart() : ""));
        derived.setProMaturityLevel(outputMaturity);
        derived.setProMetadataSidecarPath(resolveSidecarPath(sourceLayer, metadata));

        LocalRasterData data = new LocalRasterData(
                image,
                snapshot.envelope(),
                Math.min(3, Math.max(1, image.getRaster().getNumBands())),
                true,
                snapshot.sourceCrs(),
                "derived",
                snapshot.sourceCrs()
        );
        return new GeneratedRasterLayer(derived, data, profile.operation());
    }

    private static void validateSourceLayer(RasterLayer sourceLayer) {
        if (sourceLayer == null) {
            throw new IllegalArgumentException("No se selecciono una capa raster Pro.");
        }
        if (sourceLayer.isDerivedLayer()) {
            throw new IllegalArgumentException("La salida Pro MVP debe generarse desde un raster Pro base, no desde un derivado.");
        }
        String path = sourceLayer.getPath() != null ? sourceLayer.getPath().trim() : "";
        if (path.isBlank() || !new File(path).exists()) {
            throw new IllegalArgumentException("La capa Pro no conserva un raster fuente utilizable.");
        }
        if (!sourceLayer.hasProMetadata() && resolveMetadata(sourceLayer) == null) {
            throw new IllegalArgumentException("La capa seleccionada no tiene metadata Pro suficiente para generar una salida tematica.");
        }
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

    private static RasterSnapshot readSnapshot(RasterLayer sourceLayer, ProMetadataSidecarSupport.Metadata metadata) throws Exception {
        GridCoverage2D coverage = RasterCoverageSupport.readCoverageNative(sourceLayer);
        Raster raster = coverage.getRenderedImage().getData();
        Envelope envelope = extractEnvelope(coverage);
        if (envelope == null || envelope.isNull()) {
            throw new IllegalStateException("No se pudo resolver la extension geografica del raster Pro.");
        }
        String sourceCrs = RasterCoverageSupport.resolveCoverageCrsCode(coverage, sourceLayer);
        Double nodata = metadata != null && metadata.variable() != null ? metadata.variable().getNodata() : null;
        ProOceanColorPresetSupport.ResolvedLineage lineage = ProOceanColorPresetSupport.resolve(
                metadata != null ? metadata.variable() : null,
                metadata != null ? metadata.qualityPreset() : "",
                metadata != null ? metadata.flagsApplied() : List.of(),
                metadata != null ? metadata.recipe() : "",
                sourceLayer.getProMaturityLevel()
        );
        String presetLabel = lineage.presetLabel();
        String variableName = firstNonBlank(
                sourceLayer.getProVariableName(),
                metadata != null && metadata.variable() != null ? metadata.variable().getName() : ""
        );
        File qaCompanionFile = resolveLandsatQaCompanionFile(sourceLayer, metadata);
        Raster qaCompanionRaster = readCompanionRaster(sourceLayer, qaCompanionFile);
        return new RasterSnapshot(
                raster,
                envelope,
                sourceCrs,
                sourceLayer.getPath(),
                nodata,
                presetLabel,
                variableName,
                metadata != null && metadata.variable() != null ? metadata.variable().getValidMin() : null,
                metadata != null && metadata.variable() != null ? metadata.variable().getValidMax() : null,
                metadata != null && metadata.variable() != null ? metadata.variable().getScaleFactor() : null,
                metadata != null && metadata.variable() != null ? metadata.variable().getAddOffset() : null,
                metadata != null && metadata.variable() != null ? metadata.variable().getQaDescriptor() : "",
                metadata != null && metadata.dataset() != null ? metadata.dataset().getFamily() : "",
                metadata != null && metadata.variable() != null ? metadata.variable().getBandFamily() : "",
                qaCompanionRaster,
                qaCompanionFile != null ? qaCompanionFile.getAbsolutePath() : ""
        );
    }

    private static File resolveLandsatQaCompanionFile(RasterLayer sourceLayer, ProMetadataSidecarSupport.Metadata metadata) {
        if (sourceLayer == null || sourceLayer.getPath() == null || sourceLayer.getPath().isBlank()) {
            return null;
        }
        File sourceFile = new File(sourceLayer.getPath());
        if (!sourceFile.exists()) {
            return null;
        }
        if (isLandsatQaPixel(metadata)) {
            return sourceFile;
        }
        if (!isLandsatReflectanceQa(metadata) && !isLandsatSurfaceTemperatureQa(metadata)) {
            return null;
        }
        String datasetId = metadata != null && metadata.dataset() != null ? metadata.dataset().getDatasetId() : sourceLayer.getProDatasetRef();
        return ProLandsatL2Support.resolveQaPixelCompanion(sourceFile, datasetId);
    }

    private static Raster readCompanionRaster(RasterLayer sourceLayer, File companionFile) throws Exception {
        if (companionFile == null || !companionFile.exists() || !companionFile.isFile()) {
            return null;
        }
        if (sourceLayer != null && companionFile.getAbsolutePath().equalsIgnoreCase(firstNonBlank(sourceLayer.getPath(), ""))) {
            GridCoverage2D sameCoverage = RasterCoverageSupport.readCoverageNative(sourceLayer);
            return sameCoverage.getRenderedImage().getData();
        }
        RasterLayer companionLayer = new RasterLayer(companionFile.getName(), companionFile.getAbsolutePath());
        companionLayer.setSourceCRS(sourceLayer != null ? sourceLayer.getSourceCRS() : "");
        GridCoverage2D coverage = RasterCoverageSupport.readCoverageNative(companionLayer);
        return coverage.getRenderedImage().getData();
    }

    private static BufferedImage buildThematicImage(RasterSnapshot snapshot, String operation) {
        Stats stats = computeStats(snapshot, false);
        BufferedImage image = new BufferedImage(snapshot.width(), snapshot.height(), BufferedImage.TYPE_INT_ARGB);
        Raster raster = snapshot.raster();
        int minX = raster.getMinX();
        int minY = raster.getMinY();
        for (int row = 0; row < snapshot.height(); row++) {
            for (int col = 0; col < snapshot.width(); col++) {
                double value = raster.getSampleDouble(minX + col, minY + row, 0);
                if (!isValidSample(value, snapshot.nodata(), operation)) {
                    image.setRGB(col, row, TRANSPARENT_ARGB);
                    continue;
                }
                double ratio = switch (normalizeOperation(operation)) {
                    case OP_PRO_THEMATIC_CHLOR_A -> normalizeLogValue(value, stats.minPositive(), stats.maxPositive());
                    case OP_PRO_THEMATIC_KD490 -> normalizeLinear(value, stats.minPositive(), stats.maxPositive());
                    case OP_PRO_THEMATIC_TURBIDITY_TSM -> normalizeLinear(value, stats.minPositive(), stats.maxPositive());
                    default -> normalizeLinear(value, stats.minValue(), stats.maxValue());
                };
                Color color = switch (normalizeOperation(operation)) {
                    case OP_PRO_THEMATIC_CHLOR_A -> interpolateGradient(
                            ratio,
                            new Color(5, 34, 86),
                            new Color(0, 124, 170),
                            new Color(52, 168, 83),
                            new Color(247, 201, 72),
                            new Color(210, 83, 46)
                    );
                    case OP_PRO_THEMATIC_KD490 -> interpolateGradient(
                            ratio,
                            new Color(10, 58, 120),
                            new Color(0, 145, 199),
                            new Color(126, 211, 147),
                            new Color(240, 196, 75),
                            new Color(184, 86, 31)
                    );
                    case OP_PRO_THEMATIC_TURBIDITY_TSM -> interpolateGradient(
                            ratio,
                            new Color(22, 77, 86),
                            new Color(77, 161, 169),
                            new Color(194, 183, 126),
                            new Color(198, 127, 73),
                            new Color(130, 74, 44)
                    );
                    default -> interpolateGradient(ratio, new Color(40, 80, 160), new Color(240, 200, 80));
                };
                image.setRGB(col, row, color.getRGB());
            }
        }
        return image;
    }

    private static BufferedImage buildQaMaskImage(RasterSnapshot snapshot, String operation) {
        QaProfile qaProfile = resolveQaProfile(snapshot, operation);
        ensureRequiredQaCompanion(snapshot, qaProfile);
        BufferedImage image = new BufferedImage(snapshot.width(), snapshot.height(), BufferedImage.TYPE_INT_ARGB);
        for (int row = 0; row < snapshot.height(); row++) {
            for (int col = 0; col < snapshot.width(); col++) {
                if (qaProfile != null && qaProfile.hasTargetMask()) {
                    image.setRGB(
                            col,
                            row,
                            matchesQaTargetPixel(snapshot, qaProfile, col, row)
                                    ? qaTargetColor(operation).getRGB()
                                    : TRANSPARENT_ARGB
                    );
                    continue;
                }
                boolean valid = isQaValidPixel(snapshot, qaProfile, col, row);
                image.setRGB(
                        col,
                        row,
                        valid ? new Color(34, 197, 94, 72).getRGB() : qaInvalidColor(operation).getRGB()
                );
            }
        }
        return image;
    }

    private static BufferedImage buildComparisonImage(RasterSnapshot sourceSnapshot, RasterSnapshot comparisonSnapshot) {
        QaProfile sourceQa = resolveQaProfile(sourceSnapshot, sourceSnapshot.qaOperation());
        QaProfile comparisonQa = resolveQaProfile(comparisonSnapshot, comparisonSnapshot.qaOperation());
        ensureRequiredQaCompanion(sourceSnapshot, sourceQa);
        ensureRequiredQaCompanion(comparisonSnapshot, comparisonQa);
        Stats stats = computeComparisonStats(sourceSnapshot, comparisonSnapshot);
        BufferedImage image = new BufferedImage(sourceSnapshot.width(), sourceSnapshot.height(), BufferedImage.TYPE_INT_ARGB);
        Raster sourceRaster = sourceSnapshot.raster();
        Raster comparisonRaster = comparisonSnapshot.raster();
        int minX = sourceRaster.getMinX();
        int minY = sourceRaster.getMinY();
        for (int row = 0; row < sourceSnapshot.height(); row++) {
            for (int col = 0; col < sourceSnapshot.width(); col++) {
                double sourceValue = sourceRaster.getSampleDouble(minX + col, minY + row, 0);
                double comparisonValue = comparisonRaster.getSampleDouble(minX + col, minY + row, 0);
                if (!isComparablePixel(sourceSnapshot, sourceQa, col, row)
                        || !isComparablePixel(comparisonSnapshot, comparisonQa, col, row)) {
                    image.setRGB(col, row, TRANSPARENT_ARGB);
                    continue;
                }
                double delta = sourceValue - comparisonValue;
                double ratio = normalizeSymmetric(delta, stats.maxAbsoluteValue());
                Color color = interpolateGradient(
                        ratio,
                        new Color(22, 62, 124),
                        new Color(85, 149, 196),
                        new Color(243, 245, 246),
                        new Color(238, 162, 93),
                        new Color(164, 47, 45)
                );
                image.setRGB(col, row, color.getRGB());
            }
        }
        return image;
    }

    private static Stats computeStats(RasterSnapshot snapshot, boolean includeNonPositive) {
        double minValue = Double.POSITIVE_INFINITY;
        double maxValue = Double.NEGATIVE_INFINITY;
        double minPositive = Double.POSITIVE_INFINITY;
        double maxPositive = Double.NEGATIVE_INFINITY;
        Raster raster = snapshot.raster();
        int minX = raster.getMinX();
        int minY = raster.getMinY();
        for (int row = 0; row < snapshot.height(); row++) {
            for (int col = 0; col < snapshot.width(); col++) {
                double value = raster.getSampleDouble(minX + col, minY + row, 0);
                if (!Double.isFinite(value) || isNodata(value, snapshot.nodata())) {
                    continue;
                }
                if (includeNonPositive || value > 0d) {
                    minValue = Math.min(minValue, value);
                    maxValue = Math.max(maxValue, value);
                }
                if (value > 0d) {
                    minPositive = Math.min(minPositive, value);
                    maxPositive = Math.max(maxPositive, value);
                }
            }
        }
        if (!Double.isFinite(minValue) || !Double.isFinite(maxValue)) {
            minValue = 0d;
            maxValue = 1d;
        }
        if (!Double.isFinite(minPositive) || !Double.isFinite(maxPositive)) {
            minPositive = Math.max(1.0e-6d, minValue <= 0d ? 1.0e-6d : minValue);
            maxPositive = Math.max(minPositive * 1.0001d, maxValue <= 0d ? minPositive * 2d : maxValue);
        }
        if (maxValue <= minValue) {
            maxValue = minValue + 1d;
        }
        if (maxPositive <= minPositive) {
            maxPositive = minPositive * 1.0001d;
        }
        return new Stats(minValue, maxValue, minPositive, maxPositive);
    }

    private static boolean isValidSample(double value, Double nodata, String operation) {
        if (!Double.isFinite(value) || isNodata(value, nodata)) {
            return false;
        }
        String normalized = normalizeOperation(operation);
        if (OP_PRO_THEMATIC_CHLOR_A.equals(normalized)
                || OP_PRO_THEMATIC_KD490.equals(normalized)
                || OP_PRO_THEMATIC_TURBIDITY_TSM.equals(normalized)) {
            return value > 0d;
        }
        return true;
    }

    private static boolean isComparablePixel(RasterSnapshot snapshot, QaProfile qaProfile, int col, int row) {
        return isQaValidPixel(snapshot, qaProfile, col, row);
    }

    private static boolean matchesQaTargetPixel(RasterSnapshot snapshot, QaProfile qaProfile, int col, int row) {
        if (snapshot == null || qaProfile == null || !qaProfile.hasTargetMask()) {
            return false;
        }
        Raster qaRaster = snapshot.qaCompanionRaster();
        if (qaRaster == null) {
            return false;
        }
        int qaValue = qaRaster.getSample(qaRaster.getMinX() + col, qaRaster.getMinY() + row, 0);
        if (!ProLandsatL2Support.hasAnyQaPixelBits(qaValue, qaProfile.qaTargetBitMask())) {
            return false;
        }
        return qaProfile.qaRejectBitMask() == null
                || !ProLandsatL2Support.hasAnyQaPixelBits(qaValue, qaProfile.qaRejectBitMask());
    }

    private static boolean isQaValidPixel(RasterSnapshot snapshot, QaProfile qaProfile, int col, int row) {
        Raster raster = snapshot.raster();
        int sampleX = raster.getMinX() + col;
        int sampleY = raster.getMinY() + row;
        double value = raster.getSampleDouble(sampleX, sampleY, 0);
        if (!Double.isFinite(value) || isNodata(value, snapshot.nodata())) {
            return false;
        }
        if (qaProfile != null) {
            double interpreted = qaProfile.usesScaleOffset()
                    ? applyScaleOffset(value, qaProfile.scaleFactor(), qaProfile.addOffset())
                    : value;
            if (!Double.isFinite(interpreted)) {
                return false;
            }
            if (qaProfile.validMin() != null && interpreted < qaProfile.validMin()) {
                return false;
            }
            if (qaProfile.validMax() != null && interpreted > qaProfile.validMax()) {
                return false;
            }
            if (QA_SIGN_RULE_POSITIVE.equals(qaProfile.signRule())) {
                return interpreted > 0d;
            }
            if (QA_SIGN_RULE_NON_NEGATIVE.equals(qaProfile.signRule())) {
                return interpreted >= 0d;
            }
            if (qaProfile.qaInvalidBitMask() != null) {
                Raster qaRaster = snapshot.qaCompanionRaster();
                if (qaRaster == null) {
                    return false;
                }
                int qaValue = qaRaster.getSample(qaRaster.getMinX() + col, qaRaster.getMinY() + row, 0);
                if ((qaValue & qaProfile.qaInvalidBitMask()) != 0) {
                    return false;
                }
            }
            return true;
        }
        return isValidSample(value, snapshot.nodata(), resolveOperationForVariable(snapshot.variableName()));
    }

    private static boolean isNodata(double value, Double nodata) {
        return nodata != null && Double.isFinite(nodata) && Math.abs(value - nodata) <= Math.max(1.0e-9d, Math.abs(nodata) * 1.0e-9d);
    }

    private static double normalizeLogValue(double value, double min, double max) {
        double safeMin = Math.max(1.0e-6d, min);
        double safeMax = Math.max(safeMin * 1.0001d, max);
        double numerator = Math.log10(Math.max(value, safeMin)) - Math.log10(safeMin);
        double denominator = Math.log10(safeMax) - Math.log10(safeMin);
        return clampRatio(denominator == 0d ? 0d : numerator / denominator);
    }

    private static double normalizeLinear(double value, double min, double max) {
        double safeMax = max <= min ? min + 1d : max;
        return clampRatio((value - min) / (safeMax - min));
    }

    private static double normalizeSymmetric(double value, double maxAbsolute) {
        double safeMax = maxAbsolute <= 0d ? 1d : maxAbsolute;
        return clampRatio((value + safeMax) / (safeMax * 2d));
    }

    private static double clampRatio(double ratio) {
        return Math.max(0d, Math.min(1d, ratio));
    }

    private static Color interpolateGradient(double ratio, Color... stops) {
        if (stops == null || stops.length == 0) {
            return new Color(80, 120, 180);
        }
        if (stops.length == 1) {
            return stops[0];
        }
        double clamped = clampRatio(ratio);
        double scaled = clamped * (stops.length - 1);
        int lowerIndex = (int) Math.floor(scaled);
        int upperIndex = Math.min(stops.length - 1, lowerIndex + 1);
        double localRatio = scaled - lowerIndex;
        Color lower = stops[lowerIndex];
        Color upper = stops[upperIndex];
        int red = (int) Math.round(lower.getRed() + ((upper.getRed() - lower.getRed()) * localRatio));
        int green = (int) Math.round(lower.getGreen() + ((upper.getGreen() - lower.getGreen()) * localRatio));
        int blue = (int) Math.round(lower.getBlue() + ((upper.getBlue() - lower.getBlue()) * localRatio));
        return new Color(red, green, blue, 255);
    }

    private static Envelope extractEnvelope(GridCoverage2D coverage) {
        if (coverage == null || coverage.getEnvelope2D() == null) {
            return null;
        }
        return new Envelope(
                coverage.getEnvelope2D().getMinX(),
                coverage.getEnvelope2D().getMaxX(),
                coverage.getEnvelope2D().getMinY(),
                coverage.getEnvelope2D().getMaxY()
        );
    }

    private static boolean sameEnvelope(Envelope first, Envelope second) {
        if (first == null || second == null) {
            return false;
        }
        return Math.abs(first.getMinX() - second.getMinX()) <= 1.0e-9d
                && Math.abs(first.getMaxX() - second.getMaxX()) <= 1.0e-9d
                && Math.abs(first.getMinY() - second.getMinY()) <= 1.0e-9d
                && Math.abs(first.getMaxY() - second.getMaxY()) <= 1.0e-9d;
    }

    private static OperationProfile resolveOperationProfile(RasterLayer sourceLayer) {
        ProMetadataSidecarSupport.Metadata metadata = resolveMetadata(sourceLayer);
        ProOceanColorPresetSupport.ResolvedLineage lineage = ProOceanColorPresetSupport.resolve(
                metadata != null ? metadata.variable() : null,
                metadata != null ? metadata.qualityPreset() : "",
                metadata != null ? metadata.flagsApplied() : List.of(),
                metadata != null ? metadata.recipe() : "",
                sourceLayer != null
                        ? firstNonBlank(sourceLayer.getProMaturityLevel(), metadata != null ? metadata.maturity() : "")
                        : ""
        );
        String presetId = lineage.presetId() != null ? lineage.presetId() : "";
        return switch (presetId.trim().toLowerCase(Locale.ROOT)) {
            case "chlor_a_mvp" -> OperationProfile.thematic(
                    OP_PRO_THEMATIC_CHLOR_A,
                    "Mapa tematico Pro",
                    "chlor_a_mvp",
                    lineage.maturity()
            );
            case "kd490_mvp" -> OperationProfile.thematic(
                    OP_PRO_THEMATIC_KD490,
                    "Mapa tematico Pro",
                    "kd490_mvp",
                    lineage.maturity()
            );
            case "turbidity_tsm_mvp" -> OperationProfile.thematic(
                    OP_PRO_THEMATIC_TURBIDITY_TSM,
                    "Mapa tematico Pro",
                    "turbidity_tsm_mvp",
                    lineage.maturity()
            );
            default -> null;
        };
    }

    private static OperationProfile resolveLandsatMaskProfile(String operation) {
        return switch (normalizeOperation(operation)) {
            case OP_PRO_MASK_LANDSAT_CLOUDS -> OperationProfile.landsatClouds();
            case OP_PRO_MASK_LANDSAT_SHADOW -> OperationProfile.landsatShadow();
            case OP_PRO_MASK_LANDSAT_SNOW -> OperationProfile.landsatSnow();
            case OP_PRO_MASK_LANDSAT_WATER -> OperationProfile.landsatWater();
            default -> null;
        };
    }

    static String qaMenuLabel(RasterLayer sourceLayer) {
        ProMetadataSidecarSupport.Metadata metadata = resolveMetadata(sourceLayer);
        return isNasaOceanColorRangeQa(metadata)
                ? "Generar QA preliminar NASA OceanColor (validez fisica)"
                : (isLandsatReflectanceQa(metadata) || isLandsatSurfaceTemperatureQa(metadata) || isLandsatQaPixel(metadata))
                ? "Generar QA preliminar Landsat L2SP (QA_PIXEL)"
                : "Generar mascara QA basica";
    }

    static String defaultQaOperation(RasterLayer sourceLayer) {
        ProMetadataSidecarSupport.Metadata metadata = resolveMetadata(sourceLayer);
        if (isNasaOceanColorRangeQa(metadata)) {
            return OP_PRO_QA_NASA_OCEANCOLOR_L3M;
        }
        if (isLandsatReflectanceQa(metadata) || isLandsatSurfaceTemperatureQa(metadata) || isLandsatQaPixel(metadata)) {
            return OP_PRO_QA_LANDSAT_L2SP;
        }
        return OP_PRO_QA_BASIC_MASK;
    }

    static String landsatQaMaskMenuLabel(String operation) {
        return switch (normalizeOperation(operation)) {
            case OP_PRO_MASK_LANDSAT_CLOUDS -> "Generar mascara de nubes / cirrus";
            case OP_PRO_MASK_LANDSAT_SHADOW -> "Generar mascara de sombra";
            case OP_PRO_MASK_LANDSAT_SNOW -> "Generar mascara de nieve / hielo";
            case OP_PRO_MASK_LANDSAT_WATER -> "Generar mascara de agua";
            default -> "Generar mascara Landsat";
        };
    }

    static ComparisonCompatibility evaluateComparisonCompatibility(RasterLayer sourceLayer, RasterLayer comparisonLayer) {
        if (sourceLayer == null || comparisonLayer == null) {
            return new ComparisonCompatibility(false, "La comparacion temporal requiere dos capas Pro validas.");
        }
        if (comparisonLayer == sourceLayer) {
            return new ComparisonCompatibility(false, "La comparacion temporal requiere dos capas Pro distintas.");
        }
        if (sourceLayer.isDerivedLayer() || comparisonLayer.isDerivedLayer()) {
            return new ComparisonCompatibility(false, "La comparacion temporal debe hacerse entre rasters Pro base, no entre derivados.");
        }
        if (sourceLayer.getPath() == null || sourceLayer.getPath().isBlank() || !new File(sourceLayer.getPath()).exists()) {
            return new ComparisonCompatibility(false, "La capa Pro base no conserva un raster fuente utilizable.");
        }
        if (comparisonLayer.getPath() == null || comparisonLayer.getPath().isBlank() || !new File(comparisonLayer.getPath()).exists()) {
            return new ComparisonCompatibility(false, "La capa Pro de comparacion no conserva un raster fuente utilizable.");
        }

        ProMetadataSidecarSupport.Metadata sourceMetadata = resolveMetadata(sourceLayer);
        ProMetadataSidecarSupport.Metadata comparisonMetadata = resolveMetadata(comparisonLayer);
        String sourceVariable = normalizeToken(firstNonBlank(
                sourceLayer.getProVariableName(),
                sourceMetadata != null && sourceMetadata.variable() != null ? sourceMetadata.variable().getName() : ""
        ));
        String comparisonVariable = normalizeToken(firstNonBlank(
                comparisonLayer.getProVariableName(),
                comparisonMetadata != null && comparisonMetadata.variable() != null ? comparisonMetadata.variable().getName() : ""
        ));
        if (sourceVariable.isBlank() || comparisonVariable.isBlank()) {
            return new ComparisonCompatibility(false, "La comparacion temporal requiere variable oceanografica identificada en ambas capas Pro.");
        }
        if (!sourceVariable.isBlank() && !comparisonVariable.isBlank() && !sourceVariable.equals(comparisonVariable)) {
            return new ComparisonCompatibility(false, "Las capas Pro no representan la misma variable oceanografica.");
        }

        String sourceFamily = normalizeToken(sourceMetadata != null && sourceMetadata.dataset() != null ? sourceMetadata.dataset().getFamily() : "");
        String comparisonFamily = normalizeToken(comparisonMetadata != null && comparisonMetadata.dataset() != null ? comparisonMetadata.dataset().getFamily() : "");
        if (!sourceFamily.isBlank() && !comparisonFamily.isBlank() && !sourceFamily.equals(comparisonFamily)) {
            return new ComparisonCompatibility(false, "Las capas Pro no pertenecen a la misma familia fuente.");
        }

        String sourceBandFamily = normalizeToken(sourceMetadata != null && sourceMetadata.variable() != null ? sourceMetadata.variable().getBandFamily() : "");
        String comparisonBandFamily = normalizeToken(comparisonMetadata != null && comparisonMetadata.variable() != null ? comparisonMetadata.variable().getBandFamily() : "");
        if (!sourceBandFamily.isBlank() && !comparisonBandFamily.isBlank() && !sourceBandFamily.equals(comparisonBandFamily)) {
            return new ComparisonCompatibility(false, "Las capas Pro no comparten la misma familia tematica.");
        }

        String sourceTime = normalizeToken(sourceLayer.getProAcquisitionStart());
        String comparisonTime = normalizeToken(comparisonLayer.getProAcquisitionStart());
        if (sourceTime.isBlank() || comparisonTime.isBlank()) {
            return new ComparisonCompatibility(false, "La comparacion temporal requiere fecha Pro en ambas capas.");
        }
        if (!sourceTime.isBlank() && !comparisonTime.isBlank() && sourceTime.equals(comparisonTime)) {
            return new ComparisonCompatibility(false, "La comparacion temporal requiere fechas distintas.");
        }

        String sourceCrs = normalizeToken(sourceLayer.getSourceCRS());
        String comparisonCrs = normalizeToken(comparisonLayer.getSourceCRS());
        if (!sourceCrs.isBlank() && !comparisonCrs.isBlank() && !sourceCrs.equals(comparisonCrs)) {
            return new ComparisonCompatibility(false, "Las capas Pro no comparten el mismo CRS operativo.");
        }
        return new ComparisonCompatibility(true, "");
    }

    static String comparisonSummary(RasterLayer layer) {
        Map<String, String> parameters = parseParameterSpec(layer != null ? layer.getDerivedParameters() : "");
        if (parameters.isEmpty()) {
            return "";
        }
        return firstNonBlank(
                parameters.get("compareAcquisition"),
                parameters.get("compareName")
        );
    }

    static String formatAcquisitionLabel(String acquisition) {
        if (acquisition == null || acquisition.isBlank()) {
            return "sin fecha";
        }
        String value = acquisition.trim();
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }

    private static String resolveSidecarPath(RasterLayer layer, ProMetadataSidecarSupport.Metadata metadata) {
        if (layer != null && layer.getProMetadataSidecarPath() != null && !layer.getProMetadataSidecarPath().isBlank()) {
            return layer.getProMetadataSidecarPath();
        }
        return metadata != null && metadata.sidecarFile() != null ? metadata.sidecarFile().getAbsolutePath() : "";
    }

    private static String buildParameterSpec(RasterLayer sourceLayer,
                                             RasterSnapshot snapshot,
                                             ProOceanColorPresetSupport.ResolvedLineage lineage,
                                             OperationProfile profile,
                                             String outputMaturity,
                                             QaProfile qaProfile) {
        Map<String, String> values = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        values.put("sourceName", sourceLayer.getName());
        values.put("sourcePath", sourceLayer.getPath());
        values.put("sourceCrs", snapshot.sourceCrs());
        values.put("datasetRef", sourceLayer.getProDatasetRef());
        values.put("variableName", sourceLayer.getProVariableName());
        values.put("acquisition", sourceLayer.getProAcquisitionStart());
        values.put("sourceMaturity", sourceLayer.getProMaturityLevel());
        values.put("sourceFamily", snapshot.datasetFamily());
        values.put("sourceQaDescriptor", snapshot.qaDescriptor());
        values.put("outputMaturity", outputMaturity);
        values.put("preset", lineage.presetId());
        values.put("flags", String.join(",", lineage.flagsApplied()));
        values.put("recipe", lineage.recipe());
        values.put("sidecarPath", sourceLayer.getProMetadataSidecarPath());
        values.put("profile", profile.operation());
        if (qaProfile != null) {
            values.put("qaCriteriaId", qaProfile.criteriaId());
            values.put("qaCriteriaLabel", qaProfile.criteriaLabel());
            values.put("qaCriteriaSummary", qaProfile.criteriaSummary());
            values.put("qaValueMode", qaProfile.valueMode());
            values.put("qaSignRule", qaProfile.signRule());
            values.put("qaBandFamily", qaProfile.bandFamily());
            values.put("qaValidMin", qaProfile.validMin() != null ? Double.toString(qaProfile.validMin()) : "");
            values.put("qaValidMax", qaProfile.validMax() != null ? Double.toString(qaProfile.validMax()) : "");
            values.put("qaScaleFactor", qaProfile.scaleFactor() != null ? Double.toString(qaProfile.scaleFactor()) : "");
            values.put("qaAddOffset", qaProfile.addOffset() != null ? Double.toString(qaProfile.addOffset()) : "");
            values.put("qaCompanionPath", qaProfile.qaCompanionPath());
            values.put("qaBits", qaProfile.qaBitsLabel());
            values.put("qaTargetLabel", qaProfile.qaTargetLabel());
            values.put("qaTargetBits", qaProfile.qaTargetBitsLabel());
            values.put("qaRejectBits", qaProfile.qaRejectBitsLabel());
        }

        StringBuilder spec = new StringBuilder();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (spec.length() > 0) {
                spec.append(';');
            }
            spec.append(entry.getKey()).append('=').append(sanitizeParameterValue(entry.getValue()));
        }
        return spec.toString();
    }

    private static String buildComparisonParameterSpec(RasterLayer sourceLayer,
                                                       RasterLayer comparisonLayer,
                                                       RasterSnapshot sourceSnapshot,
                                                       RasterSnapshot comparisonSnapshot,
                                                       ProMetadataSidecarSupport.Metadata sourceMetadata,
                                                       ProMetadataSidecarSupport.Metadata comparisonMetadata,
                                                       String outputMaturity) {
        Map<String, String> values = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        values.put("profile", OP_PRO_COMPARE_DELTA);
        values.put("sourceName", sourceLayer.getName());
        values.put("sourcePath", sourceLayer.getPath());
        values.put("sourceCrs", sourceSnapshot.sourceCrs());
        values.put("datasetRef", sourceLayer.getProDatasetRef());
        values.put("variableName", sourceLayer.getProVariableName());
        values.put("acquisition", sourceLayer.getProAcquisitionStart());
        values.put("sourceMaturity", sourceLayer.getProMaturityLevel());
        values.put("sourceFamily", sourceMetadata != null && sourceMetadata.dataset() != null ? sourceMetadata.dataset().getFamily() : "");
        values.put("sourceQaDescriptor", sourceMetadata != null && sourceMetadata.variable() != null ? sourceMetadata.variable().getQaDescriptor() : "");
        values.put("sidecarPath", resolveSidecarPath(sourceLayer, sourceMetadata));
        values.put("compareName", comparisonLayer.getName());
        values.put("comparePath", comparisonLayer.getPath());
        values.put("compareCrs", comparisonSnapshot.sourceCrs());
        values.put("compareDatasetRef", comparisonLayer.getProDatasetRef());
        values.put("compareVariableName", comparisonLayer.getProVariableName());
        values.put("compareAcquisition", comparisonLayer.getProAcquisitionStart());
        values.put("compareMaturity", comparisonLayer.getProMaturityLevel());
        values.put("compareFamily", comparisonMetadata != null && comparisonMetadata.dataset() != null ? comparisonMetadata.dataset().getFamily() : "");
        values.put("compareQaDescriptor", comparisonMetadata != null && comparisonMetadata.variable() != null ? comparisonMetadata.variable().getQaDescriptor() : "");
        values.put("compareSidecarPath", resolveSidecarPath(comparisonLayer, comparisonMetadata));
        values.put("outputMaturity", outputMaturity);

        StringBuilder spec = new StringBuilder();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (spec.length() > 0) {
                spec.append(';');
            }
            spec.append(entry.getKey()).append('=').append(sanitizeParameterValue(entry.getValue()));
        }
        return spec.toString();
    }

    static Map<String, String> parseParameterSpec(String spec) {
        Map<String, String> values = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (spec == null || spec.isBlank()) {
            return values;
        }
        String[] tokens = spec.split(";");
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            int equalsIndex = token.indexOf('=');
            if (equalsIndex <= 0) {
                continue;
            }
            String key = token.substring(0, equalsIndex).trim();
            String value = token.substring(equalsIndex + 1).trim();
            values.put(key, unsanitizeParameterValue(value));
        }
        return values;
    }

    private static String sanitizeParameterValue(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value
                .replace("%", "%25")
                .replace(";", "%3B")
                .replace("=", "%3D")
                .replace(",", "%2C");
    }

    private static String unsanitizeParameterValue(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value
                .replace("%2C", ",")
                .replace("%3D", "=")
                .replace("%3B", ";")
                .replace("%25", "%");
    }

    private static String normalizeOperation(String operation) {
        if (operation == null || operation.isBlank()) {
            return "";
        }
        return operation.trim().toLowerCase(Locale.ROOT);
    }

    private static String resolveOperationForVariable(String variableName) {
        String normalized = variableName != null ? variableName.trim().toLowerCase(Locale.ROOT) : "";
        if (normalized.contains("chlor")) {
            return OP_PRO_THEMATIC_CHLOR_A;
        }
        if (normalized.contains("kd490")) {
            return OP_PRO_THEMATIC_KD490;
        }
        if (normalized.contains("turb") || normalized.contains("tsm") || normalized.contains("spm")) {
            return OP_PRO_THEMATIC_TURBIDITY_TSM;
        }
        return OP_PRO_QA_BASIC_MASK;
    }

    private static boolean isNasaOceanColorRangeQa(ProMetadataSidecarSupport.Metadata metadata) {
        return metadata != null
                && metadata.dataset() != null
                && ProNasaOceanColorL3Support.FAMILY.equalsIgnoreCase(metadata.dataset().getFamily())
                && metadata.variable() != null
                && ProNasaOceanColorL3Support.QA_DESCRIPTOR_PRELIMINARY_RANGE.equalsIgnoreCase(metadata.variable().getQaDescriptor());
    }

    private static boolean isLandsatReflectanceQa(ProMetadataSidecarSupport.Metadata metadata) {
        return hasQaDescriptor(metadata, ProLandsatL2Support.QA_DESCRIPTOR_REFLECTANCE);
    }

    private static boolean isLandsatSurfaceTemperatureQa(ProMetadataSidecarSupport.Metadata metadata) {
        return hasQaDescriptor(metadata, ProLandsatL2Support.QA_DESCRIPTOR_SURFACE_TEMPERATURE);
    }

    private static boolean isLandsatSurfaceTemperatureUncertainty(ProMetadataSidecarSupport.Metadata metadata) {
        return hasQaDescriptor(metadata, ProLandsatL2Support.QA_DESCRIPTOR_ST_QA);
    }

    private static boolean isLandsatQaPixel(ProMetadataSidecarSupport.Metadata metadata) {
        return hasQaDescriptor(metadata, ProLandsatL2Support.QA_DESCRIPTOR_QA_PIXEL);
    }

    private static boolean isLandsatRadsatQa(ProMetadataSidecarSupport.Metadata metadata) {
        return hasQaDescriptor(metadata, ProLandsatL2Support.QA_DESCRIPTOR_QA_RADSAT);
    }

    private static boolean isLandsatAerosolQa(ProMetadataSidecarSupport.Metadata metadata) {
        return hasQaDescriptor(metadata, ProLandsatL2Support.QA_DESCRIPTOR_SR_QA_AEROSOL);
    }

    private static boolean hasQaDescriptor(ProMetadataSidecarSupport.Metadata metadata, String descriptor) {
        return metadata != null
                && metadata.variable() != null
                && descriptor != null
                && !descriptor.isBlank()
                && descriptor.equalsIgnoreCase(metadata.variable().getQaDescriptor());
    }

    private static QaProfile resolveQaProfile(RasterSnapshot snapshot, String operation) {
        if (snapshot == null) {
            return null;
        }
        String normalizedOperation = normalizeOperation(operation);
        if (OP_PRO_QA_NASA_OCEANCOLOR_L3M.equals(normalizedOperation)
                || ProNasaOceanColorL3Support.QA_DESCRIPTOR_PRELIMINARY_RANGE.equalsIgnoreCase(snapshot.qaDescriptor())) {
            String signRule = resolveSignRule(snapshot.bandFamily(), snapshot.validMin());
            String valueMode = inferNasaQaValueMode(snapshot, signRule);
            boolean usesScaleOffset = QA_VALUE_MODE_SCALE_OFFSET.equals(valueMode);
            String summary = buildNasaQaSummary(snapshot, signRule, valueMode);
            return new QaProfile(
                    QA_CRITERIA_NASA_L3M_PHYSICAL_VALIDITY,
                    "Validez fisica preliminar NASA L3m",
                    summary,
                    valueMode,
                    signRule,
                    snapshot.bandFamily(),
                    snapshot.validMin(),
                    snapshot.validMax(),
                    usesScaleOffset ? snapshot.scaleFactor() : null,
                    usesScaleOffset ? snapshot.addOffset() : null,
                    null,
                    null,
                    null,
                    "",
                    "",
                    "",
                    "",
                    ""
            );
        }
        if (OP_PRO_MASK_LANDSAT_CLOUDS.equals(normalizedOperation)) {
            return new QaProfile(
                    "landsat_qapixel_clouds_mask_v1",
                    "Mascara Landsat QA_PIXEL - Nubes / Cirrus",
                    buildLandsatMaskSummary("nubes / cirrus", ProLandsatL2Support.QA_PIXEL_CLOUDS_MASK, ProLandsatL2Support.QA_PIXEL_FILL_BIT),
                    QA_VALUE_MODE_RAW,
                    QA_SIGN_RULE_NONE,
                    snapshot.bandFamily(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    ProLandsatL2Support.QA_PIXEL_CLOUDS_MASK,
                    ProLandsatL2Support.QA_PIXEL_FILL_BIT,
                    "Nubes / Cirrus",
                    ProLandsatL2Support.qaPixelBitsLabel(ProLandsatL2Support.QA_PIXEL_CLOUDS_MASK),
                    ProLandsatL2Support.qaPixelBitsLabel(ProLandsatL2Support.QA_PIXEL_FILL_BIT),
                    snapshot.qaCompanionPath(),
                    ProLandsatL2Support.qaPixelBitsLabel(ProLandsatL2Support.QA_PIXEL_CLOUDS_MASK)
            );
        }
        if (OP_PRO_MASK_LANDSAT_SHADOW.equals(normalizedOperation)) {
            return new QaProfile(
                    "landsat_qapixel_shadow_mask_v1",
                    "Mascara Landsat QA_PIXEL - Sombra de nube",
                    buildLandsatMaskSummary("sombra de nube", ProLandsatL2Support.QA_PIXEL_CLOUD_SHADOW_BIT, ProLandsatL2Support.QA_PIXEL_FILL_BIT),
                    QA_VALUE_MODE_RAW,
                    QA_SIGN_RULE_NONE,
                    snapshot.bandFamily(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    ProLandsatL2Support.QA_PIXEL_CLOUD_SHADOW_BIT,
                    ProLandsatL2Support.QA_PIXEL_FILL_BIT,
                    "Sombra de nube",
                    ProLandsatL2Support.qaPixelBitsLabel(ProLandsatL2Support.QA_PIXEL_CLOUD_SHADOW_BIT),
                    ProLandsatL2Support.qaPixelBitsLabel(ProLandsatL2Support.QA_PIXEL_FILL_BIT),
                    snapshot.qaCompanionPath(),
                    ProLandsatL2Support.qaPixelBitsLabel(ProLandsatL2Support.QA_PIXEL_CLOUD_SHADOW_BIT)
            );
        }
        if (OP_PRO_MASK_LANDSAT_SNOW.equals(normalizedOperation)) {
            return new QaProfile(
                    "landsat_qapixel_snow_mask_v1",
                    "Mascara Landsat QA_PIXEL - Nieve / Hielo",
                    buildLandsatMaskSummary("nieve / hielo", ProLandsatL2Support.QA_PIXEL_SNOW_BIT, ProLandsatL2Support.QA_PIXEL_FILL_BIT),
                    QA_VALUE_MODE_RAW,
                    QA_SIGN_RULE_NONE,
                    snapshot.bandFamily(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    ProLandsatL2Support.QA_PIXEL_SNOW_BIT,
                    ProLandsatL2Support.QA_PIXEL_FILL_BIT,
                    "Nieve / Hielo",
                    ProLandsatL2Support.qaPixelBitsLabel(ProLandsatL2Support.QA_PIXEL_SNOW_BIT),
                    ProLandsatL2Support.qaPixelBitsLabel(ProLandsatL2Support.QA_PIXEL_FILL_BIT),
                    snapshot.qaCompanionPath(),
                    ProLandsatL2Support.qaPixelBitsLabel(ProLandsatL2Support.QA_PIXEL_SNOW_BIT)
            );
        }
        if (OP_PRO_MASK_LANDSAT_WATER.equals(normalizedOperation)) {
            return new QaProfile(
                    "landsat_qapixel_water_mask_v1",
                    "Mascara Landsat QA_PIXEL - Agua",
                    buildLandsatMaskSummary("agua", ProLandsatL2Support.QA_PIXEL_WATER_BIT, ProLandsatL2Support.PRELIMINARY_INVALID_QA_PIXEL_MASK),
                    QA_VALUE_MODE_RAW,
                    QA_SIGN_RULE_NONE,
                    snapshot.bandFamily(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    ProLandsatL2Support.QA_PIXEL_WATER_BIT,
                    ProLandsatL2Support.PRELIMINARY_INVALID_QA_PIXEL_MASK,
                    "Agua",
                    ProLandsatL2Support.qaPixelBitsLabel(ProLandsatL2Support.QA_PIXEL_WATER_BIT),
                    ProLandsatL2Support.qaPixelBitsLabel(ProLandsatL2Support.PRELIMINARY_INVALID_QA_PIXEL_MASK),
                    snapshot.qaCompanionPath(),
                    ProLandsatL2Support.qaPixelBitsLabel(ProLandsatL2Support.QA_PIXEL_WATER_BIT)
            );
        }
        if (OP_PRO_QA_LANDSAT_L2SP.equals(normalizedOperation)
                || ProLandsatL2Support.QA_DESCRIPTOR_REFLECTANCE.equalsIgnoreCase(snapshot.qaDescriptor())
                || ProLandsatL2Support.QA_DESCRIPTOR_SURFACE_TEMPERATURE.equalsIgnoreCase(snapshot.qaDescriptor())
                || ProLandsatL2Support.QA_DESCRIPTOR_QA_PIXEL.equalsIgnoreCase(snapshot.qaDescriptor())) {
            String valueMode = inferLandsatQaValueMode(snapshot);
            boolean usesScaleOffset = QA_VALUE_MODE_SCALE_OFFSET.equals(valueMode);
            return new QaProfile(
                    QA_CRITERIA_LANDSAT_L2SP_QAPIXEL,
                    "QA preliminar Landsat L2SP",
                    buildLandsatQaSummary(snapshot, valueMode),
                    valueMode,
                    QA_SIGN_RULE_NONE,
                    snapshot.bandFamily(),
                    snapshot.validMin(),
                    snapshot.validMax(),
                    usesScaleOffset ? snapshot.scaleFactor() : null,
                    usesScaleOffset ? snapshot.addOffset() : null,
                    ProLandsatL2Support.PRELIMINARY_INVALID_QA_PIXEL_MASK,
                    null,
                    null,
                    "",
                    "",
                    "",
                    snapshot.qaCompanionPath(),
                    ProLandsatL2Support.preliminaryQaPixelBitsLabel()
            );
        }
        return new QaProfile(
                QA_CRITERIA_BASIC_VALIDITY,
                "Validez raster basica",
                "Validez raster basica: finitos + NoData.",
                QA_VALUE_MODE_RAW,
                QA_SIGN_RULE_NONE,
                snapshot.bandFamily(),
                snapshot.validMin(),
                snapshot.validMax(),
                null,
                null,
                null,
                null,
                null,
                "",
                "",
                "",
                "",
                ""
        );
    }

    private static String inferNasaQaValueMode(RasterSnapshot snapshot, String signRule) {
        if (!hasMeaningfulScaleOffset(snapshot.scaleFactor(), snapshot.addOffset())) {
            return QA_VALUE_MODE_RAW;
        }
        int rawValid = 0;
        int scaledValid = 0;
        Raster raster = snapshot.raster();
        int minX = raster.getMinX();
        int minY = raster.getMinY();
        int totalPixels = Math.max(1, snapshot.width() * snapshot.height());
        int step = Math.max(1, (int) Math.sqrt(totalPixels / 256.0d));
        for (int row = 0; row < snapshot.height(); row += step) {
            for (int col = 0; col < snapshot.width(); col += step) {
                double rawValue = raster.getSampleDouble(minX + col, minY + row, 0);
                if (matchesQaRule(rawValue, snapshot, signRule, false)) {
                    rawValid++;
                }
                if (matchesQaRule(rawValue, snapshot, signRule, true)) {
                    scaledValid++;
                }
            }
        }
        return scaledValid > rawValid ? QA_VALUE_MODE_SCALE_OFFSET : QA_VALUE_MODE_RAW;
    }

    private static String inferLandsatQaValueMode(RasterSnapshot snapshot) {
        if (!hasMeaningfulScaleOffset(snapshot.scaleFactor(), snapshot.addOffset())) {
            return QA_VALUE_MODE_RAW;
        }
        int rawValid = 0;
        int scaledValid = 0;
        Raster raster = snapshot.raster();
        int minX = raster.getMinX();
        int minY = raster.getMinY();
        int totalPixels = Math.max(1, snapshot.width() * snapshot.height());
        int step = Math.max(1, (int) Math.sqrt(totalPixels / 256.0d));
        for (int row = 0; row < snapshot.height(); row += step) {
            for (int col = 0; col < snapshot.width(); col += step) {
                double rawValue = raster.getSampleDouble(minX + col, minY + row, 0);
                if (matchesQaRule(rawValue, snapshot, QA_SIGN_RULE_NONE, false)) {
                    rawValid++;
                }
                if (matchesQaRule(rawValue, snapshot, QA_SIGN_RULE_NONE, true)) {
                    scaledValid++;
                }
            }
        }
        return scaledValid > rawValid ? QA_VALUE_MODE_SCALE_OFFSET : QA_VALUE_MODE_RAW;
    }

    private static boolean matchesQaRule(double rawValue, RasterSnapshot snapshot, String signRule, boolean applyScaleOffset) {
        if (!Double.isFinite(rawValue) || isNodata(rawValue, snapshot.nodata())) {
            return false;
        }
        double interpreted = applyScaleOffset
                ? applyScaleOffset(rawValue, snapshot.scaleFactor(), snapshot.addOffset())
                : rawValue;
        if (!Double.isFinite(interpreted)) {
            return false;
        }
        if (snapshot.validMin() != null && interpreted < snapshot.validMin()) {
            return false;
        }
        if (snapshot.validMax() != null && interpreted > snapshot.validMax()) {
            return false;
        }
        if (QA_SIGN_RULE_POSITIVE.equals(signRule)) {
            return interpreted > 0d;
        }
        if (QA_SIGN_RULE_NON_NEGATIVE.equals(signRule)) {
            return interpreted >= 0d;
        }
        return true;
    }

    private static double applyScaleOffset(double rawValue, Double scaleFactor, Double addOffset) {
        double scale = scaleFactor != null && Double.isFinite(scaleFactor) ? scaleFactor : 1d;
        double offset = addOffset != null && Double.isFinite(addOffset) ? addOffset : 0d;
        return (rawValue * scale) + offset;
    }

    private static boolean hasMeaningfulScaleOffset(Double scaleFactor, Double addOffset) {
        boolean scaleMeaningful = scaleFactor != null
                && Double.isFinite(scaleFactor)
                && Math.abs(scaleFactor - 1d) > 1.0e-9d;
        boolean offsetMeaningful = addOffset != null
                && Double.isFinite(addOffset)
                && Math.abs(addOffset) > 1.0e-9d;
        return scaleMeaningful || offsetMeaningful;
    }

    private static String resolveSignRule(String bandFamily, Double validMin) {
        String normalizedBand = normalizeToken(bandFamily);
        if ("chl".equals(normalizedBand) || "iop".equals(normalizedBand)) {
            return QA_SIGN_RULE_POSITIVE;
        }
        if ("turb".equals(normalizedBand) || "tsm".equals(normalizedBand) || "spm".equals(normalizedBand)) {
            return QA_SIGN_RULE_NON_NEGATIVE;
        }
        if (validMin != null && validMin >= 0d) {
            return QA_SIGN_RULE_NON_NEGATIVE;
        }
        return QA_SIGN_RULE_NONE;
    }

    private static String buildNasaQaSummary(RasterSnapshot snapshot, String signRule, String valueMode) {
        StringBuilder summary = new StringBuilder("Validez fisica preliminar NASA L3m: finitos + NoData");
        String range = qaRangeLabel(snapshot.validMin(), snapshot.validMax(), "");
        if (!range.isBlank()) {
            summary.append(" + ").append(range);
        }
        String signLabel = qaSignRuleLabel(signRule);
        if (!signLabel.isBlank()) {
            summary.append(" + ").append(signLabel);
        }
        summary.append(" | ").append(qaValueModeLabel(valueMode));
        return summary.toString();
    }

    private static String buildLandsatQaSummary(RasterSnapshot snapshot, String valueMode) {
        StringBuilder summary = new StringBuilder(
                ProLandsatL2Support.QA_DESCRIPTOR_SURFACE_TEMPERATURE.equalsIgnoreCase(snapshot.qaDescriptor())
                        ? "QA preliminar Landsat L2SP: temperatura superficial valida + QA_PIXEL companera"
                        : "QA preliminar Landsat L2SP: reflectancia valida + QA_PIXEL companera"
        );
        String range = qaRangeLabel(snapshot.validMin(), snapshot.validMax(), "");
        if (!range.isBlank()) {
            summary.append(" | ").append(range);
        }
        if (hasMeaningfulScaleOffset(snapshot.scaleFactor(), snapshot.addOffset())) {
            summary.append(" | ").append(qaValueModeLabel(valueMode));
        }
        if (ProLandsatL2Support.QA_DESCRIPTOR_SURFACE_TEMPERATURE.equalsIgnoreCase(snapshot.qaDescriptor())) {
            summary.append(" | ST_QA disponible como incertidumbre por pixel");
        }
        summary.append(" | ").append(ProLandsatL2Support.preliminaryQaPixelSummary());
        if (snapshot.qaCompanionPath() != null && !snapshot.qaCompanionPath().isBlank()) {
            summary.append(" | QA_PIXEL: ").append(new File(snapshot.qaCompanionPath()).getName());
        }
        return summary.toString();
    }

    private static String buildLandsatMaskSummary(String targetLabel, int targetBits, int rejectBits) {
        StringBuilder summary = new StringBuilder("Mascara preliminar Landsat QA_PIXEL: resalta ");
        summary.append(targetLabel);
        summary.append(" | bits objetivo: ").append(ProLandsatL2Support.qaPixelBitsLabel(targetBits));
        String rejectLabel = ProLandsatL2Support.qaPixelBitsLabel(rejectBits);
        if (!rejectLabel.isBlank()) {
            summary.append(" | excluye: ").append(rejectLabel);
        }
        return summary.toString();
    }

    private static String qaRangeLabel(Double validMin, Double validMax, String units) {
        if (validMin == null && validMax == null) {
            return "";
        }
        StringBuilder range = new StringBuilder("rango ");
        range.append(validMin != null ? trimDouble(validMin) : "-inf");
        range.append(" a ");
        range.append(validMax != null ? trimDouble(validMax) : "+inf");
        if (units != null && !units.isBlank()) {
            range.append(' ').append(units.trim());
        }
        return range.toString();
    }

    private static String qaSignRuleLabel(String signRule) {
        return switch (normalizeToken(signRule)) {
            case "positive" -> "positividad estricta";
            case "non_negative" -> "no negativos";
            default -> "";
        };
    }

    private static String trimDouble(Double value) {
        if (value == null || !Double.isFinite(value)) {
            return "";
        }
        if (Math.abs(value - Math.rint(value)) <= 1.0e-9d) {
            return Long.toString(Math.round(value));
        }
        String text = Double.toString(value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }

    private static String combineComparisonMaturity(String sourceMaturity, String comparisonMaturity) {
        String normalizedSource = ProOceanColorPresetSupport.normalizeMaturity(sourceMaturity);
        String normalizedComparison = ProOceanColorPresetSupport.normalizeMaturity(comparisonMaturity);
        if ("exploratorio".equals(normalizedSource) || "exploratorio".equals(normalizedComparison)) {
            return "exploratorio";
        }
        if ("operativo_mvp".equals(normalizedSource) && "operativo_mvp".equals(normalizedComparison)) {
            return "operativo_mvp";
        }
        return "preliminar";
    }

    private static Stats computeComparisonStats(RasterSnapshot sourceSnapshot, RasterSnapshot comparisonSnapshot) {
        QaProfile sourceQa = resolveQaProfile(sourceSnapshot, sourceSnapshot.qaOperation());
        QaProfile comparisonQa = resolveQaProfile(comparisonSnapshot, comparisonSnapshot.qaOperation());
        ensureRequiredQaCompanion(sourceSnapshot, sourceQa);
        ensureRequiredQaCompanion(comparisonSnapshot, comparisonQa);
        double maxAbsolute = 0d;
        Raster sourceRaster = sourceSnapshot.raster();
        Raster comparisonRaster = comparisonSnapshot.raster();
        int minX = sourceRaster.getMinX();
        int minY = sourceRaster.getMinY();
        for (int row = 0; row < sourceSnapshot.height(); row++) {
            for (int col = 0; col < sourceSnapshot.width(); col++) {
                double sourceValue = sourceRaster.getSampleDouble(minX + col, minY + row, 0);
                double comparisonValue = comparisonRaster.getSampleDouble(minX + col, minY + row, 0);
                if (!isComparablePixel(sourceSnapshot, sourceQa, col, row)
                        || !isComparablePixel(comparisonSnapshot, comparisonQa, col, row)) {
                    continue;
                }
                maxAbsolute = Math.max(maxAbsolute, Math.abs(sourceValue - comparisonValue));
            }
        }
        return new Stats(-maxAbsolute, maxAbsolute, 0d, Math.max(1.0e-6d, maxAbsolute));
    }

    private static void ensureRequiredQaCompanion(RasterSnapshot snapshot, QaProfile qaProfile) {
        if (snapshot == null || qaProfile == null || !qaProfile.requiresQaCompanion()) {
            return;
        }
        if (snapshot.qaCompanionRaster() != null) {
            if (snapshot.qaCompanionRaster().getWidth() != snapshot.width()
                    || snapshot.qaCompanionRaster().getHeight() != snapshot.height()) {
                throw new IllegalStateException("El raster QA_PIXEL companero no comparte la misma grilla raster de la capa Pro.");
            }
            return;
        }
        throw new IllegalStateException("No se encontro un raster QA_PIXEL companero para aplicar la salida Landsat basada en QA_PIXEL.");
    }

    private static Color qaInvalidColor(String operation) {
        String normalized = normalizeOperation(operation);
        return (OP_PRO_QA_NASA_OCEANCOLOR_L3M.equals(normalized) || OP_PRO_QA_LANDSAT_L2SP.equals(normalized))
                ? new Color(220, 38, 38, 188)
                : new Color(217, 70, 239, 186);
    }

    private static Color qaTargetColor(String operation) {
        return switch (normalizeOperation(operation)) {
            case OP_PRO_MASK_LANDSAT_CLOUDS -> new Color(255, 255, 255, 188);
            case OP_PRO_MASK_LANDSAT_SHADOW -> new Color(88, 28, 135, 196);
            case OP_PRO_MASK_LANDSAT_SNOW -> new Color(125, 211, 252, 196);
            case OP_PRO_MASK_LANDSAT_WATER -> new Color(37, 99, 235, 184);
            default -> new Color(250, 204, 21, 188);
        };
    }

    private static String buildComparisonLayerLabel(String presetLabel,
                                                    String sourceAcquisition,
                                                    String compareAcquisition,
                                                    String outputMaturity) {
        return presetLabel
                + " | Delta "
                + formatAcquisitionLabel(sourceAcquisition)
                + " vs "
                + formatAcquisitionLabel(compareAcquisition)
                + " ("
                + ProOceanColorPresetSupport.methodologyLabel(outputMaturity)
                + ")";
    }

    private static String normalizeToken(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        return fallback != null ? fallback.trim() : "";
    }

    record ComparisonCompatibility(boolean compatible, String reason) {
    }

    record GeneratedRasterLayer(RasterLayer layer, LocalRasterData data, String operation) {
    }

    private record RasterSnapshot(Raster raster,
                                  Envelope envelope,
                                  String sourceCrs,
                                  String sourcePath,
                                  Double nodata,
                                  String presetLabel,
                                  String variableName,
                                  Double validMin,
                                  Double validMax,
                                  Double scaleFactor,
                                  Double addOffset,
                                  String qaDescriptor,
                                  String datasetFamily,
                                  String bandFamily,
                                  Raster qaCompanionRaster,
                                  String qaCompanionPath) {
        int width() {
            return raster != null ? raster.getWidth() : 0;
        }

        int height() {
            return raster != null ? raster.getHeight() : 0;
        }

        String qaOperation() {
            if (ProNasaOceanColorL3Support.QA_DESCRIPTOR_PRELIMINARY_RANGE.equalsIgnoreCase(qaDescriptor)
                    || ProNasaOceanColorL3Support.FAMILY.equalsIgnoreCase(datasetFamily)) {
                return OP_PRO_QA_NASA_OCEANCOLOR_L3M;
            }
            if (ProLandsatL2Support.QA_DESCRIPTOR_REFLECTANCE.equalsIgnoreCase(qaDescriptor)
                    || ProLandsatL2Support.QA_DESCRIPTOR_QA_PIXEL.equalsIgnoreCase(qaDescriptor)
                    || ProLandsatL2Support.FAMILY.equalsIgnoreCase(datasetFamily)) {
                return OP_PRO_QA_LANDSAT_L2SP;
            }
            return resolveOperationForVariable(variableName);
        }
    }

    private record Stats(double minValue, double maxValue, double minPositive, double maxPositive) {
        double maxAbsoluteValue() {
            return Math.max(Math.abs(minValue), Math.abs(maxValue));
        }
    }

    private record QaProfile(String criteriaId,
                             String criteriaLabel,
                             String criteriaSummary,
                             String valueMode,
                             String signRule,
                             String bandFamily,
                             Double validMin,
                             Double validMax,
                             Double scaleFactor,
                             Double addOffset,
                             Integer qaInvalidBitMask,
                             Integer qaTargetBitMask,
                             Integer qaRejectBitMask,
                             String qaTargetLabel,
                             String qaTargetBitsLabel,
                             String qaRejectBitsLabel,
                             String qaCompanionPath,
                             String qaBitsLabel) {
        boolean usesScaleOffset() {
            return QA_VALUE_MODE_SCALE_OFFSET.equals(valueMode);
        }

        boolean requiresQaCompanion() {
            return qaInvalidBitMask != null || qaTargetBitMask != null;
        }

        boolean hasTargetMask() {
            return qaTargetBitMask != null;
        }
    }

    private record OperationProfile(String operation,
                                    String label,
                                    String presetId,
                                    String outputMaturity,
                                    boolean qaMask) {
        static OperationProfile thematic(String operation, String label, String presetId, String outputMaturity) {
            return new OperationProfile(operation, label, presetId, outputMaturity, false);
        }

        static OperationProfile qaBasic() {
            return new OperationProfile(OP_PRO_QA_BASIC_MASK, "Mascara QA basica Pro", "qa_basic_mvp", "exploratorio", true);
        }

        static OperationProfile qaNasaOceanColorL3m() {
            return new OperationProfile(OP_PRO_QA_NASA_OCEANCOLOR_L3M, "QA preliminar NASA OceanColor (validez fisica)", "qa_nasa_oceancolor_l3m", "preliminar", true);
        }

        static OperationProfile qaLandsatL2sp() {
            return new OperationProfile(OP_PRO_QA_LANDSAT_L2SP, "QA preliminar Landsat L2SP (QA_PIXEL)", "qa_landsat_l2sp", "preliminar", true);
        }

        static OperationProfile landsatClouds() {
            return new OperationProfile(OP_PRO_MASK_LANDSAT_CLOUDS, "Mascara Landsat - Nubes / Cirrus", "landsat_qapixel_clouds", "preliminar", true);
        }

        static OperationProfile landsatShadow() {
            return new OperationProfile(OP_PRO_MASK_LANDSAT_SHADOW, "Mascara Landsat - Sombra de nube", "landsat_qapixel_shadow", "preliminar", true);
        }

        static OperationProfile landsatSnow() {
            return new OperationProfile(OP_PRO_MASK_LANDSAT_SNOW, "Mascara Landsat - Nieve / Hielo", "landsat_qapixel_snow", "preliminar", true);
        }

        static OperationProfile landsatWater() {
            return new OperationProfile(OP_PRO_MASK_LANDSAT_WATER, "Mascara Landsat - Agua", "landsat_qapixel_water", "preliminar", true);
        }
    }
}
