package ar.com.catgis;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.ProRasterDerivedService;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Envelope;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class TopographyWorkflowSupport {

    private TopographyWorkflowSupport() {
    }

    public static List<Layer> getAvailableRasterLayers() {
        List<Layer> rasters = new ArrayList<>();
        if (AppContext.project() == null || AppContext.project().getLayers() == null) {
            return rasters;
        }
        for (Layer layer : AppContext.project().getLayers()) {
            if (isRasterLayer(layer)) {
                rasters.add(layer);
            }
        }
        return rasters;
    }

    public static Layer resolvePreferredRasterLayer() {
        Layer selected = AppContext.getSelectedLayer();
        if (isRasterLayer(selected)) {
            return selected;
        }
        List<Layer> rasters = getAvailableRasterLayers();
        for (Layer layer : rasters) {
            if (isDemLikeRaster(layer)) {
                return layer;
            }
        }
        return rasters.isEmpty() ? null : rasters.get(0);
    }

    public static SelectedProfileLine resolveSelectedProfileLine() {
        MapPanel mapPanel = CatgisDesktopApp.mapPanel;
        if (mapPanel == null) {
            return null;
        }

        Layer layer = mapPanel.getSelectedLayerRef();
        SimpleFeature feature = mapPanel.getSelectedFeatureRef();
        if (layer == null || feature == null) {
            return null;
        }

        Object geometryObject = feature.getDefaultGeometry();
        if (!(geometryObject instanceof Geometry geometry)) {
            return null;
        }
        if (!(geometry instanceof LineString) && !(geometry instanceof MultiLineString)) {
            return null;
        }

        String sourceCrs = layer.getSourceCRS();
        if (sourceCrs == null || sourceCrs.isBlank()) {
            sourceCrs = AppContext.project() != null ? AppContext.project().getProjectCRS() : "EPSG:4326";
        }
        String description = layer.getName() + " | " + feature.getID();
        return new SelectedProfileLine((Geometry) geometry.copy(), sourceCrs, description);
    }

    public static void showNoRasterMessage() {
        JOptionPane.showMessageDialog(
                CatgisDesktopApp.getMainFrameSafe(),
                I18n.t("No hay capas raster disponibles para trabajar con DEM, relieve, escorrentias, cuencas o perfiles.")
        );
    }

    public static String buildRasterOperationalGuidanceFragment(Layer layer, String useCase) {
        RasterOperationalContext context = resolveRasterOperationalContext(layer);
        if (context == null) {
            return "No se pudo estimar la resolucion operativa del DEM cargado. "
                    + defaultUseCaseAdvice(useCase);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<b>DEM operativo:</b> ")
                .append(context.crsCode())
                .append(" | ")
                .append(context.width())
                .append("x")
                .append(context.height())
                .append(" px");
        if (Double.isFinite(context.nominalCellSizeMeters()) && context.nominalCellSizeMeters() > 0d) {
            sb.append(" | ~")
                    .append(formatMetricValue(context.nominalCellSizeMeters()))
                    .append(" m/celda");
        }
        sb.append(".<br><br>")
                .append(describeScaleAdvice(context.nominalCellSizeMeters(), useCase));
        if (context.previewMode()) {
            sb.append("<br><br>")
                    .append("La capa puede estar en modo preview para dibujarse rapido, pero CATGIS calcula el analisis leyendo el raster real del archivo.");
        }
        return sb.toString();
    }

    private static boolean isRasterLayer(Layer layer) {
        if (layer == null) {
            return false;
        }
        if (layer instanceof RasterLayer) {
            return true;
        }
        String type = layer.getType() != null ? layer.getType().trim().toUpperCase(Locale.ROOT) : "";
        return type.contains("RASTER") || type.contains("IMAGE") || type.contains("IMAGEN");
    }

    public static boolean isDemLikeRaster(Layer layer) {
        if (!isRasterLayer(layer)) {
            return false;
        }
        String sourceName = layer.getSourceName() != null ? layer.getSourceName().toLowerCase(Locale.ROOT) : "";
        String layerName = layer.getName() != null ? layer.getName().toLowerCase(Locale.ROOT) : "";
        String path = layer.getPath() != null ? layer.getPath().toLowerCase(Locale.ROOT) : "";
        return sourceName.contains("dem")
                || layerName.contains("dem")
                || sourceName.contains("terrain")
                || sourceName.contains("elevacion")
                || sourceName.contains("elevation")
                || path.endsWith(".asc");
    }

    public static RasterVisualPreset resolveRasterVisualPreset(Layer layer, int bandCount) {
        int safeBandCount = Math.max(1, bandCount);
        if (!(layer instanceof RasterLayer rasterLayer) || !rasterLayer.isDerivedLayer()) {
            String description = safeBandCount >= 3
                    ? "Raster multibanda. Puedes ajustar opacidad, contraste y composicion RGB."
                    : "Raster DEM o monocanal. La opacidad y el contraste ayudan a mezclarlo con capas derivadas.";
            return new RasterVisualPreset(
                    "Raster general",
                    description,
                    Math.round((layer instanceof RasterLayer ? ((RasterLayer) layer).getOpacity() : 1.0f) * 100f),
                    layer instanceof RasterLayer && ((RasterLayer) layer).isGrayscale(),
                    !(layer instanceof RasterLayer) || ((RasterLayer) layer).isAutoContrast(),
                    safeBandCount >= 3
            );
        }

        String operation = rasterLayer.getDerivedOperation() != null
                ? rasterLayer.getDerivedOperation().trim().toLowerCase(Locale.ROOT)
                : "";
        return switch (operation) {
            case TerrainHydrologyAnalysisService.OP_HILLSHADE -> new RasterVisualPreset(
                    "Hillshade",
                    "Pensado como relieve base. Funciona mejor superpuesto bajo pendiente, aspecto o DEM con opacidad media.",
                    48,
                    false,
                    false,
                    false
            );
            case TerrainHydrologyAnalysisService.OP_SLOPE -> new RasterVisualPreset(
                    "Pendiente",
                    "Derivado cromatico para relieve. Suele rendir mejor entre 72% y 82% sobre hillshade.",
                    78,
                    false,
                    false,
                    false
            );
            case TerrainHydrologyAnalysisService.OP_ASPECT -> new RasterVisualPreset(
                    "Aspecto",
                    "Derivado direccional. Conviene usarlo semitransparente para no tapar el relieve base.",
                    74,
                    false,
                    false,
                    false
            );
            case TerrainHydrologyAnalysisService.OP_FLOW_DIRECTION -> new RasterVisualPreset(
                    "Direccion de flujo",
                    "Salida tecnica de lectura puntual. Opacidad media-alta para revisar patrones sin tapar completamente el DEM.",
                    82,
                    false,
                    false,
                    false
            );
            case TerrainHydrologyAnalysisService.OP_FLOW_ACCUMULATION -> new RasterVisualPreset(
                    "Acumulacion de flujo",
                    "Conviene superponerla sobre hillshade o DEM para resaltar corredores de drenaje sin perder contexto.",
                    86,
                    false,
                    false,
                    false
            );
            case FloodScenarioService.OP_PRELIMINARY_FLOOD -> new RasterVisualPreset(
                    "Inundacion preliminar",
                    "Escenario simplificado de anegamiento. Conviene superponerlo sobre DEM, hillshade o imagen base con opacidad alta.",
                    82,
                    false,
                    false,
                    false
            );
            case BooleanRiskService.OP_SLOPE_BOOLEAN_MASK -> new RasterVisualPreset(
                    "Mascara de pendiente",
                    "Mascara binaria derivada del DEM. Sirve para validar rapido el umbral de pendiente antes de combinarlo con suelos.",
                    82,
                    false,
                    false,
                    false
            );
            case BooleanRiskService.OP_SOIL_BOOLEAN_MASK -> new RasterVisualPreset(
                    "Mascara de suelo",
                    "Mascara binaria del raster de suelos. Se usa como insumo de reclasificacion preliminar territorial.",
                    82,
                    false,
                    false,
                    false
            );
            case BooleanRiskService.OP_PRELIMINARY_BOOLEAN_RISK -> new RasterVisualPreset(
                    "Riesgo booleano",
                    "Combinacion booleana preliminar entre pendiente y suelos. Conviene verla sobre DEM o imagen base.",
                    88,
                    false,
                    false,
                    false
            );
            case ProRasterDerivedService.OP_PRO_THEMATIC_CHLOR_A -> new RasterVisualPreset(
                    "Clorofila-a Pro",
                    "Salida tematica preliminar para clorofila-a. Conviene verla sobre mapa base o costa con opacidad alta.",
                    86,
                    false,
                    false,
                    false
            );
            case ProRasterDerivedService.OP_PRO_THEMATIC_KD490 -> new RasterVisualPreset(
                    "Kd490 Pro",
                    "Salida tematica preliminar para atenuacion difusa. Funciona mejor con relieve costero o linea de costa visible.",
                    86,
                    false,
                    false,
                    false
            );
            case ProRasterDerivedService.OP_PRO_THEMATIC_TURBIDITY_TSM -> new RasterVisualPreset(
                    "Turbidez / TSM Pro",
                    "Salida exploratoria para turbidez o materia en suspension. Conviene usarla con opacidad media-alta.",
                    84,
                    false,
                    false,
                    false
            );
            case ProRasterDerivedService.OP_PRO_QA_BASIC_MASK -> new RasterVisualPreset(
                    "QA basica Pro",
                    "Mascara exploratoria de cobertura valida y pixeles sospechosos. Sirve para revisar rapidamente el raster fuente.",
                    74,
                    false,
                    false,
                    false
            );
            default -> new RasterVisualPreset(
                    "Raster derivado",
                    "Salida raster derivada lista para mezcla visual con opacidad moderada.",
                    80,
                    false,
                    false,
                    false
            );
        };
    }

    public static void placeLayersAtFront(List<Layer> desiredFrontOrder) {
        if (desiredFrontOrder == null || desiredFrontOrder.isEmpty() || AppContext.project() == null) {
            return;
        }

        List<Layer> ordered = new ArrayList<>(AppContext.project().getLayers());
        List<Layer> sanitized = new ArrayList<>();
        for (Layer layer : desiredFrontOrder) {
            if (layer != null && ordered.contains(layer) && !sanitized.contains(layer)) {
                sanitized.add(layer);
            }
        }
        if (sanitized.isEmpty()) {
            return;
        }

        ordered.removeAll(sanitized);
        List<Layer> reversed = new ArrayList<>(sanitized);
        Collections.reverse(reversed);
        for (Layer layer : reversed) {
            ordered.add(0, layer);
        }

        AppContext.project().setLayerOrder(ordered);
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.reorderLayers(new ArrayList<>(ordered));
        }
        if (CatgisDesktopApp.layersPanel != null) {
            AppContext.refreshLayerList();
        }
    }

    public static void normalizeTopographyOverlayOrder() {
        if (AppContext.project() == null || AppContext.project().getLayers() == null) {
            return;
        }

        List<Layer> vectorOverlays = new ArrayList<>();
        List<Layer> derivedRasters = new ArrayList<>();
        for (Layer layer : AppContext.project().getLayers()) {
            if (isTopographyOverlayVector(layer)) {
                vectorOverlays.add(layer);
            } else if (isTopographyDerivedRaster(layer)) {
                derivedRasters.add(layer);
            }
        }

        if (vectorOverlays.isEmpty() && derivedRasters.isEmpty()) {
            return;
        }

        List<Layer> desiredFrontOrder = new ArrayList<>(vectorOverlays);
        desiredFrontOrder.addAll(derivedRasters);
        placeLayersAtFront(desiredFrontOrder);
    }

    public static ShapefileData tryRestoreTransientTopographyVectorLayer(Layer layer) {
        if (layer == null || isRasterLayer(layer)) {
            return null;
        }

        String kind = detectTransientTopographyVectorKind(layer);
        if (kind.isBlank()) {
            return null;
        }

        Layer sourceRaster = resolveSourceRasterForTransientLayer(layer);
        if (sourceRaster == null) {
            return null;
        }

        try {
            if ("contours".equals(kind)) {
                ContourGenerationService.GeneratedContourLayer generated =
                        ContourGenerationService.regenerateContours(layer, sourceRaster);
                if (generated == null || generated.data() == null) {
                    return null;
                }
                ShapefileData projected = projectVectorDataToCurrentProject(generated.layer(), generated.data());
                adoptRecoveredVectorLayer(layer, generated.layer(), projected);
                return projected;
            }

            if ("drainage".equals(kind)) {
                DrainageExtractionService.GeneratedDrainageLayer generated =
                        DrainageExtractionService.generateDrainage(
                                sourceRaster,
                                30,
                                layer.getName(),
                                DrainageExtractionService.AnalysisDetail.BALANCED,
                                DrainageExtractionService.HydrologicConditioning.ADVANCED,
                                150d,
                                DrainageExtractionService.CleanupLevel.STRONG
                        );
                if (generated == null || generated.data() == null) {
                    return null;
                }
                ShapefileData projected = projectVectorDataToCurrentProject(generated.layer(), generated.data());
                adoptRecoveredVectorLayer(layer, generated.layer(), projected);
                return projected;
            }

            TerrainHydrologyAnalysisService.AnalysisRequest request =
                    new TerrainHydrologyAnalysisService.AnalysisRequest(
                            sourceRaster,
                            sourceRaster.getName(),
                            DrainageExtractionService.AnalysisDetail.BALANCED,
                            DrainageExtractionService.HydrologicConditioning.ADVANCED,
                            30,
                            120,
                            false,
                            false,
                            false,
                            false,
                            false,
                            "stream_order".equals(kind),
                            "basins".equals(kind),
                            "outlets".equals(kind),
                            "flow_arrows".equals(kind)
                    );

            TerrainHydrologyAnalysisService.AnalysisResult result = TerrainHydrologyAnalysisService.generateAnalysis(request);
            if (result == null || result.vectorLayers() == null) {
                return null;
            }

            for (TerrainHydrologyAnalysisService.GeneratedVectorLayer generated : result.vectorLayers()) {
                if (generated == null || generated.data() == null || generated.layer() == null) {
                    continue;
                }
                if (matchesTransientTopographyKind(generated.operation(), kind)) {
                    ShapefileData projected = projectVectorDataToCurrentProject(generated.layer(), generated.data());
                    adoptRecoveredVectorLayer(layer, generated.layer(), projected);
                    return projected;
                }
            }
        } catch (Exception ignored) { CatgisLogger.warn("TopographyWorkflowSupport: operation failed", ignored); }

        return null;
    }

    private static boolean isTopographyDerivedRaster(Layer layer) {
        if (!(layer instanceof RasterLayer rasterLayer)) {
            return false;
        }
        if (rasterLayer.isDerivedLayer()) {
            return true;
        }
        String text = ((layer.getName() != null ? layer.getName() : "") + " "
                + (layer.getSourceName() != null ? layer.getSourceName() : "")).toLowerCase(Locale.ROOT);
        return text.contains("hillshade")
                || text.contains("pendiente")
                || text.contains("aspecto")
                || text.contains("direccion de flujo")
                || text.contains("acumulacion de flujo");
    }

    public static boolean isTopographyOverlayVector(Layer layer) {
        if (layer == null || isRasterLayer(layer)) {
            return false;
        }
        String text = ((layer.getName() != null ? layer.getName() : "") + " "
                + (layer.getSourceName() != null ? layer.getSourceName() : "")).toLowerCase(Locale.ROOT);
        return text.contains("curvas de nivel")
                || text.startsWith("curvas ")
                || text.contains(" escorrentias")
                || text.startsWith("escorrentias")
                || text.contains("orden de cauces")
                || text.contains("cuencas")
                || text.contains("subcuencas")
                || text.contains("outlets")
                || text.contains("flechas de flujo")
                || text.contains("cuenca por outlet")
                || text.contains("outlet ajustado")
                || text.contains("riesgo preliminar")
                || text.contains("riesgo booleano");
    }

    private static String detectTransientTopographyVectorKind(Layer layer) {
        String text = ((layer.getName() != null ? layer.getName() : "") + " "
                + (layer.getSourceName() != null ? layer.getSourceName() : "")).toLowerCase(Locale.ROOT);
        if (text.contains("curvas de nivel") || text.startsWith("curvas ")) {
            return "contours";
        }
        if (text.contains("escorrentias")) {
            return "drainage";
        }
        if (text.contains("orden de cauces")) {
            return "stream_order";
        }
        if (text.contains("flechas de flujo")) {
            return "flow_arrows";
        }
        if (text.contains("outlets")) {
            return "outlets";
        }
        if (text.contains("cuencas") || text.contains("subcuencas")) {
            return "basins";
        }
        return "";
    }

    private static boolean matchesTransientTopographyKind(String operation, String expectedKind) {
        if (expectedKind == null || expectedKind.isBlank()) {
            return false;
        }
        if ("stream_order".equals(expectedKind)) {
            return TerrainHydrologyAnalysisService.OP_STREAM_ORDER_LINES.equalsIgnoreCase(operation);
        }
        return expectedKind.equalsIgnoreCase(operation);
    }

    private static Layer resolveSourceRasterForTransientLayer(Layer layer) {
        if (AppContext.project() == null || layer == null) {
            return null;
        }

        List<String> candidates = new ArrayList<>();
        String layerName = layer.getName() != null ? layer.getName().trim() : "";
        int dashIndex = layerName.indexOf(" - ");
        if (dashIndex >= 0 && dashIndex + 3 < layerName.length()) {
            candidates.add(layerName.substring(dashIndex + 3).trim());
        }

        String sourceName = layer.getSourceName() != null ? layer.getSourceName().trim() : "";
        String sourceLower = sourceName.toLowerCase(Locale.ROOT);
        String[] markers = new String[]{
                " derivadas de ",
                " derivado de ",
                " desde ",
                " de "
        };
        for (String marker : markers) {
            int idx = sourceLower.lastIndexOf(marker.trim().toLowerCase(Locale.ROOT));
            if (idx >= 0) {
                String candidate = sourceName.substring(Math.min(sourceName.length(), idx + marker.length())).trim();
                if (!candidate.isBlank()) {
                    candidates.add(candidate);
                }
            }
        }

        for (String candidate : candidates) {
            Layer match = findRasterLayerByName(sanitizeRasterCandidate(candidate));
            if (match != null) {
                return match;
            }
        }

        return resolvePreferredRasterLayer();
    }

    private static Layer findRasterLayerByName(String name) {
        if (name == null || name.isBlank() || AppContext.project() == null) {
            return null;
        }

        for (Layer candidate : AppContext.project().getLayers()) {
            if (candidate != null && isRasterLayer(candidate) && name.equalsIgnoreCase(candidate.getName())) {
                return candidate;
            }
        }
        for (Layer candidate : AppContext.project().getLayers()) {
            if (candidate != null && isRasterLayer(candidate)
                    && candidate.getName() != null
                    && candidate.getName().toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT))) {
                return candidate;
            }
        }
        return null;
    }

    private static String sanitizeRasterCandidate(String candidate) {
        if (candidate == null) {
            return "";
        }
        String trimmed = candidate.trim();
        int metadataStart = trimmed.indexOf(';');
        if (metadataStart >= 0) {
            trimmed = trimmed.substring(0, metadataStart).trim();
        }
        return trimmed;
    }

    public static boolean isTransientTopographyVector(Layer layer) {
        return layer != null && !detectTransientTopographyVectorKind(layer).isBlank();
    }

    private static RasterOperationalContext resolveRasterOperationalContext(Layer layer) {
        if (!isRasterLayer(layer)) {
            return null;
        }

        LocalRasterData data = CatgisDesktopApp.mapPanel != null ? CatgisDesktopApp.mapPanel.getRasterData(layer) : null;
        if (data == null || data.getEnvelope() == null || data.getEnvelope().isNull()
                || data.getWidth() <= 0 || data.getHeight() <= 0) {
            return null;
        }

        Envelope envelope = data.getEnvelope();
        String crsCode = CRSDefinitions.normalizeCode(
                data.getDisplayCRS() != null && !data.getDisplayCRS().isBlank()
                        ? data.getDisplayCRS()
                        : data.getSourceCRS()
        );
        if (crsCode.isBlank() && layer != null) {
            crsCode = CRSDefinitions.normalizeCode(layer.getSourceCRS());
        }
        if (crsCode.isBlank() && AppContext.project() != null) {
            crsCode = CRSDefinitions.normalizeCode(AppContext.project().getProjectCRS());
        }
        if (crsCode.isBlank()) {
            crsCode = "EPSG:4326";
        }

        double cellWidthWorld = Math.abs(envelope.getWidth() / Math.max(1, data.getWidth()));
        double cellHeightWorld = Math.abs(envelope.getHeight() / Math.max(1, data.getHeight()));
        CRSDefinitions.CrsTechnicalDetails crsDetails = CRSDefinitions.describe(crsCode);

        double cellWidthMeters = Double.NaN;
        double cellHeightMeters = Double.NaN;
        if (crsDetails.geographic()) {
            double centerLatitude = (envelope.getMinY() + envelope.getMaxY()) * 0.5d;
            double cosine = Math.cos(Math.toRadians(Math.max(-85d, Math.min(85d, centerLatitude))));
            cellWidthMeters = cellWidthWorld * (111320d * Math.max(0.1d, Math.abs(cosine)));
            cellHeightMeters = cellHeightWorld * 110540d;
        } else if (crsDetails.unit() != null) {
            String unit = crsDetails.unit().toLowerCase(Locale.ROOT);
            if (unit.contains("met") || unit.equals("m")) {
                cellWidthMeters = cellWidthWorld;
                cellHeightMeters = cellHeightWorld;
            }
        }

        double nominalCellSizeMeters = Double.isFinite(cellWidthMeters) && Double.isFinite(cellHeightMeters)
                ? Math.max(Math.abs(cellWidthMeters), Math.abs(cellHeightMeters))
                : Double.NaN;
        return new RasterOperationalContext(
                crsCode,
                data.getWidth(),
                data.getHeight(),
                nominalCellSizeMeters,
                data.isPreviewMode()
        );
    }

    private static String describeScaleAdvice(double nominalCellSizeMeters, String useCase) {
        String baseAdvice;
        if (!Double.isFinite(nominalCellSizeMeters) || nominalCellSizeMeters <= 0d) {
            baseAdvice = "CATGIS no pudo estimar el tamano de celda en metros. Usa el DEM con criterio y valida el nivel de detalle antes de interpretar resultados finos.";
        } else if (nominalCellSizeMeters <= 5d) {
            baseAdvice = "Resolucion alta para trabajo local. Sirve bien para curvas, drenaje y escenarios preliminares de detalle.";
        } else if (nominalCellSizeMeters <= 15d) {
            baseAdvice = "Resolucion buena para relieve local, drenaje y cuencas preliminares.";
        } else if (nominalCellSizeMeters <= 40d) {
            baseAdvice = "Resolucion intermedia. Funciona bien para patrones generales, pero no conviene exigir microdetalle.";
        } else {
            baseAdvice = "Resolucion gruesa. Usala para lectura territorial general; evita interpretar detalle fino de cauces o anegamiento parcelario.";
        }
        return baseAdvice + " " + defaultUseCaseAdvice(useCase);
    }

    private static String defaultUseCaseAdvice(String useCase) {
        String normalizedUseCase = useCase != null ? useCase.trim().toLowerCase(Locale.ROOT) : "";
        return switch (normalizedUseCase) {
            case "clip" -> "Recortar el DEM antes del analisis ayuda a sacar mar, bordes y zonas irrelevantes.";
            case "contours" -> "Si la costa mete ruido, usa recorte DEM y luego excluye <= 0 m o define un umbral minimo.";
            case "drainage" -> "Si el drenaje sale muy cuadriculado, recorta el DEM, sube el umbral o baja el detalle esperado.";
            case "basin" -> "Para cuencas por outlet, un DEM recortado y un radio de ajuste corto suelen dar resultados mas coherentes.";
            case "flood" -> "Los escenarios de lluvia de este bloque son aproximaciones preliminares, no reemplazan modelacion hidraulica completa.";
            default -> "Trabaja con el CRS operativo del proyecto y recorta el DEM cuando el area real sea chica respecto del raster completo.";
        };
    }

    private static String formatMetricValue(double value) {
        if (!Double.isFinite(value)) {
            return "?";
        }
        if (value >= 100d) {
            return String.format(Locale.US, "%.0f", value);
        }
        if (value >= 10d) {
            return String.format(Locale.US, "%.1f", value);
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private static void adoptRecoveredVectorLayer(Layer target, Layer source, ShapefileData data) {
        if (target == null || source == null || data == null) {
            return;
        }

        String originalName = target.getName();
        String originalPath = target.getPath();
        String originalGroup = target.getGroupName();
        boolean originalVisible = target.isVisible();
        boolean labelsVisible = target.isLabelsVisible();
        String labelField = target.getLabelField();

        VectorLayerUtils.copyLayerAppearance(source, target);
        target.setName(originalName);
        target.setPath(originalPath);
        target.setGroupName(originalGroup);
        target.setVisible(originalVisible);
        target.setLabelsVisible(labelsVisible);
        target.setLabelField(labelField);
        target.setSourceName(data.getSourceName());
        target.setFeatureCount(data.getFeatureCount());
        target.setSourceCRS(source.getSourceCRS());
    }

    public static ShapefileData projectVectorDataToCurrentProject(Layer sourceLayer, ShapefileData data) {
        if (sourceLayer == null || data == null || AppContext.project() == null) {
            return data;
        }

        if (CadLayerSupport.isCadLayer(sourceLayer)) {
            String cadSource = CRSDefinitions.normalizeCode(sourceLayer.getSourceCRS());
            if (!cadSource.isBlank()) {
                sourceLayer.setSourceCRS(cadSource);
            }
            return data;
        }

        String sourceCrs = CRSDefinitions.normalizeCode(sourceLayer.getSourceCRS());
        String targetCrs = CRSDefinitions.normalizeCode(AppContext.project().getProjectCRS());
        if (sourceCrs.isBlank() || targetCrs.isBlank() || sourceCrs.equalsIgnoreCase(targetCrs)) {
            sourceLayer.setSourceCRS(targetCrs.isBlank() ? sourceCrs : targetCrs);
            if (!targetCrs.isBlank() && shouldNormalizeSchemaCrs(data, targetCrs)) {
                return rebuildDataWithTargetSchema(data, targetCrs);
            }
            return data;
        }

        List<SimpleFeature> features = data.getFeatures();
        if (features == null || features.isEmpty()) {
            sourceLayer.setSourceCRS(targetCrs);
            return data;
        }

        SimpleFeatureType targetSchema = rebuildSchemaForTargetCrs(data.getSchema(), targetCrs);
        List<SimpleFeature> projected = new ArrayList<>();
        Envelope projectedEnvelope = null;
        for (SimpleFeature feature : features) {
            if (feature == null) {
                continue;
            }
            Object geometryObject = feature.getDefaultGeometry();
            if (!(geometryObject instanceof Geometry geometry)) {
                projected.add(feature);
                continue;
            }
            Geometry transformed = CoordinateTransformSupport.reprojectGeometry(geometry, sourceCrs, targetCrs);
            if (transformed == null || transformed.isEmpty()) {
                transformed = (Geometry) geometry.copy();
            }
            projected.add(cloneFeatureWithGeometry(feature, transformed, targetSchema));
            if (projectedEnvelope == null) {
                projectedEnvelope = new Envelope(transformed.getEnvelopeInternal());
            } else {
                projectedEnvelope.expandToInclude(transformed.getEnvelopeInternal());
            }
        }

        sourceLayer.setSourceCRS(targetCrs);
        return new ShapefileData(
                projected,
                projectedEnvelope != null ? projectedEnvelope : data.getEnvelope(),
                data.getSourceName(),
                projected.size(),
                data.getMessage(),
                targetSchema != null ? targetSchema : data.getSchema()
        );
    }

    private static boolean shouldNormalizeSchemaCrs(ShapefileData data, String targetCrs) {
        if (data == null || data.getSchema() == null || targetCrs == null || targetCrs.isBlank()) {
            return false;
        }
        try {
            String schemaCrs = CRSDefinitions.normalizeCode(org.geotools.referencing.CRS.toSRS(
                    data.getSchema().getCoordinateReferenceSystem(),
                    true
            ));
            return schemaCrs.isBlank() || !schemaCrs.equalsIgnoreCase(targetCrs);
        } catch (Exception ex) {
            return true;
        }
    }

    private static ShapefileData rebuildDataWithTargetSchema(ShapefileData data, String targetCrs) {
        if (data == null || data.getFeatures() == null || data.getFeatures().isEmpty()) {
            return data;
        }
        SimpleFeatureType targetSchema = rebuildSchemaForTargetCrs(data.getSchema(), targetCrs);
        if (targetSchema == null) {
            return data;
        }
        List<SimpleFeature> cloned = new ArrayList<>();
        for (SimpleFeature feature : data.getFeatures()) {
            if (feature == null) {
                continue;
            }
            Object geometryObject = feature.getDefaultGeometry();
            Geometry geometry = geometryObject instanceof Geometry g ? (Geometry) g.copy() : null;
            cloned.add(cloneFeatureWithGeometry(feature, geometry, targetSchema));
        }
        return new ShapefileData(
                cloned,
                data.getEnvelope(),
                data.getSourceName(),
                cloned.size(),
                data.getMessage(),
                targetSchema
        );
    }

    private static SimpleFeature cloneFeatureWithGeometry(SimpleFeature sourceFeature, Geometry geometry, SimpleFeatureType targetSchema) {
        SimpleFeatureType featureType = targetSchema != null ? targetSchema : sourceFeature.getFeatureType();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
        int attributeCount = sourceFeature.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            Object value = sourceFeature.getAttribute(i);
            if (value instanceof Geometry) {
                builder.add(geometry != null ? geometry : ((Geometry) value).copy());
            } else {
                builder.add(value);
            }
        }
        return builder.buildFeature(sourceFeature.getID());
    }

    private static SimpleFeatureType rebuildSchemaForTargetCrs(SimpleFeatureType sourceSchema, String targetCrs) {
        if (sourceSchema == null) {
            return null;
        }
        try {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName(sourceSchema.getName());
            if (sourceSchema.getDescription() != null) {
                builder.setDescription(sourceSchema.getDescription());
            }
            String defaultGeometryName = sourceSchema.getGeometryDescriptor() != null
                    ? sourceSchema.getGeometryDescriptor().getLocalName()
                    : null;
            for (var descriptor : sourceSchema.getAttributeDescriptors()) {
                if (descriptor.equals(sourceSchema.getGeometryDescriptor())) {
                    if (targetCrs != null && !targetCrs.isBlank()) {
                        builder.setCRS(CRSDefinitions.decode(targetCrs, true));
                    }
                    builder.add(descriptor.getLocalName(), descriptor.getType().getBinding());
                } else {
                    builder.add(descriptor.getLocalName(), descriptor.getType().getBinding());
                }
            }
            if (defaultGeometryName != null && !defaultGeometryName.isBlank()) {
                builder.setDefaultGeometry(defaultGeometryName);
            }
            return builder.buildFeatureType();
        } catch (Exception ignored) {
            return sourceSchema;
        }
    }

    public record SelectedProfileLine(Geometry geometry, String sourceCrs, String description) {
    }

    public record RasterVisualPreset(String title,
                                     String description,
                                     int recommendedOpacityPercent,
                                     boolean grayscale,
                                     boolean autoContrast,
                                     boolean allowBandSelection) {
    }

    private record RasterOperationalContext(String crsCode,
                                            int width,
                                            int height,
                                            double nominalCellSizeMeters,
                                            boolean previewMode) {
    }
}
