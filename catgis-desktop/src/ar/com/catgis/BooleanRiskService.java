package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class BooleanRiskService {

    public static final String OP_SLOPE_BOOLEAN_MASK = "slope_boolean_mask";
    public static final String OP_SOIL_BOOLEAN_MASK = "soil_boolean_mask";
    public static final String OP_PRELIMINARY_BOOLEAN_RISK = "preliminary_boolean_risk";

    private static final int TRANSPARENT_ARGB = new Color(0, 0, 0, 0).getRGB();
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private BooleanRiskService() {
    }

    public static RiskResult generateRisk(RiskRequest request) throws Exception {
        validateRequest(request);

        DrainageExtractionService.HydrologyGrid demGrid = DrainageExtractionService.analyzeHydrologyGrid(
                request.demRasterLayer(),
                request.detail(),
                request.conditioning()
        );
        GridCoverage2D soilCoverage = RasterCoverageSupport.readCoverageNative(request.soilRasterLayer());
        SoilRaster soilRaster = SoilRaster.from(soilCoverage, request.soilRasterLayer());
        String parameterSpec = buildParameterSpec(request, demGrid.sourceCrsCode());

        boolean[][] slopeMask = new boolean[demGrid.height()][demGrid.width()];
        boolean[][] soilMask = new boolean[demGrid.height()][demGrid.width()];
        boolean[][] riskMask = new boolean[demGrid.height()][demGrid.width()];

        int intersectingSoilSamples = 0;
        int positiveSlopeCells = 0;
        int positiveSoilCells = 0;
        int positiveRiskCells = 0;

        for (int row = 0; row < demGrid.height(); row++) {
            for (int col = 0; col < demGrid.width(); col++) {
                if (!demGrid.isValidCell(row, col)) {
                    continue;
                }

                boolean slopePass = request.slopeRule().matches(computeSlopeDegrees(demGrid, row, col));

                Coordinate center = demGrid.worldCoordinate(row, col);
                double soilValue = center != null ? soilRaster.sample(center.x, center.y, demGrid.sourceCrsCode()) : Double.NaN;
                boolean hasSoilValue = Double.isFinite(soilValue);
                if (hasSoilValue) {
                    intersectingSoilSamples++;
                }
                boolean soilPass = hasSoilValue && request.soilRule().matches(soilValue);
                boolean riskPass = request.logicMode() == LogicMode.AND
                        ? slopePass && soilPass
                        : slopePass || soilPass;

                slopeMask[row][col] = slopePass;
                soilMask[row][col] = soilPass;
                riskMask[row][col] = riskPass;
                if (slopePass) {
                    positiveSlopeCells++;
                }
                if (soilPass) {
                    positiveSoilCells++;
                }
                if (riskPass) {
                    positiveRiskCells++;
                }
            }
        }

        if (intersectingSoilSamples == 0) {
            throw new IllegalStateException("El raster de suelos no intersecta el DEM operativo del proyecto. Revisa area, CRS y capas seleccionadas.");
        }

        List<GeneratedRasterLayer> rasterLayers = new ArrayList<>();
        if (request.generateSlopeMask()) {
            rasterLayers.add(buildMaskLayer(
                    demGrid,
                    request,
                    "Mascara pendiente",
                    OP_SLOPE_BOOLEAN_MASK,
                    parameterSpec,
                    slopeMask,
                    new Color(249, 115, 22, 212),
                    0.82f,
                    "Mascara booleana de pendiente (1=positivo) derivada de " + request.demRasterLayer().getName()
            ));
        }
        if (request.generateSoilMask()) {
            rasterLayers.add(buildMaskLayer(
                    demGrid,
                    request,
                    "Mascara suelo",
                    OP_SOIL_BOOLEAN_MASK,
                    parameterSpec,
                    soilMask,
                    new Color(120, 53, 15, 208),
                    0.82f,
                    "Mascara booleana de suelo (1=positivo) derivada de " + request.soilRasterLayer().getName()
            ));
        }
        rasterLayers.add(buildMaskLayer(
                demGrid,
                request,
                "Riesgo preliminar",
                OP_PRELIMINARY_BOOLEAN_RISK,
                parameterSpec,
                riskMask,
                new Color(220, 38, 38, 220),
                0.88f,
                buildRiskSourceName(request)
        ));

        GeneratedVectorLayer vectorLayer = null;
        if (request.vectorizePositiveZones()) {
            vectorLayer = buildRiskVectorLayer(demGrid, request, riskMask);
        }

        double positiveArea = demGrid.cellAreaSquareMeters() > 0d
                ? positiveRiskCells * demGrid.cellAreaSquareMeters()
                : Double.NaN;
        int vectorFeatureCount = vectorLayer != null && vectorLayer.data() != null
                ? vectorLayer.data().getFeatureCount()
                : 0;

        return new RiskResult(
                demGrid,
                rasterLayers,
                vectorLayer,
                positiveSlopeCells,
                positiveSoilCells,
                positiveRiskCells,
                positiveArea,
                intersectingSoilSamples,
                vectorFeatureCount
        );
    }

    public static LocalRasterData regenerateDerivedRasterData(RasterLayer layer) throws Exception {
        if (layer == null || !layer.isDerivedLayer()) {
            throw new IllegalArgumentException("La capa raster no es derivada.");
        }

        String operation = layer.getDerivedOperation();
        if (!OP_SLOPE_BOOLEAN_MASK.equalsIgnoreCase(operation)
                && !OP_SOIL_BOOLEAN_MASK.equalsIgnoreCase(operation)
                && !OP_PRELIMINARY_BOOLEAN_RISK.equalsIgnoreCase(operation)) {
            throw new IllegalArgumentException("La capa raster no corresponde a riesgo booleano preliminar.");
        }

        Map<String, String> parameters = parseParameterSpec(layer.getDerivedParameters());
        RasterLayer demLayer = new RasterLayer(
                parameters.getOrDefault("demName", layer.getName()),
                layer.getPath()
        );
        demLayer.setSourceCRS(parameters.getOrDefault("demCrs", layer.getSourceCRS()));
        demLayer.setSourceName(parameters.getOrDefault("demName", layer.getName()));

        String soilPath = parameters.getOrDefault("soilPath", "").trim();
        if (soilPath.isBlank()) {
            throw new IllegalStateException("La capa derivada de riesgo no conserva la ruta del raster de suelos.");
        }

        RasterLayer soilLayer = new RasterLayer(
                parameters.getOrDefault("soilName", new File(soilPath).getName()),
                soilPath
        );
        soilLayer.setSourceCRS(parameters.getOrDefault("soilCrs", ""));
        soilLayer.setSourceName(parameters.getOrDefault("soilName", soilLayer.getName()));

        RiskRequest request = new RiskRequest(
                demLayer,
                soilLayer,
                parameters.getOrDefault("baseName", layer.getName()),
                parseDetail(parameters.get("detail")),
                parseConditioning(parameters.get("conditioning")),
                RiskRule.from(
                        parseComparisonMode(parameters.get("slopeMode")),
                        parseDouble(parameters.get("slopeA"), 12d),
                        parseDouble(parameters.get("slopeB"), 25d)
                ),
                RiskRule.from(
                        parseComparisonMode(parameters.get("soilMode")),
                        parseDouble(parameters.get("soilA"), 250d),
                        parseDouble(parameters.get("soilB"), 400d)
                ),
                parseLogicMode(parameters.get("logic")),
                OP_SLOPE_BOOLEAN_MASK.equalsIgnoreCase(operation),
                OP_SOIL_BOOLEAN_MASK.equalsIgnoreCase(operation),
                false
        );

        RiskResult result = generateRisk(request);
        for (GeneratedRasterLayer generated : result.rasterLayers()) {
            if (generated.operation().equalsIgnoreCase(operation)) {
                return generated.data();
            }
        }
        throw new IllegalStateException("No se pudo regenerar el raster derivado de riesgo: " + operation);
    }

    private static void validateRequest(RiskRequest request) {
        if (request == null || !(request.demRasterLayer() instanceof RasterLayer)) {
            throw new IllegalArgumentException("Debes indicar un DEM raster valido.");
        }
        if (request.demRasterLayer() instanceof RasterLayer rasterLayer && rasterLayer.isDerivedLayer()) {
            throw new IllegalArgumentException("Debes elegir un DEM base real, no una capa raster derivada como inundacion, pendiente, aspecto o mascaras.");
        }
        if (!(request.soilRasterLayer() instanceof RasterLayer)) {
            throw new IllegalArgumentException("Debes indicar un raster de suelos valido.");
        }
        if (request.demRasterLayer() == request.soilRasterLayer()) {
            throw new IllegalArgumentException("El DEM y el raster de suelos deben ser capas distintas.");
        }
        if (request.slopeRule() == null) {
            throw new IllegalArgumentException("Debes definir una regla de pendiente.");
        }
        if (request.soilRule() == null) {
            throw new IllegalArgumentException("Debes definir una regla de suelo.");
        }
        request.slopeRule().validate("pendiente");
        request.soilRule().validate("suelo");
        if (request.logicMode() == null) {
            throw new IllegalArgumentException("Debes elegir una logica booleana.");
        }
    }

    private static GeneratedRasterLayer buildMaskLayer(DrainageExtractionService.HydrologyGrid grid,
                                                       RiskRequest request,
                                                       String label,
                                                       String operation,
                                                       String parameterSpec,
                                                       boolean[][] mask,
                                                       Color positiveColor,
                                                       float opacity,
                                                       String sourceName) {
        BufferedImage image = new BufferedImage(grid.width(), grid.height(), BufferedImage.TYPE_INT_ARGB);
        for (int row = 0; row < grid.height(); row++) {
            for (int col = 0; col < grid.width(); col++) {
                if (!grid.isValidCell(row, col) || !mask[row][col]) {
                    image.setRGB(col, row, TRANSPARENT_ARGB);
                    continue;
                }
                image.setRGB(col, row, positiveColor.getRGB());
            }
        }

        RasterLayer layer = new RasterLayer(buildOutputName(label, request), request.demRasterLayer().getPath());
        layer.setVisible(true);
        layer.setSourceCRS(grid.sourceCrsCode());
        layer.setSourceName(sourceName);
        layer.setFeatureCount(1);
        layer.setOpacity(opacity);
        layer.setGrayscale(false);
        layer.setAutoContrast(false);
        layer.setRedBand(0);
        layer.setGreenBand(1);
        layer.setBlueBand(2);
        layer.setRasterMode("derived");
        layer.setDerivedOperation(operation);
        layer.setDerivedParameters(parameterSpec);

        LocalRasterData data = new LocalRasterData(
                image,
                new Envelope(grid.minX(), grid.maxX(), grid.minY(), grid.maxY()),
                Math.min(3, Math.max(1, image.getRaster().getNumBands())),
                true,
                grid.sourceCrsCode(),
                "derived",
                grid.sourceCrsCode()
        );
        return new GeneratedRasterLayer(layer, data, operation);
    }

    private static GeneratedVectorLayer buildRiskVectorLayer(DrainageExtractionService.HydrologyGrid grid,
                                                             RiskRequest request,
                                                             boolean[][] riskMask) throws Exception {
        Geometry dissolved = buildPositiveGeometry(grid, riskMask);
        if (dissolved == null || dissolved.isEmpty()) {
            return null;
        }

        SimpleFeatureType schema = buildPolygonSchema(
                "RiesgoPreliminar",
                grid.sourceCrsCode(),
                "risk_id", Integer.class,
                "zone_name", String.class,
                "risk_val", Integer.class,
                "risk_class", String.class,
                "logic_mode", String.class,
                "slope_rule", String.class,
                "soil_rule", String.class,
                "dem_name", String.class,
                "soil_name", String.class,
                "area_m2", Double.class,
                "area_ha", Double.class
        );

        VectorLayer layer = new VectorLayer(buildOutputName("Zonas riesgo preliminar", request), "");
        layer.setVisible(true);
        layer.setSourceName("Zonas positivas de riesgo preliminar derivadas de "
                + request.demRasterLayer().getName() + " + " + request.soilRasterLayer().getName());
        layer.setSourceCRS(grid.sourceCrsCode());
        layer.setFillColor(new Color(239, 68, 68, 96));
        layer.setBorderColor(new Color(153, 27, 27));
        layer.setPolygonFillStyle(Layer.PolygonFillStyle.SOLID);

        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        Envelope envelope = null;
        int featureId = 1;
        for (int i = 0; i < dissolved.getNumGeometries(); i++) {
            Geometry component = dissolved.getGeometryN(i);
            if (component == null || component.isEmpty()) {
                continue;
            }
            double areaM2 = VectorMeasurementSupport.resolveAreaSquareMeters(layer, component);
            builder.set("the_geom", component);
            builder.set("risk_id", featureId);
            builder.set("zone_name", "Riesgo_" + featureId);
            builder.set("risk_val", 1);
            builder.set("risk_class", "Positivo");
            builder.set("logic_mode", request.logicMode().name());
            builder.set("slope_rule", request.slopeRule().describe("deg"));
            builder.set("soil_rule", request.soilRule().describe("raw"));
            builder.set("dem_name", truncateAttributeValue(request.demRasterLayer().getName()));
            builder.set("soil_name", truncateAttributeValue(request.soilRasterLayer().getName()));
            builder.set("area_m2", Double.isFinite(areaM2) ? areaM2 : null);
            builder.set("area_ha", Double.isFinite(areaM2) ? areaM2 / 10000d : null);
            features.add(builder.buildFeature("risk." + featureId));
            builder.reset();

            if (envelope == null) {
                envelope = new Envelope(component.getEnvelopeInternal());
            } else {
                envelope.expandToInclude(component.getEnvelopeInternal());
            }
            featureId++;
        }

        if (features.isEmpty()) {
            return null;
        }

        ShapefileData data = new ShapefileData(
                features,
                envelope,
                layer.getSourceName(),
                features.size(),
                "Zonas positivas del riesgo preliminar booleano.",
                schema
        );
        VectorLayerUtils.populateFieldConfigs(layer, schema);
        layer.setFeatureCount(features.size());
        File outputFile = writeVectorFile(request, layer, data);
        if (outputFile != null) {
            layer.setPath(outputFile.getAbsolutePath());
        }
        return new GeneratedVectorLayer(layer, data, "risk_vector");
    }

    private static File writeVectorFile(RiskRequest request, Layer layer, ShapefileData data) throws Exception {
        File outputFile = resolveVectorOutputFile(request);
        File parent = outputFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
            throw new IllegalStateException("No se pudo crear la carpeta para guardar las zonas de riesgo preliminar.");
        }
        if (!ExportVectorLayerAction.saveLayerDataToFile(layer, data, outputFile, null, false)) {
            throw new IllegalStateException("No se pudo persistir la salida vectorial de riesgo preliminar.");
        }
        return outputFile;
    }

    private static File resolveVectorOutputFile(RiskRequest request) {
        File baseDirectory = null;
        if (request.demRasterLayer() != null && request.demRasterLayer().getPath() != null && !request.demRasterLayer().getPath().isBlank()) {
            File demFile = new File(request.demRasterLayer().getPath());
            baseDirectory = demFile.getParentFile();
        }
        if (baseDirectory == null || !baseDirectory.exists()) {
            baseDirectory = new File(System.getProperty("java.io.tmpdir", "."), "catgis-risk");
        }
        String baseName = resolveEffectiveBaseName(request);
        return new File(baseDirectory, sanitizeFileStem("riesgo_preliminar_" + baseName + "_zonas") + ".shp");
    }

    private static Geometry buildPositiveGeometry(DrainageExtractionService.HydrologyGrid grid, boolean[][] mask) {
        List<Geometry> rowRuns = new ArrayList<>();
        for (int row = 0; row < grid.height(); row++) {
            int start = -1;
            for (int col = 0; col < grid.width(); col++) {
                boolean positive = grid.isValidCell(row, col) && mask[row][col];
                if (positive && start < 0) {
                    start = col;
                    continue;
                }
                if (!positive && start >= 0) {
                    Geometry run = buildRowRunPolygon(grid, row, start, col - 1);
                    if (run != null && !run.isEmpty()) {
                        rowRuns.add(run);
                    }
                    start = -1;
                }
            }
            if (start >= 0) {
                Geometry run = buildRowRunPolygon(grid, row, start, grid.width() - 1);
                if (run != null && !run.isEmpty()) {
                    rowRuns.add(run);
                }
            }
        }
        if (rowRuns.isEmpty()) {
            return null;
        }
        return UnaryUnionOp.union(rowRuns);
    }

    private static Polygon buildRowRunPolygon(DrainageExtractionService.HydrologyGrid grid,
                                              int row,
                                              int startCol,
                                              int endCol) {
        if (startCol > endCol) {
            return null;
        }
        double minX = grid.minX() + (startCol * grid.cellWidthWorld());
        double maxX = grid.minX() + ((endCol + 1d) * grid.cellWidthWorld());
        double maxY = grid.maxY() - (row * grid.cellHeightWorld());
        double minY = grid.maxY() - ((row + 1d) * grid.cellHeightWorld());
        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(minX, minY),
                new Coordinate(maxX, minY),
                new Coordinate(maxX, maxY),
                new Coordinate(minX, maxY),
                new Coordinate(minX, minY)
        };
        return GEOMETRY_FACTORY.createPolygon(coordinates);
    }

    private static double computeSlopeDegrees(DrainageExtractionService.HydrologyGrid grid, int row, int col) {
        double z1 = sampleElevation(grid, row - 1, col - 1, row, col);
        double z2 = sampleElevation(grid, row - 1, col, row, col);
        double z3 = sampleElevation(grid, row - 1, col + 1, row, col);
        double z4 = sampleElevation(grid, row, col - 1, row, col);
        double z6 = sampleElevation(grid, row, col + 1, row, col);
        double z7 = sampleElevation(grid, row + 1, col - 1, row, col);
        double z8 = sampleElevation(grid, row + 1, col, row, col);
        double z9 = sampleElevation(grid, row + 1, col + 1, row, col);

        double cellX = Math.max(1e-6d, grid.cellWidthMeters());
        double cellY = Math.max(1e-6d, grid.cellHeightMeters());
        double dzdx = ((z3 + (2d * z6) + z9) - (z1 + (2d * z4) + z7)) / (8d * cellX);
        double dzdy = ((z7 + (2d * z8) + z9) - (z1 + (2d * z2) + z3)) / (8d * cellY);
        return Math.toDegrees(Math.atan(Math.sqrt((dzdx * dzdx) + (dzdy * dzdy))));
    }

    private static double sampleElevation(DrainageExtractionService.HydrologyGrid grid,
                                          int row,
                                          int col,
                                          int fallbackRow,
                                          int fallbackCol) {
        if (grid.isValidCell(row, col)) {
            return grid.conditionedElevations()[row][col];
        }
        return grid.conditionedElevations()[fallbackRow][fallbackCol];
    }

    private static String buildParameterSpec(RiskRequest request, String operationalCrsCode) {
        return "detail=" + request.detail().name()
                + ";conditioning=" + request.conditioning().name()
                + ";logic=" + request.logicMode().name()
                + ";slopeMode=" + request.slopeRule().mode().name()
                + ";slopeA=" + formatNumber(request.slopeRule().valueA())
                + ";slopeB=" + formatNumber(request.slopeRule().valueB())
                + ";soilMode=" + request.soilRule().mode().name()
                + ";soilA=" + formatNumber(request.soilRule().valueA())
                + ";soilB=" + formatNumber(request.soilRule().valueB())
                + ";baseName=" + sanitizeParameterValue(request.baseName())
                + ";demName=" + sanitizeParameterValue(request.demRasterLayer().getName())
                + ";demCrs=" + sanitizeParameterValue(operationalCrsCode)
                + ";soilName=" + sanitizeParameterValue(request.soilRasterLayer().getName())
                + ";soilPath=" + sanitizeParameterValue(request.soilRasterLayer().getPath())
                + ";soilCrs=" + sanitizeParameterValue(request.soilRasterLayer().getSourceCRS());
    }

    private static Map<String, String> parseParameterSpec(String spec) {
        Map<String, String> values = new LinkedHashMap<>();
        if (spec == null || spec.isBlank()) {
            return values;
        }
        String[] parts = spec.split(";");
        for (String part : parts) {
            int separator = part.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String key = part.substring(0, separator).trim();
            String value = part.substring(separator + 1).trim();
            values.put(key, value.replace("%3B", ";").replace("%3D", "="));
        }
        return values;
    }

    private static String buildOutputName(String label, RiskRequest request) {
        String baseName = resolveEffectiveBaseName(request);
        return label + " - " + baseName;
    }

    private static String resolveEffectiveBaseName(RiskRequest request) {
        if (request != null && request.baseName() != null && !request.baseName().isBlank()) {
            return request.baseName().trim();
        }
        return "riesgo preliminar";
    }

    private static String buildRiskSourceName(RiskRequest request) {
        return "Riesgo booleano preliminar derivado de " + request.demRasterLayer().getName()
                + " y " + request.soilRasterLayer().getName()
                + " (1=positivo, transparente=sin coincidencia)"
                + "; pendiente=" + request.slopeRule().describe("deg")
                + "; suelo=" + request.soilRule().describe("raw")
                + "; logica=" + request.logicMode().name();
    }

    private static SimpleFeatureType buildPolygonSchema(String name, String sourceCrsCode, Object... attributes) throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(safeTypeName(name));
        if (sourceCrsCode != null && !sourceCrsCode.isBlank()) {
            try {
                CoordinateReferenceSystem crs = CRSDefinitions.decode(sourceCrsCode, true);
                builder.setCRS(crs);
            } catch (Exception ignored) { CatgisLogger.warn("BooleanRiskService: operation failed", ignored); }
        }
        builder.add("the_geom", Geometry.class);
        for (int i = 0; i + 1 < attributes.length; i += 2) {
            builder.add(String.valueOf(attributes[i]), (Class<?>) attributes[i + 1]);
        }
        return builder.buildFeatureType();
    }

    private static String safeTypeName(String value) {
        String text = value != null ? value.trim() : "";
        if (text.isBlank()) {
            return "riesgo_preliminar";
        }
        return text.replaceAll("[^A-Za-z0-9_]+", "_");
    }

    private static String sanitizeParameterValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(";", "%3B").replace("=", "%3D");
    }

    private static String sanitizeFileStem(String value) {
        if (value == null || value.isBlank()) {
            return "riesgo_preliminar";
        }
        return value
                .replaceAll("[\\\\/:*?\"<>|]+", "_")
                .replaceAll("\\s+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");
    }

    private static String truncateAttributeValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= 80 ? trimmed : trimmed.substring(0, 80);
    }

    private static String formatNumber(double value) {
        if (!Double.isFinite(value)) {
            return "0";
        }
        if (Math.rint(value) == value) {
            return String.format(Locale.US, "%.0f", value);
        }
        return String.format(Locale.US, "%.6f", value);
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble((value != null ? value.trim() : "").replace(",", "."));
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static DrainageExtractionService.AnalysisDetail parseDetail(String value) {
        try {
            return value != null && !value.isBlank()
                    ? DrainageExtractionService.AnalysisDetail.valueOf(value.trim())
                    : DrainageExtractionService.AnalysisDetail.BALANCED;
        } catch (Exception ex) {
            return DrainageExtractionService.AnalysisDetail.BALANCED;
        }
    }

    private static DrainageExtractionService.HydrologicConditioning parseConditioning(String value) {
        try {
            return value != null && !value.isBlank()
                    ? DrainageExtractionService.HydrologicConditioning.valueOf(value.trim())
                    : DrainageExtractionService.HydrologicConditioning.ROBUST;
        } catch (Exception ex) {
            return DrainageExtractionService.HydrologicConditioning.ROBUST;
        }
    }

    private static ComparisonMode parseComparisonMode(String value) {
        try {
            return value != null && !value.isBlank()
                    ? ComparisonMode.valueOf(value.trim())
                    : ComparisonMode.GREATER_THAN;
        } catch (Exception ex) {
            return ComparisonMode.GREATER_THAN;
        }
    }

    private static LogicMode parseLogicMode(String value) {
        try {
            return value != null && !value.isBlank()
                    ? LogicMode.valueOf(value.trim())
                    : LogicMode.AND;
        } catch (Exception ex) {
            return LogicMode.AND;
        }
    }

    private static double[] resolveNoDataValues(GridCoverage2D coverage) {
        try {
            Object sampleDimension = coverage.getSampleDimension(0);
            if (sampleDimension == null) {
                return new double[0];
            }
            Object values = sampleDimension.getClass().getMethod("getNoDataValues").invoke(sampleDimension);
            if (values instanceof double[] array) {
                return array;
            }
        } catch (Exception ignored) { CatgisLogger.warn("BooleanRiskService: operation failed", ignored); }
        return new double[0];
    }

    private static boolean isValidRasterValue(double value, double[] noDataValues) {
        if (!Double.isFinite(value) || Math.abs(value) > 1e19) {
            return false;
        }
        if (noDataValues != null) {
            for (double noData : noDataValues) {
                if (Double.isFinite(noData) && Math.abs(noData - value) <= 1e-9d) {
                    return false;
                }
            }
        }
        return true;
    }

    public enum ComparisonMode {
        GREATER_THAN(">"),
        GREATER_OR_EQUAL(">="),
        LESS_THAN("<"),
        LESS_OR_EQUAL("<="),
        BETWEEN("Entre");

        private final String label;

        ComparisonMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum LogicMode {
        AND("AND"),
        OR("OR");

        private final String label;

        LogicMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public record RiskRule(ComparisonMode mode, double valueA, double valueB) {

        public static RiskRule from(ComparisonMode mode, double valueA, double valueB) {
            return new RiskRule(mode != null ? mode : ComparisonMode.GREATER_THAN, valueA, valueB);
        }

        public void validate(String label) {
            if (mode == null) {
                throw new IllegalArgumentException("Debes definir una comparacion de " + label + ".");
            }
            if (!Double.isFinite(valueA)) {
                throw new IllegalArgumentException("El umbral principal de " + label + " no es valido.");
            }
            if (mode == ComparisonMode.BETWEEN && !Double.isFinite(valueB)) {
                throw new IllegalArgumentException("El rango de " + label + " requiere dos valores validos.");
            }
        }

        public boolean matches(double value) {
            if (!Double.isFinite(value)) {
                return false;
            }
            return switch (mode) {
                case GREATER_THAN -> value > valueA;
                case GREATER_OR_EQUAL -> value >= valueA;
                case LESS_THAN -> value < valueA;
                case LESS_OR_EQUAL -> value <= valueA;
                case BETWEEN -> {
                    double min = Math.min(valueA, valueB);
                    double max = Math.max(valueA, valueB);
                    yield value >= min && value <= max;
                }
            };
        }

        public String describe(String units) {
            String unitSuffix = units != null && !units.isBlank() ? " " + units : "";
            return switch (mode) {
                case GREATER_THAN -> ">" + formatNumber(valueA) + unitSuffix;
                case GREATER_OR_EQUAL -> ">=" + formatNumber(valueA) + unitSuffix;
                case LESS_THAN -> "<" + formatNumber(valueA) + unitSuffix;
                case LESS_OR_EQUAL -> "<=" + formatNumber(valueA) + unitSuffix;
                case BETWEEN -> "entre " + formatNumber(Math.min(valueA, valueB)) + " y " + formatNumber(Math.max(valueA, valueB)) + unitSuffix;
            };
        }
    }

    public record RiskRequest(Layer demRasterLayer,
                              Layer soilRasterLayer,
                              String baseName,
                              DrainageExtractionService.AnalysisDetail detail,
                              DrainageExtractionService.HydrologicConditioning conditioning,
                              RiskRule slopeRule,
                              RiskRule soilRule,
                              LogicMode logicMode,
                              boolean generateSlopeMask,
                              boolean generateSoilMask,
                              boolean vectorizePositiveZones) {
    }

    public record GeneratedRasterLayer(RasterLayer layer,
                                       LocalRasterData data,
                                       String operation) {
    }

    public record GeneratedVectorLayer(Layer layer,
                                       ShapefileData data,
                                       String operation) {
    }

    public record RiskResult(DrainageExtractionService.HydrologyGrid demGrid,
                             List<GeneratedRasterLayer> rasterLayers,
                             GeneratedVectorLayer vectorLayer,
                             int positiveSlopeCellCount,
                             int positiveSoilCellCount,
                             int positiveCellCount,
                             double positiveAreaSquareMeters,
                             int intersectingSoilSamples,
                             int vectorFeatureCount) {

        public double positiveAreaHectares() {
            return Double.isFinite(positiveAreaSquareMeters) ? positiveAreaSquareMeters / 10000d : Double.NaN;
        }
    }

    private record SoilRaster(Raster raster,
                              double minX,
                              double maxX,
                              double minY,
                              double maxY,
                              int width,
                              int height,
                              int rasterMinX,
                              int rasterMinY,
                              String sourceCrsCode,
                              double[] noDataValues) {

        static SoilRaster from(GridCoverage2D coverage, Layer fallbackLayer) {
            if (coverage == null || coverage.getRenderedImage() == null || coverage.getEnvelope2D() == null) {
                throw new IllegalStateException("No se pudo leer la cobertura raster del suelo.");
            }
            RenderedImage rendered = coverage.getRenderedImage();
            Raster raster = rendered.getData();
            int width = raster.getWidth();
            int height = raster.getHeight();
            if (width <= 0 || height <= 0) {
                throw new IllegalStateException("El raster de suelo no tiene dimensiones validas.");
            }
            return new SoilRaster(
                    raster,
                    coverage.getEnvelope2D().getMinX(),
                    coverage.getEnvelope2D().getMaxX(),
                    coverage.getEnvelope2D().getMinY(),
                    coverage.getEnvelope2D().getMaxY(),
                    width,
                    height,
                    raster.getMinX(),
                    raster.getMinY(),
                    RasterCoverageSupport.resolveCoverageCrsCode(coverage, fallbackLayer),
                    resolveNoDataValues(coverage)
            );
        }

        double sample(double x, double y, String coordinateCrsCode) {
            double[] sourcePoint = reprojectIfNeeded(x, y, coordinateCrsCode, sourceCrsCode);
            double value = sampleDirect(sourcePoint[0], sourcePoint[1]);
            if (Double.isFinite(value)) {
                return value;
            }
            if (isGeographicCrs(sourceCrsCode)) {
                double swappedValue = sampleDirect(sourcePoint[1], sourcePoint[0]);
                if (Double.isFinite(swappedValue)) {
                    return swappedValue;
                }
            }
            return Double.NaN;
        }

        private double sampleDirect(double x, double y) {
            double widthWorld = maxX - minX;
            double heightWorld = maxY - minY;
            if (!(widthWorld > 0d) || !(heightWorld > 0d)) {
                return Double.NaN;
            }
            double epsilonX = Math.max(Math.abs(widthWorld) * 1e-9d, 1e-9d);
            double epsilonY = Math.max(Math.abs(heightWorld) * 1e-9d, 1e-9d);
            if (x < minX - epsilonX || x > maxX + epsilonX || y < minY - epsilonY || y > maxY + epsilonY) {
                return Double.NaN;
            }
            int col = clamp((int) Math.floor(((x - minX) / widthWorld) * width), 0, width - 1);
            int row = clamp((int) Math.floor(((maxY - y) / heightWorld) * height), 0, height - 1);
            double value = raster.getSampleDouble(rasterMinX + col, rasterMinY + row, 0);
            return isValidRasterValue(value, noDataValues) ? value : Double.NaN;
        }

        private double[] reprojectIfNeeded(double x, double y, String sourceCode, String targetCode) {
            String normalizedSource = CRSDefinitions.normalizeCode(sourceCode);
            String normalizedTarget = CRSDefinitions.normalizeCode(targetCode);
            if (normalizedSource.isBlank() || normalizedTarget.isBlank() || normalizedSource.equalsIgnoreCase(normalizedTarget)) {
                return new double[]{x, y};
            }
            double[] transformed = CoordinateTransformSupport.transformPoint(x, y, normalizedSource, normalizedTarget);
            return transformed != null && transformed.length >= 2 ? transformed : new double[]{x, y};
        }

        private boolean isGeographicCrs(String code) {
            String normalized = CRSDefinitions.normalizeCode(code);
            return "EPSG:4326".equalsIgnoreCase(normalized)
                    || "EPSG:84".equalsIgnoreCase(normalized)
                    || "CRS:84".equalsIgnoreCase(normalized)
                    || "EPSG:4674".equalsIgnoreCase(normalized)
                    || "EPSG:4190".equalsIgnoreCase(normalized)
                    || "EPSG:4221".equalsIgnoreCase(normalized)
                    || "EPSG:4490".equalsIgnoreCase(normalized);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
