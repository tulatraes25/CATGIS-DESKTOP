package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TerrainHydrologyAnalysisService {

    public static final String OP_HILLSHADE = "hillshade";
    public static final String OP_SLOPE = "slope";
    public static final String OP_ASPECT = "aspect";
    public static final String OP_FLOW_DIRECTION = "flow_direction";
    public static final String OP_FLOW_ACCUMULATION = "flow_accumulation";
    public static final String OP_STREAM_ORDER_LINES = "stream_order_lines";

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final double HILLSHADE_AZIMUTH_DEGREES = 315d;
    private static final double HILLSHADE_ALTITUDE_DEGREES = 45d;
    private static final int TRANSPARENT_ARGB = new Color(0, 0, 0, 0).getRGB();

    private TerrainHydrologyAnalysisService() {
    }

    public static AnalysisResult generateAnalysis(AnalysisRequest request) throws Exception {
        validateRequest(request);
        DrainageExtractionService.HydrologyGrid grid = DrainageExtractionService.analyzeHydrologyGrid(
                request.rasterLayer(),
                request.detail(),
                request.conditioning()
        );
        String parameterSpec = buildParameterSpec(request);

        List<GeneratedRasterLayer> rasterLayers = new ArrayList<>();
        if (request.generateHillshade()) {
            rasterLayers.add(buildHillshadeLayer(grid, request, parameterSpec));
        }
        if (request.generateSlope()) {
            rasterLayers.add(buildSlopeLayer(grid, request, parameterSpec));
        }
        if (request.generateAspect()) {
            rasterLayers.add(buildAspectLayer(grid, request, parameterSpec));
        }
        if (request.generateFlowDirection()) {
            rasterLayers.add(buildFlowDirectionLayer(grid, request, parameterSpec));
        }
        if (request.generateFlowAccumulation()) {
            rasterLayers.add(buildFlowAccumulationLayer(grid, request, parameterSpec));
        }

        List<GeneratedVectorLayer> vectorLayers = new ArrayList<>();
        if (request.generateBasins()
                || request.generateOutlets()
                || request.generateFlowArrows()
                || request.generateStreamOrderLines()) {
            FlowNetwork network = buildFlowNetwork(grid, request.channelThreshold(), request.minimumBasinCells());
            if (request.generateBasins()) {
                GeneratedVectorLayer basins = buildBasinLayer(grid, request, network);
                if (basins != null) {
                    vectorLayers.add(basins);
                }
            }
            if (request.generateOutlets()) {
                GeneratedVectorLayer outlets = buildOutletLayer(grid, request, network);
                if (outlets != null) {
                    vectorLayers.add(outlets);
                }
            }
            if (request.generateFlowArrows()) {
                GeneratedVectorLayer arrows = buildFlowArrowLayer(grid, request, network);
                if (arrows != null) {
                    vectorLayers.add(arrows);
                }
            }
            if (request.generateStreamOrderLines()) {
                GeneratedVectorLayer streamOrders = buildStreamOrderLineLayer(grid, request, network);
                if (streamOrders != null) {
                    vectorLayers.add(streamOrders);
                }
            }
        }

        return new AnalysisResult(grid, rasterLayers, vectorLayers);
    }

    public static LocalRasterData regenerateDerivedRasterData(RasterLayer layer) throws Exception {
        if (layer == null || !layer.isDerivedLayer()) {
            throw new IllegalArgumentException("La capa raster no es derivada.");
        }
        Map<String, String> parameters = parseParameterSpec(layer.getDerivedParameters());
        DrainageExtractionService.AnalysisDetail detail = DrainageExtractionService.AnalysisDetail.valueOf(
                parameters.getOrDefault("detail", DrainageExtractionService.AnalysisDetail.BALANCED.name())
        );
        DrainageExtractionService.HydrologicConditioning conditioning = DrainageExtractionService.HydrologicConditioning.valueOf(
                parameters.getOrDefault("conditioning", DrainageExtractionService.HydrologicConditioning.ROBUST.name())
        );
        AnalysisRequest request = new AnalysisRequest(
                layer,
                parameters.getOrDefault("baseName", layer.getName()),
                detail,
                conditioning,
                parsePositiveInt(parameters.get("threshold"), 30, 2),
                parsePositiveInt(parameters.get("minBasinCells"), 120, 8),
                OP_HILLSHADE.equalsIgnoreCase(layer.getDerivedOperation()),
                OP_SLOPE.equalsIgnoreCase(layer.getDerivedOperation()),
                OP_ASPECT.equalsIgnoreCase(layer.getDerivedOperation()),
                OP_FLOW_DIRECTION.equalsIgnoreCase(layer.getDerivedOperation()),
                OP_FLOW_ACCUMULATION.equalsIgnoreCase(layer.getDerivedOperation()),
                false,
                false,
                false,
                false
        );
        AnalysisResult result = generateAnalysis(request);
        for (GeneratedRasterLayer generated : result.rasterLayers()) {
            if (generated.operation().equalsIgnoreCase(layer.getDerivedOperation())) {
                return generated.data();
            }
        }
        throw new IllegalStateException("No se pudo regenerar el raster derivado: " + layer.getDerivedOperation());
    }

    private static void validateRequest(AnalysisRequest request) {
        if (request == null || !(request.rasterLayer() instanceof RasterLayer)) {
            throw new IllegalArgumentException("Debes indicar un DEM raster valido.");
        }
        if (!request.generateHillshade()
                && !request.generateSlope()
                && !request.generateAspect()
                && !request.generateFlowDirection()
                && !request.generateFlowAccumulation()
                && !request.generateStreamOrderLines()
                && !request.generateBasins()
                && !request.generateOutlets()
                && !request.generateFlowArrows()) {
            throw new IllegalArgumentException("Selecciona al menos una salida topohidrologica.");
        }
        if (request.channelThreshold() < 2) {
            throw new IllegalArgumentException("El umbral de acumulacion debe ser 2 o mayor.");
        }
        if (request.minimumBasinCells() < 8) {
            throw new IllegalArgumentException("El tamano minimo de cuenca debe ser 8 celdas o mayor.");
        }
    }

    private static GeneratedRasterLayer buildHillshadeLayer(DrainageExtractionService.HydrologyGrid grid,
                                                            AnalysisRequest request,
                                                            String parameterSpec) {
        BufferedImage image = new BufferedImage(grid.width(), grid.height(), BufferedImage.TYPE_INT_ARGB);
        double zenithRad = Math.toRadians(90d - HILLSHADE_ALTITUDE_DEGREES);
        double azimuthMath = Math.toRadians(360d - HILLSHADE_AZIMUTH_DEGREES + 90d);

        for (int row = 0; row < grid.height(); row++) {
            for (int col = 0; col < grid.width(); col++) {
                if (!grid.isValidCell(row, col)) {
                    image.setRGB(col, row, TRANSPARENT_ARGB);
                    continue;
                }
                DerivativeMetrics metrics = computeDerivativeMetrics(grid, row, col);
                double shade = (Math.cos(zenithRad) * Math.cos(metrics.slopeRadians()))
                        + (Math.sin(zenithRad) * Math.sin(metrics.slopeRadians())
                        * Math.cos(azimuthMath - metrics.aspectMathRadians()));
                int value = (int) Math.round(Math.max(0d, Math.min(255d, shade * 255d)));
                image.setRGB(col, row, new Color(value, value, value).getRGB());
            }
        }
        return buildDerivedRasterLayer(grid, request, "Hillshade", OP_HILLSHADE, parameterSpec, image, false, false);
    }

    private static GeneratedRasterLayer buildSlopeLayer(DrainageExtractionService.HydrologyGrid grid,
                                                        AnalysisRequest request,
                                                        String parameterSpec) {
        BufferedImage image = new BufferedImage(grid.width(), grid.height(), BufferedImage.TYPE_INT_ARGB);
        double[][] slopeDegrees = new double[grid.height()][grid.width()];
        double maxSlope = 0d;

        for (int row = 0; row < grid.height(); row++) {
            for (int col = 0; col < grid.width(); col++) {
                if (!grid.isValidCell(row, col)) {
                    continue;
                }
                double slope = Math.toDegrees(computeDerivativeMetrics(grid, row, col).slopeRadians());
                slopeDegrees[row][col] = slope;
                maxSlope = Math.max(maxSlope, slope);
            }
        }

        double normalizedMax = Math.max(12d, maxSlope);
        for (int row = 0; row < grid.height(); row++) {
            for (int col = 0; col < grid.width(); col++) {
                if (!grid.isValidCell(row, col)) {
                    image.setRGB(col, row, TRANSPARENT_ARGB);
                    continue;
                }
                double ratio = Math.max(0d, Math.min(1d, Math.pow(slopeDegrees[row][col] / normalizedMax, 0.78d)));
                Color color = interpolateColor(
                        ratio,
                        new Color(246, 252, 245),
                        new Color(172, 221, 164),
                        new Color(255, 232, 146),
                        new Color(244, 144, 99),
                        new Color(145, 52, 36)
                );
                image.setRGB(col, row, color.getRGB());
            }
        }
        return buildDerivedRasterLayer(grid, request, "Pendiente", OP_SLOPE, parameterSpec, image, false, false);
    }

    private static GeneratedRasterLayer buildAspectLayer(DrainageExtractionService.HydrologyGrid grid,
                                                         AnalysisRequest request,
                                                         String parameterSpec) {
        BufferedImage image = new BufferedImage(grid.width(), grid.height(), BufferedImage.TYPE_INT_ARGB);
        for (int row = 0; row < grid.height(); row++) {
            for (int col = 0; col < grid.width(); col++) {
                if (!grid.isValidCell(row, col)) {
                    image.setRGB(col, row, TRANSPARENT_ARGB);
                    continue;
                }
                DerivativeMetrics metrics = computeDerivativeMetrics(grid, row, col);
                double slopeDegrees = Math.toDegrees(metrics.slopeRadians());
                if (slopeDegrees < 1.5d) {
                    image.setRGB(col, row, new Color(197, 203, 214).getRGB());
                    continue;
                }
                float hue = (float) (metrics.aspectDegrees() / 360d);
                float saturation = (float) Math.max(0.35d, Math.min(0.92d, slopeDegrees / 42d));
                float brightness = (float) Math.max(0.62d, 0.9d - (slopeDegrees / 180d) * 0.18d);
                Color color = Color.getHSBColor(hue, saturation, brightness);
                image.setRGB(col, row, color.getRGB());
            }
        }
        return buildDerivedRasterLayer(grid, request, "Aspecto", OP_ASPECT, parameterSpec, image, false, false);
    }

    private static GeneratedRasterLayer buildFlowDirectionLayer(DrainageExtractionService.HydrologyGrid grid,
                                                                AnalysisRequest request,
                                                                String parameterSpec) {
        BufferedImage image = new BufferedImage(grid.width(), grid.height(), BufferedImage.TYPE_INT_ARGB);
        Color[] palette = new Color[]{
                new Color(59, 130, 246),
                new Color(37, 99, 235),
                new Color(147, 51, 234),
                new Color(234, 88, 12),
                new Color(239, 68, 68),
                new Color(5, 150, 105),
                new Color(14, 165, 233),
                new Color(124, 58, 237)
        };
        for (int row = 0; row < grid.height(); row++) {
            for (int col = 0; col < grid.width(); col++) {
                if (!grid.isValidCell(row, col)) {
                    image.setRGB(col, row, TRANSPARENT_ARGB);
                    continue;
                }
                int index = grid.linearIndex(row, col);
                int next = grid.downstream()[index];
                if (next < 0) {
                    image.setRGB(col, row, new Color(15, 23, 42).getRGB());
                    continue;
                }
                int nextRow = next / grid.width();
                int nextCol = next % grid.width();
                int directionClass = resolveDirectionClass(row, col, nextRow, nextCol);
                image.setRGB(col, row, palette[Math.max(0, Math.min(palette.length - 1, directionClass))].getRGB());
            }
        }
        return buildDerivedRasterLayer(grid, request, "Direccion de flujo", OP_FLOW_DIRECTION, parameterSpec, image, false, false);
    }

    private static GeneratedRasterLayer buildFlowAccumulationLayer(DrainageExtractionService.HydrologyGrid grid,
                                                                   AnalysisRequest request,
                                                                   String parameterSpec) {
        BufferedImage image = new BufferedImage(grid.width(), grid.height(), BufferedImage.TYPE_INT_ARGB);
        double maxLog = 1d;
        double[] logValues = new double[grid.accumulation().length];
        for (int i = 0; i < grid.accumulation().length; i++) {
            double log = Math.log1p(Math.max(0L, grid.accumulation()[i]));
            logValues[i] = log;
            maxLog = Math.max(maxLog, log);
        }

        for (int row = 0; row < grid.height(); row++) {
            for (int col = 0; col < grid.width(); col++) {
                if (!grid.isValidCell(row, col)) {
                    image.setRGB(col, row, TRANSPARENT_ARGB);
                    continue;
                }
                int index = grid.linearIndex(row, col);
                double ratio = Math.max(0d, Math.min(1d, Math.pow(logValues[index] / maxLog, 0.58d)));
                Color color = interpolateColor(
                        ratio,
                        new Color(248, 245, 229),
                        new Color(210, 234, 241),
                        new Color(108, 181, 222),
                        new Color(33, 113, 181),
                        new Color(8, 48, 107)
                );
                image.setRGB(col, row, color.getRGB());
            }
        }
        return buildDerivedRasterLayer(grid, request, "Acumulacion de flujo", OP_FLOW_ACCUMULATION, parameterSpec, image, false, false);
    }

    private static GeneratedVectorLayer buildBasinLayer(DrainageExtractionService.HydrologyGrid grid,
                                                        AnalysisRequest request,
                                                        FlowNetwork network) throws Exception {
        if (network.retainedOutletIds().isEmpty()) {
            return null;
        }
        SimpleFeatureType schema = buildPolygonSchema("Cuencas", grid.sourceCrsCode(),
                "basin_id", Integer.class,
                "basin_name", String.class,
                "basin_class", String.class,
                "subbasin_id", Integer.class,
                "outlet_id", Integer.class,
                "cell_count", Integer.class,
                "area_m2", Double.class,
                "area_ha", Double.class,
                "stream_order", Integer.class
        );

        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        Envelope envelope = null;
        int largest = network.retainedOutletIds().stream()
                .map(id -> network.basinCellCounts().getOrDefault(id, 0))
                .max(Integer::compareTo)
                .orElse(1);

        int featureId = 1;
        for (int outletId : network.retainedOutletIds()) {
            List<Integer> basinCells = network.cellsByOutlet().get(outletId);
            if (basinCells == null || basinCells.isEmpty()) {
                continue;
            }
            Geometry geometry = buildBasinGeometry(grid, basinCells);
            if (geometry == null || geometry.isEmpty()) {
                continue;
            }
            int cellCount = network.basinCellCounts().getOrDefault(outletId, 0);
            double area = grid.cellAreaSquareMeters() > 0d ? cellCount * grid.cellAreaSquareMeters() : Double.NaN;
            String basinClass = classifyBasin(cellCount, largest);

            builder.set("the_geom", geometry);
            builder.set("basin_id", featureId);
            builder.set("basin_name", buildBasinName(featureId, basinClass));
            builder.set("basin_class", basinClass);
            builder.set("subbasin_id", featureId);
            builder.set("outlet_id", outletId);
            builder.set("cell_count", cellCount);
            builder.set("area_m2", Double.isFinite(area) ? area : null);
            builder.set("area_ha", Double.isFinite(area) ? area / 10000d : null);
            builder.set("stream_order", network.streamOrder()[outletId]);
            features.add(builder.buildFeature("basins." + featureId));
            builder.reset();

            if (envelope == null) {
                envelope = new Envelope(geometry.getEnvelopeInternal());
            } else {
                envelope.expandToInclude(geometry.getEnvelopeInternal());
            }
            featureId++;
        }

        if (features.isEmpty()) {
            return null;
        }

        VectorLayer layer = new VectorLayer(buildOutputName("Cuencas", request), "");
        layer.setSourceName("Cuencas y subcuencas derivadas de " + request.rasterLayer().getName());
        layer.setSourceCRS(grid.sourceCrsCode());
        layer.setFeatureCount(features.size());
        layer.setFillColor(new Color(59, 130, 246, 88));
        layer.setBorderColor(new Color(30, 64, 175));
        layer.setPolygonFillStyle(Layer.PolygonFillStyle.SOLID);
        VectorLayerUtils.populateFieldConfigs(layer, schema);
        applyBasinSymbology(layer);

        ShapefileData data = new ShapefileData(
                features,
                envelope,
                layer.getSourceName(),
                features.size(),
                "Cuencas y subcuencas delimitadas desde el DEM.",
                schema
        );
        return new GeneratedVectorLayer(layer, data, "basins");
    }

    private static GeneratedVectorLayer buildOutletLayer(DrainageExtractionService.HydrologyGrid grid,
                                                         AnalysisRequest request,
                                                         FlowNetwork network) throws Exception {
        if (network.retainedOutletIds().isEmpty()) {
            return null;
        }
        SimpleFeatureType schema = buildPointSchema("Outlets", grid.sourceCrsCode(),
                "outlet_id", Integer.class,
                "basin_name", String.class,
                "cell_count", Integer.class,
                "area_m2", Double.class,
                "area_ha", Double.class,
                "acc_cells", Long.class,
                "stream_order", Integer.class
        );

        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        Envelope envelope = null;
        int largest = network.retainedOutletIds().stream()
                .map(id -> network.basinCellCounts().getOrDefault(id, 0))
                .max(Integer::compareTo)
                .orElse(1);
        int featureId = 1;
        for (int outletId : network.retainedOutletIds()) {
            Coordinate coordinate = grid.worldCoordinate(outletId / grid.width(), outletId % grid.width());
            if (coordinate == null) {
                continue;
            }
            Point point = GEOMETRY_FACTORY.createPoint(coordinate);
            int cellCount = network.basinCellCounts().getOrDefault(outletId, 0);
            double area = grid.cellAreaSquareMeters() > 0d ? cellCount * grid.cellAreaSquareMeters() : Double.NaN;
            String basinClass = classifyBasin(cellCount, largest);

            builder.set("the_geom", point);
            builder.set("outlet_id", outletId);
            builder.set("basin_name", buildBasinName(featureId, basinClass));
            builder.set("cell_count", cellCount);
            builder.set("area_m2", Double.isFinite(area) ? area : null);
            builder.set("area_ha", Double.isFinite(area) ? area / 10000d : null);
            builder.set("acc_cells", grid.accumulation()[outletId]);
            builder.set("stream_order", network.streamOrder()[outletId]);
            features.add(builder.buildFeature("outlets." + featureId));
            builder.reset();

            if (envelope == null) {
                envelope = new Envelope(point.getEnvelopeInternal());
            } else {
                envelope.expandToInclude(point.getEnvelopeInternal());
            }
            featureId++;
        }

        if (features.isEmpty()) {
            return null;
        }

        Layer layer = new Layer(buildOutputName("Outlets", request), "", "VECTOR");
        layer.setSourceName("Puntos de salida derivados de " + request.rasterLayer().getName());
        layer.setSourceCRS(grid.sourceCrsCode());
        layer.setFeatureCount(features.size());
        layer.setPointColor(new Color(15, 23, 42));
        layer.setPointSize(11);
        layer.setPointSymbolStyle(Layer.PointSymbolStyle.TARGET);
        VectorLayerUtils.populateFieldConfigs(layer, schema);

        ShapefileData data = new ShapefileData(
                features,
                envelope,
                layer.getSourceName(),
                features.size(),
                "Outlets derivados del DEM.",
                schema
        );
        return new GeneratedVectorLayer(layer, data, "outlets");
    }

    private static GeneratedVectorLayer buildFlowArrowLayer(DrainageExtractionService.HydrologyGrid grid,
                                                            AnalysisRequest request,
                                                            FlowNetwork network) throws Exception {
        List<Integer> candidates = new ArrayList<>();
        for (int index = 0; index < network.activeChannels().length; index++) {
            if (network.activeChannels()[index] && network.downstreamActive()[index] >= 0) {
                candidates.add(index);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }

        candidates.sort(
                Comparator.<Integer>comparingInt(index -> network.streamOrder()[index]).reversed()
                        .thenComparing(Comparator.<Integer>comparingLong(index -> grid.accumulation()[index]).reversed())
        );
        double spacingWorld = Math.max(Math.abs(grid.cellWidthWorld()), Math.abs(grid.cellHeightWorld())) * 7d;
        int maxArrows = Math.max(24, Math.min(180, candidates.size() / 10));

        SimpleFeatureType schema = buildPolygonSchema("FlowArrows", grid.sourceCrsCode(),
                "arrow_id", Integer.class,
                "flow_class", String.class,
                "acc_cells", Long.class,
                "flow_deg", Double.class,
                "stream_order", Integer.class
        );

        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        List<Coordinate> anchors = new ArrayList<>();
        Envelope envelope = null;
        int featureId = 1;

        for (int index : candidates) {
            Coordinate start = grid.worldCoordinate(index / grid.width(), index % grid.width());
            Coordinate end = followArrowTarget(grid, index);
            if (start == null || end == null || start.distance(end) <= 0d) {
                continue;
            }
            int order = Math.max(1, network.streamOrder()[index]);
            double arrowSpacing = spacingWorld * Math.max(0.8d, 1d + ((order - 1) * 0.18d));
            if (isTooClose(start, anchors, arrowSpacing)) {
                continue;
            }
            Polygon arrow = buildArrowPolygon(start, end, grid, order);
            if (arrow == null || arrow.isEmpty()) {
                continue;
            }

            builder.set("the_geom", arrow);
            builder.set("arrow_id", featureId);
            builder.set("flow_class", classifyChannel(grid.accumulation()[index], request.channelThreshold()));
            builder.set("acc_cells", grid.accumulation()[index]);
            builder.set("flow_deg", computeAzimuthDegrees(start, end));
            builder.set("stream_order", order);
            features.add(builder.buildFeature("flow_arrows." + featureId));
            builder.reset();
            anchors.add(start);

            if (envelope == null) {
                envelope = new Envelope(arrow.getEnvelopeInternal());
            } else {
                envelope.expandToInclude(arrow.getEnvelopeInternal());
            }
            featureId++;
            if (features.size() >= maxArrows) {
                break;
            }
        }

        if (features.isEmpty()) {
            return null;
        }

        VectorLayer layer = new VectorLayer(buildOutputName("Flechas de flujo", request), "");
        layer.setSourceName("Flechas poligonales de sentido de flujo derivadas de " + request.rasterLayer().getName());
        layer.setSourceCRS(grid.sourceCrsCode());
        layer.setFeatureCount(features.size());
        layer.setFillColor(new Color(29, 78, 216, 185));
        layer.setBorderColor(new Color(8, 47, 107));
        layer.setPolygonFillStyle(Layer.PolygonFillStyle.SOLID);
        VectorLayerUtils.populateFieldConfigs(layer, schema);
        applyFlowArrowSymbology(layer);

        ShapefileData data = new ShapefileData(
                features,
                envelope,
                layer.getSourceName(),
                features.size(),
                "Flechas poligonales de sentido de flujo.",
                schema
        );
        return new GeneratedVectorLayer(layer, data, "flow_arrows");
    }

    private static GeneratedVectorLayer buildStreamOrderLineLayer(DrainageExtractionService.HydrologyGrid grid,
                                                                 AnalysisRequest request,
                                                                 FlowNetwork network) throws Exception {
        List<StreamOrderBranch> branches = traceStreamOrderBranches(grid, network, request.channelThreshold());
        if (branches.isEmpty()) {
            return null;
        }

        SimpleFeatureType schema = buildLineSchema("OrdenCauces", grid.sourceCrsCode(),
                "segment_id", Integer.class,
                "stream_order", Integer.class,
                "order_label", String.class,
                "flow_class", String.class,
                "acc_cells", Long.class,
                "length_m", Double.class
        );

        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        Envelope envelope = null;
        int maxOrder = 1;
        int featureId = 1;

        for (StreamOrderBranch branch : branches) {
            if (branch == null || branch.line() == null || branch.line().isEmpty() || branch.line().getNumPoints() < 2) {
                continue;
            }
            maxOrder = Math.max(maxOrder, branch.streamOrder());
            builder.set("the_geom", branch.line());
            builder.set("segment_id", featureId);
            builder.set("stream_order", branch.streamOrder());
            builder.set("order_label", "Orden " + branch.streamOrder());
            builder.set("flow_class", branch.flowClass());
            builder.set("acc_cells", branch.maxAccumulationCells());
            builder.set("length_m", branch.lengthMeters());
            features.add(builder.buildFeature("stream_order." + featureId));
            builder.reset();

            if (envelope == null) {
                envelope = new Envelope(branch.line().getEnvelopeInternal());
            } else {
                envelope.expandToInclude(branch.line().getEnvelopeInternal());
            }
            featureId++;
        }

        if (features.isEmpty()) {
            return null;
        }

        VectorLayer layer = new VectorLayer(buildOutputName("Orden de cauces", request), "");
        layer.setSourceName("Orden de cauces derivado de " + request.rasterLayer().getName());
        layer.setSourceCRS(grid.sourceCrsCode());
        layer.setFeatureCount(features.size());
        layer.setLineColor(new Color(29, 78, 216));
        layer.setLineWidth(1.45f);
        layer.setLineSymbolStyle(Layer.LineSymbolStyle.SOLID);
        VectorLayerUtils.populateFieldConfigs(layer, schema);
        applyStreamOrderSymbology(layer, maxOrder);

        ShapefileData data = new ShapefileData(
                features,
                envelope,
                layer.getSourceName(),
                features.size(),
                "Red lineal con orden de cauces derivada del DEM.",
                schema
        );
        return new GeneratedVectorLayer(layer, data, OP_STREAM_ORDER_LINES);
    }

    private static List<StreamOrderBranch> traceStreamOrderBranches(DrainageExtractionService.HydrologyGrid grid,
                                                                    FlowNetwork network,
                                                                    int channelThreshold) {
        List<StreamOrderBranch> branches = new ArrayList<>();
        boolean[] traversed = new boolean[network.activeChannels().length];

        for (int index = 0; index < network.activeChannels().length; index++) {
            if (!network.activeChannels()[index]) {
                continue;
            }
            if (network.upstreamCount()[index] != 1) {
                StreamOrderBranch branch = traceStreamOrderBranch(index, grid, network, traversed, channelThreshold);
                if (branch != null) {
                    branches.add(branch);
                }
            }
        }

        for (int index = 0; index < network.activeChannels().length; index++) {
            if (!network.activeChannels()[index] || traversed[index]) {
                continue;
            }
            StreamOrderBranch branch = traceStreamOrderBranch(index, grid, network, traversed, channelThreshold);
            if (branch != null) {
                branches.add(branch);
            }
        }
        return branches;
    }

    private static StreamOrderBranch traceStreamOrderBranch(int startIndex,
                                                            DrainageExtractionService.HydrologyGrid grid,
                                                            FlowNetwork network,
                                                            boolean[] traversed,
                                                            int channelThreshold) {
        Coordinate start = grid.worldCoordinate(startIndex / grid.width(), startIndex % grid.width());
        if (start == null) {
            return null;
        }

        int streamOrder = Math.max(1, network.streamOrder()[startIndex]);
        List<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(new Coordinate(start));
        long maxAccumulation = grid.accumulation()[startIndex];
        int current = startIndex;
        int guard = 0;

        while (current >= 0 && guard < network.downstreamActive().length) {
            traversed[current] = true;
            int nextActive = network.downstreamActive()[current];
            PathTraceResult path;
            if (nextActive >= 0) {
                path = appendPathToTarget(grid, current, nextActive, coordinates, true);
                if (!path.reachedTarget()) {
                    break;
                }
                maxAccumulation = Math.max(maxAccumulation, path.maxAccumulationCells());
                if (network.streamOrder()[nextActive] != streamOrder || network.upstreamCount()[nextActive] != 1) {
                    break;
                }
                current = nextActive;
            } else {
                path = appendPathToTarget(grid, current, -1, coordinates, false);
                maxAccumulation = Math.max(maxAccumulation, path.maxAccumulationCells());
                break;
            }
            guard++;
        }

        if (coordinates.size() < 2) {
            return null;
        }

        LineString line = GEOMETRY_FACTORY.createLineString(toUniqueCoordinateArray(coordinates));
        if (line.getNumPoints() < 2 || line.isEmpty()) {
            return null;
        }
        return new StreamOrderBranch(
                line,
                streamOrder,
                classifyChannel(maxAccumulation, channelThreshold),
                maxAccumulation,
                line.getLength() * estimateMetricScale(grid)
        );
    }

    private static PathTraceResult appendPathToTarget(DrainageExtractionService.HydrologyGrid grid,
                                                      int fromIndex,
                                                      int targetIndex,
                                                      List<Coordinate> coordinates,
                                                      boolean stopAtTarget) {
        int current = stopAtTarget ? grid.downstream()[fromIndex] : fromIndex;
        int guard = 0;
        long maxAccumulation = 0L;

        while (current >= 0 && guard < grid.downstream().length) {
            Coordinate coordinate = grid.worldCoordinate(current / grid.width(), current % grid.width());
            if (coordinate != null) {
                appendCoordinate(coordinates, coordinate);
            }
            maxAccumulation = Math.max(maxAccumulation, grid.accumulation()[current]);
            if (stopAtTarget && current == targetIndex) {
                return new PathTraceResult(true, maxAccumulation);
            }
            int next = grid.downstream()[current];
            if (!stopAtTarget && next < 0) {
                return new PathTraceResult(true, maxAccumulation);
            }
            current = next;
            guard++;
        }

        return new PathTraceResult(!stopAtTarget, maxAccumulation);
    }

    private static void appendCoordinate(List<Coordinate> coordinates, Coordinate coordinate) {
        if (coordinate == null) {
            return;
        }
        Coordinate copy = new Coordinate(coordinate);
        if (!coordinates.isEmpty()) {
            Coordinate last = coordinates.get(coordinates.size() - 1);
            if (last.distance(copy) <= 1e-12d) {
                return;
            }
        }
        coordinates.add(copy);
    }

    private static Coordinate[] toUniqueCoordinateArray(List<Coordinate> coordinates) {
        List<Coordinate> unique = new ArrayList<>();
        for (Coordinate coordinate : coordinates) {
            appendCoordinate(unique, coordinate);
        }
        return unique.toArray(new Coordinate[0]);
    }

    private static FlowNetwork buildFlowNetwork(DrainageExtractionService.HydrologyGrid grid,
                                                int channelThreshold,
                                                int minimumBasinCells) {
        int total = grid.width() * grid.height();
        boolean[] active = new boolean[total];
        int[] downstreamActive = new int[total];
        int[] upstreamCount = new int[total];
        Arrays.fill(downstreamActive, -1);

        for (int row = 0; row < grid.height(); row++) {
            for (int col = 0; col < grid.width(); col++) {
                if (!grid.isValidCell(row, col)) {
                    continue;
                }
                int index = grid.linearIndex(row, col);
                active[index] = grid.accumulation()[index] >= channelThreshold;
            }
        }

        for (int index = 0; index < total; index++) {
            if (!active[index]) {
                continue;
            }
            int next = followToNextActive(index, grid.downstream(), active);
            downstreamActive[index] = next;
            if (next >= 0) {
                upstreamCount[next]++;
            }
        }

        int[] streamOrder = computeStrahlerOrder(active, downstreamActive, upstreamCount);
        int[] outletByCell = computeOutletByCell(grid);
        Map<Integer, Integer> basinCellCounts = new LinkedHashMap<>();
        Map<Integer, List<Integer>> cellsByOutlet = new LinkedHashMap<>();

        for (int row = 0; row < grid.height(); row++) {
            for (int col = 0; col < grid.width(); col++) {
                if (!grid.isValidCell(row, col)) {
                    continue;
                }
                int index = grid.linearIndex(row, col);
                int outlet = outletByCell[index];
                if (outlet < 0) {
                    continue;
                }
                basinCellCounts.merge(outlet, 1, Integer::sum);
                cellsByOutlet.computeIfAbsent(outlet, key -> new ArrayList<>()).add(index);
            }
        }

        List<Integer> retained = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : basinCellCounts.entrySet()) {
            if (entry.getValue() >= minimumBasinCells) {
                retained.add(entry.getKey());
            }
        }
        if (retained.isEmpty() && !basinCellCounts.isEmpty()) {
            retained.add(
                    basinCellCounts.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse(-1)
            );
            retained.removeIf(value -> value < 0);
        }
        retained.sort(Comparator.comparingInt((Integer outlet) -> basinCellCounts.getOrDefault(outlet, 0)).reversed());

        return new FlowNetwork(active, downstreamActive, upstreamCount, streamOrder, outletByCell, basinCellCounts, cellsByOutlet, retained);
    }

    private static int[] computeOutletByCell(DrainageExtractionService.HydrologyGrid grid) {
        int total = grid.width() * grid.height();
        int[] outletByCell = new int[total];
        Arrays.fill(outletByCell, Integer.MIN_VALUE);
        for (int row = 0; row < grid.height(); row++) {
            for (int col = 0; col < grid.width(); col++) {
                if (!grid.isValidCell(row, col)) {
                    continue;
                }
                int index = grid.linearIndex(row, col);
                if (outletByCell[index] == Integer.MIN_VALUE) {
                    resolveOutlet(index, grid, outletByCell);
                }
            }
        }
        return outletByCell;
    }

    private static int resolveOutlet(int startIndex,
                                     DrainageExtractionService.HydrologyGrid grid,
                                     int[] outletByCell) {
        List<Integer> trail = new ArrayList<>();
        int current = startIndex;
        int guard = 0;
        while (current >= 0 && guard < grid.downstream().length) {
            if (outletByCell[current] != Integer.MIN_VALUE) {
                int cached = outletByCell[current];
                for (int index : trail) {
                    outletByCell[index] = cached;
                }
                return cached;
            }
            trail.add(current);
            int next = grid.downstream()[current];
            if (next < 0) {
                for (int index : trail) {
                    outletByCell[index] = current;
                }
                return current;
            }
            current = next;
            guard++;
        }
        for (int index : trail) {
            outletByCell[index] = -1;
        }
        return -1;
    }

    private static int[] computeStrahlerOrder(boolean[] active, int[] downstreamActive, int[] upstreamCount) {
        int total = downstreamActive.length;
        int[] order = new int[total];
        int[] processedUpstream = new int[total];
        int[] maxOrder = new int[total];
        int[] maxOrderCount = new int[total];
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        for (int index = 0; index < total; index++) {
            if (active[index] && upstreamCount[index] == 0) {
                order[index] = 1;
                queue.add(index);
            }
        }

        while (!queue.isEmpty()) {
            int index = queue.removeFirst();
            int next = downstreamActive[index];
            if (next < 0) {
                continue;
            }
            int currentOrder = Math.max(1, order[index]);
            if (currentOrder > maxOrder[next]) {
                maxOrder[next] = currentOrder;
                maxOrderCount[next] = 1;
            } else if (currentOrder == maxOrder[next]) {
                maxOrderCount[next]++;
            }

            processedUpstream[next]++;
            if (processedUpstream[next] >= upstreamCount[next]) {
                order[next] = maxOrder[next] + (maxOrderCount[next] >= 2 ? 1 : 0);
                if (order[next] <= 0) {
                    order[next] = Math.max(1, maxOrder[next]);
                }
                queue.add(next);
            }
        }

        for (int index = 0; index < total; index++) {
            if (active[index] && order[index] <= 0) {
                order[index] = 1;
            }
        }
        return order;
    }

    private static int followToNextActive(int startIndex, int[] downstream, boolean[] active) {
        int current = downstream[startIndex];
        int guard = 0;
        while (current >= 0 && guard < downstream.length) {
            if (active[current]) {
                return current;
            }
            current = downstream[current];
            guard++;
        }
        return -1;
    }

    private static Coordinate followArrowTarget(DrainageExtractionService.HydrologyGrid grid, int startIndex) {
        Coordinate start = grid.worldCoordinate(startIndex / grid.width(), startIndex % grid.width());
        if (start == null) {
            return null;
        }
        double desiredWorldLength = Math.max(Math.abs(grid.cellWidthWorld()), Math.abs(grid.cellHeightWorld())) * 3.5d;
        double accumulated = 0d;
        int current = startIndex;
        Coordinate last = start;
        int guard = 0;
        while (current >= 0 && guard < grid.downstream().length) {
            int next = grid.downstream()[current];
            if (next < 0) {
                break;
            }
            Coordinate nextCoordinate = grid.worldCoordinate(next / grid.width(), next % grid.width());
            if (nextCoordinate == null) {
                break;
            }
            accumulated += last.distance(nextCoordinate);
            last = nextCoordinate;
            current = next;
            guard++;
            if (accumulated >= desiredWorldLength) {
                break;
            }
        }
        return last.distance(start) > 0d ? last : null;
    }

    private static boolean isTooClose(Coordinate candidate, List<Coordinate> anchors, double minimumDistance) {
        for (Coordinate anchor : anchors) {
            if (anchor != null && anchor.distance(candidate) < minimumDistance) {
                return true;
            }
        }
        return false;
    }

    private static Polygon buildArrowPolygon(Coordinate start,
                                             Coordinate end,
                                             DrainageExtractionService.HydrologyGrid grid,
                                             int streamOrder) {
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double length = Math.hypot(dx, dy);
        if (!(length > 0d)) {
            return null;
        }

        double unitX = dx / length;
        double unitY = dy / length;
        double perpendicularX = -unitY;
        double perpendicularY = unitX;
        double baseSize = Math.max(Math.abs(grid.cellWidthWorld()), Math.abs(grid.cellHeightWorld()));
        double orderFactor = Math.max(1d, 1d + ((Math.max(1, streamOrder) - 1) * 0.18d));
        double shaftHalfWidth = baseSize * 0.18d * orderFactor;
        double headHalfWidth = baseSize * 0.42d * orderFactor;
        double headLength = Math.min(length * 0.5d, baseSize * 1.75d * orderFactor);
        double shaftLength = Math.max(baseSize * 0.85d, length - headLength);
        Coordinate shaftEnd = new Coordinate(start.x + unitX * shaftLength, start.y + unitY * shaftLength);

        return GEOMETRY_FACTORY.createPolygon(new Coordinate[]{
                new Coordinate(start.x + perpendicularX * shaftHalfWidth, start.y + perpendicularY * shaftHalfWidth),
                new Coordinate(shaftEnd.x + perpendicularX * shaftHalfWidth, shaftEnd.y + perpendicularY * shaftHalfWidth),
                new Coordinate(shaftEnd.x + perpendicularX * headHalfWidth, shaftEnd.y + perpendicularY * headHalfWidth),
                new Coordinate(end.x, end.y),
                new Coordinate(shaftEnd.x - perpendicularX * headHalfWidth, shaftEnd.y - perpendicularY * headHalfWidth),
                new Coordinate(shaftEnd.x - perpendicularX * shaftHalfWidth, shaftEnd.y - perpendicularY * shaftHalfWidth),
                new Coordinate(start.x - perpendicularX * shaftHalfWidth, start.y - perpendicularY * shaftHalfWidth),
                new Coordinate(start.x + perpendicularX * shaftHalfWidth, start.y + perpendicularY * shaftHalfWidth)
        });
    }

    private static Geometry buildBasinGeometry(DrainageExtractionService.HydrologyGrid grid,
                                               List<Integer> basinCells) {
        if (basinCells == null || basinCells.isEmpty()) {
            return null;
        }
        Map<Integer, List<Integer>> cellsByRow = new LinkedHashMap<>();
        for (int index : basinCells) {
            cellsByRow.computeIfAbsent(index / grid.width(), key -> new ArrayList<>()).add(index % grid.width());
        }

        double cellWidth = Math.abs(grid.cellWidthWorld());
        double cellHeight = Math.abs(grid.cellHeightWorld());
        List<Geometry> pieces = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> entry : cellsByRow.entrySet()) {
            int row = entry.getKey();
            List<Integer> columns = entry.getValue();
            columns.sort(Integer::compareTo);
            int start = columns.get(0);
            int previous = start;
            for (int i = 1; i < columns.size(); i++) {
                int current = columns.get(i);
                if (current == previous + 1) {
                    previous = current;
                    continue;
                }
                pieces.add(buildCellRunPolygon(grid, row, start, previous, cellWidth, cellHeight));
                start = current;
                previous = current;
            }
            pieces.add(buildCellRunPolygon(grid, row, start, previous, cellWidth, cellHeight));
        }
        return pieces.isEmpty() ? null : UnaryUnionOp.union(pieces);
    }

    private static Polygon buildCellRunPolygon(DrainageExtractionService.HydrologyGrid grid,
                                               int row,
                                               int startCol,
                                               int endCol,
                                               double cellWidth,
                                               double cellHeight) {
        double x1 = grid.minX() + (startCol * cellWidth);
        double x2 = grid.minX() + ((endCol + 1d) * cellWidth);
        double top = grid.maxY() - (row * cellHeight);
        double bottom = top - cellHeight;
        return GEOMETRY_FACTORY.createPolygon(new Coordinate[]{
                new Coordinate(x1, top),
                new Coordinate(x2, top),
                new Coordinate(x2, bottom),
                new Coordinate(x1, bottom),
                new Coordinate(x1, top)
        });
    }

    private static DerivativeMetrics computeDerivativeMetrics(DrainageExtractionService.HydrologyGrid grid,
                                                              int row,
                                                              int col) {
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
        double slopeRadians = Math.atan(Math.sqrt((dzdx * dzdx) + (dzdy * dzdy)));
        double aspectMath = Math.atan2(dzdy, -dzdx);
        if (aspectMath < 0d) {
            aspectMath += Math.PI * 2d;
        }
        double aspectDegrees = 90d - Math.toDegrees(aspectMath);
        if (aspectDegrees < 0d) {
            aspectDegrees += 360d;
        }
        return new DerivativeMetrics(slopeRadians, aspectMath, aspectDegrees);
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

    private static int resolveDirectionClass(int row, int col, int nextRow, int nextCol) {
        int dRow = nextRow - row;
        int dCol = nextCol - col;
        if (dRow < 0 && dCol == 0) return 0;
        if (dRow < 0 && dCol > 0) return 1;
        if (dRow == 0 && dCol > 0) return 2;
        if (dRow > 0 && dCol > 0) return 3;
        if (dRow > 0 && dCol == 0) return 4;
        if (dRow > 0 && dCol < 0) return 5;
        if (dRow == 0 && dCol < 0) return 6;
        return 7;
    }

    private static double computeAzimuthDegrees(Coordinate start, Coordinate end) {
        double azimuth = Math.toDegrees(Math.atan2(end.x - start.x, end.y - start.y));
        if (azimuth < 0d) {
            azimuth += 360d;
        }
        return azimuth;
    }

    private static GeneratedRasterLayer buildDerivedRasterLayer(DrainageExtractionService.HydrologyGrid grid,
                                                                AnalysisRequest request,
                                                                String label,
                                                                String operation,
                                                                String parameterSpec,
                                                                BufferedImage image,
                                                                boolean grayscale,
                                                                boolean autoContrast) {
        String sourcePath = request.rasterLayer().getPath() != null ? request.rasterLayer().getPath() : "";
        RasterLayer layer = new RasterLayer(buildOutputName(label, request), sourcePath);
        layer.setVisible(true);
        layer.setSourceCRS(grid.sourceCrsCode());
        layer.setSourceName(label + " derivado de " + request.rasterLayer().getName());
        layer.setFeatureCount(1);
        layer.setOpacity(resolveDefaultOpacity(operation));
        layer.setGrayscale(grayscale);
        layer.setAutoContrast(autoContrast);
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

    private static float resolveDefaultOpacity(String operation) {
        if (operation == null) {
            return 1.0f;
        }
        if (OP_HILLSHADE.equalsIgnoreCase(operation)) {
            return 0.48f;
        }
        if (OP_SLOPE.equalsIgnoreCase(operation) || OP_ASPECT.equalsIgnoreCase(operation)) {
            return OP_SLOPE.equalsIgnoreCase(operation) ? 0.78f : 0.74f;
        }
        if (OP_FLOW_DIRECTION.equalsIgnoreCase(operation)) {
            return 0.82f;
        }
        if (OP_FLOW_ACCUMULATION.equalsIgnoreCase(operation)) {
            return 0.86f;
        }
        return 1.0f;
    }

    private static SimpleFeatureType buildPolygonSchema(String name, String sourceCrsCode, Object... attributes) throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(safeTypeName(name));
        if (sourceCrsCode != null && !sourceCrsCode.isBlank()) {
            try {
                builder.setCRS(CRSDefinitions.decode(sourceCrsCode, true));
            } catch (Exception ignored) {
            }
        }
        builder.add("the_geom", Geometry.class);
        for (int i = 0; i + 1 < attributes.length; i += 2) {
            builder.add(String.valueOf(attributes[i]), (Class<?>) attributes[i + 1]);
        }
        return builder.buildFeatureType();
    }

    private static SimpleFeatureType buildPointSchema(String name, String sourceCrsCode, Object... attributes) throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(safeTypeName(name));
        if (sourceCrsCode != null && !sourceCrsCode.isBlank()) {
            try {
                builder.setCRS(CRSDefinitions.decode(sourceCrsCode, true));
            } catch (Exception ignored) {
            }
        }
        builder.add("the_geom", Point.class);
        for (int i = 0; i + 1 < attributes.length; i += 2) {
            builder.add(String.valueOf(attributes[i]), (Class<?>) attributes[i + 1]);
        }
        return builder.buildFeatureType();
    }

    private static SimpleFeatureType buildLineSchema(String name, String sourceCrsCode, Object... attributes) throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(safeTypeName(name));
        if (sourceCrsCode != null && !sourceCrsCode.isBlank()) {
            try {
                builder.setCRS(CRSDefinitions.decode(sourceCrsCode, true));
            } catch (Exception ignored) {
            }
        }
        builder.add("the_geom", LineString.class);
        for (int i = 0; i + 1 < attributes.length; i += 2) {
            builder.add(String.valueOf(attributes[i]), (Class<?>) attributes[i + 1]);
        }
        return builder.buildFeatureType();
    }

    private static String safeTypeName(String text) {
        String base = text != null && !text.isBlank() ? text.trim() : "analisis_hidro";
        String normalized = base.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]+", "_");
        normalized = normalized.replaceAll("_+", "_");
        normalized = normalized.replaceAll("^_+", "");
        normalized = normalized.replaceAll("_+$", "");
        if (normalized.isBlank()) {
            normalized = "analisis_hidro";
        }
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = "h_" + normalized;
        }
        return normalized;
    }

    private static String classifyChannel(long accumulationCells, int threshold) {
        if (accumulationCells >= (long) threshold * 8L) {
            return "Principal";
        }
        if (accumulationCells >= (long) threshold * 3L) {
            return "Secundaria";
        }
        return "Local";
    }

    private static String classifyBasin(int cellCount, int largest) {
        if (cellCount >= Math.max(1, (int) Math.round(largest * 0.6d))) {
            return "Principal";
        }
        if (cellCount >= Math.max(1, (int) Math.round(largest * 0.25d))) {
            return "Secundaria";
        }
        return "Local";
    }

    private static String buildBasinName(int basinId, String basinClass) {
        String prefix = "Principal".equalsIgnoreCase(basinClass)
                ? "Cuenca principal"
                : "Subcuenca";
        return prefix + " " + String.format(Locale.US, "%02d", Math.max(1, basinId));
    }

    private static String buildOutputName(String label, AnalysisRequest request) {
        String baseName = request.baseName() != null && !request.baseName().isBlank()
                ? request.baseName().trim()
                : request.rasterLayer().getName();
        return label + " - " + baseName;
    }

    private static String buildParameterSpec(AnalysisRequest request) {
        return "detail=" + request.detail().name()
                + ";conditioning=" + request.conditioning().name()
                + ";threshold=" + request.channelThreshold()
                + ";minBasinCells=" + request.minimumBasinCells()
                + ";baseName=" + sanitizeParameterValue(request.baseName());
    }

    private static Map<String, String> parseParameterSpec(String spec) {
        Map<String, String> values = new LinkedHashMap<>();
        if (spec == null || spec.isBlank()) {
            return values;
        }
        String[] parts = spec.split(";");
        for (String part : parts) {
            int idx = part.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            String key = part.substring(0, idx).trim();
            String value = part.substring(idx + 1).trim();
            values.put(key, value.replace("%3B", ";").replace("%3D", "="));
        }
        return values;
    }

    private static int parsePositiveInt(String value, int fallback, int minimum) {
        try {
            return Math.max(minimum, Integer.parseInt(value != null ? value.trim() : ""));
        } catch (Exception ex) {
            return Math.max(minimum, fallback);
        }
    }

    private static String sanitizeParameterValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(";", "%3B").replace("=", "%3D");
    }

    private static void applyBasinSymbology(Layer layer) {
        CategorizedSymbology symbology = layer.getPolygonCategorizedSymbology();
        symbology.setFieldName("basin_class");
        symbology.setLegendTitle("Cuencas");
        symbology.setLegendSubtitle("Principal, secundaria y local");
        symbology.clearRules();

        CategoryStyleRule local = symbology.getOrCreateRule("Local");
        local.setPrimaryColor(new Color(191, 219, 254, 105));
        local.setSecondaryColor(new Color(147, 197, 253));
        local.setLineWidth(1.0f);

        CategoryStyleRule secondary = symbology.getOrCreateRule("Secundaria");
        secondary.setPrimaryColor(new Color(96, 165, 250, 115));
        secondary.setSecondaryColor(new Color(59, 130, 246));
        secondary.setLineWidth(1.15f);

        CategoryStyleRule principal = symbology.getOrCreateRule("Principal");
        principal.setPrimaryColor(new Color(37, 99, 235, 125));
        principal.setSecondaryColor(new Color(30, 64, 175));
        principal.setLineWidth(1.4f);
    }

    private static void applyFlowArrowSymbology(Layer layer) {
        CategorizedSymbology symbology = layer.getPolygonCategorizedSymbology();
        symbology.setFieldName("flow_class");
        symbology.setLegendTitle("Sentido de flujo");
        symbology.setLegendSubtitle("Flechas por jerarquia de cauce");
        symbology.clearRules();

        CategoryStyleRule local = symbology.getOrCreateRule("Local");
        local.setPrimaryColor(new Color(125, 211, 252, 155));
        local.setSecondaryColor(new Color(14, 116, 144));
        local.setLineWidth(0.9f);

        CategoryStyleRule secondary = symbology.getOrCreateRule("Secundaria");
        secondary.setPrimaryColor(new Color(59, 130, 246, 178));
        secondary.setSecondaryColor(new Color(30, 64, 175));
        secondary.setLineWidth(1.1f);

        CategoryStyleRule principal = symbology.getOrCreateRule("Principal");
        principal.setPrimaryColor(new Color(29, 78, 216, 210));
        principal.setSecondaryColor(new Color(15, 23, 42));
        principal.setLineWidth(1.3f);
    }

    private static void applyStreamOrderSymbology(Layer layer, int maxOrder) {
        CategorizedSymbology symbology = layer.getLineCategorizedSymbology();
        symbology.setFieldName("order_label");
        symbology.setLegendTitle("Orden de cauces");
        symbology.setLegendSubtitle("Jerarquia Strahler");
        symbology.clearRules();

        int effectiveMax = Math.max(1, maxOrder);
        for (int order = 1; order <= effectiveMax; order++) {
            double ratio = effectiveMax == 1 ? 1d : (order - 1d) / (effectiveMax - 1d);
            Color color = interpolateColor(
                    ratio,
                    new Color(125, 211, 252),
                    new Color(59, 130, 246),
                    new Color(37, 99, 235),
                    new Color(15, 23, 42)
            );
            CategoryStyleRule rule = symbology.getOrCreateRule("Orden " + order);
            rule.setPrimaryColor(color);
            rule.setSecondaryColor(color.darker());
            rule.setLineStyle(Layer.LineSymbolStyle.SOLID);
            rule.setLineWidth(0.95f + ((order - 1) * 0.55f));
        }
    }

    private static double estimateMetricScale(DrainageExtractionService.HydrologyGrid grid) {
        double worldWidth = Math.max(Math.abs(grid.cellWidthWorld()), Math.abs(grid.cellHeightWorld()));
        double metricWidth = Math.max(grid.cellWidthMeters(), grid.cellHeightMeters());
        if (!(worldWidth > 0d) || !(metricWidth > 0d)) {
            return 1d;
        }
        return metricWidth / worldWidth;
    }

    public static PourPointResult generateBasinFromPourPoint(PourPointRequest request) throws Exception {
        validatePourPointRequest(request);
        DrainageExtractionService.HydrologyGrid grid = DrainageExtractionService.analyzeHydrologyGrid(
                request.rasterLayer(),
                request.detail(),
                request.conditioning()
        );
        FlowNetwork network = buildFlowNetwork(grid, request.channelThreshold(), 1);
        Coordinate targetCoordinate = transformCoordinate(request.coordinate(), request.coordinateCrs(), grid.sourceCrsCode());
        int snappedIndex = snapPourPoint(grid, targetCoordinate, request.snapRadiusCells(), request.channelThreshold());
        if (snappedIndex < 0) {
            throw new IllegalStateException("No se pudo ubicar un punto de drenaje valido dentro del DEM.");
        }

        List<Integer>[] upstream = buildUpstreamGraph(grid.downstream());
        List<Integer> basinCells = collectUpstreamCells(snappedIndex, upstream, grid.validMask(), grid.width(), grid.height());
        if (basinCells.isEmpty()) {
            throw new IllegalStateException("No se pudo delimitar una cuenca valida para el punto indicado.");
        }

        Geometry basinGeometry = buildBasinGeometry(grid, basinCells);
        if (basinGeometry == null || basinGeometry.isEmpty()) {
            throw new IllegalStateException("No se pudo construir la geometria de la cuenca.");
        }

        Coordinate snappedCoordinate = grid.worldCoordinate(snappedIndex / grid.width(), snappedIndex % grid.width());
        if (snappedCoordinate == null) {
            throw new IllegalStateException("No se pudo ubicar el outlet ajustado.");
        }

        String baseName = request.baseName() != null && !request.baseName().isBlank()
                ? request.baseName().trim()
                : request.rasterLayer().getName();
        GeneratedVectorLayer basinLayer = buildPourPointBasinLayer(grid, basinGeometry, basinCells.size(), snappedIndex, network, baseName);
        GeneratedVectorLayer outletLayer = buildPourPointOutletLayer(grid, snappedCoordinate, snappedIndex, basinCells.size(), network, baseName);
        return new PourPointResult(grid, basinLayer, outletLayer, snappedCoordinate, snappedIndex, basinCells.size());
    }

    public static BatchPourPointResult generateBasinsFromOutletLayer(BatchPourPointRequest request) throws Exception {
        validateBatchPourPointRequest(request);
        DrainageExtractionService.HydrologyGrid grid = DrainageExtractionService.analyzeHydrologyGrid(
                request.rasterLayer(),
                request.detail(),
                request.conditioning()
        );
        FlowNetwork network = buildFlowNetwork(grid, request.channelThreshold(), 1);
        List<Integer>[] upstream = buildUpstreamGraph(grid.downstream());
        List<BatchOutletSeed> seeds = collectBatchOutletSeeds(request, grid, network, upstream);
        if (seeds.isEmpty()) {
            throw new IllegalStateException("No se pudo delimitar ninguna cuenca valida a partir de los outlets elegidos.");
        }

        String baseName = request.baseName() != null && !request.baseName().isBlank()
                ? request.baseName().trim()
                : request.rasterLayer().getName();
        GeneratedVectorLayer basinsLayer = buildBatchPourPointBasinLayer(grid, network, baseName, request.outletLayerName(), seeds);
        GeneratedVectorLayer outletsLayer = buildBatchPourPointOutletLayer(grid, network, baseName, request.outletLayerName(), seeds);
        int requestedCount = request.outletData() != null ? request.outletData().getFeatureCount() : seeds.size();
        return new BatchPourPointResult(grid, basinsLayer, outletsLayer, requestedCount, seeds.size());
    }

    private static void validatePourPointRequest(PourPointRequest request) {
        if (request == null || !(request.rasterLayer() instanceof RasterLayer)) {
            throw new IllegalArgumentException("Debes indicar un DEM raster valido.");
        }
        if (request.coordinate() == null) {
            throw new IllegalArgumentException("Debes indicar un outlet o pour point.");
        }
        if (request.channelThreshold() < 2) {
            throw new IllegalArgumentException("El umbral de acumulacion debe ser 2 o mayor.");
        }
        if (request.snapRadiusCells() < 0) {
            throw new IllegalArgumentException("El radio de ajuste no puede ser negativo.");
        }
    }

    private static void validateBatchPourPointRequest(BatchPourPointRequest request) {
        if (request == null || !(request.rasterLayer() instanceof RasterLayer)) {
            throw new IllegalArgumentException("Debes indicar un DEM raster valido.");
        }
        if (request.outletData() == null || request.outletData().getFeatures() == null || request.outletData().getFeatures().isEmpty()) {
            throw new IllegalArgumentException("Debes indicar una capa puntual con uno o mas outlets.");
        }
        if (request.channelThreshold() < 2) {
            throw new IllegalArgumentException("El umbral de acumulacion debe ser 2 o mayor.");
        }
        if (request.snapRadiusCells() < 0) {
            throw new IllegalArgumentException("El radio de ajuste no puede ser negativo.");
        }
    }

    private static List<BatchOutletSeed> collectBatchOutletSeeds(BatchPourPointRequest request,
                                                                 DrainageExtractionService.HydrologyGrid grid,
                                                                 FlowNetwork network,
                                                                 List<Integer>[] upstream) throws Exception {
        List<BatchOutletSeed> seeds = new ArrayList<>();
        Map<Integer, BatchOutletSeed> bySnappedIndex = new LinkedHashMap<>();
        int featureOrdinal = 1;
        for (SimpleFeature feature : request.outletData().getFeatures()) {
            if (feature == null) {
                continue;
            }
            Point representative = extractRepresentativePoint(feature);
            if (representative == null || representative.isEmpty()) {
                featureOrdinal++;
                continue;
            }
            Coordinate transformed = transformCoordinate(
                    representative.getCoordinate(),
                    request.outletDataCrs(),
                    grid.sourceCrsCode()
            );
            int snappedIndex = snapPourPoint(grid, transformed, request.snapRadiusCells(), request.channelThreshold());
            if (snappedIndex < 0 || bySnappedIndex.containsKey(snappedIndex)) {
                featureOrdinal++;
                continue;
            }

            List<Integer> basinCells = collectUpstreamCells(snappedIndex, upstream, grid.validMask(), grid.width(), grid.height());
            if (basinCells.isEmpty()) {
                featureOrdinal++;
                continue;
            }
            Geometry basinGeometry = buildBasinGeometry(grid, basinCells);
            if (basinGeometry == null || basinGeometry.isEmpty()) {
                featureOrdinal++;
                continue;
            }
            Coordinate snappedCoordinate = grid.worldCoordinate(snappedIndex / grid.width(), snappedIndex % grid.width());
            if (snappedCoordinate == null) {
                featureOrdinal++;
                continue;
            }

            String sourceId = feature.getID() != null && !feature.getID().isBlank()
                    ? feature.getID()
                    : "outlet." + featureOrdinal;
            String sourceLabel = resolveFeatureLabel(feature, featureOrdinal);
            BatchOutletSeed seed = new BatchOutletSeed(
                    featureOrdinal,
                    sourceId,
                    sourceLabel,
                    snappedIndex,
                    snappedCoordinate,
                    basinCells.size(),
                    basinGeometry,
                    grid.accumulation()[snappedIndex],
                    Math.max(1, network.streamOrder()[snappedIndex])
            );
            bySnappedIndex.put(snappedIndex, seed);
            seeds.add(seed);
            featureOrdinal++;
        }
        return seeds;
    }

    private static GeneratedVectorLayer buildBatchPourPointBasinLayer(DrainageExtractionService.HydrologyGrid grid,
                                                                      FlowNetwork network,
                                                                      String baseName,
                                                                      String sourceLayerName,
                                                                      List<BatchOutletSeed> seeds) throws Exception {
        SimpleFeatureType schema = buildPolygonSchema("CuencasOutletBatch", grid.sourceCrsCode(),
                "basin_id", Integer.class,
                "basin_name", String.class,
                "basin_class", String.class,
                "subbasin_id", Integer.class,
                "source_id", String.class,
                "source_label", String.class,
                "pour_index", Integer.class,
                "cell_count", Integer.class,
                "area_m2", Double.class,
                "area_ha", Double.class,
                "acc_cells", Long.class,
                "stream_order", Integer.class
        );
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        List<SimpleFeature> features = new ArrayList<>();
        Envelope envelope = null;
        int largest = seeds.stream().map(seed -> seed.cellCount()).max(Integer::compareTo).orElse(1);

        int basinId = 1;
        for (BatchOutletSeed seed : seeds) {
            double area = grid.cellAreaSquareMeters() > 0d ? seed.cellCount() * grid.cellAreaSquareMeters() : Double.NaN;
            String basinClass = classifyBasin(seed.cellCount(), largest);
            builder.set("the_geom", seed.basinGeometry());
            builder.set("basin_id", basinId);
            builder.set("basin_name", buildBasinName(basinId, basinClass));
            builder.set("basin_class", basinClass);
            builder.set("subbasin_id", basinId);
            builder.set("source_id", seed.sourceId());
            builder.set("source_label", seed.sourceLabel());
            builder.set("pour_index", seed.snappedIndex());
            builder.set("cell_count", seed.cellCount());
            builder.set("area_m2", Double.isFinite(area) ? area : null);
            builder.set("area_ha", Double.isFinite(area) ? area / 10000d : null);
            builder.set("acc_cells", seed.accumulationCells());
            builder.set("stream_order", seed.streamOrder());
            features.add(builder.buildFeature("batch_basins." + basinId));
            builder.reset();

            if (envelope == null) {
                envelope = new Envelope(seed.basinGeometry().getEnvelopeInternal());
            } else {
                envelope.expandToInclude(seed.basinGeometry().getEnvelopeInternal());
            }
            basinId++;
        }

        VectorLayer layer = new VectorLayer("Cuencas outlet - " + baseName, "");
        layer.setSourceName("Cuencas delimitadas por lote desde outlets de " + (sourceLayerName != null && !sourceLayerName.isBlank() ? sourceLayerName : baseName));
        layer.setSourceCRS(grid.sourceCrsCode());
        layer.setFeatureCount(features.size());
        layer.setFillColor(new Color(59, 130, 246, 88));
        layer.setBorderColor(new Color(30, 64, 175));
        layer.setPolygonFillStyle(Layer.PolygonFillStyle.SOLID);
        VectorLayerUtils.populateFieldConfigs(layer, schema);
        applyBasinSymbology(layer);

        ShapefileData data = new ShapefileData(
                features,
                envelope,
                layer.getSourceName(),
                features.size(),
                "Cuencas delimitadas por lote desde multiples outlets.",
                schema
        );
        return new GeneratedVectorLayer(layer, data, "pour_point_basins_batch");
    }

    private static GeneratedVectorLayer buildBatchPourPointOutletLayer(DrainageExtractionService.HydrologyGrid grid,
                                                                       FlowNetwork network,
                                                                       String baseName,
                                                                       String sourceLayerName,
                                                                       List<BatchOutletSeed> seeds) throws Exception {
        SimpleFeatureType schema = buildPointSchema("OutletsBatch", grid.sourceCrsCode(),
                "outlet_id", Integer.class,
                "basin_name", String.class,
                "source_id", String.class,
                "source_label", String.class,
                "pour_index", Integer.class,
                "cell_count", Integer.class,
                "area_m2", Double.class,
                "area_ha", Double.class,
                "acc_cells", Long.class,
                "stream_order", Integer.class
        );
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        List<SimpleFeature> features = new ArrayList<>();
        Envelope envelope = null;
        int largest = seeds.stream().map(seed -> seed.cellCount()).max(Integer::compareTo).orElse(1);

        int outletId = 1;
        for (BatchOutletSeed seed : seeds) {
            double area = grid.cellAreaSquareMeters() > 0d ? seed.cellCount() * grid.cellAreaSquareMeters() : Double.NaN;
            String basinClass = classifyBasin(seed.cellCount(), largest);
            Point point = GEOMETRY_FACTORY.createPoint(seed.snappedCoordinate());
            builder.set("the_geom", point);
            builder.set("outlet_id", outletId);
            builder.set("basin_name", buildBasinName(outletId, basinClass));
            builder.set("source_id", seed.sourceId());
            builder.set("source_label", seed.sourceLabel());
            builder.set("pour_index", seed.snappedIndex());
            builder.set("cell_count", seed.cellCount());
            builder.set("area_m2", Double.isFinite(area) ? area : null);
            builder.set("area_ha", Double.isFinite(area) ? area / 10000d : null);
            builder.set("acc_cells", seed.accumulationCells());
            builder.set("stream_order", seed.streamOrder());
            features.add(builder.buildFeature("batch_outlets." + outletId));
            builder.reset();

            if (envelope == null) {
                envelope = new Envelope(point.getEnvelopeInternal());
            } else {
                envelope.expandToInclude(point.getEnvelopeInternal());
            }
            outletId++;
        }

        Layer layer = new Layer("Outlets ajustados - " + baseName, "", "VECTOR");
        layer.setSourceName("Outlets ajustados por lote desde " + (sourceLayerName != null && !sourceLayerName.isBlank() ? sourceLayerName : baseName));
        layer.setSourceCRS(grid.sourceCrsCode());
        layer.setFeatureCount(features.size());
        layer.setPointColor(new Color(15, 23, 42));
        layer.setPointSize(11);
        layer.setPointSymbolStyle(Layer.PointSymbolStyle.TARGET);
        VectorLayerUtils.populateFieldConfigs(layer, schema);

        ShapefileData data = new ShapefileData(
                features,
                envelope,
                layer.getSourceName(),
                features.size(),
                "Outlets ajustados por lote sobre el DEM.",
                schema
        );
        return new GeneratedVectorLayer(layer, data, "pour_point_outlets_batch");
    }

    private static Point extractRepresentativePoint(SimpleFeature feature) {
        if (feature == null || !(feature.getDefaultGeometry() instanceof Geometry geometry) || geometry.isEmpty()) {
            return null;
        }
        if (geometry instanceof Point point) {
            return point;
        }
        Point centroid = geometry.getCentroid();
        return centroid != null && !centroid.isEmpty() ? centroid : null;
    }

    private static String resolveFeatureLabel(SimpleFeature feature, int fallbackIndex) {
        if (feature != null) {
            for (Object attribute : feature.getAttributes()) {
                if (attribute instanceof String text && !text.isBlank()) {
                    return text.trim();
                }
            }
            if (feature.getID() != null && !feature.getID().isBlank()) {
                return feature.getID();
            }
        }
        return "Outlet " + fallbackIndex;
    }

    private static GeneratedVectorLayer buildPourPointBasinLayer(DrainageExtractionService.HydrologyGrid grid,
                                                                 Geometry basinGeometry,
                                                                 int cellCount,
                                                                 int snappedIndex,
                                                                 FlowNetwork network,
                                                                 String baseName) throws Exception {
        SimpleFeatureType schema = buildPolygonSchema("CuencaOutlet", grid.sourceCrsCode(),
                "basin_id", Integer.class,
                "basin_name", String.class,
                "pour_index", Integer.class,
                "cell_count", Integer.class,
                "area_m2", Double.class,
                "area_ha", Double.class,
                "acc_cells", Long.class,
                "stream_order", Integer.class
        );
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        double area = grid.cellAreaSquareMeters() > 0d ? cellCount * grid.cellAreaSquareMeters() : Double.NaN;
        builder.set("the_geom", basinGeometry);
        builder.set("basin_id", 1);
        builder.set("basin_name", "Cuenca outlet 01");
        builder.set("pour_index", snappedIndex);
        builder.set("cell_count", cellCount);
        builder.set("area_m2", Double.isFinite(area) ? area : null);
        builder.set("area_ha", Double.isFinite(area) ? area / 10000d : null);
        builder.set("acc_cells", grid.accumulation()[snappedIndex]);
        builder.set("stream_order", Math.max(1, network.streamOrder()[snappedIndex]));
        List<SimpleFeature> features = List.of(builder.buildFeature("pour_basin.1"));

        VectorLayer layer = new VectorLayer("Cuenca outlet - " + baseName, "");
        layer.setSourceName("Cuenca delimitada desde outlet sobre " + baseName);
        layer.setSourceCRS(grid.sourceCrsCode());
        layer.setFeatureCount(1);
        layer.setFillColor(new Color(29, 78, 216, 96));
        layer.setBorderColor(new Color(15, 23, 42));
        layer.setPolygonFillStyle(Layer.PolygonFillStyle.SOLID);
        VectorLayerUtils.populateFieldConfigs(layer, schema);

        ShapefileData data = new ShapefileData(
                features,
                new Envelope(basinGeometry.getEnvelopeInternal()),
                layer.getSourceName(),
                1,
                "Cuenca delimitada desde un pour point interactivo.",
                schema
        );
        return new GeneratedVectorLayer(layer, data, "pour_point_basin");
    }

    private static GeneratedVectorLayer buildPourPointOutletLayer(DrainageExtractionService.HydrologyGrid grid,
                                                                  Coordinate snappedCoordinate,
                                                                  int snappedIndex,
                                                                  int cellCount,
                                                                  FlowNetwork network,
                                                                  String baseName) throws Exception {
        SimpleFeatureType schema = buildPointSchema("OutletSnap", grid.sourceCrsCode(),
                "outlet_id", Integer.class,
                "basin_name", String.class,
                "pour_index", Integer.class,
                "cell_count", Integer.class,
                "area_m2", Double.class,
                "area_ha", Double.class,
                "acc_cells", Long.class,
                "stream_order", Integer.class
        );
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        Point point = GEOMETRY_FACTORY.createPoint(snappedCoordinate);
        double area = grid.cellAreaSquareMeters() > 0d ? cellCount * grid.cellAreaSquareMeters() : Double.NaN;
        builder.set("the_geom", point);
        builder.set("outlet_id", 1);
        builder.set("basin_name", "Cuenca outlet 01");
        builder.set("pour_index", snappedIndex);
        builder.set("cell_count", cellCount);
        builder.set("area_m2", Double.isFinite(area) ? area : null);
        builder.set("area_ha", Double.isFinite(area) ? area / 10000d : null);
        builder.set("acc_cells", grid.accumulation()[snappedIndex]);
        builder.set("stream_order", Math.max(1, network.streamOrder()[snappedIndex]));
        List<SimpleFeature> features = List.of(builder.buildFeature("pour_outlet.1"));

        Layer layer = new Layer("Outlet ajustado - " + baseName, "", "VECTOR");
        layer.setSourceName("Outlet ajustado desde un pour point sobre " + baseName);
        layer.setSourceCRS(grid.sourceCrsCode());
        layer.setFeatureCount(1);
        layer.setPointColor(new Color(15, 23, 42));
        layer.setPointSize(12);
        layer.setPointSymbolStyle(Layer.PointSymbolStyle.TARGET);
        VectorLayerUtils.populateFieldConfigs(layer, schema);

        ShapefileData data = new ShapefileData(
                features,
                new Envelope(point.getEnvelopeInternal()),
                layer.getSourceName(),
                1,
                "Outlet ajustado al flujo principal.",
                schema
        );
        return new GeneratedVectorLayer(layer, data, "pour_point_outlet");
    }

    private static Coordinate transformCoordinate(Coordinate coordinate, String sourceCrsCode, String targetCrsCode) throws Exception {
        if (coordinate == null) {
            return null;
        }
        String source = CRSDefinitions.normalizeCode(sourceCrsCode);
        String target = CRSDefinitions.normalizeCode(targetCrsCode);
        if (source.isBlank() || target.isBlank() || source.equalsIgnoreCase(target)) {
            return new Coordinate(coordinate);
        }
        MathTransform transform = CRS.findMathTransform(
                CRSDefinitions.decode(source, true),
                CRSDefinitions.decode(target, true),
                true
        );
        return JTS.transform(new Coordinate(coordinate), null, transform);
    }

    @SuppressWarnings("unchecked")
    private static List<Integer>[] buildUpstreamGraph(int[] downstream) {
        List<Integer>[] upstream = new List[downstream.length];
        for (int i = 0; i < downstream.length; i++) {
            upstream[i] = new ArrayList<>();
        }
        for (int index = 0; index < downstream.length; index++) {
            int next = downstream[index];
            if (next >= 0 && next < downstream.length) {
                upstream[next].add(index);
            }
        }
        return upstream;
    }

    private static List<Integer> collectUpstreamCells(int startIndex,
                                                      List<Integer>[] upstream,
                                                      boolean[][] validMask,
                                                      int width,
                                                      int height) {
        List<Integer> cells = new ArrayList<>();
        boolean[] visited = new boolean[upstream.length];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(startIndex);
        visited[startIndex] = true;

        while (!queue.isEmpty()) {
            int current = queue.removeFirst();
            int row = current / width;
            int col = current % width;
            if (row < 0 || row >= height || col < 0 || col >= width || !validMask[row][col]) {
                continue;
            }
            cells.add(current);
            for (int next : upstream[current]) {
                if (next >= 0 && next < upstream.length && !visited[next]) {
                    visited[next] = true;
                    queue.addLast(next);
                }
            }
        }
        return cells;
    }

    private static int snapPourPoint(DrainageExtractionService.HydrologyGrid grid,
                                     Coordinate coordinate,
                                     int snapRadiusCells,
                                     int channelThreshold) {
        if (coordinate == null) {
            return -1;
        }
        int row = grid.rowFromWorldY(coordinate.y);
        int col = grid.colFromWorldX(coordinate.x);
        int radius = Math.max(0, snapRadiusCells);
        int bestIndex = -1;
        long bestAccumulation = Long.MIN_VALUE;
        double bestDistance = Double.POSITIVE_INFINITY;

        for (int currentRow = row - radius; currentRow <= row + radius; currentRow++) {
            for (int currentCol = col - radius; currentCol <= col + radius; currentCol++) {
                if (!grid.isValidCell(currentRow, currentCol)) {
                    continue;
                }
                int index = grid.linearIndex(currentRow, currentCol);
                long accumulation = grid.accumulation()[index];
                Coordinate world = grid.worldCoordinate(currentRow, currentCol);
                if (world == null) {
                    continue;
                }
                double distance = world.distance(coordinate);
                boolean preferredChannel = accumulation >= channelThreshold;
                boolean bestIsChannel = bestIndex >= 0 && grid.accumulation()[bestIndex] >= channelThreshold;

                if (bestIndex < 0
                        || (preferredChannel && !bestIsChannel)
                        || (preferredChannel == bestIsChannel && accumulation > bestAccumulation)
                        || (preferredChannel == bestIsChannel && accumulation == bestAccumulation && distance < bestDistance)) {
                    bestIndex = index;
                    bestAccumulation = accumulation;
                    bestDistance = distance;
                }
            }
        }
        return bestIndex;
    }

    private static Color interpolateColor(double ratio, Color... colors) {
        if (colors == null || colors.length == 0) {
            return Color.GRAY;
        }
        if (colors.length == 1) {
            return colors[0];
        }
        double scaled = Math.max(0d, Math.min(1d, ratio)) * (colors.length - 1);
        int index = (int) Math.floor(scaled);
        if (index >= colors.length - 1) {
            return colors[colors.length - 1];
        }
        double t = scaled - index;
        Color a = colors[index];
        Color b = colors[index + 1];
        return new Color(
                (int) Math.round(a.getRed() + ((b.getRed() - a.getRed()) * t)),
                (int) Math.round(a.getGreen() + ((b.getGreen() - a.getGreen()) * t)),
                (int) Math.round(a.getBlue() + ((b.getBlue() - a.getBlue()) * t))
        );
    }

    public record AnalysisRequest(Layer rasterLayer,
                                  String baseName,
                                  DrainageExtractionService.AnalysisDetail detail,
                                  DrainageExtractionService.HydrologicConditioning conditioning,
                                  int channelThreshold,
                                  int minimumBasinCells,
                                  boolean generateHillshade,
                                  boolean generateSlope,
                                  boolean generateAspect,
                                  boolean generateFlowDirection,
                                  boolean generateFlowAccumulation,
                                  boolean generateStreamOrderLines,
                                  boolean generateBasins,
                                  boolean generateOutlets,
                                  boolean generateFlowArrows) {
    }

    public record AnalysisResult(DrainageExtractionService.HydrologyGrid grid,
                                 List<GeneratedRasterLayer> rasterLayers,
                                 List<GeneratedVectorLayer> vectorLayers) {
    }

    public record GeneratedRasterLayer(RasterLayer layer,
                                       LocalRasterData data,
                                       String operation) {
    }

    public record GeneratedVectorLayer(Layer layer,
                                       ShapefileData data,
                                       String operation) {
    }

    private record DerivativeMetrics(double slopeRadians,
                                     double aspectMathRadians,
                                     double aspectDegrees) {
    }

    public record PourPointRequest(Layer rasterLayer,
                                   String baseName,
                                   DrainageExtractionService.AnalysisDetail detail,
                                   DrainageExtractionService.HydrologicConditioning conditioning,
                                   int channelThreshold,
                                   int snapRadiusCells,
                                   Coordinate coordinate,
                                   String coordinateCrs) {
    }

    public record PourPointResult(DrainageExtractionService.HydrologyGrid grid,
                                  GeneratedVectorLayer basinLayer,
                                  GeneratedVectorLayer outletLayer,
                                  Coordinate snappedCoordinate,
                                  int snappedIndex,
                                  int basinCellCount) {
    }

    public record BatchPourPointRequest(Layer rasterLayer,
                                        String baseName,
                                        DrainageExtractionService.AnalysisDetail detail,
                                        DrainageExtractionService.HydrologicConditioning conditioning,
                                        int channelThreshold,
                                        int snapRadiusCells,
                                        ShapefileData outletData,
                                        String outletDataCrs,
                                        String outletLayerName) {
    }

    public record BatchPourPointResult(DrainageExtractionService.HydrologyGrid grid,
                                       GeneratedVectorLayer basinsLayer,
                                       GeneratedVectorLayer outletsLayer,
                                       int requestedCount,
                                       int generatedCount) {
    }

    private record FlowNetwork(boolean[] activeChannels,
                               int[] downstreamActive,
                               int[] upstreamCount,
                               int[] streamOrder,
                               int[] outletByCell,
                               Map<Integer, Integer> basinCellCounts,
                               Map<Integer, List<Integer>> cellsByOutlet,
                               List<Integer> retainedOutletIds) {
    }

    private record StreamOrderBranch(LineString line,
                                     int streamOrder,
                                     String flowClass,
                                     long maxAccumulationCells,
                                     double lengthMeters) {
    }

    private record PathTraceResult(boolean reachedTarget,
                                   long maxAccumulationCells) {
    }

    private record BatchOutletSeed(int sourceOrdinal,
                                   String sourceId,
                                   String sourceLabel,
                                   int snappedIndex,
                                   Coordinate snappedCoordinate,
                                   int cellCount,
                                   Geometry basinGeometry,
                                   long accumulationCells,
                                   int streamOrder) {
    }
}
